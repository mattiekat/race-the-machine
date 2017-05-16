package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.genome.Genome;
import plu.teamtwo.rtm.genome.GenomeCache;
import plu.teamtwo.rtm.neural.NeuralNetwork;
import plu.teamtwo.rtm.neural.SubstrateNetwork;


/**
 * This is the encoding used by HyperNEAT-LEO. Internally it uses a CPPN to encode the transition weights in the
 * resulting SubstrateNetwork.
 */
public class MultilayerSubstrateEncoding implements Genome {
    private int[][] layers;
    private int[] layerSizes;
    private GraphEncoding cppn;


    /**
     * Construct a new MultilayerSubstrateEncoding using a builder and a cache. This will construct the necessary CPPN
     * internally to calculate the weight values.
     *
     * @param builder Builder with the needed information to construct an instance.
     * @param cache Information about the nodes and edges needed by the CPPN for initialization.
     */
    MultilayerSubstrateEncoding(MultilayerSubstrateEncodingBuilder builder, GenomeCache cache) {
        layers = builder.buildLayers();
        layerSizes = SubstrateNetwork.calculateLayerSizes(layers);

        //construct the CPPN with outputs for each layer transition and enough inputs to support the largest
        // transition's dimensional space
        int maxTransDimen = 1;
        for(int i = 0; i < layers.length - 1; ++i)
            maxTransDimen = Math.max(layers[0].length + layers[1].length, maxTransDimen);
        //TODO: manually specify initial connections of the CPPN
        cppn = (GraphEncoding)new GraphEncodingBuilder()
                .inputs(maxTransDimen)
                .outputs((layers.length - 1) * 2) //multiply by two for LEO extension
                .create(cache);
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

    }


    /**
     * Create a deep copy of the genome. This will enable the copy to be modified without altering the original.
     *
     * @return A duplicate of the current instance.
     */
    @Override
    public Genome duplicate() {
        return null;
    }


    /**
     * Used to create a new cache of the appropriate type.
     *
     * @return A new cache for the specific instance of Genome.
     */
    @Override
    public GenomeCache createCache() {
        return null;
    }


    /**
     * Make random alterations to the genome (i.e. mutations).
     *
     * @param cache Cached information about the genome.
     */
    @Override
    public void mutate(GenomeCache cache) {

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
        return null;
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different the two genomes are.
     *
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    @Override
    public float compatibilityDistance(Genome other) {
        return 0;
    }


    /**
     * Create a runnable ANN which is represented by the genome.
     *
     * @return The ANN represented by the genome.
     */
    @Override
    public NeuralNetwork constructNeuralNetwork() {
        return null;
    }
}
