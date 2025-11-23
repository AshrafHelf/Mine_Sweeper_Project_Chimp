package model;



import java.util.random.RandomGenerator;


public class Rng {
private static final RandomGenerator RNG = RandomGenerator.getDefault();
public static RandomGenerator current(){ return RNG; }
}