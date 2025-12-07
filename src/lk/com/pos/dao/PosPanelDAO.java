package lk.com.pos.dao;

import lk.com.pos.dto.PosPanelDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PosPanelDAO {

    /**
     * 🔍 Search products by name or barcode
     */
    public List<PosPanelDTO> searchProducts(String searchText) {
        List<PosPanelDTO> products = new ArrayList<>();
        
        try {
            String sql = "SELECT p.product_id, p.product_name, b.brand_name, s.batch_no, " +
                        "s.qty, s.selling_price, p.barcode, s.last_price " +
                        "FROM product p " +
                        "INNER JOIN stock s ON s.product_id = p.product_id " +
                        "INNER JOIN brand b ON b.brand_id = p.brand_id " +
                        "WHERE s.qty > 0 ";
            
            if (searchText != null && !searchText.isEmpty()) {
                sql += "AND (p.product_name LIKE ? OR p.barcode LIKE ?) ";
            }
            
            sql += "ORDER BY p.product_name ASC LIMIT 100";
            
            try (Connection conn = DB.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                
                if (searchText != null && !searchText.isEmpty()) {
                    pst.setString(1, "%" + searchText + "%");
                    pst.setString(2, "%" + searchText + "%");
                }
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        products.add(mapResultSetToDTO(rs));
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error searching products");
        }
        
        return products;
    }

    /**
     * 🎯 Get product by ID and batch
     */
    public PosPanelDTO getProductByIdAndBatch(int productId, String batchNo) {
        String sql = "SELECT p.product_id, p.product_name, b.brand_name, s.batch_no, " +
                    "s.qty, s.selling_price, p.barcode, s.last_price " +
                    "FROM product p " +
                    "INNER JOIN stock s ON s.product_id = p.product_id " +
                    "INNER JOIN brand b ON b.brand_id = p.brand_id " +
                    "WHERE s.qty > 0 AND p.product_id = ? AND s.batch_no = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, productId);
            pst.setString(2, batchNo);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDTO(rs);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error getting product by ID and batch");
        }
        
        return null;
    }

    /**
     * 📦 Check stock availability
     */
    public int checkStockAvailability(int productId, String batchNo) {
        String sql = "SELECT qty FROM stock WHERE product_id = ? AND batch_no = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, productId);
            pst.setString(2, batchNo);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("qty");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error checking stock availability");
        }
        
        return 0;
    }

    /**
     * 🔄 Update stock quantity
     */
    public boolean updateStock(int productId, String batchNo, int quantity) {
        String sql = "UPDATE stock SET qty = qty - ? WHERE product_id = ? AND batch_no = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, quantity);
            pst.setInt(2, productId);
            pst.setString(3, batchNo);
            
            int rows = pst.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            handleSQLException(e, "Error updating stock");
            return false;
        }
    }

    /**
     * 📍 Get product by barcode (FAST search)
     */
    public PosPanelDTO getProductByBarcode(String barcode) {
        String sql = "SELECT p.product_id, p.product_name, b.brand_name, s.batch_no, " +
                    "s.qty, s.selling_price, p.barcode, s.last_price " +
                    "FROM product p " +
                    "INNER JOIN stock s ON s.product_id = p.product_id " +
                    "INNER JOIN brand b ON b.brand_id = p.brand_id " +
                    "WHERE s.qty > 0 AND p.barcode = ? " +
                    "ORDER BY s.qty DESC LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, barcode);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDTO(rs);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error getting product by barcode");
        }
        
        return null;
    }

    /**
     * ⚠️ Get low stock products (less than 10)
     */
    public List<PosPanelDTO> getLowStockProducts() {
        List<PosPanelDTO> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, b.brand_name, s.batch_no, " +
                    "s.qty, s.selling_price, p.barcode, s.last_price " +
                    "FROM product p " +
                    "INNER JOIN stock s ON s.product_id = p.product_id " +
                    "INNER JOIN brand b ON b.brand_id = p.brand_id " +
                    "WHERE s.qty > 0 AND s.qty < 10 " +
                    "ORDER BY s.qty ASC";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToDTO(rs));
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error getting low stock products");
        }
        
        return products;
    }

    /**
     * 💰 Refresh product price from database
     */
    public double refreshProductPrice(int productId, String batchNo) {
        String sql = "SELECT selling_price FROM stock WHERE product_id = ? AND batch_no = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, productId);
            pst.setString(2, batchNo);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("selling_price");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error refreshing product price");
        }
        
        return 0.0;
    }

    /**
     * 🔧 Map ResultSet to DTO
     */
    private PosPanelDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        return new PosPanelDTO(
            rs.getInt("product_id"),
            rs.getString("product_name"),
            rs.getString("brand_name"),
            rs.getString("batch_no"),
            rs.getInt("qty"),
            rs.getDouble("selling_price"),
            rs.getString("barcode"),
            rs.getDouble("last_price")
        );
    }

    /**
     * 🔄 Bulk update stock quantities
     */
    public int[] updateStockBatch(List<StockUpdateDTO> updates) {
        if (updates == null || updates.isEmpty()) {
            return new int[0];
        }
        
        String sql = "UPDATE stock SET qty = qty - ? WHERE product_id = ? AND batch_no = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            for (StockUpdateDTO update : updates) {
                pst.setInt(1, update.getQuantity());
                pst.setInt(2, update.getProductId());
                pst.setString(3, update.getBatchNo());
                pst.addBatch();
            }
            
            return pst.executeBatch();
            
        } catch (SQLException e) {
            handleSQLException(e, "Error updating stock batch");
            return new int[0];
        }
    }

    /**
     * 🛠️ Handle SQL exceptions
     */
    private void handleSQLException(SQLException e, String message) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * 📊 Get product count
     */
    public int getProductCount() {
        String sql = "SELECT COUNT(*) as count FROM product WHERE status = 1";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error getting product count");
        }
        
        return 0;
    }

    /**
     * 📈 Get top selling products
     */
    public List<PosPanelDTO> getTopSellingProducts(int limit) {
        List<PosPanelDTO> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, b.brand_name, s.batch_no, " +
                    "s.qty, s.selling_price, p.barcode, s.last_price " +
                    "FROM product p " +
                    "INNER JOIN stock s ON s.product_id = p.product_id " +
                    "INNER JOIN brand b ON b.brand_id = p.brand_id " +
                    "WHERE s.qty > 0 " +
                    "ORDER BY s.selling_price DESC LIMIT ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, limit);
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToDTO(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error getting top selling products");
        }
        
        return products;
    }

    /**
     * DTO for stock updates
     */
    public static class StockUpdateDTO {
        private int productId;
        private String batchNo;
        private int quantity;

        public StockUpdateDTO(int productId, String batchNo, int quantity) {
            this.productId = productId;
            this.batchNo = batchNo;
            this.quantity = quantity;
        }

        public int getProductId() { return productId; }
        public String getBatchNo() { return batchNo; }
        public int getQuantity() { return quantity; }
    }
}