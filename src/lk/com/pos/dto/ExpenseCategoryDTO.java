package lk.com.pos.dto;

public class ExpenseCategoryDTO {
    private String categoryName;
    private double amount;
    private double percentage;
    private String colorCode;
    
    public ExpenseCategoryDTO() {}
    
    public ExpenseCategoryDTO(String categoryName, double amount) {
        this.categoryName = categoryName;
        this.amount = amount;
    }
    
    public ExpenseCategoryDTO(String categoryName, double amount, double percentage) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.percentage = percentage;
    }
    
    // Getters
    public String getCategoryName() { return categoryName; }
    public double getAmount() { return amount; }
    public double getPercentage() { return percentage; }
    public String getColorCode() { return colorCode; }
    
    // Setters
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    
    // Helper methods
    public String getFormattedAmount() {
        return String.format("Rs. %,.2f", amount);
    }
    
    public String getFormattedPercentage() {
        return String.format("%.1f%%", percentage);
    }
}