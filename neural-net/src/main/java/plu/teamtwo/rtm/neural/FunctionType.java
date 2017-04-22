package plu.teamtwo.rtm.neural;

import java.util.function.Function;

//https://en.wikipedia.org/wiki/Activation_function
public enum FunctionType {
    SIGMOID((Float input) -> {
        return 1.0f / (1.0f + exp(-input));
    });


    private final Function<Float, Float> function;

    FunctionType(Function<Float, Float> fn) {
        function = fn;
    }

    float calculate(float x) {
        return function.apply(x);
    }

    /**
     * Approximation of the e^x function.
     */
    private static float exp(float x) {
        //http://www.javamex.com/tutorials/math/exp.shtml
        x = 1.0f + x / 256.0f;
        x *= x; x *= x; x *= x; x *= x;
        x *= x; x *= x; x *= x; x *= x;
        return x;
    }
}
