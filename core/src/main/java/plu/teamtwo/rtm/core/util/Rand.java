package plu.teamtwo.rtm.core.util;

import java.util.Random;

public class Rand {
    private static final Random random = new Random();

    /**
     * Used to decide if the code will do something based on a probability.
     * @param p Chance this will return true.
     * @return A value of true or false with a Bernoulli distribution whose mean is p.
     */
    public static boolean iWill(float p) {
        if(p < 0.0f || p > 1.0f)
            throw new IllegalArgumentException("The probability must be between 0 and 1");
        return getRandomNum(0.0f, 1.0f) < p;
    }


    /**
     * Generate a random real value.
     * @param min Minimum value the output can be (inclusive).
     * @param max Maximum value the output can be (inclusive).
     * @return A random number in the range [min, max].
     */
    public static float getRandomNum(float min, float max) {
        if(min >= max)
            throw new IllegalArgumentException("Max must be greater than min");

        return random.nextFloat() * (max - min) + min;
    }


    /**
     * Generate a random integer value.
     * @param min Minimum value the output can be (inclusive).
     * @param max Maximum value the output can be (inclusive).
     * @return A random number in the range [min, max].
     */
    public static int getRandomNum(int min, int max) {
        if(min >= max)
            throw new IllegalArgumentException("Max must be greater than min");

        return random.nextInt(max - min + 1) + min;
    }
}
