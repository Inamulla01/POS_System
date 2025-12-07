package lk.com.pos.dao;

import lk.com.pos.dto.CartSaleDTO;
import lk.com.pos.dto.CartSaleItemDTO;
import lk.com.pos.connection.DB;
import java.sql.*;
import java.util.List;

public class CartSaleDAO {
    
    public int saveSale(CartSaleDTO sale) throws SQLException {
        String query = "INSERT INTO sales (invoice_no, datetime, total, user_id, payment_method_id, status_id, discount_id) " +
                      "VALUES (?, NOW(), ?, ?, ?, ?, ?)";
        
        return DB.insertAndGetId(query, 
            sale.getInvoiceNo(),
            sale.getTotal(),
            sale.getUserId(),
            sale.getPaymentMethodId(),
            sale.getStatusId(),
            sale.getDiscountId()
        );
    }
    
    public boolean saveSaleItems(List<CartSaleItemDTO> saleItems) throws SQLException {
        if (saleItems.isEmpty()) return true;
        
        String query = "INSERT INTO sale_item (qty, price, discount_price, total, sales_id, stock_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        Object[][] batchParams = new Object[saleItems.size()][6];
        for (int i = 0; i < saleItems.size(); i++) {
            CartSaleItemDTO item = saleItems.get(i);
            batchParams[i] = new Object[]{
                item.getQty(),
                item.getPrice(),
                item.getDiscountPrice(),
                item.getTotal(),
                item.getSalesId(),
                item.getStockId()
            };
        }
        
        int[] results = DB.executeBatch(query, batchParams);
        return results.length == saleItems.size();
    }
    
    public boolean updateSaleStatus(int salesId, int statusId, Integer paymentMethodId) throws SQLException {
        if (paymentMethodId != null) {
            String query = "UPDATE sales SET status_id = ?, payment_method_id = ?, datetime = NOW() WHERE sales_id = ?";
            return DB.executeUpdate(query, statusId, paymentMethodId, salesId) > 0;
        } else {
            String query = "UPDATE sales SET status_id = ?, datetime = NOW() WHERE sales_id = ?";
            return DB.executeUpdate(query, statusId, salesId) > 0;
        }
    }
    
    public CartSaleDTO getSaleById(int salesId) throws SQLException {
        String query = "SELECT sales_id, invoice_no, datetime, total, user_id, payment_method_id, status_id, discount_id " +
                      "FROM sales WHERE sales_id = ?";
        
        return DB.executeQuerySafe(query, (rs) -> {
            if (rs.next()) {
                return new CartSaleDTO(
                    rs.getInt("sales_id"),
                    rs.getString("invoice_no"),
                    rs.getTimestamp("datetime"),
                    rs.getDouble("total"),
                    rs.getInt("user_id"),
                    rs.getObject("payment_method_id", Integer.class),
                    rs.getInt("status_id"),
                    rs.getObject("discount_id", Integer.class)
                );
            }
            return null;
        }, salesId);
    }
    
    public String getLastInvoiceNumber() throws SQLException {
        String query = "SELECT invoice_no FROM sales ORDER BY sales_id DESC LIMIT 1";
        
        return DB.executeQuerySafe(query, (rs) -> {
            if (rs.next()) {
                return rs.getString("invoice_no");
            }
            return null;
        });
    }
    
    public String generateInvoiceNumber() throws SQLException {
        String lastInvoice = getLastInvoiceNumber();
        if (lastInvoice != null && lastInvoice.startsWith("INV")) {
            try {
                int lastNumber = Integer.parseInt(lastInvoice.substring(3));
                int newNumber = lastNumber + 1;
                return String.format("INV%05d", newNumber);
            } catch (NumberFormatException e) {
                return "INV" + System.currentTimeMillis();
            }
        }
        return "INV00001";
    }
}