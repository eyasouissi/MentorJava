package tn.esprit.controllers;

import tn.esprit.entities.Annonce;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.services.AnnonceService;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ModifierAnnonceController {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Annonce annonce;
    private ListeAnnoncesController listeAnnoncesController;
    private final AnnonceService annonceService = new AnnonceService();

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
        titreField.setText(annonce.getTitreA());
        descriptionArea.setText(annonce.getDescriptionA());
    }

    public void setListeAnnoncesController(ListeAnnoncesController controller) {
        this.listeAnnoncesController = controller;
    }

    @FXML
    private void handleSave() {
        annonce.setTitreA(titreField.getText());
        annonce.setDescriptionA(descriptionArea.getText());
        annonce.setDateA(LocalDateTime.now());

        try {
            if (annonceService.modifier(annonce)) {
                listeAnnoncesController.refreshAnnonces();
                closeWindow();
            } else {
                showAlert("Erreur", "Échec de modification", "La modification de l'annonce a échoué.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}