package tn.esprit.controllers.user.admin;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.controllers.user.EditProfileController;
import tn.esprit.entities.User;

public class EditAdminProfileController extends EditProfileController {
    @FXML private CheckBox adminCheckbox;
    @FXML private CheckBox tutorCheckbox;
    @FXML private CheckBox studentCheckbox;
    @FXML private ImageView profileImageView;

    @Override
    public void setUserData(User user) {
        super.setUserData(user); // This handles the basic profile data and image

        if (user.getRoles() != null) {
            adminCheckbox.setSelected(user.getRoles().contains("ROLE_ADMIN"));
            tutorCheckbox.setSelected(user.getRoles().contains("ROLE_TUTOR"));
            studentCheckbox.setSelected(user.getRoles().contains("ROLE_STUDENT"));
        }

        // Ensure profile image is loaded (redundant if parent handles it, but safe to keep)
        if (user.getPfp() != null && !user.getPfp().isEmpty()) {
            try {
                profileImageView.setImage(new Image(user.getPfp()));
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }
    }

    @FXML
    @Override
    protected void handleSave() {
        // Update roles first
        currentUser.getRoles().clear();

        if (adminCheckbox.isSelected()) {
            currentUser.getRoles().add("ROLE_ADMIN");
        }
        if (tutorCheckbox.isSelected()) {
            currentUser.getRoles().add("ROLE_TUTOR");
        }
        if (studentCheckbox.isSelected()) {
            currentUser.getRoles().add("ROLE_STUDENT");
        }

        // Then call parent save functionality
        super.handleSave();
    }

    @Override
    protected void handleCancel() {
        super.handleCancel();
    }
}