package model;

import enums.CellType;

public class QuestionCell extends EmptyCell {
    public QuestionCell(Coordinate at) {
        super(at);
    }

    @Override
    public CellType getType() {
        return CellType.QUESTION;
    }
    @Override 
    public int scoreforReveal() {
    	return 1;
    }
}