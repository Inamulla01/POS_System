/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import lk.com.pos.privateclasses.RoundedPanel;

/**
 *
 * @author pasin
 */
public class SupplierPanel extends javax.swing.JPanel {

    private int currentPanelsPerRow = 3;

    /**
     * Creates new form SupplierPanel1
     */
    public SupplierPanel() {
        initComponents();
        init();
    }
    
    private void init(){
        // Configure scroll pane for smooth scrolling
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        
        // Style scrollbar
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");
        
        loadSupplier();
        setupEventListeners();
        radioButtonListener();
        
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "By Company Name, Supplier Name Or Register No");
        jTextField1.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, 
            new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
    }

    private void setupEventListeners() {
        // Search bar - trigger search on text change
        jTextField1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchFilters();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchFilters();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchFilters();
            }
        });

        // Clear placeholder text on focus
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (jTextField1.getText().equals("Search By Company Name, Supplier Name Or Register No")) {
                    jTextField1.setText("");
                    jTextField1.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (jTextField1.getText().isEmpty()) {
                    jTextField1.setText("Search By Company Name, Supplier Name Or Register No");
                    jTextField1.setForeground(Color.GRAY);
                }
            }
        });

        jTextField1.setForeground(Color.GRAY);
        
        // Radio buttons - trigger search on selection
        activeRadioBtn.addActionListener(e -> searchFilters());
        inActiveRadioBtn.addActionListener(e -> searchFilters());
        jRadioButton2.addActionListener(e -> searchFilters());  // No Due
        jRadioButton4.addActionListener(e -> searchFilters());  // Due Amount
        
        // Add component listener for responsive layout
        jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayout();
            }
        });
    }

    private void updateLayout() {
        int scrollPaneWidth = jScrollPane1.getWidth();
        int newPanelsPerRow = calculateColumns(scrollPaneWidth);
        
        // Only reload if the layout changed
        if (newPanelsPerRow != currentPanelsPerRow) {
            currentPanelsPerRow = newPanelsPerRow;
            searchFilters();
        }
    }

    private int calculateColumns(int panelWidth) {
        int availableWidth = panelWidth - 50;
        int cardWithGap = 445;

        if (availableWidth >= cardWithGap * 3) {
            return 3;
        } else if (availableWidth >= cardWithGap * 2) {
            return 2;
        } else {
            return 1;
        }
    }

    private void searchFilters() {
        String searchText = jTextField1.getText().trim();
        String status = "all";
        String dueStatus = "all";

        if (activeRadioBtn.isSelected()) {
            status = "Active";
        } else if (inActiveRadioBtn.isSelected()) {
            status = "Inactive";
        }
        
        if (jRadioButton2.isSelected()) {
            dueStatus = "no_due";
        } else if (jRadioButton4.isSelected()) {
            dueStatus = "has_due";
        }

        loadSupplier(searchText, status, dueStatus);
    }

    private void loadSupplier() {
        loadSupplier("", "all", "all");
    }

    private void loadSupplier(String searchText, String status, String dueStatus) {
        try {
            String query = "SELECT \n"
                    + "    s.suppliers_id,\n"
                    + "    s.Company,\n"
                    + "    s.suppliers_name,\n"
                    + "    s.suppliers_mobile,\n"
                    + "    s.suppliers_reg_no,\n"
                    + "    s.suppliers_address,\n"
                    + "    p.p_status,\n"
                    + "    COALESCE(cs.credit_amount, 0) as credit_amount,\n"
                    + "    COALESCE(SUM(scp.credit_pay_amount), 0) as credit_pay_amount\n"
                    + "FROM suppliers s\n"
                    + "LEFT JOIN p_status p \n"
                    + "    ON s.p_status_id = p.p_status_id\n"
                    + "LEFT JOIN credit_supplier cs \n"
                    + "    ON cs.suppliers_id = s.suppliers_id\n"
                    + "LEFT JOIN supplier_credit_pay scp \n"
                    + "    ON scp.credit_supplier_id = cs.credit_supplier_id\n"
                    + "WHERE 1=1 ";

            // Add search filter
            if (searchText != null && !searchText.trim().isEmpty()
                    && !searchText.equals("Search By Company Name, Supplier Name Or Register No")) {
                query += "AND (s.Company LIKE '%" + searchText + "%' "
                        + "OR s.suppliers_name LIKE '%" + searchText + "%' "
                        + "OR s.suppliers_reg_no LIKE '%" + searchText + "%') ";
            }

            // Add status filter
            if (!"all".equals(status)) {
                query += "AND p.p_status = '" + status + "' ";
            }
            
            // Add due status filter
            query += "GROUP BY s.suppliers_id, s.Company, s.suppliers_name, s.suppliers_mobile, "
                    + "s.suppliers_reg_no, s.suppliers_address, p.p_status, cs.credit_amount ";
            
            if ("no_due".equals(dueStatus)) {
                query += "HAVING (COALESCE(cs.credit_amount, 0) - COALESCE(SUM(scp.credit_pay_amount), 0)) = 0 ";
            } else if ("has_due".equals(dueStatus)) {
                query += "HAVING (COALESCE(cs.credit_amount, 0) - COALESCE(SUM(scp.credit_pay_amount), 0)) > 0 ";
            }

            query += "ORDER BY s.Company";
            
            // Clear existing content
            jPanel2.removeAll();
            jPanel2.setBackground(Color.decode("#F8FAFC"));
            
            // Execute query
            ResultSet rs = lk.com.pos.connection.MySQL.executeSearch(query);
            
            // Store supplier cards in a list
            java.util.List<RoundedPanel> supplierCards = new java.util.ArrayList<>();
            
            int supplierCount = 0;
            
            while (rs.next()) {
                supplierCount++;
                
                String supplierId = rs.getString("suppliers_id");
                String company = rs.getString("Company");
                String supplierName = rs.getString("suppliers_name");
                String mobile = rs.getString("suppliers_mobile");
                String regNo = rs.getString("suppliers_reg_no");
                String address = rs.getString("suppliers_address");
                String pStatus = rs.getString("p_status");
                String creditAmount = rs.getString("credit_amount");
                String paidAmount = rs.getString("credit_pay_amount");
                
                RoundedPanel supplierCard = createSupplierCard(
                    supplierId,
                    company != null ? company : "N/A",
                    supplierName != null ? supplierName : "N/A",
                    mobile != null ? mobile : "N/A",
                    regNo != null ? regNo : "N/A",
                    address != null ? address : "N/A",
                    pStatus != null ? pStatus : "Active",
                    creditAmount != null ? creditAmount : "0",
                    paidAmount != null ? paidAmount : "0"
                );
                
                supplierCards.add(supplierCard);
            }
            
            // If no suppliers found
            if (supplierCount == 0) {
                jPanel2.setLayout(new java.awt.BorderLayout());

                JPanel messagePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
                messagePanel.setBackground(Color.decode("#F8FAFC"));
                messagePanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

                JLabel noSuppliers = new JLabel("No suppliers found");
                noSuppliers.setFont(new Font("Nunito SemiBold", 0, 18));
                noSuppliers.setForeground(Color.decode("#6B7280"));
                noSuppliers.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                messagePanel.add(noSuppliers);
                jPanel2.add(messagePanel, java.awt.BorderLayout.NORTH);
            } else {
                // Create responsive grid panel
                final JPanel gridPanel = new JPanel();
                gridPanel.setBackground(Color.decode("#F8FAFC"));
                gridPanel.setLayout(new java.awt.GridLayout(0, currentPanelsPerRow, 25, 25));

                // Add all supplier cards
                for (RoundedPanel card : supplierCards) {
                    gridPanel.add(card);
                }

                // Proper scrolling implementation
                jPanel2.setLayout(new java.awt.BorderLayout());

                // Create main container
                JPanel mainContainer = new JPanel();
                mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
                mainContainer.setBackground(Color.decode("#F8FAFC"));

                // Add padding around the grid
                JPanel paddingPanel = new JPanel(new java.awt.BorderLayout());
                paddingPanel.setBackground(Color.decode("#F8FAFC"));
                paddingPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
                paddingPanel.add(gridPanel, java.awt.BorderLayout.NORTH);

                mainContainer.add(paddingPanel);
                jPanel2.add(mainContainer, java.awt.BorderLayout.NORTH);

                // Add component listener for window resize
                jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
                    private int lastColumns = currentPanelsPerRow;

                    @Override
                    public void componentResized(java.awt.event.ComponentEvent e) {
                        int panelWidth = jPanel2.getWidth();
                        int newColumns = calculateColumns(panelWidth);

                        if (newColumns != lastColumns) {
                            lastColumns = newColumns;
                            gridPanel.setLayout(new java.awt.GridLayout(0, newColumns, 25, 25));
                            gridPanel.revalidate();
                            gridPanel.repaint();
                        }
                    }
                });
            }
            
            jPanel2.revalidate();
            jPanel2.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Error loading suppliers: " + e.getMessage(), 
                "Database Error", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private RoundedPanel createSupplierCard(String supplierId, String company, String name, String mobile, 
                                           String regNo, String address, String status, String amountDue, 
                                           String paidAmount) {
        RoundedPanel card = new RoundedPanel();
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(420, 420));
        card.setMaximumSize(new Dimension(420, 420));
        card.setMinimumSize(new Dimension(380, 420));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorderThickness(0);
        card.setBorder(BorderFactory.createEmptyBorder(25, 18, 18, 18));
        
        // === HEADER SECTION ===
        JPanel headerPanel = new JPanel(new java.awt.BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel companyLabel = new JLabel(company);
        companyLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        companyLabel.setForeground(Color.decode("#1E293B"));
        companyLabel.setToolTipText(company);
        headerPanel.add(companyLabel, java.awt.BorderLayout.CENTER);
        
        // Action buttons panel
        JPanel actionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);
        
        JButton editBtn = new JButton();
        editBtn.setPreferredSize(new Dimension(30, 30));
        editBtn.setMinimumSize(new Dimension(30, 30));
        editBtn.setMaximumSize(new Dimension(30, 30));
        editBtn.setBackground(Color.decode("#EFF6FF"));
        editBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 16, 16);
            editBtn.setIcon(editIcon);
        } catch (Exception e) {
            editBtn.setText("✎");
            editBtn.setForeground(Color.decode("#3B82F6"));
        }
        
        editBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#BFDBFE"), 1));
        editBtn.setFocusable(false);
        editBtn.addActionListener(e -> editSupplier(supplierId));
        
        JButton deleteBtn = new JButton();
        deleteBtn.setPreferredSize(new Dimension(30, 30));
        deleteBtn.setMinimumSize(new Dimension(30, 30));
        deleteBtn.setMaximumSize(new Dimension(30, 30));
        deleteBtn.setBackground(Color.decode("#FEF2F2"));
        deleteBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        try {
            FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 16, 16);
            deleteBtn.setIcon(deleteIcon);
        } catch (Exception e) {
            deleteBtn.setText("×");
            deleteBtn.setForeground(Color.decode("#EF4444"));
            deleteBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        }
        
        deleteBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
        deleteBtn.setFocusable(false);
        deleteBtn.addActionListener(e -> deleteSupplier(supplierId, company));
        
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        headerPanel.add(actionPanel, java.awt.BorderLayout.EAST);
        
        card.add(headerPanel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // === SUPPLIER NAME AND STATUS BADGE ===
        JPanel nameBadgePanel = new JPanel(new java.awt.BorderLayout(10, 0));
        nameBadgePanel.setOpaque(false);
        nameBadgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameBadgePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel nameLabel = new JLabel("● " + name);
        nameLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        nameLabel.setForeground(Color.decode("#6366F1"));
        nameLabel.setToolTipText("Supplier: " + name);
        nameBadgePanel.add(nameLabel, java.awt.BorderLayout.WEST);
        
        // Status badge
        if ("Active".equals(status)) {
            JLabel statusBadge = new JLabel("✓ Active");
            statusBadge.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
            statusBadge.setForeground(Color.decode("#065F46"));
            statusBadge.setBackground(Color.decode("#D1FAE5"));
            statusBadge.setOpaque(true);
            statusBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            statusBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#10B981"), 1),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            
            JPanel badgePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
            badgePanel.setOpaque(false);
            badgePanel.add(statusBadge);
            nameBadgePanel.add(badgePanel, java.awt.BorderLayout.EAST);
        }
        
        card.add(nameBadgePanel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // === DETAILS HEADER ===
        JPanel detailsHeaderPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        detailsHeaderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel detailsHeader = new JLabel("SUPPLIER DETAILS");
        detailsHeader.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
        detailsHeader.setForeground(Color.decode("#94A3B8"));
        detailsHeaderPanel.add(detailsHeader);
        
        card.add(detailsHeaderPanel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // === DETAILS GRID ===
        JPanel detailsGrid = new JPanel(new java.awt.GridLayout(1, 2, 20, 0));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        detailsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        detailsGrid.add(createDetailPanel("Phone", mobile, Color.decode("#8B5CF6")));
        detailsGrid.add(createDetailPanel("Register No", regNo, Color.decode("#EC4899")));
        
        card.add(detailsGrid);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // === ADDRESS PANEL (FULL WIDTH) ===
        JPanel addressPanel = createDetailPanel("Address", address, Color.decode("#06B6D4"));
        addressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        card.add(addressPanel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // === PRICING HEADER ===
        JPanel priceHeaderPanel = new JPanel(new java.awt.BorderLayout());
        priceHeaderPanel.setOpaque(false);
        priceHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        priceHeaderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceHeader = new JLabel("💰 PAYMENT SUMMARY");
        priceHeader.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
        priceHeader.setForeground(Color.decode("#94A3B8"));
        
        // Calculate outstanding to determine if there's due
        double amountDueVal = 0;
        double paidAmountVal = 0;
        
        try {
            amountDueVal = Double.parseDouble(amountDue);
            paidAmountVal = Double.parseDouble(paidAmount);
        } catch (NumberFormatException e) {
            // Default to 0
        }
        
        double outstanding = amountDueVal - paidAmountVal;
        
        // View Details Button
        JButton viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 10));
        viewDetailsBtn.setForeground(Color.WHITE);
        
        // Change button color based on outstanding amount
        if (outstanding > 0) {
            viewDetailsBtn.setBackground(Color.decode("#8B5CF6")); // Purple for due
        } else {
            viewDetailsBtn.setBackground(Color.decode("#10B981")); // Green for no due
        }
        
        viewDetailsBtn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        viewDetailsBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewDetailsBtn.setFocusable(false);
        viewDetailsBtn.addActionListener(e -> viewSupplierDetails(supplierId, company));
        
        priceHeaderPanel.add(priceHeader, java.awt.BorderLayout.WEST);
        priceHeaderPanel.add(viewDetailsBtn, java.awt.BorderLayout.EAST);
        
        card.add(priceHeaderPanel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // === FINANCIAL PANELS ===
        JPanel financialPanel = new JPanel(new java.awt.GridLayout(1, 3, 10, 0));
        financialPanel.setOpaque(false);
        financialPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        financialPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        RoundedPanel amountDuePanel = createFinancialCard("Amount Due", amountDueVal, Color.decode("#DBEAFE"), Color.decode("#1E40AF"));
        RoundedPanel paidPanel = createFinancialCard("Paid Amount", paidAmountVal, Color.decode("#F3E8FF"), Color.decode("#7C3AED"));
        
        // Change outstanding panel color based on amount
        Color outstandingBg;
        Color outstandingText;
        if (outstanding > 0) {
            outstandingBg = Color.decode("#FEF3C7");  // Yellow background for due
            outstandingText = Color.decode("#92400E"); // Dark yellow text
        } else {
            outstandingBg = Color.decode("#D1FAE5");  // Green background for no due
            outstandingText = Color.decode("#059669"); // Green text
        }
        
        RoundedPanel outstandingPanel = createFinancialCard("Outstanding", outstanding, outstandingBg, outstandingText);
        
        financialPanel.add(amountDuePanel);
        financialPanel.add(paidPanel);
        financialPanel.add(outstandingPanel);
        
        card.add(financialPanel);
        
        return card;
    }
    
    private JPanel createDetailPanel(String title, String value, Color accentColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        valueLabel.setForeground(Color.decode("#1E293B"));
        valueLabel.setToolTipText(value);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);
        
        return panel;
    }
    
    private RoundedPanel createFinancialCard(String title, double amount, Color bgColor, Color textColor) {
        RoundedPanel panel = new RoundedPanel();
        panel.setBackgroundColor(bgColor);
        panel.setBorderThickness(0);
        panel.setLayout(new java.awt.GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 5));
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        titleLabel.setForeground(textColor);
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 0, 0);
        
        String formattedPrice = formatPrice(amount);
        JLabel amountLabel = new JLabel(formattedPrice);
        amountLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        amountLabel.setForeground(textColor);
        amountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        amountLabel.setToolTipText(title + ": Rs." + String.format("%.2f", amount));
        panel.add(amountLabel, gbc);
        
        return panel;
    }
    
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
    
    private void editSupplier(String supplierId) {
        System.out.println("Edit supplier: " + supplierId);
        // TODO: Implement edit functionality
    }
    
    private void deleteSupplier(String supplierId, String companyName) {
        System.out.println("Delete supplier: " + supplierId);
        // TODO: Implement delete functionality
    }
    
    private void viewSupplierDetails(String supplierId, String companyName) {
        System.out.println("View details for supplier: " + supplierId + " - " + companyName);
        // TODO: Implement view details dialog/panel
    }
    
    private void radioButtonListener() {
        // Allow deselecting radio buttons by clicking again
        activeRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (activeRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    searchFilters();
                    evt.consume();
                }
            }
        });

        inActiveRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (inActiveRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    searchFilters();
                    evt.consume();
                }
            }
        });
        
        jRadioButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (jRadioButton2.isSelected()) {
                    buttonGroup1.clearSelection();
                    searchFilters();
                    evt.consume();
                }
            }
        });

        jRadioButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (jRadioButton4.isSelected()) {
                    buttonGroup1.clearSelection();
                    searchFilters();
                    evt.consume();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        activeRadioBtn = new javax.swing.JRadioButton();
        inActiveRadioBtn = new javax.swing.JRadioButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel2 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel1 = new javax.swing.JLabel();
        deleteBtn = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        roundedPanel3 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        roundedPanel4 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        roundedPanel5 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        jTextField1.setText("Search By Company Name, Supplier Name Or Register No");

        buttonGroup1.add(activeRadioBtn);
        activeRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        activeRadioBtn.setForeground(new java.awt.Color(99, 102, 241));
        activeRadioBtn.setText("Active");

        buttonGroup1.add(inActiveRadioBtn);
        inActiveRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        inActiveRadioBtn.setForeground(new java.awt.Color(255, 51, 51));
        inActiveRadioBtn.setText("Inactive");

        jButton2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton2.setText("Add Supplier");

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20)); // NOI18N
        jLabel1.setText("Company Name");

        deleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(1, 0));

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
        jLabel21.setText("Register  No");

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

        roundedPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel36.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel36.setText("Amount Due");

        jLabel35.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(102, 102, 102));
        jLabel35.setText("Rs.10000");

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundedPanel3Layout.setVerticalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
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

        javax.swing.GroupLayout roundedPanel4Layout = new javax.swing.GroupLayout(roundedPanel4);
        roundedPanel4.setLayout(roundedPanel4Layout);
        roundedPanel4Layout.setHorizontalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        roundedPanel4Layout.setVerticalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
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

        javax.swing.GroupLayout roundedPanel5Layout = new javax.swing.GroupLayout(roundedPanel5);
        roundedPanel5.setLayout(roundedPanel5Layout);
        roundedPanel5Layout.setHorizontalGroup(
            roundedPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(roundedPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        roundedPanel5Layout.setVerticalGroup(
            roundedPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel5Layout.createSequentialGroup()
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
        jLabel32.setText("No. Muruthalawa Kandy");

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

        jButton3.setText("V");

        jLabel22.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(102, 102, 102));
        jLabel22.setText("Supplier Name");

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(roundedPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator2)
                            .addGroup(roundedPanel2Layout.createSequentialGroup()
                                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                                        .addGap(0, 1, Short.MAX_VALUE)
                                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                                        .addComponent(jButton3)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jSeparator1))
                        .addContainerGap(18, Short.MAX_VALUE))))
        );
        roundedPanel2Layout.setVerticalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(572, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(112, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton4.setForeground(new java.awt.Color(255, 153, 0));
        jRadioButton4.setText("Due Amount");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton2.setForeground(new java.awt.Color(99, 102, 241));
        jRadioButton2.setText("No Due");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(activeRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inActiveRadioBtn)
                        .addGap(192, 192, 192)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(15, 15, 15))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(activeRadioBtn)
                            .addComponent(inActiveRadioBtn)
                            .addComponent(jRadioButton4)
                            .addComponent(jRadioButton2)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton activeRadioBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JRadioButton inActiveRadioBtn;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel4;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel5;
    // End of variables declaration//GEN-END:variables
}
