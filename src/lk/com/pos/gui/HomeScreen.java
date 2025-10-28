package lk.com.pos.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicMenuItemUI;
import lk.com.pos.connection.MySQL;
import lk.com.pos.dialog.AddNewUser;
import lk.com.pos.dialog.EditProfile;
import lk.com.pos.panel.CustomerManagement;
import lk.com.pos.panel.DashboardPanel;
import lk.com.pos.panel.PosPanel;
import lk.com.pos.panel.ProductPanel;
import lk.com.pos.panel.SalesPanel;
import lk.com.pos.panel.SupplierPanel;
// import lk.com.pos.session.Session;
import lk.com.pos.util.AppIconUtil;
import raven.toast.Notifications;

public class HomeScreen extends JFrame {

    // ===== SESSION CODE COMMENTED OUT =====
    // int userId = Session.getInstance().getUserId();
    // String roleName = Session.getInstance().getRoleName();
    
    // Temporary hardcoded values for testing (remove when session is restored)
    int userId = 1;
    String roleName = "Admin";
    // ===== END SESSION CODE =====

    // Icons
    private FlatSVGIcon dashboardIcon, posIcon, supplierIcon, salesIcon, creditIcon, stockIcon, menuIcon, signOutIcon;
    private FlatSVGIcon navMenuIcon, navBellIcon, navProfileIcon, navKeyIcon, calculatorIcon;

    // Sidebar animation
    private static final int SIDEBAR_WIDTH_EXPANDED = 230;
    private static final int SIDEBAR_WIDTH_COLLAPSED = 70;
    private static final int STEP_SIZE = 20;
    private static final int SLIDE_DELAY = 8;
    private boolean isSidebarExpanded = true;
    private Timer slideTimer;
    private int animationTargetWidth;
    private boolean animationOpening;

    // Clock timer
    private Timer clockTimer;
    private SimpleDateFormat timeFormat;

    // Panels for card layout
    private DashboardPanel dashboardPanel;
    private PosPanel posPanel;
    private SupplierPanel supplierPanel;
    private SalesPanel salesPanel;
    private CustomerManagement customerManagementPanel;
    private ProductPanel productPanel;
    private CardLayout contentPanelLayout;

    // Current active button
    private JButton activeButton = null;
    private Color activeTopColor = new Color(0x12B5A6);
    private Color activeBottomColor = new Color(0x0893B0);
    private Color normalTextColor = Color.BLACK;
    private Color hoverTop = new Color(0x12B5A6);
    private Color hoverBottom = new Color(0x0893B0);
    private Color signOutHoverTop = new Color(255, 0, 0);
    private Color signOutHoverBottom = new Color(200, 0, 0);

    private JPopupMenu profilePopup;

    private JLabel notificationBadge;
    private JPopupMenu notificationPopup;

    // Track hover state for each button
    private java.util.Map<JButton, Boolean> buttonHoverStates = new java.util.HashMap<>();

    public HomeScreen() {

        initComponents();

        // ===== SESSION VALIDATION COMMENTED OUT =====
        // if (!hasValidSession()) {
        //     redirectToLogin();
        //     return;
        // }
        // ===== END SESSION CODE =====
        
        loadPanels();
        init();
        initSidebarSlider();

    }

    // ===== SESSION VALIDATION METHOD COMMENTED OUT =====
    // private boolean hasValidSession() {
    //     Session session = Session.getInstance();
    //     boolean isValid = session.getUserId() != 0 && session.getRoleName() != null;
    //
    //     if (!isValid) {
    //         System.out.println("Invalid session - UserId: " + session.getUserId() + ", Role: " + session.getRoleName());
    //     }
    //
    //     return isValid;
    // }
    // ===== END SESSION CODE =====

    // ===== REDIRECT TO LOGIN METHOD COMMENTED OUT =====
    // private void redirectToLogin() {
    //     // Dispose current frame first
    //     this.dispose();
    //
    //     // Create login screen and show notification there
    //     SwingUtilities.invokeLater(() -> {
    //         LogIn login = new LogIn();
    //         login.setVisible(true);
    //         // Show notification on the login screen
    //         Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Session expired! Please login again.");
    //     });
    // }
    // ===== END SESSION CODE =====

    private void init() {
        AppIconUtil.applyIcon(this);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // ===== SESSION VALIDATION COMMENTED OUT =====
        // if (!hasValidSession()) {
        //     redirectToLogin();
        //     return;
        // }
        // ===== END SESSION CODE =====

        try {
            ResultSet rs = MySQL.executeSearch("SELECT name FROM user WHERE user_id = " + userId);
            if (rs.next()) {
                helloLabel.setText("Hello, " + rs.getString("name") + " (" + roleName + ")");
            }
        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Database Error: " + e.getMessage());
        }

        // Initialize sidebar icons
        dashboardIcon = new FlatSVGIcon("lk/com/pos/icon/dashboard.svg", 27, 27);
        posIcon = new FlatSVGIcon("lk/com/pos/icon/cart.svg", 28, 28);
        supplierIcon = new FlatSVGIcon("lk/com/pos/icon/truck.svg", 20, 20);
        salesIcon = new FlatSVGIcon("lk/com/pos/icon/dollar.svg", 22, 23);
        creditIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 20, 20);
        stockIcon = new FlatSVGIcon("lk/com/pos/icon/box.svg", 20, 20);
        menuIcon = new FlatSVGIcon("lk/com/pos/icon/sidebar-expand.svg", 28, 28);
        signOutIcon = new FlatSVGIcon("lk/com/pos/icon/signout.svg", 20, 20);
        calculatorIcon = new FlatSVGIcon("lk/com/pos/icon/calculator.svg", 24, 24);

        // Initialize navigation bar icons
        navMenuIcon = new FlatSVGIcon("lk/com/pos/icon/menu.svg", 20, 20);
        navBellIcon = new FlatSVGIcon("lk/com/pos/icon/bell.svg", 20, 20);
        navProfileIcon = new FlatSVGIcon("lk/com/pos/icon/profile.svg", 26, 26);
        navKeyIcon = new FlatSVGIcon("lk/com/pos/icon/keyboard.svg", 25, 25);

        // Set navigation bar icons with hover effects
        setupNavButtonWithHoverText(menuBtn, navMenuIcon, "Toggle Sidebar");
        setupNavButtonWithHoverText(bellBtn, navBellIcon, "Notifications");
        setupNavButtonWithHoverText(profileBtn, navProfileIcon, "User Profile");
        setupNavButtonWithHoverText(keyBtn, navKeyIcon, "Shortcuts");
        setupNavButtonWithHoverText(calBtn, calculatorIcon, "Calculator");

        setupProfileDropdown();

        // Track hover states
        buttonHoverStates.put(dashboardBtn, false);
        buttonHoverStates.put(posBtn, false);
        buttonHoverStates.put(supplierBtn, false);
        buttonHoverStates.put(salesBtn, false);
        buttonHoverStates.put(creditBtn, false);
        buttonHoverStates.put(stockBtn, false);
        buttonHoverStates.put(calBtn, false);
        buttonHoverStates.put(signOutBtn, false);

        // Setup hover buttons for sidebar
        setupHoverButton(dashboardBtn, dashboardIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(posBtn, posIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(supplierBtn, supplierIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(salesBtn, salesIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(creditBtn, creditIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(stockBtn, stockIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(signOutBtn, signOutIcon, Color.RED, signOutHoverTop, signOutHoverBottom);

        // Setup menu button for sidebar expand/collapse
        setupMenuButtonForSidebar();

        // Logo setup
        updateLogo();

        // Button padding
        dashboardBtn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        posBtn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        supplierBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        salesBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        creditBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        stockBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        signOutBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));

        // Set sidebar collapsed at startup
        isSidebarExpanded = false;
        sidePenal.setPreferredSize(new Dimension(SIDEBAR_WIDTH_COLLAPSED, sidePenal.getPreferredSize().height));
        setButtonTextVisible(false);
        updateLogo();
        penal1.revalidate();
        penal1.repaint();

        // Initialize and start clock timer
        timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        startClockTimer();

        // Set dashboard as default active button
        setActiveButton(dashboardBtn);
        showDashboardPanel();
        updateShortcutIconVisibility();
        setupNotificationSystem();
        loadUnreadNotifications();

        new Timer(30000, e -> {
            try {
                loadUnreadNotifications();
            } catch (Exception ex) {
                // Silently ignore timer errors
            }
        }).start();

    }

    private void setupNotificationSystem() {
        notificationPopup = new JPopupMenu();
        notificationPopup.setFocusable(false);

        notificationBadge = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        notificationBadge.setOpaque(false);
        notificationBadge.setForeground(Color.WHITE);
        notificationBadge.setFont(new Font("Arial", Font.BOLD, 11));
        notificationBadge.setHorizontalAlignment(SwingConstants.CENTER);
        notificationBadge.setPreferredSize(new Dimension(18, 18));
        notificationBadge.setVisible(false);

        // Position badge on top-right of bell button
        bellBtn.setLayout(new BorderLayout());
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);
        badgePanel.add(notificationBadge);
        bellBtn.add(badgePanel, BorderLayout.NORTH);

        // 🔔 Toggle popup
        bellBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (notificationPopup.isVisible()) {
                    notificationPopup.setVisible(false);
                    // ✅ Mark as read when popup closes
                    markAllNotificationsAsRead();
                } else {
                    showNotifications();
                }
            }
        });

        // 🪟 Close popup when window loses focus
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                if (notificationPopup.isVisible()) {
                    notificationPopup.setVisible(false);
                    // ✅ Mark as read when popup closes
                    markAllNotificationsAsRead();
                }
            }
        });
    }

    private void loadUnreadNotifications() {
        try {
            ResultSet rs = MySQL.executeSearch(
                    "SELECT n.id, m.massage, mt.msg_type, n.create_at "
                    + "FROM notifocation n "
                    + "JOIN massage m ON n.massage_id = m.massage_id "
                    + "JOIN msg_type mt ON n.msg_type_id = mt.msg_type_id "
                    + "WHERE n.is_read = 1 "
                    + "ORDER BY n.create_at DESC"
            );

            notificationPopup.removeAll();
            int count = 0;
            boolean firstItem = true;

            // Create header panel with title and close button
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
            headerPanel.setBackground(new Color(0xF8F9FA));

            // Title
            JLabel titleLabel = new JLabel("Notifications");
            titleLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
            titleLabel.setForeground(new Color(0x333333));

            // Close button
            FlatSVGIcon closeIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 16, 16);
            closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                @Override
                public Color filter(Color color) {
                    return new Color(0x666666);
                }
            });

            JButton closeButton = new JButton(closeIcon);
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeButton.setToolTipText("Close");
            closeButton.addActionListener(e -> {
                notificationPopup.setVisible(false);
                // ✅ Mark all as read when ❌ is clicked
                markAllNotificationsAsRead();
            });

            // Add hover effect to close button
            closeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                        @Override
                        public Color filter(Color color) {
                            return new Color(0xFF0000);
                        }
                    });
                    closeButton.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                        @Override
                        public Color filter(Color color) {
                            return new Color(0x666666);
                        }
                    });
                    closeButton.repaint();
                }
            });

            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(closeButton, BorderLayout.EAST);

            notificationPopup.add(headerPanel);

            // Add separator after header
            JSeparator headerSeparator = new JSeparator();
            headerSeparator.setForeground(new Color(0xDDDDDD));
            notificationPopup.add(headerSeparator);

            while (rs != null && rs.next()) {
                count++;
                String message = rs.getString("massage");
                String type = rs.getString("msg_type").toLowerCase();
                String time = rs.getString("create_at");

                // Add separator between notifications
                if (!firstItem) {
                    JSeparator separator = new JSeparator();
                    separator.setForeground(new Color(0xDDDDDD));
                    notificationPopup.add(separator);
                }
                firstItem = false;

                // Select icon based on type
                FlatSVGIcon icon;
                switch (type) {
                    case "success":
                        icon = new FlatSVGIcon("lk/com/pos/icon/success.svg", 18, 18);
                        break;
                    case "warning":
                        icon = new FlatSVGIcon("lk/com/pos/icon/warning.svg", 18, 18);
                        break;
                    default:
                        icon = new FlatSVGIcon("lk/com/pos/icon/info.svg", 18, 18);
                        break;
                }

                // Create notification item
                JPanel notifItem = new JPanel(new BorderLayout(8, 0));
                notifItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                notifItem.setBackground(Color.WHITE);
                notifItem.setPreferredSize(new Dimension(450, 40));

                // Icon on the left
                JLabel iconLabel = new JLabel(icon);
                notifItem.add(iconLabel, BorderLayout.WEST);

                // Message and time in the center
                JPanel textPanel = new JPanel(new BorderLayout());
                textPanel.setOpaque(false);

                JLabel msgLabel = new JLabel("<html><div style='width: 400px;'>" + message + "</div></html>");
                msgLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));

                // Format time
                String formattedTime = formatNotificationTime(time);
                JLabel timeLabel = new JLabel(formattedTime);
                timeLabel.setFont(new Font("Nunito SemiBold", Font.ITALIC, 10));
                timeLabel.setForeground(Color.GRAY);

                textPanel.add(msgLabel, BorderLayout.CENTER);
                textPanel.add(timeLabel, BorderLayout.SOUTH);

                notifItem.add(textPanel, BorderLayout.CENTER);

                // Make panel clickable to mark as read
                notifItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                final int notifId = rs.getInt("id");
                notifItem.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        markNotificationAsRead(notifId);
                        notificationPopup.setVisible(false);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        notifItem.setBackground(new Color(0xF0F8FF));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        notifItem.setBackground(Color.WHITE);
                    }
                });

                notificationPopup.add(notifItem);
            }

            // Handle empty notifications
            if (count == 0) {
                JLabel emptyLabel = new JLabel("No new notifications");
                emptyLabel.setFont(new Font("Nunito SemiBold", Font.ITALIC, 12));
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 55));
                emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                emptyLabel.setForeground(new Color(0x666666));
                notificationPopup.add(emptyLabel);
            }

            // Show badge with count
            if (count > 0) {
                notificationBadge.setText(String.valueOf(count));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }

            // Refresh popup
            notificationPopup.revalidate();
            notificationPopup.repaint();

        } catch (SQLException e) {

            notificationBadge.setVisible(false);
        }
    }

    private void showNotifications() {
        loadUnreadNotifications();

        notificationPopup.removeAll();

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
        headerPanel.setBackground(new Color(0xF8F9FA));

        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        titleLabel.setForeground(new Color(0x333333));

        // ❌ Close icon
        final FlatSVGIcon closeIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 16, 16);
        closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return new Color(0x666666);
            }
        });

        JButton closeButton = new JButton(closeIcon);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setToolTipText("Close");

        // Hover effect
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return Color.RED;
                    }
                });
                closeButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return new Color(0x666666);
                    }
                });
                closeButton.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                notificationPopup.setVisible(false);
                // ✅ Mark as read when ❌ clicked
                markAllNotificationsAsRead();
            }
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeButton, BorderLayout.EAST);

        notificationPopup.add(headerPanel);
        notificationPopup.add(new JSeparator());

        // Load notifications list
        loadUnreadNotifications();

        // Show popup
        Point buttonLoc = bellBtn.getLocationOnScreen();
        int x = buttonLoc.x - (notificationPopup.getPreferredSize().width - bellBtn.getWidth()) / 2;
        int y = buttonLoc.y + bellBtn.getHeight();

        notificationPopup.setLocation(x, y);
        notificationPopup.setVisible(true);
        requestFocus();
    }

    private void markAllNotificationsAsRead() {
        try {
            MySQL.executeIUD("UPDATE notifocation SET is_read = 0 WHERE is_read = 1");
            loadUnreadNotifications();
        } catch (Exception e) {
            System.err.println("Error marking notifications as read: " + e.getMessage());
        }
    }

    private void markNotificationAsRead(int id) {
        try {
            MySQL.executeIUD("UPDATE notifocation SET is_read = 0 WHERE id = " + id);
            loadUnreadNotifications();
        } catch (Exception e) {

        }
    }

    private String formatNotificationTime(String dbTime) {
        try {
            if (dbTime == null || dbTime.trim().isEmpty()) {
                return "Unknown time";
            }

            SimpleDateFormat dbFormat;
            if (dbTime.contains("T")) {
                dbFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            } else if (dbTime.contains("-")) {
                dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            } else {
                return dbTime;
            }

            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, HH:mm");
            Date date = dbFormat.parse(dbTime);
            return displayFormat.format(date);

        } catch (Exception e) {
            if (dbTime != null && dbTime.length() > 16) {
                return dbTime.substring(5, 16);
            }
            return dbTime;
        }
    }

    private void startClockTimer() {
        updateTimeLabel();
        clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimeLabel();
            }
        });
        clockTimer.start();
    }

    private void updateTimeLabel() {
        Date now = new Date();
        String currentTime = timeFormat.format(now);
        time.setText(currentTime);
    }

    private void setupNavButtonWithHoverText(JButton button, FlatSVGIcon icon, String hoverText) {
        // Set initial icon with default color
        icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return new Color(0x666666); // Gray color for nav icons
            }
        });

        // Center the icon in the button
        button.setIcon(icon);
        button.setText(""); // No text initially
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);

        // Create a custom tooltip that appears instantly on hover
        button.addMouseListener(new MouseAdapter() {
            private JWindow hoverWindow;
            private JLabel hoverLabel;

            @Override
            public void mouseEntered(MouseEvent e) {
                // Change icon color on hover
                icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return new Color(0x12B5A6); // Green color on hover
                    }
                });
                button.repaint();

                // Create and show hover text window
                showHoverText(button, hoverText);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Reset icon color
                icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return new Color(0x666666); // Back to gray
                    }
                });
                button.repaint();

                // Hide hover text
                hideHoverText();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                hideHoverText();
            }

            private void showHoverText(JButton button, String text) {
                if (hoverWindow != null) {
                    hoverWindow.dispose();
                }

                // Create a transparent window for the hover text
                hoverWindow = new JWindow();
                hoverWindow.setFocusableWindowState(false);
                hoverWindow.setBackground(new Color(0, 0, 0, 0));

                // Create the hover label
                hoverLabel = new JLabel(text);
                hoverLabel.setFont(new Font("Nunitu SemiBold", Font.PLAIN, 11));
                hoverLabel.setForeground(Color.WHITE);
                hoverLabel.setBackground(new Color(0x333333));
                hoverLabel.setOpaque(true);
                hoverLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x666666)),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));

                hoverWindow.add(hoverLabel);
                hoverWindow.pack();

                // Position the hover text BELOW the button
                Point buttonLoc = button.getLocationOnScreen();
                int x = buttonLoc.x + (button.getWidth() - hoverWindow.getWidth()) / 2;
                int y = buttonLoc.y + button.getHeight() + 5; // Position below the button

                hoverWindow.setLocation(x, y);
                hoverWindow.setVisible(true);
            }

            private void hideHoverText() {
                if (hoverWindow != null) {
                    hoverWindow.dispose();
                    hoverWindow = null;
                }
            }
        });
    }

    private void setupMenuButtonForSidebar() {
        // Initial icon color for menu button (sidebar control)
        menuIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return new Color(0x666666); // Gray color by default
            }
        });
        menuBtn.setIcon(menuIcon);
        menuBtn.setContentAreaFilled(false);
        menuBtn.setFocusPainted(false);
        menuBtn.setBorderPainted(false);
        menuBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuBtn.setOpaque(false);

        // Add hover effect for menu button
        menuBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menuIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return new Color(0x12B5A6); // Green color on hover
                    }
                });
                menuBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menuIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return new Color(0x666666); // Back to gray
                    }
                });
                menuBtn.repaint();
            }
        });
    }

    private void loadPanels() {
        // Initialize CardLayout
        this.contentPanelLayout = new CardLayout();
        cardPanel.setLayout(contentPanelLayout);

        // Initialize panels
        this.dashboardPanel = new DashboardPanel();
        this.posPanel = new PosPanel();
        this.supplierPanel = new SupplierPanel();
        this.salesPanel = new SalesPanel();
        this.customerManagementPanel = new CustomerManagement();
        this.productPanel = new ProductPanel();

        // Add panels to card layout
        this.cardPanel.add(dashboardPanel, "dashboard_panel");
        this.cardPanel.add(posPanel, "pos_panel");
        this.cardPanel.add(supplierPanel, "supplier_panel");
        this.cardPanel.add(salesPanel, "sales_panel");
        this.cardPanel.add(customerManagementPanel, "customer_management_panel");
        this.cardPanel.add(productPanel, "product_panel");

        SwingUtilities.updateComponentTreeUI(cardPanel);
    }

    private void setActiveButton(JButton button) {
        // Reset all navigation buttons to normal first
        resetAllButtonsToNormal();

        // Set new active button (only if it's not sign out button)
        if (button != signOutBtn) {
            activeButton = button;
            setButtonToActive(button);
            updateShortcutIconVisibility();
        }
    }

    private void resetAllButtonsToNormal() {
        resetButtonToNormal(dashboardBtn);
        resetButtonToNormal(posBtn);
        resetButtonToNormal(supplierBtn);
        resetButtonToNormal(salesBtn);
        resetButtonToNormal(creditBtn);
        resetButtonToNormal(stockBtn);
        resetButtonToNormal(calBtn);
        resetButtonToNormal(signOutBtn);
    }

    private void resetButtonToNormal(JButton button) {
        if (button == signOutBtn) {
            button.setForeground(Color.RED);
            FlatSVGIcon icon = getButtonIcon(button);
            if (icon != null) {
                icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return Color.RED;
                    }
                });
            }
        } else {
            button.setForeground(normalTextColor);
            FlatSVGIcon icon = getButtonIcon(button);
            if (icon != null) {
                icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return normalTextColor;
                    }
                });
            }
        }
        buttonHoverStates.put(button, false);
        button.repaint();
    }

    private void setButtonToActive(JButton button) {
        if (button == signOutBtn) {
            return; // Skip sign out button
        }
        button.setForeground(Color.WHITE);
        // Set icon color to white
        FlatSVGIcon icon = getButtonIcon(button);
        if (icon != null) {
            icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                @Override
                public Color filter(Color color) {
                    return Color.WHITE;
                }
            });
        }
        button.repaint();
    }

    private FlatSVGIcon getButtonIcon(JButton button) {
        if (button == dashboardBtn) {
            return dashboardIcon;
        }
        if (button == posBtn) {
            return posIcon;
        }
        if (button == supplierBtn) {
            return supplierIcon;
        }
        if (button == salesBtn) {
            return salesIcon;
        }
        if (button == creditBtn) {
            return creditIcon;
        }
        if (button == stockBtn) {
            return stockIcon;
        }
        if (button == signOutBtn) {
            return signOutIcon;
        }
        return null;
    }

    private void showDashboardPanel() {
        contentPanelLayout.show(cardPanel, "dashboard_panel");
        setActiveButton(dashboardBtn);
        updateShortcutIconVisibility();
    }

    private void showPOSPanel() {
        contentPanelLayout.show(cardPanel, "pos_panel");
        setActiveButton(posBtn);
        updateShortcutIconVisibility();
    }

    private void showSupplierPanel() {
        contentPanelLayout.show(cardPanel, "supplier_panel");
        setActiveButton(supplierBtn);
        updateShortcutIconVisibility();
    }

    private void showSalesPanel() {
        contentPanelLayout.show(cardPanel, "sales_panel");
        setActiveButton(salesBtn);
        updateShortcutIconVisibility();
    }

    private void showCustomerManagementPanel() {
        contentPanelLayout.show(cardPanel, "customer_management_panel");
        setActiveButton(creditBtn);
        updateShortcutIconVisibility();
    }

    private void showProductPanel() {
        contentPanelLayout.show(cardPanel, "product_panel");
        setActiveButton(stockBtn);
        updateShortcutIconVisibility();
    }

    private void setupHoverButton(JButton button, FlatSVGIcon icon, Color normalTextColor, Color hoverTopColor, Color hoverBottomColor) {
        // Set initial state
        icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return normalTextColor;
            }
        });
        button.setIcon(icon);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(normalTextColor);
        button.setOpaque(true);

        // Custom UI for gradient background with hover support
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                int w = c.getWidth();
                int h = c.getHeight();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top, bottom;
                if (button == activeButton && button != signOutBtn) {
                    // Active state - always show gradient (except for sign out)
                    top = activeTopColor;
                    bottom = activeBottomColor;
                } else if (Boolean.TRUE.equals(buttonHoverStates.get(button))) {
                    // Hover state - show hover gradient
                    top = hoverTopColor;
                    bottom = hoverBottomColor;
                } else {
                    // Normal state - white background
                    top = Color.WHITE;
                    bottom = Color.WHITE;
                }
                g2.setPaint(new GradientPaint(0, 0, top, w, 0, bottom));
                g2.fillRect(0, 0, w, h);
                // Paint the button content
                super.paint(g, c);
            }
        });

        // Mouse listener for hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != activeButton || button == signOutBtn) {
                    buttonHoverStates.put(button, true);
                    button.setForeground(Color.WHITE);
                    icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                        @Override
                        public Color filter(Color color) {
                            return Color.WHITE;
                        }
                    });
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != activeButton || button == signOutBtn) {
                    buttonHoverStates.put(button, false);
                    button.setForeground(normalTextColor);
                    icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                        @Override
                        public Color filter(Color color) {
                            return normalTextColor;
                        }
                    });
                    button.repaint();
                }
            }
        });
    }

    private void initSidebarSlider() {
        slideTimer = new Timer(SLIDE_DELAY, e -> {
            int currentWidth = sidePenal.getWidth();
            if (animationOpening) {
                // Expanding
                if (currentWidth < animationTargetWidth) {
                    int next = Math.min(currentWidth + STEP_SIZE, animationTargetWidth);
                    sidePenal.setPreferredSize(new Dimension(next, sidePenal.getHeight()));
                    penal1.revalidate();
                    penal1.repaint();
                    // Show button text dynamically during expansion
                    if (next > 100) {
                        setButtonTextVisible(true);
                    }
                } else {
                    slideTimer.stop();
                    isSidebarExpanded = true;
                    setButtonTextVisible(true); // ensure fully visible at end
                    updateLogo();
                }
            } else {
                // Collapsing
                if (currentWidth > animationTargetWidth) {
                    int next = Math.max(currentWidth - STEP_SIZE, animationTargetWidth);
                    sidePenal.setPreferredSize(new Dimension(next, sidePenal.getHeight()));
                    penal1.revalidate();
                    penal1.repaint();
                    // Hide text immediately while collapsing
                    setButtonTextVisible(false);
                } else {
                    slideTimer.stop();
                    isSidebarExpanded = false;
                    updateLogo();
                }
            }
        });
    }

    private void setButtonTextVisible(boolean visible) {
        dashboardBtn.setText(visible ? " Dashboard" : "");
        posBtn.setText(visible ? " POS" : "");
        supplierBtn.setText(visible ? " Supplier" : "");
        salesBtn.setText(visible ? " Sales" : "");
        creditBtn.setText(visible ? " Credit Customers" : "");
        stockBtn.setText(visible ? " Stocks" : "");
        signOutBtn.setText(visible ? " Sign Out" : "");
    }

    private void updateLogo() {
        logo.setVerticalAlignment(SwingConstants.CENTER); // stays vertically centered
        logo.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 10)); // margin
        if (isSidebarExpanded) {
            // Expanded → big centered logo
            logo.setIcon(new FlatSVGIcon("lk/com/pos/img/pos_big_logo.svg", 180, 60));
            logo.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            // Collapsed → small logo aligned to start (left)
            logo.setIcon(new FlatSVGIcon("lk/com/pos/img/pos_small_logo_1.svg", 50, 47));
            logo.setHorizontalAlignment(SwingConstants.LEADING); // aligns to start (left)
        }
    }

    private void updateShortcutIconVisibility() {
        // Show shortcut icon only when POS panel is active, hide otherwise
        boolean isPOSPanelActive = (activeButton == posBtn);
        keyBtn.setVisible(isPOSPanelActive);

        // Also adjust the layout spacing when icon is hidden
        if (!isPOSPanelActive) {
            keyBtn.setPreferredSize(new Dimension(0, 40));
        } else {
            keyBtn.setPreferredSize(new Dimension(75, 40));
        }

        navPanel.revalidate();
        navPanel.repaint();
    }

    private void updateMenuIcon(boolean sidebarOpening) {
        try {
            if (sidebarOpening) {
                // Sidebar will open → show "close" icon
                menuIcon = new FlatSVGIcon("lk/com/pos/icon/sidebar-collapse.svg", 28, 28);
            } else {
                // Sidebar will close → show "open" icon
                menuIcon = new FlatSVGIcon("lk/com/pos/icon/sidebar-expand.svg", 28, 28);
            }
            menuBtn.setIcon(menuIcon); // Now applied to menuBtn instead of calBtn
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void typeString(Robot robot, String text) {
        for (char c : text.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                throw new RuntimeException();
            }
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        }
    }

    private void setupProfileDropdown() {
        profilePopup = new JPopupMenu();
        profilePopup.setFocusable(false);

        // Create menu items
        JMenuItem addUserItem = new JMenuItem("Add New User");
        JMenuItem editProfileItem = new JMenuItem("Edit Profile");

        // Set fonts and styles
        Font menuFont = new Font("Nunito SemiBold", Font.PLAIN, 13);
        addUserItem.setFont(menuFont);
        editProfileItem.setFont(menuFont);

        // Set icons for menu items
        FlatSVGIcon addUserIcon = new FlatSVGIcon("lk/com/pos/icon/user-add.svg", 16, 16);
        FlatSVGIcon editProfileIcon = new FlatSVGIcon("lk/com/pos/icon/user-edit.svg", 16, 16);

        // Set initial icon colors
        addUserIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return new Color(0x666666);
            }
        });
        editProfileIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return new Color(0x666666);
            }
        });

        addUserItem.setIcon(addUserIcon);
        editProfileItem.setIcon(editProfileIcon);

        // Set initial backgrounds and make sure they're opaque
        addUserItem.setBackground(Color.WHITE);
        editProfileItem.setBackground(Color.WHITE);
        addUserItem.setForeground(Color.BLACK);
        editProfileItem.setForeground(Color.BLACK);
        addUserItem.setOpaque(true);
        editProfileItem.setOpaque(true);

        // FIXED: Use a single MouseAdapter instance for both items to track state properly
        MouseAdapter menuItemHoverHandler = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                // Force set colors directly
                item.setBackground(new Color(0xE8F4FD));
                item.setForeground(new Color(0x12B5A6));

                // Update icon color
                Icon icon = item.getIcon();
                if (icon instanceof FlatSVGIcon) {
                    FlatSVGIcon svgIcon = (FlatSVGIcon) icon;
                    svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                        @Override
                        public Color filter(Color color) {
                            return new Color(0x12B5A6);
                        }
                    });
                }
                item.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                // Force reset colors directly
                item.setBackground(Color.WHITE);
                item.setForeground(Color.BLACK);

                // Reset icon color
                Icon icon = item.getIcon();
                if (icon instanceof FlatSVGIcon) {
                    FlatSVGIcon svgIcon = (FlatSVGIcon) icon;
                    svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                        @Override
                        public Color filter(Color color) {
                            return new Color(0x666666);
                        }
                    });
                }
                item.repaint();
            }
        };

        // Add hover listeners to both items
        addUserItem.addMouseListener(menuItemHoverHandler);
        editProfileItem.addMouseListener(menuItemHoverHandler);

        // Add action listeners
        addUserItem.addActionListener(e -> {
            addNewUser();
        });

        editProfileItem.addActionListener(e -> {
            editProfile();
        });

        // Add items to popup menu
        profilePopup.add(addUserItem);
        profilePopup.add(editProfileItem);

        // Setup profile button to toggle popup on click
        profileBtn.addActionListener(e -> toggleProfilePopup());

        // FIXED: Add proper popup listener to reset everything when popup opens
        profilePopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // Reset ALL properties when popup opens
                resetAllMenuItemsCompletely();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // Reset ALL properties when popup closes
                resetAllMenuItemsCompletely();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                resetAllMenuItemsCompletely();
            }
        });
    }

    // FIXED: Completely reset all menu item properties
    private void resetAllMenuItemsCompletely() {
        for (Component comp : profilePopup.getComponents()) {
            if (comp instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) comp;

                // Reset background and foreground
                item.setBackground(Color.WHITE);
                item.setForeground(Color.BLACK);

                // Reset icon color
                Icon icon = item.getIcon();
                if (icon instanceof FlatSVGIcon) {
                    FlatSVGIcon svgIcon = (FlatSVGIcon) icon;
                    svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                        @Override
                        public Color filter(Color color) {
                            return new Color(0x666666);
                        }
                    });
                }

                // Force repaint
                item.repaint();
            }
        }
    }

    private void toggleProfilePopup() {
        if (profilePopup != null) {
            if (profilePopup.isVisible()) {
                profilePopup.setVisible(false);
            } else {
                showProfilePopup();
            }
        }
    }

    private void showProfilePopup() {
        if (profilePopup != null) {
            // Reset everything before showing
            resetAllMenuItemsCompletely();

            // Calculate position relative to the profile button
            Point buttonLoc = profileBtn.getLocationOnScreen();
            int x = buttonLoc.x - (profilePopup.getPreferredSize().width - profileBtn.getWidth()) / 2;
            int y = buttonLoc.y + profileBtn.getHeight();

            profilePopup.setLocation(x, y);
            profilePopup.setVisible(true);
            profilePopup.setInvoker(profileBtn);
        }
    }

    private void addNewUser() {
        // From your main form or any other component
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        AddNewUser dialog = new AddNewUser(parentFrame, true);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        if (profilePopup != null) {
            profilePopup.setVisible(false);
        }
    }

    private void editProfile() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EditProfile dialog = new EditProfile(parentFrame, true, userId);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        // Close the popup
        if (profilePopup != null) {
            profilePopup.setVisible(false);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        penal1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cardPanel = new javax.swing.JPanel();
        navPanel = new javax.swing.JPanel();
        calBtn = new javax.swing.JButton();
        menuBtn = new javax.swing.JButton();
        bellBtn = new javax.swing.JButton();
        profileBtn = new javax.swing.JButton();
        time = new javax.swing.JLabel();
        keyBtn = new javax.swing.JButton();
        helloLabel = new javax.swing.JLabel();
        sidePenal = new javax.swing.JPanel();
        dashboardBtn = new javax.swing.JButton();
        posBtn = new javax.swing.JButton();
        supplierBtn = new javax.swing.JButton();
        salesBtn = new javax.swing.JButton();
        creditBtn = new javax.swing.JButton();
        stockBtn = new javax.swing.JButton();
        signOutBtn = new javax.swing.JButton();
        logo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Home Screen");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        penal1.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setAlignmentX(0.0F);
        jScrollPane1.setAlignmentY(0.0F);

        cardPanel.setBackground(new java.awt.Color(153, 153, 255));
        cardPanel.setLayout(new java.awt.CardLayout());
        jScrollPane1.setViewportView(cardPanel);

        calBtn.setBackground(new java.awt.Color(102, 0, 255));
        calBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        calBtn.setForeground(new java.awt.Color(204, 204, 204));
        calBtn.setBorder(null);
        calBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        calBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        calBtn.setIconTextGap(3);
        calBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        calBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        calBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calBtnActionPerformed(evt);
            }
        });

        menuBtn.setBackground(new java.awt.Color(102, 0, 255));
        menuBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        menuBtn.setBorder(null);
        menuBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        menuBtn.setIconTextGap(3);
        menuBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        menuBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        menuBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuBtnActionPerformed(evt);
            }
        });

        bellBtn.setBackground(new java.awt.Color(102, 0, 255));
        bellBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        bellBtn.setBorder(null);
        bellBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bellBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        bellBtn.setIconTextGap(3);
        bellBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        bellBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        bellBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bellBtnActionPerformed(evt);
            }
        });

        profileBtn.setBackground(new java.awt.Color(102, 0, 255));
        profileBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        profileBtn.setBorder(null);
        profileBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        profileBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        profileBtn.setIconTextGap(3);
        profileBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        profileBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        profileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileBtnActionPerformed(evt);
            }
        });

        time.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        time.setText("2024/10.20 10.25.30");

        keyBtn.setBackground(new java.awt.Color(102, 0, 255));
        keyBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        keyBtn.setBorder(null);
        keyBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        keyBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        keyBtn.setIconTextGap(3);
        keyBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        keyBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        keyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyBtnActionPerformed(evt);
            }
        });

        helloLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        helloLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        javax.swing.GroupLayout navPanelLayout = new javax.swing.GroupLayout(navPanel);
        navPanel.setLayout(navPanelLayout);
        navPanelLayout.setHorizontalGroup(
            navPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menuBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(helloLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(keyBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(calBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bellBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(time)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(profileBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        navPanelLayout.setVerticalGroup(
            navPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(navPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(helloLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(navPanelLayout.createSequentialGroup()
                        .addGroup(navPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(menuBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(navPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(profileBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(bellBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(calBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(time, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(keyBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        sidePenal.setBackground(new java.awt.Color(255, 255, 255));

        dashboardBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        dashboardBtn.setText(" Dashboard");
        dashboardBtn.setBorder(null);
        dashboardBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        dashboardBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        dashboardBtn.setIconTextGap(3);
        dashboardBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        dashboardBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        dashboardBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardBtnActionPerformed(evt);
            }
        });

        posBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        posBtn.setText(" POS");
        posBtn.setBorder(null);
        posBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        posBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        posBtn.setIconTextGap(3);
        posBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        posBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        posBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posBtnActionPerformed(evt);
            }
        });

        supplierBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        supplierBtn.setText(" Supplier");
        supplierBtn.setBorder(null);
        supplierBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        supplierBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        supplierBtn.setIconTextGap(3);
        supplierBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        supplierBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        supplierBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supplierBtnActionPerformed(evt);
            }
        });

        salesBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        salesBtn.setText(" Sales");
        salesBtn.setBorder(null);
        salesBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        salesBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        salesBtn.setIconTextGap(3);
        salesBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        salesBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        salesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salesBtnActionPerformed(evt);
            }
        });

        creditBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        creditBtn.setText(" Credit Custemers");
        creditBtn.setBorder(null);
        creditBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        creditBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        creditBtn.setIconTextGap(3);
        creditBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        creditBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        creditBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creditBtnActionPerformed(evt);
            }
        });

        stockBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        stockBtn.setText(" Stocks");
        stockBtn.setBorder(null);
        stockBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        stockBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        stockBtn.setIconTextGap(3);
        stockBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        stockBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        stockBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stockBtnActionPerformed(evt);
            }
        });

        signOutBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        signOutBtn.setForeground(new java.awt.Color(255, 0, 0));
        signOutBtn.setText(" Sign Out");
        signOutBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 51, 51)));
        signOutBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        signOutBtn.setHideActionText(true);
        signOutBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        signOutBtn.setIconTextGap(3);
        signOutBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        signOutBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        signOutBtn.setVerifyInputWhenFocusTarget(false);
        signOutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signOutBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sidePenalLayout = new javax.swing.GroupLayout(sidePenal);
        sidePenal.setLayout(sidePenalLayout);
        sidePenalLayout.setHorizontalGroup(
            sidePenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(signOutBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(sidePenalLayout.createSequentialGroup()
                .addGroup(sidePenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dashboardBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(posBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(supplierBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(salesBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(creditBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(stockBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(logo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sidePenalLayout.setVerticalGroup(
            sidePenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidePenalLayout.createSequentialGroup()
                .addComponent(logo, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dashboardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(posBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(salesBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(creditBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(supplierBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 115, Short.MAX_VALUE)
                .addComponent(signOutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        javax.swing.GroupLayout penal1Layout = new javax.swing.GroupLayout(penal1);
        penal1.setLayout(penal1Layout);
        penal1Layout.setHorizontalGroup(
            penal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, penal1Layout.createSequentialGroup()
                .addComponent(sidePenal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(penal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 792, Short.MAX_VALUE)
                    .addComponent(navPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        penal1Layout.setVerticalGroup(
            penal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(penal1Layout.createSequentialGroup()
                .addComponent(navPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1))
            .addComponent(sidePenal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(penal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(penal1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void supplierBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supplierBtnActionPerformed
        showSupplierPanel();
    }//GEN-LAST:event_supplierBtnActionPerformed

    private void calBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calBtnActionPerformed
        try {
            boolean calculatorOpened = false;

            try {
                Process process = Runtime.getRuntime().exec("calc.exe");
                if (process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    if (process.exitValue() == 0) {
                        calculatorOpened = true;
                    }
                } else {
                    calculatorOpened = true;
                }
            } catch (Exception e) {
            }

            if (!calculatorOpened) {
                try {
                    String[] powerShellCommands = {
                        "powershell -Command \"Start-Process calc -WindowStyle Normal\"",
                        "powershell -Command \"Start-Process 'C:\\Windows\\System32\\calc.exe'\"",};

                    for (String cmd : powerShellCommands) {
                        try {
                            Process process = Runtime.getRuntime().exec(cmd);
                            if (process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                                calculatorOpened = true;
                                break;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                } catch (Exception e) {
                }
            }

            if (!calculatorOpened) {
                try {
                    String[] cmdCommands = {
                        "cmd /c start calc",
                        "cmd /c start C:\\Windows\\System32\\calc.exe",
                        "start calc.exe"
                    };

                    for (String cmd : cmdCommands) {
                        try {
                            Process process = Runtime.getRuntime().exec(cmd);
                            Thread.sleep(1000);
                            calculatorOpened = true;
                            break;
                        } catch (Exception e) {
                            continue;
                        }
                    }
                } catch (Exception e) {
                }
            }

            if (!calculatorOpened) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("calc.exe");
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    Thread.sleep(1500);
                    if (process.isAlive()) {
                        calculatorOpened = true;
                    }
                } catch (Exception e) {
                }
            }

            if (!calculatorOpened) {
                try {
                    Robot robot = new Robot();
                    robot.keyPress(KeyEvent.VK_WINDOWS);
                    robot.keyPress(KeyEvent.VK_R);
                    robot.keyRelease(KeyEvent.VK_R);
                    robot.keyRelease(KeyEvent.VK_WINDOWS);
                    Thread.sleep(500);
                    typeString(robot, "calc");
                    Thread.sleep(300);
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                    calculatorOpened = true;
                } catch (Exception e) {
                }
            }

            if (!calculatorOpened) {
                try {
                    String systemRoot = System.getenv("SystemRoot");
                    String calcPath = systemRoot + "\\System32\\calc.exe";
                    File calcFile = new File(calcPath);
                    if (calcFile.exists()) {
                        Process process = Runtime.getRuntime().exec("\"" + calcPath + "\"");
                        Thread.sleep(2000);
                        if (process.isAlive()) {
                            calculatorOpened = true;
                        }
                    }
                } catch (Exception e) {
                }
            }

        } catch (Exception ex) {
        }
    }//GEN-LAST:event_calBtnActionPerformed

    private void stockBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stockBtnActionPerformed
        showProductPanel();
    }//GEN-LAST:event_stockBtnActionPerformed

    private void salesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salesBtnActionPerformed
        showSalesPanel();
    }//GEN-LAST:event_salesBtnActionPerformed

    private void creditBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditBtnActionPerformed
        showCustomerManagementPanel();
    }//GEN-LAST:event_creditBtnActionPerformed

    private void posBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posBtnActionPerformed
        showPOSPanel();
    }//GEN-LAST:event_posBtnActionPerformed

    private void dashboardBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardBtnActionPerformed
        showDashboardPanel();
    }//GEN-LAST:event_dashboardBtnActionPerformed

    private void menuBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBtnActionPerformed
        if (slideTimer.isRunning()) {
            // Ignore repeated clicks
            return;
        }

        if (isSidebarExpanded) {
            // Start collapsing
            animationOpening = false;
            animationTargetWidth = SIDEBAR_WIDTH_COLLAPSED;
            setButtonTextVisible(false); // hide text immediately
        } else {
            // Start expanding
            animationOpening = true;
            animationTargetWidth = SIDEBAR_WIDTH_EXPANDED;
            // Text will be shown after fully expanded
        }

        // Update menu icon immediately
        updateMenuIcon(!isSidebarExpanded);

        slideTimer.start();
    }//GEN-LAST:event_menuBtnActionPerformed

    private void bellBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bellBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bellBtnActionPerformed

    private void profileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_profileBtnActionPerformed

    private void keyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keyBtnActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowActivated

    private void signOutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signOutBtnActionPerformed

//        Session session = Session.getInstance();
//
//        if (session.getUserId() == 0 || session.getRoleName() == null) {
//            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "No active session found! You are not logged in.");
//            return;
//        }
//
//        int option = JOptionPane.showConfirmDialog(
//                this,
//                "Are you sure you want to log out, " + session.getRoleName() + "?",
//                "Confirm Logout",
//                JOptionPane.YES_NO_OPTION,
//                JOptionPane.QUESTION_MESSAGE
//        );
//
//        if (option == JOptionPane.YES_OPTION) {
//            try {
//                // Get user details for logout message
//                String userName = "";
//                String roleName = session.getRoleName();
//
//                // Get username from database
//                ResultSet rs = MySQL.executeSearch("SELECT name FROM user WHERE user_id = " + session.getUserId());
//                if (rs.next()) {
//                    userName = rs.getString("name");
//                }
//                rs.close();
//
//                // Create logout success message with username and role
//                String logoutMessage = userName + "(" + roleName + ") logged out successfully";
//                int massageId = 0;
//
//                // Check if message already exists in massage table
//                ResultSet checkRs = MySQL.executeSearch("SELECT massage_id FROM massage WHERE massage = '" + logoutMessage + "'");
//                if (checkRs.next()) {
//                    // Message exists, get the existing massage_id
//                    massageId = checkRs.getInt("massage_id");
//
//                } else {
//                    // Message doesn't exist, insert new message
//                    MySQL.executeIUD("INSERT INTO massage (massage) VALUES ('" + logoutMessage + "')");
//
//                    // Get the generated massage_id
//                    ResultSet generatedRs = MySQL.executeSearch("SELECT LAST_INSERT_ID() as new_id");
//                    if (generatedRs.next()) {
//                        massageId = generatedRs.getInt("new_id");
//
//                    }
//                    generatedRs.close();
//                }
//                checkRs.close();
//
//                // Insert into notifocation table
//                if (massageId > 0) {
//                    MySQL.executeIUD("INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (1, NOW(), 3, " + massageId + ")");
//
//                }
//
//                // Clear session
//                session.clear();
//
//                // Stop timers
//                if (clockTimer != null && clockTimer.isRunning()) {
//                    clockTimer.stop();
//                }
//
//                // Show success message
//                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Logged out successfully!");
//
//                // Close current window
//                this.dispose();
//
//                // Exit application
//                System.exit(0);
//
//            } catch (SQLException e) {
//
//                e.printStackTrace();
//
//                // Clear session even if notification fails
//                session.clear();
//
//                // Stop timers
//                if (clockTimer != null && clockTimer.isRunning()) {
//                    clockTimer.stop();
//                }
//
//                // Show success message
//                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Logged out successfully!");
//
//                // Close current window
//                this.dispose();
//
//                // Exit application
//                System.exit(0);
//            }
//        } else {
//            // User cancelled logout
//            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Logout cancelled.");
//        }
    }//GEN-LAST:event_signOutBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatLightLaf.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
              //  Session session = Session.getInstance();
               // if (session.getUserId() == 0 || session.getRoleName() == null) {
                    // Session invalid, go directly to login
               //     new LogIn().setVisible(true);
                //} else {
                    // Session valid, create HomeScreen
                    new HomeScreen().setVisible(true);
                //}
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bellBtn;
    private javax.swing.JButton calBtn;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JButton creditBtn;
    private javax.swing.JButton dashboardBtn;
    private javax.swing.JLabel helloLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton keyBtn;
    private javax.swing.JLabel logo;
    private javax.swing.JButton menuBtn;
    private javax.swing.JPanel navPanel;
    private javax.swing.JPanel penal1;
    private javax.swing.JButton posBtn;
    private javax.swing.JButton profileBtn;
    private javax.swing.JButton salesBtn;
    private javax.swing.JPanel sidePenal;
    private javax.swing.JButton signOutBtn;
    private javax.swing.JButton stockBtn;
    private javax.swing.JButton supplierBtn;
    private javax.swing.JLabel time;
    // End of variables declaration//GEN-END:variables
}
