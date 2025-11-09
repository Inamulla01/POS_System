package lk.com.pos.dialogpanel;

import lk.com.pos.privateclasses.Invoice;
import java.awt.Color;
import javax.swing.JOptionPane;

/**
 *
 * @author moham
 */
public class HoldPanel extends javax.swing.JPanel {

    private Invoice invoice;
    private HoldPanelListener listener;
    
    /**
     * Creates new form holdPanel
     */
    public HoldPanel() {
        initComponents();
    }
    
    public HoldPanel(Invoice invoice, HoldPanelListener listener) {
        initComponents();
        this.invoice = invoice;
        this.listener = listener;
        setInvoiceData();
        setupButtonAction();
    }
    
    private void setInvoiceData() {
        if (invoice != null) {
            String status = invoice.getStatus();
            String customerInfo = invoice.getCustomerName() != null ? 
                " - " + invoice.getCustomerName() : "";
            
            invoiceAndCondition.setText(invoice.getInvoiceNo() + " (" + status + ")" + customerInfo);
            
            // Change button text and color based on status
            if ("Completed".equalsIgnoreCase(status)) {
                controle.setText("View");
                controle.setBackground(new Color(76, 175, 80)); // Green
            } else if ("Hold".equalsIgnoreCase(status)) {
                controle.setText("Switch");
                controle.setBackground(new Color(255, 152, 0)); // Orange
            } else {
                controle.setText("Open");
                controle.setBackground(new Color(158, 158, 158)); // Gray
            }
            
            // Set tooltip with more information
            String tooltip = String.format(
                "Invoice: %s\nAmount: Rs. %.2f\nPayment: %s\nDate: %s",
                invoice.getInvoiceNo(),
                invoice.getTotal(),
                invoice.getPaymentMethod(),
                invoice.getDate()
            );
            this.setToolTipText(tooltip);
        }
    }
    
    private void setupButtonAction() {
        controle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (listener != null && invoice != null) {
                    String action = controle.getText();
                    listener.onInvoiceSelected(invoice, action);
                }
            }
        });
    }
    
    public interface HoldPanelListener {
        void onInvoiceSelected(Invoice invoice, String action);
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        invoicePanel = new javax.swing.JPanel();
        controle = new javax.swing.JButton();
        invoiceAndCondition = new javax.swing.JLabel();

        controle.setFont(new java.awt.Font("Nunito SemiBold", 1, 15)); // NOI18N
        controle.setText("Switch");

        invoiceAndCondition.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        invoiceAndCondition.setText("#INV003212 (Hold)");

        javax.swing.GroupLayout invoicePanelLayout = new javax.swing.GroupLayout(invoicePanel);
        invoicePanel.setLayout(invoicePanelLayout);
        invoicePanelLayout.setHorizontalGroup(
            invoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, invoicePanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(invoiceAndCondition)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addComponent(controle, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );
        invoicePanelLayout.setVerticalGroup(
            invoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invoicePanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(invoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(controle, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(invoiceAndCondition))
                .addGap(9, 9, 9))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invoicePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invoicePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton controle;
    private javax.swing.JLabel invoiceAndCondition;
    private javax.swing.JPanel invoicePanel;
    // End of variables declaration//GEN-END:variables
}
