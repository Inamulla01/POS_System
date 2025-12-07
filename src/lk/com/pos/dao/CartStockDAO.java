package lk.com.pos.dao;

import lk.com.pos.dto.CartStockDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartStockDAO {
    
    public CartStockDTO getAvailableStock(int productId, String batchNo) throws SQLException {
        String query = "SELECT s.stock_id, s.product_id, s.batch_no, s.qty, s.selling_price, s.cost_price " +
                      "FROM stock s " +
                      "WHERE s.product_id = ? AND s.batch_no = ? AND s.qty > 0";
        
        return DB.executeQuerySafe(query, (rs) -> {
            if (rs.next()) {
                return new CartStockDTO(
                    rs.getInt("stock_id"),
                    rs.getInt("product_id"),
                    rs.getString("batch_no"),
                    rs.getInt("qty"),
                    rs.getDouble("selling_price"),
                    rs.getDouble("cost_price")
                );
            }
            return null;
        }, productId, batchNo);
    }
    
    public List<CartStockDTO> getAvailableStockByProduct(int productId) throws SQLException {
        String query = "SELECT s.stock_id, s.product_id, s.batch_no, s.qty, s.selling_price, s.cost_price " +
                      "FROM stock s " +
                      "WHERE s.product_id = ? AND s.qty > 0 " +
                      "ORDER BY s.batch_no ASC";
        
        return DB.executeQuerySafe(query, (rs) -> {
            List<CartStockDTO> stocks = new ArrayList<>();
            while (rs.next()) {
                stocks.add(new CartStockDTO(
                    rs.getInt("stock_id"),
                    rs.getInt("product_id"),
                    rs.getString("batch_no"),
                    rs.getInt("qty"),
                    rs.getDouble("selling_price"),
                    rs.getDouble("cost_price")
                ));
            }
            return stocks;
        }, productId);
    }
    
    public boolean updateStockQuantity(int stockId, int newQty) throws SQLException {
        String query = "UPDATE stock SET qty = ? WHERE stock_id = ?";
        return DB.executeUpdate(query, newQty, stockId) > 0;
    }
    
    public boolean reduceStockQuantity(int stockId, int quantityToReduce) throws SQLException {
        String query = "UPDATE stock SET qty = qty - ? WHERE stock_id = ? AND qty >= ?";
        return DB.executeUpdate(query, quantityToReduce, stockId, quantityToReduce) > 0;
    }
    
    public int getStockId(int productId, String batchNo) throws SQLException {
        String query = "SELECT stock_id FROM stock WHERE product_id = ? AND batch_no = ?";
        
        return DB.executeQuerySafe(query, (rs) -> {
            if (rs.next()) {
                return rs.getInt("stock_id");
            }
            return -1;
        }, productId, batchNo);
    }
}