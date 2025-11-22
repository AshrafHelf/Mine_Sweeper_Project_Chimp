package model;

import enums.CellType;

public class Cell {

    private CellType type;
    private boolean revealed;
    private boolean flagged;   
    private int adjacentMines;

    public Cell() {
        this.type = CellType.EMPTY;
        this.revealed = false;
        this.flagged = false;
        this.adjacentMines = 0;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void reveal() {
        this.revealed = true;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void toggleFlag() {
        this.flagged = !this.flagged;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int count) {
        this.adjacentMines = count;
    }
}