package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import lk.com.pos.session.Session;
import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 *
 * @author moham
 */
public class AddNewLossStock extends javax.swing.JDialog {

    /**
     * Creates new form AddNewLossStock
     */
    public AddNewLossStock(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        loadStocks();
        loadLossReasons();
    }

    private void loadStocks() {
        try {
            String query = "SELECT s.stock_id, s.batch_no, p.product_name, s.qty "
                    + "FROM stock s "
                    + "JOIN product p ON s.product_id = p.product_id "
                    + "WHERE s.qty > 0 AND s.p_status_id = 1 "
                    + "ORDER BY s.batch_no, p.product_name";

            ResultSet rs = MySQL.executeSearch(query);
            Vector<String> stocks = new Vector<>();
            stocks.add("Select Stock");

            while (rs.next()) {
                String batchNo = rs.getString("batch_no");
                String productName = rs.getString("product_name");
                int qty = rs.getInt("qty");
                stocks.add(batchNo + " - " + productName + " (Available: " + qty + ")");
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(stocks);
            stockCombo.setModel(dcm);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading stocks: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLossReasons() {
        try {
            String query = "SELECT return_reason_id, reason FROM return_reason";
            ResultSet rs = MySQL.executeSearch(query);
            Vector<String> reasons = new Vector<>();
            reasons.add("Select Reason");

            while (rs.next()) {
                String reason = rs.getString("reason").toLowerCase();
                if (reason.contains("expired") || reason.contains("damaged")
                        || reason.contains("defective") || reason.contains("malfunction")
                        || reason.contains("other")) {
                    reasons.add(rs.getString("reason"));
                }
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(reasons);
            reasonCombo.setModel(dcm);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading reasons: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveStockLoss() {
        // Validation
        if (stockCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a stock",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (reasonCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a reason",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (qty.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter quantity",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int qtyValue = Integer.parseInt(qty.getText().trim());
            if (qtyValue <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get selected stock details
            String selectedStock = (String) stockCombo.getSelectedItem();
            int stockId = extractStockId(selectedStock);
            int availableQty = extractAvailableQty(selectedStock);

            if (qtyValue > availableQty) {
                JOptionPane.showMessageDialog(this,
                        "Quantity cannot exceed available stock. Available: " + availableQty,
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get reason ID
            String selectedReason = (String) reasonCombo.getSelectedItem();
            int reasonId = getReasonId(selectedReason);

            if (reasonId == -1) {
                JOptionPane.showMessageDialog(this, "Invalid reason selected",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get user from session and validate user exists
            Session session = Session.getInstance();
            int userId = session.getUserId();

            // Validate user exists in database
            if (!isValidUser(userId)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid user session. Please log in again.",
                        "Session Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Start transaction
            MySQL.getConnection().setAutoCommit(false);

            try {
                // Insert into stock_loss table
                String insertLossQuery = "INSERT INTO stock_loss (qty, stock_id, stock_loss_date, return_reason_id, user_id) "
                        + "VALUES (" + qtyValue + ", " + stockId + ", NOW(), " + reasonId + ", " + userId + ")";
                MySQL.executeIUD(insertLossQuery);

                // Update stock table - decrease quantity
                String updateStockQuery = "UPDATE stock SET qty = qty - " + qtyValue + " WHERE stock_id = " + stockId;
                MySQL.executeIUD(updateStockQuery);

                // Commit transaction
                MySQL.getConnection().commit();

                JOptionPane.showMessageDialog(this, "Stock loss added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                // Reset form
                resetForm();

            } catch (SQLException ex) {
                // Rollback transaction in case of error
                try {
                    MySQL.getConnection().rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }

                // Check if it's a foreign key constraint violation
                if (ex.getMessage().contains("foreign key constraint") && ex.getMessage().contains("user_id")) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid user account. Please contact administrator.",
                            "User Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    throw ex; // Re-throw other SQL exceptions
                }
            } finally {
                try {
                    MySQL.getConnection().setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    autoCommitEx.printStackTrace();
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding stock loss: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

// Add this method to validate if user exists in database
    private boolean isValidUser(int userId) {
        try {
            String query = "SELECT user_id FROM user WHERE user_id = " + userId;
            ResultSet rs = MySQL.executeSearch(query);
            return rs.next(); // Returns true if user exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int extractStockId(String stockText) throws SQLException {
        // Extract stock ID from the combo box text
        // Format: "batchNo - productName (Available: qty)"
        String batchNo = stockText.split(" - ")[0];

        String query = "SELECT stock_id FROM stock WHERE batch_no = '" + batchNo + "'";
        ResultSet rs = MySQL.executeSearch(query);

        if (rs.next()) {
            return rs.getInt("stock_id");
        }

        throw new SQLException("Stock not found");
    }

    private int extractAvailableQty(String stockText) {
        // Extract available quantity from the combo box text
        // Format: "batchNo - productName (Available: qty)"
        String qtyPart = stockText.split("Available: ")[1];
        qtyPart = qtyPart.replace(")", "");
        return Integer.parseInt(qtyPart.trim());
    }

    private int getReasonId(String reason) throws SQLException {
        String query = "SELECT return_reason_id FROM return_reason WHERE reason = '" + reason + "'";
        ResultSet rs = MySQL.executeSearch(query);

        if (rs.next()) {
            return rs.getInt("return_reason_id");
        }

        return -1;
    }

    private void resetForm() {
        stockCombo.setSelectedIndex(0);
        reasonCombo.setSelectedIndex(0);
        qty.setText("");
        loadStocks(); // Reload stocks to reflect updated quantities
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        qty = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        refreshBtn = new javax.swing.JButton();
        stockCombo = new javax.swing.JComboBox<>();
        reasonCombo = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        qty.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        qty.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quantity *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        qty.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                qtyKeyPressed(evt);
            }
        });

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setForeground(new java.awt.Color(8, 147, 176));
        cancelBtn.setText("Cancel");
        cancelBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        saveBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        saveBtn.setForeground(new java.awt.Color(8, 147, 176));
        saveBtn.setText("Save");
        saveBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(8, 147, 176));
        jLabel2.setText("Add Stock Loss");

        jSeparator2.setForeground(new java.awt.Color(0, 137, 176));

        refreshBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        refreshBtn.setForeground(new java.awt.Color(8, 147, 176));
        refreshBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        refreshBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBtnActionPerformed(evt);
            }
        });
        refreshBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                refreshBtnKeyPressed(evt);
            }
        });

        stockCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        stockCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stockCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Stock *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        stockCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stockComboActionPerformed(evt);
            }
        });
        stockCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                stockComboKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                stockComboKeyReleased(evt);
            }
        });

        reasonCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        reasonCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        reasonCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Reason *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        reasonCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reasonComboActionPerformed(evt);
            }
        });
        reasonCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                reasonComboKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                reasonComboKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addContainerGap(264, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stockCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(2, 2, 2))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addGap(390, 390, 390)
                                    .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(93, 93, 93)
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(qty, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reasonCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 23, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(stockCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(qty, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reasonCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        saveStockLoss();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
        loadStocks();
        loadLossReasons();
        JOptionPane.showMessageDialog(this, "Data refreshed successfully!",
                "Refresh", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_refreshBtnActionPerformed

    private void refreshBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_refreshBtnKeyPressed

    }//GEN-LAST:event_refreshBtnKeyPressed

    private void stockComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stockComboActionPerformed

    }//GEN-LAST:event_stockComboActionPerformed

    private void stockComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_stockComboKeyPressed

    }//GEN-LAST:event_stockComboKeyPressed

    private void stockComboKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_stockComboKeyReleased

    }//GEN-LAST:event_stockComboKeyReleased

    private void reasonComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reasonComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reasonComboActionPerformed

    private void reasonComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reasonComboKeyPressed

    }//GEN-LAST:event_reasonComboKeyPressed

    private void reasonComboKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reasonComboKeyReleased

    }//GEN-LAST:event_reasonComboKeyReleased

    private void qtyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_qtyKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            saveStockLoss();
        }
    }//GEN-LAST:event_qtyKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AddNewLossStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddNewLossStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddNewLossStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddNewLossStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddNewLossStock dialog = new AddNewLossStock(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField qty;
    private javax.swing.JComboBox<String> reasonCombo;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JButton saveBtn;
    private javax.swing.JComboBox<String> stockCombo;
    // End of variables declaration//GEN-END:variables
}
