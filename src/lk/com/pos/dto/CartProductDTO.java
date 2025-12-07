package lk.com.pos.dto;

import java.io.Serializable;

public class CartProductDTO implements Serializable {
    private int productId;
    private String productName;
    private String barcode;
    private int brandId;
    private String brandName;
    
    public CartProductDTO() {}
    
    public CartProductDTO(int productId, String productName, String barcode, 
                         int brandId, String brandName) {
        this.productId = productId;
        this.productName = productName;
        this.barcode = barcode;
        this.brandId = brandId;
        this.brandName = brandName;
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
    
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
}