package tn.esprit.controllers;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DraggableResizableController {

    private double offsetX, offsetY;
    private double initialWidth, initialHeight;
    private boolean isResizing = false;
    private static final double RESIZE_MARGIN = 10;
    private Region targetNode;
    private double mouseX, mouseY;

    // Initialize drag and resize listeners for the given target node
    public void initialize(Region targetNode) {
        this.targetNode = targetNode;

        // Enable dragging
        targetNode.setOnMousePressed(this::handleMousePressed);
        targetNode.setOnMouseDragged(this::handleMouseDragged);
        targetNode.setOnMouseReleased(this::handleMouseReleased);

        // Enable resizing (right bottom corner)
        targetNode.setOnMouseMoved(this::handleMouseMoved);
    }

    // Handle Mouse Press for dragging or resize
    public void handleMousePressed(MouseEvent event) {
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();
        
        if (isInResizeZone(event)) {
            isResizing = true;
            initialWidth = targetNode.getWidth();
            initialHeight = targetNode.getHeight();
        } else {
            isResizing = false;
            offsetX = event.getX(); // Relative to node
            offsetY = event.getY();
        }
        
        event.consume();
    }

    // Handle mouse dragging for both dragging and resizing
    public void handleMouseDragged(MouseEvent event) {
        if (isResizing) {
            // Calculate new width and height
            double deltaX = event.getSceneX() - mouseX;
            double deltaY = event.getSceneY() - mouseY;
            
            double newWidth = Math.max(150, initialWidth + deltaX);
            double newHeight = Math.max(150, initialHeight + deltaY);
            
            targetNode.setPrefWidth(newWidth);
            targetNode.setPrefHeight(newHeight);
        } else {
            // Handle dragging
            double newX = event.getSceneX() - offsetX;
            double newY = event.getSceneY() - offsetY;
            
            targetNode.setLayoutX(newX);
            targetNode.setLayoutY(newY);
        }
        
        event.consume();
    }

    // Handle Mouse Released for releasing the drag or resize
    public void handleMouseReleased(MouseEvent event) {
        isResizing = false;
        targetNode.setCursor(Cursor.DEFAULT);
        
        event.consume();
    }

    // Detect if the mouse is near the bottom right corner for resizing
    private void handleMouseMoved(MouseEvent event) {
        if (isInResizeZone(event)) {
            targetNode.setCursor(Cursor.SE_RESIZE);
        } else {
            targetNode.setCursor(Cursor.DEFAULT);
        }
    }
    
    // Check if the mouse is in the resize zone (bottom-right corner)
    private boolean isInResizeZone(MouseEvent event) {
        double width = targetNode.getWidth();
        double height = targetNode.getHeight();
        double x = event.getX();
        double y = event.getY();
        
        return x > width - RESIZE_MARGIN && y > height - RESIZE_MARGIN;
    }
}
