package tn.esprit.controllers.Admin;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.controllers.user.ProfileController;
import tn.esprit.controllers.user.admin.AdminProfileController;
import tn.esprit.entities.User;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SidebarController {
    // Dropdowns
    @FXML private VBox coursesDropdown;
    @FXML private VBox forumDropdown;
    @FXML private VBox groupesDropdown;
    @FXML private VBox eventsDropdown;
    @FXML private VBox pricingDropdown;
    @FXML private ImageView sidebarProfileImage;
    @FXML private Label usernameLabel;

    private User currentUser;
    private static final Map<String, String> FXML_PATHS = new HashMap<>();
    private StackPane contentArea;
    private UserSession sessionService = UserSession.getInstance();

    private Timeline imageCheckTimeline;
    private String lastProfileImagePath;
    private Button lastActiveButton;

    static {
        FXML_PATHS.put("Courses", "/interfaces/Courses/CoursesView.fxml");
        FXML_PATHS.put("Category", "/interfaces/Category/CategoryView.fxml");
        FXML_PATHS.put("Forum", "/interfaces/Forum.fxml");
        FXML_PATHS.put("Groupes", "/project/ProjectsView.fxml");
        FXML_PATHS.put("Projects", "/group/GroupsView.fxmll");
        FXML_PATHS.put("Events", "/interfaces/ListeEvenement.fxml");
        FXML_PATHS.put("Announcements", "/interfaces/listeAnnonces.fxml");
        FXML_PATHS.put("Pricing", "/FXML/Admin/PricingView.fxml");
        FXML_PATHS.put("Subscription", "/FXML/Admin/SubscriptionView.fxml");
        FXML_PATHS.put("Dashboard", "/interfaces/Admin/AdminDashboard.fxml");
        FXML_PATHS.put("users", "/interfaces/user/admin/user_crud.fxml");

    }

    public void setContentArea(StackPane contentArea) {
        System.out.println("[DEBUG] Setting content area reference");
        this.contentArea = contentArea;
    }

    public void setCurrentUser(User user) {
        System.out.println("[DEBUG] Setting current user: " + (user != null ? user.getName() : "null"));
        this.currentUser = user;
        if (user != null) {
            this.lastProfileImagePath = user.getPfp();
            usernameLabel.setText(user.getName());
        }
        updateProfileImage();
    }

    @FXML
    public void initialize() {
        System.out.println("[DEBUG] Initializing SidebarController");
        this.currentUser = sessionService.getCurrentUser();

        if (currentUser == null) {
            System.out.println("[WARNING] No current user in session!");
            if (!isLoginPageDisplayed()) {
                redirectToLogin();
            }
            return;
        }

        System.out.println("[DEBUG] Current user: " + currentUser.getName());
        usernameLabel.setText(currentUser.getName());
        updateProfileImage();

        // Setup profile image display
        Circle clip = new Circle(24, 24, 24);
        sidebarProfileImage.setClip(clip);
        sidebarProfileImage.setFitWidth(48);
        sidebarProfileImage.setFitHeight(48);
        sidebarProfileImage.setPreserveRatio(true);

        setupImageChecker();
        hideAllDropdowns();
    }

    private void updateProfileImage() {
        System.out.println("[DEBUG] Updating profile image...");
        if (currentUser == null) {
            System.out.println("[WARNING] No current user to update image for");
            return;
        }

        try {
            String imagePath = currentUser.getPfp();
            System.out.println("[DEBUG] Profile image path: " + imagePath);

            Image image = null;
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (!file.exists()) file = new File("uploads/" + imagePath);
                if (!file.exists()) file = new File("uploads/pfp/" + imagePath);

                System.out.println("[DEBUG] Checking for image at: " + file.getAbsolutePath());

                if (file.exists()) {
                    System.out.println("[DEBUG] Loading profile image from file");
                    image = new Image(file.toURI().toString());
                }
            }

            if (image == null) {
                System.out.println("[DEBUG] Loading default profile image");
                InputStream defaultStream = getClass().getResourceAsStream("/assets/images/profile.png");
                if (defaultStream != null) {
                    image = new Image(defaultStream);
                }
            }

            if (image != null) {
                final Image finalImage = image;
                Platform.runLater(() -> {
                    sidebarProfileImage.setImage(finalImage);
                    System.out.println("[DEBUG] Profile image updated successfully");
                });
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load profile image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupImageChecker() {
        System.out.println("[DEBUG] Setting up image checker");
        imageCheckTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> checkForImageUpdates())
        );
        imageCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        imageCheckTimeline.play();
    }

    private void checkForImageUpdates() {
        if (currentUser != null) {
            String currentPath = currentUser.getPfp();
            if ((currentPath != null && !currentPath.equals(lastProfileImagePath)) ||
                    isFileModified(currentPath)) {
                lastProfileImagePath = currentPath;
                updateProfileImage();
            }
        }
    }

    private boolean isFileModified(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) file = new File("uploads/" + path);
            if (!file.exists()) file = new File("uploads/pfp/" + path);
            return file.exists() && file.lastModified() > System.currentTimeMillis() - 2000;
        } catch (Exception e) {
            return false;
        }
    }

    // Navigation methods
    @FXML
    public void navigateToCourses() { loadView("Courses"); }
    @FXML
    public void navigateToCategory() { loadView("Category"); }
    @FXML
    public void navigateToForum() { loadView("Forum"); }
    @FXML
    public void navigateToGroupes() { loadView("Groupes"); }
    @FXML
    public void navigateToProjects() { loadView("Projects"); }
    @FXML
    public void navigateToEvents() { loadView("Events"); }
    @FXML
    public void navigateToAnnouncements() { loadView("Announcements"); }
    @FXML
    public void navigateToPricing() { loadView("Pricing"); }
    @FXML
    public void navigateToSubscription() { loadView("Subscription"); }
    @FXML
    public void navigateToDashboard() { loadView("Dashboard"); }
    @FXML
    public void navigateToUsers() { loadView("users"); }



    private void loadView(String viewName) {
        System.out.println("[DEBUG] Attempting to load view: " + viewName);
        if (!checkSessionBeforeAction()) return;

        try {
            String fxmlPath = FXML_PATHS.get(viewName);
            if (fxmlPath == null) {
                System.err.println("[ERROR] No FXML path mapped for view: " + viewName);
                return;
            }

            System.out.println("[DEBUG] Loading FXML from: " + fxmlPath);
            Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(content);
                System.out.println("[DEBUG] View loaded successfully");
            } else {
                System.err.println("[ERROR] Content area is null!");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load view: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load " + viewName + " view");
        }
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        UserSession.getInstance().clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToProfile(MouseEvent event) {
        System.out.println("[DEBUG] Navigating to profile");

        // Check session first
        if (currentUser == null) {
            showAlert("Session Error", "No user logged in");
            redirectToLogin();
            return;
        }

        try {
            // Standardize path format and fix typo in adminprofile.fxml
            String fxmlPath = currentUser.getRoles().contains("ROLE_ADMIN")
                    ? "/interfaces/user/admin/adminprofile.fxml"  // Fixed typo (removed extra 'l')
                    : "/interfaces/user/Profile.fxml";  // Standardized path format

            System.out.println("[DEBUG] Loading profile FXML: " + fxmlPath);

            // Verify the FXML resource exists before loading
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("FXML file not found at: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            // Set user data to the appropriate controller
            if (fxmlPath.contains("adminprofile")) {
                AdminProfileController ctrl = loader.getController();
                ctrl.setUserData(currentUser);
            } else {
                ProfileController ctrl = loader.getController();
                ctrl.setUserData(currentUser);
            }

            // Handle content display
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle(currentUser.getName() + "'s Profile");
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load profile: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load profile view:\n" + e.getMessage());

            // Fallback to default profile view if admin profile fails
            if (e.getMessage().contains("adminprofile")) {
                System.out.println("[DEBUG] Trying fallback to regular profile view");
                goToProfileFallback();
            }
        }
    }

    // Fallback method for when admin profile fails
    private void goToProfileFallback() {
        try {
            String fallbackPath = "/interfaces/user/Profile.fxml";
            System.out.println("[DEBUG] Loading fallback profile: " + fallbackPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fallbackPath));
            Parent root = loader.load();

            ProfileController ctrl = loader.getController();
            ctrl.setUserData(currentUser);

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Fallback profile load failed: " + e.getMessage());
            showAlert("Error", "Could not load any profile view");
        }
    }

    // Helper methods
    private boolean checkSessionBeforeAction() {
        if (sessionService.getCurrentUser() == null) {
            System.out.println("[WARNING] Session expired - redirecting to login");
            showAlert("Session Expired", "Your session has expired. Please login again.");
            redirectToLogin();
            return false;
        }
        return true;
    }

    private boolean isLoginPageDisplayed() {
        Stage currentStage = (Stage) (contentArea != null ? contentArea.getScene().getWindow() : null);
        return currentStage != null && currentStage.getTitle().equals("Login");
    }

    private void redirectToLogin() {
        Platform.runLater(() -> {
            try {
                System.out.println("[DEBUG] Redirecting to login");
                Parent root = FXMLLoader.load(getClass().getResource("/FXML/Auth/Login.fxml"));
                Stage stage = (Stage) (contentArea != null ? contentArea.getScene().getWindow() : new Stage());
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
                stage.show();
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to redirect to login: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void hideAllDropdowns() {
        if (coursesDropdown != null) coursesDropdown.setVisible(false);
        if (forumDropdown != null) forumDropdown.setVisible(false);
        if (groupesDropdown != null) groupesDropdown.setVisible(false);
        if (eventsDropdown != null) eventsDropdown.setVisible(false);
        if (pricingDropdown != null) pricingDropdown.setVisible(false);
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
