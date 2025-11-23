package controller;



import model.*;

import controller.MenuController;
import view.MainWindow;

import javax.swing.*;


public class App {
public static void main(String[] args) {
SwingUtilities.invokeLater(() -> {
// Bootstrap (very light DI)
var sysData = SysData.getInstance();
var questionRepo = new CsvQuestionRepository("data/questions.csv");
var historyRepo = new FileHistoryRepository("data/history.csv");


var questionService = new QuestionService(questionRepo, sysData);
var boardGenerator = new BoardGenerator();
var scoringService = new ScoringService();
var turnService = new TurnService();
var cascadeService = new CascadeService();
var historyService = new HistoryService(historyRepo);
var gameEngine = new GameEngine(boardGenerator, cascadeService, scoringService, turnService, historyService);


var mainWindow = new MainWindow();
new MenuController(mainWindow, gameEngine, questionService, historyService);
mainWindow.setVisible(true);
});
}
}