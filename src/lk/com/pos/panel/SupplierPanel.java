package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.connection.DB;
import lk.com.pos.dao.SupplierDAO;
import lk.com.pos.dto.SupplierDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import lk.com.pos.dialog.AddSupplier;
import lk.com.pos.dialog.UpdateSupplier;
import lk.com.pos.privateclasses.RoundedPanel;

// Add these imports at the top of the file (after the existing imports)
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 * SupplierPanel - Displays and manages supplier information with credit tracking
 * Features: Search, filters, keyboard navigation, payment tracking, deactivate/reactivate
 * Default: Shows ONLY active suppliers (inactive only shown when explicitly filtered)
 * 
 * @author pasin
 * @version 3.0 - Updated with DTO/DAO pattern
 */
public class SupplierPanel extends javax.swing.JPanel {

    // UI Constants - Colors
    private static final class Colors {
        static final Color TEAL_PRIMARY = new Color(28, 181, 187);
        static final Color TEAL_HOVER = new Color(60, 200, 206);
        static final Color BORDER_DEFAULT = new Color(230, 230, 230);
        static final Color BACKGROUND = Color.decode("#F8FAFC");
        static final Color CARD_WHITE = Color.WHITE;
        static final Color CARD_INACTIVE = Color.decode("#F8F9FA");
        static final Color TEXT_PRIMARY = Color.decode("#1E293B");
        static final Color TEXT_SECONDARY = Color.decode("#6B7280");
        static final Color TEXT_MUTED = Color.decode("#94A3B8");
        static final Color TEXT_INACTIVE = Color.decode("#9CA3AF");
        
        // Badge Colors
        static final Color BADGE_ACTIVE_BG = Color.decode("#D1FAE5");
        static final Color BADGE_ACTIVE_FG = Color.decode("#065F46");
        static final Color BADGE_ACTIVE_BORDER = Color.decode("#10B981");
        static final Color BADGE_INACTIVE_BG = Color.decode("#6B7280");
        static final Color BADGE_INACTIVE_FG = Color.WHITE;
        static final Color BADGE_INACTIVE_BORDER = Color.decode("#9CA3AF");
        
        // Payment Panel Colors
        static final Color PAYMENT_DUE_BG = Color.decode("#DBEAFE");
        static final Color PAYMENT_DUE_FG = Color.decode("#1E40AF");
        static final Color PAYMENT_PAID_BG = Color.decode("#F3E8FF");
        static final Color PAYMENT_PAID_FG = Color.decode("#7C3AED");
        static final Color PAYMENT_OUTSTANDING_BG = Color.decode("#FEF3C7");
        static final Color PAYMENT_OUTSTANDING_FG = Color.decode("#92400E");
        static final Color PAYMENT_NO_DUE_BG = Color.decode("#D1FAE5");
        static final Color PAYMENT_NO_DUE_FG = Color.decode("#059669");
        static final Color PAYMENT_INACTIVE_BG = Color.decode("#F3F4F6");
        static final Color PAYMENT_INACTIVE_FG = Color.decode("#6B7280");
        
        // Detail Colors
        static final Color DETAIL_PHONE = Color.decode("#8B5CF6");
        static final Color DETAIL_REG = Color.decode("#EC4899");
        static final Color DETAIL_ADDRESS = Color.decode("#06B6D4");
        static final Color DETAIL_SUPPLIER = Color.decode("#6366F1");
        
        // Button Colors
        static final Color BTN_EDIT_BG = Color.decode("#EFF6FF");
        static final Color BTN_EDIT_BORDER = Color.decode("#BFDBFE");
        static final Color BTN_DELETE_BG = Color.decode("#FEF2F2");
        static final Color BTN_DELETE_BORDER = Color.decode("#FECACA");
        static final Color BTN_REACTIVATE_BG = Color.decode("#D1FAE5");
        static final Color BTN_REACTIVATE_BORDER = Color.decode("#10B981");
        static final Color BTN_VIEW_DUE_BG = Color.decode("#8B5CF6");
        static final Color BTN_VIEW_NO_DUE_BG = Color.decode("#10B981");
        static final Color BTN_VIEW_HOVER_DUE = Color.decode("#7C3AED");
        static final Color BTN_VIEW_HOVER_NO_DUE = Color.decode("#059669");
    }
    
    // UI Constants - Dimensions
    private static final class Dimensions {
        static final Dimension CARD_SIZE = new Dimension(420, 420);
        static final Dimension CARD_MAX_SIZE = new Dimension(420, 420);
        static final Dimension CARD_MIN_SIZE = new Dimension(380, 420);
        static final Dimension ACTION_BUTTON_SIZE = new Dimension(30, 30);
        static final int CARD_WIDTH_WITH_GAP = 445;
        static final int GRID_GAP = 25;
        static final int CARD_PADDING = 25;
    }
    
    // UI Constants - Fonts
    private static final class Fonts {
        static final java.awt.Font HEADER = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font SECTION_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font BADGE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font DETAIL_TITLE = new java.awt.Font("Nunito SemiBold", 0, 13);
        static final java.awt.Font DETAIL_VALUE = new java.awt.Font("Nunito SemiBold", 1, 14);
        static final java.awt.Font SUPPLIER_NAME = new java.awt.Font("Nunito SemiBold", 0, 14);
        static final java.awt.Font PAYMENT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 12);
        static final java.awt.Font PAYMENT_AMOUNT = new java.awt.Font("Nunito ExtraBold", 1, 16);
        static final java.awt.Font HINT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 13);
        static final java.awt.Font HINT_KEY = new java.awt.Font("Consolas", 1, 11);
        static final java.awt.Font HINT_DESC = new java.awt.Font("Nunito SemiBold", 0, 11);
        static final java.awt.Font LOADING = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font POSITION = new java.awt.Font("Nunito ExtraBold", 1, 14);
    }
    
    // UI Constants - Strings
    private static final class Strings {
        static final String SEARCH_PLACEHOLDER = " Search By Company Name, Supplier Name Or Register No";
        static final String NO_SUPPLIERS = "No suppliers found";
        static final String LOADING_MESSAGE = "Loading suppliers...";
        static final String LOADING_SUBMESSAGE = "Please wait";
        static final String HELP_TITLE = "KEYBOARD SHORTCUTS";
        static final String HELP_CLOSE_HINT = "Press ? to hide";
        static final String SECTION_DETAILS = "SUPPLIER DETAILS";
        static final String SECTION_PAYMENT = "PAYMENT SUMMARY";
        static final String NO_VALUE = "N/A";
        static final String STATUS_ACTIVE = "Active";
        static final String STATUS_INACTIVE = "Inactive";
        static final String STATUS_ALL = "all";
        static final String DUE_NO_DUE = "no_due";
        static final String DUE_HAS_DUE = "has_due";
    }
    
    // Business Constants
    private static final class Business {
        static final long REFRESH_COOLDOWN_MS = 1000; // 1 second
        static final int STATUS_ACTIVE_ID = 1;
        static final int STATUS_INACTIVE_ID = 2;
    }

    // Report Constants
    private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // Keyboard Navigation
    private RoundedPanel currentFocusedCard = null;
    private List<RoundedPanel> supplierCardsList = new ArrayList<>();
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

    // DAO Instance
    private SupplierDAO supplierDAO;

    public SupplierPanel() {
        initComponents();
        initializeUI();
        createPositionIndicator();
        createKeyboardHintsPanel();
        createLoadingPanel();
        setupKeyboardShortcuts();
        
        // Initialize DAO
        supplierDAO = new SupplierDAO();
        
        loadSupplier(); // Will load ACTIVE suppliers by default (no filter selected)
        
        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
            showKeyboardHints();
        });
    }

    private void initializeUI() {
        setupScrollPane();
        setupSearchField();
        setupRadioButtons();
        setupButtons();
        setupEventListeners();
        // No radio button selected, but will show active suppliers by default
    }
    
    private void setupScrollPane() {
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        
        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5; thumb: #1CB5BB; width: 8");
    }
    
    private void setupSearchField() {
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Strings.SEARCH_PLACEHOLDER);
        try {
            jTextField1.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, 
                new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        } catch (Exception e) {
            System.err.println("Error loading search icon: " + e.getMessage());
        }
        
        jTextField1.setToolTipText("Search suppliers (Ctrl+F or /) - Press ? for help");
        
        jTextField1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
        });
    }
    
    private void setupButtons() {
        setupSupplierReportButton();
        setupAddSupplierButton();
    }
    
    private void setupSupplierReportButton() {
        supplierReportBtn.setPreferredSize(new Dimension(47, 47));
        supplierReportBtn.setMinimumSize(new Dimension(47, 47));
        supplierReportBtn.setMaximumSize(new Dimension(47, 47));

        // Set initial state - transparent background with border
        supplierReportBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
        supplierReportBtn.setForeground(Color.decode("#10B981"));

        // Remove text
        supplierReportBtn.setText("");

        // Set border with green color
        supplierReportBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set cursor
        supplierReportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remove focus painting
        supplierReportBtn.setFocusPainted(false);

        // Set icon with green color
        try {
            FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
            // Apply green color filter to the icon
            printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#10B981")));
            supplierReportBtn.setIcon(printIcon);
        } catch (Exception e) {
            System.err.println("Error loading print icon: " + e.getMessage());
        }

        // Set initial tooltip with button name
        supplierReportBtn.setToolTipText("Export Supplier Report");

        // Add hover effects
        supplierReportBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                supplierReportBtn.setBackground(Color.decode("#10B981"));
                supplierReportBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#34D399"), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon to white on hover
                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    supplierReportBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                // Update tooltip to show button name and shortcut
                supplierReportBtn.setToolTipText("Export Supplier Report (Ctrl+R)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                supplierReportBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
                supplierReportBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon back to green
                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#10B981")));
                    supplierReportBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                // Reset tooltip to just button name
                supplierReportBtn.setToolTipText("Export Supplier Report");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                supplierReportBtn.setBackground(Color.decode("#059669"));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                supplierReportBtn.setBackground(Color.decode("#10B981"));
            }
        });
    }
    
    private void setupAddSupplierButton() {
        addSupplierBtn.setPreferredSize(new Dimension(47, 47));
        addSupplierBtn.setMinimumSize(new Dimension(47, 47));
        addSupplierBtn.setMaximumSize(new Dimension(47, 47));

        // Set initial state - transparent background with border
        addSupplierBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
        addSupplierBtn.setForeground(Colors.TEAL_PRIMARY);

        // Remove text
        addSupplierBtn.setText("");

        // Set border with teal color
        addSupplierBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set cursor
        addSupplierBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remove focus painting
        addSupplierBtn.setFocusPainted(false);

        // Set icon with teal color
        try {
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
            // Apply teal color filter to the icon
            addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Colors.TEAL_PRIMARY));
            addSupplierBtn.setIcon(addIcon);
        } catch (Exception e) {
            System.err.println("Error loading add icon: " + e.getMessage());
        }

        // Set initial tooltip with button name
        addSupplierBtn.setToolTipText("Add New Supplier");

        // Add hover effects
        addSupplierBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addSupplierBtn.setBackground(Colors.TEAL_PRIMARY);
                addSupplierBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.TEAL_HOVER, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon to white on hover
                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    addSupplierBtn.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                // Update tooltip to show button name and shortcut
                addSupplierBtn.setToolTipText("Add New Supplier (Ctrl+N or Alt+A)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addSupplierBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
                addSupplierBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.TEAL_PRIMARY, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon back to teal
                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Colors.TEAL_PRIMARY));
                    addSupplierBtn.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                // Reset tooltip to just button name
                addSupplierBtn.setToolTipText("Add New Supplier");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                addSupplierBtn.setBackground(Colors.TEAL_PRIMARY.darker());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addSupplierBtn.setBackground(Colors.TEAL_PRIMARY);
            }
        });
    }
    
    private void setupRadioButtons() {
        activeRadioBtn.setToolTipText("Filter active suppliers (Alt+3)");
        inActiveRadioBtn.setToolTipText("Filter inactive suppliers (Alt+4)");
        jRadioButton2.setToolTipText("Filter no due suppliers (Alt+2)");
        jRadioButton4.setToolTipText("Filter due amount suppliers (Alt+1)");
        
        activeRadioBtn.addActionListener(evt -> onFilterChanged());
        inActiveRadioBtn.addActionListener(evt -> onFilterChanged());
        jRadioButton2.addActionListener(evt -> onFilterChanged());
        jRadioButton4.addActionListener(evt -> onFilterChanged());
        
        setupRadioButtonToggle(activeRadioBtn);
        setupRadioButtonToggle(inActiveRadioBtn);
        setupRadioButtonToggle(jRadioButton2);
        setupRadioButtonToggle(jRadioButton4);
    }
    
    private void setupRadioButtonToggle(javax.swing.JRadioButton radioBtn) {
        radioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (radioBtn.isSelected()) {
                    buttonGroup1.clearSelection(); // Clear selection
                    performSearch(); // Will default back to showing active suppliers
                    evt.consume();
                }
            }
        });
    }
    
    private void setupEventListeners() {
        jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayoutIfNeeded();
            }
        });
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> SupplierPanel.this.requestFocusInWindow());
            }
        });
    }
    
    private void onFilterChanged() {
        performSearch();
        this.requestFocusInWindow();
    }
    
    private void updateLayoutIfNeeded() {
        int scrollPaneWidth = jScrollPane1.getWidth();
        int newColumns = calculateColumns(scrollPaneWidth);
        
        if (newColumns != currentColumns) {
            currentColumns = newColumns;
            performSearch();
        }
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
        if (text == null || text.isEmpty()) return;
        
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
        addHintRow("V", "View Details", "#FCD34D");
        addHintRow("E", "Edit Supplier", "#1CB5BB");
        addHintRow("D", "Deactivate Supplier", "#F87171");
        addHintRow("R", "Reactivate Supplier", "#10B981");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("Ctrl+N", "Add New Supplier", "#1CB5BB");
        addHintRow("Alt+A", "Add New Supplier", "#1CB5BB");
        addHintRow("Ctrl+R", "Supplier Report", "#10B981");
        addHintRow("Ctrl+P", "Supplier Report", "#10B981");
        addHintRow("F5", "Refresh Data", "#06B6D4");
        addHintRow("Alt+1", "Due Amount", "#FB923C");
        addHintRow("Alt+2", "No Due", "#FB923C");
        addHintRow("Alt+3", "Active", "#FB923C");
        addHintRow("Alt+4", "Inactive", "#FB923C");
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
        registerKeyAction("V", KeyEvent.VK_V, 0, condition, this::viewDetailsForSelectedCard);
        registerKeyAction("E", KeyEvent.VK_E, 0, condition, this::editSelectedCard);
        registerKeyAction("D", KeyEvent.VK_D, 0, condition, this::deactivateSelectedCard);
        registerKeyAction("R_KEY", KeyEvent.VK_R, 0, condition, this::reactivateSelectedCard);
        
        // Enter to start navigation
        registerKeyAction("ENTER", KeyEvent.VK_ENTER, 0, condition, this::handleEnterKey);
        
        // Search
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, this::focusSearch);
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, this::handleSlashKey);
        
        // Escape
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, this::handleEscape);
        
        // Refresh
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, this::refreshSuppliers);
        
        // Add New Supplier
        registerKeyAction("CTRL_N", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, condition, this::openAddSupplier);
        registerKeyAction("ALT_A", KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, condition, this::openAddSupplier);
        
        // Supplier Report - UPDATED to show export options
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::showExportOptions);
        registerKeyAction("CTRL_P", KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, condition, this::showExportOptions);
        
        // Quick filters - Alt+1 = Due Amount, Alt+2 = No Due, Alt+3 = Active, Alt+4 = Inactive
        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton4));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(jRadioButton2));
        registerKeyAction("ALT_3", KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(activeRadioBtn));
        registerKeyAction("ALT_4", KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(inActiveRadioBtn));
        
        // Help
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
        return jTextField1.hasFocus() && 
               keyCode != KeyEvent.VK_ESCAPE && 
               keyCode != KeyEvent.VK_ENTER &&
               modifiers == 0 &&
               keyCode != KeyEvent.VK_SLASH;
    }
    
    private void handleEnterKey() {
        if (currentCardIndex == -1 && !supplierCardsList.isEmpty()) {
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
        buttonGroup1.clearSelection(); // Clear all selections
        performSearch(); // Will default to showing active suppliers
        SupplierPanel.this.requestFocusInWindow();
    }
    
    private void startNavigationFromSearch() {
        if (!supplierCardsList.isEmpty()) {
            SupplierPanel.this.requestFocusInWindow();
            if (currentCardIndex == -1) {
                currentCardIndex = 0;
                selectCurrentCard();
                scrollToCardSmooth(currentCardIndex);
                updatePositionIndicator();
            }
        }
    }

    private void navigateCards(int direction) {
        if (supplierCardsList.isEmpty()) {
            showPositionIndicator("No suppliers available");
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
        int newIndex = calculateNewIndex(direction, currentCardIndex, supplierCardsList.size());
        
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
            case KeyEvent.VK_LEFT: message = "◀️ Already at the beginning"; break;
            case KeyEvent.VK_RIGHT: message = "▶️ Already at the end"; break;
            case KeyEvent.VK_UP: message = "🔼 Already at the top"; break;
            case KeyEvent.VK_DOWN: message = "🔽 Already at the bottom"; break;
            default: return;
        }
        showPositionIndicator(message);
    }
    
    private int calculateNewIndex(int direction, int currentIndex, int totalCards) {
        int currentRow = currentIndex / currentColumns;
        int currentCol = currentIndex % currentColumns;
        int totalRows = (int) Math.ceil((double) totalCards / currentColumns);
        
        switch (direction) {
            case KeyEvent.VK_LEFT:
                if (currentCol > 0) return currentIndex - 1;
                else if (currentRow > 0) return Math.min((currentRow * currentColumns) - 1, totalCards - 1);
                return currentIndex;
                
            case KeyEvent.VK_RIGHT:
                if (currentIndex < totalCards - 1) return currentIndex + 1;
                return currentIndex;
                
            case KeyEvent.VK_UP:
                if (currentRow > 0) return Math.max(0, currentIndex - currentColumns);
                return currentIndex;
                
            case KeyEvent.VK_DOWN:
                int targetIndex = currentIndex + currentColumns;
                if (targetIndex < totalCards) return targetIndex;
                else {
                    int lastRowFirstIndex = (totalRows - 1) * currentColumns;
                    int potentialIndex = lastRowFirstIndex + currentCol;
                    if (potentialIndex < totalCards && potentialIndex > currentIndex) return potentialIndex;
                }
                return currentIndex;
                
            default:
                return currentIndex;
        }
    }
    
    private void updatePositionIndicator() {
        if (currentCardIndex < 0 || currentCardIndex >= supplierCardsList.size()) return;
        
        int row = (currentCardIndex / currentColumns) + 1;
        int col = (currentCardIndex % currentColumns) + 1;
        int totalRows = (int) Math.ceil((double) supplierCardsList.size() / currentColumns);
        
        RoundedPanel currentCard = supplierCardsList.get(currentCardIndex);
        Integer pStatusId = (Integer) currentCard.getClientProperty("pStatusId");
        
        String actionKey = "D: Deactivate";
        if (pStatusId != null && pStatusId == Business.STATUS_INACTIVE_ID) {
            actionKey = "R: Reactivate";
        }
        
        String text = String.format("Card %d/%d (Row %d/%d, Col %d) | V: View Details | E: Edit | %s", 
            currentCardIndex + 1, supplierCardsList.size(), row, totalRows, col, actionKey);
        
        showPositionIndicator(text);
    }
    
    private void selectCurrentCard() {
        if (currentCardIndex < 0 || currentCardIndex >= supplierCardsList.size()) return;
        
        RoundedPanel card = supplierCardsList.get(currentCardIndex);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(Colors.TEAL_PRIMARY, 4, 15),
            BorderFactory.createEmptyBorder(21, 14, 14, 14)
        ));
        card.setBackground(card.getBackground().brighter());
        currentFocusedCard = card;
    }
    
    private void deselectCard(int index) {
        if (index < 0 || index >= supplierCardsList.size()) return;
        
        RoundedPanel card = supplierCardsList.get(index);
        card.setBorder(BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, 18, 18, 18));
        
        Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
        if (pStatusId != null && pStatusId == Business.STATUS_INACTIVE_ID) {
            card.setBackground(Colors.CARD_INACTIVE);
        } else {
            card.setBackground(Colors.CARD_WHITE);
        }
    }
    
    private void deselectCurrentCard() {
        if (currentFocusedCard != null) {
            deselectCard(currentCardIndex);
            currentFocusedCard = null;
        }
        currentCardIndex = -1;
    }
    
    private void scrollToCardSmooth(int index) {
        if (index < 0 || index >= supplierCardsList.size()) return;
        
        SwingUtilities.invokeLater(() -> {
            try {
                RoundedPanel card = supplierCardsList.get(index);
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
        if (cardTop < viewTop) return Math.max(0, cardTop - 50);
        else if (cardBottom > viewBottom) return cardBottom - viewHeight + 50;
        else if (cardTop < viewTop + 100 && cardTop >= viewTop) return Math.max(0, cardTop - 100);
        else if (cardBottom > viewBottom - 100 && cardBottom <= viewBottom) return cardBottom - viewHeight + 100;
        return viewTop;
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

    private void viewDetailsForSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= supplierCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        RoundedPanel card = supplierCardsList.get(currentCardIndex);
        Integer supplierId = (Integer) card.getClientProperty("supplierId");
        String company = (String) card.getClientProperty("company");
        
        if (supplierId != null && company != null) {
            viewSupplierDetails(supplierId, company);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }
    
    private void editSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= supplierCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        RoundedPanel card = supplierCardsList.get(currentCardIndex);
        Integer supplierId = (Integer) card.getClientProperty("supplierId");
        
        if (supplierId != null) {
            editSupplier(supplierId);
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }
    
    private void deactivateSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= supplierCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        RoundedPanel card = supplierCardsList.get(currentCardIndex);
        Integer supplierId = (Integer) card.getClientProperty("supplierId");
        String company = (String) card.getClientProperty("company");
        Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
        
        if (supplierId != null && company != null) {
            if (pStatusId != null && pStatusId == Business.STATUS_INACTIVE_ID) {
                showPositionIndicator("ℹ️ Supplier is inactive - Use R to reactivate");
            } else {
                deactivateSupplier(supplierId, company);
            }
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }
    
    private void reactivateSelectedCard() {
        if (currentCardIndex < 0 || currentCardIndex >= supplierCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        RoundedPanel card = supplierCardsList.get(currentCardIndex);
        Integer supplierId = (Integer) card.getClientProperty("supplierId");
        String company = (String) card.getClientProperty("company");
        Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
        
        if (supplierId != null && company != null) {
            if (pStatusId != null && pStatusId == Business.STATUS_INACTIVE_ID) {
                reactivateSupplier(supplierId, company);
            } else {
                showPositionIndicator("ℹ️ Supplier is already active - Use D to deactivate");
            }
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }
    }
    
    private void focusSearch() {
        jTextField1.requestFocus();
        jTextField1.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter suppliers (Press ↓ to navigate results)");
    }
    
    private void handleEscape() {
        if (currentCardIndex >= 0) {
            deselectCurrentCard();
            showPositionIndicator("Card deselected");
        } else if (!jTextField1.getText().isEmpty() || 
                   activeRadioBtn.isSelected() || 
                   inActiveRadioBtn.isSelected() || 
                   jRadioButton2.isSelected() || 
                   jRadioButton4.isSelected()) {
            jTextField1.setText("");
            buttonGroup1.clearSelection(); // Clear all filters
            performSearch(); // Will default to showing active suppliers
            showPositionIndicator("Filters cleared - Showing active suppliers");
        }
        this.requestFocusInWindow();
    }
    
    private void refreshSuppliers() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < Business.REFRESH_COOLDOWN_MS) {
            showPositionIndicator("Please wait before refreshing again");
            return;
        }
        
        lastRefreshTime = currentTime;
        performSearch();
        showPositionIndicator("✅ Suppliers refreshed");
        this.requestFocusInWindow();
    }
    
    private void openAddSupplier() {
        AddSupplier addSupplier = new AddSupplier(null, true);
        addSupplier.setLocationRelativeTo(null);
        addSupplier.setVisible(true);
        performSearch();
        showPositionIndicator("➕ Opening Add Supplier Dialog");
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }
    
    // UPDATED: Open export options instead of directly generating report
    private void openSupplierReport() {
        showExportOptions();
    }
    
    private void toggleRadioButton(javax.swing.JRadioButton radioBtn) {
        if (radioBtn.isSelected()) {
            buttonGroup1.clearSelection(); // Clear selection
            // After clearing, will default back to showing active suppliers
            showPositionIndicator("Filter removed - Showing active suppliers");
        } else {
            radioBtn.setSelected(true);
            showPositionIndicator("Filter applied: " + radioBtn.getText());
        }
        performSearch();
        this.requestFocusInWindow();
    }
    
    private void loadSupplier() {
        String searchText = getSearchText();
        String status = getStatusFilter();
        String dueStatus = getDueFilter();
        loadSupplier(searchText, status, dueStatus);
    }
    
    private String getSearchText() {
        String text = jTextField1.getText().trim();
        return text.equals(Strings.SEARCH_PLACEHOLDER) ? "" : text;
    }
    
    /**
     * Gets status filter based on radio button selection
     * Default: Show ONLY active suppliers (even if no radio button selected)
     * Inactive suppliers ONLY shown when Inactive radio button is explicitly selected
     */
    private String getStatusFilter() {
        if (inActiveRadioBtn.isSelected()) {
            return Strings.STATUS_INACTIVE; // "Inactive" - explicitly show inactive
        } else if (activeRadioBtn.isSelected()) {
            return Strings.STATUS_ACTIVE; // "Active" - explicitly show active
        }
        // No selection - DEFAULT to Active (don't show inactive unless explicitly requested)
        return Strings.STATUS_ACTIVE;
    }
    
    private String getDueFilter() {
        if (jRadioButton2.isSelected()) return Strings.DUE_NO_DUE;
        else if (jRadioButton4.isSelected()) return Strings.DUE_HAS_DUE;
        return Strings.STATUS_ALL;
    }
    
    private void performSearch() {
        String searchText = getSearchText();
        String status = getStatusFilter();
        String dueStatus = getDueFilter();
        loadSupplier(searchText, status, dueStatus);
    }
    
    private void loadSupplier(String searchText, String status, String dueStatus) {
        showLoading(true);
        
        SwingWorker<List<SupplierDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SupplierDTO> doInBackground() throws Exception {
                return fetchSuppliersFromDatabase(searchText, status, dueStatus);
            }
            
            @Override
            protected void done() {
                try {
                    List<SupplierDTO> suppliers = get();
                    displaySuppliers(suppliers);
                } catch (Exception e) {
                    handleLoadError(e);
                } finally {
                    showLoading(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private List<SupplierDTO> fetchSuppliersFromDatabase(String searchText, String status, String dueStatus) throws Exception {
        try {
            return supplierDAO.getAllSuppliers(searchText, status, dueStatus);
        } catch (SQLException e) {
            throw new Exception("Database error while fetching suppliers: " + e.getMessage(), e);
        }
    }
    
    private void handleLoadError(Exception e) {
        System.err.println("Error loading suppliers: " + e.getMessage());
        e.printStackTrace();
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Failed to load suppliers. Please try again.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    private void displaySuppliers(List<SupplierDTO> suppliers) {
        clearSupplierCards();
        
        currentCardIndex = -1;
        currentFocusedCard = null;

        if (suppliers.isEmpty()) {
            showEmptyState();
            return;
        }

        currentColumns = calculateColumns(jScrollPane1.getWidth());
        final JPanel gridPanel = createGridPanel();
        
        for (SupplierDTO supplier : suppliers) {
            RoundedPanel card = createSupplierCard(supplier);
            gridPanel.add(card);
            supplierCardsList.add(card);
        }

        layoutCardsInPanel(gridPanel);
        setupGridResizeListener(gridPanel);
        
        jPanel2.revalidate();
        jPanel2.repaint();
    }
    
    private void clearSupplierCards() {
        for (RoundedPanel card : supplierCardsList) {
            removeAllListeners(card);
        }
        
        supplierCardsList.clear();
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
        jPanel2.setBackground(Colors.BACKGROUND);
        
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        messagePanel.setBackground(Colors.BACKGROUND);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        
        JLabel noSuppliers = new JLabel(Strings.NO_SUPPLIERS);
        noSuppliers.setFont(new java.awt.Font("Nunito SemiBold", 0, 18));
        noSuppliers.setForeground(Colors.TEXT_SECONDARY);
        noSuppliers.setHorizontalAlignment(SwingConstants.CENTER);
        
        messagePanel.add(noSuppliers);
        jPanel2.add(messagePanel, BorderLayout.NORTH);
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
        jPanel2.setBackground(Colors.BACKGROUND);
        
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
            private int lastColumns = currentColumns;
            
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int panelWidth = jPanel2.getWidth();
                int newColumns = calculateColumns(panelWidth);

                if (newColumns != lastColumns) {
                    lastColumns = newColumns;
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

        if (availableWidth >= Dimensions.CARD_WIDTH_WITH_GAP * 3) return 3;
        else if (availableWidth >= Dimensions.CARD_WIDTH_WITH_GAP * 2) return 2;
        else return 1;
    }

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
    
    private RoundedPanel createSupplierCard(SupplierDTO supplier) {
        double outstanding = supplier.getOutstandingAmount();
        
        RoundedPanel card = createBaseCard(supplier.getSupplierId(), supplier.getCompany(), supplier.getPStatusId());
        JPanel contentPanel = createCardContent(supplier, outstanding);
        
        card.add(contentPanel);
        return card;
    }
    
    private RoundedPanel createBaseCard(int supplierId, String company, int pStatusId) {
        RoundedPanel card = new RoundedPanel();
        
        if (pStatusId == Business.STATUS_INACTIVE_ID) {
            card.setBackground(Colors.CARD_INACTIVE);
        } else {
            card.setBackground(Colors.CARD_WHITE);
        }
        
        card.setPreferredSize(Dimensions.CARD_SIZE);
        card.setMaximumSize(Dimensions.CARD_MAX_SIZE);
        card.setMinimumSize(Dimensions.CARD_MIN_SIZE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorderThickness(0);
        card.setBorder(BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, 18, 18, 18));
        
        card.putClientProperty("supplierId", supplierId);
        card.putClientProperty("company", company);
        card.putClientProperty("pStatusId", pStatusId);
        
        addCardMouseListeners(card, pStatusId);
        
        return card;
    }
    
    private void addCardMouseListeners(RoundedPanel card, int pStatusId) {
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    Color hoverColor = pStatusId == Business.STATUS_INACTIVE_ID ? 
                        Colors.TEXT_INACTIVE : Colors.TEAL_HOVER;
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(hoverColor, 2, 15),
                        BorderFactory.createEmptyBorder(23, 16, 16, 16)
                    ));
                }
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    card.setBorder(BorderFactory.createEmptyBorder(Dimensions.CARD_PADDING, 18, 18, 18));
                }
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleCardClick(card);
            }
        });
    }
    
    private void handleCardClick(RoundedPanel card) {
        if (currentFocusedCard != null && currentFocusedCard != card) {
            deselectCurrentCard();
        }
        
        currentCardIndex = supplierCardsList.indexOf(card);
        selectCurrentCard();
        updatePositionIndicator();
        SupplierPanel.this.requestFocusInWindow();
    }

    private JPanel createCardContent(SupplierDTO supplier, double outstanding) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(createHeaderSection(supplier.getSupplierId(), supplier.getCompany(), supplier.getPStatusId()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(createNameBadgeSection(supplier.getSupplierName(), supplier.getStatus(), supplier.getPStatusId()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(createDetailsSectionHeader(supplier.getPStatusId()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(createDetailsGrid(supplier.getMobile(), supplier.getRegNo(), supplier.getPStatusId()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(createAddressPanel(supplier.getAddress(), supplier.getPStatusId()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(createPaymentSectionHeader(supplier.getSupplierId(), supplier.getCompany(), outstanding, supplier.getPStatusId()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(createFinancialPanels(supplier.getCreditAmount(), supplier.getPaidAmount(), outstanding, supplier.getPStatusId()));

        return contentPanel;
    }
    
    private JPanel createHeaderSection(int supplierId, String company, int pStatusId) {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel companyLabel = new JLabel(company != null ? company : Strings.NO_VALUE);
        companyLabel.setFont(Fonts.HEADER);
        
        if (pStatusId == Business.STATUS_INACTIVE_ID) {
            companyLabel.setForeground(Colors.TEXT_INACTIVE);
        } else {
            companyLabel.setForeground(Colors.TEXT_PRIMARY);
        }
        
        companyLabel.setToolTipText(company);
        headerPanel.add(companyLabel, BorderLayout.CENTER);
        
        headerPanel.add(createActionButtons(supplierId, company, pStatusId), BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createActionButtons(int supplierId, String company, int pStatusId) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);
        
        JButton editBtn = createEditButton(supplierId);
        JButton actionBtn = createActionButton(supplierId, company, pStatusId);
        
        actionPanel.add(editBtn);
        actionPanel.add(actionBtn);
        
        return actionPanel;
    }
    
    private JButton createEditButton(int supplierId) {
        JButton editBtn = new JButton();
        editBtn.setPreferredSize(Dimensions.ACTION_BUTTON_SIZE);
        editBtn.setMinimumSize(Dimensions.ACTION_BUTTON_SIZE);
        editBtn.setMaximumSize(Dimensions.ACTION_BUTTON_SIZE);
        editBtn.setBackground(Colors.BTN_EDIT_BG);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 16, 16);
            editBtn.setIcon(editIcon);
        } catch (Exception e) {
            editBtn.setText("✎");
            editBtn.setForeground(Color.decode("#3B82F6"));
        }
        
        editBtn.setBorder(BorderFactory.createLineBorder(Colors.BTN_EDIT_BORDER, 1));
        editBtn.setFocusable(false);
        editBtn.setToolTipText("Edit Supplier (E)");
        editBtn.addActionListener(e -> {
            editSupplier(supplierId);
            SupplierPanel.this.requestFocusInWindow();
        });
        
        return editBtn;
    }
    
    private JButton createActionButton(int supplierId, String company, int pStatusId) {
        JButton actionBtn = new JButton();
        actionBtn.setPreferredSize(Dimensions.ACTION_BUTTON_SIZE);
        actionBtn.setMinimumSize(Dimensions.ACTION_BUTTON_SIZE);
        actionBtn.setMaximumSize(Dimensions.ACTION_BUTTON_SIZE);
        actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionBtn.setFocusable(false);
        
        if (pStatusId == Business.STATUS_INACTIVE_ID) {
            actionBtn.setBackground(Colors.BTN_REACTIVATE_BG);
            actionBtn.setBorder(BorderFactory.createLineBorder(Colors.BTN_REACTIVATE_BORDER, 1));
            actionBtn.setToolTipText("Reactivate Supplier (R)");
            
            try {
                FlatSVGIcon refreshIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 16, 16);
                actionBtn.setIcon(refreshIcon);
            } catch (Exception e) {
                actionBtn.setText("↻");
                actionBtn.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
            }
            
            actionBtn.addActionListener(e -> {
                reactivateSupplier(supplierId, company);
                SupplierPanel.this.requestFocusInWindow();
            });
        } else {
            actionBtn.setBackground(Colors.BTN_DELETE_BG);
            actionBtn.setBorder(BorderFactory.createLineBorder(Colors.BTN_DELETE_BORDER, 1));
            actionBtn.setToolTipText("Deactivate Supplier (D)");
            
            try {
                FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 16, 16);
                actionBtn.setIcon(deleteIcon);
            } catch (Exception e) {
                actionBtn.setText("×");
                actionBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
            }
            
            actionBtn.addActionListener(e -> {
                deactivateSupplier(supplierId, company);
                SupplierPanel.this.requestFocusInWindow();
            });
        }
        
        return actionBtn;
    }
    
    private JPanel createNameBadgeSection(String supplierName, String status, int pStatusId) {
        JPanel nameBadgePanel = new JPanel(new BorderLayout(10, 0));
        nameBadgePanel.setOpaque(false);
        nameBadgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameBadgePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel nameLabel = new JLabel((supplierName != null ? supplierName : Strings.NO_VALUE));
        nameLabel.setFont(Fonts.SUPPLIER_NAME);
        
        if (pStatusId == Business.STATUS_INACTIVE_ID) {
            nameLabel.setForeground(Colors.TEXT_INACTIVE);
        } else {
            nameLabel.setForeground(Colors.DETAIL_SUPPLIER);
        }
        
        nameLabel.setToolTipText("Supplier: " + supplierName);
        nameBadgePanel.add(nameLabel, BorderLayout.WEST);
        
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);
        
        if (pStatusId == Business.STATUS_INACTIVE_ID) {
            badgePanel.add(createInactiveBadge());
        } else if (Strings.STATUS_ACTIVE.equals(status)) {
            badgePanel.add(createActiveBadge());
        }
        
        nameBadgePanel.add(badgePanel, BorderLayout.EAST);
        
        return nameBadgePanel;
    }
    
    private JLabel createActiveBadge() {
        JLabel badge = new JLabel("Active");
        badge.setFont(Fonts.BADGE);
        badge.setForeground(Colors.BADGE_ACTIVE_FG);
        badge.setBackground(Colors.BADGE_ACTIVE_BG);
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BADGE_ACTIVE_BORDER, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }
    
    private JLabel createInactiveBadge() {
        JLabel badge = new JLabel("Inactive");
        badge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
        badge.setForeground(Colors.BADGE_INACTIVE_FG);
        badge.setBackground(Colors.BADGE_INACTIVE_BG);
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BADGE_INACTIVE_BORDER, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }
    
    private JPanel createDetailsSectionHeader(int pStatusId) {
        JPanel detailsHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        detailsHeaderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel detailsHeader = new JLabel(Strings.SECTION_DETAILS);
        detailsHeader.setFont(Fonts.SECTION_TITLE);
        detailsHeader.setForeground(pStatusId == Business.STATUS_INACTIVE_ID ? 
            Colors.TEXT_INACTIVE : Colors.TEXT_MUTED);
        detailsHeaderPanel.add(detailsHeader);
        
        return detailsHeaderPanel;
    }
    
    private JPanel createDetailsGrid(String mobile, String regNo, int pStatusId) {
        JPanel detailsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        detailsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        Color phoneColor = pStatusId == Business.STATUS_INACTIVE_ID ? Colors.TEXT_INACTIVE : Colors.DETAIL_PHONE;
        Color regColor = pStatusId == Business.STATUS_INACTIVE_ID ? Colors.TEXT_INACTIVE : Colors.DETAIL_REG;
        
        detailsGrid.add(createDetailPanel("Phone", mobile != null ? mobile : Strings.NO_VALUE, phoneColor));
        detailsGrid.add(createDetailPanel("Register No", regNo != null ? regNo : Strings.NO_VALUE, regColor));
        
        return detailsGrid;
    }
    
    private JPanel createDetailPanel(String title, String value, Color accentColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.DETAIL_TITLE);
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(Fonts.DETAIL_VALUE);
        valueLabel.setForeground(Colors.TEXT_PRIMARY);
        valueLabel.setToolTipText(value);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);
        
        return panel;
    }
    
    private JPanel createAddressPanel(String address, int pStatusId) {
        Color addressColor = pStatusId == Business.STATUS_INACTIVE_ID ? 
            Colors.TEXT_INACTIVE : Colors.DETAIL_ADDRESS;
        
        JPanel addressPanel = createDetailPanel("Address", 
            address != null ? address : Strings.NO_VALUE, 
            addressColor);
        addressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        return addressPanel;
    }
    
    private JPanel createPaymentSectionHeader(int supplierId, String company, double outstanding, int pStatusId) {
        JPanel priceHeaderPanel = new JPanel(new BorderLayout());
        priceHeaderPanel.setOpaque(false);
        priceHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        priceHeaderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceHeader = new JLabel(Strings.SECTION_PAYMENT);
        priceHeader.setFont(Fonts.SECTION_TITLE);
        priceHeader.setForeground(pStatusId == Business.STATUS_INACTIVE_ID ? 
            Colors.TEXT_INACTIVE : Colors.TEXT_MUTED);
        
        JButton viewDetailsBtn = createViewDetailsButton(supplierId, company, outstanding);
        
        priceHeaderPanel.add(priceHeader, BorderLayout.WEST);
        priceHeaderPanel.add(viewDetailsBtn, BorderLayout.EAST);
        
        return priceHeaderPanel;
    }
    
    private JButton createViewDetailsButton(int supplierId, String company, double outstanding) {
        JButton btn = new JButton("View Details");
        btn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
        btn.setForeground(Color.WHITE);
        
        final Color bgColor = outstanding > 0 ? Colors.BTN_VIEW_DUE_BG : Colors.BTN_VIEW_NO_DUE_BG;
        final Color hoverColor = outstanding > 0 ? Colors.BTN_VIEW_HOVER_DUE : Colors.BTN_VIEW_HOVER_NO_DUE;
        
        btn.setBackground(bgColor);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusable(false);
        btn.setToolTipText("View Supplier Details (V)");
        btn.addActionListener(e -> {
            viewSupplierDetails(supplierId, company);
            SupplierPanel.this.requestFocusInWindow();
        });
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private JPanel createFinancialPanels(double creditAmount, double paidAmount, double outstanding, int pStatusId) {
        JPanel financialPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        financialPanel.setOpaque(false);
        financialPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        financialPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        Color dueBg = pStatusId == Business.STATUS_INACTIVE_ID ? Colors.PAYMENT_INACTIVE_BG : Colors.PAYMENT_DUE_BG;
        Color dueFg = pStatusId == Business.STATUS_INACTIVE_ID ? Colors.PAYMENT_INACTIVE_FG : Colors.PAYMENT_DUE_FG;
        Color paidBg = pStatusId == Business.STATUS_INACTIVE_ID ? Colors.PAYMENT_INACTIVE_BG : Colors.PAYMENT_PAID_BG;
        Color paidFg = pStatusId == Business.STATUS_INACTIVE_ID ? Colors.PAYMENT_INACTIVE_FG : Colors.PAYMENT_PAID_FG;
        
        RoundedPanel amountDuePanel = createFinancialCard("Amount Due", creditAmount, dueBg, dueFg);
        RoundedPanel paidPanel = createFinancialCard("Paid Amount", paidAmount, paidBg, paidFg);
        
        Color outstandingBg, outstandingFg;
        if (pStatusId == Business.STATUS_INACTIVE_ID) {
            outstandingBg = Colors.PAYMENT_INACTIVE_BG;
            outstandingFg = Colors.PAYMENT_INACTIVE_FG;
        } else if (outstanding > 0) {
            outstandingBg = Colors.PAYMENT_OUTSTANDING_BG;
            outstandingFg = Colors.PAYMENT_OUTSTANDING_FG;
        } else {
            outstandingBg = Colors.PAYMENT_NO_DUE_BG;
            outstandingFg = Colors.PAYMENT_NO_DUE_FG;
        }
        
        RoundedPanel outstandingPanel = createFinancialCard("Outstanding", outstanding, outstandingBg, outstandingFg);
        
        financialPanel.add(amountDuePanel);
        financialPanel.add(paidPanel);
        financialPanel.add(outstandingPanel);
        
        return financialPanel;
    }
    
    private RoundedPanel createFinancialCard(String title, double amount, Color bgColor, Color textColor) {
        RoundedPanel panel = new RoundedPanel();
        panel.setBackgroundColor(bgColor);
        panel.setBorderThickness(0);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 5));
        
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
    
    private String formatPrice(double price) {
        if (price >= 100000) return String.format("Rs.%.1fK", price / 1000);
        else if (price >= 10000) return String.format("Rs.%.2fK", price / 1000);
        else if (price >= 1000) return String.format("Rs.%.0f", price);
        else return String.format("Rs.%.2f", price);
    }

    private void editSupplier(int supplierId) {
        if (supplierId <= 0) {
            System.err.println("Invalid supplier ID: " + supplierId);
            return;
        }
        
        UpdateSupplier updateSupplier = new UpdateSupplier(null, true, supplierId);
        updateSupplier.setLocationRelativeTo(null);
        updateSupplier.setVisible(true);
        performSearch();
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }
    
    private void deactivateSupplier(int supplierId, String companyName) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to deactivate '" + companyName + "'?\n"
                + "Note: Supplier will be marked as inactive and hidden from the active supplier list.",
                "Confirm Deactivate",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = supplierDAO.deactivateSupplier(supplierId);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Supplier '" + companyName + "' has been deactivated successfully!\n" +
                            "You can view it in the 'Inactive Suppliers' section.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to deactivate supplier. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                
                performSearch();

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error deactivating supplier: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    private void reactivateSupplier(int supplierId, String companyName) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reactivate '" + companyName + "'?\n"
                + "Supplier will be available in the active supplier list.",
                "Confirm Reactivate",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = supplierDAO.reactivateSupplier(supplierId);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Supplier '" + companyName + "' has been reactivated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to reactivate supplier. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                
                performSearch();

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error reactivating supplier: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }
    
    private void viewSupplierDetails(int supplierId, String companyName) {
        if (supplierId <= 0) {
            System.err.println("Invalid supplier ID: " + supplierId);
            return;
        }
        
        try {
            SupplierDTO supplier = supplierDAO.getSupplierById(supplierId);
            if (supplier != null) {
                showSupplierDetailsDialog(supplier);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Supplier not found!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading supplier details: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }
    
    private void showSupplierDetailsDialog(SupplierDTO supplier) {
        // Create a custom dialog to show supplier details
        // You can expand this to show more detailed information
        String details = String.format(
            "Company: %s\n" +
            "Supplier Name: %s\n" +
            "Mobile: %s\n" +
            "Registration No: %s\n" +
            "Address: %s\n" +
            "Status: %s\n" +
            "Credit Amount: Rs.%.2f\n" +
            "Paid Amount: Rs.%.2f\n" +
            "Outstanding: Rs.%.2f",
            supplier.getCompany(),
            supplier.getSupplierName(),
            supplier.getMobile(),
            supplier.getRegNo(),
            supplier.getAddress(),
            supplier.getStatus(),
            supplier.getCreditAmount(),
            supplier.getPaidAmount(),
            supplier.getOutstandingAmount()
        );
        
        JOptionPane.showMessageDialog(this,
                details,
                "Supplier Details - " + supplier.getCompany(),
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ============================================================================
    // REPORT FUNCTIONALITY
    // ============================================================================
    
    /**
     * Shows export options dialog (similar to StockPanel)
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
            generateSupplierReport();
        });

        JButton excelButton = new JButton("Export to Excel");
        excelButton.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        excelButton.setBackground(new Color(16, 185, 129));
        excelButton.setForeground(Color.WHITE);
        excelButton.addActionListener(e -> {
            dialog.dispose();
            exportSuppliersToExcel();
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
        this.requestFocusInWindow();
    }
    
    /**
     * Generates supplier report using JasperReports
     */
    private void generateSupplierReport() {
        try {
            // Set font properties to prevent font errors
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            System.setProperty("net.sf.jasperreports.default.font.name", "Arial");
            System.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");

            // Get current filters
            String searchText = getSearchText();
            String status = getStatusFilter();
            String dueStatus = getDueFilter();
            
            // Fetch suppliers with current filters
            List<SupplierDTO> suppliers = supplierDAO.getAllSuppliers(searchText, status, dueStatus);

            if (suppliers.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No supplier data found for the selected filters.",
                    "No Data", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create a list of maps for the report data - MATCHING JRXML FIELD NAMES
            List<Map<String, Object>> reportData = new ArrayList<>();
            int activeCount = 0;
            int inactiveCount = 0;
            int dueCount = 0;
            int noDueCount = 0;
            double totalCredit = 0;
            double totalPaid = 0;
            double totalOutstanding = 0;

            for (SupplierDTO supplier : suppliers) {
                Map<String, Object> row = new HashMap<>();
                
                // Match the exact field names from the JRXML (from the provided JRXML code)
                row.put("companyName", supplier.getCompany() != null ? supplier.getCompany() : "N/A");
                row.put("name", supplier.getSupplierName() != null ? supplier.getSupplierName() : "N/A");
                row.put("phoneNo", supplier.getMobile() != null ? supplier.getMobile() : "N/A");
                row.put("registorNo", supplier.getRegNo() != null ? supplier.getRegNo() : "N/A");
                row.put("address", supplier.getAddress() != null ? supplier.getAddress() : "N/A");
                row.put("amountDue", String.format("Rs. %.2f", supplier.getCreditAmount()));
                row.put("outStanding", String.format("Rs. %.2f", supplier.getOutstandingAmount()));
                
                reportData.add(row);

                // Count statistics
                if (supplier.getPStatusId() == Business.STATUS_ACTIVE_ID) {
                    activeCount++;
                } else {
                    inactiveCount++;
                }
                
                if (supplier.getOutstandingAmount() > 0) {
                    dueCount++;
                } else {
                    noDueCount++;
                }
                
                totalCredit += supplier.getCreditAmount();
                totalPaid += supplier.getPaidAmount();
                totalOutstanding += supplier.getOutstandingAmount();
            }

            // Prepare parameters for the report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Supplier Report");
            parameters.put("generatedDate", REPORT_DATE_FORMAT.format(new Date()));
            parameters.put("totalSuppliers", suppliers.size());
            parameters.put("activeSuppliers", activeCount);
            parameters.put("inactiveSuppliers", inactiveCount);
            parameters.put("dueSuppliers", dueCount);
            parameters.put("noDueSuppliers", noDueCount);
            parameters.put("totalCredit", String.format("Rs. %.2f", totalCredit));
            parameters.put("totalPaid", String.format("Rs. %.2f", totalPaid));
            parameters.put("totalOutstanding", String.format("Rs. %.2f", totalOutstanding));
            parameters.put("filterInfo", getSupplierFilterInfo(searchText, status, dueStatus));
            
            // Set default font parameters
            parameters.put("REPORT_FONT", "Arial");
            parameters.put("REPORT_PDF_FONT", "Helvetica");

            // Load the JRXML template from classpath
            InputStream jrxmlStream = getClass().getResourceAsStream("/lk/com/pos/reports/supplierReport.jrxml");
            
            if (jrxmlStream == null) {
                // Try alternative path
                jrxmlStream = getClass().getClassLoader().getResourceAsStream("lk/com/pos/reports/supplierReport.jrxml");
                if (jrxmlStream == null) {
                    // Try to load from file system
                    File jrxmlFile = new File("src/main/resources/lk/com/pos/reports/supplierReport.jrxml");
                    if (jrxmlFile.exists()) {
                        jrxmlStream = new java.io.FileInputStream(jrxmlFile);
                    } else {
                        // Use the provided JRXML code directly
                        generateSupplierReportWithSimpleView(suppliers);
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

            showPositionIndicator("✅ Supplier report generated successfully");

        } catch (JRException e) {
            e.printStackTrace();
            
            // Try with simplified font settings
            try {
                // Get current filters
                String searchText = getSearchText();
                String status = getStatusFilter();
                String dueStatus = getDueFilter();
                
                // Fetch suppliers
                List<SupplierDTO> suppliers = supplierDAO.getAllSuppliers(searchText, status, dueStatus);
                generateSupplierReportWithSimpleView(suppliers);
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
     * Fallback method for report generation with simple HTML view
     */
    private void generateSupplierReportWithSimpleView(List<SupplierDTO> suppliers) throws Exception {
        // Create a simple HTML report as fallback
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Supplier Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #1CB5BB; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
        html.append("th { background-color: #1CB5BB; color: white; padding: 10px; text-align: left; }");
        html.append("td { border: 1px solid #ddd; padding: 8px; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append(".summary { background-color: #e8f4f8; padding: 15px; margin: 20px 0; border-radius: 5px; }");
        html.append("</style></head><body>");
        
        html.append("<h1>Supplier Report</h1>");
        html.append("<p>Generated: ").append(REPORT_DATE_FORMAT.format(new Date())).append("</p>");

        if (suppliers.isEmpty()) {
            html.append("<p>No supplier data found.</p>");
        } else {
            html.append("<table>");
            html.append("<tr><th>Company Name</th><th>Supplier Name</th><th>Phone No</th><th>Register No</th><th>Address</th><th>Amount Due</th><th>Outstanding</th></tr>");

            int activeCount = 0;
            int inactiveCount = 0;
            int dueCount = 0;
            int noDueCount = 0;
            double totalCredit = 0;
            double totalPaid = 0;
            double totalOutstanding = 0;

            for (SupplierDTO supplier : suppliers) {
                html.append("<tr>");
                html.append("<td>").append(supplier.getCompany() != null ? supplier.getCompany() : "N/A").append("</td>");
                html.append("<td>").append(supplier.getSupplierName() != null ? supplier.getSupplierName() : "N/A").append("</td>");
                html.append("<td>").append(supplier.getMobile() != null ? supplier.getMobile() : "N/A").append("</td>");
                html.append("<td>").append(supplier.getRegNo() != null ? supplier.getRegNo() : "N/A").append("</td>");
                html.append("<td>").append(supplier.getAddress() != null ? supplier.getAddress() : "N/A").append("</td>");
                html.append("<td>").append(String.format("Rs. %.2f", supplier.getCreditAmount())).append("</td>");
                html.append("<td>").append(String.format("Rs. %.2f", supplier.getOutstandingAmount())).append("</td>");
                html.append("</tr>");

                // Count statistics
                if (supplier.getPStatusId() == Business.STATUS_ACTIVE_ID) {
                    activeCount++;
                } else {
                    inactiveCount++;
                }
                
                if (supplier.getOutstandingAmount() > 0) {
                    dueCount++;
                } else {
                    noDueCount++;
                }
                
                totalCredit += supplier.getCreditAmount();
                totalPaid += supplier.getPaidAmount();
                totalOutstanding += supplier.getOutstandingAmount();
            }

            html.append("</table>");
            
            // Add summary section
            html.append("<div class='summary'>");
            html.append("<h3>Summary</h3>");
            html.append("<p><strong>Total Suppliers: ").append(suppliers.size()).append("</strong></p>");
            html.append("<p><strong>Active Suppliers: ").append(activeCount).append("</strong></p>");
            html.append("<p><strong>Inactive Suppliers: ").append(inactiveCount).append("</strong></p>");
            html.append("<p><strong>Suppliers with Due: ").append(dueCount).append("</strong></p>");
            html.append("<p><strong>Suppliers with No Due: ").append(noDueCount).append("</strong></p>");
            html.append("<p><strong>Total Credit Amount: ").append(String.format("Rs. %.2f", totalCredit)).append("</strong></p>");
            html.append("<p><strong>Total Paid Amount: ").append(String.format("Rs. %.2f", totalPaid)).append("</strong></p>");
            html.append("<p><strong>Total Outstanding: ").append(String.format("Rs. %.2f", totalOutstanding)).append("</strong></p>");
            
            // Add filter info
            String searchText = getSearchText();
            String status = getStatusFilter();
            String dueStatus = getDueFilter();
            html.append("<p><strong>Filter Info: ").append(getSupplierFilterInfo(searchText, status, dueStatus)).append("</strong></p>");
            html.append("</div>");
        }

        html.append("</body></html>");

        // Display HTML in a dialog
        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
        editorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(1000, 600));

        JDialog htmlDialog = new JDialog();
        htmlDialog.setTitle("Supplier Report - Simple View");
        htmlDialog.setModal(true);
        htmlDialog.add(scrollPane);
        htmlDialog.pack();
        htmlDialog.setLocationRelativeTo(this);
        htmlDialog.setVisible(true);

        showPositionIndicator("✅ Simple supplier report generated");
    }
    
    /**
     * Gets filter info for report
     */
    private String getSupplierFilterInfo(String searchText, String status, String dueStatus) {
        StringBuilder filter = new StringBuilder();
        
        if (!searchText.isEmpty()) {
            filter.append("Search: '").append(searchText).append("' | ");
        }
        
        if (!status.equals(Strings.STATUS_ALL)) {
            filter.append("Status: ").append(status);
        } else {
            filter.append("Status: All");
        }
        
        if (!dueStatus.equals(Strings.STATUS_ALL)) {
            if (dueStatus.equals(Strings.DUE_HAS_DUE)) {
                filter.append(" | Due: Has Due");
            } else if (dueStatus.equals(Strings.DUE_NO_DUE)) {
                filter.append(" | Due: No Due");
            }
        }
        
        return filter.toString();
    }
    
    /**
     * Exports supplier data to Excel
     */
    private void exportSuppliersToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("supplier_report.xlsx"));

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
            String status = getStatusFilter();
            String dueStatus = getDueFilter();
            
            // Fetch suppliers with current filters
            List<SupplierDTO> suppliers = supplierDAO.getAllSuppliers(searchText, status, dueStatus);

            if (suppliers.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No supplier data found for the selected filters.",
                    "No Data", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Supplier Report");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create header row (matching JRXML fields)
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Company Name", "Supplier Name", "Phone No", "Register No", "Address", "Amount Due", "Outstanding", "Status", "Paid Amount", "Credit Amount"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            int activeCount = 0;
            int inactiveCount = 0;
            int dueCount = 0;
            int noDueCount = 0;
            double totalCredit = 0;
            double totalPaid = 0;
            double totalOutstanding = 0;

            for (SupplierDTO supplier : suppliers) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(supplier.getCompany() != null ? supplier.getCompany() : "N/A");
                row.createCell(1).setCellValue(supplier.getSupplierName() != null ? supplier.getSupplierName() : "N/A");
                row.createCell(2).setCellValue(supplier.getMobile() != null ? supplier.getMobile() : "N/A");
                row.createCell(3).setCellValue(supplier.getRegNo() != null ? supplier.getRegNo() : "N/A");
                row.createCell(4).setCellValue(supplier.getAddress() != null ? supplier.getAddress() : "N/A");
                row.createCell(5).setCellValue(supplier.getCreditAmount());
                row.createCell(6).setCellValue(supplier.getOutstandingAmount());
                row.createCell(7).setCellValue(supplier.getPStatusId() == Business.STATUS_ACTIVE_ID ? "Active" : "Inactive");
                row.createCell(8).setCellValue(supplier.getPaidAmount());
                row.createCell(9).setCellValue(supplier.getCreditAmount());

                // Count statistics
                if (supplier.getPStatusId() == Business.STATUS_ACTIVE_ID) {
                    activeCount++;
                } else {
                    inactiveCount++;
                }
                
                if (supplier.getOutstandingAmount() > 0) {
                    dueCount++;
                } else {
                    noDueCount++;
                }
                
                totalCredit += supplier.getCreditAmount();
                totalPaid += supplier.getPaidAmount();
                totalOutstanding += supplier.getOutstandingAmount();
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary rows
            int summaryRowNum = rowNum + 2;

            Row summaryHeader = sheet.createRow(summaryRowNum++);
            Cell summaryCell = summaryHeader.createCell(0);
            summaryCell.setCellValue("SUPPLIER REPORT SUMMARY");
            summaryCell.setCellStyle(headerStyle);

            Row totalSuppliersRow = sheet.createRow(summaryRowNum++);
            totalSuppliersRow.createCell(0).setCellValue("Total Suppliers:");
            totalSuppliersRow.createCell(1).setCellValue(suppliers.size());

            Row activeSuppliersRow = sheet.createRow(summaryRowNum++);
            activeSuppliersRow.createCell(0).setCellValue("Active Suppliers:");
            activeSuppliersRow.createCell(1).setCellValue(activeCount);

            Row inactiveSuppliersRow = sheet.createRow(summaryRowNum++);
            inactiveSuppliersRow.createCell(0).setCellValue("Inactive Suppliers:");
            inactiveSuppliersRow.createCell(1).setCellValue(inactiveCount);

            Row dueSuppliersRow = sheet.createRow(summaryRowNum++);
            dueSuppliersRow.createCell(0).setCellValue("Suppliers with Due:");
            dueSuppliersRow.createCell(1).setCellValue(dueCount);

            Row noDueSuppliersRow = sheet.createRow(summaryRowNum++);
            noDueSuppliersRow.createCell(0).setCellValue("Suppliers with No Due:");
            noDueSuppliersRow.createCell(1).setCellValue(noDueCount);

            Row totalCreditRow = sheet.createRow(summaryRowNum++);
            totalCreditRow.createCell(0).setCellValue("Total Credit Amount:");
            totalCreditRow.createCell(1).setCellValue(String.format("Rs. %.2f", totalCredit));

            Row totalPaidRow = sheet.createRow(summaryRowNum++);
            totalPaidRow.createCell(0).setCellValue("Total Paid Amount:");
            totalPaidRow.createCell(1).setCellValue(String.format("Rs. %.2f", totalPaid));

            Row totalOutstandingRow = sheet.createRow(summaryRowNum++);
            totalOutstandingRow.createCell(0).setCellValue("Total Outstanding:");
            totalOutstandingRow.createCell(1).setCellValue(String.format("Rs. %.2f", totalOutstanding));

            Row generatedDateRow = sheet.createRow(summaryRowNum++);
            generatedDateRow.createCell(0).setCellValue("Generated Date:");
            generatedDateRow.createCell(1).setCellValue(REPORT_DATE_FORMAT.format(new Date()));

            Row filterInfoRow = sheet.createRow(summaryRowNum++);
            filterInfoRow.createCell(0).setCellValue("Filter Info:");
            filterInfoRow.createCell(1).setCellValue(getSupplierFilterInfo(searchText, status, dueStatus));

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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        activeRadioBtn = new javax.swing.JRadioButton();
        inActiveRadioBtn = new javax.swing.JRadioButton();
        addSupplierBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel2 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel1 = new javax.swing.JLabel();
        deleteBtn = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        roundedPanel3 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        roundedPanel4 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        roundedPanel5 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        supplierReportBtn = new javax.swing.JButton();

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        buttonGroup1.add(activeRadioBtn);
        activeRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        activeRadioBtn.setForeground(new java.awt.Color(99, 102, 241));
        activeRadioBtn.setText("Active");

        buttonGroup1.add(inActiveRadioBtn);
        inActiveRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        inActiveRadioBtn.setForeground(new java.awt.Color(255, 51, 51));
        inActiveRadioBtn.setText("Inactive");

        addSupplierBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addSupplierBtn.setText("Add Supplier");
        addSupplierBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSupplierBtnActionPerformed(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20)); // NOI18N
        jLabel1.setText("Company Name");

        deleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(1, 0));

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
        jLabel21.setText("Register  No");

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

        roundedPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel36.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel36.setText("Amount Due");

        jLabel35.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(102, 102, 102));
        jLabel35.setText("Rs.10000");

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundedPanel3Layout.setVerticalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
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

        javax.swing.GroupLayout roundedPanel4Layout = new javax.swing.GroupLayout(roundedPanel4);
        roundedPanel4.setLayout(roundedPanel4Layout);
        roundedPanel4Layout.setHorizontalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        roundedPanel4Layout.setVerticalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
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

        javax.swing.GroupLayout roundedPanel5Layout = new javax.swing.GroupLayout(roundedPanel5);
        roundedPanel5.setLayout(roundedPanel5Layout);
        roundedPanel5Layout.setHorizontalGroup(
            roundedPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(roundedPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        roundedPanel5Layout.setVerticalGroup(
            roundedPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel5Layout.createSequentialGroup()
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
        jLabel32.setText("No. Muruthalawa Kandy");

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

        jButton3.setText("V");

        jLabel22.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(102, 102, 102));
        jLabel22.setText("Supplier Name");

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(roundedPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 107, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator2)
                            .addGroup(roundedPanel2Layout.createSequentialGroup()
                                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                                        .addGap(0, 1, Short.MAX_VALUE)
                                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                                        .addComponent(jButton3)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jSeparator1))
                        .addContainerGap(18, Short.MAX_VALUE))))
        );
        roundedPanel2Layout.setVerticalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(572, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(112, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton4.setForeground(new java.awt.Color(255, 153, 0));
        jRadioButton4.setText("Due Amount");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton2.setForeground(new java.awt.Color(99, 102, 241));
        jRadioButton2.setText("No Due");

        supplierReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        supplierReportBtn.setText("Supplier Report");
        supplierReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supplierReportBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(activeRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inActiveRadioBtn)
                        .addGap(186, 186, 186)
                        .addComponent(supplierReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addSupplierBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(15, 15, 15))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(activeRadioBtn)
                            .addComponent(inActiveRadioBtn)
                            .addComponent(jRadioButton4)
                            .addComponent(jRadioButton2)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addSupplierBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(supplierReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

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

    private void addSupplierBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSupplierBtnActionPerformed
       AddSupplier addSupplier = new AddSupplier(null, true);
       addSupplier.setLocationRelativeTo(null);
       addSupplier.setVisible(true);
        performSearch();
       
    }//GEN-LAST:event_addSupplierBtnActionPerformed

    private void supplierReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supplierReportBtnActionPerformed
       showExportOptions();
    }//GEN-LAST:event_supplierReportBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton activeRadioBtn;
    private javax.swing.JButton addSupplierBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JRadioButton inActiveRadioBtn;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel4;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel5;
    private javax.swing.JButton supplierReportBtn;
    // End of variables declaration//GEN-END:variables
}
