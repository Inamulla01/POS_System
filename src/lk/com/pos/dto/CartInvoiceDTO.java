package lk.com.pos.dto;

import java.sql.Timestamp;
import java.io.Serializable;

public class CartInvoiceDTO implements Serializable {
    private int salesId;
    private String invoiceNo;
    private Timestamp datetime;
    private String status;
    private double total;
    private String paymentMethod;
    private int userId;
    
    public CartInvoiceDTO() {}
    
    public CartInvoiceDTO(int salesId, String invoiceNo, Timestamp datetime, 
                         String status, double total, String paymentMethod, int userId) {
        this.salesId = salesId;
        this.invoiceNo = invoiceNo;
        this.datetime = datetime;
        this.status = status;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.userId = userId;
    }
    
    // Getters and Setters
    public int getSalesId() { return salesId; }
    public void setSalesId(int salesId) { this.salesId = salesId; }
    
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    
    public Timestamp getDatetime() { return datetime; }
    public void setDatetime(Timestamp datetime) { this.datetime = datetime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}