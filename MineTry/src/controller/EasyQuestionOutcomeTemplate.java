package controller;

import java.util.random.RandomGenerator;
import enums.QuestionLevel;

public class EasyQuestionOutcomeTemplate extends QuestionOutcomeTemplate {

    @Override
    protected Outcome onCorrect(QuestionLevel qLevel, RandomGenerator rng) {
        return switch (qLevel) {
            case EASY   -> new Outcome(3, +1);
            case MEDIUM -> new Outcome(6, 0);
            case HARD   -> new Outcome(10, 0);
            case EXPERT -> new Outcome(15, +2);
        };
    }

    @Override
    protected Outcome onWrong(QuestionLevel qLevel, RandomGenerator rng) {
        return switch (qLevel) {
            case EASY -> {
                // (-3) OR nothing
                yield rng.nextBoolean() ? new Outcome(-3, 0) : new Outcome(0, 0);
            }
            case MEDIUM -> {
                // (-6) OR nothing
                yield rng.nextBoolean() ? new Outcome(-6, 0) : new Outcome(0, 0);
            }
            case HARD -> new Outcome(-10, 0);
            case EXPERT -> new Outcome(-15, -1);
        };
    }
}
