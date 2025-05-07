package tn.esprit.controllers.group;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import tn.esprit.controllers.project.StatsController;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.services.group.GroupService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GroupsController implements Initializable {

    @FXML private TableView<GroupStudent> groupsTable;
    @FXML private TableColumn<GroupStudent, String> nameColumn;
    @FXML private TableColumn<GroupStudent, String> descriptionColumn;
    @FXML private TableColumn<GroupStudent, LocalDate> creationDateColumn;
    @FXML private TableColumn<GroupStudent, LocalDate> meetingDateColumn;
    @FXML private HBox actionButtonsBox;
    @FXML private Button toggleThemeButton;
    @FXML private TextField searchField;
    @FXML private HBox header;
    @FXML private HBox navbar;
    @FXML private HBox toolbar;
    @FXML private DatePicker meetingDateFilter;


    private static final int ROWS_PER_PAGE = 4;
    private ObservableList<GroupStudent> allGroups = FXCollections.observableArrayList();
    private final GroupService groupService = GroupService.getInstance();
    private final ObservableList<GroupStudent> groupsList = FXCollections.observableArrayList();
    private boolean darkMode = false;

@FXML private Pagination pagination;

// Dans initialize() ou une méthode d'initialisation
private void initializePagination() {
    int pageCount = (int) Math.ceil((double) allGroups.size() / ROWS_PER_PAGE);
    pagination.setPageCount(pageCount);
    pagination.setCurrentPageIndex(0);
    pagination.setPageFactory(this::createPage);
    
    // Chargez les données initiales
    loadGroupsData();
}

private Node createPage(int pageIndex) {
    int fromIndex = pageIndex * ROWS_PER_PAGE;
    int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allGroups.size());
    
    groupsTable.setItems(FXCollections.observableArrayList(allGroups.subList(fromIndex, toIndex)));
    return new StackPane(groupsTable);
}

private void loadGroupsData() {
    allGroups.setAll(GroupService.getInstance().getAll());
    int pageCount = (int) Math.ceil((double) allGroups.size() / ROWS_PER_PAGE);
    pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
    pagination.setCurrentPageIndex(0);
}



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadGroups();
        setupTableSelectionListener();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        meetingDateColumn.setCellValueFactory(new PropertyValueFactory<>("meetingDate"));
    }

    private void loadGroups() {
        List<GroupStudent> groups = groupService.getAll();
        groupsList.setAll(groups);
        groupsTable.setItems(groupsList);
    }

    private void setupTableSelectionListener() {
        groupsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> actionButtonsBox.setVisible(newSelection != null)
        );
    }

    @FXML
    private void refreshGroupsList() {
        loadGroups();
        actionButtonsBox.setVisible(false);
    }

    @FXML
    private void showAddGroupView() {
        openModal("/group/AddGroupView.fxml", "Add New Group");
    }

    @FXML
    private void showEditGroupView() {
        GroupStudent selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/group/EditGroupView.fxml"));
                Parent root = loader.load();
                EditGroupController controller = loader.getController();
                controller.setGroupData(selected);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Edit Group");
                stage.setScene(new Scene(root));
                stage.setOnHidden(e -> refreshGroupsList());
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Could not load the edit group form", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Warning", "Please select a group to edit", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void deleteGroup() {
        GroupStudent selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Group");
            confirmation.setContentText("Are you sure you want to delete '" + selected.getName() + "'?");
    
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    groupService.supprimer(selected.getId());  // No need to convert here
                    refreshGroupsList();
                    showAlert("Success", "Group deleted successfully", Alert.AlertType.INFORMATION);
                } catch (RuntimeException e) {
                    showAlert("Error", "Failed to delete group: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Warning", "Please select a group to delete", Alert.AlertType.WARNING);
        }
    }
    


    @FXML
    private void showGroupDetails() {
        GroupStudent selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Group Details");
            alert.setHeaderText(selected.getName());

            String content = String.format(
                    "Description: %s\nCreation Date: %s\nNext Meeting: %s\nImage: %s",
                    selected.getDescription(),
                    selected.getCreationDate(),
                    selected.getMeetingDate(),
                    selected.getImage() != null ? selected.getImage() : "Not available"
            );

            alert.setContentText(content);
            alert.setResizable(true);
            alert.showAndWait();
        } else {
            showAlert("Warning", "Please select a group to view details", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void navigateToProjectsView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/project/ProjectsView.fxml"));
            Stage stage = (Stage) groupsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();  // Pour imprimer l'erreur dans la console
            showAlert("Error", "Could not navigate to projects view", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToGroupsView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/group/GroupsView.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not navigate to groups view", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void searchGroups() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (!keyword.isEmpty()) {
            List<GroupStudent> filteredGroups = groupService.getAll().stream()
                    .filter(g -> g.getName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            groupsList.setAll(filteredGroups);
        } else {
            refreshGroupsList();
        }
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        Scene scene = toggleThemeButton.getScene();
        if (!darkMode) {
            // Dark Mode Styles
            scene.getRoot().setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
            header.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
            navbar.setStyle("-fx-background-color: #2c3e50;");
            toolbar.setStyle("-fx-background-color: #34495e;");
            groupsTable.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-table-cell-border-color: #555;");
            toggleThemeButton.setText("☀️");
        } else {
            // Light Mode Styles
            scene.getRoot().setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: black;");
            header.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: black;");
            navbar.setStyle("-fx-background-color: #336699;");
            toolbar.setStyle("-fx-background-color: #f5f7fa;");
            groupsTable.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-table-cell-border-color: #d1d1d1;");
            toggleThemeButton.setText("🌙");
        }
        darkMode = !darkMode;
    }

@FXML
private void showStatsView(ActionEvent event) {
    try {
        // Charge le FXML des statistiques
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/Stats.fxml"));
        Parent root = loader.load();
        
        // Configure la fenêtre des statistiques
        Stage stage = new Stage();
        stage.setTitle("Statistiques des Projets par Groupe");
        stage.setScene(new Scene(root, 800, 600));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
        
        // Personnalisation supplémentaire si nécessaire
        StatsController statsController = loader.getController();
        // Vous pouvez passer des données spécifiques aux groupes ici si besoin
        // statsController.setGroupData(...);
        
        stage.show();
    } catch (IOException e) {
        System.err.println("Erreur de chargement des statistiques: " + e.getMessage());
        e.printStackTrace();
        showAlert("Erreur", "Impossible d'ouvrir les statistiques", Alert.AlertType.ERROR);
    }
}

    @FXML
    private void goToProjectsView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/project/ProjectsView.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not navigate to projects view", Alert.AlertType.ERROR);
        }
    }
    
    
    

    private void openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshGroupsList());
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
private void goToHomeFront(ActionEvent event) {
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/project/Front.fxml")); // adapte le chemin
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        System.out.println(getClass().getResource("/project/Front.fxml"));

        showAlert("Error", "Could not load home front view: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}

@FXML
private void filterByMeetingDate() {
    LocalDate selectedDate = meetingDateFilter.getValue();

    if (selectedDate != null) {
        List<GroupStudent> filteredGroups = groupService.getAll().stream()
                .filter(g -> selectedDate.equals(g.getMeetingDate()))
                .collect(Collectors.toList());
        groupsList.setAll(filteredGroups);
    }
}
@FXML
private void clearDateFilter() {
    meetingDateFilter.setValue(null);
    refreshGroupsList();  // recharge la liste complète
}

@FXML
private void showCalendar(ActionEvent event) {
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/project/Front.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Calendrier des Échéances et Réunions");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    } catch (IOException e) {
        showAlert("Erreur", "Impossible d'ouvrir le calendrier", Alert.AlertType.ERROR);
    }
}
}
