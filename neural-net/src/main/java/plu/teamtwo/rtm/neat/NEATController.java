package plu.teamtwo.rtm.neat;

import java.security.InvalidParameterException;
import java.util.LinkedList;

/**
 * The over-arching controller for the NEAT algorithm.
 */
public class NEATController {
    /// Size of the total population.
    private static final int POPULATION_SIZE = 150;
    /// Number of generations a new generation can show no improvement for.
    private static final int NEW_SPECIES_SAFE_PERIOD = 15;
    /// Number of individuals required in a species to keep the leader unchanged from one generation to the next.
    private static final int SPECIES_SIZE_TO_PROTECT_LEADER = 5;
    /// Chance for two individuals from different species to be mated.
    private static final float INTERSPECIES_MATING_RATE = 0.001f;
    /// Threshold used in compatibility distance to determine if two individuals are in the same species (δt).
    private static final float COMPATIBILITY_THRESHOLD = 3.0f;
    /// Percent of children in the next generation which are produced by crossover.
    private static final float CROSSOVER_RATE = 0.75f;

    public final Encoding encoding;
    private final int inputs;
    private final int outputs;
    private GenomeCache cache;

    private LinkedList<Species> generation = new LinkedList<>();


    //TODO: take parameter settings
    NEATController(Encoding encoding, int inputs, int outputs) {
        if(inputs <= 0 || outputs <= 0)
            throw new InvalidParameterException("The number of inputs and outputs must be greater than 0.");

        this.encoding = encoding;
        this.inputs = inputs;
        this.outputs = outputs;
        this.cache = null;
    }


    //TODO: create a function which takes a lambda to score a generation
    //TODO: create a way to get an ANN for each one and run it for a score?
    //TODO: support multithreaded scoring? Enable/disable by boolean...
    void initializeGeneration() {
        LinkedList<Genome> genomes = new LinkedList<>();
        Genome base = null;

        switch(encoding) {
            case DIRECT_ENCODING:
                cache = new DirectEncodingCache();
                base = new DirectEncoding(cache, inputs, outputs);
                break;
        }

        for(int x = 0; x < POPULATION_SIZE; ++x) {
            Genome g = base.duplicate();
            g.initialize(cache);
        }
    }
}