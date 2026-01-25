package view;

import model.Question;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;

public class QuestionDialog extends JDialog {
	

    private static final long serialVersionUID = 1L;

    private boolean answered = false;
    private boolean correct  = false;

    // --- Match GameOverDialog palette ---
    private static final Color TXT       = new Color(245, 242, 230);
    private static final Color MUTED     = new Color(210, 220, 215, 160);
    private static final Color ACCENT    = new Color(255, 215, 90);

    private static final Color CARD_FILL   = new Color(35, 68, 52, 235);
    private static final Color CARD_BORDER = new Color(140, 190, 155, 90);
    private static final Color SHADOW      = new Color(0, 0, 0, 120);

    private static final Dimension SIZE = new Dimension(760, 520);

    // Your gif path
    private static final String Q_GIF = "/img/questionsgif.gif";

    public QuestionDialog(JFrame owner, Question question) {
        super(owner, "Question", true);

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Transparent window background (no black block)
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        setContentPane(root);

        GlassCard card = new GlassCard();
        card.setLayout(new BorderLayout(14, 14));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(card, gbc);

        // ---------- HEADER ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("❓ QUESTION TILE");
        title.setForeground(TXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JLabel subtitle = new JLabel("Choose one answer (A–D). Cancel = no effect.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        titles.add(title);
        titles.add(Box.createVerticalStrut(3));
        titles.add(subtitle);

        // Right side: GIF (animated + scaled) + close
        JPanel rightHeader = new JPanel();
        rightHeader.setOpaque(false);
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.X_AXIS));

        JComponent gif = buildQuestionGif(); // ✅ real animated scaled gif
        gif.setBorder(new EmptyBorder(0, 0, 0, 10));

        JButton close = new MiniIconButton("✕");
        close.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            answered = false;
            dispose();
        });

        rightHeader.add(gif);
        rightHeader.add(close);

        header.add(titles, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        // ---------- QUESTION BOX ----------
        JPanel qBox = questionBox(question.getText());

        // ---------- ANSWERS ----------
        JPanel answersPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        answersPanel.setOpaque(false);

        List<String> opts = question.getOptions();
        for (int i = 0; i < opts.size(); i++) {
            final int idx = i;

            String label = switch (i) {
                case 0 -> "A. " + opts.get(i);
                case 1 -> "B. " + opts.get(i);
                case 2 -> "C. " + opts.get(i);
                case 3 -> "D. " + opts.get(i);
                default -> opts.get(i);
            };

            JButton btn = new GlassButton(label);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.addActionListener(e -> {
                AudioManager.playSfx("button.wav");
                answered = true;
                correct = (idx == question.getCorrectIndex());
                dispose();
            });

            answersPanel.add(btn);
        }

        // ---------- FOOTER ----------
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton cancel = new GlassButton("Cancel");
        cancel.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            answered = false;
            dispose();
        });
        footer.add(cancel);

        // ---------- CENTER STACK ----------
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(qBox);
        center.add(Box.createVerticalStrut(14));
        center.add(answersPanel);

        card.add(header, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        // ESC = cancel
        getRootPane().registerKeyboardAction(
                e -> { AudioManager.playSfx("button.wav"); answered = false; dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setSize(SIZE);
        setLocationRelativeTo(owner);
    }

    public Boolean showAndGetResult() {
        setVisible(true);
        return answered ? Boolean.valueOf(correct) : null;
    }

    // -----------------------------
    // GIF (animated + scaled safely)
    // -----------------------------
    private JComponent buildQuestionGif() {
        ImageIcon icon = loadGifSmart(Q_GIF);

        // If not found -> return an empty spacer so layout doesn't break
        if (icon == null || icon.getIconWidth() <= 0) {
        	JPanel spacer = new JPanel();
        	spacer.setOpaque(false);
        	spacer.setPreferredSize(new Dimension(86, 86));
        	return spacer;
        }

        GifView view = new GifView(icon.getImage(), 86, 86);
        view.setPreferredSize(new Dimension(86, 86));
        view.setMinimumSize(new Dimension(86, 86));
        view.setMaximumSize(new Dimension(86, 86));
        return view;
    }

    private static ImageIcon loadGifSmart(String path) {
        // 1) Normal getResource
        URL url = QuestionDialog.class.getResource(path);

        // 2) Fallback: classloader (sometimes needed in jar)
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader()
                    .getResource(path.startsWith("/") ? path.substring(1) : path);
        }

        // 3) Fallback: try without leading slash
        if (url == null && path.startsWith("/")) {
            url = QuestionDialog.class.getResource(path.substring(1));
        }

        if (url == null) return null;
        return new ImageIcon(url);
    }

    // Small component that draws the GIF scaled (keeps animation)
    private static class GifView extends JComponent {
        private static final long serialVersionUID = 1L;

        private final Image img;
        private final int w, h;
        private final Timer repaintTimer;

        GifView(Image img, int w, int h) {
            this.img = img;
            this.w = w;
            this.h = h;
            setOpaque(false);

            // repaint often enough so animated GIF advances frames smoothly
            repaintTimer = new Timer(40, e -> repaint());
            repaintTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = (getWidth() - w) / 2;
            int y = 0; // top aligned
            g2.drawImage(img, x, y, w, h, this);

            g2.dispose();
        }

        @Override public void removeNotify() {
            super.removeNotify();
            if (repaintTimer != null) repaintTimer.stop();
        }
    }

    // -----------------------------
    // Question strip
    // -----------------------------
    private static JPanel questionBox(String text) {
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int arc = 18;

                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(4, 5, w - 8, h - 8, arc, arc);

                g2.setColor(new Color(0, 0, 0, 145));
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                g2.setColor(new Color(255, 255, 255, 50));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(12, 14, 12, 14));
        box.setPreferredSize(new Dimension(1, 120));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JTextArea ta = new JTextArea(text);
        ta.setOpaque(false);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setForeground(TXT);
        ta.setFont(new Font("SansSerif", Font.PLAIN, 15));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(ta);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        box.add(sp, BorderLayout.CENTER);
        return box;
    }

    // -----------------------------
    // UI components (GameOver family)
    // -----------------------------
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

            g2.setColor(SHADOW);
            g2.fillRoundRect(8, 10, w - 16, h - 16, arc, arc);

            g2.setColor(CARD_FILL);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

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

    private static class GlassButton extends JButton {
        private static final long serialVersionUID = 1L;

        GlassButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(TXT);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setRolloverEnabled(true);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = 16;

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();

            g2.setColor(new Color(0, 0, 0, 95));
            g2.fillRoundRect(4, 6, w - 8, h - 8, arc, arc);

            Color fill = hover ? new Color(0, 0, 0, 180) : new Color(0, 0, 0, 140);
            if (press) fill = new Color(0, 0, 0, 220);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(new Color(255, 255, 255, hover ? 90 : 65));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            if (hover) {
                g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 70));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
