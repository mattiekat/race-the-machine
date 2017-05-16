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


    /**
     * Construct a substrate network with n layers each capable of having their own number of dimensions.
     * @param layers  Top level array is of layers, deeper level array is of the dimensions for the layer.
     * @param weights Top level array defines the layers being connected, lower level array defines the weight for each
     *                input coordinate to output coordinate connection in the form (output, input). For example,
     *                (x2, y2, x1, y1).
     */
    public SubstrateNetwork(int[][] layers, float[][] weights) {
        this.layers = layers;
        this.weights = weights;

        if(layers.length < 2)
            throw new InvalidParameterException("Must at minimum have an input and output layer.");
        if(weights.length != layers.length - 1)
            throw new InvalidParameterException("Invalid number of weights for the layers.");

        layerSizes = new int[layers.length];
        //for all layers
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

        for(int i = 0; i < weights.length; ++i)
            if(weights[i].length != layerSizes[i] * layerSizes[i+1])
                throw new InvalidParameterException("Weights must have exactly 1 value for every input and output combination between layers.");
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

                outputs[j] = ActivationFunction.SIGMOID.calculate(sum);
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
