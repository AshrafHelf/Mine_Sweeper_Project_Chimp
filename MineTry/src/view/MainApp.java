package view;

import model.*;
import javax.swing.*;

import controller.GameEngine;
import controller.MenuController;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

   
            // ===== Bootstrap =====
            SysData sysData = SysData.getInstance();

            QuestionRepository questionRepo = new CsvQuestionRepository("data/Questions.csv");
            HistoryRepository historyRepo   = new FileHistoryRepository("data/history.csv");

            QuestionService questionService = new QuestionService(questionRepo, sysData);
            BoardGenerator boardGenerator   = new BoardGenerator();
            HistoryService historyService   = new HistoryService(historyRepo);

            GameEngine gameEngine = new GameEngine(
                    boardGenerator,
                    historyService
            );

            sysData.setQuestions(questionRepo.findAll());

            MainWindow mainWindow = new MainWindow();

            // IMPORTANT: release audio resources on exit
            mainWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    AudioManager.shutdown();
                }
            });

            new MenuController(mainWindow, gameEngine, questionService, historyService);
            mainWindow.setVisible(true);
        });
    }
}
