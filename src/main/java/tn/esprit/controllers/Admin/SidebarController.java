package tn.esprit.controllers.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SidebarController {
    // Dropdowns
    @FXML private VBox coursesDropdown;
    @FXML private VBox forumDropdown;
    @FXML private VBox groupesDropdown;
    @FXML private VBox eventsDropdown;
    @FXML private VBox pricingDropdown;

    private Button lastActiveButton;
    private static final Map<String, String> FXML_PATHS = new HashMap<>();

    static {
        FXML_PATHS.put("Courses", "/interfaces/Courses/CoursesView.fxml");
        FXML_PATHS.put("Category", "/interfaces/Category/CategoryView.fxml");
        FXML_PATHS.put("Forum", "/interfaces/Forum/ForumView.fxml");
        // Add all other paths...
    }

    @FXML
    public void initialize() {
        hideAllDropdowns();
    }

    private void hideAllDropdowns() {
        coursesDropdown.setVisible(false);
        forumDropdown.setVisible(false);
        groupesDropdown.setVisible(false);
        eventsDropdown.setVisible(false);
        pricingDropdown.setVisible(false);
    }

    // Toggle methods
    @FXML
    public void toggleCoursesDropdown() { toggleDropdown(coursesDropdown); }
    @FXML
    public void toggleForumDropdown() { toggleDropdown(forumDropdown); }
    @FXML
    public void toggleGroupesDropdown() { toggleDropdown(groupesDropdown); }
    @FXML
    public void toggleEventsDropdown() { toggleDropdown(eventsDropdown); }
    @FXML
    public void togglePricingDropdown() { toggleDropdown(pricingDropdown); }

    private void toggleDropdown(VBox dropdown) {
        boolean visible = dropdown.isVisible();
        hideAllDropdowns();
        dropdown.setVisible(!visible);
        dropdown.setManaged(!visible);
    }

    // Navigation methods
    @FXML
    public void navigateToCourses() { navigateTo("Courses"); }
    @FXML
    public void navigateToCategory() { navigateTo("Category"); }
    @FXML
    public void navigateToForum() { navigateTo("Forum"); }
    @FXML
    public void navigateToGroupes() { navigateTo("Groupes"); }
    @FXML
    public void navigateToProjects() { navigateTo("Projects"); }
    @FXML
    public void navigateToEvents() { navigateTo("Events"); }
    @FXML
    public void navigateToAnnouncements() { navigateTo("Announcements"); }
    @FXML
    public void navigateToPricing() { navigateTo("Pricing"); }
    @FXML
    public void navigateToSubscription() { navigateTo("Subscription"); }

    private void navigateTo(String viewName) {
        try {
            String fxmlPath = FXML_PATHS.get(viewName);
            if (fxmlPath != null) {
                Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
                Stage stage = (Stage) coursesDropdown.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/Auth/LoginView.fxml"));
            Stage stage = (Stage) coursesDropdown.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}