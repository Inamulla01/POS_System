package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

public class PosCartPanel extends javax.swing.JPanel {

    private static final Color TEAL_COLOR = new Color(28, 181, 187);
    private static final Color TEXT_GRAY = new Color(102, 102, 102);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color ERROR_COLOR = new Color(255, 102, 102);
    private static final Color SUCCESS_COLOR = new Color(102, 204, 102);

    private Map<String, CartItem> cartItems = new HashMap<>();
    private JLabel noProductsLabel;
    private CartListener cartListener;

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
        // ✅ ADD: Center the panel inside scroll pane
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

        // Add payment method change listener
        paymentcombo.addActionListener(evt -> togglePaymentButtons());
// Set initial state
        togglePaymentButtons();

        ((AbstractDocument) jTextField2.getDocument()).setDocumentFilter(new NumericDocumentFilter());

        loadPaymentMethods();
        //loadCustomers();

//        paymentcombo.addActionListener(evt -> toggleCustomerCombo());
//        toggleCustomerCombo();
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

//    private void loadCustomers() {
//        try {
//            coustomerCombo.removeAllItems();
//            coustomerCombo.addItem("Select Customer");
//
//            Connection connection = lk.com.pos.connection.MySQL.getConnection();
//            String query = "SELECT * FROM credit_customer ORDER BY customer_name ASC";
//            PreparedStatement pst = connection.prepareStatement(query);
//            ResultSet rs = pst.executeQuery();
//
//            while (rs.next()) {
//                String customerName = rs.getString("customer_name");
//                String customerPhone = rs.getString("customer_phone_no");
//                coustomerCombo.addItem(customerName + " - " + customerPhone);
//            }
//
//            rs.close();
//            pst.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this,
//                    "Error loading customers: " + e.getMessage(),
//                    "Database Error",
//                    JOptionPane.ERROR_MESSAGE);
//        }
//    }
//    private void toggleCustomerCombo() {
//        String selectedPayment = (String) paymentcombo.getSelectedItem();
//
//        if (selectedPayment != null && selectedPayment.equalsIgnoreCase("Credit Payment")) {
//            coustomerCombo.setVisible(true);
//            jButton8.setVisible(true);
//            jButton9.setVisible(true);
//        } else {
//            coustomerCombo.setVisible(false);
//            jButton8.setVisible(false);
//            jButton9.setVisible(false);
//        }
//
//        revalidate();
//        repaint();
//    }
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
            String batchNo, int qty, double sellingPrice, String barcode, double lastPrice) { // ✅ ADD lastPrice parameter

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
                    batchNo, qty, sellingPrice, barcode, lastPrice); // ✅ PASS last_price
            cartItems.put(cartKey, newItem);
            updateCartPanel();
        }
    }

    private void togglePaymentButtons() {
        String selectedPayment = (String) paymentcombo.getSelectedItem();

        if (selectedPayment != null) {
            if (selectedPayment.equalsIgnoreCase("Card Payment")) {
                cardPayBtn.setVisible(true);
                creditCustomerBtn.setVisible(false);
            } else if (selectedPayment.equalsIgnoreCase("Credit Payment")) {
                cardPayBtn.setVisible(false);
                creditCustomerBtn.setVisible(true);
            } else if (selectedPayment.equalsIgnoreCase("Cash Payment")) {
                cardPayBtn.setVisible(false);
                creditCustomerBtn.setVisible(false);
            } else {
                // For "Select Payment Method" or any other option, hide both
                cardPayBtn.setVisible(false);
                creditCustomerBtn.setVisible(false);
            }
        } else {
            // If nothing is selected, hide both
            cardPayBtn.setVisible(false);
            creditCustomerBtn.setVisible(false);
        }

        revalidate();
        repaint();
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

        // Top panel - Product name and delete button
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

        // Middle panel - Quantity controls and discount per unit
        JPanel middlePanel = new JPanel(new java.awt.BorderLayout(15, 0));
        middlePanel.setOpaque(false);

        // Quantity control
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

        // Discount input panel with label (Per Unit Discount)
        JPanel discountContainer = new JPanel();
        discountContainer.setLayout(new javax.swing.BoxLayout(discountContainer, javax.swing.BoxLayout.Y_AXIS));
        discountContainer.setOpaque(false);

        JLabel discountLabel = new JLabel("Discount (per unit)");
        discountLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
        discountLabel.setForeground(TEXT_GRAY);
        discountLabel.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT);
        discountContainer.add(discountLabel);

        discountContainer.add(javax.swing.Box.createRigidArea(new Dimension(0, 3)));

        // ✅ FIX: Show exactly what's stored (per-unit discount)
        double perUnitDiscount = item.getDiscountPrice();

        JTextField discountField = new JTextField(String.format("%.2f", perUnitDiscount));
        discountField.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        discountField.setHorizontalAlignment(JTextField.RIGHT);
        discountField.setPreferredSize(new Dimension(110, 35));
        discountField.setMaximumSize(new Dimension(110, 35));
        discountField.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT);

        ((AbstractDocument) discountField.getDocument()).setDocumentFilter(new NumericDocumentFilter());

        // Real-time discount validation
        // Real-time discount validation with last_price
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

                    // User enters per-unit discount
                    double discountInput = Double.parseDouble(text);
                    double unitPrice = item.getUnitPrice();
                    double lastPrice = item.getLastPrice(); // ✅ GET last_price from CartItem

                    // Calculate final price after discount
                    double finalPricePerUnit = unitPrice - discountInput;

                    if (discountInput < 0) {
                        discountField.setForeground(ERROR_COLOR);
                        discountField.setToolTipText("Discount cannot be negative");
                    } else if (discountInput > unitPrice) {
                        discountField.setForeground(ERROR_COLOR);
                        discountField.setToolTipText("Max discount per unit: Rs." + String.format("%.2f", unitPrice));
                    } else if (finalPricePerUnit < lastPrice) {
                        // ✅ Check if discounted price is below last_price
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
                    double lastPrice = item.getLastPrice(); // ✅ GET last_price
                    double finalPricePerUnit = unitPrice - discountInput;

                    if (discountInput < 0 || discountInput > unitPrice || finalPricePerUnit < lastPrice) {
                        // Restore current value
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

// ENTER key handler with last_price validation
        discountField.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "applyDiscount");
        discountField.getActionMap().put("applyDiscount", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    String text = discountField.getText().trim();
                    if (text.isEmpty()) {
                        item.setDiscountPrice(0);
                        discountField.setText("0.00");
                        updateTotals();
                        return;
                    }

                    double discountInput = Double.parseDouble(text);
                    double unitPrice = item.getUnitPrice();
                    double lastPrice = item.getLastPrice(); // ✅ GET last_price
                    double finalPricePerUnit = unitPrice - discountInput;

                    if (discountInput < 0 || discountInput > unitPrice || finalPricePerUnit < lastPrice) {
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
                        return;
                    }

                    item.setDiscountPrice(discountInput);
                    discountField.setText(String.format("%.2f", discountInput));
                    updateTotals();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid discount value.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        discountContainer.add(discountField);
        middlePanel.add(discountContainer, java.awt.BorderLayout.EAST);

        card.add(middlePanel, java.awt.BorderLayout.CENTER);

        // Bottom panel - Price calculation
        JPanel bottomPanel = new JPanel(new java.awt.BorderLayout(10, 0));
        bottomPanel.setOpaque(false);

        JPanel calcPanel = new JPanel();
        calcPanel.setLayout(new javax.swing.BoxLayout(calcPanel, javax.swing.BoxLayout.Y_AXIS));
        calcPanel.setOpaque(false);

        // Base price calculation
        JLabel basePriceLabel = new JLabel(String.format("Rs.%.2f × %d = Rs.%.2f",
                item.getUnitPrice(), item.getQuantity(), item.getUnitPrice() * item.getQuantity()));
        basePriceLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        basePriceLabel.setForeground(TEXT_GRAY);
        basePriceLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        calcPanel.add(basePriceLabel);

        // ✅ FIX: Show discount calculation correctly
        if (item.getDiscountPrice() > 0) {
            // getDiscountPrice() stores per-unit discount
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

        // Right side - Total price
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

//        if (selectedPayment.equalsIgnoreCase("Credit Payment")) {
//            String selectedCustomer = (String) coustomerCombo.getSelectedItem();
//            if (selectedCustomer == null || selectedCustomer.equals("Select Customer")) {
//                JOptionPane.showMessageDialog(this,
//                        "Please select a customer for credit payment.",
//                        "Missing Customer",
//                        JOptionPane.WARNING_MESSAGE);
//                coustomerCombo.requestFocus();
//                return false;
//            }
//        }
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
        productCount1 = new javax.swing.JLabel();
        priceInput1 = new javax.swing.JLabel();
        cplusBtn1 = new javax.swing.JButton();
        discountPriceinput1 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        paymentcombo = new javax.swing.JComboBox<>();
        cardPayBtn = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        gradientButton1 = new lk.com.pos.privateclasses.GradientButton();
        creditCustomerBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        clearCartBtn = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        cartCount = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();

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

        productCount1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        productCount1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        productCount1.setText("1");

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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(productCount1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cminusBtn1)
                            .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(productCount1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cplusBtn1))))
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
        paymentcombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Payment Method", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        cardPayBtn.setFont(new java.awt.Font("Nunito ExtraBold", 0, 18)); // NOI18N
        cardPayBtn.setText("C");
        cardPayBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        cardPayBtn.setMaximumSize(new java.awt.Dimension(33, 35));
        cardPayBtn.setMinimumSize(new java.awt.Dimension(33, 35));
        cardPayBtn.setPreferredSize(new java.awt.Dimension(33, 35));
        cardPayBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cardPayBtnActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel21.setText("Amount Recieved:");

        jTextField2.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

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

        creditCustomerBtn.setFont(new java.awt.Font("Nunito ExtraBold", 0, 18)); // NOI18N
        creditCustomerBtn.setText("V");
        creditCustomerBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        creditCustomerBtn.setMaximumSize(new java.awt.Dimension(33, 35));
        creditCustomerBtn.setMinimumSize(new java.awt.Dimension(33, 35));
        creditCustomerBtn.setPreferredSize(new java.awt.Dimension(33, 35));
        creditCustomerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creditCustomerBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gradientButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(paymentcombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cardPayBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creditCustomerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(28, 28, 28)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel22)
                            .addGap(93, 93, 93)
                            .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paymentcombo, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cardPayBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(creditCustomerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        cartCount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 22)); // NOI18N
        cartCount.setText("Cart (01)");

        jButton3.setBackground(new java.awt.Color(255, 204, 0));
        jButton3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton3.setText("E");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(cartCount, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                            .addComponent(jButton3)))
                    .addComponent(cartCount))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
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

    private void cardPayBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cardPayBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cardPayBtnActionPerformed

    private void gradientButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gradientButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gradientButton1ActionPerformed

    private void cplusBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cplusBtn1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cplusBtn1ActionPerformed

    private void clearCartBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCartBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_clearCartBtnActionPerformed

    private void creditCustomerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditCustomerBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_creditCustomerBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cardPayBtn;
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
    private javax.swing.JButton creditCustomerBtn;
    private javax.swing.JTextField discountPriceinput;
    private javax.swing.JTextField discountPriceinput1;
    private lk.com.pos.privateclasses.GradientButton gradientButton1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JComboBox<String> paymentcombo;
    private javax.swing.JLabel priceInput;
    private javax.swing.JLabel priceInput1;
    private javax.swing.JLabel productCount;
    private javax.swing.JLabel productCount1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel4;
    // End of variables declaration//GEN-END:variables
}
