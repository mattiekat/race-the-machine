package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.io.*;
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
    private int generationNum;
    private int nextSpeciesID;
    private transient String savePath;

    private List<Species> generation = new LinkedList<>();


    //TODO: take parameter settings
    NEATController(Encoding encoding, int inputs, int outputs) {
        if(inputs <= 0 || outputs <= 0)
            throw new InvalidParameterException("The number of inputs and outputs must be greater than 0.");

        this.encoding = encoding;
        this.inputs = inputs;
        this.outputs = outputs;
        this.cache = null;
        this.generationNum = 0;
        this.nextSpeciesID = 0;
        this.savePath = null;
    }


    /**
     * Save a NEATController to a JSON archive. This should be used before creating the next generation to prevent a
     * loss of information as the generation is overwritten.
     *
     * @param controller Controller to save to a file.
     * @param path       Location to save the data to.
     * @return True if it was successfully saved, false otherwise.
     */
    static boolean saveToFile(NEATController controller, String path) {
        if(controller == null || path == null) return false;
        File dir = new File(path);
        if(!dir.exists() || !dir.isDirectory()) {
            System.err.println("The directory '" + path + "' does not exist or is not a directory.");
            return false;
        }
        File file = new File(String.format("%sG%5d.json", dir.getPath() + File.pathSeparator, controller.generationNum));
        try {
            writeToStream(controller, new FileOutputStream(file, false));
        } catch(IOException e) {
            System.err.println("Could not save NEAT Controller: " + e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * Read a NEATController from a JSON file. This will create a new NEATController with the information of the
     * most recent generation in the JSON file. This expects to receive a directory with the generations saved into it.
     * <p>
     * Note: auto save will need to be re-enabled if it is desired in the new instance.
     *
     * @param path Directory to read from.
     * @return A NEATController initialized to the latest generation in the JSON archive.
     */
    static NEATController readFromFile(String path) throws IOException {
        //verify we were given a directory.
        if(path == null) return null;
        File file = new File(path);
        if(!file.exists())
            throw new InvalidParameterException("Directory does not exist.");
        if(!file.isDirectory())
            throw new InvalidParameterException("Path is not to a directory.");
        File[] files = file.listFiles( //filter out non-files and files which are not a generation
                (File dir, String name) -> dir.isFile() && name.matches("G[0-9]{5}\\.json")
        );

        if(files == null || files.length <= 0)
            throw new FileNotFoundException("Could not find a valid generation file.");

        //find the file with the greatest number (it is most recent generation)
        file = files[0];
        for(int x = 1; x < files.length; ++x) {
            if(files[x].getName().compareTo(file.getName()) > 0)
                file = files[x];
        }
        return readFromStream(new FileInputStream(file));
    }


    /**
     * Read a NEATController from a JSON stream. This will create a new NEATController with the information of the
     * most recent generation in the JSON stream.
     *
     * @param inputStream A stream of JSON representing a NEATController.
     * @return A NEATController initialized to the latest generation in the JSON archive.
     */
    static NEATController readFromStream(InputStream inputStream) {
        return null;
    }


    /**
     * Write a NEATController to an output stream. This will save all the information about the current generation and
     * other information about the current controller state.
     *
     * @param outputStream A stream to output the JSON to.
     */
    static void writeToStream(NEATController controller, OutputStream outputStream) {

    }


    /**
     * Setup autosave (or disable it). If enabled, the NEATController will save its current state to a file before
     * creating the next generation (preserving historical information). If this throws an exception, auto-saves will
     * be disabled until it is called again without errors.
     *
     * @param path Directory to save the data in.
     */
    void setAutoSave(String path) {
        savePath = null;
        if(path == null) return;
        File file = new File(path);
        if(!file.exists())
            throw new InvalidParameterException("Directory does not exist.");
        if(!file.isDirectory())
            throw new InvalidParameterException("Path is not to a directory.");
        savePath = path;
    }


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
            initAddGenome(g);
        }
    }


    /**
     * Asses the fitness of all the members of the current generation.
     *
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
        for(Species s : generation)
            for(Genome g : s)
                threadPool.submit(new GenomeProcessor(id++, g, scoringFunction));

        //wait for all tasks to finish running
        threadPool.shutdown();
        try {
            while(!threadPool.awaitTermination(1, TimeUnit.MINUTES)) ;
        } catch(InterruptedException e) {
            threadPool.shutdownNow();
        }

        //Adjust the fitness values
        for(Species s : generation)
            s.adjustFitnessValues();
    }


    /**
     * Breed the next generation from the current one.
     */
    public void nextGeneration() {
        //TODO: this
    }


    /**
     * Add a genome to the species it belongs to (will find out which one that is), or create a new species if it is not
     * compatible with any of the existing ones.
     * <p>
     * This should only be used during initialization.
     *
     * @param genome The genome to add.
     */
    private void initAddGenome(Genome genome) {
        for(Species s : generation) {
            if(s.compatibilityDistance(genome) < COMPATIBILITY_THRESHOLD) {
                s.add(genome);
                return;
            }
        }

        generation.add(new Species(nextSpeciesID++, -1, genome));
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