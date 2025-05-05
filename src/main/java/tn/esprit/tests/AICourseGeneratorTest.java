package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class AICourseGeneratorTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Correct path to your FXML file
            URL fxmlURL = getClass().getResource("/interfaces/Courses/AIView.fxml");

            if (fxmlURL == null) {
                System.err.println("❌ ERROR: FXML file not found!");
                System.err.println("Tried path: /interfaces/AIView.fxml");
                System.err.println("Found in: " + System.getProperty("user.dir"));
                System.err.println("Please verify:");
                System.err.println("1. The file exists at: src/main/resources/interfaces/AIView.fxml");
                System.err.println("2. The resources folder is marked as 'Resources Root' in IDEA");
                return;
            }

            System.out.println("✅ FXML file found: " + fxmlURL);

            // Load FXML and set the scene
            Parent root = FXMLLoader.load(fxmlURL);
            primaryStage.setTitle("AI Course Generator Test");
            Scene scene = new Scene(root, 900, 650);
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("\n=== TEST READY ===");
            System.out.println("1. Enter course topic in text area");
            System.out.println("2. Select AI model from dropdown");
            System.out.println("3. Click 'Generate' button");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading FXML:");
            e.printStackTrace();

            if (e.getMessage().contains("Property \"value\" does not exist")) {
                System.err.println("\nSOLUTION: Check your ComboBox or ListView properties in FXML");
                System.err.println("Common fixes:");
                System.err.println("- Replace 'value' with 'items' for ComboBox");
                System.err.println("- Ensure cellFactory format is correct");
            }
        }
    }
}
