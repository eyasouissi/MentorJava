package tn.esprit; 

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("=== Étape 1 : Chargement du FXML ===");
            Parent root = loadFXML("AddProjectView");
            System.out.println("=== Étape 2 : Création de la scène ===");
            scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            stage.setTitle("JavaFX Application");
            stage.show();
            System.out.println("=== Étape 3 : Affichage terminé ===");
        } catch (Exception e) {
            System.err.println(">>> ERREUR au démarrage JavaFX !");
            e.printStackTrace(); 
        }
    }
    

    public static void setRoot(String fxml) throws IOException {
        // Charge une nouvelle scène
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String resourcePath = "/project/"+fxml + ".fxml";
        System.out.println(">>> Chargement depuis : " + resourcePath);
        var resource = App.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Fichier FXML non trouvé : " + resourcePath);
        }
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        return fxmlLoader.load();
    }
    

    public static void main(String[] args) {
        launch();
    }
}
