package tn.esprit.tests.CategoryTest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class CategoryCrudTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chemin vers le fichier FXML principal
            URL fxmlURL = getClass().getResource("/interfaces/Category/CategoryView.fxml");

            // Vérifier que le fichier FXML existe
            if (fxmlURL == null) {
                System.out.println("❌ FXML file not found! Vérifie le chemin : /interfaces/Category/CategoryView.fxml");
                return;
            } else {
                System.out.println("✅ FXML file trouvé à : " + fxmlURL);
            }

            // Charger le fichier FXML
            Parent root = FXMLLoader.load(fxmlURL);
            primaryStage.setTitle("Test CRUD Catégories");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();

        } catch (Exception e) {
            System.out.println("❌ Une erreur est survenue lors du chargement du FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
