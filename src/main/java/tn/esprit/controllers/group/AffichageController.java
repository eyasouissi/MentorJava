package tn.esprit.controllers.group;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.User;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.services.group.GroupService;
import tn.esprit.controllers.auth.UserSession;

public class AffichageController implements Initializable {

    @FXML private ListView<HBox> groupListView;
    @FXML private FlowPane kanbanPane;
    @FXML private StackPane viewContainer;
    @FXML private Button toggleViewButton;

    private boolean isListViewVisible = true;
    private final GroupService groupService = GroupService.getInstance();
    private User currentUser;

    // Optionnel : base locale des images (à adapter selon ton stockage)
    private static final String IMAGE_BASE_PATH = "C:/images/"; // exemple

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Try to get user from session if not set explicitly
        if (currentUser == null) {
            currentUser = UserSession.getInstance().getCurrentUser();
        }
        loadGroups();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void toggleView() {
        isListViewVisible = !isListViewVisible;
        groupListView.setVisible(isListViewVisible);
        kanbanPane.setVisible(!isListViewVisible);
    }

    private void loadGroups() {
        List<GroupStudent> groups = groupService.getAll();
        groupListView.getItems().clear();
        kanbanPane.getChildren().clear();

        for (GroupStudent group : groups) {
            groupListView.getItems().add(createGroupListItem(group));
            kanbanPane.getChildren().add(createKanbanCard(group));
        }
    }

    private HBox createGroupListItem(GroupStudent group) {
        ImageView imageView = createImageView(group.getImage(), 60, 60);

        VBox textContainer = new VBox(
            new Label("🧑 " + group.getName()),
            new Label("📝 " + group.getDescription())
        );
        textContainer.setSpacing(5);
        textContainer.setPadding(new Insets(5));

        Button joinButton = new Button("Rejoindre");
        joinButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        joinButton.setOnAction(e -> joinGroup(group));

        HBox box = new HBox(15, imageView, textContainer, joinButton);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");
        return box;
    }

    private VBox createKanbanCard(GroupStudent group) {
        ImageView imageView = createImageView(group.getImage(), 200, 130);

        Label nameLabel = new Label(group.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label(group.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        Button joinButton = new Button("Rejoindre");
        joinButton.setStyle("-fx-background-color: #0097a7; -fx-text-fill: white; -fx-font-weight: bold;");
        joinButton.setOnAction(e -> joinGroup(group));

        VBox card = new VBox(10, imageView, nameLabel, descLabel, joinButton);
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #0097a7;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        return card;
    }

    private ImageView createImageView(String path, double width, double height) {
        Image image;
        try {
            if (path != null && !path.isBlank()) {
                // Si le chemin est absolu, pas besoin de concaténer
                String finalPath = path.startsWith("/") || path.contains(":") ? path : IMAGE_BASE_PATH + path;
                image = new Image("file:" + finalPath);
            } else {
                image = new Image(getClass().getResource("/images/default-group.png").toExternalForm());
            }
        } catch (Exception e) {
            image = new Image(getClass().getResource("/images/default-group.png").toExternalForm());
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void joinGroup(GroupStudent group) {
        if (currentUser == null) {
            currentUser = UserSession.getInstance().getCurrentUser();
        }
        
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté pour rejoindre un groupe", AlertType.ERROR);
            return;
        }
        
        boolean success = groupService.joinGroup(currentUser.getId(), group.getId());
        if (success) {
            showAlert("Succès", "Vous avez rejoint le groupe " + group.getName(), AlertType.INFORMATION);
        } else {
            showAlert("Erreur", "Impossible de rejoindre le groupe", AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
