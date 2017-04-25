package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.io.Serializable;

public interface Genome extends Serializable {

    public abstract NeuralNetwork getANN();

    public abstract void cross();

    public abstract void mutation();


}
