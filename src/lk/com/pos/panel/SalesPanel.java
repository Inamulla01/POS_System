package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import lk.com.pos.connection.DB;
import lk.com.pos.dto.InvoiceDTO;
import lk.com.pos.dto.SaleItemDTO;
import lk.com.pos.dao.SalesDAO;
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
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SalesPanel extends javax.swing.JPanel {

    private JPanel invoicesContainer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
    private int currentWidth = 0;
    private Timer searchTimer;

    // Keyboard navigation
    private List<JPanel> invoiceCardsList = new ArrayList<>();
    private int currentCardIndex = -1;
    private JPanel currentFocusedCard = null;

    private JPanel positionIndicator;
    private JLabel positionLabel;
    private Timer positionTimer;

    private JPanel keyboardHintsPanel;
    private boolean hintsVisible = false;

    // Refresh cooldown
    private long lastRefreshTime = 0;
    private static final long REFRESH_COOLDOWN = 1000; // 1 second

    // Colors
    private static final Color TEAL_BORDER_SELECTED = new Color(28, 181, 187);
    private static final Color TEAL_BORDER_HOVER = new Color(60, 200, 206);
    private static final Color DEFAULT_BORDER = new Color(230, 230, 230);

    // DAO instance
    private SalesDAO salesDAO = new SalesDAO();

    // Export constants
    private static final SimpleDateFormat EXPORT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final SimpleDateFormat FILENAME_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public SalesPanel() {
        initComponents();
        setupPanel();
        customizeComponents();
        createPositionIndicator();
        createKeyboardHintsPanel();
        setupKeyboardShortcuts();

        loadSalesData("", "All Time");
        setupEventListeners();

        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
            showKeyboardHints();
        });
    }

    private void createPositionIndicator() {
        positionIndicator = new JPanel();
        positionIndicator.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        positionIndicator.setBackground(new Color(31, 41, 55, 230));
        positionIndicator.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_BORDER_SELECTED, 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        positionIndicator.setVisible(false);

        positionLabel = new JLabel();
        positionLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        positionLabel.setForeground(Color.WHITE);

        positionIndicator.add(positionLabel);

        setLayout(new OverlayLayout(this) {
            @Override
            public void layoutContainer(Container target) {
                super.layoutContainer(target);

                if (positionIndicator.isVisible()) {
                    Dimension size = positionIndicator.getPreferredSize();
                    int x = (getWidth() - size.width) / 2;
                    int y = 80;
                    positionIndicator.setBounds(x, y, size.width, size.height);
                }

                if (keyboardHintsPanel != null && keyboardHintsPanel.isVisible()) {
                    Dimension size = keyboardHintsPanel.getPreferredSize();
                    int x = getWidth() - size.width - 20;
                    int y = getHeight() - size.height - 20;
                    keyboardHintsPanel.setBounds(x, y, size.width, size.height);
                }
            }
        });

        add(positionIndicator, Integer.valueOf(1000));
    }

    private void createKeyboardHintsPanel() {
        keyboardHintsPanel = new JPanel();
        keyboardHintsPanel.setLayout(new BoxLayout(keyboardHintsPanel, BoxLayout.Y_AXIS));
        keyboardHintsPanel.setBackground(new Color(31, 41, 55, 240));
        keyboardHintsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_BORDER_SELECTED, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);

        JLabel title = new JLabel("KEYBOARD SHORTCUTS");
        title.setFont(new Font("Nunito ExtraBold", Font.BOLD, 13));
        title.setForeground(TEAL_BORDER_SELECTED);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);

        keyboardHintsPanel.add(Box.createVerticalStrut(10));

        addHintRow("↑ ↓", "Navigate invoices", "#FFFFFF");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("F5", "Refresh", "#34D399");
        addHintRow("Ctrl+P", "Export Options", "#10B981");
        addHintRow("Ctrl+R", "Export Options", "#10B981");
        addHintRow("Alt+1", "All Time", "#FB923C");
        addHintRow("Alt+2", "Today", "#FCD34D");
        addHintRow("Alt+3", "Last 7 Days", "#1CB5BB");
        addHintRow("Alt+4", "Last 30 Days", "#F87171");
        addHintRow("Alt+5", "Last 90 Days", "#A78BFA");
        addHintRow("Alt+6", "1 Year", "#34D399");
        addHintRow("Alt+7", "2 Years", "#60A5FA");
        addHintRow("Alt+8", "5 Years", "#F472B6");
        addHintRow("Alt+9", "10 Years", "#FBBF24");
        addHintRow("Alt+0", "20 Years", "#DC2626");
        addHintRow("Esc", "Clear All Filters", "#EF4444");
        addHintRow("?", "Toggle Help", "#1CB5BB");

        keyboardHintsPanel.add(Box.createVerticalStrut(10));

        JLabel closeHint = new JLabel("Press ? to hide");
        closeHint.setFont(new Font("Nunito SemiBold", Font.ITALIC, 10));
        closeHint.setForeground(Color.decode("#9CA3AF"));
        closeHint.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        keyboardHintsPanel.add(closeHint);

        add(keyboardHintsPanel, Integer.valueOf(1001));
    }

    private void addHintRow(String key, String description, String keyColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(280, 25));

        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(new Font("Consolas", Font.BOLD, 11));
        keyLabel.setForeground(Color.decode(keyColor));
        keyLabel.setPreferredSize(new Dimension(80, 20));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 11));
        descLabel.setForeground(Color.decode("#D1D5DB"));

        row.add(keyLabel);
        row.add(descLabel);
        keyboardHintsPanel.add(row);
    }

    private void showKeyboardHints() {
        if (!hintsVisible) {
            keyboardHintsPanel.setVisible(true);
            hintsVisible = true;
            revalidate();
            repaint();

            Timer hideTimer = new Timer(5000, e -> {
                keyboardHintsPanel.setVisible(false);
                hintsVisible = false;
                revalidate();
                repaint();
            });
            hideTimer.setRepeats(false);
            hideTimer.start();
        } else {
            keyboardHintsPanel.setVisible(false);
            hintsVisible = false;
            revalidate();
            repaint();
        }
    }

    private void showPositionIndicator(String text) {
        positionLabel.setText(text);
        positionIndicator.setVisible(true);
        revalidate();
        repaint();

        if (positionTimer != null && positionTimer.isRunning()) {
            positionTimer.stop();
        }

        positionTimer = new Timer(2000, e -> {
            positionIndicator.setVisible(false);
            revalidate();
            repaint();
        });
        positionTimer.setRepeats(false);
        positionTimer.start();
    }

    private void setupKeyboardShortcuts() {
        this.setFocusable(true);

        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;

        // Navigation
        registerKeyAction("UP", KeyEvent.VK_UP, 0, condition, () -> navigateCards(-1));
        registerKeyAction("DOWN", KeyEvent.VK_DOWN, 0, condition, () -> navigateCards(1));
        registerKeyAction("HOME", KeyEvent.VK_HOME, 0, condition, () -> navigateToFirst());
        registerKeyAction("END", KeyEvent.VK_END, 0, condition, () -> navigateToLast());

        // Search
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, () -> focusSearch());
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, () -> {
            if (!jTextField1.hasFocus()) {
                focusSearch();
            }
        });

        // Refresh
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, () -> refreshSales());

        // Export Options - UPDATED to show export options
        registerKeyAction("CTRL_P", KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, condition, this::showExportOptions);
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::showExportOptions);

        // Period filters (Extended with 20 Years)
        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(0));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(1));
        registerKeyAction("ALT_3", KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(2));
        registerKeyAction("ALT_4", KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(3));
        registerKeyAction("ALT_5", KeyEvent.VK_5, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(4));
        registerKeyAction("ALT_6", KeyEvent.VK_6, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(5));
        registerKeyAction("ALT_7", KeyEvent.VK_7, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(6));
        registerKeyAction("ALT_8", KeyEvent.VK_8, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(7));
        registerKeyAction("ALT_9", KeyEvent.VK_9, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(8));
        registerKeyAction("ALT_0", KeyEvent.VK_0, KeyEvent.ALT_DOWN_MASK, condition, () -> setPeriod(9));

        // Escape - UPDATED TO CLEAR ALL FILTERS
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, () -> handleEscape());

        // Help
        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, condition, () -> showKeyboardHints());

        // Search field
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        jTextField1.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllFilters();
            }
        });

        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "startNavigation");
        jTextField1.getActionMap().put("startNavigation", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!invoiceCardsList.isEmpty()) {
                    SalesPanel.this.requestFocusInWindow();
                    if (currentCardIndex == -1) {
                        currentCardIndex = 0;
                        selectCurrentCard();
                        scrollToCard(currentCardIndex);
                    }
                }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> SalesPanel.this.requestFocusInWindow());
            }
        });
    }

    private void registerKeyAction(String actionName, int keyCode, int modifiers, int condition, Runnable action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        this.getInputMap(condition).put(keyStroke, actionName);
        this.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextField1.hasFocus()
                        && keyCode != KeyEvent.VK_ESCAPE
                        && modifiers == 0
                        && keyCode != KeyEvent.VK_SLASH) {
                    return;
                }
                action.run();
            }
        });
    }

    private void navigateCards(int direction) {
        if (invoiceCardsList.isEmpty()) {
            showPositionIndicator("No invoices available");
            return;
        }

        if (currentCardIndex < 0) {
            currentCardIndex = 0;
            selectCurrentCard();
            scrollToCard(currentCardIndex);
            return;
        }

        int newIndex = currentCardIndex + direction;

        if (newIndex < 0) {
            showPositionIndicator("Already at the first invoice");
            return;
        }

        if (newIndex >= invoiceCardsList.size()) {
            showPositionIndicator("Already at the last invoice");
            return;
        }

        deselectCard(currentCardIndex);
        currentCardIndex = newIndex;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
    }

    private void navigateToFirst() {
        if (invoiceCardsList.isEmpty()) {
            showPositionIndicator("No invoices available");
            return;
        }

        if (currentCardIndex >= 0) {
            deselectCard(currentCardIndex);
        }
        currentCardIndex = 0;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
        showPositionIndicator("First invoice");
    }

    private void navigateToLast() {
        if (invoiceCardsList.isEmpty()) {
            showPositionIndicator("No invoices available");
            return;
        }

        if (currentCardIndex >= 0) {
            deselectCard(currentCardIndex);
        }
        currentCardIndex = invoiceCardsList.size() - 1;
        selectCurrentCard();
        scrollToCard(currentCardIndex);
        showPositionIndicator("⬇️ Last invoice");
    }

    private void selectCurrentCard() {
        if (currentCardIndex >= 0 && currentCardIndex < invoiceCardsList.size()) {
            JPanel card = invoiceCardsList.get(currentCardIndex);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(TEAL_BORDER_SELECTED, 3, 20),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            currentFocusedCard = card;

            String invoiceNo = (String) card.getClientProperty("invoiceNo");
            showPositionIndicator(String.format("Invoice %d/%d: %s",
                    currentCardIndex + 1, invoiceCardsList.size(), invoiceNo));
        }
    }

    private void deselectCard(int index) {
        if (index >= 0 && index < invoiceCardsList.size()) {
            JPanel card = invoiceCardsList.get(index);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new ShadowBorder(),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        }
    }

    private void scrollToCard(int index) {
        if (index < 0 || index >= invoiceCardsList.size()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                JPanel card = invoiceCardsList.get(index);
                Rectangle bounds = card.getBounds();
                Rectangle visible = jScrollPane1.getViewport().getViewRect();

                int targetY = bounds.y - 20;

                jScrollPane1.getViewport().setViewPosition(new Point(0, Math.max(0, targetY)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void focusSearch() {
        jTextField1.requestFocus();
        jTextField1.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter (Press ↓ to navigate)");
    }

    private void handleEscape() {
        if (currentCardIndex >= 0) {
            // Deselect invoice card
            deselectCard(currentCardIndex);
            currentFocusedCard = null;
            currentCardIndex = -1;
            showPositionIndicator("Card deselected");
        } else if (!jTextField1.getText().isEmpty() || sortByDays.getSelectedIndex() != 0) {
            // Clear all filters
            clearAllFilters();
        }
        this.requestFocusInWindow();
    }

    private void clearAllFilters() {
        boolean wasFiltered = !jTextField1.getText().isEmpty() || sortByDays.getSelectedIndex() != 0;

        jTextField1.setText("");
        sortByDays.setSelectedIndex(0); // Reset to "All Time"

        if (wasFiltered) {
            handleSearch();
            showPositionIndicator("All filters cleared - Showing all invoices");
        }

        SalesPanel.this.requestFocusInWindow();
    }

    private void refreshSales() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < REFRESH_COOLDOWN) {
            showPositionIndicator("Please wait before refreshing again");
            return;
        }

        lastRefreshTime = currentTime;
        handleSearch();
        showPositionIndicator("Sales data refreshed");
        this.requestFocusInWindow();
    }

    private void showExportOptions() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Export Options");
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton reportButton = new JButton("Generate Report");
        reportButton.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        reportButton.setBackground(new Color(28, 181, 187));
        reportButton.setForeground(Color.WHITE);
        reportButton.addActionListener(e -> {
            dialog.dispose();
            generateSalesReport();
        });

        JButton excelButton = new JButton("Export to Excel");
        excelButton.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        excelButton.setBackground(new Color(16, 185, 129));
        excelButton.setForeground(Color.WHITE);
        excelButton.addActionListener(e -> {
            dialog.dispose();
            exportSalesToExcel();
        });

        buttonPanel.add(reportButton);
        buttonPanel.add(excelButton);

        JLabel titleLabel = new JLabel("Select Export Format", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        dialog.add(titleLabel, BorderLayout.NORTH);
        dialog.add(buttonPanel, BorderLayout.CENTER);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showPositionIndicator("Export cancelled");
            }
        });

        dialog.setVisible(true);
        this.requestFocusInWindow();
    }

    private void generateSalesReport() {
        // Option to generate JasperReport if needed
        // For now, show a message and offer Excel export
        int choice = JOptionPane.showConfirmDialog(this,
                "Generate detailed sales report?\n"
                + "Note: This will create a printable report format.\n\n"
                + "Click 'Yes' for report, 'No' for Excel export.",
                "Generate Report",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            showPositionIndicator("⚠️ Report generation feature not yet implemented");
            // You can implement JasperReports here if needed
        } else {
            exportSalesToExcel();
        }
    }

    private void exportSalesToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("sales_report_" + FILENAME_DATE_FORMAT.format(new Date()) + ".xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            showPositionIndicator("Export cancelled");
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        // Ensure .xlsx extension
        if (!selectedFile.getAbsolutePath().endsWith(".xlsx")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".xlsx");
        }

        // Create final reference for use in SwingWorker
        final File finalFileToSave = selectedFile;

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Preparing export...");

                try {
                    // Get current filters
                    String searchText = jTextField1.getText().trim();
                    String selectedPeriod = sortByDays.getSelectedItem().toString();

                    // Fetch sales data
                    publish("Fetching sales data...");
                    List<InvoiceDTO> invoices = salesDAO.fetchInvoicesFromDatabase(searchText, selectedPeriod);

                    if (invoices.isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(SalesPanel.this,
                                    "No sales data found for the selected filters.",
                                    "No Data",
                                    JOptionPane.WARNING_MESSAGE);
                        });
                        return null;
                    }

                    publish("Creating Excel file...");
                    // Create Excel workbook
                    Workbook workbook = new XSSFWorkbook();

                    // ============================================
                    // Sheet 1: Sales Summary
                    // ============================================
                    publish("Creating summary sheet...");
                    Sheet summarySheet = workbook.createSheet("Sales Summary");

                    // Create styles
                    CellStyle headerStyle = createHeaderStyle(workbook);
                    CellStyle titleStyle = createTitleStyle(workbook);
                    CellStyle dataStyle = createDataStyle(workbook);
                    CellStyle currencyStyle = createCurrencyStyle(workbook);
                    CellStyle summaryStyle = createSummaryStyle(workbook);
                    CellStyle dateStyle = createDateStyle(workbook);

                    // Title row
                    Row titleRow = summarySheet.createRow(0);
                    Cell titleCell = titleRow.createCell(0);
                    titleCell.setCellValue("SALES REPORT");
                    titleCell.setCellStyle(titleStyle);
                    summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

                    // Report info
                    Row infoRow = summarySheet.createRow(1);
                    Cell infoCell = infoRow.createCell(0);
                    infoCell.setCellValue("Generated: " + EXPORT_DATE_FORMAT.format(new Date()));
                    infoCell.setCellStyle(dataStyle);
                    summarySheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

                    // Filter info
                    Row filterRow = summarySheet.createRow(2);
                    String filterInfo = buildFilterInfo(searchText, selectedPeriod);
                    Cell filterCell = filterRow.createCell(0);
                    filterCell.setCellValue(filterInfo);
                    filterCell.setCellStyle(dataStyle);
                    summarySheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));

                    // Empty row
                    summarySheet.createRow(3);

                    // Summary header
                    Row summaryHeaderRow = summarySheet.createRow(4);
                    String[] summaryHeaders = {"Metric", "Value", "Details"};
                    for (int i = 0; i < summaryHeaders.length; i++) {
                        Cell cell = summaryHeaderRow.createCell(i);
                        cell.setCellValue(summaryHeaders[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    // Calculate summary statistics
                    publish("Calculating statistics...");
                    Map<String, Object> summaryStats = calculateSummaryStats(invoices);

                    // Summary data
                    int summaryRowNum = 5;
                    summaryRowNum = addSummaryRow(summarySheet, summaryRowNum, "Total Invoices",
                            String.valueOf(summaryStats.get("totalInvoices")), "", summaryStyle, dataStyle);
                    summaryRowNum = addSummaryRow(summarySheet, summaryRowNum, "Total Sales Amount",
                            String.format("Rs. %.2f", summaryStats.get("totalSales")), "Gross sales before discounts", summaryStyle, currencyStyle);
                    summaryRowNum = addSummaryRow(summarySheet, summaryRowNum, "Total Discounts",
                            String.format("Rs. %.2f", summaryStats.get("totalDiscount")), "Item + Sale discounts", summaryStyle, currencyStyle);
                    summaryRowNum = addSummaryRow(summarySheet, summaryRowNum, "Net Sales",
                            String.format("Rs. %.2f", summaryStats.get("netSales")), "After all discounts", summaryStyle, currencyStyle);
                    summaryRowNum = addSummaryRow(summarySheet, summaryRowNum, "Average Sale Value",
                            String.format("Rs. %.2f", summaryStats.get("avgSale")), "Per invoice average", summaryStyle, currencyStyle);

                    // Payment methods summary
                    summaryRowNum++;
                    Row paymentHeaderRow = summarySheet.createRow(summaryRowNum++);
                    Cell paymentHeaderCell = paymentHeaderRow.createCell(0);
                    paymentHeaderCell.setCellValue("Payment Methods Breakdown");
                    paymentHeaderCell.setCellStyle(headerStyle);
                    summarySheet.addMergedRegion(new CellRangeAddress(paymentHeaderRow.getRowNum(), paymentHeaderRow.getRowNum(), 0, 2));

                    Map<String, Integer> paymentMethods = (Map<String, Integer>) summaryStats.get("paymentMethods");
                    for (Map.Entry<String, Integer> entry : paymentMethods.entrySet()) {
                        summaryRowNum = addSummaryRow(summarySheet, summaryRowNum, entry.getKey(),
                                entry.getValue() + " invoices", "", dataStyle, dataStyle);
                    }

                    // Auto-size columns
                    for (int i = 0; i < 3; i++) {
                        summarySheet.autoSizeColumn(i);
                    }

                    // ============================================
                    // Sheet 2: Detailed Sales
                    // ============================================
                    publish("Creating detailed sales sheet...");
                    Sheet detailSheet = workbook.createSheet("Sales Details");

                    // Detailed header row
                    Row detailHeaderRow = detailSheet.createRow(0);
                    String[] detailHeaders = {
                        "Invoice No", "Date & Time", "Customer", "Payment Method", "Cashier",
                        "Status", "Total Amount", "Item Discount", "Sale Discount", "Total Discount", "Net Amount",
                        "Items Count"
                    };

                    for (int i = 0; i < detailHeaders.length; i++) {
                        Cell cell = detailHeaderRow.createCell(i);
                        cell.setCellValue(detailHeaders[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    // Fill data rows
                    int rowNum = 1;
                    for (InvoiceDTO invoice : invoices) {
                        publish("Processing invoice " + invoice.getInvoiceNo() + "...");
                        Row row = detailSheet.createRow(rowNum++);

                        // Invoice No
                        row.createCell(0).setCellValue(invoice.getInvoiceNo());

                        // Date & Time
                        Cell dateCell = row.createCell(1);
                        dateCell.setCellValue(invoice.getFormattedDateTime());
                        dateCell.setCellStyle(dateStyle);

                        // Customer
                        row.createCell(2).setCellValue(invoice.getCustomerName() != null ? invoice.getCustomerName() : "Walk-in Customer");

                        // Payment Method
                        row.createCell(3).setCellValue(invoice.getPaymentMethod());

                        // Cashier
                        row.createCell(4).setCellValue(invoice.getCashierName());

                        // Status
                        row.createCell(5).setCellValue(invoice.getSaleStatus());

                        // Total Amount
                        Cell totalCell = row.createCell(6);
                        totalCell.setCellValue(invoice.getTotal());
                        totalCell.setCellStyle(currencyStyle);

                        // Item Discount
                        Cell itemDiscCell = row.createCell(7);
                        itemDiscCell.setCellValue(invoice.getItemDiscount());
                        itemDiscCell.setCellStyle(currencyStyle);

                        // Sale Discount
                        Cell saleDiscCell = row.createCell(8);
                        saleDiscCell.setCellValue(invoice.getSaleDiscount());
                        saleDiscCell.setCellStyle(currencyStyle);

                        // Total Discount
                        Cell totalDiscCell = row.createCell(9);
                        totalDiscCell.setCellValue(invoice.getTotalDiscount());
                        totalDiscCell.setCellStyle(currencyStyle);

                        // Net Amount
                        Cell netCell = row.createCell(10);
                        netCell.setCellValue(invoice.getTotal() - invoice.getTotalDiscount());
                        netCell.setCellStyle(currencyStyle);

                        // Items Count (load items to get count)
                        List<SaleItemDTO> items = salesDAO.loadSaleItems(invoice.getSalesId());
                        row.createCell(11).setCellValue(items.size());
                    }

                    // Auto-size columns
                    for (int i = 0; i < detailHeaders.length; i++) {
                        detailSheet.autoSizeColumn(i);
                    }

                    // ============================================
                    // Sheet 3: Items Details
                    // ============================================
                    publish("Creating items details sheet...");
                    Sheet itemsSheet = workbook.createSheet("Items Details");

                    // Items header row
                    Row itemsHeaderRow = itemsSheet.createRow(0);
                    String[] itemHeaders = {
                        "Invoice No", "Product Name", "Quantity", "Unit Price", "Item Discount", "Total Discount",
                        "Net Price", "Line Total", "Batch No"
                    };

                    for (int i = 0; i < itemHeaders.length; i++) {
                        Cell cell = itemsHeaderRow.createCell(i);
                        cell.setCellValue(itemHeaders[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    // Fill items data
                    int itemRowNum = 1;
                    for (InvoiceDTO invoice : invoices) {
                        List<SaleItemDTO> items = salesDAO.loadSaleItems(invoice.getSalesId());

                        for (SaleItemDTO item : items) {
                            Row row = itemsSheet.createRow(itemRowNum++);

                            // Invoice No
                            row.createCell(0).setCellValue(invoice.getInvoiceNo());

                            // Product Name
                            row.createCell(1).setCellValue(item.getProductName());

                            // Quantity
                            row.createCell(2).setCellValue(item.getQty());

                            // Unit Price
                            Cell priceCell = row.createCell(3);
                            priceCell.setCellValue(item.getPrice());
                            priceCell.setCellStyle(currencyStyle);

                            // Item Discount
                            Cell discCell = row.createCell(4);
                            discCell.setCellValue(item.getDiscountPrice());
                            discCell.setCellStyle(currencyStyle);

                            // Total Discount (item discount * quantity)
                            Cell totalDiscCell = row.createCell(5);
                            totalDiscCell.setCellValue(item.getDiscountPrice() * item.getQty());
                            totalDiscCell.setCellStyle(currencyStyle);

                            // Net Price (unit price - item discount)
                            Cell netPriceCell = row.createCell(6);
                            netPriceCell.setCellValue(item.getPrice() - item.getDiscountPrice());
                            netPriceCell.setCellStyle(currencyStyle);

                            // Line Total
                            Cell lineTotalCell = row.createCell(7);
                            lineTotalCell.setCellValue(item.getTotal());
                            lineTotalCell.setCellStyle(currencyStyle);

                            // Batch No
                            row.createCell(8).setCellValue(item.getBatchNo() != null ? item.getBatchNo() : "");
                        }
                    }

                    // Auto-size columns for items sheet
                    for (int i = 0; i < itemHeaders.length; i++) {
                        itemsSheet.autoSizeColumn(i);
                    }

                    // Write the output to file
                    publish("Saving file...");
                    try (FileOutputStream outputStream = new FileOutputStream(finalFileToSave)) {
                        workbook.write(outputStream);
                    }

                    workbook.close();

                    publish("✅ Export completed successfully!");

                    // Show success message
                    SwingUtilities.invokeLater(() -> {
                        showPositionIndicator("✅ Excel file saved: " + finalFileToSave.getName());

                        int openFile = JOptionPane.showConfirmDialog(SalesPanel.this,
                                "Excel file saved successfully!\nFile: " + finalFileToSave.getName()
                                + "\n\nDo you want to open the file?",
                                "Export Successful",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);

                        if (openFile == JOptionPane.YES_OPTION) {
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().open(finalFileToSave);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(SalesPanel.this,
                                            "Cannot open file automatically. Please open it manually.",
                                            "Info",
                                            JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(SalesPanel.this,
                                "Error exporting to Excel: " + e.getMessage(),
                                "Export Error",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    showPositionIndicator(message);
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for any exceptions
                } catch (Exception e) {
                    // Already handled in doInBackground
                }
            }
        };

        worker.execute();
    }

    // Helper methods for Excel creation
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.TEAL.getIndex());
        style.setFont((org.apache.poi.ss.usermodel.Font) font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private String buildFilterInfo(String searchText, String selectedPeriod) {
        StringBuilder filter = new StringBuilder();
        filter.append("Period: ").append(selectedPeriod);

        if (!searchText.isEmpty()) {
            filter.append(" | Search: '").append(searchText).append("'");
        }

        return filter.toString();
    }

    private Map<String, Object> calculateSummaryStats(List<InvoiceDTO> invoices) {
        Map<String, Object> stats = new HashMap<>();

        int totalInvoices = invoices.size();
        double totalSales = 0;
        double totalDiscount = 0;
        Map<String, Integer> paymentMethods = new HashMap<>();

        for (InvoiceDTO invoice : invoices) {
            totalSales += invoice.getTotal();
            totalDiscount += invoice.getTotalDiscount();

            // Count payment methods
            String paymentMethod = invoice.getPaymentMethod();
            paymentMethods.put(paymentMethod, paymentMethods.getOrDefault(paymentMethod, 0) + 1);
        }

        double netSales = totalSales - totalDiscount;
        double avgSale = totalInvoices > 0 ? totalSales / totalInvoices : 0;

        stats.put("totalInvoices", totalInvoices);
        stats.put("totalSales", totalSales);
        stats.put("totalDiscount", totalDiscount);
        stats.put("netSales", netSales);
        stats.put("avgSale", avgSale);
        stats.put("paymentMethods", paymentMethods);

        return stats;
    }

    private int addSummaryRow(Sheet sheet, int rowNum, String label, String value, String details,
            CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);

        if (!details.isEmpty()) {
            Cell detailsCell = row.createCell(2);
            detailsCell.setCellValue(details);
            detailsCell.setCellStyle(valueStyle);
        }

        return rowNum + 1;
    }

    private void setPeriod(int index) {
        if (index >= 0 && index < sortByDays.getItemCount()) {
            sortByDays.setSelectedIndex(index);
            handlePeriodFilter();
            showPositionIndicator("Filter: " + sortByDays.getItemAt(index));
            this.requestFocusInWindow();
        }
    }

    private void setupEventListeners() {
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

        searchTimer = new Timer(300, e -> handleSearch());
        searchTimer.setRepeats(false);

        jTextField1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }
        });

        sortByDays.addActionListener(e -> {
            handlePeriodFilter();
            this.requestFocusInWindow();
        });
    }

    private void handleSearch() {
        String searchText = jTextField1.getText().trim();
        String selectedPeriod = sortByDays.getSelectedItem().toString();
        loadSalesData(searchText, selectedPeriod);
    }

    private void handlePeriodFilter() {
        String searchText = jTextField1.getText().trim();
        String selectedPeriod = sortByDays.getSelectedItem().toString();
        loadSalesData(searchText, selectedPeriod);
    }

    private void setupPanel() {
        jPanel2.setLayout(new BorderLayout());
        jPanel2.setBackground(new Color(248, 250, 252));
    }

    private void adjustLayoutForWidth(int width) {
        if (width < 700) {
            jTextField1.setPreferredSize(new Dimension(width - 40, 50));
            sortByDays.setPreferredSize(new Dimension(width - 40, 50));
        } else {
            int halfWidth = (width - 60) / 2;
            jTextField1.setPreferredSize(new Dimension(halfWidth, 50));
            sortByDays.setPreferredSize(new Dimension(halfWidth, 50));
        }
        revalidate();
        repaint();
    }

    private void customizeComponents() {
        // Enhanced search field with FlatLaf styling
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by invoice number...");
        jTextField1.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        jTextField1.setToolTipText("Search invoices (Ctrl+F or /) - Press ESC to clear all filters");
        jTextField1.setForeground(Color.GRAY);

        // Enhanced combo box with FlatLaf styling
        sortByDays.setForeground(Color.GRAY);
        sortByDays.setModel(new DefaultComboBoxModel<>(new String[]{
            "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 90 Days",
            "1 Year", "2 Years", "5 Years", "10 Years", "20 Years"
        }));
        sortByDays.setToolTipText("Filter by period (Alt+1 to Alt+0) - Press ESC to reset");

        // Sales Report Button styling
        salesReportBtn.setFont(new Font("Nunito ExtraBold", Font.BOLD, 14));
        salesReportBtn.setForeground(Color.WHITE);
        salesReportBtn.setBackground(new Color(16, 185, 129));
        salesReportBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        salesReportBtn.setFocusPainted(false);
        salesReportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        salesReportBtn.setToolTipText("Export Sales Data (Ctrl+P or Ctrl+R)");

        // Add hover effects to sales report button
        salesReportBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                salesReportBtn.setBackground(new Color(34, 197, 94));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                salesReportBtn.setBackground(new Color(16, 185, 129));
            }
        });

        roundedPanel1.setVisible(false);

        // Enhanced scroll pane
        jScrollPane1.setBorder(null);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(20);
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");

        roundedPanel1.setBackgroundColor(Color.decode("#E0F2FF"));
        roundedPanel1.setBorderThickness(0);
        roundedPanel1.setCornerRadius(16);

        headPanel.setOpaque(false);
        middelePanel.setOpaque(false);
        buttomPanel.setOpaque(false);

        if (productPanel instanceof RoundedPanel) {
            ((RoundedPanel) productPanel).setCornerRadius(16);
            ((RoundedPanel) productPanel).setBorderThickness(0);
        }
    }

    private void loadSalesData(String searchText, String period) {
        clearInvoiceCards();

        String finalPeriod = period;
        String finalSearchText = searchText;

        SwingWorker<List<InvoiceDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InvoiceDTO> doInBackground() throws Exception {
                try {
                    return salesDAO.fetchInvoicesFromDatabase(finalSearchText, finalPeriod);
                } catch (Exception e) {
                    System.err.println("Error fetching invoices: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }

            @Override
            protected void done() {
                try {
                    List<InvoiceDTO> invoices = get();
                    displayInvoices(invoices);
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        jPanel2.removeAll();
                        jPanel2.add(createErrorPanel(e), BorderLayout.CENTER);
                        jPanel2.revalidate();
                        jPanel2.repaint();
                    });
                }
            }
        };

        worker.execute();
    }

    private void displayInvoices(List<InvoiceDTO> invoices) {
        invoicesContainer = new JPanel();
        invoicesContainer.setLayout(new BoxLayout(invoicesContainer, BoxLayout.Y_AXIS));
        invoicesContainer.setBackground(new Color(248, 250, 252));
        invoicesContainer.setOpaque(false);
        invoicesContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        clearInvoiceCards();

        if (invoices.isEmpty()) {
            invoicesContainer.add(createNoDataPanel());
        } else {
            for (InvoiceDTO data : invoices) {
                JPanel invoiceCard = createInvoiceCard(data);
                invoiceCard.putClientProperty("invoiceNo", data.getInvoiceNo());
                invoicesContainer.add(invoiceCard);
                invoicesContainer.add(Box.createRigidArea(new Dimension(0, 16)));
                invoiceCardsList.add(invoiceCard);
            }
        }

        jPanel2.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(248, 250, 252));
        wrapperPanel.add(invoicesContainer, BorderLayout.NORTH);
        jPanel2.add(wrapperPanel, BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
    }

    private JPanel createInvoiceCard(InvoiceDTO data) {
        return createInvoiceCard(
                data.getSalesId(),
                data.getInvoiceNo(),
                data.getFormattedDateTime(),
                data.getTotal(),
                data.getItemDiscount(),
                data.getSaleDiscount(),
                data.getTotalDiscount(),
                data.getPaymentMethod(),
                data.getCashierName(),
                data.getCustomerName(),
                data.getSaleStatus()
        );
    }

    private void clearInvoiceCards() {
        for (JPanel card : invoiceCardsList) {
            for (MouseListener ml : card.getMouseListeners()) {
                card.removeMouseListener(ml);
            }
        }
        invoiceCardsList.clear();
        currentCardIndex = -1;
        currentFocusedCard = null;
    }

    private JPanel createNoDataPanel() {
        JPanel noDataPanel = new JPanel();
        noDataPanel.setLayout(new BoxLayout(noDataPanel, BoxLayout.Y_AXIS));
        noDataPanel.setBackground(new Color(248, 250, 252));
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        JLabel iconLabel = new JLabel("📊");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(iconLabel);
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel noDataLabel = new JLabel("No sales records found");
        noDataLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 20));
        noDataLabel.setForeground(new Color(71, 85, 105));
        noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(noDataLabel);

        JLabel hintLabel = new JLabel("Try adjusting your search or filter to see more results");
        hintLabel.setFont(new Font("Nunito", Font.PLAIN, 15));
        hintLabel.setForeground(new Color(148, 163, 184));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noDataPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        noDataPanel.add(hintLabel);

        return noDataPanel;
    }

    private JPanel createErrorPanel(Exception e) {
        RoundedPanel errorPanel = new RoundedPanel();
        errorPanel.setBackgroundColor(new Color(254, 242, 242));
        errorPanel.setBorderThickness(2);
        errorPanel.setCornerRadius(16);
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        errorPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        errorPanel.setMaximumSize(new Dimension(500, 250));
        errorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorPanel.add(iconLabel);
        errorPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel errorLabel = new JLabel("Oops! Something went wrong");
        errorLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 18));
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorPanel.add(errorLabel);

        JLabel errorDetails = new JLabel("<html><div style='text-align: center;'>"
                + "We couldn't load the sales data.<br>"
                + "Please check your connection and try again.</div></html>");
        errorDetails.setFont(new Font("Nunito", Font.PLAIN, 14));
        errorDetails.setForeground(new Color(185, 28, 28));
        errorDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        errorPanel.add(errorDetails);

        JButton retryButton = new JButton("Retry");
        retryButton.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        retryButton.setForeground(Color.WHITE);
        retryButton.setBackground(new Color(220, 38, 38));
        retryButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        retryButton.setFocusPainted(false);
        retryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.addActionListener(ev -> loadSalesData("", "All Time"));
        errorPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        errorPanel.add(retryButton);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(248, 250, 252));
        container.add(Box.createVerticalGlue());
        container.add(errorPanel);
        container.add(Box.createVerticalGlue());

        return container;
    }

    private JPanel createInvoiceCard(int salesId, String invoiceNo, String datetime,
            double total, double itemDiscount, double saleDiscount,
            double totalDiscount, String paymentMethod,
            String cashierName, String customerName,
            String saleStatus) {
        RoundedPanel cardPanel = new RoundedPanel();
        cardPanel.setLayout(new BorderLayout(0, 0));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setCornerRadius(20);
        cardPanel.setBorderThickness(0);
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setOpaque(false);

        JPanel headerPanel = createInvoiceHeader(invoiceNo, customerName, paymentMethod, total, saleStatus);
        JPanel itemsPanel = createItemsContentPanel(salesId);
        JPanel footerPanel = createInvoiceFooter(datetime, itemDiscount, saleDiscount, totalDiscount, cashierName, salesId);

        headerPanel.setOpaque(false);
        itemsPanel.setOpaque(false);
        footerPanel.setOpaque(false);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(itemsPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        cardPanel.add(contentPanel, BorderLayout.CENTER);

        cardPanel.addMouseListener(new MouseAdapter() {
            private Timer hoverTimer;

            @Override
            public void mouseEntered(MouseEvent e) {
                if (cardPanel != currentFocusedCard) {
                    hoverTimer = new Timer(10, new ActionListener() {
                        float alpha = 0f;

                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            alpha += 0.1f;
                            if (alpha >= 1f) {
                                alpha = 1f;
                                ((Timer) evt.getSource()).stop();
                            }
                            Color baseColor = new Color(245, 247, 250);
                            cardPanel.setBackground(baseColor);
                            contentPanel.setBackground(baseColor);
                            cardPanel.repaint();
                        }
                    });
                    hoverTimer.start();
                }
                cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoverTimer != null) {
                    hoverTimer.stop();
                }
                if (cardPanel != currentFocusedCard) {
                    cardPanel.setBackground(Color.WHITE);
                    contentPanel.setBackground(Color.WHITE);
                    cardPanel.repaint();
                }
                cardPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                cardPanel.setBackground(new Color(241, 245, 249));
                contentPanel.setBackground(new Color(241, 245, 249));
                cardPanel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                cardPanel.setBackground(new Color(248, 250, 252));
                contentPanel.setBackground(new Color(248, 250, 252));
                cardPanel.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentFocusedCard != null && currentFocusedCard != cardPanel) {
                    int oldIndex = invoiceCardsList.indexOf(currentFocusedCard);
                    if (oldIndex >= 0) {
                        deselectCard(oldIndex);
                    }
                }

                currentCardIndex = invoiceCardsList.indexOf(cardPanel);
                selectCurrentCard();
                SalesPanel.this.requestFocusInWindow();
            }
        });

        return cardPanel;
    }

    private JPanel createInvoiceHeader(String invoiceNo, String customerName, String paymentMethod,
            double total, String saleStatus) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout(20, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        // LEFT SIDE: Invoice info with modern layout
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // Invoice number with icon inline
        JPanel invoiceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        invoiceRow.setOpaque(false);
        invoiceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add invoice SVG icon
        JLabel invoiceIcon = new JLabel();
        try {
            FlatSVGIcon svgIcon = new FlatSVGIcon("lk/com/pos/icon/invoice.svg", 28, 28);
            svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEAL_BORDER_SELECTED));
            invoiceIcon.setIcon(svgIcon);
        } catch (Exception e) {
            // Fallback: use text emoji
            invoiceIcon.setText("📄");
            invoiceIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        }

        JLabel invoiceLabel = new JLabel("#" + (invoiceNo != null ? invoiceNo.toUpperCase() : ""));
        invoiceLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 24));
        invoiceLabel.setForeground(new Color(30, 41, 59));

        invoiceRow.add(invoiceIcon);
        invoiceRow.add(invoiceLabel);

        // Customer name
        JLabel customerLabel = new JLabel((customerName != null ? customerName : "Walk-in Customer"));
        customerLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        customerLabel.setForeground(new Color(100, 116, 139));
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(invoiceRow);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftPanel.add(customerLabel);

        // RIGHT SIDE: Clean layout with total and badges
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // Total amount - prominent display
        JLabel totalLabel = new JLabel(String.format("Rs.%.2f", total));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 28));
        totalLabel.setForeground(new Color(16, 185, 129)); // Emerald green
        totalLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Badges container - placed below total with proper spacing
        JPanel badgesPanel = new JPanel();
        badgesPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badgesPanel.setOpaque(false);
        badgesPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Payment badge
        JLabel paymentBadge = createPaymentBadge(paymentMethod);
        badgesPanel.add(paymentBadge);

        // Status badge (only if not completed)
        if (saleStatus != null && !isCompletedStatus(saleStatus)) {
            JLabel statusBadge = createStatusBadge(saleStatus);
            badgesPanel.add(statusBadge);
        }

        rightPanel.add(totalLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Space between total and badges
        rightPanel.add(badgesPanel);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private boolean isCompletedStatus(String status) {
        if (status == null) {
            return true;
        }
        String normalized = status.trim().toLowerCase();
        return normalized.equals("completed") || normalized.equals("done")
                || normalized.equals("success") || normalized.equals("finished")
                || normalized.equals("paid") || normalized.equals("confirmed")
                || normalized.isEmpty();
    }

    private JLabel createStatusBadge(String status) {
        if (status == null) {
            return new JLabel();
        }

        String normalizedStatus = status.trim().toLowerCase();
        JLabel statusBadge = new JLabel();
        statusBadge.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Status mapping with emojis
        switch (normalizedStatus) {
            case "due amount":
            case "due":
            case "pending payment":
                statusBadge.setBackground(new Color(234, 88, 12)); // Orange
                statusBadge.setText("DUE");
                break;
            case "no due":
            case "paid":
            case "complete":
                statusBadge.setBackground(new Color(34, 197, 94)); // Green
                statusBadge.setText("PAID");
                break;
            case "hold":
            case "holding":
                statusBadge.setBackground(new Color(245, 158, 11)); // Amber
                statusBadge.setText("HOLD");
                break;
            case "cancelled":
            case "canceled":
                statusBadge.setBackground(new Color(220, 38, 38)); // Red
                statusBadge.setText("CANCELLED");
                break;
            case "void":
            case "voided":
                statusBadge.setBackground(new Color(159, 18, 57)); // Dark Red
                statusBadge.setText("VOID");
                break;
            case "pending":
            case "processing":
                statusBadge.setBackground(new Color(245, 158, 11)); // Amber
                statusBadge.setText("PENDING");
                break;
            default:
                statusBadge.setBackground(new Color(100, 116, 139)); // Gray
                String displayText = status.toUpperCase();
                if (displayText.length() > 12) {
                    displayText = displayText.substring(0, 12) + "...";
                }
                statusBadge.setText(displayText);
        }

        return statusBadge;
    }

    private JPanel createItemsContentPanel(int salesId) {
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BorderLayout());
        itemsPanel.setOpaque(false);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel itemsHeader = new JLabel("ITEMS");
        itemsHeader.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        itemsHeader.setForeground(new Color(71, 85, 105));
        itemsHeader.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        headerPanel.add(itemsHeader, BorderLayout.WEST);

        RoundedPanel itemsContainer = new RoundedPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackgroundColor(new Color(249, 250, 251));
        itemsContainer.setCornerRadius(12);
        itemsContainer.setBorderThickness(0);
        itemsContainer.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        loadSaleItems(itemsContainer, salesId);

        itemsPanel.add(headerPanel, BorderLayout.NORTH);
        itemsPanel.add(itemsContainer, BorderLayout.CENTER);

        return itemsPanel;
    }

    private JPanel createInvoiceFooter(String datetime, double itemDiscount, double saleDiscount,
            double totalDiscount, String cashierName, int salesId) {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Discount section (if applicable)
        if (totalDiscount > 0) {
            RoundedPanel discountPanel = new RoundedPanel();
            discountPanel.setLayout(new BoxLayout(discountPanel, BoxLayout.Y_AXIS));
            discountPanel.setBackgroundColor(new Color(255, 247, 237)); // Warm orange tint
            discountPanel.setCornerRadius(12);
            discountPanel.setBorderThickness(0);
            discountPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            discountPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            JLabel discountHeader = new JLabel("DISCOUNTS APPLIED");
            discountHeader.setFont(new Font("Nunito ExtraBold", Font.BOLD, 11));
            discountHeader.setForeground(new Color(194, 65, 12));
            discountPanel.add(discountHeader);
            discountPanel.add(Box.createRigidArea(new Dimension(0, 8)));

            if (itemDiscount > 0) {
                JPanel itemDiscountPanel = createDiscountRow("Item Discount", itemDiscount, new Color(100, 116, 139));
                itemDiscountPanel.setOpaque(false);
                discountPanel.add(itemDiscountPanel);
                discountPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            }

            if (saleDiscount > 0) {
                JPanel saleDiscountPanel = createDiscountRow("Sale Discount", saleDiscount, new Color(245, 158, 11));
                saleDiscountPanel.setOpaque(false);
                discountPanel.add(saleDiscountPanel);
                discountPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            }

            discountPanel.add(Box.createRigidArea(new Dimension(0, 4)));

            JSeparator separator = new JSeparator();
            separator.setForeground(new Color(253, 186, 116));
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            discountPanel.add(separator);
            discountPanel.add(Box.createRigidArea(new Dimension(0, 8)));

            JPanel totalDiscountPanel = createDiscountRow("TOTAL SAVINGS", totalDiscount, new Color(220, 38, 38));
            totalDiscountPanel.setOpaque(false);
            discountPanel.add(totalDiscountPanel);

            footerPanel.add(discountPanel);
            footerPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        }

        // Bottom info bar with better styling
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(16, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));

        // Date on left
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        datePanel.setOpaque(false);

        JLabel dateIcon = new JLabel("📅");
        dateIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        JLabel dateLabel = new JLabel(formatDateTime(datetime));
        dateLabel.setFont(new Font("Nunito", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(100, 116, 139));

        datePanel.add(dateIcon);
        datePanel.add(dateLabel);

        // Cashier on right
        JPanel cashierPanel = new JPanel();
        cashierPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        cashierPanel.setOpaque(false);

        JLabel cashierIcon = new JLabel("👤");
        cashierIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        JLabel cashierLabel = new JLabel("Cashier: ");
        cashierLabel.setFont(new Font("Nunito", Font.PLAIN, 13));
        cashierLabel.setForeground(new Color(100, 116, 139));

        JLabel cashierValue = new JLabel(cashierName != null ? cashierName : "Unknown");
        cashierValue.setFont(new Font("Nunito SemiBold", Font.BOLD, 13));
        cashierValue.setForeground(new Color(51, 65, 85));

        cashierPanel.add(cashierIcon);
        cashierPanel.add(cashierLabel);
        cashierPanel.add(cashierValue);

        bottomPanel.add(datePanel, BorderLayout.WEST);
        bottomPanel.add(cashierPanel, BorderLayout.EAST);

        footerPanel.add(bottomPanel);

        return footerPanel;
    }

    private JPanel createDiscountRow(String label, double amount, Color color) {
        JPanel discountRow = new JPanel();
        discountRow.setLayout(new BoxLayout(discountRow, BoxLayout.X_AXIS));
        discountRow.setOpaque(false);
        discountRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel discountLabel = new JLabel(label);
        discountLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 14));
        discountLabel.setForeground(new Color(71, 85, 105));

        JLabel discountValue = new JLabel(String.format("-Rs.%.2f", amount));
        discountValue.setFont(new Font("Nunito ExtraBold", Font.BOLD, 15));
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
        paymentBadge.setFont(new Font("Nunito ExtraBold", Font.BOLD, 12));
        paymentBadge.setForeground(Color.WHITE);
        paymentBadge.setOpaque(true);

        // Clean, user-friendly styling with proper padding
        paymentBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12) // Balanced padding
        ));

        // Simple color coding without emojis for cleaner look
        if (normalizedPayment.contains("CASH")) {
            paymentBadge.setBackground(new Color(34, 197, 94)); // Green
            paymentBadge.setText("CASH");
        } else if (normalizedPayment.contains("CARD") || normalizedPayment.contains("CREDIT CARD")) {
            paymentBadge.setBackground(new Color(59, 130, 246)); // Blue
            paymentBadge.setText("CARD");
        } else if (normalizedPayment.contains("CREDIT") || normalizedPayment.contains("ACCOUNT")) {
            paymentBadge.setBackground(new Color(234, 88, 12)); // Orange
            paymentBadge.setText("CREDIT");
        } else if (normalizedPayment.contains("CHEQUE") || normalizedPayment.contains("CHECK")) {
            paymentBadge.setBackground(new Color(139, 92, 246)); // Purple
            paymentBadge.setText("CHEQUE");
        } else if (normalizedPayment.contains("ONLINE") || normalizedPayment.contains("DIGITAL")) {
            paymentBadge.setBackground(new Color(168, 85, 247)); // Violet
            paymentBadge.setText("ONLINE");
        } else {
            paymentBadge.setBackground(new Color(100, 116, 139)); // Gray
            String displayText = normalizedPayment.length() > 12
                    ? normalizedPayment.substring(0, 12) + "..." : normalizedPayment;
            paymentBadge.setText(displayText);
        }

        return paymentBadge;
    }

    private void loadSaleItems(JPanel itemsListPanel, int salesId) {
        try {
            List<SaleItemDTO> items = salesDAO.loadSaleItems(salesId);

            if (items.isEmpty()) {
                JLabel noItemsLabel = new JLabel("No items in this sale");
                noItemsLabel.setFont(new Font("Nunito SemiBold", Font.ITALIC, 13));
                noItemsLabel.setForeground(new Color(148, 163, 184));
                noItemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemsListPanel.add(noItemsLabel);
            } else {
                for (int i = 0; i < items.size(); i++) {
                    SaleItemDTO item = items.get(i);
                    JPanel itemCard = createItemCard(
                            item.getProductName(),
                            item.getQty(),
                            item.getPrice(),
                            item.getDiscountPrice(),
                            item.getTotal(),
                            item.getBatchNo()
                    );
                    itemsListPanel.add(itemCard);

                    if (i < items.size() - 1) {
                        JSeparator separator = new JSeparator();
                        separator.setForeground(new Color(229, 231, 235));
                        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                        separator.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
                        itemsListPanel.add(separator);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading items");
            errorLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
            errorLabel.setForeground(new Color(220, 38, 38));
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemsListPanel.add(errorLabel);
        }
    }

    private JPanel createItemCard(String productName, int qty, double price,
            double discountPrice, double total, String batchNo) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout(15, 0));
        itemPanel.setOpaque(false);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel productLabel = new JLabel(productName != null ? productName : "Unknown Product");
        productLabel.setFont(new Font("Nunito SemiBold", Font.PLAIN, 15));
        productLabel.setForeground(new Color(30, 41, 59));
        productLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.X_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceQtyLabel = new JLabel(String.format("Rs.%.2f × %d", price, qty));
        priceQtyLabel.setFont(new Font("Nunito", Font.PLAIN, 13));
        priceQtyLabel.setForeground(new Color(100, 116, 139));
        priceQtyLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        detailsPanel.add(priceQtyLabel);

        if (discountPrice > 0) {
            JLabel discountInfo = new JLabel(String.format(" • Rs.%.2f discount", discountPrice));
            discountInfo.setFont(new Font("Nunito SemiBold", Font.PLAIN, 13));
            discountInfo.setForeground(new Color(220, 38, 38));
            discountInfo.setBorder(BorderFactory.createEmptyBorder(2, 8, 0, 0));
            detailsPanel.add(discountInfo);
        }

        JPanel extraInfoPanel = new JPanel();
        extraInfoPanel.setLayout(new BoxLayout(extraInfoPanel, BoxLayout.X_AXIS));
        extraInfoPanel.setOpaque(false);
        extraInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (batchNo != null && !batchNo.isEmpty()) {
            JLabel batchLabel = new JLabel("Batch: " + batchNo);
            batchLabel.setFont(new Font("Nunito", Font.PLAIN, 12));
            batchLabel.setForeground(new Color(148, 163, 184));
            batchLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            extraInfoPanel.add(batchLabel);
        }

        leftPanel.add(productLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftPanel.add(detailsPanel);

        if (extraInfoPanel.getComponentCount() > 0) {
            leftPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            leftPanel.add(extraInfoPanel);
        }

        JLabel totalLabel = new JLabel(String.format("Rs.%.2f", total));
        totalLabel.setFont(new Font("Nunito ExtraBold", Font.BOLD, 16));
        totalLabel.setForeground(new Color(30, 41, 59));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        itemPanel.add(leftPanel, BorderLayout.CENTER);
        itemPanel.add(totalLabel, BorderLayout.EAST);

        return itemPanel;
    }

    private String formatDateTime(String datetime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            Date date = inputFormat.parse(datetime);
            return outputFormat.format(date);
        } catch (Exception e) {
            if (datetime != null && datetime.length() >= 10) {
                return datetime.substring(0, 10) + ", " + (datetime.length() > 11 ? datetime.substring(11) : "");
            }
            return datetime != null ? datetime : "Unknown Date";
        }
    }

    static class RoundBorder extends AbstractBorder {

        private Color color;
        private int thickness;
        private int radius;

        RoundBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x + thickness / 2, y + thickness / 2,
                    width - thickness, height - thickness,
                    radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }
    }

    static class ShadowBorder extends AbstractBorder {

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < 4; i++) {
                g2d.setColor(new Color(0, 0, 0, 5 - i));
                g2d.drawRoundRect(x + i, y + i, width - 2 * i - 1, height - 2 * i - 1, 20, 20);
            }

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
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
        salesReportBtn = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

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
        sortByDays.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        sortByDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByDaysActionPerformed(evt);
            }
        });

        salesReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        salesReportBtn.setText("Sales Report");
        salesReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salesReportBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 955, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByDays, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(salesReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sortByDays, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(salesReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
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

    private void salesReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salesReportBtnActionPerformed
        showExportOptions();
    }//GEN-LAST:event_salesReportBtnActionPerformed


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
    private javax.swing.JButton salesReportBtn;
    private javax.swing.JComboBox<String> sortByDays;
    private javax.swing.JLabel total;
    // End of variables declaration//GEN-END:variables
}
