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
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import lk.com.pos.connection.DB;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class AddCheque extends javax.swing.JDialog {

    private int customerId = -1;
    private int salesId = -1;
    private Map<String, Integer> customerIdMap = new HashMap<>();
    private Map<Integer, String> customerDisplayMap = new HashMap<>();
    private boolean isSaving = false;
    private double dueAmount = 0.0;
    private double totalCredit = 0.0;
    private double totalPaid = 0.0;
    private Date latestDueDate = null;
    private double chequeAmount = 0.0;
    private boolean isChequeSaved = false; // Track if cheque was saved

    public AddCheque(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    public AddCheque(java.awt.Frame parent, boolean modal, int salesId) {
        super(parent, modal);
        this.salesId = salesId;
        initComponents();
        initializeDialog();
    }

    public AddCheque(java.awt.Frame parent, boolean modal, int salesId, double chequeAmount) {
        super(parent, modal);
        this.salesId = salesId;
        this.chequeAmount = chequeAmount;
        initComponents();
        initializeDialog();
    }

    public int getSelectedCustomerId() {
        return customerId;
    }

    public String getChequeNo() {
        return txtChequeNo.getText().trim();
    }

    public Date getChequeDate() {
        return dateChequeDate.getDate();
    }

    public String getBankName() {
        return txtBankName.getText().trim();
    }

    public String getBranch() {
        return txtBranch.getText().trim();
    }

    public double getChequeAmount() {
        try {
            return Double.parseDouble(txtAmount.getText().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public boolean isChequeSaved() {
        return isChequeSaved;
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        loadCustomerCombo();
        AutoCompleteDecorator.decorate(comboCustomer);

        dateChequeDate.setDate(new Date());

        // Set amount if provided in constructor (when salesId is passed)
        if (salesId != -1 && chequeAmount > 0) {
            txtAmount.setText(String.valueOf(chequeAmount));
            txtAmount.setEditable(false); // Make amount non-editable when salesId is passed
        } else {
            txtAmount.setText(""); // Clear amount field when no salesId
            txtAmount.setEditable(true); // Allow user to type amount
        }

        setupFocusTraversal();

        comboCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                comboCustomer.showPopup();
            }
        });

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

        comboCustomer.requestFocusInWindow();
    }

    private void deleteSalesIfNotSaved() {
        // Only delete if salesId exists and cheque was NOT saved
        if (salesId != -1 && !isChequeSaved) {
            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;

            try {
                conn = DB.getConnection();
                conn.setAutoCommit(false);

                String checkSaleSql = "SELECT COUNT(*) FROM sales WHERE sales_id = ?";
                pst = conn.prepareStatement(checkSaleSql);
                pst.setInt(1, salesId);
                rs = pst.executeQuery();

                if (rs.next() && rs.getInt(1) == 0) {
                    conn.rollback();
                    return;
                }
                rs.close();
                pst.close();

                String getSaleItemsSql = "SELECT si.stock_id, si.qty FROM sale_item si WHERE si.sales_id = ?";
                pst = conn.prepareStatement(getSaleItemsSql);
                pst.setInt(1, salesId);
                rs = pst.executeQuery();

                java.util.List<java.util.Map<String, Integer>> itemsToReturn = new java.util.ArrayList<>();
                while (rs.next()) {
                    java.util.Map<String, Integer> item = new java.util.HashMap<>();
                    item.put("stock_id", rs.getInt("stock_id"));
                    item.put("qty", rs.getInt("qty"));
                    itemsToReturn.add(item);
                }
                rs.close();
                pst.close();

                String updateStockSql = "UPDATE stock SET qty = qty + ? WHERE stock_id = ?";
                for (java.util.Map<String, Integer> item : itemsToReturn) {
                    pst = conn.prepareStatement(updateStockSql);
                    pst.setInt(1, item.get("qty"));
                    pst.setInt(2, item.get("stock_id"));
                    pst.executeUpdate();
                    pst.close();
                }

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
                        success = false;
                        break;
                    }
                }

                if (success) {
                    conn.commit();
                } else {
                    conn.rollback();
                }

            } catch (Exception e) {
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (Exception rollbackEx) {
                }

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
                }
            }
        }
    }

    private void setupKeyboardNavigation() {
        comboCustomer.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtChequeNo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    txtChequeNo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    btnSave.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, comboCustomer);
                }
            }
        });

        txtChequeNo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtAmount.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    comboCustomer.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    txtAmount.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    comboCustomer.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, txtChequeNo);
                }
            }
        });

        txtAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    dateChequeDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtChequeNo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    dateChequeDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    txtChequeNo.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, txtAmount);
                }
            }
        });

        javax.swing.JTextField dateChequeDateEditor = (javax.swing.JTextField) dateChequeDate.getDateEditor().getUiComponent();
        dateChequeDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtBankName.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtAmount.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    txtBankName.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    txtAmount.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, dateChequeDateEditor);
                }
            }
        });

        txtBankName.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtBranch.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    dateChequeDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    txtBranch.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    dateChequeDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, txtBankName);
                }
            }
        });

        txtBranch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (areAllRequiredFieldsFilled()) {
                        btnSave.requestFocusInWindow();
                    } else {
                        btnCancel.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtBankName.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (areAllRequiredFieldsFilled()) {
                        btnSave.requestFocusInWindow();
                    } else {
                        btnCancel.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    txtBankName.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, txtBranch);
                }
            }
        });

        btnSave.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume();
                    saveCheque();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtBranch.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnClear.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnClear.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnClear.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, btnSave);
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
                    txtBranch.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnCancel.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnSave.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnCancel.requestFocus();
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
                    deleteSalesIfNotSaved();
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtBranch.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnSave.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnClear.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnSave.requestFocus();
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
        if (source == comboCustomer) {
            txtChequeNo.requestFocusInWindow();
        } else if (source == txtChequeNo) {
            txtAmount.requestFocusInWindow();
        } else if (source == txtAmount) {
            dateChequeDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == dateChequeDate.getDateEditor().getUiComponent()) {
            txtBankName.requestFocusInWindow();
        } else if (source == txtBankName) {
            txtBranch.requestFocusInWindow();
        } else if (source == txtBranch) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnCancel) {
            btnClear.requestFocusInWindow();
        } else if (source == btnClear) {
            btnSave.requestFocusInWindow();
        } else if (source == btnSave) {
            comboCustomer.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == comboCustomer) {
            btnSave.requestFocusInWindow();
        } else if (source == txtChequeNo) {
            comboCustomer.requestFocusInWindow();
        } else if (source == txtAmount) {
            txtChequeNo.requestFocusInWindow();
        } else if (source == dateChequeDate.getDateEditor().getUiComponent()) {
            txtAmount.requestFocusInWindow();
        } else if (source == txtBankName) {
            dateChequeDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == txtBranch) {
            txtBankName.requestFocusInWindow();
        } else if (source == btnCancel) {
            txtBranch.requestFocusInWindow();
        } else if (source == btnClear) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnSave) {
            btnClear.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == comboCustomer) {
            txtChequeNo.requestFocusInWindow();
        } else if (source == txtChequeNo) {
            txtAmount.requestFocusInWindow();
        } else if (source == txtAmount) {
            dateChequeDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == dateChequeDate.getDateEditor().getUiComponent()) {
            txtBankName.requestFocusInWindow();
        } else if (source == txtBankName) {
            txtBranch.requestFocusInWindow();
        } else if (source == txtBranch) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnCancel) {
            btnClear.requestFocusInWindow();
        } else if (source == btnClear) {
            btnSave.requestFocusInWindow();
        } else if (source == btnSave) {
            comboCustomer.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == comboCustomer) {
            btnSave.requestFocusInWindow();
        } else if (source == txtChequeNo) {
            comboCustomer.requestFocusInWindow();
        } else if (source == txtAmount) {
            txtChequeNo.requestFocusInWindow();
        } else if (source == dateChequeDate.getDateEditor().getUiComponent()) {
            txtAmount.requestFocusInWindow();
        } else if (source == txtBankName) {
            dateChequeDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == txtBranch) {
            txtBankName.requestFocusInWindow();
        } else if (source == btnCancel) {
            txtBranch.requestFocusInWindow();
        } else if (source == btnClear) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnSave) {
            btnClear.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return comboCustomer.getSelectedIndex() > 0
                && !txtChequeNo.getText().trim().isEmpty()
                && !txtAmount.getText().trim().isEmpty()
                && dateChequeDate.getDate() != null
                && !txtBankName.getText().trim().isEmpty()
                && !txtBranch.getText().trim().isEmpty();
    }

    private void setupButtonStyles() {
        setupGradientButton(btnSave);
        setupGradientButton(btnClear);
        setupGradientButton(btnCancel);

        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnSave.setIcon(saveIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnClear.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
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
        btnSave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSave.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnSave.setIcon(hoverIcon);
                btnSave.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSave.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnSave.setIcon(normalIcon);
                btnSave.repaint();
            }
        });

        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnClear.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnClear.setIcon(hoverIcon);
                btnClear.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnClear.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnClear.setIcon(normalIcon);
                btnClear.repaint();
            }
        });

        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCancel.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnCancel.setIcon(hoverIcon);
                btnCancel.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCancel.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
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
        btnSave.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnSave.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnSave.setIcon(focusedIcon);
                btnSave.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                btnSave.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnSave.setIcon(normalIcon);
                btnSave.repaint();
            }
        });

        btnClear.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnClear.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnClear.setIcon(focusedIcon);
                btnClear.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                btnClear.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnClear.setIcon(normalIcon);
                btnClear.repaint();
            }
        });

        btnCancel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnCancel.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btnCancel.setIcon(focusedIcon);
                btnCancel.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                btnCancel.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
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
        comboCustomer.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field<br>Press <b>F2</b> to add new customer</html>");
        txtChequeNo.setToolTipText("Enter cheque number and press ENTER to move to next field");
        txtAmount.setToolTipText("Enter cheque amount and press ENTER to move to next field");
        dateChequeDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 01012024 for 01/01/2024</html>");
        txtBankName.setToolTipText("Enter bank name and press ENTER to move to next field");
        txtBranch.setToolTipText("Enter branch name and press ENTER to move to next field");
        btnAddNewCustomer.setToolTipText("Click to add new customer (or press F2)");
        btnSave.setToolTipText("Click to save cheque (or press ENTER when focused)");
        btnClear.setToolTipText("Click to clear form (or press ENTER when focused)");
        btnCancel.setToolTipText("Click to cancel (or press ESC)");
    }

    private void loadCustomerCombo() {
        try {
            customerIdMap.clear();
            customerDisplayMap.clear();

            String sql = "SELECT cc.customer_id, cc.customer_name, "
                    + "COALESCE(SUM(c.credit_amout), 0) as total_credit, "
                    + "COALESCE(SUM(cp.credit_pay_amount), 0) as total_paid, "
                    + "(COALESCE(SUM(c.credit_amout), 0) - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount, "
                    + "MAX(c.credit_final_date) as latest_due_date, "
                    + "COUNT(ch.cheque_id) as active_cheques "
                    + "FROM credit_customer cc "
                    + "LEFT JOIN credit c ON cc.customer_id = c.credit_customer_id "
                    + "LEFT JOIN credit_pay cp ON cc.customer_id = cp.credit_customer_id "
                    + "LEFT JOIN cheque ch ON cc.customer_id = ch.credit_customer_id AND ch.cheque_date >= CURDATE() "
                    + "WHERE cc.status_id = 1 "
                    + "GROUP BY cc.customer_id, cc.customer_name "
                    + "ORDER BY cc.customer_name";

            try (Connection conn = DB.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                
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

                    customers.add(displayText);
                    customerIdMap.put(displayText, customerId);
                    customerDisplayMap.put(customerId, displayText);
                    count++;
                }

                DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(customers);
                comboCustomer.setModel(dcm);
            }

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading customers: " + e.getMessage());
        }
    }

    private void loadCustomerCreditDetails(int customerId) {
        try {
            String sql = "SELECT "
                    + "COALESCE(SUM(c.credit_amout), 0) as total_credit, "
                    + "COALESCE(SUM(cp.credit_pay_amount), 0) as total_paid, "
                    + "(COALESCE(SUM(c.credit_amout), 0) - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount, "
                    + "MAX(c.credit_final_date) as latest_due_date, "
                    + "COUNT(ch.cheque_id) as active_cheques "
                    + "FROM credit_customer cc "
                    + "LEFT JOIN credit c ON cc.customer_id = c.credit_customer_id "
                    + "LEFT JOIN credit_pay cp ON cc.customer_id = cp.credit_customer_id "
                    + "LEFT JOIN cheque ch ON cc.customer_id = ch.credit_customer_id AND ch.cheque_date >= CURDATE() "
                    + "WHERE cc.customer_id = ? "
                    + "GROUP BY cc.customer_id";

            try (Connection conn = DB.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, customerId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        this.totalCredit = rs.getDouble("total_credit");
                        this.totalPaid = rs.getDouble("total_paid");
                        this.dueAmount = rs.getDouble("remaining_amount");
                        this.latestDueDate = rs.getDate("latest_due_date");
                        int activeCheques = rs.getInt("active_cheques");
                    }
                }
            }
        } catch (Exception e) {
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
        if (comboCustomer.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a customer");
            comboCustomer.requestFocus();
            return false;
        }

        if (txtChequeNo.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter cheque number");
            txtChequeNo.requestFocus();
            return false;
        }

        if (txtAmount.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter cheque amount");
            txtAmount.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid cheque amount");
                txtAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid cheque amount");
            txtAmount.requestFocus();
            return false;
        }

        if (dateChequeDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select cheque date");
            dateChequeDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        if (txtBankName.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter bank name");
            txtBankName.requestFocus();
            return false;
        }

        if (txtBranch.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter branch");
            txtBranch.requestFocus();
            return false;
        }

        Date currentDate = new Date();
        if (dateChequeDate.getDate().before(currentDate)) {
            int response = JOptionPane.showConfirmDialog(this,
                    "The cheque date is in the past. Do you want to continue?",
                    "Past Cheque Date",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                dateChequeDate.getDateEditor().getUiComponent().requestFocus();
                return false;
            }
        }

        return true;
    }

    private void saveCheque() {
        if (isSaving) {
            return;
        }

        if (!validateInputs()) {
            return;
        }

        Connection conn = null;
        PreparedStatement pst = null;
        PreparedStatement pstCreditPay = null;

        try {
            isSaving = true;

            String selectedDisplayText = (String) comboCustomer.getSelectedItem();
            this.customerId = getCustomerId(selectedDisplayText);
            if (this.customerId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid customer selected");
                isSaving = false;
                return;
            }

            String chequeNo = txtChequeNo.getText().trim();
            double amount = getChequeAmount();
            Date chequeDate = dateChequeDate.getDate();
            String bankName = txtBankName.getText().trim();
            String branch = txtBranch.getText().trim();

            if (isDuplicateChequeNo(chequeNo)) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Cheque number already exists!");
                txtChequeNo.requestFocus();
                isSaving = false;
                return;
            }

            conn = DB.getConnection();
            conn.setAutoCommit(false);

            String chequeQuery = "INSERT INTO cheque (cheque_no, cheque_date, sales_id, credit_customer_id, bank_name, branch, amount) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            pst = conn.prepareStatement(chequeQuery);
            pst.setString(1, chequeNo);
            pst.setDate(2, new java.sql.Date(chequeDate.getTime()));
            if (salesId != -1) {
                pst.setInt(3, salesId);
            } else {
                pst.setNull(3, java.sql.Types.INTEGER);
            }
            pst.setInt(4, this.customerId);
            pst.setString(5, bankName);
            pst.setString(6, branch);
            pst.setDouble(7, amount);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                if (salesId == -1) {
                    String creditPayQuery = "INSERT INTO credit_pay (credit_pay_date, credit_pay_amount, credit_customer_id) "
                            + "VALUES (NOW(), ?, ?)";

                    pstCreditPay = conn.prepareStatement(creditPayQuery);
                    pstCreditPay.setDouble(1, amount);
                    pstCreditPay.setInt(2, this.customerId);

                    int creditPayRows = pstCreditPay.executeUpdate();
                }

                createChequeNotification(selectedDisplayText, chequeNo, amount, chequeDate, bankName, branch, conn);
                conn.commit();

                isChequeSaved = true;

                String successMessage = "Cheque added successfully!";
                if (salesId == -1) {
                    successMessage += " Credit payment of Rs " + amount + " also recorded!";
                }

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, successMessage);

                dispose();

            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to add cheque!");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving cheque: " + e.getMessage());
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (pstCreditPay != null) {
                    pstCreditPay.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
            }
            isSaving = false;
        }
    }

    private boolean isDuplicateChequeNo(String chequeNo) {
        try {
            String sql = "SELECT COUNT(*) FROM cheque WHERE cheque_no = ?";
            try (Connection conn = DB.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, chequeNo);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private void createChequeNotification(String customerName, String chequeNo, double amount, Date chequeDate, String bankName, String branch, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String messageText = "New cheque added for " + customerName
                    + " | Cheque No: " + chequeNo
                    + " | Amount: Rs " + amount
                    + " | Date: " + dateFormat.format(chequeDate)
                    + " | Bank: " + bankName
                    + " | Branch: " + branch;

            if (salesId == -1) {
                messageText += " | Credit Payment Recorded";
            }

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
            pstNotification.setInt(2, 30);
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (Exception e) {
        } finally {
            try {
                if (pstMassage != null) {
                    pstMassage.close();
                }
                if (pstNotification != null) {
                    pstNotification.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private void clearForm() {
        comboCustomer.setSelectedIndex(0);
        txtChequeNo.setText("");
        txtAmount.setText("");
        dateChequeDate.setDate(new Date());
        txtBankName.setText("");
        txtBranch.setText("");
        comboCustomer.requestFocus();
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
                    comboCustomer.setSelectedItem(displayText);
                }

                comboCustomer.requestFocus();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "New customer '" + newCustomerName + "' added and selected!");
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening customer dialog: " + e.getMessage());
        }
    }

    private void setupFocusTraversal() {
        btnAddNewCustomer.setFocusable(false);
        dateChequeDate.getDateEditor().getUiComponent().setFocusable(true);
    }

    @Override
    public void dispose() {
        if (!isChequeSaved) {
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
        btnCancel = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        dateChequeDate = new com.toedter.calendar.JDateChooser();
        txtChequeNo = new javax.swing.JTextField();
        comboCustomer = new javax.swing.JComboBox<>();
        btnAddNewCustomer = new javax.swing.JButton();
        txtBankName = new javax.swing.JTextField();
        txtBranch = new javax.swing.JTextField();
        txtAmount = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setText("Add Cheque Payment");
        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        btnCancel.setText("Cancel");
        btnCancel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnCancel.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(8, 147, 176));
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

        btnClear.setText("Clear Form");
        btnClear.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnClear.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnClear.setForeground(new java.awt.Color(8, 147, 176));
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

        btnSave.setText("Save");
        btnSave.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnSave.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnSave.setForeground(new java.awt.Color(8, 147, 176));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        btnSave.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnSaveKeyPressed(evt);
            }
        });

        dateChequeDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cheque Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        dateChequeDate.setDateFormatString("MM/dd/yyyy");
        dateChequeDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        dateChequeDate.setOpaque(false);
        dateChequeDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dateChequeDateKeyPressed(evt);
            }
        });

        txtChequeNo.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        txtChequeNo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cheque No  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        txtChequeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtChequeNoActionPerformed(evt);
            }
        });

        comboCustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboCustomer.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customer *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        comboCustomer.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        comboCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboCustomerActionPerformed(evt);
            }
        });
        comboCustomer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comboCustomerKeyPressed(evt);
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

        txtBankName.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        txtBankName.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bank Name *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        txtBranch.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        txtBranch.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Branch *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        txtAmount.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        txtAmount.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        txtAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAmountActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateChequeDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(comboCustomer, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddNewCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtChequeNo, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtAmount))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtBankName, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtBranch, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(21, 21, 21))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(comboCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(btnAddNewCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtChequeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dateChequeDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBankName, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBranch, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        deleteSalesIfNotSaved();
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCancelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCancelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            deleteSalesIfNotSaved();
            dispose();
        } else {
            handleArrowNavigation(evt, btnCancel);
        }
    }//GEN-LAST:event_btnCancelKeyPressed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnClearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnClearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        } else {
            handleArrowNavigation(evt, btnClear);
        }
    }//GEN-LAST:event_btnClearKeyPressed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveCheque();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnSaveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnSaveKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            saveCheque();
        } else {
            handleArrowNavigation(evt, btnSave);
        }
    }//GEN-LAST:event_btnSaveKeyPressed

    private void dateChequeDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dateChequeDateKeyPressed
        handleArrowNavigation(evt, dateChequeDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_dateChequeDateKeyPressed

    private void txtChequeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtChequeNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtChequeNoActionPerformed

    private void comboCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboCustomerActionPerformed
        if (comboCustomer.getSelectedIndex() > 0 && !comboCustomer.isPopupVisible()) {
            String selectedDisplayText = (String) comboCustomer.getSelectedItem();
            int selectedCustomerId = getCustomerId(selectedDisplayText);
            if (selectedCustomerId != -1) {
                loadCustomerCreditDetails(selectedCustomerId);
            }
            txtChequeNo.requestFocusInWindow();
        }
    }//GEN-LAST:event_comboCustomerActionPerformed

    private void comboCustomerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboCustomerKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (comboCustomer.isPopupVisible()) {
                comboCustomer.setPopupVisible(false);
            }
            if (comboCustomer.getSelectedIndex() > 0) {
                String selectedDisplayText = (String) comboCustomer.getSelectedItem();
                int selectedCustomerId = getCustomerId(selectedDisplayText);
                if (selectedCustomerId != -1) {
                    loadCustomerCreditDetails(selectedCustomerId);
                }
                txtChequeNo.requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!comboCustomer.isPopupVisible()) {
                comboCustomer.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!comboCustomer.isPopupVisible()) {
                btnSave.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, comboCustomer);
        }
    }//GEN-LAST:event_comboCustomerKeyPressed

    private void btnAddNewCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddNewCustomerActionPerformed
        deleteSalesIfNotSaved();
        openAddNewCustomer();
    }//GEN-LAST:event_btnAddNewCustomerActionPerformed

    private void btnAddNewCustomerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnAddNewCustomerKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F2) {
            deleteSalesIfNotSaved();
            openAddNewCustomer();
        }
    }//GEN-LAST:event_btnAddNewCustomerKeyPressed

    private void txtAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmountActionPerformed

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
            java.util.logging.Logger.getLogger(AddCheque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddCheque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddCheque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddCheque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddCheque dialog = new AddCheque(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> comboCustomer;
    private com.toedter.calendar.JDateChooser dateChequeDate;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtBankName;
    private javax.swing.JTextField txtBranch;
    private javax.swing.JTextField txtChequeNo;
    // End of variables declaration//GEN-END:variables
}
