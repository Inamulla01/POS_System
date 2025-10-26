package lk.com.pos.dialog;

import com.formdev.flatlaf.FlatLightLaf;
import lk.com.pos.validation.Validater;
import lk.com.pos.connection.MySQL;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.DefaultComboBoxModel;
import java.util.Vector;
import raven.toast.Notifications;
import javax.swing.ImageIcon;
import java.awt.Image;

/**
 *
 * @author moham
 */
public class AddNewUser extends javax.swing.JDialog {

    // Track password visibility state
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
    // Store scaled icons
    private ImageIcon eyeClosedIcon;
    private ImageIcon eyeOpenIcon;

    /**
     * Creates new form AddNewUser
     */
    public AddNewUser(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        loadUserRoles();
        loadIcons();
        setupPasswordToggleButtons();
    }

    /**
     * Load and prepare icons
     */
    private void loadIcons() {
        try {
            // Try loading PNG icons first (recommended)
            java.net.URL eyeClosedUrl = getClass().getResource("/lk/com/pos/icon/eye-closed.png");
            java.net.URL eyeOpenUrl = getClass().getResource("/lk/com/pos/icon/eye-open.png");
            
            // If PNG not found, try SVG
            if (eyeClosedUrl == null) {
                eyeClosedUrl = getClass().getResource("/lk/com/pos/icon/eye-closed.svg");
            }
            if (eyeOpenUrl == null) {
                eyeOpenUrl = getClass().getResource("/lk/com/pos/icon/eye-open.svg");
            }
            
            if (eyeClosedUrl != null && eyeOpenUrl != null) {
                // Load and scale icons
                ImageIcon originalClosed = new ImageIcon(eyeClosedUrl);
                ImageIcon originalOpen = new ImageIcon(eyeOpenUrl);
                
                // Scale to 20x20
                Image scaledClosed = originalClosed.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                Image scaledOpen = originalOpen.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                
                eyeClosedIcon = new ImageIcon(scaledClosed);
                eyeOpenIcon = new ImageIcon(scaledOpen);
                
                System.out.println("Icons loaded successfully from: " + eyeClosedUrl);
            } else {
                System.out.println("Icon files not found. Using fallback.");
                createFallbackIcons();
            }
        } catch (Exception e) {
            System.out.println("Error loading icons: " + e.getMessage());
            e.printStackTrace();
            createFallbackIcons();
        }
    }
    
    /**
     * Create simple fallback icons if files not found
     */
    private void createFallbackIcons() {
        // Create simple icons programmatically
        int size = 20;
        
        // Eye closed icon (simple circle)
        java.awt.image.BufferedImage closedImg = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = closedImg.createGraphics();
        g2d.setColor(java.awt.Color.GRAY);
        g2d.fillOval(2, 2, 16, 16);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillOval(7, 7, 6, 6);
        g2d.dispose();
        eyeClosedIcon = new ImageIcon(closedImg);
        
        // Eye open icon (simple circle with line)
        java.awt.image.BufferedImage openImg = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        g2d = openImg.createGraphics();
        g2d.setColor(java.awt.Color.GRAY);
        g2d.fillOval(2, 2, 16, 16);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillOval(7, 7, 6, 6);
        g2d.setColor(java.awt.Color.RED);
        g2d.setStroke(new java.awt.BasicStroke(2));
        g2d.drawLine(2, 18, 18, 2);
        g2d.dispose();
        eyeOpenIcon = new ImageIcon(openImg);
    }

    /**
     * Setup password toggle buttons with icons and styling
     */
    private void setupPasswordToggleButtons() {
        // Setup password eye button
        passwordEyeButton.setIcon(eyeClosedIcon);
        passwordEyeButton.setText("");
        passwordEyeButton.setBorderPainted(false);
        passwordEyeButton.setContentAreaFilled(false);
        passwordEyeButton.setFocusPainted(false);
        passwordEyeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        passwordEyeButton.setToolTipText("Show/Hide Password");

        // Setup confirm password eye button
        confirmPasswordEyeButton.setIcon(eyeClosedIcon);
        confirmPasswordEyeButton.setText("");
        confirmPasswordEyeButton.setBorderPainted(false);
        confirmPasswordEyeButton.setContentAreaFilled(false);
        confirmPasswordEyeButton.setFocusPainted(false);
        confirmPasswordEyeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        confirmPasswordEyeButton.setToolTipText("Show/Hide Password");
    }

    /**
     * Toggle password visibility
     */
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordField.setEchoChar((char) 0); // Show password
            passwordEyeButton.setIcon(eyeOpenIcon);
        } else {
            passwordField.setEchoChar('•'); // Hide password
            passwordEyeButton.setIcon(eyeClosedIcon);
        }
    }

    /**
     * Toggle confirm password visibility
     */
    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if (isConfirmPasswordVisible) {
            confirmPasswordField.setEchoChar((char) 0); // Show password
            confirmPasswordEyeButton.setIcon(eyeOpenIcon);
        } else {
            confirmPasswordField.setEchoChar('•'); // Hide password
            confirmPasswordEyeButton.setIcon(eyeClosedIcon);
        }
    }

    /**
     * Load user roles into the combo box from database using your preferred
     * structure
     */
    private void loadUserRoles() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT * FROM role");
            Vector<String> userRoles = new Vector();
            userRoles.add("Select User Role");

            while (rs.next()) {
                String roleName = rs.getString("role_name");
                userRoles.add(roleName);
            }

            DefaultComboBoxModel dcm = new DefaultComboBoxModel(userRoles);
            userRoleCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error loading user roles: " + e.getMessage());
        }
    }

    /**
     * Validate and format username - remove spaces and convert to lowercase
     */
    private String formatUsername(String username) {
        // Remove all spaces and convert to lowercase
        return username.replaceAll("\\s+", "").toLowerCase();
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        // Get and format username
        String rawUsername = userNameField.getText().trim();
        String formattedUsername = formatUsername(rawUsername);

        // Set the formatted username back to the field
        if (!rawUsername.equals(formattedUsername)) {
            userNameField.setText(formattedUsername);
        }

        // Validate username
        if (!Validater.isInputFieldValid(formattedUsername)) {
            userNameField.requestFocus();
            return false;
        }

        // Check if username already exists
        if (isUsernameExists(formattedUsername)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Username '" + formattedUsername + "' already exists! Please choose a different username.");
            userNameField.requestFocus();
            userNameField.selectAll();
            return false;
        }

        // Validate password
        String password = new String(passwordField.getPassword());
        if (!Validater.isPasswordValid(password)) {
            passwordField.requestFocus();
            return false;
        }

        // Validate confirm password
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (!password.equals(confirmPassword)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Password and Confirm Password do not match!");
            confirmPasswordField.requestFocus();
            return false;
        }

        // Validate user role selection
        if (userRoleCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a user role");
            userRoleCombo.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Check if username already exists in database
     */
    private boolean isUsernameExists(String username) {
        try {
            String query = "SELECT COUNT(*) as count FROM user WHERE name = '" + username + "'";
            ResultSet rs = MySQL.executeSearch(query);
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error checking username: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get role_id from role_name
     */
    private int getRoleId(String roleName) {
        try {
            String query = "SELECT role_id FROM role WHERE role_name = '" + roleName + "'";
            ResultSet rs = MySQL.executeSearch(query);
            if (rs.next()) {
                return rs.getInt("role_id");
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error getting role ID: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Hash password for security
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Password encryption error: " + e.getMessage());
            return password; // Fallback to plain text (not recommended for production)
        }
    }

    /**
     * Save new user to database
     */
    private void saveUser() {
        try {
            String rawUsername = userNameField.getText().trim();
            String formattedUsername = formatUsername(rawUsername);
            String password = new String(passwordField.getPassword());
            String roleName = userRoleCombo.getSelectedItem().toString();
            int roleId = getRoleId(roleName);

            if (roleId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid role selected!");
                return;
            }

            // Double check if username exists (in case of race condition)
            if (isUsernameExists(formattedUsername)) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Username '" + formattedUsername + "' already exists! Please choose a different username.");
                userNameField.requestFocus();
                userNameField.selectAll();
                return;
            }

            // Hash the password
            String hashedPassword = hashPassword(password);

            // Insert into database
            String query = "INSERT INTO user (name, password, role_id) VALUES ('"
                    + formattedUsername + "', '" + hashedPassword + "', " + roleId + ")";

            MySQL.executeIUD(query);

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "User '" + formattedUsername + "' added successfully!");

            // Clear form
            clearForm();

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving user: " + e.getMessage());
        }
    }

    /**
     * Clear all form fields
     */
    private void clearForm() {
        userNameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        userRoleCombo.setSelectedIndex(0);
        userNameField.requestFocus();
        
        // Reset password visibility
        isPasswordVisible = false;
        isConfirmPasswordVisible = false;
        passwordField.setEchoChar('•');
        confirmPasswordField.setEchoChar('•');
        passwordEyeButton.setIcon(eyeClosedIcon);
        confirmPasswordEyeButton.setIcon(eyeClosedIcon);
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

        confirmPasswordEyeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmPasswordEyeButtonActionPerformed(evt);
            }
        });

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
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(confirmPasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                                .addComponent(passwordField))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(passwordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(confirmPasswordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))))
                .addGap(22, 22, 22))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(passwordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(confirmPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(confirmPasswordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
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
       togglePasswordVisibility();
    }//GEN-LAST:event_passwordEyeButtonActionPerformed

    private void confirmPasswordEyeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmPasswordEyeButtonActionPerformed
        toggleConfirmPasswordVisibility();
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
