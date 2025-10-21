package lk.com.pos.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HomeScreen extends JFrame {

    // Icons
    private FlatSVGIcon dashboardIcon, posIcon, supplierIcon, salesIcon, creditIcon, stockIcon, menuIcon, signOutIcon;

    // Sidebar animation
    private static final int SIDEBAR_WIDTH_EXPANDED = 230;
    private static final int SIDEBAR_WIDTH_COLLAPSED = 70;
    private static final int STEP_SIZE = 12;
    private static final int SLIDE_DELAY = 12;
    private boolean isSidebarExpanded = true;
    private Timer slideTimer;
    private int animationTargetWidth;
    private boolean animationOpening;

    public HomeScreen() {
        initComponents(); // Your generated GUI code here
        init();
        initSidebarSlider();
    }

    private void init() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Load icons
        dashboardIcon = new FlatSVGIcon("lk/com/pos/icon/dashboard.svg", 27, 27);
        posIcon = new FlatSVGIcon("lk/com/pos/icon/cart.svg", 28, 28);
        supplierIcon = new FlatSVGIcon("lk/com/pos/icon/truck.svg", 20, 20);
        salesIcon = new FlatSVGIcon("lk/com/pos/icon/dollar.svg", 22, 23);
        creditIcon = new FlatSVGIcon("lk/com/pos/icon/credit-card.svg", 20, 20);
        stockIcon = new FlatSVGIcon("lk/com/pos/icon/box.svg", 20, 20);
        menuIcon = new FlatSVGIcon("lk/com/pos/icon/menu.svg", 22, 22);
        signOutIcon = new FlatSVGIcon("lk/com/pos/icon/signout.svg", 20, 20);

        // Normal and hover colors
        Color normalTextColor = Color.BLACK;
        Color hoverTop = new Color(0x12B5A6);
        Color hoverBottom = new Color(0x0893B0);

        // Setup hover buttons
        setupHoverButton(dashboardBtn, dashboardIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(posBtn, posIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(supplierBtn, supplierIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(salesBtn, salesIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(creditBtn, creditIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(stockBtn, stockIcon, normalTextColor, hoverTop, hoverBottom);
        setupHoverButton(menuBtn, menuIcon, normalTextColor, hoverTop, hoverBottom);

        // Sign Out button: red gradient
        setupHoverButton(signOutBtn, signOutIcon, Color.RED, new Color(255, 0, 0), new Color(200, 0, 0));

        // Logo setup
        updateLogo();

        // Padding for alignment
        dashboardBtn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        posBtn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        supplierBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        salesBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        creditBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        stockBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        signOutBtn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        menuBtn.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 0));

        sidePenal.setPreferredSize(new Dimension(SIDEBAR_WIDTH_EXPANDED, sidePenal.getPreferredSize().height));
        penal1.revalidate();
        penal1.repaint();
    }

    private void setupHoverButton(JButton button, FlatSVGIcon icon, Color normalTextColor, Color hoverTopColor, Color hoverBottomColor) {
        icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return normalTextColor;
            }
        });
        button.setIcon(icon);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(normalTextColor);
        button.setOpaque(true);

        final boolean[] hover = {false};

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover[0] = true;
                button.setForeground(Color.WHITE);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return Color.WHITE;
                    }
                });
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover[0] = false;
                button.setForeground(normalTextColor);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                    @Override
                    public Color filter(Color color) {
                        return normalTextColor;
                    }
                });
                button.repaint();
            }
        });

        // Custom UI for gradient background
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                int w = c.getWidth();
                int h = c.getHeight();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color top = hover[0] ? hoverTopColor : Color.WHITE;
                Color bottom = hover[0] ? hoverBottomColor : Color.WHITE;

                g2.setPaint(new GradientPaint(0, 0, top, w, 0, bottom));
                g2.fillRect(0, 0, w, h);

                super.paint(g, c);
            }
        });
    }

    private void initSidebarSlider() {
        slideTimer = new Timer(SLIDE_DELAY, e -> {
            int currentWidth = sidePenal.getWidth();
            if (animationOpening) {
                if (currentWidth < animationTargetWidth) {
                    int next = Math.min(currentWidth + STEP_SIZE, animationTargetWidth);
                    sidePenal.setPreferredSize(new Dimension(next, sidePenal.getHeight()));
                    penal1.revalidate();
                    penal1.repaint();
                } else {
                    slideTimer.stop();
                    isSidebarExpanded = true;
                    setButtonTextVisible(true);
                    updateLogo();
                }
            } else {
                if (currentWidth > animationTargetWidth) {
                    int next = Math.max(currentWidth - STEP_SIZE, animationTargetWidth);
                    sidePenal.setPreferredSize(new Dimension(next, sidePenal.getHeight()));
                    penal1.revalidate();
                    penal1.repaint();
                } else {
                    slideTimer.stop();
                    isSidebarExpanded = false;
                    updateLogo();
                }
            }
        });
    }

    private void setButtonTextVisible(boolean visible) {
        dashboardBtn.setText(visible ? " Dashboard" : "");
        posBtn.setText(visible ? " POS" : "");
        supplierBtn.setText(visible ? " Supplier" : "");
        salesBtn.setText(visible ? " Sales" : "");
        creditBtn.setText(visible ? " Credit Customers" : "");
        stockBtn.setText(visible ? " Stocks" : "");
        signOutBtn.setText(visible ? " Sign Out" : "");
    }

    private void updateLogo() {
        if (isSidebarExpanded) {
            logo.setIcon(new FlatSVGIcon("lk/com/pos/img/pos_big_logo.svg", 200, 67));
        } else {
            logo.setIcon(new FlatSVGIcon("lk/com/pos/img/pos_small_logo_1.svg", 52, 50));
        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        penal1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cardPanel = new javax.swing.JPanel();
        navPanel = new javax.swing.JPanel();
        sidePenal = new javax.swing.JPanel();
        menuBtn = new javax.swing.JButton();
        dashboardBtn = new javax.swing.JButton();
        posBtn = new javax.swing.JButton();
        supplierBtn = new javax.swing.JButton();
        salesBtn = new javax.swing.JButton();
        creditBtn = new javax.swing.JButton();
        stockBtn = new javax.swing.JButton();
        signOutBtn = new javax.swing.JButton();
        logo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Home Screen");

        penal1.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setAlignmentX(0.0F);
        jScrollPane1.setAlignmentY(0.0F);

        cardPanel.setBackground(new java.awt.Color(153, 153, 255));
        cardPanel.setLayout(new java.awt.CardLayout());
        jScrollPane1.setViewportView(cardPanel);

        navPanel.setBackground(new java.awt.Color(153, 255, 153));

        javax.swing.GroupLayout navPanelLayout = new javax.swing.GroupLayout(navPanel);
        navPanel.setLayout(navPanelLayout);
        navPanelLayout.setHorizontalGroup(
            navPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        navPanelLayout.setVerticalGroup(
            navPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 75, Short.MAX_VALUE)
        );

        sidePenal.setBackground(new java.awt.Color(255, 255, 255));

        menuBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        menuBtn.setBorder(null);
        menuBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        menuBtn.setIconTextGap(3);
        menuBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        menuBtn.setPreferredSize(new java.awt.Dimension(75, 40));
        menuBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuBtnActionPerformed(evt);
            }
        });

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

        salesBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        salesBtn.setText(" Sales");
        salesBtn.setBorder(null);
        salesBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        salesBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        salesBtn.setIconTextGap(3);
        salesBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        salesBtn.setPreferredSize(new java.awt.Dimension(75, 40));

        creditBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        creditBtn.setText(" Credit Custemers");
        creditBtn.setBorder(null);
        creditBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        creditBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        creditBtn.setIconTextGap(3);
        creditBtn.setMargin(new java.awt.Insets(2, 14, 10, 14));
        creditBtn.setPreferredSize(new java.awt.Dimension(75, 40));

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

        javax.swing.GroupLayout sidePenalLayout = new javax.swing.GroupLayout(sidePenal);
        sidePenal.setLayout(sidePenalLayout);
        sidePenalLayout.setHorizontalGroup(
            sidePenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dashboardBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
            .addComponent(posBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(supplierBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(salesBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(creditBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(stockBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(signOutBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(menuBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sidePenalLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logo, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        sidePenalLayout.setVerticalGroup(
            sidePenalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidePenalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logo, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(menuBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dashboardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(posBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(supplierBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(salesBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(creditBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(stockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(signOutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        javax.swing.GroupLayout penal1Layout = new javax.swing.GroupLayout(penal1);
        penal1.setLayout(penal1Layout);
        penal1Layout.setHorizontalGroup(
            penal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, penal1Layout.createSequentialGroup()
                .addComponent(sidePenal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(penal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(navPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 837, Short.MAX_VALUE)))
        );
        penal1Layout.setVerticalGroup(
            penal1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(penal1Layout.createSequentialGroup()
                .addComponent(navPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1))
            .addComponent(sidePenal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(penal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(penal1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void supplierBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supplierBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_supplierBtnActionPerformed

    private void menuBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBtnActionPerformed
            if (slideTimer.isRunning()) {
                // If animation already running, ignore repeated clicks
                return;
            }

            if (isSidebarExpanded) {
                // start collapsing
                animationOpening = false;
                animationTargetWidth = SIDEBAR_WIDTH_COLLAPSED;
                // Hide text immediately so icons remain centered while shrinking
                setButtonTextVisible(false);
            } else {
                // start expanding
                animationOpening = true;
                animationTargetWidth = SIDEBAR_WIDTH_EXPANDED;
                // We will show text after fully expanded (in timer stop)
            }

            slideTimer.start();

    }//GEN-LAST:event_menuBtnActionPerformed

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
    private javax.swing.JLabel logo;
    private javax.swing.JButton menuBtn;
    private javax.swing.JPanel navPanel;
    private javax.swing.JPanel penal1;
    private javax.swing.JButton posBtn;
    private javax.swing.JButton salesBtn;
    private javax.swing.JPanel sidePenal;
    private javax.swing.JButton signOutBtn;
    private javax.swing.JButton stockBtn;
    private javax.swing.JButton supplierBtn;
    // End of variables declaration//GEN-END:variables
}
