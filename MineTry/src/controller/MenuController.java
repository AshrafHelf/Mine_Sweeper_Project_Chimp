package controller;

import model.GameEngine;
import model.HistoryService;
import model.QuestionService;

import model.Difficulty;
import view.GamePanel;
import view.MainWindow;

public class MenuController {

    private final MainWindow view;
    private final GameEngine engine;
    private final QuestionService qService;
    private final HistoryService hService;

    public MenuController(MainWindow view, GameEngine engine, QuestionService qService, HistoryService hService) {
        this.view = view;
        this.engine = engine;
        this.qService = qService;
        this.hService = hService;
        wire();
    }

    private void wire() {
        // When user presses "Start Game" in the main menu:
        view.onNewGame((difficulty, p1, p2) -> {
            var game = engine.newGame(difficulty, p1, p2);
            GamePanel panel = view.showGame(game);   // show the game screen
            new GameController(panel, engine, game); // connect controller to this game
        });

        // Top menu: Questions admin (for later)
        view.onOpenQuestions(() -> view.showQuestionAdmin());

        // Top menu: History (scores)
        view.onOpenHistory(() -> view.showHistory(hService.all()));
    }
}
