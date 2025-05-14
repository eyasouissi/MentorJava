package tn.esprit.controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.util.Duration;
import tn.esprit.entities.Evenement;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.services.EvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class FrontCalendarController {

    @FXML private VBox calendarContainer;
    @FXML private HBox monthHeader;
    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;

    private EvenementService evenementService = new EvenementService();
    private YearMonth currentYearMonth = YearMonth.now();

    @FXML
    public void initialize() {
        loadCalendar();
    }

    private void loadCalendar() {
        String month = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthYearLabel.setText(month + " " + currentYearMonth.getYear());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();

        calendarGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        try {
            List<Evenement> events = evenementService.getEvenementsForMonth(
                    currentYearMonth.getMonthValue(),
                    currentYearMonth.getYear()
            );

            int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1;

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = currentYearMonth.atDay(day);
                int row = (dayOfWeek / 7) + 1;
                int col = dayOfWeek % 7;

                StackPane cell = new StackPane();
                cell.getStyleClass().add("calendar-cell");
                if (date.equals(LocalDate.now())) {
                    cell.getStyleClass().add("today");
                }

                Label dayLabel = new Label(String.valueOf(day));
                dayLabel.getStyleClass().add("day-number");
                cell.getChildren().add(dayLabel);
                StackPane.setAlignment(dayLabel, Pos.TOP_LEFT);

                VBox eventsContainer = new VBox(2);
                eventsContainer.getStyleClass().add("events-container");
                eventsContainer.setMaxWidth(Double.MAX_VALUE);

                for (Evenement event : events) {
                    if (event.getDateDebut().toLocalDate().equals(date)) {
                        Button eventButton = createEventButton(event);
                        eventsContainer.getChildren().add(eventButton);
                    }
                }

                if (!eventsContainer.getChildren().isEmpty()) {
                    cell.getChildren().add(eventsContainer);
                    StackPane.setAlignment(eventsContainer, Pos.BOTTOM_CENTER);
                }

                calendarGrid.add(cell, col, row);
                dayOfWeek++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Button createEventButton(Evenement event) {
        Button eventButton = new Button(event.getTitreE());
        eventButton.getStyleClass().add("event-button");
        String[] colors = {"#4285F4", "#EA4335", "#FBBC05", "#34A853", "#673AB7"};
        String color = colors[Math.abs(event.getTitreE().hashCode()) % colors.length];
        eventButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        eventButton.setMaxWidth(Double.MAX_VALUE);
        eventButton.setOnAction(e -> showEventDetails(event));
        return eventButton;
    }

    private void showEventDetails(Evenement event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'événement");

        // Empêche l'ouverture dans une nouvelle fenêtre
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(calendarContainer.getScene().getWindow());

        // Créer une boîte de dialogue stylisée
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/calendar.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("event-dialog");
        dialogPane.setPrefWidth(550);
        dialogPane.setPrefHeight(450);

        // En-tête avec le titre de l’événement
        StackPane header = new StackPane();
        header.getStyleClass().add("event-header");
        header.setPadding(new Insets(20, 25, 20, 25));

        Label titleLabel = new Label(event.getTitreE());
        titleLabel.getStyleClass().add("event-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        header.getChildren().add(titleLabel);

        // Corps du dialogue
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.getStyleClass().add("event-content");

        VBox descriptionBox = new VBox(10);
        descriptionBox.getStyleClass().add("info-section");

        Label descHeaderLabel = new Label("Description");
        descHeaderLabel.getStyleClass().add("section-header");

        TextArea descArea = new TextArea(event.getDescriptionE());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.getStyleClass().add("event-description-area");
        descArea.setPrefRowCount(4);

        descriptionBox.getChildren().addAll(descHeaderLabel, descArea);

        VBox detailsBox = new VBox(10);
        detailsBox.getStyleClass().add("info-section");

        Label detailsHeaderLabel = new Label("Informations");
        detailsHeaderLabel.getStyleClass().add("section-header");

        GridPane detailsGrid = new GridPane();
        detailsGrid.getStyleClass().add("details-grid");
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(12);
        detailsGrid.setPadding(new Insets(10));

        addDetailRowWithIcon(detailsGrid, 0, "Date:", event.getDateDebut().toLocalDate().toString());
        addDetailRowWithIcon(detailsGrid, 1, "Heure:", event.getDateDebut().toLocalTime().toString());
        addDetailRowWithIcon(detailsGrid, 2, "Lieu:", event.getLieu());

        detailsBox.getChildren().addAll(detailsHeaderLabel, detailsGrid);

        content.getChildren().addAll(header, new Separator(), descriptionBox, new Separator(), detailsBox);

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        dialog.setOnShown(e -> {
            Button closeButtonNode = (Button) dialog.getDialogPane().lookupButton(closeButton);
            closeButtonNode.getStyleClass().add("close-button");
        });

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }


    // Méthode avec icône (nouvelle version)
    private void addDetailRowWithIcon(GridPane grid, int row, String label, String value) {
        Label iconLabel = new Label("•"); // You can replace with icon if you have icon fonts
        iconLabel.getStyleClass().add("detail-icon");

        Label detailLabel = new Label(label);
        detailLabel.getStyleClass().add("detail-label");

        Label detailValue = new Label(value);
        detailValue.getStyleClass().add("detail-value");
        detailValue.setWrapText(true);

        grid.add(iconLabel, 0, row);
        grid.add(detailLabel, 1, row);
        grid.add(detailValue, 2, row);

        // Make value column grow
        GridPane.setHgrow(detailValue, Priority.ALWAYS);
    }

    // Méthode simple (ancienne version) - gardée pour compatibilité
    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label detailLabel = new Label(label);
        detailLabel.getStyleClass().add("detail-label");

        Label detailValue = new Label(value);
        detailValue.getStyleClass().add("detail-value");

        grid.add(detailLabel, 0, row);
        grid.add(detailValue, 1, row);
    }

    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        loadCalendar();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        loadCalendar();
    }

    @FXML
    private void goToToday() {
        currentYearMonth = YearMonth.now();
        loadCalendar();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void goBackToAnnonces(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Front/AnnonceList.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}