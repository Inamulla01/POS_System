package lk.com.pos.dialog;

import com.formdev.flatlaf.FlatLightLaf;
import lk.com.pos.validation.Validater;
import lk.com.pos.connection.MySQL;
import java.awt.Image;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.*;
import raven.toast.Notifications;

public class AddNewUser extends javax.swing.JDialog {

    // ---------------- PASSWORD TOGGLE ----------------
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    private Icon eyeOpenIcon;
    private Icon eyeClosedIcon;

    public AddNewUser(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents(); // NetBeans-generated

        loadUserRoles();

        // Load icons
        ImageIcon eyeOpen = new ImageIcon(getClass().getResource("/lk/com/pos/icon/eye-open.png"));
        ImageIcon eyeClosed = new ImageIcon(getClass().getResource("/lk/com/pos/icon/eye-closed.png"));

        // Resize icons
        int iconSize = 20;
        eyeOpenIcon = new ImageIcon(eyeOpen.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
        eyeClosedIcon = new ImageIcon(eyeClosed.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));

        // Set initial icons
        passwordEyeButton.setIcon(eyeClosedIcon);
        confirmPasswordEyeButton.setIcon(eyeClosedIcon);

        // Remove button borders and background
        passwordEyeButton.setBorderPainted(false);
        passwordEyeButton.setContentAreaFilled(false);
        passwordEyeButton.setFocusPainted(false);
        passwordEyeButton.setText("");

        confirmPasswordEyeButton.setBorderPainted(false);
        confirmPasswordEyeButton.setContentAreaFilled(false);
        confirmPasswordEyeButton.setFocusPainted(false);
        confirmPasswordEyeButton.setText("");

        // Add toggle listeners
        passwordEyeButton.addActionListener(evt -> togglePasswordVisibility());
        confirmPasswordEyeButton.addActionListener(evt -> toggleConfirmPasswordVisibility());
    }

    // ---------------- PASSWORD TOGGLE METHODS ----------------
    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passwordField.setEchoChar('•');
            passwordEyeButton.setIcon(eyeClosedIcon);
        } else {
            passwordField.setEchoChar((char) 0);
            passwordEyeButton.setIcon(eyeOpenIcon);
        }
        passwordVisible = !passwordVisible;
    }

    private void toggleConfirmPasswordVisibility() {
        if (confirmPasswordVisible) {
            confirmPasswordField.setEchoChar('•');
            confirmPasswordEyeButton.setIcon(eyeClosedIcon);
        } else {
            confirmPasswordField.setEchoChar((char) 0);
            confirmPasswordEyeButton.setIcon(eyeOpenIcon);
        }
        confirmPasswordVisible = !confirmPasswordVisible;
    }

    // ---------------- DATABASE & VALIDATION ----------------
    private void loadUserRoles() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT * FROM role");
            Vector<String> userRoles = new Vector<>();
            userRoles.add("Select User Role");
            while (rs.next()) {
                userRoles.add(rs.getString("role_name"));
            }
            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(userRoles);
            userRoleCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading user roles: " + e.getMessage());
        }
    }

    private String formatUsername(String username) {
        return username.replaceAll("\\s+", "").toLowerCase();
    }

    private boolean validateInputs() {
        String rawUsername = userNameField.getText().trim();
        String formattedUsername = formatUsername(rawUsername);
        if (!rawUsername.equals(formattedUsername)) {
            userNameField.setText(formattedUsername);
        }

        if (!Validater.isInputFieldValid(formattedUsername)) {
            userNameField.requestFocus();
            return false;
        }

        if (isUsernameExists(formattedUsername)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Username '" + formattedUsername + "' already exists! Please choose a different username.");
            userNameField.requestFocus();
            userNameField.selectAll();
            return false;
        }

        String password = new String(passwordField.getPassword());
        if (!Validater.isPasswordValid(password)) {
            passwordField.requestFocus();
            return false;
        }

        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (!password.equals(confirmPassword)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Password and Confirm Password do not match!");
            confirmPasswordField.requestFocus();
            return false;
        }

        if (userRoleCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a user role");
            userRoleCombo.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isUsernameExists(String username) {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT COUNT(*) as count FROM user WHERE name='" + username + "'");
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error checking username: " + e.getMessage());
        }
        return false;
    }

    private int getRoleId(String roleName) {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT role_id FROM role WHERE role_name='" + roleName + "'");
            if (rs.next()) {
                return rs.getInt("role_id");
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error getting role ID: " + e.getMessage());
        }
        return -1;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Password encryption error: " + e.getMessage());
            return password; // fallback
        }
    }

    private void saveUser() {
        try {
            String formattedUsername = formatUsername(userNameField.getText().trim());
            String password = new String(passwordField.getPassword());
            String roleName = userRoleCombo.getSelectedItem().toString();
            int roleId = getRoleId(roleName);

            if (roleId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Invalid role selected!");
                return;
            }

            if (isUsernameExists(formattedUsername)) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Username '" + formattedUsername + "' already exists! Please choose a different username.");
                userNameField.requestFocus();
                userNameField.selectAll();
                return;
            }

            String hashedPassword = hashPassword(password);
            MySQL.executeIUD("INSERT INTO user (name, password, role_id) VALUES ('"
                    + formattedUsername + "', '" + hashedPassword + "', " + roleId + ")");

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "User '" + formattedUsername + "' added successfully!");
            clearForm();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error saving user: " + e.getMessage());
        }
    }

    private void clearForm() {
        userNameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        userRoleCombo.setSelectedIndex(0);
        userNameField.requestFocus();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        cancelBtn = new javax.swing.JButton();
        addBtn = new javax.swing.JButton();
        userNameField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        userRoleCombo = new javax.swing.JComboBox<>();
        confirmPasswordField = new javax.swing.JPasswordField();
        confirmPasswordEyeButton = new javax.swing.JButton();
        passwordEyeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New User");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(8, 147, 176));
        jLabel1.setText("Add New User");

        jSeparator1.setForeground(new java.awt.Color(0, 137, 176));

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        addBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        addBtn.setText("Save");
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        userNameField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        userNameField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Name *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        passwordField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        passwordField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Password *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        passwordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordFieldActionPerformed(evt);
            }
        });

        userRoleCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        userRoleCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        userRoleCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Role *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        confirmPasswordField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        confirmPasswordField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Confirmed Password *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        confirmPasswordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmPasswordFieldActionPerformed(evt);
            }
        });

        confirmPasswordEyeButton.setText("botton");
        confirmPasswordEyeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmPasswordEyeButtonActionPerformed(evt);
            }
        });

        passwordEyeButton.setText("buttom");
        passwordEyeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordEyeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(userNameField)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(userRoleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(33, 33, 33)
                            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(passwordField)
                                .addComponent(confirmPasswordField))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(confirmPasswordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(passwordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(22, 22, 22))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(confirmPasswordField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(confirmPasswordEyeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(userRoleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        if (validateInputs()) {
            saveUser();
        }
    }//GEN-LAST:event_addBtnActionPerformed

    private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordFieldActionPerformed

    private void confirmPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmPasswordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_confirmPasswordFieldActionPerformed

    private void passwordEyeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordEyeButtonActionPerformed

    }//GEN-LAST:event_passwordEyeButtonActionPerformed

    private void confirmPasswordEyeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmPasswordEyeButtonActionPerformed

    }//GEN-LAST:event_confirmPasswordEyeButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatLightLaf.setup();

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
            java.util.logging.Logger.getLogger(AddNewUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddNewUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddNewUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddNewUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddNewUser dialog = new AddNewUser(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton addBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton confirmPasswordEyeButton;
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton passwordEyeButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField userNameField;
    private javax.swing.JComboBox<String> userRoleCombo;
    // End of variables declaration//GEN-END:variables
}
