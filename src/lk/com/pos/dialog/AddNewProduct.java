package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

public class AddNewProduct extends javax.swing.JDialog {

    /**
     * Creates new form AddNewProduct
     */
    public AddNewProduct(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        loadCategoryCombo();
        loadBrandCombo();
        setLocationRelativeTo(parent);
        setupAutoCompleteWithNavigation();
        
        categoryCombo.setToolTipText("Use DOWN arrow to open dropdown, ENTER to select and move to next field");
        brandCombo.setToolTipText("Use DOWN arrow to open dropdown, ENTER to select and move to next field");
    }

    private void setupAutoCompleteWithNavigation() {
        // Setup auto-complete
        AutoCompleteDecorator.decorate(categoryCombo);
        AutoCompleteDecorator.decorate(brandCombo);
        
        // Add key listeners to the editor components for proper navigation
        if (categoryCombo.getEditor().getEditorComponent() != null) {
            categoryCombo.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    categoryEditorKeyPressed(evt);
                }
            });
        }
        
        if (brandCombo.getEditor().getEditorComponent() != null) {
            brandCombo.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    brandEditorKeyPressed(evt);
                }
            });
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

    private void formKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.dispose();
        }
    }
 private void categoryEditorKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // If popup is visible, let it handle the selection first
            if (!categoryCombo.isPopupVisible()) {
                brandCombo.requestFocus();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!categoryCombo.isPopupVisible()) {
                categoryCombo.showPopup();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!categoryCombo.isPopupVisible()) {
                productInput.requestFocus();
            }
        }
    }

    private void brandEditorKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // If popup is visible, let it handle the selection first
            if (!brandCombo.isPopupVisible()) {
                barcodeInput.requestFocus();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!brandCombo.isPopupVisible()) {
                brandCombo.showPopup();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (!brandCombo.isPopupVisible()) {
                categoryCombo.requestFocus();
            }
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
        scaneBarcode = new javax.swing.JButton();
        genarateBarecode = new javax.swing.JButton();
        productInput = new javax.swing.JTextField();
        addNewCategory = new javax.swing.JButton();
        addNewBrand = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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
        cancelBtn.setText("Cancel");
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
        saveBtn.setText("Save");
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
        clearFormBtn.setText("Clear Form");
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

        scaneBarcode.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        scaneBarcode.setText("B");

        genarateBarecode.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        genarateBarecode.setText("B");

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
        addNewCategory.setText("+");
        addNewCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewCategoryActionPerformed(evt);
            }
        });

        addNewBrand.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        addNewBrand.setText("+");
        addNewBrand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewBrandActionPerformed(evt);
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
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(genarateBarecode)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scaneBarcode)))
                        .addGap(22, 22, 22))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addNewCategory)
                                .addGap(18, 18, 18)
                                .addComponent(brandCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addNewBrand))
                            .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                            .addComponent(jLabel3)
                            .addComponent(productInput))
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
                    .addComponent(brandCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(addNewCategory)
                        .addComponent(addNewBrand)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaneBarcode)
                    .addComponent(genarateBarecode))
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
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            saveBtn.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            brandCombo.requestFocus();
        }
    }//GEN-LAST:event_barcodeInputKeyPressed

    private void categoryComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_categoryComboKeyPressed
        // When popup is visible, let the combo box handle navigation
        if (categoryCombo.isPopupVisible()) {
            // Only handle ENTER key when popup is visible
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                // Close the popup and move to next field
                categoryCombo.setPopupVisible(false);
                brandCombo.requestFocus();
                evt.consume(); // Prevent the event from being processed further
            }
            return;
        }
        
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                brandCombo.requestFocus();
                break;
            case KeyEvent.VK_DOWN:
                // Only open combo box if not already open
                if (!categoryCombo.isPopupVisible()) {
                    categoryCombo.showPopup();
                }
                break;
            case KeyEvent.VK_UP:
                productInput.requestFocus();
                break;
            case KeyEvent.VK_SPACE:
                // Open combo box when SPACE is pressed
                if (!categoryCombo.isPopupVisible()) {
                    categoryCombo.showPopup();
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
                barcodeInput.requestFocus();
                evt.consume(); // Prevent the event from being processed further
            }
            return;
        }
        
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                barcodeInput.requestFocus();
                break;
            case KeyEvent.VK_DOWN:
                // Only open combo box if not already open
                if (!brandCombo.isPopupVisible()) {
                    brandCombo.showPopup();
                }
                break;
            case KeyEvent.VK_UP:
                categoryCombo.requestFocus();
                break;
            case KeyEvent.VK_SPACE:
                // Open combo box when SPACE is pressed
                if (!brandCombo.isPopupVisible()) {
                    brandCombo.showPopup();
                }
                break;
        }
    }//GEN-LAST:event_brandComboKeyPressed

    private void productInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productInputKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            categoryCombo.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            categoryCombo.requestFocus();
        }
    }//GEN-LAST:event_productInputKeyPressed

    private void saveBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveProduct();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            barcodeInput.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            clearFormBtn.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            cancelBtn.requestFocus();
        }
    }//GEN-LAST:event_saveBtnKeyPressed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            barcodeInput.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            cancelBtn.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            saveBtn.requestFocus();
        }
    }//GEN-LAST:event_clearFormBtnKeyPressed

    private void cancelBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cancelBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.dispose();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            barcodeInput.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            saveBtn.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            clearFormBtn.requestFocus();
        }

    }//GEN-LAST:event_cancelBtnKeyPressed

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
    private javax.swing.JTextField productInput;
    private javax.swing.JButton saveBtn;
    private javax.swing.JButton scaneBarcode;
    // End of variables declaration//GEN-END:variables
}
