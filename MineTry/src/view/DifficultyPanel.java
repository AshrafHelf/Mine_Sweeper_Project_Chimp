package view;

import enums.Difficulty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Separate difficulty screen.
 * Pick EASY/MEDIUM/HARD with small description, then continue.
 */
public class DifficultyPanel extends JPanel {

    public interface DoneListener {
        void onDone(Difficulty difficulty);
    }

    private DoneListener doneListener;
    private Runnable backListener;

    private final Image bgImage;

    private Difficulty selected = Difficulty.EASY;

    private JLabel info;
    private DiffToggle easy, med, hard;

    public DifficultyPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setFocusable(true);

        bgImage = loadIcon("/img/splash_leaves5.gif").getImage();

        GifBackgroundPanel bg = new GifBackgroundPanel(bgImage);
        bg.setLayout(new GridBagLayout());
        add(bg, BorderLayout.CENTER);

        JPanel card = glassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 28, 18, 28));
        card.setMaximumSize(new Dimension(720, 420));

        JLabel header = new JLabel("CHOOSE DIFFICULTY");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setForeground(new Color(245, 242, 230));
        header.setFont(new Font("SansSerif", Font.BOLD, 26));

        JLabel hint = new JLabel("Pick one • ENTER to continue • ESC to go back");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(new Color(235, 235, 235, 170));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        ButtonGroup g = new ButtonGroup();
        easy = new DiffToggle("EASY");
        med  = new DiffToggle("MEDIUM");
        hard = new DiffToggle("HARD");
        g.add(easy); g.add(med); g.add(hard);
        easy.setSelected(true);

        easy.addActionListener(e -> { selected = Difficulty.EASY; updateInfo(); });
        med.addActionListener(e ->  { selected = Difficulty.MEDIUM; updateInfo(); });
        hard.addActionListener(e -> { selected = Difficulty.HARD; updateInfo(); });

        row.add(easy);
        row.add(Box.createHorizontalStrut(12));
        row.add(med);
        row.add(Box.createHorizontalStrut(12));
        row.add(hard);

        info = new JLabel();
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.setForeground(new Color(235, 235, 235, 200));
        info.setFont(new Font("SansSerif", Font.PLAIN, 13));
        updateInfo();

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton back = new SmoothButton("← BACK");
        JButton next = new SmoothButton("CONTINUE");

        back.setPreferredSize(new Dimension(150, 46));
        next.setPreferredSize(new Dimension(180, 46));

        back.addActionListener(e -> { if (backListener != null) backListener.run(); });
        next.addActionListener(e -> { if (doneListener != null) doneListener.onDone(selected); });

        actions.add(back);
        actions.add(Box.createHorizontalStrut(12));
        actions.add(next);

        card.add(header);
        card.add(Box.createVerticalStrut(6));
        card.add(hint);
        card.add(Box.createVerticalStrut(14));
        card.add(row);
        card.add(Box.createVerticalStrut(12));
        card.add(info);
        card.add(Box.createVerticalStrut(16));
        card.add(actions);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 60, 0, 60);
        bg.add(card, gbc);

        installKeys(next);
    }

    public void setOnDone(DoneListener l) { this.doneListener = l; }
    public void setOnBack(Runnable r) { this.backListener = r; }

    private void updateInfo() {
        String text = switch (selected) {
            case EASY -> "Easy: smaller grid • fewer mines • beginner friendly";
            case MEDIUM -> "Medium: balanced grid • more pressure • standard challenge";
            case HARD -> "Hard: larger grid • many mines • harsh penalties";
        };
        info.setText(text);
    }

    private void installKeys(JButton nextBtn) {
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = getInputMap(cond);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "next");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");

        am.put("next", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { nextBtn.doClick(); }});
        am.put("back", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { if (backListener != null) backListener.run(); }});
    }

    // ---------- UI helpers (same style family) ----------

    private JPanel glassCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 22;
                int w = getWidth(), h = getHeight();

                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillRoundRect(6, 6, w - 6, h - 6, arc, arc);

                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRoundRect(0, 0, w - 6, h - 6, arc, arc);

                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 8, h - 8, arc, arc);

                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private static class GifBackgroundPanel extends JPanel {
        private final Image img;
        GifBackgroundPanel(Image img) { this.img = img; }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;

            int w = getWidth(), h = getHeight();
            int iw = img.getWidth(this), ih = img.getHeight(this);
            if (iw <= 0 || ih <= 0) return;

            double s = Math.max((double) w / iw, (double) h / ih);
            int dw = (int) (iw * s), dh = (int) (ih * s);
            int x = (w - dw) / 2, y = (h - dh) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, x, y, dw, dh, this);

            g2.setComposite(AlphaComposite.SrcOver.derive(0.12f));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);

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
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setRolloverEnabled(true);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();
            int arc = 16;

            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, arc, arc);

            g2.setColor(hover ? new Color(30, 30, 30, 200) : new Color(20, 20, 20, 175));
            if (press) g2.setColor(new Color(15, 15, 15, 220));
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, arc, arc);

            g2.setColor(new Color(255, 255, 255, hover ? 85 : 55));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, arc, arc);

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
            setForeground(new Color(245, 242, 230));
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setRolloverEnabled(true);
        }

        @Override public Dimension getPreferredSize() {
            Dimension ps = super.getPreferredSize();
            return new Dimension(Math.max(ps.width, 120), 44);
        }

        @Override protected void paintComponent(Graphics g) {
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

    private static ImageIcon loadIcon(String path) {
        URL url = DifficultyPanel.class.getResource(path);
        if (url == null) return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        return new ImageIcon(url);
    }
}
