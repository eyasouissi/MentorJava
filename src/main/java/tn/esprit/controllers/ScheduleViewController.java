package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.services.group.GroupService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ScheduleViewController {
    @FXML private Button backButton;
    @FXML private Label groupNameLabel;
    @FXML private DatePicker meetingDatePicker;
    @FXML private Button saveMeetingButton;
    @FXML private VBox upcomingMeetingsContainer;
    
    private GroupStudent group;
    private GroupService groupService;
    
    @FXML
    public void initialize() {
        groupService = new GroupService();
        meetingDatePicker.setValue(LocalDate.now().plusDays(1));
    }
    
    public void setGroup(GroupStudent group) {
        this.group = group;
        
        // Update UI with group info
        if (group != null) {
            groupNameLabel.setText(group.getName() + " - Schedule");
            
            // Show current meeting date if exists
            if (group.getMeetingDate() != null) {
                meetingDatePicker.setValue(group.getMeetingDate());
                
                // Add to upcoming meetings
                addMeetingToList(group.getMeetingDate());
            }
        }
    }
    
    private void addMeetingToList(LocalDate meetingDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        String formattedDate = meetingDate.format(formatter);
        
        Label meetingLabel = new Label(formattedDate);
        meetingLabel.getStyleClass().add("meeting-date");
        
        upcomingMeetingsContainer.getChildren().add(meetingLabel);
    }
    
    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/GroupDetails.fxml"));
            Parent root = loader.load();
            
            GroupDetailsController controller = loader.getController();
            controller.setGroup(group);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not go back to group details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onSaveMeetingClick() {
        if (group == null) return;
        
        LocalDate selectedDate = meetingDatePicker.getValue();
        
        if (selectedDate == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Date", "Please select a meeting date.");
            return;
        }
        
        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date", "Meeting date cannot be in the past.");
            return;
        }
        
        try {
            // Update the group with the new meeting date
            group.setMeetingDate(selectedDate);
            groupService.updateGroup(group);
            
            // Clear and update the meetings list
            upcomingMeetingsContainer.getChildren().clear();
            addMeetingToList(selectedDate);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Meeting scheduled successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to schedule meeting: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 