package tn.esprit.services;

import tn.esprit.entities.Notification;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;

public class NotificationRepository {
    
    public void save(Notification notification) {
        String sql = "INSERT INTO notification (sender_id, recipient_id, post_id, type, message, created_at, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, notification.getSender().getId());
            pstmt.setLong(2, notification.getRecipient().getId());
            pstmt.setInt(3, notification.getPost().getId());
            pstmt.setString(4, notification.getType());
            pstmt.setString(5, notification.getMessage());
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setBoolean(7, false);
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
