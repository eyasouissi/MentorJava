package tn.esprit.controllers.Front;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.entities.User;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import tn.esprit.controllers.user.ProfileController;
import tn.esprit.controllers.user.admin.AdminProfileController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.shape.Circle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
public class SidebarController {
    @FXML private ImageView sidebarProfileImage;

    private User currentUser;
    private static final Map<String, String> FXML_PATHS = new HashMap<>();
    private StackPane contentArea;

    private Timeline imageCheckTimeline;
    private String lastProfileImagePath;
    static {
        FXML_PATHS.put("Home", "");
        FXML_PATHS.put("Courses", "");
        FXML_PATHS.put("Forum", "");
        FXML_PATHS.put("Groups", "");
        FXML_PATHS.put("Events", "");
        FXML_PATHS.put("Profile", "/interfaces/user/profile.fxml");
        FXML_PATHS.put("AdminProfile", "/interfaces/user/admin/adminprofile.fxml");
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            this.lastProfileImagePath = user.getPfp();
        }
        updateProfileImage();
    }

    public void updateProfileImage() {
        if (currentUser != null) {
            try {
                final Image finalImage;
                Image image = null;

                if (currentUser.getPfp() != null && !currentUser.getPfp().isEmpty()) {
                    InputStream is = getClass().getResourceAsStream("/" + currentUser.getPfp());
                    if (is != null) {
                        image = new Image(is);
                    } else {
                        File file = new File(currentUser.getPfp());
                        if (!file.exists()) {
                            file = new File("uploads/" + currentUser.getPfp());
                        }
                        if (file.exists()) {
                            image = new Image(file.toURI().toString());
                        }
                    }
                }

                if (image == null) {
                    InputStream defaultStream = getClass().getResourceAsStream("/assets/images/profile.png");
                    if (defaultStream != null) {
                        image = new Image(defaultStream);
                    }
                }

                finalImage = image;

                if (finalImage != null) {
                    Platform.runLater(() -> {
                        sidebarProfileImage.setImage(finalImage);
                        sidebarProfileImage.setFitWidth(32);
                        sidebarProfileImage.setFitHeight(32);
                        sidebarProfileImage.setPreserveRatio(true);
                        Circle clip = new Circle(16, 16, 16);
                        sidebarProfileImage.setClip(clip);
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void initialize() {
        // Initialize the clip for the profile image
        Circle clip = new Circle(16, 16, 16);
        sidebarProfileImage.setClip(clip);
        sidebarProfileImage.setFitWidth(32);
        sidebarProfileImage.setFitHeight(32);
        sidebarProfileImage.setPreserveRatio(true);

        // Initialize the timeline for checking image updates
        setupImageChecker();
    }

    @FXML
    private void navigateToHome() { loadView("Home"); }

    @FXML
    private void navigateToCourses() { loadView("Courses"); }

    @FXML
    private void navigateToForum() { loadView("Forum"); }

    @FXML
    private void navigateToGroups() { loadView("Groups"); }

    @FXML
    private void navigateToEvents() { loadView("Events"); }

    private void loadView(String viewName) {
        try {
            String fxmlPath = FXML_PATHS.get(viewName);
            if (fxmlPath != null && contentArea != null) {
                Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Logout failed: " + e.getMessage());
        }
    }

    @FXML
    private void goToProfile(MouseEvent event) {
        try {
            String fxmlPath = "/interfaces/user/profile.fxml";

            if (currentUser != null && currentUser.getRoles().contains("ROLE_ADMIN")) {
                fxmlPath = "/interfaces/user/admin/adminprofile.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (fxmlPath.contains("adminprofile")) {
                AdminProfileController adminController = loader.getController();
                adminController.setUserData(currentUser);
            } else {
                ProfileController profileController = loader.getController();
                profileController.setUserData(currentUser);
            }

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle(fxmlPath.contains("adminprofile") ? "Admin Profile" : "User Profile");
                stage.show();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupImageChecker() {
        imageCheckTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> checkForImageUpdates())
        );
        imageCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        imageCheckTimeline.play();
    }

    private void checkForImageUpdates() {
        if (currentUser != null) {
            String currentPath = currentUser.getPfp();

            // Check if the path has changed or if the file has been modified
            if (!currentPath.equals(lastProfileImagePath) ||
                    (currentPath != null && !currentPath.isEmpty() && isFileModified(currentPath))) {

                lastProfileImagePath = currentPath;
                updateProfileImage();
            }
        }
    }

    private boolean isFileModified(String path) {
        try {
            File file;
            if (path.startsWith("/")) {
                // Resource path
                return false; // Resources can't be modified at runtime
            } else {
                // File system path
                file = new File(path);
                if (!file.exists()) {
                    file = new File("uploads/" + path);
                }
                return file.exists() && file.lastModified() > System.currentTimeMillis() - 2000;
            }
        } catch (Exception e) {
            return false;
        }
    }
}