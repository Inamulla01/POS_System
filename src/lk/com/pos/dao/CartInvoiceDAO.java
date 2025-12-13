package lk.com.pos.dao;

import lk.com.pos.dto.CartInvoiceDTO;
import lk.com.pos.dto.CartSaleItemDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartInvoiceDAO {

    /**
     * Retrieves recent invoices within the specified number of hours
     *
     * @param hours Number of hours to look back
     * @param limit Maximum number of invoices to return
     * @return List of CartInvoiceDTO objects
     * @throws SQLException if database error occurs
     */
    public List<CartInvoiceDTO> getRecentInvoices(int hours, int limit) throws SQLException {
        String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, s.status_id, "
                + "st.status_type, pm.payment_method_name as payment_method, u.user_id "
                + "FROM sales s "
                + "INNER JOIN i_status st ON s.status_id = st.status_id "
                + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                + "INNER JOIN user u ON s.user_id = u.user_id "
                + "WHERE s.datetime >= DATE_SUB(NOW(), INTERVAL ? HOUR) "
                + "AND st.status_type IN ('Hold', 'Completed') "
                + // Only active statuses
                "ORDER BY CASE "
                + "  WHEN st.status_type = 'Hold' THEN 1 "
                + "  WHEN st.status_type = 'Completed' THEN 2 "
                + "  ELSE 3 END, "
                + "s.datetime DESC LIMIT ?";

        return DB.executeQuerySafe(query, (rs) -> {
            List<CartInvoiceDTO> invoices = new ArrayList<>();
            while (rs.next()) {
                invoices.add(new CartInvoiceDTO(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        rs.getString("payment_method"), // This can be null for hold sales
                        rs.getInt("user_id")
                ));
            }
            return invoices;
        }, hours, limit);
    }

    /**
     * Retrieves all sale items for a specific sale
     *
     * @param salesId The ID of the sale
     * @return List of CartSaleItemDTO objects
     * @throws SQLException if database error occurs
     */
    public List<CartSaleItemDTO> getSaleItemsBySaleId(int salesId) throws SQLException {
        String query = "SELECT si.sale_item_id, si.qty, si.price, si.discount_price, si.total, "
                + "si.sales_id, si.stock_id "
                + "FROM sale_item si "
                + "WHERE si.sales_id = ?";

        return DB.executeQuerySafe(query, (rs) -> {
            List<CartSaleItemDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new CartSaleItemDTO(
                        rs.getInt("sale_item_id"),
                        rs.getInt("qty"),
                        rs.getDouble("price"),
                        rs.getDouble("discount_price"),
                        rs.getDouble("total"),
                        rs.getInt("sales_id"),
                        rs.getInt("stock_id")
                ));
            }
            return items;
        }, salesId);
    }

    /**
     * Retrieves a specific invoice by sales ID
     *
     * @param salesId The ID of the sale
     * @return CartInvoiceDTO object or null if not found
     * @throws SQLException if database error occurs
     */
    public CartInvoiceDTO getInvoiceById(int salesId) throws SQLException {
        String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, s.status_id, "
                + "st.status_type, pm.payment_method_name as payment_method, u.user_id "
                + "FROM sales s "
                + "INNER JOIN i_status st ON s.status_id = st.status_id "
                + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                + "INNER JOIN user u ON s.user_id = u.user_id "
                + "WHERE s.sales_id = ?";

        List<CartInvoiceDTO> result = DB.executeQuerySafe(query, (rs) -> {
            List<CartInvoiceDTO> invoices = new ArrayList<>();
            if (rs.next()) {
                invoices.add(new CartInvoiceDTO(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        rs.getString("payment_method"),
                        rs.getInt("user_id")
                ));
            }
            return invoices;
        }, salesId);

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Retrieves all hold invoices for the current user
     *
     * @param userId The ID of the user
     * @return List of CartInvoiceDTO objects
     * @throws SQLException if database error occurs
     */
    public List<CartInvoiceDTO> getHoldInvoicesByUser(int userId) throws SQLException {
        String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, s.status_id, "
                + "st.status_type, pm.payment_method_name as payment_method, u.user_id "
                + "FROM sales s "
                + "INNER JOIN i_status st ON s.status_id = st.status_id "
                + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                + "INNER JOIN user u ON s.user_id = u.user_id "
                + "WHERE s.user_id = ? AND st.status_type = 'Hold' "
                + "ORDER BY s.datetime DESC";

        return DB.executeQuerySafe(query, (rs) -> {
            List<CartInvoiceDTO> invoices = new ArrayList<>();
            while (rs.next()) {
                invoices.add(new CartInvoiceDTO(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        rs.getString("payment_method"),
                        rs.getInt("user_id")
                ));
            }
            return invoices;
        }, userId);
    }

    /**
     * Retrieves invoices by status type
     *
     * @param statusType The status type (e.g., "Hold", "Completed")
     * @param limit Maximum number of invoices to return
     * @return List of CartInvoiceDTO objects
     * @throws SQLException if database error occurs
     */
    public List<CartInvoiceDTO> getInvoicesByStatus(String statusType, int limit) throws SQLException {
        String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, s.status_id, "
                + "st.status_type, pm.payment_method_name as payment_method, u.user_id "
                + "FROM sales s "
                + "INNER JOIN i_status st ON s.status_id = st.status_id "
                + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                + "INNER JOIN user u ON s.user_id = u.user_id "
                + "WHERE st.status_type = ? "
                + "ORDER BY s.datetime DESC LIMIT ?";

        return DB.executeQuerySafe(query, (rs) -> {
            List<CartInvoiceDTO> invoices = new ArrayList<>();
            while (rs.next()) {
                invoices.add(new CartInvoiceDTO(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        rs.getString("payment_method"),
                        rs.getInt("user_id")
                ));
            }
            return invoices;
        }, statusType, limit);
    }

    /**
     * Searches invoices by invoice number
     *
     * @param invoiceNo The invoice number to search for (partial match)
     * @return List of CartInvoiceDTO objects
     * @throws SQLException if database error occurs
     */
    public List<CartInvoiceDTO> searchInvoicesByNumber(String invoiceNo) throws SQLException {
        String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, s.status_id, "
                + "st.status_type, pm.payment_method_name as payment_method, u.user_id "
                + "FROM sales s "
                + "INNER JOIN i_status st ON s.status_id = st.status_id "
                + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                + "INNER JOIN user u ON s.user_id = u.user_id "
                + "WHERE s.invoice_no LIKE ? "
                + "ORDER BY s.datetime DESC LIMIT 50";

        return DB.executeQuerySafe(query, (rs) -> {
            List<CartInvoiceDTO> invoices = new ArrayList<>();
            while (rs.next()) {
                invoices.add(new CartInvoiceDTO(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        rs.getString("payment_method"),
                        rs.getInt("user_id")
                ));
            }
            return invoices;
        }, "%" + invoiceNo + "%");
    }

    /**
     * Gets the total count of invoices by status
     *
     * @param statusType The status type (e.g., "Hold", "Completed")
     * @return Count of invoices
     * @throws SQLException if database error occurs
     */
    public int getInvoiceCountByStatus(String statusType) throws SQLException {
        String query = "SELECT COUNT(*) as count "
                + "FROM sales s "
                + "INNER JOIN i_status st ON s.status_id = st.status_id "
                + "WHERE st.status_type = ?";

        List<Integer> result = DB.executeQuerySafe(query, (rs) -> {
            List<Integer> counts = new ArrayList<>();
            if (rs.next()) {
                counts.add(rs.getInt("count"));
            }
            return counts;
        }, statusType);

        return result.isEmpty() ? 0 : result.get(0);
    }

    /**
     * Gets invoices within a date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of CartInvoiceDTO objects
     * @throws SQLException if database error occurs
     */
    public List<CartInvoiceDTO> getInvoicesByDateRange(Date startDate, Date endDate) throws SQLException {
        String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, s.status_id, "
                + "st.status_type, pm.payment_method_name as payment_method, u.user_id "
                + "FROM sales s "
                + "INNER JOIN i_status st ON s.status_id = st.status_id "
                + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                + "INNER JOIN user u ON s.user_id = u.user_id "
                + "WHERE s.datetime BETWEEN ? AND ? "
                + "ORDER BY s.datetime DESC";

        return DB.executeQuerySafe(query, (rs) -> {
            List<CartInvoiceDTO> invoices = new ArrayList<>();
            while (rs.next()) {
                invoices.add(new CartInvoiceDTO(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        rs.getString("payment_method"),
                        rs.getInt("user_id")
                ));
            }
            return invoices;
        }, startDate, endDate);
    }
    public boolean deleteHoldInvoice(int salesId) throws SQLException {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Get all sale items to restore stock
            List<CartSaleItemDTO> saleItems = getSaleItemsBySaleId(salesId);
            
            // 2. Restore stock for each item
            for (CartSaleItemDTO item : saleItems) {
                String restoreStockQuery = "UPDATE stock s " +
                                         "INNER JOIN sale_item si ON s.stock_id = si.stock_id " +
                                         "SET s.qty = s.qty + si.qty " +
                                         "WHERE si.sale_item_id = ?";
                
                PreparedStatement pst = conn.prepareStatement(restoreStockQuery);
                pst.setInt(1, item.getSaleItemId());
                pst.executeUpdate();
                pst.close();
            }
            
            // 3. Delete sale items
            String deleteItemsQuery = "DELETE FROM sale_item WHERE sales_id = ?";
            PreparedStatement pst1 = conn.prepareStatement(deleteItemsQuery);
            pst1.setInt(1, salesId);
            pst1.executeUpdate();
            pst1.close();
            
            // 4. Delete the sale record
            String deleteSaleQuery = "DELETE FROM sales WHERE sales_id = ?";
            PreparedStatement pst2 = conn.prepareStatement(deleteSaleQuery);
            pst2.setInt(1, salesId);
            pst2.executeUpdate();
            pst2.close();
            
            conn.commit(); // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Deletes hold invoices older than specified hours
     *
     * @param hours Age in hours
     * @return Number of invoices deleted
     * @throws SQLException if database error occurs
     */
    public int deleteOldHoldInvoices(int hours) throws SQLException {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            conn.setAutoCommit(false);
            
            // Get hold invoices older than specified hours
            String getOldInvoicesQuery = "SELECT s.sales_id FROM sales s " +
                                        "INNER JOIN i_status st ON s.status_id = st.status_id " +
                                        "WHERE st.status_type = 'Hold' " +
                                        "AND s.datetime < DATE_SUB(NOW(), INTERVAL ? HOUR)";
            
            List<Integer> oldInvoiceIds = new ArrayList<>();
            PreparedStatement pst1 = conn.prepareStatement(getOldInvoicesQuery);
            pst1.setInt(1, hours);
            ResultSet rs = pst1.executeQuery();
            while (rs.next()) {
                oldInvoiceIds.add(rs.getInt("sales_id"));
            }
            rs.close();
            pst1.close();
            
            int deletedCount = 0;
            
            // Delete each old invoice
            for (int invoiceId : oldInvoiceIds) {
                if (deleteHoldInvoiceTransaction(conn, invoiceId)) {
                    deletedCount++;
                }
            }
            
            conn.commit();
            return deletedCount;
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Helper method to delete invoice within transaction
     */
    private boolean deleteHoldInvoiceTransaction(Connection conn, int salesId) throws SQLException {
        try {
            // 1. Get all sale items to restore stock
            List<CartSaleItemDTO> saleItems = getSaleItemsBySaleId(salesId);
            
            // 2. Restore stock for each item
            for (CartSaleItemDTO item : saleItems) {
                String restoreStockQuery = "UPDATE stock s " +
                                         "INNER JOIN sale_item si ON s.stock_id = si.stock_id " +
                                         "SET s.qty = s.qty + si.qty " +
                                         "WHERE si.sale_item_id = ?";
                
                PreparedStatement pst = conn.prepareStatement(restoreStockQuery);
                pst.setInt(1, item.getSaleItemId());
                pst.executeUpdate();
                pst.close();
            }
            
            // 3. Delete sale items
            String deleteItemsQuery = "DELETE FROM sale_item WHERE sales_id = ?";
            PreparedStatement pst1 = conn.prepareStatement(deleteItemsQuery);
            pst1.setInt(1, salesId);
            pst1.executeUpdate();
            pst1.close();
            
            // 4. Delete the sale record
            String deleteSaleQuery = "DELETE FROM sales WHERE sales_id = ?";
            PreparedStatement pst2 = conn.prepareStatement(deleteSaleQuery);
            pst2.setInt(1, salesId);
            int rowsAffected = pst2.executeUpdate();
            pst2.close();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            throw e;
        }
    }
}
