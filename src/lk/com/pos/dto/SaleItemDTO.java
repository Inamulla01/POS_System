package lk.com.pos.dto;

public class SaleItemDTO {
    private String productName;
    private int qty;
    private double price;
    private double discountPrice;
    private double total;
    private String batchNo;
    
    // Constructor
    public SaleItemDTO(String productName, int qty, double price, 
                      double discountPrice, double total, String batchNo) {
        this.productName = productName;
        this.qty = qty;
        this.price = price;
        this.discountPrice = discountPrice;
        this.total = total;
        this.batchNo = batchNo;
    }
    
    // Getters and Setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public double getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(double discountPrice) { this.discountPrice = discountPrice; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
}