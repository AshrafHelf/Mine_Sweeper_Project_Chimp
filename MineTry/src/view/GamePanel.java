package view;

import javax.swing.*;

import model.Board;
import model.Difficulty;
import model.Game;
import model.GameState;
import model.Player;

import java.awt.*;
import java.util.function.BiConsumer;

public class GamePanel extends JPanel {

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

    // --- Listener hooks for controller ---

    public void onBackToMenu(Runnable r) {
        this.backListener = r;
    }

    public void onRestart(Runnable r) {
        this.restartListener = r;
    }

    public void onCellReveal(BiConsumer<Integer, Integer> c) {
        this.revealListener = c;
    }

    public void onCellFlag(BiConsumer<Integer, Integer> c) {
        this.flagListener = c;
    }

    public void requestBack() {
        if (backListener != null) backListener.run();
    }

    public void requestRestart() {
        if (restartListener != null) restartListener.run();
    }

    // --- Build UI ---

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 14, 33));

        // ===== Top toolbar =====
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(new Color(13, 19, 42));

        JButton btnBack = new JButton("Back");
        JButton btnRestart = new JButton("Restart");

        btnBack.setFocusPainted(false);
        btnRestart.setFocusPainted(false);

        btnBack.addActionListener(e -> {
            if (backListener != null) backListener.run();
        });
        btnRestart.addActionListener(e -> {
            if (restartListener != null) restartListener.run();
        });

        toolbar.add(btnBack);
        toolbar.add(btnRestart);
        toolbar.addSeparator();

        lblTurn = new JLabel();
        lblTurn.setForeground(Color.WHITE);
        toolbar.add(lblTurn);

        add(toolbar, BorderLayout.NORTH);

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

        // Left-click handlers (reveal)
        boardAView.setOnCellClick((c, r) -> {
            if (revealListener != null) revealListener.accept(c, r);
        });
        boardBView.setOnCellClick((c, r) -> {
            if (revealListener != null) revealListener.accept(c, r);
        });

        // Right-click handlers (flag)
        boardAView.setOnCellRightClick((c, r) -> {
            if (flagListener != null) flagListener.accept(c, r);
        });
        boardBView.setOnCellRightClick((c, r) -> {
            if (flagListener != null) flagListener.accept(c, r);
        });

        JPanel boardsRow = new JPanel(new GridLayout(1, 2, 24, 0));
        boardsRow.setOpaque(false);
        boardsRow.add(boardAView);
        boardsRow.add(boardBView);

        center.add(boardsRow, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ===== Bottom: team lives + team score =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        bottom.setBackground(new Color(9, 12, 28));

        lblLives = new JLabel();
        lblLives.setForeground(Color.WHITE);

        lblScore = new JLabel();
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(lblScore.getFont().deriveFont(Font.BOLD, 14f));

        bottom.add(lblLives, BorderLayout.WEST);
        bottom.add(lblScore, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createLegendPanel() {
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 4));

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
        square.setBorder(BorderFactory.createLineBorder(new Color(35, 45, 90)));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(lbl.getFont().deriveFont(12f));

        p.add(square);
        p.add(lbl);
        return p;
    }

    // --- Redraw everything from model ---

    public void refreshFromModel() {
        Player p1 = game.getPlayer1();
        Player p2 = game.getPlayer2();
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

        // hearts-style lives
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
