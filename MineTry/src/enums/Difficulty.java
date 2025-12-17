package enums;  // make sure this matches your real package name

public enum Difficulty {
    EASY  (9,  9, 10,  6, 2, 10),  // cols, rows, mines, Q, S, lives
    MEDIUM(13, 13, 26, 7, 3,  8),
    HARD  (16, 16, 44,11, 4,  6);

    public final int cols;
    public final int rows;
    public final int mineCount;
    public final int questionCount;
    public final int surpriseCount;
    public final int lives;

    Difficulty(int cols, int rows,
               int mineCount, int questionCount, int surpriseCount,
               int lives) {
        this.cols = cols;
        this.rows = rows;
        this.mineCount = mineCount;
        this.questionCount = questionCount;
        this.surpriseCount = surpriseCount;
        this.lives = lives;
    }
}
