package tn.esprit.controllers.user;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.opencv.imgproc.Imgproc;
import tn.esprit.entities.User;
import tn.esprit.services.FaceRecognitionService;
import tn.esprit.controllers.auth.UserSession;
import tn.esprit.services.UserService;
import tn.esprit.tools.FileUploadUtil;
import tn.esprit.services.AIImageService;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

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

    @FXML
    public void initialize() {
        try {
            System.load("C:\\opencv\\build\\java\\x64\\opencv_java451.dll");
            System.out.println("OpenCV library loaded successfully.");
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV: " + e.getMessage());
            Platform.runLater(() -> showAlert("Error", "Failed to initialize camera. Please restart the application."));
        }

        // Initialize the FaceRecognitionService only after OpenCV is loaded
        if (!FaceRecognitionService.isInitialized()) {
            System.err.println("Face recognition service not initialized properly");
        }
    }

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

            // Use the same default paths as ProfileController
            loadImage(profileImageView, currentUser.getPfp(), "/assets/images/pfp/default-profile.png");
            loadImage(backgroundImageView, currentUser.getBg(), "/assets/images/bg/default-bg.jpg");

            if (currentUser.getDiplome() != null && !currentUser.getDiplome().isEmpty()) {
                File file = FileUploadUtil.getUploadedFile(currentUser.getDiplome());
                diplomaFileNameLabel.setText(file != null && file.exists() ? file.getName() : "File not found");
            }
        }
    }
    protected void loadImage(ImageView imageView, String path, String defaultPath) {
        try {
            if (path != null && !path.isEmpty()) {
                // First try to load from file system with relative path
                File file = new File(path);
                if (!file.exists()) {
                    // Try with uploads directory prefix if direct path doesn't work
                    file = new File("uploads/" + path);
                    if (!file.exists()) {
                        // Try with specific subdirectories
                        if (path.startsWith("pfp/") || path.startsWith("bg/")) {
                            file = new File("uploads/" + path);
                        } else {
                            file = new File("uploads/pfp/" + path);
                            if (!file.exists()) {
                                file = new File("uploads/bg/" + path);
                            }
                        }
                    }
                }

                if (file.exists()) {
                    String imageUrl = file.toURI().toString();
                    imageView.setImage(new Image(imageUrl));
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
            e.printStackTrace();
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

            // Create diplomas directory if it doesn't exist
            File diplomasDir = new File("uploads/diplomas");
            if (!diplomasDir.exists()) {
                diplomasDir.mkdirs();
            }

            // Generate unique filename
            String uniqueFileName = System.currentTimeMillis() + "_" + file.getName();
            diplomaFile = new File("uploads/diplomas/" + uniqueFileName);

            try {
                Files.copy(file.toPath(), diplomaFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                diplomaFileNameLabel.setText(diplomaFile.getName());
            } catch (IOException e) {
                showAlert("Error", "Failed to save diploma file");
                e.printStackTrace();
            }
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
            // Validate required fields
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Name is required");
                return;
            }

            // Update user object with form data
            currentUser.setName(nameField.getText().trim());

            try {
                if (!ageField.getText().isEmpty()) {
                    currentUser.setAge(Integer.parseInt(ageField.getText()));
                } else {
                    currentUser.setAge(null);
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Age", "Please enter a valid number for age");
                return;
            }

            currentUser.setCountry(countryField.getText().trim());
            currentUser.setSpeciality(specialityField.getText().trim());
            currentUser.setBio(bioArea.getText().trim());

            // Handle profile picture upload
            if (profileImageFile != null) {
                // Create pfp directory if it doesn't exist
                File pfpDir = new File("uploads/pfp");
                if (!pfpDir.exists()) {
                    pfpDir.mkdirs();
                }

                // Generate unique filename
                String uniqueFileName = System.currentTimeMillis() + "_" + profileImageFile.getName();
                File destinationFile = new File("uploads/pfp/" + uniqueFileName);

                // Copy the file
                Files.copy(profileImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Store relative path
                currentUser.setPfp("pfp/" + uniqueFileName);
            }

            // Handle background image upload
            if (backgroundImageFile != null) {
                // Create bg directory if it doesn't exist
                File bgDir = new File("uploads/bg");
                if (!bgDir.exists()) {
                    bgDir.mkdirs();
                }

                // Generate unique filename
                String uniqueFileName = System.currentTimeMillis() + "_" + backgroundImageFile.getName();
                File destinationFile = new File("uploads/bg/" + uniqueFileName);

                // Copy the file
                Files.copy(backgroundImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Store relative path
                currentUser.setBg("bg/" + uniqueFileName);
            }

            // Handle diploma upload
            if (diplomaFile != null) {
                currentUser.setDiplome("diplomas/" + diplomaFile.getName());
            }

            // Save to database
            UserService userService = new UserService();
            userService.modifier(currentUser);

            // Refresh the current user from database
            User updatedUser = userService.getById(currentUser.getId());
            currentUser = updatedUser;

            // Update session with fresh user data
            UserSession.getInstance().setCurrentUser(updatedUser);

            // Refresh parent view if exists
            if (parentController != null) {
                parentController.refreshUserData();

                // Add delay to ensure all updates are processed
                Platform.runLater(() -> {
                    if (parentController.getMainController() != null) {
                        parentController.getMainController().updateUserInfo(updatedUser);
                        parentController.getMainController().notifyProfilePictureUpdated(updatedUser.getPfp());
                    }
                });
            }

            // Close the edit window
            ((Stage) profileImageView.getScene().getWindow()).close();

            // Show success message
            showSuccessAlert("Profile Updated", "Your profile has been updated successfully!");

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid information in all fields");
        } catch (IOException e) {
            showAlert("File Upload Error", "Failed to upload files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
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
    @FXML
    protected void handleCancel() {
        ((Stage) profileImageView.getScene().getWindow()).close();
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
                        String pfpPath = FileUploadUtil.uploadFile(generatedImage, "pfp");
                        currentUser.setPfp(pfpPath);
                        new UserService().modifier(currentUser);

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

    @FXML
    protected void handleRegisterFace() {
        try {
            // Load OpenCV
            nu.pattern.OpenCV.loadShared();
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            if (currentUser == null) return;

            // Verify face recognition service is properly initialized
            if (!FaceRecognitionService.isInitialized()) {
                showAlert("Error", "Face recognition service not initialized properly");
                return;
            }

            Stage captureStage = new Stage();
            VBox root = new VBox();
            Scene scene = new Scene(root, 640, 480);

            ImageView imageView = new ImageView();
            imageView.setFitWidth(600);
            imageView.setFitHeight(400);

            Button captureButton = new Button("Capture Face for Registration");
            captureButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

            root.getChildren().addAll(imageView, captureButton);
            captureStage.setScene(scene);
            captureStage.setTitle("Register Face");

            // Initialize capture variable and ensure it's effectively final
            VideoCapture capture = new VideoCapture(0, Videoio.CAP_ANY); // Use CAP_ANY to automatically select the correct backend
            if (!capture.isOpened()) {
                showAlert("Error", "Could not access the camera");
                return;
            }

            captureStage.setOnCloseRequest(e -> {
                if (capture.isOpened()) {
                    capture.release();
                }
            });

            // Animation timer can access capture because it's now effectively final
            javafx.animation.AnimationTimer frameGrabber = new javafx.animation.AnimationTimer() {
                @Override
                public void handle(long now) {
                    Mat frame = new Mat();
                    if (capture.read(frame)) {
                        try {
                            Image image = mat2Image(frame);
                            Platform.runLater(() -> imageView.setImage(image));
                        } finally {
                            frame.release();
                        }
                    }
                }
            };
            frameGrabber.start();

            captureButton.setOnAction(e -> {
                Mat frame = new Mat();
                if (capture.read(frame)) {
                    frameGrabber.stop();
                    capture.release();
                    captureStage.close();

                    try {
                        // Convert to grayscale for better face detection
                        Mat grayFrame = new Mat();
                        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                        Image image = mat2Image(grayFrame);

                        if (!FaceRecognitionService.registerFace(currentUser.getId(), imageView)) {
                            showAlert("Registration Failed", "No face detected");
                        } else {
                            showAlert("Registration Success", "Face registered successfully!");
                        }
                    } catch (Exception ex) {
                        showAlert("Error", "Failed to register face: " + ex.getMessage());
                    }
                }
            });

            captureStage.show();
        } catch (Exception ex) {
            showAlert("Error", "An error occurred while registering the face: " + ex.getMessage());
        }
    }

    private Image mat2Image(Mat mat) {
        try {
            // Convert the Mat to a byte array
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".bmp", mat, matOfByte);

            // Convert the byte array to an Image
            byte[] byteArray = matOfByte.toArray();
            InputStream inputStream = new ByteArrayInputStream(byteArray);
            return new Image(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
