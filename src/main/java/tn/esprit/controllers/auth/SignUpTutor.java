package tn.esprit.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.services.VerificationServer;
import tn.esprit.services.VerificationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SignUpTutor {
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
    @FXML private TextField specialityField;
    @FXML private Label diplomaFileNameLabel;

    private File diplomaFile;
    private String diplomaFilePath;
    public ToggleGroup genderGroup = new ToggleGroup();
    private String redirectTarget = "/interfaces/auth/login.fxml";
    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);

        List<String> countries = Arrays.asList(
                "Tunisia", "Algeria", "Morocco", "Libya", "Egypt",
                "France", "Germany", "USA", "Canada", "UK"
        );
        countryComboBox.getItems().addAll(countries);
    }

    @FXML
    public void handleUploadDiploma(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Diploma PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        diplomaFile = fileChooser.showOpenDialog(((Button) event.getSource()).getScene().getWindow());

        if (diplomaFile != null) {
            diplomaFileNameLabel.setText(diplomaFile.getName());

            // Create diplomas directory if it doesn't exist
            File diplomasDir = new File("uploads/diplomas");
            if (!diplomasDir.exists()) {
                diplomasDir.mkdirs();
            }

            // Generate unique filename to prevent conflicts
            String uniqueFileName = System.currentTimeMillis() + "_" + diplomaFile.getName();
            diplomaFilePath = "uploads/diplomas/" + uniqueFileName;

            try {
                Files.copy(
                        diplomaFile.toPath(),
                        new File(diplomaFilePath).toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                showError("Failed to save diploma file");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSignUp(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            User newUser = createUserFromInput();
            newUser.addRole("ROLE_TUTOR");

            if (diplomaFilePath != null) {
                newUser.setDiplome(diplomaFilePath);
            }

            userService.checkAndUpdateSchema();
            userService.ajouter(newUser);

            if (userService.isEmailServiceAvailable()) {
                showSuccess("Registration successful! Please check your email to verify your account.");
            } else {
                String manualVerificationUrl = VerificationServer.getVerificationUrl(newUser.getVerificationToken());
                showAlert("Manual Verification Required",
                        "Email service unavailable. Please visit this link to verify:\n" +
                                manualVerificationUrl + "\n\nOr use this token: " +
                                newUser.getVerificationToken());
            }

            clearForm();
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        String email = emailField.getText().trim();
        String plainPassword = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String country = countryComboBox.getValue();
        String speciality = specialityField.getText().trim();

        if (email.isEmpty() || plainPassword.isEmpty() || name.isEmpty() ||
                ageText.isEmpty() || speciality.isEmpty()) {
            showError("All fields are required");
            return false;
        }

        if (diplomaFile == null) {
            showError("Please upload your diploma");
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
        newUser.setPassword(passwordField.getText());
        newUser.setVerified(false);
        newUser.setVerificationToken(VerificationService.generateVerificationToken());
        newUser.setVerificationTokenExpiry(VerificationService.calculateExpiryDate());
        newUser.setGender(getSelectedGender());
        newUser.setAge(Integer.parseInt(ageField.getText().trim()));
        newUser.setCountry(countryComboBox.getValue());
        newUser.setSpeciality(specialityField.getText().trim());
        newUser.getRoles().clear();
        return newUser;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        specialityField.clear();
        diplomaFileNameLabel.setText("No file selected");
        diplomaFile = null;
        diplomaFilePath = null;
        genderGroup.selectToggle(null);
        countryComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleLoginRedirect(ActionEvent event) {
        redirectToTarget();
    }
}