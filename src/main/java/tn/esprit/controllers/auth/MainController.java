package tn.esprit.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.controllers.user.ProfileController;
import tn.esprit.controllers.user.admin.AdminProfileController;
import tn.esprit.entities.User;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    @FXML private Label welcomeLabel;
    @FXML private VBox coursesDropdown;
    @FXML private VBox eventsDropdown;
    @FXML private VBox pricingDropdown;
    @FXML private VBox contentArea;

    private User currentUser;
    private static final Map<String, String> FXML_PATHS = new HashMap<>();

    static {
        FXML_PATHS.put("Courses", "/interfaces/courses/courses.fxml");
        FXML_PATHS.put("Category", "/interfaces/courses/category.fxml");
        FXML_PATHS.put("Forum", "/interfaces/community/forum.fxml");
        FXML_PATHS.put("Groupes", "/interfaces/community/groupes.fxml");
        FXML_PATHS.put("Events", "/interfaces/events/events.fxml");
        FXML_PATHS.put("Announcements", "/interfaces/announcements/announcements.fxml");
        FXML_PATHS.put("Pricing", "/interfaces/pricing/pricing.fxml");
        FXML_PATHS.put("Subscription", "/interfaces/pricing/subscription.fxml");
    }

    public void initializeWithUser(User user, String message) {
        this.currentUser = user;
        if (welcomeLabel != null && user != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + "! " + message);
        }
    }

    @FXML
    private void initialize() {
        if (coursesDropdown != null && eventsDropdown != null && pricingDropdown != null) {
            hideAllDropdowns();
        }
    }

    private void hideAllDropdowns() {
        coursesDropdown.setVisible(false);
        eventsDropdown.setVisible(false);
        pricingDropdown.setVisible(false);
    }

    @FXML
    private void toggleCoursesDropdown() {
        if (coursesDropdown != null) {
            coursesDropdown.setVisible(!coursesDropdown.isVisible());
            if (coursesDropdown.isVisible()) {
                eventsDropdown.setVisible(false);
                pricingDropdown.setVisible(false);
            }
        }
    }

    @FXML
    private void toggleEventsDropdown() {
        if (eventsDropdown != null) {
            eventsDropdown.setVisible(!eventsDropdown.isVisible());
            if (eventsDropdown.isVisible()) {
                coursesDropdown.setVisible(false);
                pricingDropdown.setVisible(false);
            }
        }
    }

    @FXML
    private void togglePricingDropdown() {
        if (pricingDropdown != null) {
            pricingDropdown.setVisible(!pricingDropdown.isVisible());
            if (pricingDropdown.isVisible()) {
                coursesDropdown.setVisible(false);
                eventsDropdown.setVisible(false);
            }
        }
    }

    @FXML
    private void navigateToCourses() { loadContent("Courses"); }
    @FXML
    private void navigateToCategory() { loadContent("Category"); }
    @FXML
    private void navigateToForum() { loadContent("Forum"); }
    @FXML
    private void navigateToGroupes() { loadContent("Groupes"); }
    @FXML
    private void navigateToEvents() { loadContent("Events"); }
    @FXML
    private void navigateToAnnouncements() { loadContent("Announcements"); }
    @FXML
    private void navigateToPricing() { loadContent("Pricing"); }
    @FXML
    private void navigateToSubscription() { loadContent("Subscription"); }

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
            System.out.println("Current user roles: " + currentUser.getRoles());
            String fxmlPath;

            // Check user role to determine which profile to open
            if (currentUser != null && currentUser.getRoles() != null) {
                if (currentUser.getRoles().contains("ROLE_ADMIN")) {
                    System.out.println("Loading admin profile");
                    fxmlPath = "/interfaces/user/admin/adminprofile.fxml";
                } else {
                    System.out.println("Loading regular profile");
                    fxmlPath = "/interfaces/user/profile.fxml";
                }
            } else {
                // Fallback if user or roles are null
               fxmlPath = "/interfaces/user/profile.fxml";
           }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Set user data for both profile types
            if (fxmlPath.contains("adminprofile")) {
                AdminProfileController adminController = loader.getController();
                adminController.setUserData(currentUser);
            } else {
                ProfileController profileController = loader.getController();
                profileController.setUserData(currentUser);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(fxmlPath.contains("adminprofile") ? "Admin Profile" : "User Profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}