package plu.teamtwo.rtm.neural;

import java.util.Collection;
import java.util.LinkedList;

class Neuron {
    final int id;
    final LinkedList<Dendrite> connections = new LinkedList<>();
    final FunctionType function;

    private float input = 0.0f;
    private float output = 0.0f;


    Neuron(int id, FunctionType functionType) {
        this.id = id;
        function = functionType;
    }


    void inputValue(float input) {
        this.input += input;
    }


    /**
     * Calculate the value for this neuron
     * @return The calculated value.
     */
    float calculate() {
        output = function.calculate(input);
        input = 0.0f;
        return output;
    }


    float getOutput() {
        return output;
    }
}