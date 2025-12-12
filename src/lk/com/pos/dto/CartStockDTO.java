package lk.com.pos.dto;

public class CartStockDTO {
    private int stockId;
    private int productId;
    private String batchNo;
    private int qty;
    private double sellingPrice;
    private double lastPrice; // Renamed from costPrice to match database column
    
    // Constructor without last_price
    public CartStockDTO(int stockId, int productId, String batchNo, int qty, double sellingPrice) {
        this.stockId = stockId;
        this.productId = productId;
        this.batchNo = batchNo;
        this.qty = qty;
        this.sellingPrice = sellingPrice;
        this.lastPrice = 0.0; // Default value
    }
    
    // Constructor with last_price
    public CartStockDTO(int stockId, int productId, String batchNo, int qty, double sellingPrice, double lastPrice) {
        this.stockId = stockId;
        this.productId = productId;
        this.batchNo = batchNo;
        this.qty = qty;
        this.sellingPrice = sellingPrice;
        this.lastPrice = lastPrice;
    }
    
    // Getters and Setters
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public double getLastPrice() { return lastPrice; }
    public void setLastPrice(double lastPrice) { this.lastPrice = lastPrice; }
    
    // Backward compatibility methods (if other code uses getCostPrice)
    @Deprecated
    public double getCostPrice() { return lastPrice; }
    
    @Deprecated
    public void setCostPrice(double costPrice) { this.lastPrice = costPrice; }
}