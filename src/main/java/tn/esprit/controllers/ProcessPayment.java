package tn.esprit.controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Courses;
import tn.esprit.services.CoursesService;

import java.io.IOException;
import java.net.URL;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProcessPayment implements Initializable {

    @FXML
    private TextField cardNumberField;

    @FXML
    private TextField cardNameField;

    @FXML
    private ComboBox<String> expiryMonthCombo;

    @FXML
    private ComboBox<String> expiryYearCombo;

    @FXML
    private PasswordField cvcField;

    @FXML
    private Label totalLabel;

    @FXML
    private Label paymentStatusLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    private String paymentIntentId;
    private String clientSecret;
    private double amount;
    private boolean isPremiumPayment = false;

    // Service for courses
    private CoursesService coursesService = new CoursesService();

    // Interface pour callback après succès
    public interface PaymentCompleteCallback {
        void onComplete();
    }

    private PaymentCompleteCallback paymentCompleteCallback;

    public void setOnPaymentComplete(PaymentCompleteCallback callback) {
        this.paymentCompleteCallback = callback;
    }

    // Setter for premium flag
    public void setPremiumPayment(boolean isPremiumPayment) {
        this.isPremiumPayment = isPremiumPayment;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressIndicator.setVisible(false);

        // Initialisation des ComboBox pour les dates d'expiration
        initializeExpiryFields();

        // Configuration du formattage pour le numéro de carte
        setupCardNumberFormatter();

        // Configuration du formattage pour le CVC
        setupCVCFormatter();

        // Effacer les messages d'erreur lorsqu'un champ est modifié
        setupFieldListeners();
    }

    private void initializeExpiryFields() {
        // Setup expiry month dropdown
        Platform.runLater(() -> {
            try {
                expiryMonthCombo.getItems().clear();
                for (int i = 1; i <= 12; i++) {
                    expiryMonthCombo.getItems().add(String.format("%02d", i));
                }

                // Sélectionner le mois actuel par défaut
                int currentMonth = java.time.LocalDate.now().getMonthValue();
                expiryMonthCombo.setValue(String.format("%02d", currentMonth));

                // Setup expiry year dropdown
                expiryYearCombo.getItems().clear();
                int currentYear = Year.now().getValue();
                for (int i = 0; i < 10; i++) {
                    String yearStr = String.valueOf(currentYear + i).substring(2);
                    expiryYearCombo.getItems().add(yearStr);
                }

                // Sélectionner l'année actuelle par défaut
                expiryYearCombo.setValue(String.valueOf(currentYear).substring(2));

                System.out.println("Champs de date d'expiration initialisés: Mois=" +
                        expiryMonthCombo.getValue() + ", Année=" + expiryYearCombo.getValue());
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation des champs d'expiration: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void setupCardNumberFormatter() {
        cardNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            // Supprimer tous les caractères non numériques
            String digits = newValue.replaceAll("\\D", "");

            // Limiter à 16 chiffres
            if (digits.length() > 16) {
                digits = digits.substring(0, 16);
            }

            // Formater en groupes de 4 chiffres
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(digits.charAt(i));
            }

            String result = formatted.toString();
            if (!result.equals(newValue)) {
                cardNumberField.setText(result);
            }
        });
    }

    private void setupCVCFormatter() {
        // Limiter CVC à 3-4 chiffres
        cvcField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 4) {
                cvcField.setText(oldValue);
            } else if (!newValue.matches("\\d*")) {
                cvcField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void setupFieldListeners() {
        // Effacer les messages d'erreur quand l'utilisateur modifie un champ
        cardNumberField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) clearErrorStyles();
        });
        cardNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) clearErrorStyles();
        });
        expiryMonthCombo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) clearErrorStyles();
        });
        expiryYearCombo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) clearErrorStyles();
        });
        cvcField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) clearErrorStyles();
        });
    }

    private void clearErrorStyles() {
        paymentStatusLabel.setText("");
        paymentStatusLabel.getStyleClass().remove("error-text");
    }

    // Setter pour données de paiement avec vérification des nulls
    public void setPaymentData(String paymentIntentId, String clientSecret, double amount) {
        if (paymentIntentId == null || clientSecret == null) {
            System.err.println("Error: Payment intent ID or client secret is null");
            Platform.runLater(() -> {
                paymentStatusLabel.setText("Erreur de configuration. Veuillez réessayer plus tard.");
                paymentStatusLabel.getStyleClass().add("error-text");
            });
            return;
        }

        this.paymentIntentId = paymentIntentId;
        this.clientSecret = clientSecret;
        this.amount = amount;

        Platform.runLater(() -> {
            totalLabel.setText(String.format("Total à payer: %.2f €", amount));
        });

        System.out.println("Données de paiement configurées: ID=" + paymentIntentId + ", Montant=" + amount);
    }

    @FXML
    private void processPayment(ActionEvent event) {
        // Log de début de traitement
        System.out.println("Démarrage du traitement du paiement");

        // Vérifier si les données de paiement sont initialisées
        if (paymentIntentId == null || clientSecret == null) {
            showError("Erreur de configuration du paiement");
            System.out.println("PaymentIntent ou ClientSecret est null");
            return;
        }

        if (!validateFields()) {
            System.out.println("Validation échouée: formulaire incomplet ou invalide");
            return;
        }

        // Vérification supplémentaire pour la validité du numéro de carte
        String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
        if (!isValidCardNumber(cardNumber)) {
            showError("Le numéro de carte est invalide");
            System.out.println("Validation échouée: numéro de carte invalide");
            return;
        }

        progressIndicator.setVisible(true);
        paymentStatusLabel.setText("Traitement en cours...");

        Task<Boolean> paymentTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    if (paymentIntentId == null) {
                        System.out.println("Error: PaymentIntent ID is null");
                        return false;
                    }

                    System.out.println("Tentative de récupération du PaymentIntent: " + paymentIntentId);

                    // Récupérer l'état actuel du PaymentIntent
                    PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
                    if (intent == null) {
                        System.out.println("Error: Retrieved PaymentIntent is null");
                        return false;
                    }

                    System.out.println("Status initial: " + intent.getStatus());

                    if ("succeeded".equals(intent.getStatus())) {
                        return true; // Déjà payé
                    }

                    // Créer la méthode de paiement en mode TEST
                    Map<String, Object> paymentMethodParams = new HashMap<>();
                    paymentMethodParams.put("type", "card");

                    // On utilise un token de test au lieu du numéro de carte directement
                    String cardNumber = cardNumberField.getText().replaceAll("\\s", "");

                    // Choisir le token en fonction du numéro de carte
                    String tokenId;
                    if (cardNumber.equals("4242424242424242")) {
                        tokenId = "tok_visa"; // Carte qui réussit
                    } else if (cardNumber.equals("4000000000000002")) {
                        tokenId = "tok_visa_declined"; // Carte déclinée
                    } else if (cardNumber.equals("4000000000009995")) {
                        tokenId = "tok_insufficient_funds"; // Fonds insuffisants
                    } else if (cardNumber.equals("4000000000003220")) {
                        tokenId = "tok_chargeCustomerFail"; // Échec de facturation
                    } else if (cardNumber.equals("4000008400001629")) {
                        tokenId = "tok_processingError"; // Erreur de traitement
                    } else {
                        tokenId = "tok_visa"; // Par défaut, utiliser une carte qui réussit
                    }

                    Map<String, Object> cardParams = new HashMap<>();
                    cardParams.put("token", tokenId);
                    paymentMethodParams.put("card", cardParams);

                    // Ajouter les détails de facturation
                    Map<String, Object> billingDetails = new HashMap<>();
                    billingDetails.put("name", cardNameField.getText());
                    paymentMethodParams.put("billing_details", billingDetails);

                    System.out.println("Création de la méthode de paiement avec token: " + tokenId);

                    // Créer la méthode de paiement
                    PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);
                    if (paymentMethod == null) {
                        System.out.println("Error: Created PaymentMethod is null");
                        return false;
                    }

                    // Confirmer le paiement avec cette méthode
                    Map<String, Object> confirmParams = new HashMap<>();
                    confirmParams.put("payment_method", paymentMethod.getId());

                    System.out.println("Confirmation avec méthode de paiement ID: " + paymentMethod.getId());
                    intent = intent.confirm(confirmParams);

                    System.out.println("Status final: " + intent.getStatus());
                    return "succeeded".equals(intent.getStatus());

                } catch (StripeException e) {
                    e.printStackTrace();
                    String errorMessage = formatStripeError(e);
                    System.out.println("Erreur Stripe: " + errorMessage);
                    Platform.runLater(() -> showError(errorMessage));
                    return false;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    System.out.println("NullPointerException: " + e.getMessage());
                    Platform.runLater(() -> showError("Erreur interne: données manquantes"));
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Exception non-Stripe: " + e.getMessage());
                    Platform.runLater(() -> showError("Erreur: " + e.getMessage()));
                    return false;
                }
            }
        };

        paymentTask.setOnSucceeded(workerStateEvent -> {
            boolean success = paymentTask.getValue();
            progressIndicator.setVisible(false);

            if (success) {
                paymentStatusLabel.setText("✅ Paiement réussi !");
                paymentStatusLabel.getStyleClass().add("success-text");
                System.out.println("Paiement réussi");

                // Make premium courses free after successful payment
                if (isPremiumPayment) {
                    makePremiumCoursesFree();
                }

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Courses/MainCourseFront.fxml"));
                                Parent root = loader.load();

                                Stage stage = (Stage) cardNumberField.getScene().getWindow();
                                stage.setScene(new Scene(root));
                                stage.setTitle("Cours Premium");
                            } catch (IOException e) {
                                e.printStackTrace();
                                showError("Erreur lors du chargement de la page des cours.");
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                paymentStatusLabel.setText("❌ Paiement échoué. Veuillez réessayer.");
                paymentStatusLabel.getStyleClass().add("error-text");
                System.out.println("Paiement échoué");
            }
        });

        paymentTask.setOnFailed(workerStateEvent -> {
            progressIndicator.setVisible(false);
            Throwable e = paymentTask.getException();
            e.printStackTrace();
            System.out.println("Exception pendant le traitement: " + e.getMessage());
            paymentStatusLabel.setText("Erreur interne: " + e.getMessage());
            paymentStatusLabel.getStyleClass().add("error-text");
        });

        System.out.println("Démarrage de la tâche de paiement");
        new Thread(paymentTask).start();
    }

    // Nouvelle méthode pour rendre les cours premium gratuits
    private void makePremiumCoursesFree() {
        try {
            System.out.println("Mise à jour des cours premium pour les rendre gratuits...");

            Task<Void> updateCoursesTask = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        // Récupérer tous les cours premium
                        List<Courses> premiumCourses = coursesService.getAllPremiumCourses();
                        System.out.println("Nombre de cours premium trouvés: " + premiumCourses.size());

                        // Mettre à jour chaque cours
                        for (Courses course : premiumCourses) {
                            coursesService.markCourseAsNonPremium(course);
                            System.out.println("Cours rendu gratuit: " + course.getTitle());
                        }

                        return null;
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la mise à jour des cours: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }
            };

            updateCoursesTask.setOnSucceeded(event -> {
                System.out.println("Tous les cours premium sont maintenant gratuits!");
                Platform.runLater(this::showSuccessDialogWithPremiumInfo);
            });

            updateCoursesTask.setOnFailed(event -> {
                System.err.println("Échec de la mise à jour des cours: " + updateCoursesTask.getException().getMessage());
                updateCoursesTask.getException().printStackTrace();
            });

            new Thread(updateCoursesTask).start();

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des cours premium: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Format une erreur Stripe pour l'affichage
    private String formatStripeError(StripeException e) {
        if (e == null) return "Erreur inconnue";

        if (e.getStripeError() == null) {
            // Si getStripeError() est null, retourner le message de l'exception directement
            return e.getMessage() != null ? e.getMessage() : "Erreur de paiement inconnue";
        }

        String code = e.getStripeError().getCode();

        // Si le code est null, retourner le message de l'erreur Stripe ou de l'exception
        if (code == null) {
            return e.getStripeError().getMessage() != null ?
                    e.getStripeError().getMessage() :
                    (e.getMessage() != null ? e.getMessage() : "Erreur de carte");
        }

        System.out.println("Code d'erreur Stripe: " + code);

        switch (code) {
            case "card_declined":
                return "Carte refusée";
            case "incorrect_cvc":
                return "Code CVC incorrect";
            case "expired_card":
                return "Carte expirée";
            case "processing_error":
                return "Erreur de traitement bancaire";
            case "incorrect_number":
                return "Numéro de carte invalide";
            case "insufficient_funds":
                return "Fonds insuffisants";
            default:
                return "Erreur: " + e.getStripeError().getMessage();
        }
    }

    private boolean validateFields() {
        try {
            String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
            String cardName = cardNameField.getText();
            String expMonth = expiryMonthCombo.getValue();
            String expYear = expiryYearCombo.getValue();
            String cvc = cvcField.getText();

            System.out.println("Validation des champs:");
            System.out.println("- Numéro: " + (cardNumber.isEmpty() ? "vide" : "présent (longueur: " + cardNumber.length() + ")"));
            System.out.println("- Nom: " + (cardName.isEmpty() ? "vide" : "présent"));
            System.out.println("- Mois expiration: " + (expMonth == null ? "non sélectionné" : expMonth));
            System.out.println("- Année expiration: " + (expYear == null ? "non sélectionné" : expYear));
            System.out.println("- CVC: " + (cvc.isEmpty() ? "vide" : "présent (longueur: " + cvc.length() + ")"));

            if (cardNumber.isEmpty() || cardNumber.length() < 16) {
                showError("Veuillez entrer un numéro de carte valide");
                return false;
            }

            if (cardName.isEmpty()) {
                showError("Veuillez entrer le nom sur la carte");
                return false;
            }

            if (expMonth == null || expYear == null) {
                showError("Veuillez sélectionner une date d'expiration");
                return false;
            }

            if (cvc.isEmpty() || cvc.length() < 3) {
                showError("Veuillez entrer un code CVC valide");
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("Erreur pendant la validation des champs: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de validation des données");
            return false;
        }
    }

    // Méthode pour valider le numéro de carte avec l'algorithme de Luhn
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) return false;

        // Pour les tests, accepter les cartes de test Stripe valides sans vérification Luhn
        if (cardNumber.equals("4242424242424242") ||
                cardNumber.equals("4000000000000002") ||
                cardNumber.equals("4000000000009995") ||
                cardNumber.equals("4000000000003220") ||
                cardNumber.equals("4000008400001629")) {
            return true;
        }

        // Algorithme de Luhn pour vérifier la validité du numéro de carte
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    // Afficher un message d'erreur
    private void showError(String errorMessage) {
        if (errorMessage == null) errorMessage = "Erreur inconnue";

        final String finalMessage = errorMessage;
        Platform.runLater(() -> {
            paymentStatusLabel.setText(finalMessage);
            paymentStatusLabel.getStyleClass().add("error-text");
        });
    }

    // Modified success dialog to include information about premium courses
    private void showSuccessDialogWithPremiumInfo() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Paiement réussi");
            alert.setHeaderText("Votre paiement a été effectué avec succès !");

            if (isPremiumPayment) {
                alert.setContentText("Félicitations ! Tous les cours premium sont maintenant accessibles gratuitement. " +
                        "Vous allez être redirigé vers la page des cours.");
            } else {
                alert.setContentText("Merci d'avoir choisi nos services.");
            }

            alert.showAndWait();

            // Complete payment process and invoke callbacks
            if (paymentCompleteCallback != null) {
                paymentCompleteCallback.onComplete();
            }

            Stage currentStage = (Stage) totalLabel.getScene().getWindow();

            // If this is a premium payment, navigate to premium courses
            if (isPremiumPayment) {
                navigateToPremiumCourses();
            }

            currentStage.close();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage du dialogue de succès: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigate to premium courses (now free courses)
    private void navigateToPremiumCourses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Courses/MainCourseFront.fxml"));
            Parent root = loader.load();

            // Get the controller instance if needed to set any specific filters or settings
            // MainCourseFrontController controller = loader.getController();
            // You could set any specific view settings here

            // Create and set up new stage
            Stage stage = new Stage();
            stage.setTitle("Cours (Anciennement Premium)");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors de la redirection vers les cours: " + e.getMessage());
            e.printStackTrace();

            showError("Impossible de naviguer vers les cours: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) cardNameField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de la fenêtre: " + e.getMessage());
            e.printStackTrace();
        }
    }
}