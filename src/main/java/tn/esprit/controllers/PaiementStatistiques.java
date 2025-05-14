package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.esprit.entities.Paiement;
import tn.esprit.services.paiementService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PaiementStatistiques {

    @FXML
    private PieChart pieChartOffres;

    @FXML
    private LineChart<String, Number> lineChartTemps;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private BarChart<String, Number> barChartUsers;

    @FXML
    private CategoryAxis xAxisUsers;

    @FXML
    private NumberAxis yAxisUsers;

    @FXML
    private Button btnExporterStats, btnRetourPaiements;

    private final paiementService paiementService = new paiementService();
    private List<Paiement> paiements;

    @FXML
    public void initialize() {
        try {
            // Charger tous les paiements
            paiements = paiementService.recuperer();

            // Initialiser tous les graphiques
            initialiserPieChartOffres();
            initialiserLineChartTemps();
            initialiserBarChartUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des données de paiement.");
        }
    }

    private void initialiserPieChartOffres() {
        // Regrouper les paiements par type d'offre et compter
        Map<String, Long> offreFrequency = paiements.stream()
                .filter(p -> p.getOffre() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getOffre().getName(),
                        Collectors.counting()
                ));

        // Créer les données pour le PieChart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        offreFrequency.forEach((offre, count) ->
                pieChartData.add(new PieChart.Data(offre + " (" + count + ")", count))
        );

        // Appliquer les données au graphique
        pieChartOffres.setData(pieChartData);
        pieChartOffres.setTitle("Distribution par Offre");

        // Appliquer un style pour chaque section
        applyCustomColorsToChart();
    }

    private void initialiserLineChartTemps() {
        // Préparer les séries pour le LineChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de Paiements");

        // Regrouper les paiements par mois sur les 6 derniers mois
        Map<YearMonth, Long> paiementsParMois = paiements.stream()
                .filter(p -> p.getPaymentDate() != null)
                .collect(Collectors.groupingBy(
                        p -> YearMonth.from(p.getPaymentDate()),
                        Collectors.counting()
                ));

        // Trier par date et limiter aux 6 derniers mois
        List<YearMonth> derniersMois = paiementsParMois.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        // Si nous avons plus de 6 mois de données, prenons juste les 6 derniers
        if (derniersMois.size() > 6) {
            derniersMois = derniersMois.subList(derniersMois.size() - 6, derniersMois.size());
        }

        // Ajouter les données à la série
        for (YearMonth mois : derniersMois) {
            String moisStr = mois.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            Long count = paiementsParMois.getOrDefault(mois, 0L);
            series.getData().add(new XYChart.Data<>(moisStr, count));
        }

        // Appliquer les données au graphique
        lineChartTemps.getData().add(series);
        lineChartTemps.setTitle("Évolution des Paiements");
    }

    private void initialiserBarChartUsers() {
        // Regrouper les paiements par utilisateur et compter
        Map<String, Long> userFrequency = paiements.stream()
                .filter(p -> p.getUser() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getUser().getName(),
                        Collectors.counting()
                ));

        // Trier par nombre de paiements (décroissant) et limiter aux 5 premiers
        List<Map.Entry<String, Long>> topUsers = userFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Préparer les séries pour le BarChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de Paiements");

        // Ajouter les données à la série
        for (Map.Entry<String, Long> entry : topUsers) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Appliquer les données au graphique
        barChartUsers.getData().add(series);
        barChartUsers.setTitle("Top 5 des Utilisateurs");
    }

    private void applyCustomColorsToChart() {
        // On peut appliquer des couleurs personnalisées aux sections du PieChart si désiré
        String[] pieColors = {
                "rgba(66, 133, 244, 0.8)",
                "rgba(219, 68, 55, 0.8)",
                "rgba(244, 180, 0, 0.8)",
                "rgba(15, 157, 88, 0.8)",
                "rgba(171, 71, 188, 0.8)",
                "rgba(0, 172, 193, 0.8)"
        };

        int i = 0;
        for (PieChart.Data data : pieChartOffres.getData()) {
            String color = pieColors[i % pieColors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
    }

    @FXML
    private void allerVersPaiements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/afficherPaiement.fxml"));
            AnchorPane newPage = loader.load();
            Stage currentStage = (Stage) btnRetourPaiements.getScene().getWindow();
            Scene newScene = new Scene(newPage);
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du changement de vue.");
        }
    }

    @FXML
    private void exporterStatistiques() {
        // Cette méthode pourrait être implémentée pour exporter les statistiques en PDF ou autre format
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité d'exportation des statistiques à implémenter.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}