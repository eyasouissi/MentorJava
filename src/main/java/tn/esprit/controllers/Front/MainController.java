package tn.esprit.controllers.Front;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private VBox dashboardView;
    @FXML private VBox coursesView;
    @FXML private VBox eventsView;
    @FXML private VBox messagesView;
    @FXML private VBox settingsView;
    @FXML private FlowPane coursesGrid;
    @FXML private StackPane contentPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Afficher le dashboard par défaut
        setActiveView(dashboardView);
    }

    // Méthodes de navigation
    @FXML
    private void showDashboard() {
        setActiveView(dashboardView);
        updateActiveButton("dashboardBtn");
    }

    @FXML
    private void showCourses() {
        setActiveView(coursesView);
        updateActiveButton("coursesBtn");
        loadCourses(); // À implémenter
    }

    @FXML
    private void showEvents() {
        setActiveView(eventsView);
        updateActiveButton("eventsBtn");
        loadEvents(); // À implémenter
    }

    @FXML
    private void showMessages() {
        setActiveView(messagesView);
        updateActiveButton("messagesBtn");
        loadMessages(); // À implémenter
    }

    @FXML
    private void showSettings() {
        setActiveView(settingsView);
        updateActiveButton("settingsBtn");
        loadSettings(); // À implémenter
    }

    private void setActiveView(Node view) {
        contentPane.getChildren().forEach(node -> node.setVisible(node == view));
    }

    private void updateActiveButton(String buttonId) {
        contentPane.lookupAll(".nav-button").forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("active");
                if (node.getId() != null && node.getId().equals(buttonId)) {
                    node.getStyleClass().add("active");
                }
            }
        });
    }

    private void loadCourses() {
        // Implémentez le chargement des cours
        coursesGrid.getChildren().clear();
        // Exemple :
        // coursesGrid.getChildren().add(new Label("Cours 1"));
    }

    private void loadEvents() {
        // Implémentez le chargement des événements
    }

    private void loadMessages() {
        // Implémentez le chargement des messages
    }

    private void loadSettings() {
        // Implémentez le chargement des paramètres
    }
}