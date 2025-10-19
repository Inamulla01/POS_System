package lk.com.pos.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import lk.com.pos.util.AppIconUtil;

public class HomeScreen extends javax.swing.JFrame {

    private boolean isMenuCollapsed = false; // track sidebar state

    public HomeScreen() {
        initComponents();
        init();
    }

    public void init() {
        AppIconUtil.applyIcon(this);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Setup icons
        FlatSVGIcon dashboardIcon = new FlatSVGIcon("lk/com/pos/icon/dashboard.svg", 27, 27);
        dashboardBtn.setIcon(dashboardIcon);
        FlatSVGIcon posIcon = new FlatSVGIcon("lk/com/pos/icon/cart.svg", 28, 28);
        posBtn.setIcon(posIcon);
        FlatSVGIcon supplierIcon = new FlatSVGIcon("lk/com/pos/icon/truck.svg", 20, 20);
        supplierBtn.setIcon(supplierIcon);
        FlatSVGIcon salesIcon = new FlatSVGIcon("lk/com/pos/icon/dollar.svg", 22, 23);
        salesBtn.setIcon(salesIcon);
        FlatSVGIcon creditIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 20, 20);
        creditBtn.setIcon(creditIcon);
        FlatSVGIcon stockIcon = new FlatSVGIcon("lk/com/pos/icon/box.svg", 20, 20);
        stockBtn.setIcon(stockIcon);
        FlatSVGIcon menuIcon = new FlatSVGIcon("lk/com/pos/icon/menu.svg", 22, 22);
        menuBtn.setIcon(menuIcon);
        FlatSVGIcon signOutIcon = new FlatSVGIcon("lk/com/pos/icon/signout.svg", 20, 20);
        signOutIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#FF0000")));
        signOutBtn.setIcon(signOutIcon);

        // Border adjustments
        dashboardBtn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        posBtn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        supplierBtn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        salesBtn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        creditBtn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        stockBtn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        signOutBtn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        menuBtn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        // Add menu toggle
        menuBtn.addActionListener(e -> toggleMenu());
    }

    // Toggle sidebar collapse/expand
    private void toggleMenu() {
        if (!isMenuCollapsed) {
            // Collapse menu
            dashboardBtn.setText("");
            posBtn.setText("");
            supplierBtn.setText("");
            salesBtn.setText("");
            creditBtn.setText("");
            stockBtn.setText("");
            signOutBtn.setText("");

            menuPenal.setPreferredSize(new Dimension(70, menuPenal.getHeight())); // shrink
            menuPenal.revalidate();
            menuPenal.repaint();

            isMenuCollapsed = true;
        } else {
            // Expand menu
            dashboardBtn.setText(" Dashboard");
            posBtn.setText(" POS");
            supplierBtn.setText(" Supplier");
            salesBtn.setText(" Sales");
            creditBtn.setText(" Credit Customers");
            stockBtn.setText(" Stocks");
            signOutBtn.setText(" Sign Out");

            menuPenal.setPreferredSize(new Dimension(230, menuPenal.getHeight())); // expand
            menuPenal.revalidate();
            menuPenal.repaint();

            isMenuCollapsed = false;
        }
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nevPenal = new javax.swing.JPanel();
        menuPenal = new javax.swing.JPanel();
        dashboardBtn = new javax.swing.JButton();
        posBtn = new javax.swing.JButton();
        supplierBtn = new javax.swing.JButton();
        creditBtn = new javax.swing.JButton();
        salesBtn = new javax.swing.JButton();
        stockBtn = new javax.swing.JButton();
        signOutBtn = new javax.swing.JButton();
        menuBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        cardPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Home Screen");

        nevPenal.setBackground(new java.awt.Color(255, 255, 255));

        menuPenal.setBackground(new java.awt.Color(255, 255, 255));
        menuPenal.setPreferredSize(new java.awt.Dimension(0, 0));

        dashboardBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        dashboardBtn.setText(" Dashboard");
        dashboardBtn.setBorder(null);
        dashboardBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        dashboardBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        dashboardBtn.setIconTextGap(3);
        dashboardBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        dashboardBtn.setPreferredSize(new java.awt.Dimension(75, 40));

        posBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        posBtn.setText(" POS");
        posBtn.setBorder(null);
        posBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        posBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        posBtn.setIconTextGap(3);
        posBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        posBtn.setPreferredSize(new java.awt.Dimension(75, 40));

        supplierBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        supplierBtn.setText(" Supplier");
        supplierBtn.setBorder(null);
        supplierBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        supplierBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        supplierBtn.setIconTextGap(3);
        supplierBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        supplierBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        supplierBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supplierBtnActionPerformed(evt);
            }
        });

        creditBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        creditBtn.setText(" Credit Custemers");
        creditBtn.setBorder(null);
        creditBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        creditBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        creditBtn.setIconTextGap(3);
        creditBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        creditBtn.setPreferredSize(new java.awt.Dimension(75, 40));

        salesBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        salesBtn.setText(" Sales");
        salesBtn.setBorder(null);
        salesBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        salesBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        salesBtn.setIconTextGap(3);
        salesBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        salesBtn.setPreferredSize(new java.awt.Dimension(75, 40));

        stockBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        stockBtn.setText(" Stocks");
        stockBtn.setBorder(null);
        stockBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        stockBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        stockBtn.setIconTextGap(3);
        stockBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        stockBtn.setPreferredSize(new java.awt.Dimension(75, 40));

        signOutBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        signOutBtn.setForeground(new java.awt.Color(255, 0, 0));
        signOutBtn.setText(" Sign Out");
        signOutBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 51, 51)));
        signOutBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        signOutBtn.setHideActionText(true);
        signOutBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        signOutBtn.setIconTextGap(3);
        signOutBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        signOutBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        signOutBtn.setVerifyInputWhenFocusTarget(false);

        menuBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        menuBtn.setBorder(null);
        menuBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        menuBtn.setIconTextGap(3);
        menuBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        menuBtn.setPreferredSize(new java.awt.Dimension(75, 40));

        javax.swing.GroupLayout menuPenalLayout = new javax.swing.GroupLayout(menuPenal);
        menuPenal.setLayout(menuPenalLayout);
        menuPenalLayout.setHorizontalGroup(
            menuPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuPenalLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(menuPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(menuBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(menuPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(signOutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dashboardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(posBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(supplierBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(creditBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(salesBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(stockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        menuPenalLayout.setVerticalGroup(
            menuPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuPenalLayout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(menuBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dashboardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(posBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(supplierBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(salesBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(creditBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(signOutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setAlignmentX(0.0F);
        jScrollPane1.setAlignmentY(0.0F);

        cardPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout cardPanelLayout = new javax.swing.GroupLayout(cardPanel);
        cardPanel.setLayout(cardPanelLayout);
        cardPanelLayout.setHorizontalGroup(
            cardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 892, Short.MAX_VALUE)
        );
        cardPanelLayout.setVerticalGroup(
            cardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 547, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(cardPanel);

        javax.swing.GroupLayout nevPenalLayout = new javax.swing.GroupLayout(nevPenal);
        nevPenal.setLayout(nevPenalLayout);
        nevPenalLayout.setHorizontalGroup(
            nevPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nevPenalLayout.createSequentialGroup()
                .addComponent(menuPenal, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 837, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        nevPenalLayout.setVerticalGroup(
            nevPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nevPenalLayout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE))
            .addComponent(menuPenal, javax.swing.GroupLayout.DEFAULT_SIZE, 601, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nevPenal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nevPenal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void supplierBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supplierBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_supplierBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatLightLaf.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HomeScreen().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cardPanel;
    private javax.swing.JButton creditBtn;
    private javax.swing.JButton dashboardBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton menuBtn;
    private javax.swing.JPanel menuPenal;
    private javax.swing.JPanel nevPenal;
    private javax.swing.JButton posBtn;
    private javax.swing.JButton salesBtn;
    private javax.swing.JButton signOutBtn;
    private javax.swing.JButton stockBtn;
    private javax.swing.JButton supplierBtn;
    // End of variables declaration//GEN-END:variables
}
