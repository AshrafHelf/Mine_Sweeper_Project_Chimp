package view;

import javax.swing.*;

import enums.Difficulty;
import model.Game;
import model.GameRecord;
import model.QuestionService;

import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    /**
	 * 
	 */
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

        setContentPane(root);
        root.add(menu, "menu");

        buildMenuBar();
        wireMenuPanel();

        cards.show(root, "menu");
    }

    // --- Top menu bar ---

    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu game = new JMenu("Game");

        JMenuItem scores = new JMenuItem("History");
        scores.addActionListener(e -> {
            if (openHistoryListener != null) openHistoryListener.run();
        });

        JMenuItem qadmin = new JMenuItem("Questions");
        qadmin.addActionListener(e -> {
            if (openQuestionsListener != null) openQuestionsListener.run();
        });

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());

        game.add(scores);
        game.add(qadmin);
        game.addSeparator();
        game.add(exit);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> showAboutDialog());
        help.add(about);

        bar.add(game);
        bar.add(help);

        setJMenuBar(bar);
    }

    private void showAboutDialog() {
        JDialog dlg = new JDialog(this, "About Minesweeper", true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(new Color(12, 16, 35));

        JPanel content = new JPanel();
        content.setBackground(new Color(12, 16, 35));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Minesweeper – Two Player Edition");
        title.setForeground(new Color(235, 235, 255));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitle = new JLabel("Project for Minesweeper with questions and surprises.");
        subtitle.setForeground(new Color(190, 195, 220));

        JLabel authors = new JLabel("Developed by: Chimp");
        authors.setForeground(new Color(170, 175, 205));

        JLabel info = new JLabel("<html>" +
                "• Two boards, cooperative play.<br/>" +
                "• Shared lives and team score.<br/>" +
                "• Question cells (Q) and surprise cells (S).<br/>" +
                "• Admin screens for questions and game history." +
                "</html>");
        info.setForeground(new Color(200, 205, 230));

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        authors.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(4));
        content.add(authors);
        content.add(Box.createVerticalStrut(12));
        content.add(info);

        dlg.add(content, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.setFocusPainted(false);
        ok.addActionListener(e -> dlg.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(12, 16, 35));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 12));
        bottom.add(ok);

        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setMinimumSize(new Dimension(420, 250));
        dlg.setVisible(true);
    }

    // --- Wiring main menu panel to callbacks ---

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

    // --- Public callbacks used by controllers ---

    public void onNewGame(NewGameListener l) {
        this.newGameListener = l;
    }

    public void onOpenQuestions(Runnable l) {
        this.openQuestionsListener = l;
    }

    public void onOpenHistory(Runnable l) {
        this.openHistoryListener = l;
    }

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
