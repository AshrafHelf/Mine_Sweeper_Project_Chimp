package junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import controller.GameEngine;
import enums.Difficulty;
import enums.GameState;
import model.BoardGenerator;
import model.Game;
import model.Player;
import enums.QuestionLevel;
import model.*;


public class GameEngineLivesTest {


    BoardGenerator boardGen = new BoardGenerator();
    GameEngine engine = new GameEngine(boardGen, null);
    

    // ✅ your helper
    public Game newGame(Difficulty diff, String p1Name, String p2Name) {
        Player p1 = new Player(p1Name, 0);
        Player p2 = new Player(p2Name, 0);

        Game g = new Game(diff, p1, p2);
        g.setBoards(boardGen.generate(diff), boardGen.generate(diff));
        g.setTeamLives(diff.lives);
        g.setTeamScore(0);
        g.setWon(false);
        g.setState(GameState.RUNNING);
        return g;
    }

    @Test
    public void startsWithDifficultyLives() {
        Game game = newGame(Difficulty.EASY, "P1", "P2");

        assertEquals( "EASY difficulty should start with 10 lives",10, game.getTeamLives()
               );
    }

    @Test
    public void addLivesBelowMax_capsAt10() {
        Game game = newGame(Difficulty.HARD, "P1", "P2");
        game.setTeamLives(8);

        // HARD + EXPERT + correct => +3 hearts (deterministic)
        engine.applyQuestionOutcome(game, QuestionLevel.EXPERT, true);

        assertEquals( "Lives should increase up to the maximum (10)",10, game.getTeamLives()
               );
    }

    @Test
   public void addLivesAbove10_convertsExtraToPoints() {
        Game game = newGame(Difficulty.HARD, "P1", "P2");
        game.setTeamLives(9);
        game.setTeamScore(0);

        engine.applyQuestionOutcome(game, QuestionLevel.EXPERT, true);
        // +3 hearts => 9 -> 12 => cap at 10, extra = 2 => 10 points

        assertEquals( "Lives must be capped at 10,",10, game.getTeamLives()
               );

        assertEquals("Extra lives should be converted to bonus points",50, game.getTeamScore()
                );
    }

    @Test
    public void loseLivesStillRunning() {
        Game game = newGame(Difficulty.HARD, "P1", "P2");
        game.setTeamLives(5);

        engine.applyQuestionOutcome(game, QuestionLevel.EXPERT, false);
        // -3 hearts

        assertEquals(2, game.getTeamLives());
        assertEquals( "Game should continue while lives are above zero",GameState.RUNNING, game.getState()
               );
    }

    @Test
    public void loseLivesToZero_endsGame() {
        Game game = newGame(Difficulty.HARD, "P1", "P2");
        game.setTeamLives(2);

        engine.applyQuestionOutcome(game, QuestionLevel.EXPERT, false);
        // -3 hearts => <= 0

        assertEquals(0, game.getTeamLives());
        assertEquals( "Game must end when lives reach zero",GameState.OVER, game.getState()
               );
    }
}
