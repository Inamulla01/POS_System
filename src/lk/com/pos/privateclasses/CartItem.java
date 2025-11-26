package lk.com.pos.privateclasses;

public class CartItem {
    private int productId;
    private String productName;
    private String brandName;
    private String batchNo;
    private int availableQty;
    private double unitPrice;
    private String barcode;
    private int quantity;
    private double discountPrice; // PER-UNIT discount
    private double lastPrice;
    
    public CartItem(int productId, String productName, String brandName,
                    String batchNo, int availableQty, double unitPrice, 
                    String barcode, double lastPrice) {
        // ✅ ADD: Validation
        if (productId <= 0) throw new IllegalArgumentException("Invalid product ID");
        if (availableQty < 0) throw new IllegalArgumentException("Available qty cannot be negative");
        if (unitPrice < 0) throw new IllegalArgumentException("Unit price cannot be negative");
        
        this.productId = productId;
        this.productName = productName != null ? productName : "Unknown Product";
        this.brandName = brandName != null ? brandName : "Unknown Brand";
        this.batchNo = batchNo != null ? batchNo : "N/A";
        this.availableQty = availableQty;
        this.unitPrice = unitPrice;
        this.barcode = barcode;
        this.quantity = 1;
        this.discountPrice = 0;
        this.lastPrice = lastPrice;
    }
    
    // ✅ FIXED: Calculate total price correctly
    public double getTotalPrice() {
        double baseTotal = unitPrice * quantity;
        double totalDiscount = discountPrice * quantity;
        return Math.max(0, baseTotal - totalDiscount); // Prevent negative totals
    }
    
    // ✅ NEW: Get total discount amount (useful for display)
    public double getTotalDiscount() {
        return discountPrice * quantity;
    }
    
    // ✅ NEW: Get price after discount per unit
    public double getPriceAfterDiscount() {
        return Math.max(0, unitPrice - discountPrice);
    }
    
    // ✅ NEW: Validate discount doesn't go below last price
    public boolean isDiscountValid() {
        return (unitPrice - discountPrice) >= lastPrice;
    }
    
    // ✅ NEW: Get maximum allowed discount per unit
    public double getMaxAllowedDiscount() {
        return Math.max(0, unitPrice - lastPrice);
    }
    
    public String getKey() {
        return productId + "_" + batchNo;
    }
    
    // Getters
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBrandName() { return brandName; }
    public String getBatchNo() { return batchNo; }
    public int getAvailableQty() { return availableQty; }
    public double getUnitPrice() { return unitPrice; }
    public String getBarcode() { return barcode; }
    public int getQuantity() { return quantity; }
    public double getDiscountPrice() { return discountPrice; }
    public double getLastPrice() { return lastPrice; }
    
    // Setters with validation
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (quantity > availableQty) {
            throw new IllegalArgumentException("Quantity exceeds available stock");
        }
        this.quantity = quantity;
    }
    
    // ✅ IMPROVED: Validate discount when setting
    public void setDiscountPrice(double discountPrice) {
        if (discountPrice < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }
        if (discountPrice > unitPrice) {
            throw new IllegalArgumentException("Discount cannot exceed unit price");
        }
        if ((unitPrice - discountPrice) < lastPrice) {
            throw new IllegalArgumentException(
                String.format("Price after discount (%.2f) cannot be below last price (%.2f)",
                    unitPrice - discountPrice, lastPrice)
            );
        }
        this.discountPrice = discountPrice;
    }
    
    public void setLastPrice(double lastPrice) {
        if (lastPrice < 0) {
            throw new IllegalArgumentException("Last price cannot be negative");
        }
        this.lastPrice = lastPrice;
    }
    
    // ✅ NEW: Update available quantity (when stock changes)
    public void setAvailableQty(int availableQty) {
        if (availableQty < 0) {
            throw new IllegalArgumentException("Available qty cannot be negative");
        }
        this.availableQty = availableQty;
        // Auto-adjust quantity if it exceeds new available qty
        if (this.quantity > availableQty) {
            this.quantity = Math.max(1, availableQty);
        }
    }
    
    @Override
    public String toString() {
        return String.format("CartItem{%s (%s) - Batch: %s, Qty: %d/%d, Price: %.2f, Discount: %.2f}",
            productName, brandName, batchNo, quantity, availableQty, unitPrice, discountPrice);
    }
    
    // ✅ NEW: Clone method for safe copying
    public CartItem clone() {
        CartItem copy = new CartItem(productId, productName, brandName, batchNo,
            availableQty, unitPrice, barcode, lastPrice);
        copy.quantity = this.quantity;
        copy.discountPrice = this.discountPrice;
        return copy;
    }
}