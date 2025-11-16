package lk.com.pos.dialog;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import lk.com.pos.connection.MySQL;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class AddExpenses extends javax.swing.JDialog {

    private Map<String, Integer> expensesTypeIdMap = new HashMap<>();
    private boolean isSaving = false;

    public AddExpenses(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    public boolean isExpenseSaved() {
        return !isSaving;
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        loadExpensesTypeCombo();
        AutoCompleteDecorator.decorate(comboExpensesType);

        setupFocusTraversal();

        comboExpensesType.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                comboExpensesType.showPopup();
            }
        });

        getRootPane().registerKeyboardAction(
                evt -> openAddNewExpensesType(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        comboExpensesType.requestFocusInWindow();
    }

    private void setupKeyboardNavigation() {
        comboExpensesType.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtAmount.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    txtAmount.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    btnSave.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, comboExpensesType);
                }
            }
        });

        txtAmount.addKeyListener(new java.awt.event.KeyAdapter() {
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
                    comboExpensesType.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (areAllRequiredFieldsFilled()) {
                        btnSave.requestFocusInWindow();
                    } else {
                        btnCancel.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    comboExpensesType.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, txtAmount);
                }
            }
        });

        btnSave.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtAmount.requestFocus();
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
                    txtAmount.requestFocus();
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
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    txtAmount.requestFocus();
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
        if (source == comboExpensesType) {
            txtAmount.requestFocusInWindow();
        } else if (source == txtAmount) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnCancel) {
            btnClear.requestFocusInWindow();
        } else if (source == btnClear) {
            btnSave.requestFocusInWindow();
        } else if (source == btnSave) {
            comboExpensesType.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == comboExpensesType) {
            btnSave.requestFocusInWindow();
        } else if (source == txtAmount) {
            comboExpensesType.requestFocusInWindow();
        } else if (source == btnCancel) {
            txtAmount.requestFocusInWindow();
        } else if (source == btnClear) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnSave) {
            btnClear.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == comboExpensesType) {
            txtAmount.requestFocusInWindow();
        } else if (source == txtAmount) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnCancel) {
            btnClear.requestFocusInWindow();
        } else if (source == btnClear) {
            btnSave.requestFocusInWindow();
        } else if (source == btnSave) {
            comboExpensesType.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == comboExpensesType) {
            btnSave.requestFocusInWindow();
        } else if (source == txtAmount) {
            comboExpensesType.requestFocusInWindow();
        } else if (source == btnCancel) {
            txtAmount.requestFocusInWindow();
        } else if (source == btnClear) {
            btnCancel.requestFocusInWindow();
        } else if (source == btnSave) {
            btnClear.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return comboExpensesType.getSelectedIndex() > 0
                && !txtAmount.getText().trim().isEmpty();
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

        btnAddNewExpensesType.setBorderPainted(false);
        btnAddNewExpensesType.setContentAreaFilled(false);
        btnAddNewExpensesType.setFocusPainted(false);
        btnAddNewExpensesType.setOpaque(false);
        btnAddNewExpensesType.setFocusable(false);
        btnAddNewExpensesType.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/wallet-down.svg", 25, 25);
        addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        btnAddNewExpensesType.setIcon(addIcon);

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

        btnAddNewExpensesType.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/wallet-down.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnAddNewExpensesType.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/wallet-down.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                btnAddNewExpensesType.setIcon(normalIcon);
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

        btnAddNewExpensesType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/wallet-down.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                btnAddNewExpensesType.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/wallet-down.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                btnAddNewExpensesType.setIcon(normalIcon);
            }
        });
    }

    private void setupTooltips() {
        comboExpensesType.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field<br>Press <b>F2</b> to add new expense type</html>");
        txtAmount.setToolTipText("Enter amount and press ENTER to move to next field");
        btnAddNewExpensesType.setToolTipText("Click to add new expense type (or press F2)");
        btnSave.setToolTipText("Click to save expense (or press ENTER when focused)");
        btnClear.setToolTipText("Click to clear form (or press ENTER when focused)");
        btnCancel.setToolTipText("Click to cancel (or press ESC)");
    }

    private void loadExpensesTypeCombo() {
        try {
            expensesTypeIdMap.clear();

            String sql = "SELECT expenses_type_id, expenses_type FROM expenses_type ORDER BY expenses_type";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> expenseTypes = new Vector<>();
            expenseTypes.add("Select Expense Type");

            int count = 0;
            while (rs.next()) {
                int expensesTypeId = rs.getInt("expenses_type_id");
                String expensesType = rs.getString("expenses_type");

                expenseTypes.add(expensesType);
                expensesTypeIdMap.put(expensesType, expensesTypeId);
                count++;
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(expenseTypes);
            comboExpensesType.setModel(dcm);

            System.out.println("Successfully loaded " + count + " expense types");

        } catch (Exception e) {
            System.err.println("Error loading expense types: " + e.getMessage());
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading expense types: " + e.getMessage());
        }
    }

    private int getExpensesTypeId(String expensesType) {
        Integer expensesTypeId = expensesTypeIdMap.get(expensesType);

        if (expensesTypeId == null) {
            System.err.println("Expense type ID not found for: " + expensesType);
            System.err.println("Available mappings: " + expensesTypeIdMap);
            return -1;
        }

        System.out.println("Found Expense Type ID: " + expensesTypeId + " for: " + expensesType);
        return expensesTypeId;
    }

    private boolean validateInputs() {
        if (comboExpensesType.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select an expense type");
            comboExpensesType.requestFocus();
            return false;
        }

        if (txtAmount.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter amount");
            txtAmount.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Amount must be greater than 0");
                txtAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid amount");
            txtAmount.requestFocus();
            return false;
        }

        return true;
    }

    private void saveExpense() {
        if (isSaving) {
            System.out.println("Save already in progress, skipping duplicate call...");
            return;
        }

        System.out.println("saveExpense() called at: " + new java.util.Date());

        if (!validateInputs()) {
            return;
        }

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            isSaving = true;

            String selectedExpensesType = (String) comboExpensesType.getSelectedItem();
            int expensesTypeId = getExpensesTypeId(selectedExpensesType);
            if (expensesTypeId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid expense type selected");
                isSaving = false;
                return;
            }

            double amount = Double.parseDouble(txtAmount.getText().trim());

            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

            String query = "INSERT INTO expenses (datetime, amount, expenses_type_id) VALUES (NOW(), ?, ?)";

            pst = conn.prepareStatement(query);
            pst.setDouble(1, amount);
            pst.setInt(2, expensesTypeId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                createExpenseNotification(selectedExpensesType, amount, conn);
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Expense added successfully!");

                // Ask if user wants to add another expense
                askToAddAnotherExpense();

            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to add expense!");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving expense: " + e.getMessage());
            e.printStackTrace();
        } finally {
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
            isSaving = false;
        }
    }

    private void askToAddAnotherExpense() {
        // Create custom JOptionPane with keyboard functionality
        JOptionPane optionPane = new JOptionPane(
                "Expense added successfully! Do you want to add another expense?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION
        );

        // Create the dialog
        JDialog dialog = optionPane.createDialog(this, "Add Another Expense");

        // Add keyboard functionality to the dialog
        setupOptionPaneKeyboard(dialog, optionPane);

        // Make dialog visible
        dialog.setVisible(true);

        // Get the result
        Object result = optionPane.getValue();

        // Process the result
        if (result != null) {
            int resultValue = (Integer) result;
            if (resultValue == JOptionPane.YES_OPTION) {
                clearForm();
            } else {
                dispose();
            }
        } else {
            // User closed the dialog without selecting
            dispose();
        }
    }

    private void setupOptionPaneKeyboard(JDialog dialog, JOptionPane optionPane) {
        // Get the buttons from the option pane
        List<JButton> buttons = new ArrayList<>();
        findButtons(optionPane, buttons);

        if (buttons.size() >= 2) {
            final JButton yesButton = buttons.get(0); // YES button
            final JButton noButton = buttons.get(1);  // NO button

            // Set initial focus to YES button
            SwingUtilities.invokeLater(() -> yesButton.requestFocusInWindow());

            // Add key listener to the dialog for navigation
            dialog.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            // Move focus to YES button
                            yesButton.requestFocusInWindow();
                            evt.consume();
                            break;
                        case KeyEvent.VK_RIGHT:
                            // Move focus to NO button
                            noButton.requestFocusInWindow();
                            evt.consume();
                            break;
                        case KeyEvent.VK_ENTER:
                            // Trigger the currently focused button
                            Component focused = dialog.getFocusOwner();
                            if (focused == yesButton) {
                                optionPane.setValue(JOptionPane.YES_OPTION);
                                dialog.dispose();
                            } else if (focused == noButton) {
                                optionPane.setValue(JOptionPane.NO_OPTION);
                                dialog.dispose();
                            } else {
                                // If focus is not on buttons, trigger the focused button anyway
                                if (focused instanceof JButton) {
                                    ((JButton) focused).doClick();
                                }
                            }
                            evt.consume();
                            break;
                        case KeyEvent.VK_Y:
                            // Y key for YES
                            optionPane.setValue(JOptionPane.YES_OPTION);
                            dialog.dispose();
                            evt.consume();
                            break;
                        case KeyEvent.VK_N:
                            // N key for NO
                            optionPane.setValue(JOptionPane.NO_OPTION);
                            dialog.dispose();
                            evt.consume();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            // ESC to cancel (treat as NO)
                            optionPane.setValue(JOptionPane.NO_OPTION);
                            dialog.dispose();
                            evt.consume();
                            break;
                    }
                }
            });

            // Also add key listeners to individual buttons for better handling
            yesButton.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                        noButton.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        optionPane.setValue(JOptionPane.YES_OPTION);
                        dialog.dispose();
                        evt.consume();
                    }
                }
            });

            noButton.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                        yesButton.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        optionPane.setValue(JOptionPane.NO_OPTION);
                        dialog.dispose();
                        evt.consume();
                    }
                }
            });
        }

        // Make sure dialog is focusable and request focus
        dialog.setFocusable(true);
        dialog.requestFocusInWindow();
    }

    // Helper method to find all buttons in a container
    private void findButtons(Container container, List<JButton> buttons) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                buttons.add((JButton) comp);
            } else if (comp instanceof Container) {
                findButtons((Container) comp, buttons);
            }
        }
    }

    private void createExpenseNotification(String expensesType, double amount, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            String messageText = String.format("New expense added | Type: %s | Amount: Rs %.2f", expensesType, amount);

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
            pstNotification.setInt(2, 28); // Assuming 11 is for expense notifications
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

            System.out.println("Expense notification created successfully for type: " + expensesType);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to create expense notification: " + e.getMessage());
        } finally {
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
        comboExpensesType.setSelectedIndex(0);
        txtAmount.setText("");
        comboExpensesType.requestFocus();
    }

    private void openAddNewExpensesType() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddNewExpenseType dialog = new AddNewExpenseType(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error adding new expense type: " + e.getMessage());
        }
    }

    private void setupInputDialogKeyboard(JDialog dialog, JOptionPane optionPane, JTextField textField) {
        // Get the buttons from the option pane
        List<JButton> buttons = new ArrayList<>();
        findButtons(optionPane, buttons);

        if (buttons.size() >= 2) {
            final JButton okButton = buttons.get(0);     // OK button
            final JButton cancelButton = buttons.get(1); // Cancel button

            // Add key listener to the dialog for navigation
            dialog.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    // If text field is focused, handle text navigation
                    if (textField.hasFocus()) {
                        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                            optionPane.setValue(JOptionPane.OK_OPTION);
                            dialog.dispose();
                            evt.consume();
                        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            optionPane.setValue(JOptionPane.CANCEL_OPTION);
                            dialog.dispose();
                            evt.consume();
                        }
                        // Let text field handle arrow keys for text navigation
                        return;
                    }

                    // Handle button navigation
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            // Move focus between buttons
                            if (dialog.getFocusOwner() == cancelButton) {
                                okButton.requestFocusInWindow();
                            } else {
                                cancelButton.requestFocusInWindow();
                            }
                            evt.consume();
                            break;
                        case KeyEvent.VK_RIGHT:
                            // Move focus between buttons
                            if (dialog.getFocusOwner() == okButton) {
                                cancelButton.requestFocusInWindow();
                            } else {
                                okButton.requestFocusInWindow();
                            }
                            evt.consume();
                            break;
                        case KeyEvent.VK_ENTER:
                            // Trigger the currently focused button
                            Component focused = dialog.getFocusOwner();
                            if (focused == okButton) {
                                optionPane.setValue(JOptionPane.OK_OPTION);
                                dialog.dispose();
                            } else if (focused == cancelButton) {
                                optionPane.setValue(JOptionPane.CANCEL_OPTION);
                                dialog.dispose();
                            } else {
                                if (focused instanceof JButton) {
                                    ((JButton) focused).doClick();
                                }
                            }
                            evt.consume();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            optionPane.setValue(JOptionPane.CANCEL_OPTION);
                            dialog.dispose();
                            evt.consume();
                            break;
                        case KeyEvent.VK_TAB:
                            // Tab to switch between text field and buttons
                            if (textField.hasFocus()) {
                                okButton.requestFocusInWindow();
                            } else {
                                textField.requestFocusInWindow();
                            }
                            evt.consume();
                            break;
                    }
                }
            });

            // Add key listeners to individual buttons
            okButton.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                        cancelButton.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                        textField.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        optionPane.setValue(JOptionPane.OK_OPTION);
                        dialog.dispose();
                        evt.consume();
                    }
                }
            });

            cancelButton.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                        okButton.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                        textField.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        optionPane.setValue(JOptionPane.CANCEL_OPTION);
                        dialog.dispose();
                        evt.consume();
                    }
                }
            });

            // Add key listener to text field for better navigation
            textField.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_TAB) {
                        okButton.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        optionPane.setValue(JOptionPane.OK_OPTION);
                        dialog.dispose();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        optionPane.setValue(JOptionPane.CANCEL_OPTION);
                        dialog.dispose();
                        evt.consume();
                    }
                }
            });
        }

        // Set initial focus to text field
        dialog.setFocusable(true);
        textField.requestFocusInWindow();
    }

    private void addNewExpenseType(String expenseType) {
        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = MySQL.getConnection();
            String sql = "INSERT INTO expenses_type (expenses_type) VALUES (?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, expenseType);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "Expense type added successfully!");
                loadExpensesTypeCombo(); // Reload the combo box
                // Select the newly added item
                comboExpensesType.setSelectedItem(expenseType);
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Failed to add expense type!");
            }

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error adding expense type: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupFocusTraversal() {
        btnAddNewExpensesType.setFocusable(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        btnCancel = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        txtAmount = new javax.swing.JTextField();
        comboExpensesType = new javax.swing.JComboBox<>();
        btnAddNewExpensesType = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Add Expenses");

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

        btnSave.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnSave.setForeground(new java.awt.Color(8, 147, 176));
        btnSave.setText("Save");
        btnSave.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
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

        txtAmount.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        txtAmount.setText("0");
        txtAmount.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        txtAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAmountActionPerformed(evt);
            }
        });

        comboExpensesType.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        comboExpensesType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboExpensesType.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Expenses Type *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        comboExpensesType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboExpensesTypeActionPerformed(evt);
            }
        });
        comboExpensesType.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comboExpensesTypeKeyPressed(evt);
            }
        });

        btnAddNewExpensesType.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        btnAddNewExpensesType.setForeground(new java.awt.Color(102, 102, 102));
        btnAddNewExpensesType.setBorder(null);
        btnAddNewExpensesType.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddNewExpensesType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNewExpensesTypeActionPerformed(evt);
            }
        });
        btnAddNewExpensesType.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnAddNewExpensesTypeKeyPressed(evt);
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
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jSeparator3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtAmount)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(comboExpensesType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddNewExpensesType, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                    .addComponent(comboExpensesType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(btnAddNewExpensesType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
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
        saveExpense();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnSaveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnSaveKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            saveExpense();
        } else {
            handleArrowNavigation(evt, btnSave);
        }

    }//GEN-LAST:event_btnSaveKeyPressed

    private void txtAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmountActionPerformed

    private void comboExpensesTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboExpensesTypeActionPerformed
        if (comboExpensesType.getSelectedIndex() > 0 && !comboExpensesType.isPopupVisible()) {
            txtAmount.requestFocusInWindow();
        }
    }//GEN-LAST:event_comboExpensesTypeActionPerformed

    private void comboExpensesTypeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboExpensesTypeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (comboExpensesType.isPopupVisible()) {
                comboExpensesType.setPopupVisible(false);
            }
            if (comboExpensesType.getSelectedIndex() > 0) {
                txtAmount.requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!comboExpensesType.isPopupVisible()) {
                comboExpensesType.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!comboExpensesType.isPopupVisible()) {
                btnSave.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, comboExpensesType);
        }
    }//GEN-LAST:event_comboExpensesTypeKeyPressed

    private void btnAddNewExpensesTypeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnAddNewExpensesTypeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F2) {
            openAddNewExpensesType();
        }
    }//GEN-LAST:event_btnAddNewExpensesTypeKeyPressed

    private void btnAddNewExpensesTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddNewExpensesTypeActionPerformed
        openAddNewExpensesType();
    }//GEN-LAST:event_btnAddNewExpensesTypeActionPerformed

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
            java.util.logging.Logger.getLogger(AddExpenses.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddExpenses.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddExpenses.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddExpenses.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddExpenses dialog = new AddExpenses(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnAddNewExpensesType;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> comboExpensesType;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField txtAmount;
    // End of variables declaration//GEN-END:variables
}
