package plu.teamtwo.rtm.genome;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.util.concurrent.Callable;

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
     * Create a deep copy of the genome. This will enable the copy to be modified without altering the original.
     *
     * @return A duplicate of the current instance.
     */
    Genome duplicate();


    /**
     * Used to create a new cache of the appropriate type.
     *
     * @return A new cache for the specific instance of Genome.
     */
    GenomeCache createCache();


    /**
     * Make random alterations to the genome (i.e. mutations).
     *
     * @param cache Cached information about the genome.
     */
    void mutate(GenomeCache cache);


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
    Genome crossMultipoint(GenomeCache cache, final float p1f, Genome p2, final float p2f, final boolean average);


    /**
     * Compute the compatibility distance function δ. The value represents how different the two genomes are.
     *
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    float compatibilityDistance(Genome other);


    /**
     * Create a runnable ANN which is represented by the genome.
     *
     * @return The ANN represented by the genome.
     */
    NeuralNetwork constructNeuralNetwork();


    /**
     * Construct a neural network given a Genome.
     */
    class ConstructNeuralNetwork implements Callable<NeuralNetwork> {
        private final Genome genome;

        public ConstructNeuralNetwork(Genome genome) {
            this.genome = genome;
        }


        @Override
        public NeuralNetwork call() throws Exception {
            return genome.constructNeuralNetwork();
        }
    }
}
