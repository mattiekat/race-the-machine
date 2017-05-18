package plu.teamtwo.rtm.neural;

public interface NeuralNetworkBuilder {
    /**
     * Create a new neural network given the internal settings.
     * @return The new neural network.
     */
    NeuralNetwork create();
}
