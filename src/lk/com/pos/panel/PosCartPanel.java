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

public class PosCartPanel extends javax.swing.JPanel {

    private static final Color TEAL_COLOR = new Color(28, 181, 187);
    private static final Color TEXT_GRAY = new Color(102, 102, 102);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    
    private Map<String, CartItem> cartItems = new HashMap<>();
    private JLabel noProductsLabel;
    private CartListener cartListener;

    public PosCartPanel() {
        initComponents();
        init();
    }
    
    private void init(){
        FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 18, 18);
        deleteIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(220, 0, 0)));
        
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane1.getVerticalScrollBar().setBlockIncrement(80);
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");
        
       
    this.putClientProperty(FlatClientProperties.STYLE, "arc:15");
    
        
        // Initialize cart item container
        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.Y_AXIS));
        jPanel10.setBackground(Color.WHITE);
        
        // Clear cart button action
        clearCartBtn.addActionListener(evt -> clearCart());
        
        // Amount received listener
        jTextField2.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotals(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotals(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotals(); }
        });
        
        // Show initial empty state
        showNoProductsMessage();
    }
    
    // Set listener for communication with parent panel
    public void setCartListener(CartListener listener) {
        this.cartListener = listener;
    }
    
    // Add item to cart (called from POSPanel2)
    public void addToCart(int productId, String productName, String brandName,
            String batchNo, int qty, double sellingPrice, String barcode) {
        
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
                                           batchNo, qty, sellingPrice, barcode);
            cartItems.put(cartKey, newItem);
            updateCartPanel();
        }
    }
    
    // Show empty cart message
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
        
        cartCount.setText("Cart (00)");
        
        jPanel10.revalidate();
        jPanel10.repaint();
        
        updateTotals();
    }
    
    // Update cart display
    private void updateCartPanel() {
        jPanel10.removeAll();
        
        if (cartItems.isEmpty()) {
            showNoProductsMessage();
            return;
        }
        
        // Add each cart item
        for (CartItem item : cartItems.values()) {
            RoundedPanel cartCard = createCartItemPanel(item);
            jPanel10.add(cartCard);
            jPanel10.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
        }
        
        cartCount.setText(String.format("Cart (%02d)", cartItems.size()));
        
        jPanel10.revalidate();
        jPanel10.repaint();
        
        updateTotals();
    }
    
    // Create cart item panel
    private RoundedPanel createCartItemPanel(CartItem item) {
        // Use the RoundBorder class from POSPanel2
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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setPreferredSize(new Dimension(430, 140));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(BORDER_COLOR, 1, 15),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        card.setLayout(new java.awt.BorderLayout(0, 8));
        
        // Top panel - Product name and delete button
        JPanel topPanel = new JPanel(new java.awt.BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel lblName = new JLabel(item.getProductName() + " (" + item.getBrandName() + ")");
        lblName.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        lblName.setForeground(new Color(40, 40, 40));
        topPanel.add(lblName, java.awt.BorderLayout.WEST);
        
        // Delete button
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
        
        // Middle panel - Quantity controls and discount
        JPanel middlePanel = new JPanel(new java.awt.BorderLayout(10, 0));
        middlePanel.setOpaque(false);
        
        // Quantity control
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        qtyPanel.setOpaque(false);
        
        JButton minusBtn = new JButton("-");
        minusBtn.setFont(new Font("Nunito ExtraBold", Font.PLAIN, 14));
        minusBtn.setPreferredSize(new Dimension(35, 30));
        minusBtn.addActionListener(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                updateCartPanel();
            }
        });
        qtyPanel.add(minusBtn);
        
        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()));
        qtyLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        qtyLabel.setHorizontalAlignment(JLabel.CENTER);
        qtyLabel.setPreferredSize(new Dimension(70, 30));
        qtyPanel.add(qtyLabel);
        
        JButton plusBtn = new JButton("+");
        plusBtn.setFont(new Font("Nunito ExtraBold", Font.PLAIN, 14));
        plusBtn.setPreferredSize(new Dimension(35, 30));
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
        
        // Discount input
        JTextField discountField = new JTextField(String.valueOf(item.getDiscountPrice()));
        discountField.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        discountField.setHorizontalAlignment(JTextField.RIGHT);
        discountField.setPreferredSize(new Dimension(120, 30));
        discountField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                try {
                    double discount = Double.parseDouble(discountField.getText());
                    if (discount >= 0 && discount <= (item.getUnitPrice() * item.getQuantity())) {
                        item.setDiscountPrice(discount);
                        updateCartPanel();
                    } else {
                        discountField.setText(String.valueOf(item.getDiscountPrice()));
                        JOptionPane.showMessageDialog(null,
                                "Invalid discount amount",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    discountField.setText(String.valueOf(item.getDiscountPrice()));
                }
            }
        });
        middlePanel.add(discountField, java.awt.BorderLayout.EAST);
        
        card.add(middlePanel, java.awt.BorderLayout.CENTER);
        
        // Bottom panel - Price
        JPanel bottomPanel = new JPanel(new java.awt.BorderLayout());
        bottomPanel.setOpaque(false);
        
        JLabel lblPrice = new JLabel(String.format("Rs.%.2f", item.getTotalPrice()));
        lblPrice.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        lblPrice.setForeground(TEAL_COLOR);
        lblPrice.setHorizontalAlignment(JLabel.RIGHT);
        bottomPanel.add(lblPrice, java.awt.BorderLayout.EAST);
        
        card.add(bottomPanel, java.awt.BorderLayout.SOUTH);
        
        return card;
    }
    
    // Remove item from cart
    private void removeFromCart(String cartKey) {
        cartItems.remove(cartKey);
        updateCartPanel();
    }
    
    // Clear cart
    private void clearCart() {
        if (cartItems.isEmpty()) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the cart?",
                "Clear Cart",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.clear();
            updateCartPanel();
        }
    }
    
    // Update totals
    private void updateTotals() {
        double total = 0;
        
        for (CartItem item : cartItems.values()) {
            total += item.getTotalPrice();
        }
        
        jLabel15.setText(String.format("Rs.%.2f", total));
        
        // Calculate balance
        try {
            String amountText = jTextField2.getText().trim();
            if (!amountText.isEmpty()) {
                double amountReceived = Double.parseDouble(amountText);
                double balance = amountReceived - total;
                jLabel23.setText(String.format("Rs.%.2f", balance));
            } else {
                jLabel23.setText("Rs.0.00");
            }
        } catch (NumberFormatException e) {
            jLabel23.setText("Rs.0.00");
        }
        
        // Notify listener
        if (cartListener != null) {
            cartListener.onCartUpdated(total, cartItems.size());
        }
    }
    
    // Public method to get cart items (for checkout)
    public Map<String, CartItem> getCartItems() {
        return new HashMap<>(cartItems);
    }
    
    // Public method to get total
    public double getTotal() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getTotalPrice();
        }
        return total;
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
        cartCount = new javax.swing.JLabel();
        clearCartBtn = new javax.swing.JButton();
        paymentcombo = new javax.swing.JComboBox<>();
        coustomerCombo = new javax.swing.JComboBox<>();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        gradientButton1 = new lk.com.pos.privateclasses.GradientButton();
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

        cartCount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 22)); // NOI18N
        cartCount.setText("Cart (01)");

        clearCartBtn.setFont(new java.awt.Font("Nunito ExtraBold", 0, 14)); // NOI18N
        clearCartBtn.setText("D");

        paymentcombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        paymentcombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        paymentcombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Payment Method", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        coustomerCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        coustomerCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        coustomerCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select Customer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        jButton8.setFont(new java.awt.Font("Nunito ExtraBold", 0, 18)); // NOI18N
        jButton8.setText("v");
        jButton8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jButton8.setMaximumSize(new java.awt.Dimension(33, 35));
        jButton8.setMinimumSize(new java.awt.Dimension(33, 35));
        jButton8.setPreferredSize(new java.awt.Dimension(33, 35));
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setFont(new java.awt.Font("Nunito ExtraBold", 0, 18)); // NOI18N
        jButton9.setText("+");
        jButton9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jButton9.setMaximumSize(new java.awt.Dimension(33, 35));
        jButton9.setMinimumSize(new java.awt.Dimension(33, 35));
        jButton9.setPreferredSize(new java.awt.Dimension(33, 35));
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 141, Short.MAX_VALUE)
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
                .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(roundedPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(159, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel10);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cartCount, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearCartBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(1, 1, 1)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(paymentcombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(coustomerCombo, 0, 351, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(gradientButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cartCount)
                    .addComponent(clearCartBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(paymentcombo, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coustomerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15))
                .addGap(10, 10, 10)
                .addComponent(gradientButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cplusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cplusBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cplusBtnActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    private void gradientButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gradientButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gradientButton1ActionPerformed

    private void cplusBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cplusBtn1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cplusBtn1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cartCount;
    private javax.swing.JLabel cartProductName;
    private javax.swing.JLabel cartProductName1;
    private javax.swing.JButton cartProductPanelDeleteBtn;
    private javax.swing.JButton cartProductPanelDeleteBtn1;
    private javax.swing.JButton clearCartBtn;
    private javax.swing.JButton cminusBtn;
    private javax.swing.JButton cminusBtn1;
    private javax.swing.JComboBox<String> coustomerCombo;
    private javax.swing.JButton cplusBtn;
    private javax.swing.JButton cplusBtn1;
    private javax.swing.JTextField discountPriceinput;
    private javax.swing.JTextField discountPriceinput1;
    private lk.com.pos.privateclasses.GradientButton gradientButton1;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JPanel jPanel10;
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
