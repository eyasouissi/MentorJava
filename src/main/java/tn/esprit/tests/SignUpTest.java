package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import tn.esprit.services.VerificationServer;
import java.io.IOException;

public class SignUpTest extends Application {

    public static void main(String[] args) {
        try {
            VerificationServer.start();
            launch(args);
        } catch (IOException e) {
            System.err.println("Failed to start verification server:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/interfaces/auth/RoleChoice.fxml")
            );

            primaryStage.setTitle("Select Your Role");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();

        } catch (IOException e) {
            showError("FXML Loading Error",
                    "Failed to load RoleChoice.fxml\n" +
                            "Path: src/main/resources/interfaces/auth/RoleChoice.fxml\n" +
                            "Error: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        VerificationServer.stop();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}