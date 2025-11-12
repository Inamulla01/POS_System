package lk.com.pos.dialog;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class PrintProductLabel extends javax.swing.JDialog {

    private String barcode; // Add this field to store barcode
    private String prdName;
    private double sellPrice;
  private String businessName = "Avinam PharmaX (PVT) LTD"; // Set your business name here
//    public PrintProductLabel(java.awt.Frame parent, boolean modal) {
//        super(parent, modal);
//        initComponents();
//
//    }
//
//    public PrintProductLabel(java.awt.Frame parent, boolean modal, String barcode, String productName, double sellingPrice) {
//        super(parent, modal);
//        this.barcode = barcode; // Store the barcode
//        initComponents();
//
//        barcodeField.setText(barcode);
//        this.productName.setText(productName);
//        barcodeField.setEditable(false);
//        this.productName.setEditable(false);
//        
//
//    }
    
      

    // Paper configuration class
    class PaperConfig {
        String name;
        double width; // in points (1mm = 2.83465 points)
        double height;
        int columns;
        int rows;
        double labelWidth;
        double labelHeight;

        PaperConfig(String name, double widthMM, double heightMM, int columns, int rows) {
            this.name = name;
            this.width = mmToPoints(widthMM);
            this.height = mmToPoints(heightMM);
            this.columns = columns;
            this.rows = rows;
            this.labelWidth = this.width / columns;
            this.labelHeight = this.height / rows;
        }
    }

    private PaperConfig[] paperConfigs = {
        new PaperConfig("A4 21up 70mm x 42.4mm", 210, 297, 3, 7), // 21 labels
        new PaperConfig("A4 24up 70mm x 37mm", 210, 297, 3, 8), // 24 labels
        new PaperConfig("A4 30up 70mm x 299.7mm", 210, 297, 3, 10), // 30 labels
        new PaperConfig("A4 44up 48.5mm x 25.4mm", 210, 297, 4, 11), // 44 labels
        new PaperConfig("A4 56up 52.5mm x 21mm", 210, 297, 4, 14), // 56 labels
        new PaperConfig("A4 65up 38mm x 21mm", 210, 297, 5, 13), // 65 labels
        new PaperConfig("A4 68up 48mm x 16.6mm", 210, 297, 4, 17) // 68 labels
    };

    public PrintProductLabel(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializePaperTypes();
    }

    public PrintProductLabel(java.awt.Frame parent, boolean modal, String barcode, String productName, double sellingPrice) {
        super(parent, modal);
        this.barcode = barcode; // Store the barcode
        this.prdName = productName;
        this.sellPrice = sellingPrice;
        initComponents();
        initializePaperTypes();

        barcodeField.setText(barcode);
        this.productName.setText(productName);
        barcodeField.setEditable(false);
        this.productName.setEditable(false);
    }

    private void initializePaperTypes() {
        printType.removeAllItems();
        printType.addItem("Select Print Paper Type");
        for (PaperConfig config : paperConfigs) {
            printType.addItem(config.name);
        }
    }

    private void printBarcodeLabel() {
        if (!validateInputs()) {
            return;
        }

        int totalLabels = Integer.parseInt(barcodePrintQtyField.getText());
        String selectedPaperType = (String) printType.getSelectedItem();

        // Find the selected paper configuration
        PaperConfig selectedConfig = null;
        for (PaperConfig config : paperConfigs) {
            if (config.name.equals(selectedPaperType)) {
                selectedConfig = config;
                break;
            }
        }

        if (selectedConfig == null) {
            JOptionPane.showMessageDialog(this, "Invalid paper type selected");
            return;
        }

        // Create printer job
        PrinterJob printerJob = PrinterJob.getPrinterJob();

        // Create book for multiple pages
        Book book = new Book();
        PageFormat pageFormat = createPageFormat(selectedConfig);

        int labelsPerPage = selectedConfig.columns * selectedConfig.rows;
        int totalPages = (int) Math.ceil((double) totalLabels / labelsPerPage);

        // Add pages based on total labels needed
        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            int startLabel = pageIndex * labelsPerPage;
            int endLabel = Math.min(startLabel + labelsPerPage, totalLabels);
            book.append(new BarcodePrintable(selectedConfig, startLabel, endLabel), pageFormat);
        }

        printerJob.setPageable(book);

        try {
            printerJob.print();
            JOptionPane.showMessageDialog(this,
                    "Printed " + totalLabels + " barcode label(s) successfully!");
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Printing failed: " + ex.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Inner class for printable content
    class BarcodePrintable implements Printable {
        private PaperConfig config;
        private int startLabel;
        private int endLabel;

        public BarcodePrintable(PaperConfig config, int startLabel, int endLabel) {
            this.config = config;
            this.startLabel = startLabel;
            this.endLabel = endLabel;
        }

        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
                throws PrinterException {
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Set up fonts and colors
            g2d.setColor(Color.BLACK);

            // Draw all labels for this page
            drawLabelsOnPage(g2d);

            return PAGE_EXISTS;
        }

        private void drawLabelsOnPage(Graphics2D g2d) {
            int labelsPerPage = config.columns * config.rows;
            int labelsToDraw = endLabel - startLabel;

            for (int labelIndex = 0; labelIndex < labelsToDraw; labelIndex++) {
                int row = labelIndex / config.columns;
                int col = labelIndex % config.columns;

                double x = col * config.labelWidth;
                double y = row * config.labelHeight;

                drawSingleLabel(g2d, x, y, config.labelWidth, config.labelHeight);
            }
        }

        private void drawSingleLabel(Graphics2D g2d, double x, double y, double width, double height) {
            // Increased margins for each label - 10 points on all sides
            int margin = 8;
            int labelX = (int) x + margin;
            int labelY = (int) y + margin;
            int labelWidth = (int) width - (margin * 2);
            int labelHeight = (int) height - (margin * 2);

            // Get data based on user selection
            String barcode = barcodeField.getText();
            String businessNameText = includeBusinessNameCheckbox.isSelected() ? businessName : "";
            String productNameText = includeProductNameCheckbox.isSelected() ? productName.getText() : "";
            String priceText = includePriceCheckbox.isSelected() ? String.format("Rs.%.2f", sellPrice) : "";

            // Calculate starting position
            int currentY = labelY + 8;

            // Draw business name (if selected)
            if (!businessNameText.isEmpty()) {
                Font businessFont = new Font("Arial", Font.BOLD, getFontSizeForHeight(labelHeight / 15));
                g2d.setFont(businessFont);
                drawCenteredString(g2d, businessNameText, labelX, currentY, labelWidth);
                currentY += getStringHeight(g2d, businessNameText) + 2;
            }

            // Draw product name (if selected)
            if (!productNameText.isEmpty()) {
                Font productFont = new Font("Arial", Font.BOLD, getFontSizeForHeight(labelHeight / 18));
                g2d.setFont(productFont);
                drawCenteredString(g2d, productNameText, labelX, currentY, labelWidth);
                currentY += getStringHeight(g2d, productNameText) + 2;
            }

            // Draw price (if selected)
            if (!priceText.isEmpty()) {
                Font priceFont = new Font("Arial", Font.BOLD, getFontSizeForHeight(labelHeight / 18));
                g2d.setFont(priceFont);
                drawCenteredString(g2d, priceText, labelX, currentY, labelWidth);
                currentY += getStringHeight(g2d, priceText) - 2;
            }

            // Draw barcode lines
            int barcodeHeight = (int) (labelHeight * 0.25);
            drawCode128Barcode(g2d, barcode, labelX, currentY, labelWidth, barcodeHeight);
            currentY += barcodeHeight + 5;

            // Draw barcode number BELOW the barcode
            Font barcodeFont = new Font("Arial", Font.PLAIN, getFontSizeForHeight(labelHeight / 20));
            g2d.setFont(barcodeFont);
            drawCenteredString(g2d, barcode, labelX, currentY, labelWidth);
        }

        private void drawCode128Barcode(Graphics2D g2d, String barcode, int x, int y, int width, int height) {
            int barcodeHeight = height;

            // Reduce barcode width dynamically based on label width
            double widthRatio = width > 150 ? 0.85 : width > 100 ? 0.75 : 0.65;
            int barcodeWidth = (int) (width * widthRatio);
            int barcodeX = x + (width - barcodeWidth) / 2; // Centered

            // Generate barcode pattern
            String code128Pattern = generateCode128Pattern(barcode);

            // Ensure minimum module width = 1, max = 2 px
            int moduleWidth = Math.max(1, Math.min(2, barcodeWidth / code128Pattern.length()));

            // Ensure barcode doesn't overflow label width
            int maxBars = barcodeWidth / moduleWidth;
            if (code128Pattern.length() > maxBars) {
                code128Pattern = code128Pattern.substring(0, maxBars);
            }

            // Draw bars
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < code128Pattern.length(); i++) {
                if (code128Pattern.charAt(i) == '1') {
                    int barX = barcodeX + (i * moduleWidth);
                    g2d.fillRect(barX, y, moduleWidth, barcodeHeight);
                }
            }
        }

        private void drawCenteredString(Graphics2D g2d, String text, int x, int y, int width) {
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textX = x + (width - textWidth) / 2;
            g2d.drawString(text, textX, y);
        }

        private int getStringHeight(Graphics2D g2d, String text) {
            FontMetrics fm = g2d.getFontMetrics();
            return fm.getHeight();
        }

        private int getFontSizeForHeight(double desiredHeight) {
            return Math.max(6, (int) (desiredHeight * 0.7));
        }

        private String generateCode128Pattern(String data) {
            // Code 128 character encoding (simplified version)
            String pattern = "11010010000"; // Start code B

            // Encode each character
            for (char c : data.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    pattern += getCode128CharPattern(c - '0' + 16);
                } else if (c >= 'A' && c <= 'Z') {
                    pattern += getCode128CharPattern(c - 'A' + 17);
                } else if (c >= 'a' && c <= 'z') {
                    pattern += getCode128CharPattern(c - 'a' + 49);
                } else {
                    pattern += getCode128CharPattern(0); // Space
                }
            }

            // Calculate checksum
            int checksum = 104; // Start B code value
            for (int i = 0; i < data.length(); i++) {
                char c = data.charAt(i);
                if (c >= '0' && c <= '9') {
                    checksum += (c - '0' + 16) * (i + 1);
                } else if (c >= 'A' && c <= 'Z') {
                    checksum += (c - 'A' + 17) * (i + 1);
                } else if (c >= 'a' && c <= 'z') {
                    checksum += (c - 'a' + 49) * (i + 1);
                } else {
                    checksum += 0 * (i + 1);
                }
            }
            checksum = checksum % 103;
            pattern += getCode128CharPattern(checksum);

            // Stop code
            pattern += "1100011101011";

            return pattern;
        }

        private String getCode128CharPattern(int value) {
            String[] patterns = {
                "11011001100", "11001101100", "11001100110", "10010011000", "10010001100",
                "10001001100", "10011001000", "10011000100", "10001100100", "11001001000",
                "11001000100", "11000100100", "10110011100", "10011011100", "10011001110",
                "10111001100", "10011101100", "10011100110", "11001110010", "11001011100",
                "11001001110", "11011100100", "11001110100", "11101101110", "11101001100",
                "11100101100", "11100100110", "11101100100", "11100110100", "11100110010",
                "11011011000", "11011000110", "11000110110", "10100011000", "10001011000",
                "10001000110", "10110001000", "10001101000", "10001100010", "11010001000",
                "11000101000", "11000100010", "10110111000", "10110001110", "10001101110",
                "10111011000", "10111000110", "10001110110", "11101110110", "11010001110",
                "11000101110", "11011101000", "11011100010", "11011101110", "11101011000",
                "11101000110", "11100010110", "11101101000", "11101100010", "11100011010",
                "11101111010", "11001000010", "11110001010", "10100110000", "10100001100",
                "10010110000", "10010000110", "10000101100", "10000100110", "10110010000",
                "10110000100", "10011010000", "10011000010", "10000110100", "10000110010",
                "11000010010", "11001010000", "11110111010", "11000010100", "10001111010",
                "10100111100", "10010111100", "10010011110", "10111100100", "10011110100",
                "10011110010", "11110100100", "11110010100", "11110010010", "11011011110",
                "11011110110", "11110110110", "10101111000", "10100011110", "10001011110",
                "10111101000", "10111100010", "11110101000", "11110100010", "10111011110",
                "10111101110", "11101011110", "11110101110", "11010000100", "11010010000",
                "11010011100", "11000111010"
            };

            if (value >= 0 && value < patterns.length) {
                return patterns[value];
            }
            return patterns[0];
        }
    }

    private PageFormat createPageFormat(PaperConfig config) {
        PageFormat format = new PageFormat();
        Paper paper = new Paper();

        paper.setSize(config.width, config.height);
        paper.setImageableArea(0, 0, config.width, config.height);

        format.setPaper(paper);
        format.setOrientation(PageFormat.PORTRAIT);
        return format;
    }

    private double mmToPoints(double mm) {
        return mm * 2.83465;
    }

    private boolean validateInputs() {
        if (printType.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a paper type",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (barcodeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a barcode",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            int qty = Integer.parseInt(barcodePrintQtyField.getText());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Print quantity must be greater than 0",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid print quantity",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    // Setter for business name
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
    
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        barcodeField = new javax.swing.JTextField();
        printBtn = new javax.swing.JButton();
        barcodePrintQtyField = new javax.swing.JTextField();
        includeBusinessNameCheckbox = new javax.swing.JCheckBox();
        includeProductNameCheckbox = new javax.swing.JCheckBox();
        includePriceCheckbox = new javax.swing.JCheckBox();
        printType = new javax.swing.JComboBox<>();
        productName = new javax.swing.JTextField();

        jToggleButton1.setText("jToggleButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        barcodeField.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        printBtn.setFont(new java.awt.Font("Nunito ExtraBold", 1, 14)); // NOI18N
        printBtn.setText("Print");
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printBtnActionPerformed(evt);
            }
        });

        barcodePrintQtyField.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        includeBusinessNameCheckbox.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        includeBusinessNameCheckbox.setForeground(new java.awt.Color(0, 0, 0));
        includeBusinessNameCheckbox.setText("Business Name");

        includeProductNameCheckbox.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        includeProductNameCheckbox.setForeground(new java.awt.Color(0, 0, 0));
        includeProductNameCheckbox.setText("Product Name");

        includePriceCheckbox.setFont(new java.awt.Font("Nunito SemiBold", 1, 12)); // NOI18N
        includePriceCheckbox.setForeground(new java.awt.Color(0, 0, 0));
        includePriceCheckbox.setText("Product Price");

        productName.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(printType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(barcodePrintQtyField, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(barcodeField)
                    .addComponent(productName))
                .addContainerGap(22, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(includeBusinessNameCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(includeProductNameCheckbox)
                .addGap(18, 18, 18)
                .addComponent(includePriceCheckbox)
                .addGap(34, 34, 34))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(productName, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(barcodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(includeBusinessNameCheckbox)
                    .addComponent(includeProductNameCheckbox)
                    .addComponent(includePriceCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(printType, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(barcodePrintQtyField)
                    .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void printBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBtnActionPerformed
        // TODO add your handling code here:
        printBarcodeLabel();
    }//GEN-LAST:event_printBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatLightLaf.setup();

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PrintProductLabel dialog = new PrintProductLabel(new javax.swing.JFrame(), true);
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
    private javax.swing.JTextField barcodeField;
    private javax.swing.JTextField barcodePrintQtyField;
    private javax.swing.JCheckBox includeBusinessNameCheckbox;
    private javax.swing.JCheckBox includePriceCheckbox;
    private javax.swing.JCheckBox includeProductNameCheckbox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton printBtn;
    private javax.swing.JComboBox<String> printType;
    private javax.swing.JTextField productName;
    // End of variables declaration//GEN-END:variables
}
