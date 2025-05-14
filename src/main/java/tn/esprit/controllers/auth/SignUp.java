package tn.esprit.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.services.EmailService;
import tn.esprit.services.VerificationServer;
import tn.esprit.services.VerificationService;
import tn.esprit.utils.PasswordUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SignUp {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z ]{2,30}$");

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField nameField;
    @FXML private TextField ageField;
    @FXML private Label errorLabel;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private RadioButton studentRadio;
    @FXML private RadioButton tutorRadio;
    @FXML private Text titleText;

    @FXML
    private ToggleGroup genderGroup = new ToggleGroup();
    @FXML
    private ToggleGroup roleGroup = new ToggleGroup();

    private String redirectTarget = "/interfaces/auth/login.fxml";
    private final UserService userService = UserService.getInstance();
    private boolean isAdminMode = false;

    @FXML
    public void initialize() {
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);

        studentRadio.setToggleGroup(roleGroup);
        tutorRadio.setToggleGroup(roleGroup);
        studentRadio.setSelected(true);

        List<String> countries = Arrays.asList(
                "Tunisia", "Algeria", "Morocco", "Libya", "Egypt",
                "France", "Germany", "USA", "Canada", "UK"
        );
        countryComboBox.getItems().addAll(countries);

        try {
            VerificationServer.start();
        } catch (IOException e) {
            System.err.println("Failed to start verification server:");
            e.printStackTrace();
            showError("System error: Verification service unavailable");
        }
    }

    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
    }

    public void setFormTitle(String title) {
        if (titleText != null) {
            titleText.setText(title);
        }
    }

    public void setRedirectTarget(String target) {
        this.redirectTarget = target;
    }

    @FXML
    public void handleSignUp(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            User newUser = createUserFromInput();
            System.out.println("Original password before processing: " + newUser.getPassword());

            userService.checkAndUpdateSchema();
            userService.ajouter(newUser);

            if (isAdminMode) {
                newUser.setVerified(true);
                userService.modifier(newUser);

                showSuccess("User added successfully!");
                showAlert("Success", "User added successfully!");

                ((Stage) emailField.getScene().getWindow()).close();
            } else {
                sendVerificationEmail(newUser);
                showSuccess("Registration successful! Please check your email to verify your account.");
                showAlert("Verification Needed", "Please check your email and click the verification link");
                redirectToTarget();
            }

            clearForm();
        } catch (Exception e) {
            showError("Operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateInputs() {
        String email = emailField.getText().trim();
        String plainPassword = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String country = countryComboBox.getValue();

        if (email.isEmpty() || plainPassword.isEmpty() || name.isEmpty() || ageText.isEmpty()) {
            showError("All fields are required");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Invalid email format");
            return false;
        }

        if (!NAME_PATTERN.matcher(name).matches()) {
            showError("Name must be 2-30 letters and spaces only");
            return false;
        }

        if (!plainPassword.equals(confirmPassword)) {
            showError("Passwords don't match");
            return false;
        }

        if (plainPassword.length() < 8) {
            showError("Password must be at least 8 characters");
            return false;
        }

        if (userService.getByEmail(email) != null) {
            showError("Email already registered");
            return false;
        }

        if (genderGroup.getSelectedToggle() == null) {
            showError("Please select your gender");
            return false;
        }

        if (country == null || country.isEmpty()) {
            showError("Please select a country");
            return false;
        }

        try {
            int age = Integer.parseInt(ageText);
            if (age < 13 || age > 120) {
                showError("Age must be between 13 and 120");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Age must be a number");
            return false;
        }

        return true;
    }

    private User createUserFromInput() {
        User newUser = new User();
        newUser.setEmail(emailField.getText().trim());
        newUser.setName(nameField.getText().trim());
        // Hash the password before saving
        newUser.setPassword(PasswordUtils.hashPassword(passwordField.getText()));
        newUser.setVerified(false);
        newUser.setVerificationToken(VerificationService.generateVerificationToken());
        newUser.setVerificationTokenExpiry(VerificationService.calculateExpiryDate());
        newUser.setGender(getSelectedGender());
        newUser.setAge(Integer.parseInt(ageField.getText().trim()));
        newUser.setCountry(countryComboBox.getValue());
        newUser.getRoles().clear();
        newUser.addRole(getSelectedRole());
        return newUser;
    }

    private void sendVerificationEmail(User user) {
        try {
            EmailService emailService = new EmailService();
            String verificationLink = VerificationServer.getVerificationUrl(user.getVerificationToken());

            String emailBody = String.format("""
                <html>
                    <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 10px;">
                            <h2 style="color: #4CAF50; text-align: center;">WorkAway Account Verification</h2>
                            <p style="font-size: 16px;">Hello %s,</p>
                            <p style="font-size: 16px;">Thank you for registering with WorkAway. Please verify your email address to activate your account.</p>
                            
                            <div style="text-align: center; margin: 25px 0;">
                                <a href="%s" style="background-color: #4CAF50; color: white; padding: 12px 24px; 
                                    text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                                    Verify Email Address
                                </a>
                            </div>
                            
                            <p style="font-size: 14px; color: #6c757d;">Or copy and paste this link into your browser:<br>
                            <span style="word-break: break-all;">%s</span></p>
                            
                            <p style="font-size: 14px; color: #6c757d;">If you didn't create an account with WorkAway, 
                            please ignore this email.</p>
                        </div>
                    </body>
                </html>
                """, user.getName(), verificationLink, verificationLink);

            emailService.sendEmail(
                    user.getEmail(),
                    "Verify Your WorkAway Account",
                    emailBody
            );
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String getSelectedGender() {
        RadioButton selectedRadioButton = (RadioButton) genderGroup.getSelectedToggle();
        if (selectedRadioButton == maleRadio) {
            return "Male";
        } else if (selectedRadioButton == femaleRadio) {
            return "Female";
        }
        return null;
    }

    private String getSelectedRole() {
        RadioButton selectedRadioButton = (RadioButton) roleGroup.getSelectedToggle();
        if (selectedRadioButton == tutorRadio) {
            return "ROLE_TUTOR";
        }
        return "ROLE_STUDENT";
    }

    private void redirectToTarget() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(redirectTarget));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Cannot redirect to target view");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }

    private void clearForm() {
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        nameField.clear();
        ageField.clear();
        genderGroup.selectToggle(null);
        countryComboBox.getSelectionModel().clearSelection();
        studentRadio.setSelected(true);
    }

    @FXML
    public void handleLoginRedirect(ActionEvent event) {
        if (!isAdminMode) {
            redirectToTarget();
        }
    }
}