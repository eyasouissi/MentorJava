package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.net.URL;

public class StickyNote extends StackPane {
    private TextArea textArea;
    private final NotesAIController aiController;
    private DraggableResizableController dragController;
    private boolean isBold = false;
    private boolean isResizingBorderVisible = false;
    private Color currentColor = Color.YELLOW;
    private Runnable onCloseSoundCallback;

    public StickyNote(NotesAIController aiController) {
        this.aiController = aiController;
        this.dragController = new DraggableResizableController();
        this.setPrefSize(250, 250);
        this.setMinSize(150, 150);
        
        // Initialize main content box
        VBox contentBox = new VBox(5);
        contentBox.setPadding(new Insets(10));
        contentBox.setAlignment(Pos.TOP_CENTER);
        
        // Create text area first
        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefHeight(180);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        
        // Initialize the UI with base styles
        this.setStyle("-fx-background-color: #FFFFE0; " +
                     "-fx-background-radius: 5; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 1, 1);");
        
        textArea.setStyle("-fx-background-color: transparent; " +
                         "-fx-control-inner-background: #FFFFC0; " +
                         "-fx-background: transparent; " + 
                         "-fx-border-color: transparent;");
        
        // Create control panel
        HBox toolbar = new HBox(5);
        toolbar.setAlignment(Pos.CENTER);
        
        // Add color picker
        ColorPicker colorPicker = new ColorPicker(Color.YELLOW);
        colorPicker.setMaxWidth(30);
        colorPicker.setOnAction(e -> setNoteColor(colorPicker.getValue()));
        
        // Bold toggle button
        ToggleButton boldButton = new ToggleButton("B");
        boldButton.setStyle("-fx-font-weight: bold; -fx-min-width: 30px;");
        boldButton.setOnAction(e -> toggleBold());
        
        // Summarize button with image
        Button summarizeBtn = new Button();
        try {
            URL iconUrl = getClass().getResource("/images/summarize.png");
            if (iconUrl != null) {
                ImageView summarizeIcon = new ImageView(new Image(iconUrl.toString()));
                summarizeIcon.setFitHeight(16);
                summarizeIcon.setFitWidth(16);
                summarizeBtn.setGraphic(summarizeIcon);
            } else {
                summarizeBtn.setText("AI");
                summarizeBtn.setStyle("-fx-background-color: #7d7598; -fx-font-size: 10px;");
            }
        } catch (Exception e) {
            summarizeBtn.setText("AI");
            summarizeBtn.setStyle("-fx-background-color: #90EE90; -fx-font-size: 10px;");
        }
        summarizeBtn.setTooltip(new Tooltip("Summarize text"));
        summarizeBtn.setOnAction(e -> summarizeContent());
        
        // Add controls to toolbar
        toolbar.getChildren().addAll(colorPicker, boldButton, summarizeBtn);
        
        // Add elements to content box
        contentBox.getChildren().addAll(textArea, toolbar);
        
        // Add content box to note
        this.getChildren().add(contentBox);
        
        // Now that everything is initialized, set color properly
        setNoteColor(Color.YELLOW);
        
        // Setup drag and resize
        dragController.initialize(this);
        
        // Double-click handler
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                animateAndRemove();
            }
        });
        
        // Resize border handlers
        this.setOnMouseExited(e -> showResizeBorder(false));
        this.setOnMouseEntered(e -> showResizeBorder(true));
    }

    // Method to set the callback for playing close sound
    public void setOnCloseSound(Runnable callback) {
        this.onCloseSoundCallback = callback;
    }
    
    private void showResizeBorder(boolean show) {
        isResizingBorderVisible = show;
        setNoteColor(currentColor);
    }
    
    private void setNoteColor(Color color) {
        // Store current color
        this.currentColor = color;
        
        // Generate hex color strings
        String hex = String.format("#%02X%02X%02X", 
                (int)(color.getRed() * 255), 
                (int)(color.getGreen() * 255), 
                (int)(color.getBlue() * 255));
        
        // Create a slightly lighter color for background
        String lighterHex = String.format("#%02X%02X%02X", 
                Math.min(255, (int)(color.getRed() * 255) + 30), 
                Math.min(255, (int)(color.getGreen() * 255) + 30), 
                Math.min(255, (int)(color.getBlue() * 255) + 30));
        
        // Set note background
        this.setStyle("-fx-background-color: " + lighterHex + "; " +
                "-fx-background-radius: 5; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 1, 1); " +
                (isResizingBorderVisible ? "-fx-border-width: 1; -fx-border-color: #888888;" : ""));
        
        // Update textarea background
        if (textArea != null) {
            textArea.setStyle("-fx-background-color: transparent; -fx-control-inner-background: " + hex + ";");
        }
    }
    
    private void toggleBold() {
        isBold = !isBold;
        if (isBold) {
            textArea.setFont(Font.font(textArea.getFont().getFamily(), FontWeight.BOLD, textArea.getFont().getSize()));
        } else {
            textArea.setFont(Font.font(textArea.getFont().getFamily(), FontWeight.NORMAL, textArea.getFont().getSize()));
        }
    }
    
    private void summarizeContent() {
        String original = textArea.getText().trim();
        if (!original.isEmpty()) {
            try {
                // Show loading indicator
                textArea.setDisable(true);
                textArea.setText("Summarizing...");
                
                // Run in separate thread to prevent UI freezing
                new Thread(() -> {
                    try {
                        String summary = aiController.summarizeText(original);
                        javafx.application.Platform.runLater(() -> {
                            textArea.setText(summary);
                            textArea.setDisable(false);
                        });
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> {
                            textArea.setText("Summarization failed: " + ex.getMessage() + "\n\nOriginal text:\n" + original);
                            textArea.setDisable(false);
                        });
                    }
                }).start();
            } catch (Exception ex) {
                textArea.setText("Error: " + ex.getMessage());
            }
        }
    }
    
    public void animateAndRemove() {
        // Play close sound if callback is set
        if (onCloseSoundCallback != null) {
            onCloseSoundCallback.run();
        }
        
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
                if (this.getParent() != null) {
                    // First check if we're already being removed (might be in process)
                    if (this.getParent() instanceof Pane) {
                        Pane parent = (Pane) this.getParent();
                        // Check if this node is still in the parent's children list
                        if (parent.getChildren().contains(this)) {
                            parent.getChildren().remove(this);
                            System.out.println("Successfully removed note from parent");
                        } else {
                            // Node is already being removed, just make sure it's not visible
                            this.setVisible(false);
                            System.out.println("Note already being removed, setting invisible");
                        }
                    } else {
                        // Just hide it if parent is not a Pane
                        this.setVisible(false);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error removing note: " + ex.getMessage());
                // Fallback method - hide it
                this.setVisible(false);
            }
        });

        sequence.play();
    }
} 