package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The over-arching controller for the NEAT algorithm. Note that this is not designed to be called from multiple
 * threads and may break up tasks internally.
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

    private List<Species> generation = new LinkedList<>();


    //TODO: take parameter settings
    NEATController(Encoding encoding, int inputs, int outputs) {
        if(inputs <= 0 || outputs <= 0)
            throw new InvalidParameterException("The number of inputs and outputs must be greater than 0.");

        this.encoding = encoding;
        this.inputs = inputs;
        this.outputs = outputs;
        this.cache = null;
    }


    //TODO: create a way to get an ANN for each one and run it for a score?
    //TODO: support multithreaded scoring? Enable/disable by boolean...


    /**
     * Initialize the system by creating the first generation.
     */
    void createFirstGeneration() {
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
            addGenome(g);
        }
    }


    /**
     * Asses the fitness of all the members of the current generation.
     * @param scoringFunction Method by which to asses how well the individuals perform.
     */
    public void assesGeneration(ScoringFunction scoringFunction) {
        //Construct a new thread pool
        final int MAX_THREADS = scoringFunction.maxThreads();
        ExecutorService threadPool = Executors.newFixedThreadPool(
            Math.min(
                MAX_THREADS <= 0 ? 10000 : MAX_THREADS,
                Runtime.getRuntime().availableProcessors() * 2
            )
        );

        //submit tasks to be run
        int id = 0;
        for(Species s : generation) for(Genome g : s)
            threadPool.submit(new GenomeProcessor(id++, g, scoringFunction));

        //wait for all tasks to finish running
        threadPool.shutdown();
        try {
            while(!threadPool.awaitTermination(1, TimeUnit.MINUTES));
        } catch(InterruptedException e) {
            threadPool.shutdownNow();
        };
    }


    public void nextGeneration() {
        //TODO: this
    }


    /**
     * Add a genome to the species it belongs to (will find out which one that is), or create a new species if it is not
     * compatible with any of the existing ones.
     * @param genome The genome to add.
     */
    private void addGenome(Genome genome) {
        for(Species s : generation) {
            if(s.compatibilityDistance(genome) < COMPATIBILITY_THRESHOLD) {
                s.add(genome);
                return;
            }
        }

        Species species = new Species();
        species.add(genome);
        generation.add(species);
    }



    /**
     * A runnable task which will compute the fitness of a Genome using a ScoringFunction.
     */
    private static class GenomeProcessor implements Runnable {
        private final int id;
        private final Genome genome;
        private final ScoringFunction scoringFunction;


        GenomeProcessor(int id, Genome genome, ScoringFunction scoringFunction) {
            this.id = id;
            this.genome = genome;
            this.scoringFunction = scoringFunction;
        }


        @Override
        public void run() {
            NeuralNetwork network = genome.getANN();

            float[] input;
            while((input = scoringFunction.generateInput(id)) != null) {
                float[] output = network.calculate(input, false);
                scoringFunction.acceptOutput(id, output);
            }

            genome.setFitness(scoringFunction.getScore(id));
        }
    }
}