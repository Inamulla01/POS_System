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
        // Constructor
    }
    
    public void checkAllNotifications() {
        Connection connection = null;
        try {
            connection = MySQL.getConnection();
            
            // Check low stock (only active stocks)
            checkLowStock(connection);
            
            // Check expired products (only active stocks)
            checkExpiredProducts(connection);
            
            // Check products expiring soon (only active stocks)
            checkProductsExpiringSoon(connection);
            
            // Check credit due dates
            checkCreditDueDates(connection);
            
            // Check missed credit due dates
            checkMissedCreditDueDates(connection);
            
            LOGGER.info("All notification checks completed successfully");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking notifications", e);
        } finally {
            closeConnection(connection);
        }
    }
    
    // Method to be called whenever stock quantity changes (like after sale)
    public void checkStockAfterSale(int stockId, int newQty) {
        Connection connection = null;
        try {
            connection = MySQL.getConnection();
            
            // First check if stock is active
            if (!isStockActive(connection, stockId)) {
                LOGGER.info("Stock ID " + stockId + " is inactive, skipping notification check");
                return;
            }
            
            // Check if stock reached low level (10 or less) and is not zero
            if (newQty <= 10 && newQty > 0) {
                insertLowStockNotification(connection, stockId, newQty);
            }
            // Check if stock reached zero
            else if (newQty == 0) {
                insertOutOfStockNotification(connection, stockId);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking stock after sale", e);
        } finally {
            closeConnection(connection);
        }
    }
    
    // Method to check stock quantity changes
    public void checkStockQuantityChange(int stockId, int oldQty, int newQty) {
        Connection connection = null;
        try {
            connection = MySQL.getConnection();
            
            // First check if stock is active
            if (!isStockActive(connection, stockId)) {
                LOGGER.info("Stock ID " + stockId + " is inactive, skipping notification check");
                return;
            }
            
            // Check if stock reached low level (10 or less) and is not zero
            if (newQty <= 10 && newQty > 0 && oldQty > 10) {
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
            // Check if stock was replenished from zero
            else if (newQty > 0 && oldQty == 0) {
                insertStockReplenishedFromZeroNotification(connection, stockId, newQty);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking stock quantity change", e);
        } finally {
            closeConnection(connection);
        }
    }
    
    // Helper method to safely close connection
    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }
    
    // ✅ FIXED: Helper method to check if stock is active
    private boolean isStockActive(Connection connection, int stockId) throws SQLException {
        String query = "SELECT p_status_id FROM stock WHERE stock_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, stockId);  // ✅ Set parameter FIRST
            
            try (ResultSet rs = stmt.executeQuery()) {  // ✅ Then execute query
                if (rs.next()) {
                    int statusId = rs.getInt("p_status_id");
                    return statusId == 1; // 1 = Active, 2 = Inactive
                }
            }
        }
        return false;
    }
    
    // ✅ FIXED: Helper method to check if product is active
    private boolean isProductActive(Connection connection, int productId) throws SQLException {
        String query = "SELECT p_status_id FROM product WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);  // ✅ Set parameter FIRST
            
            try (ResultSet rs = stmt.executeQuery()) {  // ✅ Then execute query
                if (rs.next()) {
                    int statusId = rs.getInt("p_status_id");
                    return statusId == 1; // 1 = Active, 2 = Inactive
                }
            }
        }
        return false;
    }
    
    private void insertLowStockNotification(Connection connection, int stockId, int currentQty) throws SQLException {
        String query = "SELECT p.product_name, s.qty, p.product_id " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.stock_id = ? AND s.p_status_id = 1 AND p.p_status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String productName = rs.getString("product_name");
                String message = "Low stock alert: " + productName + " (" + currentQty + " left)";
                
                if (!isNotificationExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                    LOGGER.info("Low stock notification inserted for: " + productName);
                }
            }
            rs.close();
        }
    }
    
    private void insertStockReplenishedNotification(Connection connection, int stockId, int currentQty) throws SQLException {
        String query = "SELECT p.product_name " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.stock_id = ? AND s.p_status_id = 1 AND p.p_status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String productName = rs.getString("product_name");
                String message = "Stock replenished: " + productName + " (now " + currentQty + " in stock)";
                
                if (!isNotificationExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                    LOGGER.info("Stock replenished notification inserted for: " + productName);
                }
            }
            rs.close();
        }
    }
    
    private void insertStockReplenishedFromZeroNotification(Connection connection, int stockId, int currentQty) throws SQLException {
        String query = "SELECT p.product_name " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.stock_id = ? AND s.p_status_id = 1 AND p.p_status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String productName = rs.getString("product_name");
                String message = "Stock back in stock: " + productName + " (now " + currentQty + " in stock)";
                
                if (!isNotificationExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                    LOGGER.info("Stock back in stock notification inserted for: " + productName);
                }
            }
            rs.close();
        }
    }
    
    private void insertOutOfStockNotification(Connection connection, int stockId) throws SQLException {
        String query = "SELECT p.product_name " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.stock_id = ? AND s.p_status_id = 1 AND p.p_status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String productName = rs.getString("product_name");
                String message = "Out of stock: " + productName + " (0 left)";
                
                if (!isNotificationExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                    LOGGER.info("Out of stock notification inserted for: " + productName);
                }
            }
            rs.close();
        }
    }
    
    private void checkLowStock(Connection connection) throws SQLException {
        String query = "SELECT p.product_name, s.qty, s.stock_id " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.qty <= 10 AND s.qty > 0 AND s.p_status_id = 1 AND p.p_status_id = 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("qty");
                String message = "Low stock: " + productName + " (" + quantity + " left)";
                
                // Check if similar message already exists to avoid duplicates
                if (!isNotificationExists(connection, message, 2)) {
                    insertNotification(connection, message, 2);
                }
            }
        }
    }
    
    private void checkExpiredProducts(Connection connection) throws SQLException {
        String query = "SELECT p.product_name, s.batch_no, s.expriy_date, s.qty " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.expriy_date < CURDATE() AND s.p_status_id = 1 AND p.p_status_id = 1 AND s.qty > 0";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String productName = rs.getString("product_name");
                String batchNo = rs.getString("batch_no");
                Date expiryDate = rs.getDate("expriy_date");
                int quantity = rs.getInt("qty");
                
                String message = "Expired Product: " + productName + " (Batch: " + batchNo + 
                                ", Expired: " + expiryDate + ", Qty: " + quantity + ")";
                
                // Check if similar message already exists to avoid duplicates
                if (!isNotificationExists(connection, message, 5)) {
                    insertNotification(connection, message, 5);
                }
            }
        }
    }
    
    private void checkProductsExpiringSoon(Connection connection) throws SQLException {
        LocalDate tenDaysFromNow = LocalDate.now().plusDays(10);
        String query = "SELECT p.product_name, s.batch_no, s.expriy_date, s.qty " +
                      "FROM stock s " +
                      "JOIN product p ON s.product_id = p.product_id " +
                      "WHERE s.expriy_date BETWEEN CURDATE() AND ? " +
                      "AND s.p_status_id = 1 AND p.p_status_id = 1 AND s.qty > 0";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(tenDaysFromNow));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String productName = rs.getString("product_name");
                String batchNo = rs.getString("batch_no");
                Date expiryDate = rs.getDate("expriy_date");
                int quantity = rs.getInt("qty");
                long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate.toLocalDate());
                
                String message = "Expiry Alert: " + productName + " (Batch: " + batchNo + 
                               ", expires in " + daysUntilExpiry + " days, Qty: " + quantity + ")";
                
                // Check if similar message already exists to avoid duplicates
                if (!isNotificationExists(connection, message, 1)) {
                    insertNotification(connection, message, 1);
                }
            }
            rs.close();
        }
    }
    
    private void checkCreditDueDates(Connection connection) throws SQLException {
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        String query = "SELECT cc.customer_name, c.credit_final_date, c.credit_amout " +
                      "FROM credit c " +
                      "JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id " +
                      "WHERE c.credit_final_date BETWEEN CURDATE() AND ? " +
                      "AND cc.status_id = 1 AND c.credit_amout > 0";
        
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
                if (!isNotificationExists(connection, message, 7)) {
                    insertNotification(connection, message, 7);
                }
            }
            rs.close();
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
                if (!isNotificationExists(connection, message, 7)) {
                    insertNotification(connection, message, 7);
                }
            }
        }
    }
    
    private boolean isNotificationExists(Connection connection, String message, int msgTypeId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM notifocation n " +
                      "JOIN massage m ON n.massage_id = m.massage_id " +
                      "WHERE m.massage = ? AND n.msg_type_id = ? " +
                      "AND DATE(n.create_at) = CURDATE()";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, message);
            stmt.setInt(2, msgTypeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("count");
                rs.close();
                return count > 0;
            }
            rs.close();
        }
        return false;
    }
    
    private int getOrCreateMassageId(Connection connection, String message) throws SQLException {
        // First try to find existing message
        String findQuery = "SELECT massage_id FROM massage WHERE massage = ?";
        try (PreparedStatement stmt = connection.prepareStatement(findQuery)) {
            stmt.setString(1, message);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("massage_id");
                rs.close();
                return id;
            }
            rs.close();
        }
        
        // If not exists, insert new message
        String insertQuery = "INSERT INTO massage (massage) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, message);
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                generatedKeys.close();
                return id;
            }
            generatedKeys.close();
        }
        return -1;
    }
    
    private void insertNotification(Connection connection, String message, int msgTypeId) throws SQLException {
        // Get or create message ID
        int massageId = getOrCreateMassageId(connection, message);
        
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
    
    // Method to check expired products every 6 hours
    public void checkExpiredProductsEvery6Hours() {
        Connection connection = null;
        try {
            connection = MySQL.getConnection();
            checkExpiredProducts(connection);
            
            // Also check products expiring soon
            checkProductsExpiringSoon(connection);
            
            LOGGER.info("Expired products check completed (6-hour interval)");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking expired products every 6 hours", e);
        } finally {
            closeConnection(connection);
        }
    }
    
    // Method to check all periodic notifications every 12 hours
    public void checkPeriodicNotificationsEvery12Hours() {
        Connection connection = null;
        try {
            connection = MySQL.getConnection();
            
            // Check low stock (only active stocks with quantity > 0)
            checkLowStock(connection);
            
            // Check products expiring soon (only active stocks with quantity > 0)
            checkProductsExpiringSoon(connection);
            
            // Check credit due dates
            checkCreditDueDates(connection);
            
            // Check missed credit due dates
            checkMissedCreditDueDates(connection);
            
            LOGGER.info("Periodic notifications check completed (12-hour interval)");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking periodic notifications every 12 hours", e);
        } finally {
            closeConnection(connection);
        }
    }
    
    // Method to check every half hour (for frequent checks)
    public void checkFrequentNotificationsEveryHalfHour() {
        Connection connection = null;
        try {
            connection = MySQL.getConnection();
            
            // Check critical notifications more frequently (only active stocks)
            checkLowStock(connection);
            checkExpiredProducts(connection);
            
            LOGGER.info("Frequent notifications check completed (30-minute interval)");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking frequent notifications every half hour", e);
        } finally {
            closeConnection(connection);
        }
    }
    
    // Static methods for external use - FIXED to properly close resources
    public static ResultSet getUnreadNotifications() throws SQLException {
        String query = "SELECT n.id, m.massage, mt.msg_type, n.create_at " +
                      "FROM notifocation n " +
                      "JOIN massage m ON n.massage_id = m.massage_id " +
                      "JOIN msg_type mt ON n.msg_type_id = mt.msg_type_id " +
                      "WHERE n.is_read = 1 " +
                      "ORDER BY n.create_at DESC";
        
        // WARNING: The caller MUST close this ResultSet and its Statement/Connection
        return MySQL.executeSearch(query);
    }
    
    public static void markAsRead(int notificationId) throws SQLException {
        String query = "UPDATE notifocation SET is_read = 0 WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = MySQL.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } finally {
            MySQL.close(null, stmt, conn);
        }
    }
    
    public static void markAllAsRead() throws SQLException {
        String query = "UPDATE notifocation SET is_read = 0 WHERE is_read = 1";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = MySQL.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
        } finally {
            MySQL.close(null, stmt, conn);
        }
    }
    
    public static int getUnreadNotificationCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM notifocation WHERE is_read = 1";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = MySQL.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        } finally {
            MySQL.close(rs, stmt, conn);
        }
    }
    
    public static void clearOldNotifications() throws SQLException {
        String query = "DELETE FROM notifocation WHERE create_at < DATE_SUB(NOW(), INTERVAL 30 DAY)";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = MySQL.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            LOGGER.info("Old notifications cleared (older than 30 days)");
        } finally {
            MySQL.close(null, stmt, conn);
        }
    }
    
    // Method to delete all notifications
    public static void deleteAllNotifications() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        
        try {
            conn = MySQL.getConnection();
            
            // First delete notifications
            String query1 = "DELETE FROM notifocation";
            stmt1 = conn.prepareStatement(query1);
            stmt1.executeUpdate();
            
            // Then messages that are not used
            String query2 = "DELETE FROM massage WHERE massage_id NOT IN (SELECT DISTINCT massage_id FROM notifocation)";
            stmt2 = conn.prepareStatement(query2);
            stmt2.executeUpdate();
            
            LOGGER.info("All notifications deleted");
        } finally {
            if (stmt1 != null) stmt1.close();
            if (stmt2 != null) stmt2.close();
            if (conn != null) conn.close();
        }
    }
}