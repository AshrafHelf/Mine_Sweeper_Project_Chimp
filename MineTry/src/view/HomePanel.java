package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.awt.image.BufferedImage;

/**
 * Home (Main Menu) - matches SplashPanel glass style.
 * Buttons: Play / History / Questions / Exit.
 */
public class HomePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private Runnable onPlay;
    private Runnable onHistory;
    private Runnable onQuestions;
    private Runnable onExit;

    private final Image gifImage;

    public HomePanel() {
        setLayout(new BorderLayout());
        setFocusable(true);
        setOpaque(true);

        // ✅ Same background family as Splash
        ImageIcon gifIcon = loadGif("/img/splash_leaves5.gif");
        this.gifImage = gifIcon.getImage();

        GifBackgroundPanel bg = new GifBackgroundPanel(gifImage);
        bg.setLayout(new GridBagLayout());
        add(bg, BorderLayout.CENTER);

        // ✅ SAME glass card as Splash
        JPanel card = glassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Title (smaller than splash but same vibe)
        OutlineLabel title = new OutlineLabel(
                "CHIMP SWEEPER",
                new Font("SansSerif", Font.BOLD, 62),
                new Color(245, 242, 230),
                new Color(10, 10, 10, 220),
                3
        );
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Home Base • Choose your path");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(240, 240, 240, 210));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // ✅ SAME button style as Splash
        JButton play = new SmoothButton("▶  Play");
        JButton history = new SmoothButton("📜  History");
        JButton questions = new SmoothButton("❓  Questions");
        JButton exit = new SmoothButton("⛔  Exit");

        play.setAlignmentX(Component.CENTER_ALIGNMENT);
        history.setAlignmentX(Component.CENTER_ALIGNMENT);
        questions.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);

        // spacing
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));
        card.add(play);
        card.add(Box.createVerticalStrut(10));
        card.add(history);
        card.add(Box.createVerticalStrut(10));
        card.add(questions);
        card.add(Box.createVerticalStrut(10));
        card.add(exit);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 70, 0, 70);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bg.add(card, gbc);

        // actions
        play.addActionListener(e -> { if (onPlay != null) onPlay.run(); });
        history.addActionListener(e -> { if (onHistory != null) onHistory.run(); });
        questions.addActionListener(e -> { if (onQuestions != null) onQuestions.run(); });
        exit.addActionListener(e -> { if (onExit != null) onExit.run(); });

        // keyboard
        installKeyBindings();
    }

    public void setOnPlay(Runnable r) { this.onPlay = r; }
    public void setOnHistory(Runnable r) { this.onHistory = r; }
    public void setOnQuestions(Runnable r) { this.onQuestions = r; }
    public void setOnExit(Runnable r) { this.onExit = r; }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    private void installKeyBindings() {
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = getInputMap(condition);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "play");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "play");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");

        am.put("play", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (onPlay != null) onPlay.run();
            }
        });
        am.put("exit", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (onExit != null) onExit.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // ✅ Same subtle readability overlay as Splash
        g2.setComposite(AlphaComposite.SrcOver.derive(0.12f));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }

    private ImageIcon loadGif(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("❌ Home GIF not found: " + path);
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }
        return new ImageIcon(url);
    }

    // --- Background that covers the whole window (same as Splash) ---
    private static class GifBackgroundPanel extends JPanel {
        private final Image gif;

        GifBackgroundPanel(Image gif) {
            this.gif = gif;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (gif == null) return;

            int w = getWidth();
            int h = getHeight();

            int imgW = gif.getWidth(this);
            int imgH = gif.getHeight(this);
            if (imgW <= 0 || imgH <= 0) return;

            double scale = Math.max((double) w / imgW, (double) h / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (w - drawW) / 2;
            int y = (h - drawH) / 2;

            g.drawImage(gif, x, y, drawW, drawH, this);
        }
    }

    // --- Glass card (COPY from Splash) ---
    private JPanel glassCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 22;

                // shadow
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillRoundRect(6, 6, getWidth() - 6, getHeight() - 6, arc, arc);

                // glass
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);

                // border highlight
                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 8, getHeight() - 8, arc, arc);

                g2.dispose();
            }
        };

        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 34, 20, 34));
        return p;
    }

    // --- Title with outline (COPY from Splash style) ---
    private static class OutlineLabel extends JComponent {
        private final String text;
        private final Font font;
        private final Color fill;
        private final Color outline;
        private final int outlineSize;

        OutlineLabel(String text, Font font, Color fill, Color outline, int outlineSize) {
            this.text = text;
            this.font = font;
            this.fill = fill;
            this.outline = outline;
            this.outlineSize = outlineSize;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(font);
            int w = fm.stringWidth(text) + 24;
            int h = fm.getHeight() + 18;
            return new Dimension(w, h);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font);

            FontMetrics fm = g2.getFontMetrics();
            int x = 12;
            int y = 9 + fm.getAscent();

            g2.setColor(outline);
            for (int dx = -outlineSize; dx <= outlineSize; dx++) {
                for (int dy = -outlineSize; dy <= outlineSize; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    g2.drawString(text, x + dx, y + dy);
                }
            }

            g2.setColor(fill);
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }

    // --- Smooth button (COPY from Splash style) ---
    private static class SmoothButton extends JButton {
        SmoothButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(new Color(245, 242, 230));
            setFont(new Font("SansSerif", Font.BOLD, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension ps = getPreferredSize();
            return new Dimension(340, ps.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();

            int arc = 16;

            // shadow
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, arc, arc);

            // base
            g2.setColor(hover ? new Color(30, 30, 30, 190) : new Color(20, 20, 20, 170));
            if (press) g2.setColor(new Color(15, 15, 15, 210));
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, arc, arc);

            // border
            g2.setColor(new Color(255, 255, 255, 55));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
