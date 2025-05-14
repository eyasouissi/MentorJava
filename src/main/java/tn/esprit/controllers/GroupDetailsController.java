package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.entities.User;
import tn.esprit.entities.group.GroupStudent;
import tn.esprit.entities.project.Project;
import tn.esprit.services.group.GroupService;
import tn.esprit.services.project.ProjectService;
import tn.esprit.utils.BrowserUtils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class GroupDetailsController {
    @FXML private Button backButton;
    @FXML private Button editImageButton;
    @FXML private Button addProjectButton;
    @FXML private ImageView groupImageView;
    @FXML private Circle groupImageCircle;
    @FXML private Text groupNameText;
    @FXML private Text memberCountText;
    @FXML private VBox projectsContainer;
    
    // Calendar related components
    @FXML private DatePicker meetingDatePicker;
    @FXML private TextField meetingUrlField;
    @FXML private Button saveMeetingButton;
    @FXML private VBox upcomingMeetingsContainer;
    @FXML private Button toggleCalendarButton;
    @FXML private VBox calendarContainer;
    
    // Add a new FXML field for background image
    @FXML private ImageView backgroundImageView;
    @FXML private StackPane groupImageContainer;
    @FXML private Button editGroupButton;
    
    private GroupService groupService;
    private ProjectService projectService;
    private GroupStudent group;
    private User currentUser;
    private boolean calendarVisible = true;
    
    @FXML
    public void initialize() {
        groupService = new GroupService();
        projectService = new ProjectService();
        
        // Initialize the date picker with tomorrow's date
        meetingDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Show the URL field again since we're going back to Google Meet
        if (meetingUrlField != null) {
            meetingUrlField.setVisible(true);
            meetingUrlField.setManaged(true);
        }
        
        // Set up the edit group button if available
        if (editGroupButton != null) {
            editGroupButton.setOnAction(e -> showEditGroupDialog());
        }
    }
    
    public void setGroup(GroupStudent group) {
        this.group = group;
        updateGroupInfo();
        loadProjects();
        loadMeetings();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Show edit buttons only for tutors
        boolean isTutor = user != null && user.getRoles().contains("ROLE_TUTOR");
        editImageButton.setVisible(isTutor);
        addProjectButton.setVisible(isTutor);
        saveMeetingButton.setVisible(isTutor);
        meetingUrlField.setEditable(isTutor);
        
        // Show edit group button only for tutors
        if (editGroupButton != null) {
            editGroupButton.setVisible(isTutor);
        }
    }
    
    private void updateGroupInfo() {
        if (group == null) return;
        
        groupNameText.setText(group.getName());
        memberCountText.setText(group.getMemberCount() + " members");
        
        // Load group images
        loadGroupImage();
        loadBackgroundImage();
    }
    
    private void loadGroupImage() {
        try {
            String imagePath = group.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (!file.exists()) {
                    file = new File("uploads/" + imagePath);
                }
                
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    groupImageView.setImage(image);
                    return;
                }
            }
            
            // Load default image if no image is found
            groupImageView.setImage(new Image(getClass().getResourceAsStream("/assets/images/group-default.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadBackgroundImage() {
        // Check if the background image view exists
        if (backgroundImageView == null) return;
        
        try {
            String imagePath = group.getBackgroundImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (!file.exists()) {
                    file = new File("uploads/" + imagePath);
                }
                
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    backgroundImageView.setImage(image);
                    
                    // Set a background blur effect
                    BoxBlur blur = new BoxBlur();
                    blur.setWidth(5);
                    blur.setHeight(5);
                    blur.setIterations(2);
                    backgroundImageView.setEffect(blur);
                    
                    // Make sure the container has the right style
                    if (groupImageContainer != null) {
                        groupImageContainer.getStyleClass().add("with-background");
                    }
                    
                    return;
                }
            }
            
            // If no background image, set a default color in the container
            if (groupImageContainer != null) {
                groupImageContainer.getStyleClass().remove("with-background");
                groupImageContainer.setStyle("-fx-background-color: #f0f0f0;");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadMeetings() {
        if (group == null) return;
        
        upcomingMeetingsContainer.getChildren().clear();
        
        // If the group has a meeting date, display it
        if (group.getMeetingDate() != null) {
            // Set the date picker to the current meeting date
            meetingDatePicker.setValue(group.getMeetingDate());
            
            // Set the meeting URL if available
            if (group.getMeetingUrl() != null) {
                meetingUrlField.setText(group.getMeetingUrl());
            } else {
                meetingUrlField.setText("");
            }
            
            // Add to the upcoming meetings list
            addMeetingToList(group.getMeetingDate(), group.getMeetingUrl());
        } else {
            Label noMeetingsLabel = new Label("No upcoming meetings");
            noMeetingsLabel.getStyleClass().add("info-label");
            upcomingMeetingsContainer.getChildren().add(noMeetingsLabel);
        }
    }
    
    private void addMeetingToList(LocalDate meetingDate, String meetingUrl) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        String formattedDate = meetingDate.format(formatter);
        
        // Create a stylish meeting item
        VBox meetingItem = new VBox(5);
        meetingItem.getStyleClass().add("meeting-item");
        
        // Date label in bold
        Label dateLabel = new Label(formattedDate);
        dateLabel.getStyleClass().add("meeting-date");
        
        // Time label
        Label timeLabel = new Label("9:00 AM - 11:00 AM");
        timeLabel.getStyleClass().add("meeting-time");
        
        // Add URL label if available
        if (meetingUrl != null && !meetingUrl.isEmpty()) {
            Label urlLabel = new Label("Meeting URL: " + meetingUrl);
            urlLabel.getStyleClass().add("meeting-url");
            urlLabel.setWrapText(true);
            meetingItem.getChildren().addAll(dateLabel, timeLabel, urlLabel);
        } else {
            meetingItem.getChildren().addAll(dateLabel, timeLabel);
        }
        
        // Create button container
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-padding: 5 0 0 0;");
        
        // Add "Join Meeting" button
        Button joinButton = new Button("Join Meeting");
        joinButton.getStyleClass().add("join-meeting-button");
        joinButton.setOnAction(e -> openMeeting());
        
        // Only add edit and delete buttons for tutors
        boolean isTutor = currentUser != null && currentUser.getRoles().contains("ROLE_TUTOR");
        
        if (isTutor) {
            // Add "Edit Meeting" button with icon
            Button editButton = new Button("Edit");
            editButton.getStyleClass().add("edit-meeting-button");
            
            try {
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/images/edit.png")));
                editIcon.setFitHeight(16);
                editIcon.setFitWidth(16);
                editButton.setGraphic(editIcon);
            } catch (Exception e) {
                System.err.println("Could not load edit icon: " + e.getMessage());
            }
            
            editButton.setOnAction(e -> editMeeting());
            
            // Add "Delete Meeting" button with icon
            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-meeting-button");
            
            try {
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/images/delete.png")));
                deleteIcon.setFitHeight(16);
                deleteIcon.setFitWidth(16);
                deleteButton.setGraphic(deleteIcon);
            } catch (Exception e) {
                System.err.println("Could not load delete icon: " + e.getMessage());
            }
            
            deleteButton.setOnAction(e -> deleteMeeting());
            
            buttonBox.getChildren().addAll(joinButton, editButton, deleteButton);
        } else {
            // For non-tutors, only show join button
            buttonBox.getChildren().add(joinButton);
        }
        
        meetingItem.getChildren().add(buttonBox);
        
        // Make the meeting item clickable to view details
        meetingItem.setStyle("-fx-cursor: hand; " + meetingItem.getStyle());
        meetingItem.setOnMouseClicked(e -> showMeetingDetails());
        
        upcomingMeetingsContainer.getChildren().add(meetingItem);
    }
    
    /**
     * Displays the meeting details in a dialog
     */
    private void showMeetingDetails() {
        if (group == null || group.getMeetingDate() == null) return;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        String formattedDate = group.getMeetingDate().format(formatter);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Meeting Details");
        alert.setHeaderText(group.getName() + " - Meeting Details");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Date: " + formattedDate),
            new Label("Time: 9:00 AM - 11:00 AM"),
            new Label("Group: " + group.getName()),
            new Label("Meeting URL: " + (group.getMeetingUrl() != null ? group.getMeetingUrl() : "None"))
        );
        
        alert.getDialogPane().setContent(content);
        alert.setResizable(true);
        alert.showAndWait();
    }
    
    /**
     * Deletes the current meeting
     */
    private void deleteMeeting() {
        if (group == null) return;
        
        // Check for tutor role
        if (currentUser == null || !currentUser.getRoles().contains("ROLE_TUTOR")) {
            showCustomAlert(Alert.AlertType.WARNING, "Permission Denied", 
                "Only tutors can delete meetings.");
            return;
        }
        
        boolean confirm = showConfirmDialog(
            "Delete Meeting", 
            "Are you sure you want to delete the meeting for " + group.getName() + "?"
        );
        
        if (confirm) {
            try {
                // Clear meeting data from the group
                group.setMeetingDate(null);
                group.setMeetingUrl(null);
                groupService.updateGroup(group);
                
                // Update UI
                upcomingMeetingsContainer.getChildren().clear();
                Label noMeetingsLabel = new Label("No upcoming meetings");
                noMeetingsLabel.getStyleClass().add("info-label");
                upcomingMeetingsContainer.getChildren().add(noMeetingsLabel);
                
                // Reset the meeting date picker
                meetingDatePicker.setValue(LocalDate.now().plusDays(1));
                meetingUrlField.clear();
                
                showCustomAlert(Alert.AlertType.INFORMATION, "Success", "Meeting deleted successfully");
            } catch (Exception e) {
                showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to delete meeting: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Opens the meeting edit interface
     */
    private void editMeeting() {
        if (group == null || group.getMeetingDate() == null) return;
        
        // Check for tutor role
        if (currentUser == null || !currentUser.getRoles().contains("ROLE_TUTOR")) {
            showCustomAlert(Alert.AlertType.WARNING, "Permission Denied", 
                "Only tutors can edit meetings.");
            return;
        }
        
        // Create a dialog for editing the meeting
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Meeting");
        dialog.setHeaderText("Edit Meeting for " + group.getName());
        
        // Create the form fields
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker datePicker = new DatePicker(group.getMeetingDate());
        TextField urlField = new TextField(group.getMeetingUrl());
        
        // Regenerate URL button
        Button regenerateButton = new Button("Generate New URL");
        regenerateButton.setOnAction(e -> {
            String meetCode = generateValidMeetCode(group.getId());
            urlField.setText("https://meet.google.com/" + meetCode);
        });
        
        // Add form fields
        content.getChildren().addAll(
            new Label("Meeting Date:"),
            datePicker,
            new Label("Meeting URL:"),
            urlField,
            regenerateButton
        );
        
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Show dialog and process result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                LocalDate newDate = datePicker.getValue();
                String newUrl = urlField.getText().trim();
                
                // Validate inputs
                if (newDate == null) {
                    showCustomAlert(Alert.AlertType.WARNING, "Missing Date", "Please select a meeting date.");
                    return;
                }
                
                if (newDate.isBefore(LocalDate.now())) {
                    showCustomAlert(Alert.AlertType.WARNING, "Invalid Date", "Meeting date cannot be in the past.");
                    return;
                }
                
                if (newUrl.isEmpty()) {
                    // Generate URL if empty
                    String meetCode = generateValidMeetCode(group.getId());
                    newUrl = "https://meet.google.com/" + meetCode;
                } else if (!newUrl.startsWith("https://meet.google.com/")) {
                    // Warn if URL doesn't look like Google Meet
                    boolean proceed = showConfirmDialog(
                        "Invalid URL Format", 
                        "The URL doesn't appear to be a Google Meet URL. " +
                        "Do you want to generate a valid Google Meet URL instead?"
                    );
                    
                    if (proceed) {
                        String meetCode = generateValidMeetCode(group.getId());
                        newUrl = "https://meet.google.com/" + meetCode;
                    }
                }
                
                // Update the group with new meeting info
                group.setMeetingDate(newDate);
                group.setMeetingUrl(newUrl);
                groupService.updateGroup(group);
                
                // Update the UI
                upcomingMeetingsContainer.getChildren().clear();
                addMeetingToList(newDate, newUrl);
                
                // Update form fields
                meetingDatePicker.setValue(newDate);
                meetingUrlField.setText(newUrl);
                
                showCustomAlert(Alert.AlertType.INFORMATION, "Success", "Meeting updated successfully");
            } catch (Exception e) {
                showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to update meeting: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Opens a meeting for the current group.
     * Generates a Google Meet link based on the group name and opens it directly.
     */
    private void openMeeting() {
        if (group == null) return;
        
        try {
            // Use any existing meeting URL if available
            if (group.getMeetingUrl() != null && !group.getMeetingUrl().isEmpty() && 
                !group.getMeetingUrl().equals("SCHEDULED")) {
                URI uri = new URI(group.getMeetingUrl());
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                    return;
                }
            }
            
            // Create a valid Google Meet URL with proper format
            String meetCode = generateValidMeetCode(group.getId());
            String meetUrl = "https://meet.google.com/" + meetCode;
            
            // Open in browser
            URI uri = new URI(meetUrl);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
                
                // Update the group's meeting URL in the database for future reference
                group.setMeetingUrl(meetUrl);
                groupService.updateGroup(group);
            } else {
                showCustomAlert(Alert.AlertType.ERROR, "Browser Error", "Default browser could not be opened.");
            }
        } catch (Exception e) {
            showCustomAlert(Alert.AlertType.ERROR, "Meeting Error", "Could not open meeting: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a valid Google Meet code in the format xxx-yyyy-zzz
     * All segments are lowercase letters only to ensure compatibility
     */
    private String generateValidMeetCode(Long groupId) {
        // Use a deterministic but unique approach based on groupId
        String seed = "group" + groupId + System.currentTimeMillis() % 1000;
        
        // Create a hash for pseudo-randomness but deterministic for same group
        int hash = Math.abs(seed.hashCode());
        
        // Use only lowercase letters a-z for maximum compatibility
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        
        StringBuilder code = new StringBuilder();
        
        // First segment: 3 letters
        for (int i = 0; i < 3; i++) {
            code.append(alphabet.charAt((hash + i) % alphabet.length()));
        }
        
        code.append("-");
        
        // Second segment: 4 letters
        for (int i = 0; i < 4; i++) {
            code.append(alphabet.charAt((hash + i + 3) % alphabet.length()));
        }
        
        code.append("-");
        
        // Third segment: 3 letters
        for (int i = 0; i < 3; i++) {
            code.append(alphabet.charAt((hash + i + 7) % alphabet.length()));
        }
        
        return code.toString();
    }
    
    private void loadProjects() {
        try {
            List<Project> projects = projectService.getProjectsByGroupId(group.getId());
            projectsContainer.getChildren().clear();
            
            if (projects.isEmpty()) {
                Text noProjectsText = new Text("No projects yet");
                noProjectsText.getStyleClass().add("section-subtitle");
                projectsContainer.getChildren().add(noProjectsText);
                return;
            }
            
            for (Project project : projects) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/project/ProjectItem.fxml"));
                Parent projectItem = loader.load();
                
                ProjectItemController controller = loader.getController();
                controller.setProject(project);
                controller.setCurrentUser(currentUser);
                controller.setParentController(this);
                
                projectsContainer.getChildren().add(projectItem);
            }
        } catch (Exception e) {
            showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to load projects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClick() {
        try {
            // Load the FrontGroup content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/FrontGroup.fxml"));
            Parent frontGroupContent = loader.load();

            // Get reference to the root BorderPane
            BorderPane rootPane = (BorderPane) backButton.getScene().getRoot();

            // Preserve existing sidebar
            Node sidebar = rootPane.getLeft();

            // Create new container with sidebar and new content
            BorderPane newRoot = new BorderPane();
            newRoot.setLeft(sidebar);
            newRoot.setCenter(frontGroupContent);

            // Update the existing scene
            Scene currentScene = backButton.getScene();
            currentScene.setRoot(newRoot);

        } catch (IOException e) {
            showCustomAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not go back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onEditImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Group Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(editImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Generate a unique filename
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path targetPath = Paths.get("uploads", "group", fileName);
                
                // Create directories if they don't exist
                Files.createDirectories(Paths.get("uploads", "group"));
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update the group in the database
                group.setImage(targetPath.toString());
                groupService.updateGroup(group);
                
                // Update the UI
                groupImageView.setImage(new Image(targetPath.toUri().toString()));
                
                showCustomAlert(Alert.AlertType.INFORMATION, "Success", "Group image updated successfully");
            } catch (Exception e) {
                showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to update image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void onAddProjectClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AddProject.fxml"));
            Parent root = loader.load();
            
            AddProjectController controller = loader.getController();
            controller.setGroup(group);
            controller.setParentController(this);
            
            Stage stage = (Stage) addProjectButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showCustomAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open add project page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onToggleCalendarClick() {
        calendarVisible = !calendarVisible;
        calendarContainer.setVisible(calendarVisible);
        calendarContainer.setManaged(calendarVisible);
        
        // Update button text
        if (toggleCalendarButton != null) {
            toggleCalendarButton.setText(calendarVisible ? "Hide Calendar" : "Show Calendar");
        }
    }
    
    @FXML
    private void onScheduleMeetingClick() {
        if (group == null) return;
        
        LocalDate selectedDate = meetingDatePicker.getValue();
        String meetingUrl = meetingUrlField.getText().trim();
        
        if (selectedDate == null) {
            showCustomAlert(Alert.AlertType.WARNING, "Missing Date", "Please select a meeting date.");
            return;
        }
        
        if (selectedDate.isBefore(LocalDate.now())) {
            showCustomAlert(Alert.AlertType.WARNING, "Invalid Date", "Meeting date cannot be in the past.");
            return;
        }
        
        try {
            // Update the group with the new meeting date
            group.setMeetingDate(selectedDate);
            
            // Process meeting URL
            if (meetingUrl.isEmpty()) {
                // Generate a valid Google Meet code with the proper format
                String meetCode = generateValidMeetCode(group.getId());
                meetingUrl = "https://meet.google.com/" + meetCode;
                
                // Set the generated URL in the URL field so the tutor can see it
                meetingUrlField.setText(meetingUrl);
            } else if (!meetingUrl.startsWith("https://meet.google.com/")) {
                // If URL doesn't start with the correct prefix, show a warning
                boolean proceed = showConfirmDialog(
                    "Invalid URL Format", 
                    "The URL doesn't appear to be a Google Meet URL. " +
                    "We recommend using a Google Meet URL for compatibility.\n\n" +
                    "Do you want to generate a valid Google Meet URL instead?");
                    
                if (proceed) {
                    // Generate a valid URL if user confirms
                    String meetCode = generateValidMeetCode(group.getId());
                    meetingUrl = "https://meet.google.com/" + meetCode;
                    meetingUrlField.setText(meetingUrl);
                }
            }
            
            // Save the meeting URL to the group
            group.setMeetingUrl(meetingUrl);
            groupService.updateGroup(group);
            
            // Clear and update the meetings list
            upcomingMeetingsContainer.getChildren().clear();
            addMeetingToList(selectedDate, meetingUrl);
            
            // Give confirmation to the tutor
            showCustomAlert(Alert.AlertType.INFORMATION, "Success", 
                "Meeting scheduled successfully!\n\n" +
                "Meeting URL: " + meetingUrl + "\n\n" +
                "This URL is now saved and will be used when students join the meeting.");
        } catch (Exception e) {
            showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to schedule meeting: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows a confirmation dialog and returns true if the user clicks OK
     */
    private boolean showConfirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    public void refreshProjects() {
        loadProjects();
    }
    
    private void showCustomAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a dialog to edit the group details
     */
    private void showEditGroupDialog() {
        if (group == null) return;
        
        // Check for tutor role
        if (currentUser == null || !currentUser.getRoles().contains("ROLE_TUTOR")) {
            showCustomAlert(Alert.AlertType.WARNING, "Permission Denied", 
                "Only tutors can edit group details.");
            return;
        }
        
        try {
            // Create a dialog for editing the group
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Group");
            dialog.setHeaderText("Edit Group: " + group.getName());
            
            // Create the form fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Group name field
            TextField nameField = new TextField(group.getName());
            nameField.setPromptText("Group Name");
            
            // Group description field
            TextArea descriptionArea = new TextArea(group.getDescription());
            descriptionArea.setPromptText("Group Description");
            descriptionArea.setPrefRowCount(3);
            
            // Current profile image preview
            ImageView profilePreview = new ImageView();
            profilePreview.setFitHeight(64);
            profilePreview.setFitWidth(64);
            profilePreview.setPreserveRatio(true);
            
            // Load current profile image or default
            try {
                if (group.getImage() != null && !group.getImage().isEmpty()) {
                    File file = new File(group.getImage());
                    if (!file.exists()) {
                        file = new File("uploads/" + group.getImage());
                    }
                    
                    if (file.exists()) {
                        profilePreview.setImage(new Image(file.toURI().toString()));
                    } else {
                        profilePreview.setImage(new Image(getClass().getResourceAsStream("/assets/images/group-default.png")));
                    }
                } else {
                    profilePreview.setImage(new Image(getClass().getResourceAsStream("/assets/images/group-default.png")));
                }
            } catch (Exception e) {
                profilePreview.setImage(new Image(getClass().getResourceAsStream("/assets/images/group-default.png")));
            }
            
            // Current background image preview
            ImageView bgPreview = new ImageView();
            bgPreview.setFitHeight(64);
            bgPreview.setFitWidth(120);
            bgPreview.setPreserveRatio(true);
            
            // Load current background image or default
            try {
                if (group.getBackgroundImage() != null && !group.getBackgroundImage().isEmpty()) {
                    File file = new File(group.getBackgroundImage());
                    if (!file.exists()) {
                        file = new File("uploads/" + group.getBackgroundImage());
                    }
                    
                    if (file.exists()) {
                        bgPreview.setImage(new Image(file.toURI().toString()));
                    } else {
                        bgPreview.setStyle("-fx-background-color: #f0f0f0;");
                    }
                } else {
                    bgPreview.setStyle("-fx-background-color: #f0f0f0;");
                }
            } catch (Exception e) {
                bgPreview.setStyle("-fx-background-color: #f0f0f0;");
            }
            
            // Buttons for selecting images
            Button selectProfileButton = new Button("Select Profile Image");
            Button selectBgButton = new Button("Select Background");
            
            // File path holders
            final String[] newProfileImagePath = {null};
            final String[] newBgImagePath = {null};
            
            // Add file selection handlers
            selectProfileButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Group Profile Image");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                );
                
                File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
                if (selectedFile != null) {
                    try {
                        profilePreview.setImage(new Image(selectedFile.toURI().toString()));
                        newProfileImagePath[0] = selectedFile.getAbsolutePath();
                    } catch (Exception ex) {
                        showCustomAlert(Alert.AlertType.ERROR, "Error", "Could not load selected image.");
                    }
                }
            });
            
            selectBgButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Group Background Image");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                );
                
                File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
                if (selectedFile != null) {
                    try {
                        bgPreview.setImage(new Image(selectedFile.toURI().toString()));
                        newBgImagePath[0] = selectedFile.getAbsolutePath();
                    } catch (Exception ex) {
                        showCustomAlert(Alert.AlertType.ERROR, "Error", "Could not load selected image.");
                    }
                }
            });
            
            // Delete group button
            Button deleteGroupButton = new Button("Delete Group");
            deleteGroupButton.getStyleClass().add("delete-button");
            deleteGroupButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            
            deleteGroupButton.setOnAction(e -> {
                boolean confirm = showConfirmDialog(
                    "Delete Group", 
                    "Are you sure you want to delete the group '" + group.getName() + "'?\n\n" +
                    "This action cannot be undone, and all associated data will be lost."
                );
                
                if (confirm) {
                    try {
                        // Delete the group
                        groupService.supprimer(group.getId());
                        
                        // Close the dialog
                        dialog.setResult(ButtonType.CANCEL);
                        
                        // Show a confirmation
                        showCustomAlert(Alert.AlertType.INFORMATION, "Success", "Group deleted successfully");
                        
                        // Navigate back to the groups list
                        onBackClick();
                    } catch (Exception ex) {
                        showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to delete group: " + ex.getMessage());
                    }
                }
            });
            
            // Add components to the grid
            grid.add(new Label("Group Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            
            grid.add(new Label("Profile Image:"), 0, 2);
            HBox profileBox = new HBox(10, profilePreview, selectProfileButton);
            profileBox.setAlignment(Pos.CENTER_LEFT);
            grid.add(profileBox, 1, 2);
            
            grid.add(new Label("Background:"), 0, 3);
            HBox bgBox = new HBox(10, bgPreview, selectBgButton);
            bgBox.setAlignment(Pos.CENTER_LEFT);
            grid.add(bgBox, 1, 3);
            
            // Add delete button to a separate section
            HBox deleteBox = new HBox(deleteGroupButton);
            deleteBox.setAlignment(Pos.CENTER_RIGHT);
            deleteBox.setPadding(new Insets(20, 0, 0, 0));
            grid.add(deleteBox, 1, 4);
            
            dialog.getDialogPane().setContent(grid);
            
            // Add buttons
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Show dialog and process result
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Validate inputs
                    String newName = nameField.getText().trim();
                    String newDescription = descriptionArea.getText().trim();
                    
                    if (newName.isEmpty()) {
                        showCustomAlert(Alert.AlertType.WARNING, "Missing Name", "Please enter a group name.");
                        return;
                    }
                    
                    if (newDescription.isEmpty()) {
                        showCustomAlert(Alert.AlertType.WARNING, "Missing Description", "Please enter a group description.");
                        return;
                    }
                    
                    // Update group name and description
                    group.setName(newName);
                    group.setDescription(newDescription);
                    
                    // Process profile image if changed
                    if (newProfileImagePath[0] != null) {
                        String fileName = UUID.randomUUID().toString() + "_" + new File(newProfileImagePath[0]).getName();
                        Path targetPath = Paths.get("uploads", "group", fileName);
                        
                        // Create directories if they don't exist
                        Files.createDirectories(Paths.get("uploads", "group"));
                        
                        // Copy the file
                        Files.copy(Paths.get(newProfileImagePath[0]), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        
                        // Update group profile image
                        group.setImage(targetPath.toString());
                    }
                    
                    // Process background image if changed
                    if (newBgImagePath[0] != null) {
                        String fileName = UUID.randomUUID().toString() + "_bg_" + new File(newBgImagePath[0]).getName();
                        Path targetPath = Paths.get("uploads", "group", fileName);
                        
                        // Create directories if they don't exist
                        Files.createDirectories(Paths.get("uploads", "group"));
                        
                        // Copy the file
                        Files.copy(Paths.get(newBgImagePath[0]), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        
                        // Update group background image
                        group.setBackgroundImage(targetPath.toString());
                    }
                    
                    // Save changes to database
                    groupService.updateGroup(group);
                    
                    // Update UI
                    updateGroupInfo();
                    
                    showCustomAlert(Alert.AlertType.INFORMATION, "Success", "Group updated successfully");
                } catch (Exception e) {
                    showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to update group: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            showCustomAlert(Alert.AlertType.ERROR, "Error", "Could not open edit dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showCustomAlert(String alertType, String title, String message) {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initStyle(StageStyle.UTILITY);
        alertStage.setTitle(title);

        VBox container = new VBox(15);
        container.getStyleClass().addAll("alert-dialog", alertType);
        container.setPadding(new Insets(20));

        // Header with icon
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text();
        icon.setStyle("-fx-font-size: 24;");
        switch(alertType) {
            case "success" -> icon.setText("✔️");
            case "error" -> icon.setText("❌");
            default -> icon.setText("ℹ️");
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        header.getChildren().addAll(icon, titleLabel);

        // Content
        Label content = new Label(message);
        content.getStyleClass().add("alert-content");
        content.setWrapText(true);

        // OK Button
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("alert-btn");
        okButton.setOnAction(e -> alertStage.close());

        container.getChildren().addAll(header, content, okButton);

        Scene scene = new Scene(container, 350, 200);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/edit-dialog.css").toExternalForm());
        } catch (NullPointerException e) {
            // Fallback inline styling
            container.setStyle("-fx-background-color: #f8f5fa; "
                    + "-fx-border-color: #8c84a1; "
                    + "-fx-border-radius: 15px; "
                    + "-fx-padding: 20;");
            titleLabel.setStyle("-fx-text-fill: #4a4458; -fx-font-size: 16; -fx-font-weight: bold;");
            content.setStyle("-fx-text-fill: #4a4458;");
            okButton.setStyle("-fx-background-color: #8c84a1; -fx-text-fill: white; -fx-padding: 8 20;");
        }

        alertStage.setScene(scene);
        alertStage.showAndWait();
    }
} 