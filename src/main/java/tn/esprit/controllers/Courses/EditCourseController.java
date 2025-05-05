package tn.esprit.controllers.Courses;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.entities.Category;
import tn.esprit.entities.Courses;
import tn.esprit.services.CategoryService;
import tn.esprit.services.CoursesService;

import java.util.List;

public class EditCourseController {
    @FXML private Label idLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField pointsField;
    @FXML private CheckBox premiumCheckBox;
    @FXML private TextField tutorField;
    @FXML private CheckBox publishedCheckBox;

    // Error labels
    @FXML private Label titleError;
    @FXML private Label descriptionError;
    @FXML private Label categoryError;
    @FXML private Label pointsError;
    @FXML private Label tutorError;

    private Courses course;
    private CoursesController parentController;
    private final CategoryService categoryService = CategoryService.getInstance();
    private final CoursesService coursesService = CoursesService.getInstance();

    @FXML
    private void initialize() {
        configureCategoryComboBox();
        setupValidations();
    }

    private void configureCategoryComboBox() {
        categoryComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return categoryComboBox.getItems().stream()
                        .filter(cat -> cat.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    public void setCategories(List<Category> categories) {
        categoryComboBox.getItems().setAll(categories);
    }

    private void setupValidations() {
        pointsField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                pointsField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    public void setCourse(Courses course) {
        this.course = course;
        populateFormFields();
    }

    private void populateFormFields() {
        idLabel.setText(String.valueOf(course.getId()));
        titleField.setText(course.getTitle());
        descriptionField.setText(course.getDescription());
        categoryComboBox.getSelectionModel().select(course.getCategory());
        pointsField.setText(String.valueOf(course.getProgressPointsRequired()));
        premiumCheckBox.setSelected(course.getIsPremium());
        tutorField.setText(course.getTutorName());
        publishedCheckBox.setSelected(course.getIsPublished());
    }

    public void setParentController(CoursesController coursesController) {
        this.parentController = coursesController;
    }

    @FXML
    private void handleUpdate() {
        if (validateForm()) {
            updateCourseData();
            try {
                coursesService.modifier(course);
                notifyParentController();
                closeWindow();
            } catch (Exception e) {
                showAlert("Erreur", "Échec de la mise à jour: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void updateCourseData() {
        course.setTitle(titleField.getText().trim());
        course.setDescription(descriptionField.getText().trim());
        course.setCategory(categoryComboBox.getValue());
        course.setProgressPointsRequired(Integer.parseInt(pointsField.getText()));
        course.setIsPremium(premiumCheckBox.isSelected());
        course.setTutorName(tutorField.getText().trim());
        course.setIsPublished(publishedCheckBox.isSelected());
    }

    private boolean validateForm() {
        clearValidationErrors();
        boolean isValid = true;

        if (titleField.getText().trim().isEmpty()) {
            markError(titleField, titleError, "Le titre est obligatoire");
            isValid = false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            markError(descriptionField, descriptionError, "La description est obligatoire");
            isValid = false;
        }

        if (categoryComboBox.getValue() == null) {
            markError(categoryComboBox, categoryError, "Sélectionnez une catégorie");
            isValid = false;
        }

        if (pointsField.getText().isEmpty()) {
            markError(pointsField, pointsError, "Les points sont obligatoires");
            isValid = false;
        } else {
            try {
                int points = Integer.parseInt(pointsField.getText());
                if (points < 0 || points > 1000) {
                    markError(pointsField, pointsError, "Doit être entre 0 et 1000");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                markError(pointsField, pointsError, "Valeur numérique invalide");
                isValid = false;
            }
        }

        if (tutorField.getText().trim().isEmpty()) {
            markError(tutorField, tutorError, "Le tuteur est obligatoire");
            isValid = false;
        }

        if (!isValid) {
            showAlert("Formulaire invalide", "Veuillez corriger les erreurs", Alert.AlertType.WARNING);
        }

        return isValid;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void notifyParentController() {
        if (parentController != null) {
            parentController.refreshCoursesList();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }

    private void markError(Control field, Label errorLabel, String message) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5;");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearValidationErrors() {
        Control[] fields = {titleField, descriptionField, categoryComboBox, pointsField, tutorField};
        for (Control field : fields) {
            field.setStyle("");
        }

        Label[] errorLabels = {titleError, descriptionError, categoryError, pointsError, tutorError};
        for (Label label : errorLabels) {
            label.setVisible(false);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}