package junit;
import org.junit.Test;

import model.Cell;
import model.CellType;
import model.Coordinate;
import model.Difficulty;
import model.Game;
import model.GameEngine;
import model.Player;
import model.QuestionLevel;

import static org.junit.Assert.*;

// Adjust package if needed: e.g. package model;
public class GameEngineQuestionTest {

    private Game createGame(Difficulty diff, int lives, int score) {
        Player p1 = new Player("P1", 0);
        Player p2 = new Player("P2", 0);
        Game g = new Game(diff, p1, p2);
        g.setTeamLives(lives);
        g.setTeamScore(score);
        return g;
    }

    // ✅ J1 – EASY + EASY question, correct answer
    @Test
    public void testApplyQuestionOutcome_easyEasyCorrect() {
        GameEngine engine = new GameEngine(null, null, null, null, null);

        Game game = createGame(Difficulty.EASY, 3, 0);

        engine.applyQuestionOutcome(game, QuestionLevel.EASY, true);

        assertEquals("Score should increase by 3 points in EASY/EASY correct",
                     3, game.getTeamScore());
        assertEquals("Lives should increase by 1 in EASY/EASY correct",
                     4, game.getTeamLives());
    }

    // ✅ J2 – MEDIUM + EXPERT question, correct answer
    @Test
    public void testApplyQuestionOutcome_mediumExpertCorrect() {
        GameEngine engine = new GameEngine(null, null, null, null, null);

        Game game = createGame(Difficulty.MEDIUM, 3, 0);

        engine.applyQuestionOutcome(game, QuestionLevel.EXPERT, true);

        assertEquals("Score should increase by 20 points in MEDIUM/EXPERT correct",
                     20, game.getTeamScore());
        assertEquals("Lives should increase by 2 in MEDIUM/EXPERT correct",
                     5, game.getTeamLives());
    }

    // ✅ J3 – HARD + HARD question, correct answer
    @Test
    public void testApplyQuestionOutcome_hardHardCorrect() {
        GameEngine engine = new GameEngine(null, null, null, null, null);

        Game game = createGame(Difficulty.HARD, 4, 10);

        engine.applyQuestionOutcome(game, QuestionLevel.HARD, true);

        // HARD/HARD correct → +20 points, +2 lives (from your table)
        assertEquals("Score should increase by 20 points in HARD/HARD correct",
                     30, game.getTeamScore());
        assertEquals("Lives should increase by 2 in HARD/HARD correct",
                     6, game.getTeamLives());
    }

    // ✅ J4 – activateQuestion works only once on a QUESTION cell
    @Test
    public void testActivateQuestion_marksUsedAndNotTwice() {
        GameEngine engine = new GameEngine(null, null, null, null, null);

        Game game = createGame(Difficulty.EASY, 3, 0);

        // create a QUESTION cell
        Cell cell = new Cell(new Coordinate(0, 0), CellType.QUESTION, 0);
        cell.setRevealed(true);          // must be revealed
        // usedSpecial is assumed false by default

        // First activation – correct EASY/EASY
        engine.activateQuestion(game, cell, QuestionLevel.EASY, true);

        assertTrue("After first activation, cell should be marked as used",
                   cell.isUsedSpecial());
        assertEquals("After EASY/EASY correct, score should be 3",
                     3, game.getTeamScore());
        assertEquals("After EASY/EASY correct, lives should be 4",
                     4, game.getTeamLives());

        // Try to activate again – should have NO further effect
        engine.activateQuestion(game, cell, QuestionLevel.EASY, true);

        assertEquals("Score should not change on second activation",
                     3, game.getTeamScore());
        assertEquals("Lives should not change on second activation",
                     4, game.getTeamLives());
    }
}
