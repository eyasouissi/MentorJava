package tn.esprit.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tn.esprit.entities.Notif;

import java.io.File;

public class NotificationsPopupController {
    @FXML private ListView<Notif> notificationList;
    private final Image defaultAvatar = new Image(getClass().getResourceAsStream("/assets/images/profile.png"));

    public void initData(ObservableList<Notif> notifications) {
        notificationList.setItems(notifications);
        notificationList.setCellFactory(lv -> new ListCell<Notif>() {
            @Override
            protected void updateItem(Notif notif, boolean empty) {
                super.updateItem(notif, empty);
                if (empty || notif == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);

                    // Safe image loading
                    ImageView pfp = new ImageView();
                    try {
                        String imagePath = notif.getTriggeredBy().getPfp();
                        if (imagePath != null && !imagePath.isEmpty()) {
                            File file = new File(imagePath);
                            if (!file.exists()) file = new File("uploads/" + imagePath);
                            if (!file.exists()) file = new File("uploads/pfp/" + imagePath);

                            if (file.exists()) {
                                pfp.setImage(new Image(file.toURI().toString()));
                            } else {
                                pfp.setImage(defaultAvatar);
                            }
                        } else {
                            pfp.setImage(defaultAvatar);
                        }
                    } catch (Exception e) {
                        pfp.setImage(defaultAvatar);
                    }

                    pfp.setFitWidth(40);
                    pfp.setFitHeight(40);

                    Label text = new Label(notif.getTriggeredBy().getName() + " " +
                            notif.getType() + " your post");

                    box.getChildren().addAll(pfp, text);
                    setGraphic(box);
                }
            }
        });
    }
}