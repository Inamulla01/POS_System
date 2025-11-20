package lk.com.pos.panel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import lk.com.pos.connection.MySQL;

/**
 * Enhanced Financial Dashboard Panel for Pharmacy POS System
 * @author moham
 */
public class DashboardPanel extends javax.swing.JPanel {
    
    // Color Constants
    private static final Color PRIMARY = new Color(8, 147, 176);
    private static final Color PRIMARY_LIGHT = new Color(10, 165, 199);
    private static final Color SUCCESS_GREEN = new Color(16, 185, 129);
    private static final Color WARNING_ORANGE = new Color(251, 146, 60);
    private static final Color DANGER_RED = new Color(239, 68, 68);
    private static final Color PURPLE = new Color(139, 92, 246);
    private static final Color PINK = new Color(236, 72, 153);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BG_LIGHT = new Color(249, 250, 251);
    
    // Custom Fonts
    private static Font NUNITO_REGULAR;
    private static Font NUNITO_SEMIBOLD;
    private static Font NUNITO_BOLD;
    private static Font NUNITO_EXTRABOLD;
    
    // Number formatters
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private static final NumberFormat numberFormat = NumberFormat.getInstance();
    
    // Static font initialization
    static {
        try {
            NUNITO_REGULAR = new Font("Nunito", Font.PLAIN, 14);
            NUNITO_SEMIBOLD = new Font("Nunito SemiBold", Font.PLAIN, 14);
            NUNITO_BOLD = new Font("Nunito", Font.BOLD, 14);
            NUNITO_EXTRABOLD = new Font("Nunito ExtraBold", Font.PLAIN, 14);
            
            if (!isFontAvailable("Nunito")) {
                NUNITO_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
                NUNITO_SEMIBOLD = new Font("Segoe UI Semibold", Font.PLAIN, 14);
                NUNITO_BOLD = new Font("Segoe UI", Font.BOLD, 14);
                NUNITO_EXTRABOLD = new Font("Segoe UI", Font.BOLD, 14);
            }
        } catch (Exception e) {
            e.printStackTrace();
            NUNITO_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
            NUNITO_SEMIBOLD = new Font("Segoe UI Semibold", Font.PLAIN, 14);
            NUNITO_BOLD = new Font("Segoe UI", Font.BOLD, 14);
            NUNITO_EXTRABOLD = new Font("Segoe UI", Font.BOLD, 14);
        }
    }
    
    private static boolean isFontAvailable(String fontName) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        for (String font : fonts) {
            if (font.toLowerCase().contains(fontName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    // UI Components
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel metricsPanel;
    private JPanel chartsPanel;
    private JPanel insightsPanel;
    private JButton todayBtn, weekBtn, monthBtn, yearBtn;
    
    // Data holders
    private double totalSales = 0;
    private double netSales = 0;
    private double paymentDue = 0;
    private double totalReturn = 0;
    private double totalPurchase = 0;
    private double purchaseDue = 0;
    private double totalExpenses = 0;
    private double grossProfit = 0;
    private int transactionCount = 0;
    
    private String currentFilter = "TODAY";
    
    /**
     * Creates new form Dashboard
     */
    public DashboardPanel() {
        initComponents();
        initCustomComponents();
        loadDashboardData();
    }
    
    private void initCustomComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createHeaderSection();
        mainPanel.add(Box.createVerticalStrut(20));
        createMetricsSection();
        mainPanel.add(Box.createVerticalStrut(20));
        createChartsSection();
        mainPanel.add(Box.createVerticalStrut(20));
        createInsightsSection();
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createHeaderSection() {
        headerPanel = new JPanel(new BorderLayout(15, 15));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        headerPanel.setBackground(BG_LIGHT);
        
        // Left side - Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(BG_LIGHT);
        
        JLabel iconLabel = new JLabel("📊");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BG_LIGHT);
        
        JLabel titleLabel = new JLabel("Financial Dashboard");
        titleLabel.setFont(NUNITO_EXTRABOLD.deriveFont(26f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel("Real-time business insights");
        subtitleLabel.setFont(NUNITO_REGULAR.deriveFont(13f));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        leftPanel.add(iconLabel);
        leftPanel.add(titlePanel);
        
        // Right side - Filters with Year and Month dropdowns
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(BG_LIGHT);
        
        // Filter buttons
        ButtonGroup filterGroup = new ButtonGroup();
        
        todayBtn = createFilterButton("Today", "TODAY");
        weekBtn = createFilterButton("Week", "WEEK");
        monthBtn = createFilterButton("Month", "MONTH");
        yearBtn = createFilterButton("Year", "YEAR");
        
        filterGroup.add(todayBtn);
        filterGroup.add(weekBtn);
        filterGroup.add(monthBtn);
        filterGroup.add(yearBtn);
        
        todayBtn.setSelected(true);
        
        rightPanel.add(todayBtn);
        rightPanel.add(weekBtn);
        rightPanel.add(monthBtn);
        rightPanel.add(yearBtn);
        
        // Year ComboBox
        JComboBox<String> yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear; i >= currentYear - 10; i--) {
            yearComboBox.addItem(String.valueOf(i));
        }
        yearComboBox.setFont(NUNITO_SEMIBOLD.deriveFont(12f));
        yearComboBox.setPreferredSize(new Dimension(90, 36));
        styleComboBox(yearComboBox);
        
        // Month ComboBox
        String[] months = {"All Months", "January", "February", "March", "April", "May", "June", 
                          "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthComboBox = new JComboBox<>(months);
        monthComboBox.setFont(NUNITO_SEMIBOLD.deriveFont(12f));
        monthComboBox.setPreferredSize(new Dimension(125, 36));
        styleComboBox(monthComboBox);
        
        rightPanel.add(Box.createHorizontalStrut(5));
        rightPanel.add(yearComboBox);
        rightPanel.add(monthComboBox);
        
        // Refresh button
        JButton refreshBtn = createActionButton("🔄", PRIMARY);
        refreshBtn.setToolTipText("Refresh Dashboard");
        refreshBtn.addActionListener(e -> {
            refreshBtn.setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                loadDashboardData();
                refreshBtn.setEnabled(true);
            });
        });
        
        // Export button
        JButton exportBtn = createActionButton("📥", SUCCESS_GREEN);
        exportBtn.setToolTipText("Export Report");
        exportBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Export functionality\nPDF & Excel export coming soon!", 
                "Export", JOptionPane.INFORMATION_MESSAGE);
        });
        
        rightPanel.add(Box.createHorizontalStrut(5));
        rightPanel.add(refreshBtn);
        rightPanel.add(exportBtn);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel);
    }
    
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private JButton createFilterButton(String text, String filter) {
        JButton btn = new JButton(text);
        btn.setFont(NUNITO_SEMIBOLD.deriveFont(13f));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(85, 36));
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_SECONDARY);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        
        btn.addActionListener(e -> {
            currentFilter = filter;
            updateFilterButtonStyles();
            loadDashboardData();
        });
        
        return btn;
    }
    
    private void updateFilterButtonStyles() {
        JButton[] buttons = {todayBtn, weekBtn, monthBtn, yearBtn};
        String[] filters = {"TODAY", "WEEK", "MONTH", "YEAR"};
        
        for (int i = 0; i < buttons.length; i++) {
            if (filters[i].equals(currentFilter)) {
                buttons[i].setBackground(PRIMARY);
                buttons[i].setForeground(Color.WHITE);
                buttons[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 1),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            } else {
                buttons[i].setBackground(Color.WHITE);
                buttons[i].setForeground(TEXT_SECONDARY);
                buttons[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            }
        }
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 36));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
        
        return btn;
    }
    
    private void createMetricsSection() {
        metricsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        metricsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        metricsPanel.setBackground(BG_LIGHT);
        
        mainPanel.add(metricsPanel);
    }
    
    private void updateMetricsCards() {
        metricsPanel.removeAll();
        
        // Row 1
        metricsPanel.add(createMetricCard("Total Sales", "Rs. " + currencyFormat.format(totalSales), 
            "💰", PRIMARY, transactionCount + " transactions"));
        metricsPanel.add(createMetricCard("Net Sales", "Rs. " + currencyFormat.format(netSales), 
            "📈", SUCCESS_GREEN, "After returns & discounts"));
        metricsPanel.add(createMetricCard("Payment Due", "Rs. " + currencyFormat.format(paymentDue), 
            "⏰", WARNING_ORANGE, "Credit pending"));
        metricsPanel.add(createMetricCard("Returns", "Rs. " + currencyFormat.format(totalReturn), 
            "↩️", DANGER_RED, "Total refunds"));
        
        // Row 2
        metricsPanel.add(createMetricCard("Total Purchase", "Rs. " + currencyFormat.format(totalPurchase), 
            "🛒", INDIGO, "Inventory bought"));
        metricsPanel.add(createMetricCard("Purchase Due", "Rs. " + currencyFormat.format(purchaseDue), 
            "📋", PINK, "Supplier payments"));
        metricsPanel.add(createMetricCard("Expenses", "Rs. " + currencyFormat.format(totalExpenses), 
            "💸", DANGER_RED, "Operating costs"));
        metricsPanel.add(createMetricCard("Gross Profit", "Rs. " + currencyFormat.format(grossProfit), 
            "💎", grossProfit >= 0 ? SUCCESS_GREEN : DANGER_RED, 
            grossProfit >= 0 ? "Profitable" : "Loss"));
        
        metricsPanel.revalidate();
        metricsPanel.repaint();
    }
    
    private JPanel createMetricCard(String title, String value, String emoji, Color accentColor, String subtitle) {
        JPanel card = new RoundedPanel(16);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(249, 250, 251));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
            }
        });
        
        // Top - Icon and Title
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        
        // Icon badge
        JPanel iconBadge = new RoundedPanel(12);
        iconBadge.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), 
                                         accentColor.getBlue(), 25));
        iconBadge.setLayout(new GridBagLayout());
        iconBadge.setPreferredSize(new Dimension(48, 48));
        iconBadge.setMinimumSize(new Dimension(48, 48));
        iconBadge.setMaximumSize(new Dimension(48, 48));
        
        JLabel iconLabel = new JLabel(emoji);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconBadge.add(iconLabel);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(NUNITO_SEMIBOLD.deriveFont(13f));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        topPanel.add(iconBadge, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Middle - Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(NUNITO_EXTRABOLD.deriveFont(24f));
        valueLabel.setForeground(TEXT_PRIMARY);
        
        // Bottom - Subtitle
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(NUNITO_REGULAR.deriveFont(11f));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private void createChartsSection() {
        chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        chartsPanel.setBackground(BG_LIGHT);
        
        mainPanel.add(chartsPanel);
    }
    
    private void updateCharts() {
        chartsPanel.removeAll();
        
        chartsPanel.add(createSalesVsReturnsChart());
        chartsPanel.add(createExpenseBreakdownChart());
        
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    private JPanel createSalesVsReturnsChart() {
        JPanel panel = new RoundedPanel(16);
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("📊 Sales vs Returns Trend");
        titleLabel.setFont(NUNITO_BOLD.deriveFont(17f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Sales", PRIMARY));
        legendPanel.add(createLegendItem("Returns", DANGER_RED));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(legendPanel, BorderLayout.EAST);
        
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try {
            String dateCondition = getDateCondition("s.datetime");
            String groupBy = getGroupByClause();
            
            // Sales data
            String salesQuery = "SELECT " + groupBy + " as period, COALESCE(SUM(s.total), 0) as total " +
                               "FROM sales s WHERE " + dateCondition + " GROUP BY period ORDER BY period";
            
            ResultSet salesRs = MySQL.executeSearch(salesQuery);
            while (salesRs.next()) {
                String label = formatChartLabel(salesRs.getString(1));
                double amount = salesRs.getDouble(2);
                dataset.addValue(amount, "Sales", label);
            }
            
            // Returns data - Calculate from return_item using total_return_amount
            String returnGroupBy = getReturnGroupByClause();
            String returnQuery = "SELECT " + returnGroupBy + " as period, " +
                                "COALESCE(SUM(ri.total_return_amount), 0) as total " +
                                "FROM return_item ri " +
                                "INNER JOIN `return` r ON ri.return_id = r.return_id " +
                                "WHERE " + dateCondition.replace("s.datetime", "r.return_date") + 
                                " GROUP BY period ORDER BY period";
            
            ResultSet returnRs = MySQL.executeSearch(returnQuery);
            while (returnRs.next()) {
                String label = formatChartLabel(returnRs.getString(1));
                double amount = returnRs.getDouble(2);
                dataset.addValue(amount, "Returns", label);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JFreeChart chart = ChartFactory.createLineChart(
            null, null, "Amount (Rs.)", dataset,
            PlotOrientation.VERTICAL, false, true, false
        );
        
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
        plot.setDomainGridlinePaint(new Color(240, 240, 240));
        plot.setOutlineVisible(false);
        
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, PRIMARY);
        renderer.setSeriesPaint(1, DANGER_RED);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        plot.setRenderer(renderer);
        
        org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 330));
        chartPanel.setBackground(Color.WHITE);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createExpenseBreakdownChart() {
        JPanel panel = new RoundedPanel(16);
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("🥧 Expense Distribution");
        titleLabel.setFont(NUNITO_BOLD.deriveFont(17f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        try {
            String dateCondition = getDateCondition("e.date");
            String query = "SELECT et.expenses_type, COALESCE(SUM(e.amount), 0) as total " +
                          "FROM expenses e INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                          "WHERE " + dateCondition + " GROUP BY et.expenses_type ORDER BY total DESC LIMIT 8";
            
            ResultSet rs = MySQL.executeSearch(query);
            while (rs.next()) {
                String type = rs.getString(1);
                double amount = rs.getDouble(2);
                if (amount > 0) {
                    dataset.setValue(type, amount);
                }
            }
            
            // If no data, add placeholder
            if (dataset.getItemCount() == 0) {
                dataset.setValue("No expenses", 1);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            dataset.setValue("Error loading data", 1);
        }
        
        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(NUNITO_REGULAR.deriveFont(11f));
        
        Color[] colors = {PRIMARY, SUCCESS_GREEN, WARNING_ORANGE, DANGER_RED, PURPLE, PINK, INDIGO, PRIMARY_LIGHT};
        int colorIndex = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable) key, colors[colorIndex % colors.length]);
            colorIndex++;
        }
        
        org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 330));
        chartPanel.setBackground(Color.WHITE);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLegendItem(String label, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        
        JPanel colorBox = new RoundedPanel(4);
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(16, 16));
        
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(NUNITO_SEMIBOLD.deriveFont(12f));
        textLabel.setForeground(TEXT_SECONDARY);
        
        panel.add(colorBox);
        panel.add(textLabel);
        
        return panel;
    }
    
    private void createInsightsSection() {
        insightsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        insightsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        insightsPanel.setBackground(BG_LIGHT);
        
        mainPanel.add(insightsPanel);
    }
    
    private void updateInsights() {
        insightsPanel.removeAll();
        
        insightsPanel.add(createBestSellingPanel());
        insightsPanel.add(createLowStockPanel());
        insightsPanel.add(createExpiringPanel());
        
        insightsPanel.revalidate();
        insightsPanel.repaint();
    }
    
    private JPanel createBestSellingPanel() {
        JPanel panel = new RoundedPanel(16);
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("🏆 Best Selling");
        titleLabel.setFont(NUNITO_BOLD.deriveFont(16f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel badge = new JLabel("TOP 5");
        badge.setFont(NUNITO_BOLD.deriveFont(10f));
        badge.setForeground(SUCCESS_GREEN);
        badge.setOpaque(true);
        badge.setBackground(new Color(SUCCESS_GREEN.getRed(), SUCCESS_GREEN.getGreen(), 
                                     SUCCESS_GREEN.getBlue(), 25));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(badge, BorderLayout.EAST);
        
        // List panel
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        try {
            String dateCondition = getDateCondition("s.datetime");
            String query = "SELECT p.product_name, SUM(si.qty) as units, SUM(si.total) as revenue " +
                          "FROM sale_item si " +
                          "INNER JOIN sales s ON si.sales_id = s.sales_id " +
                          "INNER JOIN stock st ON si.stock_id = st.stock_id " +
                          "INNER JOIN product p ON st.product_id = p.product_id " +
                          "WHERE " + dateCondition + " " +
                          "GROUP BY p.product_id ORDER BY units DESC LIMIT 5";
            
            ResultSet rs = MySQL.executeSearch(query);
            int rank = 1;
            boolean hasData = false;
            
            while (rs.next()) {
                hasData = true;
                String product = rs.getString(1);
                double units = rs.getDouble(2);
                double revenue = rs.getDouble(3);
                
                if (rank > 1) listPanel.add(Box.createVerticalStrut(8));
                
                listPanel.add(createListItem(
                    String.valueOf(rank), 
                    product, 
                    "Revenue: Rs. " + currencyFormat.format(revenue),
                    (int)units + " units",
                    PRIMARY
                ));
                
                rank++;
            }
            
            if (!hasData) {
                JLabel noDataLabel = new JLabel("No sales data available");
                noDataLabel.setFont(NUNITO_REGULAR.deriveFont(13f));
                noDataLabel.setForeground(TEXT_SECONDARY);
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                listPanel.add(noDataLabel);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading data");
            errorLabel.setFont(NUNITO_REGULAR.deriveFont(13f));
            errorLabel.setForeground(DANGER_RED);
            listPanel.add(errorLabel);
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLowStockPanel() {
        JPanel panel = new RoundedPanel(16);
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("⚠️ Low Stock Alerts");
        titleLabel.setFont(NUNITO_BOLD.deriveFont(16f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // List panel
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        try {
            // Query to find stocks with low quantity (less than 10)
            String query = "SELECT p.product_name, s.qty, s.batch_no " +
                          "FROM stock s " +
                          "INNER JOIN product p ON s.product_id = p.product_id " +
                          "WHERE s.qty < 10 AND s.qty > 0 " +
                          "ORDER BY s.qty ASC LIMIT 5";
            
            ResultSet rs = MySQL.executeSearch(query);
            boolean hasData = false;
            
            while (rs.next()) {
                hasData = true;
                String product = rs.getString(1);
                double qty = rs.getDouble(2);
                String batch = rs.getString(3);
                
                if (hasData) listPanel.add(Box.createVerticalStrut(8));
                
                listPanel.add(createListItem(
                    "📦", 
                    product, 
                    "Batch: " + (batch != null ? batch : "N/A"),
                    (int)qty + " left",
                    WARNING_ORANGE
                ));
                
                hasData = true;
            }
            
            if (!hasData) {
                JLabel noDataLabel = new JLabel("All items well stocked!");
                noDataLabel.setFont(NUNITO_REGULAR.deriveFont(13f));
                noDataLabel.setForeground(SUCCESS_GREEN);
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                listPanel.add(noDataLabel);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading stock data");
            errorLabel.setFont(NUNITO_REGULAR.deriveFont(13f));
            errorLabel.setForeground(DANGER_RED);
            listPanel.add(errorLabel);
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createExpiringPanel() {
        JPanel panel = new RoundedPanel(16);
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("📅 Recent Stock");
        titleLabel.setFont(NUNITO_BOLD.deriveFont(16f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // List panel - Show recent stock entries instead
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        try {
            String query = "SELECT p.product_name, s.manufacture_date, s.qty, s.batch_no " +
                          "FROM stock s " +
                          "INNER JOIN product p ON s.product_id = p.product_id " +
                          "WHERE s.qty > 0 " +
                          "ORDER BY s.stock_id DESC LIMIT 5";
            
            ResultSet rs = MySQL.executeSearch(query);
            boolean hasData = false;
            
            while (rs.next()) {
                hasData = true;
                String product = rs.getString(1);
                String mfgDate = rs.getString(2);
                double qty = rs.getDouble(3);
                String batch = rs.getString(4);
                
                if (hasData) listPanel.add(Box.createVerticalStrut(8));
                
                listPanel.add(createListItem(
                    "📦", 
                    product, 
                    "Batch: " + (batch != null ? batch : "N/A"),
                    (int)qty + " units",
                    PRIMARY
                ));
                
                hasData = true;
            }
            
            if (!hasData) {
                JLabel noDataLabel = new JLabel("No stock available");
                noDataLabel.setFont(NUNITO_REGULAR.deriveFont(13f));
                noDataLabel.setForeground(TEXT_SECONDARY);
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                listPanel.add(noDataLabel);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading stock data");
            errorLabel.setFont(NUNITO_REGULAR.deriveFont(13f));
            errorLabel.setForeground(DANGER_RED);
            listPanel.add(errorLabel);
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createListItem(String icon, String title, String subtitle, String badge, Color accentColor) {
        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        // Add hover effect
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(new Color(249, 250, 251));
                item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(Color.WHITE);
            }
        });
        
        // Left - Icon/Rank
        JPanel iconPanel = new RoundedPanel(8);
        iconPanel.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), 
                                         accentColor.getBlue(), 25));
        iconPanel.setLayout(new GridBagLayout());
        iconPanel.setPreferredSize(new Dimension(40, 40));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(icon.matches("\\d+") ? NUNITO_BOLD.deriveFont(16f) : 
                         new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconLabel.setForeground(icon.matches("\\d+") ? accentColor : Color.BLACK);
        iconPanel.add(iconLabel);
        
        // Center - Text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(NUNITO_SEMIBOLD.deriveFont(13f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(NUNITO_REGULAR.deriveFont(11f));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(subtitleLabel);
        
        // Right - Badge
        JLabel badgeLabel = new JLabel(badge);
        badgeLabel.setFont(NUNITO_BOLD.deriveFont(11f));
        badgeLabel.setForeground(accentColor);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), 
                                          accentColor.getBlue(), 20));
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        item.add(iconPanel, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        item.add(badgeLabel, BorderLayout.EAST);
        
        return item;
    }
    
    private void loadDashboardData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            String dateCondition = getDateCondition("s.datetime");
            
            // Total Sales
            String salesQuery = "SELECT COALESCE(SUM(total), 0), COUNT(*) FROM sales s WHERE " + dateCondition;
            ResultSet salesRs = MySQL.executeSearch(salesQuery);
            if (salesRs.next()) {
                totalSales = salesRs.getDouble(1);
                transactionCount = salesRs.getInt(2);
            }
            
            // Total Returns - Calculate from return_item table using total_return_amount
            String returnQuery = "SELECT COALESCE(SUM(ri.total_return_amount), 0) FROM return_item ri " +
                                "INNER JOIN `return` r ON ri.return_id = r.return_id " +
                                "WHERE " + dateCondition.replace("s.datetime", "r.return_date");
            ResultSet returnRs = MySQL.executeSearch(returnQuery);
            if (returnRs.next()) {
                totalReturn = returnRs.getDouble(1);
            }
            
            // Net Sales
            netSales = totalSales - totalReturn;
            
            // Payment Due (Credit) - sum of unpaid credit amounts
            String creditQuery = "SELECT COALESCE(SUM(c.credit_final_date), 0) " +
                                "FROM credit c " +
                                "INNER JOIN sales s ON c.sales_id = s.sales_id " +
                                "WHERE " + dateCondition + " AND c.credit_status = 'UNPAID'";
            
            try {
                ResultSet creditRs = MySQL.executeSearch(creditQuery);
                if (creditRs.next()) {
                    paymentDue = creditRs.getDouble(1);
                }
            } catch (Exception e) {
                // If credit_status doesn't exist, try alternative
                paymentDue = 0;
            }
            
            // Expenses
            String expenseQuery = "SELECT COALESCE(SUM(amount), 0) FROM expenses e WHERE " + 
                                 dateCondition.replace("s.datetime", "e.date");
            ResultSet expenseRs = MySQL.executeSearch(expenseQuery);
            if (expenseRs.next()) {
                totalExpenses = expenseRs.getDouble(1);
            }
            
            // Purchase & Purchase Due - Set to 0 if tables don't exist
            // You can add GRN tables later if needed
            totalPurchase = 0;
            purchaseDue = 0;
            
            // Gross Profit
            grossProfit = netSales - totalExpenses;
            
            SwingUtilities.invokeLater(() -> {
                updateMetricsCards();
                updateCharts();
                updateInsights();
                setCursor(Cursor.getDefaultCursor());
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, 
                "Error loading dashboard:\n" + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getDateCondition(String dateField) {
        switch (currentFilter) {
            case "TODAY":
                return "DATE(" + dateField + ") = CURDATE()";
                
            case "WEEK":
                return "YEARWEEK(" + dateField + ", 1) = YEARWEEK(CURDATE(), 1)";
                
            case "MONTH":
                return "YEAR(" + dateField + ") = YEAR(CURDATE()) AND MONTH(" + dateField + ") = MONTH(CURDATE())";
                
            case "YEAR":
                return "YEAR(" + dateField + ") = YEAR(CURDATE())";
                
            default:
                return "1=1";
        }
    }
    
    private String getGroupByClause() {
        switch (currentFilter) {
            case "TODAY":
                return "HOUR(s.datetime)";
            case "WEEK":
                return "DAYOFWEEK(s.datetime)";
            case "MONTH":
                return "DAY(s.datetime)";
            case "YEAR":
                return "MONTH(s.datetime)";
            default:
                return "DATE(s.datetime)";
        }
    }
    
    private String getReturnGroupByClause() {
        switch (currentFilter) {
            case "TODAY":
                return "HOUR(r.return_date)";
            case "WEEK":
                return "DAYOFWEEK(r.return_date)";
            case "MONTH":
                return "DAY(r.return_date)";
            case "YEAR":
                return "MONTH(r.return_date)";
            default:
                return "DATE(r.return_date)";
        }
    }
    
    private String formatChartLabel(String value) {
        try {
            int val = Integer.parseInt(value);
            
            switch (currentFilter) {
                case "TODAY":
                    return val + ":00";
                case "WEEK":
                    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                    return days[val - 1];
                case "MONTH":
                    return String.valueOf(val);
                case "YEAR":
                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    return months[val - 1];
                default:
                    return value;
            }
        } catch (Exception e) {
            return value;
        }
    }
    
    // Rounded Panel Helper Class
    class RoundedPanel extends JPanel {
        private int cornerRadius;
        
        public RoundedPanel(int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 
                                               cornerRadius, cornerRadius));
            g2.dispose();
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 710, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 388, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
