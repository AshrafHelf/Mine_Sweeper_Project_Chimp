package view;

import javax.swing.*;

import enums.Difficulty;
import enums.GameState;
import model.Board;
import model.Game;
import model.Player;

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

    // Background
    private final Image bg = UiAssets.background("game_bg.png");

    // HUD parts
    private JLabel lblTurnBig;
    private JLabel lblLives;
    private JLabel lblScore;
    private JTextArea eventFeed;

    private BoardView boardAView;
    private BoardView boardBView;

    // icons in HUD
    private final ImageIcon flagIcon = UiAssets.icon("flag.png", 18, 18);
    private final ImageIcon mineIcon = UiAssets.icon("mine.png", 18, 18);

    public GamePanel(Game game) {
        this.game = game;
        buildUI();
        refreshFromModel();
    }

    // --- Listener hooks for controller ---
    public void onBackToMenu(Runnable r) { this.backListener = r; }
    public void onRestart(Runnable r) { this.restartListener = r; }
    public void onCellReveal(BiConsumer<Integer, Integer> c) { this.revealListener = c; }
    public void onCellFlag(BiConsumer<Integer, Integer> c) { this.flagListener = c; }

    // Call this from controller when you show a message (optional but recommended)
    public void pushEvent(String text) {
        if (text == null || text.isBlank()) return;
        eventFeed.insert("• " + text.replace("\n", " | ") + "\n", 0);
        eventFeed.setCaretPosition(0);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // ===== Top bar =====
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        JButton btnBack = uiButton("Back to Menu");
        JButton btnRestart = uiButton("Restart");

        btnBack.addActionListener(e -> { if (backListener != null) backListener.run(); });
        btnRestart.addActionListener(e -> { if (restartListener != null) restartListener.run(); });

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBtns.setOpaque(false);
        leftBtns.add(btnBack);
        leftBtns.add(btnRestart);

        lblTurnBig = new JLabel(" ");
        lblTurnBig.setForeground(Color.WHITE);
        lblTurnBig.setFont(new Font("Serif", Font.BOLD, 22));
        lblTurnBig.setHorizontalAlignment(SwingConstants.CENTER);

        top.add(leftBtns, BorderLayout.WEST);
        top.add(lblTurnBig, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        // ===== Center: boards (center) + HUD (east) =====
        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        // Boards row (center)
        Difficulty diff = game.getDifficulty();
        Player p1 = game.getPlayer1();
        Player p2 = game.getPlayer2();

        boardAView = new BoardView(diff.cols, diff.rows, "Board A  —  " + p1.getName());
        boardBView = new BoardView(diff.cols, diff.rows, "Board B  —  " + p2.getName());

        // Hook clicks to controller
        boardAView.setOnCellClick((c, r) -> { if (revealListener != null) revealListener.accept(c, r); });
        boardBView.setOnCellClick((c, r) -> { if (revealListener != null) revealListener.accept(c, r); });

        boardAView.setOnCellRightClick((c, r) -> { if (flagListener != null) flagListener.accept(c, r); });
        boardBView.setOnCellRightClick((c, r) -> { if (flagListener != null) flagListener.accept(c, r); });

        JPanel boardsRow = new JPanel(new GridLayout(1, 2, 18, 0));
        boardsRow.setOpaque(false);
        boardsRow.add(wrapGlass(boardAView));
        boardsRow.add(wrapGlass(boardBView));

        centerWrap.add(boardsRow, BorderLayout.CENTER);

        // HUD panel (east)
        JPanel hud = buildHudPanel();
        centerWrap.add(hud, BorderLayout.EAST);

        add(centerWrap, BorderLayout.CENTER);

        // ===== Bottom (compact) =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

        lblLives = new JLabel();
        lblLives.setForeground(new Color(235, 235, 245));
        lblLives.setFont(lblLives.getFont().deriveFont(Font.BOLD, 13f));

        lblScore = new JLabel();
        lblScore.setForeground(new Color(235, 235, 245));
        lblScore.setFont(lblScore.getFont().deriveFont(Font.BOLD, 13f));

        bottom.add(lblLives, BorderLayout.WEST);
        bottom.add(lblScore, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildHudPanel() {
        JPanel hud = new JPanel();
        hud.setLayout(new BoxLayout(hud, BoxLayout.Y_AXIS));
        hud.setOpaque(false);
        hud.setPreferredSize(new Dimension(280, 10));
        hud.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        JPanel glass = new JPanel();
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));
        glass.setBackground(new Color(10, 14, 20, 170));
        glass.setOpaque(true);
        glass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 40)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("TEAM HUD");
        title.setForeground(new Color(240, 240, 250));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tip = new JLabel("<html><span style='color:#cfd6ff'>Left click</span>: reveal / activate<br/>"
                + "<span style='color:#cfd6ff'>Right click</span>: flag</html>");
        tip.setForeground(new Color(200, 205, 230));
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);

        glass.add(title);
        glass.add(Box.createVerticalStrut(8));
        glass.add(tip);
        glass.add(Box.createVerticalStrut(10));

        glass.add(sectionTitle("Legend"));
        glass.add(Box.createVerticalStrut(6));
        glass.add(legendRow(mineIcon, "Mine"));
        glass.add(legendRow(flagIcon, "Flag"));
        glass.add(legendTextRow("Q", new Color(195, 165, 40), "Question (pay cost + answer)"));
        glass.add(legendTextRow("S", new Color(120, 70, 160), "Surprise (pay cost + random)"));
        glass.add(legendTextRow("USED", new Color(90, 90, 90), "Special already used"));
        glass.add(Box.createVerticalStrut(12));

        glass.add(sectionTitle("Live Feed"));
        glass.add(Box.createVerticalStrut(6));

        eventFeed = new JTextArea(9, 20);
        eventFeed.setEditable(false);
        eventFeed.setFocusable(false);
        eventFeed.setLineWrap(true);
        eventFeed.setWrapStyleWord(true);
        eventFeed.setBackground(new Color(0, 0, 0, 0));
        eventFeed.setForeground(new Color(225, 225, 235));
        eventFeed.setFont(eventFeed.getFont().deriveFont(12f));
        eventFeed.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 35)));

        JScrollPane sp = new JScrollPane(eventFeed);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);

        glass.add(sp);

        hud.add(glass);
        return hud;
    }

    private JLabel sectionTitle(String t) {
        JLabel s = new JLabel(t.toUpperCase());
        s.setForeground(new Color(230, 230, 245));
        s.setFont(s.getFont().deriveFont(Font.BOLD, 12f));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        return s;
    }

    private JPanel legendRow(Icon icon, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        JLabel ic = new JLabel(icon);
        JLabel t = new JLabel(text);
        t.setForeground(new Color(220, 220, 235));
        row.add(ic);
        row.add(t);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JPanel legendTextRow(String tag, Color tagBg, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        JLabel pill = new JLabel(tag);
        pill.setOpaque(true);
        pill.setBackground(tagBg);
        pill.setForeground(tag.equals("Q") ? Color.BLACK : Color.WHITE);
        pill.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        pill.setFont(pill.getFont().deriveFont(Font.BOLD, 11f));

        JLabel t = new JLabel(text);
        t.setForeground(new Color(220, 220, 235));

        row.add(pill);
        row.add(t);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JButton uiButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(20, 30, 24));
        b.setBorder(BorderFactory.createLineBorder(new Color(60, 90, 70)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JComponent wrapGlass(JComponent child) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(new Color(0, 0, 0, 110));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 35)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        p.add(child, BorderLayout.CENTER);
        return p;
    }

    // --- Render background ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bg == null) {
            // fallback
            g.setColor(new Color(10, 14, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);

        // dark overlay so UI stays readable
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }

    // --- Redraw everything from model ---
    public void refreshFromModel() {
        Player p1 = game.getPlayer1();
        Board b1 = game.getBoard1();
        Board b2 = game.getBoard2();

        boardAView.renderBoard(b1);
        boardBView.renderBoard(b2);

        if (game.getState() == GameState.OVER) {
            lblTurnBig.setText("GAME OVER");
            boardAView.setBoardEnabled(false);
            boardBView.setBoardEnabled(false);
        } else {
            lblTurnBig.setText("TURN: " + game.getActivePlayer().getName());

            if (game.getActivePlayer() == p1) {
                boardAView.setBoardEnabled(true);
                boardBView.setBoardEnabled(false);
            } else {
                boardAView.setBoardEnabled(false);
                boardBView.setBoardEnabled(true);
            }
        }

        int lives = game.getTeamLives();
        StringBuilder hearts = new StringBuilder("Lives: ")
                .append(lives).append("/").append(MAX_LIVES).append("   ");

        for (int i = 0; i < MAX_LIVES; i++) {
            hearts.append(i < lives ? "♥" : "♡");
        }
        lblLives.setText(hearts.toString());

        lblScore.setText("Score: " + game.getTeamScore());
    }
}
