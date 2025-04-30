// NotesWidget.java
package tn.esprit.controllers;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class NotesWidget extends StackPane {
    private final VBox notesContainer;
    private final NotesAIController aiController;

    public NotesWidget() {
        this.aiController = new NotesAIController();
        this.setStyle("-fx-background-color: rgba(240,240,240,0.95); -fx-border-color: #ccc;");

        // Container setup
        notesContainer = new VBox(15);
        notesContainer.setPrefSize(300, 400);
        notesContainer.setStyle("-fx-padding: 15;");

        // Add Note Button
        Button addBtn = new Button("+ New Note");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addNewNote());

        this.getChildren().addAll(notesContainer, addBtn);
        StackPane.setAlignment(addBtn, javafx.geometry.Pos.BOTTOM_RIGHT);
    }

    private void addNewNote() {
        NoteComponent note = new NoteComponent(aiController);
        notesContainer.getChildren().add(note);
    }
}