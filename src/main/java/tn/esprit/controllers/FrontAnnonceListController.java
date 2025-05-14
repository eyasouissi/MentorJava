package tn.esprit.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.entities.Annonce;
import tn.esprit.entities.Evenement;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tn.esprit.services.AnnonceService;
import tn.esprit.services.EvenementService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class FrontAnnonceListController {

    @FXML private ScrollPane scrollPane;
    @FXML private GridPane annoncesContainer;
    @FXML private TextField searchField;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Label resultsCount;
    @FXML private Label mainTitle;

    // Éléments overlay
    @FXML private VBox eventDetailsOverlay;
    @FXML private VBox eventDetailsCard;
    @FXML private ImageView eventImage;
    @FXML private Label eventTitle;
    @FXML private Label eventDate;
    @FXML private Label eventLocation;
    @FXML private Label eventStatus;
    @FXML private TextArea eventDescription;
    @FXML private ImageView eventQrCode;
    @FXML private Button closeDetailsButton;

    private final AnnonceService annonceService = new AnnonceService();
    private final EvenementService evenementService = new EvenementService();
    private List<Annonce> annonces;
    private List<Annonce> filteredAnnonces;
    private static final int PAGE_SIZE = 6;
    private int currentPage = 0;
    private int totalPages = 0;

    @FXML
    public void initialize() {
        try {
            configureUI();
            loadAnnonces();
            setupEventDetailsOverlay();
            setupPaginationButtons(); // Ajout de cette ligne pour configurer les boutons de pagination
            showPage(currentPage);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement",
                    "Impossible de charger les annonces: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void configureUI() {
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Ajouter un listener pour le champ de recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterAnnonces(newVal);
            showPage(0);  // Revenir à la première page après filtrage
        });
    }

    // Nouvelle méthode pour configurer les boutons de pagination
    private void setupPaginationButtons() {
        previousButton.setOnAction(event -> previousPage());
        nextButton.setOnAction(event -> nextPage());
    }

    private void loadAnnonces() throws SQLException {
        annonces = annonceService.getAllAnnonces();
        filteredAnnonces = List.copyOf(annonces);
        calculateTotalPages();
        updateResultsCount();
    }

    private void calculateTotalPages() {
        totalPages = (int) Math.ceil((double) filteredAnnonces.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1; // Au moins une page même vide
    }

    private void setupEventDetailsOverlay() {
        eventDetailsOverlay.setVisible(false);
        eventDetailsOverlay.setPickOnBounds(true);

        closeDetailsButton.setOnAction(e -> hideEventDetails());
        eventDetailsOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == eventDetailsOverlay) {
                hideEventDetails();
            }
        });
    }

    private void showPage(int page) {
        annoncesContainer.getChildren().clear();
        currentPage = page;

        // Vérifier que la page demandée est valide
        if (currentPage < 0) currentPage = 0;
        if (currentPage >= totalPages) currentPage = totalPages - 1;

        // Calculer les indices de début et de fin
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredAnnonces.size());

        // Afficher les annonces de la page
        int row = 0, col = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            Node card = createAnnonceCard(filteredAnnonces.get(i));
            annoncesContainer.add(card, col, row);
            GridPane.setMargin(card, new Insets(10));

            if (++col >= 3) {
                col = 0;
                row++;
            }
        }
        updatePagination();
    }

    private Node createAnnonceCard(Annonce annonce) {
        VBox card = new VBox();
        card.getStyleClass().add("annonce-card");
        card.setPrefSize(280, 320);
        card.setMaxSize(280, 320);

        // Image
        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("annonce-image");
        imageView.setFitWidth(280);
        imageView.setFitHeight(180);

        try {
            imageView.setImage(loadImage(annonce.getImageUrl(), "/images/default-annonce.png"));
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default-annonce.png")));
        }

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.getStyleClass().add("annonce-image-container");

        // Contenu texte
        VBox contentBox = new VBox(8,
                createStyledLabel(annonce.getTitreA(), "annonce-title", 250),
                createStyledLabel(annonce.getDescriptionA(), "annonce-description", 250),
                createStyledLabel("Publié le: " + annonce.getDateA().toLocalDate(), "annonce-date", 250)
        );
        contentBox.getStyleClass().add("annonce-content");
        contentBox.setPadding(new Insets(15));

        card.getChildren().addAll(imageContainer, contentBox);
        setupCardHoverEffects(card);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                showEventDetails(annonce);
            }
        });

        return card;
    }

    private Label createStyledLabel(String text, String styleClass, double maxWidth) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMaxWidth(maxWidth);
        return label;
    }

    private Image loadImage(String path, String defaultPath) {
        try {
            if (path != null && !path.isEmpty()) {
                return new Image("file:" + path);
            }
            return new Image(getClass().getResourceAsStream(defaultPath));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream(defaultPath));
        }
    }

    private void setupCardHoverEffects(VBox card) {
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-effect: dropshadow(gaussian, rgba(157, 123, 198, 0.3), 15, 0, 0, 8);");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(138, 109, 174, 0.2), 10, 0, 0, 5);");
        });
    }

    private void showEventDetails(Annonce annonce) {
        try {
            Evenement evenement = evenementService.getEvenementById(annonce.getEvenementId());
            if (evenement == null) {
                showAlert("Information", "Aucun événement",
                        "Cette annonce n'est liée à aucun événement.",
                        Alert.AlertType.INFORMATION);
                return;
            }

            eventTitle.setText(evenement.getTitreE());
            eventDescription.setText(evenement.getDescriptionE());
            eventDate.setText(formatDate(evenement.getDateDebut()));
            eventLocation.setText(evenement.getLieu() != null ? evenement.getLieu() : "Non spécifié");
            updateEventStatus(evenement.getDateDebut(), evenement.getDateFin());

            eventImage.setImage(loadImage(evenement.getImageE(), "/images/default-event.png"));
            generateQRCode(evenement);

            animateShowOverlay();
        } catch (SQLException e) {
            showAlert("Erreur", "Base de données",
                    "Erreur lors du chargement de l'événement: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void animateShowOverlay() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), eventDetailsOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        eventDetailsOverlay.setVisible(true);
        fadeIn.play();

        scrollPane.setVvalue(0);
    }

    private void hideEventDetails() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), eventDetailsOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> eventDetailsOverlay.setVisible(false));
        fadeOut.play();
    }

    private void generateQRCode(Evenement evenement) {
        try {
            String qrContent = buildQRContent(evenement);
            QRCodeWriter writer = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = writer.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    200, 200,
                    hints
            );

            BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF6A4A8A : 0xFFFFFFFF);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            eventQrCode.setImage(new Image(new ByteArrayInputStream(baos.toByteArray())));

        } catch (WriterException | IOException e) {
            eventQrCode.setImage(new Image(getClass().getResourceAsStream("/images/qr-error.png")));
        }
    }

    private String buildQRContent(Evenement evenement) {
        return String.format(
                "Événement: %s\nDate: %s\nLieu: %s\n\nScanné via MentorApp",
                evenement.getTitreE(),
                formatDate(evenement.getDateDebut()),
                evenement.getLieu() != null ? evenement.getLieu() : "Non spécifié"
        );
    }

    private void updateEventStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            eventStatus.setText("À venir");
            eventStatus.getStyleClass().setAll("event-status", "status-upcoming");
        } else if (now.isAfter(end != null ? end : start.plusHours(2))) {
            eventStatus.setText("Terminé");
            eventStatus.getStyleClass().setAll("event-status", "status-past");
        } else {
            eventStatus.setText("En cours");
            eventStatus.getStyleClass().setAll("event-status", "status-ongoing");
        }
    }

    private String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH));
    }

    private void filterAnnonces(String query) {
        if (query == null || query.isEmpty()) {
            filteredAnnonces = List.copyOf(annonces);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            filteredAnnonces = annonces.stream()
                    .filter(a -> a.getTitreA().toLowerCase().contains(lowerCaseQuery) ||
                            a.getDescriptionA().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }
        calculateTotalPages();
        updateResultsCount();
    }

    private void updateResultsCount() {
        resultsCount.setText(filteredAnnonces.size() + " annonces found");
    }

    private void updatePagination() {
        pageLabel.setText("Page " + (currentPage + 1) + "/" + totalPages);

        previousButton.setDisable(currentPage <= 0);
        nextButton.setDisable(currentPage >= totalPages - 1 || filteredAnnonces.isEmpty());
    }

    @FXML
    public void previousPage() {
        if (currentPage > 0) {
            showPage(currentPage - 1);
        }
    }

    @FXML
    public void nextPage() {
        if (currentPage < totalPages - 1) {
            showPage(currentPage + 1);
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToCalendar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Front/Calendar.fxml"));
            Parent calendarRoot = loader.load();

            // Obtenir le conteneur parent (root actuel)
            Scene scene = ((Node) event.getSource()).getScene();
            Parent currentRoot = scene.getRoot();

            // Préparer la nouvelle scène pour glisser depuis la droite
            calendarRoot.translateXProperty().set(scene.getWidth());

            // Ajouter le calendrier à l’arbre de scène
            StackPane stack = new StackPane(currentRoot, calendarRoot);
            scene.setRoot(stack);

            // Animation de slide
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(calendarRoot.translateXProperty(), 0, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.seconds(0.5), kv);
            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(e -> scene.setRoot(calendarRoot)); // Nettoie la transition
            timeline.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
