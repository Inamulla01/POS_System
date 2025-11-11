/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class UpdateCustomer extends javax.swing.JDialog {

    private int customerId;
    private boolean isUpdating = false; // Flag to prevent multiple updates

    /**
     * Creates new form UpdateCustomer
     */
    public UpdateCustomer(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    // Constructor with customer ID to load data
    public UpdateCustomer(java.awt.Frame parent, boolean modal, int customerId) {
        super(parent, modal);
        this.customerId = customerId;
        initComponents();
        initializeDialog();
        loadCustomerData();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();
        setupSaveButton();

        // Set initial focus
        name.requestFocus();
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private boolean areAllRequiredFieldsFilled() {
        return !name.getText().trim().isEmpty()
                && !phoneNo.getText().trim().isEmpty()
                && !address.getText().trim().isEmpty()
                && !nic.getText().trim().isEmpty();
    }

    // ---------------- BUTTON STYLES AND EFFECTS ----------------
    private void setupButtonStyles() {
        // Setup gradient buttons for Save and Cancel
        setupGradientButton(saveBtn);
        setupGradientButton(cancelBtn);
        setupGradientButton(clearFormBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(updateIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        // Setup credit button with the same style as add buttons
        creditBtn.setBorderPainted(false);
        creditBtn.setContentAreaFilled(false);
        creditBtn.setFocusPainted(false);
        creditBtn.setOpaque(false);
        creditBtn.setFocusable(true);
        creditBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon creditIcon = new FlatSVGIcon("lk/com/pos/icon/credit-add.svg", 25, 25);
        creditIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        creditBtn.setIcon(creditIcon);

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
        // Mouse listeners for saveBtn
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(hoverIcon);
                saveBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                saveBtn.setIcon(normalIcon);
                saveBtn.repaint();
            }
        });

        // Mouse listeners for clearFormBtn
        clearFormBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(hoverIcon);
                clearFormBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
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

        // Mouse listeners for creditBtn (same as add buttons style)
        creditBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/credit-add.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                creditBtn.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/credit-add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                creditBtn.setIcon(normalIcon);
            }
        });
    }

    private void setupButtonFocusListeners() {
        // Focus listeners for saveBtn
        saveBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(focusedIcon);
                saveBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                saveBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                saveBtn.setIcon(normalIcon);
                saveBtn.repaint();
            }
        });

        // Focus listeners for clearFormBtn
        clearFormBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(focusedIcon);
                clearFormBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
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

        // Focus listeners for creditBtn (same as add buttons style)
        creditBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/credit-add.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                creditBtn.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/credit-add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                creditBtn.setIcon(normalIcon);
            }
        });
    }

    private void setupTooltips() {
        name.setToolTipText("Type customer name and press ENTER to move to next field");
        phoneNo.setToolTipText("Type phone number and press ENTER to move to next field");
        address.setToolTipText("Type address and press ENTER to move to NIC field");
        nic.setToolTipText("Type NIC number and press ENTER to move to buttons");
        saveBtn.setToolTipText("Click to update customer (or press ENTER when focused)");
        clearFormBtn.setToolTipText("Click to clear form (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
        creditBtn.setToolTipText("Credit payment functions (or press F1)");
    }

    // ---------------- VALIDATION AND BUSINESS LOGIC ----------------
    private void clearForm() {
        loadCustomerData(); // Reload original data
        name.requestFocus();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Form reset to original values!");
    }

    private void loadCustomerData() {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT * FROM credit_customer WHERE customer_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                name.setText(rs.getString("customer_name"));
                phoneNo.setText(rs.getString("customer_phone_no"));
                address.setText(rs.getString("customer_address"));
                nic.setText(rs.getString("nic"));

                // NIC field is now ENABLED and can be edited
                nic.setEnabled(true);
                nic.setBackground(Color.WHITE);
                nic.setToolTipText("Type NIC number and press ENTER to move to buttons");
            }

            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading customer data: " + e.getMessage());
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

    private boolean isCustomerNameExists(String customerName) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT COUNT(*) FROM credit_customer WHERE customer_name = ? AND customer_id != ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, customerName);
            pst.setInt(2, customerId);
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

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupKeyboardNavigation() {
        // Set up Enter key and arrow key navigation between fields
        name.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    phoneNo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    phoneNo.requestFocus();
                    evt.consume();
                }
            }
        });

        phoneNo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    name.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    name.requestFocus();
                    evt.consume();
                }
            }
        });

        address.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    nic.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    phoneNo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    nic.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    phoneNo.requestFocus();
                    evt.consume();
                }
            }
        });

        // NIC field keyboard navigation (now enabled)
        nic.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    // When all fields are filled, go directly to save button
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // When all fields are filled, go directly to save button
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    address.requestFocus();
                    evt.consume();
                }
            }
        });

        // Save button keyboard navigation - ONLY handle arrow keys, NOT ENTER key
        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                // Only handle navigation keys, let actionPerformed handle ENTER
                if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    nic.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    clearFormBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    cancelBtn.requestFocus();
                    evt.consume();
                }
                // DO NOT handle VK_ENTER here - let actionPerformed handle it
            }
        });

        // Clear Form button keyboard navigation
        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearForm();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    nic.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    saveBtn.requestFocus();
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
                    nic.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    saveBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    clearFormBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    saveBtn.requestFocus();
                    evt.consume();
                }
            }
        });

        // Credit button keyboard navigation
        creditBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    openCreditPayment();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    nic.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    name.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    saveBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    name.requestFocus();
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

        // Set up F1 key for credit functions
        getRootPane().registerKeyboardAction(
                evt -> openCreditPayment(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set up Alt+C to clear form
        getRootPane().registerKeyboardAction(
                evt -> clearForm(),
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void setupSaveButton() {
        // Remove all existing action listeners
        for (java.awt.event.ActionListener al : saveBtn.getActionListeners()) {
            saveBtn.removeActionListener(al);
        }

        // Add only one action listener
        saveBtn.addActionListener(evt -> {
            if (!isUpdating) {
                updateCustomer();
            }
        });
    }

    private void updateCustomer() {
        // Prevent multiple simultaneous updates
        if (isUpdating) {
            return;
        }
        isUpdating = true;

        Connection conn = null;
        PreparedStatement pstUpdate = null;
        PreparedStatement pstGetOldData = null;
        PreparedStatement pstCheckMessage = null;
        PreparedStatement pstInsertMessage = null;
        PreparedStatement pstInsertNotification = null;
        ResultSet rs = null;

        try {
            String customerName = name.getText().trim();
            String mobile = phoneNo.getText().trim();
            String customerAddress = address.getText().trim();
            String nicNumber = nic.getText().trim();

            // Basic validation
            if (customerName.isEmpty() || mobile.isEmpty() || customerAddress.isEmpty() || nicNumber.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Please fill all required fields!");
                isUpdating = false;
                return;
            }

            // Validate Sri Lankan mobile number using your regex
            if (!isValidSriLankanMobile(mobile)) {
                phoneNo.requestFocus();
                isUpdating = false;
                return;
            }

            // Check if customer name already exists (excluding current customer)
            if (isCustomerNameExists(customerName)) {
                name.requestFocus();
                isUpdating = false;
                return;
            }

            conn = MySQL.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get old customer data for notification message
            String getOldDataSql = "SELECT customer_name, customer_phone_no, customer_address, nic FROM credit_customer WHERE customer_id = ?";
            pstGetOldData = conn.prepareStatement(getOldDataSql);
            pstGetOldData.setInt(1, customerId);
            rs = pstGetOldData.executeQuery();

            String oldName = "";
            String oldPhone = "";
            String oldAddress = "";
            String oldNic = "";

            if (rs.next()) {
                oldName = rs.getString("customer_name");
                oldPhone = rs.getString("customer_phone_no");
                oldAddress = rs.getString("customer_address");
                oldNic = rs.getString("nic");
            }
            rs.close();
            pstGetOldData.close();

            // 2. Update the customer
            String updateSql = "UPDATE credit_customer SET customer_name = ?, customer_phone_no = ?, customer_address = ?, nic = ? WHERE customer_id = ?";
            pstUpdate = conn.prepareStatement(updateSql);
            pstUpdate.setString(1, customerName);
            pstUpdate.setString(2, mobile);
            pstUpdate.setString(3, customerAddress);
            pstUpdate.setString(4, nicNumber);
            pstUpdate.setInt(5, customerId);

            int rowsAffected = pstUpdate.executeUpdate();

            if (rowsAffected > 0) {
                // 3. Create notification message with changes
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Customer updated: ").append(oldName);

                // Add changed fields to the message
                boolean hasChanges = false;
                if (!oldName.equals(customerName)) {
                    messageBuilder.append(" [Name: ").append(oldName).append(" → ").append(customerName).append("]");
                    hasChanges = true;
                }
                if (!oldPhone.equals(mobile)) {
                    if (hasChanges) {
                        messageBuilder.append(", ");
                    }
                    messageBuilder.append("[Phone: ").append(oldPhone).append(" → ").append(mobile).append("]");
                    hasChanges = true;
                }
                if (!oldAddress.equals(customerAddress)) {
                    if (hasChanges) {
                        messageBuilder.append(", ");
                    }
                    messageBuilder.append("[Address: ").append(oldAddress).append(" → ").append(customerAddress).append("]");
                    hasChanges = true;
                }
                if (!oldNic.equals(nicNumber)) {
                    if (hasChanges) {
                        messageBuilder.append(", ");
                    }
                    messageBuilder.append("[NIC: ").append(oldNic).append(" → ").append(nicNumber).append("]");
                    hasChanges = true;
                }

                // If no specific changes detected, just show general update message
                if (!hasChanges) {
                    messageBuilder.append(" - Details updated");
                }

                String messageText = messageBuilder.toString();

                // 4. Check if message already exists in massage table
                String checkMessageSql = "SELECT massage_id FROM massage WHERE massage = ?";
                pstCheckMessage = conn.prepareStatement(checkMessageSql);
                pstCheckMessage.setString(1, messageText);
                rs = pstCheckMessage.executeQuery();

                int messageId;

                if (rs.next()) {
                    // Message already exists, get the existing massage_id
                    messageId = rs.getInt("massage_id");
                } else {
                    // Message doesn't exist, insert new message
                    String insertMessageSql = "INSERT INTO massage (massage) VALUES (?)";
                    pstInsertMessage = conn.prepareStatement(insertMessageSql, PreparedStatement.RETURN_GENERATED_KEYS);
                    pstInsertMessage.setString(1, messageText);
                    pstInsertMessage.executeUpdate();

                    // Get the generated message ID
                    ResultSet generatedKeys = pstInsertMessage.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        messageId = generatedKeys.getInt(1);
                    } else {
                        throw new Exception("Failed to get generated message ID");
                    }
                    generatedKeys.close();
                }

                // 5. Insert notification (msg_type_id 20 for "Edit Customer")
                String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (1, NOW(), 20, ?)";
                pstInsertNotification = conn.prepareStatement(notificationSql);
                pstInsertNotification.setInt(1, messageId);
                pstInsertNotification.executeUpdate();

                // Commit transaction
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "Customer updated successfully!");
                this.dispose(); // Close the dialog after successful update
            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Failed to update customer!");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Database error: " + e.getMessage());
        } finally {
            // Close all resources
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstUpdate != null) {
                    pstUpdate.close();
                }
                if (pstGetOldData != null) {
                    pstGetOldData.close();
                }
                if (pstCheckMessage != null) {
                    pstCheckMessage.close();
                }
                if (pstInsertMessage != null) {
                    pstInsertMessage.close();
                }
                if (pstInsertNotification != null) {
                    pstInsertNotification.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Always reset the flag
            isUpdating = false;
        }
    }

    private void openCreditPayment() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddCredit dialog = new AddCredit(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening credit payment dialog: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        name = new javax.swing.JTextField();
        phoneNo = new javax.swing.JTextField();
        address = new javax.swing.JTextField();
        jLabel = new javax.swing.JLabel();
        jSeparator = new javax.swing.JSeparator();
        nic = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        creditBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Customer");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        name.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        name.setText("Nimal Jayarathne");
        name.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Name *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameActionPerformed(evt);
            }
        });

        phoneNo.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        phoneNo.setText("0771234567");
        phoneNo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Phone No *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N

        address.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        address.setText("Colombo 8");
        address.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Address  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressActionPerformed(evt);
            }
        });

        jLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel.setForeground(new java.awt.Color(8, 147, 176));
        jLabel.setText("Edit Customer");

        jSeparator.setForeground(new java.awt.Color(0, 137, 176));

        nic.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        nic.setText("200530100534");
        nic.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NIC  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        nic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nicActionPerformed(evt);
            }
        });

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setForeground(new java.awt.Color(8, 147, 176));
        cancelBtn.setText("Cancel");
        cancelBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cancelBtnKeyPressed(evt);
            }
        });

        clearFormBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        clearFormBtn.setForeground(new java.awt.Color(8, 147, 176));
        clearFormBtn.setText("Clear Form");
        clearFormBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        clearFormBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFormBtnActionPerformed(evt);
            }
        });
        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                clearFormBtnKeyPressed(evt);
            }
        });

        saveBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        saveBtn.setForeground(new java.awt.Color(8, 147, 176));
        saveBtn.setText("Update");
        saveBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });
        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                saveBtnKeyPressed(evt);
            }
        });

        creditBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        creditBtn.setForeground(new java.awt.Color(8, 147, 176));
        creditBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        creditBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creditBtnActionPerformed(evt);
            }
        });
        creditBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                creditBtnKeyPressed(evt);
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
                        .addComponent(jLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(creditBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phoneNo, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(address, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(nic))
                .addGap(21, 21, 21))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(creditBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(phoneNo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameActionPerformed
        phoneNo.requestFocus();
    }//GEN-LAST:event_nameActionPerformed

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed
        nic.requestFocus();
    }//GEN-LAST:event_addressActionPerformed

    private void nicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nicActionPerformed
        saveBtn.requestFocus();
    }//GEN-LAST:event_nicActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        }
    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        updateCustomer();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            updateCustomer();
        }
    }//GEN-LAST:event_saveBtnKeyPressed

    private void creditBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditBtnActionPerformed
        openCreditPayment();
    }//GEN-LAST:event_creditBtnActionPerformed

    private void creditBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_creditBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F1) {
            openCreditPayment();
        }
    }//GEN-LAST:event_creditBtnKeyPressed

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
            java.util.logging.Logger.getLogger(UpdateCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateCustomer dialog = new UpdateCustomer(new javax.swing.JFrame(), true);
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
    private javax.swing.JTextField address;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JButton creditBtn;
    private javax.swing.JLabel jLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator;
    private javax.swing.JTextField name;
    private javax.swing.JTextField nic;
    private javax.swing.JTextField phoneNo;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
