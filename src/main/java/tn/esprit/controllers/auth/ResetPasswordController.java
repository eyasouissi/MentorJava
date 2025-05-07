package tn.esprit.controllers.auth;

import jakarta.mail.MessagingException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.services.UserService;
import tn.esprit.services.EmailService;

import java.io.IOException;

public class ResetPasswordController {
    @FXML private TextField emailField;
    @FXML private TextField digit1, digit2, digit3, digit4, digit5;
    @FXML private Label errorLabel;
    @FXML private HBox verificationBox;
    @FXML private Button verifyBtn;

    private String verificationCode;
    private String userEmail; // Store email for password reset
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        verificationBox.setVisible(false);
        verifyBtn.setDisable(true);

        // Add listeners to OTP fields to auto-verify when all digits are entered
        addDigitListeners();
    }

    private void addDigitListeners() {
        TextField[] digits = {digit1, digit2, digit3, digit4, digit5};
        for (TextField digit : digits) {
            digit.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.length() == 1) {
                    moveToNextField(digit);
                }
                checkAllDigitsFilled();
            });
        }
    }

    private void moveToNextField(TextField currentField) {
        if (currentField == digit1) digit2.requestFocus();
        else if (currentField == digit2) digit3.requestFocus();
        else if (currentField == digit3) digit4.requestFocus();
        else if (currentField == digit4) digit5.requestFocus();
    }

    private void checkAllDigitsFilled() {
        boolean allFilled = !digit1.getText().isEmpty() &&
                !digit2.getText().isEmpty() &&
                !digit3.getText().isEmpty() &&
                !digit4.getText().isEmpty() &&
                !digit5.getText().isEmpty();
        verifyBtn.setDisable(!allFilled);
    }

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Invalid email format");
            return;
        }

        if (userService.getByEmail(email) == null) {
            showError("Email not registered");
            return;
        }

        userEmail = email; // Store for later use
        verificationCode = String.valueOf(10000 + (int)(Math.random() * 90000));        try {
            new EmailService().sendEmail(email, "Password Reset Code", "Your code: " + verificationCode);
            verificationBox.setVisible(true);
            showSuccess("Code sent to email!");
            emailField.setDisable(true);
        } catch (MessagingException e) {
            showError("Failed to send email. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerifyCode() {
        String enteredCode = digit1.getText() + digit2.getText() + digit3.getText()
                + digit4.getText() + digit5.getText();

        if (enteredCode.equals(verificationCode)) {
            try {
                // Load the password reset form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/auth/new-password.fxml"));
                Parent root = loader.load();

                // Pass the email to the new password controller
                NewPasswordController controller = loader.getController();
                controller.setUserEmail(userEmail);

                // Switch scene
                Stage stage = (Stage) verifyBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                showError("Error loading password reset form");
                e.printStackTrace();
            }
        } else {
            showError("Invalid verification code");
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        // Get the current stage (window) from the event source and close it
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }
}