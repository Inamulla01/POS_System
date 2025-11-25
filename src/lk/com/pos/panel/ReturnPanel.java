package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import lk.com.pos.connection.MySQL;
import lk.com.pos.privateclasses.RoundedPanel;
import com.formdev.flatlaf.extras.FlatSVGIcon;
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
import lk.com.pos.dialog.ExchangeProductDialog;

public class ReturnPanel extends javax.swing.JPanel {

    private JPanel returnsContainer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
    private int currentWidth = 0;
    private Timer searchTimer;

    // Keyboard navigation
    private List<JPanel> returnCardsList = new ArrayList<>();
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
    private static final Color RED_BORDER_SELECTED = new Color(239, 68, 68);
    private static final Color RED_BORDER_HOVER = new Color(248, 113, 113);
    private static final Color DEFAULT_BORDER = new Color(230, 230, 230);

    public ReturnPanel() {
        initComponents();
        setupPanel();
        customizeComponents(); // This now calls setupButtons()
        createPositionIndicator();
        createKeyboardHintsPanel();
        setupKeyboardShortcuts();
        loadReturnData("", "All Time", "All Reasons");
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
                BorderFactory.createLineBorder(RED_BORDER_SELECTED, 2),
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
                BorderFactory.createLineBorder(RED_BORDER_SELECTED, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);

        JLabel title = new JLabel("⌨️ KEYBOARD SHORTCUTS");
        title.setFont(new Font("Nunito ExtraBold", Font.BOLD, 13));
        title.setForeground(RED_BORDER_SELECTED);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);

        keyboardHintsPanel.add(Box.createVerticalStrut(10));

        addHintRow("↑ ↓", "Navigate returns", "#FFFFFF");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("F5", "Refresh", "#34D399");
        addHintRow("Alt+A", "Return Product", "#EF4444");
        addHintRow("Ctrl+N", "Return Product", "#EF4444");
        addHintRow("Ctrl+P", "Return Report", "#10B981");
        addHintRow("Ctrl+R", "Return Report", "#10B981");

        keyboardHintsPanel.add(Box.createVerticalStrut(8));
        JLabel periodTitle = new JLabel("PERIOD FILTERS");
        periodTitle.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
        periodTitle.setForeground(Color.decode("#FB923C"));
        periodTitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(periodTitle);
        keyboardHintsPanel.add(Box.createVerticalStrut(4));

        addHintRow("Alt+1", "All Time", "#FB923C");
        addHintRow("Alt+2", "Today", "#FCD34D");
        addHintRow("Alt+3", "Last 7 Days", "#1CB5BB");
        addHintRow("Alt+4", "Last 30 Days", "#F87171");
        addHintRow("Alt+5", "Last 90 Days", "#A78BFA");
        addHintRow("Alt+6", "1 Year", "#34D399");
        addHintRow("Alt+7", "2 Years", "#60A5FA");
        addHintRow("Alt+8", "5 Years", "#F472B6");
        addHintRow("Alt+9", "10 Years", "#FBBF24");

        keyboardHintsPanel.add(Box.createVerticalStrut(8));
        JLabel reasonTitle = new JLabel("🏷️ REASON FILTERS");
        reasonTitle.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
        reasonTitle.setForeground(Color.decode("#F472B6"));
        reasonTitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(reasonTitle);
        keyboardHintsPanel.add(Box.createVerticalStrut(4));

        addHintRow("Shift+1", "All Reasons", "#9CA3AF");
        addHintRow("Shift+2", "Damaged", "#EF4444");
        addHintRow("Shift+3", "Wrong Item", "#F59E0B");
        addHintRow("Shift+4", "Changed Mind", "#3B82F6");
        addHintRow("Shift+5", "Expired", "#FCD34D");
        addHintRow("Shift+6", "Incorrect Size", "#A78BFA");
        addHintRow("Shift+7", "Malfunction", "#FB923C");
        addHintRow("Shift+8", "Packaging", "#34D399");
        addHintRow("Shift+9", "Defective", "#EF4444");
        addHintRow("Shift+0", "Late Delivery", "#60A5FA");
        addHintRow("Shift+-", "Other", "#F472B6");

        keyboardHintsPanel.add(Box.createVerticalStrut(8));
        addHintRow("Esc", "Clear All Filters", "#EF4444");
        addHintRow("?", "Toggle Help", "#EF4444");

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
        row.setMaximumSize(new Dimension(300, 25));

        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(new Font("Consolas", Font.BOLD, 11));
        keyLabel.setForeground(Color.decode(keyColor));
        keyLabel.setPreferredSize(new Dimension(90, 20));

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
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, () -> refreshReturns());

// Return Product - ALT+A and CTRL+N
        registerKeyAction("ALT_A", KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, condition, this::openReturnProductDialog);
        registerKeyAction("CTRL_N", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, condition, this::openReturnProductDialog);

// Return Report - CTRL+P and CTRL+R
        registerKeyAction("CTRL_P", KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, condition, this::openReturnReport);
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::openReturnReport);

        // Period filters - ALT+1 to ALT+9 (All Time, Today, 7D, 30D, 90D, 1Y, 2Y, 5Y, 10Y)
        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(0));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(1));
        registerKeyAction("ALT_3", KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(2));
        registerKeyAction("ALT_4", KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(3));
        registerKeyAction("ALT_5", KeyEvent.VK_5, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(4));
        registerKeyAction("ALT_6", KeyEvent.VK_6, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(5));
        registerKeyAction("ALT_7", KeyEvent.VK_7, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(6));
        registerKeyAction("ALT_8", KeyEvent.VK_8, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(7));
        registerKeyAction("ALT_9", KeyEvent.VK_9, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(8));

        // Reason filters - ALL 11 REASONS
        registerKeyAction("SHIFT_1", KeyEvent.VK_1, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(0));
        registerKeyAction("SHIFT_2", KeyEvent.VK_2, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(1));
        registerKeyAction("SHIFT_3", KeyEvent.VK_3, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(2));
        registerKeyAction("SHIFT_4", KeyEvent.VK_4, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(3));
        registerKeyAction("SHIFT_5", KeyEvent.VK_5, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(4));
        registerKeyAction("SHIFT_6", KeyEvent.VK_6, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(5));
        registerKeyAction("SHIFT_7", KeyEvent.VK_7, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(6));
        registerKeyAction("SHIFT_8", KeyEvent.VK_8, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(7));
        registerKeyAction("SHIFT_9", KeyEvent.VK_9, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(8));
        registerKeyAction("SHIFT_0", KeyEvent.VK_0, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(9));
        registerKeyAction("SHIFT_MINUS", KeyEvent.VK_MINUS, KeyEvent.SHIFT_DOWN_MASK, condition, () -> setReason(10));

        // Escape - Clear all filters
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
                if (!returnCardsList.isEmpty()) {
                    ReturnPanel.this.requestFocusInWindow();
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
                SwingUtilities.invokeLater(() -> ReturnPanel.this.requestFocusInWindow());
            }
        });
    }

    private void registerKeyAction(String actionName, int keyCode, int modifiers, int condition, Runnable action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        this.getInputMap(condition).put(keyStroke, actionName);
        this.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextField1.hasFocus()
                        && keyCode != KeyEvent.VK_ESCAPE
                        && modifiers == 0
                        && keyCode != KeyEvent.VK_SLASH) {
                    return;
                }
                action.run();
            }
        });
    }

    private void navigateCards(int direction) {
        if (returnCardsList.isEmpty()) {
            showPositionIndicator("No returns available");
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
            showPositionIndicator("Already at the first return");
            return;
        }

        if (newIndex >= returnCardsList.size()) {
            showPositionIndicator("Already at the last return");
            return;
        }

        deselectCard(currentCardIndex);
        currentCardIndex = newIndex;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
    }

    private void navigateToFirst() {
        if (returnCardsList.isEmpty()) {
            showPositionIndicator("No returns available");
            return;
        }

        if (currentCardIndex >= 0) {
            deselectCard(currentCardIndex);
        }
        currentCardIndex = 0;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
        showPositionIndicator("⬆️ First return");
    }

    private void navigateToLast() {
        if (returnCardsList.isEmpty()) {
            showPositionIndicator("No returns available");
            return;
        }

        if (currentCardIndex >= 0) {
            deselectCard(currentCardIndex);
        }
        currentCardIndex = returnCardsList.size() - 1;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
        showPositionIndicator("Last return");
    }

    private void selectCurrentCard() {
        if (currentCardIndex >= 0 && currentCardIndex < returnCardsList.size()) {
            JPanel card = returnCardsList.get(currentCardIndex);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(RED_BORDER_SELECTED, 3, 20),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            currentFocusedCard = card;

            String invoiceNo = (String) card.getClientProperty("invoiceNo");
            showPositionIndicator(String.format("Return %d/%d: %s",
                    currentCardIndex + 1, returnCardsList.size(), invoiceNo));
        }
    }

    private void deselectCard(int index) {
        if (index >= 0 && index < returnCardsList.size()) {
            JPanel card = returnCardsList.get(index);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new ShadowBorder(),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        }
    }

    private void scrollToCard(int index) {
        if (index < 0 || index >= returnCardsList.size()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                JPanel card = returnCardsList.get(index);
                Rectangle bounds = card.getBounds();
                Rectangle visible = jScrollPane1.getViewport().getViewRect();

                int targetY = bounds.y - 20;

                jScrollPane1.getViewport().setViewPosition(new Point(0, Math.max(0, targetY)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void focusSearch() {
        jTextField1.requestFocus();
        jTextField1.selectAll();
        showPositionIndicator("Search mode - Type to filter (Press ↓ to navigate)");
    }

    private void handleEscape() {
        if (currentCardIndex >= 0) {
            deselectCard(currentCardIndex);
            currentFocusedCard = null;
            currentCardIndex = -1;
            showPositionIndicator("Card deselected");
        } else if (!jTextField1.getText().isEmpty()
                || sortByDays.getSelectedIndex() != 0
                || sortByReason.getSelectedIndex() != 0) {
            clearAllFilters();
        }
        this.requestFocusInWindow();
    }

    private void clearAllFilters() {
        boolean wasFiltered = !jTextField1.getText().isEmpty()
                || sortByDays.getSelectedIndex() != 0
                || sortByReason.getSelectedIndex() != 0;

        jTextField1.setText("");
        sortByDays.setSelectedIndex(0);
        sortByReason.setSelectedIndex(0);

        if (wasFiltered) {
            handleFilter();
            showPositionIndicator("All filters cleared - Showing all returns");
        }

        ReturnPanel.this.requestFocusInWindow();
    }

    private void refreshReturns() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < REFRESH_COOLDOWN) {
            showPositionIndicator("Please wait before refreshing again");
            return;
        }

        lastRefreshTime = currentTime;
        handleFilter();
        showPositionIndicator("Return data refreshed");
        this.requestFocusInWindow();
    }

    private void openReturnProductDialog() {
        addProductDialog.doClick();
        showPositionIndicator("➕ Opening Return Product Dialog");
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    private void openReturnReport() {
        returnReportDialogBtn.doClick();
        showPositionIndicator("📊 Opening Return Report");
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    private void setPeriod(int index) {
        if (index >= 0 && index < sortByDays.getItemCount()) {
            sortByDays.setSelectedIndex(index);
            handleFilter();
            showPositionIndicator("Filter: " + sortByDays.getItemAt(index));
            this.requestFocusInWindow();
        }
    }

    private void setReason(int index) {
        if (index >= 0 && index < sortByReason.getItemCount()) {
            sortByReason.setSelectedIndex(index);
            handleFilter();
            showPositionIndicator("🏷️ Filter: " + sortByReason.getItemAt(index));
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
            handleFilter();
            this.requestFocusInWindow();
        });

        sortByReason.addActionListener(e -> {
            handleFilter();
            this.requestFocusInWindow();
        });
    }

    private void handleSearch() {
        String searchText = jTextField1.getText().trim();
        String selectedPeriod = sortByDays.getSelectedItem().toString();
        String selectedReason = sortByReason.getSelectedItem().toString();
        loadReturnData(searchText, selectedPeriod, selectedReason);
    }

    private void handleFilter() {
        String searchText = jTextField1.getText().trim();
        String selectedPeriod = sortByDays.getSelectedItem().toString();
        String selectedReason = sortByReason.getSelectedItem().toString();
        loadReturnData(searchText, selectedPeriod, selectedReason);
    }

    private void setupPanel() {
        jPanel2.setLayout(new BorderLayout());
        jPanel2.setBackground(new Color(248, 250, 252));
    }

    private void adjustLayoutForWidth(int width) {
        if (width < 900) {
            jTextField1.setPreferredSize(new Dimension(width - 40, 50));
            sortByDays.setPreferredSize(new Dimension((width - 56) / 2, 50));
            sortByReason.setPreferredSize(new Dimension((width - 56) / 2, 50));
        } else {
            int thirdWidth = (width - 72) / 3;
            jTextField1.setPreferredSize(new Dimension(thirdWidth, 50));
            sortByDays.setPreferredSize(new Dimension(thirdWidth, 50));
            sortByReason.setPreferredSize(new Dimension(thirdWidth, 50));
        }
        revalidate();
        repaint();
    }

    private void setupButtons() {
        setupAddReturnButton();
        setupReturnReportButton();

        // Remove text from buttons
        addProductDialog.setText("");
        returnReportDialogBtn.setText("");
    }

    private void setupAddReturnButton() {
        addProductDialog.setPreferredSize(new Dimension(47, 47));
        addProductDialog.setMinimumSize(new Dimension(47, 47));
        addProductDialog.setMaximumSize(new Dimension(47, 47));

        // Set initial state - transparent background with border
        addProductDialog.setBackground(new Color(0, 0, 0, 0)); // Transparent
        addProductDialog.setForeground(RED_BORDER_SELECTED);

        // Remove text
        addProductDialog.setText("");

        // Set border with red color (matching ReturnPanel theme)
        addProductDialog.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RED_BORDER_SELECTED, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set cursor
        addProductDialog.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remove focus painting
        addProductDialog.setFocusPainted(false);

        // Set icon with red color
        try {
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
            // Apply red color filter to the icon to match ReturnPanel theme
            addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> RED_BORDER_SELECTED));
            addProductDialog.setIcon(addIcon);
        } catch (Exception e) {
            System.err.println("Error loading add icon: " + e.getMessage());
        }

        // Set tooltip
        addProductDialog.setToolTipText("Add Return Product");

        // Add hover effects
        addProductDialog.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addProductDialog.setBackground(RED_BORDER_SELECTED);
                addProductDialog.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(RED_BORDER_HOVER, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon to white on hover
                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    addProductDialog.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                // Update tooltip to show it's clickable
                addProductDialog.setToolTipText("Add Return Product (Alt+A or Ctrl+N)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addProductDialog.setBackground(new Color(0, 0, 0, 0)); // Transparent
                addProductDialog.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(RED_BORDER_SELECTED, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon back to red
                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> RED_BORDER_SELECTED));
                    addProductDialog.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                // Reset tooltip
                addProductDialog.setToolTipText("Add Return Product");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                addProductDialog.setBackground(RED_BORDER_SELECTED.darker());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addProductDialog.setBackground(RED_BORDER_SELECTED);
            }
        });
    }

    private void setupReturnReportButton() {
        returnReportDialogBtn.setPreferredSize(new Dimension(47, 47));
        returnReportDialogBtn.setMinimumSize(new Dimension(47, 47));
        returnReportDialogBtn.setMaximumSize(new Dimension(47, 47));

        // Set initial state - transparent background with border
        returnReportDialogBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
        returnReportDialogBtn.setForeground(Color.decode("#10B981"));

        // Remove text
        returnReportDialogBtn.setText("");

        // Set border with green color
        returnReportDialogBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set cursor
        returnReportDialogBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remove focus painting
        returnReportDialogBtn.setFocusPainted(false);

        // Set icon with green color
        try {
            FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
            // Apply green color filter to the icon
            printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#10B981")));
            returnReportDialogBtn.setIcon(printIcon);
        } catch (Exception e) {
            System.err.println("Error loading print icon: " + e.getMessage());
        }

        // Set tooltip
        returnReportDialogBtn.setToolTipText("Export Return Report");

        // Add hover effects
        returnReportDialogBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                returnReportDialogBtn.setBackground(Color.decode("#10B981"));
                returnReportDialogBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#34D399"), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon to white on hover
                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    returnReportDialogBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                // Update tooltip to show it's clickable
                returnReportDialogBtn.setToolTipText("Export Return Report (Ctrl+P or Ctrl+R)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                returnReportDialogBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
                returnReportDialogBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon back to green
                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#10B981")));
                    returnReportDialogBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                // Reset tooltip
                returnReportDialogBtn.setToolTipText("Export Return Report");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                returnReportDialogBtn.setBackground(Color.decode("#059669"));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                returnReportDialogBtn.setBackground(Color.decode("#10B981"));
            }
        });
    }

    private void customizeComponents() {
        // Enhanced search field with FlatLaf styling
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by invoice number...");
        jTextField1.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        jTextField1.setToolTipText("Search returns (Ctrl+F or /) - Press ESC to clear all filters");
        jTextField1.setForeground(Color.GRAY);

        // Enhanced combo boxes with FlatLaf styling - UPDATED WITH NEW PERIODS
        sortByDays.setForeground(Color.GRAY);
        sortByDays.setModel(new DefaultComboBoxModel<>(new String[]{
            "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 90 Days",
            "1 Year", "2 Years", "5 Years", "10 Years"
        }));
        sortByDays.setToolTipText("Filter by period (Alt+1 to Alt+9) - Press ESC to reset");

        sortByReason.setForeground(Color.GRAY);
        sortByReason.setModel(new DefaultComboBoxModel<>(new String[]{
            "All Reasons", "Damaged product", "Wrong item delivered",
            "Customer changed mind", "Expired product", "Incorrect size",
            "Product malfunction", "Packaging issue", "Defective item",
            "Late delivery", "Other"
        }));
        sortByReason.setToolTipText("Filter by reason (Shift+1 to Shift+0, Shift+-) - Press ESC to reset");

        // Setup buttons with new styling
        setupButtons();

        roundedPanel1.setVisible(false);

        // Enhanced scroll pane
        jScrollPane1.setBorder(null);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(20);
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #EF4444;"
                + "width: 8");
    }

    private void clearReturnCards() {
        for (JPanel card : returnCardsList) {
            for (MouseListener ml : card.getMouseListeners()) {
                card.removeMouseListener(ml);
            }
        }
        returnCardsList.clear();
        currentCardIndex = -1;
        currentFocusedCard = null;
    }

    private void loadReturnData(String searchText, String period, String reason) {
        period = period.replace("", "").replace("", "")
                .replace("", "").replace("", "").replace("", "");
        reason = reason.replace("️", "").replace("", "").replace("", "")
                .replace("", "").replace("", "").replace("", "")
                .replace("", "").replace("", "").replace("", "")
                .replace("", "").replace("️", "");

        clearReturnCards();

        String finalPeriod = period;
        String finalReason = reason;
        String finalSearchText = searchText;

        SwingWorker<List<ReturnData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ReturnData> doInBackground() throws Exception {
                return fetchReturnsFromDatabase(finalSearchText, finalPeriod, finalReason);
            }

            @Override
            protected void done() {
                try {
                    List<ReturnData> returns = get();
                    displayReturns(returns);
                } catch (Exception e) {
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

    private static class ReturnData {

        int returnId;
        String invoiceNo;
        String returnDate;
        double returnAmount;
        double discountPrice;
        double originalTotal;
        String returnReason;
        String statusName;
        String processedBy;
        String paymentMethod;
        String customerName;
    }

    private List<ReturnData> fetchReturnsFromDatabase(String searchText, String period, String reason) throws Exception {
        List<ReturnData> returns = new ArrayList<>();

        try {
            String baseQuery = "SELECT "
                    + "r.return_id, r.return_date, r.total_return_amount, r.total_discount_price, "
                    + "s.invoice_no, s.total as original_total, "
                    + "rr.reason as return_reason, "
                    + "ps.p_status as status_name, "
                    + "u.name as processed_by, "
                    + "pm.payment_method_name, "
                    + "COALESCE(cc.customer_name, 'Walk-in Customer') as customer_name "
                    + "FROM `return` r "
                    + "INNER JOIN sales s ON r.sales_id = s.sales_id "
                    + "INNER JOIN return_reason rr ON r.return_reason_id = rr.return_reason_id "
                    + "INNER JOIN p_status ps ON r.status_id = ps.p_status_id "
                    + "INNER JOIN user u ON r.user_id = u.user_id "
                    + "INNER JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                    + "LEFT JOIN credit c ON s.sales_id = c.sales_id "
                    + "LEFT JOIN credit_customer cc ON c.credit_customer_id = cc.customer_id ";

            StringBuilder whereClause = new StringBuilder();
            List<Object> parameters = new ArrayList<>();

            if (!searchText.isEmpty()) {
                String escapedSearch = searchText.replace("'", "''")
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");
                whereClause.append("WHERE s.invoice_no LIKE '%").append(escapedSearch).append("%' ");
            }

            String dateFilter = getDateFilter(period);
            if (!dateFilter.isEmpty()) {
                if (whereClause.length() == 0) {
                    whereClause.append("WHERE ").append(dateFilter);
                } else {
                    whereClause.append("AND ").append(dateFilter);
                }
            }

            if (!reason.equals("All Reasons")) {
                if (whereClause.length() == 0) {
                    whereClause.append("WHERE rr.reason LIKE ? ");
                } else {
                    whereClause.append("AND rr.reason LIKE ? ");
                }
                parameters.add("%" + reason + "%");
            }

            String orderBy = " ORDER BY r.return_date DESC";
            String finalQuery = baseQuery + whereClause.toString() + orderBy;

            PreparedStatement pst = MySQL.getConnection().prepareStatement(finalQuery);
            for (int i = 0; i < parameters.size(); i++) {
                pst.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ReturnData data = new ReturnData();
                data.returnId = rs.getInt("return_id");
                data.invoiceNo = rs.getString("invoice_no");
                data.returnDate = rs.getString("return_date");
                data.returnAmount = rs.getDouble("total_return_amount");
                data.discountPrice = rs.getDouble("total_discount_price");
                data.originalTotal = rs.getDouble("original_total");
                data.returnReason = rs.getString("return_reason");
                data.statusName = rs.getString("status_name");
                data.processedBy = rs.getString("processed_by");
                data.paymentMethod = rs.getString("payment_method_name");
                data.customerName = rs.getString("customer_name");

                returns.add(data);
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Database error: " + e.getMessage());
        }

        return returns;
    }

    private void displayReturns(List<ReturnData> returns) {
        returnsContainer = new JPanel();
        returnsContainer.setLayout(new BoxLayout(returnsContainer, BoxLayout.Y_AXIS));
        returnsContainer.setBackground(new Color(248, 250, 252));
        returnsContainer.setOpaque(false);
        returnsContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        clearReturnCards();

        if (returns.isEmpty()) {
            returnsContainer.add(createNoDataPanel());
        } else {
            for (ReturnData data : returns) {
                JPanel returnCard = createReturnCard(
                        data.returnId, data.invoiceNo, data.returnDate,
                        data.returnAmount, data.discountPrice, data.originalTotal,
                        data.returnReason, data.statusName, data.processedBy,
                        data.paymentMethod, data.customerName
                );
                returnCard.putClientProperty("invoiceNo", data.invoiceNo);
                returnsContainer.add(returnCard);
                returnsContainer.add(Box.createRigidArea(new Dimension(0, 16)));
                returnCardsList.add(returnCard);
            }
        }

        jPanel2.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(248, 250, 252));
        wrapperPanel.add(returnsContainer, BorderLayout.NORTH);
        jPanel2.add(wrapperPanel, BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
    }

    private JPanel createNoDataPanel() {
        JPanel noDataPanel = new JPanel();
        noDataPanel.setLayout(new BoxLayout(noDataPanel, BoxLayout.Y_AXIS));
        noDataPanel.setBackground(new Color(248, 250, 252));
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        JLabel iconLabel = new JLabel("");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(iconLabel);
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel noDataLabel = new JLabel("No return records found");
        noDataLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        noDataLabel.setForeground(new Color(71, 85, 105));
        noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(noDataLabel);

        JLabel hintLabel = new JLabel("Try adjusting your search or filters to see more results");
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

        JLabel errorDetails = new JLabel("<html><div style='text-align: center;'>"
                + "We couldn't load the return data.<br>"
                + "Please check your connection and try again.</div></html>");
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
        retryButton.addActionListener(ev -> loadReturnData("", "All Time", "All Reasons"));
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

    private String getDateFilter(String period) {
        switch (period) {
            case "Today":
                return "DATE(r.return_date) = CURDATE()";
            case "Last 7 Days":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            case "Last 30 Days":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
            case "Last 90 Days":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 90 DAY)";
            case "1 Year":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 1 YEAR)";
            case "2 Years":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 2 YEAR)";
            case "5 Years":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 5 YEAR)";
            case "10 Years":
                return "r.return_date >= DATE_SUB(NOW(), INTERVAL 10 YEAR)";
            default:
                return "";
        }
    }

    private JPanel createReturnCard(int returnId, String invoiceNo, String returnDate,
            double returnAmount, double discountPrice, double originalTotal,
            String returnReason, String statusName, String processedBy,
            String paymentMethod, String customerName) {
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

        JPanel headerPanel = createReturnHeader(invoiceNo, customerName, paymentMethod, returnAmount, statusName);
        JPanel itemsPanel = createReturnItemsPanel(returnId);
        JPanel footerPanel = createReturnFooter(returnDate, returnReason, discountPrice, processedBy);

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
                                ((Timer) evt.getSource()).stop();
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
                if (hoverTimer != null) {
                    hoverTimer.stop();
                }
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
                    int oldIndex = returnCardsList.indexOf(currentFocusedCard);
                    if (oldIndex >= 0) {
                        deselectCard(oldIndex);
                    }
                }

                currentCardIndex = returnCardsList.indexOf(cardPanel);
                selectCurrentCard();
                ReturnPanel.this.requestFocusInWindow();
            }
        });

        return cardPanel;
    }

    private JPanel createReturnHeader(String invoiceNo, String customerName, String paymentMethod,
            double returnAmount, String statusName) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        JLabel iconLabel = createReturnIcon();

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel invoiceLabel = new JLabel("Return #" + (invoiceNo != null ? invoiceNo.toUpperCase() : ""));
        invoiceLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 22));
        invoiceLabel.setForeground(new Color(30, 41, 59));
        invoiceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel customerLabel = new JLabel(customerName != null ? customerName : "Walk-in Customer");
        customerLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 15));
        customerLabel.setForeground(new Color(100, 116, 139));
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(invoiceLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        textPanel.add(customerLabel);

        leftPanel.add(iconLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(12, 0)));
        leftPanel.add(textPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel totalLabel = new JLabel(String.format("Rs.%.2f", returnAmount));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 24));
        totalLabel.setForeground(new Color(239, 68, 68));
        totalLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(totalLabel);

        headerPanel.add(leftPanel);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(rightPanel);

        return headerPanel;
    }

    private JLabel createReturnIcon() {
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(40, 40));
        iconLabel.setMinimumSize(new Dimension(40, 40));
        iconLabel.setMaximumSize(new Dimension(40, 40));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        try {
            FlatSVGIcon icon = new FlatSVGIcon("lk/com/pos/icon/exchange.svg", 28, 28);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.RED));
            iconLabel.setIcon(icon);
        } catch (Exception e) {
            iconLabel.setText("");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        return iconLabel;
    }

    private JPanel createReturnItemsPanel(int returnId) {
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BorderLayout());
        itemsPanel.setOpaque(false);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel itemsHeader = new JLabel("RETURNED ITEMS");
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

        loadReturnItems(itemsContainer, returnId);

        itemsPanel.add(headerPanel, BorderLayout.NORTH);
        itemsPanel.add(itemsContainer, BorderLayout.CENTER);

        return itemsPanel;
    }

    private void loadReturnItems(JPanel itemsListPanel, int returnId) {
        try {
            String query = "SELECT "
                    + "ri.return_qty, ri.unit_return_price, ri.discount_price, ri.total_return_amount, "
                    + "p.product_name, "
                    + "st.batch_no "
                    + "FROM return_item ri "
                    + "INNER JOIN stock st ON ri.stock_id = st.stock_id "
                    + "INNER JOIN product p ON st.product_id = p.product_id "
                    + "WHERE ri.return_id = ? "
                    + "ORDER BY ri.return_item_id";

            PreparedStatement pst = MySQL.getConnection().prepareStatement(query);
            pst.setInt(1, returnId);
            ResultSet rs = pst.executeQuery();

            List<ReturnItemData> items = new ArrayList<>();

            while (rs.next()) {
                String productName = rs.getString("product_name");
                String qty = rs.getString("return_qty");
                double price = rs.getDouble("unit_return_price");
                double discountPrice = rs.getDouble("discount_price");
                double itemTotal = rs.getDouble("total_return_amount");
                String batchNo = rs.getString("batch_no");

                items.add(new ReturnItemData(productName, qty, price, discountPrice, itemTotal, batchNo));
            }

            rs.close();
            pst.close();

            if (items.isEmpty()) {
                JLabel noItemsLabel = new JLabel("No items in this return");
                noItemsLabel.setFont(new Font("Nunito SemiBold", Font.ITALIC, 13));
                noItemsLabel.setForeground(new Color(148, 163, 184));
                noItemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemsListPanel.add(noItemsLabel);
            } else {
                for (int i = 0; i < items.size(); i++) {
                    ReturnItemData item = items.get(i);
                    JPanel itemCard = createReturnItemCard(item.productName, item.qty, item.price,
                            item.discountPrice, item.total, item.batchNo);
                    itemsListPanel.add(itemCard);

                    if (i < items.size() - 1) {
                        JSeparator separator = new JSeparator();
                        separator.setForeground(new Color(229, 231, 235));
                        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                        separator.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
                        itemsListPanel.add(separator);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading items");
            errorLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
            errorLabel.setForeground(new Color(220, 38, 38));
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemsListPanel.add(errorLabel);
        }
    }

    private JPanel createReturnItemCard(String productName, String qty, double price,
            double discountPrice, double total, String batchNo) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout(10, 0));
        itemPanel.setOpaque(false);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel productLabel = new JLabel(productName != null ? productName : "Unknown Product");
        productLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        productLabel.setForeground(new Color(30, 41, 59));
        productLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.X_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceQtyLabel = new JLabel(String.format("Rs.%.2f × %s", price, qty));
        priceQtyLabel.setFont(new Font("Nunito", Font.PLAIN, 12));
        priceQtyLabel.setForeground(new Color(100, 116, 139));

        detailsPanel.add(priceQtyLabel);

        if (discountPrice > 0) {
            JLabel discountInfo = new JLabel(String.format(" • Rs.%.2f discount", discountPrice));
            discountInfo.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
            discountInfo.setForeground(new Color(220, 38, 38));
            detailsPanel.add(discountInfo);
        }

        JPanel extraInfoPanel = new JPanel();
        extraInfoPanel.setLayout(new BoxLayout(extraInfoPanel, BoxLayout.X_AXIS));
        extraInfoPanel.setOpaque(false);
        extraInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (batchNo != null && !batchNo.isEmpty()) {
            JLabel batchLabel = new JLabel("Batch: " + batchNo);
            batchLabel.setFont(new Font("Nunito", Font.PLAIN, 11));
            batchLabel.setForeground(new Color(148, 163, 184));
            extraInfoPanel.add(batchLabel);
        }

        leftPanel.add(productLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftPanel.add(detailsPanel);

        if (extraInfoPanel.getComponentCount() > 0) {
            leftPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            leftPanel.add(extraInfoPanel);
        }

        JLabel totalLabel = new JLabel(String.format("Rs.%.2f", total));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 15));
        totalLabel.setForeground(new Color(239, 68, 68));

        itemPanel.add(leftPanel, BorderLayout.CENTER);
        itemPanel.add(totalLabel, BorderLayout.EAST);

        return itemPanel;
    }

    private JPanel createReturnFooter(String returnDate, String returnReason,
            double discountPrice, String processedBy) {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Return reason panel
        RoundedPanel reasonPanel = new RoundedPanel();
        reasonPanel.setLayout(new BorderLayout(12, 0));
        reasonPanel.setBackgroundColor(new Color(254, 249, 242));
        reasonPanel.setCornerRadius(12);
        reasonPanel.setBorderThickness(0);
        reasonPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        reasonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel reasonIcon = new JLabel(getReasonEmoji(returnReason));
        reasonIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        reasonIcon.setVerticalAlignment(SwingConstants.TOP);

        JPanel reasonTextPanel = new JPanel();
        reasonTextPanel.setLayout(new BoxLayout(reasonTextPanel, BoxLayout.Y_AXIS));
        reasonTextPanel.setOpaque(false);

        JLabel reasonLabel = new JLabel("RETURN REASON");
        reasonLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 10));
        reasonLabel.setForeground(new Color(146, 64, 14));
        reasonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel reasonValue = new JLabel(returnReason != null ? returnReason : "Not specified");
        reasonValue.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        reasonValue.setForeground(new Color(71, 85, 105));
        reasonValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        reasonTextPanel.add(reasonLabel);
        reasonTextPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        reasonTextPanel.add(reasonValue);

        reasonPanel.add(reasonIcon, BorderLayout.WEST);
        reasonPanel.add(reasonTextPanel, BorderLayout.CENTER);

        footerPanel.add(reasonPanel);
        footerPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.X_AXIS));
        datePanel.setOpaque(false);
        datePanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel dateIcon = new JLabel("📅");
        dateIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        dateIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JLabel dateLabel = new JLabel(formatDateTime(returnDate));
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(100, 116, 139));

        datePanel.add(dateIcon);
        datePanel.add(dateLabel);

        JPanel processedByPanel = new JPanel();
        processedByPanel.setLayout(new BoxLayout(processedByPanel, BoxLayout.X_AXIS));
        processedByPanel.setOpaque(false);
        processedByPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JLabel processedText = new JLabel("Processed by: ");
        processedText.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        processedText.setForeground(new Color(100, 116, 139));

        JLabel processedValue = new JLabel(processedBy != null ? processedBy : "Unknown");
        processedValue.setFont(new Font("Nunito ExtraBold", Font.BOLD, 13));
        processedValue.setForeground(new Color(30, 41, 59));

        processedByPanel.add(userIcon);
        processedByPanel.add(processedText);
        processedByPanel.add(processedValue);

        bottomPanel.add(datePanel);
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(processedByPanel);

        footerPanel.add(bottomPanel);

        return footerPanel;
    }

    private String getReasonEmoji(String reason) {
        if (reason == null) {
            return "️";
        }
        String r = reason.toLowerCase();
        if (r.contains("damaged")) {
            return "️";
        }
        if (r.contains("wrong")) {
            return "";
        }
        if (r.contains("changed mind")) {
            return "";
        }
        if (r.contains("expired")) {
            return "";
        }
        if (r.contains("size")) {
            return "";
        }
        if (r.contains("malfunction")) {
            return "";
        }
        if (r.contains("packaging")) {
            return "";
        }
        if (r.contains("defective")) {
            return "";
        }
        if (r.contains("late") || r.contains("delivery")) {
            return "";
        }
        return "ℹ️";
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

    // Helper class
    private static class ReturnItemData {

        String productName;
        String qty;
        double price;
        double discountPrice;
        double total;
        String batchNo;

        ReturnItemData(String productName, String qty, double price, double discountPrice,
                double total, String batchNo) {
            this.productName = productName;
            this.qty = qty;
            this.price = price;
            this.discountPrice = discountPrice;
            this.total = total;
            this.batchNo = batchNo;
        }
    }

    // Custom border classes
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
            g2d.drawRoundRect(x + thickness / 2, y + thickness / 2,
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
                g2d.drawRoundRect(x + i, y + i, width - 2 * i - 1, height - 2 * i - 1, 20, 20);
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
        sortByReason = new javax.swing.JComboBox<>();
        addProductDialog = new javax.swing.JButton();
        returnReportDialogBtn = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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

        sortByReason.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        sortByReason.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        sortByReason.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        sortByReason.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByReasonActionPerformed(evt);
            }
        });

        addProductDialog.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addProductDialog.setText("Add Return Product");
        addProductDialog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProductDialogActionPerformed(evt);
            }
        });

        returnReportDialogBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        returnReportDialogBtn.setText("Return Report");
        returnReportDialogBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnReportDialogBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByDays, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByReason, 0, 0, Short.MAX_VALUE)
                        .addGap(226, 226, 226)
                        .addComponent(returnReportDialogBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addProductDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sortByReason, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addProductDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1)
                    .addComponent(sortByDays)
                    .addComponent(returnReportDialogBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                .addGap(17, 17, 17))
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

    private void sortByDaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByDaysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortByDaysActionPerformed

    private void sortByReasonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByReasonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortByReasonActionPerformed

    private void addProductDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProductDialogActionPerformed
        ExchangeProductDialog exchangeProductProductDialog = new ExchangeProductDialog(null, true);
        exchangeProductProductDialog.setLocationRelativeTo(null);
        exchangeProductProductDialog.setVisible(true);
        handleFilter();

    }//GEN-LAST:event_addProductDialogActionPerformed

    private void returnReportDialogBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnReportDialogBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_returnReportDialogBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addProductDialog;
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
    private javax.swing.JButton returnReportDialogBtn;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private javax.swing.JComboBox<String> sortByDays;
    private javax.swing.JComboBox<String> sortByReason;
    private javax.swing.JLabel total;
    // End of variables declaration//GEN-END:variables
}
