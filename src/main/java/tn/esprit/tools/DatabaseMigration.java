package tn.esprit.tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class for database migrations
 */
public class DatabaseMigration {
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigration.class.getName());
    
    /**
     * Run a migration SQL script
     * @param scriptPath Path to SQL script in resources folder
     * @return true if migration successful, false otherwise
     */
    public static boolean runMigration(String scriptPath) {
        try (Connection conn = MyDataBase.getInstance().getCnx()) {
            // Load SQL script from resources
            InputStream is = DatabaseMigration.class.getResourceAsStream(scriptPath);
            if (is == null) {
                LOGGER.log(Level.SEVERE, "Migration script not found: {0}", scriptPath);
                return false;
            }
            
            String sqlScript = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
                
            // Execute SQL script
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlScript);
                LOGGER.log(Level.INFO, "Migration executed successfully: {0}", scriptPath);
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running migration: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Run all necessary migrations
     */
    public static void runAllMigrations() {
        LOGGER.info("Running database migrations...");
        
        // Add new migration scripts here
        runMigration("/sql/create_group_members_table.sql");
        runMigration("/sql/update_groupstudent_table.sql");
        runMigration("/sql/create_upcoming_meetings_table.sql");
        
        LOGGER.info("Database migrations completed");
    }
} 