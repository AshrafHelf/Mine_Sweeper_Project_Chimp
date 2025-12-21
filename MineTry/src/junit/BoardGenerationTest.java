package junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


import enums.Difficulty;
import enums.GameState;
import model.BoardGenerator;
import model.Game;
import model.Player;





public class BoardGenerationTest {
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
        return g;
    }
	
	@Test
	public void BoardGenerationMineCountTest_Easy() {
		Game game = newGame(Difficulty.EASY,"test1", "test2");
		
		assertEquals("MineCount for Easy game should be 10 for Player 1",
                10, game.getMineCountforplayer1());
		assertEquals("MineCount for Easy game should be 10 for Player 2",
                10, game.getMineCountforplayer2());
	}
	@Test
	public void BoardGenerationMineCountTest_Medium() {
		Game game = newGame(Difficulty.MEDIUM,"test1", "test2");
		
		assertEquals("MineCount for Meium game should be 26 for Player 1",
                26, game.getMineCountforplayer1());
		assertEquals("MineCount for Medium game should be 26 for Player 2",
                26, game.getMineCountforplayer2());
	}
	
	@Test
	public void BoardGenerationMineCountTest_Hard() {
		Game game = newGame(Difficulty.HARD,"test1", "test2");
		
		assertEquals("MineCount for HARD game should be 44 for Player 1",
                44, game.getMineCountforplayer1());
		assertEquals("MineCount for HARD game should be 26 for Player 2",
                44, game.getMineCountforplayer2());
	}
	

}
