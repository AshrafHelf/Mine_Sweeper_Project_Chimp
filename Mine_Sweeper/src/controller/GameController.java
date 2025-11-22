package controller;

import enums.CellType;
import enums.Difficulty;
import enums.MoveResult;
import model.Board;
import model.Cell;
import model.Player;

public class GameController {

    private final Difficulty difficulty;

    // two separate boards (same size; ideally same mine layout)
    private final Board board1;
    private final Board board2;

    private final Player player1;
    private final Player player2;
    private Player currentPlayer;

    // TEAM-SHARED state
    private int hearts;       // shared pool of lives
    private int score;        // shared score
    private boolean gameOver;

    public GameController(Difficulty difficulty, String name1, String name2) {
        this.difficulty = difficulty;
        this.player1 = new Player(name1);
        this.player2 = new Player(name2);
        this.currentPlayer = player1; // player1 starts

        // create two boards of the same size.
        // NOTE: for the assignment they should be "identical" – same mine positions.
        // Easiest way is to use a BoardFactory that clones a base layout into two boards.
        this.board1 = new Board(this.difficulty);
        this.board2 = new Board(this.difficulty);

        this.hearts = difficulty.startingHearts;
        this.score = 0;
        this.gameOver = false;
    }

    /**
     * Called by the UI when the current player clicks a cell (row, col)
     * on THEIR board.
     */
    public void onCellClicked(int row, int col) {
        if (gameOver) {
            return;
        }

        Board currentBoard = getBoardFor(currentPlayer);

        MoveResult result = currentBoard.revealCell(row, col);

        switch (result) {
            case INVALID:
                // ignore; usually you *don't* switch turns on an invalid move
                return;

            case SAFE:
                // basic rule: +1 point per successful reveal (you can refine with spec rules)
                score += 1;
                break;

            case MINE:
                // stepped on a mine: lose one shared heart
                hearts--;
                // spec says revealing via *question reward* doesn't give points;
                // here it's a normal step-on-mine, so no points added.
                break;
		default:
			break;
        }

        // Check end conditions
        if (hearts <= 0 || allBoardsFinished()) {
            gameOver = true;
            // here you might trigger end-of-game screen via UI
            return;
        }

        // Otherwise, advance to the next player's turn
        switchTurn();
    }

    /**
     * Determines if both boards are "finished".
     * You can define "finished" as:
     *  - all non-mine cells revealed, OR
     *  - all mines flagged, depending on your requirement.
     */
    private boolean allBoardsFinished() {
        return boardFinished(board1) && boardFinished(board2);
    }

    private boolean boardFinished(Board board) {
        // simple version: all non-mine cells are revealed
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.getType() != CellType.MINE && !cell.isRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private Board getBoardFor(Player p) {
        return (p == player1) ? board1 : board2;
    }

    // ---------- Getters for the UI ----------

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Board getBoard1() {
        return board1;
    }

    public Board getBoard2() {
        return board2;
    }

    public int getHearts() {
        return hearts;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}