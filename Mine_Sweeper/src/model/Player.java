package model;

public class Player {
    private String name;
    private int hearts;
    private int score;

    public Player(String name) {
        this.name = name;
        this.hearts = 10;   // default
        this.score = 0;
    }

    public String getName() {
    	return name; 
    	}

    public int getHearts() {
    	return hearts; 
    	}
    public void loseHeart() {
    	hearts--; 
    	}

    public int getScore() {
    	return score; 
    	}
    public void addScore(int s) {
    	score += s; 
    	}

    public boolean isDead() {
        return hearts <= 0;
    }
}