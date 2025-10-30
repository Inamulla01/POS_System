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
import java.util.Date;
import javax.swing.*;
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
//        super(parent, modal);
initComponents();
//        initializeDialog();
}
//
//    private void initializeDialog() {
//        setLocationRelativeTo(getParent());
//        clearFields();
//
//        // Setup keyboard navigation
//        setupFocusTraversal();
//        setupArrowKeyNavigation();
//        addEnterKeyNavigation();
//
//        // Setup button styles and effects
//        setupButtonStyles();
//
//        // Setup date chooser navigation
//        setupDateChooserNavigation();
//
//        // Setup keyboard shortcuts
//        setupKeyboardShortcuts();
//
//        // Setup tooltips
//        setupTooltips();
//
//        // Set initial focus
//        name.requestFocus();
//    }
//
//    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
//    private void setupFocusTraversal() {
//        java.util.List<java.awt.Component> order = java.util.Arrays.asList(name,
//                phoneNo,
//                address,
//                nic,
//                amountDue,
//                jTextField5,
//                creditGive.getDateEditor().getUiComponent(),
//                finelDate.getDateEditor().getUiComponent(),
//                cancelBtn,
//                clearFormBtn,
//                saveBtn
//        );
//
//        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
//                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
//        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
//                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)));
//    }
//
//    private void setupArrowKeyNavigation() {
//        // Add arrow key navigation to all components
//        name.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                handleArrowNavigation(evt, name);
//            }
//        });
//
//        phoneNo.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                handleArrowNavigation(evt, phoneNo);
//            }
//        });
//
//        address.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                handleArrowNavigation(evt, address);
//            }
//        });
//
//        amountDue.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                handleArrowNavigation(evt, amountDue);
//            }
//        });
//
//        jTextField5.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                handleArrowNavigation(evt, jTextField5);
//            }
//        });
//
//        nic.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                handleArrowNavigation(evt, nic);
//            }
//        });
//
//        creditGive.getDateEditor().getUiComponent().addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                handleArrowNavigation(evt, creditGive.getDateEditor().getUiComponent());
//            }
//        });
//
//        finelDate.getDateEditor().getUiComponent().addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                handleArrowNavigation(evt, finelDate.getDateEditor().getUiComponent());
//            }
//        });
//
//        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//                    saveCustomer();
//                } else {
//                    handleArrowNavigation(evt, saveBtn);
//                }
//            }
//        });
//
//        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//                    clearForm();
//                } else {
//                    handleArrowNavigation(evt, clearFormBtn);
//                }
//            }
//        });
//
//        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//                    dispose();
//                } else {
//                    handleArrowNavigation(evt, cancelBtn);
//                }
//            }
//        });
//    }
//
//    private void handleArrowNavigation(java.awt.event.KeyEvent evt, java.awt.Component source) {
//        switch (evt.getKeyCode()) {
//            case KeyEvent.VK_RIGHT:
//                handleRightArrow(source);
//                evt.consume();
//                break;
//            case KeyEvent.VK_LEFT:
//                handleLeftArrow(source);
//                evt.consume();
//                break;
//            case KeyEvent.VK_DOWN:
//                handleDownArrow(source);
//                evt.consume();
//                break;
//            case KeyEvent.VK_UP:
//                handleUpArrow(source);
//                evt.consume();
//                break;
//        }
//    }
//
//    private void handleRightArrow(java.awt.Component source) {
//        if (source == name) {
//            phoneNo.requestFocusInWindow();
//        } else if (source == phoneNo) {
//            address.requestFocusInWindow();
//        } else if (source == address) {
//            nic.requestFocusInWindow();
//        } else if (source == nic) {
//            amountDue.requestFocusInWindow();
//        } else if (source == amountDue) {
//            jTextField5.requestFocusInWindow();
//        } else if (source == jTextField5) {
//            creditGive.getDateEditor().getUiComponent().requestFocusInWindow();
//        } else if (source == creditGive.getDateEditor().getUiComponent()) {
//            finelDate.getDateEditor().getUiComponent().requestFocusInWindow();
//        } else if (source == finelDate.getDateEditor().getUiComponent()) {
//            cancelBtn.requestFocusInWindow();
//        } else if (source == cancelBtn) {
//            clearFormBtn.requestFocusInWindow();
//        } else if (source == clearFormBtn) {
//            saveBtn.requestFocusInWindow();
//        } else if (source == saveBtn) {
//            name.requestFocusInWindow();
//        }
//    }
//
//    private void handleLeftArrow(java.awt.Component source) {
//        if (source == name) {
//            saveBtn.requestFocusInWindow();
//        } else if (source == phoneNo) {
//            name.requestFocusInWindow();
//        } else if (source == address) {
//            phoneNo.requestFocusInWindow();
//        } else if (source == nic) {
//            address.requestFocusInWindow();
//        } else if (source == amountDue) {
//            nic.requestFocusInWindow();
//        } else if (source == jTextField5) {
//            amountDue.requestFocusInWindow();
//        } else if (source == creditGive.getDateEditor().getUiComponent()) {
//            jTextField5.requestFocusInWindow();
//        } else if (source == finelDate.getDateEditor().getUiComponent()) {
//            creditGive.getDateEditor().getUiComponent().requestFocusInWindow();
//        } else if (source == cancelBtn) {
//            finelDate.getDateEditor().getUiComponent().requestFocusInWindow();
//        } else if (source == clearFormBtn) {
//            cancelBtn.requestFocusInWindow();
//        } else if (source == saveBtn) {
//            clearFormBtn.requestFocusInWindow();
//        }
//    }
//
//    private void handleDownArrow(java.awt.Component source) {
//        if (source == name) {
//            phoneNo.requestFocusInWindow();
//        } else if (source == phoneNo) {
//            address.requestFocusInWindow();
//        } else if (source == address) {
//            nic.requestFocusInWindow();
//        } else if (source == nic) {
//            amountDue.requestFocusInWindow();
//        } else if (source == amountDue) {
//            jTextField5.requestFocusInWindow();
//        } else if (source == jTextField5) {
//            creditGive.getDateEditor().getUiComponent().requestFocusInWindow();
//        } else if (source == creditGive.getDateEditor().getUiComponent()) {
//            finelDate.getDateEditor().getUiComponent().requestFocusInWindow();
//        } else if (source == finelDate.getDateEditor().getUiComponent()) {
//            cancelBtn.requestFocusInWindow();
//        } else if (source == cancelBtn) {
//            clearFormBtn.requestFocusInWindow();
//        } else if (source == clearFormBtn) {
//            saveBtn.requestFocusInWindow();
//        } else if (source == saveBtn) {
//            name.requestFocusInWindow();
//        }
//    }
//
//    private void handleUpArrow(java.awt.Component source) {
//        if (source == name) {
//            saveBtn.requestFocusInWindow();
//        } else if (source == phoneNo) {
//            name.requestFocusInWindow();
//        } else if (source == address) {
//            phoneNo.requestFocusInWindow();
//        } else if (source == nic) {
//            address.requestFocusInWindow();
//        } else if (source == amountDue) {
//            nic.requestFocusInWindow();
//        } else if (source == jTextField5) {
//            amountDue.requestFocusInWindow();
//        } else if (source == creditGive.getDateEditor().getUiComponent()) {
//            jTextField5.requestFocusInWindow();
//        } else if (source == finelDate.getDateEditor().getUiComponent()) {
//            creditGive.getDateEditor().getUiComponent().requestFocusInWindow();
//        } else if (source == cancelBtn) {
//            finelDate.getDateEditor().getUiComponent().requestFocusInWindow();
//        } else if (source == clearFormBtn) {
//            cancelBtn.requestFocusInWindow();
//        } else if (source == saveBtn) {
//            clearFormBtn.requestFocusInWindow();
//        }
//    }
//
//    private void addEnterKeyNavigation() {
//        // Map components to their next focus targets for Enter key
//        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
//        enterNavigationMap.put(name, phoneNo);
//        enterNavigationMap.put(phoneNo, address);
//        enterNavigationMap.put(address, nic);
//        enterNavigationMap.put(nic, amountDue);
//        enterNavigationMap.put(amountDue, jTextField5);
//        enterNavigationMap.put(jTextField5, creditGive.getDateEditor().getUiComponent());
//        enterNavigationMap.put(creditGive.getDateEditor().getUiComponent(), finelDate.getDateEditor().getUiComponent());
//        enterNavigationMap.put(finelDate.getDateEditor().getUiComponent(), cancelBtn);
//        enterNavigationMap.put(cancelBtn, clearFormBtn);
//        enterNavigationMap.put(clearFormBtn, saveBtn);
//        enterNavigationMap.put(saveBtn, name);
//
//        // Add key listeners to all components
//        for (java.awt.Component component : enterNavigationMap.keySet()) {
//            component.addKeyListener(new java.awt.event.KeyAdapter() {
//                @Override
//                public void keyPressed(java.awt.event.KeyEvent evt) {
//                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//                        java.awt.Component nextComponent = enterNavigationMap.get(component);
//                        if (nextComponent != null) {
//                            nextComponent.requestFocusInWindow();
//                        }
//                        evt.consume();
//                    }
//                }
//            });
//        }
//    }
//
//    private void setupDateChooserNavigation() {
//        // Get the actual text field components from date editors
//        javax.swing.JTextField creditDateEditor = (javax.swing.JTextField) creditGive.getDateEditor().getUiComponent();
//        javax.swing.JTextField finalDateEditor = (javax.swing.JTextField) finelDate.getDateEditor().getUiComponent();
//
//        // Add key listener to credit date editor
//        creditDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//                    finalDateEditor.requestFocusInWindow();
//                    evt.consume();
//                } else {
//                    handleArrowNavigation(evt, creditDateEditor);
//                }
//            }
//        });
//
//        // Add key listener to final date editor
//        finalDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//                    cancelBtn.requestFocusInWindow();
//                    evt.consume();
//                } else {
//                    handleArrowNavigation(evt, finalDateEditor);
//                }
//            }
//        });
//    }
//
//    private void setupKeyboardShortcuts() {
//        // Set up Escape key to close dialog
//        getRootPane().registerKeyboardAction(
//                evt -> dispose(),
//                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
//                JComponent.WHEN_IN_FOCUSED_WINDOW
//        );
//
//        // Set up Ctrl+Enter to save from anywhere
//        getRootPane().registerKeyboardAction(
//                evt -> saveCustomer(),
//                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
//                JComponent.WHEN_IN_FOCUSED_WINDOW
//        );
//    }
//
//    // ---------------- BUTTON STYLES AND EFFECTS ----------------
//    private void setupGradientButton(JButton button) {
//        button.setContentAreaFilled(false);
//        button.setFocusPainted(false);
//        button.setBorderPainted(false);
//        button.setForeground(Color.decode("#0893B0"));
//        button.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
//        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
//            @Override
//            public void paint(Graphics g, javax.swing.JComponent c) {
//                Graphics2D g2 = (Graphics2D) g;
//                int w = c.getWidth();
//                int h = c.getHeight();
//
//                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//                // Check button state
//                boolean isHover = button.getModel().isRollover();
//                boolean isPressed = button.getModel().isPressed();
//                boolean isFocused = button.hasFocus();
//
//                // Default state - transparent with blue border
//                if (!isFocused && !isHover && !isPressed) {
//                    g2.setColor(new Color(0, 0, 0, 0)); // Transparent
//                    g2.fillRoundRect(0, 0, w, h, 5, 5);
//
//                    // Draw border
//                    g2.setColor(Color.decode("#0893B0"));
//                    g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
//                } else {
//                    // Gradient colors for hover/focus/pressed state
//                    Color topColor = new Color(0x12, 0xB5, 0xA6); // Light
//                    Color bottomColor = new Color(0x08, 0x93, 0xB0); // Dark
//
//                    // Draw gradient
//                    GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
//                    g2.setPaint(gp);
//                    g2.fillRoundRect(0, 0, w, h, 5, 5);
//                }
//
//                // Draw button text
//                super.paint(g, c);
//            }
//        });
//    }
//
//    private void setupButtonStyles() {
//        // Setup gradient buttons
//        setupGradientButton(saveBtn);
//        setupGradientButton(clearFormBtn);
//        setupGradientButton(cancelBtn);
//
//        // Create icons with original blue color for action buttons
//        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
//        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//        saveBtn.setIcon(saveIcon);
//
//        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
//        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//        clearFormBtn.setIcon(clearIcon);
//
//        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
//        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//        cancelBtn.setIcon(cancelIcon);
//
//        // Setup mouse listeners for all buttons
//        setupButtonMouseListeners();
//        setupButtonFocusListeners();
//    }
//
//    private void setupButtonMouseListeners() {
//        // Mouse listeners for saveBtn
//        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                saveBtn.setForeground(Color.WHITE);
//                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
//                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
//                saveBtn.setIcon(hoverIcon);
//                saveBtn.repaint();
//            }
//
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                saveBtn.setForeground(Color.decode("#0893B0"));
//                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
//                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//                saveBtn.setIcon(normalIcon);
//                saveBtn.repaint();
//            }
//        });
//
//        // Mouse listeners for clearFormBtn
//        clearFormBtn.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                clearFormBtn.setForeground(Color.WHITE);
//                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
//                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
//                clearFormBtn.setIcon(hoverIcon);
//                clearFormBtn.repaint();
//            }
//
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                clearFormBtn.setForeground(Color.decode("#0893B0"));
//                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
//                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//                clearFormBtn.setIcon(normalIcon);
//                clearFormBtn.repaint();
//            }
//        });
//
//        // Mouse listeners for cancelBtn
//        cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                cancelBtn.setForeground(Color.WHITE);
//                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
//                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
//                cancelBtn.setIcon(hoverIcon);
//                cancelBtn.repaint();
//            }
//
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                cancelBtn.setForeground(Color.decode("#0893B0"));
//                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
//                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//                cancelBtn.setIcon(normalIcon);
//                cancelBtn.repaint();
//            }
//        });
//    }
//
//    private void setupButtonFocusListeners() {
//        // Focus listeners for saveBtn
//        saveBtn.addFocusListener(new java.awt.event.FocusAdapter() {
//            public void focusGained(java.awt.event.FocusEvent evt) {
//                saveBtn.setForeground(Color.WHITE);
//                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
//                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
//                saveBtn.setIcon(focusedIcon);
//                saveBtn.repaint();
//            }
//
//            public void focusLost(java.awt.event.FocusEvent evt) {
//                saveBtn.setForeground(Color.decode("#0893B0"));
//                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
//                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//                saveBtn.setIcon(normalIcon);
//                saveBtn.repaint();
//            }
//        });
//
//        // Focus listeners for clearFormBtn
//        clearFormBtn.addFocusListener(new java.awt.event.FocusAdapter() {
//            public void focusGained(java.awt.event.FocusEvent evt) {
//                clearFormBtn.setForeground(Color.WHITE);
//                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
//                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
//                clearFormBtn.setIcon(focusedIcon);
//                clearFormBtn.repaint();
//            }
//
//            public void focusLost(java.awt.event.FocusEvent evt) {
//                clearFormBtn.setForeground(Color.decode("#0893B0"));
//                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
//                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//                clearFormBtn.setIcon(normalIcon);
//                clearFormBtn.repaint();
//            }
//        });
//
//        // Focus listeners for cancelBtn
//        cancelBtn.addFocusListener(new java.awt.event.FocusAdapter() {
//            public void focusGained(java.awt.event.FocusEvent evt) {
//                cancelBtn.setForeground(Color.WHITE);
//                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
//                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
//                cancelBtn.setIcon(focusedIcon);
//                cancelBtn.repaint();
//            }
//
//            public void focusLost(java.awt.event.FocusEvent evt) {
//                cancelBtn.setForeground(Color.decode("#0893B0"));
//                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
//                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
//                cancelBtn.setIcon(normalIcon);
//                cancelBtn.repaint();
//            }
//        });
//    }
//
//    private void setupTooltips() {
//        name.setToolTipText("Type customer name and press ENTER to move to next field");
//        phoneNo.setToolTipText("Type phone number and press ENTER to move to next field");
//        address.setToolTipText("Type address and press ENTER to move to next field");
//        nic.setToolTipText("Type NIC number and press ENTER to move to next field");
//        amountDue.setToolTipText("Type amount due and press ENTER to move to next field");
//        jTextField5.setToolTipText("Type credit price (optional) and press ENTER to move to next field");
//        creditGive.setToolTipText("<html>Select credit given date and press ENTER to move to next field<br>You can also type numbers: 01012024 for 01/01/2024</html>");
//        finelDate.setToolTipText("<html>Select final date and press ENTER to move to buttons<br>You can also type numbers: 31122024 for 31/12/2024</html>");
//        saveBtn.setToolTipText("Click to save customer (or press ENTER when focused)");
//        clearFormBtn.setToolTipText("Click to clear form (or press ENTER when focused)");
//        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
//    }
//
//    // ---------------- EXISTING VALIDATION AND BUSINESS LOGIC ----------------
//    private void clearFields() {
//        name.setText("");
//        phoneNo.setText("");
//        address.setText("");
//        amountDue.setText("0");
//        jTextField5.setText("0");
//        nic.setText("");
//        finelDate.setDate(null);
//        creditGive.setDate(null);
//    }
//
//    private boolean isValidSriLankanMobile(String mobile) {
//        // Remove any spaces or dashes
//        String cleanedMobile = mobile.replaceAll("[\\s-]", "");
//
//        // Use your exact regex pattern for Sri Lankan mobile validation
//        if (!cleanedMobile.matches("^(0{1})(7{1})([0|1|2|4|5|6|7|8]{1})([0-9]{7})$")) {
//            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                    "Please enter a valid Sri Lankan mobile number (10 digits starting with 07)");
//            return false;
//        }
//        return true;
//    }
//
//    private boolean isValidNIC(String nic) {
//        if (nic == null || nic.trim().isEmpty()) {
//            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                    "NIC number is required!");
//            return false;
//        }
//
//        // Remove spaces and convert to uppercase
//        String cleanedNIC = nic.trim().toUpperCase();
//
//        // Validate NIC format (old: 9 digits + V, new: 12 digits)
//        if (!cleanedNIC.matches("^([0-9]{9}[VvXx]|[0-9]{12})$")) {
//            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                    "Please enter a valid NIC number (9 digits with V/X or 12 digits)");
//            return false;
//        }
//        return true;
//    }
//
//    private boolean isCustomerNameExists(String name) {
//        try {
//            Connection conn = MySQL.getConnection();
//            String sql = "SELECT COUNT(*) FROM credit_customer WHERE customer_name = ?";
//            PreparedStatement pst = conn.prepareStatement(sql);
//            pst.setString(1, name);
//            ResultSet rs = pst.executeQuery();
//
//            if (rs.next() && rs.getInt(1) > 0) {
//                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                        "Customer name already exists!");
//                return true;
//            }
//
//            rs.close();
//            pst.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    private boolean isNICExists(String nic) {
//        try {
//            Connection conn = MySQL.getConnection();
//            String sql = "SELECT COUNT(*) FROM credit_customer WHERE nic = ?";
//            PreparedStatement pst = conn.prepareStatement(sql);
//            pst.setString(1, nic.trim().toUpperCase());
//            ResultSet rs = pst.executeQuery();
//
//            if (rs.next() && rs.getInt(1) > 0) {
//                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                        "NIC number already exists!");
//                return true;
//            }
//
//            rs.close();
//            pst.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    private boolean isValidAmount(String amount) {
//        try {
//            double amt = Double.parseDouble(amount);
//            if (amt < 0) {
//                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                        "Amount cannot be negative!");
//                return false;
//            }
//            return true;
//        } catch (NumberFormatException e) {
//            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                    "Please enter a valid amount!");
//            return false;
//        }
//    }
//
//    private void saveCustomer() {
//        String name = name.getText().trim();
//        String mobile = phoneNo.getText().trim();
//        String address = address.getText().trim();
//        String amountDue = amountDue.getText().trim();
//        String creditPrice = jTextField5.getText().trim();
//        String nic = nic.getText().trim();
//        Date creditGivenDate = creditGive.getDate();
//        Date finalDate = finelDate.getDate();
//
//        // Basic validation
//        if (name.isEmpty() || mobile.isEmpty() || address.isEmpty() || amountDue.isEmpty() || nic.isEmpty()) {
//            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                    "Please fill all required fields!");
//            return;
//        }
//
//        // Validate dates
//        if (creditGivenDate == null || finalDate == null) {
//            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                    "Please select both dates!");
//            return;
//        }
//
//        // Validate Sri Lankan mobile number
//        if (!isValidSriLankanMobile(mobile)) {
//            phoneNo.requestFocus();
//            return;
//        }
//
//        // Validate NIC number
//        if (!isValidNIC(nic)) {
//            nic.requestFocus();
//            return;
//        }
//
//        // Check if customer name already exists
//        if (isCustomerNameExists(name)) {
//            name.requestFocus();
//            return;
//        }
//
//        // Check if NIC already exists
//        if (isNICExists(nic)) {
//            nic.requestFocus();
//            return;
//        }
//
//        // Validate amounts
//        if (!isValidAmount(amountDue)) {
//            amountDue.requestFocus();
//            return;
//        }
//
//        // Validate credit price (optional)
//        if (!creditPrice.isEmpty() && !isValidAmount(creditPrice)) {
//            jTextField5.requestFocus();
//            return;
//        }
//
//        // Check if final date is after credit given date
//        if (finalDate.before(creditGivenDate)) {
//            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
//                    "Final date must be after credit given date!");
//            finelDate.requestFocus();
//            return;
//        }
//
//        try {
//            Connection conn = MySQL.getConnection();
//            String sql = "INSERT INTO credit_customer (customer_name, customer_phone_no, customer_address, customer_amount_due, credit_price, credit_date, final_date, nic, status_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//            PreparedStatement pst = conn.prepareStatement(sql);
//            pst.setString(1, name);
//            pst.setString(2, mobile);
//            pst.setString(3, address);
//            pst.setDouble(4, Double.parseDouble(amountDue));
//
//            // Handle optional credit price
//            if (creditPrice.isEmpty()) {
//                pst.setNull(5, java.sql.Types.DOUBLE);
//            } else {
//                pst.setDouble(5, Double.parseDouble(creditPrice));
//            }
//
//            pst.setDate(6, new java.sql.Date(creditGivenDate.getTime()));
//            pst.setDate(7, new java.sql.Date(finalDate.getTime()));
//            pst.setString(8, nic.trim().toUpperCase()); // Store NIC in uppercase
//            pst.setInt(9, 1); // Default status_id (assuming 1 = Active)
//
//            int rowsAffected = pst.executeUpdate();
//
//            if (rowsAffected > 0) {
//                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
//                        "Customer added successfully!");
//                clearFields();
//                this.dispose(); // Close the dialog after successful save
//            } else {
//                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
//                        "Failed to add customer!");
//            }
//
//            pst.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
//                    "Database error: " + e.getMessage());
//        }
//    }
//
//    private void clearForm() {
//        clearFields();
//        name.requestFocus();
//        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Form cleared!");
//    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        name = new javax.swing.JTextField();
        phoneNo = new javax.swing.JTextField();
        address = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        nic = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        creditBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

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

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(8, 147, 176));
        jLabel2.setText("Add New Customer");

        jSeparator2.setForeground(new java.awt.Color(0, 137, 176));

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
        saveBtn.setText("Save");
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
        creditBtn.setText("Save");
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
                        .addComponent(jLabel2)
                        .addGap(67, 67, 67)
                        .addComponent(creditBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSeparator2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(phoneNo, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(creditBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void nicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nicActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
  
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed

    }//GEN-LAST:event_cancelBtnKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed

    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed

    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed

    }//GEN-LAST:event_saveBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed

    }//GEN-LAST:event_saveBtnKeyPressed

    private void creditBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_creditBtnActionPerformed

    private void creditBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_creditBtnKeyPressed
        // TODO add your handling code here:
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
    private javax.swing.JTextField address;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JButton creditBtn;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField name;
    private javax.swing.JTextField nic;
    private javax.swing.JTextField phoneNo;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
