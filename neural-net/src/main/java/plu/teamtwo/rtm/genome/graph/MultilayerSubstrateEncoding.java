package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.genome.Genome;
import plu.teamtwo.rtm.genome.GenomeCache;
import plu.teamtwo.rtm.neural.ActivationFunction;
import plu.teamtwo.rtm.neural.NeuralNetwork;
import plu.teamtwo.rtm.neural.SubstrateNetwork;
import plu.teamtwo.rtm.neural.SubstrateNetworkBuilder;

import java.util.Arrays;


/**
 * This is the encoding used by HyperNEAT-LEO. Internally it uses a CPPN to encode the transition weights in the
 * resulting SubstrateNetwork.
 */
public class MultilayerSubstrateEncoding implements Genome {
    /// Threshold for a link to be expressed
    private static final float LEO_THRESHOLD = 0.5f;
    /// Range of output used as a weight in substrate network, forms the range [-WIGHT_RANGE, +WEIGHT_RANGE]
    private static final float WEIGHT_RANGE = 3.0f;

    /// Activation function used to process inputs with before calculating.
    private final ActivationFunction inputFunction;
    /// Activation function to use for output nodes.
    private final ActivationFunction outputFunction;
    /// Activation function to use for hidden nodes on the substrate.
    private final ActivationFunction hiddenFunction;
    private int[][] layers;
    ///mappings from n-dimensional space into 1D vector of each layer's nodes
    private int[][] mappingProducts;
    private int[] layerSizes;
    /// The cppn used for calculating the internal connections. Note that the cppn should be called with
    ///  x1, y1, z1, ..., x2, y2, z2, ..., bias such that the the coordinates for each dimension are next to each other.
    ///  Outputs are in the form of W1, T1, W2, T2, ... where each Wi and Ti are the weight and link expression output
    ///  for the ith layer connecting to the ith + 1 layer.
    private GraphEncoding cppn;


    /**
     * Construct a new MultilayerSubstrateEncoding using a builder and a cache. This will construct the necessary CPPN
     * internally to calculate the weight values.
     *
     * @param builder Builder with the needed information to construct an instance.
     * @param cache   Information about the nodes and edges needed by the CPPN for initialization.
     */
    MultilayerSubstrateEncoding(MultilayerSubstrateEncodingBuilder builder, GenomeCache cache) {
        layers = builder.buildLayers();
        layerSizes = SubstrateNetwork.calculateLayerSizes(layers);
        inputFunction = builder.inputFunction;
        outputFunction = builder.outputFunction;
        hiddenFunction = builder.hiddenFunction;

        //calculate the mapping products for use in creating the neural network
        mappingProducts = new int[layers.length][];
        for(int layer = 0; layer < layers.length; ++layer)
            mappingProducts[layer] = calculateMappingProducts(layers[layer], false);

        //construct the CPPN with outputs for each layer transition and enough inputs to support the largest
        // transition's dimensional space
        int maxTransDimen = 1;
        for(int i = 0; i < layers.length - 1; ++i)
            maxTransDimen = Math.max(layers[0].length + layers[1].length, maxTransDimen);

        final int inputs = maxTransDimen + 1; // add a bias node
        final int outputs = (layers.length - 1) * 2; //multiply by two for LEO extension
        GraphEncodingBuilder cppnBuilder = new GraphEncodingBuilder()
                                                   .inputs(inputs)
                                                   .outputs(outputs)
                                                   .randomActivations()
                                                   .inputFunction(ActivationFunction.LINEAR)
                                                   .outputFunction(ActivationFunction.SIGMOID);

        seedLEO(cppnBuilder, layers, inputs, outputs);
        connectInputs(cppnBuilder, layers, inputs, outputs);

        cppn = (GraphEncoding) cppnBuilder.create(cache);
    }


    /**
     * Create a deep copy of the encoding allowing them to be modified independently of each other.
     *
     * @param other The encoding to copy.
     */
    private MultilayerSubstrateEncoding(MultilayerSubstrateEncoding other) {
        this(other, other.cppn.duplicate());
    }


    /**
     * Create a deep copy of everything except the internal CPPN; instead use the new one passed as a parameter.
     *
     * @param other Encoding to copy.
     * @param cppn  CPPN to use in new instance.
     */
    private MultilayerSubstrateEncoding(MultilayerSubstrateEncoding other, GraphEncoding cppn) {
        layers = new int[other.layers.length][];
        for(int i = 0; i < layers.length; ++i)
            layers[i] = Arrays.copyOf(other.layers[i], other.layers[i].length);

        layerSizes = Arrays.copyOf(other.layerSizes, other.layerSizes.length);

        this.cppn = cppn;

        inputFunction = other.inputFunction;
        outputFunction = other.outputFunction;
        hiddenFunction = other.hiddenFunction;
    }


    /**
     * Create the gaussian functions for each layer to seed the link expression outputs. This function requires that no
     * hidden nodes have been added to the builder already.
     *
     * @param builder Builder to add connections to.
     * @param layers  Dimensions of each layer.
     * @param inputs  Number of input nodes.
     * @param outputs Number of output nodes.
     * @return The builder for chaining
     */
    private static GraphEncodingBuilder seedLEO(GraphEncodingBuilder builder, int[][] layers, final int inputs, final int outputs) {
        int hiddenIndex = inputs + outputs;

        //for each of the layers create connections between corresponding dimensions to the hidden gaussian and from the
        // gaussian to the appropriate LEO output
        for(int i = 0; i < layers.length - 1; ++i) {
            final int d1 = layers[i].length;
            final int d2 = layers[i + 1].length;

            final int leo = inputs + i * 2 + 1; //index of link expression output

            //for all the relateable dimensions of the two layers, create a gaussian node and connect it to the output
            for(int j = 0; j < d1 && j < d2; ++j) {
                final int input1 = j;       //index of the first input
                final int input2 = d1 + j;  //index of the second input

                // create a gaussian node for this dimensional relationship
                builder.addHidden(ActivationFunction.GAUSSIAN, 1)
                        .connect(input1, hiddenIndex, 1.0f)            //connect from x1 to hidden
                        .connect(input2, hiddenIndex, -1.0f)           //connect from x2 to hidden
                        .connect(hiddenIndex, leo, 1.0f);              //connect from hidden to LEO

                hiddenIndex++;
            }

            //connect the bias to the LEO and use a weight which is the negative number of gaussian nodes imputing to it
            builder.connect(inputs - 1, inputs + i * 2 + 1, -((float) Math.min(d1, d2)));
        }

        return builder;
    }


    /**
     * Create a connection from every input that is used by the layer to every weight output.
     *
     * @param builder The builder to add the connections to.
     * @param layers  Dimensions of each layer.
     * @param inputs  Number of input nodes.
     * @param outputs Number of output nodes.
     * @return The builder for chaining.
     */
    private static GraphEncodingBuilder connectInputs(GraphEncodingBuilder builder, int[][] layers, final int inputs, final int outputs) {
        final int bias = inputs - 1; //index location of the bias node

        //create connection from every input to every weight output for each layer
        for(int i = 0; i < layers.length - 1; ++i) { //for all layers
            final int dimensions = layers[i].length + layers[i + 1].length;
            final int output = inputs + (i * 2); //index of output node for current layer

            //create connection from every input to its weight output that is used by this layer
            for(int input = 0; input < dimensions; ++input)
                builder.connect(input, output, 1.0f);

            //connect the bias to the weight output
            builder.connect(bias, output, 1.0f);
        }

        return builder;
    }


    /**
     * Used for initial members of the first generation to create connections between the inputs and outputs. This
     * should not be needed after the first generation. It is recommended that mutate be called after this function to
     * give the initial species some variation.
     *
     * @param cache Cached information about the Genome.
     */
    @Override
    public void initialize(GenomeCache cache) {
        //TODO: remove initialize function?
    }


    /**
     * Create a deep copy of the genome. This will enable the copy to be modified without altering the original.
     *
     * @return A duplicate of the current instance.
     */
    @Override
    public Genome duplicate() {
        return new MultilayerSubstrateEncoding(this);
    }


    /**
     * Used to create a new cache of the appropriate type.
     *
     * @return A new cache for the specific instance of Genome.
     */
    @Override
    public GenomeCache createCache() {
        return new GraphEncodingCache();
    }


    /**
     * Make random alterations to the genome (i.e. mutations).
     *
     * @param cache Cached information about the genome.
     */
    @Override
    public void mutate(GenomeCache cache) {
        cppn.mutate(cache);
    }


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param cache   Cached information about the genome.
     * @param p1f     First parent fitness.
     * @param p2      Second parent.
     * @param p2f     Second parent fitness.
     * @param average True if matching values should be averaged instead of randomly chosen.
     * @return A child which is the result of crossing the genomes
     */
    @Override
    public Genome crossMultipoint(GenomeCache cache, float p1f, Genome p2, float p2f, boolean average) {
        GraphEncoding child = cppn.crossMultipoint(cache, p1f, ((MultilayerSubstrateEncoding) p2).cppn, p2f, average);
        return new MultilayerSubstrateEncoding(this, child);
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different the two genomes are.
     *
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    @Override
    public float compatibilityDistance(Genome other) {
        return cppn.compatibilityDistance(((MultilayerSubstrateEncoding) other).cppn);
    }


    /**
     * Create a runnable ANN which is represented by the genome.
     *
     * @return The ANN represented by the genome.
     */
    @Override
    public NeuralNetwork constructNeuralNetwork() {
        //calculate outputs of CPPN for each input output pairing and then use those for the network

        final NeuralNetwork network = cppn.constructNeuralNetwork();

        final int numTransitions = layers.length - 1; //number of transitions between layers
        float[][][] weights = new float[numTransitions][][];

        //for all the layers, set weights of each input, output pair
        for(int layer = 0; layer < numTransitions; ++layer) {
            final int inputLayerSize = layerSizes[layer];       //number of nodes on input substrate
            final int outputLayerSize = layerSizes[layer + 1];  //number of nodes on output substrate

            final int[] inputDimensions = layers[layer];
            final int[] outputDimensions = layers[layer + 1];

            final int weightIndex = layer * 2;  //index in output of the weight for current layer
            final int leoIndex = layer * 2 + 1; //index in output of the link expression value for current layer

            weights[layer] = new float[outputLayerSize][inputLayerSize];
            final float[][] layerWeights = weights[layer];

            //for all outputs, construct a vector of input weights
            for(int out = 0; out < outputLayerSize; ++out) {

                //for all inputs, calculate the weight to the output
                for(int in = 0; in < inputLayerSize; ++in) {
                    //normalize the input and output coordinates to a value between -1 and 1 for all dimensions
                    final float[] inpos = mapPosition(inputDimensions, in);
                    final float[] outpos = mapPosition(outputDimensions, out);

                    //make sure we did not do a dumb since the copy may prevent errors from being pronounced
                    if(inpos.length != inputDimensions.length)
                        throw new RuntimeException("Error mapping input node to CPPN.");
                    if(outpos.length != outputDimensions.length)
                        throw new RuntimeException("Error mapping output node to CPPN.");

                    //copy the mappings into a single input for the network
                    final float[] combpos = new float[network.inputs()];
                    System.arraycopy(inpos, 0, combpos, 0, inpos.length);
                    System.arraycopy(outpos, 0, combpos, inpos.length, outpos.length);
                    combpos[combpos.length - 1] = 1; //set bias

                    //get the outputs of the network, then check for LEO and if above threshold, use the weight
                    final float[] outputs = network.calculate(combpos);
                    if(outputs[leoIndex] > LEO_THRESHOLD) {
                        float weight = outputs[weightIndex];
                        //we use SIGMOID output so this will normalize it to be within +/- WEIGHT_RANGE
                        weight = (weight * 2.0f - 1.0f) * WEIGHT_RANGE;
                        layerWeights[out][in] = weight;
                    }
                }
            }
        }

        //build the new network
        return new SubstrateNetworkBuilder()
                       .inputFunction(inputFunction)
                       .outputFunction(outputFunction)
                       .hiddenFunction(hiddenFunction)
                       .layers(layers)
                       .weights(weights)
                       .create();
    }


    /**
     *
     * @param dimensions
     * @param p
     * @return
     */
    private float[] mapPosition(int[] dimensions, int p) {
        //TODO: map the positions from 1D vector to n-dimensional space and normalize each coordinate to be between -1 and 1
        return null;
    }


    /**
     * Calculate the products needed for each dimension using dynamic programming.
     * i.e. Let each Xi be the product of all Dj dimension sizes where n > j > i.
     * <p>
     * Use this for calculating the index of a n-dimensional point in a vector. Take the point (1, 2, 3) in 3D space,
     * you would want to multiply 1 by the maximum size of the second two dimensions, and 2 by the maximum size of the
     * third dimension, and finally add the value 3 because it is the lowest order dimension. Thus let the result of
     * this function be P, and the correct index would be 1*p[0] + 2*p[2] + 3*p[3].
     *
     * @param d       Array of dimensions where d[0] is the highest order dimension and d[n-1] is the lowest order
     * @param inplace Calculate the values in-place, otherwise makes a copy first.
     * @return An array such that each Pi is the product of all Dj where i < j < n.
     */
    private static int[] calculateMappingProducts(int[] d, boolean inplace) {
        int[] p = inplace ? d : Arrays.copyOf(d, d.length);

        //calculate from the rear to make use of past calculations
        int last = p[p.length - 1];
        p[p.length - 1] = 1;

        for(int i = p.length - 2; i >= 0; --i) {
            int saved = p[i];
            p[i] = last * p[i + 1];
            last = saved;
        }

        return p;
    }
}
