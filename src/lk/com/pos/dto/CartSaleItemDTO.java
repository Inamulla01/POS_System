package lk.com.pos.dto;

import java.io.Serializable;

public class CartSaleItemDTO implements Serializable {
    private int saleItemId;
    private int qty;
    private double price;
    private double discountPrice;
    private double total;
    private int salesId;
    private int stockId;
    
    public CartSaleItemDTO() {}
    
    public CartSaleItemDTO(int saleItemId, int qty, double price, 
                          double discountPrice, double total, 
                          int salesId, int stockId) {
        this.saleItemId = saleItemId;
        this.qty = qty;
        this.price = price;
        this.discountPrice = discountPrice;
        this.total = total;
        this.salesId = salesId;
        this.stockId = stockId;
    }
    
    // Getters and Setters
    public int getSaleItemId() { return saleItemId; }
    public void setSaleItemId(int saleItemId) { this.saleItemId = saleItemId; }
    
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public double getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(double discountPrice) { this.discountPrice = discountPrice; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public int getSalesId() { return salesId; }
    public void setSalesId(int salesId) { this.salesId = salesId; }
    
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
}