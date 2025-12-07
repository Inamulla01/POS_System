package lk.com.pos.panel;

import lk.com.pos.privateclasses.RoundedPanel;
import lk.com.pos.dto.PosPanelDTO;
import lk.com.pos.dao.PosPanelDAO;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lk.com.pos.privateclasses.CartListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import lk.com.pos.privateclasses.StockTracker;
import lk.com.pos.privateclasses.ProductCardReference;
import raven.toast.Notifications;

class WrapLayout extends FlowLayout {

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(java.awt.Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(java.awt.Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(java.awt.Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            java.awt.Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int maxWidth = targetWidth - horizontalInsetsAndGap;

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                java.awt.Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    if (rowWidth != 0) {
                        rowWidth += hgap;
                    }

                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }

            addRow(dim, rowWidth, rowHeight);

            dim.width += horizontalInsetsAndGap;
            dim.height += insets.top + insets.bottom + vgap * 2;

            java.awt.Container scrollPane = javax.swing.SwingUtilities.getAncestorOfClass(
                    javax.swing.JScrollPane.class, target);
            if (scrollPane != null && target.isValid()) {
                dim.width -= (hgap + 1);
            }

            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);

        if (dim.height > 0) {
            dim.height += getVgap();
        }

        dim.height += rowHeight;
    }
}

class RoundBorder extends javax.swing.border.AbstractBorder {

    private Color color;
    private int thickness;
    private int radius;

    public RoundBorder(Color color, int thickness, int radius) {
        this.color = color;
        this.thickness = thickness;
        this.radius = radius;
    }

    @Override
    public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setColor(color);
        g2d.setStroke(new java.awt.BasicStroke(thickness));

        int adjustment = thickness / 2;
        g2d.drawRoundRect(x + adjustment, y + adjustment,
                width - thickness, height - thickness,
                radius, radius);
        g2d.dispose();
    }

    @Override
    public java.awt.Insets getBorderInsets(java.awt.Component c) {
        return new java.awt.Insets(thickness + 1, thickness + 1, thickness + 1, thickness + 1);
    }

    @Override
    public java.awt.Insets getBorderInsets(java.awt.Component c, java.awt.Insets insets) {
        insets.left = insets.right = insets.top = insets.bottom = thickness + 1;
        return insets;
    }
}

public class PosPanelDone extends javax.swing.JPanel implements CartListener {

    @Override
    public void onCartUpdated(double total, int itemCount) {
        if (!productSearchBar.hasFocus() && isShowing()) {
            if (cartUpdateTimer != null && cartUpdateTimer.isRunning()) {
                cartUpdateTimer.restart();
            } else {
                cartUpdateTimer = new javax.swing.Timer(300, e -> {
                    StockTracker.getInstance().refreshAllCards();
                });
                cartUpdateTimer.setRepeats(false);
                cartUpdateTimer.start();
            }
        }

        if (itemCount > 0 && !productSearchBar.getText().isEmpty()
                && !productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
            SwingUtilities.invokeLater(() -> {
                clearSearch();
                clearCardSelection();
            });
        }
    }

    @Override
    public void onCheckoutComplete() {
        SwingUtilities.invokeLater(() -> {
            StockTracker.getInstance().clearCache();
            StockTracker.getInstance().refreshAllCards();
            clearSearch();
        });
    }

    private javax.swing.Timer cartUpdateTimer;
    private javax.swing.Timer refreshTimer;
    private volatile boolean isLoadingProducts = false;

    private static final Color TEAL_COLOR = new Color(28, 181, 187);
    private static final Color LIGHT_GRAY_BG = new Color(245, 245, 245);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color TEXT_GRAY = new Color(102, 102, 102);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color CARD_HOVER = new Color(240, 250, 250);
    private static final Color SINGLE_RESULT_HIGHLIGHT = new Color(220, 240, 255);

    private PosCartPanel posCartPanel;
    private List<RoundedPanel> productCards = new ArrayList<>();
    private int currentCardIndex = -1;
    private int columnsPerRow = 2;
    private javax.swing.Timer searchTimer;
    private boolean hasSingleSearchResult = false;
    private boolean isInitialLoadComplete = false;
    private PosPanelDAO posPanelDAO;

    public PosPanelDone() {
        initComponents();
        init();
        SwingUtilities.invokeLater(() -> {
            loadProductInBackground("");
        });
    }

    private void init() {
        posPanelDAO = new PosPanelDAO();
        setupKeyboardNavigation();
        setupSearchFunctionality();
        setupGlobalShortcuts();

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        refreshTimer = new javax.swing.Timer(10000, e -> {
            if (isShowing() && !productSearchBar.hasFocus()) {
                StockTracker.getInstance().refreshAllCards();
            }
        });
        refreshTimer.setInitialDelay(10000);
        refreshTimer.start();

        selectProductPanel.setDrawBorder(false);
        selectProductPanel.removeBorder();

        cartPanel.setDrawBorder(false);
        cartPanel.removeBorder();

        posCartPanel = new PosCartPanel();
        posCartPanel.setCartListener(this);

        cartPanel.setLayout(new java.awt.BorderLayout());
        cartPanel.add(posCartPanel, java.awt.BorderLayout.CENTER);

        jScrollPane2.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane2.getVerticalScrollBar().setBlockIncrement(80);

        jPanel7.setLayout(new WrapLayout(FlowLayout.LEADING, 15, 15));
        jPanel7.setBackground(Color.WHITE);

        jScrollPane2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                updateProductCardSizes();
                calculateColumnsPerRow();
            }
        });

        setupSearchBarPlaceholder();

        addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        SwingUtilities.invokeLater(() -> {
                            updateProductCardSizes();
                            calculateColumnsPerRow();
                            if (isInitialLoadComplete) {
                                requestFocusInWindow();
                            }
                        });
                    }
                }
            }
        });

        jScrollPane2.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");
                
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void setupGlobalShortcuts() {
        javax.swing.InputMap inputMap = getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap actionMap = getActionMap();

        inputMap.clear();
        actionMap.clear();

        // F1 - Show Help (Centralized)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "ShowHelp");
        actionMap.put("ShowHelp", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                lk.com.pos.privateclasses.KeyboardShortcutManager.getInstance().showHelp(PosPanelDone.this);
            }
        });

        // F2 - Focus Product Search
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "FocusSearch");
        actionMap.put("FocusSearch", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                productSearchBar.requestFocusInWindow();
                if (productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
                    productSearchBar.setText("");
                    productSearchBar.setForeground(java.awt.Color.BLACK);
                }
                productSearchBar.selectAll();
                showQuickFeedback("🔍 Search Mode");
            }
        });

        // F3 - Focus Cart
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "FocusCart");
        actionMap.put("FocusCart", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null && posCartPanel.hasCartItems()) {
                    posCartPanel.requestCartFocus();
                    posCartPanel.navigateCartItems(0);
                    showQuickFeedback("🛒 Cart Mode");
                } else {
                    showQuickFeedback("Cart is empty");
                }
            }
        });

        // F4-F7 - Payment Methods (Centralized)
        setupPaymentShortcuts(inputMap, actionMap);
        
        // F8 - Clear Cart
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "ClearCart");
        actionMap.put("ClearCart", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.clearCart();
                }
            }
        });

        // F9 - Complete Sale
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "CompleteSale");
        actionMap.put("CompleteSale", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.gradientButton1ActionPerformed(null);
                }
            }
        });

        // F10 - Discount
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "Discount");
        actionMap.put("Discount", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.discountBtnActionPerformed(null);
                }
            }
        });

        // F11 - Hold Bill
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "HoldBill");
        actionMap.put("HoldBill", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.holdBtnActionPerformed(null);
                }
            }
        });

        // F12 - Switch Invoice
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "SwitchInvoice");
        actionMap.put("SwitchInvoice", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.showSwitchInvoicePanel();
                }
            }
        });

        // Ctrl+E - Exchange (Centralized)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK), "Exchange");
        actionMap.put("Exchange", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.exchangeBtnActionPerformed(null);
                }
            }
        });

        // Context-aware shortcuts (+ and -)
        setupContextAwareShortcuts(inputMap, actionMap);

        // KEEP EXISTING ALT+ SHORTCUTS (for backward compatibility)
        setupAltShortcuts(inputMap, actionMap);

        setFocusable(true);
    }

    private void setupPaymentShortcuts(javax.swing.InputMap inputMap, javax.swing.ActionMap actionMap) {
        // F4 - Cash Payment
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "CashPayment");
        actionMap.put("CashPayment", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.selectCashPayment();
                    showQuickFeedback("💵 Cash Payment Selected");
                }
            }
        });

        // F5 - Card Payment
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "CardPayment");
        actionMap.put("CardPayment", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.selectCardPayment();
                    showQuickFeedback("💳 Card Payment Selected");
                }
            }
        });

        // F6 - Credit Payment
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "CreditPayment");
        actionMap.put("CreditPayment", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.selectCreditPayment();
                    showQuickFeedback("📝 Credit Payment Selected");
                }
            }
        });

        // F7 - Cheque Payment
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "ChequePayment");
        actionMap.put("ChequePayment", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.selectChequePayment();
                    showQuickFeedback("🏦 Cheque Payment Selected");
                }
            }
        });
    }

    private void setupContextAwareShortcuts(javax.swing.InputMap inputMap, javax.swing.ActionMap actionMap) {
        // + key - Context aware (product or cart)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "IncreasePlus");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "IncreaseAdd");
        
        AbstractAction increaseAction = new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (productSearchBar.hasFocus()) {
                    return; // Don't interfere with search
                }
                
                // If cart has focus and items, increase cart item qty
                if (posCartPanel != null && posCartPanel.hasCartItems() && 
                    posCartPanel.getFocusedCartItemIndex() >= 0) {
                    posCartPanel.increaseFocusedItemQuantity();
                }
                // Otherwise, add selected product
                else if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
                    addSelectedProductToCart();
                }
            }
        };
        
        actionMap.put("IncreasePlus", increaseAction);
        actionMap.put("IncreaseAdd", increaseAction);

        // - key - Only for cart items
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "DecreaseMinus");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "DecreaseSubtract");
        
        AbstractAction decreaseAction = new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null && posCartPanel.hasCartItems() && 
                    posCartPanel.getFocusedCartItemIndex() >= 0) {
                    posCartPanel.decreaseFocusedItemQuantity();
                }
            }
        };
        
        actionMap.put("DecreaseMinus", decreaseAction);
        actionMap.put("DecreaseSubtract", decreaseAction);

        // Delete key - Context aware
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
        actionMap.put("Delete", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null && posCartPanel.hasCartItems() && 
                    posCartPanel.getFocusedCartItemIndex() >= 0) {
                    posCartPanel.deleteFocusedCartItem();
                } else if (!posCartPanel.hasCartItems()) {
                    // Cart is empty, do nothing
                } else {
                    // No specific item selected, offer to clear cart
                    posCartPanel.clearCart();
                }
            }
        });

        // Space bar - Add product (like Enter)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "AddProduct");
        actionMap.put("AddProduct", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!productSearchBar.hasFocus() && currentCardIndex >= 0 && 
                    currentCardIndex < productCards.size()) {
                    addSelectedProductToCart();
                }
            }
        });
    }

    private void setupAltShortcuts(javax.swing.InputMap inputMap, javax.swing.ActionMap actionMap) {
        // KEEP ALL EXISTING ALT+ SHORTCUTS FOR BACKWARD COMPATIBILITY
        
        // Product/Payment - Alt + P - OPEN CREDIT PAYMENT
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_DOWN_MASK), "CreditPaymentAltP");
        actionMap.put("CreditPaymentAltP", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.openCreditPayment();
                }
            }
        });

        // Discount - Alt + D
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_DOWN_MASK), "DiscountAltD");
        actionMap.put("DiscountAltD", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.discountBtnActionPerformed(null);
                }
            }
        });

        // Switch - Alt + S
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK), "SwitchAltS");
        actionMap.put("SwitchAltS", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.showSwitchInvoicePanel();
                }
            }
        });

        // Exchange Credit - Alt + E
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_DOWN_MASK), "ExchangeAltE");
        actionMap.put("ExchangeAltE", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.exchangeBtnActionPerformed(null);
                }
            }
        });

        // Hold Bill - Alt + H
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK), "HoldAltH");
        actionMap.put("HoldAltH", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.holdBtnActionPerformed(null);
                }
            }
        });

        // Delete Selected Cart Item - Alt + X
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_DOWN_MASK), "DeleteItemAltX");
        actionMap.put("DeleteItemAltX", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.deleteFocusedCartItem();
                }
            }
        });

        // Complete Sale - Alt + Enter
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), "CompleteSaleAltEnter");
        actionMap.put("CompleteSaleAltEnter", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.gradientButton1ActionPerformed(null);
                }
            }
        });

        // Product Count - Ctrl + Q
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), "ProductCountCtrlQ");
        actionMap.put("ProductCountCtrlQ", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int totalProducts = productCards.size();
                String message = "Total Products Available: " + totalProducts;
                if (totalProducts > 0) {
                    message += "\nUse Arrow Keys to navigate, Enter to add to cart";
                }
                JOptionPane.showMessageDialog(PosPanelDone.this,
                        message,
                        "Product Count",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Focus Product Search Bar - Alt + F (Product Panel Only)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK), "FocusProductSearchAltF");
        actionMap.put("FocusProductSearchAltF", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                productSearchBar.requestFocusInWindow();
                if (productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
                    productSearchBar.setText("");
                    productSearchBar.setForeground(java.awt.Color.BLACK);
                }
                productSearchBar.selectAll();
            }
        });

        // Payment Method Selection - Alt+1, Alt+2, Alt+3, Alt+4
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK), "CashPaymentAlt1");
        actionMap.put("CashPaymentAlt1", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.selectCashPayment();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK), "CardPaymentAlt2");
        actionMap.put("CardPaymentAlt2", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.selectCardPayment();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK), "CreditPaymentAlt3");
        actionMap.put("CreditPaymentAlt3", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.selectCreditPayment();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK), "ChequePaymentAlt4");
        actionMap.put("ChequePaymentAlt4", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null) {
                    posCartPanel.selectChequePayment();
                }
            }
        });

        // Cart Item Navigation - Alt + Arrow Keys
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK), "CartPrevAltUp");
        actionMap.put("CartPrevAltUp", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null && posCartPanel.hasCartItems()) {
                    posCartPanel.navigateCartItems(-1);
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK), "CartNextAltDown");
        actionMap.put("CartNextAltDown", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null && posCartPanel.hasCartItems()) {
                    posCartPanel.navigateCartItems(1);
                }
            }
        });

        // Alt + Q - Focus Quantity Field of Selected Cart Item
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.ALT_DOWN_MASK), "FocusQuantityAltQ");
        actionMap.put("FocusQuantityAltQ", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null && posCartPanel.hasCartItems()) {
                    posCartPanel.focusSelectedItemQuantity();
                }
            }
        });

        // Alt + R - Focus Discount Field of Selected Cart Item
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK), "FocusDiscountAltR");
        actionMap.put("FocusDiscountAltR", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (posCartPanel != null && posCartPanel.hasCartItems()) {
                    posCartPanel.focusSelectedItemDiscount();
                }
            }
        });

        // Alt + T - Refresh Products
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK), "RefreshProductsAltT");
        actionMap.put("RefreshProductsAltT", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                manualReload();
                Notifications.getInstance().show(
                    Notifications.Type.INFO,
                    Notifications.Location.TOP_RIGHT,
                    "Products refreshed"
                );
            }
        });
    }

    private void showQuickFeedback(String message) {
        Notifications.getInstance().show(
            Notifications.Type.INFO,
            Notifications.Location.TOP_RIGHT,
            message
        );
    }

    private void setupSearchBarPlaceholder() {
        productSearchBar.setForeground(java.awt.Color.GRAY);
        productSearchBar.setText("Search products by name or barcode...");

        productSearchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
                    productSearchBar.setText("");
                    productSearchBar.setForeground(java.awt.Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (productSearchBar.getText().isEmpty()) {
                    productSearchBar.setForeground(java.awt.Color.GRAY);
                    productSearchBar.setText("Search products by name or barcode...");
                }
            }
        });
    }

    private void setupSearchFunctionality() {
        searchTimer = new javax.swing.Timer(400, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                performSearch();
            }
        });
        searchTimer.setRepeats(false);

        productSearchBar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                handleSearchInput();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                handleSearchInput();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                handleSearchInput();
            }

            private void handleSearchInput() {
                if (!productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
                    searchTimer.restart();
                }
            }
        });
    }

    private void performSearch() {
        String searchText = productSearchBar.getText().trim();

        if (productSearchBar.getForeground().equals(java.awt.Color.GRAY) || searchText.isEmpty()) {
            loadProductInBackground("");
        } else {
            loadProductInBackground(searchText);
        }
    }

    private void clearSearch() {
        productSearchBar.setText("");
        productSearchBar.setForeground(java.awt.Color.GRAY);
        productSearchBar.setText("Search products by name or barcode...");
        loadProductInBackground("");
    }

    private void calculateColumnsPerRow() {
        int viewportWidth = jScrollPane2.getViewport().getWidth();
        if (viewportWidth < 100) {
            viewportWidth = jScrollPane2.getWidth();
            if (viewportWidth < 100) {
                viewportWidth = selectProductPanel.getWidth() - 40;
            }
        }

        int cardWidth = calculateCardWidth(viewportWidth);
        int availableWidth = viewportWidth - 50;

        if (availableWidth >= (cardWidth * 2 + 15)) {
            columnsPerRow = 2;
        } else {
            columnsPerRow = 1;
        }
    }

    private void setupKeyboardNavigation() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        productSearchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleSearchBarKeyPress(e);
            }
        });

        jScrollPane2.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        jPanel7.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        cartPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        jScrollPane2.setFocusable(true);
        jPanel7.setFocusable(true);
        productSearchBar.setFocusable(true);
        cartPanel.setFocusable(true);
        setFocusable(true);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> requestFocusInWindow());
            }
        });
    }

    private void handleSearchBarKeyPress(KeyEvent e) {
        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_ESCAPE:
                clearSearch();
                requestFocusInWindow();
                e.consume();
                break;

            case KeyEvent.VK_ENTER:
                searchTimer.stop();
                performBarcodeSearch();
                e.consume();
                break;

            case KeyEvent.VK_DOWN:
                if (!productCards.isEmpty()) {
                    requestFocusInWindow();
                    selectCardByIndex(0);
                    e.consume();
                }
                break;

            case KeyEvent.VK_UP:
                e.consume();
                break;
        }
    }

    private void handleKeyPress(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // F1 - Help is handled globally
        if (keyCode == KeyEvent.VK_F1) {
            return;
        }

        // Don't interfere with search bar
        if (productSearchBar.hasFocus()) {
            return;
        }

        // Navigation shortcuts
        if (!productCards.isEmpty() && isInitialLoadComplete) {
            switch (keyCode) {
                case KeyEvent.VK_DOWN:
                    if (!e.isAltDown()) {
                        navigateDown();
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_UP:
                    if (!e.isAltDown()) {
                        navigateUp();
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_RIGHT:
                    navigateRight();
                    e.consume();
                    break;

                case KeyEvent.VK_LEFT:
                    navigateLeft();
                    e.consume();
                    break;

                case KeyEvent.VK_ENTER:
                    if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
                        addSelectedProductToCart();
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_HOME:
                    if (!productCards.isEmpty()) {
                        selectCardByIndex(0);
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_END:
                    if (!productCards.isEmpty()) {
                        selectCardByIndex(productCards.size() - 1);
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_PAGE_DOWN:
                    if (!productCards.isEmpty()) {
                        int newIndex = Math.min(currentCardIndex + 5, productCards.size() - 1);
                        selectCardByIndex(newIndex);
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_PAGE_UP:
                    if (!productCards.isEmpty()) {
                        int newIndex = Math.max(currentCardIndex - 5, 0);
                        selectCardByIndex(newIndex);
                        e.consume();
                    }
                    break;
                    
                case KeyEvent.VK_ESCAPE:
                    clearSearch();
                    clearCardSelection();
                    e.consume();
                    break;
            }
        }
    }

    private void navigateDown() {
        if (productCards.isEmpty()) {
            return;
        }

        int newIndex;
        if (currentCardIndex == -1) {
            newIndex = 0;
        } else {
            newIndex = currentCardIndex + columnsPerRow;
            if (newIndex >= productCards.size()) {
                newIndex = newIndex % columnsPerRow;
                if (newIndex >= productCards.size()) {
                    newIndex = 0;
                }
            }
        }

        selectCardByIndex(newIndex);
        ensureCardVisible(newIndex);
    }

    private void navigateUp() {
        if (productCards.isEmpty()) {
            return;
        }

        int newIndex;
        if (currentCardIndex == -1) {
            newIndex = productCards.size() - 1;
        } else {
            newIndex = currentCardIndex - columnsPerRow;
            if (newIndex < 0) {
                int column = currentCardIndex % columnsPerRow;
                newIndex = productCards.size() - columnsPerRow + column;
                if (newIndex >= productCards.size()) {
                    newIndex = productCards.size() - 1;
                }
                if (newIndex < 0) {
                    newIndex = productCards.size() - 1;
                }
            }
        }

        selectCardByIndex(newIndex);
        ensureCardVisible(newIndex);
    }

    private void navigateRight() {
        if (productCards.isEmpty()) {
            return;
        }

        int newIndex;
        if (currentCardIndex == -1) {
            newIndex = 0;
        } else {
            newIndex = currentCardIndex + 1;
            if (newIndex >= productCards.size()) {
                newIndex = 0;
            }
        }

        selectCardByIndex(newIndex);
        ensureCardVisible(newIndex);
    }

    private void navigateLeft() {
        if (productCards.isEmpty()) {
            return;
        }

        int newIndex;
        if (currentCardIndex == -1) {
            newIndex = productCards.size() - 1;
        } else {
            newIndex = currentCardIndex - 1;
            if (newIndex < 0) {
                newIndex = productCards.size() - 1;
            }
        }

        selectCardByIndex(newIndex);
        ensureCardVisible(newIndex);
    }

    private void selectCardByIndex(int index) {
        if (index < 0 || index >= productCards.size()) {
            return;
        }

        if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
            RoundedPanel previousCard = productCards.get(currentCardIndex);
            setCardSelection(previousCard, false);
        }

        currentCardIndex = index;
        RoundedPanel currentCard = productCards.get(currentCardIndex);
        setCardSelection(currentCard, true);

        if (!hasFocus() && !productSearchBar.hasFocus()) {
            requestFocusInWindow();
        }
    }

    private void setCardSelection(RoundedPanel card, boolean selected) {
        if (selected) {
            card.setBackground(new Color(200, 245, 245));
            card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(TEAL_COLOR, 3, 20),
                    BorderFactory.createEmptyBorder(16, 18, 16, 18)
            ));
        } else {
            if (hasSingleSearchResult && productCards.size() == 1) {
                card.setBackground(SINGLE_RESULT_HIGHLIGHT);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(new Color(100, 180, 255), 2, 20),
                        BorderFactory.createEmptyBorder(16, 18, 16, 18)
                ));
            } else {
                card.setBackground(CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(BORDER_COLOR, 1, 20),
                        BorderFactory.createEmptyBorder(16, 18, 16, 18)
                ));
            }
        }
        card.repaint();
    }

    private void clearCardSelection() {
        if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
            RoundedPanel currentCard = productCards.get(currentCardIndex);
            setCardSelection(currentCard, false);
        }
        currentCardIndex = -1;
    }

    private void ensureCardVisible(int index) {
        if (index < 0 || index >= productCards.size()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            RoundedPanel card = productCards.get(index);
            java.awt.Rectangle cardRect = card.getBounds();
            java.awt.Rectangle viewRect = jScrollPane2.getViewport().getViewRect();

            int cardTop = cardRect.y;
            int cardBottom = cardRect.y + cardRect.height;
            int viewTop = viewRect.y;
            int viewBottom = viewRect.y + viewRect.height;

            if (cardTop < viewTop) {
                int y = Math.max(0, cardTop - 20);
                jScrollPane2.getViewport().setViewPosition(new java.awt.Point(0, y));
            } else if (cardBottom > viewBottom) {
                int y = cardTop - viewRect.height + cardRect.height + 20;
                y = Math.max(0, Math.min(y, jPanel7.getHeight() - viewRect.height));
                jScrollPane2.getViewport().setViewPosition(new java.awt.Point(0, y));
            }
        });
    }

    private void updateProductCardSizes() {
        int viewportWidth = jScrollPane2.getViewport().getWidth();

        if (viewportWidth < 100) {
            viewportWidth = jScrollPane2.getWidth();
            if (viewportWidth < 100) {
                viewportWidth = selectProductPanel.getWidth() - 40;
            }
        }

        int cardWidth = calculateCardWidth(viewportWidth);

        boolean hasChanges = false;
        for (java.awt.Component comp : jPanel7.getComponents()) {
            if (comp instanceof RoundedPanel) {
                Dimension currentSize = comp.getPreferredSize();
                if (currentSize.width != cardWidth) {
                    Dimension newSize = new Dimension(cardWidth, 145);
                    comp.setPreferredSize(newSize);
                    comp.setMinimumSize(new Dimension(280, 145));
                    comp.setMaximumSize(new Dimension(cardWidth, 145));
                    hasChanges = true;
                }
            }
        }

        if (hasChanges) {
            jPanel7.revalidate();
            jPanel7.repaint();
        }
    }

    private int calculateCardWidth(int viewportWidth) {
        int gap = 15;
        int padding = 25;
        int availableWidth = viewportWidth - (padding * 2);

        int minCardWidth = 320;

        int columns;
        if (availableWidth >= (minCardWidth * 2 + gap)) {
            columns = 2;
        } else if (availableWidth >= minCardWidth) {
            columns = 1;
        } else {
            columns = 1;
            minCardWidth = Math.max(280, availableWidth);
        }

        int cardWidth;
        if (columns == 2) {
            cardWidth = (availableWidth - gap) / 2;
        } else {
            cardWidth = availableWidth;
        }

        return cardWidth;
    }

    private void loadProductInBackground(String productSearch) {
        if (isLoadingProducts) {
            return;
        }

        SwingWorker<List<PosPanelDTO>, Void> worker = new SwingWorker<List<PosPanelDTO>, Void>() {
            @Override
            protected List<PosPanelDTO> doInBackground() throws Exception {
                isLoadingProducts = true;
                return loadProductFromDatabase(productSearch);
            }

            @Override
            protected void done() {
                isLoadingProducts = false;
                try {
                    List<PosPanelDTO> products = get();
                    updateProductCards(products, productSearch != null && !productSearch.isEmpty());
                    isInitialLoadComplete = true;

                    SwingUtilities.invokeLater(() -> {
                        requestFocusInWindow();
                    });

                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        Notifications.getInstance().show(
                            Notifications.Type.ERROR,
                            Notifications.Location.TOP_RIGHT,
                            "Error loading products: " + e.getMessage()
                        );
                    });
                }
            }
        };
        worker.execute();
    }

    private List<PosPanelDTO> loadProductFromDatabase(String productSearch) {
        List<PosPanelDTO> products = new ArrayList<>();

        try {
            List<PosPanelDTO> allProducts = posPanelDAO.searchProducts(productSearch);
            
            for (PosPanelDTO dto : allProducts) {
                int availableStock = StockTracker.getInstance()
                    .getAvailableStock(dto.getProductId(), dto.getBatchNo());
                
                if (availableStock > 0) {
                    // Create new DTO with available stock
                    PosPanelDTO productWithAvailableStock = new PosPanelDTO(
                        dto.getProductId(),
                        dto.getProductName(),
                        dto.getBrandName(),
                        dto.getBatchNo(),
                        availableStock,
                        dto.getSellingPrice(),
                        dto.getBarcode(),
                        dto.getLastPrice()
                    );
                    products.add(productWithAvailableStock);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    private void updateProductCards(List<PosPanelDTO> products, boolean isSearchResult) {
        jPanel7.removeAll();
        productCards.clear();

        currentCardIndex = -1;

        hasSingleSearchResult = isSearchResult && products.size() == 1;

        if (products.isEmpty()) {
            JPanel messagePanel = new JPanel(new java.awt.GridBagLayout());
            messagePanel.setBackground(Color.WHITE);
            messagePanel.setPreferredSize(new Dimension(jScrollPane2.getViewport().getWidth(), 400));

            String message = !productSearchBar.getText().isEmpty()
                    && !productSearchBar.getForeground().equals(java.awt.Color.GRAY)
                    ? "No products found for: '" + productSearchBar.getText() + "'"
                    : "No products available";

            JLabel noProductLabel = new JLabel(message);
            noProductLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 18));
            noProductLabel.setForeground(TEXT_GRAY);

            messagePanel.add(noProductLabel);
            jPanel7.add(messagePanel);
        } else {
            for (PosPanelDTO product : products) {
                RoundedPanel productCard = createProductCard(
                        product.getProductId(), product.getProductName(), product.getBrandName(),
                        product.getBatchNo(), product.getQty(), product.getSellingPrice(),
                        product.getBarcode(), product.getLastPrice()
                );

                jPanel7.add(productCard);
                productCards.add(productCard);
            }

            if (hasSingleSearchResult && productCards.size() == 1) {
                RoundedPanel singleCard = productCards.get(0);
                singleCard.setBackground(SINGLE_RESULT_HIGHLIGHT);
                singleCard.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(new Color(100, 180, 255), 2, 20),
                        BorderFactory.createEmptyBorder(16, 18, 16, 18)
                ));
                singleCard.repaint();

                SwingUtilities.invokeLater(() -> {
                    selectCardByIndex(0);
                });
            }
        }

        jPanel7.revalidate();
        jPanel7.repaint();
        calculateColumnsPerRow();

        if (!productSearchBar.hasFocus()) {
            SwingUtilities.invokeLater(() -> {
                requestFocusInWindow();
            });
        }
    }

    private void performBarcodeSearch() {
        String searchText = productSearchBar.getText().trim();
        if (searchText.isEmpty() || productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
            return;
        }

        SwingWorker<PosPanelDTO, Void> worker = new SwingWorker<PosPanelDTO, Void>() {
            @Override
            protected PosPanelDTO doInBackground() throws Exception {
                return posPanelDAO.getProductByBarcode(searchText);
            }

            @Override
            protected void done() {
                try {
                    PosPanelDTO product = get();
                    if (product != null) {
                        int availableStock = StockTracker.getInstance()
                            .getAvailableStock(product.getProductId(), product.getBatchNo());
                        
                        if (availableStock > 0) {
                            addToCart(
                                product.getProductId(),
                                product.getProductName(),
                                product.getBrandName(),
                                product.getBatchNo(),
                                availableStock,
                                product.getSellingPrice(),
                                product.getBarcode(),
                                product.getLastPrice()
                            );
                            
                            Notifications.getInstance().show(
                                Notifications.Type.SUCCESS,
                                Notifications.Location.TOP_RIGHT,
                                product.getProductName() + " added to cart"
                            );
                            
                            productSearchBar.setText("");
                            productSearchBar.setForeground(java.awt.Color.GRAY);
                            productSearchBar.setText("Search products by name or barcode...");
                            
                        } else {
                            Notifications.getInstance().show(
                                Notifications.Type.ERROR,
                                Notifications.Location.TOP_RIGHT,
                                "Out of stock: " + product.getProductName()
                            );
                        }
                    } else {
                        Notifications.getInstance().show(
                            Notifications.Type.ERROR,
                            Notifications.Location.TOP_RIGHT,
                            "Product not found for barcode: " + searchText
                        );
                    }
                } catch (Exception e) {
                    Notifications.getInstance().show(
                        Notifications.Type.ERROR,
                        Notifications.Location.TOP_RIGHT,
                        "Error searching barcode: " + e.getMessage()
                    );
                }
            }
        };
        worker.execute();
    }

    private RoundedPanel createProductCard(int productId, String productName,
    String brandName, String batchNo, int initialStock, double sellingPrice,
    String barcode, double lastPrice) {

        int viewportWidth = jScrollPane2.getViewport().getWidth();

        if (viewportWidth < 100) {
            viewportWidth = jScrollPane2.getWidth();
            if (viewportWidth < 100) {
                viewportWidth = selectProductPanel.getWidth() - 40;
            }
        }

        int cardWidth = calculateCardWidth(viewportWidth);

        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD_BG);
        card.setPreferredSize(new Dimension(cardWidth, 145));
        card.setMinimumSize(new Dimension(280, 145));
        card.setMaximumSize(new Dimension(cardWidth, 145));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(BORDER_COLOR, 1, 20),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        card.setLayout(new java.awt.BorderLayout(0, 10));

        card.putClientProperty("productId", productId);
        card.putClientProperty("productName", productName);
        card.putClientProperty("brandName", brandName);
        card.putClientProperty("batchNo", batchNo);
        card.putClientProperty("sellingPrice", sellingPrice);
        card.putClientProperty("barcode", barcode);
        card.putClientProperty("lastPrice", lastPrice);

        JPanel topPanel = new JPanel(new java.awt.BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblProductName = new JLabel(productName);
        lblProductName.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        lblProductName.setForeground(new Color(40, 40, 40));
        topPanel.add(lblProductName, java.awt.BorderLayout.WEST);

        card.add(topPanel, java.awt.BorderLayout.NORTH);

        JPanel middlePanel = new JPanel(new java.awt.BorderLayout(12, 0));
        middlePanel.setOpaque(false);

        JLabel lblBrand = new JLabel("Brand: " + brandName);
        lblBrand.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        lblBrand.setForeground(TEXT_GRAY);
        middlePanel.add(lblBrand, java.awt.BorderLayout.WEST);

        JLabel lblPrice = new JLabel(String.format("Rs.%.2f", sellingPrice));
        lblPrice.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        lblPrice.setForeground(TEAL_COLOR);
        lblPrice.setHorizontalAlignment(JLabel.RIGHT);
        middlePanel.add(lblPrice, java.awt.BorderLayout.EAST);

        card.add(middlePanel, java.awt.BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        bottomPanel.setOpaque(false);

        int displayStock = StockTracker.getInstance().getAvailableStock(productId, batchNo);

        JPanel stockBadge = new JPanel();
        stockBadge.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        stockBadge.setOpaque(true);

        if (displayStock <= 0) {
            stockBadge.setBackground(new Color(255, 230, 230));
        } else if (displayStock < 10) {
            stockBadge.setBackground(new Color(255, 243, 224));
        } else {
            stockBadge.setBackground(new Color(230, 245, 230));
        }

        JLabel lblStock = new JLabel("Stock: " + displayStock);
        lblStock.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));

        if (displayStock <= 0) {
            lblStock.setForeground(new Color(220, 53, 69));
        } else if (displayStock < 10) {
            lblStock.setForeground(new Color(255, 152, 0));
        } else {
            lblStock.setForeground(new Color(34, 139, 34));
        }

        lblStock.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        stockBadge.add(lblStock);
        bottomPanel.add(stockBadge);

        ProductCardReference cardRef = new ProductCardReference(
                productId, batchNo, lblStock, stockBadge
        );
        StockTracker.getInstance().registerCard(productId, batchNo, cardRef);

        JPanel batchBadge = new JPanel();
        batchBadge.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        batchBadge.setOpaque(true);
        batchBadge.setBackground(new Color(240, 240, 250));

        JLabel lblBatch = new JLabel(batchNo);
        lblBatch.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        lblBatch.setForeground(new Color(70, 70, 100));
        lblBatch.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

        batchBadge.add(lblBatch);
        bottomPanel.add(batchBadge);

        card.add(bottomPanel, java.awt.BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            private Color originalBg = CARD_BG;

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER);
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(TEAL_COLOR, 2, 20),
                        BorderFactory.createEmptyBorder(16, 18, 16, 18)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hasSingleSearchResult && productCards.size() == 1) {
                    card.setBackground(SINGLE_RESULT_HIGHLIGHT);
                    card.setBorder(BorderFactory.createCompoundBorder(
                            new RoundBorder(new Color(100, 180, 255), 2, 20),
                            BorderFactory.createEmptyBorder(16, 18, 16, 18)
                    ));
                } else {
                    card.setBackground(originalBg);
                    card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    card.setBorder(BorderFactory.createCompoundBorder(
                            new RoundBorder(BORDER_COLOR, 1, 20),
                            BorderFactory.createEmptyBorder(16, 18, 16, 18)
                    ));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int currentAvailable = StockTracker.getInstance().getAvailableStock(productId, batchNo);

                if (currentAvailable > 0) {
                    addToCart(productId, productName, brandName, batchNo,
                            currentAvailable, sellingPrice, barcode, lastPrice);
                    
                    Color originalColor = card.getBackground();
                    card.setBackground(new Color(144, 238, 144));
                    
                    javax.swing.Timer flashTimer = new javax.swing.Timer(200, evt -> {
                        card.setBackground(originalColor);
                    });
                    flashTimer.setRepeats(false);
                    flashTimer.start();
                    
                } else {
                    Color originalColor = card.getBackground();
                    card.setBackground(new Color(255, 200, 200));
                    
                    javax.swing.Timer flashTimer = new javax.swing.Timer(200, evt -> {
                        card.setBackground(originalColor);
                    });
                    flashTimer.setRepeats(false);
                    flashTimer.start();
                    
                    JOptionPane.showMessageDialog(
                            PosPanelDone.this,
                            "No stock available!\n\n"
                            + "This product is either:\n"
                            + "• Out of stock in the database\n"
                            + "• All units are already in your cart\n"
                            + "• Units are on hold in other pending sales",
                            "Out of Stock",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        return card;
    }

    private void addSelectedProductToCart() {
        if (currentCardIndex < 0 || currentCardIndex >= productCards.size()) {
            return;
        }

        RoundedPanel selectedCard = productCards.get(currentCardIndex);

        int productId = (int) selectedCard.getClientProperty("productId");
        String productName = (String) selectedCard.getClientProperty("productName");
        String brandName = (String) selectedCard.getClientProperty("brandName");
        String batchNo = (String) selectedCard.getClientProperty("batchNo");
        double sellingPrice = (double) selectedCard.getClientProperty("sellingPrice");
        String barcode = (String) selectedCard.getClientProperty("barcode");
        double lastPrice = (double) selectedCard.getClientProperty("lastPrice");

        int currentAvailable = StockTracker.getInstance().getAvailableStock(productId, batchNo);

        if (currentAvailable > 0) {
            addToCart(productId, productName, brandName, batchNo, 
                    currentAvailable, sellingPrice, barcode, lastPrice);

            selectedCard.setBackground(new Color(144, 238, 144));

            javax.swing.Timer flashTimer = new javax.swing.Timer(200, evt -> {
                setCardSelection(selectedCard, false);
                currentCardIndex = -1;
            });
            flashTimer.setRepeats(false);
            flashTimer.start();

        } else {
            selectedCard.setBackground(new Color(255, 200, 200));

            javax.swing.Timer flashTimer = new javax.swing.Timer(200, evt -> {
                setCardSelection(selectedCard, true);
            });
            flashTimer.setRepeats(false);
            flashTimer.start();

            JOptionPane.showMessageDialog(
                    PosPanelDone.this,
                    "No stock available!\n\n"
                    + "This product is either:\n"
                    + "• Out of stock in the database\n"
                    + "• All units are already in your cart\n"
                    + "• Units are on hold in other pending sales",
                    "Out of Stock",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    private void addToCart(int productId, String productName, String brandName,
            String batchNo, int originalStock, double sellingPrice, String barcode, double lastPrice) {

        posCartPanel.addToCart(productId, productName, brandName, batchNo, originalStock, sellingPrice, barcode, lastPrice);
    }

    public void manualReload() {
        loadProductInBackground(productSearchBar.getForeground().equals(java.awt.Color.GRAY)
                ? "" : productSearchBar.getText().trim());
    }

    public void cleanup() {
        if (searchTimer != null && searchTimer.isRunning()) {
            searchTimer.stop();
        }
        if (cartUpdateTimer != null && cartUpdateTimer.isRunning()) {
            cartUpdateTimer.stop();
        }
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cartPanel = new lk.com.pos.privateclasses.RoundedPanel();
        selectProductPanel = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel2 = new javax.swing.JLabel();
        productSearchBar = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel7 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        productName = new javax.swing.JLabel();
        batchNo = new javax.swing.JLabel();
        qty = new javax.swing.JLabel();
        sellingPrice = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        javax.swing.GroupLayout cartPanelLayout = new javax.swing.GroupLayout(cartPanel);
        cartPanel.setLayout(cartPanelLayout);
        cartPanelLayout.setHorizontalGroup(
            cartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 491, Short.MAX_VALUE)
        );
        cartPanelLayout.setVerticalGroup(
            cartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        selectProductPanel.setBackground(new java.awt.Color(255, 255, 255));
        selectProductPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 22)); // NOI18N
        jLabel2.setText("Select Product");

        productSearchBar.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        productSearchBar.setText("Search By Product Name Or Barcode");
        productSearchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                productSearchBarFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                productSearchBarFocusLost(evt);
            }
        });

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        productName.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        productName.setText("Product Name");

        batchNo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        batchNo.setText("BATCH 1");
        batchNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        qty.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        qty.setText("Stock :99");
        qty.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        sellingPrice.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        sellingPrice.setText("Rs.1000");

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(productName)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(sellingPrice)
                        .addGap(18, 18, 18))
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(qty)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(batchNo)
                        .addGap(0, 89, Short.MAX_VALUE))))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(productName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sellingPrice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(qty)
                    .addComponent(batchNo))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel7.add(roundedPanel1);

        jScrollPane2.setViewportView(jPanel7);

        javax.swing.GroupLayout selectProductPanelLayout = new javax.swing.GroupLayout(selectProductPanel);
        selectProductPanel.setLayout(selectProductPanelLayout);
        selectProductPanelLayout.setHorizontalGroup(
            selectProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectProductPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(selectProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(selectProductPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(productSearchBar, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addGap(18, 18, 18))
        );
        selectProductPanelLayout.setVerticalGroup(
            selectProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectProductPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(selectProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void productSearchBarFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_productSearchBarFocusGained
        if (productSearchBar.getText().equals("Search By Product Name Or Barcode")) {
            productSearchBar.setText("");
            productSearchBar.setForeground(java.awt.Color.BLACK);
        }
    }//GEN-LAST:event_productSearchBarFocusGained

    private void productSearchBarFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_productSearchBarFocusLost
        if (productSearchBar.getText().isEmpty()) {
            productSearchBar.setText("Search By Product Name Or Barcode");
            productSearchBar.setForeground(java.awt.Color.GRAY);
        }
    }//GEN-LAST:event_productSearchBarFocusLost


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel batchNo;
    private lk.com.pos.privateclasses.RoundedPanel cartPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel productName;
    private javax.swing.JTextField productSearchBar;
    private javax.swing.JLabel qty;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private lk.com.pos.privateclasses.RoundedPanel selectProductPanel;
    private javax.swing.JLabel sellingPrice;
    // End of variables declaration//GEN-END:variables

}
