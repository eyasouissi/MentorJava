package tn.esprit.controllers.Category;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Category;
import tn.esprit.services.CategoryService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

public class AddCategoryController {
    // Constants
    private static final String UPLOAD_DIRECTORY = "C:/xampp/htdocs/uploads/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String[] ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif"};

    // Form fields
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField iconField;
    @FXML private CheckBox isActiveCheckBox;
    @FXML private ImageView iconPreview;

    // UI elements
    @FXML private Label noIconLabel;
    @FXML private Label nameErrorLabel;
    @FXML private Label descErrorLabel;
    @FXML private Label iconErrorLabel;

    private CategoryController parentController;
    private final CategoryService categoryService = CategoryService.getInstance();
    private final FileChooser fileChooser = new FileChooser();

    @FXML
    public void initialize() {
        configureFileChooser();
        setupValidationListeners();
        resetErrorLabels();
    }

    private void configureFileChooser() {
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
    }

    private void setupValidationListeners() {
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateName());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescription());
        iconField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateIconPreview(newVal);
            validateIcon();
        });
    }

    private void resetErrorLabels() {
        nameErrorLabel.setText("");
        descErrorLabel.setText("");
        iconErrorLabel.setText("");
    }

    // Validation methods
    private void validateName() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            setError(nameField, nameErrorLabel, "Name is required");
        } else if (name.length() > 50) {
            setError(nameField, nameErrorLabel, "Maximum 50 characters");
        } else {
            clearError(nameField, nameErrorLabel);
        }
    }

    private void validateDescription() {
        String desc = descriptionField.getText().trim();
        if (desc.isEmpty()) {
            setError(descriptionField, descErrorLabel, "Description is required");
        } else if (desc.length() > 255) {
            setError(descriptionField, descErrorLabel, "Maximum 255 characters");
        } else {
            clearError(descriptionField, descErrorLabel);
        }
    }

    private void validateIcon() {
        String iconPath = iconField.getText().trim();
        if (!iconPath.isEmpty()) {
            try {
                new Image(iconPath); // Test if image is valid
                clearError(iconField, iconErrorLabel);
            } catch (Exception e) {
                setError(iconField, iconErrorLabel, "Invalid image format");
            }
        } else {
            iconErrorLabel.setText("Icon recommended");
            iconField.getStyleClass().remove("error-field");
        }
    }

    private boolean isFormValid() {
        validateName();
        validateDescription();
        validateIcon();

        return nameErrorLabel.getText().isEmpty() &&
                descErrorLabel.getText().isEmpty();
    }

    @FXML
    private void handleBrowse() {
        File selectedFile = fileChooser.showOpenDialog(iconField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                if (!isValidImageFile(selectedFile)) {
                    showAlert("Invalid Format", "Only PNG/JPEG/JPG/GIF files are allowed", Alert.AlertType.ERROR);
                    return;
                }

                if (selectedFile.length() > MAX_FILE_SIZE) {
                    showAlert("File Too Large", "Image must not exceed 2MB", Alert.AlertType.WARNING);
                    return;
                }

                File uploadDir = new File(UPLOAD_DIRECTORY);
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }

                String extension = getFileExtension(selectedFile.getName());
                String uniqueFileName = "cat_" + UUID.randomUUID() + extension;
                File destination = new File(UPLOAD_DIRECTORY + uniqueFileName);
                Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String webPath = "http://localhost/uploads/" + uniqueFileName;
                iconField.setText(webPath);

            } catch (IOException e) {
                showAlert("Upload Error", "Failed to upload image: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean isValidImageFile(File file) {
        String name = file.getName().toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

    private void updateIconPreview(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            iconPreview.setImage(null);
            noIconLabel.setVisible(true);
            return;
        }

        try {
            Image image = new Image(imageUrl);
            iconPreview.setImage(image);
            noIconLabel.setVisible(false);
        } catch (Exception e) {
            iconPreview.setImage(null);
            noIconLabel.setVisible(true);
        }
    }

    @FXML
    private void handleAdd() {
        if (!isFormValid()) {
            return;
        }

        try {
            Category category = new Category();
            category.setName(nameField.getText().trim());
            category.setDescription(descriptionField.getText().trim());
            category.setCreatedAt(LocalDateTime.now());
            category.setIsActive(isActiveCheckBox.isSelected());
            category.setIcon(iconField.getText().trim());

            categoryService.ajouter(category);



            showAlert("Success", "Category added successfully", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (Exception e) {
            showAlert("Error", "Failed to add category: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // Utility methods
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void setError(Control field, Label errorLabel, String message) {
        field.getStyleClass().add("error-field");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError(Control field, Label errorLabel) {
        field.getStyleClass().remove("error-field");
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setParentController(CategoryController controller) {
        this.parentController = controller;
    }
}