package tn.esprit.controllers;

import tn.esprit.entities.Evenement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.services.EvenementService;
import javafx.geometry.Insets;
import javafx.scene.input.ScrollEvent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ListeEvenementController {

    @FXML
    private Pagination pagination;  // Pagination component

    @FXML
    private TextField searchField;  // Champ de recherche

    @FXML
    private ComboBox<String> sortComboBox;  // ComboBox pour le tri

    @FXML
    private DatePicker dateDebutFilter;  // Filtre de date de début

    @FXML
    private DatePicker dateFinFilter;  // Filtre de date de fin

    @FXML
    private Button clearFiltersButton;  // Bouton pour effacer les filtres

    private final ObservableList<Evenement> masterEventList = FXCollections.observableArrayList();
    private FilteredList<Evenement> filteredEvenements;
    private SortedList<Evenement> sortedEvenements;

    private final EvenementService evenementService = new EvenementService();
    private Evenement evenementSelectionne; // Stocke l'événement sélectionné pour actions ultérieures

    private static final int ROWS_PER_PAGE = 3; // Réduit pour mieux voir les événements par page
    private ScrollPane scrollPane; // To be created dynamically

    // Formatter pour afficher les dates
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        // Initialiser la liste de tri
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Titre (A-Z)",
                "Titre (Z-A)",
                "Date (Récent → Ancien)",
                "Date (Ancien → Récent)",
                "Lieu (A-Z)"
        ));
        sortComboBox.getSelectionModel().selectFirst();

        // Configurer les écouteurs d'événements pour les filtres
        setupEventListeners();

        loadEvenements(); // Charge les événements au démarrage

        // Configurer la pagination pour adapter à la hauteur
        pagination.setPageFactory(this::createEvenementPage);

        // Enable keyboard navigation
        pagination.getParent().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                case PAGE_UP:
                    goToPreviousPage();
                    break;
                case DOWN:
                case PAGE_DOWN:
                    goToNextPage();
                    break;
                default:
                    break;
            }
        });
    }

    private void setupEventListeners() {
        // Écouter les changements dans le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilterPredicate();
        });

        // Écouter les changements dans le combobox de tri
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                applySorting(newValue);
            }
        });

        // Écouter les changements de dates
        dateDebutFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilterPredicate();
        });

        dateFinFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilterPredicate();
        });

        // Configurer le bouton d'effacement des filtres
        clearFiltersButton.setOnAction(e -> clearFilters());
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        dateDebutFilter.setValue(null);
        dateFinFilter.setValue(null);
        sortComboBox.getSelectionModel().selectFirst();
        updateFilterPredicate();
    }

    // Méthode pour charger les événements
    private void loadEvenements() {
        try {
            // Récupérer tous les événements depuis la base de données
            List<Evenement> evenements = evenementService.getAllEvenements();
            masterEventList.setAll(evenements); // Ajoute tous les événements à la liste maître

            // Initialiser la liste filtrée avec tous les événements
            filteredEvenements = new FilteredList<>(masterEventList, p -> true);

            // Initialiser la liste triée basée sur la liste filtrée
            sortedEvenements = new SortedList<>(filteredEvenements);

            // Appliquer le tri initial
            applySorting(sortComboBox.getSelectionModel().getSelectedItem());

            updatePagination();

        } catch (SQLException e) {
            showErrorAlert("Erreur de chargement", e.getMessage());
        }
    }

    private void updateFilterPredicate() {
        filteredEvenements.setPredicate(createCombinedPredicate());
        updatePagination();
    }

    private Predicate<Evenement> createCombinedPredicate() {
        String searchText = searchField.getText().toLowerCase().trim();
        LocalDate dateDebut = dateDebutFilter.getValue();
        LocalDate dateFin = dateFinFilter.getValue();

        return evenement -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    evenement.getTitreE().toLowerCase().contains(searchText) ||
                    evenement.getDescriptionE().toLowerCase().contains(searchText) ||
                    evenement.getLieu().toLowerCase().contains(searchText);

            boolean matchesDateDebut = true;
            boolean matchesDateFin = true;

            try {
                // Assuming getDateDebut() and getDateFin() return LocalDateTime objects
                if (dateDebut != null) {
                    LocalDate eventStartDate = evenement.getDateDebut().toLocalDate();
                    matchesDateDebut = !eventStartDate.isBefore(dateDebut);
                }

                if (dateFin != null) {
                    LocalDate eventEndDate = evenement.getDateFin().toLocalDate();
                    matchesDateFin = !eventEndDate.isAfter(dateFin);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du filtrage des dates: " + e.getMessage());
                // En cas d'erreur, on considère que l'événement correspond aux filtres de date
            }

            return matchesSearch && matchesDateDebut && matchesDateFin;
        };
    }

    private void applySorting(String sortOption) {
        if (sortedEvenements == null) return;

        switch (sortOption) {
            case "Titre (A-Z)":
                sortedEvenements.setComparator((e1, e2) ->
                        e1.getTitreE().compareToIgnoreCase(e2.getTitreE()));
                break;
            case "Titre (Z-A)":
                sortedEvenements.setComparator((e1, e2) ->
                        e2.getTitreE().compareToIgnoreCase(e1.getTitreE()));
                break;
            case "Date (Récent → Ancien)":
                sortedEvenements.setComparator((e1, e2) -> {
                    try {
                        // Direct comparison of LocalDateTime objects
                        return e2.getDateDebut().compareTo(e1.getDateDebut());
                    } catch (Exception e) {
                        System.err.println("Erreur de tri par date: " + e.getMessage());
                        return 0;
                    }
                });
                break;
            case "Date (Ancien → Récent)":
                sortedEvenements.setComparator((e1, e2) -> {
                    try {
                        // Direct comparison of LocalDateTime objects
                        return e1.getDateDebut().compareTo(e2.getDateDebut());
                    } catch (Exception e) {
                        System.err.println("Erreur de tri par date: " + e.getMessage());
                        return 0;
                    }
                });
                break;
            case "Lieu (A-Z)":
                sortedEvenements.setComparator((e1, e2) ->
                        e1.getLieu().compareToIgnoreCase(e2.getLieu()));
                break;
            default:
                sortedEvenements.setComparator(null);
        }

        updatePagination();
    }

    private void updatePagination() {
        // Définir la pagination
        int totalPageCount = (int) Math.ceil((double) sortedEvenements.size() / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(1, totalPageCount)); // Minimum 1 page

        // Si la page actuelle est maintenant invalide, réinitialiser à la première page
        if (pagination.getCurrentPageIndex() >= totalPageCount) {
            pagination.setCurrentPageIndex(0);
        } else {
            // Forcer la mise à jour de la page actuelle
            int currentPage = pagination.getCurrentPageIndex();
            pagination.setCurrentPageIndex(0);
            pagination.setCurrentPageIndex(currentPage);
        }
    }

    // Méthode pour créer une page d'événements en fonction de l'index de la page
    private ScrollPane createEvenementPage(int pageIndex) {
        // Calculer l'indice de début et de fin pour cette page
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, sortedEvenements.size());

        // Créer un nouveau VBox pour afficher les événements de cette page
        VBox pageContent = new VBox();
        pageContent.setSpacing(20);
        pageContent.setPadding(new Insets(10));

        // Ajouter les événements à la page
        if (fromIndex < sortedEvenements.size()) {
            for (int i = fromIndex; i < toIndex; i++) {
                Evenement ev = sortedEvenements.get(i);
                pageContent.getChildren().add(createEvenementCard(ev));
            }
        } else {
            // Si aucun événement à afficher, montrer un message
            Label emptyLabel = new Label("Aucun événement ne correspond à vos critères de recherche.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
            pageContent.getChildren().add(emptyLabel);
        }

        // Créer un ScrollPane pour supporter le défilement vertical
        scrollPane = new ScrollPane();
        scrollPane.setContent(pageContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(500); // Hauteur ajustable

        // Enable smooth scrolling
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() > 0) {
                smoothScroll(scrollPane, -50); // Scroll up
            } else if (event.getDeltaY() < 0) {
                smoothScroll(scrollPane, 50);  // Scroll down
            }
        });

        return scrollPane;
    }

    // Method for smooth scrolling animation
    private void smoothScroll(ScrollPane scrollPane, double deltaY) {
        double newValue = scrollPane.getVvalue() + deltaY / scrollPane.getContent().getBoundsInLocal().getHeight();
        newValue = Math.min(1, Math.max(0, newValue));

        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(scrollPane.vvalueProperty(), newValue);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(150), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @FXML
    private void goToPreviousPage() {
        if (pagination.getCurrentPageIndex() > 0) {
            pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
        }
    }

    @FXML
    private void goToNextPage() {
        if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1) {
            pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
        }
    }

    // Méthode pour créer une carte d'événement
    private HBox createEvenementCard(Evenement ev) {
        // Création d'un conteneur pour l'image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        // Chargement de l'image depuis le chemin stocké dans l'événement
        if (ev.getImageE() != null && !ev.getImageE().isEmpty()) {
            try {
                File imageFile = new File(ev.getImageE());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                } else {
                    // Image par défaut si le fichier n'existe pas
                    try {
                        imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_event.png")));
                    } catch (Exception e) {
                        System.err.println("Image par défaut introuvable: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                try {
                    // Image par défaut en cas d'erreur
                    imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_event.png")));
                } catch (Exception ex) {
                    System.err.println("Image par défaut introuvable: " + ex.getMessage());
                }
            }
        } else {
            try {
                // Image par défaut si aucun chemin n'est spécifié
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_event.png")));
            } catch (Exception e) {
                System.err.println("Image par défaut introuvable: " + e.getMessage());
            }
        }

        // Style pour le conteneur d'image
        VBox imageContainer = new VBox(imageView);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(5));
        imageContainer.setMinWidth(110);
        imageContainer.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-background-radius: 5;");

        // Les informations textuelles
        Label titre = new Label("🎯 Titre : " + ev.getTitreE());
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label description = new Label("📝 Description : " + ev.getDescriptionE());
        description.setWrapText(true);

        // Format dates properly using the formatter
        Label dateDebut = new Label("📅 Début : " + ev.getDateDebut().format(displayFormatter));
        Label dateFin = new Label("📅 Fin : " + ev.getDateFin().format(displayFormatter));
        Label lieu = new Label("📍 Lieu : " + ev.getLieu());

        VBox infoBox = new VBox(titre, description, dateDebut, dateFin, lieu);
        infoBox.setSpacing(5);
        infoBox.setPrefWidth(500);
        HBox.setHgrow(infoBox, Priority.ALWAYS); // Pour que la boîte d'info prenne l'espace disponible

        // Boutons
        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        modifierBtn.setOnAction(e -> openEvenementForm(ev));
        modifierBtn.setPrefWidth(120);

        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        supprimerBtn.setOnAction(e -> supprimerEvenement(ev));
        supprimerBtn.setPrefWidth(120);

        Button ajouterAnnonceBtn = new Button("Ajouter Annonce");
        ajouterAnnonceBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        ajouterAnnonceBtn.setOnAction(e -> {
            evenementSelectionne = ev;
            handleAjouterAnnonce(e);
        });
        ajouterAnnonceBtn.setPrefWidth(120);

        Button voirAnnoncesBtn = new Button("Voir Annonces");
        voirAnnoncesBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        voirAnnoncesBtn.setOnAction(e -> {
            evenementSelectionne = ev;
            handleVoirAnnonces(e);
        });
        voirAnnoncesBtn.setPrefWidth(120);

        VBox buttonBox = new VBox(modifierBtn, supprimerBtn, ajouterAnnonceBtn, voirAnnoncesBtn);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(5));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMinWidth(130);

        // Ajouter le conteneur d'image à la carte d'événement
        HBox card = new HBox(imageContainer, infoBox, buttonBox);
        card.setSpacing(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("event-card"); // Utilise la classe CSS définie
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 10; -fx-background-color: #f8f8f8; -fx-background-radius: 10;");

        return card;
    }

    @FXML
    private void ajouterEvenement(ActionEvent event) {
        openEvenementForm(null); // Appel pour ajouter un nouvel événement
    }

    private void openEvenementForm(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjoutEvenement.fxml"));
            Parent root = loader.load();

            // Pass the evenement to the controller for modification
            AjoutEvenementController controller = loader.getController();
            controller.setEvenement(evenement);

            Stage stage = new Stage();
            stage.setTitle(evenement == null ? "Ajouter un Événement" : "Modifier un Événement");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadEvenements(); // Recharger la liste des événements après modification ou ajout
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerEvenement(Evenement ev) {
        try {
            // Confirmation avant suppression
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText("Supprimer l'événement");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (evenementService.deleteEvenement(ev.getId())) {  // Appel à la méthode de suppression
                    loadEvenements();  // Recharger la liste des événements après suppression
                    showInfoAlert("Succès", "L'événement a été supprimé avec succès.");
                } else {
                    showErrorAlert("Erreur de suppression", "L'événement n'a pas pu être supprimé.");
                }
            }
        } catch (SQLException ex) {
            showErrorAlert("Erreur de suppression", ex.getMessage());
        }
    }

    @FXML
    private void handleAjouterAnnonce(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ajoutAnnonce.fxml"));
            Parent root = loader.load();

            AjoutAnnonceController controller = loader.getController();
            controller.setEvenement(evenementSelectionne);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible de charger l'interface: " + e.getMessage());
        }
    }

    @FXML
    private void handleVoirAnnonces(ActionEvent event) {
        if (evenementSelectionne == null) {
            showErrorAlert("Erreur", "Aucun événement sélectionné");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/listeAnnonces.fxml"));
            Parent root = loader.load();

            ListeAnnoncesController controller = loader.getController();
            controller.setEvenement(evenementSelectionne);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir la liste des annonces");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Dans ListeEvenementController.java

    @FXML
    private void showDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques des événements");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le dashboard: " + e.getMessage());
        }
    }
}