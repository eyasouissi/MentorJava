package tn.esprit.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import tn.esprit.controllers.Front.SidebarController;
import tn.esprit.controllers.user.ProfileController;
import tn.esprit.controllers.user.admin.AdminProfileController;
import tn.esprit.entities.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    @FXML private Label welcomeLabel;
    @FXML private VBox coursesDropdown;
    @FXML private VBox eventsDropdown;
    @FXML private VBox pricingDropdown;
    @FXML private VBox sidebarContainer;
    @FXML private StackPane contentArea;

    private User currentUser;

    private static final Map<String, String> FXML_PATHS = new HashMap<>();

    static {
        FXML_PATHS.put("Home", "/interfaces/Front/home.fxml");
        FXML_PATHS.put("Courses", "/interfaces/courses/courses.fxml");
        FXML_PATHS.put("Category", "/interfaces/courses/category.fxml");
        FXML_PATHS.put("Forum", "/interfaces/community/forum.fxml");
        FXML_PATHS.put("Groupes", "/interfaces/community/groupes.fxml");
        FXML_PATHS.put("Events", "/interfaces/events/events.fxml");
        FXML_PATHS.put("Announcements", "/interfaces/announcements/announcements.fxml");
        FXML_PATHS.put("Pricing", "/interfaces/pricing/pricing.fxml");
        FXML_PATHS.put("Subscription", "/interfaces/pricing/subscription.fxml");
    }
    // Add this method to MainController.java
    public void updateUserInfo(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + "!");
        }

        // Force update the sidebar profile image
        if (sidebarContainer != null && !sidebarContainer.getChildren().isEmpty()) {
            Node sidebar = sidebarContainer.getChildren().get(0);
            if (sidebar.getUserData() instanceof SidebarController) {
                SidebarController sidebarController = (SidebarController) sidebar.getUserData();
                sidebarController.setCurrentUser(user);
                sidebarController.updateProfileImage(); // Now this works because method is public
            }
        }
    }

    // Add this method to MainController.java
    public void notifyProfilePictureUpdated(String newPfpPath) {
        if (sidebarContainer != null && !sidebarContainer.getChildren().isEmpty()) {
            Node sidebar = sidebarContainer.getChildren().get(0);
            if (sidebar.getUserData() instanceof SidebarController) {
                SidebarController sidebarController = (SidebarController) sidebar.getUserData();
                // Update the current user's pfp path
                if (currentUser != null) {
                    currentUser.setPfp(newPfpPath);
                }
                sidebarController.updateProfileImage();
            }
        }
    }
    public void initializeWithUser(User user, String message) {
        this.currentUser = user;

        if (welcomeLabel != null && user != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + "! " + message);
        }

        hideAllDropdowns();

        try {
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/interfaces/Front/Sidebar.fxml"));
            Parent sidebar = sidebarLoader.load();
            SidebarController sidebarController = sidebarLoader.getController();
            sidebarController.setCurrentUser(user);
            sidebarController.setContentArea(contentArea);

            if (sidebarContainer != null) {
                sidebarContainer.getChildren().clear();
                sidebarContainer.getChildren().add(sidebar);

                // Defer the binding until the VBox is attached to the Scene
                sidebarContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        sidebarContainer.prefWidthProperty().bind(
                                newScene.widthProperty().multiply(0.2).add(40)
                        );
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hideAllDropdowns() {
        if (coursesDropdown != null) coursesDropdown.setVisible(false);
        if (eventsDropdown != null) eventsDropdown.setVisible(false);
        if (pricingDropdown != null) pricingDropdown.setVisible(false);
    }

    @FXML private void toggleCoursesDropdown() {
        coursesDropdown.setVisible(!coursesDropdown.isVisible());
        if (coursesDropdown.isVisible()) {
            eventsDropdown.setVisible(false);
            pricingDropdown.setVisible(false);
        }
    }

    @FXML private void toggleEventsDropdown() {
        eventsDropdown.setVisible(!eventsDropdown.isVisible());
        if (eventsDropdown.isVisible()) {
            coursesDropdown.setVisible(false);
            pricingDropdown.setVisible(false);
        }
    }

    @FXML private void togglePricingDropdown() {
        pricingDropdown.setVisible(!pricingDropdown.isVisible());
        if (pricingDropdown.isVisible()) {
            coursesDropdown.setVisible(false);
            eventsDropdown.setVisible(false);
        }
    }

    @FXML private void navigateToCourses() { loadContent("Courses"); }
    @FXML private void navigateToCategory() { loadContent("Category"); }
    @FXML private void navigateToForum() { loadContent("Forum"); }
    @FXML private void navigateToGroupes() { loadContent("Groupes"); }
    @FXML private void navigateToEvents() { loadContent("Events"); }
    @FXML private void navigateToAnnouncements() { loadContent("Announcements"); }
    @FXML private void navigateToPricing() { loadContent("Pricing"); }
    @FXML private void navigateToSubscription() { loadContent("Subscription"); }

    private void loadContent(String viewName) {
        try {
            String fxmlPath = FXML_PATHS.get(viewName);
            if (fxmlPath != null && contentArea != null) {
                Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToProfile(ActionEvent event) {
        try {
            String fxmlPath;
            if (currentUser != null && currentUser.getRoles() != null &&
                    currentUser.getRoles().contains("ROLE_ADMIN")) {
                fxmlPath = "/interfaces/user/admin/adminprofile.fxml";
            } else {
                fxmlPath = "/interfaces/user/profile.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (fxmlPath.contains("adminprofile")) {
                AdminProfileController adminController = loader.getController();
                adminController.setUserData(currentUser);
            } else {
                ProfileController profileController = loader.getController();
                profileController.setUserData(currentUser);
                profileController.setMainController(this); // Add this line
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User Profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
