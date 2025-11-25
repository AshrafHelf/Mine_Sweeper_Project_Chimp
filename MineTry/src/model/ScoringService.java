package model;

public class ScoringService {

    /**
     * Points gained when revealing a cell.
     * Lives are handled separately in GameEngine.
     */
    public int scoreForReveal(CellType type) {
        return switch (type) {
            case MINE -> 0; // hitting a mine costs lives, not points
            case NUMBER, EMPTY, QUESTION, SURPRISE -> 1;
        };
    }

    /**
     * Points gained/lost when changing a flag on a cell.
     *
     * @param addingFlag true if we're placing a flag, false if removing
     */
    public int scoreForFlag(CellType type, boolean addingFlag) {
        if (!addingFlag) {
            // Design choice: removing a flag does NOT change score.
            // (You could also reverse the previous effect if you want.)
            return 0;
        }

        return switch (type) {
            case MINE -> 1;  // correct flag on a mine
            case NUMBER, EMPTY, QUESTION, SURPRISE -> -3; // bad flag
        };
    }

    // Later we’ll add:
    // - getActivationCost(Difficulty diff)
    // - applySurpriseGood/Bad(...)
    // - applyQuestionResult(...)
}
