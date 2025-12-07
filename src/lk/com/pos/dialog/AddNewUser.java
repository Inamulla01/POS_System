package lk.com.pos.dialog;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lk.com.pos.validation.Validater;
import lk.com.pos.connection.DB; // CHANGED: Updated import
import lk.com.pos.connection.DB.ResultSetHandler; // ADDED: Import for ResultSetHandler
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import raven.toast.Notifications;

public class AddNewUser extends javax.swing.JDialog {

    // ---------------- PASSWORD TOGGLE ----------------
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;
    private Icon eyeOpenIcon;
    private Icon eyeClosedIcon;

    public AddNewUser(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeDialog();
        AutoCompleteDecorator.decorate(userRoleCombo);
    }

    private void initializeDialog() {
        // Center the dialog
        setLocationRelativeTo(getParent());

        // Load user roles
        loadUserRoles();

        // Load SVG eye icons
        try {
            // Load SVG icons for eye open and closed
            eyeOpenIcon = new FlatSVGIcon("lk/com/pos/icon/eye-open.svg", 25, 25);
            eyeClosedIcon = new FlatSVGIcon("lk/com/pos/icon/eye-closed.svg", 25, 25);

            // Set initial icons
            passwordEyeButton.setIcon(eyeClosedIcon);
            confirmPasswordEyeButton.setIcon(eyeClosedIcon);

        } catch (Exception e) {
            // SVG icons not found, continue without them
        }

        // Remove button borders and background
        passwordEyeButton.setBorderPainted(false);
        passwordEyeButton.setContentAreaFilled(false);
        passwordEyeButton.setFocusPainted(false);
        passwordEyeButton.setText("");

        confirmPasswordEyeButton.setBorderPainted(false);
        confirmPasswordEyeButton.setContentAreaFilled(false);
        confirmPasswordEyeButton.setFocusPainted(false);
        confirmPasswordEyeButton.setText("");

        // Setup button styles
        setupButtonStyles();

        // Setup keyboard navigation
        setupFocusTraversal();

        // Set initial focus
        userNameField.requestFocus();
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
        passwordEyeButton.setBorderPainted(false);
        passwordEyeButton.setContentAreaFilled(false);
        passwordEyeButton.setFocusPainted(false);
        passwordEyeButton.setOpaque(false);

        confirmPasswordEyeButton.setBorderPainted(false);
        confirmPasswordEyeButton.setContentAreaFilled(false);
        confirmPasswordEyeButton.setFocusPainted(false);
        confirmPasswordEyeButton.setOpaque(false);

        // Setup gradient buttons
        setupGradientButton(addBtn);
        setupGradientButton(clearFormBtn);
        setupGradientButton(cancelBtn);

        // Create icons with original blue color for action buttons
        FlatSVGIcon addIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
        addIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        addBtn.setIcon(addIcon);

        FlatSVGIcon clearIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
        clearIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        clearFormBtn.setIcon(clearIcon);

        FlatSVGIcon cancelIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
        cancelIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
        cancelBtn.setIcon(cancelIcon);

        // Setup mouse listeners for all buttons
        setupButtonMouseListeners();
        setupButtonFocusListeners();
        setupEyeButtonStyles();
    }

    private void setupButtonMouseListeners() {
        // Mouse listeners for addBtn
        addBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                addBtn.setIcon(hoverIcon);
                addBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                addBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addBtn.setIcon(normalIcon);
                addBtn.repaint();
            }
        });

        // Mouse listeners for clearFormBtn
        clearFormBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(hoverIcon);
                clearFormBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
            }
        });

        // Mouse listeners for cancelBtn
        cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon hoverIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                hoverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(hoverIcon);
                cancelBtn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });
    }

    private void setupButtonFocusListeners() {
        // Focus listeners for addBtn
        addBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                addBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                addBtn.setIcon(focusedIcon);
                addBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                addBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/add.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                addBtn.setIcon(normalIcon);
                addBtn.repaint();
            }
        });

        // Focus listeners for clearFormBtn
        clearFormBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                clearFormBtn.setIcon(focusedIcon);
                clearFormBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                clearFormBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/clear.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                clearFormBtn.setIcon(normalIcon);
                clearFormBtn.repaint();
            }
        });

        // Focus listeners for cancelBtn
        cancelBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.WHITE);
                FlatSVGIcon focusedIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                focusedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                cancelBtn.setIcon(focusedIcon);
                cancelBtn.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                cancelBtn.setForeground(Color.decode("#0893B0"));
                FlatSVGIcon normalIcon = new FlatSVGIcon("lk/com/pos/icon/cancel.svg", 25, 25);
                normalIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.decode("#0893B0")));
                cancelBtn.setIcon(normalIcon);
                cancelBtn.repaint();
            }
        });
    }

    private void setupEyeButtonStyles() {
        // Mouse listeners for eye buttons
        passwordEyeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                applyEyeButtonHoverEffect(passwordEyeButton, true);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                applyEyeButtonHoverEffect(passwordEyeButton, false);
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
        passwordEyeButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                applyEyeButtonHoverEffect(passwordEyeButton, true);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                applyEyeButtonHoverEffect(passwordEyeButton, false);
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
                if (button == passwordEyeButton) {
                    eyeIcon = new FlatSVGIcon("lk/com/pos/icon/" + (passwordVisible ? "eye-open.svg" : "eye-closed.svg"), 25, 25);
                } else {
                    eyeIcon = new FlatSVGIcon("lk/com/pos/icon/" + (confirmPasswordVisible ? "eye-open.svg" : "eye-closed.svg"), 25, 25);
                }
                eyeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#0893B0")));
                button.setIcon(eyeIcon);
            } else {
                // Reset to original icons
                if (button == passwordEyeButton) {
                    button.setIcon(passwordVisible ? eyeOpenIcon : eyeClosedIcon);
                } else {
                    button.setIcon(confirmPasswordVisible ? eyeOpenIcon : eyeClosedIcon);
                }
            }
        } catch (Exception e) {
            // Error applying hover effect, continue without it
        }
    }

    private void setupFocusTraversal() {
        // Create focus traversal order
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(
                userNameField,
                passwordField,
                passwordEyeButton,
                confirmPasswordField,
                confirmPasswordEyeButton,
                userRoleCombo,
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
        userNameField.setToolTipText("Type username and press ENTER to move to next field");
        passwordField.setToolTipText("Type password and press ENTER to move to next field");
        confirmPasswordField.setToolTipText("Type confirm password and press ENTER to move to next field");
        passwordEyeButton.setToolTipText("Press ENTER or SPACE to toggle password visibility");
        confirmPasswordEyeButton.setToolTipText("Press ENTER or SPACE to toggle confirm password visibility");
        userRoleCombo.setToolTipText("Press DOWN arrow, SPACE or F4 to open dropdown, use UP/DOWN arrows to navigate, ENTER to select");
        addBtn.setToolTipText("Press ENTER to save user");
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

        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, passwordField);
            }
        });

        passwordEyeButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, passwordEyeButton);
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

        userRoleCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, userRoleCombo);
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

    private void handleRightArrow(java.awt.Component source) {
        if (source == userNameField) {
            passwordField.requestFocusInWindow();
        } else if (source == passwordField) {
            passwordEyeButton.requestFocusInWindow();
        } else if (source == passwordEyeButton) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == confirmPasswordField) {
            confirmPasswordEyeButton.requestFocusInWindow();
        } else if (source == confirmPasswordEyeButton) {
            userRoleCombo.requestFocusInWindow();
        } else if (source == userRoleCombo) {
            cancelBtn.requestFocusInWindow(); // Go to first button (Cancel)
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow(); // Cancel -> Clear Form
        } else if (source == clearFormBtn) {
            addBtn.requestFocusInWindow(); // Clear Form -> Save
        } else if (source == addBtn) {
            userNameField.requestFocusInWindow(); // Save -> back to start
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == userNameField) {
            addBtn.requestFocusInWindow(); // Loop back to last button (Save)
        } else if (source == passwordField) {
            userNameField.requestFocusInWindow();
        } else if (source == passwordEyeButton) {
            passwordField.requestFocusInWindow();
        } else if (source == confirmPasswordField) {
            passwordEyeButton.requestFocusInWindow();
        } else if (source == confirmPasswordEyeButton) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == userRoleCombo) {
            confirmPasswordEyeButton.requestFocusInWindow();
        } else if (source == cancelBtn) {
            userRoleCombo.requestFocusInWindow(); // Cancel -> back to combo
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow(); // Clear Form -> Cancel
        } else if (source == addBtn) {
            clearFormBtn.requestFocusInWindow(); // Save -> Clear Form
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == userNameField) {
            passwordField.requestFocusInWindow();
        } else if (source == passwordField) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == passwordEyeButton) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == confirmPasswordField) {
            userRoleCombo.requestFocusInWindow();
        } else if (source == confirmPasswordEyeButton) {
            userRoleCombo.requestFocusInWindow();
        } else if (source == userRoleCombo) {
            cancelBtn.requestFocusInWindow(); // Go to first button (Cancel)
        } else if (source == cancelBtn) {
            clearFormBtn.requestFocusInWindow(); // Cancel -> Clear Form
        } else if (source == clearFormBtn) {
            addBtn.requestFocusInWindow(); // Clear Form -> Save
        } else if (source == addBtn) {
            userNameField.requestFocusInWindow(); // Save -> back to start
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == userNameField) {
            addBtn.requestFocusInWindow(); // Loop back to last button (Save)
        } else if (source == passwordField) {
            userNameField.requestFocusInWindow();
        } else if (source == passwordEyeButton) {
            userNameField.requestFocusInWindow();
        } else if (source == confirmPasswordField) {
            passwordField.requestFocusInWindow();
        } else if (source == confirmPasswordEyeButton) {
            passwordField.requestFocusInWindow();
        } else if (source == userRoleCombo) {
            confirmPasswordField.requestFocusInWindow();
        } else if (source == cancelBtn) {
            userRoleCombo.requestFocusInWindow(); // Cancel -> back to combo
        } else if (source == clearFormBtn) {
            cancelBtn.requestFocusInWindow(); // Clear Form -> Cancel
        } else if (source == addBtn) {
            clearFormBtn.requestFocusInWindow(); // Save -> Clear Form
        }
    }

    private void addEnterKeyNavigation() {
        // Map components to their next focus targets for Enter key
        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(userNameField, passwordField);
        enterNavigationMap.put(passwordField, passwordEyeButton);
        enterNavigationMap.put(passwordEyeButton, confirmPasswordField);
        enterNavigationMap.put(confirmPasswordField, confirmPasswordEyeButton);
        enterNavigationMap.put(confirmPasswordEyeButton, userRoleCombo);
        enterNavigationMap.put(userRoleCombo, cancelBtn); // Go to first button (Cancel)
        enterNavigationMap.put(cancelBtn, clearFormBtn); // Cancel -> Clear Form
        enterNavigationMap.put(clearFormBtn, addBtn); // Clear Form -> Save
        enterNavigationMap.put(addBtn, userNameField); // Save -> back to start

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
        passwordEyeButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    togglePasswordVisibility();
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

        // Special handling for action buttons
        addBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (validateInputs()) {
                        saveUser();
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

        // FIXED: Combo box navigation - complete solution
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
                            // Close popup
                            userRoleCombo.setPopupVisible(false);
                            evt.consume();
                        } else {
                            // Only move to next field if a valid item is selected
                            if (userRoleCombo.getSelectedIndex() > 0) {
                                addBtn.requestFocusInWindow();
                            }
                            evt.consume();
                        }
                        break;
                    case KeyEvent.VK_TAB:
                        // Allow normal tab navigation
                        break;
                    default:
                        // For other keys, don't consume the event
                        break;
                }
            }
        });

        // FIXED: Mouse selection handler
        userRoleCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userRoleCombo.showPopup();
            }
        });

        // FIXED: Action listener for selection changes
        userRoleCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // When user selects an item from dropdown and it closes, move focus
                if (userRoleCombo.getSelectedIndex() > 0 && !userRoleCombo.isPopupVisible()) {
                    SwingUtilities.invokeLater(() -> {
                        addBtn.requestFocusInWindow();
                    });
                }
            }
        });

        // FIXED: Popup menu listener to detect when dropdown closes
        userRoleCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                // Popup is opening
            }

            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                // Popup is closing - if a valid item was selected, move focus
                SwingUtilities.invokeLater(() -> {
                    if (userRoleCombo.getSelectedIndex() > 0) {
                        addBtn.requestFocusInWindow();
                    }
                });
            }

            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                // Popup was canceled
            }
        });
    }

    // ---------------- PASSWORD TOGGLE METHODS ----------------
    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passwordField.setEchoChar('•');
            passwordEyeButton.setIcon(eyeClosedIcon);
        } else {
            passwordField.setEchoChar((char) 0);
            passwordEyeButton.setIcon(eyeOpenIcon);
        }
        passwordVisible = !passwordVisible;

        // Re-apply hover effect if needed
        if (passwordEyeButton.hasFocus()) {
            applyEyeButtonHoverEffect(passwordEyeButton, true);
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

    // ---------------- DATABASE & VALIDATION ----------------
    private void loadUserRoles() {
        try {
            // CHANGED: Using DB.executeQuerySafe for safer query execution
            List<String> roleNames = DB.executeQuerySafe(
                "SELECT role_name FROM role", 
                new ResultSetHandler<List<String>>() {
                    @Override
                    public List<String> handle(ResultSet rs) throws SQLException {
                        List<String> roles = new ArrayList<>();
                        roles.add("Select User Role");
                        while (rs.next()) {
                            roles.add(rs.getString("role_name"));
                        }
                        return roles;
                    }
                }
            );
            
            DefaultComboBoxModel<String> dcm = new DefaultComboBoxModel<>(
                roleNames.toArray(new String[0])
            );
            userRoleCombo.setModel(dcm);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error loading user roles: " + e.getMessage());
        }
    }

    private String formatUsername(String username) {
        return username.replaceAll("\\s+", "").toLowerCase();
    }

    private boolean validateInputs() {
        String rawUsername = userNameField.getText().trim();
        String formattedUsername = formatUsername(rawUsername);
        if (!rawUsername.equals(formattedUsername)) {
            userNameField.setText(formattedUsername);
        }

        if (!Validater.isInputFieldValid(formattedUsername)) {
            userNameField.requestFocus();
            return false;
        }

        if (isUsernameExists(formattedUsername)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Username '" + formattedUsername + "' already exists! Please choose a different username.");
            userNameField.requestFocus();
            userNameField.selectAll();
            return false;
        }

        String password = new String(passwordField.getPassword());
        if (!Validater.isPasswordValid(password)) {
            passwordField.requestFocus();
            return false;
        }

        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (!password.equals(confirmPassword)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Password and Confirm Password do not match!");
            confirmPasswordField.requestFocus();
            return false;
        }

        if (userRoleCombo.getSelectedIndex() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                    "Please select a user role");
            userRoleCombo.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isUsernameExists(String username) {
        try {
            // CHANGED: Using parameterized query to prevent SQL injection
            Integer count = DB.executeQuerySafe(
                "SELECT COUNT(*) as count FROM user WHERE name = ?",
                new ResultSetHandler<Integer>() {
                    @Override
                    public Integer handle(ResultSet rs) throws SQLException {
                        if (rs.next()) {
                            return rs.getInt("count");
                        }
                        return 0;
                    }
                },
                username
            );
            
            return count > 0;
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error checking username: " + e.getMessage());
        }
        return false;
    }

    private int getRoleId(String roleName) {
        try {
            // CHANGED: Using parameterized query
            Integer roleId = DB.executeQuerySafe(
                "SELECT role_id FROM role WHERE role_name = ?",
                new ResultSetHandler<Integer>() {
                    @Override
                    public Integer handle(ResultSet rs) throws SQLException {
                        if (rs.next()) {
                            return rs.getInt("role_id");
                        }
                        return -1;
                    }
                },
                roleName
            );
            
            return roleId;
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

    private void saveUser() {
        java.sql.Connection conn = null;
        
        try {
            String formattedUsername = formatUsername(userNameField.getText().trim());
            String password = new String(passwordField.getPassword());
            String roleName = userRoleCombo.getSelectedItem().toString();
            int roleId = getRoleId(roleName);

            if (roleId == -1) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                        "Invalid role selected!");
                return;
            }

            if (isUsernameExists(formattedUsername)) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                        "Username '" + formattedUsername + "' already exists! Please choose a different username.");
                userNameField.requestFocus();
                userNameField.selectAll();
                return;
            }

            String hashedPassword = hashPassword(password);

            // CHANGED: Get connection from new DB class
            conn = DB.getConnection();

            // Start transaction
            conn.setAutoCommit(false);

            try {
                // Insert user using PreparedStatement
                String insertUserSQL = "INSERT INTO user (name, password, role_id) VALUES (?, ?, ?)";
                try (java.sql.PreparedStatement pst = conn.prepareStatement(insertUserSQL)) {
                    pst.setString(1, formattedUsername);
                    pst.setString(2, hashedPassword);
                    pst.setInt(3, roleId);
                    
                    int rowsAffected = pst.executeUpdate();

                    if (rowsAffected > 0) {
                        // Create notification for new user
                        createUserNotification(formattedUsername, roleName, conn);

                        // Commit transaction
                        conn.commit();

                        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT,
                                "User '" + formattedUsername + "' added successfully!");
                        clearForm();
                    } else {
                        conn.rollback();
                        Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                                "Failed to add user!");
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                // Reset auto-commit
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Error saving user: " + e.getMessage());
        } finally {
            // Close connection using DB class helper
            DB.closeQuietly(conn);
        }
    }

    private void createUserNotification(String username, String roleName, java.sql.Connection conn) {
        try {
            // Create the message
            String messageText = "New user account created: " + username + " (" + roleName + ")";

            // Check if this exact message already exists to avoid duplicates
            String checkSql = "SELECT COUNT(*) FROM massage WHERE massage = ?";
            try (java.sql.PreparedStatement pstMassage = conn.prepareStatement(checkSql)) {
                pstMassage.setString(1, messageText);
                try (java.sql.ResultSet rs = pstMassage.executeQuery()) {
                    int massageId;
                    
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Message already exists, get its ID
                        String getSql = "SELECT massage_id FROM massage WHERE massage = ?";
                        try (java.sql.PreparedStatement pstGet = conn.prepareStatement(getSql)) {
                            pstGet.setString(1, messageText);
                            try (java.sql.ResultSet rs2 = pstGet.executeQuery()) {
                                if (rs2.next()) {
                                    massageId = rs2.getInt(1);
                                } else {
                                    return; // Should not happen
                                }
                            }
                        }
                    } else {
                        // Insert new message
                        String insertMassageSql = "INSERT INTO massage (massage) VALUES (?)";
                        try (java.sql.PreparedStatement pstInsert = conn.prepareStatement(insertMassageSql, 
                                java.sql.Statement.RETURN_GENERATED_KEYS)) {
                            pstInsert.setString(1, messageText);
                            pstInsert.executeUpdate();

                            // Get the generated massage_id
                            try (java.sql.ResultSet rs2 = pstInsert.getGeneratedKeys()) {
                                if (rs2.next()) {
                                    massageId = rs2.getInt(1);
                                } else {
                                    throw new java.sql.SQLException("Failed to get generated massage ID");
                                }
                            }
                        }
                    }

                    // Insert notification (msg_type_id 21 = 'Add New User' from your msg_type table)
                    String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (?, NOW(), ?, ?)";
                    try (java.sql.PreparedStatement pstNotification = conn.prepareStatement(notificationSql)) {
                        pstNotification.setInt(1, 1); // is_read = 1 (unread)
                        pstNotification.setInt(2, 21); // msg_type_id 21 = 'Add New User'
                        pstNotification.setInt(3, massageId);
                        pstNotification.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private void clearForm() {
        userNameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        userRoleCombo.setSelectedIndex(0);
        userNameField.requestFocus();
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
        passwordField = new javax.swing.JPasswordField();
        userRoleCombo = new javax.swing.JComboBox<>();
        confirmPasswordField = new javax.swing.JPasswordField();
        confirmPasswordEyeButton = new javax.swing.JButton();
        passwordEyeButton = new javax.swing.JButton();
        clearFormBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New User");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Nunito ExtraBold", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(8, 147, 176));
        jLabel1.setText("Add New User");

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
        addBtn.setText("Save");
        addBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 147, 176), 2));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        userNameField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        userNameField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Name *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N

        passwordField.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        passwordField.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Password *", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nunito SemiBold", 1, 14))); // NOI18N
        passwordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordFieldActionPerformed(evt);
            }
        });

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

        confirmPasswordEyeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmPasswordEyeButtonActionPerformed(evt);
            }
        });

        passwordEyeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordEyeButtonActionPerformed(evt);
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(userNameField)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(passwordField)
                                .addComponent(confirmPasswordField))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(confirmPasswordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(passwordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(userRoleCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(22, 22, 22))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(confirmPasswordField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(confirmPasswordEyeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(userRoleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFormBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
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

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        if (validateInputs()) {
            saveUser();
        }
    }//GEN-LAST:event_addBtnActionPerformed

    private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordFieldActionPerformed

    private void confirmPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmPasswordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_confirmPasswordFieldActionPerformed

    private void passwordEyeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordEyeButtonActionPerformed

    }//GEN-LAST:event_passwordEyeButtonActionPerformed

    private void confirmPasswordEyeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmPasswordEyeButtonActionPerformed

    }//GEN-LAST:event_confirmPasswordEyeButtonActionPerformed

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
            java.util.logging.Logger.getLogger(AddNewUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddNewUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddNewUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddNewUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddNewUser dialog = new AddNewUser(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton addBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton clearFormBtn;
    private javax.swing.JButton confirmPasswordEyeButton;
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton passwordEyeButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField userNameField;
    private javax.swing.JComboBox<String> userRoleCombo;
    // End of variables declaration//GEN-END:variables
}
