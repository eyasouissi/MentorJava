package tn.esprit.controllers.Courses;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.Category;
import tn.esprit.entities.Courses;
import tn.esprit.entities.Level;
import tn.esprit.entities.User;
import tn.esprit.services.CategoryService;
import tn.esprit.services.CoursesService;
import tn.esprit.services.EmailSender;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainCourseController implements Initializable {

    @FXML private Button allCoursesButton;
    @FXML private Button premiumCoursesButton;
    @FXML private Button popularCoursesButton;
    @FXML private HBox categoriesContainer;
    @FXML private FlowPane coursesContainer;
    @FXML private Label currentCategoryLabel;
    @FXML private Label courseCountLabel;
    @FXML private VBox mainView;
    @FXML private VBox addCourseView;
    @FXML private TextField courseTitleField;
    @FXML private TextArea courseDescriptionField;
    @FXML private ComboBox<String> categoryComboBox; // Changed to String type
    @FXML private TextField tutorField;
    @FXML private CheckBox premiumCheckBox;
    @FXML private ListView<Level> levelsListView;
    @FXML private TextField levelNameField;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button prevCategoryButton;
    @FXML private Button nextCategoryButton;
    @FXML private ScrollPane categoriesScrollPane;
    @FXML private Button manualEntryBtn;
    @FXML private Button saveButton;
    @FXML private Button addButton; // Add this line if missing


    private final CoursesService coursesService = CoursesService.getInstance();
    private final CategoryService categoryService = CategoryService.getInstance();
    private final List<Level> tempLevels = new ArrayList<>();
    private List<Category> allCategories; // To store all categories

    // Pagination variables
    private int currentPage = 0;
    private final int itemsPerPage = 6;
    private int totalCourses = 0;
    private String currentFilter = "all";
    private Category currentCategory = null;
    private User currentUser;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier la session utilisateur
        checkUserSession();

        // Le reste de votre initialisation existante
        if (currentCategoryLabel == null) {
            currentCategoryLabel = new Label();
        }
        if (courseCountLabel == null) {
            courseCountLabel = new Label();
        }

        loadCategories();
        loadAllCourses();
        initializeCategoryComboBox();

        prevCategoryButton.getStyleClass().add("nav-button");
        nextCategoryButton.getStyleClass().add("nav-button");

        categoriesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        categoriesScrollPane.setFitToHeight(true);
    }



    private void checkUserSession() {
        this.currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        } else {
            updateUIForUserRole();
        }
    }

    private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/login.fxml"));
            Stage stage = (Stage) coursesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void showAddCourseForm() {
        if (currentUser == null) {
            checkUserSession(); // Vérifier à nouveau la session
            return;
        }

        if (!currentUser.getRoles().contains("ROLE_TUTOR")) {
            showAlert("Access Denied", "Only tutors can add courses");
            return;
        }

        // Initialisation du formulaire avec l'utilisateur de la session
        initializeCategoryComboBox();
        levelsListView.getItems().clear();
        tempLevels.clear();

        tutorField.setText(currentUser.getName());
        tutorField.setDisable(true);

        courseTitleField.clear();
        courseDescriptionField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        premiumCheckBox.setSelected(false);
        levelNameField.clear();

        setFormFieldsEditable(false);
        manualEntryBtn.setVisible(true);
        saveButton.setVisible(false);

        mainView.setVisible(false);
        addCourseView.setVisible(true);
    }

    private void updateUIForUserRole() {
        if (addButton != null) { // Vérification ajoutée ici
            if (currentUser != null && currentUser.getRoles().contains("ROLE_TUTOR")) {
                addButton.setVisible(true);
            } else {
                addButton.setVisible(false);
            }
        }
    }

    private void initializeCategoryComboBox() {
        allCategories = categoryService.getAll();
        categoryComboBox.getItems().clear();
        // Add category names to the combo box
        for (Category category : allCategories) {
            categoryComboBox.getItems().add(category.getName());
        }
    }

    private void scrollCategories(int direction) {
        double scrollAmount = categoriesScrollPane.getHvalue();
        double newScrollAmount = scrollAmount + (direction * 0.2);
        newScrollAmount = Math.max(0, Math.min(1, newScrollAmount));
        categoriesScrollPane.setHvalue(newScrollAmount);

        prevCategoryButton.setDisable(newScrollAmount <= 0);
        nextCategoryButton.setDisable(newScrollAmount >= 1);
    }

    private void loadCategories() {
        categoriesContainer.getChildren().clear();

        // "All" card with image icon
        VBox allCard = new VBox(10);
        allCard.getStyleClass().add("category-card");
        allCard.setAlignment(Pos.CENTER);

        ImageView allIcon = new ImageView();
        try {
            allIcon.setImage(new Image(getClass().getResourceAsStream("/images/all-icon.jpg")));
        } catch (Exception e) {
            allIcon.setImage(getDefaultIcon());
        }
        allIcon.setFitWidth(80);
        allIcon.setFitHeight(80);
        allIcon.setPreserveRatio(true);
        allIcon.getStyleClass().add("category-icon");

        Label allLabel = new Label("All");
        allLabel.setStyle("-fx-font-weight: bold;");

        allCard.getChildren().addAll(allIcon, allLabel);
        allCard.setOnMouseClicked(e -> loadAllCourses());
        categoriesContainer.getChildren().add(allCard);

        // Category cards with image icons
        for (Category category : categoryService.getAll()) {
            VBox card = new VBox(10);
            card.getStyleClass().add("category-card");
            card.setAlignment(Pos.CENTER);

            ImageView icon = new ImageView();
            icon.getStyleClass().add("category-icon");
            try {
                if (category.getIcon() != null && !category.getIcon().isEmpty()) {
                    icon.setImage(new Image(category.getIcon()));
                } else {
                    icon.setImage(getDefaultIcon());
                }
            } catch (Exception e) {
                icon.setImage(getDefaultIcon());
            }
            icon.setFitWidth(80);
            icon.setFitHeight(80);
            icon.setPreserveRatio(true);

            Label name = new Label(category.getName());
            name.setStyle("-fx-font-weight: bold;");

            card.getChildren().addAll(icon, name);
            card.setOnMouseClicked(e -> loadCoursesByCategory(category));
            categoriesContainer.getChildren().add(card);
        }

        prevCategoryButton.setOnAction(e -> scrollCategories(-1));
        nextCategoryButton.setOnAction(e -> scrollCategories(1));
    }

    private Image getDefaultIcon() {
        try {
            return new Image(getClass().getResourceAsStream("/interfaces/Category/images/all-icon.jpg"));
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void loadAllCourses() {
        currentCategory = null;
        currentFilter = "all";
        currentPage = 0;
        totalCourses = coursesService.getCount();
        loadCoursesForCurrentPage();
        setButtonStyles("all");
    }

    @FXML
    private void loadPremiumCourses() {
        currentCategory = null;
        currentFilter = "premium";
        currentPage = 0;
        totalCourses = coursesService.getPremiumCount();
        loadCoursesForCurrentPage();
        setButtonStyles("premium");
    }

    @FXML
    private void loadPopularCourses() {
        currentCategory = null;
        currentFilter = "popular";
        currentPage = 0;
        totalCourses = coursesService.getPopularCount();
        loadCoursesForCurrentPage();
        setButtonStyles("popular");
    }

    @FXML
    private void loadCoursesByCategory(Category category) {
        currentCategory = category;
        currentFilter = "category";
        currentPage = 0;
        totalCourses = coursesService.getCountByCategory(category.getId());
        loadCoursesForCurrentPage();
        setButtonStyles("all");
    }

    private void loadCoursesForCurrentPage() {
        List<Courses> courses;
        String categoryText = "";

        switch (currentFilter) {
            case "premium":
                courses = coursesService.getPremiumCoursesPaginated(currentPage, itemsPerPage);
                categoryText = "Premium Courses";
                break;
            case "popular":
                courses = coursesService.getPopularCoursesPaginated(currentPage, itemsPerPage);
                categoryText = "Popular Courses";
                break;
            case "category":
                courses = coursesService.getByCategoryPaginated(currentCategory.getId(), currentPage, itemsPerPage);
                categoryText = currentCategory.getName() + " Courses";
                break;
            default:
                courses = coursesService.getAllPaginated(currentPage, itemsPerPage);
                categoryText = "All Courses";
                break;
        }

        if (currentCategoryLabel != null) {
            currentCategoryLabel.setText(categoryText);
        }

        displayCourses(courses);
        updatePageInfo();
    }

    private void displayCourses(List<Courses> courses) {
        coursesContainer.getChildren().clear();
        if (courseCountLabel != null) {
            courseCountLabel.setText(courses.size() + " of " + totalCourses + " courses");
        }

        for (Courses course : courses) {
            VBox courseCard = createCourseCard(course);
            coursesContainer.getChildren().add(courseCard);
        }
    }


    @FXML
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadCoursesForCurrentPage();
        }
    }

    @FXML
    private void nextPage() {
        if ((currentPage + 1) * itemsPerPage < totalCourses) {
            currentPage++;
            loadCoursesForCurrentPage();
        }
    }

    private void updatePageInfo() {
        int totalPages = (int) Math.ceil((double) totalCourses / itemsPerPage);
        pageInfoLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);

        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable((currentPage + 1) * itemsPerPage >= totalCourses);
    }

    private void setButtonStyles(String activeButton) {
        String activeStyle = "-fx-background-color: #4B0082; -fx-text-fill: white; -fx-background-radius: 5;";
        String inactiveStyle = "-fx-background-color: #D8BFD8; -fx-text-fill: #4B0082; -fx-background-radius: 5;";

        allCoursesButton.setStyle(activeButton.equals("all") ? activeStyle : inactiveStyle);
        premiumCoursesButton.setStyle(activeButton.equals("premium") ? activeStyle : inactiveStyle);
        popularCoursesButton.setStyle(activeButton.equals("popular") ? activeStyle : inactiveStyle);
    }

    public void initializeWithUser(User user) {
        Platform.runLater(() -> {
            this.currentUser = user;
            System.out.println("User set: " + (user != null ? user.getName() : "null"));
            updateUIForUserRole();
        });
    }


    @FXML
    private void cancelAddCourse() {
        // Réinitialiser tous les champs
        courseTitleField.clear();
        courseDescriptionField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        tutorField.clear();
        premiumCheckBox.setSelected(false);
        levelsListView.getItems().clear();
        tempLevels.clear();
        levelNameField.clear();

        // Retour à la vue principale
        addCourseView.setVisible(false);
        mainView.setVisible(true);
    }

    // Modifiez la méthode checkSessionBeforeAction
    private boolean checkSessionBeforeAction() {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert("Session Expired", "Your session has expired. Please login again.");
            redirectToLogin();
            return false;
        }
        return true;
    }
    @FXML
    private void addLevel() {
        String levelName = levelNameField.getText().trim();
        if (!levelName.isEmpty()) {
            Level level = new Level(levelName, null);
            tempLevels.add(level);
            levelsListView.getItems().add(level);
            levelNameField.clear();
        }
    }

    @FXML
    private void saveCourse() {
        // Validation des champs obligatoires
        if (courseTitleField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter a course title");
            return;
        }

        if (categoryComboBox.getValue() == null) {
            showAlert("Error", "Please select a category");
            return;
        }

        if (tempLevels.isEmpty()) {
            showAlert("Error", "Please add at least one level");
            return;
        }

        // Trouver la catégorie sélectionnée
        String selectedCategoryName = categoryComboBox.getValue();
        Category selectedCategory = allCategories.stream()
                .filter(c -> c.getName().equals(selectedCategoryName))
                .findFirst()
                .orElse(null);

        if (selectedCategory == null) {
            showAlert("Error", "Invalid category selected");
            return;
        }

        // Création du nouveau cours
        Courses newCourse = new Courses();
        newCourse.setTitle(courseTitleField.getText().trim());
        newCourse.setDescription(courseDescriptionField.getText().trim());
        newCourse.setCategory(selectedCategory);
        newCourse.setTutorName(tutorField.getText()); // Nom du tuteur pré-rempli
        newCourse.setIsPremium(premiumCheckBox.isSelected());

        // Ajouter les niveaux
        tempLevels.forEach(newCourse::addLevel);

        // Sauvegarde du cours
        try {
            coursesService.ajouter(newCourse);

            // Envoyer une notification
            sendEmailToAdminWithAlert(newCourse.getTitle(), newCourse.getTutorName());

            // Retour à la vue principale
            cancelAddCourse();
            loadAllCourses();

            // Confirmation
            showSweetAlert(Alert.AlertType.INFORMATION,
                    "Success",
                    "Course added successfully!",
                    FontAwesomeSolid.CHECK_CIRCLE);

        } catch (Exception e) {
            showSweetAlert(Alert.AlertType.ERROR,
                    "Error",
                    "Failed to save course: " + e.getMessage(),
                    FontAwesomeSolid.EXCLAMATION_TRIANGLE);
        }
    }

    private void sendEmailToAdmin(String courseTitle, String tutorName) {
        String recipient = "esouissi870@gmail.com";
        String subject = "New course added: " + courseTitle;
        String htmlContent = "<html><body>"
                + "<h2 style='color: #2E86C1;'>A new course has been added!</h2>"
                + "<p><strong>Title:</strong> " + courseTitle + "</p>"
                + "<p><strong>Instructor:</strong> " + tutorName + "</p>"
                + "<p>Please review the course content.</p>"
                + "</body></html>";

        EmailSender.sendEmail(recipient, subject, htmlContent);
    }

    private void sendEmailToAdminWithAlert(String courseTitle, String tutorName) {
        try {
            sendEmailToAdmin(courseTitle, tutorName);

            // Success SweetAlert
            showSweetAlert(Alert.AlertType.INFORMATION,
                    "Success",
                    "Email sent to administrator",
                    FontAwesomeSolid.ENVELOPE);
        } catch (Exception e) {
            showSweetAlert(Alert.AlertType.ERROR,
                    "Error",
                    "Failed to send email: " + e.getMessage(),
                    FontAwesomeSolid.EXCLAMATION_TRIANGLE);
        }
    }

    private void showSweetAlert(Alert.AlertType type, String title, String message, FontAwesomeSolid icon) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(24);
        alert.setGraphic(fontIcon);

        alert.showAndWait();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void AIGenerator() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Courses/AIView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("AI Course Generator");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load the AI Generator");
            alert.setContentText("The AI Generator interface could not be loaded. Please check the file path.");
            alert.showAndWait();
        }
    }

    @FXML
    private void showManualForm() {
        manualEntryBtn.setVisible(false);
        saveButton.setVisible(true);
        setFormFieldsEditable(true); // Active tous les champs sauf tutor
    }

    private void setFormFieldsEditable(boolean editable) {
        courseTitleField.setDisable(!editable);
        courseDescriptionField.setDisable(!editable);
        categoryComboBox.setDisable(!editable);
        premiumCheckBox.setDisable(!editable);
        levelNameField.setDisable(!editable);
        // Note: tutorField reste toujours désactivé
    }

    public void setAIGeneratedContent(String title, String description, Category category) {
        if (title == null || description == null) {
            showAlert("AI Error", "The AI failed to generate content");
            return;
        }

        Platform.runLater(() -> {
            courseTitleField.setText(title);
            courseDescriptionField.setText(description);
            if (category != null) {
                categoryComboBox.getSelectionModel().select(category.getName());
            }

            manualEntryBtn.setVisible(true);
            saveButton.setVisible(true);
            setFormFieldsEditable(true);
        });
    }



    /////////////////////////////////////////////
////////////////Patie emna//////////////////////////
///////////////////////////////////////////////////////////////


    private void openCourseDetails(Courses course) {
        try {
            if (course.getIsPremium()) {
                // Redirection vers la page d'abonnement pour les cours premium
                redirectToSubscriptionPage(course);
            } else {
                // Affichage normal pour les cours gratuits
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Courses/CourseDetailsView.fxml"));
                Parent root = loader.load();

                CourseDetailsController controller = loader.getController();
                controller.setCourse(course);

                Stage stage = new Stage();
                stage.setTitle("Course Details");
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open course details");
        }
    }

    private void redirectToSubscriptionPage(Courses course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Courses/SubscriptionRequired.fxml"));
            Parent root = loader.load();

            // Passer les données du cours si nécessaire
            SubscriptionRequiredController controller = loader.getController();
            controller.setCourseTitle(course.getTitle());

            Stage stage = new Stage();
            stage.setTitle("Subscription Required");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load subscription page");
        }
    }

    private VBox createCourseCard(Courses course) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #D8BFD8; -fx-border-radius: 10;");
        card.setPrefSize(250, 160);

        Label titleLabel = new Label(course.getTitle() != null ? course.getTitle() : "Untitled Course");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B0082; -fx-wrap-text: true;");

        Label tutorLabel = new Label("By " + (course.getTutorName() != null ? course.getTutorName() : "Unknown"));
        tutorLabel.setStyle("-fx-text-fill: #9370DB; -fx-font-size: 12px;");

        HBox footer = new HBox(10);
        Label ratingLabel = new Label(String.format("★ %.1f", course.getAverageRating()));
        ratingLabel.setStyle("-fx-text-fill: #f39c12;");

        Label premiumLabel = new Label(course.getIsPremium() ? "PREMIUM" : "FREE");
        premiumLabel.setStyle(course.getIsPremium()
                ? "-fx-text-fill: #4B0082; -fx-font-weight: bold; -fx-border-color: #4B0082; -fx-border-width: 1; -fx-padding: 2 5;"
                : "-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        footer.getChildren().addAll(ratingLabel, premiumLabel);
        card.getChildren().addAll(titleLabel, tutorLabel, footer);

        // Ajouter un effet au survol pour indiquer que c'est cliquable
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f0e6f0; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #4B0082; -fx-border-radius: 10;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #D8BFD8; -fx-border-radius: 10;"));

        card.setOnMouseClicked(event -> openCourseDetails(course));

        return card;
    }
    /////////////////////////////////////////////
//////////////////////////////////////////
///////////////////////////////////////////////////////////////
}