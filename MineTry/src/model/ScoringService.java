package model;


import model.CellType;


public class ScoringService {
public int scoreForReveal(CellType type) {
return switch (type) {
case MINE -> 1; // auto-reveal via question may give 0 in engine
case NUMBER, EMPTY -> 1;
case SURPRISE, QUESTION -> 0; // activation has separate costs/rewards
};
}
}