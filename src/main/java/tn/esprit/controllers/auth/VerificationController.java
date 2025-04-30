package tn.esprit.controllers.auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.services.UserService;

import java.io.IOException;

public class VerificationController {
    @FXML private Label verificationStatusLabel;
    private Stage stage;

    public static void showVerificationWindow(String token) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    VerificationController.class.getResource("/interfaces/auth/verification.fxml")
            );
            Parent root = loader.load();

            VerificationController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.verifyAccount(token);

            stage.setTitle("Account Verification");
            stage.setScene(new Scene(root, 400, 200));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }

    public void verifyAccount(String token) {
        boolean success = UserService.getInstance().verifyUser(token);

        if (success) {
            verificationStatusLabel.setText("✅ Verification successful!\nYou can now login with full access.");
            verificationStatusLabel.setStyle("-fx-text-fill: green;");
        } else {
            verificationStatusLabel.setText("❌ Verification failed!\nToken may be invalid or expired.");
            verificationStatusLabel.setStyle("-fx-text-fill: red;");
        }

        // Auto-close after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(this::handleClose);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}