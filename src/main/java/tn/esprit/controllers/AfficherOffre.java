package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.entities.Offre;
import tn.esprit.services.offreService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class AfficherOffre {

    @FXML
    private TableView<Offre> tableOffres;

    @FXML
    private TableColumn<Offre, String> colNom;

    @FXML
    private TableColumn<Offre, String> colImage;

    @FXML
    private TableColumn<Offre, Double> colPrix;

    @FXML
    private TableColumn<Offre, String> colDateDebut;

    @FXML
    private TableColumn<Offre, String> colDateFin;

    @FXML
    private TableColumn<Offre, String> colDescription;

    @FXML
    private TableColumn<Offre, Void> colAction;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private TextField searchField;

    private final offreService offreService = new offreService();
    private List<Offre> toutesLesOffres;

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("name"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("price"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        try {
            toutesLesOffres = offreService.recuperer();
            tableOffres.getItems().setAll(toutesLesOffres);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des offres.");
        }

        sortComboBox.getItems().addAll("Nom (A-Z)", "Prix croissant", "Prix décroissant");

        // Recherche dynamique
        searchField.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltrageEtTri());

        // Tri dynamique
        sortComboBox.setOnAction(event -> appliquerFiltrageEtTri());

        ajouterBoutonsActions();
    }

    private void appliquerFiltrageEtTri() {
        String recherche = searchField.getText().toLowerCase();
        String critereTri = sortComboBox.getValue();

        List<Offre> resultat = toutesLesOffres.stream()
                .filter(offre -> offre.getName().toLowerCase().contains(recherche))
                .toList();

        if (critereTri != null) {
            switch (critereTri) {
                case "Prix croissant" -> resultat = resultat.stream()
                        .sorted(Comparator.comparingDouble(Offre::getPrice)).toList();
                case "Prix décroissant" -> resultat = resultat.stream()
                        .sorted(Comparator.comparingDouble(Offre::getPrice).reversed()).toList();
                case "Nom (A-Z)" -> resultat = resultat.stream()
                        .sorted(Comparator.comparing(Offre::getName, String.CASE_INSENSITIVE_ORDER)).toList();
            }
        }

        tableOffres.getItems().setAll(resultat);
    }

    private void ajouterBoutonsActions() {
        colAction.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Offre, Void> call(final TableColumn<Offre, Void> param) {
                return new TableCell<>() {
                    private final Button modifierBtn = new Button("✏ Modify");
                    private final Button deleteBtn = new Button("🗑 Delete");

                    {
                        // Style pour le bouton Modifier (jaune), agrandi
                        modifierBtn.setStyle("-fx-background-color: #a287d6; -fx-text-fill: black; -fx-font-size: 12px; -fx-padding: 5 10;");
                        modifierBtn.setOnAction(event -> {
                            Offre offre = getTableView().getItems().get(getIndex());
                            openModifierOffreWindow(offre);
                        });

                        // Style pour le bouton Supprimer (rouge), agrandi
                        deleteBtn.setStyle("-fx-background-color: #f49ca3; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10;");
                        deleteBtn.setOnAction(event -> {
                            Offre offre = getTableView().getItems().get(getIndex());
                            try {
                                offreService.supprimer(offre);
                                toutesLesOffres.remove(offre);
                                appliquerFiltrageEtTri();
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre supprimée !");
                            } catch (SQLException e) {
                                e.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'offre.");
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(10, modifierBtn, deleteBtn);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }

    private void openModifierOffreWindow(Offre offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ModifierOffre.fxml"));
            Parent root = loader.load();

            ModifierOffre modifierOffreController = loader.getController();
            modifierOffreController.setOffre(offre);
            modifierOffreController.setTableOffres(tableOffres);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Modify Offre");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification.");
        }
    }

    @FXML
    private void allerVersAjout() {
        try {
            // Assurez-vous que ce chemin correspond à l'emplacement réel de votre fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/offre.fxml"));
            Parent root = loader.load();

            // Créez une nouvelle scène avec la racine chargée
            Scene scene = new Scene(root);

            // Obtenez la fenêtre actuelle et changez sa scène
            Stage stage = (Stage) tableOffres.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Add Offre");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page d'ajout !");
        }
    }
    @FXML
    private void allerVersPaiement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AfficherPaiement.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) tableOffres.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Payment List");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des paiements !");
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
