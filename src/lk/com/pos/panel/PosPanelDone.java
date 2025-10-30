package lk.com.pos.panel;

import lk.com.pos.privateclasses.RoundedPanel;
import lk.com.pos.connection.MySQL;
import com.formdev.flatlaf.FlatClientProperties;
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
import lk.com.pos.privateclasses.CartListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

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

public class PosPanelDone extends javax.swing.JPanel implements CartListener {
    
    @Override
    public void onCartUpdated(double total, int itemCount) {
        System.out.println("Cart updated: " + itemCount + " items, Total: Rs." + total);
    }

    @Override
    public void onCheckoutComplete() {
        System.out.println("Checkout completed");
    }

    private static final Color TEAL_COLOR = new Color(28, 181, 187);
    private static final Color LIGHT_GRAY_BG = new Color(245, 245, 245);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color TEXT_GRAY = new Color(102, 102, 102);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color CARD_HOVER = new Color(240, 250, 250);

    private PosCartPanel posCartPanel;
    private List<RoundedPanel> productCards = new ArrayList<>();
    private int currentCardIndex = -1;
    private boolean keyboardNavigationEnabled = false;
    private int columnsPerRow = 2;
    private javax.swing.Timer searchTimer;
    
    public PosPanelDone() {
        initComponents();
        init();
        javax.swing.SwingUtilities.invokeLater(() -> {
            loadProduct();
        });
    }

    private void init() {
        setupKeyboardNavigation();
        setupSearchFunctionality();
        setupSearchShortcut(); // Add keyboard shortcut functionality
        selectProductPanel.putClientProperty(FlatClientProperties.STYLE, "arc:15;");
        selectProductPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(Color.WHITE, 1, 15),
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
        jPanel7.setLayout(new WrapLayout(FlowLayout.LEADING, 15, 15));
        jPanel7.setBackground(Color.WHITE);

        // Add component listener for responsive resizing
        jScrollPane2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                updateProductCardSizes();
                calculateColumnsPerRow();
            }
        });

        // Set initial placeholder state
        setupSearchBarPlaceholder();

        // Add hierarchy listener to detect when component is shown
        addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            updateProductCardSizes();
                            calculateColumnsPerRow();
                        });
                    }
                }
            }
        });

        // Simple scrollbar styling with FlatLaf
        jScrollPane2.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");
    }
    
    private void setupSearchShortcut() {
        // Create the focus search action
        javax.swing.Action focusSearchAction = new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                productSearchBar.requestFocusInWindow();
                if (productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
                    productSearchBar.setText("");
                    productSearchBar.setForeground(java.awt.Color.BLACK);
                }
                productSearchBar.selectAll();
            }
        };

        // Get the input map and action map for WHEN_IN_FOCUSED_WINDOW (global)
        javax.swing.InputMap globalInputMap = getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap globalActionMap = getActionMap();

        // Define the keys for both F and Ctrl+F
        String fKey = "FocusSearchF";
        String ctrlFKey = "FocusSearchCtrlF";

        // Bind F key (no modifiers)
        globalInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0), fKey);
        globalActionMap.put(fKey, focusSearchAction);

        // Bind Ctrl+F
        globalInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK), ctrlFKey);
        globalActionMap.put(ctrlFKey, focusSearchAction);

        // Also set up for ancestor focus for broader coverage
        javax.swing.InputMap ancestorInputMap = getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ancestorInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0), fKey);
        ancestorInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK), ctrlFKey);

        // Setup for the scroll pane
        javax.swing.InputMap scrollInputMap = jScrollPane2.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        javax.swing.ActionMap scrollActionMap = jScrollPane2.getActionMap();
        
        scrollInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0), fKey);
        scrollInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK), ctrlFKey);
        scrollActionMap.put(fKey, focusSearchAction);
        scrollActionMap.put(ctrlFKey, focusSearchAction);

        // Setup for products panel
        javax.swing.InputMap panelInputMap = jPanel7.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        javax.swing.ActionMap panelActionMap = jPanel7.getActionMap();
        
        panelInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0), fKey);
        panelInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK), ctrlFKey);
        panelActionMap.put(fKey, focusSearchAction);
        panelActionMap.put(ctrlFKey, focusSearchAction);

        // Setup for cart panel
        javax.swing.InputMap cartInputMap = cartPanel.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        javax.swing.ActionMap cartActionMap = cartPanel.getActionMap();
        
        cartInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0), fKey);
        cartInputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK), ctrlFKey);
        cartActionMap.put(fKey, focusSearchAction);
        cartActionMap.put(ctrlFKey, focusSearchAction);
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
        // Create a timer for delayed search (400ms after user stops typing)
        searchTimer = new javax.swing.Timer(400, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                performSearch();
            }
        });
        searchTimer.setRepeats(false);
        
        // Add document listener to search bar for real-time search with delay
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
                // Only search if it's not placeholder text
                if (!productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
                    searchTimer.restart();
                }
            }
        });
        
        // Add clear button functionality to reload button
        reloadBtn.setToolTipText("Refresh products and clear search");
    }
    
    private void performSearch() {
        String searchText = productSearchBar.getText().trim();
        
        // Don't search if it's placeholder text or empty
        if (productSearchBar.getForeground().equals(java.awt.Color.GRAY) || searchText.isEmpty()) {
            loadProduct(""); // Load all products
        } else {
            loadProduct(searchText);
        }
    }
    
    private void clearSearch() {
        productSearchBar.setText("");
        productSearchBar.setForeground(java.awt.Color.GRAY);
        productSearchBar.setText("Search products by name or barcode...");
        loadProduct("");
        requestFocusInWindow(); // Return focus to main panel
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
        // Enable keyboard focus for the panel
        setFocusable(true);
        requestFocusInWindow();
        
        // Add key listener to the main panel
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        
        // Add key listener to search bar
        productSearchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleSearchBarKeyPress(e);
            }
        });
        
        // Add key listener to scroll pane
        jScrollPane2.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        
        // Add key listener to products panel
        jPanel7.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        
        // Ensure components are focusable
        jScrollPane2.setFocusable(true);
        jPanel7.setFocusable(true);
        productSearchBar.setFocusable(true);
    }
    
    private void handleSearchBarKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                // Escape clears search and returns focus
                clearSearch();
                e.consume();
                break;
            case KeyEvent.VK_ENTER:
                // Enter performs immediate search
                searchTimer.stop();
                performSearch();
                e.consume();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
                // Arrow keys in search bar return focus to main panel for navigation
                requestFocusInWindow();
                if (productCards.size() > 0) {
                    selectCardByIndex(0);
                }
                e.consume();
                break;
        }
    }
    
    private void handleKeyPress(KeyEvent e) {
        // Handle keyboard shortcuts
        if (e.isControlDown()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_F: // Ctrl+F - Focus search
                    productSearchBar.requestFocusInWindow();
                    if (productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
                        productSearchBar.setText("");
                        productSearchBar.setForeground(java.awt.Color.BLACK);
                    }
                    productSearchBar.selectAll();
                    e.consume();
                    return;
                case KeyEvent.VK_R: // Ctrl+R - Refresh
                    reloadBtnActionPerformed(null);
                    e.consume();
                    return;
                case KeyEvent.VK_L: // Ctrl+L - Clear search
                    clearSearch();
                    e.consume();
                    return;
            }
        }
        
        // Single key shortcuts (no modifier needed)
        switch (e.getKeyCode()) {
            case KeyEvent.VK_F: // F key alone - Focus search
                productSearchBar.requestFocusInWindow();
                if (productSearchBar.getForeground().equals(java.awt.Color.GRAY)) {
                    productSearchBar.setText("");
                    productSearchBar.setForeground(java.awt.Color.BLACK);
                }
                productSearchBar.selectAll();
                e.consume();
                return;
            case KeyEvent.VK_F5: // F5 - Refresh
                reloadBtnActionPerformed(null);
                e.consume();
                return;
            case KeyEvent.VK_ESCAPE: // Escape - Clear search
                if (productSearchBar.hasFocus()) {
                    clearSearch();
                } else {
                    clearCardSelection();
                }
                e.consume();
                return;
        }
        
        // Handle arrow key navigation (only when not in search bar)
        if (!productSearchBar.hasFocus() && !productCards.isEmpty()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    navigateDown();
                    e.consume();
                    break;
                case KeyEvent.VK_UP:
                    navigateUp();
                    e.consume();
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
                        selectCurrentCard();
                        e.consume();
                    }
                    break;
            }
        }
    }
    
    private void navigateDown() {
        if (productCards.isEmpty() || columnsPerRow == 0) return;
        
        int newIndex;
        if (currentCardIndex == -1) {
            newIndex = 0;
        } else {
            newIndex = currentCardIndex + columnsPerRow;
            if (newIndex >= productCards.size()) {
                newIndex = Math.min(productCards.size() - 1, currentCardIndex + (currentCardIndex % columnsPerRow));
            }
        }
        
        selectCardByIndex(newIndex);
        ensureCardVisible(newIndex);
    }
    
    private void navigateUp() {
        if (productCards.isEmpty() || columnsPerRow == 0) return;
        
        int newIndex;
        if (currentCardIndex == -1) {
            newIndex = 0;
        } else {
            newIndex = currentCardIndex - columnsPerRow;
            if (newIndex < 0) {
                int currentColumn = currentCardIndex % columnsPerRow;
                int lastRowStart = (productCards.size() - 1) / columnsPerRow * columnsPerRow;
                newIndex = Math.min(lastRowStart + currentColumn, productCards.size() - 1);
            }
        }
        
        selectCardByIndex(newIndex);
        ensureCardVisible(newIndex);
    }
    
    private void navigateRight() {
        if (productCards.isEmpty() || columnsPerRow == 0) return;
        
        int newIndex;
        if (currentCardIndex == -1) {
            newIndex = 0;
        } else {
            newIndex = currentCardIndex + 1;
            if (newIndex % columnsPerRow == 0 || newIndex >= productCards.size()) {
                int currentRow = currentCardIndex / columnsPerRow;
                newIndex = currentRow * columnsPerRow;
            }
        }
        
        selectCardByIndex(Math.min(newIndex, productCards.size() - 1));
        ensureCardVisible(Math.min(newIndex, productCards.size() - 1));
    }
    
    private void navigateLeft() {
        if (productCards.isEmpty() || columnsPerRow == 0) return;
        
        int newIndex;
        if (currentCardIndex == -1) {
            newIndex = 0;
        } else {
            newIndex = currentCardIndex - 1;
            if (newIndex < 0 || (currentCardIndex % columnsPerRow == 0 && newIndex < currentCardIndex)) {
                int currentRow = currentCardIndex / columnsPerRow;
                if (currentRow > 0) {
                    newIndex = (currentRow * columnsPerRow) - 1;
                } else {
                    int lastRowStart = (productCards.size() - 1) / columnsPerRow * columnsPerRow;
                    int lastRowColumns = productCards.size() - lastRowStart;
                    newIndex = lastRowStart + (lastRowColumns - 1);
                }
            }
        }
        
        selectCardByIndex(Math.max(0, newIndex));
        ensureCardVisible(Math.max(0, newIndex));
    }

    private void selectCardByIndex(int index) {
        if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
            RoundedPanel previousCard = productCards.get(currentCardIndex);
            setCardSelection(previousCard, false);
        }
        
        currentCardIndex = index;
        RoundedPanel currentCard = productCards.get(currentCardIndex);
        setCardSelection(currentCard, true);
        
        requestFocusInWindow();
    }

    private void setCardSelection(RoundedPanel card, boolean selected) {
        if (selected) {
            card.setBackground(new Color(200, 245, 245));
            card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(TEAL_COLOR, 3, 20),
                    BorderFactory.createEmptyBorder(16, 18, 16, 18)
            ));
        } else {
            card.setBackground(CARD_BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(BORDER_COLOR, 1, 20),
                    BorderFactory.createEmptyBorder(16, 18, 16, 18)
            ));
        }
        card.repaint();
    }

    private void selectCurrentCard() {
        addSelectedProductToCart();
    }

    private void clearCardSelection() {
        if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
            RoundedPanel currentCard = productCards.get(currentCardIndex);
            setCardSelection(currentCard, false);
        }
        currentCardIndex = -1;
    }

    private void ensureCardVisible(int index) {
        if (index < 0 || index >= productCards.size()) return;
        
        RoundedPanel card = productCards.get(index);
        java.awt.Rectangle cardRect = card.getBounds();
        java.awt.Rectangle viewRect = jScrollPane2.getViewport().getViewRect();
        
        int y = cardRect.y - (viewRect.height - cardRect.height) / 2;
        y = Math.max(0, Math.min(y, jPanel7.getHeight() - viewRect.height));
        
        jScrollPane2.getViewport().setViewPosition(new java.awt.Point(0, y));
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

    private void SearchFilters() {
        // This method is kept for backward compatibility
        performSearch();
    }

    private void loadProduct() {
        loadProduct("");
    }

    private void loadProduct(String productSearch) {
        try {
            jPanel7.removeAll();
            productCards.clear();
            currentCardIndex = -1;
            
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

            if (productSearch != null && !productSearch.isEmpty()) {
                query += "AND (product.product_name LIKE '%" + productSearch + "%' "
                        + "OR product.barcode LIKE '%" + productSearch + "%') ";
            }

            query += "ORDER BY product.product_name ASC";

            ResultSet rs = MySQL.executeSearch(query);

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                String brandName = rs.getString("brand_name");
                String batchNo = rs.getString("batch_no");
                int qty = rs.getInt("qty");
                double sellingPrice = rs.getDouble("selling_price");
                String barcode = rs.getString("barcode");
                double lastPrice = rs.getDouble("last_price");

                RoundedPanel productCard = createProductCard(
                        productId, productName, brandName, batchNo, qty, sellingPrice, barcode, lastPrice
                );

                jPanel7.add(productCard);
                productCards.add(productCard);
            }

            if (jPanel7.getComponentCount() == 0) {
                JPanel messagePanel = new JPanel(new java.awt.GridBagLayout());
                messagePanel.setBackground(Color.WHITE);
                messagePanel.setPreferredSize(new Dimension(jScrollPane2.getViewport().getWidth(), 400));

                String message = productSearch != null && !productSearch.isEmpty() 
                    ? "No products found for: '" + productSearch + "'" 
                    : "No products available";
                
                JLabel noProductLabel = new JLabel(message);
                noProductLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 18));
                noProductLabel.setForeground(TEXT_GRAY);

                messagePanel.add(noProductLabel);
                jPanel7.add(messagePanel);
            }

            jPanel7.revalidate();
            jPanel7.repaint();
            
            calculateColumnsPerRow();
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                requestFocusInWindow();
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading products: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private RoundedPanel createProductCard(int productId, String productName,
        String brandName, String batchNo, int qty, double sellingPrice, String barcode, double lastPrice) { 

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
        card.putClientProperty("qty", qty);
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

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10,0));
        bottomPanel.setOpaque(false);

        JPanel stockBadge = new JPanel();
        stockBadge.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        stockBadge.setOpaque(true);
        stockBadge.setBackground(new Color(230, 245, 230));

        JLabel lblStock = new JLabel("Stock: " + qty);
        lblStock.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        lblStock.setForeground(new Color(34, 139, 34));
        lblStock.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

        stockBadge.add(lblStock);
        bottomPanel.add(stockBadge);

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
                card.setBackground(originalBg);
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(BORDER_COLOR, 1, 20),
                        BorderFactory.createEmptyBorder(16, 18, 16, 18)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                addToCart(productId, productName, brandName, batchNo, qty, sellingPrice, barcode, lastPrice);
            }
        });

        return card;
    }
    
    private void addSelectedProductToCart() {
        if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
            RoundedPanel selectedCard = productCards.get(currentCardIndex);
            
            int productId = (int) selectedCard.getClientProperty("productId");
            String productName = (String) selectedCard.getClientProperty("productName");
            String brandName = (String) selectedCard.getClientProperty("brandName");
            String batchNo = (String) selectedCard.getClientProperty("batchNo");
            int qty = (int) selectedCard.getClientProperty("qty");
            double sellingPrice = (double) selectedCard.getClientProperty("sellingPrice");
            String barcode = (String) selectedCard.getClientProperty("barcode");
            double lastPrice = (double) selectedCard.getClientProperty("lastPrice");
            
            addToCart(productId, productName, brandName, batchNo, qty, sellingPrice, barcode, lastPrice);
            
            clearCardSelection();
        }
    }

    private void addToCart(int productId, String productName, String brandName,
            String batchNo, int qty, double sellingPrice, String barcode, double lastPrice) {
        
        posCartPanel.addToCart(productId, productName, brandName, batchNo, qty, sellingPrice, barcode, lastPrice);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
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
                .addGap(18, 18, 18)
                .addGroup(selectProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 714, Short.MAX_VALUE)
                    .addGroup(selectProductPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(reloadBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(productSearchBar))
                .addGap(18, 18, 18))
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(selectProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(cartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(selectProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
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
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void reloadBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadBtnActionPerformed
        loadProduct();
        productSearchBar.setText("Search By Product Name Or Barcode");
        productSearchBar.setForeground(java.awt.Color.GRAY);
    }//GEN-LAST:event_reloadBtnActionPerformed

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
    private javax.swing.JLabel brandName;
    private lk.com.pos.privateclasses.RoundedPanel cartPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
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
