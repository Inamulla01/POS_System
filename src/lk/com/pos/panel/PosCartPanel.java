package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.Timer;
import lk.com.pos.connection.DB;
import lk.com.pos.connection.MySQL;

import lk.com.pos.dao.CartProductDAO;
import lk.com.pos.dao.CartStockDAO;
import lk.com.pos.dao.CartSaleDAO;
import lk.com.pos.dao.CartInvoiceDAO;
import lk.com.pos.dto.CartProductDTO;
import lk.com.pos.dto.CartStockDTO;
import lk.com.pos.dto.CartSaleDTO;
import lk.com.pos.dto.CartInvoiceDTO;
import lk.com.pos.dto.CartSaleItemDTO;
import lk.com.pos.dialog.AddCredit;
import lk.com.pos.dialog.AddCheque;
import lk.com.pos.dialog.AddCreditPay;
import lk.com.pos.dialog.CardPayDialog;
import lk.com.pos.dialog.DiscountDialog;
import lk.com.pos.dialog.ExchangeProductDialog;
import lk.com.pos.privateclasses.RoundedPanel;
import lk.com.pos.privateclasses.CartItem;
import lk.com.pos.privateclasses.CartListener;
import lk.com.pos.privateclasses.GradientButton;
import lk.com.pos.privateclasses.Invoice;
import lk.com.pos.session.Session;
import lk.com.pos.privateclasses.EnhancedConfirmDialog.*;

import lk.com.pos.privateclasses.StockTracker;
import lk.com.pos.privateclasses.Notification;
import lk.com.pos.utils.UserFriendlyMessages;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class PosCartPanel extends javax.swing.JPanel {

    private java.util.List<Invoice> recentInvoices = new ArrayList<>();
    private javax.swing.JDialog notificationDialog;
    private Invoice selectedInvoice;
    private boolean invoiceSelected = false;
    private java.util.List<JPanel> invoiceCardsList = new ArrayList<>();
    private int currentFocusedIndex = -1;

    private static final Color TEAL_COLOR = new Color(28, 181, 187);
    private static final Color TEXT_GRAY = new Color(102, 102, 102);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color ERROR_COLOR = new Color(255, 102, 102);
    private static final Color SUCCESS_COLOR = new Color(102, 204, 102);

    private List<RoundedPanel> cartItemPanels = new ArrayList<>();
    private int currentFocusedCartItemIndex = -1;

    private JLabel noProductsLabel;
    private CartListener cartListener;

    private boolean isAddingNewItem = false;

    private double appliedDiscountAmount = 0.0;
    private int appliedDiscountTypeId = -1;
    private double exchangeRefundAmount = 0.0;
    private int lastGeneratedSalesId = -1;

    // DAO instances
    private CartProductDAO cartProductDAO;
    private CartStockDAO cartStockDAO;
    private CartSaleDAO cartSaleDAO;
    private CartInvoiceDAO cartInvoiceDAO;

    public interface InvoiceSelectionListener {

        void onInvoiceSelected(Invoice invoice, String action);
    }

    private InvoiceSelectionListener invoiceSelectionListener;

    public void setInvoiceSelectionListener(InvoiceSelectionListener listener) {
        this.invoiceSelectionListener = listener;
    }

    public PosCartPanel() {
        initComponents();
        init();
    }

    private void init() {
        this.setBorder(null);
        this.setBackground(Color.WHITE);

        // Initialize DAOs
        cartProductDAO = new CartProductDAO();
        cartStockDAO = new CartStockDAO();
        cartSaleDAO = new CartSaleDAO();
        cartInvoiceDAO = new CartInvoiceDAO();

        FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 18, 18);
        deleteIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(220, 0, 0)));
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jPanel10.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane1.getVerticalScrollBar().setBlockIncrement(80);
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 10");

        jPanel1.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        jPanel3.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.Y_AXIS));
        jPanel10.setBackground(Color.WHITE);

        jPanel2.putClientProperty(FlatClientProperties.STYLE, "arc:20;");
        jPanel2.setBorder(BorderFactory.createEmptyBorder());
        jPanel10.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        clearCartBtn.addActionListener(evt -> clearCart());

        creditPay.addActionListener(evt -> creditPayActionPerformed(evt));
        discountBtn.addActionListener(evt -> discountBtnActionPerformed(evt));
        switchBtn.addActionListener(evt -> switchBtnActionPerformed(evt));
        exchangeBtn.addActionListener(evt -> exchangeBtnActionPerformed(evt));
        holdBtn.addActionListener(evt -> holdBtnActionPerformed(evt));
        gradientButton1.addActionListener(evt -> gradientButton1ActionPerformed(evt));

        jTextField2.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateBalance();
            }

            public void removeUpdate(DocumentEvent e) {
                updateBalance();
            }

            public void insertUpdate(DocumentEvent e) {
                updateBalance();
            }
        });

        ((AbstractDocument) jTextField2.getDocument()).setDocumentFilter(new NumericDocumentFilter());

        loadPaymentMethods();
        showNoProductsMessage();
        disablePaymentComboArrowKeys();

        SwingUtilities.invokeLater(() -> {
            setupKeyboardShortcuts();
            // Request initial focus
            requestFocusInWindow();
        });

        discountBtn.setToolTipText("Apply discount (F10 or Alt+D)");
        exchangeBtn.setToolTipText("Process exchange/return (Ctrl+E or Alt+E)");
        switchBtn.setToolTipText("Switch invoice (F12 or Alt+S)");

        jLabel24.setVisible(false);
        jLabel25.setVisible(false);
        jLabel25.setText("Rs.0.00");
        jLabel26.setVisible(false);
        jLabel27.setVisible(false);
        jLabel27.setText("Rs.0.00");

        paymentcombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAmountReceivedVisibility();
            }
        });

        final boolean[] isPlaceholder = {true};

        jTextField3.setText("Search by barcode or product name");
        jTextField3.setForeground(new Color(128, 128, 128));

        // FIXED: Improved focus handling for search bar
        jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (isPlaceholder[0]) {
                    jTextField3.setText("");
                    jTextField3.setForeground(Color.BLACK);
                    isPlaceholder[0] = false;
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String text = jTextField3.getText().trim();
                if (text.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        jTextField3.setText("Search by barcode or product name");
                        jTextField3.setForeground(new Color(128, 128, 128));
                        isPlaceholder[0] = true;
                        updateCartPanel();
                    });
                }
            }
        });

        // Improved search with better performance
        jTextField3.getDocument().addDocumentListener(new DocumentListener() {
            private Timer searchTimer = new Timer(300, e -> performSearch());

            {
                searchTimer.setRepeats(false);
            }

            private void triggerSearch() {
                if (!isPlaceholder[0]) {
                    searchTimer.restart();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                triggerSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                triggerSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                triggerSearch();
            }
        });

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!isPlaceholder[0]) {
                    String searchText = jTextField3.getText().trim();
                    if (!searchText.isEmpty()) {
                        searchAndAddProductByBarcode(searchText);
                        jTextField3.setText("");
                        isPlaceholder[0] = false;
                        updateCartPanel();
                    }
                }
            }
        });

        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    jTextField3.setText("");
                    jTextField3.setForeground(new Color(128, 128, 128));
                    jTextField3.setText("Search by barcode or product name");
                    currentFocusedCartItemIndex = -1;
                    updateCartPanel();
                    requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (!jTextField3.getForeground().equals(new Color(128, 128, 128))
                            && !jTextField3.getText().trim().isEmpty()) {
                        filterCartItems();
                        requestFocusInWindow();
                        evt.consume();
                    }
                }
            }
        });

        // FIXED: Add mouse listener to handle clicks outside search bar
        jPanel2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!jTextField3.getBounds().contains(e.getPoint())) {
                    requestFocusInWindow();
                }
            }
        });

        jPanel2.setBorder(BorderFactory.createEmptyBorder());
        javax.swing.JPanel searchWrapperPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        searchWrapperPanel.setBackground(Color.WHITE);
        searchWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        if (jTextField3.getParent() != null) {
            java.awt.Container parent = jTextField3.getParent();
            parent.remove(jTextField3);
            searchWrapperPanel.add(jTextField3, java.awt.BorderLayout.CENTER);

            java.awt.Component[] components = parent.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == jTextField3) {
                    parent.remove(i);
                    parent.add(searchWrapperPanel, i);
                    break;
                }
            }
        }

        // FIXED: Setup cash field focus handling
        setupCashFieldFocus();

        updateAmountReceivedVisibility();

        // FIXED: Make panels focusable and enable keyboard input
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        jPanel2.setFocusable(true);
        jPanel10.setFocusable(true);

        // FIXED: Add click listener to request focus
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });

        jPanel10.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void disablePaymentComboArrowKeys() {
        // Prevent arrow keys from changing payment method selection
        paymentcombo.setFocusable(false);

        // Add custom key listener to payment combo that ignores arrow keys
        paymentcombo.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int keyCode = e.getKeyCode();
                // Block arrow keys on payment combo
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN
                        || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
                    e.consume();
                }
            }
        });

        // Override the combo box input map to disable arrow key navigation
        javax.swing.InputMap im = paymentcombo.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
    }

    private void performSearch() {
        String searchText = jTextField3.getText().trim();
        if (searchText.isEmpty() || jTextField3.getForeground().equals(new Color(128, 128, 128))) {
            updateCartPanel();
        } else {
            filterCartItems();
        }
    }

    private void searchAndAddProductByBarcode(String barcode) {
        try {
            CartProductDTO product = cartProductDAO.getProductByBarcode(barcode);
            if (product == null) {
                JOptionPane.showMessageDialog(this,
                        "Product not found: " + barcode,
                        "Product Not Found",
                        JOptionPane.WARNING_MESSAGE);
                jTextField3.selectAll();
                return;
            }

            List<CartStockDTO> availableStocks = cartStockDAO.getAvailableStockByProduct(product.getProductId());
            if (availableStocks.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No available stock for: " + product.getProductName(),
                        "Out of Stock",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Take the first available stock
            CartStockDTO stock = availableStocks.get(0);

            addToCart(product.getProductId(), product.getProductName(), product.getBrandName(),
                    stock.getBatchNo(), stock.getQty(), stock.getSellingPrice(), product.getBarcode(), 0.0);

            java.awt.Toolkit.getDefaultToolkit().beep();
            jTextField3.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error searching product: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterCartItems() {
        String searchText = jTextField3.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            updateCartPanel();
            currentFocusedCartItemIndex = -1; // Clear focus when search is cleared
            return;
        }

        jPanel10.removeAll();
        if (cartItems.isEmpty()) {
            showNoProductsMessage();
            return;
        }

        jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(0, 18)));
        boolean foundItems = false;
        int firstMatchIndex = -1;
        int displayIndex = 0;

        List<CartItem> reversedItems = new ArrayList<>(cartItems.values());
        Collections.reverse(reversedItems);

        // Clear previous cart item panels
        cartItemPanels.clear();

        for (CartItem item : reversedItems) {
            String productName = item.getProductName().toLowerCase();
            String brandName = item.getBrandName().toLowerCase();
            String barcode = item.getBarcode() != null ? item.getBarcode().toLowerCase() : "";

            if (productName.contains(searchText) || brandName.contains(searchText) || barcode.contains(searchText)) {
                RoundedPanel cartCard = createCartItemPanel(item);
                jPanel10.add(cartCard);
                jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(18, 18)));
                cartItemPanels.add(cartCard);

                // Track first match
                if (firstMatchIndex == -1) {
                    firstMatchIndex = displayIndex;
                }

                foundItems = true;
                displayIndex++;
            }
        }

        if (!foundItems) {
            showNoSearchResults();
            currentFocusedCartItemIndex = -1;
        } else {
            // Automatically select the first matching item
            final int focusIndex = firstMatchIndex;
            SwingUtilities.invokeLater(() -> {
                if (focusIndex >= 0 && focusIndex < cartItemPanels.size()) {
                    setFocusedCartItem(focusIndex);
                }
            });
        }

        jPanel10.revalidate();
        jPanel10.repaint();
    }

    private void showNoSearchResults() {
        jPanel10.removeAll();
        JPanel messagePanel = new JPanel(new java.awt.GridBagLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setPreferredSize(new Dimension(jScrollPane1.getViewport().getWidth(), 300));

        JLabel noResultsLabel = new JLabel("No products found for: \"" + jTextField3.getText() + "\"");
        noResultsLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 16));
        noResultsLabel.setForeground(TEXT_GRAY);

        messagePanel.add(noResultsLabel);
        jPanel10.add(messagePanel);
        jPanel10.revalidate();
        jPanel10.repaint();
    }

    private void updateAmountReceivedVisibility() {
        String selectedPayment = (String) paymentcombo.getSelectedItem();
        boolean isCashPayment = selectedPayment != null && selectedPayment.toLowerCase().contains("cash");

        jTextField2.setVisible(isCashPayment);
        jLabel22.setVisible(isCashPayment);
        jLabel23.setVisible(isCashPayment);

        if (appliedDiscountAmount > 0) {
            jLabel24.setVisible(true);
            jLabel25.setVisible(true);
        }

        if (exchangeRefundAmount > 0) {
            jLabel26.setVisible(true);
            jLabel27.setVisible(true);
        }

        if (!isCashPayment) {
            jTextField2.setText("");
            jLabel23.setText("Rs.0.00");
            jLabel23.setForeground(Color.BLACK);
        }

        updateBalance();
    }

    private void loadPaymentMethods() {
        String[] paymentMethods = {
            "Select Payment Method",
            "Cash Payment",
            "Card Payment",
            "Credit Payment",
            "Cheque Payment"
        };

        paymentcombo.removeAllItems();
        for (String method : paymentMethods) {
            paymentcombo.addItem(method);
        }
    }

    private class NumericDocumentFilter extends DocumentFilter {

        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (isValidNumber(string)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (isValidNumber(text)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValidNumber(String text) {
            if (text == null || text.isEmpty()) {
                return true;
            }
            return text.matches("[0-9.]*");
        }
    }

    public void setCartListener(CartListener listener) {
        this.cartListener = listener;
    }

    private Map<String, CartItem> cartItems = new LinkedHashMap<>();

    public void addToCart(int productId, String productName, String brandName,
            String batchNo, int qty, double sellingPrice, String barcode, double lastPrice) {

        String cartKey = productId + "_" + batchNo;

        int availableStock = StockTracker.getInstance().getAvailableStock(productId, batchNo);
        int currentCartQty = StockTracker.getInstance().getCartQuantity(productId, batchNo);

        if (currentCartQty >= availableStock) {
            JOptionPane.showMessageDialog(this,
                    "Cannot add more. Available stock: " + availableStock,
                    "Stock Limit",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cartItems.containsKey(cartKey)) {
            // ✅ EXISTING ITEM - Update quantity AND focus on it
            CartItem item = cartItems.get(cartKey);
            if (currentCartQty < availableStock) {
                cartItems.remove(cartKey);
                item.setQuantity(item.getQuantity() + 1);
                cartItems.put(cartKey, item);
                isAddingNewItem = true; // ⚠️ FOCUS on this item
                updateCartPanel();
                StockTracker.getInstance().addToCart(productId, batchNo, 1);

            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot add more. Available stock: " + availableStock,
                        "Stock Limit",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            // ✅ NEW ITEM - Add and focus on it
            CartItem newItem = new CartItem(productId, productName, brandName,
                    batchNo, qty, sellingPrice, barcode, lastPrice);
            cartItems.put(cartKey, newItem);
            isAddingNewItem = true; // ⚠️ FOCUS on new item
            updateCartPanel();
            StockTracker.getInstance().addToCart(productId, batchNo, 1);

        }
    }

    private boolean validateAndAdjustLoadedItems() {
        boolean adjustmentsMade = false;
        List<String> itemsToRemove = new ArrayList<>();
        StringBuilder adjustmentMessage = new StringBuilder();

        for (Map.Entry<String, CartItem> entry : cartItems.entrySet()) {
            CartItem item = entry.getValue();
            int currentQty = item.getQuantity();
            int availableStock = item.getAvailableQty();

            if (currentQty > availableStock) {
                if (availableStock > 0) {
                    int difference = currentQty - availableStock;
                    item.setQuantity(availableStock);
                    StockTracker.getInstance().removeFromCart(
                            item.getProductId(),
                            item.getBatchNo(),
                            difference
                    );
                    adjustmentsMade = true;

                    adjustmentMessage.append(String.format(
                            "• %s: Adjusted from %d to %d units\n",
                            item.getProductName(), currentQty, availableStock));
                } else {
                    itemsToRemove.add(entry.getKey());
                    StockTracker.getInstance().removeFromCart(
                            item.getProductId(),
                            item.getBatchNo(),
                            currentQty
                    );
                    adjustmentsMade = true;

                    adjustmentMessage.append(String.format(
                            "• %s: Removed (no stock available)\n",
                            item.getProductName()));
                }
            }
        }

        for (String key : itemsToRemove) {
            cartItems.remove(key);
        }

        if (adjustmentsMade) {
            String message = "Stock levels have changed:\n\n" + adjustmentMessage.toString();
            JOptionPane.showMessageDialog(this,
                    message,
                    "Stock Adjustments",
                    JOptionPane.WARNING_MESSAGE);

            updateCartPanel();
            updateTotals();
        }

        return adjustmentsMade;
    }

    private void showNoProductsMessage() {
        jPanel10.removeAll();
        JPanel messagePanel = new JPanel(new java.awt.GridBagLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setPreferredSize(new Dimension(jScrollPane1.getViewport().getWidth(), 300));

        noProductsLabel = new JLabel("No products in cart");
        noProductsLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 16));
        noProductsLabel.setForeground(TEXT_GRAY);

        messagePanel.add(noProductsLabel);
        jPanel10.add(messagePanel);
        cartCount.setText("Cart (0)");
        jPanel10.revalidate();
        jPanel10.repaint();
        updateTotals();
    }

    private void updateCartPanel() {
        // Save focus state BEFORE clearing
        int savedFocusIndex = currentFocusedCartItemIndex;
        final String savedFocusedItemKey;

        if (savedFocusIndex >= 0 && savedFocusIndex < cartItemPanels.size()) {
            List<String> cartKeys = new ArrayList<>(cartItems.keySet());
            int actualIndex = cartKeys.size() - 1 - savedFocusIndex;
            savedFocusedItemKey = (actualIndex >= 0 && actualIndex < cartKeys.size())
                    ? cartKeys.get(actualIndex) : null;
        } else {
            savedFocusedItemKey = null;
        }

        // Clear efficiently
        for (RoundedPanel panel : cartItemPanels) {
            removeAllListeners(panel);
        }

        jPanel10.removeAll();
        cartItemPanels.clear();
        currentFocusedCartItemIndex = -1;

        if (cartItems.isEmpty()) {
            showNoProductsMessage();
            return;
        }

        jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(0, 18)));

        // Create cards in reverse order
        List<CartItem> reversedItems = new ArrayList<>(cartItems.values());
        Collections.reverse(reversedItems);

        for (CartItem item : reversedItems) {
            RoundedPanel cartCard = createCartItemPanel(item);
            jPanel10.add(cartCard);
            jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(18, 18)));
            cartItemPanels.add(cartCard);
        }

        cartCount.setText(String.format("Cart (%02d)", cartItems.size()));

        jPanel10.revalidate();
        jPanel10.repaint();
        updateTotals();

        // ✅ SMART FOCUS RESTORATION
        SwingUtilities.invokeLater(() -> restoreFocusSmart(savedFocusedItemKey, savedFocusIndex));
    }

    private void restoreFocusSmart(String savedFocusedItemKey, int savedFocusIndex) {
        // CASE 1: New item added via barcode scan → Focus on it
        if (isAddingNewItem) {
            if (!cartItemPanels.isEmpty()) {
                setFocusedCartItem(0); // Focus first item (newest)
                requestFocusInWindow();
            }
            isAddingNewItem = false;
            return;
        }

        // CASE 2: Existing item updated → Keep previous focus
        if (savedFocusedItemKey != null) {
            List<String> currentCartKeys = new ArrayList<>(cartItems.keySet());
            int keyIndex = currentCartKeys.indexOf(savedFocusedItemKey);

            if (keyIndex >= 0) {
                int displayIndex = currentCartKeys.size() - 1 - keyIndex;
                if (displayIndex >= 0 && displayIndex < cartItemPanels.size()) {
                    setFocusedCartItem(displayIndex); // Keep same item focused
                    requestFocusInWindow();
                    return;
                }
            }
        }

        // CASE 3: No previous focus or item removed → Don't auto-focus
        // This keeps the cart in "browsing mode" without forcing focus
        requestFocusInWindow();
    }

// Helper method for focus restoration
    private void restoreFocus(String savedFocusedItemKey, int savedFocusIndex) {
        if (isAddingNewItem) {
            if (!cartItemPanels.isEmpty()) {
                setFocusedCartItem(0);
                requestFocusInWindow();
            }
            isAddingNewItem = false;
            return;
        }

        if (savedFocusedItemKey != null) {
            List<String> currentCartKeys = new ArrayList<>(cartItems.keySet());
            int keyIndex = currentCartKeys.indexOf(savedFocusedItemKey);

            if (keyIndex >= 0) {
                int displayIndex = currentCartKeys.size() - 1 - keyIndex;
                if (displayIndex >= 0 && displayIndex < cartItemPanels.size()) {
                    setFocusedCartItem(displayIndex);
                    requestFocusInWindow();
                    return;
                }
            }
        }

        if (!cartItemPanels.isEmpty() && savedFocusIndex == -1) {
            setFocusedCartItem(0);
        }

        requestFocusInWindow();
    }

// Helper method to remove all listeners from a panel
    private void removeAllListeners(JPanel panel) {
        if (panel == null) {
            return;
        }

        // Remove mouse listeners
        for (MouseListener ml : panel.getMouseListeners()) {
            panel.removeMouseListener(ml);
        }

        // Recursively remove listeners from child components
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTextField) {
                JTextField field = (JTextField) comp;
                for (FocusListener fl : field.getFocusListeners()) {
                    field.removeFocusListener(fl);
                }
                for (KeyListener kl : field.getKeyListeners()) {
                    field.removeKeyListener(kl);
                }
            } else if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                for (ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
            } else if (comp instanceof JPanel) {
                removeAllListeners((JPanel) comp);
            }
        }
    }

    // Add this after your field declarations, before methods
    private static class RoundBorder extends javax.swing.border.AbstractBorder {

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
            g2d.setColor(color);
            g2d.setStroke(new java.awt.BasicStroke(thickness));
            int adjustment = thickness / 2;
            g2d.drawRoundRect(x + adjustment, y + adjustment, width - thickness, height - thickness, radius, radius);
            g2d.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(thickness + 1, thickness + 1, thickness + 1, thickness + 1);
        }
    }

    private void applyDiscountValue(JTextField discountField, CartItem item) {
        try {
            String text = discountField.getText().trim();
            if (text.isEmpty()) {
                item.setDiscountPrice(0);
                discountField.setText("0.00");
                discountField.setForeground(Color.BLACK);
                discountField.setToolTipText(null);
                updateCartPanelWithFocus();
                return;
            }

            double discountInput = Double.parseDouble(text);
            double unitPrice = item.getUnitPrice();
            double lastPrice = item.getLastPrice();
            double finalPricePerUnit = unitPrice - discountInput;

            if (discountInput < 0) {
                discountField.setText("0.00");
                item.setDiscountPrice(0);
                discountField.setForeground(Color.BLACK);
                discountField.setToolTipText(null);
                updateCartPanelWithFocus();

                JOptionPane.showMessageDialog(this,
                        "Discount cannot be negative",
                        "Invalid Discount",
                        JOptionPane.WARNING_MESSAGE);
            } else if (discountInput > unitPrice) {
                discountField.setText(String.format("%.2f", unitPrice));
                item.setDiscountPrice(unitPrice);
                discountField.setForeground(Color.BLACK);
                discountField.setToolTipText(null);
                updateCartPanelWithFocus();

                JOptionPane.showMessageDialog(this,
                        "Discount per unit cannot exceed Rs." + String.format("%.2f", unitPrice),
                        "Invalid Discount",
                        JOptionPane.WARNING_MESSAGE);
            } else if (finalPricePerUnit < lastPrice) {
                double maxAllowedDiscount = unitPrice - lastPrice;
                discountField.setText(String.format("%.2f", maxAllowedDiscount));
                item.setDiscountPrice(maxAllowedDiscount);
                discountField.setForeground(Color.BLACK);
                discountField.setToolTipText(null);
                updateCartPanelWithFocus();

                String message = String.format(
                        "Final price (Rs.%.2f) cannot be below last price (Rs.%.2f).\nMaximum allowed discount: Rs.%.2f",
                        finalPricePerUnit, lastPrice, maxAllowedDiscount
                );
                JOptionPane.showMessageDialog(this, message, "Invalid Discount", JOptionPane.WARNING_MESSAGE);
            } else {
                item.setDiscountPrice(discountInput);
                discountField.setText(String.format("%.2f", discountInput));
                discountField.setForeground(Color.BLACK);
                discountField.setToolTipText(null);
                updateCartPanelWithFocus();
            }
        } catch (NumberFormatException e) {
            discountField.setText(String.format("%.2f", item.getDiscountPrice()));
            discountField.setForeground(Color.BLACK);
            discountField.setToolTipText(null);
            updateCartPanelWithFocus();
        }
    }

// Helper method to update cart while preserving focus
    private void updateCartPanelWithFocus() {
        int currentFocus = currentFocusedCartItemIndex;
        updateCartPanel();
        if (currentFocus >= 0 && currentFocus < cartItemPanels.size()) {
            SwingUtilities.invokeLater(() -> setFocusedCartItem(currentFocus));
        }
    }

    private RoundedPanel createCartItemPanel(CartItem item) {
        // RoundBorder class is NOW a static inner class at top of file

        RoundedPanel card = new RoundedPanel(15);
        card.setBackground(CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setPreferredSize(new Dimension(400, 160));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(BORDER_COLOR, 1, 15),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        card.setLayout(new java.awt.BorderLayout(10, 10));

        // Store the final reference for use in mouse listener
        final RoundedPanel finalCard = card;

        // Add hover effects to the card
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (currentFocusedCartItemIndex != cartItemPanels.indexOf(finalCard)) {
                    finalCard.setBackground(new Color(245, 250, 250));
                    finalCard.setBorder(BorderFactory.createCompoundBorder(
                            new RoundBorder(TEAL_COLOR, 2, 15),
                            BorderFactory.createEmptyBorder(15, 10, 15, 10)
                    ));
                    finalCard.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (currentFocusedCartItemIndex != cartItemPanels.indexOf(finalCard)) {
                    finalCard.setBackground(CARD_BG);
                    finalCard.setBorder(BorderFactory.createCompoundBorder(
                            new RoundBorder(BORDER_COLOR, 1, 15),
                            BorderFactory.createEmptyBorder(15, 10, 15, 10)
                    ));
                    finalCard.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int index = cartItemPanels.indexOf(finalCard);
                if (index >= 0) {
                    setFocusedCartItem(index);
                }
            }
        });

        JPanel topPanel = new JPanel(new java.awt.BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblName = new JLabel(item.getProductName() + " (" + item.getBrandName() + ")");
        lblName.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        lblName.setForeground(new Color(40, 40, 40));
        topPanel.add(lblName, java.awt.BorderLayout.WEST);

        JButton deleteBtn = new JButton();
        FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 18, 18);
        deleteIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(220, 0, 0)));
        deleteBtn.setIcon(deleteIcon);
        deleteBtn.setBorder(BorderFactory.createEmptyBorder());
        deleteBtn.setBackground(CARD_BG);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> removeFromCart(item.getKey()));
        topPanel.add(deleteBtn, java.awt.BorderLayout.EAST);

        card.add(topPanel, java.awt.BorderLayout.NORTH);

        JPanel middlePanel = new JPanel(new java.awt.BorderLayout(15, 0));
        middlePanel.setOpaque(false);

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        qtyPanel.setOpaque(false);

        JButton minusBtn = new JButton("-");
        minusBtn.setFont(new Font("Nunito ExtraBold", Font.PLAIN, 16));
        minusBtn.setPreferredSize(new Dimension(40, 35));
        minusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JTextField qtyField = new JTextField(String.valueOf(item.getQuantity()));
        qtyField.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        qtyField.setHorizontalAlignment(JTextField.CENTER);
        qtyField.setPreferredSize(new Dimension(60, 35));
        qtyField.setToolTipText("Enter quantity (1-" + item.getAvailableQty() + ")");

        // TAG THE QUANTITY FIELD FOR EASY ACCESS LATER
        qtyField.putClientProperty("fieldType", "quantity");
        qtyField.putClientProperty("cartItem", item);
        qtyField.setName("quantityField_" + item.getKey());

        ((AbstractDocument) qtyField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                if (string.matches("[0-9]*")) {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + string + currentText.substring(offset);
                    if (newText.length() > 1 && newText.startsWith("0")) {
                        return;
                    }
                    if (newText.length() <= 6) {
                        super.insertString(fb, offset, string, attr);
                    }
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }
                if (text.matches("[0-9]*")) {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                    if (newText.length() > 1 && newText.startsWith("0")) {
                        return;
                    }
                    if (newText.length() <= 6) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            }
        });

        // SIMPLIFIED document listener - only visual validation
        qtyField.getDocument().addDocumentListener(new DocumentListener() {
            private void validateVisually() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String text = qtyField.getText().trim();
                        if (text.isEmpty()) {
                            qtyField.setForeground(ERROR_COLOR);
                            qtyField.setToolTipText("Quantity cannot be empty");
                            return;
                        }

                        int newQty = Integer.parseInt(text);
                        if (newQty <= 0) {
                            qtyField.setForeground(ERROR_COLOR);
                            qtyField.setToolTipText("Quantity must be at least 1");
                            return;
                        }
                        if (newQty > item.getAvailableQty()) {
                            qtyField.setForeground(ERROR_COLOR);
                            qtyField.setToolTipText("Exceeds available stock: " + item.getAvailableQty());
                            return;
                        }

                        qtyField.setForeground(SUCCESS_COLOR);
                        qtyField.setToolTipText("Valid quantity - Press Enter to apply");
                    } catch (NumberFormatException e) {
                        qtyField.setForeground(ERROR_COLOR);
                        qtyField.setToolTipText("Invalid number format");
                    }
                });
            }

            public void changedUpdate(DocumentEvent e) {
                validateVisually();
            }

            public void removeUpdate(DocumentEvent e) {
                validateVisually();
            }

            public void insertUpdate(DocumentEvent e) {
                validateVisually();
            }
        });

        // SIMPLIFIED focus listener
        qtyField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(() -> qtyField.selectAll());
                qtyField.setToolTipText("Enter quantity (1-" + item.getAvailableQty() + ") and press Enter");
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                applyQuantityChange(qtyField, item, false);
            }
        });

        qtyField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    applyQuantityChange(qtyField, item, true);
                    requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    qtyField.setText(String.valueOf(item.getQuantity()));
                    qtyField.setForeground(Color.BLACK);
                    qtyField.setToolTipText(null);
                    requestFocusInWindow();
                    evt.consume();
                }
            }
        });

        minusBtn.addActionListener(e -> {
            if (item.getQuantity() > 1) {
                // Use updateCartItemQuantity which handles StockTracker
                updateCartItemQuantity(item, item.getQuantity() - 1);
                qtyField.setText(String.valueOf(item.getQuantity()));
                qtyField.setForeground(Color.BLACK);
                qtyField.setToolTipText(null);

            } else {
                qtyField.setForeground(ERROR_COLOR);
                javax.swing.Timer timer = new javax.swing.Timer(500, evt -> qtyField.setForeground(Color.BLACK));
                timer.setRepeats(false);
                timer.start();
            }
        });

        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyField);

        JButton plusBtn = new JButton("+");
        plusBtn.setFont(new Font("Nunito ExtraBold", Font.PLAIN, 16));
        plusBtn.setPreferredSize(new Dimension(40, 35));
        plusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        plusBtn.addActionListener(e -> {
            if (item.getQuantity() < item.getAvailableQty()) {
                // Use updateCartItemQuantity which handles StockTracker
                updateCartItemQuantity(item, item.getQuantity() + 1);
                qtyField.setText(String.valueOf(item.getQuantity()));
                qtyField.setForeground(Color.BLACK);
                qtyField.setToolTipText(null);

            } else {
                qtyField.setForeground(ERROR_COLOR);
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(plusBtn),
                        "Cannot add more. Available stock: " + item.getAvailableQty(),
                        "Stock Limit", JOptionPane.WARNING_MESSAGE);
                qtyField.setForeground(Color.BLACK);
            }
        });

        qtyPanel.add(plusBtn);
        middlePanel.add(qtyPanel, java.awt.BorderLayout.WEST);

        JPanel discountContainer = new JPanel();
        discountContainer.setLayout(new javax.swing.BoxLayout(discountContainer, javax.swing.BoxLayout.Y_AXIS));
        discountContainer.setOpaque(false);

        JLabel discountLabel = new JLabel("Discount (per unit)");
        discountLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
        discountLabel.setForeground(TEXT_GRAY);
        discountLabel.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT);
        discountContainer.add(discountLabel);

        discountContainer.add(javax.swing.Box.createRigidArea(new Dimension(0, 3)));

        double perUnitDiscount = item.getDiscountPrice();
        JTextField discountField = new JTextField(String.format("%.2f", perUnitDiscount));
        discountField.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        discountField.setHorizontalAlignment(JTextField.RIGHT);
        discountField.setPreferredSize(new Dimension(110, 35));
        discountField.setMaximumSize(new Dimension(110, 35));
        discountField.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT);

        // TAG THE DISCOUNT FIELD FOR EASY ACCESS LATER
        discountField.putClientProperty("fieldType", "discount");
        discountField.putClientProperty("cartItem", item);
        discountField.setName("discountField_" + item.getKey());

        ((AbstractDocument) discountField.getDocument()).setDocumentFilter(new NumericDocumentFilter());

        // DocumentListener for real-time validation
        discountField.getDocument().addDocumentListener(new DocumentListener() {
            private void validateDiscount() {
                try {
                    String text = discountField.getText().trim();
                    if (text.isEmpty()) {
                        discountField.setForeground(Color.BLACK);
                        discountField.setToolTipText(null);
                        return;
                    }

                    double discountInput = Double.parseDouble(text);
                    double unitPrice = item.getUnitPrice();
                    double lastPrice = item.getLastPrice();
                    double finalPricePerUnit = unitPrice - discountInput;

                    if (discountInput < 0) {
                        discountField.setForeground(ERROR_COLOR);
                        discountField.setToolTipText("Discount cannot be negative");
                    } else if (discountInput > unitPrice) {
                        discountField.setForeground(ERROR_COLOR);
                        discountField.setToolTipText("Max discount per unit: Rs." + String.format("%.2f", unitPrice));
                    } else if (finalPricePerUnit < lastPrice) {
                        discountField.setForeground(ERROR_COLOR);
                        double maxAllowedDiscount = unitPrice - lastPrice;
                        discountField.setToolTipText(String.format("Cannot go below last price! Purchase price (Rs.%.2f) < Last price (Rs.%.2f). Max discount: Rs.%.2f",
                                finalPricePerUnit, lastPrice, maxAllowedDiscount));
                    } else {
                        discountField.setForeground(SUCCESS_COLOR);
                        double totalDiscount = discountInput * item.getQuantity();
                        discountField.setToolTipText(String.format("Total discount: Rs.%.2f (%.2f × %d)", totalDiscount, discountInput, item.getQuantity()));
                    }
                } catch (NumberFormatException e) {
                    discountField.setForeground(ERROR_COLOR);
                    discountField.setToolTipText("Invalid number");
                }
            }

            public void changedUpdate(DocumentEvent e) {
                validateDiscount();
            }

            public void removeUpdate(DocumentEvent e) {
                validateDiscount();
            }

            public void insertUpdate(DocumentEvent e) {
                validateDiscount();
            }
        });

        discountField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    applyDiscountValue(discountField, item);
                    evt.consume();
                } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    discountField.setText(String.format("%.2f", item.getDiscountPrice()));
                    discountField.setForeground(Color.BLACK);
                    discountField.setToolTipText(null);
                    evt.consume();
                }
            }
        });

        discountField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(() -> {
                    String currentText = discountField.getText().trim();
                    if (currentText.equals("0.00") || currentText.equals("0")) {
                        discountField.setText("");
                    } else {
                        discountField.selectAll();
                    }
                });
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                applyDiscountValue(discountField, item);
            }
        });

        discountContainer.add(discountField);
        middlePanel.add(discountContainer, java.awt.BorderLayout.EAST);
        card.add(middlePanel, java.awt.BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new java.awt.BorderLayout(10, 0));
        bottomPanel.setOpaque(false);

        JPanel calcPanel = new JPanel();
        calcPanel.setLayout(new javax.swing.BoxLayout(calcPanel, javax.swing.BoxLayout.Y_AXIS));
        calcPanel.setOpaque(false);

        JLabel basePriceLabel = new JLabel(String.format("Rs.%.2f × %d = Rs.%.2f",
                item.getUnitPrice(), item.getQuantity(), item.getUnitPrice() * item.getQuantity()));
        basePriceLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        basePriceLabel.setForeground(TEXT_GRAY);
        basePriceLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        calcPanel.add(basePriceLabel);

        if (item.getDiscountPrice() > 0) {
            double discountPerUnit = item.getDiscountPrice();
            double totalDiscount = discountPerUnit * item.getQuantity();
            JLabel discountInfo = new JLabel(String.format("Discount: Rs.%.2f × %d = -Rs.%.2f",
                    discountPerUnit, item.getQuantity(), totalDiscount));
            discountInfo.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
            discountInfo.setForeground(ERROR_COLOR);
            discountInfo.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            calcPanel.add(discountInfo);
        }

        bottomPanel.add(calcPanel, java.awt.BorderLayout.CENTER);

        JLabel lblPrice = new JLabel(String.format("Rs.%.2f", item.getTotalPrice()));
        lblPrice.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        lblPrice.setForeground(TEAL_COLOR);
        lblPrice.setHorizontalAlignment(JLabel.RIGHT);
        bottomPanel.add(lblPrice, java.awt.BorderLayout.EAST);

        card.add(bottomPanel, java.awt.BorderLayout.SOUTH);

        return card;
    }

    private void applyQuantityChange(JTextField qtyField, CartItem item, boolean showMessages) {
        String text = qtyField.getText().trim();

        try {
            if (text.isEmpty()) {
                if (showMessages) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(qtyField),
                            "Quantity cannot be empty. Reset to previous value.",
                            "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                }
                qtyField.setText(String.valueOf(item.getQuantity()));
                qtyField.setForeground(Color.BLACK);
                return;
            }

            int newQty = Integer.parseInt(text);

            if (newQty <= 0) {
                if (showMessages) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(qtyField),
                            "Quantity must be at least 1. Reset to previous value.",
                            "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                }
                qtyField.setText(String.valueOf(item.getQuantity()));
                qtyField.setForeground(Color.BLACK);
                return;
            }

            if (newQty > item.getAvailableQty()) {
                if (showMessages) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(qtyField),
                            String.format("Quantity exceeds available stock (%d). Using maximum available.",
                                    item.getAvailableQty()),
                            "Stock Limit", JOptionPane.WARNING_MESSAGE);
                }
                newQty = item.getAvailableQty();
            }

            // Use the new updateCartItemQuantity method which handles StockTracker
            if (newQty != item.getQuantity()) {
                updateCartItemQuantity(item, newQty);
            }

            qtyField.setText(String.valueOf(newQty));
            qtyField.setForeground(Color.BLACK);
            qtyField.setToolTipText(null);

        } catch (NumberFormatException e) {
            if (showMessages) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(qtyField),
                        "Invalid number format. Reset to previous value.",
                        "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            }
            qtyField.setText(String.valueOf(item.getQuantity()));
            qtyField.setForeground(Color.BLACK);
        }
    }

    private CartItem getCurrentFocusedItem() {
        if (currentFocusedCartItemIndex >= 0 && currentFocusedCartItemIndex < cartItemPanels.size()) {
            List<CartItem> itemsList = new ArrayList<>(cartItems.values());
            int actualIndex = cartItems.size() - 1 - currentFocusedCartItemIndex;
            if (actualIndex >= 0 && actualIndex < itemsList.size()) {
                return itemsList.get(actualIndex);
            }
        }
        return null;
    }

    private void removeFromCart(String cartKey) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove this item from cart?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            CartItem removedItem = cartItems.get(cartKey);
            if (removedItem != null) {
                // CRITICAL: Notify StockTracker BEFORE removing
                StockTracker.getInstance().removeFromCart(
                        removedItem.getProductId(),
                        removedItem.getBatchNo(),
                        removedItem.getQuantity()
                );
            }
            cartItems.remove(cartKey);
            updateCartPanel();
        }
    }

    void clearCart() {
        if (cartItems.isEmpty()) {
            return; // Cart already empty, do nothing
        }

        // IMPORTANT: Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the cart?",
                "Clear Cart",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Clear all items
            for (CartItem item : cartItems.values()) {
                StockTracker.getInstance().removeFromCart(
                        item.getProductId(),
                        item.getBatchNo(),
                        item.getQuantity()
                );
            }

            cartItems.clear();
            jTextField2.setText("");
            appliedDiscountAmount = 0.0;
            appliedDiscountTypeId = -1;
            exchangeRefundAmount = 0.0;
            updateCartPanel();
        }
        // If user clicks "No", nothing happens
    }

    private void updateBalance() {
        try {
            String amountText = jTextField2.getText().trim();
            double total = getTotal();

            if (!amountText.isEmpty()) {
                double amountReceived = Double.parseDouble(amountText);
                double balance = amountReceived - total;

                if (total < 0) {
                    jLabel23.setText(String.format("Refund Due: Rs.%.2f", Math.abs(total)));
                    jLabel23.setForeground(new Color(255, 102, 0));
                } else {
                    jLabel23.setText(String.format("Rs.%.2f", balance));
                    if (balance < 0) {
                        jLabel23.setForeground(ERROR_COLOR);
                    } else if (balance > 0) {
                        jLabel23.setForeground(SUCCESS_COLOR);
                    } else {
                        jLabel23.setForeground(Color.BLACK);
                    }
                }
            } else {
                if (total < 0) {
                    jLabel23.setText(String.format("Refund Due: Rs.%.2f", Math.abs(total)));
                    jLabel23.setForeground(new Color(255, 102, 0));
                } else {
                    jLabel23.setText("Rs.0.00");
                    jLabel23.setForeground(Color.BLACK);
                }
            }
        } catch (NumberFormatException e) {
            jLabel23.setText("Rs.0.00");
            jLabel23.setForeground(Color.BLACK);
        }
    }

    private void updateTotals() {
        double subtotal = 0;
        for (CartItem item : cartItems.values()) {
            subtotal += item.getTotalPrice();
        }

        double totalAfterDiscount = subtotal - appliedDiscountAmount;
        if (totalAfterDiscount < 0) {
            totalAfterDiscount = 0;
        }

        double finalTotal = totalAfterDiscount - exchangeRefundAmount;

        jLabel15.setText(String.format("Rs.%.2f", finalTotal));

        if (appliedDiscountAmount > 0) {
            jLabel25.setText(String.format("-Rs.%.2f", appliedDiscountAmount));
            jLabel25.setVisible(true);
            jLabel24.setVisible(true);
        } else {
            jLabel25.setText("Rs.0.00");
            jLabel25.setVisible(false);
            jLabel24.setVisible(false);
        }

        if (exchangeRefundAmount > 0) {
            jLabel27.setText(String.format("-Rs.%.2f", exchangeRefundAmount));
            jLabel27.setForeground(new Color(76, 175, 80));
            jLabel27.setVisible(true);
            jLabel26.setVisible(true);
        } else {
            jLabel27.setText("Rs.0.00");
            jLabel27.setVisible(false);
            jLabel26.setVisible(false);
        }

        updateBalance();
        if (cartListener != null) {
            cartListener.onCartUpdated(finalTotal, cartItems.size());
        }
    }

    public Map<String, CartItem> getCartItems() {
        return new HashMap<>(cartItems);
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getTotalPrice();
        }
        total -= appliedDiscountAmount;
        if (total < 0) {
            total = 0;
        }
        total -= exchangeRefundAmount;
        return total;
    }

    public boolean validateCheckout() {
        if (cartItems.isEmpty() && exchangeRefundAmount <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Cart is empty. Add products before checkout.",
                    "Empty Cart",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        double total = getTotal();

        if (total < 0) {
            int response = JOptionPane.showConfirmDialog(this,
                    String.format("This is a return-only transaction.\nRefund amount: Rs.%.2f\n\nProcess refund?",
                            Math.abs(total)),
                    "Refund Transaction",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            return response == JOptionPane.YES_OPTION;
        }

        if (selectedInvoice == null || !"Hold".equalsIgnoreCase(selectedInvoice.getStatus())) {
            String selectedPayment = (String) paymentcombo.getSelectedItem();
            if (selectedPayment == null || selectedPayment.equals("Select Payment Method")) {
                JOptionPane.showMessageDialog(this,
                        "Please select a payment method.",
                        "Missing Payment Method",
                        JOptionPane.WARNING_MESSAGE);
                paymentcombo.requestFocus();
                return false;
            }

            boolean isCashPayment = selectedPayment.toLowerCase().contains("cash");
            if (isCashPayment && total > 0) {
                try {
                    String amountText = jTextField2.getText().trim();
                    if (amountText.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Please enter amount received.",
                                "Missing Amount",
                                JOptionPane.WARNING_MESSAGE);
                        jTextField2.requestFocus();
                        return false;
                    }

                    double amountReceived = Double.parseDouble(amountText);
                    if (amountReceived < total) {
                        JOptionPane.showMessageDialog(this,
                                String.format("Insufficient amount. Need Rs.%.2f more.", total - amountReceived),
                                "Insufficient Payment",
                                JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid amount entered.",
                            "Invalid Amount",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    public void resetCart() {
        for (CartItem item : cartItems.values()) {
            StockTracker.getInstance().removeFromCart(
                    item.getProductId(),
                    item.getBatchNo(),
                    item.getQuantity()
            );
        }

        cartItems.clear();
        jTextField2.setText("");
        paymentcombo.setSelectedIndex(0);
        appliedDiscountAmount = 0.0;
        appliedDiscountTypeId = -1;
        exchangeRefundAmount = 0.0;

        if (jLabel25 != null) {
            jLabel25.setText("Rs.0.00");
            jLabel25.setVisible(false);
        }
        if (jLabel24 != null) {
            jLabel24.setVisible(false);
        }
        if (jLabel27 != null) {
            jLabel27.setText("Rs.0.00");
            jLabel27.setVisible(false);
        }
        if (jLabel26 != null) {
            jLabel26.setVisible(false);
        }

        updateCartPanel();
        StockTracker.getInstance().clearCart();
    }

    public void openCreditPayment() {
        creditPayActionPerformed(null);
    }

    public void selectCashPayment() {
        if (paymentcombo.getItemCount() > 1) {
            paymentcombo.setSelectedIndex(1);
            SwingUtilities.invokeLater(() -> {
                if (jTextField2.isVisible()) {
                    jTextField2.requestFocusInWindow();
                    jTextField2.selectAll();
                }
            });
        }
    }

    public void selectCardPayment() {
        if (paymentcombo.getItemCount() > 2) {
            paymentcombo.setSelectedIndex(2);
        }
    }

    public void selectCreditPayment() {
        if (paymentcombo.getItemCount() > 3) {
            paymentcombo.setSelectedIndex(3);
        }
    }

    public void selectChequePayment() {
        if (paymentcombo.getItemCount() > 4) {
            paymentcombo.setSelectedIndex(4);
        }
    }

    public void deleteFocusedCartItem() {
        if (currentFocusedCartItemIndex >= 0 && currentFocusedCartItemIndex < cartItemPanels.size()) {
            // Get the list of cart keys
            List<String> cartKeys = new ArrayList<>(cartItems.keySet());

            // Since items are displayed in reverse order (newest first),
            // we need to convert the display index to the actual map index
            int displayIndex = currentFocusedCartItemIndex;
            int actualIndex = cartKeys.size() - 1 - displayIndex;

            if (actualIndex >= 0 && actualIndex < cartKeys.size()) {
                String cartKey = cartKeys.get(actualIndex);

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Remove this item from cart?",
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    CartItem removedItem = cartItems.get(cartKey);
                    if (removedItem != null) {
                        // CRITICAL: Notify StockTracker BEFORE removing
                        StockTracker.getInstance().removeFromCart(
                                removedItem.getProductId(),
                                removedItem.getBatchNo(),
                                removedItem.getQuantity()
                        );
                    }
                    cartItems.remove(cartKey);

                    // Update the cart panel
                    updateCartPanel();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No cart item is selected. Use Alt+Up/Down to navigate to an item first.",
                    "No Item Selected",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void increaseFocusedItemQuantity() {
        if (currentFocusedCartItemIndex >= 0 && currentFocusedCartItemIndex < cartItemPanels.size()) {
            List<CartItem> itemsList = new ArrayList<>(cartItems.values());
            int actualIndex = cartItems.size() - 1 - currentFocusedCartItemIndex;
            if (actualIndex >= 0 && actualIndex < itemsList.size()) {
                CartItem item = itemsList.get(actualIndex);
                if (item.getQuantity() < item.getAvailableQty()) {
                    item.setQuantity(item.getQuantity() + 1);
                    StockTracker.getInstance().addToCart(item.getProductId(), item.getBatchNo(), 1);
                    updateCartPanelWithFocus();
                } else {
                    JOptionPane.showMessageDialog(PosCartPanel.this,
                            "Cannot add more. Available stock: " + item.getAvailableQty(),
                            "Stock Limit",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    public void decreaseFocusedItemQuantity() {
        if (currentFocusedCartItemIndex >= 0 && currentFocusedCartItemIndex < cartItemPanels.size()) {
            List<CartItem> itemsList = new ArrayList<>(cartItems.values());
            int actualIndex = cartItems.size() - 1 - currentFocusedCartItemIndex;
            if (actualIndex >= 0 && actualIndex < itemsList.size()) {
                CartItem item = itemsList.get(actualIndex);
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    StockTracker.getInstance().removeFromCart(item.getProductId(), item.getBatchNo(), 1);
                    updateCartPanelWithFocus();
                } else {
                    RoundedPanel currentPanel = cartItemPanels.get(currentFocusedCartItemIndex);
                    Color originalColor = currentPanel.getBackground();
                    currentPanel.setBackground(new Color(255, 200, 200));
                    Timer timer = new Timer(300, e -> {
                        currentPanel.setBackground(originalColor);
                        currentPanel.repaint();
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        }
    }

    private void setupKeyboardShortcuts() {
        SwingUtilities.invokeLater(() -> {
            // Clear existing shortcuts
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
            getActionMap().clear();

            // Make sure the panel can receive focus
            setFocusable(true);
            requestFocusInWindow();

            // ========================================
            // ITEM DISCOUNT SHORTCUTS (FIXED)
            // ========================================
            // Alt + R - Edit discount of FOCUSED cart item
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK), "ItemDiscountAltR");
            getActionMap().put("ItemDiscountAltR", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    focusSelectedItemDiscount();
                }
            });

            // Ctrl + D - Also edit discount of focused cart item (alternative)
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK), "ItemDiscountCtrlD");
            getActionMap().put("ItemDiscountCtrlD", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    focusSelectedItemDiscount();
                }
            });

            // Alt + D - Apply discount to entire cart (global discount)
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_DOWN_MASK), "GlobalDiscountAltD");
            getActionMap().put("GlobalDiscountAltD", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    discountBtnActionPerformed(null);
                }
            });

            // Shift + D - Quick zero discount for focused item
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.SHIFT_DOWN_MASK), "ZeroDiscountShiftD");
            getActionMap().put("ZeroDiscountShiftD", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    quickZeroDiscount();
                }
            });

            // ========================================
            // CART NAVIGATION (FIXED - NO LOOPING)
            // ========================================
            // Alt + Up - Previous cart item (NO LOOPING)
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK), "CartPrevAltUp");
            getActionMap().put("CartPrevAltUp", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (hasCartItems()) {
                        navigateCartItems(-1);
                    }
                }
            });

            // Alt + Down - Next cart item (NO LOOPING)
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK), "CartNextAltDown");
            getActionMap().put("CartNextAltDown", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (hasCartItems()) {
                        navigateCartItems(1);
                    }
                }
            });

            // Alt + Q - Focus Quantity Field
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.ALT_DOWN_MASK), "FocusQuantityAltQ");
            getActionMap().put("FocusQuantityAltQ", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    focusSelectedItemQuantity();
                }
            });

            // ========================================
            // OTHER CART SHORTCUTS
            // ========================================
            // Alt + X - Delete focused item
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_DOWN_MASK), "DeleteItemAltX");
            getActionMap().put("DeleteItemAltX", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    deleteFocusedCartItem();
                }
            });

            // ========================================
            // DELETE KEY - ALWAYS CLEAR ALL CART ITEMS
            // ========================================
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DeleteAction");
            getActionMap().put("DeleteAction", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // ALWAYS clear entire cart (ignore any focused item)
                    clearCart();
                }
            });

            // + / - keys for quantity (only when cart item focused)
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "IncreaseQty");
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "IncreaseQtyNum");
            getActionMap().put("IncreaseQty", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (currentFocusedCartItemIndex >= 0) {
                        increaseFocusedItemQuantity();
                    }
                }
            });
            getActionMap().put("IncreaseQtyNum", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (currentFocusedCartItemIndex >= 0) {
                        increaseFocusedItemQuantity();
                    }
                }
            });

            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "DecreaseQty");
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "DecreaseQtyNum");
            getActionMap().put("DecreaseQty", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (currentFocusedCartItemIndex >= 0) {
                        decreaseFocusedItemQuantity();
                    }
                }
            });
            getActionMap().put("DecreaseQtyNum", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (currentFocusedCartItemIndex >= 0) {
                        decreaseFocusedItemQuantity();
                    }
                }
            });

            // ========================================
            // PAYMENT METHOD SHORTCUTS
            // ========================================
            // F4 - Cash Payment
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "CashPaymentF4");
            getActionMap().put("CashPaymentF4", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectCashPayment();
                }
            });

            // F5 - Card Payment
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "CardPaymentF5");
            getActionMap().put("CardPaymentF5", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectCardPayment();
                }
            });

            // F6 - Credit Payment
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "CreditPaymentF6");
            getActionMap().put("CreditPaymentF6", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectCreditPayment();
                }
            });

            // F7 - Cheque Payment
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "ChequePaymentF7");
            getActionMap().put("ChequePaymentF7", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectChequePayment();
                }
            });

            // ========================================
            // OTHER TRANSACTION SHORTCUTS
            // ========================================
            // Ctrl + F - Focus Cart Search
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "FocusCartSearchCtrlF");
            getActionMap().put("FocusCartSearchCtrlF", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    focusCartSearch();
                }
            });

            // Alt + Enter - Complete Sale
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), "CompleteSaleAltEnter");
            getActionMap().put("CompleteSaleAltEnter", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gradientButton1ActionPerformed(null);
                }
            });

            // Alt + P - Credit Payment
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_DOWN_MASK), "CreditPayAltP");
            getActionMap().put("CreditPayAltP", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    creditPayActionPerformed(null);
                }
            });

            // Alt + S - Switch Invoice
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK), "SwitchAltS");
            getActionMap().put("SwitchAltS", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    switchBtnActionPerformed(e);
                }
            });

            // Alt + E - Exchange
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_DOWN_MASK), "ExchangeAltE");
            getActionMap().put("ExchangeAltE", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    exchangeBtnActionPerformed(null);
                }
            });

            // Alt + H - Hold Bill
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK), "HoldAltH");
            getActionMap().put("HoldAltH", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    holdBtnActionPerformed(null);
                }
            });

            // F9 - Complete Sale
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "CompleteSaleF9");
            getActionMap().put("CompleteSaleF9", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gradientButton1ActionPerformed(null);
                }
            });

            // F10 - Discount
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "DiscountF10");
            getActionMap().put("DiscountF10", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    discountBtnActionPerformed(null);
                }
            });

            // F11 - Hold
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "HoldF11");
            getActionMap().put("HoldF11", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    holdBtnActionPerformed(null);
                }
            });

            // F12 - Switch
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "SwitchF12");
            getActionMap().put("SwitchF12", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    switchBtnActionPerformed(null);
                }
            });

            // Escape - Clear focus or cancel
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EscapeAction");
            getActionMap().put("EscapeAction", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // Clear focus from any text field
                    requestFocusInWindow();
                    // If a cart item is focused, clear its focus
                    if (currentFocusedCartItemIndex >= 0) {
                        setFocusedCartItem(-1);
                    }
                }
            });

            setEnhancedComponentTooltips();

            // Ensure focus is on the panel
            SwingUtilities.invokeLater(() -> {
                requestFocusInWindow();
            });
        });
    }

    private void quickZeroDiscount() {
        if (currentFocusedCartItemIndex < 0 || currentFocusedCartItemIndex >= cartItemPanels.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a cart item first using Alt+Up/Down",
                    "No Item Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        RoundedPanel focusedPanel = cartItemPanels.get(currentFocusedCartItemIndex);
        JTextField discountField = findFieldByType(focusedPanel, "discount");

        if (discountField != null) {
            CartItem item = (CartItem) discountField.getClientProperty("cartItem");
            if (item != null) {
                item.setDiscountPrice(0.0);
                discountField.setText("0.00");
                discountField.setForeground(Color.BLACK);
                updateCartPanel();

                JOptionPane.showMessageDialog(this,
                        "Discount removed from " + item.getProductName(),
                        "Discount Removed",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void setupCashFieldFocus() {
        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(() -> jTextField2.selectAll());
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateBalance();
            }
        });

        // Add click listener to parent to remove focus from cash field
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!jTextField2.getBounds().contains(e.getPoint())) {
                    requestFocusInWindow();
                }
            }
        });
    }

    private int showConfirmDialogWithShortcuts(String message, String title, int optionType) {
        JOptionPane optionPane = new JOptionPane(
                message,
                JOptionPane.QUESTION_MESSAGE,
                optionType
        );

        JDialog dialog = optionPane.createDialog(this, title);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Add keyboard shortcuts directly to dialog and root pane
        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!dialog.isVisible()) {
                    return;
                }

                int keyCode = e.getKeyCode();

                switch (keyCode) {
                    case KeyEvent.VK_Y:
                        optionPane.setValue(JOptionPane.YES_OPTION);
                        break;
                    case KeyEvent.VK_N:
                        optionPane.setValue(JOptionPane.NO_OPTION);
                        break;
                    case KeyEvent.VK_ESCAPE:
                        optionPane.setValue(JOptionPane.CLOSED_OPTION);
                        break;
                    case KeyEvent.VK_ENTER:
                        optionPane.setValue(JOptionPane.YES_OPTION);
                        break;
                    default:
                        return;
                }
                e.consume();
            }
        };

        dialog.addKeyListener(keyListener);
        dialog.getRootPane().addKeyListener(keyListener);
        dialog.getContentPane().addKeyListener(keyListener);

        dialog.setFocusable(true);
        dialog.getRootPane().setFocusable(true);
        dialog.getContentPane().setFocusable(true);

        addButtonMnemonics(optionPane);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }

            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.getRootPane().requestFocusInWindow();
                });
            }
        });

        optionPane.addPropertyChangeListener(evt -> {
            if (dialog.isVisible()
                    && JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName())
                    && evt.getNewValue() != null
                    && evt.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);

        SwingUtilities.invokeLater(() -> requestFocusInWindow());

        Object value = optionPane.getValue();
        if (value == null || value.equals(JOptionPane.UNINITIALIZED_VALUE)) {
            return JOptionPane.CLOSED_OPTION;
        }
        return (value instanceof Integer) ? (Integer) value : JOptionPane.CLOSED_OPTION;
    }

    private void addButtonMnemonics(JOptionPane optionPane) {
        for (Component comp : optionPane.getComponents()) {
            if (comp instanceof Container) {
                addMnemonicsToButtons((Container) comp);
            }
        }
    }

    private void addMnemonicsToButtons(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String text = button.getText();

                if (text != null) {
                    if (text.equalsIgnoreCase("Yes")) {
                        button.setMnemonic(KeyEvent.VK_Y);
                        button.setDisplayedMnemonicIndex(0);
                        button.setToolTipText("Press Y or Alt+Y for Yes");
                    } else if (text.equalsIgnoreCase("No")) {
                        button.setMnemonic(KeyEvent.VK_N);
                        button.setDisplayedMnemonicIndex(0);
                        button.setToolTipText("Press N or Alt+N for No");
                    }
                }

            } else if (comp instanceof Container) {
                addMnemonicsToButtons((Container) comp);
            }
        }
    }

    private void addButtonShortcuts(JOptionPane optionPane, JDialog dialog) {
        for (Component comp : optionPane.getComponents()) {
            if (comp instanceof Container) {
                findAndFixButtons((Container) comp, dialog);
            }
        }
    }

    private void findAndFixButtons(Container container, JDialog dialog) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String text = button.getText();

                if (text != null) {
                    if (text.equalsIgnoreCase("Yes")) {
                        button.setMnemonic(KeyEvent.VK_Y);
                        button.setToolTipText("<html><b>Y</b> - Yes</html>");
                    } else if (text.equalsIgnoreCase("No")) {
                        button.setMnemonic(KeyEvent.VK_N);
                        button.setToolTipText("<html><b>N</b> - No</html>");
                    }
                }

                button.setFocusable(true);

            } else if (comp instanceof Container) {
                findAndFixButtons((Container) comp, dialog);
            }
        }
    }

    public void focusSelectedItemQuantity() {
        if (currentFocusedCartItemIndex >= 0 && currentFocusedCartItemIndex < cartItemPanels.size()) {
            RoundedPanel focusedPanel = cartItemPanels.get(currentFocusedCartItemIndex);
            JTextField qtyField = findFieldByType(focusedPanel, "quantity");
            if (qtyField != null) {
                qtyField.requestFocusInWindow();
                qtyField.selectAll();

                CartItem item = (CartItem) qtyField.getClientProperty("cartItem");
                if (item != null) {
                    qtyField.setToolTipText("Edit quantity for: " + item.getProductName());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a cart item first using Alt+Up/Down",
                    "No Item Selected",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void focusSelectedItemDiscount() {
        if (currentFocusedCartItemIndex >= 0 && currentFocusedCartItemIndex < cartItemPanels.size()) {
            RoundedPanel focusedPanel = cartItemPanels.get(currentFocusedCartItemIndex);
            JTextField discountField = findFieldByType(focusedPanel, "discount");
            if (discountField != null) {
                CartItem item = (CartItem) discountField.getClientProperty("cartItem");
                discountField.requestFocusInWindow();
                discountField.selectAll();

                if (item != null) {
                    discountField.setToolTipText("Edit discount for: " + item.getProductName());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a cart item first using Alt+Up/Down",
                    "No Item Selected",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private JTextField findFieldByType(Container container, String fieldType) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                if (fieldType.equals(textField.getClientProperty("fieldType"))) {
                    return textField;
                }
            }
            if (comp instanceof Container) {
                JTextField found = findFieldByType((Container) comp, fieldType);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public void navigateCartItems(int direction) {
        if (cartItemPanels.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No cart items available to navigate.",
                    "Empty Cart",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int newIndex;
        if (currentFocusedCartItemIndex == -1) {
            // No item currently focused, start at first item
            newIndex = (direction > 0) ? 0 : cartItemPanels.size() - 1;
        } else {
            newIndex = currentFocusedCartItemIndex + direction;
            // NO LOOPING - stop at the ends
            if (newIndex < 0) {
                newIndex = 0;
                return;
            }
            if (newIndex >= cartItemPanels.size()) {
                newIndex = cartItemPanels.size() - 1;
                return;
            }
        }

        setFocusedCartItem(newIndex);
    }

    private void setEnhancedComponentTooltips() {
        discountBtn.setToolTipText("<html>Apply discount to entire cart<br><b>Shortcut: F10 or Alt + D</b></html>");
        exchangeBtn.setToolTipText("<html>Process exchange/return<br><b>Shortcut: Ctrl+E or Alt + E</b></html>");
        switchBtn.setToolTipText("<html>Switch invoice<br><b>Shortcut: F12 or Alt + S</b></html>");
        holdBtn.setToolTipText("<html>Hold bill<br><b>Shortcut: F11 or Alt + H</b></html>");
        clearCartBtn.setToolTipText("<html>Clear all items<br><b>Shortcut: Delete</b></html>");
        gradientButton1.setToolTipText("<html>Complete sale<br><b>Shortcut: F9 or Alt + Enter</b></html>");
        creditPay.setToolTipText("<html>Credit Payment<br><b>Shortcut: F6 or Alt + P</b></html>");

        paymentcombo.setToolTipText("<html>Select payment method<br>"
                + "<b>Shortcuts:</b><br>"
                + "F4 - Cash Payment<br>"
                + "F5 - Card Payment<br>"
                + "F6 - Credit Payment<br>"
                + "F7 - Cheque Payment</html>");

        if (jTextField3 != null) {
            jTextField3.setToolTipText("<html>Search cart items<br><b>Shortcut: Ctrl+F</b></html>");
        }

        if (jTextField2 != null) {
            jTextField2.setToolTipText("<html>Enter amount received<br><i>Auto-focused when Cash Payment selected with F4</i></html>");
        }

        if (cartCount != null) {
            cartCount.setToolTipText("<html><b>Cart Navigation Shortcuts:</b><br>"
                    + "<b>Alt+↑/↓</b> - Navigate cart items<br>"
                    + "<b>Alt+X</b> - Delete focused item<br>"
                    + "<b>Alt+Q</b> - Edit quantity of focused item<br>"
                    + "<b>Alt+R or Ctrl+D</b> - Edit discount of focused item<br>"
                    + "<b>Shift+D</b> - Remove discount from focused item<br>"
                    + "<b>+/-</b> - Increase/decrease quantity (when item focused)<br>"
                    + "<b>Ctrl+F</b> - Search cart<br>"
                    + "<b>ESC</b> - Clear focus</html>");
        }
    }

    public void requestCartFocus() {
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
            if (!cartItemPanels.isEmpty() && currentFocusedCartItemIndex == -1) {
                setFocusedCartItem(0);
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
        });
    }

    public boolean hasCartItems() {
        return !cartItems.isEmpty();
    }

    public int getFocusedCartItemIndex() {
        return currentFocusedCartItemIndex;
    }

    public int getCartItemsCount() {
        return cartItems.size();
    }

    private void focusCartSearch() {
        jTextField3.requestFocusInWindow();
        if (jTextField3.getForeground().equals(new Color(128, 128, 128))) {
            jTextField3.setText("");
            jTextField3.setForeground(Color.BLACK);
        }
        jTextField3.selectAll();
    }

    private void setFocusedCartItem(int index) {
        if (index < 0) {
            // Clear all focus
            if (currentFocusedCartItemIndex >= 0 && currentFocusedCartItemIndex < cartItemPanels.size()) {
                RoundedPanel prevPanel = cartItemPanels.get(currentFocusedCartItemIndex);
                resetCartItemAppearance(prevPanel);
            }
            currentFocusedCartItemIndex = -1;
            return;
        }

        if (index >= cartItemPanels.size()) {
            return;
        }

        // Remove previous focus
        if (currentFocusedCartItemIndex >= 0 && currentFocusedCartItemIndex < cartItemPanels.size()) {
            RoundedPanel prevPanel = cartItemPanels.get(currentFocusedCartItemIndex);
            resetCartItemAppearance(prevPanel);
        }

        // Set new focus
        currentFocusedCartItemIndex = index;
        RoundedPanel currentPanel = cartItemPanels.get(currentFocusedCartItemIndex);
        applyFocusedCartItemAppearance(currentPanel);

        // Ensure visible in scroll pane
        SwingUtilities.invokeLater(() -> {
            java.awt.Rectangle visibleRect = currentPanel.getBounds();
            visibleRect.height += 20;
            jPanel10.scrollRectToVisible(visibleRect);

            // Ensure panel has focus for keyboard shortcuts
            requestFocusInWindow();
        });
    }

    private void resetCartItemAppearance(RoundedPanel panel) {
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(BORDER_COLOR, 1, 15),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        panel.repaint();
    }

    private void applyFocusedCartItemAppearance(RoundedPanel panel) {
        panel.setBackground(new Color(230, 250, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(TEAL_COLOR, 3, 15),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        panel.repaint();
    }

    private void handleHoldSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cart is empty. Add products before placing on hold.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Place this sale on hold?\nYou can switch back to this invoice later.\n\n"
                + "Note: Payment method is optional for hold sales.",
                "Hold Sale", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int salesId = saveSale(2);  // statusId 2 = Hold
        if (salesId != -1) {
            JOptionPane.showMessageDialog(this,
                    "Sale placed on hold! Invoice: " + getLastInvoiceNumber(salesId),
                    "Sale On Hold",
                    JOptionPane.INFORMATION_MESSAGE);
            resetCart();
            if (cartListener != null) {
                cartListener.onCartUpdated(0, 0);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to place sale on hold. Please try again.",
                    "Hold Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printSalesReceipt(int salesId) {

        String sql = """
        SELECT 
            s.invoice_no,
            s.datetime,
            u.name AS cashier_name,
            p.product_name,
            st.batch_no,
            si.qty,
            si.price,
            IFNULL(si.discount_price, 0) AS discount
        FROM sales s
        JOIN sale_item si ON s.sales_id = si.sales_id
        JOIN stock st ON si.stock_id = st.stock_id
        JOIN product p ON st.product_id = p.product_id
        JOIN user u ON s.user_id = u.user_id
        WHERE s.sales_id = ?
    """;
        System.out.println(salesId);
        try (Connection conn = DB.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, salesId);
            ResultSet rs = pst.executeQuery();

            // ---------------- Totals ----------------
            double subTotal = 0;
            double totalDiscount = 0;
            int totalItems = 0;

            String invoiceNo = "";
            String cashier = "";

            List<Map<String, Object>> items = new ArrayList<>();

            while (rs.next()) {

                invoiceNo = rs.getString("invoice_no");
                cashier = rs.getString("cashier_name");

                int qty = rs.getInt("qty");
                double unitPrice = rs.getDouble("price");
                double discount = rs.getDouble("discount");

                double amount = (qty * unitPrice) - discount;

                subTotal += amount;
                totalDiscount += discount;
                totalItems += qty;

                Map<String, Object> row = new HashMap<>();
                row.put("PROD_NAME", rs.getString("product_name"));
                row.put("BATCH_NO", rs.getString("batch_no"));
                row.put("QTY", qty);
                row.put("UNIT_PRICE", unitPrice);
                row.put("DISCOUNT", discount);
                row.put("AMOUNT", amount);

                items.add(row);
            }

            // 🔴 VERY IMPORTANT: No items = no print
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No items found for receipt");
                return;
            }

            // ---------------- Jasper Parameters ----------------
            Map<String, Object> params = new HashMap<>();
            params.put("BUSINESS_NAME", "MY POS SHOP");
            params.put("BADDRESS_PHONE", "Matale | 066 433 5420");
            params.put("INVOICE_NO", invoiceNo);
            params.put("STAFF", cashier);
            params.put("CUSTOMER", "Walking Customer");

            params.put("TOTAL_ITEMS", String.valueOf(totalItems));
            params.put("SUB_TOTAL", String.format("%.2f", subTotal));
            params.put("TOTAL_SAVINGS", totalDiscount);
            params.put("TOTAL_AMOUNT", subTotal);
            params.put("PAYMENT_METHOD", "CASH");
            params.put("AMOUNT_REC", subTotal);
            params.put("BALANCE_PAID", 0.00);

            // ---------------- DataSource ----------------
            JRBeanCollectionDataSource dataSource
                    = new JRBeanCollectionDataSource(items);

            InputStream reportStream = getClass().getResourceAsStream(
                    "/lk/com/pos/reports/pos_bill72mm1.jrxml"
            );

            JasperReport report
                    = JasperCompileManager.compileReport(reportStream);

            JasperPrint print
                    = JasperFillManager.fillReport(report, params, dataSource);

            JasperPrintManager.printReport(print, false);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Receipt print failed",
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int saveSale(int statusId) {
        try {
            int userId = Session.getInstance().getUserId();
            if (userId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "User session not found. Please log in again.",
                        "Session Error", JOptionPane.ERROR_MESSAGE);
                return -1;
            }

            String selectedPayment = (String) paymentcombo.getSelectedItem();
            Integer paymentMethodId = null;

            if (statusId == 1) {
                if (selectedPayment == null || selectedPayment.equals("Select Payment Method")) {
                    JOptionPane.showMessageDialog(this,
                            "Please select a payment method for completed sales.",
                            "Payment Method Required", JOptionPane.ERROR_MESSAGE);
                    return -1;
                }
                paymentMethodId = getPaymentMethodId(selectedPayment);
                if (paymentMethodId == -1) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid payment method selected.",
                            "Payment Method Error", JOptionPane.ERROR_MESSAGE);
                    return -1;
                }
            } else if (statusId == 2) {
                if (selectedPayment != null && !selectedPayment.equals("Select Payment Method")) {
                    paymentMethodId = getPaymentMethodId(selectedPayment);
                    if (paymentMethodId == -1) {
                        paymentMethodId = null;
                    }
                }
            }

            double total = getTotal();

            CartSaleDTO saleDTO = new CartSaleDTO();
            saleDTO.setInvoiceNo(cartSaleDAO.generateInvoiceNumber());
            saleDTO.setTotal(total);
            saleDTO.setUserId(userId);
            saleDTO.setPaymentMethodId(paymentMethodId);
            saleDTO.setStatusId(statusId);

            Integer discountId = null;
            if (appliedDiscountTypeId > 0 && appliedDiscountAmount > 0) {
                discountId = createDiscountRecord(appliedDiscountTypeId, appliedDiscountAmount);
                if (discountId > 0) {
                    saleDTO.setDiscountId(discountId);
                }
            }

            int salesId = cartSaleDAO.saveSale(saleDTO);

            if (salesId <= 0) {
                throw new Exception("Failed to save sale record");
            }

            lastGeneratedSalesId = salesId;

            List<CartSaleItemDTO> saleItems = new ArrayList<>();
            for (CartItem cartItem : cartItems.values()) {
                int stockId = cartStockDAO.getStockId(cartItem.getProductId(), cartItem.getBatchNo());
                if (stockId == -1) {
                    throw new Exception("Stock not found for product: " + cartItem.getProductName());
                }

                CartSaleItemDTO saleItem = new CartSaleItemDTO(
                        0,
                        cartItem.getQuantity(),
                        cartItem.getUnitPrice(),
                        cartItem.getDiscountPrice(),
                        cartItem.getTotalPrice(),
                        salesId,
                        stockId
                );
                saleItems.add(saleItem);
            }

            if (!cartSaleDAO.saveSaleItems(saleItems)) {
                throw new Exception("Failed to save sale items");
            }
           
            printSalesReceipt(salesId);

            for (CartItem cartItem : cartItems.values()) {
                int stockId = cartStockDAO.getStockId(cartItem.getProductId(), cartItem.getBatchNo());
                if (stockId == -1) {
                    throw new Exception("Stock not found for product: " + cartItem.getProductName());
                }

                if (!cartStockDAO.reduceStockQuantity(stockId, cartItem.getQuantity())) {
                    throw new Exception("Failed to update stock for product: " + cartItem.getProductName());
                }
            }

            if (exchangeRefundAmount > 0) {
                createExchangeRefundRecord(salesId, exchangeRefundAmount);
            }

            checkAndCreateStockNotifications();

            return salesId;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving sale: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private boolean validateStockAvailability() throws SQLException {
        if (cartItems.isEmpty()) {
            return true;
        }

        for (CartItem item : cartItems.values()) {
            CartStockDTO stock = cartStockDAO.getAvailableStock(item.getProductId(), item.getBatchNo());
            if (stock == null) {
                JOptionPane.showMessageDialog(this,
                        "Stock not found for " + item.getProductName(),
                        "Stock Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (stock.getQty() < item.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        String.format("%s - Insufficient stock!\nAvailable: %d, Requested: %d",
                                item.getProductName(), stock.getQty(), item.getQuantity()),
                        "Stock Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    private void checkAndCreateStockNotifications() {
        try {
            Notification notificationSystem = new Notification();

            for (CartItem item : cartItems.values()) {
                int stockId = cartStockDAO.getStockId(item.getProductId(), item.getBatchNo());
                if (stockId == -1) {
                    continue;
                }

                CartStockDTO stock = cartStockDAO.getAvailableStock(item.getProductId(), item.getBatchNo());
                if (stock != null) {
                    int currentQty = stock.getQty();
                    int newQty = currentQty - item.getQuantity();
                    notificationSystem.checkStockAfterSale(stockId, newQty);
                }
            }
        } catch (Exception e) {
            // Silent error handling for notifications
        }
    }

    private void displayRecentNotifications() {
        try {
            ResultSet rs = (ResultSet) Notification.getUnreadNotifications();
            StringBuilder notifications = new StringBuilder();
            int count = 0;

            while (rs.next() && count < 5) {
                String message = rs.getString("massage");
                String type = rs.getString("msg_type");
                notifications.append("• ").append(message).append("\n");
                count++;
            }

            if (count > 0) {
                JOptionPane.showMessageDialog(this,
                        "Stock Alerts:\n" + notifications.toString(),
                        "Stock Notifications",
                        JOptionPane.WARNING_MESSAGE);
            }

            rs.close();
        } catch (Exception e) {
            // Silent error handling for notifications
        }
    }

    private String getLastInvoiceNumber(int salesId) {
        try {
            CartSaleDTO sale = cartSaleDAO.getSaleById(salesId);
            if (sale != null) {
                return sale.getInvoiceNo();
            }
        } catch (Exception e) {
            // Silent error handling
        }
        return "Unknown";
    }

    private void handleSwitchAction(Invoice invoice) {
        String message = String.format(
                "Switch to invoice: %s?\nAmount: Rs.%.2f\nDate: %s\n\nCurrent cart will be cleared.",
                invoice.getInvoiceNo(), invoice.getTotal(), invoice.getDate()
        );

        int response = JOptionPane.showConfirmDialog(this, message, "Switch Invoice", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            openInvoicePanel(invoice);
            JOptionPane.showMessageDialog(this,
                    "Successfully switched to invoice: " + invoice.getInvoiceNo(),
                    "Invoice Switched",
                    JOptionPane.INFORMATION_MESSAGE);

            updateInvoiceNumberDisplay(invoice.getInvoiceNo());
        }
    }

    private void updateInvoiceNumberDisplay(String invoiceNo) {
        // Implementation for updating invoice number display
    }

    private void loadRecentInvoices() {
        recentInvoices = new ArrayList<>();
        try {
            List<CartInvoiceDTO> invoiceDTOs = cartInvoiceDAO.getRecentInvoices(24, 50);

            for (CartInvoiceDTO dto : invoiceDTOs) {
                Invoice invoice = new Invoice(
                        dto.getSalesId(),
                        dto.getInvoiceNo(),
                        dto.getDatetime(),
                        dto.getStatus(),
                        dto.getTotal(),
                        null,
                        dto.getPaymentMethod(),
                        dto.getUserId()
                );
                recentInvoices.add(invoice);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading invoices: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openInvoicePanel(Invoice invoice) {
        cartItems.clear();
        loadInvoiceItems(invoice);
        updateCartPanel();

        if ("Hold".equalsIgnoreCase(invoice.getStatus())) {
            paymentcombo.setSelectedItem("Cash Payment");
        }

        this.selectedInvoice = invoice;
    }

    private void loadInvoiceItems(Invoice invoice) {
        try {
            List<CartSaleItemDTO> saleItems = cartInvoiceDAO.getSaleItemsBySaleId(invoice.getSalesId());

            boolean hasAdjustments = false;
            StringBuilder adjustmentMessage = new StringBuilder();

            cartItems.clear();

            for (CartSaleItemDTO saleItem : saleItems) {
                int stockId = saleItem.getStockId();

                CartStockDTO stock = getStockById(stockId);
                if (stock == null) {
                    hasAdjustments = true;
                    adjustmentMessage.append(String.format(
                            "• Item with stock ID %d: Removed from cart (stock not found)\n",
                            stockId));
                    continue;
                }

                CartProductDTO product = cartProductDAO.getProductByBarcode(getProductBarcode(stock.getProductId()));
                if (product == null) {
                    hasAdjustments = true;
                    adjustmentMessage.append(String.format(
                            "• Item with product ID %d: Removed from cart (product not found)\n",
                            stock.getProductId()));
                    continue;
                }

                int savedQty = saleItem.getQty();
                int currentStock = stock.getQty();
                int qtyToLoad = Math.min(savedQty, currentStock);

                if (qtyToLoad <= 0) {
                    hasAdjustments = true;
                    adjustmentMessage.append(String.format(
                            "• %s (%s): Removed from cart (out of stock)\n",
                            product.getProductName(), product.getBrandName()));
                    continue;
                }

                String cartKey = stock.getProductId() + "_" + stock.getBatchNo();
                CartItem item = new CartItem(stock.getProductId(), product.getProductName(), product.getBrandName(),
                        stock.getBatchNo(), currentStock, saleItem.getPrice(), product.getBarcode(), stock.getSellingPrice());
                item.setQuantity(qtyToLoad);
                item.setDiscountPrice(saleItem.getDiscountPrice());
                cartItems.put(cartKey, item);

                StockTracker.getInstance().addToCart(stock.getProductId(), stock.getBatchNo(), qtyToLoad);

                if (qtyToLoad < savedQty) {
                    hasAdjustments = true;
                    adjustmentMessage.append(String.format(
                            "• %s (%s): Quantity adjusted from %d to %d\n",
                            product.getProductName(), product.getBrandName(), savedQty, qtyToLoad));
                }
            }

            if (hasAdjustments) {
                final String message = "Stock levels have changed since this sale was placed on hold:\n\n"
                        + adjustmentMessage.toString()
                        + "\nThe cart has been updated with current stock availability.";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            message,
                            "Stock Adjustments",
                            JOptionPane.WARNING_MESSAGE);
                });
            }

            updateTotals();
            updateCartPanel();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading invoice items: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private CartStockDTO getStockById(int stockId) throws SQLException {
        String query = "SELECT s.stock_id, s.product_id, s.batch_no, s.qty, s.selling_price "
                + "FROM stock s WHERE s.stock_id = ?";

        try (Connection conn = lk.com.pos.connection.MySQL.getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, stockId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new CartStockDTO(
                            rs.getInt("stock_id"),
                            rs.getInt("product_id"),
                            rs.getString("batch_no"),
                            rs.getInt("qty"),
                            rs.getDouble("selling_price"),
                            0.0
                    );
                }
            }
        }
        return null;
    }

    private String getProductBarcode(int productId) throws SQLException {
        String query = "SELECT barcode FROM product WHERE product_id = ?";

        try (Connection conn = lk.com.pos.connection.MySQL.getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, productId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("barcode");
                }
            }
        }
        return null;
    }

    public double getAppliedDiscountAmount() {
        return appliedDiscountAmount;
    }

    public int getAppliedDiscountTypeId() {
        return appliedDiscountTypeId;
    }

    public double getExchangeRefundAmount() {
        return exchangeRefundAmount;
    }

    public void setExchangeRefundAmount(double amount) {
        this.exchangeRefundAmount = amount;
        updateTotals();
    }

    private void handleCompleteSale() {
        if (selectedInvoice != null && "Hold".equalsIgnoreCase(selectedInvoice.getStatus())) {
            completeHoldSale(selectedInvoice);
            return;
        }

        if (!validateCheckout()) {
            return;
        }

        String selectedPayment = (String) paymentcombo.getSelectedItem();
        if (selectedPayment == null || selectedPayment.equals("Select Payment Method")) {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment method before completing the sale.",
                    "Missing Payment Method",
                    JOptionPane.WARNING_MESSAGE);
            paymentcombo.requestFocus();
            return;
        }

        if (selectedPayment.toLowerCase().contains("cash")) {
            handleCashPayment();

            return;
        }

        int salesId = saveSale(1);

        if (salesId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save sale. Please try again.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedPayment.toLowerCase().contains("credit")) {
            openCreditPaymentDialog(salesId);
        } else if (selectedPayment.toLowerCase().contains("card")) {
            openCardPaymentDialog(salesId);
        } else if (selectedPayment.toLowerCase().contains("cheque")) {
            openChequePaymentDialog(salesId);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Sale completed successfully! Sale ID: " + salesId,
                    "Sale Complete",
                    JOptionPane.INFORMATION_MESSAGE);

            SwingUtilities.invokeLater(() -> {
                displayRecentNotifications();
            });

            resetCart();
            if (cartListener != null) {
                cartListener.onCheckoutComplete();
            }
        }
    }

    private void completeHoldSale(Invoice holdInvoice) {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cart is empty. Add products before completing the sale.",
                    "Empty Cart",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (validateAndAdjustLoadedItems()) {
            int response = JOptionPane.showConfirmDialog(this,
                    "Some item quantities were adjusted due to stock changes.\n\nDo you want to continue with the adjusted cart?",
                    "Stock Adjustments Made",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String selectedPayment = (String) paymentcombo.getSelectedItem();
        if (selectedPayment == null || selectedPayment.equals("Select Payment Method")) {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment method to complete the sale.",
                    "Payment Method Required",
                    JOptionPane.WARNING_MESSAGE);
            paymentcombo.requestFocus();
            return;
        }

        if (selectedPayment.toLowerCase().contains("cash")) {
            try {
                String amountText = jTextField2.getText().trim();
                if (amountText.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter amount received for cash payment.",
                            "Amount Required",
                            JOptionPane.WARNING_MESSAGE);
                    jTextField2.requestFocus();
                    return;
                }

                double amountReceived = Double.parseDouble(amountText);
                double total = getTotal();
                if (amountReceived < total) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Insufficient payment!\nRequired: Rs.%.2f\nReceived: Rs.%.2f\nShort: Rs.%.2f",
                                    total, amountReceived, Math.abs(total - amountReceived)),
                            "Insufficient Payment",
                            JOptionPane.WARNING_MESSAGE);
                    jTextField2.requestFocus();
                    jTextField2.selectAll();
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid amount received. Please enter a valid number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                jTextField2.requestFocus();
                jTextField2.selectAll();
                return;
            }
        }

        double total = getTotal();
        String confirmMessage = String.format(
                "Complete hold invoice %s?\n\nOriginal Amount: Rs.%.2f\nNew Amount: Rs.%.2f\nPayment Method: %s",
                holdInvoice.getInvoiceNo(), holdInvoice.getTotal(), total, selectedPayment
        );

        if (selectedPayment.toLowerCase().contains("cash")) {
            try {
                double amountReceived = Double.parseDouble(jTextField2.getText().trim());
                double balance = amountReceived - total;
                confirmMessage += String.format("\n\nAmount Received: Rs.%.2f\nBalance: Rs.%.2f",
                        amountReceived, balance);
            } catch (NumberFormatException e) {
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                confirmMessage,
                "Complete Hold Sale",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (updateHoldSaleToCompleted(holdInvoice)) {
            if (selectedPayment.toLowerCase().contains("cash")) {
                try {
                    double amountReceived = Double.parseDouble(jTextField2.getText().trim());
                    double totalAmount = getTotal();
                    double balance = amountReceived - totalAmount;

                    String successMessage = String.format(
                            "Hold sale completed successfully!\n\nInvoice: %s\nTotal: Rs.%.2f\nReceived: Rs.%.2f\nChange: Rs.%.2f",
                            holdInvoice.getInvoiceNo(), totalAmount, amountReceived, balance
                    );
                    JOptionPane.showMessageDialog(this,
                            successMessage,
                            "Payment Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Hold sale completed successfully!\nInvoice: " + holdInvoice.getInvoiceNo(),
                            "Sale Completed",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else if (selectedPayment.toLowerCase().contains("credit")) {
                openCreditPaymentDialog(holdInvoice.getSalesId());
                return;
            } else if (selectedPayment.toLowerCase().contains("card")) {
                openCardPaymentDialog(holdInvoice.getSalesId());
                return;
            } else if (selectedPayment.toLowerCase().contains("cheque")) {
                openChequePaymentDialog(holdInvoice.getSalesId());
                return;
            } else {
                JOptionPane.showMessageDialog(this,
                        "Hold sale completed successfully!\nInvoice: " + holdInvoice.getInvoiceNo(),
                        "Sale Completed",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            resetCartAndSelectedInvoice();
        }
    }

    private boolean updateHoldSaleToCompleted(Invoice holdInvoice) {
        try {
            String selectedPayment = (String) paymentcombo.getSelectedItem();
            int paymentMethodId = getPaymentMethodId(selectedPayment);

            if (paymentMethodId == -1) {
                JOptionPane.showMessageDialog(this,
                        "Invalid payment method selected.",
                        "Payment Method Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            double total = getTotal();

            if (!cartSaleDAO.updateSaleStatus(holdInvoice.getSalesId(), 1, paymentMethodId)) {
                throw new Exception("Failed to update sales record");
            }

            deleteSaleItems(holdInvoice.getSalesId());

            List<CartSaleItemDTO> saleItems = new ArrayList<>();
            for (CartItem cartItem : cartItems.values()) {
                int stockId = cartStockDAO.getStockId(cartItem.getProductId(), cartItem.getBatchNo());
                if (stockId == -1) {
                    throw new Exception("Stock not found for product: " + cartItem.getProductName());
                }

                CartSaleItemDTO saleItem = new CartSaleItemDTO(
                        0,
                        cartItem.getQuantity(),
                        cartItem.getUnitPrice(),
                        cartItem.getDiscountPrice(),
                        cartItem.getTotalPrice(),
                        holdInvoice.getSalesId(),
                        stockId
                );
                saleItems.add(saleItem);
            }

            if (!cartSaleDAO.saveSaleItems(saleItems)) {
                throw new Exception("Failed to save sale items");
            }

            for (CartItem cartItem : cartItems.values()) {
                int stockId = cartStockDAO.getStockId(cartItem.getProductId(), cartItem.getBatchNo());
                if (stockId == -1) {
                    throw new Exception("Stock not found for product: " + cartItem.getProductName());
                }

                if (!cartStockDAO.reduceStockQuantity(stockId, cartItem.getQuantity())) {
                    throw new Exception("Failed to update stock for product: " + cartItem.getProductName());
                }
            }

            if (appliedDiscountAmount > 0 && appliedDiscountTypeId > 0) {
                int discountId = createDiscountRecord(appliedDiscountTypeId, appliedDiscountAmount);
                if (discountId > 0) {
                    updateSaleDiscount(holdInvoice.getSalesId(), discountId);
                }
            } else {
                updateSaleDiscount(holdInvoice.getSalesId(), null);
            }

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error completing hold sale: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void deleteSaleItems(int salesId) throws SQLException {
        String query = "DELETE FROM sale_item WHERE sales_id = ?";
        try (Connection conn = lk.com.pos.connection.MySQL.getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, salesId);
            pst.executeUpdate();
        }
    }

    private void updateSaleDiscount(int salesId, Integer discountId) throws SQLException {
        String query = "UPDATE sales SET discount_id = ? WHERE sales_id = ?";
        try (Connection conn = lk.com.pos.connection.MySQL.getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
            if (discountId != null) {
                pst.setInt(1, discountId);
            } else {
                pst.setNull(1, java.sql.Types.INTEGER);
            }
            pst.setInt(2, salesId);
            pst.executeUpdate();
        }
    }

    private void resetCartAndSelectedInvoice() {
        resetCart();
        selectedInvoice = null;

        if (cartListener != null) {
            cartListener.onCheckoutComplete();
        }
    }

    private void handleCashPayment() {
        try {
            double amountReceived = Double.parseDouble(jTextField2.getText().trim());
            double total = getTotal();
            double balance = amountReceived - total;

            if (balance < 0) {
                JOptionPane.showMessageDialog(this,
                        String.format("Insufficient payment!\nRequired: Rs.%.2f\nReceived: Rs.%.2f\nShort: Rs.%.2f",
                                total, amountReceived, Math.abs(balance)),
                        "Insufficient Payment",
                        JOptionPane.WARNING_MESSAGE);
                jTextField2.requestFocus();
                jTextField2.selectAll();
                return;
            }

            String confirmMessage = String.format(
                    "Complete cash payment?\n\nTotal: Rs.%.2f\nReceived: Rs.%.2f\nBalance: Rs.%.2f",
                    total, amountReceived, balance);

            int confirm = JOptionPane.showConfirmDialog(this, confirmMessage,
                    "Confirm Cash Payment", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            int salesId;
            if (selectedInvoice != null && "Hold".equalsIgnoreCase(selectedInvoice.getStatus())) {
                if (updateHoldSaleToCompleted(selectedInvoice)) {
                    String successMessage = String.format(
                            "Cash payment completed successfully!\n\nInvoice: %s\nTotal: Rs.%.2f\nReceived: Rs.%.2f\nChange: Rs.%.2f",
                            selectedInvoice.getInvoiceNo(), total, amountReceived, balance);

                    JOptionPane.showMessageDialog(this, successMessage,
                            "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

                    SwingUtilities.invokeLater(() -> {
                        displayRecentNotifications();
                    });

                    resetCartAndSelectedInvoice();
                }
            } else {
                salesId = saveSale(1);
                if (salesId != -1) {
                    String successMessage = String.format(
                            "Cash payment completed successfully!\n\nSale ID: %d\nTotal: Rs.%.2f\nReceived: Rs.%.2f\nChange: Rs.%.2f",
                            salesId, total, amountReceived, balance);

                    JOptionPane.showMessageDialog(this, successMessage,
                            "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

                    SwingUtilities.invokeLater(() -> {
                        displayRecentNotifications();
                    });

                    resetCart();
                    if (cartListener != null) {
                        cartListener.onCheckoutComplete();
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid amount received. Please enter a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            jTextField2.requestFocus();
            jTextField2.selectAll();
        }
    }

    private int createDiscountRecord(int discountTypeId, double discountAmount) throws SQLException {
        String query = "INSERT INTO discount (discount, discount_type_id) VALUES (?, ?)";

        try (Connection conn = lk.com.pos.connection.MySQL.getConnection(); PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setDouble(1, discountAmount);
            pst.setInt(2, discountTypeId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    private void createExchangeRefundRecord(int salesId, double refundAmount) throws SQLException {
        String query = "INSERT INTO `return` (sales_id, total_return_amount, total_discount_price, return_date, user_id, status_id, return_reason_id) VALUES (?, ?, ?, NOW(), ?, 1, ?)";

        try (Connection conn = lk.com.pos.connection.MySQL.getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, salesId);
            pst.setDouble(2, refundAmount);
            pst.setDouble(3, 0.0);
            pst.setInt(4, Session.getInstance().getUserId());
            pst.setInt(5, 1);

            pst.executeUpdate();
        }
    }

    private int getPaymentMethodId(String paymentMethodName) {
        Map<String, Integer> paymentMethodMap = new HashMap<>();
        paymentMethodMap.put("Cash Payment", 1);
        paymentMethodMap.put("Card Payment", 2);
        paymentMethodMap.put("Credit Payment", 3);
        paymentMethodMap.put("Cheque Payment", 4);

        return paymentMethodMap.getOrDefault(paymentMethodName, -1);
    }

    private void handleViewAction(Invoice invoice) {
        try {
            List<CartSaleItemDTO> saleItems = cartInvoiceDAO.getSaleItemsBySaleId(invoice.getSalesId());

            StringBuilder details = new StringBuilder();
            details.append("Invoice Details:\n");
            details.append("ID: ").append(invoice.getInvoiceNo()).append("\n");
            details.append("Status: ").append(invoice.getStatus()).append("\n");
            details.append("Amount: Rs. ").append(String.format("%.2f", invoice.getTotal())).append("\n");
            details.append("Date: ").append(invoice.getDate()).append("\n\n");
            details.append("Items:\n");

            double itemTotal = 0;
            for (CartSaleItemDTO saleItem : saleItems) {
                int stockId = saleItem.getStockId();
                CartStockDTO stock = getStockById(stockId);
                if (stock == null) {
                    continue;
                }

                CartProductDTO product = cartProductDAO.getProductByBarcode(getProductBarcode(stock.getProductId()));
                if (product == null) {
                    continue;
                }

                details.append(String.format("- %s (%s) x%d: Rs. %.2f (Discount: Rs. %.2f) = Rs. %.2f\n",
                        product.getProductName(), product.getBrandName(), saleItem.getQty(),
                        saleItem.getPrice(), saleItem.getDiscountPrice(), saleItem.getTotal()));
                itemTotal += saleItem.getTotal();
            }

            details.append("\nTotal: Rs. ").append(String.format("%.2f", itemTotal));

            JOptionPane.showMessageDialog(this,
                    details.toString(),
                    "View Invoice - " + invoice.getInvoiceNo(),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading invoice details: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCreditPaymentDialog(int salesId) {
        try {
            double totalAmount = getTotal();
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddCredit dialog = new AddCredit(parentFrame, true, totalAmount, salesId);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);

            if (dialog.isCreditSaved()) {
                double paidAmount = dialog.getPaidAmount();
                if (paidAmount > 0) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Credit sale completed! Sale ID: %d, Payment: Rs.%.2f", salesId, paidAmount),
                            "Sale Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Credit sale completed! Sale ID: " + salesId,
                            "Sale Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                SwingUtilities.invokeLater(() -> {
                    displayRecentNotifications();
                });

                resetCartAndSelectedInvoice();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sale was saved but credit record was not completed.\nSale ID: " + salesId
                        + "\nPlease add credit record manually if needed.",
                        "Credit Record Incomplete", JOptionPane.WARNING_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    displayRecentNotifications();
                });

                resetCartAndSelectedInvoice();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Sale was saved but error opening credit dialog.\nSale ID: " + salesId
                    + "\nError: " + e.getMessage(),
                    "Dialog Error", JOptionPane.ERROR_MESSAGE);

            SwingUtilities.invokeLater(() -> {
                displayRecentNotifications();
            });

            resetCartAndSelectedInvoice();
        }
    }

    private void openCardPaymentDialog(int salesId) {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            CardPayDialog dialog = new CardPayDialog(parentFrame, true, salesId);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);

            Integer cardPaymentId = dialog.getGeneratedCardPaymentId();
            if (cardPaymentId != null && cardPaymentId > 0) {
                JOptionPane.showMessageDialog(this,
                        String.format("Card payment completed! Sale ID: %d, Card Payment ID: %d", salesId, cardPaymentId),
                        "Sale Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    displayRecentNotifications();
                });

                resetCartAndSelectedInvoice();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sale was saved but card payment record was not completed.\nSale ID: " + salesId
                        + "\nPlease add card payment record manually if needed.",
                        "Card Payment Incomplete", JOptionPane.WARNING_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    displayRecentNotifications();
                });

                resetCartAndSelectedInvoice();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Sale was saved but error opening card payment dialog.\nSale ID: " + salesId
                    + "\nError: " + e.getMessage(),
                    "Dialog Error", JOptionPane.ERROR_MESSAGE);

            SwingUtilities.invokeLater(() -> {
                displayRecentNotifications();
            });

            resetCartAndSelectedInvoice();
        }
    }

    private void openChequePaymentDialog(int salesId) {
        try {
            double totalAmount = getTotal();
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddCheque dialog = new AddCheque(parentFrame, true, salesId, totalAmount);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);

            if (dialog.isChequeSaved()) {
                String chequeNo = dialog.getChequeNo();
                double chequeAmount = dialog.getChequeAmount();
                JOptionPane.showMessageDialog(this,
                        String.format("Cheque payment completed! Sale ID: %d, Cheque: %s, Amount: Rs.%.2f",
                                salesId, chequeNo, chequeAmount),
                        "Sale Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    displayRecentNotifications();
                });

                resetCartAndSelectedInvoice();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sale was saved but cheque record was not completed.\nSale ID: " + salesId
                        + "\nPlease add cheque record manually if needed.",
                        "Cheque Payment Incomplete", JOptionPane.WARNING_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    displayRecentNotifications();
                });

                resetCartAndSelectedInvoice();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Sale was saved but error opening cheque dialog.\nSale ID: " + salesId
                    + "\nError: " + e.getMessage(),
                    "Dialog Error", JOptionPane.ERROR_MESSAGE);

            SwingUtilities.invokeLater(() -> {
                displayRecentNotifications();
            });

            resetCartAndSelectedInvoice();
        }
    }

    public int getLastGeneratedSalesId() {
        return lastGeneratedSalesId;
    }

    private double getRefundAmountFromReturn(int returnId) throws SQLException {
        String query = "SELECT total_return_amount FROM `return` WHERE return_id = ?";

        try (Connection conn = lk.com.pos.connection.MySQL.getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, returnId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_return_amount");
                }
            }
        }
        return 0.0;
    }

    void showSwitchInvoicePanel() {
        loadRecentInvoices();
        invoiceCardsList.clear();
        currentFocusedIndex = -1;

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        notificationDialog = new javax.swing.JDialog(parentFrame, false);
        notificationDialog.setUndecorated(true);
        notificationDialog.setPreferredSize(new Dimension(480, 500));
        notificationDialog.setBackground(new Color(0, 0, 0, 0));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(0x1CB5BB), 2));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
        headerPanel.setBackground(new Color(0x1CB5BB));

        JLabel titleLabel = new JLabel("Switch Invoice");
        titleLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtonPanel.setBackground(new Color(0x1CB5BB));

        final FlatSVGIcon refreshIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 18, 18);
        refreshIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return Color.WHITE;
            }
        });

        JButton refreshButton = new JButton(refreshIcon);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.setToolTipText("Refresh");

        refreshButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                refreshIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return new Color(0xFFE0E0);
                    }
                });
                refreshButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                refreshIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return Color.WHITE;
                    }
                });
                refreshButton.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                loadRecentInvoices();
                showSwitchInvoicePanel();
            }
        });

        JButton helpButton = new JButton("?");
        helpButton.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        helpButton.setForeground(Color.WHITE);
        helpButton.setBackground(new Color(0x1CB5BB));
        helpButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        helpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        helpButton.setToolTipText("Keyboard Help");
        helpButton.addActionListener(e -> showKeyboardHelp());

        final FlatSVGIcon closeIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 18, 18);
        closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return Color.WHITE;
            }
        });

        JButton closeButton = new JButton(closeIcon);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setToolTipText("Close");

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return new Color(0xFFE0E0);
                    }
                });
                closeButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return Color.WHITE;
                    }
                });
                closeButton.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                notificationDialog.setVisible(false);
                notificationDialog.dispose();
            }
        });

        rightButtonPanel.add(refreshButton);
        rightButtonPanel.add(helpButton);
        rightButtonPanel.add(closeButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightButtonPanel, BorderLayout.EAST);

        JPanel subtitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        subtitlePanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        subtitlePanel.setBackground(new Color(0xF8F9FA));

        JLabel subtitleLabel = new JLabel("Recent Invoices (Last 24 Hours) ");
        subtitleLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(0x666666));

        JLabel countLabel = new JLabel(recentInvoices.size() + " found");
        countLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 12));
        countLabel.setForeground(new Color(0x1CB5BB));

        subtitlePanel.add(subtitleLabel);
        subtitlePanel.add(countLabel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(480, 400));

        if (recentInvoices.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
            emptyPanel.setBackground(Color.WHITE);
            JLabel emptyLabel = new JLabel("No invoices found in the last 24 hours", JLabel.CENTER);
            emptyLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
            emptyLabel.setForeground(new Color(0x999999));
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            contentPanel.add(emptyPanel);
        } else {
            for (int i = 0; i < recentInvoices.size(); i++) {
                Invoice invoice = recentInvoices.get(i);
                JPanel invoiceCard = createInvoiceCard(invoice, i);
                invoiceCardsList.add(invoiceCard);
                contentPanel.add(invoiceCard);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(subtitlePanel, BorderLayout.NORTH);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);

        notificationDialog.add(mainPanel);
        notificationDialog.pack();

        setupEnhancedKeyboardNavigation(scrollPane);

        positionDialog(notificationDialog, switchBtn);

        notificationDialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            private boolean gainedFocusOnce = false;

            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                gainedFocusOnce = true;
            }

            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                if (gainedFocusOnce) {
                    javax.swing.Timer timer = new javax.swing.Timer(100, evt -> {
                        if (!notificationDialog.isFocusOwner() && !isChildComponentFocused(notificationDialog)) {
                            notificationDialog.setVisible(false);
                            notificationDialog.dispose();
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }

            private boolean isChildComponentFocused(java.awt.Window window) {
                java.awt.Component focusOwner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner == null) {
                    return false;
                }
                return javax.swing.SwingUtilities.isDescendingFrom(focusOwner, window);
            }
        });

        notificationDialog.setVisible(true);
        if (!invoiceCardsList.isEmpty()) {
            setFocusedCard(0, scrollPane);
        }
    }

    private void positionDialog(javax.swing.JDialog dialog, java.awt.Component relativeTo) {
        java.awt.Point buttonLoc = relativeTo.getLocationOnScreen();
        int dialogWidth = dialog.getPreferredSize().width;
        int dialogHeight = dialog.getPreferredSize().height;
        java.awt.GraphicsConfiguration gc = relativeTo.getGraphicsConfiguration();
        java.awt.Rectangle screenBounds = gc.getBounds();
        int x = buttonLoc.x - (dialogWidth - relativeTo.getWidth()) / 2;
        int y = buttonLoc.y + relativeTo.getHeight();
        if (x + dialogWidth > screenBounds.x + screenBounds.width) {
            x = screenBounds.x + screenBounds.width - dialogWidth - 10;
        }
        if (x < screenBounds.x) {
            x = screenBounds.x + 10;
        }
        if (y + dialogHeight > screenBounds.y + screenBounds.height) {
            y = screenBounds.y + screenBounds.height - dialogHeight - 10;
            if (y < buttonLoc.y) {
                y = buttonLoc.y - dialogHeight - 10;
            }
        }
        if (y < screenBounds.y) {
            y = screenBounds.y + 10;
        }
        dialog.setLocation(x, y);
    }

    private void updateCartItemQuantity(CartItem item, int newQuantity) {
        if (item == null) {
            return;
        }

        int oldQuantity = item.getQuantity();
        int difference = newQuantity - oldQuantity;

        // Validate new quantity
        if (newQuantity <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be at least 1.",
                    "Invalid Quantity",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newQuantity > item.getAvailableQty()) {
            JOptionPane.showMessageDialog(this,
                    String.format("Cannot exceed available stock: %d", item.getAvailableQty()),
                    "Stock Limit",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Update item quantity
        item.setQuantity(newQuantity);

        // CRITICAL: Notify StockTracker of the change
        if (difference > 0) {
            // Quantity increased - add to tracker
            StockTracker.getInstance().addToCart(
                    item.getProductId(),
                    item.getBatchNo(),
                    difference
            );
        } else if (difference < 0) {
            // Quantity decreased - remove from tracker
            StockTracker.getInstance().removeFromCart(
                    item.getProductId(),
                    item.getBatchNo(),
                    Math.abs(difference)
            );
        }

        // Update UI
        updateCartPanel();
        updateTotals();
    }

    private void setupEnhancedKeyboardNavigation(JScrollPane scrollPane) {
        java.awt.event.KeyListener keyListener = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleEnhancedKeyboardNavigation(evt, scrollPane);
            }
        };
        notificationDialog.addKeyListener(keyListener);
        notificationDialog.setFocusable(true);
        SwingUtilities.invokeLater(() -> notificationDialog.requestFocusInWindow());
    }

    private void handleEnhancedKeyboardNavigation(java.awt.event.KeyEvent evt, JScrollPane scrollPane) {
        if (invoiceCardsList.isEmpty()) {
            return;
        }
        int keyCode = evt.getKeyCode();
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_DOWN:
                if (currentFocusedIndex < invoiceCardsList.size() - 1) {
                    setFocusedCard(currentFocusedIndex + 1, scrollPane);
                }
                evt.consume();
                break;
            case java.awt.event.KeyEvent.VK_UP:
                if (currentFocusedIndex > 0) {
                    setFocusedCard(currentFocusedIndex - 1, scrollPane);
                }
                evt.consume();
                break;
            case java.awt.event.KeyEvent.VK_ENTER:
                if (currentFocusedIndex >= 0 && currentFocusedIndex < recentInvoices.size()) {
                    Invoice invoice = recentInvoices.get(currentFocusedIndex);
                    String buttonText = getButtonTextBasedOnStatus(invoice.getStatus());
                    notificationDialog.setVisible(false);
                    notificationDialog.dispose();
                    handleInvoiceAction(invoice, buttonText);
                }
                evt.consume();
                break;
            case java.awt.event.KeyEvent.VK_ESCAPE:
                notificationDialog.setVisible(false);
                notificationDialog.dispose();
                evt.consume();
                break;
            case java.awt.event.KeyEvent.VK_HOME:
                if (!invoiceCardsList.isEmpty()) {
                    setFocusedCard(0, scrollPane);
                }
                evt.consume();
                break;
            case java.awt.event.KeyEvent.VK_END:
                if (!invoiceCardsList.isEmpty()) {
                    setFocusedCard(invoiceCardsList.size() - 1, scrollPane);
                }
                evt.consume();
                break;
            case java.awt.event.KeyEvent.VK_PAGE_DOWN:
                if (!invoiceCardsList.isEmpty()) {
                    setFocusedCard(Math.min(currentFocusedIndex + 5, invoiceCardsList.size() - 1), scrollPane);
                }
                evt.consume();
                break;
            case java.awt.event.KeyEvent.VK_PAGE_UP:
                if (!invoiceCardsList.isEmpty()) {
                    setFocusedCard(Math.max(currentFocusedIndex - 5, 0), scrollPane);
                }
                evt.consume();
                break;
            case java.awt.event.KeyEvent.VK_F1:
                showKeyboardHelp();
                evt.consume();
                break;
        }
    }

    private void setFocusedCard(int index, JScrollPane scrollPane) {
        if (index < 0 || index >= invoiceCardsList.size()) {
            return;
        }
        if (currentFocusedIndex >= 0 && currentFocusedIndex < invoiceCardsList.size()) {
            JPanel prevCard = invoiceCardsList.get(currentFocusedIndex);
            resetCardAppearance(prevCard, recentInvoices.get(currentFocusedIndex));
        }
        currentFocusedIndex = index;
        JPanel currentCard = invoiceCardsList.get(currentFocusedIndex);
        Invoice invoice = recentInvoices.get(currentFocusedIndex);
        applyFocusedCardAppearance(currentCard, invoice);
        SwingUtilities.invokeLater(() -> currentCard.scrollRectToVisible(currentCard.getBounds()));
    }

    private void resetCardAppearance(JPanel card, Invoice invoice) {
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        for (java.awt.Component comp : card.getComponents()) {
            if (comp instanceof JPanel) {
                setAllBackgrounds((JPanel) comp, Color.WHITE);
            }
        }
    }

    private void applyFocusedCardAppearance(JPanel card, Invoice invoice) {
        String buttonText = getButtonTextBasedOnStatus(invoice.getStatus());
        Color[] buttonColors = getButtonColors(buttonText);
        Color borderColor = buttonColors[0];
        card.setBackground(new Color(0xF8F9FA));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, 2), BorderFactory.createEmptyBorder(11, 15, 11, 15)));
        for (java.awt.Component comp : card.getComponents()) {
            if (comp instanceof JPanel) {
                setAllBackgrounds((JPanel) comp, new Color(0xF8F9FA));
            }
        }
    }

    private void setAllBackgrounds(JPanel panel, Color color) {
        panel.setBackground(color);
        for (java.awt.Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                setAllBackgrounds((JPanel) comp, color);
            }
        }
    }

    private void showKeyboardHelp() {
        String helpText = "<html><div style='text-align: center;'><b>Keyboard Shortcuts</b></div><br>"
                + "• <b>↑ / ↓</b> - Navigate invoices<br>"
                + "• <b>Enter</b> - Select invoice<br>"
                + "• <b>Home</b> - First invoice<br>"
                + "• <b>End</b> - Last invoice<br>"
                + "• <b>Page Up/Down</b> - Jump 5 invoices<br>"
                + "• <b>F1</b> - Show help<br>"
                + "• <b>Escape</b> - Close window</html>";
        JOptionPane.showMessageDialog(this, helpText, "Keyboard Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createInvoiceCard(Invoice invoice, int index) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(460, 100));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JPanel topInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topInfoPanel.setBackground(Color.WHITE);

        JLabel invoiceLabel = new JLabel(invoice.getInvoiceNo());
        invoiceLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        invoiceLabel.setForeground(new Color(0x333333));

        JLabel statusLabel = new JLabel("(" + invoice.getStatus() + ")");
        statusLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        Color statusColor;
        switch (invoice.getStatus().toLowerCase()) {
            case "completed":
                statusColor = new Color(0x4CAF50);
                break;
            case "hold":
                statusColor = new Color(0xFF9800);
                break;
            case "pending":
                statusColor = new Color(0x2196F3);
                break;
            default:
                statusColor = new Color(0x666666);
        }
        statusLabel.setForeground(statusColor);

        topInfoPanel.add(invoiceLabel);
        topInfoPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        topInfoPanel.add(statusLabel);

        JLabel dateLabel = new JLabel(invoice.getDate().toString());
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
        dateLabel.setForeground(new Color(0x999999));

        JLabel amountLabel = new JLabel("Rs. " + String.format("%.2f", invoice.getTotal()));
        amountLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        amountLabel.setForeground(new Color(0x1CB5BB));

        infoPanel.add(topInfoPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(dateLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(amountLabel);

        String buttonText = getButtonTextBasedOnStatus(invoice.getStatus());
        GradientActionButton actionButton = new GradientActionButton(buttonText, invoice);
        actionButton.addActionListener(e -> {
            if (notificationDialog != null) {
                notificationDialog.setVisible(false);
                notificationDialog.dispose();
            }
            handleInvoiceAction(invoice, buttonText);
        });

        Color[] buttonColors = getButtonColors(buttonText);
        Color borderColor = buttonColors[0];

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (notificationDialog != null) {
                    notificationDialog.setVisible(false);
                    notificationDialog.dispose();
                }
                handleInvoiceAction(invoice, buttonText);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (currentFocusedIndex != index) {
                    card.setBackground(new Color(0xF8F9FA));
                    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, 2), BorderFactory.createEmptyBorder(11, 15, 11, 15)));
                    infoPanel.setBackground(new Color(0xF8F9FA));
                    topInfoPanel.setBackground(new Color(0xF8F9FA));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (currentFocusedIndex != index) {
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                    infoPanel.setBackground(Color.WHITE);
                    topInfoPanel.setBackground(Color.WHITE);
                }
            }
        });

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionButton, BorderLayout.EAST);
        return card;
    }

    private String getButtonTextBasedOnStatus(String status) {
        if ("Completed".equalsIgnoreCase(status)) {
            return "View";
        } else if ("Hold".equalsIgnoreCase(status)) {
            return "Switch";
        } else {
            return "Open";
        }
    }

    private Color[] getButtonColors(String buttonText) {
        Color topColor, bottomColor;
        switch (buttonText) {
            case "Switch":
                topColor = new Color(255, 193, 7);
                bottomColor = new Color(255, 152, 0);
                break;
            case "View":
                topColor = new Color(105, 240, 174);
                bottomColor = new Color(76, 175, 80);
                break;
            case "Open":
                topColor = new Color(100, 181, 246);
                bottomColor = new Color(30, 136, 229);
                break;
            default:
                topColor = new Color(100, 181, 246);
                bottomColor = new Color(30, 136, 229);
                break;
        }
        return new Color[]{topColor, bottomColor};
    }

    class GradientActionButton extends JButton {

        private Invoice invoice;
        private boolean isHovered = false;

        public GradientActionButton(String text, Invoice invoice) {
            super(text);
            this.invoice = invoice;
            setupButton();
        }

        private void setupButton() {
            setFont(new Font("Nunito SemiBold", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(true);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 15));
            Color[] colors = getButtonColors(getText());
            setBorder(BorderFactory.createLineBorder(colors[0], 2));
            setForeground(colors[0]);
            setupButtonIcon();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    setForeground(Color.WHITE);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    Color[] colors = getButtonColors(getText());
                    setForeground(colors[0]);
                    repaint();
                }
            });
        }

        private void setupButtonIcon() {
            String buttonText = getText();
            String iconPath;
            switch (buttonText) {
                case "Switch":
                    iconPath = "lk/com/pos/icon/exchange.svg";
                    break;
                case "View":
                    iconPath = "lk/com/pos/icon/eye-open.svg";
                    break;
                case "Open":
                    iconPath = "lk/com/pos/icon/exchange.svg";
                    break;
                default:
                    iconPath = "lk/com/pos/icon/exchange.svg";
                    break;
            }
            try {
                FlatSVGIcon icon = new FlatSVGIcon("lk/com/pos/icon/exchange.svg", 16, 16);
                final Color[] colors = getButtonColors(getText());
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> colors[0]));
                setIcon(icon);
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> colors[0]));
                        repaint();
                    }
                });
            } catch (Exception e) {
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isHovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();
                Color[] colors = getButtonColors(getText());
                Color topColor = colors[0];
                Color bottomColor = colors[1];
                GradientPaint gradient = new GradientPaint(0, 0, topColor, width, 0, bottomColor);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, width, height, 8, 8);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private void handleInvoiceAction(Invoice invoice, String action) {
        this.selectedInvoice = invoice;
        this.invoiceSelected = true;
        if (invoiceSelectionListener != null) {
            invoiceSelectionListener.onInvoiceSelected(invoice, action);
        }
        switch (action) {
            case "Switch":
                handleSwitchAction(invoice);
                break;
            case "View":
                handleViewAction(invoice);
                break;
            case "Open":
                handleOpenAction(invoice);
                break;
            default:
                handleDefaultAction(invoice, action);
                break;
        }
    }

    private void handleOpenAction(Invoice invoice) {
        JOptionPane.showMessageDialog(this, "Opening invoice: " + invoice.getInvoiceNo() + "\n" + "This invoice is in " + invoice.getStatus() + " status.", "Open Invoice", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDefaultAction(Invoice invoice, String action) {
        JOptionPane.showMessageDialog(this, action + " action for invoice: " + invoice.getInvoiceNo(), action + " Invoice", JOptionPane.INFORMATION_MESSAGE);
    }

    public Invoice getSelectedInvoice() {
        return selectedInvoice;
    }

    public boolean isInvoiceSelected() {
        return invoiceSelected;
    }

    void exchangeBtnActionPerformed(java.awt.event.ActionEvent evt) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ExchangeProductDialog dialog = new ExchangeProductDialog(parentFrame, true);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);

        int returnId = dialog.getGeneratedReturnId();
        if (returnId > 0) {
            try {
                double refundAmount = getRefundAmountFromReturn(returnId);
                double totalRefundAmount = refundAmount;

                try {
                    java.lang.reflect.Method getTotalAmountMethod = dialog.getClass().getMethod("getTotalAmount");
                    if (getTotalAmountMethod != null) {
                        Object result = getTotalAmountMethod.invoke(dialog);
                        if (result instanceof Double) {
                            totalRefundAmount = (Double) result;
                        }
                    }
                } catch (Exception e) {
                    // Silent error handling
                }

                if (totalRefundAmount <= 0 && refundAmount > 0) {
                    totalRefundAmount = refundAmount;
                }

                if (totalRefundAmount > 0) {
                    this.exchangeRefundAmount = totalRefundAmount;
                    updateTotals();

                    JOptionPane.showMessageDialog(this,
                            String.format("Exchange processed! Refund credit: Rs.%.2f (Return ID: %d)",
                                    totalRefundAmount, returnId),
                            "Exchange Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    this.exchangeRefundAmount = 0.0;
                    updateTotals();
                    JOptionPane.showMessageDialog(this,
                            "Exchange processed! Return ID: " + returnId,
                            "Exchange Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                this.exchangeRefundAmount = 0.0;
                updateTotals();
                JOptionPane.showMessageDialog(this,
                        "Exchange processed but error calculating refund. Return ID: " + returnId,
                        "Exchange Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    void creditPayActionPerformed(java.awt.event.ActionEvent evt) {
        AddCreditPay addCreditPay = new AddCreditPay(null, true);
        addCreditPay.setLocationRelativeTo(null);
        addCreditPay.setVisible(true);
    }

    void switchBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (!cartItems.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "You have " + cartItems.size() + " product(s) in your current cart.\n"
                    + "Do you want to remove previous products and switch to another invoice?\n\n"
                    + "Current cart will be cleared when switching.",
                    "Warning - Products in Cart",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        showSwitchInvoicePanel();
    }

    void discountBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add products before applying discount.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double currentSubtotal = 0;
        for (CartItem item : cartItems.values()) {
            currentSubtotal += item.getTotalPrice();
        }
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DiscountDialog dialog = new DiscountDialog(parentFrame, true, currentSubtotal);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        if (dialog.isDiscountApplied()) {
            double discountAmount = dialog.getDiscountAmount();
            int discountTypeId = dialog.getDiscountTypeId();
            this.appliedDiscountAmount = discountAmount;
            this.appliedDiscountTypeId = discountTypeId;
            updateTotals();
            JOptionPane.showMessageDialog(this,
                    String.format("Discount of Rs.%.2f applied successfully!", discountAmount),
                    "Discount Applied",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

// ========================================
// PREVENT ACCIDENTAL DOUBLE-CLICKS (IMPROVED)
// ========================================
    private volatile boolean isProcessingCheckout = false;
    private long lastCheckoutAttempt = 0;

    void gradientButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // Debounce: prevent clicks within 1 second
        long now = System.currentTimeMillis();
        if (now - lastCheckoutAttempt < 1000) {
            return; // Ignore rapid clicks
        }
        lastCheckoutAttempt = now;

        if (isProcessingCheckout) {
            JOptionPane.showMessageDialog(this,
                    "Please wait, processing checkout...",
                    "Processing",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        isProcessingCheckout = true;
        gradientButton1.setEnabled(false);

        // Process in background to keep UI responsive
        SwingUtilities.invokeLater(() -> {
            try {
                handleCompleteSale();

            } finally {
                // Re-enable after 1 second
                javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
                    isProcessingCheckout = false;
                    gradientButton1.setEnabled(true);
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    void holdBtnActionPerformed(java.awt.event.ActionEvent evt) {
        handleHoldSale();
    }

    void clearCartBtnActionPerformed(java.awt.event.ActionEvent evt) {
        clearCart();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel9 = new javax.swing.JPanel();
        roundedPanel3 = new lk.com.pos.privateclasses.RoundedPanel();
        cartProductName = new javax.swing.JLabel();
        cartProductPanelDeleteBtn = new javax.swing.JButton();
        cminusBtn = new javax.swing.JButton();
        productCount = new javax.swing.JLabel();
        priceInput = new javax.swing.JLabel();
        cplusBtn = new javax.swing.JButton();
        discountPriceinput = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel10 = new javax.swing.JPanel();
        roundedPanel4 = new lk.com.pos.privateclasses.RoundedPanel();
        cartProductName1 = new javax.swing.JLabel();
        cartProductPanelDeleteBtn1 = new javax.swing.JButton();
        cminusBtn1 = new javax.swing.JButton();
        priceInput1 = new javax.swing.JLabel();
        cplusBtn1 = new javax.swing.JButton();
        discountPriceinput1 = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        paymentcombo = new javax.swing.JComboBox<>();
        jTextField2 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        gradientButton1 = new lk.com.pos.privateclasses.GradientButton();
        jLabel24 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        clearCartBtn = new javax.swing.JButton();
        holdBtn = new javax.swing.JButton();
        cartCount = new javax.swing.JLabel();
        discountBtn = new javax.swing.JButton();
        switchBtn = new javax.swing.JButton();
        creditPay = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        exchangeBtn = new javax.swing.JButton();

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        cartProductName.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        cartProductName.setText("Product Name (Brand)");

        cartProductPanelDeleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        cartProductPanelDeleteBtn.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cartProductPanelDeleteBtn.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        cminusBtn.setFont(new java.awt.Font("Nunito ExtraBold", 0, 14)); // NOI18N
        cminusBtn.setText("-");

        productCount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        productCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        productCount.setText("1");

        priceInput.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        priceInput.setText("Rs.1000");

        cplusBtn.setFont(new java.awt.Font("Nunito ExtraBold", 0, 14)); // NOI18N
        cplusBtn.setText("+");
        cplusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cplusBtnActionPerformed(evt);
            }
        });

        discountPriceinput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        discountPriceinput.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        discountPriceinput.setText(" Descount Price");

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel3Layout.createSequentialGroup()
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(roundedPanel3Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(priceInput))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, roundedPanel3Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(roundedPanel3Layout.createSequentialGroup()
                                .addComponent(cminusBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(productCount, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cplusBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(discountPriceinput, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(roundedPanel3Layout.createSequentialGroup()
                                .addComponent(cartProductName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cartProductPanelDeleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(17, 17, 17))
        );
        roundedPanel3Layout.setVerticalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cartProductName)
                    .addComponent(cartProductPanelDeleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cminusBtn)
                            .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(productCount, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cplusBtn))))
                    .addComponent(discountPriceinput))
                .addGap(10, 10, 10)
                .addComponent(priceInput)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(roundedPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(roundedPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(206, Short.MAX_VALUE))
        );

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        cartProductName1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        cartProductName1.setText("Product Name (Brand)");

        cartProductPanelDeleteBtn1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        cartProductPanelDeleteBtn1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cartProductPanelDeleteBtn1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        cminusBtn1.setFont(new java.awt.Font("Nunito ExtraBold", 0, 14)); // NOI18N
        cminusBtn1.setText("-");

        priceInput1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        priceInput1.setText("Rs.1000");

        cplusBtn1.setFont(new java.awt.Font("Nunito ExtraBold", 0, 14)); // NOI18N
        cplusBtn1.setText("+");

        discountPriceinput1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        discountPriceinput1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        discountPriceinput1.setText(" Descount Price");

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("1");

        javax.swing.GroupLayout roundedPanel4Layout = new javax.swing.GroupLayout(roundedPanel4);
        roundedPanel4.setLayout(roundedPanel4Layout);
        roundedPanel4Layout.setHorizontalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel4Layout.createSequentialGroup()
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(roundedPanel4Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(priceInput1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, roundedPanel4Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(roundedPanel4Layout.createSequentialGroup()
                                .addComponent(cminusBtn1)
                                .addGap(5, 5, 5)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cplusBtn1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 117, Short.MAX_VALUE)
                                .addComponent(discountPriceinput1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(roundedPanel4Layout.createSequentialGroup()
                                .addComponent(cartProductName1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cartProductPanelDeleteBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(17, 17, 17))
        );
        roundedPanel4Layout.setVerticalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cartProductName1)
                    .addComponent(cartProductPanelDeleteBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel4Layout.createSequentialGroup()
                        .addGap(0, 2, Short.MAX_VALUE)
                        .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cminusBtn1)
                            .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cplusBtn1)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(discountPriceinput1))
                .addGap(10, 10, 10)
                .addComponent(priceInput1)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(roundedPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(58, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(roundedPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(156, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel10);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        paymentcombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        paymentcombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        paymentcombo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        jTextField2.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField2.setText("Amount Recieved");

        jLabel22.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel22.setText("Balance Payable:");

        jLabel23.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Rs.1000.00");

        jSeparator2.setForeground(new java.awt.Color(51, 51, 51));

        jLabel14.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        jLabel14.setText("Total:");

        jLabel15.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Rs.1000.00");

        gradientButton1.setText("Complete Sale");
        gradientButton1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N

        jLabel24.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel24.setText("Discount :");

        jLabel26.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel26.setText("Exchange Credit:");

        jLabel25.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 51, 51));
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Rs.1000.00");

        jLabel27.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 102, 0));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("Rs.1000.00");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gradientButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(paymentcombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jSeparator2)
                .addGap(3, 3, 3))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 6, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addComponent(jLabel26))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                            .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(paymentcombo, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel26)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(gradientButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        clearCartBtn.setFont(new java.awt.Font("Nunito ExtraBold", 0, 14)); // NOI18N
        clearCartBtn.setText("D");

        holdBtn.setBackground(new java.awt.Color(255, 51, 51));
        holdBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        holdBtn.setText("H");

        cartCount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 22)); // NOI18N
        cartCount.setText("Cart (01)");

        discountBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        discountBtn.setText("Di");

        switchBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        switchBtn.setText("S");

        creditPay.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        creditPay.setText("P");

        jTextField3.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N

        exchangeBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        exchangeBtn.setText("E");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(cartCount, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(creditPay, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(discountBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(switchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exchangeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(holdBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearCartBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jTextField3)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(exchangeBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(holdBtn)
                                .addComponent(clearCartBtn)
                                .addComponent(switchBtn)
                                .addComponent(discountBtn)
                                .addComponent(creditPay))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(cartCount)
                        .addGap(1, 1, 1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cplusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cplusBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cplusBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cartCount;
    private javax.swing.JLabel cartProductName;
    private javax.swing.JLabel cartProductName1;
    private javax.swing.JButton cartProductPanelDeleteBtn;
    private javax.swing.JButton cartProductPanelDeleteBtn1;
    javax.swing.JButton clearCartBtn;
    private javax.swing.JButton cminusBtn;
    private javax.swing.JButton cminusBtn1;
    private javax.swing.JButton cplusBtn;
    private javax.swing.JButton cplusBtn1;
    javax.swing.JButton creditPay;
    javax.swing.JButton discountBtn;
    private javax.swing.JTextField discountPriceinput;
    private javax.swing.JTextField discountPriceinput1;
    private javax.swing.JButton exchangeBtn;
    private lk.com.pos.privateclasses.GradientButton gradientButton1;
    javax.swing.JButton holdBtn;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JComboBox<String> paymentcombo;
    private javax.swing.JLabel priceInput;
    private javax.swing.JLabel priceInput1;
    private javax.swing.JLabel productCount;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel4;
    javax.swing.JButton switchBtn;
    // End of variables declaration//GEN-END:variables
}
