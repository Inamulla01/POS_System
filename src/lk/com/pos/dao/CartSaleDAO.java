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
    
    int salesId = DB.insertAndGetId(query, 
        sale.getInvoiceNo(),
        sale.getTotal(),
        sale.getUserId(),
        sale.getPaymentMethodId(),
        sale.getStatusId(),
        sale.getDiscountId()
    );
    
    // ========================================
    // ADD THIS CODE BLOCK - CRITICAL SAFETY
    // ========================================
    if (salesId > 2_000_000_000) { // 93% of max int
        String warning = String.format(
            "⚠️ URGENT: Sales ID at %d\n" +
            "System approaching maximum capacity!\n" +
            "Contact IT department immediately.\n" +
            "Max safe value: 2,147,483,647",
            salesId
        );
        
        javax.swing.JOptionPane.showMessageDialog(
            null,
            warning,
            "System Capacity Warning",
            javax.swing.JOptionPane.WARNING_MESSAGE
        );
        
        // Log to file for monitoring
        try (java.io.FileWriter fw = new java.io.FileWriter("URGENT_SYSTEM_WARNING.txt", true)) {
            fw.write(new java.util.Date() + ": " + warning + "\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ========================================
    
    return salesId;
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
    
    // Original logic with safety checks
    if (lastInvoice != null && lastInvoice.startsWith("INV")) {
        try {
            int lastNumber = Integer.parseInt(lastInvoice.substring(3));
            
            // ========================================
            // SAFETY CHECK: Near limit?
            // ========================================
            if (lastNumber >= 99990) {
                // EMERGENCY: Switch to year-based format
                int year = java.time.Year.now().getValue();
                return String.format("%d-INV-00001", year);
            }
            
            if (lastNumber >= 99000) {
                // WARNING: Approaching limit
                String warning = String.format(
                    "⚠️ Invoice numbers at %d / 99999\n" +
                    "Will auto-switch to year format at 99990",
                    lastNumber
                );
                
                javax.swing.JOptionPane.showMessageDialog(
                    null,
                    warning,
                    "Invoice Number Warning",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );
            }
            // ========================================
            
            return String.format("INV%05d", lastNumber + 1);
        } catch (NumberFormatException e) {
            // Fallback: timestamp-based
            return "INV" + System.currentTimeMillis();
        }
    }
    
    // Check if last was year-based format (2025-INV-00001)
    if (lastInvoice != null && lastInvoice.contains("-INV-")) {
        String[] parts = lastInvoice.split("-");
        if (parts.length == 3) {
            int year = java.time.Year.now().getValue();
            int lastYear = Integer.parseInt(parts[0]);
            int lastSeq = Integer.parseInt(parts[2]);
            
            if (lastYear == year) {
                // Same year: increment
                return String.format("%d-INV-%05d", year, lastSeq + 1);
            } else {
                // New year: reset
                return String.format("%d-INV-00001", year);
            }
        }
    }
    
    return "INV00001";
}
}