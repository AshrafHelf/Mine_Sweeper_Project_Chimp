package controller;

import model.GameEngine;

import model.Board;
import model.Cell;
import model.Game;
import model.GameState;
import view.GameOverDialog;
import view.GamePanel;

import javax.swing.*;
import java.awt.*;

public class GameController {

    private final GamePanel view;
    private final GameEngine engine;
    private final Game game;

    public GameController(GamePanel view, GameEngine engine, Game game) {
        this.view = view;
        this.engine = engine;
        this.game = game;
        wire();
    }

    private void wire() {
        // LEFT CLICK = reveal
        view.onCellReveal((c, r) -> {
            if (game.getState() != GameState.RUNNING) return;

            Board board = game.getBoardFor(game.getActivePlayer());
            if (!board.inBounds(c, r)) return;

            Cell cell = board.get(c, r);
            if (cell.isRevealed() || cell.isFlagged()) {
                return; // don't reveal, don't swap turn
            }

            GameState before = game.getState();

            engine.reveal(game, c, r);

            // if still running after reveal → swap turn
            if (game.getState() == GameState.RUNNING) {
                game.swapTurn();
            }

            view.refreshFromModel();

            // if game just finished, show dialog
            if (before == GameState.RUNNING && game.getState() == GameState.OVER) {
                showGameOverDialog();
            }
        });

        // RIGHT CLICK = flag
        view.onCellFlag((c, r) -> {
            if (game.getState() != GameState.RUNNING) return;

            engine.toggleFlag(game, c, r);
            view.refreshFromModel();
        });
    }

    private void showGameOverDialog() {
        boolean won = game.isWon();
        int finalScore = game.getTeamScore();

        // find the top-level window (MainWindow extends JFrame)
        Window w = SwingUtilities.getWindowAncestor(view);
        JFrame owner = (w instanceof JFrame) ? (JFrame) w : null;

        GameOverDialog dlg = new GameOverDialog(owner, won, finalScore);
        dlg.setVisible(true);
    }
}
