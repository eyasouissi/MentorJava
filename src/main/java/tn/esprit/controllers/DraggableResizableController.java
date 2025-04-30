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
   // private VBox targetNode;
    private static final double RESIZE_MARGIN = 8;
    private Region targetNode;

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

    // Handle Mouse Press for dragging
    public void handleMousePressed(MouseEvent event) {
        if (!isResizing) {
            offsetX = event.getSceneX() - targetNode.getLayoutX();
            offsetY = event.getSceneY() - targetNode.getLayoutY();
        } else {
            initialWidth = targetNode.getWidth();
            initialHeight = targetNode.getHeight();
        }
    }

    // Handle Mouse Dragging for moving or resizing
    public void handleMouseDragged(MouseEvent event) {
        if (isResizing) {
            double newWidth = Math.max(300, event.getX());
            double newHeight = Math.max(200, event.getY());
            targetNode.setPrefWidth(newWidth);
            targetNode.setPrefHeight(newHeight);
        } else {
            targetNode.setLayoutX(event.getSceneX() - offsetX);
            targetNode.setLayoutY(event.getSceneY() - offsetY);
        }
    }

    // Handle Mouse Released for releasing the drag or resize
    private void handleMouseReleased(MouseEvent event) {
        isResizing = false;
    }

    // Detect if the mouse is near the bottom right corner for resizing
    private void handleMouseMoved(MouseEvent event) {
        boolean rightEdge = event.getX() > targetNode.getWidth() - RESIZE_MARGIN;
        boolean bottomEdge = event.getY() > targetNode.getHeight() - RESIZE_MARGIN;

        if (rightEdge && bottomEdge) {
            targetNode.setCursor(Cursor.SE_RESIZE);
            isResizing = true;
        } else {
            targetNode.setCursor(Cursor.DEFAULT);
            isResizing = false;
        }
    }
}
