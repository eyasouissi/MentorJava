// NotesWidget.java
package tn.esprit.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Random;

public class NotesWidget extends VBox {
    private final NotesAIController aiController;
    private Pane workspacePane;
    private Random random = new Random();

    public NotesWidget() {
        this.aiController = new NotesAIController();
        
        // Setup the container appearance
        this.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-border-color: #dddddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        this.setPadding(new Insets(10));
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setPrefSize(150, 80);
        
        // Create the "Add Note" button
        Button addNoteBtn = new Button("Add Note");
        addNoteBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        addNoteBtn.setPrefWidth(120);
        addNoteBtn.setOnAction(e -> addNewStickyNote());
        
        this.getChildren().add(addNoteBtn);
    }
    
    public void setWorkspacePane(Pane workspacePane) {
        this.workspacePane = workspacePane;
    }
    
    private void addNewStickyNote() {
        if (workspacePane == null) {
            System.err.println("Workspace pane is not set!");
            return;
        }
        
        // Create a new sticky note
        StickyNote note = new StickyNote(aiController);
        
        // Position the note with slight randomization to prevent perfect stacking
        double baseX = 100 + random.nextInt(50); 
        double baseY = 100 + random.nextInt(50);
        
        note.setLayoutX(baseX);
        note.setLayoutY(baseY);
        
        // Give each note a unique ID for state management
        note.setId("stickyNote_" + System.currentTimeMillis());
        
        // Add the note to the workspace
        workspacePane.getChildren().add(note);
        note.toFront();
    }
}