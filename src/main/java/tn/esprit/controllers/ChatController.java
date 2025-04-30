package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.services.ChatGPTService;

public class ChatController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField inputField;

    // Method to send messages
    public void sendMessage() {
        String userMessage = inputField.getText();
        if (userMessage.isEmpty()) return;

        // Append the user's message to the chat
        chatArea.appendText("You: " + userMessage + "\n");

        // Clear the input field
        inputField.clear();

        try {
            // Always use OpenAI API
            String response = ChatGPTService.getStudyBuddyResponse(userMessage);
            chatArea.appendText("StudyBuddy: " + response + "\n");
        } catch (Exception e) {
            e.printStackTrace(); // just print the error
            chatArea.appendText("StudyBuddy: Sorry, I couldn't get a response. Please try again.\n");
        }
    }
}
