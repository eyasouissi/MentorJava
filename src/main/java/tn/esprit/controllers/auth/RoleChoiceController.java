package tn.esprit.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleChoiceController {

    @FXML
    private Button studentButton;

    @FXML
    private Button tutorButton;

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

    @FXML
    public void hoverStudent() {
        studentButton.setStyle("-fx-pref-width: 180px; -fx-pref-height: 60px;" +
                "-fx-background-color: #EEA0FF; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 16px;" +
                "-fx-background-radius: 30px; -fx-border-color: #EEA0FF;" +
                "-fx-border-width: 2px; -fx-border-radius: 30px;" +
                "-fx-cursor: hand;");
    }

    @FXML
    public void resetStudent() {
        studentButton.setStyle("-fx-pref-width: 180px; -fx-pref-height: 60px;" +
                "-fx-background-color: white; -fx-text-fill: #5d236a;" +
                "-fx-font-weight: bold; -fx-font-size: 16px;" +
                "-fx-background-radius: 30px; -fx-border-color: #EEA0FF;" +
                "-fx-border-width: 2px; -fx-border-radius: 30px;" +
                "-fx-cursor: hand;");
    }

    @FXML
    public void hoverTutor() {
        tutorButton.setStyle("-fx-pref-width: 180px; -fx-pref-height: 60px;" +
                "-fx-background-color: #47AEFF; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 16px;" +
                "-fx-background-radius: 30px; -fx-border-color: #47AEFF;" +
                "-fx-border-width: 2px; -fx-border-radius: 30px;" +
                "-fx-cursor: hand;");
    }

    @FXML
    public void resetTutor() {
        tutorButton.setStyle("-fx-pref-width: 180px; -fx-pref-height: 60px;" +
                "-fx-background-color: white; -fx-text-fill: #5d236a;" +
                "-fx-font-weight: bold; -fx-font-size: 16px;" +
                "-fx-background-radius: 30px; -fx-border-color: #47AEFF;" +
                "-fx-border-width: 2px; -fx-border-radius: 30px;" +
                "-fx-cursor: hand;");
    }

    private void loadSignUpForm(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Sign Up");
            stage.show();

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
