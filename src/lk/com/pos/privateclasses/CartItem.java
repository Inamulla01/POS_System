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
    private double discountPrice; // ✅ This stores PER-UNIT discount
    private double lastPrice; // ✅ ADD THIS: last_price from database

    public CartItem(int productId, String productName, String brandName,
                    String batchNo, int availableQty, double unitPrice, String barcode, double lastPrice) { // ✅ UPDATE CONSTRUCTOR
        this.productId = productId;
        this.productName = productName;
        this.brandName = brandName;
        this.batchNo = batchNo;
        this.availableQty = availableQty;
        this.unitPrice = unitPrice;
        this.barcode = barcode;
        this.quantity = 1;
        this.discountPrice = 0; // Default: no discount
        this.lastPrice = lastPrice; // ✅ SET last_price
    }

    // ✅ CRITICAL FIX: Calculate total price correctly
    public double getTotalPrice() {
        double baseTotal = unitPrice * quantity;
        // discountPrice stores PER-UNIT discount, so multiply by quantity
        double totalDiscount = discountPrice * quantity;
        return baseTotal - totalDiscount;
    }

    public String getKey() {
        return productId + "_" + batchNo;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getBarcode() {
        return barcode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ✅ Returns PER-UNIT discount
    public double getDiscountPrice() {
        return discountPrice;
    }

    // ✅ Stores PER-UNIT discount
    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    // ✅ ADD GETTER AND SETTER FOR lastPrice
    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }
}