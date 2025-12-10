package model;

import java.util.ArrayDeque;
import java.util.Queue;

public class CascadeService {

    /**
     * Reveal empty-area cells starting from (startCol, startRow).
     * Expands through EMPTY cells and reveals any non-mine neighbors:
     *  - NUMBER
     *  - QUESTION
     *  - SURPRISE
     * Mines are never revealed by cascade.
     */
    public void cascadeReveal(Board board, int startCol, int startRow) {
        int rows = board.getRows();
        int cols = board.getCols();

        boolean[][] visited = new boolean[rows][cols];
        Queue<Coordinate> queue = new ArrayDeque<>();

        queue.add(new Coordinate(startCol, startRow));

        while (!queue.isEmpty()) {
            Coordinate cur = queue.remove();
            int c = cur.col();
            int r = cur.row();

            if (!board.inBounds(c, r)) continue;
            if (visited[r][c]) continue;
            visited[r][c] = true;

            Cell cell = board.get(c, r);

            // skip if already revealed or flagged
            if (cell.isRevealed() || cell.isFlagged()) continue;

            // never auto-reveal mines
            if (cell.getType() == CellType.MINE) continue;

            // reveal any NON-MINE cell (EMPTY, NUMBER, QUESTION, SURPRISE)
            cell.setRevealed(true);

            // if EMPTY, expand to neighbors
            if (cell.getType() == CellType.EMPTY || cell.getType()==CellType.QUESTION) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nc = c + dc;
                        int nr = r + dr;

                        if (board.inBounds(nc, nr) && !visited[nr][nc]) {
                            queue.add(new Coordinate(nc, nr));
                        }
                    }
                }
            }
        }
    }
}
