package tn.esprit.utils;

import tn.esprit.entities.User;

/**
 * Singleton class to manage user session across the application
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {
        // Private constructor to prevent direct instantiation
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}