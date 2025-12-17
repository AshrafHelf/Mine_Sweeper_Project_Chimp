package controller;

import model.Difficulty;
import model.Game;
import model.HistoryService;
import model.QuestionService;
import view.GamePanel;
import view.MainWindow;

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
        // When user presses "Start Game" in the main menu:
        view.onNewGame((Difficulty difficulty, String p1, String p2) -> {
            Game game = engine.newGame(difficulty, p1, p2);

            // Show game panel
            GamePanel panel = view.showGame(game);

            // PASS MainWindow + QuestionService to GameController
            new GameController(panel, engine, game, view, qService);
        });

        // Top menu: open question admin
        view.onOpenQuestions(() -> view.showQuestionAdmin(qService));

        // Top menu: open game history
        view.onOpenHistory(() -> view.showHistory(hService.all()));
    }
}
