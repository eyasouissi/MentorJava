package tn.esprit.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import tn.esprit.entities.Room;
import tn.esprit.entities.User;
import tn.esprit.services.ChatGPTService;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.PolicyNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RoomPageController {
    @FXML private ImageView backgroundImage;
    @FXML private Label roomNameLabel;
    @FXML private HBox bottomNavBar;
    @FXML private Button youtubeButton;
    @FXML private ImageView youtubeIcon;
    @FXML private WebView youtubePlayer;
    @FXML private TextField messageInput;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private HBox messageContainer;
    @FXML private Button studyBuddyButton; // Define the Study Buddy button
    @FXML private VBox chatBox;
    private User currentUser;
    private boolean chatVisible = false;
    @FXML private Button emojiButton;
    @FXML private VBox emojiPicker;

    private boolean emojiPickerVisible = false;
    // Add these new fields
    @FXML private Button whiteboardButton;
    @FXML private VBox whiteboardContainer;
    @FXML private Canvas whiteboardCanvas;
    @FXML private ColorPicker colorPicker;

    private GraphicsContext gc;
    private boolean isDrawing = false;
    private double lastX, lastY;
    private double mouseX, mouseY;

    private DraggableResizableController draggableResizableController;
    @FXML private VBox youtubeContainer;
    @FXML private WebView youtubeWebView;
    @FXML private TextField youtubeUrlField;
    private DraggableResizableController youtubeDragController;
    @FXML private VBox aiChatContainer;
    @FXML private VBox aiChatArea;
    @FXML private TextField aiInput;
    private double aiChatXOffset = 0;
    private double aiChatYOffset = 0;
    @FXML private Button timerButton;
    @FXML private VBox timerContainer;
    @FXML private TextField timerInput;
    @FXML private Label timerDisplay;
    private Timeline timer;
    private int secondsRemaining;
    private DraggableResizableController timerDragController;
    @FXML private TextField hoursInput;
    @FXML private TextField minutesInput;
    @FXML private TextField secondsInput;
    @FXML private Button pauseResumeButton;
    private boolean isTimerRunning = false;
    @FXML
    private Label encouragementLabel;
    @FXML
    private Button stopButton;
    @FXML private VBox todoContainer;
    @FXML private VBox todoList;
    @FXML private TextField todoInput;
    private DraggableResizableController todoDragController;
    @FXML private Button notesButton;
    @FXML private VBox notesContainer;
    private DraggableResizableController notesDragController;
    private int photoCount = 0;
    @FXML private Pane workspacePane;




    @FXML
    private void initialize() {
        // Trigger send on Enter key
       // messageInput.setOnAction(event -> handleSendMessage());
        // Ensure emojiPicker is initialized
        if (emojiPicker == null) {
            emojiPicker = new VBox();  // Example initialization
        }
        // Initialize whiteboard
        gc = whiteboardCanvas.getGraphicsContext2D();
        gc.setLineWidth(2);
        colorPicker.setValue(Color.BLACK);
        setupWhiteboard();


        // Initialize YouTube drag controller
        youtubeDragController = new DraggableResizableController();
        youtubeDragController.initialize(youtubeContainer);

        //timer
        timerDragController = new DraggableResizableController();
        timerDragController.initialize(timerContainer);

        //to do list
        todoDragController = new DraggableResizableController();
        todoDragController.initialize(todoContainer);

        //NOTES
        notesDragController = new DraggableResizableController();
        notesDragController.initialize(notesContainer);
        notesContainer.getChildren().add(new NotesWidget());

    }

    public void initData(Room room, User passedUser) {
        if (passedUser == null) {
            this.currentUser = new User();
            currentUser.setId(1L);
            currentUser.setName("TestUser");
        } else {
            this.currentUser = passedUser;
        }

        roomNameLabel.setText(room.getName());

        try {
            Image image = new Image(getClass().getResourceAsStream("/images/" + room.getBackgroundImage()));
            backgroundImage.setImage(image);
        } catch (Exception e) {
            backgroundImage.setImage(new Image(getClass().getResourceAsStream("/images/room2.jpg")));
        }

        backgroundImage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                backgroundImage.fitWidthProperty().bind(newScene.widthProperty());
                backgroundImage.fitHeightProperty().bind(newScene.heightProperty());
            }

            // Add mouse click event listener to the scene to detect clicks outside the chat box
            newScene.setOnMouseClicked(event -> {
                // If the chat container is visible and click is outside the chatScrollPane, hide it
                if (chatVisible && !chatScrollPane.getBoundsInParent().contains(event.getSceneX(), event.getSceneY())) {
                    hideChatContainer();
                }
            });
        });
    }



    @FXML
    private void handleLeaveRoom() {
        System.out.println("Leave Room button clicked");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/rooms/StudyRoom.fxml"));
            Stage stage = (Stage) backgroundImage.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty()) {
            addMessageToChat(currentUser.getName(), messageText);
            messageInput.clear();
        }
    }

    private void addMessageToChat(String sender, String text) {
        VBox messageBubble = new VBox();
        messageBubble.getStyleClass().add("message-bubble");

        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("message-sender");

        Label messageLabel = new Label(text);
        messageLabel.getStyleClass().add("message-text");

        messageLabel.setWrapText(true);

        messageBubble.getChildren().addAll(senderLabel, messageLabel);
        chatMessages.getChildren().add(messageBubble);

        // Scroll to the bottom of the chat to show the latest message
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);  // Scrolls to the bottom
        });
    }

    @FXML
    private void showChatContainer() {
        if (!chatVisible) {
            // Fade-in effect for the chat container
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), chatScrollPane);
            fadeTransition.setFromValue(0.0); // Initially invisible
            fadeTransition.setToValue(1.0);   // Fully visible
            fadeTransition.setCycleCount(1);   // Execute only once
            fadeTransition.play();

            chatScrollPane.setVisible(true);
            chatVisible = true;

            // After 30 seconds, fade out and hide the chat container if no new message
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(30), event -> hideChatContainer())
            );
            timeline.setCycleCount(1);
            timeline.play();
        }
    }

    private void hideChatContainer() {
        if (chatVisible) {
            // Fade-out effect for the chat container
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), chatScrollPane);
            fadeTransition.setFromValue(1.0); // Fully visible
            fadeTransition.setToValue(0.0);   // Fully invisible
            fadeTransition.setCycleCount(1);   // Execute only once
            fadeTransition.play();

            fadeTransition.setOnFinished(event -> chatScrollPane.setVisible(false));
            chatVisible = false;
        }
    }

    @FXML
    private void handleSettingsButton(ActionEvent event) {
        System.out.println("Settings button clicked");
        // Create context menu (dropdown)
        ContextMenu contextMenu = new ContextMenu();

        // Change Background Option
        MenuItem changeBgItem = new MenuItem("Change Background Image");
        changeBgItem.setOnAction(e -> handleChangeBackground());

        contextMenu.getItems().addAll(changeBgItem);

        // Show menu at button position
        Node source = (Node) event.getSource();
        contextMenu.show(source, Side.BOTTOM, 0, 0);
    }

    private void handleChangeBackground() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Background Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(backgroundImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image newImage = new Image(selectedFile.toURI().toString(),
                        backgroundImage.getFitWidth(),
                        backgroundImage.getFitHeight(),
                        true,
                        true);
                backgroundImage.setImage(newImage);

                // Optional: Save to database if needed
                // roomService.updateBackground(currentRoom.getId(), selectedFile.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Invalid Image", "Could not load the selected image.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    // Modified message adder
    private void addMessageToChat(String sender, String text, boolean isAI) {
        VBox messageBubble = new VBox();
        messageBubble.getStyleClass().addAll("message-bubble");

        // Sender label
        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("message-sender");

        // Message label
        Label messageLabel = new Label(text);
        messageLabel.getStyleClass().add("message-text");

        messageLabel.setWrapText(true);

        messageBubble.getChildren().addAll(senderLabel, messageLabel);
        chatMessages.getChildren().add(messageBubble);

        // Scroll to the bottom
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }
    // Method to handle showing emoji picker
    @FXML
    private void showEmojiPicker() {
        if (!emojiPickerVisible) {
            // Use local emojis instead of fetching from an API
            List<String> emojiList = getLocalEmojis();
            displayEmojis(emojiList);
            emojiPicker.setVisible(true);
            emojiPickerVisible = true;
        } else {
            emojiPicker.setVisible(false);
            emojiPickerVisible = false;
        }
    }

    // Local emoji list (You can add or remove emojis as needed)
    private List<String> getLocalEmojis() {
        List<String> emojis = new ArrayList<>();
        emojis.add("😀");  // Grinning face
        emojis.add("😃");  // Grinning face with big eyes
        emojis.add("😄");  // Grinning face with smiling eyes
        emojis.add("😁");  // Beaming face with smiling eyes
        emojis.add("😆");  // Grinning squinting face
        emojis.add("😅");  // Grinning face with sweat
        emojis.add("😂");  // Face with tears of joy
        emojis.add("🤣");  // Rolling on the floor laughing
        emojis.add("😊");  // Smiling face with smiling eyes
        emojis.add("😇");  // Smiling face with halo
        emojis.add("🙂");  // Slightly smiling face
        emojis.add("🙃");  // Upside-down face
        emojis.add("😉");  // Winking face
        emojis.add("😜");  // Face with stuck-out tongue and winking eye
        emojis.add("😝");  // Face with stuck-out tongue and tightly-closed eyes
        emojis.add("😛");  // Face with stuck-out tongue
        emojis.add("🤑");  // Money-mouth face
        emojis.add("🤗");  // Hugging face
        emojis.add("🤔");  // Thinking face
        emojis.add("😎");  // Smiling face with sunglasses

        // Add more emojis as needed
        return emojis;
    }

    // Method to display emojis in the UI
    private void displayEmojis(List<String> emojis) {
        emojiPicker.getChildren().clear();

        // Set initial position for the drop-up effect (starting below the input field)
        emojiPicker.setTranslateY(10);
        emojiPicker.setOpacity(0);

        // Display emojis as buttons
        for (String emoji : emojis) {
            Button emojiButton = new Button(emoji);

            // Update the style to display emojis with their colors, small size, and aligned in a row
            emojiButton.setStyle("-fx-font-size: 24px; "  // Smaller font size
                    + "-fx-font-family: \"Segoe UI Emoji\", \"Apple Color Emoji\", \"Android Emoji\"; "
                    + "-fx-background-color: transparent; "  // Transparent background
                    + "-fx-text-fill: transparent; "  // Ensure the text color doesn't interfere with the emoji colors
                    + "-fx-padding: 5px; "  // Add some padding around the emoji
                    + "-fx-min-width: 32px; "  // Set a fixed width for consistency
                    + "-fx-min-height: 32px; "  // Set a fixed height for consistency
                    + "-fx-alignment: center;");  // Align the emoji in the center of the button

            // Set a fixed width for each emoji button, so they align in a row
            emojiButton.setPrefWidth(40);  // Make sure all emojis have equal width

            // Add action on button click to insert the emoji into the text field
            emojiButton.setOnAction(event -> {
                // Add the emoji to the text field
                this.addEmojiToTextField(emoji);
            });

            // Add each emoji button to the emoji picker
            emojiPicker.getChildren().add(emojiButton);
        }

        // Apply a horizontal layout to the emoji picker (row alignment)
        emojiPicker.setStyle("-fx-spacing: 10px; "  // Adjust spacing between buttons
                + "-fx-alignment: center; "  // Center align the emojis
                + "-fx-padding: 10px;");  // Padding around the picker

        // Apply the smooth drop-up animation
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), emojiPicker);
        translateTransition.setToY(0); // Move to its original position (above the input field)
        translateTransition.setOnFinished(event -> {
            // Fade in the emoji picker once animation completes
            emojiPicker.setOpacity(1);
        });

        translateTransition.play();  // Start the transition
    }


    // Add the selected emoji to the message input field
    public void addEmojiToTextField(String emoji) {
        messageInput.appendText(emoji);  // Add the emoji to the text field
        emojiPicker.setVisible(false);   // Hide the emoji picker after selecting
        emojiPickerVisible = false;
    }

    public void addEmojiToTextField(ActionEvent actionEvent) {
    }

    private void setupWhiteboard() {
        // Set initial color
        gc.setStroke(colorPicker.getValue());

        // Handle color changes
        colorPicker.setOnAction(e -> {
            gc.setStroke(colorPicker.getValue());
            gc.setFill(colorPicker.getValue());
        });
    }

    @FXML
    private void handleWhiteboardButton() {
        whiteboardContainer.setVisible(true);
        whiteboardCanvas.setWidth(600);
        whiteboardCanvas.setHeight(400);
    }

    @FXML
    private void handleCloseWhiteboard() {
        whiteboardContainer.setVisible(false);
    }

    @FXML
    private void handleErase() {
        gc.clearRect(0, 0, whiteboardCanvas.getWidth(), whiteboardCanvas.getHeight());
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        isDrawing = true;
        lastX = event.getX();
        lastY = event.getY();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        if (isDrawing) {
            double currentX = event.getX();
            double currentY = event.getY();

            gc.strokeLine(lastX, lastY, currentX, currentY);

            lastX = currentX;
            lastY = currentY;
        }
    }

    @FXML
    private void handleMouseReleased(MouseEvent event) {
        isDrawing = false;
    }

    //YOUTUBE PLAYER
    @FXML
    private void handleYoutubeButton() {
        youtubeContainer.setVisible(true);
        youtubeContainer.toFront();
    }

    @FXML
    private void handlePlayVideo() {
        String url = youtubeUrlField.getText().trim();
        if (!url.isEmpty()) {
            String videoId = extractYouTubeId(url);
            if (videoId != null) {
                String embedUrl = "https://www.youtube.com/embed/" + videoId;
                youtubeWebView.getEngine().load(embedUrl);
            } else {
                showAlert("Invalid URL", "Please enter a valid YouTube URL");
            }
        }
    }

    @FXML
    private void handleCloseYoutube() {
        youtubeWebView.getEngine().load(null);
        youtubeContainer.setVisible(false);
    }

    private String extractYouTubeId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @FXML
    private void handleAIChatButton() {
        aiChatContainer.setVisible(!aiChatContainer.isVisible());
        if (aiChatContainer.isVisible()) {
            aiChatContainer.toFront();
        }
    }

    @FXML
    private void handleAISendMessage() {
        String message = aiInput.getText().trim();
        if (!message.isEmpty()) {
            addAIMessage("You: " + message, "user-message");
            aiInput.clear();

            try {
                String response = ChatGPTService.getStudyBuddyResponse(message);
                addAIMessage("Study Buddy: " + response, "ai-message");
            } catch (Exception e) {
                addAIMessage("Study Buddy: I'm having trouble connecting right now. Please try again later.", "ai-message");
                e.printStackTrace();
            }
        }
    }

    private void addAIMessage(String text, String styleClass) {
        Platform.runLater(() -> {
            Label message = new Label(text);
            message.getStyleClass().addAll("message-text", styleClass);
            message.setMaxWidth(250);
            message.setWrapText(true);

            HBox container = new HBox(message);
            container.setAlignment(styleClass.equals("user-message") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            aiChatArea.getChildren().add(container);
        });
    }

    @FXML
    private void handleAIChatDragStart(MouseEvent event) {
        aiChatXOffset = event.getSceneX();
        aiChatYOffset = event.getSceneY();
    }

    @FXML
    private void handleAIChatDrag(MouseEvent event) {
        double deltaX = event.getSceneX() - aiChatXOffset;
        double deltaY = event.getSceneY() - aiChatYOffset;

        aiChatContainer.setTranslateX(aiChatContainer.getTranslateX() + deltaX);
        aiChatContainer.setTranslateY(aiChatContainer.getTranslateY() + deltaY);

        aiChatXOffset = event.getSceneX();
        aiChatYOffset = event.getSceneY();
    }

    @FXML
    private void handleCloseAIChat() {
        aiChatContainer.setVisible(false);
    }

    @FXML
    private void handleTimerButton() {
        timerContainer.setVisible(!timerContainer.isVisible());
        if (timerContainer.isVisible()) {
            timerContainer.toFront();
        }
    }

    @FXML
    private void handleStartTimer() {
        try {
            int hours = parseIntOrDefault(hoursInput.getText(), 0);
            int minutes = parseIntOrDefault(minutesInput.getText(), 0);
            int seconds = parseIntOrDefault(secondsInput.getText(), 0);

            secondsRemaining = hours * 3600 + minutes * 60 + seconds;

            if (secondsRemaining <= 0) {
                showAlert("Invalid Time", "Please enter a valid time");
                encouragementLabel.setText(""); // Clear the message
                return;
            }

            if (timer != null) {
                timer.stop();
            }

            timer = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> updateTimer())
            );
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();

            // ✨ Set encouragement message
            setEncouragementMessage(hours, minutes);

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter numbers only");
            encouragementLabel.setText(""); // Clear the message
        }
    }
    private void setEncouragementMessage(int hours, int minutes) {
        List<String> messages = new ArrayList<>();

        if (hours >= 1) {
            messages.addAll(Arrays.asList(
                    "One hour? You're a machine! 🤖",
                    "Champion mode unlocked! 🏅",
                    "Study dragon awakened! 🐉📚",
                    "Big dreams need big focus! 🌟",
                    "Brain gains in progress! 🧠💪",
                    "Marathon mindset activated! 🏃‍♂️",
                    "You vs. your goals... and you're winning! 🥇",
                    "Serious study vibes detected! 📚🎶",
                    "Keep grinding, future genius! 🧠🚀",
                    "You're building an empire of knowledge! 🏰"
            ));
        } else if (minutes >= 30) {
            messages.addAll(Arrays.asList(
                    "That's a solid block of progress! 🧱",
                    "Halfway to legend status! 🌟",
                    "Smashing it, one topic at a time! 💥",
                    "30 minutes of unstoppable energy! ⚡",
                    "Stacking those wins, let's go! 🥳",
                    "Fueling the brain cells! 🧠🔥",
                    "Momentum = Magic! ✨",
                    "Look at you, so focused and unstoppable! 🎯",
                    "That's a powerful sprint! 🏃‍♂️💨",
                    "Leveling up your brain like a pro gamer! 🎮🧠"
            ));
        } else if (minutes >= 10) {
            messages.addAll(Arrays.asList(
                    "Small session, BIG impact! 🚀",
                    "Planting seeds of greatness! 🌱",
                    "Just enough to light the fire! 🔥",
                    "Short bursts = smart moves! ⚡📚",
                    "Brainstorm brewing... ☁️🌩️",
                    "You're crafting a masterpiece one minute at a time! 🎨",
                    "Strong start! 🌟",
                    "Quick focus boost incoming! 🚀",
                    "Practice makes perfect, and you're practicing! 🧠✨",
                    "Small efforts, huge rewards! 🎁"
            ));
        } else {
            messages.addAll(Arrays.asList(
                    "Quick brain flex! 💪🧠",
                    "Speedrunning knowledge! 🏎️💨",
                    "Small but mighty session! 💥",
                    "Testing the waters... and it's looking good! 🌊📚",
                    "Mini study blast! 🚀✨",
                    "5 minutes today, world domination tomorrow! 🌍",
                    "Focused flicker! 🔥",
                    "Just enough to spark genius! 💡",
                    "A sip of knowledge! ☕📖",
                    "A light warm-up — clever move! 🎯"
            ));
        }

        if (!messages.isEmpty()) {
            Random random = new Random();
            String randomMessage = messages.get(random.nextInt(messages.size()));
            encouragementLabel.setText(randomMessage);
        }
    }




    private int parseIntOrDefault(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Update the updateTimer method
    private void updateTimer() {
        if (secondsRemaining > 0) {
            secondsRemaining--;
            updateTimerDisplay(secondsRemaining);
        } else {
            handleStopTimer();
            timerDisplay.setText("00:00:00");
            playTimerSound(); // ✅ Play sound instead of alert
        }
    }
    private void playTimerSound() {
        try {
            String soundPath = getClass().getResource("/sounds/Ripple.mp3").toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);

            mediaPlayer.setCycleCount(3); // 🔁 Loop 3 times

            mediaPlayer.play();
        } catch (Exception e) {
            System.out.println("Error playing timer sound: " + e.getMessage());
        }
    }




    // New helper method
    private void updateTimerDisplay(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        timerDisplay.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    // Update handleResetTimer to fully clear
    @FXML
    private void handleResetTimer() {
        handleStopTimer();
        hoursInput.clear();
        minutesInput.clear();
        secondsInput.clear();
        secondsRemaining = 0;
        timerDisplay.setText("00:00:00");
    }



    private boolean isPaused = false; // New field to track pause state

    @FXML
    private void handleStopTimer() {
        if (timer == null) {
            return;
        }

        if (!isPaused) {
            // Pause the timer
            timer.stop();
            isPaused = true;
            stopButton.setText("Resume"); // Change label to Resume

            List<String> breakMessages = Arrays.asList(
                    "You're right, time for a break! ☕",
                    "Pause and recharge, champ! 🔋",
                    "Break time! You earned it! 🎉",
                    "Time to stretch those legs! 🧘‍♂️",
                    "Good call! Rest is productive too. 🌸",
                    "Brain nap initiated... 💤🧠",
                    "A short break makes a big difference! 🚀",
                    "Relax, refresh, refocus! 🌊",
                    "You're mastering the art of balance! ⚖️",
                    "Strategic pause = smarter moves! 🎯",
                    "Grab a snack and celebrate! 🍎🎉",
                    "You've paused like a pro! 🛑😎",
                    "Energy break = Brain boost! ⚡🧠",
                    "Rest isn't weakness, it's strategy! 🧠💪",
                    "Coffee and calm incoming! ☕🌟"
            );

            Random random = new Random();
            encouragementLabel.setText(breakMessages.get(random.nextInt(breakMessages.size())));

        } else {
            // Resume the timer
            timer.play();
            isPaused = false;
            stopButton.setText("Pause"); // Change label back to Pause

            List<String> resumeMessages = Arrays.asList(
                    "Back at it! 🚀",
                    "Let's crush the rest! 💪",
                    "Refreshed and ready! ✨",
                    "Time to shine again! 🌟",
                    "Focus mode: ON! 🎯",
                    "Charging back into action! ⚡",
                    "You’re unstoppable! 🔥",
                    "Brains reloaded, go go go! 🧠💨",
                    "No one hustles like you! 🏃‍♂️💨",
                    "Welcome back, warrior! 🛡️📚"
            );

            Random random = new Random();
            encouragementLabel.setText(resumeMessages.get(random.nextInt(resumeMessages.size())));
        }
    }



    @FXML
    private void handleCloseTimer() {
        handleStopTimer();
        timerContainer.setVisible(false);
    }



    @FXML
    private void handleTimerDragStart(MouseEvent event) {
        timerDragController.handleMousePressed(event);
    }

    @FXML
    private void handleTimerDrag(MouseEvent event) {
        timerDragController.handleMouseDragged(event);
    }

    //TO DO LIST
    @FXML
    private void handleTodoButton() {
        todoContainer.setVisible(!todoContainer.isVisible());
        if (todoContainer.isVisible()) {
            todoContainer.toFront();
        }
    }

    @FXML
    private void handleAddTodo() {
        String text = todoInput.getText().trim();
        if (!text.isEmpty()) {
            addTodoItem(text);
            todoInput.clear();
        }
    }

    private void addTodoItem(String text) {
        HBox itemBox = new HBox();
        itemBox.getStyleClass().add("todo-item");

        CheckBox checkBox = new CheckBox();
        checkBox.getStyleClass().add("todo-checkbox");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("todo-text");

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("todo-remove-btn");

        // Handle checkbox changes
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            textLabel.setStyle(newVal ? "-fx-strikethrough: true; -fx-text-fill: #95a5a6;" : "");
        });

        // Handle remove button
        removeBtn.setOnAction(e -> todoList.getChildren().remove(itemBox));

        itemBox.getChildren().addAll(checkBox, textLabel, removeBtn);
        todoList.getChildren().add(itemBox);
    }

    @FXML
    private void handleCloseTodo() {
        todoContainer.setVisible(false);
    }

    @FXML
    private void handleTodoDragStart(MouseEvent event) {
        todoDragController.handleMousePressed(event);
    }

    @FXML
    private void handleTodoDrag(MouseEvent event) {
        todoDragController.handleMouseDragged(event);
    }

    //notes
    @FXML
    private void handleNotesButton() {
        notesContainer.setVisible(!notesContainer.isVisible());
        if (notesContainer.isVisible()) {
            notesContainer.toFront();
        }
    }

    @FXML
    private void handleNotesDragStart(MouseEvent event) {
        notesDragController.handleMousePressed(event);
    }

    @FXML
    private void handleNotesDrag(MouseEvent event) {
        notesDragController.handleMouseDragged(event);
    }

    @FXML
    private void handleCloseNotes() {
        notesContainer.setVisible(false);
    }
//add photos

    @FXML
    private void handlePhotoButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(backgroundImage.getScene().getWindow());
        if (file != null) {
            addImageToWorkspace(file.toURI().toString());
        }
    }

    private void addImageToWorkspace(String imageUrl) {
        try {
            Image image = new Image(imageUrl);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);

            // Set initial size with constraints
            double initWidth = Math.min(image.getWidth(), 300);
            double initHeight = Math.min(image.getHeight(), 300);

            StackPane imageContainer = new StackPane(imageView);
            imageContainer.getStyleClass().add("image-container");
            // Add double-click handler
            imageContainer.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    animateAndRemove(imageContainer);
                }
            });

            // Size constraints
            imageContainer.setPrefSize(initWidth, initHeight);
            imageContainer.setMinSize(50, 50);
            imageContainer.setMaxSize(800, 800);

            // Bind image view to container size
            imageView.fitWidthProperty().bind(imageContainer.widthProperty());
            imageView.fitHeightProperty().bind(imageContainer.heightProperty());

            // Position with slight offset
            imageContainer.setLayoutX(100 + (photoCount * 20));
            imageContainer.setLayoutY(100 + (photoCount * 20));
            photoCount++;

            DraggableResizableController dragController = new DraggableResizableController();
            dragController.initialize(imageContainer);

            workspacePane.getChildren().add(imageContainer);
        } catch (Exception e) {
            showAlert("Error", "Could not load image");
        }
    }

    private void animateAndRemove(StackPane container) {
        // Create pop animation
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), container);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(1.2);
        scaleOut.setToY(1.2);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), container);
        scaleIn.setFromX(1.2);
        scaleIn.setFromY(1.2);
        scaleIn.setToX(0.0);
        scaleIn.setToY(0.0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Chain animations
        SequentialTransition sequence = new SequentialTransition(
                scaleOut,
                new ParallelTransition(scaleIn, fadeOut)
        );

        sequence.setOnFinished(e -> {
            workspacePane.getChildren().remove(container);
            container.setOnMouseClicked(null); // Remove event handler
        });

        sequence.play();
    }
}
