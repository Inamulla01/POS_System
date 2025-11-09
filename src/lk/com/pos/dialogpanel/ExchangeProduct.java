package lk.com.pos.dialogpanel;

import java.awt.Color;
import java.awt.Cursor;
import javax.swing.BorderFactory;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author moham
 */
public class ExchangeProduct extends javax.swing.JPanel {

    private int saleItemId;
    private int stockId;
    private int originalQuantity;
    private double unitPrice;
    private double discountPerUnit;
    private double totalAmount;
    private String batchNo;

    /**
     * Creates new form ExchangeProduct
     */
    public ExchangeProduct() {
        initComponents();
        setupSpinner();
        setupCheckbox();
    }

    public void setProductDetails(String productName, double unitPrice, 
                                 double discountPrice, int quantity, 
                                 double total, String batchNo) {
        cartProductName1.setText(productName);
        this.unitPrice = unitPrice;
        this.discountPerUnit = discountPrice / quantity;
        this.originalQuantity = quantity;
        this.totalAmount = total;
        this.batchNo = batchNo;
        
        updateDisplay();
        setupSpinner(); // Re-setup spinner with actual quantity
    }

    public void setIds(int saleItemId, int stockId) {
        this.saleItemId = saleItemId;
        this.stockId = stockId;
    }

    private void setupSpinner() {
        jSpinner1.setModel(new SpinnerNumberModel(0, 0, originalQuantity, 1));
        jSpinner1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateReturnCalculation();
            }
        });
    }

    private void setupCheckbox() {
        jCheckBox1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (jCheckBox1.isSelected()) {
                    jSpinner1.setValue(originalQuantity);
                } else {
                    jSpinner1.setValue(0);
                }
                jSpinner1.setEnabled(!jCheckBox1.isSelected());
            }
        });
    }

    private void updateDisplay() {
        brandName.setText(String.format("Rs %.2f X %d = Rs %.2f", 
            unitPrice, originalQuantity, unitPrice * originalQuantity));
        
        if (discountPerUnit > 0) {
            brandName1.setText(String.format("Discount Rs %.2f X %d = Rs %.2f", 
                discountPerUnit, originalQuantity, discountPerUnit * originalQuantity));
            brandName1.setVisible(true);
        } else {
            brandName1.setVisible(false);
        }
        
        brandName2.setText("Quantity = " + originalQuantity);
        priceInput1.setText(String.format("Rs %.2f", totalAmount));
        brandName3.setText("Batch: " + batchNo);
        
        // Update spinner maximum
        if (jSpinner1.getModel() instanceof SpinnerNumberModel) {
            SpinnerNumberModel model = (SpinnerNumberModel) jSpinner1.getModel();
            model.setMaximum(originalQuantity);
        }
    }

    private void updateReturnCalculation() {
        int returnQty = getReturnQuantity();
        double returnTotal = returnQty * unitPrice;
        double returnDiscount = returnQty * discountPerUnit;
        double netReturn = returnTotal - returnDiscount;
        
        // Update display for return amounts
        if (returnQty > 0) {
            brandName1.setText(String.format("Return: %d items = Rs %.2f", returnQty, netReturn));
            brandName1.setForeground(Color.RED);
            brandName1.setVisible(true);
            
            // Highlight the panel
            setBorder(BorderFactory.createLineBorder(Color.decode("#0893B0"), 2));
        } else {
            // Reset to original display
            if (discountPerUnit > 0) {
                brandName1.setText(String.format("Discount Rs %.2f X %d = Rs %.2f", 
                    discountPerUnit, originalQuantity, discountPerUnit * originalQuantity));
                brandName1.setForeground(Color.RED);
            } else {
                brandName1.setVisible(false);
            }
            
            // Reset border
            setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        }
    }

    public int getReturnQuantity() {
        return (Integer) jSpinner1.getValue();
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getDiscountAmount() {
        return getReturnQuantity() * discountPerUnit;
    }

    public double getReturnTotal() {
        return (getReturnQuantity() * unitPrice) - getDiscountAmount();
    }

    public int getSaleItemId() {
        return saleItemId;
    }

    public int getStockId() {
        return stockId;
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cartProductName1 = new javax.swing.JLabel();
        priceInput1 = new javax.swing.JLabel();
        brandName = new javax.swing.JLabel();
        brandName1 = new javax.swing.JLabel();
        brandName2 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        brandName3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();

        cartProductName1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        cartProductName1.setText("Product Name (Brand)");

        priceInput1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        priceInput1.setForeground(new java.awt.Color(8, 147, 176));
        priceInput1.setText("Rs.570.00");

        brandName.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        brandName.setForeground(new java.awt.Color(102, 102, 102));
        brandName.setText("Rs 200.00 X 3 = 600");

        brandName1.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        brandName1.setForeground(new java.awt.Color(255, 0, 0));
        brandName1.setText("Discount Rs 10.00 X 3 = Rs 570.00");

        brandName2.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        brandName2.setForeground(new java.awt.Color(102, 102, 102));
        brandName2.setText("Quantity = 3");

        brandName3.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        brandName3.setForeground(new java.awt.Color(102, 102, 102));
        brandName3.setText("Exchang Quantity");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(brandName1)
                    .addComponent(brandName)
                    .addComponent(brandName2)
                    .addComponent(cartProductName1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(priceInput1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(brandName3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cartProductName1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(brandName2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(brandName, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(brandName3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(priceInput1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(brandName1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel brandName;
    private javax.swing.JLabel brandName1;
    private javax.swing.JLabel brandName2;
    private javax.swing.JLabel brandName3;
    private javax.swing.JLabel cartProductName1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JLabel priceInput1;
    // End of variables declaration//GEN-END:variables
}
