package tn.esprit.controllers.auth;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import tn.esprit.entities.User;
import tn.esprit.services.FaceRecognitionService;
import tn.esprit.services.UserService;
import tn.esprit.controllers.user.admin.UserCrud;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Login {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label errorLabel;
    @FXML private ImageView richardImage;
    @FXML private TextField complimentField;
    @FXML private VBox passwordBox;
    @FXML private Button faceLoginButton;

    private final UserService userService = UserService.getInstance();
    private VideoCapture capture;

    // OpenCV initialization moved to instance block
    {
        try {
            nu.pattern.OpenCV.loadLocally(); // This will load OpenCV from Maven
            System.out.println("OpenCV loaded successfully.");
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV: " + e.getMessage());
            Platform.runLater(() -> showError("Failed to initialize camera. Please restart the application."));
        }
    }

    @FXML
    public void initialize() {
        try {
            String initialImagePath = "/assets/icons/richard_bored.png";
            InputStream imageStream = getClass().getResourceAsStream(initialImagePath);

            if (imageStream == null) {
                throw new RuntimeException("Richard image not found at: " + initialImagePath);
            }

            Image boredImage = new Image(imageStream);
            richardImage.setImage(boredImage);
            passwordBox.setVisible(false);

            complimentField.textProperty().addListener((obs, oldVal, newVal) -> {
                evaluateCompliment(newVal);
            });

        } catch (Exception e) {
            System.err.println("Error initializing Richard: " + e.getMessage());
            errorLabel.setText("Richard is unavailable! Try restarting the app.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFaceLogin(ActionEvent event) {
        try {
            System.out.println("Starting face login...");
            nu.pattern.OpenCV.loadLocally();
            startCameraCapture((Stage) emailField.getScene().getWindow());
        } catch (Exception e) {
            showError("Camera features not available. Please install OpenCV.");
            System.err.println("Error during face login: " + e.getMessage());
        }
    }

    private void startCameraCapture(Stage primaryStage) {
        try {
            Stage cameraStage = new Stage();
            VBox root = new VBox();
            Scene scene = new Scene(root, 640, 480);

            ImageView imageView = new ImageView();
            imageView.setFitWidth(600);
            imageView.setFitHeight(400);

            Button captureButton = new Button("Capture Face");
            captureButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

            root.getChildren().addAll(imageView, captureButton);
            cameraStage.setScene(scene);
            cameraStage.setTitle("Face Recognition");

            capture = new VideoCapture(0);
            if (!capture.isOpened()) {
                showError("Could not access camera");
                return;
            }

            cameraStage.setOnCloseRequest(e -> {
                if (capture != null && capture.isOpened()) {
                    capture.release();
                }
            });

            javafx.animation.AnimationTimer frameGrabber = new javafx.animation.AnimationTimer() {
                @Override
                public void handle(long now) {
                    Mat frame = new Mat();
                    if (capture.read(frame)) {
                        Image image = mat2Image(frame);
                        Platform.runLater(() -> imageView.setImage(image));
                    }
                }
            };
            frameGrabber.start();

            captureButton.setOnAction(e -> {
                Mat frame = new Mat();
                if (capture.read(frame)) {
                    frameGrabber.stop();
                    if (capture.isOpened()) {
                        capture.release();
                    }
                    cameraStage.close();
                    processCapturedFrame(frame, primaryStage);
                }
            });

            cameraStage.show();
        } catch (Exception e) {
            showError("Error starting camera: " + e.getMessage());
            System.err.println("Error initializing camera capture: " + e.getMessage());
        }
    }

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new java.io.ByteArrayInputStream(buffer.toArray()));
    }

    private void processCapturedFrame(Mat frame, Stage primaryStage) {
        try {
            Image image = mat2Image(frame);
            if (image != null) {
                ImageView imageView = new ImageView(image);

                for (User user : userService.getAll()) {
                    if (FaceRecognitionService.verifyFace(user.getId(), imageView)) {
                        redirectToMainPage(user);
                        return;
                    }
                }
                showError("Face not recognized. Please try again or use email login.");
            }
        } catch (Exception e) {
            showError("Error processing face: " + e.getMessage());
            System.err.println("Error processing captured frame: " + e.getMessage());
        }
    }

    private void evaluateCompliment(String text) {
        try {
            text = text.toLowerCase();

            if (text.contains("handsome") || text.contains("awesome") ||
                    text.contains("great") || text.contains("best") ||
                    text.contains("cute") || text.contains("adorable") ||
                    text.contains("cool") || text.contains("amazing")) {
                setRichardImage("richard_happy.png");
                passwordBox.setVisible(true);
            }
            else if (text.contains("ugly") || text.contains("stupid") ||
                    text.contains("boring") || text.contains("worst") ||
                    text.contains("dumb")) {
                setRichardImage("richard_mad.png");
                passwordBox.setVisible(false);
            }
            else {
                setRichardImage("richard_bored.png");
                passwordBox.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Error evaluating compliment: " + e.getMessage());
        }
    }

    private void setRichardImage(String imageName) {
        try {
            String imagePath = "/assets/icons/" + imageName;
            InputStream imageStream = getClass().getResourceAsStream(imagePath);

            if (imageStream == null) {
                throw new RuntimeException("Cannot find image: " + imagePath);
            }

            Image image = new Image(imageStream);
            richardImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading Richard image: " + e.getMessage());
            errorLabel.setText("Richard is moody today - try again!");
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        if (!passwordBox.isVisible()) {
            showError("Please make Richard happy first!");
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            if (email.isEmpty() || password.isEmpty()) {
                showError("Email and password are required");
                return;
            }

            User user = userService.getByEmail(email);
            if (user == null) {
                showError("Invalid email or password");
                return;
            }

            if (!userService.verifyPassword(email, password)) {
                showError("Invalid email or password");
                return;
            }

            if (user.isRestricted()) {
                showError("Account restricted. Contact support.");
                return;
            }

            if (!user.isVerified()) {
                showAlert("Warning", "Your account is not yet verified.\nSome features may be limited.");
            }

            redirectToMainPage(user);
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            System.err.println("Error during login: " + e.getMessage());
        }
    }

    private void redirectToMainPage(User user) {
        try {
            String fxmlPath = user.getRoles().contains("ROLE_ADMIN")
                    ? "/interfaces/user/admin/user_crud.fxml"
                    : "/interfaces/auth/main.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (user.getRoles().contains("ROLE_ADMIN")) {
                ((UserCrud)loader.getController()).initializeWithUser(user);
            } else {
                ((MainController)loader.getController()).initializeWithUser(user, "Welcome back!");
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(user.getRoles().contains("ROLE_ADMIN") ? "Admin Dashboard" : "Main Application");
            stage.show();
        } catch (IOException e) {
            showError("Cannot redirect to main page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/reset-password.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Cannot open password reset");
        }
    }

    @FXML
    public void handleSignUpRedirect(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/auth/RoleChoice.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Cannot open registration");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }
}
