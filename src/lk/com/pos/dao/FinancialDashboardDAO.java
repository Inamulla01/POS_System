package lk.com.pos.dao;

import lk.com.pos.dto.FinancialSummaryDTO;
import lk.com.pos.dto.TransactionDTO;
import lk.com.pos.dto.ChartDataDTO;
import lk.com.pos.dto.ExpenseCategoryDTO;
import java.util.List;

public interface FinancialDashboardDAO {
    // Core summary methods
    FinancialSummaryDTO getFinancialSummary(int year, int month) throws Exception;
    FinancialSummaryDTO getFinancialSummaryWithTrend(int year, int month, int previousYear, int previousMonth) throws Exception;
    
    // Chart data methods
    List<ChartDataDTO> getIncomeExpenseTrend(int year, int month) throws Exception;
    List<ChartDataDTO> getYearlyComparison(int currentYear, int previousYear) throws Exception;
    List<ExpenseCategoryDTO> getExpenseBreakdown(int year, int month) throws Exception;
    List<ExpenseCategoryDTO> getTopExpenseCategories(int year, int month, int limit) throws Exception;
    
    // Transaction methods
    List<TransactionDTO> getRecentTransactions(int year, int month, int limit) throws Exception;
    List<TransactionDTO> getTransactionsByDateRange(String startDate, String endDate) throws Exception;
    List<TransactionDTO> getTopExpenses(int year, int month, int limit) throws Exception;
    List<TransactionDTO> getTopIncome(int year, int month, int limit) throws Exception;
    
    // Statistical methods
    double getAverageTransactionValue(int year, int month) throws Exception;
    int getTotalTransactionsCount(int year, int month) throws Exception;
    double getTotalSalesAmount(int year, int month) throws Exception;
    double getTotalOtherIncome(int year, int month) throws Exception;
    
    // Filter methods
    List<TransactionDTO> getTransactionsByCategory(String category, int year, int month) throws Exception;
    List<TransactionDTO> getTransactionsByType(String type, int year, int month) throws Exception;
    
    // Dashboard metrics
    double getMonthlyGrowthRate(int year, int month) throws Exception;
    double getYearlyGrowthRate(int year) throws Exception;
    int getActiveDaysCount(int year, int month) throws Exception;
    
    // Utility methods
    List<Integer> getAvailableYears() throws Exception;
    List<String> getIncomeCategories() throws Exception;
    List<String> getExpenseCategories() throws Exception;
}