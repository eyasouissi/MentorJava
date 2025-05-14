package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.Room;
import tn.esprit.entities.User;
import tn.esprit.services.RoomService;
import tn.esprit.services.UserService;


import java.io.IOException;
import java.util.*;

public class StudyRoomController {

    @FXML private TilePane tilePane;
    @FXML private Label statusLabel;
    @FXML private Button leftButton;
    @FXML private Button rightButton;

    private User currentUser;
    private final RoomService roomService = new RoomService();

    private List<Room> allRooms = new ArrayList<>();
    private int currentPage = 0;
    private final int ROOMS_PER_PAGE = 8;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        this.currentUser = UserSession.getInstance().getCurrentUser(); // Get the logged-in user

        setupNavigationButtons();
        loadExistingRooms();
        tilePane.getStylesheets().add(getClass().getResource("/css/studyroom.css").toExternalForm());

    }

    private void setupNavigationButtons() {
        leftButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                displayCurrentPage();
            }
        });

        rightButton.setOnAction(e -> {
            if ((currentPage + 1) * ROOMS_PER_PAGE < allRooms.size()) {
                currentPage++;
                displayCurrentPage();
            }
        });
    }

    private void loadExistingRooms() {
        allRooms = roomService.getAllRooms();
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        tilePane.getChildren().clear(); // Clear the existing children

        // Always show the "Create Room" card first
        tilePane.getChildren().add(createCreateRoomCard());

        // Add the rooms for the current page
        int start = currentPage * ROOMS_PER_PAGE;
        int end = Math.min(start + ROOMS_PER_PAGE, allRooms.size());

        for (int i = start; i < end; i++) {
            tilePane.getChildren().add(createRoomCard(allRooms.get(i)));
        }
    }


    private Node createRoomCard(Room room) {
        AnchorPane card = new AnchorPane();
        card.getStyleClass().add("room-card");
        card.setPrefSize(350, 200);

        List<String> backgroundImages = Arrays.asList(
                "/images/room.jpg",
                "/images/room1.jpg",
                "/images/room2.jpg"
        );

        Random random = new Random();
        String selectedImagePath = backgroundImages.get(random.nextInt(backgroundImages.size()));

        ImageView background = new ImageView();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream(selectedImagePath));
            background.setImage(bgImage);
        } catch (Exception e) {
            Image defaultBg = new Image(getClass().getResourceAsStream("/images/room.jpg"));
            background.setImage(defaultBg);
        }

        background.setPreserveRatio(false);
        background.setFitWidth(card.getPrefWidth());
        background.setFitHeight(card.getPrefHeight());

        Rectangle clip = new Rectangle();
        clip.setWidth(card.getPrefWidth());
        clip.setHeight(card.getPrefHeight());
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        background.setClip(clip);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");

        Label title = new Label(room.getName());
        title.getStyleClass().add("room-title");
        title.setStyle("-fx-text-fill: white;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER);

        Label typeLabel = new Label(room.getSettings().is_public() ? "Public" : "Private");
        typeLabel.getStyleClass().add("room-type-indicator");
        typeLabel.setStyle("-fx-text-fill: white;");

        footer.getChildren().addAll(typeLabel);
        content.getChildren().addAll(title, footer);

        card.getChildren().addAll(background, content);
        card.setOnMouseClicked(event -> openRoomPage(room));

        return card;
    }

    private Node createCreateRoomCard() {
        AnchorPane card = new AnchorPane();
        card.getStyleClass().add("room-card");
        card.setPrefSize(350, 200);

        ImageView background = new ImageView();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/images/create_room.jpg"));
            background.setImage(bgImage);
        } catch (Exception e) {
            // fallback if the image is missing
            background.setStyle("-fx-background-color: #3498db;");
        }

        background.setPreserveRatio(false);
        background.setFitWidth(card.getPrefWidth());
        background.setFitHeight(card.getPrefHeight());

        Rectangle clip = new Rectangle();
        clip.setWidth(card.getPrefWidth());
        clip.setHeight(card.getPrefHeight());
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        background.setClip(clip);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");

        Label plusLabel = new Label("+");
        plusLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");

        Label textLabel = new Label("Create Room");
        textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        content.getChildren().addAll(plusLabel, textLabel);

        card.getChildren().addAll(background, content);

        card.setOnMouseClicked(event -> handleCreateRoom());

        return card;
    }

    private void openRoomPage(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/rooms/RoomPage.fxml"));
            Parent root = loader.load();

            RoomPageController controller = loader.getController();
            controller.initData(room, currentUser);


            Stage currentStage = (Stage) tilePane.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error: Could not open room page.");
        }
    }

    @FXML
    private void handleCreateRoom() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle("Create Study Room");

        VBox container = new VBox(15);
        container.getStyleClass().addAll("alert-dialog", "info"); // You can define 'info' in CSS
        container.setPadding(new Insets(20));

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text("➕");
        icon.setStyle("-fx-font-size: 24;");

        Label titleLabel = new Label("Create Study Room");
        titleLabel.getStyleClass().add("alert-title");
        header.getChildren().addAll(icon, titleLabel);

        // Input fields
        Label nameLabel = new Label("Room Name:");
        nameLabel.getStyleClass().add("alert-content");

        TextField nameField = new TextField();
        nameField.setPromptText("Room name");

        CheckBox publicCheckbox = new CheckBox("Public room");
        publicCheckbox.setSelected(true);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button createButton = new Button("Create");
        createButton.getStyleClass().add("alert-btn");

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("alert-btn");
        cancelButton.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().addAll(cancelButton, createButton);

        container.getChildren().addAll(header, nameLabel, nameField, publicCheckbox, buttonBox);

        Scene scene = new Scene(container, 400, 250);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/edit-dialog.css").toExternalForm());
        } catch (NullPointerException e) {
            // fallback styling
            container.setStyle("-fx-background-color: #f8f5fa;");
            createButton.setStyle("-fx-background-color: #8c84a1; -fx-text-fill: white;");
            cancelButton.setStyle("-fx-background-color: #8c84a1; -fx-text-fill: white;");
        }

        createButton.setOnAction(e -> {
            String roomName = nameField.getText().trim();
            boolean isPublic = publicCheckbox.isSelected();

            if (roomName.isEmpty()) {
                showCustomAlert("error", "Validation Error", "Room name cannot be empty!");
                return;
            }

            if (!UserSession.getInstance().isLoggedIn()) {
                showCustomAlert("error", "Authentication Error", "You must be logged in to create a room!");
                return;
            }

            User currentUser = UserSession.getInstance().getCurrentUser();
            Room createdRoom = roomService.createRoom(roomName, currentUser, isPublic);

            if (createdRoom != null) {
                allRooms.add(createdRoom);
                displayCurrentPage();
                showCustomAlert("success", "Room Created", "Room created successfully!");
                dialogStage.close();
            } else {
                showCustomAlert("error", "Creation Failed", "Failed to create room!");
            }
        });

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }



    private void showCustomAlert(String alertType, String title, String message) {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initStyle(StageStyle.UTILITY);
        alertStage.setTitle(title);

        VBox container = new VBox(15);
        container.getStyleClass().addAll("alert-dialog", alertType);
        container.setPadding(new Insets(20));

        // Header with icon
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text();
        icon.setStyle("-fx-font-size: 24;");
        switch(alertType) {
            case "success" -> icon.setText("✔️");
            case "error" -> icon.setText("❌");
            default -> icon.setText("ℹ️");
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        header.getChildren().addAll(icon, titleLabel);

        // Content
        Label content = new Label(message);
        content.getStyleClass().add("alert-content");
        content.setWrapText(true);

        // OK Button
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("alert-btn");
        okButton.setOnAction(e -> alertStage.close());

        container.getChildren().addAll(header, content, okButton);

        Scene scene = new Scene(container, 350, 200);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/edit-dialog.css").toExternalForm());
        } catch (NullPointerException e) {
            // Fallback inline styling
            container.setStyle("-fx-background-color: #f8f5fa; "
                    + "-fx-border-color: #8c84a1; "
                    + "-fx-border-radius: 15px; "
                    + "-fx-padding: 20;");
            titleLabel.setStyle("-fx-text-fill: #4a4458; -fx-font-size: 16; -fx-font-weight: bold;");
            content.setStyle("-fx-text-fill: #4a4458;");
            okButton.setStyle("-fx-background-color: #8c84a1; -fx-text-fill: white; -fx-padding: 8 20;");
        }

        alertStage.setScene(scene);
        alertStage.showAndWait();
    }

}
