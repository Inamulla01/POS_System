package lk.com.pos.utils;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * User-friendly error messages instead of technical jargon
 */
public class UserFriendlyMessages {
    
    public static void showError(String technicalMessage) {
        String userMessage = translateError(technicalMessage);
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                userMessage,
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    public static void showSuccess(String message) {
        SwingUtilities.invokeLater(() -> {
            raven.toast.Notifications.getInstance().show(
                raven.toast.Notifications.Type.SUCCESS,
                raven.toast.Notifications.Location.TOP_RIGHT,
                message
            );
        });
    }
    
    public static void showWarning(String message) {
        SwingUtilities.invokeLater(() -> {
            raven.toast.Notifications.getInstance().show(
                raven.toast.Notifications.Type.WARNING,
                raven.toast.Notifications.Location.TOP_RIGHT,
                message
            );
        });
    }
    
    private static String translateError(String technicalMessage) {
        if (technicalMessage == null) {
            return "An error occurred. Please try again.";
        }
        
        String lower = technicalMessage.toLowerCase();
        
        // Database connection errors
        if (lower.contains("connection") || lower.contains("timeout")) {
            return "Cannot connect to database.\n" +
                   "Please check:\n" +
                   "• Internet connection\n" +
                   "• Database server is running\n" +
                   "• Contact IT support if problem persists";
        }
        
        // Duplicate key errors
        if (lower.contains("duplicate") || lower.contains("unique")) {
            return "This record already exists.\n" +
                   "Please check if you already entered this information.";
        }
        
        // Foreign key errors
        if (lower.contains("foreign key") || lower.contains("constraint")) {
            return "Cannot complete this action.\n" +
                   "This item is being used in another record.\n" +
                   "Please remove related records first.";
        }
        
        // Out of stock
        if (lower.contains("stock") || lower.contains("quantity")) {
            return "Insufficient stock available.\n" +
                   "Please check stock levels before continuing.";
        }
        
        // Permission errors
        if (lower.contains("permission") || lower.contains("denied")) {
            return "You don't have permission to do this.\n" +
                   "Please contact your supervisor.";
        }
        
        // SQL syntax errors (shouldn't show to users)
        if (lower.contains("sql") || lower.contains("syntax")) {
            return "System error occurred.\n" +
                   "Error code: " + System.currentTimeMillis() + "\n" +
                   "Please contact IT support with this code.";
        }
        
        // Generic fallback
        return "An error occurred.\n" +
               "If this continues, please contact support.";
    }
    
    /**
     * Confirmation dialog with clear Yes/No
     */
    public static boolean confirm(String message, String title) {
        int result = JOptionPane.showConfirmDialog(
            null,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Loading message
     */
    public static void showLoading(String message) {
        SwingUtilities.invokeLater(() -> {
            raven.toast.Notifications.getInstance().show(
                raven.toast.Notifications.Type.INFO,
                raven.toast.Notifications.Location.TOP_RIGHT,
                "⏳ " + message
            );
        });
    }
}