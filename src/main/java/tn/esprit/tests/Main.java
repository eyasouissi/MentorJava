package tn.esprit.tests;

import tn.esprit.tools.MyDataBase;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        // Test database connection
        try {
            // This will trigger the connection
            MyDataBase db = MyDataBase.getInstance();
            Connection cnx = db.getCnx();

            if (cnx != null && !cnx.isClosed()) {
                System.out.println("✅ Database connection SUCCESS!");

                // Optional: Test a simple query
                System.out.println("\nTesting a simple query...");
                testSimpleQuery(cnx);
            }
        } catch (Exception e) {
            System.err.println("❌ Connection FAILED!");
            e.printStackTrace();
        }
    }

    private static void testSimpleQuery(Connection cnx) {
        try (Statement stmt = cnx.createStatement()) {
            // Try querying the database version
            ResultSet rs = stmt.executeQuery("SELECT VERSION()");
            if (rs.next()) {
                System.out.println("MySQL Version: " + rs.getString(1));
            }

            // Try listing tables (optional)
            System.out.println("\nListing tables in your database:");
            rs = stmt.executeQuery("SHOW TABLES");
            while (rs.next()) {
                System.out.println("- " + rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("Query failed: " + e.getMessage());
        }
    }
}