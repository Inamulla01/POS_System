package lk.com.pos.panel;

import lk.com.pos.privateclasses.RoundedPanel;
import lk.com.pos.connection.MySQL;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
    private static final Color SELECTED_COLOR = new Color(200, 245, 245);

    private PosCartPanel posCartPanel;
    private List<RoundedPanel> productCards = new ArrayList<>();
    private int currentCardIndex = -1;
    private int columnsPerRow = 2;
    
    // Position indicator and keyboard hints
    private JPanel positionIndicator;
    private JLabel positionLabel;
    private Timer positionTimer;
    private JPanel keyboardHintsPanel;
    private boolean hintsVisible = false;

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
        setupGlobalShortcuts();
        createPositionIndicator();
        createKeyboardHintsPanel();

        // Remove borders and styling
        selectProductPanel.setBorder(BorderFactory.createEmptyBorder());
        selectProductPanel.setBorder(null);
        selectProductPanel.putClientProperty(FlatClientProperties.STYLE, "");

        // Apply rounding to cartPanel
        cartPanel.putClientProperty(FlatClientProperties.STYLE, "arc:15;");
        cartPanel.setBorder(BorderFactory.createEmptyBorder());

        // Initialize and add PosCartPanel
        posCartPanel = new PosCartPanel();
        cartPanel.setLayout(new java.awt.BorderLayout());
        cartPanel.add(posCartPanel, java.awt.BorderLayout.CENTER);

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

        // Simple scrollbar styling with FlatLaf
        jScrollPane2.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");
                
        // Set tooltips
        productSearchBar.setToolTipText("Search products (F, Ctrl+F, or /) - Press ? for help");
        reloadBtn.setToolTipText("Refresh products (F5 or Ctrl+R)");
        
        // Request focus after initialization
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
        });
    }

    // Global keyboard shortcuts
    private void setupGlobalShortcuts() {
        setFocusable(true);
        
        // Search shortcuts (F, Ctrl+F, /)
        registerKeyAction("F", KeyEvent.VK_F, 0, () -> focusSearch());
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, () -> focusSearch());
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, () -> focusSearch());
        
        // Refresh shortcuts (F5, Ctrl+R)
        registerKeyAction("F5", KeyEvent.VK_F5, 0, () -> refreshProducts());
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, () -> refreshProducts());
        
        // Clear search (Ctrl+L, Escape)
        registerKeyAction("CTRL_L", KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK, () -> clearSearch());
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, () -> handleEscape());
        
        // Cart operations
        registerKeyAction("CTRL_ENTER", KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK, () -> quickCheckout());
        registerKeyAction("DELETE", KeyEvent.VK_DELETE, 0, () -> clearCart());
        registerKeyAction("CTRL_D", KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK, () -> clearCart());
        
        // Help
        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, () -> showKeyboardHints());
        
        // Search bar specific shortcuts
        productSearchBar.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        productSearchBar.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearSearch();
            }
        });
        
        productSearchBar.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "startNavigation");
        productSearchBar.getActionMap().put("startNavigation", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!productCards.isEmpty()) {
                    PosPanelDone.this.requestFocusInWindow();
                    if (currentCardIndex == -1) {
                        currentCardIndex = 0;
                        selectCardByIndex(currentCardIndex);
                        ensureCardVisible(currentCardIndex);
                        updatePositionIndicator();
                    }
                }
            }
        });
    }
    
    private void registerKeyAction(String actionName, int keyCode, int modifiers, Runnable action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionName);
        this.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (productSearchBar.hasFocus() && 
                    keyCode != KeyEvent.VK_ESCAPE && 
                    keyCode != KeyEvent.VK_ENTER &&
                    modifiers == 0 &&
                    keyCode != KeyEvent.VK_SLASH) {
                    return;
                }
                action.run();
            }
        });
    }

    // Position indicator
    private void createPositionIndicator() {
        positionIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        positionIndicator.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 8));
        positionIndicator.setBackground(new Color(31, 41, 55, 230));
        positionIndicator.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_COLOR, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        positionIndicator.setVisible(false);
        positionIndicator.setOpaque(true);
        
        positionLabel = new JLabel();
        positionLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14));
        positionLabel.setForeground(Color.WHITE);
        
        positionIndicator.add(positionLabel);
        
        // Use OverlayLayout for proper layering
        setLayout(new javax.swing.OverlayLayout(this));
        add(positionIndicator);
    }
    
    private void createKeyboardHintsPanel() {
        keyboardHintsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        keyboardHintsPanel.setLayout(new javax.swing.BoxLayout(keyboardHintsPanel, javax.swing.BoxLayout.Y_AXIS));
        keyboardHintsPanel.setBackground(new Color(31, 41, 55, 240));
        keyboardHintsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);
        keyboardHintsPanel.setOpaque(true);
        
        JLabel title = new JLabel("POS KEYBOARD SHORTCUTS");
        title.setFont(new java.awt.Font("Nunito ExtraBold", 1, 13));
        title.setForeground(TEAL_COLOR);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);
        
        keyboardHintsPanel.add(javax.swing.Box.createVerticalStrut(10));
        
        // POS-specific shortcuts
        addHintRow("← → ↑ ↓", "Navigate products", "#FFFFFF");
        addHintRow("Enter", "Add to Cart", "#1CB5BB");
        addHintRow("F / Ctrl+F / /", "Quick Search", "#A78BFA");
        addHintRow("F5 / Ctrl+R", "Refresh Products", "#FB923C");
        addHintRow("Ctrl+L / Esc", "Clear Search", "#9CA3AF");
        addHintRow("Ctrl+Enter", "Quick Checkout", "#34D399");
        addHintRow("Delete / Ctrl+D", "Clear Cart", "#F87171");
        addHintRow("?", "Toggle Help", "#1CB5BB");
        
        keyboardHintsPanel.add(javax.swing.Box.createVerticalStrut(10));
        
        JLabel closeHint = new JLabel("Press ? to hide");
        closeHint.setFont(new java.awt.Font("Nunito SemiBold", 2, 10));
        closeHint.setForeground(Color.decode("#9CA3AF"));
        closeHint.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        keyboardHintsPanel.add(closeHint);
        
        add(keyboardHintsPanel);
    }
    
    private void addHintRow(String key, String description, String keyColor) {
        JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 2));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(300, 25));
        
        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(new java.awt.Font("Consolas", 1, 11));
        keyLabel.setForeground(Color.decode(keyColor));
        keyLabel.setPreferredSize(new Dimension(90, 20));
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 11));
        descLabel.setForeground(Color.decode("#D1D5DB"));
        
        row.add(keyLabel);
        row.add(descLabel);
        keyboardHintsPanel.add(row);
    }
    
    private void showKeyboardHints() {
        if (!hintsVisible) {
            keyboardHintsPanel.setVisible(true);
            hintsVisible = true;
            revalidate();
            repaint();
            
            Timer hideTimer = new Timer(5000, e -> {
                keyboardHintsPanel.setVisible(false);
                hintsVisible = false;
                revalidate();
                repaint();
            });
            hideTimer.setRepeats(false);
            hideTimer.start();
        } else {
            keyboardHintsPanel.setVisible(false);
            hintsVisible = false;
            revalidate();
            repaint();
        }
    }
    
    private void showPositionIndicator(String text) {
        SwingUtilities.invokeLater(() -> {
            positionLabel.setText(text);
            positionIndicator.setVisible(true);
            
            // Position the indicator at the top center
            Dimension size = positionIndicator.getPreferredSize();
            int x = (getWidth() - size.width) / 2;
            int y = 50; // Position from top
            positionIndicator.setBounds(x, y, size.width, size.height);
            
            revalidate();
            repaint();
            
            if (positionTimer != null && positionTimer.isRunning()) {
                positionTimer.stop();
            }
            
            positionTimer = new Timer(2000, e -> {
                positionIndicator.setVisible(false);
                revalidate();
                repaint();
            });
            positionTimer.setRepeats(false);
            positionTimer.start();
        });
    }
    
    private void updatePositionIndicator() {
        if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
            int row = (currentCardIndex / columnsPerRow) + 1;
            int col = (currentCardIndex % columnsPerRow) + 1;
            int totalRows = (int) Math.ceil((double) productCards.size() / columnsPerRow);
            
            RoundedPanel currentCard = productCards.get(currentCardIndex);
            String productName = (String) currentCard.getClientProperty("productName");
            Double sellingPrice = (Double) currentCard.getClientProperty("sellingPrice");
            
            String text = String.format("Product %d/%d (Row %d/%d, Col %d) | %s - Rs.%.2f | Enter: Add to Cart", 
                currentCardIndex + 1, 
                productCards.size(),
                row,
                totalRows,
                col,
                productName,
                sellingPrice != null ? sellingPrice : 0.0
            );
            
            showPositionIndicator(text);
        }
    }

    // POS-specific action methods
    private void focusSearch() {
        productSearchBar.requestFocus();
        productSearchBar.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter products (Press ↓ to navigate results)");
    }
    
    private void refreshProducts() {
        loadProduct();
        showPositionIndicator("Products refreshed");
        this.requestFocusInWindow();
    }
    
    private void clearSearch() {
        productSearchBar.setText("");
        // Reset placeholder
        productSearchBar.setForeground(Color.GRAY);
        productSearchBar.setText("Search products by name or barcode...");
        loadProduct("", "all");
        showPositionIndicator("Search cleared");
        requestFocusInWindow();
    }
    
    private void handleEscape() {
        if (currentCardIndex >= 0) {
            clearCardSelection();
            showPositionIndicator("Product deselected");
        } else if (!productSearchBar.getText().isEmpty() && 
                   !productSearchBar.getText().equals("Search products by name or barcode...")) {
            clearSearch();
        }
        this.requestFocusInWindow();
    }
    
    private void quickCheckout() {
        if (posCartPanel != null && hasItemsInCart()) {
            showPositionIndicator("Quick checkout triggered - Processing...");
            // Add your checkout logic here
        } else {
            showPositionIndicator("Cart is empty - Add products first");
        }
    }
    
    private void clearCart() {
        if (posCartPanel != null) {
            // Try to clear cart using reflection or safe method
            try {
                // Try to call clearCart method if it exists
                java.lang.reflect.Method method = posCartPanel.getClass().getMethod("clearCart");
                method.invoke(posCartPanel);
                showPositionIndicator("Cart cleared");
            } catch (Exception e) {
                // If clearCart doesn't exist, show message
                showPositionIndicator("Clear cart functionality not available");
            }
        }
    }
    
    private boolean hasItemsInCart() {
        if (posCartPanel != null) {
            try {
                // Try to call hasItems method if it exists
                java.lang.reflect.Method method = posCartPanel.getClass().getMethod("hasItems");
                return (Boolean) method.invoke(posCartPanel);
            } catch (Exception e) {
                // Default to true if method doesn't exist
                return true;
            }
        }
        return false;
    }

    private void setupSearchBarPlaceholder() {
        productSearchBar.setForeground(Color.GRAY);
        productSearchBar.setText("Search products by name or barcode...");

        productSearchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (productSearchBar.getForeground().equals(Color.GRAY)) {
                    productSearchBar.setText("");
                    productSearchBar.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (productSearchBar.getText().isEmpty()) {
                    productSearchBar.setForeground(Color.GRAY);
                    productSearchBar.setText("Search products by name or barcode...");
                }
            }
        });
    }

    private void setupSearchFunctionality() {
        // Add document listener to search bar for real-time search
        productSearchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleSearchInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleSearchInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleSearchInput();
            }

            private void handleSearchInput() {
                // Only search if it's not placeholder text
                if (!productSearchBar.getForeground().equals(Color.GRAY)) {
                    String searchText = productSearchBar.getText().trim();
                    if (searchText.isEmpty()) {
                        loadProduct("", "all");
                    } else {
                        loadProduct(searchText, "all");
                    }
                }
            }
        });
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

    private void performSearch() {
        if (!productSearchBar.getForeground().equals(Color.GRAY)) {
            String searchText = productSearchBar.getText().trim();
            loadProduct(searchText, "all");
        }
    }

    private void handleKeyPress(KeyEvent e) {
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
                        addSelectedProductToCart();
                        e.consume();
                    }
                    break;
            }
        }
    }

    private void navigateDown() {
        if (productCards.isEmpty() || columnsPerRow == 0) {
            return;
        }

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
        if (productCards.isEmpty() || columnsPerRow == 0) {
            return;
        }

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
        if (productCards.isEmpty() || columnsPerRow == 0) {
            return;
        }

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
        if (productCards.isEmpty() || columnsPerRow == 0) {
            return;
        }

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
        if (currentCardIndex >= 0 && currentCardIndex < productCards.size()) {
            RoundedPanel currentCard = productCards.get(currentCardIndex);
            setCardSelection(currentCard, true);
            updatePositionIndicator();
        }

        requestFocusInWindow();
    }

    private void setCardSelection(RoundedPanel card, boolean selected) {
        if (selected) {
            card.setBackground(SELECTED_COLOR);
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

    private void loadProduct() {
        loadProduct("", "all");
    }

    private void loadProduct(String productSearch, String status) {
        try {
            jPanel7.removeAll();
            productCards.clear();
            currentCardIndex = -1;

            // Database query - exclude expiring and inactive products by default
            StringBuilder query = new StringBuilder(
                "SELECT product.product_id, product.product_name, suppliers.suppliers_name, "
                + "brand.brand_name, category.category_name, "
                + "stock.qty, stock.expriy_date, stock.batch_no, product.barcode, "
                + "stock.purchase_price, stock.last_price, stock.selling_price, "
                + "product.p_status_id "
                + "FROM product "
                + "JOIN stock ON stock.product_id = product.product_id "
                + "JOIN category ON category.category_id = product.category_id "
                + "JOIN brand ON brand.brand_id = product.brand_id "
                + "JOIN suppliers ON suppliers.suppliers_id = stock.suppliers_id "
                + "WHERE stock.qty > 0 "
                // Exclude inactive products and expiring products by default
                + "AND product.p_status_id = 1 "
                + "AND (stock.expriy_date IS NULL OR stock.expriy_date > DATE_ADD(CURDATE(), INTERVAL 3 MONTH)) "
            );

            // Search filter with SQL injection protection
            if (productSearch != null && !productSearch.trim().isEmpty()
                    && !productSearch.equals("Search products by name or barcode...")) {
                String escapedSearch = productSearch.replace("'", "''")
                                                   .replace("\\", "\\\\")
                                                   .replace("%", "\\%")
                                                   .replace("_", "\\_");
                query.append("AND (product.product_name LIKE '%")
                     .append(escapedSearch)
                     .append("%' OR product.barcode LIKE '%")
                     .append(escapedSearch)
                     .append("%') ");
            }

            query.append("ORDER BY product.product_name ASC");

            ResultSet rs = MySQL.executeSearch(query.toString());

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                String brandName = rs.getString("brand_name");
                String batchNo = rs.getString("batch_no");
                int qty = rs.getInt("qty");
                double sellingPrice = rs.getDouble("selling_price");
                String barcode = rs.getString("barcode");
                double lastPrice = rs.getDouble("last_price");
                int pStatusId = rs.getInt("p_status_id");

                RoundedPanel productCard = createProductCard(
                        productId, productName, brandName, batchNo, qty, sellingPrice, barcode, lastPrice, pStatusId
                );

                jPanel7.add(productCard);
                productCards.add(productCard);
            }

            if (jPanel7.getComponentCount() == 0) {
                JPanel messagePanel = new JPanel(new java.awt.GridBagLayout());
                messagePanel.setBackground(Color.WHITE);
                messagePanel.setPreferredSize(new Dimension(jScrollPane2.getViewport().getWidth(), 400));

                String message = getEmptyStateMessage(productSearch, status);

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
                if (!productCards.isEmpty()) {
                    showPositionIndicator("Loaded " + productCards.size() + " products - Use arrow keys to navigate");
                } else {
                    showPositionIndicator(getEmptyStateMessage(productSearch, status));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading products: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getEmptyStateMessage(String productSearch, String status) {
        if (productSearch != null && !productSearch.isEmpty() 
            && !productSearch.equals("Search products by name or barcode...")) {
            return "No products found for: '" + productSearch + "'";
        }
        return "No products available";
    }

    private RoundedPanel createProductCard(int productId, String productName,
            String brandName, String batchNo, int qty, double sellingPrice, String barcode, double lastPrice, int pStatusId) {

        int viewportWidth = jScrollPane2.getViewport().getWidth();

        if (viewportWidth < 100) {
            viewportWidth = jScrollPane2.getWidth();
            if (viewportWidth < 100) {
                viewportWidth = selectProductPanel.getWidth() - 40;
            }
        }

        int cardWidth = calculateCardWidth(viewportWidth);

        RoundedPanel card = new RoundedPanel(20);
        
        // Set background based on product status
        if (pStatusId == 2) {
            card.setBackground(Color.decode("#F8F9FA")); // Inactive color
        } else {
            card.setBackground(CARD_BG);
        }
        
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
        card.putClientProperty("pStatusId", pStatusId);

        JPanel topPanel = new JPanel(new java.awt.BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblProductName = new JLabel(productName);
        lblProductName.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        if (pStatusId == 2) {
            lblProductName.setForeground(Color.decode("#6B7280")); // Inactive text color
        } else {
            lblProductName.setForeground(new Color(40, 40, 40));
        }
        topPanel.add(lblProductName, java.awt.BorderLayout.WEST);

        card.add(topPanel, java.awt.BorderLayout.NORTH);

        JPanel middlePanel = new JPanel(new java.awt.BorderLayout(12, 0));
        middlePanel.setOpaque(false);

        JLabel lblBrand = new JLabel("Brand: " + brandName);
        lblBrand.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        if (pStatusId == 2) {
            lblBrand.setForeground(Color.decode("#9CA3AF")); // Inactive text color
        } else {
            lblBrand.setForeground(TEXT_GRAY);
        }
        middlePanel.add(lblBrand, java.awt.BorderLayout.WEST);

        JLabel lblPrice = new JLabel(String.format("Rs.%.2f", sellingPrice));
        lblPrice.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        if (pStatusId == 2) {
            lblPrice.setForeground(Color.decode("#9CA3AF")); // Inactive text color
        } else {
            lblPrice.setForeground(TEAL_COLOR);
        }
        lblPrice.setHorizontalAlignment(JLabel.RIGHT);
        middlePanel.add(lblPrice, java.awt.BorderLayout.EAST);

        card.add(middlePanel, java.awt.BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        bottomPanel.setOpaque(false);

        // Stock badge
        JPanel stockBadge = new JPanel();
        stockBadge.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        stockBadge.setOpaque(true);
        if (pStatusId == 2) {
            stockBadge.setBackground(Color.decode("#F3F4F6")); // Inactive badge color
        } else {
            stockBadge.setBackground(new Color(230, 245, 230));
        }

        JLabel lblStock = new JLabel("Stock: " + qty);
        lblStock.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        if (pStatusId == 2) {
            lblStock.setForeground(Color.decode("#6B7280")); // Inactive text color
        } else {
            lblStock.setForeground(new Color(34, 139, 34));
        }
        lblStock.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

        stockBadge.add(lblStock);
        bottomPanel.add(stockBadge);

        // Batch badge
        JPanel batchBadge = new JPanel();
        batchBadge.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        batchBadge.setOpaque(true);
        if (pStatusId == 2) {
            batchBadge.setBackground(Color.decode("#F3F4F6")); // Inactive badge color
        } else {
            batchBadge.setBackground(new Color(240, 240, 250));
        }

        JLabel lblBatch = new JLabel(batchNo);
        lblBatch.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        if (pStatusId == 2) {
            lblBatch.setForeground(Color.decode("#6B7280")); // Inactive text color
        } else {
            lblBatch.setForeground(new Color(70, 70, 100));
        }
        lblBatch.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

        batchBadge.add(lblBatch);
        bottomPanel.add(batchBadge);

        // Inactive badge
        if (pStatusId == 2) {
            JPanel inactiveBadge = new JPanel();
            inactiveBadge.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            inactiveBadge.setOpaque(true);
            inactiveBadge.setBackground(Color.decode("#6B7280"));

            JLabel lblInactive = new JLabel("Inactive");
            lblInactive.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
            lblInactive.setForeground(Color.WHITE);
            lblInactive.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

            inactiveBadge.add(lblInactive);
            bottomPanel.add(inactiveBadge);
        }

        card.add(bottomPanel, java.awt.BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            private Color originalBg = card.getBackground();

            @Override
            public void mouseEntered(MouseEvent e) {
                if (pStatusId != 2) { // Only allow hover for active products
                    card.setBackground(CARD_HOVER);
                    card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    card.setBorder(BorderFactory.createCompoundBorder(
                            new RoundBorder(TEAL_COLOR, 2, 20),
                            BorderFactory.createEmptyBorder(16, 18, 16, 18)
                    ));
                }
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
                if (pStatusId != 2) { // Only allow adding active products to cart
                    addToCart(productId, productName, brandName, batchNo, qty, sellingPrice, barcode, lastPrice);
                    showPositionIndicator("✓ Added to cart: " + productName);
                } else {
                    showPositionIndicator("⚠️ Cannot add inactive product to cart");
                }
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
            Integer pStatusId = (Integer) selectedCard.getClientProperty("pStatusId");

            if (pStatusId != null && pStatusId == 2) {
                showPositionIndicator("⚠️ Cannot add inactive product to cart");
                return;
            }

            addToCart(productId, productName, brandName, batchNo, qty, sellingPrice, barcode, lastPrice);

            clearCardSelection();
            showPositionIndicator("✓ Added to cart: " + productName);
        }
    }

    private void addToCart(int productId, String productName, String brandName,
            String batchNo, int qty, double sellingPrice, String barcode, double lastPrice) {

        if (posCartPanel != null) {
            posCartPanel.addToCart(productId, productName, brandName, batchNo, qty, sellingPrice, barcode, lastPrice);
        }
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(reloadBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(productSearchBar, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
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
