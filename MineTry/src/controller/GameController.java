package controller;

import model.Board;
import model.Cell;
import model.Game;
import model.Question;
import model.QuestionService;
import view.AudioManager;
import view.GameOverDialog;
import view.GamePanel;
import view.MainWindow;
import view.QuestionDialog;

import javax.swing.*;
import java.awt.*;

import enums.Difficulty;
import enums.GameState;
import enums.QuestionLevel;

public class GameController {

    private final GamePanel view;
    private final GameEngine engine;
    private final Game game;
    private final MainWindow window;
    private final QuestionService questionService;

    private final GameObserver uiObserver;

    // Prevent double game-over dialog / double win-lose sound
    private boolean gameOverShown = false;

    // Prevent “ghost controller” reacting after restart/back
    private boolean disposed = false;

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

        // Observer reacts only to GAME OVER (ignored if disposed)
        this.uiObserver = g -> showGameOverIfNeeded();
        this.engine.addObserver(this.uiObserver);

        wire();
    }

    private void wire() {

        // Back / Restart
        view.onBackToMenu(() -> {
            AudioManager.playSfx("button.wav");
            requestBackToMenu();
        });

        view.onRestart(() -> {
            AudioManager.playSfx("button.wav");
            requestRestart();
        });

        // ==========================
        // LEFT CLICK = reveal/activate
        // ==========================
        view.onCellReveal((c, r) -> {
            if (disposed) return;
            if (game.getState() != GameState.RUNNING) return;

            Board board = game.getBoardFor(game.getActivePlayer());
            if (!board.inBounds(c, r)) return;

            Cell cell = board.get(c, r);
            if (cell.isFlagged()) return;

            // ---- FIRST CLICK: not revealed yet → normal reveal in engine ----
            if (!cell.isRevealed()) {

                // Play reveal sound immediately (UX feels responsive)
                AudioManager.playSfx("revealcell.wav");

                // Reveal action
                engine.reveal(game, c, r);

                // Best-effort mine detection: if game went OVER after reveal, likely a mine chain or loss
                // (If you have a cleaner API like cell.isMine() or cell.getType()==MINE, use it instead)
                if (game.getState() == GameState.OVER) {
                    // We'll play win/lose in showGameOverIfNeeded() exactly once.
                } else {
                    // Turn ends after reveal (including cascade)
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
                        // Surprise activation
                        String msg = engine.activateSurprise(game, c, r);

                        // Sound feedback
                        // If your message contains positive keywords, play "bonus", otherwise message sound.
                        if (msg != null) {
                            String m = msg.toLowerCase();
                            if (m.contains("bonus") || m.contains("+") || m.contains("gain") || m.contains("life")
                                    || m.contains("lives") || m.contains("score")) {
                                AudioManager.playSfx("bonus.wav");
                            } else {
                                AudioManager.playSfx("message.wav");
                            }
                            view.pushEvent(msg);
                        } else {
                            AudioManager.playSfx("message.wav");
                        }

                        // Optional popup
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
                        AudioManager.playSfx("message.wav"); // opening question dialog feels nice

                        QuestionLevel level = randomQuestionLevel();

                        var maybeQ = questionService.random(level);
                        if (maybeQ.isEmpty()) {
                            JOptionPane.showMessageDialog(
                                    window,
                                    "No questions available for level " + level,
                                    "No Questions",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            break;
                        }

                        Question q = maybeQ.get();

                        QuestionDialog dialog = new QuestionDialog(window, q);
                        Boolean correct = dialog.showAndGetResult();

                        // user cancelled → don't use tile, don't change turn
                        if (correct == null) break;

                        // Apply question outcome
                        String msg = engine.activateQuestion(game, cell, q.getLevel(), correct);

                        // Sound feedback for correctness
                        AudioManager.playSfx(Boolean.TRUE.equals(correct) ? "correct.wav" : "error.wav");

                        if (msg != null) view.pushEvent(msg);

                        // Optional popup
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
            if (disposed) return;
            if (game.getState() != GameState.RUNNING) return;

            AudioManager.playSfx("button.wav"); // simple feedback for flagging

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
        if (window == null) return;

        disposeController();
        window.showMenu(); // CardLayout switch
    }

    private void requestRestart() {
        if (window == null) return;

        // Kill this controller so it won't react after restart/back
        disposeController();

        Difficulty diff = game.getDifficulty();
        String p1 = game.getPlayer1().getName();
        String p2 = game.getPlayer2().getName();

        Game newGame = engine.newGame(diff, p1, p2);
        GamePanel newPanel = window.showGame(newGame);

        // New controller instance starts clean
        new GameController(newPanel, engine, newGame, window, questionService);
    }

    // Make sure this controller becomes inert and stops listening to engine updates
    private void disposeController() {
        if (disposed) return;
        disposed = true;

        // IMPORTANT: remove observer so old controller doesn't react after restart/back
        try {
            engine.removeObserver(uiObserver);
        } catch (Exception ignored) {
     
        }
    }

    // -------------------------
    // GAME OVER HANDLING
    // -------------------------
    private void showGameOverIfNeeded() {
        if (disposed) return;
        if (game.getState() != GameState.OVER) return;

        // Prevent showing twice
        if (gameOverShown) return;
        gameOverShown = true;

        boolean won = game.isWon();
        int finalScore = game.getTeamScore();

        // Play win/lose sound ONCE here (best place)
        AudioManager.playSfx(won ? "win.wav" : "lose.wav");

        Window w = SwingUtilities.getWindowAncestor(view);
        JFrame owner = (w instanceof JFrame) ? (JFrame) w : null;

        SwingUtilities.invokeLater(() -> {
            if (disposed) return;
            GameOverDialog dlg = new GameOverDialog(owner, won, finalScore);
            dlg.setVisible(true);
        });
    }
}
