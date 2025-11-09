package lk.com.pos.service;

import lk.com.pos.connection.MySQL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class NotificationService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    static {
        initializeNotificationService();
        scheduleDailyNotifications();
    }
    
    public static void generateProductNotifications() {
        System.out.println("Generating product notifications...");
        
        // Use a set to track processed products to avoid duplicates
        Set<String> processedNotifications = new HashSet<>();
        
        checkExpiredProducts(processedNotifications);
        checkExpiringSoonProducts(processedNotifications);
        checkLowStockProducts(processedNotifications);
        
        updateLastCheckDate();
    }
    
    private static void checkExpiredProducts(Set<String> processedNotifications) {
        String query = "SELECT p.product_id, p.product_name, s.expiry_date, s.batch_no, s.qty " +
                      "FROM product p " +
                      "JOIN stock s ON p.product_id = s.product_id " +
                      "WHERE s.expiry_date < CURDATE() " +
                      "AND p.p_status_id = 1 " + // Only active products
                      "AND s.qty > 0"; // Only if there's stock
        
        try (Connection conn = MySQL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                String expiryDate = rs.getString("expiry_date");
                String batchNo = rs.getString("batch_no");
                int quantity = rs.getInt("qty");
                
                String notificationKey = "expired_" + productId + "_" + batchNo;
                
                if (!processedNotifications.contains(notificationKey) && !isDuplicateNotification(productName, "Expired")) {
                    String message = String.format("EXPIRED: %s (Batch: %s) expired on %s. Quantity: %d units", 
                        productName, batchNo, expiryDate, quantity);
                    
                    createNotification(message, 6); // msg_type_id 6 for Expired
                    processedNotifications.add(notificationKey);
                    System.out.println("Created expired notification: " + productName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking expired products: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkExpiringSoonProducts(Set<String> processedNotifications) {
        LocalDate threeMonthsLater = LocalDate.now().plusMonths(3);
        String threeMonthsLaterStr = threeMonthsLater.format(DATE_FORMATTER);
        
        String query = "SELECT p.product_id, p.product_name, s.expiry_date, s.batch_no, s.qty " +
                      "FROM product p " +
                      "JOIN stock s ON p.product_id = s.product_id " +
                      "WHERE s.expiry_date BETWEEN CURDATE() AND ? " +
                      "AND p.p_status_id = 1 " + // Only active products
                      "AND s.qty > 0"; // Only if there's stock
        
        try (Connection conn = MySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, threeMonthsLaterStr);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                String expiryDate = rs.getString("expiry_date");
                String batchNo = rs.getString("batch_no");
                int quantity = rs.getInt("qty");
                
                String notificationKey = "expiring_" + productId + "_" + batchNo;
                
                if (!processedNotifications.contains(notificationKey) && !isDuplicateNotification(productName, "Expiring Soon")) {
                    String message = String.format("EXPIRING SOON: %s (Batch: %s) expires on %s. Quantity: %d units", 
                        productName, batchNo, expiryDate, quantity);
                    
                    createNotification(message, 5); // msg_type_id 5 for Expiring Soon
                    processedNotifications.add(notificationKey);
                    System.out.println("Created expiring soon notification: " + productName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking expiring soon products: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkLowStockProducts(Set<String> processedNotifications) {
        String query = "SELECT p.product_id, p.product_name, s.qty, s.batch_no " +
                      "FROM product p " +
                      "JOIN stock s ON p.product_id = s.product_id " +
                      "WHERE s.qty < 10 " +
                      "AND s.qty > 0 " + // Only if there's stock
                      "AND p.p_status_id = 1 " + // Only active products
                      "GROUP BY p.product_id, s.batch_no";
        
        try (Connection conn = MySQL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("qty");
                String batchNo = rs.getString("batch_no");
                
                String notificationKey = "lowstock_" + productId + "_" + batchNo;
                
                if (!processedNotifications.contains(notificationKey) && !isDuplicateNotification(productName, "Low Stock")) {
                    String message = String.format("LOW STOCK: %s (Batch: %s) has only %d units left", 
                        productName, batchNo, quantity);
                    
                    createNotification(message, 4); // msg_type_id 4 for Low Stock
                    processedNotifications.add(notificationKey);
                    System.out.println("Created low stock notification: " + productName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking low stock products: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void createProductInactiveNotification(String productName, String reason) {
        String message = String.format("PRODUCT INACTIVE: %s has been deactivated. Reason: %s", 
            productName, reason);
        
        if (!isDuplicateNotification(productName, "Product Inactive")) {
            createNotification(message, 7); // msg_type_id 7 for Product Inactive
            System.out.println("Created product inactive notification: " + productName);
        }
    }
    
    public static void createProductReactivatedNotification(String productName) {
        String message = String.format("PRODUCT REACTIVATED: %s has been activated again", productName);
        
        if (!isDuplicateNotification(productName, "Product Reactivated")) {
            createNotification(message, 8); // msg_type_id 8 for Product Reactivated
            System.out.println("Created product reactivated notification: " + productName);
        }
    }
    
    public static void createProductUpdatedNotification(String productName, String updatedFields) {
        String message = String.format("PRODUCT UPDATED: %s has been modified. Changes: %s", 
            productName, updatedFields);
        
        if (!isDuplicateNotification(productName, "Updated Product")) {
            createNotification(message, 9); // msg_type_id 9 for Updated Product
            System.out.println("Created product updated notification: " + productName);
        }
    }
    
    private static boolean isDuplicateNotification(String productName, String type) {
        String checkQuery = "SELECT COUNT(*) FROM notification n " +
                           "JOIN message m ON n.message_id = m.message_id " +
                           "JOIN msg_type mt ON n.msg_type_id = mt.msg_type_id " +
                           "WHERE m.message LIKE ? AND mt.msg_type = ? AND DATE(n.created_at) = CURDATE()";
        
        try (Connection conn = MySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            
            pstmt.setString(1, "%" + productName + "%");
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking duplicate notification: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    private static void createNotification(String message, int msgTypeId) {
        Connection conn = null;
        try {
            conn = MySQL.getConnection();
            conn.setAutoCommit(false);
            
            // First insert into message table
            String insertMessage = "INSERT INTO message (message) VALUES (?)";
            int messageId;
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertMessage, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, message);
                pstmt.executeUpdate();
                
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    messageId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get message_id");
                }
            }
            
            // Then insert into notification table
            String insertNotif = "INSERT INTO notification (is_read, created_at, msg_type_id, message_id) " +
                               "VALUES (0, NOW(), ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertNotif)) {
                pstmt.setInt(1, msgTypeId);
                pstmt.setInt(2, messageId);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void updateLastCheckDate() {
        String updateQuery = "UPDATE notification_service SET last_check_date = CURDATE() WHERE id = 1";
        
        try (Connection conn = MySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            // Create table if it doesn't exist
            if (e.getMessage().contains("notification_service")) {
                createNotificationServiceTable();
                updateLastCheckDate(); // Retry
            } else {
                System.err.println("Error updating last check date: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private static void createNotificationServiceTable() {
        String createTable = "CREATE TABLE IF NOT EXISTS notification_service (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "last_check_date DATE NOT NULL" +
                            ")";
        String insertInitial = "INSERT IGNORE INTO notification_service (id, last_check_date) VALUES (1, CURDATE())";
        
        try (Connection conn = MySQL.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createTable);
            stmt.execute(insertInitial);
            System.out.println("Created notification_service table");
            
        } catch (SQLException e) {
            System.err.println("Error creating notification_service table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void initializeNotificationService() {
        createNotificationServiceTable();
        createNotificationTables();
    }
    
    private static void createNotificationTables() {
        String[] createTables = {
            "CREATE TABLE IF NOT EXISTS message (" +
            "message_id INT PRIMARY KEY AUTO_INCREMENT, " +
            "message TEXT NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            "CREATE TABLE IF NOT EXISTS msg_type (" +
            "msg_type_id INT PRIMARY KEY AUTO_INCREMENT, " +
            "msg_type VARCHAR(50) NOT NULL UNIQUE" +
            ")",
            
            "CREATE TABLE IF NOT EXISTS notification (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "is_read TINYINT(1) DEFAULT 0, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "msg_type_id INT, " +
            "message_id INT, " +
            "FOREIGN KEY (msg_type_id) REFERENCES msg_type(msg_type_id), " +
            "FOREIGN KEY (message_id) REFERENCES message(message_id)" +
            ")"
        };
        
        String[] insertMsgTypes = {
            "INSERT IGNORE INTO msg_type (msg_type_id, msg_type) VALUES (4, 'Low Stock')",
            "INSERT IGNORE INTO msg_type (msg_type_id, msg_type) VALUES (5, 'Expiring Soon')",
            "INSERT IGNORE INTO msg_type (msg_type_id, msg_type) VALUES (6, 'Expired')",
            "INSERT IGNORE INTO msg_type (msg_type_id, msg_type) VALUES (7, 'Product Inactive')",
            "INSERT IGNORE INTO msg_type (msg_type_id, msg_type) VALUES (8, 'Product Reactivated')",
            "INSERT IGNORE INTO msg_type (msg_type_id, msg_type) VALUES (9, 'Updated Product')"
        };
        
        try (Connection conn = MySQL.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create tables
            for (String createTable : createTables) {
                try {
                    stmt.execute(createTable);
                } catch (SQLException e) {
                    System.err.println("Error creating table: " + e.getMessage());
                }
            }
            
            // Insert message types
            for (String insertMsgType : insertMsgTypes) {
                try {
                    stmt.execute(insertMsgType);
                } catch (SQLException e) {
                    // Ignore duplicates
                }
            }
            
            System.out.println("Notification tables initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Error initializing notification tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean shouldRunDailyCheck() {
        String query = "SELECT last_check_date FROM notification_service WHERE id = 1";
        
        try (Connection conn = MySQL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                Date lastCheck = rs.getDate("last_check_date");
                return !lastCheck.toLocalDate().equals(LocalDate.now());
            }
        } catch (SQLException e) {
            System.err.println("Error checking last run date: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
    
    private static void scheduleDailyNotifications() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (shouldRunDailyCheck()) {
                    generateProductNotifications();
                }
            }
        }, getInitialDelay(), TimeUnit.DAYS.toMillis(1));
        
        System.out.println("Daily notification scheduler started");
    }
    
    private static long getInitialDelay() {
        LocalDate now = LocalDate.now();
        LocalDate nextRun = now.atTime(8, 0).toLocalDate(); // Run at 8:00 AM
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        return java.time.Duration.between(java.time.LocalDateTime.now(), nextRun.atTime(8, 0)).toMillis();
    }
    
    // Method to clear old notifications (optional, for maintenance)
    public static void clearOldNotifications(int daysOld) {
        String deleteQuery = "DELETE n FROM notification n " +
                           "JOIN message m ON n.message_id = m.message_id " +
                           "WHERE n.created_at < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        try (Connection conn = MySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            
            pstmt.setInt(1, daysOld);
            int deletedCount = pstmt.executeUpdate();
            System.out.println("Cleared " + deletedCount + " old notifications");
            
        } catch (SQLException e) {
            System.err.println("Error clearing old notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Manual trigger method
    public static void manuallyGenerateNotifications() {
        generateProductNotifications();
    }
}