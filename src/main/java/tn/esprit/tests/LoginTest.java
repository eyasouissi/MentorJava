package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.services.UserService;
import tn.esprit.tools.HostServicesProvider;

public class LoginTest extends Application {
    static {
        // Fix version mismatch warning
        System.setProperty("javafx.version", "17.0.15");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Starting application...");

        // Initialize services
        UserService userService = UserService.getInstance();

        if (!userService.isEmailServiceAvailable()) {
            System.out.println("""
                ⚠ Email service disabled - verification will be manual
                To enable email:
                1. Create .env file with SMTP credentials
                2. Or set environment variables
                3. For Gmail, enable 2FA and create App Password
                """);
        }

        HostServicesProvider.setHostServices(getHostServices());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/auth/login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("WorkAway Login");
        Scene scene = new Scene(root);

        // Set minimum size
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("✅ Application started successfully");
    }

    public static void main(String[] args) {
        try {
            System.out.println("JavaFX Runtime: " + System.getProperty("javafx.version"));
            launch(args);
        } catch (Exception e) {
            System.err.println("Failed to start application:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}