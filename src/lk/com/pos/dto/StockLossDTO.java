package lk.com.pos.dto;

import java.util.Date;

public class StockLossDTO {
    private int stockLossId;
    private int qty;
    private Date stockLossDate;
    private String productName;
    private String batchNo;
    private double sellingPrice;
    private String userName;
    private String reason;
    private String invoiceNo;
    
    public StockLossDTO() {
    }

    public StockLossDTO(int stockLossId, int qty, Date stockLossDate, String productName, 
                       String batchNo, double sellingPrice, String userName, 
                       String reason, String invoiceNo) {
        this.stockLossId = stockLossId;
        this.qty = qty;
        this.stockLossDate = stockLossDate;
        this.productName = productName;
        this.batchNo = batchNo;
        this.sellingPrice = sellingPrice;
        this.userName = userName;
        this.reason = reason;
        this.invoiceNo = invoiceNo;
    }

    // Getters and Setters
    public int getStockLossId() {
        return stockLossId;
    }

    public void setStockLossId(int stockLossId) {
        this.stockLossId = stockLossId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Date getStockLossDate() {
        return stockLossDate;
    }

    public void setStockLossDate(Date stockLossDate) {
        this.stockLossDate = stockLossDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    // Helper method to calculate loss amount
    public double getLossAmount() {
        return qty * sellingPrice;
    }
    
    @Override
    public String toString() {
        return "StockLossDTO{" +
                "stockLossId=" + stockLossId +
                ", qty=" + qty +
                ", stockLossDate=" + stockLossDate +
                ", productName='" + productName + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", sellingPrice=" + sellingPrice +
                ", userName='" + userName + '\'' +
                ", reason='" + reason + '\'' +
                ", invoiceNo='" + invoiceNo + '\'' +
                '}';
    }
}