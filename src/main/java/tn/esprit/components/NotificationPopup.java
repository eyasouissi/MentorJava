package tn.esprit.components;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;
import tn.esprit.entities.Notification;

public class NotificationPopup extends Popup {
    private final VBox content;
    private final FadeTransition fadeIn;
    private final FadeTransition fadeOut;

    public NotificationPopup() {
        content = new VBox(10);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        content.setPadding(new Insets(10));
        content.setMaxWidth(300);
        content.setMaxHeight(400);

        getContent().add(content);

        // Setup animations
        fadeIn = new FadeTransition(Duration.millis(200), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeOut = new FadeTransition(Duration.millis(200), content);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> hide());
    }

    public void showNotification(Notification notification) {
        HBox notificationBox = createNotificationBox(notification);
        content.getChildren().add(0, notificationBox); // Add at top

        // Remove oldest if more than 5 notifications
        if (content.getChildren().size() > 5) {
            content.getChildren().remove(content.getChildren().size() - 1);
        }

        fadeIn.play();
    }

    private HBox createNotificationBox(Notification notification) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");

        // User profile picture
        ImageView profilePic = new ImageView(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
        profilePic.setFitWidth(40);
        profilePic.setFitHeight(40);
        profilePic.setStyle("-fx-background-radius: 20;");

        // Notification message
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 14;");

        box.getChildren().addAll(profilePic, messageLabel);
        return box;
    }

    public void startFadeOut() {
        fadeOut.play();
    }
} 