/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package lk.com.pos.dialog;

import lk.com.pos.connection.MySQL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import raven.toast.Notifications;

/**
 *
 * @author pasin
 */
public class AddNewProduct1 extends javax.swing.JDialog {

    private Connection connection;
    private Integer selectedProductId = null;
    
    /**
     * Creates new form AddNewProduct1
     */
    public AddNewProduct1(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDatabase();
        loadComboBoxData();
        setupKeyboardNavigation();
        updateFormState();
    }

    private void initializeDatabase() {
        try {
            connection = MySQL.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Database connection is not available");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadComboBoxData() {
        loadCategories();
        loadBrands();
        loadSuppliers();
        loadProducts();
    }

    private void loadCategories() {
        try {
            String sql = "SELECT category_id, category_name FROM category ORDER BY category_name";
            ResultSet rs = MySQL.executeSearch(sql);
            
            List<String> categories = new ArrayList<>();
            categories.add("-- Select Category --");
            
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
            
            categoryCombo.setModel(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private void loadBrands() {
        try {
            String sql = "SELECT brand_id, brand_name FROM brand ORDER BY brand_name";
            ResultSet rs = MySQL.executeSearch(sql);
            
            List<String> brands = new ArrayList<>();
            brands.add("-- Select Brand --");
            
            while (rs.next()) {
                brands.add(rs.getString("brand_name"));
            }
            
            brandCombo.setModel(new DefaultComboBoxModel<>(brands.toArray(new String[0])));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading brands: " + e.getMessage());
        }
    }

    private void loadSuppliers() {
        try {
            String sql = "SELECT suppliers_id, suppliers_name FROM suppliers ORDER BY suppliers_name";
            ResultSet rs = MySQL.executeSearch(sql);
            
            List<String> suppliers = new ArrayList<>();
            suppliers.add("-- Select Supplier --");
            
            while (rs.next()) {
                suppliers.add(rs.getString("suppliers_name"));
            }
            
            SupplierCombo.setModel(new DefaultComboBoxModel<>(suppliers.toArray(new String[0])));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            String sql = "SELECT product_id, product_name FROM product ORDER BY product_name";
            ResultSet rs = MySQL.executeSearch(sql);
            
            List<String> products = new ArrayList<>();
            products.add("-- Search Product --");
            
            while (rs.next()) {
                products.add(rs.getString("product_name"));
            }
            
            productNameCombo.setModel(new DefaultComboBoxModel<>(products.toArray(new String[0])));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }

    private void searchProduct(String searchText) {
        try {
            String sql = "SELECT product_id, product_name FROM product WHERE product_name LIKE ? ORDER BY product_name";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, "%" + searchText + "%");
            ResultSet rs = pst.executeQuery();
            
            List<String> products = new ArrayList<>();
            products.add("-- Search Product --");
            
            while (rs.next()) {
                products.add(rs.getString("product_name"));
            }
            
            productNameCombo.setModel(new DefaultComboBoxModel<>(products.toArray(new String[0])));
            if (!searchText.isEmpty()) {
                productNameCombo.showPopup();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching products: " + e.getMessage());
        }
    }

    private void setupKeyboardNavigation() {
        // Set up Enter key navigation
        setupEnterKeyNavigation();
        
        // Set up Arrow key navigation
        setupArrowKeyNavigation();
        
        // Make comboboxes editable for adding new items
        makeCombosEditable();
    }

    private void setupEnterKeyNavigation() {
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    if (e.getSource() == productNameCombo) {
                        handleProductNameEnter();
                    } else if (e.getSource() == categoryCombo) {
                        handleCategoryEnter();
                    } else if (e.getSource() == brandCombo) {
                        handleBrandEnter();
                    } else if (e.getSource() == SupplierCombo) {
                        handleSupplierEnter();
                    } else {
                        moveToNextComponent(e.getComponent());
                    }
                }
            }
        };

        // Add to all components
        productNameCombo.addKeyListener(enterKeyAdapter);
        categoryCombo.addKeyListener(enterKeyAdapter);
        brandCombo.addKeyListener(enterKeyAdapter);
        purchasePrice.addKeyListener(enterKeyAdapter);
        lastPrice.addKeyListener(enterKeyAdapter);
        sellingPrice.addKeyListener(enterKeyAdapter);
        quantity.addKeyListener(enterKeyAdapter);
        manufactureDate.addKeyListener(enterKeyAdapter);
        expriyDate.addKeyListener(enterKeyAdapter);
        batchNoInput.addKeyListener(enterKeyAdapter);
        barcodeInput.addKeyListener(enterKeyAdapter);
        SupplierCombo.addKeyListener(enterKeyAdapter);
    }

    private void setupArrowKeyNavigation() {
        KeyAdapter arrowKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    moveToNextComponent(e.getComponent());
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    moveToPreviousComponent(e.getComponent());
                    e.consume();
                }
            }
        };

        // Add to all components
        productNameCombo.addKeyListener(arrowKeyAdapter);
        categoryCombo.addKeyListener(arrowKeyAdapter);
        brandCombo.addKeyListener(arrowKeyAdapter);
        purchasePrice.addKeyListener(arrowKeyAdapter);
        lastPrice.addKeyListener(arrowKeyAdapter);
        sellingPrice.addKeyListener(arrowKeyAdapter);
        quantity.addKeyListener(arrowKeyAdapter);
        manufactureDate.addKeyListener(arrowKeyAdapter);
        expriyDate.addKeyListener(arrowKeyAdapter);
        batchNoInput.addKeyListener(arrowKeyAdapter);
        barcodeInput.addKeyListener(arrowKeyAdapter);
        SupplierCombo.addKeyListener(arrowKeyAdapter);
    }

    private void makeCombosEditable() {
        productNameCombo.setEditable(true);
        categoryCombo.setEditable(true);
        brandCombo.setEditable(true);
        SupplierCombo.setEditable(true);
    }

    private void moveToNextComponent(java.awt.Component currentComponent) {
        if (currentComponent == productNameCombo) categoryCombo.requestFocus();
        else if (currentComponent == categoryCombo) brandCombo.requestFocus();
        else if (currentComponent == brandCombo) purchasePrice.requestFocus();
        else if (currentComponent == purchasePrice) lastPrice.requestFocus();
        else if (currentComponent == lastPrice) sellingPrice.requestFocus();
        else if (currentComponent == sellingPrice) quantity.requestFocus();
        else if (currentComponent == quantity) manufactureDate.requestFocus();
        else if (currentComponent == manufactureDate) expriyDate.requestFocus();
        else if (currentComponent == expriyDate) batchNoInput.requestFocus();
        else if (currentComponent == batchNoInput) barcodeInput.requestFocus();
        else if (currentComponent == barcodeInput) SupplierCombo.requestFocus();
        else if (currentComponent == SupplierCombo) saveBtn.requestFocus();
    }

    private void moveToPreviousComponent(java.awt.Component currentComponent) {
        if (currentComponent == categoryCombo) productNameCombo.requestFocus();
        else if (currentComponent == brandCombo) categoryCombo.requestFocus();
        else if (currentComponent == purchasePrice) brandCombo.requestFocus();
        else if (currentComponent == lastPrice) purchasePrice.requestFocus();
        else if (currentComponent == sellingPrice) lastPrice.requestFocus();
        else if (currentComponent == quantity) sellingPrice.requestFocus();
        else if (currentComponent == manufactureDate) quantity.requestFocus();
        else if (currentComponent == expriyDate) manufactureDate.requestFocus();
        else if (currentComponent == batchNoInput) expriyDate.requestFocus();
        else if (currentComponent == barcodeInput) batchNoInput.requestFocus();
        else if (currentComponent == SupplierCombo) barcodeInput.requestFocus();
        else if (currentComponent == saveBtn) SupplierCombo.requestFocus();
    }

    private void handleProductNameEnter() {
        String enteredText = productNameCombo.getEditor().getItem().toString().trim();
        if (!enteredText.isEmpty() && !enteredText.equals("-- Search Product --")) {
            // Check if product exists
            boolean productExists = checkIfProductExists(enteredText);
            if (productExists) {
                // Product exists, select it
                selectExistingProduct(enteredText);
            } else {
                // New product, move to next field
                selectedProductId = null;
                updateFormState();
                categoryCombo.requestFocus();
                Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, 
                    "New product detected. Please fill in details.");
            }
        } else {
            categoryCombo.requestFocus();
        }
    }

    private void handleCategoryEnter() {
        String enteredText = categoryCombo.getEditor().getItem().toString().trim();
        if (!enteredText.isEmpty() && !enteredText.equals("-- Select Category --")) {
            addNewCategoryIfNotExists(enteredText);
        }
        brandCombo.requestFocus();
    }

    private void handleBrandEnter() {
        String enteredText = brandCombo.getEditor().getItem().toString().trim();
        if (!enteredText.isEmpty() && !enteredText.equals("-- Select Brand --")) {
            addNewBrandIfNotExists(enteredText);
        }
        purchasePrice.requestFocus();
    }

    private void handleSupplierEnter() {
        String enteredText = SupplierCombo.getEditor().getItem().toString().trim();
        if (!enteredText.isEmpty() && !enteredText.equals("-- Select Supplier --")) {
            addNewSupplierIfNotExists(enteredText);
        }
        saveBtn.requestFocus();
    }

    private boolean checkIfProductExists(String productName) {
        try {
            String sql = "SELECT product_id FROM product WHERE product_name = ?";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, productName);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void selectExistingProduct(String productName) {
        try {
            String sql = "SELECT product_id FROM product WHERE product_name = ?";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, productName);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                selectedProductId = rs.getInt("product_id");
                updateFormState();
                purchasePrice.requestFocus();
                Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, 
                    "Existing product selected. Fill stock details only.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error selecting product: " + e.getMessage());
        }
    }

    private void updateFormState() {
        boolean isExistingProduct = selectedProductId != null;
        
        categoryCombo.setVisible(!isExistingProduct);
        brandCombo.setVisible(!isExistingProduct);
        barcodeInput.setVisible(!isExistingProduct);
        jButton1.setVisible(!isExistingProduct);
        
        if (isExistingProduct) {
            productNameCombo.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Product"));
        } else {
            productNameCombo.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Product"));
        }
    }

    private boolean validateForm() {
        // Validate product name
        String productName = productNameCombo.getEditor().getItem().toString().trim();
        if (productName.isEmpty() || productName.equals("-- Search Product --")) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Please enter a product name!");
            productNameCombo.requestFocus();
            return false;
        }

        // Validate category for new products
        if (selectedProductId == null) {
            String category = categoryCombo.getEditor().getItem().toString().trim();
            if (category.isEmpty() || category.equals("-- Select Category --")) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Please select or enter a category!");
                categoryCombo.requestFocus();
                return false;
            }

            String brand = brandCombo.getEditor().getItem().toString().trim();
            if (brand.isEmpty() || brand.equals("-- Select Brand --")) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Please select or enter a brand!");
                brandCombo.requestFocus();
                return false;
            }
        }

        // Validate supplier
        String supplier = SupplierCombo.getEditor().getItem().toString().trim();
        if (supplier.isEmpty() || supplier.equals("-- Select Supplier --")) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Please select or enter a supplier!");
            SupplierCombo.requestFocus();
            return false;
        }

        // Validate prices
        try {
            double purchasePriceVal = Double.parseDouble(purchasePrice.getText().trim());
            double sellingPriceVal = Double.parseDouble(sellingPrice.getText().trim());
            double lastPriceVal = lastPrice.getText().trim().isEmpty() ? 0 : Double.parseDouble(lastPrice.getText().trim());
            
            if (purchasePriceVal <= 0 || sellingPriceVal <= 0) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Prices must be greater than 0!");
                return false;
            }
            
            if (sellingPriceVal <= purchasePriceVal) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Selling price must be greater than purchase price!");
                sellingPrice.requestFocus();
                return false;
            }
            
            if (lastPriceVal > 0 && sellingPriceVal <= lastPriceVal) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Selling price must be greater than last price!");
                sellingPrice.requestFocus();
                return false;
            }
            
        } catch (NumberFormatException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Please enter valid prices!");
            return false;
        }

        // Validate quantity
        int quantityVal = (Integer) quantity.getValue();
        if (quantityVal < 0) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Quantity cannot be negative!");
            quantity.requestFocus();
            return false;
        }

        // Validate dates
        if (manufactureDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Please select manufacture date!");
            manufactureDate.requestFocus();
            return false;
        }

        if (expriyDate.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Please select expiry date!");
            expriyDate.requestFocus();
            return false;
        }

        if (expriyDate.getDate().before(manufactureDate.getDate())) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Expiry date cannot be before manufacture date!");
            expriyDate.requestFocus();
            return false;
        }

        return true;
    }

    private void saveProduct() {
        if (!validateForm()) {
            return;
        }

        try {
            connection.setAutoCommit(false);
            
            Integer productId = selectedProductId;
            
            // If it's a new product, insert into product table first
            if (productId == null) {
                productId = insertProduct();
                if (productId == null) {
                    connection.rollback();
                    return;
                }
            }
            
            // Insert into stock table
            if (insertStock(productId)) {
                connection.commit();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Product added to stock successfully!");
                clearForm();
            } else {
                connection.rollback();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving stock information!");
            }
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error saving product: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Integer insertProduct() throws SQLException {
        String productName = productNameCombo.getEditor().getItem().toString().trim();
        String categoryName = categoryCombo.getEditor().getItem().toString().trim();
        String brandName = brandCombo.getEditor().getItem().toString().trim();
        
        // First, ensure category and brand exist
        Integer categoryId = getOrCreateCategoryId(categoryName);
        Integer brandId = getOrCreateBrandId(brandName);
        
        if (categoryId == null || brandId == null) {
            return null;
        }

        String sql = "INSERT INTO product (product_name, brand_id, category_id, p_status_id, barcode) VALUES (?, ?, ?, 1, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, productName);
            pst.setInt(2, brandId);
            pst.setInt(3, categoryId);
            pst.setString(4, barcodeInput.getText().trim());
            
            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return null;
    }

    private Integer getOrCreateCategoryId(String categoryName) throws SQLException {
        // Check if category exists
        String checkSql = "SELECT category_id FROM category WHERE category_name = ?";
        try (PreparedStatement pst = connection.prepareStatement(checkSql)) {
            pst.setString(1, categoryName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        }
        
        // Create new category
        String insertSql = "INSERT INTO category (category_name) VALUES (?)";
        try (PreparedStatement pst = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, categoryName);
            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return null;
    }

    private Integer getOrCreateBrandId(String brandName) throws SQLException {
        // Check if brand exists
        String checkSql = "SELECT brand_id FROM brand WHERE brand_name = ?";
        try (PreparedStatement pst = connection.prepareStatement(checkSql)) {
            pst.setString(1, brandName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("brand_id");
            }
        }
        
        // Create new brand
        String insertSql = "INSERT INTO brand (brand_name) VALUES (?)";
        try (PreparedStatement pst = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, brandName);
            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return null;
    }

    private Integer getOrCreateSupplierId(String supplierName) throws SQLException {
        // Check if supplier exists
        String checkSql = "SELECT suppliers_id FROM suppliers WHERE suppliers_name = ?";
        try (PreparedStatement pst = connection.prepareStatement(checkSql)) {
            pst.setString(1, supplierName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("suppliers_id");
            }
        }
        
        // Create new supplier
        String insertSql = "INSERT INTO suppliers (suppliers_name) VALUES (?)";
        try (PreparedStatement pst = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, supplierName);
            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return null;
    }

    private boolean insertStock(Integer productId) throws SQLException {
        String supplierName = SupplierCombo.getEditor().getItem().toString().trim();
        Integer supplierId = getOrCreateSupplierId(supplierName);
        
        if (supplierId == null) {
            return false;
        }

        String sql = "INSERT INTO stock (product_id, suppliers_id, purchase_price, selling_price, quantity, manufacture_date, expriy_date, batch_no, last_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, productId);
            pst.setInt(2, supplierId);
            pst.setDouble(3, Double.parseDouble(purchasePrice.getText().trim()));
            pst.setDouble(4, Double.parseDouble(sellingPrice.getText().trim()));
            pst.setInt(5, (Integer) quantity.getValue());
            pst.setDate(6, new java.sql.Date(manufactureDate.getDate().getTime()));
            pst.setDate(7, new java.sql.Date(expriyDate.getDate().getTime()));
            pst.setString(8, batchNoInput.getText().trim());
            
            double lastPriceVal = lastPrice.getText().trim().isEmpty() ? 0 : Double.parseDouble(lastPrice.getText().trim());
            pst.setDouble(9, lastPriceVal);
            
            return pst.executeUpdate() > 0;
        }
    }

    private void addNewCategoryIfNotExists(String categoryName) {
        try {
            String checkSql = "SELECT category_id FROM category WHERE category_name = ?";
            PreparedStatement checkPst = connection.prepareStatement(checkSql);
            checkPst.setString(1, categoryName);
            ResultSet rs = checkPst.executeQuery();
            
            if (!rs.next()) {
                // Category doesn't exist, add it
                String insertSql = "INSERT INTO category (category_name) VALUES (?)";
                PreparedStatement insertPst = connection.prepareStatement(insertSql);
                insertPst.setString(1, categoryName);
                insertPst.executeUpdate();
                
                loadCategories();
                categoryCombo.setSelectedItem(categoryName);
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "New category added: " + categoryName);
            }
        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error adding category: " + e.getMessage());
        }
    }

    private void addNewBrandIfNotExists(String brandName) {
        try {
            String checkSql = "SELECT brand_id FROM brand WHERE brand_name = ?";
            PreparedStatement checkPst = connection.prepareStatement(checkSql);
            checkPst.setString(1, brandName);
            ResultSet rs = checkPst.executeQuery();
            
            if (!rs.next()) {
                // Brand doesn't exist, add it
                String insertSql = "INSERT INTO brand (brand_name) VALUES (?)";
                PreparedStatement insertPst = connection.prepareStatement(insertSql);
                insertPst.setString(1, brandName);
                insertPst.executeUpdate();
                
                loadBrands();
                brandCombo.setSelectedItem(brandName);
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "New brand added: " + brandName);
            }
        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error adding brand: " + e.getMessage());
        }
    }

    private void addNewSupplierIfNotExists(String supplierName) {
        try {
            String checkSql = "SELECT suppliers_id FROM suppliers WHERE suppliers_name = ?";
            PreparedStatement checkPst = connection.prepareStatement(checkSql);
            checkPst.setString(1, supplierName);
            ResultSet rs = checkPst.executeQuery();
            
            if (!rs.next()) {
                // Supplier doesn't exist, add it
                String insertSql = "INSERT INTO suppliers (suppliers_name) VALUES (?)";
                PreparedStatement insertPst = connection.prepareStatement(insertSql);
                insertPst.setString(1, supplierName);
                insertPst.executeUpdate();
                
                loadSuppliers();
                SupplierCombo.setSelectedItem(supplierName);
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "New supplier added: " + supplierName);
            }
        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error adding supplier: " + e.getMessage());
        }
    }

    // Rest of the code remains the same as previous version...
    // [The rest of the GUI code and event handlers remain unchanged from the previous version]

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        categoryCombo = new javax.swing.JComboBox<>();
        brandCombo = new javax.swing.JComboBox<>();
        purchasePrice = new javax.swing.JTextField();
        lastPrice = new javax.swing.JTextField();
        sellingPrice = new javax.swing.JTextField();
        quantity = new javax.swing.JSpinner();
        manufactureDate = new com.toedter.calendar.JDateChooser();
        expriyDate = new com.toedter.calendar.JDateChooser();
        barcodeInput = new javax.swing.JTextField();
        batchNoInput = new javax.swing.JTextField();
        SupplierCombo = new javax.swing.JComboBox<>();
        cancelBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        productNameCombo = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(8, 147, 176));
        jLabel1.setText("Add New Product");

        jSeparator1.setForeground(new java.awt.Color(0, 137, 176));

        categoryCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        categoryCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Category", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        brandCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        brandCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Brand", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        purchasePrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Purchase Price", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        lastPrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Last Price", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        sellingPrice.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Selling Price", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        quantity.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quantity", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        manufactureDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manufacture Date", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        manufactureDate.setOpaque(false);

        expriyDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Expriy Date", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        expriyDate.setOpaque(false);

        barcodeInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Barcode", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        batchNoInput.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Batch No", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        SupplierCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        SupplierCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        SupplierCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Supplier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        saveBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        saveBtn.setText("Save");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        clearFormBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        clearFormBtn.setText("Clear Form");
        clearFormBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFormBtnActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        jButton1.setText("B");

        productNameCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        productNameCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        productNameCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Supplier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator1)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(brandCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(purchasePrice, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(lastPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(sellingPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(quantity, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(SupplierCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                            .addComponent(batchNoInput, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jButton1)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(18, 18, 18))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(productNameCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(productNameCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(brandCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(purchasePrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sellingPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manufactureDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expriyDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(barcodeInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchNoInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(SupplierCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        saveProduct();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
         clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void productNameComboActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // When user selects a product from dropdown
        if (productNameCombo.getSelectedIndex() > 0) {
            String selectedProduct = productNameCombo.getSelectedItem().toString();
            try {
                String sql = "SELECT product_id FROM product WHERE product_name = ?";
                PreparedStatement pst = connection.prepareStatement(sql);
                pst.setString(1, selectedProduct);
                ResultSet rs = pst.executeQuery();
                
                if (rs.next()) {
                    selectedProductId = rs.getInt("product_id");
                    updateFormState();
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, 
                        "Existing product selected. Fill stock details only.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error selecting product: " + e.getMessage());
            }
        } else {
            selectedProductId = null;
            updateFormState();
        }
    }                                                

    private void productNameComboKeyReleased(java.awt.event.KeyEvent evt) {                                             
        // Real-time search as user types
        String searchText = productNameCombo.getEditor().getItem().toString();
        if (searchText.length() >= 2) {
            searchProduct(searchText);
        }
    }                                            

    private void categoryComboActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // Add new category if not exists
        if (categoryCombo.getSelectedIndex() == -1 && !categoryCombo.getSelectedItem().toString().isEmpty()) {
            addNewCategory(categoryCombo.getSelectedItem().toString());
        }
    }                                             

    private void brandComboActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // Add new brand if not exists
        if (brandCombo.getSelectedIndex() == -1 && !brandCombo.getSelectedItem().toString().isEmpty()) {
            addNewBrand(brandCombo.getSelectedItem().toString());
        }
    }                                          

    private void SupplierComboActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // Add new supplier if not exists
        if (SupplierCombo.getSelectedIndex() == -1 && !SupplierCombo.getSelectedItem().toString().isEmpty()) {
            addNewSupplier(SupplierCombo.getSelectedItem().toString());
        }
    }                                             

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // Generate barcode
        String barcode = generateBarcode();
        barcodeInput.setText(barcode);
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Barcode generated!");
    }                                        

    private void clearForm() {
        productNameCombo.setSelectedIndex(0);
        categoryCombo.setSelectedIndex(0);
        brandCombo.setSelectedIndex(0);
        SupplierCombo.setSelectedIndex(0);
        purchasePrice.setText("");
        lastPrice.setText("");
        sellingPrice.setText("");
        quantity.setValue(0);
        manufactureDate.setDate(null);
        expriyDate.setDate(null);
        barcodeInput.setText("");
        batchNoInput.setText("");
        
        selectedProductId = null;
        updateFormState();
        
        productNameCombo.requestFocus();
    }

    private void addNewCategory(String categoryName) {
        try {
            String sql = "INSERT INTO category (category_name) VALUES (?)";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, categoryName);
            pst.executeUpdate();
            
            loadCategories();
            categoryCombo.setSelectedItem(categoryName);
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "New category added!");
        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error adding category: " + e.getMessage());
        }
    }

    private void addNewBrand(String brandName) {
        try {
            String sql = "INSERT INTO brand (brand_name) VALUES (?)";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, brandName);
            pst.executeUpdate();
            
            loadBrands();
            brandCombo.setSelectedItem(brandName);
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "New brand added!");
        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error adding brand: " + e.getMessage());
        }
    }

    private void addNewSupplier(String supplierName) {
        try {
            String sql = "INSERT INTO suppliers (suppliers_name) VALUES (?)";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, supplierName);
            pst.executeUpdate();
            
            loadSuppliers();
            SupplierCombo.setSelectedItem(supplierName);
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "New supplier added!");
        } catch (SQLException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error adding supplier: " + e.getMessage());
        }
    }

    private String generateBarcode() {
        return "BC" + System.currentTimeMillis();
    }

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
            java.util.logging.Logger.getLogger(AddNewProduct1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddNewProduct1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddNewProduct1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddNewProduct1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddNewProduct1 dialog = new AddNewProduct1(new javax.swing.JFrame(), true);
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
    private javax.swing.JTextField barcodeInput;
    private javax.swing.JTextField batchNoInput;
    private javax.swing.JComboBox<String> brandCombo;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JComboBox<String> categoryCombo;
    private javax.swing.JButton clearFormBtn;
    private com.toedter.calendar.JDateChooser expriyDate;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField lastPrice;
    private com.toedter.calendar.JDateChooser manufactureDate;
    private javax.swing.JComboBox<String> productNameCombo;
    private javax.swing.JTextField purchasePrice;
    private javax.swing.JSpinner quantity;
    private javax.swing.JButton saveBtn;
    private javax.swing.JTextField sellingPrice;
    // End of variables declaration//GEN-END:variables
}
