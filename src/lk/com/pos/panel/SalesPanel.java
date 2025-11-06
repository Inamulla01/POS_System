package lk.com.pos.panel;

import lk.com.pos.connection.MySQL;
import lk.com.pos.privateclasses.RoundedPanel;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Date;

public class SalesPanel extends javax.swing.JPanel {

    private JPanel invoicesContainer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
    private int currentWidth = 0;
    
    public SalesPanel() {
        initComponents();
        setupPanel();
        customizeComponents();
        loadSalesData("", "All Time");
        setupEventListeners();
    }
    
    private void setupEventListeners() {
        // Responsive layout
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = getWidth();
                if (Math.abs(newWidth - currentWidth) > 50) {
                    currentWidth = newWidth;
                    adjustLayoutForWidth(newWidth);
                }
            }
        });
        
        // Search functionality
        jTextField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                handleSearch();
            }
        });
        
        // Period filter
        sortByDays.addActionListener(e -> {
            handlePeriodFilter();
        });
    }
    
    private void handleSearch() {
        String searchText = jTextField1.getText().trim();
        if (searchText.equals("Search by invoice number...")) {
            searchText = "";
        }
        String selectedPeriod = sortByDays.getSelectedItem().toString();
        loadSalesData(searchText, selectedPeriod);
    }
    
    private void handlePeriodFilter() {
        String searchText = jTextField1.getText().trim();
        if (searchText.equals("Search by invoice number...")) {
            searchText = "";
        }
        String selectedPeriod = sortByDays.getSelectedItem().toString();
        loadSalesData(searchText, selectedPeriod);
    }
    
    private void setupPanel() {
        jPanel2.setLayout(new BorderLayout());
        jPanel2.setBackground(new Color(248, 250, 252));
    }
    
    private void adjustLayoutForWidth(int width) {
        if (width < 700) {
            jTextField1.setPreferredSize(new Dimension(width - 40, 45));
            sortByDays.setPreferredSize(new Dimension(width - 40, 45));
        } else {
            int halfWidth = (width - 60) / 2;
            jTextField1.setPreferredSize(new Dimension(halfWidth, 45));
            sortByDays.setPreferredSize(new Dimension(halfWidth, 45));
        }
        revalidate();
        repaint();
    }
    
    private void customizeComponents() {
        // Modern search field styling
        jTextField1.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        jTextField1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
            BorderFactory.createEmptyBorder(12, 45, 12, 15)
        ));
        jTextField1.setBackground(Color.WHITE);
        jTextField1.setText("Search by invoice number...");
        jTextField1.setForeground(Color.GRAY);
        
        jTextField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (jTextField1.getText().equals("Search by invoice number...")) {
                    jTextField1.setText("");
                    jTextField1.setForeground(new Color(30, 41, 59));
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (jTextField1.getText().isEmpty()) {
                    jTextField1.setText("Search by invoice number...");
                    jTextField1.setForeground(Color.GRAY);
                }
            }
        });
        
        // Modern combo box styling
        sortByDays.setModel(new DefaultComboBoxModel<>(new String[]{
            "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 90 Days"
        }));
        sortByDays.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        sortByDays.setBackground(Color.WHITE);
        sortByDays.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        roundedPanel1.setVisible(false);
        
        jScrollPane1.setBorder(null);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane1.setBackground(new Color(248, 250, 252));
        
        // Set modern colors for stats panels
        roundedPanel1.setBackgroundColor(Color.decode("#E0F2FF")); // Sky blue
        roundedPanel1.setBorderThickness(0);
        roundedPanel1.setCornerRadius(15);
        
        headPanel.setOpaque(false);
        middelePanel.setOpaque(false);
        buttomPanel.setOpaque(false);
    
        // Also set the productPanel corner radius if it's a RoundedPanel
        if (productPanel instanceof RoundedPanel) {
            ((RoundedPanel) productPanel).setCornerRadius(15);
            ((RoundedPanel) productPanel).setBorderThickness(0);
        }
    }
    
    private void loadSalesData(String searchText, String period) {
        jPanel2.removeAll();
        JPanel loadingPanel = createLoadingPanel();
        jPanel2.add(loadingPanel, BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                invoicesContainer = new JPanel();
                invoicesContainer.setLayout(new BoxLayout(invoicesContainer, BoxLayout.Y_AXIS));
                invoicesContainer.setBackground(new Color(248, 250, 252));
                invoicesContainer.setOpaque(false);
                invoicesContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                
                try {
                    String baseQuery = "SELECT " +
                        "s.sales_id, s.invoice_no, s.datetime, s.total, " +
                        "s.discount_id, " +
                        "(SELECT COALESCE(SUM(si.discount_price), 0) FROM sale_item si WHERE si.sales_id = s.sales_id) as total_item_discount, " +
                        "d.discount as sale_discount_amount, " +
                        "dt.discount_type_id as sale_discount_type, " +
                        "dt.discount_type as sale_discount_type_name, " +
                        "pm.payment_method_name, u.name as cashier_name, " +
                        "COALESCE(cc.customer_name, 'Walk-in Customer') as customer_name " +
                        "FROM sales s " +
                        "INNER JOIN payment_method pm ON s.payment_method_id = pm.payment_method_id " +
                        "INNER JOIN user u ON s.user_id = u.user_id " +
                        "LEFT JOIN credit_customer cc ON s.credit_customer_id = cc.customer_id " +
                        "LEFT JOIN discount d ON s.discount_id = d.discount_id " +
                        "LEFT JOIN discount_type dt ON d.discount_type_id = dt.discount_type_id ";
                    
                    StringBuilder whereClause = new StringBuilder();
                    String orderBy = " ORDER BY s.datetime DESC";
                    
                    if (!searchText.isEmpty() && !searchText.equals("Search by invoice number...")) {
                        whereClause.append("WHERE s.invoice_no LIKE '%").append(searchText).append("%' ");
                    }
                    
                    String dateFilter = getDateFilter(period);
                    if (!dateFilter.isEmpty()) {
                        if (whereClause.length() == 0) {
                            whereClause.append("WHERE ").append(dateFilter);
                        } else {
                            whereClause.append("AND ").append(dateFilter);
                        }
                    }
                    
                    String finalQuery = baseQuery + whereClause.toString() + orderBy;
                    ResultSet rs = MySQL.executeSearch(finalQuery);
                    
                    int count = 0;
                    while (rs.next()) {
                        int salesId = rs.getInt("sales_id");
                        String invoiceNo = rs.getString("invoice_no");
                        String datetime = rs.getString("datetime");
                        double total = rs.getDouble("total");
                        double itemDiscount = rs.getDouble("total_item_discount");
                        int discountId = rs.getInt("discount_id");
                        double saleDiscountAmount = rs.getDouble("sale_discount_amount");
                        int saleDiscountType = rs.getInt("sale_discount_type");
                        String saleDiscountTypeName = rs.getString("sale_discount_type_name");
                        String paymentMethod = rs.getString("payment_method_name");
                        String cashierName = rs.getString("cashier_name");
                        String customerName = rs.getString("customer_name");
                        
                        double calculatedSaleDiscount = calculateSaleDiscount(total, saleDiscountAmount, saleDiscountType, discountId);
                        double totalDiscount = itemDiscount + calculatedSaleDiscount;
                        
                        JPanel invoiceCard = createInvoiceCard(salesId, invoiceNo, datetime, 
                                                               total, itemDiscount, calculatedSaleDiscount, totalDiscount, 
                                                               paymentMethod, cashierName, customerName, discountId);
                        invoicesContainer.add(invoiceCard);
                        invoicesContainer.add(Box.createRigidArea(new Dimension(0, 15)));
                        count++;
                    }
                    
                    if (count == 0) {
                        invoicesContainer.add(createNoDataPanel());
                    }
                    
                    rs.close();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    invoicesContainer.add(createErrorPanel(e));
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    jPanel2.removeAll();
                    JPanel wrapperPanel = new JPanel(new BorderLayout());
                    wrapperPanel.setBackground(new Color(248, 250, 252));
                    wrapperPanel.add(invoicesContainer, BorderLayout.NORTH);
                    jPanel2.add(wrapperPanel, BorderLayout.CENTER);
                    jPanel2.revalidate();
                    jPanel2.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private double calculateSaleDiscount(double total, double saleDiscountAmount, int saleDiscountType, int discountId) {
        double saleDiscount = 0;
        
        if (discountId > 0 && saleDiscountAmount > 0) {
            if (saleDiscountType == 1) { // Percentage discount
                saleDiscount = (total * saleDiscountAmount) / 100;
            } else if (saleDiscountType == 2) { // Fixed amount discount
                saleDiscount = saleDiscountAmount;
            }
        }
        
        return saleDiscount;
    }
    
    private JPanel createLoadingPanel() {
        JPanel loadingPanel = new JPanel();
        loadingPanel.setBackground(new Color(248, 250, 252));
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.add(Box.createVerticalGlue());
        
        JLabel loadingLabel = new JLabel("Loading sales data...");
        loadingLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 16));
        loadingLabel.setForeground(new Color(100, 116, 139));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(loadingLabel);
        loadingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 8));
        progressBar.setMaximumSize(new Dimension(200, 8));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(progressBar);
        loadingPanel.add(Box.createVerticalGlue());
        return loadingPanel;
    }
    
    private JPanel createNoDataPanel() {
        JPanel noDataPanel = new JPanel();
        noDataPanel.setLayout(new BoxLayout(noDataPanel, BoxLayout.Y_AXIS));
        noDataPanel.setBackground(new Color(248, 250, 252));
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        
        JLabel noDataLabel = new JLabel("No sales records found");
        noDataLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 18));
        noDataLabel.setForeground(new Color(148, 163, 184));
        noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(noDataLabel);
        
        JLabel hintLabel = new JLabel("Try adjusting your search or filter");
        hintLabel.setFont(new Font("Nunito", Font.PLAIN, 14));
        hintLabel.setForeground(new Color(148, 163, 184));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        noDataPanel.add(hintLabel);
        
        return noDataPanel;
    }
    
    private JPanel createErrorPanel(Exception e) {
        JPanel errorPanel = new JPanel();
        errorPanel.setBackground(new Color(254, 242, 242));
        errorPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(252, 165, 165), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel errorLabel = new JLabel("Error loading sales data");
        errorLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 16));
        errorLabel.setForeground(new Color(220, 38, 38));
        
        JLabel errorDetails = new JLabel("<html><div style='text-align: center;'>" + 
                                        "Please check your connection<br>and try again</div></html>");
        errorDetails.setFont(new Font("Nunito", Font.PLAIN, 12));
        errorDetails.setForeground(new Color(185, 28, 28));
        
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        errorPanel.add(errorLabel);
        errorPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        errorPanel.add(errorDetails);
        
        return errorPanel;
    }
    
    private String getDateFilter(String period) {
        switch (period) {
            case "Today":
                return "DATE(s.datetime) = CURDATE()";
            case "Last 7 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            case "Last 30 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
            case "Last 90 Days":
                return "s.datetime >= DATE_SUB(NOW(), INTERVAL 90 DAY)";
            default:
                return "";
        }
    }
    
    private JPanel createInvoiceCard(int salesId, String invoiceNo, String datetime, 
                                     double total, double itemDiscount, double saleDiscount, 
                                     double totalDiscount, String paymentMethod, 
                                     String cashierName, String customerName, int discountId) {
        RoundedPanel cardPanel = new RoundedPanel();
        cardPanel.setLayout(new BorderLayout(0, 0));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setCornerRadius(16);
        cardPanel.setBorderThickness(0);
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setOpaque(false);

        JPanel headerPanel = createInvoiceHeader(invoiceNo, customerName, paymentMethod, total);
        JPanel itemsPanel = createItemsContentPanel(salesId);
        JPanel footerPanel = createInvoiceFooter(datetime, itemDiscount, saleDiscount, totalDiscount, cashierName, salesId, discountId);

        headerPanel.setOpaque(false);
        itemsPanel.setOpaque(false);
        footerPanel.setOpaque(false);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(itemsPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        cardPanel.add(contentPanel, BorderLayout.CENTER);

        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cardPanel.setBackground(new Color(245, 247, 250));
                contentPanel.setBackground(new Color(245, 247, 250));
                cardPanel.repaint();
                cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                cardPanel.setBackground(Color.WHITE);
                contentPanel.setBackground(Color.WHITE);
                cardPanel.repaint();
                cardPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return cardPanel;
    }

    private JPanel createInvoiceHeader(String invoiceNo, String customerName, String paymentMethod, double total) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        JLabel iconLabel = createInvoiceIcon();

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel invoiceLabel = new JLabel("#" + (invoiceNo != null ? invoiceNo.toUpperCase() : ""));
        invoiceLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        invoiceLabel.setForeground(new Color(30, 41, 59));
        invoiceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel customerLabel = new JLabel(customerName != null ? customerName : "Walk-in Customer");
        customerLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        customerLabel.setForeground(new Color(100, 116, 139));
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(invoiceLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        textPanel.add(customerLabel);

        leftPanel.add(iconLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        leftPanel.add(textPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel paymentBadge = createPaymentBadge(paymentMethod);
        
        JLabel totalLabel = new JLabel(String.format("Rs.%.2f", total));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 22));
        totalLabel.setForeground(new Color(34, 197, 94));
        totalLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        rightPanel.add(paymentBadge);
        rightPanel.add(Box.createRigidArea(new Dimension(12, 0)));
        rightPanel.add(totalLabel);

        headerPanel.add(leftPanel);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(rightPanel);

        return headerPanel;
    }

    private JLabel createInvoiceIcon() {
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(36, 36));
        iconLabel.setMinimumSize(new Dimension(36, 36));
        iconLabel.setMaximumSize(new Dimension(36, 36));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        
        ImageIcon icon = loadInvoiceIcon();
        if (icon != null) {
            iconLabel.setIcon(icon);
        } else {
            iconLabel.setIcon(createFallbackIcon());
        }
        
        return iconLabel;
    }

    private ImageIcon loadInvoiceIcon() {
        try {
            FlatSVGIcon svgIcon = new FlatSVGIcon("lk/com/pos/icon/invoice.svg", 24, 24);
            return new ImageIcon(svgIcon.getImage());
        } catch (Exception e) {
            try {
                java.net.URL imageUrl = getClass().getResource("/lk/com/pos/icon/invoice.png");
                if (imageUrl != null) {
                    ImageIcon imageIcon = new ImageIcon(imageUrl);
                    Image scaledImage = imageIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImage);
                }
            } catch (Exception ex) {
                // All attempts failed
            }
        }
        return null;
    }

    private ImageIcon createFallbackIcon() {
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GradientPaint gradient = new GradientPaint(0, 0, new Color(59, 130, 246), size, size, new Color(37, 99, 235));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, size, size, 8, 8);
        
        g2d.setColor(new Color(30, 64, 175));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(0, 0, size-1, size-1, 8, 8);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Nunito ExtraBold", Font.BOLD, 10));
        
        FontMetrics fm = g2d.getFontMetrics();
        String text = "INV";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        int x = (size - textWidth) / 2;
        int y = (size - textHeight) / 2 + fm.getAscent();
        
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        
        return new ImageIcon(image);
    }

    private JPanel createItemsContentPanel(int salesId) {
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BorderLayout());
        itemsPanel.setOpaque(false);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        
        JLabel itemsHeader = new JLabel("ITEMS");
        itemsHeader.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
        itemsHeader.setForeground(new Color(71, 85, 105));
        itemsHeader.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        
        headerPanel.add(itemsHeader, BorderLayout.WEST);

        JPanel itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(Color.WHITE);
        itemsContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        loadSaleItems(itemsContainer, salesId);

        itemsPanel.add(headerPanel, BorderLayout.NORTH);
        itemsPanel.add(itemsContainer, BorderLayout.CENTER);

        return itemsPanel;
    }

    private JPanel createInvoiceFooter(String datetime, double itemDiscount, double saleDiscount, 
                                     double totalDiscount, String cashierName, int salesId, int discountId) {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel discountHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        discountHeaderPanel.setOpaque(false);
        discountHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        
        footerPanel.add(discountHeaderPanel);
        footerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel discountPanel = new JPanel();
        discountPanel.setLayout(new BoxLayout(discountPanel, BoxLayout.Y_AXIS));
        discountPanel.setOpaque(false);
        
        if (itemDiscount > 0) {
            JPanel itemDiscountPanel = createDiscountRow("Item Discount", itemDiscount, new Color(100, 116, 139));
            discountPanel.add(itemDiscountPanel);
            discountPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        if (saleDiscount > 0) {
            JPanel saleDiscountPanel = createDiscountRow("Sale Discount", saleDiscount, new Color(245, 158, 11));
            discountPanel.add(saleDiscountPanel);
            discountPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        if (totalDiscount > 0) {
            discountPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            
            JSeparator separator = new JSeparator();
            separator.setForeground(new Color(226, 232, 240));
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            discountPanel.add(separator);
            discountPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            
            JPanel totalDiscountPanel = createDiscountRow("TOTAL DISCOUNT", totalDiscount, new Color(220, 38, 38));
            discountPanel.add(totalDiscountPanel);
        }
        
        footerPanel.add(discountPanel);
        footerPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.X_AXIS));
        datePanel.setOpaque(false);
        datePanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        JLabel dateIcon = createCalendarIcon();
        dateIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        
        JLabel dateLabel = new JLabel(formatDateTime(datetime));
        dateLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(100, 116, 139));
        
        datePanel.add(dateIcon);
        datePanel.add(dateLabel);

        JPanel cashierPanel = new JPanel();
        cashierPanel.setLayout(new BoxLayout(cashierPanel, BoxLayout.X_AXIS));
        cashierPanel.setOpaque(false);
        cashierPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        JLabel cashierIcon = createUserIcon();
        cashierIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        
        JLabel cashierText = new JLabel("Cashier");
        cashierText.setFont(new Font("Nunito SemiBold", Font.PLAIN, 12));
        cashierText.setForeground(new Color(100, 116, 139));
        cashierText.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        
        JLabel cashierValue = new JLabel(cashierName != null ? cashierName : "Unknown");
        cashierValue.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        cashierValue.setForeground(new Color(30, 41, 59));
        
        cashierPanel.add(cashierIcon);
        cashierPanel.add(cashierText);
        cashierPanel.add(cashierValue);

        bottomPanel.add(datePanel);
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(cashierPanel);

        footerPanel.add(bottomPanel);

        return footerPanel;
    }

    private JLabel createCalendarIcon() {
        JLabel iconLabel = new JLabel();
        try {
            FlatSVGIcon calendarIcon = new FlatSVGIcon("lk/com/pos/icon/calendar.svg", 14, 14);
            iconLabel.setIcon(calendarIcon);
        } catch (Exception e) {
            iconLabel.setText("📅");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        }
        return iconLabel;
    }

    private JLabel createUserIcon() {
        JLabel iconLabel = new JLabel();
        try {
            FlatSVGIcon userIcon = new FlatSVGIcon("lk/com/pos/icon/user.svg", 14, 14);
            iconLabel.setIcon(userIcon);
        } catch (Exception e) {
            iconLabel.setText("👤");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        }
        return iconLabel;
    }

    private JPanel createDiscountRow(String label, double amount, Color color) {
        JPanel discountRow = new JPanel();
        discountRow.setLayout(new BoxLayout(discountRow, BoxLayout.X_AXIS));
        discountRow.setOpaque(false);
        discountRow.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        JLabel discountLabel = new JLabel(label);
        discountLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        discountLabel.setForeground(new Color(100, 116, 139));

        JLabel discountValue = new JLabel(String.format("-Rs.%.2f", amount));
        discountValue.setFont(new Font("Nunito ExtraBold", Font.BOLD, 13));
        discountValue.setForeground(color);

        discountRow.add(discountLabel);
        discountRow.add(Box.createHorizontalGlue());
        discountRow.add(discountValue);

        return discountRow;
    }

    private JLabel createPaymentBadge(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "UNKNOWN";
        }
        
        String normalizedPayment = paymentMethod.trim().toUpperCase();
        
        JLabel paymentBadge = new JLabel();
        paymentBadge.setFont(new Font("Nunito ExtraBold", Font.BOLD, 10));
        paymentBadge.setForeground(Color.WHITE);
        paymentBadge.setOpaque(true);
        paymentBadge.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        
        if (normalizedPayment.contains("CASH")) {
            paymentBadge.setBackground(new Color(34, 197, 94));
            paymentBadge.setText("CASH");
        } else if (normalizedPayment.contains("CARD") || normalizedPayment.contains("CREDIT CARD")) {
            paymentBadge.setBackground(new Color(59, 130, 246));
            paymentBadge.setText("CARD");
        } else if (normalizedPayment.contains("CREDIT") || normalizedPayment.contains("ACCOUNT")) {
            paymentBadge.setBackground(new Color(234, 88, 12));
            paymentBadge.setText("CREDIT");
        } else if (normalizedPayment.contains("ONLINE") || normalizedPayment.contains("DIGITAL")) {
            paymentBadge.setBackground(new Color(168, 85, 247));
            paymentBadge.setText("ONLINE");
        } else {
            paymentBadge.setBackground(new Color(100, 116, 139));
            paymentBadge.setText(normalizedPayment.length() > 8 ? 
                               normalizedPayment.substring(0, 8) + "..." : normalizedPayment);
        }
        
        return paymentBadge;
    }

    private void loadSaleItems(JPanel itemsListPanel, int salesId) {
        try {
            String query = "SELECT " +
                "si.qty, si.price, si.discount_price, si.total, " +
                "p.product_name, p.product_id " +
                "FROM sale_item si " +
                "INNER JOIN product p ON si.product_id = p.product_id " +
                "WHERE si.sales_id = ? " +
                "ORDER BY si.sale_item_id";
            
            PreparedStatement pst = MySQL.getConnection().prepareStatement(query);
            pst.setInt(1, salesId);
            ResultSet rs = pst.executeQuery();
            
            int itemCount = 0;
            while (rs.next()) {
                String productName = rs.getString("product_name");
                int qty = rs.getInt("qty");
                double price = rs.getDouble("price");
                double discountPrice = rs.getDouble("discount_price");
                double itemTotal = rs.getDouble("total");
                
                JPanel itemCard = createItemCard(productName, qty, price, discountPrice, itemTotal);
                itemsListPanel.add(itemCard);
                
                if (itemCount > 0) {
                    JSeparator separator = new JSeparator();
                    separator.setForeground(new Color(226, 232, 240));
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    separator.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                    itemsListPanel.add(separator);
                }
                
                itemCount++;
            }
            
            if (itemCount == 0) {
                JLabel noItemsLabel = new JLabel("No items in this sale");
                noItemsLabel.setFont(new Font("Nunito", Font.ITALIC, 11));
                noItemsLabel.setForeground(new Color(148, 163, 184));
                noItemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemsListPanel.add(noItemsLabel);
            }
            
            rs.close();
            pst.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading items");
            errorLabel.setFont(new Font("Nunito", Font.PLAIN, 11));
            errorLabel.setForeground(new Color(220, 38, 38));
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemsListPanel.add(errorLabel);
        }
    }

    private JPanel createItemCard(String productName, int qty, double price, 
                          double discountPrice, double total) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout(8, 0));
        itemPanel.setOpaque(false);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel productLabel = new JLabel(productName != null ? productName : "Unknown Product");
        productLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
        productLabel.setForeground(new Color(30, 41, 59));
        productLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.X_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceQtyLabel = new JLabel(String.format("Rs.%.0f × %d", price, qty));
        priceQtyLabel.setFont(new Font("Nunito", Font.PLAIN, 11));
        priceQtyLabel.setForeground(new Color(100, 116, 139));

        detailsPanel.add(priceQtyLabel);

        if (discountPrice > 0) {
            JLabel discountInfo = new JLabel(String.format(" (Rs.%.0f discount)", discountPrice));
            discountInfo.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
            discountInfo.setForeground(new Color(220, 38, 38));
            detailsPanel.add(discountInfo);
        }

        leftPanel.add(productLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        leftPanel.add(detailsPanel);

        JLabel totalLabel = new JLabel(String.format("Rs.%.2f", total));
        totalLabel.setFont(new Font("Nunito SemiBold", Font.BOLD, 13));
        totalLabel.setForeground(new Color(30, 41, 59));

        itemPanel.add(leftPanel, BorderLayout.CENTER);
        itemPanel.add(totalLabel, BorderLayout.EAST);

        return itemPanel;
    }

    private String formatDateTime(String datetime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
            Date date = inputFormat.parse(datetime);
            return outputFormat.format(date);
        } catch (Exception e) {
            if (datetime != null && datetime.length() >= 10) {
                return datetime.substring(0, 10) + ", " + (datetime.length() > 11 ? datetime.substring(11) : "");
            }
            return datetime != null ? datetime : "Unknown Date";
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        headPanel = new javax.swing.JPanel();
        paymentTypeBtn = new javax.swing.JButton();
        total = new javax.swing.JLabel();
        invoiceName = new javax.swing.JLabel();
        customerName = new javax.swing.JLabel();
        logo = new javax.swing.JLabel();
        middelePanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        productPanel = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        productprice = new javax.swing.JLabel();
        buttomPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        date = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        sortByDays = new javax.swing.JComboBox<>();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        paymentTypeBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        paymentTypeBtn.setText("card");

        total.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        total.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        total.setText("Rs.1000");

        invoiceName.setFont(new java.awt.Font("Nunito ExtraBold", 1, 18)); // NOI18N
        invoiceName.setText("Invoice #inv0010");

        customerName.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        customerName.setText("Coustomer Name");

        logo.setText("icon");

        javax.swing.GroupLayout headPanelLayout = new javax.swing.GroupLayout(headPanel);
        headPanel.setLayout(headPanelLayout);
        headPanelLayout.setHorizontalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logo, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customerName)
                    .addComponent(invoiceName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 434, Short.MAX_VALUE)
                .addComponent(paymentTypeBtn)
                .addGap(18, 18, 18)
                .addComponent(total, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headPanelLayout.setVerticalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPanelLayout.createSequentialGroup()
                .addGroup(headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(paymentTypeBtn)
                            .addComponent(total)))
                    .addGroup(headPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(headPanelLayout.createSequentialGroup()
                                .addComponent(invoiceName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(customerName))
                            .addComponent(logo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        middelePanel.setBackground(new java.awt.Color(252, 246, 252));

        jLabel5.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        jLabel5.setText("Items:");

        productPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel6.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        jLabel6.setText("Metformin 500mg Tablets");

        jLabel7.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        jLabel7.setText("Rs.75.00 * 10 (Rs.5.00 discount)");

        productprice.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12)); // NOI18N
        productprice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        productprice.setText("Rs.750");

        javax.swing.GroupLayout productPanelLayout = new javax.swing.GroupLayout(productPanel);
        productPanel.setLayout(productPanelLayout);
        productPanelLayout.setHorizontalGroup(
            productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(productprice, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        productPanelLayout.setVerticalGroup(
            productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productPanelLayout.createSequentialGroup()
                .addGroup(productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7))
                    .addGroup(productPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(productprice)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout middelePanelLayout = new javax.swing.GroupLayout(middelePanel);
        middelePanel.setLayout(middelePanelLayout);
        middelePanelLayout.setHorizontalGroup(
            middelePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(middelePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(middelePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(productPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(middelePanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        middelePanelLayout.setVerticalGroup(
            middelePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(middelePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel9.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jLabel9.setText("Discount");

        date.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        date.setText("date");

        jLabel12.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jLabel12.setText("Cashier");

        javax.swing.GroupLayout buttomPanelLayout = new javax.swing.GroupLayout(buttomPanel);
        buttomPanel.setLayout(buttomPanelLayout);
        buttomPanelLayout.setHorizontalGroup(
            buttomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttomPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(date)
                .addGap(350, 350, 350)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 318, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addGap(80, 80, 80))
        );
        buttomPanelLayout.setVerticalGroup(
            buttomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttomPanelLayout.createSequentialGroup()
                .addGroup(buttomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buttomPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(middelePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(middelePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(34, 34, 34))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(187, 187, 187))
        );

        jScrollPane1.setViewportView(jPanel2);

        sortByDays.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        sortByDays.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        sortByDays.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sort by Days", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        sortByDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByDaysActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 949, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                        .addGap(113, 113, 113)
                        .addComponent(sortByDays, 0, 368, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortByDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                .addGap(17, 17, 17))
        );

        sortByDays.getAccessibleContext().setAccessibleName("Short by Days");

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

    private void sortByDaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByDaysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortByDaysActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttomPanel;
    private javax.swing.JLabel customerName;
    private javax.swing.JLabel date;
    private javax.swing.JPanel headPanel;
    private javax.swing.JLabel invoiceName;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel logo;
    private javax.swing.JPanel middelePanel;
    private javax.swing.JButton paymentTypeBtn;
    private lk.com.pos.privateclasses.RoundedPanel productPanel;
    private javax.swing.JLabel productprice;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private javax.swing.JComboBox<String> sortByDays;
    private javax.swing.JLabel total;
    // End of variables declaration//GEN-END:variables
}
