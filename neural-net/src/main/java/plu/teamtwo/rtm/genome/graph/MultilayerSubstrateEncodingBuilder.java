package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.genome.Genome;
import plu.teamtwo.rtm.genome.GenomeBuilder;
import plu.teamtwo.rtm.genome.GenomeCache;

import java.security.InvalidParameterException;
import java.util.LinkedList;

public class MultilayerSubstrateEncodingBuilder implements GenomeBuilder {
    int[] inputs;
    int[] outputs;
    LinkedList< int[] > hidden;


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
     * Used to construct the double array of layer information and validate it.
     *
     * @return Double array of layers and dimensions for that layer.
     */
    int[][] buildLayers() {
        int[][] layers = new int[2 + hidden.size()][];
        for(int i = 0; i < hidden.size(); ++i)
            layers[i] = hidden.get(i);

        return layers;
    }
}
