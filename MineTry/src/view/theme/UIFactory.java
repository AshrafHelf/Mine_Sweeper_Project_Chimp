package view.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class UIFactory {

    private UIFactory() {}

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT);
        l.setFont(Theme.TITLE);
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_MUTED);
        l.setFont(Theme.SUBTITLE);
        return l;
    }

    public static JButton woodButton(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.BTN);
        b.setForeground(Theme.TEXT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Paint custom button (wood + banana hover)
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ButtonModel m = ((AbstractButton) c).getModel();
                int w = c.getWidth(), h = c.getHeight();

                Color base = Theme.WOOD;
                Color top = new Color(Theme.WOOD.getRed()+15, Theme.WOOD.getGreen()+10, Theme.WOOD.getBlue()+5);

                if (m.isRollover()) { base = Theme.BANANA; top = new Color(255, 236, 150); }
                if (m.isPressed()) { base = base.darker(); top = top.darker(); }

                // Shadow
                g2.setColor(new Color(0,0,0,120));
                g2.fillRoundRect(3, 4, w-6, h-6, Theme.ARC, Theme.ARC);

                // Body gradient
                g2.setPaint(new GradientPaint(0, 0, top, 0, h, base));
                g2.fillRoundRect(0, 0, w-6, h-6, Theme.ARC, Theme.ARC);

                // Border
                g2.setColor(new Color(255,255,255,45));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w-8, h-8, Theme.ARC, Theme.ARC);

                g2.dispose();
                super.paint(g, c);
            }
        });

        // Text color shift on hover
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setForeground(maybeDarkText(b.getModel().isRollover()));
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setForeground(Theme.TEXT);
            }
            private Color maybeDarkText(boolean hover) {
                return hover ? new Color(35, 35, 35) : Theme.TEXT;
            }
        });

        return b;
    }

    public static JPanel woodCard() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new GridBagLayout());
        p.setBorder(new EmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD));
        return new RoundedPanel(p, Theme.WOOD_DARK, new Color(0,0,0,110));
    }

    // Simple rounded container wrapper
    private static class RoundedPanel extends JPanel {
        private final JComponent inner;
        private final Color fill;
        private final Color shadow;

        RoundedPanel(JComponent inner, Color fill, Color shadow) {
            super(new BorderLayout());
            this.inner = inner;
            this.fill = fill;
            this.shadow = shadow;
            setOpaque(false);
            add(inner, BorderLayout.CENTER);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(shadow);
            g2.fillRoundRect(6, 7, w-12, h-12, Theme.ARC, Theme.ARC);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w-12, h-12, Theme.ARC, Theme.ARC);

            g2.setColor(new Color(255,255,255,40));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w-14, h-14, Theme.ARC, Theme.ARC);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
