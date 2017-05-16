package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.core.util.Pair;
import plu.teamtwo.rtm.genome.Genome;
import plu.teamtwo.rtm.genome.GenomeBuilder;
import plu.teamtwo.rtm.genome.GenomeCache;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

public class GraphEncodingBuilder implements GenomeBuilder {
    int inputs = 0, outputs = 0;
    List<Pair<Integer, Integer>> initialConnections = null;
    //List<>


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
     */
    public GraphEncodingBuilder connect(int f, int t) {
        if(initialConnections == null) initialConnections = new LinkedList<>();
        initialConnections.add(new Pair<>(f, t));
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
