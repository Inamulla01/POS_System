package lk.com.pos.privateclasses;

import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class StockTracker {
    private static StockTracker instance;
    private Map<String, Integer> cartQuantities = new HashMap<>();
    private Map<String, ProductCardReference> cardReferences = new HashMap<>();
    
    private StockTracker() {}
    
    public static StockTracker getInstance() {
        if (instance == null) {
            instance = new StockTracker();
        }
        return instance;
    }
    
    // Register a product card for updates
    public void registerCard(int productId, String batchNo, ProductCardReference cardRef) {
        String key = productId + "_" + batchNo;
        cardReferences.put(key, cardRef);
    }
    
    // Unregister card (when products are reloaded)
    public void unregisterCard(int productId, String batchNo) {
        String key = productId + "_" + batchNo;
        cardReferences.remove(key);
    }
    
    // Clear all registrations
    public void clearAllCards() {
        cardReferences.clear();
        cartQuantities.clear();
    }
    
    // Add to cart
    public void addToCart(int productId, String batchNo, int quantity) {
        String key = productId + "_" + batchNo;
        int current = cartQuantities.getOrDefault(key, 0);
        cartQuantities.put(key, current + quantity);
        
        // Update only the affected card (O(1) operation)
        updateCard(key);
    }
    
    // Remove from cart
    public void removeFromCart(int productId, String batchNo, int quantity) {
        String key = productId + "_" + batchNo;
        int current = cartQuantities.getOrDefault(key, 0);
        int newQty = Math.max(0, current - quantity);
        
        if (newQty == 0) {
            cartQuantities.remove(key);
        } else {
            cartQuantities.put(key, newQty);
        }
        
        // Update only the affected card (O(1) operation)
        updateCard(key);
    }
    
    // Get quantity in cart
    public int getCartQuantity(int productId, String batchNo) {
        String key = productId + "_" + batchNo;
        return cartQuantities.getOrDefault(key, 0);
    }
    
    // Get card reference (for manual updates)
    public ProductCardReference getCardReference(String key) {
        return cardReferences.get(key);
    }
    
    // Clear cart (on checkout or reset)
    public void clearCart() {
        cartQuantities.clear();
        // Update all cards at once
        updateAllCards();
    }
    
    // Efficient single card update
    private void updateCard(String key) {
        ProductCardReference cardRef = cardReferences.get(key);
        if (cardRef != null) {
            int cartQty = cartQuantities.getOrDefault(key, 0);
            // Update on EDT if not already on it
            if (SwingUtilities.isEventDispatchThread()) {
                cardRef.updateStock(cartQty);
            } else {
                SwingUtilities.invokeLater(() -> cardRef.updateStock(cartQty));
            }
        }
    }
    
    // Update all cards (used after checkout)
    private void updateAllCards() {
        SwingUtilities.invokeLater(() -> {
            for (Map.Entry<String, ProductCardReference> entry : cardReferences.entrySet()) {
                String key = entry.getKey();
                ProductCardReference cardRef = entry.getValue();
                int cartQty = cartQuantities.getOrDefault(key, 0);
                cardRef.updateStock(cartQty);
            }
        });
    }
}