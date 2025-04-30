package tn.esprit.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleChoiceController {
    @FXML
    public void handleStudentChoice(ActionEvent event) {
        loadSignUpForm("/interfaces/auth/signupstudent.fxml", event);
    }

    @FXML
    public void handleTutorChoice(ActionEvent event) {
        loadSignUpForm("/interfaces/auth/signuptutor.fxml", event);
    }

    @FXML
    public void handleLoginRedirect(ActionEvent event) {
        redirectTo("/interfaces/auth/login.fxml", event);
    }

    private void loadSignUpForm(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Sign Up");
            stage.show();

            // Close the current role selection window
            ((Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void redirectTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}