package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.entities.project.Project;
import tn.esprit.services.project.ProjectService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

public class AddProjectController {
    @FXML private Button backButton;
    @FXML private ImageView projectImageView;
    @FXML private TextField projectTitleField;
    @FXML private TextArea projectDescriptionField;
    @FXML private Slider difficultySlider;
    @FXML private DatePicker deadlinePicker;
    @FXML private Button uploadImageButton;
    @FXML private Button uploadPdfButton;
    @FXML private Label pdfFileLabel;
    @FXML private ListView<String> filesListView;
    
    private ProjectService projectService;
    private GroupStudent group;
    private GroupDetailsController parentController;
    private String selectedImagePath;
    private String selectedPdfPath;
    
    @FXML
    public void initialize() {
        projectService = new ProjectService();
        
        // Set default values
        difficultySlider.setMin(1);
        difficultySlider.setMax(5);
        difficultySlider.setValue(3);
        
        deadlinePicker.setValue(LocalDate.now().plusDays(14));
    }
    
    public void setGroup(GroupStudent group) {
        this.group = group;
    }
    
    public void setParentController(GroupDetailsController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void onBackClick() {
        navigateBack();
    }
    
    @FXML
    private void onUploadImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Generate a unique filename
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path targetPath = Paths.get("uploads", "project", fileName);
                
                // Create directories if they don't exist
                Files.createDirectories(Paths.get("uploads", "project"));
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update UI
                projectImageView.setImage(new Image(targetPath.toUri().toString()));
                selectedImagePath = targetPath.toString();
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void onUploadPdfClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project PDF");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File selectedFile = fileChooser.showOpenDialog(uploadPdfButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Generate a unique filename
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path targetPath = Paths.get("uploads", "project", "pdf", fileName);
                
                // Create directories if they don't exist
                Files.createDirectories(Paths.get("uploads", "project", "pdf"));
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update UI
                pdfFileLabel.setText(selectedFile.getName());
                selectedPdfPath = targetPath.toString();
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void onUploadFilesClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project Files");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.ppt", "*.pptx", "*.xls", "*.xlsx"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Generate a unique filename
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path targetPath = Paths.get("uploads", "project", "files", fileName);
                
                // Create directories if they don't exist
                Files.createDirectories(Paths.get("uploads", "project", "files"));
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update UI
                if (filesListView != null) {
                    filesListView.getItems().add(selectedFile.getName());
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "File uploaded successfully!");
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void onCreateClick() {
        if (!validateInputs()) return;
        
        try {
            Project project = new Project();
            project.setTitre(projectTitleField.getText().trim());
            project.setDescriptionProject(projectDescriptionField.getText().trim());
            project.setDifficulte((int) difficultySlider.getValue());
            project.setDeadline(deadlinePicker.getValue());
            project.setGroup(group);
            project.setImage(selectedImagePath);
            project.setPdfFile(selectedPdfPath);
            
            Project createdProject = projectService.addProject(project);
            
            if (createdProject != null && createdProject.getId() != null) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Project created successfully!");
                navigateBack();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create project");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create project: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateInputs() {
        String title = projectTitleField.getText().trim();
        String description = projectDescriptionField.getText().trim();
        
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Project title cannot be empty");
            return false;
        }
        
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Project description cannot be empty");
            return false;
        }
        
        if (description.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Project description must have at least 5 characters");
            return false;
        }
        
        if (deadlinePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a deadline");
            return false;
        }
        
        if (deadlinePicker.getValue().isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Deadline cannot be in the past");
            return false;
        }
        
        return true;
    }
    
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/GroupDetails.fxml"));
            Parent root = loader.load();
            
            GroupDetailsController controller = loader.getController();
            controller.setGroup(group);
            controller.setCurrentUser(null); // Will be set from session
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
            if (parentController != null) {
                parentController.refreshProjects();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not go back to group details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 