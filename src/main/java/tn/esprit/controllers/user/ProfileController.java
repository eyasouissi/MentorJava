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
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.FileUploadUtil;
import tn.esprit.tools.HostServicesProvider;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    public void setUserData(User user) {
        this.currentUser = user;
        updateUI();
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
            e.printStackTrace();
        }
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

    private void updateUI() {
        if (currentUser != null) {
            nameLabel.setText(currentUser.getName());
            ageLabel.setText(currentUser.getAge() != null ? currentUser.getAge().toString() : "Not specified");
            countryLabel.setText(currentUser.getCountry() != null ? currentUser.getCountry() : "Not specified");
            bioLabel.setText(currentUser.getBio() != null ? currentUser.getBio() : "No bio yet");
            specialityLabel.setText(currentUser.getSpeciality() != null ? currentUser.getSpeciality() : "Not specified");

            // Handle diploma display
            if (currentUser.getDiplome() != null && !currentUser.getDiplome().isEmpty()) {
                File diplomaFile = FileUploadUtil.getUploadedFile(currentUser.getDiplome());
                if (diplomaFile != null && diplomaFile.exists()) {
                    diplomaLabel.setText(diplomaFile.getName());
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

            loadImage(profileImageView, currentUser.getPfp(), "/assets/images/pfp/default-profile.png");
            loadImage(backgroundImageView, currentUser.getBg(), "/assets/images/bg/default-bg.jpg");
        }
    }

    @FXML
    private void openDiplomaPDF() {
        try {
            if (currentUser.getDiplome() == null || currentUser.getDiplome().isEmpty()) {
                showAlert("Information", "No diploma file has been uploaded yet.");
                return;
            }

            File diplomaFile = FileUploadUtil.getUploadedFile(currentUser.getDiplome());
            if (diplomaFile == null || !diplomaFile.exists()) {
                showAlert("Error", "Diploma file not found");
                return;
            }

            javafx.application.HostServices hostServices = HostServicesProvider.getHostServices();
            if (hostServices != null) {
                hostServices.showDocument(diplomaFile.getAbsolutePath());
            } else {
                showAlert("Information",
                        "Could not automatically open the PDF.\n" +
                                "Please open the file manually at:\n" +
                                diplomaFile.getAbsolutePath());
            }

        } catch (Exception e) {
            showAlert("Error", "Could not open diploma: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadImage(ImageView imageView, String path, String defaultPath) {
        try {
            if (path != null && !path.isEmpty()) {
                // First try to load as resource
                InputStream is = getClass().getResourceAsStream("/" + path);
                if (is != null) {
                    imageView.setImage(new Image(is));
                    return;
                }

                // Then try to load as uploaded file
                File file = FileUploadUtil.getUploadedFile(path);
                if (file != null && file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    return;
                }
            }

            // Load default if specified
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/user/EditProfile.fxml"));
            Parent root = loader.load();

            EditProfileController controller = loader.getController();
            controller.setUserData(currentUser);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshUserData();
        } catch (IOException e) {
            showAlert("Error", "Could not load edit profile: " + e.getMessage());
        }
    }

    public void refreshUserData() {
        UserService userService = new UserService();
        User updatedUser = userService.getById(currentUser.getId());
        if (updatedUser != null) {
            currentUser = updatedUser;
            updateUI();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}