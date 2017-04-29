package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

abstract class Genome {
    private float fitness = 0;


    void setFitness(float fitness) {
        this.fitness = fitness;
    }


    float getFitness() {
        return fitness;
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


    static Genome cross(GenomeCache cache, Genome p1, Genome p2) {
        return p1.cross(cache, p2);
    }


    static float compatibilityDistance(Genome a, Genome b) {
        return a.compatibilityDistance(b);
    }
}
