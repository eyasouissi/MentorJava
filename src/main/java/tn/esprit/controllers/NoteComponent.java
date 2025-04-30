// NoteComponent.java
package tn.esprit.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class NoteComponent extends VBox {
    private final TextArea textArea;
    private final NotesAIController aiController;

    public NoteComponent(NotesAIController aiController) {
        this.aiController = aiController;
        setNoteStyle(Color.LIGHTYELLOW);

        // Text Area
        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefSize(250, 180);
        textArea.setStyle("-fx-control-inner-background: #ffffe6;");

        // Control Panel
        HBox controls = new HBox(10);
        ColorPicker colorPicker = new ColorPicker(Color.LIGHTYELLOW);
        Button summarizeBtn = new Button("Summarize");
        Button deleteBtn = new Button("✕");

        // Style buttons
        summarizeBtn.setStyle("-fx-background-color: #90EE90; -fx-text-fill: #006400;");
        deleteBtn.setStyle("-fx-text-fill: #8B0000; -fx-font-weight: bold;");

        // Event Handlers
        colorPicker.setOnAction(e -> setNoteStyle(colorPicker.getValue()));

        summarizeBtn.setOnAction(e -> {
            String original = textArea.getText();
            if (!original.isEmpty()) {
                try {
                    String summary = aiController.summarizeText(original);
                    textArea.setText("Original:\n" + original + "\n\nSummary:\n" + summary);
                } catch (Exception ex) {
                    textArea.setText("Error: " + ex.getMessage());
                }
            }
        });

        deleteBtn.setOnAction(e -> ((VBox)this.getParent()).getChildren().remove(this));

        controls.getChildren().addAll(colorPicker, summarizeBtn, deleteBtn);
        this.getChildren().addAll(textArea, controls);
        this.setSpacing(10);
    }

    private void setNoteStyle(Color color) {
        String hex = color.toString().replace("0x", "#");
        this.setStyle("-fx-background-color: " + hex + "; -fx-border-color: derive(" + hex + ", -20%);" +
                "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");
    }
}