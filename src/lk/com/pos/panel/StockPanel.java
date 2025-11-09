package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import lk.com.pos.connection.MySQL;
import lk.com.pos.dialog.AddNewProduct;
import lk.com.pos.dialog.AddNewStock;
import lk.com.pos.dialog.PrintProductLabel;
import lk.com.pos.dialog.UpdateProduct;
import lk.com.pos.gui.HomeScreen;

public class StockPanel extends javax.swing.JPanel {

    public StockPanel() {
        initComponents();

        // FIXED: Ensure scroll pane is properly configured
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling

        init();
        loadProduct();
        setupEventListeners();
        radioButtonListener();
    }

    private void init() {
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 18, 18);
            editBtn.setIcon(editIcon);
            FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 18, 18);
            deleteBtn.setIcon(deleteIcon);
        } catch (Exception e) {
            System.err.println("Error loading icons: " + e.getMessage());
        }

        // Modern gradient-inspired colors
        roundedPanel1.setBackgroundColor(Color.decode("#E0F2FE")); // Sky blue
        roundedPanel1.setBorderThickness(0);
        roundedPanel3.setBackgroundColor(Color.decode("#FCE7F3")); // Pink
        roundedPanel3.setBorderThickness(0);
        roundedPanel4.setBackgroundColor(Color.decode("#D1FAE5")); // Green
        roundedPanel4.setBorderThickness(0);

        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");

        productSearchBar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search By Product Name Or Barcode");
        productSearchBar.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
    }

    private void setupEventListeners() {
        // Search bar - trigger search on text change
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

        // Clear placeholder text on focus
        productSearchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (productSearchBar.getText().equals("Search By Product Name Or Barcode")) {
                    productSearchBar.setText("");
                    productSearchBar.setForeground(java.awt.Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (productSearchBar.getText().isEmpty()) {
                    productSearchBar.setText("Search By Product Name Or Barcode");
                    productSearchBar.setForeground(java.awt.Color.GRAY);
                }
            }
        });

        productSearchBar.setForeground(Color.GRAY);

        // Radio buttons - trigger search on selection
        expiringRadioBtn.addActionListener(e -> SearchFilters());
        lowStockRadioBtn.addActionListener(e -> SearchFilters());
        expiredRadioBtn.addActionListener(e -> SearchFilters());
        inactiveRadioBtn.addActionListener(e -> SearchFilters());
    }

    private void SearchFilters() {
        String productSearch = productSearchBar.getText().trim();

        String status = "all";

        if (expiringRadioBtn.isSelected()) {
            status = "Expiring Soon";
        } else if (lowStockRadioBtn.isSelected()) {
            status = "Low Stock";
        } else if (expiredRadioBtn.isSelected()) {
            status = "Expired";
        } else if (inactiveRadioBtn.isSelected()) {
            status = "Inactive";
        }

        loadProduct(productSearch, status);
    }

    private void loadProduct() {
        loadProduct("", "all");
    }

    private void loadProduct(String productSearch, String status) {
        try {
            // Base query - exclude products with 0 stock
            String query = "SELECT product.product_id, product.product_name, suppliers.suppliers_name, "
                    + "brand.brand_name, category.category_name, "
                    + "stock.qty, stock.expriy_date, stock.batch_no, product.barcode, "
                    + "stock.purchase_price, stock.last_price, stock.selling_price, "
                    + "stock.stock_id, product.p_status_id "
                    + "FROM product "
                    + "JOIN stock ON stock.product_id = product.product_id "
                    + "JOIN category ON category.category_id = product.category_id "
                    + "JOIN brand ON brand.brand_id = product.brand_id "
                    + "JOIN suppliers ON suppliers.suppliers_id = stock.suppliers_id "
                    + "JOIN p_status ON p_status.p_status_id = product.p_status_id "
                    + "WHERE stock.qty > 0 "; // Only show products with stock > 0

            // Add status filter for Active/Inactive products
            if ("Inactive".equals(status)) {
                query += "AND product.p_status_id = 2 "; // Inactive products
            } else {
                query += "AND product.p_status_id = 1 "; // Active products (default)
            }

            // Add search filter for product name or barcode
            if (productSearch != null && !productSearch.trim().isEmpty()
                    && !productSearch.equals("Search By Product Name Or Barcode")) {
                query += "AND (product.product_name LIKE '%" + productSearch + "%' "
                        + "OR product.barcode LIKE '%" + productSearch + "%') ";
            }

            // Add status filter for Low Stock (less than 10 units for pharmacy)
            if ("Low Stock".equals(status)) {
                query += "AND stock.qty < 10 ";
            }

            // Add status filter for Expiring Soon (within 3 months)
            if ("Expiring Soon".equals(status)) {
                java.time.LocalDate threeMonthsLater = java.time.LocalDate.now().plusMonths(3);
                query += "AND stock.expriy_date <= '" + threeMonthsLater + "' "
                        + "AND stock.expriy_date >= CURDATE() ";
            }

            // Add status filter for Expired products
            if ("Expired".equals(status)) {
                query += "AND stock.expriy_date < CURDATE() ";
            }

            query += "ORDER BY product.product_name";

            ResultSet rs = MySQL.executeSearch(query);

            // Clear existing products
            jPanel2.removeAll();
            jPanel2.setBackground(Color.decode("#F8FAFC"));

            // Store products in a list
            java.util.List<lk.com.pos.privateclasses.RoundedPanel> productCards = new java.util.ArrayList<>();

            int productCount = 0;

            // Loop through results and create product cards
            while (rs.next()) {
                productCount++;

                int stockId = rs.getInt("stock_id");
                int pStatusId = rs.getInt("p_status_id");

                lk.com.pos.privateclasses.RoundedPanel productCard = createProductCard(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("suppliers_name"),
                        rs.getString("brand_name"),
                        rs.getString("category_name"),
                        rs.getInt("qty"),
                        rs.getString("expriy_date"),
                        rs.getString("batch_no"),
                        rs.getString("barcode"),
                        rs.getDouble("purchase_price"),
                        rs.getDouble("last_price"),
                        rs.getDouble("selling_price"),
                        stockId,
                        pStatusId
                );

                productCards.add(productCard);
            }

            // If no products found - show message at top center
            if (productCount == 0) {
                jPanel2.setLayout(new java.awt.BorderLayout());

                javax.swing.JPanel messagePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
                messagePanel.setBackground(Color.decode("#F8FAFC"));
                messagePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 0, 0, 0));

                String message = "No products with stock available";
                if ("Inactive".equals(status)) {
                    message = "No inactive products with stock available";
                } else if ("Expired".equals(status)) {
                    message = "No expired products with stock available";
                } else if ("Expiring Soon".equals(status)) {
                    message = "No expiring soon products with stock available";
                } else if ("Low Stock".equals(status)) {
                    message = "No low stock products available";
                }

                javax.swing.JLabel noProducts = new javax.swing.JLabel(message);
                noProducts.setFont(new java.awt.Font("Nunito SemiBold", 0, 18));
                noProducts.setForeground(Color.decode("#6B7280"));
                noProducts.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                messagePanel.add(noProducts);
                jPanel2.add(messagePanel, java.awt.BorderLayout.NORTH);
            } else {
                // Create responsive grid panel
                final javax.swing.JPanel gridPanel = new javax.swing.JPanel();
                gridPanel.setBackground(Color.decode("#F8FAFC"));

                // Calculate initial columns based on jPanel2 width
                int initialColumns = calculateColumns(jPanel2.getWidth());
                gridPanel.setLayout(new java.awt.GridLayout(0, initialColumns, 25, 25));

                // Add all product cards
                for (lk.com.pos.privateclasses.RoundedPanel card : productCards) {
                    gridPanel.add(card);
                }

                // FIXED: Proper scrolling implementation
                jPanel2.setLayout(new java.awt.BorderLayout());

                // Create main container with vertical layout for proper scrolling
                javax.swing.JPanel mainContainer = new javax.swing.JPanel();
                mainContainer.setLayout(new javax.swing.BoxLayout(mainContainer, javax.swing.BoxLayout.Y_AXIS));
                mainContainer.setBackground(Color.decode("#F8FAFC"));

                // Add padding around the grid
                javax.swing.JPanel paddingPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
                paddingPanel.setBackground(Color.decode("#F8FAFC"));
                paddingPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25));
                paddingPanel.add(gridPanel, java.awt.BorderLayout.NORTH);

                mainContainer.add(paddingPanel);
                jPanel2.add(mainContainer, java.awt.BorderLayout.NORTH);

                // Add component listener to jPanel2 for window resize
                jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
                    private int lastColumns = initialColumns;

                    @Override
                    public void componentResized(java.awt.event.ComponentEvent e) {
                        int panelWidth = jPanel2.getWidth();
                        int newColumns = calculateColumns(panelWidth);

                        // Only update if columns changed
                        if (newColumns != lastColumns) {
                            lastColumns = newColumns;
                            gridPanel.setLayout(new java.awt.GridLayout(0, newColumns, 25, 25));
                            gridPanel.revalidate();
                            gridPanel.repaint();
                        }
                    }
                });
            }

            // Refresh the panel
            jPanel2.revalidate();
            jPanel2.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Error loading products: " + e.getMessage(),
                    "Database Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private int calculateColumns(int panelWidth) {
        int availableWidth = panelWidth - 50;
        int cardWithGap = 445;

        if (availableWidth >= cardWithGap * 3) {
            return 3;
        } else if (availableWidth >= cardWithGap * 2) {
            return 2;
        } else {
            return 1;
        }
    }

    private lk.com.pos.privateclasses.RoundedPanel createProductCard(
            int productId, String productName, String supplierName,
            String brandName, String categoryName, int qty,
            String expiryDate, String batchNo, String barcode,
            double purchasePrice, double lastPrice, double sellingPrice,
            int stockId, int pStatusId) {

        // Create main rounded panel
        lk.com.pos.privateclasses.RoundedPanel card = new lk.com.pos.privateclasses.RoundedPanel();
        card.setLayout(new java.awt.BorderLayout());
        card.setPreferredSize(new java.awt.Dimension(420, 480));
        card.setMaximumSize(new java.awt.Dimension(420, 480));
        card.setMinimumSize(new java.awt.Dimension(380, 480));
        
        // Set different background for inactive products
        if (pStatusId == 2) {
            card.setBackground(Color.decode("#F8F9FA")); // Light gray for inactive
        } else {
            card.setBackground(java.awt.Color.WHITE);
        }
        
        card.setBorderThickness(0);
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Main content panel
        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS));
        contentPanel.setBackground(card.getBackground());
        contentPanel.setOpaque(false);

        // === HEADER SECTION ===
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));

        // Product Name
        javax.swing.JLabel nameLabel = new javax.swing.JLabel(productName);
        nameLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
        if (pStatusId == 2) {
            nameLabel.setForeground(Color.decode("#6B7280")); // Gray for inactive
        } else {
            nameLabel.setForeground(Color.decode("#1E293B"));
        }
        nameLabel.setToolTipText(productName);
        headerPanel.add(nameLabel, java.awt.BorderLayout.CENTER);

        // Action buttons panel
        javax.swing.JPanel actionPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        // Edit Button - Fixed size
        javax.swing.JButton editButton = new javax.swing.JButton();
        editButton.setPreferredSize(new java.awt.Dimension(30, 30));
        editButton.setMinimumSize(new java.awt.Dimension(30, 30));
        editButton.setMaximumSize(new java.awt.Dimension(30, 30));
        editButton.setBackground(Color.decode("#EFF6FF"));
        editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 16, 16);
            editButton.setIcon(editIcon);
        } catch (Exception e) {
            editButton.setText("✎");
            editButton.setForeground(Color.decode("#3B82F6"));
            editButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        }
        editButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#BFDBFE"), 1));
        editButton.setFocusable(false);
        
        if (pStatusId == 2) {
            // For inactive products, change to reactivate button
            editButton.setBackground(Color.decode("#D1FAE5")); // Green for reactivate
            editButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#10B981"), 1));
            try {
                FlatSVGIcon reactivateIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 16, 16);
                editButton.setIcon(reactivateIcon);
            } catch (Exception e) {
                editButton.setText("↻");
                editButton.setForeground(Color.decode("#059669"));
            }
            editButton.addActionListener(e -> reactivateProduct(productId, productName));
        } else {
            editButton.addActionListener(e -> editProduct(productId, stockId));
        }

        // Delete Button - Fixed size
        javax.swing.JButton deleteButton = new javax.swing.JButton();
        deleteButton.setPreferredSize(new java.awt.Dimension(30, 30));
        deleteButton.setMinimumSize(new java.awt.Dimension(30, 30));
        deleteButton.setMaximumSize(new java.awt.Dimension(30, 30));
        
        if (pStatusId == 2) {
            // For inactive products, show different delete style
            deleteButton.setBackground(Color.decode("#FEF3C7")); // Yellow for permanent delete
            deleteButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#F59E0B"), 1));
            try {
                FlatSVGIcon permanentDeleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 16, 16);
                deleteButton.setIcon(permanentDeleteIcon);
            } catch (Exception e) {
                deleteButton.setText("🗑");
                deleteButton.setForeground(Color.decode("#D97706"));
            }
            deleteButton.addActionListener(e -> permanentDeleteProduct(productId, productName, stockId));
        } else {
            deleteButton.setBackground(Color.decode("#FEF2F2"));
            deleteButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
            try {
                FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 16, 16);
                deleteButton.setIcon(deleteIcon);
            } catch (Exception e) {
                deleteButton.setText("×");
                deleteButton.setForeground(Color.decode("#EF4444"));
                deleteButton.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
            }
            deleteButton.addActionListener(e -> deleteProduct(productId, productName, stockId));
        }
        
        deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteButton.setFocusable(false);

        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        headerPanel.add(actionPanel, java.awt.BorderLayout.EAST);

        contentPanel.add(headerPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(8));

        // === SUPPLIER AND BADGES ROW ===
        javax.swing.JPanel supplierBadgePanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        supplierBadgePanel.setOpaque(false);
        supplierBadgePanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));

        // Supplier Label
        javax.swing.JLabel supplierLabel = new javax.swing.JLabel("● " + supplierName);
        supplierLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        if (pStatusId == 2) {
            supplierLabel.setForeground(Color.decode("#9CA3AF")); // Gray for inactive
        } else {
            supplierLabel.setForeground(Color.decode("#6366F1"));
        }
        supplierLabel.setToolTipText("Supplier: " + supplierName);
        supplierBadgePanel.add(supplierLabel, java.awt.BorderLayout.WEST);

        // Status badges on right side
        javax.swing.JPanel badgePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        badgePanel.setOpaque(false);

        // Inactive badge (highest priority)
        if (pStatusId == 2) {
            javax.swing.JLabel inactiveBadge = new javax.swing.JLabel("⛔ Inactive");
            inactiveBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
            inactiveBadge.setForeground(Color.WHITE);
            inactiveBadge.setBackground(Color.decode("#6B7280"));
            inactiveBadge.setOpaque(true);
            inactiveBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            inactiveBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.decode("#9CA3AF"), 1),
                    javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            badgePanel.add(inactiveBadge);
        }

        // Check if product is expired (only if active)
        if (pStatusId == 1) {
            boolean isExpired = false;
            try {
                java.time.LocalDate expiry = java.time.LocalDate.parse(expiryDate);
                java.time.LocalDate now = java.time.LocalDate.now();

                if (expiry.isBefore(now)) {
                    isExpired = true;
                    // EXPIRED BADGE
                    javax.swing.JLabel expiredBadge = new javax.swing.JLabel("⚠ Expired");
                    expiredBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
                    expiredBadge.setForeground(Color.decode("#7C2D12"));
                    expiredBadge.setBackground(Color.decode("#FED7AA"));
                    expiredBadge.setOpaque(true);
                    expiredBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    expiredBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            javax.swing.BorderFactory.createLineBorder(Color.decode("#FB923C"), 1),
                            javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10)
                    ));
                    badgePanel.add(expiredBadge);
                }
            } catch (Exception e) {
                // Invalid date format
            }

            // Check if expiring soon (only if not expired)
            if (!isExpired) {
                try {
                    java.time.LocalDate expiry = java.time.LocalDate.parse(expiryDate);
                    java.time.LocalDate now = java.time.LocalDate.now();
                    long monthsUntilExpiry = java.time.temporal.ChronoUnit.MONTHS.between(now, expiry);

                    if (monthsUntilExpiry <= 3 && monthsUntilExpiry >= 0) {
                        javax.swing.JLabel expiringBadge = new javax.swing.JLabel("⚠ Expiring Soon");
                        expiringBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
                        expiringBadge.setForeground(Color.decode("#92400E"));
                        expiringBadge.setBackground(Color.decode("#FEF3C7"));
                        expiringBadge.setOpaque(true);
                        expiringBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                        expiringBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                                javax.swing.BorderFactory.createLineBorder(Color.decode("#FDE047"), 1),
                                javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)
                        ));
                        badgePanel.add(expiringBadge);
                    }
                } catch (Exception e) {
                    // Invalid date format
                }
            }

            // Check if low stock (< 10 for pharmacy)
            if (qty < 10) {
                javax.swing.JLabel lowStockBadge = new javax.swing.JLabel("📉 Low Stock");
                lowStockBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
                lowStockBadge.setForeground(java.awt.Color.WHITE);
                lowStockBadge.setBackground(Color.decode("#DC2626"));
                lowStockBadge.setOpaque(true);
                lowStockBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                lowStockBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(Color.decode("#FDE047"), 1),
                        javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
                badgePanel.add(lowStockBadge);
            }
        }

        supplierBadgePanel.add(badgePanel, java.awt.BorderLayout.EAST);

        contentPanel.add(supplierBadgePanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        // === PRODUCT DETAILS HEADER ===
        javax.swing.JPanel detailsHeaderPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 20));

        javax.swing.JLabel detailsHeader = new javax.swing.JLabel("PRODUCT DETAILS");
        detailsHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
        if (pStatusId == 2) {
            detailsHeader.setForeground(Color.decode("#9CA3AF")); // Gray for inactive
        } else {
            detailsHeader.setForeground(Color.decode("#94A3B8"));
        }
        detailsHeaderPanel.add(detailsHeader);

        contentPanel.add(detailsHeaderPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        // === DETAILS GRID ===
        javax.swing.JPanel detailsGrid = new javax.swing.JPanel(new java.awt.GridLayout(3, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 180));

        // Row 1: Brand, Category
        detailsGrid.add(createDetailPanel("Brand", brandName, 
            pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#8B5CF6")));
        detailsGrid.add(createDetailPanel("Category", categoryName, 
            pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#EC4899")));

        // Row 2: Quantity, Expiry Date
        detailsGrid.add(createDetailPanel("Quantity", qty + " units", 
            pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#10B981")));
        detailsGrid.add(createDetailPanel("Expiry Date", expiryDate, 
            pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#F59E0B")));

        // Row 3: Batch No, Barcode WITH BUTTON
        detailsGrid.add(createDetailPanel("Batch No", batchNo, 
            pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#06B6D4")));
        detailsGrid.add(createBarcodePanel(barcode, pStatusId));

        contentPanel.add(detailsGrid);
        contentPanel.add(javax.swing.Box.createVerticalStrut(20));

        // === PRICING HEADER ===
        javax.swing.JPanel priceHeaderPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        priceHeaderPanel.setOpaque(false);
        priceHeaderPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 20));

        javax.swing.JLabel priceHeader = new javax.swing.JLabel("💰 PRICING");
        priceHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
        if (pStatusId == 2) {
            priceHeader.setForeground(Color.decode("#9CA3AF")); // Gray for inactive
        } else {
            priceHeader.setForeground(Color.decode("#94A3B8"));
        }
        priceHeaderPanel.add(priceHeader);

        contentPanel.add(priceHeaderPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(10));

        // === PRICING PANELS ===
        javax.swing.JPanel pricePanel = new javax.swing.JPanel(new java.awt.GridLayout(1, 3, 10, 0));
        pricePanel.setOpaque(false);
        pricePanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 55));

        // Purchase Price - Blue theme
        lk.com.pos.privateclasses.RoundedPanel purchasePanel = createPricePanel(
                "Purchase",
                purchasePrice,
                pStatusId == 2 ? Color.decode("#F3F4F6") : Color.decode("#DBEAFE"),
                pStatusId == 2 ? Color.decode("#6B7280") : Color.decode("#1E40AF")
        );

        // Last Price - Purple theme
        lk.com.pos.privateclasses.RoundedPanel lastPricePanel = createPricePanel(
                "Last Price",
                lastPrice,
                pStatusId == 2 ? Color.decode("#F3F4F6") : Color.decode("#F3E8FF"),
                pStatusId == 2 ? Color.decode("#6B7280") : Color.decode("#7C3AED")
        );

        // Selling Price - Green theme
        lk.com.pos.privateclasses.RoundedPanel sellingPanel = createPricePanel(
                "Selling",
                sellingPrice,
                pStatusId == 2 ? Color.decode("#F3F4F6") : Color.decode("#D1FAE5"),
                pStatusId == 2 ? Color.decode("#6B7280") : Color.decode("#059669")
        );

        pricePanel.add(purchasePanel);
        pricePanel.add(lastPricePanel);
        pricePanel.add(sellingPanel);

        contentPanel.add(pricePanel);

        card.add(contentPanel, java.awt.BorderLayout.CENTER);
        return card;
    }

    // Barcode panel with button to open dialog
    private javax.swing.JPanel createBarcodePanel(String barcode, int pStatusId) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        javax.swing.JLabel titleLabel = new javax.swing.JLabel("Barcode");
        titleLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 13));
        titleLabel.setForeground(pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#EF4444"));
        titleLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        // Panel for barcode value and button
        javax.swing.JPanel barcodeRow = new javax.swing.JPanel(new java.awt.BorderLayout(5, 0));
        barcodeRow.setOpaque(false);
        barcodeRow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 25));
        barcodeRow.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        javax.swing.JLabel valueLabel = new javax.swing.JLabel(barcode);
        valueLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        valueLabel.setForeground(pStatusId == 2 ? Color.decode("#6B7280") : Color.decode("#1E293B"));
        valueLabel.setToolTipText("Barcode: " + barcode);

        // Barcode button - opens dialog
        if (pStatusId == 1) { // Only show barcode button for active products
            try {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 18, 18);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.RED));

                JButton barcodeBtn = new JButton(normalIcon);
                barcodeBtn.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14));
                barcodeBtn.setPreferredSize(new java.awt.Dimension(35, 35));
                barcodeBtn.setBackground(Color.decode("#FEF2F2"));
                barcodeBtn.setForeground(Color.decode("#EF4444"));
                barcodeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                barcodeBtn.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
                barcodeBtn.setFocusable(false);
                barcodeBtn.setToolTipText("View Barcode Details");
                barcodeBtn.addActionListener(e -> openBarcodeDialog(barcode));

                barcodeRow.add(barcodeBtn, java.awt.BorderLayout.EAST);
            } catch (Exception e) {
                // If icon fails, create simple button
                JButton barcodeBtn = new JButton("📊");
                barcodeBtn.setPreferredSize(new java.awt.Dimension(35, 35));
                barcodeBtn.setBackground(Color.decode("#FEF2F2"));
                barcodeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                barcodeBtn.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
                barcodeBtn.setFocusable(false);
                barcodeBtn.addActionListener(ev -> openBarcodeDialog(barcode));
                barcodeRow.add(barcodeBtn, java.awt.BorderLayout.EAST);
            }
        }

        barcodeRow.add(valueLabel, java.awt.BorderLayout.CENTER);

        panel.add(titleLabel);
        panel.add(javax.swing.Box.createVerticalStrut(5));
        panel.add(barcodeRow);

        return panel;
    }

    // Method to open barcode dialog
    private void openBarcodeDialog(String barcode) {
        PrintProductLabel dialog = new PrintProductLabel(null, true, barcode);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private javax.swing.JPanel createDetailPanel(String title, String value, Color accentColor) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        javax.swing.JLabel titleLabel = new javax.swing.JLabel(title);
        titleLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 13));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        String displayValue = value;
        if (value != null && value.length() > 25) {
            displayValue = "<html><div style='width:140px;'>" + value + "</div></html>";
        }

        javax.swing.JLabel valueLabel = new javax.swing.JLabel(displayValue);
        valueLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        valueLabel.setForeground(Color.decode("#1E293B"));
        valueLabel.setToolTipText(value);
        valueLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(javax.swing.Box.createVerticalStrut(5));
        panel.add(valueLabel);

        return panel;
    }

    private lk.com.pos.privateclasses.RoundedPanel createPricePanel(String title, double price, Color bgColor, Color textColor) {
        lk.com.pos.privateclasses.RoundedPanel panel = new lk.com.pos.privateclasses.RoundedPanel();
        panel.setBackgroundColor(bgColor);
        panel.setBorderThickness(0);
        panel.setLayout(new java.awt.GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title Label
        javax.swing.JLabel titleLabel = new javax.swing.JLabel(title);
        titleLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12));
        titleLabel.setForeground(textColor);
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panel.add(titleLabel, gbc);

        // Price Label with smart formatting
        String formattedPrice = formatPrice(price);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(5, 0, 0, 0);

        javax.swing.JLabel priceLabel = new javax.swing.JLabel(formattedPrice);
        priceLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16));
        priceLabel.setForeground(textColor);
        priceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        priceLabel.setToolTipText(title + ": Rs." + String.format("%.2f", price));
        panel.add(priceLabel, gbc);

        return panel;
    }

    // Smart price formatting
    private String formatPrice(double price) {
        if (price >= 100000) {
            return String.format("Rs.%.1fK", price / 1000);
        } else if (price >= 10000) {
            return String.format("Rs.%.2fK", price / 1000);
        } else if (price >= 1000) {
            return String.format("Rs.%.0f", price);
        } else {
            return String.format("Rs.%.2f", price);
        }
    }

    private void editProduct(int productId, int stockId) {
        UpdateProduct updateProduct = new UpdateProduct(null, true, productId, stockId);
        updateProduct.setLocationRelativeTo(null);
        updateProduct.setVisible(true);
        SearchFilters();
    }

    private void deleteProduct(int productId, String productName, int stockId) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to deactivate '" + productName + "'?\n"
                + "Note: Product will be marked as inactive and hidden from the active product list.",
                "Confirm Deactivate",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                // Check if this specific stock has sales history using the new schema
                String checkSalesQuery = "SELECT COUNT(*) as sale_count FROM sale_item WHERE stock_id = " + stockId;
                ResultSet rs = MySQL.executeSearch(checkSalesQuery);
                
                boolean hasSales = false;
                if (rs.next()) {
                    hasSales = rs.getInt("sale_count") > 0;
                }

                // Update product status to Inactive (p_status_id = 2) instead of deleting
                String updateQuery = "UPDATE product SET p_status_id = 2 WHERE product_id = " + productId;
                MySQL.executeIUD(updateQuery);

                String message = "Product '" + productName + "' has been deactivated successfully!\n" +
                                "You can view it in the 'Inactive Products' section.";
                
                if (hasSales) {
                    message += "\n\nNote: This product batch has sales history and cannot be permanently deleted.";
                }

                javax.swing.JOptionPane.showMessageDialog(this,
                        message,
                        "Success",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

                // Reload products with current filter
                SearchFilters();

            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error deactivating product: " + e.getMessage(),
                        "Database Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reactivateProduct(int productId, String productName) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reactivate '" + productName + "'?\n"
                + "Product will be available in the active product list.",
                "Confirm Reactivate",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                // Update product status to Active (p_status_id = 1)
                String updateQuery = "UPDATE product SET p_status_id = 1 WHERE product_id = " + productId;
                MySQL.executeIUD(updateQuery);

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Product '" + productName + "' has been reactivated successfully!",
                        "Success",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

                // Reload products with current filter
                SearchFilters();

            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error reactivating product: " + e.getMessage(),
                        "Database Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void permanentDeleteProduct(int productId, String productName, int stockId) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "⚠️ WARNING: This will permanently delete '" + productName + "'!\n\n"
                + "This action cannot be undone.\n"
                + "All stock records and product information will be lost.\n\n"
                + "Are you absolutely sure?",
                "PERMANENT DELETE CONFIRMATION",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.ERROR_MESSAGE
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                // UPDATED: Check sales history by stock_id instead of product_id
                String checkSalesQuery = "SELECT COUNT(*) as sale_count FROM sale_item WHERE stock_id = " + stockId;
                ResultSet rs = MySQL.executeSearch(checkSalesQuery);

                if (rs.next() && rs.getInt("sale_count") > 0) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Cannot delete '" + productName + "'!\n\n"
                            + "This product has sales history and cannot be permanently deleted.\n"
                            + "It must remain in the system for record keeping purposes.",
                            "Delete Not Allowed",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Delete stock entries first
                String deleteStockQuery = "DELETE FROM stock WHERE stock_id = " + stockId;
                MySQL.executeIUD(deleteStockQuery);

                // Check if there are other stock entries for this product
                String checkOtherStockQuery = "SELECT COUNT(*) as other_stock FROM stock WHERE product_id = " + productId;
                ResultSet otherStockRs = MySQL.executeSearch(checkOtherStockQuery);
                
                boolean hasOtherStock = false;
                if (otherStockRs.next()) {
                    hasOtherStock = otherStockRs.getInt("other_stock") > 0;
                }

                // Only delete the product if no other stock entries exist
                if (!hasOtherStock) {
                    String deleteProductQuery = "DELETE FROM product WHERE product_id = " + productId;
                    MySQL.executeIUD(deleteProductQuery);
                }

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Product '" + productName + "' has been permanently deleted from the system." +
                        (hasOtherStock ? "\nNote: Other stock entries for this product still exist." : ""),
                        "Permanent Delete Complete",
                        javax.swing.JOptionPane.WARNING_MESSAGE);

                // Reload products with current filter
                SearchFilters();

            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error permanently deleting product: " + e.getMessage(),
                        "Database Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void radioButtonListener() {
        // Allow deselecting radio buttons by clicking again
        expiringRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (expiringRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    SearchFilters();
                    evt.consume();
                }
            }
        });

        lowStockRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (lowStockRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    SearchFilters();
                    evt.consume();
                }
            }
        });

        expiredRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (expiredRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    SearchFilters();
                    evt.consume();
                }
            }
        });

        inactiveRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (inactiveRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    SearchFilters();
                    evt.consume();
                }
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        productSearchBar = new javax.swing.JTextField();
        expiringRadioBtn = new javax.swing.JRadioButton();
        lowStockRadioBtn = new javax.swing.JRadioButton();
        expiredRadioBtn = new javax.swing.JRadioButton();
        addProductDialog = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel2 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();
        deleteBtn = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        roundedPanel3 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        roundedPanel4 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        inactiveRadioBtn = new javax.swing.JRadioButton();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        productSearchBar.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        productSearchBar.setText("Search By Product Name Or Barcode");

        buttonGroup1.add(expiringRadioBtn);
        expiringRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        expiringRadioBtn.setText("Expiring Soon Products");

        buttonGroup1.add(lowStockRadioBtn);
        lowStockRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        lowStockRadioBtn.setForeground(new java.awt.Color(255, 153, 0));
        lowStockRadioBtn.setText("Low Stock Products");

        buttonGroup1.add(expiredRadioBtn);
        expiredRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        expiredRadioBtn.setForeground(new java.awt.Color(255, 51, 51));
        expiredRadioBtn.setText("Expired");
        expiredRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expiredRadioBtnActionPerformed(evt);
            }
        });

        addProductDialog.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addProductDialog.setText("Add Stock");
        addProductDialog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProductDialogActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel19.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel19.setText("Product Name :");

        jLabel4.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 102, 102));
        jLabel4.setText("Supplier : Wellness Co");

        jButton7.setBackground(new java.awt.Color(255, 255, 204));
        jButton7.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton7.setText("Expiring Soon");

        jButton8.setBackground(new java.awt.Color(255, 51, 51));
        jButton8.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton8.setText("Low Stock");

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        deleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(3, 0));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel15.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel15.setText("Brand");

        jLabel20.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 102, 102));
        jLabel20.setText("Nature's Best");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel9);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel21.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel21.setText("Category");

        jLabel26.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(102, 102, 102));
        jLabel26.setText("Beverages");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(295, 295, 295))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel26)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel8);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        jLabel27.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel27.setText("Quantity");

        jLabel28.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(102, 102, 102));
        jLabel28.setText("10");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel10);

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));

        jLabel29.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel29.setText("Expiry Date");

        jLabel30.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(102, 102, 102));
        jLabel30.setText("2025-12-20");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(20, 20, 20))
                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(343, 343, 343))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel30)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel11);

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));

        jLabel31.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel31.setText("Batch No");

        jLabel32.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(102, 102, 102));
        jLabel32.setText("BCH01");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel12);

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));

        jLabel34.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(102, 102, 102));
        jLabel34.setText("4563258745");

        jLabel33.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel33.setText("Barcode");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(45, 45, 45))
                    .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(343, 343, 343))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel34)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel13);

        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));

        roundedPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel36.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel36.setText("Purchase Price");

        jLabel35.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(102, 102, 102));
        jLabel35.setText("Rs.40000");

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel35)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel37.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel37.setText("Last Price");

        jLabel38.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(102, 102, 102));
        jLabel38.setText("Rs.40000");

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel37)
                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        roundedPanel3Layout.setVerticalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel38)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel39.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel39.setText("Selling Price");

        jLabel40.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(102, 102, 102));
        jLabel40.setText("Rs.40000");

        javax.swing.GroupLayout roundedPanel4Layout = new javax.swing.GroupLayout(roundedPanel4);
        roundedPanel4.setLayout(roundedPanel4Layout);
        roundedPanel4Layout.setHorizontalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundedPanel4Layout.setVerticalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel40)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(roundedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSeparator3)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8))
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        roundedPanel2Layout.setVerticalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(deleteBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jButton7)
                    .addComponent(jButton8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(roundedPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(13, 13, 13))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(652, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(2028, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        buttonGroup1.add(inactiveRadioBtn);
        inactiveRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        inactiveRadioBtn.setForeground(new java.awt.Color(255, 51, 51));
        inactiveRadioBtn.setText("Inactivie");
        inactiveRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inactiveRadioBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(productSearchBar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(expiringRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lowStockRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(expiredRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inactiveRadioBtn)
                        .addGap(95, 95, 95)
                        .addComponent(addProductDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expiringRadioBtn)
                    .addComponent(lowStockRadioBtn)
                    .addComponent(expiredRadioBtn)
                    .addComponent(addProductDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inactiveRadioBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void expiredRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expiredRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_expiredRadioBtnActionPerformed

    private void addProductDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProductDialogActionPerformed
        AddNewProduct addProduct = new AddNewProduct(null, true);
        addProduct.setLocationRelativeTo(null);
        addProduct.setVisible(true);
    }//GEN-LAST:event_addProductDialogActionPerformed

    private void inactiveRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inactiveRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inactiveRadioBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addProductDialog;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JRadioButton expiredRadioBtn;
    private javax.swing.JRadioButton expiringRadioBtn;
    private javax.swing.JRadioButton inactiveRadioBtn;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JRadioButton lowStockRadioBtn;
    private javax.swing.JTextField productSearchBar;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel4;
    // End of variables declaration//GEN-END:variables
}
