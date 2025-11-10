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
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Component;

/**
 *
 * @author moham
 */
public class HoldDialog extends javax.swing.JDialog implements HoldPanel.HoldPanelListener {

    private List<Invoice> recentInvoices;
    private List<HoldPanel> invoicePanels;
    private int currentPanelIndex = -1;
    private Invoice selectedInvoice;
    private boolean invoiceSelected = false;

    /**
     * Creates new form HoldDialog
     */
    public HoldDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);
        setDialogSize(); // Set the dialog size
        initializeDialog();
        setupKeyboardNavigation();
    }

    // Set the dialog size to 580px width
    private void setDialogSize() {
        this.setSize(600, 300);
        this.setPreferredSize(new java.awt.Dimension(600, 300));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(600, 300));
    }

    // Add this method to get the selected invoice
    public Invoice getSelectedInvoice() {
        return selectedInvoice;
    }

    // Add this method to check if an invoice was selected
    public boolean isInvoiceSelected() {
        return invoiceSelected;
    }

    private void initializeDialog() {
        // Show loading message
        showLoadingMessage();

        // Load invoices in a separate thread to prevent UI freezing
        new Thread(() -> {
            loadRecentInvoices();
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

    private void loadRecentInvoices() {
        recentInvoices = new ArrayList<>();

        try {
            // Query to get invoices from the last 24 hours WITHOUT customer name
            String query
                    = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, "
                    + "s.status_id, st.status_type, "
                    + "u.user_id "
                    + "FROM sales s "
                    + "INNER JOIN i_status st ON s.status_id = st.status_id "
                    + "INNER JOIN user u ON s.user_id = u.user_id "
                    + "WHERE s.datetime >= DATE_SUB(NOW(), INTERVAL 24 HOUR) "
                    + "ORDER BY s.datetime DESC";

            ResultSet rs = MySQL.executeSearch(query);

            while (rs.next()) {
                Invoice invoice = new Invoice(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        null, // No customer name
                        "Cash", // Default payment method since it's removed from query
                        rs.getInt("user_id")
                );
                recentInvoices.add(invoice);
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
        invoicePanels = new ArrayList<>();

        if (recentInvoices.isEmpty()) {
            showNoInvoicesMessage();
        } else {
            showInvoicesList();
        }

        updateTitleWithCount();
        invoceLoadPenal.revalidate();
        invoceLoadPenal.repaint();

        // Set focus to first panel if available
        if (!invoicePanels.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                currentPanelIndex = 0;
                setPanelFocus(currentPanelIndex);
                requestFocus(); // Request focus for the dialog
            });
        }
    }

    private void showNoInvoicesMessage() {
        javax.swing.JPanel messagePanel = new javax.swing.JPanel();
        messagePanel.setBackground(new java.awt.Color(255, 255, 255));
        messagePanel.setLayout(new java.awt.BorderLayout());

        javax.swing.JLabel noDataLabel = new javax.swing.JLabel("No invoices found in the last 24 hours", javax.swing.JLabel.CENTER);
        noDataLabel.setFont(new java.awt.Font("Nunito", 1, 14));
        noDataLabel.setForeground(new java.awt.Color(128, 128, 128));
        noDataLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 20, 10));

        messagePanel.add(noDataLabel, java.awt.BorderLayout.CENTER);
        invoceLoadPenal.add(messagePanel);
    }

    private void showInvoicesList() {
        for (Invoice invoice : recentInvoices) {
            HoldPanel panel = new HoldPanel(invoice, this);
            panel.setMaximumSize(new java.awt.Dimension(580, 65));
            panel.setPreferredSize(new java.awt.Dimension(580, 65));
            panel.setMinimumSize(new java.awt.Dimension(580, 65));

            // Add mouse listener for click selection
            panel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    // Find which panel was clicked
                    for (int i = 0; i < invoicePanels.size(); i++) {
                        if (invoicePanels.get(i) == panel) {
                            currentPanelIndex = i;
                            setPanelFocus(currentPanelIndex);
                            selectCurrentPanel();
                            break;
                        }
                    }
                }
            });

            invoceLoadPenal.add(panel);
            invoicePanels.add(panel);

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
            jLabel3.setText("Recent Invoices (Last 24 Hours) - " + recentInvoices.size() + " found");
        });
    }

    private void setupKeyboardNavigation() {
        // Add key listener to the dialog
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        // Add key listener to the scroll pane as well
        jScrollPane1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        // Add key listener to the main panel
        invoceLoadPenal.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        // Make all components focusable
        this.setFocusable(true);
        jScrollPane1.setFocusable(true);
        invoceLoadPenal.setFocusable(true);

        // Request focus for the dialog
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                requestFocus();
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        if (invoicePanels == null || invoicePanels.isEmpty()) {
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                moveSelection(1);
                e.consume(); // Prevent default behavior
                break;
            case KeyEvent.VK_UP:
                moveSelection(-1);
                e.consume(); // Prevent default behavior
                break;
            case KeyEvent.VK_ENTER:
                if (currentPanelIndex >= 0 && currentPanelIndex < invoicePanels.size()) {
                    selectCurrentPanel();
                    e.consume(); // Prevent default behavior
                }
                break;
            case KeyEvent.VK_ESCAPE:
                invoiceSelected = false;
                dispose();
                e.consume(); // Prevent default behavior
                break;
        }
    }

    private void moveSelection(int direction) {
        if (invoicePanels.isEmpty()) {
            return;
        }

        // Clear previous focus
        if (currentPanelIndex >= 0 && currentPanelIndex < invoicePanels.size()) {
            clearPanelFocus(currentPanelIndex);
        }

        // Calculate new index
        currentPanelIndex += direction;

        // Wrap around
        if (currentPanelIndex < 0) {
            currentPanelIndex = invoicePanels.size() - 1;
        } else if (currentPanelIndex >= invoicePanels.size()) {
            currentPanelIndex = 0;
        }

        // Set new focus
        setPanelFocus(currentPanelIndex);
    }

    private void setPanelFocus(int index) {
        if (index >= 0 && index < invoicePanels.size()) {
            HoldPanel panel = invoicePanels.get(index);
            panel.setBorder(BorderFactory.createLineBorder(new Color(8, 147, 176), 2));

            // Set panel focused state to show button hover effect
            panel.setPanelFocused(true);

            // Ensure the panel is visible in scroll pane
            panel.scrollRectToVisible(panel.getBounds());

            // Request focus for the dialog to continue receiving key events
            requestFocus();
        }
    }

    private void clearPanelFocus(int index) {
        if (index >= 0 && index < invoicePanels.size()) {
            HoldPanel panel = invoicePanels.get(index);
            panel.setBorder(BorderFactory.createEmptyBorder());

            // Remove panel focused state to hide button hover effect
            panel.setPanelFocused(false);
        }
    }

    private void selectCurrentPanel() {
        if (currentPanelIndex >= 0 && currentPanelIndex < invoicePanels.size()) {
            HoldPanel panel = invoicePanels.get(currentPanelIndex);
            // Get the button text to determine the action
            String action = panel.getControleButton().getText();

            // Call the listener method directly with the panel's invoice
            onInvoiceSelected(panel.getInvoice(), action);
        }
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
                + "Date: " + invoice.getDate(),
                "Switch Invoice",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            this.selectedInvoice = invoice;
            this.invoiceSelected = true;
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

    // Override requestFocus to ensure dialog gets focus
    @Override
    public void requestFocus() {
        super.requestFocus();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        invoceLoadPenal = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        invoceLoadPenal.setBackground(new java.awt.Color(255, 255, 255));
        invoceLoadPenal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                invoceLoadPenalKeyPressed(evt);
            }
        });

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
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(107, Short.MAX_VALUE))
        );
        invoceLoadPenalLayout.setVerticalGroup(
            invoceLoadPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invoceLoadPenalLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(234, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(invoceLoadPenal);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        SwingUtilities.invokeLater(() -> {
            requestFocus();
        });
    }//GEN-LAST:event_formWindowOpened

    private void invoceLoadPenalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_invoceLoadPenalKeyPressed
        handleKeyPress(evt);
    }//GEN-LAST:event_invoceLoadPenalKeyPressed

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
