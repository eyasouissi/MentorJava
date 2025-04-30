package tn.esprit.entities;

public class RoomSettings {
    private int id;
    private Room room;
    private boolean isPublic;
    private int maxUsers;
    private boolean allowMediaSharing;
    private String createdAt;

    // Constructors
    public RoomSettings() {}

    public RoomSettings(int id, Room room, boolean isPublic, int maxUsers, boolean allowMediaSharing, String createdAt) {
        this.id = id;
        this.room = room;
        this.isPublic = isPublic;
        this.maxUsers = maxUsers;
        this.allowMediaSharing = allowMediaSharing;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
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
