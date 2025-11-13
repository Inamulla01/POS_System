package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import lk.com.pos.connection.MySQL;
import lk.com.pos.dialog.AddNewProduct;
import lk.com.pos.dialog.UpdateProduct;
import lk.com.pos.privateclasses.RoundedPanel;

/**
 * ProductPanel - Displays and manages product information with keyboard navigation
 * Features: Search, filters, keyboard navigation, activate/deactivate
 * Default: Shows ONLY active products (inactive only shown when explicitly filtered)
 * 
 * @author pasin
 * @version 2.0
 */
public class ProductPanel extends javax.swing.JPanel {

    private static final class Colors {
        static final Color TEAL_PRIMARY = new Color(28, 181, 187);
        static final Color TEAL_HOVER = new Color(60, 200, 206);
        static final Color BACKGROUND = Color.decode("#F8FAFC");
        static final Color CARD_WHITE = Color.WHITE;
        static final Color CARD_INACTIVE = Color.decode("#F8F9FA");
        static final Color TEXT_PRIMARY = Color.decode("#1E293B");
        static final Color TEXT_SECONDARY = Color.decode("#6B7280");
        static final Color TEXT_MUTED = Color.decode("#94A3B8");
        static final Color TEXT_INACTIVE = Color.decode("#9CA3AF");
    }
    
    private static final class Dimensions {
        static final Dimension CARD_SIZE = new Dimension(420, 280);
        static final Dimension CARD_MAX_SIZE = new Dimension(420, 280);
        static final Dimension CARD_MIN_SIZE = new Dimension(380, 280);
        static final int CARD_WIDTH_WITH_GAP = 445;
        static final int GRID_GAP = 25;
        static final int CARD_PADDING = 18;
    }
    
    private static final class Fonts {
        static final java.awt.Font POSITION = new java.awt.Font("Nunito ExtraBold", 1, 14);
        static final java.awt.Font HINT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 13);
        static final java.awt.Font HINT_KEY = new java.awt.Font("Consolas", 1, 11);
        static final java.awt.Font HINT_DESC = new java.awt.Font("Nunito SemiBold", 0, 11);
    }

    // Keyboard Navigation
    private RoundedPanel currentFocusedCard = null;
    private List<RoundedPanel> productCardsList = new ArrayList<>();
    private int currentCardIndex = -1;
    private int currentColumns = 3;
    
    // UI Components
    private JPanel positionIndicator;
    private JLabel positionLabel;
    private Timer positionTimer;
    private JPanel keyboardHintsPanel;
    private boolean hintsVisible = false;

    public ProductPanel() {
        initComponents();
        
        // Configure scroll pane
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        
        init();
        createPositionIndicator();
        createKeyboardHintsPanel();
        setupKeyboardShortcuts();
        loadProducts(); // Will load ACTIVE products by default
        setupEventListeners();
        radioButtonListener();
        
        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
            showKeyboardHints();
        });
    }

    private void init() {
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5; thumb: #1CB5BB; width: 8");

        productSearchBar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search By Product Name Or Barcode");
        productSearchBar.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        
        
        
        // Set tooltips with keyboard shortcuts
        activeRadioBtn.setToolTipText("Filter active products (Alt+1)");
        inactiveRadioBtn.setToolTipText("Filter inactive products (Alt+2)");
        productSearchBar.setToolTipText("Search products (Ctrl+F or /) - Press ? for help");
        productReportBtn.setToolTipText("Generate Product Report (Ctrl+R or Ctrl+P)");
        addProductDialog.setToolTipText("Add New Product (Ctrl+N or Alt+A)"); // ADDED
    }

    private void createPositionIndicator() {
        positionIndicator = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        positionIndicator.setBackground(new Color(31, 41, 55, 230));
        positionIndicator.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        positionIndicator.setVisible(false);
        
        positionLabel = new JLabel();
        positionLabel.setFont(Fonts.POSITION);
        positionLabel.setForeground(Color.WHITE);
        
        positionIndicator.add(positionLabel);
        
        setLayout(new javax.swing.OverlayLayout(this) {
            @Override
            public void layoutContainer(java.awt.Container target) {
                super.layoutContainer(target);
                layoutOverlays();
            }
        });
        
        add(positionIndicator, Integer.valueOf(1000));
    }
    
    private void layoutOverlays() {
        if (positionIndicator != null && positionIndicator.isVisible()) {
            Dimension size = positionIndicator.getPreferredSize();
            int x = (getWidth() - size.width) / 2;
            int y = 80;
            positionIndicator.setBounds(x, y, size.width, size.height);
        }
        
        if (keyboardHintsPanel != null && keyboardHintsPanel.isVisible()) {
            Dimension size = keyboardHintsPanel.getPreferredSize();
            int x = getWidth() - size.width - 20;
            int y = getHeight() - size.height - 20;
            keyboardHintsPanel.setBounds(x, y, size.width, size.height);
        }
    }
    
    private void showPositionIndicator(String text) {
        if (text == null || text.isEmpty()) return;
        
        positionLabel.setText(text);
        positionIndicator.setVisible(true);
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
    }

    private void createKeyboardHintsPanel() {
        keyboardHintsPanel = new JPanel();
        keyboardHintsPanel.setLayout(new BoxLayout(keyboardHintsPanel, BoxLayout.Y_AXIS));
        keyboardHintsPanel.setBackground(new Color(31, 41, 55, 240));
        keyboardHintsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);
        
        JLabel title = new JLabel("⌨️ KEYBOARD SHORTCUTS");
        title.setFont(Fonts.HINT_TITLE);
        title.setForeground(Colors.TEAL_PRIMARY);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);
        keyboardHintsPanel.add(Box.createVerticalStrut(10));
        
        // UPDATED: Added hints for Add Product shortcuts
        addHintRow("← → ↑ ↓", "Navigate cards", "#FFFFFF");
        addHintRow("E", "Edit Product", "#1CB5BB");
        addHintRow("D", "Deactivate Product", "#F87171");
        addHintRow("R", "Activate Product", "#10B981");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("Ctrl+N/Alt+A", "Add Product", "#60D5F2");
        addHintRow("Ctrl+R/P", "Product Report", "#34D399");
        addHintRow("F5", "Refresh List", "#FB923C");
        addHintRow("Alt+1", "Active Products", "#FB923C");
        addHintRow("Alt+2", "Inactive Products", "#FB923C");
        addHintRow("Esc", "Clear/Back", "#9CA3AF");
        addHintRow("?", "Toggle Help", "#1CB5BB");
        
        keyboardHintsPanel.add(Box.createVerticalStrut(10));
        
        JLabel closeHint = new JLabel("Press ? to hide");
        closeHint.setFont(new java.awt.Font("Nunito SemiBold", 2, 10));
        closeHint.setForeground(Color.decode("#9CA3AF"));
        closeHint.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        keyboardHintsPanel.add(closeHint);
        
        add(keyboardHintsPanel, Integer.valueOf(1001));
    }
    
    private void addHintRow(String key, String description, String keyColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(300, 25));
        
        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(Fonts.HINT_KEY);
        keyLabel.setForeground(Color.decode(keyColor));
        keyLabel.setPreferredSize(new Dimension(90, 20));
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(Fonts.HINT_DESC);
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

    private void setupKeyboardShortcuts() {
        this.setFocusable(true);
        
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        int arrowCondition = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
        
        // Arrow navigation
        registerKeyAction("LEFT", KeyEvent.VK_LEFT, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_LEFT));
        registerKeyAction("RIGHT", KeyEvent.VK_RIGHT, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_RIGHT));
        registerKeyAction("UP", KeyEvent.VK_UP, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_UP));
        registerKeyAction("DOWN", KeyEvent.VK_DOWN, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_DOWN));
        
        // Actions
        registerKeyAction("E", KeyEvent.VK_E, 0, condition, this::editSelectedCard);
        registerKeyAction("D", KeyEvent.VK_D, 0, condition, this::deactivateSelectedCard);
        registerKeyAction("R", KeyEvent.VK_R, 0, condition, this::activateSelectedCard);
        
        // Enter to start navigation
        registerKeyAction("ENTER", KeyEvent.VK_ENTER, 0, condition, this::handleEnterKey);
        
        // Search
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, this::focusSearch);
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, this::handleSlashKey);
        
        // Escape
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, this::handleEscape);
        
        // Report and Refresh
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::generateProductReport);
        registerKeyAction("CTRL_P", KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, condition, this::generateProductReport);
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, this::refreshProducts);
        
        // ADDED: Add Product shortcuts
        // Ctrl+N = New Product (semantic: N for New)
        // Alt+A = Add Product (semantic: A for Add)
        registerKeyAction("CTRL_N", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, condition, this::openAddProductDialog);
        registerKeyAction("ALT_A", KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, condition, this::openAddProductDialog);
        
        // Quick filters - Alt+1 = Active, Alt+2 = Inactive
        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(activeRadioBtn));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(inactiveRadioBtn));
        
        // Help
        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, condition, this::showKeyboardHints);
        
        setupSearchFieldShortcuts();
    }
    
    private void setupSearchFieldShortcuts() {
        productSearchBar.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        productSearchBar.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearSearchAndFilters();
            }
        });
        
        productSearchBar.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "startNavigation");
        productSearchBar.getActionMap().put("startNavigation", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startNavigationFromSearch();
            }
        });
    }
    
    private void registerKeyAction(String actionName, int keyCode, int modifiers, int condition, Runnable action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        this.getInputMap(condition).put(keyStroke, actionName);
        this.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (shouldIgnoreKeyAction(keyCode, modifiers)) {
                    return;
                }
                action.run();
            }
        });
    }
    
    private boolean shouldIgnoreKeyAction(int keyCode, int modifiers) {
        return productSearchBar.hasFocus() && 
               keyCode != KeyEvent.VK_ESCAPE && 
               keyCode != KeyEvent.VK_ENTER &&
               modifiers == 0 &&
               keyCode != KeyEvent.VK_SLASH;
    }
    
    private void handleEnterKey() {
        if (currentCardIndex == -1 && !productCardsList.isEmpty()) {
            navigateCards(KeyEvent.VK_RIGHT);
        }
    }
    
    private void handleSlashKey() {
        if (!productSearchBar.hasFocus()) {
            focusSearch();
        }
    }
    
    private void clearSearchAndFilters() {
        productSearchBar.setText("");
        buttonGroup1.clearSelection();
        SearchFilters();
        ProductPanel.this.requestFocusInWindow();
    }
    
    private void startNavigationFromSearch() {
        if (!productCardsList.isEmpty()) {
            ProductPanel.this.requestFocusInWindow();
            if (currentCardIndex == -1) {
                currentCardIndex = 0;
                selectCurrentCard();
                scrollToCardSmooth(currentCardIndex);
                updatePositionIndicator();
            }
        }
    }
    
    private void focusSearch() {
        productSearchBar.requestFocus();
        productSearchBar.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter products (Press ↓ to navigate results)");
    }
    
    private void handleEscape() {
        if (currentCardIndex >= 0) {
            deselectCurrentCard();
            showPositionIndicator("Card deselected");
        } else if (!productSearchBar.getText().isEmpty() || 
                   activeRadioBtn.isSelected() || 
                   inactiveRadioBtn.isSelected()) {
            productSearchBar.setText("");
            buttonGroup1.clearSelection();
            SearchFilters();
            showPositionIndicator("Filters cleared - Showing active products");
        }
        this.requestFocusInWindow();
    }
    
    private void refreshProducts() {
        SearchFilters();
        showPositionIndicator("✅ Products refreshed");
        this.requestFocusInWindow();
    }
    
    private void toggleRadioButton(javax.swing.JRadioButton radioBtn) {
        if (radioBtn.isSelected()) {
            buttonGroup1.clearSelection();
            showPositionIndicator("Filter removed - Showing active products");
        } else {
            radioBtn.setSelected(true);
            showPositionIndicator("Filter applied: " + radioBtn.getText());
        }
        SearchFilters();
        this.requestFocusInWindow();
    }
    
    /**
     * ADDED: Opens Add Product dialog
     */
    private void openAddProductDialog() {
        addProductDialog.doClick();
        showPositionIndicator("Opening Add Product dialog");
    }

    private void navigateCards(int direction) {
        if (productCardsList.isEmpty()) {
            showPositionIndicator("No products available");
            return;
        }
        
        if (currentCardIndex < 0) {
            currentCardIndex = 0;
            selectCurrentCard();
            scrollToCardSmooth(currentCardIndex);
            updatePositionIndicator();
            return;
        }
        
        int oldIndex = currentCardIndex;
        int newIndex = calculateNewIndex(direction, currentCardIndex, productCardsList.size());
        
        if (newIndex != oldIndex) {
            deselectCard(oldIndex);
            currentCardIndex = newIndex;
            selectCurrentCard();
            scrollToCardSmooth(currentCardIndex);
            updatePositionIndicator();
        } else {
            showBoundaryMessage(direction);
        }
    }
    
    private void showBoundaryMessage(int direction) {
        String message;
        switch (direction) {
            case KeyEvent.VK_LEFT: message = "◀️ Already at the beginning"; break;
            case KeyEvent.VK_RIGHT: message = "▶️ Already at the end"; break;
            case KeyEvent.VK_UP: message = "🔼 Already at the top"; break;
            case KeyEvent.VK_DOWN: message = "🔽 Already at the bottom"; break;
            default: return;
        }
        showPositionIndicator(message);
    }
    
    private int calculateNewIndex(int direction, int currentIndex, int totalCards) {
        int currentRow = currentIndex / currentColumns;
        int currentCol = currentIndex % currentColumns;
        int totalRows = (int) Math.ceil((double) totalCards / currentColumns);
        
        switch (direction) {
            case KeyEvent.VK_LEFT:
                if (currentCol > 0) return currentIndex - 1;
                else if (currentRow > 0) return Math.min((currentRow * currentColumns) - 1, totalCards - 1);
                return currentIndex;
                
            case KeyEvent.VK_RIGHT:
                if (currentIndex < totalCards - 1) return currentIndex + 1;
                return currentIndex;
                
            case KeyEvent.VK_UP:
                if (currentRow > 0) return Math.max(0, currentIndex - currentColumns);
                return currentIndex;
                
            case KeyEvent.VK_DOWN:
                int targetIndex = currentIndex + currentColumns;
                if (targetIndex < totalCards) return targetIndex;
                else {
                    int lastRowFirstIndex = (totalRows - 1) * currentColumns;
                    int potentialIndex = lastRowFirstIndex + currentCol;
                    if (potentialIndex < totalCards && potentialIndex > currentIndex) return potentialIndex;
                }
                return currentIndex;
                
            default:
                return currentIndex;
        }
    }
    
    private void updatePositionIndicator() {
        if (currentCardIndex < 0 || currentCardIndex >= productCardsList.size()) return;
        
        int row = (currentCardIndex / currentColumns) + 1;
        int col = (currentCardIndex % currentColumns) + 1;
        int totalRows = (int) Math.ceil((double) productCardsList.size() / currentColumns);
        
        RoundedPanel currentCard = productCardsList.get(currentCardIndex);
        Integer pStatusId = (Integer) currentCard.getClientProperty("pStatusId");
        
        String actionKey = "D: Deactivate";
        if (pStatusId != null && pStatusId == 2) {
            actionKey = "R: Activate";
        }
        
        String text = String.format("Card %d/%d (Row %d/%d, Col %d) | E: Edit | %s", 
            currentCardIndex + 1, productCardsList.size(), row, totalRows, col, actionKey);
        
        showPositionIndicator(text);
    }
    
    private void selectCurrentCard() {
        if (currentCardIndex < 0 || currentCardIndex >= productCardsList.size()) return;
        
        RoundedPanel card = productCardsList.get(currentCardIndex);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(Colors.TEAL_PRIMARY, 4, 15),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        card.setBackground(card.getBackground().brighter());
        currentFocusedCard = card;
    }
    
    private void deselectCard(int index) {
        if (index < 0 || index >= productCardsList.size()) return;
        
        RoundedPanel card = productCardsList.get(index);
        card.setBorder(BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, 18, 18, 18));
        
        Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
        if (pStatusId != null && pStatusId == 2) {
            card.setBackground(Colors.CARD_INACTIVE);
        } else {
            card.setBackground(Colors.CARD_WHITE);
        }
    }
    
    private void deselectCurrentCard() {
        if (currentFocusedCard != null) {
            deselectCard(currentCardIndex);
            currentFocusedCard = null;
        }
        currentCardIndex = -1;
    }
    
    private void scrollToCardSmooth(int index) {
        if (index < 0 || index >= productCardsList.size()) return;
        
        SwingUtilities.invokeLater(() -> {
            try {
                RoundedPanel card = productCardsList.get(index);
                Point cardLocation = card.getLocation();
                Dimension cardSize = card.getSize();
                Rectangle viewRect = jScrollPane1.getViewport().getViewRect();
                
                int cardTop = cardLocation.y;
                int cardBottom = cardLocation.y + cardSize.height;
                int viewTop = viewRect.y;
                int viewBottom = viewRect.y + viewRect.height;
                
                int targetY;
                if (cardTop < viewTop) {
                    targetY = Math.max(0, cardTop - 50);
                } else if (cardBottom > viewBottom) {
                    targetY = cardBottom - viewRect.height + 50;
                } else {
                    return; // Card is already visible
                }
                
                animateScroll(viewRect.x, viewRect.y, targetY);
            } catch (Exception e) {
                System.err.println("Error scrolling to card: " + e.getMessage());
            }
        });
    }
    
    private void animateScroll(int x, int startY, int endY) {
        final int steps = 10;
        final int delay = 15;
        Timer scrollTimer = new Timer(delay, null);
        final int[] step = {0};
        
        scrollTimer.addActionListener(e -> {
            step[0]++;
            if (step[0] <= steps) {
                double progress = (double) step[0] / steps;
                double easeProgress = 1 - Math.pow(1 - progress, 3);
                int newY = (int) (startY + (endY - startY) * easeProgress);
                jScrollPane1.getViewport().setViewPosition(new Point(x, newY));
            } else {
                scrollTimer.stop();
            }
        });
        scrollTimer.start();
    }

    private void editSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= productCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        RoundedPanel card = productCardsList.get(currentCardIndex);
        Integer productId = (Integer) card.getClientProperty("productId");
        
        if (productId != null) {
            editProduct(productId);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }
    
    private void deactivateSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= productCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        RoundedPanel card = productCardsList.get(currentCardIndex);
        Integer productId = (Integer) card.getClientProperty("productId");
        String productName = (String) card.getClientProperty("productName");
        Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
        
        if (productId != null && productName != null) {
            if (pStatusId != null && pStatusId == 2) {
                showPositionIndicator("Product is inactive - Use R to activate");
            } else {
                deactivateProduct(productId, productName);
            }
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }
    
    private void activateSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= productCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        RoundedPanel card = productCardsList.get(currentCardIndex);
        Integer productId = (Integer) card.getClientProperty("productId");
        String productName = (String) card.getClientProperty("productName");
        Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
        
        if (productId != null && productName != null) {
            if (pStatusId != null && pStatusId == 2) {
                activateProduct(productId, productName);
            } else {
                showPositionIndicator("Product is already active - Use D to deactivate");
            }
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }

    class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int arc;

        public RoundedBorder(Color color, int thickness, int arc) {
            this.color = color;
            this.thickness = thickness;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new java.awt.BasicStroke(thickness));
            
            int offset = thickness / 2;
            g2d.drawRoundRect(x + offset, y + offset, 
                             width - thickness, height - thickness, arc, arc);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int inset = thickness + 2;
            return new Insets(inset, inset, inset, inset);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int inset = thickness + 2;
            insets.left = insets.right = insets.top = insets.bottom = inset;
            return insets;
        }
    }

    // ============================================================================
    // EVENT LISTENERS
    // ============================================================================
    
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
        
        // Resize listener
        jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayoutIfNeeded();
            }
        });
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> ProductPanel.this.requestFocusInWindow());
            }
        });
    }
    
    private void updateLayoutIfNeeded() {
        int scrollPaneWidth = jScrollPane1.getWidth();
        int newColumns = calculateColumns(scrollPaneWidth);
        
        if (newColumns != currentColumns) {
            currentColumns = newColumns;
            SearchFilters();
        }
    }

    /**
     * Search with filters
     * Default: Show ONLY active products (even if no radio button selected)
     */
    private void SearchFilters() {
        String searchText = productSearchBar.getText().trim();

        String status = "Active"; // DEFAULT to Active

        if (inactiveRadioBtn.isSelected()) {
            status = "Inactive";
        } else if (activeRadioBtn.isSelected()) {
            status = "Active";
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
        loadProducts("", "Active");
    }

    /**
     * Load products with proper Active/Inactive filtering
     */
    private void loadProducts(String searchText, String status) {
        try {
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

            if ("Inactive".equals(status)) {
                query += "AND product.p_status_id = 2 ";
            } else {
                query += "AND product.p_status_id = 1 ";
            }

            if (searchText != null && !searchText.trim().isEmpty()
                    && !searchText.equals("Search By Product Name Or Barcode")) {
                String escapedSearch = escapeSQL(searchText);
                query += "AND (product.product_name LIKE '%" + escapedSearch + "%' "
                        + "OR product.barcode LIKE '%" + escapedSearch + "%') ";
            }

            query += "ORDER BY product.product_name";

            ResultSet rs = MySQL.executeSearch(query);

            clearProductCards();

            List<RoundedPanel> productCards = new ArrayList<>();
            int productCount = 0;

            while (rs.next()) {
                productCount++;

                RoundedPanel productCard = createProductCard(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("brand_name"),
                        rs.getString("category_name"),
                        rs.getString("barcode"),
                        rs.getInt("p_status_id")
                );

                productCards.add(productCard);
                productCardsList.add(productCard);
            }

            if (productCount == 0) {
                showEmptyState(status, searchText);
            } else {
                displayProductCards(productCards);
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
    
    private void clearProductCards() {
        for (RoundedPanel card : productCardsList) {
            removeAllListeners(card);
        }
        
        productCardsList.clear();
        jPanel2.removeAll();
        currentCardIndex = -1;
        currentFocusedCard = null;
    }
    
    private void removeAllListeners(Component component) {
        for (java.awt.event.MouseListener ml : component.getMouseListeners()) {
            component.removeMouseListener(ml);
        }
        
        for (Component child : getAllComponents(component)) {
            if (child instanceof JButton) {
                JButton btn = (JButton) child;
                for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
            }
        }
    }
    
    private List<Component> getAllComponents(Component container) {
        List<Component> list = new ArrayList<>();
        if (container instanceof java.awt.Container) {
            for (Component comp : ((java.awt.Container) container).getComponents()) {
                list.add(comp);
                if (comp instanceof java.awt.Container) {
                    list.addAll(getAllComponents(comp));
                }
            }
        }
        return list;
    }
    
    private void showEmptyState(String status, String searchText) {
        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.setBackground(Color.decode("#F8FAFC"));

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
    }
    
    private void displayProductCards(List<RoundedPanel> productCards) {
        currentColumns = calculateColumns(jPanel2.getWidth());
        final javax.swing.JPanel gridPanel = new javax.swing.JPanel();
        gridPanel.setBackground(Color.decode("#F8FAFC"));
        gridPanel.setLayout(new java.awt.GridLayout(0, currentColumns, 25, 25));

        for (RoundedPanel card : productCards) {
            gridPanel.add(card);
        }

        jPanel2.setLayout(new java.awt.BorderLayout());

        javax.swing.JPanel mainContainer = new javax.swing.JPanel();
        mainContainer.setLayout(new javax.swing.BoxLayout(mainContainer, javax.swing.BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.decode("#F8FAFC"));

        javax.swing.JPanel paddingPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        paddingPanel.setBackground(Color.decode("#F8FAFC"));
        paddingPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25));
        paddingPanel.add(gridPanel, java.awt.BorderLayout.NORTH);

        mainContainer.add(paddingPanel);
        jPanel2.add(mainContainer, java.awt.BorderLayout.NORTH);

        setupGridResizeListener(gridPanel, currentColumns);
    }
    
    private void setupGridResizeListener(final javax.swing.JPanel gridPanel, int initialColumns) {
        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
            private int lastColumns = initialColumns;

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int panelWidth = jPanel2.getWidth();
                int newColumns = calculateColumns(panelWidth);

                if (newColumns != lastColumns) {
                    lastColumns = newColumns;
                    currentColumns = newColumns;
                    gridPanel.setLayout(new java.awt.GridLayout(0, newColumns, 25, 25));
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }
        });
    }
    
    private String escapeSQL(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("'", "''")
                   .replace("%", "\\%")
                   .replace("_", "\\_");
    }

    private int calculateColumns(int panelWidth) {
        int availableWidth = panelWidth - 50;

        if (availableWidth >= Dimensions.CARD_WIDTH_WITH_GAP * 3) {
            return 3;
        } else if (availableWidth >= Dimensions.CARD_WIDTH_WITH_GAP * 2) {
            return 2;
        } else {
            return 1;
        }
    }

    private lk.com.pos.privateclasses.RoundedPanel createProductCard(
            int productId, String productName, String brandName,
            String categoryName, String barcode, int pStatusId) {

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new java.awt.BorderLayout());
        card.setPreferredSize(Dimensions.CARD_SIZE);
        card.setMaximumSize(Dimensions.CARD_MAX_SIZE);
        card.setMinimumSize(Dimensions.CARD_MIN_SIZE);
        
        if (pStatusId == 2) {
            card.setBackground(Colors.CARD_INACTIVE);
        } else {
            card.setBackground(Colors.CARD_WHITE);
        }
        
        card.setBorderThickness(0);
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, 18, 18, 18));
        
        // Store data as client properties
        card.putClientProperty("productId", productId);
        card.putClientProperty("productName", productName);
        card.putClientProperty("pStatusId", pStatusId);
        
        addCardMouseListeners(card, pStatusId);

        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS));
        contentPanel.setBackground(card.getBackground());
        contentPanel.setOpaque(false);

        // Header
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));

        javax.swing.JLabel nameLabel = new javax.swing.JLabel(productName);
        nameLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
        if (pStatusId == 2) {
            nameLabel.setForeground(Color.decode("#6B7280"));
        } else {
            nameLabel.setForeground(Color.decode("#1E293B"));
        }
        nameLabel.setToolTipText(productName);
        headerPanel.add(nameLabel, java.awt.BorderLayout.CENTER);

        // Action buttons
        javax.swing.JPanel actionPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        javax.swing.JButton editButton = createEditButton(productId, pStatusId, productName);
        javax.swing.JButton deleteButton = createDeleteButton(productId, pStatusId, productName);

        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        headerPanel.add(actionPanel, java.awt.BorderLayout.EAST);

        contentPanel.add(headerPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(8));

        // Supplier and Status Badge
        javax.swing.JPanel supplierStatusPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        supplierStatusPanel.setOpaque(false);
        supplierStatusPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));

        String supplierName = getSupplierForProduct(productId);
        
        javax.swing.JLabel supplierLabel = new javax.swing.JLabel("Supplier: " + supplierName);
        supplierLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        if (pStatusId == 2) {
            supplierLabel.setForeground(Color.decode("#9CA3AF"));
        } else {
            supplierLabel.setForeground(Color.decode("#6366F1"));
        }
        supplierLabel.setToolTipText("Supplier: " + supplierName);
        supplierStatusPanel.add(supplierLabel, java.awt.BorderLayout.WEST);

        javax.swing.JPanel badgePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);

        if (pStatusId == 2) {
            javax.swing.JLabel inactiveBadge = new javax.swing.JLabel("Inactive");
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
            javax.swing.JLabel activeBadge = new javax.swing.JLabel("Active");
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

        // Details Header
        javax.swing.JPanel detailsHeaderPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 20));

        javax.swing.JLabel detailsHeader = new javax.swing.JLabel("PRODUCT DETAILS");
        detailsHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
        detailsHeader.setForeground(Color.decode("#94A3B8"));
        detailsHeaderPanel.add(detailsHeader);

        contentPanel.add(detailsHeaderPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        // Details Grid
        javax.swing.JPanel detailsGrid = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 120));

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
    
    private void addCardMouseListeners(RoundedPanel card, int pStatusId) {
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    Color hoverColor = pStatusId == 2 ? Colors.TEXT_INACTIVE : Colors.TEAL_HOVER;
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(hoverColor, 2, 15),
                        BorderFactory.createEmptyBorder(16, 16, 16, 16)
                    ));
                }
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    card.setBorder(BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, 18, 18, 18));
                }
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleCardClick(card);
            }
        });
    }
    
    private void handleCardClick(RoundedPanel card) {
        if (currentFocusedCard != null && currentFocusedCard != card) {
            deselectCurrentCard();
        }
        
        currentCardIndex = productCardsList.indexOf(card);
        selectCurrentCard();
        updatePositionIndicator();
        ProductPanel.this.requestFocusInWindow();
    }
    
    private JButton createEditButton(int productId, int pStatusId, String productName) {
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
            editButton.setBackground(Color.decode("#D1FAE5"));
            editButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#10B981"), 1));
            editButton.setToolTipText("Activate Product (R)");
            try {
                FlatSVGIcon activateIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 16, 16);
                editButton.setIcon(activateIcon);
            } catch (Exception e) {
                editButton.setText("↻");
                editButton.setForeground(Color.decode("#059669"));
            }
            editButton.addActionListener(e -> {
                activateProduct(productId, productName);
                ProductPanel.this.requestFocusInWindow();
            });
        } else {
            editButton.setToolTipText("Edit Product (E)");
            editButton.addActionListener(e -> {
                editProduct(productId);
                ProductPanel.this.requestFocusInWindow();
            });
        }
        
        return editButton;
    }
    
    private JButton createDeleteButton(int productId, int pStatusId, String productName) {
        javax.swing.JButton deleteButton = new javax.swing.JButton();
        deleteButton.setPreferredSize(new java.awt.Dimension(30, 30));
        deleteButton.setMinimumSize(new java.awt.Dimension(30, 30));
        deleteButton.setMaximumSize(new java.awt.Dimension(30, 30));
        
        if (pStatusId == 2) {
            deleteButton.setVisible(false);
        } else {
            deleteButton.setBackground(Color.decode("#FEF2F2"));
            deleteButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
            deleteButton.setToolTipText("Deactivate Product (D)");
            deleteButton.addActionListener(e -> {
                deactivateProduct(productId, productName);
                ProductPanel.this.requestFocusInWindow();
            });
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
        
        return deleteButton;
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
        try {
            String query = "SELECT stock_id FROM stock WHERE product_id = " + productId + " LIMIT 1";
            ResultSet rs = MySQL.executeSearch(query);
            
            if (rs.next()) {
                int stockId = rs.getInt("stock_id");
                UpdateProduct updateProduct = new UpdateProduct(null, true, productId);
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
    
    private void generateProductReport() {
        // TODO: Implement your actual report generation logic here.
        // This could involve fetching data and using a library like JasperReports.
        System.out.println("Product Report generation triggered.");
        JOptionPane.showMessageDialog(this, 
                "Product Report generation is not yet implemented.\n"
                + "You can add your report logic in the 'generateProductReport()' method.", 
                "Feature Under Development", 
                JOptionPane.INFORMATION_MESSAGE);
        this.requestFocusInWindow();
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
        productReportBtn = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        productSearchBar.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        addProductDialog.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addProductDialog.setText("Add Product");
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

        productReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        productReportBtn.setText("Product Report");
        productReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productReportBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 888, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(productSearchBar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(activeRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inactiveRadioBtn)
                        .addGap(184, 184, 184)
                        .addComponent(productReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addProductDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                    .addComponent(activeRadioBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(productReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
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
       AddNewProduct addNewProduct = new AddNewProduct(null, true);
       addNewProduct.setLocationRelativeTo(null);
       addNewProduct.setVisible(true);
        SearchFilters();
    }//GEN-LAST:event_addProductDialogActionPerformed

    private void inactiveRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inactiveRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inactiveRadioBtnActionPerformed

    private void activeRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_activeRadioBtnActionPerformed

    private void productReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productReportBtnActionPerformed
        generateProductReport();
    }//GEN-LAST:event_productReportBtnActionPerformed


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
    private javax.swing.JButton productReportBtn;
    private javax.swing.JTextField productSearchBar;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    // End of variables declaration//GEN-END:variables
}
