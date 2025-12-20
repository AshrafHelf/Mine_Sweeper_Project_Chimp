package model;

import enums.CellType;

public class MineCell extends Cell {

    public MineCell(Coordinate at) {
        super(at);
    }

    @Override
    public CellType getType() {
        return CellType.MINE;
    }
    @Override
    public int scoreforReveal() {
    	return 0;
    }
    @Override
    public int scoreforFlag(boolean addedFlag) {
    	if(!addedFlag)
    		return 0;
    	return -3;
    	
    }
}
