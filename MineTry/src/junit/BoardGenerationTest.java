package junit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


import enums.CellType;
import enums.Difficulty;
import enums.GameState;
import model.Board;
import model.BoardGenerator;
import model.Cell;
import model.Game;
import model.Player;





public class BoardGenerationTest {
	
	int MineCount1;
	int MineCount2;
	int QuestionCount1;
	int QuestionCount2;
	int SurpriseCount1;
	int SurpriseCount2;
	
	 @Before
	public  void setup() {
		 MineCount1=0;
		 MineCount2=0;
		 QuestionCount1=0;
		 QuestionCount2=0;
		 SurpriseCount1=0;
		 SurpriseCount2=0;
		}
	
	 BoardGenerator boardGen = new BoardGenerator();
	
	 
	public Game newGame(Difficulty diff, String p1Name, String p2Name) {
        Player p1 = new Player(p1Name, 0);
        Player p2 = new Player(p2Name, 0);

        Game g = new Game(diff, p1, p2);
        g.setBoards(boardGen.generate(diff), boardGen.generate(diff));

        // 🔹 use difficulty-based lives
        g.setTeamLives(diff.lives);

        g.setTeamScore(0);
        g.setWon(false);
        g.setState(GameState.RUNNING);
        Board b1 = g.getBoard1();
        Board b2 = g.getBoard2();
        for(int i=0 ;i < b1.getRows(); i++) {
    		for (int j=0; j<b1.getCols();j++) {
    			Cell c = b1.get(i, j);
    			if(c.getType()==CellType.MINE)
    				MineCount1++;
    			else if(c.getType()==CellType.QUESTION)
    				QuestionCount1++;
    			else if(c.getType()==CellType.SURPRISE)
    				SurpriseCount1++;
    	}
    	}
        for(int i=0 ;i < b2.getRows(); i++) {
    		for (int j=0; j<b2.getCols();j++) {
    			Cell c = b2.get(i, j);
    			if(c.getType()==CellType.MINE)
    				MineCount2++;
    			else if(c.getType()==CellType.QUESTION)
    				QuestionCount2++;
    			else if(c.getType()==CellType.SURPRISE)
    				SurpriseCount2++;
    	}
    	}
        
        
        return g;
        
    }
	 
	
	@Test
	public void BoardGenerationMineCountTest_Easay_Player1() {
		newGame(Difficulty.EASY,"test1", "test2");
		
		assertEquals("MineCount for Easy game should be 26 for Player 1",
                10, this.MineCount1);
	}
	@Test
	public void BoardGenerationMineCountTest_Easy_Player2() {
		newGame(Difficulty.EASY,"test1", "test2");
		
		assertEquals("MineCount for Easy game should be 26 for Player 2",
                10, this.MineCount2);
	}
	
	@Test
	public void BoardGenerationQuestionCountTest_Easy_Player1() {
		newGame(Difficulty.EASY,"test1", "test2");
		
		assertEquals("Question Count for Easy game should be 7 for Player 1",
                6, this.QuestionCount1);
	}
	
	@Test
	public void BoardGenerationQuestionCountTest_Easy_Player2() {
		newGame(Difficulty.EASY,"test1", "test2");
		
		assertEquals("Question Count for Easy game should be 7 for Player 2",
                6, this.QuestionCount2);
	}
	@Test
	public void BoardGenerationSurpriseCountTest_Easy_Player1() {
		newGame(Difficulty.EASY,"test1", "test2");
		
		assertEquals("Question Count for Easy game should be 2 for Player 1",
                2, this.SurpriseCount1);
	}
	@Test
	public void BoardGenerationSurpriseCountTest_Easy_Player2() {
		newGame(Difficulty.EASY,"test1", "test2");
		
		assertEquals("Surprise Count for Easy game should be 3 for Player 2",
                2, this.SurpriseCount2);
	}
	
	@Test
	public void BoardGenerationMineCountTest_Medium_Player1() {
		newGame(Difficulty.MEDIUM,"test1", "test2");
		
		assertEquals("MineCount for HARD game should be 26 for Player 1",
                26, this.MineCount1);
	}
	@Test
	public void BoardGenerationMineCountTest_Medium_Player2() {
		newGame(Difficulty.MEDIUM,"test1", "test2");
		
		assertEquals("MineCount for HARD game should be 26 for Player 2",
                26, this.MineCount2);
	}
	
	@Test
	public void BoardGenerationQuestionCountTest_Medium_Player1() {
		newGame(Difficulty.MEDIUM,"test1", "test2");
		
		assertEquals("Question Count for Medium game should be 7 for Player 1",
                7, this.QuestionCount1);
	}
	
	@Test
	public void BoardGenerationQuestionCountTest_Medium_Player2() {
		newGame(Difficulty.MEDIUM,"test1", "test2");
		
		assertEquals("Question Count for Medium game should be 7 for Player 2",
                7, this.QuestionCount2);
	}
	@Test
	public void BoardGenerationSurpriseCountTest_Medium_Player1() {
		newGame(Difficulty.MEDIUM,"test1", "test2");
		
		assertEquals("Question Count for Medium game should be 2 for Player 1",
                3, this.SurpriseCount1);
	}
	@Test
	public void BoardGenerationSurpriseCountTest_Medium_Player2() {
		newGame(Difficulty.MEDIUM,"test1", "test2");
		
		assertEquals("Surprise Count for Medium game should be 3 for Player 2",
                3, this.SurpriseCount2);
	}
	
	@Test
	public void BoardGenerationMineCountTest_Hard_Player1() {
		newGame(Difficulty.HARD,"test1", "test2");
		
		assertEquals("MineCount for HARD game should be 44 for Player 1",
                44, this.MineCount1);
	}
	@Test
	public void BoardGenerationMineCountTest_Hard_Player2() {
		newGame(Difficulty.HARD,"test1", "test2");
		
		assertEquals("MineCount for HARD game should be 44 for Player 2",
                44, this.MineCount2);
	}
	
	@Test
	public void BoardGenerationQuestionCountTest_Hard_Player1() {
		newGame(Difficulty.HARD,"test1", "test2");
		
		assertEquals("Question Count for HARD game should be 11 for Player 1",
                11, this.QuestionCount1);
	}
	
	@Test
	public void BoardGenerationQuestionCountTest_Hard_Player2() {
		newGame(Difficulty.HARD,"test1", "test2");
		
		assertEquals("Question Count for HARD game should be 11 for Player 2",
                11, this.QuestionCount2);
	}
	@Test
	public void BoardGenerationSurpriseCountTest_Hard_Player1() {
		newGame(Difficulty.HARD,"test1", "test2");
		
		assertEquals("Surprise Count for HARD game should be 4 for Player 1",
                4, this.SurpriseCount1);
	}
	@Test
	public void BoardGenerationSurpriseCountTest_Hard_Player2() {
		newGame(Difficulty.HARD,"test1", "test2");
		
		assertEquals("Surprise Count for HARD game should be 4 for Player 2",
                4, this.SurpriseCount2);
	}
	
	
	
	
	

}
