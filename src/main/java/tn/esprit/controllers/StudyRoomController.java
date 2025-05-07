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
import javafx.stage.Stage;
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

        Label typeLabel = new Label(room.getSettings().isPublic() ? "Public" : "Private");
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

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Create Study Room");
        dialog.setHeaderText("Enter room details:");

        VBox content = new VBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Room name");
        CheckBox publicCheckbox = new CheckBox("Public room");
        publicCheckbox.setSelected(true);
        content.getChildren().addAll(new Label("Room Name:"), nameField, publicCheckbox);
        dialog.getDialogPane().setContent(content);

        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == createButton) {
                String roomName = nameField.getText().trim();
                boolean isPublic = publicCheckbox.isSelected();

                if (roomName.isEmpty()) {
                    statusLabel.setText("Room name cannot be empty!");
                    return null;
                }

                if (!UserSession.getInstance().isLoggedIn()) {
                    statusLabel.setText("You must be logged in to create a room!");
                    return null;
                }
                User currentUser = UserSession.getInstance().getCurrentUser();



                Room createdRoom = roomService.createRoom(roomName, currentUser, isPublic);

                if (createdRoom != null) {
                    allRooms.add(createdRoom);
                    displayCurrentPage();
                    statusLabel.setText("Room created successfully!");
                    return true;
                }
                statusLabel.setText("Failed to create room!");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) dialog.close();
        });
    }

}
