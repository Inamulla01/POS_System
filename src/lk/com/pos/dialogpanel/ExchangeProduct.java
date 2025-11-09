package lk.com.pos.dialogpanel;

import javax.swing.*;

public class ExchangeProduct extends javax.swing.JPanel {

    public ExchangeProduct() {
        initComponents();
    }

    // Getter methods for all components
    public JLabel getProductName() {
        return productName;
    }

    public JLabel getProductPrice() {
        return productPrice;
    }

    public JLabel getQtyCount() {
        return qtyCount;
    }

    public JLabel getDiscountItem() {
        return discountItem;
    }

    public JLabel getQty() {
        return qty;
    }

    public JTextField getExchangeQtyField() {
        return newQty;
    }

    public JCheckBox getCheckBox() {
        return jCheckBox2;
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        productPenal = new javax.swing.JPanel();
        productName = new javax.swing.JLabel();
        productPrice = new javax.swing.JLabel();
        qtyCount = new javax.swing.JLabel();
        discountItem = new javax.swing.JLabel();
        qty = new javax.swing.JLabel();
        brandName4 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        newQty = new javax.swing.JTextField();

        productPenal.setBackground(new java.awt.Color(248, 250, 252));

        productName.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        productName.setText("Product Name (Brand)");

        productPrice.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        productPrice.setForeground(new java.awt.Color(8, 147, 176));
        productPrice.setText("Rs.570.00");

        qtyCount.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        qtyCount.setForeground(new java.awt.Color(102, 102, 102));
        qtyCount.setText("Rs 200.00 X 3 = 600");

        discountItem.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        discountItem.setForeground(new java.awt.Color(255, 0, 0));
        discountItem.setText("Discount Rs 10.00 X 3 = Rs 570.00");

        qty.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        qty.setForeground(new java.awt.Color(102, 102, 102));
        qty.setText("Quantity = 3");

        brandName4.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        brandName4.setForeground(new java.awt.Color(102, 102, 102));
        brandName4.setText("Exchang Quantity");

        javax.swing.GroupLayout productPenalLayout = new javax.swing.GroupLayout(productPenal);
        productPenal.setLayout(productPenalLayout);
        productPenalLayout.setHorizontalGroup(
            productPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productPenalLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(productPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productPenalLayout.createSequentialGroup()
                        .addGroup(productPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(discountItem)
                            .addComponent(qtyCount)
                            .addComponent(qty))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 209, Short.MAX_VALUE)
                        .addGroup(productPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(productPrice, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(brandName4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(newQty, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(12, 12, 12))
                    .addGroup(productPenalLayout.createSequentialGroup()
                        .addComponent(productName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox2)
                        .addContainerGap())))
        );
        productPenalLayout.setVerticalGroup(
            productPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productPenalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(productPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productPenalLayout.createSequentialGroup()
                        .addComponent(jCheckBox2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(productPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(productPenalLayout.createSequentialGroup()
                                .addComponent(brandName4, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(newQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(productPenalLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(qty, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(qtyCount, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(productName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(productPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(productPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discountItem, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(productPenal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(productPenal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel brandName4;
    private javax.swing.JLabel discountItem;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JTextField newQty;
    private javax.swing.JLabel productName;
    private javax.swing.JPanel productPenal;
    private javax.swing.JLabel productPrice;
    private javax.swing.JLabel qty;
    private javax.swing.JLabel qtyCount;
    // End of variables declaration//GEN-END:variables
}
