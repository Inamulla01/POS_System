package lk.com.pos.dto;

import java.time.LocalDate;

public class TransactionDTO {
    private LocalDate date;
    private String type;
    private String category;
    private String description;
    private double amount;
    private String status;
    
    public TransactionDTO() {}
    
    public TransactionDTO(LocalDate date, String type, String category, 
                         String description, double amount, String status) {
        this.date = date;
        this.type = type;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }
    
    // Getters
    public LocalDate getDate() { return date; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    
    // Setters
    public void setDate(LocalDate date) { this.date = date; }
    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    
    // Helper methods
    public String getFormattedAmount() {
        String prefix = type.equals("Income") ? "+ Rs. " : "- Rs. ";
        return String.format(prefix + "%,.2f", amount);
    }
    
    public String getFormattedDate() {
        return date.toString();
    }
    
    public boolean isIncome() {
        return "Income".equals(type);
    }
    
    public boolean isExpense() {
        return "Expense".equals(type);
    }
}