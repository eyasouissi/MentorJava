// DashboardController.java
package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import tn.esprit.services.EvenementService;
import tn.esprit.services.YearWeek;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private Tab dailyTab;
    @FXML private Tab weeklyTab;
    @FXML private Tab monthlyTab;

    private EvenementService evenementService = new EvenementService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupDailyChart();
            setupWeeklyChart();
            setupMonthlyChart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDailyChart() throws Exception {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> dailyChart = new BarChart<>(xAxis, yAxis);
        dailyChart.setTitle("Création d'événements par jour");
        xAxis.setLabel("Date");
        yAxis.setLabel("Nombre d'événements");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Événements");

        Map<LocalDate, Long> dailyStats = evenementService.getEventsCountByDay();
        dailyStats.forEach((date, count) -> {
            series.getData().add(new XYChart.Data<>(date.format(DateTimeFormatter.ISO_LOCAL_DATE), count));
        });

        dailyChart.getData().add(series);
        dailyTab.setContent(dailyChart);
    }

    private void setupWeeklyChart() throws Exception {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> weeklyChart = new BarChart<>(xAxis, yAxis);
        weeklyChart.setTitle("Création d'événements par semaine");
        xAxis.setLabel("Semaine");
        yAxis.setLabel("Nombre d'événements");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Événements");

        Map<YearWeek, Long> weeklyStats = evenementService.getEventsCountByWeek();
        weeklyStats.forEach((yearWeek, count) -> {
            series.getData().add(new XYChart.Data<>(yearWeek.toString(), count));
        });

        weeklyChart.getData().add(series);
        weeklyTab.setContent(weeklyChart);
    }

    private void setupMonthlyChart() throws Exception {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> monthlyChart = new BarChart<>(xAxis, yAxis);
        monthlyChart.setTitle("Création d'événements par mois");
        xAxis.setLabel("Mois");
        yAxis.setLabel("Nombre d'événements");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Événements");

        Map<YearMonth, Long> monthlyStats = evenementService.getEventsCountByMonth();
        monthlyStats.forEach((yearMonth, count) -> {
            series.getData().add(new XYChart.Data<>(yearMonth.toString(), count));
        });

        monthlyChart.getData().add(series);
        monthlyTab.setContent(monthlyChart);
    }
}