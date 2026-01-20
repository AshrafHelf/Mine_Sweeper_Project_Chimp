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
        if (!addedFlag) return 0;  // removing a flag doesn't change score (your current rule)
        return +1;                 // ✅ correct mine flag reward
    }

}
