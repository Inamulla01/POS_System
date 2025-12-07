package lk.com.pos.dto;

public class PosPanelDTO {
    private int productId;
    private String productName;
    private String brandName;
    private String batchNo;
    private int qty;
    private double sellingPrice;
    private String barcode;
    private double lastPrice;

    // Constructor
    public PosPanelDTO(int productId, String productName, String brandName, String batchNo,
                      int qty, double sellingPrice, String barcode, double lastPrice) {
        this.productId = productId;
        this.productName = productName;
        this.brandName = brandName;
        this.batchNo = batchNo;
        this.qty = qty;
        this.sellingPrice = sellingPrice;
        this.barcode = barcode;
        this.lastPrice = lastPrice;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getLastPrice() { return lastPrice; }
    public void setLastPrice(double lastPrice) { this.lastPrice = lastPrice; }

    @Override
    public String toString() {
        return "PosPanelDTO{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", qty=" + qty +
                ", sellingPrice=" + sellingPrice +
                ", barcode='" + barcode + '\'' +
                ", lastPrice=" + lastPrice +
                '}';
    }
}