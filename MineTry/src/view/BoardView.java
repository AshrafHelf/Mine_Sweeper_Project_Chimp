package view;

import javax.swing.*;

import model.Board;
import model.Cell;
import model.CellType;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class BoardView extends JPanel {

    private final int cols;
    private final int rows;
    private final JButton[][] buttons;

    private BiConsumer<Integer, Integer> onCellClick;       // left click = reveal
    private BiConsumer<Integer, Integer> onCellRightClick;  // right click = flag

    public BoardView(int cols, int rows, String title) {
        this.cols = cols;
        this.rows = rows;
        this.buttons = new JButton[rows][cols];

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(230, 230, 255));
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(rows, cols, 2, 2));
        grid.setBackground(new Color(15, 24, 54));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton cellBtn = new JButton();
                cellBtn.setPreferredSize(new Dimension(26, 26));
                cellBtn.setMargin(new Insets(0, 0, 0, 0));
                cellBtn.setFocusPainted(false);
                cellBtn.setBorder(BorderFactory.createLineBorder(new Color(28, 39, 80)));
                cellBtn.setBackground(new Color(23, 35, 74));
                cellBtn.setForeground(Color.WHITE);
                cellBtn.setFont(cellBtn.getFont().deriveFont(Font.BOLD, 12f));

                final int cc = c;
                final int rr = r;

                cellBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!cellBtn.isEnabled()) return;

                        if (SwingUtilities.isRightMouseButton(e)) {
                            if (onCellRightClick != null) {
                                onCellRightClick.accept(cc, rr);
                            }
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            if (onCellClick != null) {
                                onCellClick.accept(cc, rr);
                            }
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
            }
        }
    }

    public void renderBoard(Board board) {
        if (board == null) return;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.get(c, r);
                JButton btn = buttons[r][c];

                if (cell == null) {
                    btn.setText("");
                    btn.setBackground(new Color(23, 35, 74));
                    continue;
                }

                if (!cell.isRevealed()) {
                    // HIDDEN CELL
                    if (cell.isFlagged()) {
                        btn.setText("F");
                        btn.setBackground(new Color(200, 80, 80));
                        btn.setForeground(Color.WHITE);
                    } else {
                        btn.setText("");
                        btn.setBackground(new Color(23, 35, 74));
                        btn.setForeground(Color.WHITE);
                    }
                } else {
                    // REVEALED CELL
                    switch (cell.getType()) {
                        case MINE -> {
                            btn.setText("M");
                            btn.setBackground(new Color(139, 0, 0));
                            btn.setForeground(Color.WHITE);
                        }
                        case NUMBER -> {
                            int n = cell.getAdjacentMines();
                            btn.setText(String.valueOf(n));
                            btn.setBackground(new Color(31, 46, 96));
                            btn.setForeground(colorForNumber(n));
                        }
                        case EMPTY -> {
                            btn.setText("");
                            btn.setBackground(new Color(31, 46, 96));
                            btn.setForeground(Color.WHITE);
                        }
                        case SURPRISE -> {
                            btn.setText("S");
                            btn.setBackground(new Color(186, 85, 211));
                            btn.setForeground(Color.WHITE);
                        }
                        case QUESTION -> {
                            btn.setText("Q");
                            btn.setBackground(new Color(255, 215, 0));
                            btn.setForeground(Color.BLACK);
                        }
                    }
                }
            }
        }
    }

    private Color colorForNumber(int n) {
        return switch (n) {
            case 1 -> new Color(72, 163, 255);
            case 2 -> new Color(120, 200, 120);
            case 3 -> new Color(255, 99, 132);
            case 4 -> new Color(186, 85, 211);
            case 5 -> new Color(255, 165, 0);
            default -> Color.WHITE;
        };
    }
}
