package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import tn.esprit.services.HuggingFaceService;
public class ForumFrontMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        try {



            // Load the FXML file from the correct path
            Parent root = FXMLLoader.load(getClass().getResource("/interfaces/ForumFront.fxml"));

            // Set up the main stage
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setTitle("Forum Interface");
            primaryStage.setScene(scene);

            // Set minimum window size
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML file: " + e.getMessage());
            e.printStackTrace();
        }

    }

}