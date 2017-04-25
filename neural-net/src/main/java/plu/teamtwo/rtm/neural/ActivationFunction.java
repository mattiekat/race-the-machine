package plu.teamtwo.rtm.neural;

import java.util.function.Function;

/**
 * This defines a function used for activation on a Neuron. Each value has with it associated a function which is able
 * connection calculate the value when called.
 *
 * Take a look at https://en.wikipedia.org/wiki/Activation_function for more information.
 */
public enum ActivationFunction {
    ABS(        (Float x) -> x >= 0 ? x : -x                                ),
    GAUSSIAN(   (Float x) -> (float)Math.exp( -(x * x) )                    ),
    LINEAR(     (Float x) -> x                                              ),
    SIGMOID(    (Float x) -> (1.0f / (1.0f + (float)Math.exp(-x)))          ),
    SINC(       (Float x) -> (float)Math.sin(x) / x                         ),
    SINUSOID(   (Float x) -> (float)Math.sin(x)                             ),
    SOFTPLUS(   (Float x) -> (float)Math.log1p(Math.exp(x))                 ),
    SYMETRIC(   (Float x) -> (x >= 0 ? -x : x) + 1.0f                       ),
    TANH(       (Float x) -> (float)Math.tanh(x)                            );


    private final Function<Float, Float> function;


    /**
     * Constructor which sets the function of this specific type.
     * @param fn The function connection use for activation.
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
}