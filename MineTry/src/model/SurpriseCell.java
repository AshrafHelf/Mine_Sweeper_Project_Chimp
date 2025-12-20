package model;

import enums.CellType;

public class SurpriseCell extends EmptyCell {
    public SurpriseCell(Coordinate at) {
        super(at);
    }

    @Override
    public CellType getType() {
        return CellType.SURPRISE;
    }
    @Override 
    public int scoreforReveal() {
    	return 1;
    }
}