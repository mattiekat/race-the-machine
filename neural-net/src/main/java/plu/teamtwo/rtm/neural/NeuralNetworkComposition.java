package plu.teamtwo.rtm.neural;

import plu.teamtwo.rtm.core.util.Pair;

public class NeuralNetworkComposition extends Pair<NeuralNetwork, NeuralNetwork> implements NeuralNetwork {
    private NeuralNetworkComposition() {
        super();
    }


    public NeuralNetworkComposition(NeuralNetwork a, NeuralNetwork b) {
        super(a, b);
    }


    /**
     * Calculate the outputs of the neural network given the inputs. Note individual implementations are responsible for
     * mapping the inputs/outputs to n-dimensional space if necessary.
     *
     * @param inputs Array of values to set the input neurons to.
     * @return Output of the network.
     */
    @Override
    public float[] calculate(float... inputs) {
        return b.calculate(a.calculate(inputs));
    }


    /**
     * Steps values through the neural network by processing from the final nodes to the initial nodes. This could be
     * used with real-time applications where direct input-output pairing are not so important as temporal
     * comprehension. Note individual implementations are responsible for mapping the inputs/outputs to n-dimensional
     * space if necessary.
     * <p>
     * Flush should not be called between calls to step.
     *
     * @param inputs Array of values to set the input neurons to.
     * @return Array of values from the output neurons.
     */
    @Override
    public float[] step(float... inputs) {
        return b.step(a.step(inputs));
    }


    /**
     * Empty the network of all stored values. This should be called between tests.
     */
    @Override
    public void flush() {
        a.flush();
        b.flush();
    }


    /**
     * Determines if the neural network has recurrent cycles in it.
     *
     * @return True if it is a recurrent neural network.
     */
    @Override
    public boolean isRecurrent() {
        return a.isRecurrent() || b.isRecurrent();
    }
}