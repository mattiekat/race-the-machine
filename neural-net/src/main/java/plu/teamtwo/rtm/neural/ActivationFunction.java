package plu.teamtwo.rtm.neural;

import java.util.function.Function;

/**
 * This defines a function used for activation on a Neuron. Each value has with it associated a function which is able
 * to calculate the value when called.
 *
 * Take a look at https://en.wikipedia.org/wiki/Activation_function for more information.
 */
public enum ActivationFunction {
    SIGMOID( (Float x) -> (1.0f / (1.0f + exp(-x))) ),
    LINEAR( (Float x) -> x );


    private final Function<Float, Float> function;


    /**
     * Constructor which sets the function of this specific type.
     * @param fn The function to use for activation.
     */
    ActivationFunction(Function<Float, Float> fn) {
        function = fn;
    }


    /**
     * Calcualate the activation function given x.
     * @param x The input.
     * @return activation(x)
     */
    float calculate(float x) {
        return function.apply(x);
    }


    /**
     * Approximation of the e^x function. Accurate to within 1e-3;
     * @return an approximation of e^x.
     */
    private static float exp(float x) {
        //http://www.javamex.com/tutorials/math/exp.shtml
        x = 1.0f + x / 256.0f;
        x *= x; x *= x; x *= x; x *= x;
        x *= x; x *= x; x *= x; x *= x;
        return x;
    }
}