package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.utils.StripeConfig;

public class mainFx extends Application {

    @Override

    public void start(Stage primaryStage) throws Exception {
        StripeConfig.initializeStripe();
        try {
         //  FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/frontOffre.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/afficherOffre.fxml"));

            Parent root = loader.load();

            Scene scene = new Scene(root);

            String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
            System.out.println("Fichier CSS chargé depuis : " + cssPath);
            scene.getStylesheets().add(cssPath);

            primaryStage.setTitle("Liste des Offres 💼");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace(); // Ceci te montrera l'erreur réelle (FXML introuvable, erreur de contrôleur, etc.)
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}