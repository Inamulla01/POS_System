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
public class AddCreditPay extends javax.swing.JDialog {

    private Integer creditId; // Credit ID passed from calling dialog (can be null)
    private double remainingAmount = 0.0;
    private boolean isSaving = false; // Flag to prevent multiple saves
    private Map<String, Integer> creditIdMap = new HashMap<>(); // Map to store display text to credit ID mapping

    /**
     * Creates new form AddCreditPay
     */
    public AddCreditPay(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    // Constructor with credit ID (when called from credit management)
    public AddCreditPay(java.awt.Frame parent, boolean modal, int creditId) {
        super(parent, modal);
        this.creditId = creditId;
        initComponents();
        initializeDialog();
        loadCreditData();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());

        // Load credit combo data FIRST
        loadCreditCombo();
        AutoCompleteDecorator.decorate(creditCombo);

        // Hide remaining amount label initially if no creditId is provided
        if (creditId == null) {
            remainingAmountLabel.setVisible(false);
        } else {
            // If creditId is provided, show the credit info but don't disable combo
            loadCreditData();
            remainingAmountLabel.setVisible(true);
        }

        // Set current date as default for payment date
        manufactureDate.setDate(new Date());

        // THEN setup keyboard navigation and focus
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();
        setupFocusTraversal();

        // Add mouse listeners to combo box (always enabled)
        creditCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                creditCombo.showPopup();
            }
        });

        // Add F1 and F2 shortcuts
        getRootPane().registerKeyboardAction(
                evt -> openAddNewCredit(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> refreshCredits(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set initial focus
        creditCombo.requestFocusInWindow();
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupKeyboardNavigation() {
        // Credit combo keyboard navigation (always enabled)
        creditCombo.addKeyListener(new java.awt.event.KeyAdapter() {
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
                    handleArrowNavigation(evt, creditCombo);
                }
            }
        });

        // Payment date keyboard navigation
        javax.swing.JTextField manufactureDateEditor = (javax.swing.JTextField) manufactureDate.getDateEditor().getUiComponent();
        manufactureDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    creditCombo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    address.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    creditCombo.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, manufactureDateEditor);
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
                    manufactureDate.getDateEditor().getUiComponent().requestFocus();
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
                    manufactureDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, address);
                }
            }
        });

        // Save button keyboard navigation - FIXED: Now includes ENTER key support
        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveCreditPay();
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
        if (source == creditCombo) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            address.requestFocusInWindow();
        } else if (source == address) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            creditCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == creditCombo) {
            saveBtn.requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            creditCombo.requestFocusInWindow();
        } else if (source == address) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            address.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == creditCombo) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            address.requestFocusInWindow();
        } else if (source == address) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            creditCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == creditCombo) {
            saveBtn.requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            creditCombo.requestFocusInWindow();
        } else if (source == address) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            address.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return creditCombo.getSelectedIndex() > 0
                && manufactureDate.getDate() != null
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

        // Setup add new credit button with the same style as add buttons
        addNewCredit.setBorderPainted(false);
        addNewCredit.setContentAreaFilled(false);
        addNewCredit.setFocusPainted(false);
        addNewCredit.setOpaque(false);
        addNewCredit.setFocusable(false);
        addNewCredit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon creditIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 25, 25);
        creditIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewCredit.setIcon(creditIcon);

        // Setup mouse listeners for all buttons
        setupButtonMouseListeners();
        setupButtonFocusListeners();

        // Fix save button to prevent duplicate saves
        setupSaveButton();
    }

    private void setupSaveButton() {
        // Remove all existing action listeners
        for (java.awt.event.ActionListener al : saveBtn.getActionListeners()) {
            saveBtn.removeActionListener(al);
        }

        // Add only one action listener
        saveBtn.addActionListener(evt -> {
            if (!isSaving) {
                saveCreditPay();
            }
        });
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

        // Mouse listeners for addNewCredit
        addNewCredit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addNewCredit.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                addNewCredit.setIcon(normalIcon);
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

        // Focus listeners for addNewCredit
        addNewCredit.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addNewCredit.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                addNewCredit.setIcon(normalIcon);
            }
        });
    }

    private void setupTooltips() {
        creditCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field<br>Press <b>F2</b> to refresh credit list</html>");
        manufactureDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 01012024 for 01/01/2024</html>");
        address.setToolTipText("Type payment amount and press ENTER to move to next field");
        addNewCredit.setToolTipText("Click to add new credit (or press F1)");
        saveBtn.setToolTipText("Click to save credit payment (or press ENTER when focused)");
        clearFormBtn.setToolTipText("Click to clear form (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
    }

    // ---------------- BUSINESS LOGIC ----------------
    private void loadCreditCombo() {
        try {
            // Clear the mapping first
            creditIdMap.clear();

            // Load ALL credits (including fully paid ones)
            String sql = "SELECT c.credit_id, cc.customer_name, c.credit_amout, "
                    + "COALESCE(SUM(cp.credit_pay_amount), 0) as paid_amount, "
                    + "(c.credit_amout - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount "
                    + "FROM credit c "
                    + "JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id "
                    + "LEFT JOIN credit_pay cp ON c.credit_id = cp.credit_id "
                    + "GROUP BY c.credit_id, cc.customer_name, c.credit_amout "
                    + "ORDER BY c.credit_given_date DESC";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> credits = new Vector<>();
            credits.add("Select Credit");
            while (rs.next()) {
                int creditId = rs.getInt("credit_id");
                String customerName = rs.getString("customer_name");
                double creditAmount = rs.getDouble("credit_amout");
                double paidAmount = rs.getDouble("paid_amount");
                double remaining = rs.getDouble("remaining_amount");

                String displayText = String.format("%s - Total: %.2f, Paid: %.2f, Due: %.2f",
                        customerName, creditAmount, paidAmount, remaining);
                credits.add(displayText);

                // Store the mapping between display text and credit ID
                creditIdMap.put(displayText, creditId);
            }
            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(credits);
            creditCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading credits: " + e.getMessage());
        }
    }

    private void loadCreditInfo() {
        try {
            String sql = "SELECT c.credit_id, cc.customer_name, c.credit_amout, "
                    + "COALESCE(SUM(cp.credit_pay_amount), 0) as paid_amount, "
                    + "(c.credit_amout - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount "
                    + "FROM credit c "
                    + "JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id "
                    + "LEFT JOIN credit_pay cp ON c.credit_id = cp.credit_id "
                    + "WHERE c.credit_id = ? "
                    + "GROUP BY c.credit_id, cc.customer_name, c.credit_amout";

            Connection conn = MySQL.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, creditId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String customerName = rs.getString("customer_name");
                double creditAmount = rs.getDouble("credit_amout");
                double paidAmount = rs.getDouble("paid_amount");
                remainingAmount = rs.getDouble("remaining_amount");

                String displayText = String.format("%s - Total: %.2f, Paid: %.2f, Due: %.2f",
                        customerName, creditAmount, paidAmount, remainingAmount);

                // Find and select this credit in the combo box
                for (int i = 0; i < creditCombo.getItemCount(); i++) {
                    if (creditCombo.getItemAt(i).equals(displayText)) {
                        creditCombo.setSelectedIndex(i);
                        break;
                    }
                }

                // Update remaining amount label
                updateRemainingAmountLabel();
            }

            rs.close();
            pst.close();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading credit info: " + e.getMessage());
        }
    }

    private void updateRemainingAmountLabel() {
        remainingAmountLabel.setText(String.format("Remaining Amount: %.2f", remainingAmount));
        address.setToolTipText("Type payment amount (max: " + remainingAmount + ") and press ENTER to move to next field");
    }

    private void loadCreditData() {
        // This method is called when creditId is provided
        loadCreditInfo();
    }

    private int getSelectedCreditId() {
        String selected = (String) creditCombo.getSelectedItem();
        if (selected == null || selected.equals("Select Credit")) {
            return -1;
        }

        // Use the mapping to get the credit ID
        Integer creditId = creditIdMap.get(selected);
        return creditId != null ? creditId : -1;
    }

    private boolean validateInputs() {
        // Validate credit selection
        if (creditCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a credit");
            creditCombo.requestFocus();
            return false;
        }

        // Validate payment date
        if (manufactureDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select payment date");
            manufactureDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        // Validate amount
        String amountText = address.getText().trim();
        if (amountText.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter payment amount");
            address.requestFocus();
            return false;
        }

        // Check if amount is numeric
        if (!amountText.matches("^[0-9]*\\.?[0-9]+$")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid payment amount");
            address.requestFocus();
            return false;
        }

        double amount = Double.parseDouble(amountText);

        if (amount <= 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Payment amount must be greater than 0");
            address.requestFocus();
            return false;
        }

        // Check if payment amount exceeds remaining amount
        if (amount > remainingAmount) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    String.format("Payment amount (%.2f) cannot exceed remaining amount: %.2f", amount, remainingAmount));
            address.requestFocus();
            return false;
        }

        return true;
    }

    private void saveCreditPay() {
        // Prevent multiple simultaneous saves
        if (isSaving) {
            return;
        }

        isSaving = true;

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            if (!validateInputs()) {
                isSaving = false;
                return;
            }

            int selectedCreditId = getSelectedCreditId();
            if (selectedCreditId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid credit selected");
                isSaving = false;
                return;
            }

            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String paymentDateStr = datetimeFormat.format(manufactureDate.getDate());

            double amount = Double.parseDouble(address.getText().trim());

            conn = MySQL.getConnection();

            // Start transaction
            conn.setAutoCommit(false);

            String query = "INSERT INTO credit_pay (credit_pay_date, credit_pay_amount, credit_id) VALUES (?, ?, ?)";

            pst = conn.prepareStatement(query);
            pst.setString(1, paymentDateStr);
            pst.setDouble(2, amount);
            pst.setInt(3, selectedCreditId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                // Create notification for credit payment
                createCreditPaymentNotification(selectedCreditId, amount, conn);

                // Commit transaction
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Credit payment added successfully!");
                dispose(); // Close the dialog after successful save
            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to add credit payment!");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving credit payment: " + e.getMessage());
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
            isSaving = false;
        }
    }

    private void createCreditPaymentNotification(int creditId, double amount, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;
        PreparedStatement pstCreditInfo = null;

        try {
            // First, get customer name and credit details for the notification message
            String creditInfoSql = "SELECT cc.customer_name, c.credit_amout, "
                    + "COALESCE(SUM(cp.credit_pay_amount), 0) as total_paid "
                    + "FROM credit c "
                    + "JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id "
                    + "LEFT JOIN credit_pay cp ON c.credit_id = cp.credit_id "
                    + "WHERE c.credit_id = ? "
                    + "GROUP BY cc.customer_name, c.credit_amout";

            pstCreditInfo = conn.prepareStatement(creditInfoSql);
            pstCreditInfo.setInt(1, creditId);
            ResultSet rs = pstCreditInfo.executeQuery();

            String customerName = "Unknown Customer";
            double totalCredit = 0.0;
            double totalPaid = 0.0;

            if (rs.next()) {
                customerName = rs.getString("customer_name");
                totalCredit = rs.getDouble("credit_amout");
                totalPaid = rs.getDouble("total_paid");
            }

            // Create the message
            String messageText = String.format("Credit payment received from %s: Rs.%,.2f (Total Credit: Rs.%,.2f, Remaining: Rs.%,.2f)",
                    customerName, amount, totalCredit, (totalCredit - totalPaid));

            // Check if this exact message already exists to avoid duplicates
            String checkSql = "SELECT COUNT(*) FROM massage WHERE massage = ?";
            pstMassage = conn.prepareStatement(checkSql);
            pstMassage.setString(1, messageText);
            rs = pstMassage.executeQuery();

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

            // Insert notification (msg_type_id 12 = 'Credit Payed' from your msg_type table)
            String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)";
            pstNotification = conn.prepareStatement(notificationSql);
            pstNotification.setInt(1, 1); // is_read = 1 (unread)
            pstNotification.setInt(2, 12); // msg_type_id 12 = 'Credit Payed'
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

            System.out.println("Credit payment notification created successfully for customer: " + customerName);

        } catch (Exception e) {
            e.printStackTrace();
            // Don't throw exception here - we don't want notification failure to affect credit payment creation
            System.err.println("Failed to create credit payment notification: " + e.getMessage());
        } finally {
            // Close resources
            try {
                if (pstCreditInfo != null) {
                    pstCreditInfo.close();
                }
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
        creditCombo.setSelectedIndex(0);
        manufactureDate.setDate(new Date());
        address.setText("");
        remainingAmountLabel.setText("Remaining Amount: 0.00");
        remainingAmount = 0.0;
        // Hide the label when clearing form if no creditId was provided
        if (creditId == null) {
            remainingAmountLabel.setVisible(false);
        }
        creditCombo.requestFocus();
    }

    private void openAddNewCredit() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddCredit dialog = new AddCredit(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            loadCreditCombo();
            creditCombo.requestFocus();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening credit dialog: " + e.getMessage());
        }
    }

    private void refreshCredits() {
        loadCreditCombo();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Credit list refreshed!");
        creditCombo.requestFocus();
    }

    // ---------------- FOCUS TRAVERSAL SETUP ----------------
    private void setupFocusTraversal() {
        try {
            // Remove add button from keyboard navigation
            addNewCredit.setFocusable(false);

            // Make date editor focusable
            if (manufactureDate.getDateEditor() != null && manufactureDate.getDateEditor().getUiComponent() != null) {
                manufactureDate.getDateEditor().getUiComponent().setFocusable(true);
            }
        } catch (Exception e) {
            System.err.println("Error in focus traversal setup: " + e.getMessage());
        }
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
        creditCombo = new javax.swing.JComboBox<>();
        manufactureDate = new com.toedter.calendar.JDateChooser();
        address = new javax.swing.JTextField();
        addNewCredit = new javax.swing.JButton();
        remainingAmountLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New Credit Payment");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Add New Credit Payment");

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
        clearFormBtn.setText("Clear");
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

        creditCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        creditCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        creditCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Credit *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        creditCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creditComboActionPerformed(evt);
            }
        });
        creditCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                creditComboKeyPressed(evt);
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

        address.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        address.setText("0");
        address.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressActionPerformed(evt);
            }
        });

        addNewCredit.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewCredit.setForeground(new java.awt.Color(102, 102, 102));
        addNewCredit.setBorder(null);
        addNewCredit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addNewCredit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewCreditActionPerformed(evt);
            }
        });
        addNewCredit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                addNewCreditKeyPressed(evt);
            }
        });

        remainingAmountLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 18)); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel3)
                            .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(address)))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(remainingAmountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(creditCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(addNewCredit, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 21, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remainingAmountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(creditCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewCredit, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        saveCreditPay();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        handleArrowNavigation(evt, saveBtn);
    }//GEN-LAST:event_saveBtnKeyPressed

    private void creditComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditComboActionPerformed
        if (creditCombo.getSelectedIndex() > 0 && !creditCombo.isPopupVisible()) {
            // When a credit is selected, update the remaining amount display
            updateSelectedCreditRemainingAmount();
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        }
    }//GEN-LAST:event_creditComboActionPerformed
    private void updateSelectedCreditRemainingAmount() {
        String selected = (String) creditCombo.getSelectedItem();
        if (selected == null || selected.equals("Select Credit")) {
            remainingAmount = 0.0;
            updateRemainingAmountLabel();
            return;
        }

        // Extract the remaining amount from the display text
        String[] parts = selected.split(", Due: ");
        if (parts.length > 1) {
            String amountStr = parts[1].trim();
            // Use regex to validate the amount format
            if (amountStr.matches("^[0-9]*\\.?[0-9]+$")) {
                remainingAmount = Double.parseDouble(amountStr);
            } else {
                remainingAmount = 0.0;
            }
            updateRemainingAmountLabel();
        } else {
            remainingAmount = 0.0;
            updateRemainingAmountLabel();
        }
    }

    private void creditComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_creditComboKeyPressed
        if (creditId != null) {
            return; // Skip if credit is pre-selected
        }
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (creditCombo.isPopupVisible()) {
                creditCombo.setPopupVisible(false);
            }
            if (creditCombo.getSelectedIndex() > 0) {
                updateSelectedCreditRemainingAmount();
                manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!creditCombo.isPopupVisible()) {
                creditCombo.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!creditCombo.isPopupVisible()) {
                saveBtn.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, creditCombo);
        }
    }//GEN-LAST:event_creditComboKeyPressed

    private void manufactureDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manufactureDateKeyPressed
        handleArrowNavigation(evt, manufactureDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_manufactureDateKeyPressed

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed
        if (areAllRequiredFieldsFilled()) {
            saveBtn.requestFocusInWindow();
        } else {
            cancelBtn.requestFocusInWindow();
        }
    }//GEN-LAST:event_addressActionPerformed

    private void addNewCreditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCreditActionPerformed
        openAddNewCredit();
    }//GEN-LAST:event_addNewCreditActionPerformed

    private void addNewCreditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewCreditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F1) {
            openAddNewCredit();
        }
    }//GEN-LAST:event_addNewCreditKeyPressed

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
            java.util.logging.Logger.getLogger(AddCreditPay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddCreditPay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddCreditPay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddCreditPay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddCreditPay dialog = new AddCreditPay(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton addNewCredit;
    private javax.swing.JTextField address;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JComboBox<String> creditCombo;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private com.toedter.calendar.JDateChooser manufactureDate;
    private javax.swing.JLabel remainingAmountLabel;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
