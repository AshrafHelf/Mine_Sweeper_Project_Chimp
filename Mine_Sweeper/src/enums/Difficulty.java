package enums;


	public enum Difficulty {
	    EASY(9, 9, 10,6,2,10),
	    MEDIUM(13, 13, 26,7,3,8),
	    HARD(16, 16, 44,11,4,6);

	    public final int rows;
	    public final int cols;
	    public final int mines;
	    public final int questions;
	    public final int surprise;
	    public final int startingHearts;

	    Difficulty(int rows, int cols, int mines, int questions, int surprise,int startingHeart) {
	        this.rows = rows;
	        this.cols = cols;
	        this.mines = mines;
	        this.questions = questions;
	        this.surprise = surprise;
	        this.startingHearts= startingHeart;
	    }
	}


