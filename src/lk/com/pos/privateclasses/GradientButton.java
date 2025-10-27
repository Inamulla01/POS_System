package lk.com.pos.privateclasses;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Gradient rounded button suitable for NetBeans drag & drop.
 * - Public no-arg constructor required by GUI builders.
 * - Use setters to customize colors, radius and preferred size.
 */
public class GradientButton extends JButton {
    private Color startColor = new Color(0, 161, 143);       // left/top
    private Color endColor = new Color(0, 150, 140);         // right/bottom
    private Color hoverStartColor = new Color(0, 185, 165);
    private Color hoverEndColor = new Color(0, 160, 150);
    private Color disabledColor = new Color(180, 180, 180, 160);
    private int cornerRadius = 12;
    private boolean hovered = false;

    // default size shown in NetBeans palette
    private Dimension pref = new Dimension(220, 44);

    // No-arg constructor (important for NetBeans Beans / Palette)
    public GradientButton() {
        this("Button");
    }

    public GradientButton(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Mouse hover/pressed effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // ensure pressed repaint
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return pref;
    }

    public void setPreferredButtonSize(Dimension d) {
        this.pref = d;
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw a subtle drop shadow
        int shadowOffset = 2;
        int shadowAlpha = 50; // 0-255
        g2.setColor(new Color(0, 0, 0, shadowAlpha));
        g2.fillRoundRect(shadowOffset, shadowOffset, w - shadowOffset * 2, h - shadowOffset * 2, cornerRadius, cornerRadius);

        // pick colors based on state
        boolean pressed = getModel().isArmed() && getModel().isPressed();
        boolean enabled = isEnabled();

        Color s = startColor;
        Color e = endColor;

        if (!enabled) {
            s = disabledColor;
            e = disabledColor;
        } else if (pressed) {
            // darken for pressed
            s = s.darker();
            e = e.darker();
        } else if (hovered) {
            s = hoverStartColor;
            e = hoverEndColor;
        }

        // paint gradient background
        GradientPaint gp = new GradientPaint(0, 0, s, w, h, e);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w - shadowOffset, h - shadowOffset, cornerRadius, cornerRadius);

        // optional inner highlight (very subtle)
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
        g2.setPaint(Color.WHITE);
        g2.fillRoundRect(0, 0, w - shadowOffset, (h - shadowOffset) / 2, cornerRadius, cornerRadius);

        // draw text
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        if (text != null) {
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            int tx = (w - textWidth) / 2 - 1;
            int ty = (h + textHeight) / 2 - 3;
            g2.drawString(text, tx, ty);
        }

        g2.dispose();
    }

    // === Setters to customize appearance at runtime ===
    public void setStartColor(Color startColor) {
        this.startColor = startColor;
        repaint();
    }

    public void setEndColor(Color endColor) {
        this.endColor = endColor;
        repaint();
    }

    public void setHoverStartColor(Color hoverStartColor) {
        this.hoverStartColor = hoverStartColor;
        repaint();
    }

    public void setHoverEndColor(Color hoverEndColor) {
        this.hoverEndColor = hoverEndColor;
        repaint();
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    public void setDisabledColor(Color disabledColor) {
        this.disabledColor = disabledColor;
        repaint();
    }
}
