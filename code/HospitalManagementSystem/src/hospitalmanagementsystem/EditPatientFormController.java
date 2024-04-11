/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hospitalmanagementsystem;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.scene.text.Text;

/**
 *
 * @author WINDOWS 10
 */
public class EditPatientFormController implements Initializable {
    @FXML
    private Text filePathText;

    @FXML
    private AnchorPane main_form;

    @FXML
    private TextField edit_patientID;


    @FXML
    private TextField edit_name;

    @FXML
    private ComboBox<String> edit_gender;

    @FXML
    private TextField edit_contactNumber;


    @FXML
    private TextArea edit_address;

    @FXML
    private ComboBox<String> edit_status;

    @FXML
    private Button edit_updateBtn;

    private AlertMessage alert = new AlertMessage();

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    public void updateBtn() {

        if (edit_patientID.getText().isEmpty() || edit_name.getText().isEmpty()
                || edit_gender.getSelectionModel().getSelectedItem() == null
                || edit_contactNumber.getText().isEmpty()
                || edit_address.getText().isEmpty()
                || edit_status.getSelectionModel().getSelectedItem() == null) {
            alert.errorMessage("Please fill all blank fields");
        } else {
            String updateData = "UPDATE patient SET full_name = ?, gender = ?"
                    + ", mobile_number = ?, address = ?, status = ?, date_modify = ? "
                    + "WHERE patient_id = '"
                    + edit_patientID.getText() + "'";
            connect = Database.connectDB();
            try {
                if (alert.confirmationMessage("Are you sure you want to UPDATE Patient ID: " + edit_patientID.getText()
                        + "?")) {
                    prepare = connect.prepareStatement(updateData);
                    Date date = new Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    prepare.setString(1, edit_name.getText());
                    prepare.setString(2, edit_gender.getSelectionModel().getSelectedItem());
                    prepare.setString(3, edit_contactNumber.getText());
                    prepare.setString(4, edit_address.getText());
                    prepare.setString(5, edit_status.getSelectionModel().getSelectedItem());
                    prepare.setString(6, String.valueOf(sqlDate));
                    prepare.executeUpdate();
                    alert.successMessage("Updated Successfully!");
                } else {
                    alert.errorMessage("Cancelled.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    // CLOSE THE EDITPATIENTFORM FXML FILE AND OPEN IT AGAIN
    public void setField() {
        if (edit_patientID != null) {
            edit_patientID.setText(String.valueOf(Data.temp_PatientID));
            edit_name.setText(Data.temp_name);
            edit_gender.getSelectionModel().select(Data.temp_gender);
            edit_contactNumber.setText(String.valueOf(Data.temp_number));
            edit_address.setText(Data.temp_address);
            edit_status.getSelectionModel().select(Data.temp_status);
        }
    }



    public void genderList() {
        List<String> genderL = new ArrayList<>();

        for (String data : Data.gender) {
            genderL.add(data);
        }

        ObservableList listData = FXCollections.observableList(genderL);
        edit_gender.setItems(listData);
    }

    public void statusList() {
        List<String> statusL = new ArrayList<>();

        for (String data : Data.status) {
            statusL.add(data);
        }

        ObservableList listData = FXCollections.observableList(statusL);
        edit_status.setItems(listData);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setField();
        genderList();
        statusList();
    }

    public void uploadFile(ActionEvent actionEvent) {
        try {
            // Get the database connection
            Connection connection = Database.connectDB();

            if (connection != null) {
                // Replace these with your actual table and column names
                String tableName = "document";
                String patientIdColumnName = "patient_id";
                String documentColumnName = "document";
                String docNameColumnName = "doc_name";

                // Prepare the SQL statement
                String sql = "INSERT INTO " + tableName + " (" + patientIdColumnName + ", " + documentColumnName + ", " + docNameColumnName + ") VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    // Read the file into a byte array

                    File file = new File(filePathText.getText());
                    byte[] fileData = new byte[(int) file.length()];
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        fileInputStream.read(fileData);
                    }

                    // Set parameters in the SQL statement
                    preparedStatement.setString(1,edit_patientID.getText() );
                    preparedStatement.setBytes(2, fileData);
                    preparedStatement.setString(3, "abc");

                    // Execute the SQL statement to insert the file into the database
                    preparedStatement.executeUpdate();

                    alert.successMessage("Uploaded Successfully!");
                }
            }
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }


    }

    public void retrive(ActionEvent actionEvent) {

        String sql = "SELECT document FROM document WHERE doc_id = ?";
        try (Connection connection = Database.connectDB();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, "12");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Retrieve the BLOB data
                    byte[] imageData = resultSet.getBytes("document");

                    // Specify the path to save the image
                    String outputPath = "C:\\Users\\Lenovo\\Downloads\\doc.jpeg";

                    // Convert and save the image
                    saveImage(imageData, outputPath);
                    alert.successMessage("Downloaded doc Successfully at  C:\\Users\\Lenovo\\Downloads\\doc.jpg");

                } else {
                    System.out.println("Image not found for ID: " + 2);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static void saveImage(byte[] imageData, String outputPath) {
        try (OutputStream outputStream = new FileOutputStream(outputPath)) {
            // Write the BLOB data to the output stream
            outputStream.write(imageData);

            System.out.println("Image saved successfully at: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void browse(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        // Set extension filters, optionally.
        fileChooser.getExtensionFilters().addAll(
               
              
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif","*.jpeg")
               
        );

        // Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            filePathText.setText(file.getAbsolutePath());
        } else {
            filePathText.setText("File selection cancelled.");
        }
    }
}
