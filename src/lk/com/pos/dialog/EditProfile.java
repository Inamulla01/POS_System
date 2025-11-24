package lk.com.pos.dialog;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.validation.Validater;
import lk.com.pos.connection.MySQL;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.*;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

public class EditProfile extends javax.swing.JDialog {

    // ---------------- PASSWORD TOGGLE ----------------
    private boolean newPasswordVisible = false;
    private boolean confirmPasswordVisible = false;
    private Icon eyeOpenIcon;
    private Icon eyeClosedIcon;

    // ---------------- USER DATA ----------------
    private int currentUserId;
    private String currentUsername;

    public EditProfile(java.awt.Frame parent, boolean modal, int userId) {
        super(parent, modal);
        this.currentUserId = userId;
        initComponents();
        initializeDialog();
        AutoCompleteDecorator.decorate(userRoleCombo);
    }

    private void initializeDialog() {
        // Center the dialog
        setLocationRelativeTo(getParent());

        // Load user roles and user data
        loadUserRoles();
        loadUserData();

        // Load SVG eye icons with #999999 color
        try {
            // Load SVG icons for eye open and closed with gray color
            eyeOpenIcon = new FlatSVGIcon("lk/com/pos/icon/eye-open.svg", 25, 25);

            eyeClosedIcon = new FlatSVGIcon("lk/com/pos/icon/eye-closed.svg", 25, 25);

            // Set initial icons
            newPasswordEyeButton.setIcon(eyeClosedIcon);
            confirmPasswordEyeButton.setIcon(eyeClosedIcon);

        } catch (Exception e) {
            // SVG eye icons not found - silent failure
        }

        // Remove button borders and background
        newPasswordEyeButton.setBorderPainted(false);
        newPasswordEyeButton.setContentAreaFilled(false);
        newPasswordEyeButton.setFocusPainted(false);
        newPasswordEyeButton.setText("");

        confirmPasswordEyeButton.setBorderPainted(false);
        confirmPasswordEyeButton.setContentAreaFilled(false);
        confirmPasswordEyeButton.setFocusPainted(false);
        confirmPasswordEyeButton.setText("");

        // Setup button styles
        setupButtonStyles();

        // Setup keyboard navigation
        setupFocusTraversal();

        // Make username field non-editable
        userNameField.setEditable(false);
        userNameField.setBackground(new java.awt.Color(240, 240, 240));

        // Initially hide password fields
        setPasswordFieldsVisible(false);

        // Set initial focus
        userRoleCombo.requestFocus();

        // Set default size to (456, 300) as you requested
        setSize(456, 300);
        setMinimumSize(new java.awt.Dimension(456, 300));
    }

    private void setupGradientButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.decode("#0893B0"));
        button.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                int w = c.getWidth();
                int h = c.getHeight();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Check button state
                boolean isHover = button.getModel().isRollover();
                boolean isPressed = button.getModel().isPressed();
                boolean isFocused = button.hasFocus();

                // Default state - transparent with blue border
                if (!isFocused && !isHover && !isPressed) {
                    g2.setColor(new Color(0, 0, 0, 0)); // Transparent
                    g2.fillRoundRect(0, 0, w, h, 5, 5);

                    // Draw border
                    g2.setColor(Color.decode("#0893B0"));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
                } else {
                    // Gradient colors for hover/focus/pressed state
                    Color topColor = new Color(0x12, 0xB5, 0xA6); // Light
                    Color bottomColor = new Color(0x08, 0x93, 0xB0); // Dark

                    // Draw gradient
                    GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 5, 5);
                }

                // Draw button text
                super.paint(g, c);
            }
        });
    }

    private void setupButtonStyles() {
        // Remove borders, backgrounds, and set hover effects for eye buttons
        newPasswordEyeButton.setBorderPainted(false);
        newPasswordEyeButton.setContentAreaFilled(false);
        newPasswordEyeButton.setFocusPainted(false);
        newPasswordEyeButton.setOpaque(false);

        confirmPasswordEyeButton.setBorderPainted(false);
        confirmPasswordEyeButton.setContentAreaFilled(false);
        confirmPasswordEyeButton.setFocusPainted(false);
        confirmPasswordEyeButton.setOpaque(false);

        // Setup gradient buttons
        setupGradientButton(addBtn);
        setupGradientButton(cancelBtn);
        setupGradientButton(PasswordBtn);
        setupGradientButton(clearFormBtn);

        // FIXED: Swap the icons as requested
        // Cancel button gets CLEAR icon
        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(clearIcon);

        // Clear button gets CANCEL icon  
        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(cancelIcon);

        // Other buttons keep their original icons
        FlatSVGIcon updateIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
        updateIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        addBtn.setIcon(updateIcon);

        FlatSVGIcon passwordIcon = new FlatSVGIcon("lk/com/pos/icon/password.svg", 25, 25);
        passwordIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        PasswordBtn.setIcon(passwordIcon);

        // Setup mouse listeners for all buttons
        setupButtonMouseListeners();
        setupButtonFocusListeners();
        setupEyeButtonStyles();
    }

    private void setupButtonMouseListeners() {
        // Mouse listeners for addBtn (Update button)
        addBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                addBtn.setIcon(hoverIcon);
                addBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                addBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addBtn.setIcon(normalIcon);
                addBtn.repaint();
            }
        });

        // Mouse listeners for cancelBtn (Cancel button - now has CLEAR icon)
        cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(hoverIcon);
                cancelBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });

        // Mouse listeners for PasswordBtn (Change Password button)
        PasswordBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                PasswordBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/password.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                PasswordBtn.setIcon(hoverIcon);
                PasswordBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                PasswordBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/password.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                PasswordBtn.setIcon(normalIcon);
                PasswordBtn.repaint();
            }
        });

        // Mouse listeners for clearFormBtn (Clear Form button - now has CANCEL icon)
        clearFormBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(hoverIcon);
                clearFormBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
            }
        });
    }

    private void setupButtonFocusListeners() {
        // Focus listeners for addBtn (Update button)
        addBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                addBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                addBtn.setIcon(focusedIcon);
                addBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                addBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/update.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addBtn.setIcon(normalIcon);
                addBtn.repaint();
            }
        });

        // Focus listeners for cancelBtn (Cancel button - now has CLEAR icon)
        cancelBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(focusedIcon);
                cancelBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });

        // Focus listeners for PasswordBtn (Change Password button)
        PasswordBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                PasswordBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/password.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                PasswordBtn.setIcon(focusedIcon);
                PasswordBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                PasswordBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/password.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                PasswordBtn.setIcon(normalIcon);
                PasswordBtn.repaint();
            }
        });

        // Focus listeners for clearFormBtn (Clear Form button - now has CANCEL icon)
        clearFormBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(focusedIcon);
                clearFormBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
            }
        });
    }

    private void setupEyeButtonStyles() {
        // Mouse listeners for eye buttons
        newPasswordEyeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                applyEyeButtonHoverEffect(newPasswordEyeButton, true);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                applyEyeButtonHoverEffect(newPasswordEyeButton, false);
            }
        });

        confirmPasswordEyeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                applyEyeButtonHoverEffect(confirmPasswordEyeButton, true);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                applyEyeButtonHoverEffect(confirmPasswordEyeButton, false);
            }
        });

        // Focus listeners for eye buttons
        newPasswordEyeButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                applyEyeButtonHoverEffect(newPasswordEyeButton, true);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                applyEyeButtonHoverEffect(newPasswordEyeButton, false);
            }
        });

        confirmPasswordEyeButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                applyEyeButtonHoverEffect(confirmPasswordEyeButton, true);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                applyEyeButtonHoverEffect(confirmPasswordEyeButton, false);
            }
        });
    }

    private void applyEyeButtonHoverEffect(JButton button, boolean active) {
        try {
            if (active) {
                // Create blue tinted icons for hover/focus state
                FlatSVGIcon eyeIcon;
                if (button == newPasswordEyeButton) {
                    eyeIcon = new FlatSVGIcon("lk/com/pos/icon/" + (newPasswordVisible ? "eye-open.svg" : "eye-closed.svg"), 25, 25);
                } else {
                    eyeIcon = new FlatSVGIcon("lk/com/pos/icon/" + (confirmPasswordVisible ? "eye-open.svg" : "eye-closed.svg"), 25, 25);
                }
                eyeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#0893B0")));
                button.setIcon(eyeIcon);
            } else {
                // Reset to original gray icons (#999999)
                if (button == newPasswordEyeButton) {
                    button.setIcon(newPasswordVisible ? eyeOpenIcon : eyeClosedIcon);
                } else {
                    button.setIcon(confirmPasswordVisible ? eyeOpenIcon : eyeClosedIcon);
                }
            }
        } catch (Exception e) {
            // Error applying hover effect - silent failure
        }
    }

    private void setupFocusTraversal() {
        // Create focus traversal order
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(
                userNameField,
                userRoleCombo,
                PasswordBtn,
                newPasswordField,
                newPasswordEyeButton,
                confirmPasswordField,
                confirmPasswordEyeButton,
                addBtn,
                clearFormBtn,
                cancelBtn
        );

        // Set focus traversal keys
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)));

        // Add comprehensive key navigation
        setupArrowKeyNavigation();
        addEnterKeyNavigation();

        // Add ESC key to close - GLOBAL ESC LISTENER
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Setup tooltips
        userNameField.setToolTipText("Username (read-only)");
        userRoleCombo.setToolTipText("Press DOWN arrow, SPACE or F4 to open dropdown, use UP/DOWN arrows to navigate, ENTER to select");
        PasswordBtn.setToolTipText("Press ENTER or SPACE to toggle password fields visibility");
        newPasswordField.setToolTipText("Type new password and press ENTER to move to next field");
        confirmPasswordField.setToolTipText("Type confirm password and press ENTER to move to next field");
        newPasswordEyeButton.setToolTipText("Press ENTER or SPACE to toggle password visibility");
        confirmPasswordEyeButton.setToolTipText("Press ENTER or SPACE to toggle confirm password visibility");
        addBtn.setToolTipText("Press ENTER to update profile");
        clearFormBtn.setToolTipText("Press ENTER to clear form");
        cancelBtn.setToolTipText("Press ENTER to cancel, ESC to close dialog");
    }

    private void setupArrowKeyNavigation() {
        // Add arrow key navigation to all components
        userNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, userNameField);
            }
        });

        userRoleCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, userRoleCombo);
            }
        });

        PasswordBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, PasswordBtn);
            }
        });

        newPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, newPasswordField);
            }
        });

        newPasswordEyeButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, newPasswordEyeButton);
            }
        });

        confirmPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, confirmPasswordField);
            }
        });

        confirmPasswordEyeButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, confirmPasswordEyeButton);
            }
        });

        addBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, addBtn);
            }
        });

        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, clearFormBtn);
            }
        });

        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, cancelBtn);
            }
        });
    }

    private void handleArrowNavigation(java.awt.event.KeyEvent evt, java.awt.Component source) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                handleRightArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_LEFT:
                handleLeftArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                handleDownArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_UP:
                handleUpArrow(source);
                evt.consume();
                break;
            case KeyEvent.VK_ESCAPE:
                // ESC key closes the dialog from any component
                dispose();
                evt.consume();
                break;
        }
    }
// Replace your current button navigation methods with these:

    private void handleRightArrow(java.awt.Component source) {
        if (source == userNameField) {
            userRoleCombo.requestFocusInWindow();
        } else if (source == userRoleCombo) {
            PasswordBtn.requestFocusInWindow();
        } else if (source == PasswordBtn) {
            if (newPasswordField.isVisible()) {
                newPasswordField.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow(); // Go to first button
            }
        } else if (source == newPasswordField) {
            newPasswordEyeButton.requestFocusInWindow();
        } else if (source == newPasswordEyeButton) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == confirmPasswordField) {
            confirmPasswordEyeButton.requestFocusInWindow();
        } else if (source == confirmPasswordEyeButton) {
            cancelBtn.requestFocusInWindow(); // Go to first button
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow(); // Cancel -> Clear Form
        } else if (source == clearFormBtn) {
            addBtn.requestFocusInWindow(); // Clear Form -> Update
        } else if (source == addBtn) {
            userNameField.requestFocusInWindow(); // Update -> back to start
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == userNameField) {
            addBtn.requestFocusInWindow(); // Back to last button
        } else if (source == userRoleCombo) {
            userNameField.requestFocusInWindow();
        } else if (source == PasswordBtn) {
            userRoleCombo.requestFocusInWindow();
        } else if (source == newPasswordField) {
            PasswordBtn.requestFocusInWindow();
        } else if (source == newPasswordEyeButton) {
            newPasswordField.requestFocusInWindow();
        } else if (source == confirmPasswordField) {
            newPasswordEyeButton.requestFocusInWindow();
        } else if (source == confirmPasswordEyeButton) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == cancelBtn) {
            if (confirmPasswordField.isVisible()) {
                confirmPasswordEyeButton.requestFocusInWindow();
            } else {
                PasswordBtn.requestFocusInWindow();
            }
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow(); // Clear Form -> Cancel
        } else if (source == addBtn) {
            clearFormBtn.requestFocusInWindow(); // Update -> Clear Form
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == userNameField) {
            userRoleCombo.requestFocusInWindow();
        } else if (source == userRoleCombo) {
            PasswordBtn.requestFocusInWindow();
        } else if (source == PasswordBtn) {
            if (newPasswordField.isVisible()) {
                newPasswordField.requestFocusInWindow();
            } else {
                cancelBtn.requestFocusInWindow();
            }
        } else if (source == newPasswordField) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == newPasswordEyeButton) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == confirmPasswordField) {
            cancelBtn.requestFocusInWindow();
        } else if (source == confirmPasswordEyeButton) {
            cancelBtn.requestFocusInWindow();
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow();
        } else if (source == clearFormBtn) {
            addBtn.requestFocusInWindow();
        } else if (source == addBtn) {
            userNameField.requestFocusInWindow(); // Back to top
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == userNameField) {
            addBtn.requestFocusInWindow(); // Back to bottom
        } else if (source == userRoleCombo) {
            userNameField.requestFocusInWindow();
        } else if (source == PasswordBtn) {
            userRoleCombo.requestFocusInWindow();
        } else if (source == newPasswordField) {
            PasswordBtn.requestFocusInWindow();
        } else if (source == newPasswordEyeButton) {
            PasswordBtn.requestFocusInWindow();
        } else if (source == confirmPasswordField) {
            newPasswordField.requestFocusInWindow();
        } else if (source == confirmPasswordEyeButton) {
            newPasswordField.requestFocusInWindow();
        } else if (source == cancelBtn) {
            if (confirmPasswordField.isVisible()) {
                confirmPasswordEyeButton.requestFocusInWindow();
            } else {
                PasswordBtn.requestFocusInWindow();
            }
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow();
        } else if (source == addBtn) {
            clearFormBtn.requestFocusInWindow();
        }
    }

    private void addEnterKeyNavigation() {
        // Map components to their next focus targets for Enter key
        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(userNameField, userRoleCombo);
        enterNavigationMap.put(userRoleCombo, PasswordBtn);
        enterNavigationMap.put(PasswordBtn, addBtn);
        enterNavigationMap.put(newPasswordField, newPasswordEyeButton);
        enterNavigationMap.put(newPasswordEyeButton, confirmPasswordField);
        enterNavigationMap.put(confirmPasswordField, confirmPasswordEyeButton);
        enterNavigationMap.put(confirmPasswordEyeButton, addBtn);
        enterNavigationMap.put(addBtn, clearFormBtn);
        enterNavigationMap.put(clearFormBtn, cancelBtn);
        enterNavigationMap.put(cancelBtn, userNameField);

        // Add key listeners to all components
        for (java.awt.Component component : enterNavigationMap.keySet()) {
            component.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        java.awt.Component nextComponent = enterNavigationMap.get(component);
                        if (nextComponent != null) {
                            nextComponent.requestFocusInWindow();
                        }
                        evt.consume();
                    }
                }
            });
        }

        // Special handling for eye buttons - also trigger toggle on Enter and Space
        newPasswordEyeButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    toggleNewPasswordVisibility();
                    evt.consume();
                }
            }
        });

        confirmPasswordEyeButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    toggleConfirmPasswordVisibility();
                    evt.consume();
                }
            }
        });

        // Special handling for PasswordBtn - toggle password fields on Enter and Space
        PasswordBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    togglePasswordFields();
                    evt.consume();
                }
            }
        });

        // Special handling for action buttons
        addBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (validateInputs()) {
                        updateUser();
                    }
                    evt.consume();
                }
            }
        });

        clearFormBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    clearForm();
                    evt.consume();
                }
            }
        });

        cancelBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                    evt.consume();
                }
            }
        });

        // Special handling for combo box with proper popup opening
        userRoleCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (!userRoleCombo.isPopupVisible()) {
                            userRoleCombo.showPopup();
                            evt.consume();
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (!userRoleCombo.isPopupVisible()) {
                            userRoleCombo.showPopup();
                            evt.consume();
                        }
                        break;
                    case KeyEvent.VK_SPACE:
                        if (!userRoleCombo.isPopupVisible()) {
                            userRoleCombo.showPopup();
                            evt.consume();
                        }
                        break;
                    case KeyEvent.VK_F4:
                        if (!userRoleCombo.isPopupVisible()) {
                            userRoleCombo.showPopup();
                            evt.consume();
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                        if (userRoleCombo.isPopupVisible()) {
                            // Close popup and stay in combo box
                            userRoleCombo.setPopupVisible(false);
                            evt.consume();
                        } else {
                            // Move to next field only if popup is not visible
                            PasswordBtn.requestFocusInWindow();
                            evt.consume();
                        }
                        break;
                }
            }
        });

        // Add mouse listener to combo box for better UX
        userRoleCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userRoleCombo.showPopup();
            }
        });

        // Add action listener to auto-move focus when item is selected
        userRoleCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Auto-move to next field when an item is selected (but not the placeholder)
                if (userRoleCombo.getSelectedIndex() > 0 && !userRoleCombo.isPopupVisible()) {
                    PasswordBtn.requestFocusInWindow();
                }
            }
        });
    }

    // ---------------- PASSWORD FIELDS VISIBILITY ----------------
    private void setPasswordFieldsVisible(boolean visible) {
        newPasswordField.setVisible(visible);
        newPasswordEyeButton.setVisible(visible);
        confirmPasswordField.setVisible(visible);
        confirmPasswordEyeButton.setVisible(visible);

        if (!visible) {
            // Clear password fields when hiding
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        }

        // Force UI update
        revalidate();
        repaint();
    }

    private void togglePasswordFields() {
        boolean currentlyVisible = newPasswordField.isVisible();
        setPasswordFieldsVisible(!currentlyVisible);

        if (!currentlyVisible) {
            newPasswordField.requestFocus();
            setSize(456, 420); // Extended height for password fields
        } else {
            setSize(456, 300); // Default height
        }

        // Center the dialog after resizing
        setLocationRelativeTo(getParent());
    }

    // ---------------- PASSWORD TOGGLE METHODS ----------------
    private void toggleNewPasswordVisibility() {
        if (newPasswordVisible) {
            newPasswordField.setEchoChar('•');
            newPasswordEyeButton.setIcon(eyeClosedIcon);
        } else {
            newPasswordField.setEchoChar((char) 0);
            newPasswordEyeButton.setIcon(eyeOpenIcon);
        }
        newPasswordVisible = !newPasswordVisible;

        // Re-apply hover effect if needed
        if (newPasswordEyeButton.hasFocus()) {
            applyEyeButtonHoverEffect(newPasswordEyeButton, true);
        }
    }

    private void toggleConfirmPasswordVisibility() {
        if (confirmPasswordVisible) {
            confirmPasswordField.setEchoChar('•');
            confirmPasswordEyeButton.setIcon(eyeClosedIcon);
        } else {
            confirmPasswordField.setEchoChar((char) 0);
            confirmPasswordEyeButton.setIcon(eyeOpenIcon);
        }
        confirmPasswordVisible = !confirmPasswordVisible;

        // Re-apply hover effect if needed
        if (confirmPasswordEyeButton.hasFocus()) {
            applyEyeButtonHoverEffect(confirmPasswordEyeButton, true);
        }
    }

    // ---------------- LOAD USER DATA ----------------
    private void loadUserData() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT u.name, u.password, r.role_name FROM user u "
                    + "INNER JOIN role r ON u.role_id = r.role_id WHERE u.user_id = " + currentUserId);

            if (rs.next()) {
                currentUsername = rs.getString("name");
                userNameField.setText(currentUsername);

                // Set the role in combo box
                String userRole = rs.getString("role_name");
                for (int i = 0; i < userRoleCombo.getItemCount(); i++) {
                    if (userRoleCombo.getItemAt(i).equals(userRole)) {
                        userRoleCombo.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "User data not found!");
                this.dispose();
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading user data: " + e.getMessage());
            this.dispose();
        }
    }

    // ---------------- DATABASE & VALIDATION ----------------
    private void loadUserRoles() {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT * FROM role");
            Vector<String> userRoles = new Vector<>();
            userRoles.add("Select User Role");
            while (rs.next()) {
                userRoles.add(rs.getString("role_name"));
            }
            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(userRoles);
            userRoleCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading user roles: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Only validate passwords if they are visible (user wants to change password)
        if (newPasswordField.isVisible()) {
            if (!Validater.isPasswordValid(newPassword)) {
                newPasswordField.requestFocus();
                return false;
            }

            if (!newPassword.equals(confirmPassword)) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "New Password and Confirm Password do not match!");
                confirmPasswordField.requestFocus();
                return false;
            }
        }

        if (userRoleCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a user role");
            userRoleCombo.requestFocus();
            return false;
        }

        return true;
    }

    private int getRoleId(String roleName) {
        try {
            ResultSet rs = MySQL.executeSearch("SELECT role_id FROM role WHERE role_name='" + roleName + "'");
            if (rs.next()) {
                return rs.getInt("role_id");
            }
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error getting role ID: " + e.getMessage());
        }
        return -1;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Password encryption error: " + e.getMessage());
            return password; // fallback
        }
    }

private void updateUser() {
    java.sql.Connection conn = null;
    java.sql.PreparedStatement pst = null;
    
    try {
        String newPassword = new String(newPasswordField.getPassword());
        String roleName = userRoleCombo.getSelectedItem().toString();
        int roleId = getRoleId(roleName);

        if (roleId == -1) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Invalid role selected!");
            return;
        }

        // Get database connection
        conn = MySQL.getConnection();
        
        // Start transaction
        conn.setAutoCommit(false);
        
        String sql;
        boolean passwordChanged = false;
        
        if (!newPassword.isEmpty() && newPasswordField.isVisible()) {
            // Update password and role
            String hashedPassword = hashPassword(newPassword);
            sql = "UPDATE user SET password = ?, role_id = ? WHERE user_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, hashedPassword);
            pst.setInt(2, roleId);
            pst.setInt(3, currentUserId);
            passwordChanged = true;
        } else {
            // Update only role
            sql = "UPDATE user SET role_id = ? WHERE user_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, roleId);
            pst.setInt(2, currentUserId);
        }

        int rowsAffected = pst.executeUpdate();

        if (rowsAffected > 0) {
            // Create notification for profile update
            createProfileUpdateNotification(passwordChanged, roleName, conn);
            
            // Commit transaction
            conn.commit();
            
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                    "Profile updated successfully!");
            this.dispose();
        } else {
            conn.rollback();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Failed to update profile!");
        }

    } catch (Exception e) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (Exception rollbackEx) {
            // Silent rollback failure
        }
        Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                "Error updating profile: " + e.getMessage());
    } finally {
        // Close resources
        try {
            if (pst != null) pst.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            // Silent resource closing failure
        }
    }
}

private void createProfileUpdateNotification(boolean passwordChanged, String newRole, java.sql.Connection conn) {
    java.sql.PreparedStatement pstMassage = null;
    java.sql.PreparedStatement pstNotification = null;
    
    try {
        // Get old role for comparison
        String oldRole = "";
        java.sql.PreparedStatement pstOldRole = conn.prepareStatement("SELECT r.role_name FROM user u JOIN role r ON u.role_id = r.role_id WHERE u.user_id = ?");
        pstOldRole.setInt(1, currentUserId);
        java.sql.ResultSet rs = pstOldRole.executeQuery();
        if (rs.next()) {
            oldRole = rs.getString("role_name");
        }
        pstOldRole.close();
        
        // Create the message based on what was changed
        String messageText;
        if (passwordChanged && !oldRole.equals(newRole)) {
            messageText = String.format("Profile updated for %s: Password changed and role changed from %s to %s", 
                                      currentUsername, oldRole, newRole);
        } else if (passwordChanged) {
            messageText = String.format("Profile updated for %s: Password changed", currentUsername);
        } else if (!oldRole.equals(newRole)) {
            messageText = String.format("Profile updated for %s: Role changed from %s to %s", 
                                      currentUsername, oldRole, newRole);
        } else {
            messageText = String.format("Profile updated for %s: No significant changes detected", currentUsername);
        }
        
        // Check if this exact message already exists to avoid duplicates
        String checkSql = "SELECT COUNT(*) FROM massage WHERE massage = ?";
        pstMassage = conn.prepareStatement(checkSql);
        pstMassage.setString(1, messageText);
        rs = pstMassage.executeQuery();
        
        int massageId;
        if (rs.next() && rs.getInt(1) > 0) {
            // Message already exists, get its ID
            String getSql = "SELECT massage_id FROM massage WHERE massage = ?";
            pstMassage.close();
            pstMassage = conn.prepareStatement(getSql);
            pstMassage.setString(1, messageText);
            rs = pstMassage.executeQuery();
            rs.next();
            massageId = rs.getInt(1);
        } else {
            // Insert new message
            pstMassage.close();
            String insertMassageSql = "INSERT INTO massage (massage) VALUES (?)";
            pstMassage = conn.prepareStatement(insertMassageSql, java.sql.PreparedStatement.RETURN_GENERATED_KEYS);
            pstMassage.setString(1, messageText);
            pstMassage.executeUpdate();
            
            // Get the generated massage_id
            rs = pstMassage.getGeneratedKeys();
            if (rs.next()) {
                massageId = rs.getInt(1);
            } else {
                throw new java.sql.SQLException("Failed to get generated massage ID");
            }
        }
        
        // Insert notification (msg_type_id 22 = 'Edit Profile' from your msg_type table)
        String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)";
        pstNotification = conn.prepareStatement(notificationSql);
        pstNotification.setInt(1, 1); // is_read = 1 (unread)
        pstNotification.setInt(2, 22); // msg_type_id 22 = 'Edit Profile'
        pstNotification.setInt(3, massageId);
        pstNotification.executeUpdate();
        
    } catch (Exception e) {
        // Silent notification creation failure
    } finally {
        // Close resources
        try {
            if (pstMassage != null) pstMassage.close();
            if (pstNotification != null) pstNotification.close();
        } catch (Exception e) {
            // Silent resource closing failure
        }
    }
}
    private void clearForm() {
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        userRoleCombo.setSelectedIndex(0);
        loadUserData(); // Reload original user data
        userRoleCombo.requestFocus();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        cancelBtn = new javax.swing.JButton();
        addBtn = new javax.swing.JButton();
        userNameField = new javax.swing.JTextField();
        userRoleCombo = new javax.swing.JComboBox<>();
        confirmPasswordField = new javax.swing.JPasswordField();
        confirmPasswordEyeButton = new javax.swing.JButton();
        newPasswordField = new javax.swing.JPasswordField();
        newPasswordEyeButton = new javax.swing.JButton();
        PasswordBtn = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Profile");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(456, 387));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(8, 147, 176));
        jLabel1.setText("Edit Profile");

        jSeparator1.setForeground(new java.awt.Color(0, 137, 176));

        cancelBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        cancelBtn.setForeground(new java.awt.Color(8, 147, 176));
        cancelBtn.setText("Cancel");
        cancelBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        addBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        addBtn.setForeground(new java.awt.Color(8, 147, 176));
        addBtn.setText("Update");
        addBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        userNameField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        userNameField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Name *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        userRoleCombo.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        userRoleCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        userRoleCombo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Role *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        confirmPasswordField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        confirmPasswordField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Confirmed Password *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        confirmPasswordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmPasswordFieldActionPerformed(evt);
            }
        });

        confirmPasswordEyeButton.setText("botton");
        confirmPasswordEyeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmPasswordEyeButtonActionPerformed(evt);
            }
        });

        newPasswordField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        newPasswordField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "New Password *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        newPasswordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPasswordFieldActionPerformed(evt);
            }
        });

        newPasswordEyeButton.setText("botton");
        newPasswordEyeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPasswordEyeButtonActionPerformed(evt);
            }
        });

        PasswordBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        PasswordBtn.setForeground(new java.awt.Color(8, 147, 176));
        PasswordBtn.setText("Change Password");
        PasswordBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        PasswordBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PasswordBtnActionPerformed(evt);
            }
        });

        clearFormBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 16)); // NOI18N
        clearFormBtn.setForeground(new java.awt.Color(8, 147, 176));
        clearFormBtn.setText("Clear Form");
        clearFormBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(userNameField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(confirmPasswordField)
                                    .addComponent(newPasswordField))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(newPasswordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, Short.MAX_VALUE)
                                    .addComponent(confirmPasswordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(userRoleCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(PasswordBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(22, 22, 22))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PasswordBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(userRoleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newPasswordField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newPasswordEyeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(confirmPasswordField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(confirmPasswordEyeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                    .addComponent(addBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        if (validateInputs()) {
            updateUser();
        }
    }//GEN-LAST:event_addBtnActionPerformed

    private void confirmPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmPasswordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_confirmPasswordFieldActionPerformed

    private void confirmPasswordEyeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmPasswordEyeButtonActionPerformed
        toggleConfirmPasswordVisibility();
    }//GEN-LAST:event_confirmPasswordEyeButtonActionPerformed

    private void newPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPasswordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newPasswordFieldActionPerformed

    private void newPasswordEyeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPasswordEyeButtonActionPerformed
        toggleNewPasswordVisibility();
    }//GEN-LAST:event_newPasswordEyeButtonActionPerformed

    private void PasswordBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PasswordBtnActionPerformed
        togglePasswordFields();
    }//GEN-LAST:event_PasswordBtnActionPerformed

    private void clearFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFormBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearFormBtnActionPerformed

    private void clearFormBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearFormBtnKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearForm();
        } else {
            handleArrowNavigation(evt, clearFormBtn);
        }
    }//GEN-LAST:event_clearFormBtnKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatLightLaf.setup();

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
            java.util.logging.Logger.getLogger(EditProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EditProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EditProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EditProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EditProfile dialog = new EditProfile(new javax.swing.JFrame(), true, 1); // Replace 1 with actual user ID
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
    private javax.swing.JButton PasswordBtn;
    private javax.swing.JButton addBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JButton confirmPasswordEyeButton;
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton newPasswordEyeButton;
    private javax.swing.JPasswordField newPasswordField;
    private javax.swing.JTextField userNameField;
    private javax.swing.JComboBox<String> userRoleCombo;
    // End of variables declaration//GEN-END:variables
}
