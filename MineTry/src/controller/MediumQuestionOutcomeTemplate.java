package controller;

import java.util.random.RandomGenerator;
import enums.QuestionLevel;

public class MediumQuestionOutcomeTemplate extends QuestionOutcomeTemplate {

    @Override
    protected Outcome onCorrect(QuestionLevel qLevel, RandomGenerator rng) {
        return switch (qLevel) {
            case EASY   -> new Outcome(8, +1);
            case MEDIUM -> new Outcome(10, +1);
            case HARD   -> new Outcome(15, +1);
            case EXPERT -> new Outcome(20, +2);
        };
    }

    @Override
    protected Outcome onWrong(QuestionLevel qLevel, RandomGenerator rng) {
        return switch (qLevel) {
            case EASY   -> new Outcome(-8, 0);
            case MEDIUM -> {
                // (-10 & -1♥) OR nothing
                yield rng.nextBoolean() ? new Outcome(-10, -1) : new Outcome(0, 0);
            }
            case HARD   -> new Outcome(-15, -1);
            case EXPERT -> {
                // (-20 & -1♥) OR (-20 & -2♥)
                yield new Outcome(-20, rng.nextBoolean() ? -1 : -2);
            }
        };
    }
}
