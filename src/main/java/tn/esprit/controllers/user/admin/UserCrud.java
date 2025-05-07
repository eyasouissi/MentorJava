package tn.esprit.controllers.user.admin;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.controllers.auth.SignUp;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class UserCrud {
    private static final int ROWS_PER_PAGE = 4;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> rolesColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button profileButton;
    @FXML private TextField searchField;
    @FXML private Button sortButton;

    @FXML private Pagination pagination;

    private User currentUser;
    private final UserService userService = UserService.getInstance();
    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData = new FilteredList<>(masterData);
    private SortedList<User> sortedData = new SortedList<>(filteredData);
    private boolean sortByRole = false;

    public void initializeWithUser(User user) {
        this.currentUser = user;
        initialize();
    }

    @FXML
    public void initialize() {
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        rolesColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(String.join(", ", user.getRoles()));
        });

        setupActionsColumn();
        pagination.setPageFactory(this::createPage);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getName().toLowerCase().contains(lowerCaseFilter);
            });
            updatePagination();
        });

        // Remove the updateSorting() call from here
        refreshTable();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) sortedData.size() / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, sortedData.size());

        ObservableList<User> pageItems;
        if (sortedData.isEmpty() || fromIndex > sortedData.size()) {
            pageItems = FXCollections.observableArrayList();
        } else {
            pageItems = FXCollections.observableArrayList(sortedData.subList(fromIndex, toIndex));
        }

        usersTable.setItems(pageItems);
        usersTable.setFixedCellSize(30);
        double headerHeight = 30;
        double rowHeight = usersTable.getFixedCellSize();
        double tableHeight = headerHeight + (Math.min(pageItems.size(), ROWS_PER_PAGE) * rowHeight) + 2;

        usersTable.setPrefHeight(tableHeight);
        usersTable.setMinHeight(tableHeight);
        usersTable.setMaxHeight(tableHeight);

        VBox box = new VBox(usersTable);
        box.setPrefHeight(tableHeight);
        return box;
    }

    @FXML
    private void handleSortByRole() {
        sortByRole = !sortByRole; // Toggle sorting state

        if (sortByRole) {
            sortedData.setComparator((u1, u2) -> {
                boolean u1IsTutor = u1.getRoles().contains("ROLE_TUTOR");
                boolean u2IsTutor = u2.getRoles().contains("ROLE_TUTOR");

                if (u1IsTutor && !u2IsTutor) {
                    return -1;
                } else if (!u1IsTutor && u2IsTutor) {
                    return 1;
                } else {
                    return 0;
                }
            });
            sortButton.setText("Clear Sorting");
        } else {
            sortedData.setComparator(null); // Clear sorting
            sortButton.setText("Sort by Role");
        }

        // Force refresh of the current page
        int currentPage = pagination.getCurrentPageIndex();
        pagination.setPageFactory(pageIndex -> createPage(pageIndex));
        pagination.setCurrentPageIndex(currentPage);
    }

    private void updateSorting() {
        sortedData.setComparator((u1, u2) -> {
            boolean u1IsTeacher = u1.getRoles().contains("ROLE_TEACHER");
            boolean u2IsTeacher = u2.getRoles().contains("ROLE_TEACHER");

            if (u1IsTeacher && !u2IsTeacher) {
                return -1;
            } else if (!u1IsTeacher && u2IsTeacher) {
                return 1;
            } else {
                return 0;
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");

                    {
                        editBtn.setStyle("-fx-background-color: #e8b8f3; -fx-text-fill: white; -fx-pref-width: 60;");
                        deleteBtn.setStyle("-fx-background-color: #296198; -fx-text-fill: white; -fx-pref-width: 70;");
                        editBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            showEditDialog(user);
                        });
                        deleteBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            confirmAndDeleteUser(user);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(new HBox(5, editBtn, deleteBtn));
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleAddUser() {
        try {
            URL fxmlLocation = getClass().getResource("/interfaces/auth/SignUp.fxml");
            if (fxmlLocation == null) {
                throw new IOException("FXML file not found at: /interfaces/auth/SignUp.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            SignUp controller = loader.getController();
            controller.setRedirectTarget("/interfaces/user/admin/dashboard.fxml");
            controller.setFormTitle("Add New User");
            controller.setAdminMode(true);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add New User");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not open user creation form.\n" +
                    "File path: /interfaces/auth/SignUp.fxml\n" +
                    "Error details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/user/admin/adminprofile.fxml"));
            Parent root = loader.load();

            AdminProfileController controller = loader.getController();
            controller.setUserData(currentUser);

            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Profile");
        } catch (IOException e) {
            showAlert("Error", "Could not open profile page: " + e.getMessage());
        }
    }

    private void confirmAndDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete User: " + user.getName());
        alert.setContentText("Are you sure you want to delete this user?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userService.supprimer(user.getId().intValue());
                refreshTable();
                showAlert("Success", "User deleted successfully!");
            }
        });
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    private void refreshTable() {
        try {
            usersTable.getSelectionModel().clearSelection();
            List<User> allUsers = userService.getAll();

            masterData.setAll(allUsers.stream()
                    .filter(user -> !user.getRoles().contains("ROLE_ADMIN"))
                    .collect(Collectors.toList()));

            filteredData = new FilteredList<>(masterData, user -> {
                String searchText = searchField.getText();
                return searchText == null || searchText.isEmpty()
                        || user.getName().toLowerCase().contains(searchText.toLowerCase());
            });

            sortedData = new SortedList<>(filteredData);
            // Apply sorting if it was previously set
            if (sortByRole) {
                handleSortByRole();
            }

            updatePagination();

            Platform.runLater(() -> {
                emailColumn.setPrefWidth(emailColumn.getWidth());
                nameColumn.setPrefWidth(nameColumn.getWidth());
                rolesColumn.setPrefWidth(rolesColumn.getWidth());
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to refresh data: " + e.getMessage());
        }
    }
    private void showEditDialog(User user) {
        try {
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Modifier l'utilisateur");
            dialog.setHeaderText(null);

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setPrefWidth(400);
            dialogPane.setStyle("-fx-background-color: #bb94ed; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 5);");

            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setAlignment(Pos.CENTER);

            TextField emailField = new TextField(user.getEmail());
            emailField.setPromptText("Email");
            emailField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8; -fx-border-color: #cfb5ea; -fx-background-color: white;");

            TextField nameField = new TextField(user.getName());
            nameField.setPromptText("Nom");
            nameField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8; -fx-border-color: #cfb5ea; -fx-background-color: white;");

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Laisser vide pour garder l’ancien");
            passwordField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8; -fx-border-color: #cfb5ea; -fx-background-color: white;");

            ComboBox<String> rolesComboBox = new ComboBox<>();
            rolesComboBox.getItems().addAll("ROLE_STUDENT", "ROLE_TUTOR");
            String userRole = user.getRoles().stream()
                    .filter(role -> !role.equals("ROLE_ADMIN"))
                    .findFirst()
                    .orElse("ROLE_STUDENT");
            rolesComboBox.getSelectionModel().select(userRole);
            rolesComboBox.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 5; -fx-border-color: #cfb5ea; -fx-background-color: white;");

            grid.add(new Label("Email :"), 0, 0);
            grid.add(emailField, 1, 0);
            grid.add(new Label("Nom :"), 0, 1);
            grid.add(nameField, 1, 1);
            grid.add(new Label("Mot de passe :"), 0, 2);
            grid.add(passwordField, 1, 2);
            grid.add(new Label("Rôle :"), 0, 3);
            grid.add(rolesComboBox, 1, 3);

            dialogPane.setContent(grid);

            // Styling buttons with rounded corners and colors
            Platform.runLater(() -> {
                Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
                saveButton.setStyle(
                        "-fx-background-color: #9a5daa; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 10; " +
                                "-fx-pref-width: 100; " +
                                "-fx-cursor: hand;");

                Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
                cancelButton.setStyle(
                        "-fx-background-color: #5e8cb8; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 10; " +
                                "-fx-pref-width: 100; " +
                                "-fx-cursor: hand;");
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    user.setEmail(emailField.getText());
                    user.setName(nameField.getText());
                    if (!passwordField.getText().isEmpty()) {
                        user.setPassword(passwordField.getText());
                    }
                    user.getRoles().clear();
                    user.addRole(rolesComboBox.getValue());
                    return user;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(updatedUser -> {
                if (updatedUser.getId() != null) {
                    try {
                        userService.modifier(updatedUser);
                        refreshTable();
                        showAlert("Succès", "Utilisateur modifié avec succès !");
                    } catch (Exception e) {
                        showAlert("Erreur", "Échec de la modification : " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l’édition : " + e.getMessage());
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}