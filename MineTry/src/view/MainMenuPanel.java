package view;

import javax.swing.*;

import model.Difficulty;

import java.awt.*;

public class MainMenuPanel extends JPanel {

    public interface StartListener {
        void start(Difficulty difficulty, String player1, String player2);
    }

    private StartListener onStart;
    private Runnable onHistory;
    private Runnable onQuestions;
    private Runnable onExit;

    public MainMenuPanel() {
        build();
    }

    public void setOnStart(StartListener l) {
        this.onStart = l;
    }

    public void setOnHistory(Runnable r) {
        this.onHistory = r;
    }

    public void setOnQuestions(Runnable r) {
        this.onQuestions = r;
    }

    public void setOnExit(Runnable r) {
        this.onExit = r;
    }

    private void build() {
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 14, 33)); // dark background

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        card.setBackground(new Color(18, 24, 52));

        // Title
        JLabel title = new JLabel("Minesweeper");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Two-player, questions & surprises edition");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(200, 200, 220));
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));

        // Form panel
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());
        JTextField player1Field = new JTextField("Player 1", 12);
        JTextField player2Field = new JTextField("Player 2", 12);

        JLabel diffLabel = new JLabel("Difficulty:");
        diffLabel.setForeground(Color.WHITE);
        JLabel p1Label = new JLabel("Player 1 name:");
        p1Label.setForeground(Color.WHITE);
        JLabel p2Label = new JLabel("Player 2 name:");
        p2Label.setForeground(Color.WHITE);

        c.gridx = 0; c.gridy = 0;
        form.add(diffLabel, c);
        c.gridx = 1;
        form.add(difficultyBox, c);

        c.gridx = 0; c.gridy = 1;
        form.add(p1Label, c);
        c.gridx = 1;
        form.add(player1Field, c);

        c.gridx = 0; c.gridy = 2;
        form.add(p2Label, c);
        c.gridx = 1;
        form.add(player2Field, c);

        card.add(form);
        card.add(Box.createVerticalStrut(10));

        // ===== Difficulty info panel =====
        JLabel infoTitle = new JLabel("Mode details");
        infoTitle.setForeground(new Color(210, 210, 235));
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoTitle.setFont(infoTitle.getFont().deriveFont(Font.BOLD, 13f));

        JTextArea diffInfo = new JTextArea(4, 24);
        diffInfo.setEditable(false);
        diffInfo.setLineWrap(true);
        diffInfo.setWrapStyleWord(true);
        diffInfo.setFocusable(false);
        diffInfo.setBackground(new Color(15, 20, 45));
        diffInfo.setForeground(new Color(210, 215, 240));
        diffInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 50, 90)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(infoTitle);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(diffInfo);

        card.add(infoPanel);
        card.add(Box.createVerticalStrut(20));

        // Buttons row
        JPanel buttonsRow = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonsRow.setOpaque(false);

        JButton startBtn = new JButton("Start Game");
        JButton historyBtn = new JButton("History");
        JButton questionsBtn = new JButton("Questions");
        JButton exitBtn = new JButton("Exit");

        for (JButton b : new JButton[]{startBtn, historyBtn, questionsBtn, exitBtn}) {
            b.setFocusPainted(false);
        }

        buttonsRow.add(startBtn);
        buttonsRow.add(historyBtn);
        buttonsRow.add(questionsBtn);
        buttonsRow.add(exitBtn);

        card.add(buttonsRow);

        // Add card to center
        GridBagConstraints rootC = new GridBagConstraints();
        rootC.gridx = 0;
        rootC.gridy = 0;
        rootC.weightx = 1;
        rootC.weighty = 1;
        rootC.fill = GridBagConstraints.NONE;
        add(card, rootC);

        // === Difficulty description logic ===
        difficultyBox.addActionListener(e -> {
            Difficulty diff = (Difficulty) difficultyBox.getSelectedItem();
            String text = describeDifficulty(diff);
            diffInfo.setText(text);
            difficultyBox.setToolTipText(text.replace("\n", " "));
        });

        // set initial info
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

        historyBtn.addActionListener(e -> {
            if (onHistory != null) onHistory.run();
        });

        questionsBtn.addActionListener(e -> {
            if (onQuestions != null) onQuestions.run();
        });

        exitBtn.addActionListener(e -> {
            if (onExit != null) onExit.run();
        });
    }

    private String describeDifficulty(Difficulty diff) {
        if (diff == null) return "";
        // You can change these numbers to exactly match the PDF.
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
