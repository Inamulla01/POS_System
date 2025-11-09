/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import lk.com.pos.dialogpanel.ExchangeProduct;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class ExchangeProductDialog extends javax.swing.JDialog {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");
    private List<ExchangeProduct> productPanels = new ArrayList<>();
    private int selectedInvoiceId = -1;
    private double totalReturnAmount = 0.0;
    private double totalDiscountAmount = 0.0;

    // Maps to store IDs for combo box items
    private Map<String, Integer> invoiceIdMap = new HashMap<>();
    private Map<String, Integer> reasonIdMap = new HashMap<>();

    /**
     * Creates new form ExchangeProductDialog
     */
    public ExchangeProductDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setTitle("Product Return/Exchange");

        // Clear the static panel first
        clearStaticPanel();
        loadInvoiceNumbers();
        loadReturnReasons();
        setupKeyboardShortcuts();

        // Set the scroll pane to use a proper layout
        productsScrollPanel.setLayout(new BoxLayout(productsScrollPanel, BoxLayout.Y_AXIS));
        productsScrollPanel.setBackground(Color.WHITE);
    }

    private void clearStaticPanel() {
        invoiceNumberLabel.setText("");
        customerNameLabel.setText("");
        dateLabel.setText("");
        cashierLabel.setText("");
        totalAmountLabel.setText("");
        discountLabel.setText("");
    }

    private void loadInvoiceNumbers() {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT DISTINCT s.sales_id, s.invoice_no, s.datetime, s.total, "
                    + "cc.customer_name, u.name as cashier_name "
                    + "FROM sales s "
                    + "LEFT JOIN credit_customer cc ON s.credit_customer_id = cc.customer_id "
                    + "JOIN user u ON s.user_id = u.user_id "
                    + "ORDER BY s.datetime DESC LIMIT 50";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            invoiceCombo.removeAllItems();
            invoiceCombo.addItem("Select Invoice");
            invoiceIdMap.clear();

            while (rs.next()) {
                int salesId = rs.getInt("sales_id");
                String invoiceNo = rs.getString("invoice_no");
                java.util.Date date = rs.getTimestamp("datetime");
                double total = rs.getDouble("total");
                String customerName = rs.getString("customer_name");
                String cashier = rs.getString("cashier_name");

                String displayText = String.format("%s - %s - Rs.%.2f",
                        invoiceNo, dateFormat.format(date), total);

                // Add to combo box
                invoiceCombo.addItem(displayText);
                // Store the sales_id in our map
                invoiceIdMap.put(displayText, salesId);
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_RIGHT,
                    "Error loading invoices: " + e.getMessage());
        }
    }

    private void loadReturnReasons() {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT return_reason_id, reason FROM return_reason";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            reasonCombo.removeAllItems();
            reasonCombo.addItem("Select Return Reason");
            reasonIdMap.clear();

            while (rs.next()) {
                int reasonId = rs.getInt("return_reason_id");
                String reason = rs.getString("reason");

                // Add to combo box
                reasonCombo.addItem(reason);
                // Store the ID in our map
                reasonIdMap.put(reason, reasonId);
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_RIGHT,
                    "Error loading return reasons: " + e.getMessage());
        }
    }

    private void loadInvoiceDetails(int salesId) {
        clearProductPanels();
        clearStaticPanel();

        try {
            Connection conn = MySQL.getConnection();

            // First get invoice header information
            String headerSql = "SELECT s.invoice_no, s.datetime, s.total, "
                    + "cc.customer_name, u.name as cashier_name, "
                    + "d.discount "
                    + "FROM sales s "
                    + "LEFT JOIN credit_customer cc ON s.credit_customer_id = cc.customer_id "
                    + "JOIN user u ON s.user_id = u.user_id "
                    + "LEFT JOIN discount d ON s.discount_id = d.discount_id "
                    + "WHERE s.sales_id = ?";

            PreparedStatement headerPst = conn.prepareStatement(headerSql);
            headerPst.setInt(1, salesId);
            ResultSet headerRs = headerPst.executeQuery();

            if (headerRs.next()) {
                // Update the static panel with invoice details
                invoiceNumberLabel.setText("#" + headerRs.getString("invoice_no"));
                customerNameLabel.setText(headerRs.getString("customer_name") != null
                        ? headerRs.getString("customer_name") : "Walk-in Customer");
                dateLabel.setText(dateFormat.format(headerRs.getTimestamp("datetime")));
                cashierLabel.setText(headerRs.getString("cashier_name"));
                totalAmountLabel.setText(String.format("Rs %.2f", headerRs.getDouble("total")));

                // Set discount if available
                double discount = headerRs.getDouble("discount");
                if (discount > 0) {
                    discountLabel.setText(String.format("Discount: Rs %.2f", discount));
                } else {
                    discountLabel.setText("No Discount");
                }
            }
            headerRs.close();
            headerPst.close();

            // Now get product details
            String productSql = "SELECT si.sale_item_id, si.qty, si.price, si.discount_price, si.total, "
                    + "p.product_name, b.brand_name, st.stock_id, st.batch_no "
                    + "FROM sale_item si "
                    + "JOIN stock st ON si.stock_id = st.stock_id "
                    + "JOIN product p ON st.product_id = p.product_id "
                    + "JOIN brand b ON p.brand_id = b.brand_id "
                    + "WHERE si.sales_id = ?";

            PreparedStatement productPst = conn.prepareStatement(productSql);
            productPst.setInt(1, salesId);
            ResultSet productRs = productPst.executeQuery();

            // Clear existing products
            productsScrollPanel.removeAll();

            while (productRs.next()) {
                int saleItemId = productRs.getInt("sale_item_id");
                int quantity = productRs.getInt("qty");
                double price = productRs.getDouble("price");
                double discountPrice = productRs.getDouble("discount_price");
                double total = productRs.getDouble("total");
                String productName = productRs.getString("product_name");
                String brandName = productRs.getString("brand_name");
                int stockId = productRs.getInt("stock_id");
                String batchNo = productRs.getString("batch_no");

                // Create product panel
                ExchangeProduct productPanel = new ExchangeProduct();
                productPanel.setProductDetails(
                        productName + " (" + brandName + ")",
                        price,
                        discountPrice,
                        quantity,
                        total,
                        batchNo
                );
                productPanel.setIds(saleItemId, stockId);

                // Add to scroll panel
                productsScrollPanel.add(productPanel);
                productPanels.add(productPanel);

                // Add some spacing between product panels
                productsScrollPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            // Refresh the scroll panel
            productsScrollPanel.revalidate();
            productsScrollPanel.repaint();

            productRs.close();
            productPst.close();

            // Show success message
            Notifications.getInstance().show(Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_RIGHT,
                    "Invoice details loaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_RIGHT,
                    "Error loading invoice details: " + e.getMessage());
        }
    }

    private void clearProductPanels() {
        productPanels.clear();
        productsScrollPanel.removeAll();
        productsScrollPanel.revalidate();
        productsScrollPanel.repaint();
    }

    private void setupKeyboardShortcuts() {
        // ESC to close
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // F5 to refresh
        getRootPane().registerKeyboardAction(
                e -> refreshData(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void refreshData() {
        loadInvoiceNumbers();
        loadReturnReasons();
        Notifications.getInstance().show(Notifications.Type.SUCCESS,
                Notifications.Location.TOP_RIGHT, "Data refreshed successfully");
    }

    private boolean validateForm() {
        if (selectedInvoiceId == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING,
                    Notifications.Location.TOP_RIGHT, "Please select an invoice");
            invoiceCombo.requestFocus();
            return false;
        }

        if (reasonCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING,
                    Notifications.Location.TOP_RIGHT, "Please select a return reason");
            reasonCombo.requestFocus();
            return false;
        }

        // Check if at least one product has return quantity > 0
        boolean hasReturns = false;
        for (ExchangeProduct panel : productPanels) {
            if (panel.getReturnQuantity() > 0) {
                hasReturns = true;
                break;
            }
        }

        if (!hasReturns) {
            Notifications.getInstance().show(Notifications.Type.WARNING,
                    Notifications.Location.TOP_RIGHT, "Please select at least one product to return");
            return false;
        }

        return true;
    }

    private void processReturn() {
        if (!validateForm()) {
            return;
        }

        Connection conn = null;
        PreparedStatement returnPst = null;
        ResultSet generatedKeys = null;

        try {
            conn = MySQL.getConnection();
            conn.setAutoCommit(false);

            // Get return reason ID from our map
            String selectedReason = (String) reasonCombo.getSelectedItem();
            Integer returnReasonId = reasonIdMap.get(selectedReason);

            if (returnReasonId == null) {
                // Default to 1 if not found
                returnReasonId = 1;
            }

            // Get current user (you might want to pass this from the main application)
            int currentUserId = 1; // Default admin user

            // Calculate totals
            totalReturnAmount = 0.0;
            totalDiscountAmount = 0.0;
            for (ExchangeProduct panel : productPanels) {
                if (panel.getReturnQuantity() > 0) {
                    totalReturnAmount += panel.getReturnTotal();
                    totalDiscountAmount += panel.getDiscountAmount();
                }
            }

            // Insert into return table
            String returnSql = "INSERT INTO return (return_date, total_return_amount, return_reason_id, "
                    + "status_id, sales_id, user_id, total_discount_price) "
                    + "VALUES (NOW(), ?, ?, 1, ?, ?, ?)";

            returnPst = conn.prepareStatement(returnSql, PreparedStatement.RETURN_GENERATED_KEYS);
            returnPst.setDouble(1, totalReturnAmount);
            returnPst.setInt(2, returnReasonId);
            returnPst.setInt(3, selectedInvoiceId);
            returnPst.setInt(4, currentUserId);
            returnPst.setDouble(5, totalDiscountAmount);

            returnPst.executeUpdate();

            generatedKeys = returnPst.getGeneratedKeys();
            int returnId = -1;
            if (generatedKeys.next()) {
                returnId = generatedKeys.getInt(1);
            }

            // Insert return items and update stock
            for (ExchangeProduct panel : productPanels) {
                int returnQty = panel.getReturnQuantity();
                if (returnQty > 0) {
                    // Update stock quantity
                    String updateStockSql = "UPDATE stock SET qty = qty + ? WHERE stock_id = ?";
                    PreparedStatement stockPst = conn.prepareStatement(updateStockSql);
                    stockPst.setInt(1, returnQty);
                    stockPst.setInt(2, panel.getStockId());
                    stockPst.executeUpdate();
                    stockPst.close();

                    // Insert return item
                    String returnItemSql = "INSERT INTO return_item (return_qty, unit_return_price, "
                            + "discount_price, total_return_amount, stock_id, sale_item_id, return_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement returnItemPst = conn.prepareStatement(returnItemSql);
                    returnItemPst.setInt(1, returnQty);
                    returnItemPst.setDouble(2, panel.getUnitPrice());
                    returnItemPst.setDouble(3, panel.getDiscountAmount());
                    returnItemPst.setDouble(4, panel.getReturnTotal());
                    returnItemPst.setInt(5, panel.getStockId());
                    returnItemPst.setInt(6, panel.getSaleItemId());
                    returnItemPst.setInt(7, returnId);

                    returnItemPst.executeUpdate();
                    returnItemPst.close();
                }
            }

            conn.commit();

            Notifications.getInstance().show(Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_RIGHT, "Return processed successfully! Return ID: " + returnId);

            dispose();

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_RIGHT, "Error processing return: " + e.getMessage());
        } finally {
            try {
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
                if (returnPst != null) {
                    returnPst.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Check if invoice already has returns to prevent duplicate returns
    private boolean checkInvoiceHasReturns(int salesId) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT COUNT(*) as return_count FROM return WHERE sales_id = ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, salesId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int returnCount = rs.getInt("return_count");
                rs.close();
                pst.close();
                return returnCount > 0;
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
        jPanel2 = new javax.swing.JPanel();
        invoiceNumberLabel = new javax.swing.JLabel();
        totalAmountLabel = new javax.swing.JLabel();
        customerNameLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        dateLabel = new javax.swing.JLabel();
        brandName2 = new javax.swing.JLabel();
        cashierLabel = new javax.swing.JLabel();
        discountLabel = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        reasonCombo = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Exchange Products");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        saveBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        saveBtn.setForeground(new java.awt.Color(8, 147, 176));
        saveBtn.setText("Save");
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

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        invoiceNumberLabel.setFont(new java.awt.Font("Nunito SemiBold", 3, 14)); // NOI18N
        invoiceNumberLabel.setText("#inv3243345");

        totalAmountLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        totalAmountLabel.setForeground(new java.awt.Color(8, 147, 176));
        totalAmountLabel.setText("Rs.570.00");

        customerNameLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        customerNameLabel.setForeground(new java.awt.Color(102, 102, 102));
        customerNameLabel.setText("Customer Name");

        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("CASE");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
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

        jCheckBox1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(discountLabel)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(invoiceNumberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(57, 57, 57))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(customerNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(8, 8, 8))
                                .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(24, 24, 24))))
                .addGap(211, 211, 211)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(151, 151, 151)
                        .addComponent(jCheckBox1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalAmountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(brandName2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cashierLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(560, 560, 560))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(invoiceNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(customerNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addGap(2, 2, 2)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalAmountLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(brandName2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cashierLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(275, Short.MAX_VALUE))
        );

        productsScrollPanel.setViewportView(jPanel2);

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
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(invoiceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(productsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                    .addComponent(reasonCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(productsScrollPanel)
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
        processReturn();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed

    }//GEN-LAST:event_saveBtnKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearProductPanels();
        clearStaticPanel();
        invoiceCombo.setSelectedIndex(0);
        reasonCombo.setSelectedIndex(0);
        selectedInvoiceId = -1;
        invoiceIdMap.clear();
        reasonIdMap.clear();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed

    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed

    }//GEN-LAST:event_cancelBtnKeyPressed

    private void invoiceComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_invoiceComboKeyPressed

    }//GEN-LAST:event_invoiceComboKeyPressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void invoiceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceComboActionPerformed
        if (invoiceCombo.getSelectedIndex() > 0) {
            String selectedText = (String) invoiceCombo.getSelectedItem();
            Integer salesId = invoiceIdMap.get(selectedText);

            if (salesId != null) {
                selectedInvoiceId = salesId;

                // Check if this invoice already has returns
                if (checkInvoiceHasReturns(selectedInvoiceId)) {
                    int option = JOptionPane.showConfirmDialog(this,
                            "This invoice already has return records. Do you want to process another return?",
                            "Invoice Has Existing Returns",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (option != JOptionPane.YES_OPTION) {
                        invoiceCombo.setSelectedIndex(0);
                        selectedInvoiceId = -1;
                        return;
                    }
                }

                loadInvoiceDetails(selectedInvoiceId);
            }
        } else {
            selectedInvoiceId = -1;
            clearProductPanels();
            clearStaticPanel();
        }
    }//GEN-LAST:event_invoiceComboActionPerformed

    private void reasonComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reasonComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reasonComboActionPerformed

    private void reasonComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reasonComboKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_reasonComboKeyPressed

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
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JScrollPane productsScrollPanel;
    private javax.swing.JComboBox<String> reasonCombo;
    private javax.swing.JButton saveBtn;
    private javax.swing.JLabel totalAmountLabel;
    // End of variables declaration//GEN-END:variables
}
