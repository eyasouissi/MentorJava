package tn.esprit.controllers.user.admin;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import tn.esprit.controllers.user.EditProfileController;
import tn.esprit.entities.User;

public class EditAdminProfileController extends EditProfileController {
    @FXML private CheckBox adminCheckbox;
    @FXML private CheckBox tutorCheckbox;
    @FXML private CheckBox studentCheckbox;

    @Override
    public void setUserData(User user) {
        super.setUserData(user);

        if (user.getRoles() != null) {
            adminCheckbox.setSelected(user.getRoles().contains("ROLE_ADMIN"));
            tutorCheckbox.setSelected(user.getRoles().contains("ROLE_TUTOR"));
            studentCheckbox.setSelected(user.getRoles().contains("ROLE_STUDENT"));
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
}