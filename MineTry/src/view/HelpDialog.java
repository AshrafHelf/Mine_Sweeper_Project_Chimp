package view;

import view.theme.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class HelpDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final CardLayout pages = new CardLayout();
    private final JPanel pageRoot = new JPanel(pages);

    private int pageIndex = 0;
    private final int pageCount = 4;

    private final JLabel lblStep = new JLabel("", SwingConstants.CENTER);
    private final JButton btnPrev = UIFactory.glassButton("← Back");
    private final JButton btnNext = UIFactory.glassButton("Next →");

    public static void open(Window owner) { open(owner, 0); }

    public static void open(Window owner, int startPage) {
        HelpDialog dlg = new HelpDialog(owner, startPage);
        dlg.setVisible(true);
    }

    public HelpDialog(Window owner, int startPage) {
        super(owner, "Help", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel glass = UIFactory.glassCard();
        glass.setLayout(new BorderLayout(12, 12));
        glass.setBorder(new EmptyBorder(16, 18, 14, 18));

        root.add(glass, BorderLayout.CENTER);
        setContentPane(root);


        // header
        JLabel title = UIFactory.dialogTitle("HOW TO PLAY");
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = UIFactory.dialogSubtitle("Quick guide • Special cells • Lives & score");
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        glass.add(header, BorderLayout.NORTH);

        // pages
        pageRoot.setOpaque(false);
        pageRoot.add(pageOverview(), "p0");
        pageRoot.add(pageControls(), "p1");
        pageRoot.add(pageSpecialCells(), "p2");
        pageRoot.add(pageScoring(), "p3");

        JPanel pageWrap = new JPanel(new BorderLayout());
        pageWrap.setOpaque(false);
        pageWrap.add(pageRoot, BorderLayout.CENTER);

        glass.add(pageWrap, BorderLayout.CENTER);

        // footer
        lblStep.setForeground(new Color(235, 235, 235, 170));
        lblStep.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton btnClose = UIFactory.glassButton("Done");
        btnPrev.addActionListener(e -> { AudioManager.playSfx("button.wav"); prev(); });
        btnNext.addActionListener(e -> { AudioManager.playSfx("button.wav"); next(); });
        btnClose.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });

        JPanel btnRow = new JPanel();
        btnRow.setOpaque(false);
        btnRow.setLayout(new BoxLayout(btnRow, BoxLayout.X_AXIS));
        btnRow.add(btnPrev);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(btnNext);
        btnRow.add(Box.createHorizontalGlue());
        btnRow.add(btnClose);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(lblStep, BorderLayout.CENTER);
        footer.add(btnRow, BorderLayout.SOUTH);

        glass.add(footer, BorderLayout.SOUTH);

        pageIndex = Math.max(0, Math.min(pageCount - 1, startPage));
        showPage(pageIndex);

        installKeys(glass);

        pack();
        setSize(720, 460);
        setLocationRelativeTo(owner);
    }

    private JComponent pageOverview() {
        return page(
                "Welcome to Chimp Sweeper",
                bullet(
                        "Goal: clear safe tiles and avoid mines.",
                        "Two players, two boards — your actions affect the team.",
                        "Team lives are shared. If lives hit 0 → game over.",
                        "Some tiles are special: Question (Q) and Surprise (S)."
                ),
                tip("Tip: If you’re unsure, flag it first 🚩")
        );
    }

    private JComponent pageControls() {
        return page(
                "Controls & Flow",
                bullet(
                        "Reveal: open a tile (numbers help you deduce mines).",
                        "Flag: mark a tile you think is a mine.",
                        "Question tiles (Q): trigger a question when opened.",
                        "Surprise tiles (S): random good or bad event."
                ),
                tip("Keyboard: ESC closes • ←/→ browse • F1 opens help anywhere")
        );
    }

    private JComponent pageSpecialCells() {
        return page(
                "Special Tiles (Q / S)",
                bullet(
                        "Q (Question): answer correctly for rewards.",
                        "Wrong answers can cost score or lives (depends on rules).",
                        "S (Surprise): instant random event (bonus / extra life / penalty).",
                        "These tiles add risk/reward and make each game unique."
                ),
                tip("Use Q/S tiles strategically when the board is tight.")
        );
    }

    private JComponent pageScoring() {
        return page(
                "Lives & Score",
                bullet(
                        "Lives: shared by the team. Mines and bad outcomes reduce lives.",
                        "Score: increases with safe progress and good outcomes.",
                        "Difficulty increases pressure and penalties (and rewards).",
                        "Winning needs smart revealing + flags + managing Q/S risks."
                ),
                tip("Play like a duo: one clears, one deduces patterns and flags.")
        );
    }

    private JComponent page(String h, JComponent body, JComponent bottom) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel header = UIFactory.dialogTitle(h);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(header);
        p.add(Box.createVerticalStrut(10));
        p.add(body);
        p.add(Box.createVerticalStrut(12));
        p.add(bottom);

        return p;
    }

    private JComponent bullet(String... lines) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String s : lines) {
            JLabel l = new JLabel("• " + s);
            l.setForeground(new Color(235, 235, 235, 190));
            l.setFont(new Font("SansSerif", Font.PLAIN, 13));
            l.setBorder(new EmptyBorder(3, 0, 3, 0));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(l);
        }
        return box;
    }

    private JComponent tip(String text) {
        JPanel tip = UIFactory.glassPill();
        tip.setLayout(new BorderLayout());
        tip.setBorder(new EmptyBorder(10, 12, 10, 12));
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel("💡 " + text);
        l.setForeground(new Color(245, 242, 230));
        l.setFont(new Font("SansSerif", Font.BOLD, 13));

        tip.add(l, BorderLayout.CENTER);
        return tip;
    }

    private void showPage(int i) {
        pages.show(pageRoot, "p" + i);
        lblStep.setText("Page " + (i + 1) + " / " + pageCount + "   •   ESC close   •   ← / → browse");
        btnPrev.setEnabled(i > 0);
        btnNext.setEnabled(i < pageCount - 1);
    }

    private void next() { if (pageIndex < pageCount - 1) showPage(++pageIndex); }
    private void prev() { if (pageIndex > 0) showPage(--pageIndex); }

    private void installKeys(JComponent root) {
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = root.getInputMap(cond);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prev");

        am.put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { AudioManager.playSfx("button.wav"); dispose(); }
        });
        am.put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { AudioManager.playSfx("button.wav"); next(); }
        });
        am.put("prev", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { AudioManager.playSfx("button.wav"); prev(); }
        });
    }
}
