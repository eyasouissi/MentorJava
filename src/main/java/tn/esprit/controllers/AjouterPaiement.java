package tn.esprit.controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.Courses;
import tn.esprit.entities.Offre;
import tn.esprit.entities.Paiement;
import tn.esprit.entities.User;
import tn.esprit.services.CoursesService;
import tn.esprit.services.paiementService;


import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AjouterPaiement {

    @FXML
    private Label userLabel; // Label to display user name
    @FXML
    private DatePicker datePaiementPicker;
    @FXML
    private Label statusLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private Label offreLabel;
    @FXML
    private Button confirmerButton;
    @FXML
    private Button profileBtn; // For profile button

    private final paiementService paiementService = new paiementService();
    private Offre offre;
    private User currentUser; // Store the current logged-in user
    private boolean isPremiumOffre = false;
    @FXML
    public void initialize() {
        // Set default date to today
        datePaiementPicker.setValue(LocalDate.now());

        // Try to get user from session
        currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser != null) {
            updateUserDisplay();
        } else {
            System.out.println("Warning: No user found in session during AjouterPaiement initialization");
        }
    }

    @FXML
    private void goHome() {
        // Navigation logic to be implemented
    }

    @FXML
    private void goOffres() {
        // Navigation logic to be implemented
    }


    public void setOffre(Offre offre) {
        this.offre = offre;
        if (offreLabel != null && prixLabel != null && offre != null) {
            offreLabel.setText("Offre : " + offre.getName());
            prixLabel.setText("Prix : " + offre.getPrice() + "$");
        }
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Debug information
        System.out.println("Setting current user in AjouterPaiement: " +
                (user != null ? user.getName() : "null"));

        // Update UI with user info
        updateUserDisplay();
    }


    private void updateUserDisplay() {
        if (userLabel != null && currentUser != null) {
            // Update the user label with the user's name
            userLabel.setText(currentUser.getName());
            System.out.println("User display updated: " + currentUser.getName());
        } else {
            System.out.println("Unable to update user display: " +
                    (userLabel == null ? "userLabel is null" : "currentUser is null"));
        }

        // Update the profile button text if it exists
        if (profileBtn != null && currentUser != null) {
            profileBtn.setText(currentUser.getName());
        }
    }

    @FXML
    public void ajouterPaiement() {
        LocalDate selectedDate = datePaiementPicker.getValue();

        // Double-check that we have a user
        if (currentUser == null) {
            currentUser = UserSession.getInstance().getCurrentUser();
        }

        if (currentUser == null || selectedDate == null || offre == null) {
            statusLabel.setText("Error: User information or date missing.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            confirmerButton.setDisable(true);
            statusLabel.setText("Préparation du paiement Stripe...");

            String paymentIntentId = createPaymentIntent(offre.getPrice());
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            String clientSecret = intent.getClientSecret();

            openStripePaymentWindow(paymentIntentId, clientSecret, offre.getPrice());

        } catch (StripeException | IOException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        } finally {
            confirmerButton.setDisable(false);
        }
    }

    private String createPaymentIntent(double amount) throws StripeException {
        long amountInCents = (long) (amount * 100);
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInCents);
        params.put("currency", "eur");
        params.put("description", "Achat de l'offre: " + offre.getName() + " par " + currentUser.getName());

        Map<String, Object> paymentMethodOptions = new HashMap<>();
        Map<String, Object> cardOptions = new HashMap<>();
        cardOptions.put("request_three_d_secure", "automatic");
        paymentMethodOptions.put("card", cardOptions);
        params.put("payment_method_options", paymentMethodOptions);

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getId();
    }

    private void openStripePaymentWindow(String paymentIntentId, String clientSecret, double amount) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ProcessPayment.fxml"));
        Parent root = loader.load();
        ProcessPayment processPaymentController = loader.getController();

        // Set payment data
        processPaymentController.setPaymentData(paymentIntentId, clientSecret, amount);

        // Indicate if this is a premium payment
        processPaymentController.setPremiumPayment(isPremiumOffre);

        processPaymentController.setOnPaymentComplete(() -> {
            try {
                LocalDateTime paymentDate = datePaiementPicker.getValue().atStartOfDay();
                Paiement paiement = new Paiement(currentUser, offre, paymentDate);
                paiementService.ajouter(paiement);
                statusLabel.setText("Paiement effectué avec succès !");
                statusLabel.setStyle("-fx-text-fill: green;");

                // Close current window after successful payment
                if (isPremiumOffre) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) confirmerButton.getScene().getWindow();
                        stage.close();
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Erreur lors de l'enregistrement du paiement : " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Paiement par carte");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}