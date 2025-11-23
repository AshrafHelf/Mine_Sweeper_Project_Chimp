package model;

import java.util.ArrayDeque;
import java.util.Deque;

import model.Board;
import model.Cell;
import model.CellType;

public class CascadeService {

    public void cascadeReveal(Board board, int c, int r) {
        if (!board.inBounds(c, r)) {
            return;
        }

        Cell start = board.get(c, r);
        if (start.isRevealed() || start.getType() == CellType.MINE) {
            // If already revealed or a mine, just reveal it and stop
            start.setRevealed(true);
            return;
        }

        // Queue will hold coordinates [col, row]
        Deque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{c, r});

        while (!q.isEmpty()) {
            int[] pos = q.removeFirst();
            int cc = pos[0];
            int rr = pos[1];

            if (!board.inBounds(cc, rr)) continue;

            Cell cell = board.get(cc, rr);
            if (cell.isRevealed()) continue;

            cell.setRevealed(true);

            // Only expand from EMPTY cells (0 adjacent mines)
            if (cell.getType() == CellType.EMPTY) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dc == 0 && dr == 0) continue;

                        int nc = cc + dc;
                        int nr = rr + dr;

                        if (board.inBounds(nc, nr)) {
                            Cell neigh = board.get(nc, nr);

                            if (!neigh.isRevealed() && neigh.getType() != CellType.MINE) {
                                q.add(new int[]{nc, nr});
                            }
                        }
                    }
                }
            }
        }
    }
}
