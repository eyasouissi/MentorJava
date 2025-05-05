package tn.esprit.controllers.user;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.imgproc.Imgproc;
import tn.esprit.entities.User;
import tn.esprit.services.FaceRecognitionService;
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
            InputStream is = null;

            if (path != null && !path.isEmpty()) {
                is = getClass().getResourceAsStream(path.startsWith("/") ? path : "/" + path);
                if (is != null) {
                    imageView.setImage(new Image(is));
                    return;
                }

                File file = FileUploadUtil.getUploadedFile(path);
                if (file != null && file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    return;
                }

                System.err.println("Could not load image for: " + path);
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
            currentUser.setName(nameField.getText().trim());

            if (!ageField.getText().isEmpty()) {
                currentUser.setAge(Integer.parseInt(ageField.getText()));
            } else {
                currentUser.setAge(null);
            }

            currentUser.setCountry(countryField.getText().trim());
            currentUser.setSpeciality(specialityField.getText().trim());
            currentUser.setBio(bioArea.getText().trim());

            if (profileImageFile != null) {
                String pfpPath = FileUploadUtil.uploadFile(profileImageFile, "pfp");
                currentUser.setPfp(pfpPath);
            }

            if (backgroundImageFile != null) {
                String bgPath = FileUploadUtil.uploadFile(backgroundImageFile, "bg");
                currentUser.setBg(bgPath);
            }

            if (diplomaFile != null) {
                currentUser.setDiplome(diplomaFile.getPath());
            }

            new UserService().modifier(currentUser);

            if (parentController != null) {
                parentController.refreshUserData();
            }

            ((Stage) profileImageView.getScene().getWindow()).close();
        } catch (NumberFormatException e) {
            showAlert("Invalid Age", "Please enter a valid number for age.");
        } catch (IOException e) {
            showAlert("Upload Error", "Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Failed to save: " + e.getMessage());
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
