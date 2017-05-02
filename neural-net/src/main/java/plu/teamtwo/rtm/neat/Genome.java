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
     * Used for initial members of the first generation to create random connections between the inputs and outputs.
     * This should not be needed after the first generation.
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


    /**
     * Make random alterations to the genome (i.e. mutations).
     *
     * @param cache Cached information about the genome.
     */
    abstract void mutate(GenomeCache cache);


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param cache Cached information about the genome.
     * @param other The other parent.
     * @return A child which is the result of crossing the genomes
     */
    abstract Genome cross(GenomeCache cache, Genome other);

    /**
     * Compute the compatibility distance function δ. The value represents how different the two genomes are.
     *
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    abstract float compatibilityDistance(Genome other);


    /**
     * Create a runnable ANN which is represented by the genome.
     *
     * @return The ANN represented by the genome.
     */
    abstract NeuralNetwork getANN();


    /**
     * Cross the genomes of two parents to create a child.
     *
     * @param cache Cached information about the genome.
     * @param p1    First parent, the most fit of the two.
     * @param p2    Second parent, the less fit of the two.
     * @return A child which is the result of crossing the genomes
     */
    static Genome cross(GenomeCache cache, Genome p1, Genome p2) {
        return p1.cross(cache, p2);
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different the two genomes are.
     *
     * @param a First genome.
     * @param b Second genome.
     * @return The compatibility distance.
     */
    static float compatibilityDistance(Genome a, Genome b) {
        return a.compatibilityDistance(b);
    }
}
