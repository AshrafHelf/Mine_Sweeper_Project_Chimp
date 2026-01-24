package view;

import enums.Difficulty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Setup screen:
 * - Player 1 picks a chimp
 * - Player 2 picks a different chimp (no duplicates enforced)
 * - Choose difficulty
 * - Enter player names
 * - Start game
 * - Back to Home
 *
 * Visual style: matches Splash/Home glass system.
 */
public class MainMenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public interface StartListener {
        void start(Difficulty difficulty, String p1, String p2);
    }

    private StartListener onStart;
    private Runnable onBack;

    // ---- Data ----
    private final String[] chimps = {
            "Banana Lover", "Jungle Ninja", "Smart Scout",
            "Pirate Chimp", "Forest Mage", "Cool Skater"
    };

    private String selectedChimpP1;
    private String selectedChimpP2;

    private Difficulty selectedDifficulty = Difficulty.EASY;

    public String getSelectedChimpP1() { return selectedChimpP1; }
    public String getSelectedChimpP2() { return selectedChimpP2; }

    // ---- UI ----
    private final List<ChimpToggle> p1ChimpBtns = new ArrayList<>();
    private final List<ChimpToggle> p2ChimpBtns = new ArrayList<>();

    private JLabel p1SelectedLabel;
    private JLabel p2SelectedLabel;

    private JTextField p1Field;
    private JTextField p2Field;
    private JLabel diffInfo;

    // Background
    private final Image gifImage;

    public MainMenuPanel() {
        setLayout(new BorderLayout());
        setFocusable(true);
        setOpaque(true);

        gifImage = loadGif("/img/splash_leaves5.gif").getImage();

        GifBackgroundPanel bg = new GifBackgroundPanel(gifImage);
        bg.setLayout(new GridBagLayout());
        add(bg, BorderLayout.CENTER);

        JPanel card = glassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(820, 520)); // ✅ smaller & cleaner

        // ---- Header ----
        OutlineLabel title = new OutlineLabel(
                "SETUP CAMP",
                new Font("SansSerif", Font.BOLD, 52),
                new Color(245, 242, 230),
                new Color(10, 10, 10, 220),
                3
        );
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Pick your chimps • Choose difficulty • Start the adventure");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(240, 240, 240, 200));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(14));

        // ---- Chimp pickers ----
        card.add(twoPlayerChimpPicker());
        card.add(Box.createVerticalStrut(12));

        // ---- Difficulty ----
        card.add(difficultyPicker());
        card.add(Box.createVerticalStrut(8));

        diffInfo = new JLabel();
        diffInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        diffInfo.setForeground(new Color(235, 235, 235, 200));
        diffInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        updateDiffInfo();
        card.add(diffInfo);

        card.add(Box.createVerticalStrut(12));

        // ---- Player names ----
        card.add(playerNamesRow());
        card.add(Box.createVerticalStrut(14));

        // ---- Actions row (ONLY Start + Back) ----
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton back = new SmoothButton("←  BACK");
        JButton start = new SmoothButton("🍌  START GAME");

        back.setPreferredSize(new Dimension(160, 46));
        start.setPreferredSize(new Dimension(240, 46));

        actions.add(back);
        actions.add(Box.createHorizontalStrut(12));
        actions.add(start);

        card.add(actions);

        // place card in center
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 60, 0, 60);
        bg.add(card, gbc);

        // ---- Default selections ----
        selectedChimpP1 = chimps[0];
        selectedChimpP2 = chimps[1];

        if (!p1ChimpBtns.isEmpty()) p1ChimpBtns.get(0).setSelected(true);
        if (p2ChimpBtns.size() > 1) p2ChimpBtns.get(1).setSelected(true);

        syncChimpLocks();
        updateSelectedLabels();

        // ---- Wiring ----
        back.addActionListener(e -> { if (onBack != null) onBack.run(); });

        start.addActionListener(e -> {
            if (onStart == null) return;
            String p1 = safeName(p1Field.getText(), "Player 1");
            String p2 = safeName(p2Field.getText(), "Player 2");
            onStart.start(selectedDifficulty, p1, p2);
        });

        installKeyBindings(start);
    }

    public void setOnStart(StartListener l) { this.onStart = l; }
    public void setOnBack(Runnable r) { this.onBack = r; }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    // ---------------- UI Sections ----------------

    private JComponent twoPlayerChimpPicker() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t = new JLabel("Choose Your Chimps (No Duplicates)");
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        t.setForeground(new Color(240, 240, 240, 200));
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        wrap.add(t);
        wrap.add(Box.createVerticalStrut(8));

        JPanel twoCols = new JPanel(new GridLayout(1, 2, 16, 0));
        twoCols.setOpaque(false);

        twoCols.add(buildChimpColumn("PLAYER 1", true));
        twoCols.add(buildChimpColumn("PLAYER 2", false));

        wrap.add(twoCols);
        return wrap;
    }

    private JComponent buildChimpColumn(String header, boolean isP1) {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel h = new JLabel(header);
        h.setAlignmentX(Component.CENTER_ALIGNMENT);
        h.setForeground(new Color(240, 240, 240, 190));
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
        col.add(h);
        col.add(Box.createVerticalStrut(6));

        JPanel grid = new JPanel(new GridLayout(3, 2, 10, 10));
        grid.setOpaque(false);

        ButtonGroup group = new ButtonGroup();

        for (String chimp : chimps) {
            ChimpToggle b = new ChimpToggle(chimp);

            b.addActionListener(e -> {
                if (isP1) selectedChimpP1 = chimp;
                else selectedChimpP2 = chimp;

                syncChimpLocks();
                updateSelectedLabels();
            });

            group.add(b);
            grid.add(b);

            if (isP1) p1ChimpBtns.add(b);
            else p2ChimpBtns.add(b);
        }

        col.add(grid);

        // Selected label (makes it feel intentional + game-like)
        JLabel sel = new JLabel();
        sel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sel.setForeground(new Color(235, 235, 235, 190));
        sel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sel.setBorder(new EmptyBorder(6, 0, 0, 0));
        col.add(sel);

        if (isP1) p1SelectedLabel = sel;
        else p2SelectedLabel = sel;

        return col;
    }

    private JComponent difficultyPicker() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t = new JLabel("Difficulty");
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        t.setForeground(new Color(240, 240, 240, 200));
        t.setFont(new Font("SansSerif", Font.BOLD, 14));

        wrap.add(t);
        wrap.add(Box.createVerticalStrut(8));

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        ButtonGroup g = new ButtonGroup();

        JToggleButton easy = new DiffToggle("EASY");
        JToggleButton med  = new DiffToggle("MEDIUM");
        JToggleButton hard = new DiffToggle("HARD");

        easy.addActionListener(e -> { selectedDifficulty = Difficulty.EASY; updateDiffInfo(); });
        med.addActionListener(e ->  { selectedDifficulty = Difficulty.MEDIUM; updateDiffInfo(); });
        hard.addActionListener(e -> { selectedDifficulty = Difficulty.HARD; updateDiffInfo(); });

        g.add(easy); g.add(med); g.add(hard);

        easy.setSelected(true);

        row.add(easy);
        row.add(Box.createHorizontalStrut(10));
        row.add(med);
        row.add(Box.createHorizontalStrut(10));
        row.add(hard);

        wrap.add(row);
        return wrap;
    }

    private JComponent playerNamesRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ✅ start empty (avoid submitting placeholder text)
        p1Field = glassField("");
        p2Field = glassField("");

        row.add(labeledField("Player 1", p1Field));
        row.add(labeledField("Player 2", p2Field));

        return row;
    }

    private JComponent labeledField(String label, JTextField field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setForeground(new Color(240, 240, 240, 190));
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }

    // ---------------- Behaviors ----------------

    private void updateSelectedLabels() {
        if (p1SelectedLabel != null) p1SelectedLabel.setText("Selected: " + selectedChimpP1 + " ✅");
        if (p2SelectedLabel != null) p2SelectedLabel.setText("Selected: " + selectedChimpP2 + " ✅");
    }

    /** Enforces: P1 and P2 cannot have the same chimp. */
    private void syncChimpLocks() {
        for (ChimpToggle b : p1ChimpBtns) b.setEnabled(true);
        for (ChimpToggle b : p2ChimpBtns) b.setEnabled(true);

        if (selectedChimpP1 != null) {
            for (ChimpToggle b : p2ChimpBtns) {
                if (b.getText().equals(selectedChimpP1)) {
                    b.setEnabled(false);
                    if (b.isSelected()) autoPickDifferent(false);
                    break;
                }
            }
        }

        if (selectedChimpP2 != null) {
            for (ChimpToggle b : p1ChimpBtns) {
                if (b.getText().equals(selectedChimpP2)) {
                    b.setEnabled(false);
                    if (b.isSelected()) autoPickDifferent(true);
                    break;
                }
            }
        }

        repaint();
    }

    private void autoPickDifferent(boolean forP1) {
        if (forP1) {
            for (ChimpToggle b : p1ChimpBtns) {
                if (b.isEnabled()) {
                    b.setSelected(true);
                    selectedChimpP1 = b.getText();
                    return;
                }
            }
        } else {
            for (ChimpToggle b : p2ChimpBtns) {
                if (b.isEnabled()) {
                    b.setSelected(true);
                    selectedChimpP2 = b.getText();
                    return;
                }
            }
        }
    }

    private void updateDiffInfo() {
        diffInfo.setText(switch (selectedDifficulty) {
            case EASY -> "Easy: smaller board • fewer mines • softer penalties";
            case MEDIUM -> "Medium: balanced board • more pressure • more surprises";
            case HARD -> "Hard: large board • many mines • harsh penalties • big rewards";
        });
    }

    private void installKeyBindings(JButton startBtn) {
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = getInputMap(condition);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "start");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");

        am.put("start", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                startBtn.doClick();
            }
        });
        am.put("back", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (onBack != null) onBack.run();
            }
        });
    }

    private String safeName(String s, String fallback) {
        if (s == null) return fallback;
        s = s.trim();
        return s.isEmpty() ? fallback : s;
    }

    // ---------------- Glass helpers (Splash style) ----------------

    private JPanel glassCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 22;
                int w = getWidth();
                int h = getHeight();

                // shadow
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillRoundRect(6, 6, w - 6, h - 6, arc, arc);

                // ✅ darker glass (was 110)
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRoundRect(0, 0, w - 6, h - 6, arc, arc);

                // border highlight
                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 8, h - 8, arc, arc);

                g2.dispose();
            }
        };

        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 26, 18, 26));
        return p;
    }

    private JTextField glassField(String text) {
        JTextField f = new JTextField(text, 16);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBackground(new Color(0, 0, 0, 135));
        f.setForeground(new Color(245, 242, 230));
        f.setCaretColor(new Color(245, 242, 230));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 45)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return f;
    }

    private ImageIcon loadGif(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Setup GIF not found: " + path);
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }
        return new ImageIcon(url);
    }

    // ---------------- Components ----------------

    private static class OutlineLabel extends JComponent {
        private final String text;
        private final Font font;
        private final Color fill;
        private final Color outline;
        private final int outlineSize;

        OutlineLabel(String text, Font font, Color fill, Color outline, int outlineSize) {
            this.text = text;
            this.font = font;
            this.fill = fill;
            this.outline = outline;
            this.outlineSize = outlineSize;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(font);
            return new Dimension(fm.stringWidth(text) + 24, fm.getHeight() + 18);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font);

            FontMetrics fm = g2.getFontMetrics();
            int x = 12;
            int y = 9 + fm.getAscent();

            g2.setColor(outline);
            for (int dx = -outlineSize; dx <= outlineSize; dx++) {
                for (int dy = -outlineSize; dy <= outlineSize; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    g2.drawString(text, x + dx, y + dy);
                }
            }

            g2.setColor(fill);
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }

    private static class SmoothButton extends JButton {
        SmoothButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(new Color(245, 242, 230));
            setFont(new Font("SansSerif", Font.BOLD, 15));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();
            int arc = 16;

            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, arc, arc);

            g2.setColor(hover ? new Color(30, 30, 30, 200) : new Color(20, 20, 20, 175));
            if (press) g2.setColor(new Color(15, 15, 15, 210));
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, arc, arc);

            g2.setColor(new Color(255, 255, 255, hover ? 85 : 55));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ChimpToggle extends JToggleButton {
        ChimpToggle(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();
            boolean on = isSelected();
            boolean disabled = !isEnabled();
            int arc = 16;

            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(3, 4, getWidth() - 6, getHeight() - 6, arc, arc);

            Color fill;
            Color border;

            if (disabled) {
                fill = new Color(10, 10, 10, 120);
                border = new Color(255, 255, 255, 25);
            } else if (on) {
                fill = new Color(255, 218, 120, 180);
                border = new Color(255, 245, 200, 120);
            } else {
                fill = hover ? new Color(30, 30, 30, 200) : new Color(20, 20, 20, 165);
                if (press) fill = new Color(10, 10, 10, 210);
                border = new Color(255, 255, 255, hover ? 75 : 45);
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);

            g2.setColor(border);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 8, getHeight() - 8, arc, arc);

            // text color
            Color fg = new Color(245, 242, 230);
            if (on && !disabled) fg = new Color(25, 20, 10);
            if (disabled) fg = new Color(240, 240, 240, 90);
            setForeground(fg);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class DiffToggle extends JToggleButton {
        DiffToggle(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();
            boolean on = isSelected();
            int arc = 16;

            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(3, 4, getWidth() - 6, getHeight() - 6, arc, arc);

            Color fill = on ? new Color(255, 218, 120, 180)
                    : (hover ? new Color(30, 30, 30, 200) : new Color(20, 20, 20, 165));
            if (press && !on) fill = new Color(10, 10, 10, 210);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);

            g2.setColor(new Color(255, 255, 255, hover ? 75 : 45));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 8, getHeight() - 8, arc, arc);

            setForeground(on ? new Color(25, 20, 10) : new Color(245, 242, 230));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class GifBackgroundPanel extends JPanel {
        private final Image gif;

        GifBackgroundPanel(Image gif) {
            this.gif = gif;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (gif == null) return;

            int w = getWidth();
            int h = getHeight();
            int imgW = gif.getWidth(this);
            int imgH = gif.getHeight(this);
            if (imgW <= 0 || imgH <= 0) return;

            double scale = Math.max((double) w / imgW, (double) h / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (w - drawW) / 2;
            int y = (h - drawH) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(gif, x, y, drawW, drawH, this);

            // subtle readability overlay
            g2.setComposite(AlphaComposite.SrcOver.derive(0.12f));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }
}
