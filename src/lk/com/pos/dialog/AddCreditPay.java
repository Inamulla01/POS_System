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
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import lk.com.pos.connection.MySQL;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

// Add barcode printing imports
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

public class AddCreditPay extends javax.swing.JDialog {

    private Integer customerId;
    private double remainingAmount = 0.0;
    private double originalRemainingAmount = 0.0;
    private boolean isSaving = false;
    private Map<String, Integer> customerIdMap = new HashMap<>();
    private Map<String, Double> customerDiscountMap = new HashMap<>();
    private Map<String, String> customerDiscountTypeMap = new HashMap<>();
    private Map<String, Double> customerRemainingAmountMap = new HashMap<>();
    private double paidAmount = 0.0;
    private double discountAmount = 0.0;
    private double discountPercentage = 0.0;
    private boolean hasDiscount = false;
    private boolean customerDataLoaded = false; // Add flag to track if customer data is loaded

    public AddCreditPay(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
        loadCustomerData(); // Load customer data even without customerId
        customerCombo.addActionListener(e -> updateRemainingAmountForSelectedCustomer());
    }

    public AddCreditPay(java.awt.Frame parent, boolean modal, int customerId) {
        super(parent, modal);
        this.customerId = customerId;
        initComponents();
        initializeDialog();
        loadCustomerData(); // Load customer data with customerId
        customerCombo.addActionListener(e -> updateRemainingAmountForSelectedCustomer());
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());

        loadCustomerCombo();
        AutoCompleteDecorator.decorate(customerCombo);

        paymentDate.setDate(new Date());

        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();
        setupFocusTraversal();

        // Auto-generate barcode on open
        autoGenerateBarcode();

        customerCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                customerCombo.showPopup();
            }
        });

        getRootPane().registerKeyboardAction(
                evt -> refreshCustomers(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Add F3 and F4 shortcuts for barcode operations
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generateBarcode();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                printBarcode();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        customerCombo.requestFocusInWindow();
    }

    // New method to update remaining amount when customer is selected
    private void updateRemainingAmountForSelectedCustomer() {
        if (customerCombo.getSelectedIndex() > 0) {
            updateSelectedCustomerRemainingAmount();
        }
    }

    private void autoGenerateBarcode() {
        try {
            // Get the last credit payment barcode from database
            String sql = "SELECT credit_pay_barcode FROM credit_pay WHERE credit_pay_barcode LIKE 'CRDPAY%' ORDER BY LENGTH(credit_pay_barcode), credit_pay_barcode DESC LIMIT 1";
            ResultSet rs = MySQL.executeSearch(sql);

            int lastNumber = 0;
            if (rs.next()) {
                String lastBarcode = rs.getString("credit_pay_barcode");
                if (lastBarcode != null && lastBarcode.startsWith("CRDPAY")) {
                    try {
                        // Extract the number part (everything after "CRDPAY")
                        String numberPart = lastBarcode.substring(6);
                        lastNumber = Integer.parseInt(numberPart);
                    } catch (NumberFormatException e) {
                        lastNumber = 0;
                    }
                }
            }

            // Generate next barcode with proper format: CRDPAY0000001
            String nextBarcode = String.format("CRDPAY%07d", lastNumber + 1);
            barcodeField.setText(nextBarcode);

            // Make barcode field editable
            barcodeField.setEditable(true);

        } catch (Exception e) {
            // If error, generate starting barcode
            String barcode = "CRDPAY0000001";
            barcodeField.setText(barcode);
            barcodeField.setEditable(true);
        }
    }

    private void setupKeyboardNavigation() {
        customerCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    updateRemainingAmountForSelectedCustomer(); // Update when navigating away
                    paymentDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    updateRemainingAmountForSelectedCustomer(); // Update when navigating away
                    paymentDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    updateRemainingAmountForSelectedCustomer(); // Update when navigating away
                    saveBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, customerCombo);
                }
            }
        });

        javax.swing.JTextField paymentDateEditor = (javax.swing.JTextField) paymentDate.getDateEditor().getUiComponent();
        paymentDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    amountField.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    customerCombo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    amountField.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    customerCombo.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, paymentDateEditor);
                }
            }
        });

        amountField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    barcodeField.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    paymentDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    barcodeField.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    paymentDate.getDateEditor().getUiComponent().requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, amountField);
                }
            }
        });

        barcodeField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    amountField.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    amountField.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, barcodeField);
                }
            }
        });

        genarateBarecode.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    generateBarcode();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, genarateBarecode);
                }
            }
        });

        printBarcode.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    printBarcode();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, printBarcode);
                }
            }
        });

        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveCreditPay();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    barcodeField.requestFocus();
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
                    barcodeField.requestFocus();
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
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    barcodeField.requestFocus();
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
            paymentDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == paymentDate.getDateEditor().getUiComponent()) {
            amountField.requestFocusInWindow();
        } else if (source == amountField) {
            barcodeField.requestFocusInWindow();
        } else if (source == barcodeField) {
            genarateBarecode.requestFocusInWindow();
        } else if (source == genarateBarecode) {
            printBarcode.requestFocusInWindow();
        } else if (source == printBarcode) {
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
        } else if (source == paymentDate.getDateEditor().getUiComponent()) {
            customerCombo.requestFocusInWindow();
        } else if (source == amountField) {
            paymentDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == barcodeField) {
            amountField.requestFocusInWindow();
        } else if (source == genarateBarecode) {
            barcodeField.requestFocusInWindow();
        } else if (source == printBarcode) {
            genarateBarecode.requestFocusInWindow();
        } else if (source == cancelBtn) {
            printBarcode.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == customerCombo) {
            paymentDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == paymentDate.getDateEditor().getUiComponent()) {
            amountField.requestFocusInWindow();
        } else if (source == amountField) {
            barcodeField.requestFocusInWindow();
        } else if (source == barcodeField) {
            cancelBtn.requestFocusInWindow();
        } else if (source == genarateBarecode) {
            cancelBtn.requestFocusInWindow();
        } else if (source == printBarcode) {
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
        } else if (source == paymentDate.getDateEditor().getUiComponent()) {
            customerCombo.requestFocusInWindow();
        } else if (source == amountField) {
            paymentDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == barcodeField) {
            amountField.requestFocusInWindow();
        } else if (source == genarateBarecode) {
            barcodeField.requestFocusInWindow();
        } else if (source == printBarcode) {
            barcodeField.requestFocusInWindow();
        } else if (source == cancelBtn) {
            barcodeField.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            barcodeField.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return customerCombo.getSelectedIndex() > 0
                && paymentDate.getDate() != null
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

        // Setup barcode buttons
        setupBarcodeButton(genarateBarecode);
        setupBarcodeButton(printBarcode);

        FlatSVGIcon barcodeIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 20, 20);
        barcodeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        genarateBarecode.setIcon(barcodeIcon);

        FlatSVGIcon printerIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 20, 20);
        printerIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        printBarcode.setIcon(printerIcon);

        setupButtonMouseListeners();
        setupButtonFocusListeners();
        setupSaveButton();
    }

    private void setupBarcodeButton(JButton button) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setFocusable(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupSaveButton() {
        for (java.awt.event.ActionListener al : saveBtn.getActionListeners()) {
            saveBtn.removeActionListener(al);
        }

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

        genarateBarecode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 20, 20);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                genarateBarecode.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 20, 20);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                genarateBarecode.setIcon(normalIcon);
            }
        });

        printBarcode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 20, 20);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                printBarcode.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 20, 20);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                printBarcode.setIcon(normalIcon);
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

        genarateBarecode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 20, 20);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                genarateBarecode.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 20, 20);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                genarateBarecode.setIcon(normalIcon);
            }
        });

        printBarcode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 20, 20);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                printBarcode.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 20, 20);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                printBarcode.setIcon(normalIcon);
            }
        });
    }

    private void setupTooltips() {
        customerCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field<br>Press <b>F2</b> to refresh customer list</html>");
        paymentDate.setToolTipText("<html>Type date in format dd/mm/yyyy then press ENTER<br>You can also type numbers: 01012024 for 01/01/2024</html>");
        amountField.setToolTipText("Type payment amount and press ENTER to move to barcode field");
        barcodeField.setToolTipText("<html>Type barcode and press ENTER to move to buttons<br>Press <b>F3</b> to generate barcode<br>Press <b>F4</b> to print barcode<br>Barcode is editable</html>");
        genarateBarecode.setToolTipText("Click to generate barcode (or press F3)");
        printBarcode.setToolTipText("Click to print barcode (or press F4)");
        saveBtn.setToolTipText("Click to save credit payment (or press ENTER when focused)");
        clearFormBtn.setToolTipText("Click to clear form (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
    }

    private void generateBarcode() {
        try {
            // Get the last credit payment barcode from database
            String sql = "SELECT credit_pay_barcode FROM credit_pay WHERE credit_pay_barcode LIKE 'CRDPAY%' ORDER BY LENGTH(credit_pay_barcode), credit_pay_barcode DESC LIMIT 1";
            ResultSet rs = MySQL.executeSearch(sql);

            int lastNumber = 0;
            if (rs.next()) {
                String lastBarcode = rs.getString("credit_pay_barcode");
                if (lastBarcode != null && lastBarcode.startsWith("CRDPAY")) {
                    try {
                        // Extract the number part (everything after "CRDPAY")
                        String numberPart = lastBarcode.substring(6);
                        lastNumber = Integer.parseInt(numberPart);
                    } catch (NumberFormatException e) {
                        lastNumber = 0;
                    }
                }
            }

            // Generate next barcode with proper format: CRDPAY0000001
            String nextBarcode = String.format("CRDPAY%07d", lastNumber + 1);
            barcodeField.setText(nextBarcode);

            // Make barcode field editable
            barcodeField.setEditable(true);

            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT,
                    "Barcode generated: " + nextBarcode);

        } catch (Exception e) {
            // If error, generate starting barcode
            String nextBarcode = String.format("CRDPAY%07d", 1);
            barcodeField.setText(nextBarcode);
            barcodeField.setEditable(true);

            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT,
                    "Barcode generated: " + nextBarcode);
        }
    }

    private void printBarcode() {
        String barcodeText = barcodeField.getText().trim();

        if (barcodeText.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please generate or enter a barcode first");
            barcodeField.requestFocus();
            return;
        }

        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("Credit Payment Barcode - " + barcodeText);

            printerJob.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return Printable.NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Set up fonts
                    Font barcodeFont = new Font("Monospaced", Font.BOLD, 24);
                    Font labelFont = new Font("Nunito SemiBold", Font.PLAIN, 12);

                    // Draw barcode number
                    g2d.setFont(barcodeFont);
                    g2d.drawString(barcodeText, 50, 100);

                    // Draw label
                    g2d.setFont(labelFont);
                    g2d.drawString("Credit Payment Receipt", 50, 130);
                    g2d.drawString("Generated by POS System", 50, 145);

                    // Draw current date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    g2d.drawString("Date: " + dateFormat.format(new Date()), 50, 160);

                    return Printable.PAGE_EXISTS;
                }
            });

            if (printerJob.printDialog()) {
                printerJob.print();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "Barcode printed successfully!");
            }
        } catch (PrinterException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Printing error: " + e.getMessage());
        }
    }

    private void loadCustomerCombo() {
        try {
            customerIdMap.clear();
            customerDiscountMap.clear();
            customerDiscountTypeMap.clear();
            customerRemainingAmountMap.clear();

            // FIXED: Correct SQL query that properly calculates remaining amount
            String sql = "SELECT "
                    + "    cc.customer_id, "
                    + "    cc.customer_name, "
                    + "    COALESCE((SELECT SUM(credit_amout) FROM credit WHERE credit_customer_id = cc.customer_id), 0) as total_credit, "
                    + "    COALESCE((SELECT SUM(credit_pay_amount) FROM credit_pay WHERE credit_customer_id = cc.customer_id), 0) as total_paid "
                    + "FROM credit_customer cc "
                    + "WHERE cc.status_id = 1 "
                    + "ORDER BY cc.customer_name";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> customers = new Vector<>();
            customers.add("Select Customer");

            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                double totalCredit = rs.getDouble("total_credit");
                double totalPaid = rs.getDouble("total_paid");
                double remaining = totalCredit - totalPaid;

                System.out.println("DEBUG - Loading customer: " + customerName
                        + ", ID: " + customerId
                        + ", Total Credit: " + totalCredit
                        + ", Total Paid: " + totalPaid
                        + ", Remaining: " + remaining);

                if (remaining > 0) {
                    String displayText = String.format("%s - Due: %.2f", customerName, remaining);
                    customers.add(displayText);
                    customerIdMap.put(displayText, customerId);
                    customerRemainingAmountMap.put(displayText, remaining);
                    System.out.println("DEBUG - Added to combo: " + displayText + " (ID: " + customerId + ")");
                }
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(customers);
            customerCombo.setModel(dcm);

            // Debug the maps
            debugCustomerMaps();

            // If customerId was provided in constructor, select that customer
            if (this.customerId != null && this.customerId > 0) {
                selectCustomerById(this.customerId);
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void debugCustomerMaps() {
        System.out.println("=== DEBUG: Customer Maps ===");
        System.out.println("Customer ID Map size: " + customerIdMap.size());
        System.out.println("Customer Remaining Amount Map size: " + customerRemainingAmountMap.size());

        for (Map.Entry<String, Integer> entry : customerIdMap.entrySet()) {
            System.out.println("Key: " + entry.getKey()
                    + ", ID: " + entry.getValue()
                    + ", Remaining: " + customerRemainingAmountMap.get(entry.getKey()));
        }
        System.out.println("=== END DEBUG ===");
    }

    private void selectCustomerById(int customerId) {
        try {
            String sql = "SELECT customer_name FROM credit_customer WHERE customer_id = ?";
            Connection conn = MySQL.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String customerName = rs.getString("customer_name");

                // Find and select the customer in combo box
                for (int i = 0; i < customerCombo.getItemCount(); i++) {
                    String item = customerCombo.getItemAt(i);
                    if (item.startsWith(customerName + " - Due: ")) {
                        customerCombo.setSelectedIndex(i);
                        updateSelectedCustomerRemainingAmount();
                        break;
                    }
                }
            }

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            // Ignore error - customer may not be in the list
            System.out.println("DEBUG - Error selecting customer by ID: " + e.getMessage());
        }
    }

    private void loadCustomerDiscount(int customerId) {
        try {
            String sql = "SELECT d.discount, dt.discount_type "
                    + "FROM credit_discount cd "
                    + "JOIN discount d ON cd.discount_id = d.discount_id "
                    + "JOIN discount_type dt ON d.discount_type_id = dt.discount_type_id "
                    + "WHERE cd.credit_id = ? "
                    + "ORDER BY d.discount_id DESC LIMIT 1";

            Connection conn = MySQL.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            hasDiscount = false;
            discountAmount = 0.0;
            discountPercentage = 0.0;

            if (rs.next()) {
                double discountValue = rs.getDouble("discount");
                String discountType = rs.getString("discount_type");
                hasDiscount = true;

                if ("percentage".equals(discountType)) {
                    discountPercentage = discountValue;
                    discountAmount = originalRemainingAmount * (discountPercentage / 100);
                } else if ("fixed amount".equals(discountType)) {
                    discountAmount = discountValue;
                    discountPercentage = (discountAmount / originalRemainingAmount) * 100;
                }
                System.out.println("DEBUG - Discount loaded: " + discountType + " = " + discountValue);
            }

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            // Discount might not exist for this customer, which is fine
            System.out.println("DEBUG - No discount found for customer: " + e.getMessage());
        }
    }

    private void loadCustomerInfo() {
        try {
            // Fixed SQL query using COALESCE to handle null values
            String sql = "SELECT "
                    + "    cc.customer_id, "
                    + "    cc.customer_name, "
                    + "    COALESCE((SELECT SUM(credit_amout) FROM credit WHERE credit_customer_id = cc.customer_id), 0) as total_credit, "
                    + "    COALESCE((SELECT SUM(credit_pay_amount) FROM credit_pay WHERE credit_customer_id = cc.customer_id), 0) as total_paid "
                    + "FROM credit_customer cc "
                    + "WHERE cc.customer_id = ?";

            Connection conn = MySQL.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String customerName = rs.getString("customer_name");
                double totalCredit = rs.getDouble("total_credit");
                double totalPaid = rs.getDouble("total_paid");
                originalRemainingAmount = totalCredit - totalPaid;
                remainingAmount = originalRemainingAmount;

                System.out.println("DEBUG - loadCustomerInfo: " + customerName
                        + ", Total Credit: " + totalCredit
                        + ", Total Paid: " + totalPaid
                        + ", Original Remaining: " + originalRemainingAmount);

                // Load discount for customer
                loadCustomerDiscount(customerId);

                // Apply discount to remaining amount
                if (hasDiscount) {
                    remainingAmount = originalRemainingAmount - discountAmount;
                    if (remainingAmount < 0) {
                        remainingAmount = 0;
                    }
                    System.out.println("DEBUG - After discount: " + remainingAmount);
                }

                updateRemainingAmountLabel();

                // Select customer in combo box
                selectCustomerById(customerId);
            }

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading customer info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateRemainingAmountLabel() {
        String tooltipText = "Type payment amount (max: " + remainingAmount + ") and press ENTER to move to barcode field";
        if (hasDiscount) {
            tooltipText += "\nDiscount applied: " + discountPercentage + "% (Rs. " + discountAmount + ")";
        }
        amountField.setToolTipText(tooltipText);
        System.out.println("DEBUG - Tooltip set: " + tooltipText);
    }

    private void updateSelectedCustomerRemainingAmount() {
        String selected = (String) customerCombo.getSelectedItem();
        System.out.println("DEBUG - updateSelectedCustomerRemainingAmount called");
        System.out.println("DEBUG - Selected customer text: " + selected);

        if (selected == null || selected.equals("Select Customer")) {
            remainingAmount = 0.0;
            originalRemainingAmount = 0.0;
            hasDiscount = false;
            System.out.println("DEBUG - No customer selected or 'Select Customer' chosen");
            updateRemainingAmountLabel();
            return;
        }

        // Get customer ID from map
        Integer customerId = customerIdMap.get(selected);
        System.out.println("DEBUG - Customer ID from map: " + customerId);

        if (customerId == null) {
            remainingAmount = 0.0;
            originalRemainingAmount = 0.0;
            hasDiscount = false;
            System.out.println("DEBUG - Customer ID not found in map");
            updateRemainingAmountLabel();
            return;
        }

        // Get remaining amount directly from database to ensure accuracy
        try {
            String sql = "SELECT "
                    + "    COALESCE((SELECT SUM(credit_amout) FROM credit WHERE credit_customer_id = ?), 0) - "
                    + "    COALESCE((SELECT SUM(credit_pay_amount) FROM credit_pay WHERE credit_customer_id = ?), 0) as remaining_amount";

            Connection conn = MySQL.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, customerId);
            pst.setInt(2, customerId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                originalRemainingAmount = rs.getDouble("remaining_amount");
                remainingAmount = originalRemainingAmount;
                System.out.println("DEBUG - Database query result - Original remaining: " + originalRemainingAmount);

                // Check if this customer has discount
                loadCustomerDiscount(customerId);

                // Apply discount to remaining amount
                if (hasDiscount) {
                    remainingAmount = originalRemainingAmount - discountAmount;
                    if (remainingAmount < 0) {
                        remainingAmount = 0;
                    }
                    System.out.println("DEBUG - After discount - Remaining: " + remainingAmount + ", Discount: " + discountAmount);
                }
            } else {
                originalRemainingAmount = 0.0;
                remainingAmount = 0.0;
                hasDiscount = false;
                System.out.println("DEBUG - No data returned from database query");
            }

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            // If database query fails, fall back to the cached value
            originalRemainingAmount = customerRemainingAmountMap.getOrDefault(selected, 0.0);
            remainingAmount = originalRemainingAmount;
            hasDiscount = false;

            System.err.println("DEBUG - Error updating remaining amount for customer: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("DEBUG - Final remaining amount: " + remainingAmount);
        System.out.println("DEBUG - Will validate against: " + amountField.getText());
        updateRemainingAmountLabel();
    }

    private void loadCustomerData() {
        customerDataLoaded = true;
        if (customerId != null && customerId > 0) {
            loadCustomerInfo();
        } else {
            // When opening without customerId, ensure the combo box is loaded
            // and set up the action listener for customer selection
            loadCustomerCombo();
            System.out.println("DEBUG - Customer data loaded without specific customer ID");
        }
    }

    private int getSelectedCustomerId() {
        String selected = (String) customerCombo.getSelectedItem();
        if (selected == null || selected.equals("Select Customer")) {
            return -1;
        }

        Integer customerId = customerIdMap.get(selected);
        return customerId != null ? customerId : -1;
    }

    private boolean validateInputs() {
        System.out.println("DEBUG - validateInputs called");
        System.out.println("DEBUG - Customer selected index: " + customerCombo.getSelectedIndex());
        System.out.println("DEBUG - Payment date: " + paymentDate.getDate());
        System.out.println("DEBUG - Amount field text: " + amountField.getText());
        System.out.println("DEBUG - Remaining amount: " + remainingAmount);

        // Ensure remaining amount is updated if customer is selected
        if (customerCombo.getSelectedIndex() > 0) {
            updateSelectedCustomerRemainingAmount();
        }

        if (customerCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a customer");
            customerCombo.requestFocus();
            return false;
        }

        if (paymentDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select payment date");
            paymentDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter payment amount");
            amountField.requestFocus();
            return false;
        }

        if (!amountText.matches("^[0-9]*\\.?[0-9]+$")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid payment amount");
            amountField.requestFocus();
            return false;
        }

        double amount = Double.parseDouble(amountText);
        System.out.println("DEBUG - Parsed amount: " + amount);

        if (amount <= 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Payment amount must be greater than 0");
            amountField.requestFocus();
            return false;
        }

        System.out.println("DEBUG - Checking if " + amount + " > " + remainingAmount);
        if (amount > remainingAmount) {
            String message = String.format("Payment amount (%.2f) cannot exceed remaining amount: %.2f", amount, remainingAmount);
            if (hasDiscount) {
                message += String.format("\n(Original due: %.2f - Discount: %.2f = %.2f)",
                        originalRemainingAmount, discountAmount, remainingAmount);
            }
            System.out.println("DEBUG - Validation failed: " + message);
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, message);
            amountField.requestFocus();
            return false;
        }

        // Validate barcode
        String barcodeText = barcodeField.getText().trim();
        if (barcodeText.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter or generate barcode");
            barcodeField.requestFocus();
            return false;
        }

        // Validate barcode format - should start with CRDPAY and have 7 digits
        if (!barcodeText.matches("CRDPAY\\d{7}")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Barcode must be in format: CRDPAY followed by 7 digits (e.g., CRDPAY0000001)");
            barcodeField.requestFocus();
            return false;
        }

        System.out.println("DEBUG - All validations passed");
        return true;
    }

    private void saveCreditPay() {
        if (isSaving) {
            return;
        }

        isSaving = true;

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            System.out.println("DEBUG - saveCreditPay called");
            if (!validateInputs()) {
                isSaving = false;
                return;
            }

            int selectedCustomerId = getSelectedCustomerId();
            if (selectedCustomerId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid customer selected");
                isSaving = false;
                return;
            }

            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String paymentDateStr = datetimeFormat.format(paymentDate.getDate());

            double amount = Double.parseDouble(amountField.getText().trim());
            String barcode = barcodeField.getText().trim();

            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

            // Check if barcode already exists
            String checkBarcodeSql = "SELECT COUNT(*) FROM credit_pay WHERE credit_pay_barcode = ?";
            PreparedStatement pstCheckBarcode = conn.prepareStatement(checkBarcodeSql);
            pstCheckBarcode.setString(1, barcode);
            ResultSet rsBarcode = pstCheckBarcode.executeQuery();
            if (rsBarcode.next() && rsBarcode.getInt(1) > 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Barcode already exists. Please generate a new one.");
                barcodeField.requestFocus();
                barcodeField.selectAll();

                rsBarcode.close();
                pstCheckBarcode.close();
                conn.rollback();
                conn.setAutoCommit(true);
                conn.close();
                isSaving = false;
                return;
            }
            rsBarcode.close();
            pstCheckBarcode.close();

            // Insert into credit_pay table with customer_id and barcode
            String query = "INSERT INTO credit_pay (credit_pay_date, credit_pay_amount, credit_pay_barcode, credit_customer_id) VALUES (?, ?, ?, ?)";

            pst = conn.prepareStatement(query);
            pst.setString(1, paymentDateStr);
            pst.setDouble(2, amount);
            pst.setString(3, barcode);
            pst.setInt(4, selectedCustomerId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                this.paidAmount = amount;

                // Check if customer still has due amount using proper calculation
                String checkDueSql = "SELECT "
                        + "COALESCE((SELECT SUM(credit_amout) FROM credit WHERE credit_customer_id = ?), 0) - "
                        + "COALESCE((SELECT SUM(credit_pay_amount) FROM credit_pay WHERE credit_customer_id = ?), 0) as remaining_amount";

                PreparedStatement pstCheck = conn.prepareStatement(checkDueSql);
                pstCheck.setInt(1, selectedCustomerId);
                pstCheck.setInt(2, selectedCustomerId);
                ResultSet rs = pstCheck.executeQuery();

                if (rs.next()) {
                    double newRemaining = rs.getDouble("remaining_amount");
                    // Update customer status if no due amount
                    if (newRemaining <= 0) {
                        String updateStatusSql = "UPDATE credit_customer SET status_id = 2 WHERE customer_id = ?";
                        PreparedStatement pstUpdate = conn.prepareStatement(updateStatusSql);
                        pstUpdate.setInt(1, selectedCustomerId);
                        pstUpdate.executeUpdate();
                        pstUpdate.close();
                    }
                }
                rs.close();
                pstCheck.close();

                createCreditPaymentNotification(selectedCustomerId, amount, barcode, conn);

                conn.commit();

                String successMessage = String.format("Credit payment added successfully! Amount: Rs. %.2f | Barcode: %s", amount, barcode);
                if (hasDiscount) {
                    successMessage += String.format("\nDiscount applied: Rs. %.2f (%.2f%%)", discountAmount, discountPercentage);
                }
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, successMessage);
                dispose();
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
                // Rollback exception ignored
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving credit payment: " + e.getMessage());
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
                // Closing resources exception ignored
            }
            isSaving = false;
        }
    }

    private void createCreditPaymentNotification(int customerId, double amount, String barcode, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;
        PreparedStatement pstCustomerInfo = null;

        try {
            String customerInfoSql = "SELECT customer_name FROM credit_customer WHERE customer_id = ?";
            pstCustomerInfo = conn.prepareStatement(customerInfoSql);
            pstCustomerInfo.setInt(1, customerId);
            ResultSet rs = pstCustomerInfo.executeQuery();

            String customerName = "Unknown Customer";
            if (rs.next()) {
                customerName = rs.getString("customer_name");
            }

            String messageText = String.format("Credit payment received from %s: Rs.%,.2f (Barcode: %s)", customerName, amount, barcode);
            if (hasDiscount) {
                messageText += String.format(" (Discount applied: Rs.%,.2f)", discountAmount);
            }

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
            pstNotification.setInt(2, 12); // Using msg_type_id 12 for "Credit Payed"
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (Exception e) {
            // Error handled silently as notification creation is not critical
        } finally {
            try {
                if (pstCustomerInfo != null) {
                    pstCustomerInfo.close();
                }
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
        paymentDate.setDate(new Date());
        amountField.setText("");

        // Auto-generate new barcode when form is cleared
        autoGenerateBarcode();

        remainingAmount = 0.0;
        originalRemainingAmount = 0.0;
        hasDiscount = false;
        discountAmount = 0.0;
        discountPercentage = 0.0;
        customerCombo.requestFocus();
    }

    private void refreshCustomers() {
        loadCustomerCombo();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Customer list refreshed!");
        customerCombo.requestFocus();
    }

    private void setupFocusTraversal() {
        try {
            if (paymentDate.getDateEditor() != null && paymentDate.getDateEditor().getUiComponent() != null) {
                paymentDate.getDateEditor().getUiComponent().setFocusable(true);
            }
        } catch (Exception e) {
            // Focus traversal setup error handled silently
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
        customerCombo = new javax.swing.JComboBox<>();
        paymentDate = new com.toedter.calendar.JDateChooser();
        amountField = new javax.swing.JTextField();
        barcodeField = new javax.swing.JTextField();
        printBarcode = new javax.swing.JButton();
        genarateBarecode = new javax.swing.JButton();

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

        paymentDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Given Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        paymentDate.setDateFormatString("MM/dd/yyyy");
        paymentDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        paymentDate.setOpaque(false);
        paymentDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                paymentDateKeyPressed(evt);
            }
        });

        amountField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        amountField.setText("0");
        amountField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Amount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        amountField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                amountFieldActionPerformed(evt);
            }
        });

        barcodeField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        barcodeField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Barcode  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        barcodeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barcodeFieldActionPerformed(evt);
            }
        });

        printBarcode.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        printBarcode.setMaximumSize(new java.awt.Dimension(75, 7));
        printBarcode.setMinimumSize(new java.awt.Dimension(75, 7));
        printBarcode.setPreferredSize(new java.awt.Dimension(75, 7));
        printBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printBarcodeActionPerformed(evt);
            }
        });
        printBarcode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                printBarcodeKeyPressed(evt);
            }
        });

        genarateBarecode.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        genarateBarecode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genarateBarecodeActionPerformed(evt);
            }
        });
        genarateBarecode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                genarateBarecodeKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                genarateBarecodeKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(83, 83, 83))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paymentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(amountField, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(customerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(barcodeField)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(genarateBarecode, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(printBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(2, 2, 2)))
                .addGap(0, 21, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(customerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(paymentDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(amountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(barcodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(printBarcode, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(genarateBarecode, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
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

    private void customerComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerComboActionPerformed
        if (customerCombo.getSelectedIndex() > 0 && !customerCombo.isPopupVisible()) {
            updateSelectedCustomerRemainingAmount();
            paymentDate.getDateEditor().getUiComponent().requestFocusInWindow();
        }
    }//GEN-LAST:event_customerComboActionPerformed

    private void customerComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_customerComboKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (customerCombo.isPopupVisible()) {
                customerCombo.setPopupVisible(false);
            }
            if (customerCombo.getSelectedIndex() > 0) {
                updateSelectedCustomerRemainingAmount();
                paymentDate.getDateEditor().getUiComponent().requestFocusInWindow();
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

    private void paymentDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paymentDateKeyPressed
        handleArrowNavigation(evt, paymentDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_paymentDateKeyPressed

    private void amountFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_amountFieldActionPerformed
        barcodeField.requestFocusInWindow();
    }//GEN-LAST:event_amountFieldActionPerformed

    private void barcodeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeFieldActionPerformed
        if (areAllRequiredFieldsFilled()) {
            saveBtn.requestFocusInWindow();
        } else {
            cancelBtn.requestFocusInWindow();
        }

    }//GEN-LAST:event_barcodeFieldActionPerformed

    private void printBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBarcodeActionPerformed
        printBarcode();
    }//GEN-LAST:event_printBarcodeActionPerformed

    private void printBarcodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_printBarcodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            printBarcodeActionPerformed(null);
        } else {
            handleArrowNavigation(evt, printBarcode);
        }
    }//GEN-LAST:event_printBarcodeKeyPressed

    private void genarateBarecodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genarateBarecodeActionPerformed
        generateBarcode();
    }//GEN-LAST:event_genarateBarecodeActionPerformed

    private void genarateBarecodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_genarateBarecodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            genarateBarecodeActionPerformed(null);
        } else {
            handleArrowNavigation(evt, genarateBarecode);
        }
    }//GEN-LAST:event_genarateBarecodeKeyPressed

    private void genarateBarecodeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_genarateBarecodeKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_genarateBarecodeKeyReleased

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
    private javax.swing.JTextField amountField;
    private javax.swing.JTextField barcodeField;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JComboBox<String> customerCombo;
    private javax.swing.JButton genarateBarecode;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private com.toedter.calendar.JDateChooser paymentDate;
    private javax.swing.JButton printBarcode;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
