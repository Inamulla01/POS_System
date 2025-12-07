package lk.com.pos.dao;

import lk.com.pos.dto.InvoiceDTO;
import lk.com.pos.dto.SaleItemDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {
    
    /**
     * Fetch invoices from database with search and period filter
     */
    public List<InvoiceDTO> fetchInvoicesFromDatabase(String searchText, String period) throws Exception {
        List<InvoiceDTO> invoices = new ArrayList<>();
        
        String baseQuery = "SELECT " +
            "s.sales_id, s.invoice_no, s.datetime, s.total, " +
            "s.status_id, " +
            "st.status_name, " +
            "(SELECT COALESCE(SUM(si.discount_price), 0) FROM sale_item si WHERE si.sales_id = s.sales_id) as total_item_discount, " +
            "COALESCE(d.discount, 0) as total_sale_discount, " +
            "pm.payment_method_name, u.name as cashier_name, " +
            "(SELECT cc.customer_name FROM credit c " +
            "INNER JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id " +
            "WHERE c.sales_id = s.sales_id LIMIT 1) as customer_name " +
            "FROM sales s " +
            "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id " +
            "LEFT JOIN user u ON s.user_id = u.user_id " +
            "LEFT JOIN status st ON s.status_id = st.status_id " +
            "LEFT JOIN discount d ON s.discount_id = d.discount_id ";
        
        StringBuilder whereClause = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        whereClause.append("WHERE 1=1 ");
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            whereClause.append("AND s.invoice_no LIKE ? ");
            parameters.add("%" + searchText + "%");
        }
        
        String dateFilter = getDateFilter(period);
        if (!dateFilter.isEmpty()) {
            whereClause.append("AND ").append(dateFilter);
        }
        
        String orderBy = " ORDER BY s.datetime DESC";
        String finalQuery = baseQuery + whereClause.toString() + orderBy;
        
        System.out.println("Executing Query: " + finalQuery);
        
        return DB.executeQuerySafe(finalQuery, rs -> {
            List<InvoiceDTO> result = new ArrayList<>();
            while (rs.next()) {
                InvoiceDTO data = new InvoiceDTO();
                data.setSalesId(rs.getInt("sales_id"));
                data.setInvoiceNo(rs.getString("invoice_no"));
                data.setDatetime(rs.getTimestamp("datetime"));
                data.setTotal(rs.getDouble("total"));
                data.setItemDiscount(rs.getDouble("total_item_discount"));
                data.setStatusId(rs.getInt("status_id"));
                data.setSaleStatus(rs.getString("status_name"));
                data.setSaleDiscount(rs.getDouble("total_sale_discount"));
                data.setTotalDiscount(data.getItemDiscount() + data.getSaleDiscount());
                data.setPaymentMethod(rs.getString("payment_method_name"));
                data.setCashierName(rs.getString("cashier_name"));
                
                String custName = rs.getString("customer_name");
                data.setCustomerName((custName != null && !custName.isEmpty()) ? custName : "Walk-in Customer");
                
                result.add(data);
            }
            return result;
        }, parameters.toArray());
    }
    
    /**
     * Load sale items for a specific sales ID
     */
    public List<SaleItemDTO> loadSaleItems(int salesId) throws Exception {
        String query = "SELECT " +
            "si.qty, si.price, si.discount_price, si.total, " +
            "p.product_name, p.product_id, " +
            "st.stock_id, st.batch_no " +
            "FROM sale_item si " +
            "INNER JOIN stock st ON si.stock_id = st.stock_id " +
            "INNER JOIN product p ON st.product_id = p.product_id " +
            "WHERE si.sales_id = ? " +
            "ORDER BY si.sale_item_id";
        
        return DB.executeQuerySafe(query, rs -> {
            List<SaleItemDTO> items = new ArrayList<>();
            while (rs.next()) {
                String productName = rs.getString("product_name");
                int qty = rs.getInt("qty");
                double price = rs.getDouble("price");
                double discountPrice = rs.getDouble("discount_price");
                double itemTotal = rs.getDouble("total");
                String batchNo = rs.getString("batch_no");
                
                items.add(new SaleItemDTO(productName, qty, price, discountPrice, itemTotal, batchNo));
            }
            return items;
        }, salesId);
    }
    
    /**
     * Get date filter SQL based on period
     */
    private String getDateFilter(String period) {
        if (period == null) return "";
        
        // Remove any emoji characters
        period = period.replaceAll("[^\\x00-\\x7F]", "").trim();
        
        switch (period) {
            case "Today":
                return "DATE(s.datetime) = CURDATE()";
            case "Last 7 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            case "Last 30 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
            case "Last 90 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 90 DAY)";
            case "1 Year":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 1 YEAR)";
            case "2 Years":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 2 YEAR)";
            case "5 Years":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 5 YEAR)";
            case "10 Years":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 10 YEAR)";
            case "20 Years":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 20 YEAR)";
            default:
                return "";
        }
    }
    
    /**
     * Get invoice count for statistics
     */
    public int getInvoiceCount(String searchText, String period) throws Exception {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) as count FROM sales WHERE 1=1 ");
        List<Object> parameters = new ArrayList<>();
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            query.append("AND invoice_no LIKE ? ");
            parameters.add("%" + searchText + "%");
        }
        
        String dateFilter = getDateFilter(period);
        if (!dateFilter.isEmpty()) {
            query.append("AND ").append(dateFilter);
        }
        
        return DB.executeQuerySafe(query.toString(), rs -> {
            return rs.next() ? rs.getInt("count") : 0;
        }, parameters.toArray());
    }
    
    /**
     * Get total sales amount for period
     */
    public double getTotalSales(String period) throws Exception {
        String query = "SELECT COALESCE(SUM(total), 0) as total_sales FROM sales WHERE 1=1 " + 
                      getDateFilter(period);
        
        return DB.executeQuerySafe(query, rs -> {
            return rs.next() ? rs.getDouble("total_sales") : 0.0;
        });
    }
    
    /**
     * Debug: Check status values in database
     */
    public void debugStatusValues() throws Exception {
        String query = "SELECT DISTINCT s.status_id, st.status_name, COUNT(*) as count " +
                      "FROM sales s " +
                      "LEFT JOIN status st ON s.status_id = st.status_id " +
                      "GROUP BY s.status_id, st.status_name " +
                      "ORDER BY s.status_id";
        
        DB.executeQuerySafe(query, rs -> {
            System.out.println("=== Sales Status Distribution ===");
            while (rs.next()) {
                int statusId = rs.getInt("status_id");
                String statusName = rs.getString("status_name");
                int count = rs.getInt("count");
                System.out.println("Status ID: " + statusId + ", Name: '" + statusName + "', Count: " + count);
            }
            return null;
        });
        
        String statusQuery = "SELECT status_id, status_name FROM status ORDER BY status_id";
        DB.executeQuerySafe(statusQuery, rs -> {
            System.out.println("=== All Status Types in status table ===");
            while (rs.next()) {
                int statusId = rs.getInt("status_id");
                String statusName = rs.getString("status_name");
                System.out.println("Status ID: " + statusId + ", Name: '" + statusName + "'");
            }
            return null;
        });
    }
}