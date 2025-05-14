package tn.esprit.entities;

public class RoomSettings {
    private Long id;
    private Room room;
    private boolean is_public;
    private int maxUsers;
    private boolean allowMediaSharing;
    private String createdAt;

    // Constructors
    public RoomSettings() {
        this.is_public = true;
        this.maxUsers = 10;
        this.allowMediaSharing = true;
    }

    public RoomSettings(Long id, Room room, boolean is_public, int maxUsers, boolean allowMediaSharing, String createdAt) {
        this.id = id;
        this.room = room;
        this.is_public = is_public;
        this.maxUsers = maxUsers;
        this.allowMediaSharing = allowMediaSharing;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean is_public() {
        return is_public;
    }

    public void setPublic(boolean is_public) {
        this.is_public = is_public;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public boolean isAllowMediaSharing() {
        return allowMediaSharing;
    }

    public void setAllowMediaSharing(boolean allowMediaSharing) {
        this.allowMediaSharing = allowMediaSharing;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
