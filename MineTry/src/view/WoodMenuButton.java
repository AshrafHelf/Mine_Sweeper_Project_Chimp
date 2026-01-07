package view;

import javax.swing.*;
import java.awt.*;

public class WoodMenuButton extends JButton {
    private static final long serialVersionUID = 1L;

    private boolean selected;

    public WoodMenuButton(String text) {
        super(text);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false); // مهم جدا
        setOpaque(false);            // مهم جدا
        setRolloverEnabled(true);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.LEFT);

        setForeground(new Color(40, 26, 10));
        setFont(getFont().deriveFont(Font.BOLD, 18f));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 14));

        setPreferredSize(new Dimension(260, 46));
    }

    public void setSelectedState(boolean v) {
        this.selected = v;
        repaint();
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

            // Wood colors (no transparency -> no ghosting)
            Color top = new Color(224, 184, 110);
            Color mid = new Color(192, 140, 68);
            Color bot = new Color(160, 100, 45);

            if (hover) {
                top = top.brighter();
            }
            if (pressed) {
                top = top.darker();
                mid = mid.darker();
                bot = bot.darker();
            }

            // Shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(6, 6, w - 8, h - 8, 18, 18);

            // Body gradient
            GradientPaint gp1 = new GradientPaint(0, 0, top, 0, h * 0.55f, mid);
            g2.setPaint(gp1);
            g2.fillRoundRect(0, 0, w - 8, h - 8, 18, 18);

            GradientPaint gp2 = new GradientPaint(0, h * 0.55f, mid, 0, h, bot);
            g2.setPaint(gp2);
            g2.fillRoundRect(0, 0, w - 8, h - 8, 18, 18);

            // Outline
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(90, 55, 25));
            g2.drawRoundRect(0, 0, w - 8, h - 8, 18, 18);

            // Selected highlight bar (like real game menu)
            if (selected) {
                g2.setColor(new Color(255, 240, 170));
                g2.fillRoundRect(0, 0, 10, h - 8, 10, 10);

                // slight glow overlay
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(10, 0, w - 18, h - 8, 18, 18);
            }

            // Text (manual draw for clean look)
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();

            int textX = 22;
            int textY = (h - 8 - fm.getHeight()) / 2 + fm.getAscent();

            // text color
            g2.setColor(selected ? new Color(30, 18, 6) : new Color(40, 26, 10));
            g2.drawString(getText(), textX, textY);

        } finally {
            g2.dispose();
        }
    }
}
