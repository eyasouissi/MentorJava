package tn.esprit.utils;

import java.awt.Desktop;
import java.net.URI;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Utility class for handling browser-related operations.
 */
public class BrowserUtils {
    
    /**
     * Opens a URL in the default browser.
     * Handles exceptions gracefully and provides feedback if browser could not be opened.
     * 
     * @param url The URL to open
     * @return true if successful, false otherwise
     */
    public static boolean openInDefaultBrowser(String url) {
        try {
            // Check if Desktop is supported
            if (!Desktop.isDesktopSupported()) {
                showError("Browser Error", "Desktop is not supported on this platform.");
                return false;
            }
            
            Desktop desktop = Desktop.getDesktop();
            
            // Check if browsing is supported
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                showError("Browser Error", "Opening URLs is not supported on this platform.");
                return false;
            }
            
            // Create and validate URI
            URI uri = new URI(url);
            
            // Open in browser
            desktop.browse(uri);
            return true;
            
        } catch (Exception e) {
            showError("Browser Error", "Could not open browser: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Shows an error alert dialog with the specified title and message.
     * 
     * @param title The title of the alert
     * @param message The error message to display
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 