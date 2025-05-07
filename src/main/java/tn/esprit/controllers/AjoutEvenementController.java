package tn.esprit.controllers;

import tn.esprit.entities.Evenement;
import tn.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

public class AjoutEvenementController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private ComboBox<Integer> heureDebutCombo;
    @FXML private ComboBox<Integer> minuteDebutCombo;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<Integer> heureFinCombo;
    @FXML private ComboBox<Integer> minuteFinCombo;
    @FXML private TextField lieuField;
    @FXML private TextField imagePathField;

    // Labels d'erreur
    @FXML private Label titreError;
    @FXML private Label descriptionError;
    @FXML private Label dateError;
    @FXML private Label lieuError;
    @FXML private Label imageError;

    private Evenement evenement;
    private final EvenementService evenementService = new EvenementService();

    @FXML
    public void initialize() {
        // Remplir les ComboBox des heures et minutes
        heureDebutCombo.getItems().addAll(IntStream.range(0, 24).boxed().toArray(Integer[]::new));
        minuteDebutCombo.getItems().addAll(IntStream.range(0, 60).boxed().toArray(Integer[]::new));
        heureFinCombo.getItems().addAll(IntStream.range(0, 24).boxed().toArray(Integer[]::new));
        minuteFinCombo.getItems().addAll(IntStream.range(0, 60).boxed().toArray(Integer[]::new));

        // Valeurs par défaut
        heureDebutCombo.getSelectionModel().select(12);
        minuteDebutCombo.getSelectionModel().select(0);
        heureFinCombo.getSelectionModel().select(13);
        minuteFinCombo.getSelectionModel().select(0);
        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now());

        // Validation en temps réel
        titreField.textProperty().addListener((obs, oldVal, newVal) -> validateTitre());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescription());
        lieuField.textProperty().addListener((obs, oldVal, newVal) -> validateLieu());

        // Validation des dates
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        heureDebutCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        minuteDebutCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        heureFinCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        minuteFinCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        if (evenement != null) {
            titreField.setText(evenement.getTitreE());
            descriptionField.setText(evenement.getDescriptionE() != null ? evenement.getDescriptionE() : "");

            LocalDateTime dateDebut = evenement.getDateDebut();
            LocalDateTime dateFin = evenement.getDateFin();

            dateDebutPicker.setValue(dateDebut.toLocalDate());
            heureDebutCombo.setValue(dateDebut.getHour());
            minuteDebutCombo.setValue(dateDebut.getMinute());

            dateFinPicker.setValue(dateFin.toLocalDate());
            heureFinCombo.setValue(dateFin.getHour());
            minuteFinCombo.setValue(dateFin.getMinute());

            lieuField.setText(evenement.getLieu());
            imagePathField.setText(evenement.getImageE());
        }
    }

    @FXML
    private void enregistrer() {
        resetErrorMessages();

        boolean isValid = true;

        // Validation du titre
        if (!validateTitre()) {
            isValid = false;
        }

        // Validation de la description
        if (!validateDescription()) {
            isValid = false;
        }

        // Validation des dates
        if (!validateDates()) {
            isValid = false;
        }

        // Validation du lieu
        if (!validateLieu()) {
            isValid = false;
        }

        // Validation de l'image
        if (!validateImage()) {
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        try {
            LocalDateTime dateDebut = LocalDateTime.of(
                    dateDebutPicker.getValue(),
                    LocalTime.of(heureDebutCombo.getValue(), minuteDebutCombo.getValue())
            );

            LocalDateTime dateFin = LocalDateTime.of(
                    dateFinPicker.getValue(),
                    LocalTime.of(heureFinCombo.getValue(), minuteFinCombo.getValue())
            );

            if (this.evenement != null) {
                // Modification
                this.evenement.setTitreE(titreField.getText());
                this.evenement.setDescriptionE(descriptionField.getText());
                this.evenement.setDateDebut(dateDebut);
                this.evenement.setDateFin(dateFin);
                this.evenement.setLieu(lieuField.getText());
                this.evenement.setImageE(imagePathField.getText());

                evenementService.modifier(this.evenement);
                showAlert("Succès", "Événement modifié avec succès");
            } else {
                // Création
                Evenement newEvenement = new Evenement(
                        titreField.getText(),
                        descriptionField.getText(),
                        dateDebut,
                        dateFin,
                        imagePathField.getText(),
                        lieuField.getText(),
                        1 // user_id par défaut
                );

                evenementService.addEvenement(newEvenement);
                showAlert("Succès", "Événement ajouté avec succès");
            }

            closeWindow();
        } catch (SQLException | IOException e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthodes de validation
    private boolean validateTitre() {
        if (titreField.getText().isEmpty()) {
            showError(titreError, "Le titre est obligatoire");
            return false;
        } else if (titreField.getText().length() < 3) {
            showError(titreError, "Le titre doit contenir au moins 3 caractères");
            return false;
        }
        titreError.setVisible(false);
        return true;
    }

    private boolean validateDescription() {
        if (descriptionField.getText().isEmpty()) {
            showError(descriptionError, "La description est obligatoire");
            return false;
        } else if (descriptionField.getText().length() < 10) {
            showError(descriptionError, "La description doit contenir au moins 10 caractères");
            return false;
        }
        descriptionError.setVisible(false);
        return true;
    }

    private boolean validateDates() {
        if (dateDebutPicker.getValue() == null || heureDebutCombo.getValue() == null ||
                minuteDebutCombo.getValue() == null) {
            showError(dateError, "La date et heure de début sont obligatoires");
            return false;
        }

        if (dateFinPicker.getValue() == null || heureFinCombo.getValue() == null ||
                minuteFinCombo.getValue() == null) {
            showError(dateError, "La date et heure de fin sont obligatoires");
            return false;
        }

        LocalDateTime dateDebut = LocalDateTime.of(
                dateDebutPicker.getValue(),
                LocalTime.of(heureDebutCombo.getValue(), minuteDebutCombo.getValue())
        );

        LocalDateTime dateFin = LocalDateTime.of(
                dateFinPicker.getValue(),
                LocalTime.of(heureFinCombo.getValue(), minuteFinCombo.getValue())
        );

        if (dateFin.isBefore(dateDebut)) {
            showError(dateError, "La date de fin doit être après la date de début");
            return false;
        }

        dateError.setVisible(false);
        return true;
    }

    private boolean validateLieu() {
        if (lieuField.getText().isEmpty()) {
            showError(lieuError, "Le lieu est obligatoire");
            return false;
        }
        lieuError.setVisible(false);
        return true;
    }

    private boolean validateImage() {
        if (imagePathField.getText().isEmpty()) {
            showError(imageError, "Une image est obligatoire");
            return false;
        }
        imageError.setVisible(false);
        return true;
    }

    private void resetErrorMessages() {
        titreError.setVisible(false);
        descriptionError.setVisible(false);
        dateError.setVisible(false);
        lieuError.setVisible(false);
        imageError.setVisible(false);
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(titreField.getScene().getWindow());
        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getAbsolutePath());
            imageError.setVisible(false);
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
}