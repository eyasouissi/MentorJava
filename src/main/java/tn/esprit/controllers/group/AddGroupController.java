package tn.esprit.controllers.group;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import tn.esprit.entities.group.GroupStudent;
import tn.esprit.entities.project.Project;
import tn.esprit.services.group.GroupService;
import tn.esprit.services.project.ProjectService;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AddGroupController {

    @FXML private Circle imageCircle;
    @FXML private ImageView imageView;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker meetingDatePicker;
    @FXML private TextField imagePathField;
    @FXML private TextField pdfPathField;
    
    @FXML private ListView<String> projectListView;

    private String imagePath;
    private String pdfPath;
    private File selectedImageFile;
    private File selectedPdfFile;
    private final ProjectService projectService = ProjectService.getInstance();


    @FXML
    private void initialize() {
        // Charger les noms des projets disponibles au démarrage
        List<String> projectNames = ProjectService.getInstance().getAllProjectTitles(); // à toi de définir ce service
        projectListView.getItems().addAll(projectNames);
        projectListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
public void handleBrowseImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif"));
    File selectedFile = fileChooser.showOpenDialog(new Stage());

    if (selectedFile != null) {
        selectedImageFile = selectedFile;
        imagePath = selectedFile.getPath(); // MAJ de la variable globale
        imagePathField.setText(imagePath);  // MAJ du champ texte

        Image image = new Image(selectedFile.toURI().toString());
        imageCircle.setFill(new javafx.scene.paint.ImagePattern(image)); // afficher dans le cercle
    }
}


    @FXML
    public void handleBrowsePdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        selectedPdfFile = fileChooser.showOpenDialog(new Stage());

        if (selectedPdfFile != null) {
            pdfPathField.setText(selectedPdfFile.getPath());  // Afficher le chemin dans le TextField
        }
    }


    @FXML
private void handleSave(ActionEvent event) throws SQLException {
    String name = nameField.getText();
    String description = descriptionField.getText();
    LocalDate meetingDate = meetingDatePicker.getValue();

    if (name.isEmpty() || description.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Erreur", "Les champs marqués d'un * sont obligatoires");
        return;
    }

    if (meetingDate != null && meetingDate.isBefore(LocalDate.now())) {
        showAlert(Alert.AlertType.ERROR, "Erreur", "La date de réunion doit être dans le futur");
        return;
    }

    GroupStudent group = new GroupStudent();
    group.setName(name);
    group.setDescription(description);
    group.setMeetingDate(meetingDate);
    group.setImage(imagePath);
    group.setPdfFile(pdfPath);

    // Associer les projets sélectionnés au groupe
    ObservableList<String> selectedProjectTitles = projectListView.getSelectionModel().getSelectedItems();
    for (String title : selectedProjectTitles) {
            Optional<Project> projectOpt = projectService.findByTitle(title);
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                group.addProject(project);
            } else {
                showAlert(Alert.AlertType.WARNING, "Avertissement", 
                        "Le projet '" + title + "' n'a pas été trouvé et ne sera pas associé au groupe");
            }
        }
    try {
        GroupService.getInstance().ajouter(group); // il va aussi sauvegarder les projets associés
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Groupe ajouté avec succès !");
        closeWindow();
    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du groupe : " + e.getMessage());
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Une erreur s'est produite");
        alert.setContentText("Impossible d'enregistrer les données : " + e.getMessage());
        alert.showAndWait();
    }
}


    @FXML
    private void cancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToGroupsView(ActionEvent event) {
        navigate("/group/GroupsView.fxml", event);
    }

    private void navigate(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
