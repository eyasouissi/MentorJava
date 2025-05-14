package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import tn.esprit.entities.Comment;
import tn.esprit.entities.Post;
import tn.esprit.tools.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

// CommentController.java
public class CommentController {
    private Comment currentComment;
    private Post parentPost;
    private Runnable refreshComments;

    public void setCommentData(Comment comment, Post post, Runnable refreshCallback) {
        this.currentComment = comment;
        this.parentPost = post;
        this.refreshComments = refreshCallback;
    }

    @FXML
    public void handleEditComment() {
        TextInputDialog dialog = new TextInputDialog(currentComment.getContent());
        dialog.setTitle("Edit Comment");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newContent -> {
            if (newContent.trim().isEmpty()) return;

            try (Connection conn = MyDataBase.getInstance().getCnx();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "UPDATE comment SET content = ?, is_edited = TRUE WHERE id = ?")) {

                pstmt.setString(1, newContent);
                pstmt.setLong(2, currentComment.getId());
                pstmt.executeUpdate();

                currentComment.setContent(newContent);
                currentComment.setEdited(true);
                refreshComments.run();

            } catch (SQLException e) {
                showAlert("Error", "Update failed: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleDeleteComment() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Comment");
        confirm.setHeaderText("Are you sure you want to delete this comment?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = MyDataBase.getInstance().getCnx();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "DELETE FROM comment WHERE id = ?")) {

                    pstmt.setLong(1, currentComment.getId());
                    pstmt.executeUpdate();

                    parentPost.getComments().remove(currentComment);
                    refreshComments.run();

                } catch (SQLException e) {
                    showAlert("Error", "Delete failed: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}