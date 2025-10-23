package classbutton1;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class RoundedComboBox<E> extends JComboBox<E> {

    private int radius = 10; // adjust corner radius

    public RoundedComboBox() {
        setOpaque(false);
        setBackground(Color.WHITE);
        setBorder(new RoundedBorder(radius));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        super.paintComponent(g);
        g2.dispose();
    }

    private static class RoundedBorder implements Border {
        private int radius;
        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(180, 180, 180)); // border color
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}
