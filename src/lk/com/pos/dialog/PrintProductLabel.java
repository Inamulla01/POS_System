package lk.com.pos.dialog;

import com.formdev.flatlaf.FlatLightLaf;


public class PrintProductLabel extends javax.swing.JDialog {
    
    private String barcode; // Add this field to store barcode
    private String prdName;
    private double sellPrice;

    public PrintProductLabel(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
   
    public PrintProductLabel(java.awt.Frame parent, boolean modal, String barcode, String productName, double sellingPrice) {
        super(parent, modal);
        this.barcode = barcode; // Store the barcode
        initComponents();
        barcodetext.setText(barcode);
        prdNameText.setText(productName);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        barcodetext = new javax.swing.JTextField();
        printBtn = new javax.swing.JButton();
        printQty = new javax.swing.JTextField();
        businessName = new javax.swing.JCheckBox();
        productName = new javax.swing.JCheckBox();
        productPrice = new javax.swing.JCheckBox();
        printType = new javax.swing.JComboBox<>();
        prdNameText = new javax.swing.JTextField();

        jToggleButton1.setText("jToggleButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        barcodetext.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        printBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        printBtn.setText("Print");

        printQty.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        businessName.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        businessName.setForeground(new java.awt.Color(0, 0, 0));
        businessName.setText("Business Name");

        productName.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        productName.setForeground(new java.awt.Color(0, 0, 0));
        productName.setText("Product Name");

        productPrice.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        productPrice.setForeground(new java.awt.Color(0, 0, 0));
        productPrice.setText("Product Price");

        prdNameText.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(printType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(printQty, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(barcodetext)
                    .addComponent(prdNameText))
                .addContainerGap(22, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(businessName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productName)
                .addGap(18, 18, 18)
                .addComponent(productPrice)
                .addGap(34, 34, 34))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(prdNameText, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(barcodetext, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(businessName)
                    .addComponent(productName)
                    .addComponent(productPrice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(printType, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(printQty)
                    .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatLightLaf.setup();

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PrintProductLabel dialog = new PrintProductLabel(new javax.swing.JFrame(), true);
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
    private javax.swing.JCheckBox businessName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTextField prdNameText;
    private javax.swing.JButton printBtn;
    private javax.swing.JTextField printQty;
    private javax.swing.JComboBox<String> printType;
    private javax.swing.JCheckBox productName;
    private javax.swing.JCheckBox productPrice;
    // End of variables declaration//GEN-END:variables
}
