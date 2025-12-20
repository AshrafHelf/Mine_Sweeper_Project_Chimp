package view;

import model.Difficulty;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public interface StartListener {
        void start(Difficulty difficulty, String player1, String player2);
    }

    private StartListener onStart;
    private Runnable onHistory;
    private Runnable onQuestions;
    private Runnable onHelp;
    private Runnable onExit;
    private Runnable onSettings;

    


    private transient Image bgImg; // classpath image

    public MainMenuPanel() {
        setOpaque(false);
        bgImg = loadImage("/images/jungle.png"); // reuse your background
        build();
    }

    public void setOnStart(StartListener l) { this.onStart = l; }
    public void setOnHistory(Runnable r) { this.onHistory = r; }
    public void setOnQuestions(Runnable r) { this.onQuestions = r; }
    public void setOnHelp(Runnable r) { this.onHelp = r; }
    public void setOnExit(Runnable r) { this.onExit = r; }
    public void setOnSettings(Runnable r) {
        this.onSettings = r;
    }
    private void build() {
        setLayout(new BorderLayout());

        // Left menu
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 18));
        left.setPreferredSize(new Dimension(280, 10));

        JLabel title = new JLabel("Minesweeper");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 34f));

        JLabel sub = new JLabel("CHIMP — Two Player Edition");
        sub.setForeground(new Color(220, 230, 245));
        sub.setFont(sub.getFont().deriveFont(13f));

        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);
        left.add(Box.createVerticalStrut(22));

        JButton btnStart = menuButton("Start New Game");
        JButton btnHistory = menuButton("History");
        JButton btnQuestions = menuButton("Questions");
        JButton btnHelp = menuButton("Help");
        JButton btnSettings = menuButton("Settings");
        JButton btnExit = menuButton("Exit");

        left.add(btnStart);
        left.add(Box.createVerticalStrut(10));
        left.add(btnHistory);
        left.add(Box.createVerticalStrut(10));
        left.add(btnQuestions);
        left.add(Box.createVerticalStrut(10));
        left.add(btnHelp);
        left.add(Box.createVerticalStrut(10));
        left.add(btnSettings);

        left.add(Box.createVerticalGlue());
        left.add(btnExit);

        // Center “hero” area (kept minimal on purpose)
        JPanel hero = new JPanel(new BorderLayout());
        hero.setOpaque(false);
        hero.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));

        JLabel hint = new JLabel("<html><div style='width:520px;'>"
                + "<b>Tip:</b> Q-cells trigger questions. S-cells trigger surprises.<br>"
                + "Shared lives are converted to points at the end — final score may increase."
                + "</div></html>");
        hint.setForeground(new Color(230, 235, 250));
        hint.setFont(hint.getFont().deriveFont(14f));
        hint.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel hintCard = new JPanel(new BorderLayout());
        hintCard.setOpaque(false);
        hintCard.add(hint, BorderLayout.CENTER);
        hintCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 70)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        hero.add(hintCard, BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);
        add(hero, BorderLayout.CENTER);

        // Actions
        btnStart.addActionListener(e -> openNewGameDialog());
        btnHistory.addActionListener(e -> { if (onHistory != null) onHistory.run(); });
        btnQuestions.addActionListener(e -> { if (onQuestions != null) onQuestions.run(); });
        btnHelp.addActionListener(e -> { if (onHelp != null) onHelp.run(); });
        btnSettings.addActionListener(e -> { if (onSettings != null) onSettings.run(); });
 
        btnExit.addActionListener(e -> { if (onExit != null) onExit.run(); });
    }

    private void openNewGameDialog() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        NewGameDialog dlg = new NewGameDialog(owner);
        dlg.setVisible(true);

        if (!dlg.isConfirmed()) return;

        if (onStart != null) {
            onStart.start(dlg.getDifficulty(), dlg.getPlayer1(), dlg.getPlayer2());
        }
    }

    private JButton menuButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(20, 28, 60, 190));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(240, 42));
        b.setMaximumSize(new Dimension(240, 42));

        // Hover
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(34, 46, 92, 210));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(20, 28, 60, 190));
            }
        });

        return b;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Background image
        if (bgImg != null) {
            g2.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(10, 14, 33));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Dark overlay for readability
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Left gradient (Portal vibe)
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0, 0, 0, 180),
                320, 0, new Color(0, 0, 0, 0)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, 420, getHeight());

        g2.dispose();
        super.paintComponent(g);
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            return url == null ? null : new ImageIcon(url).getImage();
        } catch (Exception e) {
            return null;
        }
    }
}
