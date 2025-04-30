package tn.esprit.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import tn.esprit.entities.Forum;
import tn.esprit.tools.MyDataBase;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ForumController implements Initializable {

    // Form Fields
    @FXML private TextField titleField;
    @FXML private HTMLEditor htmlEditor;
    @FXML private TextField topicsField;
    @FXML private CheckBox isPublicCheckbox;

    // Validation Labels
    @FXML private Label titleError;
    @FXML private Label titleWordCount;
    @FXML private Label descriptionError;
    @FXML private Label descriptionCharCount;
    @FXML private Label topicsError;

    // Table Components
    @FXML private TableView<Forum> forumTable;
    @FXML private TableColumn<Forum, String> titleColumn;
    @FXML private TableColumn<Forum, String> descriptionColumn;
    @FXML private TableColumn<Forum, Boolean> isPublicColumn;
    @FXML private TableColumn<Forum, String> topicsColumn;
    @FXML private TableColumn<Forum, Integer> viewsColumn;
    @FXML private TableColumn<Forum, Integer> totalPostsColumn;
    @FXML private TableColumn<Forum, String> createdAtColumn;
    @FXML private TableColumn<Forum, String> updatedAtColumn;

    // Constants
    private static final int MAX_TITLE_WORDS = 3;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    // Data
    private ObservableList<Forum> forumList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureTableColumns();
        loadForums();
        setupTableSelectionListener();
        setupInputValidations();
    }

    private void configureTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Configure description column to display HTML content
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> new TableCell<Forum, String>() {
            private final WebView webView = new WebView();
            private final WebEngine webEngine = webView.getEngine();
            {
                webView.setPrefHeight(50);
                webView.setPrefWidth(300);
                webView.setContextMenuEnabled(false);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Wrap content in basic HTML structure
                    String wrappedContent = "<!DOCTYPE html><html><head>" +
                            "<style>body { margin: 0; padding: 0; font-family: Arial; font-size: 12px; }</style>" +
                            "</head><body>" + item + "</body></html>";
                    webEngine.loadContent(wrappedContent);
                    setGraphic(webView);
                }
            }
        });

        isPublicColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getIsPublic()));
        topicsColumn.setCellValueFactory(new PropertyValueFactory<>("topics"));
        viewsColumn.setCellValueFactory(new PropertyValueFactory<>("views"));
        totalPostsColumn.setCellValueFactory(new PropertyValueFactory<>("totalPosts"));

        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || getTableRow() == null ? null :
                        getTableRow().getItem().getCreatedAt().format(dateFormatter));
            }
        });

        updatedAtColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || getTableRow() == null ? null :
                        getTableRow().getItem().getUpdatedAt().format(dateFormatter));
            }
        });

        // Add column resize listener
        descriptionColumn.widthProperty().addListener((obs, oldVal, newVal) -> {
            forumTable.refresh();
        });
    }

    private void setupInputValidations() {
        setupTitleValidation();
        setupDescriptionValidation();
        setupTopicsValidation();
    }

    private void setupTitleValidation() {
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && Character.isLowerCase(newVal.charAt(0))) {
                titleField.setText(Character.toUpperCase(newVal.charAt(0)) + newVal.substring(1));
            }

            String[] words = newVal.trim().split("\\s+");
            boolean validWordCount = words.length <= MAX_TITLE_WORDS;

            titleWordCount.setText("Words: " + words.length + "/" + MAX_TITLE_WORDS);
            titleWordCount.setStyle(validWordCount ? "" : "-fx-text-fill: red;");

            if (!validWordCount) {
                titleField.setText(String.join(" ", Arrays.copyOf(words, MAX_TITLE_WORDS)));
            }

            boolean isEmpty = newVal.trim().isEmpty();
            titleError.setText(isEmpty ? "Title cannot be empty!" : "");
            setFieldStyle(titleField, isEmpty || !validWordCount);
        });
    }

    private void setupDescriptionValidation() {
        htmlEditor.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus is lost
                validateDescriptionContent();
            }
        });
    }

    private void validateDescriptionContent() {
        String htmlContent = htmlEditor.getHtmlText();
        String plainText = HTML_TAG_PATTERN.matcher(htmlContent).replaceAll("");

        boolean validLength = plainText.length() <= MAX_DESCRIPTION_LENGTH;
        descriptionCharCount.setText("Characters: " + plainText.length() + "/" + MAX_DESCRIPTION_LENGTH);
        descriptionCharCount.setStyle(validLength ? "" : "-fx-text-fill: red;");

        boolean isEmpty = plainText.trim().isEmpty();
        descriptionError.setText(isEmpty ? "Description cannot be empty!" : "");
    }

    private void setupTopicsValidation() {
        topicsField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSingleWord = !newVal.contains(" ");
            boolean isEmpty = newVal.trim().isEmpty();

            topicsError.setText(isEmpty ? "Topic cannot be empty!" :
                    !isSingleWord ? "Only one word allowed!" : "");
            setFieldStyle(topicsField, isEmpty || !isSingleWord);

            if (!isSingleWord) {
                topicsField.setText(newVal.replaceAll("\\s+", ""));
            }
        });
    }

    private void setFieldStyle(TextInputControl field, boolean invalid) {
        if (invalid) {
            if (!field.getStyleClass().contains("error-field")) {
                field.getStyleClass().add("error-field");
            }
        } else {
            field.getStyleClass().removeAll("error-field");
        }
    }

    private void loadForums() {
        forumList.clear();
        String sql = "SELECT id, title, description, is_public, topics, created_at, updated_at, views, totalposts FROM forum";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Forum forum = new Forum();
                forum.setId(rs.getLong("id"));
                forum.setTitle(rs.getString("title"));
                forum.setDescription(rs.getString("description"));
                forum.setIsPublic(rs.getBoolean("is_public"));
                forum.setTopics(rs.getString("topics"));
                forum.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                forum.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                forum.setViews(rs.getInt("views"));
                forum.setTotalPosts(rs.getInt("totalposts"));
                forumList.add(forum);
            }
            forumTable.setItems(forumList);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading forums: " + e.getMessage());
        }
    }

    private void setupTableSelectionListener() {
        forumTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                titleField.setText(newVal.getTitle());
                htmlEditor.setHtmlText(newVal.getDescription());
                topicsField.setText(newVal.getTopics());
                isPublicCheckbox.setSelected(newVal.getIsPublic());
            }
        });
    }

    @FXML
    private void handleAdd() {
        validateDescriptionContent();
        if (!isFormValid()) return;

        String title = titleField.getText().trim();
        String description = htmlEditor.getHtmlText();
        String topics = topicsField.getText().trim();
        boolean isPublic = isPublicCheckbox.isSelected();

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO forum (title, description, is_public, topics, created_at, updated_at, views, totalposts) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setBoolean(3, isPublic);
            pstmt.setString(4, topics);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(7, 0);
            pstmt.setInt(8, 0);

            pstmt.executeUpdate();
            loadForums();
            clearFields();
            showSuccessAlert("Success", "Forum added successfully!");
        } catch (SQLException e) {
            showAlert("Database Error", "Error adding forum: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        validateDescriptionContent();
        if (!isFormValid()) return;

        Forum selectedForum = forumTable.getSelectionModel().getSelectedItem();
        if (selectedForum == null) {
            showAlert("Selection Error", "Please select a forum to update.");
            return;
        }

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE forum SET title=?, description=?, is_public=?, topics=?, updated_at=? WHERE id=?")) {

            pstmt.setString(1, titleField.getText().trim());
            pstmt.setString(2, htmlEditor.getHtmlText());
            pstmt.setBoolean(3, isPublicCheckbox.isSelected());
            pstmt.setString(4, topicsField.getText().trim());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(6, selectedForum.getId());

            pstmt.executeUpdate();
            loadForums();
            clearFields();
            showSuccessAlert("Success", "Forum updated successfully!");
        } catch (SQLException e) {
            showAlert("Database Error", "Error updating forum: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Forum selectedForum = forumTable.getSelectionModel().getSelectedItem();
        if (selectedForum == null) {
            showAlert("Selection Error", "Please select a forum to delete.");
            return;
        }

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM forum WHERE id=?")) {

            pstmt.setLong(1, selectedForum.getId());
            pstmt.executeUpdate();
            forumList.remove(selectedForum);
            clearFields();
            showSuccessAlert("Success", "Forum deleted successfully!");
        } catch (SQLException e) {
            showAlert("Database Error", "Error deleting forum: " + e.getMessage());
        }
    }

    private boolean isFormValid() {
        String plainDescription = HTML_TAG_PATTERN.matcher(htmlEditor.getHtmlText()).replaceAll("");

        boolean titleValid = !titleField.getText().trim().isEmpty() &&
                titleField.getText().trim().split("\\s+").length <= MAX_TITLE_WORDS;

        boolean descriptionValid = !plainDescription.trim().isEmpty() &&
                plainDescription.length() <= MAX_DESCRIPTION_LENGTH;

        boolean topicsValid = !topicsField.getText().trim().isEmpty() &&
                !topicsField.getText().contains(" ");

        return titleValid && descriptionValid && topicsValid;
    }

    private void clearFields() {
        titleField.clear();
        htmlEditor.setHtmlText("");
        topicsField.clear();
        isPublicCheckbox.setSelected(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("error");
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("success-alert");
        alert.showAndWait();
    }
}