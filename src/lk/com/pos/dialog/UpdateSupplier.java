package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import raven.toast.Notifications;

/**
 *
 * @author HP
 */
public class UpdateSupplier extends javax.swing.JDialog {

    private int supplierId;
    
    /**
     * Creates new form UpdateSupplier
     */
    public UpdateSupplier(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }
    
    // Constructor with supplier ID to load data
    public UpdateSupplier(java.awt.Frame parent, boolean modal, int supplierId) {
        super(parent, modal);
        this.supplierId = supplierId;
        initComponents();
        initializeDialog();
        loadSupplierData();
    }
    
    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();
        
        // Set initial focus
        jTextField1.requestFocus();
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------

    private boolean areAllRequiredFieldsFilled() {
        return !jTextField1.getText().trim().isEmpty() &&
               !jTextField2.getText().trim().isEmpty() &&
               !jTextField3.getText().trim().isEmpty();
    }

    // ---------------- BUTTON STYLES AND EFFECTS ----------------
    private void setupButtonStyles() {
        // Setup gradient buttons for Save and Cancel
        setupGradientButton(updateBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        updateBtn.setIcon(updateIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        // Setup refresh button with the same style as add buttons
        refreshBtn.setBorderPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setOpaque(false);
        refreshBtn.setFocusable(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon refreshIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
        refreshIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        refreshBtn.setIcon(refreshIcon);

        // Setup mouse listeners for all buttons
        setupButtonMouseListeners();
        setupButtonFocusListeners();
    }

    private void setupGradientButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.decode("#0893B0"));
        button.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                int w = c.getWidth();
                int h = c.getHeight();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Check button state
                boolean isHover = button.getModel().isRollover();
                boolean isPressed = button.getModel().isPressed();
                boolean isFocused = button.hasFocus();

                // Default state - transparent with blue border
                if (!isFocused && !isHover && !isPressed) {
                    g2.setColor(new Color(0, 0, 0, 0)); // Transparent
                    g2.fillRoundRect(0, 0, w, h, 5, 5);

                    // Draw border
                    g2.setColor(Color.decode("#0893B0"));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
                } else {
                    // Gradient colors for hover/focus/pressed state
                    Color topColor = new Color(0x12, 0xB5, 0xA6); // Light
                    Color bottomColor = new Color(0x08, 0x93, 0xB0); // Dark

                    // Draw gradient
                    GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 5, 5);
                }

                // Draw button text
                super.paint(g, c);
            }
        });
    }

    private void setupButtonMouseListeners() {
        // Mouse listeners for updateBtn
        updateBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                updateBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                updateBtn.setIcon(hoverIcon);
                updateBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                updateBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                updateBtn.setIcon(normalIcon);
                updateBtn.repaint();
            }
        });

        // Mouse listeners for cancelBtn
        cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(hoverIcon);
                cancelBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });

        // Mouse listeners for refreshBtn (same as add buttons style)
        refreshBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                refreshBtn.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                refreshBtn.setIcon(normalIcon);
            }
        });
    }

    private void setupButtonFocusListeners() {
        // Focus listeners for updateBtn
        updateBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                updateBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                updateBtn.setIcon(focusedIcon);
                updateBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                updateBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                updateBtn.setIcon(normalIcon);
                updateBtn.repaint();
            }
        });

        // Focus listeners for cancelBtn
        cancelBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(focusedIcon);
                cancelBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });

        // Focus listeners for refreshBtn (same as add buttons style)
        refreshBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                refreshBtn.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                refreshBtn.setIcon(normalIcon);
            }
        });
    }

    private void setupTooltips() {
        jTextField1.setToolTipText("Type supplier name and press ENTER to move to next field");
        jTextField2.setToolTipText("Type phone number and press ENTER to move to next field");
        jTextField3.setToolTipText("Type address and press ENTER to move to buttons");
        jTextField4.setToolTipText("Registration number (read-only)");
        updateBtn.setToolTipText("Click to update supplier (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
        refreshBtn.setToolTipText("Reload original data (or press F1)");
    }

    // ---------------- VALIDATION AND BUSINESS LOGIC ----------------
    private void clearForm() {
        loadSupplierData(); // Reload original data
        jTextField1.requestFocus();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Form reset to original values!");
    }
    
    private void loadSupplierData() {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT * FROM suppliers WHERE suppliers_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, supplierId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                jTextField1.setText(rs.getString("suppliers_name"));
                jTextField2.setText(rs.getString("suppliers_mobile"));
                jTextField3.setText(rs.getString("suppliers_address"));
                jTextField4.setText(rs.getString("suppliers_reg_no"));
            }
            
            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading supplier data: " + e.getMessage());
        }
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
            String sql = "SELECT COUNT(*) FROM suppliers WHERE suppliers_name = ? AND suppliers_id != ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setInt(2, supplierId);
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
    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
private void setupKeyboardNavigation() {
    // Set up Enter key and arrow key navigation between fields
    jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                jTextField2.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
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
            } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                jTextField3.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                jTextField1.requestFocus();
                evt.consume();
            }
        }
    });

    jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                // When all fields are filled, go directly to update button
                if (areAllRequiredFieldsFilled()) {
                    updateBtn.requestFocusInWindow();
                } else {
                    cancelBtn.requestFocusInWindow();
                }
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                jTextField2.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                // When all fields are filled, go directly to update button
                if (areAllRequiredFieldsFilled()) {
                    updateBtn.requestFocusInWindow();
                } else {
                    cancelBtn.requestFocusInWindow();
                }
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                jTextField2.requestFocus();
                evt.consume();
            }
        }
    });

    // Update button keyboard navigation
    updateBtn.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                updateSupplier();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                jTextField3.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                cancelBtn.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                cancelBtn.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                cancelBtn.requestFocus();
                evt.consume();
            }
        }
    });

    // Cancel button keyboard navigation
    cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                dispose();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                jTextField3.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                updateBtn.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                updateBtn.requestFocus();
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                updateBtn.requestFocus();
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

    // Set up Ctrl+Enter to update from anywhere
    getRootPane().registerKeyboardAction(
        evt -> updateSupplier(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );

    // Set up F1 key for refresh/clear form
    getRootPane().registerKeyboardAction(
        evt -> clearForm(),
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );
}
    private void updateSupplier() {
        String name = jTextField1.getText().trim();
        String mobile = jTextField2.getText().trim();
        String address = jTextField3.getText().trim();

        // Basic validation
        if (name.isEmpty() || mobile.isEmpty() || address.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please fill all required fields!");
            return;
        }

        // Validate Sri Lankan mobile number using your regex
        if (!isValidSriLankanMobile(mobile)) {
            jTextField2.requestFocus();
            return;
        }

        // Check if supplier name already exists (excluding current supplier)
        if (isSupplierNameExists(name)) {
            jTextField1.requestFocus();
            return;
        }

        try {
            Connection conn = MySQL.getConnection();
            String sql = "UPDATE suppliers SET suppliers_name = ?, suppliers_mobile = ?, suppliers_address = ? WHERE suppliers_id = ?";
            
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, mobile);
            pst.setString(3, address);
            pst.setInt(4, supplierId);
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "Supplier updated successfully!");
                this.dispose(); // Close the dialog after successful update
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Failed to update supplier!");
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
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        cancelBtn = new javax.swing.JButton();
        updateBtn = new javax.swing.JButton();
        refreshBtn = new javax.swing.JButton();

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
        jTextField4.setEnabled(false);
        jTextField4.setOpaque(true);
        jTextField4.setPreferredSize(new java.awt.Dimension(64, 40));
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(8, 147, 176));
        jLabel2.setText("Edit Customer");

        jSeparator2.setForeground(new java.awt.Color(0, 137, 176));

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setForeground(new java.awt.Color(8, 147, 176));
        cancelBtn.setText("Cancel");
        cancelBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        updateBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        updateBtn.setForeground(new java.awt.Color(8, 147, 176));
        updateBtn.setText("Save");
        updateBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });

        refreshBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        refreshBtn.setForeground(new java.awt.Color(8, 147, 176));
        refreshBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        refreshBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBtnActionPerformed(evt);
            }
        });
        refreshBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                refreshBtnKeyPressed(evt);
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
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.LEADING))
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(21, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        updateSupplier();
    }//GEN-LAST:event_updateBtnActionPerformed

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_refreshBtnActionPerformed

    private void refreshBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_refreshBtnKeyPressed
        clearForm();
    }//GEN-LAST:event_refreshBtnKeyPressed

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
            java.util.logging.Logger.getLogger(UpdateSupplier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateSupplier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateSupplier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateSupplier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateSupplier dialog = new UpdateSupplier(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
