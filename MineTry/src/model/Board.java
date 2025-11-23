package model;

public class Board {

    private final int cols;
    private final int rows;
    private final Cell[][] grid;

    public Board(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.grid = new Cell[rows][cols];
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public Cell get(int c, int r) {
        return grid[r][c];
    }

    public void set(int c, int r, Cell cell) {
        grid[r][c] = cell;
    }

    public boolean inBounds(int c, int r) {
        return c >= 0 && r >= 0 && c < cols && r < rows;
    }
}
