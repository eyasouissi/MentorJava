package tn.esprit.controllers.project;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import tn.esprit.services.project.ProjectService;
import java.util.Map;

public class StatsController {
    @FXML private BarChart<String, Number> difficultyBarChart;
    @FXML private LineChart<String, Number> creationDateLineChart;
    
    private final ProjectService projectService = ProjectService.getInstance();

    @FXML
    public void initialize() {
        loadDifficultyStats();
        loadCreationDateStats();
    }
    
    private void loadDifficultyStats() {
        difficultyBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Difficulté");
        
        // Utilisation directe de la Map retournée par le service
        projectService.countProjectsByDifficulty().forEach((difficulty, count) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(difficulty.toString(), count);
            series.getData().add(data);
            
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    switch(difficulty) {
                        case 1:
                            newNode.setStyle("-fx-bar-fill: #2ecc71;"); // Vert
                            break;
                        case 2:
                            newNode.setStyle("-fx-bar-fill: #f1c40f;"); // Jaune
                            break;
                        case 3:
                            newNode.setStyle("-fx-bar-fill: #e67e22;"); // Orange
                            break;
                        case 4:
                            newNode.setStyle("-fx-bar-fill: #e74c3c;"); // Rouge
                            break;
                    }
                }
            });
        });
        
        difficultyBarChart.getData().add(series);
    }
    
    private void loadCreationDateStats() {
        creationDateLineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Création");
        
        projectService.countProjectsByCreationDate().forEach((date, count) -> {
            series.getData().add(new XYChart.Data<>(date.toString(), count));
        });
        
        creationDateLineChart.getData().add(series);
    }
}