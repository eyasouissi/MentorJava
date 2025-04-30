package tn.esprit.services;

import tn.esprit.entities.Room;
import tn.esprit.entities.RoomSettings;
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

    public Room createRoom(String name, User owner, boolean isPublic) {
        String roomSql = "INSERT INTO room (name, owner_id, is_active, created_at, backgroundImage) " +
                "VALUES (?, ?, ?, ?, ?)";
        String settingsSql = "INSERT INTO room_settings (room_id, is_public, max_users, allow_media_sharing) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement roomStmt = connection.prepareStatement(roomSql, Statement.RETURN_GENERATED_KEYS)) {
            // Create room
            roomStmt.setString(1, name);
            roomStmt.setInt(2, owner.getId().intValue());
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
                    room.setId(generatedKeys.getInt(1));
                    room.setName(name);
                    room.setOwner(owner);
                    room.setActive(true);
                    room.setCreatedAt(LocalDateTime.now().toString());

                    // Create settings
                    try (PreparedStatement settingsStmt = connection.prepareStatement(settingsSql)) {
                        RoomSettings settings = new RoomSettings();
                        settings.setPublic(isPublic);
                        settings.setMaxUsers(10);
                        settings.setAllowMediaSharing(true);

                        settingsStmt.setInt(1, room.getId());
                        settingsStmt.setBoolean(2, settings.isPublic());
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
                "WHERE r.is_active = true";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("r.id"));
                room.setName(rs.getString("r.name"));
                room.setActive(rs.getBoolean("r.is_active"));
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

    private Set<User> getRoomParticipants(int roomId) {
        Set<User> participants = new HashSet<>();
        String sql = "SELECT u.* FROM room_users ru " +
                "JOIN user u ON ru.user_id = u.id " +
                "WHERE ru.room_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
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

    private RoomSettings getRoomSettings(int roomId) {
        String sql = "SELECT * FROM room_settings WHERE room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                RoomSettings settings = new RoomSettings();
                settings.setId(rs.getInt("id"));
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

    public boolean deleteRoom(int roomId) {
        String sql = "UPDATE room SET is_active = false WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
            return false;
        }
    }

    public boolean addUserToRoom(Room room, User user) {
        String sql = "INSERT INTO room_users (room_id, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, room.getId());
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
            stmt.setInt(1, room.getId());
            stmt.setLong(2, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing user from room: " + e.getMessage());
            return false;
        }
    }
    public boolean updateRoom(Room updatedRoom) {
        String updateRoomSql = "UPDATE room SET name = ?, is_active = ?, backgroundImage = ? WHERE id = ?";
        String updateSettingsSql = "UPDATE room_settings SET is_public = ?, max_users = ?, allow_media_sharing = ? WHERE room_id = ?";

        try {
            connection.setAutoCommit(false);

            // Update room basic info
            try (PreparedStatement roomStmt = connection.prepareStatement(updateRoomSql)) {
                roomStmt.setString(1, updatedRoom.getName());
                roomStmt.setBoolean(2, updatedRoom.isActive());
                roomStmt.setString(3, updatedRoom.getBackgroundImage());
                roomStmt.setInt(4, updatedRoom.getId());
                int roomUpdated = roomStmt.executeUpdate();

                if (roomUpdated == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // Update room settings
            try (PreparedStatement settingsStmt = connection.prepareStatement(updateSettingsSql)) {
                RoomSettings settings = updatedRoom.getSettings();
                settingsStmt.setBoolean(1, settings.isPublic());
                settingsStmt.setInt(2, settings.getMaxUsers());
                settingsStmt.setBoolean(3, settings.isAllowMediaSharing());
                settingsStmt.setInt(4, updatedRoom.getId());
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

    public Optional<Room> getRoomById(int id) {
        String sql = "SELECT r.*, u.*, rs.* FROM room r " +
                "LEFT JOIN user u ON r.owner_id = u.id " +
                "LEFT JOIN room_settings rs ON r.id = rs.room_id " +
                "WHERE r.id = ? AND r.is_active = true";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("r.id"));
                room.setName(rs.getString("r.name"));
                room.setActive(rs.getBoolean("r.is_active"));
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
                settings.setId(rs.getInt("rs.id"));
                settings.setPublic(rs.getBoolean("rs.is_public"));
                settings.setMaxUsers(rs.getInt("rs.max_users"));
                settings.setAllowMediaSharing(rs.getBoolean("rs.allow_media_sharing"));
                room.setSettings(settings);

                // Load participants
                room.setUsers(getRoomParticipants(id));

                return Optional.of(room);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room by ID: " + e.getMessage());
        }
        return Optional.empty();
    }
}