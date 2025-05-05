package tn.esprit.tests.CoursesTest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class CoursesCrudTest extends Application {

    public static void main(String[] args) {
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Chemin vers le fichier FXML principal des Courses
            URL fxmlURL = getClass().getResource("/interfaces/Courses/CoursesView.fxml");
            // 2. Vérification que le fichier existe
            if (fxmlURL == null) {
                System.err.println("❌ ERREUR : Fichier FXML introuvable !");
                System.err.println("Chemin essayé : /tn/esprit/views/CoursesView.fxml");
                System.err.println("Vérifiez que :");
                System.err.println("1. Le fichier existe dans src/main/resources/tn/esprit/views");
                System.err.println("2. Le nom du fichier est correct (attention à la casse)");
                return;
            }

            System.out.println("✅ Fichier FXML trouvé : " + fxmlURL);

            // 3. Chargement du FXML
            Parent root = FXMLLoader.load(fxmlURL);

            // 4. Configuration de la fenêtre
            primaryStage.setTitle("Test CRUD - Gestion des Cours");
            Scene scene = new Scene(root, 1000, 700); // Taille adaptée à votre interface

            // 5. Option : Ajouter un CSS si nécessaire
            /*
            URL cssURL = getClass().getResource("/tn/esprit/css/courses.css");
            if (cssURL != null) {
                scene.getStylesheets().add(cssURL.toExternalForm());
            }
            */

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors du chargement :");
            e.printStackTrace();

            // Messages d'erreur spécifiques
            if (e instanceof NullPointerException) {
                System.err.println("-> Vérifiez que le contrôleur est bien défini dans le FXML");
            }
            if (e.getMessage() != null && e.getMessage().contains("Location is required")) {
                System.err.println("-> Le chemin vers le FXML est incorrect");
            }
        }
    }
}