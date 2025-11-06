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
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

// Add barcode printing imports
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 *
 * @author pasin
 */
public class UpdateProduct extends javax.swing.JDialog {

    private int productId;
    private String originalBarcode;

    /**
     * Creates new form UpdateProduct
     */
    public UpdateProduct(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
        AutoCompleteDecorator.decorate(categoryCombo);
        AutoCompleteDecorator.decorate(brandCombo);
    }

    public UpdateProduct(java.awt.Frame parent, boolean modal, int productId) {
        super(parent, modal);
        this.productId = productId;
        initComponents();
        initializeDialog();
        AutoCompleteDecorator.decorate(categoryCombo);
        AutoCompleteDecorator.decorate(brandCombo);
        loadProductData();
    }

    private void loadProductData() {
        try {
            String query = "SELECT p.*, c.category_name, b.brand_name "
                    + "FROM product p "
                    + "LEFT JOIN category c ON p.category_id = c.category_id "
                    + "LEFT JOIN brand b ON p.brand_id = b.brand_id "
                    + "WHERE p.product_id = " + productId;

            ResultSet rs = MySQL.executeSearch(query);
            if (rs.next()) {
                productInput.setText(rs.getString("product_name"));
                originalBarcode = rs.getString("barcode");
                barcodeInput.setText(originalBarcode);

                // Set category
                String categoryName = rs.getString("category_name");
                for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                    if (categoryCombo.getItemAt(i).equals(categoryName)) {
                        categoryCombo.setSelectedIndex(i);
                        break;
                    }
                }

                // Set brand
                String brandName = rs.getString("brand_name");
                for (int i = 0; i < brandCombo.getItemCount(); i++) {
                    if (brandCombo.getItemAt(i).equals(brandName)) {
                        brandCombo.setSelectedIndex(i);
                        break;
                    }
                }

                // Disable barcode field - CANNOT BE CHANGED
                barcodeInput.setEnabled(false);
                barcodeInput.setBackground(new Color(240, 240, 240));
                barcodeInput.setToolTipText("Barcode cannot be changed for existing products");
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Product not found!");
                this.dispose();
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading product data: " + e.getMessage());
            this.dispose();
        }
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setTitle("Update Product");
        loadCategoryCombo();
        loadBrandCombo();
        setupFocusTraversal();
        setupArrowKeyNavigation();
        addEnterKeyNavigation();
        setupButtonStyles();

        categoryCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                categoryCombo.showPopup();
            }
        });

        brandCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                brandCombo.showPopup();
            }
        });

        // Add F1 and F2 shortcuts for adding category and brand
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openAddNewCategory();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openAddNewBrand();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Add F3 shortcut for printing barcode
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                printBarcode();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Setup tooltips
        productInput.setToolTipText("Type product name and press ENTER to move to next field");
        categoryCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to brand<br>Press <b>F1</b> to add new category</html>");
        brandCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to barcode<br>Press <b>F2</b> to add new brand</html>");
        barcodeInput.setToolTipText("<html>Barcode cannot be changed for existing products<br>Press <b>F3</b> to print barcode</html>");
        addNewCategory.setToolTipText("Click to add new category (or press F1)");
        addNewBrand.setToolTipText("Click to add new brand (or press F2)");
        printBarcode.setToolTipText("Click to print barcode (or press F3)");

        productInput.requestFocus();
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupFocusTraversal() {
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(
                productInput,
                categoryCombo,
                brandCombo,
                barcodeInput,
                cancelBtn,
                clearFormBtn,
                updateBtn
        );

        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)));
    }

    private void setupArrowKeyNavigation() {
        // Add arrow key navigation to all components
        productInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, productInput);
            }
        });

        categoryCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, categoryCombo);
            }
        });

        brandCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, brandCombo);
            }
        });

        barcodeInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, barcodeInput);
            }
        });

        updateBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveProduct();
                } else {
                    handleArrowNavigation(evt, updateBtn);
                }
            }
        });

        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearForm();
                } else {
                    handleArrowNavigation(evt, clearFormBtn);
                }
            }
        });

        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                } else {
                    handleArrowNavigation(evt, cancelBtn);
                }
            }
        });

        addNewCategory.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    openAddNewCategory();
                } else {
                    handleArrowNavigation(evt, addNewCategory);
                }
            }
        });

        addNewBrand.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    openAddNewBrand();
                } else {
                    handleArrowNavigation(evt, addNewBrand);
                }
            }
        });

        printBarcode.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    printBarcode();
                } else {
                    handleArrowNavigation(evt, printBarcode);
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
        if (source == productInput) {
            categoryCombo.requestFocusInWindow();
        } else if (source == categoryCombo) {
            addNewCategory.requestFocusInWindow();
        } else if (source == addNewCategory) {
            brandCombo.requestFocusInWindow();
        } else if (source == brandCombo) {
            addNewBrand.requestFocusInWindow();
        } else if (source == addNewBrand) {
            barcodeInput.requestFocusInWindow();
        } else if (source == barcodeInput) {
            printBarcode.requestFocusInWindow();
        } else if (source == printBarcode) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            updateBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            productInput.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == productInput) {
            updateBtn.requestFocusInWindow();
        } else if (source == categoryCombo) {
            productInput.requestFocusInWindow();
        } else if (source == addNewCategory) {
            categoryCombo.requestFocusInWindow();
        } else if (source == brandCombo) {
            addNewCategory.requestFocusInWindow();
        } else if (source == addNewBrand) {
            brandCombo.requestFocusInWindow();
        } else if (source == barcodeInput) {
            addNewBrand.requestFocusInWindow();
        } else if (source == printBarcode) {
            barcodeInput.requestFocusInWindow();
        } else if (source == cancelBtn) {
            printBarcode.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == productInput) {
            categoryCombo.requestFocusInWindow();
        } else if (source == categoryCombo) {
            brandCombo.requestFocusInWindow();
        } else if (source == addNewCategory) {
            brandCombo.requestFocusInWindow();
        } else if (source == brandCombo) {
            barcodeInput.requestFocusInWindow();
        } else if (source == addNewBrand) {
            barcodeInput.requestFocusInWindow();
        } else if (source == barcodeInput) {
            cancelBtn.requestFocusInWindow();
        } else if (source == printBarcode) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            updateBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            productInput.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == productInput) {
            updateBtn.requestFocusInWindow();
        } else if (source == categoryCombo) {
            productInput.requestFocusInWindow();
        } else if (source == addNewCategory) {
            productInput.requestFocusInWindow();
        } else if (source == brandCombo) {
            categoryCombo.requestFocusInWindow();
        } else if (source == addNewBrand) {
            categoryCombo.requestFocusInWindow();
        } else if (source == barcodeInput) {
            brandCombo.requestFocusInWindow();
        } else if (source == printBarcode) {
            brandCombo.requestFocusInWindow();
        } else if (source == cancelBtn) {
            barcodeInput.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void addEnterKeyNavigation() {
        // Map components to their next focus targets for Enter key
        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(productInput, categoryCombo);
        enterNavigationMap.put(categoryCombo, brandCombo);
        enterNavigationMap.put(brandCombo, barcodeInput);
        enterNavigationMap.put(barcodeInput, updateBtn); // Changed to go directly to update button
        enterNavigationMap.put(cancelBtn, clearFormBtn);
        enterNavigationMap.put(clearFormBtn, updateBtn);
        enterNavigationMap.put(updateBtn, productInput);
        enterNavigationMap.put(addNewCategory, brandCombo);
        enterNavigationMap.put(addNewBrand, barcodeInput);
        enterNavigationMap.put(printBarcode, cancelBtn);

        // Add key listeners to all components
        for (java.awt.Component component : enterNavigationMap.keySet()) {
            component.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        // Special handling for combo boxes when popup is visible
                        if (component == categoryCombo && categoryCombo.isPopupVisible()) {
                            categoryCombo.setPopupVisible(false);
                            evt.consume();
                            return;
                        }
                        if (component == brandCombo && brandCombo.isPopupVisible()) {
                            brandCombo.setPopupVisible(false);
                            evt.consume();
                            return;
                        }
                        
                        java.awt.Component nextComponent = enterNavigationMap.get(component);
                        if (nextComponent != null) {
                            nextComponent.requestFocusInWindow();
                            
                            // Special case: if we're moving from barcode to update button and form is valid, auto-save
                            if (component == barcodeInput && nextComponent == updateBtn && validateForm()) {
                                saveProduct();
                            }
                        }
                        evt.consume();
                    }
                }
            });
        }
    }

    // ---------------- BUTTON STYLES AND EFFECTS ----------------
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
        // Setup icon buttons
        addNewCategory.setBorderPainted(false);
        addNewCategory.setContentAreaFilled(false);
        addNewCategory.setFocusPainted(false);
        addNewCategory.setOpaque(false);
        addNewCategory.setFocusable(true);

        addNewBrand.setBorderPainted(false);
        addNewBrand.setContentAreaFilled(false);
        addNewBrand.setFocusPainted(false);
        addNewBrand.setOpaque(false);
        addNewBrand.setFocusable(true);

        printBarcode.setBorderPainted(false);
        printBarcode.setContentAreaFilled(false);
        printBarcode.setFocusPainted(false);
        printBarcode.setOpaque(false);
        printBarcode.setFocusable(true);

        // Setup icons for add buttons
        FlatSVGIcon categoryIcon = new FlatSVGIcon("lk/com/pos/icon/category.svg", 25, 25);
        categoryIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewCategory.setIcon(categoryIcon);

        FlatSVGIcon brandIcon = new FlatSVGIcon("lk/com/pos/icon/add-brand.svg", 25, 25);
        brandIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewBrand.setIcon(brandIcon);

        FlatSVGIcon printerIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 25, 25);
        printerIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        printBarcode.setIcon(printerIcon);

        // Setup gradient buttons
        setupGradientButton(updateBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        updateBtn.setIcon(updateIcon);

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
        // Mouse listeners for updateBtn
        updateBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                updateBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                updateBtn.setIcon(hoverIcon);
                updateBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                updateBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                updateBtn.setIcon(normalIcon);
                updateBtn.repaint();
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

        // Mouse listeners for addNewCategory
        addNewCategory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/category.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addNewCategory.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/category.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                addNewCategory.setIcon(normalIcon);
            }
        });

        // Mouse listeners for addNewBrand
        addNewBrand.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add-brand.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addNewBrand.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add-brand.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                addNewBrand.setIcon(normalIcon);
            }
        });

        // Mouse listeners for printBarcode
        printBarcode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                printBarcode.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                printBarcode.setIcon(normalIcon);
            }
        });
    }

    private void setupButtonFocusListeners() {
        // Focus listeners for updateBtn
        updateBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                updateBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                updateBtn.setIcon(focusedIcon);
                updateBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                updateBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                updateBtn.setIcon(normalIcon);
                updateBtn.repaint();
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

        // Focus listeners for add buttons
        addNewCategory.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/category.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addNewCategory.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/category.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                addNewCategory.setIcon(normalIcon);
            }
        });

        addNewBrand.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add-brand.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addNewBrand.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add-brand.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                addNewBrand.setIcon(normalIcon);
            }
        });

        printBarcode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                printBarcode.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                printBarcode.setIcon(normalIcon);
            }
        });
    }

    // ---------------- DATABASE OPERATIONS ----------------
    private void loadCategoryCombo() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT * FROM category");
            Vector<String> categories = new Vector<>();
            categories.add("Select Category");
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(categories);
            categoryCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading categories: " + e.getMessage());
        }
    }

    private void loadBrandCombo() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT * FROM brand");
            Vector<String> brands = new Vector<>();
            brands.add("Select Brand");
            while (rs.next()) {
                brands.add(rs.getString("brand_name"));
            }
            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(brands);
            brandCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading brands: " + e.getMessage());
        }
    }

    private void printBarcode() {
        String barcodeText = barcodeInput.getText().trim();

        if (barcodeText.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "No barcode available to print");
            return;
        }

        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("Barcode Print - " + barcodeText);

            printerJob.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return Printable.NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Set up fonts
                    Font barcodeFont = new Font("Monospaced", Font.BOLD, 24);
                    Font labelFont = new Font("Nunito SemiBold", Font.PLAIN, 12);

                    // Draw barcode number
                    g2d.setFont(barcodeFont);
                    g2d.drawString(barcodeText, 50, 100);

                    // Draw label
                    g2d.setFont(labelFont);
                    g2d.drawString("Product Barcode", 50, 130);
                    g2d.drawString("Generated by POS System", 50, 145);

                    return Printable.PAGE_EXISTS;
                }
            });

            if (printerJob.printDialog()) {
                printerJob.print();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                        "Barcode sent to printer successfully");
            }
        } catch (PrinterException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Printing error: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (productInput.getText().trim().isEmpty()) {
            productInput.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter product name");
            return false;
        }

        if (categoryCombo.getSelectedIndex() <= 0) {
            categoryCombo.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a category");
            return false;
        }

        if (brandCombo.getSelectedIndex() <= 0) {
            brandCombo.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a brand");
            return false;
        }

        return true;
    }

    private void saveProduct() {
        if (!validateForm()) {
            return;
        }

        try {
            String productName = productInput.getText().trim();

            int brandId = 0;
            ResultSet brandRs = MySQL.executeSearch("SELECT brand_id FROM brand WHERE brand_name = '"
                    + brandCombo.getSelectedItem().toString() + "'");
            if (brandRs.next()) {
                brandId = brandRs.getInt("brand_id");
            }

            // Check if product with same name and brand already exists (excluding current product)
            ResultSet productCheckRs = MySQL.executeSearch(
                    "SELECT product_id FROM product WHERE product_name = '" + productName
                    + "' AND brand_id = " + brandId
                    + " AND product_id != " + productId
            );

            if (productCheckRs.next()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Product '" + productName + "' already exists with this brand!");
                productInput.requestFocus();
                productInput.selectAll();
                return;
            }

            int categoryId = 0;
            ResultSet catRs = MySQL.executeSearch("SELECT category_id FROM category WHERE category_name = '"
                    + categoryCombo.getSelectedItem().toString() + "'");
            if (catRs.next()) {
                categoryId = catRs.getInt("category_id");
            }

            String query = "UPDATE product SET "
                    + "product_name = '" + productName + "', "
                    + "brand_id = " + brandId + ", "
                    + "category_id = " + categoryId + " "
                    + "WHERE product_id = " + productId;

            MySQL.executeIUD(query);

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "Product updated successfully!");

            this.dispose();

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error updating product: " + e.getMessage());
        }
    }

    private void clearForm() {
        // Reload original data
        loadProductData();
        productInput.requestFocus();
    }

    private void openAddNewCategory() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddNewCategoryDialog dialog = new AddNewCategoryDialog(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            loadCategoryCombo();
            categoryCombo.requestFocus();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening category dialog: " + e.getMessage());
        }
    }

    private void openAddNewBrand() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddNewBrandDialog dialog = new AddNewBrandDialog(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            loadBrandCombo();
            brandCombo.requestFocus();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening brand dialog: " + e.getMessage());
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        categoryCombo = new javax.swing.JComboBox<>();
        brandCombo = new javax.swing.JComboBox<>();
        barcodeInput = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        updateBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        printBarcode = new javax.swing.JButton();
        productInput = new javax.swing.JTextField();
        addNewCategory = new javax.swing.JButton();
        addNewBrand = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Update Product");

        jSeparator3.setForeground(new java.awt.Color(0, 137, 176));

        categoryCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        categoryCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        categoryCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Category *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        categoryCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                categoryComboKeyPressed(evt);
            }
        });

        brandCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        brandCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        brandCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Brand *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        brandCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                brandComboKeyPressed(evt);
            }
        });

        barcodeInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        barcodeInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Barcode *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        barcodeInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barcodeInputActionPerformed(evt);
            }
        });
        barcodeInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                barcodeInputKeyPressed(evt);
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

        updateBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        updateBtn.setForeground(new java.awt.Color(8, 147, 176));
        updateBtn.setText("Update");
        updateBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });
        updateBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                updateBtnKeyPressed(evt);
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

        printBarcode.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        printBarcode.setMaximumSize(new java.awt.Dimension(75, 7));
        printBarcode.setMinimumSize(new java.awt.Dimension(75, 7));
        printBarcode.setPreferredSize(new java.awt.Dimension(75, 7));
        printBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printBarcodeActionPerformed(evt);
            }
        });
        printBarcode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                printBarcodeKeyPressed(evt);
            }
        });

        productInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        productInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Product Name *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        productInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productInputActionPerformed(evt);
            }
        });
        productInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                productInputKeyPressed(evt);
            }
        });

        addNewCategory.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewCategoryActionPerformed(evt);
            }
        });
        addNewCategory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                addNewCategoryKeyPressed(evt);
            }
        });

        addNewBrand.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewBrand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewBrandActionPerformed(evt);
            }
        });
        addNewBrand.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                addNewBrandKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(112, 112, 112))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(addNewCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(brandCombo, 0, 230, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(addNewBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jSeparator3)
                                .addComponent(jLabel3)
                                .addComponent(productInput))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(productInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(addNewCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(brandCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewBrand, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 556, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addNewBrandKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewBrandKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addNewBrandActionPerformed(null);
        } else {
            handleArrowNavigation(evt, addNewBrand);
        }

    }//GEN-LAST:event_addNewBrandKeyPressed

    private void addNewBrandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewBrandActionPerformed
        openAddNewBrand();
    }//GEN-LAST:event_addNewBrandActionPerformed

    private void addNewCategoryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewCategoryKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addNewCategoryActionPerformed(null);
        } else {
            handleArrowNavigation(evt, addNewCategory);
        }
    }//GEN-LAST:event_addNewCategoryKeyPressed

    private void addNewCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCategoryActionPerformed
        openAddNewCategory();
    }//GEN-LAST:event_addNewCategoryActionPerformed

    private void productInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            categoryCombo.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, productInput);
        }
    }//GEN-LAST:event_productInputKeyPressed

    private void productInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productInputActionPerformed
        categoryCombo.requestFocus();
    }//GEN-LAST:event_productInputActionPerformed

    private void printBarcodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_printBarcodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            printBarcodeActionPerformed(null);
        } else {
            handleArrowNavigation(evt, printBarcode);
        }
    }//GEN-LAST:event_printBarcodeKeyPressed

    private void printBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBarcodeActionPerformed
    printBarcode();
    }//GEN-LAST:event_printBarcodeActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        } else {
            handleArrowNavigation(evt, clearFormBtn);
        }
    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void updateBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveProduct();
        } else {
            handleArrowNavigation(evt, updateBtn);
        }
    }//GEN-LAST:event_updateBtnKeyPressed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        saveProduct();
    }//GEN-LAST:event_updateBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.dispose();
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.dispose();
        } else {
            handleArrowNavigation(evt, cancelBtn);
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void brandComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_brandComboKeyPressed
if (brandCombo.isPopupVisible()) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                brandCombo.setPopupVisible(false);
                evt.consume();
            }
            return;
        }

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            barcodeInput.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, brandCombo);
        }
    }//GEN-LAST:event_brandComboKeyPressed

    private void categoryComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_categoryComboKeyPressed
        if (categoryCombo.isPopupVisible()) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                categoryCombo.setPopupVisible(false);
                evt.consume();
            }
            return;
        }

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            brandCombo.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, categoryCombo);
        }
    }//GEN-LAST:event_categoryComboKeyPressed

    private void barcodeInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_barcodeInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            updateBtn.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, barcodeInput);
        }
    }//GEN-LAST:event_barcodeInputKeyPressed

    private void barcodeInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeInputActionPerformed
      updateBtn.requestFocus();
    }//GEN-LAST:event_barcodeInputActionPerformed

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
            java.util.logging.Logger.getLogger(UpdateProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateProduct dialog = new UpdateProduct(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton addNewBrand;
    private javax.swing.JButton addNewCategory;
    private javax.swing.JTextField barcodeInput;
    private javax.swing.JComboBox<String> brandCombo;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JComboBox<String> categoryCombo;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton printBarcode;
    private javax.swing.JTextField productInput;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
