package lk.com.pos.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import lk.com.pos.connection.MySQL;
import lk.com.pos.util.AppIconUtil;
import raven.toast.Notifications;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import lk.com.pos.session.Session;
import java.awt.event.KeyEvent;

public class LogIn extends javax.swing.JFrame {

    // ---------------- PASSWORD TOGGLE ----------------
    private boolean passwordVisible = false;
    private Icon eyeOpenIcon;
    private Icon eyeClosedIcon;

    public LogIn() {
        initComponents();
        init();
    }

    public void init() {
        AppIconUtil.applyIcon(this);

        // Load SVG eye icons
        try {
            // Load SVG icons for eye open and closed
            eyeOpenIcon = new FlatSVGIcon("lk/com/pos/icon/eye-open.svg", 25, 25);
            eyeClosedIcon = new FlatSVGIcon("lk/com/pos/icon/eye-closed.svg", 25, 25);

            // Set initial icon
            passwordEyeButton.setIcon(eyeClosedIcon);

            // Remove button borders and background
            passwordEyeButton.setBorderPainted(false);
            passwordEyeButton.setContentAreaFilled(false);
            passwordEyeButton.setFocusPainted(false);
            passwordEyeButton.setText("");

        } catch (Exception e) {
            System.out.println("SVG eye icons not found: " + e.getMessage());
            // Fallback to text if icons are not available
            passwordEyeButton.setText("👁");
        }

        try {
            FlatSVGIcon loginImage = new FlatSVGIcon("lk/com/pos/img/login.svg", 300, 300);
            logInImg.setIcon(loginImage);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Image load error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        try {
            FlatSVGIcon userImage = new FlatSVGIcon("lk/com/pos/icon/user.svg", 90, 90);
            userIcon.setIcon(userImage);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Image load error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Gradient button styling (NetBeans-safe)
        loginBtn.setContentAreaFilled(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Nunito SemiBold", Font.BOLD, 14));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Override paint behavior using BasicButtonUI
        loginBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                int w = c.getWidth();
                int h = c.getHeight();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient colors
                Color topColor = new Color(0x12, 0xB5, 0xA6); // Light
                Color bottomColor = new Color(0x08, 0x93, 0xB0); // Dark

                // Draw gradient
                GradientPaint gp = new GradientPaint(0, 0, topColor, w, 0, bottomColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 5, 5);

                // Draw button text
                super.paint(g, c);
            }
        });

        // Setup keyboard navigation
        setupFocusTraversal();
        setupButtonStyles();

        // Set initial focus
        userName.requestFocus();
    }

    private void setupFocusTraversal() {
        // Create focus traversal order
        java.util.List<java.awt.Component> order = java.util.Arrays.asList(
                userName,
                password,
                loginBtn,
                passwordEyeButton
        );

        // Set focus traversal keys
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                java.util.Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)));

        // Add comprehensive key navigation
        setupArrowKeyNavigation();
        addEnterKeyNavigation();

        // Add F2 shortcut for password toggle
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                togglePasswordVisibility();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Add ESC key to close
        getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Setup tooltips
        userName.setToolTipText("Type username and press ENTER to move to password field");
        password.setToolTipText("<html>Type password and press ENTER to login<br>Press <b>F2</b> to toggle password visibility</html>");
        passwordEyeButton.setToolTipText("<html>Press ENTER or SPACE to toggle password visibility<br>Press <b>F2</b> to toggle from anywhere</html>");
        loginBtn.setToolTipText("Press ENTER to login");
    }

    private void setupArrowKeyNavigation() {
        // Add arrow key navigation to all components
        userName.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, userName);
            }
        });

        password.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, password);
            }
        });

        passwordEyeButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, passwordEyeButton);
            }
        });

        loginBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                handleArrowNavigation(evt, loginBtn);
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
        }
    }

    private void handleRightArrow(java.awt.Component source) {
        if (source == userName) {
            password.requestFocusInWindow();
        } else if (source == password) {
            loginBtn.requestFocusInWindow(); // Skip eye button, go directly to login
        } else if (source == passwordEyeButton) {
            userName.requestFocusInWindow();
        } else if (source == loginBtn) {
            passwordEyeButton.requestFocusInWindow();
        }
    }

    private void handleLeftArrow(java.awt.Component source) {
        if (source == userName) {
            passwordEyeButton.requestFocusInWindow();
        } else if (source == password) {
            userName.requestFocusInWindow();
        } else if (source == passwordEyeButton) {
            loginBtn.requestFocusInWindow();
        } else if (source == loginBtn) {
            password.requestFocusInWindow(); // Skip eye button
        }
    }

    private void handleDownArrow(java.awt.Component source) {
        if (source == userName) {
            password.requestFocusInWindow();
        } else if (source == password) {
            loginBtn.requestFocusInWindow(); // Skip eye button, go directly to login
        } else if (source == passwordEyeButton) {
            userName.requestFocusInWindow();
        } else if (source == loginBtn) {
            passwordEyeButton.requestFocusInWindow();
        }
    }

    private void handleUpArrow(java.awt.Component source) {
        if (source == userName) {
            passwordEyeButton.requestFocusInWindow();
        } else if (source == password) {
            userName.requestFocusInWindow();
        } else if (source == passwordEyeButton) {
            loginBtn.requestFocusInWindow();
        } else if (source == loginBtn) {
            password.requestFocusInWindow(); // Skip eye button
        }
    }

    private void addEnterKeyNavigation() {
        // Map components to their next focus targets for Enter key
        java.util.Map<java.awt.Component, java.awt.Component> enterNavigationMap = new java.util.HashMap<>();
        enterNavigationMap.put(userName, password);
        enterNavigationMap.put(password, loginBtn); // Password field goes directly to login button
        enterNavigationMap.put(passwordEyeButton, loginBtn);
        enterNavigationMap.put(loginBtn, userName);

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

        // Special handling for password eye button - also trigger toggle on Enter
        passwordEyeButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    togglePasswordVisibility();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    togglePasswordVisibility();
                    evt.consume();
                }
            }
        });

        // Special handling for login button - trigger login on Enter
        loginBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginBtnActionPerformed(null);
                    evt.consume();
                }
            }
        });

        // Special handling for password field - trigger login on Enter when in password field
        password.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Instead of moving focus, directly trigger login
                    loginBtnActionPerformed(null);
                    evt.consume();
                }
            }
        });
    }

    private void setupButtonStyles() {
        // Setup mouse listeners for eye button with SVG icon color change on hover/focus
        passwordEyeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                passwordEyeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                // Change SVG icon color on hover
                applyEyeButtonHoverEffect(true);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                applyEyeButtonHoverEffect(false);
            }
        });

        // Setup focus listeners for eye button
        passwordEyeButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                applyEyeButtonHoverEffect(true);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                applyEyeButtonHoverEffect(false);
            }
        });
    }

    private void applyEyeButtonHoverEffect(boolean active) {
        try {
            if (active) {
                // Create blue tinted icons for hover/focus state
                FlatSVGIcon eyeOpenHover = new FlatSVGIcon("lk/com/pos/icon/eye-open.svg", 25, 25);
                eyeOpenHover.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#0893B0")));

                FlatSVGIcon eyeClosedHover = new FlatSVGIcon("lk/com/pos/icon/eye-closed.svg", 25, 25);
                eyeClosedHover.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#0893B0")));

                if (passwordVisible) {
                    passwordEyeButton.setIcon(eyeOpenHover);
                } else {
                    passwordEyeButton.setIcon(eyeClosedHover);
                }
            } else {
                // Reset to original icons
                if (passwordVisible) {
                    passwordEyeButton.setIcon(eyeOpenIcon);
                } else {
                    passwordEyeButton.setIcon(eyeClosedIcon);
                }
            }
        } catch (Exception e) {
            System.out.println("Error applying hover effect: " + e.getMessage());
        }
    }

    // ---------------- PASSWORD TOGGLE METHOD ----------------
    private void togglePasswordVisibility() {
        if (passwordVisible) {
            password.setEchoChar('•');
            passwordEyeButton.setIcon(eyeClosedIcon);
        } else {
            password.setEchoChar((char) 0);
            passwordEyeButton.setIcon(eyeOpenIcon);
        }
        passwordVisible = !passwordVisible;

        // Re-apply hover effect if needed
        if (passwordEyeButton.hasFocus()) {
            applyEyeButtonHoverEffect(true);
        }
    }

    // ---------------- PASSWORD HASHING METHOD ----------------
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
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
        } catch (java.security.NoSuchAlgorithmException e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT,
                    "Password encryption error: " + e.getMessage());
            return password; // fallback
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        logInImg = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        userName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        loginBtn = new javax.swing.JButton();
        userIcon = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        passwordEyeButton = new javax.swing.JButton();

        jLabel3.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jLabel3.setText("User Name");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LogIn Page");
        setFocusTraversalPolicyProvider(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jLabel1.setText("User Name *");

        userName.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        userName.setToolTipText("");
        userName.setPreferredSize(new java.awt.Dimension(64, 30));
        userName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userNameActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jLabel2.setText("Password *");

        password.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        password.setToolTipText("");
        password.setPreferredSize(new java.awt.Dimension(64, 30));
        password.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordActionPerformed(evt);
            }
        });

        loginBtn.setBackground(new java.awt.Color(115, 230, 203));
        loginBtn.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        loginBtn.setText("Log-in");
        loginBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        loginBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginBtnActionPerformed(evt);
            }
        });

        userIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel4.setFont(new java.awt.Font("Nunito SemiBold", 1, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Copyright © 2025 Avinam Global. All rights reserved.");

        passwordEyeButton.setText("buttom");
        passwordEyeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordEyeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(logInImg, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(userName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loginBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(28, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(49, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(logInImg, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addGap(19, 19, 19))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(userIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(passwordEyeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(loginBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(72, 72, 72))))
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
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordActionPerformed

    private void userNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userNameActionPerformed

    private void loginBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginBtnActionPerformed
        String username = userName.getText().trim();
        String passwordText = String.valueOf(this.password.getPassword()).trim();

        if (username.isEmpty() || passwordText.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Please fill all fields!");
            return;
        }

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        PreparedStatement pstCheckMessage = null;
        PreparedStatement pstInsertMessage = null;
        PreparedStatement pstNotification = null;

        try {
            // Hash the password before comparing
            String hashedPassword = hashPassword(passwordText);

            con = MySQL.getConnection();
            String sql = "SELECT u.user_id, u.name, r.role_name "
                    + "FROM user u "
                    + "INNER JOIN role r ON u.role_id = r.role_id "
                    + "WHERE u.name = ? AND u.password = ?";
            pst = con.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, hashedPassword); // Use hashed password
            rs = pst.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String roleName = rs.getString("role_name");
                String userName = rs.getString("name");

                // Store session data
                Session.getInstance().setSession(userId, roleName);

                // Create login success message with username and role
                String loginMessage = userName + "(" + roleName + ") logged in successfully";
                int massageId = 0;

                // Check if message already exists
                String checkMessageSql = "SELECT massage_id FROM massage WHERE massage = ?";
                pstCheckMessage = con.prepareStatement(checkMessageSql);
                pstCheckMessage.setString(1, loginMessage);
                ResultSet rsMessage = pstCheckMessage.executeQuery();

                if (rsMessage.next()) {
                    // Message exists, get the existing massage_id
                    massageId = rsMessage.getInt("massage_id");
                } else {
                    // Message doesn't exist, insert new message
                    String insertMessageSql = "INSERT INTO massage (massage) VALUES (?)";
                    pstInsertMessage = con.prepareStatement(insertMessageSql, PreparedStatement.RETURN_GENERATED_KEYS);
                    pstInsertMessage.setString(1, loginMessage);
                    pstInsertMessage.executeUpdate();

                    // Get the generated massage_id
                    ResultSet generatedKeys = pstInsertMessage.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        massageId = generatedKeys.getInt(1);
                    }
                    generatedKeys.close();
                }
                rsMessage.close();

                // Insert into notification table
                if (massageId > 0) {
                    String notificationSql = "INSERT INTO notifocation (is_read, create_at, msg_type_id, massage_id) VALUES (1, NOW(), 3, ?)";
                    pstNotification = con.prepareStatement(notificationSql);
                    pstNotification.setInt(1, massageId);
                    pstNotification.executeUpdate();
                }

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Login Successful");

                this.dispose();
                new HomeScreen().setVisible(true);

            } else {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Invalid Username or Password");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unexpected error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close all resources
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (pstCheckMessage != null) {
                    pstCheckMessage.close();
                }
                if (pstInsertMessage != null) {
                    pstInsertMessage.close();
                }
                if (pstNotification != null) {
                    pstNotification.close();
                }
                // DON'T close the connection here to keep it in pool
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }//GEN-LAST:event_loginBtnActionPerformed

    private void passwordEyeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordEyeButtonActionPerformed
        togglePasswordVisibility();
    }//GEN-LAST:event_passwordEyeButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LogIn().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel logInImg;
    private javax.swing.JButton loginBtn;
    private javax.swing.JPasswordField password;
    private javax.swing.JButton passwordEyeButton;
    private javax.swing.JLabel userIcon;
    private javax.swing.JTextField userName;
    // End of variables declaration//GEN-END:variables
}
