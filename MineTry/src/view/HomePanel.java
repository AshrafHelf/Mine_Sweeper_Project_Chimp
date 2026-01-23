package view;

import javax.swing.*;
import java.awt.*;

import view.theme.UIFactory;

public class HomePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private Runnable onPlay;
    private Runnable onHistory;
    private Runnable onQuestions;
    private Runnable onExit;

    public HomePanel() {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(new Color(12, 18, 16)); // jungle dark fallback

        JPanel card = UIFactory.woodCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        JLabel title = UIFactory.title("CHIMP SWEEPER");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = UIFactory.subtitle("Home Base");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton play = UIFactory.woodButton("PLAY 🍌");
        JButton history = UIFactory.woodButton("HISTORY");
        JButton questions = UIFactory.woodButton("QUESTIONS");
        JButton exit = UIFactory.woodButton("EXIT");

        play.setAlignmentX(Component.CENTER_ALIGNMENT);
        history.setAlignmentX(Component.CENTER_ALIGNMENT);
        questions.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(18));
        card.add(play);
        card.add(Box.createVerticalStrut(10));
        card.add(history);
        card.add(Box.createVerticalStrut(10));
        card.add(questions);
        card.add(Box.createVerticalStrut(10));
        card.add(exit);

        add(card);

        play.addActionListener(e -> { if (onPlay != null) onPlay.run(); });
        history.addActionListener(e -> { if (onHistory != null) onHistory.run(); });
        questions.addActionListener(e -> { if (onQuestions != null) onQuestions.run(); });
        exit.addActionListener(e -> { if (onExit != null) onExit.run(); });
    }

    public void setOnPlay(Runnable r) { this.onPlay = r; }
    public void setOnHistory(Runnable r) { this.onHistory = r; }
    public void setOnQuestions(Runnable r) { this.onQuestions = r; }
    public void setOnExit(Runnable r) { this.onExit = r; }
}
