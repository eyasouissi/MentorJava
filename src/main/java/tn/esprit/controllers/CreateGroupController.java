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
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.User;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.services.group.GroupService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

public class CreateGroupController {
    @FXML private Button backButton;
    @FXML private ImageView groupImageView;
    @FXML private TextField groupNameField;
    @FXML private TextArea groupDescriptionField;
    @FXML private ComboBox<String> subjectComboBox;
    @FXML private TextField maxMembersField;
    
    private GroupService groupService;
    private User currentUser;
    private FrontGroupController parentController;
    private String selectedImagePath;
    
    @FXML
    public void initialize() {
        groupService = new GroupService();
        currentUser = UserSession.getInstance().getCurrentUser();
        
        // Initialize subject comboBox
        subjectComboBox.getItems().addAll(
            "Mathematics",
            "Computer Science",
            "Physics",
            "Chemistry",
            "Biology",
            "Engineering",
            "Languages",
            "History",
            "Other"
        );
        subjectComboBox.setValue("Computer Science");
    }
    
    public void setParentController(FrontGroupController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void onBackClick() {
        navigateBack();
    }
    
    @FXML
    private void onUploadImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Group Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(groupImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Generate a unique filename
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path targetPath = Paths.get("uploads", "group", fileName);
                
                // Create directories if they don't exist
                Files.createDirectories(Paths.get("uploads", "group"));
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update UI
                Image image = new Image(targetPath.toUri().toString());
                groupImageView.setImage(image);
                selectedImagePath = targetPath.toString();
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void onCreateClick() {
        if (!validateInputs()) return;
        
        try {
            // Create the group
            GroupStudent group = new GroupStudent();
            group.setName(groupNameField.getText().trim());
            group.setDescription(getFormattedDescription());
            group.setCreationDate(LocalDate.now());
            group.setImage(selectedImagePath);
            
            // Set the creator ID - this fixes the 'created_by_id' error
            group.setCreatedById(currentUser.getId());

            // Parse max members
            try {
                int maxMembers = Integer.parseInt(maxMembersField.getText().trim());
                if (maxMembers < 2) {
                    maxMembers = 2; // Ensure minimum of 2 members
                    showAlert(Alert.AlertType.WARNING, "Warning", "Minimum group size is 2. Setting to 2 members.");
                } else if (maxMembers > 100) {
                    maxMembers = 100; // Cap at 100 members
                    showAlert(Alert.AlertType.WARNING, "Warning", "Maximum group size is 100. Setting to 100 members.");
                }
                group.setMaxMembers(maxMembers); // Set max members
                group.setMemberCount(1); // Start with 1 (the creator)
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Invalid number format for maximum members. Using default value of 10.");
                group.setMaxMembers(10); // Default value
                group.setMemberCount(1); // Start with 1 (the creator)
            }
            
            // Add the group to the database
            GroupStudent createdGroup = groupService.addGroup(group);
            
            if (createdGroup != null && createdGroup.getId() != null) {
                // Add the creator as the first member (tutor)
                boolean added = groupService.joinGroup(currentUser.getId(), createdGroup.getId());
                
                if (added) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Group created successfully!");
                    navigateToGroupDetails(createdGroup);
                } else {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Group created but failed to add you as a member.");
                    navigateBack();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create group");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create group: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateInputs() {
        String name = groupNameField.getText().trim();
        String description = groupDescriptionField.getText().trim();
        String maxMembers = maxMembersField.getText().trim();
        
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Group name cannot be empty");
            return false;
        }
        
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Group description cannot be empty");
            return false;
        }
        
        if (description.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Group description must have at least 5 characters");
            return false;
        }
        
        if (maxMembers.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Maximum members cannot be empty");
            return false;
        }
        
        try {
            int max = Integer.parseInt(maxMembers);
            if (max < 2) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Maximum members must be at least 2");
                return false;
            }
            if (max > 100) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Maximum members cannot exceed 100");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Maximum members must be a number");
            return false;
        }
        
        return true;
    }
    
    private String getFormattedDescription() {
        String subject = subjectComboBox.getValue();
        String description = groupDescriptionField.getText().trim();
        
        // Prepend the subject to the description
        return String.format("Subject: %s\n\n%s", subject, description);
    }
    
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/FrontGroup.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void navigateToGroupDetails(GroupStudent group) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/GroupDetails.fxml"));
            Parent root = loader.load();
            
            GroupDetailsController controller = loader.getController();
            controller.setGroup(group);
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to group details: " + e.getMessage());
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