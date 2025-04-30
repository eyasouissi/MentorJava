package tn.esprit.entities;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class Room {
    private int id;
    private String name;
    private User owner;
    private boolean isActive;
    private String createdAt;
    private Set<User> users;
    private RoomSettings settings;
    private String backgroundImage = "/images/room.jpg"; // Default path
    private String stateJson;

    // List of available background images
    private static final List<String> BACKGROUND_IMAGES = new ArrayList<>();

    static {
        // Add available background image paths to the list
        BACKGROUND_IMAGES.add("/images/room.jpg");  // Example image 1
        BACKGROUND_IMAGES.add("/images/room1.jpg");  // Example image 2
        BACKGROUND_IMAGES.add("/images/room2.jpg");  // Example image 3
    }

    // Randomly select a background image from the list
    public static String getRandomBackgroundImage() {
        Random rand = new Random();
        return BACKGROUND_IMAGES.get(rand.nextInt(BACKGROUND_IMAGES.size()));
    }

    // Constructor to set a random background image when creating a new room
    public Room() {
        // Select a random background image when a new room is created
        this.backgroundImage = getRandomBackgroundImage();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public RoomSettings getSettings() {
        return settings;
    }

    public void setSettings(RoomSettings settings) {
        this.settings = settings;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImagePath) {
        this.backgroundImage = backgroundImagePath;
    }

    public String getStateJson() {
        return stateJson;
    }

    public void setStateJson(String stateJson) {
        this.stateJson = stateJson;
    }
}
