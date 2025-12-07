package lk.com.pos.dto;

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
    private int reorderLevel;
    
    // Additional fields for display
    private String brandName;
    private String categoryName;
    private String supplierName;
    
    // Constructors
    public ProductDTO() {}
    
    public ProductDTO(int productId, String productName, String barcode, 
                     int brandId, int categoryId, int pStatusId, 
                     int reorderLevel) {
        this.productId = productId;
        this.productName = productName;
        this.barcode = barcode;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.pStatusId = pStatusId;
        this.reorderLevel = reorderLevel;
    }
    
    public ProductDTO(int productId, String productName, String barcode,
                     String brandName, String categoryName, int pStatusId) {
        this.productId = productId;
        this.productName = productName;
        this.barcode = barcode;
        this.brandName = brandName;
        this.categoryName = categoryName;
        this.pStatusId = pStatusId;
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
    
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    
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
    
    @Override
    public String toString() {
        return productName + " (" + barcode + ")";
    }
}