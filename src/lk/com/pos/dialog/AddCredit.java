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
public class AddCredit extends javax.swing.JDialog {

    private int customerId = -1; // This will be returned to calling point
    private Double amount;
    private Map<String, Integer> customerIdMap = new HashMap<>(); // Map to store display text to customer ID mapping
    private boolean isSaving = false; // Add this flag to prevent duplicate saves

    /**
     * Creates new form AddCreditPay
     */
    public AddCredit(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    // Constructor with amount only
    public AddCredit(java.awt.Frame parent, boolean modal, Double amount) {
        super(parent, modal);
        this.amount = amount;
        initComponents();
        initializeDialog(amount);
    }

    // Method to get the selected customer ID
    public int getSelectedCustomerId() {
        return customerId;
    }

    // Method to check if credit was saved successfully
    public boolean isCreditSaved() {
        return customerId != -1;
    }

    private void initializeDialog() {
        initializeDialog(null);
    }

    private void initializeDialog(Double amount) {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        // Load customer combo data
        loadCustomerCombo();
        AutoCompleteDecorator.decorate(SupplierCombo);

        // Set current date as default for given date
        manufactureDate.setDate(new Date());

        // Set focus traversal
        setupFocusTraversal();

        // Set amount if provided and not null
        if (amount != null) {
            address.setText(String.valueOf(amount));
        } else {
            address.setText("0");
        }

        // Add mouse listeners to combo box
        SupplierCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SupplierCombo.showPopup();
            }
        });

        // Add F1 and F2 shortcuts ONLY - Remove any Enter/Ctrl+Enter shortcuts
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

        // REMOVED: Ctrl+Enter shortcut to prevent duplicate saves
        // Set initial focus
        SupplierCombo.requestFocusInWindow();
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
                    saveBtn.requestFocus();
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
                    // When all fields are filled, go directly to save button
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    expriyDate.getDateEditor().getUiComponent().requestFocus();
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
                    expriyDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, address);
                }
            }
        });

        // Save button keyboard navigation - FIXED: Remove duplicate Enter key handling
        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Let the ActionListener handle the save, just consume the event
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    clearFormBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    clearFormBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    clearFormBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, saveBtn);
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
                    saveBtn.requestFocus();
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
                    saveBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    clearFormBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    saveBtn.requestFocus();
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
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            SupplierCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == SupplierCombo) {
            saveBtn.requestFocusInWindow();
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
        } else if (source == saveBtn) {
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
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            SupplierCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == SupplierCombo) {
            saveBtn.requestFocusInWindow();
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
        } else if (source == saveBtn) {
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
        // Setup gradient buttons for Save, Clear Form, and Cancel
        setupGradientButton(saveBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(saveIcon);

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
        saveBtn.setToolTipText("Click to save credit (or press ENTER when focused)");
        clearFormBtn.setToolTipText("Click to clear form (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
    }

    // ---------------- BUSINESS LOGIC ----------------
    private void loadCustomerCombo() {
        try {
            // Clear the mapping first
            customerIdMap.clear();

            // SIMPLIFIED QUERY - Load ALL customers from credit_customer table
            String sql = "SELECT customer_id, customer_name FROM credit_customer WHERE status_id = 1 ORDER BY customer_name";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> customers = new Vector<>();
            customers.add("Select Customer");

            int count = 0;
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");

                // Simple display - just customer name
                String displayText = customerName;

                customers.add(displayText);

                // Store the mapping between display text and customer ID
                customerIdMap.put(displayText, customerId);
                count++;
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(customers);
            SupplierCombo.setModel(dcm);

            // Debug output
            System.out.println("Successfully loaded " + count + " customers into selector");
            System.out.println("Combo box now has " + SupplierCombo.getItemCount() + " items");

        } catch (Exception e) {
            System.err.println("Error loading customers: " + e.getMessage());
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading customers: " + e.getMessage());
        }
    }

    private int getCustomerId(String displayText) {
        // Use the mapping to get the customer ID
        Integer customerId = customerIdMap.get(displayText);

        if (customerId == null) {
            System.err.println("Customer ID not found for: " + displayText);
            System.err.println("Available mappings: " + customerIdMap);
            return -1;
        }

        System.out.println("Found Customer ID: " + customerId + " for: " + displayText);
        return customerId;
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

    private void saveCredit() {
        // Prevent multiple simultaneous saves
        if (isSaving) {
            System.out.println("Save already in progress, skipping duplicate call...");
            return;
        }

        System.out.println("saveCredit() called at: " + new Date()); // Debug output

        if (!validateInputs()) {
            return;
        }

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            isSaving = true; // Set flag to prevent duplicate saves

            String selectedDisplayText = (String) SupplierCombo.getSelectedItem();
            this.customerId = getCustomerId(selectedDisplayText); // Set the customer ID to return
            if (this.customerId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid customer selected");
                isSaving = false; // Reset flag
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String givenDateStr = dateFormat.format(manufactureDate.getDate());
            String finalDateStr = dateFormat.format(expriyDate.getDate());

            double amount = Double.parseDouble(address.getText().trim());

            conn = MySQL.getConnection();

            // Start transaction
            conn.setAutoCommit(false);

            String query = "INSERT INTO credit (credit_given_date, credit_final_date, credit_amout, credit_customer_id) "
                    + "VALUES (?, ?, ?, ?)";

            pst = conn.prepareStatement(query);
            pst.setString(1, givenDateStr);
            pst.setString(2, finalDateStr);
            pst.setDouble(3, amount);
            pst.setInt(4, this.customerId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                // Create notification for new credit
                createCreditNotification(selectedDisplayText, amount, conn);

                // Commit transaction
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Credit added successfully!");
                dispose(); // Close the dialog after successful save
            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to add credit!");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving credit: " + e.getMessage());
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
            isSaving = false; // Always reset the flag
        }
    }

    private void createCreditNotification(String customerName, double amount, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            // Create the message
            String messageText = "New credit added for " + customerName + ": Rs." + String.format("%,.2f", amount);

            // Check if this exact message already exists to avoid duplicates
            String checkSql = "SELECT COUNT(*) FROM massage WHERE massage = ?";
            pstMassage = conn.prepareStatement(checkSql);
            pstMassage.setString(1, messageText);
            ResultSet rs = pstMassage.executeQuery();

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
                pstMassage = conn.prepareStatement(insertMassageSql, PreparedStatement.RETURN_GENERATED_KEYS);
                pstMassage.setString(1, messageText);
                pstMassage.executeUpdate();

                // Get the generated massage_id
                rs = pstMassage.getGeneratedKeys();
                if (rs.next()) {
                    massageId = rs.getInt(1);
                } else {
                    throw new Exception("Failed to get generated massage ID");
                }
            }

            // Insert notification (msg_type_id 11 = 'Add New Credit' from your msg_type table)
            String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)";
            pstNotification = conn.prepareStatement(notificationSql);
            pstNotification.setInt(1, 1); // is_read = 1 (unread)
            pstNotification.setInt(2, 11); // msg_type_id 11 = 'Add New Credit'
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

            System.out.println("Credit notification created successfully for customer: " + customerName);

        } catch (Exception e) {
            e.printStackTrace();
            // Don't throw exception here - we don't want notification failure to affect credit creation
            System.err.println("Failed to create credit notification: " + e.getMessage());
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
        SupplierCombo.setSelectedIndex(0);
        manufactureDate.setDate(new Date());
        expriyDate.setDate(null);
        address.setText("0");
        SupplierCombo.requestFocus();
    }

    private void openCreditPayDialog() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddCreditPay dialog = new AddCreditPay(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening credit payment dialog: " + e.getMessage());
        }
    }

    private void openAddNewCustomer() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddNewCustomer dialog = new AddNewCustomer(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            loadCustomerCombo(); // Reload customers after adding new one
            SupplierCombo.requestFocus();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening customer dialog: " + e.getMessage());
        }
    }

    // ---------------- FOCUS TRAVERSAL SETUP ----------------
    private void setupFocusTraversal() {
        // Remove add buttons from keyboard navigation
        creditPayBtn.setFocusable(false);
        addNewCustomer.setFocusable(false);

        // Make date editors focusable
        manufactureDate.getDateEditor().getUiComponent().setFocusable(true);
        expriyDate.getDateEditor().getUiComponent().setFocusable(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        cancelBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        creditPayBtn = new javax.swing.JButton();
        SupplierCombo = new javax.swing.JComboBox<>();
        manufactureDate = new com.toedter.calendar.JDateChooser();
        expriyDate = new com.toedter.calendar.JDateChooser();
        address = new javax.swing.JTextField();
        addNewCustomer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add Credit");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Add New Credit");

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

        address.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        address.setText("0");
        address.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressActionPerformed(evt);
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
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(creditPayBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(21, 21, 21))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(21, 21, 21)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(SupplierCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(addNewCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addComponent(manufactureDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(address, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addGap(21, 21, 21)))
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
                .addGap(198, 198, 198)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(77, 77, 77)
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
                    .addContainerGap(77, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void creditPayBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_creditPayBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F1) {
            openCreditPayDialog();
        }
    }//GEN-LAST:event_creditPayBtnKeyPressed

    private void creditPayBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditPayBtnActionPerformed
        openCreditPayDialog();
    }//GEN-LAST:event_creditPayBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume(); // Consume the event to prevent multiple triggers
            saveCredit();
        } else {
            handleArrowNavigation(evt, saveBtn);
        }
    }//GEN-LAST:event_saveBtnKeyPressed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        saveCredit();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        } else {
            handleArrowNavigation(evt, clearFormBtn);
        }
    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        } else {
            handleArrowNavigation(evt, cancelBtn);
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

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
                saveBtn.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, SupplierCombo);
        }
    }//GEN-LAST:event_SupplierComboKeyPressed

    private void manufactureDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manufactureDateKeyPressed
        handleArrowNavigation(evt, manufactureDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_manufactureDateKeyPressed

    private void expriyDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_expriyDateKeyPressed
        handleArrowNavigation(evt, expriyDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_expriyDateKeyPressed

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed

    }//GEN-LAST:event_addressActionPerformed

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
            java.util.logging.Logger.getLogger(AddCredit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddCredit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddCredit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddCredit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddCredit dialog = new AddCredit(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
