package tn.esprit.controllers.user.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.controllers.user.ProfileController;
import tn.esprit.entities.User;

import java.io.IOException;

public class AdminProfileController extends ProfileController {
    @FXML private Label adminBadgeLabel;
    @FXML private Button dashboardButton;
    @FXML private Button editButton;
    @FXML private Button logoutButton;
    @FXML private Hyperlink diplomaLink;
    @FXML private ImageView profileImageView;
    @FXML private ImageView backgroundImageView;

    private User currentUser;

    @Override
    public void setUserData(User user) {
        super.setUserData(user);
        this.currentUser = user;

        // Always show admin badge since this is the admin profile view
        adminBadgeLabel.setText("ADMIN");
        adminBadgeLabel.setTextFill(Color.RED);
        adminBadgeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Set profile image if available
        if (user.getPfp() != null && !user.getPfp().isEmpty()) {
            try {
                profileImageView.setImage(new Image(user.getPfp()));
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }

        // Set background image if available
        if (user.getBg() != null && !user.getBg().isEmpty()) {
            try {
                backgroundImageView.setImage(new Image(user.getBg()));
            } catch (Exception e) {
                System.err.println("Error loading background image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/user/admin/user_crud.fxml"));
            Parent root = loader.load();

            UserCrud controller = loader.getController();
            controller.initializeWithUser(currentUser);

            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not navigate to dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/auth/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not logout: " + e.getMessage());
        }
    }

    @FXML
    private void openDiplomaPDF() {
        if (currentUser != null && currentUser.getDiplome() != null && !currentUser.getDiplome().isEmpty()) {
            try {
                // Implement your PDF opening logic here
                // For example:
                // HostServices hostServices = getHostServices();
                // hostServices.showDocument(currentUser.getDiplome());
                showAlert("Info", "Opening diploma: " + currentUser.getDiplome());
            } catch (Exception e) {
                showAlert("Error", "Could not open diploma: " + e.getMessage());
            }
        } else {
            showAlert("Info", "No diploma available for this user");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}