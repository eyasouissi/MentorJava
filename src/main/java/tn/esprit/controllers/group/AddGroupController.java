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
    private static final String UPLOAD_DIRECTORY = "C:/xampp/htdocs/uploads/";

    @FXML
    private void initialize() {
        // Charger les noms des projets disponibles au démarrage
        List<String> projectNames = ProjectService.getInstance().getAllProjectTitles(); // à toi de définir ce service
        projectListView.getItems().addAll(projectNames);
        projectListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private String copyFileToUploadDirectory(File sourceFile) throws IOException {
        File uploadDir = new File(UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        File destFile = new File(uploadDir, fileName);
        java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath());
        return destFile.getAbsolutePath();
    }

    @FXML
    public void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            try {
                String newImagePath = copyFileToUploadDirectory(selectedFile);
                imagePath = newImagePath; // Update the global imagePath
                imagePathField.setText(newImagePath);

                // Display the uploaded image
                Image image = new Image(new File(newImagePath).toURI().toString());
                imageCircle.setFill(new javafx.scene.paint.ImagePattern(image));
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBrowsePdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            try {
                String newPdfPath = copyFileToUploadDirectory(selectedFile);
                pdfPath = newPdfPath; // Update the global pdfPath
                pdfPathField.setText(newPdfPath);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload PDF: " + e.getMessage());
            }
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
