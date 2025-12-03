package lk.com.pos.panel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import lk.com.pos.dto.ChartDataDTO;
import lk.com.pos.dto.DashboardDataDTO;
import lk.com.pos.dto.ExpenseCategoryDTO;
import lk.com.pos.dto.FinancialSummaryDTO;
import lk.com.pos.dto.TransactionDTO;
import lk.com.pos.service.FinancialDashboardService;
import lk.com.pos.service.FinancialDashboardServiceImpl;

/**
 * Modern Financial Dashboard Panel for Pharmacy POS System
 * Using DAO & DTO Pattern with Real Database Connection
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
    
    // Service Layer
    private final FinancialDashboardService dashboardService;
    
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
    private FinancialSummaryDTO currentSummary;
    private DashboardDataDTO dashboardData;
    
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
    
    public ChartPanel() {
        this.dashboardService = new FinancialDashboardServiceImpl();
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
        
        // Year selector - Load available years from database
        yearComboBox = new JComboBox<>();
        loadAvailableYears();
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
        exportBtn.addActionListener(e -> showExportOptions());
        
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
    
    private void loadAvailableYears() {
    try {
        List<Integer> years = dashboardService.getAvailableYears();
        yearComboBox.removeAllItems(); // Clear existing items
        for (Integer year : years) {
            yearComboBox.addItem(String.valueOf(year));
        }
        // Set current year as default
        int currentYear = LocalDate.now().getYear();
        yearComboBox.setSelectedItem(String.valueOf(currentYear));
    } catch (Exception e) {
        e.printStackTrace();
        // Fallback to default years
        yearComboBox.removeAllItems(); // Clear existing items
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear; i >= currentYear - 5; i--) {
            yearComboBox.addItem(String.valueOf(i));
        }
    }
}
    
    private void showExportOptions() {
        JDialog exportDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Export Options", true);
        exportDialog.setLayout(new BorderLayout(10, 10));
        exportDialog.setSize(400, 300);
        exportDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("📤 Export Dashboard Data", SwingConstants.CENTER);
        titleLabel.setFont(NUNITO_EXTRABOLD.deriveFont(18f));
        
        JButton csvBtn = createStyledButton("📊 Export to CSV", PRIMARY, true);
        csvBtn.addActionListener(e -> {
            try {
                int year = Integer.parseInt((String) yearComboBox.getSelectedItem());
                int month = monthComboBox.getSelectedIndex();
                
                String csvData = dashboardService.exportToCSV(year, month);
                
                // Save to file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new java.io.File(
                    String.format("dashboard_export_%d_%d.csv", year, month)
                ));
                
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    java.io.File file = fileChooser.getSelectedFile();
                    try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                        writer.write(csvData);
                        JOptionPane.showMessageDialog(this, 
                            "✅ Export successful!\nFile saved: " + file.getName(), 
                            "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                
                exportDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Export failed: " + ex.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton pdfBtn = createStyledButton("📄 Export to PDF", PRIMARY_LIGHT, true);
        pdfBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "PDF export functionality coming soon!\nCurrently supports CSV export only.", 
                "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton printBtn = createStyledButton("🖨️ Print Dashboard", WARNING_ORANGE, true);
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Print functionality coming soon!", 
                "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
        });
        
        contentPanel.add(titleLabel);
        contentPanel.add(csvBtn);
        contentPanel.add(pdfBtn);
        contentPanel.add(printBtn);
        
        exportDialog.add(contentPanel, BorderLayout.CENTER);
        exportDialog.setVisible(true);
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
        if (currentSummary == null) return;
        
        summaryCardsPanel.removeAll();
        
        summaryCardsPanel.add(createSummaryCard(
            "Total Income", 
            currentSummary.getFormattedTotalIncome(), 
            "From sales & other sources",
            String.format("%+.1f%%", currentSummary.getIncomeChangePercent()),
            PRIMARY,
            INCOME_GREEN,
            "💵"
        ));
        
        summaryCardsPanel.add(createSummaryCard(
            "Total Expenses", 
            currentSummary.getFormattedTotalExpense(), 
            "Operating costs",
            String.format("%+.1f%%", currentSummary.getExpenseChangePercent()),
            EXPENSE_RED,
            EXPENSE_RED,
            "📊"
        ));
        
        summaryCardsPanel.add(createSummaryCard(
            "Net Profit", 
            currentSummary.getFormattedNetProfit(), 
            currentSummary.isProfitable() ? "Profitable period" : "Loss period",
            String.format("%+.1f%%", currentSummary.getProfitChangePercent()),
            PRIMARY_LIGHTER,
            currentSummary.isProfitable() ? INCOME_GREEN : EXPENSE_RED,
            "💎"
        ));
        
        summaryCardsPanel.add(createSummaryCard(
            "Transactions", 
            currentSummary.getFormattedTransactionCount(), 
            "Total sales count",
            currentSummary.getFormattedTransactionCount() + " sales",
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
        if (dashboardData == null) return;
        
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
            List<ChartDataDTO> chartData = dashboardData.getIncomeExpenseTrend();
            
            for (ChartDataDTO data : chartData) {
                dataset.addValue(data.getIncome(), "Income", data.getPeriod());
                dataset.addValue(data.getExpense(), "Expense", data.getPeriod());
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
            List<ExpenseCategoryDTO> expenseBreakdown = dashboardData.getExpenseBreakdown();
            
            for (ExpenseCategoryDTO category : expenseBreakdown) {
                dataset.setValue(category.getCategoryName() + " (" + category.getFormattedPercentage() + ")", 
                               category.getAmount());
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
    
    private void createTransactionTable() {
        transactionPanel = new RoundedPanel(20);
        transactionPanel.setBackground(Color.WHITE);
        transactionPanel.setLayout(new BorderLayout(15, 15));
        transactionPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        transactionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));
        
        mainPanel.add(transactionPanel);
    }
    
    private void updateTransactionTable() {
        if (dashboardData == null) return;
        
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
        
        viewAllBtn.addActionListener(e -> showAllTransactionsDialog());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllBtn, BorderLayout.EAST);
        
        // Table
        String[] columns = {"Date", "Type", "Category", "Description", "Amount", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try {
            List<TransactionDTO> transactions = dashboardData.getRecentTransactions();
            
            for (TransactionDTO transaction : transactions) {
                model.addRow(new Object[]{
                    transaction.getFormattedDate(),
                    transaction.getType(),
                    transaction.getCategory(),
                    transaction.getDescription() != null ? transaction.getDescription() : "N/A",
                    transaction.getAmount(),
                    transaction.getStatus()
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
                } else if (column == 5) { // Status column
                    setFont(NUNITO_REGULAR.deriveFont(12f));
                    String status = (String) value;
                    if ("Completed".equals(status) || "Approved".equals(status)) {
                        setForeground(INCOME_GREEN);
                    } else if ("Pending".equals(status)) {
                        setForeground(WARNING_ORANGE);
                    } else {
                        setForeground(EXPENSE_RED);
                    }
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 252, 252));
                }
                
                return c;
            }
        });
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);  // Date
        table.getColumnModel().getColumn(1).setPreferredWidth(90);   // Type
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Category
        table.getColumnModel().getColumn(3).setPreferredWidth(200);  // Description
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // Amount
        table.getColumnModel().getColumn(5).setPreferredWidth(90);   // Status
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        transactionPanel.add(headerPanel, BorderLayout.NORTH);
        transactionPanel.add(scrollPane, BorderLayout.CENTER);
        
        transactionPanel.revalidate();
        transactionPanel.repaint();
    }
    
    private void showAllTransactionsDialog() {
        try {
            int year = Integer.parseInt((String) yearComboBox.getSelectedItem());
            int month = monthComboBox.getSelectedIndex();
            
            List<TransactionDTO> allTransactions = dashboardService.getRecentTransactions(year, month, 100);
            
            JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), 
                                        "All Transactions", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);
            
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            
            JLabel titleLabel = new JLabel("📋 All Transactions (" + allTransactions.size() + ")");
            titleLabel.setFont(NUNITO_EXTRABOLD.deriveFont(16f));
            
            JButton exportBtn = new JButton("📥 Export");
            exportBtn.setFont(NUNITO_SEMIBOLD.deriveFont(12f));
            exportBtn.addActionListener(e -> exportTransactionsToCSV(allTransactions));
            
            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(exportBtn, BorderLayout.EAST);
            
            // Table
            String[] columns = {"Date", "Type", "Category", "Description", "Amount", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            for (TransactionDTO transaction : allTransactions) {
                model.addRow(new Object[]{
                    transaction.getFormattedDate(),
                    transaction.getType(),
                    transaction.getCategory(),
                    transaction.getDescription(),
                    transaction.getAmount(),
                    transaction.getStatus()
                });
            }
            
            JTable table = new JTable(model);
            table.setFont(NUNITO_REGULAR.deriveFont(12f));
            table.setRowHeight(40);
            
            JScrollPane scrollPane = new JScrollPane(table);
            
            dialog.add(headerPanel, BorderLayout.NORTH);
            dialog.add(scrollPane, BorderLayout.CENTER);
            
            dialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading transactions: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportTransactionsToCSV(java.util.List<TransactionDTO> transactions) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Date,Type,Category,Description,Amount,Status\n");
            
            for (TransactionDTO transaction : transactions) {
                csv.append(transaction.getFormattedDate()).append(",")
                   .append(transaction.getType()).append(",")
                   .append(transaction.getCategory()).append(",")
                   .append(transaction.getDescription()).append(",")
                   .append(transaction.getAmount()).append(",")
                   .append(transaction.getStatus()).append("\n");
            }
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("transactions_export.csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write(csv.toString());
                    JOptionPane.showMessageDialog(this, 
                        "✅ Export successful!", 
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Export failed: " + e.getMessage(), 
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadDashboardData() {
        // Show loading state
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<DashboardDataDTO, Void> worker = new SwingWorker<DashboardDataDTO, Void>() {
            @Override
            protected DashboardDataDTO doInBackground() throws Exception {
                int year = Integer.parseInt((String) yearComboBox.getSelectedItem());
                int month = monthComboBox.getSelectedIndex();
                
                return dashboardService.getCompleteDashboardData(year, month);
            }
            
            @Override
            protected void done() {
                try {
                    dashboardData = get();
                    currentSummary = dashboardData.getSummary();
                    
                    // Update UI on EDT
                    SwingUtilities.invokeLater(() -> {
                        updateSummaryCards();
                        updateCharts();
                        updateTransactionTable();
                        setCursor(Cursor.getDefaultCursor());
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(ChartPanel.this, 
                        "Error loading dashboard data:\n" + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
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
