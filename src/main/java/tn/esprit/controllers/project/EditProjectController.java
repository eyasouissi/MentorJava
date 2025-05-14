package tn.esprit.controllers.project;

import java.io.File;
import java.time.LocalDate;

import tn.esprit.entities.project.Project;
import tn.esprit.services.project.ProjectService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EditProjectController {

    // Champs FXML
    @FXML private Label idLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private HBox starsContainer;
    @FXML private Label ratingLabel;
    @FXML private TextField fileField;
    @FXML private DatePicker deadlinePicker;
    @FXML private Button browseFileButton;
    @FXML private Button updateButton;

    // Variables pour le système d'étoiles
    private int currentRating = 0;
    private final int MAX_STARS = 4;
    private final Text[] stars = new Text[MAX_STARS];
    
    private Project selectedProject;
    private String filePath;
    private tn.esprit.controllers.GroupDetailsController parentController;

    @FXML
    public void initialize() {
        initializeStarRatingSystem();
    }

    private void initializeStarRatingSystem() {
        starsContainer.getChildren().clear();
        
        for (int i = 0; i < MAX_STARS; i++) {
            final int rating = i + 1;
            Text star = new Text("★");
            star.setFont(Font.font(24));
            
            // Style initial
            star.setFill(Color.LIGHTGRAY);
            star.setStyle("-fx-cursor: hand;");
            
            // Gestion des événements
            star.setOnMouseClicked(e -> {
                currentRating = rating;
                updateStarsAppearance();
                updateRatingLabel();
            });
            
            star.setOnMouseEntered(e -> {
                if (currentRating == 0) {
                    previewStars(rating);
                }
            });
            
            star.setOnMouseExited(e -> {
                if (currentRating == 0) {
                    resetStars();
                } else {
                    updateStarsAppearance();
                }
            });
            
            stars[i] = star;
            starsContainer.getChildren().add(star);
        }
    }

    private void previewStars(int upTo) {
        for (int i = 0; i < MAX_STARS; i++) {
            stars[i].setFill(i < upTo ? Color.GOLD : Color.LIGHTGRAY);
        }
    }

    private void updateStarsAppearance() {
        Color starColor = getColorForRating(currentRating);
        
        for (int i = 0; i < MAX_STARS; i++) {
            stars[i].setFill(i < currentRating ? starColor : Color.LIGHTGRAY);
        }
    }

    private Color getColorForRating(int rating) {
        switch (rating) {
            case 1: return Color.GREEN;
            case 2: return Color.YELLOW;
            case 3: return Color.ORANGE;
            case 4: return Color.RED;
            default: return Color.LIGHTGRAY;
        }
    }

    private void resetStars() {
        for (Text star : stars) {
            star.setFill(Color.LIGHTGRAY);
        }
    }

    private void updateRatingLabel() {
        ratingLabel.setText(currentRating + "/" + MAX_STARS);
    }

    public void setProject(Project project) {
        this.selectedProject = project;
        if (project != null) {
            // Initialisation des champs standards
            if (idLabel != null) idLabel.setText(String.valueOf(project.getId()));
            if (titleField != null) titleField.setText(project.getTitre());
            if (descriptionArea != null) descriptionArea.setText(project.getDescriptionProject());
            if (fileField != null) fileField.setText(project.getPdfFile());
            if (deadlinePicker != null) deadlinePicker.setValue(project.getDeadline());
            
            // Initialisation du système d'étoiles
            if (project.getDifficulte() != null) {
                currentRating = project.getDifficulte();
                updateStarsAppearance();
                updateRatingLabel();
            }
        }
    }

    public void setParentController(tn.esprit.controllers.GroupDetailsController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleBrowseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(browseFileButton.getScene().getWindow());
        if (selectedFile != null) {
            filePath = selectedFile.getAbsolutePath();
            fileField.setText(filePath);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            // Validation des champs obligatoires
            String title = titleField.getText();
            String description = descriptionArea.getText();
            LocalDate deadline = deadlinePicker.getValue();

            if (title.isEmpty() || description.isEmpty() || currentRating == 0) {
                showAlert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs obligatoires et sélectionner une difficulté");
                return;
            }

            // Mise à jour du projet
            selectedProject.setTitre(title);
            selectedProject.setDescriptionProject(description);
            selectedProject.setDifficulte(currentRating);
            selectedProject.setDeadline(deadline);
            selectedProject.setPdfFile(filePath);

            // Sauvegarde
            ProjectService.getInstance().updateProject(selectedProject);
            showAlert(Alert.AlertType.INFORMATION, "Projet mis à jour avec succès!");
            
            // Refresh parent controller if available
            if (parentController != null) {
                parentController.refreshProjects();
            }
            
            // Fermeture de la fenêtre
            ((Stage) updateButton.getScene().getWindow()).close();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        ((Stage) updateButton.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}