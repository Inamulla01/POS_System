package lk.com.pos.dto;

import java.io.Serializable;

public class CartStockDTO implements Serializable {
    private int stockId;
    private int productId;
    private String batchNo;
    private int qty;
    private double sellingPrice;
    private double costPrice;
    
    public CartStockDTO() {}
    
    public CartStockDTO(int stockId, int productId, String batchNo, 
                       int qty, double sellingPrice, double costPrice) {
        this.stockId = stockId;
        this.productId = productId;
        this.batchNo = batchNo;
        this.qty = qty;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice;
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
    
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
}