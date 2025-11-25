package lk.com.pos.privateclasses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Enhanced JOptionPane with keyboard shortcuts:
 * - Y for Yes
 * - N for No
 * - Enter for default button (Yes)
 * - Escape to close
 */
public class EnhancedConfirmDialog {
    
    /**
     * Shows a confirmation dialog with keyboard shortcuts
     * @param parent Parent component
     * @param message Dialog message
     * @param title Dialog title
     * @param optionType JOptionPane option type (YES_NO_OPTION, etc.)
     * @return User's choice (YES_OPTION, NO_OPTION, CLOSED_OPTION)
     */
    public static int showConfirmDialog(Component parent, String message, String title, int optionType) {
        // Create the option pane
        final JOptionPane optionPane = new JOptionPane(
            message,
            JOptionPane.QUESTION_MESSAGE,
            optionType
        );
        
        // Create dialog
        final JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Add property change listener to close dialog when value is set
        optionPane.addPropertyChangeListener(evt -> {
            if (dialog.isVisible() && 
                JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName()) &&
                evt.getNewValue() != null &&
                evt.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                dialog.dispose();
            }
        });
        
        // Setup keyboard shortcuts BEFORE showing dialog
        setupKeyboardShortcuts(dialog, optionPane, optionType);
        
        // Handle window closing
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }
            
            @Override
            public void windowOpened(WindowEvent e) {
                // Request focus when window opens
                SwingUtilities.invokeLater(() -> {
                    dialog.requestFocusInWindow();
                });
            }
        });
        
        // Show dialog
        dialog.setVisible(true);
        
        // Get result
        Object value = optionPane.getValue();
        if (value == null || value.equals(JOptionPane.UNINITIALIZED_VALUE)) {
            return JOptionPane.CLOSED_OPTION;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return JOptionPane.CLOSED_OPTION;
    }
    
    private static void setupKeyboardShortcuts(JDialog dialog, JOptionPane optionPane, int optionType) {
        // Create key listener that will handle all keyboard shortcuts
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!dialog.isVisible()) return;
                
                int keyCode = e.getKeyCode();
                
                switch (keyCode) {
                    case KeyEvent.VK_Y:
                        if (optionType == JOptionPane.YES_NO_OPTION || 
                            optionType == JOptionPane.YES_NO_CANCEL_OPTION) {
                            optionPane.setValue(JOptionPane.YES_OPTION);
                            e.consume();
                        }
                        break;
                        
                    case KeyEvent.VK_N:
                        if (optionType == JOptionPane.YES_NO_OPTION || 
                            optionType == JOptionPane.YES_NO_CANCEL_OPTION) {
                            optionPane.setValue(JOptionPane.NO_OPTION);
                            e.consume();
                        }
                        break;
                        
                    case KeyEvent.VK_ESCAPE:
                        optionPane.setValue(JOptionPane.CLOSED_OPTION);
                        e.consume();
                        break;
                        
                    case KeyEvent.VK_ENTER:
                        // Press the default button (typically Yes)
                        if (optionType == JOptionPane.YES_NO_OPTION) {
                            optionPane.setValue(JOptionPane.YES_OPTION);
                        } else if (optionType == JOptionPane.OK_CANCEL_OPTION) {
                            optionPane.setValue(JOptionPane.OK_OPTION);
                        }
                        e.consume();
                        break;
                }
            }
        };
        
        // Add key listener to dialog
        dialog.addKeyListener(keyAdapter);
        
        // Add key listener to all components recursively
        addKeyListenerRecursively(dialog.getContentPane(), keyAdapter);
        
        // Make dialog and root pane focusable
        dialog.setFocusable(true);
        dialog.getRootPane().setFocusable(true);
        
        // Setup button mnemonics and tooltips
        addShortcutsToButtons(dialog.getContentPane(), optionType);
        
        // Add component listener to request focus when shown
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.requestFocusInWindow();
                    dialog.getRootPane().requestFocusInWindow();
                });
            }
        });
    }
    
    /**
     * Recursively add key listener to all components
     */
    private static void addKeyListenerRecursively(Component comp, KeyListener listener) {
        comp.addKeyListener(listener);
        
        if (comp instanceof Container) {
            Container container = (Container) comp;
            for (Component child : container.getComponents()) {
                addKeyListenerRecursively(child, listener);
            }
        }
    }
    
    /**
     * Add mnemonics and tooltips to buttons
     */
    private static void addShortcutsToButtons(Container container, int optionType) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String text = button.getText();
                
                // Add mnemonics and tooltips based on button text
                if (text != null) {
                    text = text.toLowerCase();
                    
                    if (text.contains("yes")) {
                        button.setMnemonic(KeyEvent.VK_Y);
                        button.setDisplayedMnemonicIndex(0);
                        button.setToolTipText("<html><b>Y</b> - Yes | <b>Enter</b> - Confirm</html>");
                    } else if (text.contains("no")) {
                        button.setMnemonic(KeyEvent.VK_N);
                        button.setDisplayedMnemonicIndex(0);
                        button.setToolTipText("<html><b>N</b> - No | <b>Escape</b> - Cancel</html>");
                    } else if (text.contains("ok")) {
                        button.setMnemonic(KeyEvent.VK_O);
                        button.setToolTipText("<html><b>O</b> - OK | <b>Enter</b> - Confirm</html>");
                    } else if (text.contains("cancel")) {
                        button.setMnemonic(KeyEvent.VK_C);
                        button.setToolTipText("<html><b>C</b> - Cancel | <b>Escape</b> - Close</html>");
                    }
                }
                
                // Make buttons focusable
                button.setFocusable(true);
                
            } else if (comp instanceof Container) {
                addShortcutsToButtons((Container) comp, optionType);
            }
        }
    }
    
    /**
     * Alternative method with message type parameter
     */
    public static int showConfirmDialog(Component parent, String message, String title, 
                                       int optionType, int messageType) {
        // Create the option pane with message type
        final JOptionPane optionPane = new JOptionPane(
            message,
            messageType,
            optionType
        );
        
        // Create dialog
        final JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Add property change listener
        optionPane.addPropertyChangeListener(evt -> {
            if (dialog.isVisible() && 
                JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName()) &&
                evt.getNewValue() != null &&
                evt.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                dialog.dispose();
            }
        });
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts(dialog, optionPane, optionType);
        
        // Handle window closing
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }
            
            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.requestFocusInWindow();
                });
            }
        });
        
        // Show dialog
        dialog.setVisible(true);
        
        // Get result
        Object value = optionPane.getValue();
        if (value == null || value.equals(JOptionPane.UNINITIALIZED_VALUE)) {
            return JOptionPane.CLOSED_OPTION;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return JOptionPane.CLOSED_OPTION;
    }
    
    // Demo/Test method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Enhanced Confirm Dialog");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JButton testButton1 = new JButton("Test YES/NO Dialog");
            testButton1.addActionListener(e -> {
                int result = EnhancedConfirmDialog.showConfirmDialog(
                    frame,
                    "Do you want to proceed?\n\n" +
                    "Keyboard shortcuts:\n" +
                    "Y - Yes\n" +
                    "N - No\n" +
                    "Enter - Default (Yes)\n" +
                    "Escape - Close",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
                );
                
                showResult(frame, result);
            });
            
            JButton testButton2 = new JButton("Test WARNING Dialog");
            testButton2.addActionListener(e -> {
                int result = EnhancedConfirmDialog.showConfirmDialog(
                    frame,
                    "Are you sure you want to delete this item?\n\nThis action cannot be undone!",
                    "Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                showResult(frame, result);
            });
            
            JButton testButton3 = new JButton("Test ERROR Dialog");
            testButton3.addActionListener(e -> {
                int result = EnhancedConfirmDialog.showConfirmDialog(
                    frame,
                    "An error occurred!\n\nDo you want to retry?",
                    "Error",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE
                );
                
                showResult(frame, result);
            });
            
            JLabel instructions = new JLabel(
                "<html><b>Instructions:</b><br>" +
                "Click any button to test the dialog.<br>" +
                "Try using keyboard shortcuts:<br>" +
                "• Press <b>Y</b> for Yes<br>" +
                "• Press <b>N</b> for No<br>" +
                "• Press <b>Enter</b> for default (Yes)<br>" +
                "• Press <b>Escape</b> to close</html>"
            );
            
            panel.add(instructions);
            panel.add(testButton1);
            panel.add(testButton2);
            panel.add(testButton3);
            
            frame.add(panel);
            frame.setVisible(true);
        });
    }
    
    private static void showResult(JFrame frame, int result) {
        String resultText;
        int messageType;
        
        switch (result) {
            case JOptionPane.YES_OPTION:
                resultText = "✓ You selected: YES";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            case JOptionPane.NO_OPTION:
                resultText = "✗ You selected: NO";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            default:
                resultText = "Dialog was closed without selection";
                messageType = JOptionPane.WARNING_MESSAGE;
                break;
        }
        
        JOptionPane.showMessageDialog(frame, resultText, "Result", messageType);
    }
}