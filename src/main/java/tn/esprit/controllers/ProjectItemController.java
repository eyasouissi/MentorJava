package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.controllers.project.EditProjectController;
import tn.esprit.entities.User;
import tn.esprit.entities.project.Project;
import tn.esprit.services.project.ProjectService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ProjectItemController {
    @FXML private ImageView projectImageView;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label difficultyLabel;
    @FXML private Button viewPdfButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    private Project project;
    private User currentUser;
    private ProjectService projectService;
    private GroupDetailsController parentController;
    
    @FXML
    public void initialize() {
        projectService = new ProjectService();
    }
    
    public void setProject(Project project) {
        this.project = project;
        
        // Set project data
        titleLabel.setText(project.getTitre());
        
        // Truncate description to prevent overflow
        String desc = project.getDescriptionProject();
        if (desc.length() > 150) {
            desc = desc.substring(0, 147) + "...";
        }
        descriptionLabel.setText(desc);
        
        // Format and set deadline
        if (project.getDeadline() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            deadlineLabel.setText("Due: " + project.getDeadline().format(formatter));
        } else {
            deadlineLabel.setText("No deadline");
        }
        
        // Set difficulty (1-5)
        StringBuilder difficultyText = new StringBuilder("Difficulty: ");
        for (int i = 0; i < project.getDifficulte(); i++) {
            difficultyText.append("★");
        }
        for (int i = project.getDifficulte(); i < 5; i++) {
            difficultyText.append("☆");
        }
        difficultyLabel.setText(difficultyText.toString());
        
        // Show PDF button if a PDF file is provided
        viewPdfButton.setVisible(project.getPdfFile() != null && !project.getPdfFile().isEmpty());
        
        // Load project image
        loadProjectImage();
    }
    
    private void loadProjectImage() {
        try {
            String imagePath = project.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (!file.exists()) {
                    file = new File("uploads/" + imagePath);
                }
                
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    projectImageView.setImage(image);
                    return;
                }
            }
            
            // Load default image if no image is found
            projectImageView.setImage(new Image(getClass().getResourceAsStream("/assets/images/project-default.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Show edit and delete buttons only for tutors
        boolean isTutor = user != null && user.getRoles().equals("TUTOR");
        editButton.setVisible(isTutor);
        deleteButton.setVisible(isTutor);
    }
    
    public void setParentController(GroupDetailsController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void onViewPdfClick() {
        if (project.getPdfFile() == null || project.getPdfFile().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No PDF file available for this project.");
            return;
        }
        
        try {
            File pdfFile = new File(project.getPdfFile());
            if (!pdfFile.exists()) {
                pdfFile = new File("uploads/" + project.getPdfFile());
            }
            
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not open PDF file: File not found or desktop not supported.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open PDF file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onEditClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/EditProjectView.fxml"));
            Parent root = loader.load();
            
            EditProjectController controller = loader.getController();
            controller.setProject(project);
            controller.setParentController(parentController);
            
            Stage stage = (Stage) editButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open edit project page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onDeleteClick() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this project?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    projectService.supprimer(project.getId());
                    
                    if (parentController != null) {
                        parentController.refreshProjects();
                    }
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Project deleted successfully!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete project: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 