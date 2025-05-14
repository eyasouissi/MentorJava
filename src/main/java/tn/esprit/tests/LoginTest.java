package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tn.esprit.services.UserService;
import tn.esprit.tools.HostServicesProvider;
import tn.esprit.utils.StripeConfig;

import java.io.IOException;

public class LoginTest extends Application {
    private static BorderPane rootPane; // Shared root container
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        StripeConfig.initializeStripe();

        // Initialize root container
        rootPane = new BorderPane();

        // Load login screen into center initially
        loadLoginScreen();

        primaryStage.setTitle("WorkAway");
        primaryStage.setScene(new Scene(rootPane, 1200, 800));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    // Method to load login screen
    public static void loadLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(LoginTest.class.getResource("/interfaces/auth/login.fxml"));
            Parent loginView = loader.load();
            rootPane.setCenter(loginView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load main interface after login
    public static void loadMainInterface() {
        try {
            // Load sidebar
            FXMLLoader sidebarLoader = new FXMLLoader(LoginTest.class.getResource("/interfaces/Sidebar.fxml"));
            Parent sidebar = sidebarLoader.load();
            rootPane.setLeft(sidebar);

            // Load study room
            FXMLLoader studyLoader = new FXMLLoader(LoginTest.class.getResource("/interfaces/rooms/StudyRoom.fxml"));
            Parent studyRoom = studyLoader.load();
            rootPane.setCenter(studyRoom);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}