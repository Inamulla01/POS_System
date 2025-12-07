package lk.com.pos.dto;

public class ReturnItemDetailsDTO {
    private String productName;
    private String qty;
    private double price;
    private double discountPrice;
    private double total;
    private String batchNo;

    // Getters and Setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(double discountPrice) { this.discountPrice = discountPrice; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
}