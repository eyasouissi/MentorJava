package tn.esprit.controllers.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import tn.esprit.services.UserService;

public class NewPasswordController {
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button resetBtn;

    private String userEmail;
    private final UserService userService = UserService.getInstance();

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords don't match");
            return;
        }

        if (newPassword.length() < 8) {
            showError("Password must be at least 8 characters");
            return;
        }

        try {
            boolean success = userService.updatePassword(userEmail, newPassword);
            if (success) {
                showSuccess("Password updated successfully!");
                resetBtn.setDisable(true);

                // Optionally: Add a delay and then close the window or go back to login
            } else {
                showError("Failed to update password");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.RED);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.GREEN);
    }
}