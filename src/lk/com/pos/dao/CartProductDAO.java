package lk.com.pos.dao;

import lk.com.pos.dto.CartProductDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartProductDAO {
    
    public CartProductDTO getProductByBarcode(String barcode) throws SQLException {
        String query = "SELECT p.product_id, p.product_name, b.brand_name, p.barcode, b.brand_id " +
                      "FROM product p " +
                      "INNER JOIN brand b ON p.brand_id = b.brand_id " +
                      "WHERE p.barcode = ? LIMIT 1";
        
        return DB.executeQuerySafe(query, (rs) -> {
            if (rs.next()) {
                return new CartProductDTO(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("barcode"),
                    rs.getInt("brand_id"),
                    rs.getString("brand_name")
                );
            }
            return null;
        }, barcode);
    }
    
    public List<CartProductDTO> searchProducts(String searchTerm) throws SQLException {
        String query = "SELECT p.product_id, p.product_name, b.brand_name, p.barcode, b.brand_id " +
                      "FROM product p " +
                      "INNER JOIN brand b ON p.brand_id = b.brand_id " +
                      "WHERE p.product_name LIKE ? OR p.barcode LIKE ? " +
                      "ORDER BY p.product_name LIMIT 50";
        
        String likeTerm = "%" + searchTerm + "%";
        return DB.executeQuerySafe(query, (rs) -> {
            List<CartProductDTO> products = new ArrayList<>();
            while (rs.next()) {
                products.add(new CartProductDTO(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("barcode"),
                    rs.getInt("brand_id"),
                    rs.getString("brand_name")
                ));
            }
            return products;
        }, likeTerm, likeTerm);
    }
}