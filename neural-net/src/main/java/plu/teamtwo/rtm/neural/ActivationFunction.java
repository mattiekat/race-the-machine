package plu.teamtwo.rtm.neural;

import plu.teamtwo.rtm.core.util.Rand;

import java.util.function.Function;

/**
 * This defines a function used for activation on a Neuron. Each value has with it associated a function which is able
 * to calculate the value when called.
 * <p>
 * Take a look at https://en.wikipedia.org/wiki/Activation_function for more information.
 */
public enum ActivationFunction {
    ABS((Float x) -> x >= 0 ? x : -x),
    GAUSSIAN((Float x) -> (float) Math.exp(-(x * x))),
    LINEAR((Float x) -> x),
    SIGMOID((Float x) -> (1.0f / (1.0f + (float) Math.exp(-x)))),
    SINC((Float x) -> x == 0.0f ? 1.0f : (float) Math.sin(x) / x),
    SINUSOID((Float x) -> (float) Math.sin(x)),
    SOFTPLUS((Float x) -> (float) Math.log1p(Math.exp(x))),
    SYMETRIC((Float x) -> (x >= 0 ? -x : x) + 1.0f),
    TANH((Float x) -> (float) Math.tanh(x));


    private final Function<Float, Float> function;


    /**
     * Constructor which sets the function of this specific type.
     *
     * @param fn The function to use for activation.
     */
    ActivationFunction(Function<Float, Float> fn) {
        function = fn;
    }


    /**
     * Select a random activation function.
     *
     * @return A random activation function.
     */
    public static ActivationFunction randomActivationFunction() {
        ActivationFunction[] fns = ActivationFunction.class.getEnumConstants();
        int x = Rand.getRandomNum(0, fns.length - 1);
        return fns[x];
    }


    /**
     * Bound a number to a range. The result of this function will be in the range [min, max]. If the number goes out of
     * bounds in either direction, it will be set to the boundary it surpassed.
     *
     * @param min The minimum value the number can take.
     * @param max The maximum value the number can take.
     * @param num The number to bound.
     * @return A number which is within the range [min, max]
     */
    public static float bound(float min, float max, float num) {
        num = num < min ? min : num;
        num = num > max ? max : num;
        return num;
    }


    /**
     * Calcualate the activation function given x.
     *
     * @param x The input.
     * @return activation(x)
     */
    float calculate(float x) {
        return function.apply(x);
    }
}