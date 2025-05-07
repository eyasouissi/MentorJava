package tn.esprit.controllers.Courses;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SubscriptionRequiredController {
    @FXML private Label messageLabel;

    public void setCourseTitle(String title) {
        messageLabel.setText("Premium Course: " + title);
    }

    @FXML
    private void goBackToCourses() {
        // Fermer la fenêtre actuelle
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}