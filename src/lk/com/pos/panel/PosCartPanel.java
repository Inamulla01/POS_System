package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import lk.com.pos.privateclasses.RoundedPanel;
import java.util.HashMap;
import java.util.Map;
import lk.com.pos.privateclasses.CartItem;
import lk.com.pos.privateclasses.CartListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import lk.com.pos.connection.MySQL;
import lk.com.pos.dialog.AddCredit;
import lk.com.pos.dialog.CardPayDialog;
import lk.com.pos.dialog.DiscountDialog;
import lk.com.pos.dialog.ExchangeProductDialog;
import lk.com.pos.privateclasses.Invoice;
import raven.toast.Notifications;

// ✅ ADD: Import statements at the top
import lk.com.pos.session.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import lk.com.pos.dialog.AddCheque;

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

    private Map<String, CartItem> cartItems = new HashMap<>();
    private JLabel noProductsLabel;
    private CartListener cartListener;

    // ✅ ADDED DISCOUNT FIELDS
    private double appliedDiscountAmount = 0.0;
    private int appliedDiscountTypeId = -1;
    
    // ✅ ADDED EXCHANGE REFUND FIELD
    private double exchangeRefundAmount = 0.0;

    // ✅ ADD: Instance variable for generated sales ID
    private int lastGeneratedSalesId = -1;

    // Interface for callback
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

    this.putClientProperty(FlatClientProperties.STYLE, "arc:15");

    jPanel1.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
    jPanel3.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

    jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.Y_AXIS));
    jPanel10.setBackground(Color.WHITE);

    jPanel2.putClientProperty(FlatClientProperties.STYLE, "arc:20;");
    jPanel2.setBorder(BorderFactory.createEmptyBorder());
    jPanel10.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

    clearCartBtn.addActionListener(evt -> clearCart());

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
    
    // ✅ ADDED: Setup keyboard shortcuts and tooltips (delayed initialization)
    SwingUtilities.invokeLater(() -> {
        setupKeyboardShortcuts();
    });
    
    discountBtn.setToolTipText("Apply discount (Ctrl+D)");
    exchangeBtn.setToolTipText("Process exchange/return (Ctrl+E)");
    switchBtn.setToolTipText("Switch invoice (Ctrl+W)");
    
    // ✅ ADDED: Initially hide discount and exchange refund labels
    jLabel24.setVisible(false); // "Discount:" label
    jLabel25.setVisible(false); // Discount amount
    jLabel25.setText("Rs.0.00");

    jLabel26.setVisible(false); // "Exchange Refund:" label
    jLabel27.setVisible(false); // Exchange refund amount
    jLabel27.setText("Rs.0.00");

    // ✅ ADDED: Setup payment method combo listener for Amount Received visibility
    paymentcombo.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            updateAmountReceivedVisibility();
        }
    });
    
    // ✅ ADDED: Setup search functionality with placeholder
    jTextField3.setText("Search by barcode or product name");
    jTextField3.setForeground(new Color(128, 128, 128)); // Gray color

    jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
        @Override
        public void focusGained(java.awt.event.FocusEvent evt) {
            if (jTextField3.getText().equals("Search by barcode or product name")) {
                jTextField3.setText("");
                jTextField3.setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(java.awt.event.FocusEvent evt) {
            if (jTextField3.getText().isEmpty()) {
                jTextField3.setText("Search by barcode or product name");
                jTextField3.setForeground(new Color(128, 128, 128));
            }
        }
    });

    jTextField3.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filterCartItems();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filterCartItems();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filterCartItems();
        }
    });
    
    // ✅ ADDED: Barcode scanning support
    jTextField3.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            // When Enter is pressed (barcode scanner sends Enter)
            String searchText = jTextField3.getText().trim();
            
            if (!searchText.isEmpty() && !searchText.equals("Search by barcode or product name")) {
                // Try to find product by barcode
                searchAndAddProductByBarcode(searchText);
            }
        }
    });
    
    // ✅ ADDED: Remove the panel border and wrap the search field in a panel with margins
    // Remove any previous border from jPanel2
    jPanel2.setBorder(BorderFactory.createEmptyBorder());
    
    // Create a wrapper panel for the search field with margins
    javax.swing.JPanel searchWrapperPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
    searchWrapperPanel.setBackground(Color.WHITE);
    searchWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
    
    // Remove jTextField3 from its current parent and add it to the wrapper
    if (jTextField3.getParent() != null) {
        java.awt.Container parent = jTextField3.getParent();
        parent.remove(jTextField3);
        searchWrapperPanel.add(jTextField3, java.awt.BorderLayout.CENTER);
        
        // Find the position of jTextField3 in the layout and replace it with the wrapper
        java.awt.Component[] components = parent.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == jTextField3) {
                parent.remove(i);
                parent.add(searchWrapperPanel, i);
                break;
            }
        }
    }
    
    // ✅ ADDED: Initially hide amount received field and balance payable
    updateAmountReceivedVisibility();
}

// ✅ ADDED: Auto-add product by barcode
private void searchAndAddProductByBarcode(String barcode) {
    try {
        String query = "SELECT p.product_id, p.product_name, b.brand_name, s.batch_no, " +
                      "s.qty, s.selling_price, p.barcode " +
                      "FROM product p " +
                      "INNER JOIN brand b ON p.brand_id = b.brand_id " +
                      "INNER JOIN stock s ON p.product_id = s.product_id " +
                      "WHERE p.barcode = ? AND s.qty > 0 " +
                      "ORDER BY s.batch_no ASC LIMIT 1";
        
        PreparedStatement pst = MySQL.getConnection().prepareStatement(query);
        pst.setString(1, barcode);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            // Product found - add to cart
            int productId = rs.getInt("product_id");
            String productName = rs.getString("product_name");
            String brandName = rs.getString("brand_name");
            String batchNo = rs.getString("batch_no");
            int availableQty = rs.getInt("qty");
            double sellingPrice = rs.getDouble("selling_price");
            String foundBarcode = rs.getString("barcode");
            
            // Add to cart
            addToCart(productId, productName, brandName, batchNo, 
                     availableQty, sellingPrice, foundBarcode, 0.0);
            
            // Play beep sound (optional)
            java.awt.Toolkit.getDefaultToolkit().beep();
            
            // Clear search field
            jTextField3.setText("");
            
            Notifications.getInstance().show(
                    Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_RIGHT,
                    "Added: " + productName + " (" + brandName + ")"
            );
        } else {
            // Product not found
            Notifications.getInstance().show(
                    Notifications.Type.WARNING,
                    Notifications.Location.TOP_RIGHT,
                    "Product not found: " + barcode
            );
            jTextField3.selectAll();
        }
        
    } catch (Exception e) {
        System.err.println("Error searching by barcode: " + e.getMessage());
        e.printStackTrace();
    }
}

// ✅ ADDED: Method to apply margins to search bar
private void applySearchBarMargins() {
    if (jTextField3.getParent() != null) {
        // Get the current preferred size
        Dimension currentSize = jTextField3.getPreferredSize();
        // Add 30px to width (15px left + 15px right)
        jTextField3.setPreferredSize(new Dimension(currentSize.width + 30, currentSize.height));
        
        // Also set minimum and maximum sizes to maintain consistency
        jTextField3.setMinimumSize(new Dimension(100 + 30, currentSize.height));
        if (jTextField3.getMaximumSize() != null) {
            jTextField3.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, 
                currentSize.height
            ));
        }
    }
}

    // ✅ ADDED: Filter cart items based on search
    private void filterCartItems() {
        String searchText = jTextField3.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            updateCartPanel();
            return;
        }
        
        jPanel10.removeAll();

        if (cartItems.isEmpty()) {
            showNoProductsMessage();
            return;
        }

        jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(0, 18)));

        boolean foundItems = false;
        for (CartItem item : cartItems.values()) {
            String productName = item.getProductName().toLowerCase();
            String brandName = item.getBrandName().toLowerCase();
            String barcode = item.getBarcode() != null ? item.getBarcode().toLowerCase() : "";
            
            if (productName.contains(searchText) || brandName.contains(searchText) || barcode.contains(searchText)) {
                RoundedPanel cartCard = createCartItemPanel(item);
                jPanel10.add(cartCard);
                jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(18, 18)));
                foundItems = true;
            }
        }

        if (!foundItems) {
            showNoSearchResults();
        }

        jPanel10.revalidate();
        jPanel10.repaint();
    }

    // ✅ ADDED: Show no search results message
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

    // ✅ ADDED: NEW METHOD - Update Amount Received Visibility
    private void updateAmountReceivedVisibility() {
        String selectedPayment = (String) paymentcombo.getSelectedItem();
        
        // Show Amount Received and Balance only for Cash payment
        boolean isCashPayment = selectedPayment != null && 
                               selectedPayment.toLowerCase().contains("cash");
        
        jTextField2.setVisible(isCashPayment);
        jLabel22.setVisible(isCashPayment); // "Balance Payable:" label
        jLabel23.setVisible(isCashPayment); // Balance amount
        
        // Always show discount and exchange refund labels if they have values
        if (appliedDiscountAmount > 0) {
            jLabel24.setVisible(true);
            jLabel25.setVisible(true);
        }
        
        if (exchangeRefundAmount > 0) {
            jLabel26.setVisible(true);
            jLabel27.setVisible(true);
        }
        
        // Clear amount received if not cash payment
        if (!isCashPayment) {
            jTextField2.setText("");
            jLabel23.setText("Rs.0.00");
            jLabel23.setForeground(Color.BLACK);
        }
        
        updateBalance();
    }

    private void loadPaymentMethods() {
        try {
            paymentcombo.removeAllItems();
            paymentcombo.addItem("Select Payment Method");

            Connection connection = lk.com.pos.connection.MySQL.getConnection();
            String query = "SELECT * FROM payment_method ORDER BY payment_method_name ASC";
            PreparedStatement pst = connection.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String paymentMethod = rs.getString("payment_method_name");
                paymentcombo.addItem(paymentMethod);
            }

            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading payment methods: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
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

    public void addToCart(int productId, String productName, String brandName,
            String batchNo, int qty, double sellingPrice, String barcode, double lastPrice) {

        String cartKey = productId + "_" + batchNo;

        if (cartItems.containsKey(cartKey)) {
            CartItem item = cartItems.get(cartKey);

            if (item.getQuantity() < item.getAvailableQty()) {
                item.setQuantity(item.getQuantity() + 1);
                updateCartPanel();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot add more. Available stock: " + item.getAvailableQty(),
                        "Stock Limit",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            CartItem newItem = new CartItem(productId, productName, brandName,
                    batchNo, qty, sellingPrice, barcode, lastPrice);
            cartItems.put(cartKey, newItem);
            updateCartPanel();
        }
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
        jPanel10.removeAll();

        if (cartItems.isEmpty()) {
            showNoProductsMessage();
            return;
        }

        jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(0, 18)));

        for (CartItem item : cartItems.values()) {
            RoundedPanel cartCard = createCartItemPanel(item);
            jPanel10.add(cartCard);
            jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(18, 18)));
        }

        cartCount.setText(String.format("Cart (%02d)", cartItems.size()));

        jPanel10.revalidate();
        jPanel10.repaint();

        updateTotals();
    }

    // ✅ ADDED: Helper method to apply discount value
    private void applyDiscountValue(JTextField discountField, CartItem item) {
        try {
            String text = discountField.getText().trim();
            if (text.isEmpty()) {
                item.setDiscountPrice(0);
                discountField.setText("0");
                updateTotals();
                return;
            }

            double discountInput = Double.parseDouble(text);
            double unitPrice = item.getUnitPrice();
            double lastPrice = item.getLastPrice();
            double finalPricePerUnit = unitPrice - discountInput;

            if (discountInput < 0 || discountInput > unitPrice || finalPricePerUnit < lastPrice) {
                discountField.setText(String.format("%.2f", item.getDiscountPrice()));
                discountField.setForeground(Color.BLACK);

                String message;
                if (discountInput < 0) {
                    message = "Discount cannot be negative";
                } else if (discountInput > unitPrice) {
                    message = "Discount per unit cannot exceed Rs." + String.format("%.2f", unitPrice);
                } else {
                    double maxAllowedDiscount = unitPrice - lastPrice;
                    message = String.format(
                            "Final price (Rs.%.2f) cannot be below last price (Rs.%.2f).\nMaximum allowed discount: Rs.%.2f",
                            finalPricePerUnit, lastPrice, maxAllowedDiscount
                    );
                }

                JOptionPane.showMessageDialog(null, message, "Invalid Discount", JOptionPane.WARNING_MESSAGE);
            } else {
                item.setDiscountPrice(discountInput);
                discountField.setText(String.format("%.2f", discountInput));
                discountField.setForeground(Color.BLACK);
                updateTotals();
            }
        } catch (NumberFormatException e) {
            discountField.setText(String.format("%.2f", item.getDiscountPrice()));
            discountField.setForeground(Color.BLACK);
        }
    }

    private RoundedPanel createCartItemPanel(CartItem item) {
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

        RoundedPanel card = new RoundedPanel(15);
        card.setBackground(CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setPreferredSize(new Dimension(400, 160));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(BORDER_COLOR, 1, 15),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        card.setLayout(new java.awt.BorderLayout(10, 10));

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

        // ✅ UPDATED: Enhanced quantity panel with JTextField
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        qtyPanel.setOpaque(false);

        JButton minusBtn = new JButton("-");
        minusBtn.setFont(new Font("Nunito ExtraBold", Font.PLAIN, 16));
        minusBtn.setPreferredSize(new Dimension(40, 35));
        minusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ✅ CHANGED: Replace JLabel with JTextField
        JTextField qtyField = new JTextField(String.valueOf(item.getQuantity()));
        qtyField.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        qtyField.setHorizontalAlignment(JTextField.CENTER);
        qtyField.setPreferredSize(new Dimension(60, 35));
        qtyField.setToolTipText("Enter quantity (1-" + item.getAvailableQty() + ")");

        // ✅ ADD: Numeric filter for quantity field - only allows digits
        ((AbstractDocument) qtyField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                
                // Only allow digits
                if (string.matches("[0-9]*")) {
                    // Check if resulting text would be valid
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + string + 
                                   currentText.substring(offset);
                    
                    // Prevent leading zeros (except single "0")
                    if (newText.length() > 1 && newText.startsWith("0")) {
                        return;
                    }
                    
                    // Check max length (prevent entering huge numbers)
                    if (newText.length() <= 6) { // Max 999999
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
                
                // Only allow digits
                if (text.matches("[0-9]*")) {
                    // Check if resulting text would be valid
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + text + 
                                   currentText.substring(offset + length);
                    
                    // Prevent leading zeros (except single "0")
                    if (newText.length() > 1 && newText.startsWith("0")) {
                        return;
                    }
                    
                    // Check max length
                    if (newText.length() <= 6) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            }
        });

        // ✅ ADD: Real-time validation with visual feedback
        qtyField.getDocument().addDocumentListener(new DocumentListener() {
            private void validateAndUpdate() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String text = qtyField.getText().trim();
                        
                        // Empty field validation
                        if (text.isEmpty()) {
                            qtyField.setForeground(ERROR_COLOR);
                            qtyField.setToolTipText("Quantity cannot be empty");
                            return;
                        }

                        int newQty = Integer.parseInt(text);
                        
                        // Zero or negative validation
                        if (newQty <= 0) {
                            qtyField.setForeground(ERROR_COLOR);
                            qtyField.setToolTipText("Quantity must be at least 1");
                            return;
                        }
                        
                        // Stock limit validation
                        if (newQty > item.getAvailableQty()) {
                            qtyField.setForeground(ERROR_COLOR);
                            qtyField.setToolTipText("Exceeds available stock: " + item.getAvailableQty());
                            return;
                        }
                        
                        // Valid quantity
                        qtyField.setForeground(SUCCESS_COLOR);
                        qtyField.setToolTipText("Valid quantity (" + newQty + " available)");
                        
                        // Only update if quantity actually changed
                        if (item.getQuantity() != newQty) {
                            item.setQuantity(newQty);
                            updateCartPanel();
                        }
                        
                    } catch (NumberFormatException e) {
                        qtyField.setForeground(ERROR_COLOR);
                        qtyField.setToolTipText("Invalid number format");
                    }
                });
            }

            public void changedUpdate(DocumentEvent e) {
                validateAndUpdate();
            }

            public void removeUpdate(DocumentEvent e) {
                validateAndUpdate();
            }

            public void insertUpdate(DocumentEvent e) {
                validateAndUpdate();
            }
        });

        // ✅ ADD: Focus listener with comprehensive validation
        qtyField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // Select all text when focused for easy editing
                SwingUtilities.invokeLater(() -> qtyField.selectAll());
                qtyField.setToolTipText("Enter quantity (1-" + item.getAvailableQty() + ")");
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String text = qtyField.getText().trim();
                boolean isValid = false;
                String errorMessage = null;
                
                try {
                    // Empty field - reset to 1
                    if (text.isEmpty()) {
                        errorMessage = "Quantity cannot be empty. Reset to 1.";
                        item.setQuantity(1);
                        qtyField.setText("1");
                    } else {
                        int newQty = Integer.parseInt(text);
                        
                        // Zero or negative - reset to 1
                        if (newQty <= 0) {
                            errorMessage = "Quantity must be at least 1. Reset to 1.";
                            item.setQuantity(1);
                            qtyField.setText("1");
                        }
                        // Exceeds stock - reset to max available
                        else if (newQty > item.getAvailableQty()) {
                            errorMessage = String.format(
                                "Quantity exceeds available stock (%d). Set to maximum available.",
                                item.getAvailableQty()
                            );
                            item.setQuantity(item.getAvailableQty());
                            qtyField.setText(String.valueOf(item.getAvailableQty()));
                        }
                        // Valid quantity
                        else {
                            isValid = true;
                            item.setQuantity(newQty);
                            qtyField.setText(String.valueOf(newQty));
                        }
                    }
                } catch (NumberFormatException e) {
                    errorMessage = "Invalid number format. Reset to previous value.";
                    qtyField.setText(String.valueOf(item.getQuantity()));
                }
                
                // Reset color to normal
                qtyField.setForeground(Color.BLACK);
                qtyField.setToolTipText(null);
                
                // Show error message if validation failed
                if (!isValid && errorMessage != null) {
                    JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(qtyField),
                        errorMessage,
                        "Invalid Quantity",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
                
                // Update cart display
                updateCartPanel();
            }
        });

        // ✅ ADD: Key listener for Enter key
        qtyField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    // Transfer focus to trigger validation
                    qtyField.transferFocus();
                } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    // Cancel editing - revert to original value
                    qtyField.setText(String.valueOf(item.getQuantity()));
                    qtyField.setForeground(Color.BLACK);
                    qtyField.transferFocus();
                }
            }
        });

        // ✅ UPDATED: Minus button action with validation
        minusBtn.addActionListener(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                qtyField.setText(String.valueOf(item.getQuantity()));
                qtyField.setForeground(Color.BLACK);
                qtyField.setToolTipText(null);
                updateCartPanel();
            } else {
                // Already at minimum
                qtyField.setForeground(ERROR_COLOR);
                javax.swing.Timer timer = new javax.swing.Timer(500, evt -> {
                    qtyField.setForeground(Color.BLACK);
                });
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

        // ✅ UPDATED: Plus button action with validation
        plusBtn.addActionListener(e -> {
            if (item.getQuantity() < item.getAvailableQty()) {
                item.setQuantity(item.getQuantity() + 1);
                qtyField.setText(String.valueOf(item.getQuantity()));
                qtyField.setForeground(Color.BLACK);
                qtyField.setToolTipText(null);
                updateCartPanel();
            } else {
                // Already at maximum
                qtyField.setForeground(ERROR_COLOR);
                JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(plusBtn),
                    "Cannot add more. Available stock: " + item.getAvailableQty(),
                    "Stock Limit",
                    JOptionPane.WARNING_MESSAGE
                );
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

        ((AbstractDocument) discountField.getDocument()).setDocumentFilter(new NumericDocumentFilter());

        discountField.getDocument().addDocumentListener(new DocumentListener() {
            private void validateDiscount() {
                try {
                    String text = discountField.getText().trim();
                    if (text.isEmpty()) {
                        discountField.setForeground(Color.BLACK);
                        item.setDiscountPrice(0);
                        updateTotals();
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
                        discountField.setToolTipText(String.format(
                                "Cannot go below last price! Purchase price (Rs.%.2f) < Last price (Rs.%.2f). Max discount: Rs.%.2f",
                                finalPricePerUnit, lastPrice, maxAllowedDiscount
                        ));
                    } else {
                        discountField.setForeground(SUCCESS_COLOR);
                        double totalDiscount = discountInput * item.getQuantity();
                        discountField.setToolTipText(String.format("Total discount: Rs.%.2f (%.2f × %d)",
                                totalDiscount, discountInput, item.getQuantity()));
                        item.setDiscountPrice(discountInput);
                        updateTotals();
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

        // ✅ ADD: Enter key listener for instant update
        discountField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    // Apply the discount immediately
                    applyDiscountValue(discountField, item);
                    // Transfer focus to trigger visual feedback
                    discountField.transferFocus();
                } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    // Cancel editing - revert to original value
                    discountField.setText(String.format("%.2f", item.getDiscountPrice()));
                    discountField.setForeground(Color.BLACK);
                    discountField.transferFocus();
                }
            }
        });

        discountField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (discountField.getText().trim().equals("0") || discountField.getText().trim().equals("0.00")) {
                    discountField.setText("");
                }
                // Select all text for easy editing
                SwingUtilities.invokeLater(() -> discountField.selectAll());
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

            JLabel discountInfo = new JLabel(String.format(
                    "Discount: Rs.%.2f × %d = -Rs.%.2f",
                    discountPerUnit, item.getQuantity(), totalDiscount
            ));
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

    private void removeFromCart(String cartKey) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove this item from cart?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.remove(cartKey);
            updateCartPanel();
        }
    }

    private void clearCart() {
        if (cartItems.isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the cart?",
                "Clear Cart",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.clear();
            jTextField2.setText("");
            
            // ✅ FIXED: Clear discount and exchange refund when clearing cart
            appliedDiscountAmount = 0.0;
            appliedDiscountTypeId = -1;
            exchangeRefundAmount = 0.0;
            
            updateCartPanel();
        }
    }

    // ✅ FIXED: Updated updateBalance() method
    private void updateBalance() {
        try {
            String amountText = jTextField2.getText().trim();
            double total = getTotal(); // This already includes discount and exchange refund

            if (!amountText.isEmpty()) {
                double amountReceived = Double.parseDouble(amountText);
                double balance = amountReceived - total;

                jLabel23.setText(String.format("Rs.%.2f", balance));

                if (balance < 0) {
                    jLabel23.setForeground(ERROR_COLOR);
                } else if (balance > 0) {
                    jLabel23.setForeground(SUCCESS_COLOR);
                } else {
                    jLabel23.setForeground(Color.BLACK);
                }
            } else {
                jLabel23.setText("Rs.0.00");
                jLabel23.setForeground(Color.BLACK);
            }
        } catch (NumberFormatException e) {
            jLabel23.setText("Rs.0.00");
            jLabel23.setForeground(Color.BLACK);
        }
    }

    // ✅ FIXED: Updated updateTotals() method
    private void updateTotals() {
        double subtotal = 0;

        // Calculate subtotal from cart items
        for (CartItem item : cartItems.values()) {
            subtotal += item.getTotalPrice();
        }
        
        // Apply discount to subtotal
        double totalAfterDiscount = subtotal - appliedDiscountAmount;
        if (totalAfterDiscount < 0) totalAfterDiscount = 0;
        
        // Add exchange refund to get final total
        double finalTotal = totalAfterDiscount + exchangeRefundAmount;

        // Update total label
        jLabel15.setText(String.format("Rs.%.2f", finalTotal));

        // Update discount label visibility and value
        if (appliedDiscountAmount > 0) {
            jLabel25.setText(String.format("-Rs.%.2f", appliedDiscountAmount));
            jLabel25.setVisible(true);
            jLabel24.setVisible(true); // "Discount:" label
        } else {
            jLabel25.setText("Rs.0.00");
            jLabel25.setVisible(false);
            jLabel24.setVisible(false);
        }
        
        // Update exchange refund label visibility and value
        if (exchangeRefundAmount > 0) {
            jLabel27.setText(String.format("+Rs.%.2f", exchangeRefundAmount));
            jLabel27.setVisible(true);
            jLabel26.setVisible(true); // "Exchange Refund:" label
        } else {
            jLabel27.setText("Rs.0.00");
            jLabel27.setVisible(false);
            jLabel26.setVisible(false);
        }

        // Update balance
        updateBalance();

        // Notify listener
        if (cartListener != null) {
            cartListener.onCartUpdated(finalTotal, cartItems.size());
        }
    }

    public Map<String, CartItem> getCartItems() {
        return new HashMap<>(cartItems);
    }

    // ✅ FIXED: Updated getTotal() method
    public double getTotal() {
        double total = 0;
        
        // Calculate subtotal from cart items
        for (CartItem item : cartItems.values()) {
            total += item.getTotalPrice();
        }
        
        // Apply discount (subtract)
        total -= appliedDiscountAmount;
        if (total < 0) total = 0;
        
        // Add exchange refund (add)
        total += exchangeRefundAmount;
        
        return total;
    }

//    // ✅ ADD: Show sale summary dialog
//    private boolean showSaleSummary() {
//        StringBuilder summary = new StringBuilder();
//        summary.append("<html><body style='font-family: Nunito; font-size: 12px;'>");
//        summary.append("<h2 style='color: #0893B0;'>Sale Summary</h2>");
//        summary.append("<table cellpadding='5'>");
//        
//        double subtotal = 0;
//        for (CartItem item : cartItems.values()) {
//            subtotal += item.getUnitPrice() * item.getQuantity();
//            summary.append(String.format(
//                "<tr><td>%s (%s)</td><td>x%d</td><td align='right'>Rs.%.2f</td></tr>",
//                item.getProductName(), item.getBrandName(), 
//                item.getQuantity(), item.getTotalPrice()
//            ));
//        }
//        
//        summary.append("<tr><td colspan='3'><hr></td></tr>");
//        summary.append(String.format("<tr><td><b>Subtotal:</b></td><td></td><td align='right'><b>Rs.%.2f</b></td></tr>", subtotal));
//        
//        if (appliedDiscountAmount > 0) {
//            summary.append(String.format("<tr style='color: red;'><td>Discount:</td><td></td><td align='right'>-Rs.%.2f</td></tr>", appliedDiscountAmount));
//        }
//        
//        if (exchangeRefundAmount > 0) {
//            summary.append(String.format("<tr style='color: orange;'><td>Exchange Refund:</td><td></td><td align='right'>+Rs.%.2f</td></tr>", exchangeRefundAmount));
//        }
//        
//        summary.append(String.format("<tr><td colspan='3'><hr></td></tr>"));
//        summary.append(String.format("<tr><td><b>TOTAL:</b></td><td></td><td align='right'><b style='color: #0893B0; font-size: 16px;'>Rs.%.2f</b></td></tr>", getTotal()));
//        
//        summary.append("</table>");
//        summary.append("<br><p>Payment Method: <b>" + paymentcombo.getSelectedItem() + "</b></p>");
//        summary.append("</body></html>");
//        
//        int confirm = JOptionPane.showConfirmDialog(this,
//                new JLabel(summary.toString()),
//                "Confirm Sale",
//                JOptionPane.YES_NO_OPTION,
//                JOptionPane.QUESTION_MESSAGE);
//        
//        return confirm == JOptionPane.YES_OPTION;
//    }

    // ✅ MODIFIED: Updated to use discounted total
    public boolean validateCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cart is empty. Add products before checkout.",
                    "Empty Cart",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String selectedPayment = (String) paymentcombo.getSelectedItem();
        if (selectedPayment == null || selectedPayment.equals("Select Payment Method")) {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment method.",
                    "Missing Payment Method",
                    JOptionPane.WARNING_MESSAGE);
            paymentcombo.requestFocus();
            return false;
        }

        // ✅ FIXED: Only validate amount for cash payments
        boolean isCashPayment = selectedPayment.toLowerCase().contains("cash");
        if (isCashPayment) {
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
                double total = getTotal(); // This now includes discount and exchange refund

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

        return true;
    }

    // ✅ FIXED: Updated resetCart() method
    public void resetCart() {
        cartItems.clear();
        jTextField2.setText("");
        paymentcombo.setSelectedIndex(0);
        
        // Clear discount
        appliedDiscountAmount = 0.0;
        appliedDiscountTypeId = -1;
        
        // Clear exchange refund
        exchangeRefundAmount = 0.0;
        
        // Reset label visibility and values
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
    }

    /**
     * Setup keyboard shortcuts
     */
    private void setupKeyboardShortcuts() {
        // Wait until the component is properly initialized
        SwingUtilities.invokeLater(() -> {
            if (getRootPane() != null) {
                // Ctrl+D for Discount
                getRootPane().registerKeyboardAction(
                    evt -> discountBtnActionPerformed(null),
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
                );

                // Ctrl+E for Exchange
                getRootPane().registerKeyboardAction(
                    evt -> exchangeBtnActionPerformed(null),
                    KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
                );

                // Ctrl+W for Switch (already exists)
                getRootPane().registerKeyboardAction(
                    evt -> showSwitchInvoicePanel(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
                );
            }
        });
    }

    // Enhanced dialog positioning to ensure it stays on screen
    private void positionDialog(javax.swing.JDialog dialog, java.awt.Component relativeTo) {
        java.awt.Point buttonLoc = relativeTo.getLocationOnScreen();
        int dialogWidth = dialog.getPreferredSize().width;
        int dialogHeight = dialog.getPreferredSize().height;

        // Get screen dimensions
        java.awt.GraphicsConfiguration gc = relativeTo.getGraphicsConfiguration();
        java.awt.Rectangle screenBounds = gc.getBounds();

        // Calculate initial position (centered below the button)
        int x = buttonLoc.x - (dialogWidth - relativeTo.getWidth()) / 2;
        int y = buttonLoc.y + relativeTo.getHeight();

        // Ensure dialog stays within screen bounds
        if (x + dialogWidth > screenBounds.x + screenBounds.width) {
            x = screenBounds.x + screenBounds.width - dialogWidth - 10; // 10px margin from right edge
        }
        if (x < screenBounds.x) {
            x = screenBounds.x + 10; // 10px margin from left edge
        }
        if (y + dialogHeight > screenBounds.y + screenBounds.height) {
            y = screenBounds.y + screenBounds.height - dialogHeight - 10; // 10px margin from bottom
            // If it still doesn't fit, try above the button
            if (y < buttonLoc.y) {
                y = buttonLoc.y - dialogHeight - 10; // 10px above the button
            }
        }
        if (y < screenBounds.y) {
            y = screenBounds.y + 10; // 10px margin from top
        }

        dialog.setLocation(x, y);
    }

    // ENHANCED SWITCH INVOICE PANEL WITH JDialog FOR BETTER CLICK-OUTSIDE BEHAVIOR
    private void showSwitchInvoicePanel() {
        loadRecentInvoices();
        invoiceCardsList.clear();
        currentFocusedIndex = -1;

        // Create a dialog instead of popup for better behavior
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        notificationDialog = new javax.swing.JDialog(parentFrame, false); // non-modal
        notificationDialog.setUndecorated(true);
        notificationDialog.setPreferredSize(new Dimension(480, 500));
        notificationDialog.setBackground(new Color(0, 0, 0, 0)); // Transparent background

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(0x1CB5BB), 2));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
        headerPanel.setBackground(new Color(0x1CB5BB));

        JLabel titleLabel = new JLabel("Switch Invoice");
        titleLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        // Right side panel for buttons
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtonPanel.setBackground(new Color(0x1CB5BB));

        // Refresh button
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

        // Help button
        JButton helpButton = new JButton("?");
        helpButton.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        helpButton.setForeground(Color.WHITE);
        helpButton.setBackground(new Color(0x1CB5BB));
        helpButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        helpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        helpButton.setToolTipText("Keyboard Help");
        helpButton.addActionListener(e -> showKeyboardHelp());

        // Close icon
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

        // Subtitle Panel
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

        // Content Panel with Scroll
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

        // Create a wrapper panel to hold subtitle and scroll pane
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(subtitlePanel, BorderLayout.NORTH);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);

        notificationDialog.add(mainPanel);
        notificationDialog.pack();

        // Enhanced keyboard setup
        setupEnhancedKeyboardNavigation(scrollPane);

        // Use the new positioning method
        positionDialog(notificationDialog, switchBtn);

        // Enhanced window focus listener to close when focus is lost
        notificationDialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            private boolean gainedFocusOnce = false;

            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                gainedFocusOnce = true;
            }

            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                // Only close if focus was gained at least once (to handle initial focus issues)
                if (gainedFocusOnce) {
                    // Small delay to handle click events properly
                    javax.swing.Timer timer = new javax.swing.Timer(100, evt -> {
                        if (!notificationDialog.isFocusOwner()
                                && !isChildComponentFocused(notificationDialog)) {
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

                // Check if the focused component is within our dialog
                return javax.swing.SwingUtilities.isDescendingFrom(focusOwner, window);
            }
        });

        notificationDialog.setVisible(true);

        // Auto-focus first item if available
        if (!invoiceCardsList.isEmpty()) {
            setFocusedCard(0, scrollPane);
        }
    }

    // Enhanced keyboard setup
    private void setupEnhancedKeyboardNavigation(JScrollPane scrollPane) {
        // Create key listener
        java.awt.event.KeyListener keyListener = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleEnhancedKeyboardNavigation(evt, scrollPane);
            }
        };

        // Add to dialog
        notificationDialog.addKeyListener(keyListener);
        notificationDialog.setFocusable(true);

        // Request focus
        SwingUtilities.invokeLater(() -> {
            notificationDialog.requestFocusInWindow();
        });
    }

    // Enhanced keyboard navigation handler
    private void handleEnhancedKeyboardNavigation(java.awt.event.KeyEvent evt, JScrollPane scrollPane) {
        if (invoiceCardsList.isEmpty()) {
            return;
        }

        int keyCode = evt.getKeyCode();

        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_DOWN:
                // Move down with wrap-around
                if (currentFocusedIndex < invoiceCardsList.size() - 1) {
                    setFocusedCard(currentFocusedIndex + 1, scrollPane);
                } else {
                    setFocusedCard(0, scrollPane); // Wrap to top
                }
                evt.consume();
                break;

            case java.awt.event.KeyEvent.VK_UP:
                // Move up with wrap-around
                if (currentFocusedIndex > 0) {
                    setFocusedCard(currentFocusedIndex - 1, scrollPane);
                } else {
                    setFocusedCard(invoiceCardsList.size() - 1, scrollPane); // Wrap to bottom
                }
                evt.consume();
                break;

            case java.awt.event.KeyEvent.VK_ENTER:
                // Select current invoice
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
                // Close dialog
                notificationDialog.setVisible(false);
                notificationDialog.dispose();
                evt.consume();
                break;

            case java.awt.event.KeyEvent.VK_HOME:
                // Jump to first item
                if (!invoiceCardsList.isEmpty()) {
                    setFocusedCard(0, scrollPane);
                }
                evt.consume();
                break;

            case java.awt.event.KeyEvent.VK_END:
                // Jump to last item
                if (!invoiceCardsList.isEmpty()) {
                    setFocusedCard(invoiceCardsList.size() - 1, scrollPane);
                }
                evt.consume();
                break;

            case java.awt.event.KeyEvent.VK_PAGE_DOWN:
                // Page down - move by 5 items
                if (!invoiceCardsList.isEmpty()) {
                    int newIndex = Math.min(currentFocusedIndex + 5, invoiceCardsList.size() - 1);
                    setFocusedCard(newIndex, scrollPane);
                }
                evt.consume();
                break;

            case java.awt.event.KeyEvent.VK_PAGE_UP:
                // Page up - move by 5 items
                if (!invoiceCardsList.isEmpty()) {
                    int newIndex = Math.max(currentFocusedIndex - 5, 0);
                    setFocusedCard(newIndex, scrollPane);
                }
                evt.consume();
                break;

            case java.awt.event.KeyEvent.VK_F1:
                // Show help
                showKeyboardHelp();
                evt.consume();
                break;
        }
    }

    private void setFocusedCard(int index, JScrollPane scrollPane) {
        if (index < 0 || index >= invoiceCardsList.size()) {
            return;
        }

        // Remove focus from previous card
        if (currentFocusedIndex >= 0 && currentFocusedIndex < invoiceCardsList.size()) {
            JPanel prevCard = invoiceCardsList.get(currentFocusedIndex);
            resetCardAppearance(prevCard, recentInvoices.get(currentFocusedIndex));
        }

        // Set focus to new card
        currentFocusedIndex = index;
        JPanel currentCard = invoiceCardsList.get(currentFocusedIndex);
        Invoice invoice = recentInvoices.get(currentFocusedIndex);
        applyFocusedCardAppearance(currentCard, invoice);

        // Scroll to make the focused card visible
        SwingUtilities.invokeLater(() -> {
            currentCard.scrollRectToVisible(currentCard.getBounds());
        });
    }

    private void resetCardAppearance(JPanel card, Invoice invoice) {
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0E0E0), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Reset all panel backgrounds
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
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(11, 15, 11, 15)
        ));

        // Set all panel backgrounds
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

    // Keyboard help method
    private void showKeyboardHelp() {
        String helpText = "<html><div style='text-align: center;'><b>Keyboard Shortcuts</b></div><br>"
                + "• <b>↑ / ↓</b> - Navigate invoices<br>"
                + "• <b>Enter</b> - Select invoice<br>"
                + "• <b>Home</b> - First invoice<br>"
                + "• <b>End</b> - Last invoice<br>"
                + "• <b>Page Up/Down</b> - Jump 5 invoices<br>"
                + "• <b>F1</b> - Show this help<br>"
                + "• <b>Escape</b> - Close window</html>";

        JOptionPane.showMessageDialog(this, helpText, "Keyboard Help",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createInvoiceCard(Invoice invoice, int index) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0E0E0), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(460, 100));

        // Left side - Invoice info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Invoice number and status
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

        // Date and amount
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

        // Right side - Action button
        String buttonText = getButtonTextBasedOnStatus(invoice.getStatus());
        GradientActionButton actionButton = new GradientActionButton(buttonText, invoice);
        actionButton.addActionListener(e -> {
            if (notificationDialog != null) {
                notificationDialog.setVisible(false);
                notificationDialog.dispose();
            }
            handleInvoiceAction(invoice, buttonText);
        });

        // Get button colors based on status
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
                    card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borderColor, 2),
                            BorderFactory.createEmptyBorder(11, 15, 11, 15)
                    ));
                    infoPanel.setBackground(new Color(0xF8F9FA));
                    topInfoPanel.setBackground(new Color(0xF8F9FA));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (currentFocusedIndex != index) {
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)
                    ));
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

    // Custom gradient button class
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

            // Set initial border and text color
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

                // Update icon color on hover
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
                System.err.println("Error loading icon: " + iconPath + " - " + e.getMessage());
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

    private void handleSwitchAction(Invoice invoice) {
        int response = JOptionPane.showConfirmDialog(this,
                "Switch to invoice: " + invoice.getInvoiceNo() + "?\n"
                + "Amount: Rs. " + String.format("%.2f", invoice.getTotal()) + "\n"
                + "Date: " + invoice.getDate(),
                "Switch Invoice",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            openInvoicePanel(invoice);
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "Successfully switched to invoice: " + invoice.getInvoiceNo());
        }
    }

    private void handleOpenAction(Invoice invoice) {
        JOptionPane.showMessageDialog(this,
                "Opening invoice: " + invoice.getInvoiceNo() + "\n"
                + "This invoice is in " + invoice.getStatus() + " status.",
                "Open Invoice",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDefaultAction(Invoice invoice, String action) {
        JOptionPane.showMessageDialog(this,
                action + " action for invoice: " + invoice.getInvoiceNo(),
                action + " Invoice",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public Invoice getSelectedInvoice() {
        return selectedInvoice;
    }

    public boolean isInvoiceSelected() {
        return invoiceSelected;
    }

    private void loadRecentInvoices() {
        recentInvoices = new ArrayList<>();

        try {
            String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, "
                    + "s.status_id, st.status_type, "
                    + "u.user_id "
                    + "FROM sales s "
                    + "INNER JOIN i_status st ON s.status_id = st.status_id "
                    + "INNER JOIN user u ON s.user_id = u.user_id "
                    + "WHERE s.datetime >= DATE_SUB(NOW(), INTERVAL 24 HOUR) "
                    + "ORDER BY "
                    + "CASE "
                    + "    WHEN st.status_type = 'Hold' THEN 1 "
                    + "    WHEN st.status_type = 'Completed' THEN 2 "
                    + "    ELSE 3 "
                    + "END, "
                    + "s.datetime DESC";

            ResultSet rs = MySQL.executeSearch(query);

            while (rs.next()) {
                Invoice invoice = new Invoice(
                        rs.getInt("sales_id"),
                        rs.getString("invoice_no"),
                        rs.getTimestamp("datetime"),
                        rs.getString("status_type"),
                        rs.getDouble("total"),
                        null,
                        "Cash",
                        rs.getInt("user_id")
                );
                recentInvoices.add(invoice);
            }

        } catch (Exception e) {
            System.err.println("Error loading invoices: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading invoices: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openInvoicePanel(Invoice invoice) {
        cartItems.clear();
        loadInvoiceItems(invoice);
        updateCartPanel();
    }

    private void handleViewAction(Invoice invoice) {
        try {
            String detailQuery
                    = "SELECT si.qty, p.product_name, b.brand_name, si.price, si.discount_price, si.total "
                    + "FROM sale_item si "
                    + "INNER JOIN stock st ON si.stock_id = st.stock_id "
                    + "INNER JOIN product p ON st.product_id = p.product_id "
                    + "INNER JOIN brand b ON p.brand_id = b.brand_id "
                    + "WHERE si.sales_id = " + invoice.getSalesId();

            ResultSet rs = MySQL.executeSearch(detailQuery);

            StringBuilder details = new StringBuilder();
            details.append("Invoice Details:\n");
            details.append("ID: ").append(invoice.getInvoiceNo()).append("\n");
            details.append("Status: ").append(invoice.getStatus()).append("\n");
            details.append("Amount: Rs. ").append(String.format("%.2f", invoice.getTotal())).append("\n");
            details.append("Date: ").append(invoice.getDate()).append("\n\n");
            details.append("Items:\n");

            double itemTotal = 0;
            while (rs.next()) {
                String productName = rs.getString("product_name");
                String brandName = rs.getString("brand_name");
                int qty = rs.getInt("qty");
                double price = rs.getDouble("price");
                double discount = rs.getDouble("discount_price");
                double total = rs.getDouble("total");

                details.append(String.format("- %s (%s) x%d: Rs. %.2f (Discount: Rs. %.2f) = Rs. %.2f\n",
                        productName, brandName, qty, price, discount, total));
                itemTotal += total;
            }

            details.append("\nTotal: Rs. ").append(String.format("%.2f", itemTotal));

            JOptionPane.showMessageDialog(this,
                    details.toString(),
                    "View Invoice - " + invoice.getInvoiceNo(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading invoice details: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInvoiceItems(Invoice invoice) {
        try {
            String detailQuery
                    = "SELECT si.qty, p.product_name, b.brand_name, st.batch_no, "
                    + "si.price, si.discount_price, si.total, p.barcode, " // Fixed: changed st.barcode to p.barcode
                    + "st.selling_price as last_price, p.product_id, st.stock_id "
                    + "FROM sale_item si "
                    + "INNER JOIN stock st ON si.stock_id = st.stock_id "
                    + "INNER JOIN product p ON st.product_id = p.product_id "
                    + "INNER JOIN brand b ON p.brand_id = b.brand_id "
                    + "WHERE si.sales_id = " + invoice.getSalesId();

            ResultSet rs = MySQL.executeSearch(detailQuery);

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                String brandName = rs.getString("brand_name");
                String batchNo = rs.getString("batch_no");
                int qty = rs.getInt("qty");
                double sellingPrice = rs.getDouble("price");
                String barcode = rs.getString("barcode");  // Now this will work
                double lastPrice = rs.getDouble("last_price");
                double discountPrice = rs.getDouble("discount_price");

                String cartKey = productId + "_" + batchNo;

                CartItem item = new CartItem(productId, productName, brandName,
                        batchNo, qty, sellingPrice, barcode, lastPrice);
                item.setDiscountPrice(discountPrice);

                cartItems.put(cartKey, item);
            }

            updateTotals();

        } catch (Exception e) {
            System.err.println("Error loading invoice items: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading invoice items: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ✅ FIXED: Updated exchangeBtnActionPerformed() method to properly add exchange item price to refund
    
    // ✅ ADDED: NEW METHOD - Get Refund Amount
    private double getRefundAmountFromReturn(int returnId) {
        try {
            String query = "SELECT total_return_amount FROM `return` WHERE return_id = " + returnId;
            ResultSet rs = MySQL.executeSearch(query);
            
            if (rs.next()) {
                return rs.getDouble("total_return_amount");
            }
        } catch (Exception e) {
            System.err.println("Error getting refund amount: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    // ✅ ADDED: GETTER METHODS FOR DISCOUNT AND EXCHANGE
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

    // ✅ ADD: Main Complete Sale Handler
    private void handleCompleteSale() {
        // Validate checkout first
        if (!validateCheckout()) {
            return;
        }
//        
//        // Show summary before proceeding
//        if (!showSaleSummary()) {
//            return;
//        }
        
        String selectedPayment = (String) paymentcombo.getSelectedItem();
        
        // Check payment method selection
        if (selectedPayment == null || selectedPayment.equals("Select Payment Method")) {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment method before completing the sale.",
                    "Missing Payment Method",
                    JOptionPane.WARNING_MESSAGE);
            paymentcombo.requestFocus();
            return;
        }
        
        // Handle Cash Payment - Direct save
        if (selectedPayment.toLowerCase().contains("cash")) {
            handleCashPayment();
            return;
        }
        
        // For other payment methods, save sale first then open dialog
        int salesId = saveSale(1); // status_id = 1 for Completed
        
        if (salesId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save sale. Please try again.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Open appropriate payment dialog based on selection
        if (selectedPayment.toLowerCase().contains("credit")) {
            openCreditPaymentDialog(salesId);
        } else if (selectedPayment.toLowerCase().contains("card")) {
            openCardPaymentDialog(salesId);
        } else if (selectedPayment.toLowerCase().contains("cheque")) {
            openChequePaymentDialog(salesId);
        } else {
            // Unknown payment method - show success and clear cart
            Notifications.getInstance().show(
                    Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_RIGHT,
                    "Sale completed successfully! Sale ID: " + salesId
            );
            resetCart();
            if (cartListener != null) {
                cartListener.onCartUpdated(0, 0);
            }
        }
    }

    // ✅ ADD: Hold Sale Handler
    private void handleHoldSale() {
        // Basic validation - just check if cart has items
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cart is empty. Add products before placing on hold.",
                    "Empty Cart",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirm hold action
        int confirm = JOptionPane.showConfirmDialog(this,
                "Place this sale on hold?\n" +
                "You can switch back to this invoice later.",
                "Hold Sale",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Save sale with Hold status (status_id = 2)
        int salesId = saveSale(2);
        
        if (salesId != -1) {
            Notifications.getInstance().show(
                    Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_RIGHT,
                    "Sale placed on hold! Sale ID: " + salesId
            );
            
            // Clear cart after successful hold
            resetCart();
            if (cartListener != null) {
                cartListener.onCartUpdated(0, 0);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to place sale on hold. Please try again.",
                    "Hold Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ✅ ADD: Cash Payment Handler
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
            
            // Confirm cash payment
            String confirmMessage = String.format(
                    "Complete cash payment?\n\n" +
                    "Total: Rs.%.2f\n" +
                    "Received: Rs.%.2f\n" +
                    "Balance: Rs.%.2f",
                    total, amountReceived, balance
            );
            
            int confirm = JOptionPane.showConfirmDialog(this,
                    confirmMessage,
                    "Confirm Cash Payment",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Save sale with Completed status
            int salesId = saveSale(1);
            
            if (salesId != -1) {
                // Show success message with balance
                String successMessage = String.format(
                        "Cash payment completed successfully!\n\n" +
                        "Sale ID: %d\n" +
                        "Total: Rs.%.2f\n" +
                        "Received: Rs.%.2f\n" +
                        "Change: Rs.%.2f",
                        salesId, total, amountReceived, balance
                );
                
                JOptionPane.showMessageDialog(this,
                        successMessage,
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                
                Notifications.getInstance().show(
                        Notifications.Type.SUCCESS,
                        Notifications.Location.TOP_RIGHT,
                        "Cash payment completed! Sale ID: " + salesId
                );
                
                // Clear cart
                resetCart();
                if (cartListener != null) {
                    cartListener.onCartUpdated(0, 0);
                }
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid amount received. Please enter a valid number.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            jTextField2.requestFocus();
            jTextField2.selectAll();
        }
    }

    // ✅ ADD: Update Stock Quantity Method
    private boolean updateStockQuantities(Connection conn) throws Exception {
        PreparedStatement pstUpdateStock = null;
        PreparedStatement pstCheckStock = null;
        
        try {
            // First, verify all stock quantities are sufficient
            String checkQuery = "SELECT stock_id, qty FROM stock WHERE stock_id = ?";
            pstCheckStock = conn.prepareStatement(checkQuery);
            
            for (CartItem item : cartItems.values()) {
                int stockId = getStockId(item.getProductId(), item.getBatchNo());
                
                if (stockId == -1) {
                    throw new Exception("Stock not found for: " + item.getProductName() + " (Batch: " + item.getBatchNo() + ")");
                }
                
                // Check current stock
                pstCheckStock.setInt(1, stockId);
                ResultSet rs = pstCheckStock.executeQuery();
                
                if (rs.next()) {
                    int currentQty = rs.getInt("qty");
                    int requestedQty = item.getQuantity();
                    
                    if (currentQty < requestedQty) {
                        throw new Exception(
                            String.format("Insufficient stock for %s!\nAvailable: %d, Required: %d",
                            item.getProductName(), currentQty, requestedQty)
                        );
                    }
                } else {
                    throw new Exception("Stock record not found for: " + item.getProductName());
                }
            }
            
            // All stock quantities verified - now update them
            String updateQuery = "UPDATE stock SET qty = qty - ? WHERE stock_id = ?";
            pstUpdateStock = conn.prepareStatement(updateQuery);
            
            for (CartItem item : cartItems.values()) {
                int stockId = getStockId(item.getProductId(), item.getBatchNo());
                
                pstUpdateStock.setInt(1, item.getQuantity());
                pstUpdateStock.setInt(2, stockId);
                pstUpdateStock.addBatch();
                
                System.out.println(String.format("Updating stock: Product=%s, Batch=%s, Qty Sold=%d, Stock ID=%d",
                    item.getProductName(), item.getBatchNo(), item.getQuantity(), stockId));
            }
            
            int[] updateResults = pstUpdateStock.executeBatch();
            
            // Verify all updates succeeded
            for (int result : updateResults) {
                if (result <= 0) {
                    throw new Exception("Failed to update stock quantity for one or more items");
                }
            }
            
            System.out.println("All stock quantities updated successfully!");
            return true;
            
        } finally {
            if (pstCheckStock != null) pstCheckStock.close();
            if (pstUpdateStock != null) pstUpdateStock.close();
        }
    }

    // ✅ ADD: Generate Invoice Number Method
    private String generateInvoiceNumber() {
        try {
            String query = "SELECT invoice_no FROM sales ORDER BY sales_id DESC LIMIT 1";
            ResultSet rs = MySQL.executeSearch(query);
            
            if (rs.next()) {
                String lastInvoice = rs.getString("invoice_no");
                if (lastInvoice != null && lastInvoice.startsWith("INV")) {
                    // Extract number from INV00001 format
                    int lastNumber = Integer.parseInt(lastInvoice.substring(3));
                    int newNumber = lastNumber + 1;
                    return String.format("INV%05d", newNumber);
                }
            }
            
            // First invoice
            return "INV00001";
            
        } catch (Exception e) {
            System.err.println("Error generating invoice number: " + e.getMessage());
            e.printStackTrace();
            // Fallback to timestamp-based invoice
            return "INV" + System.currentTimeMillis();
        }
    }

    // ✅ FIXED: Save Sale Method - Updated to match actual database schema
private int saveSale(int statusId) {
    Connection conn = null;
    PreparedStatement pstSale = null;
    PreparedStatement pstSaleItem = null;
    ResultSet generatedKeys = null;
    int generatedSalesId = -1;
    
    try {
        // Get session user ID
        int userId = Session.getInstance().getUserId();
        
        if (userId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "User session not found. Please log in again.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        
        // Get payment method ID
        String selectedPayment = (String) paymentcombo.getSelectedItem();
        int paymentMethodId = getPaymentMethodId(selectedPayment);
        
        if (paymentMethodId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Invalid payment method selected.",
                    "Payment Method Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        
        // Calculate totals
        double total = getTotal();
        
        // Start transaction
        conn = MySQL.getConnection();
        conn.setAutoCommit(false);
        
        // ✅ Update stock quantities FIRST
        try {
            updateStockQuantities(conn);
        } catch (Exception stockError) {
            throw new Exception("Stock update failed: " + stockError.getMessage());
        }
        
        // ✅ FIXED: Insert into sales table - using ONLY columns that exist
        // Based on your schema: sales_id, datetime, total, user_id, payment_method_id, invoice_no, status_id, discount_id
        String salesQuery = "INSERT INTO sales (invoice_no, datetime, total, user_id, payment_method_id, status_id, discount_id) " +
                           "VALUES (?, NOW(), ?, ?, ?, ?, ?)";
        
        pstSale = conn.prepareStatement(salesQuery, Statement.RETURN_GENERATED_KEYS);
        pstSale.setString(1, generateInvoiceNumber());
        pstSale.setDouble(2, total);
        pstSale.setInt(3, userId);
        pstSale.setInt(4, paymentMethodId);
        pstSale.setInt(5, statusId);
        
        // ✅ FIXED: Handle discount_id (references discount table)
        // If no discount applied, set to NULL
        if (appliedDiscountTypeId > 0 && appliedDiscountAmount > 0) {
            // Create a discount record first and get its ID
            int discountId = createDiscountRecord(conn, appliedDiscountTypeId, appliedDiscountAmount);
            if (discountId > 0) {
                pstSale.setInt(6, discountId);
            } else {
                pstSale.setNull(6, java.sql.Types.INTEGER);
            }
        } else {
            pstSale.setNull(6, java.sql.Types.INTEGER);
        }
        
        int rowsAffected = pstSale.executeUpdate();
        
        if (rowsAffected > 0) {
            // Get generated sales ID
            generatedKeys = pstSale.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedSalesId = generatedKeys.getInt(1);
                lastGeneratedSalesId = generatedSalesId;
                
                // Insert sale items
                String saleItemQuery = "INSERT INTO sale_item (qty, price, discount_price, total, sales_id, stock_id) " +
                                      "VALUES (?, ?, ?, ?, ?, ?)";
                
                pstSaleItem = conn.prepareStatement(saleItemQuery);
                
                for (CartItem item : cartItems.values()) {
                    int stockId = getStockId(item.getProductId(), item.getBatchNo());
                    
                    if (stockId == -1) {
                        throw new Exception("Stock not found for product: " + item.getProductName() + " (Batch: " + item.getBatchNo() + ")");
                    }
                    
                    pstSaleItem.setInt(1, item.getQuantity());
                    pstSaleItem.setDouble(2, item.getUnitPrice());
                    pstSaleItem.setDouble(3, item.getDiscountPrice());
                    pstSaleItem.setDouble(4, item.getTotalPrice());
                    pstSaleItem.setInt(5, generatedSalesId);
                    pstSaleItem.setInt(6, stockId);
                    
                    pstSaleItem.addBatch();
                }
                
                pstSaleItem.executeBatch();
                
                // ✅ If exchange refund exists, create a record in the return table
                if (exchangeRefundAmount > 0) {
                    createExchangeRefundRecord(conn, generatedSalesId, exchangeRefundAmount);
                }
                
                // Commit transaction
                conn.commit();
                
                System.out.println("Sale saved successfully! Sales ID: " + generatedSalesId + ", Status ID: " + statusId);
                System.out.println("Stock quantities decreased for all items.");
                
                return generatedSalesId;
            } else {
                throw new Exception("Failed to retrieve generated sales ID");
            }
        } else {
            throw new Exception("Failed to insert sale record");
        }
        
    } catch (Exception e) {
        // Rollback on error
        try {
            if (conn != null) {
                conn.rollback();
                System.out.println("Transaction rolled back - stock quantities restored");
            }
        } catch (Exception rollbackEx) {
            rollbackEx.printStackTrace();
        }
        
        System.err.println("Error saving sale: " + e.getMessage());
        e.printStackTrace();
        
        JOptionPane.showMessageDialog(this,
                "Error saving sale: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        
        return -1;
        
    } finally {
        try {
            if (generatedKeys != null) generatedKeys.close();
            if (pstSaleItem != null) pstSaleItem.close();
            if (pstSale != null) pstSale.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

    // ✅ NEW METHOD: Create discount record in discount table
    private int createDiscountRecord(Connection conn, int discountTypeId, double discountAmount) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            String query = "INSERT INTO discount (discount, discount_type_id) VALUES (?, ?)";
            pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setDouble(1, discountAmount);
            pst.setInt(2, discountTypeId);
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating discount record: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return -1;
    }

    // ✅ NEW METHOD: Create exchange refund record
    private void createExchangeRefundRecord(Connection conn, int salesId, double refundAmount) {
        PreparedStatement pst = null;
        
        try {
            // Note: Adjust this query based on your actual return table structure
            // This is a placeholder - you may need to modify based on your requirements
            String query = "INSERT INTO `return` (sales_id, total_return_amount, datetime, user_id, status_id) " +
                          "VALUES (?, ?, NOW(), ?, 1)";
            
            pst = conn.prepareStatement(query);
            pst.setInt(1, salesId);
            pst.setDouble(2, refundAmount);
            pst.setInt(3, Session.getInstance().getUserId());
            
            pst.executeUpdate();
            
            System.out.println("Exchange refund record created: Rs." + refundAmount);
            
        } catch (Exception e) {
            System.err.println("Error creating exchange refund record: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - just log the error as this is supplementary information
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ✅ ADD: Get Payment Method ID
    private int getPaymentMethodId(String paymentMethodName) {
        try {
            String query = "SELECT payment_method_id FROM payment_method WHERE payment_method_name = ?";
            PreparedStatement pst = MySQL.getConnection().prepareStatement(query);
            pst.setString(1, paymentMethodName);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("payment_method_id");
            }
        } catch (Exception e) {
            System.err.println("Error getting payment method ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // ✅ ADD: Get Stock ID
    private int getStockId(int productId, String batchNo) {
        try {
            String query = "SELECT stock_id FROM stock WHERE product_id = ? AND batch_no = ?";
            PreparedStatement pst = MySQL.getConnection().prepareStatement(query);
            pst.setInt(1, productId);
            pst.setString(2, batchNo);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("stock_id");
            }
        } catch (Exception e) {
            System.err.println("Error getting stock ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    

    // ✅ ADD: Open Credit Payment Dialog
    private void openCreditPaymentDialog(int salesId) {
        try {
            double totalAmount = getTotal();
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            
            AddCredit dialog = new AddCredit(parentFrame, true, totalAmount, salesId);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            
            // Check if credit was saved
            if (dialog.isCreditSaved()) {
                double paidAmount = dialog.getPaidAmount();
                
                if (paidAmount > 0) {
                    Notifications.getInstance().show(
                            Notifications.Type.SUCCESS,
                            Notifications.Location.TOP_RIGHT,
                            String.format("Credit sale completed! Sale ID: %d, Payment: Rs.%.2f", salesId, paidAmount)
                    );
                } else {
                    Notifications.getInstance().show(
                            Notifications.Type.SUCCESS,
                            Notifications.Location.TOP_RIGHT,
                            "Credit sale completed! Sale ID: " + salesId
                    );
                }
                
                // Clear cart
                resetCart();
                if (cartListener != null) {
                    cartListener.onCartUpdated(0, 0);
                }
            } else {
                // Credit dialog was closed without saving - inform user
                JOptionPane.showMessageDialog(this,
                        "Sale was saved but credit record was not completed.\n" +
                        "Sale ID: " + salesId + "\n" +
                        "Please add credit record manually if needed.",
                        "Credit Record Incomplete",
                        JOptionPane.WARNING_MESSAGE);
                
                // Still clear cart as sale was saved
                resetCart();
                if (cartListener != null) {
                    cartListener.onCartUpdated(0, 0);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error opening credit payment dialog: " + e.getMessage());
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(this,
                    "Sale was saved but error opening credit dialog.\n" +
                    "Sale ID: " + salesId + "\n" +
                    "Error: " + e.getMessage(),
                    "Dialog Error",
                    JOptionPane.ERROR_MESSAGE);
            
            // Clear cart anyway
            resetCart();
            if (cartListener != null) {
                cartListener.onCartUpdated(0, 0);
            }
        }
    }

    // ✅ ADD: Open Card Payment Dialog
    private void openCardPaymentDialog(int salesId) {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            
            CardPayDialog dialog = new CardPayDialog(parentFrame, true, salesId);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            
            // Check if card payment was saved
            Integer cardPaymentId = dialog.getGeneratedCardPaymentId();
            
            if (cardPaymentId != null && cardPaymentId > 0) {
                Notifications.getInstance().show(
                        Notifications.Type.SUCCESS,
                        Notifications.Location.TOP_RIGHT,
                        String.format("Card payment completed! Sale ID: %d, Card Payment ID: %d", salesId, cardPaymentId)
                );
                
                // Clear cart
                resetCart();
                if (cartListener != null) {
                    cartListener.onCartUpdated(0, 0);
                }
            } else {
                // Card dialog was closed without saving
                JOptionPane.showMessageDialog(this,
                        "Sale was saved but card payment record was not completed.\n" +
                        "Sale ID: " + salesId + "\n" +
                        "Please add card payment record manually if needed.",
                        "Card Payment Incomplete",
                        JOptionPane.WARNING_MESSAGE);
                
                // Still clear cart
                resetCart();
                if (cartListener != null) {
                    cartListener.onCartUpdated(0, 0);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error opening card payment dialog: " + e.getMessage());
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(this,
                    "Sale was saved but error opening card payment dialog.\n" +
                    "Sale ID: " + salesId + "\n" +
                    "Error: " + e.getMessage(),
                    "Dialog Error",
                    JOptionPane.ERROR_MESSAGE);
            
            // Clear cart anyway
            resetCart();
            if (cartListener != null) {
                cartListener.onCartUpdated(0, 0);
            }
        }
    }

    private void openChequePaymentDialog(int salesId) {
    try {
        double totalAmount = getTotal();
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // ✅ FIXED: Create and show the AddCheque dialog
        AddCheque dialog = new AddCheque(parentFrame, true, salesId, totalAmount);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        
        // Check if cheque was saved
        if (dialog.isChequeSaved()) {
            String chequeNo = dialog.getChequeNo();
            double chequeAmount = dialog.getChequeAmount();
            
            Notifications.getInstance().show(
                    Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_RIGHT,
                    String.format("Cheque payment completed! Sale ID: %d, Cheque: %s, Amount: Rs.%.2f", 
                                 salesId, chequeNo, chequeAmount)
            );
            
            // Clear cart
            resetCart();
            if (cartListener != null) {
                cartListener.onCartUpdated(0, 0);
            }
        } else {
            // Cheque dialog was closed without saving
            JOptionPane.showMessageDialog(this,
                    "Sale was saved but cheque record was not completed.\n" +
                    "Sale ID: " + salesId + "\n" +
                    "Please add cheque record manually if needed.",
                    "Cheque Payment Incomplete",
                    JOptionPane.WARNING_MESSAGE);
            
            // Still clear cart as sale was saved
            resetCart();
            if (cartListener != null) {
                cartListener.onCartUpdated(0, 0);
            }
        }
        
    } catch (Exception e) {
        System.err.println("Error opening cheque payment dialog: " + e.getMessage());
        e.printStackTrace();
        
        JOptionPane.showMessageDialog(this,
                "Sale was saved but error opening cheque dialog.\n" +
                "Sale ID: " + salesId + "\n" +
                "Error: " + e.getMessage(),
                "Dialog Error",
                JOptionPane.ERROR_MESSAGE);
        
        // Clear cart anyway
        resetCart();
        if (cartListener != null) {
            cartListener.onCartUpdated(0, 0);
        }
    }
}

    // ✅ ADD: Get Last Generated Sales ID (helper method)
    public int getLastGeneratedSalesId() {
        return lastGeneratedSalesId;
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
        exchangeBtn = new javax.swing.JButton();
        discountBtn = new javax.swing.JButton();
        switchBtn = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();

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
        cplusBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cplusBtn1ActionPerformed(evt);
            }
        });

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
        gradientButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gradientButton1ActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel24.setText("Discount :");

        jLabel26.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel26.setText("Exchange Refund:");

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
        clearCartBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearCartBtnActionPerformed(evt);
            }
        });

        holdBtn.setBackground(new java.awt.Color(255, 51, 51));
        holdBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        holdBtn.setText("H");
        holdBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holdBtnActionPerformed(evt);
            }
        });

        cartCount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 22)); // NOI18N
        cartCount.setText("Cart (01)");

        exchangeBtn.setBackground(new java.awt.Color(255, 204, 0));
        exchangeBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        exchangeBtn.setText("E");
        exchangeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exchangeBtnActionPerformed(evt);
            }
        });

        discountBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        discountBtn.setText("Di");
        discountBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discountBtnActionPerformed(evt);
            }
        });

        switchBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        switchBtn.setText("S");
        switchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchBtnActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton4.setText("P");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(cartCount, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(discountBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(switchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exchangeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(holdBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearCartBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(holdBtn)
                            .addComponent(clearCartBtn)
                            .addComponent(exchangeBtn)
                            .addComponent(switchBtn)
                            .addComponent(discountBtn)
                            .addComponent(jButton4)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(cartCount)
                        .addGap(1, 1, 1)))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jTextField3.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jTextField3)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
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

    private void gradientButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gradientButton1ActionPerformed
        handleCompleteSale();
    }//GEN-LAST:event_gradientButton1ActionPerformed

    private void cplusBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cplusBtn1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cplusBtn1ActionPerformed

    private void clearCartBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCartBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_clearCartBtnActionPerformed

    private void holdBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holdBtnActionPerformed
        handleHoldSale();
    }//GEN-LAST:event_holdBtnActionPerformed

    private void discountBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discountBtnActionPerformed
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cart is empty. Add products before applying discount.",
                    "Empty Cart",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Calculate current subtotal (before any discount)
        double currentSubtotal = 0;
        for (CartItem item : cartItems.values()) {
            currentSubtotal += item.getTotalPrice();
        }

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DiscountDialog dialog = new DiscountDialog(parentFrame, true, currentSubtotal);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);

        // Check if discount was applied
        if (dialog.isDiscountApplied()) {
            double discountAmount = dialog.getDiscountAmount();
            int discountTypeId = dialog.getDiscountTypeId();
            
            // Store discount info
            this.appliedDiscountAmount = discountAmount;
            this.appliedDiscountTypeId = discountTypeId;
            
            // Update UI to show discount
            updateTotals();
            
            Notifications.getInstance().show(
                    Notifications.Type.SUCCESS, 
                    Notifications.Location.TOP_RIGHT,
                    String.format("Discount of Rs.%.2f applied successfully!", discountAmount)
            );
        }
    }//GEN-LAST:event_discountBtnActionPerformed

    private void switchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchBtnActionPerformed
        showSwitchInvoicePanel();
    }//GEN-LAST:event_switchBtnActionPerformed
    private void exchangeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exchangeBtnActionPerformed
       JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
    ExchangeProductDialog dialog = new ExchangeProductDialog(parentFrame, true);
    dialog.setLocationRelativeTo(parentFrame);
    dialog.setVisible(true);
    
    // Check if exchange was processed
    int returnId = dialog.getGeneratedReturnId();
    if (returnId > 0) {
        try {
            // Exchange was successful - get the refund amount from database
            double refundAmount = getRefundAmountFromReturn(returnId);
            
            // ✅ FIXED: If we can't get the exchange item price from dialog,
            // we'll use a different approach to calculate total refund
            double totalRefundAmount = refundAmount;
            
            // Alternative: If the dialog provides the total amount directly
            // You might need to check what methods are available in ExchangeProductDialog
            try {
                // Try to get total amount using reflection or available methods
                // This is a fallback approach
                java.lang.reflect.Method getTotalAmountMethod = dialog.getClass().getMethod("getTotalAmount");
                if (getTotalAmountMethod != null) {
                    Object result = getTotalAmountMethod.invoke(dialog);
                    if (result instanceof Double) {
                        totalRefundAmount = (Double) result;
                    }
                }
            } catch (Exception e) {
                // Method not available, use the refund amount from database
                System.out.println("getTotalAmount method not available, using refund amount from database");
            }
            
            // Ensure we have a valid refund amount
            if (totalRefundAmount <= 0 && refundAmount > 0) {
                totalRefundAmount = refundAmount;
            }
            
            if (totalRefundAmount > 0) {
                // Store exchange refund amount
                this.exchangeRefundAmount = totalRefundAmount;
                
                // Update UI to show exchange refund
                updateTotals();
                
                Notifications.getInstance().show(
                        Notifications.Type.SUCCESS, 
                        Notifications.Location.TOP_RIGHT,
                        String.format("Exchange processed successfully! Refund: Rs.%.2f (Return ID: %d)", 
                                    totalRefundAmount, returnId)
                );
            } else {
                // If no valid refund amount, reset and show basic success
                this.exchangeRefundAmount = 0.0;
                updateTotals();
                
                Notifications.getInstance().show(
                        Notifications.Type.SUCCESS, 
                        Notifications.Location.TOP_RIGHT,
                        "Exchange processed successfully! Return ID: " + returnId
                );
            }
            
        } catch (Exception e) {
            System.err.println("Error processing exchange refund: " + e.getMessage());
            e.printStackTrace();
            
            // Reset exchange refund on error
            this.exchangeRefundAmount = 0.0;
            updateTotals();
            
            Notifications.getInstance().show(
                    Notifications.Type.ERROR, 
                    Notifications.Location.TOP_RIGHT,
                    "Exchange processed but error calculating refund amount. Return ID: " + returnId
            );
        }
    }

    }//GEN-LAST:event_exchangeBtnActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cartCount;
    private javax.swing.JLabel cartProductName;
    private javax.swing.JLabel cartProductName1;
    private javax.swing.JButton cartProductPanelDeleteBtn;
    private javax.swing.JButton cartProductPanelDeleteBtn1;
    private javax.swing.JButton clearCartBtn;
    private javax.swing.JButton cminusBtn;
    private javax.swing.JButton cminusBtn1;
    private javax.swing.JButton cplusBtn;
    private javax.swing.JButton cplusBtn1;
    private javax.swing.JButton discountBtn;
    private javax.swing.JTextField discountPriceinput;
    private javax.swing.JTextField discountPriceinput1;
    private javax.swing.JButton exchangeBtn;
    private lk.com.pos.privateclasses.GradientButton gradientButton1;
    private javax.swing.JButton holdBtn;
    private javax.swing.JButton jButton4;
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
    private javax.swing.JButton switchBtn;
    // End of variables declaration//GEN-END:variables
}
