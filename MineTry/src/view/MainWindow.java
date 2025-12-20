package view;

import javax.swing.*;

import model.Difficulty;
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
    private BananaRainOverlay bananaOverlay;


    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final MainMenuPanel menu = new MainMenuPanel();

    
    public MainWindow() {
        super("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationByPlatform(true);

        // ✅ Remove top menu bar ("Game | Help")
        setJMenuBar(null);

        setContentPane(root);
        root.add(menu, "menu");
        
     // Add a glass pane overlay for win animations
        Image banana = new ImageIcon(getClass().getResource("/images/banana.png")).getImage();
        BananaRainOverlay overlay = new BananaRainOverlay(banana);
        setGlassPane(overlay);
        overlay.setVisible(false);


        wireMenuPanel();

        cards.show(root, "menu");
    }

    // =========================
    // Main menu panel callbacks
    // =========================

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

        menu.setOnHelp(this::showAboutDialog);

        // ✅ هذا السطر هو الناقص
        menu.setOnSettings(this::showSettingsDialog);

        menu.setOnExit(this::dispose);
    }


    // =========================
    // Public callbacks (controllers)
    // =========================

    public void onNewGame(NewGameListener l) {
        this.newGameListener = l;
    }

    public void onOpenQuestions(Runnable l) {
        this.openQuestionsListener = l;
    }

    public void onOpenHistory(Runnable l) {
        this.openHistoryListener = l;
    }

    // =========================
    // Screen navigation
    // =========================

    public GamePanel showGame(Game game) {
        SoundManager.stopMusic(); // stop menu theme
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
        SoundManager.playMusicLoop("/music/menu_theme.wav");
        SoundManager.setMusicVolume(0.35f);
        cards.show(root, "menu");
        
    }


    // =========================
    // About / Help dialog
    // =========================
    
    private void showSettingsDialog() {
        JDialog dlg = new JDialog(this, "Settings", true);
        dlg.setLayout(new java.awt.BorderLayout());
        dlg.getContentPane().setBackground(new java.awt.Color(12, 16, 35));

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(16,16,16,16));
        p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS));

        JCheckBox mute = new JCheckBox("Mute");
        mute.setOpaque(false);
        mute.setForeground(java.awt.Color.WHITE);
        mute.setSelected(SoundManager.isMusicMuted());
        mute.addActionListener(e -> SoundManager.setMusicMuted(mute.isSelected()));

        JLabel volLbl = new JLabel("Music volume");
        volLbl.setForeground(java.awt.Color.WHITE);

        JSlider vol = new JSlider(0, 100, (int)(SoundManager.getMusicVolume()*100));
        vol.addChangeListener(e -> SoundManager.setMusicVolume(vol.getValue()/100f));

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> dlg.dispose());

        p.add(mute);
        p.add(javax.swing.Box.createVerticalStrut(10));
        p.add(volLbl);
        p.add(vol);
        p.add(javax.swing.Box.createVerticalStrut(14));
        p.add(ok);

        dlg.add(p, java.awt.BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
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

        JLabel authors = new JLabel("Developed by: CHIMP");
        authors.setForeground(new Color(170, 175, 205));

        JLabel info = new JLabel("<html>" +
                "• Two boards, cooperative play.<br/>" +
                "• Shared lives and team score.<br/>" +
                "• Question cells (Q) and surprise cells (S).<br/>" +
                "• Admin screens for questions and game history.<br/>" +
                "• Remaining lives are converted to points at the end." +
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
        dlg.setMinimumSize(new Dimension(420, 260));
        dlg.setVisible(true);
    }
    
    
}
