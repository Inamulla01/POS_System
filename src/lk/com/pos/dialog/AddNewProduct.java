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

public class AddNewProduct extends javax.swing.JDialog {

    public AddNewProduct(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
        AutoCompleteDecorator.decorate(categoryCombo);
        AutoCompleteDecorator.decorate(brandCombo);
    }

private void addEnterKeyNavigation() {
    // Map components to their next focus targets for Enter key
    java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
    enterNavigationMap.put(productInput, categoryCombo);
    
    // For combo boxes, we'll handle Enter key in their specific keyPressed methods
    // to account for selection state
    
    enterNavigationMap.put(addNewCategory, brandCombo);
    enterNavigationMap.put(addNewBrand, barcodeInput);
    enterNavigationMap.put(barcodeInput, genarateBarecode);
    enterNavigationMap.put(genarateBarecode, printBarcode);
    enterNavigationMap.put(printBarcode, saveBtn);
    enterNavigationMap.put(saveBtn, clearFormBtn);
    enterNavigationMap.put(clearFormBtn, cancelBtn);
    enterNavigationMap.put(cancelBtn, productInput);

    // Add key listeners to all components except combo boxes
    for (java.awt.Component component : enterNavigationMap.keySet()) {
        // Skip combo boxes as they have their own Enter key handling
        if (component != categoryCombo && component != brandCombo) {
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
}
    private void initializeDialog() {
        // Center the dialog
        setLocationRelativeTo(getParent());

        // Load combo box data
        loadCategoryCombo();
        loadBrandCombo();

        // Set focus traversal
        setupFocusTraversal();

        // Add mouse listeners to combo boxes
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

        // Add action listeners to combo boxes
        categoryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryComboActionPerformed(evt);
            }
        });

        brandCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brandComboActionPerformed(evt);
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

        // Setup tooltips
        categoryCombo.setToolTipText("Use DOWN arrow to open dropdown, ENTER to select and move to next field");
        brandCombo.setToolTipText("Use DOWN arrow to open dropdown, ENTER to select and move to next field");
        productInput.setToolTipText("Type product name and press ENTER to move to next field");
        barcodeInput.setToolTipText("Type barcode and press ENTER to move to next field");
        addNewCategory.setToolTipText("Press ENTER to add new category, RIGHT arrow to go to add brand");
        addNewBrand.setToolTipText("Press ENTER to add new brand, LEFT arrow to go to add category");
        genarateBarecode.setToolTipText("Press ENTER to generate barcode, RIGHT arrow to go to print barcode");
        printBarcode.setToolTipText("Press ENTER to print barcode, LEFT arrow to go to generate barcode");

        // Setup button styles with proper focus and hover effects
        setupButtonStyles();

        // Set initial focus
        productInput.requestFocus();
    }

    private void categoryComboActionPerformed(java.awt.event.ActionEvent evt) {
        // Auto-move to next field when an item is selected (but not the placeholder)
        if (categoryCombo.getSelectedIndex() > 0 && !categoryCombo.isPopupVisible()) {
            addNewCategory.requestFocusInWindow();
        }
    }

    private void brandComboActionPerformed(java.awt.event.ActionEvent evt) {
        // Auto-move to next field when an item is selected (but not the placeholder)
        if (brandCombo.getSelectedIndex() > 0 && !brandCombo.isPopupVisible()) {
            addNewBrand.requestFocusInWindow();
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
        // Remove borders, backgrounds, and set hover effects for add buttons
        addNewCategory.setBorderPainted(false);
        addNewCategory.setContentAreaFilled(false);
        addNewCategory.setFocusPainted(false);
        addNewCategory.setOpaque(false);

        addNewBrand.setBorderPainted(false);
        addNewBrand.setContentAreaFilled(false);
        addNewBrand.setFocusPainted(false);
        addNewBrand.setOpaque(false);

        genarateBarecode.setBorderPainted(false);
        genarateBarecode.setContentAreaFilled(false);
        genarateBarecode.setFocusPainted(false);
        genarateBarecode.setOpaque(false);

        printBarcode.setBorderPainted(false);
        printBarcode.setContentAreaFilled(false);
        printBarcode.setFocusPainted(false);
        printBarcode.setOpaque(false);

        // Create icons with original gray color for add buttons
        FlatSVGIcon categoryIcon = new FlatSVGIcon("lk/com/pos/icon/category.svg", 25, 25);
        categoryIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewCategory.setIcon(categoryIcon);

        FlatSVGIcon brandIcon = new FlatSVGIcon("lk/com/pos/icon/add-brand.svg", 25, 25);
        brandIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewBrand.setIcon(brandIcon);

        // Create icons for barcode buttons
        FlatSVGIcon barcodeIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 25, 25);
        barcodeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        genarateBarecode.setIcon(barcodeIcon);

        FlatSVGIcon printerIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 25, 25);
        printerIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        printBarcode.setIcon(printerIcon);

        // Setup gradient buttons
        setupGradientButton(saveBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        saveBtn.setIcon(addIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(cancelIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(clearIcon);

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

        // Add mouse listeners for hover effects on add buttons
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

        // Mouse listeners for barcode buttons
        genarateBarecode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                genarateBarecode.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                genarateBarecode.setIcon(normalIcon);
            }
        });

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

        // Focus listeners for barcode buttons
        genarateBarecode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                genarateBarecode.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                genarateBarecode.setIcon(normalIcon);
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

    private void setupFocusTraversal() {
        // Create focus traversal order
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(
                productInput,
                categoryCombo,
                addNewCategory,
                brandCombo,
                addNewBrand,
                barcodeInput,
                genarateBarecode,
                printBarcode,
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

        addNewCategory.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, addNewCategory);
            }
        });

        brandCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, brandCombo);
            }
        });

        addNewBrand.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, addNewBrand);
            }
        });

        barcodeInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, barcodeInput);
            }
        });

        genarateBarecode.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, genarateBarecode);
            }
        });

        printBarcode.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, printBarcode);
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
            genarateBarecode.requestFocusInWindow();
        } else if (source == genarateBarecode) {
            printBarcode.requestFocusInWindow();
        } else if (source == printBarcode) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            productInput.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == productInput) {
            cancelBtn.requestFocusInWindow();
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
        } else if (source == genarateBarecode) {
            barcodeInput.requestFocusInWindow();
        } else if (source == printBarcode) {
            genarateBarecode.requestFocusInWindow();
        } else if (source == saveBtn) {
            printBarcode.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == productInput) {
            categoryCombo.requestFocusInWindow();
        } else if (source == categoryCombo) {
            barcodeInput.requestFocusInWindow();
        } else if (source == addNewCategory) {
            barcodeInput.requestFocusInWindow();
        } else if (source == brandCombo) {
            barcodeInput.requestFocusInWindow();
        } else if (source == addNewBrand) {
            barcodeInput.requestFocusInWindow();
        } else if (source == barcodeInput) {
            saveBtn.requestFocusInWindow();
        } else if (source == genarateBarecode) {
            saveBtn.requestFocusInWindow();
        } else if (source == printBarcode) {
            saveBtn.requestFocusInWindow();
        } else if (source == saveBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            productInput.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == productInput) {
            cancelBtn.requestFocusInWindow();
        } else if (source == categoryCombo) {
            productInput.requestFocusInWindow();
        } else if (source == addNewCategory) {
            productInput.requestFocusInWindow();
        } else if (source == brandCombo) {
            productInput.requestFocusInWindow();
        } else if (source == addNewBrand) {
            productInput.requestFocusInWindow();
        } else if (source == barcodeInput) {
            categoryCombo.requestFocusInWindow();
        } else if (source == genarateBarecode) {
            barcodeInput.requestFocusInWindow();
        } else if (source == printBarcode) {
            barcodeInput.requestFocusInWindow();
        } else if (source == saveBtn) {
            barcodeInput.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            saveBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }


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

    private void generateBarcode() {
        // Generate a simple barcode (you can replace this with your barcode generation logic)
        String barcode = "BC" + System.currentTimeMillis();
        barcodeInput.setText(barcode);
        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                "Barcode generated: " + barcode);
    }

    private void printBarcode() {
        // Add your barcode printing logic here
        if (barcodeInput.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please generate or enter a barcode first");
            barcodeInput.requestFocus();
            return;
        }

        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                "Printing barcode: " + barcodeInput.getText().trim());
        // Add actual printing code here
    }

    private boolean validateForm() {
        // Validate Product Name
        if (productInput.getText().trim().isEmpty()) {
            productInput.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter product name");
            return false;
        }

        // Validate Category
        if (categoryCombo.getSelectedIndex() <= 0) {
            categoryCombo.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a category");
            return false;
        }

        // Validate Brand
        if (brandCombo.getSelectedIndex() <= 0) {
            brandCombo.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a brand");
            return false;
        }

        // Validate Barcode
        if (barcodeInput.getText().trim().isEmpty()) {
            barcodeInput.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter barcode");
            return false;
        }

        return true;
    }

    private void saveProduct() {
        if (!validateForm()) {
            return;
        }

        try {
            // Check if product with same name and brand already exists
            String productName = productInput.getText().trim();

            // Get brand_id for the check
            int brandId = 0;
            ResultSet brandRs = MySQL.executeSearch("SELECT brand_id FROM brand WHERE brand_name = '"
                    + brandCombo.getSelectedItem().toString() + "'");
            if (brandRs.next()) {
                brandId = brandRs.getInt("brand_id");
            }

            // Check if product with same name and brand already exists
            ResultSet productCheckRs = MySQL.executeSearch(
                    "SELECT product_id FROM product WHERE product_name = '" + productName
                    + "' AND brand_id = " + brandId
            );

            if (productCheckRs.next()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Product '" + productName + "' already exists with this brand!");
                productInput.requestFocus();
                productInput.selectAll();
                return;
            }

            // Check if barcode already exists
            ResultSet barcodeCheckRs = MySQL.executeSearch("SELECT product_id FROM product WHERE barcode = '"
                    + barcodeInput.getText().trim() + "'");
            if (barcodeCheckRs.next()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Barcode already exists for another product!");
                barcodeInput.requestFocus();
                barcodeInput.selectAll();
                return;
            }

            // Get category_id
            int categoryId = 0;
            ResultSet catRs = MySQL.executeSearch("SELECT category_id FROM category WHERE category_name = '"
                    + categoryCombo.getSelectedItem().toString() + "'");
            if (catRs.next()) {
                categoryId = catRs.getInt("category_id");
            }

            // Default status_id for active product (assuming 1 is active)
            int statusId = 1;

            // Insert into product table
            String query = "INSERT INTO product (product_name, brand_id, category_id, p_status_id, barcode) "
                    + "VALUES ('" + productName + "', " + brandId + ", " + categoryId
                    + ", " + statusId + ", '" + barcodeInput.getText().trim() + "')";

            MySQL.executeIUD(query);

            // Show success message
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "Product added successfully!");

            // Clear form after successful save
            clearForm();

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error saving product: " + e.getMessage());
        }
    }

    private void clearForm() {
        productInput.setText("");
        categoryCombo.setSelectedIndex(0);
        brandCombo.setSelectedIndex(0);
        barcodeInput.setText("");
        productInput.requestFocus();
    }

    private void openAddNewCategory() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddNewCategoryDialog dialog = new AddNewCategoryDialog(parentFrame, true);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            // After closing the category dialog, refresh the combo
            loadCategoryCombo();
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
            // After closing the brand dialog, refresh the combo
            loadBrandCombo();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening brand dialog: " + e.getMessage());
        }
    }

    private void categoryEditorKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // If popup is visible, let it handle the selection first
            if (!categoryCombo.isPopupVisible()) {
                addNewCategory.requestFocusInWindow();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!categoryCombo.isPopupVisible()) {
                categoryCombo.showPopup();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!categoryCombo.isPopupVisible()) {
                productInput.requestFocusInWindow();
            }
        } else {
            handleArrowNavigation(evt, categoryCombo.getEditor().getEditorComponent());
        }
    }

    private void brandEditorKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // If popup is visible, let it handle the selection first
            if (!brandCombo.isPopupVisible()) {
                addNewBrand.requestFocusInWindow();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!brandCombo.isPopupVisible()) {
                brandCombo.showPopup();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!brandCombo.isPopupVisible()) {
                categoryCombo.requestFocusInWindow();
            }
        } else {
            handleArrowNavigation(evt, brandCombo.getEditor().getEditorComponent());
        }
    }

    private void formKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.dispose();
        }
    }

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
        saveBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        printBarcode = new javax.swing.JButton();
        genarateBarecode = new javax.swing.JButton();
        productInput = new javax.swing.JTextField();
        addNewCategory = new javax.swing.JButton();
        addNewBrand = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New Product");

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Add New Product");

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

        genarateBarecode.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        genarateBarecode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genarateBarecodeActionPerformed(evt);
            }
        });
        genarateBarecode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                genarateBarecodeKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                genarateBarecodeKeyReleased(evt);
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
                        .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(genarateBarecode, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(printBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(genarateBarecode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void barcodeInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeInputActionPerformed
        saveBtn.requestFocus();
    }//GEN-LAST:event_barcodeInputActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();

    }//GEN-LAST:event_cancelBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        saveProduct();


    }//GEN-LAST:event_saveBtnActionPerformed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();

    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void productInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productInputActionPerformed
        categoryCombo.requestFocus();

    }//GEN-LAST:event_productInputActionPerformed

    private void addNewCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCategoryActionPerformed
        openAddNewCategory();

    }//GEN-LAST:event_addNewCategoryActionPerformed

    private void addNewBrandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewBrandActionPerformed
        openAddNewBrand();

    }//GEN-LAST:event_addNewBrandActionPerformed

    private void barcodeInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_barcodeInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            genarateBarecode.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, barcodeInput);
        }
    }//GEN-LAST:event_barcodeInputKeyPressed

    private void categoryComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_categoryComboKeyPressed
        // When popup is visible, let the combo box handle navigation
        if (categoryCombo.isPopupVisible()) {
            // Only handle ENTER key when popup is visible
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                // Close the popup and move to next field
                categoryCombo.setPopupVisible(false);
                // Only move to next field if an item is actually selected (not the placeholder)
                if (categoryCombo.getSelectedIndex() > 0) {
                    addNewCategory.requestFocusInWindow();
                }
                evt.consume();
            }
            return;
        }

        // When popup is NOT visible, handle navigation
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                // Only move to next field if an item is selected
                if (categoryCombo.getSelectedIndex() > 0) {
                    addNewCategory.requestFocusInWindow();
                } else {
                    // If no item selected, open the dropdown
                    categoryCombo.showPopup();
                }
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                // Only open combo box if not already open
                if (!categoryCombo.isPopupVisible()) {
                    categoryCombo.showPopup();
                }
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                productInput.requestFocusInWindow();
                evt.consume();
                break;
            case KeyEvent.VK_TAB:
                // Allow normal tab navigation
                break;
            case KeyEvent.VK_SPACE:
                // Open combo box when SPACE is pressed
                if (!categoryCombo.isPopupVisible()) {
                    categoryCombo.showPopup();
                }
                evt.consume();
                break;
            default:
                // For arrow keys, handle navigation
                if (evt.getKeyCode() == KeyEvent.VK_LEFT || evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    handleArrowNavigation(evt, categoryCombo);
                }
                break;
        }
    }//GEN-LAST:event_categoryComboKeyPressed

    private void brandComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_brandComboKeyPressed
        // When popup is visible, let the combo box handle navigation
        if (brandCombo.isPopupVisible()) {
            // Only handle ENTER key when popup is visible
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                // Close the popup and move to next field
                brandCombo.setPopupVisible(false);
                // Only move to next field if an item is actually selected (not the placeholder)
                if (brandCombo.getSelectedIndex() > 0) {
                    addNewBrand.requestFocusInWindow();
                }
                evt.consume();
            }
            return;
        }

        // When popup is NOT visible, handle navigation
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                // Only move to next field if an item is selected
                if (brandCombo.getSelectedIndex() > 0) {
                    addNewBrand.requestFocusInWindow();
                } else {
                    // If no item selected, open the dropdown
                    brandCombo.showPopup();
                }
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                // Only open combo box if not already open
                if (!brandCombo.isPopupVisible()) {
                    brandCombo.showPopup();
                }
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                categoryCombo.requestFocusInWindow();
                evt.consume();
                break;
            case KeyEvent.VK_TAB:
                // Allow normal tab navigation
                break;
            case KeyEvent.VK_SPACE:
                // Open combo box when SPACE is pressed
                if (!brandCombo.isPopupVisible()) {
                    brandCombo.showPopup();
                }
                evt.consume();
                break;
            default:
                // For arrow keys, handle navigation
                if (evt.getKeyCode() == KeyEvent.VK_LEFT || evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    handleArrowNavigation(evt, brandCombo);
                }
                break;
        }
    }//GEN-LAST:event_brandComboKeyPressed

    private void productInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            categoryCombo.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, productInput);
        }
    }//GEN-LAST:event_productInputKeyPressed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveProduct();
        } else {
            handleArrowNavigation(evt, saveBtn);
        }
    }//GEN-LAST:event_saveBtnKeyPressed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
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

    private void genarateBarecodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genarateBarecodeActionPerformed
        generateBarcode();
    }//GEN-LAST:event_genarateBarecodeActionPerformed

    private void printBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBarcodeActionPerformed
        printBarcode();
    }//GEN-LAST:event_printBarcodeActionPerformed

    private void addNewCategoryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewCategoryKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addNewCategoryActionPerformed(null);
        } else {
            handleArrowNavigation(evt, addNewCategory);
        }
    }//GEN-LAST:event_addNewCategoryKeyPressed

    private void addNewBrandKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewBrandKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addNewBrandActionPerformed(null);
        } else {
            handleArrowNavigation(evt, addNewBrand);
        }
    }//GEN-LAST:event_addNewBrandKeyPressed

    private void genarateBarecodeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_genarateBarecodeKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_genarateBarecodeKeyReleased

    private void genarateBarecodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_genarateBarecodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            genarateBarecodeActionPerformed(null);
        } else {
            handleArrowNavigation(evt, genarateBarecode);
        }
    }//GEN-LAST:event_genarateBarecodeKeyPressed

    private void printBarcodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_printBarcodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            printBarcodeActionPerformed(null);
        } else {
            handleArrowNavigation(evt, printBarcode);
        }
    }//GEN-LAST:event_printBarcodeKeyPressed

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
            java.util.logging.Logger.getLogger(AddNewProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddNewProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddNewProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddNewProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddNewProduct dialog = new AddNewProduct(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton genarateBarecode;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton printBarcode;
    private javax.swing.JTextField productInput;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
