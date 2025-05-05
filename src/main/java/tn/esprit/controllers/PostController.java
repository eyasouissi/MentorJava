package tn.esprit.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.converter.DefaultStringConverter;
import tn.esprit.entities.Forum;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.MyDataBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import tn.esprit.services.PurgoMalumService;
import tn.esprit.services.LocalSentimentAnalyzer;
import tn.esprit.services.WritingQualityService;
import tn.esprit.services.GiphyService;

public class PostController implements Initializable {

    @FXML private VBox postContainer;
    @FXML private Label forumTitle;
    @FXML private TextArea postContent;
    @FXML private ListView<Post> postsListView;
    @FXML private FlowPane photosFlowPane;
    @FXML private Button submitPostBtn;
    @FXML private Label charCountLabel;
    @FXML
    private VBox postInputContainer;
    @FXML
    private Label postErrorLabel;
    @FXML
    private Label contentErrorLabel;




    private ObservableList<Post> posts = FXCollections.observableArrayList();
    private Forum currentForum;
    private List<String> selectedPhotoNames = new ArrayList<>();
    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    private Stage imageStage;
    private StackPane imageContainer;
    private int currentImageIndex = 0;
    private List<String> currentAlbumPhotos = new ArrayList<>();
    @FXML private Label moderationErrorLabel;
    private final PurgoMalumService purgoMalumService = new PurgoMalumService();
    @FXML private Label sentimentErrorLabel;
    private final LocalSentimentAnalyzer sentimentAnalyzer = new LocalSentimentAnalyzer();
    @FXML private VBox writingFeedbackContainer;
    @FXML private ListView<String> writingIssuesList;
    private final WritingQualityService writingService = new WritingQualityService();
    private boolean hasPostAttempt = false;
    private boolean hasQualityIssues = false;
    @FXML private Button returnButton;
    @FXML private FlowPane gifReactionsContainer;
    private final GiphyService giphyService = new GiphyService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPostListView();
        setupInputValidation();
        postContent.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(oldVal)) {
                resetPostState();
            }
        });
    }

    private void setupInputValidation() {
        // Character limit enforcement
        postContent.setTextFormatter(new TextFormatter<>(new DefaultStringConverter(), "", change -> {
            if (change.getControlNewText().length() <= 200) {
                return change;
            }
            return null;
        }));

        // Character count binding
        charCountLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            int length = postContent.getText().length();
            return length + "/200";
        }, postContent.textProperty()));

        // Validation styling
        postContent.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isEmpty = newVal.trim().isEmpty();
            boolean isTooLong = newVal.length() > 200;

            postContent.pseudoClassStateChanged(errorClass, isEmpty || isTooLong);
            charCountLabel.pseudoClassStateChanged(errorClass, isTooLong);
        });
    }

    public void setForum(Forum forum) {
        this.currentForum = forum;
        forumTitle.setText(forum.getTitle());
        loadPosts();
    }

    private void loadPosts() {
        posts.clear();
        String sql = "SELECT id, content, created_at, user_id, photos FROM post WHERE forum_id = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, currentForum.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setContent(rs.getString("content"));
                post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                // Retrieve user_id as Long from database
                Long userId = rs.getLong("user_id"); // Use getLong() instead of getInt()
                User postUser = new User();
                postUser.setId(userId); // Now passing a Long
                post.setUser(postUser);
                post.setPhotos(rs.getString("photos"));  // Add this line

                posts.add(post);
            }
            postsListView.setItems(posts);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading posts: " + e.getMessage());
        }
    }

    private void setupPostListView() {
        postsListView.setCellFactory(lv -> new ListCell<Post>() {
            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createPostCard(post));
                }
            }
        });
    }





    private void loadImage(String fileName, ImageView imageView) {
        try {
            Path imagePath = Paths.get("uploads", fileName);
            Image image = new Image(imagePath.toUri().toString());
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }
    private void updateCounter(Label label, int current, int total) {
        label.setText(current + " / " + total);
        label.setVisible(total > 1);
    }


    private Image loadDefaultImage() {
        InputStream stream = getClass().getResourceAsStream("/assets/default_pfp.png");
        return stream != null ? new Image(stream) : null;
    }

    private HBox createActionButtons(Post post) {
        HBox actions = new HBox(10);
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        // ... button handlers ...
        return actions;
    }


    @FXML
    private void handleAddPhotos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());
        if (files != null) {
            files.forEach(file -> {
                try {
                    Path uploadDir = Paths.get("uploads");
                    if (!Files.exists(uploadDir)) {
                        Files.createDirectories(uploadDir);
                    }

                    // Sanitize filename before processing
                    String cleanName = sanitizeFilename(file.getName());
                    Path destination = uploadDir.resolve(cleanName);

                    // Copy with sanitized filename
                    Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                    if (!selectedPhotoNames.contains(cleanName)) {
                        selectedPhotoNames.add(cleanName);
                        photosFlowPane.getChildren().add(
                                createPhotoThumbnail(cleanName, destination.toUri().toString())
                        );
                    }
                } catch (IOException e) {
                    showAlert("File Error", "Failed to save: " + e.getMessage());
                }
            });
        }
    }

    private VBox createPostCard(Post post) {
        VBox card = new VBox(10);
        card.getStyleClass().add("post-card");
        card.setPadding(new Insets(10));
        card.setPrefWidth(400);

        // Get user with ID 8
        UserService userService = new UserService();
        User user8 = userService.getById(8L);

        // --- Header: Profile Pic + Info ---
        HBox postHeader = new HBox(10);
        postHeader.getStyleClass().add("post-header");
        postHeader.setAlignment(Pos.CENTER_LEFT);

        // Create circular avatar container
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(50, 50);
        Circle clip = new Circle(100);
        avatarContainer.setClip(clip);

        ImageView profilePic = new ImageView();
        profilePic.getStyleClass().add("profile-pic");
        profilePic.setPreserveRatio(true);
        profilePic.setSmooth(true);

        // Set initial image
        profilePic.setImage(loadDefaultImage());
        avatarContainer.getChildren().add(profilePic);

        try {
            if (user8 != null) {
                String pfpPath = user8.getPfp();
                if (pfpPath != null && !pfpPath.isEmpty()) {
                    String resourcePath = "/assets/uploads/pfp/" + pfpPath.substring(pfpPath.lastIndexOf("/") + 1);
                    try (InputStream imageStream = getClass().getResourceAsStream(resourcePath)) {
                        if (imageStream != null) {
                            Image userImage = new Image(imageStream);
                            profilePic.setImage(userImage);

                            // Adjust image scaling to show full content
                            if (userImage.getWidth() > userImage.getHeight()) {
                                profilePic.setFitWidth(50);
                            } else {
                                profilePic.setFitHeight(50);
                            }
                        }
                    }
                }

                VBox userInfo = new VBox(5);
                userInfo.getStyleClass().add("user-info");
                Label userName = new Label(user8.getName() != null ? user8.getName() : "User 8");
                userName.getStyleClass().add("user-name");
                Label postDate = new Label("Posted: " + post.getCreatedAt().toLocalDate());
                postDate.getStyleClass().add("post-date");
                userInfo.getChildren().addAll(userName, postDate);
                postHeader.getChildren().addAll(avatarContainer, userInfo);
            }
        } catch (Exception e) {
            postHeader.getChildren().addAll(avatarContainer, new Label("User 8"));
        }

        // --- Post Content ---
        TextArea postContent = new TextArea(post.getContent());
        postContent.getStyleClass().add("post-content");
        postContent.setWrapText(true);
        postContent.setEditable(false);
        postContent.setPrefRowCount(4);

        // --- Photo Gallery ---
        if (post.getPhotos() != null && !post.getPhotos().isEmpty()) {
            List<String> photos = Arrays.asList(post.getPhotos().split(","));
            // Gallery Container
            StackPane galleryContainer = new StackPane();
            galleryContainer.setAlignment(Pos.CENTER);
            galleryContainer.setPrefSize(400, 300);
            galleryContainer.setStyle("-fx-background-color: #f0f0f0;");

            // Main Image View
            ImageView currentImage = new ImageView();
            currentImage.setPreserveRatio(true);
            currentImage.setFitWidth(380);
            currentImage.setFitHeight(280);
            currentImage.setSmooth(true);
            loadGalleryImage(photos.get(0), currentImage);

            // Navigation Controls
            Button prevButton = new Button("❮");
            Button nextButton = new Button("❯");
            prevButton.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white;");
            nextButton.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white;");

            HBox navButtons = new HBox(20, prevButton, nextButton);
            navButtons.setAlignment(Pos.CENTER);
            navButtons.setSpacing(320);
            navButtons.setOpacity(0.3);
            navButtons.setOnMouseEntered(e -> navButtons.setOpacity(1));
            navButtons.setOnMouseExited(e -> navButtons.setOpacity(0.3));

            // Image Counter
            Label counterLabel = new Label("1/" + photos.size());
            counterLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.5);");
            counterLabel.setPadding(new Insets(5));

            // Swipe Handling
            AtomicInteger currentIndex = new AtomicInteger(0);
            galleryContainer.setOnMousePressed(e -> {
                double dragStartX = e.getSceneX();
                galleryContainer.setOnMouseReleased(event -> {
                    double delta = event.getSceneX() - dragStartX;
                    if (Math.abs(delta) > 50) {
                        if (delta > 0) {
                            currentIndex.set((currentIndex.get() - 1 + photos.size()) % photos.size());
                        } else {
                            currentIndex.set((currentIndex.get() + 1) % photos.size());
                        }
                        loadGalleryImage(photos.get(currentIndex.get()), currentImage);
                        counterLabel.setText((currentIndex.get() + 1) + "/" + photos.size());
                    }
                });
            });

            // Button Actions
            prevButton.setOnAction(e -> {
                currentIndex.set((currentIndex.get() - 1 + photos.size()) % photos.size());
                loadGalleryImage(photos.get(currentIndex.get()), currentImage);
                counterLabel.setText((currentIndex.get() + 1) + "/" + photos.size());
            });

            nextButton.setOnAction(e -> {
                currentIndex.set((currentIndex.get() + 1) % photos.size());
                loadGalleryImage(photos.get(currentIndex.get()), currentImage);
                counterLabel.setText((currentIndex.get() + 1) + "/" + photos.size());
            });

            // Lightbox Click
            currentImage.setOnMouseClicked(e -> showLightbox(photos, currentIndex.get()));

            galleryContainer.getChildren().addAll(currentImage, counterLabel, navButtons);
            card.getChildren().add(galleryContainer);
        }

        // --- Translate Button with Context Menu ---
        Button translateBtn = new Button("Translate");
        translateBtn.getStyleClass().add("translate-btn");

        // Language map with Map.ofEntries for more than 10 entries
        Map<String, String> languageCodes = Map.ofEntries(
                Map.entry("English", "en"),
                Map.entry("Arabic", "ar"),
                Map.entry("Chinese (Simplified)", "zh-CN"),
                Map.entry("Chinese (Traditional)", "zh-TW"),
                Map.entry("German", "de"),
                Map.entry("French", "fr"),
                Map.entry("Spanish", "es"),
                Map.entry("Italian", "it"),
                Map.entry("Japanese", "ja"),
                Map.entry("Russian", "ru"),
                Map.entry("Korean", "ko")
        );

        ContextMenu translateMenu = new ContextMenu();
        List<String> languages = new ArrayList<>(languageCodes.keySet());

        for (String langName : languages) {
            MenuItem langItem = new MenuItem(langName);
            langItem.getStyleClass().add("lang-item");
            String targetLang = languageCodes.get(langName);

            langItem.setOnAction(e -> {
                if (targetLang == null) {
                    showAlert("Translation Error", "Invalid language configuration");
                    return;
                }
                handleTranslationSelection(
                        post.getContent(),
                        targetLang,
                        translateBtn,
                        postContent
                );
            });
            translateMenu.getItems().add(langItem);
        }

        // Store original content reference
        final String originalContent = post.getContent();

        // Set menu behavior with revert functionality
        translateBtn.setOnAction(e -> {
            if (translateBtn.getText().startsWith("Translate")) {
                translateMenu.show(translateBtn, Side.BOTTOM, 0, 0);
            } else {
                postContent.setText(originalContent);
                translateBtn.setText("Translate");
                translateBtn.getStyleClass().remove("original-btn");
            }
        });

        // --- Action Buttons ---
        ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/edit.png")));
        editIcon.setFitWidth(20);
        editIcon.setFitHeight(20);
        Button editBtn = new Button("", editIcon);
        editBtn.setStyle("-fx-background-color: transparent;");
        editBtn.getStyleClass().add("icon-button");
        Tooltip.install(editBtn, new Tooltip("Edit"));

        ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/delete.png")));
        deleteIcon.setFitWidth(20);
        deleteIcon.setFitHeight(20);
        Button deleteBtn = new Button("", deleteIcon);
        deleteBtn.setStyle("-fx-background-color: transparent;");
        deleteBtn.getStyleClass().add("icon-button");
        Tooltip.install(deleteBtn, new Tooltip("Delete"));


        HBox actions = new HBox(10, editBtn, deleteBtn, translateBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(Double.MAX_VALUE);
        actions.setMinWidth(0);
        actions.setSpacing(10);

        editBtn.setOnAction(e -> handleEditPost(post));
        deleteBtn.setOnAction(e -> handleDeletePost(post));

        HBox actionWrapper = new HBox(actions);
        actionWrapper.setAlignment(Pos.BOTTOM_RIGHT);
        actionWrapper.setPadding(new Insets(10, 0, 0, 0));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(
                postHeader,
                postContent,
                spacer,
                actionWrapper
        );

        return card;
    }
    private Node createGalleryContainer(List<String> photos) {
        FlowPane gallery = new FlowPane();
        gallery.setPadding(new Insets(10));
        gallery.setHgap(10);
        gallery.setVgap(10);
        gallery.setPrefWrapLength(400);

        photos.forEach(fileName -> {
            ImageView thumb = new ImageView();
            thumb.setFitWidth(100);
            thumb.setFitHeight(100);
            thumb.setPreserveRatio(true);
            loadGalleryImage(fileName, thumb);

            // Add click handler
            thumb.setOnMouseClicked(e ->
                    showLightbox(photos, photos.indexOf(fileName))
            );

            gallery.getChildren().add(thumb);
        });

        return gallery;
    }
    private void loadGalleryImage(String fileName, ImageView imageView) {
        try {
            // Sanitize filename from database
            String safeName = sanitizeFilename(fileName);
            Path imagePath = Paths.get("uploads", safeName);

            // Validate path
            if (!Files.exists(imagePath)) {
                throw new FileNotFoundException("Image not found: " + safeName);
            }

            Image image = new Image(imagePath.toUri().toString());
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            // Fallback to default image
            imageView.setImage(loadDefaultImage());
        }
    }

    private void showLanguageSelectionDialog(String originalText, Button translateBtn, TextArea postContent) {
        // Sample list of languages to translate to
        List<String> languages = Arrays.asList("EN", "DE", "FR", "ES", "IT");

        // Create a ComboBox for language selection
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll(languages);

        // Show a dialog to select a language
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Select Language");
        alert.setHeaderText("Choose a language to translate to:");
        alert.getDialogPane().setContent(languageSelector);

        // Add handler when a language is selected
        alert.showAndWait().ifPresent(response -> {
            if (languageSelector.getValue() != null) {
                String selectedLanguage = languageSelector.getValue();
                translateContent(originalText, selectedLanguage, translateBtn, postContent);
            }
        });
    }

    private void translateContent(String originalText, String targetLanguage, Button translateBtn, TextArea postContent) {
        postContent.setText("Translating...");
        translateBtn.setDisable(true);

        new Thread(() -> {
            try {
                String translated = TranslateController.translateText(originalText, targetLanguage);

                Platform.runLater(() -> {
                    postContent.setText(translated);
                    translateBtn.setText("Original");
                    translateBtn.setDisable(false);
                    setupRevertButton(originalText, translateBtn, postContent);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    postContent.setText(originalText);
                    translateBtn.setDisable(false);
                    showTranslationError(ex.getMessage());
                });
            }
        }).start();
    }

    private void showTranslationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Translation Error");
        alert.setHeaderText("Could not translate text");
        alert.setContentText("Error: " + message + "\n\nPlease:\n1. Check your internet connection\n2. Try again later\n3. Contact support if it persists");
        alert.showAndWait();
    }

    private void setupRevertButton(String original, Button btn, TextArea area) {
        btn.setOnAction(e -> {
            area.setText(original);
            btn.setText("Translate");
            btn.setOnAction(event ->
                    showLanguageSelectionDialog(original, btn, area)
            );
        });
    }

    private void revertToOriginalContent(String originalText, Button translateBtn, TextArea postContent) {
        postContent.setText(originalText);
        translateBtn.setText("Translate");
        translateBtn.setOnAction(e ->
                showLanguageSelectionDialog(originalText, translateBtn, postContent)
        );
    }
    private void handleTranslationSelection(String originalText, String targetLang,
                                            Button translateBtn, TextArea postContent) {
        postContent.setText("Translating...");
        translateBtn.setDisable(true);

        new Thread(() -> {
            try {
                if (!isValidLanguageCode(targetLang)) {
                    throw new IllegalArgumentException("Invalid language code: " + targetLang);
                }

                String translated = TranslateController.translateText(originalText, targetLang);

                Platform.runLater(() -> {
                    postContent.setText(translated);
                    translateBtn.setText("Original (" + targetLang + ")");
                    translateBtn.getStyleClass().add("original-btn");
                    translateBtn.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    postContent.setText(originalText);
                    translateBtn.setDisable(false);
                    showTranslationError(ex.getMessage());
                });
            }
        }).start();
    }


    private boolean isValidLanguageCode(String code) {
        return code != null && code.matches("^[a-zA-Z]{2}(-[a-zA-Z]{2})?$");
    }

    private void setupRevertButton(String original, MenuButton btn, TextArea area) {
        btn.setOnAction(e -> {
            area.setText(original);
            btn.setText("Translate");
            btn.getStyleClass().remove("original-btn");
            btn.setDisable(false);
        });
    }

    private ImageView createPhotoThumbnail(String fileName) {
        ImageView imageView = new ImageView();
        try {
            Path imagePath = Paths.get("uploads", fileName);
            Image image = new Image(imagePath.toUri().toString());
            imageView.setImage(image);
            imageView.setFitWidth(300);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
        return imageView;
    }
    private void createPhotoGallery(Post post, VBox card) {
        if (post.getPhotos() == null || post.getPhotos().isEmpty()) return;

        List<String> photos = Arrays.asList(post.getPhotos().split(","));
        if (photos.isEmpty()) return;

        // Create gallery container
        StackPane galleryContainer = new StackPane();
        galleryContainer.setAlignment(Pos.CENTER);
        galleryContainer.setPrefSize(400, 300);
        galleryContainer.setStyle("-fx-background-color: #f0f0f0;");

        ImageView currentImage = new ImageView();
        currentImage.setPreserveRatio(true);
        currentImage.setFitWidth(380);
        currentImage.setFitHeight(280);
        currentImage.setSmooth(true);

        Label counterLabel = new Label();
        counterLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.5);");
        counterLabel.setPadding(new Insets(5));

        // Navigation buttons
        Button prevButton = new Button("❮");
        Button nextButton = new Button("❯");
        prevButton.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white;");
        nextButton.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white;");

        HBox navButtons = new HBox(prevButton, nextButton);
        navButtons.setAlignment(Pos.CENTER);
        navButtons.setSpacing(320);
        navButtons.setMouseTransparent(true);

        AtomicInteger currentIndex = new AtomicInteger(0);
        loadImage(photos.get(0), currentImage);
        updateCounter(counterLabel, currentIndex.get() + 1, photos.size());

        // Swipe detection
        final double[] dragStartX = {0};
        galleryContainer.setOnMousePressed(e -> dragStartX[0] = e.getSceneX());
        galleryContainer.setOnMouseReleased(e -> {
            double delta = e.getSceneX() - dragStartX[0];
            if (Math.abs(delta) > 50) { // Swipe threshold
                if (delta > 0) { // Swipe right
                    currentIndex.set((currentIndex.get() - 1 + photos.size()) % photos.size());
                } else { // Swipe left
                    currentIndex.set((currentIndex.get() + 1) % photos.size());
                }
                animateImageTransition(currentImage, photos.get(currentIndex.get()));
                updateCounter(counterLabel, currentIndex.get() + 1, photos.size());
            }
        });

        // Button actions
        prevButton.setOnAction(e -> {
            currentIndex.set((currentIndex.get() - 1 + photos.size()) % photos.size());
            animateImageTransition(currentImage, photos.get(currentIndex.get()));
            updateCounter(counterLabel, currentIndex.get() + 1, photos.size());
        });

        nextButton.setOnAction(e -> {
            currentIndex.set((currentIndex.get() + 1) % photos.size());
            animateImageTransition(currentImage, photos.get(currentIndex.get()));
            updateCounter(counterLabel, currentIndex.get() + 1, photos.size());
        });

        // Click to open lightbox
        currentImage.setOnMouseClicked(e -> showLightbox(photos, currentIndex.get()));

        galleryContainer.getChildren().addAll(currentImage, counterLabel, navButtons);
        card.getChildren().add(galleryContainer);
    }
    private void showLightbox(List<String> photos, int startIndex) {
        Stage lightboxStage = new Stage();
        lightboxStage.initModality(Modality.APPLICATION_MODAL);
        lightboxStage.initStyle(StageStyle.UNDECORATED);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.95);");

        ImageView fullImageView = new ImageView();
        fullImageView.setPreserveRatio(true);
        fullImageView.setFitWidth(800);
        fullImageView.setFitHeight(600);
        loadImage(photos.get(startIndex), fullImageView);

        Label lightboxCounter = new Label();
        lightboxCounter.setStyle("-fx-text-fill: white; -fx-font-size: 18;");

        AtomicInteger currentIndex = new AtomicInteger(startIndex);
        updateCounter(lightboxCounter, currentIndex.get() + 1, photos.size());

        // Swipe detection for lightbox
        final double[] dragStartX = {0};
        root.setOnMousePressed(e -> dragStartX[0] = e.getSceneX());
        root.setOnMouseReleased(e -> {
            double delta = e.getSceneX() - dragStartX[0];
            if (Math.abs(delta) > 50) {
                if (delta > 0) {
                    currentIndex.set((currentIndex.get() - 1 + photos.size()) % photos.size());
                } else {
                    currentIndex.set((currentIndex.get() + 1) % photos.size());
                }
                loadImage(photos.get(currentIndex.get()), fullImageView);
                updateCounter(lightboxCounter, currentIndex.get() + 1, photos.size());
            }
        });

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 24;");
        closeButton.setOnAction(e -> lightboxStage.close());

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(fullImageView, lightboxCounter);

        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(20));

        root.getChildren().addAll(container, closeButton);

        Scene scene = new Scene(root, 1000, 800);
        lightboxStage.setScene(scene);
        lightboxStage.show();
    }

    private void animateImageTransition(ImageView imageView, String imagePath) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), imageView);
        transition.setFromX(0);
        transition.setToX(imageView.getTranslateX() > 0 ? -imageView.getFitWidth() : imageView.getFitWidth());
        transition.setOnFinished(e -> {
            loadImage(imagePath, imageView);
            imageView.setTranslateX(0);
        });
        transition.play();
    }
    private void showImageLightbox(List<String> photos, int startIndex) {
        currentAlbumPhotos = photos;
        currentImageIndex = startIndex;

        // Create lightbox stage
        Stage lightboxStage = new Stage();
        lightboxStage.initModality(Modality.APPLICATION_MODAL);
        lightboxStage.initStyle(StageStyle.UNDECORATED);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.9);");

        // Image view
        ImageView fullImageView = new ImageView();
        fullImageView.setPreserveRatio(true);
        fullImageView.setSmooth(true);
        fullImageView.setFitWidth(800);
        fullImageView.setFitHeight(600);

        // Navigation buttons
        Button prevButton = new Button("◀");
        Button nextButton = new Button("▶");
        prevButton.setStyle("-fx-font-size: 24; -fx-text-fill: white; -fx-background-color: transparent;");
        nextButton.setStyle("-fx-font-size: 24; -fx-text-fill: white; -fx-background-color: transparent;");

        HBox navBox = new HBox(20, prevButton, nextButton);
        navBox.setAlignment(Pos.CENTER);

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);

        // Update image
        Runnable updateImage = () -> {
            Path imagePath = Paths.get("uploads", currentAlbumPhotos.get(currentImageIndex));
            fullImageView.setImage(new Image(imagePath.toUri().toString()));
        };

        prevButton.setOnAction(e -> {
            currentImageIndex = (currentImageIndex - 1 + currentAlbumPhotos.size()) % currentAlbumPhotos.size();
            updateImage.run();
        });

        nextButton.setOnAction(e -> {
            currentImageIndex = (currentImageIndex + 1) % currentAlbumPhotos.size();
            updateImage.run();
        });

        // Close on click outside
        root.setOnMouseClicked(e -> {
            if (e.getTarget() == root) {
                lightboxStage.close();
            }
        });

        // Initial image load
        updateImage.run();

        container.getChildren().addAll(fullImageView, navBox);
        root.getChildren().add(container);

        Scene scene = new Scene(root, 1200, 800);
        lightboxStage.setScene(scene);
        lightboxStage.show();
    }

    @FXML
    private void handleCreatePost() {
        // Clear previous errors and styles
        postContent.pseudoClassStateChanged(errorClass, false);
        charCountLabel.pseudoClassStateChanged(errorClass, false);
        contentErrorLabel.setText("");
        moderationErrorLabel.setVisible(false);
        sentimentErrorLabel.setVisible(false);
        writingFeedbackContainer.setVisible(false);

        String content = postContent.getText().trim();
        boolean hasErrors = false;

        // Basic validation
        if (content.isEmpty()) {
            postContent.getStyleClass().add("text-field-error");
            contentErrorLabel.setText("Post content cannot be empty!");
            playPopAnimation(postInputContainer);

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> contentErrorLabel.setText(""));
            pause.play();
            return;
        } else {
            postContent.getStyleClass().remove("text-field-error");
        }

        if (content.length() > 200) {
            charCountLabel.pseudoClassStateChanged(errorClass, true);
            showAlert("Error", "Post content cannot exceed 200 characters!");
            hasErrors = true;
        }

        if (hasErrors) return;

        // Handle "Post Anyway" click
        if (submitPostBtn.getText().equals("Post Anyway")) {
            createCleanPost(content);
            submitPostBtn.setText("Post");
            return;
        }

        // Disable submit button during checks
        submitPostBtn.setDisable(true);
        submitPostBtn.setText("Checking...");

        // Create combined check task
        Task<Void> checkTask = new Task<>() {
            private boolean hasProfanity = false;
            private boolean hasNegativeSentiment = false;
            private boolean hasWritingIssues = false;

            @Override
            protected Void call() throws Exception {
                // 1. Profanity check (blocking)
                hasProfanity = purgoMalumService.containsProfanity(content);
                if (hasProfanity) return null;

                // 2. Sentiment check (blocking)
                LocalSentimentAnalyzer.SentimentResult sentimentResult = sentimentAnalyzer.analyze(content);
                hasNegativeSentiment = sentimentResult.getScore() < -0.1;
                if (hasNegativeSentiment) return null;

                // 3. Writing quality check (non-blocking)
                List<WritingQualityService.WritingIssue> issues = writingService.checkText(content);
                hasWritingIssues = !issues.isEmpty();
                if (hasWritingIssues) {
                    Platform.runLater(() -> showWritingFeedback(issues));
                }

                return null;
            }

            @Override
            protected void succeeded() {
                submitPostBtn.setDisable(false);
                submitPostBtn.setText("Post");

                if (hasProfanity) {
                    moderationErrorLabel.setText("Post contains inappropriate language");
                    moderationErrorLabel.setVisible(true);
                    playPopAnimation(postInputContainer);
                }
                else if (hasNegativeSentiment) {
                    sentimentErrorLabel.setText("Negative sentiment detected");
                    sentimentErrorLabel.setVisible(true);
                    playPopAnimation(postInputContainer);
                }
                else if (hasWritingIssues) {
                    submitPostBtn.setText("Post Anyway");
                }
                else {
                    createCleanPost(content);
                }
            }

            @Override
            protected void failed() {
                submitPostBtn.setDisable(false);
                submitPostBtn.setText("Post");
                showAlert("Error", "Post check failed: " + getException().getMessage());
            }
        };

        new Thread(checkTask).start();
    }
    private void checkSentiment(String content) {
        LocalSentimentAnalyzer.SentimentResult result = sentimentAnalyzer.analyze(content);

        if (result.getScore() < -0.1) {
            String message = "Negative sentiment detected. Please revise your post.";
            sentimentErrorLabel.setText(message);
            sentimentErrorLabel.setVisible(true);
            playPopAnimation(postInputContainer);

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> {
                sentimentErrorLabel.setVisible(false);
                sentimentErrorLabel.setText("");
            });
            pause.play();
        } else {
            // Proceed to writing quality check
            checkWritingQuality(content);
        }
    }
    private void createCleanPost(String cleanContent) {
        Post newPost = new Post();
        newPost.setContent(cleanContent);
        newPost.setForum(currentForum);
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setUpdatedAt(LocalDateTime.now());
        newPost.setLikes(0);
        newPost.setPhotos(selectedPhotoNames.isEmpty() ? null : String.join(",", selectedPhotoNames));

        String sql = "INSERT INTO post (content, forum_id, user_id, likes, photos, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, newPost.getContent());
            pstmt.setLong(2, currentForum.getId());
            pstmt.setInt(3, 1); // Replace with actual user ID
            pstmt.setInt(4, newPost.getLikes());
            pstmt.setString(5, newPost.getPhotos());
            pstmt.setTimestamp(6, Timestamp.valueOf(newPost.getCreatedAt()));
            pstmt.setTimestamp(7, Timestamp.valueOf(newPost.getUpdatedAt()));

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) newPost.setId(rs.getInt(1));
            }

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                posts.add(newPost);
                postContent.clear();
                photosFlowPane.getChildren().clear();
                selectedPhotoNames.clear();
                moderationErrorLabel.setVisible(false);
            });

        } catch (SQLException e) {
            Platform.runLater(() ->
                    showAlert("Database Error", "Create post failed: " + e.getMessage())
            );
        }
    }

    private void playPopAnimation(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }


    private Node createPhotoThumbnail(String fileName, String imageUrl) {
        VBox container = new VBox(5);
        container.getStyleClass().add("photo-thumbnail");

        ImageView imageView = new ImageView(new Image(imageUrl));
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCursor(Cursor.HAND);

        // Add hover effect
        imageView.setOnMouseEntered(e -> imageView.setOpacity(0.8));
        imageView.setOnMouseExited(e -> imageView.setOpacity(1.0));

        // Add click handler for preview
        imageView.setOnMouseClicked(e -> {
            List<String> photos = List.of(fileName);
            showImageLightbox(photos, 0);
        });

        Button removeBtn = new Button("X");
        removeBtn.getStyleClass().add("remove-photo-btn");
        removeBtn.setOnAction(e -> {
            selectedPhotoNames.remove(fileName);
            photosFlowPane.getChildren().remove(container);
        });

        container.getChildren().addAll(imageView, removeBtn);
        return container;
    }

    private void handleEditPost(Post post) {
        // Create a dialog for the user to input new content
        TextInputDialog dialog = new TextInputDialog(post.getContent());
        dialog.setTitle("Edit Post");
        dialog.setHeaderText("Edit your post content");

        // Show dialog and get result
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newContent -> {
            // If content is empty, return early and show an error message
            if (newContent.trim().isEmpty()) {
                showAlert("Error", "Content cannot be empty!");
                return;
            }

            // SQL to update the post content in the database
            String sql = "UPDATE post SET content=?, updated_at=? WHERE id=?";
            try (Connection conn = MyDataBase.getInstance().getCnx();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, newContent);  // Set new content
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));  // Set updated timestamp
                pstmt.setInt(3, post.getId());  // Set the post id for the correct record

                // Execute the update query
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Successfully updated, so refresh the posts and show a success alert
                    loadPosts();
                    showSuccessAlert("Success", "Post updated!");
                } else {
                    // If no rows are affected, show an error (shouldn't happen in normal case)
                    showAlert("Error", "Post update failed, please try again.");
                }

            } catch (SQLException e) {
                // Show database error if there's an issue with the update
                showAlert("Error", "Update failed: " + e.getMessage());
            }
        });
    }


    private void handleDeletePost(Post post) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete this post?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM post WHERE id=?";
            try (Connection conn = MyDataBase.getInstance().getCnx();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, post.getId());
                pstmt.executeUpdate();
                posts.remove(post);
                showSuccessAlert("Success", "Post deleted!");

            } catch (SQLException e) {
                showAlert("Error", "Delete failed: " + e.getMessage());
            }
        }
    }

    private void showSuccessAlert(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private String sanitizeFilename(String originalName) {
        // Remove path traversal characters and replace special characters
        return originalName
                .replaceAll("[\\\\/:*?\"<>|]", "_")  // Replace problematic characters
                .replaceAll("\\s+", "_")             // Replace whitespace with single underscore
                .trim();                             // Remove leading/trailing spaces
    }
    private void checkWritingQuality(String content) {
        Task<List<WritingQualityService.WritingIssue>> writingTask = new Task<>() {
            @Override
            protected List<WritingQualityService.WritingIssue> call() throws Exception {
                return writingService.checkText(content);
            }
        };

        writingTask.setOnSucceeded(e -> {
            List<WritingQualityService.WritingIssue> issues = writingTask.getValue();
            if (!issues.isEmpty()) {
                showWritingFeedback(issues);
            } else {
                createCleanPost(content);
            }
        });

        writingTask.setOnFailed(e -> {
            writingFeedbackContainer.setVisible(false);
            createCleanPost(content); // Post anyway if check fails
        });

        new Thread(writingTask).start();
    }

    private void showWritingFeedback(List<WritingQualityService.WritingIssue> issues) {
        ObservableList<String> feedbackItems = FXCollections.observableArrayList();

        issues.forEach(issue -> {
            String feedback = String.format("[%s] %s",
                    issue.getCategory(),
                    issue.getMessage());
            feedbackItems.add(feedback);
        });

        Platform.runLater(() -> {
            writingIssuesList.setItems(feedbackItems);
            writingFeedbackContainer.setVisible(true);
            submitPostBtn.setText("Post Anyway");
        });
    }
    private void resetPostState() {
        hasPostAttempt = false;
        hasQualityIssues = false;
        submitPostBtn.setText("Post");
    }

    private void showModerationError() {
        moderationErrorLabel.setText("Your post contains inappropriate language");
        moderationErrorLabel.setVisible(true);
        playPopAnimation(postInputContainer);
    }

    private void showSentimentError() {
        sentimentErrorLabel.setText("Negative sentiment detected");
        sentimentErrorLabel.setVisible(true);
        playPopAnimation(postInputContainer);
    }

    @FXML
    private void handleReturnToForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ForumFront.fxml"));
            Parent root = loader.load();

            // Refresh forum data when returning
            ForumFrontController controller = loader.getController();
            controller.loadForums();

            Stage currentStage = (Stage) returnButton.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Forums");
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not return to forums: " + e.getMessage());
        }
    }
}