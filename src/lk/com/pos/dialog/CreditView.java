package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import lk.com.pos.dialogpanel.CraditPay;
import raven.toast.Notifications;

public class CreditView extends javax.swing.JDialog {

    private int customerId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private List<CreditPanelInfo> creditPanels = new ArrayList<>();
    private int selectedCreditIndex = -1;
    private int selectedPaymentIndex = -1;
    private static final Color SELECTED_BORDER_COLOR = Color.decode("#0893B0");
    private static final Color DEFAULT_BORDER_COLOR = new Color(200, 200, 200);
    private static final Color ICON_DEFAULT_COLOR = Color.decode("#999999");
    private static final Color ICON_HOVER_COLOR = Color.decode("#0893B0");

    private class CreditPanelInfo {

        JPanel panel;
        JButton editButton;
        JButton dropButton;
        JButton addButton;
        int creditId;
        boolean isExpanded = false;
        boolean hasPayments = false;
        List<CraditPay> paymentPanels = new ArrayList<>();

        public CreditPanelInfo(JPanel panel, JButton editButton, JButton dropButton, JButton addButton,
                int creditId, boolean hasPayments) {
            this.panel = panel;
            this.editButton = editButton;
            this.dropButton = dropButton;
            this.addButton = addButton;
            this.creditId = creditId;
            this.hasPayments = hasPayments;
        }
    }

    public CreditView(java.awt.Frame parent, boolean modal, int customerId) {
        super(parent, modal);
        this.customerId = customerId;
        initComponents();
        initializeDialog();
        loadCustomerCredits();
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setTitle("Credit Details View");

        jPanel1.remove(creditPanel);
        jPanel1.revalidate();
        jPanel1.repaint();

        setupKeyboardShortcuts();
        setupFocusTraversal();
        setupToolTips();
    }

    private void setupToolTips() {
        jScrollPane1.setToolTipText("Use UP/DOWN arrows to navigate, ENTER to expand/collapse, F1 to edit, F2 to add payment, F3 to toggle expand, F5 to refresh, ESC to close");
    }

    private void setupFocusTraversal() {
        jScrollPane1.setFocusable(true);
        jScrollPane1.requestFocusInWindow();

        jScrollPane1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleKeyPress(evt);
            }
        });

        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleKeyPress(evt);
            }
        });

        jPanel1.setFocusable(true);
    }

    private void handleKeyPress(java.awt.event.KeyEvent evt) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                navigateDown();
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                navigateUp();
                evt.consume();
                break;
            case KeyEvent.VK_ENTER:
                handleEnterKey();
                evt.consume();
                break;
            case KeyEvent.VK_F1:
                handleF1Key();
                evt.consume();
                break;
            case KeyEvent.VK_F2:
                handleF2Key();
                evt.consume();
                break;
            case KeyEvent.VK_F3:
                handleF3Key();
                evt.consume();
                break;
            case KeyEvent.VK_F5:
                refreshCredits();
                evt.consume();
                break;
            case KeyEvent.VK_ESCAPE:
                dispose();
                evt.consume();
                break;
        }
    }

    private void setupKeyboardShortcuts() {
        getRootPane().registerKeyboardAction(
                evt -> navigateDown(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> navigateUp(),
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> handleEnterKey(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> handleF1Key(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> handleF2Key(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> handleF3Key(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> refreshCredits(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void navigateDown() {
        if (creditPanels.isEmpty()) {
            return;
        }

        if (selectedCreditIndex == -1) {
            selectCreditPanel(0);
            return;
        }

        CreditPanelInfo currentCredit = creditPanels.get(selectedCreditIndex);

        if (currentCredit.isExpanded && currentCredit.hasPayments && !currentCredit.paymentPanels.isEmpty()) {
            if (selectedPaymentIndex == -1) {
                selectPaymentPanel(0);
            } else if (selectedPaymentIndex < currentCredit.paymentPanels.size() - 1) {
                selectPaymentPanel(selectedPaymentIndex + 1);
            } else if (selectedCreditIndex < creditPanels.size() - 1) {
                selectCreditPanel(selectedCreditIndex + 1);
            }
        } else if (selectedCreditIndex < creditPanels.size() - 1) {
            selectCreditPanel(selectedCreditIndex + 1);
        }
    }

    private void navigateUp() {
        if (creditPanels.isEmpty()) {
            return;
        }

        if (selectedPaymentIndex != -1) {
            if (selectedPaymentIndex > 0) {
                selectPaymentPanel(selectedPaymentIndex - 1);
            } else {
                deselectPaymentPanel();
                updateCreditPanelBorder(selectedCreditIndex, true);
                updateButtonIconsForSelectedCredit();
            }
        } else if (selectedCreditIndex > 0) {
            CreditPanelInfo prevCredit = creditPanels.get(selectedCreditIndex - 1);
            selectCreditPanel(selectedCreditIndex - 1);

            if (prevCredit.isExpanded && prevCredit.hasPayments && !prevCredit.paymentPanels.isEmpty()) {
                selectPaymentPanel(prevCredit.paymentPanels.size() - 1);
            }
        }
    }

    private void handleEnterKey() {
        if (selectedCreditIndex == -1) {
            return;
        }

        CreditPanelInfo selectedCredit = creditPanels.get(selectedCreditIndex);

        if (selectedPaymentIndex != -1) {
            return;
        }

        if (selectedCredit.hasPayments) {
            selectedCredit.dropButton.doClick();
        }
    }

    private void handleF1Key() {
        // F1 - Edit: Credit when credit selected, Payment when payment selected
        if (selectedCreditIndex == -1) {
            return;
        }

        if (selectedPaymentIndex != -1) {
            // Edit Payment
            CreditPanelInfo selectedCredit = creditPanels.get(selectedCreditIndex);
            if (selectedPaymentIndex < selectedCredit.paymentPanels.size()) {
                CraditPay selectedPayment = selectedCredit.paymentPanels.get(selectedPaymentIndex);
                selectedPayment.triggerEdit();
            }
        } else {
            // Edit Credit
            CreditPanelInfo selectedCredit = creditPanels.get(selectedCreditIndex);
            editCredit(selectedCredit.creditId);
        }
    }

    private void handleF2Key() {
        // F2 - Add Payment (only works when credit is selected, not payment)
        if (selectedCreditIndex == -1) {
            return;
        }

        if (selectedPaymentIndex != -1) {
            return;
        }

        CreditPanelInfo selectedCredit = creditPanels.get(selectedCreditIndex);
        addPayment(selectedCredit.creditId);
    }

    private void handleF3Key() {
        // F3 - Toggle Expand/Collapse (only when credit is selected)
        if (selectedCreditIndex == -1) {
            return;
        }

        if (selectedPaymentIndex != -1) {
            return;
        }

        CreditPanelInfo selectedCredit = creditPanels.get(selectedCreditIndex);
        if (selectedCredit.hasPayments) {
            selectedCredit.dropButton.doClick();
        }
    }

    private void selectCreditPanel(int index) {
        if (index < 0 || index >= creditPanels.size()) {
            return;
        }

        if (selectedCreditIndex != -1 && selectedCreditIndex != index) {
            updateCreditPanelBorder(selectedCreditIndex, false);
            resetButtonIconsForCredit(selectedCreditIndex);
        }

        deselectPaymentPanel();

        selectedCreditIndex = index;
        updateCreditPanelBorder(index, true);

        scrollToPanel(creditPanels.get(index).panel);

        updateButtonIconsForSelectedCredit();

        // Update tooltip based on selection
        updateSelectionToolTip();
    }

    private void selectPaymentPanel(int index) {
        if (selectedCreditIndex == -1) {
            return;
        }

        CreditPanelInfo currentCredit = creditPanels.get(selectedCreditIndex);
        if (index < 0 || index >= currentCredit.paymentPanels.size()) {
            return;
        }

        if (selectedPaymentIndex != -1 && selectedPaymentIndex < currentCredit.paymentPanels.size()) {
            currentCredit.paymentPanels.get(selectedPaymentIndex).setBorderColor(DEFAULT_BORDER_COLOR);
        }

        updateCreditPanelBorder(selectedCreditIndex, false);
        resetButtonIconsForCredit(selectedCreditIndex);

        selectedPaymentIndex = index;
        currentCredit.paymentPanels.get(index).setBorderColor(SELECTED_BORDER_COLOR);

        scrollToPanel(currentCredit.paymentPanels.get(index));

        // Update tooltip based on selection
        updateSelectionToolTip();
    }

    private void updateSelectionToolTip() {
        String tooltip = "Navigation: UP/DOWN arrows | ";

        if (selectedPaymentIndex != -1) {
            tooltip += "F1: Edit Payment";
        } else if (selectedCreditIndex != -1) {
            CreditPanelInfo credit = creditPanels.get(selectedCreditIndex);
            tooltip += "F1: Edit Credit | F2: Add Payment";
            if (credit.hasPayments) {
                tooltip += " | F3: " + (credit.isExpanded ? "Collapse" : "Expand");
            }
        } else {
            tooltip += "Select an item to see available actions";
        }

        tooltip += " | F5: Refresh | ESC: Close";

        jScrollPane1.setToolTipText(tooltip);
    }

    private void deselectPaymentPanel() {
        if (selectedCreditIndex != -1 && selectedPaymentIndex != -1) {
            CreditPanelInfo currentCredit = creditPanels.get(selectedCreditIndex);
            if (selectedPaymentIndex < currentCredit.paymentPanels.size()) {
                currentCredit.paymentPanels.get(selectedPaymentIndex).setBorderColor(DEFAULT_BORDER_COLOR);
            }
        }
        selectedPaymentIndex = -1;
    }

    private void updateCreditPanelBorder(int index, boolean selected) {
        if (index < 0 || index >= creditPanels.size()) {
            return;
        }

        JPanel panel = creditPanels.get(index).panel;
        if (selected) {
            panel.setBorder(BorderFactory.createLineBorder(SELECTED_BORDER_COLOR, 3));
        } else {
            panel.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR, 1));
        }
        panel.revalidate();
        panel.repaint();
    }

    private void updateButtonIconsForSelectedCredit() {
        if (selectedCreditIndex == -1) {
            return;
        }

        CreditPanelInfo selectedCredit = creditPanels.get(selectedCreditIndex);

        setupSelectedIconButton(selectedCredit.editButton, "edit.svg");
        setupSelectedIconButton(selectedCredit.addButton, "money-add.svg");

        if (selectedCredit.dropButton != null) {
            if (selectedCredit.isExpanded) {
                setupSelectedIconButton(selectedCredit.dropButton, "collapse.svg");
            } else {
                setupSelectedIconButton(selectedCredit.dropButton, "expand.svg");
            }
        }
    }

    private void resetButtonIconsForCredit(int index) {
        if (index < 0 || index >= creditPanels.size()) {
            return;
        }

        CreditPanelInfo credit = creditPanels.get(index);

        setupIconButton(credit.editButton, "edit.svg");
        setupIconButton(credit.addButton, "money-add.svg");

        if (credit.dropButton != null) {
            if (credit.isExpanded) {
                setupIconButton(credit.dropButton, "collapse.svg");
            } else {
                setupIconButton(credit.dropButton, "expand.svg");
            }
        }
    }

    private void setupSelectedIconButton(JButton button, String iconName) {
        try {
            FlatSVGIcon selectedIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
            selectedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> SELECTED_BORDER_COLOR));
            button.setIcon(selectedIcon);
        } catch (Exception e) {
            button.setText(iconName.substring(0, 1).toUpperCase());
        }
    }

    private void scrollToPanel(JComponent component) {
        SwingUtilities.invokeLater(() -> {
            java.awt.Rectangle bounds = component.getBounds();
            bounds.y -= 50;
            bounds.height += 100;
            jScrollPane1.getViewport().scrollRectToVisible(bounds);
        });
    }

    private void loadCustomerCredits() {
        creditPanels.clear();
        selectedCreditIndex = -1;
        selectedPaymentIndex = -1;
        jPanel1.removeAll();

        try {
            Connection conn = MySQL.getConnection();
            // FIXED: Removed sales join since credit table doesn't have sales_id
            String sql = "SELECT c.credit_id, c.credit_given_date, c.credit_final_date, "
                    + "c.credit_amout " // FIXED: credit_amout -> credit_amout
                    + "FROM credit c "
                    + "WHERE c.credit_customer_id = ? "
                    + "ORDER BY c.credit_given_date DESC";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            int yPosition = 90;
            boolean hasCredits = false;

            while (rs.next()) {
                hasCredits = true;
                int creditId = rs.getInt("credit_id");
                double creditAmount = rs.getDouble("credit_amout"); // FIXED: credit_amout -> credit_amount
                java.util.Date givenDate = rs.getDate("credit_given_date");
                java.util.Date finalDate = rs.getDate("credit_final_date");

                boolean hasPayments = checkIfCreditHasPayments(creditId);

                CreditPanelInfo creditInfo = createCreditPanel(creditId, creditAmount,
                        givenDate, finalDate, hasPayments);
                creditInfo.panel.setBounds(20, yPosition, 505, 115);
                jPanel1.add(creditInfo.panel);

                yPosition += 120;
            }

            if (!hasCredits) {
                JLabel noDataLabel = new JLabel("No credit records found for this customer");
                noDataLabel.setFont(new java.awt.Font("Nunito SemiBold", 0, 16));
                noDataLabel.setForeground(Color.GRAY);
                noDataLabel.setBounds(100, 100, 400, 30);
                jPanel1.add(noDataLabel);
            }

            jPanel1.add(jLabel3);
            jPanel1.add(jSeparator3);

            jPanel1.setPreferredSize(new java.awt.Dimension(551, Math.max(522, yPosition + 50)));
            jPanel1.revalidate();
            jPanel1.repaint();

            // Setup mouse selection after creating all panels
            setupMouseSelection();

            if (!creditPanels.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    selectCreditPanel(0);
                });
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_RIGHT,
                    "Error loading credit details: " + e.getMessage());
        }
    }

    private void setupMouseSelection() {
        for (int i = 0; i < creditPanels.size(); i++) {
            CreditPanelInfo creditInfo = creditPanels.get(i);
            final int creditIndex = i;

            // Add mouse listener to credit panel
            creditInfo.panel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    selectCreditPanel(creditIndex);
                    jPanel1.requestFocusInWindow();
                }
            });

            // Add mouse listeners to credit panel buttons to prevent event bubbling
            creditInfo.editButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    selectCreditPanel(creditIndex);
                    jPanel1.requestFocusInWindow();
                    evt.consume();
                }
            });

            creditInfo.addButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    selectCreditPanel(creditIndex);
                    jPanel1.requestFocusInWindow();
                    evt.consume();
                }
            });

            if (creditInfo.dropButton != null) {
                creditInfo.dropButton.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        selectCreditPanel(creditIndex);
                        jPanel1.requestFocusInWindow();
                        evt.consume();
                    }
                });
            }
        }
    }

    private boolean checkIfCreditHasPayments(int creditId) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT COUNT(*) as payment_count FROM credit_pay WHERE credit_id = ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, creditId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("payment_count");
                rs.close();
                pst.close();
                return count > 0;
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private CreditPanelInfo createCreditPanel(int creditId, double creditAmount,
            java.util.Date givenDate, java.util.Date finalDate,
            boolean hasPayments) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(248,250,252));
        panel.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR, 1));
        panel.setLayout(null);

        // Credit ID Label (since we don't have invoice_no anymore)
        JLabel creditLabel = new JLabel(String.format("#CRD"+ creditId));
        creditLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 16));
        creditLabel.setBounds(14, 9, 297, 23);
        panel.add(creditLabel);

        // Amount Label
        JLabel amountLabel = new JLabel(String.format("Rs %.2f", creditAmount));
        amountLabel.setFont(new java.awt.Font("Nunito ExtraBold", 1, 35));
        amountLabel.setBounds(14, 41, 297, 40);
        panel.add(amountLabel);

        // Date Label
        String dateText = dateFormat.format(givenDate) + " - " + dateFormat.format(finalDate);
        JLabel dateLabel = new JLabel(dateText);
        dateLabel.setFont(new java.awt.Font("Nunito SemiBold", 1, 16));
        dateLabel.setBounds(14, 82, 297, 23);
        panel.add(dateLabel);

        // Add Button (for adding payments)
        JButton addBtn = createStyledButton("money-add.svg", 430, 9, 25, 23);
        addBtn.setToolTipText("Add payment for this credit (F2)");
        addBtn.addActionListener(e -> addPayment(creditId));
        panel.add(addBtn);

        // Edit Button
        JButton editBtn = createStyledButton("edit.svg", 467, 9, 23, 23);
        editBtn.setToolTipText("Edit this credit (F1)");
        editBtn.addActionListener(e -> editCredit(creditId));
        panel.add(editBtn);

        // Expand/Collapse Button - ONLY if has payments
        JButton dropBtn = null;
        if (hasPayments) {
            dropBtn = createStyledButton("expand.svg", 467, 82, 23, 23);
            dropBtn.setToolTipText("Expand/collapse payments (F3)");
            CreditPanelInfo creditInfo = new CreditPanelInfo(panel, editBtn, dropBtn, addBtn, creditId, hasPayments);

            dropBtn.addActionListener(e -> {
                toggleCreditPayments(creditInfo);
            });
            panel.add(dropBtn);

            creditPanels.add(creditInfo);
            return creditInfo;
        } else {
            CreditPanelInfo creditInfo = new CreditPanelInfo(panel, editBtn, null, addBtn, creditId, hasPayments);
            creditPanels.add(creditInfo);
            return creditInfo;
        }
    }

    private JButton createStyledButton(String iconName, int x, int y, int width, int height) {
        JButton button = new JButton();
        button.setBounds(x, y, width, height);
        button.setPreferredSize(new java.awt.Dimension(width, height));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);

        setupIconButton(button, iconName);

        return button;
    }

    private void setupIconButton(JButton button, String iconName) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ICON_DEFAULT_COLOR));
            button.setIcon(icon);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);

            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
                    hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ICON_HOVER_COLOR));
                    button.setIcon(hoverIcon);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (isButtonInSelectedComponent(button)) {
                        FlatSVGIcon selectedIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
                        selectedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> SELECTED_BORDER_COLOR));
                        button.setIcon(selectedIcon);
                    } else {
                        FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
                        normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ICON_DEFAULT_COLOR));
                        button.setIcon(normalIcon);
                    }
                }
            });
        } catch (Exception e) {
            button.setText(iconName.substring(0, 1).toUpperCase());
        }
    }

    private boolean isButtonInSelectedComponent(JButton button) {
        if (selectedCreditIndex != -1) {
            CreditPanelInfo selectedCredit = creditPanels.get(selectedCreditIndex);
            if (button == selectedCredit.editButton || button == selectedCredit.dropButton || button == selectedCredit.addButton) {
                return true;
            }
        }

        return false;
    }

    private void toggleCreditPayments(CreditPanelInfo creditInfo) {
        if (creditInfo.isExpanded) {
            for (CraditPay payPanel : creditInfo.paymentPanels) {
                creditInfo.panel.remove(payPanel);
            }
            creditInfo.paymentPanels.clear();
            creditInfo.panel.setSize(505, 115);

            setupIconButton(creditInfo.dropButton, "expand.svg");
            creditInfo.dropButton.setToolTipText("Expand payments (F3)");
            creditInfo.isExpanded = false;

            if (creditPanels.indexOf(creditInfo) == selectedCreditIndex) {
                selectedPaymentIndex = -1;
                updateCreditPanelBorder(selectedCreditIndex, true);
                updateButtonIconsForSelectedCredit();
            }

        } else {
            loadCreditPayments(creditInfo);
            creditInfo.isExpanded = true;

            setupIconButton(creditInfo.dropButton, "collapse.svg");
            creditInfo.dropButton.setToolTipText("Collapse payments (F3)");

            if (creditPanels.indexOf(creditInfo) == selectedCreditIndex && !creditInfo.paymentPanels.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    selectPaymentPanel(0);
                });
            }
        }

        repositionPanels();

        if (creditPanels.indexOf(creditInfo) == selectedCreditIndex) {
            scrollToPanel(creditInfo.panel);
        }

        // Update tooltip after expansion state change
        updateSelectionToolTip();
    }

    private void loadCreditPayments(CreditPanelInfo creditInfo) {
        try {
            Connection conn = MySQL.getConnection();
            String sql = "SELECT credit_pay_id, credit_pay_date, credit_pay_amount "
                    + "FROM credit_pay "
                    + "WHERE credit_id = ? "
                    + "ORDER BY credit_pay_date DESC";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, creditInfo.creditId);
            ResultSet rs = pst.executeQuery();

            int yPosition = 130;
            int paymentCount = 0;

            while (rs.next()) {
                int payId = rs.getInt("credit_pay_id");
                java.util.Date payDate = rs.getTimestamp("credit_pay_date");
                double payAmount = rs.getDouble("credit_pay_amount");

                CraditPay payPanel = new CraditPay(payId, payDate, payAmount, creditInfo.creditId, this);
                payPanel.setBounds(12, yPosition, 481, 85);
                creditInfo.panel.add(payPanel);
                creditInfo.paymentPanels.add(payPanel);

                // Add mouse listener to payment panel for selection
                final int paymentIndex = paymentCount;
                payPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        if (selectedCreditIndex != creditPanels.indexOf(creditInfo)) {
                            selectCreditPanel(creditPanels.indexOf(creditInfo));
                        }
                        selectPaymentPanel(paymentIndex);
                        jPanel1.requestFocusInWindow();
                    }
                });

                yPosition += 100;
                paymentCount++;
            }

            int newHeight = 115 + (paymentCount * 100) + 10;
            creditInfo.panel.setSize(505, newHeight);

            creditInfo.panel.revalidate();
            creditInfo.panel.repaint();

            rs.close();
            pst.close();

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_RIGHT,
                    "Error loading payment details: " + e.getMessage());
        }
    }

    private void repositionPanels() {
        int yPosition = 90;

        for (CreditPanelInfo creditInfo : creditPanels) {
            creditInfo.panel.setLocation(20, yPosition);
            yPosition += creditInfo.panel.getHeight() + 5;
        }

        jPanel1.setPreferredSize(new java.awt.Dimension(551, Math.max(522, yPosition + 50)));
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    private void addPayment(int creditId) {
        try {
            // First check if the credit has remaining amount
            if (!checkCreditHasRemainingAmount(creditId)) {
                // Show option pane if credit is fully paid
                int option = JOptionPane.showOptionDialog(this,
                        "This credit is already fully paid. Do you want to view the payment details?",
                        "Credit Fully Paid",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new Object[]{"View Details", "Cancel"},
                        "View Details");

                if (option == JOptionPane.YES_OPTION) {
                    // Optionally expand the credit panel to show payments
                    for (CreditPanelInfo creditInfo : creditPanels) {
                        if (creditInfo.creditId == creditId && creditInfo.hasPayments && !creditInfo.isExpanded) {
                            creditInfo.dropButton.doClick();
                            break;
                        }
                    }
                }
                return;
            }

            Window ownerWindow = getOwner();
            AddCreditPay dialog = null;

            if (ownerWindow instanceof JFrame) {
                dialog = new AddCreditPay((JFrame) ownerWindow, true, creditId);
            } else {
                dialog = new AddCreditPay((JFrame) null, true, creditId);
            }

            dialog.setLocationRelativeTo(this);

            // Add window listener to refresh after dialog closes
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    refreshCredits();
                }
            });

            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_RIGHT, "Error opening add payment dialog: " + e.getMessage());
        }
    }

    private boolean checkCreditHasRemainingAmount(int creditId) {
        try {
            Connection conn = MySQL.getConnection();
            // FIXED: Corrected field name from credit_amout to credit_amount
            String sql = "SELECT c.credit_amout, COALESCE(SUM(cp.credit_pay_amount), 0) as paid_amount, "
                    + "(c.credit_amout - COALESCE(SUM(cp.credit_pay_amount), 0)) as remaining_amount "
                    + "FROM credit c "
                    + "LEFT JOIN credit_pay cp ON c.credit_id = cp.credit_id "
                    + "WHERE c.credit_id = ? "
                    + "GROUP BY c.credit_id, c.credit_amout";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, creditId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                double remainingAmount = rs.getDouble("remaining_amount");
                rs.close();
                pst.close();
                return remainingAmount > 0;
            }

            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void editCredit(int creditId) {
        try {
            Window ownerWindow = getOwner();
            UpdateCredit dialog = null;

            if (ownerWindow instanceof JFrame) {
                dialog = new UpdateCredit((JFrame) ownerWindow, true, creditId);
            } else {
                dialog = new UpdateCredit((JFrame) null, true, creditId);
            }

            dialog.setLocationRelativeTo(this);

            // Add window listener to refresh after dialog closes
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    refreshCredits();
                }
            });

            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_RIGHT, "Error opening edit credit dialog: " + e.getMessage());
        }
    }

    public void refreshCredits() {
        jPanel1.removeAll();
        creditPanels.clear();

        jPanel1.add(jLabel3);
        jPanel1.add(jSeparator3);

        loadCustomerCredits();
    }

    // ... Rest of the generated code remains the same ...
    // The generated initComponents() method and other auto-generated code stays unchanged  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        creditPanel = new javax.swing.JPanel();
        date = new javax.swing.JLabel();
        editBtn = new javax.swing.JButton();
        dropBtn = new javax.swing.JButton();
        amount = new javax.swing.JLabel();
        invoiceId = new javax.swing.JLabel();
        addBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(551, 522));
        jPanel1.setPreferredSize(new java.awt.Dimension(551, 522));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Credit Details View");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        creditPanel.setBackground(new java.awt.Color(51, 255, 0));

        date.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        date.setText("02/30/2025 - 03/30/2025");

        editBtn.setText("E");
        editBtn.setPreferredSize(new java.awt.Dimension(33, 33));

        dropBtn.setText("D");
        dropBtn.setPreferredSize(new java.awt.Dimension(33, 33));

        amount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 35)); // NOI18N
        amount.setText("Rs 20,000");

        invoiceId.setFont(new java.awt.Font("Nunito SemiBold", 3, 16)); // NOI18N
        invoiceId.setForeground(new java.awt.Color(102, 102, 102));
        invoiceId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        invoiceId.setText("0000111");

        addBtn.setText("A");
        addBtn.setPreferredSize(new java.awt.Dimension(33, 33));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout creditPanelLayout = new javax.swing.GroupLayout(creditPanel);
        creditPanel.setLayout(creditPanelLayout);
        creditPanelLayout.setHorizontalGroup(
            creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(creditPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creditPanelLayout.createSequentialGroup()
                        .addComponent(invoiceId)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(creditPanelLayout.createSequentialGroup()
                        .addGroup(creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(amount, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(creditPanelLayout.createSequentialGroup()
                                .addComponent(date)
                                .addGap(98, 98, 98)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 171, Short.MAX_VALUE)
                        .addComponent(dropBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        creditPanelLayout.setVerticalGroup(
            creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, creditPanelLayout.createSequentialGroup()
                .addGroup(creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creditPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(creditPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(invoiceId)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(amount, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(creditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dropBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(creditPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(25, 25, 25)
                            .addComponent(jLabel3))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(creditPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(352, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CreditView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CreditView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CreditView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CreditView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // This main method is for testing only
                // In your actual application, use: new CreditView(parent, modal, customerId)
                CreditView dialog = new CreditView(new javax.swing.JFrame(), true , 3); // Example customer ID
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JLabel amount;
    private javax.swing.JPanel creditPanel;
    private javax.swing.JLabel date;
    private javax.swing.JButton dropBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JLabel invoiceId;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables
}
