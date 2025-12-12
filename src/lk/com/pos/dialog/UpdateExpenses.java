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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import lk.com.pos.connection.DB;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class UpdateExpenses extends javax.swing.JDialog {

    private Map<String, Integer> expensesTypeIdMap = new HashMap<>();
    private ButtonGroup statusGroup;
    private int expensesId;
    private boolean isSaving = false;

    // Focus traversal order
    private java.util.List<Component> focusOrder = new ArrayList<>();

    /**
     * Creates new form UpdateExpenses with expenses ID for editing
     */
    public UpdateExpenses(java.awt.Frame parent, boolean modal, int expensesId) {
        super(parent, modal);
        this.expensesId = expensesId;
        initComponents();
        initializeDialog();
    }

    private void initializeDialog() {
        // Initialize button group
        statusGroup = new ButtonGroup();
        statusGroup.add(jRadioButton1); // Paid
        statusGroup.add(jRadioButton2); // Unpaid

        // Set default selection to Paid
        jRadioButton1.setSelected(true);

        setLocationRelativeTo(getParent());

        // Initialize focus order
        initializeFocusOrder();

        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        loadExpensesTypeCombo();
        AutoCompleteDecorator.decorate(comboExpensesType);

        // Set default date and time to current
        paymentDate.setDate(new Date());
        timePicker1.setTime(LocalTime.now());

        // Make time picker focusable
        timePicker1.setFocusable(true);

        // Register global keyboard shortcuts
        setupGlobalShortcuts();

        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Load existing expenses data
        loadExpensesData();

        setupFocusTraversal();
        comboExpensesType.requestFocusInWindow();
    }

    private void initializeFocusOrder() {
        // Get the internal text field from time picker
        Component timeTextField = timePicker1.getComponent(0);

        // Focus traversal order
        focusOrder.add(comboExpensesType);
        focusOrder.add(paymentDate.getDateEditor().getUiComponent());
        focusOrder.add(timeTextField); // Use the internal text field
        focusOrder.add(txtAmount);
        focusOrder.add(jRadioButton1);
        focusOrder.add(jRadioButton2);
        focusOrder.add(jTextArea1);
        focusOrder.add(btnSave);
        focusOrder.add(btnClear);
        focusOrder.add(btnCancel);
    }

    private void setupGlobalShortcuts() {
        // F1 - Save
        getRootPane().registerKeyboardAction(
                evt -> saveExpense(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // F2 - Clear
        getRootPane().registerKeyboardAction(
                evt -> clearForm(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void setupKeyboardNavigation() {
        comboExpensesType.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (comboExpensesType.isPopupVisible()) {
                        comboExpensesType.setPopupVisible(false);
                    }
                    if (comboExpensesType.getSelectedIndex() > 0) {
                        paymentDate.requestFocusInWindow();
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
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    clearForm();
                    evt.consume();
                }
            }
        });

        // Date picker navigation
        paymentDate.getDateEditor().getUiComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    timePicker1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    timePicker1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    comboExpensesType.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    clearForm();
                    evt.consume();
                }
            }
        });

        // FIXED: Time Picker navigation - ensure typing is enabled when focused
        timePicker1.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // When time picker gets focus, ensure the internal text field is focused and ready for typing
                SwingUtilities.invokeLater(() -> {
                    Component timePickerComponent = timePicker1.getComponent(0);
                    if (timePickerComponent instanceof JTextField) {
                        JTextField timeTextField = (JTextField) timePickerComponent;
                        timeTextField.requestFocusInWindow();
                        timeTextField.selectAll(); // Optional: select all text for easy replacement
                    }
                });
            }
        });

        // Add key listener to the main time picker component
        timePicker1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtAmount.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtAmount.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    paymentDate.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    clearForm();
                    evt.consume();
                }
                // Let other keys (typing keys) pass through to the internal text field
            }
        });

        // Also ensure the internal text field handles typing properly
        Component timePicker1Component = timePicker1.getComponent(0);
        if (timePicker1Component instanceof JTextField) {
            JTextField timeTextField = (JTextField) timePicker1Component;

            // Add ActionListener for Enter key (when user types and presses Enter)
            timeTextField.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    txtAmount.requestFocusInWindow();
                }
            });

            timeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        txtAmount.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                        txtAmount.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                        paymentDate.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                        saveExpense();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                        clearForm();
                        evt.consume();
                    }
                    // Let typing keys (digits, colon, etc.) through without consuming
                }
            });
        }

        txtAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    jRadioButton1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    timePicker1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    jRadioButton1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    clearForm();
                    evt.consume();
                }
            }
        });

        // Radio button navigation with space bar support
        jRadioButton1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        jRadioButton1.setSelected(true);
                        jTextArea1.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_LEFT:
                        jRadioButton2.setSelected(true);
                        jRadioButton2.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_UP:
                        txtAmount.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_DOWN:
                        jTextArea1.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F1:
                        saveExpense();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F2:
                        clearForm();
                        evt.consume();
                        break;
                }
            }
        });

        jRadioButton2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        jRadioButton2.setSelected(true);
                        jTextArea1.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_LEFT:
                        jRadioButton1.setSelected(true);
                        jRadioButton1.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_UP:
                        txtAmount.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_DOWN:
                        jTextArea1.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F1:
                        saveExpense();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F2:
                        clearForm();
                        evt.consume();
                        break;
                }
            }
        });

        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnSave.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jRadioButton1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnSave.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    clearForm();
                    evt.consume();
                }
            }
        });

        btnSave.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextArea1.requestFocusInWindow();
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
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    clearForm();
                    evt.consume();
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
                    jTextArea1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnCancel.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnSave.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnCancel.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    clearForm();
                    evt.consume();
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
                    jTextArea1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnSave.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnClear.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnSave.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    saveExpense();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    clearForm();
                    evt.consume();
                }
            }
        });
    }

    private void setupButtonStyles() {
        setupGradientButton(btnSave);
        setupGradientButton(btnClear);
        setupGradientButton(btnCancel);

        setButtonIcons();
        setupButtonMouseListeners();
        setupButtonFocusListeners();
    }

    private void setupGradientButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.decode("#0893B0"));
        button.setFont(new Font("Nunito SemiBold", Font.BOLD, 16));
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

    private void setButtonIcons() {
        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnSave.setIcon(saveIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnClear.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnCancel.setIcon(cancelIcon);
    }

    private void setupButtonMouseListeners() {
        setupButtonHoverEffect(btnSave);
        setupButtonHoverEffect(btnClear);
        setupButtonHoverEffect(btnCancel);
    }

    private void setupButtonHoverEffect(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon(getIconName(button), 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                button.setIcon(hoverIcon);
                button.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon(getIconName(button), 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                button.setIcon(normalIcon);
                button.repaint();
            }
        });
    }

    private String getIconName(JButton button) {
        if (button.getText().contains("Update")) {
            return "lk/com/pos/icon/update.svg";
        }
        if (button.getText().contains("Clear")) {
            return "lk/com/pos/icon/cancel.svg";
        }
        if (button.getText().contains("Cancel")) {
            return "lk/com/pos/icon/clear.svg";
        }
        return "lk/com/pos/icon/update.svg";
    }

    private void setupButtonFocusListeners() {
        setupButtonFocusEffect(btnSave);
        setupButtonFocusEffect(btnClear);
        setupButtonFocusEffect(btnCancel);
    }

    private void setupButtonFocusEffect(JButton button) {
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                button.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon(getIconName(button), 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                button.setIcon(focusedIcon);
                button.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                button.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon(getIconName(button), 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                button.setIcon(normalIcon);
                button.repaint();
            }
        });
    }

    private void setupTooltips() {
        comboExpensesType.setToolTipText("Select expenses type (F1: Save, F2: Clear)");
        txtAmount.setToolTipText("Enter expenses amount (F1: Save, F2: Clear)");
        jRadioButton1.setToolTipText("Select Paid status (Space/Enter: Select, Arrow keys: Switch)");
        jRadioButton2.setToolTipText("Select Unpaid status (Space/Enter: Select, Arrow keys: Switch)");
    }

    private void setupFocusTraversal() {
        // Set custom focus traversal policy
        getContentPane().setFocusTraversalPolicy(new javax.swing.LayoutFocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
                int index = focusOrder.indexOf(aComponent);
                if (index >= 0 && index < focusOrder.size() - 1) {
                    return focusOrder.get(index + 1);
                }
                return super.getComponentAfter(focusCycleRoot, aComponent);
            }

            @Override
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
                int index = focusOrder.indexOf(aComponent);
                if (index > 0) {
                    return focusOrder.get(index - 1);
                }
                return super.getComponentBefore(focusCycleRoot, aComponent);
            }
        });
    }

    private void loadExpensesTypeCombo() {
        try {
            expensesTypeIdMap.clear();

            String sql = "SELECT expenses_type_id, expenses_type FROM expenses_type ORDER BY expenses_type";
            
            DB.executeQuerySafe(sql, rs -> {
                Vector<String> expensesTypes = new Vector<>();
                expensesTypes.add("Select Expenses Type");

                while (rs.next()) {
                    int expensesTypeId = rs.getInt("expenses_type_id");
                    String expensesType = rs.getString("expenses_type");

                    expensesTypes.add(expensesType);
                    expensesTypeIdMap.put(expensesType, expensesTypeId);
                }

                comboExpensesType.setModel(new javax.swing.DefaultComboBoxModel<>(expensesTypes));
                return null;
            });

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading expenses types: " + e.getMessage());
        }
    }

    private void loadExpensesData() {
        if (expensesId <= 0) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid expenses ID");
            dispose();
            return;
        }

        try {
            String sql = "SELECT e.date, e.amount, e.expenses_type_id, e.e_status_id, e.time, e.description, et.expenses_type "
                    + "FROM expenses e "
                    + "JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id "
                    + "WHERE e.expenses_id = ?";

            DB.executeQuerySafe(sql, rs -> {
                if (rs.next()) {
                    // Load data into form fields
                    double amount = rs.getDouble("amount");
                    java.sql.Date date = rs.getDate("date");
                    java.sql.Time time = rs.getTime("time");
                    String description = rs.getString("description");
                    int expensesTypeId = rs.getInt("expenses_type_id");
                    int statusId = rs.getInt("e_status_id");
                    String expensesType = rs.getString("expenses_type");

                    // Set form fields
                    txtAmount.setText(String.valueOf(amount));
                    paymentDate.setDate(date);

                    // Set time
                    if (time != null) {
                        timePicker1.setTime(time.toLocalTime());
                    }

                    // Set description
                    if (description != null) {
                        jTextArea1.setText(description);
                    }

                    // Set expenses type
                    for (int i = 0; i < comboExpensesType.getItemCount(); i++) {
                        if (comboExpensesType.getItemAt(i).equals(expensesType)) {
                            comboExpensesType.setSelectedIndex(i);
                            break;
                        }
                    }

                    // Set status
                    if (statusId == 1) {
                        jRadioButton1.setSelected(true);
                    } else {
                        jRadioButton2.setSelected(true);
                    }

                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Expenses record not found");
                    dispose();
                }
                return null;
            }, expensesId);

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading expenses data: " + e.getMessage());
            dispose();
        }
    }

    private boolean validateInputs() {
        if (comboExpensesType.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select an expenses type");
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

        if (paymentDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a date");
            paymentDate.requestFocus();
            return false;
        }

        if (timePicker1.getTime() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a time");
            timePicker1.requestFocus();
            return false;
        }

        return true;
    }

    private void saveExpense() {
        if (isSaving) {
            return;
        }

        if (!validateInputs()) {
            return;
        }

        Connection conn = null;

        try {
            isSaving = true;

            String selectedExpensesType = (String) comboExpensesType.getSelectedItem();
            int expensesTypeId = expensesTypeIdMap.get(selectedExpensesType);
            double amount = Double.parseDouble(txtAmount.getText().trim());
            java.sql.Date date = new java.sql.Date(paymentDate.getDate().getTime());
            java.sql.Time time = java.sql.Time.valueOf(timePicker1.getTime());
            String description = jTextArea1.getText().trim();
            int statusId = jRadioButton1.isSelected() ? 1 : 2; // 1=Paid, 2=Unpaid

            conn = DB.getConnection();

            String query = "UPDATE expenses SET date = ?, amount = ?, expenses_type_id = ?, e_status_id = ?, time = ?, description = ? WHERE expenses_id = ?";

            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setDate(1, date);
                pst.setDouble(2, amount);
                pst.setInt(3, expensesTypeId);
                pst.setInt(4, statusId);
                pst.setTime(5, time);
                pst.setString(6, description.isEmpty() ? null : description);
                pst.setInt(7, expensesId);

                int rowsAffected = pst.executeUpdate();

                if (rowsAffected > 0) {
                    createExpenseNotification(selectedExpensesType, amount);
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Expenses updated successfully!");
                    dispose();
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to update expenses!");
                }
            }

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error updating expenses: " + e.getMessage());
        } finally {
            DB.closeQuietly(conn);
            isSaving = false;
        }
    }

    private void createExpenseNotification(String expensesType, double amount) {
        Connection conn = null;
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;
        ResultSet rs = null;

        try {
            String messageText = String.format("Expenses updated | Type: %s | Amount: Rs %.2f", expensesType, amount);

            conn = DB.getConnection();

            // Check if message already exists
            String checkSql = "SELECT COUNT(*) FROM massage WHERE massage = ?";
            pstMassage = conn.prepareStatement(checkSql);
            pstMassage.setString(1, messageText);
            rs = pstMassage.executeQuery();

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
            pstNotification.setInt(2, 29); // Expenses message type (using same as income for now)
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (Exception e) {
        } finally {
            DB.closeQuietly(rs, pstMassage, pstNotification, conn);
        }
    }

    private void clearForm() {
        // Reload original data
        loadExpensesData();
        comboExpensesType.requestFocus();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        comboExpensesType = new javax.swing.JComboBox<>();
        paymentDate = new com.toedter.calendar.JDateChooser();
        timePicker1 = new com.github.lgooddatepicker.components.TimePicker();
        txtAmount = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        btnCancel = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Expenses");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setText("Edit Expenses");
        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

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

        paymentDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        paymentDate.setDateFormatString("MM/dd/yyyy");
        paymentDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        paymentDate.setOpaque(false);
        paymentDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                paymentDateKeyPressed(evt);
            }
        });

        timePicker1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Time *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        timePicker1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        timePicker1.setOpaque(false);

        txtAmount.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        txtAmount.setText("0");
        txtAmount.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        txtAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAmountActionPerformed(evt);
            }
        });

        jRadioButton1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jRadioButton1.setText("Paid");

        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jRadioButton2.setText("UnPaid");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Description (Optional)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

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

        btnSave.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnSave.setForeground(new java.awt.Color(8, 147, 176));
        btnSave.setText("Update");
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(comboExpensesType, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(paymentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timePicker1, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(txtAmount)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton2))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(comboExpensesType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(paymentDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
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

    private void comboExpensesTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboExpensesTypeActionPerformed
        if (comboExpensesType.getSelectedIndex() > 0 && !comboExpensesType.isPopupVisible()) {
            paymentDate.requestFocusInWindow();
        }
    }//GEN-LAST:event_comboExpensesTypeActionPerformed

    private void comboExpensesTypeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboExpensesTypeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (comboExpensesType.isPopupVisible()) {
                comboExpensesType.setPopupVisible(false);
            }
            if (comboExpensesType.getSelectedIndex() > 0) {
                paymentDate.requestFocusInWindow();
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
        }
    }//GEN-LAST:event_comboExpensesTypeKeyPressed

    private void paymentDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paymentDateKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            timePicker1.requestFocusInWindow();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            timePicker1.requestFocusInWindow();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            comboExpensesType.requestFocusInWindow();
            evt.consume();
        }
    }//GEN-LAST:event_paymentDateKeyPressed

    private void txtAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmountActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCancelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCancelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
            evt.consume();
        }
    }//GEN-LAST:event_btnCancelKeyPressed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnClearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnClearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
            evt.consume();
        }
    }//GEN-LAST:event_btnClearKeyPressed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveExpense();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnSaveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnSaveKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveExpense();
            evt.consume();
        }
    }//GEN-LAST:event_btnSaveKeyPressed

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
            java.util.logging.Logger.getLogger(UpdateExpenses.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateExpenses.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateExpenses.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateExpenses.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateExpenses dialog = new UpdateExpenses(new javax.swing.JFrame(), true, 3);
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
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> comboExpensesType;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextArea jTextArea1;
    private com.toedter.calendar.JDateChooser paymentDate;
    private com.github.lgooddatepicker.components.TimePicker timePicker1;
    private javax.swing.JTextField txtAmount;
    // End of variables declaration//GEN-END:variables
}
