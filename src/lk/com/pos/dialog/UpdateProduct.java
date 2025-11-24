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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

// Add barcode printing imports
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import javax.swing.JComboBox;

/**
 *
 * @author moham
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
    }

    public UpdateProduct(java.awt.Frame parent, boolean modal, int productId) {
        super(parent, modal);
        this.productId = productId;
        initComponents();
        initializeDialog();
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
                // Load product name
                productInput.setText(rs.getString("product_name"));

                // Load category
                String categoryName = rs.getString("category_name");
                if (categoryName != null) {
                    for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                        if (categoryCombo.getItemAt(i).equals(categoryName)) {
                            categoryCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }

                // Load brand
                String brandName = rs.getString("brand_name");
                if (brandName != null) {
                    for (int i = 0; i < brandCombo.getItemCount(); i++) {
                        if (brandCombo.getItemAt(i).equals(brandName)) {
                            brandCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }

                // Load barcode
                originalBarcode = rs.getString("barcode");
                barcodeInput.setText(originalBarcode);
            } else {
                this.dispose();
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    private void setupProductInputLimit() {
        // Create a custom document that limits input to 35 characters
        productInput.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if (str == null) {
                    return;
                }

                String currentText = getText(0, getLength());
                String newText = currentText.substring(0, offset) + str + currentText.substring(offset);

                if (newText.length() <= 35) {
                    super.insertString(offset, str, attr);
                } else {
                    // If adding this string would exceed limit, only add what fits
                    int availableChars = 35 - currentText.length();
                    if (availableChars > 0) {
                        super.insertString(offset, str.substring(0, availableChars), attr);
                    }
                }
            }
        });

        // Set tooltip to inform user about the limit
        productInput.setToolTipText("<html>Type product name (max 35 characters) and press ENTER to move to next field</html>");
    }

    private void addEnterKeyNavigation() {
        // Map components to their next focus targets for Enter key
        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(productInput, categoryCombo);
        enterNavigationMap.put(categoryCombo, brandCombo);
        enterNavigationMap.put(brandCombo, barcodeInput);
        // REMOVED barcodeInput from here - it has custom logic
        enterNavigationMap.put(cancelBtn, clearFormBtn);
        enterNavigationMap.put(clearFormBtn, updateBtn);
        enterNavigationMap.put(updateBtn, productInput);

        // Add key listeners to all components except combo boxes and barcodeInput
        for (java.awt.Component component : enterNavigationMap.keySet()) {
            if (component != categoryCombo && component != brandCombo && component != barcodeInput) {
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

    private boolean allFieldsFilled() {
        boolean filled = !productInput.getText().trim().isEmpty()
                && categoryCombo.getSelectedIndex() > 0
                && brandCombo.getSelectedIndex() > 0
                && !barcodeInput.getText().trim().isEmpty();

        return filled;
    }

    private void handleEnterWithAllFieldsFilled(java.awt.event.KeyEvent evt, java.awt.Component source) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            java.awt.Component nextComponent = getNextComponent(source);
            if (nextComponent != null) {
                nextComponent.requestFocusInWindow();
            }
            evt.consume();
        }
    }

    private java.awt.Component getNextComponent(java.awt.Component source) {
        if (source == productInput) {
            return categoryCombo;
        }
        if (source == categoryCombo) {
            return brandCombo;
        }
        if (source == brandCombo) {
            return barcodeInput;
        }
        if (source == barcodeInput) {
            return cancelBtn; // Default navigation
        }
        if (source == cancelBtn) {
            return clearFormBtn;
        }
        if (source == clearFormBtn) {
            return updateBtn;
        }
        if (source == updateBtn) {
            return productInput;
        }
        return null;
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setupProductInputLimit(); // Add character limit for product name
        loadCategoryCombo();
        loadBrandCombo();
        setupFocusTraversal();
        AutoCompleteDecorator.decorate(categoryCombo);
        AutoCompleteDecorator.decorate(brandCombo);
        preventUpArrowPopup(categoryCombo);
        preventUpArrowPopup(brandCombo);

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

        // Add F3 and F4 shortcuts for barcode operations
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generateBarcode();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                printBarcode();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),
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
        productInput.setToolTipText("<html>Type product name (max 35 characters) and press ENTER to move to next field</html>");
        categoryCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to brand<br>Press <b>F1</b> to add new category</html>");
        brandCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to barcode<br>Press <b>F2</b> to add new brand</html>");
        barcodeInput.setToolTipText("<html>Type barcode and press ENTER to move to buttons<br>Press <b>F3</b> to generate barcode<br>Press <b>F4</b> to print barcode</html>");
        genarateBarecode.setToolTipText("Click to generate barcode (or press F3)");
        printBarcode.setToolTipText("Click to print barcode (or press F4)");

        setupButtonStyles();

        // Set focus to product name field after the dialog is visible
        SwingUtilities.invokeLater(() -> {
            productInput.requestFocusInWindow();
            productInput.selectAll(); // Select all text for easy editing
        });
    }

    private void preventUpArrowPopup(JComboBox<?> comboBox) {
        // Remove the default UP key binding that opens the popup
        comboBox.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
        comboBox.getInputMap(JComponent.WHEN_FOCUSED).remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));

        // Also remove the UP key binding that might be in the popup
        comboBox.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
    }

    private void categoryComboActionPerformed(java.awt.event.ActionEvent evt) {
        if (categoryCombo.getSelectedIndex() > 0 && !categoryCombo.isPopupVisible()) {
            brandCombo.requestFocusInWindow();
        }
    }

    private void brandComboActionPerformed(java.awt.event.ActionEvent evt) {
        if (brandCombo.getSelectedIndex() > 0 && !brandCombo.isPopupVisible()) {
            barcodeInput.requestFocusInWindow();
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

                boolean isHover = button.getModel().isRollover();
                boolean isPressed = button.getModel().isPressed();
                boolean isFocused = button.hasFocus();

                if (!isFocused && !isHover && !isPressed) {
                    g2.setColor(new Color(0, 0, 0, 0));
                    g2.fillRoundRect(0, 0, w, h, 5, 5);
                    g2.setColor(Color.decode("#0893B0"));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
                } else {
                    Color topColor = new Color(0x12, 0xB5, 0xA6);
                    Color bottomColor = new Color(0x08, 0x93, 0xB0);
                    GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 5, 5);
                }
                super.paint(g, c);
            }
        });
    }

    private void setupButtonStyles() {

        genarateBarecode.setBorderPainted(false);
        genarateBarecode.setContentAreaFilled(false);
        genarateBarecode.setFocusPainted(false);
        genarateBarecode.setOpaque(false);
        genarateBarecode.setFocusable(false);

        printBarcode.setBorderPainted(false);
        printBarcode.setContentAreaFilled(false);
        printBarcode.setFocusPainted(false);
        printBarcode.setOpaque(false);
        printBarcode.setFocusable(false);

        FlatSVGIcon barcodeIcon = new FlatSVGIcon("lk/com/pos/icon/barcode.svg", 25, 25);
        barcodeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        genarateBarecode.setIcon(barcodeIcon);

        FlatSVGIcon printerIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 25, 25);
        printerIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        printBarcode.setIcon(printerIcon);

        setupGradientButton(updateBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        updateBtn.setIcon(updateIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(cancelIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(clearIcon);

        setupButtonMouseListeners();
        setupButtonFocusListeners();
    }

    private void setupButtonMouseListeners() {
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

        setupArrowKeyNavigation();
        addEnterKeyNavigation();
    }

    private void setupArrowKeyNavigation() {
        productInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleEnterWithAllFieldsFilled(evt, productInput);
                } else {
                    handleArrowNavigation(evt, productInput);
                }
            }
        });

        categoryCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER && !categoryCombo.isPopupVisible()) {
                    handleEnterWithAllFieldsFilled(evt, categoryCombo);
                } else {
                    handleArrowNavigation(evt, categoryCombo);
                }
            }
        });

        brandCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER && !brandCombo.isPopupVisible()) {
                    handleEnterWithAllFieldsFilled(evt, brandCombo);
                } else {
                    handleArrowNavigation(evt, brandCombo);
                }
            }
        });

        barcodeInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Check if all fields are filled
                    if (allFieldsFilled()) {
                        // All fields filled - go directly to Update button
                        updateBtn.requestFocusInWindow();
                    } else {
                        // Not all fields filled - go to cancel button
                        cancelBtn.requestFocusInWindow();
                    }
                    evt.consume();
                } else {
                    handleArrowNavigation(evt, barcodeInput);
                }
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
            brandCombo.requestFocusInWindow();
        } else if (source == brandCombo) {
            barcodeInput.requestFocusInWindow();
        } else if (source == barcodeInput) {
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
        } else if (source == brandCombo) {
            categoryCombo.requestFocusInWindow();
        } else if (source == barcodeInput) {
            brandCombo.requestFocusInWindow();
        } else if (source == cancelBtn) {
            barcodeInput.requestFocusInWindow();
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
        } else if (source == brandCombo) {
            barcodeInput.requestFocusInWindow();
        } else if (source == barcodeInput) {
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
            // Don't open popup - just move focus
            productInput.requestFocusInWindow();
        } else if (source == brandCombo) {
            // Don't open popup - just move focus
            categoryCombo.requestFocusInWindow();
        } else if (source == barcodeInput) {
            brandCombo.requestFocusInWindow();
        } else if (source == cancelBtn) {
            barcodeInput.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == updateBtn) {
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
            // Exception handling without print statements
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
            // Exception handling without print statements
        }
    }

    private void generateBarcode() {
        // Generate 13-digit numeric barcode
        StringBuilder barcode = new StringBuilder();

        // Generate 13 random digits
        for (int i = 0; i < 13; i++) {
            int digit = (int) (Math.random() * 10); // Random digit from 0-9
            barcode.append(digit);
        }

        barcodeInput.setText(barcode.toString());
    }

    private void printBarcode() {
        String barcodeText = barcodeInput.getText().trim();

        if (barcodeText.isEmpty()) {
            barcodeInput.requestFocus();
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
            }
        } catch (PrinterException e) {
            // Exception handling without print statements
        }
    }

    private boolean validateForm() {
        if (productInput.getText().trim().isEmpty()) {
            productInput.requestFocus();
            return false;
        }

        if (productInput.getText().trim().length() > 35) {
            productInput.requestFocus();
            return false;
        }

        if (categoryCombo.getSelectedIndex() <= 0) {
            categoryCombo.requestFocus();
            return false;
        }

        if (brandCombo.getSelectedIndex() <= 0) {
            brandCombo.requestFocus();
            return false;
        }

        if (barcodeInput.getText().trim().isEmpty()) {
            barcodeInput.requestFocus();
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

            // Ensure the product name doesn't exceed 35 characters
            if (productName.length() > 35) {
                productName = productName.substring(0, 35);
            }

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
                productInput.requestFocus();
                productInput.selectAll();
                return;
            }

            // Check if barcode already exists for another product
            String currentBarcode = barcodeInput.getText().trim();
            if (!currentBarcode.equals(originalBarcode)) {
                ResultSet barcodeCheckRs = MySQL.executeSearch("SELECT product_id FROM product WHERE barcode = '"
                        + currentBarcode + "' AND product_id != " + productId);
                if (barcodeCheckRs.next()) {
                    barcodeInput.requestFocus();
                    barcodeInput.selectAll();
                    return;
                }
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
                    + "category_id = " + categoryId + ", "
                    + "barcode = '" + currentBarcode + "' "
                    + "WHERE product_id = " + productId;

            MySQL.executeIUD(query);

            this.dispose();

        } catch (Exception e) {
            // Exception handling without print statements
        }
    }

    private void clearForm() {
        // Reload original data instead of clearing completely
        loadProductData();

        // Set focus back to product name and select all text
        SwingUtilities.invokeLater(() -> {
            productInput.requestFocusInWindow();
            productInput.selectAll();
        });
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
        updateBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        printBarcode = new javax.swing.JButton();
        genarateBarecode = new javax.swing.JButton();
        productInput = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 147, 176));
        jLabel3.setText("Edit Product");

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
                                    .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(brandCombo, 0, 263, Short.MAX_VALUE))
                                .addComponent(jSeparator3)
                                .addComponent(jLabel3)
                                .addComponent(productInput))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                    .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(brandCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(printBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(genarateBarecode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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

    private void categoryComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_categoryComboKeyPressed
        if (categoryCombo.isPopupVisible()) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                categoryCombo.setPopupVisible(false);
                if (categoryCombo.getSelectedIndex() > 0) {
                    brandCombo.requestFocusInWindow();
                }
                evt.consume();
            }
            return;
        }

        // Handle navigation when popup is NOT visible
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                if (categoryCombo.getSelectedIndex() > 0) {
                    brandCombo.requestFocusInWindow();
                } else {
                    categoryCombo.showPopup();
                }
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                // Only show popup on DOWN if not using for navigation
                if (!evt.isControlDown()) {
                    categoryCombo.showPopup();
                }
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                // FIX: Explicitly prevent popup from opening and move focus
                productInput.requestFocusInWindow();
                evt.consume();
                break;
            case KeyEvent.VK_RIGHT:
                brandCombo.requestFocusInWindow();
                evt.consume();
                break;
            case KeyEvent.VK_LEFT:
                productInput.requestFocusInWindow();
                evt.consume();
                break;
            case KeyEvent.VK_SPACE:
                if (!categoryCombo.isPopupVisible()) {
                    categoryCombo.showPopup();
                }
                evt.consume();
                break;
            default:
                break;
        }
    }//GEN-LAST:event_categoryComboKeyPressed

    private void brandComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_brandComboKeyPressed
        // If popup is visible, only handle ENTER to close it
        if (brandCombo.isPopupVisible()) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                brandCombo.setPopupVisible(false);
                if (brandCombo.getSelectedIndex() > 0) {
                    barcodeInput.requestFocusInWindow();
                }
                evt.consume();
            }
            return;
        }

        // Handle navigation when popup is NOT visible
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                if (brandCombo.getSelectedIndex() > 0) {
                    barcodeInput.requestFocusInWindow();
                } else {
                    brandCombo.showPopup();
                }
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                // Only show popup on DOWN if not using for navigation
                if (!evt.isControlDown()) {
                    brandCombo.showPopup();
                }
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                // FIX: Explicitly prevent popup from opening and move focus
                categoryCombo.requestFocusInWindow();
                evt.consume();
                break;
            case KeyEvent.VK_RIGHT:
                barcodeInput.requestFocusInWindow();
                evt.consume();
                break;
            case KeyEvent.VK_LEFT:
                categoryCombo.requestFocusInWindow();
                evt.consume();
                break;
            case KeyEvent.VK_SPACE:
                if (!brandCombo.isPopupVisible()) {
                    brandCombo.showPopup();
                }
                evt.consume();
                break;
            default:
                break;
        }
    }//GEN-LAST:event_brandComboKeyPressed

    private void barcodeInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeInputActionPerformed
        cancelBtn.requestFocus();
    }//GEN-LAST:event_barcodeInputActionPerformed

    private void barcodeInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_barcodeInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // Check if all fields are filled
            if (allFieldsFilled()) {
                // All fields filled - go directly to Update button
                updateBtn.requestFocusInWindow();
            } else {
                // Not all fields filled - go to cancel button
                cancelBtn.requestFocusInWindow();
            }
            evt.consume();
        } else {
            handleArrowNavigation(evt, barcodeInput);
        }
    }//GEN-LAST:event_barcodeInputKeyPressed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.dispose();
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.dispose();
        } else {
            handleArrowNavigation(evt, cancelBtn);
        }
    }//GEN-LAST:event_cancelBtnKeyPressed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        saveProduct();

    }//GEN-LAST:event_updateBtnActionPerformed

    private void updateBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveProduct();
        } else {
            handleArrowNavigation(evt, updateBtn);
        }
    }//GEN-LAST:event_updateBtnKeyPressed

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

    private void printBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBarcodeActionPerformed
        printBarcode();
    }//GEN-LAST:event_printBarcodeActionPerformed

    private void printBarcodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_printBarcodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            printBarcodeActionPerformed(null);
        } else {
            handleArrowNavigation(evt, printBarcode);
        }
    }//GEN-LAST:event_printBarcodeKeyPressed

    private void genarateBarecodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genarateBarecodeActionPerformed
        generateBarcode();
    }//GEN-LAST:event_genarateBarecodeActionPerformed

    private void genarateBarecodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_genarateBarecodeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            genarateBarecodeActionPerformed(null);
        } else {
            handleArrowNavigation(evt, genarateBarecode);
        }
    }//GEN-LAST:event_genarateBarecodeKeyPressed

    private void genarateBarecodeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_genarateBarecodeKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_genarateBarecodeKeyReleased

    private void productInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productInputActionPerformed
        categoryCombo.requestFocus();
    }//GEN-LAST:event_productInputActionPerformed

    private void productInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            categoryCombo.requestFocusInWindow();
        } else {
            handleArrowNavigation(evt, productInput);
        }
    }//GEN-LAST:event_productInputKeyPressed

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
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
