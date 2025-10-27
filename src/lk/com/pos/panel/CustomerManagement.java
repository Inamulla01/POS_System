package lk.com.pos.panel;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import lk.com.pos.privateclasses.RoundedPanel;

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
        
        // Load icons
        loadIcons();
        
        // Configure the rounded panel
        configureRoundedPanel();
        
        // Remove button borders
        removeButtonBorders();
        
        // Setup hover effects
        setupHoverEffects();
    }
    
    private void loadIcons() {
        FlatSVGIcon blueEdit = new FlatSVGIcon("lk/com/pos/icon/blueEdit.svg", 20, 20);
        editButton.setIcon(blueEdit);
        
        FlatSVGIcon redDelete = new FlatSVGIcon("lk/com/pos/icon/redDelete.svg", 20, 20);
        deleteButton.setIcon(redDelete);
        
        FlatSVGIcon telephone = new FlatSVGIcon("lk/com/pos/icon/telephone.svg", 20, 20);
        telephone1.setIcon(telephone);
        
        FlatSVGIcon address = new FlatSVGIcon("lk/com/pos/icon/address.svg", 20, 20);
        address1.setIcon(address);
        
        FlatSVGIcon date = new FlatSVGIcon("lk/com/pos/icon/date.svg", 20, 20);
        date1.setIcon(date);
        
        FlatSVGIcon loan = new FlatSVGIcon("lk/com/pos/icon/money-bag.svg", 20, 20);
        loan1.setIcon(loan);
    }
    
    private void configureRoundedPanel() {
        // Configure the rounded panel appearance
        roundedPanel1.setCornerRadius(12);
        roundedPanel1.setBackgroundColor(Color.WHITE);
        // No border - just rounded corners
        roundedPanel1.setBorderThickness(0);
    }
    
    private void removeButtonBorders() {
        // Edit and Delete buttons (icon only, circular style)
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setFocusPainted(false);
        editButton.setBackground(new Color(59, 130, 246));
        editButton.setOpaque(false);

        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setBackground(new Color(239, 68, 68));
        deleteButton.setOpaque(false);

        // Icon buttons (non-clickable, just decorative)
        telephone1.setBorderPainted(false);
        telephone1.setContentAreaFilled(false);
        telephone1.setFocusPainted(false);
        telephone1.setEnabled(false);

        address1.setBorderPainted(false);
        address1.setContentAreaFilled(false);
        address1.setFocusPainted(false);
        address1.setEnabled(false);

        date1.setBorderPainted(false);
        date1.setContentAreaFilled(false);
        date1.setFocusPainted(false);
        date1.setEnabled(false);

        loan1.setBorderPainted(false);
        loan1.setContentAreaFilled(false);
        loan1.setFocusPainted(false);
        loan1.setEnabled(false);
    }
    
    private void setupHoverEffects() {
        // Card hover effect
        roundedPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                roundedPanel1.setBackgroundColor(new Color(248, 250, 252));
                roundedPanel1.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                roundedPanel1.setBackgroundColor(Color.WHITE);
                roundedPanel1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        // Edit button hover effect (blue)
        setupButtonHoverEffect(editButton, new Color(59, 130, 246), new Color(37, 99, 235));
        
        // Delete button hover effect (red)
        setupButtonHoverEffect(deleteButton, new Color(239, 68, 68), new Color(220, 38, 38));
        
        // Add Customer button hover effect (green)
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
        roundedPanel1 = new lk.com.pos.privateclasses.RoundedPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        telephone1 = new javax.swing.JButton();
        address1 = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        date1 = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        loan1 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jPanel1.setBackground(new java.awt.Color(235, 236, 238));
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

        jLabel3.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        jLabel3.setText("Nimal  Jayarathne");

        jLabel11.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 0, 0));
        jLabel11.setText("Deactive");

        jLabel12.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 204, 51));
        jLabel12.setText("Active");

        jLabel13.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jLabel13.setText("0771234567");

        telephone1.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        telephone1.setBorder(null);

        address1.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        address1.setBorder(null);

        jLabel14.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jLabel14.setText("Colombo 8");

        date1.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        date1.setBorder(null);

        jLabel15.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jLabel15.setText("05/12/2025");

        loan1.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        loan1.setBorder(null);

        jLabel16.setFont(new java.awt.Font("Nunito SemiBold", 0, 14)); // NOI18N
        jLabel16.setText("10000");

        editButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        editButton.setBorder(null);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        deleteButton.setFont(new java.awt.Font("Nunito SemiBold", 0, 12)); // NOI18N
        deleteButton.setBorder(null);

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel1Layout.createSequentialGroup()
                .addContainerGap(36, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
            .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(roundedPanel1Layout.createSequentialGroup()
                    .addGap(38, 38, 38)
                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(roundedPanel1Layout.createSequentialGroup()
                            .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(address1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(date1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(loan1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(telephone1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel15)
                                .addComponent(jLabel14)
                                .addComponent(jLabel13)))
                        .addGroup(roundedPanel1Layout.createSequentialGroup()
                            .addGap(17, 17, 17)
                            .addComponent(jLabel12)
                            .addGap(18, 18, 18)
                            .addComponent(jLabel11)))
                    .addContainerGap(131, Short.MAX_VALUE)))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(182, Short.MAX_VALUE))
            .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(roundedPanel1Layout.createSequentialGroup()
                    .addContainerGap(41, Short.MAX_VALUE)
                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12)
                        .addComponent(jLabel11))
                    .addGap(18, 18, 18)
                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(telephone1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel13))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel14)
                        .addComponent(address1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(date1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addGroup(roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(roundedPanel1Layout.createSequentialGroup()
                            .addGap(15, 15, 15)
                            .addComponent(jLabel16))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel1Layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(loan1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap()))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 725, Short.MAX_VALUE)
                        .addComponent(addCustomerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(273, 273, 273))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(addCustomerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(82, 82, 82)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(645, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1022, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1025, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 235, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addCustomerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCustomerButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addCustomerButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCustomerButton;
    private javax.swing.JButton address1;
    private javax.swing.JButton date1;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton loan1;
    private lk.com.pos.privateclasses.RoundedPanel roundedPanel1;
    private javax.swing.JButton telephone1;
    // End of variables declaration//GEN-END:variables
}
