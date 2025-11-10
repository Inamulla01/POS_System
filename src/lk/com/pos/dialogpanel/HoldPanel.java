package lk.com.pos.dialogpanel;

import lk.com.pos.privateclasses.Invoice;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 *
 * @author moham
 */
public class HoldPanel extends javax.swing.JPanel {

    private Invoice invoice;
    private HoldPanelListener listener;
    private boolean isPanelFocused = false;

    /**
     * Creates new form holdPanel
     */
    public HoldPanel() {
        initComponents();
        setPanelSize();
        setupButtonStyles();
        setupPanelFocus();
    }

    public HoldPanel(Invoice invoice, HoldPanelListener listener) {
        initComponents();
        this.invoice = invoice;
        this.listener = listener;
        setPanelSize();
        setupButtonStyles();
        setupPanelFocus();
        setInvoiceData();
        setupButtonAction();
    }

    // Set the panel size to 580px width
    private void setPanelSize() {
        this.setSize(580, 60);
        this.setPreferredSize(new java.awt.Dimension(580, 60));
        this.setMaximumSize(new java.awt.Dimension(580, 60));
        this.setMinimumSize(new java.awt.Dimension(580, 60));

        invoicePanel.setSize(519, 60);
        invoicePanel.setPreferredSize(new java.awt.Dimension(580, 60));
        invoicePanel.setMaximumSize(new java.awt.Dimension(580, 60));
        invoicePanel.setMinimumSize(new java.awt.Dimension(580, 60));
    }

    // Setup panel focus behavior
    private void setupPanelFocus() {
        this.setFocusable(true);
        this.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                isPanelFocused = true;
                // Force button to show hover effect when panel is focused via keyboard
                controle.getModel().setRollover(true);
                controle.getModel().setArmed(true);
                updateButtonAppearance();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                isPanelFocused = false;
                // Remove hover effect when panel loses focus
                controle.getModel().setRollover(false);
                controle.getModel().setArmed(false);
                updateButtonAppearance();
            }
        });

        // Add mouse listener to handle panel focus on click
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                requestFocusInWindow();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!isPanelFocused) {
                    controle.getModel().setRollover(true);
                    updateButtonAppearance();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!isPanelFocused) {
                    controle.getModel().setRollover(false);
                    updateButtonAppearance();
                }
            }
        });
    }

    // Setup button styles with same hover effects as before
    private void setupButtonStyles() {
        setupGradientButton(controle);
    }

    private void setupGradientButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                int w = c.getWidth();
                int h = c.getHeight();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Check button state - show hover effect when panel is focused OR button is hovered
                boolean isHover = button.getModel().isRollover();
                boolean isPressed = button.getModel().isPressed();
                boolean isFocused = button.hasFocus();
                boolean isPanelFocused = HoldPanel.this.isPanelFocused;

                // Show gradient fill when panel is focused OR button is hovered/focused
                if (isPanelFocused || isHover || isFocused) {
                    // Hover/Focus/Pressed state - gradient fill
                    Color topColor, bottomColor;

                    String buttonText = button.getText();
                    switch (buttonText) {
                        case "Switch":
                            // Orange gradient for Switch
                            topColor = new Color(255, 193, 7);    // Light Orange
                            bottomColor = new Color(255, 152, 0); // Dark Orange
                            break;
                        case "View":
                            // Green gradient for View
                            topColor = new Color(105, 240, 174);  // Light Green
                            bottomColor = new Color(76, 175, 80); // Dark Green
                            break;
                        case "Open":
                            // Blue gradient for Open
                            topColor = new Color(100, 181, 246);  // Light Blue
                            bottomColor = new Color(30, 136, 229); // Dark Blue
                            break;
                        default:
                            // Default blue gradient
                            topColor = new Color(100, 181, 246);
                            bottomColor = new Color(30, 136, 229);
                            break;
                    }

                    // Draw gradient
                    GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 5, 5);

                    // White text for filled state
                    button.setForeground(Color.WHITE);
                } else {
                    // Default state - transparent with colored border
                    g2.setColor(new Color(0, 0, 0, 0)); // Transparent
                    g2.fillRoundRect(0, 0, w, h, 5, 5);

                    // Draw border with appropriate color based on button text
                    String buttonText = button.getText();
                    Color borderColor;
                    switch (buttonText) {
                        case "Switch":
                            borderColor = new Color(255, 152, 0); // Orange
                            break;
                        case "View":
                            borderColor = new Color(76, 175, 80); // Green
                            break;
                        case "Open":
                            borderColor = new Color(158, 158, 158); // Gray
                            break;
                        default:
                            borderColor = new Color(8, 147, 176); // Blue
                            break;
                    }
                    g2.setColor(borderColor);
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);

                    // Set text color to match border
                    button.setForeground(borderColor);
                }

                // Draw button text
                super.paint(g, c);
            }
        });

        // Add hover effects
        setupButtonMouseListeners(button);
        setupButtonFocusListeners(button);
    }

    private void setupButtonMouseListeners(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.repaint();
            }
        });
    }

    private void setupButtonFocusListeners(JButton button) {
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                button.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                button.repaint();
            }
        });
    }

    // Setup button icons based on action
    private void setupButtonIcon() {
        String buttonText = controle.getText();
        String iconPath;
        Color iconColor;

        // Determine icon and color based on button state
        boolean isHover = controle.getModel().isRollover();
        boolean isFocused = controle.hasFocus();
        boolean isPanelFocused = this.isPanelFocused;

        // White icon when panel is focused OR button is hovered/focused
        if (isPanelFocused || isHover || isFocused) {
            iconColor = Color.WHITE;
        } else {
            // Colored icon for normal state
            switch (buttonText) {
                case "Switch":
                    iconColor = new Color(255, 152, 0); // Orange
                    break;
                case "View":
                    iconColor = new Color(76, 175, 80); // Green
                    break;
                case "Open":
                    iconColor = new Color(158, 158, 158); // Gray
                    break;
                default:
                    iconColor = new Color(8, 147, 176); // Blue
                    break;
            }
        }

        // Set appropriate icon based on button text
        switch (buttonText) {
            case "Switch":
                iconPath = "lk/com/pos/icon/exchange.svg";
                break;
            case "View":
                iconPath = "lk/com/pos/icon/eye-open.svg";
                break;
            case "Open":
                iconPath = "lk/com/pos/icon/exchange.svg"; // Default to switch icon for Open
                break;
            default:
                iconPath = "lk/com/pos/icon/exchange.svg";
                break;
        }

        try {
            FlatSVGIcon icon = new FlatSVGIcon(iconPath, 20, 20);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> iconColor));
            controle.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Error loading icon: " + iconPath + " - " + e.getMessage());
            // Continue without icon if there's an error
        }
    }

    // Add this getter method for the button
    public javax.swing.JButton getControleButton() {
        return controle;
    }

    // Add this getter method for the invoice
    public Invoice getInvoice() {
        return invoice;
    }

    private void setInvoiceData() {
        if (invoice != null) {
            String status = invoice.getStatus();

            // Display only invoice number and status (no customer name)
            invoiceAndCondition.setText(invoice.getInvoiceNo() + " (" + status + ")");

            // Change button text based on status
            if ("Completed".equalsIgnoreCase(status)) {
                controle.setText("View");
            } else if ("Hold".equalsIgnoreCase(status)) {
                controle.setText("Switch");
            } else {
                controle.setText("Open");
            }

            // Setup button icon
            setupButtonIcon();

            // Set tooltip with invoice information (no customer name)
            String tooltip = String.format(
                    "Invoice: %s\nAmount: Rs. %.2f\nDate: %s",
                    invoice.getInvoiceNo(),
                    invoice.getTotal(),
                    invoice.getDate()
            );
            this.setToolTipText(tooltip);

            // Refresh button to apply appropriate colors and icons
            controle.repaint();
        }
    }

    private void setupButtonAction() {
        controle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (listener != null && invoice != null) {
                    String action = controle.getText();
                    listener.onInvoiceSelected(invoice, action);
                }
            }
        });
    }

    // Method to update button appearance when panel focus changes
    public void updateButtonAppearance() {
        setupButtonIcon();
        controle.repaint();
    }

    // Method to set panel focus state (called from HoldDialog)
    public void setPanelFocused(boolean focused) {
        this.isPanelFocused = focused;
        if (focused) {
            controle.getModel().setRollover(true);
            controle.getModel().setArmed(true);
        } else {
            controle.getModel().setRollover(false);
            controle.getModel().setArmed(false);
        }
        updateButtonAppearance();
    }

    public interface HoldPanelListener {

        void onInvoiceSelected(Invoice invoice, String action);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        invoicePanel = new javax.swing.JPanel();
        controle = new javax.swing.JButton();
        invoiceAndCondition = new javax.swing.JLabel();

        controle.setFont(new java.awt.Font("Nunito SemiBold", 1, 15)); // NOI18N
        controle.setText("Switch");

        invoiceAndCondition.setFont(new java.awt.Font("Nunito ExtraBold", 1, 16)); // NOI18N
        invoiceAndCondition.setText("#INV003212 (Hold)");

        javax.swing.GroupLayout invoicePanelLayout = new javax.swing.GroupLayout(invoicePanel);
        invoicePanel.setLayout(invoicePanelLayout);
        invoicePanelLayout.setHorizontalGroup(
            invoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, invoicePanelLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(invoiceAndCondition, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(controle, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        invoicePanelLayout.setVerticalGroup(
            invoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invoicePanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(invoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(controle, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(invoiceAndCondition))
                .addGap(9, 9, 9))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invoicePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invoicePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton controle;
    private javax.swing.JLabel invoiceAndCondition;
    private javax.swing.JPanel invoicePanel;
    // End of variables declaration//GEN-END:variables
}
