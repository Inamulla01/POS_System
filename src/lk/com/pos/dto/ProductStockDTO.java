package lk.com.pos.dto;

import java.time.LocalDate;

public class ProductStockDTO {
    private int productId;
    private int stockId;
    private int pStatusId;
    private int qty;
    private String productName;
    private String supplierName;
    private String brandName;
    private String categoryName;
    private String expiryDate;
    private String batchNo;
    private String barcode;
    private double purchasePrice;
    private double lastPrice;
    private double sellingPrice;
    private int batchIndex;
    private int totalBatches;
    private int priorityScore;
    
    // Constructors
    public ProductStockDTO() {}
    
    public ProductStockDTO(int productId, int stockId, int pStatusId, int qty, String productName, 
                          String supplierName, String brandName, String categoryName, 
                          String expiryDate, String batchNo, String barcode, 
                          double purchasePrice, double lastPrice, double sellingPrice) {
        this.productId = productId;
        this.stockId = stockId;
        this.pStatusId = pStatusId;
        this.qty = qty;
        this.productName = productName;
        this.supplierName = supplierName;
        this.brandName = brandName;
        this.categoryName = categoryName;
        this.expiryDate = expiryDate;
        this.batchNo = batchNo;
        this.barcode = barcode;
        this.purchasePrice = purchasePrice;
        this.lastPrice = lastPrice;
        this.sellingPrice = sellingPrice;
    }
    
    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    
    public int getPStatusId() { return pStatusId; }
    public void setPStatusId(int pStatusId) { this.pStatusId = pStatusId; }
    
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    
    public double getLastPrice() { return lastPrice; }
    public void setLastPrice(double lastPrice) { this.lastPrice = lastPrice; }
    
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public int getBatchIndex() { return batchIndex; }
    public void setBatchIndex(int batchIndex) { this.batchIndex = batchIndex; }
    
    public int getTotalBatches() { return totalBatches; }
    public void setTotalBatches(int totalBatches) { this.totalBatches = totalBatches; }
    
    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }
    
    // Utility methods
    public boolean isExpired() {
        if (expiryDate == null || expiryDate.isEmpty()) return false;
        try {
            LocalDate expiry = LocalDate.parse(expiryDate);
            return expiry.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isExpiringSoon() {
        if (expiryDate == null || expiryDate.isEmpty()) return false;
        try {
            LocalDate expiry = LocalDate.parse(expiryDate);
            LocalDate threeMonthsLater = LocalDate.now().plusMonths(3);
            return !expiry.isBefore(LocalDate.now()) && !expiry.isAfter(threeMonthsLater);
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isLowStock() {
        return qty < 10;
    }
    
    public boolean isInactive() {
        return pStatusId == 2;
    }
    
    @Override
    public String toString() {
        return productName + " (" + batchNo + ")";
    }
}