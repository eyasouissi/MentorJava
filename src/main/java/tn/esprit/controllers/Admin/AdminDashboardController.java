package tn.esprit.controllers.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.User;
import java.io.IOException;

public class AdminDashboardController {
    @FXML private VBox sidebarContainer;
    @FXML private StackPane contentArea;

    private User currentUser;

    @FXML
    public void initialize() {
        // Verify user session first
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }
        initializeWithUser(currentUser);
    }

    private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/login.fxml"));
            Stage stage = (Stage) sidebarContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializeWithUser(User user) {
        this.currentUser = user;
        System.out.println("Initializing with user: " + (user != null ? user.getName() : "null")); // Debug
        loadSidebar();
    }

    private void loadSidebar() {
        System.out.println("Attempting to load sidebar..."); // Debug

        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Admin/Sidebar.fxml"));
            System.out.println("FXML loader created, resource: " + loader.getLocation()); // Debug

            Parent sidebar = loader.load();
            System.out.println("Sidebar FXML loaded successfully"); // Debug

            // Get the controller and set up dependencies
            SidebarController sidebarController = loader.getController();
            System.out.println("Sidebar controller obtained"); // Debug

            sidebarController.setContentArea(contentArea);
            System.out.println("Content area set on sidebar controller"); // Debug

            if (currentUser != null) {
                sidebarController.setCurrentUser(currentUser);
                System.out.println("Current user set on sidebar controller"); // Debug
            }

            // Clear and add the sidebar
            sidebarContainer.getChildren().clear();
            sidebarContainer.getChildren().add(sidebar);
            System.out.println("Sidebar added to container"); // Debug

        } catch (IOException e) {
            System.err.println("CRITICAL ERROR LOADING SIDEBAR FXML:");
            e.printStackTrace();
            showErrorAlert("Failed to load sidebar", "Could not load the sidebar navigation. Please check the resource path.\nError: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("UNEXPECTED ERROR LOADING SIDEBAR:");
            e.printStackTrace();
            showErrorAlert("Unexpected error", "An unexpected error occurred while loading the sidebar.\nError: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}