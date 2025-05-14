package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TodoListWidget extends VBox {
    private VBox todoItemsContainer;
    private TextField addTaskField;
    private Color currentColor = Color.rgb(255, 230, 230); // Default light pink
    private Consumer<Void> onStateChanged;
    private Runnable onCloseSoundCallback;
    private final List<String> pastelColors = List.of(
            "#FFE6E6", // Light Pink
            "#E6F9FF", // Light Blue
            "#E6FFEA", // Light Green
            "#FFF8E6", // Light Yellow
            "#F0E6FF", // Light Purple
            "#FFE6F0", // Light Rose
            "#E6FFF5"  // Light Mint
    );
    private int colorIndex = 0;

    public TodoListWidget() {
        this.setPadding(new Insets(15));
        this.setSpacing(10);
        this.setPrefWidth(300);
        this.setMinWidth(250);
        this.setMaxWidth(400);
        
        // Apply initial styling
        updateContainerStyle();
        
        // Create title with color picker
        HBox titleBar = createTitleBar();
        
        // Create the main todo list container
        todoItemsContainer = new VBox(5);
        todoItemsContainer.getStyleClass().add("todo-items-container");
        todoItemsContainer.setPadding(new Insets(10, 5, 10, 5));
        
        // Create the input field for new tasks
        HBox inputContainer = createInputBar();
        
        // Add all components to the main container
        this.getChildren().addAll(titleBar, new Separator(), todoItemsContainer, new Separator(), inputContainer);
        
        // Apply shadow effect
        this.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.2)));
    }
    
    private HBox createTitleBar() {
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 0, 5, 0));
        
        // Create title
        Label titleLabel = new Label("To-Do List");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #333333;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        // Create color change button
        Button colorButton = new Button("🎨");
        colorButton.setTooltip(new Tooltip("Change color"));
        colorButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
        colorButton.setOnAction(e -> changeColor());
        
        // Create close button
        Button closeButton = new Button("✕");
        closeButton.setTooltip(new Tooltip("Close"));
        closeButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #888888;");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #e74c3c;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #888888;"));
        closeButton.setOnAction(e -> animateAndClose());
        
        titleBar.getChildren().addAll(titleLabel, colorButton, closeButton);
        return titleBar;
    }
    
    private HBox createInputBar() {
        HBox inputContainer = new HBox(5);
        inputContainer.setAlignment(Pos.CENTER);
        
        addTaskField = new TextField();
        addTaskField.setPromptText("Add a new task...");
        addTaskField.getStyleClass().add("todo-input");
        HBox.setHgrow(addTaskField, Priority.ALWAYS);
        
        Button addButton = new Button("+");
        addButton.getStyleClass().add("todo-add-button");
        addButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20;");
        addButton.setOnAction(e -> addTask());
        
        // Add task on Enter key
        addTaskField.setOnAction(e -> addTask());
        
        inputContainer.getChildren().addAll(addTaskField, addButton);
        return inputContainer;
    }
    
    public void addTask() {
        String taskText = addTaskField.getText().trim();
        if (!taskText.isEmpty()) {
            addTaskItem(taskText, false);
            addTaskField.clear();
            notifyStateChanged();
        }
    }
    
    public HBox addTaskItem(String text, boolean isCompleted) {
        HBox taskItem = new HBox(10);
        taskItem.setPadding(new Insets(8, 5, 8, 5));
        taskItem.setAlignment(Pos.CENTER_LEFT);
        taskItem.getStyleClass().add("todo-item");
        
        // Create attractive checkbox
        CheckBox checkBox = new CheckBox();
        checkBox.getStyleClass().add("todo-checkbox");
        checkBox.setSelected(isCompleted);
        
        // Create task label
        Label taskLabel = new Label(text);
        taskLabel.getStyleClass().add("todo-task-text");
        taskLabel.setWrapText(true);
        taskLabel.setStyle(isCompleted ? "-fx-strikethrough: true; -fx-text-fill: #95a5a6;" : "-fx-text-fill: #333333;");
        HBox.setHgrow(taskLabel, Priority.ALWAYS);
        
        // Create delete button
        Button deleteButton = new Button("✕");
        deleteButton.getStyleClass().add("todo-delete-button");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-cursor: hand;");
        
        // Add hover effect for each task item
        taskItem.setOnMouseEntered(e -> 
            taskItem.setStyle("-fx-background-color: rgba(0, 0, 0, 0.05); -fx-background-radius: 5;"));
        taskItem.setOnMouseExited(e -> 
            taskItem.setStyle("-fx-background-color: transparent;"));
        
        // Set up checkbox action
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            taskLabel.setStyle(newVal ? 
                "-fx-strikethrough: true; -fx-text-fill: #95a5a6;" : 
                "-fx-text-fill: #333333;");
            notifyStateChanged();
        });
        
        // Set up delete button action
        deleteButton.setOnAction(e -> {
            todoItemsContainer.getChildren().remove(taskItem);
            notifyStateChanged();
        });
        
        taskItem.getChildren().addAll(checkBox, taskLabel, deleteButton);
        todoItemsContainer.getChildren().add(taskItem);
        
        return taskItem;
    }
    
    private void changeColor() {
        // Cycle through pastel colors
        colorIndex = (colorIndex + 1) % pastelColors.size();
        String hexColor = pastelColors.get(colorIndex);
        currentColor = Color.web(hexColor);
        
        try {
            // Create animation for color change
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(100), this);
            scaleOut.setFromX(1.0);
            scaleOut.setFromY(1.0);
            scaleOut.setToX(1.02);
            scaleOut.setToY(1.02);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(100), this);
            scaleIn.setFromX(1.02);
            scaleIn.setFromY(1.02);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            SequentialTransition sequence = new SequentialTransition(scaleOut, scaleIn);
            sequence.setOnFinished(e -> {
                updateContainerStyle();
                notifyStateChanged();
            });
            
            sequence.play();
        } catch (Exception e) {
            // If animation fails, just update the style directly
            System.err.println("Animation error: " + e.getMessage());
            updateContainerStyle();
            notifyStateChanged();
        }
    }
    
    /**
     * Set a callback to be called when the widget is closed.
     * This can be used to play a sound effect.
     */
    public void setOnCloseSound(Runnable callback) {
        this.onCloseSoundCallback = callback;
    }
    
    public void animateAndClose() {
        // Play close sound if callback is set
        if (onCloseSoundCallback != null) {
            onCloseSoundCallback.run();
        }
        
        try {
            // Create pop animation
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), this);
            scaleOut.setFromX(1.0);
            scaleOut.setFromY(1.0);
            scaleOut.setToX(1.2);
            scaleOut.setToY(1.2);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), this);
            scaleIn.setFromX(1.2);
            scaleIn.setFromY(1.2);
            scaleIn.setToX(0.0);
            scaleIn.setToY(0.0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), this);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            // Chain animations
            SequentialTransition sequence = new SequentialTransition(
                    scaleOut,
                    new ParallelTransition(scaleIn, fadeOut)
            );

            sequence.setOnFinished(e -> {
                try {
                    // First make sure we're invisible
                    this.setVisible(false);
                    
                    // Also hide the parent container if it exists
                    if (this.getParent() != null) {
                        this.getParent().setVisible(false);
                        
                        // Try to remove from parent if it's a Pane
                        if (this.getParent() instanceof Pane) {
                            // Just hide instead of removing
                            this.getParent().setVisible(false);
                        }
                    }
                    
                    // Notify about state changes
                    notifyStateChanged();
                } catch (Exception ex) {
                    System.err.println("Error closing todo widget: " + ex.getMessage());
                    // Fallback method
                    this.setVisible(false);
                    if (this.getParent() != null) {
                        this.getParent().setVisible(false);
                    }
                    notifyStateChanged();
                }
            });

            sequence.play();
        } catch (Exception e) {
            System.err.println("Animation error: " + e.getMessage());
            // Fallback to just hiding the widget
            this.setVisible(false);
            if (this.getParent() != null) {
                this.getParent().setVisible(false);
            }
            notifyStateChanged();
        }
    }
    
    /**
     * Resets the widget visibility and opacity for reopening
     */
    public void restore() {
        this.setVisible(true);
        this.setOpacity(1.0);
        this.setScaleX(1.0);
        this.setScaleY(1.0);
    }
    
    private void updateContainerStyle() {
        // Convert Color to hex for CSS
        String hexColor = String.format("#%02X%02X%02X%02X",
                (int) (currentColor.getRed() * 255),
                (int) (currentColor.getGreen() * 255),
                (int) (currentColor.getBlue() * 255),
                (int) (0.85 * 255)); // Add alpha channel for transparency (85%)
        
        // Create a slightly darker color for border
        Color borderColor = currentColor.darker();
        String borderHex = String.format("#%02X%02X%02X",
                (int) (borderColor.getRed() * 255),
                (int) (borderColor.getGreen() * 255),
                (int) (borderColor.getBlue() * 255));
        
        this.setStyle(
                "-fx-background-color: " + hexColor + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: " + borderHex + ";" +
                "-fx-border-width: 1;"
        );
    }
    
    public void setOnStateChanged(Consumer<Void> callback) {
        this.onStateChanged = callback;
    }
    
    private void notifyStateChanged() {
        if (onStateChanged != null) {
            onStateChanged.accept(null);
        }
    }
    
    public List<TodoItem> getTodoItems() {
        List<TodoItem> items = new ArrayList<>();
        for (int i = 0; i < todoItemsContainer.getChildren().size(); i++) {
            HBox itemBox = (HBox) todoItemsContainer.getChildren().get(i);
            CheckBox checkBox = (CheckBox) itemBox.getChildren().get(0);
            Label textLabel = (Label) itemBox.getChildren().get(1);
            
            TodoItem item = new TodoItem();
            item.setText(textLabel.getText());
            item.setCompleted(checkBox.isSelected());
            items.add(item);
        }
        return items;
    }
    
    public void clearTasks() {
        todoItemsContainer.getChildren().clear();
    }
    
    public Color getCurrentColor() {
        return currentColor;
    }
    
    public void setCurrentColor(Color color) {
        this.currentColor = color;
        updateContainerStyle();
    }
    
    public static class TodoItem {
        private String text;
        private boolean completed;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
} 