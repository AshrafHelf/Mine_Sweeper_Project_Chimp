package view;

import javax.swing.*;
import model.Board;
import model.Cell;
import model.CellType;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class BoardView extends JPanel {

    private static final long serialVersionUID = 1L;

    private final int cols;
    private final int rows;
    private final JButton[][] buttons;

    private BiConsumer<Integer, Integer> onCellClick;       // left click = reveal
    private BiConsumer<Integer, Integer> onCellRightClick;  // right click = flag

    // ---- Icons (cached) ----
    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    // Your resource paths
    private static final String ICON_FLAG   = "/images/flag.png";
    private static final String ICON_BANANA = "/images/banana.png";

    // Visuals
    private static final Color CELL_HIDDEN_BG   = new Color(23, 35, 74);
    private static final Color CELL_REVEALED_BG = new Color(31, 46, 96);
    private static final Color CELL_FLAG_BG     = new Color(160, 60, 60);
    private static final Color CELL_MINE_BG     = new Color(120, 85, 0);   // banana-ish
    private static final Color BORDER           = new Color(28, 39, 80);

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
                cellBtn.setPreferredSize(new Dimension(28, 28)); // slightly bigger than before
                cellBtn.setMargin(new Insets(0, 0, 0, 0));
                cellBtn.setFocusPainted(false);
                cellBtn.setBorder(BorderFactory.createLineBorder(BORDER));
                cellBtn.setBackground(CELL_HIDDEN_BG);
                cellBtn.setForeground(Color.WHITE);
                cellBtn.setFont(cellBtn.getFont().deriveFont(Font.BOLD, 13f));
                cellBtn.setHorizontalAlignment(SwingConstants.CENTER);
                cellBtn.setVerticalAlignment(SwingConstants.CENTER);

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
            }
        }
    }

    public void renderBoard(Board board) {
        if (board == null) return;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.get(c, r);
                JButton btn = buttons[r][c];

                // Reset common state (important)
                btn.setText("");
                btn.setIcon(null);
                btn.setToolTipText(null);

                if (cell == null) {
                    btn.setBackground(CELL_HIDDEN_BG);
                    continue;
                }

                // Pick icon size based on the current button size.
                // (Works even if you later change preferredSize.)
                int iconSize = btn.getPreferredSize().width - 2;

                if (!cell.isRevealed()) {
                    // HIDDEN CELL
                    if (cell.isFlagged()) {
                        btn.setBackground(CELL_FLAG_BG);
                        btn.setIcon(loadScaledIcon(ICON_FLAG, iconSize));
                        btn.setToolTipText("Flag");
                    } else {
                        btn.setBackground(CELL_HIDDEN_BG);
                    }
                    btn.setForeground(Color.WHITE);
                    continue;
                }

                // REVEALED CELL
                CellType t = cell.getType();
                switch (t) {
                    case MINE -> {
                        btn.setBackground(CELL_MINE_BG);
                        btn.setIcon(loadScaledIcon(ICON_BANANA, iconSize + 12)); // slightly bigger
                        btn.setToolTipText("Mine (Banana)");
                    }
                    case NUMBER -> {
                        int n = cell.getAdjacentMines();
                        btn.setBackground(CELL_REVEALED_BG);
                        btn.setText(String.valueOf(n));
                        btn.setForeground(colorForNumber(n));
                    }
                    case EMPTY -> {
                        btn.setBackground(CELL_REVEALED_BG);
                    }
                    case SURPRISE -> {
                        if (cell.isUsedSpecial()) {
                            btn.setBackground(new Color(90, 90, 90));
                            btn.setText("S✓");
                            btn.setForeground(Color.WHITE);
                            btn.setToolTipText("Surprise (Used)");
                        } else {
                            btn.setBackground(new Color(186, 85, 211));
                            btn.setText("S");
                            btn.setForeground(Color.WHITE);
                            btn.setToolTipText("Surprise");
                        }
                    }
                    case QUESTION -> {
                        if (cell.isUsedSpecial()) {
                            btn.setBackground(new Color(90, 90, 90));
                            btn.setText("Q✓");
                            btn.setForeground(Color.WHITE);
                            btn.setToolTipText("Question (Used)");
                        } else {
                            btn.setBackground(new Color(255, 215, 0));
                            btn.setText("Q");
                            btn.setForeground(Color.BLACK);
                            btn.setToolTipText("Question");
                        }
                    }
                }
            }
        }
    }

    // ---------- Icon helpers (safe for JAR) ----------

    private static ImageIcon loadScaledIcon(String classpath, int size) {
        String key = classpath + "#" + size;

        ImageIcon cached = ICON_CACHE.get(key);
        if (cached != null) return cached;

        URL url = BoardView.class.getResource(classpath);
        if (url == null) {
            // Resource missing → return a transparent placeholder so UI doesn't crash
            ImageIcon placeholder = new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
            ICON_CACHE.put(key, placeholder);
            return placeholder;
        }

        ImageIcon base = new ImageIcon(url);
        Image scaled = base.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        ImageIcon out = new ImageIcon(scaled);
        ICON_CACHE.put(key, out);
        return out;
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
