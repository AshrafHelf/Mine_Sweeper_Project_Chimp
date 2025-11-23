package model;


import model.Difficulty;


public record GameRecord(String player1, String player2, Difficulty difficulty, int score1, int score2, long timestamp) {}