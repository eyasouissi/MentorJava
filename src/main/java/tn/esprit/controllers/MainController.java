package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MainController {
    @FXML private BorderPane mainContainer;

    @FXML
    public void initialize() {
        loadSidebar();
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Sidebar.fxml"));
            Parent sidebar = loader.load();

            SidebarController sidebarController = loader.getController();
            sidebarController.setMainContainer(mainContainer);

            mainContainer.setLeft(sidebar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}