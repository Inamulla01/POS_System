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
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import raven.toast.Notifications;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 *
 * @author moham
 */
public class AddCredit extends javax.swing.JDialog {

    private int customerId = -1;
    private int creditId = -1;
    private Double amount;
    private Map<String, Integer> customerIdMap = new HashMap<>();
    private Map<Integer, String> customerDisplayMap = new HashMap<>();
    private boolean isSaving = false;
    private int salesId = -1;
    private double paidAmount = 0.0;
    private boolean isCreditSaved = false; // Track if credit was saved

    public AddCredit(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    public AddCredit(java.awt.Frame parent, boolean modal, Double amount) {
        super(parent, modal);
        this.amount = amount;
        initComponents();
        initializeDialog(amount);
    }

    public AddCredit(java.awt.Frame parent, boolean modal, Double amount, int salesId) {
        super(parent, modal);
        this.amount = amount;
        this.salesId = salesId;
        initComponents();
        initializeDialog(amount);
    }

    public int getSelectedCustomerId() {
        return customerId;
    }

    public int getCreatedCreditId() {
        return creditId;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public boolean isCreditSaved() {
        return isCreditSaved; // Return our tracked flag
    }

    public boolean isPaymentMade() {
        return paidAmount > 0;
    }

    private void initializeDialog() {
        initializeDialog(null);
    }

    private void initializeDialog(Double amount) {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        loadCustomerCombo();
        AutoCompleteDecorator.decorate(customerCombo);

        givenDate.setDate(new Date());

        setupFocusTraversal();

        if (amount != null) {
            amountField.setText(String.valueOf(amount));
        } else {
            amountField.setText("0");
        }

        customerCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                customerCombo.showPopup();
            }
        });

        getRootPane().registerKeyboardAction(
                evt -> {
                    deleteSalesIfNotSaved();
                    openCreditPayDialog();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> {
                    deleteSalesIfNotSaved();
                    openAddNewCustomer();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> {
                    deleteSalesIfNotSaved();
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        customerCombo.requestFocusInWindow();
    }

    private void deleteSalesIfNotSaved() {
        // Only delete if salesId exists and credit was NOT saved
        if (salesId != -1 && !isCreditSaved) {
            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;

            try {
                conn = MySQL.getConnection();
                conn.setAutoCommit(false); // Start transaction

                // First, check if the sale still exists
                String checkSaleSql = "SELECT COUNT(*) FROM sales WHERE sales_id = ?";
                pst = conn.prepareStatement(checkSaleSql);
                pst.setInt(1, salesId);
                rs = pst.executeQuery();

                if (rs.next() && rs.getInt(1) == 0) {
                    // Sale doesn't exist anymore, nothing to do
                    conn.rollback();
                    return;
                }
                rs.close();
                pst.close();

                // Get all sale items for this sales_id to return them to stock
                String getSaleItemsSql = "SELECT si.stock_id, si.qty FROM sale_item si WHERE si.sales_id = ?";
                pst = conn.prepareStatement(getSaleItemsSql);
                pst.setInt(1, salesId);
                rs = pst.executeQuery();

                // Store the items to return to stock
                java.util.List<java.util.Map<String, Integer>> itemsToReturn = new java.util.ArrayList<>();
                while (rs.next()) {
                    java.util.Map<String, Integer> item = new java.util.HashMap<>();
                    item.put("stock_id", rs.getInt("stock_id"));
                    item.put("qty", rs.getInt("qty"));
                    itemsToReturn.add(item);
                }
                rs.close();
                pst.close();

                // Return each item to stock
                String updateStockSql = "UPDATE stock SET qty = qty + ? WHERE stock_id = ?";
                for (java.util.Map<String, Integer> item : itemsToReturn) {
                    pst = conn.prepareStatement(updateStockSql);
                    pst.setInt(1, item.get("qty"));
                    pst.setInt(2, item.get("stock_id"));
                    pst.executeUpdate();
                    pst.close();
                }

                // Delete related records in correct order to maintain referential integrity
                String[] deleteQueries = {
                    "DELETE FROM cheque WHERE sales_id = ?",
                    "DELETE FROM card_pay WHERE sales_id = ?",
                    "DELETE FROM stock_loss WHERE sales_id = ?",
                    "DELETE FROM sale_item WHERE sales_id = ?",
                    "DELETE FROM sales WHERE sales_id = ?"
                };

                boolean success = true;
                for (String query : deleteQueries) {
                    try {
                        pst = conn.prepareStatement(query);
                        pst.setInt(1, salesId);
                        pst.executeUpdate();
                        pst.close();
                    } catch (Exception e) {
                        System.err.println("Error executing query: " + query + " - " + e.getMessage());
                        success = false;
                        break;
                    }
                }

                if (success) {
                    conn.commit();
                    System.out.println("Successfully deleted sale #" + salesId + " and returned stock");
                } else {
                    conn.rollback();
                    System.err.println("Failed to delete sale #" + salesId + ", transaction rolled back");
                }

            } catch (Exception e) {
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (Exception rollbackEx) {
                    // Rollback exception ignored
                }

                // Show error notification to user
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Error deleting sale: " + e.getMessage());
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (pst != null) {
                        pst.close();
                    }
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (Exception e) {
                    // Closing resources exception ignored
                }
            }
        }
    }

    private void setupKeyboardNavigation() {
        customerCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    givenDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    givenDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    saveBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, customerCombo);
                }
            }
        });

        javax.swing.JTextField givenDateEditor = (javax.swing.JTextField) givenDate.getDateEditor().getUiComponent();
        givenDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    finalDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    customerCombo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    finalDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    customerCombo.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, givenDateEditor);
                }
            }
        });

        javax.swing.JTextField finalDateEditor = (javax.swing.JTextField) finalDate.getDateEditor().getUiComponent();
        finalDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    amountField.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    givenDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    amountField.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    givenDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, finalDateEditor);
                }
            }
        });

        amountField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    finalDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    finalDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, amountField);
                }
            }
        });

        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveCredit();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    amountField.requestFocus();
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

        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearForm();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    amountField.requestFocus();
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

        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    deleteSalesIfNotSaved();
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    amountField.requestFocus();
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
        if (source == customerCombo) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == givenDate.getDateEditor().getUiComponent()) {
            finalDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == finalDate.getDateEditor().getUiComponent()) {
            amountField.requestFocusInWindow();
        } else if (source == amountField) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            customerCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == customerCombo) {
            saveBtn.requestFocusInWindow();
        } else if (source == givenDate.getDateEditor().getUiComponent()) {
            customerCombo.requestFocusInWindow();
        } else if (source == finalDate.getDateEditor().getUiComponent()) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == amountField) {
            finalDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            amountField.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == customerCombo) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == givenDate.getDateEditor().getUiComponent()) {
            finalDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == finalDate.getDateEditor().getUiComponent()) {
            amountField.requestFocusInWindow();
        } else if (source == amountField) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            customerCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == customerCombo) {
            saveBtn.requestFocusInWindow();
        } else if (source == givenDate.getDateEditor().getUiComponent()) {
            customerCombo.requestFocusInWindow();
        } else if (source == finalDate.getDateEditor().getUiComponent()) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == amountField) {
            finalDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            amountField.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return customerCombo.getSelectedIndex() > 0
                && givenDate.getDate() != null
                && finalDate.getDate() != null
                && !amountField.getText().trim().isEmpty()
                && !amountField.getText().trim().equals("0");
    }

    private void setupButtonStyles() {
        setupGradientButton(saveBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(saveIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        creditPayBtn.setBorderPainted(false);
        creditPayBtn.setContentAreaFilled(false);
        creditPayBtn.setFocusPainted(false);
        creditPayBtn.setOpaque(false);
        creditPayBtn.setFocusable(false);
        creditPayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon creditIcon = new FlatSVGIcon("lk/com/pos/icon/money-add.svg", 25, 25);
        creditIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        creditPayBtn.setIcon(creditIcon);

        addNewCustomer.setBorderPainted(false);
        addNewCustomer.setContentAreaFilled(false);
        addNewCustomer.setFocusPainted(false);
        addNewCustomer.setOpaque(false);
        addNewCustomer.setFocusable(false);
        addNewCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon customerIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
        customerIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewCustomer.setIcon(customerIcon);

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
        customerCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field<br>Press <b>F2</b> to add new customer</html>");
        givenDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 01012024 for 01/01/2024</html>");
        finalDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 31122024 for 31/12/2024</html>");
        amountField.setToolTipText("Type credit amount and press ENTER to move to next field");
        creditPayBtn.setToolTipText("Click to open credit payment dialog (or press F1)");
        addNewCustomer.setToolTipText("Click to add new customer (or press F2)");
        saveBtn.setToolTipText("Click to save credit (or press ENTER when focused)");
        clearFormBtn.setToolTipText("Click to clear form (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
    }

    private void loadCustomerCombo() {
        try {
            customerIdMap.clear();
            customerDisplayMap.clear();

            String sql = "SELECT cc.customer_id, cc.customer_name, "
                    + "COALESCE(SUM(c.credit_amout), 0) as total_credit, "
                    + "COALESCE(SUM(cp.credit_pay_amount), 0) as total_paid, "
                    + "(COALESCE(SUM(c.credit_amout), 0) - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount, "
                    + "MAX(c.credit_final_date) as latest_due_date "
                    + "FROM credit_customer cc "
                    + "LEFT JOIN credit c ON cc.customer_id = c.credit_customer_id "
                    + "LEFT JOIN credit_pay cp ON cc.customer_id = cp.credit_customer_id "
                    + "WHERE cc.status_id = 1 "
                    + "GROUP BY cc.customer_id, cc.customer_name "
                    + "ORDER BY cc.customer_name";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> customers = new Vector<>();
            customers.add("Select Customer");

            int count = 0;
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                double totalCredit = rs.getDouble("total_credit");
                double totalPaid = rs.getDouble("total_paid");
                double remainingAmount = rs.getDouble("remaining_amount");
                Date latestDueDate = rs.getDate("latest_due_date");

                String displayText;
                if (totalCredit > 0) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    String dueDateStr = latestDueDate != null ? dateFormat.format(latestDueDate) : "No Due Date";

                    displayText = String.format("%s | Total: Rs %.2f | Paid: Rs %.2f | Due: Rs %.2f | Due Date: %s",
                            customerName, totalCredit, totalPaid, remainingAmount, dueDateStr);
                } else {
                    displayText = String.format("%s | No Credit History", customerName);
                }

                customers.add(displayText);
                customerIdMap.put(displayText, customerId);
                customerDisplayMap.put(customerId, displayText);
                count++;
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(customers);
            customerCombo.setModel(dcm);

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading customers: " + e.getMessage());
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
        if (customerCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a customer");
            customerCombo.requestFocus();
            return false;
        }

        if (givenDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select credit given date");
            givenDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        if (finalDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select credit final date");
            finalDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        if (finalDate.getDate().before(givenDate.getDate())) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Final date must be after given date");
            finalDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Credit amount must be greater than 0");
                amountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid credit amount");
            amountField.requestFocus();
            return false;
        }

        return true;
    }

    private void saveCredit() {
        if (isSaving) {
            return;
        }

        if (!validateInputs()) {
            return;
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet generatedKeys = null;

        try {
            isSaving = true;

            String selectedDisplayText = (String) customerCombo.getSelectedItem();
            this.customerId = getCustomerId(selectedDisplayText);
            if (this.customerId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid customer selected");
                isSaving = false;
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String finalDateStr = dateFormat.format(finalDate.getDate());

            java.util.Date givenDateValue = givenDate.getDate();
            if (givenDateValue != null) {
                java.util.Date currentDateTime = new java.util.Date();
                java.util.Calendar dateCal = java.util.Calendar.getInstance();
                java.util.Calendar timeCal = java.util.Calendar.getInstance();

                dateCal.setTime(givenDateValue);
                timeCal.setTime(currentDateTime);

                dateCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
                dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
                dateCal.set(java.util.Calendar.SECOND, timeCal.get(java.util.Calendar.SECOND));
                dateCal.set(java.util.Calendar.MILLISECOND, timeCal.get(java.util.Calendar.MILLISECOND));

                givenDateValue = dateCal.getTime();
            }

            String givenDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(givenDateValue);
            double amount = Double.parseDouble(amountField.getText().trim());

            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

            String updateCustomerSql = "UPDATE credit_customer SET status_id = 1 WHERE customer_id = ?";
            PreparedStatement pstUpdateCustomer = conn.prepareStatement(updateCustomerSql);
            pstUpdateCustomer.setInt(1, this.customerId);
            pstUpdateCustomer.executeUpdate();
            pstUpdateCustomer.close();

            String query = "INSERT INTO credit (credit_given_date, credit_final_date, credit_amout, credit_customer_id, sales_id) "
                    + "VALUES (?, ?, ?, ?, ?)";

            pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, givenDateStr);
            pst.setString(2, finalDateStr);
            pst.setDouble(3, amount);
            pst.setInt(4, this.customerId);
            if (salesId != -1) {
                pst.setInt(5, salesId);
            } else {
                pst.setNull(5, java.sql.Types.INTEGER);
            }

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    this.creditId = generatedKeys.getInt(1);
                }

                createCreditNotification(selectedDisplayText, amount, conn);
                conn.commit();

                // Mark that credit was successfully saved
                isCreditSaved = true;

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Credit added successfully!");

                askToOpenCreditPayDialog();

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
                // Rollback exception ignored
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving credit: " + e.getMessage());
        } finally {
            try {
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                // Closing resources exception ignored
            }
            isSaving = false;
        }
    }

    private void askToOpenCreditPayDialog() {
        Object[] options = {"Yes", "No"};
        JOptionPane optionPane = new JOptionPane(
                "Credit added successfully! Do you want to add a payment for this credit?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]
        );

        JDialog dialog = optionPane.createDialog(this, "Open Credit Payment");

        dialog.getRootPane().registerKeyboardAction(
                evt -> {
                    optionPane.setValue(options[0]);
                    dialog.dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.getRootPane().registerKeyboardAction(
                evt -> {
                    optionPane.setValue(options[1]);
                    dialog.dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.getRootPane().registerKeyboardAction(
                evt -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.setVisible(true);
        dialog.dispose();

        Object result = optionPane.getValue();

        if (result == options[0]) {
            dispose();
            openCreditPayDialogForCustomer();
        } else {
            dispose();
        }
    }

    private void openCreditPayDialogForCustomer() {
        try {
            AddCreditPay dialog = new AddCreditPay(null, true, this.customerId);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            double paymentAmount = dialog.getPaidAmount();
            if (paymentAmount > 0) {
                this.paidAmount = paymentAmount;
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "Payment of Rs " + paymentAmount + " recorded successfully!");
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening credit payment dialog: " + e.getMessage());
        }
    }

    private void createCreditNotification(String customerName, double amount, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            String cleanCustomerName = customerName.split("\\|")[0].trim();
            String messageText = "New credit added for " + cleanCustomerName + ": Rs." + String.format("%,.2f", amount);

            String checkSql = "SELECT COUNT(*) FROM massage WHERE massage = ?";
            pstMassage = conn.prepareStatement(checkSql);
            pstMassage.setString(1, messageText);
            ResultSet rs = pstMassage.executeQuery();

            int massageId;
            if (rs.next() && rs.getInt(1) > 0) {
                String getSql = "SELECT massage_id FROM massage WHERE massage = ?";
                pstMassage.close();
                pstMassage = conn.prepareStatement(getSql);
                pstMassage.setString(1, messageText);
                rs = pstMassage.executeQuery();
                rs.next();
                massageId = rs.getInt(1);
            } else {
                pstMassage.close();
                String insertMassageSql = "INSERT INTO massage (massage) VALUES (?)";
                pstMassage = conn.prepareStatement(insertMassageSql, PreparedStatement.RETURN_GENERATED_KEYS);
                pstMassage.setString(1, messageText);
                pstMassage.executeUpdate();

                rs = pstMassage.getGeneratedKeys();
                if (rs.next()) {
                    massageId = rs.getInt(1);
                } else {
                    throw new Exception("Failed to get generated massage ID");
                }
            }

            String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)";
            pstNotification = conn.prepareStatement(notificationSql);
            pstNotification.setInt(1, 1);
            pstNotification.setInt(2, 11);
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (Exception e) {
            // Error handled silently as notification creation is not critical
        } finally {
            try {
                if (pstMassage != null) {
                    pstMassage.close();
                }
                if (pstNotification != null) {
                    pstNotification.close();
                }
            } catch (Exception e) {
                // Closing resources exception ignored
            }
        }
    }

    private void clearForm() {
        customerCombo.setSelectedIndex(0);
        givenDate.setDate(new Date());
        finalDate.setDate(null);
        amountField.setText("0");
        customerCombo.requestFocus();
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
            AddNewCustomer dialog = new AddNewCustomer((JFrame) getParent(), true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            if (dialog.isCustomerSaved()) {
                int newCustomerId = dialog.getSavedCustomerId();
                String newCustomerName = dialog.getSavedCustomerName();

                loadCustomerCombo();

                String displayText = customerDisplayMap.get(newCustomerId);
                if (displayText != null) {
                    customerCombo.setSelectedItem(displayText);
                }

                customerCombo.requestFocus();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "New customer '" + newCustomerName + "' added and selected!");
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening customer dialog: " + e.getMessage());
        }
    }

    private void setupFocusTraversal() {
        creditPayBtn.setFocusable(false);
        addNewCustomer.setFocusable(false);

        givenDate.getDateEditor().getUiComponent().setFocusable(true);
        finalDate.getDateEditor().getUiComponent().setFocusable(true);
    }

    // Override dispose to handle sales deletion when dialog is closed
    @Override
    public void dispose() {
        // Only delete if we haven't saved a credit
        if (!isCreditSaved) {
            deleteSalesIfNotSaved();
        }
        super.dispose();
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
        customerCombo = new javax.swing.JComboBox<>();
        givenDate = new com.toedter.calendar.JDateChooser();
        finalDate = new com.toedter.calendar.JDateChooser();
        amountField = new javax.swing.JTextField();
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

        customerCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        customerCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        customerCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customer *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        customerCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerComboActionPerformed(evt);
            }
        });
        customerCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                customerComboKeyPressed(evt);
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

        finalDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Final Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        finalDate.setDateFormatString("MM/dd/yyyy");
        finalDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        finalDate.setOpaque(false);
        finalDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                finalDateKeyPressed(evt);
            }
        });

        amountField.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        amountField.setText("0");
        amountField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        amountField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                amountFieldActionPerformed(evt);
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
                        .addComponent(customerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
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
                        .addGap(21, 21, 21))))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(21, 21, 21)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(amountField, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(addNewCustomer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(givenDate, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(finalDate, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGap(21, 21, 21)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(creditPayBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(customerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(135, 135, 135)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(88, 88, 88)
                    .addComponent(addNewCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(givenDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(finalDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(13, 13, 13)
                    .addComponent(amountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            deleteSalesIfNotSaved();
            openCreditPayDialog();
        }
    }//GEN-LAST:event_creditPayBtnKeyPressed

    private void creditPayBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditPayBtnActionPerformed
        deleteSalesIfNotSaved();
        openCreditPayDialog();
    }//GEN-LAST:event_creditPayBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
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
            deleteSalesIfNotSaved();
            dispose();
        } else {
            handleArrowNavigation(evt, cancelBtn);
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        deleteSalesIfNotSaved();
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void customerComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerComboActionPerformed
        if (customerCombo.getSelectedIndex() > 0 && !customerCombo.isPopupVisible()) {
            givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
        }
    }//GEN-LAST:event_customerComboActionPerformed

    private void customerComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_customerComboKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (customerCombo.isPopupVisible()) {
                customerCombo.setPopupVisible(false);
            }
            if (customerCombo.getSelectedIndex() > 0) {
                givenDate.getDateEditor().getUiComponent().requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!customerCombo.isPopupVisible()) {
                customerCombo.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!customerCombo.isPopupVisible()) {
                saveBtn.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, customerCombo);
        }
    }//GEN-LAST:event_customerComboKeyPressed

    private void givenDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_givenDateKeyPressed
        handleArrowNavigation(evt, givenDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_givenDateKeyPressed

    private void finalDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_finalDateKeyPressed
        handleArrowNavigation(evt, finalDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_finalDateKeyPressed

    private void amountFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_amountFieldActionPerformed

    }//GEN-LAST:event_amountFieldActionPerformed

    private void addNewCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCustomerActionPerformed
        deleteSalesIfNotSaved();
        openAddNewCustomer();
    }//GEN-LAST:event_addNewCustomerActionPerformed

    private void addNewCustomerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewCustomerKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F2) {
            deleteSalesIfNotSaved();
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
    private javax.swing.JButton addNewCustomer;
    private javax.swing.JTextField amountField;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JButton creditPayBtn;
    private javax.swing.JComboBox<String> customerCombo;
    private com.toedter.calendar.JDateChooser finalDate;
    private com.toedter.calendar.JDateChooser givenDate;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
