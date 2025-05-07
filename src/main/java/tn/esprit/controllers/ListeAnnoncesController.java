package tn.esprit.controllers;

import tn.esprit.entities.Annonce;
import tn.esprit.entities.Evenement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.services.AnnonceService;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class ListeAnnoncesController {
    @FXML private TableView<Annonce> annoncesTable;
    @FXML private TableColumn<Annonce, String> titreColumn;
    @FXML private TableColumn<Annonce, String> descriptionColumn;
    @FXML private TableColumn<Annonce, LocalDateTime> dateColumn;
    @FXML private Button deleteButton;
    @FXML private Button editButton;
    @FXML private TableColumn<Annonce, String> imageColumn;

    private Evenement evenement;
    private final AnnonceService annonceService = new AnnonceService();
    private final ObservableList<Annonce> annoncesList = FXCollections.observableArrayList();

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        chargerAnnonces();
    }

    @FXML
    private void initialize() {
        titreColumn.setCellValueFactory(cellData -> cellData.getValue().titreAProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionAProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateAProperty());

        // Configuration de la colonne d'image
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().imageUrlProperty());
        imageColumn.setCellFactory(param -> new TableCell<Annonce, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);

                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    try {
                        // Définir une taille fixe pour l'aperçu dans la table
                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);
                        imageView.setPreserveRatio(true);

                        // Charger l'image à partir du chemin stocké
                        File file = new File(imagePath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString());
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            // Si l'image n'existe pas, afficher un placeholder ou message
                            setGraphic(null);
                            setText("Image non trouvée");
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("Erreur");
                        e.printStackTrace();
                    }
                }
            }
        });




        // Configuration de la sélection (code existant)
        annoncesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isSelected = newSelection != null;
                    deleteButton.setDisable(!isSelected);
                    editButton.setDisable(!isSelected);
                });
    }

    private void chargerAnnonces() {
        try {
            annoncesList.setAll(annonceService.getAnnoncesByEvenement(evenement.getId()));
            annoncesTable.setItems(annoncesList);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des annonces", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void voirImageEnDetail() {
        Annonce selectedAnnonce = annoncesTable.getSelectionModel().getSelectedItem();
        if (selectedAnnonce != null && selectedAnnonce.getImageUrl() != null) {
            try {
                // Créer une nouvelle fenêtre pour afficher l'image
                Stage imageStage = new Stage();
                imageStage.setTitle("Image: " + selectedAnnonce.getTitreA());

                // Créer une vue d'image
                ImageView imageView = new ImageView();

                // Charger l'image depuis le chemin stocké
                File imageFile = new File(selectedAnnonce.getImageUrl());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);

                    // Définir les propriétés d'affichage de l'image
                    imageView.setPreserveRatio(true);

                    // Limitation de la taille de l'image à afficher
                    double maxWidth = 800;
                    double maxHeight = 600;
                    imageView.setFitWidth(maxWidth);
                    imageView.setFitHeight(maxHeight);

                    // Créer un ScrollPane pour permettre le défilement si l'image est grande
                    ScrollPane scrollPane = new ScrollPane();
                    scrollPane.setContent(imageView);
                    scrollPane.setPannable(true); // Permet de faire glisser l'image
                    scrollPane.setFitToWidth(true);
                    scrollPane.setFitToHeight(true);

                    // Créer la scène et afficher la fenêtre
                    Scene scene = new Scene(scrollPane, Math.min(image.getWidth(), maxWidth),
                            Math.min(image.getHeight(), maxHeight));
                    imageStage.setScene(scene);
                    imageStage.initModality(Modality.APPLICATION_MODAL);
                    imageStage.show();
                } else {
                    showAlert("Erreur", "Image introuvable",
                            "Le fichier image n'existe pas à l'emplacement: " + selectedAnnonce.getImageUrl(),
                            Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Erreur", "Erreur d'affichage",
                        "Impossible d'afficher l'image: " + e.getMessage(),
                        Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleDeleteAnnonce() {
        Annonce selectedAnnonce = annoncesTable.getSelectionModel().getSelectedItem();
        if (selectedAnnonce != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText("Supprimer l'annonce");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette annonce ?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (annonceService.supprimer(selectedAnnonce.getId())) {
                        annoncesList.remove(selectedAnnonce);
                        showAlert("Succès", "Annonce supprimée", "L'annonce a été supprimée avec succès.", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Erreur", "Échec de suppression", "La suppression de l'annonce a échoué.", Alert.AlertType.ERROR);
                    }
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression", e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void handleEditAnnonce() {
        Annonce selectedAnnonce = annoncesTable.getSelectionModel().getSelectedItem();
        if (selectedAnnonce != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ModifierAnnonce.fxml"));
                Parent root = loader.load();

                ModifierAnnonceController controller = loader.getController();
                controller.setAnnonce(selectedAnnonce);
                controller.setListeAnnoncesController(this);

                Stage stage = new Stage();
                stage.setTitle("Modifier l'annonce");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                showAlert("Erreur", "Erreur d'ouverture", "Impossible d'ouvrir la fenêtre de modification.", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public void refreshAnnonces() {
        chargerAnnonces();
    }

    @FXML
    public void closeWindow(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
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