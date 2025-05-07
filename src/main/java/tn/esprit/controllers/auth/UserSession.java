package tn.esprit.controllers.auth;


import tn.esprit.entities.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public static void start(User user) {
        getInstance().currentUser = user;
    }


    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        currentUser = null;
        instance = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
    public void clearSession() {
        this.currentUser = null;
    }
}
