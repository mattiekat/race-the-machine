package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.genome.Genome;
import plu.teamtwo.rtm.genome.GenomeBuilder;
import plu.teamtwo.rtm.genome.GenomeCache;

public class GraphEncodingBuilder implements GenomeBuilder {
    int inputs = 0, outputs = 0;


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
