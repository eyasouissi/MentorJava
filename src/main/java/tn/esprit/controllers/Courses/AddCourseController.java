package tn.esprit.controllers.Courses;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.entities.Category;
import tn.esprit.entities.Courses;
import tn.esprit.services.CoursesService;

import java.util.List;

public class AddCourseController {
    // Form fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField pointsField;
    @FXML private CheckBox premiumCheckBox;
    @FXML private CheckBox publishedCheckBox;
    @FXML private TextField tutorField;

    // Error labels
    @FXML private Label titleError;
    @FXML private Label descriptionError;
    @FXML private Label categoryError;
    @FXML private Label pointsError;
    @FXML private Label tutorError;

    private CoursesController parentController;
    private final CoursesService coursesService = CoursesService.getInstance();

    @FXML
    private void initialize() {
        configureCategoryComboBox();
        setupFieldValidations();
        setupFormListeners();
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

    private void setupFieldValidations() {
        // Only allow numbers in points field
        pointsField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                pointsField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            validatePointsField();
        });
    }

    private void setupFormListeners() {
        titleField.textProperty().addListener((obs, oldVal, newVal) -> validateTitleField());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescriptionField());
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateCategoryField());
        tutorField.textProperty().addListener((obs, oldVal, newVal) -> validateTutorField());
    }

    public void setCategories(List<Category> categories) {
        categoryComboBox.getItems().setAll(categories);
    }

    @FXML
    private void handleSave() {
        if (validateForm()) {
            try {
                Courses newCourse = createCourseFromInput();
                coursesService.ajouter(newCourse);
                notifyParentController();
                closeWindow();
            } catch (Exception e) {
                showError("Critical Error", "Failed to create course: " + e.getMessage());
            }
        }
    }

    private Courses createCourseFromInput() {
        Courses course = new Courses();
        course.setTitle(titleField.getText().trim());
        course.setDescription(descriptionField.getText().trim());
        course.setCategory(categoryComboBox.getValue());
        course.setProgressPointsRequired(Integer.parseInt(pointsField.getText()));
        course.setIsPremium(premiumCheckBox.isSelected());
        course.setIsPublished(publishedCheckBox.isSelected());
        course.setTutorName(tutorField.getText().trim());
        return course;
    }

    private boolean validateForm() {
        boolean titleValid = validateTitleField();
        boolean descValid = validateDescriptionField();
        boolean categoryValid = validateCategoryField();
        boolean pointsValid = validatePointsField();
        boolean tutorValid = validateTutorField();

        return titleValid && descValid && categoryValid && pointsValid && tutorValid;
    }

    private boolean validateTitleField() {
        boolean isValid = !titleField.getText().trim().isEmpty();
        setFieldState(titleField, titleError, isValid, "Title is required");
        return isValid;
    }

    private boolean validateDescriptionField() {
        boolean isValid = !descriptionField.getText().trim().isEmpty();
        setFieldState(descriptionField, descriptionError, isValid, "Description is required");
        return isValid;
    }

    private boolean validateCategoryField() {
        boolean isValid = categoryComboBox.getValue() != null;
        setFieldState(categoryComboBox, categoryError, isValid, "Please select a category");
        return isValid;
    }

    private boolean validatePointsField() {
        if (pointsField.getText().isEmpty()) {
            setFieldState(pointsField, pointsError, false, "Points are required");
            return false;
        }

        try {
            int points = Integer.parseInt(pointsField.getText());
            boolean isValid = points >= 0 && points <= 1000;
            setFieldState(pointsField, pointsError, isValid, "Must be between 0-1000");
            return isValid;
        } catch (NumberFormatException e) {
            setFieldState(pointsField, pointsError, false, "Invalid number");
            return false;
        }
    }

    private boolean validateTutorField() {
        boolean isValid = !tutorField.getText().trim().isEmpty();
        setFieldState(tutorField, tutorError, isValid, "Tutor name is required");
        return isValid;
    }

    private void setFieldState(Control field, Label errorLabel, boolean isValid, String errorMessage) {
        if (isValid) {
            field.getStyleClass().remove("error-field");
            errorLabel.setVisible(false);
        } else {
            field.getStyleClass().add("error-field");
            errorLabel.setText(errorMessage);
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void notifyParentController() {
        if (parentController != null) {
            parentController.refreshCoursesList();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setParentController(CoursesController controller) {
        this.parentController = controller;
    }
}