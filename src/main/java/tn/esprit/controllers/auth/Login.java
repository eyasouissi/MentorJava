package tn.esprit.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.controllers.user.admin.UserCrud; // Added import
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Login {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label errorLabel;
    @FXML private ImageView richardImage;
    @FXML private TextField complimentField;
    @FXML private VBox passwordBox;

    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        try {
            String initialImagePath = "/assets/icons/richard_bored.png";
            InputStream imageStream = getClass().getResourceAsStream(initialImagePath);

            if (imageStream == null) {
                throw new RuntimeException("Richard image not found at: " + initialImagePath);
            }

            Image boredImage = new Image(imageStream);
            richardImage.setImage(boredImage);
            passwordBox.setVisible(false);

            complimentField.textProperty().addListener((obs, oldVal, newVal) -> {
                evaluateCompliment(newVal);
            });

        } catch (Exception e) {
            System.err.println("Error initializing Richard: " + e.getMessage());
            errorLabel.setText("Richard is unavailable! Try restarting the app.");
            e.printStackTrace();
        }
    }

    private void evaluateCompliment(String text) {
        try {
            text = text.toLowerCase();

            if (text.contains("handsome") || text.contains("awesome") ||
                    text.contains("great") || text.contains("best") ||
                    text.contains("cute") || text.contains("adorable") ||
                    text.contains("cool") || text.contains("amazing")) {
                setRichardImage("richard_happy.png");
                passwordBox.setVisible(true);
            }
            else if (text.contains("ugly") || text.contains("stupid") ||
                    text.contains("boring") || text.contains("worst") ||
                    text.contains("dumb")) {
                setRichardImage("richard_mad.png");
                passwordBox.setVisible(false);
            }
            else {
                setRichardImage("richard_bored.png");
                passwordBox.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Error evaluating compliment: " + e.getMessage());
        }
    }

    private void setRichardImage(String imageName) {
        try {
            String imagePath = "/assets/icons/" + imageName;
            InputStream imageStream = getClass().getResourceAsStream(imagePath);

            if (imageStream == null) {
                throw new RuntimeException("Cannot find image: " + imagePath);
            }

            Image image = new Image(imageStream);
            richardImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading Richard image: " + e.getMessage());
            errorLabel.setText("Richard is moody today - try again!");
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        if (!passwordBox.isVisible()) {
            showError("Please make Richard happy first!");
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            if (email.isEmpty() || password.isEmpty()) {
                showError("Email and password are required");
                return;
            }

            System.out.println("Login attempt for: " + email);
            User user = userService.getByEmail(email);

            if (user == null) {
                System.out.println("No user found with email: " + email);
                showError("Invalid email or password");
                return;
            }

            System.out.println("Found user: " + user.getEmail());
            System.out.println("Verifying password...");

            if (!userService.verifyPassword(email, password)) {
                showError("Invalid email or password");
                return;
            }

            if (user.isRestricted()) {
                showError("Account restricted. Contact support.");
                return;
            }

            if (!user.isVerified()) {
                showAlert("Warning", "Your account is not yet verified.\nSome features may be limited.");
            }

            redirectToMainPage(user);
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToMainPage(User user) {
        try {
            String fxmlPath;
            if (user.getRoles().contains("ROLE_ADMIN")) { // Changed to check roles set
                fxmlPath = "/interfaces/user/admin/user_crud.fxml";
            } else {
                fxmlPath = "/interfaces/auth/main.fxml";
            }

            URL fxmlLocation = getClass().getResource(fxmlPath);
            System.out.println("Loading FXML from: " + fxmlLocation);

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            if (user.getRoles().contains("ROLE_ADMIN")) { // Changed to check roles set
                UserCrud controller = loader.getController();
                controller.initializeWithUser(user); // Initialize with current user
            } else {
                MainController controller = loader.getController();
                controller.initializeWithUser(user, "You have successfully logged in!");
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(user.getRoles().contains("ROLE_ADMIN") ? "Admin Dashboard" : "Main Application");
            stage.show();

        } catch (IOException e) {
            System.err.println("Redirect failed: " + e.getMessage());
            e.printStackTrace();
            showError("Cannot redirect to main page: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/reset-password.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Cannot open password reset");
        }
    }

    @FXML
    public void handleSignUpRedirect(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/RoleChoice.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Cannot open registration");
        }
    }
}