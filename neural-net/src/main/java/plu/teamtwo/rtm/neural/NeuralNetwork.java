package plu.teamtwo.rtm.neural;

/**
 * This is an ANN which can be run on inputs and then will provide outputs based on that.
 * ANN's are not to be modified once made, if a new one is desired with different structure,
 * it will need to be re-built. This is designed to work in tandem with Genome.
 * <p>
 * To use a NeuralNetwork, first construct it with the correct information about the number of different node types.
 * Next call setNeuron for all neurons, keep in mind that [0, endInput) will be the inputs,
 * [endInput, endOutput) will be the output nodes, and [endOutput, neurons.length) will be the hidden nodes.
 * Finally call validate() which will finalize the structure, enabling it to be calculated.
 * <p>
 */
public interface NeuralNetwork {

    /**
     * Calculate the outputs of the neural network given the inputs. Note individual implementations are responsible for
     * mapping the inputs/outputs to n-dimensional space if necessary.
     *
     * @param inputs Array of values to set the input neurons to.
     * @return Output of the network.
     */
    float[] calculate(float... inputs);

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
    float[] step(float... inputs);

    /**
     * Empty the network of all stored values. This should be called between tests.
     */
    void flush();

    /**
     * Determines if the neural network has recurrent cycles in it.
     *
     * @return True if it is a recurrent neural network.
     */
    boolean isRecurrent();

    /**
     * Get the number of inputs the network expects to receive.
     *
     * @return Number of expected inputs.
     */
    int inputs();

    /**
     * Get the number of outputs the network will produced when calculate or step is called.
     *
     * @return Number of outputs produced by the network.
     */
    int outputs();
}