package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.genome.Genome;
import plu.teamtwo.rtm.genome.GenomeBuilder;
import plu.teamtwo.rtm.genome.GenomeCache;
import plu.teamtwo.rtm.neural.ActivationFunction;

import java.util.LinkedList;

public class MultilayerSubstrateEncodingBuilder implements GenomeBuilder {
    private int[] inputs;
    private int[] outputs;
    private LinkedList< int[] > hidden = new LinkedList<>();

    /// Activation function used to process inputs with before calculating.
    ActivationFunction inputFunction = ActivationFunction.LINEAR;
    /// Activation function to use for output nodes.
    ActivationFunction outputFunction = ActivationFunction.SIGMOID;
    /// Activation function to use for hidden nodes on the substrate.
    ActivationFunction hiddenFunction = ActivationFunction.SIGMOID;


    /**
     * Create and return the appropriate type of cache to be used for the given genome.
     *
     * @return The appropriate type of cache for the given genome.
     */
    @Override
    public GenomeCache createCache() {
        return new GraphEncodingCache();
    }


    /**
     * Creates a new, blank genome ready for mutations. This allows the the GAController to distance itself from the
     * specifics of the problem itself.
     *
     * @param cache The cache used in creation.
     * @return A new genome ready for use.
     */
    @Override
    public Genome create(GenomeCache cache) {
        return new MultilayerSubstrateEncoding(this, cache);
    }


    /**
     * Sets the input dimension sizes for the network. If you have an 5x4x3 input space, then pass in an array
     * [5, 4, 3].
     *
     * @param inputDimensions Input dimension sizes.
     */
    public MultilayerSubstrateEncodingBuilder inputs(int[] inputDimensions) {
        inputs = inputDimensions;
        return this;
    }


    /**
     * Sets the output dimension sizes for the network. If you have an 5x4x3 output space, then pass in an array
     * [5, 4, 3].
     *
     * @param outputDimensions Output dimension sizes.
     */
    public MultilayerSubstrateEncodingBuilder outputs(int[] outputDimensions) {
        outputs = outputDimensions;
        return this;
    }


    /**
     * Adds a new hidden layer to the end of the list.
     *
     * @param layerDimensions Dimensions sizes for the layer to add.
     */
    public MultilayerSubstrateEncodingBuilder addLayer(int[] layerDimensions) {
        hidden.add(layerDimensions);
        return this;
    }

    /**
     * Set the activation function to use on inputs before processing the data.
     *
     * @param fn The function to use for inputs to the network.
     */
    public MultilayerSubstrateEncodingBuilder inputFunction(ActivationFunction fn) {
        this.inputFunction = fn;
        return this;
    }


    /**
     * Sets the activation function for outputs from the network. This will, in effect, set the range of possible
     * outputs from the network as well since not all output functions have the same range.
     *
     * @param fn The function to use for outputs form the network.
     */
    public MultilayerSubstrateEncodingBuilder outputFunction(ActivationFunction fn) {
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
    public MultilayerSubstrateEncodingBuilder hiddenFunction(ActivationFunction fn) {
        this.hiddenFunction = fn;
        return this;
    }


    /**
     * Used to construct the double array of layer information and validate it.
     *
     * @return Double array of layers and dimensions for that layer.
     */
    int[][] buildLayers() {
        int[][] layers = new int[2 + hidden.size()][];
        layers[0] = inputs;
        for(int i = 0; i < hidden.size(); ++i)
            layers[i + 1] = hidden.get(i);
        layers[layers.length - 1] = outputs;

        return layers;
    }
}
