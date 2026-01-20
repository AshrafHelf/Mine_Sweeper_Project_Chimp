package controller;

import java.util.random.RandomGenerator;
import enums.QuestionLevel;

public class HardQuestionOutcomeTemplate extends QuestionOutcomeTemplate {

    @Override
    protected Outcome onCorrect(QuestionLevel qLevel, RandomGenerator rng) {
        return switch (qLevel) {
            case EASY   -> new Outcome(10, +1);
            case MEDIUM -> new Outcome(15, rng.nextBoolean() ? +1 : +2);
            case HARD   -> new Outcome(20, +2);
            case EXPERT -> new Outcome(40, +3);
        };
    }

    @Override
    protected Outcome onWrong(QuestionLevel qLevel, RandomGenerator rng) {
        return switch (qLevel) {
            case EASY   -> new Outcome(-10, -1);
            case MEDIUM -> new Outcome(-15, rng.nextBoolean() ? -1 : -2);
            case HARD   -> new Outcome(-20, -2);
            case EXPERT -> new Outcome(-40, -3);
        };
    }
}
