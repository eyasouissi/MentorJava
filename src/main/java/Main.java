import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/front/AnnonceList.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);

        scene.getStylesheets().add(getClass().getResource("/css/listeAF.css").toExternalForm());

        primaryStage.setTitle("Mentor - Gestion des Événements");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}