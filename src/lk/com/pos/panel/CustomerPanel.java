package lk.com.pos.panel;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.dao.CustomerDAO;
import lk.com.pos.dto.CustomerDTO;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import lk.com.pos.dialog.AddNewCustomer;
import lk.com.pos.dialog.CreditView;
import lk.com.pos.dialog.UpdateCustomer;
import java.io.InputStream;

/**
 * CustomerPanel - Displays and manages customer information with credit details
 * Features: Search, filters, keyboard navigation, credit tracking, report
 * generation
 *
 * Updated to use CustomerDAO and CustomerDTO with new database connection Added
 * report and export functionality exactly like ChequePanel
 *
 * @author Your Name
 * @version 4.0
 */
public class CustomerPanel extends javax.swing.JPanel {

    // Date & Number Formatting
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    // UI Constants - Colors
    private static final class Colors {

        static final Color TEAL_PRIMARY = new Color(28, 181, 187);
        static final Color TEAL_HOVER = new Color(60, 200, 206);
        static final Color BORDER_DEFAULT = new Color(230, 230, 230);
        static final Color BACKGROUND = Color.decode("#F8FAFC");
        static final Color CARD_WHITE = Color.WHITE;
        static final Color TEXT_PRIMARY = Color.decode("#1E293B");
        static final Color TEXT_SECONDARY = Color.decode("#6B7280");
        static final Color TEXT_MUTED = Color.decode("#94A3B8");

        // Badge Colors
        static final Color BADGE_MISSED_BG = Color.decode("#FED7AA");
        static final Color BADGE_MISSED_FG = Color.decode("#7C2D12");
        static final Color BADGE_MISSED_BORDER = Color.decode("#FB923C");
        static final Color BADGE_HIGH_RISK_BG = Color.decode("#DC2626");
        static final Color BADGE_HIGH_RISK_FG = Color.WHITE;
        static final Color BADGE_HIGH_RISK_BORDER = Color.decode("#FECACA");

        // Payment Panel Colors
        static final Color PAYMENT_DUE_BG = Color.decode("#DBEAFE");
        static final Color PAYMENT_DUE_FG = Color.decode("#1E40AF");
        static final Color PAYMENT_PAID_BG = Color.decode("#D1FAE5");
        static final Color PAYMENT_PAID_FG = Color.decode("#059669");
        static final Color PAYMENT_OUTSTANDING_BG = Color.decode("#FEF3C7");
        static final Color PAYMENT_OUTSTANDING_FG = Color.decode("#92400E");

        // Detail Colors
        static final Color DETAIL_PHONE = Color.decode("#8B5CF6");
        static final Color DETAIL_NIC = Color.decode("#EC4899");
        static final Color DETAIL_DUE_DATE = Color.decode("#10B981");
        static final Color DETAIL_REG_DATE = Color.decode("#06B6D4");
        static final Color DETAIL_STATUS = Color.decode("#6366F1");

        // Button Colors
        static final Color BTN_EDIT_BG = Color.decode("#EFF6FF");
        static final Color BTN_EDIT_BORDER = Color.decode("#BFDBFE");
        static final Color BTN_VIEW_BG = Color.decode("#8B5CF6");
        static final Color BTN_VIEW_BG_HOVER = Color.decode("#7C3AED");
        static final Color BTN_VIEW_BORDER = Color.decode("#7C3AED");
    }

    // UI Constants - Dimensions
    private static final class Dimensions {

        static final Dimension CARD_SIZE = new Dimension(420, 470);
        static final Dimension CARD_MAX_SIZE = new Dimension(420, 470);
        static final Dimension CARD_MIN_SIZE = new Dimension(380, 470);
        static final Dimension ACTION_BUTTON_SIZE = new Dimension(30, 30);
        static final int CARD_WIDTH_WITH_GAP = 445;
        static final int GRID_GAP = 25;
        static final int CARD_PADDING = 16;
    }

    // UI Constants - Fonts
    private static final class Fonts {

        static final java.awt.Font HEADER = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font SECTION_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font BADGE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font BADGE_SMALL = new java.awt.Font("Nunito ExtraBold", 1, 10);
        static final java.awt.Font DETAIL_TITLE = new java.awt.Font("Nunito SemiBold", 0, 13);
        static final java.awt.Font DETAIL_VALUE = new java.awt.Font("Nunito SemiBold", 1, 14);
        static final java.awt.Font STATUS = new java.awt.Font("Nunito SemiBold", 0, 14);
        static final java.awt.Font PAYMENT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 12);
        static final java.awt.Font PAYMENT_AMOUNT = new java.awt.Font("Nunito ExtraBold", 1, 16);
        static final java.awt.Font ADDRESS_TITLE = new java.awt.Font("Nunito SemiBold", 1, 13);
        static final java.awt.Font HINT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 13);
        static final java.awt.Font HINT_KEY = new java.awt.Font("Consolas", 1, 11);
        static final java.awt.Font HINT_DESC = new java.awt.Font("Nunito SemiBold", 0, 11);
        static final java.awt.Font LOADING = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font POSITION = new java.awt.Font("Nunito ExtraBold", 1, 14);
    }

    // UI Constants - Strings
    private static final class Strings {

        static final String SEARCH_PLACEHOLDER = "Search By Customer Name or NIC";
        static final String NO_CUSTOMERS = "No customers found";
        static final String LOADING_MESSAGE = "Loading customers...";
        static final String LOADING_SUBMESSAGE = "Please wait";
        static final String HELP_TITLE = "KEYBOARD SHORTCUTS";
        static final String HELP_CLOSE_HINT = "Press ? to hide";
        static final String SECTION_DETAILS = "CUSTOMER DETAILS";
        static final String SECTION_PAYMENT = "PAYMENT SUMMARY";
        static final String ADDRESS_PREFIX = "Address";
        static final String NO_CREDIT = "No Credit";
        static final String NO_ADDRESS = "No Address";
        static final String NO_VALUE = "N/A";
    }

    // Business Constants
    private static final class Business {

        static final double HIGH_RISK_THRESHOLD = 50000.0;
        static final long REFRESH_COOLDOWN_MS = 1000; // 1 second
    }

    // Keyboard Navigation
    private lk.com.pos.privateclasses.RoundedPanel currentFocusedCard = null;
    private List<lk.com.pos.privateclasses.RoundedPanel> customerCardsList = new ArrayList<>();
    private int currentCardIndex = -1;
    private int currentColumns = 3;

    // UI Components
    private JPanel positionIndicator;
    private JLabel positionLabel;
    private Timer positionTimer;
    private JPanel keyboardHintsPanel;
    private boolean hintsVisible = false;
    private JPanel loadingPanel;
    private JLabel loadingLabel;

    // State
    private long lastRefreshTime = 0;
    private CustomerDAO customerDAO;

    public CustomerPanel() {
        initComponents();
        initializeUI();
        createPositionIndicator();
        createKeyboardHintsPanel();
        createLoadingPanel();
        setupKeyboardShortcuts();

        // Initialize DAO with error handling
        try {
            customerDAO = new CustomerDAO();
            loadCustomers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize database connection: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("Database initialization error: " + e.getMessage());
        }

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
        setupRadioButtons();

        setupButtons();
        setupEventListeners();
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

        // Hide delete button as requested
        if (deleteBtn != null) {
            deleteBtn.setVisible(false);
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

        jTextField1.setToolTipText("Search customers (Ctrl+F or /) - Press ? for help");

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                performSearch();
            }
        });
    }

    /**
     * Configures buttons
     */
    private void setupButtons() {
        setupAddNewCustomerButton();
        setupExportButton();

        // Remove text from buttons
        addNewCoustomerBtn.setText("");
        customerReportBtn.setText("");
    }

    private void setupAddNewCustomerButton() {
        addNewCoustomerBtn.setPreferredSize(new Dimension(47, 47));
        addNewCoustomerBtn.setMinimumSize(new Dimension(47, 47));
        addNewCoustomerBtn.setMaximumSize(new Dimension(47, 47));

        // Set initial state - transparent background with border
        addNewCoustomerBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
        addNewCoustomerBtn.setForeground(Colors.TEAL_PRIMARY);

        // Remove text
        addNewCoustomerBtn.setText("");

        // Set border with teal color
        addNewCoustomerBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set cursor
        addNewCoustomerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remove focus painting
        addNewCoustomerBtn.setFocusPainted(false);

        // Set icon with teal color
        try {
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
            // Apply teal color filter to the icon
            addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Colors.TEAL_PRIMARY));
            addNewCoustomerBtn.setIcon(addIcon);
        } catch (Exception e) {
            System.err.println("Error loading add icon: " + e.getMessage());
        }

        // Set tooltip
        addNewCoustomerBtn.setToolTipText("Add New Customer");

        // Add hover effects
        addNewCoustomerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addNewCoustomerBtn.setBackground(Colors.TEAL_PRIMARY);
                addNewCoustomerBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.TEAL_HOVER, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon to white on hover
                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    addNewCoustomerBtn.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                // Update tooltip to show it's clickable
                addNewCoustomerBtn.setToolTipText("Add New Customer (Ctrl+N or Alt+A)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addNewCoustomerBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
                addNewCoustomerBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon back to teal
                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Colors.TEAL_PRIMARY));
                    addNewCoustomerBtn.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                // Reset tooltip
                addNewCoustomerBtn.setToolTipText("Add New Customer");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                addNewCoustomerBtn.setBackground(Colors.TEAL_PRIMARY.darker());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addNewCoustomerBtn.setBackground(Colors.TEAL_PRIMARY);
            }
        });
    }

    private void setupExportButton() {
        customerReportBtn.setPreferredSize(new Dimension(47, 47));
        customerReportBtn.setMinimumSize(new Dimension(47, 47));
        customerReportBtn.setMaximumSize(new Dimension(47, 47));

        // Set initial state - transparent background with border
        customerReportBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
        customerReportBtn.setForeground(Color.decode("#10B981"));

        // Remove text
        customerReportBtn.setText("");

        // Set border with green color
        customerReportBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set cursor
        customerReportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remove focus painting
        customerReportBtn.setFocusPainted(false);

        // Set icon with green color
        try {
            FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
            // Apply green color filter to the icon
            printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#10B981")));
            customerReportBtn.setIcon(printIcon);
        } catch (Exception e) {
            System.err.println("Error loading print icon: " + e.getMessage());
        }

        // Set tooltip
        customerReportBtn.setToolTipText("Export Customer Report");

        // Add hover effects
        customerReportBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                customerReportBtn.setBackground(Color.decode("#10B981"));
                customerReportBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#34D399"), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon to white on hover
                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    customerReportBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                // Update tooltip to show it's clickable
                customerReportBtn.setToolTipText("Export Customer Report (Ctrl+R or Ctrl+P)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                customerReportBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
                customerReportBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon back to green
                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#10B981")));
                    customerReportBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                // Reset tooltip
                customerReportBtn.setToolTipText("Export Customer Report");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                customerReportBtn.setBackground(Color.decode("#059669"));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                customerReportBtn.setBackground(Color.decode("#10B981"));
            }
        });
    }

    /**
     * Configures radio buttons
     */
    private void setupRadioButtons() {
        jRadioButton1.putClientProperty(FlatClientProperties.STYLE, "foreground:#EF4444;");
        jRadioButton2.putClientProperty(FlatClientProperties.STYLE, "foreground:#6366F1;");
        jRadioButton4.putClientProperty(FlatClientProperties.STYLE, "foreground:#F97316;");

        jRadioButton4.setToolTipText("Filter due amount customers (Alt+1)");
        jRadioButton1.setToolTipText("Filter missed due date customers (Alt+2)");
        jRadioButton2.setToolTipText("Filter no due customers (Alt+3)");

        // Set "Due Amount" as default
        jRadioButton4.setSelected(true);

        // Add action listeners
        jRadioButton1.addActionListener(evt -> onFilterChanged());
        jRadioButton2.addActionListener(evt -> onFilterChanged());
        jRadioButton4.addActionListener(evt -> onFilterChanged());

        // Toggle on click if already selected
        setupRadioButtonToggle(jRadioButton1);
        setupRadioButtonToggle(jRadioButton2);
        setupRadioButtonToggle(jRadioButton4);
    }

    /**
     * Sets up toggle behavior for radio button
     */
    private void setupRadioButtonToggle(javax.swing.JRadioButton radioBtn) {
        radioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (radioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    performSearch();
                    evt.consume();
                }
            }
        });
    }

    /**
     * Sets up main panel
     */
    private void setupPanel() {
        jPanel2.setBackground(Colors.BACKGROUND);
    }

    /**
     * Sets up event listeners
     */
    private void setupEventListeners() {
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> CustomerPanel.this.requestFocusInWindow());
            }
        });
    }

    /**
     * Called when filter changes
     */
    private void onFilterChanged() {
        performSearch();
        this.requestFocusInWindow();
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

        loadingLabel = new JLabel(Strings.LOADING_MESSAGE);
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
        addHintRow("V", "View Credit Details", "#FCD34D");
        addHintRow("E", "Edit Customer", "#1CB5BB");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("Ctrl+N/Alt+A", "Add Customer", "#60D5F2");
        addHintRow("Ctrl+R", "Customer Report", "#10B981");
        addHintRow("Ctrl+P", "Customer Report", "#10B981");
        addHintRow("F5", "Refresh Data", "#06B6D4");
        addHintRow("Alt+1", "Due Amount (Default)", "#FB923C");
        addHintRow("Alt+2", "Missed Due Date", "#EF4444");
        addHintRow("Alt+3", "No Due", "#6366F1");
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
        registerKeyAction("V", KeyEvent.VK_V, 0, condition, this::viewCreditForSelectedCard);
        registerKeyAction("E", KeyEvent.VK_E, 0, condition, this::editSelectedCard);

        // Enter to start navigation
        registerKeyAction("ENTER", KeyEvent.VK_ENTER, 0, condition, this::handleEnterKey);

        // Search
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, this::focusSearch);
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, this::handleSlashKey);

        // Escape
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, this::handleEscape);

        // Refresh
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, this::refreshCustomers);

        // Customer Report
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::openCustomerReport);
        registerKeyAction("CTRL_P", KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, condition, this::openCustomerReport);

        // Add New Customer shortcuts
        registerKeyAction("CTRL_N", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, condition, this::openAddCustomerDialog);
        registerKeyAction("ALT_A", KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, condition, this::openAddCustomerDialog);

        // Quick filters
        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton4));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton1));
        registerKeyAction("ALT_3", KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton2));
        registerKeyAction("ALT_0", KeyEvent.VK_0, KeyEvent.ALT_DOWN_MASK, condition, this::clearFilters);

        // Help
        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, condition, this::showKeyboardHints);

        setupSearchFieldShortcuts();
    }

    /**
     * Sets up search field specific shortcuts
     */
    private void setupSearchFieldShortcuts() {
        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        jTextField1.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearSearchAndFilters();
            }
        });

        jTextField1.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "startNavigation");
        jTextField1.getActionMap().put("startNavigation", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startNavigationFromSearch();
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
        return jTextField1.hasFocus()
                && keyCode != KeyEvent.VK_ESCAPE
                && keyCode != KeyEvent.VK_ENTER
                && modifiers == 0
                && keyCode != KeyEvent.VK_SLASH;
    }

    /**
     * Handles Enter key
     */
    private void handleEnterKey() {
        if (currentCardIndex == -1 && !customerCardsList.isEmpty()) {
            navigateCards(KeyEvent.VK_RIGHT);
        }
    }

    /**
     * Handles slash key
     */
    private void handleSlashKey() {
        if (!jTextField1.hasFocus()) {
            focusSearch();
        }
    }

    /**
     * Clears search and filters
     */
    private void clearSearchAndFilters() {
        jTextField1.setText("");
        buttonGroup1.clearSelection();
        performSearch();
        CustomerPanel.this.requestFocusInWindow();
    }

    /**
     * Starts navigation from search field
     */
    private void startNavigationFromSearch() {
        if (!customerCardsList.isEmpty()) {
            CustomerPanel.this.requestFocusInWindow();
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
        if (customerCardsList.isEmpty()) {
            showPositionIndicator("No customers available");
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
        int newIndex = calculateNewIndex(direction, currentCardIndex, customerCardsList.size());

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
            return Math.min((currentRow * currentColumns) - 1, customerCardsList.size() - 1);
        }
        return currentIndex;
    }

    private int calculateRightIndex(int currentIndex, int currentRow, int totalCards) {
        if (currentIndex < totalCards - 1) {
            int nextIndex = currentIndex + 1;
            int nextRow = nextIndex / currentColumns;
            return nextIndex;
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
        if (currentCardIndex < 0 || currentCardIndex >= customerCardsList.size()) {
            return;
        }

        int row = (currentCardIndex / currentColumns) + 1;
        int col = (currentCardIndex % currentColumns) + 1;
        int totalRows = (int) Math.ceil((double) customerCardsList.size() / currentColumns);

        String text = String.format("Card %d/%d (Row %d/%d, Col %d) | V: View Credits | E: Edit",
                currentCardIndex + 1,
                customerCardsList.size(),
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
        if (currentCardIndex < 0 || currentCardIndex >= customerCardsList.size()) {
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = customerCardsList.get(currentCardIndex);

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
        if (index < 0 || index >= customerCardsList.size()) {
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = customerCardsList.get(index);

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
        if (index < 0 || index >= customerCardsList.size()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                lk.com.pos.privateclasses.RoundedPanel card = customerCardsList.get(index);

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
            return viewTop; // No scroll needed
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
     * Views credit details for selected card
     */
    private void viewCreditForSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= customerCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = customerCardsList.get(currentCardIndex);
        Integer customerId = (Integer) card.getClientProperty("customerId");

        if (customerId != null) {
            showPaymentDetails(customerId);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }

    /**
     * Edits selected card
     */
    private void editSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= customerCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = customerCardsList.get(currentCardIndex);
        Integer customerId = (Integer) card.getClientProperty("customerId");

        if (customerId != null) {
            editCustomer(customerId);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }

    /**
     * Focuses search field
     */
    private void focusSearch() {
        jTextField1.requestFocus();
        jTextField1.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter customers (Press ↓ to navigate results)");
    }

    /**
     * Handles Escape key
     */
    private void handleEscape() {
        if (currentCardIndex >= 0) {
            deselectCurrentCard();
            showPositionIndicator("Card deselected");
        } else if (!jTextField1.getText().isEmpty() || buttonGroup1.getSelection() != null) {
            jTextField1.setText("");
            buttonGroup1.clearSelection();
            performSearch();
            showPositionIndicator("Filters cleared");
        }
        this.requestFocusInWindow();
    }

    /**
     * Refreshes customer list
     */
    private void refreshCustomers() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < Business.REFRESH_COOLDOWN_MS) {
            showPositionIndicator("Please wait before refreshing again");
            return;
        }

        lastRefreshTime = currentTime;
        performSearch();
        showPositionIndicator("✅ Customers refreshed");
        this.requestFocusInWindow();
    }

    /**
     * Opens customer report
     */
    private void openCustomerReport() {
        showExportOptions();
        showPositionIndicator("📊 Opening Customer Report Options");
    }

    /**
     * Opens Add Customer dialog
     */
    private void openAddCustomerDialog() {
        addNewCoustomerBtn.doClick();
        showPositionIndicator("➕ Opening Add New Customer dialog");
    }

    /**
     * Clears all filters
     */
    private void clearFilters() {
        buttonGroup1.clearSelection();
        performSearch();
        showPositionIndicator("All filters cleared");
        this.requestFocusInWindow();
    }

    /**
     * Toggles radio button state
     */
    private void toggleRadioButton(javax.swing.JRadioButton radioBtn) {
        if (radioBtn.isSelected()) {
            buttonGroup1.clearSelection();
            showPositionIndicator("Filter removed: " + radioBtn.getText());
        } else {
            radioBtn.setSelected(true);
            showPositionIndicator("Filter applied: " + radioBtn.getText());
        }
        performSearch();
        this.requestFocusInWindow();
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
            generateCustomerReport();
        });

        JButton excelButton = new JButton("Export to Excel");
        excelButton.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        excelButton.setBackground(new Color(16, 185, 129));
        excelButton.setForeground(Color.WHITE);
        excelButton.addActionListener(e -> {
            dialog.dispose();
            exportCustomerToExcel();
        });

        buttonPanel.add(reportButton);
        buttonPanel.add(excelButton);

        JLabel titleLabel = new JLabel("Select Export Format", SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16));
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
    }

    /**
     * Generates customer report using JasperReports
     */
    private void generateCustomerReport() {
        try {
            // Set font properties to prevent font errors
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            System.setProperty("net.sf.jasperreports.default.font.name", "Arial");
            System.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");

            // Get current filters
            String searchText = getSearchText();
            boolean missedDueDateOnly = jRadioButton1.isSelected();
            boolean noDueOnly = jRadioButton2.isSelected();
            boolean dueAmountOnly = jRadioButton4.isSelected();

            // Fetch customer data
            List<CustomerDTO> customers = customerDAO.searchCustomers(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly);

            if (customers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No customers found for the selected filters.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create a list of maps for the report data - MATCHING JRXML FIELD NAMES
            List<Map<String, Object>> reportData = new ArrayList<>();
            double totalCredit = 0;
            double totalPaid = 0;
            double totalOutstanding = 0;

            for (CustomerDTO customer : customers) {
                Map<String, Object> row = new HashMap<>();
                
                // Match the exact field names from the JRXML
                row.put("nicNo", customer.getNic() != null ? customer.getNic() : "N/A");
                row.put("name", customer.getCustomerName());
                row.put("address", customer.getAddress() != null ? customer.getAddress() : "N/A");
                row.put("dueDate", customer.getLatestDueDate() != null ? 
                        formatReportDate(customer.getLatestDueDate()) : "N/A");
                row.put("phoneNo", formatPhoneNumber(customer.getPhone()));
                row.put("amountDue", formatAmount(customer.getTotalCreditAmount()));
                row.put("outStanding", formatAmount(customer.getTotalCreditAmount() - customer.getTotalPaid()));
                
                reportData.add(row);

                // Calculate totals
                totalCredit += customer.getTotalCreditAmount();
                totalPaid += customer.getTotalPaid();
                totalOutstanding += (customer.getTotalCreditAmount() - customer.getTotalPaid());
            }

            // Prepare parameters for the report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Customer Report");
            parameters.put("generatedDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            parameters.put("totalCustomers", customers.size());
            parameters.put("totalCredit", formatAmount(totalCredit));
            parameters.put("totalPaid", formatAmount(totalPaid));
            parameters.put("totalOutstanding", formatAmount(totalOutstanding));
            parameters.put("filterInfo", getCustomerFilterInfo(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly));
            
            // Set default font parameters
            parameters.put("REPORT_FONT", "Arial");
            parameters.put("REPORT_PDF_FONT", "Helvetica");

            // Load the JRXML template from classpath
            InputStream jrxmlStream = getClass().getResourceAsStream("/lk/com/pos/reports/customerReport.jrxml");
            
            if (jrxmlStream == null) {
                // Try alternative path
                jrxmlStream = getClass().getClassLoader().getResourceAsStream("lk/com/pos/reports/customerReport.jrxml");
                if (jrxmlStream == null) {
                    JOptionPane.showMessageDialog(this,
                            "Report template not found. Please ensure customerReport.jrxml is in the classpath.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Compile and fill the report
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Display the report
            JasperViewer.viewReport(jasperPrint, false);

            showPositionIndicator("✅ Customer report generated successfully");

        } catch (JRException e) {
            e.printStackTrace();
            
            // Try with simplified font settings
            try {
                generateCustomerReportWithSimpleFont();
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
    private void generateCustomerReportWithSimpleFont() throws Exception {
        JOptionPane.showMessageDialog(this,
                "Creating a simple report with default fonts...",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);

        // Create a simple HTML report as fallback
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Customer Report</title></head><body>");
        html.append("<h1>Customer Report</h1>");
        html.append("<p>Generated: ").append(new java.util.Date()).append("</p>");

        // Get current filters
        String searchText = getSearchText();
        boolean missedDueDateOnly = jRadioButton1.isSelected();
        boolean noDueOnly = jRadioButton2.isSelected();
        boolean dueAmountOnly = jRadioButton4.isSelected();

        // Fetch customer data
        List<CustomerDTO> customers = customerDAO.searchCustomers(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly);

        if (customers.isEmpty()) {
            html.append("<p>No customers found.</p>");
        } else {
            html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
            html.append("<tr><th>Name</th><th>Phone</th><th>NIC</th><th>Due Date</th><th>Address</th><th>Amount Due</th><th>Outstanding</th></tr>");

            double totalCredit = 0;
            double totalPaid = 0;
            double totalOutstanding = 0;

            for (CustomerDTO customer : customers) {
                double outstanding = customer.getTotalCreditAmount() - customer.getTotalPaid();
                html.append("<tr>");
                html.append("<td>").append(customer.getCustomerName()).append("</td>");
                html.append("<td>").append(formatPhoneNumber(customer.getPhone())).append("</td>");
                html.append("<td>").append(customer.getNic()).append("</td>");
                html.append("<td>").append(formatReportDate(customer.getLatestDueDate())).append("</td>");
                html.append("<td>").append(customer.getAddress() != null ? customer.getAddress() : "N/A").append("</td>");
                html.append("<td>").append(formatAmount(customer.getTotalCreditAmount())).append("</td>");
                html.append("<td>").append(formatAmount(outstanding)).append("</td>");
                html.append("</tr>");

                totalCredit += customer.getTotalCreditAmount();
                totalPaid += customer.getTotalPaid();
                totalOutstanding += outstanding;
            }

            html.append("</table>");
            html.append("<p><strong>Total Customers: ").append(customers.size()).append("</strong></p>");
            html.append("<p><strong>Total Credit Amount: ").append(formatAmount(totalCredit)).append("</strong></p>");
            html.append("<p><strong>Total Paid: ").append(formatAmount(totalPaid)).append("</strong></p>");
            html.append("<p><strong>Total Outstanding: ").append(formatAmount(totalOutstanding)).append("</strong></p>");
        }

        html.append("</body></html>");

        // Display HTML in a dialog
        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
        editorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        JDialog htmlDialog = new JDialog();
        htmlDialog.setTitle("Customer Report - Simple View");
        htmlDialog.setModal(true);
        htmlDialog.add(scrollPane);
        htmlDialog.pack();
        htmlDialog.setLocationRelativeTo(this);
        htmlDialog.setVisible(true);

        showPositionIndicator("✅ Simple customer report generated");
    }

    /**
     * Formats date for report
     */
    private String formatReportDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        try {
            return new SimpleDateFormat("dd/MM/yyyy").format(date);
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    /**
     * Formats amount for display
     */
    private String formatAmount(double amount) {
        return String.format("Rs. %.2f", amount);
    }

    /**
     * Gets filter info for report
     */
    private String getCustomerFilterInfo(String searchText, boolean missedDueDateOnly,
            boolean noDueOnly, boolean dueAmountOnly) {
        StringBuilder filter = new StringBuilder();
        
        if (!searchText.isEmpty()) {
            filter.append("Search: '").append(searchText).append("' | ");
        }
        
        if (missedDueDateOnly) {
            filter.append("Filter: Missed Due Date");
        } else if (noDueOnly) {
            filter.append("Filter: No Due");
        } else if (dueAmountOnly) {
            filter.append("Filter: Due Amount");
        } else {
            filter.append("Filter: All Customers");
        }
        
        return filter.toString();
    }

    /**
     * Exports customer data to Excel
     */
    private void exportCustomerToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("customer_report.xlsx"));

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
            String searchText = getSearchText();
            boolean missedDueDateOnly = jRadioButton1.isSelected();
            boolean noDueOnly = jRadioButton2.isSelected();
            boolean dueAmountOnly = jRadioButton4.isSelected();

            // Fetch customer data
            List<CustomerDTO> customers = customerDAO.searchCustomers(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly);

            if (customers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No customers found for the selected filters.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Customer Report");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Name", "Phone No", "NIC No", "Due Date",
                "Address", "Amount Due", "Paid Amount", "Outstanding", "Status"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            double totalCredit = 0;
            double totalPaid = 0;
            double totalOutstanding = 0;

            for (CustomerDTO customer : customers) {
                double outstanding = customer.getTotalCreditAmount() - customer.getTotalPaid();
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(customer.getCustomerName());
                row.createCell(1).setCellValue(formatPhoneNumber(customer.getPhone()));
                row.createCell(2).setCellValue(customer.getNic());
                row.createCell(3).setCellValue(formatReportDate(customer.getLatestDueDate()));
                row.createCell(4).setCellValue(customer.getAddress() != null ? customer.getAddress() : "N/A");
                row.createCell(5).setCellValue(customer.getTotalCreditAmount());
                row.createCell(6).setCellValue(customer.getTotalPaid());
                row.createCell(7).setCellValue(outstanding);
                row.createCell(8).setCellValue(customer.getStatus());

                // Update totals
                totalCredit += customer.getTotalCreditAmount();
                totalPaid += customer.getTotalPaid();
                totalOutstanding += outstanding;
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

            Row totalCustomersRow = sheet.createRow(summaryRowNum++);
            totalCustomersRow.createCell(0).setCellValue("Total Customers:");
            totalCustomersRow.createCell(1).setCellValue(customers.size());

            Row totalCreditRow = sheet.createRow(summaryRowNum++);
            totalCreditRow.createCell(0).setCellValue("Total Credit Amount:");
            totalCreditRow.createCell(1).setCellValue(totalCredit);

            Row totalPaidRow = sheet.createRow(summaryRowNum++);
            totalPaidRow.createCell(0).setCellValue("Total Paid Amount:");
            totalPaidRow.createCell(1).setCellValue(totalPaid);

            Row totalOutstandingRow = sheet.createRow(summaryRowNum++);
            totalOutstandingRow.createCell(0).setCellValue("Total Outstanding:");
            totalOutstandingRow.createCell(1).setCellValue(totalOutstanding);

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

    /**
     * Loads customers with default filters
     */
    private void loadCustomers() {
        String searchText = getSearchText();
        boolean missedDueDateOnly = jRadioButton1.isSelected();
        boolean noDueOnly = jRadioButton2.isSelected();
        boolean dueAmountOnly = jRadioButton4.isSelected();

        loadCustomersAsync(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly);
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
        loadCustomers();
    }

    /**
     * Loads customers asynchronously using CustomerDAO
     */
    private void loadCustomersAsync(String searchText, boolean missedDueDateOnly,
            boolean noDueOnly, boolean dueAmountOnly) {
        showLoading(true);

        SwingWorker<List<CustomerDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CustomerDTO> doInBackground() throws Exception {
                return customerDAO.searchCustomers(searchText, missedDueDateOnly, noDueOnly, dueAmountOnly);
            }

            @Override
            protected void done() {
                try {
                    List<CustomerDTO> customers = get();
                    displayCustomers(customers);
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
        System.err.println("Error loading customers: " + e.getMessage());
        e.printStackTrace();

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Failed to load customers. Please try again.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Displays customers in grid using CustomerDTO
     */
    private void displayCustomers(List<CustomerDTO> customers) {
        clearCustomerCards();

        currentCardIndex = -1;
        currentFocusedCard = null;

        if (customers.isEmpty()) {
            showEmptyState();
            return;
        }

        currentColumns = calculateColumns(jPanel2.getWidth());
        final JPanel gridPanel = createGridPanel();

        for (CustomerDTO customer : customers) {
            lk.com.pos.privateclasses.RoundedPanel card = createCustomerCard(customer);
            gridPanel.add(card);
            customerCardsList.add(card);
        }

        layoutCardsInPanel(gridPanel);
        setupGridResizeListener(gridPanel);

        jPanel2.revalidate();
        jPanel2.repaint();
    }

    /**
     * Clears all customer cards
     */
    private void clearCustomerCards() {
        for (lk.com.pos.privateclasses.RoundedPanel card : customerCardsList) {
            removeAllListeners(card);
        }

        customerCardsList.clear();
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

        JLabel noCustomers = new JLabel(Strings.NO_CUSTOMERS);
        noCustomers.setFont(new java.awt.Font("Nunito SemiBold", 0, 18));
        noCustomers.setForeground(Colors.TEXT_SECONDARY);
        noCustomers.setHorizontalAlignment(SwingConstants.CENTER);

        messagePanel.add(noCustomers);
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
     * Calculates number of columns based on width
     */
    private int calculateColumns(int panelWidth) {
        int availableWidth = panelWidth - 50;

        if (availableWidth >= Dimensions.CARD_WIDTH_WITH_GAP * 3) {
            return 3;
        } else if (availableWidth >= Dimensions.CARD_WIDTH_WITH_GAP * 2) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Custom Rounded Border Class
     */
    class RoundedBorder extends AbstractBorder {

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
     * Creates customer card from CustomerDTO
     */
    private lk.com.pos.privateclasses.RoundedPanel createCustomerCard(CustomerDTO customer) {
        // Calculate outstanding and dates
        double outstanding = customer.getTotalCreditAmount() - customer.getTotalPaid();
        boolean missedDueDate = customer.isMissedDueDate();
        String displayDueDate = formatDueDate(customer.getLatestDueDate());
        String regDate = formatRegistrationDate(customer.getRegistrationDate());

        // Create card
        lk.com.pos.privateclasses.RoundedPanel card = createBaseCard(customer.getCustomerId(), customer.getCustomerName());

        // Create content
        JPanel contentPanel = createCardContent(customer, outstanding, missedDueDate, displayDueDate, regDate);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Checks if due date is missed
     */
    private boolean checkMissedDueDate(Date finalDate, double outstanding) {
        if (finalDate == null || outstanding <= 0) {
            return false;
        }

        try {
            Date today = new Date();
            return finalDate.before(today);
        } catch (Exception e) {
            System.err.println("Error checking due date: " + e.getMessage());
            return false;
        }
    }

    /**
     * Formats due date for display
     */
    private String formatDueDate(Date finalDate) {
        if (finalDate == null) {
            return Strings.NO_CREDIT;
        }

        try {
            return DISPLAY_DATE_FORMAT.format(finalDate);
        } catch (Exception e) {
            System.err.println("Error formatting due date: " + e.getMessage());
            return Strings.NO_VALUE;
        }
    }

    /**
     * Formats registration date for display
     */
    private String formatRegistrationDate(Date registrationDate) {
        if (registrationDate == null) {
            return Strings.NO_VALUE;
        }

        try {
            return DISPLAY_DATE_FORMAT.format(registrationDate);
        } catch (Exception e) {
            System.err.println("Error formatting registration date: " + e.getMessage());
            return Strings.NO_VALUE;
        }
    }

    /**
     * Creates base card panel
     */
    private lk.com.pos.privateclasses.RoundedPanel createBaseCard(int customerId, String customerName) {
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

        // Store properties
        card.putClientProperty("customerId", customerId);
        card.putClientProperty("customerName", customerName);

        // Add mouse listeners
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

        currentCardIndex = customerCardsList.indexOf(card);
        selectCurrentCard();
        updatePositionIndicator();
        CustomerPanel.this.requestFocusInWindow();
    }

    /**
     * Creates card content panel using CustomerDTO
     */
    private JPanel createCardContent(CustomerDTO customer, double outstanding,
            boolean missedDueDate, String displayDueDate, String regDate) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CARD_WHITE);
        contentPanel.setOpaque(false);

        // Add sections
        contentPanel.add(createHeaderSection(customer.getCustomerId(), customer.getCustomerName()));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createStatusBadgeSection(customer.getStatus(), missedDueDate, outstanding));
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(createDetailsSectionHeader());
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(createDetailsGrid(customer.getPhone(), customer.getNic(), displayDueDate, regDate));
        contentPanel.add(Box.createVerticalStrut(20));

        if (hasAddress(customer.getAddress())) {
            contentPanel.add(createAddressSection(customer.getAddress()));
            contentPanel.add(Box.createVerticalStrut(15));
        }

        contentPanel.add(createPaymentSectionHeader(customer.getCustomerId()));
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(createPaymentPanels(customer.getTotalCreditAmount(), customer.getTotalPaid(), outstanding));

        return contentPanel;
    }

    /**
     * Creates header section
     */
    private JPanel createHeaderSection(int customerId, String customerName) {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel nameLabel = new JLabel(customerName);
        nameLabel.setFont(Fonts.HEADER);
        nameLabel.setForeground(Colors.TEXT_PRIMARY);
        nameLabel.setToolTipText(customerName);
        headerPanel.add(nameLabel, BorderLayout.CENTER);

        headerPanel.add(createActionButtons(customerId), BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Creates action buttons panel
     */
    private JPanel createActionButtons(int customerId) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        JButton editButton = createEditButton(customerId);
        actionPanel.add(editButton);

        return actionPanel;
    }

    /**
     * Creates edit button
     */
    private JButton createEditButton(int customerId) {
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
        editButton.setToolTipText("Edit Customer (E)");
        editButton.addActionListener(e -> {
            editCustomer(customerId);
            CustomerPanel.this.requestFocusInWindow();
        });

        return editButton;
    }

    /**
     * Creates status and badge section
     */
    private JPanel createStatusBadgeSection(String status, boolean missedDueDate, double outstanding) {
        JPanel statusBadgePanel = new JPanel(new BorderLayout(10, 0));
        statusBadgePanel.setOpaque(false);
        statusBadgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(Fonts.STATUS);
        statusLabel.setForeground(Colors.DETAIL_STATUS);
        statusLabel.setToolTipText("Status: " + status);
        statusBadgePanel.add(statusLabel, BorderLayout.WEST);

        JPanel badgePanel = createBadgesPanel(missedDueDate, outstanding);
        statusBadgePanel.add(badgePanel, BorderLayout.EAST);

        return statusBadgePanel;
    }

    /**
     * Creates badges panel
     */
    private JPanel createBadgesPanel(boolean missedDueDate, double outstanding) {
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        badgePanel.setOpaque(false);

        if (missedDueDate) {
            badgePanel.add(createMissedDueDateBadge());
        }

        if (outstanding > Business.HIGH_RISK_THRESHOLD) {
            badgePanel.add(createHighRiskBadge());
        }

        return badgePanel;
    }

    /**
     * Creates missed due date badge
     */
    private JLabel createMissedDueDateBadge() {
        JLabel badge = new JLabel("Missed Due Date");
        badge.setFont(Fonts.BADGE);
        badge.setForeground(Colors.BADGE_MISSED_FG);
        badge.setBackground(Colors.BADGE_MISSED_BG);
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BADGE_MISSED_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return badge;
    }

    /**
     * Creates high risk badge
     */
    private JLabel createHighRiskBadge() {
        JLabel badge = new JLabel("High Risk");
        badge.setFont(Fonts.BADGE_SMALL);
        badge.setForeground(Colors.BADGE_HIGH_RISK_FG);
        badge.setBackground(Colors.BADGE_HIGH_RISK_BG);
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BADGE_HIGH_RISK_BORDER, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }

    /**
     * Creates details section header
     */
    private JPanel createDetailsSectionHeader() {
        JPanel detailsHeaderPanel = new JPanel(new BorderLayout());
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel detailsHeader = new JLabel(Strings.SECTION_DETAILS);
        detailsHeader.setFont(Fonts.SECTION_TITLE);
        detailsHeader.setForeground(Colors.TEXT_MUTED);
        detailsHeaderPanel.add(detailsHeader, BorderLayout.WEST);

        return detailsHeaderPanel;
    }

    /**
     * Creates details grid
     */
    private JPanel createDetailsGrid(String phone, String nic, String displayDueDate, String regDate) {
        JPanel detailsGrid = new JPanel(new GridLayout(2, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        detailsGrid.add(createDetailPanel("Phone", formatPhoneNumber(phone), Colors.DETAIL_PHONE));
        detailsGrid.add(createDetailPanel("NIC", nic, Colors.DETAIL_NIC));
        detailsGrid.add(createDetailPanel("Due Date", displayDueDate, Colors.DETAIL_DUE_DATE));
        detailsGrid.add(createDetailPanel("Registered Date", regDate, Colors.DETAIL_REG_DATE));

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

        String displayValue = value;
        if (value != null && value.length() > 25) {
            displayValue = "<html><div style='width:140px;'>" + value + "</div></html>";
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
     * Checks if address is valid
     */
    private boolean hasAddress(String address) {
        return address != null && !address.trim().isEmpty();
    }

    /**
     * Creates address section
     */
    private JPanel createAddressSection(String address) {
        JPanel addressPanel = new JPanel(new BorderLayout());
        addressPanel.setOpaque(false);
        addressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel addressTitle = new JLabel(Strings.ADDRESS_PREFIX);
        addressTitle.setFont(Fonts.ADDRESS_TITLE);
        addressTitle.setForeground(Colors.DETAIL_STATUS);
        addressTitle.setToolTipText("Customer Address");

        JLabel addressLabel = new JLabel("<html><div style='width:360px;'>" + address + "</div></html>");
        addressLabel.setFont(Fonts.DETAIL_VALUE);
        addressLabel.setForeground(Colors.TEXT_PRIMARY);
        addressLabel.setToolTipText(address);
        addressLabel.setVerticalAlignment(SwingConstants.TOP);

        addressPanel.add(addressTitle, BorderLayout.NORTH);
        addressPanel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
        addressPanel.add(addressLabel, BorderLayout.CENTER);

        return addressPanel;
    }

    /**
     * Creates payment section header
     */
    private JPanel createPaymentSectionHeader(int customerId) {
        JPanel paymentHeaderPanel = new JPanel(new BorderLayout());
        paymentHeaderPanel.setOpaque(false);
        paymentHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel paymentHeader = new JLabel(Strings.SECTION_PAYMENT);
        paymentHeader.setFont(Fonts.SECTION_TITLE);
        paymentHeader.setForeground(Colors.TEXT_MUTED);
        paymentHeaderPanel.add(paymentHeader, BorderLayout.WEST);

        JButton paymentDetailsBtn = createViewDetailsButton(customerId);
        paymentHeaderPanel.add(paymentDetailsBtn, BorderLayout.EAST);

        return paymentHeaderPanel;
    }

    /**
     * Creates view details button
     */
    private JButton createViewDetailsButton(int customerId) {
        JButton btn = new JButton("View Details");
        btn.setFont(new java.awt.Font("Nunito SemiBold", 0, 10));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Colors.BTN_VIEW_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BTN_VIEW_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("View Credit Details (V)");
        btn.addActionListener(e -> {
            showPaymentDetails(customerId);
            CustomerPanel.this.requestFocusInWindow();
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Colors.BTN_VIEW_BG_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Colors.BTN_VIEW_BG);
            }
        });

        return btn;
    }

    /**
     * Creates payment panels
     */
    private JPanel createPaymentPanels(double totalCreditAmount, double totalPaid, double outstanding) {
        JPanel paymentPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        paymentPanel.setOpaque(false);
        paymentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        lk.com.pos.privateclasses.RoundedPanel creditPanel = createPaymentPanel(
                "Amount Due",
                totalCreditAmount,
                Colors.PAYMENT_DUE_BG,
                Colors.PAYMENT_DUE_FG
        );

        lk.com.pos.privateclasses.RoundedPanel paidPanel = createPaymentPanel(
                "Paid Amount",
                totalPaid,
                Colors.PAYMENT_PAID_BG,
                Colors.PAYMENT_PAID_FG
        );

        Color outstandingBg = outstanding > 0 ? Colors.PAYMENT_OUTSTANDING_BG : Colors.PAYMENT_PAID_BG;
        Color outstandingFg = outstanding > 0 ? Colors.PAYMENT_OUTSTANDING_FG : Colors.PAYMENT_PAID_FG;
        lk.com.pos.privateclasses.RoundedPanel outstandingPanel = createPaymentPanel(
                "OutStanding",
                outstanding,
                outstandingBg,
                outstandingFg
        );

        paymentPanel.add(creditPanel);
        paymentPanel.add(paidPanel);
        paymentPanel.add(outstandingPanel);

        return paymentPanel;
    }

    /**
     * Creates payment panel
     */
    private lk.com.pos.privateclasses.RoundedPanel createPaymentPanel(String title, double amount,
            Color bgColor, Color textColor) {
        lk.com.pos.privateclasses.RoundedPanel panel = new lk.com.pos.privateclasses.RoundedPanel();
        panel.setBackgroundColor(bgColor);
        panel.setBorderThickness(0);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.PAYMENT_TITLE);
        titleLabel.setForeground(textColor);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 0, 0);

        JLabel amountLabel = new JLabel(formatPrice(amount));
        amountLabel.setFont(Fonts.PAYMENT_AMOUNT);
        amountLabel.setForeground(textColor);
        amountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        amountLabel.setToolTipText(title + ": Rs." + String.format("%.2f", amount));
        panel.add(amountLabel, gbc);

        return panel;
    }

    /**
     * Formats phone number
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return Strings.NO_VALUE;
        }

        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() == 10) {
            return cleaned.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return phone;
    }

    /**
     * Formats price with smart display
     */
    private String formatPrice(double price) {
        if (price >= 100000) {
            return String.format("Rs.%.1fK", price / 1000);
        } else if (price >= 10000) {
            return String.format("Rs.%.2fK", price / 1000);
        } else if (price >= 1000) {
            return String.format("Rs.%.0f", price);
        } else {
            return String.format("Rs.%.2f", price);
        }
    }

    /**
     * Opens edit customer dialog
     */
    private void editCustomer(int customerId) {
        if (customerId <= 0) {
            System.err.println("Invalid customer ID: " + customerId);
            return;
        }

        // Call your UpdateCustomer dialog here
        // UpdateCustomer updateCustomer = new UpdateCustomer(null, true, customerId);
        // updateCustomer.setLocationRelativeTo(null);
        // updateCustomer.setVisible(true);
        JOptionPane.showMessageDialog(this, "Edit Customer ID: " + customerId,
                "Edit Customer", JOptionPane.INFORMATION_MESSAGE);
        performSearch();
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    /**
     * Opens payment details dialog
     */
    private void showPaymentDetails(int customerId) {
        if (customerId <= 0) {
            System.err.println("Invalid customer ID: " + customerId);
            return;
        }

        // Call your CreditView dialog here
        // CreditView creditView = new CreditView(null, true, customerId);
        // creditView.setLocationRelativeTo(null);
        // creditView.setVisible(true);
        JOptionPane.showMessageDialog(this, "View Credit Details for Customer ID: " + customerId,
                "Credit Details", JOptionPane.INFORMATION_MESSAGE);
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        addNewCoustomerBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel1 = new javax.swing.JLabel();
        deleteBtn = new javax.swing.JButton();
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
        roundedPanel3 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        roundedPanel4 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        customerReportBtn = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton1.setForeground(new java.awt.Color(255, 51, 51));
        jRadioButton1.setText("Missed Due Date");

        addNewCoustomerBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewCoustomerBtn.setText("Add New Customer");
        addNewCoustomerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewCoustomerBtnActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(248, 250, 252));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20)); // NOI18N
        jLabel1.setText("Customer Name ");

        deleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jButton8.setBackground(new java.awt.Color(255, 51, 51));
        jButton8.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton8.setText("Missed Due Date");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(2, 0));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel15.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel15.setText("Phone");

        jLabel20.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 102, 102));
        jLabel20.setText("0701865698");

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
        jLabel21.setText("NIC");

        jLabel26.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(102, 102, 102));
        jLabel26.setText("200563253698");

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
        jLabel27.setText("Final Date");

        jLabel28.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(102, 102, 102));
        jLabel28.setText("2025-12-20");

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
        jLabel29.setText("Expiry Date");

        jLabel30.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(102, 102, 102));
        jLabel30.setText("2025-12-20");

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

        jLabel36.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel36.setText("Amount Due");

        jLabel35.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(102, 102, 102));
        jLabel35.setText("Rs.10000");

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jLabel37.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel37.setText("Paid Amount");

        jLabel38.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(102, 102, 102));
        jLabel38.setText("Rs.7500");

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        roundedPanel3Layout.setVerticalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel38)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel39.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel39.setText("OutStanding ");

        jLabel40.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(102, 102, 102));
        jLabel40.setText("Rs.2500");

        javax.swing.GroupLayout roundedPanel4Layout = new javax.swing.GroupLayout(roundedPanel4);
        roundedPanel4.setLayout(roundedPanel4Layout);
        roundedPanel4Layout.setHorizontalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        roundedPanel4Layout.setVerticalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel40)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel31.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel31.setText("Address");

        jLabel32.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(102, 102, 102));
        jLabel32.setText("2025-12-20");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(281, 281, 281))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton2.setText("V");

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))
                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                        .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jSeparator2)
                                .addGroup(roundedPanel1Layout.createSequentialGroup()
                                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, roundedPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(roundedPanel1Layout.createSequentialGroup()
                                            .addGap(0, 1, Short.MAX_VALUE)
                                            .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(roundedPanel1Layout.createSequentialGroup()
                                            .addComponent(jButton2)
                                            .addGap(0, 0, Short.MAX_VALUE))))
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(jSeparator1)))
                        .addContainerGap(25, Short.MAX_VALUE))))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addGap(8, 8, 8)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(roundedPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(525, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton2.setForeground(new java.awt.Color(99, 102, 241));
        jRadioButton2.setText("No Due");

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton4.setForeground(new java.awt.Color(255, 153, 0));
        jRadioButton4.setText("Due Amount");

        customerReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        customerReportBtn.setText("Customer Report");
        customerReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerReportBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addGap(301, 301, 301)
                        .addComponent(customerReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewCoustomerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addNewCoustomerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(customerReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jRadioButton2)
                        .addComponent(jRadioButton4)
                        .addComponent(jRadioButton1)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
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

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void addNewCoustomerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCoustomerBtnActionPerformed
        AddNewCustomer addNewCustomer = new AddNewCustomer(null, true);
        addNewCustomer.setLocationRelativeTo(null);
        addNewCustomer.setVisible(true);
        performSearch();

    }//GEN-LAST:event_addNewCoustomerBtnActionPerformed

    private void customerReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerReportBtnActionPerformed
        showExportOptions();
    }//GEN-LAST:event_customerReportBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewCoustomerBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton customerReportBtn;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel4;
    // End of variables declaration//GEN-END:variables
}
