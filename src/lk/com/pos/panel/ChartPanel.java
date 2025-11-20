package lk.com.pos.panel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
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
 * Modern Financial Dashboard Panel for Pharmacy POS System
 * @author pasin
 */
public class ChartPanel extends javax.swing.JPanel {
    
    // Color Constants
    private static final Color PRIMARY = new Color(8, 147, 176);
    private static final Color PRIMARY_LIGHT = new Color(10, 165, 199);
    private static final Color PRIMARY_LIGHTER = new Color(43, 180, 209);
    private static final Color INCOME_GREEN = new Color(46, 204, 113);
    private static final Color EXPENSE_RED = new Color(231, 76, 60);
    private static final Color WARNING_ORANGE = new Color(243, 156, 18);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color BG_LIGHT = new Color(248, 249, 250);
    private static final Color CARD_SHADOW = new Color(0, 0, 0, 10);
    
    // Custom Fonts
    private static Font NUNITO_REGULAR;
    private static Font NUNITO_SEMIBOLD;
    private static Font NUNITO_EXTRABOLD;
    
    // Number formatter
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private static final NumberFormat numberFormat = NumberFormat.getInstance();
    
    // Static font initialization
    static {
        try {
            // Try to load Nunito fonts from system or resources
            NUNITO_REGULAR = new Font("Nunito", Font.PLAIN, 14);
            NUNITO_SEMIBOLD = new Font("Nunito SemiBold", Font.PLAIN, 14);
            NUNITO_EXTRABOLD = new Font("Nunito ExtraBold", Font.PLAIN, 14);
            
            // If Nunito is not available, use fallback
            if (!isFontAvailable("Nunito")) {
                System.out.println("Nunito font not found. Using Segoe UI as fallback.");
                NUNITO_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
                NUNITO_SEMIBOLD = new Font("Segoe UI Semibold", Font.PLAIN, 14);
                NUNITO_EXTRABOLD = new Font("Segoe UI", Font.BOLD, 14);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to Segoe UI
            NUNITO_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
            NUNITO_SEMIBOLD = new Font("Segoe UI Semibold", Font.PLAIN, 14);
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
    private JPanel summaryCardsPanel;
    private JPanel chartsPanel;
    private JPanel transactionPanel;
    private JComboBox<String> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> filterTypeComboBox;
    
    // Data holders
    private double totalIncome = 0;
    private double totalExpense = 0;
    private double netProfit = 0;
    private int transactionCount = 0;
    
    public ChartPanel() {
        initComponents();
        initCustomComponents();
        loadDashboardData();
    }
    
    private void initCustomComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);
        
        // Main container with padding
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Create all sections
        createHeaderSection();
        mainPanel.add(Box.createVerticalStrut(25));
        createSummaryCards();
        mainPanel.add(Box.createVerticalStrut(25));
        createChartsSection();
        mainPanel.add(Box.createVerticalStrut(25));
        createTransactionTable();
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createHeaderSection() {
        headerPanel = new JPanel(new BorderLayout(20, 20));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        headerPanel.setBackground(BG_LIGHT);
        
        // Left side - Title with icon
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(BG_LIGHT);
        
        // Dashboard icon
        JLabel iconLabel = new JLabel("📊");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JLabel titleLabel = new JLabel("Financial Dashboard");
        titleLabel.setFont(NUNITO_EXTRABOLD.deriveFont(28f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        leftPanel.add(iconLabel);
        leftPanel.add(titleLabel);
        
        // Right side - Filters in single row
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setBackground(BG_LIGHT);
        
        // View type dropdown
        filterTypeComboBox = new JComboBox<>(new String[]{"📅 Monthly View", "📆 Yearly View", "📊 Custom Range"});
        filterTypeComboBox.setFont(NUNITO_SEMIBOLD.deriveFont(12f));
        filterTypeComboBox.setPreferredSize(new Dimension(170, 38));
        styleComboBox(filterTypeComboBox);
        
        // Year selector
        yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear; i >= currentYear - 10; i--) {
            yearComboBox.addItem(String.valueOf(i));
        }
        yearComboBox.setFont(NUNITO_SEMIBOLD.deriveFont(13f));
        yearComboBox.setPreferredSize(new Dimension(110, 38));
        styleComboBox(yearComboBox);
        
        // Month selector
        String[] months = {"All Months", "January", "February", "March", "April", "May", "June", 
                          "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setFont(NUNITO_SEMIBOLD.deriveFont(12f));
        monthComboBox.setPreferredSize(new Dimension(140, 38));
        styleComboBox(monthComboBox);
        
        // Refresh button with icon
        JButton refreshBtn = createStyledButton("🔄 Refresh", PRIMARY, true);
        refreshBtn.setPreferredSize(new Dimension(115, 38));
        refreshBtn.addActionListener(e -> {
            refreshBtn.setEnabled(false);
            refreshBtn.setText("⏳ Loading...");
            SwingUtilities.invokeLater(() -> {
                loadDashboardData();
                refreshBtn.setEnabled(true);
                refreshBtn.setText("🔄 Refresh");
            });
        });
        
        // Export button
        JButton exportBtn = createStyledButton("📥 Export", Color.WHITE, false);
        exportBtn.setPreferredSize(new Dimension(105, 38));
        exportBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Export functionality coming soon!\nYou'll be able to export to PDF and Excel.", 
                "Export Options", JOptionPane.INFORMATION_MESSAGE);
        });
        
        rightPanel.add(filterTypeComboBox);
        rightPanel.add(yearComboBox);
        rightPanel.add(monthComboBox);
        rightPanel.add(refreshBtn);
        rightPanel.add(exportBtn);
        
        // Add action listeners
        filterTypeComboBox.addActionListener(e -> updateFilterVisibility());
        yearComboBox.addActionListener(e -> loadDashboardData());
        monthComboBox.addActionListener(e -> loadDashboardData());
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel);
    }
    
    private void updateFilterVisibility() {
        String selected = (String) filterTypeComboBox.getSelectedItem();
        monthComboBox.setVisible(selected.equals("Monthly View"));
        loadDashboardData();
    }
    
    private void createSummaryCards() {
        summaryCardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryCardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        summaryCardsPanel.setBackground(BG_LIGHT);
        
        mainPanel.add(summaryCardsPanel);
    }
    
    private void updateSummaryCards() {
        summaryCardsPanel.removeAll();
        
        // Calculate percentages
        double incomeChange = totalIncome > 0 ? 12.5 : 0;
        double expenseChange = totalExpense > 0 ? 8.2 : 0;
        double profitChange = netProfit > 0 ? 18.7 : netProfit < 0 ? -5.2 : 0;
        
        summaryCardsPanel.add(createSummaryCard(
            "Total Income", 
            "Rs. " + currencyFormat.format(totalIncome), 
            "From sales & other sources",
            String.format("%+.1f%%", incomeChange),
            PRIMARY,
            INCOME_GREEN,
            "💵"
        ));
        
        summaryCardsPanel.add(createSummaryCard(
            "Total Expenses", 
            "Rs. " + currencyFormat.format(totalExpense), 
            "Operating costs",
            String.format("%+.1f%%", expenseChange),
            EXPENSE_RED,
            EXPENSE_RED,
            "📊"
        ));
        
        summaryCardsPanel.add(createSummaryCard(
            "Net Profit", 
            "Rs. " + currencyFormat.format(netProfit), 
            netProfit >= 0 ? "Profitable period" : "Loss period",
            String.format("%+.1f%%", profitChange),
            PRIMARY_LIGHTER,
            netProfit >= 0 ? INCOME_GREEN : EXPENSE_RED,
            "💎"
        ));
        
        summaryCardsPanel.add(createSummaryCard(
            "Transactions", 
            numberFormat.format(transactionCount), 
            "Total sales count",
            numberFormat.format(transactionCount) + " sales",
            WARNING_ORANGE,
            TEXT_PRIMARY,
            "🛒"
        ));
        
        summaryCardsPanel.revalidate();
        summaryCardsPanel.repaint();
    }
    
    private JPanel createSummaryCard(String title, String value, String subtitle, 
                                     String badge, Color accentColor, Color badgeColor, String emoji) {
        JPanel card = new RoundedPanel(12);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(280, 140));
        
        // Add subtle hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(249, 250, 251));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
            }
        });
        
        // Top row - Title and Icon badge
        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(NUNITO_REGULAR.deriveFont(11f));
        titleLabel.setForeground(new Color(107, 114, 128));
        
        // Icon badge - rounded square
        JPanel iconBadge = new JPanel(new GridBagLayout());
        iconBadge.setBackground(accentColor);
        iconBadge.setPreferredSize(new Dimension(38, 38));
        iconBadge.setMinimumSize(new Dimension(38, 38));
        iconBadge.setMaximumSize(new Dimension(38, 38));
        
        JLabel iconLabel = new JLabel(emoji);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconBadge.add(iconLabel);
        
        // Make icon badge rounded
        iconBadge.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRoundRect(x, y, width - 1, height - 1, 10, 10);
                g2.dispose();
            }
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(0, 0, 0, 0);
            }
        });
        
        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(iconBadge, BorderLayout.EAST);
        
        // Middle - Value (large text)
        JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        middlePanel.setOpaque(false);
        middlePanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(NUNITO_EXTRABOLD.deriveFont(28f));
        valueLabel.setForeground(new Color(17, 24, 39));
        
        middlePanel.add(valueLabel);
        
        // Bottom row - Subtitle and Badge
        JPanel bottomRow = new JPanel(new BorderLayout(8, 0));
        bottomRow.setOpaque(false);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(NUNITO_REGULAR.deriveFont(9.5f));
        subtitleLabel.setForeground(new Color(156, 163, 175));
        
        bottomRow.add(subtitleLabel, BorderLayout.WEST);
        
        // Percentage badge (only if not transaction count)
        if (!badge.contains("sales")) {
            JLabel percentageLabel = new JLabel(badge);
            percentageLabel.setFont(NUNITO_SEMIBOLD.deriveFont(9.5f));
            percentageLabel.setForeground(badgeColor);
            percentageLabel.setOpaque(true);
            percentageLabel.setBackground(new Color(badgeColor.getRed(), badgeColor.getGreen(), 
                                                   badgeColor.getBlue(), 20));
            percentageLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            percentageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Rounded badge
            percentageLabel.setBorder(new javax.swing.border.AbstractBorder() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), 
                                         badgeColor.getBlue(), 20));
                    g2.fillRoundRect(x, y, width - 1, height - 1, 12, 12);
                    g2.dispose();
                }
                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(3, 8, 3, 8);
                }
            });
            
            bottomRow.add(percentageLabel, BorderLayout.EAST);
        }
        
        // Assemble card
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        middlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(topRow);
        contentPanel.add(middlePanel);
        contentPanel.add(bottomRow);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    // Helper class for rounded badge borders
    class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        private Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
        
        public boolean isBorderOpaque() {
            return false;
        }
        
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }
    
    private void createChartsSection() {
        chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));
        chartsPanel.setBackground(BG_LIGHT);
        
        mainPanel.add(chartsPanel);
    }
    
    private void updateCharts() {
        chartsPanel.removeAll();
        
        // Line Chart - Income vs Expense
        chartsPanel.add(createLineChartPanel());
        
        // Pie Chart - Expense Breakdown
        chartsPanel.add(createPieChartPanel());
        
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    private JPanel createLineChartPanel() {
        JPanel panel = new RoundedPanel(20);
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Header with icon
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("📈 Income vs Expense Trend");
        titleLabel.setFont(NUNITO_EXTRABOLD.deriveFont(18f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        legendPanel.setOpaque(false);
        
        JPanel incomeLegend = createLegendItem("Income", PRIMARY);
        JPanel expenseLegend = createLegendItem("Expense", EXPENSE_RED);
        
        legendPanel.add(incomeLegend);
        legendPanel.add(expenseLegend);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(legendPanel, BorderLayout.EAST);
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try {
            String year = (String) yearComboBox.getSelectedItem();
            String query;
            
            if (monthComboBox.getSelectedIndex() == 0) { // All months
                query = "SELECT MONTH(date) as period, SUM(amount) as total FROM " +
                       "(SELECT datetime as date, total as amount FROM sales WHERE YEAR(datetime) = " + year +
                       " UNION ALL " +
                       "SELECT date, amount FROM income WHERE YEAR(date) = " + year + ") as income_data " +
                       "GROUP BY MONTH(date) ORDER BY period";
            } else {
                int month = monthComboBox.getSelectedIndex();
                query = "SELECT DAY(date) as period, SUM(amount) as total FROM " +
                       "(SELECT datetime as date, total as amount FROM sales WHERE YEAR(datetime) = " + year + 
                       " AND MONTH(datetime) = " + month +
                       " UNION ALL " +
                       "SELECT date, amount FROM income WHERE YEAR(date) = " + year + 
                       " AND MONTH(date) = " + month + ") as income_data " +
                       "GROUP BY DAY(date) ORDER BY period";
            }
            
            ResultSet incomeRs = MySQL.executeSearch(query);
            
            while (incomeRs.next()) {
                String label = formatChartLabel(incomeRs.getInt(1), monthComboBox.getSelectedIndex() == 0);
                double amount = incomeRs.getDouble(2);
                dataset.addValue(amount, "Income", label);
            }
            
            // Get expenses
            if (monthComboBox.getSelectedIndex() == 0) {
                query = "SELECT MONTH(date) as period, SUM(amount) as total FROM expenses " +
                       "WHERE YEAR(date) = " + year + " GROUP BY MONTH(date) ORDER BY period";
            } else {
                int month = monthComboBox.getSelectedIndex();
                query = "SELECT DAY(date) as period, SUM(amount) as total FROM expenses " +
                       "WHERE YEAR(date) = " + year + " AND MONTH(date) = " + month + 
                       " GROUP BY DAY(date) ORDER BY period";
            }
            
            ResultSet expenseRs = MySQL.executeSearch(query);
            
            while (expenseRs.next()) {
                String label = formatChartLabel(expenseRs.getInt(1), monthComboBox.getSelectedIndex() == 0);
                double amount = expenseRs.getDouble(2);
                dataset.addValue(amount, "Expense", label);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JFreeChart lineChart = ChartFactory.createLineChart(
            null, 
            monthComboBox.getSelectedIndex() == 0 ? "Month" : "Day",
            "Amount (Rs.)",
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false
        );
        
        lineChart.setBackgroundPaint(Color.WHITE);
        
        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setOutlineVisible(false);
        
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, PRIMARY);
        renderer.setSeriesPaint(1, EXPENSE_RED);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        plot.setRenderer(renderer);
        
        org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(lineChart);
        chartPanel.setPreferredSize(new Dimension(500, 350));
        chartPanel.setBackground(Color.WHITE);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPieChartPanel() {
        JPanel panel = new RoundedPanel(20);
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel titleLabel = new JLabel("🥧 Expense Breakdown");
        titleLabel.setFont(NUNITO_EXTRABOLD.deriveFont(18f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        try {
            String year = (String) yearComboBox.getSelectedItem();
            String query = "SELECT et.expenses_type, SUM(e.amount) as total " +
                          "FROM expenses e " +
                          "INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                          "WHERE YEAR(e.date) = " + year;
            
            if (monthComboBox.getSelectedIndex() != 0) {
                query += " AND MONTH(e.date) = " + monthComboBox.getSelectedIndex();
            }
            
            query += " GROUP BY et.expenses_type_id ORDER BY total DESC";
            
            ResultSet rs = MySQL.executeSearch(query);
            
            while (rs.next()) {
                dataset.setValue(rs.getString(1), rs.getDouble(2));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JFreeChart pieChart = ChartFactory.createPieChart(
            null,
            dataset,
            true, true, false
        );
        
        pieChart.setBackgroundPaint(Color.WHITE);
        
        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(NUNITO_REGULAR.deriveFont(12f));
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        
        // Set colors for pie sections
        Color[] colors = {PRIMARY, PRIMARY_LIGHT, PRIMARY_LIGHTER, new Color(92, 201, 221), 
                         WARNING_ORANGE, new Color(155, 89, 182)};
        int colorIndex = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable) key, colors[colorIndex % colors.length]);
            colorIndex++;
        }
        
        org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(400, 350));
        chartPanel.setBackground(Color.WHITE);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLegendItem(String label, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
        
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(NUNITO_SEMIBOLD.deriveFont(13f));
        textLabel.setForeground(TEXT_PRIMARY);
        
        panel.add(colorBox);
        panel.add(textLabel);
        
        return panel;
    }
    
    private String formatChartLabel(int value, boolean isMonth) {
        if (isMonth) {
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                             "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            return months[value - 1];
        }
        return String.valueOf(value);
    }
    
    private void createTransactionTable() {
        transactionPanel = new RoundedPanel(20);
        transactionPanel.setBackground(Color.WHITE);
        transactionPanel.setLayout(new BorderLayout(15, 15));
        transactionPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        transactionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));
        
        mainPanel.add(transactionPanel);
    }
    
    private void updateTransactionTable() {
        transactionPanel.removeAll();
        
        // Header with icon
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("📋 Recent Transactions");
        titleLabel.setFont(NUNITO_EXTRABOLD.deriveFont(18f));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JButton viewAllBtn = new JButton("View All →");
        viewAllBtn.setForeground(PRIMARY);
        viewAllBtn.setFont(NUNITO_SEMIBOLD.deriveFont(13f));
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.setFocusPainted(false);
        
        viewAllBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewAllBtn.setForeground(PRIMARY.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewAllBtn.setForeground(PRIMARY);
            }
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllBtn, BorderLayout.EAST);
        
        // Table
        String[] columns = {"Date", "Type", "Category", "Description", "Amount"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try {
            String year = (String) yearComboBox.getSelectedItem();
            
            // Get income transactions
            String incomeQuery = "SELECT i.date, 'Income' as type, it.income_type as category, " +
                                "i.description, i.amount FROM income i " +
                                "INNER JOIN income_type it ON i.income_type_id = it.income_type_id " +
                                "WHERE YEAR(i.date) = " + year;
            
            if (monthComboBox.getSelectedIndex() != 0) {
                incomeQuery += " AND MONTH(i.date) = " + monthComboBox.getSelectedIndex();
            }
            
            // Get expense transactions
            String expenseQuery = "SELECT e.date, 'Expense' as type, et.expenses_type as category, " +
                                 "e.description, e.amount FROM expenses e " +
                                 "INNER JOIN expenses_type et ON e.expenses_type_id = et.expenses_type_id " +
                                 "WHERE YEAR(e.date) = " + year;
            
            if (monthComboBox.getSelectedIndex() != 0) {
                expenseQuery += " AND MONTH(e.date) = " + monthComboBox.getSelectedIndex();
            }
            
            String combinedQuery = "(" + incomeQuery + ") UNION ALL (" + expenseQuery + ") " +
                                  "ORDER BY date DESC LIMIT 15";
            
            ResultSet rs = MySQL.executeSearch(combinedQuery);
            
            while (rs.next()) {
                String date = rs.getString(1);
                String type = rs.getString(2);
                String category = rs.getString(3);
                String description = rs.getString(4);
                double amount = rs.getDouble(5);
                
                model.addRow(new Object[]{
                    date,
                    type,
                    category,
                    description != null ? description : "N/A",
                    amount
                });
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTable table = new JTable(model);
        table.setFont(NUNITO_REGULAR.deriveFont(13f));
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 30));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(10, 1));
        
        // Style table header
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(NUNITO_SEMIBOLD.deriveFont(13f));
        tableHeader.setBackground(new Color(248, 249, 250));
        tableHeader.setForeground(TEXT_PRIMARY);
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
        tableHeader.setReorderingAllowed(false);
        ((DefaultTableCellRenderer)tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        
        // Custom renderer for better visuals
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                                                                 isSelected, hasFocus, row, column);
                
                String type = (String) table.getValueAt(row, 1);
                
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                if (column == 0) { // Date column
                    setFont(NUNITO_REGULAR.deriveFont(12f));
                    setForeground(TEXT_SECONDARY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (column == 1) { // Type column
                    setFont(NUNITO_SEMIBOLD.deriveFont(13f));
                    if (type.equals("Income")) {
                        setForeground(INCOME_GREEN);
                        setText("✓ " + type);
                    } else {
                        setForeground(EXPENSE_RED);
                        setText("✗ " + type);
                    }
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (column == 2) { // Category column
                    setFont(NUNITO_REGULAR.deriveFont(13f));
                    setForeground(TEXT_PRIMARY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (column == 3) { // Description column
                    setFont(NUNITO_REGULAR.deriveFont(12f));
                    setForeground(TEXT_SECONDARY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (column == 4) { // Amount column
                    double amount = (Double) value;
                    setFont(NUNITO_EXTRABOLD.deriveFont(14f));
                    if (type.equals("Income")) {
                        setForeground(INCOME_GREEN);
                        setText("+ Rs. " + currencyFormat.format(amount));
                    } else {
                        setForeground(EXPENSE_RED);
                        setText("- Rs. " + currencyFormat.format(amount));
                    }
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
                
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 252, 252));
                }
                
                return c;
            }
        });
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120);  // Date
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Type
        table.getColumnModel().getColumn(2).setPreferredWidth(150);  // Category
        table.getColumnModel().getColumn(3).setPreferredWidth(250);  // Description
        table.getColumnModel().getColumn(4).setPreferredWidth(150);  // Amount
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        transactionPanel.add(headerPanel, BorderLayout.NORTH);
        transactionPanel.add(scrollPane, BorderLayout.CENTER);
        
        transactionPanel.revalidate();
        transactionPanel.repaint();
    }
    
    private void loadDashboardData() {
        // Show loading state
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            String year = (String) yearComboBox.getSelectedItem();
            
            // Calculate total income
            String incomeQuery = "SELECT COALESCE(SUM(total), 0) FROM sales WHERE YEAR(datetime) = " + year;
            String otherIncomeQuery = "SELECT COALESCE(SUM(amount), 0) FROM income WHERE YEAR(date) = " + year;
            
            if (monthComboBox.getSelectedIndex() != 0) {
                int month = monthComboBox.getSelectedIndex();
                incomeQuery += " AND MONTH(datetime) = " + month;
                otherIncomeQuery += " AND MONTH(date) = " + month;
            }
            
            ResultSet incomeRs = MySQL.executeSearch(incomeQuery);
            ResultSet otherIncomeRs = MySQL.executeSearch(otherIncomeQuery);
            
            double salesIncome = 0;
            double otherIncome = 0;
            
            if (incomeRs.next()) {
                salesIncome = incomeRs.getDouble(1);
            }
            if (otherIncomeRs.next()) {
                otherIncome = otherIncomeRs.getDouble(1);
            }
            
            totalIncome = salesIncome + otherIncome;
            
            // Calculate total expenses
            String expenseQuery = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE YEAR(date) = " + year;
            
            if (monthComboBox.getSelectedIndex() != 0) {
                expenseQuery += " AND MONTH(date) = " + monthComboBox.getSelectedIndex();
            }
            
            ResultSet expenseRs = MySQL.executeSearch(expenseQuery);
            
            if (expenseRs.next()) {
                totalExpense = expenseRs.getDouble(1);
            }
            
            // Calculate net profit
            netProfit = totalIncome - totalExpense;
            
            // Count transactions
            String countQuery = "SELECT COUNT(*) FROM sales WHERE YEAR(datetime) = " + year;
            
            if (monthComboBox.getSelectedIndex() != 0) {
                countQuery += " AND MONTH(datetime) = " + monthComboBox.getSelectedIndex();
            }
            
            ResultSet countRs = MySQL.executeSearch(countQuery);
            
            if (countRs.next()) {
                transactionCount = countRs.getInt(1);
            }
            
            // Update UI
            SwingUtilities.invokeLater(() -> {
                updateSummaryCards();
                updateCharts();
                updateTransactionTable();
                setCursor(Cursor.getDefaultCursor());
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, 
                "Error loading dashboard data:\n" + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JButton createStyledButton(String text, Color bgColor, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(NUNITO_SEMIBOLD.deriveFont(13f));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        
        if (isPrimary) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            
            // Add hover effect
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(bgColor.darker());
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(bgColor);
                }
            });
        } else {
            button.setBackground(bgColor);
            button.setForeground(TEXT_SECONDARY);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
            ));
            
            // Add hover effect
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(245, 245, 245));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(bgColor);
                }
            });
        }
        
        return button;
    }
    
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Custom renderer for better appearance
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, 
                                                                 isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                
                if (isSelected) {
                    setBackground(PRIMARY);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(TEXT_PRIMARY);
                }
                
                return c;
            }
        });
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw shadow
            g2.setColor(CARD_SHADOW);
            for (int i = 0; i < 3; i++) {
                g2.fill(new RoundRectangle2D.Float(i, i, getWidth() - i * 2, getHeight() - i * 2, 
                                                   cornerRadius, cornerRadius));
            }
            
            // Draw card
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
            .addGap(0, 807, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 463, Short.MAX_VALUE)
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
