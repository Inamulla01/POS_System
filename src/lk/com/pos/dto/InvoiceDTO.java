package lk.com.pos.dto;

import java.sql.Timestamp;

public class InvoiceDTO {
    private int salesId;
    private String invoiceNo;
    private Timestamp datetime;
    private double total;
    private double itemDiscount;
    private double saleDiscount;
    private double totalDiscount;
    private String paymentMethod;
    private String cashierName;
    private String customerName;
    private int statusId;
    private String saleStatus;
    
    // Constructors
    public InvoiceDTO() {}
    
    public InvoiceDTO(int salesId, String invoiceNo, Timestamp datetime, double total, 
                     double itemDiscount, double saleDiscount, double totalDiscount, 
                     String paymentMethod, String cashierName, String customerName, 
                     int statusId, String saleStatus) {
        this.salesId = salesId;
        this.invoiceNo = invoiceNo;
        this.datetime = datetime;
        this.total = total;
        this.itemDiscount = itemDiscount;
        this.saleDiscount = saleDiscount;
        this.totalDiscount = totalDiscount;
        this.paymentMethod = paymentMethod;
        this.cashierName = cashierName;
        this.customerName = customerName;
        this.statusId = statusId;
        this.saleStatus = saleStatus;
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
    
    public double getItemDiscount() { return itemDiscount; }
    public void setItemDiscount(double itemDiscount) { this.itemDiscount = itemDiscount; }
    
    public double getSaleDiscount() { return saleDiscount; }
    public void setSaleDiscount(double saleDiscount) { this.saleDiscount = saleDiscount; }
    
    public double getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(double totalDiscount) { this.totalDiscount = totalDiscount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }
    
    public String getSaleStatus() { return saleStatus; }
    public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }
    
    // Helper method to format datetime
    public String getFormattedDateTime() {
        if (datetime == null) return "Unknown Date";
        return datetime.toString();
    }
}