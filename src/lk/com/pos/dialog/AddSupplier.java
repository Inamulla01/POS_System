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
public class AddSupplier extends javax.swing.JDialog {

    /**
     * Creates new form AddSupplier
     */
    public AddSupplier(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        clearFields();

        // Setup keyboard navigation
        setupFocusTraversal();
        setupArrowKeyNavigation();
        addEnterKeyNavigation();

        // Setup button styles and effects
        setupButtonStyles();

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Setup tooltips
        setupTooltips();

        // Set initial focus
        name.requestFocus();
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupFocusTraversal() {
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(name,
                phoneNo,
                address,
                companyfield,
                registerNo,
                cancelBtn,
                saveBtn
        );

        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)));
    }

    private void setupArrowKeyNavigation() {
        // Add arrow key navigation to all components
        name.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, name);
            }
        });

        phoneNo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, phoneNo);
            }
        });

        address.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, address);
            }
        });

        companyfield.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, companyfield);
            }
        });

        registerNo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, registerNo);
            }
        });

        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveSupplier();
                } else {
                    handleArrowNavigation(evt, saveBtn);
                }
            }
        });

        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                } else {
                    handleArrowNavigation(evt, cancelBtn);
                }
            }
        });
    }

    private void handleArrowNavigation(java.awt.event.KeyEvent evt, java.awt.Component source) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                handleRightArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_LEFT:
                handleLeftArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                handleDownArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                handleUpArrow(source);
                evt.consume();
                break;
        }
    }

    private void handleRightArrow(java.awt.Component source) {
        if (source == name) {
            phoneNo.requestFocusInWindow();
        } else if (source == phoneNo) {
            address.requestFocusInWindow();
        } else if (source == address) {
            companyfield.requestFocusInWindow();
        } else if (source == companyfield) {
            registerNo.requestFocusInWindow();
        } else if (source == registerNo) {
            // When all fields are filled, go directly to save button
            if (areAllRequiredFieldsFilled()) {
                saveBtn.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
        } else if (source == cancelBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            name.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == name) {
            saveBtn.requestFocusInWindow();
        } else if (source == phoneNo) {
            name.requestFocusInWindow();
        } else if (source == address) {
            phoneNo.requestFocusInWindow();
        } else if (source == companyfield) {
            address.requestFocusInWindow();
        } else if (source == registerNo) {
            companyfield.requestFocusInWindow();
        } else if (source == cancelBtn) {
            registerNo.requestFocusInWindow();
        } else if (source == saveBtn) {
            if (areAllRequiredFieldsFilled()) {
                registerNo.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == name) {
            phoneNo.requestFocusInWindow();
        } else if (source == phoneNo) {
            address.requestFocusInWindow();
        } else if (source == address) {
            companyfield.requestFocusInWindow();
        } else if (source == companyfield) {
            registerNo.requestFocusInWindow();
        } else if (source == registerNo) {
            // When all fields are filled, go directly to save button
            if (areAllRequiredFieldsFilled()) {
                saveBtn.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
        } else if (source == cancelBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            name.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == name) {
            saveBtn.requestFocusInWindow();
        } else if (source == phoneNo) {
            name.requestFocusInWindow();
        } else if (source == address) {
            phoneNo.requestFocusInWindow();
        } else if (source == companyfield) {
            address.requestFocusInWindow();
        } else if (source == registerNo) {
            companyfield.requestFocusInWindow();
        } else if (source == cancelBtn) {
            registerNo.requestFocusInWindow();
        } else if (source == saveBtn) {
            if (areAllRequiredFieldsFilled()) {
                registerNo.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
        }
    }

    private void addEnterKeyNavigation() {
        // Map components to their next focus targets for Enter key
        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(name, phoneNo);
        enterNavigationMap.put(phoneNo, address);
        enterNavigationMap.put(address, companyfield);
        enterNavigationMap.put(companyfield, registerNo);

        // Special handling for registerNo field - go to save if all fields filled
        enterNavigationMap.put(registerNo, null);

        enterNavigationMap.put(cancelBtn, saveBtn);
        enterNavigationMap.put(saveBtn, name);

        // Add key listeners to all components
        for (java.awt.Component component : enterNavigationMap.keySet()) {
            component.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        // Special case for registerNo field - go to save if all fields filled
                        if (component == registerNo) {
                            if (areAllRequiredFieldsFilled()) {
                                saveBtn.requestFocusInWindow();
                            } else {
                                cancelBtn.requestFocusInWindow();
                            }
                        } else {
                            java.awt.Component nextComponent = enterNavigationMap.get(component);
                            if (nextComponent != null) {
                                nextComponent.requestFocusInWindow();
                            }
                        }
                        evt.consume();
                    }
                }
            });
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return !name.getText().trim().isEmpty()
                && !phoneNo.getText().trim().isEmpty()
                && !address.getText().trim().isEmpty()
                && !companyfield.getText().trim().isEmpty()
                && !registerNo.getText().trim().isEmpty();
    }

    private void setupKeyboardShortcuts() {
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

        // Set up F1 key for refresh/clear form
        getRootPane().registerKeyboardAction(
                evt -> clearForm(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    // ---------------- BUTTON STYLES AND EFFECTS ----------------
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

    private void setupButtonStyles() {
        // Setup gradient buttons
        setupGradientButton(saveBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(saveIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        refreshBtn.setBorderPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setOpaque(false);
        refreshBtn.setFocusable(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon categoryIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
        categoryIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        refreshBtn.setIcon(categoryIcon);

        // Setup mouse listeners for all buttons
        setupButtonMouseListeners();
        setupButtonFocusListeners();
    }

    private void setupButtonMouseListeners() {
        // Mouse listeners for saveBtn
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(hoverIcon);
                saveBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                saveBtn.setIcon(normalIcon);
                saveBtn.repaint();
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

        // Mouse listeners for refreshBtn
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
        // Focus listeners for saveBtn
        saveBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(focusedIcon);
                saveBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                saveBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                saveBtn.setIcon(normalIcon);
                saveBtn.repaint();
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

        // Focus listeners for refreshBtn
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
        name.setToolTipText("Type supplier name and press ENTER to move to next field");
        phoneNo.setToolTipText("Type phone number and press ENTER to move to next field");
        address.setToolTipText("Type address and press ENTER to move to next field");
        companyfield.setToolTipText("Type company name and press ENTER to move to next field");
        registerNo.setToolTipText("Type register number and press ENTER to move to buttons");
        saveBtn.setToolTipText("Click to save supplier (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
        refreshBtn.setToolTipText("Clear form (or press F1)");
    }

    // ---------------- VALIDATION AND BUSINESS LOGIC ----------------
    private void clearFields() {
        name.setText("");
        phoneNo.setText("");
        address.setText("");
        companyfield.setText("");
        registerNo.setText("");
    }

    private void clearForm() {
        clearFields();
        name.requestFocus();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Form cleared!");
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

    private boolean isSupplierNameExists(String supplierName) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT COUNT(*) FROM suppliers WHERE suppliers_name = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, supplierName);
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
        String supplierName = name.getText().trim();
        String mobile = phoneNo.getText().trim();
        String supplierAddress = address.getText().trim();
        String company = companyfield.getText().trim();
        String regNo = registerNo.getText().trim();

        // Basic validation
        if (supplierName.isEmpty() || mobile.isEmpty() || supplierAddress.isEmpty() || company.isEmpty() || regNo.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please fill all required fields!");
            return;
        }

        // Validate Sri Lankan mobile number using your regex
        if (!isValidSriLankanMobile(mobile)) {
            phoneNo.requestFocus();
            return;
        }

        // Check if supplier name already exists
        if (isSupplierNameExists(supplierName)) {
            name.requestFocus();
            return;
        }

        // Check if registration number already exists
        if (isRegistrationNoExists(regNo)) {
            registerNo.requestFocus();
            return;
        }

        try {
            Connection conn = MySQL.getConnection();
            String sql = "INSERT INTO suppliers (suppliers_name, suppliers_mobile, suppliers_address, Company, suppliers_reg_no) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, supplierName);
            pst.setString(2, mobile);
            pst.setString(3, supplierAddress);
            pst.setString(4, company);
            pst.setString(5, regNo);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                // Add notification for new supplier
                addSupplierNotification(supplierName, mobile);

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

    private void addSupplierNotification(String supplierName, String phoneNo) {
        try {
            // Create the notification message
            String message = "New supplier added: " + supplierName + " (Phone: " + phoneNo + ")";

            int messageId;

            // Check if the message already exists in the massage table
            String checkMessageQuery = "SELECT massage_id FROM massage WHERE massage = '" + message + "'";
            ResultSet checkRs = MySQL.executeSearch(checkMessageQuery);

            if (checkRs.next()) {
                // Message already exists, get the existing ID
                messageId = checkRs.getInt("massage_id");
                checkRs.close();
            } else {
                // Message doesn't exist, insert new one
                String insertMessageQuery = "INSERT INTO massage (massage) VALUES ('" + message + "')";
                MySQL.executeIUD(insertMessageQuery);

                // Get the last inserted message ID
                ResultSet messageRs = MySQL.executeSearch("SELECT LAST_INSERT_ID() as message_id");
                if (messageRs.next()) {
                    messageId = messageRs.getInt("message_id");
                } else {
                    // Fallback if LAST_INSERT_ID() doesn't work
                    messageId = getMaxMessageId();
                }
                if (messageRs != null) {
                    messageRs.close();
                }
            }

            // Insert into notifocation table
            // msg_type_id 10 corresponds to 'Add New Supplier' based on your msg_type table
            String insertNotificationQuery = String.format(
                    "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) "
                    + "VALUES (1, NOW(), 10, %d)", messageId
            );

            MySQL.executeIUD(insertNotificationQuery);

        } catch (Exception e) {
            System.err.println("Error creating supplier notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

// Helper method to get the maximum message ID as fallback
    private int getMaxMessageId() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT MAX(massage_id) as max_id FROM massage");
            if (rs.next()) {
                return rs.getInt("max_id");
            }
        } catch (Exception e) {
            System.err.println("Error getting max message ID: " + e.getMessage());
        }
        return 0;
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        name = new javax.swing.JTextField();
        phoneNo = new javax.swing.JTextField();
        address = new javax.swing.JTextField();
        registerNo = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        refreshBtn = new javax.swing.JButton();
        companyfield = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New Supplier");

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

        registerNo.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        registerNo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Register No *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        registerNo.setPreferredSize(new java.awt.Dimension(64, 40));
        registerNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerNoActionPerformed(evt);
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

        saveBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        saveBtn.setForeground(new java.awt.Color(8, 147, 176));
        saveBtn.setText("Save");
        saveBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(8, 147, 176));
        jLabel2.setText("Add New Supplier");

        jSeparator2.setForeground(new java.awt.Color(0, 137, 176));

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

        companyfield.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        companyfield.setText("Colombo 8");
        companyfield.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Company Name  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        companyfield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                companyfieldActionPerformed(evt);
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
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGap(390, 390, 390)
                                .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 20, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(registerNo, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel2)
                                    .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                                    .addComponent(phoneNo))
                                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(companyfield, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phoneNo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(companyfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(registerNo, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22))
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

    private void nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameActionPerformed

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addressActionPerformed

    private void registerNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_registerNoActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        saveSupplier();

    }//GEN-LAST:event_saveBtnActionPerformed

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_refreshBtnActionPerformed

    private void refreshBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_refreshBtnKeyPressed
        clearForm();
    }//GEN-LAST:event_refreshBtnKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void companyfieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_companyfieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_companyfieldActionPerformed

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
    private javax.swing.JTextField address;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextField companyfield;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField name;
    private javax.swing.JTextField phoneNo;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JTextField registerNo;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
