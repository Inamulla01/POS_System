package lk.com.pos.panel;

import lk.com.pos.privateclasses.RoundedPanel;
import lk.com.pos.connection.MySQL;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import lk.com.pos.privateclasses.CartListener;

// WrapLayout class for responsive wrapping
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

// Custom rounded border class for badges
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

        // Adjust for stroke width to prevent clipping
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

public class posPanel extends javax.swing.JPanel implements CartListener {
    
    @Override
    public void onCartUpdated(double total, int itemCount) {
        // Already implemented - just keep this
        System.out.println("Cart updated: " + itemCount + " items, Total: Rs." + total);
    }

    @Override
    public void onCheckoutComplete() {
        // Already implemented - just keep this
        System.out.println("Checkout completed");
    }

    private static final Color TEAL_COLOR = new Color(28, 181, 187);
    private static final Color LIGHT_GRAY_BG = new Color(245, 245, 245);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color TEXT_GRAY = new Color(102, 102, 102);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color CARD_HOVER = new Color(240, 250, 250);

    private PosCartPanel posCartPanel;
    
    public posPanel() {
        initComponents();
        init();
        // Use invokeLater to ensure component is fully laid out before loading products
        javax.swing.SwingUtilities.invokeLater(() -> {
            loadProduct();
        });
    }

    private void init() {
        selectProductPanel.putClientProperty(FlatClientProperties.STYLE, "arc:15;");
        selectProductPanel.setBorder(BorderFactory.createCompoundBorder(
        new RoundBorder(Color.WHITE, 1, 15), // White border, 1px thickness, 15px radius
        BorderFactory.createEmptyBorder(0, 0, 0, 0)
    ));
        cartPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;");
        
        
        
        // Initialize and add PosCartPanel
        posCartPanel = new PosCartPanel();
        
       cartPanel.setLayout(new java.awt.BorderLayout());
        cartPanel.add(posCartPanel, java.awt.BorderLayout.CENTER);
        cartPanel.putClientProperty(FlatClientProperties.STYLE, "arc:15;");
        
        jScrollPane2.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane2.getVerticalScrollBar().setBlockIncrement(80);


        // Setup jPanel7 with responsive grid layout
        jPanel7.setLayout(new WrapLayout(FlowLayout.CENTER, 15, 15));
        jPanel7.setBackground(Color.WHITE);

        // Add component listener for responsive resizing
        jScrollPane2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                updateProductCardSizes();
            }
        });

        // Set initial placeholder state
        productSearchBar.setForeground(java.awt.Color.GRAY);
        if (productSearchBar.getText().isEmpty()) {
            productSearchBar.setText("Search By Product Name Or Barcode");
        }

        // Add hierarchy listener to detect when component is shown
        addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        // Delay to ensure layout is complete
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            updateProductCardSizes();
                        });
                    }
                }
            }
        });

        // Add document listener to search bar for real-time search
        productSearchBar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SearchFilters();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SearchFilters();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SearchFilters();
            }
        });

        // Simple scrollbar styling with FlatLaf
        jScrollPane2.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");

        

        // DON'T load sample products here - wait for proper sizing
    }

    private void updateProductCardSizes() {
        int viewportWidth = jScrollPane2.getViewport().getWidth();

        // Better fallback for initial load
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
                // Only update if size has actually changed
                if (currentSize.width != cardWidth) {
                    Dimension newSize = new Dimension(cardWidth, 145);
                    comp.setPreferredSize(newSize);
                    comp.setMinimumSize(new Dimension(280, 145));
                    comp.setMaximumSize(new Dimension(cardWidth, 145));
                    hasChanges = true;
                }
            }
        }

        // Only revalidate if there were actual changes
        if (hasChanges) {
            jPanel7.revalidate();
            jPanel7.repaint();
        }
    }

    private int calculateCardWidth(int viewportWidth) {
        int gap = 15;
        int padding = 25;
        int availableWidth = viewportWidth - (padding * 2);

        // Minimum card width for good appearance
        int minCardWidth = 320;

        // Calculate how many cards can fit
        int columns;
        if (availableWidth >= (minCardWidth * 2 + gap)) {
            // 2 columns - preferred layout
            columns = 2;
        } else if (availableWidth >= minCardWidth) {
            // 1 column for small screens
            columns = 1;
        } else {
            // Fallback for very small screens
            columns = 1;
            minCardWidth = Math.max(280, availableWidth);
        }

        // Calculate card width based on columns
        int cardWidth;
        if (columns == 2) {
            cardWidth = (availableWidth - gap) / 2;
        } else {
            cardWidth = availableWidth;
        }

        return cardWidth;
    }

    private void loadSampleProducts() {
        jPanel7.removeAll();

        // Sample Product 1
        RoundedPanel card1 = createProductCard(
                1,
                "Panadol Extra",
                "GSK",
                "BATCH 1",
                99,
                250.00,
                "8901234567890"
        );
        jPanel7.add(card1);

        // Sample Product 2
        RoundedPanel card2 = createProductCard(
                2,
                "Amoxicillin 500mg",
                "Pfizer",
                "BATCH 2",
                150,
                450.00,
                "8901234567891"
        );
        jPanel7.add(card2);

        jPanel7.revalidate();
        jPanel7.repaint();
    }

    private void SearchFilters() {
        String searchText = productSearchBar.getText().trim();
        // Don't search if it's the placeholder text or empty

        if (searchText.isEmpty() || searchText.equals("Search By Product Name Or Barcode")) {
            loadProduct(""); // Load all products
        } else {
            loadProduct(searchText);
        }
    }

    private void loadProduct() {
        loadProduct("");
    }

    private void loadProduct(String productSearch) {
        try {
            // Clear existing products
            jPanel7.removeAll();

            // Base query
            String query = "SELECT product.product_id, product.product_name, suppliers.suppliers_name, "
                    + "brand.brand_name, category.category_name, "
                    + "stock.qty, stock.expriy_date, stock.batch_no, product.barcode, "
                    + "stock.purchase_price, stock.last_price, stock.selling_price "
                    + "FROM product "
                    + "JOIN stock ON stock.product_id = product.product_id "
                    + "JOIN category ON category.category_id = product.category_id "
                    + "JOIN brand ON brand.brand_id = product.brand_id "
                    + "JOIN suppliers ON suppliers.suppliers_id = stock.suppliers_id "
                    + "WHERE stock.qty > 0 ";

            // Add search filter if provided
            if (!productSearch.isEmpty()) {
                query += "AND (product.product_name LIKE '%" + productSearch + "%' "
                        + "OR product.barcode LIKE '%" + productSearch + "%') ";
            }

            query += "ORDER BY product.product_name ASC";

            ResultSet rs = MySQL.executeSearch(query);

            // Create product cards
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                String brandName = rs.getString("brand_name");
                String batchNo = rs.getString("batch_no");
                int qty = rs.getInt("qty");
                double sellingPrice = rs.getDouble("selling_price");
                String barcode = rs.getString("barcode");

                // Create product card
                RoundedPanel productCard = createProductCard(
                        productId, productName, brandName, batchNo, qty, sellingPrice, barcode
                );

                jPanel7.add(productCard);
            }

            // If no products found, show message
            if (jPanel7.getComponentCount() == 0) {
                // Create a wrapper panel to center the message
                JPanel messagePanel = new JPanel(new java.awt.GridBagLayout());
                messagePanel.setBackground(Color.WHITE);
                messagePanel.setPreferredSize(new Dimension(jScrollPane2.getViewport().getWidth(), 400));

                JLabel noProductLabel = new JLabel("No products found");
                noProductLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 18));
                noProductLabel.setForeground(TEXT_GRAY);

                messagePanel.add(noProductLabel);
                jPanel7.add(messagePanel);
            }

            // Refresh panel
            jPanel7.revalidate();
            jPanel7.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading products: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private RoundedPanel createProductCard(int productId, String productName,
            String brandName, String batchNo, int qty, double sellingPrice, String barcode) {

        // Get current viewport width with better fallback handling
        int viewportWidth = jScrollPane2.getViewport().getWidth();

        // Better fallback calculation
        if (viewportWidth < 100) {
            // Try to get from scroll pane directly
            viewportWidth = jScrollPane2.getWidth();
            if (viewportWidth < 100) {
                // Use parent panel width as last resort
                viewportWidth = selectProductPanel.getWidth() - 40;
            }
        }

        int cardWidth = calculateCardWidth(viewportWidth);

        // Create rounded panel for product card with more height
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

        // Top panel - Product name
        JPanel topPanel = new JPanel(new java.awt.BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblProductName = new JLabel(productName);
        lblProductName.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        lblProductName.setForeground(new Color(40, 40, 40));
        topPanel.add(lblProductName, java.awt.BorderLayout.WEST);

        card.add(topPanel, java.awt.BorderLayout.NORTH);

        // Middle panel - Brand and Price
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

        // Bottom panel - Stock and Batch with better styling
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomPanel.setOpaque(false);

        // Stock badge with rounded corners
        JPanel stockBadge = new JPanel();
        stockBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        stockBadge.setOpaque(true);
        stockBadge.setBackground(new Color(230, 245, 230));

        JLabel lblStock = new JLabel("Stock: " + qty);
        lblStock.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        lblStock.setForeground(new Color(34, 139, 34));
        lblStock.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

        stockBadge.add(lblStock);
        bottomPanel.add(stockBadge);

        // Batch badge with rounded corners
        JPanel batchBadge = new JPanel();
        batchBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        batchBadge.setOpaque(true);
        batchBadge.setBackground(new Color(240, 240, 250));

        JLabel lblBatch = new JLabel(batchNo);
        lblBatch.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        lblBatch.setForeground(new Color(70, 70, 100));
        lblBatch.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

        batchBadge.add(lblBatch);
        bottomPanel.add(batchBadge);

        card.add(bottomPanel, java.awt.BorderLayout.SOUTH);

        // Add hover effect and click handler
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
                card.setBackground(originalBg);
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(BORDER_COLOR, 1, 20),
                        BorderFactory.createEmptyBorder(16, 18, 16, 18)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                addToCart(productId, productName, brandName, batchNo, qty, sellingPrice, barcode);
            }
        });

        return card;
    }

    private void addToCart(int productId, String productName, String brandName,
            String batchNo, int qty, double sellingPrice, String barcode) {
        
        posCartPanel.addToCart(productId, productName, brandName, batchNo, qty, sellingPrice, barcode);
    }
    
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cartPanel = new lk.com.pos.privateclasses.RoundedPanel();
        selectProductPanel = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel2 = new javax.swing.JLabel();
        reloadBtn = new javax.swing.JButton();
        productSearchBar = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel7 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        productName = new javax.swing.JLabel();
        brandName = new javax.swing.JLabel();
        batchNo = new javax.swing.JLabel();
        qty = new javax.swing.JLabel();
        sellingPrice = new javax.swing.JLabel();

        setBackground(new java.awt.Color(204, 255, 255));

        javax.swing.GroupLayout cartPanelLayout = new javax.swing.GroupLayout(cartPanel);
        cartPanel.setLayout(cartPanelLayout);
        cartPanelLayout.setHorizontalGroup(
            cartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 479, Short.MAX_VALUE)
        );
        cartPanelLayout.setVerticalGroup(
            cartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        selectProductPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 22)); // NOI18N
        jLabel2.setText("Select Product");

        reloadBtn.setFont(new java.awt.Font("Nunito ExtraBold", 0, 14)); // NOI18N
        reloadBtn.setText("R");
        reloadBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadBtnActionPerformed(evt);
            }
        });

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

        brandName.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        brandName.setForeground(new java.awt.Color(102, 102, 102));
        brandName.setText("Brand :");

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
                        .addComponent(brandName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                        .addComponent(sellingPrice)
                        .addGap(18, 18, 18))
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(qty)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(batchNo)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(productName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(brandName)
                    .addComponent(sellingPrice))
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
                .addGroup(selectProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, selectProductPanelLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(selectProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(productSearchBar)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, selectProductPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(reloadBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(selectProductPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE)))
                .addContainerGap())
        );
        selectProductPanelLayout.setVerticalGroup(
            selectProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectProductPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(selectProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(reloadBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(selectProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(cartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(selectProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(65, Short.MAX_VALUE))
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

    private void reloadBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadBtnActionPerformed
        loadProduct();
        productSearchBar.setText("Search By Product Name Or Barcode");
        productSearchBar.setForeground(java.awt.Color.GRAY);
    }//GEN-LAST:event_reloadBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel batchNo;
    private javax.swing.JLabel brandName;
    private lk.com.pos.privateclasses.RoundedPanel cartPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel productName;
    private javax.swing.JTextField productSearchBar;
    private javax.swing.JLabel qty;
    private javax.swing.JButton reloadBtn;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private lk.com.pos.privateclasses.RoundedPanel selectProductPanel;
    private javax.swing.JLabel sellingPrice;
    // End of variables declaration//GEN-END:variables
}
