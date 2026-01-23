package view;

import javax.swing.*;
import java.awt.*;

public class ShadowLabel extends JComponent {
    private String text;
    private Font font;
    private Color textColor = new Color(245, 242, 230);
    private Color shadowColor = new Color(0, 0, 0, 180);
    private int shadowOffset = 4;

    public ShadowLabel(String text, Font font) {
        this.text = text;
        this.font = font;
        setOpaque(false);
    }

    public void setText(String t) { this.text = t; repaint(); }
    public void setTextColor(Color c) { this.textColor = c; repaint(); }
    public void setShadowOffset(int px) { this.shadowOffset = px; repaint(); }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(font);
        return new Dimension(fm.stringWidth(text) + shadowOffset + 10, fm.getHeight() + shadowOffset + 10);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int x = 5;
        int y = 5 + fm.getAscent();

        // shadow
        g2.setColor(shadowColor);
        g2.drawString(text, x + shadowOffset, y + shadowOffset);

        // main text
        g2.setColor(textColor);
        g2.drawString(text, x, y);

        g2.dispose();
    }
}
