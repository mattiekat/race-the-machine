package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

public interface Genome {
    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param cache   Cached information about the genome.
     * @param p1      First parent, the most fit of the two.
     * @param p1f     First parent fitness.
     * @param p2      Second parent.
     * @param p2f     Second parent fitness.
     * @param average True if matching values should be averaged instead of randomly chosen.
     * @return A child which is the result of crossing the genomes
     */
    static Genome crossMultipoint(GenomeCache cache, Genome p1, final float p1f, Genome p2,
                                  final float p2f, final boolean average) {
        return p1.crossMultipoint(cache, p1f, p2, p2f, average);
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


    /**
     * Used for initial members of the first generation to create connections between the inputs and outputs. This
     * should not be needed after the first generation. It is recommended that mutate be called after this function to
     * give the initial species some variation.
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
     * @param cache   Cached information about the genome.
     * @param p1f     First parent fitness.
     * @param p2      Second parent.
     * @param p2f     Second parent fitness.
     * @param average True if matching values should be averaged instead of randomly chosen.
     * @return A child which is the result of crossing the genomes
     */
    public DirectEncoding crossMultipoint(GenomeCache cache, final float p1f, Genome p2,
                                          final float p2f, final boolean average);


    /**
     * Compute the compatibility distance function δ. The value represents how different the two genomes are.
     *
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    public abstract float compatibilityDistance(Genome other);


    /**
     * Create a runnable ANN which is represented by the genome.
     *
     * @return The ANN represented by the genome.
     */
    public abstract NeuralNetwork getANN();
}
