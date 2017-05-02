package plu.teamtwo.rtm.core.util;

import java.util.Random;

public class Rand {
    private static final Random random = new Random();


    /**
     * Used to decide if the code will do something based on a probability.
     *
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
     *
     * @param min Minimum value the output can be (inclusive).
     * @param max Maximum value the output can be (exclusive).
     * @return A random number in the range [min, max].
     */
    public static float getRandomNum(float min, float max) {
        if(min >= max)
            throw new IllegalArgumentException("Max must be greater than min");

        return random.nextFloat() * (max - min) + min;
    }


    /**
     * Generate a random integer value.
     *
     * @param min Minimum value the output can be (inclusive).
     * @param max Maximum value the output can be (inclusive).
     * @return A random number in the range [min, max].
     */
    public static int getRandomNum(int min, int max) {
        if(min >= max)
            throw new IllegalArgumentException("Max must be greater than min");

        return random.nextInt(max - min + 1) + min;
    }


    /**
     * A function designed to get the index which should be dropped with a higher probability of dropping larger index
     * values. This is useful for dropping the lowest scoring members of a species by probability. The function used is
     * Floor( ((n*w + 1) - (n*w + 1)<sup>-x + 1</sup>) / w ) where x is a random number in the range [0, 1), is is the
     * number of indices, and w is a weight factor (large number favor selecting larger indices).
     *
     * @param n Number of items (Size)
     * @param w Weighting factor (As the value approaches zero, it will become an equal distribution, while larger
     *          numbers favor larger indices. The value must be greater than zero.
     * @return An index in the range [0, n)
     */
    public static int randomBackWeightedIndex(int n, float w) {
        final float x = random.nextFloat();
        final float b = (float) n * w + 1.0f;
        return (int) ((b - Math.pow(b, 1.0f - x)) / w);
    }


    /**
     * A function designed to get the index which should be dropped with a higher probability of dropping smaller index
     * values. This is useful for choosing the most fit members for breeding by probability. The function used is
     * Floor( ((n*w + 1)<sup>x</sup> - 1) / w ) where x is a random number in the range [0, 1), is is the
     * number of indices, and w is a weight factor (large numbers favor selecting smaller indices).
     *
     * @param n Number of items (Size)
     * @param w Weighting factor (As the value approaches zero, it will become an equal distribution, while larger
     *          numbers favor smaller indices. The value must be greater than zero.
     * @return An index in the range [0, n)
     */
    public static int randomFrontWeightedIndex(int n, float w) {
        final float x = random.nextFloat();
        final float numerator = (float) (Math.pow(n * w + 1.0f, x) - 1.0f);
        return (int) (numerator / w);
    }


    /**
     * Set the random seed to be used for future calculations.
     *
     * @param seed A seed to be used for calculated values.
     */
    public static void seedRandom(long seed) {
        random.setSeed(seed);
    }
}
