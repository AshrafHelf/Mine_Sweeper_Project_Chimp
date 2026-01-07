package view;

import javax.swing.*;
import enums.Difficulty;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainMenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public interface StartListener {
        void start(Difficulty difficulty, String player1, String player2);
    }

    private StartListener onStart;
    private Runnable onHistory;
    private Runnable onQuestions;
    private Runnable onExit;
    private WoodMenuButton selectedBtn;



    public MainMenuPanel() {
        build();
    }

    public void setOnStart(StartListener l) { this.onStart = l; }
    public void setOnHistory(Runnable r) { this.onHistory = r; }
    public void setOnQuestions(Runnable r) { this.onQuestions = r; }
    public void setOnExit(Runnable r) { this.onExit = r; }

    // ===== Background =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Sky -> Jungle gradient
            GradientPaint gp = new GradientPaint(0, 0, new Color(120, 200, 235),
                                                 0, h, new Color(10, 60, 40));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // Canopy top
            g2.setColor(new Color(10, 45, 25, 220));
            int canopyY = (int) (h * 0.06);
            int canopyH = (int) (h * 0.20);
            for (int i = 0; i < 7; i++) {
                int cx = (int) (i * (w / 6.0));
                g2.fillOval(cx - 180, canopyY, 360, canopyH);
            }

            // Vines
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(70, 120, 60, 170));
            g2.drawLine((int)(w*0.16), 0, (int)(w*0.16), (int)(h*0.28));
            g2.drawLine((int)(w*0.30), 0, (int)(w*0.30), (int)(h*0.22));
            g2.drawLine((int)(w*0.86), 0, (int)(w*0.86), (int)(h*0.26));

            // Bottom silhouette
            g2.setColor(new Color(8, 25, 18, 230));
            g2.fillRect(0, (int) (h * 0.82), w, (int) (h * 0.18));

            // Soft fog band behind menu
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillRoundRect(30, (int)(h*0.18), 330, (int)(h*0.55), 35, 35);

        } finally {
            g2.dispose();
        }
    }

    private void build() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // ===== Left Menu =====
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(320, 10));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(45, 30, 30, 20));

        JLabel title = new JLabel("JUNGLE MADNESS");
        title.setForeground(new Color(245, 250, 235));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Two-player edition");
        sub.setForeground(new Color(210, 230, 210));
        sub.setFont(sub.getFont().deriveFont(12.5f));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);
        left.add(Box.createVerticalStrut(30));

        WoodMenuButton play = new WoodMenuButton("PLAY");
        WoodMenuButton ranking = new WoodMenuButton("RANKING");
        WoodMenuButton questions = new WoodMenuButton("QUESTIONS");
        WoodMenuButton settings = new WoodMenuButton("SETTINGS");
        WoodMenuButton exit = new WoodMenuButton("EXIT GAME");


        left.add(play);
        left.add(Box.createVerticalStrut(12));
        left.add(ranking);
        left.add(Box.createVerticalStrut(12));
        left.add(questions);
        left.add(Box.createVerticalStrut(12));
        left.add(settings);
        left.add(Box.createVerticalStrut(12));
        left.add(exit);

        add(left, BorderLayout.WEST);

        // default selection
        setSelected(play);

        // ===== Actions =====
        play.addActionListener(e -> { setSelected(play); showPlayDialog(); });


        ranking.addActionListener(e -> { setSelected(ranking); if (onHistory != null) onHistory.run(); });

        questions.addActionListener(e -> { setSelected(questions); if (onQuestions != null) onQuestions.run(); });


        settings.addActionListener(e -> { setSelected(settings); /* later */ });


        exit.addActionListener(e -> { setSelected(exit); if (onExit != null) onExit.run(); });

    }

   


    private void setSelected(WoodMenuButton b) {
        if (selectedBtn != null) selectedBtn.setSelectedState(false);
        selectedBtn = b;
        if (selectedBtn != null) selectedBtn.setSelectedState(true);
    }


    // ===== PLAY dialog (setup only appears when needed) =====
    private void showPlayDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Start Game", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        root.setBackground(new Color(10, 35, 25));

        JLabel header = new JLabel("LOCAL PLAY");
        header.setForeground(new Color(245, 250, 235));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));

        JLabel hint = new JLabel("Choose difficulty and enter players names");
        hint.setForeground(new Color(210, 230, 210));
        hint.setFont(hint.getFont().deriveFont(12.5f));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(header);
        top.add(Box.createVerticalStrut(4));
        top.add(hint);

        root.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());
        JTextField p1Field = new JTextField("Player 1", 14);
        JTextField p2Field = new JTextField("Player 2", 14);

        styleInput(difficultyBox);
        styleInput(p1Field);
        styleInput(p2Field);

        JLabel l1 = dialogLabel("Difficulty:");
        JLabel l2 = dialogLabel("Player 1:");
        JLabel l3 = dialogLabel("Player 2:");

        c.gridx = 0; c.gridy = 0;
        center.add(l1, c);
        c.gridx = 1;
        center.add(difficultyBox, c);

        c.gridx = 0; c.gridy = 1;
        center.add(l2, c);
        c.gridx = 1;
        center.add(p1Field, c);

        c.gridx = 0; c.gridy = 2;
        center.add(l3, c);
        c.gridx = 1;
        center.add(p2Field, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        JLabel md = new JLabel("MODE DETAILS");
        md.setForeground(new Color(220, 245, 220));
        md.setFont(md.getFont().deriveFont(Font.BOLD, 12.5f));
        center.add(md, c);

        c.gridy = 4;
        JTextArea info = new JTextArea(6, 28);
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFocusable(false);
        info.setBackground(new Color(5, 20, 14));
        info.setForeground(new Color(210, 240, 210));
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 140, 90)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        center.add(info, c);

        root.add(center, BorderLayout.CENTER);

        // buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton cancel = dialogButton("CANCEL");
        JButton start = dialogButton("START");

        bottom.add(cancel);
        bottom.add(start);

        root.add(bottom, BorderLayout.SOUTH);

        // logic
        difficultyBox.addActionListener(e -> {
            Difficulty diff = (Difficulty) difficultyBox.getSelectedItem();
            info.setText(describeDifficulty(diff));
        });

        difficultyBox.setSelectedIndex(0);
        info.setText(describeDifficulty((Difficulty) difficultyBox.getSelectedItem()));

        cancel.addActionListener(e -> dlg.dispose());

        start.addActionListener(e -> {
            if (onStart == null) return;

            Difficulty diff = (Difficulty) difficultyBox.getSelectedItem();
            String p1 = p1Field.getText().trim();
            String p2 = p2Field.getText().trim();
            if (p1.isEmpty()) p1 = "Player 1";
            if (p2.isEmpty()) p2 = "Player 2";

            dlg.dispose();
            onStart.start(diff, p1, p2);
        });

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(520, 430));
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private JLabel dialogLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(235, 250, 235));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12.5f));
        return l;
    }

    private void styleInput(JComponent comp) {
        comp.setFont(comp.getFont().deriveFont(13f));
        comp.setForeground(new Color(20, 35, 20));
        comp.setBackground(new Color(230, 245, 220));
        comp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 140, 90), 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        comp.setPreferredSize(new Dimension(220, 28));
    }

    private JButton dialogButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setForeground(new Color(40, 26, 10));
        b.setBackground(new Color(224, 184, 110));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 55, 25), 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13.5f));
        return b;
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
}
