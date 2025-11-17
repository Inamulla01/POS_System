package lk.com.pos.dialog;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.connection.MySQL;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CreditView extends javax.swing.JDialog {

    private int customerId;
    private JPanel creditsContainer;
    private JPanel paymentsContainer;
    private boolean paymentsVisible = false;

    public CreditView(java.awt.Frame parent, boolean modal, int customerId) {
        super(parent, modal);
        this.customerId = customerId;
        initComponents();
        initializeCustomComponents();
        loadCustomerData();
    }

    private void initializeCustomComponents() {
        // Create containers for dynamic content
        creditsContainer = new JPanel();
        creditsContainer.setLayout(new BoxLayout(creditsContainer, BoxLayout.Y_AXIS));
        creditsContainer.setBackground(Color.WHITE);
        
        paymentsContainer = new JPanel();
        paymentsContainer.setLayout(new BoxLayout(paymentsContainer, BoxLayout.Y_AXIS));
        paymentsContainer.setBackground(Color.WHITE);

        // Replace the static panels with our dynamic containers
        replaceCreditPanel();
        replacePaymentPanel();

        // Set icons for buttons
        setButtonIcons();
        
        // Add action listeners
        addBtn.addActionListener(evt -> openAddCreditDialog());
        dropBtn.addActionListener(evt -> togglePaymentsVisibility());
        
        // Hide static content that will be replaced
        amount.setText("");
        date.setText("");
        invoiceId.setText("");
        amount1.setText("");
        date1.setText("");
        totalPayed.setText("Loading...");
        
        // Fix scroll pane settings - Enable mouse wheel scrolling
        jScrollPane1.getViewport().setBackground(Color.WHITE);
        jScrollPane1.setWheelScrollingEnabled(true);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        
        jPanel1.setBackground(Color.WHITE);
        
        // Remove fixed size constraints for proper scrolling
        jPanel1.setPreferredSize(null);
        
        // Enable scrolling on all components
        enableScrollingOnAllComponents(jPanel1);
        
        // Update labels to match the design
        jLabel1.setText("AVAILABLE CREDITS");
        jLabel2.setText("PAYMENT HISTORY");
        jLabel3.setText("Credit Overview");
        
        // Style adjustments to match the design
        styleHeaderLabels();
    }

    private void enableScrollingOnAllComponents(Container container) {
        // Enable mouse wheel scrolling for all components in the container
        for (Component comp : container.getComponents()) {
            // Add mouse wheel listener to forward events to the scroll pane
            comp.addMouseWheelListener(evt -> {
                JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
                if (verticalBar != null && verticalBar.isVisible()) {
                    int notches = evt.getWheelRotation();
                    int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                    verticalBar.setValue(newValue);
                    evt.consume();
                }
            });
            
            // Recursively process child containers
            if (comp instanceof Container) {
                enableScrollingOnAllComponents((Container) comp);
            }
        }
    }

    private void styleHeaderLabels() {
        // Style for AVAILABLE CREDITS label
        jLabel1.setFont(new Font("Nunito SemiBold", Font.BOLD, 16));
        jLabel1.setForeground(new Color(60, 60, 60));
        
        // Style for PAYMENT HISTORY label
        jLabel2.setFont(new Font("Nunito SemiBold", Font.BOLD, 16));
        jLabel2.setForeground(new Color(60, 60, 60));
        
        // Style for Credit Overview title
        jLabel3.setFont(new Font("Nunito ExtraBold", Font.BOLD, 24));
        jLabel3.setForeground(new Color(8, 147, 176));
        
        // Style for total payments label
        totalPayed.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        totalPayed.setForeground(Color.BLACK);
    }

    private void replaceCreditPanel() {
        // Remove all components from creditPanel
        creditPanel.removeAll();
        creditPanel.setLayout(new BorderLayout());
        creditPanel.setBackground(Color.WHITE);
        
        // Enable scrolling on credit panel
        creditPanel.addMouseWheelListener(evt -> {
            JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
            if (verticalBar != null && verticalBar.isVisible()) {
                int notches = evt.getWheelRotation();
                int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                verticalBar.setValue(newValue);
                evt.consume();
            }
        });
        
        // Add the credits container directly
        creditPanel.add(creditsContainer, BorderLayout.NORTH);
        
        creditPanel.revalidate();
        creditPanel.repaint();
    }

    private void replacePaymentPanel() {
        // Remove all components from creditPayPanel
        creditPayPanel.removeAll();
        creditPayPanel.setLayout(new BorderLayout());
        creditPayPanel.setBackground(Color.WHITE);
        
        // Enable scrolling on payment panel
        creditPayPanel.addMouseWheelListener(evt -> {
            JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
            if (verticalBar != null && verticalBar.isVisible()) {
                int notches = evt.getWheelRotation();
                int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                verticalBar.setValue(newValue);
                evt.consume();
            }
        });
        
        // Add payments container directly
        creditPayPanel.add(paymentsContainer, BorderLayout.NORTH);
        
        creditPayPanel.revalidate();
        creditPayPanel.repaint();
        
        // Initially hide the payments
        creditPayPanel.setVisible(false);
    }

    private void setButtonIcons() {
        try {
            // Set icons using FlatSVGIcon
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/credit-add.svg", 15, 15);
            addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
            addBtn.setIcon(addIcon);
            addBtn.setText("");
            addBtn.setBackground(new Color(8, 147, 176));
            addBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            updateDropButtonIcon();
            
        } catch (Exception e) {
            System.out.println("Icons not available, using text buttons: " + e.getMessage());
            // Fallback to text
            addBtn.setText("A");
            dropBtn.setText("▼");
        }
    }

    private void updateDropButtonIcon() {
        try {
            if (paymentsVisible) {
                FlatSVGIcon upArrowIcon = new FlatSVGIcon("lk/com/pos/icon/dropUp.svg", 22, 22);
                upArrowIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#666666")));
                dropBtn.setIcon(upArrowIcon);
                dropBtn.setText("");
            } else {
                FlatSVGIcon downArrowIcon = new FlatSVGIcon("lk/com/pos/icon/dropDown.svg", 22, 22);
                downArrowIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#666666")));
                dropBtn.setIcon(downArrowIcon);
                dropBtn.setText("");
            }
        } catch (Exception e) {
            if (paymentsVisible) {
                dropBtn.setText("▲");
            } else {
                dropBtn.setText("▼");
            }
            dropBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        }
    }

    private void togglePaymentsVisibility() {
        paymentsVisible = !paymentsVisible;
        creditPayPanel.setVisible(paymentsVisible);
        updateDropButtonIcon();
        
        // Force layout update
        creditPanel1.revalidate();
        creditPanel1.repaint();
        
        // Update the main scroll pane
        jScrollPane1.revalidate();
        jScrollPane1.repaint();
        
        // Ensure proper scrolling
        SwingUtilities.invokeLater(() -> {
            if (paymentsVisible) {
                // Scroll to show the expanded payments section
                Rectangle visibleRect = creditPanel1.getVisibleRect();
                creditPanel1.scrollRectToVisible(visibleRect);
            }
        });
    }

    private void loadCustomerData() {
        System.out.println("Loading data for customer ID: " + customerId);
        loadCredits();
        loadTotalPayed();
        loadPayments();
    }

    private void loadCredits() {
        creditsContainer.removeAll();
        
        String query = "SELECT c.credit_id, c.credit_given_date, c.credit_final_date, " +
                      "c.credit_amout, s.invoice_no " +
                      "FROM credit c " +
                      "LEFT JOIN sales s ON c.sales_id = s.sales_id " +
                      "WHERE c.credit_customer_id = " + customerId + " " +
                      "ORDER BY c.credit_given_date DESC";
        
        System.out.println("Executing credit query: " + query);
        
        try {
            ResultSet rs = MySQL.executeSearch(query);
            boolean hasCredits = false;
            
            while (rs.next()) {
                hasCredits = true;
                int creditId = rs.getInt("credit_id");
                java.sql.Timestamp givenDate = rs.getTimestamp("credit_given_date");
                java.sql.Date finalDate = rs.getDate("credit_final_date");
                double amount = rs.getDouble("credit_amout");
                String invoiceNo = rs.getString("invoice_no");
                
                System.out.println("Found credit: ID=" + creditId + ", Amount=" + amount + ", Invoice=" + invoiceNo);
                
                JPanel creditCard = createCreditCard(creditId, givenDate, finalDate, amount, invoiceNo);
                creditsContainer.add(creditCard);
                creditsContainer.add(Box.createRigidArea(new Dimension(0, 12)));
            }
            
            if (!hasCredits) {
                JLabel noCreditsLabel = new JLabel("No credit records found");
                noCreditsLabel.setFont(new Font("Nunito SemiBold", Font.ITALIC, 14));
                noCreditsLabel.setForeground(Color.GRAY);
                noCreditsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                noCreditsLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
                creditsContainer.add(noCreditsLabel);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading credits: " + e.getMessage());
            
            // Add error label to container
            JLabel errorLabel = new JLabel("Error loading credits");
            errorLabel.setForeground(Color.RED);
            errorLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
            creditsContainer.add(errorLabel);
        }
        
        creditsContainer.revalidate();
        creditsContainer.repaint();
        
        // Update scroll pane
        jScrollPane1.revalidate();
        jScrollPane1.repaint();
    }

    private JPanel createCreditCard(int creditId, java.sql.Timestamp givenDate, 
                                   java.sql.Date finalDate, double amount, String invoiceNo) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(240, 245, 240)); // Light green background
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Enable scrolling on credit card
        card.addMouseWheelListener(evt -> {
            JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
            if (verticalBar != null && verticalBar.isVisible()) {
                int notches = evt.getWheelRotation();
                int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                verticalBar.setValue(newValue);
                evt.consume();
            }
        });

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 245, 240));

        // Top panel with status, invoice ID and edit button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 245, 240));
        
        JLabel statusLabel = new JLabel("Active Credit");
        statusLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        statusLabel.setForeground(new Color(80, 80, 80));
        
        // Right panel for invoice ID and edit button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(new Color(240, 245, 240));
        
        JLabel invoiceLabel = new JLabel("#" + (invoiceNo != null ? invoiceNo : "N/A"));
        invoiceLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        invoiceLabel.setForeground(new Color(100, 100, 100));
        
        // Edit button for credit - positioned in top right corner
        JButton editCreditBtn = createEditButton();
        editCreditBtn.addActionListener(e -> openUpdateCreditDialog(creditId));
        
        rightPanel.add(invoiceLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        rightPanel.add(editCreditBtn);
        
        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Center panel with amount
        JLabel amountLabel = new JLabel("Rs " + String.format("%,.2f", amount));
        amountLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        amountLabel.setForeground(Color.BLACK);

        // Bottom panel with dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
        String dateRange = "Valid: " + dateFormat.format(givenDate) + " - " + dateFormat.format(finalDate) + ", " + 
                         new SimpleDateFormat("yyyy").format(finalDate);
        JLabel dateLabel = new JLabel(dateRange);
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(120, 120, 120));

        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(amountLabel, BorderLayout.CENTER);
        contentPanel.add(dateLabel, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JButton createEditButton() {
        JButton editBtn = new JButton();
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/edit.svg", 16, 16);
            editIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#666666")));
            editBtn.setIcon(editIcon);
        } catch (Exception e) {
            editBtn.setText("✏️");
            editBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
        }
        editBtn.setPreferredSize(new Dimension(30, 30));
        editBtn.setBackground(Color.WHITE);
        editBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.setToolTipText("Edit");
        
        // Add hover effect
        editBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                editBtn.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                editBtn.setBackground(Color.WHITE);
            }
        });
        
        return editBtn;
    }

    private void loadTotalPayed() {
        String query = "SELECT SUM(credit_pay_amount) as total_payed " +
                      "FROM credit_pay " +
                      "WHERE credit_customer_id = " + customerId;
        
        try {
            ResultSet rs = MySQL.executeSearch(query);
            if (rs.next()) {
                double total = rs.getDouble("total_payed");
                if (!rs.wasNull()) {
                    totalPayed.setText("Total Payed : Rs " + String.format("%,.2f", total));
                } else {
                    totalPayed.setText("Total Payed : Rs 0.00");
                }
            } else {
                totalPayed.setText("Rs 0.00");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            totalPayed.setText("Total Payed : Rs 0.00");
            showError("Error loading total payments: " + e.getMessage());
        }
    }

    private void loadPayments() {
        paymentsContainer.removeAll();
        
        String query = "SELECT credit_pay_id, credit_pay_date, credit_pay_amount " +
                      "FROM credit_pay " +
                      "WHERE credit_customer_id = " + customerId + " " +
                      "ORDER BY credit_pay_date DESC";
        
        System.out.println("Executing payment query: " + query);
        
        try {
            ResultSet rs = MySQL.executeSearch(query);
            boolean hasPayments = false;
            
            while (rs.next()) {
                hasPayments = true;
                int payId = rs.getInt("credit_pay_id");
                java.sql.Timestamp payDate = rs.getTimestamp("credit_pay_date");
                double amount = rs.getDouble("credit_pay_amount");
                
                System.out.println("Found payment: ID=" + payId + ", Amount=" + amount + ", Date=" + payDate);
                
                JPanel paymentCard = createPaymentCard(payId, payDate, amount);
                paymentsContainer.add(paymentCard);
                paymentsContainer.add(Box.createRigidArea(new Dimension(0, 8)));
            }
            
            if (!hasPayments) {
                JLabel noPaymentsLabel = new JLabel("No payment records found");
                noPaymentsLabel.setFont(new Font("Nunito SemiBold", Font.ITALIC, 12));
                noPaymentsLabel.setForeground(Color.GRAY);
                noPaymentsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                noPaymentsLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
                paymentsContainer.add(noPaymentsLabel);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading payments: " + e.getMessage());
            
            // Add error label to container
            JLabel errorLabel = new JLabel("Error loading payments");
            errorLabel.setForeground(Color.RED);
            errorLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
            paymentsContainer.add(errorLabel);
        }
        
        paymentsContainer.revalidate();
        paymentsContainer.repaint();
    }

    private JPanel createPaymentCard(int payId, java.sql.Timestamp payDate, double amount) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Increased height for date

        // Enable scrolling on payment card
        card.addMouseWheelListener(evt -> {
            JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
            if (verticalBar != null && verticalBar.isVisible()) {
                int notches = evt.getWheelRotation();
                int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                verticalBar.setValue(newValue);
                evt.consume();
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        // Left side - Amount
        JLabel amountLabel = new JLabel("Rs " + String.format("%,.2f", amount));
        amountLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        amountLabel.setForeground(Color.BLACK);

        // Center - Date and Time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a");
        JLabel dateLabel = new JLabel(dateFormat.format(payDate));
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(120, 120, 120));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Right side - Status and Edit button
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        

        
        // Edit button
        JButton editPaymentBtn = createEditButton();
        editPaymentBtn.addActionListener(e -> openUpdateCreditPayDialog(payId));
        editPaymentBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Space between status and button
        rightPanel.add(editPaymentBtn);

        contentPanel.add(amountLabel, BorderLayout.WEST);
        contentPanel.add(dateLabel, BorderLayout.CENTER);
        contentPanel.add(rightPanel, BorderLayout.EAST);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private void openAddCreditDialog() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddCredit dialog = new AddCredit(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            
            // Refresh data after dialog closes
            loadCustomerData();
        } catch (Exception e) {
            showError("Error opening add credit dialog: " + e.getMessage());
        }
    }

    private void openUpdateCreditDialog(int creditId) {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            UpdateCredit dialog = new UpdateCredit(parentFrame, true, creditId);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            
            // Refresh data after dialog closes
            loadCustomerData();
        } catch (Exception e) {
            showError("Error opening update credit dialog: " + e.getMessage());
        }
    }

    private void openUpdateCreditPayDialog(int creditPayId) {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            UpdateCreditPay dialog = new UpdateCreditPay(parentFrame, true, creditPayId);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            
            // Refresh data after dialog closes
            loadCustomerData();
        } catch (Exception e) {
            showError("Error opening update payment dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        creditPanel = new javax.swing.JPanel();
        date = new javax.swing.JLabel();
        editCreditBtn = new javax.swing.JButton();
        amount = new javax.swing.JLabel();
        invoiceId = new javax.swing.JLabel();
        creditPanel1 = new javax.swing.JPanel();
        dropBtn = new javax.swing.JButton();
        totalPayed = new javax.swing.JLabel();
        creditPayPanel = new javax.swing.JPanel();
        date1 = new javax.swing.JLabel();
        editPaymentBtn = new javax.swing.JButton();
        amount1 = new javax.swing.JLabel();
        addBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(551, 522));
        jPanel1.setPreferredSize(new java.awt.Dimension(551, 522));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Credit Details View");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        creditPanel.setBackground(new java.awt.Color(255, 255, 255));

        date.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        date.setText("02/30/2025 - 03/30/2025");

        editCreditBtn.setPreferredSize(new java.awt.Dimension(33, 33));
        editCreditBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCreditBtnActionPerformed(evt);
            }
        });

        amount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 35)); // NOI18N
        amount.setText("Rs 20,000");

        invoiceId.setFont(new java.awt.Font("Nunito SemiBold", 3, 16)); // NOI18N
        invoiceId.setForeground(new java.awt.Color(102, 102, 102));
        invoiceId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        invoiceId.setText("#0000111");

        javax.swing.GroupLayout creditPanelLayout = new javax.swing.GroupLayout(creditPanel);
        creditPanel.setLayout(creditPanelLayout);
        creditPanelLayout.setHorizontalGroup(
            creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(creditPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creditPanelLayout.createSequentialGroup()
                        .addComponent(invoiceId)
                        .addGap(216, 216, 216))
                    .addComponent(amount, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(date))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(editCreditBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        creditPanelLayout.setVerticalGroup(
            creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, creditPanelLayout.createSequentialGroup()
                .addGroup(creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creditPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(editCreditBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(creditPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(invoiceId)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(amount, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        creditPanel1.setBackground(new java.awt.Color(204, 204, 204));

        dropBtn.setPreferredSize(new java.awt.Dimension(33, 33));

        totalPayed.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        totalPayed.setText("Rs 20,000");

        creditPayPanel.setBackground(new java.awt.Color(255, 255, 255));

        date1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        date1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        date1.setText("02/30/2025 10.20.20");

        editPaymentBtn.setPreferredSize(new java.awt.Dimension(33, 33));

        amount1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 35)); // NOI18N
        amount1.setText("Rs 10,000");

        javax.swing.GroupLayout creditPayPanelLayout = new javax.swing.GroupLayout(creditPayPanel);
        creditPayPanel.setLayout(creditPayPanelLayout);
        creditPayPanelLayout.setHorizontalGroup(
            creditPayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(creditPayPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(creditPayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creditPayPanelLayout.createSequentialGroup()
                        .addComponent(date1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(creditPayPanelLayout.createSequentialGroup()
                        .addComponent(amount1, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editPaymentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        creditPayPanelLayout.setVerticalGroup(
            creditPayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(creditPayPanelLayout.createSequentialGroup()
                .addGroup(creditPayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creditPayPanelLayout.createSequentialGroup()
                        .addGap(0, 10, Short.MAX_VALUE)
                        .addComponent(amount1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(creditPayPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(editPaymentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(date1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout creditPanel1Layout = new javax.swing.GroupLayout(creditPanel1);
        creditPanel1.setLayout(creditPanel1Layout);
        creditPanel1Layout.setHorizontalGroup(
            creditPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(creditPanel1Layout.createSequentialGroup()
                .addGroup(creditPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creditPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(totalPayed, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dropBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(creditPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(creditPayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        creditPanel1Layout.setVerticalGroup(
            creditPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(creditPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(creditPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalPayed, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dropBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creditPayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(7, 7, 7))
        );

        addBtn.setPreferredSize(new java.awt.Dimension(33, 33));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel1.setText("CREDIT DETAILS");

        jLabel2.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel2.setText("CREDIT PAYMENT DETAILS");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator3)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(creditPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(creditPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 32, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addGap(7, 7, 7)
                .addComponent(creditPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creditPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(150, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addBtnActionPerformed

    private void editCreditBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCreditBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editCreditBtnActionPerformed

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
            java.util.logging.Logger.getLogger(CreditView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CreditView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CreditView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CreditView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // This main method is for testing only
                // In your actual application, use: new CreditView(parent, modal, customerId)
                CreditView dialog = new CreditView(new javax.swing.JFrame(), true, 3); // Example customer ID
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
    private javax.swing.JButton addBtn;
    private javax.swing.JLabel amount;
    private javax.swing.JLabel amount1;
    private javax.swing.JPanel creditPanel;
    private javax.swing.JPanel creditPanel1;
    private javax.swing.JPanel creditPayPanel;
    private javax.swing.JLabel date;
    private javax.swing.JLabel date1;
    private javax.swing.JButton dropBtn;
    private javax.swing.JButton editCreditBtn;
    private javax.swing.JButton editPaymentBtn;
    private javax.swing.JLabel invoiceId;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel totalPayed;
    // End of variables declaration//GEN-END:variables
}
