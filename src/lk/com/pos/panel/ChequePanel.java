package lk.com.pos.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.connection.MySQL;
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
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

/**
 * ChequePanel - Displays and manages cheque information
 * Features: Search, filters, keyboard navigation, status management
 */
public class ChequePanel extends javax.swing.JPanel {
   
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    
    private static final class Colors {
        static final Color TEAL_PRIMARY = new Color(28, 181, 187);
        static final Color TEAL_HOVER = new Color(60, 200, 206);
        static final Color BORDER_DEFAULT = new Color(230, 230, 230);
        static final Color BACKGROUND = Color.decode("#F8FAFC");
        static final Color CARD_WHITE = Color.WHITE;
        static final Color TEXT_PRIMARY = Color.decode("#1E293B");
        static final Color TEXT_SECONDARY = Color.decode("#6B7280");
        static final Color TEXT_MUTED = Color.decode("#94A3B8");
        
        static final Color BADGE_BOUNCED_BG = Color.decode("#FED7AA");
        static final Color BADGE_BOUNCED_FG = Color.decode("#7C2D12");
        static final Color BADGE_BOUNCED_BORDER = Color.decode("#FB923C");
        
        static final Color BADGE_CLEARED_BG = Color.decode("#D1FAE5");
        static final Color BADGE_CLEARED_FG = Color.decode("#059669");
        
        static final Color BADGE_PENDING_BG = Color.decode("#FEF3C7");
        static final Color BADGE_PENDING_FG = Color.decode("#92400E");
        
        static final Color DETAIL_PHONE = Color.decode("#8B5CF6");
        static final Color DETAIL_NIC = Color.decode("#EC4899");
        static final Color DETAIL_BANK = Color.decode("#10B981");
        static final Color DETAIL_DATE = Color.decode("#06B6D4");
        static final Color DETAIL_STATUS = Color.decode("#6366F1");
        
        static final Color BTN_EDIT_BG = Color.decode("#EFF6FF");
        static final Color BTN_EDIT_BORDER = Color.decode("#BFDBFE");
        static final Color BTN_STATUS_BG = Color.decode("#F3E8FF");
        static final Color BTN_STATUS_BORDER = Color.decode("#C4B5FD");
    }
    
    private static final class Dimensions {
        static final Dimension CARD_SIZE = new Dimension(420, 500);
        static final Dimension CARD_MAX_SIZE = new Dimension(420, 500);
        static final Dimension CARD_MIN_SIZE = new Dimension(380, 500);
        static final Dimension ACTION_BUTTON_SIZE = new Dimension(30, 30);
        static final int CARD_WIDTH_WITH_GAP = 445;
        static final int GRID_GAP = 25;
        static final int CARD_PADDING = 16;
    }
    
    private static final class Fonts {
        static final java.awt.Font HEADER = new java.awt.Font("Nunito ExtraBold", 1, 20);
        static final java.awt.Font SECTION_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font BADGE = new java.awt.Font("Nunito ExtraBold", 1, 11);
        static final java.awt.Font DETAIL_TITLE = new java.awt.Font("Nunito SemiBold", 0, 13);
        static final java.awt.Font DETAIL_VALUE = new java.awt.Font("Nunito SemiBold", 1, 14);
        static final java.awt.Font AMOUNT_TITLE = new java.awt.Font("Nunito ExtraBold", 1, 12);
        static final java.awt.Font AMOUNT_VALUE = new java.awt.Font("Nunito ExtraBold", 1, 18);
        static final java.awt.Font LOADING = new java.awt.Font("Nunito ExtraBold", 1, 20);
    }
    
    private static final class Strings {
        static final String SEARCH_PLACEHOLDER = "Search By Cheque No, Customer Name or Invoice No";
        static final String NO_CHEQUES = "No cheques found";
        static final String LOADING_MESSAGE = "Loading cheques...";
        static final String SECTION_DETAILS = "CHEQUE DETAILS";
        static final String SECTION_AMOUNT = "CHEQUE AMOUNT";
    }

    private List<lk.com.pos.privateclasses.RoundedPanel> chequeCardsList = new ArrayList<>();
    private lk.com.pos.privateclasses.RoundedPanel currentFocusedCard = null;
    private int currentCardIndex = -1;
    private int currentColumns = 3;
    
    private JPanel loadingPanel;
    private JLabel loadingLabel;
    
    private long lastRefreshTime = 0;
    private static final long REFRESH_COOLDOWN_MS = 1000;

    public ChequePanel() {
        initComponents();
        initializeUI();
        createLoadingPanel();
        setupKeyboardShortcuts();
        loadCheques();
    }

    private void initializeUI() {
        setupScrollPane();
        setupIcons();
        setupSearchField();
        setupRadioButtons();
        setupButtons();
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
        
        jTextField1.setToolTipText("Search cheques");
        
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                performSearch();
            }
        });
    }
    
    private void setupButtons() {
        customerReportBtn.setToolTipText("Generate Cheque Report");
        addNewCoustomerBtn.setText("Add New Cheque");
        addNewCoustomerBtn.setToolTipText("Add New Cheque");
    }
    
    private void setupRadioButtons() {
        jRadioButton1.putClientProperty(FlatClientProperties.STYLE, "foreground:#EF4444;");
        jRadioButton2.putClientProperty(FlatClientProperties.STYLE, "foreground:#10B981;");
        jRadioButton4.putClientProperty(FlatClientProperties.STYLE, "foreground:#F97316;");
        
        jRadioButton4.setToolTipText("Filter pending cheques");
        jRadioButton1.setToolTipText("Filter bounced cheques");
        jRadioButton2.setToolTipText("Filter cleared cheques");
        
        jRadioButton4.setSelected(true);
        
        jRadioButton1.addActionListener(evt -> onFilterChanged());
        jRadioButton2.addActionListener(evt -> onFilterChanged());
        jRadioButton4.addActionListener(evt -> onFilterChanged());
        
        setupRadioButtonToggle(jRadioButton1);
        setupRadioButtonToggle(jRadioButton2);
        setupRadioButtonToggle(jRadioButton4);
    }
    
    private void setupRadioButtonToggle(javax.swing.JRadioButton radioBtn) {
        radioBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (radioBtn.isSelected()) {
                    ButtonGroup bg = ((ButtonGroup) radioBtn.getClientProperty("buttonGroup"));
                    if (bg != null) bg.clearSelection();
                    performSearch();
                    evt.consume();
                }
            }
        });
    }
    
    private void setupPanel() {
        jPanel2.setBackground(Colors.BACKGROUND);
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
        
        JLabel subLabel = new JLabel("Please wait");
        subLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 11));
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

    private void setupKeyboardShortcuts() {
        this.setFocusable(true);
        // Add keyboard shortcuts similar to CustomerPanel if needed
    }

    private static class ChequeCardData {
        int chequeId;
        String chequeNo, customerName, invoiceNo, bankName, branch;
        String phone, nic, address;
        String givenDate, chequeDate;
        String chequeType;
        double chequeAmount;
        int salesId;
    }
    
    private void loadCheques() {
        String searchText = getSearchText();
        boolean bouncedOnly = jRadioButton1.isSelected();
        boolean clearedOnly = jRadioButton2.isSelected();
        boolean pendingOnly = jRadioButton4.isSelected();
        
        loadChequesAsync(searchText, bouncedOnly, clearedOnly, pendingOnly);
    }
    
    private String getSearchText() {
        String text = jTextField1.getText().trim();
        return text.equals(Strings.SEARCH_PLACEHOLDER) ? "" : text;
    }
    
    private void performSearch() {
        loadCheques();
    }
    
    private void loadChequesAsync(String searchText, boolean bouncedOnly, 
                                  boolean clearedOnly, boolean pendingOnly) {
        showLoading(true);
        
        SwingWorker<List<ChequeCardData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ChequeCardData> doInBackground() throws Exception {
                return fetchChequesFromDatabase(searchText, bouncedOnly, clearedOnly, pendingOnly);
            }
            
            @Override
            protected void done() {
                try {
                    List<ChequeCardData> cheques = get();
                    displayCheques(cheques);
                } catch (Exception e) {
                    handleLoadError(e);
                } finally {
                    showLoading(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private List<ChequeCardData> fetchChequesFromDatabase(String searchText, boolean bouncedOnly, 
                                                          boolean clearedOnly, boolean pendingOnly) throws Exception {
        List<ChequeCardData> cheques = new ArrayList<>();
        
        try {
            String query = buildChequeQuery(searchText, bouncedOnly, clearedOnly, pendingOnly);
            ResultSet rs = MySQL.executeSearch(query);

            while (rs.next()) {
                ChequeCardData data = createChequeDataFromResultSet(rs);
                cheques.add(data);
            }
            
        } catch (SQLException e) {
            throw new Exception("Database error while fetching cheques: " + e.getMessage(), e);
        }
        
        return cheques;
    }
    
    private ChequeCardData createChequeDataFromResultSet(ResultSet rs) throws SQLException {
        ChequeCardData data = new ChequeCardData();
        
        data.chequeId = rs.getInt("cheque_id");
        data.chequeNo = rs.getString("cheque_no");
        data.customerName = rs.getString("customer_name");
        data.invoiceNo = rs.getString("invoice_no");
        data.bankName = rs.getString("bank_name");
        data.branch = rs.getString("branch");
        data.phone = rs.getString("customer_phone_no");
        data.nic = rs.getString("nic");
        data.address = rs.getString("customer_address");
        data.givenDate = rs.getString("given_date");
        data.chequeDate = rs.getString("cheque_date");
        data.chequeType = rs.getString("cheque_type");
        data.chequeAmount = rs.getDouble("cheque_amount");
        data.salesId = rs.getInt("sales_id");
        
        return data;
    }
    
    private String buildChequeQuery(String searchText, boolean bouncedOnly, 
                                   boolean clearedOnly, boolean pendingOnly) {
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT ch.cheque_id, ch.cheque_no, ch.cheque_date, ");
        query.append("ch.bank_name, ch.branch, ch.cheque_amount, ch.sales_id, ");
        query.append("cc.customer_name, cc.customer_phone_no, cc.nic, cc.customer_address, ");
        query.append("s.invoice_no, ct.cheque_type, ");
        query.append("DATE(ch.date_time) as given_date ");
        query.append("FROM cheque ch ");
        query.append("LEFT JOIN credit_customer cc ON ch.credit_customer_id = cc.customer_id ");
        query.append("LEFT JOIN sales s ON ch.sales_id = s.sales_id ");
        query.append("LEFT JOIN cheque_type ct ON ch.cheque_type_id = ct.cheque_type_id ");
        query.append("WHERE 1=1 ");
        
        if (isValidSearchText(searchText)) {
            String escapedSearch = escapeSQL(searchText);
            query.append("AND (ch.cheque_no LIKE '%").append(escapedSearch).append("%' ");
            query.append("OR cc.customer_name LIKE '%").append(escapedSearch).append("%' ");
            query.append("OR s.invoice_no LIKE '%").append(escapedSearch).append("%') ");
        }
        
        query.append(buildStatusFilter(bouncedOnly, clearedOnly, pendingOnly));
        query.append("ORDER BY ch.cheque_id DESC");
        
        return query.toString();
    }
    
    private boolean isValidSearchText(String searchText) {
        return searchText != null && 
               !searchText.isEmpty() && 
               !searchText.equals(Strings.SEARCH_PLACEHOLDER);
    }
    
    private String escapeSQL(String input) {
        if (input == null) return "";
        
        return input.replace("\\", "\\\\")
                   .replace("'", "''")
                   .replace("%", "\\%")
                   .replace("_", "\\_");
    }
    
    private String buildStatusFilter(boolean bouncedOnly, boolean clearedOnly, boolean pendingOnly) {
        if (bouncedOnly) {
            return "AND ct.cheque_type = 'Bounced' ";
        } else if (clearedOnly) {
            return "AND ct.cheque_type = 'Cleared' ";
        } else if (pendingOnly) {
            return "AND ct.cheque_type = 'Pending' ";
        }
        return "";
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

    private void displayCheques(List<ChequeCardData> cheques) {
        clearChequeCards();
        
        currentCardIndex = -1;
        currentFocusedCard = null;

        if (cheques.isEmpty()) {
            showEmptyState();
            return;
        }

        currentColumns = calculateColumns(jPanel2.getWidth());
        final JPanel gridPanel = createGridPanel();
        
        for (ChequeCardData data : cheques) {
            lk.com.pos.privateclasses.RoundedPanel card = createChequeCard(data);
            gridPanel.add(card);
            chequeCardsList.add(card);
        }

        layoutCardsInPanel(gridPanel);
        setupGridResizeListener(gridPanel);
        
        jPanel2.revalidate();
        jPanel2.repaint();
    }
    
    private void clearChequeCards() {
        chequeCardsList.clear();
        jPanel2.removeAll();
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
    }
    
    private lk.com.pos.privateclasses.RoundedPanel createChequeCard(ChequeCardData data) {
        String displayGivenDate = formatDate(data.givenDate);
        String displayChequeDate = formatDate(data.chequeDate);

        lk.com.pos.privateclasses.RoundedPanel card = createBaseCard(data.chequeId, data.chequeNo, data.salesId);
        JPanel contentPanel = createCardContent(data, displayGivenDate, displayChequeDate);
        
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }
    
    private String formatDate(String date) {
        if (date == null) return "N/A";
        
        try {
            Date d = DATE_FORMAT.parse(date);
            return DISPLAY_DATE_FORMAT.format(d);
        } catch (Exception e) {
            return date;
        }
    }
    
    private lk.com.pos.privateclasses.RoundedPanel createBaseCard(int chequeId, String chequeNo, int salesId) {
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
        });
    }
    
    private JPanel createCardContent(ChequeCardData data, String displayGivenDate, String displayChequeDate) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CARD_WHITE);
        contentPanel.setOpaque(false);

        contentPanel.add(createHeaderSection(data.chequeId, data.customerName, data.salesId));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createStatusBadgeSection(data.chequeType));
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(createDetailsSectionHeader());
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(createDetailsGrid1(data.chequeNo, data.invoiceNo, displayGivenDate, displayChequeDate));
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(createDetailsGrid2(data.bankName, data.branch));
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(createDetailsGrid3(data.phone, data.nic));
        contentPanel.add(Box.createVerticalStrut(15));
        
        if (data.address != null && !data.address.trim().isEmpty()) {
            contentPanel.add(createAddressSection(data.address));
            contentPanel.add(Box.createVerticalStrut(15));
        }
        
        contentPanel.add(createAmountSectionHeader());
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(createAmountPanel(data.chequeAmount));

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

        headerPanel.add(createActionButtons(chequeId, salesId), BorderLayout.EAST);

        return headerPanel;
    }
    
    private JPanel createActionButtons(int chequeId, int salesId) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        JButton statusButton = createStatusButton(chequeId);
        JButton editButton = createEditButton(chequeId, salesId);
        
        actionPanel.add(statusButton);
        actionPanel.add(editButton);

        return actionPanel;
    }
    
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
        statusButton.setToolTipText("Change Status");
        
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
        editButton.setToolTipText("Edit Cheque");
        editButton.addActionListener(e -> {
            editCheque(chequeId, salesId);
            ChequePanel.this.requestFocusInWindow();
        });

        return editButton;
    }
    
    private JPanel createStatusBadgeSection(String chequeType) {
        JPanel statusBadgePanel = new JPanel(new BorderLayout(10, 0));
        statusBadgePanel.setOpaque(false);
        statusBadgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JPanel badgePanel = createBadgePanel(chequeType);
        statusBadgePanel.add(badgePanel, BorderLayout.WEST);

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
                    BorderFactory.createLineBorder(Colors.BADGE_BOUNCED_BORDER, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        } else if ("Cleared".equalsIgnoreCase(chequeType)) {
            badge.setForeground(Colors.BADGE_CLEARED_FG);
            badge.setBackground(Colors.BADGE_CLEARED_BG);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BADGE_CLEARED_FG, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        } else if ("Pending".equalsIgnoreCase(chequeType)) {
            badge.setForeground(Colors.BADGE_PENDING_FG);
            badge.setBackground(Colors.BADGE_PENDING_BG);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.BADGE_PENDING_FG, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        }
        
        return badge;
    }
    
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
    
    private JPanel createDetailsGrid1(String chequeNo, String invoiceNo, String givenDate, String chequeDate) {
        JPanel detailsGrid = new JPanel(new GridLayout(2, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        detailsGrid.add(createDetailPanel("Cheque No", chequeNo, Colors.DETAIL_STATUS));
        detailsGrid.add(createDetailPanel("Invoice No", invoiceNo != null ? invoiceNo : "N/A", Colors.DETAIL_NIC));
        detailsGrid.add(createDetailPanel("Given Date", givenDate, Colors.DETAIL_DATE));
        detailsGrid.add(createDetailPanel("Cheque Date", chequeDate, Colors.DETAIL_BANK));

        return detailsGrid;
    }
    
    private JPanel createDetailsGrid2(String bankName, String branch) {
        JPanel detailsGrid = new JPanel(new GridLayout(1, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        detailsGrid.add(createDetailPanel("Bank Name", bankName != null ? bankName : "N/A", Colors.DETAIL_BANK));
        detailsGrid.add(createDetailPanel("Branch", branch != null ? branch : "N/A", Colors.DETAIL_DATE));

        return detailsGrid;
    }
    
    private JPanel createDetailsGrid3(String phone, String nic) {
        JPanel detailsGrid = new JPanel(new GridLayout(1, 2, 20, 15));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

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
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);

        return panel;
    }
    
    private JPanel createAddressSection(String address) {
        JPanel addressPanel = new JPanel(new BorderLayout());
        addressPanel.setOpaque(false);
        addressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel addressTitle = new JLabel("Address");
        addressTitle.setFont(new java.awt.Font("Nunito SemiBold", 1, 13));
        addressTitle.setForeground(Colors.DETAIL_STATUS);
        
        JLabel addressLabel = new JLabel("<html><div style='width:360px;'>" + address + "</div></html>");
        addressLabel.setFont(Fonts.DETAIL_VALUE);
        addressLabel.setForeground(Colors.TEXT_PRIMARY);
        addressLabel.setToolTipText(address);
        
        addressPanel.add(addressTitle, BorderLayout.NORTH);
        addressPanel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
        addressPanel.add(addressLabel, BorderLayout.CENTER);
        
        return addressPanel;
    }
    
    private JPanel createAmountSectionHeader() {
        JPanel amountHeaderPanel = new JPanel(new BorderLayout());
        amountHeaderPanel.setOpaque(false);
        amountHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel amountHeader = new JLabel(Strings.SECTION_AMOUNT);
        amountHeader.setFont(Fonts.SECTION_TITLE);
        amountHeader.setForeground(Colors.TEXT_MUTED);
        amountHeaderPanel.add(amountHeader, BorderLayout.WEST);

        return amountHeaderPanel;
    }
    
    private JPanel createAmountPanel(double amount) {
        lk.com.pos.privateclasses.RoundedPanel panel = new lk.com.pos.privateclasses.RoundedPanel();
        panel.setBackgroundColor(Color.decode("#DBEAFE"));
        panel.setBorderThickness(0);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Cheque Amount");
        titleLabel.setFont(Fonts.AMOUNT_TITLE);
        titleLabel.setForeground(Color.decode("#1E40AF"));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 0, 0);

        JLabel amountLabel = new JLabel(formatPrice(amount));
        amountLabel.setFont(Fonts.AMOUNT_VALUE);
        amountLabel.setForeground(Color.decode("#1E40AF"));
        amountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(amountLabel, gbc);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
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

    private void changeStatus(int chequeId) {
        try {
            // Get current status
            String query = "SELECT ct.cheque_type, ct.cheque_type_id FROM cheque ch " +
                          "INNER JOIN cheque_type ct ON ch.cheque_type_id = ct.cheque_type_id " +
                          "WHERE ch.cheque_id = " + chequeId;
            ResultSet rs = MySQL.executeSearch(query);
            
            String currentStatus = "Unknown";
            int currentTypeId = 0;
            
            if (rs.next()) {
                currentStatus = rs.getString("cheque_type");
                currentTypeId = rs.getInt("cheque_type_id");
            }
            
            // Show status change dialog
            String[] options = {"Pending", "Cleared", "Bounced"};
            JComboBox<String> statusCombo = new JComboBox<>(options);
            statusCombo.setSelectedItem(currentStatus);
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(new JLabel("Current Status: " + currentStatus), BorderLayout.NORTH);
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
                
                if (!newStatus.equals(currentStatus)) {
                    // Get new type ID
                    String typeQuery = "SELECT cheque_type_id FROM cheque_type WHERE cheque_type = '" + newStatus + "'";
                    ResultSet typeRs = MySQL.executeSearch(typeQuery);
                    
                    if (typeRs.next()) {
                        int newTypeId = typeRs.getInt("cheque_type_id");
                        
                        // Update status
                        String updateQuery = "UPDATE cheque SET cheque_type_id = " + newTypeId + 
                                           " WHERE cheque_id = " + chequeId;
                        MySQL.executeIUD(updateQuery);
                        
                        JOptionPane.showMessageDialog(
                            this,
                            "Cheque status updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        performSearch(); // Refresh the list
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
    
    private void editCheque(int chequeId, int salesId) {
        JOptionPane.showMessageDialog(
            this,
            "Edit Cheque Dialog\nCheque ID: " + chequeId + "\nSales ID: " + salesId,
            "Edit Cheque",
            JOptionPane.INFORMATION_MESSAGE
        );
        // TODO: Implement edit cheque dialog similar to UpdateCustomer
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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

        jRadioButton1.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton1.setForeground(new java.awt.Color(255, 51, 51));
        jRadioButton1.setText("Bounced");

        addNewCoustomerBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewCoustomerBtn.setText("Add New Customer");
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

        jRadioButton2.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton2.setForeground(new java.awt.Color(99, 102, 241));
        jRadioButton2.setText("Cleared");

        jRadioButton4.setFont(new java.awt.Font("Nunito SemiBold", 0, 16)); // NOI18N
        jRadioButton4.setForeground(new java.awt.Color(255, 153, 0));
        jRadioButton4.setText("Pending");

        customerReportBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        customerReportBtn.setText("Cheque Report");
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
                        .addGap(79, 79, 79)
                        .addComponent(customerReportBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewCoustomerBtn)))
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 615, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void addNewCoustomerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCoustomerBtnActionPerformed
        
    }//GEN-LAST:event_addNewCoustomerBtnActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void customerReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerReportBtnActionPerformed
      
    }//GEN-LAST:event_customerReportBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewBound;
    private javax.swing.JButton addNewCoustomerBtn;
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
