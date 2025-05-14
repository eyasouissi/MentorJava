package tn.esprit.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.controllers.Courses.AIController;
import tn.esprit.entities.Room;
import tn.esprit.entities.User;
import tn.esprit.services.ChatGPTService;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.PolicyNode;
import java.util.*;

import tn.esprit.entities.RoomState;
import javafx.collections.ListChangeListener;
import tn.esprit.services.RoomService;
import tn.esprit.controllers.TodoListWidget;
import tn.esprit.controllers.TodoListWidget.TodoItem;

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
    private TodoListWidget todoListWidget;
    @FXML private TextField todoInput;
    private DraggableResizableController todoDragController;
    @FXML private Button notesButton;
    @FXML private VBox notesContainer;
    private DraggableResizableController notesDragController;
    private int photoCount = 0;
    @FXML private Pane workspacePane;
    private Room currentRoom;
    private RoomState roomState;
    private RoomService roomService = new RoomService();
    private AIController aiController = new AIController();

    private MediaPlayer timerPlayer; // keep a reference
    private int playCount = 0;
    private final int MAX_PLAYS = 3;
    private MediaPlayer backgroundPlayer;
    private boolean isMusicPlaying = false;
    private List<Double> currentStrokePoints;
    private String currentStrokeColor;

    private boolean isRestoringState = false;

    private MediaPlayer clickSoundPlayer;
    private MediaPlayer closeWidgetSoundPlayer;

    @FXML
    private void initialize() {
        // Add explicit ID assignments and ensure workspace pane exists
        if (workspacePane == null) {
            System.err.println("Error: workspacePane is null!");
            return;
        }

        setupBackgroundMusic();

        // Initialize all containers first
        initializeContainer(whiteboardContainer, "whiteboardContainer");
        initializeContainer(timerContainer, "timerContainer");
        initializeContainer(todoContainer, "todoContainer");
        initializeContainer(notesContainer, "notesContainer");
        initializeContainer(youtubeContainer, "youtubeContainer");

        // Initialize whiteboard
        if (whiteboardCanvas != null) {
            gc = whiteboardCanvas.getGraphicsContext2D();
            gc.setLineWidth(2);
            colorPicker.setValue(Color.BLACK);
            setupWhiteboard();
        }

        // Initialize drag controllers and add to workspace
        initializeDraggableWidget(youtubeContainer, youtubeDragController = new DraggableResizableController());
        initializeDraggableWidget(timerContainer, timerDragController = new DraggableResizableController());
        initializeDraggableWidget(todoContainer, todoDragController = new DraggableResizableController());
        initializeDraggableWidget(notesContainer, notesDragController = new DraggableResizableController());

        // Add notes widget if not already present
        if (notesContainer != null && notesContainer.getChildren().isEmpty()) {
            notesContainer.getChildren().clear(); // Clear existing if any
            
            // Create a button-only widget that adds sticky notes
            Button addNoteBtn = new Button("Add Note");
            addNoteBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
            addNoteBtn.setPrefWidth(120);
            addNoteBtn.setOnAction(e -> handleNotesButton());
            
            notesContainer.getChildren().add(addNoteBtn);
            notesContainer.setAlignment(Pos.CENTER);
            notesContainer.setPadding(new Insets(10));
            
            // Ensure proper styling
            notesContainer.getStyleClass().add("notes-widget-container");
            notesContainer.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-border-color: #dddddd; -fx-border-radius: 5; -fx-background-radius: 5;");
            notesContainer.setPrefSize(150, 80);
        }

        // Initialize emoji picker
        if (emojiPicker == null) {
            emojiPicker = new VBox();
        }

        // Create the TodoListWidget
        if (todoContainer != null) {
            todoListWidget = new TodoListWidget();
            todoListWidget.setOnStateChanged(state -> updateState());
            todoListWidget.setOnCloseSound(() -> playCloseWidgetSound());
            todoContainer.getChildren().clear();
            todoContainer.getChildren().add(todoListWidget);
            todoContainer.setAlignment(Pos.CENTER);
            
            // Set ID for better debugging
            todoListWidget.setId("todoListWidget");
        }

        // Load CSS when scene is available
        backgroundImage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    try {
                        String cssPath = getClass().getResource("/css/todo-list.css").toExternalForm();
                        if (cssPath != null && !newScene.getStylesheets().contains(cssPath)) {
                            newScene.getStylesheets().add(cssPath);
                        }
                    } catch (Exception e) {
                        System.err.println("Could not load todo-list.css: " + e.getMessage());
                    }
                });
            }
        });

        // Debug print
        System.out.println("=== Initialization Complete ===");
        workspacePane.getChildren().forEach(node ->
                System.out.println("Initialized widget: " + node.getId() + " | Visible: " + node.isVisible())
        );

        setupSounds();
    }

    private void initializeContainer(VBox container, String id) {
        if (container != null) {
            container.setId(id);
            container.setManaged(false); // Set as unmanaged

            container.visibleProperty().addListener((obs, oldVal, newVal) -> {
                if (!isRestoringState) {
                    updateState();
                }
            });
        }
    }

    private void initializeDraggableWidget(VBox container, DraggableResizableController controller) {
        if (container != null && controller != null) {
            controller.initialize(container);
            if (!workspacePane.getChildren().contains(container)) {
                workspacePane.getChildren().add(container);
                System.out.println("Added " + container.getId() + " to workspace");
            }
            
            // Add movement tracking
            container.setOnMouseReleased(event -> {
                System.out.println("Widget moved: " + container.getId() + 
                    " to X:" + container.getLayoutX() + 
                    " Y:" + container.getLayoutY());
                updateState();
            });
        }
    }

    private void animateAndRemoveWidget(VBox container) {
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
            container.setVisible(false);
            updateState();
        });

        sequence.play();
    }

    public void initData(Room room, User user) {
        this.currentRoom = room;
        System.out.println("Initializing room: " + room.getId());

        // Debug print the initial state
        if (room.getState() != null) {
            System.out.println("Initial room state: " + room.getStateJson());
        } else {
            System.out.println("No initial state found");
        }

        if (user == null) {
            this.currentUser = new User();
            currentUser.setId(1L);
            currentUser.setName("TestUser");
        } else {
            this.currentUser = user;
        }

        // Load saved state
        this.roomState = room.getState();
        if (this.roomState == null) {
            System.out.println("Creating new room state");
            this.roomState = new RoomState();
            currentRoom.setState(this.roomState);
        } else {
            System.out.println("Loaded existing room state with " +
                    roomState.getWidgetPositions().size() + " widget positions");

            // Debug print all saved widget states
            roomState.getWidgetPositions().forEach((widgetId, pos) -> {
                System.out.println("Loaded state for " + widgetId +
                        " | Visible: " + pos.isVisible() +
                        " | Position: (" + pos.getX() + ", " + pos.getY() + ")");
            });
        }

        roomNameLabel.setText(room.getName());

        try {
            Image image = new Image(getClass().getResourceAsStream("/images/" + room.getBackgroundImage()));
            backgroundImage.setImage(image);
        } catch (Exception e) {
            backgroundImage.setImage(new Image(getClass().getResourceAsStream("/images/room2.jpg")));
        }

        // Wait for scene to be ready before restoring state
        backgroundImage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                backgroundImage.fitWidthProperty().bind(newScene.widthProperty());
                backgroundImage.fitHeightProperty().bind(newScene.heightProperty());

                // Ensure all widgets are initialized before restoring state
                Platform.runLater(() -> {
                    System.out.println("Scene is ready, initializing widgets...");
                    ensureWidgetsInWorkspace();

                    // Add a small delay to ensure all widgets are properly laid out
                    PauseTransition delay = new PauseTransition(Duration.millis(100));
                    delay.setOnFinished(e -> {
                        System.out.println("Restoring room state...");
                        applySavedState();
                    });
                    delay.play();
                });
            }
        });

        // Set up state tracking after initialization
        setupStateTracking();
    }

    private void setupStateTracking() {
        // Track all existing nodes
        workspacePane.getChildren().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                for (Node node : c.getAddedSubList()) {
                    trackNodeMovement(node);
                }
            }
        });

        // Track initial nodes
        workspacePane.getChildren().forEach(this::trackNodeMovement);
    }

    private void trackNodeMovement(Node node) {
        if (node == null || node.getId() == null) {
            return;
        }

        // Track position changes
        node.layoutXProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Position changed for " + node.getId() + ": X = " + newVal);
            updateState();
        });

        node.layoutYProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Position changed for " + node.getId() + ": Y = " + newVal);
            updateState();
        });

        // Track visibility changes
        node.visibleProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Visibility changed for " + node.getId() + ": " + newVal);
            updateState();
        });

        // Track parent changes (in case widget is removed/added)
        node.parentProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Parent changed for " + node.getId());
            if (newVal == null && oldVal != null) {
                // Only attempt to re-add the node if it's not already in the workspace
                if (!workspacePane.getChildren().contains(node)) {
                    System.out.println("Re-adding widget to workspace: " + node.getId());
                    try {
                        workspacePane.getChildren().add(node);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Cannot add " + node.getId() + " to workspace: " + e.getMessage());
                    }
                } else {
                    System.out.println("Widget " + node.getId() + " is already in workspace, skipping add");
                }
            }
            updateState();
        });

        // Initial state capture
        updateState();
    }

    private void updateState() {
        if (roomState == null || currentRoom == null) {
            System.err.println("Cannot update state: roomState or currentRoom is null");
            return;
        }

        System.out.println("=== Saving Room State ===");

        // Save widget positions and visibility
        workspacePane.getChildren().forEach(node -> {
            if (node.getId() == null) {
                System.out.println("Node without ID: " + node);
                return;
            }

            // Get current widget state
            RoomState.WidgetPosition currentPos = roomState.getWidgetPositions().get(node.getId());

            // Create new position state
            RoomState.WidgetPosition pos = new RoomState.WidgetPosition();
            // Update position setting to include translation
            pos.setX(node.getLayoutX() + node.getTranslateX());
            pos.setY(node.getLayoutY() + node.getTranslateY());
            pos.setVisible(node.isVisible());
            
            // If it's a sticky note, store additional info
            if (node instanceof StickyNote) {
                pos.setType("StickyNote");
            }

            // Only update state if there's an actual change
            if (currentPos == null ||
                    currentPos.getX() != pos.getX() ||
                    currentPos.getY() != pos.getY() ||
                    currentPos.isVisible() != pos.isVisible()) {

                System.out.println("Saving updated state for " + node.getId() +
                        "\n  Position: (" + pos.getX() + ", " + pos.getY() + ")" +
                        "\n  Visibility: " + pos.isVisible() +
                        (currentPos != null ?
                                "\n  Previous visibility: " + currentPos.isVisible() :
                                "\n  No previous state"));

            roomState.getWidgetPositions().put(node.getId(), pos);

                // Save state immediately when visibility changes
                if (currentPos == null || currentPos.isVisible() != pos.isVisible()) {
                    try {
                        System.out.println("Saving state immediately due to visibility change");
        currentRoom.setState(roomState);
                        roomService.saveImmediately(currentRoom);
                    } catch (Exception e) {
                        System.err.println("Error saving immediate state update: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
        
        // Check for removed widgets (especially important for sticky notes)
        List<String> widgetsToRemove = new ArrayList<>();
        roomState.getWidgetPositions().forEach((widgetId, pos) -> {
            boolean found = false;
            for (Node node : workspacePane.getChildren()) {
                if (widgetId.equals(node.getId())) {
                    found = true;
                    break;
                }
            }
            
            if (!found && (widgetId.startsWith("stickyNote_") || 
                (pos.getType() != null && pos.getType().equals("StickyNote")))) {
                widgetsToRemove.add(widgetId);
            }
        });
        
        // Remove widgets that no longer exist
        widgetsToRemove.forEach(widgetId -> {
            System.out.println("Removing state for deleted widget: " + widgetId);
            roomState.getWidgetPositions().remove(widgetId);
        });

        // Save todo items
        roomState.getTodoItems().clear();
        if (todoListWidget != null) {
            List<TodoListWidget.TodoItem> items = todoListWidget.getTodoItems();
            for (TodoListWidget.TodoItem item : items) {
                RoomState.TodoItem stateItem = new RoomState.TodoItem();
                stateItem.setText(item.getText());
                stateItem.setCompleted(item.isCompleted());
                roomState.getTodoItems().add(stateItem);
            }
        }

        // Update current room state and save
        try {
            currentRoom.setState(roomState);
            roomService.saveImmediately(currentRoom);
            System.out.println("Room state saved successfully");
            System.out.println("Saved state JSON: " + currentRoom.getStateJson());
        } catch (Exception e) {
            System.err.println("Error saving room state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applySavedState() {
        if (roomState == null) {
            System.out.println("No room state to restore");
            return;
        }

        try {
            isRestoringState = true;
            System.out.println("=== Starting State Restoration ===");
            System.out.println("Room state widget positions: " + roomState.getWidgetPositions().size());

            // Ensure all widgets are in workspace first
            ensureWidgetsInWorkspace();
            
            // Handle sticky notes first (create them if they don't exist)
            List<String> processedStickyNotes = new ArrayList<>();
            roomState.getWidgetPositions().forEach((widgetId, pos) -> {
                if (widgetId.startsWith("stickyNote_") || 
                     (pos.getType() != null && pos.getType().equals("StickyNote"))) {
                    boolean found = false;
                    for (Node node : workspacePane.getChildren()) {
                        if (widgetId.equals(node.getId())) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found && pos.isVisible()) {
                        // Create a new sticky note
                        StickyNote note = new StickyNote(new NotesAIController());
                        note.setId(widgetId);
                        note.setLayoutX(pos.getX());
                        note.setLayoutY(pos.getY());
                        note.setVisible(true);
                        // Set close sound callback
                        note.setOnCloseSound(() -> playCloseWidgetSound());
                        workspacePane.getChildren().add(note);
                        System.out.println("Restored sticky note from state: " + widgetId);
                    }
                    
                    processedStickyNotes.add(widgetId);
                }
            });

            // Create a map of all widgets for easier access
            Map<String, Node> widgetMap = new HashMap<>();
            workspacePane.getChildren().forEach(node -> {
                if (node.getId() != null) {
                    widgetMap.put(node.getId(), node);
                }
            });

            // Single pass restoration with proper ordering
            roomState.getWidgetPositions().forEach((widgetId, pos) -> {
                // Skip sticky notes as we've already processed them
                if (processedStickyNotes.contains(widgetId)) {
                    return;
                }
                
                Node widget = widgetMap.get(widgetId);
                if (widget != null) {
                    // First update position
                    widget.setLayoutX(pos.getX());
                    widget.setLayoutY(pos.getY());
                    widget.setTranslateX(0);
                    widget.setTranslateY(0);

                    // Ensure widget is properly set up
                    widget.setManaged(true);
                    widget.setMouseTransparent(false);

                    // Force visibility state immediately
                    widget.setVisible(pos.isVisible());

                    // If visible, ensure proper z-order
                    if (pos.isVisible()) {
                        widget.toFront();
                    }

                    System.out.println("Restored widget: " + widgetId +
                            " | Position: (" + pos.getX() + ", " + pos.getY() + ")" +
                            " | Visibility: " + pos.isVisible());
                } else {
                    System.err.println("Widget not found: " + widgetId);
                }
            });

            // Force a layout pass
            workspacePane.layout();
            
            // Restore Todo items
            restoreTodoItems();

            // Verify final state after a short delay
            PauseTransition verificationDelay = new PauseTransition(Duration.millis(100));
            verificationDelay.setOnFinished(e -> {
                System.out.println("=== Verifying Final State ===");
                widgetMap.forEach((id, widget) -> {
                    RoomState.WidgetPosition savedPos = roomState.getWidgetPositions().get(id);
                    if (savedPos != null) {
                        boolean visibilityMatch = savedPos.isVisible() == widget.isVisible();
                        System.out.println("Widget " + id + " final state:" +
                                "\n  Expected visibility: " + savedPos.isVisible() +
                                "\n  Actual visibility: " + widget.isVisible() +
                                "\n  Visibility matches: " + visibilityMatch);

                        if (!visibilityMatch) {
                            System.out.println("  Forcing visibility correction for " + id);
                            widget.setVisible(savedPos.isVisible());
                        }
                    }
                });
            });
            verificationDelay.play();
        } finally {
            isRestoringState = false;
        }
    }

    private void ensureWidgetsInWorkspace() {
        System.out.println("Ensuring widgets are in workspace...");

        // List of all widget containers that should be in the workspace
        Node[] widgets = {
                whiteboardContainer,
                timerContainer,
                todoContainer,
                notesContainer,
                youtubeContainer,
                aiChatContainer
        };

        for (Node widget : widgets) {
            if (widget != null) {
                if (!workspacePane.getChildren().contains(widget)) {
                    System.out.println("Adding missing widget to workspace: " + widget.getId());
                    workspacePane.getChildren().add(widget);
                }
                // Ensure the widget is properly set up
                widget.setManaged(true);
                widget.setMouseTransparent(false);
                widget.setCache(true); // Enable caching for better performance
            } else {
                System.err.println("Warning: widget is null!");
            }
        }

        System.out.println("Workspace now contains " + workspacePane.getChildren().size() + " widgets");
    }

    private void restoreTodoItems() {
        System.out.println("Restoring " + roomState.getTodoItems().size() + " todo items");
        
        if (todoListWidget != null) {
            todoListWidget.clearTasks();
            
            if (roomState.getTodoItems().isEmpty()) {
                // Add a default welcome task if list is empty
                todoListWidget.addTaskItem("Welcome! Add your tasks here 📝", false);
            } else {
                roomState.getTodoItems().forEach(item -> {
                    todoListWidget.addTaskItem(item.getText(), item.isCompleted());
                });
            }
        }
    }

    @FXML
    private void handleLeaveRoom() {
        System.out.println("=== Saving Final Room State ===");

        // Force a final state update before leaving
        updateState();

        // Save state immediately
        try {
            roomService.saveImmediately(currentRoom);
            System.out.println("Room state saved successfully before leaving");
        } catch (Exception e) {
            System.err.println("Error saving room state before leaving: " + e.getMessage());
        }

        // Clean up resources
        cleanupResources();

        try {
            // 1. Get reference to the main container
            BorderPane rootPane = (BorderPane) backgroundImage.getScene().getRoot();
            Node sidebar = rootPane.getLeft();

            // Detach sidebar from old root to avoid parenting issues
            rootPane.setLeft(null);

            // 2. Load StudyRoom content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/rooms/StudyRoom.fxml"));
            Parent studyRoomContent = loader.load();

            // 3. Create new container with preserved sidebar
            BorderPane newRoot = new BorderPane();
            newRoot.setLeft(sidebar); // Reattach sidebar
            newRoot.setCenter(studyRoomContent);

            // 4. Update the scene root
            Scene currentScene = backgroundImage.getScene();
            currentScene.setRoot(newRoot);

            // 5. Update the sidebar controller's reference
            if (sidebar instanceof VBox) {
                Object controller = ((VBox) sidebar).getProperties().get("controller");
                if (controller instanceof SidebarController) {
                    ((SidebarController) controller).setMainContainer(newRoot);
                }
            }

        } catch (IOException e) {
            System.err.println("Error loading study room scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to clean up resources when leaving the room
    private void cleanupResources() {
        // Clear sticky notes from workspace
        List<Node> nodesToRemove = new ArrayList<>();
        for (Node node : workspacePane.getChildren()) {
            if (node instanceof StickyNote) {
                nodesToRemove.add(node);
            }
        }
        
        // Remove in a separate loop to avoid ConcurrentModificationException
        for (Node node : nodesToRemove) {
            try {
                workspacePane.getChildren().remove(node);
            } catch (Exception e) {
                System.err.println("Error removing sticky note: " + e.getMessage());
            }
        }
        
        // Stop any running timers
        if (timer != null) {
            timer.stop();
        }
        
        // Stop any playing sounds
        if (timerPlayer != null) {
            timerPlayer.stop();
        }
        
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
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
        playClickSound();
        boolean newVisibility = !whiteboardContainer.isVisible();
        whiteboardContainer.setVisible(newVisibility);
        if (newVisibility) {
            whiteboardContainer.toFront();
        }
        updateState();
    }

    @FXML
    private void handleCloseWhiteboard() {
        playCloseWidgetSound();
        whiteboardContainer.setVisible(false);
        updateState();
    }

    @FXML
    private void handleErase() {
        gc.clearRect(0, 0, whiteboardCanvas.getWidth(), whiteboardCanvas.getHeight());
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        currentStrokePoints = new ArrayList<>();
        currentStrokeColor = colorPicker.getValue().toString();
        currentStrokePoints.add(event.getX());
        currentStrokePoints.add(event.getY());

        isDrawing = true;
        lastX = event.getX();
        lastY = event.getY();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        currentStrokePoints.add(event.getX());
        currentStrokePoints.add(event.getY());
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
        RoomState.WhiteboardStroke stroke = new RoomState.WhiteboardStroke();
        stroke.setColor(currentStrokeColor);
        stroke.getPoints().addAll(currentStrokePoints);
        roomState.getWhiteboardData().add(stroke);
        updateState();
        isDrawing = false;
    }

    //YOUTUBE PLAYER
    @FXML
    private void handleYoutubeButton() {
        playClickSound();
        boolean newVisibility = !youtubeContainer.isVisible();
        youtubeContainer.setVisible(newVisibility);
        if (newVisibility) {
            youtubeContainer.toFront();
        }
        updateState();
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
        playCloseWidgetSound();
        youtubeWebView.getEngine().load(null);
        youtubeContainer.setVisible(false);
        updateState();
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
        playClickSound();
        boolean newVisibility = !aiChatContainer.isVisible();
        aiChatContainer.setVisible(newVisibility);
        if (newVisibility) {
            aiChatContainer.toFront();
        }
        updateState();
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
        playCloseWidgetSound();
        aiChatContainer.setVisible(false);
        updateState();
    }

    @FXML
    private void handleTimerButton() {
        playClickSound();
        boolean newVisibility = !timerContainer.isVisible();
        timerContainer.setVisible(newVisibility);
        if (newVisibility) {
            timerContainer.toFront();
        }
        updateState();
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
            URL resource = getClass().getResource("/sounds/Ripple.wav");
            if (resource == null) {
                throw new IllegalArgumentException("Audio file not found!");
            }

            String soundPath = resource.toExternalForm();
            Media sound = new Media(soundPath);
            timerPlayer = new MediaPlayer(sound);

            // Reset play count
            playCount = 0;

            timerPlayer.setOnEndOfMedia(() -> {
                playCount++;
                if (playCount < MAX_PLAYS) {
                    // Pause for 500 milliseconds before replaying
                    PauseTransition pause = new PauseTransition(Duration.millis(500));
                    pause.setOnFinished(e -> {
                        timerPlayer.seek(Duration.ZERO);
                        timerPlayer.play();
                    });
                    pause.play();
                }
            });

            timerPlayer.play();

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
                    "You're unstoppable! 🔥",
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
        playCloseWidgetSound();
        handleStopTimer();
        timerContainer.setVisible(false);
        updateState();
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
        playClickSound();
        boolean newVisibility = !todoContainer.isVisible();
        todoContainer.setVisible(newVisibility);
        if (newVisibility) {
            todoContainer.toFront();
            
            // Make sure the TodoListWidget is visible too
            if (todoListWidget != null) {
                todoListWidget.restore();
            }
        }
        updateState();
    }

    @FXML
    private void handleAddTodo() {
        if (todoListWidget != null && todoInput != null) {
            String text = todoInput.getText().trim();
            if (!text.isEmpty()) {
                todoListWidget.addTaskItem(text, false);
                todoInput.clear();
                updateState();
            }
        }
    }

    @FXML
    private void handleCloseTodo() {
        playCloseWidgetSound();
        try {
            if (todoListWidget != null) {
                todoListWidget.animateAndClose();
            } else {
                // Fallback if widget is null
                todoContainer.setVisible(false);
                updateState();
            }
        } catch (Exception e) {
            System.err.println("Error closing Todo list: " + e.getMessage());
            // Fallback in case of errors
            todoContainer.setVisible(false);
            updateState();
        }
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
        playClickSound();
        try {
            // Create a new sticky note and add it directly to workspace
            StickyNote note = new StickyNote(new NotesAIController());
            
            // Position with slight randomization
            Random random = new Random();
            double posX = 100 + random.nextInt(200);
            double posY = 100 + random.nextInt(150);
            note.setLayoutX(posX);
            note.setLayoutY(posY);
            
            // Give unique ID for state management
            note.setId("stickyNote_" + System.currentTimeMillis());
            
            // Set close sound callback
            note.setOnCloseSound(() -> playCloseWidgetSound());
            
            // Add to workspace and bring to front
            workspacePane.getChildren().add(note);
            note.toFront();
            
            // Save state
            updateState();
        } catch (Exception e) {
            System.err.println("Error creating sticky note: " + e.getMessage());
            e.printStackTrace();
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
        updateState();
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

    public void forceStateRestoration() {
        Platform.runLater(() -> {
            applySavedState();
            workspacePane.requestLayout();

            // Debug: Print all nodes in workspace
            System.out.println("=== Workspace Children ===");
            workspacePane.getChildren().forEach(node ->
                    System.out.println(node.getId() + " | Visible: " + node.isVisible())
            );
        });
    }

    private void setupBackgroundMusic() {
        try {
            URL resource = getClass().getResource("/sounds/DoubleKnock.wav");
            if (resource == null) {
                throw new IllegalArgumentException("Background music file not found!");
            }

            String soundPath = resource.toExternalForm();
            Media sound = new Media(soundPath);
            backgroundPlayer = new MediaPlayer(sound);

            backgroundPlayer.setCycleCount(1);         // Play once
            backgroundPlayer.setVolume(0.9);           // 90% volume

            backgroundPlayer.setOnEndOfMedia(() -> {
                System.out.println("Background music finished playing.");
                isMusicPlaying = false;
            });

            backgroundPlayer.play();
            isMusicPlaying = true;

        } catch (Exception e) {
            System.out.println("Error loading background music: " + e.getMessage());
        }
    }

    private void setupSounds() {
        try {
            // Initialize click sound
            URL clickResource = getClass().getResource("/sounds/click.wav");
            if (clickResource == null) {
                // Try alternative sound if click.wav doesn't exist
                clickResource = getClass().getResource("/sounds/DoubleKnock.wav");
            }
            
            if (clickResource != null) {
                Media clickSound = new Media(clickResource.toExternalForm());
                clickSoundPlayer = new MediaPlayer(clickSound);
                clickSoundPlayer.setVolume(0.5); // 50% volume
            } else {
                System.err.println("Click sound file not found");
            }
            
            // Initialize close widget sound
            URL closeResource = getClass().getResource("/sounds/ZipZap.wav");
            if (closeResource == null) {
                // Try alternative sound if close.wav doesn't exist
                closeResource = getClass().getResource("/sounds/ZipZap.wav");
            }
            
            if (closeResource != null) {
                Media closeSound = new Media(closeResource.toExternalForm());
                closeWidgetSoundPlayer = new MediaPlayer(closeSound);
                closeWidgetSoundPlayer.setVolume(0.6); // 60% volume
            } else {
                System.err.println("Close widget sound file not found");
            }
        } catch (Exception e) {
            System.err.println("Error setting up sound effects: " + e.getMessage());
        }
    }
    
    // Add this method to play button click sound
    private void playClickSound() {
        try {
            if (clickSoundPlayer != null) {
                clickSoundPlayer.stop();
                clickSoundPlayer.seek(Duration.ZERO);
                clickSoundPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("Error playing click sound: " + e.getMessage());
        }
    }
    
    // Add this method to play widget close sound
    private void playCloseWidgetSound() {
        try {
            if (closeWidgetSoundPlayer != null) {
                closeWidgetSoundPlayer.stop();
                closeWidgetSoundPlayer.seek(Duration.ZERO);
                closeWidgetSoundPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("Error playing close widget sound: " + e.getMessage());
        }
    }

}
