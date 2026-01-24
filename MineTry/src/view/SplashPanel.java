package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.awt.image.BufferedImage;

public class SplashPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private Runnable onContinue;

    // fade from black (alpha: 1 -> 0)
    private float fadeAlpha = 1f;
    private Timer fadeTimer;

    private final Image gifImage;

    public SplashPanel() {
        setLayout(new BorderLayout());
        setFocusable(true);
        setOpaque(true);

        ImageIcon gifIcon = loadGif("/img/splash_leaves5.gif");
        this.gifImage = gifIcon.getImage();

        GifBackgroundPanel bg = new GifBackgroundPanel(gifImage);
        bg.setLayout(new GridBagLayout());
        add(bg, BorderLayout.CENTER);

        // --- card ---
        JPanel card = glassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(860, 260)); // tweak width/height as you like


        // --- TITLE ROW: title + splash overlay like Minecraft ---
        JComponent titleBlock = buildTitleWithSplash();
        titleBlock.setAlignmentX(Component.CENTER_ALIGNMENT);
        

        JLabel subtitle = new JLabel("A Jungle Co-Op Minesweeper Experience");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(240, 240, 240, 210));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // Nice primary button (more user-friendly than “click anywhere” only)
        JButton enterBtn = new SmoothButton("Enter Jungle");
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterBtn.addActionListener(e -> fireContinue());

        // Hint: blinking (still there)
        BlinkLabel hint = new BlinkLabel("Click anywhere / press ENTER to continue");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(new Color(255, 230, 140, 230));
        hint.setFont(new Font("SansSerif", Font.BOLD, 13));

        // spacing
        card.add(titleBlock);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(14));
        card.add(enterBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(hint);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 70, 0, 70);
        gbc.fill = GridBagConstraints.NONE;
        bg.add(card, gbc);

        // Click anywhere (attach to bg, not only panel)
        MouseAdapter clickAnywhere = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { fireContinue(); }
        };
        bg.addMouseListener(clickAnywhere);
        card.addMouseListener(clickAnywhere);
        this.addMouseListener(clickAnywhere);

        // Key bindings (reliable)
        installKeyBindings();

        // Fade-in
        fadeTimer = new Timer(20, e -> {
            fadeAlpha -= 0.035f;
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f;
                fadeTimer.stop();
            }
            repaint();
        });
        fadeTimer.start();
    }

    public void setOnContinue(Runnable r) {
        this.onContinue = r;
    }

    private void fireContinue() {
        if (onContinue != null) onContinue.run();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    private void installKeyBindings() {
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = getInputMap(condition);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "continue");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "continue");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "continue"); // optional

        am.put("continue", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { fireContinue(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // --- Cinematic gradient overlay (darkens edges + improves readability) ---
        // top fade
        g2.setComposite(AlphaComposite.SrcOver.derive(0.18f));
        g2.setPaint(new GradientPaint(0, 0, new Color(0,0,0,200), 0, h/2f, new Color(0,0,0,0)));
        g2.fillRect(0, 0, w, h);

        // bottom fade
        g2.setComposite(AlphaComposite.SrcOver.derive(0.22f));
        g2.setPaint(new GradientPaint(0, h, new Color(0,0,0,220), 0, h/2f, new Color(0,0,0,0)));
        g2.fillRect(0, 0, w, h);

        // subtle vignette
        g2.setComposite(AlphaComposite.SrcOver.derive(0.10f));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, 12);
        g2.fillRect(0, h-12, w, 12);
        g2.fillRect(0, 0, 12, h);
        g2.fillRect(w-12, 0, 12, h);

        // --- Fade from black (keep your fade) ---
        if (fadeAlpha > 0f) {
            g2.setComposite(AlphaComposite.SrcOver.derive(fadeAlpha));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
        }

        g2.dispose();
    }

    private ImageIcon loadGif(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("❌ Splash GIF not found: " + path);
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }
        return new ImageIcon(url);
    }

 // --- Title + splash overlay ---
    private JComponent buildTitleWithSplash() {
        OutlineLabel title = new OutlineLabel(
                "CHIMP SWEEPER",
                new Font("SansSerif", Font.BOLD, 74),
                new Color(245, 242, 230),
                new Color(10, 10, 10, 220),
                3
        );

        RotatedLabel splash = new RotatedLabel("NEW ADVENTURE!", -12);
        splash.setFont(new Font("SansSerif", Font.BOLD, 20));
        splash.setForeground(new Color(255, 220, 90));

        JPanel overlay = new JPanel();
        overlay.setOpaque(false);
        overlay.setLayout(new OverlayLayout(overlay));

        JPanel splashWrap = new JPanel(null);
        splashWrap.setOpaque(false);
        splashWrap.add(splash);

        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titleWrap.setOpaque(false);
        titleWrap.add(title);

        overlay.add(splashWrap);
        overlay.add(titleWrap);

        // ✅ Size the overlay bigger than the title so splash never clips
        Dimension t = title.getPreferredSize();
        Dimension s = splash.getPreferredSize();

        int extraRight = s.width + 40; // more safety
        int extraTop   = 28;           // more top room for rotation

        Dimension big = new Dimension(t.width + extraRight, t.height + extraTop);

        overlay.setPreferredSize(big);
        overlay.setMinimumSize(big);
        overlay.setMaximumSize(new Dimension(2000, big.height));

        // ✅ VERY IMPORTANT: wrappers must also report the same size to OverlayLayout
        titleWrap.setPreferredSize(big);
        titleWrap.setMinimumSize(big);
        titleWrap.setMaximumSize(new Dimension(2000, big.height));

        splashWrap.setPreferredSize(big);
        splashWrap.setMinimumSize(big);
        splashWrap.setMaximumSize(new Dimension(2000, big.height));

        // ✅ Clamp splash bounds so it can never go outside the overlay
        splashWrap.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int w = splashWrap.getWidth();
                int h = splashWrap.getHeight();
                Dimension sd = splash.getPreferredSize();

                // placement (over right end of title)
                int desiredX = (int) (w * 0.68);
                int desiredY = (int) (h * 0.36);

                int x = Math.max(0, Math.min(desiredX, w - sd.width - 6));
                int y = Math.max(0, Math.min(desiredY, h - sd.height - 6));

                // ✅ DO NOT add +5 (that can cause clipping)
                splash.setBounds(x, y, sd.width, sd.height);
            }
        });

        return overlay;
    }


    // --- Background that covers the whole window ---
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

    // --- Glass card (cleaner + soft shadow) ---
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
                g2.setColor(new Color(10, 10, 10, 140));
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);

                // border highlight
                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 8, getHeight() - 8, arc, arc);

                g2.dispose();
            }
        };

        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(18, 30, 18, 30));
        return p;
    }

    // --- Title with outline ---
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

    // --- Rotated label ---
    private static class RotatedLabel extends JLabel {
        private final double degrees;

        RotatedLabel(String text, double degrees) {
            super(text);
            this.degrees = degrees;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            double rad = Math.toRadians(degrees);

            // rotated bounding box
            int w = d.width;
            int h = d.height;

            int newW = (int) Math.ceil(Math.abs(w * Math.cos(rad)) + Math.abs(h * Math.sin(rad)));
            int newH = (int) Math.ceil(Math.abs(w * Math.sin(rad)) + Math.abs(h * Math.cos(rad)));

            // add a little safety padding
            return new Dimension(newW + 12, newH + 12);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // rotate around center
            g2.rotate(Math.toRadians(degrees), w / 2.0, h / 2.0);

            super.paintComponent(g2);
            g2.dispose();
        }
    }


    // --- Blinking hint ---
    private static class BlinkLabel extends JLabel {
        private float a = 1f;
        private boolean down = true;

        BlinkLabel(String text) {
            super(text);
            setOpaque(false);

            new Timer(60, e -> {
                if (down) a -= 0.06f; else a += 0.06f;
                if (a <= 0.25f) { a = 0.25f; down = false; }
                if (a >= 1f) { a = 1f; down = true; }

                Color c = getForeground();
                setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(255 * a)));
            }).start();
        }
    }

    // --- Smooth button (Minecraft-ish but modern) ---
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
            return new Dimension(320, ps.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();

            int w = getWidth();
            int h = getHeight();
            int arc = 16;

            int shadowOffset = 4;
            int pressOffset  = press ? 2 : 0;

            // shadow (slightly down/right)
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(shadowOffset, shadowOffset + pressOffset, w - shadowOffset, h - shadowOffset, arc, arc);

            // base (FULL AREA so text is perfectly centered)
            Color base = hover ? new Color(30, 80, 45, 190) : new Color(20, 60, 35, 170);
            if (press) base = new Color(15, 45, 25, 210);

            g2.setColor(base);
            g2.fillRoundRect(0, pressOffset, w - shadowOffset, h - shadowOffset, arc, arc);

            // border
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, pressOffset + 1, w - shadowOffset - 2, h - shadowOffset - 2, arc, arc);

            g2.dispose();

            // draw text AFTER, Swing centers it correctly
            super.paintComponent(g);
        }
    }
    

}
