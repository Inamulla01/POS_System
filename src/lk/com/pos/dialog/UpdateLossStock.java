package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import lk.com.pos.session.Session;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import raven.toast.Notifications;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 *
 * @author moham
 */
public class UpdateLossStock extends javax.swing.JDialog {

    private int stockLossId;
    private int originalStockId;
    private int originalQty;
    private int originalReasonId;

    /**
     * Creates new form UpdateLossStock
     */
    public UpdateLossStock(java.awt.Frame parent, boolean modal, int stockLossId) {
        super(parent, modal);
        this.stockLossId = stockLossId;
        initComponents();
        initializeDialog();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());

        // Load data first
        loadStocks();
        loadLossReasons();

        // Then load existing stock loss data
        loadStockLossData();

        // Setup keyboard navigation and styling
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();
        setupFocusTraversal();

        // Set initial focus
        stockCombo.requestFocusInWindow();
    }

    // ---------------- LOAD EXISTING STOCK LOSS DATA ----------------
    private void loadStockLossData() {
        try {
            String query = "SELECT sl.qty, sl.stock_id, sl.return_reason_id, "
                    + "s.batch_no, p.product_name, rr.reason "
                    + "FROM stock_loss sl "
                    + "JOIN stock s ON sl.stock_id = s.stock_id "
                    + "JOIN product p ON s.product_id = p.product_id "
                    + "JOIN return_reason rr ON sl.return_reason_id = rr.return_reason_id "
                    + "WHERE sl.stock_loss_id = " + stockLossId;

            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                // Store original values
                originalStockId = rs.getInt("stock_id");
                originalQty = rs.getInt("qty");
                originalReasonId = rs.getInt("return_reason_id");

                // Set quantity
                qty.setText(String.valueOf(originalQty));

                // Select the stock in combo box
                selectStockInComboBox(originalStockId);

                // Select the reason in combo box
                selectReasonInComboBox(originalReasonId);

            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Stock loss record not found!");
                dispose();
            }

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading stock loss data: " + e.getMessage());
        }
    }

    private void selectStockInComboBox(int stockId) {
        try {
            // First get the stock details for the given stockId
            String query = "SELECT s.batch_no, p.product_name, s.qty "
                    + "FROM stock s "
                    + "JOIN product p ON s.product_id = p.product_id "
                    + "WHERE s.stock_id = " + stockId;

            ResultSet rs = MySQL.executeSearch(query);
            if (rs.next()) {
                String batchNo = rs.getString("batch_no");
                String productName = rs.getString("product_name");
                int availableQty = rs.getInt("qty");

                String stockDisplay = batchNo + " - " + productName + " (Available: " + availableQty + ")";

                // Now find this exact string in the combo box
                for (int i = 0; i < stockCombo.getItemCount(); i++) {
                    String item = stockCombo.getItemAt(i);
                    if (item.equals(stockDisplay)) {
                        stockCombo.setSelectedIndex(i);
                        return;
                    }
                }

                // If not found, try partial match
                for (int i = 0; i < stockCombo.getItemCount(); i++) {
                    String item = stockCombo.getItemAt(i);
                    if (item.contains(batchNo) && item.contains(productName)) {
                        stockCombo.setSelectedIndex(i);
                        return;
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void selectReasonInComboBox(int reasonId) {
        try {
            // First get the reason text for the given reasonId
            String query = "SELECT reason FROM return_reason WHERE return_reason_id = " + reasonId;
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                String reasonText = rs.getString("reason");

                // Now find this exact reason in the combo box
                for (int i = 0; i < reasonCombo.getItemCount(); i++) {
                    String item = reasonCombo.getItemAt(i);
                    if (item.equals(reasonText)) {
                        reasonCombo.setSelectedIndex(i);
                        return;
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupKeyboardNavigation() {
        // Stock Combo keyboard navigation with popup support
        stockCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (stockCombo.isPopupVisible()) {
                        stockCombo.setPopupVisible(false);
                    }
                    if (stockCombo.getSelectedIndex() > 0) {
                        reasonCombo.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!stockCombo.isPopupVisible()) {
                        stockCombo.showPopup();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    if (!stockCombo.isPopupVisible()) {
                        updateBtn.requestFocusInWindow();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    reasonCombo.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, stockCombo);
                }
            }
        });

        // Reason Combo keyboard navigation with popup support
        reasonCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (reasonCombo.isPopupVisible()) {
                        reasonCombo.setPopupVisible(false);
                    }
                    if (reasonCombo.getSelectedIndex() > 0) {
                        qty.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!reasonCombo.isPopupVisible()) {
                        reasonCombo.showPopup();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    if (!reasonCombo.isPopupVisible()) {
                        stockCombo.requestFocusInWindow();
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    qty.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    stockCombo.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, reasonCombo);
                }
            }
        });

        // Quantity field keyboard navigation
        qty.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (areAllRequiredFieldsFilled()) {
                        updateBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    reasonCombo.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (areAllRequiredFieldsFilled()) {
                        updateBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (areAllRequiredFieldsFilled()) {
                        updateBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    reasonCombo.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, qty);
                }
            }
        });

        // Update button keyboard navigation
        updateBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateStockLoss();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    qty.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    stockCombo.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    refreshBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    cancelBtn.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, updateBtn);
                }
            }
        });

        // Refresh button keyboard navigation
        refreshBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_F5) {
                    refreshData();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    qty.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    cancelBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    cancelBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    updateBtn.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, refreshBtn);
                }
            }
        });

        // Cancel button keyboard navigation
        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    qty.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    refreshBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    updateBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    refreshBtn.requestFocusInWindow();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, cancelBtn);
                }
            }
        });

        // Register global keyboard shortcuts
        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> updateStockLoss(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> refreshData(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void handleArrowNavigation(java.awt.event.KeyEvent evt, java.awt.Component source) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                handleRightArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_LEFT:
                handleLeftArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                handleDownArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                handleUpArrow(source);
                evt.consume();
                break;
        }
    }

    private void handleRightArrow(java.awt.Component source) {
        if (source == stockCombo) {
            reasonCombo.requestFocusInWindow();
        } else if (source == reasonCombo) {
            qty.requestFocusInWindow();
        } else if (source == qty) {
            if (areAllRequiredFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
        } else if (source == cancelBtn) {
            refreshBtn.requestFocusInWindow();
        } else if (source == refreshBtn) {
            updateBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            stockCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == stockCombo) {
            updateBtn.requestFocusInWindow();
        } else if (source == reasonCombo) {
            stockCombo.requestFocusInWindow();
        } else if (source == qty) {
            reasonCombo.requestFocusInWindow();
        } else if (source == cancelBtn) {
            qty.requestFocusInWindow();
        } else if (source == refreshBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            refreshBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == stockCombo) {
            if (!stockCombo.isPopupVisible()) {
                reasonCombo.requestFocusInWindow();
            }
        } else if (source == reasonCombo) {
            if (!reasonCombo.isPopupVisible()) {
                qty.requestFocusInWindow();
            }
        } else if (source == qty) {
            if (areAllRequiredFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
        } else if (source == cancelBtn) {
            refreshBtn.requestFocusInWindow();
        } else if (source == refreshBtn) {
            updateBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            stockCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == stockCombo) {
            if (!stockCombo.isPopupVisible()) {
                updateBtn.requestFocusInWindow();
            }
        } else if (source == reasonCombo) {
            if (!reasonCombo.isPopupVisible()) {
                stockCombo.requestFocusInWindow();
            }
        } else if (source == qty) {
            reasonCombo.requestFocusInWindow();
        } else if (source == cancelBtn) {
            qty.requestFocusInWindow();
        } else if (source == refreshBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            refreshBtn.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return stockCombo.getSelectedIndex() > 0
                && reasonCombo.getSelectedIndex() > 0
                && !qty.getText().trim().isEmpty()
                && !qty.getText().trim().equals("0");
    }

    // ---------------- BUTTON STYLES AND EFFECTS ----------------
    private void setupButtonStyles() {
        setupGradientButton(updateBtn);
        setupGradientButton(cancelBtn);

        refreshBtn.setBorderPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setOpaque(false);
        refreshBtn.setFocusable(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Create icons
        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        updateBtn.setIcon(updateIcon);

        FlatSVGIcon refreshIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
        refreshIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        refreshBtn.setIcon(refreshIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        setupButtonMouseListeners();
        setupButtonFocusListeners();
    }

    private void setupGradientButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.decode("#0893B0"));
        button.setFont(new Font("Nunito SemiBold", Font.BOLD, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                int w = c.getWidth();
                int h = c.getHeight();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isHover = button.getModel().isRollover();
                boolean isPressed = button.getModel().isPressed();
                boolean isFocused = button.hasFocus();

                if (!isFocused && !isHover && !isPressed) {
                    g2.setColor(new Color(0, 0, 0, 0));
                    g2.fillRoundRect(0, 0, w, h, 5, 5);
                    g2.setColor(Color.decode("#0893B0"));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
                } else {
                    Color topColor = new Color(0x12, 0xB5, 0xA6);
                    Color bottomColor = new Color(0x08, 0x93, 0xB0);
                    GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 5, 5);
                }

                super.paint(g, c);
            }
        });
    }

    private void setupButtonMouseListeners() {
        updateBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                updateBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                updateBtn.setIcon(hoverIcon);
                updateBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                updateBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                updateBtn.setIcon(normalIcon);
                updateBtn.repaint();
            }
        });

        refreshBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                refreshBtn.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                refreshBtn.setIcon(normalIcon);
            }
        });

        cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(hoverIcon);
                cancelBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });
    }

    private void setupButtonFocusListeners() {
        updateBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                updateBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                updateBtn.setIcon(focusedIcon);
                updateBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                updateBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                updateBtn.setIcon(normalIcon);
                updateBtn.repaint();
            }
        });

        refreshBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                refreshBtn.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                refreshBtn.setIcon(normalIcon);
            }
        });

        cancelBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(focusedIcon);
                cancelBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });
    }

    private void setupTooltips() {
        stockCombo.setToolTipText("Select stock item");
        reasonCombo.setToolTipText("Select loss reason");
        qty.setToolTipText("Enter quantity");
        updateBtn.setToolTipText("Update stock loss record");
        refreshBtn.setToolTipText("Refresh data");
        cancelBtn.setToolTipText("Close dialog");
    }

    private void setupFocusTraversal() {
        try {
            java.util.Set<AWTKeyStroke> forwardKeys = cancelBtn.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
            java.util.Set<AWTKeyStroke> newForwardKeys = new java.util.HashSet<>(forwardKeys);
            newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            cancelBtn.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);
        } catch (Exception e) {
        }
    }

    // ---------------- NOTIFICATION SYSTEM ----------------
    private void createStockLossNotification(String productName, String batchNo, String reason, int quantity, double sellingPrice, Connection conn) {
        PreparedStatement pstMassage = null;
        PreparedStatement pstNotification = null;

        try {
            String messageText = String.format("Stock loss updated: %s (Batch: %s) - %s (Qty: %d, Value: Rs.%,.2f)",
                    productName, batchNo, reason, quantity, quantity * sellingPrice);

            String checkSql = "SELECT COUNT(*) FROM massage WHERE massage = ?";
            pstMassage = conn.prepareStatement(checkSql);
            pstMassage.setString(1, messageText);
            ResultSet rs = pstMassage.executeQuery();

            int massageId;
            if (rs.next() && rs.getInt(1) > 0) {
                String getSql = "SELECT massage_id FROM massage WHERE massage = ?";
                pstMassage.close();
                pstMassage = conn.prepareStatement(getSql);
                pstMassage.setString(1, messageText);
                rs = pstMassage.executeQuery();
                rs.next();
                massageId = rs.getInt(1);
            } else {
                pstMassage.close();
                String insertMassageSql = "INSERT INTO massage (massage) VALUES (?)";
                pstMassage = conn.prepareStatement(insertMassageSql, PreparedStatement.RETURN_GENERATED_KEYS);
                pstMassage.setString(1, messageText);
                pstMassage.executeUpdate();

                rs = pstMassage.getGeneratedKeys();
                if (rs.next()) {
                    massageId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get generated massage ID");
                }
            }

            String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)";
            pstNotification = conn.prepareStatement(notificationSql);
            pstNotification.setInt(1, 1);
            pstNotification.setInt(2, 25);
            pstNotification.setInt(3, massageId);
            pstNotification.executeUpdate();

        } catch (Exception e) {
        } finally {
            try {
                if (pstMassage != null) {
                    pstMassage.close();
                }
                if (pstNotification != null) {
                    pstNotification.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private String getProductNameAndBatch(int stockId) throws SQLException {
        String query = "SELECT p.product_name, s.batch_no, s.selling_price "
                + "FROM stock s "
                + "JOIN product p ON s.product_id = p.product_id "
                + "WHERE s.stock_id = " + stockId;
        ResultSet rs = MySQL.executeSearch(query);

        if (rs.next()) {
            return rs.getString("product_name") + "|"
                    + rs.getString("batch_no") + "|"
                    + rs.getDouble("selling_price");
        }

        throw new SQLException("Product details not found for stock ID: " + stockId);
    }

    // ---------------- EXISTING BUSINESS LOGIC ----------------
    private void loadStocks() {
        try {
            String query = "SELECT s.stock_id, s.batch_no, p.product_name, s.qty "
                    + "FROM stock s "
                    + "JOIN product p ON s.product_id = p.product_id "
                    + "WHERE s.qty > 0 AND s.p_status_id = 1 "
                    + "ORDER BY s.batch_no, p.product_name";

            ResultSet rs = MySQL.executeSearch(query);
            Vector<String> stocks = new Vector<>();
            stocks.add("Select Stock");

            while (rs.next()) {
                String batchNo = rs.getString("batch_no");
                String productName = rs.getString("product_name");
                int qty = rs.getInt("qty");
                stocks.add(batchNo + " - " + productName + " (Available: " + qty + ")");
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(stocks);
            stockCombo.setModel(dcm);

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading stocks: " + e.getMessage());
        }
    }

    private void loadLossReasons() {
        try {
            String query = "SELECT return_reason_id, reason FROM return_reason";
            ResultSet rs = MySQL.executeSearch(query);
            Vector<String> reasons = new Vector<>();
            reasons.add("Select Reason");

            while (rs.next()) {
                String reason = rs.getString("reason").toLowerCase();
                if (reason.contains("expired") || reason.contains("damaged")
                        || reason.contains("defective") || reason.contains("malfunction")
                        || reason.contains("other")) {
                    reasons.add(rs.getString("reason"));
                }
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(reasons);
            reasonCombo.setModel(dcm);

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading reasons: " + e.getMessage());
        }
    }

    private void refreshData() {
        loadStocks();
        loadLossReasons();
        loadStockLossData();
        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                "Data refreshed successfully!");
        stockCombo.requestFocusInWindow();
    }

    private void updateStockLoss() {
        // Validation
        if (stockCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a stock!");
            stockCombo.requestFocus();
            return;
        }

        if (reasonCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a reason!");
            reasonCombo.requestFocus();
            return;
        }

        if (qty.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter quantity!");
            qty.requestFocus();
            return;
        }

        Connection conn = null;
        try {
            int newQty = Integer.parseInt(qty.getText().trim());
            if (newQty <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Quantity must be greater than 0!");
                qty.requestFocus();
                return;
            }

            // Get selected stock details
            String selectedStock = (String) stockCombo.getSelectedItem();
            int newStockId = extractStockId(selectedStock);
            int availableQty = extractAvailableQty(selectedStock);

            // Calculate the difference in quantity
            int qtyDifference = newQty - originalQty;

            // For stock change, we need to handle both old and new stock
            if (newStockId != originalStockId) {
                // If stock changed, we need to revert old stock and apply to new stock
                if (newQty > availableQty) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                            "Quantity cannot exceed available stock. Available: " + availableQty);
                    qty.requestFocus();
                    return;
                }
            } else {
                // Same stock, check if increased quantity is available
                if (qtyDifference > availableQty) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                            "Increased quantity cannot exceed available stock. Available: " + availableQty);
                    qty.requestFocus();
                    return;
                }
            }

            // Get reason ID
            String selectedReason = (String) reasonCombo.getSelectedItem();
            int reasonId = getReasonId(selectedReason);

            if (reasonId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Invalid reason selected!");
                return;
            }

            // Get user from session
            Session session = Session.getInstance();
            int userId = session.getUserId();

            // Validate user exists in database
            if (!isValidUser(userId)) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Invalid user session. Please log in again!");
                return;
            }

            // Get product details for notification
            String productDetails = getProductNameAndBatch(newStockId);
            String[] details = productDetails.split("\\|");
            String productName = details[0];
            String batchNo = details[1];
            double sellingPrice = Double.parseDouble(details[2]);

            // Get database connection
            conn = MySQL.getConnection();

            // Start transaction
            conn.setAutoCommit(false);

            try {
                // Revert the original stock loss (add back the original quantity to stock)
                String revertStockQuery = "UPDATE stock SET qty = qty + " + originalQty + " WHERE stock_id = " + originalStockId;
                MySQL.executeIUD(revertStockQuery);

                // Apply the new stock loss (subtract the new quantity from stock)
                String applyStockQuery = "UPDATE stock SET qty = qty - " + newQty + " WHERE stock_id = " + newStockId;
                MySQL.executeIUD(applyStockQuery);

                // Update the stock_loss record
                String updateLossQuery = "UPDATE stock_loss SET qty = " + newQty + ", "
                        + "stock_id = " + newStockId + ", "
                        + "return_reason_id = " + reasonId + ", "
                        + "user_id = " + userId + ", "
                        + "stock_loss_date = NOW() "
                        + "WHERE stock_loss_id = " + stockLossId;
                MySQL.executeIUD(updateLossQuery);

                // Create notification for stock loss update
                createStockLossNotification(productName, batchNo, selectedReason, newQty, sellingPrice, conn);

                // Commit transaction
                conn.commit();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "Stock loss updated successfully!");

                // Close dialog
                dispose();

            } catch (SQLException ex) {
                // Rollback transaction in case of error
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (SQLException rollbackEx) {
                }

                if (ex.getMessage().contains("foreign key constraint") && ex.getMessage().contains("user_id")) {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                            "Invalid user account. Please contact administrator!");
                } else {
                    throw ex;
                }
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                    }
                } catch (SQLException autoCommitEx) {
                }
            }

        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter a valid number for quantity!");
        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error updating stock loss: " + e.getMessage());
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    private boolean isValidUser(int userId) {
        try {
            String query = "SELECT user_id FROM user WHERE user_id = " + userId;
            ResultSet rs = MySQL.executeSearch(query);
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private int extractStockId(String stockText) throws SQLException {
        if (stockText.equals("Select Stock")) {
            throw new SQLException("Not a valid stock item");
        }

        String batchNo = stockText.split(" - ")[0];
        String query = "SELECT stock_id FROM stock WHERE batch_no = '" + batchNo + "'";
        ResultSet rs = MySQL.executeSearch(query);

        if (rs.next()) {
            return rs.getInt("stock_id");
        }

        throw new SQLException("Stock not found");
    }

    private int extractAvailableQty(String stockText) {
        if (stockText.equals("Select Stock")) {
            return 0;
        }

        String qtyPart = stockText.split("Available: ")[1];
        qtyPart = qtyPart.replace(")", "");
        return Integer.parseInt(qtyPart.trim());
    }

    private int getReasonId(String reason) throws SQLException {
        if (reason.equals("Select Reason")) {
            return -1;
        }

        String query = "SELECT return_reason_id FROM return_reason WHERE reason = '" + reason + "'";
        ResultSet rs = MySQL.executeSearch(query);

        if (rs.next()) {
            return rs.getInt("return_reason_id");
        }

        return -1;
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        qty = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        updateBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        refreshBtn = new javax.swing.JButton();
        stockCombo = new javax.swing.JComboBox<>();
        reasonCombo = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Stock Loss");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        qty.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        qty.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quantity *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 0, 14))); // NOI18N
        qty.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                qtyKeyPressed(evt);
            }
        });

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setForeground(new java.awt.Color(8, 147, 176));
        cancelBtn.setText("Cancel");
        cancelBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        updateBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        updateBtn.setForeground(new java.awt.Color(8, 147, 176));
        updateBtn.setText("Update");
        updateBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(8, 147, 176));
        jLabel2.setText("Edit Stock Loss");

        jSeparator2.setForeground(new java.awt.Color(0, 137, 176));

        refreshBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        refreshBtn.setForeground(new java.awt.Color(8, 147, 176));
        refreshBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        refreshBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBtnActionPerformed(evt);
            }
        });
        refreshBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                refreshBtnKeyPressed(evt);
            }
        });

        stockCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        stockCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stockCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Stock *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        stockCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stockComboActionPerformed(evt);
            }
        });
        stockCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                stockComboKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                stockComboKeyReleased(evt);
            }
        });

        reasonCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        reasonCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        reasonCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Reason *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        reasonCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reasonComboActionPerformed(evt);
            }
        });
        reasonCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                reasonComboKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                reasonComboKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel2)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(2, 2, 2))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addGap(390, 390, 390)
                                        .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(95, 95, 95)
                                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(stockCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(reasonCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(qty)))
                        .addGap(0, 23, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(stockCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(qty, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reasonCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        updateStockLoss();
    }//GEN-LAST:event_updateBtnActionPerformed

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
        refreshData();
    }//GEN-LAST:event_refreshBtnActionPerformed

    private void refreshBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_refreshBtnKeyPressed
    }//GEN-LAST:event_refreshBtnKeyPressed

    private void stockComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stockComboActionPerformed

    }//GEN-LAST:event_stockComboActionPerformed

    private void stockComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_stockComboKeyPressed

    }//GEN-LAST:event_stockComboKeyPressed

    private void stockComboKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_stockComboKeyReleased

    }//GEN-LAST:event_stockComboKeyReleased

    private void reasonComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reasonComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reasonComboActionPerformed

    private void reasonComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reasonComboKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_reasonComboKeyPressed

    private void reasonComboKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reasonComboKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_reasonComboKeyReleased

    private void qtyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_qtyKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            updateStockLoss();
        }
    }//GEN-LAST:event_qtyKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UpdateLossStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateLossStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateLossStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateLossStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateLossStock dialog = new UpdateLossStock(new javax.swing.JFrame(), true, 1);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField qty;
    private javax.swing.JComboBox<String> reasonCombo;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JComboBox<String> stockCombo;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
