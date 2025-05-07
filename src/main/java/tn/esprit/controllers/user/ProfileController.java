package tn.esprit.controllers.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
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

    private User currentUser;
    private MainController mainController;

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String nonEmptyOrDefault(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public MainController getMainController() {
        return mainController;
    }
}
