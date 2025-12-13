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
}
