package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import lk.com.pos.dialogpanel.ExchangeProduct;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ExchangeProductDialog extends javax.swing.JDialog {

    private Map<Integer, String> invoiceMap = new HashMap<>();
    private Map<Integer, String> reasonMap = new HashMap<>();
    private Map<Integer, Integer> productOriginalQtys = new HashMap<>();
    private Map<Integer, Integer> productSaleItemIds = new HashMap<>();
    private Map<Integer, Integer> productStockIds = new HashMap<>();
    private Map<Integer, Double> productUnitPrices = new HashMap<>();
    private Map<Integer, Double> productDiscountPrices = new HashMap<>();
    private Map<Integer, Boolean> productSelectedStatus = new HashMap<>();
    private double invoiceTotalDiscount = 0.0;
    private String invoiceDiscountType = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH.mm.ss");
    private int currentSalesId = -1;
    private int currentUserId = 1;
    private int currentCreditCustomerId = -1;
    private double currentCreditAmount = 0.0;
    private boolean isCreditPayment = false;

    public ExchangeProductDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        loadInvoices();
        loadReturnReasons();

        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        showNoInvoiceSelectedMessage();
    }

    private void loadInvoices() {
        try {
            String query = "SELECT s.sales_id, s.invoice_no, s.datetime, s.total, "
                    + "pm.payment_method_name, cc.customer_name "
                    + "FROM sales s "
                    + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                    + "LEFT JOIN credit_customer cc ON s.credit_customer_id = cc.customer_id "
                    + "WHERE s.status_id = 1 ORDER BY s.sales_id DESC";
            ResultSet rs = MySQL.executeSearch(query);

            invoiceMap.clear();
            invoiceCombo.removeAllItems();
            invoiceCombo.addItem("Select Invoice");

            // Create a custom renderer for the combo box
            invoiceCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                    if (index > 0 && value instanceof String) {
                        String invoiceNo = (String) value;
                        // Find the sales ID for this invoice
                        int salesId = -1;
                        for (Map.Entry<Integer, String> entry : invoiceMap.entrySet()) {
                            if (entry.getValue().equals(invoiceNo)) {
                                salesId = entry.getKey();
                                break;
                            }
                        }

                        if (salesId != -1) {
                            try {
                                String detailQuery = "SELECT s.datetime, s.total, "
                                        + "pm.payment_method_name, cc.customer_name "
                                        + "FROM sales s "
                                        + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                                        + "LEFT JOIN credit_customer cc ON s.credit_customer_id = cc.customer_id "
                                        + "WHERE s.sales_id = " + salesId;

                                ResultSet detailRs = MySQL.executeSearch(detailQuery);
                                if (detailRs.next()) {
                                    Timestamp timestamp = detailRs.getTimestamp("datetime");
                                    String formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(timestamp);
                                    double total = detailRs.getDouble("total");
                                    String paymentMethod = detailRs.getString("payment_method_name");
                                    String customerName = detailRs.getString("customer_name");

                                    String displayText = String.format("%s | %s | Rs.%.2f | %s | %s",
                                            invoiceNo,
                                            formattedDate,
                                            total,
                                            paymentMethod != null ? paymentMethod.replace(" Payment", "") : "Unknown",
                                            customerName != null ? customerName : "Walk-in Customer");

                                    setText(displayText);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return this;
                }
            });

            while (rs.next()) {
                int salesId = rs.getInt("sales_id");
                String invoiceNo = rs.getString("invoice_no");
                invoiceMap.put(salesId, invoiceNo);

                // Add just the invoice number - the renderer will handle the display
                invoiceCombo.addItem(invoiceNo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading invoices: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReturnReasons() {
        try {
            String query = "SELECT return_reason_id, reason FROM return_reason";
            ResultSet rs = MySQL.executeSearch(query);

            reasonMap.clear();
            reasonCombo.removeAllItems();
            reasonCombo.addItem("Select Reason");

            while (rs.next()) {
                int reasonId = rs.getInt("return_reason_id");
                String reason = rs.getString("reason");
                reasonMap.put(reasonId, reason);
                reasonCombo.addItem(reason);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading return reasons: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showNoInvoiceSelectedMessage() {
        productsPanel.removeAll();

        JPanel messagePanel = new JPanel();
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setPreferredSize(new Dimension(600, 200));

        JLabel messageLabel = new JLabel("Please select an invoice to view details", JLabel.CENTER);
        messageLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 16));
        messageLabel.setForeground(new Color(150, 150, 150));

        messagePanel.add(messageLabel, BorderLayout.CENTER);
        productsPanel.add(messagePanel);

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void loadInvoiceDetails(String invoiceNo) {
        try {
            productsPanel.removeAll();
            productOriginalQtys.clear();
            productSaleItemIds.clear();
            productStockIds.clear();
            productUnitPrices.clear();
            productDiscountPrices.clear();
            productSelectedStatus.clear();
            invoiceTotalDiscount = 0.0;
            invoiceDiscountType = null;
            currentSalesId = -1;
            currentCreditCustomerId = -1;
            currentCreditAmount = 0.0;
            isCreditPayment = false;

            String query = "SELECT s.sales_id, s.datetime, s.total, s.invoice_no, "
                    + "u.name as cashier_name, pm.payment_method_name, "
                    + "cc.customer_id, cc.customer_name, d.discount, dt.discount_type, "
                    + "s.discount_id, s.credit_customer_id, s.payment_method_id "
                    + "FROM sales s "
                    + "LEFT JOIN user u ON s.user_id = u.user_id "
                    + "LEFT JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id "
                    + "LEFT JOIN credit_customer cc ON s.credit_customer_id = cc.customer_id "
                    + "LEFT JOIN discount d ON s.discount_id = d.discount_id "
                    + "LEFT JOIN discount_type dt ON d.discount_type_id = dt.discount_type_id "
                    + "WHERE s.invoice_no = '" + invoiceNo + "'";

            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                currentSalesId = rs.getInt("sales_id");

                // Check if this is a credit payment
                int paymentMethodId = rs.getInt("payment_method_id");
                isCreditPayment = (paymentMethodId == 3); // 3 = Credit Payment

                if (isCreditPayment) {
                    currentCreditCustomerId = rs.getInt("credit_customer_id");
                    // Load current credit amount for this customer
                    loadCurrentCreditAmount();
                }

                JPanel invoiceHeaderPanel = createInvoiceHeaderPanel(rs);
                productsPanel.add(invoiceHeaderPanel);
                productsPanel.add(Box.createVerticalStrut(10));

                Double invoiceDiscount = rs.getDouble("discount");
                String discountType = rs.getString("discount_type");
                boolean hasInvoiceDiscount = rs.getObject("discount_id") != null;

                if (hasInvoiceDiscount && invoiceDiscount != null && discountType != null) {
                    invoiceTotalDiscount = invoiceDiscount;
                    invoiceDiscountType = discountType;
                }

                loadInvoiceProducts(currentSalesId, hasInvoiceDiscount);
            }

            productsPanel.revalidate();
            productsPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading invoice details: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCurrentCreditAmount() {
        try {
            String query = "SELECT credit_amout FROM credit WHERE credit_customer_id = ? ORDER BY credit_id DESC LIMIT 1";
            PreparedStatement stmt = MySQL.getConnection().prepareStatement(query);
            stmt.setInt(1, currentCreditCustomerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentCreditAmount = rs.getDouble("credit_amout");
            } else {
                currentCreditAmount = 0.0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading credit amount: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double getTotalCreditPayments() {
        try {
            String query = "SELECT SUM(credit_pay_amount) as total_paid FROM credit_pay cp "
                    + "JOIN credit c ON cp.credit_id = c.credit_id "
                    + "WHERE c.credit_customer_id = ?";
            PreparedStatement stmt = MySQL.getConnection().prepareStatement(query);
            stmt.setInt(1, currentCreditCustomerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_paid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private JPanel createInvoiceHeaderPanel(ResultSet rs) throws SQLException {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setLayout(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 5));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Left side - Invoice details
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel invoiceNoLabel = new JLabel("#" + rs.getString("invoice_no"));
        invoiceNoLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        invoiceNoLabel.setForeground(new Color(0, 0, 0));
        invoiceNoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String customerName = rs.getString("customer_name");
        JLabel customerLabel = new JLabel(customerName != null ? customerName : "Customer Name");
        customerLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        customerLabel.setForeground(new Color(102, 102, 102));
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Timestamp timestamp = rs.getTimestamp("datetime");
        String formattedDate = dateFormat.format(timestamp);
        JLabel dateLabel = new JLabel(formattedDate);
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(102, 102, 102));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fixed Discount Display Logic
        Double invoiceDiscount = rs.getDouble("discount");
        String discountType = rs.getString("discount_type");
        Object discountId = rs.getObject("discount_id");
        JLabel discountLabel = new JLabel();
        discountLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        discountLabel.setForeground(new Color(255, 0, 0));
        discountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (discountId != null && invoiceDiscount != null && invoiceDiscount > 0) {
            String discountText = "Discount ";
            if ("percentage".equalsIgnoreCase(discountType)) {
                double discountAmount = rs.getDouble("total") * invoiceDiscount / 100;
                discountText += String.format("%.0f%% = Rs %.2f", invoiceDiscount, discountAmount);
            } else {
                discountText += "Rs " + String.format("%.2f", invoiceDiscount);
            }
            discountLabel.setText(discountText);
        } else {
            discountLabel.setText("No Discount");
        }

        leftPanel.add(invoiceNoLabel);
        leftPanel.add(Box.createVerticalStrut(1));
        leftPanel.add(customerLabel);
        leftPanel.add(dateLabel);
        leftPanel.add(discountLabel);

        // Right side - Payment method, total, and cashier
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel paymentTotalPanel = new JPanel();
        paymentTotalPanel.setBackground(Color.WHITE);
        paymentTotalPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        String paymentMethodName = rs.getString("payment_method_name");
        JButton paymentBtn = new JButton(paymentMethodName != null ? paymentMethodName.replace(" Payment", "").toUpperCase() : "PAYMENT");
        paymentBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        paymentBtn.setForeground(Color.WHITE);
        paymentBtn.setFocusPainted(false);
        paymentBtn.setBorderPainted(false);
        paymentBtn.setPreferredSize(new Dimension(85, 32));
        paymentBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updatePaymentMethodColor(paymentBtn, paymentMethodName);

        JLabel totalLabel = new JLabel("Rs." + String.format("%.2f", rs.getDouble("total")));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        totalLabel.setForeground(new Color(8, 147, 176));

        paymentTotalPanel.add(paymentBtn);
        paymentTotalPanel.add(totalLabel);

        JPanel cashierPanel = new JPanel();
        cashierPanel.setBackground(Color.WHITE);
        cashierPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        JLabel cashierText = new JLabel("Cashier");
        cashierText.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        cashierText.setForeground(new Color(102, 102, 102));

        JLabel cashierName = new JLabel(rs.getString("cashier_name"));
        cashierName.setFont(new Font("Nunito ExtraBold", Font.BOLD, 13));
        cashierName.setForeground(new Color(0, 0, 0));

        cashierPanel.add(cashierText);
        cashierPanel.add(cashierName);

        rightPanel.add(paymentTotalPanel);
        rightPanel.add(cashierPanel);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void updatePaymentMethodColor(JButton paymentBtn, String paymentMethodName) {
        Color bgColor;
        Color fgColor = Color.WHITE;

        if (paymentMethodName != null) {
            String method = paymentMethodName.toLowerCase();
            if (method.contains("cash")) {
                bgColor = new Color(40, 167, 69);
            } else if (method.contains("card")) {
                bgColor = new Color(66, 66, 255);
            } else if (method.contains("credit")) {
                bgColor = new Color(255, 193, 7);
                fgColor = Color.BLACK;
            } else {
                bgColor = new Color(108, 117, 125);
            }
        } else {
            bgColor = new Color(108, 117, 125);
        }

        paymentBtn.setBackground(bgColor);
        paymentBtn.setForeground(fgColor);
    }

    private void loadInvoiceProducts(int salesId, boolean hasInvoiceDiscount) {
        try {
            String query = "SELECT si.sale_item_id, si.qty, si.price, si.discount_price, "
                    + "p.product_name, b.brand_name, st.stock_id, "
                    + "si.total as item_total, st.qty as current_stock_qty "
                    + "FROM sale_item si "
                    + "JOIN stock st ON si.stock_id = st.stock_id "
                    + "JOIN product p ON st.product_id = p.product_id "
                    + "JOIN brand b ON p.brand_id = b.brand_id "
                    + "WHERE si.sales_id = " + salesId;

            ResultSet rs = MySQL.executeSearch(query);

            boolean hasProducts = false;
            int productIndex = 0;

            // Create a container panel for better vertical layout
            JPanel productsContainer = new JPanel();
            productsContainer.setBackground(Color.WHITE);
            productsContainer.setLayout(new BoxLayout(productsContainer, BoxLayout.Y_AXIS));

            while (rs.next()) {
                hasProducts = true;
                int saleItemId = rs.getInt("sale_item_id");
                int originalQty = rs.getInt("qty");
                int currentStockQty = rs.getInt("current_stock_qty");
                int stockId = rs.getInt("stock_id");
                double price = rs.getDouble("price");
                double discountPrice = rs.getDouble("discount_price");

                productOriginalQtys.put(productIndex, originalQty);
                productSaleItemIds.put(productIndex, saleItemId);
                productStockIds.put(productIndex, stockId);
                productUnitPrices.put(productIndex, price);
                productDiscountPrices.put(productIndex, discountPrice);
                productSelectedStatus.put(productIndex, false);

                ExchangeProduct productPanel = new ExchangeProduct();
                productPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
                productPanel.setBackground(Color.WHITE);

                productPanel.getProductName().setText(
                        rs.getString("product_name") + " (" + rs.getString("brand_name") + ")"
                );

                double itemTotal = rs.getDouble("item_total");

                productPanel.getQty().setText("Original Quantity = " + originalQty);
                productPanel.getQty().setToolTipText("Available in stock: " + currentStockQty);

                if (hasInvoiceDiscount) {
                    double originalItemTotal = price * originalQty;
                    productPanel.getQtyCount().setText(
                            "Rs " + String.format("%.2f", price) + " X " + originalQty + " = "
                            + String.format("%.2f", originalItemTotal)
                    );

                    if (discountPrice > 0) {
                        productPanel.getDiscountItem().setText(
                                "Item Disc: Rs " + String.format("%.2f", discountPrice)
                                + " | Inv Disc Applied"
                        );
                    } else {
                        productPanel.getDiscountItem().setText("Invoice Discount Applied");
                    }
                } else {
                    double totalBeforeDiscount = price * originalQty;
                    double totalAfterDiscount = itemTotal;
                    double totalItemDiscount = discountPrice * originalQty;

                    productPanel.getQtyCount().setText(
                            "Rs " + String.format("%.2f", price) + " X " + originalQty + " = "
                            + String.format("%.2f", totalBeforeDiscount)
                    );

                    if (totalItemDiscount > 0) {
                        productPanel.getDiscountItem().setText(
                                "Discount Rs " + String.format("%.2f", discountPrice) + " X " + originalQty
                                + " = Rs " + String.format("%.2f", totalAfterDiscount)
                        );
                    } else {
                        productPanel.getDiscountItem().setText("No Item Discount");
                    }
                }

                productPanel.getProductPrice().setText("Rs." + String.format("%.2f", itemTotal));

                int maxExchangeQty = Math.min(originalQty, currentStockQty);

                // Set up text field for quantity input
                productPanel.getExchangeQtyField().setText("0");
                productPanel.getExchangeQtyField().setEnabled(false);

                final int currentIndex = productIndex;
                productPanel.getCheckBox().addActionListener(e -> {
                    boolean isSelected = productPanel.getCheckBox().isSelected();
                    productSelectedStatus.put(currentIndex, isSelected);
                    productPanel.getExchangeQtyField().setEnabled(isSelected);

                    if (isSelected) {
                        int minQty = Math.max(1, originalQty);
                        productPanel.getExchangeQtyField().setText(String.valueOf(minQty));
                    } else {
                        productPanel.getExchangeQtyField().setText("0");
                    }
                });

                productPanel.getExchangeQtyField().addActionListener(e -> {
                    validateExchangeQuantity(productPanel, originalQty, currentStockQty);
                });

                productPanel.getExchangeQtyField().addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        validateExchangeQuantity(productPanel, originalQty, currentStockQty);
                    }
                });

                productsContainer.add(productPanel);

                if (!rs.isLast()) {
                    JSeparator productSeparator = new JSeparator();
                    productSeparator.setForeground(new Color(240, 240, 240));
                    productSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
                    productsContainer.add(productSeparator);
                }

                productIndex++;
            }

            if (!hasProducts) {
                JLabel noProductsLabel = new JLabel("No products found for this invoice", JLabel.CENTER);
                noProductsLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
                noProductsLabel.setForeground(new Color(150, 150, 150));
                noProductsLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
                productsContainer.add(noProductsLabel);
            }

            // Add the container to the scroll panel
            productsPanel.add(productsContainer);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void validateExchangeQuantity(ExchangeProduct productPanel, int originalQty, int currentStockQty) {
        try {
            String qtyText = productPanel.getExchangeQtyField().getText().trim();
            if (qtyText.isEmpty()) {
                productPanel.getExchangeQtyField().setText("0");
                return;
            }

            int exchangeQty = Integer.parseInt(qtyText);

            if (exchangeQty > originalQty) {
                JOptionPane.showMessageDialog(this,
                        "Exchange quantity cannot exceed original purchased quantity (" + originalQty + ")",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                productPanel.getExchangeQtyField().setText(String.valueOf(originalQty));
            } else if (exchangeQty > currentStockQty) {
                JOptionPane.showMessageDialog(this,
                        "Exchange quantity cannot exceed available stock (" + currentStockQty + ")",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                productPanel.getExchangeQtyField().setText(String.valueOf(currentStockQty));
            } else if (productPanel.getCheckBox().isSelected() && exchangeQty < 1) {
                JOptionPane.showMessageDialog(this,
                        "Exchange quantity must be at least 1 for selected products",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                productPanel.getExchangeQtyField().setText("1");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number for exchange quantity",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            productPanel.getExchangeQtyField().setText("0");
        }
    }

    private void clearForm() {
        invoiceCombo.setSelectedIndex(0);
        reasonCombo.setSelectedIndex(0);
        showNoInvoiceSelectedMessage();
        invoiceTotalDiscount = 0.0;
        invoiceDiscountType = null;
        productOriginalQtys.clear();
        productSaleItemIds.clear();
        productStockIds.clear();
        productUnitPrices.clear();
        productDiscountPrices.clear();
        productSelectedStatus.clear();
        currentSalesId = -1;
        currentCreditCustomerId = -1;
        currentCreditAmount = 0.0;
        isCreditPayment = false;
    }

    private double calculateRefundAmount() {
        double refundAmount = 0.0;
        int productIndex = 0;

        for (Component comp : productsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel containerPanel = (JPanel) comp;
                // Check if this container panel has ExchangeProduct components
                for (Component innerComp : containerPanel.getComponents()) {
                    if (innerComp instanceof ExchangeProduct) {
                        ExchangeProduct productPanel = (ExchangeProduct) innerComp;

                        // Check if checkbox is selected
                        boolean isSelected = productPanel.getCheckBox().isSelected();

                        if (isSelected) {
                            try {
                                String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                                if (exchangeQty > 0) {
                                    String priceText = productPanel.getProductPrice().getText();
                                    double itemPrice = 0.0;
                                    try {
                                        itemPrice = Double.parseDouble(priceText.replace("Rs.", "").trim());
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }

                                    int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                                    double proportionalAmount = (itemPrice / originalQty) * exchangeQty;
                                    refundAmount += proportionalAmount;
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        productIndex++;
                    }
                }
            } else if (comp instanceof ExchangeProduct) {
                ExchangeProduct productPanel = (ExchangeProduct) comp;

                // Check if checkbox is selected
                boolean isSelected = productPanel.getCheckBox().isSelected();

                if (isSelected) {
                    try {
                        String qtyText = productPanel.getExchangeQtyField().getText().trim();
                        int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                        if (exchangeQty > 0) {
                            String priceText = productPanel.getProductPrice().getText();
                            double itemPrice = 0.0;
                            try {
                                itemPrice = Double.parseDouble(priceText.replace("Rs.", "").trim());
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                            double proportionalAmount = (itemPrice / originalQty) * exchangeQty;
                            refundAmount += proportionalAmount;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                productIndex++;
            }
        }

        return refundAmount;
    }

    private boolean isStockLossReason(String reason) {
        return reason.toLowerCase().contains("expired")
                || reason.toLowerCase().contains("damaged")
                || reason.toLowerCase().contains("theft")
                || reason.toLowerCase().contains("misplaced");
    }

    private void processExchange() {
        Connection conn = null;
        try {
            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

            // Get selected reason
            int reasonId = -1;
            String selectedReason = "";
            for (Map.Entry<Integer, String> entry : reasonMap.entrySet()) {
                if (entry.getValue().equals(reasonCombo.getSelectedItem())) {
                    reasonId = entry.getKey();
                    selectedReason = entry.getValue();
                    break;
                }
            }

            if (reasonId == -1) {
                JOptionPane.showMessageDialog(this, "Invalid return reason selected",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if any products are selected with quantity > 0
            boolean hasSelectedProducts = false;
            int productIndex = 0;

            for (Component comp : productsPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel containerPanel = (JPanel) comp;
                    for (Component innerComp : containerPanel.getComponents()) {
                        if (innerComp instanceof ExchangeProduct) {
                            ExchangeProduct productPanel = (ExchangeProduct) innerComp;
                            if (productPanel.getCheckBox().isSelected()) {
                                try {
                                    String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                    int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
                                    if (exchangeQty > 0) {
                                        hasSelectedProducts = true;
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    // Ignore parse errors
                                }
                            }
                            productIndex++;
                        }
                    }
                } else if (comp instanceof ExchangeProduct) {
                    ExchangeProduct productPanel = (ExchangeProduct) comp;
                    if (productPanel.getCheckBox().isSelected()) {
                        try {
                            String qtyText = productPanel.getExchangeQtyField().getText().trim();
                            int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
                            if (exchangeQty > 0) {
                                hasSelectedProducts = true;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Ignore parse errors
                        }
                    }
                    productIndex++;
                }

                if (hasSelectedProducts) {
                    break;
                }
            }

            if (!hasSelectedProducts) {
                JOptionPane.showMessageDialog(this, "Please select at least one product to return with quantity greater than 0",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Calculate total refund amount and discount
            double totalRefundAmount = calculateRefundAmount();
            double totalDiscountPrice = calculateTotalDiscountPrice();

            // Insert into return table
            String returnQuery = "INSERT INTO `return` (return_date, total_return_amount, return_reason_id, "
                    + "status_id, sales_id, user_id, total_discount_price) "
                    + "VALUES (NOW(), ?, ?, 1, ?, ?, ?)";

            PreparedStatement returnStmt = conn.prepareStatement(returnQuery, Statement.RETURN_GENERATED_KEYS);
            returnStmt.setDouble(1, totalRefundAmount);
            returnStmt.setInt(2, reasonId);
            returnStmt.setInt(3, currentSalesId);
            returnStmt.setInt(4, currentUserId);
            returnStmt.setDouble(5, totalDiscountPrice);

            int affectedRows = returnStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating return failed, no rows affected.");
            }

            int returnId;
            try (ResultSet generatedKeys = returnStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    returnId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating return failed, no ID obtained.");
                }
            }

            // Process return items
            boolean isStockLoss = isStockLossReason(selectedReason);
            productIndex = 0;

            for (Component comp : productsPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel containerPanel = (JPanel) comp;
                    for (Component innerComp : containerPanel.getComponents()) {
                        if (innerComp instanceof ExchangeProduct) {
                            ExchangeProduct productPanel = (ExchangeProduct) innerComp;

                            if (productPanel.getCheckBox().isSelected()) {
                                try {
                                    String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                    int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                                    if (exchangeQty > 0) {
                                        int saleItemId = productSaleItemIds.get(productIndex);
                                        int stockId = productStockIds.get(productIndex);
                                        int originalQty = productOriginalQtys.get(productIndex);
                                        double unitPrice = productUnitPrices.get(productIndex);
                                        double discountPrice = productDiscountPrices.get(productIndex);

                                        // Calculate individual item amounts
                                        double itemTotalBeforeDiscount = unitPrice * exchangeQty;
                                        double itemDiscountAmount = (discountPrice / originalQty) * exchangeQty;
                                        double itemTotalReturnAmount = itemTotalBeforeDiscount - itemDiscountAmount;

                                        // Insert return item
                                        String returnItemQuery = "INSERT INTO return_item (return_qty, unit_return_price, "
                                                + "discount_price, total_return_amount, stock_id, sale_item_id, return_id) "
                                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                                        PreparedStatement returnItemStmt = conn.prepareStatement(returnItemQuery);
                                        returnItemStmt.setString(1, String.valueOf(exchangeQty));
                                        returnItemStmt.setDouble(2, unitPrice);
                                        returnItemStmt.setDouble(3, itemDiscountAmount);
                                        returnItemStmt.setDouble(4, itemTotalReturnAmount);
                                        returnItemStmt.setInt(5, stockId);
                                        returnItemStmt.setInt(6, saleItemId);
                                        returnItemStmt.setInt(7, returnId);

                                        returnItemStmt.executeUpdate();

                                        // Handle stock based on return reason
                                        if (isStockLoss) {
                                            // Add to stock_loss table
                                            String stockLossQuery = "INSERT INTO stock_loss (qty, stock_id, stock_loss_date, "
                                                    + "return_reason_id, user_id, sales_id) "
                                                    + "VALUES (?, ?, NOW(), ?, ?, ?)";

                                            PreparedStatement stockLossStmt = conn.prepareStatement(stockLossQuery);
                                            stockLossStmt.setInt(1, exchangeQty);
                                            stockLossStmt.setInt(2, stockId);
                                            stockLossStmt.setInt(3, reasonId);
                                            stockLossStmt.setInt(4, currentUserId);
                                            stockLossStmt.setInt(5, currentSalesId);

                                            stockLossStmt.executeUpdate();
                                        } else {
                                            // Update stock quantity (add back to inventory)
                                            String updateStockQuery = "UPDATE stock SET qty = qty + ? WHERE stock_id = ?";
                                            PreparedStatement updateStockStmt = conn.prepareStatement(updateStockQuery);
                                            updateStockStmt.setInt(1, exchangeQty);
                                            updateStockStmt.setInt(2, stockId);
                                            updateStockStmt.executeUpdate();
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                            productIndex++;
                        }
                    }
                } else if (comp instanceof ExchangeProduct) {
                    ExchangeProduct productPanel = (ExchangeProduct) comp;

                    if (productPanel.getCheckBox().isSelected()) {
                        try {
                            String qtyText = productPanel.getExchangeQtyField().getText().trim();
                            int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                            if (exchangeQty > 0) {
                                int saleItemId = productSaleItemIds.get(productIndex);
                                int stockId = productStockIds.get(productIndex);
                                int originalQty = productOriginalQtys.get(productIndex);
                                double unitPrice = productUnitPrices.get(productIndex);
                                double discountPrice = productDiscountPrices.get(productIndex);

                                // Calculate individual item amounts
                                double itemTotalBeforeDiscount = unitPrice * exchangeQty;
                                double itemDiscountAmount = (discountPrice / originalQty) * exchangeQty;
                                double itemTotalReturnAmount = itemTotalBeforeDiscount - itemDiscountAmount;

                                // Insert return item
                                String returnItemQuery = "INSERT INTO return_item (return_qty, unit_return_price, "
                                        + "discount_price, total_return_amount, stock_id, sale_item_id, return_id) "
                                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                                PreparedStatement returnItemStmt = conn.prepareStatement(returnItemQuery);
                                returnItemStmt.setString(1, String.valueOf(exchangeQty));
                                returnItemStmt.setDouble(2, unitPrice);
                                returnItemStmt.setDouble(3, itemDiscountAmount);
                                returnItemStmt.setDouble(4, itemTotalReturnAmount);
                                returnItemStmt.setInt(5, stockId);
                                returnItemStmt.setInt(6, saleItemId);
                                returnItemStmt.setInt(7, returnId);

                                returnItemStmt.executeUpdate();

                                // Handle stock based on return reason
                                if (isStockLoss) {
                                    // Add to stock_loss table
                                    String stockLossQuery = "INSERT INTO stock_loss (qty, stock_id, stock_loss_date, "
                                            + "return_reason_id, user_id, sales_id) "
                                            + "VALUES (?, ?, NOW(), ?, ?, ?)";

                                    PreparedStatement stockLossStmt = conn.prepareStatement(stockLossQuery);
                                    stockLossStmt.setInt(1, exchangeQty);
                                    stockLossStmt.setInt(2, stockId);
                                    stockLossStmt.setInt(3, reasonId);
                                    stockLossStmt.setInt(4, currentUserId);
                                    stockLossStmt.setInt(5, currentSalesId);

                                    stockLossStmt.executeUpdate();
                                } else {
                                    // Update stock quantity (add back to inventory)
                                    String updateStockQuery = "UPDATE stock SET qty = qty + ? WHERE stock_id = ?";
                                    PreparedStatement updateStockStmt = conn.prepareStatement(updateStockQuery);
                                    updateStockStmt.setInt(1, exchangeQty);
                                    updateStockStmt.setInt(2, stockId);
                                    updateStockStmt.executeUpdate();
                                }
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    productIndex++;
                }
            }

            // Handle credit payment refund
            if (isCreditPayment && currentCreditCustomerId != -1) {
                handleCreditRefund(conn, totalRefundAmount);
            }

            // Commit transaction
            conn.commit();

            String message = buildSuccessMessage(totalRefundAmount, selectedReason, isStockLoss);
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this, "Error processing exchange: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this, "Error parsing price data",
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleCreditRefund(Connection conn, double refundAmount) throws SQLException {
        // Get total payments made by customer
        double totalPaid = getTotalCreditPayments();

        if (totalPaid >= refundAmount) {
            // Customer has paid more than refund amount - give cash refund
            // Update credit table to decrease the amount
            String updateCreditQuery = "UPDATE credit SET credit_amout = credit_amout - ? "
                    + "WHERE credit_customer_id = ? ORDER BY credit_id DESC LIMIT 1";
            PreparedStatement updateStmt = conn.prepareStatement(updateCreditQuery);
            updateStmt.setDouble(1, refundAmount);
            updateStmt.setInt(2, currentCreditCustomerId);
            updateStmt.executeUpdate();
        } else {
            // Customer hasn't paid enough - just decrease credit amount
            String updateCreditQuery = "UPDATE credit SET credit_amout = credit_amout - ? "
                    + "WHERE credit_customer_id = ? ORDER BY credit_id DESC LIMIT 1";
            PreparedStatement updateStmt = conn.prepareStatement(updateCreditQuery);
            updateStmt.setDouble(1, refundAmount);
            updateStmt.setInt(2, currentCreditCustomerId);
            updateStmt.executeUpdate();
        }
    }

    private String buildSuccessMessage(double refundAmount, String selectedReason, boolean isStockLoss) {
        StringBuilder message = new StringBuilder();
        message.append("Exchange processed successfully!\n");
        message.append("Refund Amount: Rs.").append(String.format("%.2f", refundAmount)).append("\n");

        if (isCreditPayment) {
            double totalPaid = getTotalCreditPayments();
            if (totalPaid >= refundAmount) {
                message.append("Payment Method: CREDIT (Customer has paid)\n");
                message.append("Return Amount to Customer: Rs.").append(String.format("%.2f", refundAmount)).append("\n");
                message.append("Credit balance decreased by: Rs.").append(String.format("%.2f", refundAmount));
            } else {
                message.append("Payment Method: CREDIT (Customer hasn't paid fully)\n");
                message.append("Credit balance decreased by: Rs.").append(String.format("%.2f", refundAmount)).append("\n");
                message.append("No cash refund - amount deducted from credit balance");
            }
        } else {
            message.append("Payment Method: CASH/CARD\n");
            message.append("Return Amount to Customer: Rs.").append(String.format("%.2f", refundAmount));
        }

        message.append("\n\nStock Handling: ");
        message.append(isStockLoss ? "Items marked as stock loss (not returned to inventory)"
                : "Items returned to inventory");

        return message.toString();
    }

    private double calculateTotalDiscountPrice() {
        double totalDiscount = 0.0;
        int productIndex = 0;

        for (Component comp : productsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel containerPanel = (JPanel) comp;
                for (Component innerComp : containerPanel.getComponents()) {
                    if (innerComp instanceof ExchangeProduct) {
                        ExchangeProduct productPanel = (ExchangeProduct) innerComp;

                        if (productPanel.getCheckBox().isSelected()) {
                            try {
                                String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                                if (exchangeQty > 0) {
                                    int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                                    double discountPrice = productDiscountPrices.getOrDefault(productIndex, 0.0);

                                    double proportionalDiscount = (discountPrice / originalQty) * exchangeQty;
                                    totalDiscount += proportionalDiscount;
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        productIndex++;
                    }
                }
            } else if (comp instanceof ExchangeProduct) {
                ExchangeProduct productPanel = (ExchangeProduct) comp;

                if (productPanel.getCheckBox().isSelected()) {
                    try {
                        String qtyText = productPanel.getExchangeQtyField().getText().trim();
                        int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

                        if (exchangeQty > 0) {
                            int originalQty = productOriginalQtys.getOrDefault(productIndex, 1);
                            double discountPrice = productDiscountPrices.getOrDefault(productIndex, 0.0);

                            double proportionalDiscount = (discountPrice / originalQty) * exchangeQty;
                            totalDiscount += proportionalDiscount;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                productIndex++;
            }
        }

        return totalDiscount;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        saveBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        invoiceCombo = new javax.swing.JComboBox<>();
        productsScrollPanel = new javax.swing.JScrollPane();
        productsPanel = new javax.swing.JPanel();
        invoiceNumberLabel = new javax.swing.JLabel();
        totalAmountLabel = new javax.swing.JLabel();
        customerNameLabel = new javax.swing.JLabel();
        paymentMethod = new javax.swing.JButton();
        dateLabel = new javax.swing.JLabel();
        brandName2 = new javax.swing.JLabel();
        cashierLabel = new javax.swing.JLabel();
        discountLabel = new javax.swing.JLabel();
        reasonCombo = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Exchange Products");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        saveBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        saveBtn.setForeground(new java.awt.Color(8, 147, 176));
        saveBtn.setText("Exchange");
        saveBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });
        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                saveBtnKeyPressed(evt);
            }
        });

        clearFormBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        clearFormBtn.setForeground(new java.awt.Color(8, 147, 176));
        clearFormBtn.setText("Clear Form");
        clearFormBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        clearFormBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFormBtnActionPerformed(evt);
            }
        });
        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                clearFormBtnKeyPressed(evt);
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
        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cancelBtnKeyPressed(evt);
            }
        });

        invoiceCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        invoiceCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        invoiceCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Invoice No *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        invoiceCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invoiceComboActionPerformed(evt);
            }
        });
        invoiceCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                invoiceComboKeyPressed(evt);
            }
        });

        productsScrollPanel.setBackground(new java.awt.Color(255, 255, 255));
        productsScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        productsScrollPanel.setOpaque(false);
        productsScrollPanel.setPreferredSize(new java.awt.Dimension(563, 370));

        productsPanel.setBackground(new java.awt.Color(255, 255, 255));

        invoiceNumberLabel.setFont(new java.awt.Font("Nunito SemiBold", 3, 14)); // NOI18N
        invoiceNumberLabel.setText("#inv3243345");

        totalAmountLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        totalAmountLabel.setForeground(new java.awt.Color(8, 147, 176));
        totalAmountLabel.setText("Rs.570.00");

        customerNameLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        customerNameLabel.setForeground(new java.awt.Color(102, 102, 102));
        customerNameLabel.setText("Customer Name");

        paymentMethod.setBackground(new java.awt.Color(51, 0, 255));
        paymentMethod.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        paymentMethod.setForeground(new java.awt.Color(255, 255, 255));
        paymentMethod.setText("CASE");
        paymentMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentMethodActionPerformed(evt);
            }
        });

        dateLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        dateLabel.setForeground(new java.awt.Color(102, 102, 102));
        dateLabel.setText("10/10/2025, 15.25.20");

        brandName2.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        brandName2.setForeground(new java.awt.Color(102, 102, 102));
        brandName2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        brandName2.setText("Cashier");

        cashierLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        cashierLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cashierLabel.setText("Hirun");

        discountLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        discountLabel.setForeground(new java.awt.Color(255, 0, 0));
        discountLabel.setText("Discount 10% =  RS 70.00");

        javax.swing.GroupLayout productsPanelLayout = new javax.swing.GroupLayout(productsPanel);
        productsPanel.setLayout(productsPanelLayout);
        productsPanelLayout.setHorizontalGroup(
            productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, productsPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(discountLabel)
                    .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(productsPanelLayout.createSequentialGroup()
                            .addComponent(invoiceNumberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(57, 57, 57))
                        .addGroup(productsPanelLayout.createSequentialGroup()
                            .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(productsPanelLayout.createSequentialGroup()
                                    .addComponent(customerNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(8, 8, 8))
                                .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(24, 24, 24))))
                .addGap(211, 211, 211)
                .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productsPanelLayout.createSequentialGroup()
                        .addComponent(paymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalAmountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(productsPanelLayout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(brandName2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cashierLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(560, 560, 560))
        );
        productsPanelLayout.setVerticalGroup(
            productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productsPanelLayout.createSequentialGroup()
                        .addComponent(invoiceNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(customerNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(productsPanelLayout.createSequentialGroup()
                        .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(paymentMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalAmountLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cashierLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(brandName2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(714, 714, 714))
        );

        productsScrollPanel.setViewportView(productsPanel);

        reasonCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        reasonCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        reasonCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Exchange Reason *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        reasonCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reasonComboActionPerformed(evt);
            }
        });
        reasonCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                reasonComboKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(invoiceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reasonCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(productsScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addGap(7, 7, 7)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invoiceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reasonCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
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

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed

        if (invoiceCombo.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (reasonCombo.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a return reason",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if any products are selected with quantity > 0
        boolean hasValidExchange = false;
        int productIndex = 0;

        for (Component comp : productsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel containerPanel = (JPanel) comp;
                for (Component innerComp : containerPanel.getComponents()) {
                    if (innerComp instanceof ExchangeProduct) {
                        ExchangeProduct productPanel = (ExchangeProduct) innerComp;
                        if (productPanel.getCheckBox().isSelected()) {
                            try {
                                String qtyText = productPanel.getExchangeQtyField().getText().trim();
                                int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
                                if (exchangeQty > 0) {
                                    hasValidExchange = true;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // Ignore parse errors here, they'll be caught in validation
                            }
                        }
                        productIndex++;
                    }
                }
            } else if (comp instanceof ExchangeProduct) {
                ExchangeProduct productPanel = (ExchangeProduct) comp;
                if (productPanel.getCheckBox().isSelected()) {
                    try {
                        String qtyText = productPanel.getExchangeQtyField().getText().trim();
                        int exchangeQty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
                        if (exchangeQty > 0) {
                            hasValidExchange = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore parse errors here, they'll be caught in validation
                    }
                }
                productIndex++;
            }

            if (hasValidExchange) {
                break;
            }
        }

        if (!hasValidExchange) {
            JOptionPane.showMessageDialog(this, "Please select products to exchange and enter quantities greater than 0",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double refundAmount = calculateRefundAmount();

        // Show confirmation with refund amount and stock handling info
        String selectedReason = (String) reasonCombo.getSelectedItem();
        boolean isStockLoss = isStockLossReason(selectedReason);

        String confirmationMessage = "Total Refund Amount: Rs." + String.format("%.2f", refundAmount)
                + "\nReturn Reason: " + selectedReason
                + "\n\nStock Handling: "
                + (isStockLoss ? "Items will be marked as STOCK LOSS (not returned to inventory)"
                        : "Items will be RETURNED TO INVENTORY")
                + "\n\nDo you want to proceed with the exchange?";

        int confirm = JOptionPane.showConfirmDialog(this, confirmationMessage,
                "Confirm Exchange", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            processExchange();
        }

    }//GEN-LAST:event_saveBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed

    }//GEN-LAST:event_saveBtnKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed

    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed

    }//GEN-LAST:event_cancelBtnKeyPressed

    private void invoiceComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_invoiceComboKeyPressed

    }//GEN-LAST:event_invoiceComboKeyPressed

    private void invoiceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceComboActionPerformed
        if (invoiceCombo.getSelectedIndex() > 0) {
            String selectedInvoice = (String) invoiceCombo.getSelectedItem();
            loadInvoiceDetails(selectedInvoice);
        }
    }//GEN-LAST:event_invoiceComboActionPerformed

    private void reasonComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reasonComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reasonComboActionPerformed

    private void reasonComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reasonComboKeyPressed

    }//GEN-LAST:event_reasonComboKeyPressed

    private void paymentMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentMethodActionPerformed

    }//GEN-LAST:event_paymentMethodActionPerformed

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
            java.util.logging.Logger.getLogger(ExchangeProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ExchangeProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ExchangeProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ExchangeProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ExchangeProductDialog dialog = new ExchangeProductDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JLabel brandName2;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel cashierLabel;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JLabel customerNameLabel;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JLabel discountLabel;
    private javax.swing.JComboBox<String> invoiceCombo;
    private javax.swing.JLabel invoiceNumberLabel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton paymentMethod;
    private javax.swing.JPanel productsPanel;
    private javax.swing.JScrollPane productsScrollPanel;
    private javax.swing.JComboBox<String> reasonCombo;
    private javax.swing.JButton saveBtn;
    private javax.swing.JLabel totalAmountLabel;
    // End of variables declaration//GEN-END:variables
}
