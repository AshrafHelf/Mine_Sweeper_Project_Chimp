package model;

import java.util.Random;

public class Rng {

    private static final Random RNG = new Random();

    private Rng() {
        // utility class, no instances
    }

    public static Random current() {
        return RNG;
    }
}
