package tn.esprit.components;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class NotificationBell extends StackPane {
    private final ImageView bellIcon;
    private final Label countLabel;
    private final Circle notificationDot;
    private int notificationCount = 0;
    private Timeline shakeTimeline;

    public NotificationBell() {
        // Load bell icon
        bellIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/bell.png")));
        bellIcon.setFitWidth(24);
        bellIcon.setFitHeight(24);

        // Create notification count label
        countLabel = new Label("0");
        countLabel.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-background-radius: 50%;");
        countLabel.setMinWidth(20);
        countLabel.setMinHeight(20);
        countLabel.setAlignment(Pos.CENTER);
        countLabel.setVisible(false);

        // Create notification dot
        notificationDot = new Circle(5, Color.RED);
        notificationDot.setVisible(false);

        // Position elements
        setAlignment(Pos.CENTER);
        getChildren().addAll(bellIcon, countLabel);
        StackPane.setAlignment(countLabel, Pos.TOP_RIGHT);

        // Initialize shake animation
        setupShakeAnimation();
    }

    private void setupShakeAnimation() {
        RotateTransition rotate1 = new RotateTransition(Duration.millis(100), bellIcon);
        rotate1.setByAngle(20);
        rotate1.setCycleCount(1);

        RotateTransition rotate2 = new RotateTransition(Duration.millis(100), bellIcon);
        rotate2.setByAngle(-40);
        rotate2.setCycleCount(1);

        RotateTransition rotate3 = new RotateTransition(Duration.millis(100), bellIcon);
        rotate3.setByAngle(20);
        rotate3.setCycleCount(1);

        shakeTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                bellIcon.setRotate(0);
                SequentialTransition sequence = new SequentialTransition(rotate1, rotate2, rotate3);
                sequence.play();
            })
        );
        shakeTimeline.setCycleCount(1);
    }

    public void addNotification() {
        notificationCount++;
        updateNotificationDisplay();
        shakeTimeline.play();
    }

    public void clearNotifications() {
        notificationCount = 0;
        updateNotificationDisplay();
    }

    private void updateNotificationDisplay() {
        if (notificationCount > 0) {
            countLabel.setText(String.valueOf(notificationCount));
            countLabel.setVisible(true);
            notificationDot.setVisible(true);
        } else {
            countLabel.setVisible(false);
            notificationDot.setVisible(false);
        }
    }

    public int getNotificationCount() {
        return notificationCount;
    }
} 