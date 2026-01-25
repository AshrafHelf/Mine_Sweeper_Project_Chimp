package controller;

import enums.Difficulty;
import model.Game;
import model.HistoryService;
import model.QuestionService;
import view.AudioManager;
import view.GamePanel;
import view.HelpDialog;
import view.MainWindow;
import view.SettingsDialog;
import view.Toast;

/**
 * Wires MainWindow UI actions (menus/buttons) to the game logic + dialogs.
 * Keeps navigation clean and centralized.
 */
public class MenuController {

    private final MainWindow view;
    private final GameEngine engine;
    private final QuestionService qService;
    private final HistoryService hService;

    public MenuController(MainWindow view,
                          GameEngine engine,
                          QuestionService qService,
                          HistoryService hService) {
        this.view = view;
        this.engine = engine;
        this.qService = qService;
        this.hService = hService;
        wire();
    }

    private void wire() {

        // Start Game from Setup screen
        view.onNewGame((Difficulty difficulty, String p1, String p2) -> {
            AudioManager.playSfx("button.wav");

            Game game = engine.newGame(difficulty, p1, p2);

            // stop menu music before entering gameplay (no in-game track yet)
            AudioManager.stopMusic();

            GamePanel panel = view.showGame(game);

            // Wire in-game Settings + Help (C requirement)
            panel.onOpenSettings(() -> {
                AudioManager.playSfx("button.wav");
                new SettingsDialog(view).setVisible(true);
            });

            panel.onOpenHelp(() -> {
                AudioManager.playSfx("message.wav");
                Toast.show(view, "Help guide coming next 🌿🐒");
            });

            // Gameplay controller
            new GameController(panel, engine, game, view, qService);
        });

        // Main window menus (or Home buttons)
        view.onOpenQuestions(() -> {
            AudioManager.playSfx("button.wav");
            view.showQuestionAdmin(qService);
        });

        view.onOpenHistory(() -> {
            AudioManager.playSfx("button.wav");
            view.showHistory(hService.all());
        });

        view.onOpenSettings(() -> {
            AudioManager.playSfx("button.wav");
            SettingsDialog dlg = new SettingsDialog(view);
            dlg.setAlwaysOnTop(true);
            dlg.setVisible(true);
            dlg.toFront();
            dlg.requestFocus();
            dlg.setAlwaysOnTop(false);
        });

        view.onOpenHelp(() -> {
            AudioManager.playSfx("message.wav");
            HelpDialog.open(view, 0);
        });

    }
}
