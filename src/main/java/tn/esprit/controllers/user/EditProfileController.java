package tn.esprit.controllers.user;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.FileUploadUtil;
import tn.esprit.services.AIImageService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class EditProfileController {
    @FXML protected ImageView profileImageView;
    @FXML protected ImageView backgroundImageView;
    @FXML protected TextField nameField;
    @FXML protected TextField ageField;
    @FXML protected TextField countryField;
    @FXML protected TextField specialityField;
    @FXML protected TextArea bioArea;
    @FXML protected Label diplomaFileNameLabel;

    protected User currentUser;
    protected File profileImageFile;
    protected File backgroundImageFile;
    protected File diplomaFile;
    private ProfileController parentController;

    public void setUserData(User user) {
        this.currentUser = user;
        loadUserData();
    }

    public void setParentController(ProfileController parentController) {
        this.parentController = parentController;
    }

    protected void loadUserData() {
        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            ageField.setText(currentUser.getAge() != null ? currentUser.getAge().toString() : "");
            countryField.setText(currentUser.getCountry() != null ? currentUser.getCountry() : "");
            specialityField.setText(currentUser.getSpeciality() != null ? currentUser.getSpeciality() : "");
            bioArea.setText(currentUser.getBio() != null ? currentUser.getBio() : "");

            // Load images
            loadImage(profileImageView, currentUser.getPfp(), "/assets/images/pfp/default-profile.png");
            loadImage(backgroundImageView, currentUser.getBg(), "/assets/images/bg/default-bg.jpg");

            // Set diploma file name if exists
            if (currentUser.getDiplome() != null && !currentUser.getDiplome().isEmpty()) {
                File file = FileUploadUtil.getUploadedFile(currentUser.getDiplome());
                diplomaFileNameLabel.setText(file != null && file.exists() ? file.getName() : "File not found");
            }
        }
    }

    protected void loadImage(ImageView imageView, String path, String defaultPath) {
        try {
            if (path != null && !path.isEmpty()) {
                // Try as resource first
                InputStream is = getClass().getResourceAsStream("/" + path);
                if (is != null) {
                    imageView.setImage(new Image(is));
                    return;
                }

                // Try as uploaded file
                File file = FileUploadUtil.getUploadedFile(path);
                if (file != null && file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    return;
                }
            }

            // Load default if specified path fails
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

    @FXML
    protected void handleChangeProfilePicture() {
        File file = showFileChooser("Select Profile Picture", "*.png", "*.jpg", "*.jpeg");
        if (file != null) {
            profileImageFile = file;
            profileImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void handleChangeBackgroundImage() {
        File file = showFileChooser("Select Background Image", "*.png", "*.jpg", "*.jpeg");
        if (file != null) {
            backgroundImageFile = file;
            backgroundImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void handleUploadDiploma() {
        File file = showFileChooser("Select Diploma PDF", "*.pdf");
        if (file != null) {
            if (file.length() > FileUploadUtil.MAX_FILE_SIZE) {
                showAlert("Error", "File size exceeds 10MB limit");
                return;
            }
            diplomaFile = file;
            diplomaFileNameLabel.setText(file.getName());
        }
    }

    protected File showFileChooser(String title, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Files", extensions));
        return fileChooser.showOpenDialog(diplomaFileNameLabel.getScene().getWindow());
    }

    @FXML
    protected void handleSave() {
        try {
            // Validate and update fields
            currentUser.setName(nameField.getText().trim());

            if (!ageField.getText().isEmpty()) {
                currentUser.setAge(Integer.parseInt(ageField.getText()));
            } else {
                currentUser.setAge(null);
            }

            currentUser.setCountry(countryField.getText().trim());
            currentUser.setSpeciality(specialityField.getText().trim());
            currentUser.setBio(bioArea.getText().trim());

            // Handle file uploads
            if (profileImageFile != null) {
                String pfpPath = FileUploadUtil.uploadFile(profileImageFile, "pfp");
                currentUser.setPfp(pfpPath);
            }

            if (backgroundImageFile != null) {
                String bgPath = FileUploadUtil.uploadFile(backgroundImageFile, "bg");
                currentUser.setBg(bgPath);
            }

            if (diplomaFile != null) {
                String diplomaPath = FileUploadUtil.uploadFile(diplomaFile, "diplomas");
                currentUser.setDiplome(diplomaPath);
            }

            // Save to database
            UserService userService = new UserService();
            userService.modifier(currentUser);

            // Refresh parent controller if exists
            if (parentController != null) {
                parentController.refreshUserData();
            }

            // Close window
            ((Stage) profileImageView.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            showAlert("Invalid Age", "Please enter a valid number for age.");
        } catch (IOException e) {
            showAlert("Upload Error", "Failed to upload file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "Failed to save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleCancel() {
        ((Stage) profileImageView.getScene().getWindow()).close();
    }

    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void handleGenerateProfilePicture() {
        try {
            TextInputDialog dialog = new TextInputDialog("A futuristic avatar");
            dialog.setTitle("Generate Profile Picture");
            dialog.setHeaderText("Describe your desired profile picture");
            dialog.setContentText("Prompt:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().isEmpty()) {
                ProgressIndicator progress = new ProgressIndicator();
                StackPane root = new StackPane(progress);
                Scene scene = new Scene(root, 200, 200);
                Stage loadingStage = new Stage();
                loadingStage.setScene(scene);
                loadingStage.show();

                new Thread(() -> {
                    try {
                        File generatedImage = AIImageService.generateImage(result.get());

                        // Save the generated image to the uploads directory
                        String pfpPath = FileUploadUtil.uploadFile(generatedImage, "pfp");
                        currentUser.setPfp(pfpPath);

                        // Update the database
                        UserService userService = new UserService();
                        userService.modifier(currentUser);

                        Platform.runLater(() -> {
                            loadingStage.close();
                            profileImageFile = generatedImage;
                            profileImageView.setImage(new Image(generatedImage.toURI().toString()));
                            showAlert("Success", "Profile picture generated and saved successfully!");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            loadingStage.close();
                            showAlert("Generation Error", "Failed to generate image: " + e.getMessage());
                        });
                    }
                }).start();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to start generation: " + e.getMessage());
        }
    }
}