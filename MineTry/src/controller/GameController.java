package controller;

import model.Board;
import model.Cell;
import model.CellType;
import model.Difficulty;
import model.Game;
import model.GameEngine;
import model.GameState;
import model.QuestionLevel;
import view.GameOverDialog;
import view.GamePanel;
import view.MainWindow;
import model.Question;
import model.QuestionService;
import view.QuestionAdminDialog;


import javax.swing.*;
import java.awt.*;

public class GameController {

	private final GamePanel view;
	private final GameEngine engine;
	private final Game game;
	private final MainWindow window;
	private final QuestionService questionService;


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
wire();
}


    private void wire() {

        // Back / Restart
        view.onBackToMenu(this::requestBackToMenu);
        view.onRestart(this::requestRestart);

        // LEFT CLICK = reveal or activate
        view.onCellReveal((c, r) -> {
            if (game.getState() != GameState.RUNNING) return;

            Board board = game.getBoardFor(game.getActivePlayer());
            if (!board.inBounds(c, r)) return;

            Cell cell = board.get(c, r);
            if (cell.isFlagged()) return;

            // =============================
            // FIRST CLICK → reveal normally
            // =============================
            if (!cell.isRevealed()) {
                engine.reveal(game, c, r);

                if (game.getState() == GameState.RUNNING) {
                    game.swapTurn();
                }

            // =============================
            // SECOND CLICK → activate S/Q
            // =============================
            } else {

                switch (cell.getType()) {

                case SURPRISE -> {
                    engine.activateSurprise(game, c, r);

                    if (game.getState() == GameState.RUNNING) {
                        game.swapTurn();
                    }
                }
                case QUESTION -> {
                    // Reveal the cell
                    cell.setRevealed(true);

                    // TEMPORARY: simulate a correct answer for testing
                    boolean correct = true;
                    QuestionLevel level = QuestionLevel.EASY;  // TEMP: change later when dialog works

                    // Apply the scoring & hearts logic
                    engine.activateQuestion(game, cell, level, correct);

                    if (game.getState() == GameState.RUNNING) {
                        game.swapTurn();
                    }

                    view.refreshFromModel();
                }



                    default -> {
                        // Clicking revealed normal cells does nothing
                        return;
                    }
                }
            }

            // Update view
            view.refreshFromModel();
            showGameOverIfNeeded();
        });

        // RIGHT CLICK = flag
        view.onCellFlag((c, r) -> {
            if (game.getState() != GameState.RUNNING) return;

            engine.toggleFlag(game, c, r);
            view.refreshFromModel();
            showGameOverIfNeeded();
        });
    }
    
    private QuestionLevel randomQuestionLevel() {
        QuestionLevel[] levels = QuestionLevel.values();
        int idx = model.Rng.current().nextInt(levels.length);
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

        new GameController(newPanel, engine, newGame, window , questionService);
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
