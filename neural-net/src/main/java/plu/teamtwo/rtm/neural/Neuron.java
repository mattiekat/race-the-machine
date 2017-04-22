package plu.teamtwo.rtm.neural;

import java.util.LinkedList;

class Neuron {
    final int id;
    final LinkedList<Dendrite> connections = new LinkedList<>();
    final ActivationFunction function;

    private float input = 0.0f;
    private float output = 0.0f;


    Neuron(int id, ActivationFunction activationFunction) {
        this.id = id;
        function = activationFunction;
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