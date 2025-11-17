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
public class UpdateCreditPay extends javax.swing.JDialog {

    private int creditPayId; // Credit Pay ID passed from calling dialog
    private Integer creditCustomerId; // Credit Customer ID from the record
    private double remainingAmount = 0.0;
    private boolean isUpdating = false; // Flag to prevent multiple updates
    private Map<String, Integer> creditCustomerIdMap = new HashMap<>(); // Map to store display text to credit customer ID mapping

    /**
     * Creates new form UpdateCreditPay
     */
    public UpdateCreditPay(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    // Constructor with credit pay ID
    public UpdateCreditPay(java.awt.Frame parent, boolean modal, int creditPayId) {
        super(parent, modal);
        this.creditPayId = creditPayId;
        initComponents();
        initializeDialog();
        loadCreditPayData();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());

        // Load credit combo data FIRST
        loadCreditCombo();
        AutoCompleteDecorator.decorate(creditCombo);

        // THEN setup keyboard navigation and focus
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();
        setupFocusTraversal();

        // Add mouse listeners to combo box
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
        // Credit combo keyboard navigation
        creditCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    givenDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    givenDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    updateBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, creditCombo);
                }
            }
        });

        // Payment date keyboard navigation
        javax.swing.JTextField givenDateEditor = (javax.swing.JTextField) givenDate.getDateEditor().getUiComponent();
        givenDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
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
                    handleArrowNavigation(evt, givenDateEditor);
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
                    givenDate.getDateEditor().getUiComponent().requestFocus();
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
                    givenDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, address);
                }
            }
        });

        // Update button keyboard navigation - FIXED: Now includes ENTER key support
        updateBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateCreditPay();
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
        if (source == creditCombo) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == givenDate.getDateEditor().getUiComponent()) {
            address.requestFocusInWindow();
        } else if (source == address) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            updateBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            creditCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == creditCombo) {
            updateBtn.requestFocusInWindow();
        } else if (source == givenDate.getDateEditor().getUiComponent()) {
            creditCombo.requestFocusInWindow();
        } else if (source == address) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            address.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == creditCombo) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == givenDate.getDateEditor().getUiComponent()) {
            address.requestFocusInWindow();
        } else if (source == address) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            updateBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            creditCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == creditCombo) {
            updateBtn.requestFocusInWindow();
        } else if (source == givenDate.getDateEditor().getUiComponent()) {
            creditCombo.requestFocusInWindow();
        } else if (source == address) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            address.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return creditCombo.getSelectedIndex() > 0
                && givenDate.getDate() != null
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

        // Fix update button to prevent duplicate updates
        setupUpdateButton();
    }

    private void setupUpdateButton() {
        // Remove all existing action listeners - we'll handle everything via keyboard
        for (java.awt.event.ActionListener al : updateBtn.getActionListeners()) {
            updateBtn.removeActionListener(al);
        }

        // Add mouse listener for click events
        updateBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (!isUpdating) {
                    updateCreditPay();
                }
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
        givenDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 01012024 for 01/01/2024</html>");
        address.setToolTipText("Type payment amount and press ENTER to move to next field");
        addNewCredit.setToolTipText("Click to add new credit (or press F1)");
        updateBtn.setToolTipText("Click to update credit payment (or press ENTER when focused)");
        clearFormBtn.setToolTipText("Click to reload original data (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
    }

    // ---------------- BUSINESS LOGIC ----------------
    private void loadCreditCombo() {
        try {
            // Clear the mapping first
            creditCustomerIdMap.clear();

            // Load credit customers with their credit information
            String sql = "SELECT cc.customer_id, cc.customer_name, "
                    + "COALESCE(SUM(c.credit_amout), 0) as total_credit, "
                    + "COALESCE(SUM(cp.credit_pay_amount), 0) as total_paid, "
                    + "(COALESCE(SUM(c.credit_amout), 0) - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount "
                    + "FROM credit_customer cc "
                    + "LEFT JOIN credit c ON cc.customer_id = c.credit_customer_id "
                    + "LEFT JOIN credit_pay cp ON cc.customer_id = cp.credit_customer_id "
                    + "WHERE cc.status_id = 1 " // Only active customers with due amounts
                    + "GROUP BY cc.customer_id, cc.customer_name "
                    + "HAVING remaining_amount > 0 "
                    + "ORDER BY cc.customer_name";

            System.out.println("Loading credit customers combo...");

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> creditCustomers = new Vector<>();
            creditCustomers.add("Select Credit Customer");

            int count = 0;
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                double totalCredit = rs.getDouble("total_credit");
                double totalPaid = rs.getDouble("total_paid");
                double remaining = rs.getDouble("remaining_amount");

                String displayText = String.format("%s - Total: %.2f, Paid: %.2f, Due: %.2f",
                        customerName, totalCredit, totalPaid, remaining);
                creditCustomers.add(displayText);

                // Store the mapping between display text and customer ID
                creditCustomerIdMap.put(displayText, customerId);
                count++;
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(creditCustomers);
            creditCombo.setModel(dcm);

            System.out.println("Successfully loaded " + count + " credit customers into combo box");

        } catch (Exception e) {
            System.err.println("Error loading credit customers: " + e.getMessage());
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading credit customers: " + e.getMessage());
        }
    }

    private void loadCreditPayData() {
        try {
            Connection conn = MySQL.getConnection();

            // Load the specific credit payment record with all details
            String sql = "SELECT cp.*, cc.customer_id, cc.customer_name, "
                    + "(SELECT COALESCE(SUM(c.credit_amout), 0) FROM credit c WHERE c.credit_customer_id = cc.customer_id) as total_credit, "
                    + "(SELECT COALESCE(SUM(cp2.credit_pay_amount), 0) FROM credit_pay cp2 WHERE cp2.credit_customer_id = cc.customer_id) as total_paid, "
                    + "cp.credit_pay_amount as current_payment "
                    + "FROM credit_pay cp "
                    + "JOIN credit_customer cc ON cp.credit_customer_id = cc.customer_id "
                    + "WHERE cp.credit_pay_id = ?";

            System.out.println("Loading credit payment data for ID: " + creditPayId);

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, creditPayId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Store customer ID from the record
                creditCustomerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                double totalCredit = rs.getDouble("total_credit");
                double totalPaid = rs.getDouble("total_paid");
                double currentPayment = rs.getDouble("current_payment");

                System.out.println("Found record - Customer ID: " + creditCustomerId
                        + ", Customer: " + customerName
                        + ", Total Credit: " + totalCredit
                        + ", Total Paid: " + totalPaid
                        + ", Current Payment: " + currentPayment);

                // Calculate remaining amount
                // Remaining = Total credit - Total paid + Current payment (since we're editing current payment)
                remainingAmount = (totalCredit - totalPaid) + currentPayment;
                System.out.println("Calculated remaining amount: " + remainingAmount);

                // Create display text for the combo box
                String displayText = String.format("%s - Total: %.2f, Paid: %.2f, Due: %.2f",
                        customerName, totalCredit, totalPaid - currentPayment, remainingAmount);

                System.out.println("Looking for customer: " + displayText);

                // Find and select this customer in the combo box
                boolean found = false;
                for (int i = 0; i < creditCombo.getItemCount(); i++) {
                    String item = creditCombo.getItemAt(i);
                    if (item.equals(displayText)) {
                        creditCombo.setSelectedIndex(i);
                        found = true;
                        System.out.println("Found exact match at index: " + i);
                        break;
                    }
                }

                if (!found) {
                    System.out.println("No exact match found, searching by customer ID...");
                    // If not found by exact text, find by customer ID
                    for (int i = 0; i < creditCombo.getItemCount(); i++) {
                        String item = creditCombo.getItemAt(i);
                        Integer itemCustomerId = creditCustomerIdMap.get(item);
                        if (itemCustomerId != null && itemCustomerId == creditCustomerId) {
                            creditCombo.setSelectedIndex(i);
                            found = true;
                            System.out.println("Found by customer ID at index: " + i);
                            break;
                        }
                    }
                }

                if (!found) {
                    System.out.println("Customer not found in combo, selecting first available customer");
                    // If still not found, just select the first customer (if available)
                    if (creditCombo.getItemCount() > 1) {
                        creditCombo.setSelectedIndex(1);
                    }
                }

                // Set payment date
                Date paymentDate = rs.getTimestamp("credit_pay_date");
                givenDate.setDate(paymentDate);
                System.out.println("Set payment date: " + paymentDate);

                // Set amount
                address.setText(String.format("%.2f", currentPayment));
                System.out.println("Set payment amount: " + currentPayment);

                // Update remaining amount label and tooltip
                updateRemainingAmountLabel();
                address.setToolTipText("Type payment amount (max: " + String.format("%.2f", remainingAmount) + ") and press ENTER to move to next field");

                System.out.println("All data loaded successfully!");

            } else {
                System.err.println("No credit payment record found with ID: " + creditPayId);
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Credit payment record not found!");
            }

            rs.close();
            pst.close();
        } catch (Exception e) {
            System.err.println("Error loading credit payment data: " + e.getMessage());
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading credit payment data: " + e.getMessage());
        }
    }

    private void updateRemainingAmountLabel() {
        remainingAmountLabel.setText(String.format("Remaining Amount: %.2f", remainingAmount));
        System.out.println("Updated remaining amount label to: " + remainingAmount);
    }

    private void updateSelectedCreditRemainingAmount() {
        String selected = (String) creditCombo.getSelectedItem();
        if (selected == null || selected.equals("Select Credit Customer")) {
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

    private int getSelectedCreditCustomerId() {
        String selected = (String) creditCombo.getSelectedItem();
        if (selected == null || selected.equals("Select Credit Customer")) {
            return -1;
        }

        // Use the mapping to get the customer ID
        Integer customerId = creditCustomerIdMap.get(selected);
        return customerId != null ? customerId : -1;
    }

    private boolean validateInputs() {
        // Validate credit selection
        if (creditCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a credit customer");
            creditCombo.requestFocus();
            return false;
        }

        // Validate payment date
        if (givenDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select payment date");
            givenDate.getDateEditor().getUiComponent().requestFocus();
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

    private void updateCreditPay() {
        // Prevent multiple simultaneous updates
        if (isUpdating) {
            return;
        }

        isUpdating = true;

        Connection conn = null;
        PreparedStatement pstUpdate = null;
        PreparedStatement pstCheckMessage = null;
        PreparedStatement pstInsertMessage = null;
        PreparedStatement pstInsertNotification = null;
        PreparedStatement pstGetCustomerInfo = null;
        ResultSet rs = null;

        try {
            if (!validateInputs()) {
                isUpdating = false;
                return;
            }

            int selectedCustomerId = getSelectedCreditCustomerId();
            if (selectedCustomerId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid credit customer selected");
                isUpdating = false;
                return;
            }

            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String paymentDateStr = datetimeFormat.format(givenDate.getDate());

            double amount = Double.parseDouble(address.getText().trim());

            conn = MySQL.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get customer information for notification message
            String customerSql = "SELECT cc.customer_name, cp.credit_pay_amount as old_amount "
                    + "FROM credit_pay cp "
                    + "JOIN credit_customer cc ON cp.credit_customer_id = cc.customer_id "
                    + "WHERE cp.credit_pay_id = ?";
            pstGetCustomerInfo = conn.prepareStatement(customerSql);
            pstGetCustomerInfo.setInt(1, creditPayId);
            rs = pstGetCustomerInfo.executeQuery();

            String customerName = "Unknown Customer";
            double oldAmount = 0.0;

            if (rs.next()) {
                customerName = rs.getString("customer_name");
                oldAmount = rs.getDouble("old_amount");
            }
            rs.close();
            pstGetCustomerInfo.close();

            // 2. Update the credit payment
            String updateSql = "UPDATE credit_pay SET credit_pay_date = ?, credit_pay_amount = ?, credit_customer_id = ? "
                    + "WHERE credit_pay_id = ?";
            pstUpdate = conn.prepareStatement(updateSql);
            pstUpdate.setString(1, paymentDateStr);
            pstUpdate.setDouble(2, amount);
            pstUpdate.setInt(3, selectedCustomerId);
            pstUpdate.setInt(4, creditPayId);

            int rowsAffected = pstUpdate.executeUpdate();

            if (rowsAffected > 0) {
                // 3. Create notification message
                String messageText = String.format("Credit payment updated for %s: Rs.%.2f → Rs.%.2f",
                        customerName, oldAmount, amount);

                // Check if message already exists in massage table
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

                // 4. Insert notification (msg_type_id 18 for "Edit Credit Pay")
                String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (1, NOW(), 18, ?)";
                pstInsertNotification = conn.prepareStatement(notificationSql);
                pstInsertNotification.setInt(1, messageId);
                pstInsertNotification.executeUpdate();

                // Commit transaction
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "Credit payment updated successfully!");
                dispose(); // Close the dialog after successful update
            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Failed to update credit payment!");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error updating credit payment: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close all resources
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstUpdate != null) {
                    pstUpdate.close();
                }
                if (pstGetCustomerInfo != null) {
                    pstGetCustomerInfo.close();
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

    private void clearForm() {
        loadCreditPayData(); // Reload original data
        creditCombo.requestFocus();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Form reset to original values!");
    }

    private void openAddNewCredit() {
        try {
           
            AddCredit dialog = new AddCredit(null, true);
            dialog.setLocationRelativeTo(null);
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
            if (givenDate.getDateEditor() != null && givenDate.getDateEditor().getUiComponent() != null) {
                givenDate.getDateEditor().getUiComponent().setFocusable(true);
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
        updateBtn = new javax.swing.JButton();
        creditCombo = new javax.swing.JComboBox<>();
        givenDate = new com.toedter.calendar.JDateChooser();
        address = new javax.swing.JTextField();
        addNewCredit = new javax.swing.JButton();
        remainingAmountLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Credit Payment");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Update Credit Payment");

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

        givenDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Given Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        givenDate.setDateFormatString("MM/dd/yyyy");
        givenDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        givenDate.setOpaque(false);
        givenDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                givenDateKeyPressed(evt);
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
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(remainingAmountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel3)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(creditCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addNewCredit, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(givenDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(address, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 24, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(remainingAmountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(creditCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewCredit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(givenDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
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

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        updateCreditPay();
    }//GEN-LAST:event_updateBtnActionPerformed

    private void updateBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // Only process if not already updating and the button has focus
            if (!isUpdating && updateBtn.hasFocus()) {
                // Temporarily disable the button to prevent action listener from firing
                updateBtn.setEnabled(false);
                SwingUtilities.invokeLater(() -> {
                    updateBtn.setEnabled(true);
                });
                updateCreditPay();
            }
            evt.consume(); // Consume the event to prevent further processing
        } else {
            handleArrowNavigation(evt, updateBtn);
        }
    }//GEN-LAST:event_updateBtnKeyPressed

    private void creditComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditComboActionPerformed
        if (creditCombo.getSelectedIndex() > 0 && !creditCombo.isPopupVisible()) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        }
    }//GEN-LAST:event_creditComboActionPerformed

    private void creditComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_creditComboKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (creditCombo.isPopupVisible()) {
                creditCombo.setPopupVisible(false);
            }
            if (creditCombo.getSelectedIndex() > 0) {
                givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!creditCombo.isPopupVisible()) {
                creditCombo.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!creditCombo.isPopupVisible()) {
                updateBtn.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, creditCombo);
        }
    }//GEN-LAST:event_creditComboKeyPressed

    private void givenDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_givenDateKeyPressed
        handleArrowNavigation(evt, givenDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_givenDateKeyPressed

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed
        if (areAllRequiredFieldsFilled()) {
            updateBtn.requestFocusInWindow();
        } else {
            cancelBtn.requestFocusInWindow();
        }
    }//GEN-LAST:event_addressActionPerformed

    private void addNewCreditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewCreditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F1) {
            openAddNewCredit();
        }
    }//GEN-LAST:event_addNewCreditKeyPressed

    private void addNewCreditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCreditActionPerformed
        openAddNewCredit();
    }//GEN-LAST:event_addNewCreditActionPerformed

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
            java.util.logging.Logger.getLogger(UpdateCreditPay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateCreditPay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateCreditPay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateCreditPay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateCreditPay dialog = new UpdateCreditPay(new javax.swing.JFrame(), true,3);
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
    private com.toedter.calendar.JDateChooser givenDate;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel remainingAmountLabel;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
