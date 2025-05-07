package tn.esprit.controllers.project;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import tn.esprit.entities.project.Project;
import tn.esprit.services.project.ProjectService;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProjectsController implements Initializable {

    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> titleColumn;
    @FXML private TableColumn<Project, String> descriptionColumn;
    @FXML private TableColumn<Project, String> difficulteColumn;
    @FXML private TableColumn<Project, LocalDate> deadlineColumn;
    @FXML private HBox actionButtonsBox;
    @FXML private HBox toolbar;
    @FXML private HBox navbar;
    @FXML private HBox header; 
    @FXML private TextField searchField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> difficulteComboBox;
    @FXML private AnchorPane rootPane; 
    @FXML private Button toggleThemeButton;
    @FXML private ListView<String> projectsListView;
    @FXML private AnchorPane root;
    @FXML private Pagination pagination;

private List<Project> allProjects = new ArrayList<>();
private boolean isDarkMode = false;
private static final int ROWS_PER_PAGE = 4;
private final ProjectService projectService = ProjectService.getInstance();
private final ObservableList<Project> projectsList = FXCollections.observableArrayList();

@FXML
private void applyFilters() {
    String titleOrDesc = searchField.getText();
    LocalDate startDate = startDatePicker.getValue();
    LocalDate endDate = endDatePicker.getValue();
    String difficulteStr = difficulteComboBox.getValue();

    Integer difficulte = null;
    if (difficulteStr != null && !difficulteStr.isEmpty()) {
        difficulte = difficulteStr.trim().split("★").length;
    }

    // Appel au service
    List<Project> filteredProjects = projectService.getProjectsFiltered(
        titleOrDesc,
        startDate,
        endDate,
        difficulte
    );

    // ✅ Mise à jour des données internes et pagination
    allProjects.clear();
    allProjects.addAll(filteredProjects);
    updatePagination();
}

private void initializePagination() {
    pagination.setPageFactory(this::createPage);
    loadProjectsData();  // Changé de loadData() à loadProjectsData()
}
private Node createPage(int pageIndex) {
    int fromIndex = pageIndex * ROWS_PER_PAGE;
    int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allProjects.size());  // allItems → allProjects
    projectsTable.setItems(FXCollections.observableArrayList(allProjects.subList(fromIndex, toIndex)));  // tableView → projectsTable
    return new StackPane(projectsTable);  // tableView → projectsTable
}
private void loadProjectsData() {  // Renommé de loadData() à loadProjectsData()
    List<Project> fetchedProjects = projectService.getAll();
    allProjects.clear();
    allProjects.addAll(fetchedProjects); // <- Important !
    projectsList.setAll(fetchedProjects);
    projectsTable.setItems(projectsList);
}

// Méthode pour mettre à jour la pagination
private void updatePagination() {
    int totalItems = allProjects.size();
    int pageCount = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
    pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
    pagination.setCurrentPageIndex(0);
    pagination.setPageFactory(this::createPage);
}

@FXML
private void showStatsView(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/Stats.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("Statistiques des Projets");
        stage.setScene(new Scene(root, 800, 600));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
        stage.show();
        
    } catch (IOException e) {
        System.err.println("Erreur de chargement: " + e.getMessage());
        e.printStackTrace();
        showAlert("Erreur", "Impossible d'ouvrir les statistiques", Alert.AlertType.ERROR);
    }
}

@FXML
private void clearFilters() {
    searchField.clear();
    startDatePicker.setValue(null);
    endDatePicker.setValue(null);
    difficulteComboBox.setValue(null);

    List<Project> all = projectService.getAll();
    allProjects.clear();
    allProjects.addAll(all);
    updatePagination();
}


public List<Project> getAllProjects() {
    return this.projectsList; // ou la liste que tu utilises pour stocker les projets
}



@FXML
private void toggleTheme(ActionEvent event) {
    Scene scene = toggleThemeButton.getScene();

    if (!isDarkMode) {
        // Mode Sombre
        scene.getRoot().setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        header.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        navbar.setStyle("-fx-background-color: #2c3e50;");
        toolbar.setStyle("-fx-background-color: #34495e;");
        projectsTable.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-table-cell-border-color: #555;");
        toggleThemeButton.setText("☀️"); // Soleil pour repasser en mode clair
    } else {
        // Mode Clair
        scene.getRoot().setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: black;");
        header.setStyle("-fx-background-color: #f5f7fa; -fx-text-fill: black;");
        navbar.setStyle("-fx-background-color: #336699;");
        toolbar.setStyle("-fx-background-color: #f5f7fa;");
        projectsTable.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-table-cell-border-color: #d1d1d1;");
        toggleThemeButton.setText("🌙"); // Lune pour repasser en mode sombre
    }

    isDarkMode = !isDarkMode;
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

@FXML
private void goToHomeFront(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/Front.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void updateTableView(List<Project> filteredProjects) {
    projectsTable.getItems().clear();
    projectsTable.getItems().addAll(filteredProjects);
}


   
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadProjects();
        initializePagination(); // Au lieu de load...()
        setupTableSelectionListener();
difficulteComboBox.setItems(FXCollections.observableArrayList(
    "★", "★ ★", "★ ★ ★", "★ ★ ★ ★"
));

    }


    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descriptionProject"));
        difficulteColumn.setCellValueFactory(new PropertyValueFactory<>("difficulte"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));

        deadlineColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
    }

    private void loadProjects() {
        projectsList.setAll(projectService.getAll());
        projectsTable.setItems(projectsList);
    }

    private void setupTableSelectionListener() {
        projectsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> actionButtonsBox.setVisible(newSelection != null)
        );
    }

    @FXML
    private void refreshProjectsList() {
        projectsListView.getItems().clear();
        for (Project project : allProjects) {
            projectsListView.getItems().add(project.getTitre() + ": " + project.getDescriptionProject());
        }
    }
    

    @FXML
    private void showAddProjectView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/AddProjectView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Project");
            stage.setScene(new Scene(root));

            stage.setOnHidden(e -> refreshProjectsList());
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load the add project form", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void showEditProjectView() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/EditProjectView.fxml"));
                Parent root = loader.load();

                EditProjectController controller = loader.getController();
                controller.setProject(selected);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Edit Project");
                stage.setScene(new Scene(root));

                stage.setOnHidden(e -> refreshProjectsList());
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Could not load the edit project form", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            showAlert("Warning", "Please select a project to edit", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void deleteProject() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Project");
            confirmation.setContentText("Are you sure you want to delete '" + selected.getTitre() + "'?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                projectService.supprimer(selected.getId());
                refreshProjectsList();
                showAlert("Success", "Project deleted successfully", Alert.AlertType.INFORMATION);
            }
        } else {
            showAlert("Warning", "Please select a project to delete", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void showProjectDetails() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Project Details");
            alert.setHeaderText(selected.getTitre());

            String content = String.format(
                    "Description: %s\nDifficulte: %s\nDeadline: %s\nPDF File: %s\nImage: %s",
                    selected.getDescriptionProject(),
                    selected.getDifficulte(),
                    selected.getDeadline() != null ? selected.getDeadline().toString() : "Not set",
                    selected.getPdfFile() != null ? selected.getPdfFile() : "Not available",
                    selected.getImage() != null ? selected.getImage() : "Not available"
            );

            alert.setContentText(content);
            alert.setResizable(true);
            alert.showAndWait();
        } else {
            showAlert("Warning", "Please select a project to view details", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void navigateToGroupsView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/group/GroupsView.fxml"));
            Stage stage = (Stage) projectsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert("Error", "Could not load the groups view", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
private void showCalendar(ActionEvent event) {
    try {
        // Chemin relatif depuis la racine des resources
        URL fxmlUrl = getClass().getResource("/project/Front.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Fichier FXML non trouvé: /project/Front.fxml");
        }
        
        Parent root = FXMLLoader.load(fxmlUrl);
        Stage stage = new Stage();
        stage.setTitle("Calendrier");
        stage.setScene(new Scene(root));
        stage.show();
        
    } catch (IOException e) {
        System.err.println("Erreur de chargement: " + e.getMessage());
        e.printStackTrace();
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Impossible d'ouvrir le calendrier");
        alert.setContentText("Le fichier de calendrier est introuvable ou inaccessible.");
        alert.showAndWait();
        
    }
}
}
