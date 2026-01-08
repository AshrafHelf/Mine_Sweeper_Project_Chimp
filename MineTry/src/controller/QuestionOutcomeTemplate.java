package controller;

import java.util.random.RandomGenerator;
import enums.QuestionLevel;

public abstract class QuestionOutcomeTemplate {

    // TEMPLATE METHOD (final skeleton)
    public final Outcome decide(QuestionLevel level, boolean correct, RandomGenerator rng) {
        if (correct) {
            return onCorrect(level, rng);
        }
        return onWrong(level, rng);
    }

    protected abstract Outcome onCorrect(QuestionLevel level, RandomGenerator rng);
    protected abstract Outcome onWrong(QuestionLevel level, RandomGenerator rng);

    // shared message builder (same for all)
    public final String buildMessage(boolean correct, Outcome o) {
        StringBuilder sb = new StringBuilder();
        sb.append(correct ? "Correct answer!\n" : "Wrong answer!\n");

        if (o.points() != 0) {
            sb.append(o.points() > 0 ? "+" : "").append(o.points()).append(" pts\n");
        }
        if (o.heartsDelta() != 0) {
            sb.append(o.heartsDelta() > 0 ? "+" : "").append(o.heartsDelta()).append(" \u2665");
        }
        if (o.points() == 0 && o.heartsDelta() == 0) {
            sb.append("No change in score or lives.");
        }
        return sb.toString();
    }
}
