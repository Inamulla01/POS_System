/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.com.pos.privateclasses;


import java.util.Date;

public class Invoice {
    private int salesId;
    private String invoiceNo;
    private Date date;
    private String status;
    private double total;
    private String customerName;
    private String paymentMethod;
    private int userId;
    
    // Constructors
    public Invoice() {}
    
    public Invoice(int salesId, String invoiceNo, Date date, String status, double total, 
                  String customerName, String paymentMethod, int userId) {
        this.salesId = salesId;
        this.invoiceNo = invoiceNo;
        this.date = date;
        this.status = status;
        this.total = total;
        this.customerName = customerName;
        this.paymentMethod = paymentMethod;
        this.userId = userId;
    }
    
    // Getters and Setters
    public int getSalesId() { return salesId; }
    public void setSalesId(int salesId) { this.salesId = salesId; }
    
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    @Override
    public String toString() {
        return "Invoice{" +
                "salesId=" + salesId +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", total=" + total +
                ", customerName='" + customerName + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", userId=" + userId +
                '}';
    }
}