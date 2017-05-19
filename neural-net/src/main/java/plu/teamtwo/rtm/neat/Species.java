package plu.teamtwo.rtm.neat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static plu.teamtwo.rtm.core.util.Rand.getRandomNum;

class Species implements Iterable<Individual> {
    /// Threshold used in compatibility distance to determine if two individuals are in the same species (δt).
    private static final float COMPATIBILITY_THRESHOLD = 3.5f;

    final int speciesID;
    final int parentSpeciesID;
    final int appeared;

    /// Average fitness of this species
    private float fitness;

    /// Highest avg. fitness this species has ever had.
    private float peakFitness;
    /// Last time the peakFitness was improved upon.
    private int lastImprovement;

    private Individual representative;
    private List<Individual> memebers = new ArrayList<>();
    private transient boolean sorted;


    /**
     * Create and initialize a new species. This is the primary constructor for creating a new species which did not
     * exist before.
     *
     * @param speciesID       ID of the new species (should be unique)
     * @param parentSpeciesID ID of the parent species (or -1 if no parent)
     * @param curGen          The starting generation for this species.
     * @param representative  First member of the species which will represent the genetic traits.
     */
    Species(int speciesID, int parentSpeciesID, int curGen, Individual representative) {
        this(speciesID, parentSpeciesID, curGen);
        this.representative = representative;
        memebers.add(representative);
    }


    /**
     * Creates an empty Species which has only the core information.
     *
     * @param speciesID       An identifier for this species.
     * @param parentSpeciesID Identifier for the species which this one resulted from.
     * @param appeared        When this species appeared.
     */
    private Species(int speciesID, int parentSpeciesID, int appeared) {
        this.speciesID = speciesID;
        this.parentSpeciesID = parentSpeciesID;
        this.appeared = appeared;
    }


    /**
     * Create an empty duplicate of this species which can then have new things added to it. It will set the
     * representative in the created instance to a random genome in the current instance (but will not add any genomes
     * to its member list).
     *
     * @return A shell of the species which has the core information without any of the
     */
    Species emptyDuplicate() {
        Species s = new Species(speciesID, parentSpeciesID, appeared);

        s.fitness = fitness;
        s.peakFitness = peakFitness;
        s.lastImprovement = lastImprovement;

        //set rep to random one in this list of members
        if(memebers.size() > 0)
            s.representative = memebers.get(getRandomNum(0, memebers.size() - 1));
        else //just in case this is used multiple times
            s.representative = representative;

        return s;
    }


    /**
     * Get the representative of this species. The representative is arbitrarily chosen, and will be the same across
     * multiple calls through the same generation.
     *
     * @return Representative of this species.
     */
    Individual getRep() {
        return representative;
    }


    /**
     * Get the average fitness of the current members in this species.
     *
     * @return The average fitness of the current members in this species.
     */
    float getFitness() {
        return fitness;
    }


    /**
     * Find the peak average adjusted fitness this species has had.
     *
     * @return Highest average adjusted fitness this species has ever had.
     */
    float getPeakFitness() {
        return peakFitness;
    }


    /**
     * Find when this species last saw an average increase in adjusted fitness.
     *
     * @return Generation number of the last time peakFitness was improved upon.
     */
    int getLastImprovement() {
        return lastImprovement;
    }


    /**
     * Finds and returns the most fit member of this species.
     *
     * @return Most fit member of this species.
     */
    Individual getChampion() {
        return getNthMostFit(0);
    }


    /**
     * Finds and returns the nth most fit member of this species.
     *
     * @param n Rank of the member we want between 0 (most fit) and size - 1 (least fit).
     * @return nth most fit member of this species.
     */
    Individual getNthMostFit(int n) {
        if(memebers.size() <= n) return null;
        sortByFitness();
        return memebers.get(n);
    }


    /**
     * Finds and removes the nth most fit member of this species.
     *
     * @param n Rank of the member we want between 0 (most fit) and size - 1 (least fit).
     * @return Former nth most fit member of this species.
     */
    Individual removeNthMostFit(int n) {
        Individual g = getNthMostFit(n);
        if(g == null) return null;
        if(!memebers.remove(g)) return null;
        return g;
    }


    /**
     * Drop values from the end of the species, including the value at the index passed in.
     *
     * @param i Index of the first value to drop.
     */
    void dropEnd(int i) {
        while(memebers.size() > i)
            memebers.remove(memebers.size() - 1);
    }


    /**
     * Checks if the compatibility distance between an individual and the representative of the species is within the
     * threshold.
     *
     * @param other Individual to check the compatibility of.
     * @return True of the individuals are compatible, false otherwise.
     */
    boolean isCompatible(Individual other) {
        if(getRep() == null) return true;
        return getRep().compatibilityDistance(other) < COMPATIBILITY_THRESHOLD;
    }


    int size() {
        return memebers.size();
    }


    /**
     * Adds a new individual to the species. This will return false if it is not able to be added. False will be
     * returned if the individual is not compatible with this one.
     *
     * @param other Individual to be added.
     * @return True if it was added successfully, false otherwise.
     */
    boolean add(Individual other) {
        if(!isCompatible(other)) return false;
        sorted = false;
        return memebers.add(other);
    }


    /**
     * Calculate the average fitness and adjusted fitness values for this species.
     */
    void calculateFitness(int curGen) {
        sorted = false;
        final int size = memebers.size();
        fitness = 0;
        for(Individual i : memebers)
            fitness += i.getFitness();

        fitness /= (float) memebers.size();

        if(fitness > peakFitness) {
            peakFitness = fitness;
            lastImprovement = curGen;
        }
    }


    /**
     * Get an iterator which will go over the genomes in this species.
     *
     * @return An iterator over the genomes in this species.
     */
    @Override
    public Iterator<Individual> iterator() {
        return memebers.iterator();
    }


    /**
     * Sort the species by fitness in descending order, such that the most fit members are at the front of the list.
     */
    void sortByFitness() {
        if(sorted) return;
        //sort in descending order
        memebers.sort((Individual a, Individual b) -> new Float(b.getFitness()).compareTo(a.getFitness()));
        sorted = true;
    }
}
