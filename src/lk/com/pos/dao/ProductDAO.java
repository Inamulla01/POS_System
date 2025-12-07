package lk.com.pos.dao;

import lk.com.pos.dto.ProductDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ProductDAO - Data Access Object for Product operations
 * @author pasin
 */
public class ProductDAO {
    private static final Logger log = Logger.getLogger(ProductDAO.class.getName());
    
    /**
     * Get all products with details (brand, category names) for display
     * @param searchText Optional search filter
     * @param status "Active" or "Inactive" or null for all
     * @return List of ProductDTO
     */
    public List<ProductDTO> getProductsForDisplay(String searchText, String status) {
        String query = "SELECT DISTINCT " +
                       "p.product_id, " +
                       "p.product_name, " +
                       "b.brand_name, " +
                       "c.category_name, " +
                       "p.barcode, " +
                       "p.p_status_id " +
                       "FROM product p " +
                       "JOIN category c ON c.category_id = p.category_id " +
                       "JOIN brand b ON b.brand_id = p.brand_id " +
                       "WHERE 1=1 ";
        
        List<Object> params = new ArrayList<>();
        
        // Add status filter
        if ("Inactive".equals(status)) {
            query += "AND p.p_status_id = 2 ";
        } else if ("Active".equals(status)) {
            query += "AND p.p_status_id = 1 ";
        }
        
        // Add search filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            query += "AND (p.product_name LIKE ? OR p.barcode LIKE ?) ";
            params.add("%" + searchText + "%");
            params.add("%" + searchText + "%");
        }
        
        query += "ORDER BY p.product_name";
        
        try {
            return DB.executeQuerySafe(query, (ResultSet rs) -> {
                List<ProductDTO> resultList = new ArrayList<>();
                while (rs.next()) {
                    ProductDTO product = new ProductDTO();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setBrandName(rs.getString("brand_name"));
                    product.setCategoryName(rs.getString("category_name"));
                    product.setBarcode(rs.getString("barcode"));
                    product.setPStatusId(rs.getInt("p_status_id"));
                    resultList.add(product);
                }
                return resultList;
            }, params.toArray());
            
        } catch (SQLException e) {
            log.severe("Error getting products for display: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get product by ID with all details
     * @param productId Product ID
     * @return ProductDTO or null if not found
     */
    public ProductDTO getProductById(int productId) {
        String query = "SELECT p.*, b.brand_name, c.category_name " +
                       "FROM product p " +
                       "LEFT JOIN brand b ON b.brand_id = p.brand_id " +
                       "LEFT JOIN category c ON c.category_id = p.category_id " +
                       "WHERE p.product_id = ?";
        
        try {
            return DB.executeQuerySafe(query, (ResultSet rs) -> {
                if (rs.next()) {
                    ProductDTO product = new ProductDTO();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setBarcode(rs.getString("barcode"));
                    product.setBrandId(rs.getInt("brand_id"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setPStatusId(rs.getInt("p_status_id"));
                    product.setReorderLevel(rs.getInt("reorder_level"));
                    product.setBrandName(rs.getString("brand_name"));
                    product.setCategoryName(rs.getString("category_name"));
                    return product;
                }
                return null;
            }, productId);
            
        } catch (SQLException e) {
            log.severe("Error getting product by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get product by barcode
     * @param barcode Barcode
     * @return ProductDTO or null if not found
     */
    public ProductDTO getProductByBarcode(String barcode) {
        String query = "SELECT p.*, b.brand_name, c.category_name " +
                       "FROM product p " +
                       "LEFT JOIN brand b ON b.brand_id = p.brand_id " +
                       "LEFT JOIN category c ON c.category_id = p.category_id " +
                       "WHERE p.barcode = ?";
        
        try {
            return DB.executeQuerySafe(query, (ResultSet rs) -> {
                if (rs.next()) {
                    ProductDTO product = new ProductDTO();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setBarcode(rs.getString("barcode"));
                    product.setBrandId(rs.getInt("brand_id"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setPStatusId(rs.getInt("p_status_id"));
                    product.setReorderLevel(rs.getInt("reorder_level"));
                    product.setBrandName(rs.getString("brand_name"));
                    product.setCategoryName(rs.getString("category_name"));
                    return product;
                }
                return null;
            }, barcode);
            
        } catch (SQLException e) {
            log.severe("Error getting product by barcode: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Insert new product
     * @param product ProductDTO object
     * @return Generated product ID, or -1 if failed
     */
    public int insertProduct(ProductDTO product) {
        String query = "INSERT INTO product (product_name, barcode, brand_id, " +
                       "category_id, p_status_id, reorder_level) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            return DB.insertAndGetId(query,
                product.getProductName(),
                product.getBarcode(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getPStatusId(),
                product.getReorderLevel()
            );
            
        } catch (SQLException e) {
            log.severe("Error inserting product: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Update product
     * @param product ProductDTO object
     * @return Number of affected rows
     */
    public int updateProduct(ProductDTO product) {
        String query = "UPDATE product SET " +
                       "product_name = ?, " +
                       "barcode = ?, " +
                       "brand_id = ?, " +
                       "category_id = ?, " +
                       "p_status_id = ?, " +
                       "reorder_level = ? " +
                       "WHERE product_id = ?";
        
        try {
            return DB.executeUpdate(query,
                product.getProductName(),
                product.getBarcode(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getPStatusId(),
                product.getReorderLevel(),
                product.getProductId()
            );
            
        } catch (SQLException e) {
            log.severe("Error updating product: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Update product status (activate/deactivate)
     * @param productId Product ID
     * @param pStatusId Status ID (1=Active, 2=Inactive)
     * @return Number of affected rows
     */
    public int updateProductStatus(int productId, int pStatusId) {
        String query = "UPDATE product SET p_status_id = ? WHERE product_id = ?";
        
        try {
            return DB.executeUpdate(query, pStatusId, productId);
        } catch (SQLException e) {
            log.severe("Error updating product status: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Delete product (permanent deletion)
     * @param productId Product ID
     * @return Number of affected rows
     */
    public int deleteProduct(int productId) {
        String query = "DELETE FROM product WHERE product_id = ?";
        
        try {
            return DB.executeUpdate(query, productId);
        } catch (SQLException e) {
            log.severe("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get supplier name for product
     * @param productId Product ID
     * @return Supplier name or "N/A"
     */
    public String getSupplierForProduct(int productId) {
        String query = "SELECT s.suppliers_name " +
                       "FROM stock st " +
                       "JOIN suppliers s ON s.suppliers_id = st.suppliers_id " +
                       "WHERE st.product_id = ? LIMIT 1";
        
        try {
            return DB.executeQuerySafe(query, (ResultSet rs) -> {
                if (rs.next()) {
                    return rs.getString("suppliers_name");
                }
                return "N/A";
            }, productId);
            
        } catch (SQLException e) {
            log.severe("Error getting supplier for product: " + e.getMessage());
            e.printStackTrace();
            return "N/A";
        }
    }
    
    /**
     * Check if barcode exists
     * @param barcode Barcode to check
     * @param excludeProductId Product ID to exclude (for updates)
     * @return true if barcode exists
     */
    public boolean isBarcodeExists(String barcode, int excludeProductId) {
        String query = "SELECT COUNT(*) as count FROM product " +
                       "WHERE barcode = ? AND product_id != ?";
        
        try {
            return DB.executeQuerySafe(query, (ResultSet rs) -> {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
                return false;
            }, barcode, excludeProductId);
            
        } catch (SQLException e) {
            log.severe("Error checking barcode: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get product count by status
     * @param status "Active" or "Inactive"
     * @return Count of products
     */
    public int getProductCountByStatus(String status) {
        String query = "SELECT COUNT(*) as count FROM product WHERE p_status_id = ?";
        int statusId = "Inactive".equals(status) ? 2 : 1;
        
        try {
            return DB.executeQuerySafe(query, (ResultSet rs) -> {
                if (rs.next()) {
                    return rs.getInt("count");
                }
                return 0;
            }, statusId);
            
        } catch (SQLException e) {
            log.severe("Error getting product count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get all active products
     * @return List of ProductDTO
     */
    public List<ProductDTO> getAllActiveProducts() {
        return getProductsForDisplay(null, "Active");
    }
    
    /**
     * Get all inactive products
     * @return List of ProductDTO
     */
    public List<ProductDTO> getAllInactiveProducts() {
        return getProductsForDisplay(null, "Inactive");
    }
    
    /**
     * Search products by name or barcode
     * @param searchText Search text
     * @return List of ProductDTO
     */
    public List<ProductDTO> searchProducts(String searchText) {
        return getProductsForDisplay(searchText, null);
    }
    
    /**
     * Get stock ID for product
     * @param productId Product ID
     * @return Stock ID or -1 if not found
     */
    public int getStockIdForProduct(int productId) {
        String query = "SELECT stock_id FROM stock WHERE product_id = ? LIMIT 1";
        
        try {
            return DB.executeQuerySafe(query, (ResultSet rs) -> {
                if (rs.next()) {
                    return rs.getInt("stock_id");
                }
                return -1;
            }, productId);
            
        } catch (SQLException e) {
            log.severe("Error getting stock ID for product: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
}