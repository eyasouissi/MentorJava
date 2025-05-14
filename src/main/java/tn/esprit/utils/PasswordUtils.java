package tn.esprit.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 * Uses $2y$ prefix format (compatible with Symfony) and a cost factor of 13.
 */
public class PasswordUtils {

    private static final int COST_FACTOR = 13;
    private static final String PREFIX = "$2y$"; // For Symfony compatibility

    /**
     * Hashes a password using BCrypt with $2y$ format (Symfony compatible)
     * 
     * @param password The plain text password to hash
     * @return The hashed password
     */
    public static String hashPassword(String password) {
        // Generate a salt with the cost factor
        String salt = BCrypt.gensalt(COST_FACTOR);
        
        // Replace the prefix to ensure Symfony compatibility
        salt = salt.replace("$2a$", PREFIX);
        
        // Hash the password with the modified salt
        return BCrypt.hashpw(password, salt);
    }

    /**
     * Verifies a password against a hashed password
     * 
     * @param plainPassword The plain text password to check
     * @param hashedPassword The hashed password to check against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
} 