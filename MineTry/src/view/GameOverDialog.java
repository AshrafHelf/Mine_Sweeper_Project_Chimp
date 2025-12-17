package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GameOverDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private transient Image bgImg;
    private transient Image resultImg;

    private static final Color GLASS_BG = new Color(10, 14, 33, 185);
    private static final Color BORDER   = new Color(120, 170, 120, 140);

    public GameOverDialog(JFrame owner, boolean won, int finalScore) {
        super(owner, "Game Over", true);

        setUndecorated(false);
        setResizable(false);

        // load images (classpath → works in JAR)
        bgImg = loadImage("/images/jungle_bg.png");
        resultImg = loadImage(won ? "/images/win_chimp.png"
                                  : "/images/lose_chimp.png");

        buildUI(won, finalScore);

        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(420, 300));
    }

    private void buildUI(boolean won, int finalScore) {
        setLayout(new BorderLayout());

        // ===== Glass card =====
        JPanel card = new GlassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.setOpaque(false);

        // Title
        JLabel title = new JLabel(won ? "Team Victory!" : "Game Over");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setForeground(won
                ? new Color(120, 220, 150)
                : new Color(230, 120, 130));

        JLabel subtitle = new JLabel(
                won ? "You cleared the board!" : "All lives were lost"
        );
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(220, 225, 245));
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        // Image
        JLabel imgLabel = new JLabel();
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (resultImg != null) {
            imgLabel.setIcon(new ImageIcon(resultImg.getScaledInstance(
                    180, 180, Image.SCALE_SMOOTH
            )));
        }

        JLabel score = new JLabel("Final team score: " + finalScore);
        score.setAlignmentX(Component.CENTER_ALIGNMENT);
        score.setForeground(new Color(210, 215, 240));
        score.setFont(score.getFont().deriveFont(Font.BOLD, 14f));

        JButton ok = new JButton("OK");
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        styleButton(ok);
        ok.addActionListener(e -> dispose());

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(10));
        card.add(imgLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(score);
        card.add(Box.createVerticalStrut(14));
        card.add(ok);

        add(card, BorderLayout.CENTER);
    }

    // ===== Background painting =====
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // base
        g2.setColor(new Color(10, 14, 33));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // background image
        if (bgImg != null) {
            g2.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
        }

        // overlay
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
        super.paint(g);
    }

    // ===== Helpers =====
    private void styleButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBackground(new Color(46, 125, 50));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 34));
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            return url == null ? null : new ImageIcon(url).getImage();
        } catch (Exception e) {
            return null;
        }
    }

    // ===== Glass card panel =====
    private static class GlassCard extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 22;

            // shadow
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 12, arc, arc);

            Shape rr = new RoundRectangle2D.Float(
                    0, 0, getWidth() - 10, getHeight() - 10, arc, arc
            );

            g2.setColor(GLASS_BG);
            g2.fill(rr);

            g2.setColor(BORDER);
            g2.draw(rr);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
