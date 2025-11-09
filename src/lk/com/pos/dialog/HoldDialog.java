package lk.com.pos.dialog;

import lk.com.pos.dialogpanel.HoldPanel;
import lk.com.pos.privateclasses.Invoice;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lk.com.pos.connection.MySQL;

/**
 *
 * @author moham
 */
public class HoldDialog extends javax.swing.JDialog implements HoldPanel.HoldPanelListener {

    private List<Invoice> todayInvoices;

    /**
     * Creates new form HoldDialog
     */
    public HoldDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);
        initializeDialog();
    }

    private void initializeDialog() {
        // Show loading message
        showLoadingMessage();

        // Load invoices in a separate thread to prevent UI freezing
        new Thread(() -> {
            loadTodayInvoices();
            SwingUtilities.invokeLater(() -> {
                populateInvoicePanels();
            });
        }).start();
    }

    private void showLoadingMessage() {
        invoceLoadPenal.removeAll();
        invoceLoadPenal.setLayout(new java.awt.BorderLayout());

        javax.swing.JLabel loadingLabel = new javax.swing.JLabel("Loading invoices...", javax.swing.JLabel.CENTER);
        loadingLabel.setFont(new java.awt.Font("Nunito", 1, 16));
        loadingLabel.setForeground(new java.awt.Color(128, 128, 128));
        invoceLoadPenal.add(loadingLabel, java.awt.BorderLayout.CENTER);

        invoceLoadPenal.revalidate();
        invoceLoadPenal.repaint();
    }

    private void loadTodayInvoices() {
        todayInvoices = new ArrayList<>();

        try {
            // Query to get today's invoices with customer and payment method information
            String query
                    = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, "
                    + "s.status_id, st.status_type, "
                    + "pm.payment_method_name, "
                    + "cc.customer_name, "
                    + "u.user_id "
                    + "FROM sales s "
                    + "INNER JOIN i_status st ON s.status_id = st.status_id "
                    + "INNER JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                    + "LEFT JOIN credit_customer cc ON s.credit_customer_id = cc.customer_id "
                    + "INNER JOIN user u ON s.user_id = u.user_id "
                    + "WHERE DATE(s.datetime) = CURDATE() "
                    + "ORDER BY s.datetime DESC";

            ResultSet rs = MySQL.executeSearch(query);

            while (rs.next()) {
                Invoice invoice = new Invoice(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        rs.getString("customer_name"),
                        rs.getString("payment_method_name"),
                        rs.getInt("user_id")
                );
                todayInvoices.add(invoice);
            }

        } catch (SQLException e) {
            System.err.println("Error loading invoices: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error loading invoices: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void populateInvoicePanels() {
        invoceLoadPenal.removeAll();
        invoceLoadPenal.setLayout(new BoxLayout(invoceLoadPenal, BoxLayout.Y_AXIS));

        if (todayInvoices.isEmpty()) {
            showNoInvoicesMessage();
        } else {
            showInvoicesList();
        }

        updateTitleWithCount();
        invoceLoadPenal.revalidate();
        invoceLoadPenal.repaint();
    }

    private void showNoInvoicesMessage() {
        javax.swing.JPanel messagePanel = new javax.swing.JPanel();
        messagePanel.setBackground(new java.awt.Color(255, 255, 255));
        messagePanel.setLayout(new java.awt.BorderLayout());

        javax.swing.JLabel noDataLabel = new javax.swing.JLabel("No invoices found for today", javax.swing.JLabel.CENTER);
        noDataLabel.setFont(new java.awt.Font("Nunito", 1, 14));
        noDataLabel.setForeground(new java.awt.Color(128, 128, 128));
        noDataLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 20, 10));

        messagePanel.add(noDataLabel, java.awt.BorderLayout.CENTER);
        invoceLoadPenal.add(messagePanel);
    }

    private void showInvoicesList() {
        for (Invoice invoice : todayInvoices) {
            HoldPanel panel = new HoldPanel(invoice, this);
            panel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 65));
            invoceLoadPenal.add(panel);

            // Add spacing between panels
            invoceLoadPenal.add(createSpacer());
        }
    }

    private javax.swing.JPanel createSpacer() {
        javax.swing.JPanel spacer = new javax.swing.JPanel();
        spacer.setBackground(new java.awt.Color(255, 255, 255));
        spacer.setPreferredSize(new java.awt.Dimension(10, 5));
        spacer.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 5));
        return spacer;
    }

    private void updateTitleWithCount() {
        SwingUtilities.invokeLater(() -> {
            jLabel3.setText("Today's Invoices (" + todayInvoices.size() + ")");
        });
    }

    @Override
    public void onInvoiceSelected(Invoice invoice, String action) {
        switch (action) {
            case "Switch":
                handleSwitchAction(invoice);
                break;
            case "View":
                handleViewAction(invoice);
                break;
            case "Open":
                handleOpenAction(invoice);
                break;
            default:
                handleDefaultAction(invoice, action);
                break;
        }
    }

    private void handleSwitchAction(Invoice invoice) {
        int response = JOptionPane.showConfirmDialog(this,
                "Switch to invoice: " + invoice.getInvoiceNo() + "?\n"
                + "Amount: Rs. " + String.format("%.2f", invoice.getTotal()) + "\n"
                + "Customer: " + (invoice.getCustomerName() != null ? invoice.getCustomerName() : "Walk-in") + "\n"
                + "Payment: " + invoice.getPaymentMethod(),
                "Switch Invoice",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            // Implement switch logic here - you can pass the invoice back to main form
            JOptionPane.showMessageDialog(this,
                    "Switched to invoice: " + invoice.getInvoiceNo(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Here you can return the selected invoice to the main form
            // For example: mainForm.switchToInvoice(invoice);
            this.dispose();
        }
    }

    private void handleViewAction(Invoice invoice) {
        // Get detailed invoice information
        try {
            String detailQuery
                    = "SELECT si.qty, p.product_name, si.price, si.discount_price, si.total "
                    + "FROM sale_item si "
                    + "INNER JOIN stock st ON si.stock_id = st.stock_id "
                    + "INNER JOIN product p ON st.product_id = p.product_id "
                    + "WHERE si.sales_id = " + invoice.getSalesId();

            ResultSet rs = MySQL.executeSearch(detailQuery);

            StringBuilder details = new StringBuilder();
            details.append("Invoice Details:\n");
            details.append("ID: ").append(invoice.getInvoiceNo()).append("\n");
            details.append("Status: ").append(invoice.getStatus()).append("\n");
            details.append("Amount: Rs. ").append(String.format("%.2f", invoice.getTotal())).append("\n");
            details.append("Customer: ").append(invoice.getCustomerName() != null ? invoice.getCustomerName() : "Walk-in").append("\n");
            details.append("Payment: ").append(invoice.getPaymentMethod()).append("\n");
            details.append("Date: ").append(invoice.getDate()).append("\n\n");
            details.append("Items:\n");

            double itemTotal = 0;
            while (rs.next()) {
                String productName = rs.getString("product_name");
                int qty = rs.getInt("qty");
                double price = rs.getDouble("price");
                double discount = rs.getDouble("discount_price");
                double total = rs.getDouble("total");

                details.append(String.format("- %s x%d: Rs. %.2f (Discount: Rs. %.2f) = Rs. %.2f\n",
                        productName, qty, price, discount, total));
                itemTotal += total;
            }

            details.append("\nTotal: Rs. ").append(String.format("%.2f", itemTotal));

            JOptionPane.showMessageDialog(this,
                    details.toString(),
                    "View Invoice - " + invoice.getInvoiceNo(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading invoice details: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleOpenAction(Invoice invoice) {
        JOptionPane.showMessageDialog(this,
                "Opening invoice: " + invoice.getInvoiceNo() + "\n"
                + "This invoice is in " + invoice.getStatus() + " status.",
                "Open Invoice",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDefaultAction(Invoice invoice, String action) {
        JOptionPane.showMessageDialog(this,
                action + " action for invoice: " + invoice.getInvoiceNo(),
                action + " Invoice",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to refresh the dialog
    public void refreshInvoices() {
        initializeDialog();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        invoceLoadPenal = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        invoceLoadPenal.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Exchange Products");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        javax.swing.GroupLayout invoceLoadPenalLayout = new javax.swing.GroupLayout(invoceLoadPenal);
        invoceLoadPenal.setLayout(invoceLoadPenalLayout);
        invoceLoadPenalLayout.setHorizontalGroup(
            invoceLoadPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invoceLoadPenalLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(invoceLoadPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        invoceLoadPenalLayout.setVerticalGroup(
            invoceLoadPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invoceLoadPenalLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(invoceLoadPenal);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(HoldDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HoldDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HoldDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HoldDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                HoldDialog dialog = new HoldDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JPanel invoceLoadPenal;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables
}
