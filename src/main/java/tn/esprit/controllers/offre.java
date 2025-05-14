package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Offre;
import tn.esprit.services.offreService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class offre {
    @FXML
    private TextField nom_offre;

    @FXML
    private TextField image_offre;

    @FXML
    private TextField prix;

    @FXML
    private DatePicker date_debut;

    @FXML
    private DatePicker date_fin;

    @FXML
    private TextField description;

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnChoisirImage;

    private File selectedFile;

    @FXML
    public void initialize() {
        // Initialiser les champs si nécessaire
        date_debut.setValue(LocalDate.now());
    }

    @FXML
    public void choisirImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedFile = fileChooser.showOpenDialog(btnChoisirImage.getScene().getWindow());
        if (selectedFile != null) {
            image_offre.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    public void ajouterOffre(ActionEvent event) {
        try {
            // Validation des champs
            if (nom_offre.getText().isEmpty() || prix.getText().isEmpty() ||
                    date_debut.getValue() == null || date_fin.getValue() == null ||
                    description.getText().isEmpty()) {
                afficherAlerte("Erreur", "Tous les champs sont obligatoires.");
                return;
            }

            String nom = nom_offre.getText();
            Double prixVal;
            try {
                prixVal = Double.parseDouble(prix.getText());
                if (prixVal <= 0) {
                    afficherAlerte("Erreur", "Le prix doit être supérieur à 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                afficherAlerte("Erreur", "Le prix doit être un nombre valide.");
                return;
            }

            LocalDateTime dateDebut = date_debut.getValue().atStartOfDay();
            LocalDate dateFin = date_fin.getValue();

            // Vérifier que la date de fin est après la date de début
            if (dateFin.isBefore(date_debut.getValue())) {
                afficherAlerte("Erreur", "La date de fin doit être après la date de début.");
                return;
            }

            String desc = description.getText();

            // Copier l'image dans un dossier du projet si une image est sélectionnée
            String imagePath = "";
            if (selectedFile != null) {
                Path destination = Paths.get("src/main/resources/images/" + selectedFile.getName());
                Files.createDirectories(destination.getParent());
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                imagePath = "/images/" + selectedFile.getName();
            }

            // Créer l'offre
            Offre nouvelleOffre = new Offre(nom, imagePath, prixVal, dateDebut, dateFin, desc);

            // Ajouter l'offre à la base de données en utilisant le service
            offreService service = new offreService();
            service.ajouter(nouvelleOffre);

            // Réinitialiser les champs
            reinitialiserChamps();

            afficherAlerte("Succès", "L'offre a été ajoutée avec succès!");

        } catch (Exception e) {
            afficherAlerte("Erreur", "Une erreur s'est produite: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void reinitialiserChamps() {
        nom_offre.clear();
        image_offre.clear();
        prix.clear();
        date_debut.setValue(LocalDate.now());
        date_fin.setValue(null);
        description.clear();
        selectedFile = null;
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void allerVersAffichage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/afficherOffre.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible de charger la page d'affichage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}