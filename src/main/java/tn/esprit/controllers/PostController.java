package tn.esprit.controllers;

import javafx.animation.Interpolator;
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
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.entities.Comment;
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
import java.time.format.DateTimeFormatter;
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
    private User currentUser;


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

    // In PostController.java
    private User getCurrentUser() {
        return UserSession.getInstance().getCurrentUser();
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

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void loadPosts() {
        posts.clear();

        String postSql = "SELECT p.*, u.name as user_name, u.pfp as user_pfp, " +
                "(SELECT COUNT(*) FROM post_likes WHERE post_id = p.id) as likes, " +
                "EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.id AND user_id = ?) as has_liked " +
                "FROM post p " +
                "JOIN user u ON p.user_id = u.id " +
                "WHERE forum_id = ?";

        String commentSql = "SELECT c.*, u.name as commenter_name, u.pfp as commenter_pfp " +
                "FROM comment c " +
                "JOIN user u ON c.user_id = u.id " +
                "WHERE c.post_id = ? " +
                "ORDER BY c.createdAt ASC";

        String likesSql = "SELECT u.id, u.name, u.pfp FROM post_likes pl " +
                "JOIN user u ON pl.user_id = u.id " +
                "WHERE pl.post_id = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement postStmt = conn.prepareStatement(postSql);
             PreparedStatement commentStmt = conn.prepareStatement(commentSql);
             PreparedStatement likesStmt = conn.prepareStatement(likesSql)) {

            User currentUser = UserSession.getInstance().getCurrentUser();
            postStmt.setLong(1, currentUser != null ? currentUser.getId() : -1);
            postStmt.setLong(2, currentForum.getId());

            ResultSet postRs = postStmt.executeQuery();

            while (postRs.next()) {
                Post post = new Post();
                // Basic post info
                post.setId(postRs.getInt("id"));
                post.setContent(postRs.getString("content"));

                // Handle post dates
                Timestamp postCreated = postRs.getTimestamp("created_at");
                post.setCreatedAt(postCreated != null ?
                        postCreated.toLocalDateTime() :
                        LocalDateTime.now());

                Timestamp postUpdated = postRs.getTimestamp("updated_at");
                post.setUpdatedAt(postUpdated != null ?
                        postUpdated.toLocalDateTime() :
                        LocalDateTime.now());

                post.setLikes(postRs.getInt("likes"));

                // Post author
                User postUser = new User();
                postUser.setId(postRs.getLong("user_id"));
                postUser.setName(postRs.getString("user_name"));
                postUser.setPfp(postRs.getString("user_pfp"));
                post.setUser(postUser);

                // Photos
                String photos = postRs.getString("photos");
                if (photos != null && !photos.isEmpty()) {
                    post.setPhotos(photos);
                }

                // Load comments
                List<Comment> comments = new ArrayList<>();
                commentStmt.setInt(1, post.getId());
                try (ResultSet commentRs = commentStmt.executeQuery()) {
                    while (commentRs.next()) {
                        Comment comment = new Comment();
                        comment.setId(commentRs.getLong("id"));
                        comment.setContent(commentRs.getString("content"));

                        // Handle comment date
                        Timestamp commentCreated = commentRs.getTimestamp("createdAt");
                        comment.setCreatedAt(commentCreated != null ?
                                commentCreated.toLocalDateTime() :
                                LocalDateTime.now());

                        // Comment author
                        User commentUser = new User();
                        commentUser.setId(commentRs.getLong("user_id"));
                        commentUser.setName(commentRs.getString("commenter_name"));
                        commentUser.setPfp(commentRs.getString("commenter_pfp"));
                        comment.setUser(commentUser);

                        comments.add(comment);
                    }
                }
                post.setComments(comments);

                // Load likes
                Set<User> likers = new HashSet<>();
                likesStmt.setInt(1, post.getId());
                try (ResultSet likesRs = likesStmt.executeQuery()) {
                    while (likesRs.next()) {
                        User liker = new User();
                        liker.setId(likesRs.getLong("id"));
                        liker.setName(likesRs.getString("name"));
                        liker.setPfp(likesRs.getString("pfp"));
                        likers.add(liker);
                    }
                }
                post.setLikedByUsers(likers);

                // Add current user's like status
                if (currentUser != null && postRs.getBoolean("has_liked")) {
                    post.getLikedByUsers().add(currentUser);
                }

                posts.add(post);
            }

            postsListView.setItems(posts);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading posts: " + e.getMessage());
            e.printStackTrace();
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

        User user = post.getUser();

        // --- Header: Profile Pic + Info ---
        HBox postHeader = new HBox(10);
        postHeader.getStyleClass().add("post-header");
        postHeader.setAlignment(Pos.CENTER_LEFT);

        ImageView profilePic = new ImageView();
        profilePic.setFitWidth(50);
        profilePic.setFitHeight(50);
        profilePic.setPreserveRatio(false);
        profilePic.setSmooth(true);
        Circle clip = new Circle(25, 25, 25);
        profilePic.setClip(clip);
        profilePic.setImage(loadDefaultImage());

        try {
            if (user != null) {
                String pfpPath = user.getPfp();
                if (pfpPath != null && !pfpPath.isEmpty()) {
                    File imageFile = new File(pfpPath);
                    if (!imageFile.exists()) imageFile = new File("uploads/" + pfpPath);
                    if (!imageFile.exists()) imageFile = new File("uploads/pfp/" + pfpPath);
                    if (imageFile.exists()) {
                        Image userImage = new Image(imageFile.toURI().toString());
                        profilePic.setImage(userImage);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
            profilePic.setImage(loadDefaultImage());
        }

        StackPane avatarContainer = new StackPane(profilePic);
        avatarContainer.setPrefSize(50, 50);

        VBox userInfo = new VBox(5);
        userInfo.getStyleClass().add("user-info");

        Label userName = new Label(user != null && user.getName() != null ? user.getName() : "User");
        userName.getStyleClass().add("user-name");

        Label postDate = new Label("Posted: " + post.getCreatedAt().toLocalDate());
        postDate.getStyleClass().add("post-date");

        userInfo.getChildren().addAll(userName, postDate);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- Action Buttons (Edit/Delete) ---
        HBox topRightActions = new HBox(10);
        topRightActions.setAlignment(Pos.TOP_RIGHT);
        User currentUser = UserSession.getInstance().getCurrentUser();
        boolean isCurrentUsersPost = currentUser != null && user != null && currentUser.getId().equals(user.getId());

        if (isCurrentUsersPost) {
            ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/edit.png")));
            editIcon.setFitWidth(20);
            editIcon.setFitHeight(20);
            Button editBtn = new Button("", editIcon);
            editBtn.setStyle("-fx-background-color: transparent;");
            editBtn.getStyleClass().add("icon-button");
            Tooltip.install(editBtn, new Tooltip("Edit"));
            editBtn.setOnAction(e -> handleEditPost(post));

            ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/delete.png")));
            deleteIcon.setFitWidth(20);
            deleteIcon.setFitHeight(20);
            Button deleteBtn = new Button("", deleteIcon);
            deleteBtn.setStyle("-fx-background-color: transparent;");
            deleteBtn.getStyleClass().add("icon-button");
            Tooltip.install(deleteBtn, new Tooltip("Delete"));
            deleteBtn.setOnAction(e -> handleDeletePost(post));

            topRightActions.getChildren().addAll(editBtn, deleteBtn);
        }

        HBox headerWrapper = new HBox(postHeader, spacer, topRightActions);
        headerWrapper.setAlignment(Pos.CENTER_LEFT);
        postHeader.getChildren().addAll(avatarContainer, userInfo);

        // --- Post Content ---
        TextArea postContent = new TextArea(post.getContent());
        postContent.getStyleClass().add("post-content");
        postContent.setWrapText(true);
        postContent.setEditable(false);
        postContent.setPrefRowCount(4);

        card.getChildren().addAll(headerWrapper, postContent);

        // --- Photo Gallery ---
        if (post.getPhotos() != null && !post.getPhotos().isEmpty()) {
            List<String> photos = Arrays.asList(post.getPhotos().split(","));

            StackPane galleryContainer = new StackPane();
            galleryContainer.setAlignment(Pos.CENTER);
            galleryContainer.setPrefSize(400, 300);

            ImageView currentImage = new ImageView();
            currentImage.setPreserveRatio(true);
            currentImage.setFitWidth(380);
            currentImage.setFitHeight(280);
            currentImage.setSmooth(true);
            loadGalleryImage(photos.get(0), currentImage);

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

            Label counterLabel = new Label("1/" + photos.size());
            counterLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.5);");
            counterLabel.setPadding(new Insets(5));

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

            currentImage.setOnMouseClicked(e -> showLightbox(photos, currentIndex.get()));
            galleryContainer.getChildren().addAll(currentImage, counterLabel, navButtons);
            card.getChildren().add(galleryContainer);
        }

        // --- Translate Section ---
        ImageView translateIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/translate.png")));
        translateIcon.setFitWidth(20);
        translateIcon.setFitHeight(20);
        Button translateBtn = new Button("", translateIcon);
        translateBtn.getStyleClass().add("translate-btn");
        Tooltip.install(translateBtn, new Tooltip("Translate"));

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
            translateMenu.getItems().add(langItem);
        }

        final String originalContent = post.getContent();
        final TextArea finalPostContent = postContent;

        Button showOriginalBtn = new Button("Show Original");
        showOriginalBtn.getStyleClass().add("show-original-btn");
        showOriginalBtn.setVisible(false);

        translateBtn.setOnAction(e -> {
            if (!showOriginalBtn.isVisible()) {
                translateMenu.show(translateBtn, Side.BOTTOM, 0, 0);
            } else {
                finalPostContent.setText(originalContent);
                showOriginalBtn.setVisible(false);
                translateBtn.getStyleClass().remove("active-translate");
            }
        });

        for (MenuItem langItem : translateMenu.getItems()) {
            langItem.setOnAction(e -> {
                String targetLang = languageCodes.get(langItem.getText());
                handleTranslationSelection(originalContent, targetLang, null, finalPostContent, showOriginalBtn);
            });
        }

        HBox translateContainer = new HBox(5, translateBtn, showOriginalBtn);
        translateContainer.setAlignment(Pos.CENTER_LEFT);

        // --- Like Button Section ---
        ImageView likeIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/like.png")));
        likeIcon.setFitWidth(20);
        likeIcon.setFitHeight(20);

        Button likeBtn = new Button("", likeIcon);
        likeBtn.getStyleClass().add("like-btn");
        Tooltip.install(likeBtn, new Tooltip("Like"));

        Label likeCounter = new Label(String.valueOf(post.getLikes()));
        likeCounter.getStyleClass().add("like-counter");

        HBox likersBubbles = createLikersBubbles(post);

        if (currentUser != null && post.hasLiked(currentUser)) {
            likeBtn.getStyleClass().add("liked");
        }

        likeBtn.setOnAction(e -> handleLike(post, likeBtn, likeCounter));

        HBox likeContainer = new HBox(5, likeBtn, likeCounter, likersBubbles);
        likeContainer.setAlignment(Pos.CENTER_RIGHT);

        // --- Comment Button Section ---
        ImageView commentIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/comment.png")));
        commentIcon.setFitWidth(20);
        commentIcon.setFitHeight(20);

        Button commentBtn = new Button("", commentIcon);
        commentBtn.getStyleClass().add("translate-btn"); // Use same style for identical look
        Tooltip.install(commentBtn, new Tooltip("Comment"));


        Label commentCounter = new Label(String.valueOf(post.getComments().size()));
        commentCounter.getStyleClass().add("comment-counter");

        HBox commentContainer = new HBox(5, commentBtn, commentCounter);
        commentContainer.setAlignment(Pos.CENTER_RIGHT);

        // --- Bottom Controls ---
        HBox bottomControls = new HBox(10);
        bottomControls.setAlignment(Pos.CENTER_LEFT);
        bottomControls.setPadding(new Insets(10, 0, 0, 0));

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        bottomControls.getChildren().addAll(likeContainer, commentContainer, translateContainer, bottomSpacer);

        VBox.setVgrow(postContent, Priority.ALWAYS);
        card.getChildren().add(bottomControls);

        // --- Comments Section ---
        VBox commentsSection = new VBox(10);
        commentsSection.setVisible(false);
        commentsSection.managedProperty().bind(commentsSection.visibleProperty());

        commentBtn.setOnAction(e -> commentsSection.setVisible(!commentsSection.isVisible()));

        // Comment Input
        HBox commentInput = new HBox(10);
        commentInput.setAlignment(Pos.CENTER_LEFT);
        commentInput.setPadding(new Insets(10, 40, 10, 40)); // adds space on the sides
        commentInput.setMaxWidth(Double.MAX_VALUE); // let the whole HBox grow if possible

// Current User Avatar
        ImageView currentUserPfp = new ImageView(loadCurrentUserPfp());
        currentUserPfp.setFitWidth(30);
        currentUserPfp.setFitHeight(30);
        currentUserPfp.setClip(new Circle(15, 15, 15));

// Comment Field
        TextArea commentField = new TextArea();
        commentField.setPromptText("Write a comment...");
        commentField.setPrefRowCount(1);
        commentField.setWrapText(true);
        commentField.setMaxWidth(Double.MAX_VALUE); // allow to grow horizontally
        HBox.setHgrow(commentField, Priority.ALWAYS); // make it stretch in HBox

// Send Button
        Button sendBtn = new Button("", new ImageView(new Image(getClass().getResourceAsStream("/icons/send.png"))));
        sendBtn.getStyleClass().add("send-btn");

// Add all elements


        commentInput.getChildren().addAll(currentUserPfp, commentField, sendBtn);
        commentsSection.getChildren().add(commentInput);

        // Existing Comments
        VBox commentsList = new VBox(5);
        post.getComments().forEach(comment ->
                commentsList.getChildren().add(createCommentCard(comment, post))
        );

        commentsSection.getChildren().add(commentsList);
        card.getChildren().add(commentsSection);

        // Send Button Handler
        sendBtn.setOnAction(e -> {
            String content = commentField.getText().trim();
            if (!content.isEmpty()) {
                createComment(post, content);
                commentField.clear();
            }
        });

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
                                            HBox translateControls,
                                            TextArea postContent,
                                            Button showOriginalBtn) {
        postContent.setText("Translating...");

        new Thread(() -> {
            try {
                String translated = TranslateController.translateText(originalText, targetLang);

                Platform.runLater(() -> {
                    postContent.setText(translated);
                    showOriginalBtn.setVisible(true);
                    translateControls.lookup(".show-original-btn")
                            .setStyle("-fx-text-fill: #007bff; -fx-underline: true;");
                    translateControls.lookup(".translate-btn")
                            .getStyleClass().add("active-translate");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    postContent.setText(originalText);
                    showOriginalBtn.setVisible(false);
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
        if (currentUser == null) {
            showAlert("Error", "No logged-in user found. Cannot create post.");
            return;
        }

        Post newPost = new Post();
        newPost.setContent(cleanContent);
        newPost.setForum(currentForum);
        newPost.setUser(currentUser); // Set the current user on the Post object
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
            pstmt.setInt(3, currentUser.getId().intValue());
            pstmt.setInt(4, newPost.getLikes());
            pstmt.setString(5, newPost.getPhotos());
            pstmt.setTimestamp(6, Timestamp.valueOf(newPost.getCreatedAt()));
            pstmt.setTimestamp(7, Timestamp.valueOf(newPost.getUpdatedAt()));

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) newPost.setId(rs.getInt(1));
            }

            Platform.runLater(() -> {
                posts.add(newPost);
                postContent.clear();
                photosFlowPane.getChildren().clear();
                selectedPhotoNames.clear();
                moderationErrorLabel.setVisible(false);
            });

        } catch (SQLException e) {
            Platform.runLater(() -> showAlert("Database Error", "Create post failed: " + e.getMessage()));
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
        // Create custom dialog stage
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Edit Post ✏️");

        // Create main container
        VBox container = new VBox(15);
        container.getStyleClass().add("edit-dialog");

        // Create text area with current content
        TextArea editArea = new TextArea(post.getContent());
        editArea.setWrapText(true);
        editArea.setPrefRowCount(4);
        editArea.getStyleClass().add("edit-textarea");

        // Create button container
        HBox buttonContainer = new HBox(12);
        buttonContainer.getStyleClass().add("button-container");
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        // Create buttons with emojis
        Button saveButton = new Button(" Save");
        saveButton.getStyleClass().add("save-btn");
        Button cancelButton = new Button(" Cancel");
        cancelButton.getStyleClass().add("cancel-btn");

        // Add emoji graphics
        cancelButton.setGraphic(new Text("❌"));

        // Add button actions
        saveButton.setOnAction(e -> {
            String newContent = editArea.getText().trim();
            if (newContent.isEmpty()) {
                showAlert("Error", "Content cannot be empty!");
                return;
            }
            updatePostInDatabase(post, newContent, dialog);
        });

        cancelButton.setOnAction(e -> dialog.close());

        buttonContainer.getChildren().addAll(cancelButton, saveButton);
        container.getChildren().addAll(editArea, buttonContainer);

        // Create scene with CSS
        Scene scene = new Scene(container, 400, 300);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/edit-dialog.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("CSS file not found, using inline fallback");
            // Fallback styling matching the lavender theme
            container.setStyle("-fx-background-color: #f8f5fa; "
                    + "-fx-border-color: #8c84a1; "
                    + "-fx-border-radius: 15px; "
                    + "-fx-padding: 20;");

            editArea.setStyle("-fx-border-color: #8c84a1; "
                    + "-fx-background-color: white; "
                    + "-fx-font-family: 'Comic Sans MS';");
        }

        dialog.setScene(scene);

        // Add cute scaling animation
        saveButton.hoverProperty().addListener((obs, oldVal, isHovering) -> {
            if (isHovering) {
                saveButton.setScaleX(1.05);
                saveButton.setScaleY(1.05);
            } else {
                saveButton.setScaleX(1.0);
                saveButton.setScaleY(1.0);
            }
        });

        dialog.showAndWait();
    }
    private void updatePostInDatabase(Post post, String newContent, Stage dialog) {
        String sql = "UPDATE post SET content=?, updated_at=? WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newContent);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, post.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                loadPosts();
                dialog.close();
                showSuccessAlert("Success", "Post updated successfully!");
            } else {
                showAlert("Error", "Post update failed. Please try again.");
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Update failed: " + ex.getMessage());
        }
    }

    private void handleDeletePost(Post post) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete this post?");
        confirm.setContentText("All comments and likes will be automatically deleted.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = MyDataBase.getInstance().getCnx();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "DELETE FROM post WHERE id = ?")) {

                pstmt.setInt(1, post.getId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    Platform.runLater(() -> {
                        posts.remove(post);
                        showSuccessAlert("Success", "Post deleted successfully!");
                    });
                }
            } catch (SQLException e) {
                Platform.runLater(() ->
                        showAlert("Error", "Delete failed: " + e.getMessage())
                );
            }
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
        switch(alertType) {
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

    // Update your existing alert methods
    private void showAlert(String title, String message) {
        showCustomAlert("error", title, message);
    }

    private void showSuccessAlert(String title, String message) {
        showCustomAlert("success", title, message);
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

    //likes

    private void animateLikeButton(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private void animateCounter(Label label) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), label);
        tt.setFromY(0);
        tt.setToY(-5);
        tt.setAutoReverse(true);
        tt.setCycleCount(4);
        tt.play();
    }

    private void handleLike(Post post, Button likeBtn, Label counter) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("Authentication Required", "You must be logged in to like posts");
            return;
        }

        boolean wasLiked = post.hasLiked(currentUser);
        post.toggleLike(currentUser);

        // Update database
        new Thread(() -> {
            try (Connection conn = MyDataBase.getInstance().getCnx()) {
                if (wasLiked) {
                    // Remove like
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "DELETE FROM post_likes WHERE user_id = ? AND post_id = ?")) {
                        pstmt.setInt(1, currentUser.getId().intValue());
                        pstmt.setInt(2, post.getId());
                        pstmt.executeUpdate();
                    }
                } else {
                    // Add like
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO post_likes (user_id, post_id) VALUES (?, ?)")) {
                        pstmt.setInt(1, currentUser.getId().intValue());
                        pstmt.setInt(2, post.getId());
                        pstmt.executeUpdate();
                    }
                }

                // Update post likes count
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE post SET likes = ? WHERE id = ?")) {
                    pstmt.setInt(1, post.getLikes());
                    pstmt.setInt(2, post.getId());
                    pstmt.executeUpdate();
                }

            } catch (SQLException ex) {
                Platform.runLater(() ->
                        showAlert("Database Error", "Could not update like: " + ex.getMessage()));
            }
        }).start();

        // Update UI
        Platform.runLater(() -> {
            animateLikeButton(likeBtn);
            animateCounter(counter);

            if (post.hasLiked(currentUser)) {
                likeBtn.getStyleClass().add("liked");
            } else {
                likeBtn.getStyleClass().remove("liked");
            }

            counter.setText(String.valueOf(post.getLikes()));
        });
    }


    //comments
    private Node createCommentCard(Comment comment, Post parentPost) {
        VBox commentCard = new VBox(5);
        commentCard.setStyle("-fx-padding: 10; -fx-background-color: #f4f4f4; -fx-background-radius: 8;");
        commentCard.setMinWidth(300); // Optional: set a minimum width to make sure the comment card is not too small

        // StackPane to hold comment card and the delete button on top-right
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-background-color: transparent;");

        // Header section (Profile picture, Username, and Date)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView pfp = new ImageView(loadProfileImage(comment.getUser().getPfp()));
        pfp.setFitWidth(40);
        pfp.setFitHeight(40);
        pfp.setClip(new Circle(20, 20, 20)); // Makes image round

        Label username = new Label(comment.getUser().getName());
        username.setStyle("-fx-font-weight: bold;");

        // Create VBox for username and date
        VBox usernameDateBox = new VBox();
        usernameDateBox.setSpacing(2);  // Space between username and date
        usernameDateBox.getChildren().add(username);

        // Format date to be shown under username
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = comment.getCreatedAt() != null
                ? comment.getCreatedAt().format(formatter)
                : "Unknown date";
        if (comment.isEdited()) {
            formattedDate += " (edited)";
        }
        Label dateLabel = new Label(formattedDate);
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");

        usernameDateBox.getChildren().add(dateLabel);  // Add date under the username

        header.getChildren().addAll(pfp, usernameDateBox);  // Add profile pic and username/date VBox to header

        // Comment content (TextArea)
        TextArea contentArea = new TextArea(comment.getContent());
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        contentArea.setMaxHeight(60);  // Make the content area shorter (limit height)

        // Delete Button (small PNG image) - Same as post delete button
        ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/delete.png")));
        deleteIcon.setFitWidth(20);
        deleteIcon.setFitHeight(20);

        Button deleteButton = new Button("", deleteIcon);
        deleteButton.setStyle("-fx-background-color: transparent;");
        deleteButton.getStyleClass().add("icon-button");
        Tooltip.install(deleteButton, new Tooltip("Delete"));

        // Show delete button only for current user's comments
        User currentUser = UserSession.getInstance().getCurrentUser();
        boolean isCurrentUserComment = currentUser != null
                && currentUser.getId().equals(comment.getUser().getId());
        deleteButton.setVisible(isCurrentUserComment);

        deleteButton.setOnAction(e -> {
            CommentController controller = new CommentController();
            controller.setCommentData(comment, parentPost, () -> refreshCommentsSection(parentPost));
            controller.handleDeleteComment();
        });

        // Add header and content to comment card
        VBox contentBox = new VBox(header, contentArea);
        contentBox.setSpacing(5);

        // Add delete button in the top-right of the card using StackPane
        stackPane.getChildren().addAll(commentCard, deleteButton);

        // Position delete button in the top-right corner of the comment card
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);

        // Add content to the comment card
        commentCard.getChildren().add(contentBox);

        return stackPane;
    }





    private void createComment(Post post, String content) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(UserSession.getInstance().getCurrentUser());
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());

        // Ensure SQL query includes the created_at field
        String sql = "INSERT INTO comment (content, user_id, post_id, createdAt) VALUES (?, ?, ?, ?)";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set the parameters for the SQL query
            pstmt.setString(1, comment.getContent());
            pstmt.setInt(2, comment.getUser().getId().intValue());
            pstmt.setInt(3, post.getId());
            pstmt.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt())); // Set created_at here

            // Execute the update and handle the result
            pstmt.executeUpdate();

            // Get the generated key (the comment ID)
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    comment.setId(rs.getLong(1));  // Set the generated ID for the comment
                }
            }

            // Add the comment to the post's comment list and refresh the UI
            post.getComments().add(comment);
            refreshCommentsSection(post);

        } catch (SQLException e) {
            showAlert("Error", "Failed to post comment: " + e.getMessage());
        }
    }


    private void refreshCommentsSection(Post post) {
        VBox commentsSection = (VBox) postsListView.lookup("#commentsSection_" + post.getId());
        if (commentsSection != null) {
            commentsSection.getChildren().clear();
            post.getComments().forEach(comment ->
                    commentsSection.getChildren().add(createCommentCard(comment, post))
            );
        }
    }

// Add these methods in the PostController class

    private Image loadCurrentUserPfp() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getPfp() != null) {
            return loadProfileImage(currentUser.getPfp());
        }
        return loadDefaultImage();
    }

    private Image loadProfileImage(String pfpPath) {
        try {
            if (pfpPath != null && !pfpPath.isEmpty()) {
                File imageFile = new File(pfpPath);
                if (!imageFile.exists()) imageFile = new File("uploads/" + pfpPath);
                if (!imageFile.exists()) imageFile = new File("uploads/pfp/" + pfpPath);
                if (imageFile.exists()) {
                    return new Image(imageFile.toURI().toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
        }
        return loadDefaultImage();
    }



    private HBox createLikersBubbles(Post post) {
        HBox bubbles = new HBox(-6); // light overlap
        bubbles.setAlignment(Pos.CENTER_LEFT);
        bubbles.setStyle("-fx-padding: 0 0 0 5;");

        List<User> likers = new ArrayList<>(post.getLikedByUsers());
        int totalLikers = likers.size();
        int showCount = Math.min(3, totalLikers);

        // Create profile bubbles
        for (int i = 0; i < showCount; i++) {
            ImageView bubble = createProfileBubble(likers.get(i));
            bubbles.getChildren().add(bubble);
        }

        // Only show the +X label if there are 4 or more likes
        if (totalLikers >= 4) {
            Label moreLabel = createMoreLabel(totalLikers - 3, post.getLikedByUsers());
            bubbles.getChildren().add(moreLabel);
        }

        // Bind the full popup to the bubbles as well
        createLikersPopup(bubbles, post.getLikedByUsers());

        return bubbles;
    }

    private Label createMoreLabel(int count, Set<User> likers) {
        Label label = new Label("+" + count);
        label.getStyleClass().add("more-likers-label");

        // Create popup content once (not on every hover)
        ContextMenu popup = new ContextMenu();
        popup.getStyleClass().add("likers-popup");

        // Populate popup content
        if (!likers.isEmpty()) {
            for (User user : likers) {
                MenuItem item = new MenuItem();
                HBox itemContent = createLikerItem(user); // Fixed size in createLikerItem
                item.setGraphic(itemContent);
                item.getStyleClass().add("liker-item");
                popup.getItems().add(item);
            }
        } else {
            MenuItem emptyItem = new MenuItem("No likes yet");
            emptyItem.setDisable(true);
            popup.getItems().add(emptyItem);
        }

        // Hover handling with smooth transitions
        final PauseTransition hoverDelay = new PauseTransition(Duration.millis(200));
        label.setOnMouseEntered(e -> {
            hoverDelay.setOnFinished(ev -> {
                if (!popup.isShowing()) {
                    popup.show(label, Side.BOTTOM, 0, 5);
                }
            });
            hoverDelay.play();
        });

        label.setOnMouseExited(e -> {
            hoverDelay.stop();
            popup.hide();
        });

        // Click handling to keep popup visible
        label.setOnMousePressed(e -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                popup.show(label, Side.BOTTOM, 0, 5);
            }
        });

        return label;
    }
    private ImageView createProfileBubble(User user) {
        ImageView bubble = new ImageView();
        bubble.setFitWidth(26);
        bubble.setFitHeight(26);
        bubble.setPreserveRatio(true);
        bubble.setSmooth(true);

        // Circular clip with CSS-based animation
        Circle clip = new Circle(13, 13, 13);
        bubble.setClip(clip);
        bubble.getStyleClass().add("profile-bubble"); // Add CSS class

        try {
            String pfpPath = user.getPfp();
            Image image;
            if (pfpPath != null && !pfpPath.isEmpty()) {
                Path imagePath = Paths.get("uploads", pfpPath);
                if (!Files.exists(imagePath)) {
                    imagePath = Paths.get("uploads/pfp", pfpPath);
                }
                image = new Image(imagePath.toUri().toString());
            } else {
                image = loadDefaultImage();
            }
            bubble.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
            bubble.setImage(loadDefaultImage());
        }

        return bubble;
    }

    private void createLikersPopup(Node anchor, Set<User> likers) {
        ContextMenu popup = new ContextMenu();
        popup.getStyleClass().add("likers-popup");

        // Use CSS for styling instead of inline styles
        popup.setStyle(null);

        if (!likers.isEmpty()) {
            for (User user : likers) {
                MenuItem item = new MenuItem();
                HBox itemContent = createLikerItem(user);
                item.setGraphic(itemContent);
                item.getStyleClass().add("liker-item");
                popup.getItems().add(item);
            }
        } else {
            MenuItem emptyItem = new MenuItem("No likes yet");
            emptyItem.setDisable(true);
            popup.getItems().add(emptyItem);
        }

        // Add hover delay to prevent flickering
        final PauseTransition hoverDelay = new PauseTransition(Duration.millis(300));
        anchor.setOnMouseEntered(e -> {
            hoverDelay.setOnFinished(ev -> {
                if (!popup.isShowing()) {
                    popup.show(anchor, Side.BOTTOM, 0, 5);
                }
            });
            hoverDelay.play();
        });

        anchor.setOnMouseExited(e -> {
            hoverDelay.stop();
            popup.hide();
        });
    }

    private HBox createLikerItem(User user) {
        HBox item = new HBox(8); // Reduced spacing
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("liker-item-container");

        ImageView pfp = createProfileBubble(user);
        pfp.setFitWidth(20);  // Smaller size for list items
        pfp.setFitHeight(20);

        Label name = new Label(user.getName());
        name.getStyleClass().add("liker-name");

        item.getChildren().addAll(pfp, name);
        return item;
    }


}