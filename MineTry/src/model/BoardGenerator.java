package model;

import java.util.random.RandomGenerator;

import enums.CellType;
import enums.Difficulty;

import java.util.Random;

public class BoardGenerator {
	 private static final Random RNG = new Random();
    public Board generate(Difficulty d) {
        int cols = d.cols;
        int rows = d.rows;

        Board b = new Board(cols, rows);

        int mineCount      = d.mineCount;
        int questionCount  = d.questionCount;
        int surpriseCount  = d.surpriseCount;

        int totalCells = cols * rows;
        if (mineCount + questionCount + surpriseCount > totalCells) {
            throw new IllegalArgumentException("Too many special cells for board size");
        }

        boolean[][] mines     = new boolean[rows][cols];
        boolean[][] questions = new boolean[rows][cols];
        boolean[][] surprises = new boolean[rows][cols];

        RandomGenerator rng = BoardGenerator.current();

        // ---- 1. Place mines ----
        int placed = 0;
        while (placed < mineCount) {
            int c = rng.nextInt(cols);
            int r = rng.nextInt(rows);
            if (!mines[r][c]) {
                mines[r][c] = true;
                placed++;
            }
        }

     // ---- 2. Place question tiles (Q) on non-mine cells
//      that are NOT adjacent to any mine ----
placed = 0;
int safetyCounter = 0;
int maxAttempts = cols * rows * 20;  // just to avoid an infinite loop

while (placed < questionCount && safetyCounter < maxAttempts) {
  safetyCounter++;
  int c = rng.nextInt(cols);
  int r = rng.nextInt(rows);

  if (mines[r][c]) continue;
  if (questions[r][c] || surprises[r][c]) continue;

  // do not put Q next to a mine
  if (countAdjacentMines(mines, c, r) > 0) continue;

  questions[r][c] = true;
  placed++;
}

//---- 3. Place surprise tiles (S) on remaining free cells
//      that are NOT adjacent to any mine ----
placed = 0;
safetyCounter = 0;

while (placed < surpriseCount && safetyCounter < maxAttempts) {
  safetyCounter++;
  int c = rng.nextInt(cols);
  int r = rng.nextInt(rows);

  if (mines[r][c]) continue;
  if (questions[r][c] || surprises[r][c]) continue;

  // do not put S next to a mine
  if (countAdjacentMines(mines, c, r) > 0) continue;

  surprises[r][c] = true;
  placed++;
}


        // ---- 4. Build cells: mines, Q, S, then numbers/empties ----
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (mines[r][c]) {
                    b.set(c, r, new Cell(new Coordinate(c, r), CellType.MINE, 0));
                } else if (questions[r][c]) {
                    b.set(c, r, new Cell(new Coordinate(c, r), CellType.QUESTION, 0));
                } else if (surprises[r][c]) {
                    b.set(c, r, new Cell(new Coordinate(c, r), CellType.SURPRISE, 0));
                } else {
                    int adjMines = countAdjacentMines(mines, c, r);
                    if (adjMines > 0) {
                        b.set(c, r, new Cell(new Coordinate(c, r), CellType.NUMBER, adjMines));
                    } else {
                        b.set(c, r, new Cell(new Coordinate(c, r), CellType.EMPTY, 0));
                    }
                }
            }
        }

        return b;
    }

    private int countAdjacentMines(boolean[][] mines, int c, int r) {
        int rows = mines.length;
        int cols = mines[0].length;
        int count = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nc = c + dc;
                int nr = r + dr;
                if (nc >= 0 && nr >= 0 && nc < cols && nr < rows) {
                    if (mines[nr][nc]) count++;
                }
            }
        }
        return count;
    }
    public static Random current() {
        return RNG;
    }
}
