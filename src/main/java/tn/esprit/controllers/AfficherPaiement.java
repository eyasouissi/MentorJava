package tn.esprit.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;
import tn.esprit.entities.Paiement;
import tn.esprit.services.paiementService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherPaiement {

    @FXML
    private TableView<Paiement> tablePaiements;

    @FXML
    private TableColumn<Paiement, String> colUser;

    @FXML
    private TableColumn<Paiement, String> colOffre;

    @FXML
    private TableColumn<Paiement, String> colDate;

    @FXML
    private TableColumn<Paiement, Void> colAction;

    @FXML
    private Button btnRetourOffres, btnExporterPDF, btnVoirStatistiques;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    private final paiementService paiementService = new paiementService();
    private ObservableList<Paiement> paiementsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUser.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUser() != null ? cellData.getValue().getUser().getName() : "N/A"));
        colOffre.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getOffre() != null ? cellData.getValue().getOffre().getName() : "N/A"));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPaymentDate() != null ? cellData.getValue().getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""
        ));

        chargerPaiements();
        ajouterBoutonsActions();

        sortComboBox.getItems().addAll("Date croissante", "Date décroissante");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltrageEtTri());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltrageEtTri());
    }

    private void chargerPaiements() {
        try {
            List<Paiement> paiements = paiementService.recuperer();
            paiementsList.setAll(paiements);
            tablePaiements.setItems(paiementsList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des paiements.");
        }
    }

    private void appliquerFiltrageEtTri() {
        String recherche = searchField.getText().toLowerCase();
        String critereTri = sortComboBox.getValue();

        List<Paiement> resultat = paiementsList.stream()
                .filter(paiement -> paiement.getUser() != null &&
                        paiement.getUser().getName().toLowerCase().contains(recherche))
                .collect(Collectors.toList());

        if (critereTri != null) {
            switch (critereTri) {
                case "Date croissante" -> resultat.sort((p1, p2) -> p1.getPaymentDate().compareTo(p2.getPaymentDate()));
                case "Date décroissante" -> resultat.sort((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()));
            }
        }

        tablePaiements.getItems().setAll(resultat);
    }

    private void ajouterBoutonsActions() {
        colAction.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Paiement, Void> call(final TableColumn<Paiement, Void> param) {
                return new TableCell<>() {
                    private final Button deleteBtn = new Button("🗑 Supprimer");

                    {
                        deleteBtn.getStyleClass().add("supprimer-btn");
                        deleteBtn.setOnAction(event -> {
                            Paiement paiement = getTableView().getItems().get(getIndex());
                            try {
                                paiementService.supprimer(paiement);
                                getTableView().getItems().remove(paiement);
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Paiement supprimé !");
                            } catch (SQLException e) {
                                e.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le paiement.");
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(new HBox(10, deleteBtn));
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void allerVersOffres() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/afficherOffre.fxml"));
            AnchorPane newPage = loader.load();
            Stage currentStage = (Stage) btnRetourOffres.getScene().getWindow();
            Scene newScene = new Scene(newPage);
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du changement de vue.");
        }
    }

    @FXML
    private void allerVersStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/PaiementStatistiques.fxml"));
            BorderPane newPage = loader.load();
            Stage currentStage = (Stage) btnVoirStatistiques.getScene().getWindow();
            Scene newScene = new Scene(newPage);
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la vue statistiques.");
        }
    }

    @FXML
    private void exporterPDF() {
        Document document = new Document();
        try {
            String userHome = System.getProperty("user.home");
            String downloadsPath = userHome + "/Downloads/liste_paiements.pdf";

            PdfWriter.getInstance(document, new FileOutputStream(downloadsPath));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.MAGENTA);
            Paragraph title = new Paragraph("Liste des Paiements\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);

            PdfPCell cell;

            cell = new PdfPCell(new Phrase("Utilisateur"));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Offre"));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Date de Paiement"));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);

            for (Paiement paiement : tablePaiements.getItems()) {
                table.addCell(paiement.getUser() != null ? paiement.getUser().getName() : "N/A");
                table.addCell(paiement.getOffre() != null ? paiement.getOffre().getName() : "N/A");
                table.addCell(paiement.getPaymentDate() != null ? paiement.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            }

            document.add(table);
            document.close();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le fichier PDF a été enregistré dans Téléchargements !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du PDF.");
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