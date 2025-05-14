package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MyDataBase {
    // Enhanced connection parameters
    private final String URL = "jdbc:mysql://localhost:3306/mentordb"
            + "?autoReconnect=true"
            + "&useSSL=false"
            + "&serverTimezone=UTC"
            + "&maxReconnects=10"
            + "&initialTimeout=2"
            + "&socketTimeout=0" // 0 = no timeout
            + "&connectTimeout=30000"
            + "&zeroDateTimeBehavior=convertToNull"
            + "&allowPublicKeyRetrieval=true";

    private final String USER = "root";
    private final String PWD = "";

    private volatile Connection cnx;
    private static MyDataBase instance;
    private boolean keepAliveRunning = true;

    private MyDataBase() {
        createConnection();
        startKeepAlive();
        addShutdownHook();
    }

    private void createConnection() {
        int maxRetries = 5;
        int retryDelay = 2000; // 2 seconds

        for (int i = 0; i < maxRetries; i++) {
            try {
                if (cnx != null && !cnx.isClosed()) {
                    cnx.close();
                }
                cnx = DriverManager.getConnection(URL, USER, PWD);
                cnx.setAutoCommit(true); // Enable auto-commit
                System.out.println("Connection established");
                return;
            } catch (SQLException e) {
                System.err.println("Connection attempt " + (i + 1) + " failed: " + e.getMessage());
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw new RuntimeException("Failed to establish database connection after " + maxRetries + " attempts");
    }

    private void startKeepAlive() {
        Thread keepAliveThread = new Thread(() -> {
            while (keepAliveRunning) {
                try {
                    Thread.sleep(300000); // Ping every 5 minutes
                    if (cnx != null && !cnx.isClosed()) {
                        try (PreparedStatement ps = cnx.prepareStatement("SELECT 1")) {
                            ps.executeQuery();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (SQLException e) {
                    System.err.println("Keep-alive failed, reconnecting...");
                    createConnection();
                }
            }
        });
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            keepAliveRunning = false;
            if (cnx != null) {
                try {
                    if (!cnx.isClosed()) {
                        cnx.close();
                        System.out.println("Database connection closed gracefully");
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }));
    }

    public static synchronized MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed() || !cnx.isValid(5)) {
                System.out.println("Connection invalid, reconnecting...");
                createConnection();
            }
        } catch (SQLException e) {
            System.err.println("Connection validation failed: " + e.getMessage());
            createConnection();
        }
        return cnx;
    }

    // Example method to update forum view counts
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
