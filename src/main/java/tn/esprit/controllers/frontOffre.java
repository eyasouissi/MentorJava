package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.stage.Stage;
import tn.esprit.entities.Offre;
import tn.esprit.entities.User;
import tn.esprit.services.offreService;
import tn.esprit.utils.UserSession;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import net.glxn.qrgen.javase.QRCode;

public class frontOffre {

    @FXML
    private Pagination offersPagination;

    @FXML
    private Button profileBtn;

    private offreService offreService;
    private List<Offre> allOffers;
    private final int OFFERS_PER_PAGE = 3;
    private User currentUser;

    public frontOffre() {
        offreService = new offreService();
        allOffers = new ArrayList<>();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Current user set in OffresController: " + (user != null ? user.getName() : "null"));
    }

    @FXML
    private void goHome() {
        // Navigation home
    }

    @FXML
    private void goAbout() {
        // Navigation about
    }

    @FXML
    private void goForum() {
        // Navigation forum
    }

    public void initialize() {
        try {
            allOffers = offreService.recuperer();
            int pageCount = (int) Math.ceil((double) allOffers.size() / OFFERS_PER_PAGE);
            offersPagination.setPageCount(pageCount);
            offersPagination.setCurrentPageIndex(0);
            offersPagination.setPageFactory(this::createPage);
            if (currentUser == null) {
                currentUser = UserSession.getInstance().getCurrentUser();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createPage(int pageIndex) {
        HBox pageOffersContainer = new HBox(20);
        pageOffersContainer.setAlignment(javafx.geometry.Pos.CENTER);
        pageOffersContainer.setStyle("-fx-padding: 10;");

        int start = pageIndex * OFFERS_PER_PAGE;
        int end = Math.min(start + OFFERS_PER_PAGE, allOffers.size());

        for (int i = start; i < end; i++) {
            VBox offerBox = createOfferBox(allOffers.get(i));
            pageOffersContainer.getChildren().add(offerBox);
        }

        return pageOffersContainer;
    }

    private VBox createOfferBox(Offre offre) {
        VBox offerBox = new VBox(10);
        offerBox.setPrefWidth(260);
        offerBox.setStyle("""
            -fx-background-color: white;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 0, 4);
            -fx-padding: 15px;
            -fx-alignment: center;
        """);

        ImageView imageView = new ImageView();
        if (offre.getImagePath() != null && !offre.getImagePath().isEmpty()) {
            File imageFile = new File("src/main/resources/images/" + offre.getImagePath());
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
                imageView.setFitWidth(230);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                offerBox.getChildren().add(imageView);
            }
        }

        Label nomOffre = new Label(offre.getName());
        nomOffre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4b0082;");

        Label prixOffre = new Label("$" + offre.getPrice() + " / mois");
        prixOffre.setStyle("-fx-font-size: 15px; -fx-text-fill: #6a5acd;");

        Label description = new Label("\uD83D\uDCDD " + offre.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b0082;");

        Label dateDebut = new Label("\uD83D\uDCC5 Début : " + offre.getStartDate().toLocalDate());
        dateDebut.setStyle("-fx-font-size: 12px; -fx-text-fill: #4b0082;");

        Label dateFin = new Label("\uD83D\uDCC5 Fin : " + offre.getEndDate());
        dateFin.setStyle("-fx-font-size: 12px; -fx-text-fill: #4b0082;");

        Button btnAbonner = new Button("Subscribe");
        btnAbonner.setStyle("""
            -fx-background-color: #6a5acd;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8px;
            -fx-padding: 8 16;
        """);
        btnAbonner.setOnAction(e -> onSubscribeButtonClick(offre));

        // 🔥 QR Code
        long joursRestants = calculateRemainingDays(offre);
        String qrText = "Remaining days before the end of the subscription \"" + offre.getName() + "\" : " + joursRestants;
        ImageView qrCodeImageView = generateQRCodeImageView(qrText, 100, 100);
        if (qrCodeImageView != null) {
            offerBox.getChildren().add(qrCodeImageView);
        }

        offerBox.getChildren().addAll(nomOffre, prixOffre, description, dateDebut, dateFin, btnAbonner);
        return offerBox;
    }

    private void onSubscribeButtonClick(Offre offre) {
        System.out.println("User subscribed to offre : " + offre.getName());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjouterPaiement.fxml"));
            Parent root = loader.load();
            AjouterPaiement ajouterPaiementController = loader.getController();
            if (ajouterPaiementController != null) {
                ajouterPaiementController.setOffre(offre);
                User user = (currentUser != null) ? currentUser : UserSession.getInstance().getCurrentUser();
                if (user != null) {
                    ajouterPaiementController.setCurrentUser(user);
                }
            }
            Scene currentScene = offersPagination.getScene();
            if (currentScene != null) {
                Stage stage = (Stage) currentScene.getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long calculateRemainingDays(Offre offre) {
        try {
            LocalDate today = LocalDate.now();
            Object endDateObj = offre.getEndDate();
            LocalDate endDate;

            if (endDateObj instanceof LocalDate) {
                endDate = (LocalDate) endDateObj;
            } else if (endDateObj instanceof java.sql.Date) {
                endDate = ((java.sql.Date) endDateObj).toLocalDate();
            } else if (endDateObj instanceof java.util.Date) {
                endDate = new java.sql.Date(((java.util.Date) endDateObj).getTime()).toLocalDate();
            } else {
                endDate = LocalDate.parse(endDateObj.toString());
            }

            long days = ChronoUnit.DAYS.between(today, endDate);
            return Math.max(0, days);
        } catch (Exception e) {
            System.err.println("Error calculating remaining days: " + e.getMessage());
            return 0;
        }
    }

    // ✅ Méthode QR Code
    private ImageView generateQRCodeImageView(String text, int width, int height) {
        try {
            ByteArrayOutputStream qrOutput = QRCode.from(text)
                    .withSize(width, height)
                    .stream();
            ByteArrayInputStream bis = new ByteArrayInputStream(qrOutput.toByteArray());
            Image qrImage = new Image(bis);
            ImageView qrImageView = new ImageView(qrImage);
            qrImageView.setFitWidth(width);
            qrImageView.setFitHeight(height);
            return qrImageView;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
