package tn.esprit.controllers.Category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.esprit.entities.Category;
import tn.esprit.services.CategoryService;

import java.io.IOException;
import java.util.List;

public class CategoryController {

    // UI Components
    @FXML private ListView<Category> categoryListView;
    @FXML private TextField searchField;
    @FXML private Button addButton;

    // Services
    private final CategoryService categoryService = CategoryService.getInstance();
    private ObservableList<Category> originalList;

    @FXML
    public void initialize() {
        setupUI();
        setupListView();
        setupSearch();
        loadCategories();
    }

    private void setupUI() {
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addButton.setGraphic(new FontIcon("fas-plus"));
    }

    private void setupListView() {
        categoryListView.setCellFactory(param -> new ListCell<Category>() {
            private final HBox container = new HBox(10);
            private final ImageView iconView = new ImageView();
            private final VBox textContainer = new VBox(3);
            private final Label nameLabel = new Label();
            private final Label descLabel = new Label();
            private final HBox buttonBox = new HBox(5);

            {
                configureCellLayout();
                setupActionButtons();
            }

            private void configureCellLayout() {
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(10));
                container.getStyleClass().add("category-cell");

                iconView.setFitWidth(32);
                iconView.setFitHeight(32);
                iconView.setPreserveRatio(true);
                iconView.getStyleClass().add("clickable-icon");

                nameLabel.getStyleClass().add("category-name");
                descLabel.getStyleClass().add("category-description");
                descLabel.setMaxWidth(300);
                descLabel.setWrapText(true);

                textContainer.getChildren().addAll(nameLabel, descLabel);
                container.getChildren().addAll(iconView, textContainer, new Region(), buttonBox);
                HBox.setHgrow(textContainer, Priority.ALWAYS);
            }

            private void setupActionButtons() {
                Button detailsBtn = createActionButton("Details", "fas-info", "#2196F3");
                Button editBtn = createActionButton("Edit", "fas-edit", "#FFC107");
                Button deleteBtn = createActionButton("Delete", "fas-trash", "#F44336");

                detailsBtn.setOnAction(event -> showCategoryDetails(getItem()));
                editBtn.setOnAction(event -> handleEditCategory(getItem()));
                deleteBtn.setOnAction(event -> handleDeleteCategory(getItem()));

                buttonBox.getChildren().addAll(detailsBtn, editBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(item.getName());
                    descLabel.setText(item.getDescription());
                    loadIcon(item.getIcon());
                    setGraphic(container);
                }
            }

            private void loadIcon(String iconUrl) {
                try {
                    if (iconUrl != null && !iconUrl.isEmpty()) {
                        Image image = new Image(iconUrl, true);
                        image.errorProperty().addListener((obs, wasError, isNowError) -> {
                            if (isNowError) {
                                setDefaultIcon();
                            }
                        });
                        iconView.setImage(image);
                        iconView.setOnMouseClicked(e -> showFullScreenImage(iconUrl));
                    } else {
                        setDefaultIcon();
                    }
                } catch (Exception ex) {
                    setDefaultIcon();
                }
            }

            private void setDefaultIcon() {
                iconView.setImage(new Image(getClass().getResourceAsStream("/interfaces/Category/images/default-icon.png")));
                iconView.setOnMouseClicked(e -> handleDefaultIconClick(e));
            }
        });
    }

    @FXML
    private void handleDefaultIconClick(MouseEvent event) {
        showFullScreenImage(getClass().getResource("/interfaces/Category/images/default-icon.png").toString());
    }

    private Button createActionButton(String text, String iconCode, String color) {
        Button button = new Button(text);
        button.setGraphic(new FontIcon(iconCode));
        button.getStyleClass().add("action-button");
        button.setStyle(String.format("-fx-background-color: %s;", color));
        return button;
    }

    private void setupSearch() {
        searchField.setPromptText("Search...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                categoryListView.setItems(originalList);
            } else {
                FilteredList<Category> filteredList = new FilteredList<>(originalList);
                filteredList.setPredicate(category ->
                        category.getName().toLowerCase().contains(newVal.toLowerCase()) ||
                                (category.getDescription() != null &&
                                        category.getDescription().toLowerCase().contains(newVal.toLowerCase()))
                );
                categoryListView.setItems(filteredList);
            }
        });
    }

    public void loadCategories() {
        try {
            List<Category> categories = categoryService.getAll();
            originalList = FXCollections.observableArrayList(categories);
            categoryListView.setItems(originalList);
        } catch (Exception e) {
            showAlert("Error", "Loading Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showCategoryDetails(Category category) {
        Stage detailsStage = createDetailsStage(category);
        detailsStage.showAndWait();
    }

    private Stage createDetailsStage(Category category) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Category Details");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("details-container");

        Label titleLabel = new Label("Category Details");
        titleLabel.getStyleClass().add("details-title");

        GridPane detailsGrid = createDetailsGrid(category);
        Button closeButton = createCloseButton(stage);

        root.getChildren().addAll(titleLabel, detailsGrid, closeButton);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 500, 400);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        stage.setScene(scene);
        return stage;
    }

    private GridPane createDetailsGrid(Category category) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.getStyleClass().add("details-grid");

        addDetailRow(grid, 0, "Name:", category.getName());
        addDetailRow(grid, 1, "Description:",
                category.getDescription() != null ? category.getDescription() : "Not specified");
        addDetailRow(grid, 2, "Status:", category.getIsActive() ? "Active" : "Inactive");
        addDetailRow(grid, 3, "Created At:", category.getCreatedAt().toString());
        addDetailRow(grid, 4, "Course Count:", String.valueOf(category.getCourseCount()));

        if (category.getIcon() != null && !category.getIcon().isEmpty()) {
            try {
                ImageView iconView = new ImageView(new Image(category.getIcon()));
                iconView.setFitWidth(100);
                iconView.setFitHeight(100);
                iconView.setPreserveRatio(true);
                iconView.getStyleClass().add("clickable-icon");
                iconView.setOnMouseClicked(e -> showFullScreenImage(category.getIcon()));

                grid.add(new Label("Icon:"), 0, 5);
                grid.add(iconView, 1, 5);
            } catch (Exception e) {
                grid.add(new Label("Icon: (loading error)"), 0, 5);
            }
        }

        return grid;
    }

    private Button createCloseButton(Stage stage) {
        Button button = new Button("Close");
        button.getStyleClass().add("close-button");
        button.setOnAction(e -> stage.close());
        return button;
    }

    private void showFullScreenImage(String imageUrl) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Full Screen Image");
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("Press ESC to exit full screen");

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");

        ImageView imageView = new ImageView(new Image(imageUrl));
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(root.widthProperty());
        imageView.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(imageView);

        Scene scene = new Scene(root, Color.BLACK);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        stage.setScene(scene);
        stage.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("detail-label");
        grid.add(lbl, 0, row);

        Label val = new Label(value);
        val.getStyleClass().add("detail-value");
        val.setWrapText(true);
        grid.add(val, 1, row);
        GridPane.setHgrow(val, Priority.ALWAYS);
    }

    @FXML
    private void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Category/AddCategoryView.fxml"));
            Parent root = loader.load();

            AddCategoryController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("New Category");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadCategories();
        } catch (IOException e) {
            showAlert("Error", "Editor Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEditCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/Category/EditCategoryView.fxml"));
            Parent root = loader.load();

            EditCategoryController controller = loader.getController();
            controller.setCategory(category);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Category");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadCategories();
        } catch (IOException e) {
            showAlert("Error", "Editor Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clearSearch() {
        searchField.clear();
        categoryListView.setItems(originalList);
    }

    private void handleDeleteCategory(Category category) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Confirmation");
        confirmation.setHeaderText("Delete category '" + category.getName() + "'?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    categoryService.supprimer(category.getId());
                    showAlert("Success", "Category Deleted", "The category was deleted successfully.", Alert.AlertType.INFORMATION);
                    loadCategories();
                } catch (Exception e) {
                    showAlert("Error", "Deletion Failed", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleEditCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleEditCategory(selected);
        } else {
            showAlert("Error", "No Selection", "Please select a category to edit", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}