package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.dao.StockLossDAO;
import lk.com.pos.dto.StockLossDTO;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.AbstractAction;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lk.com.pos.dialog.AddNewLossStock;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * StockLossPanel - Enhanced panel for managing stock losses Features: Time
 * period filters, search, keyboard navigation, modern UI
 *
 * @author pasin
 * @version 2.0
 */
public class StockLossPanel extends javax.swing.JPanel {

    // DAO instance
    private StockLossDAO stockLossDAO;

    // Date & Number Formatting
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // UI Constants - Colors (same as before)
    private static final class Colors {

        static final Color TEAL_PRIMARY = new Color(28, 181, 187);
        static final Color TEAL_HOVER = new Color(60, 200, 206);
        static final Color BORDER_DEFAULT = new Color(230, 230, 230);
        static final Color BACKGROUND = Color.decode("#F8FAFC");
        static final Color CARD_WHITE = Color.WHITE;
        static final Color TEXT_PRIMARY = Color.decode("#1E293B");
        static final Color TEXT_SECONDARY = Color.decode("#6B7280");
        static final Color TEXT_MUTED = Color.decode("#94A3B8");
        static final Color BADGE_BOUNCED_BG = Color.decode("#FEE2E2");
        static final Color BADGE_BOUNCED_FG = Color.decode("#991B1B");
        static final Color BADGE_BOUNCED_BORDER = Color.decode("#F87171");
        static final Color BADGE_EXPIRED_BG = Color.decode("#FED7AA");
        static final Color BADGE_EXPIRED_FG = Color.decode("#7C2D12");
        static final Color BADGE_EXPIRED_BORDER = Color.decode("#FB923C");
        static final Color BADGE_DAMAGED_BG = Color.decode("#FECACA");
        static final Color BADGE_DAMAGED_FG = Color.decode("#7F1D1D");
        static final Color BADGE_DAMAGED_BORDER = Color.decode("#EF4444");
        static final Color DETAIL_BATCH = Color.decode("#8B5CF6");
        static final Color DETAIL_INVOICE = Color.decode("#EC4899");
        static final Color DETAIL_PRICE = Color.decode("#10B981");
        static final Color DETAIL_QTY = Color.decode("#06B6D4");
        static final Color DETAIL_USER = Color.decode("#6366F1");
        static final Color DETAIL_DATE = Color.decode("#F59E0B");
        static final Color LOSS_HIGH = Color.decode("#DC2626");
        static final Color LOSS_MEDIUM = Color.decode("#DC2626");
        static final Color LOSS_LOW = Color.decode("#DC2626");
        static final Color LOSS_BG = Color.decode("#FEE2E2");
        static final Color LOSS_BORDER = Color.decode("#FECACA");
        static final Color BTN_EDIT_BG = Color.decode("#EFF6FF");
        static final Color BTN_EDIT_BORDER = Color.decode("#BFDBFE");
        static final Color EXPORT_BTN_BG = new Color(0, 0, 0, 0);
        static final Color EXPORT_BTN_FG = Color.decode("#10B981");
        static final Color EXPORT_BTN_BORDER = Color.decode("#10B981");
        static final Color EXPORT_BTN_HOVER_BG = Color.decode("#10B981");
        static final Color EXPORT_BTN_HOVER_BORDER = Color.decode("#34D399");
        static final Color ADD_STOCK_BTN_BG = new Color(0, 0, 0, 0);
        static final Color ADD_STOCK_BTN_FG = Colors.TEAL_PRIMARY;
        static final Color ADD_STOCK_BTN_BORDER = Colors.TEAL_PRIMARY;
        static final Color ADD_STOCK_BTN_HOVER_BG = Colors.TEAL_PRIMARY;
        static final Color ADD_STOCK_BTN_HOVER_BORDER = Colors.TEAL_HOVER;
    }

    // UI Constants - Dimensions (same as before)
    private static final class Dimensions {

        static final Dimension CARD_SIZE = new Dimension(420, 420);
        static final Dimension CARD_MAX_SIZE = new Dimension(420, 420);
        static final Dimension CARD_MIN_SIZE = new Dimension(380, 420);
        static final Dimension ACTION_BUTTON_SIZE = new Dimension(30, 30);
        static final int CARD_WIDTH_WITH_GAP = 445;
        static final int GRID_GAP = 25;
        static final int CARD_PADDING = 16;
        static final int THREE_COLUMN_MIN_WIDTH = 1200;
        static final int TWO_COLUMN_MIN_WIDTH = 768;
        static final int SINGLE_COLUMN_MAX_WIDTH = 767;
        static final Dimension ACTION_BTN_SIZE = new Dimension(47, 47);
    }

    // UI Constants - Fonts (same as before)
    private static final class Fonts {

        static final java.awt.Font HEADER = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font SECTION_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font BADGE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font DETAIL_TITLE = new java.awt.Font("Nunito SemiBold", 0, 13);
        static final java.awt.Font DETAIL_VALUE = new java.awt.Font("Nunito SemiBold", 1, 14);
        static final java.awt.Font LOSS_AMOUNT = new java.awt.Font("Nunito ExtraBold", 1, 18);
        static final java.awt.Font HINT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 13);
        static final java.awt.Font HINT_KEY = new java.awt.Font("Consolas", 1, 11);
        static final java.awt.Font HINT_DESC = new java.awt.Font("Nunito SemiBold", 0, 11);
        static final java.awt.Font LOADING = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font POSITION = new java.awt.Font("Nunito ExtraBold", 1, 14);
    }

    // UI Constants - Strings (same as before)
    private static final class Strings {

        static final String SEARCH_PLACEHOLDER = "Search by Product Name, Invoice, or Batch No";
        static final String NO_RECORDS = "No stock loss records found";
        static final String LOADING_MESSAGE = "Loading stock losses...";
        static final String LOADING_SUBMESSAGE = "Please wait";
        static final String HELP_TITLE = "KEYBOARD SHORTCUTS";
        static final String HELP_CLOSE_HINT = "Press ? to hide";
        static final String SECTION_DETAILS = "LOSS DETAILS";
        static final String NO_VALUE = "N/A";
    }

    // Business Constants (same as before)
    private static final class Business {

        static final double HIGH_LOSS_THRESHOLD = 10000.0;
        static final double MEDIUM_LOSS_THRESHOLD = 5000.0;
        static final long REFRESH_COOLDOWN_MS = 1000;
    }

    // Keyboard Navigation (same as before)
    private lk.com.pos.privateclasses.RoundedPanel currentFocusedCard = null;
    private List<lk.com.pos.privateclasses.RoundedPanel> lossCardsList = new ArrayList<>();
    private int currentCardIndex = -1;
    private int currentColumns = 3;

    // UI Components (same as before)
    private JPanel positionIndicator;
    private JLabel positionLabel;
    private Timer positionTimer;
    private JPanel keyboardHintsPanel;
    private boolean hintsVisible = false;
    private JPanel loadingPanel;

    // State (same as before)
    private long lastRefreshTime = 0;

    public StockLossPanel() {
        // Initialize DAO
        stockLossDAO = new StockLossDAO();

        initComponents();
        initializeUI();
        createPositionIndicator();
        createKeyboardHintsPanel();
        createLoadingPanel();
        setupKeyboardShortcuts();
        loadStockLosses();

        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
            showKeyboardHints();
        });
    }

    /**
     * Initializes UI components and settings
     */
    private void initializeUI() {
        setupScrollPane();
        setupIcons();
        setupSearchField();
        setupReasonComboBox();
        setupComboBox();
        setupButtons();
        setupPanel();
    }

    /**
     * Configures scroll pane styling
     */
    private void setupScrollPane() {
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5; thumb: #1CB5BB; width: 8");
    }

    /**
     * Sets up button icons
     */
    private void setupIcons() {
        try {
            FlatSVGIcon blueEdit = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 20, 20);
            editBtn.setIcon(blueEdit);
        } catch (Exception e) {
            System.err.println("Error loading edit icon: " + e.getMessage());
        }
    }

    /**
     * Configures search field
     */
    private void setupSearchField() {
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Strings.SEARCH_PLACEHOLDER);
        try {
            jTextField1.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                    new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        } catch (Exception e) {
            System.err.println("Error loading search icon: " + e.getMessage());
        }

        jTextField1.setToolTipText("Search stock losses (Ctrl+F or /)");

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                performSearch();
            }
        });
    }

    /**
     * Configures reason combo box (replaces radio buttons)
     */
    private void setupReasonComboBox() {
        sortByReason.removeAllItems();
        sortByReason.addItem("All Reasons");
        sortByReason.addItem("Expired");
        sortByReason.addItem("Damaged");
        sortByReason.addItem("Bounced");
        sortByReason.addItem("Other");

        sortByReason.setSelectedItem("All Reasons");
        sortByReason.setToolTipText("Filter by reason (Alt+R)");

        sortByReason.addActionListener(evt -> {
            performSearch();
            this.requestFocusInWindow();
        });
    }

    /**
     * Configures combo box with 20 years option
     */
    private void setupComboBox() {
        sortByDays.removeAllItems();
        sortByDays.addItem("All Time");
        sortByDays.addItem("Today");
        sortByDays.addItem("Last 7 Days");
        sortByDays.addItem("Last 30 Days");
        sortByDays.addItem("Last 90 Days");
        sortByDays.addItem("1 Year");
        sortByDays.addItem("2 Years");
        sortByDays.addItem("5 Years");
        sortByDays.addItem("10 Years");
        sortByDays.addItem("20 Years"); // Added 20 years option

        sortByDays.setSelectedItem("All Time");
        sortByDays.setToolTipText("Filter by time period (Alt+T)");

        sortByDays.addActionListener(evt -> {
            performSearch();
            this.requestFocusInWindow();
        });
    }

    /**
     * Configures buttons
     */
    private void setupButtons() {
        setupExportButton();
        setupAddStockButton();

        lostReportBtn.setToolTipText("Generate Stock Loss Report (Ctrl+R)");
        addNewLostBtn.setToolTipText("Add New Stock Loss (Ctrl+N)");

        // Add action listener for report button
        lostReportBtn.addActionListener(e -> showExportOptions());
    }

    /**
     * Sets up export button with icon and hover effects
     */
    private void setupExportButton() {
        lostReportBtn.setPreferredSize(Dimensions.ACTION_BTN_SIZE);
        lostReportBtn.setMinimumSize(Dimensions.ACTION_BTN_SIZE);
        lostReportBtn.setMaximumSize(Dimensions.ACTION_BTN_SIZE);

        lostReportBtn.setBackground(Colors.EXPORT_BTN_BG);
        lostReportBtn.setForeground(Colors.EXPORT_BTN_FG);
        lostReportBtn.setText("");

        lostReportBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.EXPORT_BTN_BORDER, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        lostReportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lostReportBtn.setFocusPainted(false);

        try {
            FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
            printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Colors.EXPORT_BTN_FG));
            lostReportBtn.setIcon(printIcon);
        } catch (Exception e) {
            System.err.println("Error loading print icon: " + e.getMessage());
        }

        lostReportBtn.setToolTipText("Export Stock Loss Report");

        lostReportBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lostReportBtn.setBackground(Colors.EXPORT_BTN_HOVER_BG);
                lostReportBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.EXPORT_BTN_HOVER_BORDER, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    lostReportBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                lostReportBtn.setToolTipText("Export Stock Loss Report (Ctrl+R)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lostReportBtn.setBackground(Colors.EXPORT_BTN_BG);
                lostReportBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.EXPORT_BTN_BORDER, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Colors.EXPORT_BTN_FG));
                    lostReportBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                lostReportBtn.setToolTipText("Export Stock Loss Report");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lostReportBtn.setBackground(Color.decode("#059669"));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                lostReportBtn.setBackground(Colors.EXPORT_BTN_HOVER_BG);
            }
        });
    }

    /**
     * Sets up add stock button with icon and hover effects
     */
    private void setupAddStockButton() {
        addNewLostBtn.setPreferredSize(Dimensions.ACTION_BTN_SIZE);
        addNewLostBtn.setMinimumSize(Dimensions.ACTION_BTN_SIZE);
        addNewLostBtn.setMaximumSize(Dimensions.ACTION_BTN_SIZE);

        addNewLostBtn.setBackground(Colors.ADD_STOCK_BTN_BG);
        addNewLostBtn.setForeground(Colors.ADD_STOCK_BTN_FG);
        addNewLostBtn.setText("");

        addNewLostBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.ADD_STOCK_BTN_BORDER, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        addNewLostBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addNewLostBtn.setFocusPainted(false);

        try {
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
            addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Colors.ADD_STOCK_BTN_FG));
            addNewLostBtn.setIcon(addIcon);
        } catch (Exception e) {
            System.err.println("Error loading add icon: " + e.getMessage());
        }

        addNewLostBtn.setToolTipText("Add New Stock Loss");

        addNewLostBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addNewLostBtn.setBackground(Colors.ADD_STOCK_BTN_HOVER_BG);
                addNewLostBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.ADD_STOCK_BTN_HOVER_BORDER, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    addNewLostBtn.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                addNewLostBtn.setToolTipText("Add New Stock Loss (Ctrl+N)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addNewLostBtn.setBackground(Colors.ADD_STOCK_BTN_BG);
                addNewLostBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.ADD_STOCK_BTN_BORDER, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Colors.ADD_STOCK_BTN_FG));
                    addNewLostBtn.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                addNewLostBtn.setToolTipText("Add New Stock Loss");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                addNewLostBtn.setBackground(Colors.TEAL_PRIMARY.darker());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addNewLostBtn.setBackground(Colors.ADD_STOCK_BTN_HOVER_BG);
            }
        });
    }

    /**
     * Configures panel
     */
    private void setupPanel() {
        jPanel2.setBackground(Colors.BACKGROUND);

        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayoutForScreenSize();
            }
        });
    }

    /**
     * Updates layout based on screen size
     */
    private void updateLayoutForScreenSize() {
        if (!lossCardsList.isEmpty()) {
            int panelWidth = jPanel2.getWidth();
            int newColumns = calculateColumns(panelWidth);

            if (newColumns != currentColumns) {
                currentColumns = newColumns;
                reorganizeCardsLayout();
            }
        }
    }

    /**
     * Reorganizes cards when column count changes
     */
    private void reorganizeCardsLayout() {
        if (lossCardsList.isEmpty()) {
            return;
        }

        jPanel2.removeAll();

        JPanel gridPanel = createGridPanel();
        for (lk.com.pos.privateclasses.RoundedPanel card : lossCardsList) {
            gridPanel.add(card);
        }

        layoutCardsInPanel(gridPanel);
        jPanel2.revalidate();
        jPanel2.repaint();

        if (currentCardIndex >= 0) {
            updatePositionIndicator();
        }
    }

    /**
     * Creates loading overlay panel
     */
    private void createLoadingPanel() {
        loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(new Color(248, 250, 252, 230));
        loadingPanel.setVisible(false);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel loadingLabel = new JLabel(Strings.LOADING_MESSAGE);
        loadingLabel.setFont(Fonts.LOADING);
        loadingLabel.setForeground(Colors.TEAL_PRIMARY);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel(Strings.LOADING_SUBMESSAGE);
        subLabel.setFont(Fonts.HINT_DESC);
        subLabel.setForeground(Colors.TEXT_SECONDARY);
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(loadingLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(subLabel);
        centerPanel.add(Box.createVerticalGlue());

        loadingPanel.add(centerPanel, BorderLayout.CENTER);
        add(loadingPanel, Integer.valueOf(2000));
    }

    /**
     * Shows or hides loading panel
     */
    private void showLoading(boolean show) {
        SwingUtilities.invokeLater(() -> {
            loadingPanel.setVisible(show);
            if (show) {
                loadingPanel.setBounds(0, 0, getWidth(), getHeight());
            }
            revalidate();
            repaint();
        });
    }

    /**
     * Creates position indicator panel
     */
    private void createPositionIndicator() {
        positionIndicator = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        positionIndicator.setBackground(new Color(31, 41, 55, 230));
        positionIndicator.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        positionIndicator.setVisible(false);

        positionLabel = new JLabel();
        positionLabel.setFont(Fonts.POSITION);
        positionLabel.setForeground(Color.WHITE);

        positionIndicator.add(positionLabel);

        setLayout(new javax.swing.OverlayLayout(this) {
            @Override
            public void layoutContainer(java.awt.Container target) {
                super.layoutContainer(target);
                layoutOverlays();
            }
        });

        add(positionIndicator, Integer.valueOf(1000));
    }

    /**
     * Layouts overlay panels
     */
    private void layoutOverlays() {
        if (positionIndicator != null && positionIndicator.isVisible()) {
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

        if (loadingPanel != null && loadingPanel.isVisible()) {
            loadingPanel.setBounds(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * Shows position indicator with text
     */
    private void showPositionIndicator(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

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

    /**
     * Creates keyboard hints panel
     */
    private void createKeyboardHintsPanel() {
        keyboardHintsPanel = new JPanel();
        keyboardHintsPanel.setLayout(new BoxLayout(keyboardHintsPanel, BoxLayout.Y_AXIS));
        keyboardHintsPanel.setBackground(new Color(31, 41, 55, 240));
        keyboardHintsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);

        JLabel title = new JLabel(Strings.HELP_TITLE);
        title.setFont(Fonts.HINT_TITLE);
        title.setForeground(Colors.TEAL_PRIMARY);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);
        keyboardHintsPanel.add(Box.createVerticalStrut(10));

        addHintRow("← → ↑ ↓", "Navigate cards", "#FFFFFF");
        addHintRow("E", "Edit Loss Record", "#1CB5BB");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("Ctrl+N", "Add New Loss", "#60D5F2");
        addHintRow("Ctrl+R", "Loss Report", "#10B981");
        addHintRow("F5", "Refresh Data", "#06B6D4");
        addHintRow("Alt+T", "Time Period", "#F59E0B");
        addHintRow("Alt+R", "Reason Filter", "#EC4899");
        addHintRow("Esc", "Clear/Back", "#9CA3AF");
        addHintRow("?", "Toggle Help", "#1CB5BB");

        keyboardHintsPanel.add(Box.createVerticalStrut(10));

        JLabel closeHint = new JLabel(Strings.HELP_CLOSE_HINT);
        closeHint.setFont(new java.awt.Font("Nunito SemiBold", 2, 10));
        closeHint.setForeground(Color.decode("#9CA3AF"));
        closeHint.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        keyboardHintsPanel.add(closeHint);

        add(keyboardHintsPanel, Integer.valueOf(1001));
    }

    /**
     * Adds hint row to keyboard hints panel
     */
    private void addHintRow(String key, String description, String keyColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(300, 25));

        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(Fonts.HINT_KEY);
        keyLabel.setForeground(Color.decode(keyColor));
        keyLabel.setPreferredSize(new Dimension(90, 20));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(Fonts.HINT_DESC);
        descLabel.setForeground(Color.decode("#D1D5DB"));

        row.add(keyLabel);
        row.add(descLabel);
        keyboardHintsPanel.add(row);
    }

    /**
     * Shows or hides keyboard hints
     */
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

    /**
     * Sets up all keyboard shortcuts
     */
    private void setupKeyboardShortcuts() {
        this.setFocusable(true);

        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        int arrowCondition = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

        // Arrow navigation
        registerKeyAction("LEFT", KeyEvent.VK_LEFT, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_LEFT));
        registerKeyAction("RIGHT", KeyEvent.VK_RIGHT, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_RIGHT));
        registerKeyAction("UP", KeyEvent.VK_UP, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_UP));
        registerKeyAction("DOWN", KeyEvent.VK_DOWN, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_DOWN));

        // Actions
        registerKeyAction("E", KeyEvent.VK_E, 0, condition, this::editSelectedCard);

        // Enter to start navigation
        registerKeyAction("ENTER", KeyEvent.VK_ENTER, 0, condition, this::handleEnterKey);

        // Search
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, this::focusSearch);
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, this::handleSlashKey);

        // Escape
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, this::handleEscape);

        // Refresh
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, this::refreshLosses);

        // Report
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::openLossReport);

        // Add New
        registerKeyAction("CTRL_N", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, condition, this::openAddLossDialog);

        // Quick filters
        registerKeyAction("ALT_T", KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK, condition, this::focusTimePeriod);
        registerKeyAction("ALT_R", KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK, condition, this::focusReasonFilter);
        registerKeyAction("ALT_0", KeyEvent.VK_0, KeyEvent.ALT_DOWN_MASK, condition, this::clearFilters);

        // Help
        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, condition, this::showKeyboardHints);

        // FIXED: Setup search field shortcuts properly
        setupSearchFieldShortcuts();
    }

    /**
     * FIXED: Sets up search field specific shortcuts
     */
    private void setupSearchFieldShortcuts() {
        // Clear search on Escape when search field is focused
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        jTextField1.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                jTextField1.setText("");
                performSearch();
                StockLossPanel.this.requestFocusInWindow();
                showPositionIndicator("Search cleared");
            }
        });

        // Start navigation from search field with Down arrow
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "startNavigation");
        jTextField1.getActionMap().put("startNavigation", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startNavigationFromSearch();
            }
        });

        // Search on Enter key
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "performSearch");
        jTextField1.getActionMap().put("performSearch", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                performSearch();
                StockLossPanel.this.requestFocusInWindow();
                showPositionIndicator("Search performed");
            }
        });
    }

    /**
     * Registers a keyboard action
     */
    private void registerKeyAction(String actionName, int keyCode, int modifiers, int condition, Runnable action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        this.getInputMap(condition).put(keyStroke, actionName);
        this.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (shouldIgnoreKeyAction(keyCode, modifiers)) {
                    return;
                }
                action.run();
            }
        });
    }

    /**
     * Determines if key action should be ignored
     */
    private boolean shouldIgnoreKeyAction(int keyCode, int modifiers) {
        // Don't ignore slash key even when search field is focused
        if (keyCode == KeyEvent.VK_SLASH && modifiers == 0) {
            return false;
        }

        return jTextField1.hasFocus()
                && keyCode != KeyEvent.VK_ESCAPE
                && keyCode != KeyEvent.VK_ENTER
                && modifiers == 0;
    }

    /**
     * Handles Enter key
     */
    private void handleEnterKey() {
        if (currentCardIndex == -1 && !lossCardsList.isEmpty()) {
            navigateCards(KeyEvent.VK_RIGHT);
        }
    }

    /**
     * Handles slash key
     */
    private void handleSlashKey() {
        if (!jTextField1.hasFocus()) {
            focusSearch();
        } else {
            // If already in search field, do nothing (allows typing slash)
            jTextField1.requestFocus();
        }
    }

    /**
     * Clears search and filters
     */
    private void clearSearchAndFilters() {
        jTextField1.setText("");
        sortByDays.setSelectedItem("All Time");
        sortByReason.setSelectedItem("All Reasons");
        performSearch();
        StockLossPanel.this.requestFocusInWindow();
    }

    /**
     * Starts navigation from search field
     */
    private void startNavigationFromSearch() {
        if (!lossCardsList.isEmpty()) {
            StockLossPanel.this.requestFocusInWindow();
            if (currentCardIndex == -1) {
                currentCardIndex = 0;
                selectCurrentCard();
                scrollToCardSmooth(currentCardIndex);
                updatePositionIndicator();
            }
        }
    }

    /**
     * Navigates between cards using arrow keys
     */
    private void navigateCards(int direction) {
        if (lossCardsList.isEmpty()) {
            showPositionIndicator("No stock loss records available");
            return;
        }

        if (currentCardIndex < 0) {
            currentCardIndex = 0;
            selectCurrentCard();
            scrollToCardSmooth(currentCardIndex);
            updatePositionIndicator();
            return;
        }

        int oldIndex = currentCardIndex;
        int newIndex = calculateNewIndex(direction, currentCardIndex, lossCardsList.size());

        if (newIndex != oldIndex) {
            deselectCard(oldIndex);
            currentCardIndex = newIndex;
            selectCurrentCard();
            scrollToCardSmooth(currentCardIndex);
            updatePositionIndicator();
        } else {
            showBoundaryMessage(direction);
        }
    }

    /**
     * Shows message when at navigation boundary
     */
    private void showBoundaryMessage(int direction) {
        String message;
        switch (direction) {
            case KeyEvent.VK_LEFT:
                message = "◀️ Already at the beginning";
                break;
            case KeyEvent.VK_RIGHT:
                message = "▶️ Already at the end";
                break;
            case KeyEvent.VK_UP:
                message = "🔼 Already at the top";
                break;
            case KeyEvent.VK_DOWN:
                message = "🔽 Already at the bottom";
                break;
            default:
                return;
        }
        showPositionIndicator(message);
    }

    /**
     * Calculates new card index based on direction
     */
    private int calculateNewIndex(int direction, int currentIndex, int totalCards) {
        int currentRow = currentIndex / currentColumns;
        int currentCol = currentIndex % currentColumns;
        int totalRows = (int) Math.ceil((double) totalCards / currentColumns);

        switch (direction) {
            case KeyEvent.VK_LEFT:
                return calculateLeftIndex(currentIndex, currentRow, currentCol);

            case KeyEvent.VK_RIGHT:
                return calculateRightIndex(currentIndex, currentRow, totalCards);

            case KeyEvent.VK_UP:
                return calculateUpIndex(currentIndex, currentRow);

            case KeyEvent.VK_DOWN:
                return calculateDownIndex(currentIndex, currentRow, currentCol, totalCards, totalRows);

            default:
                return currentIndex;
        }
    }

    private int calculateLeftIndex(int currentIndex, int currentRow, int currentCol) {
        if (currentCol > 0) {
            return currentIndex - 1;
        } else if (currentRow > 0) {
            return Math.min((currentRow * currentColumns) - 1, lossCardsList.size() - 1);
        }
        return currentIndex;
    }

    private int calculateRightIndex(int currentIndex, int currentRow, int totalCards) {
        if (currentIndex < totalCards - 1) {
            return currentIndex + 1;
        }
        return currentIndex;
    }

    private int calculateUpIndex(int currentIndex, int currentRow) {
        if (currentRow > 0) {
            return Math.max(0, currentIndex - currentColumns);
        }
        return currentIndex;
    }

    private int calculateDownIndex(int currentIndex, int currentRow, int currentCol, int totalCards, int totalRows) {
        int targetIndex = currentIndex + currentColumns;
        if (targetIndex < totalCards) {
            return targetIndex;
        } else {
            int lastRowFirstIndex = (totalRows - 1) * currentColumns;
            int potentialIndex = lastRowFirstIndex + currentCol;

            if (potentialIndex < totalCards && potentialIndex > currentIndex) {
                return potentialIndex;
            }
        }
        return currentIndex;
    }

    /**
     * Updates position indicator with current position
     */
    private void updatePositionIndicator() {
        if (currentCardIndex < 0 || currentCardIndex >= lossCardsList.size()) {
            return;
        }

        int row = (currentCardIndex / currentColumns) + 1;
        int col = (currentCardIndex % currentColumns) + 1;
        int totalRows = (int) Math.ceil((double) lossCardsList.size() / currentColumns);

        String text = String.format("Loss Record %d/%d (Row %d/%d, Col %d) | E: Edit",
                currentCardIndex + 1,
                lossCardsList.size(),
                row,
                totalRows,
                col
        );

        showPositionIndicator(text);
    }

    /**
     * Selects current card visually
     */
    private void selectCurrentCard() {
        if (currentCardIndex < 0 || currentCardIndex >= lossCardsList.size()) {
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = lossCardsList.get(currentCardIndex);

        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(Colors.TEAL_PRIMARY, 4, 15),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        card.setBackground(card.getBackground().brighter());
        currentFocusedCard = card;
    }

    /**
     * Deselects card at index
     */
    private void deselectCard(int index) {
        if (index < 0 || index >= lossCardsList.size()) {
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = lossCardsList.get(index);

        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(Colors.BORDER_DEFAULT, 2, 15),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        card.setBackground(Colors.CARD_WHITE);
    }

    /**
     * Deselects current card
     */
    private void deselectCurrentCard() {
        if (currentFocusedCard != null) {
            deselectCard(currentCardIndex);
            currentFocusedCard = null;
        }
        currentCardIndex = -1;
    }

    /**
     * Scrolls to card smoothly
     */
    private void scrollToCardSmooth(int index) {
        if (index < 0 || index >= lossCardsList.size()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                lk.com.pos.privateclasses.RoundedPanel card = lossCardsList.get(index);

                Point cardLocation = card.getLocation();
                Dimension cardSize = card.getSize();
                Rectangle viewRect = jScrollPane1.getViewport().getViewRect();

                int cardTop = cardLocation.y;
                int cardBottom = cardLocation.y + cardSize.height;
                int viewTop = viewRect.y;
                int viewBottom = viewRect.y + viewRect.height;

                int targetY = calculateScrollTarget(cardTop, cardBottom, viewTop, viewBottom, viewRect.height);

                if (targetY != viewRect.y) {
                    animateScroll(viewRect.x, viewRect.y, targetY);
                }

            } catch (Exception e) {
                System.err.println("Error scrolling to card: " + e.getMessage());
            }
        });
    }

    /**
     * Calculates scroll target Y position
     */
    private int calculateScrollTarget(int cardTop, int cardBottom, int viewTop, int viewBottom, int viewHeight) {
        int targetY = viewTop;

        if (cardTop < viewTop) {
            targetY = Math.max(0, cardTop - 50);
        } else if (cardBottom > viewBottom) {
            targetY = cardBottom - viewHeight + 50;
        } else if (cardTop < viewTop + 100 && cardTop >= viewTop) {
            targetY = Math.max(0, cardTop - 100);
        } else if (cardBottom > viewBottom - 100 && cardBottom <= viewBottom) {
            targetY = cardBottom - viewHeight + 100;
        } else {
            return viewTop;
        }

        return targetY;
    }

    /**
     * Animates scroll to position
     */
    private void animateScroll(int x, int startY, int endY) {
        final int steps = 10;
        final int delay = 15;

        Timer scrollTimer = new Timer(delay, null);
        final int[] step = {0};

        scrollTimer.addActionListener(e -> {
            step[0]++;
            if (step[0] <= steps) {
                double progress = (double) step[0] / steps;
                double easeProgress = 1 - Math.pow(1 - progress, 3);

                int newY = (int) (startY + (endY - startY) * easeProgress);
                jScrollPane1.getViewport().setViewPosition(new Point(x, newY));
            } else {
                scrollTimer.stop();
            }
        });

        scrollTimer.start();
    }

    /**
     * Edits selected card
     */
    private void editSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= lossCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = lossCardsList.get(currentCardIndex);
        Integer lossId = (Integer) card.getClientProperty("lossId");

        if (lossId != null) {
            editStockLoss(lossId);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }

    /**
     * Focuses search field
     */
    private void focusSearch() {
        jTextField1.requestFocus();
        jTextField1.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter records");
    }

    /**
     * Focuses time period combo box
     */
    private void focusTimePeriod() {
        sortByDays.requestFocus();
        sortByDays.showPopup();
        showPositionIndicator("📅 Select time period filter");
    }

    /**
     * Focuses reason filter combo box
     */
    private void focusReasonFilter() {
        sortByReason.requestFocus();
        sortByReason.showPopup();
        showPositionIndicator("📋 Select reason filter");
    }

    /**
     * Handles Escape key
     */
    private void handleEscape() {
        if (currentCardIndex >= 0) {
            deselectCurrentCard();
            showPositionIndicator("Card deselected");
        } else if (!jTextField1.getText().isEmpty()
                || !sortByDays.getSelectedItem().equals("All Time")
                || !sortByReason.getSelectedItem().equals("All Reasons")) {
            jTextField1.setText("");
            sortByDays.setSelectedItem("All Time");
            sortByReason.setSelectedItem("All Reasons");
            performSearch();
            showPositionIndicator("Filters cleared");
        }
        this.requestFocusInWindow();
    }

    /**
     * Refreshes loss records
     */
    private void refreshLosses() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < Business.REFRESH_COOLDOWN_MS) {
            showPositionIndicator("Please wait before refreshing again");
            return;
        }

        lastRefreshTime = currentTime;
        performSearch();
        showPositionIndicator("✅ Stock losses refreshed");
        this.requestFocusInWindow();
    }

    /**
     * Opens loss report
     */
    private void openLossReport() {
        showExportOptions();
        showPositionIndicator("📊 Opening Stock Loss Report");
    }

    /**
     * Opens Add Loss dialog
     */
    private void openAddLossDialog() {
        addNewLostBtn.doClick();
        showPositionIndicator("➕ Opening Add New Loss dialog");
    }

    /**
     * Clears all filters
     */
    private void clearFilters() {
        jTextField1.setText("");
        sortByDays.setSelectedItem("All Time");
        sortByReason.setSelectedItem("All Reasons");
        performSearch();
        showPositionIndicator("All filters cleared");
        this.requestFocusInWindow();
    }

    /**
     * Loads stock losses with filters
     */
    private void loadStockLosses() {
        String searchText = getSearchText();
        String timePeriod = (String) sortByDays.getSelectedItem();
        String reasonFilter = (String) sortByReason.getSelectedItem();

        loadLossesAsync(searchText, timePeriod, reasonFilter);
    }

    /**
     * Gets search text from field
     */
    private String getSearchText() {
        String text = jTextField1.getText().trim();
        return text.equals(Strings.SEARCH_PLACEHOLDER) ? "" : text;
    }

    /**
     * Performs search based on current filters
     */
    private void performSearch() {
        loadStockLosses();
    }

    /**
     * Loads losses asynchronously
     */
    private void loadLossesAsync(String searchText, String timePeriod, String reasonFilter) {
        showLoading(true);

        SwingWorker<List<StockLossDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StockLossDTO> doInBackground() throws Exception {
                return stockLossDAO.getAllStockLosses(searchText, timePeriod, reasonFilter);
            }

            @Override
            protected void done() {
                try {
                    List<StockLossDTO> losses = get();
                    displayLosses(losses);
                } catch (Exception e) {
                    handleLoadError(e);
                } finally {
                    showLoading(false);
                }
            }
        };

        worker.execute();
    }

    /**
     * Handles load error
     */
    private void handleLoadError(Exception e) {
        System.err.println("Error loading stock losses: " + e.getMessage());
        e.printStackTrace();

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Failed to load stock losses. Please try again.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Displays losses in grid
     */
    private void displayLosses(List<StockLossDTO> losses) {
        clearLossCards();

        currentCardIndex = -1;
        currentFocusedCard = null;

        if (losses.isEmpty()) {
            showEmptyState();
            return;
        }

        // Calculate initial columns based on current width
        int panelWidth = jPanel2.getWidth();
        currentColumns = calculateColumns(panelWidth);

        final JPanel gridPanel = createGridPanel();

        for (StockLossDTO data : losses) {
            lk.com.pos.privateclasses.RoundedPanel card = createLossCard(data);
            gridPanel.add(card);
            lossCardsList.add(card);
        }

        layoutCardsInPanel(gridPanel);
        setupGridResizeListener(gridPanel);

        jPanel2.revalidate();
        jPanel2.repaint();
    }

    /**
     * Clears all loss cards
     */
    private void clearLossCards() {
        for (lk.com.pos.privateclasses.RoundedPanel card : lossCardsList) {
            removeAllListeners(card);
        }

        lossCardsList.clear();
        jPanel2.removeAll();
    }

    /**
     * Removes all listeners from component and children
     */
    private void removeAllListeners(Component component) {
        for (java.awt.event.MouseListener ml : component.getMouseListeners()) {
            component.removeMouseListener(ml);
        }

        for (Component child : getAllComponents(component)) {
            if (child instanceof JButton) {
                JButton btn = (JButton) child;
                for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
            }
        }
    }

    /**
     * Gets all components recursively
     */
    private List<Component> getAllComponents(Component container) {
        List<Component> list = new ArrayList<>();
        if (container instanceof java.awt.Container) {
            for (Component comp : ((java.awt.Container) container).getComponents()) {
                list.add(comp);
                if (comp instanceof java.awt.Container) {
                    list.addAll(getAllComponents(comp));
                }
            }
        }
        return list;
    }

    /**
     * Shows empty state message
     */
    private void showEmptyState() {
        jPanel2.setLayout(new BorderLayout());
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        messagePanel.setBackground(Colors.BACKGROUND);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        JLabel noRecords = new JLabel(Strings.NO_RECORDS);
        noRecords.setFont(new java.awt.Font("Nunito SemiBold", 0, 18));
        noRecords.setForeground(Colors.TEXT_SECONDARY);
        noRecords.setHorizontalAlignment(SwingConstants.CENTER);

        messagePanel.add(noRecords);
        jPanel2.add(messagePanel, BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
    }

    /**
     * Creates grid panel for cards
     */
    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, currentColumns, Dimensions.GRID_GAP, Dimensions.GRID_GAP));
        gridPanel.setBackground(Colors.BACKGROUND);
        return gridPanel;
    }

    /**
     * Layouts cards in panel
     */
    private void layoutCardsInPanel(JPanel gridPanel) {
        jPanel2.setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Colors.BACKGROUND);

        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setBackground(Colors.BACKGROUND);
        paddingPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        paddingPanel.add(gridPanel, BorderLayout.NORTH);

        mainContainer.add(paddingPanel);
        jPanel2.add(mainContainer, BorderLayout.NORTH);
    }

    /**
     * Sets up grid resize listener
     */
    private void setupGridResizeListener(final JPanel gridPanel) {
        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int panelWidth = jPanel2.getWidth();
                int newColumns = calculateColumns(panelWidth);

                if (newColumns != currentColumns) {
                    currentColumns = newColumns;
                    gridPanel.setLayout(new GridLayout(0, newColumns, Dimensions.GRID_GAP, Dimensions.GRID_GAP));
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }
        });
    }

    /**
     * Calculates number of columns based on width - RESPONSIVE
     */
    private int calculateColumns(int panelWidth) {
        // Account for padding (25px on each side)
        int availableWidth = panelWidth - 50;

        // Responsive column calculation
        if (availableWidth >= Dimensions.THREE_COLUMN_MIN_WIDTH) {
            return 3;
        } else if (availableWidth >= Dimensions.TWO_COLUMN_MIN_WIDTH) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Custom Rounded Border Class
     */
    class RoundedBorder extends javax.swing.border.AbstractBorder {

        private final Color color;
        private final int thickness;
        private final int arc;

        public RoundedBorder(Color color, int thickness, int arc) {
            this.color = color;
            this.thickness = thickness;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));

            int offset = thickness / 2;
            g2d.drawRoundRect(x + offset, y + offset,
                    width - thickness, height - thickness, arc, arc);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int inset = thickness + 2;
            return new Insets(inset, inset, inset, inset);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int inset = thickness + 2;
            insets.left = insets.right = insets.top = insets.bottom = inset;
            return insets;
        }
    }

    /**
     * Creates loss card from DTO
     */
    private lk.com.pos.privateclasses.RoundedPanel createLossCard(StockLossDTO data) {
        String displayDate = formatLossDate(data.getStockLossDate());

        lk.com.pos.privateclasses.RoundedPanel card = createBaseCard(data.getStockLossId(), data.getProductName());
        JPanel contentPanel = createCardContent(data, displayDate);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Formats loss date for display
     */
    private String formatLossDate(Date lossDate) {
        if (lossDate == null) {
            return Strings.NO_VALUE;
        }

        try {
            return DISPLAY_DATE_FORMAT.format(lossDate);
        } catch (Exception e) {
            return DATE_FORMAT.format(lossDate);
        }
    }

    /**
     * Creates base card panel
     */
    private lk.com.pos.privateclasses.RoundedPanel createBaseCard(int lossId, String productName) {
        lk.com.pos.privateclasses.RoundedPanel card = new lk.com.pos.privateclasses.RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(Dimensions.CARD_SIZE);
        card.setMaximumSize(Dimensions.CARD_MAX_SIZE);
        card.setMinimumSize(Dimensions.CARD_MIN_SIZE);
        card.setBackground(Colors.CARD_WHITE);
        card.setBorderThickness(0);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(Colors.BORDER_DEFAULT, 2, 15),
                BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, Dimensions.CARD_PADDING,
                        Dimensions.CARD_PADDING, Dimensions.CARD_PADDING)
        ));

        card.putClientProperty("lossId", lossId);
        card.putClientProperty("productName", productName);

        addCardMouseListeners(card);

        return card;
    }

    /**
     * Adds mouse listeners to card
     */
    private void addCardMouseListeners(lk.com.pos.privateclasses.RoundedPanel card) {
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                            new RoundedBorder(Colors.TEAL_HOVER, 2, 15),
                            BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, Dimensions.CARD_PADDING,
                                    Dimensions.CARD_PADDING, Dimensions.CARD_PADDING)
                    ));
                }
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                            new RoundedBorder(Colors.BORDER_DEFAULT, 2, 15),
                            BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, Dimensions.CARD_PADDING,
                                    Dimensions.CARD_PADDING, Dimensions.CARD_PADDING)
                    ));
                }
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleCardClick(card);
            }
        });
    }

    /**
     * Handles card click
     */
    private void handleCardClick(lk.com.pos.privateclasses.RoundedPanel card) {
        if (currentFocusedCard != null && currentFocusedCard != card) {
            deselectCurrentCard();
        }

        currentCardIndex = lossCardsList.indexOf(card);
        selectCurrentCard();
        updatePositionIndicator();
        StockLossPanel.this.requestFocusInWindow();
    }

    /**
     * Creates card content panel from DTO
     */
    private JPanel createCardContent(StockLossDTO data, String displayDate) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CARD_WHITE);
        contentPanel.setOpaque(false);

        // Header with product name and edit button
        contentPanel.add(createHeaderSection(data.getStockLossId(), data.getProductName()));
        contentPanel.add(Box.createVerticalStrut(15));

        // Loss details header with reason badge on the right
        contentPanel.add(createDetailsSectionHeader(data.getReason()));
        contentPanel.add(Box.createVerticalStrut(10));

        // Details grid (batch, invoice, price, qty, user, date)
        contentPanel.add(createDetailsGrid(data.getBatchNo(), data.getInvoiceNo(), data.getSellingPrice(),
                data.getQty(), data.getUserName(), displayDate));
        contentPanel.add(Box.createVerticalStrut(20));

        // Loss amount at the bottom
        contentPanel.add(createLossAmountPanel(data.getLossAmount()));

        return contentPanel;
    }

    private JPanel createHeaderSection(int lossId, String productName) {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(Fonts.HEADER);
        nameLabel.setForeground(Colors.TEXT_PRIMARY);
        nameLabel.setToolTipText(productName);
        headerPanel.add(nameLabel, BorderLayout.CENTER);

        headerPanel.add(createActionButtons(lossId), BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Creates action buttons panel
     */
    private JPanel createActionButtons(int lossId) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        JButton editButton = createEditButton(lossId);
        actionPanel.add(editButton);

        return actionPanel;
    }

    /**
     * Creates edit button
     */
    private JButton createEditButton(int lossId) {
        JButton editButton = new JButton();
        editButton.setPreferredSize(Dimensions.ACTION_BUTTON_SIZE);
        editButton.setMinimumSize(Dimensions.ACTION_BUTTON_SIZE);
        editButton.setMaximumSize(Dimensions.ACTION_BUTTON_SIZE);
        editButton.setBackground(Colors.BTN_EDIT_BG);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 16, 16);
            editButton.setIcon(editIcon);
        } catch (Exception e) {
            editButton.setText("✎");
            editButton.setForeground(Color.decode("#3B82F6"));
            editButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        }

        editButton.setBorder(BorderFactory.createLineBorder(Colors.BTN_EDIT_BORDER, 1));
        editButton.setFocusable(false);
        editButton.setToolTipText("Edit Loss Record (E)");
        editButton.addActionListener(e -> {
            editStockLoss(lossId);
            StockLossPanel.this.requestFocusInWindow();
        });

        return editButton;
    }

    /**
     * Creates reason badge section (removed paid/unpaid badges)
     */
    private JPanel createReasonBadgeSection(String reason) {
        JPanel reasonBadgePanel = new JPanel(new BorderLayout(10, 0));
        reasonBadgePanel.setOpaque(false);
        reasonBadgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel reasonBadge = createReasonBadge(reason);
        reasonBadgePanel.add(reasonBadge, BorderLayout.WEST);

        return reasonBadgePanel;
    }

    /**
     * Creates reason badge
     */
    private JLabel createReasonBadge(String reason) {
        JLabel badge = new JLabel(reason != null ? reason : "Unknown");
        badge.setFont(Fonts.BADGE);

        Color bgColor, fgColor, borderColor;

        if (reason != null && reason.toLowerCase().contains("expire")) {
            bgColor = Colors.BADGE_EXPIRED_BG;
            fgColor = Colors.BADGE_EXPIRED_FG;
            borderColor = Colors.BADGE_EXPIRED_BORDER;
        } else if (reason != null && reason.toLowerCase().contains("damage")) {
            bgColor = Colors.BADGE_DAMAGED_BG;
            fgColor = Colors.BADGE_DAMAGED_FG;
            borderColor = Colors.BADGE_DAMAGED_BORDER;
        } else {
            bgColor = Colors.BADGE_BOUNCED_BG;
            fgColor = Colors.BADGE_BOUNCED_FG;
            borderColor = Colors.BADGE_BOUNCED_BORDER;
        }

        badge.setForeground(fgColor);
        badge.setBackground(bgColor);
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        return badge;
    }

    private JPanel createDetailsSectionHeader(String reason) {
        JPanel detailsHeaderPanel = new JPanel(new BorderLayout());
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel detailsHeader = new JLabel(Strings.SECTION_DETAILS);
        detailsHeader.setFont(Fonts.SECTION_TITLE);
        detailsHeader.setForeground(Colors.TEXT_MUTED);
        detailsHeaderPanel.add(detailsHeader, BorderLayout.WEST);

        // Add reason badge on the right
        JLabel reasonBadge = createReasonBadge(reason);
        detailsHeaderPanel.add(reasonBadge, BorderLayout.EAST);

        return detailsHeaderPanel;
    }

    private JPanel createDetailsGrid(String batchNo, String invoiceNo, double unitPrice,
            int quantityLost, String recordedBy, String displayDate) {
        JPanel detailsGrid = new JPanel(new GridLayout(3, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        detailsGrid.add(createDetailPanel("Batch No", batchNo, Colors.DETAIL_BATCH));
        detailsGrid.add(createDetailPanel("Invoice No", invoiceNo, Colors.DETAIL_INVOICE));
        detailsGrid.add(createDetailPanel("Unit Price", formatPrice(unitPrice), Colors.DETAIL_PRICE));
        detailsGrid.add(createDetailPanel("Quantity Lost", String.valueOf(quantityLost), Colors.DETAIL_QTY));
        detailsGrid.add(createDetailPanel("Recorded By", recordedBy, Colors.DETAIL_USER));
        detailsGrid.add(createDetailPanel("Loss Date", displayDate, Colors.DETAIL_DATE));

        return detailsGrid;
    }

    /**
     * Creates detail panel
     */
    private JPanel createDetailPanel(String title, String value, Color accentColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.DETAIL_TITLE);
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        String displayValue = value != null ? value : Strings.NO_VALUE;
        if (displayValue.length() > 20) {
            displayValue = "<html><div style='width:140px;'>" + displayValue + "</div></html>";
        }

        JLabel valueLabel = new JLabel(displayValue);
        valueLabel.setFont(Fonts.DETAIL_VALUE);
        valueLabel.setForeground(Colors.TEXT_PRIMARY);
        valueLabel.setToolTipText(value);
        valueLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);

        return panel;
    }

    /**
     * Creates loss amount panel with red color
     */
    private JPanel createLossAmountPanel(double lossAmount) {
        lk.com.pos.privateclasses.RoundedPanel panel = new lk.com.pos.privateclasses.RoundedPanel();

        Color bgColor = Colors.LOSS_BG;
        Color fgColor = Colors.LOSS_HIGH;
        Color borderColor = Colors.LOSS_BORDER;

        panel.setBackgroundColor(bgColor);
        panel.setBorderColor(borderColor);
        panel.setBorderThickness(2);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Loss Amount");
        titleLabel.setFont(Fonts.SECTION_TITLE);
        titleLabel.setForeground(fgColor);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 0, 0);

        JLabel amountLabel = new JLabel("Rs. " + PRICE_FORMAT.format(lossAmount));
        amountLabel.setFont(Fonts.LOSS_AMOUNT);
        amountLabel.setForeground(fgColor);
        amountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        amountLabel.setToolTipText("Total Loss: Rs." + String.format("%.2f", lossAmount));
        panel.add(amountLabel, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        wrapper.add(panel, BorderLayout.CENTER);

        return wrapper;
    }

    /**
     * Formats price
     */
    private String formatPrice(double price) {
        return "Rs." + PRICE_FORMAT.format(price);
    }

    /**
     * Opens edit stock loss dialog
     */
    private void editStockLoss(int lossId) {
        if (lossId <= 0) {
            System.err.println("Invalid loss ID: " + lossId);
            return;
        }

        // Get the stock loss data from DAO
        StockLossDTO stockLoss = stockLossDAO.getStockLossById(lossId);

        if (stockLoss != null) {
            JOptionPane.showMessageDialog(this,
                    "Edit Stock Loss - ID: " + lossId
                    + "\nProduct: " + stockLoss.getProductName()
                    + "\nReason: " + stockLoss.getReason()
                    + "\nQuantity: " + stockLoss.getQty()
                    + "\nLoss Amount: Rs." + String.format("%.2f", stockLoss.getLossAmount())
                    + "\n\nThis would open an edit dialog for the stock loss record.",
                    "Edit Stock Loss",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Stock loss record not found with ID: " + lossId,
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    /**
     * Shows export options dialog
     */
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
        reportButton.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        reportButton.setBackground(new Color(28, 181, 187));
        reportButton.setForeground(Color.WHITE);
        reportButton.addActionListener(e -> {
            dialog.dispose();
            generateStockLossReport();
        });

        JButton excelButton = new JButton("Export to Excel");
        excelButton.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        excelButton.setBackground(new Color(16, 185, 129));
        excelButton.setForeground(Color.WHITE);
        excelButton.addActionListener(e -> {
            dialog.dispose();
            exportStockLossToExcel();
        });

        buttonPanel.add(reportButton);
        buttonPanel.add(excelButton);

        JLabel titleLabel = new JLabel("Select Export Format", SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        dialog.add(titleLabel, BorderLayout.NORTH);
        dialog.add(buttonPanel, BorderLayout.CENTER);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                showPositionIndicator("Export cancelled");
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Generates stock loss report using JasperReports
     */
    private void generateStockLossReport() {
        try {
            // Set font properties to prevent font errors
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            System.setProperty("net.sf.jasperreports.default.font.name", "Arial");
            System.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");

            // Get current filters
            String searchText = jTextField1.getText().trim();
            String timePeriod = (String) sortByDays.getSelectedItem();
            String reasonFilter = (String) sortByReason.getSelectedItem();

            // Fetch stock loss data
            List<StockLossDTO> stockLosses = stockLossDAO.getAllStockLosses(searchText, timePeriod, reasonFilter);

            if (stockLosses.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No stock loss records found for the selected filters.",
                        "No Data",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create a list of maps for the report data - MATCHING JRXML FIELD NAMES
            List<Map<String, Object>> reportData = new ArrayList<>();
            double totalLossAmount = 0;
            int totalQuantity = 0;

            for (StockLossDTO loss : stockLosses) {
                Map<String, Object> row = new HashMap<>();

                // Match the exact field names from the JRXML
                row.put("productName", loss.getProductName());
                row.put("batchNo", loss.getBatchNo());
                row.put("quantity", String.valueOf(loss.getQty()));
                row.put("recordBy", loss.getUserName());
                row.put("lossDate", DATE_FORMAT.format(loss.getStockLossDate()));
                row.put("lossAmount", "Rs." + String.format("%.2f", loss.getLossAmount()));
                row.put("reason", loss.getReason());

                reportData.add(row);

                // Calculate totals
                totalLossAmount += loss.getLossAmount();
                totalQuantity += loss.getQty();
            }

            // Prepare parameters for the report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Stock Loss Report");
            parameters.put("generatedDate", REPORT_DATE_FORMAT.format(new Date()));
            parameters.put("totalRecords", stockLosses.size());
            parameters.put("totalLossAmount", "Rs." + String.format("%.2f", totalLossAmount));
            parameters.put("totalQuantity", String.valueOf(totalQuantity));
            parameters.put("filterInfo", getStockLossFilterInfo(searchText, timePeriod, reasonFilter));

            // Set default font parameters
            parameters.put("REPORT_FONT", "Arial");
            parameters.put("REPORT_PDF_FONT", "Helvetica");

            // Load the JRXML template from classpath
            InputStream jrxmlStream = getClass().getResourceAsStream("/lk/com/pos/reports/stockLossReport.jrxml");

            if (jrxmlStream == null) {
                // Try alternative path
                jrxmlStream = getClass().getClassLoader().getResourceAsStream("lk/com/pos/reports/stockLossReport.jrxml");
                if (jrxmlStream == null) {
                    // Try to load from file system
                    File jrxmlFile = new File("src/main/resources/lk/com/pos/reports/stockLossReport.jrxml");
                    if (jrxmlFile.exists()) {
                        jrxmlStream = new java.io.FileInputStream(jrxmlFile);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Report template not found. Please ensure stockLossReport.jrxml is in the classpath.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // Compile and fill the report
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Display the report
            JasperViewer.viewReport(jasperPrint, false);

            showPositionIndicator("✅ Stock loss report generated successfully");

        } catch (JRException e) {
            e.printStackTrace();

            // Try with simplified font settings
            try {
                generateStockLossReportWithSimpleFont();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error generating report: " + e.getMessage(),
                        "Report Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fallback method for report generation with simple fonts
     */
    private void generateStockLossReportWithSimpleFont() throws Exception {
        // Create a simple HTML report as fallback
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Stock Loss Report</title></head><body>");
        html.append("<h1>Stock Loss Report</h1>");
        html.append("<p>Generated: ").append(new java.util.Date()).append("</p>");

        // Get current filters
        String searchText = jTextField1.getText().trim();
        String timePeriod = (String) sortByDays.getSelectedItem();
        String reasonFilter = (String) sortByReason.getSelectedItem();

        // Fetch stock loss data
        List<StockLossDTO> stockLosses = stockLossDAO.getAllStockLosses(searchText, timePeriod, reasonFilter);

        if (stockLosses.isEmpty()) {
            html.append("<p>No stock loss records found.</p>");
        } else {
            html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
            html.append("<tr><th>Product Name</th><th>Batch No</th><th>Quantity</th><th>Record By</th><th>Loss Date</th><th>Loss Amount</th><th>Reason</th></tr>");

            double totalLossAmount = 0;
            int totalQuantity = 0;

            for (StockLossDTO loss : stockLosses) {
                html.append("<tr>");
                html.append("<td>").append(loss.getProductName()).append("</td>");
                html.append("<td>").append(loss.getBatchNo()).append("</td>");
                html.append("<td>").append(loss.getQty()).append("</td>");
                html.append("<td>").append(loss.getUserName()).append("</td>");
                html.append("<td>").append(DATE_FORMAT.format(loss.getStockLossDate())).append("</td>");
                html.append("<td>").append("Rs." + String.format("%.2f", loss.getLossAmount())).append("</td>");
                html.append("<td>").append(loss.getReason()).append("</td>");
                html.append("</tr>");

                // Calculate totals
                totalLossAmount += loss.getLossAmount();
                totalQuantity += loss.getQty();
            }

            html.append("</table>");
            html.append("<p><strong>Total Records: ").append(stockLosses.size()).append("</strong></p>");
            html.append("<p><strong>Total Quantity: ").append(totalQuantity).append("</strong></p>");
            html.append("<p><strong>Total Loss Amount: Rs.").append(String.format("%.2f", totalLossAmount)).append("</strong></p>");
        }

        html.append("</body></html>");

        // Display HTML in a dialog
        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
        editorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(900, 600));

        JDialog htmlDialog = new JDialog();
        htmlDialog.setTitle("Stock Loss Report - Simple View");
        htmlDialog.setModal(true);
        htmlDialog.add(scrollPane);
        htmlDialog.pack();
        htmlDialog.setLocationRelativeTo(this);
        htmlDialog.setVisible(true);

        showPositionIndicator("✅ Simple stock loss report generated");
    }

    /**
     * Gets filter info for report
     */
    private String getStockLossFilterInfo(String searchText, String timePeriod, String reasonFilter) {
        StringBuilder filter = new StringBuilder();

        if (!searchText.isEmpty()) {
            filter.append("Search: '").append(searchText).append("' | ");
        }

        filter.append("Time Period: ").append(timePeriod);

        if (!reasonFilter.equals("All Reasons")) {
            filter.append(" | Reason: ").append(reasonFilter);
        }

        return filter.toString();
    }

    /**
     * Exports stock loss data to Excel
     */
    private void exportStockLossToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("stock_loss_report.xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            showPositionIndicator("Export cancelled");
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();

        // Ensure .xlsx extension
        if (!fileToSave.getAbsolutePath().endsWith(".xlsx")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
        }

        try {
            // Get current filters
            String searchText = jTextField1.getText().trim();
            String timePeriod = (String) sortByDays.getSelectedItem();
            String reasonFilter = (String) sortByReason.getSelectedItem();

            // Fetch stock loss data
            List<StockLossDTO> stockLosses = stockLossDAO.getAllStockLosses(searchText, timePeriod, reasonFilter);

            if (stockLosses.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No stock loss records found for the selected filters.",
                        "No Data",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Stock Loss Report");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Product Name", "Batch No", "Quantity", "Record By", "Loss Date", "Loss Amount", "Reason"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            double totalLossAmount = 0;
            int totalQuantity = 0;

            for (StockLossDTO loss : stockLosses) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(loss.getProductName());
                row.createCell(1).setCellValue(loss.getBatchNo());
                row.createCell(2).setCellValue(loss.getQty());
                row.createCell(3).setCellValue(loss.getUserName());
                row.createCell(4).setCellValue(DATE_FORMAT.format(loss.getStockLossDate()));
                row.createCell(5).setCellValue("Rs." + String.format("%.2f", loss.getLossAmount()));
                row.createCell(6).setCellValue(loss.getReason());

                // Calculate totals
                totalLossAmount += loss.getLossAmount();
                totalQuantity += loss.getQty();
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary rows
            int summaryRowNum = rowNum + 2;

            Row summaryHeader = sheet.createRow(summaryRowNum++);
            Cell summaryCell = summaryHeader.createCell(0);
            summaryCell.setCellValue("SUMMARY");
            summaryCell.setCellStyle(headerStyle);

            Row totalRecordsRow = sheet.createRow(summaryRowNum++);
            totalRecordsRow.createCell(0).setCellValue("Total Records:");
            totalRecordsRow.createCell(1).setCellValue(stockLosses.size());

            Row totalQuantityRow = sheet.createRow(summaryRowNum++);
            totalQuantityRow.createCell(0).setCellValue("Total Quantity:");
            totalQuantityRow.createCell(1).setCellValue(totalQuantity);

            Row totalLossRow = sheet.createRow(summaryRowNum++);
            totalLossRow.createCell(0).setCellValue("Total Loss Amount:");
            totalLossRow.createCell(1).setCellValue("Rs." + String.format("%.2f", totalLossAmount));

            Row generatedDateRow = sheet.createRow(summaryRowNum++);
            generatedDateRow.createCell(0).setCellValue("Generated Date:");
            generatedDateRow.createCell(1).setCellValue(REPORT_DATE_FORMAT.format(new Date()));

            Row filterInfoRow = sheet.createRow(summaryRowNum++);
            filterInfoRow.createCell(0).setCellValue("Filters Applied:");
            filterInfoRow.createCell(1).setCellValue(getStockLossFilterInfo(searchText, timePeriod, reasonFilter));

            // Write the output to file
            try (FileOutputStream outputStream = new FileOutputStream(fileToSave)) {
                workbook.write(outputStream);
            }

            workbook.close();

            showPositionIndicator("✅ Excel file saved: " + fileToSave.getName());

            // Ask if user wants to open the file
            int openFile = JOptionPane.showConfirmDialog(this,
                    "Excel file saved successfully!\nDo you want to open it?",
                    "Success",
                    JOptionPane.YES_NO_OPTION);

            if (openFile == JOptionPane.YES_OPTION) {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(fileToSave);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error exporting to Excel: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        addNewLostBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel1 = new javax.swing.JLabel();
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
        jPanel5 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        lostReportBtn = new javax.swing.JButton();
        sortByDays = new javax.swing.JComboBox<>();
        sortByReason = new javax.swing.JComboBox<>();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        addNewLostBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewLostBtn.setText("Add New Lost");
        addNewLostBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewLostBtnActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(248, 250, 252));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20)); // NOI18N
        jLabel1.setText("Product Name");

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jButton8.setBackground(new java.awt.Color(255, 51, 51));
        jButton8.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton8.setText("Bounced");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(2, 0));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel15.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel15.setText("Batch No");

        jLabel20.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 102, 102));
        jLabel20.setText("1236598756");

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
        jLabel21.setText("Invoice No ");

        jLabel26.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(102, 102, 102));
        jLabel26.setText("inv0006");

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
        jLabel27.setText("Unit Price");

        jLabel28.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(102, 102, 102));
        jLabel28.setText("Rs.200");

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
        jLabel29.setText("Quantity Lost");

        jLabel30.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(102, 102, 102));
        jLabel30.setText("10");

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

        jLabel36.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("Lost Amount");

        jLabel35.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(102, 102, 102));
        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel35.setText("Rs.10000");

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                    .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
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

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new java.awt.GridLayout(1, 2));

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));

        jLabel41.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel41.setText("Recored By");

        jLabel42.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(102, 102, 102));
        jLabel42.setText("Hirun");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(20, 20, 20))
                    .addComponent(jLabel42, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(343, 343, 343))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel41)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel42, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.add(jPanel12);

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));

        jLabel22.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel22.setText("Date");

        jLabel43.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(102, 102, 102));
        jLabel43.setText("2025-10-20");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel43, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(295, 295, 295))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel43, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.add(jPanel13);

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(roundedPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator2)
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 96, Short.MAX_VALUE)
                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addGap(8, 8, 8)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(479, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        lostReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        lostReportBtn.setText("Expense Lost");
        lostReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lostReportBtnActionPerformed(evt);
            }
        });

        sortByDays.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        sortByDays.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        sortByDays.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        sortByDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByDaysActionPerformed(evt);
            }
        });

        sortByReason.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        sortByReason.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        sortByReason.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        sortByReason.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByReasonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByDays, 0, 151, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByReason, 0, 157, Short.MAX_VALUE)
                        .addGap(41, 41, 41)
                        .addComponent(lostReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewLostBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addNewLostBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lostReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sortByReason, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sortByDays, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                .addGap(14, 14, 14))
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void addNewLostBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewLostBtnActionPerformed
        AddNewLossStock addNewLossStock = new AddNewLossStock(null, true);
        addNewLossStock.setLocationRelativeTo(null);
        addNewLossStock.setVisible(true);
        performSearch();
    }//GEN-LAST:event_addNewLostBtnActionPerformed

    private void lostReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lostReportBtnActionPerformed

    }//GEN-LAST:event_lostReportBtnActionPerformed

    private void sortByDaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByDaysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortByDaysActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void sortByReasonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByReasonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortByReasonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewLostBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton lostReportBtn;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    private javax.swing.JComboBox<String> sortByDays;
    private javax.swing.JComboBox<String> sortByReason;
    // End of variables declaration//GEN-END:variables
}
