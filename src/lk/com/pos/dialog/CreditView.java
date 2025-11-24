package lk.com.pos.dialog;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.connection.MySQL;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CreditView extends javax.swing.JDialog {

    private int customerId;
    private JPanel creditsContainer;
    private JPanel paymentsContainer;
    private boolean paymentsVisible = false;
    private JLabel discountLabel;
    private JLabel netCreditLabel;
    private double customerDiscount = 0;
    private String customerDiscountType = null;
    private boolean hasCustomerDiscount = false;

    // Keyboard navigation variables
    private List<SelectablePanel> selectablePanels = new ArrayList<>();
    private int currentSelectionIndex = -1;
    private boolean navigationEnabled = true;

    public CreditView(java.awt.Frame parent, boolean modal, int customerId) {
        super(parent, modal);
        this.customerId = customerId;
        initComponents();
        initializeCustomComponents();
        loadCustomerData();
        setupKeyboardNavigation();
    }

    private void initializeCustomComponents() {
        creditsContainer = new JPanel();
        creditsContainer.setLayout(new BoxLayout(creditsContainer, BoxLayout.Y_AXIS));
        creditsContainer.setBackground(Color.WHITE);

        paymentsContainer = new JPanel();
        paymentsContainer.setLayout(new BoxLayout(paymentsContainer, BoxLayout.Y_AXIS));
        paymentsContainer.setBackground(Color.WHITE);

        discountLabel = new JLabel("Rs 0.00");
        discountLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        discountLabel.setForeground(new Color(220, 53, 69));
        discountLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        netCreditLabel = new JLabel("Rs 0.00");
        netCreditLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        netCreditLabel.setForeground(new Color(40, 167, 69));
        netCreditLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        replaceCreditPanel();
        replacePaymentPanel();

        setButtonIcons();

        addBtn.addActionListener(evt -> openAddCreditDialog());
        dropBtn.addActionListener(evt -> togglePaymentsVisibility());

        amount.setText("");
        date.setText("");
        invoiceId.setText("");
        amount1.setText("");
        date1.setText("");
        totalPayed.setText("Loading...");

        jScrollPane1.getViewport().setBackground(Color.WHITE);
        jScrollPane1.setWheelScrollingEnabled(true);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        jPanel1.setBackground(Color.WHITE);
        jPanel1.setPreferredSize(null);

        enableScrollingOnAllComponents(jPanel1);

        jLabel1.setText("AVAILABLE CREDITS");
        jLabel2.setText("PAYMENT HISTORY");
        jLabel3.setText("Credit Overview");

        styleHeaderLabels();
        addDiscountLabelsToLayout();
    }

    private void setupKeyboardNavigation() {
        // Make the dialog focusable and add key listener
        setFocusable(true);
        jPanel1.setFocusable(true);
        jPanel1.requestFocusInWindow();

        jPanel1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!navigationEnabled) {
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        navigateUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        navigateDown();
                        break;
                    case KeyEvent.VK_E:
                        if (currentSelectionIndex >= 0) {
                            editSelectedItem();
                        }
                        break;
                    case KeyEvent.VK_D:
                        togglePaymentsVisibility();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (currentSelectionIndex >= 0) {
                            editSelectedItem();
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        clearSelection();
                        break;
                }
            }
        });

        // Ensure panel maintains focus
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1.requestFocusInWindow();
                clearSelection();
            }
        });
    }

    private void navigateUp() {
        if (selectablePanels.isEmpty()) {
            return;
        }

        if (currentSelectionIndex > 0) {
            currentSelectionIndex--;
        } else {
            // We're at the first item - check if we're in payments and should go back to credits
            if (paymentsVisible && currentSelectionIndex == 0) {
                SelectablePanel current = selectablePanels.get(currentSelectionIndex);
                if (!current.isCredit) {
                    // We're at the first payment item, close payments and select last credit
                    togglePaymentsVisibility();
                    // After closing payments, select the last credit panel
                    SwingUtilities.invokeLater(() -> {
                        if (!selectablePanels.isEmpty()) {
                            // Find the last credit panel
                            for (int i = selectablePanels.size() - 1; i >= 0; i--) {
                                if (selectablePanels.get(i).isCredit) {
                                    currentSelectionIndex = i;
                                    updateSelectionVisual();
                                    scrollToSelectedItem();
                                    break;
                                }
                            }
                        }
                    });
                    return;
                }
            }
            // Cycle to last item
            currentSelectionIndex = selectablePanels.size() - 1;
        }
        updateSelectionVisual();
        scrollToSelectedItem();
    }

    private void navigateDown() {
        if (selectablePanels.isEmpty()) {
            return;
        }

        if (currentSelectionIndex < selectablePanels.size() - 1) {
            currentSelectionIndex++;
        } else {
            // We're at the last item - check if we can open payments
            if (!paymentsVisible && hasPayments()) {
                // Open payments and select the first payment
                togglePaymentsVisibility();
                // After opening payments, select the first payment panel
                SwingUtilities.invokeLater(() -> {
                    if (!selectablePanels.isEmpty()) {
                        // Find the first payment panel
                        for (int i = 0; i < selectablePanels.size(); i++) {
                            if (!selectablePanels.get(i).isCredit) {
                                currentSelectionIndex = i;
                                updateSelectionVisual();
                                scrollToSelectedItem();
                                break;
                            }
                        }
                    }
                });
                return;
            } else {
                // Cycle back to first item
                currentSelectionIndex = 0;
            }
        }
        updateSelectionVisual();
        scrollToSelectedItem();
    }

    private boolean hasPayments() {
        // Check if there are any payment records
        Component[] paymentComponents = paymentsContainer.getComponents();
        for (Component comp : paymentComponents) {
            if (comp instanceof JPanel) {
                Object payIdObj = ((JComponent) comp).getClientProperty("payId");
                if (payIdObj instanceof Integer) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateSelectionVisual() {
        // Clear all borders first
        for (SelectablePanel panel : selectablePanels) {
            panel.setSelected(false);
        }

        // Set border for selected item
        if (currentSelectionIndex >= 0 && currentSelectionIndex < selectablePanels.size()) {
            selectablePanels.get(currentSelectionIndex).setSelected(true);
        }

        jPanel1.repaint();
    }

    private void scrollToSelectedItem() {
        if (currentSelectionIndex >= 0 && currentSelectionIndex < selectablePanels.size()) {
            SelectablePanel selectedPanel = selectablePanels.get(currentSelectionIndex);

            SwingUtilities.invokeLater(() -> {
                try {
                    // Get the absolute position of the panel in the scroll pane
                    int panelY = getAbsoluteY(selectedPanel.panel);

                    if (panelY >= 0) {
                        // Calculate the target scroll position
                        JViewport viewport = jScrollPane1.getViewport();
                        int viewportHeight = viewport.getHeight();
                        int scrollY = panelY - (viewportHeight / 3); // Show panel 1/3 from top

                        // Ensure scroll position is within bounds
                        JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
                        int maxScroll = verticalBar.getMaximum() - viewportHeight;
                        scrollY = Math.max(0, Math.min(scrollY, maxScroll));

                        // Set the scroll position
                        verticalBar.setValue(scrollY);
                    }
                } catch (Exception e) {
                    // Fallback: try simple scrolling
                    try {
                        Rectangle bounds = selectedPanel.panel.getBounds();
                        Container parent = selectedPanel.panel.getParent();
                        if (parent != null) {
                            // Create a rectangle with padding
                            Rectangle targetRect = new Rectangle(
                                    bounds.x,
                                    Math.max(0, bounds.y - 30),
                                    bounds.width,
                                    bounds.height + 60
                            );
                            // Use the viewport to scroll
                            jScrollPane1.getViewport().scrollRectToVisible(targetRect);
                        }
                    } catch (Exception ex) {
                        // Fallback scrolling failed
                    }
                }
            });
        }
    }

    private int getAbsoluteY(Component comp) {
        int y = comp.getY();
        Container parent = comp.getParent();

        // Traverse up the container hierarchy to get absolute Y position
        while (parent != null && parent != jScrollPane1.getViewport().getView()) {
            y += parent.getY();
            parent = parent.getParent();
        }

        return y;
    }

    private void editSelectedItem() {
        if (currentSelectionIndex >= 0 && currentSelectionIndex < selectablePanels.size()) {
            SelectablePanel selectedPanel = selectablePanels.get(currentSelectionIndex);
            selectedPanel.performEditAction();
        }
    }

    private void clearSelection() {
        currentSelectionIndex = -1;
        updateSelectionVisual();
    }

    // Inner class for selectable panels
    private class SelectablePanel {

        private JPanel panel;
        private int creditId;
        private int payId;
        private boolean isCredit;

        public SelectablePanel(JPanel panel, int creditId, int payId, boolean isCredit) {
            this.panel = panel;
            this.creditId = creditId;
            this.payId = payId;
            this.isCredit = isCredit;
        }

        public void setSelected(boolean selected) {
            if (selected) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(8, 147, 176), 3), // Blue selection border
                        BorderFactory.createEmptyBorder(13, 18, 13, 18)
                ));
            } else {
                // Restore original border based on type
                if (isCredit) {
                    panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
                            BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                } else {
                    panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                            BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            }
        }

        public void performEditAction() {
            if (isCredit) {
                openUpdateCreditDialog(creditId);
            } else {
                openUpdateCreditPayDialog(payId);
            }
        }

        public Rectangle getBounds() {
            return panel.getBounds();
        }
    }

    private void addDiscountLabelsToLayout() {
        jPanel1.remove(jLabel4);

        JPanel discountPanel = new JPanel();
        discountPanel.setLayout(new BoxLayout(discountPanel, BoxLayout.Y_AXIS));
        discountPanel.setBackground(Color.WHITE);

        JPanel totalDiscountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        totalDiscountPanel.setBackground(Color.WHITE);

        JLabel discountTitle = new JLabel("TOTAL DISCOUNT : ");
        discountTitle.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        discountTitle.setForeground(new Color(60, 60, 60));

        totalDiscountPanel.add(discountTitle);
        totalDiscountPanel.add(discountLabel);

        JPanel netCreditPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        netCreditPanel.setBackground(Color.WHITE);
        netCreditPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        JLabel netCreditTitle = new JLabel("NET CREDITS : ");
        netCreditTitle.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        netCreditTitle.setForeground(new Color(60, 60, 60));

        netCreditPanel.add(netCreditTitle);
        netCreditPanel.add(netCreditLabel);

        discountPanel.add(totalDiscountPanel);
        discountPanel.add(netCreditPanel);

        jPanel1.add(discountPanel);
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    private void enableScrollingOnAllComponents(Container container) {
        for (Component comp : container.getComponents()) {
            comp.addMouseWheelListener(evt -> {
                JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
                if (verticalBar != null && verticalBar.isVisible()) {
                    int notches = evt.getWheelRotation();
                    int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                    verticalBar.setValue(newValue);
                    evt.consume();
                }
            });

            if (comp instanceof Container) {
                enableScrollingOnAllComponents((Container) comp);
            }
        }
    }

    private void styleHeaderLabels() {
        jLabel1.setFont(new Font("Nunito SemiBold", Font.BOLD, 16));
        jLabel1.setForeground(new Color(60, 60, 60));

        jLabel2.setFont(new Font("Nunito SemiBold", Font.BOLD, 16));
        jLabel2.setForeground(new Color(60, 60, 60));

        jLabel3.setFont(new Font("Nunito ExtraBold", Font.BOLD, 24));
        jLabel3.setForeground(new Color(8, 147, 176));

        totalPayed.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        totalPayed.setForeground(Color.BLACK);
    }

    private void replaceCreditPanel() {
        creditPanel.removeAll();
        creditPanel.setLayout(new BorderLayout());
        creditPanel.setBackground(Color.WHITE);

        creditPanel.addMouseWheelListener(evt -> {
            JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
            if (verticalBar != null && verticalBar.isVisible()) {
                int notches = evt.getWheelRotation();
                int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                verticalBar.setValue(newValue);
                evt.consume();
            }
        });

        creditPanel.add(creditsContainer, BorderLayout.NORTH);
        creditPanel.revalidate();
        creditPanel.repaint();
    }

    private void replacePaymentPanel() {
        creditPayPanel.removeAll();
        creditPayPanel.setLayout(new BorderLayout());
        creditPayPanel.setBackground(Color.WHITE);

        creditPayPanel.addMouseWheelListener(evt -> {
            JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
            if (verticalBar != null && verticalBar.isVisible()) {
                int notches = evt.getWheelRotation();
                int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                verticalBar.setValue(newValue);
                evt.consume();
            }
        });

        creditPayPanel.add(paymentsContainer, BorderLayout.NORTH);
        creditPayPanel.revalidate();
        creditPayPanel.repaint();

        creditPayPanel.setVisible(false);
    }

    private void setButtonIcons() {
        try {
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/credit-add.svg", 15, 15);
            addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
            addBtn.setIcon(addIcon);
            addBtn.setText("");
            addBtn.setBackground(new Color(8, 147, 176));
            addBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            updateDropButtonIcon();

        } catch (Exception e) {
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

        creditPanel1.revalidate();
        creditPanel1.repaint();

        jScrollPane1.revalidate();
        jScrollPane1.repaint();

        SwingUtilities.invokeLater(() -> {
            // Rebuild selectable panels when payments visibility changes
            rebuildSelectablePanels();

            // If we're opening payments and there's no current selection, select the first payment
            if (paymentsVisible && currentSelectionIndex == -1 && hasPayments()) {
                for (int i = 0; i < selectablePanels.size(); i++) {
                    if (!selectablePanels.get(i).isCredit) {
                        currentSelectionIndex = i;
                        updateSelectionVisual();
                        scrollToSelectedItem();
                        break;
                    }
                }
            }
        });
    }

    private void loadCustomerData() {
        selectablePanels.clear();
        currentSelectionIndex = -1;
        loadCustomerDiscount();
        loadCredits();
        loadTotalPayed();
        loadPayments();

        // Rebuild selectable panels after data is loaded
        SwingUtilities.invokeLater(() -> {
            rebuildSelectablePanels();
        });
    }

    private void rebuildSelectablePanels() {
        selectablePanels.clear();

        // Add credit panels
        Component[] creditComponents = creditsContainer.getComponents();
        for (Component comp : creditComponents) {
            if (comp instanceof JPanel) {
                Object creditIdObj = ((JComponent) comp).getClientProperty("creditId");
                if (creditIdObj instanceof Integer) {
                    int creditId = (Integer) creditIdObj;
                    selectablePanels.add(new SelectablePanel((JPanel) comp, creditId, -1, true));
                }
            }
        }

        // Add payment panels if visible
        if (paymentsVisible) {
            Component[] paymentComponents = paymentsContainer.getComponents();
            for (Component comp : paymentComponents) {
                if (comp instanceof JPanel) {
                    Object payIdObj = ((JComponent) comp).getClientProperty("payId");
                    if (payIdObj instanceof Integer) {
                        int payId = (Integer) payIdObj;
                        selectablePanels.add(new SelectablePanel((JPanel) comp, -1, payId, false));
                    }
                }
            }
        }

        // Adjust current selection if it's now invalid
        if (currentSelectionIndex >= selectablePanels.size()) {
            if (!selectablePanels.isEmpty()) {
                currentSelectionIndex = selectablePanels.size() - 1;
            } else {
                currentSelectionIndex = -1;
            }
        }

        // Set initial selection if none exists
        if (!selectablePanels.isEmpty() && currentSelectionIndex == -1) {
            currentSelectionIndex = 0;
        }

        updateSelectionVisual();
    }

    private void loadCustomerDiscount() {
        String customerDiscountQuery = "SELECT d.discount, dt.discount_type "
                + "FROM credit_discount cd "
                + "JOIN discount d ON cd.discount_id = d.discount_id "
                + "JOIN discount_type dt ON d.discount_type_id = dt.discount_type_id "
                + "WHERE cd.credit_id IN (SELECT credit_id FROM credit WHERE credit_customer_id = ?) "
                + "LIMIT 1";

        try {
            java.sql.PreparedStatement pstmt = MySQL.getConnection().prepareStatement(customerDiscountQuery);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                customerDiscount = rs.getDouble("discount");
                customerDiscountType = rs.getString("discount_type");
                hasCustomerDiscount = !rs.wasNull() && customerDiscount > 0;

                if (hasCustomerDiscount) {
                    double totalCreditAmount = getTotalCreditAmount();
                    double totalDiscountAmount = calculateDiscountAmount(totalCreditAmount, customerDiscount, customerDiscountType);
                    double netCreditAmount = totalCreditAmount - totalDiscountAmount;

                    discountLabel.setText("Rs " + String.format("%,.2f", totalDiscountAmount));
                    netCreditLabel.setText("Rs " + String.format("%,.2f", netCreditAmount));
                } else {
                    discountLabel.setText("Rs 0.00");
                    netCreditLabel.setText("Rs " + String.format("%,.2f", getTotalCreditAmount()));
                }
            } else {
                hasCustomerDiscount = false;
                discountLabel.setText("Rs 0.00");
                netCreditLabel.setText("Rs " + String.format("%,.2f", getTotalCreditAmount()));
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            discountLabel.setText("Rs 0.00");
            try {
                netCreditLabel.setText("Rs " + String.format("%,.2f", getTotalCreditAmount()));
            } catch (Exception ex) {
                netCreditLabel.setText("Rs 0.00");
            }
        }
    }

    private double getTotalCreditAmount() {
        String query = "SELECT SUM(credit_amout) as total_amount FROM credit WHERE credit_customer_id = ?";
        try {
            java.sql.PreparedStatement pstmt = MySQL.getConnection().prepareStatement(query);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total_amount");
                return !rs.wasNull() ? total : 0.0;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            // Error getting total credit amount
        }
        return 0.0;
    }

    private void loadCredits() {
        creditsContainer.removeAll();

        String query = "SELECT c.credit_id, c.credit_given_date, c.credit_final_date, "
                + "c.credit_amout, s.invoice_no "
                + "FROM credit c "
                + "LEFT JOIN sales s ON c.sales_id = s.sales_id "
                + "WHERE c.credit_customer_id = ? "
                + "ORDER BY c.credit_given_date DESC";

        try {
            java.sql.PreparedStatement pstmt = MySQL.getConnection().prepareStatement(query);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            boolean hasCredits = false;

            while (rs.next()) {
                hasCredits = true;
                int creditId = rs.getInt("credit_id");
                java.sql.Timestamp givenDate = rs.getTimestamp("credit_given_date");
                java.sql.Date finalDate = rs.getDate("credit_final_date");
                double amount = rs.getDouble("credit_amout");
                String invoiceNo = rs.getString("invoice_no");

                double discountAmount = 0;
                double netAmount = amount;

                if (hasCustomerDiscount) {
                    discountAmount = calculateDiscountAmount(amount, customerDiscount, customerDiscountType);
                    netAmount = amount - discountAmount;
                }

                JPanel creditCard = createCreditCard(creditId, givenDate, finalDate, amount,
                        invoiceNo, hasCustomerDiscount, customerDiscount,
                        customerDiscountType, discountAmount, netAmount);
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

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            showError("Error loading credits: " + e.getMessage());

            JLabel errorLabel = new JLabel("Error loading credits");
            errorLabel.setForeground(Color.RED);
            errorLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
            creditsContainer.add(errorLabel);
        }

        creditsContainer.revalidate();
        creditsContainer.repaint();
        jScrollPane1.revalidate();
        jScrollPane1.repaint();
    }

    private JPanel createCreditCard(int creditId, java.sql.Timestamp givenDate,
            java.sql.Date finalDate, double amount, String invoiceNo,
            boolean hasDiscount, double discount, String discountType,
            double discountAmount, double netAmount) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(240, 245, 240));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Store credit ID for keyboard navigation
        card.putClientProperty("creditId", creditId);

        int cardHeight = hasDiscount ? 120 : 100;
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardHeight));

        card.addMouseWheelListener(evt -> {
            JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
            if (verticalBar != null && verticalBar.isVisible()) {
                int notches = evt.getWheelRotation();
                int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                verticalBar.setValue(newValue);
                evt.consume();
            }
        });

        // Add mouse click listener for selection
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1.requestFocusInWindow();
                selectPanel(card);
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 245, 240));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 245, 240));

        JLabel statusLabel = new JLabel("Active Credit");
        statusLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        statusLabel.setForeground(new Color(80, 80, 80));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(new Color(240, 245, 240));

        JLabel invoiceLabel = new JLabel("#" + (invoiceNo != null ? invoiceNo : "N/A"));
        invoiceLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        invoiceLabel.setForeground(new Color(100, 100, 100));

        JButton editCreditBtn = createEditButton();
        editCreditBtn.addActionListener(e -> openUpdateCreditDialog(creditId));

        rightPanel.add(invoiceLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        rightPanel.add(editCreditBtn);

        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(240, 245, 240));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel amountLabel = new JLabel("Rs " + String.format("%,.2f", amount));
        amountLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        amountLabel.setForeground(Color.BLACK);
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(amountLabel);

        if (hasDiscount) {
            JPanel discountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            discountPanel.setBackground(new Color(240, 245, 240));

            String discountText;
            if ("percentage".equals(discountType)) {
                discountText = String.format("Customer Discount: -%.0f%% (Rs %,.2f)", discount, discountAmount);
            } else {
                discountText = String.format("Customer Discount: -Rs %,.2f", discount);
            }

            JLabel discountLabel = new JLabel(discountText);
            discountLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 12));
            discountLabel.setForeground(new Color(220, 53, 69));

            JLabel netAmountLabel = new JLabel("Net: Rs " + String.format("%,.2f", netAmount));
            netAmountLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 12));
            netAmountLabel.setForeground(new Color(40, 167, 69));
            netAmountLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

            discountPanel.add(discountLabel);
            discountPanel.add(netAmountLabel);
            discountPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            centerPanel.add(discountPanel);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
        String dateRange = "Valid: " + dateFormat.format(givenDate) + " - " + dateFormat.format(finalDate) + ", "
                + new SimpleDateFormat("yyyy").format(finalDate);
        JLabel dateLabel = new JLabel(dateRange);
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(120, 120, 120));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(dateLabel);

        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private void selectPanel(JPanel panel) {
        for (int i = 0; i < selectablePanels.size(); i++) {
            if (selectablePanels.get(i).panel == panel) {
                currentSelectionIndex = i;
                updateSelectionVisual();
                scrollToSelectedItem();
                break;
            }
        }
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
        editBtn.setToolTipText("Edit (E)");

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
        String query = "SELECT SUM(credit_pay_amount) as total_payed FROM credit_pay WHERE credit_customer_id = ?";

        try {
            java.sql.PreparedStatement pstmt = MySQL.getConnection().prepareStatement(query);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

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

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            totalPayed.setText("Total Payed : Rs 0.00");
            showError("Error loading total payments: " + e.getMessage());
        }
    }

    private void loadPayments() {
        paymentsContainer.removeAll();

        String query = "SELECT credit_pay_id, credit_pay_date, credit_pay_amount "
                + "FROM credit_pay "
                + "WHERE credit_customer_id = ? "
                + "ORDER BY credit_pay_date DESC";

        try {
            java.sql.PreparedStatement pstmt = MySQL.getConnection().prepareStatement(query);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            boolean hasPayments = false;

            while (rs.next()) {
                hasPayments = true;
                int payId = rs.getInt("credit_pay_id");
                java.sql.Timestamp payDate = rs.getTimestamp("credit_pay_date");
                double amount = rs.getDouble("credit_pay_amount");

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

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            showError("Error loading payments: " + e.getMessage());

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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Store payment ID for keyboard navigation
        card.putClientProperty("payId", payId);

        card.addMouseWheelListener(evt -> {
            JScrollBar verticalBar = jScrollPane1.getVerticalScrollBar();
            if (verticalBar != null && verticalBar.isVisible()) {
                int notches = evt.getWheelRotation();
                int newValue = verticalBar.getValue() + (notches * verticalBar.getUnitIncrement());
                verticalBar.setValue(newValue);
                evt.consume();
            }
        });

        // Add mouse click listener for selection
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1.requestFocusInWindow();
                selectPanel(card);
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JLabel amountLabel = new JLabel("Rs " + String.format("%,.2f", amount));
        amountLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        amountLabel.setForeground(Color.BLACK);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a");
        JLabel dateLabel = new JLabel(dateFormat.format(payDate));
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(120, 120, 120));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);

        JButton editPaymentBtn = createEditButton();
        editPaymentBtn.addActionListener(e -> openUpdateCreditPayDialog(payId));
        editPaymentBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(editPaymentBtn);

        contentPanel.add(amountLabel, BorderLayout.WEST);
        contentPanel.add(dateLabel, BorderLayout.CENTER);
        contentPanel.add(rightPanel, BorderLayout.EAST);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private double calculateDiscountAmount(double totalAmount, double discount, String discountType) {
        if ("percentage".equals(discountType)) {
            return totalAmount * discount / 100;
        } else {
            return discount;
        }
    }

    private void openAddCreditDialog() {
        try {
            navigationEnabled = false;
            AddCredit dialog = new AddCredit(null, true);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            navigationEnabled = true;
            jPanel1.requestFocusInWindow();

            loadCustomerData();
        } catch (Exception e) {
            navigationEnabled = true;
            showError("Error opening add credit dialog: " + e.getMessage());
        }
    }

    private void openUpdateCreditDialog(int creditId) {
        try {
            navigationEnabled = false;
            UpdateCredit dialog = new UpdateCredit(null, true, creditId);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            navigationEnabled = true;
            jPanel1.requestFocusInWindow();

            loadCustomerData();
        } catch (Exception e) {
            navigationEnabled = true;
            showError("Error opening update credit dialog: " + e.getMessage());
        }
    }

    private void openUpdateCreditPayDialog(int creditPayId) {
        try {
            navigationEnabled = false;
            UpdateCreditPay dialog = new UpdateCreditPay(null, true, creditPayId);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            navigationEnabled = true;
            jPanel1.requestFocusInWindow();

            loadCustomerData();
        } catch (Exception e) {
            navigationEnabled = true;
            showError("Error opening update payment dialog: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Override setVisible to ensure focus when dialog is shown
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            SwingUtilities.invokeLater(() -> {
                jPanel1.requestFocusInWindow();
                if (!selectablePanels.isEmpty() && currentSelectionIndex == -1) {
                    currentSelectionIndex = 0;
                    updateSelectionVisual();
                }
            });
        }
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
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(551, 522));
        jPanel1.setPreferredSize(new java.awt.Dimension(551, 522));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });

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
                    .addComponent(invoiceId)
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
                .addContainerGap(26, Short.MAX_VALUE))
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

        jLabel4.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel4.setText("DISCOUNT : ");

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
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jSeparator3)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)
                                .addComponent(creditPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(creditPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 32, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(7, 7, 7)
                .addComponent(creditPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creditPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(115, Short.MAX_VALUE))
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

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
        jPanel1.requestFocusInWindow();
        clearSelection();
    }//GEN-LAST:event_jPanel1MouseClicked

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        jPanel1.requestFocusInWindow();
    }//GEN-LAST:event_formWindowActivated

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
                CreditView dialog = new CreditView(new javax.swing.JFrame(), true, 9); // Example customer ID
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
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel totalPayed;
    // End of variables declaration//GEN-END:variables
}
