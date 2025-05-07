package tn.esprit.controllers.Front;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
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
import java.util.HashMap;
import java.util.Map;

public class SidebarController {
    @FXML private ImageView sidebarProfileImage;
    @FXML private Label usernameLabel;

    private User currentUser;
    private static final Map<String, String> FXML_PATHS = new HashMap<>();
    private StackPane contentArea;
    private UserSession sessionService = UserSession.getInstance();

    private Timeline imageCheckTimeline;
    private String lastProfileImagePath;

    static {
        FXML_PATHS.put("Home", "/interfaces/auth/main.fxml");
        FXML_PATHS.put("Courses", "/interfaces/Courses/MainCourseFront.fxml");
        FXML_PATHS.put("Forum", "/interfaces/ForumFront.fxml");
        FXML_PATHS.put("Groups", "/group/GroupsView.fxml");
        FXML_PATHS.put("Events", "/interfaces/front/AnnonceList.fxml");
        FXML_PATHS.put("Profile", "/interfaces/user/profile.fxml");
        FXML_PATHS.put("AdminProfile", "/interfaces/user/admin/adminprofile.fxml");
        FXML_PATHS.put("StudyRoom", "/interfaces/rooms/StudyRoom.fxml");
        FXML_PATHS.put("Subscription", "");


    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            this.lastProfileImagePath = user.getPfp();
            usernameLabel.setText(user.getName());
        }
        updateProfileImage();
    }

    public void updateProfileImage() {
        this.currentUser = sessionService.getCurrentUser();
        if (currentUser != null) {
            try {
                Image image = null;
                String imagePath = currentUser.getPfp();
                File file = new File(imagePath);
                if (!file.exists()) file = new File("uploads/" + imagePath);
                if (!file.exists()) file = new File("uploads/pfp/" + imagePath);
                if (file.exists()) {
                    image = new Image(file.toURI().toString());
                } else {
                    InputStream defaultStream = getClass().getResourceAsStream("/assets/images/profile.png");
                    if (defaultStream != null) {
                        image = new Image(defaultStream);
                    }
                }

                if (image != null) {
                    final Image finalImage = image;
                    Platform.runLater(() -> {
                        sidebarProfileImage.setImage(finalImage);
                        sidebarProfileImage.setFitWidth(32);
                        sidebarProfileImage.setFitHeight(32);
                        sidebarProfileImage.setPreserveRatio(true);
                        sidebarProfileImage.setClip(new Circle(16, 16, 16));
                        sidebarProfileImage.requestFocus();
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }
    }

    public void initialize() {
        this.currentUser = sessionService.getCurrentUser();
        if (currentUser == null) {
            // Vérifier si la redirection vers la page de login a déjà eu lieu.
            if (!isLoginPageDisplayed()) {
                redirectToLogin();
            }
            return;
        }

        usernameLabel.setText(currentUser.getName());
        updateProfileImage();

        sidebarProfileImage.setClip(new Circle(16, 16, 16));
        sidebarProfileImage.setFitWidth(32);
        sidebarProfileImage.setFitHeight(32);
        sidebarProfileImage.setPreserveRatio(true);

        setupImageChecker();
    }

    private boolean isLoginPageDisplayed() {
        Stage currentStage = (Stage) (contentArea != null ? contentArea.getScene().getWindow() : null);
        if (currentStage != null) {
            Scene scene = currentStage.getScene();
            if (scene != null && scene.getRoot() != null) {
                String rootFXML = scene.getRoot().getId();  // Assurez-vous que votre page de login a un ID ou une autre méthode pour la reconnaître.
                return rootFXML != null && rootFXML.equals("loginPage");  // Ajustez cette ligne selon votre logique de validation.
            }
        }
        return false;
    }


    private void redirectToLogin() {
        Platform.runLater(() -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/login.fxml"));
                Stage stage = (Stage) (contentArea != null ? contentArea.getScene().getWindow() : new Stage());
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Failed to redirect to login: " + e.getMessage());
            }
        });
    }

    private boolean checkSessionBeforeAction() {
        if (sessionService.getCurrentUser() == null) {
            showAlert("Session Expired", "Your session has expired. Please login again.");
            redirectToLogin();
            return false;
        }
        return true;
    }

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

    @FXML private void navigateToHome()    { if (checkSessionBeforeAction()) loadView("Home"); }
    @FXML private void navigateToCourses() { if (checkSessionBeforeAction()) loadView("Courses"); }
    @FXML private void navigateToForum()   { if (checkSessionBeforeAction()) loadView("Forum"); }
    @FXML private void navigateToGroups()  { if (checkSessionBeforeAction()) loadView("Groups"); }
    @FXML private void navigateToEvents()  { if (checkSessionBeforeAction()) loadView("Events"); }
    @FXML private void navigateTOStudyRoom()  { if (checkSessionBeforeAction()) loadView("StudyRoom"); }
    @FXML private void navigateTOSubscription()  { if (checkSessionBeforeAction()) loadView("Subscription"); }




    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            sessionService.clearSession();
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
        if (!checkSessionBeforeAction()) return;
        try {
            String fxmlPath = currentUser.getRoles().contains("ROLE_ADMIN") ?
                    "/interfaces/user/admin/adminprofile.fxml" :
                    "/interfaces/user/profile.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (fxmlPath.contains("adminprofile")) {
                AdminProfileController ctrl = loader.getController();
                ctrl.setUserData(currentUser);
            } else {
                ProfileController ctrl = loader.getController();
                ctrl.setUserData(currentUser);
            }

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Profile");
                stage.show();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load profile: " + e.getMessage());
        }
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
            return file.exists() && file.lastModified() > System.currentTimeMillis() - 2000;
        } catch (Exception e) {
            return false;
        }
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
