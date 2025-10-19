package lk.com.pos.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import javax.swing.*;
import lk.com.pos.util.AppIconUtil;

/**
 * HomeScreen with ChatGPT-like sliding sidebar.
 *
 * NOTE: This file omits initComponents() — keep your generated initComponents()
 * in the class. This code will re-parent the panels produced by initComponents()
 * into a BorderLayout so the sidebar can resize reliably.
 */
public class HomeScreen extends javax.swing.JFrame {

    // --- animation / sizes
    private boolean isMenuCollapsed = false;
    private final int expandedWidth = 230;
    private final int collapsedWidth = 70;
    private final int animationDelayMs = 8;   // timer delay (ms)
    private final int animationStepPx = 8;    // pixels per tick
    private Timer animationTimer;

    public HomeScreen() {
        initComponents(); // your GUI builder method (keep it)
        init();           // our initialization
    }

    private void init() {
        // apply app icon and maximize
        AppIconUtil.applyIcon(this);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // ---------- Set icons ----------
        dashboardBtn.setIcon(new FlatSVGIcon("lk/com/pos/icon/dashboard.svg", 27, 27));
        posBtn.setIcon(new FlatSVGIcon("lk/com/pos/icon/cart.svg", 28, 28));
        supplierBtn.setIcon(new FlatSVGIcon("lk/com/pos/icon/truck.svg", 20, 20));
        salesBtn.setIcon(new FlatSVGIcon("lk/com/pos/icon/dollar.svg", 22, 23));
        creditBtn.setIcon(new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 20, 20));
        stockBtn.setIcon(new FlatSVGIcon("lk/com/pos/icon/box.svg", 20, 20));
        menuBtn.setIcon(new FlatSVGIcon("lk/com/pos/icon/menu.svg", 22, 22));
        signOutBtn.setIcon(new FlatSVGIcon("lk/com/pos/icon/signout.svg", 20, 20));
        signOutBtn.setForeground(Color.RED);

        // ---------- Button common tweaks ----------
        JButton[] allButtons = { menuBtn, dashboardBtn, posBtn, supplierBtn, salesBtn, creditBtn, stockBtn, signOutBtn };
        for (JButton b : allButtons) {
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(false);
            b.setBorderPainted(false);
            b.setHorizontalAlignment(SwingConstants.LEADING);
            b.setIconTextGap(8);
            b.setPreferredSize(new Dimension(expandedWidth - 20, 40)); // height consistent
        }

        // Keep initial texts as in your original design
        dashboardBtn.setText(" Dashboard");
        posBtn.setText(" POS");
        supplierBtn.setText(" Supplier");
        salesBtn.setText(" Sales");
        creditBtn.setText(" Credit Customers");
        stockBtn.setText(" Stocks");
        signOutBtn.setText(" Sign Out");

        // ---------- RE-PARENT into BorderLayout for reliable resizing ----------
        // Remove generated layout constraints and use BorderLayout so we can change width easily.
        // We assume initComponents set up `nevPenal`, `menuPenal`, and `jScrollPane1` already.
        // We'll clear nevPenal and add menuPenal to WEST and jScrollPane1 to CENTER.
        nevPenal.removeAll();
        nevPenal.setLayout(new BorderLayout());
        // Make sure menuPenal has initial preferred size (expanded by default)
        menuPenal.setPreferredSize(new Dimension(expandedWidth, menuPenal.getHeight()));
        menuPenal.setMinimumSize(new Dimension(collapsedWidth, 0));
        menuPenal.setMaximumSize(new Dimension(expandedWidth, Integer.MAX_VALUE));

        nevPenal.add(menuPenal, BorderLayout.WEST);
        nevPenal.add(jScrollPane1, BorderLayout.CENTER);
        nevPenal.revalidate();
        nevPenal.repaint();

        // ---------- Menu button action: toggle with animation ----------
        menuBtn.addActionListener(e -> toggleMenuAnimated());

        // Ensure initial GUI is laid out
        SwingUtilities.invokeLater(() -> {
            menuPenal.revalidate();
            menuPenal.repaint();
            nevPenal.revalidate();
            nevPenal.repaint();
        });
    }

    /**
     * Animate sidebar sliding in/out. While collapsing the labels disappear quickly
     * and icons center; while expanding labels reappear after animation.
     */
    private void toggleMenuAnimated() {
        // prevent overlapping animations
        if (animationTimer != null && animationTimer.isRunning()) return;

        final int startWidth = menuPenal.getWidth();
        final int targetWidth = isMenuCollapsed ? expandedWidth : collapsedWidth;
        final int direction = (targetWidth > startWidth) ? 1 : -1; // 1 expand, -1 collapse
        // If collapsing, hide text immediately to avoid layout flicker; icons will center during animation.
        if (!isMenuCollapsed) prepareForCollapsedState();

        animationTimer = new Timer(animationDelayMs, null);
        animationTimer.addActionListener(evt -> {
            int current = menuPenal.getWidth();
            int next = current + animationStepPx * direction;

            // clamp
            if ((direction > 0 && next >= targetWidth) || (direction < 0 && next <= targetWidth)) {
                next = targetWidth;
            }

            // apply new width
            menuPenal.setPreferredSize(new Dimension(next, menuPenal.getHeight()));
            menuPenal.setMinimumSize(new Dimension(next, 0));
            menuPenal.setMaximumSize(new Dimension(next, Integer.MAX_VALUE));
            nevPenal.revalidate();
            nevPenal.repaint();

            // finished
            if (next == targetWidth) {
                ((Timer) evt.getSource()).stop();
                isMenuCollapsed = !isMenuCollapsed;
                if (isMenuCollapsed) {
                    // fully collapsed: center icons
                    centerButtonIcons();
                } else {
                    // fully expanded: restore text and left alignment
                    restoreExpandedState();
                }
            }
        });
        animationTimer.start();
    }

    /** Prepare quick immediate changes for collapsing to avoid label flicker */
    private void prepareForCollapsedState() {
        // remove text (instant)
        dashboardBtn.setText("");
        posBtn.setText("");
        supplierBtn.setText("");
        salesBtn.setText("");
        creditBtn.setText("");
        stockBtn.setText("");
        signOutBtn.setText("");

        // set buttons to have minimal preferred width so they shrink nicely
        JButton[] btns = { dashboardBtn, posBtn, supplierBtn, salesBtn, creditBtn, stockBtn, signOutBtn, menuBtn };
        for (JButton b : btns) {
            b.setPreferredSize(new Dimension(collapsedWidth - 10, 48)); // square-ish for icons
            // center icons
            b.setHorizontalAlignment(SwingConstants.LEADING);
            b.setHorizontalTextPosition(SwingConstants.LEADING);
        }
    }

    /** When fully collapsed, center icons and shrink button widths to icon size */
    private void centerButtonIcons() {
        JButton[] btns = { dashboardBtn, posBtn, supplierBtn, salesBtn, creditBtn, stockBtn, signOutBtn, menuBtn };
        for (JButton b : btns) {
            b.setHorizontalAlignment(SwingConstants.LEADING);
            b.setHorizontalTextPosition(SwingConstants.LEADING);
            // tighten preferred size to icon-only width
            int iconSize = 48; // button height
            b.setPreferredSize(new Dimension(collapsedWidth - 8, iconSize));
        }
        menuPenal.revalidate();
        menuPenal.repaint();
    }

    /** Restore buttons to expanded look (icon + text, left aligned) */
    private void restoreExpandedState() {
        // restore text
        dashboardBtn.setText(" Dashboard");
        posBtn.setText(" POS");
        supplierBtn.setText(" Supplier");
        salesBtn.setText(" Sales");
        creditBtn.setText(" Credit Customers");
        stockBtn.setText(" Stocks");
        signOutBtn.setText(" Sign Out");

        // restore alignment and sizes
        JButton[] btns = { dashboardBtn, posBtn, supplierBtn, salesBtn, creditBtn, stockBtn, signOutBtn, menuBtn };
        for (JButton b : btns) {
            b.setHorizontalAlignment(SwingConstants.LEADING);
            b.setHorizontalTextPosition(SwingConstants.RIGHT);
            b.setPreferredSize(new Dimension(expandedWidth - 20, 40));
        }
        menuPenal.setPreferredSize(new Dimension(expandedWidth, menuPenal.getHeight()));
        menuPenal.revalidate();
        menuPenal.repaint();
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
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Home Screen");

        nevPenal.setBackground(new java.awt.Color(255, 51, 51));

        menuPenal.setBackground(new java.awt.Color(255, 204, 204));
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
                .addGap(76, 76, 76)
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

        cardPanel.setBackground(new java.awt.Color(153, 153, 255));

        jLabel1.setBackground(new java.awt.Color(255, 51, 204));
        jLabel1.setText("jLabel1");
        jLabel1.setOpaque(true);

        javax.swing.GroupLayout cardPanelLayout = new javax.swing.GroupLayout(cardPanel);
        cardPanel.setLayout(cardPanelLayout);
        cardPanelLayout.setHorizontalGroup(
            cardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(651, Short.MAX_VALUE))
        );
        cardPanelLayout.setVerticalGroup(
            cardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(252, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(cardPanel);

        javax.swing.GroupLayout nevPenalLayout = new javax.swing.GroupLayout(nevPenal);
        nevPenal.setLayout(nevPenalLayout);
        nevPenalLayout.setHorizontalGroup(
            nevPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nevPenalLayout.createSequentialGroup()
                .addComponent(menuPenal, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 837, Short.MAX_VALUE))
        );
        nevPenalLayout.setVerticalGroup(
            nevPenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(menuPenal, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nevPenalLayout.createSequentialGroup()
                .addContainerGap(67, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 555, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nevPenal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nevPenal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private javax.swing.JLabel jLabel1;
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
