package model;

public class Cell {

    private final CellType type;
    private final Coordinate at;     // <<< FIXED: Uses your own Coordinate type
    private final int adjacentMines;

    private boolean revealed;
    private boolean flagged;

    public Cell(Coordinate at, CellType type, int adjacentMines) {
        this.type = type;
        this.at = at;
        this.adjacentMines = adjacentMines;
        this.revealed = false;
        this.flagged = false;
    }

    public CellType getType() {
        return type;
    }

    public Coordinate getAt() {
        return at;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
        if (revealed) {
            this.flagged = false; // auto-remove flag if revealed
        }
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }
}
