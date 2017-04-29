package plu.teamtwo.rtm.neat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Species implements Iterable<Genome> {
    final int speciesID;
    final int parentSpeciesID;

    private List<Genome> memebers = new ArrayList<>();


    Species(int speciesID, int parentSpeciesID, Genome representative) {
        this.speciesID = speciesID;
        this.parentSpeciesID = parentSpeciesID;
        memebers.add(representative);
    }

    /**
     * Get the representative of this species. The representative is arbitrarily chosen, and will be the same across
     * multiple calls through the same generation.
     * @return Representative of this species.
     */
    Genome getRep() {
        return memebers.get(0);
    }


    /**
     * Calculate the compatibility distance between a genome and the representative of the species.
     * @param genome Genome to check the compatibility of.
     * @return A real number representing how different the genome is from this species.
     */
    float compatibilityDistance(Genome genome) {
        return getRep().compatibilityDistance(genome);
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
    void adjustFitnessValues() {
        final int size = memebers.size();
        for(Genome i : memebers)
            i.adjustFitness(size);
    }


    @Override
    public Iterator<Genome> iterator() {
        return memebers.iterator();
    }
}
