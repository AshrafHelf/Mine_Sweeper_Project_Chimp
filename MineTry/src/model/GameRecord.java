package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import enums.Difficulty;

public class GameRecord {

    private final LocalDateTime finishedAt;
    private final Difficulty difficulty;
    private final String player1Name;
    private final String player2Name;
    private final int finalScore;
    private final boolean won;

    private static final DateTimeFormatter CSV_TIME_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public GameRecord(LocalDateTime finishedAt,
                      Difficulty difficulty,
                      String player1Name,
                      String player2Name,
                      int finalScore,
                      boolean won) {
        this.finishedAt = finishedAt;
        this.difficulty = difficulty;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.finalScore = finalScore;
        this.won = won;
    }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public Difficulty getDifficulty() { return difficulty; }
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }
    public int getFinalScore() { return finalScore; }
    public boolean isWon() { return won; }

    // ---------- CSV helpers ----------

    public String toCsvLine() {
        return String.join(",",
                CSV_TIME_FMT.format(finishedAt),
                difficulty.name(),
                escape(player1Name),
                escape(player2Name),
                Integer.toString(finalScore),
                Boolean.toString(won)
        );
    }

    public static GameRecord fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Bad history row: " + line);
        }

        LocalDateTime time = LocalDateTime.parse(parts[0].trim(), CSV_TIME_FMT);
        Difficulty diff = Difficulty.valueOf(parts[1].trim());
        String p1 = unescape(parts[2]);
        String p2 = unescape(parts[3]);
        int score = Integer.parseInt(parts[4].trim());
        boolean won = Boolean.parseBoolean(parts[5].trim());

        return new GameRecord(time, diff, p1, p2, score, won);
    }

    private static String escape(String s) {
        // very simple: replace commas to avoid breaking CSV
        return s.replace(",", " ");
    }

    private static String unescape(String s) {
        return s;
    }
}
