package tn.esprit.services;

import tn.esprit.entities.Forum;
import tn.esprit.entities.Post;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumService {
    private Connection cnx;
    
    public ForumService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }
    
    public int getTotalPostCount() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM post";
        
        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting posts: " + e.getMessage());
        }
        
        return count;
    }
    
    public int getTodayPostCount() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM post WHERE DATE(created_at) = CURDATE()";
        
        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting today's posts: " + e.getMessage());
        }
        
        return count;
    }
    
    public Map<LocalDate, Integer> getPostCountByDate() {
        Map<LocalDate, Integer> result = new HashMap<>();
        String query = "SELECT DATE(created_at) as post_date, COUNT(*) as count " +
                       "FROM post " +
                       "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 15 DAY) " +
                       "GROUP BY DATE(created_at) " +
                       "ORDER BY post_date";
        
        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Date date = rs.getDate("post_date");
                int count = rs.getInt("count");
                
                LocalDate localDate = date.toLocalDate();
                result.put(localDate, count);
            }
        } catch (SQLException e) {
            System.err.println("Error getting post counts by date: " + e.getMessage());
        }
        
        return result;
    }
    
    public List<Forum> getMostActiveForums(int limit) {
        List<Forum> forums = new ArrayList<>();
        String query = "SELECT f.*, COUNT(p.id) as post_count " +
                       "FROM forum f " +
                       "JOIN post p ON p.forum_id = f.id " +
                       "GROUP BY f.id " +
                       "ORDER BY post_count DESC " +
                       "LIMIT ?";
        
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Forum forum = new Forum();
                    forum.setId(rs.getLong("id"));
                    forum.setTitle(rs.getString("title"));
                    forum.setDescription(rs.getString("description"));
                    forum.setIsPublic(rs.getBoolean("is_public"));
                    forum.setViews(rs.getInt("views"));
                    forum.setTotalPosts(rs.getInt("post_count"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        forum.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        forum.setUpdatedAt(updatedAt.toLocalDateTime());
                    }
                    
                    forums.add(forum);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting most active forums: " + e.getMessage());
        }
        
        return forums;
    }
} 