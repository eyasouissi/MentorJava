package tn.esprit.tests.CategoryTest;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class testFront extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chemin vers le fichier FXML de l'interface carrousel
            URL fxmlURL = getClass().getResource("/interfaces/Courses/MainViewFront.fxml");

            // Debug: Affiche le chemin absolu
            System.out.println("Chemin du fichier FXML: " +
                    (fxmlURL != null ? fxmlURL.toExternalForm() : "null"));

            if (fxmlURL == null) {
                System.err.println("❌ Erreur: Fichier FXML introuvable");
                System.err.println("Vérifiez que le fichier existe dans:");
                System.err.println("src/main/resources/interfaces/Category/CategoriesView.fxml");
                return;
            }

            // Chargement de l'interface
            Parent root = FXMLLoader.load(fxmlURL);

            // Configuration de la fenêtre de test
            primaryStage.setTitle("Test Interface Catégories - Carrousel");
            Scene scene = new Scene(root, 1000, 700);



            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Interface chargée avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur critique lors du chargement:");
            e.printStackTrace();

            // Message plus clair pour les erreurs courantes
            if (e.getMessage().contains("Location is not set")) {
                System.err.println("\n➡ Solution possible: Vérifiez que le fichier FXML est dans le bon dossier");
                System.err.println("Le chemin doit être: src/main/resources/interfaces/Category/CategoriesView.fxml");
            } else if (e.getMessage().contains("Caused by: javafx.fxml.LoadException")) {
                System.err.println("\n➡ Erreur FXML: Vérifiez la syntaxe de votre fichier FXML");
            }
        }
    }
}