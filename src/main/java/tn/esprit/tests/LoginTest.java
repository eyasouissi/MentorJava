package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.tools.HostServicesProvider;

public class LoginTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set the HostServices instance
        HostServicesProvider.setHostServices(getHostServices());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/auth/login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Login Test");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}