package lk.com.pos.privateclasses;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ProductCardReference {
    private final int productId;
    private final String batchNo;
    private final int originalStock;
    private final JLabel stockLabel;
    private final JPanel stockBadge;
    
    public ProductCardReference(int productId, String batchNo, int originalStock, 
                               JLabel stockLabel, JPanel stockBadge) {
        this.productId = productId;
        this.batchNo = batchNo;
        this.originalStock = originalStock;
        this.stockLabel = stockLabel;
        this.stockBadge = stockBadge;
    }
    
    // Fast update - only changes the label text and colors
    public void updateStock(int cartQuantity) {
        int availableStock = originalStock - cartQuantity;
        
        // Update text
        if (cartQuantity > 0) {
            stockLabel.setText(String.format("Stock: %d (%d in cart)", availableStock, cartQuantity));
        } else {
            stockLabel.setText("Stock: " + availableStock);
        }
        
        // Update colors based on availability
        if (availableStock <= 0) {
            stockLabel.setForeground(new Color(220, 53, 69)); // Red
            stockBadge.setBackground(new Color(255, 230, 230));
        } else if (availableStock < 10) {
            stockLabel.setForeground(new Color(255, 152, 0)); // Orange
            stockBadge.setBackground(new Color(255, 243, 224));
        } else {
            stockLabel.setForeground(new Color(34, 139, 34)); // Green
            stockBadge.setBackground(new Color(230, 245, 230));
        }
        
        // No revalidate/repaint needed - just text/color change
    }
    
    public int getProductId() {
        return productId;
    }
    
    public String getBatchNo() {
        return batchNo;
    }
    
    public int getAvailableStock(int cartQuantity) {
        return originalStock - cartQuantity;
    }
}