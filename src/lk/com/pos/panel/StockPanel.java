package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
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
import lk.com.pos.dao.ProductStockDAO;
import lk.com.pos.dto.ProductStockDTO;
import lk.com.pos.dialog.AddNewStock;
import lk.com.pos.dialog.PrintProductLabel;
import lk.com.pos.dialog.UpdateProduct;
import lk.com.pos.dialog.UpdateProductStock;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class StockPanel extends javax.swing.JPanel {

    private lk.com.pos.privateclasses.RoundedPanel currentFocusedCard = null;
    private java.util.List<lk.com.pos.privateclasses.RoundedPanel> productCardsList = new java.util.ArrayList<>();
    private int currentCardIndex = -1;
    private int currentColumns = 3;

    private JPanel positionIndicator;
    private JLabel positionLabel;
    private Timer positionTimer;

    private JPanel keyboardHintsPanel;
    private boolean hintsVisible = false;

    private JPanel loadingPanel;
    private JLabel loadingLabel;

    // Track product batches
    private Map<Integer, Integer> productBatchCount = new HashMap<>();
    private Map<Integer, Integer> productBatchIndex = new HashMap<>();

    // Refresh cooldown
    private long lastRefreshTime = 0;
    private static final long REFRESH_COOLDOWN = 1000; // 1 second

    // Teal/Cyan border colors
    private static final Color TEAL_BORDER_SELECTED = new Color(28, 181, 187);
    private static final Color TEAL_BORDER_HOVER = new Color(60, 200, 206);
    private static final Color DEFAULT_BORDER = new Color(230, 230, 230);
    private static final Color BATCH_HIGHLIGHT = new Color(255, 243, 205); // Light yellow for same product

    // UPDATED: Responsive card dimensions
    private int cardWidth = 420;
    private int cardHeight = 480;
    private int gridGap = 25;

    // DAO instance
    private ProductStockDAO productStockDAO;
    
    // Report formatting
    private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public StockPanel() {
        initComponents();
        
        // Initialize DAO
        productStockDAO = new ProductStockDAO();

        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        init();
        createPositionIndicator();
        createKeyboardHintsPanel();
        createLoadingPanel();
        loadProduct();
        setupEventListeners();
        radioButtonListener();
        setupKeyboardShortcuts();
        
        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
            showKeyboardHints();
        });
    }

    private void init() {
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 18, 18);
            editBtn.setIcon(editIcon);
            FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 18, 18);
            deleteBtn.setIcon(deleteIcon);
        } catch (Exception e) {
            System.err.println("Error loading icons: " + e.getMessage());
        }

        roundedPanel1.setBackgroundColor(Color.decode("#E0F2FE"));
        roundedPanel1.setBorderThickness(0);
        roundedPanel3.setBackgroundColor(Color.decode("#FCE7F3"));
        roundedPanel3.setBorderThickness(0);
        roundedPanel4.setBackgroundColor(Color.decode("#D1FAE5"));
        roundedPanel4.setBorderThickness(0);

        jScrollPane1.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "track: #F5F5F5;"
                + "thumb: #1CB5BB;"
                + "width: 8");

        productSearchBar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search By Product Name Or Barcode");
        productSearchBar.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                new FlatSVGIcon("lk/com/pos/icon/search.svg", 16, 16));
        
        setupActionButtons();
        
        addStockBtn.setToolTipText("Add Stock (Ctrl+N or Alt+A)");
        stockReportBtn.setToolTipText("Generate Stock Report (Ctrl+R or Ctrl+P)");
        productSearchBar.setToolTipText("Search products (Ctrl+F or /) - Press ? for help");
        expiringRadioBtn.setToolTipText("Filter expiring products (Alt+1)");
        lowStockRadioBtn.setToolTipText("Filter low stock products (Alt+2)");
        expiredRadioBtn.setToolTipText("Filter expired products (Alt+3)");
        inactiveRadioBtn.setToolTipText("Filter inactive products (Alt+4)");
    }

    private void setupActionButtons() {
        setupAddStockButton();
        setupStockReportButton();
    }

    private void setupAddStockButton() {
        addStockBtn.setPreferredSize(new Dimension(47, 47));
        addStockBtn.setMinimumSize(new Dimension(47, 47));
        addStockBtn.setMaximumSize(new Dimension(47, 47));

        // Set initial state - transparent background with border
        addStockBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
        addStockBtn.setForeground(TEAL_BORDER_SELECTED);

        // Remove text
        addStockBtn.setText("");

        // Set border with teal color
        addStockBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_BORDER_SELECTED, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set cursor
        addStockBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remove focus painting
        addStockBtn.setFocusPainted(false);

        // Set icon with teal color
        try {
            FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
            // Apply teal color filter to the icon
            addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEAL_BORDER_SELECTED));
            addStockBtn.setIcon(addIcon);
        } catch (Exception e) {
            System.err.println("Error loading add icon: " + e.getMessage());
        }

        // Set initial tooltip with button name
        addStockBtn.setToolTipText("Add Stock");

        // Add hover effects
        addStockBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addStockBtn.setBackground(TEAL_BORDER_SELECTED);
                addStockBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TEAL_BORDER_HOVER, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon to white on hover
                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    addStockBtn.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                // Update tooltip to show button name and shortcut
                addStockBtn.setToolTipText("Add Stock (Ctrl+N or Alt+A)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addStockBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
                addStockBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TEAL_BORDER_SELECTED, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon back to teal
                try {
                    FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 24, 24);
                    addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEAL_BORDER_SELECTED));
                    addStockBtn.setIcon(addIcon);
                } catch (Exception e) {
                    System.err.println("Error loading add icon: " + e.getMessage());
                }

                // Reset tooltip to just button name
                addStockBtn.setToolTipText("Add Stock");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                addStockBtn.setBackground(TEAL_BORDER_SELECTED.darker());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addStockBtn.setBackground(TEAL_BORDER_SELECTED);
            }
        });
    }

    private void setupStockReportButton() {
        stockReportBtn.setPreferredSize(new Dimension(47, 47));
        stockReportBtn.setMinimumSize(new Dimension(47, 47));
        stockReportBtn.setMaximumSize(new Dimension(47, 47));

        // Set initial state - transparent background with border
        stockReportBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
        stockReportBtn.setForeground(Color.decode("#10B981"));

        // Remove text
        stockReportBtn.setText("");

        // Set border with green color
        stockReportBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set cursor
        stockReportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remove focus painting
        stockReportBtn.setFocusPainted(false);

        // Set icon with green color
        try {
            FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
            // Apply green color filter to the icon
            printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#10B981")));
            stockReportBtn.setIcon(printIcon);
        } catch (Exception e) {
            System.err.println("Error loading print icon: " + e.getMessage());
        }

        // Set initial tooltip with button name
        stockReportBtn.setToolTipText("Generate Stock Report");

        // Add hover effects
        stockReportBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                stockReportBtn.setBackground(Color.decode("#10B981"));
                stockReportBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#34D399"), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon to white on hover
                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    stockReportBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                // Update tooltip to show button name and shortcut
                stockReportBtn.setToolTipText("Generate Stock Report (Ctrl+R or Ctrl+P)");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                stockReportBtn.setBackground(new Color(0, 0, 0, 0)); // Transparent
                stockReportBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                // Change icon back to green
                try {
                    FlatSVGIcon printIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 24, 24);
                    printIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#10B981")));
                    stockReportBtn.setIcon(printIcon);
                } catch (Exception e) {
                    System.err.println("Error loading print icon: " + e.getMessage());
                }

                // Reset tooltip to just button name
                stockReportBtn.setToolTipText("Generate Stock Report");
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                stockReportBtn.setBackground(Color.decode("#059669"));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                stockReportBtn.setBackground(Color.decode("#10B981"));
            }
        });
    }

    // Custom Rounded Border Class
    class RoundedBorder extends javax.swing.border.AbstractBorder {
        private Color color;
        private int thickness;
        private int arc;

        public RoundedBorder(Color color, int thickness, int arc) {
            this.color = color;
            this.thickness = thickness;
            this.arc = arc;
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
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
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            int inset = thickness + 2;
            return new java.awt.Insets(inset, inset, inset, inset);
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c, java.awt.Insets insets) {
            int inset = thickness + 2;
            insets.left = insets.right = insets.top = insets.bottom = inset;
            return insets;
        }
    }

    private void createLoadingPanel() {
        loadingPanel = new JPanel();
        loadingPanel.setLayout(new java.awt.BorderLayout());
        loadingPanel.setBackground(new Color(248, 250, 252, 230));
        loadingPanel.setVisible(false);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new javax.swing.BoxLayout(centerPanel, javax.swing.BoxLayout.Y_AXIS));
        
        loadingLabel = new JLabel("Loading products...");
        loadingLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
        loadingLabel.setForeground(TEAL_BORDER_SELECTED);
        loadingLabel.setHorizontalAlignment(JLabel.CENTER);
        loadingLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        JLabel subLabel = new JLabel("Please wait");
        subLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        subLabel.setForeground(Color.decode("#6B7280"));
        subLabel.setHorizontalAlignment(JLabel.CENTER);
        subLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        centerPanel.add(javax.swing.Box.createVerticalGlue());
        centerPanel.add(loadingLabel);
        centerPanel.add(javax.swing.Box.createVerticalStrut(10));
        centerPanel.add(subLabel);
        centerPanel.add(javax.swing.Box.createVerticalGlue());
        
        loadingPanel.add(centerPanel, java.awt.BorderLayout.CENTER);
        add(loadingPanel, Integer.valueOf(2000));
    }

    private void showLoading(boolean show) {
        SwingUtilities.invokeLater(() -> {
            loadingPanel.setVisible(show);
            if (show) {
                Dimension size = new Dimension(getWidth(), getHeight());
                loadingPanel.setBounds(0, 0, size.width, size.height);
            }
            revalidate();
            repaint();
        });
    }

    private void createPositionIndicator() {
        positionIndicator = new JPanel();
        positionIndicator.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 8));
        positionIndicator.setBackground(new Color(31, 41, 55, 230));
        positionIndicator.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_BORDER_SELECTED, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        positionIndicator.setVisible(false);
        
        positionLabel = new JLabel();
        positionLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14));
        positionLabel.setForeground(Color.WHITE);
        
        positionIndicator.add(positionLabel);
        
        setLayout(new javax.swing.OverlayLayout(this) {
            @Override
            public void layoutContainer(java.awt.Container target) {
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
                
                if (loadingPanel != null && loadingPanel.isVisible()) {
                    loadingPanel.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        });
        
        add(positionIndicator, Integer.valueOf(1000));
    }

    private void createKeyboardHintsPanel() {
        keyboardHintsPanel = new JPanel();
        keyboardHintsPanel.setLayout(new javax.swing.BoxLayout(keyboardHintsPanel, javax.swing.BoxLayout.Y_AXIS));
        keyboardHintsPanel.setBackground(new Color(31, 41, 55, 240));
        keyboardHintsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_BORDER_SELECTED, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        keyboardHintsPanel.setVisible(false);
        
        JLabel title = new JLabel("KEYBOARD SHORTCUTS");
        title.setFont(new java.awt.Font("Nunito ExtraBold", 1, 13));
        title.setForeground(TEAL_BORDER_SELECTED);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        keyboardHintsPanel.add(title);
        
        keyboardHintsPanel.add(javax.swing.Box.createVerticalStrut(10));
        
        // MODIFIED: Updated help hints
        addHintRow("← → ↑ ↓", "Navigate cards", "#FFFFFF");
        addHintRow("Ctrl+← →", "Previous/Next Batch", "#A78BFA");
        addHintRow("B", "Show Barcode", "#FCD34D");
        addHintRow("E", "Edit Product", "#1CB5BB");
        addHintRow("D", "Deactivate Product", "#F87171");
        addHintRow("R", "Reactivate Product", "#10B981");
        addHintRow("Ctrl+F", "Search", "#A78BFA");
        addHintRow("Ctrl+N", "Add Stock", "#34D399");
        addHintRow("Ctrl+R/P", "Stock Report", "#34D399");
        addHintRow("F5", "Refresh List", "#FB923C");
        addHintRow("Alt+1-4", "Quick Filters", "#FB923C");
        addHintRow("Esc", "Clear/Back", "#9CA3AF");
        addHintRow("?", "Toggle Help", "#1CB5BB");
        
        keyboardHintsPanel.add(javax.swing.Box.createVerticalStrut(10));
        
        JLabel closeHint = new JLabel("Press ? to hide");
        closeHint.setFont(new java.awt.Font("Nunito SemiBold", 2, 10));
        closeHint.setForeground(Color.decode("#9CA3AF"));
        closeHint.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        keyboardHintsPanel.add(closeHint);
        
        add(keyboardHintsPanel, Integer.valueOf(1001));
    }

    private void addHintRow(String key, String description, String keyColor) {
        JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 2));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(300, 25));
        
        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(new java.awt.Font("Consolas", 1, 11));
        keyLabel.setForeground(Color.decode(keyColor));
        keyLabel.setPreferredSize(new Dimension(90, 20));
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 11));
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
        int arrowCondition = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
        
        // Normal arrow navigation
        registerKeyAction("LEFT", KeyEvent.VK_LEFT, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_LEFT));
        registerKeyAction("RIGHT", KeyEvent.VK_RIGHT, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_RIGHT));
        registerKeyAction("UP", KeyEvent.VK_UP, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_UP));
        registerKeyAction("DOWN", KeyEvent.VK_DOWN, 0, arrowCondition, () -> navigateCards(KeyEvent.VK_DOWN));
        
        // Ctrl + Arrow keys for batch navigation
        registerKeyAction("CTRL_LEFT", KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK, condition, () -> navigateToPreviousBatch());
        registerKeyAction("CTRL_RIGHT", KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK, condition, () -> navigateToNextBatch());
        
        registerKeyAction("B", KeyEvent.VK_B, 0, condition, () -> openBarcodeForSelectedCard());
        registerKeyAction("E", KeyEvent.VK_E, 0, condition, () -> editSelectedCard());
        registerKeyAction("D", KeyEvent.VK_D, 0, condition, () -> deleteSelectedCard());
        registerKeyAction("R", KeyEvent.VK_R, 0, condition, () -> reactivateSelectedCard());
        
        registerKeyAction("ENTER", KeyEvent.VK_ENTER, 0, condition, () -> {
            if (currentCardIndex == -1 && !productCardsList.isEmpty()) {
                navigateCards(KeyEvent.VK_RIGHT);
            }
        });
        
        registerKeyAction("CTRL_F", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, condition, () -> focusSearch());
        registerKeyAction("SLASH", KeyEvent.VK_SLASH, 0, condition, () -> {
            if (!productSearchBar.hasFocus()) {
                focusSearch();
            }
        });
        registerKeyAction("ESCAPE", KeyEvent.VK_ESCAPE, 0, condition, () -> handleEscape());
        
        // FIXED: Changed addProductDialogActionPerformed to addStockBtnActionPerformed
        registerKeyAction("CTRL_N", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, condition, () -> addStockBtnActionPerformed(null));
        registerKeyAction("ALT_A", KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, condition, () -> addStockBtnActionPerformed(null));

        // MODIFIED: Report and Refresh shortcuts as per user request
        // Ctrl+R = Report (semantic: R for Report)
        // Ctrl+P = Report (semantic: P for Print/Report)
        // F5 = Refresh (universal standard)
        registerKeyAction("CTRL_R", KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, condition, this::showExportOptions);
        registerKeyAction("CTRL_P", KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, condition, this::showExportOptions);
        registerKeyAction("F5", KeyEvent.VK_F5, 0, condition, () -> refreshProducts());
        
        registerKeyAction("ALT_1", KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(expiringRadioBtn));
        registerKeyAction("ALT_2", KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(lowStockRadioBtn));
        registerKeyAction("ALT_3", KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(expiredRadioBtn));
        registerKeyAction("ALT_4", KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK, condition, () -> toggleRadioButton(inactiveRadioBtn));
        registerKeyAction("ALT_0", KeyEvent.VK_0, KeyEvent.ALT_DOWN_MASK, condition, () -> clearFilters());
        
        registerKeyAction("SHIFT_SLASH", KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK, condition, () -> showKeyboardHints());
        
        productSearchBar.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        productSearchBar.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                productSearchBar.setText("");
                buttonGroup1.clearSelection();
                SearchFilters();
                StockPanel.this.requestFocusInWindow();
            }
        });
        
        productSearchBar.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "startNavigation");
        productSearchBar.getActionMap().put("startNavigation", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!productCardsList.isEmpty()) {
                    StockPanel.this.requestFocusInWindow();
                    if (currentCardIndex == -1) {
                        currentCardIndex = 0;
                        selectCurrentCard();
                        scrollToCardSmooth(currentCardIndex);
                        updatePositionIndicator();
                    }
                }
            }
        });
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> StockPanel.this.requestFocusInWindow());
            }
        });
    }

    private void registerKeyAction(String actionName, int keyCode, int modifiers, int condition, Runnable action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        this.getInputMap(condition).put(keyStroke, actionName);
        this.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (productSearchBar.hasFocus() && 
                    keyCode != KeyEvent.VK_ESCAPE && 
                    keyCode != KeyEvent.VK_ENTER &&
                    modifiers == 0 &&
                    keyCode != KeyEvent.VK_SLASH) {
                    return;
                }
                action.run();
            }
        });
    }

    private void navigateToNextBatch() {
        if (currentCardIndex < 0 || currentCardIndex >= productCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        lk.com.pos.privateclasses.RoundedPanel currentCard = productCardsList.get(currentCardIndex);
        Integer currentProductId = (Integer) currentCard.getClientProperty("productId");
        
        if (currentProductId == null) return;
        
        for (int i = currentCardIndex + 1; i < productCardsList.size(); i++) {
            lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(i);
            Integer productId = (Integer) card.getClientProperty("productId");
            
            if (productId != null && productId.equals(currentProductId)) {
                deselectCard(currentCardIndex);
                currentCardIndex = i;
                selectCurrentCard();
                scrollToCardSmooth(currentCardIndex);
                updatePositionIndicator();
                highlightSameProductBatches(currentProductId);
                return;
            }
        }
        
        Integer batchCount = productBatchCount.get(currentProductId);
        if (batchCount != null && batchCount > 1) {
            showPositionIndicator("Already at last batch of this product");
        } else {
            showPositionIndicator("ℹ️ This product has only one batch");
        }
    }

    private void navigateToPreviousBatch() {
        if (currentCardIndex < 0 || currentCardIndex >= productCardsList.size()) {
            showPositionIndicator("Select a card first (use arrow keys)");
            return;
        }
        
        lk.com.pos.privateclasses.RoundedPanel currentCard = productCardsList.get(currentCardIndex);
        Integer currentProductId = (Integer) currentCard.getClientProperty("productId");
        
        if (currentProductId == null) return;
        
        for (int i = currentCardIndex - 1; i >= 0; i--) {
            lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(i);
            Integer productId = (Integer) card.getClientProperty("productId");
            
            if (productId != null && productId.equals(currentProductId)) {
                deselectCard(currentCardIndex);
                currentCardIndex = i;
                selectCurrentCard();
                scrollToCardSmooth(currentCardIndex);
                updatePositionIndicator();
                highlightSameProductBatches(currentProductId);
                return;
            }
        }
        
        Integer batchCount = productBatchCount.get(currentProductId);
        if (batchCount != null && batchCount > 1) {
            showPositionIndicator("Already at first batch of this product");
        } else {
            showPositionIndicator("ℹ️ This product has only one batch");
        }
    }

    private void highlightSameProductBatches(Integer productId) {
        for (lk.com.pos.privateclasses.RoundedPanel card : productCardsList) {
            Integer cardProductId = (Integer) card.getClientProperty("productId");
            if (cardProductId != null && cardProductId.equals(productId) && card != currentFocusedCard) {
                card.setBackground(BATCH_HIGHLIGHT);
            }
        }
        
        Timer resetTimer = new Timer(2000, e -> {
            for (lk.com.pos.privateclasses.RoundedPanel card : productCardsList) {
                if (card != currentFocusedCard) {
                    Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
                    if (pStatusId != null && pStatusId == 2) {
                        card.setBackground(Color.decode("#F8F9FA"));
                    } else {
                        card.setBackground(Color.WHITE);
                    }
                }
            }
            jPanel2.repaint();
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }

    private void navigateCards(int direction) {
        if (productCardsList.isEmpty()) {
            showPositionIndicator("No products available");
            return;
        }
        
        int totalCards = productCardsList.size();
        
        if (currentCardIndex < 0) {
            currentCardIndex = 0;
            selectCurrentCard();
            scrollToCardSmooth(currentCardIndex);
            updatePositionIndicator();
            return;
        }
        
        int oldIndex = currentCardIndex;
        int newIndex = calculateNewIndex(direction, currentCardIndex, totalCards);
        
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
        switch (direction) {
            case KeyEvent.VK_LEFT:
                showPositionIndicator("◀️ Already at the beginning");
                break;
            case KeyEvent.VK_RIGHT:
                showPositionIndicator("▶️ Already at the end");
                break;
            case KeyEvent.VK_UP:
                showPositionIndicator("🔼 Already at the top");
                break;
            case KeyEvent.VK_DOWN:
                showPositionIndicator("🔽 Already at the bottom");
                break;
        }
    }

    private int calculateNewIndex(int direction, int currentIndex, int totalCards) {
        int currentRow = currentIndex / currentColumns;
        int currentCol = currentIndex % currentColumns;
        int totalRows = (int) Math.ceil((double) totalCards / currentColumns);
        
        switch (direction) {
            case KeyEvent.VK_LEFT:
                if (currentCol > 0) {
                    return currentIndex - 1;
                } else if (currentRow > 0) {
                    int prevRowLastIndex = Math.min((currentRow * currentColumns) - 1, totalCards - 1);
                    return prevRowLastIndex;
                } else {
                    return currentIndex;
                }
                
            case KeyEvent.VK_RIGHT:
                if (currentIndex < totalCards - 1) {
                    int nextIndex = currentIndex + 1;
                    int nextRow = nextIndex / currentColumns;
                    if (nextRow == currentRow) {
                        return nextIndex;
                    } else {
                        return nextIndex;
                    }
                } else {
                    return currentIndex;
                }
                
            case KeyEvent.VK_UP:
                if (currentRow > 0) {
                    int targetIndex = currentIndex - currentColumns;
                    return Math.max(0, targetIndex);
                } else {
                    return currentIndex;
                }
                
            case KeyEvent.VK_DOWN:
                int targetIndex = currentIndex + currentColumns;
                if (targetIndex < totalCards) {
                    return targetIndex;
                } else {
                    int lastRowFirstIndex = (totalRows - 1) * currentColumns;
                    int potentialIndex = lastRowFirstIndex + currentCol;
                    
                    if (potentialIndex < totalCards && potentialIndex > currentIndex) {
                        return potentialIndex;
                    } else {
                        return currentIndex;
                    }
                }
        }
        
        return currentIndex;
    }

    private void updatePositionIndicator() {
        if (currentCardIndex >= 0 && currentCardIndex < productCardsList.size()) {
            int row = (currentCardIndex / currentColumns) + 1;
            int col = (currentCardIndex % currentColumns) + 1;
            int totalRows = (int) Math.ceil((double) productCardsList.size() / currentColumns);
            
            lk.com.pos.privateclasses.RoundedPanel currentCard = productCardsList.get(currentCardIndex);
            Integer productId = (Integer) currentCard.getClientProperty("productId");
            Integer batchCount = productBatchCount.get(productId);
            Integer batchIndex = productBatchIndex.get(currentCardIndex);
            Integer pStatusId = (Integer) currentCard.getClientProperty("pStatusId");
            
            String batchInfo = "";
            if (batchCount != null && batchCount > 1 && batchIndex != null) {
                batchInfo = String.format(" | Batch %d/%d (Ctrl+←/→)", batchIndex, batchCount);
            }
            
            String actionKey = "D: Deactivate";
            if (pStatusId != null && pStatusId == 2) {
                actionKey = "R: Reactivate";
            }
            
            String text = String.format("Card %d/%d (Row %d/%d, Col %d)%s | B: Barcode | E: Edit | %s", 
                currentCardIndex + 1, 
                productCardsList.size(),
                row,
                totalRows,
                col,
                batchInfo,
                actionKey
            );
            
            showPositionIndicator(text);
        }
    }

    private void selectCurrentCard() {
        if (currentCardIndex >= 0 && currentCardIndex < productCardsList.size()) {
            lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(currentCardIndex);
            
            card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(TEAL_BORDER_SELECTED, 4, 15),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
            ));
            
            card.setBackground(card.getBackground().brighter());
            currentFocusedCard = card;
        }
    }

    private void deselectCard(int index) {
        if (index >= 0 && index < productCardsList.size()) {
            lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(index);
            
            card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(DEFAULT_BORDER, 2, 15),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
            ));
            
            Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
            if (pStatusId != null && pStatusId == 2) {
                card.setBackground(Color.decode("#F8F9FA"));
            } else {
                card.setBackground(Color.WHITE);
            }
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
        if (index < 0 || index >= productCardsList.size()) return;
        
        SwingUtilities.invokeLater(() -> {
            try {
                lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(index);
                
                Point cardLocation = card.getLocation();
                Dimension cardSize = card.getSize();
                Rectangle viewRect = jScrollPane1.getViewport().getViewRect();
                
                int cardTop = cardLocation.y;
                int cardBottom = cardLocation.y + cardSize.height;
                int viewTop = viewRect.y;
                int viewBottom = viewRect.y + viewRect.height;
                
                boolean needsScroll = false;
                int targetY = viewRect.y;
                
                if (cardTop < viewTop) {
                    targetY = Math.max(0, cardTop - 50);
                    needsScroll = true;
                } else if (cardBottom > viewBottom) {
                    targetY = cardBottom - viewRect.height + 50;
                    needsScroll = true;
                } else if (cardTop < viewTop + 100 && cardTop >= viewTop) {
                    targetY = Math.max(0, cardTop - 100);
                    needsScroll = true;
                } else if (cardBottom > viewBottom - 100 && cardBottom <= viewBottom) {
                    targetY = cardBottom - viewRect.height + 100;
                    needsScroll = true;
                }
                
                if (needsScroll) {
                    final int startY = viewRect.y;
                    final int endY = targetY;
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
                            jScrollPane1.getViewport().setViewPosition(new Point(viewRect.x, newY));
                        } else {
                            scrollTimer.stop();
                        }
                    });
                    
                    scrollTimer.start();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void openBarcodeForSelectedCard() {
        if (currentCardIndex >= 0 && currentCardIndex < productCardsList.size()) {
            lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(currentCardIndex);
            String barcode = (String) card.getClientProperty("barcode");
            String productName = (String) card.getClientProperty("productName");
            Double sellingPrice = (Double) card.getClientProperty("sellingPrice");
            
            if (barcode != null && !barcode.isEmpty()) {
                openBarcodeDialog(barcode, productName, sellingPrice != null ? sellingPrice : 0.0);
                SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
            } else {
                showPositionIndicator("No barcode available");
            }
        } else {
            showPositionIndicator("Select a card first (use arrow keys)");
        }
    }

    private void editSelectedCard() {
        if (currentCardIndex >= 0 && currentCardIndex < productCardsList.size()) {
            lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(currentCardIndex);
            Integer productId = (Integer) card.getClientProperty("productId");
            Integer stockId = (Integer) card.getClientProperty("stockId");
            
            if (productId != null && stockId != null) {
                editProduct(productId, stockId);
                SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
            }
        } else {
            showPositionIndicator("Select a card first (use arrow keys)");
        }
    }

    private void deleteSelectedCard() {
        if (currentCardIndex >= 0 && currentCardIndex < productCardsList.size()) {
            lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(currentCardIndex);
            Integer productId = (Integer) card.getClientProperty("productId");
            String productName = (String) card.getClientProperty("productName");
            Integer stockId = (Integer) card.getClientProperty("stockId");
            Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
            
            if (productId != null && productName != null && stockId != null) {
                if (pStatusId != null && pStatusId == 2) {
                    showPositionIndicator("ℹ️ Product is inactive - Use R to reactivate");
                } else {
                    deleteProduct(productId, productName, stockId);
                }
                SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
            }
        } else {
            showPositionIndicator("Select a card first (use arrow keys)");
        }
    }

    private void reactivateSelectedCard() {
        if (currentCardIndex >= 0 && currentCardIndex < productCardsList.size()) {
            lk.com.pos.privateclasses.RoundedPanel card = productCardsList.get(currentCardIndex);
            Integer productId = (Integer) card.getClientProperty("productId");
            String productName = (String) card.getClientProperty("productName");
            Integer pStatusId = (Integer) card.getClientProperty("pStatusId");
            
            if (productId != null && productName != null) {
                if (pStatusId != null && pStatusId == 2) {
                    reactivateProduct(productId, productName);
                } else {
                    showPositionIndicator("ℹ️ Product is already active - Use D to deactivate");
                }
                SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
            }
        } else {
            showPositionIndicator("Select a card first (use arrow keys)");
        }
    }

    private void focusSearch() {
        productSearchBar.requestFocus();
        productSearchBar.selectAll();
        showPositionIndicator("🔍 Search mode - Type to filter products (Press ↓ to navigate results)");
    }

    private void handleEscape() {
        if (currentCardIndex >= 0) {
            deselectCurrentCard();
            showPositionIndicator("Card deselected");
        } else if (!productSearchBar.getText().isEmpty() || buttonGroup1.getSelection() != null) {
            productSearchBar.setText("");
            buttonGroup1.clearSelection();
            SearchFilters();
            showPositionIndicator("Filters cleared");
        }
        this.requestFocusInWindow();
    }

    private void refreshProducts() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < REFRESH_COOLDOWN) {
            showPositionIndicator("Please wait before refreshing again");
            return;
        }
        
        lastRefreshTime = currentTime;
        SearchFilters();
        showPositionIndicator("Products refreshed");
        this.requestFocusInWindow();
    }

    private void clearFilters() {
        buttonGroup1.clearSelection();
        SearchFilters();
        showPositionIndicator("All filters cleared");
        this.requestFocusInWindow();
    }

    private void toggleRadioButton(javax.swing.JRadioButton radioBtn) {
        if (radioBtn.isSelected()) {
            buttonGroup1.clearSelection();
            showPositionIndicator("Filter removed: " + radioBtn.getText());
        } else {
            radioBtn.setSelected(true);
            showPositionIndicator("Filter applied: " + radioBtn.getText());
        }
        SearchFilters();
        this.requestFocusInWindow();
    }

    private void setupEventListeners() {
        productSearchBar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SearchFilters();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SearchFilters();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SearchFilters();
            }
        });

        productSearchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (productSearchBar.getText().equals("Search By Product Name Or Barcode")) {
                    productSearchBar.setText("");
                    productSearchBar.setForeground(java.awt.Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (productSearchBar.getText().isEmpty()) {
                    productSearchBar.setText("Search By Product Name Or Barcode");
                    productSearchBar.setForeground(java.awt.Color.GRAY);
                }
            }
        });

        productSearchBar.setForeground(Color.GRAY);

        expiringRadioBtn.addActionListener(e -> {
            SearchFilters();
            this.requestFocusInWindow();
        });
        lowStockRadioBtn.addActionListener(e -> {
            SearchFilters();
            this.requestFocusInWindow();
        });
        expiredRadioBtn.addActionListener(e -> {
            SearchFilters();
            this.requestFocusInWindow();
        });
        inactiveRadioBtn.addActionListener(e -> {
            SearchFilters();
            this.requestFocusInWindow();
        });
    }

    private void SearchFilters() {
        String productSearch = productSearchBar.getText().trim();

        String status = "all";

        if (expiringRadioBtn.isSelected()) {
            status = "Expiring Soon";
        } else if (lowStockRadioBtn.isSelected()) {
            status = "Low Stock";
        } else if (expiredRadioBtn.isSelected()) {
            status = "Expired";
        } else if (inactiveRadioBtn.isSelected()) {
            status = "Inactive";
        }

        loadProduct(productSearch, status);
    }

    private void loadProduct() {
        loadProduct("", "all");
    }

    private void loadProduct(String productSearch, String status) {
        showLoading(true);
        
        SwingWorker<List<ProductStockDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ProductStockDTO> doInBackground() throws Exception {
                try {
                    // Get batch counts first
                    productBatchCount = productStockDAO.getBatchCounts();
                    // Get product stock data
                    return productStockDAO.getProductStock(productSearch, status, productBatchCount);
                } catch (Exception e) {
                    throw new Exception("Error loading products: " + e.getMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<ProductStockDTO> products = get();
                    displayProducts(products);
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(StockPanel.this,
                                "Error loading products: " + e.getMessage(),
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE);
                    });
                } finally {
                    showLoading(false);
                }
            }
        };
        
        worker.execute();
    }

    private void displayProducts(List<ProductStockDTO> products) {
        clearProductCards();
        
        productBatchIndex.clear();
        currentCardIndex = -1;
        currentFocusedCard = null;

        if (products.isEmpty()) {
            showEmptyState();
            return;
        }

        // UPDATED: Calculate responsive columns based on current panel width
        currentColumns = calculateColumns(jPanel2.getWidth());
        
        final JPanel gridPanel = createGridPanel();
        
        for (int i = 0; i < products.size(); i++) {
            ProductStockDTO data = products.get(i);
            lk.com.pos.privateclasses.RoundedPanel card = createProductCard(data);
            gridPanel.add(card);
            productCardsList.add(card);
            productBatchIndex.put(i, data.getBatchIndex());
        }

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(gridPanel, java.awt.BorderLayout.NORTH);
        setupGridResizeListener(gridPanel);
        
        jPanel2.revalidate();
        jPanel2.repaint();
    }

    private void clearProductCards() {
        // Remove all listeners to prevent memory leaks
        for (lk.com.pos.privateclasses.RoundedPanel card : productCardsList) {
            for (java.awt.event.MouseListener ml : card.getMouseListeners()) {
                card.removeMouseListener(ml);
            }
            
            for (Component comp : getAllComponents(card)) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                        btn.removeActionListener(al);
                    }
                }
            }
        }
        
        productCardsList.clear();
        jPanel2.removeAll();
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
        jPanel2.setLayout(new java.awt.BorderLayout());
        JPanel messagePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        messagePanel.setBackground(Color.decode("#F8FAFC"));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        
        String message = getEmptyStateMessage();
        
        JLabel noProducts = new JLabel(message);
        noProducts.setFont(new java.awt.Font("Nunito SemiBold", 0, 18));
        noProducts.setForeground(Color.decode("#6B7280"));
        noProducts.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        messagePanel.add(noProducts);
        jPanel2.add(messagePanel, java.awt.BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
    }

    private String getEmptyStateMessage() {
        if (expiringRadioBtn.isSelected()) {
            return "No expiring soon products - Everything is good!";
        } else if (lowStockRadioBtn.isSelected()) {
            return "No low stock products - Inventory is healthy!";
        } else if (expiredRadioBtn.isSelected()) {
            return "No expired products - Great inventory management!";
        } else if (inactiveRadioBtn.isSelected()) {
            return "No inactive products with stock available";
        }
        return "No products with stock available";
    }

    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel();
        // UPDATED: Responsive grid gap
        int responsiveGap = Math.max(15, Math.min(gridGap, jPanel2.getWidth() / 50));
        gridPanel.setLayout(new java.awt.GridLayout(0, currentColumns, responsiveGap, responsiveGap));
        gridPanel.setBackground(Color.decode("#F8FAFC"));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(responsiveGap, responsiveGap, responsiveGap, responsiveGap));
        return gridPanel;
    }

    private void setupGridResizeListener(final JPanel gridPanel) {
        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int panelWidth = jPanel2.getWidth();
                int newColumns = calculateColumns(panelWidth);

                if (newColumns != currentColumns) {
                    currentColumns = newColumns;
                    // UPDATED: Responsive grid gap on resize
                    int responsiveGap = Math.max(15, Math.min(gridGap, panelWidth / 50));
                    gridPanel.setLayout(new java.awt.GridLayout(0, newColumns, responsiveGap, responsiveGap));
                    gridPanel.setBorder(BorderFactory.createEmptyBorder(responsiveGap, responsiveGap, responsiveGap, responsiveGap));
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }
        });
    }

    // UPDATED: More responsive column calculation
    private int calculateColumns(int panelWidth) {
        // Subtract scrollbar and padding
        int availableWidth = panelWidth - 50;
        
        // Dynamic card width based on screen size
        int responsiveCardWidth = Math.max(350, Math.min(cardWidth, availableWidth / 2));
        int cardWithGap = responsiveCardWidth + gridGap;

        if (availableWidth >= cardWithGap * 4) {
            return 4;  // Extra large screens
        } else if (availableWidth >= cardWithGap * 3) {
            return 3;
        } else if (availableWidth >= cardWithGap * 2) {
            return 2;
        } else {
            return 1;
        }
    }

    private lk.com.pos.privateclasses.RoundedPanel createProductCard(ProductStockDTO data) {
        lk.com.pos.privateclasses.RoundedPanel card = new lk.com.pos.privateclasses.RoundedPanel();
        card.setLayout(new java.awt.BorderLayout());
        
        // UPDATED: More flexible card sizing
        card.setPreferredSize(new java.awt.Dimension(cardWidth, cardHeight));
        card.setMaximumSize(new java.awt.Dimension(cardWidth + 50, cardHeight + 50));
        card.setMinimumSize(new java.awt.Dimension(cardWidth - 70, cardHeight - 50));
        
        // Store all properties in the card
        card.putClientProperty("productId", data.getProductId());
        card.putClientProperty("productName", data.getProductName());
        card.putClientProperty("stockId", data.getStockId());
        card.putClientProperty("pStatusId", data.getPStatusId());
        card.putClientProperty("barcode", data.getBarcode());
        card.putClientProperty("batchIndex", data.getBatchIndex());
        card.putClientProperty("totalBatches", data.getTotalBatches());
        card.putClientProperty("sellingPrice", data.getSellingPrice());
        
        if (data.getPStatusId() == 2) {
            card.setBackground(Color.decode("#F8F9FA"));
        } else {
            card.setBackground(java.awt.Color.WHITE);
        }
        
        card.setBorderThickness(0);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(DEFAULT_BORDER, 2, 15),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(
                            data.getPStatusId() == 2 ? Color.decode("#9CA3AF") : TEAL_BORDER_HOVER, 2, 15),
                        BorderFactory.createEmptyBorder(16, 16, 16, 16)
                    ));
                }
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (card != currentFocusedCard) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(DEFAULT_BORDER, 2, 15),
                        BorderFactory.createEmptyBorder(16, 16, 16, 16)
                    ));
                }
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (currentFocusedCard != null && currentFocusedCard != card) {
                    deselectCurrentCard();
                }
                
                currentCardIndex = productCardsList.indexOf(card);
                selectCurrentCard();
                updatePositionIndicator();
                StockPanel.this.requestFocusInWindow();
            }
        });

        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS));
        contentPanel.setBackground(card.getBackground());
        contentPanel.setOpaque(false);

        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));

        javax.swing.JLabel nameLabel = new javax.swing.JLabel(data.getProductName());
        nameLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
        if (data.getPStatusId() == 2) {
            nameLabel.setForeground(Color.decode("#6B7280"));
        } else {
            nameLabel.setForeground(Color.decode("#1E293B"));
        }
        nameLabel.setToolTipText(data.getProductName());
        headerPanel.add(nameLabel, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel actionPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        // Edit button
        javax.swing.JButton editButton = new javax.swing.JButton();
        editButton.setPreferredSize(new java.awt.Dimension(30, 30));
        editButton.setMinimumSize(new java.awt.Dimension(30, 30));
        editButton.setMaximumSize(new java.awt.Dimension(30, 30));
        editButton.setBackground(Color.decode("#EFF6FF"));
        editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 16, 16);
            editButton.setIcon(editIcon);
        } catch (Exception e) {
            editButton.setText("✎");
            editButton.setForeground(Color.decode("#3B82F6"));
            editButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        }
        editButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#BFDBFE"), 1));
        editButton.setFocusable(false);
        
        editButton.setToolTipText("Edit Product (E)");
        editButton.addActionListener(e -> {
            editProduct(data.getProductId(), data.getStockId());
            StockPanel.this.requestFocusInWindow();
        });

        // Delete/Reactivate button
        javax.swing.JButton actionButton = new javax.swing.JButton();
        actionButton.setPreferredSize(new java.awt.Dimension(30, 30));
        actionButton.setMinimumSize(new java.awt.Dimension(30, 30));
        actionButton.setMaximumSize(new java.awt.Dimension(30, 30));
        
        if (data.getPStatusId() == 2) {
            actionButton.setBackground(Color.decode("#D1FAE5"));
            actionButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#10B981"), 1));
            actionButton.setToolTipText("Reactivate Product (R)");
            try {
                FlatSVGIcon redirectIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 16, 16);
                actionButton.setIcon(redirectIcon);
            } catch (Exception e) {
                actionButton.setText("↻");
                actionButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
            }
            actionButton.addActionListener(e -> {
                reactivateProduct(data.getProductId(), data.getProductName());
                StockPanel.this.requestFocusInWindow();
            });
        } else {
            actionButton.setBackground(Color.decode("#FEF2F2"));
            actionButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
            actionButton.setToolTipText("Deactivate Product (D)");
            try {
                FlatSVGIcon deleteIcon = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 16, 16);
                actionButton.setIcon(deleteIcon);
            } catch (Exception e) {
                actionButton.setText("×");
                actionButton.setFont(new java.awt.Font("Nunito ExtraBold", 1, 20));
            }
            actionButton.addActionListener(e -> {
                deleteProduct(data.getProductId(), data.getProductName(), data.getStockId());
                StockPanel.this.requestFocusInWindow();
            });
        }
        
        actionButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        actionButton.setFocusable(false);

        actionPanel.add(editButton);
        actionPanel.add(actionButton);
        headerPanel.add(actionPanel, java.awt.BorderLayout.EAST);

        contentPanel.add(headerPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(8));

        javax.swing.JPanel supplierBadgePanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        supplierBadgePanel.setOpaque(false);
        supplierBadgePanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));

        javax.swing.JLabel supplierLabel = new javax.swing.JLabel(data.getSupplierName());
        supplierLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 14));
        if (data.getPStatusId() == 2) {
            supplierLabel.setForeground(Color.decode("#9CA3AF"));
        } else {
            supplierLabel.setForeground(Color.decode("#6366F1"));
        }
        supplierLabel.setToolTipText("Supplier: " + data.getSupplierName());
        supplierBadgePanel.add(supplierLabel, java.awt.BorderLayout.WEST);

        javax.swing.JPanel badgePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        badgePanel.setOpaque(false);

        // Batch indicator badge
        if (data.getTotalBatches() > 1 && data.getPStatusId() == 1) {
            javax.swing.JLabel batchBadge = new javax.swing.JLabel(String.format("Batch %d/%d", data.getBatchIndex(), data.getTotalBatches()));
            batchBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
            batchBadge.setForeground(Color.WHITE);
            batchBadge.setBackground(Color.decode("#8B5CF6"));
            batchBadge.setOpaque(true);
            batchBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            batchBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.decode("#A78BFA"), 1),
                    javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            batchBadge.setToolTipText("This product has " + data.getTotalBatches() + " batches. Press Ctrl+←/→ to navigate between them.");
            badgePanel.add(batchBadge);
        }

        if (data.getPStatusId() == 2) {
            javax.swing.JLabel inactiveBadge = new javax.swing.JLabel("Inactive");
            inactiveBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
            inactiveBadge.setForeground(Color.WHITE);
            inactiveBadge.setBackground(Color.decode("#6B7280"));
            inactiveBadge.setOpaque(true);
            inactiveBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            inactiveBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.decode("#9CA3AF"), 1),
                    javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            badgePanel.add(inactiveBadge);
        }

        if (data.getPStatusId() == 1) {
            boolean isExpired = data.isExpired();
            
            if (isExpired) {
                javax.swing.JLabel expiredBadge = new javax.swing.JLabel("Expired");
                expiredBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
                expiredBadge.setForeground(Color.decode("#7C2D12"));
                expiredBadge.setBackground(Color.decode("#FED7AA"));
                expiredBadge.setOpaque(true);
                expiredBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                expiredBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(Color.decode("#FB923C"), 1),
                        javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10)
                ));
                badgePanel.add(expiredBadge);
            } else if (data.isExpiringSoon()) {
                javax.swing.JLabel expiringBadge = new javax.swing.JLabel("Expiring Soon");
                expiringBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
                expiringBadge.setForeground(Color.decode("#92400E"));
                expiringBadge.setBackground(Color.decode("#FEF3C7"));
                expiringBadge.setOpaque(true);
                expiringBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                expiringBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(Color.decode("#FDE047"), 1),
                        javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
                badgePanel.add(expiringBadge);
            }

            if (data.isLowStock()) {
                javax.swing.JLabel lowStockBadge = new javax.swing.JLabel("Low Stock");
                lowStockBadge.setFont(new java.awt.Font("Nunito ExtraBold", 1, 10));
                lowStockBadge.setForeground(java.awt.Color.WHITE);
                lowStockBadge.setBackground(Color.decode("#DC2626"));
                lowStockBadge.setOpaque(true);
                lowStockBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                lowStockBadge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(Color.decode("#FCA5A5"), 1),
                        javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
                badgePanel.add(lowStockBadge);
            }
        }

        supplierBadgePanel.add(badgePanel, java.awt.BorderLayout.EAST);

        contentPanel.add(supplierBadgePanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        javax.swing.JPanel detailsHeaderPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        detailsHeaderPanel.setOpaque(false);
        detailsHeaderPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 20));

        javax.swing.JLabel detailsHeader = new javax.swing.JLabel("PRODUCT DETAILS");
        detailsHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
        if (data.getPStatusId() == 2) {
            detailsHeader.setForeground(Color.decode("#9CA3AF"));
        } else {
            detailsHeader.setForeground(Color.decode("#94A3B8"));
        }
        detailsHeaderPanel.add(detailsHeader);

        contentPanel.add(detailsHeaderPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));

        javax.swing.JPanel detailsGrid = new javax.swing.JPanel(new java.awt.GridLayout(3, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 180));

        detailsGrid.add(createDetailPanel("Brand", data.getBrandName(), 
            data.getPStatusId() == 2 ? Color.decode("#9CA3AF") : Color.decode("#8B5CF6")));
        detailsGrid.add(createDetailPanel("Category", data.getCategoryName(), 
            data.getPStatusId() == 2 ? Color.decode("#9CA3AF") : Color.decode("#EC4899")));

        detailsGrid.add(createDetailPanel("Quantity", data.getQty() + " units", 
            data.getPStatusId() == 2 ? Color.decode("#9CA3AF") : Color.decode("#10B981")));
        detailsGrid.add(createDetailPanel("Expiry Date", data.getExpiryDate(), 
            data.getPStatusId() == 2 ? Color.decode("#9CA3AF") : Color.decode("#F59E0B")));

        detailsGrid.add(createDetailPanel("Batch No", data.getBatchNo(), 
            data.getPStatusId() == 2 ? Color.decode("#9CA3AF") : Color.decode("#06B6D4")));
        detailsGrid.add(createBarcodePanel(data.getBarcode(), data.getPStatusId(), 
            data.getProductName(), data.getSellingPrice()));

        contentPanel.add(detailsGrid);
        contentPanel.add(javax.swing.Box.createVerticalStrut(20));

        javax.swing.JPanel priceHeaderPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        priceHeaderPanel.setOpaque(false);
        priceHeaderPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 20));

        javax.swing.JLabel priceHeader = new javax.swing.JLabel("PRICING");
        priceHeader.setFont(new java.awt.Font("Nunito ExtraBold", 1, 11));
        if (data.getPStatusId() == 2) {
            priceHeader.setForeground(Color.decode("#9CA3AF"));
        } else {
            priceHeader.setForeground(Color.decode("#94A3B8"));
        }
        priceHeaderPanel.add(priceHeader);

        contentPanel.add(priceHeaderPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(10));

        javax.swing.JPanel pricePanel = new javax.swing.JPanel(new java.awt.GridLayout(1, 3, 10, 0));
        pricePanel.setOpaque(false);
        pricePanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 55));

        lk.com.pos.privateclasses.RoundedPanel purchasePanel = createPricePanel(
                "Purchase",
                data.getPurchasePrice(),
                data.getPStatusId() == 2 ? Color.decode("#F3F4F6") : Color.decode("#DBEAFE"),
                data.getPStatusId() == 2 ? Color.decode("#6B7280") : Color.decode("#1E40AF")
        );

        lk.com.pos.privateclasses.RoundedPanel lastPricePanel = createPricePanel(
                "Last Price",
                data.getLastPrice(),
                data.getPStatusId() == 2 ? Color.decode("#F3F4F6") : Color.decode("#F3E8FF"),
                data.getPStatusId() == 2 ? Color.decode("#6B7280") : Color.decode("#7C3AED")
        );

        lk.com.pos.privateclasses.RoundedPanel sellingPanel = createPricePanel(
                "Selling",
                data.getSellingPrice(),
                data.getPStatusId() == 2 ? Color.decode("#F3F4F6") : Color.decode("#D1FAE5"),
                data.getPStatusId() == 2 ? Color.decode("#6B7280") : Color.decode("#059669")
        );

        pricePanel.add(purchasePanel);
        pricePanel.add(lastPricePanel);
        pricePanel.add(sellingPanel);

        contentPanel.add(pricePanel);

        card.add(contentPanel, java.awt.BorderLayout.CENTER);
        return card;
    }

    private javax.swing.JPanel createBarcodePanel(String barcode, int pStatusId, String productName, double sellingPrice) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        javax.swing.JLabel titleLabel = new javax.swing.JLabel("Barcode");
        titleLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 13));
        titleLabel.setForeground(pStatusId == 2 ? Color.decode("#9CA3AF") : Color.decode("#EF4444"));
        titleLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        javax.swing.JPanel barcodeRow = new javax.swing.JPanel(new java.awt.BorderLayout(5, 0));
        barcodeRow.setOpaque(false);
        barcodeRow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 25));
        barcodeRow.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        javax.swing.JLabel valueLabel = new javax.swing.JLabel(barcode);
        valueLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        valueLabel.setForeground(pStatusId == 2 ? Color.decode("#6B7280") : Color.decode("#1E293B"));
        valueLabel.setToolTipText("Barcode: " + barcode);

        if (pStatusId == 1) {
            try {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 18, 18);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.RED));

                JButton barcodeBtn = new JButton(normalIcon);
                barcodeBtn.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14));
                barcodeBtn.setPreferredSize(new java.awt.Dimension(35, 35));
                barcodeBtn.setBackground(Color.decode("#FEF2F2"));
                barcodeBtn.setForeground(Color.decode("#EF4444"));
                barcodeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                barcodeBtn.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
                barcodeBtn.setFocusable(false);
                barcodeBtn.setToolTipText("View Barcode Details & Copy (B)");
                
                barcodeBtn.addActionListener(e -> {
                    openBarcodeDialog(barcode, productName, sellingPrice);
                    StockPanel.this.requestFocusInWindow();
                });

                barcodeRow.add(barcodeBtn, java.awt.BorderLayout.EAST);
            } catch (Exception e) {
                JButton barcodeBtn = new JButton("📊");
                barcodeBtn.setPreferredSize(new java.awt.Dimension(35, 35));
                barcodeBtn.setBackground(Color.decode("#FEF2F2"));
                barcodeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                barcodeBtn.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#FECACA"), 1));
                barcodeBtn.setFocusable(false);
                barcodeBtn.setToolTipText("View Barcode Details & Copy (B)");
                barcodeBtn.addActionListener(ev -> {
                    openBarcodeDialog(barcode, productName, sellingPrice);
                    StockPanel.this.requestFocusInWindow();
                });
                barcodeRow.add(barcodeBtn, java.awt.BorderLayout.EAST);
            }
        }

        barcodeRow.add(valueLabel, java.awt.BorderLayout.CENTER);

        panel.add(titleLabel);
        panel.add(javax.swing.Box.createVerticalStrut(5));
        panel.add(barcodeRow);

        return panel;
    }

    private void openBarcodeDialog(String barcode, String productName, double sellingPrice) {
        PrintProductLabel dialog = new PrintProductLabel(null, true, barcode, productName, sellingPrice);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private javax.swing.JPanel createDetailPanel(String title, String value, Color accentColor) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        javax.swing.JLabel titleLabel = new javax.swing.JLabel(title);
        titleLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 13));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        String displayValue = value;
        if (value != null && value.length() > 25) {
            displayValue = "<html><div style='width:140px;'>" + value + "</div></html>";
        }

        javax.swing.JLabel valueLabel = new javax.swing.JLabel(displayValue);
        valueLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        valueLabel.setForeground(Color.decode("#1E293B"));
        valueLabel.setToolTipText(value);
        valueLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(javax.swing.Box.createVerticalStrut(5));
        panel.add(valueLabel);

        return panel;
    }

    private lk.com.pos.privateclasses.RoundedPanel createPricePanel(String title, double price, Color bgColor, Color textColor) {
        lk.com.pos.privateclasses.RoundedPanel panel = new lk.com.pos.privateclasses.RoundedPanel();
        panel.setBackgroundColor(bgColor);
        panel.setBorderThickness(0);
        panel.setLayout(new java.awt.GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        javax.swing.JLabel titleLabel = new javax.swing.JLabel(title);
        titleLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 12));
        titleLabel.setForeground(textColor);
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panel.add(titleLabel, gbc);

        String formattedPrice = formatPrice(price);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(5, 0, 0, 0);

        javax.swing.JLabel priceLabel = new javax.swing.JLabel(formattedPrice);
        priceLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16));
        priceLabel.setForeground(textColor);
        priceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        priceLabel.setToolTipText(title + ": Rs." + String.format("%.2f", price));
        panel.add(priceLabel, gbc);

        return panel;
    }

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

    private void editProduct(int productId, int stockId) {
        UpdateProductStock updateProductStock = new UpdateProductStock(null, true, productId, stockId);
        updateProductStock.setLocationRelativeTo(null);
        updateProductStock.setVisible(true);
        SearchFilters();
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    private void deleteProduct(int productId, String productName, int stockId) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to deactivate '" + productName + "'?\n"
                + "Note: Product will be marked as inactive and hidden from the active product list.",
                "Confirm Deactivate",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean hasSales = productStockDAO.hasSalesHistory(stockId);
                
                boolean success = productStockDAO.updateProductStatus(productId, 2);

                if (success) {
                    String message = "Product '" + productName + "' has been deactivated successfully!\n" +
                                    "You can view it in the 'Inactive Products' section.";
                    
                    if (hasSales) {
                        message += "\n\nNote: This product batch has sales history and cannot be permanently deleted.";
                    }

                    JOptionPane.showMessageDialog(this,
                            message,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    SearchFilters();
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error deactivating product: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    private void reactivateProduct(int productId, String productName) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reactivate '" + productName + "'?\n"
                + "Product will be available in the active product list.",
                "Confirm Reactivate",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = productStockDAO.updateProductStatus(productId, 1);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Product '" + productName + "' has been reactivated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    SearchFilters();
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error reactivating product: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        
        SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
    }

    private void radioButtonListener() {
        expiringRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (expiringRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    SearchFilters();
                    evt.consume();
                }
            }
        });

        lowStockRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (lowStockRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    SearchFilters();
                    evt.consume();
                }
            }
        });

        expiredRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (expiredRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    SearchFilters();
                    evt.consume();
                }
            }
        });

        inactiveRadioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (inactiveRadioBtn.isSelected()) {
                    buttonGroup1.clearSelection();
                    SearchFilters();
                    evt.consume();
                }
            }
        });
    }
    
    // ============================================================================
    // REPORT FUNCTIONALITY
    // ============================================================================
    
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
            generateStockReport();
        });

        JButton excelButton = new JButton("Export to Excel");
        excelButton.setFont(new java.awt.Font("Nunito SemiBold", 1, 14));
        excelButton.setBackground(new Color(16, 185, 129));
        excelButton.setForeground(Color.WHITE);
        excelButton.addActionListener(e -> {
            dialog.dispose();
            exportStockToExcel();
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
    
    private void generateStockReport() {
        try {
            // Set font properties to prevent font errors
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            System.setProperty("net.sf.jasperreports.default.font.name", "Arial");
            System.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");

            // Get current filters
            String productSearch = productSearchBar.getText().trim();
            String status = "all";

            if (expiringRadioBtn.isSelected()) {
                status = "Expiring Soon";
            } else if (lowStockRadioBtn.isSelected()) {
                status = "Low Stock";
            } else if (expiredRadioBtn.isSelected()) {
                status = "Expired";
            } else if (inactiveRadioBtn.isSelected()) {
                status = "Inactive";
            }

            // Get batch counts first
            productBatchCount = productStockDAO.getBatchCounts();
            // Get product stock data
            List<ProductStockDTO> products = productStockDAO.getProductStock(productSearch, status, productBatchCount);

            if (products.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No stock data found for the selected filters.",
                    "No Data", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create a list of maps for the report data - MATCHING JRXML FIELD NAMES
            List<Map<String, Object>> reportData = new ArrayList<>();
            int totalQuantity = 0;
            int activeCount = 0;
            int inactiveCount = 0;
            int expiringCount = 0;
            int lowStockCount = 0;
            int expiredCount = 0;

            for (ProductStockDTO product : products) {
                Map<String, Object> row = new HashMap<>();
                
                // Match the exact field names from the JRXML
                row.put("product_name", product.getProductName());
                row.put("quantity", String.valueOf(product.getQty()));
                row.put("expiryDate", product.getExpiryDate());
                row.put("batchNo", product.getBatchNo());
                row.put("barcodeNo", product.getBarcode());
                row.put("perchasePrice", String.format("Rs. %.2f", product.getPurchasePrice()));
                row.put("lastPrice", String.format("Rs. %.2f", product.getLastPrice()));
                row.put("sellingPrice", String.format("Rs. %.2f", product.getSellingPrice()));
                
                reportData.add(row);

                // Count statistics
                totalQuantity += product.getQty();
                
                if (product.getPStatusId() == 1) {
                    activeCount++;
                    if (product.isExpiringSoon()) expiringCount++;
                    if (product.isLowStock()) lowStockCount++;
                    if (product.isExpired()) expiredCount++;
                } else {
                    inactiveCount++;
                }
            }

            // Prepare parameters for the report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Stock Report");
            parameters.put("generatedDate", REPORT_DATE_FORMAT.format(new Date()));
            parameters.put("totalProducts", products.size());
            parameters.put("totalQuantity", totalQuantity);
            parameters.put("activeProducts", activeCount);
            parameters.put("inactiveProducts", inactiveCount);
            parameters.put("expiringProducts", expiringCount);
            parameters.put("lowStockProducts", lowStockCount);
            parameters.put("expiredProducts", expiredCount);
            parameters.put("filterInfo", getStockFilterInfo(productSearch, status));
            
            // Set default font parameters
            parameters.put("REPORT_FONT", "Arial");
            parameters.put("REPORT_PDF_FONT", "Helvetica");

            // Load the JRXML template from classpath
            InputStream jrxmlStream = getClass().getResourceAsStream("/lk/com/pos/reports/stockReport.jrxml");
            
            if (jrxmlStream == null) {
                // Try alternative path
                jrxmlStream = getClass().getClassLoader().getResourceAsStream("lk/com/pos/reports/stockReport.jrxml");
                if (jrxmlStream == null) {
                    // Try to load from file system
                    File jrxmlFile = new File("src/main/resources/lk/com/pos/reports/stockReport.jrxml");
                    if (jrxmlFile.exists()) {
                        jrxmlStream = new java.io.FileInputStream(jrxmlFile);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Report template not found. Please ensure stockReport.jrxml is in the classpath.",
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

            showPositionIndicator("✅ Stock report generated successfully");

        } catch (JRException e) {
            e.printStackTrace();
            
            // Try with simplified font settings
            try {
                generateStockReportWithSimpleFont();
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
    private void generateStockReportWithSimpleFont() throws Exception {
        // Create a simple HTML report as fallback
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Stock Report</title></head><body>");
        html.append("<h1>Stock Report</h1>");
        html.append("<p>Generated: ").append(new java.util.Date()).append("</p>");

        // Get current filters
        String productSearch = productSearchBar.getText().trim();
        String status = "all";

        if (expiringRadioBtn.isSelected()) {
            status = "Expiring Soon";
        } else if (lowStockRadioBtn.isSelected()) {
            status = "Low Stock";
        } else if (expiredRadioBtn.isSelected()) {
            status = "Expired";
        } else if (inactiveRadioBtn.isSelected()) {
            status = "Inactive";
        }

        // Get batch counts first
        productBatchCount = productStockDAO.getBatchCounts();
        // Get product stock data
        List<ProductStockDTO> products = productStockDAO.getProductStock(productSearch, status, productBatchCount);

        if (products.isEmpty()) {
            html.append("<p>No stock data found.</p>");
        } else {
            html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
            html.append("<tr><th>Product Name</th><th>Quantity</th><th>Expiry Date</th><th>Batch No</th><th>Purchase Price</th><th>Last Price</th><th>Selling Price</th></tr>");

            int totalQuantity = 0;
            int activeCount = 0;
            int inactiveCount = 0;
            int expiringCount = 0;
            int lowStockCount = 0;
            int expiredCount = 0;

            for (ProductStockDTO product : products) {
                html.append("<tr>");
                html.append("<td>").append(product.getProductName()).append("</td>");
                html.append("<td>").append(product.getQty()).append("</td>");
                html.append("<td>").append(product.getExpiryDate()).append("</td>");
                html.append("<td>").append(product.getBatchNo()).append("</td>");
                html.append("<td>").append(String.format("Rs. %.2f", product.getPurchasePrice())).append("</td>");
                html.append("<td>").append(String.format("Rs. %.2f", product.getLastPrice())).append("</td>");
                html.append("<td>").append(String.format("Rs. %.2f", product.getSellingPrice())).append("</td>");
                html.append("</tr>");

                // Count statistics
                totalQuantity += product.getQty();
                
                if (product.getPStatusId() == 1) {
                    activeCount++;
                    if (product.isExpiringSoon()) expiringCount++;
                    if (product.isLowStock()) lowStockCount++;
                    if (product.isExpired()) expiredCount++;
                } else {
                    inactiveCount++;
                }
            }

            html.append("</table>");
            html.append("<h3>Summary</h3>");
            html.append("<p><strong>Total Products: ").append(products.size()).append("</strong></p>");
            html.append("<p><strong>Total Quantity: ").append(totalQuantity).append(" units</strong></p>");
            html.append("<p><strong>Active Products: ").append(activeCount).append("</strong></p>");
            html.append("<p><strong>Inactive Products: ").append(inactiveCount).append("</strong></p>");
            html.append("<p><strong>Expiring Soon: ").append(expiringCount).append("</strong></p>");
            html.append("<p><strong>Low Stock: ").append(lowStockCount).append("</strong></p>");
            html.append("<p><strong>Expired: ").append(expiredCount).append("</strong></p>");
        }

        html.append("</body></html>");

        // Display HTML in a dialog
        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
        editorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(1000, 600));

        JDialog htmlDialog = new JDialog();
        htmlDialog.setTitle("Stock Report - Simple View");
        htmlDialog.setModal(true);
        htmlDialog.add(scrollPane);
        htmlDialog.pack();
        htmlDialog.setLocationRelativeTo(this);
        htmlDialog.setVisible(true);

        showPositionIndicator("✅ Simple stock report generated");
    }
    
    /**
     * Gets filter info for report
     */
    private String getStockFilterInfo(String searchText, String status) {
        StringBuilder filter = new StringBuilder();
        
        if (!searchText.isEmpty()) {
            filter.append("Search: '").append(searchText).append("' | ");
        }
        
        if (!status.equals("all")) {
            filter.append("Filter: ").append(status);
        } else {
            filter.append("Filter: All Products");
        }
        
        return filter.toString();
    }
    
    /**
     * Exports stock data to Excel
     */
    private void exportStockToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("stock_report.xlsx"));

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
            String productSearch = productSearchBar.getText().trim();
            String status = "all";

            if (expiringRadioBtn.isSelected()) {
                status = "Expiring Soon";
            } else if (lowStockRadioBtn.isSelected()) {
                status = "Low Stock";
            } else if (expiredRadioBtn.isSelected()) {
                status = "Expired";
            } else if (inactiveRadioBtn.isSelected()) {
                status = "Inactive";
            }

            // Get batch counts first
            productBatchCount = productStockDAO.getBatchCounts();
            // Get product stock data
            List<ProductStockDTO> products = productStockDAO.getProductStock(productSearch, status, productBatchCount);

            if (products.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No stock data found for the selected filters.",
                    "No Data", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Stock Report");

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
            String[] headers = {"Product Name", "Quantity", "Expiry Date", "Batch No", "Barcode", "Purchase Price", "Last Price", "Selling Price", "Supplier", "Brand", "Category", "Status"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            int totalQuantity = 0;
            int activeCount = 0;
            int inactiveCount = 0;
            int expiringCount = 0;
            int lowStockCount = 0;
            int expiredCount = 0;

            for (ProductStockDTO product : products) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(product.getProductName());
                row.createCell(1).setCellValue(product.getQty());
                row.createCell(2).setCellValue(product.getExpiryDate());
                row.createCell(3).setCellValue(product.getBatchNo());
                row.createCell(4).setCellValue(product.getBarcode());
                row.createCell(5).setCellValue(product.getPurchasePrice());
                row.createCell(6).setCellValue(product.getLastPrice());
                row.createCell(7).setCellValue(product.getSellingPrice());
                row.createCell(8).setCellValue(product.getSupplierName());
                row.createCell(9).setCellValue(product.getBrandName());
                row.createCell(10).setCellValue(product.getCategoryName());
                row.createCell(11).setCellValue(product.getPStatusId() == 1 ? "Active" : "Inactive");

                // Count statistics
                totalQuantity += product.getQty();
                
                if (product.getPStatusId() == 1) {
                    activeCount++;
                    if (product.isExpiringSoon()) expiringCount++;
                    if (product.isLowStock()) lowStockCount++;
                    if (product.isExpired()) expiredCount++;
                } else {
                    inactiveCount++;
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary rows
            int summaryRowNum = rowNum + 2;

            Row summaryHeader = sheet.createRow(summaryRowNum++);
            Cell summaryCell = summaryHeader.createCell(0);
            summaryCell.setCellValue("STOCK REPORT SUMMARY");
            summaryCell.setCellStyle(headerStyle);

            Row totalProductsRow = sheet.createRow(summaryRowNum++);
            totalProductsRow.createCell(0).setCellValue("Total Products:");
            totalProductsRow.createCell(1).setCellValue(products.size());

            Row totalQuantityRow = sheet.createRow(summaryRowNum++);
            totalQuantityRow.createCell(0).setCellValue("Total Quantity:");
            totalQuantityRow.createCell(1).setCellValue(totalQuantity + " units");

            Row activeProductsRow = sheet.createRow(summaryRowNum++);
            activeProductsRow.createCell(0).setCellValue("Active Products:");
            activeProductsRow.createCell(1).setCellValue(activeCount);

            Row inactiveProductsRow = sheet.createRow(summaryRowNum++);
            inactiveProductsRow.createCell(0).setCellValue("Inactive Products:");
            inactiveProductsRow.createCell(1).setCellValue(inactiveCount);

            Row expiringProductsRow = sheet.createRow(summaryRowNum++);
            expiringProductsRow.createCell(0).setCellValue("Expiring Soon:");
            expiringProductsRow.createCell(1).setCellValue(expiringCount);

            Row lowStockProductsRow = sheet.createRow(summaryRowNum++);
            lowStockProductsRow.createCell(0).setCellValue("Low Stock:");
            lowStockProductsRow.createCell(1).setCellValue(lowStockCount);

            Row expiredProductsRow = sheet.createRow(summaryRowNum++);
            expiredProductsRow.createCell(0).setCellValue("Expired:");
            expiredProductsRow.createCell(1).setCellValue(expiredCount);

            Row generatedDateRow = sheet.createRow(summaryRowNum++);
            generatedDateRow.createCell(0).setCellValue("Generated Date:");
            generatedDateRow.createCell(1).setCellValue(REPORT_DATE_FORMAT.format(new Date()));

            Row filterInfoRow = sheet.createRow(summaryRowNum++);
            filterInfoRow.createCell(0).setCellValue("Filter Info:");
            filterInfoRow.createCell(1).setCellValue(getStockFilterInfo(productSearch, status));

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
        productSearchBar = new javax.swing.JTextField();
        expiringRadioBtn = new javax.swing.JRadioButton();
        lowStockRadioBtn = new javax.swing.JRadioButton();
        expiredRadioBtn = new javax.swing.JRadioButton();
        addStockBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        roundedPanel2 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();
        deleteBtn = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
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
        jPanel12 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        roundedPanel3 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        roundedPanel4 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        inactiveRadioBtn = new javax.swing.JRadioButton();
        stockReportBtn = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));

        productSearchBar.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        productSearchBar.setText("Search By Product Name Or Barcode");

        buttonGroup1.add(expiringRadioBtn);
        expiringRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        expiringRadioBtn.setText("Expiring Soon Products");

        buttonGroup1.add(lowStockRadioBtn);
        lowStockRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        lowStockRadioBtn.setForeground(new java.awt.Color(255, 153, 0));
        lowStockRadioBtn.setText("Low Stock Products");

        buttonGroup1.add(expiredRadioBtn);
        expiredRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        expiredRadioBtn.setForeground(new java.awt.Color(255, 51, 51));
        expiredRadioBtn.setText("Expired");
        expiredRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expiredRadioBtnActionPerformed(evt);
            }
        });

        addStockBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addStockBtn.setText("Add Stock");
        addStockBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStockBtnActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel19.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel19.setText("Product Name :");

        jLabel4.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 102, 102));
        jLabel4.setText("Supplier : Wellness Co");

        jButton7.setBackground(new java.awt.Color(255, 255, 204));
        jButton7.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton7.setText("Expiring Soon");

        jButton8.setBackground(new java.awt.Color(255, 51, 51));
        jButton8.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        jButton8.setText("Low Stock");

        editBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        deleteBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new java.awt.GridLayout(3, 0));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel15.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel15.setText("Brand");

        jLabel20.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 102, 102));
        jLabel20.setText("Nature's Best");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel9);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel21.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel21.setText("Category");

        jLabel26.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(102, 102, 102));
        jLabel26.setText("Beverages");

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
                .addComponent(jLabel26)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel8);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        jLabel27.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel27.setText("Quantity");

        jLabel28.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(102, 102, 102));
        jLabel28.setText("10");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jLabel30)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel11);

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));

        jLabel31.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel31.setText("Batch No");

        jLabel32.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(102, 102, 102));
        jLabel32.setText("BCH01");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel12);

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));

        jLabel34.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(102, 102, 102));
        jLabel34.setText("4563258745");

        jLabel33.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jLabel33.setText("Barcode");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(45, 45, 45))
                    .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(343, 343, 343))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel34)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel13);

        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));

        roundedPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel36.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel36.setText("Purchase Price");

        jLabel35.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(102, 102, 102));
        jLabel35.setText("Rs.40000");

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel35)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel37.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel37.setText("Last Price");

        jLabel38.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(102, 102, 102));
        jLabel38.setText("Rs.40000");

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel37)
                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
        jLabel39.setText("Selling Price");

        jLabel40.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(102, 102, 102));
        jLabel40.setText("Rs.40000");

        javax.swing.GroupLayout roundedPanel4Layout = new javax.swing.GroupLayout(roundedPanel4);
        roundedPanel4.setLayout(roundedPanel4Layout);
        roundedPanel4Layout.setHorizontalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundedPanel4Layout.setVerticalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel40)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(roundedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSeparator3)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8))
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        roundedPanel2Layout.setVerticalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(deleteBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jButton7)
                    .addComponent(jButton8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(roundedPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundedPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(13, 13, 13))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(652, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(2028, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        buttonGroup1.add(inactiveRadioBtn);
        inactiveRadioBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        inactiveRadioBtn.setForeground(new java.awt.Color(255, 51, 51));
        inactiveRadioBtn.setText("Inactivie");
        inactiveRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inactiveRadioBtnActionPerformed(evt);
            }
        });

        stockReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        stockReportBtn.setText("Stock Report");
        stockReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stockReportBtnActionPerformed(evt);
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
                        .addComponent(productSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(expiringRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lowStockRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(expiredRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inactiveRadioBtn)
                        .addGap(198, 198, 198)
                        .addComponent(stockReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addStockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expiringRadioBtn)
                    .addComponent(lowStockRadioBtn)
                    .addComponent(expiredRadioBtn)
                    .addComponent(addStockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inactiveRadioBtn)
                    .addComponent(stockReportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addGap(18, 18, 18))
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

    private void expiredRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expiredRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_expiredRadioBtnActionPerformed

    private void addStockBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStockBtnActionPerformed
        AddNewStock addNewStock = new AddNewStock(null, true);
        addNewStock.setLocationRelativeTo(null);
        addNewStock.setVisible(true);
        SearchFilters();
    }//GEN-LAST:event_addStockBtnActionPerformed

    private void inactiveRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inactiveRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inactiveRadioBtnActionPerformed

    private void stockReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stockReportBtnActionPerformed
       showExportOptions();
    }//GEN-LAST:event_stockReportBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addStockBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JRadioButton expiredRadioBtn;
    private javax.swing.JRadioButton expiringRadioBtn;
    private javax.swing.JRadioButton inactiveRadioBtn;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
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
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JRadioButton lowStockRadioBtn;
    private javax.swing.JTextField productSearchBar;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel2;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel3;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel4;
    private javax.swing.JButton stockReportBtn;
    // End of variables declaration//GEN-END:variables
}
