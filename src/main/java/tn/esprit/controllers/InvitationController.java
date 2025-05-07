package tn.esprit.controllers;

import tn.esprit.entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.services.EvenementService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class InvitationController {
    @FXML private Label titreLabel;
    @FXML private Label dateLabel;
    @FXML private Label lieuLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView imageEventView;

    private final EvenementService evenementService = new EvenementService();
    private Evenement evenement;

    public void initialize(URL location, ResourceBundle resources) {
        // Style par défaut
        imageEventView.setFitWidth(300);
        imageEventView.setPreserveRatio(true);
        imageEventView.setSmooth(true);
        imageEventView.setCache(true);
    }

    public void loadEvenementData(Evenement event) {
        this.evenement = event;
        updateUI();
    }

    public void loadEvenementFromId(int eventId) {
        try {
            this.evenement = evenementService.getEvenementById(eventId);
            updateUI();
        } catch (Exception e) {
            showError("Événement introuvable", "L'événement demandé n'existe pas ou n'est pas accessible.");
        }
    }

    private void updateUI() {
        if (evenement == null) return;

        titreLabel.setText(evenement.getTitreE());
        dateLabel.setText(formatDateTime(evenement.getDateDebut()));
        lieuLabel.setText(evenement.getLieu() != null ? evenement.getLieu() : "Lieu non spécifié");
        descriptionLabel.setText(evenement.getDescriptionE());

        // Charger l'image
        if (evenement.getImageE() != null && !evenement.getImageE().isEmpty()) {
            try {
                Image image = new Image("file:" + evenement.getImageE());
                imageEventView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur de chargement de l'image: " + e.getMessage());
                loadDefaultImage();
            }
        } else {
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-event.png"));
            imageEventView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Erreur de chargement de l'image par défaut");
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "Date non spécifiée";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH'h'mm", Locale.FRENCH);
        return dateTime.format(formatter);
    }

    private void showError(String title, String message) {
        titreLabel.setText(title);
        descriptionLabel.setText(message);
        lieuLabel.setText("");
        dateLabel.setText("");
    }

    @FXML
    private void handleAddToCalendar() {
        // Implémentez l'ajout au calendrier ici
        // Utilisez Desktop pour les liens calendar:// ou créez un fichier .ics
    }

    @FXML
    private void handleShareEvent() {
        // Implémentez le partage sur les réseaux sociaux
    }
}