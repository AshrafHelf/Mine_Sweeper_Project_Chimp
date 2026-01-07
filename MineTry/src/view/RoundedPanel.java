package view;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final int arc;
    private final Color fill;
    private final Color border;
    private final boolean shadow;

    public RoundedPanel(int arc, Color fill, Color border, boolean shadow) {
        this.arc = arc;
        this.fill = fill;
        this.border = border;
        this.shadow = shadow;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(8, 8, w - 16, h - 16, arc, arc);
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 16, h - 16, arc, arc);

            g2.setStroke(new BasicStroke(2f));
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 16, h - 16, arc, arc);

            super.paintComponent(g);
        } finally {
            g2.dispose();
        }
    }
}
