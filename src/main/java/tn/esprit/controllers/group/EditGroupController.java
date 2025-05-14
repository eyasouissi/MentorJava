package tn.esprit.controllers.group;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.services.group.GroupService;

import java.io.File;
import java.time.LocalDate;

public class EditGroupController {

    @FXML private Label idLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Label creationDateLabel;
    @FXML private DatePicker meetingDatePicker;
    @FXML private TextField imagePathField;
    @FXML private TextField pdfPathField;
    @FXML private ImageView imageCircle;

    private GroupStudent currentGroup;
    private String imagePath;
    private String pdfPath;

    public void setGroupData(GroupStudent group) {
        this.currentGroup = group;

        // Log debugging information
        System.out.println("Setting group data: ID=" + group.getId() + ", Name=" + group.getName());

        idLabel.setText(String.valueOf(group.getId()));
        nameField.setText(group.getName());
        descriptionField.setText(group.getDescription());
        creationDateLabel.setText(group.getCreationDate() != null ? group.getCreationDate().toString() : "N/A");
        meetingDatePicker.setValue(group.getMeetingDate());
        imagePath = group.getImage();
        imagePathField.setText(imagePath != null ? imagePath : "");
        pdfPath = group.getPdfFile();
        pdfPathField.setText(pdfPath != null ? pdfPath : "");

        // Display the image in the ImageView if an image is available
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image("file:" + imagePath);
                    imageCircle.setImage(image);
                    System.out.println("Image loaded successfully from: " + imagePath);
                } else {
                    System.out.println("Image file does not exist: " + imagePath);
                    // Set a placeholder image
                    imageCircle.setImage(null);
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                // Set a placeholder image or leave it blank
                imageCircle.setImage(null);
            }
        } else {
            System.out.println("No image path provided");
            imageCircle.setImage(null);
        }
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                imagePath = selectedFile.getAbsolutePath();
                imagePathField.setText(imagePath);
                
                // Display the image in the ImageView
                Image image = new Image("file:" + imagePath);
                imageCircle.setImage(image);
                System.out.println("New image selected: " + imagePath);
            } catch (Exception e) {
                System.err.println("Error loading selected image: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load the selected image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBrowsePdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier PDF");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        File selectedFile = fileChooser.showOpenDialog(pdfPathField.getScene().getWindow());
        if (selectedFile != null) {
            pdfPath = selectedFile.getAbsolutePath();
            pdfPathField.setText(pdfPath);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            // Get values from fields
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            LocalDate meetingDate = meetingDatePicker.getValue();

            // Validate required fields
            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Group name is required");
                nameField.requestFocus();
                return;
            }

            if (description.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Description is required");
                descriptionField.requestFocus();
                return;
            }

            // Validate meeting date if provided
            if (meetingDate != null && meetingDate.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Meeting date must be in the future");
                meetingDatePicker.requestFocus();
                return;
            }

            // Update the group object with new values
            currentGroup.setName(name);
            currentGroup.setDescription(description);
            currentGroup.setMeetingDate(meetingDate);
            currentGroup.setImage(imagePath);
            currentGroup.setPdfFile(pdfPath);

            System.out.println("Updating group: " + currentGroup.getId() + " - " + currentGroup.getName());
            System.out.println("Image path: " + imagePath);

            // Save changes to database
            GroupService.getInstance().modifier(currentGroup);
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Group updated successfully!");
            
            // Close the dialog
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update group: " + e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
