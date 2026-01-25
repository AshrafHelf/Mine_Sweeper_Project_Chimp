package view;

import javax.swing.*;
import java.awt.*;

public final class Toast {

    private Toast() {}

    public static void show(Component anyChild, String message) {
        Window w = SwingUtilities.getWindowAncestor(anyChild);
        if (w != null) show(w, message, 1800);
    }

    public static void show(Window owner, String message, int ms) {
        if (owner == null) return;

        JWindow popup = new JWindow(owner);
        popup.setBackground(new Color(0,0,0,0));

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int ww = getWidth();
                int hh = getHeight();
                int arc = 22;

                g2.setColor(new Color(0,0,0,120));
                g2.fillRoundRect(6, 7, ww-12, hh-12, arc, arc);

                g2.setColor(new Color(15,15,15,210));
                g2.fillRoundRect(0, 0, ww-12, hh-12, arc, arc);

                g2.setColor(new Color(255,255,255,65));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, ww-14, hh-14, arc, arc);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lbl = new JLabel(message);
        lbl.setForeground(new Color(245, 242, 230));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        p.add(lbl, BorderLayout.CENTER);

        popup.setContentPane(p);
        popup.pack();

        int x = owner.getX() + (owner.getWidth() - popup.getWidth()) / 2;
        int y = owner.getY() + owner.getHeight() - popup.getHeight() - 40;
        popup.setLocation(x, y);

        popup.setAlwaysOnTop(true);
        popup.setVisible(true);

        new Timer(ms, e -> {
            popup.setVisible(false);
            popup.dispose();
        }) {{ setRepeats(false); }}.start();
    }
}
