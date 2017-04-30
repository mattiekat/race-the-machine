package plu.teamtwo.rtm.neat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class Species implements Iterable<Genome> {
    final int speciesID;
    final int parentSpeciesID;
    final int appeared;

    /// Average adjusted fitness of this species
    private float fitness;
    private float adjFitness;

    /// Highest avg. adjusted fitness this species has ever had.
    private float peakFitness;
    /// Last time the peakFitness was improved upon.
    private int lastImprovement;

    private List<Genome> memebers = new ArrayList<>();


    Species(int speciesID, int parentSpeciesID, int curGen, Genome representative) {
        this.speciesID = speciesID;
        this.parentSpeciesID = parentSpeciesID;
        this.appeared = curGen;
        memebers.add(representative);
    }


    /**
     * This will breed the current generation to create the next gen. It is possible that in the process the species
     * will be split into two or more resulting species.
     *
     * @param nextGen   Integer representing the new generation's number.
     * @param allowance Number of offspring this species should produce for the next generation.
     * @return A list of the resulting species after breeding. Will be empty if the allowance is 0.
     */
    List<Species> breed(int nextGen, int allowance) {
        List<Species> species = new LinkedList<>();
        species.add(this);
        return species;
    }


    /**
     * Get the representative of this species. The representative is arbitrarily chosen, and will be the same across
     * multiple calls through the same generation.
     * @return Representative of this species.
     */
    Genome getRep() {
        return memebers.get(0);
    }

    float getFitness() { return fitness; }

    float getAdjFitness() { return adjFitness; }

    float getPeakFitness() { return peakFitness; }

    int getLastImprovement() { return lastImprovement; }


    /**
     * Calculate the compatibility distance between a genome and the representative of the species.
     * @param genome Genome to check the compatibility of.
     * @return A real number representing how different the genome is from this species.
     */
    float compatibilityDistance(Genome genome) {
        return getRep().compatibilityDistance(genome);
    }


    int size() {
        return memebers.size();
    }


    /**
     * Adds a new genome to the species. Make sure in advance that it satisfies any compatibility distance thresholds.
     * @param genome Genome to be added.
     */
    void add(Genome genome) {
        memebers.add(genome);
    }


    /**
     * Adjust the fitness values for all members of this species. This will effectively do nothing if the fitness values
     * are not already set for each member of the species.
     */
    void adjustFitnessValues(int curGen) {
        final int size = memebers.size();
        fitness = adjFitness = 0;
        for(Genome i : memebers) {
            i.adjustFitness(size);
            fitness += i.getFitness();
            adjFitness += i.getAdjFitness();
        }
        fitness /= (float)memebers.size();
        adjFitness /= (float)memebers.size();

        if(adjFitness > peakFitness) {
            peakFitness = adjFitness;
            lastImprovement = curGen;
        }
    }


    @Override
    public Iterator<Genome> iterator() {
        return memebers.iterator();
    }


    void sortByFitness() {
        memebers.sort((Genome a, Genome b) -> Math.round(a.getFitness() - b.getFitness()));
    }
}
