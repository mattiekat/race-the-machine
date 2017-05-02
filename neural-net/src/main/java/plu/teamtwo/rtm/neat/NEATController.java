package plu.teamtwo.rtm.neat;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import plu.teamtwo.rtm.neural.NeuralNetwork;
import static plu.teamtwo.rtm.core.util.Rand.*;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
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
    /// Number of generations a new species can show no improvement for.
    private static final int NEW_SPECIES_SAFE_PERIOD = 15;
    /// Number of generations a species can show no improvement before being removed.
    private static final int GENERATIONS_BEFORE_REMOVAL = 15;
    /// Minimum number of new members in the next generation of a species which has not been removed.
    private static final int MINIMUM_BREEDING_ALLOWANCE = 1;
    /// Chance for two individuals from different species to be mated.
    private static final float INTERSPECIES_MATING_RATE = 0.001f;
    /// Number of individuals required in a species to keep the leader unchanged from one generation to the next.
    private static final int SPECIES_SIZE_TO_PROTECT_LEADER = 5;
    /// Percent of children in the next generation which are produced by crossover.
    private static final float BREEDING_CROSSOVER_RATE = 0.75f;
    /// The percentage of the species to drop; uses floor function, so in small species less will be dropped.
    private static final float BREEDING_DROP_RATE = 0.20f;

    public final Encoding encoding;
    private final int inputs;
    private final int outputs;
    private GenomeCache cache;
    private int generationNum;
    private int nextSpeciesID;
    private float fitness;
    private float adjFitness;
    private transient String savePath;
    private transient boolean sorted;

    private List<Species> generation = new ArrayList<>();


    //TODO: take parameter settings
    public NEATController(Encoding encoding, int inputs, int outputs) {
        if(inputs <= 0 || outputs <= 0)
            throw new InvalidParameterException("The number of inputs and outputs must be greater than 0.");

        this.encoding = encoding;
        this.inputs = inputs;
        this.outputs = outputs;
        this.cache = null;
        this.generationNum = 0;
        this.nextSpeciesID = 0;
        this.savePath = null;
        this.sorted = false;
    }


    /**
     * Read a NEATController from a JSON stream. This will create a new NEATController with the information of the
     * most recent generation in the JSON stream.
     *
     * @param inputStream A stream of JSON representing a NEATController.
     * @return A NEATController initialized to the latest generation in the JSON archive.
     */
    public static NEATController readFromStream(InputStream inputStream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        Gson gson = new Gson();
        NEATController controller = gson.fromJson(reader, NEATController.class);
        reader.close();
        return controller;
    }


    /**
     * Write a NEATController to an output stream. This will save all the information about the current generation and
     * other information about the current controller state.
     *
     * @param outputStream A stream to output the JSON to.
     */
    public static void writeToStream(NEATController controller, OutputStream outputStream) throws IOException {
        controller.sortByFitness();

        Gson gson = new Gson();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        writer.setIndent("  ");
        gson.toJson(controller, NEATController.class, writer);
        writer.close();
    }


    /**
     * Setup autosave (or disable it). If enabled, the NEATController will save its current state to a file before
     * creating the next generation (preserving historical information). If this throws an exception, auto-saves will
     * be disabled until it is called again without errors.
     *
     * @param path Directory to save the data in.
     */
    public void setAutoSave(String path) {
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
    public void createFirstGeneration() {
        //TODO: switch to creating a fully connected input-output system as the paper describes?
        Genome base = null;
        sorted = false;

        switch(encoding) {
            case DIRECT_ENCODING:
                cache = new DirectEncodingCache();
                base = new DirectEncoding(cache, inputs, outputs);
                break;
        }

        for(int x = 0; x < POPULATION_SIZE; ++x) {
            Genome g = base.duplicate();
            g.initialize(cache);
            addGenome(generation, g, -1);
        }
    }


    /**
     * Asses the fitness of all the members of the current generation.
     *
     * @param scoringFunction Method by which to asses how well the individuals perform.
     */
    public void assesGeneration(ScoringFunction scoringFunction) {
        sorted = false;
        //Construct a new thread pool
        final int MAX_THREADS = scoringFunction.getMaxThreads();
        ExecutorService threadPool = Executors.newFixedThreadPool(
                Math.min(
                        MAX_THREADS <= 0 ? 10000 : MAX_THREADS,
                        Runtime.getRuntime().availableProcessors() * 2
                )
        );

        //submit tasks to be run
        for(Species s : generation)
            for(Genome g : s)
                threadPool.submit(new GenomeProcessor(g, scoringFunction.createNew()));

        //wait for all tasks to finish running
        threadPool.shutdown();
        try {
            while(!threadPool.awaitTermination(1, TimeUnit.MINUTES))
                /*Keep waiting*/ ;
        } catch(InterruptedException e) {
            threadPool.shutdownNow();
        }

        //Adjust the fitness values
        for(Species s : generation)
            s.adjustFitnessValues(generationNum);

        calculateFitness();
    }


    /**
     * Breed the next generation from the current one.
     */
    public void nextGeneration() {
        if(savePath != null)
            Archiver.saveToFile(this, savePath);
        sortByFitness();
        cache.newGeneration();

        //remove any non-improving species
        generation.removeIf(s ->
            //must be out of safe period
            (generationNum - s.appeared) >= NEW_SPECIES_SAFE_PERIOD &&
            //remove if it has not improved in a while
            (s.getLastImprovement() - generationNum) > GENERATIONS_BEFORE_REMOVAL
        );

        //make sure every species gets a members in the next generation
        int[] allowances = new int[generation.size()];
        Arrays.fill(allowances, MINIMUM_BREEDING_ALLOWANCE);
        //keep track of total breeding allowance given
        int bred = generation.size() * MINIMUM_BREEDING_ALLOWANCE;

        final float globalAdjFitnessSum = adjFitness * (float)POPULATION_SIZE;
        final int allowanceAfterGuarantee = Math.min(POPULATION_SIZE - bred, 0);

        if(allowanceAfterGuarantee == 0) {
            System.err.println("Warning: generation " + generationNum + " may exceed population size due to the " +
                    "number of species and minimum breeding allowance."
            );
        }

        //Determine an estimated allowance for each species
        // (SpeciesAvgAdjFitness * NumSpeciesMembers) / GlobalAvgAdjFitness = BreedingAllowance
        for(int i = 0; i < generation.size(); ++i) {
            final Species s = generation.get(i);
            allowances[i] = (int) ((s.getAdjFitness() * s.size() * (float)allowanceAfterGuarantee) / globalAdjFitnessSum);
            bred += allowances[i];
        }

        //since we floor the value, randomly assign any remaining
        //TODO: use function to favor higher-performing species?
        while(allowanceAfterGuarantee - bred > 0) {
            final int species = getRandomNum(0, allowances.length - 1);
            allowances[species]++;
            bred++;
        }

        //breed each of the species
        List<Species> nextGen = new ArrayList<>(generation.size());
        for(int i = 0; i < generation.size(); ++i) {
            final Species s = generation.get(i);
            nextGen.addAll(
                breed(s, generationNum, allowances[i])
            );
        }

        //update the generation (drops old one)
        sorted = false;
        generationNum++;
        generation = nextGen;
    }


    /**
     * Get the number of generations since creation.
     *
     * @return The current generation number.
     */
    public int getGenerationNum() {
        return generationNum;
    }


    /**
     * Get the average fitness of all individuals.
     *
     * @return The population fitness.
     */
    public float getFitness() {
        return fitness;
    }


    /**
     * Get the average adjusted fitness of all individuals.
     *
     * @return The adjusted population fitness.
     */
    public float getAdjFitness() {
        return adjFitness;
    }


    /**
     * Sort the species by their average individual fitness in descending order such that the most fit species is listed
     * at the head of the list.
     */
    private void sortByFitness() {
        if(sorted) return;
        //sort descending
        generation.sort( (Species a, Species b) -> Math.round(b.getAdjFitness() - a.getAdjFitness()) );
        for(Species s : generation)
            s.sortByFitness();
        sorted = true;
    }


    /**
     * Add a genome to the species it belongs to (will find out which one that is), or create a new species if it is not
     * compatible with any of the existing ones.
     *
     * @param species A list of species to attempt adding the genome to.
     * @param genome The genome to add.
     * @param parentSpeciesID ID of the parent species should this need to add a new species for the genome.
     */
    private void addGenome(List<Species> species, Genome genome, int parentSpeciesID) {
        for(Species s : species)
            if(s.add(genome)) return;

        species.add(new Species(nextSpeciesID++, parentSpeciesID, generationNum, genome));
    }


    /**
     * Update the global fitness values.
     */
    private void calculateFitness() {
        fitness = adjFitness = 0;
        //Adjust the fitness values
        for(Species s : generation) {
            fitness += s.getFitness() * (float) s.size();
            adjFitness += s.getAdjFitness() * (float) s.size();
        }
        fitness /= POPULATION_SIZE;
        adjFitness /= POPULATION_SIZE;
    }


    /**
     * This will breed the current generation to create the next gen. It is possible that in the process the species
     * will be split into two or more resulting species.
     *
     * @param nextGen   Integer representing the new generation's number.
     * @param offspring Number of offspring this species should produce for the next generation.
     * @return A list of the resulting species after breeding. Will be empty if the allowance is 0.
     */
    private List<Species> breed(Species species, int nextGen, int offspring) {
        List<Species> newSpeciesList = new LinkedList<>();
        if(offspring <= 0) //handle the odd case
            return newSpeciesList;

        //add an empty duplicate of the last generation to the list for the new generation to fall into
        newSpeciesList.add(species.emptyDuplicate());

        //will we protect the leader
        if(offspring >= SPECIES_SIZE_TO_PROTECT_LEADER) {
            addGenome(newSpeciesList, species.getChampion(), species.speciesID);
            offspring--;
        }

        //TODO: do we need to drop off the back? We can just use a greater weight in selecting next generation...
        { //drop the "bottom performers" using a probability distribution function which maps from [0,1] to [0, size - 1]
            int numToDrop = (int) (species.size() * BREEDING_DROP_RATE);
            if(species.size() - numToDrop <= 0) {
                System.err.println("BREEDING_DROP_RATE is too high.");
                numToDrop = species.size() - 2;
            }
            for(int i = 0; i < numToDrop; ++i)
                species.removeNthMostFit(
                        randomBackWeightedIndex(species.size(), 10.0f)
                );
        }

        //create the children
        while(offspring-- > 0) {
            Genome child = null;
            if(species.size() > 1 && iWill(BREEDING_CROSSOVER_RATE)) { //use crossover on two random individuals
                int i1 = randomFrontWeightedIndex(species.size(), 0.15f), i2 = 0;
                //select a i2 which is not the same as i1
                while((i2 = randomFrontWeightedIndex(species.size(), 0.15f)) == i1);

                Genome p1 = species.getNthMostFit(i1);
                Genome p2 = species.getNthMostFit(i2);
                child  = p1.cross(cache, p2);
            }
            else { //copy and mutate
                int i = randomFrontWeightedIndex(species.size(), 0.15f);
                child = species.getNthMostFit(i).duplicate();
            }
            child.mutate(cache);
            addGenome(newSpeciesList, child, species.speciesID);
        }

        newSpeciesList.removeIf(s -> s.size() <= 0);
        return newSpeciesList;
    }



    /**
     * A runnable task which will compute the fitness of a Genome using a ScoringFunction.
     */
    private static class GenomeProcessor implements Runnable {
        private final Genome genome;
        private final ScoringFunction scoringFunction;


        GenomeProcessor(Genome genome, ScoringFunction scoringFunction) {
            this.genome = genome;
            this.scoringFunction = scoringFunction;
        }


        @Override
        public void run() {
            NeuralNetwork network = genome.getANN();

            float[] input;
            while((input = scoringFunction.generateInput()) != null) {
                float[] output = network.calculate(input, false);
                scoringFunction.acceptOutput(output);
            }

            genome.setFitness(scoringFunction.getScore());
        }
    }
}