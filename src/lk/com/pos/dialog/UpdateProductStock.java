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
public class UpdateProductStock extends javax.swing.JDialog {

    private int productId;
    private int stockId;
    private String originalBarcode;
    private String originalBatchNo;

    /**
     * Creates new form UpdateProduct
     */
    public UpdateProductStock(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
        AutoCompleteDecorator.decorate(categoryCombo);
        AutoCompleteDecorator.decorate(brandCombo);
        AutoCompleteDecorator.decorate(SupplierCombo);
    }

    // Updated constructor to accept stockId
    public UpdateProductStock(java.awt.Frame parent, boolean modal, int productId, int stockId) {
        super(parent, modal);
        this.productId = productId;
        this.stockId = stockId;
        initComponents();
        initializeDialog();
        AutoCompleteDecorator.decorate(categoryCombo);
        AutoCompleteDecorator.decorate(brandCombo);
        AutoCompleteDecorator.decorate(SupplierCombo);
        loadProductData();
    }

    private void loadProductData() {
        try {
            // Load product data with category and brand names
            String productQuery = "SELECT p.*, c.category_name, b.brand_name "
                    + "FROM product p "
                    + "LEFT JOIN category c ON p.category_id = c.category_id "
                    + "LEFT JOIN brand b ON p.brand_id = b.brand_id "
                    + "WHERE p.product_id = " + productId;

            ResultSet productRs = MySQL.executeSearch(productQuery);
            if (productRs.next()) {
                productInput.setText(productRs.getString("product_name"));
                originalBarcode = productRs.getString("barcode");
                barcodeInput.setText(originalBarcode);

                // Set category
                String categoryName = productRs.getString("category_name");
                for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                    if (categoryCombo.getItemAt(i).equals(categoryName)) {
                        categoryCombo.setSelectedIndex(i);
                        break;
                    }
                }

                // Set brand
                String brandName = productRs.getString("brand_name");
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
                return;
            }

            // Load specific stock data using stockId
            String stockQuery = "SELECT * FROM stock WHERE stock_id = " + stockId + " AND product_id = " + productId;
            ResultSet stockRs = MySQL.executeSearch(stockQuery);
            if (stockRs.next()) {
                originalBatchNo = stockRs.getString("batch_no");
                batchNoInput.setText(originalBatchNo);

                // REMOVED: Batch number field is now ENABLED and can be changed
                batchNoInput.setEnabled(true);
                batchNoInput.setBackground(Color.WHITE);
                batchNoInput.setToolTipText("<html>Batch number - you can type your own or press <b>F3</b> to generate new one</html>");

                purchasePrice.setText(String.valueOf(stockRs.getDouble("purchase_price")));
                lastPrice.setText(String.valueOf(stockRs.getDouble("last_price")));
                sellingPrice.setText(String.valueOf(stockRs.getDouble("selling_price")));
                quantityInput.setText(String.valueOf(stockRs.getInt("qty")));

                // Set supplier
                int supplierId = stockRs.getInt("suppliers_id");
                if (supplierId > 0) {
                    ResultSet supplierRs = MySQL.executeSearch("SELECT suppliers_name FROM suppliers WHERE suppliers_id = " + supplierId);
                    if (supplierRs.next()) {
                        String supplierName = supplierRs.getString("suppliers_name");
                        for (int i = 0; i < SupplierCombo.getItemCount(); i++) {
                            if (SupplierCombo.getItemAt(i).equals(supplierName)) {
                                SupplierCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }

                // Set dates - FIXED: Use java.sql.Date directly
                java.sql.Date mfgDate = stockRs.getDate("manufacture_date");
                java.sql.Date expDate = stockRs.getDate("expriy_date");

                if (mfgDate != null) {
                    // Convert java.sql.Date to java.util.Date for JDateChooser
                    manufactureDate.setDate(new java.util.Date(mfgDate.getTime()));
                } else {
                    manufactureDate.setDate(null);
                }

                if (expDate != null) {
                    // Convert java.sql.Date to java.util.Date for JDateChooser
                    expriyDate.setDate(new java.util.Date(expDate.getTime()));
                } else {
                    expriyDate.setDate(null);
                }

                // Force UI refresh
                manufactureDate.repaint();
                expriyDate.repaint();

            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Stock entry not found!");
                this.dispose();
            }

        } catch (Exception e) {
            e.printStackTrace(); // Add this to see the full error
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading product data: " + e.getMessage());
            this.dispose();
        }
    }

    private void initializeDialog() {
        setLocationRelativeTo(getParent());
        setTitle("Update Product & Stock");
        loadCategoryCombo();
        loadBrandCombo();
        loadSupplierCombo();
        setupFocusTraversal();
        setupArrowKeyNavigation();
        addEnterKeyNavigation();
        setupButtonStyles();

        // Add mouse listeners to show popup on click
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

        SupplierCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SupplierCombo.showPopup();
            }
        });

        // Add action listeners to move focus after selection
        categoryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (categoryCombo.getSelectedIndex() > 0 && !categoryCombo.isPopupVisible()) {
                    brandCombo.requestFocusInWindow();
                }
            }
        });

        brandCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (brandCombo.getSelectedIndex() > 0 && !brandCombo.isPopupVisible()) {
                    SupplierCombo.requestFocusInWindow();
                }
            }
        });

        SupplierCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (SupplierCombo.getSelectedIndex() > 0 && !SupplierCombo.isPopupVisible()) {
                    purchasePrice.requestFocusInWindow();
                }
            }
        });

        // Add F1 to F5 shortcuts
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openAddNewProduct();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openAddNewCategory();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openAddNewBrand();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openAddNewSupplier();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                printBarcode();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

// Change from F3 to F6 for generating batch number
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generateBatchNumber();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), // Changed from VK_F3 to VK_F6
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // ESC key to close dialog
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
        categoryCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to brand<br>Press <b>F2</b> to add new category</html>");
        brandCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to supplier<br>Press <b>F3</b> to add new brand</html>");
        SupplierCombo.setToolTipText("<html>Use DOWN arrow to open dropdown, ENTER to select and move to purchase price<br>Press <b>F4</b> to add new supplier</html>");
        purchasePrice.setToolTipText("Enter purchase price and press ENTER to move to next field");
        lastPrice.setToolTipText("Enter last price and press ENTER to move to next field");
        sellingPrice.setToolTipText("Enter selling price and press ENTER to move to next field");
        batchNoInput.setToolTipText("<html>Batch number - you can type your own or press <b>F6</b> to generate new one</html>");
        generateBatchBtn.setToolTipText("Click to generate new batch number (or press F6)");
        quantityInput.setToolTipText("Enter quantity and press ENTER to move to next field");
        barcodeInput.setToolTipText("<html>Barcode cannot be changed for existing products<br>Press <b>F5</b> to print barcode</html>");
        addNewProduct.setToolTipText("Click to add new product (or press F1)");
        addNewCategory.setToolTipText("Click to add new category (or press F2)");
        addNewBrand.setToolTipText("Click to add new brand (or press F3)");
        addNewSupplier.setToolTipText("Click to add new supplier (or press F4)");
        printBarcode.setToolTipText("Click to print barcode (or press F5)");

        productInput.requestFocus();
        productInput.requestFocusInWindow();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent evt) {
                productInput.requestFocusInWindow();
            }
        });
    }

    // ---------------- BATCH NUMBER GENERATION ----------------
    private void generateBatchNumber() {
        String batchNumber;
        do {
            // Generate 3 capital letters
            String letters = "";
            for (int i = 0; i < 3; i++) {
                letters += (char) ('A' + (int) (Math.random() * 26));
            }

            // Generate exactly 10 numbers
            String numbers = "";
            for (int i = 0; i < 10; i++) {
                numbers += (int) (Math.random() * 10);
            }

            batchNumber = letters + numbers;
        } while (isBatchNumberExists(batchNumber));

        batchNoInput.setText(batchNumber);
        batchNoInput.requestFocus();
        batchNoInput.selectAll();
    }

    private boolean isBatchNumberExists(String batchNo) {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT batch_no FROM stock WHERE batch_no = '" + batchNo + "' AND stock_id != " + stockId);
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- KEYBOARD NAVIGATION SETUP ----------------
    private void setupFocusTraversal() {
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(
                productInput,
                categoryCombo,
                brandCombo,
                SupplierCombo,
                purchasePrice,
                lastPrice,
                sellingPrice,
                batchNoInput,
                quantityInput,
                manufactureDate.getDateEditor().getUiComponent(),
                expriyDate.getDateEditor().getUiComponent(),
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
        java.awt.Component[] components = {
            productInput, purchasePrice, lastPrice, sellingPrice, batchNoInput, quantityInput,
            manufactureDate.getDateEditor().getUiComponent(), expriyDate.getDateEditor().getUiComponent(),
            barcodeInput, updateBtn, clearFormBtn, cancelBtn,
            addNewProduct, addNewCategory, addNewBrand, addNewSupplier, printBarcode, generateBatchBtn
        };

        for (java.awt.Component component : components) {
            component.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        handleEnterWithAllFieldsFilled(evt, component);
                    } else {
                        handleArrowNavigation(evt, component);
                    }
                }
            });
        }

        // Special handling for combo boxes
        setupComboBoxNavigation(categoryCombo);
        setupComboBoxNavigation(brandCombo);
        setupComboBoxNavigation(SupplierCombo);

        // Special handling for buttons with Enter key
        updateBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveProductAndStock();
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

        // Special handling for action buttons
        addNewProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    openAddNewProduct();
                } else {
                    handleArrowNavigation(evt, addNewProduct);
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

        addNewSupplier.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    openAddNewSupplier();
                } else {
                    handleArrowNavigation(evt, addNewSupplier);
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

        generateBatchBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    generateBatchNumber();
                } else {
                    handleArrowNavigation(evt, generateBatchBtn);
                }
            }
        });

        // Special handling for batch number input with F3
        batchNoInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_F6) {  // Changed from VK_F3 to VK_F6
                    generateBatchNumber();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (allFieldsFilled()) {
                        updateBtn.requestFocusInWindow();
                    } else {
                        quantityInput.requestFocusInWindow();
                    }
                } else {
                    handleArrowNavigation(evt, batchNoInput);
                }
            }
        });
    }

    private void setupComboBoxNavigation(javax.swing.JComboBox<String> comboBox) {
        comboBox.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                // If popup is visible, handle selection first
                if (comboBox.isPopupVisible()) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        comboBox.setPopupVisible(false);
                        // Move to next field after selection
                        moveToNextField(comboBox);
                        evt.consume();
                    }
                    return;
                }

                // If popup is NOT visible, handle navigation
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        if (comboBox.getSelectedIndex() > 0) {
                            // Valid item selected, move to next field
                            moveToNextField(comboBox);
                        } else {
                            // No selection, open popup
                            comboBox.showPopup();
                        }
                        evt.consume();
                        break;

                    case KeyEvent.VK_DOWN:
                        if (!comboBox.isPopupVisible()) {
                            comboBox.showPopup();
                        }
                        evt.consume();
                        break;

                    case KeyEvent.VK_UP:
                        if (!comboBox.isPopupVisible()) {
                            moveToPreviousField(comboBox);
                        }
                        evt.consume();
                        break;

                    case KeyEvent.VK_RIGHT:
                        moveToNextField(comboBox);
                        evt.consume();
                        break;

                    case KeyEvent.VK_LEFT:
                        moveToPreviousField(comboBox);
                        evt.consume();
                        break;

                    default:
                        // Let other keys be handled normally
                        break;
                }
            }
        });
    }

    private void moveToNextField(java.awt.Component currentComponent) {
        java.awt.Component nextComponent = null;

        if (currentComponent == productInput) {
            nextComponent = categoryCombo;
        } else if (currentComponent == categoryCombo) {
            nextComponent = brandCombo;
        } else if (currentComponent == brandCombo) {
            nextComponent = SupplierCombo;
        } else if (currentComponent == SupplierCombo) {
            nextComponent = purchasePrice;
        } else if (currentComponent == purchasePrice) {
            nextComponent = lastPrice;
        } else if (currentComponent == lastPrice) {
            nextComponent = sellingPrice;
        } else if (currentComponent == sellingPrice) {
            nextComponent = batchNoInput;
        } else if (currentComponent == batchNoInput) {
            nextComponent = quantityInput;
        } else if (currentComponent == quantityInput) {
            nextComponent = manufactureDate.getDateEditor().getUiComponent();
        } else if (currentComponent == manufactureDate.getDateEditor().getUiComponent()) {
            nextComponent = expriyDate.getDateEditor().getUiComponent();
        } else if (currentComponent == expriyDate.getDateEditor().getUiComponent()) {
            // Skip barcodeInput if disabled, go to buttons
            if (barcodeInput.isEnabled()) {
                nextComponent = barcodeInput;
            } else {
                // When all fields are filled, go directly to update button
                if (allFieldsFilled()) {
                    nextComponent = updateBtn;
                } else {
                    nextComponent = cancelBtn;
                }
            }
        } else if (currentComponent == barcodeInput) {
            // When all fields are filled, go directly to update button
            if (allFieldsFilled()) {
                nextComponent = updateBtn;
            } else {
                nextComponent = cancelBtn;
            }
        } else if (currentComponent == cancelBtn) {
            nextComponent = clearFormBtn;
        } else if (currentComponent == clearFormBtn) {
            nextComponent = updateBtn;
        } else if (currentComponent == updateBtn) {
            nextComponent = productInput;
        }

        if (nextComponent != null && nextComponent.isEnabled()) {
            nextComponent.requestFocusInWindow();
        } else if (nextComponent != null) {
            // If the next component is disabled, recursively find the next enabled component
            moveToNextField(nextComponent);
        }
    }

    private void moveToPreviousField(java.awt.Component currentComponent) {
        java.awt.Component prevComponent = null;

        if (currentComponent == categoryCombo) {
            prevComponent = productInput;
        } else if (currentComponent == brandCombo) {
            prevComponent = categoryCombo;
        } else if (currentComponent == SupplierCombo) {
            prevComponent = brandCombo;
        } else if (currentComponent == purchasePrice) {
            prevComponent = SupplierCombo;
        } else if (currentComponent == lastPrice) {
            prevComponent = purchasePrice;
        } else if (currentComponent == sellingPrice) {
            prevComponent = lastPrice;
        } else if (currentComponent == batchNoInput) {
            prevComponent = sellingPrice;
        } else if (currentComponent == quantityInput) {
            prevComponent = batchNoInput;
        } else if (currentComponent == manufactureDate.getDateEditor().getUiComponent()) {
            prevComponent = quantityInput;
        } else if (currentComponent == expriyDate.getDateEditor().getUiComponent()) {
            prevComponent = manufactureDate.getDateEditor().getUiComponent();
        } else if (currentComponent == barcodeInput) {
            prevComponent = expriyDate.getDateEditor().getUiComponent();
        } else if (currentComponent == cancelBtn) {
            // Skip barcodeInput if disabled, go to date field
            if (barcodeInput.isEnabled()) {
                prevComponent = barcodeInput;
            } else {
                prevComponent = expriyDate.getDateEditor().getUiComponent();
            }
        } else if (currentComponent == clearFormBtn) {
            prevComponent = cancelBtn;
        } else if (currentComponent == updateBtn) {
            prevComponent = clearFormBtn;
        } else if (currentComponent == productInput) {
            prevComponent = updateBtn;
        }

        if (prevComponent != null && prevComponent.isEnabled()) {
            prevComponent.requestFocusInWindow();
        } else if (prevComponent != null) {
            // If the previous component is disabled, recursively find the previous enabled component
            moveToPreviousField(prevComponent);
        }
    }

    private void handleEnterWithAllFieldsFilled(java.awt.event.KeyEvent evt, java.awt.Component source) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            moveToNextField(source);
            evt.consume();
        }
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
            generateBatchBtn.requestFocusInWindow();
        } else if (source == generateBatchBtn) {
            quantityInput.requestFocusInWindow();
        } else if (source == quantityInput) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            // Skip barcodeInput if disabled
            if (barcodeInput.isEnabled()) {
                barcodeInput.requestFocusInWindow();
            } else {
                printBarcode.requestFocusInWindow();
            }
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
        } else if (source == SupplierCombo) {
            addNewBrand.requestFocusInWindow();
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
        } else if (source == generateBatchBtn) {
            batchNoInput.requestFocusInWindow();
        } else if (source == quantityInput) {
            generateBatchBtn.requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            quantityInput.requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == barcodeInput) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == printBarcode) {
            // Skip barcodeInput if disabled
            if (barcodeInput.isEnabled()) {
                barcodeInput.requestFocusInWindow();
            } else {
                expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
            }
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
            SupplierCombo.requestFocusInWindow();
        } else if (source == addNewBrand) {
            SupplierCombo.requestFocusInWindow();
        } else if (source == SupplierCombo) {
            purchasePrice.requestFocusInWindow();
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
        } else if (source == generateBatchBtn) {
            quantityInput.requestFocusInWindow();
        } else if (source == quantityInput) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            // Skip barcodeInput if disabled
            if (barcodeInput.isEnabled()) {
                barcodeInput.requestFocusInWindow();
            } else {
                // When all fields are filled, go directly to update button
                if (allFieldsFilled()) {
                    updateBtn.requestFocusInWindow();
                } else {
                    cancelBtn.requestFocusInWindow();
                }
            }
        } else if (source == barcodeInput) {
            // When all fields are filled, go directly to update button
            if (allFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
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
        } else if (source == SupplierCombo) {
            brandCombo.requestFocusInWindow();
        } else if (source == addNewSupplier) {
            brandCombo.requestFocusInWindow();
        } else if (source == purchasePrice) {
            SupplierCombo.requestFocusInWindow();
        } else if (source == lastPrice) {
            purchasePrice.requestFocusInWindow();
        } else if (source == sellingPrice) {
            lastPrice.requestFocusInWindow();
        } else if (source == batchNoInput) {
            sellingPrice.requestFocusInWindow();
        } else if (source == generateBatchBtn) {
            sellingPrice.requestFocusInWindow();
        } else if (source == quantityInput) {
            batchNoInput.requestFocusInWindow();
        } else if (source == manufactureDate.getDateEditor().getUiComponent()) {
            quantityInput.requestFocusInWindow();
        } else if (source == expriyDate.getDateEditor().getUiComponent()) {
            manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == barcodeInput) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == printBarcode) {
            expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
        } else if (source == cancelBtn) {
            // Skip barcodeInput if disabled
            if (barcodeInput.isEnabled()) {
                barcodeInput.requestFocusInWindow();
            } else {
                expriyDate.getDateEditor().getUiComponent().requestFocusInWindow();
            }
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
        enterNavigationMap.put(brandCombo, SupplierCombo);
        enterNavigationMap.put(SupplierCombo, purchasePrice);
        enterNavigationMap.put(purchasePrice, lastPrice);
        enterNavigationMap.put(lastPrice, sellingPrice);
        enterNavigationMap.put(sellingPrice, batchNoInput);
        enterNavigationMap.put(batchNoInput, quantityInput);
        enterNavigationMap.put(quantityInput, manufactureDate.getDateEditor().getUiComponent());
        enterNavigationMap.put(manufactureDate.getDateEditor().getUiComponent(), expriyDate.getDateEditor().getUiComponent());
        enterNavigationMap.put(expriyDate.getDateEditor().getUiComponent(), barcodeInput.isEnabled() ? barcodeInput : (allFieldsFilled() ? updateBtn : cancelBtn));
        enterNavigationMap.put(barcodeInput, allFieldsFilled() ? updateBtn : cancelBtn);
        enterNavigationMap.put(cancelBtn, clearFormBtn);
        enterNavigationMap.put(clearFormBtn, updateBtn);
        enterNavigationMap.put(updateBtn, productInput);
        enterNavigationMap.put(addNewProduct, categoryCombo);
        enterNavigationMap.put(addNewCategory, brandCombo);
        enterNavigationMap.put(addNewBrand, SupplierCombo);
        enterNavigationMap.put(addNewSupplier, purchasePrice);
        enterNavigationMap.put(printBarcode, cancelBtn);
        enterNavigationMap.put(generateBatchBtn, quantityInput);

        // Add key listeners to all components except combo boxes
        for (java.awt.Component component : enterNavigationMap.keySet()) {
            if (!(component instanceof javax.swing.JComboBox)) {
                component.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent evt) {
                        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                            java.awt.Component nextComponent = enterNavigationMap.get(component);
                            if (nextComponent != null && nextComponent.isEnabled()) {
                                nextComponent.requestFocusInWindow();
                            } else if (nextComponent != null) {
                                // If next component is disabled, find next enabled component
                                moveToNextField(component);
                            }

                            // Special case: if we're moving to update button and form is valid, auto-save
                            if (nextComponent == updateBtn && validateForm()) {
                                saveProductAndStock();
                            }
                            evt.consume();
                        }
                    }
                });
            }
        }
    }

    // Check if all required fields are filled
    private boolean allFieldsFilled() {
        return !productInput.getText().trim().isEmpty()
                && categoryCombo.getSelectedIndex() > 0
                && brandCombo.getSelectedIndex() > 0
                && !purchasePrice.getText().trim().isEmpty()
                && !sellingPrice.getText().trim().isEmpty()
                && !batchNoInput.getText().trim().isEmpty()
                && !quantityInput.getText().trim().isEmpty();
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
        JButton[] iconButtons = {addNewProduct, addNewCategory, addNewBrand, addNewSupplier, printBarcode, generateBatchBtn};
        for (JButton button : iconButtons) {
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(false);
            button.setFocusable(true);
        }

        // Setup icons for add buttons
        FlatSVGIcon productIcon = new FlatSVGIcon("lk/com/pos/icon/add-product.svg", 25, 25);
        productIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewProduct.setIcon(productIcon);

        FlatSVGIcon categoryIcon = new FlatSVGIcon("lk/com/pos/icon/category.svg", 25, 25);
        categoryIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewCategory.setIcon(categoryIcon);

        FlatSVGIcon brandIcon = new FlatSVGIcon("lk/com/pos/icon/add-brand.svg", 25, 25);
        brandIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewBrand.setIcon(brandIcon);

        FlatSVGIcon supplierIcon = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 25, 25);
        supplierIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        addNewSupplier.setIcon(supplierIcon);

        FlatSVGIcon printerIcon = new FlatSVGIcon("lk/com/pos/icon/printer.svg", 25, 25);
        printerIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        printBarcode.setIcon(printerIcon);

        // Setup batch icon with gray color
        FlatSVGIcon batchIcon = new FlatSVGIcon("lk/com/pos/icon/refresh.svg", 25, 25);
        batchIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
        generateBatchBtn.setIcon(batchIcon);

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

        // Mouse listeners for icon buttons
        setupIconButtonMouseListener(addNewProduct, "add-product");
        setupIconButtonMouseListener(addNewCategory, "category");
        setupIconButtonMouseListener(addNewBrand, "add-brand");
        setupIconButtonMouseListener(addNewSupplier, "addCustomer");
        setupIconButtonMouseListener(printBarcode, "printer");
        setupIconButtonMouseListener(generateBatchBtn, "refresh");
    }

    private void setupIconButtonMouseListener(JButton button, String iconName) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName + ".svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                button.setIcon(hoverIcon);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName + ".svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                button.setIcon(normalIcon);
            }
        });
    }

    private void setupButtonFocusListeners() {
        // Focus listeners for gradient buttons
        setupGradientButtonFocusListener(updateBtn, "update");
        setupGradientButtonFocusListener(clearFormBtn, "cancel");
        setupGradientButtonFocusListener(cancelBtn, "clear");

        // Focus listeners for icon buttons
        setupIconButtonFocusListener(addNewProduct, "add-product");
        setupIconButtonFocusListener(addNewCategory, "category");
        setupIconButtonFocusListener(addNewBrand, "add-brand");
        setupIconButtonFocusListener(addNewSupplier, "addCustomer");
        setupIconButtonFocusListener(printBarcode, "printer");
        setupIconButtonFocusListener(generateBatchBtn, "refresh");
    }

    private void setupGradientButtonFocusListener(JButton button, String iconName) {
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                button.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName + ".svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                button.setIcon(focusedIcon);
                button.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                button.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName + ".svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                button.setIcon(normalIcon);
                button.repaint();
            }
        });
    }

    private void setupIconButtonFocusListener(JButton button, String iconName) {
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName + ".svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                button.setIcon(focusedIcon);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName + ".svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#999999")));
                button.setIcon(normalIcon);
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

    private void loadSupplierCombo() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT * FROM suppliers");
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

        if (purchasePrice.getText().trim().isEmpty()) {
            purchasePrice.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter purchase price");
            return false;
        }

        if (sellingPrice.getText().trim().isEmpty()) {
            sellingPrice.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter selling price");
            return false;
        }

        if (batchNoInput.getText().trim().isEmpty()) {
            batchNoInput.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter or generate batch number");
            return false;
        }

        if (quantityInput.getText().trim().isEmpty()) {
            quantityInput.requestFocus();
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter quantity");
            return false;
        }

        try {
            Double.parseDouble(purchasePrice.getText().trim());
            Double.parseDouble(sellingPrice.getText().trim());
            Integer.parseInt(quantityInput.getText().trim());
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please enter valid numbers for price and quantity fields");
            return false;
        }

        return true;
    }

private void saveProductAndStock() {
    if (!validateForm()) {
        return;
    }

    // Database connection and prepared statements
    java.sql.Connection conn = null;
    java.sql.PreparedStatement pstUpdateProduct = null;
    java.sql.PreparedStatement pstUpdateStock = null;
    java.sql.PreparedStatement pstGetOldData = null;
    java.sql.PreparedStatement pstCheckMessage = null;
    java.sql.PreparedStatement pstInsertMessage = null;
    java.sql.PreparedStatement pstInsertNotification = null;
    java.sql.ResultSet rs = null;

    try {
        // Get current data for notification message
        String productName = productInput.getText().trim();
        String batchNoValue = batchNoInput.getText().trim();
        
        // Get old data for comparison
        String getOldDataSql = "SELECT p.product_name, p.barcode, s.batch_no, s.purchase_price, s.selling_price, s.qty " +
                             "FROM product p JOIN stock s ON p.product_id = s.product_id " +
                             "WHERE p.product_id = ? AND s.stock_id = ?";
        
        conn = MySQL.getConnection();
        conn.setAutoCommit(false); // Start transaction
        
        pstGetOldData = conn.prepareStatement(getOldDataSql);
        pstGetOldData.setInt(1, productId);
        pstGetOldData.setInt(2, stockId);
        rs = pstGetOldData.executeQuery();

        String oldProductName = "";
        String oldBarcode = "";
        String oldBatchNo = "";
        double oldPurchasePrice = 0;
        double oldSellingPrice = 0;
        int oldQuantity = 0;
        
        if (rs.next()) {
            oldProductName = rs.getString("product_name");
            oldBarcode = rs.getString("barcode");
            oldBatchNo = rs.getString("batch_no");
            oldPurchasePrice = rs.getDouble("purchase_price");
            oldSellingPrice = rs.getDouble("selling_price");
            oldQuantity = rs.getInt("qty");
        }
        rs.close();
        pstGetOldData.close();

        // Update product table
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

        String productUpdateQuery = "UPDATE product SET "
                + "product_name = ?, "
                + "brand_id = ?, "
                + "category_id = ? "
                + "WHERE product_id = ?";

        pstUpdateProduct = conn.prepareStatement(productUpdateQuery);
        pstUpdateProduct.setString(1, productName);
        pstUpdateProduct.setInt(2, brandId);
        pstUpdateProduct.setInt(3, categoryId);
        pstUpdateProduct.setInt(4, productId);

        pstUpdateProduct.executeUpdate();

        // Update stock table
        int supplierId = 0;
        if (SupplierCombo.getSelectedIndex() > 0) {
            ResultSet supplierRs = MySQL.executeSearch("SELECT suppliers_id FROM suppliers WHERE suppliers_name = '"
                    + SupplierCombo.getSelectedItem().toString() + "'");
            if (supplierRs.next()) {
                supplierId = supplierRs.getInt("suppliers_id");
            }
        }

        double purchasePriceValue = Double.parseDouble(purchasePrice.getText().trim());
        double lastPriceValue = lastPrice.getText().trim().isEmpty() ? 0 : Double.parseDouble(lastPrice.getText().trim());
        double sellingPriceValue = Double.parseDouble(sellingPrice.getText().trim());
        int quantityValue = Integer.parseInt(quantityInput.getText().trim());

        // Check if batch number already exists (excluding current stock)
        if (!batchNoValue.equals(originalBatchNo)) {
            ResultSet batchCheckRs = MySQL.executeSearch(
                    "SELECT stock_id FROM stock WHERE batch_no = '" + batchNoValue
                    + "' AND stock_id != " + stockId
            );
            if (batchCheckRs.next()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Batch number '" + batchNoValue + "' already exists!");
                batchNoInput.requestFocus();
                batchNoInput.selectAll();
                return;
            }
        }

        // Format dates for SQL
        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String mfgDateStr = manufactureDate.getDate() != null ? "'" + sqlDateFormat.format(manufactureDate.getDate()) + "'" : "NULL";
        String expDateStr = expriyDate.getDate() != null ? "'" + sqlDateFormat.format(expriyDate.getDate()) + "'" : "NULL";

        String stockUpdateQuery = "UPDATE stock SET "
                + "batch_no = ?, "
                + "purchase_price = ?, "
                + "last_price = ?, "
                + "selling_price = ?, "
                + "qty = ?, "
                + "suppliers_id = ?, "
                + "manufacture_date = " + mfgDateStr + ", "
                + "expriy_date = " + expDateStr + " "
                + "WHERE stock_id = ? AND product_id = ?";

        pstUpdateStock = conn.prepareStatement(stockUpdateQuery);
        pstUpdateStock.setString(1, batchNoValue);
        pstUpdateStock.setDouble(2, purchasePriceValue);
        pstUpdateStock.setDouble(3, lastPriceValue);
        pstUpdateStock.setDouble(4, sellingPriceValue);
        pstUpdateStock.setInt(5, quantityValue);
        pstUpdateStock.setInt(6, supplierId > 0 ? supplierId : 0);
        pstUpdateStock.setInt(7, stockId);
        pstUpdateStock.setInt(8, productId);

        int stockRowsAffected = pstUpdateStock.executeUpdate();

        if (stockRowsAffected > 0) {
            // Create notification message with changes
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Product/Stock updated: ").append(oldProductName);
            
            // Track if any changes were made
            boolean hasChanges = false;
            
            // Check product changes
            if (!oldProductName.equals(productName)) {
                messageBuilder.append(" [Product: ").append(oldProductName).append(" → ").append(productName).append("]");
                hasChanges = true;
            }
            
            // Check stock changes
            if (!oldBatchNo.equals(batchNoValue)) {
                if (hasChanges) messageBuilder.append(", ");
                messageBuilder.append("[Batch: ").append(oldBatchNo).append(" → ").append(batchNoValue).append("]");
                hasChanges = true;
            }
            
            if (oldPurchasePrice != purchasePriceValue) {
                if (hasChanges) messageBuilder.append(", ");
                messageBuilder.append("[Purchase: ").append(String.format("%.2f", oldPurchasePrice))
                             .append(" → ").append(String.format("%.2f", purchasePriceValue)).append("]");
                hasChanges = true;
            }
            
            if (oldSellingPrice != sellingPriceValue) {
                if (hasChanges) messageBuilder.append(", ");
                messageBuilder.append("[Selling: ").append(String.format("%.2f", oldSellingPrice))
                             .append(" → ").append(String.format("%.2f", sellingPriceValue)).append("]");
                hasChanges = true;
            }
            
            if (oldQuantity != quantityValue) {
                if (hasChanges) messageBuilder.append(", ");
                messageBuilder.append("[Qty: ").append(oldQuantity).append(" → ").append(quantityValue).append("]");
                hasChanges = true;
            }
            
            // If no specific changes detected, show general update message
            if (!hasChanges) {
                messageBuilder.append(" - Details updated");
            }
            
            String messageText = messageBuilder.toString();

            // Check if message already exists in massage table
            String checkMessageSql = "SELECT massage_id FROM massage WHERE massage = ?";
            pstCheckMessage = conn.prepareStatement(checkMessageSql);
            pstCheckMessage.setString(1, messageText);
            rs = pstCheckMessage.executeQuery();

            int messageId;
            
            if (rs.next()) {
                // Message already exists, get the existing massage_id
                messageId = rs.getInt("massage_id");
            } else {
                // Message doesn't exist, insert new message
                String insertMessageSql = "INSERT INTO massage (massage) VALUES (?)";
                pstInsertMessage = conn.prepareStatement(insertMessageSql, java.sql.PreparedStatement.RETURN_GENERATED_KEYS);
                pstInsertMessage.setString(1, messageText);
                pstInsertMessage.executeUpdate();
                
                // Get the generated message ID
                java.sql.ResultSet generatedKeys = pstInsertMessage.getGeneratedKeys();
                if (generatedKeys.next()) {
                    messageId = generatedKeys.getInt(1);
                } else {
                    throw new java.sql.SQLException("Failed to get generated message ID");
                }
                generatedKeys.close();
            }

            // Insert notification (msg_type_id 14 for "Edit Product/Stock")
            String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (1, NOW(), 14, ?)";
            pstInsertNotification = conn.prepareStatement(notificationSql);
            pstInsertNotification.setInt(1, messageId);
            pstInsertNotification.executeUpdate();

            // Commit transaction
            conn.commit();

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "Product and stock updated successfully!");
            this.dispose();
        } else {
            conn.rollback();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Failed to update product and stock!");
        }

    } catch (Exception e) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }
        Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                "Error updating product and stock: " + e.getMessage());
        e.printStackTrace();
    } finally {
        // Close all resources
        try {
            if (rs != null) rs.close();
            if (pstUpdateProduct != null) pstUpdateProduct.close();
            if (pstUpdateStock != null) pstUpdateStock.close();
            if (pstGetOldData != null) pstGetOldData.close();
            if (pstCheckMessage != null) pstCheckMessage.close();
            if (pstInsertMessage != null) pstInsertMessage.close();
            if (pstInsertNotification != null) pstInsertNotification.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}
    private void clearForm() {
        // Reload original data
        loadProductData();
        productInput.requestFocus();
    }

    private void openAddNewProduct() {
        try {
            AddNewProduct addProduct = new AddNewProduct(null, true);
        addProduct.setLocationRelativeTo(null);
        addProduct.setVisible(true);
            // Note: Product name is a text field, so no need to reload combo
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening product dialog: " + e.getMessage());
        }
    }

    private void openAddNewCategory() {
        try {
            AddNewCategoryDialog dialog = new AddNewCategoryDialog(null, true);
            dialog.setLocationRelativeTo(null);
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
            AddNewBrandDialog dialog = new AddNewBrandDialog(null, true);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            loadBrandCombo();
            brandCombo.requestFocus();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening brand dialog: " + e.getMessage());
        }
    }

    private void openAddNewSupplier() {
        try {
            AddSupplier dialog = new AddSupplier(null, true);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            loadSupplierCombo();
            SupplierCombo.requestFocus();
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error opening supplier dialog: " + e.getMessage());
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
        updateBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        printBarcode = new javax.swing.JButton();
        productInput = new javax.swing.JTextField();
        addNewCategory = new javax.swing.JButton();
        addNewBrand = new javax.swing.JButton();
        SupplierCombo = new javax.swing.JComboBox<>();
        purchasePrice = new javax.swing.JTextField();
        lastPrice = new javax.swing.JTextField();
        manufactureDate = new com.toedter.calendar.JDateChooser();
        expriyDate = new com.toedter.calendar.JDateChooser();
        addNewSupplier = new javax.swing.JButton();
        addNewProduct = new javax.swing.JButton();
        generateBatchBtn = new javax.swing.JButton();
        batchNoInput = new javax.swing.JTextField();
        sellingPrice = new javax.swing.JTextField();
        quantityInput = new javax.swing.JTextField();

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

        SupplierCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        SupplierCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        SupplierCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Supplier *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
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

        purchasePrice.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        purchasePrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Purchase Price *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        purchasePrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                purchasePriceKeyPressed(evt);
            }
        });

        lastPrice.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        lastPrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Last Price *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        lastPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lastPriceKeyPressed(evt);
            }
        });

        manufactureDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manufacture Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        manufactureDate.setDateFormatString("MM/dd/yyyy");
        manufactureDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        manufactureDate.setOpaque(false);
        manufactureDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                manufactureDateKeyPressed(evt);
            }
        });

        expriyDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Expriy Date *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        expriyDate.setDateFormatString("MM/dd/yyyy");
        expriyDate.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        expriyDate.setOpaque(false);
        expriyDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                expriyDateKeyPressed(evt);
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

        generateBatchBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        generateBatchBtn.setForeground(new java.awt.Color(153, 153, 153));
        generateBatchBtn.setBorder(null);
        generateBatchBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        generateBatchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateBatchBtnActionPerformed(evt);
            }
        });
        generateBatchBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                generateBatchBtnKeyPressed(evt);
            }
        });

        batchNoInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        batchNoInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Batch No *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        batchNoInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                batchNoInputKeyPressed(evt);
            }
        });

        sellingPrice.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        sellingPrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Selling Price *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        sellingPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sellingPriceKeyPressed(evt);
            }
        });

        quantityInput.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        quantityInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quantity *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        quantityInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                quantityInputKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(sellingPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(batchNoInput, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateBatchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(quantityInput, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(printBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jSeparator3)
                                .addComponent(jLabel3)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(productInput, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(addNewProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(SupplierCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(addNewSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(purchasePrice, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lastPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(addNewCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(brandCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(addNewBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(21, 21, 21))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addNewProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(productInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(addNewCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(brandCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewBrand, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SupplierCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(addNewSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(purchasePrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(quantityInput, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sellingPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(batchNoInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(generateBatchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            saveProductAndStock();
        } else {
            handleArrowNavigation(evt, updateBtn);
        }
    }//GEN-LAST:event_updateBtnKeyPressed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        saveProductAndStock();
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
            SupplierCombo.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN && !brandCombo.isPopupVisible()) {
            brandCombo.showPopup();
            evt.consume();
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
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN && !categoryCombo.isPopupVisible()) {
            categoryCombo.showPopup();
            evt.consume();
        } else {
            handleArrowNavigation(evt, categoryCombo);
        }
    }//GEN-LAST:event_categoryComboKeyPressed

    private void barcodeInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_barcodeInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // When all fields are filled, go directly to update button
            if (allFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
        } else {
            handleArrowNavigation(evt, barcodeInput);
        }
    }//GEN-LAST:event_barcodeInputKeyPressed

    private void barcodeInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeInputActionPerformed
        updateBtn.requestFocus();
    }//GEN-LAST:event_barcodeInputActionPerformed

    private void SupplierComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SupplierComboActionPerformed
        if (SupplierCombo.getSelectedIndex() > 0 && !SupplierCombo.isPopupVisible()) {
            purchasePrice.requestFocusInWindow();
        }
    }//GEN-LAST:event_SupplierComboActionPerformed

    private void SupplierComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SupplierComboKeyPressed
        if (SupplierCombo.isPopupVisible()) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                SupplierCombo.setPopupVisible(false);
                evt.consume();
            }
            return;
        }

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // Only move to next field if an item is actually selected (not the placeholder)
            if (SupplierCombo.getSelectedIndex() > 0) {
                // When all fields are filled, go directly to update button
                if (allFieldsFilled()) {
                    updateBtn.requestFocusInWindow();
                } else {
                    purchasePrice.requestFocusInWindow();
                }
            }
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN && !SupplierCombo.isPopupVisible()) {
            SupplierCombo.showPopup();
            evt.consume();
        } else {
            handleArrowNavigation(evt, SupplierCombo);
        }
    }//GEN-LAST:event_SupplierComboKeyPressed

    private void purchasePriceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_purchasePriceKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // When all fields are filled, go directly to update button
            if (allFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                lastPrice.requestFocusInWindow();
            }
        } else {
            handleArrowNavigation(evt, purchasePrice);
        }
    }//GEN-LAST:event_purchasePriceKeyPressed

    private void lastPriceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lastPriceKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // When all fields are filled, go directly to update button
            if (allFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                sellingPrice.requestFocusInWindow();
            }
        } else {
            handleArrowNavigation(evt, lastPrice);
        }
    }//GEN-LAST:event_lastPriceKeyPressed

    private void manufactureDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manufactureDateKeyPressed
        handleArrowNavigation(evt, manufactureDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_manufactureDateKeyPressed

    private void expriyDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_expriyDateKeyPressed
        handleArrowNavigation(evt, expriyDate.getDateEditor().getUiComponent());
    }//GEN-LAST:event_expriyDateKeyPressed

    private void addNewSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewSupplierActionPerformed
        openAddNewSupplier();
    }//GEN-LAST:event_addNewSupplierActionPerformed

    private void addNewSupplierKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewSupplierKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addNewSupplierActionPerformed(null);
        } else {
            handleArrowNavigation(evt, addNewSupplier);
        }
    }//GEN-LAST:event_addNewSupplierKeyPressed

    private void addNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewProductActionPerformed
        openAddNewProduct();
    }//GEN-LAST:event_addNewProductActionPerformed

    private void addNewProductKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addNewProductKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addNewProductActionPerformed(null);
        } else {
            handleArrowNavigation(evt, addNewProduct);
        }
    }//GEN-LAST:event_addNewProductKeyPressed

    private void generateBatchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateBatchBtnActionPerformed
        generateBatchNumber();
    }//GEN-LAST:event_generateBatchBtnActionPerformed

    private void generateBatchBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_generateBatchBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            generateBatchNumber();
        } else {
            handleArrowNavigation(evt, generateBatchBtn);
        }
    }//GEN-LAST:event_generateBatchBtnKeyPressed

    private void batchNoInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_batchNoInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (allFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                quantityInput.requestFocusInWindow();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_F3) {
            generateBatchNumber();
            evt.consume();
        } else {
            handleArrowNavigation(evt, batchNoInput);
        }
    }//GEN-LAST:event_batchNoInputKeyPressed

    private void sellingPriceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sellingPriceKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (allFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                batchNoInput.requestFocusInWindow();
            }
        } else {
            handleArrowNavigation(evt, sellingPrice);
        }
    }//GEN-LAST:event_sellingPriceKeyPressed

    private void quantityInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quantityInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (allFieldsFilled()) {
                updateBtn.requestFocusInWindow();
            } else {
                manufactureDate.getDateEditor().getUiComponent().requestFocusInWindow();
            }
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
            java.util.logging.Logger.getLogger(UpdateProductStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UpdateProductStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UpdateProductStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UpdateProductStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateProductStock dialog = new UpdateProductStock(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton addNewBrand;
    private javax.swing.JButton addNewCategory;
    private javax.swing.JButton addNewProduct;
    private javax.swing.JButton addNewSupplier;
    private javax.swing.JTextField barcodeInput;
    private javax.swing.JTextField batchNoInput;
    private javax.swing.JComboBox<String> brandCombo;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JComboBox<String> categoryCombo;
    private javax.swing.JButton clearFormBtn;
    private com.toedter.calendar.JDateChooser expriyDate;
    private javax.swing.JButton generateBatchBtn;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField lastPrice;
    private com.toedter.calendar.JDateChooser manufactureDate;
    private javax.swing.JButton printBarcode;
    private javax.swing.JTextField productInput;
    private javax.swing.JTextField purchasePrice;
    private javax.swing.JTextField quantityInput;
    private javax.swing.JTextField sellingPrice;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
