package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Main Menu screen (Home):
 * - Play / History / Questions / Settings / Help / Exit
 * - Jungle glass overlay on animated GIF background.
 *
 * NOTE:
 * - Callbacks are optional. If onHelp/onSettings are not set, it still opens dialogs directly.
 */
public class MainMenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private Runnable onPlay;
    private Runnable onHistory;
    private Runnable onQuestions;
    private Runnable onSettings;
    private Runnable onHelp;
    private Runnable onExit;

    // Background
    private final Image gifImage;

    public MainMenuPanel() {
        setLayout(new BorderLayout());
        setFocusable(true);
        setOpaque(true);

        ImageIcon gifIcon = loadGif("/img/splash_leaves5.gif");
        this.gifImage = gifIcon.getImage();

        GifBackgroundPanel bg = new GifBackgroundPanel(gifImage);
        bg.setLayout(new GridBagLayout());
        add(bg, BorderLayout.CENTER);

        // Center glass card
        JPanel card = glassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(860, 520));

        // Title
        ShadowLabel title = new ShadowLabel("CHIMP SWEEPER", new Font("SansSerif", Font.BOLD, 74));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Home Base • Choose your path");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(240, 240, 240, 190));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        // Buttons (match screenshot)
        JButton play      = new SmoothButton("▶  Play");
        JButton history   = new SmoothButton("📜  History");
        JButton questions = new SmoothButton("❓  Questions");
        JButton settings  = new SmoothButton("⚙  Settings");
        JButton help      = new SmoothButton("❔  Help");
        JButton exit      = new SmoothButton("⛔  Exit");

        for (JButton b : new JButton[]{play, history, questions, settings, help, exit}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        card.add(play);
        card.add(Box.createVerticalStrut(10));
        card.add(history);
        card.add(Box.createVerticalStrut(10));
        card.add(questions);
        card.add(Box.createVerticalStrut(10));
        card.add(settings);
        card.add(Box.createVerticalStrut(10));
        card.add(help);
        card.add(Box.createVerticalStrut(10));
        card.add(exit);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 70, 0, 70);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bg.add(card, gbc);

        // Wiring
        play.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            if (onPlay != null) onPlay.run();
        });

        history.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            if (onHistory != null) onHistory.run();
        });

        questions.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            if (onQuestions != null) onQuestions.run();
        });

        settings.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            if (onSettings != null) onSettings.run();
            else openSettings();
        });

        help.addActionListener(e -> {
            AudioManager.playSfx("message.wav");
            if (onHelp != null) onHelp.run();
            else openHelp();
        });

        exit.addActionListener(e -> {
            AudioManager.playSfx("button.wav");
            if (onExit != null) onExit.run();
        });

        installKeyBindings(play);
    }

    // ---------- Callbacks ----------
    public void setOnPlay(Runnable r) { this.onPlay = r; }
    public void setOnHistory(Runnable r) { this.onHistory = r; }
    public void setOnQuestions(Runnable r) { this.onQuestions = r; }
    public void setOnSettings(Runnable r) { this.onSettings = r; }
    public void setOnHelp(Runnable r) { this.onHelp = r; }
    public void setOnExit(Runnable r) { this.onExit = r; }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    // ---------- Reliable dialog openers (even if controller forgot callbacks) ----------
    private void openHelp() {
        Window w = SwingUtilities.getWindowAncestor(this);
        SwingUtilities.invokeLater(() -> {
            try {
                HelpDialog.open(w);
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(w, "Help failed to open:\n" + ex.getMessage());
            }
        });
    }

    private void openSettings() {
        Window w = SwingUtilities.getWindowAncestor(this);
        SwingUtilities.invokeLater(() -> {
            try {
                SettingsDialog dlg = new SettingsDialog(w);
                dlg.setAlwaysOnTop(true);
                dlg.setVisible(true);
                dlg.toFront();
                dlg.requestFocus();
                dlg.setAlwaysOnTop(false);
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(w, "Settings failed to open:\n" + ex.getMessage());
            }
        });
    }

    // ---------- Keys ----------
    private void installKeyBindings(JButton playBtn) {
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = getInputMap(condition);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "play");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "play");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "help");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "settings");

        am.put("play", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                playBtn.doClick();
            }
        });

        am.put("exit", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                AudioManager.playSfx("button.wav");
                if (onExit != null) onExit.run();
            }
        });

        am.put("help", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                AudioManager.playSfx("message.wav");
                if (onHelp != null) onHelp.run();
                else openHelp();
            }
        });

        am.put("settings", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                AudioManager.playSfx("button.wav");
                if (onSettings != null) onSettings.run();
                else openSettings();
            }
        });
    }

    // ---------- Background / visuals ----------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // subtle readability overlay
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(0.12f));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private ImageIcon loadGif(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("❌ Menu GIF not found: " + path);
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }
        return new ImageIcon(url);
    }

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

                // glass
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRoundRect(0, 0, w - 6, h - 6, arc, arc);

                // border highlight
                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 8, h - 8, arc, arc);

                g2.dispose();
            }
        };

        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 34, 20, 34));
        return p;
    }

    // ---------- Button styles ----------
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
        public Dimension getPreferredSize() {
            return new Dimension(320, 44);
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

            Color fill = hover ? new Color(30, 30, 30, 200) : new Color(20, 20, 20, 175);
            if (press) fill = new Color(15, 15, 15, 210);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, arc, arc);

            g2.setColor(new Color(255, 255, 255, hover ? 85 : 55));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Fullscreen background ----------
    private static class GifBackgroundPanel extends JPanel {
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
            g2.dispose();
        }
    }
}
