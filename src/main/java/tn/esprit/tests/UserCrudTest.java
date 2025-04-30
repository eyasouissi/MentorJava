package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UserCrudTest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/interfaces/user/admin/user_crud.fxml"));
        primaryStage.setTitle("User CRUD Management");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}