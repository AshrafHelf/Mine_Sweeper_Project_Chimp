package view;

import model.*;

import javax.swing.*;

import controller.GameEngine;
import controller.MenuController;

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
        
            
          
            HistoryService historyService   = new HistoryService(historyRepo);
            GameEngine gameEngine           = new GameEngine(
                    boardGenerator,
                 
              
                    
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
