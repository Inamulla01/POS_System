package lk.com.pos.privateclasses;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ProductCardReference {
    private final int productId;
    private final String batchNo;
    private final JLabel stockLabel;
    private final JPanel stockBadge;
    
    // ✅ Cache to avoid unnecessary UI updates
    private volatile String lastLabelText = "";
    private volatile int lastLabelColorRGB = -1;
    private volatile int lastBadgeColorRGB = -1;
    
    // ✅ Flag to prevent concurrent updates
    private volatile boolean isUpdating = false;
    
    public ProductCardReference(int productId, String batchNo, 
                               JLabel stockLabel, JPanel stockBadge) {
        if (stockLabel == null) {
            throw new IllegalArgumentException("stockLabel cannot be null");
        }
        
        this.productId = productId;
        this.batchNo = batchNo != null ? batchNo : "N/A";
        this.stockLabel = stockLabel;
        this.stockBadge = stockBadge;
    }
    
    /**
     * ✅ OPTIMIZED: Thread-safe stock update with change detection
     */
    public void updateStock(int cartQuantity, int availableStock) {
        // Prevent concurrent updates to same card
        if (isUpdating) {
            return;
        }
        
        try {
            isUpdating = true;
            
            // Calculate new values
            String newText;
            if (cartQuantity > 0) {
                newText = String.format("Stock: %d (%d in cart)", availableStock, cartQuantity);
            } else {
                newText = "Stock: " + availableStock;
            }
            
            Color newLabelColor = getLabelColor(availableStock);
            Color newBadgeColor = getBadgeColor(availableStock);
            
            // ✅ FIXED: Compare RGB values instead of object references
            int newLabelRGB = newLabelColor.getRGB();
            int newBadgeRGB = newBadgeColor.getRGB();
            
            boolean needsUpdate = !newText.equals(lastLabelText) ||
                                 newLabelRGB != lastLabelColorRGB ||
                                 (stockBadge != null && newBadgeRGB != lastBadgeColorRGB);
            
            if (!needsUpdate) {
                return; // Skip if nothing changed
            }
            
            // Update cache
            lastLabelText = newText;
            lastLabelColorRGB = newLabelRGB;
            lastBadgeColorRGB = newBadgeRGB;
            
            // ✅ IMPROVED: Update UI with error handling
            final String finalText = newText;
            final Color finalLabelColor = newLabelColor;
            final Color finalBadgeColor = newBadgeColor;
            
            Runnable updateTask = () -> {
                try {
                    if (stockLabel.isDisplayable()) {
                        stockLabel.setText(finalText);
                        stockLabel.setForeground(finalLabelColor);
                    }
                    
                    if (stockBadge != null && stockBadge.isDisplayable()) {
                        stockBadge.setBackground(finalBadgeColor);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating card UI for " + productId + "_" + batchNo + ": " + e.getMessage());
                }
            };
            
            if (SwingUtilities.isEventDispatchThread()) {
                updateTask.run();
            } else {
                SwingUtilities.invokeLater(updateTask);
            }
            
        } finally {
            isUpdating = false;
        }
    }
    
    /**
     * ✅ NEW: Force update (ignore cache)
     */
    public void forceUpdate(int cartQuantity, int availableStock) {
        lastLabelText = "";
        lastLabelColorRGB = -1;
        lastBadgeColorRGB = -1;
        updateStock(cartQuantity, availableStock);
    }
    
    /**
     * ✅ NEW: Check if card is valid and displayable
     */
    public boolean isValid() {
        return stockLabel != null && stockLabel.isDisplayable();
    }
    
    // Helper methods for color calculation
    private Color getLabelColor(int availableStock) {
        if (availableStock <= 0) {
            return new Color(220, 53, 69); // Red
        } else if (availableStock < 10) {
            return new Color(255, 152, 0); // Orange
        } else {
            return new Color(34, 139, 34); // Green
        }
    }
    
    private Color getBadgeColor(int availableStock) {
        if (availableStock <= 0) {
            return new Color(255, 230, 230); // Light red
        } else if (availableStock < 10) {
            return new Color(255, 243, 224); // Light orange
        } else {
            return new Color(230, 245, 230); // Light green
        }
    }
    
    // Getters
    public int getProductId() { return productId; }
    public String getBatchNo() { return batchNo; }
    public JLabel getStockLabel() { return stockLabel; }
    public JPanel getStockBadge() { return stockBadge; }
    public String getKey() { return productId + "_" + batchNo; }
    
    @Override
    public String toString() {
        return String.format("ProductCardRef{%d_%s, text='%s'}", 
            productId, batchNo, lastLabelText);
    }
}