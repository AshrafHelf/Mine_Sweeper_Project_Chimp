package view;

import javax.swing.*;
import java.awt.*;

public class WoodButton extends JButton {
    private static final long serialVersionUID = 1L;

    public WoodButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(new Color(40, 26, 10));
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        setMargin(new Insets(10, 16, 10, 16));
        setRolloverEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            boolean hover = getModel().isRollover();
            boolean pressed = getModel().isPressed();

            Color top = hover ? new Color(235, 198, 125) : new Color(224, 184, 110);
            Color mid = hover ? new Color(204, 152, 78) : new Color(192, 140, 68);
            Color bot = hover ? new Color(170, 110, 50) : new Color(160, 100, 45);

            if (pressed) {
                top = top.darker();
                mid = mid.darker();
                bot = bot.darker();
            }

            // Shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(4, 4, w - 6, h - 6, 22, 22);

            // Body gradient
            GradientPaint gp1 = new GradientPaint(0, 0, top, 0, h * 0.55f, mid);
            g2.setPaint(gp1);
            g2.fillRoundRect(0, 0, w - 6, h - 6, 22, 22);

            GradientPaint gp2 = new GradientPaint(0, h * 0.55f, mid, 0, h, bot);
            g2.setPaint(gp2);
            g2.fillRoundRect(0, 0, w - 6, h - 6, 22, 22);

            // Outline
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(90, 55, 25));
            g2.drawRoundRect(0, 0, w - 6, h - 6, 22, 22);

            // Simple "grain"
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(90, 55, 25, 70));
            for (int y = 12; y < h - 18; y += 10) {
                g2.drawLine(18, y, w - 30, y);
            }

            super.paintComponent(g);
        } finally {
            g2.dispose();
        }
    }
}
