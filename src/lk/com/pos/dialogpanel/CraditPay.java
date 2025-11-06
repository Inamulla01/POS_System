package lk.com.pos.dialogpanel;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Window;
import java.text.SimpleDateFormat;
import javax.swing.*;
import raven.toast.Notifications;
import lk.com.pos.dialog.CreditView;
import lk.com.pos.dialog.UpdateCreditPay;

public class CraditPay extends javax.swing.JPanel {

    private int payId;
    private int creditId;
    private CreditView parentDialog;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final Color ICON_DEFAULT_COLOR = Color.decode("#999999");
    private static final Color ICON_HOVER_COLOR = Color.decode("#0893B0");
    private static final Color SELECTED_BORDER_COLOR = Color.decode("#0893B0");
    private static final Color DEFAULT_BORDER_COLOR = new Color(200, 200, 200);
    
    private boolean isSelected = false;

    public CraditPay(int payId, java.util.Date payDate, double payAmount, 
                          int creditId, CreditView parentDialog) {
        this.payId = payId;
        this.creditId = creditId;
        this.parentDialog = parentDialog;
        
        initComponents();
        
        setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR, 1));
        
        amount.setText(String.format("Rs %.2f", payAmount));
        date.setText(dateFormat.format(payDate));
        
        setupButton();
        
        // Set tooltip for the panel
        setToolTipText("Click to select, F1 to edit payment");
        
        // Make the panel focusable and add mouse listener
        setFocusable(true);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                requestFocusInWindow();
            }
        });
    }

    private void setupButton() {
        setupIconButton(editBtn, "edit.svg");
        editBtn.setToolTipText("Edit this payment (F1)");
        editBtn.addActionListener(e -> editPayment());
        
        // Add mouse listener to edit button to prevent event bubbling
        editBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                evt.consume(); // Prevent event from bubbling to panel
            }
        });
    }

    private void setupIconButton(JButton button, String iconName) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ICON_DEFAULT_COLOR));
            button.setIcon(icon);
            button.setText("");
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setFocusable(false);
            
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
                    hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ICON_HOVER_COLOR));
                    button.setIcon(hoverIcon);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (isSelected) {
                        FlatSVGIcon selectedIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
                        selectedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> SELECTED_BORDER_COLOR));
                        button.setIcon(selectedIcon);
                    } else {
                        FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/" + iconName, 25, 25);
                        normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ICON_DEFAULT_COLOR));
                        button.setIcon(normalIcon);
                    }
                }
            });
        } catch (Exception e) {
            button.setText(iconName.substring(0, 1).toUpperCase());
        }
    }

    private void editPayment() {
        System.out.println("Edit payment called for payId: " + payId);
        
        try {
            // Get the owner frame of the parentDialog
            Window ownerWindow = parentDialog != null ? parentDialog.getOwner() : null;
            UpdateCreditPay dialog = null;
            
            if (ownerWindow instanceof JFrame) {
                // Use the owner JFrame
                JFrame ownerFrame = (JFrame) ownerWindow;
                dialog = new UpdateCreditPay(ownerFrame, true, payId);
            } else {
                // Use null parent
                dialog = new UpdateCreditPay((JFrame) null, true, payId);
            }
            
            // Set location relative to parent dialog
            if (parentDialog != null) {
                dialog.setLocationRelativeTo(parentDialog);
            }
            
            // Add window listener to refresh after dialog closes
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    refreshParent();
                }
            });
            
            dialog.setVisible(true);
            
        } catch (Exception e) {
            System.err.println("Error opening UpdateCreditPay dialog: " + e.getMessage());
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                Notifications.Location.TOP_RIGHT, "Error opening payment editor: " + e.getMessage());
        }
    }

    private void refreshParent() {
        // Refresh parent dialog after editing
        if (parentDialog != null) {
            System.out.println("Refreshing parent credits after payment edit...");
            SwingUtilities.invokeLater(() -> {
                parentDialog.refreshCredits();
            });
        }
    }

    public void setBorderColor(Color color) {
        int borderWidth = color.equals(SELECTED_BORDER_COLOR) ? 3 : 1;
        setBorder(BorderFactory.createLineBorder(color, borderWidth));
        
        isSelected = color.equals(SELECTED_BORDER_COLOR);
        
        if (isSelected) {
            updateButtonIconForSelection();
            setToolTipText("Payment selected - F1: Edit Payment");
        } else {
            updateButtonIconForDeselection();
            setToolTipText("Click to select, F1 to edit payment");
        }
        
        repaint();
    }

    private void updateButtonIconForSelection() {
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/edit.svg", 25, 25);
            editIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> SELECTED_BORDER_COLOR));
            editBtn.setIcon(editIcon);
            editBtn.setToolTipText("Edit this payment (F1) - Selected");
        } catch (Exception e) {
            System.err.println("Error updating selected button icon: " + e.getMessage());
        }
    }

    private void updateButtonIconForDeselection() {
        try {
            FlatSVGIcon editIcon = new FlatSVGIcon("lk/com/pos/icon/edit.svg", 25, 25);
            editIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ICON_DEFAULT_COLOR));
            editBtn.setIcon(editIcon);
            editBtn.setToolTipText("Edit this payment (F1)");
        } catch (Exception e) {
            System.err.println("Error updating deselected button icon: " + e.getMessage());
        }
    }

    public void triggerEdit() {
        System.out.println("TriggerEdit called for payment ID: " + payId);
        editPayment();
    }
    
    // Getter methods for debugging
    public int getPayId() {
        return payId;
    }
    
    public int getCreditId() {
        return creditId;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        date = new javax.swing.JLabel();
        editBtn = new javax.swing.JButton();
        amount = new javax.swing.JLabel();

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        date.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        date.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        date.setText("02/30/2025 10.20.20");

        editBtn.setText("E");
        editBtn.setPreferredSize(new java.awt.Dimension(33, 33));

        amount.setFont(new java.awt.Font("Nunito ExtraBold", 1, 35)); // NOI18N
        amount.setText("Rs 10,000");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(date)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(amount, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 10, Short.MAX_VALUE)
                        .addComponent(amount, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel amount;
    private javax.swing.JLabel date;
    private javax.swing.JButton editBtn;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
}
