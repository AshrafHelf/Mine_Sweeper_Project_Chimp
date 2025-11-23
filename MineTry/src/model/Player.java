package model;


public class Player {
private final String name;
private int lives;
private int score;


public Player(String name, int lives) { this.name = name; this.lives = lives; }
public String getName(){ return name; }
public int getLives(){ return lives; }
public void setLives(int l){ this.lives=l; }
public int getScore(){ return score; }
public void addScore(int delta){ this.score += delta; }
}