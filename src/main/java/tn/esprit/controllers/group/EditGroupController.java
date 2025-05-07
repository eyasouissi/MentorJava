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

        idLabel.setText(String.valueOf(group.getId()));
        nameField.setText(group.getName());
        descriptionField.setText(group.getDescription());
        creationDateLabel.setText(group.getCreationDate().toString());
        meetingDatePicker.setValue(group.getMeetingDate());
        imagePath = group.getImage();
        imagePathField.setText(imagePath);
        pdfPath = group.getPdfFile();
        pdfPathField.setText(pdfPath);

        // Display the image in the Circle if an image is available
        if (imagePath != null && !imagePath.isEmpty()) {
            Image image = new Image("file:" + imagePath);
            imageCircle.setImage(image);
        }
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            imagePathField.setText(imagePath);
            
            // Display the image in the Circle
            Image image = new Image("file:" + imagePath);
            imageCircle.setImage(image);
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
        String name = nameField.getText();
        String description = descriptionField.getText();
        LocalDate meetingDate = meetingDatePicker.getValue();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les champs marqués d'un * sont obligatoires");
            return;
        }

        if (meetingDate != null && meetingDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de réunion doit être dans le futur");
            return;
        }

        currentGroup.setName(name);
        currentGroup.setDescription(description);
        currentGroup.setMeetingDate(meetingDate);
        currentGroup.setImage(imagePath);
        currentGroup.setPdfFile(pdfPath);

        try {
            GroupService.getInstance().modifier(currentGroup);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Groupe mis à jour avec succès !");
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour : " + e.getMessage());
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
