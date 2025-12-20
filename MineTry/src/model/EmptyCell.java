package model;

import enums.CellType;

public class EmptyCell extends Cell {

    public EmptyCell(Coordinate at) {
        super(at);
    }

    @Override
    public CellType getType() {
        return CellType.EMPTY;
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
