package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import lk.com.pos.dialogpanel.ExchangeProduct;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.List;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import raven.toast.Notifications;

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
    private int currentUserId = 1;
    private int currentCreditCustomerId = -1;
    private double currentCreditAmount = 0.0;
    private boolean isCreditPayment = false;

    private List<ExchangeProduct> productPanels = new ArrayList<>();
    private int currentProductIndex = -1;
    private boolean isFirstEnterOnProduct = false;

    // Variable to store return ID for calling center
    private int generatedReturnId = -1;

    public ExchangeProductDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        loadInvoices();
        loadReturnReasons();
        setupKeyboardNavigation();
        setupButtonStyles();

        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        showNoInvoiceSelectedMessage();
    }

    // Method to get return ID for calling center
    public int getReturnId() {
        return generatedReturnId;
    }

    // Method to check if exchange was successful
    public boolean isExchangeSuccessful() {
        return generatedReturnId != -1;
    }

    private void setupKeyboardNavigation() {
        // Register ESC key to close dialog
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Register F1 key for quick access to exchange button
        getRootPane().registerKeyboardAction(
                e -> {
                    if (saveBtn.isEnabled()) {
                        saveBtnActionPerformed(null);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Setup focus traversal
        setupFocusTraversal();
        addEnterKeyNavigation();
        setupComboBoxNavigation();
    }

    private void setupButtonStyles() {
        setupGradientButton(saveBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Set icons for buttons
        FlatSVGIcon exchangeIcon = new FlatSVGIcon("lk/com/pos/icon/exchange.svg", 25, 25);
        exchangeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(exchangeIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
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
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(hoverIcon);
                clearFormBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
            }
        });

        cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(hoverIcon);
                cancelBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
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
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(focusedIcon);
                clearFormBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
            }
        });

        cancelBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(focusedIcon);
                cancelBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });
    }

    private void setupFocusTraversal() {
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)));

        setupArrowKeyNavigation();
    }

    private void addEnterKeyNavigation() {
        java.util.Map<JComponent, JComponent> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(invoiceCombo, reasonCombo);
        enterNavigationMap.put(reasonCombo, saveBtn);
        enterNavigationMap.put(saveBtn, clearFormBtn);
        enterNavigationMap.put(clearFormBtn, cancelBtn);
        enterNavigationMap.put(cancelBtn, invoiceCombo);

        for (java.util.Map.Entry<JComponent, JComponent> entry : enterNavigationMap.entrySet()) {
            entry.getKey().addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER && !(evt.getComponent() instanceof JComboBox)) {
                        JComponent nextComponent = enterNavigationMap.get(evt.getComponent());
                        if (nextComponent != null) {
                            nextComponent.requestFocusInWindow();
                        }
                        evt.consume();
                    }
                }
            });
        }
    }

    private void setupComboBoxNavigation() {
        // Invoice combo navigation
        invoiceCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (invoiceCombo.isPopupVisible()) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        invoiceCombo.setPopupVisible(false);
                        if (invoiceCombo.getSelectedIndex() > 0) {
                            String selectedInvoice = (String) invoiceCombo.getSelectedItem();
                            loadInvoiceDetails(selectedInvoice);
                        }
                        SwingUtilities.invokeLater(() -> {
                            if (!productPanels.isEmpty()) {
                                focusFirstProduct();
                            } else {
                                reasonCombo.requestFocusInWindow();
                            }
                        });
                        evt.consume();
                    }
                    return;
                }

                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (!invoiceCombo.isPopupVisible()) {
                            invoiceCombo.showPopup();
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_UP:
                        cancelBtn.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (invoiceCombo.getSelectedIndex() > 0) {
                            String selectedInvoice = (String) invoiceCombo.getSelectedItem();
                            loadInvoiceDetails(selectedInvoice);
                            SwingUtilities.invokeLater(() -> {
                                if (!productPanels.isEmpty()) {
                                    focusFirstProduct();
                                } else {
                                    reasonCombo.requestFocusInWindow();
                                }
                            });
                        } else {
                            invoiceCombo.showPopup();
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (!productPanels.isEmpty()) {
                            focusFirstProduct();
                        } else {
                            reasonCombo.requestFocusInWindow();
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_LEFT:
                        cancelBtn.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_SPACE:
                        if (!invoiceCombo.isPopupVisible()) {
                            invoiceCombo.showPopup();
                        }
                        evt.consume();
                        break;
                }
            }
        });

        invoiceCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!invoiceCombo.isPopupVisible() && invoiceCombo.getSelectedIndex() > 0) {
                    String selectedInvoice = (String) invoiceCombo.getSelectedItem();
                    loadInvoiceDetails(selectedInvoice);

                    SwingUtilities.invokeLater(() -> {
                        if (!productPanels.isEmpty()) {
                            focusFirstProduct();
                        } else {
                            reasonCombo.requestFocusInWindow();
                        }
                    });
                }
            }
        });

        // Reason combo navigation
        reasonCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (reasonCombo.isPopupVisible()) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        reasonCombo.setPopupVisible(false);
                        SwingUtilities.invokeLater(() -> {
                            saveBtn.requestFocusInWindow();
                        });
                        evt.consume();
                    }
                    return;
                }

                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (!reasonCombo.isPopupVisible()) {
                            reasonCombo.showPopup();
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (reasonCombo.getSelectedIndex() <= 0) {
                            reasonCombo.showPopup();
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                saveBtn.requestFocusInWindow();
                            });
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_UP:
                        if (!productPanels.isEmpty()) {
                            focusLastProduct();
                        } else {
                            invoiceCombo.requestFocusInWindow();
                        }
                        evt.consume();
                        break;
                    case KeyEvent.VK_RIGHT:
                        saveBtn.requestFocusInWindow();
                        evt.consume();
                        break;
                    case KeyEvent.VK_LEFT:
                        if (!productPanels.isEmpty()) {
                            focusLastProduct();
                        } else {
                            invoiceCombo.requestFocusInWindow();
                        }
                        evt.consume();
                        break;
                }
            }
        });

        invoiceCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (!invoiceCombo.isPopupVisible()) {
                    invoiceCombo.showPopup();
                }
            }
        });

        reasonCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (!reasonCombo.isPopupVisible()) {
                    reasonCombo.showPopup();
                }
            }
        });

        invoiceCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (invoiceCombo.isPopupVisible()) {
                    invoiceCombo.setPopupVisible(false);
                }
            }
        });

        reasonCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (reasonCombo.isPopupVisible()) {
                    reasonCombo.setPopupVisible(false);
                }
            }
        });

        reasonCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!reasonCombo.isPopupVisible() && reasonCombo.getSelectedIndex() > 0) {
                    SwingUtilities.invokeLater(() -> {
                        saveBtn.requestFocusInWindow();
                    });
                }
            }
        });
    }

    private void setupArrowKeyNavigation() {
        invoiceCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (!invoiceCombo.isPopupVisible()) {
                    handleArrowNavigation(evt, invoiceCombo);
                }
            }
        });

        reasonCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (!reasonCombo.isPopupVisible()) {
                    handleArrowNavigation(evt, reasonCombo);
                }
            }
        });

        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveBtnActionPerformed(null);
                } else {
                    handleArrowNavigation(evt, saveBtn);
                }
            }
        });

        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearFormBtnActionPerformed(null);
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
        if (source == invoiceCombo) {
            if (!productPanels.isEmpty()) {
                focusFirstProduct();
            } else {
                reasonCombo.requestFocusInWindow();
            }
        } else if (source == reasonCombo) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            invoiceCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == invoiceCombo) {
            cancelBtn.requestFocusInWindow();
        } else if (source == reasonCombo) {
            if (!productPanels.isEmpty()) {
                focusLastProduct();
            } else {
                invoiceCombo.requestFocusInWindow();
            }
        } else if (source == saveBtn) {
            reasonCombo.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == invoiceCombo) {
            if (!invoiceCombo.isPopupVisible()) {
                invoiceCombo.showPopup();
            }
        } else if (source == reasonCombo) {
            if (!reasonCombo.isPopupVisible()) {
                reasonCombo.showPopup();
            }
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            invoiceCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == invoiceCombo) {
            cancelBtn.requestFocusInWindow();
        } else if (source == reasonCombo) {
            if (!productPanels.isEmpty()) {
                focusLastProduct();
            } else {
                invoiceCombo.requestFocusInWindow();
            }
        } else if (source == saveBtn) {
            reasonCombo.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void focusFirstProduct() {
        if (!productPanels.isEmpty()) {
            currentProductIndex = 0;
            ExchangeProduct firstProduct = productPanels.get(0);
            firstProduct.getCheckBox().requestFocusInWindow();
            setupProductFocusBorder(firstProduct, true);
            scrollToProduct(firstProduct);
            isFirstEnterOnProduct = true;
        }
    }

    private void focusLastProduct() {
        if (!productPanels.isEmpty()) {
            currentProductIndex = productPanels.size() - 1;
            ExchangeProduct lastProduct = productPanels.get(currentProductIndex);
            lastProduct.getCheckBox().requestFocusInWindow();
            setupProductFocusBorder(lastProduct, true);
            scrollToProduct(lastProduct);
            isFirstEnterOnProduct = true;
        }
    }

    private void scrollToProduct(ExchangeProduct productPanel) {
        SwingUtilities.invokeLater(() -> {
            try {
                Rectangle rect = productPanel.getBounds();
                productsScrollPanel.getViewport().scrollRectToVisible(rect);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupProductFocusBorder(ExchangeProduct productPanel, boolean focused) {
        if (focused) {
            productPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 150, 0), 3),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            productPanel.setBackground(new Color(240, 255, 240));
        } else {
            // If checkbox is selected, keep dark green border always
            if (productPanel.getCheckBox().isSelected()) {
                productPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 100, 0), 3), // Darker green for selected
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                productPanel.setBackground(new Color(230, 245, 230)); // Light green background for selected
            } else {
                productPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                productPanel.setBackground(Color.WHITE);
            }
        }
        productPanel.repaint();
    }

    private void setupProductKeyboardNavigation(ExchangeProduct productPanel, int productIndex) {
        productPanel.getCheckBox().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                setupProductFocusBorder(productPanel, true);
                currentProductIndex = productIndex;
                isFirstEnterOnProduct = true;
                scrollToProduct(productPanel);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                setupProductFocusBorder(productPanel, false);
            }
        });

        productPanel.getNewQty().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                setupProductFocusBorder(productPanel, true);
                currentProductIndex = productIndex;
                scrollToProduct(productPanel);
                productPanel.getNewQty().selectAll();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                setupProductFocusBorder(productPanel, false);
                validateQuantityField(productPanel, productIndex);
            }
        });

        // Add document listener to prevent text input and validate in real-time
        ((javax.swing.text.PlainDocument) productPanel.getNewQty().getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(javax.swing.text.DocumentFilter.FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                // Only allow digits
                if (string != null && string.matches("\\d+")) {
                    String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
                    if (isValidQuantity(newText, productPanel, productIndex)) {
                        super.insertString(fb, offset, string, attr);
                    }
                }
            }

            @Override
            public void replace(javax.swing.text.DocumentFilter.FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                if (text != null && text.matches("\\d*")) {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                    if (isValidQuantity(newText, productPanel, productIndex)) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            }
        });

        // Add checkbox change listener to update border when selected
        productPanel.getCheckBox().addActionListener(e -> {
            setupProductFocusBorder(productPanel, false); // Update border based on new selection state
        });

        // KEYBOARD NAVIGATION FOR CHECKBOX - SIMPLIFIED VERSION
        productPanel.getCheckBox().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        // Toggle checkbox selection
                        boolean isSelected = !productPanel.getCheckBox().isSelected();
                        productPanel.getCheckBox().setSelected(isSelected);
                        productSelectedStatus.put(productIndex, isSelected);
                        productPanel.getNewQty().setEnabled(isSelected);

                        if (isSelected) {
                            // Set default quantity to the original purchased quantity
                            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                            int currentStockQty = getCurrentStockQty(productIndex);
                            int maxExchangeQty = Math.min(originalQty, currentStockQty);
                            int defaultQty = Math.max(1, Math.min(originalQty, maxExchangeQty));

                            productPanel.getNewQty().setText(String.valueOf(defaultQty));

                            // Move focus to quantity field for immediate editing
                            SwingUtilities.invokeLater(() -> {
                                productPanel.getNewQty().requestFocusInWindow();
                                productPanel.getNewQty().selectAll();
                            });
                        } else {
                            productPanel.getNewQty().setText("0");
                        }
                        evt.consume();
                        break;

                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_RIGHT:
                        moveToNextProduct(productIndex);
                        evt.consume();
                        break;

                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_LEFT:
                        moveToPreviousProduct(productIndex);
                        evt.consume();
                        break;

                    case KeyEvent.VK_TAB:
                        if (evt.isShiftDown()) {
                            moveToPreviousProduct(productIndex);
                        } else {
                            moveToNextProduct(productIndex);
                        }
                        evt.consume();
                        break;
                }
            }
        });

        // KEYBOARD NAVIGATION FOR QUANTITY FIELD
        productPanel.getNewQty().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        // ENTER in quantity field: Validate and move to next product or reason field
                        validateQuantityField(productPanel, productIndex);
                        moveToNextProduct(productIndex);
                        evt.consume();
                        break;

                    case KeyEvent.VK_UP:
                        // UP: Go back to checkbox
                        validateQuantityField(productPanel, productIndex);
                        productPanel.getCheckBox().requestFocusInWindow();
                        evt.consume();
                        break;

                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_RIGHT:
                        // DOWN/RIGHT: Validate and move to next product
                        validateQuantityField(productPanel, productIndex);
                        moveToNextProduct(productIndex);
                        evt.consume();
                        break;

                    case KeyEvent.VK_LEFT:
                        // LEFT: Validate and go back to checkbox
                        validateQuantityField(productPanel, productIndex);
                        productPanel.getCheckBox().requestFocusInWindow();
                        evt.consume();
                        break;

                    case KeyEvent.VK_TAB:
                        validateQuantityField(productPanel, productIndex);
                        if (evt.isShiftDown()) {
                            productPanel.getCheckBox().requestFocusInWindow();
                        } else {
                            moveToNextProduct(productIndex);
                        }
                        evt.consume();
                        break;

                    case KeyEvent.VK_ESCAPE:
                        // ESC: Clear selection and go back to checkbox
                        productPanel.getCheckBox().setSelected(false);
                        productSelectedStatus.put(productIndex, false);
                        productPanel.getNewQty().setEnabled(false);
                        productPanel.getNewQty().setText("0");
                        productPanel.getCheckBox().requestFocusInWindow();
                        evt.consume();
                        break;
                }
            }
        });

        // Add focus lost validation
        productPanel.getNewQty().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                validateQuantityField(productPanel, productIndex);
            }
        });
    }

    // Validation method for quantity field
    private boolean isValidQuantity(String text, ExchangeProduct productPanel, int productIndex) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        try {
            int value = Integer.parseInt(text);
            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
            int currentStockQty = getCurrentStockQty(productIndex);
            int maxExchangeQty = Math.min(originalQty, currentStockQty);

            // Allow values from 1 to the maximum exchange quantity
            return value >= 1 && value <= maxExchangeQty;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Method to validate and correct quantity field
    private void validateQuantityField(ExchangeProduct productPanel, int productIndex) {
        String text = productPanel.getNewQty().getText().trim();

        if (text.isEmpty()) {
            // If empty, set to minimum 1
            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
            int currentStockQty = getCurrentStockQty(productIndex);
            int maxExchangeQty = Math.min(originalQty, currentStockQty);
            int defaultQty = Math.max(1, Math.min(originalQty, maxExchangeQty));
            productPanel.getNewQty().setText(String.valueOf(defaultQty));
            return;
        }

        try {
            int value = Integer.parseInt(text);
            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
            int currentStockQty = getCurrentStockQty(productIndex);
            int maxExchangeQty = Math.min(originalQty, currentStockQty);

            // Apply constraints
            if (value < 1) {
                int defaultQty = Math.max(1, Math.min(originalQty, maxExchangeQty));
                productPanel.getNewQty().setText(String.valueOf(defaultQty));
                showToastNotification("Quantity cannot be less than 1", Notifications.Type.WARNING);
            } else if (value > maxExchangeQty) {
                productPanel.getNewQty().setText(String.valueOf(maxExchangeQty));
                showToastNotification("Cannot exchange more than available quantity (" + maxExchangeQty + ")", Notifications.Type.WARNING);
            }
            // If value is valid (1 to maxExchangeQty), keep it as is

        } catch (NumberFormatException e) {
            // If not a number, set to default existing quantity
            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
            int currentStockQty = getCurrentStockQty(productIndex);
            int maxExchangeQty = Math.min(originalQty, currentStockQty);
            int defaultQty = Math.max(1, Math.min(originalQty, maxExchangeQty));
            productPanel.getNewQty().setText(String.valueOf(defaultQty));
            showToastNotification("Please enter a valid number", Notifications.Type.WARNING);
        }
    }

    // Helper method to show toast notifications
    private void showToastNotification(String message, Notifications.Type type) {
        Notifications.getInstance().show(type, Notifications.Location.TOP_RIGHT, message);
    }

    private void moveToNextProduct(int currentIndex) {
        if (currentIndex < productPanels.size() - 1) {
            // Move to next product panel
            currentProductIndex = currentIndex + 1;
            ExchangeProduct nextProduct = productPanels.get(currentProductIndex);
            nextProduct.getCheckBox().requestFocusInWindow();
            setupProductFocusBorder(nextProduct, true);
            scrollToProduct(nextProduct);
            isFirstEnterOnProduct = true;
        } else {
            // No more products, move to reason field
            reasonCombo.requestFocusInWindow();
        }
    }

    private void moveToPreviousProduct(int currentIndex) {
        if (currentIndex > 0) {
            // Move to previous product panel
            currentProductIndex = currentIndex - 1;
            ExchangeProduct prevProduct = productPanels.get(currentProductIndex);
            prevProduct.getCheckBox().requestFocusInWindow();
            setupProductFocusBorder(prevProduct, true);
            scrollToProduct(prevProduct);
            isFirstEnterOnProduct = true;
        } else {
            // First product, move back to invoice combo
            invoiceCombo.requestFocusInWindow();
        }
    }

    private int getCurrentStockQty(int productIndex) {
        try {
            int stockId = productStockIds.get(productIndex);
            String query = "SELECT qty FROM stock WHERE stock_id = " + stockId;
            ResultSet rs = MySQL.executeSearch(query);
            if (rs.next()) {
                return rs.getInt("qty");
            }
        } catch (SQLException e) {
            showToastNotification("Error loading stock quantity: " + e.getMessage(), Notifications.Type.ERROR);
        }
        return 0;
    }

    private void loadInvoices() {
        try {
            String query = "SELECT sales_id, invoice_no FROM sales WHERE status_id = 1 ORDER BY sales_id DESC";
            ResultSet rs = MySQL.executeSearch(query);

            invoiceMap.clear();
            invoiceCombo.removeAllItems();
            invoiceCombo.addItem("Select Invoice");

            while (rs.next()) {
                int salesId = rs.getInt("sales_id");
                String invoiceNo = rs.getString("invoice_no");
                invoiceMap.put(salesId, invoiceNo);
                invoiceCombo.addItem(invoiceNo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showToastNotification("Error loading invoices: " + e.getMessage(), Notifications.Type.ERROR);
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
            e.printStackTrace();
            showToastNotification("Error loading return reasons: " + e.getMessage(), Notifications.Type.ERROR);
        }
    }

    private void showNoInvoiceSelectedMessage() {
        productsPanel.removeAll();
        productPanels.clear();

        JPanel messagePanel = new JPanel();
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setPreferredSize(new Dimension(538, 200));

        JLabel messageLabel = new JLabel("Please select an invoice to view details", JLabel.CENTER);
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
            currentProductIndex = -1;
            isFirstEnterOnProduct = false;

            String query = "SELECT s.sales_id, s.datetime, s.total, s.invoice_no, "
                    + "u.name as cashier_name, pm.payment_method_name, "
                    + "cc.customer_id, cc.customer_name, d.discount, dt.discount_type, "
                    + "s.discount_id, s.credit_customer_id, s.payment_method_id "
                    + "FROM sales s "
                    + "LEFT JOIN user u ON s.user_id = u.user_id "
                    + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                    + "LEFT JOIN credit_customer cc ON s.credit_customer_id = cc.customer_id "
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
            }

            productsPanel.revalidate();
            productsPanel.repaint();

            SwingUtilities.invokeLater(() -> {
                if (!productPanels.isEmpty()) {
                    focusFirstProduct();
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showToastNotification("Error loading invoice details: " + e.getMessage(), Notifications.Type.ERROR);
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
            e.printStackTrace();
            showToastNotification("Error loading credit amount: " + e.getMessage(), Notifications.Type.ERROR);
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
            showToastNotification("Error loading credit payments: " + e.getMessage(), Notifications.Type.ERROR);
        }
        return 0.0;
    }

    private JPanel createInvoiceHeaderPanel(ResultSet rs) throws SQLException {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setLayout(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 5));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

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
                bgColor = new Color(40, 167, 69); // Green for cash
            } else if (method.contains("card")) {
                bgColor = new Color(66, 66, 255); // Blue for card
            } else if (method.contains("credit")) {
                bgColor = new Color(255, 193, 7); // Yellow for credit
                fgColor = Color.BLACK;
            } else {
                bgColor = new Color(108, 117, 125); // Gray for others
            }
        } else {
            bgColor = new Color(108, 117, 125); // Gray for unknown
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
                productPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
                productPanel.setMinimumSize(new Dimension(530, 120));
                productPanel.setPreferredSize(new Dimension(530, 124));
                productPanel.setBackground(Color.WHITE);
                productPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

                productPanel.getProductName().setText(
                        rs.getString("product_name") + " (" + rs.getString("brand_name") + ")"
                );

                double itemTotal = rs.getDouble("item_total");

                productPanel.getQty().setText("Original Quantity = " + originalQty);
                // Update tooltip to show max available quantity constraint
                int maxExchangeQty = Math.min(originalQty, currentStockQty);
                productPanel.getQty().setToolTipText("Available in stock: " + currentStockQty + " | Max exchange: " + maxExchangeQty);

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

                // Initialize quantity field
                productPanel.getNewQty().setText("0");
                productPanel.getNewQty().setEnabled(false);
                productPanel.getCheckBox().setSelected(false);

                final int currentIndex = productIndex;
                productPanel.getCheckBox().addActionListener(e -> {
                    boolean isSelected = productPanel.getCheckBox().isSelected();
                    productSelectedStatus.put(currentIndex, isSelected);
                    productPanel.getNewQty().setEnabled(isSelected);

                    if (isSelected) {
                        int originalQtyVal = productOriginalQtys.getOrDefault(currentIndex, 1);
                        int currentStockQtyVal = getCurrentStockQty(currentIndex);
                        int maxExchangeQtyVal = Math.min(originalQtyVal, currentStockQtyVal);
                        int defaultQty = Math.max(1, Math.min(originalQtyVal, maxExchangeQtyVal));
                        productPanel.getNewQty().setText(String.valueOf(defaultQty));
                    } else {
                        productPanel.getNewQty().setText("0");
                    }
                    // Update border when checkbox state changes
                    setupProductFocusBorder(productPanel, false);
                });

                productPanel.getNewQty().addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        productPanel.getNewQty().selectAll();
                    }
                });

                setupProductKeyboardNavigation(productPanel, productIndex);
                productPanels.add(productPanel);

                productsContainer.add(productPanel);

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
                noProductsLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 20, 30));
                productsContainer.add(noProductsLabel);
            }

            productsPanel.add(productsContainer);

        } catch (SQLException e) {
            e.printStackTrace();
            showToastNotification("Error loading products: " + e.getMessage(), Notifications.Type.ERROR);
        }
    }

    private void clearForm() {
        invoiceCombo.setSelectedIndex(0);
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
        productPanels.clear();
        currentSalesId = -1;
        currentCreditCustomerId = -1;
        currentCreditAmount = 0.0;
        isCreditPayment = false;
        currentProductIndex = -1;
        isFirstEnterOnProduct = false;
        generatedReturnId = -1; // Reset return ID

        invoiceCombo.requestFocusInWindow();
    }

    private double calculateRefundAmount() {
        double refundAmount = 0.0;
        int productIndex = 0;

        for (ExchangeProduct productPanel : productPanels) {
            if (productSelectedStatus.getOrDefault(productIndex, false)) {
                try {
                    String qtyText = productPanel.getNewQty().getText().trim();
                    int exchangeQty = Integer.parseInt(qtyText);

                    if (exchangeQty > 0) {
                        // Get the stored data from your maps
                        double unitPrice = productUnitPrices.getOrDefault(productIndex, 0.0);
                        double discountPrice = productDiscountPrices.getOrDefault(productIndex, 0.0);
                        int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);

                        // Calculate the total price for the original purchase
                        double originalTotalPrice = unitPrice * originalQty;

                        // Calculate the actual paid amount after discount
                        double actualPaidAmount = originalTotalPrice - discountPrice;

                        // Calculate the effective unit price (after discount)
                        double effectiveUnitPrice = actualPaidAmount / originalQty;

                        // Calculate refund amount for exchanged quantity
                        double proportionalAmount = effectiveUnitPrice * exchangeQty;
                        refundAmount += proportionalAmount;

                        System.out.println("Product " + productIndex
                                + " - Unit Price: " + unitPrice
                                + ", Original Qty: " + originalQty
                                + ", Discount: " + discountPrice
                                + ", Effective Unit: " + effectiveUnitPrice
                                + ", Exchange Qty: " + exchangeQty
                                + ", Refund: " + proportionalAmount);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid quantity for product index " + productIndex + ": " + e.getMessage());
                }
            }
            productIndex++;
        }

        System.out.println("Total Refund Amount: " + refundAmount);
        return Math.max(0.0, refundAmount); // Ensure non-negative
    }

    private boolean isStockLossReason(String reason) {
        return reason.toLowerCase().contains("expired")
                || reason.toLowerCase().contains("damaged")
                || reason.toLowerCase().contains("theft")
                || reason.toLowerCase().contains("misplaced");
    }

    private void processExchange() {
        Connection conn = null;
        try {
            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

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
                showToastNotification("Invalid return reason selected", Notifications.Type.ERROR);
                return;
            }

            boolean hasSelectedProducts = false;
            for (Boolean selected : productSelectedStatus.values()) {
                if (selected) {
                    hasSelectedProducts = true;
                    break;
                }
            }

            if (!hasSelectedProducts) {
                showToastNotification("Please select at least one product to return", Notifications.Type.WARNING);
                return;
            }

            double totalRefundAmount = calculateRefundAmount();
            double totalDiscountPrice = calculateTotalDiscountPrice();

            String returnQuery = "INSERT INTO `return` (return_date, total_return_amount, return_reason_id, "
                    + "status_id, sales_id, user_id, total_discount_price) "
                    + "VALUES (NOW(), ?, ?, 1, ?, ?, ?)";

            PreparedStatement returnStmt = conn.prepareStatement(returnQuery, Statement.RETURN_GENERATED_KEYS);
            returnStmt.setDouble(1, totalRefundAmount);
            returnStmt.setInt(2, reasonId);
            returnStmt.setInt(3, currentSalesId);
            returnStmt.setInt(4, currentUserId);
            returnStmt.setDouble(5, totalDiscountPrice);

            int affectedRows = returnStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating return failed, no rows affected.");
            }

            int returnId;
            try (ResultSet generatedKeys = returnStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    returnId = generatedKeys.getInt(1);
                    generatedReturnId = returnId; // Store return ID for calling center
                } else {
                    throw new SQLException("Creating return failed, no ID obtained.");
                }
            }

            boolean isStockLoss = isStockLossReason(selectedReason);
            int productIndex = 0;

            // FIX: Use productPanels list instead of productsPanel.getComponents()
            for (ExchangeProduct productPanel : productPanels) {
                if (productSelectedStatus.getOrDefault(productIndex, false)) {
                    try {
                        int exchangeQty = Integer.parseInt(productPanel.getNewQty().getText().trim());
                        if (exchangeQty > 0) {
                            int saleItemId = productSaleItemIds.get(productIndex);
                            int stockId = productStockIds.get(productIndex);
                            int originalQty = productOriginalQtys.get(productIndex);
                            double unitPrice = productUnitPrices.get(productIndex);
                            double discountPrice = productDiscountPrices.get(productIndex);

                            double itemTotalBeforeDiscount = unitPrice * exchangeQty;
                            double itemDiscountAmount = (discountPrice / originalQty) * exchangeQty;
                            double itemTotalReturnAmount = itemTotalBeforeDiscount - itemDiscountAmount;

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

                            if (isStockLoss) {
                                String stockLossQuery = "INSERT INTO stock_loss (qty, stock_id, stock_loss_date, "
                                        + "return_reason_id, user_id, sales_id) "
                                        + "VALUES (?, ?, NOW(), ?, ?, ?)";

                                PreparedStatement stockLossStmt = conn.prepareStatement(stockLossQuery);
                                stockLossStmt.setInt(1, exchangeQty);
                                stockLossStmt.setInt(2, stockId);
                                stockLossStmt.setInt(3, reasonId);
                                stockLossStmt.setInt(4, currentUserId);
                                stockLossStmt.setInt(5, currentSalesId);

                                stockLossStmt.executeUpdate();
                            } else {
                                String updateStockQuery = "UPDATE stock SET qty = qty + ? WHERE stock_id = ?";
                                PreparedStatement updateStockStmt = conn.prepareStatement(updateStockQuery);
                                updateStockStmt.setInt(1, exchangeQty);
                                updateStockStmt.setInt(2, stockId);
                                updateStockStmt.executeUpdate();
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Skip products with invalid quantity
                    }
                }

                productIndex++;
            }

            if (isCreditPayment && currentCreditCustomerId != -1) {
                handleCreditRefund(conn, totalRefundAmount);
            }

            conn.commit();

            String message = "Exchange processed successfully! Return ID: " + returnId;
            showToastNotification(message, Notifications.Type.SUCCESS);

            // Close dialog after successful exchange
            dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showToastNotification("Error processing exchange: " + e.getMessage(), Notifications.Type.ERROR);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showToastNotification("Error parsing price data", Notifications.Type.ERROR);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleCreditRefund(Connection conn, double refundAmount) throws SQLException {
        double totalPaid = getTotalCreditPayments();

        if (totalPaid >= refundAmount) {
            String updateCreditQuery = "UPDATE credit SET credit_amout = credit_amout - ? "
                    + "WHERE credit_customer_id = ? ORDER BY credit_id DESC LIMIT 1";
            PreparedStatement updateStmt = conn.prepareStatement(updateCreditQuery);
            updateStmt.setDouble(1, refundAmount);
            updateStmt.setInt(2, currentCreditCustomerId);
            updateStmt.executeUpdate();
        } else {
            String updateCreditQuery = "UPDATE credit SET credit_amout = credit_amout - ? "
                    + "WHERE credit_customer_id = ? ORDER BY credit_id DESC LIMIT 1";
            PreparedStatement updateStmt = conn.prepareStatement(updateCreditQuery);
            updateStmt.setDouble(1, refundAmount);
            updateStmt.setInt(2, currentCreditCustomerId);
            updateStmt.executeUpdate();
        }
    }

    private double calculateTotalDiscountPrice() {
        double totalDiscount = 0.0;
        int productIndex = 0;

        for (ExchangeProduct productPanel : productPanels) {
            if (productSelectedStatus.getOrDefault(productIndex, false)) {
                try {
                    int exchangeQty = Integer.parseInt(productPanel.getNewQty().getText().trim());
                    if (exchangeQty > 0) {
                        int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                        double discountPrice = productDiscountPrices.getOrDefault(productIndex, 0.0);

                        // Calculate proportional discount
                        double proportionalDiscount = (discountPrice / originalQty) * exchangeQty;
                        totalDiscount += proportionalDiscount;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid quantities
                }
            }
            productIndex++;
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
        invoiceCombo = new javax.swing.JComboBox<>();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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

        invoiceCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        invoiceCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        invoiceCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Invoice No *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        invoiceCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invoiceComboActionPerformed(evt);
            }
        });
        invoiceCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                invoiceComboKeyPressed(evt);
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
                    .addComponent(invoiceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reasonCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(productsScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                .addComponent(invoiceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        if (invoiceCombo.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (reasonCombo.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a return reason",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if any products are selected with quantities > 0
        boolean hasSelectedProducts = false;
        int productIndex = 0;

        // FIX: Use productPanels list instead of productsPanel.getComponents()
        for (ExchangeProduct productPanel : productPanels) {
            if (productSelectedStatus.getOrDefault(productIndex, false)) {
                try {
                    int exchangeQty = Integer.parseInt(productPanel.getNewQty().getText().trim());
                    if (exchangeQty > 0) {
                        hasSelectedProducts = true;
                        break;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid quantities
                }
            }
            productIndex++;
        }

        if (!hasSelectedProducts) {
            JOptionPane.showMessageDialog(this, "Please select quantities to exchange",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double refundAmount = calculateRefundAmount();
        String selectedReason = (String) reasonCombo.getSelectedItem();
        boolean isStockLoss = isStockLossReason(selectedReason);

        String confirmationMessage = "Total Refund Amount: Rs." + String.format("%.2f", refundAmount)
                + "\nReturn Reason: " + selectedReason
                + "\n\nStock Handling: "
                + (isStockLoss ? "Items will be marked as STOCK LOSS (not returned to inventory)"
                        : "Items will be RETURNED TO INVENTORY")
                + "\n\nDo you want to proceed with the exchange?";

        int confirm = JOptionPane.showConfirmDialog(this, confirmationMessage,
                "Confirm Exchange", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            processExchange();
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

    private void invoiceComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_invoiceComboKeyPressed

    }//GEN-LAST:event_invoiceComboKeyPressed

    private void invoiceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceComboActionPerformed
        if (!invoiceCombo.isPopupVisible() && invoiceCombo.getSelectedIndex() > 0) {
            String selectedInvoice = (String) invoiceCombo.getSelectedItem();
            loadInvoiceDetails(selectedInvoice);
        }
    }//GEN-LAST:event_invoiceComboActionPerformed

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
    private javax.swing.JComboBox<String> invoiceCombo;
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
