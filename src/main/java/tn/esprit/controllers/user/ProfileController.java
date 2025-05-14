package tn.esprit.controllers.user;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.controllers.auth.MainController;
import tn.esprit.entities.User;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.services.UserService;
import tn.esprit.tools.HostServicesProvider;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ProfileController {
    @FXML private ImageView profileImageView;
    @FXML private ImageView backgroundImageView;
    @FXML private Label nameLabel;
    @FXML private Label ageLabel;
    @FXML private Label countryLabel;
    @FXML private Label bioLabel;
    @FXML private Label specialityLabel;
    @FXML private Label diplomaLabel;
    @FXML private Hyperlink diplomaLink;
    @FXML private Button editButton;
    @FXML private HBox badgesContainer;
    @FXML private Label noBadgesLabel;
    @FXML private Label studyTimeLabel;

    private User currentUser;
    private MainController mainController;
    private long totalStudySeconds = 0;
    
    // Badge configuration - mirror of the MainController configuration
    private static final Map<Long, String> BADGES = new LinkedHashMap<>();
    private static final Map<Long, String> BADGE_DESCRIPTIONS = new LinkedHashMap<>();
    
    static {
        // First badge after 5 minutes (300 seconds)
        BADGES.put(300L, "/badges/badge1.png");
        BADGE_DESCRIPTIONS.put(300L, "First Steps: 5 minutes of study time");

        // Second badge after 1 hour (3600 seconds)
        BADGES.put(3600L, "/badges/badge2.png");
        BADGE_DESCRIPTIONS.put(3600L, "Dedicated Learner: 1 hour of study time");

        // Subsequent badges with 5-hour increments (18000 seconds)
        long currentThreshold = 3600L;
        for(int i = 3; i <= 24; i++) {
            currentThreshold += 5 * 3600; // Add 5 hours in seconds
            BADGES.put(currentThreshold, "/badges/badge" + i + ".png");
            
            // Create descriptions for each badge
            int hours = (int)(currentThreshold / 3600);
            String description;
            
            if (i == 3) description = "Knowledge Explorer: " + hours + " hours of study time";
            else if (i == 4) description = "Dedicated Scholar: " + hours + " hours of study time";
            else if (i == 5) description = "Learning Champion: " + hours + " hours of study time";
            else if (i == 6) description = "Study Master: " + hours + " hours of study time";
            else if (i <= 10) description = "Academic Virtuoso: " + hours + " hours of study time";
            else if (i <= 15) description = "Knowledge Sage: " + hours + " hours of study time";
            else if (i <= 20) description = "Wisdom Guardian: " + hours + " hours of study time";
            else description = "Learning Legend: " + hours + " hours of study time";
            
            BADGE_DESCRIPTIONS.put(currentThreshold, description);
        }
    }

    public void setUserData(User user) {
        this.currentUser = user;
        updateUI();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        Circle clip = new Circle(75, 75, 75);
        profileImageView.setClip(clip);

        try {
            Image editIcon = new Image(getClass().getResourceAsStream("/assets/images/icons/edit-icon.png"));
            ImageView editIconView = new ImageView(editIcon);
            editIconView.setFitHeight(20);
            editIconView.setFitWidth(20);
            editButton.setGraphic(editIconView);
        } catch (Exception e) {
            editButton.setText("Edit");
        }

        editButton.setOnAction(event -> handleEditProfile());
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/auth/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load login screen: " + e.getMessage());
        }
    }

    private void updateUI() {
        if (currentUser == null) return;

        nameLabel.setText(currentUser.getName());
        ageLabel.setText(currentUser.getAge() != null ? currentUser.getAge().toString() : "Not specified");
        countryLabel.setText(nonEmptyOrDefault(currentUser.getCountry(), "Not specified"));
        bioLabel.setText(nonEmptyOrDefault(currentUser.getBio(), "No bio yet"));
        specialityLabel.setText(nonEmptyOrDefault(currentUser.getSpeciality(), "Not specified"));

        handleDiplomaDisplay();
        loadImage(profileImageView, currentUser.getPfp(), "/assets/images/pfp/default-profile.png");
        loadImage(backgroundImageView, currentUser.getBg(), "/assets/images/bg/default-bg.jpg");
        
        // Load user badges
        loadUserBadges();
    }

    private void handleDiplomaDisplay() {
        String diplomaPath = currentUser.getDiplome();
        if (diplomaPath != null && !diplomaPath.isEmpty()) {
            File file = resolveFile(diplomaPath);
            if (file != null && file.exists()) {
                diplomaLabel.setText(file.getName());
                diplomaLink.setVisible(true);
                diplomaLink.setOnAction(event -> openDiplomaPDF());
            } else {
                diplomaLabel.setText("File not found");
                diplomaLink.setVisible(false);
            }
        } else {
            diplomaLabel.setText("No diploma uploaded");
            diplomaLink.setVisible(false);
        }
    }

    @FXML
    private void openDiplomaPDF() {
        String path = currentUser.getDiplome();
        if (path == null || path.isEmpty()) {
            showAlert("Information", "No diploma file has been uploaded yet.");
            return;
        }

        File diplomaFile = resolveFile(path);
        if (diplomaFile == null || !diplomaFile.exists()) {
            showAlert("Error", "Diploma file not found");
            return;
        }

        javafx.application.HostServices hostServices = HostServicesProvider.getHostServices();
        if (hostServices != null) {
            hostServices.showDocument(diplomaFile.getAbsolutePath());
        } else {
            showAlert("Information", "Please open the file manually at:\n" + diplomaFile.getAbsolutePath());
        }
    }

    private File resolveFile(String path) {
        File file = new File(path);
        if (file.exists()) return file;

        file = new File("uploads/" + path);
        if (file.exists()) return file;

        if (path.startsWith("pfp/") || path.startsWith("bg/")) {
            file = new File("uploads/" + path);
        } else {
            file = new File("uploads/pfp/" + path);
            if (!file.exists()) file = new File("uploads/bg/" + path);
        }

        return file.exists() ? file : null;
    }

    private void loadImage(ImageView imageView, String path, String defaultPath) {
        try {
            File file = resolveFile(path);
            if (file != null && file.exists()) {
                imageView.setImage(new Image(file.toURI().toString()));
                return;
            }

            if (defaultPath != null) {
                InputStream defaultStream = getClass().getResourceAsStream(defaultPath);
                if (defaultStream != null) {
                    imageView.setImage(new Image(defaultStream));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    private void handleEditProfile() {
        try {
            URL location = getClass().getResource("/interfaces/user/EditProfile.fxml");
            if (location == null) {
                throw new IllegalStateException("FXML file not found at /interfaces/user/EditProfile.fxml");
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            EditProfileController controller = loader.getController();
            controller.setUserData(currentUser);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshUserData();
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load edit profile: " + e.getMessage());
        }
    }

    public void refreshUserData() {
        UserService userService = new UserService();
        User updatedUser = userService.getById(currentUser.getId());
        if (updatedUser != null) {
            currentUser = updatedUser;
            updateUI();

            UserSession.getInstance().setCurrentUser(updatedUser);
            if (mainController != null) {
                mainController.updateUserInfo(updatedUser);
                mainController.notifyProfilePictureUpdated(updatedUser.getPfp());
            } else {
                System.out.println("Warning: mainController is null in ProfileController");
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

    private String nonEmptyOrDefault(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public MainController getMainController() {
        return mainController;
    }

    private void loadUserBadges() {
        // Clear the badges container
        if (badgesContainer != null) {
            badgesContainer.getChildren().clear();
            
            // First load the study time
            loadStudyTime();
            
            // Then add badges based on study time
            Set<Long> earnedBadges = new HashSet<>();
            for (Long threshold : BADGES.keySet()) {
                if (totalStudySeconds >= threshold) {
                    earnedBadges.add(threshold);
                    addBadgeToContainer(BADGES.get(threshold));
                }
            }
            
            // Show message if no badges
            if (noBadgesLabel != null) {
                noBadgesLabel.setVisible(earnedBadges.isEmpty());
                if (earnedBadges.isEmpty()) {
                    badgesContainer.getChildren().add(noBadgesLabel);
                }
            }
        }
    }
    
    private void loadStudyTime() {
        try {
            // Load study time from the same file as MainController uses
            String timeFile = "study_time_" + currentUser.getId() + ".dat";
            java.nio.file.Path path = Paths.get(timeFile);
            if (Files.exists(path)) {
                String content = Files.readString(path);
                totalStudySeconds = Long.parseLong(content.trim());
                
                // Update study time label
                if (studyTimeLabel != null) {
                    long hours = totalStudySeconds / 3600;
                    long minutes = (totalStudySeconds % 3600) / 60;
                    studyTimeLabel.setText(String.format("Total study time: %d hours, %d minutes", hours, minutes));
                }
            } else if (studyTimeLabel != null) {
                studyTimeLabel.setText("No study time recorded yet");
            }
        } catch (Exception e) {
            System.err.println("Error loading study time: " + e.getMessage());
            if (studyTimeLabel != null) {
                studyTimeLabel.setText("Error loading study time");
            }
        }
    }
    
    private void addBadgeToContainer(String badgeImagePath) {
        try {
            // Get the threshold from the badge path
            Long threshold = null;
            for (Map.Entry<Long, String> entry : BADGES.entrySet()) {
                if (entry.getValue().equals(badgeImagePath)) {
                    threshold = entry.getKey();
                    break;
                }
            }
            
            ImageView badgeImage = new ImageView(new Image(getClass().getResourceAsStream(badgeImagePath)));
            badgeImage.setFitWidth(50);
            badgeImage.setFitHeight(50);
            
            // Add a tooltip with the badge description
            if (threshold != null && BADGE_DESCRIPTIONS.containsKey(threshold)) {
                String description = BADGE_DESCRIPTIONS.get(threshold);
                Tooltip tooltip = new Tooltip(description);
                tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #6a3093; -fx-text-fill: white;");
                Tooltip.install(badgeImage, tooltip);
            }
            
            // Add hover effect to badge
            setupBadgeHoverEffect(badgeImage);
            
            // Create a VBox to hold badge and optional label
            VBox badgeBox = new VBox(5);
            badgeBox.setAlignment(Pos.CENTER);
            badgeBox.getChildren().add(badgeImage);
            
            badgesContainer.getChildren().add(badgeBox);
        } catch (Exception e) {
            System.err.println("Error adding badge: " + e.getMessage());
        }
    }
    
    private void setupBadgeHoverEffect(ImageView badgeImage) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), badgeImage);
        scaleUp.setToX(1.2);
        scaleUp.setToY(1.2);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), badgeImage);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        badgeImage.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.playFromStart();
        });
        
        badgeImage.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.playFromStart();
        });
    }

    @FXML
    private void showAllAchievements() {
        try {
            // Create a dialog to show all possible achievements
            Stage achievementsStage = new Stage();
            achievementsStage.initModality(Modality.APPLICATION_MODAL);
            achievementsStage.setTitle("All Achievements");
            
            // Create a scrollable grid to display badges
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            scrollPane.setPrefWidth(600);
            
            // Create a flow pane to hold badges in a grid
            FlowPane badgesGrid = new FlowPane();
            badgesGrid.setHgap(20);
            badgesGrid.setVgap(20);
            badgesGrid.setPadding(new Insets(20));
            badgesGrid.setPrefWrapLength(550); // width to wrap at
            badgesGrid.setStyle("-fx-background-color: #f5f0fa;");
            
            // Add all badges to the grid
            for (Map.Entry<Long, String> entry : BADGES.entrySet()) {
                VBox badgeBox = createAchievementBadge(entry.getKey(), entry.getValue());
                badgesGrid.getChildren().add(badgeBox);
            }
            
            scrollPane.setContent(badgesGrid);
            
            // Create a container with a title
            VBox container = new VBox(15);
            container.setPadding(new Insets(20));
            container.setStyle("-fx-background-color: white;");
            
            Label titleLabel = new Label("Study Time Achievements");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #6a3093;");
            
            Label descriptionLabel = new Label("Earn badges by studying and tracking your time in the app!");
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
            
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #6a3093; -fx-text-fill: white;");
            closeButton.setOnAction(e -> achievementsStage.close());
            
            container.getChildren().addAll(titleLabel, descriptionLabel, scrollPane, closeButton);
            
            // Set the scene and show the dialog
            Scene scene = new Scene(container);
            achievementsStage.setScene(scene);
            achievementsStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error showing achievements: " + e.getMessage());
            showAlert("Error", "Could not display achievements: " + e.getMessage());
        }
    }
    
    private VBox createAchievementBadge(Long threshold, String badgePath) {
        VBox badgeBox = new VBox(10);
        badgeBox.setAlignment(Pos.CENTER);
        badgeBox.setPrefWidth(150);
        
        try {
            ImageView badgeImage = new ImageView(new Image(getClass().getResourceAsStream(badgePath)));
            badgeImage.setFitWidth(64);
            badgeImage.setFitHeight(64);
            
            // Format the threshold into a readable form
            String timeDescription;
            if (threshold < 60) {
                timeDescription = threshold + " seconds";
            } else if (threshold < 3600) {
                timeDescription = (threshold / 60) + " minutes";
            } else {
                timeDescription = (threshold / 3600) + " hours";
            }
            
            Label badgeTitle = new Label(BADGE_DESCRIPTIONS.get(threshold));
            badgeTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
            badgeTitle.setWrapText(true);
            badgeTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            // Check if the user has earned this badge
            boolean isEarned = totalStudySeconds >= threshold;
            String statusText = isEarned ? "✓ Earned" : "⟡ Locked";
            String statusColor = isEarned ? "#4CAF50" : "#888888";
            
            Label statusLabel = new Label(statusText);
            statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-weight: " + 
                                (isEarned ? "bold" : "normal") + ";");
            
            badgeBox.getChildren().addAll(badgeImage, badgeTitle, statusLabel);
            
            // Gray out the badge if not earned
            if (!isEarned) {
                badgeImage.setOpacity(0.4);
                badgeBox.setOpacity(0.7);
            }
        } catch (Exception e) {
            System.err.println("Error creating achievement badge: " + e.getMessage());
        }
        
        return badgeBox;
    }
}
