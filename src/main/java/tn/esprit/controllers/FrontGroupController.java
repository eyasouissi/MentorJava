package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.services.group.GroupService;
import tn.esprit.controllers.auth.UserSession;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FrontGroupController {
    @FXML private Button createGroupBtn;
    @FXML private Button emptyStateCreateBtn;
    @FXML private FlowPane groupsContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox emptyState;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> subjectFilter;
    @FXML private ComboBox<String> sortBy;
    
    private GroupService groupService;
    private User currentUser;
    private List<GroupStudent> allGroups;
    private StackPane contentArea;

    @FXML
    public void initialize() {
        groupService = new GroupService();
        currentUser = UserSession.getInstance().getCurrentUser();

        // Always show buttons (remove role check)
        boolean isTutor = currentUser != null &&
                currentUser.getRoles() != null &&
                currentUser.getRoles().contains("ROLE_TUTOR");

        createGroupBtn.setVisible(isTutor);
        emptyStateCreateBtn.setVisible(isTutor);

        setupFilterAndSort();
        loadGroups();
    }
    private void setupFilterAndSort() {
        // Initialize subject filter
        subjectFilter.getItems().addAll(
            "All",
            "Mathematics",
            "Computer Science",
            "Physics",
            "Chemistry",
            "Biology",
            "Engineering",
            "Languages",
            "History",
            "Other"
        );
        subjectFilter.setValue("All");
        
        // Initialize sort options
        sortBy.getItems().addAll(
            "Newest First",
            "Oldest First",
            "Name A-Z",
            "Name Z-A",
            "Most Members",
            "Least Members"
        );
        sortBy.setValue("Newest First");
    }
    
    private void loadGroups() {
        loadingIndicator.setVisible(true);
        emptyState.setVisible(false);
        groupsContainer.getChildren().clear();
        
        try {
            allGroups = groupService.getAllGroups();
            applyFiltersAndSort();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load groups: " + e.getMessage());
            e.printStackTrace();
        } finally {
            loadingIndicator.setVisible(false);
        }
    }
    
    private void applyFiltersAndSort() {
        if (allGroups == null || allGroups.isEmpty()) {
            emptyState.setVisible(true);
            return;
        }
        
        // Apply search filter
        String searchTerm = searchField.getText().trim().toLowerCase();
        String filterSubject = subjectFilter.getValue();
        
        List<GroupStudent> filteredGroups = allGroups.stream()
            .filter(group -> {
                // Check if name exists and matches search
                boolean matchesSearch = searchTerm.isEmpty() || 
                                      (group.getName() != null && group.getName().toLowerCase().contains(searchTerm)) ||
                                      (group.getDescription() != null && group.getDescription().toLowerCase().contains(searchTerm));
                
                // Check if description exists and matches subject filter
                boolean matchesSubject = "All".equals(filterSubject) || 
                                       (group.getDescription() != null && 
                                        group.getDescription().contains(filterSubject));
                
                return matchesSearch && matchesSubject;
            })
            .collect(Collectors.toList());
        
        // Apply sorting
        sortFilteredGroups(filteredGroups);
        
        // Display filtered groups
        if (filteredGroups.isEmpty()) {
            emptyState.setVisible(true);
        } else {
            displayGroups(filteredGroups);
        }
    }
    
    private void sortFilteredGroups(List<GroupStudent> groups) {
        String sortOption = sortBy.getValue();
        
        switch (sortOption) {
            case "Newest First":
                groups.sort(Comparator.comparing((GroupStudent g) -> g.getCreationDate(), Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Oldest First":
                groups.sort(Comparator.comparing((GroupStudent g) -> g.getCreationDate(), Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "Name A-Z":
                groups.sort(Comparator.comparing((GroupStudent g) -> g.getName() != null ? g.getName() : "", String.CASE_INSENSITIVE_ORDER));
                break;
            case "Name Z-A":
                groups.sort(Comparator.comparing((GroupStudent g) -> g.getName() != null ? g.getName() : "", String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Most Members":
                groups.sort(Comparator.comparing((GroupStudent g) -> g.getMemberCount() != null ? g.getMemberCount() : 0, Comparator.reverseOrder()));
                break;
            case "Least Members":
                groups.sort(Comparator.comparing((GroupStudent g) -> g.getMemberCount() != null ? g.getMemberCount() : 0));
                break;
        }
    }
    
    private void displayGroups(List<GroupStudent> groups) {
        groupsContainer.getChildren().clear();
        
        for (GroupStudent group : groups) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/GroupCard.fxml"));
                Parent groupCard = loader.load();
                GroupCardController controller = loader.getController();
                controller.setGroup(group);
                controller.setCurrentUser(currentUser);
                controller.setParentController(this);
                
                groupsContainer.getChildren().add(groupCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onCreateGroupClick() {
        try {
            // 1. Load the CreateGroup content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/CreateGroup.fxml"));
            Parent createGroupContent = loader.load();
            CreateGroupController controller = loader.getController();
            controller.setParentController(this);

            // 2. Get reference to the root container
            BorderPane rootPane = (BorderPane) createGroupBtn.getScene().getRoot();

            // 3. Preserve existing sidebar
            Node sidebar = rootPane.getLeft();

            // 4. Create new container with sidebar and create group content
            BorderPane newRoot = new BorderPane();
            newRoot.setLeft(sidebar);
            newRoot.setCenter(createGroupContent);

            // 5. Update the scene
            Scene currentScene = createGroupBtn.getScene();
            currentScene.setRoot(newRoot);

            // 6. Maintain sidebar controller state


        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not open create group page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onSearchClick() {
        applyFiltersAndSort();
    }
    
    @FXML
    private void onSearchKeyReleased() {
        applyFiltersAndSort();
    }
    
    @FXML
    private void onFilterChange() {
        applyFiltersAndSort();
    }
    
    @FXML
    private void onSortChange() {
        applyFiltersAndSort();
    }
    
    public void refreshGroups() {
        loadGroups();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }
} 