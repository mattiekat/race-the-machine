package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

abstract class Genome {
    private float fitness = 0;
    private float adjFitness = 0;


    /**
     * Set the fitness value for this individual.
     *
     * @param fitness A measure of how well this individual performed.
     */
    void setFitness(float fitness) {
        this.fitness = fitness;
        this.adjFitness = 0;
    }


    /**
     * Get the fitness value for this individual.
     *
     * @return This individual's fitness value or 0 if unset.
     */
    float getFitness() {
        return fitness;
    }


    /**
     * Sets the adjusted fitness.
     *
     * @param n Number of individuals in the species.
     */
    void adjustFitness(int n) {
        adjFitness = fitness / (float) n;
    }


    /**
     * Get the adjusted fitness of this individual.
     *
     * @return This individual's fitness value adjusted for the species, or 0 if unset.
     */
    float getAdjFitness() {
        return adjFitness;
    }


    /**
     * Used for initial members of the first generation to create random edges between the input nodes
     * and the output nodes. This should not be needed after the first generation.
     *
     * @param cache Cached information about the Genome.
     */
    abstract void initialize(GenomeCache cache);

    /**
     * Create a deep copy of the genome. This will enable the copy to be modified without altering the original.
     *
     * @return A duplicate of the current instance.
     */
    abstract Genome duplicate();

    /**
     * Used to create a new cache of the appropriate type.
     *
     * @return A new cache for the specific instance of Genome.
     */
    abstract GenomeCache createCache();

    abstract void mutate(GenomeCache cache);

    abstract Genome cross(GenomeCache cache, Genome other);

    /**
     * Compute the compatibility distance function δ. The value represents how different this genome is from the other
     * one by counting the disjoint and excess edges, and the the average difference in the weights.
     *
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    abstract float compatibilityDistance(Genome other);

    abstract NeuralNetwork getANN();


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param cache Cached information about the genome.
     * @param p1    First parent, the most fit of the two.
     * @param p2    Second parent, the less fit of the two.
     * @return A child which is the result of crossing the genomes
     */
    static Genome cross(GenomeCache cache, Genome p1, Genome p2) {
        return p1.cross(cache, p2);
    }


    static float compatibilityDistance(Genome a, Genome b) {
        return a.compatibilityDistance(b);
    }
}
