package tn.esprit.tests;
import javafx.scene.text.Font;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try{
        Parent root = FXMLLoader.load(getClass().getResource("/interfaces/Forum.fxml"));
        primaryStage.setTitle("User CRUD Management");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }catch (Exception e) {
        e.printStackTrace();
    }
}}