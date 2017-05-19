package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.core.util.Pair;
import plu.teamtwo.rtm.core.util.Triple;
import plu.teamtwo.rtm.genome.Genome;
import plu.teamtwo.rtm.genome.GenomeBuilder;
import plu.teamtwo.rtm.genome.GenomeCache;
import plu.teamtwo.rtm.neural.ActivationFunction;

import java.util.LinkedList;
import java.util.List;

public class GraphEncodingBuilder implements GenomeBuilder {
    int inputs = 0, outputs = 0;

    /// Default activation function to use for input nodes if a random one cannot be selected.
    ActivationFunction inputFunction = ActivationFunction.LINEAR;
    /// Activation function to use for output nodes. (Output node will not mutate their activation function)
    ActivationFunction outputFunction = ActivationFunction.SIGMOID;
    /// Default activation function to use for hidden nodes if a random one cannot be selected.
    ActivationFunction hiddenFunction = ActivationFunction.TANH;

    /// Should the genome be able to randomize its activation functions (except for output nodes)?
    boolean randomActivations = false;
    /// Use the step stepping function instead of the calculate function
    boolean realTimeProcessing = false;
    List<ActivationFunction> hiddenNodes = new LinkedList<>();
    List<Triple<Integer, Integer, Float>> initialConnections = null;


    /**
     * Set the number of inputs.
     *
     * @param inputs Number of inputs the system should accept.
     */
    public GraphEncodingBuilder inputs(int inputs) {
        this.inputs = inputs;
        return this;
    }


    /**
     * Set the number of outputs.
     *
     * @param outputs Number of outputs the system should generate.
     */
    public GraphEncodingBuilder outputs(int outputs) {
        this.outputs = outputs;
        return this;
    }


    /**
     * Allow the graph encoding to have different activation functions randomly mutated. Call this if you are creating
     * a CPPN, as it needs to be able to randomize said functions.
     */
    public GraphEncodingBuilder randomActivations() {
        randomActivations = true;
        return this;
    }


    /**
     * Use the step function of the network rather than calculate. This does not work for discrete input output pairs.
     */
    public GraphEncodingBuilder realTimeProcessing() {
        realTimeProcessing = true;
        return this;
    }


    /**
     * Specify that this graph encoding should start with no initial connections. This does not need to be called if
     * manually setting connections.
     */
    public GraphEncodingBuilder emptyInit() {
        initialConnections = new LinkedList<>();
        return this;
    }


    /**
     * Manually specify a connection between two nodes. If this is used, then it must be used for all connections that
     * should exist.
     *
     * @param f From node; the starting node of the connection.
     * @param t To node; the destination node of the connection.
     * @param w Weight of the connection.
     */
    public GraphEncodingBuilder connect(int f, int t, float w) {
        if(initialConnections == null) initialConnections = new LinkedList<>();
        initialConnections.add(new Triple<>(f, t, w));
        return this;
    }


    /**
     * Add a hidden node (or a couple) to the initial state of the network. If this is used, manual connections should
     * be specified or the nodes will not be connected at initialization. Added hidden nodes will have IDs which start
     * at (inputs + outputs - 1) and will extend for the number which are added.
     *
     * @param fn  Activation function to be used.
     * @param num Number of this type of hidden node to add in a row.
     */
    public GraphEncodingBuilder addHidden(ActivationFunction fn, int num) {
        for(int x = 0; x < num; ++x)
            hiddenNodes.add(fn);

        return this;
    }


    /**
     * Set the default input function to use if one cannot be randomly selected. This setting is not used if we allow
     * for random activations.
     *
     * @param fn The function to use for inputs to the network if we do not allow for random activation functions.
     */
    public GraphEncodingBuilder inputFunction(ActivationFunction fn) {
        this.inputFunction = fn;
        return this;
    }


    /**
     * Sets the output function to use for the network. This will, in effect, set the range of possible outputs from the
     * network as well since not all output functions have the same range.
     *
     * @param fn The function to use for outputs form the network.
     */
    public GraphEncodingBuilder outputFunction(ActivationFunction fn) {
        this.outputFunction = fn;
        return this;
    }


    /**
     * Set the default hidden function to use if one cannot be randomly selected. This setting is not used if we allow
     * for random activations.
     *
     * @param fn The function to use for hidden nodes within the network if we do not allow for random activation
     *           functions.
     */
    public GraphEncodingBuilder hiddenFunction(ActivationFunction fn) {
        this.hiddenFunction = fn;
        return this;
    }


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
     * @param cache Cached information about the generation.
     * @return A new genome ready for use.
     */
    @Override
    public Genome create(GenomeCache cache) {
        return new GraphEncoding(this, (GraphEncodingCache) cache);
    }
}
