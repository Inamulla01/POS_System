package lk.com.pos.dto;

public class PerformanceMetricsDTO {
    private double currentRevenue;
    private double previousRevenue;
    private double revenueGrowth;
    private double currentProfit;
    private double previousProfit;
    private double profitGrowth;
    private int currentTransactions;
    private int previousTransactions;
    private double transactionGrowth;
    private double currentExpenses;
    private double previousExpenses;
    private double expenseGrowth;
    private double averageTransactionValue;
    private double customerRetentionRate;
    private double revenuePerTransaction;
    
    // Getters and Setters
    public double getCurrentRevenue() { return currentRevenue; }
    public void setCurrentRevenue(double currentRevenue) { this.currentRevenue = currentRevenue; }
    
    public double getPreviousRevenue() { return previousRevenue; }
    public void setPreviousRevenue(double previousRevenue) { this.previousRevenue = previousRevenue; }
    
    public double getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(double revenueGrowth) { this.revenueGrowth = revenueGrowth; }
    
    public double getCurrentProfit() { return currentProfit; }
    public void setCurrentProfit(double currentProfit) { this.currentProfit = currentProfit; }
    
    public double getPreviousProfit() { return previousProfit; }
    public void setPreviousProfit(double previousProfit) { this.previousProfit = previousProfit; }
    
    public double getProfitGrowth() { return profitGrowth; }
    public void setProfitGrowth(double profitGrowth) { this.profitGrowth = profitGrowth; }
    
    public int getCurrentTransactions() { return currentTransactions; }
    public void setCurrentTransactions(int currentTransactions) { this.currentTransactions = currentTransactions; }
    
    public int getPreviousTransactions() { return previousTransactions; }
    public void setPreviousTransactions(int previousTransactions) { this.previousTransactions = previousTransactions; }
    
    public double getTransactionGrowth() { return transactionGrowth; }
    public void setTransactionGrowth(double transactionGrowth) { this.transactionGrowth = transactionGrowth; }
    
    public double getCurrentExpenses() { return currentExpenses; }
    public void setCurrentExpenses(double currentExpenses) { this.currentExpenses = currentExpenses; }
    
    public double getPreviousExpenses() { return previousExpenses; }
    public void setPreviousExpenses(double previousExpenses) { this.previousExpenses = previousExpenses; }
    
    public double getExpenseGrowth() { return expenseGrowth; }
    public void setExpenseGrowth(double expenseGrowth) { this.expenseGrowth = expenseGrowth; }
    
    public double getAverageTransactionValue() { return averageTransactionValue; }
    public void setAverageTransactionValue(double averageTransactionValue) { this.averageTransactionValue = averageTransactionValue; }
    
    public double getCustomerRetentionRate() { return customerRetentionRate; }
    public void setCustomerRetentionRate(double customerRetentionRate) { this.customerRetentionRate = customerRetentionRate; }
    
    public double getRevenuePerTransaction() { return revenuePerTransaction; }
    public void setRevenuePerTransaction(double revenuePerTransaction) { this.revenuePerTransaction = revenuePerTransaction; }
    
    // Helper methods
    public String getFormattedRevenueGrowth() {
        return String.format("%+.1f%%", revenueGrowth);
    }
    
    public String getFormattedProfitGrowth() {
        return String.format("%+.1f%%", profitGrowth);
    }
    
    public String getFormattedTransactionGrowth() {
        return String.format("%+.1f%%", transactionGrowth);
    }
    
    public String getFormattedExpenseGrowth() {
        return String.format("%+.1f%%", expenseGrowth);
    }
    
    public String getFormattedRetentionRate() {
        return String.format("%.1f%%", customerRetentionRate);
    }
}