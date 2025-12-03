package lk.com.pos.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ProductDTO - Data Transfer Object for Product entity
 * @author pasin
 */
public class ProductDTO {
    private int productId;
    private String productName;
    private String barcode;
    private int brandId;
    private int categoryId;
    private int pStatusId;
    private BigDecimal unitPrice;
    private BigDecimal sellingPrice;
    private int reorderLevel;
    private int stockId;
    private int suppliersId;
    private int quantity;
    private Date stockDate;
    private BigDecimal buyingPrice;
    
    // Additional fields for display
    private String brandName;
    private String categoryName;
    private String supplierName;
    private String statusName;
    
    // Constructors
    public ProductDTO() {}
    
    // For basic product operations
    public ProductDTO(int productId, String productName, String barcode, 
                     int brandId, int categoryId, int pStatusId, 
                     BigDecimal unitPrice, BigDecimal sellingPrice, 
                     int reorderLevel) {
        this.productId = productId;
        this.productName = productName;
        this.barcode = barcode;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.pStatusId = pStatusId;
        this.unitPrice = unitPrice;
        this.sellingPrice = sellingPrice;
        this.reorderLevel = reorderLevel;
    }
    
    // For display purposes
    public ProductDTO(int productId, String productName, String barcode,
                     String brandName, String categoryName, int pStatusId,
                     BigDecimal unitPrice, BigDecimal sellingPrice) {
        this.productId = productId;
        this.productName = productName;
        this.barcode = barcode;
        this.brandName = brandName;
        this.categoryName = categoryName;
        this.pStatusId = pStatusId;
        this.unitPrice = unitPrice;
        this.sellingPrice = sellingPrice;
    }
    
    // For complete product with stock
    public ProductDTO(int productId, String productName, String barcode,
                     int brandId, int categoryId, int pStatusId,
                     BigDecimal unitPrice, BigDecimal sellingPrice, int reorderLevel,
                     int stockId, int suppliersId, int quantity, Date stockDate, BigDecimal buyingPrice) {
        this.productId = productId;
        this.productName = productName;
        this.barcode = barcode;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.pStatusId = pStatusId;
        this.unitPrice = unitPrice;
        this.sellingPrice = sellingPrice;
        this.reorderLevel = reorderLevel;
        this.stockId = stockId;
        this.suppliersId = suppliersId;
        this.quantity = quantity;
        this.stockDate = stockDate;
        this.buyingPrice = buyingPrice;
    }
    
    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public int getBrandId() { return brandId; }
    public void setBrandId(int brandId) { this.brandId = brandId; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public int getPStatusId() { return pStatusId; }
    public void setPStatusId(int pStatusId) { this.pStatusId = pStatusId; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    
    public int getSuppliersId() { return suppliersId; }
    public void setSuppliersId(int suppliersId) { this.suppliersId = suppliersId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public Date getStockDate() { return stockDate; }
    public void setStockDate(Date stockDate) { this.stockDate = stockDate; }
    
    public BigDecimal getBuyingPrice() { return buyingPrice; }
    public void setBuyingPrice(BigDecimal buyingPrice) { this.buyingPrice = buyingPrice; }
    
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public String getStatusName() { 
        if (pStatusId == 1) return "Active";
        else if (pStatusId == 2) return "Inactive";
        return "Unknown";
    }
    
    public void setStatusName(String statusName) { this.statusName = statusName; }
    
    @Override
    public String toString() {
        return productName + " (" + barcode + ")";
    }
}