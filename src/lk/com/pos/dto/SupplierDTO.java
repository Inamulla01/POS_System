package lk.com.pos.dto;

import java.sql.Timestamp;

/**
 * Supplier Data Transfer Object
 * Contains all supplier information for transfer between layers
 * 
 * @author pasin
 * @version 1.0
 */
public class SupplierDTO {
    private int supplierId;
    private String company;
    private String supplierName;
    private String mobile;
    private String regNo;
    private String address;
    private int pStatusId;
    private String status;
    private double creditAmount;
    private double paidAmount;
    private double outstandingAmount;

    // Default constructor
    public SupplierDTO() {
    }

    // Full constructor
    public SupplierDTO(int supplierId, String company, String supplierName, String mobile, 
                      String regNo, String address, int pStatusId, String status,
                      double creditAmount, double paidAmount) {
        this.supplierId = supplierId;
        this.company = company;
        this.supplierName = supplierName;
        this.mobile = mobile;
        this.regNo = regNo;
        this.address = address;
        this.pStatusId = pStatusId;
        this.status = status;
        this.creditAmount = creditAmount;
        this.paidAmount = paidAmount;
        this.outstandingAmount = creditAmount - paidAmount;
    }

    // Getters and Setters
    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPStatusId() {
        return pStatusId;
    }

    public void setPStatusId(int pStatusId) {
        this.pStatusId = pStatusId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(double creditAmount) {
        this.creditAmount = creditAmount;
        calculateOutstanding();
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
        calculateOutstanding();
    }

    public double getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(double outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    private void calculateOutstanding() {
        this.outstandingAmount = this.creditAmount - this.paidAmount;
    }

    // Utility methods
    public boolean isActive() {
        return pStatusId == 1;
    }

    public boolean hasDueAmount() {
        return outstandingAmount > 0;
    }

    @Override
    public String toString() {
        return "SupplierDTO{" +
                "supplierId=" + supplierId +
                ", company='" + company + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", mobile='" + mobile + '\'' +
                ", regNo='" + regNo + '\'' +
                ", status='" + status + '\'' +
                ", creditAmount=" + creditAmount +
                ", paidAmount=" + paidAmount +
                ", outstandingAmount=" + outstandingAmount +
                '}';
    }

    /**
     * Inner class for Payment history
     */
    public static class PaymentDTO {
        private int paymentId;
        private double amount;
        private String description;
        private Timestamp payDate;
        private String userName;
        
        public PaymentDTO() {
        }
        
        public PaymentDTO(int paymentId, double amount, String description, Timestamp payDate, String userName) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.description = description;
            this.payDate = payDate;
            this.userName = userName;
        }
        
        // Getters and setters
        public int getPaymentId() { return paymentId; }
        public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Timestamp getPayDate() { return payDate; }
        public void setPayDate(Timestamp payDate) { this.payDate = payDate; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getFormattedDate() {
            if (payDate != null) {
                return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(payDate);
            }
            return "";
        }
    }
}