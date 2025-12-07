package lk.com.pos.dao;

import lk.com.pos.connection.DB;
import lk.com.pos.dto.ReturnDTO;
import lk.com.pos.dto.ReturnDetailsDTO;
import lk.com.pos.dto.ReturnItemDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReturnDAO {

    // Get all returns with filters
    public List<ReturnDetailsDTO> getReturnsWithFilters(String searchText, String period, String reason) throws SQLException {
        String baseQuery = "SELECT " +
            "r.return_id, r.return_date, r.total_return_amount, r.total_discount_price, " +
            "s.invoice_no, s.total as original_total, " +
            "rr.reason as return_reason, " +
            "ps.p_status as status_name, " +
            "u.name as processed_by, " +
            "pm.payment_method_name, " +
            "COALESCE(cc.customer_name, 'Walk-in Customer') as customer_name " +
            "FROM `return` r " +
            "INNER JOIN sales s ON r.sales_id = s.sales_id " +
            "INNER JOIN return_reason rr ON r.return_reason_id = rr.return_reason_id " +
            "INNER JOIN p_status ps ON r.status_id = ps.p_status_id " +
            "INNER JOIN user u ON r.user_id = u.user_id " +
            "INNER JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id " +
            "LEFT JOIN credit c ON s.sales_id = c.sales_id " +
            "LEFT JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id ";
        
        StringBuilder whereClause = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            whereClause.append("WHERE s.invoice_no LIKE ? ");
            parameters.add("%" + searchText + "%");
        }
        
        String dateFilter = getDateFilter(period);
        if (!dateFilter.isEmpty()) {
            if (whereClause.length() == 0) {
                whereClause.append("WHERE ").append(dateFilter);
            } else {
                whereClause.append("AND ").append(dateFilter);
            }
        }
        
        if (reason != null && !reason.equals("All Reasons")) {
            if (whereClause.length() == 0) {
                whereClause.append("WHERE rr.reason LIKE ? ");
            } else {
                whereClause.append("AND rr.reason LIKE ? ");
            }
            parameters.add("%" + reason + "%");
        }
        
        String orderBy = " ORDER BY r.return_date DESC";
        String finalQuery = baseQuery + whereClause.toString() + orderBy;
        
        return DB.executeQuerySafe(finalQuery, rs -> {
            List<ReturnDetailsDTO> returns = new ArrayList<>();
            while (rs.next()) {
                ReturnDetailsDTO dto = new ReturnDetailsDTO();
                dto.setReturnId(rs.getInt("return_id"));
                dto.setInvoiceNo(rs.getString("invoice_no"));
                
                Timestamp timestamp = rs.getTimestamp("return_date");
                if (timestamp != null) {
                    dto.setReturnDate(timestamp.toLocalDateTime());
                }
                
                dto.setReturnAmount(rs.getDouble("total_return_amount"));
                dto.setDiscountPrice(rs.getDouble("total_discount_price"));
                dto.setOriginalTotal(rs.getDouble("original_total"));
                dto.setReturnReason(rs.getString("return_reason"));
                dto.setStatusName(rs.getString("status_name"));
                dto.setProcessedBy(rs.getString("processed_by"));
                dto.setPaymentMethod(rs.getString("payment_method_name"));
                dto.setCustomerName(rs.getString("customer_name"));
                
                returns.add(dto);
            }
            return returns;
        }, parameters.toArray());
    }

    // Helper method for date filtering
    private String getDateFilter(String period) {
        if (period == null) return "";
        
        switch (period) {
            case "Today":
                return "DATE(r.return_date) = CURDATE() ";
            case "Last 7 Days":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 7 DAY) ";
            case "Last 30 Days":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 30 DAY) ";
            case "Last 90 Days":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 90 DAY) ";
            case "1 Year":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 1 YEAR) ";
            case "2 Years":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 2 YEAR) ";
            case "5 Years":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 5 YEAR) ";
            case "10 Years":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 10 YEAR) ";
            case "20 Years":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 20 YEAR) ";
            default:
                return "";
        }
    }

    // Add new return
    public int addReturn(ReturnDTO returnDTO) throws SQLException {
        String sql = "INSERT INTO `return` (sales_id, return_date, total_return_amount, " +
                    "total_discount_price, return_reason_id, status_id, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        return DB.insertAndGetId(sql, 
            returnDTO.getSalesId(),
            Timestamp.valueOf(returnDTO.getReturnDate()),
            returnDTO.getTotalReturnAmount(),
            returnDTO.getTotalDiscountPrice(),
            returnDTO.getReturnReasonId(),
            returnDTO.getStatusId(),
            returnDTO.getUserId()
        );
    }

    // Add return item
    public boolean addReturnItem(ReturnItemDTO itemDTO) throws SQLException {
        String sql = "INSERT INTO return_item (return_id, stock_id, return_qty, " +
                    "unit_return_price, discount_price, total_return_amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        int rows = DB.executeUpdate(sql,
            itemDTO.getReturnId(),
            itemDTO.getStockId(),
            itemDTO.getReturnQty(),
            itemDTO.getUnitReturnPrice(),
            itemDTO.getDiscountPrice(),
            itemDTO.getTotalReturnAmount()
        );
        
        return rows > 0;
    }

    // Get return by ID
    public ReturnDTO getReturnById(int returnId) throws SQLException {
        String sql = "SELECT * FROM `return` WHERE return_id = ?";
        
        return DB.executeQuerySafe(sql, rs -> {
            if (rs.next()) {
                ReturnDTO dto = new ReturnDTO();
                dto.setReturnId(rs.getInt("return_id"));
                dto.setSalesId(rs.getInt("sales_id"));
                
                Timestamp timestamp = rs.getTimestamp("return_date");
                if (timestamp != null) {
                    dto.setReturnDate(timestamp.toLocalDateTime());
                }
                
                dto.setTotalReturnAmount(rs.getDouble("total_return_amount"));
                dto.setTotalDiscountPrice(rs.getDouble("total_discount_price"));
                dto.setReturnReasonId(rs.getInt("return_reason_id"));
                dto.setStatusId(rs.getInt("status_id"));
                dto.setUserId(rs.getInt("user_id"));
                return dto;
            }
            return null;
        }, returnId);
    }

    // Get return items for a specific return
    public List<ReturnItemDTO> getReturnItems(int returnId) throws SQLException {
        String sql = "SELECT * FROM return_item WHERE return_id = ?";
        
        return DB.executeQuerySafe(sql, rs -> {
            List<ReturnItemDTO> items = new ArrayList<>();
            while (rs.next()) {
                ReturnItemDTO item = new ReturnItemDTO();
                item.setReturnItemId(rs.getInt("return_item_id"));
                item.setReturnId(rs.getInt("return_id"));
                item.setStockId(rs.getInt("stock_id"));
                item.setReturnQty(rs.getDouble("return_qty"));
                item.setUnitReturnPrice(rs.getDouble("unit_return_price"));
                item.setDiscountPrice(rs.getDouble("discount_price"));
                item.setTotalReturnAmount(rs.getDouble("total_return_amount"));
                
                items.add(item);
            }
            return items;
        }, returnId);
    }
}