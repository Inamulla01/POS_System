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
import lk.com.pos.connection.MySQL;

import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

public class AddNewStock extends javax.swing.JDialog {

    private Set<String> generatedBatchNumbers = new HashSet<>();

    public AddNewStock(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
        AutoCompleteDecorator.decorate(productNameCombo);
        AutoCompleteDecorator.decorate(SupplierCombo);
    }

    private void initializeDialog() {
        // Center the dialog
        setLocationRelativeTo(getParent());

        // Load combo box data
        loadProductCombo();
        loadSupplierCombo();

        // Generate initial batch number
        generateBatchNumber();

        // Set focus traversal
        setupFocusTraversal();

        // Add mouse listeners to combo boxes
        productNameCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                productNameCombo.showPopup();
            }
        });

        SupplierCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SupplierCombo.showPopup();
            }
        });

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        batchNoInput.setToolTipText("Batch number is auto-generated. Press F2 or Space to regenerate.");
        productNameCombo.setToolTipText("Use DOWN arrow to open dropdown, ENTER to select and move to next field");
        SupplierCombo.setToolTipText("Use DOWN arrow to open dropdown, ENTER to select and move to next field");
        quantityInput.setToolTipText("Type quantity and press ENTER to move to next field");
        manufactureDate.setToolTipText("Type date in format dd/mm/yyyy then press ENTER");
        expriyDate.setToolTipText("Type date in format dd/mm/yyyy then press ENTER");
        addNewProduct.setToolTipText("Press ENTER to add new product, RIGHT arrow to go to add supplier");
        addNewSupplier.setToolTipText("Press ENTER to add new supplier, LEFT arrow to go to add product");

        // Fix batch number input to be focusable but not editable
        batchNoInput.setEditable(false);
        batchNoInput.setFocusable(true);

        // Make date editors focusable and add key listeners
        manufactureDate.getDateEditor().getUiComponent().setFocusable(true);
        expriyDate.getDateEditor().getUiComponent().setFocusable(true);

        // Setup button styles with proper focus and hover effects
        setupButtonStyles();

        // Setup proper date chooser navigation
        setupDateChooserNavigation();

        // Set default quantity
        quantityInput.setText("1");
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

    private void applyGradientBackground(JButton button, boolean active) {
        // This method is now redundant since the UI paints based on button state
        button.repaint(); // Trigger repaint to show/hide gradient
    }

    private void setupButtonStyles() {
        // Remove borders, backgrounds, and set hover effects for add buttons
        addNewProduct.setBorderPainted(false);
        addNewProduct.setContentAreaFilled(false);
        addNewProduct.setFocusPainted(false);
        addNewProduct.setOpaque(false);

        addNewSupplier.setBorderPainted(false);
        addNewSupplier.setContentAreaFilled(false);
        addNewSupplier.setFocusPainted(false);
        addNewSupplier.setOpaque(false);

        // Create icons with original gray color for add buttons
        FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add-product.svg", 25, 25);
        addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewProduct.setIcon(addIcon);

        FlatSVGIcon add1Icon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
        add1Icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewSupplier.setIcon(add1Icon);

        // Setup gradient buttons
        setupGradientButton(saveBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon add2Icon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        add2Icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(add2Icon);

        FlatSVGIcon add3Icon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        add3Icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(add3Icon);

        FlatSVGIcon add4Icon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        add4Icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(add4Icon);

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

            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon pressedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                pressedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(pressedIcon);
                saveBtn.repaint();
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                saveBtn.setForeground(Color.WHITE);
                FlatSVGIcon releasedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                releasedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                saveBtn.setIcon(releasedIcon);
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

            public void mousePressed(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon pressedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                pressedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(pressedIcon);
                clearFormBtn.repaint();
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon releasedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                releasedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(releasedIcon);
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

            public void mousePressed(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon pressedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                pressedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(pressedIcon);
                cancelBtn.repaint();
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon releasedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                releasedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(releasedIcon);
                cancelBtn.repaint();
            }
        });

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

        // Add mouse listeners for hover effects - gray background on hover, blue icon
        addNewProduct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {

                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add-product.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0"))); // Blue
                addNewProduct.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {

                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add-product.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999"))); // Blue
                addNewProduct.setIcon(normalIcon);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {

                FlatSVGIcon pressedIcon = new FlatSVGIcon("lk/com/pos/icon/add-product.svg", 25, 25);
                pressedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0"))); // Blue
                addNewProduct.setIcon(pressedIcon);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {

                FlatSVGIcon releasedIcon = new FlatSVGIcon("lk/com/pos/icon/add-product.svg", 25, 25);
                releasedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999"))); // Blue
                addNewProduct.setIcon(releasedIcon);
            }
        });

        addNewSupplier.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {

                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0"))); // Blue
                addNewSupplier.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {

                // Keep blue icon color
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999"))); // Blue
                addNewSupplier.setIcon(normalIcon);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {

                FlatSVGIcon pressedIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                pressedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0"))); // Blue
                addNewSupplier.setIcon(pressedIcon);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {

                FlatSVGIcon releasedIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                releasedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999"))); // Blue
                addNewSupplier.setIcon(releasedIcon);
            }
        });
        // Add focus listeners to maintain blue icon color when focused
        addNewProduct.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                // Keep blue icon color when focused
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add-product.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0"))); // Blue
                addNewProduct.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                // Keep blue icon color when focus lost
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add-product.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999"))); // Blue
                addNewProduct.setIcon(normalIcon);
            }
        });

        addNewSupplier.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                // Keep blue icon color when focused
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0"))); // Blue
                addNewSupplier.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                // Keep blue icon color when focus lost
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999"))); // Blue
                addNewSupplier.setIcon(normalIcon);
            }
        });
    }

    private void setupDateChooserNavigation() {
        // Get the actual text field components from date editors
        javax.swing.JTextField manufactureDateEditor = (javax.swing.JTextField) manufactureDate.getDateEditor().getUiComponent();
        javax.swing.JTextField expriyDateEditor = (javax.swing.JTextField) expriyDate.getDateEditor().getUiComponent();

        // Add key listener to manufacture date editor
        manufactureDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    expriyDateEditor.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    expriyDateEditor.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    quantityInput.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    expriyDateEditor.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    quantityInput.requestFocusInWindow();
                    evt.consume();
                }
            }
        });

        // Add key listener to expiry date editor
        expriyDateEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    saveBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    manufactureDateEditor.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    saveBtn.requestFocusInWindow();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    manufactureDateEditor.requestFocusInWindow();
                    evt.consume();
                }
            }
        });
    }

    private void loadProductCombo() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT product_id, product_name FROM product");
            Vector<String> products = new Vector<>();
            products.add("Select Product");
            while (rs.next()) {
                products.add(rs.getString("product_name"));
            }
            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(products);
            productNameCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading products: " + e.getMessage());
        }
    }

    private void loadSupplierCombo() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT suppliers_id, suppliers_name FROM suppliers");
            Vector<String> suppliers = new Vector<>();
            suppliers.add("Select Supplier");
            while (rs.next()) {
                suppliers.add(rs.getString("suppliers_name"));
            }
            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(suppliers);
            SupplierCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading suppliers: " + e.getMessage());
        }
    }

    private void generateBatchNumber() {
        String batchNumber;
        do {
            // Generate 3 capital letters
            String letters = "";
            for (int i = 0; i < 3; i++) {
                letters += (char) ('A' + (int) (Math.random() * 26));
            }

            // Generate exactly 10 numbers (total 13 characters: 3 letters + 10 numbers)
            String numbers = "";
            for (int i = 0; i < 10; i++) {
                numbers += (int) (Math.random() * 10);
            }

            batchNumber = letters + numbers;
        } while (generatedBatchNumbers.contains(batchNumber) || isBatchNumberExists(batchNumber));

        generatedBatchNumbers.add(batchNumber);
        batchNoInput.setText(batchNumber);
    }

    private boolean isBatchNumberExists(String batchNo) {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT batch_no FROM stock WHERE batch_no = '" + batchNo + "'");
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    private void setupFocusTraversal() {
        // Create focus traversal order based on the image layout
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(
                productNameCombo,
                addNewProduct,
                SupplierCombo,
                addNewSupplier,
                purchasePrice,
                lastPrice,
                sellingPrice,
                batchNoInput,
                quantityInput,
                manufactureDate.getDateEditor().getUiComponent(),
                expriyDate.getDateEditor().getUiComponent(),
                saveBtn,
                clearFormBtn,
                cancelBtn
        );

        // Set focus traversal keys
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)));

        // Add comprehensive key navigation
        setupArrowKeyNavigation();
        addEnterKeyNavigation();
    }

    private void setupArrowKeyNavigation() {
        // Add arrow key navigation to all components
        productNameCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, productNameCombo);
            }
        });

        addNewProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, addNewProduct);
            }
        });

        SupplierCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, SupplierCombo);
            }
        });

        addNewSupplier.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, addNewSupplier);
            }
        });

        purchasePrice.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, purchasePrice);
            }
        });

        lastPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, lastPrice);
            }
        });

        sellingPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, sellingPrice);
            }
        });

        batchNoInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, batchNoInput);
            }
        });

        quantityInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, quantityInput);
            }
        });

        manufactureDate.getDateEditor().getUiComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, manufactureDate.getDateEditor().getUiComponent());
            }
        });

        expriyDate.getDateEditor().getUiComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, expriyDate.getDateEditor().getUiComponent());
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
        if (source == productNameCombo) {
            addNewProduct.requestFocusInWindow();
        } else if (source == addNewProduct) {
            SupplierCombo.requestFocusInWindow();
        } else if (source == SupplierCombo) {
            addNewSupplier.requestFocusInWindow();
        } else if (source == addNewSupplier) {
            purchasePrice.requestFocusInWindow();
        } else if (source == purchasePrice) {
            lastPrice.requestFocusInWindow();
        } else if (source == lastPrice) {
            sellingPrice.requestFocusInWindow();
        } else if (source == sellingPrice) {
            batchNoInput.requestFocusInWindow();
        } else if (source == batchNoInput) {
            quantityInput.requestFocusInWindow();
        } else if (source == quantityInput) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            productNameCombo.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == productNameCombo) {
            cancelBtn.requestFocusInWindow();
        } else if (source == addNewProduct) {
            productNameCombo.requestFocusInWindow();
        } else if (source == SupplierCombo) {
            addNewProduct.requestFocusInWindow();
        } else if (source == addNewSupplier) {
            SupplierCombo.requestFocusInWindow();
        } else if (source == purchasePrice) {
            addNewSupplier.requestFocusInWindow();
        } else if (source == lastPrice) {
            purchasePrice.requestFocusInWindow();
        } else if (source == sellingPrice) {
            lastPrice.requestFocusInWindow();
        } else if (source == batchNoInput) {
            sellingPrice.requestFocusInWindow();
        } else if (source == quantityInput) {
            batchNoInput.requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            quantityInput.requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == saveBtn) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == productNameCombo) {
            SupplierCombo.requestFocusInWindow();
        } else if (source == addNewProduct) {
            purchasePrice.requestFocusInWindow();
        } else if (source == SupplierCombo) {
            purchasePrice.requestFocusInWindow();
        } else if (source == addNewSupplier) {
            purchasePrice.requestFocusInWindow();
        } else if (source == purchasePrice) {
            sellingPrice.requestFocusInWindow();
        } else if (source == lastPrice) {
            sellingPrice.requestFocusInWindow();
        } else if (source == sellingPrice) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == batchNoInput) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == quantityInput) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            productNameCombo.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == productNameCombo) {
            cancelBtn.requestFocusInWindow();
        } else if (source == addNewProduct) {
            productNameCombo.requestFocusInWindow();
        } else if (source == SupplierCombo) {
            productNameCombo.requestFocusInWindow();
        } else if (source == addNewSupplier) {
            productNameCombo.requestFocusInWindow();
        } else if (source == purchasePrice) {
            SupplierCombo.requestFocusInWindow();
        } else if (source == lastPrice) {
            purchasePrice.requestFocusInWindow();
        } else if (source == sellingPrice) {
            purchasePrice.requestFocusInWindow();
        } else if (source == batchNoInput) {
            sellingPrice.requestFocusInWindow();
        } else if (source == quantityInput) {
            batchNoInput.requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            sellingPrice.requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == saveBtn) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void addEnterKeyNavigation() {
        // Map components to their next focus targets for Enter key
        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(productNameCombo, addNewProduct);
        enterNavigationMap.put(addNewProduct, SupplierCombo);
        enterNavigationMap.put(SupplierCombo, addNewSupplier);
        enterNavigationMap.put(addNewSupplier, purchasePrice);
        enterNavigationMap.put(purchasePrice, lastPrice);
        enterNavigationMap.put(lastPrice, sellingPrice);
        enterNavigationMap.put(sellingPrice, batchNoInput);
        enterNavigationMap.put(batchNoInput, quantityInput);
        enterNavigationMap.put(quantityInput, manufactureDate.getDateEditor().getUiComponent());
        enterNavigationMap.put(manufactureDate.getDateEditor().getUiComponent(), expriyDate.getDateEditor().getUiComponent());
        enterNavigationMap.put(expriyDate.getDateEditor().getUiComponent(), saveBtn);
        enterNavigationMap.put(saveBtn, clearFormBtn);
        enterNavigationMap.put(clearFormBtn, cancelBtn);
        enterNavigationMap.put(cancelBtn, productNameCombo);

        // Add key listeners to all components
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
        // Validate product selection
        if (productNameCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a product");
            productNameCombo.requestFocus();
            return false;
        }

        // Validate supplier selection
        if (SupplierCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select a supplier");
            SupplierCombo.requestFocus();
            return false;
        }

        // Validate purchase price
        try {
            double purchasePriceValue = Double.parseDouble(purchasePrice.getText().trim());
            if (purchasePriceValue <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Purchase price must be greater than 0");
                purchasePrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid purchase price");
            purchasePrice.requestFocus();
            return false;
        }

        // Validate selling price
        try {
            double sellingPriceValue = Double.parseDouble(sellingPrice.getText().trim());
            if (sellingPriceValue <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Selling price must be greater than 0");
                sellingPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid selling price");
            sellingPrice.requestFocus();
            return false;
        }

        // Validate quantity
        try {
            int quantity = Integer.parseInt(quantityInput.getText().trim());
            if (quantity <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Quantity must be greater than 0");
                quantityInput.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please enter a valid quantity");
            quantityInput.requestFocus();
            return false;
        }

        // Validate dates
        if (manufactureDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select manufacture date");
            manufactureDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        if (expriyDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Please select expiry date");
            expriyDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        // Validate expiry date is after manufacture date
        if (expriyDate.getDate().before(manufactureDate.getDate())) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Expiry date must be after manufacture date");
            expriyDate.getDateEditor().getUiComponent().requestFocus();
            return false;
        }

        return true;
    }

    private int getProductId(String productName) {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT product_id FROM product WHERE product_name = '" + productName + "'");
            if (rs.next()) {
                return rs.getInt("product_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getSupplierId(String supplierName) {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT suppliers_id FROM suppliers WHERE suppliers_name = '" + supplierName + "'");
            if (rs.next()) {
                return rs.getInt("suppliers_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void saveStock() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Get IDs from names
            int productId = getProductId(productNameCombo.getSelectedItem().toString());
            int supplierId = getSupplierId(SupplierCombo.getSelectedItem().toString());

            if (productId == -1 || supplierId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Invalid product or supplier selected");
                return;
            }

            // Format dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String manufactureDateStr = dateFormat.format(manufactureDate.getDate());
            String expiryDateStr = dateFormat.format(expriyDate.getDate());

            // Get current date and time for stock added timestamp
            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = datetimeFormat.format(new Date());

            // Get values
            String batchNo = batchNoInput.getText().trim();
            int quantity = Integer.parseInt(quantityInput.getText().trim());
            double sellingPriceValue = Double.parseDouble(sellingPrice.getText().trim());
            double purchasePriceValue = Double.parseDouble(purchasePrice.getText().trim());
            double lastPriceValue = lastPrice.getText().trim().isEmpty() ? 0 : Double.parseDouble(lastPrice.getText().trim());

            // Create SQL query with added_date_time field
            String query = String.format(
                    "INSERT INTO stock (batch_no, expriy_date, manufacture_date, qty, selling_price, last_price, purchase_price, product_id, suppliers_id, date_time) "
                    + "VALUES ('%s', '%s', '%s', %d, %.2f, %.2f, %.2f, %d, %d, '%s')",
                    batchNo, expiryDateStr, manufactureDateStr, quantity, sellingPriceValue, lastPriceValue, purchasePriceValue, productId, supplierId, currentDateTime
            );

            // Execute query
            MySQL.executeIUD(query);

            clearForm();
            generateBatchNumber();
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Stock added successfully!");
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving stock: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        productNameCombo.setSelectedIndex(0);
        SupplierCombo.setSelectedIndex(0);
        purchasePrice.setText("");
        lastPrice.setText("");
        sellingPrice.setText("");
        quantityInput.setText("1");
        manufactureDate.setDate(null);
        expriyDate.setDate(null);
        productNameCombo.requestFocus();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        purchasePrice = new javax.swing.JTextField();
        lastPrice = new javax.swing.JTextField();
        sellingPrice = new javax.swing.JTextField();
        manufactureDate = new com.toedter.calendar.JDateChooser();
        expriyDate = new com.toedter.calendar.JDateChooser();
        SupplierCombo = new javax.swing.JComboBox<>();
        cancelBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        batchNoInput = new javax.swing.JTextField();
        productNameCombo = new javax.swing.JComboBox<>();
        addNewSupplier = new javax.swing.JButton();
        addNewProduct = new javax.swing.JButton();
        quantityInput = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New Stock");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(8, 147, 176));
        jLabel1.setText("Add New Stock");

        jSeparator1.setForeground(new java.awt.Color(0, 137, 176));

        purchasePrice.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        purchasePrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Purchase Price", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        purchasePrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                purchasePriceKeyPressed(evt);
            }
        });

        lastPrice.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        lastPrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Last Price", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        lastPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lastPriceKeyPressed(evt);
            }
        });

        sellingPrice.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        sellingPrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Selling Price", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        sellingPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sellingPriceKeyPressed(evt);
            }
        });

        manufactureDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manufacture Date", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        manufactureDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        manufactureDate.setOpaque(false);
        manufactureDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                manufactureDateKeyPressed(evt);
            }
        });

        expriyDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Expriy Date", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        expriyDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        expriyDate.setOpaque(false);
        expriyDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                expriyDateKeyPressed(evt);
            }
        });

        SupplierCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        SupplierCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        SupplierCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Supplier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        SupplierCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SupplierComboActionPerformed(evt);
            }
        });
        SupplierCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SupplierComboKeyPressed(evt);
            }
        });

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setForeground(new java.awt.Color(8, 147, 176));
        cancelBtn.setText(" Cancel");
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
        saveBtn.setText(" Save");
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

        clearFormBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        clearFormBtn.setForeground(new java.awt.Color(8, 147, 176));
        clearFormBtn.setText(" Clear Form");
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

        batchNoInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        batchNoInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Batch No", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        batchNoInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                batchNoInputKeyPressed(evt);
            }
        });

        productNameCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        productNameCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        productNameCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Products", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        productNameCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productNameComboActionPerformed(evt);
            }
        });
        productNameCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                productNameComboKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                productNameComboKeyReleased(evt);
            }
        });

        addNewSupplier.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewSupplier.setForeground(new java.awt.Color(102, 102, 102));
        addNewSupplier.setBorder(null);
        addNewSupplier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addNewSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewSupplierActionPerformed(evt);
            }
        });
        addNewSupplier.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                addNewSupplierKeyPressed(evt);
            }
        });

        addNewProduct.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewProduct.setForeground(new java.awt.Color(153, 153, 153));
        addNewProduct.setBorder(null);
        addNewProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addNewProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewProductActionPerformed(evt);
            }
        });
        addNewProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                addNewProductKeyPressed(evt);
            }
        });

        quantityInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        quantityInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quantity", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        quantityInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                quantityInputKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(productNameCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addNewProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(SupplierCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(addNewSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                            .addComponent(purchasePrice, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(lastPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(sellingPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(batchNoInput, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(quantityInput, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(productNameCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(addNewProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SupplierCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(addNewSupplier, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(purchasePrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sellingPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchNoInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quantityInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
        generateBatchNumber();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        saveStock();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();

    }//GEN-LAST:event_cancelBtnActionPerformed

    private void productNameComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productNameComboActionPerformed
        if (productNameCombo.getSelectedIndex() > 0 && !productNameCombo.isPopupVisible()) {
            addNewProduct.requestFocusInWindow();
        }
    }//GEN-LAST:event_productNameComboActionPerformed

    private void productNameComboKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productNameComboKeyReleased

    }//GEN-LAST:event_productNameComboKeyReleased

    private void SupplierComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SupplierComboActionPerformed
        if (SupplierCombo.getSelectedIndex() > 0 && !SupplierCombo.isPopupVisible()) {
            addNewSupplier.requestFocusInWindow();
        }
    }//GEN-LAST:event_SupplierComboActionPerformed

    private void addNewSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewSupplierActionPerformed
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        AddSupplier dialog = new AddSupplier(parentFrame, true);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        loadSupplierCombo();

    }//GEN-LAST:event_addNewSupplierActionPerformed

    private void addNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewProductActionPerformed
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        AddNewProduct dialog = new AddNewProduct(parentFrame, true);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        loadProductCombo();
    }//GEN-LAST:event_addNewProductActionPerformed

    private void purchasePriceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_purchasePriceKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            lastPrice.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, purchasePrice);
        }
    }//GEN-LAST:event_purchasePriceKeyPressed

    private void lastPriceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lastPriceKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            sellingPrice.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, lastPrice);
        }
    }//GEN-LAST:event_lastPriceKeyPressed

    private void sellingPriceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sellingPriceKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            batchNoInput.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, sellingPrice);
        }
    }//GEN-LAST:event_sellingPriceKeyPressed

    private void batchNoInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_batchNoInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            quantityInput.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_F2 || evt.getKeyCode() == KeyEvent.VK_SPACE) {
            generateBatchNumber();
        } else {
            handleArrowNavigation(evt, batchNoInput);
        }
    }//GEN-LAST:event_batchNoInputKeyPressed

    private void manufactureDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manufactureDateKeyPressed
        handleArrowNavigation(evt, manufactureDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_manufactureDateKeyPressed

    private void expriyDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_expriyDateKeyPressed
        handleArrowNavigation(evt, expriyDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_expriyDateKeyPressed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveStock();
        } else {
            handleArrowNavigation(evt, saveBtn);
        }
    }//GEN-LAST:event_saveBtnKeyPressed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
            generateBatchNumber();
        } else {
            handleArrowNavigation(evt, clearFormBtn);
        }
    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.dispose();
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.dispose();
        } else {
            handleArrowNavigation(evt, cancelBtn);
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void SupplierComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SupplierComboKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (SupplierCombo.isPopupVisible()) {
                SupplierCombo.setPopupVisible(false);
            }
            addNewSupplier.requestFocusInWindow();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!SupplierCombo.isPopupVisible()) {
                SupplierCombo.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!SupplierCombo.isPopupVisible()) {
                addNewProduct.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, SupplierCombo);
        }
    }//GEN-LAST:event_SupplierComboKeyPressed

    private void productNameComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productNameComboKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (productNameCombo.isPopupVisible()) {
                productNameCombo.setPopupVisible(false);
            }
            addNewProduct.requestFocusInWindow();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!productNameCombo.isPopupVisible()) {
                productNameCombo.showPopup();
                evt.consume();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!productNameCombo.isPopupVisible()) {
                cancelBtn.requestFocusInWindow();
                evt.consume();
            }
        } else {
            handleArrowNavigation(evt, productNameCombo);
        }
    }//GEN-LAST:event_productNameComboKeyPressed

    private void addNewProductKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewProductKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addNewProductActionPerformed(null);
        } else {
            handleArrowNavigation(evt, addNewProduct);
        }
    }//GEN-LAST:event_addNewProductKeyPressed

    private void addNewSupplierKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewSupplierKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addNewSupplierActionPerformed(null);
        } else {
            handleArrowNavigation(evt, addNewSupplier);
        }

    }//GEN-LAST:event_addNewSupplierKeyPressed

    private void quantityInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quantityInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, quantityInput);
        }
    }//GEN-LAST:event_quantityInputKeyPressed

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
            java.util.logging.Logger.getLogger(AddNewStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddNewStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddNewStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddNewStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddNewStock dialog = new AddNewStock(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<String> SupplierCombo;
    private javax.swing.JButton addNewProduct;
    private javax.swing.JButton addNewSupplier;
    private javax.swing.JTextField batchNoInput;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private com.toedter.calendar.JDateChooser expriyDate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField lastPrice;
    private com.toedter.calendar.JDateChooser manufactureDate;
    private javax.swing.JComboBox<String> productNameCombo;
    private javax.swing.JTextField purchasePrice;
    private javax.swing.JTextField quantityInput;
    private javax.swing.JButton saveBtn;
    private javax.swing.JTextField sellingPrice;
    // End of variables declaration//GEN-END:variables
}
