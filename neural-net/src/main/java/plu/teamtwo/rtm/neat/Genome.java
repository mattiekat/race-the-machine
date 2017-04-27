package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.io.Serializable;

public interface Genome extends Serializable {
    public abstract Genome duplicate();

    public abstract void mutate(GenomeMutations mutations);

    public abstract Genome cross(Genome other);

    public abstract NeuralNetwork getANN();


    public static Genome cross(Genome p1, Genome p2) {
        return p1.cross(p2);
    }
}
