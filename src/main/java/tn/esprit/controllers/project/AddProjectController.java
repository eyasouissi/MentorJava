package tn.esprit.controllers.project;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.fxmisc.richtext.StyleClassedTextArea;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.project.Project;
import tn.esprit.services.project.ProjectService;

public class AddProjectController {

    @FXML private TextField titleField;
    @FXML private StyleClassedTextArea descriptionArea;
    @FXML private DatePicker deadlinePicker;
    @FXML private TextField pdfField;
    @FXML private TextField imageField;
    @FXML private Button browsePdfButton;
    @FXML private Button browseImageButton;
    @FXML private Button saveButton;
    
    // Étoiles de difficulté
    @FXML private Button star1;
    @FXML private Button star2;
    @FXML private Button star3;
    @FXML private Button star4;
    
    // Barre d'outils de formatage
    @FXML private ToolBar formatToolbar;
    @FXML private ComboBox<String> fontFamilyCombo;
    @FXML private ComboBox<Integer> fontSizeCombo;
    @FXML private ColorPicker textColorPicker;
    @FXML private ToggleButton boldButton;
    @FXML private ToggleButton italicButton;
    @FXML private Button emojiButton; 
    @FXML private ToggleButton underlineButton;
    @FXML private Button undoButton;
    @FXML private Button redoButton;

    private int selectedDifficulty = 0;
    private String pdfPath;
    private String imagePath;

    @FXML
    public void initialize() {
        // Initialisation des étoiles
        resetStars();
        
        // Initialisation de l'éditeur de texte riche
        setupRichTextEditor();
        underlineButton.setOnAction(e -> updateTextStyle());
        descriptionArea.getStyleClass().add("rich-text-area");

    }
    
    private void setupRichTextEditor() {
        fontFamilyCombo.getItems().addAll(Font.getFamilies());
        fontFamilyCombo.setValue("Arial");
    
        fontSizeCombo.getItems().addAll(8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 28, 32, 36);
        fontSizeCombo.setValue(12);
    
        fontFamilyCombo.setOnAction(e -> updateTextStyle());
        fontSizeCombo.setOnAction(e -> updateTextStyle());
        textColorPicker.setOnAction(e -> updateTextStyle());
        boldButton.setOnAction(e -> updateTextStyle());
        italicButton.setOnAction(e -> updateTextStyle());
        emojiButton.setOnAction(this::showEmojiMenu);
        underlineButton.setOnAction(e -> updateTextStyle());
    }
    

    @FXML
private void alignLeft(ActionEvent event) {
    descriptionArea.setStyle("-fx-text-alignment: left;");
}

@FXML
private void alignCenter(ActionEvent event) {
    descriptionArea.setStyle("-fx-text-alignment: center;");
}

@FXML
private void alignRight(ActionEvent event) {
    descriptionArea.setStyle("-fx-text-alignment: right;");
}

    private void updateTextAlignment(String alignment) {
        descriptionArea.setStyle("-fx-text-alignment: " + alignment + ";");
    }
    

    @FXML
private void insertBullet(ActionEvent event) {
    int caret = descriptionArea.getCaretPosition();
    descriptionArea.insertText(caret, "• ");
}

@FXML
private void insertNumberedList(ActionEvent event) {
    int caret = descriptionArea.getCaretPosition();
    descriptionArea.insertText(caret, "1. ");
}


    @FXML
    private void undoAction(ActionEvent event) {
        System.out.println("Undo clicked!");
        // Exemple basique : faire un undo sur TextArea
        // À améliorer si tu utilises Trumbowyg ou un éditeur HTML plus complexe
        if (descriptionArea != null) {
            descriptionArea.undo();
        }
    }
    
    @FXML
    private void redoAction(ActionEvent event) {
        System.out.println("Redo clicked!");
        if (descriptionArea != null) {
            descriptionArea.redo();
        }
    }
    

    @FXML
    private void showEmojiMenu(ActionEvent event) { // Ajoutez cette méthode
        ContextMenu emojiMenu = new ContextMenu();
        
        String[] emojis = {"😀", "😂", "😍", "👍", "👎", "🔥", "⭐", "🎉"};
        for (String emoji : emojis) {
            MenuItem item = new MenuItem(emoji);
            item.setOnAction(e -> descriptionArea.insertText(descriptionArea.getCaretPosition(), emoji));
            emojiMenu.getItems().add(item);
        }
        
        emojiMenu.show(emojiButton, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    @FXML
private void updateTextStyle() {
    String style = String.format(
        "-fx-font-family: '%s'; -fx-font-size: %dpx; -fx-text-fill: %s; %s %s %s",
        fontFamilyCombo.getValue(),
        fontSizeCombo.getValue(),
        toHexString(textColorPicker.getValue()),
        boldButton.isSelected() ? "-fx-font-weight: bold;" : "",
        italicButton.isSelected() ? "-fx-font-style: italic;" : "",
        underlineButton.isSelected() ? "-fx-underline: true;" : ""
    );
    descriptionArea.setStyle(style);
}

private String toHexString(Color color) {
    return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
}


private String removeStyleProperty(String style, String property) {
    return style.replaceAll(property + ":[^;]*;", "");
}
    
    private void showEmojiPicker() {
        ContextMenu emojiMenu = new ContextMenu();
        
        String[] emojis = {"😀", "😂", "😍", "👍", "👎", "🔥", "⭐", "🎉", "❤️", "✨", "🙏", "👀"};
        for (String emoji : emojis) {
            MenuItem item = new MenuItem(emoji);
            item.setOnAction(e -> descriptionArea.insertText(descriptionArea.getCaretPosition(), emoji));
            emojiMenu.getItems().add(item);
        }
        
        emojiMenu.show(emojiButton, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    @FXML
    private void handleStarClick(ActionEvent event) {
        Button clickedStar = (Button) event.getSource();
        resetStars();
        
        if (clickedStar == star1) {
            selectedDifficulty = 1;
            star1.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 24px; -fx-background-color: transparent;");
        } else if (clickedStar == star2) {
            selectedDifficulty = 2;
            star1.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 24px; -fx-background-color: transparent;");
            star2.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 24px; -fx-background-color: transparent;");
        } else if (clickedStar == star3) {
            selectedDifficulty = 3;
            star1.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 24px; -fx-background-color: transparent;");
            star2.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 24px; -fx-background-color: transparent;");
            star3.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 24px; -fx-background-color: transparent;");
        } else if (clickedStar == star4) {
            selectedDifficulty = 4;
            star1.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 24px; -fx-background-color: transparent;");
            star2.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 24px; -fx-background-color: transparent;");
            star3.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 24px; -fx-background-color: transparent;");
            star4.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 24px; -fx-background-color: transparent;");
        }
    }
    
    private void resetStars() {
        String defaultStyle = "-fx-text-fill: #bdc3c7; -fx-font-size: 24px; -fx-background-color: transparent;";
        star1.setStyle(defaultStyle);
        star2.setStyle(defaultStyle);
        star3.setStyle(defaultStyle);
        star4.setStyle(defaultStyle);
    }

    @FXML
    private void handleBrowsePdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(browsePdfButton.getScene().getWindow());
        if (selectedFile != null) {
            pdfPath = selectedFile.getAbsolutePath();
            pdfField.setText(pdfPath);
        }
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(browseImageButton.getScene().getWindow());
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            imageField.setText(imagePath);
        }
    }

    @FXML
private void handleSave(ActionEvent event) {
    String titre = titleField.getText();
    String description = descriptionArea.getText();
    LocalDate deadline = deadlinePicker.getValue();

    if (titre.isEmpty() || description.isEmpty() || selectedDifficulty == 0) {
        showAlert(Alert.AlertType.ERROR, 
                 "Erreur de validation", 
                 "Veuillez remplir tous les champs obligatoires et sélectionner une difficulté.");
        return;
    }

    Project project = new Project();
    project.setTitre(titre);
    project.setDescriptionProject(description);
    project.setDifficulte(selectedDifficulty);
    project.setDeadline(deadline);
    project.setPdfFile(pdfPath);
    project.setImage(imagePath);

    try {
        ProjectService.getInstance().ajouter(project);
        showAlert(Alert.AlertType.INFORMATION, 
                 "Succès", 
                 "Projet ajouté avec succès !");
        clearFields();
    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, 
                 "Erreur d'enregistrement", 
                 "Erreur lors de l'ajout du projet : " + e.getMessage());
    }
}

    private void clearFields() {
        titleField.clear();
        descriptionArea.clear();
        deadlinePicker.setValue(null);
        pdfField.clear();
        imageField.clear();
        pdfPath = null;
        imagePath = null;
        selectedDifficulty = 0;
        resetStars();
        
        // Réinitialiser les styles de texte
        fontFamilyCombo.setValue("Arial");
        fontSizeCombo.setValue(12);
        textColorPicker.setValue(Color.BLACK);
        boldButton.setSelected(false);
        italicButton.setSelected(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToGroups(ActionEvent event) {
        navigate("/group/GroupsView.fxml", event);
    }

    @FXML
    private void goToProjects(ActionEvent event) {
        navigate("/project/ProjectsView.fxml", event);
    }
    
    private void navigate(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        clearFields();
    }
    @FXML
private void toggleTheme(ActionEvent event) {
    Scene scene = ((Node) event.getSource()).getScene();
    Button themeButton = (Button) event.getSource();
    
    if (scene.getStylesheets().contains(getClass().getResource("/styles/dark-theme.css").toExternalForm())) {
        scene.getStylesheets().remove(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles/light-theme.css").toExternalForm());
        themeButton.setText("🌙"); // Icône pour passer en mode sombre
    } else {
        scene.getStylesheets().remove(getClass().getResource("/styles/light-theme.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        themeButton.setText("☀️"); // Icône pour passer en mode clair
    }
}
@FXML
private void goToProjectsView(ActionEvent event) {
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/project/ProjectsView.fxml"));
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        showAlert(Alert.AlertType.ERROR, "Error", "Could not navigate to projects view");
    }
}
@FXML
    private void goToGroupsView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/group/GroupsView.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not navigate to groups view");
        }
    }
}