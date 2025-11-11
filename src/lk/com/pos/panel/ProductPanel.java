package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import lk.com.pos.connection.MySQL;
import lk.com.pos.dialog.AddNewProduct;
import lk.com.pos.dialog.UpdateProductStock;

public class ProductPanel extends javax.swing.JPanel {

    public ProductPanel() {
        initComponents();
        
        // Configure scroll pane
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        
        init();
        loadProducts();
        setupEventListeners();
        radioButtonListener();
    }

    private void init() {
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

        // Radio buttons - trigger search on selection
        activeRadioBtn.addActionListener(e -> SearchFilters());
        inactiveRadioBtn.addActionListener(e -> SearchFilters());
    }

    private void SearchFilters() {
        String searchText = productSearchBar.getText().trim();

        String status = "all";

        if (activeRadioBtn.isSelected()) {
            status = "Active";
        } else if (inactiveRadioBtn.isSelected()) {
            status = "Inactive";
        }

        loadProducts(searchText, status);
    }

    private void radioButtonListener() {
        // Allow deselecting radio buttons by clicking again
        activeRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (activeRadioBtn.isSelected()) {
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

    private void loadProducts() {
        loadProducts("", "all");
    }

    private void loadProducts(String searchText, String status) {
        try {
            // Query to get distinct products (not stock-specific)
            String query = "SELECT DISTINCT "
                    + "product.product_id, "
                    + "product.product_name, "
                    + "brand.brand_name, "
                    + "category.category_name, "
                    + "product.barcode, "
                    + "product.p_status_id "
                    + "FROM product "
                    + "JOIN category ON category.category_id = product.category_id "
                    + "JOIN brand ON brand.brand_id = product.brand_id "
                    + "WHERE 1=1 ";

            // Add status filter
            if ("Active".equals(status)) {
                query += "AND product.p_status_id = 1 ";
            } else if ("Inactive".equals(status)) {
                query += "AND product.p_status_id = 2 ";
            }

            // Add search filter
            if (searchText != null && !searchText.trim().isEmpty()
                    && !searchText.equals("Search By Product Name Or Barcode")) {
                query += "AND (product.product_name LIKE '%" + searchText + "%' "
                        + "OR product.barcode LIKE '%" + searchText + "%') ";
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

                lk.com.pos.privateclasses.RoundedPanel productCard = createProductCard(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("brand_name"),
                        rs.getString("category_name"),
                        rs.getString("barcode"),
                        rs.getInt("p_status_id")
                );

                productCards.add(productCard);
            }

            // If no products found
            if (productCount == 0) {
                jPanel2.setLayout(new java.awt.BorderLayout());

                javax.swing.JPanel messagePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
                messagePanel.setBackground(Color.decode("#F8FAFC"));
                messagePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 0, 0, 0));

                String message = "No products available";
                if ("Active".equals(status)) {
                    message = "No active products available";
                } else if ("Inactive".equals(status)) {
                    message = "No inactive products available";
                } else if (searchText != null && !searchText.trim().isEmpty() 
                           && !searchText.equals("Search By Product Name Or Barcode")) {
                    message = "No products found matching: " + searchText;
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

                // Calculate initial columns
                int initialColumns = calculateColumns(jPanel2.getWidth());
                gridPanel.setLayout(new java.awt.GridLayout(0, initialColumns, 25, 25));

                // Add all product cards
                for (lk.com.pos.privateclasses.RoundedPanel card : productCards) {
                    gridPanel.add(card);
                }

                jPanel2.setLayout(new java.awt.BorderLayout());

                // Create main container
                javax.swing.JPanel mainContainer = new javax.swing.JPanel();
                mainContainer.setLayout(new javax.swing.BoxLayout(mainContainer, javax.swing.BoxLayout.Y_AXIS));
                mainContainer.setBackground(Color.decode("#F8FAFC"));

                // Add padding
                javax.swing.JPanel paddingPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
                paddingPanel.setBackground(Color.decode("#F8FAFC"));
                paddingPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25));
                paddingPanel.add(gridPanel, java.awt.BorderLayout.NORTH);

                mainContainer.add(paddingPanel);
                jPanel2.add(mainContainer, java.awt.BorderLayout.NORTH);

                // Add resize listener
                jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
                    private int lastColumns = initialColumns;

                    @Override
                    public void componentResized(java.awt.event.ComponentEvent e) {
                        int panelWidth = jPanel2.getWidth();
                        int newColumns = calculateColumns(panelWidth);

                        if (newColumns != lastColumns) {
                            lastColumns = newColumns;
                            gridPanel.setLayout(new java.awt.GridLayout(0, newColumns, 25, 25));
                            gridPanel.revalidate();
                            gridPanel.repaint();
                        }
                    }
                });
            }

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
            int productId, String productName, String brandName,
            String categoryName, String barcode, int pStatusId) {

        // Create main rounded panel
        lk.com.pos.privateclasses.RoundedPanel card = new lk.com.pos.privateclasses.RoundedPanel();
        card.setLayout(new java.awt.BorderLayout());
        card.setPreferredSize(new java.awt.Dimension(420, 280));
        card.setMaximumSize(new java.awt.Dimension(420, 280));
        card.setMinimumSize(new java.awt.Dimension(380, 280));
        
        // Set background based on status
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

        // Edit Button
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
        }
        editButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#BFDBFE"), 1));
        editButton.setFocusable(false);
        
        if (pStatusId == 2) {
            // Inactive - show activate button
            editButton.setBackground(Color.decode("#D1FAE5"));
            editButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#10B981"), 1));
            try {
                FlatSVGIcon activateIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 16, 16);
                editButton.setIcon(activateIcon);
            } catch (Exception e) {
                editButton.setText("↻");
                editButton.setForeground(Color.decode("#059669"));
            }
            editButton.addActionListener(e -> activateProduct(productId, productName));
        } else {
            editButton.addActionListener(e -> editProduct(productId));
        }

        // Delete/Deactivate Button
        javax.swing.JButton deleteButton = new javax.swing.JButton();
        deleteButton.setPreferredSize(new java.awt.Dimension(30, 30));
        deleteButton.setMinimumSize(new java.awt.Dimension(30, 30));
        deleteButton.setMaximumSize(new java.awt.Dimension(30, 30));
        
        if (pStatusId == 2) {
            // Already inactive - don't show deactivate button
            deleteButton.setVisible(false);
        } else {
            // Active - show deactivate button
            deleteButton.setBackground(Color.decode("#FEF2F2"));
            deleteButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
            deleteButton.addActionListener(e -> deactivateProduct(productId, productName));
        }
        
        try {
            FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 16, 16);
            deleteButton.setIcon(deleteIcon);
        } catch (Exception e) {
            deleteButton.setText("×");
            deleteButton.setForeground(Color.decode("#EF4444"));
        }
        
        deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteButton.setFocusable(false);

        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        headerPanel.add(actionPanel, java.awt.BorderLayout.EAST);

        contentPanel.add(headerPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(8));

        // === SUPPLIER AND STATUS BADGE ROW ===
        javax.swing.JPanel supplierStatusPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        supplierStatusPanel.setOpaque(false);
        supplierStatusPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));

        // Get supplier name for this product
        String supplierName = getSupplierForProduct(productId);
        
        // Supplier Label on LEFT
        javax.swing.JLabel supplierLabel = new javax.swing.JLabel("Supplier: " + supplierName);
        supplierLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        if (pStatusId == 2) {
            supplierLabel.setForeground(Color.decode("#9CA3AF")); // Gray for inactive
        } else {
            supplierLabel.setForeground(Color.decode("#6366F1"));
        }
        supplierLabel.setToolTipText("Supplier: " + supplierName);
        supplierStatusPanel.add(supplierLabel, java.awt.BorderLayout.WEST);

        // Status Badge on RIGHT
        javax.swing.JPanel badgePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);

        if (pStatusId == 2) {
            javax.swing.JLabel inactiveBadge = new javax.swing.JLabel("⛔ Inactive");
            inactiveBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
            inactiveBadge.setForeground(Color.WHITE);
            inactiveBadge.setBackground(Color.decode("#EF4444"));
            inactiveBadge.setOpaque(true);
            inactiveBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.decode("#DC2626"), 1),
                    javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            badgePanel.add(inactiveBadge);
        } else {
            javax.swing.JLabel activeBadge = new javax.swing.JLabel("✓ Active");
            activeBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
            activeBadge.setForeground(Color.decode("#065F46"));
            activeBadge.setBackground(Color.decode("#D1FAE5"));
            activeBadge.setOpaque(true);
            activeBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.decode("#10B981"), 1),
                    javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            badgePanel.add(activeBadge);
        }

        supplierStatusPanel.add(badgePanel, java.awt.BorderLayout.EAST);

        contentPanel.add(supplierStatusPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(20));

        // === DETAILS HEADER ON RIGHT ===
        javax.swing.JPanel detailsHeaderPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 20));

        javax.swing.JLabel detailsHeader = new javax.swing.JLabel("PRODUCT DETAILS");
        detailsHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
        detailsHeader.setForeground(Color.decode("#94A3B8"));
        detailsHeaderPanel.add(detailsHeader);

        contentPanel.add(detailsHeaderPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        // === DETAILS GRID ===
        javax.swing.JPanel detailsGrid = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 120));

        // Brand, Category, Barcode
        detailsGrid.add(createDetailPanel("Brand", brandName, 
            pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#8B5CF6")));
        detailsGrid.add(createDetailPanel("Category", categoryName, 
            pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#EC4899")));
        detailsGrid.add(createDetailPanel("Barcode", barcode, 
            pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#EF4444")));

        contentPanel.add(detailsGrid);

        card.add(contentPanel, java.awt.BorderLayout.CENTER);
        return card;
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

    // Method to get supplier name for a product
    private String getSupplierForProduct(int productId) {
        try {
            String query = "SELECT suppliers.suppliers_name FROM stock "
                    + "JOIN suppliers ON suppliers.suppliers_id = stock.suppliers_id "
                    + "WHERE stock.product_id = " + productId + " LIMIT 1";
            ResultSet rs = MySQL.executeSearch(query);
            
            if (rs.next()) {
                return rs.getString("suppliers_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private void editProduct(int productId) {
        // Get the first stock_id for this product
        try {
            String query = "SELECT stock_id FROM stock WHERE product_id = " + productId + " LIMIT 1";
            ResultSet rs = MySQL.executeSearch(query);
            
            if (rs.next()) {
                int stockId = rs.getInt("stock_id");
                UpdateProductStock updateProduct = new UpdateProductStock(null, true, productId, stockId);
                updateProduct.setLocationRelativeTo(null);
                updateProduct.setVisible(true);
                SearchFilters();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "No stock records found for this product!",
                        "Error",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deactivateProduct(int productId, String productName) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to deactivate '" + productName + "'?\n"
                + "Note: Product will be marked as inactive and hidden from active products.",
                "Confirm Deactivate",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                String updateQuery = "UPDATE product SET p_status_id = 2 WHERE product_id = " + productId;
                MySQL.executeIUD(updateQuery);

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Product '" + productName + "' has been deactivated successfully!\n"
                        + "You can view it in the 'Inactive Products' filter.",
                        "Success",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

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

    private void activateProduct(int productId, String productName) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to activate '" + productName + "'?\n"
                + "Product will be available in the active product list.",
                "Confirm Activate",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                String updateQuery = "UPDATE product SET p_status_id = 1 WHERE product_id = " + productId;
                MySQL.executeIUD(updateQuery);

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Product '" + productName + "' has been activated successfully!",
                        "Success",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

                SearchFilters();
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error activating product: " + e.getMessage(),
                        "Database Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        productSearchBar = new javax.swing.JTextField();
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
        jPanel13 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        inactiveRadioBtn = new javax.swing.JRadioButton();
        activeRadioBtn = new javax.swing.JRadioButton();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        productSearchBar.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        productSearchBar.setText("Search By Product Name Or Barcode");

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
        jButton7.setText("Active");

        jButton8.setBackground(new java.awt.Color(255, 51, 51));
        jButton8.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton8.setText("Inactive");

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        deleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(2, 2));

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
                .addContainerGap(72, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addContainerGap(15, Short.MAX_VALUE))
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

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                .addGap(9, 9, 9))
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
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
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

        buttonGroup1.add(activeRadioBtn);
        activeRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        activeRadioBtn.setForeground(new java.awt.Color(51, 51, 51));
        activeRadioBtn.setText("Activie");
        activeRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeRadioBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(productSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 562, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(activeRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inactiveRadioBtn)
                        .addGap(18, 18, 18)
                        .addComponent(addProductDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addProductDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inactiveRadioBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(activeRadioBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addProductDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProductDialogActionPerformed
       
    }//GEN-LAST:event_addProductDialogActionPerformed

    private void inactiveRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inactiveRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inactiveRadioBtnActionPerformed

    private void activeRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_activeRadioBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton activeRadioBtn;
    private javax.swing.JButton addProductDialog;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JRadioButton inactiveRadioBtn;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField productSearchBar;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    // End of variables declaration//GEN-END:variables
}
