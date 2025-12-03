package lk.com.pos.dto;

public class FinancialSummaryDTO {
    private double totalIncome;
    private double totalExpense;
    private double netProfit;
    private int transactionCount;
    private double incomeChangePercent;
    private double expenseChangePercent;
    private double profitChangePercent;
    
    public FinancialSummaryDTO() {}
    
    public FinancialSummaryDTO(double totalIncome, double totalExpense, 
                              double netProfit, int transactionCount) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netProfit = netProfit;
        this.transactionCount = transactionCount;
    }
    
    // Getters
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public double getNetProfit() { return netProfit; }
    public int getTransactionCount() { return transactionCount; }
    public double getIncomeChangePercent() { return incomeChangePercent; }
    public double getExpenseChangePercent() { return expenseChangePercent; }
    public double getProfitChangePercent() { return profitChangePercent; }
    
    // Setters
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }
    public void setNetProfit(double netProfit) { this.netProfit = netProfit; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public void setIncomeChangePercent(double incomeChangePercent) { this.incomeChangePercent = incomeChangePercent; }
    public void setExpenseChangePercent(double expenseChangePercent) { this.expenseChangePercent = expenseChangePercent; }
    public void setProfitChangePercent(double profitChangePercent) { this.profitChangePercent = profitChangePercent; }
    
    // Helper methods
    public String getFormattedTotalIncome() {
        return String.format("Rs. %,.2f", totalIncome);
    }
    
    public String getFormattedTotalExpense() {
        return String.format("Rs. %,.2f", totalExpense);
    }
    
    public String getFormattedNetProfit() {
        return String.format("Rs. %,.2f", netProfit);
    }
    
    public String getFormattedTransactionCount() {
        return String.format("%,d", transactionCount);
    }
    
    public boolean isProfitable() {
        return netProfit >= 0;
    }
}