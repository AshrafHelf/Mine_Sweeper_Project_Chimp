package model;


import model.*;

import java.util.random.RandomGenerator;

public class GameEngine {

    private static final int BONUS_PER_LIFE = 5; // TODO: later make per-difficulty

    private final BoardGenerator boardGen;
    private final CascadeService cascade;
    private final ScoringService scoring;
    private final TurnService turnService; // kept for future use
    private final HistoryService history;  // kept for future use

    public GameEngine(BoardGenerator boardGen,
                      CascadeService cascade,
                      ScoringService scoring,
                      TurnService turnService,
                      HistoryService history) {
        this.boardGen = boardGen;
        this.cascade = cascade;
        this.scoring = scoring;
        this.turnService = turnService;
        this.history = history;
    }

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


    // Reveal a cell on the active player's board
    public void reveal(Game game, int col, int row) {
        if (game.getState() != GameState.RUNNING) return;

        Player active = game.getActivePlayer();
        Board board = game.getBoardFor(active);

        if (!board.inBounds(col, row)) return;

        Cell cell = board.get(col, row);

        // cannot reveal flagged cell
        if (cell.isFlagged()) return;

        // --- Safe first click per board ---
        if (isFirstClickOnBoard(board) && cell.getType() == CellType.MINE) {
            makeCellSafeAndRebuildBoard(board, col, row);
            cell = board.get(col, row); // reload cell after rebuild
        }

        // --- Normal reveal logic ---
        switch (cell.getType()) {
            case MINE -> {
                cell.setRevealed(true);
                int lives = game.getTeamLives() - 1;
                game.setTeamLives(lives);

                if (lives <= 0) {
                    // lose on lives
                    finishGame(game, false);
                    return;
                }
            }
            case EMPTY -> {
                cascade.cascadeReveal(board, col, row);
                game.addToTeamScore(scoring.scoreForReveal(CellType.EMPTY));
            }
            case NUMBER -> {
                cell.setRevealed(true);
                game.addToTeamScore(scoring.scoreForReveal(CellType.NUMBER));
            }
            case SURPRISE, QUESTION -> {
                // For now they just reveal; special behavior later
                cell.setRevealed(true);
            }
        }

        // If still running after this move, check win condition on the active board
        if (game.getState() == GameState.RUNNING && boardCleared(board)) {
            finishGame(game, true);
        }
    }

    // Toggle flag on current player's board
    public void toggleFlag(Game game, int col, int row) {
        if (game.getState() != GameState.RUNNING) return;

        Board board = game.getBoardFor(game.getActivePlayer());
        if (!board.inBounds(col, row)) return;

        Cell cell = board.get(col, row);
        if (cell.isRevealed()) return;

        cell.setFlagged(!cell.isFlagged());
    }

    // ------------------ helpers ------------------

    private boolean isFirstClickOnBoard(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.get(c, r).isRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Board is "cleared" when there is NO cell that is both:
     *   - not revealed
     *   - not flagged
     *
     * i.e., all non-flag cells are revealed.
     */
    /**
     * Board is "cleared" when all NON-MINE cells are revealed.
     * Flags do NOT matter for win condition.
     */
    private boolean boardCleared(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.get(c, r);
                // If it's NOT a mine and still hidden → not cleared yet
                if (cell.getType() != CellType.MINE && !cell.isRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Used on the very first click of a board if the clicked cell was a mine.
     * We rebuild mine positions so that (safeCol,safeRow) is guaranteed safe.
     */
    private void makeCellSafeAndRebuildBoard(Board board, int safeCol, int safeRow) {
        int cols = board.getCols();
        int rows = board.getRows();

        boolean[][] mines = new boolean[rows][cols];

        // 1) reconstruct mine map from current board
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.get(c, r);
                if (cell.getType() == CellType.MINE) {
                    mines[r][c] = true;
                }
            }
        }

        // 2) remove mine from the clicked cell
        mines[safeRow][safeCol] = false;

        // 3) place that mine somewhere else
        RandomGenerator rng = Rng.current();
        while (true) {
            int c = rng.nextInt(cols);
            int r = rng.nextInt(rows);

            if (!mines[r][c] && !(c == safeCol && r == safeRow)) {
                mines[r][c] = true;
                break;
            }
        }

        // 4) rebuild all cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mines[r][c]) {
                    board.set(c, r, new Cell(new Coordinate(c, r), CellType.MINE, 0));
                } else {
                    int adj = countAdjacentMines(mines, c, r);
                    if (adj > 0) {
                        board.set(c, r, new Cell(new Coordinate(c, r), CellType.NUMBER, adj));
                    } else {
                        board.set(c, r, new Cell(new Coordinate(c, r), CellType.EMPTY, 0));
                    }
                }
            }
        }
    }

    private int countAdjacentMines(boolean[][] mines, int c, int r) {
        int rows = mines.length;
        int cols = mines[0].length;
        int count = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nc = c + dc;
                int nr = r + dr;
                if (nc >= 0 && nr >= 0 && nc < cols && nr < rows) {
                    if (mines[nr][nc]) count++;
                }
            }
        }
        return count;
    }

    // ---- End of game handling ----

    private void finishGame(Game game, boolean won) {
        game.setWon(won);

        // If team won, convert remaining lives to points
        if (won) {
            int lives = game.getTeamLives();
            if (lives > 0) {
                game.addToTeamScore(lives * BONUS_PER_LIFE);
            }
        }

        // lives are considered "used up" after conversion
        game.setTeamLives(0);

        // reveal all cells on both boards and clear flags
        revealAllBoards(game);

        game.setState(GameState.OVER);
    }

    private void revealAllBoards(Game game) {
        revealAllOnBoard(game.getBoard1());
        revealAllOnBoard(game.getBoard2());
    }

    private void revealAllOnBoard(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.get(c, r);
                cell.setFlagged(false);   // remove flags so real content is shown
                cell.setRevealed(true);
            }
        }
    }
}
