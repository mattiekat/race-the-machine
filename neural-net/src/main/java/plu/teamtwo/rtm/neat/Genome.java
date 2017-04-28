package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public interface Genome extends Serializable {
    Genome duplicate();

    /**
     * Used to create a new cache of the appropriate type.
     * @return A new cache for the specific instance of Genome.
     */
    GenomeCache createCache();

    void mutate(GenomeCache cache);

    Genome cross(GenomeCache cache, Genome other);

    /**
     * Compute the compatibility distance function δ. The value represents how different this genome is from the other
     * one by counting the disjoint and excess edges, and the the average difference in the weights.
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    float compatibilityDistance(Genome other);

    NeuralNetwork getANN();

    void toJSON(OutputStream stream);

    static Genome fromJSON(InputStream stream) {
        return null;
    }

    static Genome cross(GenomeCache cache, Genome p1, Genome p2) {
        return p1.cross(cache, p2);
    }

    static float compatibilityDistance(Genome a, Genome b) {
        return a.compatibilityDistance(b);
    }
}
