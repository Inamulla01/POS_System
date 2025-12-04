package lk.com.pos.dialog;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import lk.com.pos.connection.DB; // CHANGED: Updated import
import lk.com.pos.connection.DB.ResultSetHandler; // ADDED: Import for ResultSetHandler

import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class CardPayDialog extends javax.swing.JDialog {

    private Integer generatedCardPaymentId = null;
    private Integer salesId = null; // Add this field to store sales ID
    private boolean isCardPaymentSaved = false; // Track if card payment was saved

    /**
     * Creates new form CardPayDialog
     */
    public CardPayDialog(java.awt.Frame parent, boolean modal, Integer salesId) {
        super(parent, modal);
        this.salesId = salesId; // Store the sales ID
        initComponents();
        initializeDialog();
    }

    /**
     * Get the generated card payment ID after saving
     *
     * @return the card_payment_id or null if not saved
     */
    public Integer getGeneratedCardPaymentId() {
        return generatedCardPaymentId;
    }

    private void initializeDialog() {
        // Center the dialog
        setLocationRelativeTo(getParent());

        // Set focus traversal
        setupFocusTraversal();

        // Add keyboard shortcuts
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteSalesIfNotSaved();
                dispose();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Setup button styles
        setupButtonStyles();

        // Set focus to code input when dialog opens
        codeInput.requestFocusInWindow();

        // Update title to show sales ID if available
        if (salesId != null) {
            setTitle("Add Card Payment - Sales #" + salesId);
        }
    }

    private void deleteSalesIfNotSaved() {
        // Only delete if salesId exists and card payment was NOT saved
        if (salesId != null && salesId != -1 && !isCardPaymentSaved) {
            Connection conn = null;

            try {
                conn = DB.getConnection(); // CHANGED: Using DB.getConnection()
                conn.setAutoCommit(false); // Start transaction

                // First, check if the sale still exists
                Integer saleCount = DB.executeQuerySafe(
                    "SELECT COUNT(*) FROM sales WHERE sales_id = ?",
                    new ResultSetHandler<Integer>() {
                        @Override
                        public Integer handle(ResultSet rs) throws SQLException {
                            if (rs.next()) {
                                return rs.getInt(1);
                            }
                            return 0;
                        }
                    },
                    salesId
                );

                if (saleCount == 0) {
                    // Sale doesn't exist anymore, nothing to do
                    conn.rollback();
                    return;
                }

                // Get all sale items for this sales_id to return them to stock
                List<Map<String, Integer>> itemsToReturn = DB.executeQuerySafe(
                    "SELECT si.stock_id, si.qty FROM sale_item si WHERE si.sales_id = ?",
                    new ResultSetHandler<List<Map<String, Integer>>>() {
                        @Override
                        public List<Map<String, Integer>> handle(ResultSet rs) throws SQLException {
                            List<Map<String, Integer>> items = new ArrayList<>();
                            while (rs.next()) {
                                Map<String, Integer> item = new HashMap<>();
                                item.put("stock_id", rs.getInt("stock_id"));
                                item.put("qty", rs.getInt("qty"));
                                items.add(item);
                            }
                            return items;
                        }
                    },
                    salesId
                );

                // Return each item to stock
                for (Map<String, Integer> item : itemsToReturn) {
                    DB.executeUpdate(
                        "UPDATE stock SET qty = qty + ? WHERE stock_id = ?",
                        item.get("qty"), item.get("stock_id")
                    );
                }

                // Delete related records in correct order to maintain referential integrity
                String[] deleteQueries = {
                    "DELETE FROM cheque WHERE sales_id = ?",
                    "DELETE FROM card_pay WHERE sales_id = ?",
                    "DELETE FROM stock_loss WHERE sales_id = ?",
                    "DELETE FROM sale_item WHERE sales_id = ?",
                    "DELETE FROM sales WHERE sales_id = ?"
                };

                boolean success = true;
                for (String query : deleteQueries) {
                    try {
                        DB.executeUpdate(query, salesId);
                    } catch (Exception e) {
                        System.err.println("Error executing query: " + query + " - " + e.getMessage());
                        success = false;
                        break;
                    }
                }

                if (success) {
                    conn.commit();
                    System.out.println("Successfully deleted sale #" + salesId + " and returned stock");
                } else {
                    conn.rollback();
                    System.err.println("Failed to delete sale #" + salesId + ", transaction rolled back");
                }

            } catch (Exception e) {
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (Exception rollbackEx) {
                    // Rollback exception ignored
                }
                
                // Show error notification to user
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Error deleting sale: " + e.getMessage());
            } finally {
                // Close connection using DB class helper
                DB.closeQuietly(conn);
            }
        }
    }

    private void setupGradientButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.decode("#0893B0"));
        button.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                int w = c.getWidth();
                int h = c.getHeight();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Check button state
                boolean isHover = button.getModel().isRollover();
                boolean isPressed = button.getModel().isPressed();
                boolean isFocused = button.hasFocus();

                // Default state - transparent with blue border
                if (!isFocused && !isHover && !isPressed) {
                    g2.setColor(new Color(0, 0, 0, 0)); // Transparent
                    g2.fillRoundRect(0, 0, w, h, 5, 5);

                    // Draw border
                    g2.setColor(Color.decode("#0893B0"));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
                } else {
                    // Gradient colors for hover/focus/pressed state
                    Color topColor = new Color(0x12, 0xB5, 0xA6); // Light
                    Color bottomColor = new Color(0x08, 0x93, 0xB0); // Dark

                    // Draw gradient
                    GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 5, 5);
                }

                // Draw button text
                super.paint(g, c);
            }
        });
    }

    private void setupButtonStyles() {
        // Setup gradient buttons
        setupGradientButton(saveBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(saveIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        // Setup mouse listeners for all buttons
        setupButtonMouseListeners();
        setupButtonFocusListeners();
    }

    private void setupButtonMouseListeners() {
        // Mouse listeners for saveBtn
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(hoverIcon);
                saveBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                saveBtn.setIcon(normalIcon);
                saveBtn.repaint();
            }
        });

        // Mouse listeners for clearFormBtn
        clearFormBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(hoverIcon);
                clearFormBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
            }
        });

        // Mouse listeners for cancelBtn
        cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(hoverIcon);
                cancelBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });
    }

    private void setupButtonFocusListeners() {
        // Focus listeners for saveBtn
        saveBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(focusedIcon);
                saveBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                saveBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                saveBtn.setIcon(normalIcon);
                saveBtn.repaint();
            }
        });

        // Focus listeners for clearFormBtn
        clearFormBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(focusedIcon);
                clearFormBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
            }
        });

        // Focus listeners for cancelBtn
        cancelBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(focusedIcon);
                cancelBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });
    }

    private void setupFocusTraversal() {
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(
                codeInput,
                saveBtn,
                clearFormBtn,
                cancelBtn
        );

        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)));

        setupArrowKeyNavigation();
        addEnterKeyNavigation();
    }

    private void setupArrowKeyNavigation() {
        codeInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, codeInput);
            }
        });

        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, saveBtn);
            }
        });

        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, clearFormBtn);
            }
        });

        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, cancelBtn);
            }
        });
    }

    private void handleArrowNavigation(java.awt.event.KeyEvent evt, java.awt.Component source) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                handleRightArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_LEFT:
                handleLeftArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                handleDownArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                handleUpArrow(source);
                evt.consume();
                break;
        }
    }

    private void handleRightArrow(java.awt.Component source) {
        if (source == codeInput) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            codeInput.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == codeInput) {
            cancelBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            codeInput.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == codeInput) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            codeInput.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == codeInput) {
            cancelBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            codeInput.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void addEnterKeyNavigation() {
        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(codeInput, saveBtn);
        enterNavigationMap.put(saveBtn, clearFormBtn);
        enterNavigationMap.put(clearFormBtn, cancelBtn);
        enterNavigationMap.put(cancelBtn, codeInput);

        for (java.awt.Component component : enterNavigationMap.keySet()) {
            component.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        java.awt.Component nextComponent = enterNavigationMap.get(component);
                        if (nextComponent != null) {
                            nextComponent.requestFocusInWindow();
                        }
                        evt.consume();
                    }
                }
            });
        }
    }

    private boolean validateInputs() {
        if (salesId == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "No sales ID provided");
            return false;
        }

        if (codeInput.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter card payment code");
            codeInput.requestFocus();
            return false;
        }

        String code = codeInput.getText().trim();
        if (code.length() > 20) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Card payment code must be 20 characters or less");
            codeInput.requestFocus();
            codeInput.selectAll();
            return false;
        }

        // Check if code already exists for this sales ID
        if (isCardPayCodeExists(code)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Card payment code already exists");
            codeInput.requestFocus();
            codeInput.selectAll();
            return false;
        }

        return true;
    }

    private boolean isCardPayCodeExists(String code) {
        try {
            // CHANGED: Using DB.executeQuerySafe with parameterized query
            Integer count = DB.executeQuerySafe(
                "SELECT COUNT(*) FROM card_pay WHERE card_pay_code = ? AND sales_id = ?",
                new ResultSetHandler<Integer>() {
                    @Override
                    public Integer handle(ResultSet rs) throws SQLException {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                        return 0;
                    }
                },
                code, salesId
            );
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void saveCardPayment() {
        if (!validateInputs()) {
            return;
        }

        Connection conn = null;

        try {
            String code = codeInput.getText().trim();

            conn = DB.getConnection(); // CHANGED: Using DB.getConnection()
            conn.setAutoCommit(false);

            // CHANGED: Using DB.insertAndGetId for auto-increment ID
            int generatedId = DB.insertAndGetId(
                "INSERT INTO card_pay (card_pay_code, sales_id) VALUES (?, ?)",
                code, salesId
            );

            if (generatedId > 0) {
                generatedCardPaymentId = generatedId;
                isCardPaymentSaved = true; // Mark as saved
                
                // Create notification
                createCardPaymentNotification(code, conn);
                conn.commit();
                
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "Card payment saved successfully! ID: " + generatedCardPaymentId);

                // Close the dialog after successful save
                this.dispose();
            } else {
                conn.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Failed to save card payment");
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
                // Rollback exception ignored
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error saving card payment: " + e.getMessage());
        } finally {
            DB.closeQuietly(conn);
        }
    }

    private void createCardPaymentNotification(String code, Connection conn) {
        try {
            // Get sale total to include in notification
            Double saleTotal = DB.executeQuerySafe(
                "SELECT total FROM sales WHERE sales_id = ?",
                new ResultSetHandler<Double>() {
                    @Override
                    public Double handle(ResultSet rs) throws SQLException {
                        if (rs.next()) {
                            return rs.getDouble("total");
                        }
                        return 0.0;
                    }
                },
                salesId
            );

            String totalInfo = "";
            if (saleTotal != null && saleTotal > 0) {
                totalInfo = " | Sale Total: Rs " + String.format("%,.2f", saleTotal);
            }

            String messageText = "New card payment added | Code: " + code + totalInfo + " | Sales ID: " + salesId;

            // Check if message already exists
            Integer existingMessageId = DB.executeQuerySafe(
                "SELECT massage_id FROM massage WHERE massage = ?",
                new ResultSetHandler<Integer>() {
                    @Override
                    public Integer handle(ResultSet rs) throws SQLException {
                        if (rs.next()) {
                            return rs.getInt("massage_id");
                        }
                        return null;
                    }
                },
                messageText
            );

            int massageId;
            if (existingMessageId != null) {
                massageId = existingMessageId;
            } else {
                // Insert new message
                massageId = DB.insertAndGetId(
                    "INSERT INTO massage (massage) VALUES (?)",
                    messageText
                );
            }

            // Insert notification
            DB.executeUpdate(
                "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)",
                1, 13, massageId
            );

        } catch (Exception e) {
            // Error handled silently as notification creation is not critical
            System.err.println("Error creating notification: " + e.getMessage());
        }
    }

    private void clearForm() {
        codeInput.setText("");
        generatedCardPaymentId = null;
        codeInput.requestFocus();
    }

    // Override dispose to handle sales deletion when dialog is closed
    @Override
    public void dispose() {
        // Only delete if we haven't saved a card payment
        if (!isCardPaymentSaved) {
            deleteSalesIfNotSaved();
        }
        super.dispose();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        codeInput = new javax.swing.JTextField();
        clearFormBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add Card Payment");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Add Card Payment");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        codeInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        codeInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Code No *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        codeInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeInputActionPerformed(evt);
            }
        });
        codeInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                codeInputKeyPressed(evt);
            }
        });

        clearFormBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        clearFormBtn.setForeground(new java.awt.Color(8, 147, 176));
        clearFormBtn.setText("Clear Form");
        clearFormBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        clearFormBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFormBtnActionPerformed(evt);
            }
        });
        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                clearFormBtnKeyPressed(evt);
            }
        });

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setForeground(new java.awt.Color(8, 147, 176));
        cancelBtn.setText("Cancel");
        cancelBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cancelBtnKeyPressed(evt);
            }
        });

        saveBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        saveBtn.setForeground(new java.awt.Color(8, 147, 176));
        saveBtn.setText("Save");
        saveBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });
        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                saveBtnKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(codeInput, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(22, 22, 22))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(codeInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void codeInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeInputActionPerformed
        saveBtn.requestFocusInWindow();
    }//GEN-LAST:event_codeInputActionPerformed

    private void codeInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codeInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveBtn.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, codeInput);
        }
    }//GEN-LAST:event_codeInputKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        } else {
            handleArrowNavigation(evt, clearFormBtn);
        }
    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        deleteSalesIfNotSaved();
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            deleteSalesIfNotSaved();
            this.dispose();
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            deleteSalesIfNotSaved();
            this.dispose();
        } else {
            handleArrowNavigation(evt, cancelBtn);
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        saveCardPayment();

    }//GEN-LAST:event_saveBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveCardPayment();
        } else {
            handleArrowNavigation(evt, saveBtn);
        }
    }//GEN-LAST:event_saveBtnKeyPressed

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
            java.util.logging.Logger.getLogger(CardPayDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CardPayDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CardPayDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CardPayDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CardPayDialog dialog = new CardPayDialog(new javax.swing.JFrame(), true, 1);
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
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JTextField codeInput;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
