package lk.com.pos.service;

import lk.com.pos.dto.*;
import java.util.List;

public interface FinancialDashboardService {
    // Summary methods
    FinancialSummaryDTO getFinancialSummary(int year, int month) throws Exception;
    FinancialSummaryDTO getFinancialSummaryWithComparison(int year, int month) throws Exception;
    
    // Chart methods
    List<ChartDataDTO> getIncomeExpenseTrend(int year, int month) throws Exception;
    List<ChartDataDTO> getYearlyComparison(int currentYear) throws Exception;
    List<ExpenseCategoryDTO> getExpenseBreakdown(int year, int month) throws Exception;
    List<ExpenseCategoryDTO> getTopExpenseCategories(int year, int month, int limit) throws Exception;
    
    // Transaction methods
    List<TransactionDTO> getRecentTransactions(int year, int month, int limit) throws Exception;
    List<TransactionDTO> getTransactionsByDateRange(String startDate, String endDate) throws Exception;
    List<TransactionDTO> getTopExpenses(int year, int month, int limit) throws Exception;
    List<TransactionDTO> getTopIncome(int year, int month, int limit) throws Exception;
    
    // Statistical methods
    DashboardMetricsDTO getDashboardMetrics(int year, int month) throws Exception;
    PerformanceMetricsDTO getPerformanceMetrics(int year, int month) throws Exception;
    
    // Filter methods
    List<TransactionDTO> getTransactionsByCategory(String category, int year, int month) throws Exception;
    List<TransactionDTO> getTransactionsByType(String type, int year, int month) throws Exception;
    
    // Dashboard data methods
    DashboardDataDTO getCompleteDashboardData(int year, int month) throws Exception;
    
    // Utility methods
    List<Integer> getAvailableYears() throws Exception;
    List<String> getIncomeCategories() throws Exception;
    List<String> getExpenseCategories() throws Exception;
    
    // Export methods
    String exportToCSV(int year, int month) throws Exception;
    String exportToPDF(int year, int month) throws Exception;
    
    // Validation methods
    boolean validateYear(int year) throws Exception;
    boolean validateMonth(int month) throws Exception;
}