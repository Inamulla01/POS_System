package lk.com.pos.dao;

import lk.com.pos.dto.CustomerDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Customer Data Access Object
 * Handles all database operations for customers
 */
public class CustomerDAO {
    private static final Logger log = Logger.getLogger(CustomerDAO.class.getName());

    /**
     * Search customers with filters
     */
    public List<CustomerDTO> searchCustomers(String searchText, boolean missedDueDateOnly, 
                                           boolean noDueOnly, boolean dueAmountOnly) throws SQLException {
        // Build query and parameters
        StringBuilder queryBuilder = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        queryBuilder.append("SELECT * FROM (");
        queryBuilder.append(buildCustomerSubquery());
        
        // Add search filter
        boolean hasSearch = isValidSearchText(searchText);
        if (hasSearch) {
            queryBuilder.append(" WHERE (cc.customer_name LIKE ? OR cc.nic LIKE ?) ");
        }
        
        queryBuilder.append(" GROUP BY cc.customer_id, cc.customer_name, cc.customer_phone_no, ");
        queryBuilder.append("cc.customer_address, cc.nic, cc.date_time, s.status_name ");
        queryBuilder.append(") AS customer_data WHERE 1=1 ");
        
        // Add search parameters
        if (hasSearch) {
            String searchPattern = "%" + searchText + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }
        
        // Apply status filters
        String statusFilter = buildStatusFilter(missedDueDateOnly, noDueOnly, dueAmountOnly);
        queryBuilder.append(statusFilter);
        queryBuilder.append(" ORDER BY customer_id DESC");
        
        String finalQuery = queryBuilder.toString();
        log.info("Executing query: " + finalQuery);
        
        return DB.executeQuerySafe(finalQuery, rs -> {
            List<CustomerDTO> customers = new ArrayList<>();
            while (rs.next()) {
                CustomerDTO customer = mapResultSetToCustomerDTO(rs);
                customers.add(customer);
            }
            return customers;
        }, parameters.toArray());
    }

    /**
     * Build customer subquery
     */
    private String buildCustomerSubquery() {
        return "SELECT cc.customer_id, cc.customer_name, cc.customer_phone_no, "
                + "cc.customer_address, cc.nic, cc.date_time, s.status_name, "
                + "MAX(c.credit_final_date) as latest_due_date, "
                + "IFNULL(SUM(c.credit_amout), 0) AS total_credit_amount, "
                + "IFNULL(SUM(cp.credit_pay_amount), 0) AS total_paid "
                + "FROM credit_customer cc "
                + "JOIN status s ON s.status_id = cc.status_id "
                + "LEFT JOIN credit c ON c.credit_customer_id = cc.customer_id "
                + "LEFT JOIN credit_pay cp ON cp.credit_customer_id = cc.customer_id ";
    }

    /**
     * Check if search text is valid
     */
    private boolean isValidSearchText(String searchText) {
        return searchText != null && !searchText.trim().isEmpty();
    }

    /**
     * Build status filter clause
     */
    private String buildStatusFilter(boolean missedDueDateOnly, boolean noDueOnly, boolean dueAmountOnly) {
        if (missedDueDateOnly) {
            return " AND latest_due_date < CURDATE() AND total_credit_amount > total_paid ";
        } else if (noDueOnly) {
            return " AND total_credit_amount <= total_paid ";
        } else if (dueAmountOnly) {
            return " AND total_credit_amount > total_paid ";
        }
        return "";
    }

    /**
     * Map ResultSet to CustomerDTO
     */
    private CustomerDTO mapResultSetToCustomerDTO(ResultSet rs) throws SQLException {
        CustomerDTO customer = new CustomerDTO();
        
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setCustomerName(rs.getString("customer_name"));
        customer.setPhone(rs.getString("customer_phone_no"));
        customer.setAddress(rs.getString("customer_address"));
        customer.setNic(rs.getString("nic"));
        
        // Handle date conversions
        Date regDate = rs.getDate("date_time");
        if (regDate != null) {
            customer.setRegistrationDate(new java.util.Date(regDate.getTime()));
        }
        
        customer.setStatus(rs.getString("status_name"));
        
        Date dueDate = rs.getDate("latest_due_date");
        if (dueDate != null) {
            customer.setLatestDueDate(new java.util.Date(dueDate.getTime()));
        }
        
        customer.setTotalCreditAmount(rs.getDouble("total_credit_amount"));
        customer.setTotalPaid(rs.getDouble("total_paid"));
        
        return customer;
    }

    /**
     * Get customer by ID
     */
    public CustomerDTO getCustomerById(int customerId) throws SQLException {
        String query = "SELECT * FROM (" + buildCustomerSubquery() + 
                      " WHERE cc.customer_id = ? " +
                      "GROUP BY cc.customer_id, cc.customer_name, cc.customer_phone_no, " +
                      "cc.customer_address, cc.nic, cc.date_time, s.status_name) AS customer_data";
        
        List<CustomerDTO> customers = DB.executeQuerySafe(query, rs -> {
            List<CustomerDTO> result = new ArrayList<>();
            if (rs.next()) {
                result.add(mapResultSetToCustomerDTO(rs));
            }
            return result;
        }, customerId);
        
        return customers.isEmpty() ? null : customers.get(0);
    }

    /**
     * Get all customers
     */
    public List<CustomerDTO> getAllCustomers() throws SQLException {
        return searchCustomers("", false, false, false);
    }

    /**
     * Get customers with due amounts
     */
    public List<CustomerDTO> getCustomersWithDueAmount() throws SQLException {
        return searchCustomers("", false, false, true);
    }

    /**
     * Get customers with missed due dates
     */
    public List<CustomerDTO> getCustomersWithMissedDueDate() throws SQLException {
        return searchCustomers("", true, false, false);
    }

    /**
     * Get customers with no due
     */
    public List<CustomerDTO> getCustomersWithNoDue() throws SQLException {
        return searchCustomers("", false, true, false);
    }

    /**
     * Get total customer count
     */
    public int getCustomerCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM credit_customer WHERE status_id = 1";
        
        return DB.executeQuerySafe(query, rs -> {
            return rs.next() ? rs.getInt("count") : 0;
        });
    }

    /**
     * Get total outstanding amount across all customers
     */
    public double getTotalOutstanding() throws SQLException {
        String query = "SELECT SUM(outstanding) as total_outstanding FROM (" +
                      "SELECT (SUM(c.credit_amout) - IFNULL(SUM(cp.credit_pay_amount), 0)) as outstanding " +
                      "FROM credit c " +
                      "LEFT JOIN credit_pay cp ON c.credit_id = cp.credit_id " +
                      "GROUP BY c.credit_customer_id" +
                      ") AS outstanding_table";
        
        return DB.executeQuerySafe(query, rs -> {
            return rs.next() ? rs.getDouble("total_outstanding") : 0.0;
        });
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            return DB.testConnection();
        } catch (Exception e) {
            log.severe("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}