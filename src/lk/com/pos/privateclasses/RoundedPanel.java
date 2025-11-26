package lk.com.pos.privateclasses;

import java.awt.*;
import javax.swing.*;

public class RoundedPanel extends JPanel {
    private int cornerRadius = 20;
    private Color backgroundColor = Color.WHITE;
    private Color borderColor = new Color(180, 180, 180);
    private int borderThickness = 0; // ✅ Changed to 0 (no border by default)
    private boolean drawBorder = false; // ✅ New flag to control border drawing

    public RoundedPanel() {
        setOpaque(false);
    }

    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // ✅ Background only
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // ✅ Border (only if enabled)
        if (drawBorder && borderThickness > 0) {
            int offset = borderThickness / 2;
            g2.setStroke(new BasicStroke(borderThickness));
            g2.setColor(borderColor);
            g2.drawRoundRect(offset, offset, width - borderThickness, height - borderThickness, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }

    // === Setter methods ===
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setBorderThickness(int thickness) {
        this.borderThickness = thickness;
        repaint();
    }

    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
        repaint();
    }

    public void removeBorder() {
        this.drawBorder = false;
        this.borderThickness = 0;
        repaint();
    }

    public void setRounded(boolean rounded) {
        if (!rounded) {
            this.cornerRadius = 0;
        }
        repaint();
    }
}