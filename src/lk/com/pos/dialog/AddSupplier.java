package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.event.KeyEvent;
import javax.swing.*;
import raven.toast.Notifications;

/**
 *
 * @author HP
 */
public class AddSupplier extends javax.swing.JDialog {

    /**
     * Creates new form AddSupplier
     */
    public AddSupplier(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent); // Center the dialog
        clearFields();
        setupKeyboardNavigation();
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupKeyboardNavigation() {
        // Set up Enter key navigation between fields
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    jTextField2.requestFocus();
                    evt.consume();
                }
            }
        });

        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    jTextField3.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextField1.requestFocus();
                    evt.consume();
                }
            }
        });

        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    jTextField4.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextField2.requestFocus();
                    evt.consume();
                }
            }
        });

        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    jButton1.requestFocus(); // Save button
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextField3.requestFocus();
                    evt.consume();
                }
            }
        });

        // Save button keyboard navigation
        jButton1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveSupplier();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextField4.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    jButton2.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    jButton2.requestFocus();
                    evt.consume();
                }
            }
        });

        // Cancel button keyboard navigation
        jButton2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextField4.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    jButton1.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    jButton1.requestFocus();
                    evt.consume();
                }
            }
        });

        // Set up Escape key to close dialog
        getRootPane().registerKeyboardAction(
            evt -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set up Ctrl+Enter to save from anywhere
        getRootPane().registerKeyboardAction(
            evt -> saveSupplier(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set initial focus
        jTextField1.requestFocus();
    }

    private void clearFields() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
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

    private boolean isSupplierNameExists(String name) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT COUNT(*) FROM suppliers WHERE suppliers_name = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Supplier name already exists!");
                return true;
            }
            
            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isRegistrationNoExists(String regNo) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT COUNT(*) FROM suppliers WHERE suppliers_reg_no = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, regNo);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Registration number already exists!");
                return true;
            }
            
            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveSupplier() {
        String name = jTextField1.getText().trim();
        String mobile = jTextField2.getText().trim();
        String address = jTextField3.getText().trim();
        String regNo = jTextField4.getText().trim();

        // Basic validation
        if (name.isEmpty() || mobile.isEmpty() || address.isEmpty() || regNo.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please fill all required fields!");
            return;
        }

        // Validate Sri Lankan mobile number using your regex
        if (!isValidSriLankanMobile(mobile)) {
            jTextField2.requestFocus();
            return;
        }

        // Check if supplier name already exists
        if (isSupplierNameExists(name)) {
            jTextField1.requestFocus();
            return;
        }

        // Check if registration number already exists
        if (isRegistrationNoExists(regNo)) {
            jTextField4.requestFocus();
            return;
        }

        try {
            Connection conn = MySQL.getConnection();
            String sql = "INSERT INTO suppliers (suppliers_name, suppliers_mobile, suppliers_address, suppliers_reg_no) VALUES (?, ?, ?, ?)";
            
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, mobile);
            pst.setString(3, address);
            pst.setString(4, regNo);
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "Supplier added successfully!");
                clearFields();
                this.dispose(); // Close the dialog after successful save
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Failed to add supplier!");
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
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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
        jTextField4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Register No *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        jTextField4.setPreferredSize(new java.awt.Dimension(64, 40));
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

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

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(8, 147, 176));
        jLabel2.setText("Add New Customer");

        jSeparator2.setForeground(new java.awt.Color(0, 137, 176));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.LEADING))
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(21, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
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
        saveSupplier();

    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(AddSupplier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddSupplier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddSupplier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddSupplier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddSupplier dialog = new AddSupplier(new javax.swing.JFrame(), true);
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
