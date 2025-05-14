package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.io.IOException;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.User;

public class SidebarController {
    private BorderPane mainContainer;

    @FXML private VBox coursesDropdown;
    @FXML private VBox forumDropdown;
    @FXML private VBox groupesDropdown;
    @FXML private VBox eventsDropdown;
    @FXML private VBox pricingDropdown;
    private User currentUser;

    public void setMainContainer(BorderPane mainContainer) {
        this.mainContainer = mainContainer;
    }

    @FXML
    public void initialize() {
        hideAllDropdowns();
        currentUser = UserSession.getInstance().getCurrentUser();
    }

    private void hideAllDropdowns() {
        setDropdownVisibility(coursesDropdown, false);
        setDropdownVisibility(forumDropdown, false);
        setDropdownVisibility(groupesDropdown, false);
        setDropdownVisibility(eventsDropdown, false);
        setDropdownVisibility(pricingDropdown, false);
    }

    private void setDropdownVisibility(VBox dropdown, boolean visible) {
        if (dropdown != null) {
            dropdown.setVisible(visible);
            dropdown.setManaged(visible);
        }
    }

    @FXML public void toggleCoursesDropdown() { toggleDropdown(coursesDropdown); }
    @FXML public void toggleForumDropdown() { toggleDropdown(forumDropdown); }
    @FXML public void toggleGroupesDropdown() { toggleDropdown(groupesDropdown); }
    @FXML public void toggleEventsDropdown() { toggleDropdown(eventsDropdown); }
    @FXML public void togglePricingDropdown() { toggleDropdown(pricingDropdown); }

    private void toggleDropdown(VBox dropdown) {
        if (dropdown != null) {
            boolean isVisible = dropdown.isVisible();
            hideAllDropdowns();
            dropdown.setVisible(!isVisible);
            dropdown.setManaged(!isVisible);
        }
    }

    @FXML
    public void navigateToEvents() {
        loadContent("/views/ListeEvenement.fxml");
    }

    @FXML public void navigateToCourses() { showNotImplemented(); }
    @FXML public void navigateToCategory() { showNotImplemented(); }
    @FXML public void navigateToForum() { showNotImplemented(); }
    @FXML public void navigateToGroupes() { showNotImplemented(); }
    @FXML public void navigateToProjects() { showNotImplemented(); }
    @FXML public void navigateToAnnouncements() { showNotImplemented(); }
    @FXML public void navigateToPricing() { showNotImplemented(); }
    @FXML public void navigateToSubscription() { showNotImplemented(); }
    @FXML public void navigateToDashboard() { showNotImplemented(); }
    @FXML public void navigateToUsers() { showNotImplemented(); }
    @FXML public void navigateToSettings() { showNotImplemented(); }

    private void showNotImplemented() {
        System.out.println("Cette fonctionnalité n'est pas implémentée");
    }

    private void loadContent(String fxmlPath) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContainer.setCenter(content);
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        UserSession.getInstance().clearSession();
        loadContent("/interfaces/Auth/LoginView.fxml");
    }
}