package lk.com.pos.dto;

public class ChartDataDTO {
    private String period;
    private double income;
    private double expense;
    private double profit;
    
    public ChartDataDTO() {}
    
    public ChartDataDTO(String period, double income, double expense) {
        this.period = period;
        this.income = income;
        this.expense = expense;
        this.profit = income - expense;
    }
    
    // Getters
    public String getPeriod() { return period; }
    public double getIncome() { return income; }
    public double getExpense() { return expense; }
    public double getProfit() { return profit; }
    
    // Setters
    public void setPeriod(String period) { this.period = period; }
    public void setIncome(double income) { this.income = income; }
    public void setExpense(double expense) { this.expense = expense; }
    public void setProfit(double profit) { this.profit = profit; }
    
    // Helper methods
    public String getFormattedIncome() {
        return String.format("%,.2f", income);
    }
    
    public String getFormattedExpense() {
        return String.format("%,.2f", expense);
    }
    
    public String getFormattedProfit() {
        return String.format("%,.2f", profit);
    }
}