package lk.com.pos.dto;

/**
 * ChequeDTO - Data Transfer Object for Cheque data
 * Maintains same structure as your original ChequeCardData
 */
public class ChequeDTO {
    private int chequeId;
    private String chequeNo;
    private String customerName;
    private String invoiceNo;
    private String bankName;
    private String branch;
    private String phone;
    private String nic;
    private String address;
    private String givenDate;
    private String chequeDate;
    private String chequeType;
    private double chequeAmount;
    private int salesId;

    // Constructors
    public ChequeDTO() {}

    public ChequeDTO(int chequeId, String chequeNo, String customerName, String invoiceNo, 
                    String bankName, String branch, String phone, String nic, String address,
                    String givenDate, String chequeDate, String chequeType, double chequeAmount, int salesId) {
        this.chequeId = chequeId;
        this.chequeNo = chequeNo;
        this.customerName = customerName;
        this.invoiceNo = invoiceNo;
        this.bankName = bankName;
        this.branch = branch;
        this.phone = phone;
        this.nic = nic;
        this.address = address;
        this.givenDate = givenDate;
        this.chequeDate = chequeDate;
        this.chequeType = chequeType;
        this.chequeAmount = chequeAmount;
        this.salesId = salesId;
    }

    // Getters and Setters
    public int getChequeId() { return chequeId; }
    public void setChequeId(int chequeId) { this.chequeId = chequeId; }
    
    public String getChequeNo() { return chequeNo; }
    public void setChequeNo(String chequeNo) { this.chequeNo = chequeNo; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getGivenDate() { return givenDate; }
    public void setGivenDate(String givenDate) { this.givenDate = givenDate; }
    
    public String getChequeDate() { return chequeDate; }
    public void setChequeDate(String chequeDate) { this.chequeDate = chequeDate; }
    
    public String getChequeType() { return chequeType; }
    public void setChequeType(String chequeType) { this.chequeType = chequeType; }
    
    public double getChequeAmount() { return chequeAmount; }
    public void setChequeAmount(double chequeAmount) { this.chequeAmount = chequeAmount; }
    
    public int getSalesId() { return salesId; }
    public void setSalesId(int salesId) { this.salesId = salesId; }
}