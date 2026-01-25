package view.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class UIFactory {

    private UIFactory() {}

    public static JLabel title(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(Theme.TEXT);
        l.setFont(Theme.TITLE);
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(Theme.TEXT_MUTED);
        l.setFont(Theme.SUBTITLE);
        return l;
    }

    public static JButton woodButton(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.BTN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JPanel woodCard() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new GridBagLayout());
        p.setBorder(new EmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD));
        return new RoundedPanel(p, Theme.WOOD_DARK, new Color(0,0,0,110));
    }
    
    // =========================
    // Simple settings widgets
    // =========================

    public static JCheckBox styledCheck(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setOpaque(false);
        cb.setForeground(Theme.TEXT);
        cb.setFont(new Font("SansSerif", Font.BOLD, 13));
        cb.setFocusPainted(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return cb;
    }

    public static JSlider styledSlider(int value) {
        JSlider s = new JSlider(0, 100, value);
        s.setOpaque(false);

        // DON'T kill focus completely — it can make it feel "dead" on some systems
        s.setFocusable(true);

        // readable on glass
        s.setForeground(Theme.TEXT);
        s.setBackground(new Color(0, 0, 0, 0));

        // nicer ticks (optional)
        s.setPaintTicks(true);
        s.setMajorTickSpacing(25);
        s.setMinorTickSpacing(5);

        return s;
    }


    // =========================
    // Glass system (dialogs + screens)
    // =========================

    public static JLabel dialogTitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT);
        l.setFont(Theme.DIALOG_TITLE);
        return l;
    }

    public static JLabel dialogSubtitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_MUTED);
        l.setFont(Theme.DIALOG_SUB);
        return l;
    }

    /** Standard glass card used across Splash/Home/Select/Difficulty/Dialogs. */
    public static JPanel glassCard() {
        return glassPanel(Theme.GLASS_FILL, true);
    }
    /** Small rounded glass pill for tips/badges (HelpDialog etc.). */
    public static JPanel glassPill() {
        JPanel p = new GlassPillPanel();
        p.setOpaque(false);
        return p;
    }


    /** Parameterized glass panel (GamePanel uses different fills per row). */
    public static JPanel glassPanel(Color fill, boolean thickBorder) {
        JPanel p = new GlassPanel(fill, thickBorder);
        p.setOpaque(false);
        return p;
    }

    /** Dark glass button used across most screens. */
    public static JButton glassButton(String text) {
        return new GlassButton(text, false);
    }

    /** Primary jungle-green button (Splash/Home “Enter / Continue”). */
    public static JButton primaryButton(String text) {
        return new GlassButton(text, true);
    }

    /** Small X button for undecorated dialogs/cards. */
    public static JButton miniIconButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(Theme.TEXT);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return b;
    }

    // =========================
    // Form styling helpers (used by ChimpSelect / Question forms / Settings)
    // =========================

    public static void styleTextField(JTextField f) {
        f.setBackground(new Color(0, 0, 0, 120));
        f.setForeground(new Color(235, 235, 235, 220));
        f.setCaretColor(new Color(235, 235, 235, 220));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 45), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleTextArea(JTextArea a) {
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBackground(new Color(0, 0, 0, 0));
        a.setForeground(new Color(235, 235, 235, 220));
        a.setCaretColor(new Color(235, 235, 235, 220));
        a.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 45), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setBackground(new Color(0, 0, 0, 120));
        c.setForeground(new Color(235, 235, 235, 220));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 45), 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
    }

    // =========================
    // Painters (FIXED)
    // =========================

    /** Simple rounded container wrapper */
    private static class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;

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

            int w = getWidth();
            int h = getHeight();
            int arc = Theme.ARC;

            int pad = Theme.SHADOW_PAD; // 6

            int innerW = Math.max(1, w - pad * 2);
            int innerH = Math.max(1, h - pad * 2);

            // shadow behind
            g2.setColor(shadow);
            g2.fillRoundRect(pad, pad + 1, innerW, innerH, arc, arc);

            // fill
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, innerW, innerH, arc, arc);

            // border
            g2.setColor(new Color(255, 255, 255, 40));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, innerW - 2, innerH - 2, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    private static class GlassPillPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        GlassPillPanel() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int arc = 16;

            // subtle fill
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // border
            g2.setColor(new Color(255, 255, 255, 45));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();
        }
    }


    /** Glass card/panel painter */
    private static class GlassPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final Color fill;
        private final boolean thick;

        GlassPanel(Color fill, boolean thick) {
            super(new BorderLayout());
            this.fill = fill;
            this.thick = thick;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int arc = Theme.GLASS_ARC;
            int pad = Theme.SHADOW_PAD;

            int innerW = Math.max(1, w - pad * 2);
            int innerH = Math.max(1, h - pad * 2);

            // shadow
            g2.setColor(Theme.GLASS_SHADOW);
            g2.fillRoundRect(pad, pad + 1, innerW, innerH, arc, arc);

            // fill
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, innerW, innerH, arc, arc);

            // border
            g2.setColor(new Color(255, 255, 255, thick ? 65 : 45));
            g2.setStroke(new BasicStroke(thick ? 2.2f : 1.8f));
            g2.drawRoundRect(1, 1, innerW - 2, innerH - 2, arc, arc);

            g2.dispose();
        }
    }

    /** Glass button painter */
    private static class GlassButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final boolean primary;

        GlassButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Theme.TEXT);
            setFont(primary ? Theme.BTN : Theme.DIALOG_BTN);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setRolloverEnabled(true);
        }

        @Override public Dimension getMaximumSize() {
            Dimension ps = getPreferredSize();
            return new Dimension(340, ps.height);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 16;

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();

            int shadowOffset = 4;
            int pressOffset  = press ? 2 : 0;

            int innerW = Math.max(1, w - shadowOffset);
            int innerH = Math.max(1, h - shadowOffset);

            // shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(shadowOffset, shadowOffset + pressOffset, innerW, innerH, arc, arc);

            Color base;
            if (primary) {
                base = hover ? new Color(30, 80, 45, 190) : new Color(20, 60, 35, 170);
                if (press) base = new Color(15, 45, 25, 210);
            } else {
                base = hover ? new Color(0, 0, 0, 185) : new Color(0, 0, 0, 150);
                if (press) base = new Color(0, 0, 0, 220);
            }

            // fill
            g2.setColor(base);
            g2.fillRoundRect(0, pressOffset, innerW, innerH, arc, arc);

            // border
            g2.setColor(new Color(255, 255, 255, hover ? 85 : 55));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, pressOffset + 1, innerW - 2, innerH - 2, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
