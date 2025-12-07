package lk.com.pos.dao;

import lk.com.pos.dto.StockLossDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockLossDAO {
    
    // Get all stock losses with filters
    public List<StockLossDTO> getAllStockLosses(String searchText, String timePeriod, String reasonFilter) {
        List<StockLossDTO> losses = new ArrayList<>();
        
        try {
            // Build query and parameters
            StringBuilder queryBuilder = new StringBuilder();
            List<Object> params = new ArrayList<>();
            
            queryBuilder.append("SELECT sl.stock_loss_id, sl.qty, sl.stock_loss_date, ");
            queryBuilder.append("p.product_name, s.batch_no, s.selling_price, ");
            queryBuilder.append("u.name AS user_name, rr.reason, ");
            queryBuilder.append("IFNULL(sa.sales_id, 'N/A') AS invoice_no ");
            queryBuilder.append("FROM stock_loss sl ");
            queryBuilder.append("INNER JOIN stock s ON sl.stock_id = s.stock_id ");
            queryBuilder.append("INNER JOIN product p ON s.product_id = p.product_id ");
            queryBuilder.append("INNER JOIN user u ON sl.user_id = u.user_id ");
            queryBuilder.append("INNER JOIN return_reason rr ON sl.return_reason_id = rr.return_reason_id ");
            queryBuilder.append("LEFT JOIN sales sa ON sl.sales_id = sa.sales_id ");
            queryBuilder.append("WHERE 1=1 ");
            
            // Add search filter
            if (searchText != null && !searchText.trim().isEmpty()) {
                queryBuilder.append("AND (p.product_name LIKE ? OR s.batch_no LIKE ? OR sa.sales_id LIKE ?) ");
                String searchPattern = "%" + searchText.trim() + "%";
                params.add(searchPattern);
                params.add(searchPattern);
                params.add(searchPattern);
            }
            
            // Add date filter
            String dateCondition = buildDateCondition(timePeriod);
            if (!dateCondition.isEmpty()) {
                queryBuilder.append(dateCondition);
            }
            
            // Add reason filter
            if (reasonFilter != null && !reasonFilter.equals("All Reasons")) {
                queryBuilder.append("AND rr.reason LIKE ? ");
                params.add("%" + reasonFilter + "%");
            }
            
            queryBuilder.append("ORDER BY sl.stock_loss_date DESC, sl.stock_loss_id DESC");
            
            String query = queryBuilder.toString();
            
            losses = DB.executeQuerySafe(query, rs -> {
                List<StockLossDTO> result = new ArrayList<>();
                while (rs.next()) {
                    StockLossDTO dto = new StockLossDTO(
                        rs.getInt("stock_loss_id"),
                        rs.getInt("qty"),
                        rs.getTimestamp("stock_loss_date"),
                        rs.getString("product_name"),
                        rs.getString("batch_no"),
                        rs.getDouble("selling_price"),
                        rs.getString("user_name"),
                        rs.getString("reason"),
                        rs.getString("invoice_no")
                    );
                    result.add(dto);
                }
                return result;
            }, params.toArray());
            
        } catch (SQLException e) {
            System.err.println("Error fetching stock losses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return losses;
    }
    
    // Helper method to build date condition
    private String buildDateCondition(String timePeriod) {
        if (timePeriod == null || timePeriod.equals("All Time")) {
            return "";
        }
        
        switch (timePeriod) {
            case "Today":
                return "AND DATE(sl.stock_loss_date) = CURDATE() ";
            case "Last 7 Days":
                return "AND sl.stock_loss_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ";
            case "Last 30 Days":
                return "AND sl.stock_loss_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) ";
            case "Last 90 Days":
                return "AND sl.stock_loss_date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY) ";
            case "1 Year":
                return "AND sl.stock_loss_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) ";
            case "2 Years":
                return "AND sl.stock_loss_date >= DATE_SUB(CURDATE(), INTERVAL 2 YEAR) ";
            case "5 Years":
                return "AND sl.stock_loss_date >= DATE_SUB(CURDATE(), INTERVAL 5 YEAR) ";
            case "10 Years":
                return "AND sl.stock_loss_date >= DATE_SUB(CURDATE(), INTERVAL 10 YEAR) ";
            case "20 Years":
                return "AND sl.stock_loss_date >= DATE_SUB(CURDATE(), INTERVAL 20 YEAR) ";
            default:
                return "";
        }
    }
    
    // Rest of the DAO methods remain the same...
    public StockLossDTO getStockLossById(int lossId) {
        try {
            String query = "SELECT sl.stock_loss_id, sl.qty, sl.stock_loss_date, " +
                          "p.product_name, s.batch_no, s.selling_price, " +
                          "u.name AS user_name, rr.reason, " +
                          "IFNULL(sa.sales_id, 'N/A') AS invoice_no " +
                          "FROM stock_loss sl " +
                          "INNER JOIN stock s ON sl.stock_id = s.stock_id " +
                          "INNER JOIN product p ON s.product_id = p.product_id " +
                          "INNER JOIN user u ON sl.user_id = u.user_id " +
                          "INNER JOIN return_reason rr ON sl.return_reason_id = rr.return_reason_id " +
                          "LEFT JOIN sales sa ON sl.sales_id = sa.sales_id " +
                          "WHERE sl.stock_loss_id = ?";
            
            return DB.executeQuerySafe(query, rs -> {
                if (rs.next()) {
                    return new StockLossDTO(
                        rs.getInt("stock_loss_id"),
                        rs.getInt("qty"),
                        rs.getTimestamp("stock_loss_date"),
                        rs.getString("product_name"),
                        rs.getString("batch_no"),
                        rs.getDouble("selling_price"),
                        rs.getString("user_name"),
                        rs.getString("reason"),
                        rs.getString("invoice_no")
                    );
                }
                return null;
            }, lossId);
            
        } catch (SQLException e) {
            System.err.println("Error fetching stock loss by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Add, update, delete methods...
    public boolean addStockLoss(int stockId, int qty, int userId, int reasonId, Integer salesId) {
        try {
            String sql = "INSERT INTO stock_loss (stock_id, qty, stock_loss_date, user_id, return_reason_id, sales_id) " +
                        "VALUES (?, ?, NOW(), ?, ?, ?)";
            
            int rowsAffected = DB.executeUpdate(sql, stockId, qty, userId, reasonId, salesId);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding stock loss: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateStockLoss(int lossId, int stockId, int qty, int userId, int reasonId, Integer salesId) {
        try {
            String sql = "UPDATE stock_loss SET stock_id = ?, qty = ?, user_id = ?, " +
                        "return_reason_id = ?, sales_id = ? WHERE stock_loss_id = ?";
            
            int rowsAffected = DB.executeUpdate(sql, stockId, qty, userId, reasonId, salesId, lossId);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating stock loss: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteStockLoss(int lossId) {
        try {
            String sql = "DELETE FROM stock_loss WHERE stock_loss_id = ?";
            int rowsAffected = DB.executeUpdate(sql, lossId);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting stock loss: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}