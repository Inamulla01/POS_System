package lk.com.pos.dialog;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import lk.com.pos.connection.MySQL;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class UpdateCredit extends javax.swing.JDialog {

    private int creditId; // Credit ID passed from calling dialog
    private boolean isUpdating = false; // Flag to prevent multiple updates
    private Map<String, Integer> customerIdMap = new HashMap<>();

    /**
     * Creates new form UpdateCredit
     */
    public UpdateCredit(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    // Constructor with credit ID
    public UpdateCredit(java.awt.Frame parent, boolean modal, int creditId) {
        super(parent, modal);
        this.creditId = creditId;
        initComponents();
        initializeDialog();
        loadCreditData();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());

        // Load customer combo data FIRST
        loadCustomerCombo();
        AutoCompleteDecorator.decorate(SupplierCombo);

        // THEN setup keyboard navigation and focus
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();
        setupFocusTraversal();
        setupUpdateButton();

        // Add mouse listeners to combo box
        SupplierCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SupplierCombo.showPopup();
            }
        });

        // Add F1 and F2 shortcuts
        getRootPane().registerKeyboardAction(
                evt -> openCreditPayDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> openAddNewCustomer(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set up Ctrl+Enter to update from anywhere
        getRootPane().registerKeyboardAction(
                evt -> updateCredit(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set initial focus
        SupplierCombo.requestFocusInWindow();
    }

    // ---------------- PREVENT DUPLICATE UPDATES ----------------
    private void setupUpdateButton() {
        // Remove all existing action listeners
        for (java.awt.event.ActionListener al : updateBtn.getActionListeners()) {
            updateBtn.removeActionListener(al);
        }

        // Add only one action listener with protection
        updateBtn.addActionListener(evt -> {
            if (!isUpdating) {
                updateCredit();
            }
        });
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupKeyboardNavigation() {
        // Set up Enter key and arrow key navigation between fields
        SupplierCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    manufactureDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    manufactureDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    updateBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, SupplierCombo);
                }
            }
        });

        // Manufacture date keyboard navigation
        javax.swing.JTextField manufactureDateEditor = (javax.swing.JTextField) manufactureDate.getDateEditor().getUiComponent();
        manufactureDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    expriyDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    SupplierCombo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    expriyDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    SupplierCombo.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, manufactureDateEditor);
                }
            }
        });

        // Expiry date keyboard navigation
        javax.swing.JTextField expriyDateEditor = (javax.swing.JTextField) expriyDate.getDateEditor().getUiComponent();
        expriyDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    manufactureDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    manufactureDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, expriyDateEditor);
                }
            }
        });

        // Amount field keyboard navigation
        address.addKeyListener(new java.awt.event.KeyAdapter() {
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
                    expriyDate.getDateEditor().getUiComponent().requestFocus();
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
                    expriyDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, address);
                }
            }
        });

        // Update button keyboard navigation - ONLY handle navigation keys, NOT ENTER key
        updateBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                // Only handle navigation keys, let actionPerformed handle ENTER
                if (evt.getKeyCode() != KeyEvent.VK_ENTER) {
                    handleArrowNavigation(evt, updateBtn);
                }
            }
        });

        // Clear form button keyboard navigation
        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearForm();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    updateBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, clearFormBtn);
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
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    updateBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    clearFormBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    updateBtn.requestFocus();
                    evt.consume();
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
        if (source == SupplierCombo) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            address.requestFocusInWindow();
        } else if (source == address) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            updateBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            SupplierCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == SupplierCombo) {
            updateBtn.requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            SupplierCombo.requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == address) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            address.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == SupplierCombo) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            address.requestFocusInWindow();
        } else if (source == address) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            updateBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            SupplierCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == SupplierCombo) {
            updateBtn.requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            SupplierCombo.requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == address) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            address.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return SupplierCombo.getSelectedIndex() > 0
                && manufactureDate.getDate() != null
                && expriyDate.getDate() != null
                && !address.getText().trim().isEmpty()
                && !address.getText().trim().equals("0");
    }

    // ---------------- BUTTON STYLES AND EFFECTS ----------------
    private void setupButtonStyles() {
        // Setup gradient buttons for Update, Clear Form, and Cancel
        setupGradientButton(updateBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        updateBtn.setIcon(updateIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        // Setup credit pay button with the same style as add buttons
        creditPayBtn.setBorderPainted(false);
        creditPayBtn.setContentAreaFilled(false);
        creditPayBtn.setFocusPainted(false);
        creditPayBtn.setOpaque(false);
        creditPayBtn.setFocusable(false);
        creditPayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon creditIcon = new FlatSVGIcon("lk/com/pos/icon/money-add.svg", 25, 25);
        creditIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        creditPayBtn.setIcon(creditIcon);

        // Setup add new customer button with the same style as add buttons
        addNewCustomer.setBorderPainted(false);
        addNewCustomer.setContentAreaFilled(false);
        addNewCustomer.setFocusPainted(false);
        addNewCustomer.setOpaque(false);
        addNewCustomer.setFocusable(false);
        addNewCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon customerIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
        customerIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewCustomer.setIcon(customerIcon);

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

        // Mouse listeners for creditPayBtn
        creditPayBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/money-add.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                creditPayBtn.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/money-add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                creditPayBtn.setIcon(normalIcon);
            }
        });

        // Mouse listeners for addNewCustomer
        addNewCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addNewCustomer.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                addNewCustomer.setIcon(normalIcon);
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

        // Focus listeners for creditPayBtn
        creditPayBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/money-add.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                creditPayBtn.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/money-add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                creditPayBtn.setIcon(normalIcon);
            }
        });

        // Focus listeners for addNewCustomer
        addNewCustomer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addNewCustomer.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                addNewCustomer.setIcon(normalIcon);
            }
        });
    }

    private void setupTooltips() {
        SupplierCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field<br>Press <b>F2</b> to add new customer</html>");
        manufactureDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 01012024 for 01/01/2024</html>");
        expriyDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 31122024 for 31/12/2024</html>");
        address.setToolTipText("Type credit amount and press ENTER to move to next field");
        creditPayBtn.setToolTipText("Click to open credit payment dialog (or press F1)");
        addNewCustomer.setToolTipText("Click to add new customer (or press F2)");
        updateBtn.setToolTipText("Click to update credit (or press ENTER when focused)");
        clearFormBtn.setToolTipText("Click to reload original data (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
    }

    private void loadCustomerCombo() {
        try {
            customerIdMap.clear();

            // CORRECTED QUERY - Join credit_pay using credit_customer_id instead of credit_id
            String sql = "SELECT cc.customer_id, cc.customer_name, "
                    + "COALESCE(SUM(c.credit_amout), 0) as total_credit, "
                    + "COALESCE(SUM(cp.credit_pay_amount), 0) as paid_amount, "
                    + "(COALESCE(SUM(c.credit_amout), 0) - COALESCE(SUM(cp.credit_pay_amount), 0)) as due_amount "
                    + "FROM credit_customer cc "
                    + "LEFT JOIN credit c ON cc.customer_id = c.credit_customer_id "
                    + "LEFT JOIN credit_pay cp ON cc.customer_id = cp.credit_customer_id "  // FIXED: Join on customer_id
                    + "WHERE cc.status_id = 1 "
                    + "GROUP BY cc.customer_id, cc.customer_name "
                    + "ORDER BY cc.customer_name";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> customers = new Vector<>();
            customers.add("Select Customer");

            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                double totalCredit = rs.getDouble("total_credit");
                double paidAmount = rs.getDouble("paid_amount");
                double dueAmount = rs.getDouble("due_amount");

                // Updated display - customer name with total credit, paid amount, and due amount
                String displayText = String.format("%s - Total: %.2f, Paid: %.2f, Due: %.2f",
                        customerName, totalCredit, paidAmount, dueAmount);
                customers.add(displayText);

                // Store mapping for later retrieval
                customerIdMap.put(displayText, customerId);
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(customers);
            SupplierCombo.setModel(dcm);

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading customers: " + e.getMessage());
            e.printStackTrace(); // Add this for debugging
        }
    }

    private void loadCreditData() {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT c.*, cc.customer_name FROM credit c "
                    + "JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id "
                    + "WHERE c.credit_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, creditId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Store credit_customer_id from the record
                int customerId = rs.getInt("credit_customer_id");
                String customerName = rs.getString("customer_name");

                // Get current total credit, paid amount, and due amount for this customer to create display text
                String creditQuery = "SELECT "
                        + "COALESCE(SUM(c.credit_amout), 0) as total_credit, "
                        + "COALESCE(SUM(cp.credit_pay_amount), 0) as paid_amount, "
                        + "(COALESCE(SUM(c.credit_amout), 0) - COALESCE(SUM(cp.credit_pay_amount), 0)) as due_amount "
                        + "FROM credit c "
                        + "LEFT JOIN credit_pay cp ON c.credit_customer_id = cp.credit_customer_id "  // FIXED: Join on customer_id
                        + "WHERE c.credit_customer_id = ?";
                PreparedStatement creditPst = conn.prepareStatement(creditQuery);
                creditPst.setInt(1, customerId);
                ResultSet creditRs = creditPst.executeQuery();

                double totalCredit = 0;
                double paidAmount = 0;
                double dueAmount = 0;

                if (creditRs.next()) {
                    totalCredit = creditRs.getDouble("total_credit");
                    paidAmount = creditRs.getDouble("paid_amount");
                    dueAmount = creditRs.getDouble("due_amount");
                }

                String displayText = String.format("%s - Total: %.2f, Paid: %.2f, Due: %.2f",
                        customerName, totalCredit, paidAmount, dueAmount);
                SupplierCombo.setSelectedItem(displayText);

                creditRs.close();
                creditPst.close();

                // Set dates
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                Date givenDate = rs.getDate("credit_given_date");
                Date finalDate = rs.getDate("credit_final_date");

                manufactureDate.setDate(givenDate);
                expriyDate.setDate(finalDate);

                // Set amount
                double amount = rs.getDouble("credit_amout");
                address.setText(String.valueOf(amount));
            }

            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading credit data: " + e.getMessage());
        }
    }

    private int getCustomerId(String displayText) {
        if (displayText == null || displayText.equals("Select Customer")) {
            return -1;
        }

        // Use the map to get customer ID
        Integer customerId = customerIdMap.get(displayText);
        return customerId != null ? customerId : -1;
    }

    private boolean validateInputs() {
        if (SupplierCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a customer");
            SupplierCombo.requestFocus();
            return false;
        }

        if (manufactureDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select credit given date");
            manufactureDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        if (expriyDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select credit final date");
            expriyDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        if (expriyDate.getDate().before(manufactureDate.getDate())) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Final date must be after given date");
            expriyDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(address.getText().trim());
            if (amount <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Credit amount must be greater than 0");
                address.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid credit amount");
            address.requestFocus();
            return false;
        }

        return true;
    }

    private void updateCredit() {
        // Prevent multiple simultaneous updates
        if (isUpdating) {
            return;
        }
        isUpdating = true;

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pst = null;

        try {
            if (!validateInputs()) {
                isUpdating = false;
                return;
            }

            String selectedDisplayText = (String) SupplierCombo.getSelectedItem();
            int customerId = getCustomerId(selectedDisplayText);
            if (customerId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid customer selected");
                isUpdating = false;
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String givenDateStr = dateFormat.format(manufactureDate.getDate());
            String finalDateStr = dateFormat.format(expriyDate.getDate());

            double amount = Double.parseDouble(address.getText().trim());

            // Get database connection
            conn = MySQL.getConnection();

            // Start transaction
            conn.setAutoCommit(false);

            // First, get the old credit data for comparison
            String oldDataSql = "SELECT cc.customer_name, c.credit_given_date, c.credit_final_date, c.credit_amout "
                    + "FROM credit c "
                    + "JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id "
                    + "WHERE c.credit_id = ?";
            java.sql.PreparedStatement pstOld = conn.prepareStatement(oldDataSql);
            pstOld.setInt(1, creditId);
            java.sql.ResultSet rs = pstOld.executeQuery();

            String oldCustomerName = "";
            String oldGivenDate = "";
            String oldFinalDate = "";
            double oldAmount = 0.0;

            if (rs.next()) {
                oldCustomerName = rs.getString("customer_name");
                oldGivenDate = rs.getString("credit_given_date");
                oldFinalDate = rs.getString("credit_final_date");
                oldAmount = rs.getDouble("credit_amout");
            }
            pstOld.close();

            // Extract new customer name from display text
            String newCustomerName = selectedDisplayText.split(" - ")[0].trim();

            // Update the credit record - only update customer, dates, and amount (NOT sales_id)
            String query = "UPDATE credit SET credit_given_date = ?, credit_final_date = ?, credit_amout = ?, credit_customer_id = ? "
                    + "WHERE credit_id = ?";

            pst = conn.prepareStatement(query);
            pst.setString(1, givenDateStr);
            pst.setString(2, finalDateStr);
            pst.setDouble(3, amount);
            pst.setInt(4, customerId);
            pst.setInt(5, creditId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                // Create notification for credit update
                createCreditUpdateNotification(oldCustomerName, newCustomerName, oldGivenDate, givenDateStr,
                        oldFinalDate, finalDateStr, oldAmount, amount, conn);

                // Commit transaction
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Credit updated successfully!");
                dispose(); // Close the dialog after successful update
            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to update credit!");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error updating credit: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (pst != null) {
                    pst.close();
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

    private void createCreditUpdateNotification(String oldCustomerName, String newCustomerName,
            String oldGivenDate, String newGivenDate,
            String oldFinalDate, String newFinalDate,
            double oldAmount, double newAmount,
            java.sql.Connection conn) {
        java.sql.PreparedStatement pstMassage = null;
        java.sql.PreparedStatement pstNotification = null;

        try {
            // Build detailed change message
            StringBuilder changes = new StringBuilder();

            // Check what was changed
            if (!oldCustomerName.equals(newCustomerName)) {
                changes.append("Customer: ").append(oldCustomerName).append(" → ").append(newCustomerName).append("; ");
            }
            if (!oldGivenDate.equals(newGivenDate)) {
                changes.append("Given Date: ").append(oldGivenDate).append(" → ").append(newGivenDate).append("; ");
            }
            if (!oldFinalDate.equals(newFinalDate)) {
                changes.append("Final Date: ").append(oldFinalDate).append(" → ").append(newFinalDate).append("; ");
            }
            if (oldAmount != newAmount) {
                changes.append(String.format("Amount: Rs.%,.2f → Rs.%,.2f", oldAmount, newAmount)).append("; ");
            }

            // Remove trailing semicolon and space if present
            if (changes.length() > 0) {
                changes.setLength(changes.length() - 2);
            }

            String messageText;
            if (changes.length() > 0) {
                messageText = String.format("Credit updated for %s: %s", newCustomerName, changes.toString());
            } else {
                messageText = String.format("Credit updated for %s: No changes detected", newCustomerName);
            }

            // Check if this exact message already exists to avoid duplicates
            String checkSql = "SELECT COUNT(*) FROM massage WHERE massage = ?";
            pstMassage = conn.prepareStatement(checkSql);
            pstMassage.setString(1, messageText);
            java.sql.ResultSet rs = pstMassage.executeQuery();

            int massageId;
            if (rs.next() && rs.getInt(1) > 0) {
                // Message already exists, get its ID
                String getSql = "SELECT massage_id FROM massage WHERE massage = ?";
                pstMassage.close();
                pstMassage = conn.prepareStatement(getSql);
                pstMassage.setString(1, messageText);
                rs = pstMassage.executeQuery();
                rs.next();
                massageId = rs.getInt(1);
            } else {
                // Insert new message
                pstMassage.close();
                String insertMassageSql = "INSERT INTO massage (massage) VALUES (?)";
                pstMassage = conn.prepareStatement(insertMassageSql, java.sql.PreparedStatement.RETURN_GENERATED_KEYS);
                pstMassage.setString(1, messageText);
                pstMassage.executeUpdate();

                // Get the generated massage_id
                rs = pstMassage.getGeneratedKeys();
                if (rs.next()) {
                    massageId = rs.getInt(1);
                } else {
                    throw new java.sql.SQLException("Failed to get generated massage ID");
                }
            }

            // Insert notification (msg_type_id 17 = 'Edit Credit' from your msg_type table)
            String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)";
            pstNotification = conn.prepareStatement(notificationSql);
            pstNotification.setInt(1, 1); // is_read = 1 (unread)
            pstNotification.setInt(2, 17); // msg_type_id 17 = 'Edit Credit'
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

            System.out.println("Credit update notification created successfully for: " + newCustomerName);

        } catch (Exception e) {
            e.printStackTrace();
            // Don't throw exception here - we don't want notification failure to affect credit update
            System.err.println("Failed to create credit update notification: " + e.getMessage());
        } finally {
            // Close resources
            try {
                if (pstMassage != null) {
                    pstMassage.close();
                }
                if (pstNotification != null) {
                    pstNotification.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        loadCreditData(); // Reload original data
        SupplierCombo.requestFocus();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Form reset to original values!");
    }

    private void openCreditPayDialog() {
        try {
         
            AddCreditPay dialog = new AddCreditPay(null, true);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening credit payment dialog: " + e.getMessage());
        }
    }

    private void openAddNewCustomer() {
        try {
            
            AddNewCustomer dialog = new AddNewCustomer(null, true);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening customer dialog: " + e.getMessage());
        }
    }

    // ---------------- FOCUS TRAVERSAL SETUP ----------------
    private void setupFocusTraversal() {
        try {
            // Remove add buttons from keyboard navigation
            creditPayBtn.setFocusable(false);
            addNewCustomer.setFocusable(false);

            // Make date editors focusable
            if (manufactureDate.getDateEditor() != null && manufactureDate.getDateEditor().getUiComponent() != null) {
                manufactureDate.getDateEditor().getUiComponent().setFocusable(true);
            }
            if (expriyDate.getDateEditor() != null && expriyDate.getDateEditor().getUiComponent() != null) {
                expriyDate.getDateEditor().getUiComponent().setFocusable(true);
            }
        } catch (Exception e) {
            System.err.println("Error in focus traversal setup: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        address = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        cancelBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        updateBtn = new javax.swing.JButton();
        creditPayBtn = new javax.swing.JButton();
        manufactureDate = new com.toedter.calendar.JDateChooser();
        expriyDate = new com.toedter.calendar.JDateChooser();
        SupplierCombo = new javax.swing.JComboBox<>();
        addNewCustomer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Credit");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        address.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        address.setText("0");
        address.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Edit Credit");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

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

        updateBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        updateBtn.setForeground(new java.awt.Color(8, 147, 176));
        updateBtn.setText("Update");
        updateBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });
        updateBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                updateBtnKeyPressed(evt);
            }
        });

        creditPayBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        creditPayBtn.setForeground(new java.awt.Color(8, 147, 176));
        creditPayBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        creditPayBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creditPayBtnActionPerformed(evt);
            }
        });
        creditPayBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                creditPayBtnKeyPressed(evt);
            }
        });

        manufactureDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Given Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        manufactureDate.setDateFormatString("MM/dd/yyyy");
        manufactureDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        manufactureDate.setOpaque(false);
        manufactureDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                manufactureDateKeyPressed(evt);
            }
        });

        expriyDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Final Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        expriyDate.setDateFormatString("MM/dd/yyyy");
        expriyDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        expriyDate.setOpaque(false);
        expriyDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                expriyDateKeyPressed(evt);
            }
        });

        SupplierCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        SupplierCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        SupplierCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customer *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        SupplierCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SupplierComboActionPerformed(evt);
            }
        });
        SupplierCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SupplierComboKeyPressed(evt);
            }
        });

        addNewCustomer.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewCustomer.setForeground(new java.awt.Color(102, 102, 102));
        addNewCustomer.setBorder(null);
        addNewCustomer.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addNewCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewCustomerActionPerformed(evt);
            }
        });
        addNewCustomer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                addNewCustomerKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(SupplierCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addNewCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(creditPayBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator3)
                    .addComponent(address, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(21, 21, 21))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(creditPayBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SupplierCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(addNewCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed
        // When Enter is pressed in amount field, move to next field
        if (areAllRequiredFieldsFilled()) {
            updateBtn.requestFocusInWindow();
        } else {
            cancelBtn.requestFocusInWindow();
        }
    }//GEN-LAST:event_addressActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        } else {
            handleArrowNavigation(evt, cancelBtn);
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        } else {
            handleArrowNavigation(evt, clearFormBtn);
        }
    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        updateCredit();

    }//GEN-LAST:event_updateBtnActionPerformed

    private void updateBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateBtnKeyPressed
        if (evt.getKeyCode() != KeyEvent.VK_ENTER) {
            handleArrowNavigation(evt, updateBtn);
        }
    }//GEN-LAST:event_updateBtnKeyPressed

    private void creditPayBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditPayBtnActionPerformed
        openCreditPayDialog();
    }//GEN-LAST:event_creditPayBtnActionPerformed

    private void creditPayBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_creditPayBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F1) {
            openCreditPayDialog();
        }
    }//GEN-LAST:event_creditPayBtnKeyPressed

    private void manufactureDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manufactureDateKeyPressed
        handleArrowNavigation(evt, manufactureDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_manufactureDateKeyPressed

    private void expriyDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_expriyDateKeyPressed
        handleArrowNavigation(evt, expriyDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_expriyDateKeyPressed

    private void SupplierComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SupplierComboActionPerformed
        if (SupplierCombo.getSelectedIndex() > 0 && !SupplierCombo.isPopupVisible()) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        }
    }//GEN-LAST:event_SupplierComboActionPerformed

    private void SupplierComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SupplierComboKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (SupplierCombo.isPopupVisible()) {
                SupplierCombo.setPopupVisible(false);
            }
            if (SupplierCombo.getSelectedIndex() > 0) {
                manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!SupplierCombo.isPopupVisible()) {
                SupplierCombo.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!SupplierCombo.isPopupVisible()) {
                updateBtn.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, SupplierCombo);
        }
    }//GEN-LAST:event_SupplierComboKeyPressed

    private void addNewCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCustomerActionPerformed
        openAddNewCustomer();
    }//GEN-LAST:event_addNewCustomerActionPerformed

    private void addNewCustomerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewCustomerKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F2) {
            openAddNewCustomer();
        }
    }//GEN-LAST:event_addNewCustomerKeyPressed

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
            java.util.logging.Logger.getLogger(UpdateCredit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateCredit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateCredit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateCredit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateCredit dialog = new UpdateCredit(new javax.swing.JFrame(), true,3);
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
    private javax.swing.JComboBox<String> SupplierCombo;
    private javax.swing.JButton addNewCustomer;
    private javax.swing.JTextField address;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JButton creditPayBtn;
    private com.toedter.calendar.JDateChooser expriyDate;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private com.toedter.calendar.JDateChooser manufactureDate;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
