package lk.com.pos.dto;

import java.util.Date;

/**
 * Customer Data Transfer Object
 * Represents customer data with credit information
 */
public class CustomerDTO {
    private int customerId;
    private String customerName;
    private String phone;
    private String address;
    private String nic;
    private Date registrationDate;
    private String status;
    private Date latestDueDate;
    private double totalCreditAmount;
    private double totalPaid;

    // Constructors
    public CustomerDTO() {}

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
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getLatestDueDate() { return latestDueDate; }
    public void setLatestDueDate(Date latestDueDate) { this.latestDueDate = latestDueDate; }

    public double getTotalCreditAmount() { return totalCreditAmount; }
    public void setTotalCreditAmount(double totalCreditAmount) { this.totalCreditAmount = totalCreditAmount; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    // Utility methods
    public double getOutstanding() {
        return totalCreditAmount - totalPaid;
    }

    public boolean hasOutstanding() {
        return getOutstanding() > 0;
    }

    public boolean isMissedDueDate() {
        if (latestDueDate == null || getOutstanding() <= 0) {
            return false;
        }
        return latestDueDate.before(new Date());
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", phone='" + phone + '\'' +
                ", nic='" + nic + '\'' +
                ", totalCreditAmount=" + totalCreditAmount +
                ", totalPaid=" + totalPaid +
                ", outstanding=" + getOutstanding() +
                '}';
    }
}