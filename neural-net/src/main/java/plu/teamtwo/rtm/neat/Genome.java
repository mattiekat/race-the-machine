package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.io.Serializable;

public interface Genome extends Serializable {
    public abstract Genome duplicate();

    public abstract void mutate(GenomeMutations mutations);

    public abstract Genome cross(Genome other);

    /**
     * Compute the compatibility distance function δ. The value represents how different this genome is from the other
     * one by counting the disjoint and excess edges, and the the average difference in the weights.
     * @param other The genome to compare this one against.
     * @return The compatibility distance.
     */
    public abstract float compatibilityDistance(Genome other);

    public abstract NeuralNetwork getANN();


    public static Genome cross(Genome p1, Genome p2) {
        return p1.cross(p2);
    }
}
