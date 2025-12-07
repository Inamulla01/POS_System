package lk.com.pos.dao;

import lk.com.pos.dto.CartInvoiceDTO;
import lk.com.pos.dto.CartSaleItemDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartInvoiceDAO {
    
    public List<CartInvoiceDTO> getRecentInvoices(int hours, int limit) throws SQLException {
        String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, s.status_id, " +
                      "st.status_type, pm.method_name as payment_method, u.user_id " +
                      "FROM sales s " +
                      "INNER JOIN i_status st ON s.status_id = st.status_id " +
                      "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id " +
                      "INNER JOIN user u ON s.user_id = u.user_id " +
                      "WHERE s.datetime >= DATE_SUB(NOW(), INTERVAL ? HOUR) " +
                      "ORDER BY CASE WHEN st.status_type = 'Hold' THEN 1 WHEN st.status_type = 'Completed' THEN 2 ELSE 3 END, " +
                      "s.datetime DESC LIMIT ?";
        
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
        }, hours, limit);
    }
    
    public List<CartSaleItemDTO> getSaleItemsBySaleId(int salesId) throws SQLException {
        String query = "SELECT si.sale_item_id, si.qty, si.price, si.discount_price, si.total, " +
                      "si.sales_id, si.stock_id " +
                      "FROM sale_item si " +
                      "WHERE si.sales_id = ?";
        
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
}