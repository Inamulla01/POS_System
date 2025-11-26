package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import lk.com.pos.connection.MySQL;
import lk.com.pos.privateclasses.RoundedPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SalesPanel extends javax.swing.JPanel {

    private JPanel invoicesContainer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
    private int currentWidth = 0;
    private Timer searchTimer;
    
    // Keyboard navigation
    private List<JPanel> invoiceCardsList = new ArrayList<>();
    private int currentCardIndex = -1;
    private JPanel currentFocusedCard = null;
    
    private JPanel positionIndicator;
    private JLabel positionLabel;
    private Timer positionTimer;
    
    private JPanel keyboardHintsPanel;
    private boolean hintsVisible = false;
    
    // Refresh cooldown
    private long lastRefreshTime = 0;
    private static final long REFRESH_COOLDOWN = 1000; // 1 second
    
    // Colors
    private static final Color TEAL_BORDER_SELECTED = new Color(28, 181, 187);
    private static final Color TEAL_BORDER_HOVER = new Color(60, 200, 206);
    private static final Color DEFAULT_BORDER = new Color(230, 230, 230);
    
    // Data classes
    private static class InvoiceData {
        int salesId;
        String invoiceNo;
        String datetime;
        double total;
        double itemDiscount;
        double saleDiscount;
        double totalDiscount;
        String paymentMethod;
        String cashierName;
        String customerName;
        int statusId;
        String saleStatus;
        List<ItemData> items = new ArrayList<>(); // ADD THIS LINE
    }
    
    private static class ItemData {
        String productName;
        int qty;
        double price;
        double discountPrice;
        double total;
        String batchNo;
        
        ItemData(String productName, int qty, double price, double discountPrice, 
                 double total, String batchNo) {
            this.productName = productName;
            this.qty = qty;
            this.price = price;
            this.discountPrice = discountPrice;
            this.total = total;
            this.batchNo = batchNo;
        }
    }

    public SalesPanel() {
        initComponents();
        setupPanel();
        customizeComponents();
        createPositionIndicator();
        createKeyboardHintsPanel();
        setupKeyboardShortcuts();
        
        loadSalesData("", "All Time");
        setupEventListeners();
        
        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
            showKeyboardHints();
        });
    }
    
    private void createPositionIndicator() {
        positionIndicator = new JPanel();
        positionIndicator.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        positionIndicator.setBackground(new Color(31, 41, 55, 230));
        positionIndicator.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_BORDER_SELECTED, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        positionIndicator.setVisible(false);
        
        positionLabel = new JLabel();
        positionLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        positionLabel.setForeground(Color.WHITE);
        
        positionIndicator.add(positionLabel);
        
        setLayout(new OverlayLayout(this) {
            @Override
            public void layoutContainer(Container target) {
                super.layoutContainer(target);
                
                if (positionIndicator.isVisible()) {
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
        });
        
        add(positionIndicator, Integer.valueOf(1000));
    }

    private void createKeyboardHintsPanel() {
        keyboardHintsPanel = new JPanel();
        keyboardHintsPanel.setLayout(new BoxLayout(keyboardHintsPanel, BoxLayout.Y_AXIS));
        keyboardHintsPanel.setBackground(new Color(31, 41, 55, 240));
        keyboardHintsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_BORDER_SELECTED, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);
        
        JLabel title = new JLabel("KEYBOARD SHORTCUTS");
        title.setFont(new Font("Nunito ExtraBold", Font.BOLD, 13));
        title.setForeground(TEAL_BORDER_SELECTED);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);
        
        keyboardHintsPanel.add(Box.createVerticalStrut(10));
        
        addHintRow("↑ ↓", "Navigate invoices", "#FFFFFF");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("F5", "Refresh", "#34D399");
        addHintRow("Ctrl+P", "Sales Report", "#10B981");
        addHintRow("Ctrl+R", "Sales Report", "#10B981");
        addHintRow("Alt+1", "All Time", "#FB923C");
        addHintRow("Alt+2", "Today", "#FCD34D");
        addHintRow("Alt+3", "Last 7 Days", "#1CB5BB");
        addHintRow("Alt+4", "Last 30 Days", "#F87171");
        addHintRow("Alt+5", "Last 90 Days", "#A78BFA");
        addHintRow("Alt+6", "1 Year", "#34D399");
        addHintRow("Alt+7", "2 Years", "#60A5FA");
        addHintRow("Alt+8", "5 Years", "#F472B6");
        addHintRow("Alt+9", "10 Years", "#FBBF24");
        addHintRow("Esc", "Clear All Filters", "#EF4444");
        addHintRow("?", "Toggle Help", "#1CB5BB");
        
        keyboardHintsPanel.add(Box.createVerticalStrut(10));
        
        JLabel closeHint = new JLabel("Press ? to hide");
        closeHint.setFont(new Font("Nunito SemiBold", Font.ITALIC, 10));
        closeHint.setForeground(Color.decode("#9CA3AF"));
        closeHint.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        keyboardHintsPanel.add(closeHint);
        
        add(keyboardHintsPanel, Integer.valueOf(1001));
    }

    private void addHintRow(String key, String description, String keyColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(280, 25));
        
        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(new Font("Consolas", Font.BOLD, 11));
        keyLabel.setForeground(Color.decode(keyColor));
        keyLabel.setPreferredSize(new Dimension(80, 20));
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
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

    private void setupKeyboardShortcuts() {
        this.setFocusable(true);
        
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        
        // Navigation
        registerKeyAction("UP", KeyEvent.VK_UP, 0, condition, () -> navigateCards(-1));
        registerKeyAction("DOWN", KeyEvent.VK_DOWN, 0, condition, () -> navigateCards(1));
        registerKeyAction("HOME", KeyEvent.VK_HOME, 0, condition, () -> navigateToFirst());
        registerKeyAction("END", KeyEvent.VK_END, 0, condition, () -> navigateToLast());
        
        // Search
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, () -> focusSearch());
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, () -> {
            if (!jTextField1.hasFocus()) {
                focusSearch();
            }
        });
        
        // Refresh
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, () -> refreshSales());
        
        // Sales Report - CTRL+P and CTRL+R
        registerKeyAction("CTRL_P", KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, condition, this::openSalesReport);
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::openSalesReport);
        
        // Period filters (Extended)
        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(0));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(1));
        registerKeyAction("ALT_3", KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(2));
        registerKeyAction("ALT_4", KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(3));
        registerKeyAction("ALT_5", KeyEvent.VK_5, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(4));
        registerKeyAction("ALT_6", KeyEvent.VK_6, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(5));
        registerKeyAction("ALT_7", KeyEvent.VK_7, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(6));
        registerKeyAction("ALT_8", KeyEvent.VK_8, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(7));
        registerKeyAction("ALT_9", KeyEvent.VK_9, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(8));
        
        // Escape - UPDATED TO CLEAR ALL FILTERS
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, () -> handleEscape());
        
        // Help
        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, condition, () -> showKeyboardHints());
        
        // Search field
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        jTextField1.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllFilters();
            }
        });
        
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "startNavigation");
        jTextField1.getActionMap().put("startNavigation", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!invoiceCardsList.isEmpty()) {
                    SalesPanel.this.requestFocusInWindow();
                    if (currentCardIndex == -1) {
                        currentCardIndex = 0;
                        selectCurrentCard();
                        scrollToCard(currentCardIndex);
                    }
                }
            }
        });
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> SalesPanel.this.requestFocusInWindow());
            }
        });
    }

    private void registerKeyAction(String actionName, int keyCode, int modifiers, int condition, Runnable action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        this.getInputMap(condition).put(keyStroke, actionName);
        this.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextField1.hasFocus() && 
                    keyCode != KeyEvent.VK_ESCAPE && 
                    modifiers == 0 &&
                    keyCode != KeyEvent.VK_SLASH) {
                    return;
                }
                action.run();
            }
        });
    }

    private void navigateCards(int direction) {
        if (invoiceCardsList.isEmpty()) {
            showPositionIndicator("No invoices available");
            return;
        }
        
        if (currentCardIndex < 0) {
            currentCardIndex = 0;
            selectCurrentCard();
            scrollToCard(currentCardIndex);
            return;
        }
        
        int newIndex = currentCardIndex + direction;
        
        if (newIndex < 0) {
            showPositionIndicator("Already at the first invoice");
            return;
        }
        
        if (newIndex >= invoiceCardsList.size()) {
            showPositionIndicator("Already at the last invoice");
            return;
        }
        
        deselectCard(currentCardIndex);
        currentCardIndex = newIndex;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
    }

    private void navigateToFirst() {
        if (invoiceCardsList.isEmpty()) {
            showPositionIndicator("No invoices available");
            return;
        }
        
        if (currentCardIndex >= 0) {
            deselectCard(currentCardIndex);
        }
        currentCardIndex = 0;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
        showPositionIndicator("First invoice");
    }

    private void navigateToLast() {
        if (invoiceCardsList.isEmpty()) {
            showPositionIndicator("No invoices available");
            return;
        }
        
        if (currentCardIndex >= 0) {
            deselectCard(currentCardIndex);
        }
        currentCardIndex = invoiceCardsList.size() - 1;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
        showPositionIndicator("⬇️ Last invoice");
    }

    private void selectCurrentCard() {
        if (currentCardIndex >= 0 && currentCardIndex < invoiceCardsList.size()) {
            JPanel card = invoiceCardsList.get(currentCardIndex);
            card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(TEAL_BORDER_SELECTED, 3, 20),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            currentFocusedCard = card;
            
            String invoiceNo = (String) card.getClientProperty("invoiceNo");
            showPositionIndicator(String.format("Invoice %d/%d: %s", 
                currentCardIndex + 1, invoiceCardsList.size(), invoiceNo));
        }
    }

    private void deselectCard(int index) {
        if (index >= 0 && index < invoiceCardsList.size()) {
            JPanel card = invoiceCardsList.get(index);
            card.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        }
    }

    private void scrollToCard(int index) {
        if (index < 0 || index >= invoiceCardsList.size()) return;
        
        SwingUtilities.invokeLater(() -> {
            try {
                JPanel card = invoiceCardsList.get(index);
                Rectangle bounds = card.getBounds();
                Rectangle visible = jScrollPane1.getViewport().getViewRect();
                
                int targetY = bounds.y - 20;
                
                jScrollPane1.getViewport().setViewPosition(new Point(0, Math.max(0, targetY)));
            } catch (Exception e) {
                System.err.println("Error scrolling to card: " + e.getMessage());
            }
        });
    }

    private void focusSearch() {
        jTextField1.requestFocus();
        jTextField1.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter (Press ↓ to navigate)");
    }

    private void handleEscape() {
        if (currentCardIndex >= 0) {
            // Deselect invoice card
            deselectCard(currentCardIndex);
            currentFocusedCard = null;
            currentCardIndex = -1;
            showPositionIndicator("Card deselected");
        } else if (!jTextField1.getText().isEmpty() || sortByDays.getSelectedIndex() != 0) {
            // Clear all filters
            clearAllFilters();
        }
        this.requestFocusInWindow();
    }

    private void clearAllFilters() {
        boolean wasFiltered = !jTextField1.getText().isEmpty() || sortByDays.getSelectedIndex() != 0;
        
        jTextField1.setText("");
        sortByDays.setSelectedIndex(0); // Reset to "All Time"
        
        if (wasFiltered) {
            handleSearch();
            showPositionIndicator("All filters cleared - Showing all invoices");
        }
        
        SalesPanel.this.requestFocusInWindow();
    }

    private void refreshSales() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < REFRESH_COOLDOWN) {
            showPositionIndicator("Please wait before refreshing again");
            return;
        }
        
        lastRefreshTime = currentTime;
        handleSearch();
        showPositionIndicator("Sales data refreshed");
        this.requestFocusInWindow();
    }

    private void openSalesReport() {
        salesReportBtn.doClick();
        showPositionIndicator("Opening Sales Report");
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    private void setPeriod(int index) {
        if (index >= 0 && index < sortByDays.getItemCount()) {
            sortByDays.setSelectedIndex(index);
            handlePeriodFilter();
            showPositionIndicator("Filter: " + sortByDays.getItemAt(index));
            this.requestFocusInWindow();
        }
    }
    
    private void setupEventListeners() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = getWidth();
                if (Math.abs(newWidth - currentWidth) > 50) {
                    currentWidth = newWidth;
                    adjustLayoutForWidth(newWidth);
                }
            }
        });
        
        searchTimer = new Timer(300, e -> handleSearch());
        searchTimer.setRepeats(false);
        
        jTextField1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }
        });
        
        sortByDays.addActionListener(e -> {
            handlePeriodFilter();
            this.requestFocusInWindow();
        });
    }
    
    private void handleSearch() {
        String searchText = jTextField1.getText().trim();
        String selectedPeriod = sortByDays.getSelectedItem().toString();
        loadSalesData(searchText, selectedPeriod);
    }
    
    private void handlePeriodFilter() {
        String searchText = jTextField1.getText().trim();
        String selectedPeriod = sortByDays.getSelectedItem().toString();
        loadSalesData(searchText, selectedPeriod);
    }
    
    private void setupPanel() {
        jPanel2.setLayout(new BorderLayout());
        jPanel2.setBackground(new Color(248, 250, 252));
    }
    
    private void adjustLayoutForWidth(int width) {
        if (width < 700) {
            jTextField1.setPreferredSize(new Dimension(width - 40, 50));
            sortByDays.setPreferredSize(new Dimension(width - 40, 50));
        } else {
            int halfWidth = (width - 60) / 2;
            jTextField1.setPreferredSize(new Dimension(halfWidth, 50));
            sortByDays.setPreferredSize(new Dimension(halfWidth, 50));
        }
        revalidate();
        repaint();
    }
    
    private void customizeComponents() {
        // Enhanced search field with FlatLaf styling
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by invoice number...");
        jTextField1.setToolTipText("Search invoices (Ctrl+F or /) - Press ESC to clear all filters");
        jTextField1.setForeground(Color.GRAY);
        
        // Enhanced combo box with FlatLaf styling
        sortByDays.setForeground(Color.GRAY);
        sortByDays.setModel(new DefaultComboBoxModel<>(new String[]{
            "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 90 Days", 
            "1 Year", "2 Years", "5 Years", "10 Years"
        }));
        sortByDays.setToolTipText("Filter by period (Alt+1 to Alt+9) - Press ESC to reset");
        
        // Sales Report Button Tooltip
        salesReportBtn.setToolTipText("Generate Sales Report (Ctrl+P or Ctrl+R)");
        
        roundedPanel1.setVisible(false);
        
        // Enhanced scroll pane
        jScrollPane1.setBorder(null);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(20);
        
        roundedPanel1.setBackgroundColor(Color.decode("#E0F2FF"));
        roundedPanel1.setBorderThickness(0);
        roundedPanel1.setCornerRadius(16);
        
        headPanel.setOpaque(false);
        middelePanel.setOpaque(false);
        buttomPanel.setOpaque(false);
    
        if (productPanel instanceof RoundedPanel) {
            ((RoundedPanel) productPanel).setCornerRadius(16);
            ((RoundedPanel) productPanel).setBorderThickness(0);
        }
    }
    
    private void loadSalesData(String searchText, String period) {
        System.out.println("📊 Loading sales data - Search: '" + searchText + "', Period: '" + period + "'");
        
        clearInvoiceCards();
        
        // Show loading indicator
        showLoadingIndicator();
        
        String finalPeriod = period;
        String finalSearchText = searchText;
        
        SwingWorker<List<InvoiceData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InvoiceData> doInBackground() {
                try {
                    System.out.println("🔍 Fetching invoices from database...");
                    List<InvoiceData> invoices = fetchInvoicesFromDatabase(finalSearchText, finalPeriod);
                    System.out.println("✅ Fetched " + invoices.size() + " invoices from database");
                    return invoices;
                } catch (Exception e) {
                    System.err.println("❌ Error fetching invoices: " + e.getMessage());
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<InvoiceData> invoices = get();
                    System.out.println("🎯 Displaying " + invoices.size() + " invoices in UI");
                    displayInvoices(invoices);
                } catch (Exception e) {
                    System.err.println("❌ Error displaying invoices: " + e.getMessage());
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        jPanel2.removeAll();
                        jPanel2.add(createErrorPanel(e), BorderLayout.CENTER);
                        jPanel2.revalidate();
                        jPanel2.repaint();
                    });
                }
            }
        };
        
        worker.execute();
    }

    private void showLoadingIndicator() {
        SwingUtilities.invokeLater(() -> {
            jPanel2.removeAll();
            jPanel2.add(createLoadingPanel(), BorderLayout.CENTER);
            jPanel2.revalidate();
            jPanel2.repaint();
        });
    }

    private JPanel createLoadingPanel() {
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setBackground(new Color(248, 250, 252));
        loadingPanel.add(Box.createRigidArea(new Dimension(0, 100)));
        
        // Loading spinner
        JLabel spinnerLabel = new JLabel("⏳");
        spinnerLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        spinnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(spinnerLabel);
        loadingPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel loadingLabel = new JLabel("Loading sales data...");
        loadingLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        loadingLabel.setForeground(new Color(71, 85, 105));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(loadingLabel);
        
        JLabel subLabel = new JLabel("Please wait while we fetch your invoices");
        subLabel.setFont(new Font("Nunito", Font.PLAIN, 14));
        subLabel.setForeground(new Color(148, 163, 184));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        loadingPanel.add(subLabel);
        
        return loadingPanel;
    }

    private List<InvoiceData> fetchInvoicesFromDatabase(String searchText, String period) {
    List<InvoiceData> invoices = new ArrayList<>();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    try {
        conn = MySQL.getConnection();
        if (conn == null) {
            System.err.println("❌ Database connection is null");
            return invoices;
        }
        
        // Build the query with period filter and search
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ")
            .append("s.sales_id, s.invoice_no, s.datetime, s.total, ")
            .append("s.status_id, u.name as cashier_name, ")
            .append("s.payment_method_id, st.status_type, ")
            // Calculate item discount in the main query
            .append("COALESCE((SELECT SUM((si.price * si.qty) - si.total) ")
            .append("FROM sale_item si WHERE si.sales_id = s.sales_id), 0) as item_discount, ")
            // Get sale discount from discount table
            .append("COALESCE(d.discount, 0) as sale_discount ")
            .append("FROM sales s ")
            .append("LEFT JOIN user u ON s.user_id = u.user_id ")
            .append("LEFT JOIN i_status st ON s.status_id = st.status_id ")
            .append("LEFT JOIN discount d ON s.discount_id = d.discount_id ")
            .append("WHERE 1=1 ");
        
        // Add search filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            queryBuilder.append("AND s.invoice_no LIKE ? ");
        }
        
        // Add period filter
        String dateFilter = getDateFilter(period);
        if (!dateFilter.isEmpty()) {
            queryBuilder.append("AND ").append(dateFilter).append(" ");
        }
        
        // Order by latest first (newest invoices on top)
        queryBuilder.append("ORDER BY s.datetime DESC, s.sales_id DESC");
        
        pst = conn.prepareStatement(queryBuilder.toString());
        
        int paramIndex = 1;
        if (searchText != null && !searchText.trim().isEmpty()) {
            pst.setString(paramIndex++, "%" + searchText + "%");
        }
        
        rs = pst.executeQuery();
        
        // First, collect all invoice data without items
        List<Integer> salesIds = new ArrayList<>();
        while (rs.next()) {
            InvoiceData data = new InvoiceData();
            data.salesId = rs.getInt("sales_id");
            data.invoiceNo = rs.getString("invoice_no");
            data.datetime = rs.getString("datetime");
            data.total = rs.getDouble("total");
            data.statusId = rs.getInt("status_id");
            data.saleStatus = rs.getString("status_type");
            data.cashierName = rs.getString("cashier_name");
            data.customerName = "Walk-in Customer";
            
            // Determine payment method
            int paymentMethodId = rs.getInt("payment_method_id");
            data.paymentMethod = getPaymentMethodName(paymentMethodId);
            
            // Get discounts directly from the result set
            data.itemDiscount = rs.getDouble("item_discount");
            double saleDiscountPercentage = rs.getDouble("sale_discount");
            
            // Calculate sale discount amount (percentage of total)
            if (saleDiscountPercentage > 0) {
                data.saleDiscount = (data.total * saleDiscountPercentage) / 100;
            } else {
                data.saleDiscount = 0.0;
            }
            
            data.totalDiscount = data.itemDiscount + data.saleDiscount;
            
            invoices.add(data);
            salesIds.add(data.salesId);
        }
        
        System.out.println("✅ Successfully loaded " + invoices.size() + " invoice headers from database");
        
        // Now close the main result set BEFORE loading items
        closeResources(rs, pst, conn);
        
        // Load items for all invoices in batch
        if (!salesIds.isEmpty()) {
            loadItemsForInvoices(invoices, salesIds);
        }
        
    } catch (Exception e) {
        System.err.println("❌ Error fetching real invoices: " + e.getMessage());
        e.printStackTrace();
    } finally {
        closeResources(rs, pst, conn);
    }
    
    return invoices;
}

private void loadItemsForInvoices(List<InvoiceData> invoices, List<Integer> salesIds) {
    // Create a map for quick lookup
    Map<Integer, InvoiceData> invoiceMap = new HashMap<>();
    for (InvoiceData invoice : invoices) {
        invoiceMap.put(invoice.salesId, invoice);
    }
    
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    try {
        conn = MySQL.getConnection();
        if (conn == null) {
            System.err.println("Failed to get database connection for items");
            return;
        }
        
        // Create a query for all sales IDs at once
        String placeholders = String.join(",", Collections.nCopies(salesIds.size(), "?"));
        String query = "SELECT " +
            "si.sales_id, si.qty, si.price, si.discount_price, si.total, " +
            "p.product_name, st.batch_no " +
            "FROM sale_item si " +
            "INNER JOIN stock st ON si.stock_id = st.stock_id " +
            "INNER JOIN product p ON st.product_id = p.product_id " +
            "WHERE si.sales_id IN (" + placeholders + ") " +
            "ORDER BY si.sales_id, si.sale_item_id";
        
        pst = conn.prepareStatement(query);
        
        // Set all the sales IDs as parameters
        for (int i = 0; i < salesIds.size(); i++) {
            pst.setInt(i + 1, salesIds.get(i));
        }
        
        rs = pst.executeQuery();
        
        while (rs.next()) {
            int salesId = rs.getInt("sales_id");
            InvoiceData invoice = invoiceMap.get(salesId);
            
            if (invoice != null) {
                String productName = rs.getString("product_name");
                int qty = rs.getInt("qty");
                double price = rs.getDouble("price");
                double discountPrice = rs.getDouble("discount_price");
                double itemTotal = rs.getDouble("total");
                String batchNo = rs.getString("batch_no");
                
                invoice.items.add(new ItemData(productName, qty, price, discountPrice, itemTotal, batchNo));
            }
        }
        
        System.out.println("✅ Successfully loaded items for " + invoiceMap.size() + " invoices");
        
    } catch (SQLException e) {
        System.err.println("Error loading sale items in batch: " + e.getMessage());
        e.printStackTrace();
    } finally {
        closeResources(rs, pst, conn);
    }
}

    private List<ItemData> loadSaleItemsForInvoice(int salesId) {
        List<ItemData> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = MySQL.getConnection();
            if (conn == null) {
                System.err.println("Failed to get database connection for items");
                return items;
            }
            
            String query = "SELECT " +
                "si.qty, si.price, si.discount_price, si.total, " +
                "p.product_name, st.batch_no " +
                "FROM sale_item si " +
                "INNER JOIN stock st ON si.stock_id = st.stock_id " +
                "INNER JOIN product p ON st.product_id = p.product_id " +
                "WHERE si.sales_id = ? " +
                "ORDER BY si.sale_item_id";
            
            pst = conn.prepareStatement(query);
            pst.setInt(1, salesId);
            rs = pst.executeQuery();
            
            while (rs.next()) {
                String productName = rs.getString("product_name");
                int qty = rs.getInt("qty");
                double price = rs.getDouble("price");
                double discountPrice = rs.getDouble("discount_price");
                double itemTotal = rs.getDouble("total");
                String batchNo = rs.getString("batch_no");
                
                items.add(new ItemData(productName, qty, price, discountPrice, itemTotal, batchNo));
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading sale items: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pst, conn);
        }
        
        return items;
    }

    private String getPaymentMethodName(int paymentMethodId) {
        switch (paymentMethodId) {
            case 1: return "Cash";
            case 2: return "Card";
            case 3: return "Credit";
            case 4: return "Cheque";
            case 5: return "Online";
            default: return "Unknown";
        }
    }

    private String getDateFilter(String period) {
        switch (period) {
            case "Today":
                return "DATE(s.datetime) = CURDATE()";
            case "Last 7 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            case "Last 30 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
            case "Last 90 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 90 DAY)";
            case "1 Year":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 1 YEAR)";
            case "2 Years":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 2 YEAR)";
            case "5 Years":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 5 YEAR)";
            case "10 Years":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 10 YEAR)";
            default:
                return ""; // All Time - no date filter
        }
    }

    private void displayInvoices(List<InvoiceData> invoices) {
        invoicesContainer = new JPanel();
        invoicesContainer.setLayout(new BoxLayout(invoicesContainer, BoxLayout.Y_AXIS));
        invoicesContainer.setBackground(new Color(248, 250, 252));
        invoicesContainer.setOpaque(false);
        invoicesContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        clearInvoiceCards();
        
        if (invoices.isEmpty()) {
            invoicesContainer.add(createNoDataPanel());
        } else {
            for (InvoiceData data : invoices) {
                JPanel invoiceCard = createInvoiceCard(data);
                invoiceCard.putClientProperty("invoiceNo", data.invoiceNo);
                invoicesContainer.add(invoiceCard);
                invoicesContainer.add(Box.createRigidArea(new Dimension(0, 16)));
                invoiceCardsList.add(invoiceCard);
            }
        }
        
        jPanel2.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(248, 250, 252));
        wrapperPanel.add(invoicesContainer, BorderLayout.NORTH);
        jPanel2.add(wrapperPanel, BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
        
        // Show success message
        showPositionIndicator("✅ Loaded " + invoices.size() + " invoices");
    }

    private void clearInvoiceCards() {
        for (JPanel card : invoiceCardsList) {
            for (MouseListener ml : card.getMouseListeners()) {
                card.removeMouseListener(ml);
            }
        }
        invoiceCardsList.clear();
        currentCardIndex = -1;
        currentFocusedCard = null;
    }
    
    private JPanel createNoDataPanel() {
        JPanel noDataPanel = new JPanel();
        noDataPanel.setLayout(new BoxLayout(noDataPanel, BoxLayout.Y_AXIS));
        noDataPanel.setBackground(new Color(248, 250, 252));
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 60)));
        
        JLabel iconLabel = new JLabel("📭");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(iconLabel);
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel noDataLabel = new JLabel("No sales records found");
        noDataLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        noDataLabel.setForeground(new Color(71, 85, 105));
        noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(noDataLabel);
        
        JLabel hintLabel = new JLabel("Try adjusting your search or filter to see more results");
        hintLabel.setFont(new Font("Nunito", Font.PLAIN, 15));
        hintLabel.setForeground(new Color(148, 163, 184));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        noDataPanel.add(hintLabel);
        
        return noDataPanel;
    }
    
    private JPanel createErrorPanel(Exception e) {
        RoundedPanel errorPanel = new RoundedPanel();
        errorPanel.setBackgroundColor(new Color(254, 242, 242));
        errorPanel.setBorderThickness(2);
        errorPanel.setCornerRadius(16);
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        errorPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        errorPanel.setMaximumSize(new Dimension(500, 250));
        errorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorPanel.add(iconLabel);
        errorPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        
        JLabel errorLabel = new JLabel("Oops! Something went wrong");
        errorLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorPanel.add(errorLabel);
        
        JLabel errorDetails = new JLabel("<html><div style='text-align: center;'>" + 
                                        "We couldn't load the sales data.<br>" +
                                        "Please check your connection and try again.</div></html>");
        errorDetails.setFont(new Font("Nunito", Font.PLAIN, 14));
        errorDetails.setForeground(new Color(185, 28, 28));
        errorDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        errorPanel.add(errorDetails);
        
        JButton retryButton = new JButton("Retry");
        retryButton.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        retryButton.setForeground(Color.WHITE);
        retryButton.setBackground(new Color(220, 38, 38));
        retryButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        retryButton.setFocusPainted(false);
        retryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.addActionListener(ev -> loadSalesData("", "All Time"));
        errorPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        errorPanel.add(retryButton);
        
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(248, 250, 252));
        container.add(Box.createVerticalGlue());
        container.add(errorPanel);
        container.add(Box.createVerticalGlue());
        
        return container;
    }
    
    private JPanel createInvoiceCard(InvoiceData data) {
        // Extract values from data object
        int salesId = data.salesId;
        String invoiceNo = data.invoiceNo;
        String datetime = data.datetime;
        double total = data.total;
        double itemDiscount = data.itemDiscount;
        double saleDiscount = data.saleDiscount;
        double totalDiscount = data.totalDiscount;
        String paymentMethod = data.paymentMethod;
        String cashierName = data.cashierName;
        String customerName = data.customerName;
        String saleStatus = data.saleStatus;
        
        RoundedPanel cardPanel = new RoundedPanel();
        cardPanel.setLayout(new BorderLayout(0, 0));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setCornerRadius(20);
        cardPanel.setBorderThickness(0);
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setOpaque(false);

        JPanel headerPanel = createInvoiceHeader(invoiceNo, customerName, paymentMethod, total, saleStatus);
        JPanel itemsPanel = createItemsContentPanel(data.items); // Use pre-loaded items
        JPanel footerPanel = createInvoiceFooter(datetime, itemDiscount, saleDiscount, totalDiscount, cashierName, salesId);

        headerPanel.setOpaque(false);
        itemsPanel.setOpaque(false);
        footerPanel.setOpaque(false);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(itemsPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        cardPanel.add(contentPanel, BorderLayout.CENTER);

        cardPanel.addMouseListener(new MouseAdapter() {
            private Timer hoverTimer;
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (cardPanel != currentFocusedCard) {
                    hoverTimer = new Timer(10, new ActionListener() {
                        float alpha = 0f;
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            alpha += 0.1f;
                            if (alpha >= 1f) {
                                alpha = 1f;
                                ((Timer)evt.getSource()).stop();
                            }
                            Color baseColor = new Color(245, 247, 250);
                            cardPanel.setBackground(baseColor);
                            contentPanel.setBackground(baseColor);
                            cardPanel.repaint();
                        }
                    });
                    hoverTimer.start();
                }
                cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoverTimer != null) hoverTimer.stop();
                if (cardPanel != currentFocusedCard) {
                    cardPanel.setBackground(Color.WHITE);
                    contentPanel.setBackground(Color.WHITE);
                    cardPanel.repaint();
                }
                cardPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                cardPanel.setBackground(new Color(241, 245, 249));
                contentPanel.setBackground(new Color(241, 245, 249));
                cardPanel.repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                cardPanel.setBackground(new Color(248, 250, 252));
                contentPanel.setBackground(new Color(248, 250, 252));
                cardPanel.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentFocusedCard != null && currentFocusedCard != cardPanel) {
                    int oldIndex = invoiceCardsList.indexOf(currentFocusedCard);
                    if (oldIndex >= 0) {
                        deselectCard(oldIndex);
                    }
                }
                
                currentCardIndex = invoiceCardsList.indexOf(cardPanel);
                selectCurrentCard();
                SalesPanel.this.requestFocusInWindow();
            }
        });

        return cardPanel;
    }

    private JPanel createInvoiceHeader(String invoiceNo, String customerName, String paymentMethod, 
                                  double total, String saleStatus) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout(20, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        // LEFT SIDE: Invoice info with modern layout
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // Invoice number with icon inline
        JPanel invoiceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        invoiceRow.setOpaque(false);
        invoiceRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add invoice icon
        JLabel invoiceIcon = new JLabel("📄");
        invoiceIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        
        JLabel invoiceLabel = new JLabel("#" + (invoiceNo != null ? invoiceNo.toUpperCase() : ""));
        invoiceLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 24));
        invoiceLabel.setForeground(new Color(30, 41, 59));
        
        invoiceRow.add(invoiceIcon);
        invoiceRow.add(invoiceLabel);

        // Customer name
        JLabel customerLabel = new JLabel((customerName != null ? customerName : "Walk-in Customer"));
        customerLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        customerLabel.setForeground(new Color(100, 116, 139));
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(invoiceRow);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftPanel.add(customerLabel);

        // RIGHT SIDE: Clean layout with total and badges
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // Total amount - prominent display
        JLabel totalLabel = new JLabel(String.format("Rs.%.2f", total));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 28));
        totalLabel.setForeground(new Color(16, 185, 129)); // Emerald green
        totalLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // Badges container - placed below total with proper spacing
        JPanel badgesPanel = new JPanel();
        badgesPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badgesPanel.setOpaque(false);
        badgesPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        // Payment badge
        JLabel paymentBadge = createPaymentBadge(paymentMethod);
        badgesPanel.add(paymentBadge);
        
        // Status badge (only if not completed)
        if (saleStatus != null && !isCompletedStatus(saleStatus)) {
            JLabel statusBadge = createStatusBadge(saleStatus);
            badgesPanel.add(statusBadge);
        }

        rightPanel.add(totalLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Space between total and badges
        rightPanel.add(badgesPanel);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private boolean isCompletedStatus(String status) {
        if (status == null) return true;
        String normalized = status.trim().toLowerCase();
        return normalized.equals("completed") || normalized.equals("done") || 
               normalized.equals("success") || normalized.equals("finished") ||
               normalized.equals("paid") || normalized.equals("confirmed") ||
               normalized.isEmpty();
    }

    private JLabel createStatusBadge(String status) {
        if (status == null) return new JLabel();
        
        String normalizedStatus = status.trim().toLowerCase();
        JLabel statusBadge = new JLabel();
        statusBadge.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        
        // Status mapping with emojis
        switch (normalizedStatus) {
            case "due amount":
            case "due":
            case "pending payment":
                statusBadge.setBackground(new Color(234, 88, 12)); // Orange
                statusBadge.setText("DUE");
                break;
            case "no due":
            case "paid":
            case "complete":
                statusBadge.setBackground(new Color(34, 197, 94)); // Green
                statusBadge.setText("PAID");
                break;
            case "hold":
            case "holding":
                statusBadge.setBackground(new Color(245, 158, 11)); // Amber
                statusBadge.setText("HOLD");
                break;
            case "cancelled":
            case "canceled":
                statusBadge.setBackground(new Color(220, 38, 38)); // Red
                statusBadge.setText("CANCELLED");
                break;
            case "void":
            case "voided":
                statusBadge.setBackground(new Color(159, 18, 57)); // Dark Red
                statusBadge.setText("VOID");
                break;
            case "pending":
            case "processing":
                statusBadge.setBackground(new Color(245, 158, 11)); // Amber
                statusBadge.setText("PENDING");
                break;
            default:
                statusBadge.setBackground(new Color(100, 116, 139)); // Gray
                String displayText = status.toUpperCase();
                if (displayText.length() > 12) {
                    displayText = displayText.substring(0, 12) + "...";
                }
                statusBadge.setText(displayText);
        }
        
        return statusBadge;
    }

    private JPanel createItemsContentPanel(List<ItemData> items) {
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BorderLayout());
        itemsPanel.setOpaque(false);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel itemsHeader = new JLabel("ITEMS");
        itemsHeader.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        itemsHeader.setForeground(new Color(71, 85, 105));
        itemsHeader.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        
        headerPanel.add(itemsHeader, BorderLayout.WEST);

        RoundedPanel itemsContainer = new RoundedPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackgroundColor(new Color(249, 250, 251));
        itemsContainer.setCornerRadius(12);
        itemsContainer.setBorderThickness(0);
        itemsContainer.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Use pre-loaded items
        if (items.isEmpty()) {
            JLabel noItemsLabel = new JLabel("No items in this sale");
            noItemsLabel.setFont(new Font("Nunito SemiBold", Font.ITALIC, 13));
            noItemsLabel.setForeground(new Color(148, 163, 184));
            noItemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemsContainer.add(noItemsLabel);
        } else {
            for (int i = 0; i < items.size(); i++) {
                ItemData item = items.get(i);
                JPanel itemCard = createItemCard(item.productName, item.qty, item.price, 
                                               item.discountPrice, item.total, item.batchNo);
                itemsContainer.add(itemCard);
                
                if (i < items.size() - 1) {
                    JSeparator separator = new JSeparator();
                    separator.setForeground(new Color(229, 231, 235));
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    separator.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
                    itemsContainer.add(separator);
                }
            }
        }

        itemsPanel.add(headerPanel, BorderLayout.NORTH);
        itemsPanel.add(itemsContainer, BorderLayout.CENTER);

        return itemsPanel;
    }

    private JPanel createInvoiceFooter(String datetime, double itemDiscount, double saleDiscount, 
                                 double totalDiscount, String cashierName, int salesId) {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Discount section (if applicable)
        if (totalDiscount > 0) {
            RoundedPanel discountPanel = new RoundedPanel();
            discountPanel.setLayout(new BoxLayout(discountPanel, BoxLayout.Y_AXIS));
            discountPanel.setBackgroundColor(new Color(255, 247, 237)); // Warm orange tint
            discountPanel.setCornerRadius(12);
            discountPanel.setBorderThickness(0);
            discountPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            discountPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
            
            JLabel discountHeader = new JLabel("DISCOUNTS APPLIED");
            discountHeader.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
            discountHeader.setForeground(new Color(194, 65, 12));
            discountPanel.add(discountHeader);
            discountPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            
            if (itemDiscount > 0) {
                JPanel itemDiscountPanel = createDiscountRow("Item Discount", itemDiscount, new Color(100, 116, 139));
                itemDiscountPanel.setOpaque(false);
                discountPanel.add(itemDiscountPanel);
                discountPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            }
            
            if (saleDiscount > 0) {
                JPanel saleDiscountPanel = createDiscountRow("Sale Discount", saleDiscount, new Color(245, 158, 11));
                saleDiscountPanel.setOpaque(false);
                discountPanel.add(saleDiscountPanel);
                discountPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            }
            
            discountPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            
            JSeparator separator = new JSeparator();
            separator.setForeground(new Color(253, 186, 116));
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            discountPanel.add(separator);
            discountPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            
            JPanel totalDiscountPanel = createDiscountRow("TOTAL SAVINGS", totalDiscount, new Color(220, 38, 38));
            totalDiscountPanel.setOpaque(false);
            discountPanel.add(totalDiscountPanel);
            
            footerPanel.add(discountPanel);
            footerPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        }

        // Bottom info bar with better styling
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(16, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));

        // Date on left
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        datePanel.setOpaque(false);
        
        JLabel dateIcon = new JLabel("📅");
        dateIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        
        JLabel dateLabel = new JLabel(formatDateTime(datetime));
        dateLabel.setFont(new Font("Nunito", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(100, 116, 139));
        
        datePanel.add(dateIcon);
        datePanel.add(dateLabel);

        // Cashier on right
        JPanel cashierPanel = new JPanel();
        cashierPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        cashierPanel.setOpaque(false);
        
        JLabel cashierIcon = new JLabel("👤");
        cashierIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        
        JLabel cashierLabel = new JLabel("Cashier: ");
        cashierLabel.setFont(new Font("Nunito", Font.PLAIN, 13));
        cashierLabel.setForeground(new Color(100, 116, 139));
        
        JLabel cashierValue = new JLabel(cashierName != null ? cashierName : "Unknown");
        cashierValue.setFont(new Font("Nunito SemiBold", Font.BOLD, 13));
        cashierValue.setForeground(new Color(51, 65, 85));
        
        cashierPanel.add(cashierIcon);
        cashierPanel.add(cashierLabel);
        cashierPanel.add(cashierValue);

        bottomPanel.add(datePanel, BorderLayout.WEST);
        bottomPanel.add(cashierPanel, BorderLayout.EAST);

        footerPanel.add(bottomPanel);

        return footerPanel;
    }

    private JPanel createDiscountRow(String label, double amount, Color color) {
        JPanel discountRow = new JPanel();
        discountRow.setLayout(new BoxLayout(discountRow, BoxLayout.X_AXIS));
        discountRow.setOpaque(false);
        discountRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel discountLabel = new JLabel(label);
        discountLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        discountLabel.setForeground(new Color(71, 85, 105));

        JLabel discountValue = new JLabel(String.format("-Rs.%.2f", amount));
        discountValue.setFont(new Font("Nunito ExtraBold", Font.BOLD, 15));
        discountValue.setForeground(color);

        discountRow.add(discountLabel);
        discountRow.add(Box.createHorizontalGlue());
        discountRow.add(discountValue);

        return discountRow;
    }

    private JLabel createPaymentBadge(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "UNKNOWN";
        }
        
        String normalizedPayment = paymentMethod.trim().toUpperCase();
        
        JLabel paymentBadge = new JLabel();
        paymentBadge.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        paymentBadge.setForeground(Color.WHITE);
        paymentBadge.setOpaque(true);
        
        // Clean, user-friendly styling with proper padding
        paymentBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        
        // Simple color coding without emojis for cleaner look
        if (normalizedPayment.contains("CASH")) {
            paymentBadge.setBackground(new Color(34, 197, 94)); // Green
            paymentBadge.setText("CASH");
        } else if (normalizedPayment.contains("CARD") || normalizedPayment.contains("CREDIT CARD")) {
            paymentBadge.setBackground(new Color(59, 130, 246)); // Blue
            paymentBadge.setText("CARD");
        } else if (normalizedPayment.contains("CREDIT") || normalizedPayment.contains("ACCOUNT")) {
            paymentBadge.setBackground(new Color(234, 88, 12)); // Orange
            paymentBadge.setText("CREDIT");
        } else if (normalizedPayment.contains("CHEQUE") || normalizedPayment.contains("CHECK")) {
            paymentBadge.setBackground(new Color(139, 92, 246)); // Purple
            paymentBadge.setText("CHEQUE");
        } else if (normalizedPayment.contains("ONLINE") || normalizedPayment.contains("DIGITAL")) {
            paymentBadge.setBackground(new Color(168, 85, 247)); // Violet
            paymentBadge.setText("ONLINE");
        } else {
            paymentBadge.setBackground(new Color(100, 116, 139)); // Gray
            String displayText = normalizedPayment.length() > 12 ? 
                               normalizedPayment.substring(0, 12) + "..." : normalizedPayment;
            paymentBadge.setText(displayText);
        }
        
        return paymentBadge;
    }

    private JPanel createItemCard(String productName, int qty, double price, 
                          double discountPrice, double total, String batchNo) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout(15, 0));
        itemPanel.setOpaque(false);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel productLabel = new JLabel(productName != null ? productName : "Unknown Product");
        productLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 15));
        productLabel.setForeground(new Color(30, 41, 59));
        productLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.X_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceQtyLabel = new JLabel(String.format("Rs.%.2f × %d", price, qty));
        priceQtyLabel.setFont(new Font("Nunito", Font.PLAIN, 13));
        priceQtyLabel.setForeground(new Color(100, 116, 139));
        priceQtyLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        detailsPanel.add(priceQtyLabel);

        if (discountPrice > 0) {
            JLabel discountInfo = new JLabel(String.format(" • Rs.%.2f discount", discountPrice));
            discountInfo.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
            discountInfo.setForeground(new Color(220, 38, 38));
            discountInfo.setBorder(BorderFactory.createEmptyBorder(2, 8, 0, 0));
            detailsPanel.add(discountInfo);
        }

        JPanel extraInfoPanel = new JPanel();
        extraInfoPanel.setLayout(new BoxLayout(extraInfoPanel, BoxLayout.X_AXIS));
        extraInfoPanel.setOpaque(false);
        extraInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (batchNo != null && !batchNo.isEmpty()) {
            JLabel batchLabel = new JLabel("Batch: " + batchNo);
            batchLabel.setFont(new Font("Nunito", Font.PLAIN, 12));
            batchLabel.setForeground(new Color(148, 163, 184));
            batchLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            extraInfoPanel.add(batchLabel);
        }

        leftPanel.add(productLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftPanel.add(detailsPanel);
        
        if (extraInfoPanel.getComponentCount() > 0) {
            leftPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            leftPanel.add(extraInfoPanel);
        }

        JLabel totalLabel = new JLabel(String.format("Rs.%.2f", total));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        totalLabel.setForeground(new Color(30, 41, 59));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        itemPanel.add(leftPanel, BorderLayout.CENTER);
        itemPanel.add(totalLabel, BorderLayout.EAST);

        return itemPanel;
    }

    private String formatDateTime(String datetime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            Date date = inputFormat.parse(datetime);
            return outputFormat.format(date);
        } catch (Exception e) {
            if (datetime != null && datetime.length() >= 10) {
                return datetime.substring(0, 10) + ", " + (datetime.length() > 11 ? datetime.substring(11) : "");
            }
            return datetime != null ? datetime : "Unknown Date";
        }
    }
    
    // Utility method to close database resources
    private void closeResources(ResultSet rs, PreparedStatement pst, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.err.println("Error closing ResultSet: " + e.getMessage());
        }
        try {
            if (pst != null) pst.close();
        } catch (SQLException e) {
            System.err.println("Error closing PreparedStatement: " + e.getMessage());
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing Connection: " + e.getMessage());
        }
    }
    
    static class RoundBorder extends AbstractBorder {
        private Color color;
        private int thickness;
        private int radius;
        
        RoundBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x + thickness/2, y + thickness/2, 
                             width - thickness, height - thickness, 
                             radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }
    }
    
    static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            for (int i = 0; i < 4; i++) {
                g2d.setColor(new Color(0, 0, 0, 5 - i));
                g2d.drawRoundRect(x + i, y + i, width - 2*i - 1, height - 2*i - 1, 20, 20);
            }
            
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        headPanel = new javax.swing.JPanel();
        paymentTypeBtn = new javax.swing.JButton();
        total = new javax.swing.JLabel();
        invoiceName = new javax.swing.JLabel();
        customerName = new javax.swing.JLabel();
        logo = new javax.swing.JLabel();
        middelePanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        productPanel = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        productprice = new javax.swing.JLabel();
        buttomPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        date = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        sortByDays = new javax.swing.JComboBox<>();
        salesReportBtn = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        paymentTypeBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        paymentTypeBtn.setText("card");

        total.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        total.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        total.setText("Rs.1000");

        invoiceName.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        invoiceName.setText("Invoice #inv0010");

        customerName.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        customerName.setText("Coustomer Name");

        logo.setText("icon");

        javax.swing.GroupLayout headPanelLayout = new javax.swing.GroupLayout(headPanel);
        headPanel.setLayout(headPanelLayout);
        headPanelLayout.setHorizontalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logo, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customerName)
                    .addComponent(invoiceName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 434, Short.MAX_VALUE)
                .addComponent(paymentTypeBtn)
                .addGap(18, 18, 18)
                .addComponent(total, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headPanelLayout.setVerticalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPanelLayout.createSequentialGroup()
                .addGroup(headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(paymentTypeBtn)
                            .addComponent(total)))
                    .addGroup(headPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(headPanelLayout.createSequentialGroup()
                                .addComponent(invoiceName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(customerName))
                            .addComponent(logo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        middelePanel.setBackground(new java.awt.Color(252, 246, 252));

        jLabel5.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        jLabel5.setText("Items:");

        productPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel6.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        jLabel6.setText("Metformin 500mg Tablets");

        jLabel7.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        jLabel7.setText("Rs.75.00 * 10 (Rs.5.00 discount)");

        productprice.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        productprice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        productprice.setText("Rs.750");

        javax.swing.GroupLayout productPanelLayout = new javax.swing.GroupLayout(productPanel);
        productPanel.setLayout(productPanelLayout);
        productPanelLayout.setHorizontalGroup(
            productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(productprice, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        productPanelLayout.setVerticalGroup(
            productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productPanelLayout.createSequentialGroup()
                .addGroup(productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7))
                    .addGroup(productPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(productprice)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout middelePanelLayout = new javax.swing.GroupLayout(middelePanel);
        middelePanel.setLayout(middelePanelLayout);
        middelePanelLayout.setHorizontalGroup(
            middelePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(middelePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(middelePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(productPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(middelePanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        middelePanelLayout.setVerticalGroup(
            middelePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(middelePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel9.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jLabel9.setText("Discount");

        date.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        date.setText("date");

        jLabel12.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jLabel12.setText("Cashier");

        javax.swing.GroupLayout buttomPanelLayout = new javax.swing.GroupLayout(buttomPanel);
        buttomPanel.setLayout(buttomPanelLayout);
        buttomPanelLayout.setHorizontalGroup(
            buttomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttomPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(date)
                .addGap(350, 350, 350)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 318, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addGap(80, 80, 80))
        );
        buttomPanelLayout.setVerticalGroup(
            buttomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttomPanelLayout.createSequentialGroup()
                .addGroup(buttomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buttomPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(middelePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(middelePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(34, 34, 34))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(187, 187, 187))
        );

        jScrollPane1.setViewportView(jPanel2);

        sortByDays.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        sortByDays.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        sortByDays.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        sortByDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByDaysActionPerformed(evt);
            }
        });

        salesReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        salesReportBtn.setText("Sales Report");
        salesReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salesReportBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 955, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByDays, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(100, 100, 100)
                        .addComponent(salesReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sortByDays, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(salesReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                .addGap(17, 17, 17))
        );

        sortByDays.getAccessibleContext().setAccessibleName("Short by Days");

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

    private void sortByDaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByDaysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortByDaysActionPerformed

    private void salesReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salesReportBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_salesReportBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttomPanel;
    private javax.swing.JLabel customerName;
    private javax.swing.JLabel date;
    private javax.swing.JPanel headPanel;
    private javax.swing.JLabel invoiceName;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel logo;
    private javax.swing.JPanel middelePanel;
    private javax.swing.JButton paymentTypeBtn;
    private lk.com.pos.privateclasses.RoundedPanel productPanel;
    private javax.swing.JLabel productprice;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private javax.swing.JButton salesReportBtn;
    private javax.swing.JComboBox<String> sortByDays;
    private javax.swing.JLabel total;
    // End of variables declaration//GEN-END:variables
}
