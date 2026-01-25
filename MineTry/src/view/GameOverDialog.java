package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;

public class GameOverDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    // --- Jungle palette ---
    private static final Color TXT       = new Color(245, 242, 230);
    private static final Color MUTED     = new Color(210, 220, 215, 160);
    private static final Color ACCENT    = new Color(255, 215, 90);        // banana

    // Card glass (green jungle)
    private static final Color CARD_FILL   = new Color(35, 68, 52, 235);
    private static final Color CARD_BORDER = new Color(140, 190, 155, 90);
    private static final Color SHADOW      = new Color(0, 0, 0, 120);

    // Safe fixed sizes so nothing gets clipped
    private static final Dimension SIZE_LOSE = new Dimension(620, 640);
    private static final Dimension SIZE_WIN  = new Dimension(620, 610);

    public GameOverDialog(JFrame owner, boolean won, int finalScore) {
        super(owner, "Game Over", true);

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ✅ Make the whole dialog window transparent -> removes the black square underneath
        setBackground(new Color(0, 0, 0, 0));

        // Root does NOT paint any background (so no dark rectangle)
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(root);

        // ---------- card ----------
        GlassCard card = new GlassCard();
        card.setLayout(new BorderLayout(14, 14));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(card, gbc);

        // ---------- header ----------
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel(won ? "🍌 TEAM VICTORY!" : "💥 GAME OVER");
        title.setForeground(TXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel sub = new JLabel(won ? "You cleared the board :)" : "All team lives were lost :(");
        sub.setForeground(MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(sub);

        JButton close = new MiniIconButton("✕");
        close.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });

        top.add(titles, BorderLayout.WEST);
        top.add(close, BorderLayout.EAST);

        // ---------- center ----------
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // ✅ GIF for BOTH win & lose
        String gifPath = won ? "/img/wingif.gif" : "/img/losegif.gif";
        JLabel gifLabel = new JLabel(loadGif(gifPath));
        gifLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gifLabel.setBorder(new EmptyBorder(6, 0, 6, 0));

        center.add(gifLabel);
        center.add(Box.createVerticalStrut(12));

        JPanel scoreStrip = scoreBox(finalScore);
        scoreStrip.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel tip = tipBox(won ? "Nice teamwork. GG WP" : "Try using flags earlier when unsure.");
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(scoreStrip);
        center.add(Box.createVerticalStrut(14));
        center.add(tip);

        // ---------- footer ----------
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JButton ok = new BananaButton("CONTINUE");
        ok.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(ok);

        footer.add(btnWrap, BorderLayout.EAST);

        // Assemble
        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        // ESC closes
        getRootPane().registerKeyboardAction(
                e -> { AudioManager.playSfx("button.wav"); dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // ✅ fixed size guarantees button + tip never clipped
        setSize(won ? SIZE_WIN : SIZE_LOSE);
        setLocationRelativeTo(owner);
    }

    // ---------- helpers ----------

    private static ImageIcon loadGif(String path) {
        URL url = GameOverDialog.class.getResource(path);
        if (url == null) return new ImageIcon(); // no crash on demo
        return new ImageIcon(url);
    }

    private static JPanel scoreBox(int finalScore) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int arc = 18;

                g2.setColor(new Color(0, 0, 0, 130));
                g2.fillRoundRect(4, 5, w - 8, h - 8, arc, arc);

                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 18, 12, 18));
        p.setPreferredSize(new Dimension(360, 98));
        p.setMaximumSize(new Dimension(360, 98));

        JLabel t = new JLabel("FINAL TEAM SCORE", SwingConstants.CENTER);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        t.setForeground(ACCENT);
        t.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel v = new JLabel(String.valueOf(finalScore), SwingConstants.CENTER);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        v.setForeground(TXT);
        v.setFont(new Font("SansSerif", Font.BOLD, 42));

        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(v);

        return p;
    }

    private static JPanel tipBox(String text) {
        JPanel tip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 16;
                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(40, 75, 58, 220));
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                g2.setColor(new Color(255, 215, 90, 70));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

                g2.dispose();
            }
        };
        tip.setOpaque(false);
        tip.setLayout(new BorderLayout());
        tip.setBorder(new EmptyBorder(10, 14, 10, 14));
        tip.setPreferredSize(new Dimension(500, 70));
        tip.setMaximumSize(new Dimension(500, 70));

        JLabel l = new JLabel("💡 " + text);
        l.setForeground(TXT);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        tip.add(l, BorderLayout.CENTER);

        return tip;
    }

    // ---------- shared UI ----------

    private static class GlassCard extends JPanel {
        private static final long serialVersionUID = 1L;
        GlassCard() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 28;

            // Shadow (soft)
            g2.setColor(SHADOW);
            g2.fillRoundRect(8, 10, w - 16, h - 16, arc, arc);

            // Fill (full)
            g2.setColor(CARD_FILL);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Border
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();
        }
    }

    private static class MiniIconButton extends JButton {
        private static final long serialVersionUID = 1L;
        MiniIconButton(String t) {
            super(t);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(TXT);
            setFont(new Font("SansSerif", Font.BOLD, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        }
    }

    private static class BananaButton extends JButton {
        private static final long serialVersionUID = 1L;

        BananaButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(new Color(25, 25, 25));
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setRolloverEnabled(true);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = 18;

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();

            g2.setColor(new Color(0, 0, 0, 95));
            g2.fillRoundRect(4, 6, w - 8, h - 8, arc, arc);

            Color fill = hover ? new Color(255, 225, 110) : ACCENT;
            if (press) fill = new Color(240, 200, 70);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(new Color(0, 0, 0, hover ? 130 : 95));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
