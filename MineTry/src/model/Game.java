package model;

import enums.Difficulty;
import enums.GameState;

public class Game {
    private final Difficulty difficulty;
    private final Player p1, p2;
    private Board b1, b2; // each player board
    private int activePlayerIndex; // 0 = p1, 1 = p2
    private GameState state = GameState.READY;

    // team-based resources (shared)
    private int teamLives;
    private int teamScore;

    // result: true = won, false = lost
    private boolean won;

    public Game(Difficulty difficulty, Player p1, Player p2) {
        this.difficulty = difficulty;
        this.p1 = p1;
        this.p2 = p2;
        this.activePlayerIndex = 0;
        this.won = false;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Player getPlayer1() {
        return p1;
    }

    public Player getPlayer2() {
        return p2;
    }

    public Player getActivePlayer() {
        return activePlayerIndex == 0 ? p1 : p2;
    }

    public Player getOtherPlayer() {
        return activePlayerIndex == 0 ? p2 : p1;
    }

    public void swapTurn() {
        activePlayerIndex = (activePlayerIndex == 0) ? 1 : 0;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState s) {
        this.state = s;
    }

    public Board getBoardFor(Player p) {
        return (p == p1) ? b1 : b2;
    }

    public Board getBoard1() {
        return b1;
    }

    public Board getBoard2() {
        return b2;
    }

    public void setBoards(Board b1, Board b2) {
        this.b1 = b1;
        this.b2 = b2;
    }

    // --- team resources ---

    public int getTeamLives() {
        return teamLives;
    }

    public void setTeamLives(int teamLives) {
        this.teamLives = teamLives;
    }

    public int getTeamScore() {
        return teamScore;
    }

    public void setTeamScore(int teamScore) {
        this.teamScore = teamScore;
    }

    public void addToTeamScore(int delta) {
        this.teamScore += delta;
    }

    // --- win/lose flag ---

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }
    
   
}
