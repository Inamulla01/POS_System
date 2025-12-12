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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import lk.com.pos.connection.DB;
import lk.com.pos.connection.DB.ResultSetHandler;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class UpdateCheque extends javax.swing.JDialog {

    private int chequeId = -1;
    private int customerId = -1;
    private int originalSalesId = -1;
    private Map<String, Integer> customerIdMap = new HashMap<>();
    private boolean isUpdating = false;
    private double dueAmount = 0.0;
    private double totalCredit = 0.0;
    private double totalPaid = 0.0;
    private Date latestDueDate = null;

    public UpdateCheque(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    public UpdateCheque(java.awt.Frame parent, boolean modal, int chequeId) {
        super(parent, modal);
        this.chequeId = chequeId;
        initComponents();
        initializeDialog();
        loadChequeData();
    }

    public boolean isChequeUpdated() {
        return chequeId != -1;
    }

    public double getChequeAmount() {
        try {
            return Double.parseDouble(txtAmount.getText().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        loadCustomerCombo();
        AutoCompleteDecorator.decorate(comboCustomer);

        setupFocusTraversal();

        comboCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                comboCustomer.showPopup();
            }
        });

        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        comboCustomer.requestFocusInWindow();
    }

    private void loadChequeData() {
        if (chequeId == -1) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "No cheque ID provided");
            dispose();
            return;
        }

        try {
            String sql = "SELECT ch.cheque_no, ch.cheque_date, ch.sales_id, ch.credit_customer_id, "
                    + "ch.bank_name, ch.branch, ch.amount, cc.customer_name "
                    + "FROM cheque ch "
                    + "JOIN credit_customer cc ON ch.credit_customer_id = cc.customer_id "
                    + "WHERE ch.cheque_id = ?";

            // Using the new DB class with try-with-resources
            try (Connection conn = DB.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                
                pst.setInt(1, chequeId);
                
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        // Populate form fields
                        txtChequeNo.setText(rs.getString("cheque_no"));
                        dateChequeDate.setDate(rs.getDate("cheque_date"));
                        txtBankName.setText(rs.getString("bank_name"));
                        txtBranch.setText(rs.getString("branch"));

                        // Load and set the amount
                        double amount = rs.getDouble("amount");
                        txtAmount.setText(String.valueOf(amount));

                        this.originalSalesId = rs.getInt("sales_id");
                        this.customerId = rs.getInt("credit_customer_id");

                        String customerName = rs.getString("customer_name");

                        // Find and select the customer in the combo box
                        selectCustomerInCombo(customerName);

                        // Load customer credit details
                        loadCustomerCreditDetails(this.customerId);

                    } else {
                        Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Cheque not found!");
                        dispose();
                    }
                }
            }

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error loading cheque data: " + e.getMessage());
            e.printStackTrace();
            dispose();
        }
    }

    private void selectCustomerInCombo(String customerName) {
        for (int i = 0; i < comboCustomer.getItemCount(); i++) {
            String item = comboCustomer.getItemAt(i);
            if (item.contains(customerName + " |")) {
                comboCustomer.setSelectedIndex(i);
                break;
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
                    btnUpdate.requestFocus();
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
                        btnUpdate.requestFocusInWindow();
                    } else {
                        btnCancel.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtBankName.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (areAllRequiredFieldsFilled()) {
                        btnUpdate.requestFocusInWindow();
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

        btnUpdate.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume();
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
                    handleArrowNavigation(evt, btnUpdate);
                }
            }
        });

        btnClear.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    resetForm();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtBranch.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnCancel.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnUpdate.requestFocus();
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
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtBranch.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnUpdate.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnClear.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnUpdate.requestFocus();
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
            btnUpdate.requestFocusInWindow();
        } else if (source == btnUpdate) {
            comboCustomer.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == comboCustomer) {
            btnUpdate.requestFocusInWindow();
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
        } else if (source == btnUpdate) {
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
            btnUpdate.requestFocusInWindow();
        } else if (source == btnUpdate) {
            comboCustomer.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == comboCustomer) {
            btnUpdate.requestFocusInWindow();
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
        } else if (source == btnUpdate) {
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
        setupGradientButton(btnUpdate);
        setupGradientButton(btnClear);
        setupGradientButton(btnCancel);

        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnUpdate.setIcon(updateIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnClear.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnCancel.setIcon(cancelIcon);

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

    }

    private void setupButtonFocusListeners() {
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

    }

    private void setupTooltips() {
        comboCustomer.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field<br>Press <b>F2</b> to add new customer</html>");
        txtChequeNo.setToolTipText("Enter cheque number and press ENTER to move to next field");
        txtAmount.setToolTipText("Enter cheque amount and press ENTER to move to next field");
        dateChequeDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 01012024 for 01/01/2024</html>");
        txtBankName.setToolTipText("Enter bank name and press ENTER to move to next field");
        txtBranch.setToolTipText("Enter branch name and press ENTER to move to next field");
        btnUpdate.setToolTipText("Click to update cheque (or press ENTER when focused)");
        btnClear.setToolTipText("Click to reset form (or press ENTER when focused)");
        btnCancel.setToolTipText("Click to cancel (or press ESC)");
    }

    private void loadCustomerCombo() {
        try {
            customerIdMap.clear();

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

            // Using the new DB class with executeQuerySafe
            java.util.List<java.util.Map<String, Object>> results = DB.executeQuerySafe(sql, new ResultSetHandler<java.util.List<java.util.Map<String, Object>>>() {
                @Override
                public java.util.List<java.util.Map<String, Object>> handle(ResultSet rs) throws SQLException {
                    java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
                    while (rs.next()) {
                        java.util.Map<String, Object> row = new java.util.HashMap<>();
                        row.put("customer_id", rs.getInt("customer_id"));
                        row.put("customer_name", rs.getString("customer_name"));
                        row.put("total_credit", rs.getDouble("total_credit"));
                        row.put("total_paid", rs.getDouble("total_paid"));
                        row.put("remaining_amount", rs.getDouble("remaining_amount"));
                        row.put("latest_due_date", rs.getDate("latest_due_date"));
                        row.put("active_cheques", rs.getInt("active_cheques"));
                        list.add(row);
                    }
                    return list;
                }
            });

            Vector<String> customers = new Vector<>();
            customers.add("Select Customer");

            int count = 0;
            for (java.util.Map<String, Object> row : results) {
                int customerId = (Integer) row.get("customer_id");
                String customerName = (String) row.get("customer_name");
                double totalCredit = (Double) row.get("total_credit");
                double totalPaid = (Double) row.get("total_paid");
                double remainingAmount = (Double) row.get("remaining_amount");
                Date latestDueDate = (Date) row.get("latest_due_date");
                int activeCheques = (Integer) row.get("active_cheques");

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
                count++;
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(customers);
            comboCustomer.setModel(dcm);

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

            // Using the new DB class with try-with-resources
            try (Connection conn = DB.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                
                pst.setInt(1, customerId);
                
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        this.totalCredit = rs.getDouble("total_credit");
                        this.totalPaid = rs.getDouble("total_paid");
                        this.dueAmount = rs.getDouble("remaining_amount");
                        this.latestDueDate = rs.getDate("latest_due_date");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
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

    private void updateCheque() {
        if (isUpdating) {
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            isUpdating = true;

            String selectedDisplayText = (String) comboCustomer.getSelectedItem();
            int newCustomerId = getCustomerId(selectedDisplayText);
            if (newCustomerId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid customer selected");
                isUpdating = false;
                return;
            }

            String chequeNo = txtChequeNo.getText().trim();
            double amount = Double.parseDouble(txtAmount.getText().trim());
            Date chequeDate = dateChequeDate.getDate();
            String bankName = txtBankName.getText().trim();
            String branch = txtBranch.getText().trim();

            if (isDuplicateChequeNo(chequeNo, chequeId)) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Cheque number already exists!");
                txtChequeNo.requestFocus();
                isUpdating = false;
                return;
            }

            // Using the new DB class with try-with-resources
            try (Connection conn = DB.getConnection()) {
                conn.setAutoCommit(false);

                String query = "UPDATE cheque SET cheque_no = ?, cheque_date = ?, credit_customer_id = ?, bank_name = ?, branch = ?, amount = ? WHERE cheque_id = ?";

                try (PreparedStatement pst = conn.prepareStatement(query)) {
                    pst.setString(1, chequeNo);
                    pst.setDate(2, new java.sql.Date(chequeDate.getTime()));
                    pst.setInt(3, newCustomerId);
                    pst.setString(4, bankName);
                    pst.setString(5, branch);
                    pst.setDouble(6, amount);
                    pst.setInt(7, chequeId);

                    int rowsAffected = pst.executeUpdate();

                    if (rowsAffected > 0) {
                        createChequeUpdateNotification(selectedDisplayText, chequeNo, amount, chequeDate, bankName, branch, conn);
                        conn.commit();

                        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Cheque updated successfully!");
                        dispose();

                    } else {
                        conn.rollback();
                        Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to update cheque!");
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error updating cheque: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
    }

    private boolean isDuplicateChequeNo(String chequeNo, int excludeChequeId) {
        try {
            String sql = "SELECT COUNT(*) FROM cheque WHERE cheque_no = ? AND cheque_id != ?";
            
            // Using the new DB class with try-with-resources
            try (Connection conn = DB.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                
                pst.setString(1, chequeNo);
                pst.setInt(2, excludeChequeId);
                
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createChequeUpdateNotification(String customerName, String chequeNo, double amount, Date chequeDate, String bankName, String branch, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String messageText = "Cheque updated for " + customerName
                    + " | Cheque No: " + chequeNo
                    + " | Amount: Rs " + amount
                    + " | Date: " + dateFormat.format(chequeDate)
                    + " | Bank: " + bankName
                    + " | Branch: " + branch;

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
            pstNotification.setInt(2, 31); // Edit Cheque Payment message type
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DB.closeQuietly(pstMassage, pstNotification);
        }
    }

    private void resetForm() {
        loadChequeData();
        comboCustomer.requestFocus();
    }

    private void setupFocusTraversal() {
        dateChequeDate.getDateEditor().getUiComponent().setFocusable(true);
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        btnCancel = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        comboCustomer = new javax.swing.JComboBox<>();
        txtAmount = new javax.swing.JTextField();
        txtChequeNo = new javax.swing.JTextField();
        dateChequeDate = new com.toedter.calendar.JDateChooser();
        txtBankName = new javax.swing.JTextField();
        txtBranch = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Edit Cheque Payment");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

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
        btnClear.setText("Clear Form");
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
        btnUpdate.setText("Update");
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

        comboCustomer.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        comboCustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboCustomer.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customer *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
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

        txtAmount.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        txtAmount.setText("0");
        txtAmount.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        txtAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAmountActionPerformed(evt);
            }
        });

        txtChequeNo.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        txtChequeNo.setText("0");
        txtChequeNo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cheque No  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        txtChequeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtChequeNoActionPerformed(evt);
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

        txtBankName.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        txtBankName.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bank Name *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        txtBranch.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        txtBranch.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Branch *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator3)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(comboCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(21, 21, 21))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(dateChequeDate, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(txtChequeNo, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtBankName, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtBranch, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(comboCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtChequeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateChequeDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBankName, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBranch, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCancelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCancelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        } else {
            handleArrowNavigation(evt, btnCancel);
        }
    }//GEN-LAST:event_btnCancelKeyPressed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        resetForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnClearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnClearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            resetForm();
        } else {
            handleArrowNavigation(evt, btnClear);
        }
    }//GEN-LAST:event_btnClearKeyPressed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateCheque();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnUpdateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnUpdateKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            updateCheque();
        } else {
            handleArrowNavigation(evt, btnUpdate);
        }
    }//GEN-LAST:event_btnUpdateKeyPressed

    private void comboCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboCustomerActionPerformed
        if (comboCustomer.getSelectedIndex() > 0 && !comboCustomer.isPopupVisible()) {
            // Load customer credit details when a customer is selected
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
                // Load customer credit details when a customer is selected
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
                btnUpdate.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, comboCustomer);
        }
    }//GEN-LAST:event_comboCustomerKeyPressed

    private void txtAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmountActionPerformed

    private void txtChequeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtChequeNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtChequeNoActionPerformed

    private void dateChequeDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dateChequeDateKeyPressed
        handleArrowNavigation(evt, dateChequeDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_dateChequeDateKeyPressed

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
            java.util.logging.Logger.getLogger(UpdateCheque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateCheque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateCheque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateCheque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateCheque dialog = new UpdateCheque(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnUpdate;
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
