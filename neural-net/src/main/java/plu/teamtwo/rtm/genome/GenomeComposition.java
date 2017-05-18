package plu.teamtwo.rtm.genome;

import plu.teamtwo.rtm.core.util.Pair;
import plu.teamtwo.rtm.neural.NeuralNetworkComposition;

public class GenomeComposition extends Pair<Genome, Genome> implements Genome {
    public GenomeComposition() {
        super();
    }


    public GenomeComposition(Genome a, Genome b) {
        super(a, b);
    }


    /**
     * Create a deep copy of the genome. This will enable the copy to be modified without altering the original.
     *
     * @return A duplicate of the current instance.
     */
    @Override
    public Genome duplicate() {
        return new GenomeComposition(a.duplicate(), b.duplicate());
    }


    /**
     * Used to create a new cache of the appropriate type.
     *
     * @return A new cache for the specific instance of Genome.
     */
    @Override
    public GenomeCache createCache() {
        return new CacheComposition(a.createCache(), b.createCache());
    }


    /**
     * Make random alterations to the genome (i.e. mutations).
     *
     * @param gCache Cached information about the genome.
     */
    @Override
    public void mutate(GenomeCache gCache) {
        CacheComposition cache = (CacheComposition) gCache;
        a.mutate(cache.a);
        b.mutate(cache.b);
    }


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param gCache  Cached information about the genome.
     * @param p1f     First parent fitness.
     * @param p2      Second parent.
     * @param p2f     Second parent fitness.
     * @param average True if matching values should be averaged instead of randomly chosen.
     * @return A child which is the result of crossing the genomes
     */
    @Override
    public GenomeComposition crossMultipoint(GenomeCache gCache, float p1f, Genome p2, float p2f, boolean average) {
        CacheComposition cache = (CacheComposition) gCache;
        GenomeComposition other = (GenomeComposition) p2;
        return new GenomeComposition(
                a.crossMultipoint(cache.a, p1f, other.a, p2f, average),
                b.crossMultipoint(cache.b, p1f, other.b, p2f, average)
        );
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different the two genomes are.
     *
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    @Override
    public float compatibilityDistance(Genome other) {
        GenomeComposition o = (GenomeComposition) other;
        return a.compatibilityDistance(o.a) + b.compatibilityDistance(o.b);
    }


    /**
     * Create a runnable ANN which is represented by the genome.
     *
     * @return The ANN represented by the genome.
     */
    @Override
    public NeuralNetworkComposition constructNeuralNetwork() {
        return new NeuralNetworkComposition(
                a.constructNeuralNetwork(),
                b.constructNeuralNetwork()
        );
    }
}
