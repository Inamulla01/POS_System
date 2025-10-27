/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import raven.toast.Notifications;

/**
 *
 * @author Hirun
 */
public class AddNewCustomer extends javax.swing.JDialog {

    /**
     * Creates new form AddNewCustomer
     */
    public AddNewCustomer(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);
        clearFields();
    }

    private void clearFields() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("0");
        jTextField5.setText("0");
        jTextField6.setText("");
        jDateChooser2.setDate(null);
        jDateChooser3.setDate(null);
    }

    private boolean isValidSriLankanMobile(String mobile) {
        // Remove any spaces or dashes
        String cleanedMobile = mobile.replaceAll("[\\s-]", "");
        
        // Use your exact regex pattern for Sri Lankan mobile validation
        if (!cleanedMobile.matches("^(0{1})(7{1})([0|1|2|4|5|6|7|8]{1})([0-9]{7})$")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter a valid Sri Lankan mobile number (10 digits starting with 07)");
            return false;
        }
        return true;
    }

    private boolean isValidNIC(String nic) {
        if (nic == null || nic.trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "NIC number is required!");
            return false;
        }
        
        // Remove spaces and convert to uppercase
        String cleanedNIC = nic.trim().toUpperCase();
        
        // Validate NIC format (old: 9 digits + V, new: 12 digits)
        if (!cleanedNIC.matches("^([0-9]{9}[VvXx]|[0-9]{12})$")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter a valid NIC number (9 digits with V/X or 12 digits)");
            return false;
        }
        return true;
    }

    private boolean isCustomerNameExists(String name) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT COUNT(*) FROM credit_customer WHERE customer_name = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Customer name already exists!");
                return true;
            }
            
            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isNICExists(String nic) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT COUNT(*) FROM credit_customer WHERE nic = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, nic.trim().toUpperCase());
            ResultSet rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "NIC number already exists!");
                return true;
            }
            
            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isValidAmount(String amount) {
        try {
            double amt = Double.parseDouble(amount);
            if (amt < 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Amount cannot be negative!");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter a valid amount!");
            return false;
        }
    }

    private void saveCustomer() {
        String name = jTextField1.getText().trim();
        String mobile = jTextField2.getText().trim();
        String address = jTextField3.getText().trim();
        String amountDue = jTextField4.getText().trim();
        String creditPrice = jTextField5.getText().trim();
        String nic = jTextField6.getText().trim();
        Date creditGivenDate = jDateChooser3.getDate();
        Date finalDate = jDateChooser2.getDate();

        // Basic validation
        if (name.isEmpty() || mobile.isEmpty() || address.isEmpty() || amountDue.isEmpty() || nic.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please fill all required fields!");
            return;
        }

        // Validate dates
        if (creditGivenDate == null || finalDate == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select both dates!");
            return;
        }

        // Validate Sri Lankan mobile number
        if (!isValidSriLankanMobile(mobile)) {
            jTextField2.requestFocus();
            return;
        }

        // Validate NIC number
        if (!isValidNIC(nic)) {
            jTextField6.requestFocus();
            return;
        }

        // Check if customer name already exists
        if (isCustomerNameExists(name)) {
            jTextField1.requestFocus();
            return;
        }

        // Check if NIC already exists
        if (isNICExists(nic)) {
            jTextField6.requestFocus();
            return;
        }

        // Validate amounts
        if (!isValidAmount(amountDue)) {
            jTextField4.requestFocus();
            return;
        }

        // Validate credit price (optional)
        if (!creditPrice.isEmpty() && !isValidAmount(creditPrice)) {
            jTextField5.requestFocus();
            return;
        }

        // Check if final date is after credit given date
        if (finalDate.before(creditGivenDate)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Final date must be after credit given date!");
            jDateChooser2.requestFocus();
            return;
        }

        try {
            Connection conn = MySQL.getConnection();
            String sql = "INSERT INTO credit_customer (customer_name, customer_phone_no, customer_address, customer_amount_due, credit_price, credit_date, final_date, nic, status_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, mobile);
            pst.setString(3, address);
            pst.setDouble(4, Double.parseDouble(amountDue));
            
            // Handle optional credit price
            if (creditPrice.isEmpty()) {
                pst.setNull(5, java.sql.Types.DOUBLE);
            } else {
                pst.setDouble(5, Double.parseDouble(creditPrice));
            }
            
            pst.setDate(6, new java.sql.Date(creditGivenDate.getTime()));
            pst.setDate(7, new java.sql.Date(finalDate.getTime()));
            pst.setString(8, nic.trim().toUpperCase()); // Store NIC in uppercase
            pst.setInt(9, 1); // Default status_id (assuming 1 = Active)
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "Customer added successfully!");
                clearFields();
                this.dispose(); // Close the dialog after successful save
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Failed to add customer!");
            }
            
            pst.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Database error: " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jDateChooser3 = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jTextField1.setText("Nimal Jayarathne");
        jTextField1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Name *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jTextField2.setText("0771234567");
        jTextField2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Phone No *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N

        jTextField3.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jTextField3.setText("Colombo 8");
        jTextField3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Address  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jTextField4.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jTextField4.setText("10000");
        jTextField4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount Due *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        jTextField4.setPreferredSize(new java.awt.Dimension(64, 40));
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jDateChooser2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Final Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        jDateChooser2.setOpaque(false);
        jDateChooser2.setPreferredSize(new java.awt.Dimension(106, 40));

        jButton2.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(115, 230, 203));
        jButton1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jButton1.setText("Save");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jDateChooser3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Credit Given *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        jDateChooser3.setOpaque(false);
        jDateChooser3.setPreferredSize(new java.awt.Dimension(106, 40));

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(8, 147, 176));
        jLabel2.setText("Add New Customer");

        jSeparator2.setForeground(new java.awt.Color(0, 137, 176));

        jTextField5.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jTextField5.setText("10000");
        jTextField5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Credit Price (optional)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        jTextField5.setPreferredSize(new java.awt.Dimension(64, 40));
        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        jTextField6.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jTextField6.setText("200530100534");
        jTextField6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NIC  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
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
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTextField3)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jDateChooser3, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jTextField6))
                        .addGap(21, 21, 21))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
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

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        saveCustomer();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

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
            java.util.logging.Logger.getLogger(AddNewCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddNewCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddNewCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddNewCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddNewCustomer dialog = new AddNewCustomer(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private com.toedter.calendar.JDateChooser jDateChooser3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
}
