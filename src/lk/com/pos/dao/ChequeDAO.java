
package lk.com.pos.dao;

import lk.com.pos.dto.ChequeDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChequeDAO - Ultra Fast Data Access Object for Cheque operations
 * ✅ FIXED: No more connection leaks - all connections properly closed
 */
public class ChequeDAO {
    
    // Cache for frequently accessed data (performance boost)
    private static final ConcurrentHashMap<String, Integer> typeCache = new ConcurrentHashMap<>();
    
    /**
     * 🚀 Get all cheques with filters - FIXED CONNECTION LEAK
     * Now properly manages Connection, Statement, and ResultSet lifecycle
     */
    public List<ChequeDTO> getCheques(String searchText, boolean bouncedOnly, 
                                     boolean clearedOnly, boolean pendingOnly) throws SQLException {
        String query = buildChequeQuery(searchText, bouncedOnly, clearedOnly, pendingOnly);
        
        List<ChequeDTO> cheques = new ArrayList<>();
        
        // ✅ FIXED: Properly manage all resources with try-with-resources
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ChequeDTO data = mapResultSetToDTO(rs);
                cheques.add(data);
            }
        }
        // All resources (Connection, Statement, ResultSet) are automatically closed here
        
        return cheques;
    }
    
    /**
     * 🔄 Update cheque status - FIXED CONNECTION LEAK
     */
    public boolean updateChequeStatus(int chequeId, String newStatus) throws SQLException {
        // Get type ID from cache or database
        Integer typeId = typeCache.get(newStatus);
        if (typeId == null) {
            typeId = getChequeTypeIdFromDB(newStatus);
            if (typeId != null) {
                typeCache.put(newStatus, typeId);
            }
        }
        
        if (typeId != null && typeId > 0) {
            String updateQuery = "UPDATE cheque SET cheque_type_id = ? WHERE cheque_id = ?";
            
            // ✅ Using DB.executeUpdate() is safe - it manages connections internally
            int rowsUpdated = DB.executeUpdate(updateQuery, typeId, chequeId);
            return rowsUpdated > 0;
        }
        
        return false;
    }
    
    /**
     * 📋 Get current cheque status and type ID - FIXED CONNECTION LEAK
     */
    public ChequeStatus getChequeStatus(int chequeId) throws SQLException {
        String query = "SELECT ct.cheque_type, ct.cheque_type_id FROM cheque ch "
                + "INNER JOIN cheque_type ct ON ch.cheque_type_id = ct.cheque_type_id "
                + "WHERE ch.cheque_id = ?";
        
        // ✅ FIXED: Properly manage all resources
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, chequeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ChequeStatus(
                        rs.getString("cheque_type"),
                        rs.getInt("cheque_type_id")
                    );
                }
            }
        }
        // All resources automatically closed here
        
        return null;
    }
    
    /**
     * 🔍 Get cheque type ID from database - FIXED CONNECTION LEAK
     */
    private Integer getChequeTypeIdFromDB(String chequeType) throws SQLException {
        String query = "SELECT cheque_type_id FROM cheque_type WHERE cheque_type = ?";
        
        // ✅ FIXED: Properly manage all resources
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, chequeType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cheque_type_id");
                }
            }
        }
        // All resources automatically closed here
        
        return null;
    }
    
    /**
     * 🗺️ Map ResultSet to DTO (same mapping as before)
     */
    private ChequeDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        ChequeDTO data = new ChequeDTO();
        
        data.setChequeId(rs.getInt("cheque_id"));
        data.setChequeNo(rs.getString("cheque_no"));
        data.setCustomerName(rs.getString("customer_name"));
        data.setInvoiceNo(rs.getString("invoice_no"));
        data.setBankName(rs.getString("bank_name"));
        data.setBranch(rs.getString("branch"));
        data.setPhone(rs.getString("customer_phone_no"));
        data.setNic(rs.getString("nic"));
        data.setAddress(rs.getString("customer_address"));
        data.setGivenDate(rs.getString("given_date"));
        data.setChequeDate(rs.getString("cheque_date"));
        data.setChequeType(rs.getString("cheque_type"));
        data.setChequeAmount(rs.getDouble("cheque_amount"));
        data.setSalesId(rs.getInt("sales_id"));
        
        return data;
    }
    
    /**
     * 🎯 Build dynamic query (same logic as before)
     */
    private String buildChequeQuery(String searchText, boolean bouncedOnly,
                                   boolean clearedOnly, boolean pendingOnly) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ch.cheque_id, ch.cheque_no, ch.cheque_date, ");
        query.append("ch.bank_name, ch.branch, ch.amount as cheque_amount, ch.sales_id, ");
        query.append("cc.customer_name, cc.customer_phone_no, cc.nic, cc.customer_address, ");
        query.append("s.invoice_no, ct.cheque_type, ");
        query.append("DATE(ch.given_date) as given_date ");
        query.append("FROM cheque ch ");
        query.append("LEFT JOIN credit_customer cc ON ch.credit_customer_id = cc.customer_id ");
        query.append("LEFT JOIN sales s ON ch.sales_id = s.sales_id ");
        query.append("LEFT JOIN cheque_type ct ON ch.cheque_type_id = ct.cheque_type_id ");
        query.append("WHERE 1=1 ");

        if (isValidSearchText(searchText)) {
            String escapedSearch = escapeSQL(searchText);
            query.append("AND (ch.cheque_no LIKE '%").append(escapedSearch).append("%' ");
            query.append("OR cc.customer_name LIKE '%").append(escapedSearch).append("%' ");
            query.append("OR s.invoice_no LIKE '%").append(escapedSearch).append("%') ");
        }

        String statusFilter = buildStatusFilter(bouncedOnly, clearedOnly, pendingOnly);
        query.append(statusFilter);

        query.append("ORDER BY ch.cheque_id DESC");

        return query.toString();
    }
    
    // Helper methods (same as before)
    private boolean isValidSearchText(String searchText) {
        return searchText != null && !searchText.isEmpty() && 
               !searchText.equals("Search By Cheque No, Customer Name or Invoice No");
    }
    
    private String escapeSQL(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("'", "''")
                   .replace("%", "\\%")
                   .replace("_", "\\_");
    }
    
    private String buildStatusFilter(boolean bouncedOnly, boolean clearedOnly, boolean pendingOnly) {
        if (bouncedOnly) return "AND ct.cheque_type = 'Bounced' ";
        if (clearedOnly) return "AND ct.cheque_type = 'Cleared' ";
        if (pendingOnly) return "AND ct.cheque_type = 'Pending' ";
        return "";
    }
    
    /**
     * Inner class for status information
     */
    public static class ChequeStatus {
        private final String chequeType;
        private final int typeId;
        
        public ChequeStatus(String chequeType, int typeId) {
            this.chequeType = chequeType;
            this.typeId = typeId;
        }
        
        public String getChequeType() { return chequeType; }
        public int getTypeId() { return typeId; }
    }
}