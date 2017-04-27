package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.io.Serializable;

public interface Genome extends Serializable {
    Genome duplicate();

    void mutate(GenomeMutations mutations);

    Genome cross(Genome other);

    /**
     * Compute the compatibility distance function δ. The value represents how different this genome is from the other
     * one by counting the disjoint and excess edges, and the the average difference in the weights.
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    float compatibilityDistance(Genome other);

    NeuralNetwork getANN();

    static Genome cross(Genome p1, Genome p2) {
        return p1.cross(p2);
    }
    static float compatibilityDistance(Genome a, Genome b) { return a.compatibilityDistance(b); }
}
