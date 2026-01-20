package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import enums.Difficulty;

import java.awt.*;
import java.net.URL;

public class MainMenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public interface StartListener {
        void start(Difficulty difficulty, String player1, String player2);
    }

    private StartListener onStart;
    private Runnable onHistory;
    private Runnable onQuestions;
    private Runnable onExit;

    // UI fields (so we can read values)
    private JComboBox<Difficulty> difficultyBox;
    private JTextField player1Field;
    private JTextField player2Field;
    private JTextArea diffInfo;

    // Background
    private Image bg;

    public MainMenuPanel() {
        loadBackground("/img/menu_bg.png"); // <-- put your jungle menu image here
        build();
    }

    public void setOnStart(StartListener l) { this.onStart = l; }
    public void setOnHistory(Runnable r) { this.onHistory = r; }
    public void setOnQuestions(Runnable r) { this.onQuestions = r; }
    public void setOnExit(Runnable r) { this.onExit = r; }

    private void loadBackground(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
            	System.out.println("Resource URL = " + getClass().getResource("/img/menu_bg.png"));

                System.err.println("❌ Menu background not found: " + path);
                bg = null;
                return;
            }
            bg = new ImageIcon(url).getImage();
        } catch (Exception e) {
            e.printStackTrace();
            bg = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw background image (scaled)
        if (bg != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();

            // cover-style scaling
            double imgW = bg.getWidth(null);
            double imgH = bg.getHeight(null);
            double scale = Math.max(w / imgW, h / imgH);

            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (w - drawW) / 2;
            int y = (h - drawH) / 2;

            g2.drawImage(bg, x, y, drawW, drawH, this);

            // dark overlay like Godot menu
            g2.setComposite(AlphaComposite.SrcOver.derive(0.55f));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);

            // subtle vignette
            g2.setComposite(AlphaComposite.SrcOver.derive(0.35f));
            g2.setPaint(new RadialGradientPaint(
                    new Point(w / 2, h / 2),
                    Math.max(w, h) * 0.65f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 220)}
            ));
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        } else {
            // fallback background if image missing
            g.setColor(new Color(8, 10, 16));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void build() {
        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel title = new JLabel("MINESWEEPER");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(245, 245, 245));
        title.setFont(new Font("Serif", Font.BOLD, 52));

        JLabel subtitle = new JLabel("JUNGLE CO-OP EDITION");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(200, 200, 200));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(26));

        // Buttons (center list style)
        JButton newGameBtn = menuButton("NEW GAME");
        JButton historyBtn  = menuButton("HISTORY");
        JButton questionsBtn= menuButton("QUESTIONS");
        JButton quitBtn     = menuButton("QUIT");

        content.add(newGameBtn);
        content.add(Box.createVerticalStrut(12));
        content.add(historyBtn);
        content.add(Box.createVerticalStrut(12));
        content.add(questionsBtn);
        content.add(Box.createVerticalStrut(12));
        content.add(quitBtn);
        content.add(Box.createVerticalStrut(26));

        // Settings block (like your 3rd pic)
        JPanel settings = new JPanel(new GridBagLayout());
        settings.setOpaque(false);
        settings.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel diffLbl = label("Difficulty:");
        JLabel p1Lbl   = label("Player 1:");
        JLabel p2Lbl   = label("Player 2:");

        difficultyBox = new JComboBox<>(Difficulty.values());
        styleCombo(difficultyBox);

        player1Field = new JTextField("Player 1", 14);
        player2Field = new JTextField("Player 2", 14);
        styleField(player1Field);
        styleField(player2Field);

        c.gridx = 0; c.gridy = 0; settings.add(diffLbl, c);
        c.gridx = 1; c.gridy = 0; settings.add(difficultyBox, c);

        c.gridx = 0; c.gridy = 1; settings.add(p1Lbl, c);
        c.gridx = 1; c.gridy = 1; settings.add(player1Field, c);

        c.gridx = 0; c.gridy = 2; settings.add(p2Lbl, c);
        c.gridx = 1; c.gridy = 2; settings.add(player2Field, c);

        content.add(settings);

        // Difficulty description (back again)
        diffInfo = new JTextArea(4, 28);
        diffInfo.setEditable(false);
        diffInfo.setLineWrap(true);
        diffInfo.setWrapStyleWord(true);
        diffInfo.setFocusable(false);
        diffInfo.setOpaque(true);
        diffInfo.setBackground(new Color(0, 0, 0, 120));
        diffInfo.setForeground(new Color(220, 220, 220));
        diffInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 35)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        diffInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(Box.createVerticalStrut(10));
        content.add(diffInfo);

        // Set initial & change listener
        difficultyBox.addActionListener(e -> updateDiffInfo());
        difficultyBox.setSelectedIndex(0);
        updateDiffInfo();

        // Root add to center
        GridBagConstraints rootC = new GridBagConstraints();
        rootC.gridx = 0;
        rootC.gridy = 0;
        rootC.weightx = 1;
        rootC.weighty = 1;
        rootC.anchor = GridBagConstraints.CENTER;
        add(content, rootC);

        // Actions
        newGameBtn.addActionListener(e -> {
            if (onStart == null) return;
            Difficulty diff = (Difficulty) difficultyBox.getSelectedItem();
            String p1 = safeName(player1Field.getText(), "Player 1");
            String p2 = safeName(player2Field.getText(), "Player 2");
            onStart.start(diff, p1, p2);
        });

        historyBtn.addActionListener(e -> { if (onHistory != null) onHistory.run(); });
        questionsBtn.addActionListener(e -> { if (onQuestions != null) onQuestions.run(); });
        quitBtn.addActionListener(e -> { if (onExit != null) onExit.run(); });
    }

    private void updateDiffInfo() {
        Difficulty diff = (Difficulty) difficultyBox.getSelectedItem();
        String text = describeDifficulty(diff);
        diffInfo.setText(text);
        difficultyBox.setToolTipText(text.replace("\n", " "));
    }

    private String safeName(String s, String fallback) {
        if (s == null) return fallback;
        s = s.trim();
        return s.isEmpty() ? fallback : s;
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(new Color(210, 210, 210));
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private JButton menuButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setBorderPainted(true);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(new Color(230, 230, 230));
        b.setFont(new Font("Serif", Font.PLAIN, 22));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60)),
                new EmptyBorder(10, 28, 10, 28)
        ));

        // simple hover effect
        b.addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (m.isRollover()) {
                b.setForeground(Color.WHITE);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 150)),
                        new EmptyBorder(10, 28, 10, 28)
                ));
            } else {
                b.setForeground(new Color(230, 230, 230));
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 60)),
                        new EmptyBorder(10, 28, 10, 28)
                ));
            }
        });

        return b;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(0, 0, 0, 140));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60)),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(new Color(0, 0, 0, 140));
        cb.setForeground(Color.WHITE);
        cb.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 60)));
    }

    private String describeDifficulty(Difficulty diff) {
        if (diff == null) return "";
        return switch (diff) {
            case EASY -> """
                    Easy:
                    • Smaller board, fewer mines.
                    • More lives, softer penalties.
                    • Fewer Q and S cells.""";
            case MEDIUM -> """
                    Medium:
                    • Balanced board and mines.
                    • Standard lives.
                    • More Q and S cells.""";
            case HARD -> """
                    Hard:
                    • Large board, many mines.
                    • Fewer lives, harsh penalties.
                    • Many Q and S cells (big rewards).""";
        };
    }
}
