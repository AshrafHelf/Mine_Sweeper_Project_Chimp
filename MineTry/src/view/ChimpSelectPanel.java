package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChimpSelectPanel extends JPanel {

    public interface DoneListener {
        void onDone(String chimpP1, String chimpP2, String nameP1, String nameP2);
    }

    private DoneListener doneListener;
    private Runnable backListener;

    private final Image bgImage;

    private static class Chimp {
        final String id;
        final String name;
        final String desc;
        final ImageIcon icon;

        Chimp(String id, String name, String desc, String imgPath) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.icon = loadIcon(imgPath);
        }
    }

    private final List<Chimp> chimps = new ArrayList<>();

    private int stepPlayer = 1; // 1 then 2
    private int index = 0;

    private String chimpP1 = null;
    private String chimpP2 = null;

    // UI
    private JLabel header, nameLbl, descLbl, pageHint;

    private SpotlightImage spotlight;

    private JButton left, right, select, back;

    private JTextField tfP1;
    private JTextField tfP2;

    public ChimpSelectPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setFocusable(true);

        bgImage = loadIcon("/img/splash_leaves5.gif").getImage();

        // ✅ Your 8 chimps
        chimps.add(new Chimp("knight",  "Knight Chimp",   "Shielded • Brave • Steady", "/img/chimps/chimp1.png"));
        chimps.add(new Chimp("gamer",   "Gamer Chimp",    "Quick Hands • Focus Mode",  "/img/chimps/chimp2.png"));
        chimps.add(new Chimp("ninja",   "Ninja Chimp",    "Silent • Agile • Sharp",   "/img/chimps/chimp3.png"));
        chimps.add(new Chimp("fairy",   "Fairy Chimp",    "Lucky • Sparkly • Chill",  "/img/chimps/chimp4.png"));
        chimps.add(new Chimp("hacker",  "Hacker Chimp",   "Smart • Calculated • OP",  "/img/chimps/chimp5.png"));
        chimps.add(new Chimp("monday",  "Monday Chimp",   "Tired • But Still Wins",   "/img/chimps/chimp6.png"));
        chimps.add(new Chimp("trainer", "Trainer Chimp",  "Strong • Consistent",      "/img/chimps/chimp7.png"));
        chimps.add(new Chimp("astro",   "Astronaut Chimp","Explorer • Fearless",      "/img/chimps/chimp8.png"));

        GifBackgroundPanel bg = new GifBackgroundPanel(bgImage);
        bg.setLayout(new GridBagLayout());
        add(bg, BorderLayout.CENTER);

        JPanel card = glassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 28, 18, 28));
        card.setMaximumSize(new Dimension(820, 610));

        header = new JLabel("PLAYER 1 — Create your profile");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setForeground(new Color(245, 242, 230));
        header.setFont(new Font("SansSerif", Font.BOLD, 22));

        pageHint = new JLabel("◀ ▶ to browse • ENTER to select • ESC to back");
        pageHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageHint.setForeground(new Color(235, 235, 235, 170));
        pageHint.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel carousel = new JPanel(new BorderLayout(18, 0));
        carousel.setOpaque(false);

        left = new SmoothButton("◀");
        right = new SmoothButton("▶");
        left.setPreferredSize(new Dimension(90, 60));
        right.setPreferredSize(new Dimension(90, 60));

        spotlight = new SpotlightImage();
        spotlight.setFocusable(false);
        spotlight.setOpaque(false);
        spotlight.setPreferredSize(new Dimension(380, 330));

        carousel.add(left, BorderLayout.WEST);
        carousel.add(spotlight, BorderLayout.CENTER);
        carousel.add(right, BorderLayout.EAST);

        nameLbl = new JLabel("");
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLbl.setForeground(new Color(245, 242, 230));
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 20));

        descLbl = new JLabel("");
        descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLbl.setForeground(new Color(235, 235, 235, 190));
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // ===== Name inputs (profile) =====
        JPanel namesRow = new JPanel(new GridLayout(1, 2, 14, 0));
        namesRow.setOpaque(false);
        namesRow.setMaximumSize(new Dimension(700, 62));

        tfP1 = styledField("Player 1 name");
        tfP2 = styledField("Player 2 name");

        JPanel p1Box = labeledBox("PLAYER 1", tfP1);
        JPanel p2Box = labeledBox("PLAYER 2", tfP2);

        namesRow.add(p1Box);
        namesRow.add(p2Box);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);

        back = new SmoothButton("← BACK");
        select = new SmoothButton("SELECT");

        back.setPreferredSize(new Dimension(170, 46));
        select.setPreferredSize(new Dimension(200, 46));

        actions.add(back);
        actions.add(Box.createHorizontalStrut(14));
        actions.add(select);

        card.add(header);
        card.add(Box.createVerticalStrut(6));
        card.add(pageHint);
        card.add(Box.createVerticalStrut(12));
        card.add(carousel);
        card.add(Box.createVerticalStrut(10));
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(descLbl);
        card.add(Box.createVerticalStrut(12));
        card.add(namesRow);
        card.add(Box.createVerticalStrut(16));
        card.add(actions);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 60, 0, 60);
        bg.add(card, gbc);

        left.addActionListener(e -> move(-1));
        right.addActionListener(e -> move(+1));
        select.addActionListener(e -> onSelect());
        back.addActionListener(e -> { if (backListener != null) backListener.run(); });

        installKeys();
        refreshUI();
        updateNameFieldState();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
    }

    public void setOnDone(DoneListener l) { this.doneListener = l; }
    public void setOnBack(Runnable r) { this.backListener = r; }

    public void resetFlow() {
        stepPlayer = 1;
        chimpP1 = null;
        chimpP2 = null;
        index = 0;

        tfP1.setText("");
        tfP2.setText("");

        refreshUI();
        updateNameFieldState();
        SwingUtilities.invokeLater(() -> tfP1.requestFocusInWindow());
    }

    private void updateNameFieldState() {
        boolean p1Step = (stepPlayer == 1);
        tfP1.setEnabled(p1Step);
        tfP2.setEnabled(!p1Step);

        if (p1Step) {
            tfP1.requestFocusInWindow();
        } else {
            tfP2.requestFocusInWindow();
        }
    }

    private String currentNameOrNull() {
        String s = (stepPlayer == 1 ? tfP1.getText() : tfP2.getText());
        s = (s == null) ? "" : s.trim();
        return s.isEmpty() ? null : s;
    }

    private void move(int dir) {
        if (chimps.isEmpty()) return;

        int attempts = 0;
        do {
            index = (index + dir + chimps.size()) % chimps.size();
            attempts++;
            if (attempts > chimps.size() + 2) break;
        } while (isLockedForCurrentPlayer(chimps.get(index).id));

        refreshUI();
    }

    private boolean isLockedForCurrentPlayer(String chimpId) {
        return stepPlayer == 2 && chimpId.equals(chimpP1);
    }

    private void onSelect() {
        if (chimps.isEmpty()) return;

        String nm = currentNameOrNull();
        if (nm == null) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a name for Player " + stepPlayer + " before selecting.",
                    "Name Required",
                    JOptionPane.WARNING_MESSAGE
            );
            updateNameFieldState();
            return;
        }

        String chosenId = chimps.get(index).id;

        if (stepPlayer == 1) {
            chimpP1 = chosenId;
            stepPlayer = 2;

            if (isLockedForCurrentPlayer(chosenId)) move(+1);
            refreshUI();
            updateNameFieldState();
            return;
        }

        if (chosenId.equals(chimpP1)) {
            move(+1);
            return;
        }

        chimpP2 = chosenId;

        String p1Name = tfP1.getText().trim();
        String p2Name = tfP2.getText().trim();

        if (doneListener != null) doneListener.onDone(chimpP1, chimpP2, p1Name, p2Name);
    }

    private void refreshUI() {
        header.setText(stepPlayer == 1 ? "PLAYER 1 — Create your profile" : "PLAYER 2 — Create your profile");

        Chimp c = chimps.get(index);
        nameLbl.setText(c.name);
        descLbl.setText(c.desc);

        spotlight.setLocked(isLockedForCurrentPlayer(c.id));
        spotlight.setImage(scaleToFit(c.icon.getImage(), 270, 270));

        if (stepPlayer == 2 && c.id.equals(chimpP1)) {
            descLbl.setText("Already chosen by Player 1 — pick another");
        }
    }

    private Image scaleToFit(Image img, int maxW, int maxH) {
        if (img == null) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return img.getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
    }

    private void installKeys() {
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = getInputMap(cond);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "select");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");

        am.put("left", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { left.doClick(); }});
        am.put("right", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { right.doClick(); }});
        am.put("select", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { select.doClick(); }});
        am.put("back", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { back.doClick(); }});
    }

    // ===== Style helpers =====
    private JPanel labeledBox(String label, JTextField field) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BorderLayout(0, 6));

        JLabel l = new JLabel(label);
        l.setForeground(new Color(235, 235, 235, 190));
        l.setFont(new Font("SansSerif", Font.BOLD, 12));

        box.add(l, BorderLayout.NORTH);
        box.add(field, BorderLayout.CENTER);
        return box;
    }

    private JTextField styledField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setColumns(16);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setForeground(new Color(245, 242, 230));
        tf.setCaretColor(new Color(245, 242, 230));
        tf.setBackground(new Color(0, 0, 0, 120));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 55)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        tf.setToolTipText(placeholder);
        return tf;
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
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
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

    private static class SpotlightImage extends JPanel {
        private Image image;
        private boolean locked;

        void setImage(Image img) { this.image = img; repaint(); }
        void setLocked(boolean locked) { this.locked = locked; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
            Color glowA = locked ? new Color(220, 80, 80, 110) : new Color(120, 220, 255, 110);
            Color glowB = new Color(0, 0, 0, 0);

            RadialGradientPaint rg = new RadialGradientPaint(
                    new Point(w / 2, (int) (h * 0.78)),
                    Math.min(w, h) * 0.52f,
                    new float[]{0f, 1f},
                    new Color[]{glowA, glowB}
            );
            g2.setPaint(rg);
            g2.fillOval((int) (w * 0.18), (int) (h * 0.55), (int) (w * 0.64), (int) (h * 0.38));

            g2.setComposite(AlphaComposite.SrcOver.derive(0.10f));
            g2.setColor(Color.WHITE);
            Polygon beam = new Polygon(
                    new int[]{w / 2, (int) (w * 0.18), (int) (w * 0.82)},
                    new int[]{0, (int) (h * 0.62), (int) (h * 0.62)},
                    3
            );
            g2.fillPolygon(beam);

            if (image != null) {
                g2.setComposite(AlphaComposite.SrcOver.derive(locked ? 0.45f : 1f));
                int iw = image.getWidth(this);
                int ih = image.getHeight(this);
                int x = (w - iw) / 2;
                int y = (h - ih) / 2 + 4;
                g2.drawImage(image, x, y, this);
            }

            if (locked) {
                g2.setComposite(AlphaComposite.SrcOver.derive(1f));
                g2.setColor(new Color(255, 140, 140, 220));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                String msg = "LOCKED";
                int tw = g2.getFontMetrics().stringWidth(msg);
                g2.drawString(msg, (w - tw) / 2, (int) (h * 0.92));
            }

            g2.dispose();
        }
    }

    private static ImageIcon loadIcon(String path) {
        URL url = ChimpSelectPanel.class.getResource(path);
        if (url == null) return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        return new ImageIcon(url);
    }
}
