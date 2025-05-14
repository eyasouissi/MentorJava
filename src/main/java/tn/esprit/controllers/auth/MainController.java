package tn.esprit.controllers.auth;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.controllers.Courses.MainCourseController;
import tn.esprit.controllers.Front.SidebarController;
import tn.esprit.controllers.PostController;
import tn.esprit.controllers.RoomPageController;
import tn.esprit.controllers.user.ProfileController;
import tn.esprit.controllers.user.admin.AdminProfileController;
import tn.esprit.entities.*;
import tn.esprit.services.AnnonceService;
import tn.esprit.services.CategoryService;
import tn.esprit.services.RoomService;
import tn.esprit.tools.MyDataBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
// Add at the top of MainController.java
import java.time.LocalDateTime;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MainController {
    // UI Components
    @FXML
    private Label welcomeLabel;
    @FXML
    private VBox coursesDropdown;
    @FXML
    private VBox eventsDropdown;
    @FXML
    private VBox pricingDropdown;
    @FXML
    private VBox sidebarContainer;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label motivationLabel;
    @FXML
    private Text welcomeText;
    @FXML
    private ImageView motivationImage;
    @FXML
    private FlowPane mainRecommendationsFlowPane;
    // In the FXML injections
    @FXML
    private HBox categoriesBox;
    @FXML
    private ScrollPane categoriesScroll;
    @FXML
    private Button prevCatButton;
    @FXML
    private Button nextCatButton;

    // Data Components
    private User currentUser;
    private final ObservableList<Forum> recentForums = FXCollections.observableArrayList();
    private final ObservableList<Forum> forumList = FXCollections.observableArrayList();
    private static final int MAX_RECENT = 5;
    private static final int MAX_RECOMMENDATIONS = 8;
    @FXML
    private HBox userRoomContainer;
    private final RoomService roomService = new RoomService();
    @FXML
    private VBox latestAnnonceContainer;
    private final AnnonceService annonceService = new AnnonceService();
    // In your MainController class
    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label monthYear;
    @FXML
    private Button prevMonth;
    @FXML
    private Button nextMonth;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label timeLabel;

    private long totalSeconds = 0;
    private Timeline timeline;
    private LocalDate currentDate;
    private String timeFile;
    private final Map<LocalDate, List<String>> schedules = new HashMap<>();
    // FXML Paths
    // Badge configuration
    private static final Map<Long, String> BADGES = new LinkedHashMap<>();
    @FXML
    private BorderPane rootPane;
    @FXML
    private ImageView posterImage;

    // Add this with other data components
    private MediaPlayer mediaPlayer;

    static {
        // First badge after 5 minutes (300 seconds)
        BADGES.put(300L, "/badges/badge1.png");

        // Second badge after 1 hour (3600 seconds)
        BADGES.put(3600L, "/badges/badge2.png");

        // Subsequent badges with 5-hour increments (18000 seconds)
        long currentThreshold = 3600L;
        for(int i = 3; i <= 24; i++) {
            currentThreshold += 5 * 3600; // Add 5 hours in seconds
            BADGES.put(currentThreshold, "/badges/badge" + i + ".png");
        }
    }
    private final Set<Long> unlockedBadges = new HashSet<>();
    @FXML
    private HBox badgesContainer;

    private static final Map<String, String> FXML_PATHS = new HashMap<>();

    static {
        FXML_PATHS.put("Home", "/interfaces/Front/home.fxml");
        FXML_PATHS.put("Courses", "/interfaces/courses/courses.fxml");
        FXML_PATHS.put("Category", "/interfaces/courses/category.fxml");
        FXML_PATHS.put("Forum", "/interfaces/community/forum.fxml");
        FXML_PATHS.put("Groupes", "/interfaces/community/groupes.fxml");
        FXML_PATHS.put("Events", "/interfaces/events/events.fxml");
        FXML_PATHS.put("Announcements", "/interfaces/announcements/announcements.fxml");
        FXML_PATHS.put("Pricing", "/interfaces/pricing/pricing.fxml");
        FXML_PATHS.put("Subscription", "/interfaces/pricing/subscription.fxml");
    }

    // Initialization
    public void initializeWithUser(User user, String message) {
        this.currentUser = user;
        initializeUIElements(message);
        setupRecommendationEngine();
        loadForums();
        loadRecommendations();
        setupSidebar();
        loadRandomCategories();
        displayRandomUserRoom();
        displayLatestAnnonce();
        setupAnnonceListeners();
        initializeCalendar();
        this.timeFile = "study_time_" + user.getId() + ".dat";
        initializeTimeTracking();
        initializeBackgroundAudio();

        // In your MainController.java
        contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Get the root BorderPane
                BorderPane rootPane = (BorderPane) newScene.getRoot();
                // Access center content if needed
                StackPane centerPane = (StackPane) rootPane.getCenter();
            }
        });

        // Rest of your existing initialization code...
        initializeUIElements(message);
        setupRecommendationEngine();


        try {
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/interfaces/Front/Sidebar.fxml"));
            Parent sidebar = sidebarLoader.load();
            SidebarController sidebarController = sidebarLoader.getController();

            sidebarController.setCurrentUser(user);
            sidebarController.setContentArea(contentArea);

            if (sidebarContainer != null) {
                sidebarContainer.getChildren().clear();
                sidebarContainer.getChildren().add(sidebar);

                // Defer the binding until the VBox is attached to the Scene
                sidebarContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        sidebarContainer.prefWidthProperty().bind(
                                newScene.widthProperty().multiply(0.2).add(40)
                        );
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initializeUIElements(String message) {
        if (welcomeLabel != null && currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName() + "! " + message);
        }
        welcomeText.setText("Welcome, " + currentUser.getName() + "!");
        setupMotivationElements();
        hideAllDropdowns();
        
        // Apply hover effects to elements
        applyHoverEffects();
    }

    /**
     * Apply hover effects to the main UI elements
     */
    private void applyHoverEffects() {
        try {
            // Apply style class and hover animation to recommendations container if it exists
            if (mainRecommendationsFlowPane != null && mainRecommendationsFlowPane.getParent() != null) {
                VBox recommendationsContainer = (VBox) mainRecommendationsFlowPane.getParent();
                recommendationsContainer.getStyleClass().add("recommendations-container");
                setupHoverAnimation(recommendationsContainer);
            }
            
            // Apply style class and hover animation to latest announcement container if it exists
            if (latestAnnonceContainer != null) {
                latestAnnonceContainer.getStyleClass().add("announcement-container");
                setupHoverAnimation(latestAnnonceContainer);
            }
            
            // Apply style class and hover animation to poster image container if it exists
            if (posterImage != null && posterImage.getParent() != null) {
                StackPane imageContainer = (StackPane) posterImage.getParent();
                imageContainer.getStyleClass().add("poster-image-container");
                setupHoverAnimation(imageContainer);
            }
        } catch (Exception e) {
            System.err.println("Error applying hover effects: " + e.getMessage());
        }
    }
    
    /**
     * Creates scale and shadow animations for hover effects
     * @param node The node to apply animations to
     */
    private void setupHoverAnimation(Node node) {
        if (node == null) return;
        
        try {
            // Create scale transition animations
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), node);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), node);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            
            // Remove any existing handlers to avoid duplication
            node.setOnMouseEntered(null);
            node.setOnMouseExited(null);
            
            // Add mouse enter event
            node.setOnMouseEntered(e -> {
                scaleDown.stop(); // Stop any running animations
                scaleUp.playFromStart();
                playClickSound();
            });
            
            // Add mouse exit event
            node.setOnMouseExited(e -> {
                scaleUp.stop(); // Stop any running animations
                scaleDown.playFromStart();
            });
        } catch (Exception e) {
            System.err.println("Error setting up hover animation for " + node + ": " + e.getMessage());
        }
    }

    // Recommendation System
    private void setupRecommendationEngine() {
        recentForums.addListener((ListChangeListener<Forum>) c -> updateRecommendations());
    }

    private void updateRecommendations() {
        mainRecommendationsFlowPane.getChildren().clear();
        getRecommendedForums().forEach(forum -> {
            Label bubble = createRecommendationBubble(forum);
            mainRecommendationsFlowPane.getChildren().add(bubble);
        });
    }

    private void loadForums() {
        String sql = "SELECT f.*, (SELECT COUNT(*) FROM post p WHERE p.forum_id = f.id) AS total_posts " +
                "FROM forum f WHERE f.is_public = true";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Forum forum = new Forum();
                forum.setId(rs.getLong("id"));
                forum.setTitle(rs.getString("title"));
                forum.setDescription(rs.getString("description"));
                forum.setTopics(rs.getString("topics"));
                forum.setViews(rs.getInt("views"));
                forum.setTotalPosts(rs.getInt("total_posts"));
                forumList.add(forum);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading forums: " + e.getMessage());
        }
    }

    private List<Forum> getRecommendedForums() {
        Map<Forum, Integer> forumScores = new HashMap<>();
        Set<String> recentTopics = getRecentTopics();

        forumList.forEach(forum -> {
            if (!recentForums.contains(forum)) {
                int score = calculateSimilarityScore(forum.getTopics(), recentTopics);
                forumScores.put(forum, score);
            }
        });

        return forumScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_RECOMMENDATIONS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private int calculateSimilarityScore(String forumTopics, Set<String> recentTopics) {
        Set<String> ft = new HashSet<>(Arrays.asList(forumTopics.split(",")));
        ft.retainAll(recentTopics);
        return ft.size();
    }

    private Set<String> getRecentTopics() {
        return recentForums.stream()
                .map(Forum::getTopics)
                .flatMap(topics -> Arrays.stream(topics.split(",")))
                .map(String::trim)
                .filter(topic -> !topic.isEmpty())
                .collect(Collectors.toSet());
    }

    private Label createRecommendationBubble(Forum forum) {
        Label bubble = new Label(forum.getTitle());
        bubble.getStyleClass().add("recommendation-bubble");
        bubble.setOnMouseClicked(e -> showForumDetails(forum));
        return bubble;
    }

    // Navigation Methods
    private void showForumDetails(Forum forum) {
        try {
            // Get reference to the current stage
            Stage currentStage = (Stage) mainRecommendationsFlowPane.getScene().getWindow();

            // Load the PostView content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/PostView.fxml"));
            Parent root = loader.load();

            // Configure the controller
            PostController controller = loader.getController();
            controller.setForum(forum);
            controller.setCurrentUser(currentUser);

            // Replace the current scene content
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Posts in " + forum.getTitle());

        } catch (IOException e) {
            showAlert("Navigation Error", "Could not load forum details: " + e.getMessage());
        }
    }

    // User Management
    public void updateUserInfo(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + "!");
        }

        if (sidebarContainer != null && !sidebarContainer.getChildren().isEmpty()) {
            Node sidebar = sidebarContainer.getChildren().get(0);
            if (sidebar.getUserData() instanceof SidebarController) {
                SidebarController sidebarController = (SidebarController) sidebar.getUserData();
                sidebarController.setCurrentUser(user);
                sidebarController.updateProfileImage();
            }
        }
    }

    public void notifyProfilePictureUpdated(String newPfpPath) {
        if (sidebarContainer != null && !sidebarContainer.getChildren().isEmpty()) {
            Node sidebar = sidebarContainer.getChildren().get(0);
            if (sidebar.getUserData() instanceof SidebarController) {
                SidebarController sidebarController = (SidebarController) sidebar.getUserData();
                if (currentUser != null) {
                    currentUser.setPfp(newPfpPath);
                }
                sidebarController.updateProfileImage();
            }
        }
    }

    // UI Interactions
    private void setupMotivationElements() {
        if (motivationLabel != null) {
            Random rand = new Random();
            motivationLabel.setText(studyPhrases[rand.nextInt(studyPhrases.length)]);
        }
        if (motivationImage != null) {
            Random rand = new Random();
            String imagePath = imagePaths[rand.nextInt(imagePaths.length)];
            motivationImage.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        }
    }

    private void setupSidebar() {
        try {
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/interfaces/Front/Sidebar.fxml"));
            Parent sidebar = sidebarLoader.load();
            SidebarController sidebarController = sidebarLoader.getController();

            sidebarController.setCurrentUser(currentUser);
            sidebarController.setContentArea(contentArea);

            if (sidebarContainer != null) {
                sidebarContainer.getChildren().clear();
                sidebarContainer.getChildren().add(sidebar);

                sidebarContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        sidebarContainer.prefWidthProperty().bind(
                                newScene.widthProperty().multiply(0.2).add(40)
                        );
                    }
                });
            }
        } catch (IOException e) {
            showAlert("UI Error", "Could not load sidebar: " + e.getMessage());
        }
    }

    private void hideAllDropdowns() {
        if (coursesDropdown != null) coursesDropdown.setVisible(false);
        if (eventsDropdown != null) eventsDropdown.setVisible(false);
        if (pricingDropdown != null) pricingDropdown.setVisible(false);
    }

    @FXML
    private void toggleCoursesDropdown() {
        coursesDropdown.setVisible(!coursesDropdown.isVisible());
        if (coursesDropdown.isVisible()) {
            eventsDropdown.setVisible(false);
            pricingDropdown.setVisible(false);
        }
    }

    @FXML
    private void toggleEventsDropdown() {
        eventsDropdown.setVisible(!eventsDropdown.isVisible());
        if (eventsDropdown.isVisible()) {
            coursesDropdown.setVisible(false);
            pricingDropdown.setVisible(false);
        }
    }

    @FXML
    private void togglePricingDropdown() {
        pricingDropdown.setVisible(!pricingDropdown.isVisible());
        if (pricingDropdown.isVisible()) {
            coursesDropdown.setVisible(false);
            eventsDropdown.setVisible(false);
        }
    }

    // Navigation Controls
    @FXML
    private void navigateToCourses() {
        loadContent("Courses");
    }

    @FXML
    private void navigateToCategory() {
        loadContent("Category");
    }

    @FXML
    private void navigateToForum() {
        loadContent("Forum");
    }

    @FXML
    private void navigateToGroupes() {
        loadContent("Groupes");
    }

    @FXML
    private void navigateToEvents() {
        loadContent("Events");
    }

    @FXML
    private void navigateToAnnouncements() {
        loadContent("Announcements");
    }

    @FXML
    private void navigateToPricing() {
        loadContent("Pricing");
    }

    @FXML
    private void navigateToSubscription() {
        loadContent("Subscription");
    }

    private void loadContent(String viewName) {
        try {
            String fxmlPath = FXML_PATHS.get(viewName);
            if (fxmlPath != null && contentArea != null) {
                Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(content);
            }
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not load view: " + e.getMessage());
        }
    }

    // User Actions
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Logout Error", "Could not load login screen: " + e.getMessage());
        }
    }

    @FXML
    private void goToProfile(ActionEvent event) {
        try {
            String fxmlPath = determineProfilePath();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            setupProfileController(loader, fxmlPath);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User Profile");
            stage.show();
        } catch (IOException e) {
            showAlert("Profile Error", "Could not load profile: " + e.getMessage());
        }
    }

    private String determineProfilePath() {
        if (currentUser != null && currentUser.getRoles() != null &&
                currentUser.getRoles().contains("ROLE_ADMIN")) {
            return "/interfaces/user/admin/adminprofile.fxml";
        }
        return "/interfaces/user/profile.fxml";
    }

    private void setupProfileController(FXMLLoader loader, String fxmlPath) {
        if (fxmlPath.contains("adminprofile")) {
            AdminProfileController adminController = loader.getController();
            adminController.setUserData(currentUser);
        } else {
            ProfileController profileController = loader.getController();
            profileController.setUserData(currentUser);
            profileController.setMainController(this);
        }
    }

    // Utilities
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Constants
    private final String[] studyPhrases = {
            "Grab your notes and get in the zone — it's time to focus.",
            "Take a deep breath, clear your mind — you're ready to learn.",
            "Set your goals for this session — every small step counts.",
            "Eliminate distractions and sharpen your focus — let's begin.",
            "You've got this — now open your book and dive in."
    };

    private final String[] imagePaths = {
            "/assets/icons/6.png",
            "/assets/icons/2.png",
            "/assets/icons/5.png"
    };

    private void loadRecommendations() {
        // Change from fetchRecommendedForums() to getRecommendedForums()
        List<Forum> recommendedForums = getRecommendedForums(); // Corrected method name
        mainRecommendationsFlowPane.getChildren().clear();
        recommendedForums.forEach(forum -> {
            Label bubble = createRecommendationBubble(forum);
            mainRecommendationsFlowPane.getChildren().add(bubble);
        });
    }

    private void loadRandomCategories() {
        CategoryService categoryService = new CategoryService();
        List<Category> allCategories = categoryService.getAll();

        // Shuffle the categories
        Collections.shuffle(allCategories);

        // Clear previous categories
        categoriesBox.getChildren().clear();

        // Add category cards
        allCategories.forEach(category -> {
            VBox card = createCategoryCard(category);
            categoriesBox.getChildren().add(card);
        });

        // Set up scroll buttons
        prevCatButton.setOnAction(e -> scrollCategories(-0.2));
        nextCatButton.setOnAction(e -> scrollCategories(0.2));
    }

    private VBox createCategoryCard(Category category) {
        VBox card = new VBox(10);
        card.getStyleClass().add("category-card");
        card.setAlignment(Pos.CENTER);

        try {
            ImageView icon = new ImageView();
            if (category.getIcon() != null && !category.getIcon().isEmpty()) {
                icon.setImage(new Image(category.getIcon()));
            } else {
                icon.setImage(new Image(getClass().getResourceAsStream("/images/default-category.png")));
            }
            icon.setFitWidth(60);
            icon.setFitHeight(60);

            Label name = new Label(category.getName());
            name.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B0082;");

            card.getChildren().addAll(icon, name);
            card.setOnMouseClicked(e -> openCoursesForCategory(category));

        } catch (Exception e) {
            System.out.println("Error loading category image: " + e.getMessage());
        }

        return card;
    }

    private void scrollCategories(double direction) {
        double scrollAmount = categoriesScroll.getHvalue();
        double newScroll = scrollAmount + direction;
        newScroll = Math.max(0, Math.min(newScroll, 1));
        categoriesScroll.setHvalue(newScroll);
    }

    private void openCoursesForCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Courses/courses.fxml"));
            Parent root = loader.load();

            // Get the courses controller and load the category
            MainCourseController controller = loader.getController();
            controller.loadCoursesByCategory(category);

            Stage stage = (Stage) categoriesBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Could not open courses page");
        }
    }


    private void displayRandomUserRoom() {
        if (currentUser == null) return;

        List<Room> userRooms = roomService.getRoomsByOwnerId(currentUser.getId());
        if (userRooms.isEmpty()) {
            userRoomContainer.setVisible(false);
            return;
        }

        // Select a random room
        Random random = new Random();
        Room randomRoom = userRooms.get(random.nextInt(userRooms.size()));

        // Create room card
        Node roomCard = createRoomCard(randomRoom);
        userRoomContainer.getChildren().clear();
        userRoomContainer.getChildren().add(roomCard);
    }

    private Node createRoomCard(Room room) {
        AnchorPane card = new AnchorPane();
        card.getStyleClass().add("room-card");
        card.setPrefSize(350, 200);

        List<String> backgroundImages = Arrays.asList(
                "/images/room.jpg",
                "/images/room1.jpg",
                "/images/room2.jpg"
        );

        Random random = new Random();
        String selectedImagePath = backgroundImages.get(random.nextInt(backgroundImages.size()));

        ImageView background = new ImageView();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream(selectedImagePath));
            background.setImage(bgImage);
        } catch (Exception e) {
            Image defaultBg = new Image(getClass().getResourceAsStream("/images/room.jpg"));
            background.setImage(defaultBg);
        }

        background.setPreserveRatio(false);
        background.setFitWidth(card.getPrefWidth());
        background.setFitHeight(card.getPrefHeight());

        Rectangle clip = new Rectangle();
        clip.setWidth(card.getPrefWidth());
        clip.setHeight(card.getPrefHeight());
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        background.setClip(clip);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");

        Label title = new Label(room.getName());
        title.getStyleClass().add("room-title");
        title.setStyle("-fx-text-fill: white;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER);

        Label typeLabel = new Label(room.getSettings().is_public() ? "Public" : "Private");
        typeLabel.getStyleClass().add("room-type-indicator");
        typeLabel.setStyle("-fx-text-fill: white;");

        footer.getChildren().addAll(typeLabel);
        content.getChildren().addAll(title, footer);

        card.getChildren().addAll(background, content);
        card.setOnMouseClicked(event -> openRoomPage(room));

        return card;
    }

    private void openRoomPage(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/rooms/RoomPage.fxml"));
            Parent root = loader.load();

            RoomPageController controller = loader.getController();
            controller.initData(room, currentUser);

            Stage currentStage = (Stage) userRoomContainer.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not open room: " + e.getMessage());
        }
    }


    private void displayLatestAnnonce() {
        try {
            Annonce latest = annonceService.getLatestAnnonce();
            latestAnnonceContainer.getChildren().clear();

            if (latest != null) {
                Node annonceCard = createAnnonceCard(latest);
                latestAnnonceContainer.getChildren().add(annonceCard);
            } else {
                Label noAnnonceLabel = new Label("No recent announcements");
                noAnnonceLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
                latestAnnonceContainer.getChildren().add(noAnnonceLabel);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching latest announcement: " + e.getMessage());
            Label errorLabel = new Label("Error loading announcements");
            errorLabel.setStyle("-fx-text-fill: #ff4444;");
            latestAnnonceContainer.getChildren().add(errorLabel);
        }
    }

    private Node createAnnonceCard(Annonce annonce) {
        VBox card = new VBox(10);
        card.getStyleClass().add("annonce-card");
        card.setPrefSize(280, 120);
        card.setMaxSize(280, 120);
        card.setStyle("-fx-background-color: #f8f5fa; -fx-background-radius: 10; -fx-padding: 15;");

        // Title
        Label title = new Label(annonce.getTitreA());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B0082; -fx-font-size: 14px;");
        title.setWrapText(true);

        // Description
        Label description = new Label(annonce.getDescriptionA());
        description.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        description.setWrapText(true);
        description.setMaxHeight(40);
        description.setPrefHeight(40);

        // Date
        Label date = new Label("Posted: " + formatDate(annonce.getDateA()));
        date.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        card.getChildren().addAll(title, description, date);

        // Click handler
        card.setOnMouseClicked(e -> showAnnonceDetails(annonce));

        return card;
    }

    private String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"));
    }

    private void showAnnonceDetails(Annonce annonce) {

    }


    private void setupAnnonceListeners() {
        annonceService.addAnnonceCreatedListener(annonce -> {
            Platform.runLater(() -> {
                displayLatestAnnonce();
                showNewAnnonceNotification(annonce);
            });
        });
    }

    private void showNewAnnonceNotification(Annonce annonce) {
        Label notification = new Label("New announcement: " + annonce.getTitreA());
        notification.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10;");

        StackPane root = (StackPane) contentArea.getScene().getRoot();
        root.getChildren().add(notification);

        // Position at top-right
        StackPane.setAlignment(notification, Pos.TOP_RIGHT);
        StackPane.setMargin(notification, new Insets(20));

        // Auto-remove after 5 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e -> root.getChildren().remove(notification));
        delay.play();
    }

    // In your MainController class
    private void initializeCalendar() {
        currentDate = LocalDate.now();
        prevMonth.setOnAction(e -> changeMonth(-1));
        nextMonth.setOnAction(e -> changeMonth(1));
        loadSchedules();
        buildCalendar();
    }

    private void buildCalendar() {
        calendarGrid.getChildren().clear();
        monthYear.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // Add day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < days.length; i++) {
            Label header = new Label(days[i]);
            header.getStyleClass().add("day-header");
            calendarGrid.add(header, i, 0);
        }

        // Add calendar days
        LocalDate firstDay = currentDate.withDayOfMonth(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        int row = 1;

        for (int i = 1; i <= currentDate.lengthOfMonth(); i++) {
            LocalDate date = currentDate.withDayOfMonth(i);
            VBox dayBox = createDayBox(date);
            calendarGrid.add(dayBox, dayOfWeek, row);

            if (++dayOfWeek == 7) {
                dayOfWeek = 0;
                row++;
            }
        }
    }

    private VBox createDayBox(LocalDate date) {
        VBox dayBox = new VBox(2);
        dayBox.getStyleClass().add("calendar-day");
        dayBox.setPadding(new Insets(5));

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B0082;");
        dayBox.getChildren().add(dayNumber);

        List<String> events = schedules.getOrDefault(date, new ArrayList<>());
        for (String event : events) {
            Label eventLabel = new Label(event);
            eventLabel.getStyleClass().add("schedule-event");
            eventLabel.setMaxWidth(Double.MAX_VALUE);
            dayBox.getChildren().add(eventLabel);
        }

        // In createDayBox method change the click handler to:
        dayBox.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) { // Double-click
                showScheduleDialog(date);
            }
        });
        return dayBox;
    }

    private void showScheduleDialog(LocalDate date) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle("Daily Schedule");

        VBox container = new VBox(15);
        container.getStyleClass().addAll("alert-dialog", "info");
        container.setPadding(new Insets(20));

        // Header with icon
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text("📅");
        icon.setStyle("-fx-font-size: 24;");

        Label titleLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")));
        titleLabel.getStyleClass().add("alert-title");
        header.getChildren().addAll(icon, titleLabel);

        // Content area
        VBox contentBox = new VBox(10);
        List<String> events = schedules.getOrDefault(date, new ArrayList<>());

        if (events.isEmpty()) {
            Label noEventsLabel = new Label("No events... just vibes 🌈\n(Time to relax!)");
            noEventsLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-alignment: CENTER;");
            noEventsLabel.setWrapText(true);
            contentBox.getChildren().add(noEventsLabel);
        } else {
            for (String event : events) {
                HBox eventRow = createEventRow(date, event);
                contentBox.getChildren().add(eventRow);
            }
        }

        // Add new event section
        HBox addEventBox = new HBox(10);
        TextField newEventField = new TextField();
        newEventField.setPromptText("Add new event");
        Button addButton = new Button("➕");
        addButton.getStyleClass().add("alert-btn");

        addButton.setOnAction(e -> {
            if (!newEventField.getText().isEmpty()) {
                schedules.computeIfAbsent(date, k -> new ArrayList<>()).add(newEventField.getText());
                saveSchedule(date, newEventField.getText());
                newEventField.clear();
                refreshDialog(contentBox, date);  // Refresh the dialog content
                buildCalendar();  // Refresh calendar display
            }
        });

        addEventBox.getChildren().addAll(newEventField, addButton);

        // Close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("alert-btn");
        closeButton.setOnAction(e -> dialogStage.close());

        container.getChildren().addAll(header, contentBox, addEventBox, closeButton);

        // Style setup
        Scene scene = new Scene(container, 400, 300);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/edit-dialog.css").toExternalForm());
        } catch (NullPointerException e) {
            // Fallback styling
            container.setStyle("-fx-background-color: #f8f5fa; "
                    + "-fx-border-color: #8c84a1; "
                    + "-fx-border-radius: 15px; "
                    + "-fx-padding: 20;");
            titleLabel.setStyle("-fx-text-fill: #4a4458; -fx-font-size: 16; -fx-font-weight: bold;");
            closeButton.setStyle("-fx-background-color: #8c84a1; -fx-text-fill: white; -fx-padding: 8 20;");
        }

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private HBox createEventRow(LocalDate date, String event) {
        HBox eventRow = new HBox(10);
        eventRow.setAlignment(Pos.CENTER_LEFT);

        Label eventLabel = new Label(event);
        eventLabel.setWrapText(true);

        Button editButton = new Button("✏️");
        editButton.getStyleClass().add("icon-button");

        Button deleteButton = new Button("🗑️");
        deleteButton.getStyleClass().add("icon-button");

        // Edit functionality
        editButton.setOnAction(e -> {
            TextInputDialog editDialog = new TextInputDialog(event);
            editDialog.setTitle("Edit Event");
            editDialog.setHeaderText("Edit your schedule");
            editDialog.setContentText("New event text:");

            editDialog.showAndWait().ifPresent(newEvent -> {
                int index = schedules.get(date).indexOf(event);
                schedules.get(date).set(index, newEvent);
                saveScheduleChanges(date);
                buildCalendar();
                eventLabel.setText(newEvent);
            });
        });

        // Delete functionality
        deleteButton.setOnAction(e -> {
            schedules.get(date).remove(event);
            if (schedules.get(date).isEmpty()) {
                schedules.remove(date);
            }
            saveScheduleChanges(date);
            buildCalendar();
            ((VBox) eventRow.getParent()).getChildren().remove(eventRow);
        });

        eventRow.getChildren().addAll(eventLabel, editButton, deleteButton);
        return eventRow;
    }

    private void refreshDialog(VBox contentBox, LocalDate date) {
        contentBox.getChildren().clear();
        List<String> events = schedules.getOrDefault(date, new ArrayList<>());

        if (events.isEmpty()) {
            Label noEventsLabel = new Label("Schedule cleared! 🎉\n(Time for coffee?)");
            noEventsLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-alignment: CENTER;");
            contentBox.getChildren().add(noEventsLabel);
        } else {
            for (String event : events) {
                contentBox.getChildren().add(createEventRow(date, event));
            }
        }
    }

    // In saveScheduleChanges method
    private void saveScheduleChanges(LocalDate date) {
        if (currentUser == null) return;

        try {
            java.nio.file.Path path = Paths.get("schedules_" + currentUser.getId() + ".txt");
            List<String> lines = new ArrayList<>();

            for (Map.Entry<LocalDate, List<String>> entry : schedules.entrySet()) {
                for (String event : entry.getValue()) {
                    lines.add(entry.getKey().toString() + "|" + event);
                }
            }

            Files.write(
                    path,
                    lines,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            showCustomAlert("error", "Save Error", "Failed to save changes: " + e.getMessage());
        }
    }

    private void changeMonth(int months) {
        currentDate = currentDate.plusMonths(months);
        loadSchedules();
        buildCalendar();
    }

    // Simple file-based persistence (you can replace with database)
    private void saveSchedule(LocalDate date, String event) {
        if (currentUser == null) return;

        try {
            java.nio.file.Path path = Paths.get("schedules_" + currentUser.getId() + ".txt");
            Files.write(path, (date.toString() + "|" + event + "\n").getBytes(),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSchedules() {
        schedules.clear();
        if (currentUser == null) return; // Ensure currentUser is available

        try {
            java.nio.file.Path path = Paths.get("schedules_" + currentUser.getId() + ".txt");
            if (Files.exists(path)) {
                Files.lines(path).forEach(line -> {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2) {
                        LocalDate date = LocalDate.parse(parts[0]);
                        schedules.computeIfAbsent(date, k -> new ArrayList<>()).add(parts[1]);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showCustomAlert(String alertType, String title, String message) {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initStyle(StageStyle.UTILITY);
        alertStage.setTitle(title);

        VBox container = new VBox(15);
        container.getStyleClass().addAll("alert-dialog", alertType);
        container.setPadding(new Insets(20));

        // Header with icon
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text();
        icon.setStyle("-fx-font-size: 24;");
        switch (alertType) {
            case "success" -> icon.setText("✔️");
            case "error" -> icon.setText("❌");
            default -> icon.setText("ℹ️");
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        header.getChildren().addAll(icon, titleLabel);

        // Content
        Label content = new Label(message);
        content.getStyleClass().add("alert-content");
        content.setWrapText(true);

        // OK Button
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("alert-btn");
        okButton.setOnAction(e -> alertStage.close());

        container.getChildren().addAll(header, content, okButton);

        Scene scene = new Scene(container, 350, 200);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/edit-dialog.css").toExternalForm());
        } catch (NullPointerException e) {
            // Fallback inline styling
            container.setStyle("-fx-background-color: #f8f5fa; "
                    + "-fx-border-color: #8c84a1; "
                    + "-fx-border-radius: 15px; "
                    + "-fx-padding: 20;");
            titleLabel.setStyle("-fx-text-fill: #4a4458; -fx-font-size: 16; -fx-font-weight: bold;");
            content.setStyle("-fx-text-fill: #4a4458;");
            okButton.setStyle("-fx-background-color: #8c84a1; -fx-text-fill: white; -fx-padding: 8 20;");
        }

        alertStage.setScene(scene);
        alertStage.showAndWait();
    }


    private void initializeTimeTracking() {
        loadSavedTime();
        loadEarnedBadges();
        setupProgressAnimation();
        startTimeCounter();
        setupShutdownHook();
    }

    private void loadEarnedBadges() {
        BADGES.keySet().forEach(threshold -> {
            if (totalSeconds >= threshold) {
                unlockedBadges.add(threshold);
                addBadgeToContainer(BADGES.get(threshold));
            }
        });
    }

    private void loadSavedTime() {
        try {
            // Explicitly use java.nio.file.Path
            java.nio.file.Path path = Paths.get(timeFile);
            if (Files.exists(path)) {
                String content = Files.readString(path);
                totalSeconds = Long.parseLong(content.trim());
            }
        } catch (Exception e) {
            System.err.println("Error loading study time: " + e.getMessage());
        }
    }

    private void setupProgressAnimation() {
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

    }

    private void startTimeCounter() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            totalSeconds++;
            updateTimeDisplay();
            saveTimePeriodically();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateTimeDisplay() {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String timeString = String.format("You've spent %02d:%02d:%02d studying on MENTOR! 📚",
                hours, minutes, seconds);

        timeLabel.setText(timeString);

        // Check for new badges
        if(currentUser != null) {
            BADGES.keySet().forEach(threshold -> {
                if(totalSeconds >= threshold && !unlockedBadges.contains(threshold)) {
                    unlockedBadges.add(threshold);
                    addBadgeToContainer(BADGES.get(threshold)); // Directly add badge without animation
                }
            });
        }
    }
    private void saveTimePeriodically() {
        if (totalSeconds % 60 == 0) { // Save every minute
            saveStudyTime();
        }
    }

    private void saveStudyTime() {
        try {
            Files.writeString(Paths.get(timeFile), // Changed to timeFile
                    String.valueOf(totalSeconds),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving study time: " + e.getMessage());
        }
    }

    private void setupShutdownHook() {
        String finalTimeFile = this.timeFile; // Capture current value
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timeline.stop();
            try {
                Files.writeString(Paths.get(finalTimeFile),
                        String.valueOf(totalSeconds),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                System.err.println("Error saving study time on shutdown: " + e.getMessage());
            }
        }));
    }


    private void addBadgeToContainer(String badgeImagePath) {
        ImageView smallBadge = new ImageView(new Image(getClass().getResourceAsStream(badgeImagePath)));
        smallBadge.setFitWidth(50);
        smallBadge.setFitHeight(50);

        // Add hover effect
        smallBadge.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), smallBadge);
            st.setToX(1.2);
            st.setToY(1.2);
            st.play();
        });

        smallBadge.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), smallBadge);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        badgesContainer.getChildren().add(smallBadge);
    }

    private void initializeBackgroundAudio() {
        try {
            String audioPath = getClass().getResource("/sounds/Bling.wav").toString();
            System.out.println("Loading audio from: " + audioPath); // Debug output

            Media media = new Media(audioPath);
            mediaPlayer = new MediaPlayer(media);

            // Add error listeners
            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer Error: " + mediaPlayer.getError().getMessage());
                showAlert("Audio Error", "Failed to play background music: " + mediaPlayer.getError().getMessage());
            });

            media.setOnError(() -> {
                System.err.println("Media Error: " + media.getError().getMessage());
                showAlert("Audio Error", "Invalid audio file: " + media.getError().getMessage());
            });

            mediaPlayer.setVolume(0.9); // 90% volume
            mediaPlayer.setCycleCount(1); // Play only once

            // Start playing when media is ready
            mediaPlayer.setOnReady(() -> {
                System.out.println("Audio is ready, starting playback...");
                mediaPlayer.play();
            });

            // Handle scene changes
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && mediaPlayer.getStatus() == MediaPlayer.Status.READY) {
                    System.out.println("Scene attached - playing audio");
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                } else {
                    System.out.println("Scene detached - pausing audio");
                    mediaPlayer.pause();
                }
            });

        } catch (Exception e) {
            System.err.println("Audio Initialization Error: " + e.getMessage());
            showAlert("Audio Error", "Failed to initialize audio: " + e.getMessage());
        }
    }

    /**
     * Plays a soft click sound when elements are hovered
     */
    private void playClickSound() {
        try {
            // Create and play a subtle hover sound
            String soundPath = getClass().getResource("/sounds/hover.wav").toExternalForm();
            if (soundPath == null) {
                // Try alternative sounds if hover.wav doesn't exist
                soundPath = getClass().getResource("/sounds/click.wav").toExternalForm();
                if (soundPath == null) {
                    soundPath = getClass().getResource("/sounds/ZipZap.wav").toExternalForm();
                }
            }
            
            if (soundPath != null) {
                Media hoverSound = new Media(soundPath);
                MediaPlayer hoverPlayer = new MediaPlayer(hoverSound);
                hoverPlayer.setVolume(0.2); // Low volume for hover sound
                hoverPlayer.play();
            }
        } catch (Exception e) {
            // Silently ignore sound errors
            System.out.println("Could not play hover sound: " + e.getMessage());
        }
    }
}