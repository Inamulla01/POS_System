package lk.com.pos.dto;

import java.time.LocalDate;
import java.util.List;

public class DashboardDataDTO {
    private FinancialSummaryDTO summary;
    private List<ChartDataDTO> incomeExpenseTrend;
    private List<ExpenseCategoryDTO> expenseBreakdown;
    private List<ChartDataDTO> yearlyComparison;
    private List<TransactionDTO> recentTransactions;
    private List<TransactionDTO> topExpenses;
    private List<TransactionDTO> topIncome;
    private DashboardMetricsDTO metrics;
    private PerformanceMetricsDTO performanceMetrics;
    private int year;
    private int month;
    private LocalDate generatedDate;
    private String periodLabel;
    
    // Getters and Setters
    public FinancialSummaryDTO getSummary() { return summary; }
    public void setSummary(FinancialSummaryDTO summary) { this.summary = summary; }
    
    public List<ChartDataDTO> getIncomeExpenseTrend() { return incomeExpenseTrend; }
    public void setIncomeExpenseTrend(List<ChartDataDTO> incomeExpenseTrend) { this.incomeExpenseTrend = incomeExpenseTrend; }
    
    public List<ExpenseCategoryDTO> getExpenseBreakdown() { return expenseBreakdown; }
    public void setExpenseBreakdown(List<ExpenseCategoryDTO> expenseBreakdown) { this.expenseBreakdown = expenseBreakdown; }
    
    public List<ChartDataDTO> getYearlyComparison() { return yearlyComparison; }
    public void setYearlyComparison(List<ChartDataDTO> yearlyComparison) { this.yearlyComparison = yearlyComparison; }
    
    public List<TransactionDTO> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<TransactionDTO> recentTransactions) { this.recentTransactions = recentTransactions; }
    
    public List<TransactionDTO> getTopExpenses() { return topExpenses; }
    public void setTopExpenses(List<TransactionDTO> topExpenses) { this.topExpenses = topExpenses; }
    
    public List<TransactionDTO> getTopIncome() { return topIncome; }
    public void setTopIncome(List<TransactionDTO> topIncome) { this.topIncome = topIncome; }
    
    public DashboardMetricsDTO getMetrics() { return metrics; }
    public void setMetrics(DashboardMetricsDTO metrics) { this.metrics = metrics; }
    
    public PerformanceMetricsDTO getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(PerformanceMetricsDTO performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    
    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }
    
    public String getPeriodLabel() { return periodLabel; }
    public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
    
    // Helper methods
    public boolean hasData() {
        return summary != null && 
               incomeExpenseTrend != null && !incomeExpenseTrend.isEmpty() &&
               expenseBreakdown != null && !expenseBreakdown.isEmpty();
    }
    
    public String getGeneratedDateFormatted() {
        return generatedDate.toString();
    }
}