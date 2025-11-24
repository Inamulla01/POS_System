package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import lk.com.pos.dialogpanel.ExchangeProduct;
import lk.com.pos.session.Session;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ExchangeProductDialog extends javax.swing.JDialog {

    private Map<Integer, String> invoiceMap = new HashMap<>();
    private Map<Integer, String> reasonMap = new HashMap<>();
    private Map<Integer, Integer> productOriginalQtys = new HashMap<>();
    private Map<Integer, Integer> productSaleItemIds = new HashMap<>();
    private Map<Integer, Integer> productStockIds = new HashMap<>();
    private Map<Integer, Double> productUnitPrices = new HashMap<>();
    private Map<Integer, Double> productDiscountPrices = new HashMap<>();
    private Map<Integer, Boolean> productSelectedStatus = new HashMap<>();
    private double invoiceTotalDiscount = 0.0;
    private String invoiceDiscountType = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH.mm.ss");
    private int currentSalesId = -1;
    private int currentUserId = -1;
    private int currentCreditCustomerId = -1;
    private double currentCreditAmount = 0.0;
    private boolean isCreditPayment = false;

    // Keyboard navigation variables
    private int currentProductIndex = -1;
    private java.util.List<ExchangeProduct> productPanels = new java.util.ArrayList<>();

    // Return ID to be passed back to calling point
    private int generatedReturnId = -1;

    public ExchangeProductDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setupButtonStyles();
        setupKeyboardNavigation();
        loadInvoices();
        loadReturnReasons();
        
        // Initialize user from session
        initializeUserFromSession();

        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        showNoInvoiceSelectedMessage();
    }

    // Method to get the generated return ID
    public int getGeneratedReturnId() {
        return generatedReturnId;
    }

    private void initializeUserFromSession() {
        Session session = Session.getInstance();
        currentUserId = session.getUserId();
    }

    private void setupButtonStyles() {
        setupGradientButton(saveBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/exchange.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(saveIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

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

    private void setupButtonMouseListeners() {
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/exchange.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(hoverIcon);
                saveBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/exchange.svg", 25, 25);
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
    }

    private void setupButtonFocusListeners() {
        saveBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/exchange.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(focusedIcon);
                saveBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                saveBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/exchange.svg", 25, 25);
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
    }

    private void setupKeyboardNavigation() {
        // Set initial focus to invoice text field
        SwingUtilities.invokeLater(() -> {
            invoiceNo.requestFocusInWindow();
        });

        // Setup tooltips
        invoiceNo.setToolTipText("Enter invoice number and press ENTER to load details");
        reasonCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select<br>Navigate with arrow keys</html>");
        saveBtn.setToolTipText("Press ENTER to process exchange");
        clearFormBtn.setToolTipText("Press ENTER to clear form");
        cancelBtn.setToolTipText("Press ENTER to cancel");

        // Invoice text field keyboard handling
        invoiceNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        if (!invoiceNo.getText().trim().isEmpty()) {
                            String invoiceNumber = invoiceNo.getText().trim();
                            loadInvoiceDetails(invoiceNumber);
                            if (!productPanels.isEmpty()) {
                                moveFocusToFirstProduct();
                            } else {
                                reasonCombo.requestFocusInWindow();
                            }
                        }
                        evt.consume();
                        break;

                    case KeyEvent.VK_TAB:
                        if (evt.isShiftDown()) {
                            cancelBtn.requestFocusInWindow();
                        } else {
                            if (!productPanels.isEmpty()) {
                                moveFocusToFirstProduct();
                            } else {
                                reasonCombo.requestFocusInWindow();
                            }
                        }
                        evt.consume();
                        break;

                    case KeyEvent.VK_DOWN:
                        if (!productPanels.isEmpty()) {
                            moveFocusToFirstProduct();
                        } else {
                            reasonCombo.requestFocusInWindow();
                        }
                        evt.consume();
                        break;
                }
            }
        });

        // Reason combo keyboard handling
        reasonCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (reasonCombo.isPopupVisible()) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        reasonCombo.setPopupVisible(false);
                        if (reasonCombo.getSelectedIndex() > 0) {
                            saveBtn.requestFocusInWindow();
                        }
                        evt.consume();
                    }
                    return;
                }

                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        if (reasonCombo.getSelectedIndex() > 0) {
                            saveBtn.requestFocusInWindow();
                        } else {
                            reasonCombo.showPopup();
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_DOWN:
                        if (!reasonCombo.isPopupVisible()) {
                            reasonCombo.showPopup();
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_UP:
                        moveFocusToLastProduct();
                        evt.consume();
                        break;
                    case KeyEvent.VK_TAB:
                        if (evt.isShiftDown()) {
                            invoiceNo.requestFocusInWindow();
                        } else {
                            saveBtn.requestFocusInWindow();
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_SPACE:
                        if (!reasonCombo.isPopupVisible()) {
                            reasonCombo.showPopup();
                        }
                        evt.consume();
                        break;
                    default:
                        break;
                }
            }
        });

        // Save button keyboard handling
        saveBtn.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveBtnActionPerformed(null);
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    reasonCombo.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    clearFormBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    clearFormBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    cancelBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_TAB) {
                    if (evt.isShiftDown()) {
                        reasonCombo.requestFocusInWindow();
                    } else {
                        clearFormBtn.requestFocusInWindow();
                    }
                    evt.consume();
                }
            }
        });

        // Clear form button keyboard handling
        clearFormBtn.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearFormBtnActionPerformed(null);
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    reasonCombo.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    saveBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    saveBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    cancelBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_TAB) {
                    if (evt.isShiftDown()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                }
            }
        });

        // Cancel button keyboard handling
        cancelBtn.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    cancelBtnActionPerformed(null);
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    reasonCombo.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    saveBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    clearFormBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    saveBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_TAB) {
                    if (evt.isShiftDown()) {
                        clearFormBtn.requestFocusInWindow();
                    } else {
                        invoiceNo.requestFocusInWindow();
                    }
                    evt.consume();
                }
            }
        });

        // Global key listener for product navigation
        productsPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleProductNavigation(e);
            }
        });
        productsPanel.setFocusable(true);

        // Add ESC key to close dialog
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void handleProductNavigation(KeyEvent e) {
        if (productPanels.isEmpty()) {
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                if (currentProductIndex < productPanels.size() - 1) {
                    currentProductIndex++;
                    focusProductPanel(currentProductIndex);
                    scrollToProduct(currentProductIndex);
                } else {
                    reasonCombo.requestFocusInWindow();
                }
                e.consume();
                break;

            case KeyEvent.VK_UP:
                if (currentProductIndex > 0) {
                    currentProductIndex--;
                    focusProductPanel(currentProductIndex);
                    scrollToProduct(currentProductIndex);
                } else {
                    invoiceNo.requestFocusInWindow();
                }
                e.consume();
                break;

            case KeyEvent.VK_ENTER:
                if (currentProductIndex >= 0 && currentProductIndex < productPanels.size()) {
                    ExchangeProduct currentPanel = productPanels.get(currentProductIndex);
                    JCheckBox checkBox = currentPanel.getCheckBox();

                    // Toggle checkbox selection
                    checkBox.setSelected(!checkBox.isSelected());
                    productSelectedStatus.put(currentProductIndex, checkBox.isSelected());

                    // Update border colors based on selection and focus
                    updateProductPanelBorder(currentPanel, checkBox.isSelected(), true);

                    // If selected, enable quantity field and focus on it
                    if (checkBox.isSelected()) {
                        currentPanel.getExchangeQtyField().setEnabled(true);
                        currentPanel.getExchangeQtyField().requestFocusInWindow();
                        currentPanel.getExchangeQtyField().selectAll();
                    } else {
                        currentPanel.getExchangeQtyField().setEnabled(false);
                        // Don't change the quantity value when unchecking
                    }
                }
                e.consume();
                break;

            case KeyEvent.VK_RIGHT:
                // Move to next product or to reason combo
                if (currentProductIndex < productPanels.size() - 1) {
                    currentProductIndex++;
                    focusProductPanel(currentProductIndex);
                    scrollToProduct(currentProductIndex);
                } else {
                    reasonCombo.requestFocusInWindow();
                }
                e.consume();
                break;

            case KeyEvent.VK_LEFT:
                // Move to previous product or to invoice field
                if (currentProductIndex > 0) {
                    currentProductIndex--;
                    focusProductPanel(currentProductIndex);
                    scrollToProduct(currentProductIndex);
                } else {
                    invoiceNo.requestFocusInWindow();
                }
                e.consume();
                break;

            case KeyEvent.VK_TAB:
                if (e.isShiftDown()) {
                    reasonCombo.requestFocusInWindow();
                } else {
                    invoiceNo.requestFocusInWindow();
                }
                e.consume();
                break;
        }
    }

    private void updateProductPanelBorder(ExchangeProduct panel, boolean isSelected, boolean hasFocus) {
        if (isSelected && hasFocus) {
            // Selected and focused - YELLOW border
            panel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
        } else if (isSelected) {
            // Selected but not focused - RED border
            panel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        } else if (hasFocus) {
            // Not selected but focused - GREEN border
            panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        } else {
            // Not selected and not focused - default LIGHT GRAY border
            panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        }
    }

    private void focusProductPanel(int index) {
        // Reset all panel borders based on their selection status
        for (int i = 0; i < productPanels.size(); i++) {
            ExchangeProduct panel = productPanels.get(i);
            boolean isSelected = productSelectedStatus.getOrDefault(i, false);
            updateProductPanelBorder(panel, isSelected, (i == index));
        }

        // Scroll to the focused product
        if (index >= 0 && index < productPanels.size()) {
            ExchangeProduct currentPanel = productPanels.get(index);
            currentPanel.scrollRectToVisible(currentPanel.getBounds());
        }
    }

    private void scrollToProduct(int index) {
        if (index >= 0 && index < productPanels.size()) {
            ExchangeProduct panel = productPanels.get(index);
            Rectangle bounds = panel.getBounds();
            Rectangle viewRect = productsScrollPanel.getViewport().getViewRect();

            if (!viewRect.contains(bounds)) {
                productsScrollPanel.getViewport().scrollRectToVisible(
                        new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height)
                );
            }
        }
    }

    private void moveFocusToFirstProduct() {
        if (!productPanels.isEmpty()) {
            currentProductIndex = 0;
            focusProductPanel(currentProductIndex);
            productsPanel.requestFocusInWindow();
            scrollToProduct(currentProductIndex);
        } else {
            reasonCombo.requestFocusInWindow();
        }
    }

    private void moveFocusToLastProduct() {
        if (!productPanels.isEmpty()) {
            currentProductIndex = productPanels.size() - 1;
            focusProductPanel(currentProductIndex);
            productsPanel.requestFocusInWindow();
            scrollToProduct(currentProductIndex);
        } else {
            invoiceNo.requestFocusInWindow();
        }
    }

    private void setupQuantityFieldNavigation(ExchangeProduct productPanel, int productIndex) {
        productPanel.getExchangeQtyField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        // When ENTER is pressed in quantity field, move to next product
                        productsPanel.requestFocusInWindow();
                        if (currentProductIndex < productPanels.size() - 1) {
                            currentProductIndex++;
                            focusProductPanel(currentProductIndex);
                            scrollToProduct(currentProductIndex);
                        } else {
                            reasonCombo.requestFocusInWindow();
                        }
                        e.consume();
                        break;

                    case KeyEvent.VK_ESCAPE:
                        // ESC to go back to product selection without changing quantity
                        productsPanel.requestFocusInWindow();
                        focusProductPanel(currentProductIndex);
                        e.consume();
                        break;

                    case KeyEvent.VK_UP:
                        // Move to previous product
                        productsPanel.requestFocusInWindow();
                        if (currentProductIndex > 0) {
                            currentProductIndex--;
                            focusProductPanel(currentProductIndex);
                            scrollToProduct(currentProductIndex);
                        } else {
                            invoiceNo.requestFocusInWindow();
                        }
                        e.consume();
                        break;

                    case KeyEvent.VK_DOWN:
                        // Move to next product
                        productsPanel.requestFocusInWindow();
                        if (currentProductIndex < productPanels.size() - 1) {
                            currentProductIndex++;
                            focusProductPanel(currentProductIndex);
                            scrollToProduct(currentProductIndex);
                        } else {
                            reasonCombo.requestFocusInWindow();
                        }
                        e.consume();
                        break;

                    case KeyEvent.VK_TAB:
                        // TAB to move to next field
                        if (e.isShiftDown()) {
                            // Shift+TAB - move to previous product
                            productsPanel.requestFocusInWindow();
                            if (currentProductIndex > 0) {
                                currentProductIndex--;
                                focusProductPanel(currentProductIndex);
                                scrollToProduct(currentProductIndex);
                            } else {
                                invoiceNo.requestFocusInWindow();
                            }
                        } else {
                            // TAB - move to next product or reason combo
                            productsPanel.requestFocusInWindow();
                            if (currentProductIndex < productPanels.size() - 1) {
                                currentProductIndex++;
                                focusProductPanel(currentProductIndex);
                                scrollToProduct(currentProductIndex);
                            } else {
                                reasonCombo.requestFocusInWindow();
                            }
                        }
                        e.consume();
                        break;
                }
            }
        });

        // Update border when quantity field loses focus
        productPanel.getExchangeQtyField().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                boolean isSelected = productPanel.getCheckBox().isSelected();
                updateProductPanelBorder(productPanel, isSelected, false);
            }

            @Override
            public void focusGained(FocusEvent e) {
                boolean isSelected = productPanel.getCheckBox().isSelected();
                updateProductPanelBorder(productPanel, isSelected, true);
            }
        });
    }

    private void loadInvoices() {
        try {
            String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, "
                    + "pm.payment_method_name, cc.customer_name "
                    + "FROM sales s "
                    + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                    + "LEFT JOIN credit c ON s.sales_id = c.sales_id "
                    + "LEFT JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id "
                    + "WHERE s.status_id = 1 ORDER BY s.sales_id DESC";
            ResultSet rs = MySQL.executeSearch(query);

            invoiceMap.clear();

            while (rs.next()) {
                int salesId = rs.getInt("sales_id");
                String invoiceNo = rs.getString("invoice_no");
                invoiceMap.put(salesId, invoiceNo);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading invoices",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReturnReasons() {
        try {
            String query = "SELECT return_reason_id, reason FROM return_reason";
            ResultSet rs = MySQL.executeSearch(query);

            reasonMap.clear();
            reasonCombo.removeAllItems();
            reasonCombo.addItem("Select Reason");

            while (rs.next()) {
                int reasonId = rs.getInt("return_reason_id");
                String reason = rs.getString("reason");
                reasonMap.put(reasonId, reason);
                reasonCombo.addItem(reason);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading return reasons",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showNoInvoiceSelectedMessage() {
        productsPanel.removeAll();
        productPanels.clear();
        currentProductIndex = -1;

        JPanel messagePanel = new JPanel();
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setPreferredSize(new Dimension(563, 200));

        JLabel messageLabel = new JLabel("Please enter an invoice number and press ENTER to view details", JLabel.CENTER);
        messageLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 16));
        messageLabel.setForeground(new Color(150, 150, 150));

        messagePanel.add(messageLabel, BorderLayout.CENTER);
        productsPanel.add(messagePanel);

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void loadInvoiceDetails(String invoiceNo) {
        try {
            productsPanel.removeAll();
            productPanels.clear();
            currentProductIndex = -1;

            productOriginalQtys.clear();
            productSaleItemIds.clear();
            productStockIds.clear();
            productUnitPrices.clear();
            productDiscountPrices.clear();
            productSelectedStatus.clear();
            invoiceTotalDiscount = 0.0;
            invoiceDiscountType = null;
            currentSalesId = -1;
            currentCreditCustomerId = -1;
            currentCreditAmount = 0.0;
            isCreditPayment = false;

            String query = "SELECT s.sales_id, s.datetime, s.total, s.invoice_no, "
                    + "u.name as cashier_name, pm.payment_method_name, "
                    + "c.credit_customer_id, cc.customer_name, d.discount, dt.discount_type, "
                    + "s.discount_id, s.payment_method_id "
                    + "FROM sales s "
                    + "LEFT JOIN user u ON s.user_id = u.user_id "
                    + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                    + "LEFT JOIN credit c ON s.sales_id = c.sales_id "
                    + "LEFT JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id "
                    + "LEFT JOIN discount d ON s.discount_id = d.discount_id "
                    + "LEFT JOIN discount_type dt ON d.discount_type_id = dt.discount_type_id "
                    + "WHERE s.invoice_no = '" + invoiceNo + "'";

            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                currentSalesId = rs.getInt("sales_id");

                int paymentMethodId = rs.getInt("payment_method_id");
                isCreditPayment = (paymentMethodId == 3);

                if (isCreditPayment) {
                    currentCreditCustomerId = rs.getInt("credit_customer_id");
                    loadCurrentCreditAmount();
                }

                JPanel invoiceHeaderPanel = createInvoiceHeaderPanel(rs);
                productsPanel.add(invoiceHeaderPanel);
                productsPanel.add(Box.createVerticalStrut(10));

                Double invoiceDiscount = rs.getDouble("discount");
                String discountType = rs.getString("discount_type");
                boolean hasInvoiceDiscount = rs.getObject("discount_id") != null;

                if (hasInvoiceDiscount && invoiceDiscount != null && discountType != null) {
                    invoiceTotalDiscount = invoiceDiscount;
                    invoiceDiscountType = discountType;
                }

                loadInvoiceProducts(currentSalesId, hasInvoiceDiscount);

                SwingUtilities.invokeLater(() -> {
                    moveFocusToFirstProduct();
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invoice not found: " + invoiceNo,
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                showNoInvoiceSelectedMessage();
            }

            productsPanel.revalidate();
            productsPanel.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading invoice details",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCurrentCreditAmount() {
        try {
            String query = "SELECT credit_amout FROM credit WHERE credit_customer_id = ? ORDER BY credit_id DESC LIMIT 1";
            PreparedStatement stmt = MySQL.getConnection().prepareStatement(query);
            stmt.setInt(1, currentCreditCustomerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentCreditAmount = rs.getDouble("credit_amout");
            } else {
                currentCreditAmount = 0.0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading credit amount",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double getTotalCreditPayments() {
        try {
            String query = "SELECT SUM(credit_pay_amount) as total_paid FROM credit_pay cp "
                    + "JOIN credit c ON cp.credit_id = c.credit_id "
                    + "WHERE c.credit_customer_id = ?";
            PreparedStatement stmt = MySQL.getConnection().prepareStatement(query);
            stmt.setInt(1, currentCreditCustomerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_paid");
            }
        } catch (SQLException e) {
            // Silent error handling
        }
        return 0.0;
    }

    private JPanel createInvoiceHeaderPanel(ResultSet rs) throws SQLException {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setLayout(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 5));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Left side - Invoice details
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel invoiceNoLabel = new JLabel("#" + rs.getString("invoice_no"));
        invoiceNoLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        invoiceNoLabel.setForeground(new Color(0, 0, 0));
        invoiceNoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String customerName = rs.getString("customer_name");
        JLabel customerLabel = new JLabel(customerName != null ? customerName : "Customer Name");
        customerLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        customerLabel.setForeground(new Color(102, 102, 102));
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Timestamp timestamp = rs.getTimestamp("datetime");
        String formattedDate = dateFormat.format(timestamp);
        JLabel dateLabel = new JLabel(formattedDate);
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(102, 102, 102));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fixed Discount Display Logic
        Double invoiceDiscount = rs.getDouble("discount");
        String discountType = rs.getString("discount_type");
        Object discountId = rs.getObject("discount_id");
        JLabel discountLabel = new JLabel();
        discountLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        discountLabel.setForeground(new Color(255, 0, 0));
        discountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (discountId != null && invoiceDiscount != null && invoiceDiscount > 0) {
            String discountText = "Discount ";
            if ("percentage".equalsIgnoreCase(discountType)) {
                double discountAmount = rs.getDouble("total") * invoiceDiscount / 100;
                discountText += String.format("%.0f%% = Rs %.2f", invoiceDiscount, discountAmount);
            } else {
                discountText += "Rs " + String.format("%.2f", invoiceDiscount);
            }
            discountLabel.setText(discountText);
        } else {
            discountLabel.setText("No Discount");
        }

        leftPanel.add(invoiceNoLabel);
        leftPanel.add(Box.createVerticalStrut(1));
        leftPanel.add(customerLabel);
        leftPanel.add(dateLabel);
        leftPanel.add(discountLabel);

        // Right side - Payment method, total, and cashier
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel paymentTotalPanel = new JPanel();
        paymentTotalPanel.setBackground(Color.WHITE);
        paymentTotalPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        String paymentMethodName = rs.getString("payment_method_name");
        JButton paymentBtn = new JButton(paymentMethodName != null ? paymentMethodName.replace(" Payment", "").toUpperCase() : "PAYMENT");
        paymentBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        paymentBtn.setForeground(Color.WHITE);
        paymentBtn.setFocusPainted(false);
        paymentBtn.setBorderPainted(false);
        paymentBtn.setPreferredSize(new Dimension(85, 32));
        paymentBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updatePaymentMethodColor(paymentBtn, paymentMethodName);

        JLabel totalLabel = new JLabel("Rs." + String.format("%.2f", rs.getDouble("total")));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        totalLabel.setForeground(new Color(8, 147, 176));

        paymentTotalPanel.add(paymentBtn);
        paymentTotalPanel.add(totalLabel);

        JPanel cashierPanel = new JPanel();
        cashierPanel.setBackground(Color.WHITE);
        cashierPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        JLabel cashierText = new JLabel("Cashier");
        cashierText.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        cashierText.setForeground(new Color(102, 102, 102));

        JLabel cashierName = new JLabel(rs.getString("cashier_name"));
        cashierName.setFont(new Font("Nunito ExtraBold", Font.BOLD, 13));
        cashierName.setForeground(new Color(0, 0, 0));

        cashierPanel.add(cashierText);
        cashierPanel.add(cashierName);

        rightPanel.add(paymentTotalPanel);
        rightPanel.add(cashierPanel);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void updatePaymentMethodColor(JButton paymentBtn, String paymentMethodName) {
        Color bgColor;
        Color fgColor = Color.WHITE;

        if (paymentMethodName != null) {
            String method = paymentMethodName.toLowerCase();
            if (method.contains("cash")) {
                bgColor = new Color(40, 167, 69);
            } else if (method.contains("card")) {
                bgColor = new Color(66, 66, 255);
            } else if (method.contains("credit")) {
                bgColor = new Color(255, 193, 7);
                fgColor = Color.BLACK;
            } else {
                bgColor = new Color(108, 117, 125);
            }
        } else {
            bgColor = new Color(108, 117, 125);
        }

        paymentBtn.setBackground(bgColor);
        paymentBtn.setForeground(fgColor);
    }

    private void loadInvoiceProducts(int salesId, boolean hasInvoiceDiscount) {
        try {
            String query = "SELECT si.sale_item_id, si.qty, si.price, si.discount_price, "
                    + "p.product_name, b.brand_name, st.stock_id, "
                    + "si.total as item_total, st.qty as current_stock_qty "
                    + "FROM sale_item si "
                    + "JOIN stock st ON si.stock_id = st.stock_id "
                    + "JOIN product p ON st.product_id = p.product_id "
                    + "JOIN brand b ON p.brand_id = b.brand_id "
                    + "WHERE si.sales_id = " + salesId;

            ResultSet rs = MySQL.executeSearch(query);

            boolean hasProducts = false;
            int productIndex = 0;

            // Create a container panel for better vertical layout
            JPanel productsContainer = new JPanel();
            productsContainer.setBackground(Color.WHITE);
            productsContainer.setLayout(new BoxLayout(productsContainer, BoxLayout.Y_AXIS));

            while (rs.next()) {
                hasProducts = true;
                int saleItemId = rs.getInt("sale_item_id");
                int originalQty = rs.getInt("qty");
                int currentStockQty = rs.getInt("current_stock_qty");
                int stockId = rs.getInt("stock_id");
                double price = rs.getDouble("price");
                double discountPrice = rs.getDouble("discount_price");

                productOriginalQtys.put(productIndex, originalQty);
                productSaleItemIds.put(productIndex, saleItemId);
                productStockIds.put(productIndex, stockId);
                productUnitPrices.put(productIndex, price);
                productDiscountPrices.put(productIndex, discountPrice);
                productSelectedStatus.put(productIndex, false);

                ExchangeProduct productPanel = new ExchangeProduct();
                productPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
                productPanel.setBackground(Color.WHITE);
                // Set default border - light gray
                productPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

                productPanel.getProductName().setText(
                        rs.getString("product_name") + " (" + rs.getString("brand_name") + ")"
                );

                double itemTotal = rs.getDouble("item_total");

                productPanel.getQty().setText("Original Quantity = " + originalQty);
                productPanel.getQty().setToolTipText("Available in stock: " + currentStockQty);

                if (hasInvoiceDiscount) {
                    double originalItemTotal = price * originalQty;
                    productPanel.getQtyCount().setText(
                            "Rs " + String.format("%.2f", price) + " X " + originalQty + " = "
                            + String.format("%.2f", originalItemTotal)
                    );

                    if (discountPrice > 0) {
                        productPanel.getDiscountItem().setText(
                                "Item Disc: Rs " + String.format("%.2f", discountPrice)
                                + " | Inv Disc Applied"
                        );
                    } else {
                        productPanel.getDiscountItem().setText("Invoice Discount Applied");
                    }
                } else {
                    double totalBeforeDiscount = price * originalQty;
                    double totalAfterDiscount = itemTotal;
                    double totalItemDiscount = discountPrice * originalQty;

                    productPanel.getQtyCount().setText(
                            "Rs " + String.format("%.2f", price) + " X " + originalQty + " = "
                            + String.format("%.2f", totalBeforeDiscount)
                    );

                    if (totalItemDiscount > 0) {
                        productPanel.getDiscountItem().setText(
                                "Discount Rs " + String.format("%.2f", discountPrice) + " X " + originalQty
                                + " = Rs " + String.format("%.2f", totalAfterDiscount)
                        );
                    } else {
                        productPanel.getDiscountItem().setText("No Item Discount");
                    }
                }

                productPanel.getProductPrice().setText("Rs." + String.format("%.2f", itemTotal));

                int maxExchangeQty = Math.min(originalQty, currentStockQty);

                // Set up text field for quantity input
                // Set default quantity to existing/original quantity
                productPanel.getExchangeQtyField().setText(String.valueOf(originalQty));
                productPanel.getExchangeQtyField().setEnabled(false);

                final int currentIndex = productIndex;
                productPanel.getCheckBox().addActionListener(e -> {
                    boolean isSelected = productPanel.getCheckBox().isSelected();
                    productSelectedStatus.put(currentIndex, isSelected);
                    productPanel.getExchangeQtyField().setEnabled(isSelected);

                    // Update border color based on selection
                    updateProductPanelBorder(productPanel, isSelected, (currentIndex == currentProductIndex));

                    if (isSelected) {
                        // Auto-focus on quantity field when checkbox is selected
                        productPanel.getExchangeQtyField().requestFocusInWindow();
                        productPanel.getExchangeQtyField().selectAll();
                    }
                    // Don't change the quantity value when unchecking - keep current value
                });

                productPanel.getExchangeQtyField().addActionListener(e -> {
                    validateExchangeQuantity(productPanel, originalQty, currentStockQty);
                });

                productPanel.getExchangeQtyField().addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        validateExchangeQuantity(productPanel, originalQty, currentStockQty);
                    }
                });

                // Add enhanced keyboard navigation for quantity field
                setupQuantityFieldNavigation(productPanel, productIndex);

                productsContainer.add(productPanel);
                productPanels.add(productPanel);

                if (!rs.isLast()) {
                    JSeparator productSeparator = new JSeparator();
                    productSeparator.setForeground(new Color(240, 240, 240));
                    productSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
                    productsContainer.add(productSeparator);
                }

                productIndex++;
            }

            if (!hasProducts) {
                JLabel noProductsLabel = new JLabel("No products found for this invoice", JLabel.CENTER);
                noProductsLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
                noProductsLabel.setForeground(new Color(150, 150, 150));
                noProductsLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
                productsContainer.add(noProductsLabel);
            }

            // Add the container to the scroll panel
            productsPanel.add(productsContainer);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void validateExchangeQuantity(ExchangeProduct productPanel, int originalQty, int currentStockQty) {
        try {
            String qtyText = productPanel.getExchangeQtyField().getText().trim();
            if (qtyText.isEmpty()) {
                // Set to original quantity if empty
                productPanel.getExchangeQtyField().setText(String.valueOf(originalQty));
                return;
            }

            int exchangeQty = Integer.parseInt(qtyText);

            // Don't allow more than original purchased quantity
            if (exchangeQty > originalQty) {
                JOptionPane.showMessageDialog(this,
                        "Exchange quantity cannot exceed original purchased quantity (" + originalQty + ")",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                productPanel.getExchangeQtyField().setText(String.valueOf(originalQty));
            } // Don't allow more than available stock
            else if (exchangeQty > currentStockQty) {
                JOptionPane.showMessageDialog(this,
                        "Exchange quantity cannot exceed available stock (" + currentStockQty + ")",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                productPanel.getExchangeQtyField().setText(String.valueOf(currentStockQty));
            } // Must be at least 1 for selected products
            else if (productPanel.getCheckBox().isSelected() && exchangeQty < 1) {
                JOptionPane.showMessageDialog(this,
                        "Exchange quantity must be at least 1 for selected products",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                productPanel.getExchangeQtyField().setText("1");
            } // If quantity is 0 and product is selected, set to at least 1
            else if (productPanel.getCheckBox().isSelected() && exchangeQty == 0) {
                JOptionPane.showMessageDialog(this,
                        "Exchange quantity must be at least 1 for selected products",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                productPanel.getExchangeQtyField().setText("1");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number for exchange quantity",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            // Set back to original quantity on invalid input
            productPanel.getExchangeQtyField().setText(String.valueOf(originalQty));
        }
    }

    private void clearForm() {
        invoiceNo.setText("");
        reasonCombo.setSelectedIndex(0);
        showNoInvoiceSelectedMessage();
        invoiceTotalDiscount = 0.0;
        invoiceDiscountType = null;
        productOriginalQtys.clear();
        productSaleItemIds.clear();
        productStockIds.clear();
        productUnitPrices.clear();
        productDiscountPrices.clear();
        productSelectedStatus.clear();
        currentSalesId = -1;
        currentCreditCustomerId = -1;
        currentCreditAmount = 0.0;
        isCreditPayment = false;
        currentProductIndex = -1;
        productPanels.clear();
        generatedReturnId = -1;

        // Reset focus to invoice text field
        SwingUtilities.invokeLater(() -> {
            invoiceNo.requestFocusInWindow();
        });
    }

    private double calculateRefundAmount() {
        double refundAmount = 0.0;
        int productIndex = 0;

        for (Component comp : productsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel containerPanel = (JPanel) comp;
                for (Component innerComp : containerPanel.getComponents()) {
                    if (innerComp instanceof ExchangeProduct) {
                        ExchangeProduct productPanel = (ExchangeProduct) innerComp;

                        // Check if checkbox is selected
                        boolean isSelected = productPanel.getCheckBox().isSelected();

                        if (isSelected) {
                            try {
                                String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                                if (exchangeQty > 0) {
                                    String priceText = productPanel.getProductPrice().getText();
                                    double itemPrice = 0.0;
                                    try {
                                        itemPrice = Double.parseDouble(priceText.replace("Rs.", "").trim());
                                    } catch (NumberFormatException e) {
                                        // Silent error handling
                                    }

                                    int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                                    double proportionalAmount = (itemPrice / originalQty) * exchangeQty;
                                    refundAmount += proportionalAmount;
                                }
                            } catch (NumberFormatException e) {
                                // Silent error handling
                            }
                        }
                        productIndex++;
                    }
                }
            } else if (comp instanceof ExchangeProduct) {
                ExchangeProduct productPanel = (ExchangeProduct) comp;

                // Check if checkbox is selected
                boolean isSelected = productPanel.getCheckBox().isSelected();

                if (isSelected) {
                    try {
                        String qtyText = productPanel.getExchangeQtyField().getText().trim();
                        int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                        if (exchangeQty > 0) {
                            String priceText = productPanel.getProductPrice().getText();
                            double itemPrice = 0.0;
                            try {
                                itemPrice = Double.parseDouble(priceText.replace("Rs.", "").trim());
                            } catch (NumberFormatException e) {
                                // Silent error handling
                            }

                            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                            double proportionalAmount = (itemPrice / originalQty) * exchangeQty;
                            refundAmount += proportionalAmount;
                        }
                    } catch (NumberFormatException e) {
                        // Silent error handling
                    }
                }
                productIndex++;
            }
        }

        return refundAmount;
    }

    private boolean isStockLossReason(String reason) {
        return reason.toLowerCase().contains("expired")
                || reason.toLowerCase().contains("damaged")
                || reason.toLowerCase().contains("theft")
                || reason.toLowerCase().contains("misplaced");
    }

    private boolean processExchange() {
        Connection conn = null;
        try {
            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

            // Get selected reason
            int reasonId = -1;
            String selectedReason = "";
            for (Map.Entry<Integer, String> entry : reasonMap.entrySet()) {
                if (entry.getValue().equals(reasonCombo.getSelectedItem())) {
                    reasonId = entry.getKey();
                    selectedReason = entry.getValue();
                    break;
                }
            }

            if (reasonId == -1) {
                JOptionPane.showMessageDialog(this, "Invalid return reason selected",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Check if any products are selected with quantity > 0
            boolean hasSelectedProducts = false;
            int productIndex = 0;

            for (Component comp : productsPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel containerPanel = (JPanel) comp;
                    for (Component innerComp : containerPanel.getComponents()) {
                        if (innerComp instanceof ExchangeProduct) {
                            ExchangeProduct productPanel = (ExchangeProduct) innerComp;
                            if (productPanel.getCheckBox().isSelected()) {
                                try {
                                    String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                    int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
                                    if (exchangeQty > 0) {
                                        hasSelectedProducts = true;
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    // Ignore parse errors
                                }
                            }
                            productIndex++;
                        }
                    }
                } else if (comp instanceof ExchangeProduct) {
                    ExchangeProduct productPanel = (ExchangeProduct) comp;
                    if (productPanel.getCheckBox().isSelected()) {
                        try {
                            String qtyText = productPanel.getExchangeQtyField().getText().trim();
                            int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
                            if (exchangeQty > 0) {
                                hasSelectedProducts = true;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Ignore parse errors
                        }
                    }
                    productIndex++;
                }

                if (hasSelectedProducts) {
                    break;
                }
            }

            if (!hasSelectedProducts) {
                JOptionPane.showMessageDialog(this, "Please select at least one product to return with quantity greater than 0",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Calculate total refund amount and discount - USE ORIGINAL QUANTITIES FROM DATABASE
            double totalRefundAmount = calculateRefundAmount();
            double totalDiscountPrice = calculateTotalDiscountPrice();

            // Insert into return table - USING SESSION USER ID
            String returnQuery = "INSERT INTO `return` (return_date, total_return_amount, return_reason_id, "
                    + "status_id, sales_id, user_id, total_discount_price) "
                    + "VALUES (NOW(), ?, ?, 1, ?, ?, ?)";

            PreparedStatement returnStmt = conn.prepareStatement(returnQuery, Statement.RETURN_GENERATED_KEYS);
            returnStmt.setDouble(1, totalRefundAmount);
            returnStmt.setInt(2, reasonId);
            returnStmt.setInt(3, currentSalesId);
            returnStmt.setInt(4, currentUserId); // USING SESSION USER ID HERE
            returnStmt.setDouble(5, totalDiscountPrice);

            int affectedRows = returnStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating return failed, no rows affected.");
            }

            int returnId;
            try (ResultSet generatedKeys = returnStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    returnId = generatedKeys.getInt(1);
                    generatedReturnId = returnId; // Store the return ID
                } else {
                    throw new SQLException("Creating return failed, no ID obtained.");
                }
            }

            // Process return items - USE ORIGINAL QUANTITIES FROM DATABASE
            boolean isStockLoss = isStockLossReason(selectedReason);
            productIndex = 0;

            for (Component comp : productsPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel containerPanel = (JPanel) comp;
                    for (Component innerComp : containerPanel.getComponents()) {
                        if (innerComp instanceof ExchangeProduct) {
                            ExchangeProduct productPanel = (ExchangeProduct) innerComp;

                            if (productPanel.getCheckBox().isSelected()) {
                                try {
                                    String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                    int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                                    if (exchangeQty > 0) {
                                        int saleItemId = productSaleItemIds.get(productIndex);
                                        int stockId = productStockIds.get(productIndex);
                                        int originalQty = productOriginalQtys.get(productIndex); // USE ORIGINAL QTY FROM DATABASE
                                        double unitPrice = productUnitPrices.get(productIndex);
                                        double discountPrice = productDiscountPrices.get(productIndex);

                                        // Calculate individual item amounts USING ORIGINAL QUANTITY
                                        double itemTotalBeforeDiscount = unitPrice * originalQty;
                                        double itemDiscountAmount = (discountPrice / originalQty) * exchangeQty;
                                        double itemTotalReturnAmount = (itemTotalBeforeDiscount / originalQty) * exchangeQty;

                                        // Insert return item
                                        String returnItemQuery = "INSERT INTO return_item (return_qty, unit_return_price, "
                                                + "discount_price, total_return_amount, stock_id, sale_item_id, return_id) "
                                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                                        PreparedStatement returnItemStmt = conn.prepareStatement(returnItemQuery);
                                        returnItemStmt.setString(1, String.valueOf(exchangeQty));
                                        returnItemStmt.setDouble(2, unitPrice);
                                        returnItemStmt.setDouble(3, itemDiscountAmount);
                                        returnItemStmt.setDouble(4, itemTotalReturnAmount);
                                        returnItemStmt.setInt(5, stockId);
                                        returnItemStmt.setInt(6, saleItemId);
                                        returnItemStmt.setInt(7, returnId);

                                        returnItemStmt.executeUpdate();

                                        // Handle stock based on return reason
                                        if (isStockLoss) {
                                            // Add to stock_loss table - USING SESSION USER ID
                                            String stockLossQuery = "INSERT INTO stock_loss (qty, stock_id, stock_loss_date, "
                                                    + "return_reason_id, user_id, sales_id) "
                                                    + "VALUES (?, ?, NOW(), ?, ?, ?)";

                                            PreparedStatement stockLossStmt = conn.prepareStatement(stockLossQuery);
                                            stockLossStmt.setInt(1, exchangeQty);
                                            stockLossStmt.setInt(2, stockId);
                                            stockLossStmt.setInt(3, reasonId);
                                            stockLossStmt.setInt(4, currentUserId); // USING SESSION USER ID HERE
                                            stockLossStmt.setInt(5, currentSalesId);

                                            stockLossStmt.executeUpdate();
                                        } else {
                                            // Update stock quantity (add back to inventory) - USE EXCHANGE QTY
                                            String updateStockQuery = "UPDATE stock SET qty = qty + ? WHERE stock_id = ?";
                                            PreparedStatement updateStockStmt = conn.prepareStatement(updateStockQuery);
                                            updateStockStmt.setInt(1, exchangeQty);
                                            updateStockStmt.setInt(2, stockId);
                                            updateStockStmt.executeUpdate();
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    // Silent error handling
                                }
                            }
                            productIndex++;
                        }
                    }
                } else if (comp instanceof ExchangeProduct) {
                    ExchangeProduct productPanel = (ExchangeProduct) comp;

                    if (productPanel.getCheckBox().isSelected()) {
                        try {
                            String qtyText = productPanel.getExchangeQtyField().getText().trim();
                            int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                            if (exchangeQty > 0) {
                                int saleItemId = productSaleItemIds.get(productIndex);
                                int stockId = productStockIds.get(productIndex);
                                int originalQty = productOriginalQtys.get(productIndex); // USE ORIGINAL QTY FROM DATABASE
                                double unitPrice = productUnitPrices.get(productIndex);
                                double discountPrice = productDiscountPrices.get(productIndex);

                                // Calculate individual item amounts USING ORIGINAL QUANTITY
                                double itemTotalBeforeDiscount = unitPrice * originalQty;
                                double itemDiscountAmount = (discountPrice / originalQty) * exchangeQty;
                                double itemTotalReturnAmount = (itemTotalBeforeDiscount / originalQty) * exchangeQty;

                                // Insert return item
                                String returnItemQuery = "INSERT INTO return_item (return_qty, unit_return_price, "
                                        + "discount_price, total_return_amount, stock_id, sale_item_id, return_id) "
                                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                                PreparedStatement returnItemStmt = conn.prepareStatement(returnItemQuery);
                                returnItemStmt.setString(1, String.valueOf(exchangeQty));
                                returnItemStmt.setDouble(2, unitPrice);
                                returnItemStmt.setDouble(3, itemDiscountAmount);
                                returnItemStmt.setDouble(4, itemTotalReturnAmount);
                                returnItemStmt.setInt(5, stockId);
                                returnItemStmt.setInt(6, saleItemId);
                                returnItemStmt.setInt(7, returnId);

                                returnItemStmt.executeUpdate();

                                // Handle stock based on return reason
                                if (isStockLoss) {
                                    // Add to stock_loss table - USING SESSION USER ID
                                    String stockLossQuery = "INSERT INTO stock_loss (qty, stock_id, stock_loss_date, "
                                            + "return_reason_id, user_id, sales_id) "
                                            + "VALUES (?, ?, NOW(), ?, ?, ?)";

                                    PreparedStatement stockLossStmt = conn.prepareStatement(stockLossQuery);
                                    stockLossStmt.setInt(1, exchangeQty);
                                    stockLossStmt.setInt(2, stockId);
                                    stockLossStmt.setInt(3, reasonId);
                                    stockLossStmt.setInt(4, currentUserId); // USING SESSION USER ID HERE
                                    stockLossStmt.setInt(5, currentSalesId);

                                    stockLossStmt.executeUpdate();
                                } else {
                                    // Update stock quantity (add back to inventory) - USE EXCHANGE QTY
                                    String updateStockQuery = "UPDATE stock SET qty = qty + ? WHERE stock_id = ?";
                                    PreparedStatement updateStockStmt = conn.prepareStatement(updateStockQuery);
                                    updateStockStmt.setInt(1, exchangeQty);
                                    updateStockStmt.setInt(2, stockId);
                                    updateStockStmt.executeUpdate();
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Silent error handling
                        }
                    }
                    productIndex++;
                }
            }

            // Handle credit payment refund
            if (isCreditPayment && currentCreditCustomerId != -1) {
                handleCreditRefund(conn, totalRefundAmount);
            }

            // Commit transaction
            conn.commit();

            String message = buildSuccessMessage(totalRefundAmount, selectedReason, isStockLoss);
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Silent rollback exception
                }
            }
            JOptionPane.showMessageDialog(this, "Error processing exchange",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (NumberFormatException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Silent rollback exception
                }
            }
            JOptionPane.showMessageDialog(this, "Error parsing price data",
                    "Data Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    // Silent exception
                }
            }
        }
    }

    private void handleCreditRefund(Connection conn, double refundAmount) throws SQLException {
        // Get total payments made by customer
        double totalPaid = getTotalCreditPayments();

        if (totalPaid >= refundAmount) {
            // Customer has paid more than refund amount - give cash refund
            // Update credit table to decrease the amount
            String updateCreditQuery = "UPDATE credit SET credit_amout = credit_amout - ? "
                    + "WHERE credit_customer_id = ? ORDER BY credit_id DESC LIMIT 1";
            PreparedStatement updateStmt = conn.prepareStatement(updateCreditQuery);
            updateStmt.setDouble(1, refundAmount);
            updateStmt.setInt(2, currentCreditCustomerId);
            updateStmt.executeUpdate();
        } else {
            // Customer hasn't paid enough - just decrease credit amount
            String updateCreditQuery = "UPDATE credit SET credit_amout = credit_amout - ? "
                    + "WHERE credit_customer_id = ? ORDER BY credit_id DESC LIMIT 1";
            PreparedStatement updateStmt = conn.prepareStatement(updateCreditQuery);
            updateStmt.setDouble(1, refundAmount);
            updateStmt.setInt(2, currentCreditCustomerId);
            updateStmt.executeUpdate();
        }
    }

    private String buildSuccessMessage(double refundAmount, String selectedReason, boolean isStockLoss) {
        StringBuilder message = new StringBuilder();
        message.append("Exchange processed successfully!\n");
        message.append("Return ID: ").append(generatedReturnId).append("\n");
        message.append("Refund Amount: Rs.").append(String.format("%.2f", refundAmount)).append("\n");

        if (isCreditPayment) {
            double totalPaid = getTotalCreditPayments();
            if (totalPaid >= refundAmount) {
                message.append("Payment Method: CREDIT (Customer has paid)\n");
                message.append("Return Amount to Customer: Rs.").append(String.format("%.2f", refundAmount)).append("\n");
                message.append("Credit balance decreased by: Rs.").append(String.format("%.2f", refundAmount));
            } else {
                message.append("Payment Method: CREDIT (Customer hasn't paid fully)\n");
                message.append("Credit balance decreased by: Rs.").append(String.format("%.2f", refundAmount)).append("\n");
                message.append("No cash refund - amount deducted from credit balance");
            }
        } else {
            message.append("Payment Method: CASH/CARD\n");
            message.append("Return Amount to Customer: Rs.").append(String.format("%.2f", refundAmount));
        }

        message.append("\n\nStock Handling: ");
        message.append(isStockLoss ? "Items marked as stock loss (not returned to inventory)"
                : "Items returned to inventory");

        return message.toString();
    }

    private double calculateTotalDiscountPrice() {
        double totalDiscount = 0.0;
        int productIndex = 0;

        for (Component comp : productsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel containerPanel = (JPanel) comp;
                for (Component innerComp : containerPanel.getComponents()) {
                    if (innerComp instanceof ExchangeProduct) {
                        ExchangeProduct productPanel = (ExchangeProduct) innerComp;

                        if (productPanel.getCheckBox().isSelected()) {
                            try {
                                String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                                if (exchangeQty > 0) {
                                    int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                                    double discountPrice = productDiscountPrices.getOrDefault(productIndex, 0.0);

                                    double proportionalDiscount = (discountPrice / originalQty) * exchangeQty;
                                    totalDiscount += proportionalDiscount;
                                }
                            } catch (NumberFormatException e) {
                                // Silent error handling
                            }
                        }
                        productIndex++;
                    }
                }
            } else if (comp instanceof ExchangeProduct) {
                ExchangeProduct productPanel = (ExchangeProduct) comp;

                if (productPanel.getCheckBox().isSelected()) {
                    try {
                        String qtyText = productPanel.getExchangeQtyField().getText().trim();
                        int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                        if (exchangeQty > 0) {
                            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                            double discountPrice = productDiscountPrices.getOrDefault(productIndex, 0.0);

                            double proportionalDiscount = (discountPrice / originalQty) * exchangeQty;
                            totalDiscount += proportionalDiscount;
                        }
                    } catch (NumberFormatException e) {
                        // Silent error handling
                    }
                }
                productIndex++;
            }
        }

        return totalDiscount;
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        saveBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        productsScrollPanel = new javax.swing.JScrollPane();
        productsPanel = new javax.swing.JPanel();
        invoiceNumberLabel = new javax.swing.JLabel();
        totalAmountLabel = new javax.swing.JLabel();
        customerNameLabel = new javax.swing.JLabel();
        paymentMethod = new javax.swing.JButton();
        dateLabel = new javax.swing.JLabel();
        brandName2 = new javax.swing.JLabel();
        cashierLabel = new javax.swing.JLabel();
        discountLabel = new javax.swing.JLabel();
        reasonCombo = new javax.swing.JComboBox<>();
        invoiceNo = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Exchange Product");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Exchange Products");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        saveBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        saveBtn.setForeground(new java.awt.Color(8, 147, 176));
        saveBtn.setText("Exchange");
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

        productsScrollPanel.setBackground(new java.awt.Color(255, 255, 255));
        productsScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        productsScrollPanel.setOpaque(false);
        productsScrollPanel.setPreferredSize(new java.awt.Dimension(563, 370));

        productsPanel.setBackground(new java.awt.Color(255, 255, 255));

        invoiceNumberLabel.setFont(new java.awt.Font("Nunito SemiBold", 3, 14)); // NOI18N
        invoiceNumberLabel.setText("#inv3243345");

        totalAmountLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        totalAmountLabel.setForeground(new java.awt.Color(8, 147, 176));
        totalAmountLabel.setText("Rs.570.00");

        customerNameLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        customerNameLabel.setForeground(new java.awt.Color(102, 102, 102));
        customerNameLabel.setText("Customer Name");

        paymentMethod.setBackground(new java.awt.Color(51, 0, 255));
        paymentMethod.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        paymentMethod.setForeground(new java.awt.Color(255, 255, 255));
        paymentMethod.setText("CASE");
        paymentMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentMethodActionPerformed(evt);
            }
        });

        dateLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        dateLabel.setForeground(new java.awt.Color(102, 102, 102));
        dateLabel.setText("10/10/2025, 15.25.20");

        brandName2.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        brandName2.setForeground(new java.awt.Color(102, 102, 102));
        brandName2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        brandName2.setText("Cashier");

        cashierLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        cashierLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cashierLabel.setText("Hirun");

        discountLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        discountLabel.setForeground(new java.awt.Color(255, 0, 0));
        discountLabel.setText("Discount 10% =  RS 70.00");

        javax.swing.GroupLayout productsPanelLayout = new javax.swing.GroupLayout(productsPanel);
        productsPanel.setLayout(productsPanelLayout);
        productsPanelLayout.setHorizontalGroup(
            productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, productsPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(discountLabel)
                    .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(productsPanelLayout.createSequentialGroup()
                            .addComponent(invoiceNumberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(57, 57, 57))
                        .addGroup(productsPanelLayout.createSequentialGroup()
                            .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(productsPanelLayout.createSequentialGroup()
                                    .addComponent(customerNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(8, 8, 8))
                                .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(24, 24, 24))))
                .addGap(211, 211, 211)
                .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productsPanelLayout.createSequentialGroup()
                        .addComponent(paymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalAmountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(productsPanelLayout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(brandName2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cashierLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(560, 560, 560))
        );
        productsPanelLayout.setVerticalGroup(
            productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productsPanelLayout.createSequentialGroup()
                        .addComponent(invoiceNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(customerNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(productsPanelLayout.createSequentialGroup()
                        .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(paymentMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalAmountLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cashierLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(brandName2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(714, 714, 714))
        );

        productsScrollPanel.setViewportView(productsPanel);

        reasonCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        reasonCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        reasonCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Exchange Reason *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        reasonCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reasonComboActionPerformed(evt);
            }
        });
        reasonCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                reasonComboKeyPressed(evt);
            }
        });

        invoiceNo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        invoiceNo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Invoice No", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(reasonCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(invoiceNo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 563, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(productsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addGap(7, 7, 7)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invoiceNo, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reasonCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed

   
        if (invoiceNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an invoice number",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (reasonCombo.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a return reason",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if any products are selected with quantity > 0
        boolean hasValidExchange = false;
        int productIndex = 0;

        for (Component comp : productsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel containerPanel = (JPanel) comp;
                for (Component innerComp : containerPanel.getComponents()) {
                    if (innerComp instanceof ExchangeProduct) {
                        ExchangeProduct productPanel = (ExchangeProduct) innerComp;
                        if (productPanel.getCheckBox().isSelected()) {
                            try {
                                String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
                                if (exchangeQty > 0) {
                                    hasValidExchange = true;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // Ignore parse errors here, they'll be caught in validation
                            }
                        }
                        productIndex++;
                    }
                }
            } else if (comp instanceof ExchangeProduct) {
                ExchangeProduct productPanel = (ExchangeProduct) comp;
                if (productPanel.getCheckBox().isSelected()) {
                    try {
                        String qtyText = productPanel.getExchangeQtyField().getText().trim();
                        int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
                        if (exchangeQty > 0) {
                            hasValidExchange = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore parse errors here, they'll be caught in validation
                    }
                }
                productIndex++;
            }

            if (hasValidExchange) {
                break;
            }
        }

        if (!hasValidExchange) {
            JOptionPane.showMessageDialog(this, "Please select products to exchange and enter quantities greater than 0",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double refundAmount = calculateRefundAmount();

        // Show confirmation with refund amount and stock handling info
        String selectedReason = (String) reasonCombo.getSelectedItem();
        boolean isStockLoss = isStockLossReason(selectedReason);

        String confirmationMessage = "Total Refund Amount: Rs." + String.format("%.2f", refundAmount)
                + "\nReturn Reason: " + selectedReason
                + "\n\nStock Handling: "
                + (isStockLoss ? "Items will be marked as STOCK LOSS (not returned to inventory)"
                        : "Items will be returned to inventory")
                + "\n\nDo you want to proceed with the exchange?";

        int confirm = JOptionPane.showConfirmDialog(this, confirmationMessage,
                "Confirm Exchange", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = processExchange();
            if (success) {
                dispose();
            }
        }
    }//GEN-LAST:event_saveBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed

    }//GEN-LAST:event_saveBtnKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed

    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed

    }//GEN-LAST:event_cancelBtnKeyPressed

    private void reasonComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reasonComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reasonComboActionPerformed

    private void reasonComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reasonComboKeyPressed

    }//GEN-LAST:event_reasonComboKeyPressed

    private void paymentMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentMethodActionPerformed

    }//GEN-LAST:event_paymentMethodActionPerformed

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
            java.util.logging.Logger.getLogger(ExchangeProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ExchangeProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ExchangeProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ExchangeProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ExchangeProductDialog dialog = new ExchangeProductDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JLabel brandName2;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel cashierLabel;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JLabel customerNameLabel;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JLabel discountLabel;
    private javax.swing.JTextField invoiceNo;
    private javax.swing.JLabel invoiceNumberLabel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton paymentMethod;
    private javax.swing.JPanel productsPanel;
    private javax.swing.JScrollPane productsScrollPanel;
    private javax.swing.JComboBox<String> reasonCombo;
    private javax.swing.JButton saveBtn;
    private javax.swing.JLabel totalAmountLabel;
    // End of variables declaration//GEN-END:variables
}
