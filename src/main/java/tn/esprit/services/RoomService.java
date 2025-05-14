package tn.esprit.services;

import com.google.gson.Gson;
import tn.esprit.entities.Room;
import tn.esprit.entities.RoomSettings;
import tn.esprit.entities.RoomState;
import tn.esprit.entities.User;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class RoomService {
    private final Connection connection;

    public RoomService() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    public Room createRoom(String name, User owner, boolean is_public) {
        String roomSql = "INSERT INTO room (name, owner_id, isActive, created_at, backgroundImage) " +
                "VALUES (?, ?, ?, ?, ?)";
        String settingsSql = "INSERT INTO room_settings (room_id, is_public, max_users, allow_media_sharing) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement roomStmt = connection.prepareStatement(roomSql, Statement.RETURN_GENERATED_KEYS)) {
            // Create room
            roomStmt.setString(1, name);
            roomStmt.setLong(2, owner.getId());
            roomStmt.setBoolean(3, true);
            roomStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            roomStmt.setString(5, Room.getRandomBackgroundImage());

            int affectedRows = roomStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating room failed, no rows affected.");
            }

            try (ResultSet generatedKeys = roomStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Room room = new Room();
                    room.setId(generatedKeys.getLong(1));
                    room.setName(name);
                    room.setOwner(owner);
                    room.setActive(true);
                    room.setCreatedAt(LocalDateTime.now().toString());

                    // Create settings
                    try (PreparedStatement settingsStmt = connection.prepareStatement(settingsSql)) {
                        RoomSettings settings = new RoomSettings();
                        settings.setPublic(is_public);
                        settings.setMaxUsers(10);
                        settings.setAllowMediaSharing(true);

                        settingsStmt.setLong(1, room.getId());
                        settingsStmt.setBoolean(2, settings.is_public());
                        settingsStmt.setInt(3, settings.getMaxUsers());
                        settingsStmt.setBoolean(4, settings.isAllowMediaSharing());
                        settingsStmt.executeUpdate();

                        room.setSettings(settings);
                    }

                    // Update owner relationship
                    addUserToRoom(room, owner);
                    return room;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating room: " + e.getMessage());
        }
        return null;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, u.* FROM room r " +
                "LEFT JOIN user u ON r.owner_id = u.id " +
                "WHERE r.isActive = true";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getLong("r.id"));
                room.setName(rs.getString("r.name"));
                room.setActive(rs.getBoolean("r.isActive"));
                room.setCreatedAt(rs.getString("r.created_at"));
                room.setBackgroundImage(rs.getString("r.backgroundImage"));

                // Set owner
                User owner = new User();
                owner.setId(rs.getLong("u.id"));
                owner.setName(rs.getString("u.name"));
                owner.setEmail(rs.getString("u.email"));
                room.setOwner(owner);

                // Load settings
                room.setSettings(getRoomSettings(room.getId()));

                // Load participants
                room.setUsers(getRoomParticipants(room.getId()));

                rooms.add(room);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms: " + e.getMessage());
        }
        return rooms;
    }

    private Set<User> getRoomParticipants(Long roomId) {
        Set<User> participants = new HashSet<>();
        String sql = "SELECT u.* FROM room_users ru " +
                "JOIN user u ON ru.user_id = u.id " +
                "WHERE ru.room_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, roomId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                participants.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching participants: " + e.getMessage());
        }
        return participants;
    }

    private RoomSettings getRoomSettings(Long roomId) {
        String sql = "SELECT * FROM room_settings WHERE room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, roomId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                RoomSettings settings = new RoomSettings();
                settings.setId(rs.getLong("id"));
                settings.setPublic(rs.getBoolean("is_public"));
                settings.setMaxUsers(rs.getInt("max_users"));
                settings.setAllowMediaSharing(rs.getBoolean("allow_media_sharing"));
                return settings;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching settings: " + e.getMessage());
        }
        return new RoomSettings();
    }

    public boolean deleteRoom(Long roomId) {
        String sql = "UPDATE room SET isActive = false WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
            return false;
        }
    }

    public boolean addUserToRoom(Room room, User user) {
        String sql = "INSERT INTO room_users (room_id, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, room.getId());
            stmt.setLong(2, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding user to room: " + e.getMessage());
            return false;
        }
    }

    public boolean removeUserFromRoom(Room room, User user) {
        String sql = "DELETE FROM room_users WHERE room_id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, room.getId());
            stmt.setLong(2, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing user from room: " + e.getMessage());
            return false;
        }
    }

    public boolean updateRoom(Room updatedRoom) {
        String updateRoomSql = "UPDATE room SET name = ?, isActive = ?, backgroundImage = ? WHERE id = ?";
        String updateSettingsSql = "UPDATE room_settings SET is_public = ?, max_users = ?, allow_media_sharing = ? WHERE room_id = ?";

        try {
            connection.setAutoCommit(false);

            // Update room basic info
            try (PreparedStatement roomStmt = connection.prepareStatement(updateRoomSql)) {
                roomStmt.setString(1, updatedRoom.getName());
                roomStmt.setBoolean(2, updatedRoom.isActive());
                roomStmt.setString(3, updatedRoom.getBackgroundImage());
                roomStmt.setLong(4, updatedRoom.getId());
                int roomUpdated = roomStmt.executeUpdate();

                if (roomUpdated == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // Update room settings
            try (PreparedStatement settingsStmt = connection.prepareStatement(updateSettingsSql)) {
                RoomSettings settings = updatedRoom.getSettings();
                settingsStmt.setBoolean(1, settings.is_public());
                settingsStmt.setInt(2, settings.getMaxUsers());
                settingsStmt.setBoolean(3, settings.isAllowMediaSharing());
                settingsStmt.setLong(4, updatedRoom.getId());
                int settingsUpdated = settingsStmt.executeUpdate();

                if (settingsUpdated == 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Error updating room: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public Optional<Room> getRoomById(Long id) {
        String sql = "SELECT r.*, u.*, rs.*, r.state_json FROM room r " +
                "LEFT JOIN user u ON r.owner_id = u.id " +
                "LEFT JOIN room_settings rs ON r.id = rs.room_id " +
                "WHERE r.id = ? AND r.isActive = true";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Room room = new Room();
                room.setId(rs.getLong("r.id"));
                room.setName(rs.getString("r.name"));
                room.setActive(rs.getBoolean("r.isActive"));
                room.setCreatedAt(rs.getString("r.created_at"));
                room.setBackgroundImage(rs.getString("r.backgroundImage"));

                // Set owner
                User owner = new User();
                owner.setId(rs.getLong("u.id"));
                owner.setName(rs.getString("u.name"));
                owner.setEmail(rs.getString("u.email"));
                room.setOwner(owner);

                // Set settings
                RoomSettings settings = new RoomSettings();
                settings.setId(rs.getLong("rs.id"));
                settings.setPublic(rs.getBoolean("rs.is_public"));
                settings.setMaxUsers(rs.getInt("rs.max_users"));
                settings.setAllowMediaSharing(rs.getBoolean("rs.allow_media_sharing"));
                room.setSettings(settings);

                // Load state from state_json
                String stateJson = rs.getString("state_json");
                if (stateJson != null && !stateJson.isEmpty()) {
                    try {
                        RoomState state = new Gson().fromJson(stateJson, RoomState.class);
                        room.setState(state);
                        System.out.println("Loaded room state: " + stateJson);
                    } catch (Exception e) {
                        System.err.println("Error parsing room state JSON: " + e.getMessage());
                        room.setState(new RoomState());
                    }
                } else {
                    System.out.println("No saved state found for room " + id);
                    room.setState(new RoomState());
                }

                // Load participants
                room.setUsers(getRoomParticipants(id));

                return Optional.of(room);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    private final Map<Long, Timer> saveDebouncers = new HashMap<>();

    public void debouncedSave(Room room, int delayMillis) {
        if (saveDebouncers.containsKey(room.getId())) {
            saveDebouncers.get(room.getId()).cancel();
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveRoomState(room);
                timer.cancel();
                saveDebouncers.remove(room.getId());
            }
        }, delayMillis);

        saveDebouncers.put(room.getId(), timer);
    }

    public void saveImmediately(Room room) {
        cancelPendingSave(room.getId());
        saveRoomState(room);
    }

    private void cancelPendingSave(Long roomId) {
        if (saveDebouncers.containsKey(roomId)) {
            saveDebouncers.get(roomId).cancel();
            saveDebouncers.remove(roomId);
        }
    }

    public void saveRoomState(Room room) {
        String sql = "UPDATE room SET state_json = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String stateJson = new Gson().toJson(room.getState());
            stmt.setString(1, stateJson);
            stmt.setLong(2, room.getId());
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Successfully saved room state: " + stateJson);
            } else {
                System.err.println("Failed to save room state - no rows updated");
            }
        } catch (SQLException e) {
            System.err.println("Error saving room state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Room> getRoomsByOwnerId(Long ownerId) {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT r.*, u.* FROM room r " +
                "LEFT JOIN user u ON r.owner_id = u.id " +
                "WHERE r.owner_id = ? AND r.isActive = true";

        try (Connection connection = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setLong(1, ownerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room();
                    // Map room properties
                    room.setId(rs.getLong("r.id"));
                    room.setName(rs.getString("r.name"));
                    room.setActive(rs.getBoolean("r.isActive"));
                    room.setCreatedAt(rs.getString("r.created_at"));
                    room.setBackgroundImage(rs.getString("r.backgroundImage"));

                    // Map owner
                    User owner = new User();
                    owner.setId(rs.getLong("u.id"));
                    owner.setName(rs.getString("u.name"));
                    owner.setEmail(rs.getString("u.email"));
                    room.setOwner(owner);

                    // Load settings and participants
                    room.setSettings(getRoomSettings(room.getId()));
                    room.setUsers(getRoomParticipants(room.getId()));

                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms for owner ID " + ownerId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rooms;
    }
}