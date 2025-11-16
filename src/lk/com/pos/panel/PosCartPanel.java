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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (isValidNumber(string)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
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

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        qtyPanel.setOpaque(false);

        JButton minusBtn = new JButton("-");
        minusBtn.setFont(new Font("Nunito ExtraBold", Font.PLAIN, 16));
        minusBtn.setPreferredSize(new Dimension(40, 35));
        minusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        minusBtn.addActionListener(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                updateCartPanel();
            }
        });
        qtyPanel.add(minusBtn);

        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()));
        qtyLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        qtyLabel.setHorizontalAlignment(JLabel.CENTER);
        qtyLabel.setPreferredSize(new Dimension(60, 35));
        qtyPanel.add(qtyLabel);

        JButton plusBtn = new JButton("+");
        plusBtn.setFont(new Font("Nunito ExtraBold", Font.PLAIN, 16));
        plusBtn.setPreferredSize(new Dimension(40, 35));
        plusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        plusBtn.addActionListener(e -> {
            if (item.getQuantity() < item.getAvailableQty()) {
                item.setQuantity(item.getQuantity() + 1);
                updateCartPanel();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot add more. Available stock: " + item.getAvailableQty(),
                        "Stock Limit",
                        JOptionPane.WARNING_MESSAGE);
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

        discountField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (discountField.getText().trim().equals("0") || discountField.getText().trim().equals("0.00")) {
                    discountField.setText("");
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
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
            updateCartPanel();
        }
    }

    private void updateBalance() {
        try {
            String amountText = jTextField2.getText().trim();
            double total = getTotal();

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

    private void updateTotals() {
        double total = 0;

        for (CartItem item : cartItems.values()) {
            total += item.getTotalPrice();
        }

        jLabel15.setText(String.format("Rs.%.2f", total));

        updateBalance();

        if (cartListener != null) {
            cartListener.onCartUpdated(total, cartItems.size());
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
        return total;
    }

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
            double total = getTotal();

            if (amountReceived < total) {
                JOptionPane.showMessageDialog(this,
                        String.format("Insufficient amount. Need Rs.%.2f more.", total - amountReceived),
                        "Insufficient Payment",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid amount entered.",
                    "Invalid Amount",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void resetCart() {
        cartItems.clear();
        jTextField2.setText("");
        paymentcombo.setSelectedIndex(0);
        updateCartPanel();
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
                FlatSVGIcon icon = new FlatSVGIcon(iconPath, 16, 16);
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
        jButton1 = new javax.swing.JButton();
        cartCount = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
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

        jButton1.setBackground(new java.awt.Color(255, 51, 51));
        jButton1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton1.setText("H");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        cartCount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 22)); // NOI18N
        cartCount.setText("Cart (01)");

        jButton3.setBackground(new java.awt.Color(255, 204, 0));
        jButton3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton3.setText("E");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton2.setText("Di");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
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
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(switchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                            .addComponent(jButton1)
                            .addComponent(clearCartBtn)
                            .addComponent(jButton3)
                            .addComponent(switchBtn)
                            .addComponent(jButton2)
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
        // TODO add your handling code here:
    }//GEN-LAST:event_gradientButton1ActionPerformed

    private void cplusBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cplusBtn1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cplusBtn1ActionPerformed

    private void clearCartBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCartBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_clearCartBtnActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Double totalAmount = 1000.0; // Your total amount here

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DiscountDialog dialog = new DiscountDialog(parentFrame, true, totalAmount);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);

// Check results after dialog closes
        if (dialog.isDiscountApplied()) {
            Double discountAmount = dialog.getDiscountAmount();
            int discountTypeId = dialog.getDiscountTypeId();

            // Calculate final amount after discount
            Double finalAmount = totalAmount - discountAmount;

            System.out.println("Original Total: " + totalAmount);
            System.out.println("Discount: " + discountAmount);
            System.out.println("Final Amount: " + finalAmount);
            System.out.println("Discount Type ID: " + discountTypeId);

            // Update your UI with the discounted amount
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void switchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchBtnActionPerformed
        showSwitchInvoicePanel();
    }//GEN-LAST:event_switchBtnActionPerformed
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ExchangeProductDialog dialog = new ExchangeProductDialog(parentFrame, true);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);

    }//GEN-LAST:event_jButton3ActionPerformed

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
    private javax.swing.JTextField discountPriceinput;
    private javax.swing.JTextField discountPriceinput1;
    private lk.com.pos.privateclasses.GradientButton gradientButton1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
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
