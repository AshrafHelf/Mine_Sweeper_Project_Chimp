package view;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

import enums.Difficulty;
import model.Game;
import model.GameRecord;
import model.QuestionService;

import java.awt.*;
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

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final SplashPanel splash = new SplashPanel();
    private final HomePanel home = new HomePanel();

    // NOTE: "setup" screen is now optional/unused because Play -> chimps -> difficulty -> game
    private final MainMenuPanel menu = new MainMenuPanel();

    private final ChimpSelectPanel chimpSelect = new ChimpSelectPanel();
    private final DifficultyPanel difficulty = new DifficultyPanel();

    // store chosen chimps (optional for later)
    private String chosenChimpP1;
    private String chosenChimpP2;
    private String chosenNameP1;
    private String chosenNameP2;

    // --- Admin passcode (simple version)
    private static final String ADMIN_PASSCODE = "1234";
    private boolean adminUnlocked = false;

    // --- Theme colors
    private static final Color BG_DARK = new Color(10, 12, 18);
    private static final Color BAR_BG  = new Color(14, 18, 28);
    private static final Color TEXT    = new Color(235, 235, 245);
    private static final Color MUTED   = new Color(160, 170, 190);
    private static final Color ACCENT  = new Color(76, 175, 80); // jungle green

    public MainWindow() {
        super("Minesweeper – Jungle Co-op");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1200, 820);          // bigger default
        setLocationRelativeTo(null); // center

        setContentPane(root);
        root.setBackground(BG_DARK);

   
        // Cards
        root.add(splash, "splash");
        root.add(home, "home");

        // keep this if you still want it accessible from somewhere (not used by Play now)
        root.add(menu, "setup");

        root.add(chimpSelect, "chimps");
        root.add(difficulty, "difficulty");

        buildMenuBar();

        // Wiring (IMPORTANT)
        wireSplashPanel();
        wireHomePanel();
        wireMenuPanel();          // harmless even if setup not used
        wireChimpSelectPanel();   // ✅ FIX: was missing (buttons were “not working”)
        wireDifficultyPanel();    // ✅ FIX: was missing

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

        // Force a flat paint for the bar background
        bar.setUI(new BasicMenuBarUI() {
            @Override public void paint(Graphics g, JComponent c) {
                g.setColor(BAR_BG);
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
            }
        });

        JMenu game = new JMenu("Game");
        JMenu help = new JMenu("Help");

        styleMenu(game);
        styleMenu(help);

        JMenuItem scores = new JMenuItem("History");
        JMenuItem qadmin = new JMenuItem("Questions");
        JMenuItem exit   = new JMenuItem("Exit");

        styleMenuItem(scores);
        styleMenuItem(qadmin);
        styleMenuItem(exit);

        scores.addActionListener(e -> {
            if (openHistoryListener != null) openHistoryListener.run();
        });

        qadmin.addActionListener(e -> {
            if (openQuestionsListener != null) openQuestionsListener.run();
        });

        exit.addActionListener(e -> dispose());

        game.add(scores);
        game.add(qadmin);
        game.addSeparator();
        game.add(exit);

        JMenuItem about = new JMenuItem("About");
        styleMenuItem(about);
        about.addActionListener(e -> showAboutDialog());
        help.add(about);

        bar.add(game);
        bar.add(help);

        setJMenuBar(bar);
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
                if (model.isArmed() || model.isSelected()) {
                    g.setColor(new Color(24, 30, 44));
                } else {
                    g.setColor(BAR_BG);
                }
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

        JLabel authors = new JLabel("Developed by: Chimp");
        authors.setForeground(MUTED);

        JLabel info = new JLabel("<html>" +
                "• Question cells (Q): activation cost + outcomes<br/>" +
                "• Surprise cells (S): activation cost + random good/bad<br/>" +
                "• Admin: question bank + game history" +
                "</html>");
        info.setForeground(new Color(200, 205, 230));

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        authors.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        ok.addActionListener(e -> dlg.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(BG_DARK);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 12));
        bottom.add(ok);

        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setMinimumSize(new Dimension(420, 250));
        dlg.setVisible(true);
    }

    // -------------------------
    // Wiring panels
    // -------------------------

    private void wireSplashPanel() {
        splash.setOnContinue(() -> cards.show(root, "home"));
    }

    private void wireHomePanel() {
        // ✅ Play goes to new flow
        home.setOnPlay(() -> {
            chimpSelect.resetFlow();
            cards.show(root, "chimps");
        });

        home.setOnHistory(() -> {
            if (openHistoryListener != null) openHistoryListener.run();
        });

        home.setOnQuestions(() -> {
            if (openQuestionsListener != null) openQuestionsListener.run();
        });

        home.setOnExit(this::dispose);
    }

    // (Optional old setup screen wiring — safe to keep)
    private void wireMenuPanel() {
        menu.setOnStart((diff, p1, p2) -> {
            if (newGameListener != null) newGameListener.start(diff, p1, p2);
        });

        menu.setOnBack(() -> cards.show(root, "home"));
    }

    // ✅ This was missing before (caused BACK/SELECT to “not work”)
    private void wireChimpSelectPanel() {
        chimpSelect.setOnBack(() -> cards.show(root, "home"));

        chimpSelect.setOnDone((p1, p2, n1, n2) -> {
            chosenChimpP1 = p1;
            chosenChimpP2 = p2;
            chosenNameP1 = n1;
            chosenNameP2 = n2;
            cards.show(root, "difficulty");
        });
    }


    private void wireDifficultyPanel() {
        difficulty.setOnBack(() -> cards.show(root, "chimps"));

        difficulty.setOnDone(diff -> {
            if (newGameListener != null) {
                newGameListener.start(diff, chosenNameP1, chosenNameP2);
            }
        });
    }

    // -------------------------
    // Public callbacks used by controllers
    // -------------------------

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
        ImageIcon p1 = loadChimpAvatar(chosenChimpP1, 34, 34);
        ImageIcon p2 = loadChimpAvatar(chosenChimpP2, 34, 34);

        GamePanel gamePanel = new GamePanel(game, p1, p2);
        root.add(gamePanel, "game");
        cards.show(root, "game");
        revalidate();
        repaint();
        return gamePanel;
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



    // If you still call this from a controller, it returns to Home (clean)
    public void showMenu() {
        cards.show(root, "home");
    }

    // -------------------------
    // Admin dialogs
    // -------------------------

    public void showQuestionAdmin(QuestionService qService) {
        if (!requireAdminPasscode()) return;
        QuestionAdminDialog dialog = new QuestionAdminDialog(this, qService);
        dialog.setVisible(true);
    }

    public void showHistory(List<GameRecord> records) {
        HistoryDialog dialog = new HistoryDialog(this, records);
        dialog.setVisible(true);
    }

    // -------------------------
    // Admin passcode
    // -------------------------

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
            JOptionPane.showMessageDialog(
                    this,
                    "Wrong passcode.",
                    "Access denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        adminUnlocked = true;
        return true;
    }

    // -------------------------
    // Resource helpers
    // -------------------------

    @SuppressWarnings("unused")
    private void setAppIcon(String fileName) {
        URL url = MainWindow.class.getClassLoader().getResource("img/" + fileName);
        if (url != null) {
            setIconImage(new ImageIcon(url).getImage());
        } else {
            System.out.println("⚠ App icon not found: img/" + fileName);
        }
    }
}
