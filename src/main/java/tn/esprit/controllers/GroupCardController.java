package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.User;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.services.group.GroupService;

import java.io.File;
import java.io.IOException;

public class GroupCardController {
    @FXML private ImageView groupImageView;
    @FXML private Text groupNameText;
    @FXML private Text groupDescriptionText;
    @FXML private Text memberCountText;
    @FXML private Button joinButton;
    
    private GroupService groupService;
    private GroupStudent group;
    private User currentUser;
    private FrontGroupController parentController;

    @FXML
    public void initialize() {
        groupService = new GroupService();
    }
    
    public void setGroup(GroupStudent group) {
        this.group = group;
        
        // Set group data
        groupNameText.setText(group.getName() != null ? group.getName() : "No Name");
        
        // Handle description - check for null
        String desc = group.getDescription();
        if (desc == null) {
            desc = "No description available";
        } else if (desc.length() > 100) {
            desc = desc.substring(0, 97) + "...";
        }
        groupDescriptionText.setText(desc);
        
        // Set member count
        int memberCount = group.getMemberCount() != null ? group.getMemberCount() : 0;
        memberCountText.setText(memberCount + " members");
        
        // Load group image
        loadGroupImage();
    }
    
    private void loadGroupImage() {
        try {
            String imagePath = group.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (!file.exists()) {
                    file = new File("uploads/" + imagePath);
                }
                
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    groupImageView.setImage(image);
                    return;
                }
            }
            
            // Try to load default image safely
            try {
                // First try the asset path
                java.io.InputStream stream = getClass().getResourceAsStream("/assets/images/group-default.png");
                if (stream != null) {
                    groupImageView.setImage(new Image(stream));
                    return;
                }
                
                // Try alternate locations if the first one fails
                stream = getClass().getResourceAsStream("/images/group-default.png");
                if (stream != null) {
                    groupImageView.setImage(new Image(stream));
                    return;
                }
                
                stream = getClass().getResourceAsStream("/images/default-group.png");
                if (stream != null) {
                    groupImageView.setImage(new Image(stream));
                    return;
                }
                
                // If all resource paths fail, just set a null image and style the ImageView with CSS
                groupImageView.setImage(null);
                groupImageView.setStyle("-fx-background-color: #E0E0E0;"); // Light gray background
                
            } catch (Exception e) {
                System.err.println("Could not load default image: " + e.getMessage());
                groupImageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error loading group image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Show join button only for students
        boolean isStudent = user != null && user.getRoles().contains("ROLE_STUDENT");
        
        // Check if the student is already a member
        boolean isMember = false;
        if (isStudent) {
            try {
                isMember = groupService.isUserMemberOfGroup(user.getId(), group.getId());
            } catch (Exception e) {
                System.err.println("Error checking group membership: " + e.getMessage());
                // Default to showing the Join button if we can't check membership status
                isMember = false;
            }
        }
        
        joinButton.setVisible(isStudent && !isMember);
        
        // Change text to "View Group" if already a member
        if (isStudent && isMember) {
            joinButton.setText("View Group");
            joinButton.setVisible(true);
        }
    }
    
    public void setParentController(FrontGroupController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void onCardClick() {
        try {
            if (currentUser.getRoles().contains("ROLE_TUTOR") ||
                (currentUser.getRoles().contains("ROLE_STUDENT") &&
                 groupService.isUserMemberOfGroup(currentUser.getId(), group.getId()))) {
                navigateToGroupDetails();
            }
        } catch (Exception e) {
            System.err.println("Error checking group membership: " + e.getMessage());
            // If we can't check membership, default to allowing navigation for tutors only
            if (currentUser.getRoles().contains("ROLE_TUTOR")) {
                navigateToGroupDetails();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to verify your membership in this group. Please try again later.");
            }
        }
    }
    
    @FXML
    private void onJoinClick() {
        if (joinButton.getText().equals("View Group")) {
            navigateToGroupDetails();
            return;
        }
        
        try {
            boolean joined = groupService.joinGroup(currentUser.getId(), group.getId());
            
            if (joined) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "You have successfully joined the group!");
                navigateToGroupDetails();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to join the group. Group might be full.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to join the group: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToGroupDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/GroupDetails.fxml"));
            Parent groupDetails = loader.load();

            // Get controller and set data
            GroupDetailsController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setGroup(group);

            // Get reference to the existing root pane
            BorderPane rootPane = (BorderPane) groupNameText.getScene().getRoot();

            // Preserve the existing sidebar
            Node sidebar = rootPane.getLeft();

            // Create a NEW BorderPane for the scene
            BorderPane newRoot = new BorderPane();
            newRoot.setLeft(sidebar);  // Preserve existing sidebar
            newRoot.setCenter(groupDetails);  // Set new content

            // Update the existing scene
            Scene currentScene = groupNameText.getScene();
            currentScene.setRoot(newRoot);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not open group details: " + e.getMessage());
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