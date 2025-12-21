package junit;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import controller.GameEngine;
import enums.Difficulty;
import model.Board;
import model.BoardGenerator;
import model.Cell;
import model.Game;

public class ToggleFlagRevealedCellTest {

    private GameEngine engine;
    private Game game;
    private Board board;
    private Cell cell;

    @Before
    public void setUp() {
        BoardGenerator gen = new BoardGenerator();
        engine = new GameEngine(gen, null);
        game = engine.newGame(Difficulty.EASY, "p1", "p2");

        board = game.getBoardFor(game.getActivePlayer());
        cell = board.get(0, 0);

        // common precondition for all tests
        cell.setRevealed(true);
    }

    @Test
    public void toggleFlag_revealedCell_flagStateUnchanged() {
        boolean flaggedBefore = cell.isFlagged();

        engine.toggleFlag(game, 0, 0);

        assertEquals(flaggedBefore, cell.isFlagged());
    }

    @Test
    public void toggleFlag_revealedCell_scoreNotChanged() {
        int scoreBefore = game.getTeamScore();

        engine.toggleFlag(game, 0, 0);

        assertEquals(scoreBefore, game.getTeamScore());
    }

    @Test
    public void toggleFlag_revealedCell_multipleToggles_noEffect() {
        boolean flaggedBefore = cell.isFlagged();

        engine.toggleFlag(game, 0, 0);
        engine.toggleFlag(game, 0, 0);
        engine.toggleFlag(game, 0, 0);

        assertEquals(flaggedBefore, cell.isFlagged());
    }

    @Test
    public void toggleFlag_revealedCell_remainsRevealed() {
        engine.toggleFlag(game, 0, 0);

        assertTrue(cell.isRevealed());
    }
}
