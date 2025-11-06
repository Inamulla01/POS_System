/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package lk.com.pos.panel;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.privateclasses.RoundedPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;

/**
 * Enhanced Notification Panel for POS System
 * @author pasin
 */
public class NotificationPanel extends javax.swing.JPanel {

    // Data
    private ArrayList<Notification> notifications;
    private String currentFilter = "all";
    
    // Icons
    private FlatSVGIcon deleteIcon;
    private FlatSVGIcon lowStockIcon;
    private FlatSVGIcon calendarIcon;
    private FlatSVGIcon creditCardIcon;
    private FlatSVGIcon expiredIcon;
    private FlatSVGIcon bellIcon;
    
    // Database connection details - UPDATE THESE
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pos_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Pasindu@2005";

    /**
     * Creates new form NotificationPanel
     */
    public NotificationPanel() {
        notifications = new ArrayList<>();
        initIcons();
        initComponents();
        init();
    }
    
    /**
     * Initialize icons
     */
    private void initIcons() {
        try {
            deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 18, 18);
            lowStockIcon = new FlatSVGIcon("lk/com/pos/icon/down-arrow.svg", 18, 18);
            calendarIcon = new FlatSVGIcon("lk/com/pos/icon/calander.svg", 18, 18);
            creditCardIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 18, 18);
            expiredIcon = new FlatSVGIcon("lk/com/pos/icon/expired.svg", 18, 18);
            bellIcon = new FlatSVGIcon("lk/com/pos/icon/bell.svg", 18, 18);
        } catch (Exception e) {
            System.err.println("Error loading icons: " + e.getMessage());
        }
    }
    
    /**
     * Initialize the panel after NetBeans initComponents
     */
    private void init() {
        // Setup the main panel
        jPanel1.setBackground(new Color(249, 250, 251));
        jPanel2.setBackground(Color.WHITE);
        jPanel6.setBackground(new Color(249, 250, 251));
        
        // Setup header buttons
        setupFilterButtons();
        
        // Setup scroll pane
        jScrollPane1.setBorder(null);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane1.setBackground(new Color(249, 250, 251));
        
        // Setup Mark All as Read button
        jButton9.setBackground(new Color(79, 70, 229));
        jButton9.setForeground(Color.WHITE);
        jButton9.addActionListener(e -> markAllAsRead());
        
        // Load notifications from database
        loadNotifications();
    }
    
    /**
     * Setup filter buttons with proper styling and actions
     */
    private void setupFilterButtons() {
        // All button
        jButton1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jButton1.addActionListener(e -> applyFilter("all"));
        
        // Unread button
        jButton2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jButton2.addActionListener(e -> applyFilter("unread"));
        
        // Low Stock button
        jButton5.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jButton5.addActionListener(e -> applyFilter("low_stock"));
        
        // Expiring button
        jButton6.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jButton6.addActionListener(e -> applyFilter("expiring"));
        
        // Missed Due Date button
        jButton7.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jButton7.addActionListener(e -> applyFilter("missed_due_date"));
        
        // Expired button
        jButton8.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jButton8.addActionListener(e -> applyFilter("expired"));
        
        // Apply initial filter style
        updateFilterButtonStyles();
    }
    
    /**
     * Apply filter
     */
    private void applyFilter(String filter) {
        currentFilter = filter;
        updateFilterButtonStyles();
        displayNotifications();
    }
    
    /**
     * Update filter button styles based on current filter
     */
    private void updateFilterButtonStyles() {
        JButton[] buttons = {jButton1, jButton2, jButton5, jButton6, jButton7, jButton8};
        String[] filters = {"all", "unread", "low_stock", "expiring", "missed_due_date", "expired"};
        
        for (int i = 0; i < buttons.length; i++) {
            if (filters[i].equals(currentFilter)) {
                buttons[i].setBackground(new Color(79, 70, 229));
                buttons[i].setForeground(Color.WHITE);
            } else {
                buttons[i].setBackground(new Color(243, 244, 246));
                buttons[i].setForeground(new Color(55, 65, 81));
            }
        }
    }
    
    /**
     * Load notifications from database
     */
    private void loadNotifications() {
        notifications.clear();
        
        try (Connection conn = getConnection()) {
            String sql = "SELECT n.id, n.is_read, n.create_at, mt.msg_type, m.massage " +
                        "FROM notifocation n " +
                        "INNER JOIN msg_type mt ON n.msg_type_id = mt.msg_type_id " +
                        "INNER JOIN massage m ON n.massage_id = m.massage_id " +
                        "ORDER BY n.create_at DESC";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    Notification notif = new Notification();
                    notif.id = rs.getInt("id");
                    notif.isRead = rs.getInt("is_read") == 1;
                    notif.createAt = rs.getTimestamp("create_at");
                    notif.type = rs.getString("msg_type");
                    notif.message = rs.getString("massage");
                    
                    notifications.add(notif);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            loadSampleData();
        }
        
        displayNotifications();
        updateUnreadCount();
    }
    
    /**
     * Load sample data for testing
     */
    private void loadSampleData() {
        notifications.clear();
        
        Notification n1 = new Notification();
        n1.id = 1;
        n1.type = "Low Stock";
        n1.message = "Paracetamol 500mg has only 8 units remaining";
        n1.isRead = false;
        n1.createAt = new Timestamp(System.currentTimeMillis() - 5 * 60 * 1000);
        notifications.add(n1);
        
        Notification n2 = new Notification();
        n2.id = 2;
        n2.type = "Expiring";
        n2.message = "Amoxicillin 250mg will expire in 15 days";
        n2.isRead = false;
        n2.createAt = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        notifications.add(n2);
        
        Notification n3 = new Notification();
        n3.id = 3;
        n3.type = "Missed Due Date";
        n3.message = "Supplier 'Micro Labs Lanka' has Rs.36,000 outstanding payment";
        n3.isRead = false;
        n3.createAt = new Timestamp(System.currentTimeMillis() - 2 * 60 * 60 * 1000);
        notifications.add(n3);
        
        Notification n4 = new Notification();
        n4.id = 4;
        n4.type = "Expired";
        n4.message = "Vitamin C Tablets expired on 2025-10-28";
        n4.isRead = true;
        n4.createAt = new Timestamp(System.currentTimeMillis() - 3 * 60 * 60 * 1000);
        notifications.add(n4);
        
        Notification n5 = new Notification();
        n5.id = 5;
        n5.type = "Sale";
        n5.message = "Large sale completed - Rs.125,000 for Invoice #1234";
        n5.isRead = true;
        n5.createAt = new Timestamp(System.currentTimeMillis() - 5 * 60 * 60 * 1000);
        notifications.add(n5);
    }
    
    /**
     * Display notifications in the UI
     */
    private void displayNotifications() {
        jPanel6.removeAll();
        jPanel6.setLayout(new BoxLayout(jPanel6, BoxLayout.Y_AXIS));
        jPanel6.setBackground(new Color(249, 250, 251));
        jPanel6.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        ArrayList<Notification> filtered = getFilteredNotifications();
        
        if (filtered.isEmpty()) {
            JPanel emptyPanel = createEmptyStatePanel();
            jPanel6.add(emptyPanel);
        } else {
            for (Notification notif : filtered) {
                RoundedPanel notifPanel = createNotificationPanel(notif);
                jPanel6.add(notifPanel);
                jPanel6.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        jPanel6.revalidate();
        jPanel6.repaint();
    }
    
    /**
     * Get filtered notifications
     */
    private ArrayList<Notification> getFilteredNotifications() {
        ArrayList<Notification> filtered = new ArrayList<>();
        
        for (Notification notif : notifications) {
            if (shouldShowNotification(notif)) {
                filtered.add(notif);
            }
        }
        
        return filtered;
    }
    
    /**
     * Check if notification should be shown
     */
    private boolean shouldShowNotification(Notification notif) {
        if (currentFilter.equals("all")) return true;
        if (currentFilter.equals("unread")) return !notif.isRead;
        
        String type = notif.type.toLowerCase().replace(" ", "_");
        return currentFilter.equals(type);
    }
    
    /**
     * Create empty state panel
     */
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(60, 20, 60, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
        JLabel iconLabel = new JLabel("🔔");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JLabel titleLabel = new JLabel("No notifications");
        titleLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        JLabel msgLabel = new JLabel("You're all caught up!");
        msgLabel.setFont(new Font("Nunito", Font.PLAIN, 14));
        msgLabel.setForeground(new Color(107, 114, 128));
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(msgLabel);
        
        return panel;
    }
    
    /**
     * Create notification panel
     */
    private RoundedPanel createNotificationPanel(Notification notif) {
        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        
        Color borderColor = notif.isRead ? new Color(229, 231, 235) : getTypeColor(notif.type);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 2, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        
        // Icon panel
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setOpaque(true);
        iconPanel.setBackground(getTypeBackgroundColor(notif.type));
        iconPanel.setPreferredSize(new Dimension(48, 48));
        iconPanel.setBorder(BorderFactory.createLineBorder(getTypeColor(notif.type), 1, true));
        
        JLabel iconLabel = new JLabel(getEmojiForType(notif.type));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconPanel.add(iconLabel, BorderLayout.CENTER);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(Color.WHITE);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JLabel titleLabel = new JLabel(notif.type);
        titleLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        titleLabel.setForeground(notif.isRead ? new Color(55, 65, 81) : new Color(17, 24, 39));
        
        JButton deleteBtn = new JButton("✕");
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 14));
        deleteBtn.setForeground(new Color(239, 68, 68));
        deleteBtn.setPreferredSize(new Dimension(30, 30));
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deleteNotification(notif.id));
        
        titleRow.add(titleLabel, BorderLayout.WEST);
        titleRow.add(deleteBtn, BorderLayout.EAST);
        
        contentPanel.add(titleRow);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Message
        JLabel messageLabel = new JLabel("<html>" + notif.message + "</html>");
        messageLabel.setFont(new Font("Nunito", Font.PLAIN, 14));
        messageLabel.setForeground(notif.isRead ? new Color(107, 114, 128) : new Color(55, 65, 81));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Bottom row
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(Color.WHITE);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBottom.setBackground(Color.WHITE);
        
        JLabel timeLabel = new JLabel(formatTime(notif.createAt));
        timeLabel.setFont(new Font("Nunito", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(107, 114, 128));
        leftBottom.add(timeLabel);
        
        if (!notif.isRead) {
            JLabel unreadDot = new JLabel("●");
            unreadDot.setForeground(new Color(79, 70, 229));
            unreadDot.setFont(new Font("Arial", Font.BOLD, 10));
            leftBottom.add(unreadDot);
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        if (!notif.isRead) {
            JButton markReadBtn = new JButton("Mark as Read");
            markReadBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
            markReadBtn.setBackground(new Color(243, 244, 246));
            markReadBtn.setForeground(new Color(55, 65, 81));
            markReadBtn.setBorderPainted(false);
            markReadBtn.setFocusPainted(false);
            markReadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            markReadBtn.addActionListener(e -> markAsRead(notif.id));
            buttonPanel.add(markReadBtn);
        }
        
        JButton detailsBtn = new JButton("View Details");
        detailsBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        detailsBtn.setBackground(new Color(224, 231, 255));
        detailsBtn.setForeground(new Color(67, 56, 202));
        detailsBtn.setBorderPainted(false);
        detailsBtn.setFocusPainted(false);
        detailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailsBtn.addActionListener(e -> viewDetails(notif));
        buttonPanel.add(detailsBtn);
        
        bottomRow.add(leftBottom, BorderLayout.WEST);
        bottomRow.add(buttonPanel, BorderLayout.EAST);
        
        contentPanel.add(bottomRow);
        
        panel.add(iconPanel, BorderLayout.WEST);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Get emoji for type
     */
    private String getEmojiForType(String type) {
        type = type.toLowerCase();
        if (type.contains("stock") && !type.contains("arrival")) return "📉";
        if (type.contains("expiring")) return "⏰";
        if (type.contains("expired")) return "⚠️";
        if (type.contains("due") || type.contains("payment")) return "💳";
        if (type.contains("sale")) return "✅";
        if (type.contains("arrival")) return "📦";
        return "📅";
    }
    
    /**
     * Get color for type
     */
    private Color getTypeColor(String type) {
        type = type.toLowerCase();
        if (type.contains("stock")) return new Color(249, 115, 22);
        if (type.contains("expiring")) return new Color(234, 179, 8);
        if (type.contains("expired")) return new Color(239, 68, 68);
        if (type.contains("due") || type.contains("payment")) return new Color(168, 85, 247);
        if (type.contains("sale")) return new Color(34, 197, 94);
        return new Color(79, 70, 229);
    }
    
    /**
     * Get background color for type
     */
    private Color getTypeBackgroundColor(String type) {
        type = type.toLowerCase();
        if (type.contains("stock")) return new Color(255, 247, 237);
        if (type.contains("expiring")) return new Color(254, 252, 232);
        if (type.contains("expired")) return new Color(254, 242, 242);
        if (type.contains("due") || type.contains("payment")) return new Color(250, 245, 255);
        if (type.contains("sale")) return new Color(240, 253, 244);
        return new Color(238, 242, 255);
    }
    
    /**
     * Format timestamp
     */
    private String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "Unknown time";
        
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (minutes < 60) return minutes + " minutes ago";
        if (hours < 24) return hours + " hours ago";
        if (days < 7) return days + " days ago";
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(timestamp);
    }
    
    /**
     * Mark notification as read
     */
    private void markAsRead(int notifId) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE notifocation SET is_read = 1 WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, notifId);
                pstmt.executeUpdate();
            }
            
            for (Notification notif : notifications) {
                if (notif.id == notifId) {
                    notif.isRead = true;
                    break;
                }
            }
            
            displayNotifications();
            updateUnreadCount();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating notification: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Mark all as read
     */
    private void markAllAsRead() {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE notifocation SET is_read = 1";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.executeUpdate();
            }
            
            for (Notification notif : notifications) {
                notif.isRead = true;
            }
            
            displayNotifications();
            updateUnreadCount();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating notifications: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Delete notification
     */
    private void deleteNotification(int notifId) {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this notification?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = getConnection()) {
                String sql = "DELETE FROM notifocation WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, notifId);
                    pstmt.executeUpdate();
                }
                
                notifications.removeIf(n -> n.id == notifId);
                displayNotifications();
                updateUnreadCount();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting notification: " + e.getMessage(),
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * View notification details
     */
    private void viewDetails(Notification notif) {
        JOptionPane.showMessageDialog(this,
            "Type: " + notif.type + "\n" +
            "Message: " + notif.message + "\n" +
            "Time: " + formatTime(notif.createAt) + "\n" +
            "Status: " + (notif.isRead ? "Read" : "Unread"),
            "Notification Details",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Update unread count
     */
    private void updateUnreadCount() {
        long unreadCount = notifications.stream().filter(n -> !n.isRead).count();
        jButton2.setText("Unread (" + unreadCount + ")");
        jButton9.setEnabled(unreadCount > 0);
    }
    
    /**
     * Get database connection
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Notification data class
     */
    private static class Notification {
        int id;
        boolean isRead;
        Timestamp createAt;
        String type;
        String message;
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel6 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        cartProductPanelDeleteBtn1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();

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

        jButton9.setBackground(new java.awt.Color(102, 102, 255));
        jButton9.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton9.setText("Mark All as Read");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
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
                .addContainerGap(13, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7)
                    .addComponent(jButton8)
                    .addComponent(jButton9))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        cartProductPanelDeleteBtn1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        cartProductPanelDeleteBtn1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cartProductPanelDeleteBtn1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        jLabel1.setText("icon");

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel2.setText("Alert");

        jLabel3.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jLabel3.setText("Product or customer name");

        jLabel4.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jLabel4.setText("Date");

        jButton10.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        jButton10.setText("Mark as Read");

        jButton11.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        jButton11.setText("View Details");

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 453, Short.MAX_VALUE)
                        .addComponent(jButton10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cartProductPanelDeleteBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cartProductPanelDeleteBtn1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton10)
                        .addComponent(jButton11)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(310, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel6);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(15, 15, 15))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                .addGap(15, 15, 15))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cartProductPanelDeleteBtn1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    // End of variables declaration//GEN-END:variables
}
