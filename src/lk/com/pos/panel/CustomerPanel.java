package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.connection.MySQL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CustomerPanel extends javax.swing.JPanel {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

    public CustomerPanel() {
        initComponents();
        init();
        // Load due amount customers by default
        loadCustomers();
    }

    private void init() {
        // Set modern scrollbar styling
        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");

        // Set icons for buttons
        FlatSVGIcon blueEdit = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 20, 20);
        editBtn.setIcon(blueEdit);

        FlatSVGIcon redDelete = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 20, 20);
        deleteBtn.setIcon(redDelete);
        
        // Modern styling for search field
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search By Customer Name or NIC");
        jTextField1.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, 
            new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        
        // Add focus listeners for search field
        jTextField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (jTextField1.getText().equals("Search By Customer Name or NIC")) {
                    jTextField1.setText("");
                    jTextField1.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (jTextField1.getText().isEmpty()) {
                    jTextField1.setText("Search By Customer Name or NIC");
                    jTextField1.setForeground(Color.GRAY);
                }
            }
        });
        
        // Set initial text color for placeholder
        jTextField1.setForeground(Color.GRAY);
        
        // Modern button styling
        jButton1.putClientProperty(FlatClientProperties.STYLE, ""
            + "background:#3B82F6;"
            + "foreground:#FFFFFF;"
            + "borderWidth:0;"
            + "focusWidth:0;"
            + "innerFocusWidth:0");
        
        // Radio button styling
        jRadioButton1.putClientProperty(FlatClientProperties.STYLE, ""
            + "foreground:#EF4444;");
        jRadioButton2.putClientProperty(FlatClientProperties.STYLE, ""
            + "foreground:#6366F1;");
        jRadioButton4.putClientProperty(FlatClientProperties.STYLE, ""
            + "foreground:#F97316;");

        // Set "Due Amount" as default selected radio button
        jRadioButton4.setSelected(true);
        
        // Setup panel background
        jPanel2.setBackground(Color.decode("#F8FAFC"));
        
        // Add key listener for search
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchCustomers();
            }
        });
        
        // Add listeners for all radio buttons
        jRadioButton1.addActionListener(evt -> searchCustomers());
        jRadioButton2.addActionListener(evt -> searchCustomers());
        jRadioButton4.addActionListener(evt -> searchCustomers());
    }

    private void loadCustomers() {
        String searchText = jTextField1.getText().trim();
        // Don't search if it's the placeholder text
        if (searchText.equals("Search By Customer Name or NIC")) {
            searchText = "";
        }
        boolean missedDueDateOnly = jRadioButton1.isSelected();
        boolean noDueOnly = jRadioButton2.isSelected();
        boolean dueAmountOnly = jRadioButton4.isSelected();
        
        loadCustomers(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly);
    }

    private void loadCustomers(String searchText, boolean missedDueDateOnly, boolean noDueOnly, boolean dueAmountOnly) {
        try {
            // Clear existing products
            jPanel2.removeAll();
            jPanel2.setBackground(Color.decode("#F8FAFC"));

            String query = buildQuery(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly);
            ResultSet rs = MySQL.executeSearch(query);

            // Store customers in a list
            java.util.List<lk.com.pos.privateclasses.RoundedPanel> customerCards = new java.util.ArrayList<>();

            int customerCount = 0;

            // Loop through results and create customer cards
            while (rs.next()) {
                customerCount++;

                lk.com.pos.privateclasses.RoundedPanel customerCard = createCustomerCard(
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("customer_phone_no"),
                    rs.getString("customer_address"),
                    rs.getString("nic"),
                    rs.getString("date_time"),
                    rs.getString("status_name"),
                    rs.getString("latest_due_date"),
                    rs.getDouble("total_credit_amount"),
                    rs.getDouble("total_paid")
                );

                customerCards.add(customerCard);
            }

            // If no customers found - show message
            if (customerCount == 0) {
                jPanel2.setLayout(new java.awt.BorderLayout());

                javax.swing.JPanel messagePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
                messagePanel.setBackground(Color.decode("#F8FAFC"));
                messagePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 0, 0, 0));

                javax.swing.JLabel noCustomers = new javax.swing.JLabel("No customers found");
                noCustomers.setFont(new java.awt.Font("Nunito SemiBold", 0, 18));
                noCustomers.setForeground(Color.decode("#6B7280"));
                noCustomers.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                messagePanel.add(noCustomers);
                jPanel2.add(messagePanel, java.awt.BorderLayout.NORTH);
            } else {
                // Create responsive grid panel - EXACTLY LIKE PRODUCT PANEL
                final javax.swing.JPanel gridPanel = new javax.swing.JPanel();
                gridPanel.setBackground(Color.decode("#F8FAFC"));

                // Calculate initial columns based on jPanel2 width - 3 columns like product panel
                int initialColumns = calculateColumns(jPanel2.getWidth());
                gridPanel.setLayout(new java.awt.GridLayout(0, initialColumns, 25, 25));

                // Add all customer cards
                for (lk.com.pos.privateclasses.RoundedPanel card : customerCards) {
                    gridPanel.add(card);
                }

                // Proper scrolling implementation like product panel
                jPanel2.setLayout(new java.awt.BorderLayout());

                // Create main container with vertical layout for proper scrolling
                javax.swing.JPanel mainContainer = new javax.swing.JPanel();
                mainContainer.setLayout(new javax.swing.BoxLayout(mainContainer, javax.swing.BoxLayout.Y_AXIS));
                mainContainer.setBackground(Color.decode("#F8FAFC"));

                // Add padding around the grid
                javax.swing.JPanel paddingPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
                paddingPanel.setBackground(Color.decode("#F8FAFC"));
                paddingPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25));
                paddingPanel.add(gridPanel, java.awt.BorderLayout.NORTH);

                mainContainer.add(paddingPanel);
                jPanel2.add(mainContainer, java.awt.BorderLayout.NORTH);

                // Add component listener to jPanel2 for window resize
                jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
                    private int lastColumns = initialColumns;

                    @Override
                    public void componentResized(java.awt.event.ComponentEvent e) {
                        int panelWidth = jPanel2.getWidth();
                        int newColumns = calculateColumns(panelWidth);

                        // Only update if columns changed
                        if (newColumns != lastColumns) {
                            lastColumns = newColumns;
                            gridPanel.setLayout(new java.awt.GridLayout(0, newColumns, 25, 25));
                            gridPanel.revalidate();
                            gridPanel.repaint();
                        }
                    }
                });
            }

            // Refresh the panel
            jPanel2.revalidate();
            jPanel2.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Error loading customers: " + e.getMessage(),
                    "Database Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private int calculateColumns(int panelWidth) {
        int availableWidth = panelWidth - 50; // Account for padding
        int cardWithGap = 445; // Same as product panel

        if (availableWidth >= cardWithGap * 3) {
            return 3; // 3 columns on large screens
        } else if (availableWidth >= cardWithGap * 2) {
            return 2; // 2 columns on medium screens
        } else {
            return 1; // 1 column on small screens
        }
    }

    private String buildQuery(String searchText, boolean missedDueDateOnly, boolean noDueOnly, boolean dueAmountOnly) {
        StringBuilder query = new StringBuilder();
        
        // Use subquery to handle aggregation properly
        query.append("SELECT * FROM (");
        
        query.append("SELECT ");
        query.append("cc.customer_id, ");
        query.append("cc.customer_name, ");
        query.append("cc.customer_phone_no, ");
        query.append("cc.customer_address, ");
        query.append("cc.nic, ");
        query.append("cc.date_time, ");
        query.append("s.status_name, ");
        query.append("MAX(c.credit_final_date) as latest_due_date, ");
        query.append("IFNULL(SUM(c.credit_amout), 0) AS total_credit_amount, ");
        query.append("IFNULL(SUM(cp.credit_pay_amount), 0) AS total_paid ");
        query.append("FROM credit_customer cc ");
        query.append("JOIN status s ON s.status_id = cc.status_id ");
        query.append("LEFT JOIN credit c ON c.credit_customer_id = cc.customer_id ");
        query.append("LEFT JOIN credit_pay cp ON cp.credit_id = c.credit_id ");
        
        // Add search filter
        if (!searchText.isEmpty() && !searchText.equals("Search By Customer Name or NIC")) {
            query.append("WHERE (cc.customer_name LIKE '%").append(searchText).append("%' ");
            query.append("OR cc.nic LIKE '%").append(searchText).append("%') ");
        }
        
        query.append("GROUP BY cc.customer_id, cc.customer_name, cc.customer_phone_no, ");
        query.append("cc.customer_address, cc.nic, cc.date_time, s.status_name ");
        
        query.append(") AS customer_data ");
        query.append("WHERE 1=1 ");
        
        // Apply filters based on radio button selection
        if (missedDueDateOnly) {
            query.append("AND latest_due_date < CURDATE() ");
            query.append("AND total_credit_amount > total_paid ");
        } else if (noDueOnly) {
            query.append("AND total_credit_amount <= total_paid ");
        } else if (dueAmountOnly) {
            query.append("AND total_credit_amount > total_paid ");
        }
        
        query.append("ORDER BY customer_id DESC");
        
        return query.toString();
    }

    private void searchCustomers() {
        String searchText = jTextField1.getText().trim();
        // Don't search if it's the placeholder text
        if (searchText.equals("Search By Customer Name or NIC")) {
            searchText = "";
        }
        boolean missedDueDateOnly = jRadioButton1.isSelected();
        boolean noDueOnly = jRadioButton2.isSelected();
        boolean dueAmountOnly = jRadioButton4.isSelected();
        loadCustomers(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly);
    }

    private lk.com.pos.privateclasses.RoundedPanel createCustomerCard(
            int customerId, String customerName, String phone,
            String address, String nic, String registrationDate,
            String status, String finalDate, double totalCreditAmount, 
            double totalPaid) {

        // Calculate outstanding balance
        double outstanding = totalCreditAmount - totalPaid;
        
        // Check if missed due date - LIKE PRODUCT PANEL'S EXPIRED BADGE
        boolean missedDueDate = false;
        String displayDueDate = "No Credit";
        try {
            if (finalDate != null) {
                java.util.Date dueDate = DATE_FORMAT.parse(finalDate);
                java.util.Date today = new java.util.Date();
                missedDueDate = dueDate.before(today) && outstanding > 0;
                displayDueDate = DISPLAY_DATE_FORMAT.format(dueDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Format registration date
        String regDate = "N/A";
        try {
            if (registrationDate != null) {
                java.util.Date reg = DATE_FORMAT.parse(registrationDate);
                regDate = DISPLAY_DATE_FORMAT.format(reg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create main rounded panel - EXACTLY LIKE PRODUCT PANEL
        lk.com.pos.privateclasses.RoundedPanel card = new lk.com.pos.privateclasses.RoundedPanel();
        card.setLayout(new java.awt.BorderLayout());
        card.setPreferredSize(new java.awt.Dimension(420, 470)); // Increased height for address section
        card.setMaximumSize(new java.awt.Dimension(420, 470));
        card.setMinimumSize(new java.awt.Dimension(380, 470));
        card.setBackground(java.awt.Color.WHITE);
        card.setBorderThickness(0);
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Main content panel
        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS));
        contentPanel.setBackground(java.awt.Color.WHITE);
        contentPanel.setOpaque(false);

        // === HEADER SECTION ===
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));

        // Customer Name
        javax.swing.JLabel nameLabel = new javax.swing.JLabel(customerName);
        nameLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
        nameLabel.setForeground(Color.decode("#1E293B"));
        nameLabel.setToolTipText(customerName);
        headerPanel.add(nameLabel, java.awt.BorderLayout.CENTER);

        // Action buttons panel - EXACTLY LIKE PRODUCT PANEL
        javax.swing.JPanel actionPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        // Edit Button - Fixed size like product panel
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
            editButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        }
        editButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#BFDBFE"), 1));
        editButton.setFocusable(false);
        editButton.addActionListener(e -> editCustomer(customerId));

        // Delete Button - Fixed size like product panel
        javax.swing.JButton deleteButton = new javax.swing.JButton();
        deleteButton.setPreferredSize(new java.awt.Dimension(30, 30));
        deleteButton.setMinimumSize(new java.awt.Dimension(30, 30));
        deleteButton.setMaximumSize(new java.awt.Dimension(30, 30));
        deleteButton.setBackground(Color.decode("#FEF2F2"));
        deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        try {
            FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 16, 16);
            deleteButton.setIcon(deleteIcon);
        } catch (Exception e) {
            deleteButton.setText("×");
            deleteButton.setForeground(Color.decode("#EF4444"));
            deleteButton.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
        }
        deleteButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
        deleteButton.setFocusable(false);
        deleteButton.addActionListener(e -> deleteCustomer(customerId));

        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        headerPanel.add(actionPanel, java.awt.BorderLayout.EAST);

        contentPanel.add(headerPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(8));

        // === STATUS AND BADGES ROW - EXACTLY LIKE PRODUCT PANEL ===
        javax.swing.JPanel statusBadgePanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        statusBadgePanel.setOpaque(false);
        statusBadgePanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));

        // Status Label - Like supplier in product panel
        javax.swing.JLabel statusLabel = new javax.swing.JLabel("● " + status);
        statusLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        statusLabel.setForeground(Color.decode("#6366F1"));
        statusLabel.setToolTipText("Status: " + status);
        statusBadgePanel.add(statusLabel, java.awt.BorderLayout.WEST);

        // Status badges on right side - EXACTLY LIKE PRODUCT PANEL
        javax.swing.JPanel badgePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        badgePanel.setOpaque(false);

        // Missed Due Date Badge - EXACTLY LIKE PRODUCT PANEL'S EXPIRED BADGE
        if (missedDueDate) {
            javax.swing.JLabel missedBadge = new javax.swing.JLabel("⚠ Missed Due Date");
            missedBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
            missedBadge.setForeground(Color.decode("#7C2D12"));
            missedBadge.setBackground(Color.decode("#FED7AA"));
            missedBadge.setOpaque(true);
            missedBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            missedBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.decode("#FB923C"), 1),
                    javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            badgePanel.add(missedBadge);
        }

        // High Credit Risk Badge (if outstanding is high) - Like low stock badge
        if (outstanding > 50000) {
            javax.swing.JLabel highRiskBadge = new javax.swing.JLabel("📉 High Risk");
            highRiskBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
            highRiskBadge.setForeground(java.awt.Color.WHITE);
            highRiskBadge.setBackground(Color.decode("#DC2626"));
            highRiskBadge.setOpaque(true);
            highRiskBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            highRiskBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1),
                    javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            badgePanel.add(highRiskBadge);
        }

        statusBadgePanel.add(badgePanel, java.awt.BorderLayout.EAST);

        contentPanel.add(statusBadgePanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        // === CUSTOMER DETAILS HEADER ===
        javax.swing.JPanel detailsHeaderPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 20));

        javax.swing.JLabel detailsHeader = new javax.swing.JLabel("CUSTOMER DETAILS");
        detailsHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
        detailsHeader.setForeground(Color.decode("#94A3B8"));
        detailsHeaderPanel.add(detailsHeader, java.awt.BorderLayout.WEST);

        contentPanel.add(detailsHeaderPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        // === DETAILS GRID - REMOVED STATUS AND CREDIT AMOUNT ===
        javax.swing.JPanel detailsGrid = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 20, 15)); // Changed to 2 rows
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 120)); // Reduced height

        // Row 1: Phone, NIC
        detailsGrid.add(createDetailPanel("Phone", formatPhoneNumber(phone), Color.decode("#8B5CF6")));
        detailsGrid.add(createDetailPanel("NIC", nic, Color.decode("#EC4899")));

        // Row 2: Due Date, Registered - REMOVED CREDIT AMOUNT AND STATUS
        detailsGrid.add(createDetailPanel("Due Date", displayDueDate, Color.decode("#10B981")));
        detailsGrid.add(createDetailPanel("Registered Date", regDate, Color.decode("#06B6D4")));

        contentPanel.add(detailsGrid);
        contentPanel.add(javax.swing.Box.createVerticalStrut(20));

        // === ADDRESS SECTION - MOVED BELOW DETAILS GRID ===
        if (address != null && !address.trim().isEmpty()) {
            javax.swing.JPanel addressPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
            addressPanel.setOpaque(false);
            addressPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 50));
            
            javax.swing.JLabel addressTitle = new javax.swing.JLabel("📍 Address");
            addressTitle.setFont(new java.awt.Font("Nunito SemiBold", 1, 13));
            addressTitle.setForeground(Color.decode("#6366F1"));
            addressTitle.setToolTipText("Customer Address");
            
            // Create address label with proper wrapping
            javax.swing.JLabel addressLabel = new javax.swing.JLabel("<html><div style='width:360px;'>" + address + "</div></html>");
            addressLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
            addressLabel.setForeground(Color.decode("#1E293B"));
            addressLabel.setToolTipText(address);
            addressLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            
            addressPanel.add(addressTitle, java.awt.BorderLayout.NORTH);
            addressPanel.add(javax.swing.Box.createVerticalStrut(5), java.awt.BorderLayout.CENTER);
            addressPanel.add(addressLabel, java.awt.BorderLayout.CENTER);
            
            contentPanel.add(addressPanel);
            contentPanel.add(javax.swing.Box.createVerticalStrut(15));
        }

        // === PAYMENT SUMMARY HEADER ===
        javax.swing.JPanel paymentHeaderPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        paymentHeaderPanel.setOpaque(false);
        paymentHeaderPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 25));

        javax.swing.JLabel paymentHeader = new javax.swing.JLabel("💰 PAYMENT SUMMARY");
        paymentHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
        paymentHeader.setForeground(Color.decode("#94A3B8"));
        paymentHeaderPanel.add(paymentHeader, java.awt.BorderLayout.WEST);
        
        // View Details Button - With colorful background
        javax.swing.JButton paymentDetailsBtn = new javax.swing.JButton("View Details");
        paymentDetailsBtn.setFont(new java.awt.Font("Nunito SemiBold", 0, 10)); // Smaller
        paymentDetailsBtn.setForeground(Color.WHITE);
        paymentDetailsBtn.setBackground(Color.decode("#8B5CF6")); // Purple background
        paymentDetailsBtn.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(Color.decode("#7C3AED"), 1),
            javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10) // Reduced padding
        ));
        paymentDetailsBtn.setFocusable(false);
        paymentDetailsBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        paymentDetailsBtn.addActionListener(e -> showPaymentDetails(customerId));
        
        // Add hover effect
        paymentDetailsBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                paymentDetailsBtn.setBackground(Color.decode("#7C3AED")); // Darker purple on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                paymentDetailsBtn.setBackground(Color.decode("#8B5CF6")); // Original purple
            }
        });
        
        paymentHeaderPanel.add(paymentDetailsBtn, java.awt.BorderLayout.EAST);

        contentPanel.add(paymentHeaderPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(12));

        // === PAYMENT PANELS - EXACTLY LIKE PRODUCT PRICING PANELS ===
        javax.swing.JPanel paymentPanel = new javax.swing.JPanel(new java.awt.GridLayout(1, 3, 10, 0));
        paymentPanel.setOpaque(false);
        paymentPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 65));

        // Credit Amount - Blue theme
        lk.com.pos.privateclasses.RoundedPanel creditPanel = createPaymentPanel(
                "Amount Due",
                totalCreditAmount,
                Color.decode("#DBEAFE"),
                Color.decode("#1E40AF")
        );

        // Paid Amount - Green theme
        lk.com.pos.privateclasses.RoundedPanel paidPanel = createPaymentPanel(
                "Paid Amount",
                totalPaid,
                Color.decode("#D1FAE5"),
                Color.decode("#059669")
        );

        // Outstanding - Color based on amount
        Color outstandingBg = outstanding > 0 ? Color.decode("#FEF3C7") : Color.decode("#D1FAE5");
        Color outstandingText = outstanding > 0 ? Color.decode("#92400E") : Color.decode("#059669");
        lk.com.pos.privateclasses.RoundedPanel outstandingPanel = createPaymentPanel(
                "OutStanding",
                outstanding,
                outstandingBg,
                outstandingText
        );

        paymentPanel.add(creditPanel);
        paymentPanel.add(paidPanel);
        paymentPanel.add(outstandingPanel);

        contentPanel.add(paymentPanel);

        card.add(contentPanel, java.awt.BorderLayout.CENTER);
        return card;
    }

    private String getShortAddress(String address) {
        if (address == null || address.trim().isEmpty()) return "No Address";
        if (address.length() <= 25) return address;
        return address.substring(0, 22) + "...";
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "N/A";
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() == 10) {
            return cleaned.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return phone;
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

    private lk.com.pos.privateclasses.RoundedPanel createPaymentPanel(String title, double amount, Color bgColor, Color textColor) {
        lk.com.pos.privateclasses.RoundedPanel panel = new lk.com.pos.privateclasses.RoundedPanel();
        panel.setBackgroundColor(bgColor);
        panel.setBorderThickness(0);
        panel.setLayout(new java.awt.GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title Label
        javax.swing.JLabel titleLabel = new javax.swing.JLabel(title);
        titleLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12));
        titleLabel.setForeground(textColor);
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panel.add(titleLabel, gbc);

        // Amount Label with smart formatting
        String formattedAmount = formatPrice(amount);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(5, 0, 0, 0);

        javax.swing.JLabel amountLabel = new javax.swing.JLabel(formattedAmount);
        amountLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16));
        amountLabel.setForeground(textColor);
        amountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        amountLabel.setToolTipText(title + ": Rs." + String.format("%.2f", amount));
        panel.add(amountLabel, gbc);

        return panel;
    }

    // Smart price formatting - same as product panel
    private String formatPrice(double price) {
        if (price >= 100000) {
            return String.format("Rs.%.1fK", price / 1000);
        } else if (price >= 10000) {
            return String.format("Rs.%.2fK", price / 1000);
        } else if (price >= 1000) {
            return String.format("Rs.%.0f", price);
        } else {
            return String.format("Rs.%.2f", price);
        }
    }

    private void editCustomer(int customerId) {
        // TODO: Implement edit customer functionality
        JOptionPane.showMessageDialog(this, 
            "Edit customer with ID: " + customerId, 
            "Edit Customer", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showPaymentDetails(int customerId) {
        // TODO: Implement payment details view
        JOptionPane.showMessageDialog(this, 
            "Payment details for customer ID: " + customerId, 
            "Payment Details", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteCustomer(int customerId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this customer?\nThis action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Delete related credit_pay records first
                MySQL.executeIUD("DELETE FROM credit_pay WHERE credit_id IN (SELECT credit_id FROM credit WHERE credit_customer_id = " + customerId + ")");
                // Delete related credit records
                MySQL.executeIUD("DELETE FROM credit WHERE credit_customer_id = " + customerId);
                // Delete the customer
                MySQL.executeIUD("DELETE FROM credit_customer WHERE customer_id = " + customerId);
                
                JOptionPane.showMessageDialog(this,
                    "Customer deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reload customers
                loadCustomers();
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error deleting customer: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel1 = new javax.swing.JLabel();
        deleteBtn = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jButton8 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        roundedPanel2 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        roundedPanel3 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        roundedPanel4 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jTextField1.setText("Search By Customer Name or NIC");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton1.setForeground(new java.awt.Color(255, 51, 51));
        jRadioButton1.setText("Missed Due Date");

        jButton1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton1.setText("Add New Customer");

        jPanel2.setBackground(new java.awt.Color(248, 250, 252));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20)); // NOI18N
        jLabel1.setText("Customer Name ");

        deleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jButton8.setBackground(new java.awt.Color(255, 51, 51));
        jButton8.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton8.setText("Missed Due Date");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(2, 0));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel15.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel15.setText("Phone");

        jLabel20.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 102, 102));
        jLabel20.setText("0701865698");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.add(jPanel9);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel21.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel21.setText("NIC");

        jLabel26.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(102, 102, 102));
        jLabel26.setText("200563253698");

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
                .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.add(jPanel8);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        jLabel27.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel27.setText("Final Date");

        jLabel28.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(102, 102, 102));
        jLabel28.setText("2025-12-20");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.add(jPanel10);

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));

        jLabel29.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel29.setText("Expiry Date");

        jLabel30.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(102, 102, 102));
        jLabel30.setText("2025-12-20");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(20, 20, 20))
                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(343, 343, 343))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.add(jPanel11);

        roundedPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel36.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel36.setText("Amount Due");

        jLabel35.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(102, 102, 102));
        jLabel35.setText("Rs.10000");

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundedPanel2Layout.setVerticalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel35)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jLabel37.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel37.setText("Paid Amount");

        jLabel38.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(102, 102, 102));
        jLabel38.setText("Rs.7500");

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel39.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel39.setText("OutStanding ");

        jLabel40.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(102, 102, 102));
        jLabel40.setText("Rs.2500");

        javax.swing.GroupLayout roundedPanel4Layout = new javax.swing.GroupLayout(roundedPanel4);
        roundedPanel4.setLayout(roundedPanel4Layout);
        roundedPanel4Layout.setHorizontalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        roundedPanel4Layout.setVerticalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel40)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel31.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel31.setText("Address");

        jLabel32.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(102, 102, 102));
        jLabel32.setText("2025-12-20");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(281, 281, 281))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton2.setText("V");

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jSeparator2)
                                .addGroup(roundedPanel1Layout.createSequentialGroup()
                                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, roundedPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(roundedPanel1Layout.createSequentialGroup()
                                            .addGap(0, 1, Short.MAX_VALUE)
                                            .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(roundedPanel1Layout.createSequentialGroup()
                                            .addComponent(jButton2)
                                            .addGap(0, 0, Short.MAX_VALUE))))
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(jSeparator1)))
                        .addContainerGap(25, Short.MAX_VALUE))))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addGap(8, 8, 8)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(roundedPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(525, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton2.setForeground(new java.awt.Color(99, 102, 241));
        jRadioButton2.setText("No Due");

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton4.setForeground(new java.awt.Color(255, 153, 0));
        jRadioButton4.setText("Due Amount");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addGap(150, 150, 150)
                        .addComponent(jButton1)))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jRadioButton2)
                        .addComponent(jRadioButton4)
                        .addComponent(jRadioButton1)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
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
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel4;
    // End of variables declaration//GEN-END:variables
}
