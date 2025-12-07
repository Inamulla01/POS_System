package lk.com.pos.dao;

import lk.com.pos.connection.DB;
import lk.com.pos.dto.SupplierDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Supplier operations
 * Uses the new DB connection class with connection pooling
 * 
 * @author pasin
 * @version 2.0
 */
public class SupplierDAO {
    
    // Status constants
    private static final int STATUS_ACTIVE_ID = 1;
    private static final int STATUS_INACTIVE_ID = 2;
    
    /**
     * Get all suppliers with optional filters
     */
    public List<SupplierDTO> getAllSuppliers(String searchText, String status, String dueStatus) throws SQLException {
        String query = buildSupplierQuery(searchText, status, dueStatus);
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            List<SupplierDTO> suppliers = new ArrayList<>();
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplierDTO(rs));
            }
            return suppliers;
        });
    }
    
    /**
     * Get supplier by ID
     */
    public SupplierDTO getSupplierById(int supplierId) throws SQLException {
        String query = "SELECT s.suppliers_id, s.Company, s.suppliers_name, s.suppliers_mobile, " +
                      "s.suppliers_reg_no, s.suppliers_address, p.p_status, s.p_status_id, " +
                      "COALESCE(cs.credit_amount, 0) as credit_amount, " +
                      "COALESCE(SUM(scp.credit_pay_amount), 0) as credit_pay_amount " +
                      "FROM suppliers s " +
                      "LEFT JOIN p_status p ON s.p_status_id = p.p_status_id " +
                      "LEFT JOIN credit_supplier cs ON cs.suppliers_id = s.suppliers_id " +
                      "LEFT JOIN supplier_credit_pay scp ON scp.credit_supplier_id = cs.credit_supplier_id " +
                      "WHERE s.suppliers_id = ? " +
                      "GROUP BY s.suppliers_id, s.Company, s.suppliers_name, s.suppliers_mobile, " +
                      "s.suppliers_reg_no, s.suppliers_address, p.p_status, s.p_status_id, cs.credit_amount";
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            if (rs.next()) {
                return mapResultSetToSupplierDTO(rs);
            }
            return null;
        }, supplierId);
    }
    
    /**
     * Add new supplier
     */
    public int addSupplier(SupplierDTO supplier) throws SQLException {
        String query = "INSERT INTO suppliers (Company, suppliers_name, suppliers_mobile, " +
                      "suppliers_reg_no, suppliers_address, p_status_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        return DB.insertAndGetId(query, 
            supplier.getCompany(),
            supplier.getSupplierName(),
            supplier.getMobile(),
            supplier.getRegNo(),
            supplier.getAddress(),
            supplier.getPStatusId());
    }
    
    /**
     * Update existing supplier
     */
    public boolean updateSupplier(SupplierDTO supplier) throws SQLException {
        String query = "UPDATE suppliers SET Company = ?, suppliers_name = ?, suppliers_mobile = ?, " +
                      "suppliers_reg_no = ?, suppliers_address = ?, p_status_id = ? " +
                      "WHERE suppliers_id = ?";
        
        int rowsAffected = DB.executeUpdate(query,
            supplier.getCompany(),
            supplier.getSupplierName(),
            supplier.getMobile(),
            supplier.getRegNo(),
            supplier.getAddress(),
            supplier.getPStatusId(),
            supplier.getSupplierId());
        
        return rowsAffected > 0;
    }
    
    /**
     * Deactivate supplier (set status to inactive)
     */
    public boolean deactivateSupplier(int supplierId) throws SQLException {
        String query = "UPDATE suppliers SET p_status_id = ? WHERE suppliers_id = ?";
        int rowsAffected = DB.executeUpdate(query, STATUS_INACTIVE_ID, supplierId);
        return rowsAffected > 0;
    }
    
    /**
     * Reactivate supplier (set status to active)
     */
    public boolean reactivateSupplier(int supplierId) throws SQLException {
        String query = "UPDATE suppliers SET p_status_id = ? WHERE suppliers_id = ?";
        int rowsAffected = DB.executeUpdate(query, STATUS_ACTIVE_ID, supplierId);
        return rowsAffected > 0;
    }
    
    /**
     * Check if company name already exists
     */
    public boolean companyExists(String company, int excludeSupplierId) throws SQLException {
        String query = "SELECT COUNT(*) FROM suppliers WHERE Company = ? AND suppliers_id != ?";
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }, company, excludeSupplierId);
    }
    
    /**
     * Check if registration number already exists
     */
    public boolean regNoExists(String regNo, int excludeSupplierId) throws SQLException {
        String query = "SELECT COUNT(*) FROM suppliers WHERE suppliers_reg_no = ? AND suppliers_id != ?";
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }, regNo, excludeSupplierId);
    }
    
    /**
     * Get suppliers with outstanding payments
     */
    public List<SupplierDTO> getSuppliersWithOutstanding() throws SQLException {
        String query = buildSupplierQuery("", "active", "has_due");
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            List<SupplierDTO> suppliers = new ArrayList<>();
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplierDTO(rs));
            }
            return suppliers;
        });
    }
    
    /**
     * Get active suppliers with no due
     */
    public List<SupplierDTO> getSuppliersWithNoDue() throws SQLException {
        String query = buildSupplierQuery("", "active", "no_due");
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            List<SupplierDTO> suppliers = new ArrayList<>();
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplierDTO(rs));
            }
            return suppliers;
        });
    }
    
    /**
     * Get inactive suppliers
     */
    public List<SupplierDTO> getInactiveSuppliers() throws SQLException {
        String query = buildSupplierQuery("", "inactive", "all");
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            List<SupplierDTO> suppliers = new ArrayList<>();
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplierDTO(rs));
            }
            return suppliers;
        });
    }
    
    /**
     * Get active suppliers
     */
    public List<SupplierDTO> getActiveSuppliers() throws SQLException {
        String query = buildSupplierQuery("", "active", "all");
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            List<SupplierDTO> suppliers = new ArrayList<>();
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplierDTO(rs));
            }
            return suppliers;
        });
    }
    
    /**
     * Search suppliers by company name, supplier name, or registration number
     */
    public List<SupplierDTO> searchSuppliers(String searchText) throws SQLException {
        String query = buildSupplierQuery(searchText, "active", "all");
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            List<SupplierDTO> suppliers = new ArrayList<>();
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplierDTO(rs));
            }
            return suppliers;
        });
    }
    
    /**
     * Add credit amount for a supplier
     */
    public boolean addCreditAmount(int supplierId, double amount) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM credit_supplier WHERE suppliers_id = ?";
        
        boolean exists = DB.executeQuerySafe(checkQuery, (ResultSet rs) -> {
            return rs.next() && rs.getInt(1) > 0;
        }, supplierId);
        
        if (exists) {
            String updateQuery = "UPDATE credit_supplier SET credit_amount = credit_amount + ? WHERE suppliers_id = ?";
            int rowsAffected = DB.executeUpdate(updateQuery, amount, supplierId);
            return rowsAffected > 0;
        } else {
            String insertQuery = "INSERT INTO credit_supplier (suppliers_id, credit_amount) VALUES (?, ?)";
            int rowsAffected = DB.executeUpdate(insertQuery, supplierId, amount);
            return rowsAffected > 0;
        }
    }
    
    /**
     * Add payment for supplier credit
     */
    public boolean addPayment(int supplierId, double amount, String description) throws SQLException {
        String getCreditIdQuery = "SELECT credit_supplier_id FROM credit_supplier WHERE suppliers_id = ?";
        
        Integer creditSupplierId = DB.executeQuerySafe(getCreditIdQuery, (ResultSet rs) -> {
            if (rs.next()) {
                return rs.getInt("credit_supplier_id");
            }
            return null;
        }, supplierId);
        
        if (creditSupplierId == null) {
            return false;
        }
        
        String insertQuery = "INSERT INTO supplier_credit_pay (credit_supplier_id, credit_pay_amount, description, pay_date) " +
                           "VALUES (?, ?, ?, NOW())";
        
        int rowsAffected = DB.executeUpdate(insertQuery, creditSupplierId, amount, description);
        return rowsAffected > 0;
    }
    
    /**
     * Get payment history for a supplier
     */
    public List<SupplierDTO.PaymentDTO> getPaymentHistory(int supplierId) throws SQLException {
        String query = "SELECT scp.supplier_credit_pay_id, scp.credit_pay_amount, scp.description, " +
                      "scp.pay_date, u.user_name " +
                      "FROM supplier_credit_pay scp " +
                      "LEFT JOIN credit_supplier cs ON cs.credit_supplier_id = scp.credit_supplier_id " +
                      "LEFT JOIN users u ON scp.user_id = u.user_id " +
                      "WHERE cs.suppliers_id = ? " +
                      "ORDER BY scp.pay_date DESC";
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            List<SupplierDTO.PaymentDTO> payments = new ArrayList<>();
            while (rs.next()) {
                SupplierDTO.PaymentDTO payment = new SupplierDTO.PaymentDTO(
                    rs.getInt("supplier_credit_pay_id"),
                    rs.getDouble("credit_pay_amount"),
                    rs.getString("description"),
                    rs.getTimestamp("pay_date"),
                    rs.getString("user_name")
                );
                payments.add(payment);
            }
            return payments;
        }, supplierId);
    }
    
    /**
     * Get total credit amount for a supplier
     */
    public double getTotalCredit(int supplierId) throws SQLException {
        String query = "SELECT COALESCE(credit_amount, 0) FROM credit_supplier WHERE suppliers_id = ?";
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }, supplierId);
    }
    
    /**
     * Get total paid amount for a supplier
     */
    public double getTotalPaid(int supplierId) throws SQLException {
        String query = "SELECT COALESCE(SUM(scp.credit_pay_amount), 0) " +
                      "FROM credit_supplier cs " +
                      "LEFT JOIN supplier_credit_pay scp ON scp.credit_supplier_id = cs.credit_supplier_id " +
                      "WHERE cs.suppliers_id = ? " +
                      "GROUP BY cs.suppliers_id";
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }, supplierId);
    }
    
    /**
     * Get total outstanding amount for a supplier
     */
    public double getTotalOutstanding(int supplierId) throws SQLException {
        double credit = getTotalCredit(supplierId);
        double paid = getTotalPaid(supplierId);
        return credit - paid;
    }
    
    /**
     * Get all supplier IDs and company names
     */
    public List<Object[]> getAllSupplierNames() throws SQLException {
        String query = "SELECT suppliers_id, Company FROM suppliers WHERE p_status_id = ? ORDER BY Company";
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            List<Object[]> suppliers = new ArrayList<>();
            while (rs.next()) {
                suppliers.add(new Object[]{rs.getInt("suppliers_id"), rs.getString("Company")});
            }
            return suppliers;
        }, STATUS_ACTIVE_ID);
    }
    
    /**
     * Get count of suppliers by status
     */
    public int getSupplierCountByStatus(int statusId) throws SQLException {
        String query = "SELECT COUNT(*) FROM suppliers WHERE p_status_id = ?";
        
        return DB.executeQuerySafe(query, (ResultSet rs) -> {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }, statusId);
    }
    
    /**
     * Build supplier query with filters
     */
    private String buildSupplierQuery(String searchText, String status, String dueStatus) {
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT s.suppliers_id, s.Company, s.suppliers_name, s.suppliers_mobile, ");
        query.append("s.suppliers_reg_no, s.suppliers_address, p.p_status, s.p_status_id, ");
        query.append("COALESCE(cs.credit_amount, 0) as credit_amount, ");
        query.append("COALESCE(SUM(scp.credit_pay_amount), 0) as credit_pay_amount ");
        query.append("FROM suppliers s ");
        query.append("LEFT JOIN p_status p ON s.p_status_id = p.p_status_id ");
        query.append("LEFT JOIN credit_supplier cs ON cs.suppliers_id = s.suppliers_id ");
        query.append("LEFT JOIN supplier_credit_pay scp ON scp.credit_supplier_id = cs.credit_supplier_id ");
        query.append("WHERE 1=1 ");
        
        // Search filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            String escapedSearch = escapeSQL(searchText.trim());
            query.append("AND (s.Company LIKE '%").append(escapedSearch).append("%' ");
            query.append("OR s.suppliers_name LIKE '%").append(escapedSearch).append("%' ");
            query.append("OR s.suppliers_reg_no LIKE '%").append(escapedSearch).append("%') ");
        }
        
        // Status filter
        if ("inactive".equalsIgnoreCase(status)) {
            query.append("AND s.p_status_id = ").append(STATUS_INACTIVE_ID).append(" ");
        } else {
            // Default to active
            query.append("AND s.p_status_id = ").append(STATUS_ACTIVE_ID).append(" ");
        }
        
        query.append("GROUP BY s.suppliers_id, s.Company, s.suppliers_name, s.suppliers_mobile, ");
        query.append("s.suppliers_reg_no, s.suppliers_address, p.p_status, s.p_status_id, cs.credit_amount ");
        
        // Due filters - only apply when NOT showing inactive suppliers
        if (!"inactive".equalsIgnoreCase(status)) {
            if ("no_due".equalsIgnoreCase(dueStatus)) {
                query.append("HAVING (COALESCE(cs.credit_amount, 0) - COALESCE(SUM(scp.credit_pay_amount), 0)) = 0 ");
            } else if ("has_due".equalsIgnoreCase(dueStatus)) {
                query.append("HAVING (COALESCE(cs.credit_amount, 0) - COALESCE(SUM(scp.credit_pay_amount), 0)) > 0 ");
            }
        }
        
        query.append("ORDER BY s.Company");
        
        return query.toString();
    }
    
    /**
     * Map ResultSet to SupplierDTO
     */
    private SupplierDTO mapResultSetToSupplierDTO(ResultSet rs) throws SQLException {
        SupplierDTO supplier = new SupplierDTO();
        
        supplier.setSupplierId(rs.getInt("suppliers_id"));
        supplier.setCompany(rs.getString("Company"));
        supplier.setSupplierName(rs.getString("suppliers_name"));
        supplier.setMobile(rs.getString("suppliers_mobile"));
        supplier.setRegNo(rs.getString("suppliers_reg_no"));
        supplier.setAddress(rs.getString("suppliers_address"));
        supplier.setStatus(rs.getString("p_status"));
        supplier.setPStatusId(rs.getInt("p_status_id"));
        supplier.setCreditAmount(rs.getDouble("credit_amount"));
        supplier.setPaidAmount(rs.getDouble("credit_pay_amount"));
        
        return supplier;
    }
    
    /**
     * Escape SQL special characters
     */
    private String escapeSQL(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("'", "''")
                   .replace("%", "\\%")
                   .replace("_", "\\_");
    }
}