package lk.com.pos.privateclasses;

public class CartItem {
    private int productId;
    private String productName;
    private String brandName;
    private String batchNo;
    private int availableQty;
    private int quantity;
    private double unitPrice;
    private double discountPrice;
    private String barcode;
    
    public CartItem(int productId, String productName, String brandName, 
                   String batchNo, int availableQty, double unitPrice, String barcode) {
        this.productId = productId;
        this.productName = productName;
        this.brandName = brandName;
        this.batchNo = batchNo;
        this.availableQty = availableQty;
        this.quantity = 1;
        this.unitPrice = unitPrice;
        this.discountPrice = 0;
        this.barcode = barcode;
    }
    
    // Getters
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBrandName() { return brandName; }
    public String getBatchNo() { return batchNo; }
    public int getAvailableQty() { return availableQty; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getDiscountPrice() { return discountPrice; }
    public String getBarcode() { return barcode; }
    
    // Setters
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDiscountPrice(double discountPrice) { this.discountPrice = discountPrice; }
    
    public double getTotalPrice() {
        return (unitPrice * quantity) - discountPrice;
    }
    
    public String getKey() {
        return productId + "_" + batchNo;
    }
}