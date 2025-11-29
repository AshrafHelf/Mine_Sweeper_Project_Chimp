package controller;

import model.*;
import view.MainWindow;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // ===== Bootstrap (very light DI) =====
            SysData sysData = SysData.getInstance();

            // Repos (file-based)
            QuestionRepository questionRepo = new CsvQuestionRepository("data/Questions.csv");
            HistoryRepository historyRepo = new FileHistoryRepository("data/history.csv");

            // Services
            QuestionService questionService = new QuestionService(questionRepo, sysData);
            BoardGenerator boardGenerator   = new BoardGenerator();
            ScoringService scoringService   = new ScoringService();
            TurnService turnService         = new TurnService();
            CascadeService cascadeService   = new CascadeService();
            HistoryService historyService   = new HistoryService(historyRepo);
            GameEngine gameEngine           = new GameEngine(
                    boardGenerator,
                    cascadeService,
                    scoringService,
                    turnService,
                    historyService
            );

            // Load initial data into SysData (optional but nice)
            sysData.setQuestions(questionRepo.findAll());
            // If you want history cached as well:
            // sysData.setHistory(historyService.all());

            // UI
            MainWindow mainWindow = new MainWindow();
            new MenuController(mainWindow, gameEngine, questionService, historyService);
            mainWindow.setVisible(true);
        });
    }
}
