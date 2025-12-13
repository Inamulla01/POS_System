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
        
        // Check if search is valid
        boolean hasSearch = isValidSearchText(searchText);
        
        // Add WHERE clause for search INSIDE the subquery (before GROUP BY)
        if (hasSearch) {
            queryBuilder.append(" WHERE (cc.customer_name LIKE ? OR cc.nic LIKE ?) ");
            String searchPattern = "%" + searchText + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }
        
        queryBuilder.append(" GROUP BY cc.customer_id, cc.customer_name, cc.customer_phone_no, ");
        queryBuilder.append("cc.customer_address, cc.nic, cc.date_time, s.status_name ");
        queryBuilder.append(") AS customer_data ");
        
        // Apply status filters AFTER the subquery
        String statusFilter = buildStatusFilter(missedDueDateOnly, noDueOnly, dueAmountOnly);
        
        if (!statusFilter.isEmpty()) {
            queryBuilder.append(" WHERE 1=1 ");
            queryBuilder.append(statusFilter);
        }
        
        queryBuilder.append(" ORDER BY customer_id DESC");
        
        String finalQuery = queryBuilder.toString();
        log.info("Executing query: " + finalQuery);
        log.info("Filters - Search: '" + searchText + "', MissedDue: " + missedDueDateOnly + ", NoDue: " + noDueOnly + ", DueAmount: " + dueAmountOnly);
        
        List<CustomerDTO> customers = DB.executeQuerySafe(finalQuery, rs -> {
            List<CustomerDTO> customerList = new ArrayList<>();
            int count = 0;
            while (rs.next()) {
                CustomerDTO customer = mapResultSetToCustomerDTO(rs);
                customerList.add(customer);
                count++;
                
                // Log customers found
                if (count <= 10) {
                    log.info("Customer " + count + ": ID=" + customer.getCustomerId() 
                            + ", Name=" + customer.getCustomerName() 
                            + ", Credit=" + customer.getTotalCreditAmount() 
                            + ", Paid=" + customer.getTotalPaid() 
                            + ", Outstanding=" + customer.getOutstanding()
                            + ", DueDate=" + customer.getLatestDueDate());
                }
            }
            log.info("Total customers found: " + count);
            return customerList;
        }, parameters.toArray());
        
        log.info("Returning " + customers.size() + " customers");
        return customers;
    }

    /**
     * Build customer subquery - NO WHERE clause here
     */
    private String buildCustomerSubquery() {
        return "SELECT cc.customer_id, cc.customer_name, cc.customer_phone_no, "
                + "cc.customer_address, cc.nic, cc.date_time, s.status_name, "
                + "MAX(c.credit_final_date) as latest_due_date, "
                + "IFNULL(SUM(c.credit_amout), 0) AS total_credit_amount, "
                + "IFNULL(cp.total_paid, 0) AS total_paid "
                + "FROM credit_customer cc "
                + "JOIN status s ON s.status_id = cc.status_id "
                + "LEFT JOIN credit c ON c.credit_customer_id = cc.customer_id "
                + "LEFT JOIN ("
                + "    SELECT credit_customer_id, SUM(credit_pay_amount) as total_paid "
                + "    FROM credit_pay "
                + "    GROUP BY credit_customer_id"
                + ") cp ON cp.credit_customer_id = cc.customer_id ";
    }

    /**
     * Check if search text is valid
     */
    private boolean isValidSearchText(String searchText) {
        return searchText != null && !searchText.trim().isEmpty();
    }

    /**
     * Build status filter clause
     * Modified to handle customers with no credit records better
     */
    private String buildStatusFilter(boolean missedDueDateOnly, boolean noDueOnly, boolean dueAmountOnly) {
        if (missedDueDateOnly) {
            log.info("Applying filter: Missed Due Date (latest_due_date < CURDATE() AND outstanding > 0)");
            return " AND latest_due_date IS NOT NULL AND latest_due_date < CURDATE() AND total_credit_amount > total_paid ";
        } else if (noDueOnly) {
            log.info("Applying filter: No Due (total_credit_amount <= total_paid OR no credit records)");
            // Show customers with no outstanding balance OR no credit at all
            return " AND total_credit_amount <= total_paid ";
        } else if (dueAmountOnly) {
            log.info("Applying filter: Due Amount (total_credit_amount > total_paid AND has credit)");
            // Only show customers who actually have credit records AND outstanding balance
            return " AND total_credit_amount > 0 AND total_credit_amount > total_paid ";
        }
        log.info("No filter applied - showing all customers");
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
        log.info("Getting all customers (no filters)");
        return searchCustomers("", false, false, false);
    }

    /**
     * Get customers with due amounts
     */
    public List<CustomerDTO> getCustomersWithDueAmount() throws SQLException {
        log.info("Getting customers with due amounts");
        return searchCustomers("", false, false, true);
    }

    /**
     * Get customers with missed due dates
     */
    public List<CustomerDTO> getCustomersWithMissedDueDate() throws SQLException {
        log.info("Getting customers with missed due dates");
        return searchCustomers("", true, false, false);
    }

    /**
     * Get customers with no due
     */
    public List<CustomerDTO> getCustomersWithNoDue() throws SQLException {
        log.info("Getting customers with no due");
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
        String query = "SELECT IFNULL(SUM(total_credit_amount - total_paid), 0) as total_outstanding "
                + "FROM ("
                + "    SELECT cc.customer_id, "
                + "           IFNULL(SUM(c.credit_amout), 0) AS total_credit_amount, "
                + "           IFNULL(cp.total_paid, 0) AS total_paid "
                + "    FROM credit_customer cc "
                + "    LEFT JOIN credit c ON c.credit_customer_id = cc.customer_id "
                + "    LEFT JOIN ("
                + "        SELECT credit_customer_id, SUM(credit_pay_amount) as total_paid "
                + "        FROM credit_pay "
                + "        GROUP BY credit_customer_id"
                + "    ) cp ON cp.credit_customer_id = cc.customer_id "
                + "    WHERE cc.status_id = 1 "
                + "    GROUP BY cc.customer_id"
                + ") AS customer_totals";
        
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