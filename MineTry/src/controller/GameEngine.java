package controller;


import model.*;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.random.RandomGenerator;

public class GameEngine {

    private static final int BONUS_PER_LIFE = 5; // TODO: later make per-difficulty aza bdna y3ne
    private static final int MAX_LIVES = 10;   


    private final BoardGenerator boardGen;
   
    private final ScoringService scoring;
    private final HistoryService history;  // kept for future use
    

    public GameEngine(BoardGenerator boardGen,
                    
                      ScoringService scoring,
                      
                      HistoryService history) {
        this.boardGen = boardGen;
        
        this.scoring = scoring;
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
                this.cascadeReveal(board, col, row);
                game.addToTeamScore(scoring.scoreForReveal(CellType.EMPTY));
            }
            case NUMBER -> {
                cell.setRevealed(true);
                game.addToTeamScore(scoring.scoreForReveal(CellType.NUMBER));
            }
            case SURPRISE -> {
                // FIRST click on S: reveal like empty (+1 point), DO NOT activate yet
                this.cascadeReveal(board, col, row);
                game.addToTeamScore(scoring.scoreForReveal(CellType.SURPRISE));
                // activation happens on SECOND click in GameController
            }
            case QUESTION -> {
                // FIRST click on Q: reveal like empty (+1 point), DO NOT open question yet
                this.cascadeReveal(board, col, row);
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

        // Can't flag revealed cells
        if (cell.isRevealed()) return;

        boolean addingFlag = !cell.isFlagged();
        cell.setFlagged(addingFlag);

        // Score only the FIRST time a flag is PLACED on this cell
        if (addingFlag && !cell.isFlagScored()) {
            int deltaScore = scoring.scoreForFlag(cell.getType(), true);
            game.addToTeamScore(deltaScore);
            cell.setFlagScored(true);
        }

        // Removing flag: no score change (and we KEEP flagScored=true so it can't be farmed)
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
        RandomGenerator rng = BoardGenerator.current();
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
 // change team lives and handle overflow / game over
 // returns overflow points gained from hearts above MAX_LIVES
    private int addLives(Game game, int delta) {
        int current = game.getTeamLives();
        int newLives = current + delta;

        int overflowPoints = 0;

        if (newLives > MAX_LIVES) {
            int extraHearts = newLives - MAX_LIVES;
            newLives = MAX_LIVES;

            // convert extra hearts to points (same rule everywhere)
            overflowPoints = extraHearts * BONUS_PER_LIFE;
            game.addToTeamScore(overflowPoints);
        }

        if (newLives <= 0) {
            game.setTeamLives(0);
            finishGame(game, false);
        } else {
            game.setTeamLives(newLives);
        }

        return overflowPoints;
    }


    private int activationCostFor(Difficulty diff) {
        return switch (diff) {
            case EASY -> 5;
            case MEDIUM -> 8;
            case HARD -> 12;
        };
    }


    // apply the big scoring table: depends on game difficulty + question level + correctness
    public String applyQuestionOutcome(Game game, QuestionLevel qLevel, boolean correct) {
        Difficulty gDiff = game.getDifficulty();
        RandomGenerator rng = BoardGenerator.current();

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

     // AFTER you compute points + heartsDelta:

     // Apply points first
     if (points != 0) {
         game.addToTeamScore(points);
     }

     // Apply hearts and capture overflow points
     int overflowPoints = 0;
     if (heartsDelta != 0) {
         overflowPoints = addLives(game, heartsDelta);
     }

     // ------- build message for popup -------
     StringBuilder sb = new StringBuilder();
     sb.append(correct ? "Correct answer!\n" : "Wrong answer!\n");


     if (points != 0) {
         sb.append("Question effect: ")
           .append(points > 0 ? "+" : "")
           .append(points).append(" pts\n");
     }

     if (heartsDelta != 0) {
         sb.append("Hearts: ")
           .append(heartsDelta > 0 ? "+" : "")
           .append(heartsDelta).append(" \u2665\n");
     }

     if (overflowPoints > 0) {
         sb.append("Heart overflow: +").append(overflowPoints).append(" pts\n");
     }

   

     return sb.toString();

    }


    public String activateQuestion(Game game, Cell cell, QuestionLevel level, boolean correct) {
        if (!cell.isRevealed() || cell.getType() != CellType.QUESTION || cell.isUsedSpecial()) return null;

        int beforeScore = game.getTeamScore();
        int beforeLives = game.getTeamLives();

        int activationCost = activationCostFor(game.getDifficulty());
        game.addToTeamScore(-activationCost);

        String outcomeMsg = applyQuestionOutcome(game, level, correct);

        cell.setUsedSpecial(true);

        int deltaScore = game.getTeamScore() - beforeScore;
        int deltaLives = game.getTeamLives() - beforeLives;

        return "Activation cost: -" + activationCost + " pts\n"
                + outcomeMsg + "\n"
                + "Net change: " + signed(deltaScore) + " pts, " + signed(deltaLives) + " \u2665";
    }

    private String signed(int x) { return (x >= 0 ? "+" : "") + x; }






    
    
    public String activateSurprise(Game game, int col, int row) {
        Board board = game.getBoardFor(game.getActivePlayer());
        if (!board.inBounds(col, row)) return null;

        Cell cell = board.get(col, row);

        // can only activate revealed, non-used surprise cells
        if (!cell.isRevealed()
                || cell.getType() != CellType.SURPRISE
                || cell.isUsedSpecial()) {
            return null;
        }

        String msg = applySurpriseEffect(game);

        // mark as USED so it can't be activated again
        cell.setUsedSpecial(true);

        return msg;
    }




    
 // Surprise activation logic according to difficulty
    private String applySurpriseEffect(Game game) {
        Difficulty diff = game.getDifficulty();

        int activationCost;
        int surprisePoints;

        switch (diff) {
            case EASY -> { activationCost = 5;  surprisePoints = 8;  }
            case MEDIUM -> { activationCost = 8; surprisePoints = 12; }
            case HARD -> { activationCost = 12; surprisePoints = 16; }
            default -> { activationCost = 5; surprisePoints = 8; }
        }

        RandomGenerator rng = BoardGenerator.current();
        boolean good = rng.nextBoolean();

        // 1) activation cost
        game.addToTeamScore(-activationCost);

        // 2) outcome points (good=+surprisePoints, bad=-surprisePoints)
        int pointsDelta = good ? +surprisePoints : -surprisePoints;
        game.addToTeamScore(pointsDelta);

        // 3) hearts (good +1, bad -1) + overflow conversion via addLives
        int heartsDelta = good ? +1 : -1;
        int overflowPoints = addLives(game, heartsDelta);

        // Build explicit message (shows cost + outcome + overflow)
        StringBuilder sb = new StringBuilder();
        sb.append(good ? "Good surprise!\n" : "Bad surprise!\n");

        sb.append("Activation cost: -").append(activationCost).append(" pts\n");
        sb.append("Surprise effect: ").append(pointsDelta >= 0 ? "+" : "").append(pointsDelta).append(" pts\n");

        sb.append("Hearts: ").append(heartsDelta >= 0 ? "+" : "").append(heartsDelta).append(" \u2665\n");

        if (overflowPoints > 0) {
            sb.append("Heart overflow: +").append(overflowPoints).append(" pts\n");
        }

        int net = (-activationCost) + pointsDelta + overflowPoints;
        sb.append("Net change: ").append(net >= 0 ? "+" : "").append(net).append(" pts");

        return sb.toString();
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

        // 🔹 NEW: record the game in history
        if (history != null) {
            history.recordGame(game);
        }
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
    public void cascadeReveal(Board board, int startCol, int startRow) {
        int rows = board.getRows();
        int cols = board.getCols();

        boolean[][] visited = new boolean[rows][cols];
        Queue<Coordinate> queue = new ArrayDeque<>();

        queue.add(new Coordinate(startCol, startRow));

        while (!queue.isEmpty()) {
            Coordinate cur = queue.remove();
            int c = cur.col();
            int r = cur.row();

            if (!board.inBounds(c, r)) continue;
            if (visited[r][c]) continue;
            visited[r][c] = true;

            Cell cell = board.get(c, r);

            // skip if already revealed or flagged
            if (cell.isRevealed() || cell.isFlagged()) continue;

            // never auto-reveal mines
            if (cell.getType() == CellType.MINE) continue;

            // reveal any NON-MINE cell (EMPTY, NUMBER, QUESTION, SURPRISE)
            cell.setRevealed(true);

            // if EMPTY, expand to neighbors
            if (cell.getType() == CellType.EMPTY || cell.getType()==CellType.QUESTION) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nc = c + dc;
                        int nr = r + dr;

                        if (board.inBounds(nc, nr) && !visited[nr][nc]) {
                            queue.add(new Coordinate(nc, nr));
                        }
                    }
                }
            }
        }
    

    }
}
