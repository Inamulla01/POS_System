/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
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
import javax.swing.JDialog;
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
public class ExpenseIncomeDialog extends javax.swing.JDialog {

    private Map<String, Integer> expensesTypeIdMap = new HashMap<>();
    private Map<String, Integer> incomeTypeIdMap = new HashMap<>();
    private boolean isExpenseSaving = false;
    private boolean isIncomeSaving = false;

    // Button groups for radio buttons
    private ButtonGroup expenseStatusGroup;
    private ButtonGroup incomeStatusGroup;

    // Focus traversal orders
    private java.util.List<Component> expenseFocusOrder = new ArrayList<>();
    private java.util.List<Component> incomeFocusOrder = new ArrayList<>();

    public ExpenseIncomeDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    private void initializeDialog() {
        // Initialize button groups
        expenseStatusGroup = new ButtonGroup();
        expenseStatusGroup.add(jRadioButton1); // Paid
        expenseStatusGroup.add(jRadioButton2); // Unpaid

        incomeStatusGroup = new ButtonGroup();
        incomeStatusGroup.add(jRadioButton3); // Paid
        incomeStatusGroup.add(jRadioButton4); // Unpaid

        // Set default selection to Paid
        jRadioButton1.setSelected(true);
        jRadioButton3.setSelected(true);

        setLocationRelativeTo(getParent());

        // Initialize focus orders
        initializeFocusOrders();

        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        loadExpensesTypeCombo();
        loadIncomeTypeCombo();

        AutoCompleteDecorator.decorate(comboExpensesType);
        AutoCompleteDecorator.decorate(comboIncomeType);

        setupFocusTraversal();

        // Set default dates to current date
        paymentDate.setDate(new Date());
        paymentDate1.setDate(new Date());

        // Set default time to current time
        timePicker1.setTime(LocalTime.now());
        timePicker2.setTime(LocalTime.now());

        // Make time pickers focusable
        timePicker1.setFocusable(true);
        timePicker2.setFocusable(true);

        // Register global keyboard shortcuts
        setupGlobalShortcuts();

        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        comboExpensesType.requestFocusInWindow();
    }

    private void initializeFocusOrders() {
        // Get the internal text fields from time pickers
        Component timeTextField1 = timePicker1.getComponent(0);
        Component timeTextField2 = timePicker2.getComponent(0);
        
        // Expenses tab focus traversal order
        expenseFocusOrder.add(comboExpensesType);
        expenseFocusOrder.add(paymentDate.getDateEditor().getUiComponent());
        expenseFocusOrder.add(timeTextField1); // Use the internal text field
        expenseFocusOrder.add(txtAmount);
        expenseFocusOrder.add(jRadioButton1);
        expenseFocusOrder.add(jRadioButton2);
        expenseFocusOrder.add(jTextArea1);
        expenseFocusOrder.add(btnSave);
        expenseFocusOrder.add(btnClear);
        expenseFocusOrder.add(btnCancel);

        // Income tab focus traversal order
        incomeFocusOrder.add(comboIncomeType);
        incomeFocusOrder.add(paymentDate1.getDateEditor().getUiComponent());
        incomeFocusOrder.add(timeTextField2); // Use the internal text field
        incomeFocusOrder.add(txtAmount1);
        incomeFocusOrder.add(jRadioButton3);
        incomeFocusOrder.add(jRadioButton4);
        incomeFocusOrder.add(jTextArea2);
        incomeFocusOrder.add(btnSave1);
        incomeFocusOrder.add(btnClear1);
        incomeFocusOrder.add(btnCancel1);
    }

    private void setupGlobalShortcuts() {
        // F1 - Switch to Income tab
        getRootPane().registerKeyboardAction(
                evt -> switchToIncomeTab(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // F2 - Switch to Expenses tab
        getRootPane().registerKeyboardAction(
                evt -> switchToExpenseTab(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void switchToIncomeTab() {
        jTabbedPane1.setSelectedIndex(1); // Income tab is index 1
        comboIncomeType.requestFocusInWindow();
        showTabSwitchNotification("Income Tab");
    }

    private void switchToExpenseTab() {
        jTabbedPane1.setSelectedIndex(0); // Expenses tab is index 0
        comboExpensesType.requestFocusInWindow();
        showTabSwitchNotification("Expenses Tab");
    }

    private void showTabSwitchNotification(String tabName) {
        // Optional: You can add a small status message here if needed
    }

    private void setupKeyboardNavigation() {
        // Expenses Tab Navigation
        setupExpensesNavigation();

        // Income Tab Navigation  
        setupIncomeNavigation();
    }

    private void setupExpensesNavigation() {
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
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        // Fix for date picker - add listener to the actual text field inside date picker
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
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
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
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
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
                        switchToIncomeTab();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                        switchToExpenseTab();
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
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        // Radio button navigation - improved arrow key handling and added space bar support
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
                        switchToIncomeTab();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F2:
                        switchToExpenseTab();
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
                        switchToIncomeTab();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F2:
                        switchToExpenseTab();
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
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
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
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        btnClear.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearExpenseForm();
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
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
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
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });
    }

    private void setupIncomeNavigation() {
        comboIncomeType.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (comboIncomeType.isPopupVisible()) {
                        comboIncomeType.setPopupVisible(false);
                    }
                    if (comboIncomeType.getSelectedIndex() > 0) {
                        paymentDate1.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!comboIncomeType.isPopupVisible()) {
                        comboIncomeType.showPopup();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    if (!comboIncomeType.isPopupVisible()) {
                        btnSave1.requestFocusInWindow();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        // Fix for date picker in income tab
        paymentDate1.getDateEditor().getUiComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    timePicker2.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    timePicker2.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    comboIncomeType.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        // FIXED: Time Picker navigation for income tab - ensure typing is enabled when focused
        timePicker2.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // When time picker gets focus, ensure the internal text field is focused and ready for typing
                SwingUtilities.invokeLater(() -> {
                    Component timePickerComponent = timePicker2.getComponent(0);
                    if (timePickerComponent instanceof JTextField) {
                        JTextField timeTextField = (JTextField) timePickerComponent;
                        timeTextField.requestFocusInWindow();
                        timeTextField.selectAll(); // Optional: select all text for easy replacement
                    }
                });
            }
        });

        // Add key listener to the main time picker component
        timePicker2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtAmount1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtAmount1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    paymentDate1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
                // Let other keys (typing keys) pass through to the internal text field
            }
        });

        // Also ensure the internal text field handles typing properly
        Component timePicker2Component = timePicker2.getComponent(0);
        if (timePicker2Component instanceof JTextField) {
            JTextField timeTextField = (JTextField) timePicker2Component;
            
            // Add ActionListener for Enter key (when user types and presses Enter)
            timeTextField.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    txtAmount1.requestFocusInWindow();
                }
            });

            timeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        txtAmount1.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                        txtAmount1.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                        paymentDate1.requestFocusInWindow();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                        switchToIncomeTab();
                        evt.consume();
                    } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                        switchToExpenseTab();
                        evt.consume();
                    }
                    // Let typing keys (digits, colon, etc.) through without consuming
                }
            });
        }

        txtAmount1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    jRadioButton3.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    timePicker2.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    jRadioButton3.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        // Radio button navigation for income tab with space bar support
        jRadioButton3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        jRadioButton3.setSelected(true);
                        jTextArea2.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_LEFT:
                        jRadioButton4.setSelected(true);
                        jRadioButton4.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_UP:
                        txtAmount1.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_DOWN:
                        jTextArea2.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F1:
                        switchToIncomeTab();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F2:
                        switchToExpenseTab();
                        evt.consume();
                        break;
                }
            }
        });

        jRadioButton4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        jRadioButton4.setSelected(true);
                        jTextArea2.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_LEFT:
                        jRadioButton3.setSelected(true);
                        jRadioButton3.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_UP:
                        txtAmount1.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_DOWN:
                        jTextArea2.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F1:
                        switchToIncomeTab();
                        evt.consume();
                        break;
                    case KeyEvent.VK_F2:
                        switchToExpenseTab();
                        evt.consume();
                        break;
                }
            }
        });

        jTextArea2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnSave1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jRadioButton3.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnSave1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        btnSave1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveIncome();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextArea2.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnClear1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnClear1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnClear1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        btnClear1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearIncomeForm();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextArea2.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnCancel1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnSave1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnCancel1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });

        btnCancel1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    jTextArea2.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    btnSave1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    btnClear1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    btnSave1.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F1) {
                    switchToIncomeTab();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
                    switchToExpenseTab();
                    evt.consume();
                }
            }
        });
    }

    private void setupButtonStyles() {
        // Expenses Tab Buttons
        setupGradientButton(btnSave);
        setupGradientButton(btnClear);
        setupGradientButton(btnCancel);

        // Income Tab Buttons
        setupGradientButton(btnSave1);
        setupGradientButton(btnClear1);
        setupGradientButton(btnCancel1);

        // Set icons for buttons
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
        // Expenses Tab Icons
        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/save.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnSave.setIcon(saveIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnClear.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        btnCancel.setIcon(cancelIcon);

        // Income Tab Icons
        btnSave1.setIcon(saveIcon);
        btnClear1.setIcon(clearIcon);
        btnCancel1.setIcon(cancelIcon);
    }

    private void setupButtonMouseListeners() {
        // Expenses Tab Button Hover Effects
        setupButtonHoverEffect(btnSave);
        setupButtonHoverEffect(btnClear);
        setupButtonHoverEffect(btnCancel);

        // Income Tab Button Hover Effects
        setupButtonHoverEffect(btnSave1);
        setupButtonHoverEffect(btnClear1);
        setupButtonHoverEffect(btnCancel1);
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
        if (button.getText().contains("Save")) {
            return "lk/com/pos/icon/save.svg";
        }
        if (button.getText().contains("Clear")) {
            return "lk/com/pos/icon/clear.svg";
        }
        if (button.getText().contains("Cancel")) {
            return "lk/com/pos/icon/cancel.svg";
        }
        return "lk/com/pos/icon/save.svg";
    }

    private void setupButtonFocusListeners() {
        // Expenses Tab Focus Effects
        setupButtonFocusEffect(btnSave);
        setupButtonFocusEffect(btnClear);
        setupButtonFocusEffect(btnCancel);

        // Income Tab Focus Effects
        setupButtonFocusEffect(btnSave1);
        setupButtonFocusEffect(btnClear1);
        setupButtonFocusEffect(btnCancel1);
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
        comboExpensesType.setToolTipText("Select expense type (F2: Expenses Tab, F1: Income Tab)");
        comboIncomeType.setToolTipText("Select income type (F2: Expenses Tab, F1: Income Tab)");
        txtAmount.setToolTipText("Enter expense amount (F2: Expenses Tab, F1: Income Tab)");
        txtAmount1.setToolTipText("Enter income amount (F2: Expenses Tab, F1: Income Tab)");

        // Add tooltips to radio buttons to indicate space bar functionality
        jRadioButton1.setToolTipText("Select Paid status (Space/Enter: Select, Arrow keys: Switch)");
        jRadioButton2.setToolTipText("Select Unpaid status (Space/Enter: Select, Arrow keys: Switch)");
        jRadioButton3.setToolTipText("Select Paid status (Space/Enter: Select, Arrow keys: Switch)");
        jRadioButton4.setToolTipText("Select Unpaid status (Space/Enter: Select, Arrow keys: Switch)");
    }

    private void loadExpensesTypeCombo() {
        try {
            expensesTypeIdMap.clear();

            String sql = "SELECT expenses_type_id, expenses_type FROM expenses_type ORDER BY expenses_type";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> expenseTypes = new Vector<>();
            expenseTypes.add("Select Expense Type");

            while (rs.next()) {
                int expensesTypeId = rs.getInt("expenses_type_id");
                String expensesType = rs.getString("expenses_type");

                expenseTypes.add(expensesType);
                expensesTypeIdMap.put(expensesType, expensesTypeId);
            }

            comboExpensesType.setModel(new javax.swing.DefaultComboBoxModel<>(expenseTypes));

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading expense types: " + e.getMessage());
        }
    }

    private void loadIncomeTypeCombo() {
        try {
            incomeTypeIdMap.clear();

            String sql = "SELECT income_type_id, income_type FROM income_type ORDER BY income_type";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> incomeTypes = new Vector<>();
            incomeTypes.add("Select Income Type");

            while (rs.next()) {
                int incomeTypeId = rs.getInt("income_type_id");
                String incomeType = rs.getString("income_type");

                incomeTypes.add(incomeType);
                incomeTypeIdMap.put(incomeType, incomeTypeId);
            }

            comboIncomeType.setModel(new javax.swing.DefaultComboBoxModel<>(incomeTypes));

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading income types: " + e.getMessage());
        }
    }

    private boolean validateExpenseInputs() {
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

    private boolean validateIncomeInputs() {
        if (comboIncomeType.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select an income type");
            comboIncomeType.requestFocus();
            return false;
        }

        if (txtAmount1.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter amount");
            txtAmount1.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(txtAmount1.getText().trim());
            if (amount <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Amount must be greater than 0");
                txtAmount1.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid amount");
            txtAmount1.requestFocus();
            return false;
        }

        if (paymentDate1.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a date");
            paymentDate1.requestFocus();
            return false;
        }

        if (timePicker2.getTime() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a time");
            timePicker2.requestFocus();
            return false;
        }

        return true;
    }

    private void saveExpense() {
        if (isExpenseSaving) {
            return;
        }

        if (!validateExpenseInputs()) {
            return;
        }

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            isExpenseSaving = true;

            String selectedExpensesType = (String) comboExpensesType.getSelectedItem();
            int expensesTypeId = expensesTypeIdMap.get(selectedExpensesType);
            double amount = Double.parseDouble(txtAmount.getText().trim());
            java.sql.Date date = new java.sql.Date(paymentDate.getDate().getTime());
            java.sql.Time time = java.sql.Time.valueOf(timePicker1.getTime());
            String description = jTextArea1.getText().trim();
            int statusId = jRadioButton1.isSelected() ? 1 : 2; // 1=Paid, 2=Unpaid

            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

            String query = "INSERT INTO expenses (date, amount, expenses_type_id, e_status_id, time, description) VALUES (?, ?, ?, ?, ?, ?)";

            pst = conn.prepareStatement(query);
            pst.setDate(1, date);
            pst.setDouble(2, amount);
            pst.setInt(3, expensesTypeId);
            pst.setInt(4, statusId);
            pst.setTime(5, time);
            pst.setString(6, description.isEmpty() ? null : description);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                createExpenseNotification(selectedExpensesType, amount, conn);
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Expense added successfully!");

                // Ask if user wants to add another expense
                askToAddAnother("expense");
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
                // Silent rollback failure
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving expense: " + e.getMessage());
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
                // Silent resource closing failure
            }
            isExpenseSaving = false;
        }
    }

    private void saveIncome() {
        if (isIncomeSaving) {
            return;
        }

        if (!validateIncomeInputs()) {
            return;
        }

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            isIncomeSaving = true;

            String selectedIncomeType = (String) comboIncomeType.getSelectedItem();
            int incomeTypeId = incomeTypeIdMap.get(selectedIncomeType);
            double amount = Double.parseDouble(txtAmount1.getText().trim());
            java.sql.Date date = new java.sql.Date(paymentDate1.getDate().getTime());
            java.sql.Time time = java.sql.Time.valueOf(timePicker2.getTime());
            String description = jTextArea2.getText().trim();
            int statusId = jRadioButton3.isSelected() ? 1 : 2; // 1=Paid, 2=Unpaid

            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

            String query = "INSERT INTO income (amount, date, time, description, income_type_id, status_id) VALUES (?, ?, ?, ?, ?, ?)";

            pst = conn.prepareStatement(query);
            pst.setDouble(1, amount);
            pst.setDate(2, date);
            pst.setTime(3, time);
            pst.setString(4, description.isEmpty() ? null : description);
            pst.setInt(5, incomeTypeId);
            pst.setInt(6, statusId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                createIncomeNotification(selectedIncomeType, amount, conn);
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Income added successfully!");

                // Ask if user wants to add another income
                askToAddAnother("income");
            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to add income!");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
                // Silent rollback failure
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving income: " + e.getMessage());
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
                // Silent resource closing failure
            }
            isIncomeSaving = false;
        }
    }

    private void askToAddAnother(String type) {
        JOptionPane optionPane = new JOptionPane(
                type.substring(0, 1).toUpperCase() + type.substring(1) + " added successfully! Do you want to add another " + type + "?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION
        );

        JDialog dialog = optionPane.createDialog(this, "Add Another " + type.substring(0, 1).toUpperCase() + type.substring(1));
        setupOptionPaneKeyboard(dialog, optionPane);
        dialog.setVisible(true);

        Object result = optionPane.getValue();
        if (result != null) {
            int resultValue = (Integer) result;
            if (resultValue == JOptionPane.YES_OPTION) {
                if (type.equals("expense")) {
                    clearExpenseForm();
                } else {
                    clearIncomeForm();
                }
            } else {
                dispose();
            }
        } else {
            dispose();
        }
    }

    private void setupOptionPaneKeyboard(JDialog dialog, JOptionPane optionPane) {
        List<JButton> buttons = new ArrayList<>();
        findButtons(optionPane, buttons);

        if (buttons.size() >= 2) {
            final JButton yesButton = buttons.get(0);
            final JButton noButton = buttons.get(1);

            SwingUtilities.invokeLater(() -> yesButton.requestFocusInWindow());

            dialog.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            yesButton.requestFocusInWindow();
                            evt.consume();
                            break;
                        case KeyEvent.VK_RIGHT:
                            noButton.requestFocusInWindow();
                            evt.consume();
                            break;
                        case KeyEvent.VK_ENTER:
                            Component focused = dialog.getFocusOwner();
                            if (focused == yesButton) {
                                optionPane.setValue(JOptionPane.YES_OPTION);
                                dialog.dispose();
                            } else if (focused == noButton) {
                                optionPane.setValue(JOptionPane.NO_OPTION);
                                dialog.dispose();
                            }
                            evt.consume();
                            break;
                        case KeyEvent.VK_Y:
                            optionPane.setValue(JOptionPane.YES_OPTION);
                            dialog.dispose();
                            evt.consume();
                            break;
                        case KeyEvent.VK_N:
                            optionPane.setValue(JOptionPane.NO_OPTION);
                            dialog.dispose();
                            evt.consume();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            optionPane.setValue(JOptionPane.NO_OPTION);
                            dialog.dispose();
                            evt.consume();
                            break;
                    }
                }
            });
        }

        dialog.setFocusable(true);
        dialog.requestFocusInWindow();
    }

    private void findButtons(java.awt.Container container, List<JButton> buttons) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                buttons.add((JButton) comp);
            } else if (comp instanceof java.awt.Container) {
                findButtons((java.awt.Container) comp, buttons);
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
            pstNotification.setInt(2, 28); // Expense message type
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (Exception e) {
            // Silent notification creation failure
        } finally {
            try {
                if (pstMassage != null) {
                    pstMassage.close();
                }
                if (pstNotification != null) {
                    pstNotification.close();
                }
            } catch (Exception e) {
                // Silent resource closing failure
            }
        }
    }

    private void createIncomeNotification(String incomeType, double amount, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            String messageText = String.format("New income added | Type: %s | Amount: Rs %.2f", incomeType, amount);

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
            pstNotification.setInt(2, 29); // Income message type
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (Exception e) {
            // Silent notification creation failure
        } finally {
            try {
                if (pstMassage != null) {
                    pstMassage.close();
                }
                if (pstNotification != null) {
                    pstNotification.close();
                }
            } catch (Exception e) {
                // Silent resource closing failure
            }
        }
    }

    private void clearExpenseForm() {
        comboExpensesType.setSelectedIndex(0);
        txtAmount.setText("0");
        paymentDate.setDate(new Date());
        timePicker1.setTime(LocalTime.now());
        jRadioButton1.setSelected(true); // Set default to Paid
        jTextArea1.setText("");
        comboExpensesType.requestFocus();
    }

    private void clearIncomeForm() {
        comboIncomeType.setSelectedIndex(0);
        txtAmount1.setText("0");
        paymentDate1.setDate(new Date());
        timePicker2.setTime(LocalTime.now());
        jRadioButton3.setSelected(true); // Set default to Paid
        jTextArea2.setText("");
        comboIncomeType.requestFocus();
    }

    private void setupFocusTraversal() {
        // Set custom focus traversal policy
        getContentPane().setFocusTraversalPolicy(new javax.swing.LayoutFocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
                int currentTab = jTabbedPane1.getSelectedIndex();

                if (currentTab == 0) { // Expenses tab
                    int index = expenseFocusOrder.indexOf(aComponent);
                    if (index >= 0 && index < expenseFocusOrder.size() - 1) {
                        return expenseFocusOrder.get(index + 1);
                    }
                } else if (currentTab == 1) { // Income tab
                    int index = incomeFocusOrder.indexOf(aComponent);
                    if (index >= 0 && index < incomeFocusOrder.size() - 1) {
                        return incomeFocusOrder.get(index + 1);
                    }
                }
                return super.getComponentAfter(focusCycleRoot, aComponent);
            }

            @Override
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
                int currentTab = jTabbedPane1.getSelectedIndex();

                if (currentTab == 0) { // Expenses tab
                    int index = expenseFocusOrder.indexOf(aComponent);
                    if (index > 0) {
                        return expenseFocusOrder.get(index - 1);
                    }
                } else if (currentTab == 1) { // Income tab
                    int index = incomeFocusOrder.indexOf(aComponent);
                    if (index > 0) {
                        return incomeFocusOrder.get(index - 1);
                    }
                }
                return super.getComponentBefore(focusCycleRoot, aComponent);
            }
        });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        txtAmount = new javax.swing.JTextField();
        comboExpensesType = new javax.swing.JComboBox<>();
        timePicker1 = new com.github.lgooddatepicker.components.TimePicker();
        paymentDate = new com.toedter.calendar.JDateChooser();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        btnCancel1 = new javax.swing.JButton();
        btnClear1 = new javax.swing.JButton();
        btnSave1 = new javax.swing.JButton();
        txtAmount1 = new javax.swing.JTextField();
        comboIncomeType = new javax.swing.JComboBox<>();
        timePicker2 = new com.github.lgooddatepicker.components.TimePicker();
        paymentDate1 = new com.toedter.calendar.JDateChooser();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New Expenses & Income");

        jTabbedPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jTabbedPane1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

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

        btnClear.setText("Clear");
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

        txtAmount.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        txtAmount.setText("0");
        txtAmount.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        txtAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAmountActionPerformed(evt);
            }
        });

        comboExpensesType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboExpensesType.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Expenses Type *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        comboExpensesType.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
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

        timePicker1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Time *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        timePicker1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        timePicker1.setOpaque(false);

        paymentDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        paymentDate.setDateFormatString("MM/dd/yyyy");
        paymentDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        paymentDate.setOpaque(false);
        paymentDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                paymentDateKeyPressed(evt);
            }
        });

        jRadioButton1.setText("Paid");
        jRadioButton1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N

        jRadioButton2.setText("UnPaid");
        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(comboExpensesType, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(paymentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timePicker1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(txtAmount)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton2)))
                .addGap(21, 21, 21))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        jTabbedPane1.addTab("Expenses", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        btnCancel1.setText("Cancel");
        btnCancel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnCancel1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnCancel1.setForeground(new java.awt.Color(8, 147, 176));
        btnCancel1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancel1ActionPerformed(evt);
            }
        });
        btnCancel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCancel1KeyPressed(evt);
            }
        });

        btnClear1.setText("Clear");
        btnClear1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnClear1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnClear1.setForeground(new java.awt.Color(8, 147, 176));
        btnClear1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClear1ActionPerformed(evt);
            }
        });
        btnClear1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnClear1KeyPressed(evt);
            }
        });

        btnSave1.setText("Save");
        btnSave1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        btnSave1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        btnSave1.setForeground(new java.awt.Color(8, 147, 176));
        btnSave1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSave1ActionPerformed(evt);
            }
        });
        btnSave1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnSave1KeyPressed(evt);
            }
        });

        txtAmount1.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        txtAmount1.setText("0");
        txtAmount1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        txtAmount1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAmount1ActionPerformed(evt);
            }
        });

        comboIncomeType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboIncomeType.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Income Type *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        comboIncomeType.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        comboIncomeType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboIncomeTypeActionPerformed(evt);
            }
        });
        comboIncomeType.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comboIncomeTypeKeyPressed(evt);
            }
        });

        timePicker2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Time *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        timePicker2.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        timePicker2.setOpaque(false);

        paymentDate1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        paymentDate1.setDateFormatString("MM/dd/yyyy");
        paymentDate1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        paymentDate1.setOpaque(false);
        paymentDate1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                paymentDate1KeyPressed(evt);
            }
        });

        jRadioButton3.setText("Paid");
        jRadioButton3.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N

        jRadioButton4.setText("UnPaid");
        jRadioButton4.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        jTextArea2.setColumns(20);
        jTextArea2.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jTextArea2.setRows(5);
        jTextArea2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Description (Optional)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        jScrollPane2.setViewportView(jTextArea2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnCancel1, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(comboIncomeType, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(paymentDate1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timePicker2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(txtAmount1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton4)))
                .addGap(21, 21, 21))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(comboIncomeType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(paymentDate1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtAmount1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButton3)
                            .addComponent(jRadioButton4))
                        .addGap(18, 18, 18)))
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSave1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        jTabbedPane1.addTab("Income", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton2ActionPerformed

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

    private void comboExpensesTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboExpensesTypeActionPerformed
        if (comboExpensesType.getSelectedIndex() > 0 && !comboExpensesType.isPopupVisible()) {
            paymentDate.requestFocusInWindow();
        }
    }//GEN-LAST:event_comboExpensesTypeActionPerformed

    private void txtAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmountActionPerformed

    private void btnSaveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnSaveKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveExpense();
            evt.consume();
        }
    }//GEN-LAST:event_btnSaveKeyPressed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveExpense();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnClearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnClearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
            evt.consume();
        }
    }//GEN-LAST:event_btnClearKeyPressed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearExpenseForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnCancelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCancelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
            evt.consume();
        }
    }//GEN-LAST:event_btnCancelKeyPressed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCancel1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancel1ActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancel1ActionPerformed

    private void btnCancel1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCancel1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
            evt.consume();
        }
    }//GEN-LAST:event_btnCancel1KeyPressed

    private void btnClear1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClear1ActionPerformed
        clearIncomeForm();
    }//GEN-LAST:event_btnClear1ActionPerformed

    private void btnClear1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnClear1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearIncomeForm();
            evt.consume();
        }
    }//GEN-LAST:event_btnClear1KeyPressed

    private void btnSave1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSave1ActionPerformed
        saveIncome();
    }//GEN-LAST:event_btnSave1ActionPerformed

    private void btnSave1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnSave1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveIncome();
            evt.consume();
        }
    }//GEN-LAST:event_btnSave1KeyPressed

    private void txtAmount1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmount1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmount1ActionPerformed

    private void comboIncomeTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboIncomeTypeActionPerformed
        if (comboIncomeType.getSelectedIndex() > 0 && !comboIncomeType.isPopupVisible()) {
            paymentDate1.requestFocusInWindow();
        }
    }//GEN-LAST:event_comboIncomeTypeActionPerformed

    private void comboIncomeTypeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboIncomeTypeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (comboIncomeType.isPopupVisible()) {
                comboIncomeType.setPopupVisible(false);
            }
            if (comboIncomeType.getSelectedIndex() > 0) {
                paymentDate1.requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!comboIncomeType.isPopupVisible()) {
                comboIncomeType.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!comboIncomeType.isPopupVisible()) {
                btnSave1.requestFocusInWindow();
                evt.consume();
            }
        }
    }//GEN-LAST:event_comboIncomeTypeKeyPressed

    private void paymentDate1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paymentDate1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            timePicker2.requestFocusInWindow();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            timePicker2.requestFocusInWindow();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            comboIncomeType.requestFocusInWindow();
            evt.consume();
        }
    }//GEN-LAST:event_paymentDate1KeyPressed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton4ActionPerformed

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
            java.util.logging.Logger.getLogger(ExpenseIncomeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ExpenseIncomeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ExpenseIncomeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ExpenseIncomeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ExpenseIncomeDialog dialog = new ExpenseIncomeDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnCancel1;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnClear1;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSave1;
    private javax.swing.JComboBox<String> comboExpensesType;
    private javax.swing.JComboBox<String> comboIncomeType;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private com.toedter.calendar.JDateChooser paymentDate;
    private com.toedter.calendar.JDateChooser paymentDate1;
    private com.github.lgooddatepicker.components.TimePicker timePicker1;
    private com.github.lgooddatepicker.components.TimePicker timePicker2;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtAmount1;
    // End of variables declaration//GEN-END:variables
}
