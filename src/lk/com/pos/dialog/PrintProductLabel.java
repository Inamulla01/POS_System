package lk.com.pos.dialog;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import lk.com.pos.privateclasses.GradientButton;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

public class PrintProductLabel extends javax.swing.JDialog {

    private String barcode;

    public PrintProductLabel(JFrame parent, boolean modal, String barcode) {
        super(parent, modal);
        this.barcode = barcode;
        initComponents();
        initializeDialog();
    }

    private void initializeDialog() {
        setTitle("Print Barcode Label");
        setLocationRelativeTo(getParent());
        barcodetext.setText(barcode);
//        customizePrintButton();
        printQty.setText("1"); // Default quantity
    }

//    private void customizePrintButton() {
//        // Remove the button recreation and just customize the existing button
//        printBtn.setContentAreaFilled(false);
//        printBtn.setFocusPainted(false);
//        printBtn.setBorderPainted(false);
//        printBtn.setOpaque(true);
//        printBtn.setForeground(Color.WHITE);
//        printBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
//        printBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
//    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        barcodetext = new javax.swing.JTextField();
        printBtn = new javax.swing.JButton();
        printQty = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        barcodetext.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        printBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        printBtn.setText("Print");
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printBtnActionPerformed(evt);
            }
        });

        printQty.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(barcodetext)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(printQty, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(barcodetext, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(printQty, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                .addGap(13, 13, 13))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void printBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBtnActionPerformed
        // TODO add your handling code here:
        String qtyText = printQty.getText().trim();
        String barcodeNo = barcodetext.getText().trim();
        
        System.out.println(qtyText);
        System.out.println(barcodeNo);

        if (barcodeNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Barcode is empty!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter quantity.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (quantity > 100) {
                JOptionPane.showMessageDialog(this, "Maximum 100 barcodes can be printed at once.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Print barcodes
        printBarcodes(barcodeNo, quantity);
    }//GEN-LAST:event_printBtnActionPerformed

//    private void printBarcodes(String barcode, int quantity) {
//        try {
//            InputStream jasperStream = getClass().getResourceAsStream("/lk/com/pos/reports/barcode.jasper");
//
//            if (jasperStream == null) {
//                JOptionPane.showMessageDialog(this, 
//                    "Barcode template not found!\nPlease check if barcode.jasper exists in the reports folder.", 
//                    "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            // Create parameters
//            Map<String, Object> parameters = new HashMap<>();
//            parameters.put("REPORT_TITLE", "Product Barcode");
//            parameters.put("COMPANY_NAME", "Your Store Name");
//
//            // Create data source with multiple copies
//            List<BarcodeItem> barcodeList = new ArrayList<>();
//            for (int i = 0; i < quantity; i++) {
//                barcodeList.add(new BarcodeItem(barcode));
//            }
//
//            JRDataSource dataSource = new JRBeanCollectionDataSource(barcodeList);
//
//            // Fill and display report
//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, parameters, dataSource);
//            
//            // Show preview
//            JasperViewer viewer = new JasperViewer(jasperPrint, false);
//            viewer.setTitle("Barcode Preview - " + barcode);
//            viewer.setVisible(true);
//    
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, 
//                "Error generating barcode: " + e.getMessage(), 
//                "Print Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
    private void printBarcodes(String barcode, int quantity) {
        try {
            InputStream jasperStream = getClass().getResourceAsStream("/lk/com/pos/reports/barcode.jasper");

            if (jasperStream == null) {
                JOptionPane.showMessageDialog(this,
                        "Barcode template not found!\nPlease check if barcode.jasper exists in the reports folder.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Product Barcode");
            parameters.put("COMPANY_NAME", "Your Store Name");

            // Create data source with multiple copies
            List<BarcodeItem> barcodeList = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                barcodeList.add(new BarcodeItem(barcode));
            }

            JRDataSource dataSource = new JRBeanCollectionDataSource(barcodeList);

            // Fill and display report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, parameters, dataSource);

            // Close this dialog first
            dispose();

            // Show preview
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Barcode Preview - " + barcode);
            viewer.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error generating barcode: " + e.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper class for JasperReports data source
    public static class BarcodeItem {

        private String barcode;

        public BarcodeItem(String barcode) {
            this.barcode = barcode;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PrintProductLabel dialog = new PrintProductLabel(new javax.swing.JFrame(), true, "123456789012");
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
    private javax.swing.JTextField barcodetext;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton printBtn;
    private javax.swing.JTextField printQty;
    // End of variables declaration//GEN-END:variables
}
