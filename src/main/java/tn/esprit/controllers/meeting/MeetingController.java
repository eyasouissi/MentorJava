package tn.esprit.controllers.meeting;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import tn.esprit.entities.User;
import tn.esprit.entities.group.GroupStudent;

public class MeetingController {
    @FXML private WebView webView;
    @FXML private Label cameraOffLabel;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField chatInput;
    @FXML private Button sendButton;
    @FXML private Button toggleMicButton;
    @FXML private Button toggleCameraButton;
    @FXML private Button shareScreenButton;
    @FXML private Button leaveButton;
    @FXML private Label meetingTitleLabel;
    @FXML private Label participantsLabel;
    @FXML private Button inviteButton;
    
    private boolean micOn = true;
    private boolean cameraOn = true;
    private boolean screenSharing = false;
    private User currentUser;
    private GroupStudent group;
    private String[] randomMessages = {
        "I think we should focus on the project timeline",
        "Has everyone reviewed the latest documentation?",
        "Let's schedule another follow-up meeting next week",
        "I agree with that approach",
        "I'll share my screen to demonstrate the issue",
        "Can everyone see my screen?",
        "The deadline is approaching quickly",
        "I'll take notes during this meeting",
        "Let's vote on the proposed solution",
        "Who wants to present first?"
    };
    
    private Random random = new Random();
    private int participantCount = 1;
    
    /**
     * Initializes the meeting interface
     */
    @FXML
    public void initialize() {
        // Show a placeholder for video (since we can't actually capture webcam without additional libraries)
        webView.getEngine().loadContent(
            "<html><body style='margin:0;padding:0;background:#333;color:white;display:flex;align-items:center;justify-content:center;height:100%;'>" +
            "<div style='text-align:center;'>" +
            "<svg width='80' height='80' viewBox='0 0 24 24' fill='white'><path d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z'/></svg>" +
            "<p>Your video feed would appear here</p>" +
            "</div></body></html>"
        );
        
        // Add a simulated participant after delay
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    addParticipant();
                    simulateIncomingMessage();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Sets the current user for the meeting
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Sets the group for this meeting
     */
    public void setGroup(GroupStudent group) {
        this.group = group;
        if (group != null) {
            meetingTitleLabel.setText(group.getName() + " Meeting");
        }
    }
    
    /**
     * Toggles the microphone on/off
     */
    @FXML
    public void toggleMicrophone() {
        micOn = !micOn;
        toggleMicButton.setText(micOn ? "Mute" : "Unmute");
        showNotification(micOn ? "Microphone turned on" : "Microphone turned off");
    }
    
    /**
     * Toggles the camera on/off
     */
    @FXML
    public void toggleCamera() {
        cameraOn = !cameraOn;
        toggleCameraButton.setText(cameraOn ? "Turn Off Camera" : "Turn On Camera");
        cameraOffLabel.setVisible(!cameraOn);
        showNotification(cameraOn ? "Camera turned on" : "Camera turned off");
    }
    
    /**
     * Simulates screen sharing
     */
    @FXML
    public void shareScreen() {
        screenSharing = !screenSharing;
        shareScreenButton.setText(screenSharing ? "Stop Sharing" : "Share Screen");
        
        if (screenSharing) {
            webView.getEngine().loadContent(
                "<html><body style='margin:0;padding:0;background:#444;color:white;display:flex;align-items:center;justify-content:center;height:100%;'>" +
                "<div style='text-align:center;'>" +
                "<svg width='80' height='80' viewBox='0 0 24 24' fill='white'><path d='M20 18c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z'/></svg>" +
                "<p>You are sharing your screen</p>" +
                "</div></body></html>"
            );
            showNotification("Screen sharing started");
        } else {
            // Restore regular video view
            webView.getEngine().loadContent(
                "<html><body style='margin:0;padding:0;background:#333;color:white;display:flex;align-items:center;justify-content:center;height:100%;'>" +
                "<div style='text-align:center;'>" +
                "<svg width='80' height='80' viewBox='0 0 24 24' fill='white'><path d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z'/></svg>" +
                "<p>Your video feed would appear here</p>" +
                "</div></body></html>"
            );
            showNotification("Screen sharing stopped");
        }
    }
    
    /**
     * Sends a chat message
     */
    @FXML
    public void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            addMessageToChat(currentUser != null ? currentUser.getName() : "You", message, true);
            chatInput.clear();
            
            // Simulate response after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500 + random.nextInt(2000));
                    Platform.runLater(this::simulateIncomingMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Simulates adding another participant to the meeting
     */
    private void addParticipant() {
        participantCount++;
        participantsLabel.setText("Participants: " + participantCount);
        showNotification("A new participant has joined");
    }
    
    /**
     * Simulates receiving a message from another participant
     */
    private void simulateIncomingMessage() {
        String[] names = {"Alex", "Jordan", "Taylor", "Casey", "Morgan"};
        String sender = names[random.nextInt(names.length)];
        String message = randomMessages[random.nextInt(randomMessages.length)];
        addMessageToChat(sender, message, false);
    }
    
    /**
     * Adds a message to the chat panel
     */
    private void addMessageToChat(String sender, String message, boolean isFromCurrentUser) {
        VBox messageBox = new VBox(2);
        messageBox.getStyleClass().add("chat-message");
        
        Label senderLabel = new Label(sender);
        senderLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        
        messageBox.getChildren().addAll(senderLabel, messageLabel);
        
        if (isFromCurrentUser) {
            messageBox.getStyleClass().add("sent-message");
            messageBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBox.getStyleClass().add("received-message");
            messageBox.setAlignment(Pos.CENTER_LEFT);
        }
        
        chatMessagesContainer.getChildren().add(messageBox);
    }
    
    /**
     * Displays a notification about an action
     */
    private void showNotification(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Meeting Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
        
        // Auto close after 2 seconds
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Invites a new participant to the meeting
     */
    @FXML
    public void inviteParticipant() {
        // In a real app, this would show a dialog to select a user from contacts
        showNotification("Invitation sent");
        
        // Simulate someone joining after invitation
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(this::addParticipant);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Closes the meeting and returns to the previous screen
     */
    @FXML
    public void leaveMeeting() {
        // Close the meeting window
        Stage stage = (Stage) leaveButton.getScene().getWindow();
        stage.close();
    }
} 