package view;

import enums.Difficulty;
import model.Game;
import model.GameRecord;
import model.QuestionService;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.plaf.basic.BasicMenuItemUI;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;

public class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    public interface NewGameListener {
        void start(Difficulty difficulty, String p1, String p2);
    }

    private NewGameListener newGameListener;

    private Runnable openQuestionsListener;
    private Runnable openHistoryListener;

    // Global hooks (Menu bar + per-screen Help/Settings buttons)
    private Runnable openSettingsListener;
    private Runnable openHelpListener;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final SplashPanel splash = new SplashPanel();
    private final HomePanel home = new HomePanel();

    private final ChimpSelectPanel chimpSelect = new ChimpSelectPanel();
    private final DifficultyPanel difficulty = new DifficultyPanel();

    // store chosen chimps/names for Play flow
    private String chosenChimpP1;
    private String chosenChimpP2;
    private String chosenNameP1;
    private String chosenNameP2;

    // avoid piling multiple game panels
    private GamePanel currentGamePanel;

    // --- Admin passcode
    private static final String ADMIN_PASSCODE = "1234";
    private boolean adminUnlocked = false;

    // --- Theme colors
    private static final Color BG_DARK = new Color(10, 12, 18);
    private static final Color BAR_BG  = new Color(14, 18, 28);
    private static final Color TEXT    = new Color(235, 235, 245);
    private static final Color MUTED   = new Color(160, 170, 190);
    private static final Color ACCENT  = new Color(76, 175, 80);

    public MainWindow() {
        super("Minesweeper – Jungle Co-op");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1200, 820);
        setLocationRelativeTo(null);

        setContentPane(root);
        root.setBackground(BG_DARK);

        root.add(splash, "splash");
        root.add(home, "home");
        root.add(chimpSelect, "chimps");
        root.add(difficulty, "difficulty");

        // Global Help/Settings (used by menu bar and can be reused by panels)
        this.openHelpListener = () -> {
            AudioManager.playSfx("message.wav");
            try {
                HelpDialog.open(this, 0);
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Help failed to open:\n" + ex.getMessage());
            }
        };

        this.openSettingsListener = () -> {
            AudioManager.playSfx("button.wav");
            try {
                SettingsDialog dlg = new SettingsDialog(this);
                dlg.setAlwaysOnTop(true);
                dlg.setVisible(true);
                dlg.toFront();
                dlg.requestFocus();
                dlg.setAlwaysOnTop(false);
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Settings failed to open:\n" + ex.getMessage());
            }
        };

        buildMenuBar();

        wireSplashPanel();
        wireHomePanel();
        wireChimpSelectPanel();
        wireDifficultyPanel();

        cards.show(root, "splash");
    }

    // -------------------------
    // Menu bar
    // -------------------------
    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        bar.setBackground(BAR_BG);
        bar.setOpaque(true);

        bar.setUI(new BasicMenuBarUI() {
            @Override public void paint(Graphics g, JComponent c) {
                g.setColor(BAR_BG);
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
            }
        });

        JMenu game = new JMenu("Game");
        JMenu tools = new JMenu("Tools");
        JMenu help = new JMenu("Help");

        styleMenu(game);
        styleMenu(tools);
        styleMenu(help);

        JMenuItem history = new JMenuItem("History");
        JMenuItem qadmin  = new JMenuItem("Questions");
        JMenuItem exit    = new JMenuItem("Exit");

        JMenuItem settings = new JMenuItem("Settings");

        JMenuItem howToPlay = new JMenuItem("How to Play");
        JMenuItem about = new JMenuItem("About");

        styleMenuItem(history);
        styleMenuItem(qadmin);
        styleMenuItem(exit);
        styleMenuItem(settings);
        styleMenuItem(howToPlay);
        styleMenuItem(about);

        history.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            if (openHistoryListener != null) openHistoryListener.run();
        });

        qadmin.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            if (openQuestionsListener != null) openQuestionsListener.run();
        });

        settings.addActionListener(e -> {
            if (openSettingsListener != null) openSettingsListener.run();
        });

        exit.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            dispose();
        });

        howToPlay.addActionListener(e -> {
            if (openHelpListener != null) openHelpListener.run();
        });

        about.addActionListener(e -> {
            AudioManager.playSfx("message.wav");
            showAboutDialog();
        });

        game.add(history);
        game.add(qadmin);
        game.addSeparator();
        game.add(exit);

        tools.add(settings);

        help.add(howToPlay);
        help.addSeparator();
        help.add(about);

        bar.add(game);
        bar.add(tools);
        bar.add(help);

        setJMenuBar(bar);

        game.setMnemonic(KeyEvent.VK_G);
        tools.setMnemonic(KeyEvent.VK_T);
        help.setMnemonic(KeyEvent.VK_H);
    }

    private void styleMenu(JMenu menu) {
        menu.setForeground(TEXT);
        menu.setFont(menu.getFont().deriveFont(Font.BOLD, 13f));
        menu.setOpaque(true);
        menu.setBackground(BAR_BG);
    }

    private void styleMenuItem(JMenuItem item) {
        item.setForeground(TEXT);
        item.setBackground(BAR_BG);
        item.setOpaque(true);
        item.setFont(item.getFont().deriveFont(13f));

        item.setUI(new BasicMenuItemUI() {
            @Override
            protected void paintBackground(Graphics g, JMenuItem c, Color bgColor) {
                ButtonModel model = c.getModel();
                g.setColor((model.isArmed() || model.isSelected()) ? new Color(24, 30, 44) : BAR_BG);
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
            }

            @Override
            protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                g.setFont(menuItem.getFont());
                g.setColor(menuItem.getModel().isArmed() ? ACCENT : TEXT);
                FontMetrics fm = g.getFontMetrics();
                int y = textRect.y + fm.getAscent();
                g.drawString(text, textRect.x, y);
            }
        });
    }

    // -------------------------
    // About dialog
    // -------------------------
    private void showAboutDialog() {
        JDialog dlg = new JDialog(this, "About Minesweeper", true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(BG_DARK);

        JPanel content = new JPanel();
        content.setBackground(BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Minesweeper – Jungle Co-op Edition");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitle = new JLabel("Two boards • shared lives • Q & S special cells");
        subtitle.setForeground(MUTED);

        JLabel authors = new JLabel("Developed by: Chimp Sweeper Team");
        authors.setForeground(MUTED);

        JLabel info = new JLabel("<html>" +
                "• Question cells (Q): activation cost + outcomes<br/>" +
                "• Surprise cells (S): activation cost + random good/bad<br/>" +
                "• Admin tools: question bank + game history" +
                "</html>");
        info.setForeground(new Color(200, 205, 230));

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(8));
        content.add(authors);
        content.add(Box.createVerticalStrut(12));
        content.add(info);

        dlg.add(content, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.setFocusPainted(false);
        ok.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            dlg.dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(BG_DARK);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 12));
        bottom.add(ok);

        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setMinimumSize(new Dimension(440, 260));
        dlg.setVisible(true);
    }

    // -------------------------
    // Wiring panels
    // -------------------------
    private void wireSplashPanel() {
        splash.setOnContinue(() -> cards.show(root, "home"));
    }

    private void wireHomePanel() {

        home.setOnPlay(() -> {
            AudioManager.playSfx("button.wav");
            chimpSelect.resetFlow();
            cards.show(root, "chimps");
        });

        home.setOnHistory(() -> {
            AudioManager.playSfx("button.wav");
            if (openHistoryListener != null) openHistoryListener.run();
        });

        home.setOnQuestions(() -> {
            AudioManager.playSfx("button.wav");
            if (openQuestionsListener != null) openQuestionsListener.run();
        });

        // ✅ FIX: wire settings/help so Home buttons actually open dialogs
        home.setOnSettings(() -> {
            if (openSettingsListener != null) openSettingsListener.run();
        });

        home.setOnHelp(() -> {
            if (openHelpListener != null) openHelpListener.run();
        });

        home.setOnExit(() -> {
            AudioManager.playSfx("button.wav");
            dispose();
        });
    }


    private void wireChimpSelectPanel() {
        chimpSelect.setOnBack(() -> {
            AudioManager.playSfx("button.wav");
            cards.show(root, "home");
        });

        // If ChimpSelectPanel has Settings/Help buttons, uncomment:
        // chimpSelect.setOnSettings(() -> { if (openSettingsListener != null) openSettingsListener.run(); });
        // chimpSelect.setOnHelp(() -> { if (openHelpListener != null) openHelpListener.run(); });

        chimpSelect.setOnDone((p1, p2, n1, n2) -> {
            AudioManager.playSfx("button.wav");
            chosenChimpP1 = p1;
            chosenChimpP2 = p2;
            chosenNameP1 = n1;
            chosenNameP2 = n2;
            cards.show(root, "difficulty");
        });
    }

    private void wireDifficultyPanel() {
        difficulty.setOnBack(() -> {
            AudioManager.playSfx("button.wav");
            cards.show(root, "chimps");
        });

        // If DifficultyPanel has Settings/Help buttons, uncomment:
        // difficulty.setOnHelp(() -> { if (openHelpListener != null) openHelpListener.run(); });

        difficulty.setOnDone(diff -> {
            AudioManager.playSfx("button.wav");
            if (newGameListener != null) {
                newGameListener.start(diff, chosenNameP1, chosenNameP2);
            }
        });
    }

    // -------------------------
    // Public callbacks used by controllers
    // -------------------------
    public void onNewGame(NewGameListener l) { this.newGameListener = l; }
    public void onOpenQuestions(Runnable l) { this.openQuestionsListener = l; }
    public void onOpenHistory(Runnable l) { this.openHistoryListener = l; }

    public void onOpenSettings(Runnable l) { this.openSettingsListener = l; }
    public void onOpenHelp(Runnable l) { this.openHelpListener = l; }

    public GamePanel showGame(Game game) {
        if (currentGamePanel != null) {
            root.remove(currentGamePanel);
            currentGamePanel = null;
        }

        ImageIcon p1 = loadChimpAvatar(chosenChimpP1, 34, 34);
        ImageIcon p2 = loadChimpAvatar(chosenChimpP2, 34, 34);

        currentGamePanel = new GamePanel(game, p1, p2);

        root.add(currentGamePanel, "game");
        cards.show(root, "game");
        revalidate();
        repaint();

        return currentGamePanel;
    }

    public void showMenu() {
        cards.show(root, "home");
    }

    // -------------------------
    // Admin dialogs
    // -------------------------
    public void showQuestionAdmin(QuestionService qService) {
        if (!requireAdminPasscode()) return;
        new QuestionAdminDialog(this, qService).setVisible(true);
    }

    public void showHistory(List<GameRecord> records) {
        new HistoryDialog(this, records).setVisible(true);
    }

    private boolean requireAdminPasscode() {
        if (adminUnlocked) return true;

        JPasswordField pf = new JPasswordField();
        pf.setEchoChar('•');

        int ok = JOptionPane.showConfirmDialog(
                this,
                pf,
                "Enter Admin Passcode",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (ok != JOptionPane.OK_OPTION) return false;

        String entered = new String(pf.getPassword()).trim();
        if (!ADMIN_PASSCODE.equals(entered)) {
            AudioManager.playSfx("error.wav");
            JOptionPane.showMessageDialog(this, "Wrong passcode.", "Access denied", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        AudioManager.playSfx("correct.wav");
        adminUnlocked = true;
        return true;
    }

    private ImageIcon loadChimpAvatar(String chimpId, int w, int h) {
        if (chimpId == null) return null;

        String file = switch (chimpId) {
            case "knight"  -> "chimps/chimp1.png";
            case "gamer"   -> "chimps/chimp2.png";
            case "ninja"   -> "chimps/chimp3.png";
            case "fairy"   -> "chimps/chimp4.png";
            case "hacker"  -> "chimps/chimp5.png";
            case "monday"  -> "chimps/chimp6.png";
            case "trainer" -> "chimps/chimp7.png";
            case "astro"   -> "chimps/chimp8.png";
            default -> null;
        };

        if (file == null) return null;

        URL url = getClass().getClassLoader().getResource("img/" + file);
        if (url == null) return null;

        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
