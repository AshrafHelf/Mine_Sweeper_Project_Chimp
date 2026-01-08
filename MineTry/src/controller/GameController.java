package controller;

import model.Board;
import model.Cell;
import model.Game;
import model.Question;
import model.QuestionService;
import view.GameOverDialog;
import view.GamePanel;
import view.MainWindow;
import view.QuestionDialog;

import javax.swing.*;

import enums.Difficulty;
import enums.GameState;
import enums.QuestionLevel;

import java.awt.*;

public class GameController {

    private final GamePanel view;
    private final GameEngine engine;
    private final Game game;
    private final MainWindow window;
    private final QuestionService questionService;
    private final GameObserver uiObserver;

    public GameController(GamePanel view,
                          GameEngine engine,
                          Game game,
                          MainWindow window,
                          QuestionService questionService) {

        this.view = view;
        this.engine = engine;
        this.game = game;
        this.window = window;
        this.questionService = questionService;

        // ✅ Observer only reacts to GAME OVER
        this.uiObserver = g -> showGameOverIfNeeded();

        this.engine.addObserver(this.uiObserver);

        wire();
    }


    private void wire() {

        // Back / Restart
        view.onBackToMenu(this::requestBackToMenu);
        view.onRestart(this::requestRestart);

        // ==========================
        // LEFT CLICK = reveal/activate
        // ==========================
        view.onCellReveal((c, r) -> {
            if (game.getState() != GameState.RUNNING) return;

            Board board = game.getBoardFor(game.getActivePlayer());
            if (!board.inBounds(c, r)) return;

            Cell cell = board.get(c, r);
            if (cell.isFlagged()) return;

            // ---- FIRST CLICK: not revealed yet → normal reveal in engine ----
            if (!cell.isRevealed()) {
                engine.reveal(game, c, r);

                // ✅ revealing (including cascade) ends the turn
                if (game.getState() == GameState.RUNNING) {
                    game.swapTurn();
                }
            }

            // ---- SECOND CLICK: already revealed (possible special activation) ----
            else {

                // If special tile already used, do nothing
                if (cell.isUsedSpecial()) {
                    view.refreshFromModel();
                    return;
                }

                switch (cell.getType()) {

                    case SURPRISE -> {
                        // activate surprise (good/bad heart + points)
                        String msg = engine.activateSurprise(game, c, r);

                        if (msg != null && window != null) {
                            JOptionPane.showMessageDialog(
                                    window,
                                    msg,
                                    "Surprise Cell",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }

                        // ❌ NO swapTurn here – activation does NOT end the turn
                    }

                    case QUESTION -> {
                        // 1) pick a level (random for now)
                        QuestionLevel level = randomQuestionLevel();

                        // 2) pick a random question
                        var maybeQ = questionService.random(level);
                        if (maybeQ.isEmpty()) {
                            JOptionPane.showMessageDialog(window,
                                    "No questions available for level " + level,
                                    "No Questions",
                                    JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }

                        Question q = maybeQ.get();

                        // 3) show dialog and get answer
                        QuestionDialog dialog = new QuestionDialog(window, q);
                        Boolean correct = dialog.showAndGetResult();

                        // user cancelled → don't use tile, don't change turn
                        if (correct == null) {
                            break;
                        }

                        // 4) apply scoring & hearts
                        String msg = engine.activateQuestion(game, cell, q.getLevel(), correct);

                        if (msg != null && window != null) {
                            JOptionPane.showMessageDialog(
                                    window,
                                    msg,
                                    "Question Result",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }

                        // ❌ NO swapTurn here – activation does NOT end the turn
                    }

                    default -> {
                        // clicking a revealed normal cell does nothing
                    }
                }
            }

            view.refreshFromModel();
            showGameOverIfNeeded();
        });

        

        // ==========================
        // RIGHT CLICK = flag / unflag
        // ==========================
        view.onCellFlag((c, r) -> {
            if (game.getState() != GameState.RUNNING) return;

            engine.toggleFlag(game, c, r);
            view.refreshFromModel();
            showGameOverIfNeeded();
        });
    }

    // Random question level 1 of {EASY, MEDIUM, HARD, EXPERT}
    private QuestionLevel randomQuestionLevel() {
        QuestionLevel[] levels = QuestionLevel.values();
        int idx = model.BoardGenerator.current().nextInt(levels.length);
        return levels[idx];
    }

    // -------------------------
    // NAVIGATION
    // -------------------------

    private void requestBackToMenu() {
        if (window != null) {
            window.showMenu();   // CardLayout switch
        }
    }

    private void requestRestart() {
        if (window == null) return;

        Difficulty diff = game.getDifficulty();
        String p1 = game.getPlayer1().getName();
        String p2 = game.getPlayer2().getName();

        Game newGame = engine.newGame(diff, p1, p2);
        GamePanel newPanel = window.showGame(newGame);

        new GameController(newPanel, engine, newGame, window, questionService);
    }

    // -------------------------
    // GAME OVER HANDLING
    // -------------------------

    private void showGameOverIfNeeded() {
        if (game.getState() != GameState.OVER) return;

        boolean won = game.isWon();
        int finalScore = game.getTeamScore();

        Window w = SwingUtilities.getWindowAncestor(view);
        JFrame owner = (w instanceof JFrame) ? (JFrame) w : null;

        GameOverDialog dlg = new GameOverDialog(owner, won, finalScore);
        dlg.setVisible(true);
    }
    
    
}
