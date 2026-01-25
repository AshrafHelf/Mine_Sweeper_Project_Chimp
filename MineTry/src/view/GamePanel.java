package view;

import javax.swing.*;

import enums.Difficulty;
import enums.GameState;
import model.Board;
import model.Game;
import model.Player;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.function.BiConsumer;

public class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int MAX_LIVES = 10;
    
    private JPanel bananasRow;
    private ImageIcon bananaIcon;


    private final Game game;
    private final ImageIcon p1Avatar;
    private final ImageIcon p2Avatar;
    
    private Runnable settingsListener;
    private Runnable helpListener;

    public void onOpenSettings(Runnable r) { this.settingsListener = r; }
    public void onOpenHelp(Runnable r) { this.helpListener = r; }

    private Runnable backListener;
    private Runnable restartListener;
    private BiConsumer<Integer, Integer> revealListener;
    private BiConsumer<Integer, Integer> flagListener;

    private final ImageIcon bgGif = loadGif("/img/splash_leaves5.gif");

    // --- Theme ---
    private static final Color TXT_MAIN  = new Color(245, 242, 230);
    private static final Color TXT_MUTED = new Color(235, 235, 235, 170);

    private static final Color GLASS_TOP    = new Color(10, 14, 20, 140);
    private static final Color GLASS_GUIDE  = new Color(10, 14, 20, 120);
    private static final Color GLASS_BOARDS = new Color(10, 14, 20, 170);
    private static final Color GLASS_BOTTOM = new Color(10, 14, 20, 185);

    private static final Color STROKE_THIN  = new Color(255, 255, 255, 45);
    private static final Color STROKE_THICK = new Color(255, 255, 255, 65);

    private static final Color SHADOW = new Color(0, 0, 0, 95);

    // Guide chip accents
    private static final Color ACC_Q = new Color(195, 165, 40);
    private static final Color ACC_S = new Color(120, 70, 160);

    // UI
    private JLabel lblDifficulty;
    private JTextArea eventFeed;

    private BoardView boardAView;
    private BoardView boardBView;

    private BoardCard boardACard;
    private BoardCard boardBCard;
    private JLabel lblScore;

    public GamePanel(Game game, ImageIcon p1Avatar, ImageIcon p2Avatar) {
        this.game = game;
        this.p1Avatar = p1Avatar;
        this.p2Avatar = p2Avatar;

        setLayout(new BorderLayout());
        setOpaque(false);

        buildUI();
        refreshFromModel();
    }

    public GamePanel(Game game) {
        this(game, null, null);
    }


    // --- Listener hooks for controller ---
    public void onBackToMenu(Runnable r) { this.backListener = r; }
    public void onRestart(Runnable r) { this.restartListener = r; }
    public void onCellReveal(BiConsumer<Integer, Integer> c) { this.revealListener = c; }
    public void onCellFlag(BiConsumer<Integer, Integer> c) { this.flagListener = c; }

    // ✅ Controller uses this
    public void pushEvent(String text) {
        if (text == null || text.isBlank()) return;
        if (eventFeed == null) return;
        eventFeed.insert("• " + text.replace("\n", " | ") + "\n", 0);
        eventFeed.setCaretPosition(0);
    }

    private void buildUI() {
        add(buildTopBar(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(12, 16, 10, 16));

        center.add(buildGuideRow());
        center.add(Box.createVerticalStrut(12));
        center.add(buildBoardsRow());

        add(center, BorderLayout.CENTER);

        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    // =========================
    // Top bar
    // =========================
    private JComponent buildTopBar() {
        GlassPanel bar = new GlassPanel(GLASS_TOP, true);
        bar.setLayout(new BorderLayout(12, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JButton btnBack = new GlassButton("← Back");
        JButton btnRestart = new GlassButton("Restart");

        btnBack.addActionListener(e -> { if (backListener != null) backListener.run(); });
        btnRestart.addActionListener(e -> { if (restartListener != null) restartListener.run(); });

        left.add(btnBack);
        left.add(btnRestart);

        lblDifficulty = new JLabel("Difficulty: " + game.getDifficulty().name(), SwingConstants.CENTER);
        lblDifficulty.setForeground(TXT_MAIN);
        lblDifficulty.setFont(new Font("SansSerif", Font.BOLD, 16));

        bar.add(left, BorderLayout.WEST);
        bar.add(lblDifficulty, BorderLayout.CENTER);
        
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

     // Help button removed – help is accessible via top menu

        JButton btnSettings = new GlassButton("⚙ Settings");

        btnSettings.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            if (settingsListener != null) settingsListener.run();
        });
        right.add(btnSettings);

        bar.add(right, BorderLayout.EAST);

        return wrapWithMargin(bar, 10, 16, 0, 16);
    }

    // =========================
    // Guide + Events row
    // =========================
    private JComponent buildGuideRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        GlassPanel guide = new GlassPanel(GLASS_GUIDE, false);
        guide.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        guide.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        guide.add(chip("🖱", "Reveal", new Color(255,255,255,120)));
        guide.add(chip("🚩", "Flag",  new Color(255,255,255,120)));
        guide.add(chip("Q", "Question", ACC_Q));
        guide.add(chip("S", "Surprise",  ACC_S));

        GlassPanel events = new GlassPanel(GLASS_GUIDE, false);
        events.setLayout(new BorderLayout());
        events.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        events.setPreferredSize(new Dimension(360, 90));

        JLabel t = new JLabel("Events");
        t.setForeground(TXT_MAIN);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));

        eventFeed = new JTextArea(4, 20);
        eventFeed.setEditable(false);
        eventFeed.setFocusable(false);
        eventFeed.setLineWrap(true);
        eventFeed.setWrapStyleWord(true);
        eventFeed.setForeground(new Color(235, 235, 245));
        eventFeed.setBackground(new Color(0, 0, 0, 0));
        eventFeed.setFont(new Font("SansSerif", Font.PLAIN, 12));
        eventFeed.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JScrollPane sp = new JScrollPane(eventFeed);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        events.add(t, BorderLayout.NORTH);
        events.add(sp, BorderLayout.CENTER);

        row.add(guide, BorderLayout.CENTER);
        row.add(events, BorderLayout.EAST);

        return row;
    }

    private JComponent chip(String left, String right, Color accent) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);

        JLabel a = new JLabel(left);
        a.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 220));
        a.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel b = new JLabel(right);
        b.setForeground(TXT_MAIN);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));

        // pill background
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pill.setOpaque(true);
        pill.setBackground(new Color(0, 0, 0, 90));
        pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255,255,255,35)),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        pill.add(a);
        pill.add(b);

        p.add(pill);
        return p;
    }

    // =========================
    // Boards row
    // =========================
    private JComponent buildBoardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);

        Difficulty diff = game.getDifficulty();

        boardAView = new BoardView(diff.cols, diff.rows, "");
        boardBView = new BoardView(diff.cols, diff.rows, "");

        boardAView.setOnCellClick((c, r) -> { if (revealListener != null) revealListener.accept(c, r); });
        boardBView.setOnCellClick((c, r) -> { if (revealListener != null) revealListener.accept(c, r); });

        boardAView.setOnCellRightClick((c, r) -> { if (flagListener != null) flagListener.accept(c, r); });
        boardBView.setOnCellRightClick((c, r) -> { if (flagListener != null) flagListener.accept(c, r); });

        Player p1 = game.getPlayer1();
        Player p2 = game.getPlayer2();

        ImageIcon av1 = scaleIconOrFallback(p1Avatar, "A");
        ImageIcon av2 = scaleIconOrFallback(p2Avatar, "B");

        boardACard = new BoardCard("BOARD A", p1.getName(), av1, boardAView);
        boardBCard = new BoardCard("BOARD B", p2.getName(), av2, boardBView);

        row.add(boardACard);
        row.add(boardBCard);

        return row;
    }

    // =========================
    // Bottom bar (bananas + score)
    // =========================
    private JComponent buildBottomBar() {
        GlassPanel bottom = new GlassPanel(GLASS_BOTTOM, true);
        bottom.setLayout(new FlowLayout(FlowLayout.CENTER, 18, 10));
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        bananaIcon = UiAssets.icon("banana.png", 26, 26);

        bananasRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        bananasRow.setOpaque(false);

        JLabel bananasText = new JLabel("Team Lives:");
        bananasText.setForeground(TXT_MAIN);
        bananasText.setFont(new Font("SansSerif", Font.BOLD, 14));

        lblScore = new JLabel("Score: 0");
        lblScore.setForeground(TXT_MAIN);
        lblScore.setFont(new Font("SansSerif", Font.BOLD, 14));

        bottom.add(bananasText);
        bottom.add(bananasRow);
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(lblScore);

        return wrapWithMargin(bottom, 8, 16, 12, 16);
    }



    // =========================
    // Refresh from model
    // =========================
    public void refreshFromModel() {
        lblDifficulty.setText("Difficulty: " + game.getDifficulty().name());

        boardAView.renderBoard(game.getBoard1());
        boardBView.renderBoard(game.getBoard2());

        if (game.getState() == GameState.OVER) {
            boardAView.setBoardEnabled(false);
            boardBView.setBoardEnabled(false);
            boardACard.setState(false, true);
            boardBCard.setState(false, true);
        } else {
            boolean p1Turn = game.getActivePlayer() == game.getPlayer1();

            boardAView.setBoardEnabled(p1Turn);
            boardBView.setBoardEnabled(!p1Turn);

            boardACard.setState(p1Turn, false);
            boardBCard.setState(!p1Turn, false);
        }

        updateBananas(game.getTeamLives());
        lblScore.setText("Score: " + game.getTeamScore());
    }

    
    private void updateBananas(int lives) {
        bananasRow.removeAll();

        for (int i = 0; i < MAX_LIVES; i++) {
            JLabel b = new JLabel();

            if (bananaIcon != null) {
                // full banana if alive, dim placeholder if lost
                b.setIcon(bananaIcon);
                b.setEnabled(i < lives);
                b.setOpaque(false);

                if (i >= lives) {
                    // dim lost bananas
                    b.setIcon(new ImageIcon(
                            ((ImageIcon) bananaIcon).getImage()
                                    .getScaledInstance(18, 18, Image.SCALE_SMOOTH)
                    ));
                    b.setForeground(new Color(255, 255, 255, 70));
                }
            } else {
                // fallback if banana.png missing
                b.setText(i < lives ? "🍌" : "▫");
                b.setForeground(TXT_MAIN);
            }

            bananasRow.add(b);
        }

        bananasRow.revalidate();
        bananasRow.repaint();
    }


    // =========================
    // Background paint
    // =========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bgGif == null) {
            g.setColor(new Color(10, 12, 18));
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        Image img = bgGif.getImage();
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

        // dark overlay so UI stays readable
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 115));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    // =========================
    // Components
    // =========================

    private static class GlassPanel extends JPanel {
        private final Color fill;
        private final boolean thick;

        GlassPanel(Color fill, boolean thick) {
            this.fill = fill;
            this.thick = thick;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 22;

            // shadow
            g2.setColor(SHADOW);
            g2.fillRoundRect(6, 6, w - 8, h - 8, arc, arc);

            // fill
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 8, h - 8, arc, arc);

            // stroke
            g2.setColor(thick ? STROKE_THICK : STROKE_THIN);
            g2.setStroke(new BasicStroke(thick ? 2.2f : 1.8f));
            g2.drawRoundRect(1, 1, w - 10, h - 10, arc, arc);

            // top highlight line (nice glass feel)
            g2.setColor(new Color(255, 255, 255, thick ? 28 : 20));
            g2.drawRoundRect(2, 2, w - 12, h - 12, arc, arc);

            g2.dispose();
        }
    }

    private static class GlassButton extends JButton {
        GlassButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(TXT_MAIN);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = 18;

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();

            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillRoundRect(4, 5, w - 6, h - 6, arc, arc);

            Color fill = hover ? new Color(0, 0, 0, 170) : new Color(0, 0, 0, 135);
            if (press) fill = new Color(0, 0, 0, 210);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 6, h - 6, arc, arc);

            g2.setColor(new Color(255, 255, 255, hover ? 90 : 60));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 8, h - 8, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class BoardCard extends JPanel {
        private final BoardHeader header;
        private final JPanel overlay;
        private boolean active = true;
        private boolean gameOver = false;

        BoardCard(String boardLabel, String playerName, ImageIcon avatar, JComponent board) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            header = new BoardHeader(boardLabel, playerName, avatar);

            GlassPanel frame = new GlassPanel(GLASS_BOARDS, true);
            frame.setLayout(new BorderLayout());
            frame.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            frame.add(header, BorderLayout.NORTH);
            frame.add(Box.createVerticalStrut(10), BorderLayout.CENTER); // replaced below with layered

            JLayeredPane layers = new JLayeredPane();
            layers.setLayout(new OverlayLayout(layers));
            layers.setOpaque(false);

            JPanel boardWrap = new JPanel(new BorderLayout());
            boardWrap.setOpaque(false);
            boardWrap.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            boardWrap.add(board, BorderLayout.CENTER);

            overlay = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    if (active && !gameOver) return;

                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(new Color(0, 0, 0, 140));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                    g2.setColor(new Color(255, 255, 255, 220));

                    String msg = gameOver ? "GAME OVER" : "WAITING…";
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(msg)) / 2;
                    int y = getHeight() / 2;

                    g2.drawString(msg, x, y);
                    g2.dispose();
                }
            };
            overlay.setOpaque(false);

            layers.add(boardWrap);
            layers.add(overlay);

            // put layered boards in the frame
            frame.removeAll();
            frame.add(header, BorderLayout.NORTH);
            frame.add(layers, BorderLayout.CENTER);

            add(frame, BorderLayout.CENTER);
        }

        void setState(boolean active, boolean gameOver) {
            this.active = active;
            this.gameOver = gameOver;
            header.setActive(active && !gameOver);
            overlay.repaint();
        }
    }

    private static class BoardHeader extends JPanel {
        private final String boardLabel;
        private final JLabel nameLabel;
        private boolean active;

        BoardHeader(String boardLabel, String playerName, Icon avatar) {
            this.boardLabel = boardLabel;
            setOpaque(false);
            setLayout(new BorderLayout());

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            left.setOpaque(false);

            JLabel tag = new JLabel(boardLabel);
            tag.setOpaque(true);
            tag.setBackground(new Color(255, 255, 255, 45));
            tag.setForeground(TXT_MAIN);
            tag.setFont(new Font("SansSerif", Font.BOLD, 11));
            tag.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

            JLabel av = new JLabel(avatar);

            nameLabel = new JLabel(playerName);
            nameLabel.setForeground(TXT_MAIN);
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

            left.add(tag);
            left.add(av);
            left.add(nameLabel);

            JLabel badge = new JLabel(" ");
            badge.setForeground(TXT_MUTED);
            badge.setFont(new Font("SansSerif", Font.BOLD, 12));

            add(left, BorderLayout.WEST);
            add(badge, BorderLayout.EAST);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setActive(true);
        }

        void setActive(boolean active) {
            this.active = active;
            nameLabel.setForeground(active ? new Color(255,255,255,245) : new Color(255,255,255,180));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 18;

            // header glass base
            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // active white glow (subtle)
            if (active) {
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                g2.setColor(new Color(255, 255, 255, 85));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            } else {
                g2.setColor(new Color(255, 255, 255, 35));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            }

            g2.dispose();
        }
    }

    // =========================
    // Helpers
    // =========================
    private static ImageIcon loadGif(String path) {
        URL url = GamePanel.class.getResource(path);
        if (url == null) {
            System.out.println("⚠ Background not found: " + path);
            return null;
        }
        return new ImageIcon(url);
    }

    private static JComponent wrapWithMargin(JComponent c, int top, int left, int bottom, int right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private static ImageIcon scaleIconOrFallback(ImageIcon src, String letter) {
        if (src == null || src.getIconWidth() <= 0) {
            // fallback tiny circle avatar (won’t crash)
            BufferedImage img = new BufferedImage(52, 52, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(255, 255, 255, 55));
            g2.fillOval(0, 0, 52, 52);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawOval(1, 1, 50, 50);

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int x = (34 - fm.stringWidth(letter)) / 2;
            int y = (34 - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(new Color(255, 255, 255, 220));
            g2.drawString(letter, x, y);

            g2.dispose();
            return new ImageIcon(img);
        }

        Image scaled = src.getImage().getScaledInstance(52, 52, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
