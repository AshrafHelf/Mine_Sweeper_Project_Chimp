package view;

import javax.swing.*;

import enums.Difficulty;
import model.Game;
import model.GameRecord;
import model.QuestionService;

import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    public interface NewGameListener {
        void start(Difficulty difficulty, String p1, String p2);
    }

    private NewGameListener newGameListener;
    private Runnable openQuestionsListener;
    private Runnable openHistoryListener;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final MainMenuPanel menu = new MainMenuPanel();

    public MainWindow() {
        super("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationByPlatform(true);

        // remove top menu bar
        setJMenuBar(null);

        setContentPane(root);
        root.add(menu, "menu");

        wireMenuPanel();

        cards.show(root, "menu");
    }

    private void wireMenuPanel() {
        menu.setOnStart((diff, p1, p2) -> {
            if (newGameListener != null) newGameListener.start(diff, p1, p2);
        });

        menu.setOnHistory(() -> {
            if (openHistoryListener != null) openHistoryListener.run();
        });

        menu.setOnQuestions(() -> {
            if (openQuestionsListener != null) openQuestionsListener.run();
        });

        menu.setOnExit(this::dispose);
    }

    public void onNewGame(NewGameListener l) { this.newGameListener = l; }
    public void onOpenQuestions(Runnable l) { this.openQuestionsListener = l; }
    public void onOpenHistory(Runnable l) { this.openHistoryListener = l; }

    public GamePanel showGame(Game game) {
        GamePanel gamePanel = new GamePanel(game);
        root.add(gamePanel, "game");
        cards.show(root, "game");
        return gamePanel;
    }

    public void showQuestionAdmin(QuestionService qService) {
        QuestionAdminDialog dialog = new QuestionAdminDialog(this, qService);
        dialog.setVisible(true);
    }

    public void showHistory(List<GameRecord> records) {
        HistoryDialog dialog = new HistoryDialog(this, records);
        dialog.setVisible(true);
    }

    public void showMenu() {
        cards.show(root, "menu");
    }
}
