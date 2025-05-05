package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.tools.MyDataBase;

import java.net.URL;
import java.sql.*;

public class MainViewTest extends Application {  // 1. Doit étendre Application pour être une app JavaFX

    public static void main(String[] args) {
        launch(args);  // 2. Lance l'application JavaFX
    }

    @Override  // 3. Implémentation correcte de la méthode start
    public void start(Stage primaryStage) {
        try {
            // 1. Chemin vers le fichier FXML principal des Courses
            URL fxmlURL = getClass().getResource("/interfaces/Front/MainView.fxml");
            // 2. Vérification que le fichier existe
            if (fxmlURL == null) {
                System.err.println("❌ ERREUR : Fichier FXML introuvable !");
                System.err.println("Chemin essayé : /interfaces/Front/MainView.fxml");
                System.err.println("Vérifiez que :");
                System.err.println("1. Le fichier existe dans src/main/resources/interfaces/Front");
                System.err.println("2. Le nom du fichier est correct (attention à la casse)");
                return;
            }

            System.out.println("✅ Fichier FXML trouvé : " + fxmlURL);

            // 3. Chargement du FXML
            Parent root = FXMLLoader.load(fxmlURL);

            // 4. Configuration de la fenêtre
            primaryStage.setTitle("Test CRUD - Gestion des Cours");
            Scene scene = new Scene(root, 1000, 700); // Taille adaptée à votre interface

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