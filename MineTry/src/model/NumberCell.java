package model;

import enums.CellType;

public class NumberCell extends Cell {

    private final int adjacentMines;

    public NumberCell(Coordinate at, int adjacentMines) {
        super(at);
        this.adjacentMines = adjacentMines;
    }

    @Override
    public CellType getType() {
        return CellType.NUMBER;
    }

    @Override
    public int getAdjacentMines() {
        return adjacentMines;
    }
    @Override 
    public int scoreforReveal() {
    	return 1;
    }
    @Override
    public int scoreforFlag(boolean addedFlag) {
    	if(!addedFlag)
    		return 0;
    	return -3;
    	
    }
}
