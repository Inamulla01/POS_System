package lk.com.pos.dto;

public class DashboardMetricsDTO {
    private double totalIncome;
    private double totalExpense;
    private double netProfit;
    private int transactionCount;
    private double averageTransactionValue;
    private int activeDays;
    private double growthRate;
    private double profitMargin;
    private double expenseRatio;
    private double incomeChangePercent;
    private double expenseChangePercent;
    private double profitChangePercent;
    
    // Getters and Setters
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }
    
    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }
    
    public double getNetProfit() { return netProfit; }
    public void setNetProfit(double netProfit) { this.netProfit = netProfit; }
    
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    
    public double getAverageTransactionValue() { return averageTransactionValue; }
    public void setAverageTransactionValue(double averageTransactionValue) { this.averageTransactionValue = averageTransactionValue; }
    
    public int getActiveDays() { return activeDays; }
    public void setActiveDays(int activeDays) { this.activeDays = activeDays; }
    
    public double getGrowthRate() { return growthRate; }
    public void setGrowthRate(double growthRate) { this.growthRate = growthRate; }
    
    public double getProfitMargin() { return profitMargin; }
    public void setProfitMargin(double profitMargin) { this.profitMargin = profitMargin; }
    
    public double getExpenseRatio() { return expenseRatio; }
    public void setExpenseRatio(double expenseRatio) { this.expenseRatio = expenseRatio; }
    
    public double getIncomeChangePercent() { return incomeChangePercent; }
    public void setIncomeChangePercent(double incomeChangePercent) { this.incomeChangePercent = incomeChangePercent; }
    
    public double getExpenseChangePercent() { return expenseChangePercent; }
    public void setExpenseChangePercent(double expenseChangePercent) { this.expenseChangePercent = expenseChangePercent; }
    
    public double getProfitChangePercent() { return profitChangePercent; }
    public void setProfitChangePercent(double profitChangePercent) { this.profitChangePercent = profitChangePercent; }
    
    // Helper methods
    public String getFormattedProfitMargin() {
        return String.format("%.1f%%", profitMargin);
    }
    
    public String getFormattedExpenseRatio() {
        return String.format("%.1f%%", expenseRatio);
    }
    
    public String getFormattedGrowthRate() {
        return String.format("%+.1f%%", growthRate);
    }
}