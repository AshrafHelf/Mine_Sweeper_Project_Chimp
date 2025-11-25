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
    
    private int startingLivesFor(Difficulty diff) {
        return switch (diff) {
            case EASY   -> 10;  // “קחשמה ליחתמ עם 10 לבבות”
            case MEDIUM -> 8;   // “קחשמ בינוני … 8 לבבות”
            case HARD   -> 6;   // “קחשמ קשה … 6 לבבות”
        };
    }


    // Reveal a cell on the active player's board
    public void reveal(Game game, int col, int row) {
        if (game.getState() != GameState.RUNNING) return;

        Player active = game.getActivePlayer();
        Board board = game.getBoardFor(active);

        if (!board.inBounds(col, row)) return;

        Cell cell = board.get(col, row);

        // can't reveal flagged cells
        if (cell.isFlagged()) return;

        // 🔹 SAFE FIRST CLICK PER BOARD:
        // If no cells are revealed yet on this board and the clicked cell is a mine,
        // move that mine elsewhere and rebuild the board so this cell becomes safe.
        if (isFirstClickOnBoard(board) && cell.getType() == CellType.MINE) {
            makeCellSafeAndRebuildBoard(board, col, row);
            cell = board.get(col, row); // reload cell after rebuild
        }

        // Normal reveal logic
        switch (cell.getType()) {
            case MINE -> {
                cell.setRevealed(true);
                int lives = game.getTeamLives() - 1;
                game.setTeamLives(lives);

                if (lives <= 0) {
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
            case SURPRISE -> {
                // FIRST click on S: reveal like empty (+1 point), DO NOT activate yet
                cell.setRevealed(true);
                game.addToTeamScore(scoring.scoreForReveal(CellType.SURPRISE));
                // activation happens on SECOND click in GameController
            }
            case QUESTION -> {
                // FIRST click on Q: reveal like empty (+1 point), DO NOT open question yet
                cell.setRevealed(true);
                game.addToTeamScore(scoring.scoreForReveal(CellType.QUESTION));
                // activation (question dialog) happens on SECOND click in GameController
            }
        }

        // If still running, check if this board is cleared (win)
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

        boolean addingFlag = !cell.isFlagged();
        cell.setFlagged(addingFlag);

        int deltaScore = scoring.scoreForFlag(cell.getType(), addingFlag);
        game.addToTeamScore(deltaScore);
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
    
 // change team lives and end game if <= 0
    private void addLives(Game game, int delta) {
        int lives = game.getTeamLives() + delta;
        if (lives < 0) lives = 0;
        game.setTeamLives(lives);

        if (lives <= 0) {
            finishGame(game, false);
        }
    }

    // apply the big scoring table: depends on game difficulty + question level + correctness
    public void applyQuestionOutcome(Game game, QuestionLevel qLevel, boolean correct) {
        Difficulty gDiff = game.getDifficulty();
        RandomGenerator rng = Rng.current();

        int points = 0;
        int heartsDelta = 0;

        switch (gDiff) {
            case EASY -> {
                switch (qLevel) {
                    case EASY -> {
                        if (correct) {
                            points = 3;
                            heartsDelta = +1;
                        } else {
                            if (rng.nextBoolean()) {
                                points = -3;   // OR nothing
                            }
                        }
                    }
                    case MEDIUM -> {
                        if (correct) {
                            points = 6;
                            // TODO: reveal one mine cell somewhere
                        } else {
                            if (rng.nextBoolean()) {
                                points = -6;   // OR nothing
                            }
                        }
                    }
                    case HARD -> {
                        if (correct) {
                            points = 10;
                            // TODO: reveal a random 3x3 area
                        } else {
                            points = -10;
                        }
                    }
                    case EXPERT -> {
                        if (correct) {
                            points = 15;
                            heartsDelta = +2;
                        } else {
                            points = -15;
                            heartsDelta = -1;
                        }
                    }
                }
            }
            case MEDIUM -> {
                switch (qLevel) {
                    case EASY -> {
                        if (correct) {
                            points = 8;
                            heartsDelta = +1;
                        } else {
                            points = -8;
                        }
                    }
                    case MEDIUM -> {
                        if (correct) {
                            points = 10;
                            heartsDelta = +1;
                        } else {
                            // (-10pts & -1♥) OR nothing
                            if (rng.nextBoolean()) {
                                points = -10;
                                heartsDelta = -1;
                            }
                        }
                    }
                    case HARD -> {
                        if (correct) {
                            points = 15;
                            heartsDelta = +1;
                        } else {
                            points = -15;
                            heartsDelta = -1;
                        }
                    }
                    case EXPERT -> {
                        if (correct) {
                            points = 20;
                            heartsDelta = +2;
                        } else {
                            // either (-20 & -1♥) OR (-20 & -2♥)
                            points = -20;
                            heartsDelta = rng.nextBoolean() ? -1 : -2;
                        }
                    }
                }
            }
            case HARD -> {
                switch (qLevel) {
                    case EASY -> {
                        if (correct) {
                            points = 10;
                            heartsDelta = +1;
                        } else {
                            points = -10;
                            heartsDelta = -1;
                        }
                    }
                    case MEDIUM -> {
                        if (correct) {
                            points = 15;
                            heartsDelta = rng.nextBoolean() ? +1 : +2;
                        } else {
                            points = -15;
                            heartsDelta = rng.nextBoolean() ? -1 : -2;
                        }
                    }
                    case HARD -> {
                        if (correct) {
                            points = 20;
                            heartsDelta = +2;
                        } else {
                            points = -20;
                            heartsDelta = -2;
                        }
                    }
                    case EXPERT -> {
                        if (correct) {
                            points = 40;
                            heartsDelta = +3;
                        } else {
                            points = -40;
                            heartsDelta = -3;
                        }
                    }
                }
            }
        }

        if (points != 0) {
            game.addToTeamScore(points);
        }
        if (heartsDelta != 0) {
            addLives(game, heartsDelta);
        }
    }

    
    public void activateQuestion(Game game, Cell cell, QuestionLevel level, boolean correct) {
        // Only revealed, unused QUESTION cells can be activated
        if (!cell.isRevealed() ||
            cell.getType() != CellType.QUESTION ||
            cell.isUsedSpecial()) {
            return;
        }

        applyQuestionOutcome(game, level, correct);

        // mark as USED so it can't be used again
        cell.setUsedSpecial(true);
    }


    
    
    public void activateSurprise(Game game, int col, int row) {
        Board board = game.getBoardFor(game.getActivePlayer());
        if (!board.inBounds(col, row)) return;

        Cell cell = board.get(col, row);

        // can only activate revealed, non-used surprise cells
        if (!cell.isRevealed() ||
            cell.getType() != CellType.SURPRISE ||
            cell.isUsedSpecial()) {
            return;
        }

        applySurpriseEffect(game);

        // mark as USED so it can't be activated again
        cell.setUsedSpecial(true);
    }


    
 // Surprise activation logic according to difficulty
    private void applySurpriseEffect(Game game) {
        Difficulty diff = game.getDifficulty();

        int activationCost = 0;
        int surprisePoints = 0;

        // values taken from the spec:
        // Easy:   cost 5,  good +1 life +8 pts,  bad -1 life -8 pts
        // Medium: cost 8,  good +1 life +12 pts, bad -1 life -12 pts
        // Hard:   cost 12, good +1 life +16 pts, bad -1 life -16 pts
        switch (diff) {
            case EASY:
                activationCost = 5;
                surprisePoints = 8;
                break;
            case MEDIUM:
                activationCost = 8;
                surprisePoints = 12;
                break;
            case HARD:
                activationCost = 12;
                surprisePoints = 16;
                break;
            default:
                // fallback – should never happen, but keeps compiler 100% happy
                activationCost = 5;
                surprisePoints = 8;
                break;
        }

        // Pay activation cost (score may go negative)
        game.addToTeamScore(-activationCost);

        java.util.random.RandomGenerator rng = Rng.current();
        boolean good = rng.nextBoolean(); // 50-50 good / bad

        int lives = game.getTeamLives();

        if (good) {
            // GOOD SURPRISE: +1 life, +surprisePoints
            lives += 1;
            game.setTeamLives(lives);
            game.addToTeamScore(surprisePoints);
        } else {
            // BAD SURPRISE: -1 life, -surprisePoints
            lives -= 1;
            game.setTeamLives(lives);
            game.addToTeamScore(-surprisePoints);

            if (lives <= 0) {
                finishGame(game, false);
            }
        }
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
