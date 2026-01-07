package view;

import javax.swing.*;

import enums.Difficulty;
import enums.GameState;
import model.Board;
import model.Game;
import model.Player;
import view.WoodButton;

import java.awt.*;
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

    public GamePanel(Game game) {
        this.game = game;
        buildUI();
        refreshFromModel();
    }

    public void onBackToMenu(Runnable r) { this.backListener = r; }
    public void onRestart(Runnable r) { this.restartListener = r; }
    public void onCellReveal(BiConsumer<Integer, Integer> c) { this.revealListener = c; }
    public void onCellFlag(BiConsumer<Integer, Integer> c) { this.flagListener = c; }

    public void requestBack() { if (backListener != null) backListener.run(); }
    public void requestRestart() { if (restartListener != null) restartListener.run(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = new GradientPaint(0, 0, new Color(12, 60, 40), 0, h, new Color(5, 18, 12));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRect(0, (int)(h*0.78), w, (int)(h*0.22));

        } finally {
            g2.dispose();
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // ===== Top bar =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBtns.setOpaque(false);

        WoodButton btnBack = new WoodButton("MENU");
        WoodButton btnRestart = new WoodButton("RESTART");

        btnBack.addActionListener(e -> { if (backListener != null) backListener.run(); });
        btnRestart.addActionListener(e -> { if (restartListener != null) restartListener.run(); });

        leftBtns.add(btnBack);
        leftBtns.add(btnRestart);

        lblTurn = new JLabel();
        lblTurn.setForeground(new Color(240, 255, 240));
        lblTurn.setFont(lblTurn.getFont().deriveFont(Font.BOLD, 14f));

        topBar.add(leftBtns, BorderLayout.WEST);
        topBar.add(lblTurn, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        // ===== Center: boards + legend =====
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel legend = createLegendPanel();
        center.add(legend, BorderLayout.NORTH);

        Difficulty diff = game.getDifficulty();
        Player p1 = game.getPlayer1();
        Player p2 = game.getPlayer2();

        boardAView = new BoardView(diff.cols, diff.rows, "Board A – " + p1.getName());
        boardBView = new BoardView(diff.cols, diff.rows, "Board B – " + p2.getName());

        boardAView.setOnCellClick((c, r) -> { if (revealListener != null) revealListener.accept(c, r); });
        boardBView.setOnCellClick((c, r) -> { if (revealListener != null) revealListener.accept(c, r); });

        boardAView.setOnCellRightClick((c, r) -> { if (flagListener != null) flagListener.accept(c, r); });
        boardBView.setOnCellRightClick((c, r) -> { if (flagListener != null) flagListener.accept(c, r); });

        JPanel boardsRow = new JPanel(new GridLayout(1, 2, 24, 0));
        boardsRow.setOpaque(false);
        boardsRow.add(boardAView);
        boardsRow.add(boardBView);

        center.add(boardsRow, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ===== Bottom: lives + score =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bottom.setBackground(new Color(6, 22, 14));
        bottom.setOpaque(true);

        lblLives = new JLabel();
        lblLives.setForeground(new Color(230, 255, 230));

        lblScore = new JLabel();
        lblScore.setForeground(new Color(230, 255, 230));
        lblScore.setFont(lblScore.getFont().deriveFont(Font.BOLD, 14f));

        bottom.add(lblLives, BorderLayout.WEST);
        bottom.add(lblScore, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        legend.setOpaque(true);
        legend.setBackground(new Color(6, 22, 14));
        legend.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 140, 90)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        legend.add(createLegendItem(new Color(255, 215, 0), "Q"));
        legend.add(createLegendItem(new Color(186, 85, 211), "S"));
        legend.add(createLegendItem(new Color(255, 99, 132), "Flag"));
        legend.add(createLegendItem(new Color(23, 35, 74), "Normal"));

        return legend;
    }

    private JComponent createLegendItem(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        p.setOpaque(false);

        JPanel square = new JPanel();
        square.setPreferredSize(new Dimension(14, 14));
        square.setBackground(color);
        square.setBorder(BorderFactory.createLineBorder(new Color(90, 140, 90)));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(240, 255, 240));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));

        p.add(square);
        p.add(lbl);
        return p;
    }

    public void refreshFromModel() {
        Player p1 = game.getPlayer1();
        Board b1 = game.getBoard1();
        Board b2 = game.getBoard2();

        boardAView.renderBoard(b1);
        boardBView.renderBoard(b2);

        if (game.getState() == GameState.OVER) {
            lblTurn.setText("Game over! Team lost all lives.");
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

        for (int i = 0; i < MAX_LIVES; i++) {
            hearts.append(i < lives ? "♥" : "♡");
        }
        lblLives.setText(hearts.toString());

        lblScore.setText("Team score: " + game.getTeamScore());
    }
}
