package lk.com.pos.panel;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.privateclasses.RoundedPanel;
import lk.com.pos.connection.MySQL;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.border.Border;

public class NotificationPanel extends javax.swing.JPanel {

    private Timer reloadTimer;
    private Map<Integer, Color> messageTypeColors;
    private Map<Integer, String> messageTypeIcons;
    private Map<Integer, Color> iconColors;
    private Map<Integer, Integer> iconSizes;
    private String currentFilter = "ALL";
    private Color defaultButtonColor = new Color(245, 245, 245);
    private Color activeButtonColor = new Color(102, 102, 255);
    private Color defaultTextColor = new Color(66, 66, 66);
    private Color activeTextColor = Color.WHITE;

    // Keyboard navigation variables
    private java.util.List<RoundedPanel> notificationPanels;
    private int currentFocusIndex = -1;
    private final int DEFAULT_BORDER_SIZE = 1;
    private final int FOCUS_BORDER_SIZE = 2;
    private final int CORNER_RADIUS = 20;

    public NotificationPanel() {
        initComponents();
        initializeMessageTypeMappings();
        jPanel6.setBorder(null);
        customizeButtons();
        setupKeyboardNavigation();
        loadNotifications();
        startAutoReload();
        setupButtonListeners();
        filterNotifications("ALL"); // Set ALL as default active filter
    }

    private void initializeMessageTypeMappings() {
        messageTypeColors = new HashMap<>();

        // Soft, eye-friendly color palette
        messageTypeColors.put(1, new Color(255, 183, 77));   // Soft Orange - Expired Soon
        messageTypeColors.put(2, new Color(239, 83, 80));    // Soft Red - Low Stock
        messageTypeColors.put(3, new Color(102, 187, 106));  // Soft Green - Logged In
        messageTypeColors.put(4, new Color(121, 134, 203));  // Soft Purple - Log Out
        messageTypeColors.put(5, new Color(229, 115, 115));  // Light Red - Expired
        messageTypeColors.put(7, new Color(255, 138, 101));  // Soft Coral - Missed Due Date
        messageTypeColors.put(8, new Color(77, 182, 172));   // Teal - Add New Product
        messageTypeColors.put(9, new Color(129, 199, 132));  // Light Green - Add New Stock
        messageTypeColors.put(10, new Color(149, 117, 205)); // Soft Purple - Add New Supplier
        messageTypeColors.put(11, new Color(100, 181, 246)); // Soft Blue - Add New Credit
        messageTypeColors.put(12, new Color(77, 208, 225));  // Cyan - Credit Payed
        messageTypeColors.put(13, new Color(72, 201, 176));  // Mint Green - Complete Sale
        messageTypeColors.put(14, new Color(171, 71, 188));  // Soft Magenta - Edit Product/Stock
        messageTypeColors.put(16, new Color(255, 167, 38));  // Amber - Edit Supplier
        messageTypeColors.put(17, new Color(41, 182, 246));  // Sky Blue - Edit Credit
        messageTypeColors.put(18, new Color(126, 87, 194));  // Medium Purple - Edit Credit Pay
        messageTypeColors.put(19, new Color(87, 188, 95));   // Fresh Green - Add New Customer
        messageTypeColors.put(20, new Color(245, 124, 0));   // Orange - Edit Customer
        messageTypeColors.put(21, new Color(57, 204, 204));  // Turquoise - Add New User
        messageTypeColors.put(22, new Color(156, 204, 101)); // Light Green - Edit Profile
        messageTypeColors.put(23, new Color(179, 157, 219)); // Lavender - Add New Category
        messageTypeColors.put(24, new Color(255, 158, 128)); // Peach - Add New Brand
        messageTypeColors.put(25, new Color(229, 57, 53));   // Red - Stock Loss
        messageTypeColors.put(26, new Color(158, 158, 158)); // Gray - Return Product

        // Updated message types from your database
        messageTypeColors.put(27, new Color(76, 175, 80));   // Green - Add Credit Discount
        messageTypeColors.put(28, new Color(38, 198, 218));  // Light Blue - Add Expenses
        messageTypeColors.put(29, new Color(57, 73, 171));   // Indigo - Edit Expenses
        messageTypeColors.put(30, new Color(0, 150, 136));   // Teal - Add Cheque Payment
        messageTypeColors.put(31, new Color(104, 159, 56));  // Light Green - Edit Cheque Payment

        // Message type to icon filename mapping
        messageTypeIcons = new HashMap<>();
        messageTypeIcons.put(1, "expire-solid");      // Expired Soon
        messageTypeIcons.put(2, "graph-down");        // Low Stock
        messageTypeIcons.put(3, "user-in");           // Logged In
        messageTypeIcons.put(4, "user-out");          // Log Out
        messageTypeIcons.put(5, "clock-conflict");    // Expired
        messageTypeIcons.put(7, "calendar-cross");    // Missed Due Date
        messageTypeIcons.put(8, "box-add");           // Add New Product
        messageTypeIcons.put(9, "box-add");           // Add New Stock
        messageTypeIcons.put(10, "user-plus");        // Add New Supplier
        messageTypeIcons.put(11, "credit-card-check");// Add New Credit
        messageTypeIcons.put(12, "money-add");        // Credit Payed
        messageTypeIcons.put(13, "cart-check");       // Complete Sale
        messageTypeIcons.put(14, "package-shipping"); // Edit Product/Stock
        messageTypeIcons.put(16, "user-pen");         // Edit Supplier
        messageTypeIcons.put(17, "credit-edit");      // Edit Credit
        messageTypeIcons.put(18, "creditpayment-add");// Edit Credit Pay
        messageTypeIcons.put(19, "user-plus");        // Add New Customer
        messageTypeIcons.put(20, "user-pen");         // Edit Customer
        messageTypeIcons.put(21, "user-plus");        // Add New User
        messageTypeIcons.put(22, "user-pen");         // Edit Profile
        messageTypeIcons.put(23, "category");         // Add New Category
        messageTypeIcons.put(24, "add-brand");        // Add New Brand
        messageTypeIcons.put(25, "graph-down");       // Stock Loss
        messageTypeIcons.put(26, "rotate-circle");    // Return Product

        // Updated message type icons
        messageTypeIcons.put(27, "discount");         // Add Credit Discount
        messageTypeIcons.put(28, "money-remove");     // Add Expenses
        messageTypeIcons.put(29, "money-edit");       // Edit Expenses
        messageTypeIcons.put(30, "cheque");           // Add Cheque Payment
        messageTypeIcons.put(31, "cheque-edit");      // Edit Cheque Payment

        // Individual icon sizes for different message types
        iconSizes = new HashMap<>();
        iconSizes.put(1, 30);   // Expired Soon
        iconSizes.put(2, 26);   // Low Stock
        iconSizes.put(3, 26);   // Logged In
        iconSizes.put(4, 26);   // Log Out
        iconSizes.put(5, 28);   // Expired
        iconSizes.put(7, 28);   // Missed Due Date
        iconSizes.put(8, 30);   // Add New Product
        iconSizes.put(9, 28);   // Add New Stock
        iconSizes.put(10, 26);  // Add New Supplier
        iconSizes.put(11, 28);  // Add New Credit
        iconSizes.put(12, 30);  // Credit Payed
        iconSizes.put(13, 32);  // Complete Sale
        iconSizes.put(14, 26);  // Edit Product/Stock
        iconSizes.put(16, 26);  // Edit Supplier
        iconSizes.put(17, 28);  // Edit Credit
        iconSizes.put(18, 30);  // Edit Credit Pay
        iconSizes.put(19, 26);  // Add New Customer
        iconSizes.put(20, 26);  // Edit Customer
        iconSizes.put(21, 28);  // Add New User
        iconSizes.put(22, 26);  // Edit Profile
        iconSizes.put(23, 28);  // Add New Category
        iconSizes.put(24, 30);  // Add New Brand
        iconSizes.put(25, 32);  // Stock Loss
        iconSizes.put(26, 30);  // Return Product

        // Updated message type icon sizes
        iconSizes.put(27, 30);  // Add Credit Discount
        iconSizes.put(28, 28);  // Add Expenses
        iconSizes.put(29, 28);  // Edit Expenses
        iconSizes.put(30, 30);  // Add Cheque Payment
        iconSizes.put(31, 30);  // Edit Cheque Payment

        iconColors = new HashMap<>(messageTypeColors);
    }

    private Border createRoundedLineBorder(Color color, int thickness, int radius) {
        return new Border() {
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

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        };
    }

    private Color getLightBackgroundColor(Color baseColor) {
        // Create a lighter version of the base color for background
        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1] * 0.3f, 0.95f); // Reduce saturation and increase brightness
    }

    private void setupKeyboardNavigation() {
        notificationPanels = new java.util.ArrayList<>();

        // Register keyboard actions
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        // Up arrow key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp");
        actionMap.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFocusUp();
            }
        });

        // Down arrow key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFocusDown();
            }
        });

        // D key for delete
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "deleteCurrent");
        actionMap.put("deleteCurrent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteFocusedNotification();
            }
        });

        // V key for view
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "viewCurrent");
        actionMap.put("viewCurrent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewFocusedNotification();
            }
        });

        // Function keys for filters
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "filterAll");
        actionMap.put("filterAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterNotifications("ALL");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "filterUnread");
        actionMap.put("filterUnread", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterNotifications("UNREAD");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "filterLowStock");
        actionMap.put("filterLowStock", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterNotifications("LOW_STOCK");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "filterExpiring");
        actionMap.put("filterExpiring", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterNotifications("EXPIRING");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "filterMissedDue");
        actionMap.put("filterMissedDue", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterNotifications("MISSED_DUE_DATE");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "filterExpired");
        actionMap.put("filterExpired", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterNotifications("EXPIRED");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "markAllRead");
        actionMap.put("markAllRead", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAllAsRead();
            }
        });

        // Alt + D for delete all
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK), "deleteAll");
        actionMap.put("deleteAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAllNotifications();
            }
        });

        // Make panel focusable
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void moveFocusUp() {
        if (notificationPanels.isEmpty()) {
            return;
        }

        if (currentFocusIndex > 0) {
            setFocusedNotification(currentFocusIndex - 1);
        } else {
            // Wrap to bottom
            setFocusedNotification(notificationPanels.size() - 1);
        }
    }

    private void moveFocusDown() {
        if (notificationPanels.isEmpty()) {
            return;
        }

        if (currentFocusIndex < notificationPanels.size() - 1) {
            setFocusedNotification(currentFocusIndex + 1);
        } else {
            // Wrap to top
            setFocusedNotification(0);
        }
    }

    private void setFocusedNotification(int index) {
        // Remove focus from current
        if (currentFocusIndex >= 0 && currentFocusIndex < notificationPanels.size()) {
            RoundedPanel prevPanel = notificationPanels.get(currentFocusIndex);
            updateNotificationBorder(prevPanel, DEFAULT_BORDER_SIZE);
        }

        // Set focus to new
        currentFocusIndex = index;
        if (currentFocusIndex >= 0 && currentFocusIndex < notificationPanels.size()) {
            RoundedPanel currentPanel = notificationPanels.get(currentFocusIndex);
            updateNotificationBorder(currentPanel, FOCUS_BORDER_SIZE);

            // Scroll to make it visible
            Rectangle bounds = currentPanel.getBounds();
            bounds.y -= 50; // Add some padding
            bounds.height += 100;
            jPanel6.scrollRectToVisible(bounds);
        }

        // Ensure panel has focus for continued keyboard input
        this.requestFocusInWindow();
    }

    private void updateNotificationBorder(RoundedPanel panel, int borderSize) {
        Integer msgTypeId = (Integer) panel.getClientProperty("msgTypeId");
        if (msgTypeId != null) {
            Color borderColor = messageTypeColors.getOrDefault(msgTypeId, new Color(189, 189, 189));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    createRoundedLineBorder(borderColor, borderSize, CORNER_RADIUS),
                    new EmptyBorder(12, 15, 12, 15)
            ));
        }
    }

    private void deleteFocusedNotification() {
        if (currentFocusIndex >= 0 && currentFocusIndex < notificationPanels.size()) {
            RoundedPanel panel = notificationPanels.get(currentFocusIndex);
            Integer notificationId = (Integer) panel.getClientProperty("notificationId");
            if (notificationId != null) {
                deleteNotification(notificationId);
            }
        }
    }

    private void viewFocusedNotification() {
        if (currentFocusIndex >= 0 && currentFocusIndex < notificationPanels.size()) {
            RoundedPanel panel = notificationPanels.get(currentFocusIndex);
            Integer notificationId = (Integer) panel.getClientProperty("notificationId");
            String type = (String) panel.getClientProperty("notificationType");
            String message = (String) panel.getClientProperty("notificationMessage");
            Timestamp timestamp = (Timestamp) panel.getClientProperty("notificationTimestamp");

            if (notificationId != null) {
                viewNotificationDetails(notificationId, type, message, timestamp);
            }
        }
    }

    private void customizeButtons() {
        JButton[] filterButtons = {jButton1, jButton2, jButton5, jButton6, jButton7, jButton8};
        for (JButton btn : filterButtons) {
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setBackground(defaultButtonColor);
            btn.setForeground(defaultTextColor);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        }

        jButton9.setFocusPainted(false);
        jButton9.setBorderPainted(false);
        jButton9.setBackground(new Color(102, 102, 255));
        jButton9.setForeground(Color.WHITE);
        jButton9.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jButton9.setBorder(new EmptyBorder(8, 20, 8, 20));

        deleteAll.setFocusPainted(false);
        deleteAll.setBorderPainted(false);
        deleteAll.setBackground(new Color(244, 67, 54));
        deleteAll.setForeground(Color.WHITE);
        deleteAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteAll.setBorder(new EmptyBorder(8, 20, 8, 20));

        jPanel1.setBackground(new Color(250, 250, 250));
        jPanel1.setBorder(new EmptyBorder(0, 0, 0, 0));
        jPanel2.setBackground(Color.WHITE);
        jPanel6.setBackground(new Color(250, 250, 250));
        jPanel6.setBorder(new EmptyBorder(0, 10, 0, 10));
    }

    private void setupButtonListeners() {
        jButton1.addActionListener(e -> filterNotifications("ALL"));
        jButton2.addActionListener(e -> filterNotifications("UNREAD"));
        jButton5.addActionListener(e -> filterNotifications("LOW_STOCK"));
        jButton6.addActionListener(e -> filterNotifications("EXPIRING"));
        jButton7.addActionListener(e -> filterNotifications("MISSED_DUE_DATE"));
        jButton8.addActionListener(e -> filterNotifications("EXPIRED"));

        jButton9.addActionListener(e -> markAllAsRead());
        deleteAll.addActionListener(e -> deleteAllNotifications());
    }

    private void filterNotifications(String filter) {
        currentFilter = filter;
        loadNotifications();
        updateButtonAppearance();
        // Reset focus after filter change
        currentFocusIndex = -1;
        if (!notificationPanels.isEmpty()) {
            setFocusedNotification(0);
        }
    }

    private void updateButtonAppearance() {
        JButton[] filterButtons = {jButton1, jButton2, jButton5, jButton6, jButton7, jButton8};
        for (JButton btn : filterButtons) {
            btn.setBackground(defaultButtonColor);
            btn.setForeground(defaultTextColor);
        }

        JButton activeButton = null;
        switch (currentFilter) {
            case "ALL":
                activeButton = jButton1;
                break;
            case "UNREAD":
                activeButton = jButton2;
                break;
            case "LOW_STOCK":
                activeButton = jButton5;
                break;
            case "EXPIRING":
                activeButton = jButton6;
                break;
            case "MISSED_DUE_DATE":
                activeButton = jButton7;
                break;
            case "EXPIRED":
                activeButton = jButton8;
                break;
        }

        if (activeButton != null) {
            activeButton.setBackground(activeButtonColor);
            activeButton.setForeground(activeTextColor);
        }
    }

    private void loadNotifications() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MySQL.getConnection();
            String sql = buildQuery();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            jPanel6.removeAll();
            jPanel6.setLayout(new BoxLayout(jPanel6, BoxLayout.Y_AXIS));
            notificationPanels.clear();

            boolean hasNotifications = false;

            while (rs.next()) {
                hasNotifications = true;
                createNotificationItem(
                        rs.getInt("n.id"),
                        rs.getString("mt.msg_type"),
                        rs.getString("m.massage"),
                        rs.getTimestamp("n.create_at"),
                        rs.getInt("n.is_read"),
                        rs.getInt("n.msg_type_id")
                );
            }

            if (!hasNotifications) {
                JLabel noNotifications = new JLabel("No notifications found");
                noNotifications.setFont(new Font("Nunito SemiBold", Font.PLAIN, 16));
                noNotifications.setForeground(new Color(158, 158, 158));
                noNotifications.setHorizontalAlignment(SwingConstants.CENTER);
                noNotifications.setBorder(new EmptyBorder(40, 0, 0, 0));
                jPanel6.add(noNotifications);
            }

            jPanel6.revalidate();
            jPanel6.repaint();
            updateUnreadCount();

            // Set focus to first notification if available
            if (!notificationPanels.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    setFocusedNotification(0);
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String buildQuery() {
        String baseQuery = "SELECT n.id, n.is_read, n.create_at, n.msg_type_id, mt.msg_type, m.massage "
                + "FROM notifocation n "
                + "INNER JOIN msg_type mt ON n.msg_type_id = mt.msg_type_id "
                + "INNER JOIN massage m ON n.massage_id = m.massage_id ";

        switch (currentFilter) {
            case "UNREAD":
                return baseQuery + "WHERE n.is_read = 1 ORDER BY n.create_at DESC"; // Changed to 1 for unread
            case "LOW_STOCK":
                return baseQuery + "WHERE n.msg_type_id = 2 ORDER BY n.create_at DESC";
            case "EXPIRING":
                return baseQuery + "WHERE n.msg_type_id = 1 ORDER BY n.create_at DESC";
            case "MISSED_DUE_DATE":
                return baseQuery + "WHERE n.msg_type_id = 7 ORDER BY n.create_at DESC";
            case "EXPIRED":
                return baseQuery + "WHERE n.msg_type_id = 5 ORDER BY n.create_at DESC";
            default:
                return baseQuery + "ORDER BY n.create_at DESC";
        }
    }

    private void createNotificationItem(int id, String msgType, String message, Timestamp timestamp,
            int isRead, int msgTypeId) {
        RoundedPanel notificationPanel = new RoundedPanel();
        notificationPanel.setLayout(new BorderLayout(15, 0));
        notificationPanel.setPreferredSize(new Dimension(900, 90));
        notificationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        Color borderColor = messageTypeColors.getOrDefault(msgTypeId, new Color(189, 189, 189));
        Color lightBackgroundColor = getLightBackgroundColor(borderColor);

        // Enhanced rounded border for notification panel only
        notificationPanel.setBorder(BorderFactory.createCompoundBorder(
                createRoundedLineBorder(borderColor, DEFAULT_BORDER_SIZE, CORNER_RADIUS),
                new EmptyBorder(12, 15, 12, 15)
        ));

        // FIXED: 1 means unread, 0 means read
        if (isRead == 1) { // Unread notifications
            notificationPanel.setBackground(new Color(250, 253, 255));
        } else { // Read notifications
            notificationPanel.setBackground(Color.WHITE);
        }

        // Store data for keyboard actions
        notificationPanel.putClientProperty("notificationId", id);
        notificationPanel.putClientProperty("notificationType", msgType);
        notificationPanel.putClientProperty("notificationMessage", message);
        notificationPanel.putClientProperty("notificationTimestamp", timestamp);
        notificationPanel.putClientProperty("msgTypeId", msgTypeId);
        notificationPanel.putClientProperty("isRead", isRead); // Store isRead for mouse listener

        // Icon panel with background color and NO border
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(60, 60));

        // Icon container with background color and rounded corners (NO border)
        JPanel iconContainer = new JPanel(new GridBagLayout());
        iconContainer.setOpaque(true);
        iconContainer.setBackground(lightBackgroundColor); // Light background color
        iconContainer.setPreferredSize(new Dimension(50, 50));
        // Make icon container fully rounded (circular)
        iconContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String iconType = messageTypeIcons.getOrDefault(msgTypeId, "info");
        int iconSize = iconSizes.getOrDefault(msgTypeId, 28);
        JLabel iconLabel = createLocalIcon(iconType, borderColor, iconSize); // Icon with same border color
        iconContainer.add(iconLabel);
        iconPanel.add(iconContainer);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false);

        JLabel typeLabel = new JLabel(msgType);
        typeLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 15));
        typeLabel.setForeground(borderColor);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        msgLabel.setForeground(new Color(97, 97, 97));
        msgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        messagePanel.add(typeLabel);
        messagePanel.add(Box.createVerticalStrut(4));
        messagePanel.add(msgLabel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(180, 70));

        rightPanel.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(180, 35));

        // "View Details" button with same background color as icon, dark text, and NO border
        JButton viewBtn = new JButton("View Details");
        viewBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
        viewBtn.setBackground(lightBackgroundColor); // Same background color as icon
        viewBtn.setForeground(new Color(66, 66, 66)); // Dark text color
        viewBtn.setFocusPainted(false);
        viewBtn.setBorderPainted(false); // No border
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBtn.setBorder(new EmptyBorder(6, 12, 6, 12)); // Only padding, no border
        viewBtn.setPreferredSize(new Dimension(100, 30));

        JButton deleteBtn = new JButton();
        try {
            FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 18, 18);
            deleteBtn.setIcon(deleteIcon);
        } catch (Exception e) {
            deleteBtn.setText("×");
            deleteBtn.setFont(new Font("Arial", Font.BOLD, 16));
        }
        deleteBtn.setForeground(new Color(244, 67, 54));
        deleteBtn.setBorder(BorderFactory.createEmptyBorder());
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setPreferredSize(new Dimension(30, 30));
        deleteBtn.setToolTipText("Delete notification");

        buttonPanel.add(viewBtn);
        buttonPanel.add(deleteBtn);

        rightPanel.add(buttonPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel dateLabel = new JLabel(getRelativeTime(timestamp));
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
        dateLabel.setForeground(new Color(130, 130, 130));
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel dateWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        dateWrapper.setOpaque(false);
        dateWrapper.setMaximumSize(new Dimension(180, 20));
        dateWrapper.setBorder(new EmptyBorder(0, 0, 0, 10));
        dateWrapper.add(dateLabel);

        rightPanel.add(dateWrapper);
        rightPanel.add(Box.createVerticalGlue());

        notificationPanel.add(iconPanel, BorderLayout.WEST);
        notificationPanel.add(messagePanel, BorderLayout.CENTER);
        notificationPanel.add(rightPanel, BorderLayout.EAST);

        viewBtn.addActionListener(e -> viewNotificationDetails(id, msgType, message, timestamp));
        deleteBtn.addActionListener(e -> deleteNotification(id));

        notificationPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // FIXED: 1 means unread, so mark as read only if it's unread
                if (isRead == 1) {
                    markAsRead(id);
                }
                // Set focus when clicked
                int clickedIndex = notificationPanels.indexOf(notificationPanel);
                if (clickedIndex >= 0) {
                    setFocusedNotification(clickedIndex);
                }
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                notificationPanel.setBackground(new Color(248, 248, 248));
                // Remove border color changes on hover for icon container
                iconContainer.setBackground(getLightBackgroundColor(borderColor.darker()));
                // Change view button background on hover
                viewBtn.setBackground(getLightBackgroundColor(borderColor.darker()));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // FIXED: 1 means unread, 0 means read
                if (isRead == 1) {
                    notificationPanel.setBackground(new Color(250, 253, 255));
                } else {
                    notificationPanel.setBackground(Color.WHITE);
                }
                // Only reset background if not focused
                int panelIndex = notificationPanels.indexOf(notificationPanel);
                if (panelIndex != currentFocusIndex) {
                    iconContainer.setBackground(lightBackgroundColor);
                    viewBtn.setBackground(lightBackgroundColor);
                }
            }
        });

        jPanel6.add(notificationPanel);
        jPanel6.add(Box.createRigidArea(new Dimension(0, 12)));

        // Add to navigation list
        notificationPanels.add(notificationPanel);
    }

    private JLabel createLocalIcon(String iconType, Color color, int size) {
        try {
            FlatSVGIcon svgIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconType + ".svg", size, size);

            // Apply color filter - this will replace black with your desired color
            svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> {
                // Replace any non-transparent color with the specified color
                if (c.getAlpha() > 0) {
                    return new Color(color.getRed(), color.getGreen(), color.getBlue(), c.getAlpha());
                }
                return c;
            }));

            JLabel label = new JLabel(svgIcon);
            label.setPreferredSize(new Dimension(size, size));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder());
            return label;

        } catch (Exception e) {
            System.err.println("Error loading icon: " + iconType + ".svg - " + e.getMessage());

            // Fallback: create a simple colored icon
            return createSimpleColoredIcon(color, size);
        }
    }

    private JLabel createSimpleColoredIcon(Color color, int size) {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2d.dispose();
            }
        };
        label.setPreferredSize(new Dimension(size, size));
        label.setOpaque(false);
        return label;
    }

    private void markAsRead(int notificationId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = MySQL.getConnection();
            // FIXED: Set is_read to 0 to mark as read (since 1 is unread, 0 is read)
            String sql = "UPDATE notifocation SET is_read = 0 WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
            loadNotifications();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void markAllAsRead() {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = MySQL.getConnection();
            // FIXED: Set is_read to 0 where it's currently 1 (unread)
            String sql = "UPDATE notifocation SET is_read = 0 WHERE is_read = 1";
            stmt = conn.prepareStatement(sql);
            int updated = stmt.executeUpdate();
            loadNotifications();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, updated + " notification(s) marked as read");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error marking notifications as read: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteNotification(int notificationId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this notification?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = MySQL.getConnection();
                String sql = "DELETE FROM notifocation WHERE id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, notificationId);
                stmt.executeUpdate();
                loadNotifications();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting notification: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deleteAllNotifications() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete ALL notifications?",
                "Confirm Delete All",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = MySQL.getConnection();
                String sql = "DELETE FROM notifocation";
                stmt = conn.prepareStatement(sql);
                int deleted = stmt.executeUpdate();
                loadNotifications();
                JOptionPane.showMessageDialog(this, deleted + " notification(s) deleted");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting notifications: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void viewNotificationDetails(int id, String type, String message, Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm:ss");
        String details = String.format(
                "<html><div style='padding:10px;font-family:Nunito;'>"
                + "<b style='font-size:14px;'>Type:</b> <span style='font-size:13px;'>%s</span><br><br>"
                + "<b style='font-size:14px;'>Message:</b> <span style='font-size:13px;'>%s</span><br><br>"
                + "<b style='font-size:14px;'>Time:</b> <span style='font-size:13px;'>%s</span>"
                + "</div></html>",
                type, message, sdf.format(timestamp)
        );

        JOptionPane.showMessageDialog(this, details, "Notification Details",
                JOptionPane.INFORMATION_MESSAGE);

        // FIXED: Mark as read when viewing details (only if it's unread)
        markAsRead(id);
    }

    private void updateUnreadCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MySQL.getConnection();
            // FIXED: Count notifications where is_read = 1 (unread)
            String sql = "SELECT COUNT(*) as unread_count FROM notifocation WHERE is_read = 1";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int unreadCount = rs.getInt("unread_count");
                jButton2.setText("Unread (" + unreadCount + ")");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void startAutoReload() {
        reloadTimer = new Timer();
        reloadTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    loadNotifications();
                });
            }
        }, 0, 30000);
    }

    private String getRelativeTime(Timestamp timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp.getTime();

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (weeks < 4) {
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (months < 12) {
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }

    public void stopAutoReload() {
        if (reloadTimer != null) {
            reloadTimer.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        deleteAll = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel6 = new javax.swing.JPanel();
        notfication = new lk.com.pos.privateclasses.RoundedPanel();
        deltBtn = new javax.swing.JButton();
        icon = new javax.swing.JLabel();
        massageType = new javax.swing.JLabel();
        msg = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        viewButton = new javax.swing.JButton();

        jButton1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton1.setText("All");

        jButton2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton2.setText("Unread (0)");

        jButton5.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton5.setText("Low Stock");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton6.setText("Expiring");

        jButton7.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton7.setText("Missed Due Date");

        jButton8.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton8.setText("Expired");

        jButton9.setBackground(new java.awt.Color(92, 107, 192));
        jButton9.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton9.setForeground(new java.awt.Color(255, 255, 255));
        jButton9.setText("Mark All as Read");

        deleteAll.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        deleteAll.setText("Delete All");
        deleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(deleteAll)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteAll, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        deltBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        deltBtn.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        deltBtn.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        icon.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        icon.setText("icon");

        massageType.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        massageType.setText("Alert");

        msg.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        msg.setText("Product or customer name");

        jLabel4.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Date");

        viewButton.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        viewButton.setText("View Details");

        javax.swing.GroupLayout notficationLayout = new javax.swing.GroupLayout(notfication);
        notfication.setLayout(notficationLayout);
        notficationLayout.setHorizontalGroup(
            notficationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notficationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(icon, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notficationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(notficationLayout.createSequentialGroup()
                        .addComponent(msg)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notficationLayout.createSequentialGroup()
                        .addGroup(notficationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(notficationLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(notficationLayout.createSequentialGroup()
                                .addComponent(massageType)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 715, Short.MAX_VALUE)
                                .addComponent(viewButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(deltBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21))))
        );
        notficationLayout.setVerticalGroup(
            notficationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notficationLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(notficationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(notficationLayout.createSequentialGroup()
                        .addGroup(notficationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deltBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(viewButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                        .addComponent(jLabel4))
                    .addGroup(notficationLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(massageType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(msg)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(icon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(notfication, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(notfication, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(317, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel6);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1))
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
                .addGap(1, 1, 1))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void deleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteAllActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteAll;
    private javax.swing.JButton deltBtn;
    private javax.swing.JLabel icon;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel massageType;
    private javax.swing.JLabel msg;
    private lk.com.pos.privateclasses.RoundedPanel notfication;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables
}
