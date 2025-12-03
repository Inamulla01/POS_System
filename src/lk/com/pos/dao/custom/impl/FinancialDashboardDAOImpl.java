package lk.com.pos.dao.custom.impl;

import lk.com.pos.dao.FinancialDashboardDAO;
import lk.com.pos.dto.*;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class FinancialDashboardDAOImpl implements FinancialDashboardDAO {
    
    @Override
    public FinancialSummaryDTO getFinancialSummary(int year, int month) throws Exception {
        FinancialSummaryDTO summary = new FinancialSummaryDTO();
        
        try (Connection conn = DB.getConnection()) {
            // Get sales income
            double salesIncome = getSalesIncome(conn, year, month);
            
            // Get other income
            double otherIncome = getOtherIncome(conn, year, month);
            
            // Get total expenses
            double totalExpenses = getTotalExpenses(conn, year, month);
            
            // Get transaction count
            int transactionCount = getTransactionCount(conn, year, month);
            
            // Set summary
            summary.setTotalIncome(salesIncome + otherIncome);
            summary.setTotalExpense(totalExpenses);
            summary.setNetProfit((salesIncome + otherIncome) - totalExpenses);
            summary.setTransactionCount(transactionCount);
            
            // Calculate percentage changes
            summary.setIncomeChangePercent(calculateIncomeChangePercent(conn, year, month));
            summary.setExpenseChangePercent(calculateExpenseChangePercent(conn, year, month));
            summary.setProfitChangePercent(calculateProfitChangePercent(conn, year, month));
            
        } catch (SQLException e) {
            throw new Exception("Error getting financial summary: " + e.getMessage(), e);
        }
        
        return summary;
    }
    
    @Override
    public FinancialSummaryDTO getFinancialSummaryWithTrend(int year, int month, int previousYear, int previousMonth) throws Exception {
        FinancialSummaryDTO current = getFinancialSummary(year, month);
        FinancialSummaryDTO previous = getFinancialSummary(previousYear, previousMonth);
        
        // Calculate actual percentage changes
        if (previous.getTotalIncome() > 0) {
            current.setIncomeChangePercent(
                ((current.getTotalIncome() - previous.getTotalIncome()) / previous.getTotalIncome()) * 100
            );
        }
        
        if (previous.getTotalExpense() > 0) {
            current.setExpenseChangePercent(
                ((current.getTotalExpense() - previous.getTotalExpense()) / previous.getTotalExpense()) * 100
            );
        }
        
        if (previous.getNetProfit() > 0) {
            current.setProfitChangePercent(
                ((current.getNetProfit() - previous.getNetProfit()) / previous.getNetProfit()) * 100
            );
        }
        
        return current;
    }
    
    @Override
    public List<ChartDataDTO> getIncomeExpenseTrend(int year, int month) throws Exception {
        List<ChartDataDTO> chartData = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query;
            boolean isMonthly = month == 0;
            
            if (isMonthly) {
                query = "SELECT " +
                       "  m.month as period, " +
                       "  COALESCE(SUM(i.income), 0) as income, " +
                       "  COALESCE(SUM(e.expense), 0) as expense " +
                       "FROM (SELECT 1 as month UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 " +
                       "      UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 " +
                       "      UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12) m " +
                       "LEFT JOIN ( " +
                       "  SELECT MONTH(date) as month, amount as income, 0 as expense " +
                       "  FROM (SELECT datetime as date, total as amount FROM sales WHERE YEAR(datetime) = ? " +
                       "        UNION ALL " +
                       "        SELECT date, amount FROM income WHERE YEAR(date) = ?) as inc " +
                       ") i ON m.month = i.month " +
                       "LEFT JOIN ( " +
                       "  SELECT MONTH(date) as month, 0 as income, amount as expense " +
                       "  FROM expenses WHERE YEAR(date) = ? " +
                       ") e ON m.month = e.month " +
                       "GROUP BY m.month ORDER BY m.month";
            } else {
                // Get days in month
                int daysInMonth = getDaysInMonth(year, month);
                
                StringBuilder dayQuery = new StringBuilder("SELECT d.day as period FROM (");
                for (int i = 1; i <= daysInMonth; i++) {
                    dayQuery.append("SELECT ").append(i).append(" as day");
                    if (i < daysInMonth) dayQuery.append(" UNION ALL ");
                }
                dayQuery.append(") d");
                
                query = "SELECT " +
                       "  d.day as period, " +
                       "  COALESCE(SUM(i.income), 0) as income, " +
                       "  COALESCE(SUM(e.expense), 0) as expense " +
                       "FROM (" + dayQuery.toString() + ") d " +
                       "LEFT JOIN ( " +
                       "  SELECT DAY(date) as day, amount as income, 0 as expense " +
                       "  FROM (SELECT datetime as date, total as amount FROM sales " +
                       "        WHERE YEAR(datetime) = ? AND MONTH(datetime) = ? " +
                       "        UNION ALL " +
                       "        SELECT date, amount FROM income " +
                       "        WHERE YEAR(date) = ? AND MONTH(date) = ?) as inc " +
                       ") i ON d.day = i.day " +
                       "LEFT JOIN ( " +
                       "  SELECT DAY(date) as day, 0 as income, amount as expense " +
                       "  FROM expenses WHERE YEAR(date) = ? AND MONTH(date) = ? " +
                       ") e ON d.day = e.day " +
                       "GROUP BY d.day ORDER BY d.day";
            }
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                if (isMonthly) {
                    pst.setInt(1, year);
                    pst.setInt(2, year);
                    pst.setInt(3, year);
                } else {
                    pst.setInt(1, year);
                    pst.setInt(2, month);
                    pst.setInt(3, year);
                    pst.setInt(4, month);
                    pst.setInt(5, year);
                    pst.setInt(6, month);
                }
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        String period = isMonthly ? 
                            getMonthName(rs.getInt("period")) : 
                            String.valueOf(rs.getInt("period"));
                        
                        chartData.add(new ChartDataDTO(
                            period,
                            rs.getDouble("income"),
                            rs.getDouble("expense")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error getting income expense trend: " + e.getMessage(), e);
        }
        
        return chartData;
    }
    
    @Override
    public List<ChartDataDTO> getYearlyComparison(int currentYear, int previousYear) throws Exception {
        List<ChartDataDTO> comparisonData = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT " +
                          "  m.month, " +
                          "  COALESCE(c.current_amount, 0) as current, " +
                          "  COALESCE(p.previous_amount, 0) as previous " +
                          "FROM (SELECT 1 as month UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 " +
                          "      UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 " +
                          "      UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12) m " +
                          "LEFT JOIN ( " +
                          "  SELECT MONTH(date) as month, SUM(amount) as current_amount " +
                          "  FROM (SELECT datetime as date, total as amount FROM sales WHERE YEAR(datetime) = ? " +
                          "        UNION ALL " +
                          "        SELECT date, amount FROM income WHERE YEAR(date) = ? " +
                          "        UNION ALL " +
                          "        SELECT date, -amount as amount FROM expenses WHERE YEAR(date) = ?) as data " +
                          "  GROUP BY MONTH(date) " +
                          ") c ON m.month = c.month " +
                          "LEFT JOIN ( " +
                          "  SELECT MONTH(date) as month, SUM(amount) as previous_amount " +
                          "  FROM (SELECT datetime as date, total as amount FROM sales WHERE YEAR(datetime) = ? " +
                          "        UNION ALL " +
                          "        SELECT date, amount FROM income WHERE YEAR(date) = ? " +
                          "        UNION ALL " +
                          "        SELECT date, -amount as amount FROM expenses WHERE YEAR(date) = ?) as data " +
                          "  GROUP BY MONTH(date) " +
                          ") p ON m.month = p.month " +
                          "ORDER BY m.month";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, currentYear);
                pst.setInt(2, currentYear);
                pst.setInt(3, currentYear);
                pst.setInt(4, previousYear);
                pst.setInt(5, previousYear);
                pst.setInt(6, previousYear);
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        comparisonData.add(new ChartDataDTO(
                            getMonthName(rs.getInt("month")),
                            rs.getDouble("current"),
                            rs.getDouble("previous")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error getting yearly comparison: " + e.getMessage(), e);
        }
        
        return comparisonData;
    }
    
    @Override
    public List<ExpenseCategoryDTO> getExpenseBreakdown(int year, int month) throws Exception {
        List<ExpenseCategoryDTO> breakdown = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            // First get total expenses to calculate percentages
            double totalExpenses = getTotalExpenses(conn, year, month);
            
            String query = "SELECT et.expenses_type as category_name, " +
                          "SUM(e.amount) as total_amount " +
                          "FROM expenses e " +
                          "INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                          "WHERE YEAR(e.date) = ? ";
            
            if (month > 0) {
                query += "AND MONTH(e.date) = ? ";
            }
            
            query += "GROUP BY et.expenses_type_id ORDER BY total_amount DESC";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, year);
                if (month > 0) {
                    pst.setInt(2, month);
                }
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        double amount = rs.getDouble("total_amount");
                        double percentage = totalExpenses > 0 ? (amount / totalExpenses) * 100 : 0;
                        
                        ExpenseCategoryDTO category = new ExpenseCategoryDTO(
                            rs.getString("category_name"),
                            amount,
                            percentage
                        );
                        
                        breakdown.add(category);
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error getting expense breakdown: " + e.getMessage(), e);
        }
        
        return breakdown;
    }
    
    @Override
    public List<ExpenseCategoryDTO> getTopExpenseCategories(int year, int month, int limit) throws Exception {
        List<ExpenseCategoryDTO> breakdown = getExpenseBreakdown(year, month);
        
        if (breakdown.size() > limit) {
            return breakdown.subList(0, limit);
        }
        
        return breakdown;
    }
    
    @Override
    public List<TransactionDTO> getRecentTransactions(int year, int month, int limit) throws Exception {
        List<TransactionDTO> transactions = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT date, type, category, description, amount, status FROM (" +
                          "  SELECT i.date, 'Income' as type, it.income_type as category, " +
                          "         i.description, i.amount, es.e_status_name as status " +
                          "  FROM income i " +
                          "  INNER JOIN income_type it ON i.income_type_id = it.income_type_id " +
                          "  INNER JOIN e_status es ON i.status_id = es.e_status_id " +
                          "  WHERE YEAR(i.date) = ? ";
            
            if (month > 0) {
                query += "AND MONTH(i.date) = ? ";
            }
            
            query += "  UNION ALL " +
                    "  SELECT e.date, 'Expense' as type, et.expenses_type as category, " +
                    "         e.description, e.amount, es.e_status_name as status " +
                    "  FROM expenses e " +
                    "  INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                    "  INNER JOIN e_status es ON e.e_status_id = es.e_status_id " +
                    "  WHERE YEAR(e.date) = ? ";
            
            if (month > 0) {
                query += "AND MONTH(e.date) = ? ";
            }
            
            query += ") as combined ORDER BY date DESC LIMIT ?";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                int paramIndex = 1;
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                pst.setInt(paramIndex++, limit);
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(new TransactionDTO(
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("status")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error getting recent transactions: " + e.getMessage(), e);
        }
        
        return transactions;
    }
    
    @Override
    public List<TransactionDTO> getTransactionsByDateRange(String startDate, String endDate) throws Exception {
        List<TransactionDTO> transactions = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT date, type, category, description, amount, status FROM (" +
                          "  SELECT i.date, 'Income' as type, it.income_type as category, " +
                          "         i.description, i.amount, es.e_status_name as status " +
                          "  FROM income i " +
                          "  INNER JOIN income_type it ON i.income_type_id = it.income_type_id " +
                          "  INNER JOIN e_status es ON i.status_id = es.e_status_id " +
                          "  WHERE i.date BETWEEN ? AND ? " +
                          "  UNION ALL " +
                          "  SELECT e.date, 'Expense' as type, et.expenses_type as category, " +
                          "         e.description, e.amount, es.e_status_name as status " +
                          "  FROM expenses e " +
                          "  INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                          "  INNER JOIN e_status es ON e.e_status_id = es.e_status_id " +
                          "  WHERE e.date BETWEEN ? AND ? " +
                          ") as combined ORDER BY date DESC";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, startDate);
                pst.setString(2, endDate);
                pst.setString(3, startDate);
                pst.setString(4, endDate);
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(new TransactionDTO(
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("status")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error getting transactions by date range: " + e.getMessage(), e);
        }
        
        return transactions;
    }
    
    @Override
    public List<TransactionDTO> getTopExpenses(int year, int month, int limit) throws Exception {
        List<TransactionDTO> expenses = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            // Build query based on whether month is specified
            String query;
            
            if (month > 0) {
                query = "SELECT e.date, et.expenses_type as category, " +
                       "e.description, e.amount, es.e_status_name as status " +
                       "FROM expenses e " +
                       "INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                       "INNER JOIN e_status es ON e.e_status_id = es.e_status_id " +
                       "WHERE YEAR(e.date) = ? AND MONTH(e.date) = ? " +
                       "ORDER BY e.amount DESC LIMIT ?";
            } else {
                query = "SELECT e.date, et.expenses_type as category, " +
                       "e.description, e.amount, es.e_status_name as status " +
                       "FROM expenses e " +
                       "INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                       "INNER JOIN e_status es ON e.e_status_id = es.e_status_id " +
                       "WHERE YEAR(e.date) = ? " +
                       "ORDER BY e.amount DESC LIMIT ?";
            }
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                if (month > 0) {
                    pst.setInt(1, year);
                    pst.setInt(2, month);
                    pst.setInt(3, limit);
                } else {
                    pst.setInt(1, year);
                    pst.setInt(2, limit);
                }
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        expenses.add(new TransactionDTO(
                            rs.getDate("date").toLocalDate(),
                            "Expense",
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("status")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error getting top expenses: " + e.getMessage(), e);
        }
        
        return expenses;
    }
    
    @Override
    public List<TransactionDTO> getTopIncome(int year, int month, int limit) throws Exception {
        List<TransactionDTO> incomeList = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT date, type, category, description, amount, status FROM (" +
                          "  SELECT s.datetime as date, 'Sales' as type, 'Sales' as category, " +
                          "         CONCAT('Sales #', s.sales_id) as description, " +
                          "         s.total as amount, 'Completed' as status " +
                          "  FROM sales s " +
                          "  WHERE YEAR(s.datetime) = ? ";
            
            if (month > 0) {
                query += "AND MONTH(s.datetime) = ? ";
            }
            
            query += "  UNION ALL " +
                    "  SELECT i.date, 'Other Income' as type, it.income_type as category, " +
                    "         i.description, i.amount, es.e_status_name as status " +
                    "  FROM income i " +
                    "  INNER JOIN income_type it ON i.income_type_id = it.income_type_id " +
                    "  INNER JOIN e_status es ON i.status_id = es.e_status_id " +
                    "  WHERE YEAR(i.date) = ? ";
            
            if (month > 0) {
                query += "AND MONTH(i.date) = ? ";
            }
            
            query += ") as income_data ORDER BY amount DESC LIMIT ?";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                int paramIndex = 1;
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                pst.setInt(paramIndex++, limit);
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        incomeList.add(new TransactionDTO(
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("status")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error getting top income: " + e.getMessage(), e);
        }
        
        return incomeList;
    }
    
    @Override
    public double getAverageTransactionValue(int year, int month) throws Exception {
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT AVG(total) FROM sales WHERE YEAR(datetime) = ?";
            
            if (month > 0) {
                query += " AND MONTH(datetime) = ?";
            }
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, year);
                if (month > 0) {
                    pst.setInt(2, month);
                }
                
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error getting average transaction value: " + e.getMessage(), e);
        }
        
        return 0;
    }
    
    @Override
    public int getTotalTransactionsCount(int year, int month) throws Exception {
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT COUNT(*) FROM sales WHERE YEAR(datetime) = ?";
            
            if (month > 0) {
                query += " AND MONTH(datetime) = ?";
            }
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, year);
                if (month > 0) {
                    pst.setInt(2, month);
                }
                
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error getting total transactions count: " + e.getMessage(), e);
        }
        
        return 0;
    }
    
    @Override
    public double getTotalSalesAmount(int year, int month) throws Exception {
        return getSalesIncome(null, year, month);
    }
    
    @Override
    public double getTotalOtherIncome(int year, int month) throws Exception {
        return getOtherIncome(null, year, month);
    }
    
    @Override
    public List<TransactionDTO> getTransactionsByCategory(String category, int year, int month) throws Exception {
        List<TransactionDTO> transactions = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT date, type, category, description, amount, status FROM (" +
                          "  SELECT i.date, 'Income' as type, it.income_type as category, " +
                          "         i.description, i.amount, es.e_status_name as status " +
                          "  FROM income i " +
                          "  INNER JOIN income_type it ON i.income_type_id = it.income_type_id " +
                          "  INNER JOIN e_status es ON i.status_id = es.e_status_id " +
                          "  WHERE it.income_type = ? AND YEAR(i.date) = ? ";
            
            if (month > 0) {
                query += "AND MONTH(i.date) = ? ";
            }
            
            query += "  UNION ALL " +
                    "  SELECT e.date, 'Expense' as type, et.expenses_type as category, " +
                    "         e.description, e.amount, es.e_status_name as status " +
                    "  FROM expenses e " +
                    "  INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                    "  INNER JOIN e_status es ON e.e_status_id = es.e_status_id " +
                    "  WHERE et.expenses_type = ? AND YEAR(e.date) = ? ";
            
            if (month > 0) {
                query += "AND MONTH(e.date) = ? ";
            }
            
            query += ") as combined ORDER BY date DESC";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                int paramIndex = 1;
                pst.setString(paramIndex++, category);
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                pst.setString(paramIndex++, category);
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(new TransactionDTO(
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("status")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error getting transactions by category: " + e.getMessage(), e);
        }
        
        return transactions;
    }
    
    @Override
    public List<TransactionDTO> getTransactionsByType(String type, int year, int month) throws Exception {
        if ("Income".equalsIgnoreCase(type)) {
            return getTopIncome(year, month, 1000); // Large limit to get all
        } else if ("Expense".equalsIgnoreCase(type)) {
            return getTopExpenses(year, month, 1000);
        }
        
        throw new Exception("Invalid transaction type. Use 'Income' or 'Expense'");
    }
    
    @Override
    public double getMonthlyGrowthRate(int year, int month) throws Exception {
        try (Connection conn = DB.getConnection()) {
            // Get current month total
            double currentTotal = getSalesIncome(conn, year, month) + getOtherIncome(conn, year, month);
            
            // Get previous month total
            int prevYear = month == 1 ? year - 1 : year;
            int prevMonth = month == 1 ? 12 : month - 1;
            double previousTotal = getSalesIncome(conn, prevYear, prevMonth) + getOtherIncome(conn, prevYear, prevMonth);
            
            if (previousTotal > 0) {
                return ((currentTotal - previousTotal) / previousTotal) * 100;
            }
        } catch (SQLException e) {
            throw new Exception("Error getting monthly growth rate: " + e.getMessage(), e);
        }
        
        return 0;
    }
    
    @Override
    public double getYearlyGrowthRate(int year) throws Exception {
        try (Connection conn = DB.getConnection()) {
            double currentYearTotal = getSalesIncome(conn, year, 0) + getOtherIncome(conn, year, 0);
            double previousYearTotal = getSalesIncome(conn, year - 1, 0) + getOtherIncome(conn, year - 1, 0);
            
            if (previousYearTotal > 0) {
                return ((currentYearTotal - previousYearTotal) / previousYearTotal) * 100;
            }
        } catch (SQLException e) {
            throw new Exception("Error getting yearly growth rate: " + e.getMessage(), e);
        }
        
        return 0;
    }
    
    @Override
    public int getActiveDaysCount(int year, int month) throws Exception {
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT COUNT(DISTINCT DATE(date)) as active_days FROM (" +
                          "  SELECT datetime as date FROM sales WHERE YEAR(datetime) = ?";
            
            if (month > 0) {
                query += " AND MONTH(datetime) = ?";
            }
            
            query += "  UNION " +
                    "  SELECT date FROM income WHERE YEAR(date) = ?";
            
            if (month > 0) {
                query += " AND MONTH(date) = ?";
            }
            
            query += "  UNION " +
                    "  SELECT date FROM expenses WHERE YEAR(date) = ?";
            
            if (month > 0) {
                query += " AND MONTH(date) = ?";
            }
            
            query += ") as all_dates";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                int paramIndex = 1;
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                pst.setInt(paramIndex++, year);
                if (month > 0) {
                    pst.setInt(paramIndex++, month);
                }
                
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("active_days");
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error getting active days count: " + e.getMessage(), e);
        }
        
        return 0;
    }
    
    @Override
    public List<Integer> getAvailableYears() throws Exception {
        List<Integer> years = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT DISTINCT YEAR(date) as year FROM (" +
                          "  SELECT datetime as date FROM sales " +
                          "  UNION ALL " +
                          "  SELECT date FROM income " +
                          "  UNION ALL " +
                          "  SELECT date FROM expenses " +
                          ") as all_dates ORDER BY year DESC";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        years.add(rs.getInt("year"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error getting available years: " + e.getMessage(), e);
        }
        
        // If no years found, add current year
        if (years.isEmpty()) {
            years.add(LocalDate.now().getYear());
        }
        
        return years;
    }
    
    @Override
    public List<String> getIncomeCategories() throws Exception {
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT DISTINCT income_type FROM income_type ORDER BY income_type";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        categories.add(rs.getString("income_type"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error getting income categories: " + e.getMessage(), e);
        }
        
        return categories;
    }
    
    @Override
    public List<String> getExpenseCategories() throws Exception {
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            String query = "SELECT DISTINCT expenses_type FROM expenses_type ORDER BY expenses_type";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        categories.add(rs.getString("expenses_type"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error getting expense categories: " + e.getMessage(), e);
        }
        
        return categories;
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private double getSalesIncome(Connection conn, int year, int month) throws SQLException {
        if (conn == null) {
            try (Connection newConn = DB.getConnection()) {
                return getSalesIncome(newConn, year, month);
            }
        }
        
        String query = "SELECT COALESCE(SUM(total), 0) FROM sales WHERE YEAR(datetime) = ?";
        
        if (month > 0) {
            query += " AND MONTH(datetime) = ?";
        }
        
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, year);
            if (month > 0) {
                pst.setInt(2, month);
            }
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        
        return 0;
    }
    
    private double getOtherIncome(Connection conn, int year, int month) throws SQLException {
        if (conn == null) {
            try (Connection newConn = DB.getConnection()) {
                return getOtherIncome(newConn, year, month);
            }
        }
        
        String query = "SELECT COALESCE(SUM(amount), 0) FROM income WHERE YEAR(date) = ?";
        
        if (month > 0) {
            query += " AND MONTH(date) = ?";
        }
        
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, year);
            if (month > 0) {
                pst.setInt(2, month);
            }
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        
        return 0;
    }
    
    private double getTotalExpenses(Connection conn, int year, int month) throws SQLException {
        if (conn == null) {
            try (Connection newConn = DB.getConnection()) {
                return getTotalExpenses(newConn, year, month);
            }
        }
        
        String query = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE YEAR(date) = ?";
        
        if (month > 0) {
            query += " AND MONTH(date) = ?";
        }
        
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, year);
            if (month > 0) {
                pst.setInt(2, month);
            }
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        
        return 0;
    }
    
    private int getTransactionCount(Connection conn, int year, int month) throws SQLException {
        if (conn == null) {
            try (Connection newConn = DB.getConnection()) {
                return getTransactionCount(newConn, year, month);
            }
        }
        
        String query = "SELECT COUNT(*) FROM sales WHERE YEAR(datetime) = ?";
        
        if (month > 0) {
            query += " AND MONTH(datetime) = ?";
        }
        
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, year);
            if (month > 0) {
                pst.setInt(2, month);
            }
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    private double calculateIncomeChangePercent(Connection conn, int year, int month) throws SQLException {
        // Simplified calculation
        double currentIncome = getSalesIncome(conn, year, month) + getOtherIncome(conn, year, month);
        
        int compareYear = month == 1 ? year - 1 : year;
        int compareMonth = month == 1 ? 12 : month - 1;
        
        double previousIncome = getSalesIncome(conn, compareYear, compareMonth) + 
                               getOtherIncome(conn, compareYear, compareMonth);
        
        if (previousIncome > 0) {
            return ((currentIncome - previousIncome) / previousIncome) * 100;
        }
        
        return 0;
    }
    
    private double calculateExpenseChangePercent(Connection conn, int year, int month) throws SQLException {
        double currentExpense = getTotalExpenses(conn, year, month);
        
        int compareYear = month == 1 ? year - 1 : year;
        int compareMonth = month == 1 ? 12 : month - 1;
        
        double previousExpense = getTotalExpenses(conn, compareYear, compareMonth);
        
        if (previousExpense > 0) {
            return ((currentExpense - previousExpense) / previousExpense) * 100;
        }
        
        return 0;
    }
    
    private double calculateProfitChangePercent(Connection conn, int year, int month) throws SQLException {
        double currentProfit = (getSalesIncome(conn, year, month) + getOtherIncome(conn, year, month)) - 
                              getTotalExpenses(conn, year, month);
        
        int compareYear = month == 1 ? year - 1 : year;
        int compareMonth = month == 1 ? 12 : month - 1;
        
        double previousProfit = (getSalesIncome(conn, compareYear, compareMonth) + 
                                getOtherIncome(conn, compareYear, compareMonth)) - 
                               getTotalExpenses(conn, compareYear, compareMonth);
        
        if (previousProfit > 0) {
            return ((currentProfit - previousProfit) / previousProfit) * 100;
        }
        
        return 0;
    }
    
    private int getDaysInMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return yearMonth.lengthOfMonth();
    }
    
    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month - 1];
    }
}