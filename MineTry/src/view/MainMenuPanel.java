package view;

import javax.swing.*;
import model.Difficulty;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class MainMenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public interface StartListener {
        void start(Difficulty difficulty, String player1, String player2);
    }

    private StartListener onStart;
    private Runnable onHistory;
    private Runnable onQuestions;
    private Runnable onExit;

    // Background image (classpath -> works in JAR)
    private transient Image bgImg;

    // Theme
    private static final Color INK_BG = new Color(10, 14, 33);
    private static final Color GLASS = new Color(10, 14, 33, 170);
    private static final Color GLASS_2 = new Color(18, 24, 52, 180);
    private static final Color BORDER = new Color(120, 170, 120, 140); // jungle accent

    // UI controls you use in listeners
    private JComboBox<Difficulty> difficultyBox;
    private JTextField player1Field;
    private JTextField player2Field;
    private JTextArea diffInfo;

    public MainMenuPanel() {
        setOpaque(false);
        // Put your menu background image here:
        bgImg = loadImage("/images/menu_bg.png");
        build();
    }

    public void setOnStart(StartListener l) { this.onStart = l; }
    public void setOnHistory(Runnable r) { this.onHistory = r; }
    public void setOnQuestions(Runnable r) { this.onQuestions = r; }
    public void setOnExit(Runnable r) { this.onExit = r; }

    private void build() {
        setLayout(new GridBagLayout());

        // ===== Main card =====
        JPanel card = new RoundedGlassPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        card.setOpaque(false);

        // Title
        JLabel title = new JLabel("Minesweeper");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 34f));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Two-player, questions & surprises edition");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(210, 215, 240));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        difficultyBox = new JComboBox<>(Difficulty.values());
        player1Field = new JTextField("Player 1", 14);
        player2Field = new JTextField("Player 2", 14);

        styleInput(difficultyBox);
        styleInput(player1Field);
        styleInput(player2Field);

        JLabel diffLabel = mkLabel("Difficulty:");
        JLabel p1Label = mkLabel("Player 1 name:");
        JLabel p2Label = mkLabel("Player 2 name:");

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(diffLabel, c);
        c.gridx = 1; c.weightx = 1;
        form.add(difficultyBox, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        form.add(p1Label, c);
        c.gridx = 1; c.weightx = 1;
        form.add(player1Field, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        form.add(p2Label, c);
        c.gridx = 1; c.weightx = 1;
        form.add(player2Field, c);

        card.add(form);
        card.add(Box.createVerticalStrut(10));

        // Mode details block
        JLabel infoTitle = new JLabel("Mode details");
        infoTitle.setForeground(new Color(235, 235, 255));
        infoTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoTitle.setFont(infoTitle.getFont().deriveFont(Font.BOLD, 13f));

        diffInfo = new JTextArea(4, 28);
        diffInfo.setEditable(false);
        diffInfo.setLineWrap(true);
        diffInfo.setWrapStyleWord(true);
        diffInfo.setFocusable(false);
        diffInfo.setOpaque(false);
        diffInfo.setForeground(new Color(210, 215, 240));
        diffInfo.setFont(diffInfo.getFont().deriveFont(13f));

        JPanel infoCard = new RoundedGlassPanelSmall();
        infoCard.setLayout(new BorderLayout());
        infoCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        infoCard.setOpaque(false);
        infoCard.add(diffInfo, BorderLayout.CENTER);

        JPanel infoWrap = new JPanel();
        infoWrap.setOpaque(false);
        infoWrap.setLayout(new BoxLayout(infoWrap, BoxLayout.Y_AXIS));
        infoWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoWrap.add(infoTitle);
        infoWrap.add(Box.createVerticalStrut(6));
        infoWrap.add(infoCard);

        card.add(infoWrap);
        card.add(Box.createVerticalStrut(18));

        // Buttons grid
        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);

        JButton startBtn = new JButton("Start Game");
        JButton historyBtn = new JButton("History");
        JButton questionsBtn = new JButton("Questions");
        JButton exitBtn = new JButton("Exit");

        stylePrimaryButton(startBtn);
        styleButton(historyBtn);
        styleButton(questionsBtn);
        styleDangerButton(exitBtn);

        buttons.add(startBtn);
        buttons.add(historyBtn);
        buttons.add(questionsBtn);
        buttons.add(exitBtn);

        card.add(buttons);

        // Add card centered
        GridBagConstraints root = new GridBagConstraints();
        root.gridx = 0;
        root.gridy = 0;
        root.insets = new Insets(20, 20, 20, 20);
        add(card, root);

        // Difficulty description logic
        difficultyBox.addActionListener(e -> {
            Difficulty diff = (Difficulty) difficultyBox.getSelectedItem();
            String text = describeDifficulty(diff);
            diffInfo.setText(text);
            difficultyBox.setToolTipText(text.replace("\n", " "));
        });

        difficultyBox.setSelectedIndex(0);
        diffInfo.setText(describeDifficulty((Difficulty) difficultyBox.getSelectedItem()));
        difficultyBox.setToolTipText(diffInfo.getText().replace("\n", " "));

        // Actions
        startBtn.addActionListener(e -> {
            if (onStart != null) {
                Difficulty diff = (Difficulty) difficultyBox.getSelectedItem();
                String p1 = player1Field.getText().trim();
                String p2 = player2Field.getText().trim();
                if (p1.isEmpty()) p1 = "Player 1";
                if (p2.isEmpty()) p2 = "Player 2";
                onStart.start(diff, p1, p2);
            }
        });

        historyBtn.addActionListener(e -> { if (onHistory != null) onHistory.run(); });
        questionsBtn.addActionListener(e -> { if (onQuestions != null) onQuestions.run(); });
        exitBtn.addActionListener(e -> { if (onExit != null) onExit.run(); });
    }

    // ===== Painting: background + overlay =====
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // base fill
        g2.setColor(INK_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // bg image
        if (bgImg != null) {
            g2.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
        }

        // overlay for readability
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // vignette
        g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 30),
                0, getHeight(), new Color(0, 0, 0, 170)));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
        super.paintComponent(g);
    }

    // ===== Styling helpers =====
    private JLabel mkLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(235, 235, 255));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12.5f));
        return l;
    }

    private void styleInput(JComponent c) {
        c.setFont(c.getFont().deriveFont(13f));
        c.setForeground(Color.WHITE);
        c.setBackground(new Color(15, 20, 45));
        c.setOpaque(true);
        if (c instanceof JTextField tf) {
            tf.setCaretColor(Color.WHITE);
        }
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 60, 70), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void styleButton(JButton b) {
        baseButton(b);
        Color normal = new Color(27, 36, 78);
        Color hover = new Color(35, 50, 110);
        applyHover(b, normal, hover);
    }

    private void stylePrimaryButton(JButton b) {
        baseButton(b);
        Color normal = new Color(46, 125, 50);
        Color hover = new Color(56, 142, 60);
        applyHover(b, normal, hover);
    }

    private void styleDangerButton(JButton b) {
        baseButton(b);
        Color normal = new Color(120, 30, 30);
        Color hover = new Color(150, 40, 40);
        applyHover(b, normal, hover);
    }

    private void baseButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13.5f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 36));
    }

    private void applyHover(JButton b, Color normal, Color hover) {
        b.setBackground(normal);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(normal); }
        });
    }

    private String describeDifficulty(Difficulty diff) {
        if (diff == null) return "";
        return switch (diff) {
            case EASY -> """
                    Easy game:
                    • Smaller board with fewer mines.
                    • Few question (Q) and surprise (S) cells.
                    • More starting lives and softer penalties.""";
            case MEDIUM -> """
                    Medium game:
                    • Medium board and more mines.
                    • More Q and S cells.
                    • Standard starting lives and moderate penalties.""";
            case HARD -> """
                    Hard game:
                    • Larger board with many mines.
                    • Many Q and S cells.
                    • Fewer starting lives, harsh penalties and big rewards.""";
        };
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

    // ===== Custom panels =====

    private static class RoundedGlassPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        public RoundedGlassPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 24;

            // shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 12, arc, arc);

            Shape rr = new RoundRectangle2D.Float(0, 0, getWidth() - 10, getHeight() - 10, arc, arc);

            // glass
            g2.setColor(GLASS);
            g2.fill(rr);

            // border accent
            g2.setColor(BORDER);
            g2.draw(rr);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedGlassPanelSmall extends JPanel {
        private static final long serialVersionUID = 1L;
        public RoundedGlassPanelSmall() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;
            Shape rr = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            g2.setColor(GLASS_2);
            g2.fill(rr);

            g2.setColor(new Color(120, 170, 120, 120));
            g2.draw(rr);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
