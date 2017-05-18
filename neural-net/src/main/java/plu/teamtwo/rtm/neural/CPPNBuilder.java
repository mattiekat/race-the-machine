package plu.teamtwo.rtm.neural;

import plu.teamtwo.rtm.core.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class CPPNBuilder implements NeuralNetworkBuilder {
    float maxDifferenceBetweenOutputs = 1e-4f;
    int in = -1, out = -1, hidden = -1, maxRecurrentCycles = 20;
    Map<Pair<Integer, Integer>, Float> connections = new HashMap<>();
    Map<Integer, ActivationFunction> activation = new HashMap<>(); //TODO: switch to having a list of nodes


    /**
     * Set the number of inputs the network should accept.
     *
     * @param in Number of inputs.
     */
    public CPPNBuilder inputs(int in) {
        this.in = in;
        return this;
    }


    /**
     * Set the number of outputs the network should generate.
     *
     * @param out Number of outputs.
     */
    public CPPNBuilder outputs(int out) {
        this.out = out;
        return this;
    }


    /**
     * Set the number of hidden nodes which should be generated.
     *
     * @param hidden Number of hidden nodes.
     */
    public CPPNBuilder hidden(int hidden) {
        this.hidden = hidden;
        return this;
    }


    /**
     * Create a connection between two nodes.
     *
     * @param from   Sending node.
     * @param to     Receiving node.
     * @param weight Multiplier of the connection.
     */
    public CPPNBuilder connect(int from, int to, float weight) {
        connections.put(new Pair<>(from, to), weight);
        return this;
    }


    /**
     * Set an activation function.
     *
     * @param node ID of the node for which the activation function should be changed.
     * @param fn   Function which should be used by the node.
     */
    public CPPNBuilder setFunction(int node, ActivationFunction fn) {
        activation.put(node, fn);
        return this;
    }


    /**
     * Set the maximum number of recurrent cycle calculations to allow during computation.
     *
     * @param maxRecurrentCycles Max number of recurrent cycles to compute.
     */
    public CPPNBuilder setMaxRecurrentCycles(int maxRecurrentCycles) {
        this.maxRecurrentCycles = maxRecurrentCycles;
        return this;
    }


    /**
     * Sets the maximum difference between outputs allowed before considering the results to have converged.
     *
     * @param maxDifferenceBetweenOutputs The maximum difference between allowed between outputs.
     */
    public CPPNBuilder setMaxDifferenceBetweenOutputs(float maxDifferenceBetweenOutputs) {
        this.maxDifferenceBetweenOutputs = maxDifferenceBetweenOutputs;
        return this;
    }


    /**
     * Construct the network with the specified configuration.
     *
     * @return The new neural network.
     */
    @Override
    public CPPN create() {
        return new CPPN(this);
    }
}
