package plu.teamtwo.rtm.neural;

import java.security.InvalidParameterException;

/**
 * Represents a substrate network which has only a few outputs at definable coordinates.
 */
public class SubstrateNetwork implements NeuralNetwork {
    /// Defines the dimensions of each layer, e.g. d[0] = [2, 3] would define an input of 2 by 3 (output is final layer)
    ///  this also defines the mapping of the input arrays to the first substrate and so on.
    private final int[][] layers;

    /// Defines weights between substrates (e.g. weights[0] defines a weight matrix between input and next substrate).
    ///  Stored as (x2, y2, ..., x1, y1, ...), i.e. (output, input)
    private final float[][] weights;

    /// Product of each layer's dimensions
    private final transient int[] layerSizes;

    /// Activation function used to process inputs with before calculating.
    private final ActivationFunction inputFunction;
    /// Activation function to use for output nodes.
    private final ActivationFunction outputFunction;
    /// Activation function to use for hidden nodes on the substrate.
    private final ActivationFunction hiddenFunction;


    /**
     * Create a new substrate network with the information provided by a builder.
     */
    SubstrateNetwork(SubstrateNetworkBuilder builder) {
        layers = builder.layers;
        weights = builder.weights;

        inputFunction  = builder.inputFunction;
        outputFunction = builder.outputFunction;
        hiddenFunction = builder.hiddenFunction;

        if(layers.length < 2)
            throw new InvalidParameterException("Must at minimum have an input and output layer.");
        if(weights.length != layers.length - 1)
            throw new InvalidParameterException("Invalid number of weights for the layers.");

        layerSizes = calculateLayerSizes(layers);

        for(int i = 0; i < weights.length; ++i)
            if(weights[i].length != layerSizes[i] * layerSizes[i + 1])
                throw new InvalidParameterException("Weights must have exactly 1 value for every input and output combination between layers.");
    }


    /**
     * Calculate the size of all layers. This calculates the product of all the dimensions maximum value in a layer.
     * The results of this allow for calculating the needed length of a single-dimension to rpresent the whole
     * multi-dimensional space.
     * <p>
     * Think of the dimensions less as physical dimensions, and instead as the dimensions of a square or a cube, since
     * the layer-space is finite.
     *
     * @param layers The dimensions of each layer.
     * @return The maximum size of each layer given the dimensions for it.
     */
    public static int[] calculateLayerSizes(int[][] layers) {
        int[] layerSizes = new int[layers.length];
        for(int i = 0; i < layers.length; ++i) {
            //find product of all dimensions
            int size = 1;

            if(layers[i].length < 1)
                throw new InvalidParameterException("A layer must have at least 1 dimension");

            for(int d : layers[i]) {
                if(d <= 0) throw new InvalidParameterException("Invalid dimension size");
                size *= d;
            }
            layerSizes[i] = size;
        }

        return layerSizes;
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
        if(inputs.length != layerSizes[0])
            throw new InvalidParameterException("Invalid number of inputs.");

//        if(inputFunction != ActivationFunction.LINEAR)
//            //would do same thing without if statement, except it would take longer
//            for(int i = 0; i < inputs.length; ++i)
//                inputs[i] = inputFunction.calculate(inputs[i]);

        float[] last = inputs;
        float[] outputs = null;

        //for each layer, calculate the value of the next one given the inputs and weights of the inputs
        // layer is the current input; don't run last layer, it is output
        for(int layer = 0; layer < (layers.length - 1); ++layer) {
            outputs = new float[layerSizes[layer + 1]];

            //for all the outputs, calculate the value based on all inputs and associated weights
            for(int j = 0; j < layerSizes[layer + 1]; ++j) {
                float sum = 0;
                //beginning of the weights for this specific output.
                final int weightOffset = j * layerSizes[layer];

                //dot(input, weights[layer] + weightOffset)
                for(int i = 0; i < layerSizes[layer]; ++i)
                    sum += last[i] * weights[layer][weightOffset + i];

                outputs[j] = (layer == layers.length - 2) ?
                                     outputFunction.calculate(sum) :
                                     hiddenFunction.calculate(sum);
            }

            last = outputs;
        }

        return outputs;
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
        return calculate(inputs); //for now just call calculate since we don't store values within the network
    }


    /**
     * Empty the network of all stored values. This should be called between tests.
     */
    @Override
    public void flush() {

    }


    /**
     * Determines if the neural network has recurrent cycles in it.
     *
     * @return True if it is a recurrent neural network.
     */
    @Override
    public boolean isRecurrent() {
        return false;
    }
}
