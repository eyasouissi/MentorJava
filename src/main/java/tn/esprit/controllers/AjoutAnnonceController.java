package tn.esprit.controllers;

import tn.esprit.entities.Annonce;
import tn.esprit.entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.services.AnnonceService;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class AjoutAnnonceController {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField imageUrlField;

    private Evenement evenement;
    private final AnnonceService annonceService = new AnnonceService();

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    // Méthode pour ouvrir le FileChooser et sélectionner une image
    @FXML
    private void choisirImage() {
        // Créer un FileChooser
        FileChooser fileChooser = new FileChooser();

        // Définir les filtres pour n'accepter que les images
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        // Ouvrir la boîte de dialogue pour choisir un fichier
        File file = fileChooser.showOpenDialog(null);

        // Si un fichier est sélectionné, mettre à jour le champ imageUrlField avec son chemin
        if (file != null) {
            imageUrlField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void enregistrer() {
        try {
            // Validation des champs obligatoires
            if (titreField.getText().isEmpty() || datePicker.getValue() == null || imageUrlField.getText().isEmpty()) {
                showAlert("Erreur", "Le titre, la date et l'URL de l'image sont obligatoires");
                return;
            }

            // Vérifier si l'URL de l'image est valide
            String imageUrl = imageUrlField.getText();
            if (!isValidImageUrl(imageUrl)) {
                showAlert("Erreur", "L'URL de l'image n'est pas valide");
                return;
            }

            // Gestion de l'heure (valeur par défaut si vide)
            LocalTime heure = heureField.getText().isEmpty()
                    ? LocalTime.MIDNIGHT
                    : LocalTime.parse(heureField.getText());

            LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), heure);

            // Création de l'annonce
            Annonce annonce = new Annonce(
                    titreField.getText(),
                    descriptionField.getText(),
                    dateTime,
                    imageUrl, // Champ image_url
                    evenement.getId()
            );

            // Insertion dans la base
            annonceService.addAnnonce(annonce);

            showAlert("Succès", "Annonce ajoutée avec succès");
            closeWindow();

        } catch (SQLException e) {
            showAlert("Erreur BD", "Échec de l'ajout: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour valider l'URL de l'image (ex: vérifier les extensions valides)
    private boolean isValidImageUrl(String url) {
        // Liste des extensions valides pour les images
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

        // Vérifie si l'URL se termine par une des extensions valides
        for (String ext : validExtensions) {
            if (url.toLowerCase().endsWith(ext)) {
                return true;
            }
        }

        return false;
    }
}
