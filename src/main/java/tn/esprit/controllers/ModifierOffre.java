package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Offre;
import tn.esprit.services.offreService;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.SQLException;

public class ModifierOffre {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtImage;

    @FXML
    private TextField txtPrix;

    @FXML
    private DatePicker dateDebut;

    @FXML
    private DatePicker dateFin;

    @FXML
    private TextArea txtDescription;

    @FXML
    private Button btnSave;

    private Offre offreToModify;
    private final offreService offreService = new offreService();
    private TableView<Offre> tableOffres;

    // Méthode pour définir l'offre à modifier et la TableView
    public void setOffre(Offre offre) {
        this.offreToModify = offre;

        // Pré-remplir les champs avec les informations de l'offre
        txtNom.setText(offre.getName());
        txtImage.setText(offre.getImagePath());
        txtPrix.setText(String.valueOf(offre.getPrice()));
        dateDebut.setValue(offre.getStartDate().toLocalDate());
        dateFin.setValue(offre.getEndDate());
        txtDescription.setText(offre.getDescription());
    }

    public void setTableOffres(TableView<Offre> tableOffres) {
        this.tableOffres = tableOffres;
    }

    @FXML
    public void initialize() {
        // Action lors du clic sur le bouton "Enregistrer"
        btnSave.setOnAction(event -> {
            try {
                saveChanges();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder les modifications.");
            }
        });
    }

    private void saveChanges() throws SQLException {
        // Met à jour l'offre avec les nouvelles valeurs
        offreToModify.setName(txtNom.getText());
        offreToModify.setImagePath(txtImage.getText());
        offreToModify.setPrice(Double.parseDouble(txtPrix.getText()));
        offreToModify.setStartDate(dateDebut.getValue().atStartOfDay()); // Convert LocalDate → LocalDateTime
        offreToModify.setEndDate(dateFin.getValue()); // Ici c’est OK car endDate est LocalDate
        offreToModify.setEndDate(dateFin.getValue());
        offreToModify.setDescription(txtDescription.getText());

        // Appelle la méthode pour modifier l'offre dans la base de données
        offreToModify.setStartDate(dateDebut.getValue().atStartOfDay());

        // Mettre à jour la ligne dans le tableau
        updateTableRow();

        // Fermer la fenêtre après la modification
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre modifiée avec succès !");
    }

    // Méthode pour mettre à jour la ligne dans le tableau
    private void updateTableRow() {
        // Rechercher l'indice de l'offre modifiée dans le tableau
        for (Offre offre : tableOffres.getItems()) {
            if (offre.getId() == offreToModify.getId()) {
                // Mettre à jour les données de la ligne avec les nouvelles valeurs
                offre.setName(offreToModify.getName());
                offre.setImagePath(offreToModify.getImagePath());
                offre.setPrice(offreToModify.getPrice());
                offre.setStartDate(offreToModify.getStartDate());
                offre.setEndDate(offreToModify.getEndDate());
                offre.setDescription(offreToModify.getDescription());

                // Rafraîchir la ligne du tableau
                tableOffres.refresh();
                break;
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}