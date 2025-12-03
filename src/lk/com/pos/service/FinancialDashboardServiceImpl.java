package lk.com.pos.service;

import lk.com.pos.dao.FinancialDashboardDAO;
import lk.com.pos.dao.custom.impl.FinancialDashboardDAOImpl;
import lk.com.pos.dto.*;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FinancialDashboardServiceImpl implements FinancialDashboardService {
    
    private final FinancialDashboardDAO financialDashboardDAO;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public FinancialDashboardServiceImpl() {
        this.financialDashboardDAO = new FinancialDashboardDAOImpl();
    }
    
    @Override
    public FinancialSummaryDTO getFinancialSummary(int year, int month) throws Exception {
        return financialDashboardDAO.getFinancialSummary(year, month);
    }
    
    @Override
    public FinancialSummaryDTO getFinancialSummaryWithComparison(int year, int month) throws Exception {
        // Get previous period for comparison
        int previousYear = month == 1 ? year - 1 : year;
        int previousMonth = month == 1 ? 12 : month - 1;
        
        return financialDashboardDAO.getFinancialSummaryWithTrend(year, month, previousYear, previousMonth);
    }
    
    @Override
    public List<ChartDataDTO> getIncomeExpenseTrend(int year, int month) throws Exception {
        return financialDashboardDAO.getIncomeExpenseTrend(year, month);
    }
    
    @Override
    public List<ChartDataDTO> getYearlyComparison(int currentYear) throws Exception {
        int previousYear = currentYear - 1;
        return financialDashboardDAO.getYearlyComparison(currentYear, previousYear);
    }
    
    @Override
    public List<ExpenseCategoryDTO> getExpenseBreakdown(int year, int month) throws Exception {
        List<ExpenseCategoryDTO> breakdown = financialDashboardDAO.getExpenseBreakdown(year, month);
        
        // Assign colors to categories
        String[] colors = {"#08AEEA", "#2AF598", "#08B176", "#F9D423", "#FF6B6B", "#6A11CB", "#2575FC"};
        for (int i = 0; i < breakdown.size(); i++) {
            breakdown.get(i).setColorCode(colors[i % colors.length]);
        }
        
        return breakdown;
    }
    
    @Override
    public List<ExpenseCategoryDTO> getTopExpenseCategories(int year, int month, int limit) throws Exception {
        return financialDashboardDAO.getTopExpenseCategories(year, month, limit);
    }
    
    @Override
    public List<TransactionDTO> getRecentTransactions(int year, int month, int limit) throws Exception {
        return financialDashboardDAO.getRecentTransactions(year, month, limit);
    }
    
    @Override
    public List<TransactionDTO> getTransactionsByDateRange(String startDate, String endDate) throws Exception {
        validateDateRange(startDate, endDate);
        return financialDashboardDAO.getTransactionsByDateRange(startDate, endDate);
    }
    
    @Override
    public List<TransactionDTO> getTopExpenses(int year, int month, int limit) throws Exception {
        return financialDashboardDAO.getTopExpenses(year, month, limit);
    }
    
    @Override
    public List<TransactionDTO> getTopIncome(int year, int month, int limit) throws Exception {
        return financialDashboardDAO.getTopIncome(year, month, limit);
    }
    
    @Override
    public DashboardMetricsDTO getDashboardMetrics(int year, int month) throws Exception {
        DashboardMetricsDTO metrics = new DashboardMetricsDTO();
        
        FinancialSummaryDTO summary = getFinancialSummary(year, month);
        double avgTransaction = financialDashboardDAO.getAverageTransactionValue(year, month);
        int activeDays = financialDashboardDAO.getActiveDaysCount(year, month);
        double growthRate = financialDashboardDAO.getMonthlyGrowthRate(year, month);
        
        metrics.setTotalIncome(summary.getTotalIncome());
        metrics.setTotalExpense(summary.getTotalExpense());
        metrics.setNetProfit(summary.getNetProfit());
        metrics.setTransactionCount(summary.getTransactionCount());
        metrics.setAverageTransactionValue(avgTransaction);
        metrics.setActiveDays(activeDays);
        metrics.setGrowthRate(growthRate);
        metrics.setIncomeChangePercent(summary.getIncomeChangePercent());
        metrics.setExpenseChangePercent(summary.getExpenseChangePercent());
        metrics.setProfitChangePercent(summary.getProfitChangePercent());
        
        // Calculate additional metrics
        metrics.setProfitMargin(summary.getTotalIncome() > 0 ? 
            (summary.getNetProfit() / summary.getTotalIncome()) * 100 : 0);
        metrics.setExpenseRatio(summary.getTotalIncome() > 0 ? 
            (summary.getTotalExpense() / summary.getTotalIncome()) * 100 : 0);
        
        return metrics;
    }
    
    @Override
    public PerformanceMetricsDTO getPerformanceMetrics(int year, int month) throws Exception {
        PerformanceMetricsDTO metrics = new PerformanceMetricsDTO();
        
        FinancialSummaryDTO current = getFinancialSummary(year, month);
        
        // Get previous month for comparison
        int previousYear = month == 1 ? year - 1 : year;
        int previousMonth = month == 1 ? 12 : month - 1;
        FinancialSummaryDTO previous = getFinancialSummary(previousYear, previousMonth);
        
        // Calculate metrics
        metrics.setCurrentRevenue(current.getTotalIncome());
        metrics.setPreviousRevenue(previous.getTotalIncome());
        metrics.setRevenueGrowth(calculateGrowth(current.getTotalIncome(), previous.getTotalIncome()));
        
        metrics.setCurrentProfit(current.getNetProfit());
        metrics.setPreviousProfit(previous.getNetProfit());
        metrics.setProfitGrowth(calculateGrowth(current.getNetProfit(), previous.getNetProfit()));
        
        metrics.setCurrentTransactions(current.getTransactionCount());
        metrics.setPreviousTransactions(previous.getTransactionCount());
        metrics.setTransactionGrowth(calculateGrowth(current.getTransactionCount(), previous.getTransactionCount()));
        
        metrics.setCurrentExpenses(current.getTotalExpense());
        metrics.setPreviousExpenses(previous.getTotalExpense());
        metrics.setExpenseGrowth(calculateGrowth(current.getTotalExpense(), previous.getTotalExpense()));
        
        // Calculate efficiency metrics
        metrics.setAverageTransactionValue(financialDashboardDAO.getAverageTransactionValue(year, month));
        metrics.setCustomerRetentionRate(calculateRetentionRate(year, month));
        metrics.setRevenuePerTransaction(current.getTotalIncome() / Math.max(current.getTransactionCount(), 1));
        
        return metrics;
    }
    
    @Override
    public List<TransactionDTO> getTransactionsByCategory(String category, int year, int month) throws Exception {
        validateCategory(category);
        return financialDashboardDAO.getTransactionsByCategory(category, year, month);
    }
    
    @Override
    public List<TransactionDTO> getTransactionsByType(String type, int year, int month) throws Exception {
        validateTransactionType(type);
        return financialDashboardDAO.getTransactionsByType(type, year, month);
    }
    
    @Override
    public DashboardDataDTO getCompleteDashboardData(int year, int month) throws Exception {
        DashboardDataDTO dashboardData = new DashboardDataDTO();
        
        // Set summary
        dashboardData.setSummary(getFinancialSummaryWithComparison(year, month));
        
        // Set charts data
        dashboardData.setIncomeExpenseTrend(getIncomeExpenseTrend(year, month));
        dashboardData.setExpenseBreakdown(getExpenseBreakdown(year, month));
        dashboardData.setYearlyComparison(getYearlyComparison(year));
        
        // Set transactions
        dashboardData.setRecentTransactions(getRecentTransactions(year, month, 15));
        dashboardData.setTopExpenses(getTopExpenses(year, month, 5));
        dashboardData.setTopIncome(getTopIncome(year, month, 5));
        
        // Set metrics
        dashboardData.setMetrics(getDashboardMetrics(year, month));
        dashboardData.setPerformanceMetrics(getPerformanceMetrics(year, month));
        
        // Set metadata
        dashboardData.setYear(year);
        dashboardData.setMonth(month);
        dashboardData.setGeneratedDate(LocalDate.now());
        dashboardData.setPeriodLabel(getPeriodLabel(year, month));
        
        return dashboardData;
    }
    
    @Override
    public List<Integer> getAvailableYears() throws Exception {
        return financialDashboardDAO.getAvailableYears();
    }
    
    @Override
    public List<String> getIncomeCategories() throws Exception {
        return financialDashboardDAO.getIncomeCategories();
    }
    
    @Override
    public List<String> getExpenseCategories() throws Exception {
        return financialDashboardDAO.getExpenseCategories();
    }
    
    @Override
    public String exportToCSV(int year, int month) throws Exception {
        // Implementation for CSV export
        DashboardDataDTO data = getCompleteDashboardData(year, month);
        
        StringBuilder csv = new StringBuilder();
        csv.append("Financial Dashboard Export\n");
        csv.append("Period: ").append(getPeriodLabel(year, month)).append("\n");
        csv.append("Generated: ").append(LocalDate.now()).append("\n\n");
        
        // Summary section
        csv.append("SUMMARY\n");
        csv.append("Total Income,").append(data.getSummary().getFormattedTotalIncome()).append("\n");
        csv.append("Total Expense,").append(data.getSummary().getFormattedTotalExpense()).append("\n");
        csv.append("Net Profit,").append(data.getSummary().getFormattedNetProfit()).append("\n");
        csv.append("Transaction Count,").append(data.getSummary().getFormattedTransactionCount()).append("\n\n");
        
        // Recent Transactions
        csv.append("RECENT TRANSACTIONS\n");
        csv.append("Date,Type,Category,Description,Amount,Status\n");
        for (TransactionDTO transaction : data.getRecentTransactions()) {
            csv.append(transaction.getFormattedDate()).append(",")
               .append(transaction.getType()).append(",")
               .append(transaction.getCategory()).append(",")
               .append(transaction.getDescription()).append(",")
               .append(transaction.getFormattedAmount()).append(",")
               .append(transaction.getStatus()).append("\n");
        }
        
        return csv.toString();
    }
    
    @Override
    public String exportToPDF(int year, int month) throws Exception {
        // Placeholder for PDF export implementation
        // In real implementation, use libraries like iText or Apache PDFBox
        throw new Exception("PDF export not yet implemented");
    }
    
    @Override
    public boolean validateYear(int year) throws Exception {
        List<Integer> availableYears = getAvailableYears();
        return availableYears.contains(year);
    }
    
    @Override
    public boolean validateMonth(int month) throws Exception {
        return month >= 0 && month <= 12;
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private double calculateGrowth(double current, double previous) {
        if (previous == 0) return 0;
        return ((current - previous) / previous) * 100;
    }
    
    private double calculateRetentionRate(int year, int month) throws Exception {
        // Simplified calculation - in real app, use customer data
        try {
            // This is a placeholder calculation
            int currentTransactions = financialDashboardDAO.getTotalTransactionsCount(year, month);
            
            // Get previous month count
            int previousYear = month == 1 ? year - 1 : year;
            int previousMonth = month == 1 ? 12 : month - 1;
            int previousTransactions = financialDashboardDAO.getTotalTransactionsCount(previousYear, previousMonth);
            
            if (previousTransactions > 0) {
                return (double) currentTransactions / previousTransactions * 100;
            }
        } catch (Exception e) {
            // Return default value if calculation fails
        }
        
        return 75.0; // Default value
    }
    
    private void validateDateRange(String startDate, String endDate) throws Exception {
        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            
            if (start.isAfter(end)) {
                throw new Exception("Start date cannot be after end date");
            }
            
            if (start.isAfter(LocalDate.now())) {
                throw new Exception("Start date cannot be in the future");
            }
            
        } catch (Exception e) {
            throw new Exception("Invalid date format. Use yyyy-MM-dd", e);
        }
    }
    
    private void validateCategory(String category) throws Exception {
        if (category == null || category.trim().isEmpty()) {
            throw new Exception("Category cannot be empty");
        }
    }
    
    private void validateTransactionType(String type) throws Exception {
        if (!"Income".equalsIgnoreCase(type) && !"Expense".equalsIgnoreCase(type)) {
            throw new Exception("Transaction type must be 'Income' or 'Expense'");
        }
    }
    
    private String getPeriodLabel(int year, int month) {
        if (month == 0) {
            return String.valueOf(year);
        } else {
            String[] monthNames = {"January", "February", "March", "April", "May", "June",
                                  "July", "August", "September", "October", "November", "December"};
            return monthNames[month - 1] + " " + year;
        }
    }
}