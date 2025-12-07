package lk.com.pos.dao;

import lk.com.pos.dto.ProductStockDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.*;

public class ProductStockDAO {
    
    // Get batch counts per product
    public Map<Integer, Integer> getBatchCounts() throws SQLException {
        Map<Integer, Integer> batchCounts = new HashMap<>();
        String query = "SELECT product_id, COUNT(*) as batch_count " +
                      "FROM stock WHERE qty > 0 GROUP BY product_id";
        
        return DB.executeQuerySafe(query, rs -> {
            while (rs.next()) {
                batchCounts.put(rs.getInt("product_id"), rs.getInt("batch_count"));
            }
            return batchCounts;
        });
    }
    
    // Get all product stock with filters
    public List<ProductStockDTO> getProductStock(String productSearch, String status, 
                                                 Map<Integer, Integer> batchCounts) throws SQLException {
        List<ProductStockDTO> products = new ArrayList<>();
        
        // Build the query
        StringBuilder query = new StringBuilder(
            "SELECT product.product_id, product.product_name, suppliers.suppliers_name, "
            + "brand.brand_name, category.category_name, "
            + "stock.qty, stock.expriy_date, stock.batch_no, product.barcode, "
            + "stock.purchase_price, stock.last_price, stock.selling_price, "
            + "stock.stock_id, product.p_status_id, "
            + "CASE "
            + "  WHEN stock.expriy_date < CURDATE() THEN 1 "
            + "  WHEN stock.qty < 10 THEN 2 "
            + "  WHEN stock.expriy_date <= DATE_ADD(CURDATE(), INTERVAL 3 MONTH) THEN 3 "
            + "  ELSE 4 "
            + "END as priority "
            + "FROM product "
            + "JOIN stock ON stock.product_id = product.product_id "
            + "JOIN category ON category.category_id = product.category_id "
            + "JOIN brand ON brand.brand_id = product.brand_id "
            + "JOIN suppliers ON suppliers.suppliers_id = stock.suppliers_id "
            + "JOIN p_status ON p_status.p_status_id = product.p_status_id "
            + "WHERE stock.qty > 0 "
        );

        // Apply status filter
        if ("Inactive".equals(status)) {
            query.append("AND product.p_status_id = 2 ");
        } else {
            query.append("AND product.p_status_id = 1 ");
        }

        // Apply search filter
        if (productSearch != null && !productSearch.trim().isEmpty()
                && !productSearch.equals("Search By Product Name Or Barcode")) {
            // Sanitize input to prevent SQL injection
            String escapedSearch = productSearch.replace("'", "''")
                                               .replace("\\", "\\\\")
                                               .replace("%", "\\%")
                                               .replace("_", "\\_");
            query.append("AND (product.product_name LIKE ? OR product.barcode LIKE ?) ");
        }

        // Apply status-specific filters
        if ("Low Stock".equals(status)) {
            query.append("AND stock.qty < 10 ");
        } else if ("Expiring Soon".equals(status)) {
            query.append("AND stock.expriy_date <= DATE_ADD(CURDATE(), INTERVAL 3 MONTH) ")
                 .append("AND stock.expriy_date >= CURDATE() ");
        } else if ("Expired".equals(status)) {
            query.append("AND stock.expriy_date < CURDATE() ");
        }

        // Order by
        query.append("ORDER BY priority ASC, product.product_name ASC, stock.expriy_date ASC");
        
        // Track batch indices per product
        Map<Integer, Integer> currentBatchIndex = new HashMap<>();
        
        return DB.executeQuerySafe(query.toString(), rs -> {
            while (rs.next()) {
                ProductStockDTO dto = new ProductStockDTO();
                dto.setProductId(rs.getInt("product_id"));
                dto.setStockId(rs.getInt("stock_id"));
                dto.setPStatusId(rs.getInt("p_status_id"));
                dto.setQty(rs.getInt("qty"));
                dto.setProductName(rs.getString("product_name"));
                dto.setSupplierName(rs.getString("suppliers_name"));
                dto.setBrandName(rs.getString("brand_name"));
                dto.setCategoryName(rs.getString("category_name"));
                dto.setExpiryDate(rs.getString("expriy_date"));
                dto.setBatchNo(rs.getString("batch_no"));
                dto.setBarcode(rs.getString("barcode"));
                dto.setPurchasePrice(rs.getDouble("purchase_price"));
                dto.setLastPrice(rs.getDouble("last_price"));
                dto.setSellingPrice(rs.getDouble("selling_price"));
                dto.setPriorityScore(rs.getInt("priority"));
                
                // Calculate batch index
                int productId = dto.getProductId();
                int batchIdx = currentBatchIndex.getOrDefault(productId, 0) + 1;
                currentBatchIndex.put(productId, batchIdx);
                dto.setBatchIndex(batchIdx);
                dto.setTotalBatches(batchCounts.getOrDefault(productId, 1));
                
                products.add(dto);
            }
            return products;
        }, 
        // Handle parameters for search
        (productSearch != null && !productSearch.trim().isEmpty() && 
         !productSearch.equals("Search By Product Name Or Barcode")) ? 
            new Object[]{"%" + productSearch + "%", "%" + productSearch + "%"} : 
            new Object[]{});
    }
    
    // Deactivate/activate product
    public boolean updateProductStatus(int productId, int status) throws SQLException {
        String query = "UPDATE product SET p_status_id = ? WHERE product_id = ?";
        int rowsAffected = DB.executeUpdate(query, status, productId);
        return rowsAffected > 0;
    }
    
    // Check if product has sales history
    public boolean hasSalesHistory(int stockId) throws SQLException {
        String query = "SELECT COUNT(*) as sale_count FROM sale_item WHERE stock_id = ?";
        return DB.executeQuerySafe(query, rs -> {
            if (rs.next()) {
                return rs.getInt("sale_count") > 0;
            }
            return false;
        }, stockId);
    }
    
    // Get product by ID
    public ProductStockDTO getProductById(int productId, int stockId) throws SQLException {
        String query = "SELECT product.product_id, product.product_name, suppliers.suppliers_name, "
                     + "brand.brand_name, category.category_name, "
                     + "stock.qty, stock.expriy_date, stock.batch_no, product.barcode, "
                     + "stock.purchase_price, stock.last_price, stock.selling_price, "
                     + "stock.stock_id, product.p_status_id "
                     + "FROM product "
                     + "JOIN stock ON stock.product_id = product.product_id "
                     + "JOIN category ON category.category_id = product.category_id "
                     + "JOIN brand ON brand.brand_id = product.brand_id "
                     + "JOIN suppliers ON suppliers.suppliers_id = stock.suppliers_id "
                     + "WHERE product.product_id = ? AND stock.stock_id = ?";
        
        return DB.executeQuerySafe(query, rs -> {
            if (rs.next()) {
                ProductStockDTO dto = new ProductStockDTO();
                dto.setProductId(rs.getInt("product_id"));
                dto.setStockId(rs.getInt("stock_id"));
                dto.setPStatusId(rs.getInt("p_status_id"));
                dto.setQty(rs.getInt("qty"));
                dto.setProductName(rs.getString("product_name"));
                dto.setSupplierName(rs.getString("suppliers_name"));
                dto.setBrandName(rs.getString("brand_name"));
                dto.setCategoryName(rs.getString("category_name"));
                dto.setExpiryDate(rs.getString("expriy_date"));
                dto.setBatchNo(rs.getString("batch_no"));
                dto.setBarcode(rs.getString("barcode"));
                dto.setPurchasePrice(rs.getDouble("purchase_price"));
                dto.setLastPrice(rs.getDouble("last_price"));
                dto.setSellingPrice(rs.getDouble("selling_price"));
                return dto;
            }
            return null;
        }, productId, stockId);
    }
}