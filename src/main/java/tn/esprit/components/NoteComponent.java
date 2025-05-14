// In a new file: NoteComponent.java
package tn.esprit.components;

import javafx.scene.layout.VBox;
import javafx.scene.control.TextArea;

public class NoteComponent extends VBox {
    private TextArea contentArea;

    public NoteComponent() {
        contentArea = new TextArea();
        this.getChildren().add(contentArea);
    }

    public void setContent(String content) {
        contentArea.setText(content);
    }
}