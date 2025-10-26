package lk.com.pos.panel;


import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
/**
 *
 * @author moham
 */
public class CustomerManagement extends javax.swing.JPanel {

    /**
     * Creates new form CustomerManagement
     */
    public CustomerManagement() {
        initComponents();

         // Configure RoundedPanel
        configureRoundedPanel();

        // Load icons
        loadIcons();

        // Remove button borders
        removeButtonBorders();

        // Setup hover effects
        setupHoverEffects();

    }

      private void configureRoundedPanel() {
        // Configure the rounded panel appearance
        roundedPanel2.setCornerRadius(12);
        roundedPanel2.setBackgroundColor(Color.WHITE);
        // No border - just rounded corners
        roundedPanel2.setBorderThickness(0);
    }

    private void loadIcons() {
        FlatSVGIcon blueEdit = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 20, 20);
        editButton.setIcon(blueEdit);
        
        FlatSVGIcon redDelete = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 20, 20);
        deleteButton.setIcon(redDelete);
        
        FlatSVGIcon telephoneIcon = new FlatSVGIcon("lk/com/pos/icon/telephone.svg", 20, 20);
        telephone.setIcon(telephoneIcon);
        
        FlatSVGIcon addressIcon = new FlatSVGIcon("lk/com/pos/icon/address.svg", 20, 20);
        address.setIcon(addressIcon);
        
        FlatSVGIcon dateIcon = new FlatSVGIcon("lk/com/pos/icon/newdate.svg", 20, 20);
        date.setIcon(dateIcon);
        
        FlatSVGIcon loanIcon = new FlatSVGIcon("lk/com/pos/icon/newmoney-bag2.svg", 20, 20);
        loan.setIcon(loanIcon);
        
        FlatSVGIcon addCustomer = new FlatSVGIcon("lk/com/pos/icon/addCustomer.svg", 18, 18);
        addCustomerButton.setIcon(addCustomer);
    }

    
     private void removeButtonBorders() {
        // Edit and Delete buttons (icon only)
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setFocusPainted(false);
        editButton.setOpaque(false);

        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(false);

        // Icon buttons (non-clickable, decorative)
        telephone.setBorderPainted(false);
        telephone.setContentAreaFilled(false);
        telephone.setFocusPainted(false);
        telephone.setEnabled(false);
        telephone.setOpaque(false);

        address.setBorderPainted(false);
        address.setContentAreaFilled(false);
        address.setFocusPainted(false);
        address.setEnabled(false);
        address.setOpaque(false);

        date.setBorderPainted(false);
        date.setContentAreaFilled(false);
        date.setFocusPainted(false);
        date.setEnabled(false);
        date.setOpaque(false);

        loan.setBorderPainted(false);
        loan.setContentAreaFilled(false);
        loan.setFocusPainted(false);
        loan.setEnabled(false);
        loan.setOpaque(false);
    }

    private void setupHoverEffects() {
        // Card hover effect (subtle background change)
        roundedPanel2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
//                roundedPanel2.setBackgroundColor(new Color(248, 250, 252));
                roundedPanel2.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
//                roundedPanel2.setBackgroundColor(Color.WHITE);
                roundedPanel2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        // Edit button hover effect (blue)
        setupButtonHoverEffect(editButton, new Color(59, 130, 246), new Color(37, 99, 235));
        
        // Delete button hover effect (red)
        setupButtonHoverEffect(deleteButton, new Color(239, 68, 68), new Color(220, 38, 38));
        
        // Add Customer button hover effect (green/teal)
        setupButtonHoverEffect(addCustomerButton, new Color(115, 230, 203), new Color(16, 185, 129));
    }

    private void setupButtonHoverEffect(javax.swing.JButton button, Color normalColor, Color hoverColor) {
        button.setBackground(normalColor);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        addCustomerButton = new javax.swing.JButton();
        roundedPanel2 = new lk.com.pos.panel.RoundedPanel();
        jLabel1 = new javax.swing.JLabel();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        telephone = new javax.swing.JButton();
        address = new javax.swing.JButton();
        date = new javax.swing.JButton();
        loan = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));
        jPanel1.setPreferredSize(new java.awt.Dimension(1366, 768));

        jTextField1.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jTextField1.setText("Search Customer");

        addCustomerButton.setBackground(new java.awt.Color(115, 230, 203));
        addCustomerButton.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        addCustomerButton.setText("Add Customer");
        addCustomerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCustomerButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        jLabel1.setText("Nimal   Jayarathne");

        editButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        editButton.setBorder(null);
        editButton.setPreferredSize(new java.awt.Dimension(25, 25));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        deleteButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        deleteButton.setBorder(null);
        deleteButton.setPreferredSize(new java.awt.Dimension(25, 25));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        telephone.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        telephone.setBorder(null);
        telephone.setPreferredSize(new java.awt.Dimension(25, 25));

        address.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        address.setBorder(null);
        address.setPreferredSize(new java.awt.Dimension(25, 25));

        date.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        date.setBorder(null);
        date.setPreferredSize(new java.awt.Dimension(25, 25));

        loan.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        loan.setBorder(null);
        loan.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel8.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jLabel8.setText("10000");

        jLabel7.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jLabel7.setText("05/12/2025");

        jLabel5.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jLabel5.setText("Colombo 8");

        jLabel6.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jLabel6.setText("0771234567");

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel2Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(30, 30, 30)
                        .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(roundedPanel2Layout.createSequentialGroup()
                        .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(telephone, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(loan, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(116, 116, 116)))
                .addGap(14, 14, 14))
        );
        roundedPanel2Layout.setVerticalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(deleteButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(telephone, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 484, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 619, Short.MAX_VALUE)
                .addComponent(addCustomerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(92, 92, 92))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(addCustomerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(76, 76, 76)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(420, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1309, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addCustomerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCustomerButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addCustomerButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCustomerButton;
    private javax.swing.JButton address;
    private javax.swing.JButton date;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton loan;
    private lk.com.pos.panel.RoundedPanel roundedPanel2;
    private javax.swing.JButton telephone;
    // End of variables declaration//GEN-END:variables
}
