package model;

import enums.CellType;

public abstract class Cell {

    protected final Coordinate at;

    protected boolean revealed;
    protected boolean flagged;
    protected boolean usedSpecial;
    protected boolean isFlaggedscore;

    public boolean isFlaggedscore() {
		return isFlaggedscore;
	}

	public void setFlaggedscore(boolean isFlaggedscore) {
		this.isFlaggedscore = isFlaggedscore;
	}

	protected Cell(Coordinate at) {
        this.at = at;
        this.revealed = false;
        this.flagged = false;
        this.usedSpecial = false;
        this.isFlaggedscore=false;
    }

    public Coordinate getAt() {
        return at;
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

    public boolean isUsedSpecial() {
        return usedSpecial;
    }

    public void setUsedSpecial(boolean usedSpecial) {
        this.usedSpecial = usedSpecial;
    }

    
    public abstract CellType getType();
    public abstract int scoreforReveal();
    public abstract int scoreforFlag(boolean addedFlag);
    
    public int getAdjacentMines() {
        return 0;
    }
}
