package view;

import javax.swing.*;
import model.Board;
import model.Cell;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class BoardView extends JPanel {

    private static final long serialVersionUID = 1L;

    // ===== Premium sizing =====
    // If you change TILE, EVERYTHING stays consistent (icons, fonts, layout)
    private static final int TILE = 42;
    private static final int GAP  = 1; // 0 looks too "stuck". 1 looks premium + still tight.
    private static final Dimension CELL_SIZE = new Dimension(TILE, TILE);

    // Icon should fill the tile without clipping the rounded border
    private static final int ICON_PAD = 2;                 // breathing room inside tile
    private static final int ICON_SIZE = TILE - ICON_PAD;  // was TILE-6, but our paint now uses full tile -> perfect

    private final int cols;
    private final int rows;
    private final TileButton[][] buttons;

    private BiConsumer<Integer, Integer> onCellClick;
    private BiConsumer<Integer, Integer> onCellRightClick;

    // Icons cached via UiAssets (theme icons)
    private final ImageIcon flagIcon = safeIcon("bananaflag.png", ICON_SIZE, ICON_SIZE);
    private final ImageIcon mineIcon = safeIcon("mine1.png", ICON_SIZE, ICON_SIZE);
    // Optional: you can add more icons later (Q/S/used), but text is fine.

    // ===== Jungle palette =====
    private static final Color TILE_HIDDEN        = new Color(18, 30, 22);
    private static final Color TILE_HIDDEN_BORDER = new Color(55, 95, 70);

    private static final Color TILE_REVEALED        = new Color(32, 48, 34);
    private static final Color TILE_REVEALED_BORDER = new Color(85, 130, 100);

    private static final Color TILE_USED        = new Color(70, 70, 70);
    private static final Color TILE_USED_BORDER = new Color(150, 150, 150, 120);

    private static final Color TILE_Q        = new Color(195, 165, 40);
    private static final Color TILE_Q_BORDER = new Color(255, 235, 170);

    private static final Color TILE_S        = new Color(120, 70, 160);
    private static final Color TILE_S_BORDER = new Color(210, 170, 255);

    public BoardView(int cols, int rows, String ignoredTitle) {
        this.cols = cols;
        this.rows = rows;
        this.buttons = new TileButton[rows][cols];

        setOpaque(false);
        setLayout(new GridLayout(rows, cols, GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        buildCells();
    }

    private void buildCells() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                final int cc = c;
                final int rr = r;

                TileButton btn = new TileButton(CELL_SIZE);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setEnabled(true);
                btn.setRolloverEnabled(true);

                // IMPORTANT: remove any internal padding that can shrink icons/text
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setIconTextGap(0);

                styleHidden(btn);

                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!btn.isEnabled()) return;

                        if (SwingUtilities.isRightMouseButton(e)) {
                            if (onCellRightClick != null) onCellRightClick.accept(cc, rr);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            if (onCellClick != null) onCellClick.accept(cc, rr);
                        }
                    }
                });

                buttons[r][c] = btn;
                add(btn);
            }
        }
    }

    // ===== Controller hooks =====
    public void setOnCellClick(BiConsumer<Integer, Integer> listener) { this.onCellClick = listener; }
    public void setOnCellRightClick(BiConsumer<Integer, Integer> listener) { this.onCellRightClick = listener; }

    public void setBoardEnabled(boolean enabled) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                TileButton b = buttons[r][c];
                b.setEnabled(enabled);
                b.setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                b.setDimmed(!enabled);
            }
        }
        repaint();
    }

    public void renderBoard(Board board) {
        if (board == null) return;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                Cell cell = board.get(c, r);
                TileButton btn = buttons[r][c];

                // reset
                btn.setText("");
                btn.setIcon(null);
                btn.setTextColor(Color.WHITE);
                btn.setTextSize(14);
                btn.setBold(true);

                if (cell == null) {
                    styleHidden(btn);
                    continue;
                }

                if (!cell.isRevealed()) {
                    if (cell.isFlagged()) {
                        styleFlagged(btn);
                        setFullIcon(btn, flagIcon);
                    } else {
                        styleHidden(btn);
                    }
                } else {
                    switch (cell.getType()) {

                        case MINE -> {
                            styleMine(btn);
                            setFullIcon(btn, mineIcon);
                        }

                        case NUMBER -> {
                            styleRevealed(btn);
                            int n = cell.getAdjacentMines();
                            if (n > 0) {
                                btn.setText(String.valueOf(n));
                                btn.setTextColor(colorForNumber(n));
                                btn.setTextSize(16);
                            } else {
                                btn.setText("");
                            }
                        }

                        case EMPTY -> styleRevealed(btn);

                        case QUESTION -> {
                            if (cell.isUsedSpecial()) {
                                styleUsed(btn);
                                btn.setText("×");
                                btn.setTextSize(22);
                            } else {
                                styleQuestion(btn);
                                btn.setText("Q");
                                btn.setTextColor(Color.BLACK);
                                btn.setTextSize(16);
                            }
                        }

                        case SURPRISE -> {
                            if (cell.isUsedSpecial()) {
                                styleUsed(btn);
                                btn.setText("×");
                                btn.setTextSize(22);
                            } else {
                                styleSurprise(btn);
                                btn.setText("S");
                                btn.setTextSize(16);
                            }
                        }
                    }
                }

                // Hard-lock size again (prevents any expansion bugs)
                btn.lockSize();
            }
        }

        revalidate();
        repaint();
    }

    // ===== Icon helper (fixes "icon smaller than tile") =====
    // Ensures icon is always scaled to the same logical size, not button's transient width/height.
    private void setFullIcon(JButton btn, ImageIcon base) {
        if (base == null || base.getIconWidth() <= 0) {
            btn.setIcon(null);
            return;
        }
        btn.setIcon(base);
        btn.setText("");
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setIconTextGap(0);
    }

    private static ImageIcon safeIcon(String name, int w, int h) {
        ImageIcon ic = UiAssets.icon(name, w, h);
        if (ic == null || ic.getIconWidth() <= 0) {
            System.out.println("⚠ Missing icon: img/" + name);
            return null;
        }
        return ic;
    }

    // ===== Styles =====
    private void styleHidden(TileButton b) {
        b.setTileColors(TILE_HIDDEN, TILE_HIDDEN_BORDER);
    }

    private void styleRevealed(TileButton b) {
        b.setTileColors(TILE_REVEALED, TILE_REVEALED_BORDER);
    }

    private void styleFlagged(TileButton b) {
        b.setTileColors(new Color(26, 44, 32), new Color(120, 185, 150));
    }

    private void styleMine(TileButton b) {
        b.setTileColors(new Color(70, 18, 18), new Color(190, 90, 90));
    }

    private void styleUsed(TileButton b) {
        b.setTileColors(TILE_USED, TILE_USED_BORDER);
    }

    private void styleQuestion(TileButton b) {
        b.setTileColors(TILE_Q, TILE_Q_BORDER);
    }

    private void styleSurprise(TileButton b) {
        b.setTileColors(TILE_S, TILE_S_BORDER);
    }

    private Color colorForNumber(int n) {
        return switch (n) {
            case 1 -> new Color(140, 210, 255);
            case 2 -> new Color(150, 255, 170);
            case 3 -> new Color(255, 150, 150);
            case 4 -> new Color(220, 170, 255);
            case 5 -> new Color(255, 210, 130);
            default -> Color.WHITE;
        };
    }

    // =========================
    // Premium tile button
    // =========================
    private static class TileButton extends JButton {
        private final Dimension size;
        private Color fill = new Color(0, 0, 0);
        private Color stroke = new Color(255, 255, 255, 40);
        private boolean dimmed = false;

        TileButton(Dimension fixed) {
            super("");
            this.size = fixed;

            // hard lock
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);

            setFont(new Font("SansSerif", Font.BOLD, 14));
            setForeground(Color.WHITE);

            // remove UI padding
            setMargin(new Insets(0, 0, 0, 0));
            setIconTextGap(0);
        }

        void lockSize() {
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        void setTileColors(Color fill, Color stroke) {
            this.fill = fill;
            this.stroke = stroke;
            repaint();
        }

        void setTextColor(Color c) { setForeground(c); }
        void setBold(boolean on) { setFont(getFont().deriveFont(on ? Font.BOLD : Font.PLAIN, getFont().getSize2D())); }
        void setTextSize(int px) { setFont(getFont().deriveFont((float) px)); }

        void setDimmed(boolean dim) {
            this.dimmed = dim;
            repaint();
        }

        @Override public Dimension getPreferredSize() { return size; }
        @Override public Dimension getMinimumSize() { return size; }
        @Override public Dimension getMaximumSize() { return size; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 12;

            // Shadow (outside tile, does not shrink content)
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(2, 3, w - 2, h - 2, arc, arc);

            // Tile fill (FULL tile area)
            Color f = fill;
            boolean hover = getModel().isRollover() && isEnabled() && !dimmed;
            if (hover) {
                f = new Color(
                        Math.min(255, fill.getRed() + 10),
                        Math.min(255, fill.getGreen() + 10),
                        Math.min(255, fill.getBlue() + 10),
                        fill.getAlpha()
                );
            }
            if (dimmed) f = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 95);

            g2.setColor(f);
            g2.fillRoundRect(0, 0, w - 3, h - 3, arc, arc);

            // Stroke (inside)
            Color s = dimmed ? new Color(255, 255, 255, 25) : stroke;
            g2.setColor(s);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 5, h - 5, arc, arc);

            // subtle highlight
            if (!dimmed) {
                g2.setColor(new Color(255, 255, 255, 16));
                g2.drawRoundRect(2, 2, w - 7, h - 7, arc, arc);
            }

            g2.dispose();

            // Paint icon/text after our tile
            super.paintComponent(g);
        }
    }
}
