package lk.com.pos.privateclasses;

import lk.com.pos.connection.MySQL;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Notification {
    private static final Logger LOGGER = Logger.getLogger(Notification.class.getName());
    
    public Notification() {
        // Constructor using static MySQL connection
    }
    
    public void checkAllNotifications() {
        try {
            Connection connection = MySQL.getConnection();
            
            // Check low stock
            checkLowStock(connection);
            
            // Check expired products
            checkExpiredProducts(connection);
            
            // Check products expiring soon (within 30 days)
            checkProductsExpiringSoon(connection);
            
            // Check credit due dates
            checkCreditDueDates(connection);
            
            // Check missed credit due dates
            checkMissedCreditDueDates(connection);
            
            // Clear old notifications
            clearOldNotifications();
            
            LOGGER.info("All notification checks completed successfully");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking notifications", e);
        }
    }
    
    // Method to be called whenever stock quantity changes
    public void checkStockQuantityChange(int stockId, int oldQty, int newQty) {
        try {
            Connection connection = MySQL.getConnection();
            
            // Check if stock reached low level (10 or less)
            if (newQty <= 10 && oldQty > 10) {
                insertLowStockNotification(connection, stockId, newQty);
            }
            // Check if stock was replenished from low level
            else if (newQty > 10 && oldQty <= 10) {
                insertStockReplenishedNotification(connection, stockId, newQty);
            }
            // Check if stock reached zero
            else if (newQty == 0 && oldQty > 0) {
                insertOutOfStockNotification(connection, stockId);
            }
            // Check if stock was completely depleted
            else if (newQty == 0 && oldQty > 0) {
                insertOutOfStockNotification(connection, stockId);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking stock quantity change", e);
        }
    }
    
    private void insertLowStockNotification(Connection connection, int stockId, int currentQty) throws SQLException {
        String query = "SELECT p.product_name, s.qty " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.stock_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String productName = rs.getString("product_name");
                String message = "Low stock alert: " + productName + " (" + currentQty + " left)";
                
                if (!isMessageExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                    LOGGER.info("Low stock notification created for: " + productName);
                }
            }
        }
    }
    
    private void insertStockReplenishedNotification(Connection connection, int stockId, int currentQty) throws SQLException {
        String query = "SELECT p.product_name " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.stock_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String productName = rs.getString("product_name");
                String message = "Stock replenished: " + productName + " (now " + currentQty + " in stock)";
                
                if (!isMessageExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                    LOGGER.info("Stock replenished notification created for: " + productName);
                }
            }
        }
    }
    
    private void insertOutOfStockNotification(Connection connection, int stockId) throws SQLException {
        String query = "SELECT p.product_name " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.stock_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String productName = rs.getString("product_name");
                String message = "Out of stock: " + productName + " (0 left)";
                
                if (!isMessageExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                    LOGGER.info("Out of stock notification created for: " + productName);
                }
            }
        }
    }
    
    private void checkLowStock(Connection connection) throws SQLException {
        String query = "SELECT p.product_name, s.qty, s.stock_id " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.qty <= 10 AND s.p_status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("qty");
                String message = "Low stock: " + productName + " (" + quantity + " left)";
                
                // Check if similar message already exists to avoid duplicates
                if (!isMessageExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                }
            }
        }
    }
    
    private void checkExpiredProducts(Connection connection) throws SQLException {
        String query = "SELECT p.product_name, s.batch_no, s.expriy_date " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.expriy_date < CURDATE() AND s.p_status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String productName = rs.getString("product_name");
                String batchNo = rs.getString("batch_no");
                Date expiryDate = rs.getDate("expriy_date");
                String message = "Expired Product: " + productName + " (Batch: " + batchNo + 
                                ", Expired: " + expiryDate + ")";
                
                // Check if similar message already exists to avoid duplicates
                if (!isMessageExists(connection, message, 5)) {
                    insertNotification(connection, message, 5);
                }
            }
        }
    }
    
    private void checkProductsExpiringSoon(Connection connection) throws SQLException {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        String query = "SELECT p.product_name, s.batch_no, s.expriy_date " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.expriy_date BETWEEN CURDATE() AND ? AND s.p_status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(thirtyDaysFromNow));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String productName = rs.getString("product_name");
                String batchNo = rs.getString("batch_no");
                Date expiryDate = rs.getDate("expriy_date");
                long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate.toLocalDate());
                
                String message = "Expiry Alert: " + productName + " (Batch: " + batchNo + 
                               ", expires in " + daysUntilExpiry + " days)";
                
                // Check if similar message already exists to avoid duplicates
                if (!isMessageExists(connection, message, 1)) {
                    insertNotification(connection, message, 1);
                }
            }
        }
    }
    
    private void checkCreditDueDates(Connection connection) throws SQLException {
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        String query = "SELECT cc.customer_name, c.credit_final_date, c.credit_amout " +
                      "FROM credit c " +
                      "JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id " +
                      "WHERE c.credit_final_date BETWEEN CURDATE() AND ? " +
                      "AND cc.status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(sevenDaysFromNow));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String customerName = rs.getString("customer_name");
                Date dueDate = rs.getDate("credit_final_date");
                double amount = rs.getDouble("credit_amout");
                long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate.toLocalDate());
                
                String message = "Credit Due Soon: " + customerName + " (Amount: " + amount + 
                               ", Due in " + daysUntilDue + " days)";
                
                // Check if similar message already exists to avoid duplicates
                if (!isMessageExists(connection, message, 7)) {
                    insertNotification(connection, message, 7);
                }
            }
        }
    }
    
    private void checkMissedCreditDueDates(Connection connection) throws SQLException {
        String query = "SELECT cc.customer_name, c.credit_final_date, c.credit_amout " +
                      "FROM credit c " +
                      "JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id " +
                      "WHERE c.credit_final_date < CURDATE() " +
                      "AND cc.status_id = 1 " +
                      "AND c.credit_amout > 0";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String customerName = rs.getString("customer_name");
                Date dueDate = rs.getDate("credit_final_date");
                double amount = rs.getDouble("credit_amout");
                long daysOverdue = ChronoUnit.DAYS.between(dueDate.toLocalDate(), LocalDate.now());
                
                String message = "Overdue Credit: " + customerName + " (Amount: " + amount + 
                               ", Overdue by " + daysOverdue + " days)";
                
                // Check if similar message already exists to avoid duplicates
                if (!isMessageExists(connection, message, 7)) {
                    insertNotification(connection, message, 7);
                }
            }
        }
    }
    
    private boolean isMessageExists(Connection connection, String message, int msgTypeId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM notifocation n " +
                      "JOIN massage m ON n.massage_id = m.massage_id " +
                      "WHERE m.massage = ? AND n.msg_type_id = ? " +
                      "AND DATE(n.create_at) = CURDATE()";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, message);
            stmt.setInt(2, msgTypeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        }
        return false;
    }
    
    private void insertNotification(Connection connection, String message, int msgTypeId) throws SQLException {
        // First insert into massage table
        int massageId = insertMassage(connection, message);
        
        if (massageId > 0) {
            // Then insert into notifocation table
            String query = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) " +
                         "VALUES (1, NOW(), ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, msgTypeId);
                stmt.setInt(2, massageId);
                stmt.executeUpdate();
            }
        }
    }
    
    private int insertMassage(Connection connection, String message) throws SQLException {
        String query = "INSERT INTO massage (massage) VALUES (?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, message);
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return -1;
    }
    
    // Static methods for external use
    public static ResultSet getUnreadNotifications() throws SQLException {
        String query = "SELECT n.id, m.massage, mt.msg_type, n.create_at " +
                      "FROM notifocation n " +
                      "JOIN massage m ON n.massage_id = m.massage_id " +
                      "JOIN msg_type mt ON n.msg_type_id = mt.msg_type_id " +
                      "WHERE n.is_read = 1 " +
                      "ORDER BY n.create_at DESC";
        
        return MySQL.executeSearch(query);
    }
    
    public static void markAsRead(int notificationId) {
        String query = "UPDATE notifocation SET is_read = 0 WHERE id = " + notificationId;
        MySQL.executeIUD(query);
    }
    
    public static void markAllAsRead() {
        String query = "UPDATE notifocation SET is_read = 0 WHERE is_read = 1";
        MySQL.executeIUD(query);
    }
    
    public static int getUnreadNotificationCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM notifocation WHERE is_read = 1";
        ResultSet rs = MySQL.executeSearch(query);
        
        if (rs.next()) {
            return rs.getInt("count");
        }
        return 0;
    }
    
    public static void clearOldNotifications() {
        String query = "DELETE FROM notifocation WHERE create_at < DATE_SUB(NOW(), INTERVAL 30 DAY)";
        MySQL.executeIUD(query);
    }
}