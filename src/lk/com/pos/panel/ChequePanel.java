package lk.com.pos.panel;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.connection.DB;
import lk.com.pos.dao.ChequeDAO;
import lk.com.pos.dto.ChequeDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
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
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import lk.com.pos.dialog.AddCheque;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * ChequePanel - Displays and manages cheque information with report generation
 */
public class ChequePanel extends javax.swing.JPanel {

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
        static final Color BADGE_BOUNCED_BG = Color.decode("#FED7AA");
        static final Color BADGE_BOUNCED_FG = Color.decode("#7C2D12");
        static final Color BADGE_BOUNCED_BORDER = Color.decode("#FB923C");

        static final Color BADGE_CLEARED_BG = Color.decode("#D1FAE5");
        static final Color BADGE_CLEARED_FG = Color.decode("#059669");
        static final Color BADGE_CLEARED_BORDER = Color.decode("#059669");

        static final Color BADGE_PENDING_BG = Color.decode("#FEF3C7");
        static final Color BADGE_PENDING_FG = Color.decode("#92400E");
        static final Color BADGE_PENDING_BORDER = Color.decode("#92400E");

        // Amount Panel Colors
        static final Color AMOUNT_BG = Color.decode("#DBEAFE");
        static final Color AMOUNT_FG = Color.decode("#1E40AF");
        static final Color AMOUNT_BORDER = Color.decode("#BFDBFE");

        // Detail Colors
        static final Color DETAIL_PHONE = Color.decode("#8B5CF6");
        static final Color DETAIL_NIC = Color.decode("#EC4899");
        static final Color DETAIL_BANK = Color.decode("#10B981");
        static final Color DETAIL_DATE = Color.decode("#06B6D4");
        static final Color DETAIL_STATUS = Color.decode("#6366F1");

        // Button Colors
        static final Color BTN_EDIT_BG = Color.decode("#EFF6FF");
        static final Color BTN_EDIT_BORDER = Color.decode("#BFDBFE");
        static final Color BTN_STATUS_BG = Color.decode("#F3E8FF");
        static final Color BTN_STATUS_BORDER = Color.decode("#C4B5FD");

        // Add Bound Button Colors
        static final Color BTN_ADD_BOUND_BG = Color.decode("#FEF3C7");
        static final Color BTN_ADD_BOUND_FG = Color.decode("#92400E");
        static final Color BTN_ADD_BOUND_BORDER = Color.decode("#F59E0B");

        // Position Indicator
        static final Color POSITION_BG = new Color(31, 41, 55, 230);
        static final Color POSITION_FG = Color.WHITE;

        // Help Panel
        static final Color HELP_BG = new Color(31, 41, 55, 240);
        static final Color HELP_TITLE = Colors.TEAL_PRIMARY;
        static final Color HELP_TEXT = Color.decode("#D1D5DB");
    }

    // UI Constants - Dimensions
    private static final class Dimensions {

        static final Dimension CARD_SIZE = new Dimension(420, 520);
        static final Dimension CARD_MAX_SIZE = new Dimension(420, 520);
        static final Dimension CARD_MIN_SIZE = new Dimension(380, 520);
        static final Dimension ACTION_BUTTON_SIZE = new Dimension(30, 30);
        static final Dimension ADD_BOUND_BUTTON_SIZE = new Dimension(30, 30);
        static final int CARD_WIDTH_WITH_GAP = 445;
        static final int GRID_GAP = 25;
        static final int CARD_PADDING = 16;
    }

    // UI Constants - Fonts
    private static final class Fonts {

        static final java.awt.Font HEADER = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font SECTION_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font BADGE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font DETAIL_TITLE = new java.awt.Font("Nunito SemiBold", 0, 13);
        static final java.awt.Font DETAIL_VALUE = new java.awt.Font("Nunito SemiBold", 1, 14);
        static final java.awt.Font AMOUNT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 12);
        static final java.awt.Font AMOUNT_VALUE = new java.awt.Font("Nunito ExtraBold", 1, 16);
        static final java.awt.Font LOADING = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font POSITION = new java.awt.Font("Nunito ExtraBold", 1, 14);
        static final java.awt.Font HINT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 13);
        static final java.awt.Font HINT_KEY = new java.awt.Font("Consolas", 1, 11);
        static final java.awt.Font HINT_DESC = new java.awt.Font("Nunito SemiBold", 0, 11);
    }

    // UI Constants - Strings
    private static final class Strings {

        static final String SEARCH_PLACEHOLDER = "Search By Cheque No, Customer Name or Invoice No";
        static final String NO_CHEQUES = "No cheques found";
        static final String LOADING_MESSAGE = "Loading cheques...";
        static final String LOADING_SUBMESSAGE = "Please wait";
        static final String SECTION_DETAILS = "CHEQUE DETAILS";
        static final String SECTION_AMOUNT = "CHEQUE AMOUNT";
        static final String HELP_TITLE = "KEYBOARD SHORTCUTS";
        static final String HELP_CLOSE_HINT = "Press ? to hide";
    }

    // Business Constants
    private static final class Business {

        static final long REFRESH_COOLDOWN_MS = 1000;
    }

    // Keyboard Navigation
    private List<lk.com.pos.privateclasses.RoundedPanel> chequeCardsList = new ArrayList<>();
    private lk.com.pos.privateclasses.RoundedPanel currentFocusedCard = null;
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

    public ChequePanel() {
        initComponents();
        initializeUI();
        createPositionIndicator();
        createKeyboardHintsPanel();
        createLoadingPanel();
        setupKeyboardShortcuts();
        loadCheques();

        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
            showKeyboardHints();
        });
    }

    private void initializeUI() {
        setupScrollPane();
        setupIcons();
        setupSearchField();
        setupRadioButtons();
        setupButtons();
        setupActionButtons();
        setupEventListeners();
        setupPanel();
    }

    private void setupScrollPane() {
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5; thumb: #1CB5BB; width: 8");
    }

    private void setupIcons() {
        // Icons will be set for individual cards
    }

    private void setupSearchField() {
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Strings.SEARCH_PLACEHOLDER);
        try {
            jTextField1.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                    new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        } catch (Exception e) {
            System.err.println("Error loading search icon: " + e.getMessage());
        }

        jTextField1.setToolTipText("Search cheques (Ctrl+F or /) - Press ? for help");

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                performSearch();
            }
        });
    }

    private void setupButtons() {
        // Set initial tooltips
        customerReportBtn.setToolTipText("Export Cheque Report");
        addNewCoustomerBtn.setToolTipText("Add New Cheque");

        // Ensure text is empty
        addNewCoustomerBtn.setText("");
        customerReportBtn.setText("");
    }

    private void setupActionButtons() {
        setupAddNewChequeButton();
        setupExportButton();
    }

    private void setupAddNewChequeButton() {
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

        // Set initial tooltip with button name
        addNewCoustomerBtn.setToolTipText("Add New Cheque");

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

                // Update tooltip to show button name and shortcut
                addNewCoustomerBtn.setToolTipText("Add New Cheque (Ctrl+N or Alt+A)");
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

                // Reset tooltip to just button name
                addNewCoustomerBtn.setToolTipText("Add New Cheque");
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

        // Set initial tooltip with button name
        customerReportBtn.setToolTipText("Export Cheque Report");

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

                // Update tooltip to show button name and shortcut
                customerReportBtn.setToolTipText("Export Cheque Report (Ctrl+R)");
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

                // Reset tooltip to just button name
                customerReportBtn.setToolTipText("Export Cheque Report");
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

    private void setupRadioButtons() {
        jRadioButton1.putClientProperty(FlatClientProperties.STYLE, "foreground:#EF4444;");
        jRadioButton2.putClientProperty(FlatClientProperties.STYLE, "foreground:#10B981;");
        jRadioButton4.putClientProperty(FlatClientProperties.STYLE, "foreground:#F97316;");

        jRadioButton4.setToolTipText("Filter pending cheques (Alt+1)");
        jRadioButton1.setToolTipText("Filter bounced cheques (Alt+2)");
        jRadioButton2.setToolTipText("Filter cleared cheques (Alt+3)");

        for (java.awt.event.ActionListener al : jRadioButton1.getActionListeners()) {
            jRadioButton1.removeActionListener(al);
        }
        for (java.awt.event.ActionListener al : jRadioButton2.getActionListeners()) {
            jRadioButton2.removeActionListener(al);
        }
        for (java.awt.event.ActionListener al : jRadioButton4.getActionListeners()) {
            jRadioButton4.removeActionListener(al);
        }

        jRadioButton1.addActionListener(evt -> {
            if (jRadioButton1.isSelected()) {
                performSearch();
            }
        });
        jRadioButton2.addActionListener(evt -> {
            if (jRadioButton2.isSelected()) {
                performSearch();
            }
        });
        jRadioButton4.addActionListener(evt -> {
            if (jRadioButton4.isSelected()) {
                performSearch();
            }
        });

        jRadioButton4.setSelected(true);
    }

    private void setupPanel() {
        jPanel2.setBackground(Colors.BACKGROUND);
    }

    private void setupEventListeners() {
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> ChequePanel.this.requestFocusInWindow());
            }
        });
    }

    private void onFilterChanged() {
        performSearch();
        this.requestFocusInWindow();
    }

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

    private void createPositionIndicator() {
        positionIndicator = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        positionIndicator.setBackground(Colors.POSITION_BG);
        positionIndicator.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        positionIndicator.setVisible(false);

        positionLabel = new JLabel();
        positionLabel.setFont(Fonts.POSITION);
        positionLabel.setForeground(Colors.POSITION_FG);

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

    private void createKeyboardHintsPanel() {
        keyboardHintsPanel = new JPanel();
        keyboardHintsPanel.setLayout(new BoxLayout(keyboardHintsPanel, BoxLayout.Y_AXIS));
        keyboardHintsPanel.setBackground(Colors.HELP_BG);
        keyboardHintsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);

        JLabel title = new JLabel(Strings.HELP_TITLE);
        title.setFont(Fonts.HINT_TITLE);
        title.setForeground(Colors.HELP_TITLE);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);
        keyboardHintsPanel.add(Box.createVerticalStrut(10));

        addHintRow("← → ↑ ↓", "Navigate cards", "#FFFFFF");
        addHintRow("S", "Change Status", "#7C3AED");
        addHintRow("E", "Edit Cheque", "#1CB5BB");
        addHintRow("B", "Add Bound (Bounced only)", "#F59E0B");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("Ctrl+N/Alt+A", "Add Cheque", "#60D5F2");
        addHintRow("Ctrl+R", "Cheque Report", "#10B981");
        addHintRow("F5", "Refresh Data", "#06B6D4");
        addHintRow("Alt+1", "Pending (Default)", "#F97316");
        addHintRow("Alt+2", "Bounced", "#EF4444");
        addHintRow("Alt+3", "Cleared", "#10B981");
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
        descLabel.setForeground(Colors.HELP_TEXT);

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

    private void setupKeyboardShortcuts() {
        this.setFocusable(true);

        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        int arrowCondition = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

        registerKeyAction("LEFT", KeyEvent.VK_LEFT, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_LEFT));
        registerKeyAction("RIGHT", KeyEvent.VK_RIGHT, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_RIGHT));
        registerKeyAction("UP", KeyEvent.VK_UP, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_UP));
        registerKeyAction("DOWN", KeyEvent.VK_DOWN, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_DOWN));

        registerKeyAction("S", KeyEvent.VK_S, 0, condition, this::changeStatusForSelectedCard);
        registerKeyAction("E", KeyEvent.VK_E, 0, condition, this::editSelectedCard);
        registerKeyAction("B", KeyEvent.VK_B, 0, condition, this::addBoundForSelectedCard);

        registerKeyAction("ENTER", KeyEvent.VK_ENTER, 0, condition, this::handleEnterKey);

        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, this::focusSearch);
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, this::handleSlashKey);

        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, this::handleEscape);

        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, this::refreshCheques);

        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::openChequeReport);

        registerKeyAction("CTRL_N", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, condition, this::openAddChequeDialog);
        registerKeyAction("ALT_A", KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, condition, this::openAddChequeDialog);

        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton4));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton1));
        registerKeyAction("ALT_3", KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton2));
        registerKeyAction("ALT_0", KeyEvent.VK_0, KeyEvent.ALT_DOWN_MASK, condition, this::clearFilters);

        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, condition, this::showKeyboardHints);

        setupSearchFieldShortcuts();
    }

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

    private boolean shouldIgnoreKeyAction(int keyCode, int modifiers) {
        return jTextField1.hasFocus()
                && keyCode != KeyEvent.VK_ESCAPE
                && keyCode != KeyEvent.VK_ENTER
                && modifiers == 0
                && keyCode != KeyEvent.VK_SLASH;
    }

    private void handleEnterKey() {
        if (currentCardIndex == -1 && !chequeCardsList.isEmpty()) {
            navigateCards(KeyEvent.VK_RIGHT);
        }
    }

    private void handleSlashKey() {
        if (!jTextField1.hasFocus()) {
            focusSearch();
        }
    }

    private void clearSearchAndFilters() {
        jTextField1.setText("");
        buttonGroup1.clearSelection();
        performSearch();
        ChequePanel.this.requestFocusInWindow();
        showPositionIndicator("Search and filters cleared");
    }

    private void startNavigationFromSearch() {
        if (!chequeCardsList.isEmpty()) {
            ChequePanel.this.requestFocusInWindow();
            if (currentCardIndex == -1) {
                currentCardIndex = 0;
                selectCurrentCard();
                scrollToCardSmooth(currentCardIndex);
                updatePositionIndicator();
            }
        }
    }

    private void navigateCards(int direction) {
        if (chequeCardsList.isEmpty()) {
            showPositionIndicator("No cheques available");
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
        int newIndex = calculateNewIndex(direction, currentCardIndex, chequeCardsList.size());

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
            return Math.min((currentRow * currentColumns) - 1, chequeCardsList.size() - 1);
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

    private void updatePositionIndicator() {
        if (currentCardIndex < 0 || currentCardIndex >= chequeCardsList.size()) {
            return;
        }

        int row = (currentCardIndex / currentColumns) + 1;
        int col = (currentCardIndex % currentColumns) + 1;
        int totalRows = (int) Math.ceil((double) chequeCardsList.size() / currentColumns);

        String text = String.format("Card %d/%d (Row %d/%d, Col %d) | S: Change Status | E: Edit",
                currentCardIndex + 1,
                chequeCardsList.size(),
                row,
                totalRows,
                col
        );

        showPositionIndicator(text);
    }

    private void selectCurrentCard() {
        if (currentCardIndex < 0 || currentCardIndex >= chequeCardsList.size()) {
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = chequeCardsList.get(currentCardIndex);

        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(Colors.TEAL_PRIMARY, 4, 15),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        card.setBackground(card.getBackground().brighter());
        currentFocusedCard = card;
    }

    private void deselectCard(int index) {
        if (index < 0 || index >= chequeCardsList.size()) {
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = chequeCardsList.get(index);

        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(Colors.BORDER_DEFAULT, 2, 15),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        card.setBackground(Colors.CARD_WHITE);
    }

    private void deselectCurrentCard() {
        if (currentFocusedCard != null) {
            deselectCard(currentCardIndex);
            currentFocusedCard = null;
        }
        currentCardIndex = -1;
    }

    private void scrollToCardSmooth(int index) {
        if (index < 0 || index >= chequeCardsList.size()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                lk.com.pos.privateclasses.RoundedPanel card = chequeCardsList.get(index);

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

    private void changeStatusForSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= chequeCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = chequeCardsList.get(currentCardIndex);
        Integer chequeId = (Integer) card.getClientProperty("chequeId");

        if (chequeId != null) {
            changeStatus(chequeId);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }

    private void editSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= chequeCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = chequeCardsList.get(currentCardIndex);
        Integer chequeId = (Integer) card.getClientProperty("chequeId");
        Integer salesId = (Integer) card.getClientProperty("salesId");

        if (chequeId != null) {
            // Only pass salesId if it exists and is valid
            Integer salesToPass = (salesId != null && salesId > 0) ? salesId : null;
            editCheque(chequeId, salesToPass);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }

    private void addBoundForSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= chequeCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }

        lk.com.pos.privateclasses.RoundedPanel card = chequeCardsList.get(currentCardIndex);
        Integer chequeId = (Integer) card.getClientProperty("chequeId");
        Integer salesId = (Integer) card.getClientProperty("salesId");
        String chequeType = (String) card.getClientProperty("chequeType");

        if (chequeId != null && "Bounced".equalsIgnoreCase(chequeType)) {
            // Only pass salesId if it exists and is valid
            Integer invoiceToPass = (salesId != null && salesId > 0) ? salesId : null;
            addNewBoundEntry(chequeId, invoiceToPass, chequeType);
        } else {
            showPositionIndicator("Add Bound is only available for bounced cheques");
        }
    }

    private void focusSearch() {
        jTextField1.requestFocus();
        jTextField1.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter cheques (Press ↓ to navigate results)");
    }

    private void handleEscape() {
        if (currentCardIndex >= 0) {
            deselectCurrentCard();
            showPositionIndicator("Card deselected");
        } else if (!jTextField1.getText().isEmpty()
                || (buttonGroup1.getSelection() != null)) {
            clearSearchAndFilters();
            showPositionIndicator("Filters cleared");
        }
        this.requestFocusInWindow();
    }

    private void refreshCheques() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < Business.REFRESH_COOLDOWN_MS) {
            showPositionIndicator("Please wait before refreshing again");
            return;
        }

        lastRefreshTime = currentTime;
        performSearch();
        showPositionIndicator("✅ Cheques refreshed");
        this.requestFocusInWindow();
    }

    private void openChequeReport() {
        showExportOptions();
        showPositionIndicator("📊 Opening Cheque Report Options");
    }

    private void openAddChequeDialog() {
        addNewCoustomerBtn.doClick();
        showPositionIndicator("➕ Opening Add New Cheque dialog");
    }

    private void clearFilters() {
        buttonGroup1.clearSelection();
        performSearch();
        showPositionIndicator("All filters cleared - showing all cheques");
        this.requestFocusInWindow();
    }

    private void toggleRadioButton(javax.swing.JRadioButton radioBtn) {
        if (radioBtn.isSelected()) {
            buttonGroup1.clearSelection();
            showPositionIndicator("All filters cleared - showing all cheques");
        } else {
            radioBtn.setSelected(true);
            showPositionIndicator("Filter applied: " + radioBtn.getText());
        }
        performSearch();
        this.requestFocusInWindow();
    }

    // =====================================================================
    // 🚀 DATABASE METHODS USING DAO PATTERN
    // =====================================================================
    private List<ChequeDTO> fetchChequesFromDatabase(String searchText, boolean bouncedOnly,
            boolean clearedOnly, boolean pendingOnly) throws Exception {

        ChequeDAO chequeDAO = new ChequeDAO();
        return chequeDAO.getCheques(searchText, bouncedOnly, clearedOnly, pendingOnly);
    }

    private void changeStatus(int chequeId) {
        try {
            ChequeDAO chequeDAO = new ChequeDAO();
            ChequeDAO.ChequeStatus currentStatus = chequeDAO.getChequeStatus(chequeId);

            if (currentStatus == null) {
                JOptionPane.showMessageDialog(this, "Cheque not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] options = {"Pending", "Cleared", "Bounced"};
            JComboBox<String> statusCombo = new JComboBox<>(options);
            statusCombo.setSelectedItem(currentStatus.getChequeType());

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(new JLabel("Current Status: " + currentStatus.getChequeType()), BorderLayout.NORTH);
            panel.add(new JLabel("Select New Status:"), BorderLayout.CENTER);
            panel.add(statusCombo, BorderLayout.SOUTH);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Change Cheque Status",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String newStatus = (String) statusCombo.getSelectedItem();

                if (!newStatus.equals(currentStatus.getChequeType())) {
                    boolean success = chequeDAO.updateChequeStatus(chequeId, newStatus);

                    if (success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Cheque status updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        performSearch();
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Failed to update cheque status!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to change status: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void displayCheques(List<ChequeDTO> cheques) {
        clearChequeCards();

        currentCardIndex = -1;
        currentFocusedCard = null;

        if (cheques.isEmpty()) {
            showEmptyState();
            return;
        }

        currentColumns = calculateColumns(jPanel2.getWidth());
        final JPanel gridPanel = createGridPanel();

        for (ChequeDTO data : cheques) {
            lk.com.pos.privateclasses.RoundedPanel card = createChequeCard(data);
            gridPanel.add(card);
            chequeCardsList.add(card);
        }

        layoutCardsInPanel(gridPanel);
        setupGridResizeListener(gridPanel);

        jPanel2.revalidate();
        jPanel2.repaint();
    }

    private void loadChequesAsync(String searchText, boolean bouncedOnly,
            boolean clearedOnly, boolean pendingOnly) {
        showLoading(true);
        loadingLabel.setText("Loading cheques...");

        SwingWorker<List<ChequeDTO>, Void> worker = new SwingWorker<List<ChequeDTO>, Void>() {
            @Override
            protected List<ChequeDTO> doInBackground() throws Exception {
                return fetchChequesFromDatabase(searchText, bouncedOnly, clearedOnly, pendingOnly);
            }

            @Override
            protected void done() {
                try {
                    List<ChequeDTO> cheques = get();
                    displayCheques(cheques);

                    if (cheques.isEmpty()) {
                        showPositionIndicator("No cheques found with current filters");
                    } else {
                        showPositionIndicator("Loaded " + cheques.size() + " cheques");
                    }

                } catch (Exception e) {
                    handleLoadError(e);
                    showPositionIndicator("Error loading cheques: " + e.getMessage());
                } finally {
                    showLoading(false);
                }
            }
        };

        worker.execute();
    }

    // =====================================================================
    // 🎨 UI METHODS - UPDATED TO USE ChequeDTO
    // =====================================================================
    private lk.com.pos.privateclasses.RoundedPanel createChequeCard(ChequeDTO data) {
        String displayGivenDate = formatDate(data.getGivenDate());
        String displayChequeDate = formatDate(data.getChequeDate());

        lk.com.pos.privateclasses.RoundedPanel card = createBaseCard(data.getChequeId(), data.getChequeNo(), data.getSalesId(), data.getChequeType());
        JPanel contentPanel = createCardContent(data, displayGivenDate, displayChequeDate);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private String formatDate(String date) {
        if (date == null) {
            return "N/A";
        }

        try {
            Date d = DATE_FORMAT.parse(date);
            return DISPLAY_DATE_FORMAT.format(d);
        } catch (Exception e) {
            return date;
        }
    }

    private lk.com.pos.privateclasses.RoundedPanel createBaseCard(int chequeId, String chequeNo, int salesId, String chequeType) {
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

        card.putClientProperty("chequeId", chequeId);
        card.putClientProperty("chequeNo", chequeNo);
        card.putClientProperty("salesId", salesId);
        card.putClientProperty("chequeType", chequeType);

        addCardMouseListeners(card);

        return card;
    }

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

    private void handleCardClick(lk.com.pos.privateclasses.RoundedPanel card) {
        if (currentFocusedCard != null && currentFocusedCard != card) {
            deselectCurrentCard();
        }

        currentCardIndex = chequeCardsList.indexOf(card);
        selectCurrentCard();
        updatePositionIndicator();
        ChequePanel.this.requestFocusInWindow();
    }

    private JPanel createCardContent(ChequeDTO data, String displayGivenDate, String displayChequeDate) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CARD_WHITE);
        contentPanel.setOpaque(false);

        contentPanel.add(createHeaderSection(data.getChequeId(), data.getCustomerName(), data.getSalesId()));
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(createStatusBadgeSection(data.getChequeType()));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createDetailsSectionHeader(data.getChequeType()));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createDetailsGrid1(data.getChequeNo(), data.getInvoiceNo(), displayGivenDate, displayChequeDate));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createDetailsGrid2(data.getBankName(), data.getBranch()));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createDetailsGrid3(data.getPhone(), data.getNic()));
        contentPanel.add(Box.createVerticalStrut(10));

        if (data.getAddress() != null && !data.getAddress().trim().isEmpty()) {
            contentPanel.add(createAddressSection(data.getAddress()));
            contentPanel.add(Box.createVerticalStrut(10));
        }

        contentPanel.add(createAmountSectionHeader());
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createAmountPanel(data.getChequeAmount()));

        return contentPanel;
    }

    private JPanel createHeaderSection(int chequeId, String customerName, int salesId) {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel nameLabel = new JLabel(customerName != null ? customerName : "Unknown Customer");
        nameLabel.setFont(Fonts.HEADER);
        nameLabel.setForeground(Colors.TEXT_PRIMARY);
        nameLabel.setToolTipText(customerName);
        headerPanel.add(nameLabel, BorderLayout.CENTER);

        headerPanel.add(createHeaderActionButtons(chequeId, salesId), BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createHeaderActionButtons(int chequeId, int salesId) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        // Add existing action buttons
        JButton statusButton = createStatusButton(chequeId);
        JButton editButton = createEditButton(chequeId, salesId);

        actionPanel.add(statusButton);
        actionPanel.add(editButton);

        return actionPanel;
    }

    // Button creation methods
    private JButton createStatusButton(int chequeId) {
        JButton statusButton = new JButton();
        statusButton.setPreferredSize(Dimensions.ACTION_BUTTON_SIZE);
        statusButton.setMinimumSize(Dimensions.ACTION_BUTTON_SIZE);
        statusButton.setMaximumSize(Dimensions.ACTION_BUTTON_SIZE);
        statusButton.setBackground(Colors.BTN_STATUS_BG);
        statusButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        statusButton.setText("S");
        statusButton.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12));
        statusButton.setForeground(Color.decode("#7C3AED"));
        statusButton.setBorder(BorderFactory.createLineBorder(Colors.BTN_STATUS_BORDER, 1));
        statusButton.setFocusable(false);
        statusButton.setToolTipText("Change Status (S)");

        statusButton.addActionListener(e -> {
            changeStatus(chequeId);
            ChequePanel.this.requestFocusInWindow();
        });

        return statusButton;
    }

    private JButton createEditButton(int chequeId, int salesId) {
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
        editButton.setToolTipText("Edit Cheque (E)");
        editButton.addActionListener(e -> {
            // Only pass salesId if it exists and is valid
            Integer salesToPass = (salesId > 0) ? salesId : null;
            editCheque(chequeId, salesToPass);
            ChequePanel.this.requestFocusInWindow();
        });

        return editButton;
    }

    private JPanel createDetailsSectionHeader(String chequeType) {
        JPanel detailsHeaderPanel = new JPanel(new BorderLayout());
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25)); // Increased height for better alignment

        JLabel detailsHeader = new JLabel(Strings.SECTION_DETAILS);
        detailsHeader.setFont(Fonts.SECTION_TITLE);
        detailsHeader.setForeground(Colors.TEXT_MUTED);
        detailsHeaderPanel.add(detailsHeader, BorderLayout.WEST);

        // Create a container for the right-aligned elements
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setOpaque(false);
        rightPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Add cheque status badge
        JLabel statusBadge = createInlineStatusBadge(chequeType);
        statusBadge.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Add + button for bounced cheques only
        if ("Bounced".equalsIgnoreCase(chequeType)) {
            JButton addBoundButton = createAddBoundButton(chequeType);
            addBoundButton.setAlignmentY(Component.CENTER_ALIGNMENT);
            rightPanel.add(addBoundButton);
        }

        rightPanel.add(statusBadge);
        detailsHeaderPanel.add(rightPanel, BorderLayout.EAST);

        return detailsHeaderPanel;
    }

    private JLabel createInlineStatusBadge(String chequeType) {
        JLabel badge = new JLabel(chequeType != null ? chequeType.toUpperCase() : "UNKNOWN");
        badge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 9)); // Slightly smaller font
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setVerticalAlignment(SwingConstants.CENTER);

        // Set fixed size for consistent alignment
        badge.setPreferredSize(new Dimension(70, 20));
        badge.setMinimumSize(new Dimension(70, 20));
        badge.setMaximumSize(new Dimension(70, 20));

        if ("Bounced".equalsIgnoreCase(chequeType)) {
            badge.setForeground(Colors.BADGE_BOUNCED_FG);
            badge.setBackground(Colors.BADGE_BOUNCED_BG);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BADGE_BOUNCED_BORDER, 1),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6) // Reduced padding
            ));
        } else if ("Cleared".equalsIgnoreCase(chequeType)) {
            badge.setForeground(Colors.BADGE_CLEARED_FG);
            badge.setBackground(Colors.BADGE_CLEARED_BG);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BADGE_CLEARED_BORDER, 1),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
        } else if ("Pending".equalsIgnoreCase(chequeType)) {
            badge.setForeground(Colors.BADGE_PENDING_FG);
            badge.setBackground(Colors.BADGE_PENDING_BG);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BADGE_PENDING_BORDER, 1),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
        } else {
            badge.setForeground(Colors.TEXT_PRIMARY);
            badge.setBackground(Colors.BACKGROUND);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BORDER_DEFAULT, 1),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
        }

        return badge;
    }

    private JButton createAddBoundButton(String chequeType) {
        JButton addBoundButton = new JButton();
        addBoundButton.setPreferredSize(new Dimension(28, 22)); // Slightly smaller for better alignment
        addBoundButton.setMinimumSize(new Dimension(28, 22));
        addBoundButton.setMaximumSize(new Dimension(28, 22));
        addBoundButton.setBackground(Colors.BTN_ADD_BOUND_BG);
        addBoundButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            // Use + icon
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 12, 12); // Smaller icon
            addBoundButton.setIcon(addIcon);
        } catch (Exception e) {
            // Fallback to text if icon not found
            addBoundButton.setText("+");
            addBoundButton.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10)); // Smaller font
        }

        addBoundButton.setForeground(Colors.BTN_ADD_BOUND_FG);
        addBoundButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BTN_ADD_BOUND_BORDER, 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2) // Reduced padding
        ));
        addBoundButton.setFocusable(false);
        addBoundButton.setToolTipText("Add New Bound Entry");

        // Store cheque type for later use
        addBoundButton.putClientProperty("chequeType", chequeType);

        addBoundButton.addActionListener(e -> {
            // Get the parent card to find cheque ID
            Component parent = addBoundButton;
            while (parent != null && !(parent instanceof lk.com.pos.privateclasses.RoundedPanel)) {
                parent = parent.getParent();
            }

            if (parent instanceof lk.com.pos.privateclasses.RoundedPanel) {
                lk.com.pos.privateclasses.RoundedPanel card = (lk.com.pos.privateclasses.RoundedPanel) parent;
                Integer chequeId = (Integer) card.getClientProperty("chequeId");
                Integer invoiceId = (Integer) card.getClientProperty("salesId");

                if (chequeId != null) {
                    // Only pass invoiceId if it exists and is valid
                    Integer invoiceToPass = (invoiceId != null && invoiceId > 0) ? invoiceId : null;
                    addNewBoundEntry(chequeId, invoiceToPass, chequeType);
                }
            }

            ChequePanel.this.requestFocusInWindow();
        });

        return addBoundButton;
    }

    private void addNewBoundEntry(int chequeId, Integer invoiceId, String chequeType) {
        showPositionIndicator("➕ Opening Add New Bound dialog");

        // Build message showing what's being passed
        StringBuilder message = new StringBuilder();
        message.append("Add New Bound Entry\n");
        message.append("Cheque ID: ").append(chequeId).append("\n");
        message.append("Cheque Type: ").append(chequeType).append("\n");

        if (invoiceId != null) {
            message.append("Invoice ID: ").append(invoiceId);
        } else {
            message.append("Invoice ID: Not available (no invoice linked)");
        }

        JOptionPane.showMessageDialog(
                this,
                message.toString(),
                "Add New Bound",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Here you would typically open your actual "Add New Bound" dialog
        // and pass chequeId and invoiceId (which might be null)
        openAddBoundDialog(chequeId, invoiceId, chequeType);
    }

    private void openAddBoundDialog(int chequeId, Integer invoiceId, String chequeType) {
        // This is where you would implement your actual "Add New Bound" dialog
        // You have access to:
        // - chequeId (always available)
        // - invoiceId (might be null if no invoice exists)
        // - chequeType (for context)

        System.out.println("Opening Add Bound Dialog:");
        System.out.println("Cheque ID: " + chequeId);
        System.out.println("Invoice ID: " + (invoiceId != null ? invoiceId : "No invoice"));
        System.out.println("Cheque Type: " + chequeType);

        // Your dialog implementation would go here
        // For example:
        // new AddBoundDialog(chequeId, invoiceId, chequeType).setVisible(true);
    }

    private JPanel createStatusBadgeSection(String chequeType) {
        // This method now creates an empty panel since we moved the status to details header
        JPanel statusBadgePanel = new JPanel(new BorderLayout(10, 0));
        statusBadgePanel.setOpaque(false);
        statusBadgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5)); // Reduced height
        statusBadgePanel.setPreferredSize(new Dimension(0, 5));

        return statusBadgePanel;
    }

    private JPanel createBadgePanel(String chequeType) {
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        badgePanel.setOpaque(false);

        JLabel badge = createStatusBadge(chequeType);
        badgePanel.add(badge);

        return badgePanel;
    }

    private JLabel createStatusBadge(String chequeType) {
        JLabel badge = new JLabel(chequeType != null ? chequeType : "Unknown");
        badge.setFont(Fonts.BADGE);
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);

        if ("Bounced".equalsIgnoreCase(chequeType)) {
            badge.setForeground(Colors.BADGE_BOUNCED_FG);
            badge.setBackground(Colors.BADGE_BOUNCED_BG);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BADGE_BOUNCED_BORDER, 2),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
        } else if ("Cleared".equalsIgnoreCase(chequeType)) {
            badge.setForeground(Colors.BADGE_CLEARED_FG);
            badge.setBackground(Colors.BADGE_CLEARED_BG);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BADGE_CLEARED_BORDER, 2),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
        } else if ("Pending".equalsIgnoreCase(chequeType)) {
            badge.setForeground(Colors.BADGE_PENDING_FG);
            badge.setBackground(Colors.BADGE_PENDING_BG);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BADGE_PENDING_BORDER, 2),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
        } else {
            badge.setForeground(Colors.TEXT_PRIMARY);
            badge.setBackground(Colors.BACKGROUND);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BORDER_DEFAULT, 2),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
        }

        return badge;
    }

    private JPanel createDetailsGrid1(String chequeNo, String invoiceNo, String givenDate, String chequeDate) {
        JPanel detailsGrid = new JPanel(new GridLayout(2, 2, 20, 12));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        detailsGrid.add(createDetailPanel("Cheque No", chequeNo, Colors.DETAIL_STATUS));
        detailsGrid.add(createDetailPanel("Invoice No", invoiceNo != null ? invoiceNo : "N/A", Colors.DETAIL_NIC));
        detailsGrid.add(createDetailPanel("Given Date", givenDate, Colors.DETAIL_DATE));
        detailsGrid.add(createDetailPanel("Cheque Date", chequeDate, Colors.DETAIL_BANK));

        return detailsGrid;
    }

    private JPanel createDetailsGrid2(String bankName, String branch) {
        JPanel detailsGrid = new JPanel(new GridLayout(1, 2, 20, 12));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        detailsGrid.add(createDetailPanel("Bank Name", bankName != null ? bankName : "N/A", Colors.DETAIL_BANK));
        detailsGrid.add(createDetailPanel("Branch", branch != null ? branch : "N/A", Colors.DETAIL_DATE));

        return detailsGrid;
    }

    private JPanel createDetailsGrid3(String phone, String nic) {
        JPanel detailsGrid = new JPanel(new GridLayout(1, 2, 20, 12));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        detailsGrid.add(createDetailPanel("Phone", formatPhoneNumber(phone), Colors.DETAIL_PHONE));
        detailsGrid.add(createDetailPanel("NIC", nic != null ? nic : "N/A", Colors.DETAIL_NIC));

        return detailsGrid;
    }

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
        panel.add(Box.createVerticalStrut(3));
        panel.add(valueLabel);

        return panel;
    }

    private JPanel createAddressSection(String address) {
        JPanel addressPanel = new JPanel(new BorderLayout());
        addressPanel.setOpaque(false);
        addressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel addressTitle = new JLabel("Address");
        addressTitle.setFont(new java.awt.Font("Nunito SemiBold", 1, 13));
        addressTitle.setForeground(Colors.DETAIL_STATUS);

        JLabel addressLabel = new JLabel("<html><div style='width:360px;'>" + address + "</div></html>");
        addressLabel.setFont(Fonts.DETAIL_VALUE);
        addressLabel.setForeground(Colors.TEXT_PRIMARY);
        addressLabel.setToolTipText(address);

        addressPanel.add(addressTitle, BorderLayout.NORTH);
        addressPanel.add(Box.createVerticalStrut(3), BorderLayout.CENTER);
        addressLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        addressPanel.add(addressLabel, BorderLayout.CENTER);

        return addressPanel;
    }

    private JPanel createAmountSectionHeader() {
        JPanel amountHeaderPanel = new JPanel(new BorderLayout());
        amountHeaderPanel.setOpaque(false);
        amountHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel amountHeader = new JLabel(Strings.SECTION_AMOUNT);
        amountHeader.setFont(Fonts.SECTION_TITLE);
        amountHeader.setForeground(Colors.TEXT_MUTED);
        amountHeaderPanel.add(amountHeader, BorderLayout.WEST);

        return amountHeaderPanel;
    }

    private JPanel createAmountPanel(double amount) {
        lk.com.pos.privateclasses.RoundedPanel panel = new lk.com.pos.privateclasses.RoundedPanel();
        panel.setBackgroundColor(Colors.AMOUNT_BG);
        panel.setBorderThickness(2);
        panel.setBorderColor(Colors.AMOUNT_BORDER);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("CHEQUE AMOUNT");
        titleLabel.setFont(Fonts.AMOUNT_TITLE);
        titleLabel.setForeground(Colors.AMOUNT_FG);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 0, 0);

        JLabel amountLabel = new JLabel(formatPrice(amount));
        amountLabel.setFont(Fonts.AMOUNT_VALUE);
        amountLabel.setForeground(Colors.AMOUNT_FG);
        amountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        amountLabel.setToolTipText("Cheque Amount: " + formatPrice(amount));
        panel.add(amountLabel, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        wrapper.add(panel, BorderLayout.CENTER);

        return wrapper;
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "N/A";
        }

        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() == 10) {
            return cleaned.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return phone;
    }

    private String formatPrice(double price) {
        return String.format("Rs.%.2f", price);
    }

    private void editCheque(int chequeId, Integer salesId) {
        StringBuilder message = new StringBuilder();
        message.append("Edit Cheque Dialog\n");
        message.append("Cheque ID: ").append(chequeId).append("\n");

        if (salesId != null) {
            message.append("Sales ID: ").append(salesId);
        } else {
            message.append("Sales ID: Not available (no sales linked)");
        }

        JOptionPane.showMessageDialog(
                this,
                message.toString(),
                "Edit Cheque",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String getSearchText() {
        String text = jTextField1.getText().trim();
        return text.equals(Strings.SEARCH_PLACEHOLDER) ? "" : text;
    }

    private void performSearch() {
        System.out.println("Performing search - Pending: " + jRadioButton4.isSelected()
                + ", Bounced: " + jRadioButton1.isSelected()
                + ", Cleared: " + jRadioButton2.isSelected());
        loadCheques();
    }

    private void loadCheques() {
        String searchText = getSearchText();
        boolean bouncedOnly = jRadioButton1.isSelected();
        boolean clearedOnly = jRadioButton2.isSelected();
        boolean pendingOnly = jRadioButton4.isSelected();

        loadChequesAsync(searchText, bouncedOnly, clearedOnly, pendingOnly);
    }

    private void clearChequeCards() {
        for (lk.com.pos.privateclasses.RoundedPanel card : chequeCardsList) {
            removeAllListeners(card);
        }

        chequeCardsList.clear();
        jPanel2.removeAll();
    }

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

    private void showEmptyState() {
        jPanel2.setLayout(new BorderLayout());
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        messagePanel.setBackground(Colors.BACKGROUND);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        JLabel noCheques = new JLabel(Strings.NO_CHEQUES);
        noCheques.setFont(new java.awt.Font("Nunito SemiBold", 0, 18));
        noCheques.setForeground(Colors.TEXT_SECONDARY);
        noCheques.setHorizontalAlignment(SwingConstants.CENTER);

        messagePanel.add(noCheques);
        jPanel2.add(messagePanel, BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
    }

    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, currentColumns, Dimensions.GRID_GAP, Dimensions.GRID_GAP));
        gridPanel.setBackground(Colors.BACKGROUND);
        return gridPanel;
    }

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

    private void handleLoadError(Exception e) {
        System.err.println("Error loading cheques: " + e.getMessage());
        e.printStackTrace();

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Failed to load cheques. Please try again.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    // =====================================================================
    // 📊 REPORT GENERATION METHODS - FIXED FONT ISSUE
    // =====================================================================
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
            generateChequeReport();
        });

        JButton excelButton = new JButton("Export to Excel");
        excelButton.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        excelButton.setBackground(new Color(16, 185, 129));
        excelButton.setForeground(Color.WHITE);
        excelButton.addActionListener(e -> {
            dialog.dispose();
            exportChequeToExcel();
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

    private void generateChequeReport() {
        try {
            // Set font properties to prevent font errors
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            System.setProperty("net.sf.jasperreports.default.font.name", "Arial");
            System.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");

            // Get current filters
            String searchText = getSearchText();
            boolean bouncedOnly = jRadioButton1.isSelected();
            boolean clearedOnly = jRadioButton2.isSelected();
            boolean pendingOnly = jRadioButton4.isSelected();

            // Fetch cheque data
            List<ChequeDTO> cheques = fetchChequesFromDatabase(searchText, bouncedOnly, clearedOnly, pendingOnly);

            if (cheques.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No cheques found for the selected filters.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create a list of maps for the report data
            List<Map<String, Object>> reportData = new ArrayList<>();
            for (ChequeDTO cheque : cheques) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", cheque.getCustomerName());
                row.put("phoneNo", cheque.getPhone());
                row.put("invoiceNo", cheque.getInvoiceNo() != null ? cheque.getInvoiceNo() : "N/A");
                row.put("chequeNo", cheque.getChequeNo());
                row.put("givenDate", formatReportDate(cheque.getGivenDate()));
                row.put("chequeDate", formatReportDate(cheque.getChequeDate()));
                row.put("amount", formatAmount(cheque.getChequeAmount()));
                row.put("status", cheque.getChequeType());
                reportData.add(row);
            }

            // Prepare parameters
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Cheque Report");
            parameters.put("generatedDate", new java.util.Date().toString());
            parameters.put("filterInfo", getFilterInfo(searchText, bouncedOnly, clearedOnly, pendingOnly));
            parameters.put("totalCheques", cheques.size());
            parameters.put("totalAmount", calculateTotalAmount(cheques));

            // Set default font parameters
            parameters.put("REPORT_FONT", "Arial");
            parameters.put("REPORT_PDF_FONT", "Helvetica");

            // Load and compile the report
            InputStream jrxmlStream = getClass().getClassLoader()
                    .getResourceAsStream("lk/com/pos/reports/chequeReport.jrxml");

            if (jrxmlStream == null) {
                // Try alternative path
                jrxmlStream = getClass().getResourceAsStream("/lk/com/pos/reports/chequeReport.jrxml");
                if (jrxmlStream == null) {
                    JOptionPane.showMessageDialog(this,
                            "Report template not found at lk/com/pos/reports/chequeReport.jrxml",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

            // Create data source from list of maps
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);

            // Fill and display report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Display report
            JasperViewer.viewReport(jasperPrint, false);

            showPositionIndicator("✅ Report generated successfully");

        } catch (JRException e) {
            if (e.getMessage() != null && e.getMessage().contains("Font")) {
                // Try with simplified font settings
                try {
                    generateReportWithSimpleFont();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Font error: Please install Arial font on your system.\n" + e.getMessage(),
                            "Font Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                e.printStackTrace();
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

    private void generateReportWithSimpleFont() throws Exception {
        // Alternative method that creates a simple report without font dependencies
        JOptionPane.showMessageDialog(this,
                "Creating a simple report with default fonts...",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);

        // Create a simple HTML report as fallback
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Cheque Report</title></head><body>");
        html.append("<h1>Cheque Report</h1>");
        html.append("<p>Generated: ").append(new java.util.Date()).append("</p>");

        // Get current filters
        String searchText = getSearchText();
        boolean bouncedOnly = jRadioButton1.isSelected();
        boolean clearedOnly = jRadioButton2.isSelected();
        boolean pendingOnly = jRadioButton4.isSelected();

        // Fetch cheque data
        List<ChequeDTO> cheques = fetchChequesFromDatabase(searchText, bouncedOnly, clearedOnly, pendingOnly);

        if (cheques.isEmpty()) {
            html.append("<p>No cheques found.</p>");
        } else {
            html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
            html.append("<tr><th>Name</th><th>Phone</th><th>Invoice No</th><th>Cheque No</th><th>Given Date</th><th>Cheque Date</th><th>Amount</th><th>Status</th></tr>");

            double totalAmount = 0;
            for (ChequeDTO cheque : cheques) {
                html.append("<tr>");
                html.append("<td>").append(cheque.getCustomerName()).append("</td>");
                html.append("<td>").append(cheque.getPhone()).append("</td>");
                html.append("<td>").append(cheque.getInvoiceNo() != null ? cheque.getInvoiceNo() : "N/A").append("</td>");
                html.append("<td>").append(cheque.getChequeNo()).append("</td>");
                html.append("<td>").append(formatReportDate(cheque.getGivenDate())).append("</td>");
                html.append("<td>").append(formatReportDate(cheque.getChequeDate())).append("</td>");
                html.append("<td>").append(formatAmount(cheque.getChequeAmount())).append("</td>");
                html.append("<td>").append(cheque.getChequeType()).append("</td>");
                html.append("</tr>");

                totalAmount += cheque.getChequeAmount();
            }

            html.append("</table>");
            html.append("<p><strong>Total Cheques: ").append(cheques.size()).append("</strong></p>");
            html.append("<p><strong>Total Amount: ").append(formatAmount(totalAmount)).append("</strong></p>");
        }

        html.append("</body></html>");

        // Display HTML in a dialog
        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
        editorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        JDialog htmlDialog = new JDialog();
        htmlDialog.setTitle("Cheque Report - Simple View");
        htmlDialog.setModal(true);
        htmlDialog.add(scrollPane);
        htmlDialog.pack();
        htmlDialog.setLocationRelativeTo(this);
        htmlDialog.setVisible(true);

        showPositionIndicator("✅ Simple report generated");
    }

    private void exportChequeToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("cheque_report.xlsx"));

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
            boolean bouncedOnly = jRadioButton1.isSelected();
            boolean clearedOnly = jRadioButton2.isSelected();
            boolean pendingOnly = jRadioButton4.isSelected();

            // Fetch cheque data
            List<ChequeDTO> cheques = fetchChequesFromDatabase(searchText, bouncedOnly, clearedOnly, pendingOnly);

            if (cheques.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No cheques found for the selected filters.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Cheque Report");

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
            String[] headers = {"Name", "Phone No", "Invoice No", "Cheque No",
                "Given Date", "Cheque Date", "Amount", "Status"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            for (ChequeDTO cheque : cheques) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(cheque.getCustomerName());
                row.createCell(1).setCellValue(cheque.getPhone());
                row.createCell(2).setCellValue(cheque.getInvoiceNo() != null ? cheque.getInvoiceNo() : "N/A");
                row.createCell(3).setCellValue(cheque.getChequeNo());
                row.createCell(4).setCellValue(formatReportDate(cheque.getGivenDate()));
                row.createCell(5).setCellValue(formatReportDate(cheque.getChequeDate()));
                row.createCell(6).setCellValue(cheque.getChequeAmount());
                row.createCell(7).setCellValue(cheque.getChequeType());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary row
            Row summaryRow = sheet.createRow(rowNum + 1);
            Cell summaryCell = summaryRow.createCell(0);
            summaryCell.setCellValue("Summary:");
            summaryCell.setCellStyle(headerStyle);

            Row totalRow = sheet.createRow(rowNum + 2);
            totalRow.createCell(0).setCellValue("Total Cheques:");
            totalRow.createCell(1).setCellValue(cheques.size());

            Row amountRow = sheet.createRow(rowNum + 3);
            amountRow.createCell(0).setCellValue("Total Amount:");
            amountRow.createCell(1).setCellValue(calculateTotalAmount(cheques));

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

    private String formatReportDate(String date) {
        if (date == null || date.isEmpty()) {
            return "N/A";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            return outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            return date;
        }
    }

    private String formatAmount(double amount) {
        return String.format("Rs.%.2f", amount);
    }

    private String getFilterInfo(String searchText, boolean bouncedOnly,
            boolean clearedOnly, boolean pendingOnly) {
        StringBuilder filter = new StringBuilder("Filters: ");

        if (!searchText.isEmpty()) {
            filter.append("Search: '").append(searchText).append("', ");
        }

        if (bouncedOnly) {
            filter.append("Bounced Cheques, ");
        } else if (clearedOnly) {
            filter.append("Cleared Cheques, ");
        } else if (pendingOnly) {
            filter.append("Pending Cheques, ");
        } else {
            filter.append("All Cheques, ");
        }

        return filter.toString().replaceAll(", $", "");
    }

    private double calculateTotalAmount(List<ChequeDTO> cheques) {
        double total = 0;
        for (ChequeDTO cheque : cheques) {
            total += cheque.getChequeAmount();
        }
        return total;
    }

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
        jPanel3 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        statusbtn = new javax.swing.JButton();
        addNewBound = new javax.swing.JButton();
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
        jRadioButton1.setText("Bounced");

        addNewCoustomerBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewCoustomerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewCoustomerBtnActionPerformed(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setBackground(new java.awt.Color(248, 250, 252));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20)); // NOI18N
        jLabel1.setText("Customer Name ");

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
        jLabel15.setText("Cheque No");

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
        jLabel27.setText("Given Date");

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
        jLabel29.setText("Cheque Date");

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

        jLabel36.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("Cheque Amount");

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
                    .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel31.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel31.setText("Address");

        jLabel32.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(102, 102, 102));
        jLabel32.setText("N0.25 Muruthalawa Kandy");

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

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel33.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel33.setText("Bank Name");

        jLabel34.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(102, 102, 102));
        jLabel34.setText("Branch Name");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new java.awt.GridLayout(1, 2));

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));

        jLabel41.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel41.setText("Phone");

        jLabel42.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(102, 102, 102));
        jLabel42.setText("0715698745");

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
        jLabel22.setText("NIC");

        jLabel43.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(102, 102, 102));
        jLabel43.setText("200645789658");

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

        statusbtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        addNewBound.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(roundedPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator2)
                            .addGroup(roundedPanel1Layout.createSequentialGroup()
                                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, roundedPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                                        .addComponent(addNewBound, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(statusbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(roundedPanel1Layout.createSequentialGroup()
                                        .addComponent(jButton2)
                                        .addGap(0, 12, Short.MAX_VALUE))
                                    .addComponent(editBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jSeparator1))
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(statusbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewBound, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addGap(8, 8, 8)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
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

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton2.setForeground(new java.awt.Color(99, 102, 241));
        jRadioButton2.setText("Cleared");

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton4.setForeground(new java.awt.Color(255, 153, 0));
        jRadioButton4.setText("Pending");

        customerReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        customerReportBtn.setText("Export");
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addGap(142, 142, 142)
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
                    .addComponent(addNewCoustomerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jRadioButton2)
                        .addComponent(jRadioButton4)
                        .addComponent(jRadioButton1)
                        .addComponent(customerReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
                .addGap(19, 19, 19))
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void addNewCoustomerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCoustomerBtnActionPerformed
        AddCheque addCheque = new AddCheque(null, true);
        addCheque.setLocationRelativeTo(this);
        addCheque.setVisible(true);
        performSearch();
    }//GEN-LAST:event_addNewCoustomerBtnActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void customerReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerReportBtnActionPerformed
        openChequeReport();
    }//GEN-LAST:event_customerReportBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewBound;
    private javax.swing.JButton addNewCoustomerBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton customerReportBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton jButton2;
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
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
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
    private javax.swing.JButton statusbtn;
    // End of variables declaration//GEN-END:variables
}
