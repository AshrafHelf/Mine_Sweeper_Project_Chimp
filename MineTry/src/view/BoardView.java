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

    private final int cols;
    private final int rows;
    private final JButton[][] buttons;

    private BiConsumer<Integer, Integer> onCellClick;       // left click = reveal
    private BiConsumer<Integer, Integer> onCellRightClick;  // right click = flag

    // Icons
    private final ImageIcon baseFlag = new ImageIcon(getClass().getClassLoader().getResource("img/flag.png"));
    private final ImageIcon baseMine = new ImageIcon(getClass().getClassLoader().getResource("img/mine.png"));


    // Jungle-ish palette
    private static final Color TILE_HIDDEN = new Color(20, 32, 24);
    private static final Color TILE_HIDDEN_BORDER = new Color(35, 60, 45);

    private static final Color TILE_REVEALED = new Color(34, 52, 36);
    private static final Color TILE_REVEALED_BORDER = new Color(60, 90, 70);

    private static final Color TILE_USED = new Color(70, 70, 70);
    private static final Color TILE_Q = new Color(195, 165, 40);
    private static final Color TILE_S = new Color(120, 70, 160);

    public BoardView(int cols, int rows, String title) {
        this.cols = cols;
        this.rows = rows;
        this.buttons = new JButton[rows][cols];

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(235, 235, 245));
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(rows, cols, 3, 3));
        grid.setOpaque(false);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                JButton cellBtn = new JButton();
                cellBtn.setPreferredSize(new Dimension(28, 28));
                cellBtn.setMargin(new Insets(0, 0, 0, 0));
                cellBtn.setFocusPainted(false);
                cellBtn.setOpaque(true);
                cellBtn.setContentAreaFilled(true);

                styleHidden(cellBtn);

                final int cc = c;
                final int rr = r;

                cellBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!cellBtn.isEnabled()) return;

                        if (SwingUtilities.isRightMouseButton(e)) {
                            if (onCellRightClick != null) onCellRightClick.accept(cc, rr);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            if (onCellClick != null) onCellClick.accept(cc, rr);
                        }
                    }
                });

                buttons[r][c] = cellBtn;
                grid.add(cellBtn);
            }
        }

        add(grid, BorderLayout.CENTER);
    }

    public void setOnCellClick(BiConsumer<Integer, Integer> listener) {
        this.onCellClick = listener;
    }

    public void setOnCellRightClick(BiConsumer<Integer, Integer> listener) {
        this.onCellRightClick = listener;
    }

    public void setBoardEnabled(boolean enabled) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                buttons[r][c].setEnabled(enabled);
                buttons[r][c].setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        }
    }
    private void setFullIcon(JButton btn, ImageIcon baseIcon) {
        if (baseIcon == null) {
            btn.setIcon(null);
            return;
        }

        int w = Math.max(1, btn.getWidth());
        int h = Math.max(1, btn.getHeight());

        // If not laid out yet, fallback to preferred size
        if (w <= 1 || h <= 1) {
            Dimension pref = btn.getPreferredSize();
            w = Math.max(1, pref.width);
            h = Math.max(1, pref.height);
        }

        Image img = baseIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        btn.setIcon(new ImageIcon(img));

        btn.setText("");
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);

        btn.setIconTextGap(0);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setBorderPainted(false); // optional if you want clean tiles
        btn.setContentAreaFilled(true);
    }

    public void renderBoard(Board board) {
        if (board == null) return;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.get(c, r);
                JButton btn = buttons[r][c];

                // reset
                btn.setText("");
                btn.setIcon(null);

                if (cell == null) {
                    styleHidden(btn);
                    continue;
                }

                if (!cell.isRevealed()) {
                    // HIDDEN
                    if (cell.isFlagged()) {
                        styleFlagged(btn);

                        // ✅ FULL-CELL FLAG ICON
                        setFullIcon(btn, baseFlag);
                    } else {
                        styleHidden(btn);
                    }

                } else {
                    // REVEALED
                    switch (cell.getType()) {
                        case MINE -> {
                            styleMine(btn);

                            // ✅ FULL-CELL MINE ICON
                            setFullIcon(btn, baseMine);
                        }

                        case NUMBER -> {
                            int n = cell.getAdjacentMines();
                            styleRevealed(btn);
                            btn.setText(String.valueOf(n));
                            btn.setForeground(colorForNumber(n));
                        }

                        case EMPTY -> {
                            styleRevealed(btn);
                        }

                        case SURPRISE -> {
                            if (cell.isUsedSpecial()) {
                                styleUsed(btn);
                                btn.setText("USED");
                            } else {
                                styleSurprise(btn);
                                btn.setText("S");
                            }
                        }

                        case QUESTION -> {
                            if (cell.isUsedSpecial()) {
                                styleUsed(btn);
                                btn.setText("USED");
                            } else {
                                styleQuestion(btn);
                                btn.setText("Q");
                            }
                        }
                    }
                }
            }
        }
    }


    private void styleHidden(JButton b) {
        b.setBackground(TILE_HIDDEN);
        b.setBorder(BorderFactory.createLineBorder(TILE_HIDDEN_BORDER));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
    }

    private void styleRevealed(JButton b) {
        b.setBackground(TILE_REVEALED);
        b.setBorder(BorderFactory.createLineBorder(TILE_REVEALED_BORDER));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
    }

    private void styleFlagged(JButton b) {
        b.setBackground(new Color(30, 50, 35));
        b.setBorder(BorderFactory.createLineBorder(new Color(90, 140, 110)));
        b.setIcon(baseFlag);
    }

    private void styleMine(JButton b) {
        b.setBackground(new Color(70, 20, 20));
        b.setBorder(BorderFactory.createLineBorder(new Color(150, 60, 60)));
        b.setIcon(baseMine);
    }

    private void styleUsed(JButton b) {
        b.setBackground(TILE_USED);
        b.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120)));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 10f));
    }

    private void styleQuestion(JButton b) {
        b.setBackground(TILE_Q);
        b.setBorder(BorderFactory.createLineBorder(new Color(235, 210, 120)));
        b.setForeground(Color.BLACK);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
    }

    private void styleSurprise(JButton b) {
        b.setBackground(TILE_S);
        b.setBorder(BorderFactory.createLineBorder(new Color(190, 140, 220)));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
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
}
