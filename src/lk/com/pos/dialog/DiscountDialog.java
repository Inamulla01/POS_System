package lk.com.pos.dialog;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import lk.com.pos.connection.MySQL;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

/**
 *
 * @author moham
 */
public class DiscountDialog extends javax.swing.JDialog {

    private Double discountAmount = 0.0;
    private int discountTypeId = -1;
    private Double originalTotal;
    private Map<String, Integer> discountTypeIdMap = new HashMap<>();
    private boolean isSaving = false;

    /**
     * Creates new form DiscountDialog
     */
    public DiscountDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
    }

    // Constructor with total amount
    public DiscountDialog(java.awt.Frame parent, boolean modal, Double total) {
        super(parent, modal);
        this.originalTotal = total;
        initComponents();
        initializeDialog(total);
    }

    // Method to get the discount amount
    public Double getDiscountAmount() {
        return discountAmount;
    }

    // Method to get the discount type ID
    public int getDiscountTypeId() {
        return discountTypeId;
    }

    // Method to check if discount was applied successfully
    public boolean isDiscountApplied() {
        return discountAmount > 0 && discountTypeId != -1;
    }

    private void initializeDialog() {
        initializeDialog(null);
    }

    private void initializeDialog(Double total) {
        setLocationRelativeTo(getParent());
        setupKeyboardNavigation();
        setupButtonStyles();
        setupTooltips();

        // Load discount types
        loadDiscountTypes();
        AutoCompleteDecorator.decorate(diTypeCombo);

        // Set focus traversal
        setupFocusTraversal();

        // Set total if provided
        if (total != null && total > 0) {
            calculateDiscount(); // Calculate initial discount
        }

        // Add mouse listeners to combo box
        diTypeCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                diTypeCombo.showPopup();
            }
        });

        // Add keyboard shortcuts
        getRootPane().registerKeyboardAction(
                evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set initial focus
        diTypeCombo.requestFocusInWindow();
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupKeyboardNavigation() {
        // Discount type combo keyboard navigation
        diTypeCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    discountInput.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    discountInput.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    saveBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, diTypeCombo);
                }
            }
        });

        // Discount input keyboard navigation
        discountInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    // When both fields are filled, go to save button
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    diTypeCombo.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // When both fields are filled, go to save button
                    if (areAllRequiredFieldsFilled()) {
                        saveBtn.requestFocusInWindow();
                    } else {
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    diTypeCombo.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, discountInput);
                }
            }
        });

        // Save button keyboard navigation
        saveBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    discountInput.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    cancelBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, saveBtn);
                }
            }
        });

        // Cancel button keyboard navigation
        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    discountInput.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    saveBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    saveBtn.requestFocus();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    saveBtn.requestFocus();
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, cancelBtn);
                }
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
        if (source == diTypeCombo) {
            discountInput.requestFocusInWindow();
        } else if (source == discountInput) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            diTypeCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == diTypeCombo) {
            saveBtn.requestFocusInWindow();
        } else if (source == discountInput) {
            diTypeCombo.requestFocusInWindow();
        } else if (source == cancelBtn) {
            discountInput.requestFocusInWindow();
        } else if (source == saveBtn) {
            cancelBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == diTypeCombo) {
            discountInput.requestFocusInWindow();
        } else if (source == discountInput) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            diTypeCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == diTypeCombo) {
            saveBtn.requestFocusInWindow();
        } else if (source == discountInput) {
            diTypeCombo.requestFocusInWindow();
        } else if (source == cancelBtn) {
            discountInput.requestFocusInWindow();
        } else if (source == saveBtn) {
            cancelBtn.requestFocusInWindow();
        }
    }

    private boolean areAllRequiredFieldsFilled() {
        return diTypeCombo.getSelectedIndex() > 0
                && !discountInput.getText().trim().isEmpty()
                && !discountInput.getText().trim().equals("0");
    }

    // ---------------- BUTTON STYLES AND EFFECTS ----------------
    private void setupButtonStyles() {
        // Setup gradient buttons for Save and Cancel
        setupGradientButton(saveBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon saveIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        saveIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(saveIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        // Setup mouse listeners for all buttons
        setupButtonMouseListeners();
        setupButtonFocusListeners();
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

    private void setupTooltips() {
        diTypeCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to next field</html>");
        discountInput.setToolTipText("<html>Type discount amount/percentage and press ENTER<br>Discount will be calculated based on selected type</html>");
        saveBtn.setToolTipText("Click to apply discount (or press ENTER when focused)");
        cancelBtn.setToolTipText("Click to cancel (or press ESC)");
    }

    // ---------------- BUSINESS LOGIC ----------------
    private void loadDiscountTypes() {
        try {
            // Clear the mapping first
            discountTypeIdMap.clear();

            String sql = "SELECT discount_type_id, discount_type FROM discount_type ORDER BY discount_type_id";

            ResultSet rs = MySQL.executeSearch(sql);
            Vector<String> discountTypes = new Vector<>();
            discountTypes.add("Select Discount Type");

            int count = 0;
            while (rs.next()) {
                int typeId = rs.getInt("discount_type_id");
                String discountType = rs.getString("discount_type");

                // Create display text
                String displayText = discountType;

                discountTypes.add(displayText);

                // Store the mapping between display text and type ID
                discountTypeIdMap.put(displayText, typeId);
                count++;
            }

            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(discountTypes);
            diTypeCombo.setModel(dcm);

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading discount types: " + e.getMessage());
        }
    }

    private int getDiscountTypeId(String displayText) {
        Integer typeId = discountTypeIdMap.get(displayText);

        if (typeId == null) {
            return -1;
        }

        return typeId;
    }

    private void calculateDiscount() {
    if (originalTotal == null || originalTotal <= 0) {
        return;
    }

    if (diTypeCombo.getSelectedIndex() <= 0) {
        return;
    }

    try {
        String discountValueStr = discountInput.getText().trim();
        if (discountValueStr.isEmpty()) {
            return;
        }

        double discountValue = Double.parseDouble(discountValueStr);
        String selectedType = (String) diTypeCombo.getSelectedItem();

        // Check if it's percentage discount (PERC)
        if (selectedType != null && selectedType.toUpperCase().contains("PERC")) {
            // Percentage discount
            if (discountValue > 100) {
                discountValue = 100; // Cap at 100%
                discountInput.setText("100");
                JOptionPane.showMessageDialog(this,
                        "Percentage discount cannot exceed 100%",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
            }
            if (discountValue < 0) {
                discountValue = 0;
                discountInput.setText("0");
            }
            
            // Calculate discount amount from percentage
            discountAmount = (originalTotal * discountValue) / 100.0;
            
        } else if (selectedType != null && selectedType.toUpperCase().contains("FIXD")) {
            // Fixed amount discount
            if (discountValue > originalTotal) {
                discountValue = originalTotal; // Cap at total amount
                discountInput.setText(String.format("%.2f", discountValue));
                JOptionPane.showMessageDialog(this,
                        "Discount amount cannot exceed total amount",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
            }
            if (discountValue < 0) {
                discountValue = 0;
                discountInput.setText("0");
            }
            
            // Use the value directly as discount amount
            discountAmount = discountValue;
            
        } else {
            // Default to fixed amount if type is unclear
            if (discountValue > originalTotal) {
                discountValue = originalTotal;
            }
            if (discountValue < 0) {
                discountValue = 0;
            }
            discountAmount = discountValue;
        }

    } catch (NumberFormatException e) {
        discountAmount = 0.0;
    }
}

    private boolean validateInputs() {
    if (diTypeCombo.getSelectedIndex() == 0) {
        Notifications.getInstance().show(Notifications.Type.WARNING, 
                Notifications.Location.TOP_RIGHT, "Please select a discount type");
        diTypeCombo.requestFocus();
        return false;
    }

    if (discountInput.getText().trim().isEmpty()) {
        Notifications.getInstance().show(Notifications.Type.WARNING, 
                Notifications.Location.TOP_RIGHT, "Please enter discount value");
        discountInput.requestFocus();
        return false;
    }

    double discountValue = 0;
    try {
        discountValue = Double.parseDouble(discountInput.getText().trim());
        if (discountValue <= 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, 
                    Notifications.Location.TOP_RIGHT, "Discount value must be greater than 0");
            discountInput.requestFocus();
            return false;
        }
    } catch (NumberFormatException e) {
        Notifications.getInstance().show(Notifications.Type.WARNING, 
                Notifications.Location.TOP_RIGHT, "Please enter a valid discount value");
        discountInput.requestFocus();
        return false;
    }

    String selectedType = (String) diTypeCombo.getSelectedItem();
    
    // Validate percentage discount
    if (selectedType != null && selectedType.toUpperCase().contains("PERC")) {
        if (discountValue > 100) {
            Notifications.getInstance().show(Notifications.Type.WARNING, 
                    Notifications.Location.TOP_RIGHT, "Percentage discount cannot exceed 100%");
            discountInput.requestFocus();
            return false;
        }
    }
    
    // Validate fixed amount discount
    if (selectedType != null && selectedType.toUpperCase().contains("FIXD")) {
        if (originalTotal != null && discountValue > originalTotal) {
            Notifications.getInstance().show(Notifications.Type.WARNING, 
                    Notifications.Location.TOP_RIGHT, 
                    String.format("Discount amount cannot exceed total (Rs.%.2f)", originalTotal));
            discountInput.requestFocus();
            return false;
        }
    }

    return true;
}

    private void applyDiscount() {
        // Prevent multiple simultaneous saves
        if (isSaving) {
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            isSaving = true;

            String selectedDisplayText = (String) diTypeCombo.getSelectedItem();
            this.discountTypeId = getDiscountTypeId(selectedDisplayText);
            if (this.discountTypeId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid discount type selected");
                isSaving = false;
                return;
            }

            // Calculate final discount amount
            calculateDiscount();

            if (discountAmount <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Invalid discount amount calculated");
                isSaving = false;
                return;
            }

            // Save discount to database
            String query = "INSERT INTO discount (discount, discount_type_id) VALUES (?, ?)";

            Connection conn = MySQL.getConnection();
            PreparedStatement pst = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setDouble(1, discountAmount);
            pst.setInt(2, this.discountTypeId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Discount applied successfully!");
                dispose(); // Close the dialog after successful application
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Failed to apply discount!");
            }

            pst.close();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error applying discount: " + e.getMessage());
        } finally {
            isSaving = false;
        }
    }

    private void clearForm() {
        diTypeCombo.setSelectedIndex(0);
        discountInput.setText("");
        diTypeCombo.requestFocus();
    }

    // ---------------- FOCUS TRAVERSAL SETUP ----------------
    private void setupFocusTraversal() {
        // Make all main components focusable in order
        diTypeCombo.setFocusable(true);
        discountInput.setFocusable(true);
        saveBtn.setFocusable(true);
        cancelBtn.setFocusable(true);
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        diTypeCombo = new javax.swing.JComboBox<>();
        cancelBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        discountInput = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Discount");

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Discount");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        diTypeCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        diTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        diTypeCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Discount Type *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        diTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diTypeComboActionPerformed(evt);
            }
        });
        diTypeCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diTypeComboKeyPressed(evt);
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
        saveBtn.setText("Add");
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

        discountInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        discountInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Discount  *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        discountInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discountInputActionPerformed(evt);
            }
        });
        discountInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                discountInputKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSeparator3)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(diTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(discountInput, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 21, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(diTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discountInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void diTypeComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diTypeComboKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (diTypeCombo.isPopupVisible()) {
                diTypeCombo.setPopupVisible(false);
            }
            if (diTypeCombo.getSelectedIndex() > 0) {
                discountInput.requestFocusInWindow();
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!diTypeCombo.isPopupVisible()) {
                diTypeCombo.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!diTypeCombo.isPopupVisible()) {
                saveBtn.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, diTypeCombo);
        }

    }//GEN-LAST:event_diTypeComboKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        } else {
            handleArrowNavigation(evt, cancelBtn);
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        applyDiscount();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            applyDiscount();
        } else {
            handleArrowNavigation(evt, saveBtn);
        }
    }//GEN-LAST:event_saveBtnKeyPressed

    private void discountInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discountInputActionPerformed
        calculateDiscount();
    }//GEN-LAST:event_discountInputActionPerformed

    private void discountInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_discountInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            calculateDiscount();
            if (areAllRequiredFieldsFilled()) {
                saveBtn.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
            evt.consume();
        } else {
            handleArrowNavigation(evt, discountInput);
        }
    }//GEN-LAST:event_discountInputKeyPressed

    private void diTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diTypeComboActionPerformed
        if (diTypeCombo.getSelectedIndex() > 0 && !diTypeCombo.isPopupVisible()) {
            discountInput.requestFocusInWindow();
        }
    }//GEN-LAST:event_diTypeComboActionPerformed

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
            java.util.logging.Logger.getLogger(DiscountDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DiscountDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DiscountDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DiscountDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DiscountDialog dialog = new DiscountDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<String> diTypeCombo;
    private javax.swing.JTextField discountInput;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
