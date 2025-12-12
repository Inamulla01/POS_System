package lk.com.pos.dto;

import java.util.Date;

/**
 * Customer Data Transfer Object
 * Represents customer data with credit information
 * 
 * @author POS System
 * @version 1.0
 */
public class CustomerDTO {
    
    // Basic customer information
    private int customerId;
    private String customerName;
    private String phone;
    private String address;
    private String nic;
    private Date registrationDate;
    private String status;
    
    // Credit information
    private Date latestDueDate;
    private double totalCreditAmount;
    private double totalPaid;
    
    /**
     * Default constructor
     */
    public CustomerDTO() {
        this.customerId = 0;
        this.customerName = "";
        this.phone = "";
        this.address = "";
        this.nic = "";
        this.registrationDate = null;
        this.status = "";
        this.latestDueDate = null;
        this.totalCreditAmount = 0.0;
        this.totalPaid = 0.0;
    }
    
    /**
     * Full constructor
     */
    public CustomerDTO(int customerId, String customerName, String phone, String address, 
                      String nic, Date registrationDate, String status, Date latestDueDate,
                      double totalCreditAmount, double totalPaid) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.nic = nic;
        this.registrationDate = registrationDate;
        this.status = status;
        this.latestDueDate = latestDueDate;
        this.totalCreditAmount = totalCreditAmount;
        this.totalPaid = totalPaid;
    }
    
    // Getters and Setters
    
    public int getCustomerId() { 
        return customerId; 
    }
    
    public void setCustomerId(int customerId) { 
        this.customerId = customerId; 
    }
    
    public String getCustomerName() { 
        return customerName; 
    }
    
    public void setCustomerName(String customerName) { 
        this.customerName = customerName; 
    }
    
    public String getPhone() { 
        return phone; 
    }
    
    public void setPhone(String phone) { 
        this.phone = phone; 
    }
    
    public String getAddress() { 
        return address; 
    }
    
    public void setAddress(String address) { 
        this.address = address; 
    }
    
    public String getNic() { 
        return nic; 
    }
    
    public void setNic(String nic) { 
        this.nic = nic; 
    }
    
    public Date getRegistrationDate() { 
        return registrationDate; 
    }
    
    public void setRegistrationDate(Date registrationDate) { 
        this.registrationDate = registrationDate; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
    
    public Date getLatestDueDate() { 
        return latestDueDate; 
    }
    
    public void setLatestDueDate(Date latestDueDate) { 
        this.latestDueDate = latestDueDate; 
    }
    
    public double getTotalCreditAmount() { 
        return totalCreditAmount; 
    }
    
    public void setTotalCreditAmount(double totalCreditAmount) { 
        this.totalCreditAmount = totalCreditAmount; 
    }
    
    public double getTotalPaid() { 
        return totalPaid; 
    }
    
    public void setTotalPaid(double totalPaid) { 
        this.totalPaid = totalPaid; 
    }
    
    // Utility methods
    
    /**
     * Calculates the outstanding amount (credit amount - paid amount)
     * 
     * @return outstanding amount
     */
    public double getOutstanding() {
        return totalCreditAmount - totalPaid;
    }
    
    /**
     * Checks if customer has outstanding balance
     * 
     * @return true if outstanding > 0, false otherwise
     */
    public boolean hasOutstanding() {
        return getOutstanding() > 0;
    }
    
    /**
     * Checks if the customer has missed their due date
     * Returns true only if:
     * 1. There is a due date set
     * 2. There is outstanding balance (totalCreditAmount > totalPaid)
     * 3. The due date is before today
     * 
     * @return true if due date is missed, false otherwise
     */
    public boolean isMissedDueDate() {
        // No due date set
        if (latestDueDate == null) {
            System.out.println("DEBUG: Customer " + customerId + " - No due date set");
            return false;
        }
        
        // No outstanding balance
        double outstanding = getOutstanding();
        if (outstanding <= 0) {
            System.out.println("DEBUG: Customer " + customerId + " - No outstanding balance: " + outstanding);
            return false;
        }
        
        try {
            Date today = new Date();
            boolean isMissed = latestDueDate.before(today);
            System.out.println("DEBUG: Customer " + customerId + " (" + customerName + ") - Due: " + latestDueDate + ", Today: " + today + ", Missed: " + isMissed + ", Outstanding: " + outstanding);
            return isMissed;
        } catch (Exception e) {
            System.err.println("Error checking missed due date for customer " + customerId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if customer has no due (fully paid)
     * 
     * @return true if no outstanding balance, false otherwise
     */
    public boolean hasNoDue() {
        return getOutstanding() <= 0;
    }
    
    /**
     * Checks if this is a high-risk customer (outstanding > threshold)
     * 
     * @param threshold the risk threshold amount
     * @return true if outstanding exceeds threshold, false otherwise
     */
    public boolean isHighRisk(double threshold) {
        return getOutstanding() > threshold;
    }
    
    @Override
    public String toString() {
        return "CustomerDTO{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", phone='" + phone + '\'' +
                ", nic='" + nic + '\'' +
                ", status='" + status + '\'' +
                ", totalCreditAmount=" + totalCreditAmount +
                ", totalPaid=" + totalPaid +
                ", outstanding=" + getOutstanding() +
                ", latestDueDate=" + latestDueDate +
                ", missedDueDate=" + isMissedDueDate() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CustomerDTO that = (CustomerDTO) obj;
        return customerId == that.customerId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(customerId);
    }
}