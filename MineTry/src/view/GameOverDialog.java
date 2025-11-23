package view;

import javax.swing.*;
import java.awt.*;

public class GameOverDialog extends JDialog {

    public GameOverDialog(JFrame owner, boolean won, int finalScore) {
        super(owner, "Game Over", true); // modal dialog
        buildUI(won, finalScore);
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(360, 200));
    }

    private void buildUI(boolean won, int finalScore) {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(12, 16, 35));

        // ===== Center content =====
        JPanel content = new JPanel();
        content.setBackground(new Color(12, 16, 35));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        String titleText = won ? "Team Victory!" : "Game Over";
        String bigText = won ? "You cleared the board 🎉" : "All lives lost 😢";

        JLabel title = new JLabel(titleText);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(won ? new Color(120, 220, 150)
                                : new Color(230, 120, 130));

        JLabel subtitle = new JLabel(bigText);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(210, 215, 240));
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        JLabel score = new JLabel("Final team score: " + finalScore);
        score.setAlignmentX(Component.CENTER_ALIGNMENT);
        score.setForeground(new Color(200, 205, 230));
        score.setFont(score.getFont().deriveFont(Font.BOLD, 14f));

        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(12));
        content.add(score);

        add(content, BorderLayout.CENTER);

        // ===== Bottom buttons =====
        JButton ok = new JButton("OK");
        ok.setFocusPainted(false);
        ok.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(12, 16, 35));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 12));
        bottom.add(ok);

        add(bottom, BorderLayout.SOUTH);
    }
}
