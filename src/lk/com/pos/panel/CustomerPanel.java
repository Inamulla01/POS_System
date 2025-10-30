package lk.com.pos.panel;

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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class CustomerPanel extends javax.swing.JPanel {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private int currentPanelWidth = 0;

    public CustomerPanel() {
        initComponents();
        init();
        loadCustomers();
    }

    private void init() {
        // Set icons for buttons
        FlatSVGIcon blueEdit = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 20, 20);
        editBtn.setIcon(blueEdit);

        FlatSVGIcon redDelete = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 20, 20);
        deleteBtn.setIcon(redDelete);
        
        // Set placeholder text color for search field
        jTextField1.setForeground(Color.GRAY);
        
        // Add focus listeners for search field placeholder
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (jTextField1.getText().equals("Search By Customer Name or NIC")) {
                    jTextField1.setText("");
                    jTextField1.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (jTextField1.getText().isEmpty()) {
                    jTextField1.setForeground(Color.GRAY);
                    jTextField1.setText("Search By Customer Name or NIC");
                }
            }
        });
        
        // Add key listener for search
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchCustomers();
            }
        });
        
        // Add listener for missed due date filter
        jRadioButton1.addActionListener(evt -> loadCustomers());
        
        // Setup responsive grid layout for jPanel2
        jPanel2.setLayout(new GridBagLayout());
        jPanel2.setBackground(new Color(245, 245, 245));
        
        // Add component listener for responsive behavior
        jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int newWidth = jScrollPane1.getViewport().getWidth();
                if (Math.abs(newWidth - currentPanelWidth) > 50) {
                    currentPanelWidth = newWidth;
                    loadCustomers();
                }
            }
        });
    }

    private int getColumnsForWidth(int width) {
        if (width < 900) return 1;
        if (width < 1400) return 2;
        return 3;
    }

    private void loadCustomers() {
        try {
            // Clear existing panels
            jPanel2.removeAll();
            
            String query = buildQuery();
            ResultSet rs = MySQL.executeSearch(query);
            
            // Get viewport width for responsive layout
            int viewportWidth = jScrollPane1.getViewport().getWidth();
            int columns = getColumnsForWidth(viewportWidth);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.weightx = 1.0;
            gbc.weighty = 0;
            
            int row = 0;
            int col = 0;
            boolean hasData = false;
            
            while (rs.next()) {
                hasData = true;
                
                gbc.gridx = col;
                gbc.gridy = row;
                
                jPanel2.add(createCustomerCard(rs), gbc);
                
                col++;
                if (col >= columns) {
                    col = 0;
                    row++;
                }
            }
            
            // Add empty filler panel at the end
            gbc.gridx = 0;
            gbc.gridy = row + 1;
            gbc.gridwidth = columns;
            gbc.weighty = 1.0;
            javax.swing.JPanel filler = new javax.swing.JPanel();
            filler.setOpaque(false);
            jPanel2.add(filler, gbc);
            
            if (!hasData) {
                showNoDataMessage();
            }
            
            // Refresh the panel
            jPanel2.revalidate();
            jPanel2.repaint();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading customers: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("cc.customer_id, ");
        query.append("cc.customer_name, ");
        query.append("cc.customer_phone_no, ");
        query.append("cc.customer_address, ");
        query.append("cc.customer_amount_due, ");
        query.append("cc.final_date, ");
        query.append("cc.nic, ");
        query.append("s.status_name, ");
        query.append("IFNULL(SUM(cp.credit_pay_amount), 0) AS total_paid ");
        query.append("FROM credit_customer cc ");
        query.append("JOIN status s ON s.status_id = cc.status_id ");
        query.append("LEFT JOIN credit_pay cp ON cp.credit_customer_id = cc.customer_id ");
        
        // Add filter for missed due date
        if (jRadioButton1.isSelected()) {
            query.append("WHERE cc.final_date < CURDATE() ");
            query.append("AND cc.customer_amount_due > IFNULL((SELECT SUM(credit_pay_amount) FROM credit_pay WHERE credit_customer_id = cc.customer_id), 0) ");
        }
        
        query.append("GROUP BY cc.customer_id ");
        query.append("ORDER BY cc.customer_id DESC");
        
        return query.toString();
    }

    private void searchCustomers() {
        String searchText = jTextField1.getText().trim();
        
        // Don't search if placeholder text
        if (searchText.equals("Search By Customer Name or NIC") || searchText.isEmpty()) {
            loadCustomers();
            return;
        }
        
        try {
            jPanel2.removeAll();
            
            String query = "SELECT " +
                "cc.customer_id, " +
                "cc.customer_name, " +
                "cc.customer_phone_no, " +
                "cc.customer_address, " +
                "cc.customer_amount_due, " +
                "cc.final_date, " +
                "cc.nic, " +
                "s.status_name, " +
                "IFNULL(SUM(cp.credit_pay_amount), 0) AS total_paid " +
                "FROM credit_customer cc " +
                "JOIN status s ON s.status_id = cc.status_id " +
                "LEFT JOIN credit_pay cp ON cp.credit_customer_id = cc.customer_id " +
                "WHERE cc.customer_name LIKE '%" + searchText + "%' " +
                "OR cc.nic LIKE '%" + searchText + "%' " +
                "GROUP BY cc.customer_id " +
                "ORDER BY cc.customer_id DESC";
            
            ResultSet rs = MySQL.executeSearch(query);
            
            // Get viewport width for responsive layout
            int viewportWidth = jScrollPane1.getViewport().getWidth();
            int columns = getColumnsForWidth(viewportWidth);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.weightx = 1.0;
            gbc.weighty = 0;
            
            int row = 0;
            int col = 0;
            boolean hasData = false;
            
            while (rs.next()) {
                hasData = true;
                
                gbc.gridx = col;
                gbc.gridy = row;
                
                jPanel2.add(createCustomerCard(rs), gbc);
                
                col++;
                if (col >= columns) {
                    col = 0;
                    row++;
                }
            }
            
            // Add empty filler panel
            gbc.gridx = 0;
            gbc.gridy = row + 1;
            gbc.gridwidth = columns;
            gbc.weighty = 1.0;
            javax.swing.JPanel filler = new javax.swing.JPanel();
            filler.setOpaque(false);
            jPanel2.add(filler, gbc);
            
            if (!hasData) {
                showNoDataMessage();
            }
            
            jPanel2.revalidate();
            jPanel2.repaint();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private javax.swing.JPanel createCustomerCard(ResultSet rs) throws SQLException {
        // Get data from ResultSet
        int customerId = rs.getInt("customer_id");
        String customerName = rs.getString("customer_name");
        String phone = rs.getString("customer_phone_no");
        String address = rs.getString("customer_address");
        double amountDue = rs.getDouble("customer_amount_due");
        String finalDate = rs.getString("final_date");
        String nic = rs.getString("nic");
        double totalPaid = rs.getDouble("total_paid");
        
        // Calculate outstanding balance
        double outstanding = amountDue - totalPaid;
        
        // Check if missed due date
        boolean missedDueDate = false;
        try {
            java.util.Date dueDate = DATE_FORMAT.parse(finalDate);
            java.util.Date today = new java.util.Date();
            missedDueDate = dueDate.before(today) && outstanding > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create a new rounded panel for this customer
        lk.com.pos.privateclasses.RoundedPanel customerCard = new lk.com.pos.privateclasses.RoundedPanel();
        customerCard.setBackground(Color.WHITE);
        customerCard.setPreferredSize(new Dimension(420, 450));
        customerCard.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        
        // Customer name section
        javax.swing.JPanel headerPanel = new javax.swing.JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setLayout(new java.awt.BorderLayout());
        
        javax.swing.JLabel nameLabel = new javax.swing.JLabel(customerName);
        nameLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        nameLabel.setForeground(new Color(15, 23, 42));
        
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        
        // Edit button
        javax.swing.JButton editButton = new javax.swing.JButton();
        editButton.setPreferredSize(new Dimension(36, 36));
        editButton.setBackground(new Color(239, 246, 255));
        editButton.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        editButton.setFocusPainted(false);
        editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        FlatSVGIcon blueEdit = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 18, 18);
        editButton.setIcon(blueEdit);
        editButton.addActionListener(evt -> editCustomer(customerId));
        
        // Delete button
        javax.swing.JButton deleteButton = new javax.swing.JButton();
        deleteButton.setPreferredSize(new Dimension(36, 36));
        deleteButton.setBackground(new Color(254, 242, 242));
        deleteButton.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        FlatSVGIcon redDelete = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 18, 18);
        deleteButton.setIcon(redDelete);
        deleteButton.addActionListener(evt -> deleteCustomer(customerId));
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        headerPanel.add(nameLabel, java.awt.BorderLayout.WEST);
        headerPanel.add(buttonPanel, java.awt.BorderLayout.EAST);
        
        // Customer details header with missed due date on right
        javax.swing.JPanel detailsHeaderPanel = new javax.swing.JPanel();
        detailsHeaderPanel.setBackground(Color.WHITE);
        detailsHeaderPanel.setLayout(new java.awt.BorderLayout());
        
        javax.swing.JLabel detailsHeaderLabel = new javax.swing.JLabel("CUSTOMER DETAILS");
        detailsHeaderLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 11));
        detailsHeaderLabel.setForeground(new Color(148, 163, 184));
        
        javax.swing.JLabel missedLabel = new javax.swing.JLabel("⚠ Missed Due Date");
        missedLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 11));
        missedLabel.setForeground(new Color(239, 68, 68));
        missedLabel.setVisible(missedDueDate);
        
        detailsHeaderPanel.add(detailsHeaderLabel, java.awt.BorderLayout.WEST);
        detailsHeaderPanel.add(missedLabel, java.awt.BorderLayout.EAST);
        
        // Customer details grid
        javax.swing.JPanel detailsGrid = new javax.swing.JPanel();
        detailsGrid.setLayout(new java.awt.GridLayout(2, 2, 20, 15));
        detailsGrid.setBackground(Color.WHITE);
        
        detailsGrid.add(createInfoPanel("Phone", phone, new Color(236, 72, 153)));
        detailsGrid.add(createInfoPanel("NIC", nic, new Color(6, 182, 212)));
        detailsGrid.add(createInfoPanel("Final Date", finalDate, new Color(245, 158, 11)));
        detailsGrid.add(createInfoPanel("Amount Due", "Rs. " + PRICE_FORMAT.format(amountDue), new Color(16, 185, 129)));
        
        // Address section
        javax.swing.JPanel addressPanel = createInfoPanel("Address", address, new Color(139, 92, 246));
        
        // Payment details header with button
        javax.swing.JPanel paymentHeaderPanel = new javax.swing.JPanel();
        paymentHeaderPanel.setBackground(Color.WHITE);
        paymentHeaderPanel.setLayout(new java.awt.BorderLayout());
        
        javax.swing.JLabel paymentHeaderLabel = new javax.swing.JLabel("PAYMENT DETAILS");
        paymentHeaderLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 11));
        paymentHeaderLabel.setForeground(new Color(148, 163, 184));
        
        javax.swing.JButton paymentDetailsBtn = new javax.swing.JButton("View Details");
        paymentDetailsBtn.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
        paymentDetailsBtn.setForeground(new Color(59, 130, 246));
        paymentDetailsBtn.setBackground(Color.WHITE);
        paymentDetailsBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        paymentDetailsBtn.setFocusPainted(false);
        paymentDetailsBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        paymentDetailsBtn.addActionListener(evt -> showPaymentDetails(customerId));
        
        paymentHeaderPanel.add(paymentHeaderLabel, java.awt.BorderLayout.WEST);
        paymentHeaderPanel.add(paymentDetailsBtn, java.awt.BorderLayout.EAST);
        
        // Payment cards
        javax.swing.JPanel paymentGrid = new javax.swing.JPanel();
        paymentGrid.setLayout(new java.awt.GridLayout(1, 3, 10, 0));
        paymentGrid.setBackground(Color.WHITE);
        
        paymentGrid.add(createPaymentCard("Amount Due", "Rs. " + PRICE_FORMAT.format(amountDue), 
            new Color(219, 234, 254), new Color(37, 99, 235)));
        paymentGrid.add(createPaymentCard("Paid Amount", "Rs. " + PRICE_FORMAT.format(totalPaid), 
            new Color(209, 250, 229), new Color(22, 163, 74)));
        paymentGrid.add(createPaymentCard("Outstanding", "Rs. " + PRICE_FORMAT.format(outstanding), 
            new Color(254, 243, 199), new Color(234, 88, 12)));
        
        // Layout the customer card
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(customerCard);
        customerCard.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerPanel, 0, 380, Short.MAX_VALUE)
                    .addComponent(detailsHeaderPanel, 0, 380, Short.MAX_VALUE)
                    .addComponent(detailsGrid, 0, 380, Short.MAX_VALUE)
                    .addComponent(addressPanel, 0, 380, Short.MAX_VALUE)
                    .addComponent(paymentHeaderPanel, 0, 380, Short.MAX_VALUE)
                    .addComponent(paymentGrid, 0, 380, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(detailsHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(detailsGrid, 95, 95, 95)
                .addGap(15, 15, 15)
                .addComponent(addressPanel, 60, 60, 60)
                .addGap(18, 18, 18)
                .addComponent(paymentHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(paymentGrid, 80, 80, 80)
                .addGap(20, 20, 20))
        );
        
        return customerCard;
    }

    private javax.swing.JPanel createInfoPanel(String label, String value, Color labelColor) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        
        javax.swing.JLabel labelComponent = new javax.swing.JLabel(label);
        labelComponent.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        labelComponent.setForeground(labelColor);
        labelComponent.setAlignmentX(LEFT_ALIGNMENT);
        
        javax.swing.JLabel valueComponent = new javax.swing.JLabel(value != null ? value : "N/A");
        valueComponent.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        valueComponent.setForeground(new Color(51, 65, 85));
        valueComponent.setAlignmentX(LEFT_ALIGNMENT);
        
        panel.add(labelComponent);
        panel.add(javax.swing.Box.createRigidArea(new Dimension(0, 6)));
        panel.add(valueComponent);
        
        return panel;
    }

    private lk.com.pos.privateclasses.RoundedPanel createPaymentCard(String label, String value, Color bgColor, Color textColor) {
        lk.com.pos.privateclasses.RoundedPanel card = new lk.com.pos.privateclasses.RoundedPanel();
        card.setBackground(bgColor);
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        
        javax.swing.JLabel labelComponent = new javax.swing.JLabel(label);
        labelComponent.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
        labelComponent.setForeground(textColor);
        
        javax.swing.JLabel valueComponent = new javax.swing.JLabel(value);
        valueComponent.setFont(new Font("Nunito ExtraBold", Font.BOLD, 13));
        valueComponent.setForeground(textColor);
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(card);
        card.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labelComponent)
                    .addComponent(valueComponent))
                .addGap(8, 8, 8))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(labelComponent)
                .addGap(8, 8, 8)
                .addComponent(valueComponent)
                .addGap(20, 20, 20))
        );
        
        return card;
    }

    private void showNoDataMessage() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        javax.swing.JPanel noDataPanel = new javax.swing.JPanel();
        noDataPanel.setOpaque(false);
        noDataPanel.setLayout(new javax.swing.BoxLayout(noDataPanel, javax.swing.BoxLayout.Y_AXIS));
        
        javax.swing.JLabel noDataLabel = new javax.swing.JLabel("No customers found");
        noDataLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 18));
        noDataLabel.setForeground(new Color(148, 163, 184));
        noDataLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        javax.swing.JLabel subLabel = new javax.swing.JLabel("Try adjusting your search or filters");
        subLabel.setFont(new Font("Nunito", Font.PLAIN, 14));
        subLabel.setForeground(new Color(203, 213, 225));
        subLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        noDataPanel.add(noDataLabel);
        noDataPanel.add(javax.swing.Box.createRigidArea(new Dimension(0, 8)));
        noDataPanel.add(subLabel);
        
        jPanel2.add(noDataPanel, gbc);
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
                MySQL.executeIUD("DELETE FROM credit_pay WHERE credit_customer_id = " + customerId);
                
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

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jTextField1.setText("Search By Customer Name or NIC");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jRadioButton1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton1.setForeground(new java.awt.Color(255, 51, 51));
        jRadioButton1.setText("Missed Due Date");

        jButton1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton1.setText("Add New Customer");

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
                        .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
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
                .addContainerGap(440, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton1)
                        .addGap(180, 180, 180)
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
                        .addComponent(jRadioButton1)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                .addGap(18, 18, 18))
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
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
