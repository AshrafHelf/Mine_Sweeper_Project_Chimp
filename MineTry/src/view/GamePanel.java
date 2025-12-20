package view;

import javax.swing.*;

import model.Board;
import model.Difficulty;
import model.Game;
import model.GameState;
import model.Player;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.BiConsumer;

public class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int MAX_LIVES = 10;

    private final Game game;

    private Runnable backListener;
    private Runnable restartListener;
    private BiConsumer<Integer, Integer> revealListener;
    private BiConsumer<Integer, Integer> flagListener;

    private JLabel lblTurn;
    private JLabel lblLives;
    private JLabel lblScore;

    private BoardView boardAView;
    private BoardView boardBView;

    private transient Image backgroundImg;

    // Theme colors (consistent)
    private static final Color INK_BG       = new Color(10, 14, 33);
    private static final Color GLASS_BG     = new Color(10, 14, 33, 170);
    private static final Color GLASS_BG_2   = new Color(18, 24, 52, 180);
    private static final Color ACCENT_LINE  = new Color(120, 170, 120, 130); // jungle accent
    private static final Color BORDER_LINE  = new Color(35, 45, 90, 200);

    public GamePanel(Game game) {
        this.game = game;

        setOpaque(false);
        backgroundImg = loadImage("/images/jungle.png");

        buildUI();
        refreshFromModel();
    }

    // --- Listener hooks for controller ---
    public void onBackToMenu(Runnable r) { this.backListener = r; }
    public void onRestart(Runnable r) { this.restartListener = r; }
    public void onCellReveal(BiConsumer<Integer, Integer> c) { this.revealListener = c; }
    public void onCellFlag(BiConsumer<Integer, Integer> c) { this.flagListener = c; }

    public void requestBack() { if (backListener != null) backListener.run(); }
    public void requestRestart() { if (restartListener != null) restartListener.run(); }

    private void buildUI() {
        setLayout(new BorderLayout());

        // ===== Top bar (glass) =====
        JPanel top = new GlassBarPanel();
        top.setLayout(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBtns.setOpaque(false);

        JButton btnBack = new JButton("Back");
        JButton btnRestart = new JButton("Restart");
        styleTopButton(btnBack, false);
        styleTopButton(btnRestart, true);

        btnBack.addActionListener(e -> { if (backListener != null) backListener.run(); });
        btnRestart.addActionListener(e -> { if (restartListener != null) restartListener.run(); });

        leftBtns.add(btnBack);
        leftBtns.add(btnRestart);

        lblTurn = new JLabel();
        lblTurn.setForeground(Color.WHITE);
        lblTurn.setFont(lblTurn.getFont().deriveFont(Font.BOLD, 14f));

        top.add(leftBtns, BorderLayout.WEST);
        top.add(lblTurn, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        // ===== Center =====
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // Legend pill bar
        JPanel legendWrap = createLegendPanel();
        center.add(legendWrap, BorderLayout.NORTH);


        Difficulty diff = game.getDifficulty();
        Player p1 = game.getPlayer1();
        Player p2 = game.getPlayer2();

        boardAView = new BoardView(diff.cols, diff.rows, "Board A – " + p1.getName());
        boardBView = new BoardView(diff.cols, diff.rows, "Board B – " + p2.getName());

        boardAView.setOnCellClick((c, r) -> { if (revealListener != null) revealListener.accept(c, r); });
        boardBView.setOnCellClick((c, r) -> { if (revealListener != null) revealListener.accept(c, r); });

        boardAView.setOnCellRightClick((c, r) -> { if (flagListener != null) flagListener.accept(c, r); });
        boardBView.setOnCellRightClick((c, r) -> { if (flagListener != null) flagListener.accept(c, r); });

        JPanel boardsRow = new JPanel(new GridLayout(1, 2, 22, 0));
        boardsRow.setOpaque(false);

        boardsRow.add(new BoardCard(boardAView));
        boardsRow.add(new BoardCard(boardBView));

        center.add(boardsRow, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ===== Bottom bar (glass) =====
        JPanel bottom = new GlassBarPanel();
        bottom.setLayout(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        lblLives = new JLabel();
        lblLives.setForeground(Color.WHITE);
        lblLives.setFont(lblLives.getFont().deriveFont(13f));

        lblScore = new JLabel();
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(lblScore.getFont().deriveFont(Font.BOLD, 15f));

        bottom.add(lblLives, BorderLayout.WEST);
        bottom.add(lblScore, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    private void styleTopButton(JButton b, boolean primary) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12.5f));

        if (primary) {
            b.setBackground(new Color(46, 125, 50)); // jungle green
        } else {
            b.setBackground(new Color(27, 36, 78));
        }
    }

    

 // inside GamePanel

    private JPanel createLegendPanel() {
        JPanel legendWrap = new GlassPillPanel();
        legendWrap.setLayout(new FlowLayout(FlowLayout.CENTER, 18, 10));

        Icon flagIcon = scaledIcon("/images/flag.png", 20, 20);
        Icon bananaIcon = scaledIcon("/images/banana.png", 22, 22);

        legendWrap.add(createLegendItemDot(new Color(255, 215, 0), "Question (Q)"));
        legendWrap.add(createLegendItemDot(new Color(186, 85, 211), "Surprise (S)"));

        legendWrap.add(createLegendItemIcon(flagIcon, "Flag"));
        legendWrap.add(createLegendItemIcon(bananaIcon, "Banana = Mine"));

        legendWrap.add(createLegendItemDot(new Color(23, 35, 74), "Normal"));

        return legendWrap;
    }


    private JComponent createLegendItemIcon(Icon icon, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setOpaque(false);

        JLabel iconLbl = new JLabel(icon);
        JLabel lbl = new JLabel(text);

        lbl.setForeground(new Color(235, 235, 255));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13.5f));

        p.add(iconLbl);
        p.add(lbl);
        return p;
    }

    private JComponent createLegendItemDot(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setOpaque(false);

        JComponent dot = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 0, 0, 120));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));

        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(235, 235, 255));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13.5f));

        p.add(dot);
        p.add(lbl);
        return p;
    }

    // --- Redraw everything from model ---
    public void refreshFromModel() {
        Player p1 = game.getPlayer1();
        Board b1 = game.getBoard1();
        Board b2 = game.getBoard2();

        boardAView.renderBoard(b1);
        boardBView.renderBoard(b2);

        if (game.getState() == GameState.OVER) {
            lblTurn.setText("Game ended.");
            boardAView.setBoardEnabled(false);
            boardBView.setBoardEnabled(false);
        } else {
            lblTurn.setText("Current turn: " + game.getActivePlayer().getName());

            if (game.getActivePlayer() == p1) {
                boardAView.setBoardEnabled(true);
                boardBView.setBoardEnabled(false);
            } else {
                boardAView.setBoardEnabled(false);
                boardBView.setBoardEnabled(true);
            }
        }

        int lives = game.getTeamLives();
        StringBuilder hearts = new StringBuilder("Shared lives: ")
                .append(lives).append("/").append(MAX_LIVES).append("   ");
        for (int i = 0; i < MAX_LIVES; i++) hearts.append(i < lives ? "♥" : "♡");
        lblLives.setText(hearts.toString());

        lblScore.setText("Team score: " + game.getTeamScore());
    }

    // ===== Background painting =====
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // base fill
        g2.setColor(INK_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // background image
        if (backgroundImg != null) {
            g2.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
        }

        // stronger overlay (so boards are readable on bright jungle)
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // soft vignette
        g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 40),
                0, getHeight(), new Color(0, 0, 0, 170)));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
        super.paintComponent(g);
    }

    private Image loadImage(String classpath) {
        try {
            java.net.URL url = getClass().getResource(classpath);
            if (url == null) return null;
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            return null;
        }
    }

    // =========================
    // Small internal UI classes
    // =========================

    /** Semi-transparent top/bottom bar */
    private static class GlassBarPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        public GlassBarPanel() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(GLASS_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(BORDER_LINE);
            g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Legend background pill */
    private static class GlassPillPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        public GlassPillPanel() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;
            Shape rr = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);

            g2.setColor(GLASS_BG_2);
            g2.fill(rr);

            g2.setColor(ACCENT_LINE);
            g2.draw(rr);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Card wrapper for each board */
    private static class BoardCard extends JPanel {
        private static final long serialVersionUID = 1L;

        public BoardCard(JComponent board) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(board, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;

            // subtle shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(4, 6, getWidth()-8, getHeight()-10, arc, arc);

            // glass body
            g2.setColor(new Color(10, 14, 33, 160));
            g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, arc, arc);

            // border
            g2.setColor(new Color(120, 170, 120, 140));
            g2.drawRoundRect(0, 0, getWidth()-5, getHeight()-5, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    private Icon scaledIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) return null;
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

}


