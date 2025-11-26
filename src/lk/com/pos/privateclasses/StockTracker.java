package lk.com.pos.privateclasses;

import lk.com.pos.connection.MySQL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.SwingUtilities;

public class StockTracker {
    private static StockTracker instance;
    private Map<String, ProductCardReference> cardReferences = new ConcurrentHashMap<>();
    private Map<String, StockCache> stockCache = new ConcurrentHashMap<>();
    private Map<String, Integer> cartQuantities = new ConcurrentHashMap<>();
    
    private String currentSessionId;
    private int userId;
    private volatile long lastRefreshTime = 0;
    private static final long CACHE_DURATION = 3000;
    private static final long REFRESH_THROTTLE = 2000;
    private final Object refreshLock = new Object();
    
    private StockTracker() {
        generateSessionId();
    }
    
    public static StockTracker getInstance() {
        if (instance == null) {
            synchronized (StockTracker.class) {
                if (instance == null) {
                    instance = new StockTracker();
                }
            }
        }
        return instance;
    }
    
    private void generateSessionId() {
        this.currentSessionId = "POS_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    private static class StockCache {
        int stockQuantity;
        int holdQuantity;
        long timestamp;
        
        StockCache(int stockQuantity, int holdQuantity) {
            this.stockQuantity = stockQuantity;
            this.holdQuantity = holdQuantity;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
    
    public void registerCard(int productId, String batchNo, ProductCardReference cardRef) {
        String key = productId + "_" + batchNo;
        cardReferences.put(key, cardRef);
        updateCardDisplayFromDB(productId, batchNo);
    }
    
    public int getAvailableStock(int productId, String batchNo) {
        String cacheKey = productId + "_" + batchNo;
        
        StockCache cached = stockCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return Math.max(0, cached.stockQuantity - cached.holdQuantity);
        }
        
        String query = "SELECT s.qty as stock_quantity, " +
                      "COALESCE((SELECT SUM(si.qty) FROM sale_item si " +
                      " JOIN sales sa ON si.sales_id = sa.sales_id " +
                      " JOIN stock st ON si.stock_id = st.stock_id " +
                      " WHERE st.product_id = ? AND st.batch_no = ? " +
                      " AND sa.status_id = 2), 0) as hold_quantity " +
                      "FROM stock s " +
                      "WHERE s.product_id = ? AND s.batch_no = ? AND s.qty >= 0";
        
        try (Connection conn = MySQL.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            
            pst.setInt(1, productId);
            pst.setString(2, batchNo);
            pst.setInt(3, productId);
            pst.setString(4, batchNo);
            
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int stockQty = rs.getInt("stock_quantity");
                int holdQty = rs.getInt("hold_quantity");
                
                stockCache.put(cacheKey, new StockCache(stockQty, holdQty));
                
                return Math.max(0, stockQty - holdQty);
            }
        } catch (SQLException e) {
        }
        return 0;
    }
    
    public void refreshStockForKeys(java.util.Set<String> keys) {
        if (keys.isEmpty()) return;
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT s.product_id, s.batch_no, s.qty as stock_quantity, ");
        queryBuilder.append("COALESCE((SELECT SUM(si.qty) FROM sale_item si ");
        queryBuilder.append("JOIN sales sa ON si.sales_id = sa.sales_id ");
        queryBuilder.append("JOIN stock st ON si.stock_id = st.stock_id ");
        queryBuilder.append("WHERE st.product_id = s.product_id AND st.batch_no = s.batch_no ");
        queryBuilder.append("AND sa.status_id = 2), 0) as hold_quantity ");
        queryBuilder.append("FROM stock s WHERE ");
        
        boolean first = true;
        for (String key : keys) {
            if (!first) queryBuilder.append(" OR ");
            queryBuilder.append("(s.product_id = ? AND s.batch_no = ?)");
            first = false;
        }
        
        try (Connection conn = MySQL.getConnection();
             PreparedStatement pst = conn.prepareStatement(queryBuilder.toString())) {
            
            int paramIndex = 1;
            for (String key : keys) {
                String[] parts = key.split("_");
                if (parts.length == 2) {
                    pst.setInt(paramIndex++, Integer.parseInt(parts[0]));
                    pst.setString(paramIndex++, parts[1]);
                }
            }
            
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String batchNo = rs.getString("batch_no");
                int stockQty = rs.getInt("stock_quantity");
                int holdQty = rs.getInt("hold_quantity");
                
                String cacheKey = productId + "_" + batchNo;
                stockCache.put(cacheKey, new StockCache(stockQty, holdQty));
            }
        } catch (SQLException e) {
        }
    }
    
    public int getCartQuantity(int productId, String batchNo) {
        String key = productId + "_" + batchNo;
        Integer qty = cartQuantities.get(key);
        return qty != null ? qty : 0;
    }
    
    public void addToCart(int productId, String batchNo, int quantity) {
        String key = productId + "_" + batchNo;
        int currentQty = cartQuantities.getOrDefault(key, 0);
        int newQty = currentQty + quantity;
        cartQuantities.put(key, newQty);
        updateCardDisplay(key, newQty);
    }
    
    public void removeFromCart(int productId, String batchNo, int quantity) {
        String key = productId + "_" + batchNo;
        int currentQty = cartQuantities.getOrDefault(key, 0);
        int newQty = Math.max(0, currentQty - quantity);
        
        if (newQty == 0) {
            cartQuantities.remove(key);
        } else {
            cartQuantities.put(key, newQty);
        }
        updateCardDisplay(key, newQty);
    }
    
    public void clearCart() {
        cartQuantities.clear();
        SwingUtilities.invokeLater(() -> {
            for (Map.Entry<String, ProductCardReference> entry : cardReferences.entrySet()) {
                String key = entry.getKey();
                updateCardDisplay(key, 0);
            }
        });
    }
    
    private void updateCardDisplay(String key, int cartQuantity) {
        ProductCardReference cardRef = cardReferences.get(key);
        if (cardRef != null) {
            int availableStock = getAvailableStock(cardRef.getProductId(), cardRef.getBatchNo());
            
            if (SwingUtilities.isEventDispatchThread()) {
                cardRef.updateStock(cartQuantity, availableStock);
            } else {
                SwingUtilities.invokeLater(() -> cardRef.updateStock(cartQuantity, availableStock));
            }
        }
    }
    
    private void updateCardDisplayFromDB(int productId, String batchNo) {
        String key = productId + "_" + batchNo;
        int cartQuantity = getCartQuantity(productId, batchNo);
        updateCardDisplay(key, cartQuantity);
    }
    
    public void refreshAllCards() {
        synchronized (refreshLock) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastRefreshTime < REFRESH_THROTTLE) {
                return;
            }
            
            lastRefreshTime = currentTime;
        }
        
        refreshStockForKeys(cardReferences.keySet());
        
        SwingUtilities.invokeLater(() -> {
            for (String key : cardReferences.keySet()) {
                Integer cartQty = cartQuantities.get(key);
                int quantity = (cartQty != null) ? cartQty : 0;
                updateCardDisplay(key, quantity);
            }
        });
    }
    
    public void invalidateCache(int productId, String batchNo) {
        String key = productId + "_" + batchNo;
        stockCache.remove(key);
    }
    
    public void clearCache() {
        stockCache.clear();
    }
    
    public void clearAllCards() {
        cardReferences.clear();
        stockCache.clear();
    }
    
    public ProductCardReference getCardReference(String key) {
        return cardReferences.get(key);
    }
    
    public void unregisterCard(int productId, String batchNo) {
        String key = productId + "_" + batchNo;
        cardReferences.remove(key);
        stockCache.remove(key);
    }
    
    public void resetForNewSale() {
        cardReferences.clear();
        stockCache.clear();
        cartQuantities.clear();
    }
}