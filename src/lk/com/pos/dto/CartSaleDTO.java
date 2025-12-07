package lk.com.pos.dto;

import java.sql.Timestamp;
import java.io.Serializable;

public class CartSaleDTO implements Serializable {
    private int salesId;
    private String invoiceNo;
    private Timestamp datetime;
    private double total;
    private int userId;
    private Integer paymentMethodId;
    private int statusId;
    private Integer discountId;
    
    public CartSaleDTO() {}
    
    public CartSaleDTO(int salesId, String invoiceNo, Timestamp datetime, 
                      double total, int userId, Integer paymentMethodId, 
                      int statusId, Integer discountId) {
        this.salesId = salesId;
        this.invoiceNo = invoiceNo;
        this.datetime = datetime;
        this.total = total;
        this.userId = userId;
        this.paymentMethodId = paymentMethodId;
        this.statusId = statusId;
        this.discountId = discountId;
    }
    
    // Getters and Setters
    public int getSalesId() { return salesId; }
    public void setSalesId(int salesId) { this.salesId = salesId; }
    
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    
    public Timestamp getDatetime() { return datetime; }
    public void setDatetime(Timestamp datetime) { this.datetime = datetime; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public Integer getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(Integer paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    
    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }
    
    public Integer getDiscountId() { return discountId; }
    public void setDiscountId(Integer discountId) { this.discountId = discountId; }
}