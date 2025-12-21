package junit;

import static org.junit.Assert.*;
import org.junit.Test;

import controller.GameEngine;
import enums.Difficulty;
import enums.GameState;
import model.Board;
import model.BoardGenerator;
import model.Cell;
import model.Game;

public class ToggleFlagRevealedCellTest {

    @Test
    public void toggleFlag_revealedCell_noChange() {
        BoardGenerator gen = new BoardGenerator();
        GameEngine engine = new GameEngine(gen, null);
        Game game = engine.newGame(Difficulty.EASY, "p1", "p2");

        Board board = game.getBoardFor(game.getActivePlayer());
        Cell cell = board.get(0, 0);

        cell.setRevealed(true);

        boolean flaggedBefore = cell.isFlagged();
        int scoreBefore = game.getTeamScore();

        engine.toggleFlag(game, 0, 0);

        assertEquals(flaggedBefore, cell.isFlagged());
        assertEquals(scoreBefore, game.getTeamScore());
    }
}
