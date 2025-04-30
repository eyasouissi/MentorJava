package tn.esprit.controllers;

// Create new file: StudyBuddyAI.java
public class StudyBuddyAI {
    public static String getResponse(String userMessage) {
        String lowerMsg = userMessage.toLowerCase();

        if (lowerMsg.contains("motivat") || lowerMsg.contains("encourage")) {
            return "Remember: Every expert was once a beginner. You're making progress every time you study! 💪";
        }
        else if (lowerMsg.contains("math") || lowerMsg.contains("calculate")) {
            return "For math help, remember to break problems into smaller steps. Would you like me to suggest some learning resources?";
        }
        else if (lowerMsg.contains("schedule") || lowerMsg.contains("plan")) {
            return "Try using the Pomodoro technique: 25 minutes focused study, 5 minute break. Repeat! 🍅";
        }
        else if (lowerMsg.contains("hello") || lowerMsg.contains("hi")) {
            return "Hi there! Ready to study? I'm here to help with motivation and study tips!";
        }

        return "I'm here to help you study! Try asking about:\n- Motivation\n- Study techniques\n- Subject help";
    }
}
