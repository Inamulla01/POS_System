package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.connection.MySQL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.AbstractAction;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.SwingConstants;

/**
 * ExpensePanel - Displays and manages expense information with status toggle
 * Features: Search, filters, keyboard navigation, status management, time filtering
 * 
 * @author pasin
 * @version 2.0
 */
public class ExpensePanel extends javax.swing.JPanel {
   
    // Date & Number Formatting
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    
    // UI Constants - Modern Color Palette
    private static final class Colors {
        static final Color TEAL_PRIMARY = new Color(28, 181, 187);
        static final Color TEAL_HOVER = new Color(60, 200, 206);
        static final Color BORDER_DEFAULT = new Color(230, 230, 230);
        static final Color BACKGROUND = Color.decode("#F8FAFC");
        static final Color CARD_WHITE = Color.WHITE;
        static final Color CARD_INACTIVE = Color.decode("#F8F9FA");
        static final Color TEXT_PRIMARY = Color.decode("#1E293B");
        static final Color TEXT_SECONDARY = Color.decode("#6B7280");
        static final Color TEXT_MUTED = Color.decode("#94A3B8");
        static final Color TEXT_INACTIVE = Color.decode("#9CA3AF");
        
        // Status Colors - Modern Badge Style
        static final Color STATUS_PAID_BG = Color.decode("#DCFCE7");
        static final Color STATUS_PAID_FG = Color.decode("#166534");
        static final Color STATUS_PAID_BORDER = Color.decode("#BBF7D0");
        
        // Updated Unpaid Colors - Light Red Theme
        static final Color STATUS_UNPAID_BG = Color.decode("#FEF2F2");
        static final Color STATUS_UNPAID_FG = Color.decode("#DC2626");
        static final Color STATUS_UNPAID_BORDER = Color.decode("#FECACA");
        
        // Button Colors - Modern Style
        static final Color BTN_EDIT_BG = Color.decode("#EFF6FF");
        static final Color BTN_EDIT_HOVER = Color.decode("#DBEAFE");
        static final Color BTN_EDIT_FG = Color.decode("#1D4ED8");
        
        static final Color BTN_DELETE_BG = Color.decode("#FEF2F2");
        static final Color BTN_DELETE_HOVER = Color.decode("#FECACA");
        static final Color BTN_DELETE_FG = Color.decode("#DC2626");
        
        // Status Toggle Button Colors
        static final Color BTN_STATUS_BG = Color.decode("#FEFCE8");
        static final Color BTN_STATUS_HOVER = Color.decode("#FEF9C3");
        static final Color BTN_STATUS_FG = Color.decode("#CA8A04");
        
        // Position Indicator
        static final Color POSITION_BG = new Color(31, 41, 55, 230);
        static final Color POSITION_FG = Color.WHITE;
        
        // Help Panel
        static final Color HELP_BG = new Color(31, 41, 55, 240);
        static final Color HELP_TITLE = Colors.TEAL_PRIMARY;
        static final Color HELP_TEXT = Color.decode("#D1D5DB");
        
        // Amount Colors
        static final Color AMOUNT_PAID = TEAL_PRIMARY;
        static final Color AMOUNT_UNPAID = Color.decode("#EF4444");
        static final Color AMOUNT_BG_PAID = Color.decode("#F0F9FF");
        static final Color AMOUNT_BG_UNPAID = Color.decode("#FEF2F2");
    }
    
    // UI Constants - Modern Dimensions
    private static final class Dimensions {
        static final Dimension CARD_SIZE = new Dimension(420, 280);
        static final Dimension CARD_MAX_SIZE = new Dimension(420, 280);
        static final Dimension CARD_MIN_SIZE = new Dimension(380, 280);
        static final Dimension ACTION_BUTTON_SIZE = new Dimension(30, 30);
        static final int CARD_WIDTH_WITH_GAP = 445;
        static final int GRID_GAP = 25;
        static final int CARD_PADDING = 18;
        static final int BORDER_RADIUS = 15;
    }
    
    // UI Constants - Fonts (Using Nunito like ProductPanel)
    private static final class Fonts {
        static final java.awt.Font HEADER = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font SECTION_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font DETAIL_TITLE = new java.awt.Font("Nunito SemiBold", 0, 13);
        static final java.awt.Font DETAIL_VALUE = new java.awt.Font("Nunito SemiBold", 1, 14);
        static final java.awt.Font AMOUNT = new java.awt.Font("Nunito ExtraBold", 1, 18);
        static final java.awt.Font POSITION = new java.awt.Font("Nunito ExtraBold", 1, 14);
        static final java.awt.Font HINT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 13);
        static final java.awt.Font HINT_KEY = new java.awt.Font("Consolas", 1, 11);
        static final java.awt.Font HINT_DESC = new java.awt.Font("Nunito SemiBold", 0, 11);
        static final java.awt.Font BUTTON = new java.awt.Font("Nunito SemiBold", 0, 13);
    }
    
    // UI Constants - Strings
    private static final class Strings {
        static final String SEARCH_PLACEHOLDER = "🔍 Search by expense type...";
        static final String NO_EXPENSES = "📝 No expenses found";
        static final String NO_EXPENSES_DESC = "Try adjusting your search or filters";
        static final String SECTION_DETAILS = "EXPENSE DETAILS";
        static final String STATUS_PAID = "PAID";
        static final String STATUS_UNPAID = "UNPAID";
        static final String HELP_TITLE = "⌨️ KEYBOARD SHORTCUTS";
        static final String HELP_CLOSE_HINT = "Press ? to hide";
        
        // Time filter options
        static final String[] TIME_FILTERS = {
            "📅 All Time", "🕐 Today", "📆 Last 7 Days", "🗓️ Last 30 Days", "📊 Last 90 Days", 
            "📈 1 Year", "📉 2 Years", "💼 5 Years", "🏢 10 Years"
        };
    }

    // Keyboard Navigation
    private lk.com.pos.privateclasses.RoundedPanel currentFocusedCard = null;
    private List<lk.com.pos.privateclasses.RoundedPanel> expenseCardsList = new ArrayList<>();
    private int currentCardIndex = -1;
    private int currentColumns = 3;
    
    // UI Components
    private JPanel positionIndicator;
    private JLabel positionLabel;
    private Timer positionTimer;
    private JPanel keyboardHintsPanel;
    private boolean hintsVisible = false;

    public ExpensePanel() {
        initComponents();
        
        // Configure scroll pane
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        
        initializeUI();
        createPositionIndicator();
        createKeyboardHintsPanel();
        setupKeyboardShortcuts();
        loadExpenses();
        setupEventListeners();
        radioButtonListener();
        
        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
            showKeyboardHints();
        });
    }

    /**
     * Initializes UI components and settings
     */
    private void initializeUI() {
        setupScrollPane();
        setupIcons();
        setupSearchField();
        setupRadioButtons();
        setupButtons();
        setupTimeFilter();
        setupPanel();
    }
    
    private void setupScrollPane() {
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5; thumb: #1CB5BB; width: 8");
    }
    
    private void setupIcons() {
        // Icons will be loaded as needed
    }
    
    private void setupSearchField() {
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Strings.SEARCH_PLACEHOLDER);
        
        try {
            jTextField1.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, 
                new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        } catch (Exception e) {
            System.err.println("Error loading search icon: " + e.getMessage());
        }
        
        // Set tooltips with keyboard shortcuts
        jRadioButton1.setToolTipText("Filter unpaid expenses (Alt+1)");
        jRadioButton2.setToolTipText("Filter paid expenses (Alt+2)");
        jTextField1.setToolTipText("Search expenses (Ctrl+F or /) - Press ? for help");
        expenseReportBtn.setToolTipText("Generate Expense Report (Ctrl+R)");
        addNewExpenseBtn.setToolTipText("Add New Expense (Ctrl+N or Alt+A)");
    }
    
    private void setupTimeFilter() {
        // Remove existing items and add time filter options
        sortByDays.removeAllItems();
        for (String filter : Strings.TIME_FILTERS) {
            sortByDays.addItem(filter);
        }
        
        // Set default selection
        sortByDays.setSelectedItem("📅 All Time");
    }
    
    private void setupButtons() {
        expenseReportBtn.setToolTipText("📊 Generate Expense Report (Ctrl+R)");
        addNewExpenseBtn.setToolTipText("➕ Add New Expense (Ctrl+N or Alt+A)");
        
        // Style buttons
        styleButton(addNewExpenseBtn, Colors.TEAL_PRIMARY, Color.WHITE);
        styleButton(expenseReportBtn, Color.decode("#8B5CF6"), Color.WHITE);
    }
    
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }
    
    private void setupRadioButtons() {
        jRadioButton1.putClientProperty(FlatClientProperties.STYLE, "foreground:#EF4444;");
        jRadioButton2.putClientProperty(FlatClientProperties.STYLE, "foreground:#10B981;");
        
        jRadioButton1.setToolTipText("Filter unpaid expenses (Alt+1)");
        jRadioButton2.setToolTipText("Filter paid expenses (Alt+2)");
        
        setupRadioButtonToggle(jRadioButton1);
        setupRadioButtonToggle(jRadioButton2);
    }
    
    private void setupRadioButtonToggle(javax.swing.JRadioButton radioBtn) {
        radioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (radioBtn.isSelected()) {
                    // Clear selection if already selected
                    SwingUtilities.invokeLater(() -> {
                        radioBtn.setSelected(false);
                        performSearch();
                    });
                }
            }
        });
    }
    
    private void setupPanel() {
        jPanel1.setBackground(Colors.BACKGROUND);
        jPanel2.setBackground(Colors.BACKGROUND);
    }

    private void createPositionIndicator() {
        positionIndicator = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        positionIndicator.setBackground(Colors.POSITION_BG);
        positionIndicator.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        positionIndicator.setVisible(false);
        
        positionLabel = new JLabel();
        positionLabel.setFont(Fonts.POSITION);
        positionLabel.setForeground(Colors.POSITION_FG);
        
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
    
    private void createKeyboardHintsPanel() {
        keyboardHintsPanel = new JPanel();
        keyboardHintsPanel.setLayout(new BoxLayout(keyboardHintsPanel, BoxLayout.Y_AXIS));
        keyboardHintsPanel.setBackground(Colors.HELP_BG);
        keyboardHintsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);
        
        JLabel title = new JLabel(Strings.HELP_TITLE);
        title.setFont(Fonts.HINT_TITLE);
        title.setForeground(Colors.HELP_TITLE);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);
        keyboardHintsPanel.add(Box.createVerticalStrut(10));
        
        // Keyboard shortcuts
        addHintRow("← → ↑ ↓", "Navigate cards", "#FFFFFF");
        addHintRow("E", "Edit Expense", "#1CB5BB");
        addHintRow("D", "Toggle Status", "#F87171");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("Ctrl+N/Alt+A", "Add Expense", "#60D5F2");
        addHintRow("Ctrl+R", "Expense Report", "#34D399");
        addHintRow("F5", "Refresh List", "#FB923C");
        addHintRow("Alt+1", "Unpaid Expenses", "#FB923C");
        addHintRow("Alt+2", "Paid Expenses", "#FB923C");
        addHintRow("Esc", "Clear/Back", "#9CA3AF");
        addHintRow("?", "Toggle Help", "#1CB5BB");
        
        keyboardHintsPanel.add(Box.createVerticalStrut(10));
        
        JLabel closeHint = new JLabel(Strings.HELP_CLOSE_HINT);
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
        descLabel.setForeground(Colors.HELP_TEXT);
        
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
        registerKeyAction("D", KeyEvent.VK_D, 0, condition, this::toggleStatusForSelectedCard);
        
        // Enter to start navigation
        registerKeyAction("ENTER", KeyEvent.VK_ENTER, 0, condition, this::handleEnterKey);
        
        // Search
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, this::focusSearch);
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, this::handleSlashKey);
        
        // Escape
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, this::handleEscape);
        
        // Report and Refresh
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::generateExpenseReport);
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, this::refreshExpenses);
        
        // Add Expense shortcuts
        registerKeyAction("CTRL_N", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, condition, this::openAddExpenseDialog);
        registerKeyAction("ALT_A", KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, condition, this::openAddExpenseDialog);
        
        // Quick filters - Alt+1 = Unpaid, Alt+2 = Paid
        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton1));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton2));
        
        // Help
        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, condition, this::showKeyboardHints);
        
        setupSearchFieldShortcuts();
    }
    
    private void setupSearchFieldShortcuts() {
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        jTextField1.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearSearchAndFilters();
            }
        });
        
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "startNavigation");
        jTextField1.getActionMap().put("startNavigation", new AbstractAction() {
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
        return jTextField1.hasFocus() && 
               keyCode != KeyEvent.VK_ESCAPE && 
               keyCode != KeyEvent.VK_ENTER &&
               modifiers == 0 &&
               keyCode != KeyEvent.VK_SLASH;
    }
    
    private void handleEnterKey() {
        if (currentCardIndex == -1 && !expenseCardsList.isEmpty()) {
            navigateCards(KeyEvent.VK_RIGHT);
        }
    }
    
    private void handleSlashKey() {
        if (!jTextField1.hasFocus()) {
            focusSearch();
        }
    }
    
    private void clearSearchAndFilters() {
        jTextField1.setText("");
        buttonGroup1.clearSelection();
        sortByDays.setSelectedItem("📅 All Time");
        performSearch();
        ExpensePanel.this.requestFocusInWindow();
        showPositionIndicator("🧹 Filters cleared");
    }
    
    private void startNavigationFromSearch() {
        if (!expenseCardsList.isEmpty()) {
            ExpensePanel.this.requestFocusInWindow();
            if (currentCardIndex == -1) {
                currentCardIndex = 0;
                selectCurrentCard();
                scrollToCardSmooth(currentCardIndex);
                updatePositionIndicator();
            }
        }
    }
    
    private void focusSearch() {
        jTextField1.requestFocus();
        jTextField1.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter expenses (Press ↓ to navigate results)");
    }
    
    private void handleEscape() {
        if (currentCardIndex >= 0) {
            deselectCurrentCard();
            showPositionIndicator("📋 Card deselected");
        } else if (!jTextField1.getText().isEmpty() || 
                   jRadioButton1.isSelected() || 
                   jRadioButton2.isSelected() ||
                   !sortByDays.getSelectedItem().equals("📅 All Time")) {
            clearSearchAndFilters();
        }
        this.requestFocusInWindow();
    }
    
    private void refreshExpenses() {
        performSearch();
        showPositionIndicator("✅ Expenses refreshed");
        this.requestFocusInWindow();
    }
    
    private void toggleRadioButton(javax.swing.JRadioButton radioBtn) {
        if (radioBtn.isSelected()) {
            buttonGroup1.clearSelection();
            showPositionIndicator("🔍 Filter removed - Showing all expenses");
        } else {
            radioBtn.setSelected(true);
            showPositionIndicator("🎯 Filter applied: " + radioBtn.getText());
        }
        performSearch();
        this.requestFocusInWindow();
    }
    
    private void openAddExpenseDialog() {
        addNewExpenseBtn.doClick();
        showPositionIndicator("➕ Opening Add New Expense dialog");
    }

    private void navigateCards(int direction) {
        if (expenseCardsList.isEmpty()) {
            showPositionIndicator("📝 No expenses available");
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
        int newIndex = calculateNewIndex(direction, currentCardIndex, expenseCardsList.size());
        
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
        if (currentCardIndex < 0 || currentCardIndex >= expenseCardsList.size()) return;
        
        int row = (currentCardIndex / currentColumns) + 1;
        int col = (currentCardIndex % currentColumns) + 1;
        int totalRows = (int) Math.ceil((double) expenseCardsList.size() / currentColumns);
        
        lk.com.pos.privateclasses.RoundedPanel currentCard = expenseCardsList.get(currentCardIndex);
        Integer statusId = (Integer) currentCard.getClientProperty("statusId");
        
        String actionKey = "D: Mark Paid";
        if (statusId != null && statusId == 1) {
            actionKey = "D: Mark Unpaid";
        }
        
        String text = String.format("📋 Card %d/%d (Row %d/%d, Col %d) | E: Edit | %s", 
            currentCardIndex + 1, expenseCardsList.size(), row, totalRows, col, actionKey);
        
        showPositionIndicator(text);
    }
    
    private void selectCurrentCard() {
        if (currentCardIndex < 0 || currentCardIndex >= expenseCardsList.size()) return;
        
        lk.com.pos.privateclasses.RoundedPanel card = expenseCardsList.get(currentCardIndex);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(Colors.TEAL_PRIMARY, 4, Dimensions.BORDER_RADIUS),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        card.setBackground(card.getBackground().brighter());
        currentFocusedCard = card;
    }
    
    private void deselectCard(int index) {
        if (index < 0 || index >= expenseCardsList.size()) return;
        
        lk.com.pos.privateclasses.RoundedPanel card = expenseCardsList.get(index);
        card.setBorder(BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, 18, 18, 18));
        
        Integer statusId = (Integer) card.getClientProperty("statusId");
        if (statusId != null && statusId == 2) {
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
        if (index < 0 || index >= expenseCardsList.size()) return;
        
        SwingUtilities.invokeLater(() -> {
            try {
                lk.com.pos.privateclasses.RoundedPanel card = expenseCardsList.get(index);
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
    
    private void toggleStatusForSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= expenseCardsList.size()) {
            showPositionIndicator("🎯 Select a card first (use arrow keys)");
            return;
        }
        
        lk.com.pos.privateclasses.RoundedPanel card = expenseCardsList.get(currentCardIndex);
        Integer expenseId = (Integer) card.getClientProperty("expenseId");
        String expenseType = (String) card.getClientProperty("expenseType");
        Integer currentStatusId = (Integer) card.getClientProperty("statusId");
        
        if (expenseId != null && currentStatusId != null) {
            toggleExpenseStatus(expenseId, currentStatusId, expenseType);
        }
    }
    
    private void editSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= expenseCardsList.size()) {
            showPositionIndicator("🎯 Select a card first (use arrow keys)");
            return;
        }
        
        lk.com.pos.privateclasses.RoundedPanel card = expenseCardsList.get(currentCardIndex);
        Integer expenseId = (Integer) card.getClientProperty("expenseId");
        
        if (expenseId != null) {
            editExpense(expenseId);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }

    /**
     * Data class to hold expense information
     */
    private static class ExpenseCardData {
        int expenseId;
        String expenseType, dateTime;
        double amount;
        int statusId;
        String statusName;
    }

    // ============================================================================
    // EVENT LISTENERS
    // ============================================================================
    
    private void setupEventListeners() {
        // Search bar - trigger search on text change
        jTextField1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }
        });

        // Radio buttons - trigger search on selection
        jRadioButton1.addActionListener(e -> performSearch());
        jRadioButton2.addActionListener(e -> performSearch());
        
        // Time filter
        sortByDays.addActionListener(e -> performSearch());
        
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
                SwingUtilities.invokeLater(() -> ExpensePanel.this.requestFocusInWindow());
            }
        });
    }
    
    private void updateLayoutIfNeeded() {
        int scrollPaneWidth = jScrollPane1.getWidth();
        int newColumns = calculateColumns(scrollPaneWidth);
        
        if (newColumns != currentColumns) {
            currentColumns = newColumns;
            performSearch();
        }
    }

    private void radioButtonListener() {
        // Allow deselecting radio buttons by clicking again
        jRadioButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (jRadioButton1.isSelected()) {
                    buttonGroup1.clearSelection();
                    performSearch();
                    evt.consume();
                }
            }
        });

        jRadioButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (jRadioButton2.isSelected()) {
                    buttonGroup1.clearSelection();
                    performSearch();
                    evt.consume();
                }
            }
        });
    }
    
    private void loadExpenses() {
        String searchText = jTextField1.getText().trim();
        boolean unpaidOnly = jRadioButton1.isSelected();
        boolean paidOnly = jRadioButton2.isSelected();
        String timeFilter = (String) sortByDays.getSelectedItem();
        
        loadExpensesAsync(searchText, unpaidOnly, paidOnly, timeFilter);
    }
    
    private void performSearch() {
        loadExpenses();
    }
    
    private void loadExpensesAsync(String searchText, boolean unpaidOnly, boolean paidOnly, String timeFilter) {
        SwingWorker<List<ExpenseCardData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ExpenseCardData> doInBackground() throws Exception {
                return fetchExpensesFromDatabase(searchText, unpaidOnly, paidOnly, timeFilter);
            }
            
            @Override
            protected void done() {
                try {
                    List<ExpenseCardData> expenses = get();
                    displayExpenses(expenses);
                } catch (Exception e) {
                    handleLoadError(e);
                }
            }
        };
        
        worker.execute();
    }
    
    private List<ExpenseCardData> fetchExpensesFromDatabase(String searchText, boolean unpaidOnly, boolean paidOnly, String timeFilter) throws Exception {
        List<ExpenseCardData> expenses = new ArrayList<>();
        ResultSet rs = null;
        
        try {
            String query = buildExpenseQuery(searchText, unpaidOnly, paidOnly, timeFilter);
            rs = MySQL.executeSearch(query);

            while (rs.next()) {
                ExpenseCardData data = new ExpenseCardData();
                data.expenseId = rs.getInt("expenses_id");
                data.expenseType = rs.getString("expenses_type");
                data.dateTime = rs.getString("datetime");
                data.amount = rs.getDouble("amount");
                data.statusId = rs.getInt("e_status_id");
                data.statusName = rs.getString("e_status_name");
                
                expenses.add(data);
            }
            
        } catch (SQLException e) {
            throw new Exception("Database error while fetching expenses: " + e.getMessage(), e);
        }
        
        return expenses;
    }
    
    private String buildExpenseQuery(String searchText, boolean unpaidOnly, boolean paidOnly, String timeFilter) {
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT e.expenses_id, et.expenses_type, e.datetime, e.amount, ");
        query.append("es.e_status_id, es.e_status_name ");
        query.append("FROM expenses e ");
        query.append("JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id ");
        query.append("JOIN e_status es ON e.e_status_id = es.e_status_id ");
        query.append("WHERE 1=1 ");
        
        if (searchText != null && !searchText.isEmpty()) {
            String escapedSearch = searchText.replace("'", "''");
            query.append("AND et.expenses_type LIKE '%").append(escapedSearch).append("%' ");
        }
        
        if (unpaidOnly) {
            query.append("AND es.e_status_id = 2 "); // Assuming 2 = Unpaid
        } else if (paidOnly) {
            query.append("AND es.e_status_id = 1 "); // Assuming 1 = Paid
        }
        
        // Add time filter
        if (timeFilter != null && !timeFilter.equals("📅 All Time")) {
            String dateCondition = getDateCondition(timeFilter);
            if (dateCondition != null) {
                query.append("AND e.datetime >= '").append(dateCondition).append("' ");
            }
        }
        
        query.append("ORDER BY e.expenses_id DESC");
        
        return query.toString();
    }
    
    private String getDateCondition(String timeFilter) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        switch (timeFilter) {
            case "🕐 Today":
                return dateFormat.format(cal.getTime());
                
            case "📆 Last 7 Days":
                cal.add(Calendar.DAY_OF_YEAR, -7);
                return dateFormat.format(cal.getTime());
                
            case "🗓️ Last 30 Days":
                cal.add(Calendar.DAY_OF_YEAR, -30);
                return dateFormat.format(cal.getTime());
                
            case "📊 Last 90 Days":
                cal.add(Calendar.DAY_OF_YEAR, -90);
                return dateFormat.format(cal.getTime());
                
            case "📈 1 Year":
                cal.add(Calendar.YEAR, -1);
                return dateFormat.format(cal.getTime());
                
            case "📉 2 Years":
                cal.add(Calendar.YEAR, -2);
                return dateFormat.format(cal.getTime());
                
            case "💼 5 Years":
                cal.add(Calendar.YEAR, -5);
                return dateFormat.format(cal.getTime());
                
            case "🏢 10 Years":
                cal.add(Calendar.YEAR, -10);
                return dateFormat.format(cal.getTime());
                
            default:
                return null;
        }
    }
    
    private void handleLoadError(Exception e) {
        System.err.println("Error loading expenses: " + e.getMessage());
        e.printStackTrace();
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "❌ Failed to load expenses. Please try again.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    private void displayExpenses(List<ExpenseCardData> expenses) {
        clearExpenseCards();
        
        currentCardIndex = -1;
        currentFocusedCard = null;

        if (expenses.isEmpty()) {
            showEmptyState();
            return;
        }

        currentColumns = calculateColumns(jPanel2.getWidth());
        final JPanel gridPanel = createGridPanel();
        
        for (ExpenseCardData data : expenses) {
            lk.com.pos.privateclasses.RoundedPanel card = createExpenseCard(data);
            gridPanel.add(card);
            expenseCardsList.add(card);
        }

        layoutCardsInPanel(gridPanel);
        setupGridResizeListener(gridPanel);
        
        jPanel2.revalidate();
        jPanel2.repaint();
    }
    
    private void clearExpenseCards() {
        for (lk.com.pos.privateclasses.RoundedPanel card : expenseCardsList) {
            removeAllListeners(card);
        }
        
        expenseCardsList.clear();
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
    
    private void showEmptyState() {
        jPanel2.setLayout(new BorderLayout());
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Colors.BACKGROUND);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(80, 0, 0, 0));
        
        JLabel noExpenses = new JLabel(Strings.NO_EXPENSES);
        noExpenses.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24));
        noExpenses.setForeground(Colors.TEXT_SECONDARY);
        noExpenses.setHorizontalAlignment(SwingConstants.CENTER);
        noExpenses.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel noExpensesDesc = new JLabel(Strings.NO_EXPENSES_DESC);
        noExpensesDesc.setFont(new java.awt.Font("Nunito SemiBold", 0, 16));
        noExpensesDesc.setForeground(Colors.TEXT_MUTED);
        noExpensesDesc.setHorizontalAlignment(SwingConstants.CENTER);
        noExpensesDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        messagePanel.add(noExpenses);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(noExpensesDesc);
        jPanel2.add(messagePanel, BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
    }
    
    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, currentColumns, Dimensions.GRID_GAP, Dimensions.GRID_GAP));
        gridPanel.setBackground(Colors.BACKGROUND);
        return gridPanel;
    }
    
    private void layoutCardsInPanel(JPanel gridPanel) {
        jPanel2.setLayout(new BorderLayout());
        
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Colors.BACKGROUND);
        
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setBackground(Colors.BACKGROUND);
        paddingPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        paddingPanel.add(gridPanel, BorderLayout.NORTH);
        
        mainContainer.add(paddingPanel);
        jPanel2.add(mainContainer, BorderLayout.NORTH);
    }
    
    private void setupGridResizeListener(final JPanel gridPanel) {
        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int panelWidth = jPanel2.getWidth();
                int newColumns = calculateColumns(panelWidth);

                if (newColumns != currentColumns) {
                    currentColumns = newColumns;
                    gridPanel.setLayout(new GridLayout(0, newColumns, Dimensions.GRID_GAP, Dimensions.GRID_GAP));
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }
        });
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

    /**
     * Custom Rounded Border Class
     */
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
            g2d.setStroke(new BasicStroke(thickness));
            
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
    
    private lk.com.pos.privateclasses.RoundedPanel createExpenseCard(ExpenseCardData data) {
        lk.com.pos.privateclasses.RoundedPanel card = createBaseCard(data.expenseId, data.expenseType, data.statusId);
        
        // Store the initial status on the card
        card.putClientProperty("statusId", data.statusId);
        
        JPanel contentPanel = createCardContent(data);
        
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }
    
    private lk.com.pos.privateclasses.RoundedPanel createBaseCard(int expenseId, String expenseType, int statusId) {
        lk.com.pos.privateclasses.RoundedPanel card = new lk.com.pos.privateclasses.RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(Dimensions.CARD_SIZE);
        card.setMaximumSize(Dimensions.CARD_MAX_SIZE);
        card.setMinimumSize(Dimensions.CARD_MIN_SIZE);
        
        if (statusId == 2) {
            card.setBackground(Colors.CARD_INACTIVE);
        } else {
            card.setBackground(Colors.CARD_WHITE);
        }
        
        card.setBorderThickness(0);
        card.setBorder(BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, 18, 18, 18));
        
        card.putClientProperty("expenseId", expenseId);
        card.putClientProperty("expenseType", expenseType);
        card.putClientProperty("statusId", statusId);
        
        addCardMouseListeners(card, statusId);
        
        return card;
    }
    
    private void addCardMouseListeners(lk.com.pos.privateclasses.RoundedPanel card, int statusId) {
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    Color hoverColor = statusId == 2 ? Colors.TEXT_INACTIVE : Colors.TEAL_HOVER;
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(hoverColor, 2, Dimensions.BORDER_RADIUS),
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
    
    private void handleCardClick(lk.com.pos.privateclasses.RoundedPanel card) {
        if (currentFocusedCard != null && currentFocusedCard != card) {
            deselectCurrentCard();
        }
        
        currentCardIndex = expenseCardsList.indexOf(card);
        selectCurrentCard();
        updatePositionIndicator();
        ExpensePanel.this.requestFocusInWindow();
    }
    
    private JPanel createCardContent(ExpenseCardData data) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(contentPanel.getBackground());
        contentPanel.setOpaque(false);

        // Main title section (like "Rent") - color doesn't change for unpaid
        contentPanel.add(createMainTitleSection(data.expenseType));
        contentPanel.add(Box.createVerticalStrut(15));
        
        // EXPENSE DETAILS (left) and Status badge (right) on same line
        contentPanel.add(createDetailsAndStatusSection(data.statusId, data.statusName));
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Date section
        contentPanel.add(createDateSection(data.dateTime, data.statusId));
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Amount display
        contentPanel.add(createAmountDisplay(data.amount, data.statusId));

        return contentPanel;
    }
    
    private JPanel createMainTitleSection(String expenseType) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel titleLabel = new JLabel(expenseType);
        titleLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24));
        titleLabel.setForeground(Colors.TEXT_PRIMARY); // Always same color, doesn't change for unpaid
        titleLabel.setToolTipText(expenseType);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(createActionButtons(0, 0, expenseType), BorderLayout.EAST);

        return titlePanel;
    }
    
    private JPanel createDetailsAndStatusSection(int statusId, String statusName) {
        JPanel detailsStatusPanel = new JPanel(new BorderLayout());
        detailsStatusPanel.setOpaque(false);
        detailsStatusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // EXPENSE DETAILS on the LEFT
        JLabel detailsHeader = new JLabel("EXPENSE DETAILS");
        detailsHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12));
        detailsHeader.setForeground(Colors.TEXT_MUTED);
        
        // Status badge on the RIGHT
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);

        JLabel statusBadge = new JLabel(statusName != null ? statusName.toUpperCase() : "UNPAID");
        statusBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12));
        statusBadge.setOpaque(true);
        statusBadge.setHorizontalAlignment(SwingConstants.CENTER);
        statusBadge.setVerticalAlignment(SwingConstants.CENTER);
        
        // Set fixed size for consistent alignment
        statusBadge.setPreferredSize(new Dimension(80, 28));
        statusBadge.setMinimumSize(new Dimension(80, 28));
        statusBadge.setMaximumSize(new Dimension(80, 28));
        
        boolean isPaid = statusId == 1;
        
        if (isPaid) {
            statusBadge.setForeground(Colors.STATUS_PAID_FG);
            statusBadge.setBackground(Colors.STATUS_PAID_BG);
            statusBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.STATUS_PAID_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
        } else {
            // Light red theme for unpaid status
            statusBadge.setForeground(Colors.STATUS_UNPAID_FG);
            statusBadge.setBackground(Colors.STATUS_UNPAID_BG);
            statusBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.STATUS_UNPAID_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
        }
        
        badgePanel.add(statusBadge);
        
        // Add both to the panel
        detailsStatusPanel.add(detailsHeader, BorderLayout.WEST);  // LEFT side
        detailsStatusPanel.add(badgePanel, BorderLayout.EAST);     // RIGHT side

        return detailsStatusPanel;
    }
    
    private JPanel createDateSection(String dateTime, int statusId) {
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setOpaque(false);
        datePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Date label on the left
        JLabel dateLabel = new JLabel("Date");
        dateLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        dateLabel.setForeground(Colors.TEXT_SECONDARY); // Always same color
        
        // Date value on the right
        String displayDate = formatDate(dateTime);
        JLabel dateValue = new JLabel(displayDate);
        dateValue.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        dateValue.setForeground(Colors.TEXT_PRIMARY); // Always same color
        dateValue.setHorizontalAlignment(SwingConstants.RIGHT);

        datePanel.add(dateLabel, BorderLayout.WEST);
        datePanel.add(dateValue, BorderLayout.EAST);

        return datePanel;
    }
    
    private JPanel createAmountDisplay(double amount, int statusId) {
        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.setOpaque(false);
        amountPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        lk.com.pos.privateclasses.RoundedPanel amountContainer = new lk.com.pos.privateclasses.RoundedPanel();
        if (statusId == 2) {
            amountContainer.setBackgroundColor(Colors.AMOUNT_BG_UNPAID); // Light red for unpaid
        } else {
            amountContainer.setBackgroundColor(Colors.AMOUNT_BG_PAID); // Light blue for paid
        }
        amountContainer.setBorderThickness(0);
        amountContainer.setLayout(new BorderLayout());
        amountContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel amountLabel = new JLabel("Rs." + PRICE_FORMAT.format(amount));
        amountLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
        if (statusId == 2) {
            amountLabel.setForeground(Colors.AMOUNT_UNPAID); // Red for unpaid amount
        } else {
            amountLabel.setForeground(Colors.AMOUNT_PAID); // Teal for paid amount
        }
        amountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        amountContainer.add(amountLabel, BorderLayout.CENTER);
        amountPanel.add(amountContainer, BorderLayout.CENTER);
        
        return amountPanel;
    }
    
    private JButton createEditButton(int expenseId, int statusId, String expenseType) {
        JButton editButton = new JButton();
        editButton.setPreferredSize(Dimensions.ACTION_BUTTON_SIZE);
        editButton.setMinimumSize(Dimensions.ACTION_BUTTON_SIZE);
        editButton.setMaximumSize(Dimensions.ACTION_BUTTON_SIZE);
        editButton.setBackground(Colors.BTN_EDIT_BG);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 16, 16);
            editButton.setIcon(editIcon);
        } catch (Exception e) {
            editButton.setText("✎");
            editButton.setForeground(Colors.BTN_EDIT_FG);
            editButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 12));
        }
        
        editButton.setBorder(BorderFactory.createLineBorder(Colors.BTN_EDIT_BG, 1));
        editButton.setFocusable(false);
        editButton.setToolTipText("Edit Expense (E)");
        
        // Modern hover effect
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                editButton.setBackground(Colors.BTN_EDIT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                editButton.setBackground(Colors.BTN_EDIT_BG);
            }
        });
        
        editButton.addActionListener(e -> {
            editExpense(expenseId);
            ExpensePanel.this.requestFocusInWindow();
        });

        return editButton;
    }
    
    private JButton createStatusToggleButton(int expenseId, int statusId, String expenseType) {
        JButton statusButton = new JButton();
        statusButton.setPreferredSize(Dimensions.ACTION_BUTTON_SIZE);
        statusButton.setMinimumSize(Dimensions.ACTION_BUTTON_SIZE);
        statusButton.setMaximumSize(Dimensions.ACTION_BUTTON_SIZE);
        statusButton.setBackground(Colors.BTN_STATUS_BG);
        statusButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        try {
            FlatSVGIcon activateIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 16, 16);
            statusButton.setIcon(activateIcon);
        } catch (Exception e) {
            statusButton.setText("🔄");
            statusButton.setForeground(Colors.BTN_STATUS_FG);
            statusButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 12));
        }
        
        statusButton.setBorder(BorderFactory.createLineBorder(Colors.BTN_STATUS_BG, 1));
        statusButton.setFocusable(false);
        statusButton.setToolTipText("Toggle Status (D) - Change between Paid/Unpaid");
        
        // Modern hover effect
        statusButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                statusButton.setBackground(Colors.BTN_STATUS_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                statusButton.setBackground(Colors.BTN_STATUS_BG);
            }
        });
        
        statusButton.addActionListener(e -> {
            toggleExpenseStatus(expenseId, statusId, expenseType);
            ExpensePanel.this.requestFocusInWindow();
        });

        return statusButton;
    }
    
    private JButton createDeleteButton(int expenseId, int statusId, String expenseType) {
        JButton deleteButton = new JButton();
        deleteButton.setPreferredSize(Dimensions.ACTION_BUTTON_SIZE);
        deleteButton.setMinimumSize(Dimensions.ACTION_BUTTON_SIZE);
        deleteButton.setMaximumSize(Dimensions.ACTION_BUTTON_SIZE);
        deleteButton.setBackground(Colors.BTN_DELETE_BG);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        try {
            FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 16, 16);
            deleteButton.setIcon(deleteIcon);
        } catch (Exception e) {
            deleteButton.setText("🗑");
            deleteButton.setForeground(Colors.BTN_DELETE_FG);
            deleteButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 12));
        }
        
        deleteButton.setBorder(BorderFactory.createLineBorder(Colors.BTN_DELETE_BG, 1));
        deleteButton.setFocusable(false);
        deleteButton.setToolTipText("Delete Expense");
        
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                deleteButton.setBackground(Colors.BTN_DELETE_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                deleteButton.setBackground(Colors.BTN_DELETE_BG);
            }
        });
        
        deleteButton.addActionListener(e -> {
            deleteExpense(expenseId, expenseType);
            ExpensePanel.this.requestFocusInWindow();
        });

        return deleteButton;
    }
    
    private JPanel createActionButtons(int expenseId, int statusId, String expenseType) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        JButton editButton = createEditButton(expenseId, statusId, expenseType);
        JButton statusButton = createStatusToggleButton(expenseId, statusId, expenseType);
        JButton deleteButton = createDeleteButton(expenseId, statusId, expenseType);
        
        actionPanel.add(editButton);
        actionPanel.add(statusButton);
        actionPanel.add(deleteButton);

        return actionPanel;
    }
    
    private void deleteExpense(int expenseId, String expenseType) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "❓ Are you sure you want to delete this expense?\n📝 Expense: " + expenseType,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String deleteQuery = "DELETE FROM expenses WHERE expenses_id = " + expenseId;
                MySQL.executeIUD(deleteQuery);
                
                showPositionIndicator("🗑️ Expense deleted: " + expenseType);
                performSearch(); // Refresh the list
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "❌ Failed to delete expense: " + e.getMessage(),
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private String formatDate(String dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        
        try {
            Date date = DATE_FORMAT.parse(dateTime);
            return DISPLAY_DATE_FORMAT.format(date);
        } catch (Exception e) {
            return dateTime;
        }
    }
    
    private void toggleExpenseStatus(int expenseId, int currentStatusId, String expenseType) {
        // Toggle: 1 (Paid) <-> 2 (Unpaid)
        int newStatusId = (currentStatusId == 1) ? 2 : 1;
        String newStatusName = (newStatusId == 1) ? Strings.STATUS_PAID : Strings.STATUS_UNPAID;
        String currentStatusName = (currentStatusId == 1) ? Strings.STATUS_PAID : Strings.STATUS_UNPAID;
        
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "🔄 Are you sure you want to mark '" + expenseType + "' as " + newStatusName.toLowerCase() + "?\n" +
                "📊 Current status: " + currentStatusName,
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            String updateQuery = String.format(
                "UPDATE expenses SET e_status_id = %d WHERE expenses_id = %d",
                newStatusId, expenseId
            );
            
            MySQL.executeIUD(updateQuery);
            
            // Refresh the list to show updated status
            performSearch();
            
            showPositionIndicator("✅ Status updated to: " + newStatusName);
            
        } catch (Exception e) {
            System.err.println("Error updating expense status: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "❌ Failed to update status. Please try again.",
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editExpense(int expenseId) {
        // TODO: Implement edit expense dialog
        JOptionPane.showMessageDialog(this,
                "✏️ Edit Expense dialog for ID: " + expenseId + "\nPlease implement your edit dialog here.",
                "Edit Expense",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void generateExpenseReport() {
        // TODO: Implement your actual report generation logic here.
        System.out.println("Expense Report generation triggered.");
        JOptionPane.showMessageDialog(this, 
                "📊 Expense Report generation is not yet implemented.\n"
                + "You can add your report logic in the 'generateExpenseReport()' method.", 
                "Feature Under Development", 
                JOptionPane.INFORMATION_MESSAGE);
        this.requestFocusInWindow();
    }
   
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        addNewExpenseBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel1 = new javax.swing.JLabel();
        editBtn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jButton8 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        roundedPanel3 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        deleteBtn = new javax.swing.JButton();
        expenseReportBtn = new javax.swing.JButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        sortByDays = new javax.swing.JComboBox<>();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        addNewExpenseBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewExpenseBtn.setText("Add New Expense");
        addNewExpenseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewExpenseBtnActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(248, 250, 252));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20)); // NOI18N
        jLabel1.setText("Expense Type");

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jButton8.setBackground(new java.awt.Color(255, 51, 51));
        jButton8.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton8.setText("UnPaid");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(1, 0));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel15.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel15.setText("Date:");

        jLabel20.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 102, 102));
        jLabel20.setText("2025-12-20");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 162, Short.MAX_VALUE)
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel7.add(jPanel9);

        jLabel37.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("Paid Amount");

        jLabel38.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(102, 102, 102));
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setText("Rs.7500");

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        roundedPanel3Layout.setVerticalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel38)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        deleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(roundedPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator2)
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSeparator1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addGap(8, 8, 8)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roundedPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(371, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(232, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        expenseReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        expenseReportBtn.setText("Expense Report");
        expenseReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expenseReportBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton2.setForeground(new java.awt.Color(99, 102, 241));
        jRadioButton2.setText("Paid");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton1.setForeground(new java.awt.Color(255, 51, 51));
        jRadioButton1.setText("Unpaid");

        sortByDays.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        sortByDays.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        sortByDays.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        sortByDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByDaysActionPerformed(evt);
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
                        .addComponent(jTextField1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByDays, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addGap(18, 18, 18)
                        .addComponent(expenseReportBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewExpenseBtn)))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButton2)
                            .addComponent(jRadioButton1))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addNewExpenseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(expenseReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sortByDays, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                .addGap(14, 14, 14))
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
       performSearch();
            }//GEN-LAST:event_jTextField1ActionPerformed

    private void addNewExpenseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewExpenseBtnActionPerformed
       
    }//GEN-LAST:event_addNewExpenseBtnActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void expenseReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expenseReportBtnActionPerformed
      
    }//GEN-LAST:event_expenseReportBtnActionPerformed

    private void sortByDaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByDaysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortByDaysActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewExpenseBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton expenseReportBtn;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private javax.swing.JComboBox<String> sortByDays;
    // End of variables declaration//GEN-END:variables
}
