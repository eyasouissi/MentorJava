package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;

import tn.esprit.utils.UserSession;
import tn.esprit.tools.DatabaseMigration;
import tn.esprit.tools.MyDataBase;

/**
 * Main application class for the Mentor application
 */
public class MainApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainApplication.class.getName());
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection
            LOGGER.info("Initializing database connection...");
            MyDataBase.getInstance();
            
            // Run database migrations
            LOGGER.info("Running database migrations...");
            DatabaseMigration.runAllMigrations();
            
            // Check if user is logged in or go to login page
            FXMLLoader loader;
            if (UserSession.getInstance().isLoggedIn()) {
                loader = new FXMLLoader(getClass().getResource("/interfaces/FrontGroup.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/interfaces/auth/login.fxml"));
            }
            
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Mentor Application");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            LOGGER.info("Application started successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
} 