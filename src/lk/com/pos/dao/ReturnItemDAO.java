package lk.com.pos.dao;

import lk.com.pos.connection.DB;
import lk.com.pos.dto.ReturnItemDetailsDTO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReturnItemDAO {
    
    // Get return item details for display
    public List<ReturnItemDetailsDTO> getReturnItemDetails(int returnId) throws SQLException {
        String sql = "SELECT " +
                    "ri.return_qty, ri.unit_return_price, ri.discount_price, ri.total_return_amount, " +
                    "p.product_name, " +
                    "st.batch_no " +
                    "FROM return_item ri " +
                    "INNER JOIN stock st ON ri.stock_id = st.stock_id " +
                    "INNER JOIN product p ON st.product_id = p.product_id " +
                    "WHERE ri.return_id = ? " +
                    "ORDER BY ri.return_item_id";
        
        return DB.executeQuerySafe(sql, rs -> {
            List<ReturnItemDetailsDTO> items = new ArrayList<>();
            while (rs.next()) {
                ReturnItemDetailsDTO item = new ReturnItemDetailsDTO();
                item.setProductName(rs.getString("product_name"));
                item.setQty(String.valueOf(rs.getDouble("return_qty")));
                item.setPrice(rs.getDouble("unit_return_price"));
                item.setDiscountPrice(rs.getDouble("discount_price"));
                item.setTotal(rs.getDouble("total_return_amount"));
                item.setBatchNo(rs.getString("batch_no"));
                
                items.add(item);
            }
            return items;
        }, returnId);
    }

    // Delete return item
    public boolean deleteReturnItem(int returnItemId) throws SQLException {
        String sql = "DELETE FROM return_item WHERE return_item_id = ?";
        int rows = DB.executeUpdate(sql, returnItemId);
        return rows > 0;
    }
}