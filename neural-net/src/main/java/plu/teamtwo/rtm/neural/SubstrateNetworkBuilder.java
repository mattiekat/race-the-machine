package plu.teamtwo.rtm.neural;

public class SubstrateNetworkBuilder implements NeuralNetworkBuilder {
    /// Defines the dimensions of each layer, e.g. d[0] = [2, 3] would define an input of 2 by 3 (output is final layer)
    ///  this also defines the mapping of the input arrays to the first substrate and so on.
    int[][] layers;

    /// Defines weights between substrates (e.g. weights[0] defines a weight matrix between input and next substrate).
    ///  Stored as (x2, y2, ..., x1, y1, ...), i.e. (output, input)
    float[][] weights;

    /// Activation function used to process inputs with before calculating.
    ActivationFunction inputFunction = ActivationFunction.LINEAR;
    /// Activation function to use for output nodes.
    ActivationFunction outputFunction = ActivationFunction.SIGMOID;
    /// Activation function to use for hidden nodes on the substrate.
    ActivationFunction hiddenFunction = ActivationFunction.SIGMOID;


    /**
     * Create a new neural network given the internal settings.
     *
     * @return The new neural network.
     */
    @Override
    public NeuralNetwork create() {
        return new SubstrateNetwork(this);
    }


    /**
     * Define the dimensions of each layer, e.g. d[0] = [2, 3] would define an input of 2 by 3 (output is final layer)
     * this also defines the mapping of the input arrays to the first substrate and so on.
     */
    public SubstrateNetworkBuilder layers(int[][] layers) {
        this.layers = layers;
        return this;
    }


    /**
     * Define weights between substrates (e.g. weights[0] defines a weight matrix between input and next substrate).
     * Stored as (x2, y2, ..., x1, y1, ...), i.e. (output, input)
     */
    public SubstrateNetworkBuilder weights(float[][] weights) {
        this.weights = weights;
        return this;
    }


    /**
     * Set the activation function to use on inputs before processing the data.
     *
     * @param fn The function to use for inputs to the network.
     */
    public SubstrateNetworkBuilder inputFunction(ActivationFunction fn) {
        this.inputFunction = fn;
        return this;
    }


    /**
     * Sets the activation function for outputs from the network. This will, in effect, set the range of possible
     * outputs from the network as well since not all output functions have the same range.
     *
     * @param fn The function to use for outputs form the network.
     */
    public SubstrateNetworkBuilder outputFunction(ActivationFunction fn) {
        this.outputFunction = fn;
        return this;
    }


    /**
     * Set the hidden function to use. This setting is not used if we allow
     * for random activations.
     *
     * @param fn The function to use for hidden nodes within the network if we do not allow for random activation
     *           functions.
     */
    public SubstrateNetworkBuilder hiddenFunction(ActivationFunction fn) {
        this.hiddenFunction = fn;
        return this;
    }
}
