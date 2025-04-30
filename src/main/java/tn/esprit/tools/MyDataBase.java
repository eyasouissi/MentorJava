package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MyDataBase {

    // Database connection parameters
    private final String URL = "jdbc:mysql://localhost:3306/mentordb"
            + "?autoReconnect=true"
            + "&useSSL=false"
            + "&serverTimezone=UTC"
            + "&maxReconnects=5";
    private final String USER = "root";
    private final String PWD = "";

    private Connection cnx;
    private static MyDataBase instance;

    // Private constructor to ensure Singleton pattern
    private MyDataBase() {
        createConnection();
    }

    // Create and establish connection
    private void createConnection() {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = DriverManager.getConnection(URL, USER, PWD);
                System.out.println("Connection established");
            }
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    // Singleton pattern to get the instance of MyDataBase
    public static synchronized MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    // Get the connection, ensure it's valid before returning
    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed() || !cnx.isValid(2)) {
                createConnection(); // Reconnect if necessary
            }
        } catch (SQLException e) {
            System.err.println("Connection validation failed: " + e.getMessage());
        }
        return cnx;
    }

    // Method to update the view count for a forum
    public void updateForumViews(Long forumId, int newViewCount) {
        String sql = "UPDATE forum SET views = ? WHERE id = ?";

        try (PreparedStatement pstmt = getCnx().prepareStatement(sql)) {
            pstmt.setInt(1, newViewCount);
            pstmt.setLong(2, forumId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("View count updated for forum with ID: " + forumId);
            } else {
                System.out.println("No forum found with ID: " + forumId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
