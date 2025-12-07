package lk.com.pos.dialog;

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
import java.sql.*;
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
import lk.com.pos.connection.DB;  // Changed import
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class CreditDiscount extends javax.swing.JDialog {

    private int customerId = -1;
    private Map<String, Integer> customerIdMap = new HashMap<>();
    private Map<Integer, String> customerDisplayMap = new HashMap<>(); // Reverse mapping for quick lookup
    private boolean isSaving = false;
    private double totalCredit = 0.0;
    private double totalPaid = 0.0;
    private double dueAmount = 0.0;
    private Date latestDueDate = null;
    private int newlyAddedCustomerId = -1; // Track newly added customer
    private int currentDiscountId = -1; // Track current discount ID for updates
    private boolean isUpdateMode = false; // Track if we're in update mode

    // Default constructor
    public CreditDiscount(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    // New constructor that accepts customer ID
    public CreditDiscount(java.awt.Frame parent, boolean modal, int customerId) {
        super(parent, modal);
        this.customerId = customerId;
        initComponents();
        initializeDialog();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        loadCustomerCombo();
        loadDiscountTypeCombo();
        AutoCompleteDecorator.decorate(comboCustomer3);

        setupFocusTraversal();

        // Fix mouse selection issue
        setupComboBoxMouseSelection();

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

        comboCustomer3.requestFocusInWindow();
    }

    private void setupComboBoxMouseSelection() {
        // Remove any existing mouse listeners to avoid duplicates
        for (java.awt.event.MouseListener ml : comboCustomer3.getMouseListeners()) {
            if (ml.getClass().getName().contains("CreditDiscount")) {
                comboCustomer3.removeMouseListener(ml);
            }
        }

        // Add proper mouse listener for selection
        comboCustomer3.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Let the combo box handle the click first
                SwingUtilities.invokeLater(() -> {
                    handleCustomerSelection();
                });
            }
        });

        // Also handle item selection changes
        comboCustomer3.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (comboCustomer3.getSelectedIndex() > 0 && !comboCustomer3.isPopupVisible()) {
                    handleCustomerSelection();
                    // Auto move to next field after selection
                    diTypeCombo.requestFocusInWindow();
                }
            }
        });
    }

    private void handleCustomerSelection() {
        if (comboCustomer3.getSelectedIndex() > 0) {
            String selectedDisplayText = (String) comboCustomer3.getSelectedItem();
            int selectedCustomerId = getCustomerId(selectedDisplayText);
            if (selectedCustomerId != -1) {
                loadCustomerCreditDetails(selectedCustomerId);
                checkExistingDiscount(selectedCustomerId);
            }
        }
    }

    // Add this method to check for existing discount
    private void checkExistingDiscount(int customerId) {
        try {
            String discountInfo = DB.executeQuerySafe(
                "SELECT cd.credit_discount_id, d.discount_id, d.discount, dt.discount_type " +
                "FROM credit_discount cd " +
                "JOIN discount d ON cd.discount_id = d.discount_id " +
                "JOIN discount_type dt ON d.discount_type_id = dt.discount_type_id " +
                "WHERE cd.credit_id = ? " +
                "ORDER BY cd.credit_discount_id DESC LIMIT 1",
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt("discount_id") + "|" + 
                               rs.getDouble("discount") + "|" + 
                               rs.getString("discount_type");
                    }
                    return null;
                },
                customerId
            );
            
            if (discountInfo != null) {
                String[] parts = discountInfo.split("\\|");
                currentDiscountId = Integer.parseInt(parts[0]);
                double discountValue = Double.parseDouble(parts[1]);
                String discountType = parts[2];

                // Load existing discount into form
                diTypeCombo.setSelectedItem(discountType);
                discountInput.setText(String.valueOf(discountValue));

                // Change button text and icon to indicate update mode
                btnUpdate.setText("Update Discount");
                isUpdateMode = true;

                // Change icon to update.svg
                FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnUpdate.setIcon(updateIcon);

                // Update hover and focus icons for update mode
                updateButtonIconsForUpdateMode();

                // Show info message
                Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT,
                        "Existing discount loaded. You can update it.");
            } else {
                // No existing discount
                currentDiscountId = -1;
                isUpdateMode = false;
                btnUpdate.setText("Save");

                // Reset to add icon
                FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnUpdate.setIcon(addIcon);

                // Update hover and focus icons for add mode
                updateButtonIconsForAddMode();

                // Clear form if no discount exists
                diTypeCombo.setSelectedIndex(0);
                discountInput.setText("");
            }

        } catch (SQLException e) {
            // Silent error handling
            currentDiscountId = -1;
            isUpdateMode = false;
            btnUpdate.setText("Save");

            // Reset to add icon on error
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
            addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
            btnUpdate.setIcon(addIcon);
            updateButtonIconsForAddMode();
        }
    }

    private void updateButtonIconsForUpdateMode() {
        // Update mouse listeners for update mode
        for (java.awt.event.MouseListener ml : btnUpdate.getMouseListeners()) {
            btnUpdate.removeMouseListener(ml);
        }

        btnUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnUpdate.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnUpdate.setIcon(hoverIcon);
                btnUpdate.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnUpdate.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnUpdate.setIcon(normalIcon);
                btnUpdate.repaint();
            }
        });

        // Update focus listeners for update mode
        for (java.awt.event.FocusListener fl : btnUpdate.getFocusListeners()) {
            btnUpdate.removeFocusListener(fl);
        }

        btnUpdate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnUpdate.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnUpdate.setIcon(focusedIcon);
                btnUpdate.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                btnUpdate.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnUpdate.setIcon(normalIcon);
                btnUpdate.repaint();
            }
        });
    }

    private void updateButtonIconsForAddMode() {
        // Update mouse listeners for add mode
        for (java.awt.event.MouseListener ml : btnUpdate.getMouseListeners()) {
            btnUpdate.removeMouseListener(ml);
        }

        btnUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnUpdate.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnUpdate.setIcon(hoverIcon);
                btnUpdate.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnUpdate.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnUpdate.setIcon(normalIcon);
                btnUpdate.repaint();
            }
        });

        // Update focus listeners for add mode
        for (java.awt.event.FocusListener fl : btnUpdate.getFocusListeners()) {
            btnUpdate.removeFocusListener(fl);
        }

        btnUpdate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnUpdate.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnUpdate.setIcon(focusedIcon);
                btnUpdate.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                btnUpdate.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnUpdate.setIcon(normalIcon);
                btnUpdate.repaint();
            }
        });
    }

    private void setupKeyboardNavigation() {
        comboCustomer3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (comboCustomer3.isPopupVisible()) {
                        comboCustomer3.setPopupVisible(false);
                    }
                    if (comboCustomer3.getSelectedIndex() > 0) {
                        String selectedDisplayText = (String) comboCustomer3.getSelectedItem();
                        int selectedCustomerId = getCustomerId(selectedDisplayText);
                        if (selectedCustomerId != -1) {
                            loadCustomerCreditDetails(selectedCustomerId);
                            checkExistingDiscount(selectedCustomerId);
                        }
                        diTypeCombo.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!comboCustomer3.isPopupVisible()) {
                        comboCustomer3.showPopup();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    if (!comboCustomer3.isPopupVisible()) {
                        btnUpdate.requestFocusInWindow();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    diTypeCombo.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, comboCustomer3);
                }
            }
        });

        diTypeCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (diTypeCombo.isPopupVisible()) {
                        diTypeCombo.setPopupVisible(false);
                    }
                    if (diTypeCombo.getSelectedIndex() > 0) {
                        discountInput.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!diTypeCombo.isPopupVisible()) {
                        diTypeCombo.showPopup();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    if (!diTypeCombo.isPopupVisible()) {
                        comboCustomer3.requestFocusInWindow();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    discountInput.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    comboCustomer3.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, diTypeCombo);
                }
            }
        });

        discountInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (areAllRequiredFieldsFilled()) {
                        btnUpdate.requestFocusInWindow();
                    } else {
                        btnCancel.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    diTypeCombo.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (areAllRequiredFieldsFilled()) {
                        btnUpdate.requestFocusInWindow();
                    } else {
                        btnCancel.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    diTypeCombo.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, discountInput);
                }
            }
        });

        btnUpdate.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume();
                    btnUpdate.doClick(); // Trigger the button click to save
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    discountInput.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnClear.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnClear.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnClear.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, btnUpdate);
                }
            }
        });

        btnClear.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearForm();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    discountInput.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnCancel.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnUpdate.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnCancel.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, btnClear);
                }
            }
        });

        btnCancel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    discountInput.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnUpdate.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnClear.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnUpdate.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, btnCancel);
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
        if (source == comboCustomer3) {
            diTypeCombo.requestFocusInWindow();
        } else if (source == diTypeCombo) {
            discountInput.requestFocusInWindow();
        } else if (source == discountInput) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnCancel) {
            btnClear.requestFocusInWindow();
        } else if (source == btnClear) {
            btnUpdate.requestFocusInWindow();
        } else if (source == btnUpdate) {
            comboCustomer3.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == comboCustomer3) {
            btnUpdate.requestFocusInWindow();
        } else if (source == diTypeCombo) {
            comboCustomer3.requestFocusInWindow();
        } else if (source == discountInput) {
            diTypeCombo.requestFocusInWindow();
        } else if (source == btnCancel) {
            discountInput.requestFocusInWindow();
        } else if (source == btnClear) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnUpdate) {
            btnClear.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == comboCustomer3) {
            diTypeCombo.requestFocusInWindow();
        } else if (source == diTypeCombo) {
            discountInput.requestFocusInWindow();
        } else if (source == discountInput) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnCancel) {
            btnClear.requestFocusInWindow();
        } else if (source == btnClear) {
            btnUpdate.requestFocusInWindow();
        } else if (source == btnUpdate) {
            comboCustomer3.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == comboCustomer3) {
            btnUpdate.requestFocusInWindow();
        } else if (source == diTypeCombo) {
            comboCustomer3.requestFocusInWindow();
        } else if (source == discountInput) {
            diTypeCombo.requestFocusInWindow();
        } else if (source == btnCancel) {
            discountInput.requestFocusInWindow();
        } else if (source == btnClear) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnUpdate) {
            btnClear.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return comboCustomer3.getSelectedIndex() > 0
                && diTypeCombo.getSelectedIndex() > 0
                && !discountInput.getText().trim().isEmpty();
    }

    private void setupButtonStyles() {
        setupGradientButton(btnUpdate);
        setupGradientButton(btnClear);
        setupGradientButton(btnCancel);

        // Start with add icon for save button
        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnUpdate.setIcon(updateIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnClear.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnCancel.setIcon(cancelIcon);

        btnAddNewCustomer.setBorderPainted(false);
        btnAddNewCustomer.setContentAreaFilled(false);
        btnAddNewCustomer.setFocusPainted(false);
        btnAddNewCustomer.setOpaque(false);
        btnAddNewCustomer.setFocusable(false);
        btnAddNewCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon customerIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
        customerIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        btnAddNewCustomer.setIcon(customerIcon);

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

                boolean isHover = button.getModel().isRollover();
                boolean isPressed = button.getModel().isPressed();
                boolean isFocused = button.hasFocus();

                if (!isFocused && !isHover && !isPressed) {
                    g2.setColor(new Color(0, 0, 0, 0));
                    g2.fillRoundRect(0, 0, w, h, 5, 5);

                    g2.setColor(Color.decode("#0893B0"));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
                } else {
                    Color topColor = new Color(0x12, 0xB5, 0xA6);
                    Color bottomColor = new Color(0x08, 0x93, 0xB0);

                    GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 5, 5);
                }

                super.paint(g, c);
            }
        });
    }

    private void setupButtonMouseListeners() {
        btnUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnUpdate.setForeground(Color.WHITE);
                if (isUpdateMode) {
                    FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                    hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                    btnUpdate.setIcon(hoverIcon);
                } else {
                    FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                    hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                    btnUpdate.setIcon(hoverIcon);
                }
                btnUpdate.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnUpdate.setForeground(Color.decode("#0893B0"));
                if (isUpdateMode) {
                    FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                    normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                    btnUpdate.setIcon(normalIcon);
                } else {
                    FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                    normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                    btnUpdate.setIcon(normalIcon);
                }
                btnUpdate.repaint();
            }
        });

        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnClear.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnClear.setIcon(hoverIcon);
                btnClear.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnClear.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnClear.setIcon(normalIcon);
                btnClear.repaint();
            }
        });

        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCancel.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnCancel.setIcon(hoverIcon);
                btnCancel.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCancel.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnCancel.setIcon(normalIcon);
                btnCancel.repaint();
            }
        });

        btnAddNewCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnAddNewCustomer.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                btnAddNewCustomer.setIcon(normalIcon);
            }
        });
    }

    private void setupButtonFocusListeners() {
        btnUpdate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnUpdate.setForeground(Color.WHITE);
                if (isUpdateMode) {
                    FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                    focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                    btnUpdate.setIcon(focusedIcon);
                } else {
                    FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                    focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                    btnUpdate.setIcon(focusedIcon);
                }
                btnUpdate.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                btnUpdate.setForeground(Color.decode("#0893B0"));
                if (isUpdateMode) {
                    FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                    normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                    btnUpdate.setIcon(normalIcon);
                } else {
                    FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                    normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                    btnUpdate.setIcon(normalIcon);
                }
                btnUpdate.repaint();
            }
        });

        btnClear.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnClear.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnClear.setIcon(focusedIcon);
                btnClear.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                btnClear.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnClear.setIcon(normalIcon);
                btnClear.repaint();
            }
        });

        btnCancel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnCancel.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnCancel.setIcon(focusedIcon);
                btnCancel.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                btnCancel.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnCancel.setIcon(normalIcon);
                btnCancel.repaint();
            }
        });

        btnAddNewCustomer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnAddNewCustomer.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                btnAddNewCustomer.setIcon(normalIcon);
            }
        });
    }

    private void setupTooltips() {
        comboCustomer3.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field<br>Press <b>F2</b> to add new customer</html>");
        diTypeCombo.setToolTipText("Select discount type and press ENTER to move to next field");
        discountInput.setToolTipText("Enter discount amount/percentage and press ENTER to move to next field");
        btnAddNewCustomer.setToolTipText("Click to add new customer (or press F2)");
        btnUpdate.setToolTipText("Click to save/update discount (or press ENTER when focused)");
        btnClear.setToolTipText("Click to clear form (or press ENTER when focused)");
        btnCancel.setToolTipText("Click to cancel (or press ESC)");
    }

    private void loadCustomerCombo() {
        try {
            customerIdMap.clear();
            customerDisplayMap.clear();

            Vector<String> customers = DB.executeQuerySafe(
                "SELECT cc.customer_id, cc.customer_name, " +
                "COALESCE(SUM(c.credit_amout), 0) as total_credit, " +
                "COALESCE(SUM(cp.credit_pay_amount), 0) as total_paid, " +
                "(COALESCE(SUM(c.credit_amout), 0) - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount, " +
                "MAX(c.credit_final_date) as latest_due_date, " +
                "COUNT(ch.cheque_id) as active_cheques " +
                "FROM credit_customer cc " +
                "LEFT JOIN credit c ON cc.customer_id = c.credit_customer_id " +
                "LEFT JOIN credit_pay cp ON cc.customer_id = cp.credit_customer_id " +
                "LEFT JOIN cheque ch ON cc.customer_id = ch.credit_customer_id AND ch.cheque_date >= CURDATE() " +
                "WHERE cc.status_id = 1 " +
                "GROUP BY cc.customer_id, cc.customer_name " +
                "ORDER BY cc.customer_name",
                (ResultSet rs) -> {
                    Vector<String> list = new Vector<>();
                    list.add("Select Customer");
                    
                    while (rs.next()) {
                        int customerId = rs.getInt("customer_id");
                        String customerName = rs.getString("customer_name");
                        double totalCredit = rs.getDouble("total_credit");
                        double totalPaid = rs.getDouble("total_paid");
                        double remainingAmount = rs.getDouble("remaining_amount");
                        Date latestDueDate = rs.getDate("latest_due_date");
                        int activeCheques = rs.getInt("active_cheques");

                        String displayText;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        String dueDateStr = latestDueDate != null ? dateFormat.format(latestDueDate) : "No Due Date";

                        if (totalCredit > 0) {
                            if (activeCheques > 0) {
                                displayText = String.format("%s | Total: Rs %.2f | Paid: Rs %.2f | Due: Rs %.2f | Due Date: %s | Active Cheques: %d",
                                        customerName, totalCredit, totalPaid, remainingAmount, dueDateStr, activeCheques);
                            } else {
                                displayText = String.format("%s | Total: Rs %.2f | Paid: Rs %.2f | Due: Rs %.2f | Due Date: %s",
                                        customerName, totalCredit, totalPaid, remainingAmount, dueDateStr);
                            }
                        } else {
                            if (activeCheques > 0) {
                                displayText = String.format("%s | No Credit History | Active Cheques: %d",
                                        customerName, activeCheques);
                            } else {
                                displayText = String.format("%s | No Credit History | No Active Cheques",
                                        customerName);
                            }
                        }

                        list.add(displayText);
                        customerIdMap.put(displayText, customerId);
                        customerDisplayMap.put(customerId, displayText);
                    }
                    return list;
                }
            );

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(customers);
            comboCustomer3.setModel(dcm);

            // Pre-select the customer if provided
            if (this.customerId != -1 && customerDisplayMap.containsKey(this.customerId)) {
                String displayText = customerDisplayMap.get(this.customerId);
                for (int i = 0; i < comboCustomer3.getItemCount(); i++) {
                    if (displayText.equals(comboCustomer3.getItemAt(i))) {
                        comboCustomer3.setSelectedIndex(i);
                        loadCustomerCreditDetails(this.customerId);
                        checkExistingDiscount(this.customerId);
                        break;
                    }
                }
            }

            // Auto-select newly added customer if available
            if (newlyAddedCustomerId != -1 && customerDisplayMap.containsKey(newlyAddedCustomerId)) {
                String displayText = customerDisplayMap.get(newlyAddedCustomerId);
                for (int i = 0; i < comboCustomer3.getItemCount(); i++) {
                    if (displayText.equals(comboCustomer3.getItemAt(i))) {
                        comboCustomer3.setSelectedIndex(i);
                        loadCustomerCreditDetails(newlyAddedCustomerId);
                        checkExistingDiscount(newlyAddedCustomerId);

                        // Reset the flag
                        newlyAddedCustomerId = -1;
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading customers: " + e.getMessage());
        }
    }

    private void loadDiscountTypeCombo() {
        try {
            Vector<String> discountTypes = DB.executeQuerySafe(
                "SELECT discount_type_id, discount_type FROM discount_type",
                (ResultSet rs) -> {
                    Vector<String> list = new Vector<>();
                    list.add("Select Discount Type");
                    while (rs.next()) {
                        list.add(rs.getString("discount_type"));
                    }
                    return list;
                }
            );

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(discountTypes);
            diTypeCombo.setModel(dcm);

        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error loading discount types");
        }
    }

    private void loadCustomerCreditDetails(int customerId) {
        try {
            String creditInfo = DB.executeQuerySafe(
                "SELECT " +
                "COALESCE(SUM(c.credit_amout), 0) as total_credit, " +
                "COALESCE(SUM(cp.credit_pay_amount), 0) as total_paid, " +
                "(COALESCE(SUM(c.credit_amout), 0) - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount, " +
                "MAX(c.credit_final_date) as latest_due_date, " +
                "COUNT(ch.cheque_id) as active_cheques " +
                "FROM credit_customer cc " +
                "LEFT JOIN credit c ON cc.customer_id = c.credit_customer_id " +
                "LEFT JOIN credit_pay cp ON cc.customer_id = cp.credit_customer_id " +
                "LEFT JOIN cheque ch ON cc.customer_id = ch.credit_customer_id AND ch.cheque_date >= CURDATE() " +
                "WHERE cc.customer_id = ? " +
                "GROUP BY cc.customer_id",
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getDouble("total_credit") + "|" +
                               rs.getDouble("total_paid") + "|" +
                               rs.getDouble("remaining_amount") + "|" +
                               rs.getDate("latest_due_date").getTime();
                    }
                    return null;
                },
                customerId
            );

            if (creditInfo != null) {
                String[] parts = creditInfo.split("\\|");
                this.totalCredit = Double.parseDouble(parts[0]);
                this.totalPaid = Double.parseDouble(parts[1]);
                this.dueAmount = Double.parseDouble(parts[2]);
                this.latestDueDate = new Date(Long.parseLong(parts[3]));
            }

        } catch (SQLException e) {
            // Silent error handling for credit details loading
        }
    }

    private int getCustomerId(String displayText) {
        Integer customerId = customerIdMap.get(displayText);

        if (customerId == null) {
            return -1;
        }

        return customerId;
    }

    private boolean validateInputs() {
        if (comboCustomer3.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a customer");
            comboCustomer3.requestFocus();
            return false;
        }

        if (diTypeCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select discount type");
            diTypeCombo.requestFocus();
            return false;
        }

        if (discountInput.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter discount value");
            discountInput.requestFocus();
            return false;
        }

        try {
            double discount = Double.parseDouble(discountInput.getText().trim());
            if (discount <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid discount value");
                discountInput.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid discount value");
            discountInput.requestFocus();
            return false;
        }

        return true;
    }

    private void updateDiscount() {
        if (isSaving) {
            return;
        }

        if (!validateInputs()) {
            return;
        }

        Connection conn = null;
        PreparedStatement pstDiscount = null;
        PreparedStatement pstCreditDiscount = null;
        PreparedStatement checkPst = null;
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            isSaving = true;

            String selectedDisplayText = (String) comboCustomer3.getSelectedItem();
            this.customerId = getCustomerId(selectedDisplayText);
            if (this.customerId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid customer selected");
                isSaving = false;
                return;
            }

            String discountType = (String) diTypeCombo.getSelectedItem();
            double discountValue = Double.parseDouble(discountInput.getText().trim());

            // Get discount type ID
            int discountTypeId = getDiscountTypeId(discountType);
            if (discountTypeId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid discount type");
                isSaving = false;
                return;
            }

            conn = DB.getConnection();
            conn.setAutoCommit(false);

            if (isUpdateMode && currentDiscountId != -1) {
                // UPDATE EXISTING DISCOUNT
                String updateQuery = "UPDATE discount SET discount = ?, discount_type_id = ? WHERE discount_id = ?";
                pstDiscount = conn.prepareStatement(updateQuery);
                pstDiscount.setDouble(1, discountValue);
                pstDiscount.setInt(2, discountTypeId);
                pstDiscount.setInt(3, currentDiscountId);

                int updatedRows = pstDiscount.executeUpdate();
                if (updatedRows > 0) {
                    createDiscountNotification(selectedDisplayText, discountValue, discountType, conn, true);
                    conn.commit();

                    String successMessage = String.format("Discount updated to %.2f %s successfully!",
                            discountValue, discountType.equals("percentage") ? "%" : "");
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, successMessage);

                    dispose();
                } else {
                    conn.rollback();
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to update discount!");
                }
            } else {
                // INSERT NEW DISCOUNT (only if no existing discount)
                // First check if customer already has a discount (double-check)
                String checkSql = "SELECT COUNT(*) as count FROM credit_discount WHERE credit_id = ?";
                checkPst = conn.prepareStatement(checkSql);
                checkPst.setInt(1, this.customerId);
                
                try (ResultSet checkRs = checkPst.executeQuery()) {
                    if (checkRs.next() && checkRs.getInt("count") > 0) {
                        conn.rollback();
                        isSaving = false;
                        return;
                    }
                }

                // Insert into discount table
                String discountQuery = "INSERT INTO discount (discount, discount_type_id) VALUES (?, ?)";
                pstDiscount = conn.prepareStatement(discountQuery, Statement.RETURN_GENERATED_KEYS);
                pstDiscount.setDouble(1, discountValue);
                pstDiscount.setInt(2, discountTypeId);

                int discountRows = pstDiscount.executeUpdate();
                if (discountRows > 0) {
                    try (ResultSet generatedKeys = pstDiscount.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int discountId = generatedKeys.getInt(1);

                            // Insert into credit_discount table
                            String creditDiscountQuery = "INSERT INTO credit_discount (credit_id, discount_id) VALUES (?, ?)";
                            pstCreditDiscount = conn.prepareStatement(creditDiscountQuery);
                            pstCreditDiscount.setInt(1, this.customerId);
                            pstCreditDiscount.setInt(2, discountId);

                            int creditDiscountRows = pstCreditDiscount.executeUpdate();
                            if (creditDiscountRows > 0) {
                                createDiscountNotification(selectedDisplayText, discountValue, discountType, conn, false);
                                conn.commit();

                                String successMessage = String.format("Discount of %.2f %s applied successfully to customer!",
                                        discountValue, discountType.equals("percentage") ? "%" : "");
                                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, successMessage);

                                dispose();
                            } else {
                                conn.rollback();
                                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to apply discount to customer!");
                            }
                        } else {
                            conn.rollback();
                            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to get discount ID!");
                        }
                    }
                } else {
                    conn.rollback();
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to create discount!");
                }
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                // Silent rollback exception handling
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error applying discount: " + e.getMessage());
        } finally {
            // Close resources
            DB.closeQuietly(pstNotification, pstMassage, pstCreditDiscount, pstDiscount, checkPst);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            isSaving = false;
        }
    }

    private int getDiscountTypeId(String discountType) {
        try {
            Integer discountTypeId = DB.executeQuerySafe(
                "SELECT discount_type_id FROM discount_type WHERE discount_type = ?",
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt("discount_type_id");
                    }
                    return -1;
                },
                discountType
            );
            return discountTypeId;
        } catch (SQLException e) {
            return -1;
        }
    }

    private void createDiscountNotification(String customerName, double discountValue, String discountType, Connection conn, boolean isUpdate) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            String action = isUpdate ? "updated for" : "applied to";
            String messageText = String.format("Discount %s %s: %.2f %s",
                    action, customerName, discountValue, discountType.equals("percentage") ? "%" : "LKR");

            int massageId;

            // Check if the message already exists in the massage table
            Integer existingMassageId = DB.executeQuerySafe(
                "SELECT massage_id FROM massage WHERE massage = ?",
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt("massage_id");
                    }
                    return null;
                },
                messageText
            );

            if (existingMassageId != null) {
                massageId = existingMassageId;
            } else {
                // Insert new message
                String insertMassageSql = "INSERT INTO massage (massage) VALUES (?)";
                pstMassage = conn.prepareStatement(insertMassageSql, Statement.RETURN_GENERATED_KEYS);
                pstMassage.setString(1, messageText);
                pstMassage.executeUpdate();

                try (ResultSet generatedKeys = pstMassage.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        massageId = generatedKeys.getInt(1);
                    } else {
                        // Fallback if no generated key
                        massageId = getMaxMessageId(conn);
                    }
                }
            }

            // Insert into notification table
            String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)";
            pstNotification = conn.prepareStatement(notificationSql);
            pstNotification.setInt(1, 1); // is_read = 1 (unread)
            pstNotification.setInt(2, 26); // Credit discount message type
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (SQLException e) {
            // Silent exception handling for notification
        } finally {
            DB.closeQuietly(pstMassage, pstNotification);
        }
    }

    // Helper method to get the maximum message ID as fallback
    private int getMaxMessageId(Connection conn) {
        try {
            Integer maxId = DB.executeQuerySafe(
                "SELECT MAX(massage_id) as max_id FROM massage",
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt("max_id");
                    }
                    return 0;
                }
            );
            return maxId;
        } catch (SQLException e) {
            return 0;
        }
    }

    private void clearForm() {
        comboCustomer3.setSelectedIndex(0);
        diTypeCombo.setSelectedIndex(0);
        discountInput.setText("");
        currentDiscountId = -1;
        isUpdateMode = false;
        btnUpdate.setText("Save");

        // Reset to add icon
        FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnUpdate.setIcon(addIcon);
        updateButtonIconsForAddMode();

        comboCustomer3.requestFocus();
    }

    private void openAddNewCustomer() {
        try {
            AddNewCustomer dialog = new AddNewCustomer((JFrame) getParent(), true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            if (dialog.isCustomerSaved()) {
                // Store the new customer ID
                newlyAddedCustomerId = dialog.getSavedCustomerId();

                // Refresh the customer combo box
                loadCustomerCombo();

                comboCustomer3.requestFocus();
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening customer dialog: " + e.getMessage());
        }
    }

    private void setupFocusTraversal() {
        btnAddNewCustomer.setFocusable(false);
    }

    // Add a public method to set customer ID after dialog creation
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
        if (customerId != -1 && customerDisplayMap.containsKey(customerId)) {
            String displayText = customerDisplayMap.get(customerId);
            comboCustomer3.setSelectedItem(displayText);
            loadCustomerCreditDetails(customerId);
            checkExistingDiscount(customerId);
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        diTypeCombo = new javax.swing.JComboBox<>();
        discountInput = new javax.swing.JTextField();
        comboCustomer3 = new javax.swing.JComboBox<>();
        btnCancel = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnAddNewCustomer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Credit Discount");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        diTypeCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        diTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        diTypeCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Discount Type *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        diTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diTypeComboActionPerformed(evt);
            }
        });
        diTypeCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diTypeComboKeyPressed(evt);
            }
        });

        discountInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        discountInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Discount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        discountInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discountInputActionPerformed(evt);
            }
        });
        discountInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                discountInputKeyPressed(evt);
            }
        });

        comboCustomer3.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        comboCustomer3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboCustomer3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customer *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        comboCustomer3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboCustomer3ActionPerformed(evt);
            }
        });
        comboCustomer3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comboCustomer3KeyPressed(evt);
            }
        });

        btnCancel.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(8, 147, 176));
        btnCancel.setText("Cancel");
        btnCancel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnCancel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCancelKeyPressed(evt);
            }
        });

        btnClear.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnClear.setForeground(new java.awt.Color(8, 147, 176));
        btnClear.setText("Clear");
        btnClear.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        btnClear.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnClearKeyPressed(evt);
            }
        });

        btnUpdate.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(8, 147, 176));
        btnUpdate.setText("Save");
        btnUpdate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        btnUpdate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnUpdateKeyPressed(evt);
            }
        });

        btnAddNewCustomer.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        btnAddNewCustomer.setForeground(new java.awt.Color(102, 102, 102));
        btnAddNewCustomer.setBorder(null);
        btnAddNewCustomer.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddNewCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNewCustomerActionPerformed(evt);
            }
        });
        btnAddNewCustomer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnAddNewCustomerKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(diTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discountInput, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator3)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(comboCustomer3, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddNewCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboCustomer3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnAddNewCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(diTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discountInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddNewCustomerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnAddNewCustomerKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F2) {
            openAddNewCustomer();
        }
    }//GEN-LAST:event_btnAddNewCustomerKeyPressed

    private void btnAddNewCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddNewCustomerActionPerformed
        openAddNewCustomer();
    }//GEN-LAST:event_btnAddNewCustomerActionPerformed

    private void btnUpdateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnUpdateKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            // Don't call updateDiscount here - let ActionListener handle it
        } else {
            handleArrowNavigation(evt, btnUpdate);
        }
    }//GEN-LAST:event_btnUpdateKeyPressed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (!isSaving) {
            updateDiscount();
        }

    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnClearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnClearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        } else {
            handleArrowNavigation(evt, btnClear);
        }
    }//GEN-LAST:event_btnClearKeyPressed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnCancelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCancelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        } else {
            handleArrowNavigation(evt, btnCancel);
        }
    }//GEN-LAST:event_btnCancelKeyPressed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void comboCustomer3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboCustomer3KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (comboCustomer3.isPopupVisible()) {
                comboCustomer3.setPopupVisible(false);
            }
            if (comboCustomer3.getSelectedIndex() > 0) {
                String selectedDisplayText = (String) comboCustomer3.getSelectedItem();
                int selectedCustomerId = getCustomerId(selectedDisplayText);
                if (selectedCustomerId != -1) {
                    loadCustomerCreditDetails(selectedCustomerId);
                    checkExistingDiscount(selectedCustomerId);
                }
                diTypeCombo.requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!comboCustomer3.isPopupVisible()) {
                comboCustomer3.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!comboCustomer3.isPopupVisible()) {
                btnUpdate.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, comboCustomer3);
        }
    }//GEN-LAST:event_comboCustomer3KeyPressed

    private void comboCustomer3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboCustomer3ActionPerformed
        if (comboCustomer3.getSelectedIndex() > 0 && !comboCustomer3.isPopupVisible()) {
            String selectedDisplayText = (String) comboCustomer3.getSelectedItem();
            int selectedCustomerId = getCustomerId(selectedDisplayText);
            if (selectedCustomerId != -1) {
                loadCustomerCreditDetails(selectedCustomerId);
                checkExistingDiscount(selectedCustomerId);
            }
            diTypeCombo.requestFocusInWindow();
        }
    }//GEN-LAST:event_comboCustomer3ActionPerformed

    private void discountInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_discountInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (areAllRequiredFieldsFilled()) {
                btnUpdate.requestFocusInWindow();
            } else {
                btnCancel.requestFocusInWindow();
            }
            evt.consume();
        } else {
            handleArrowNavigation(evt, discountInput);
        }
    }//GEN-LAST:event_discountInputKeyPressed

    private void discountInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discountInputActionPerformed

    }//GEN-LAST:event_discountInputActionPerformed

    private void diTypeComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diTypeComboKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (diTypeCombo.isPopupVisible()) {
                diTypeCombo.setPopupVisible(false);
            }
            if (diTypeCombo.getSelectedIndex() > 0) {
                discountInput.requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!diTypeCombo.isPopupVisible()) {
                diTypeCombo.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!diTypeCombo.isPopupVisible()) {
                btnUpdate.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, diTypeCombo);
        }
    }//GEN-LAST:event_diTypeComboKeyPressed

    private void diTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diTypeComboActionPerformed
        if (diTypeCombo.getSelectedIndex() > 0 && !diTypeCombo.isPopupVisible()) {
            discountInput.requestFocusInWindow();
        }
    }//GEN-LAST:event_diTypeComboActionPerformed

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
            java.util.logging.Logger.getLogger(CreditDiscount.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CreditDiscount.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CreditDiscount.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CreditDiscount.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CreditDiscount dialog = new CreditDiscount(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnAddNewCustomer;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> comboCustomer3;
    private javax.swing.JComboBox<String> diTypeCombo;
    private javax.swing.JTextField discountInput;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables
}
