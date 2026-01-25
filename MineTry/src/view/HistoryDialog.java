package view;

import model.GameRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Color TXT   = new Color(245, 242, 230);
    private static final Color MUTED = new Color(235, 235, 235, 170);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public HistoryDialog(JFrame owner, List<GameRecord> records) {
        super(owner, "Game History", true);
        setUndecorated(true);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        GlassCard card = new GlassCard();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        // ---------- header ----------
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("📜 GAME HISTORY");
        title.setForeground(TXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel sub = new JLabel("Recent matches • Difficulty • Score • Result");
        sub.setForeground(MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(sub);

        JButton close = new MiniIconButton("✕");
        close.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });

        top.add(titles, BorderLayout.WEST);
        top.add(close, BorderLayout.EAST);

        // ---------- table ----------
        JComponent tableComp = buildTable(records);

        // ---------- footer ----------
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton done = new GlassButton("Close");
        done.addActionListener(e -> { AudioManager.playSfx("button.wav"); dispose(); });

        footer.add(done);

        card.add(top, BorderLayout.NORTH);
        card.add(tableComp, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        // ESC closes
        getRootPane().registerKeyboardAction(
                e -> { AudioManager.playSfx("button.wav"); dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        pack();
        setSize(920, 520);
        setLocationRelativeTo(owner);
    }

    private JComponent buildTable(List<GameRecord> records) {
        if (records == null || records.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));

            JLabel l1 = new JLabel("No games yet.");
            l1.setForeground(TXT);
            l1.setFont(new Font("SansSerif", Font.BOLD, 16));
            l1.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel l2 = new JLabel("Play a match and your history will appear here.");
            l2.setForeground(MUTED);
            l2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            l2.setAlignmentX(Component.CENTER_ALIGNMENT);

            empty.add(Box.createVerticalGlue());
            empty.add(l1);
            empty.add(Box.createVerticalStrut(6));
            empty.add(l2);
            empty.add(Box.createVerticalGlue());
            return empty;
        }

        String[] cols = {"Date / Time", "Difficulty", "Player 1", "Player 2", "Score", "Result"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (GameRecord r : records) {
            model.addRow(new Object[]{
                    TIME_FMT.format(r.getFinishedAt()),
                    r.getDifficulty().name(),
                    r.getPlayer1Name(),
                    r.getPlayer2Name(),
                    r.getFinalScore(),
                    r.isWon() ? "Win" : "Lose"
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        table.setOpaque(true);
        table.setBackground(new Color(10, 10, 10)); // solid = no artifacts
        table.setForeground(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(255, 220, 90, 90));
        table.setSelectionForeground(new Color(20, 20, 20));

        JTableHeader header = table.getTableHeader();
        header.setOpaque(true);
        header.setBackground(new Color(20, 20, 20));
        header.setForeground(TXT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);

        // viewport MUST paint a base too
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(new Color(10, 10, 10));


        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(8, 0, 2, 0));
        wrap.add(new RoundedWell(scroll), BorderLayout.CENTER);

        return wrap;
    }

    // ---------- shared UI ----------
    private static class RoundedWell extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JComponent content;

        RoundedWell(JComponent content) {
            this.content = content;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 10, 10, 10));
            add(content, BorderLayout.CENTER);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight(), arc = 18;

            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(new Color(255, 255, 255, 45));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();
        }
    }

    private static class GlassCard extends JPanel {
        private static final long serialVersionUID = 1L;
        GlassCard() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 24;

            int pad = 6;
            int ww = Math.max(1, w - pad * 2);
            int hh = Math.max(1, h - pad * 2);

            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(pad, pad + 1, ww, hh, arc, arc);

            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(0, 0, ww, hh, arc, arc);

            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, ww - 2, hh - 2, arc, arc);

            g2.dispose();
        }
    }

    private static class MiniIconButton extends JButton {
        private static final long serialVersionUID = 1L;
        MiniIconButton(String t) {
            super(t);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(TXT);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        }
    }

    private static class GlassButton extends JButton {
        private static final long serialVersionUID = 1L;
        GlassButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(TXT);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setRolloverEnabled(true);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = 18;

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();

            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillRoundRect(4, 5, w - 6, h - 6, arc, arc);

            Color fill = hover ? new Color(0, 0, 0, 175) : new Color(0, 0, 0, 140);
            if (press) fill = new Color(0, 0, 0, 220);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 6, h - 6, arc, arc);

            g2.setColor(new Color(255, 255, 255, hover ? 95 : 65));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 8, h - 8, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
