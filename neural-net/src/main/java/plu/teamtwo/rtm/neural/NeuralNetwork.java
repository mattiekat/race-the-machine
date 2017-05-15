package plu.teamtwo.rtm.neural;


import plu.teamtwo.rtm.core.util.Pair;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * This is an ANN which can be run on inputs and then will provide outputs based on that.
 * ANN's are not to be modified once made, if a new one is desired with different structure,
 * it will need to be re-built. This is designed to work in tandem with Genome.
 * <p>
 * To use a NeuralNetwork, first construct it with the correct information about the number of different node types.
 * Next call setNeuron for all neurons, keep in mind that [0, endInput) will be the inputs,
 * [endInput, endOutput) will be the output nodes, and [endOutput, neurons.length) will be the hidden nodes.
 * Finally call validate() which will finalize the structure, enabling it to be calculated.
 * <p>
 * TODO: Make sure output nodes are calculated last or change how we calculate recurrence
 */
public class NeuralNetwork {
    private static final ActivationFunction DEFAULT_ACTIVATION_FUNCTION = ActivationFunction.SIGMOID;
    private static final int DEFAULT_MAX_RECURRENT_CYCLES = 20;
    private static final float DEFAULT_MAX_DIFFERENCE_BETWEEN_OUTPUTS = 1e-4f;

    /// The neurons in the ANN. The neurons are stored in this order: input, output, hidden.
    private final Neuron[] neurons;
    /// The end of the input nodes in the ANN (i.e. input nodes are [0, endInput) ).
    private final int endInput;
    /// The end of the output nodes in the ANN (i.e. output nodes are [endInput, endOutput) ).
    private final int endOutput;

    private NeuralNetwork(Builder builder) {
        if(builder.in < 1 || builder.out < 1 || builder.hidden < 0)
            throw new InvalidParameterException("Invalid number of nodes to form an ANN.");

        endInput = builder.in;
        endOutput = builder.in + builder.out;

        neurons = new Neuron[builder.in + builder.out + builder.hidden];
        for(int i = 0; i < neurons.length; ++i)
            neurons[i] = new Neuron(DEFAULT_ACTIVATION_FUNCTION);

        for(Pair<Integer, Integer> k : builder.connections.keySet())
            connect(k.a, k.b, builder.connections.get(k));

        for(int f : builder.activation.keySet())
            setFunction(f, builder.activation.get(f));

        //construct the backreferences
        for(int i = 0; i < neurons.length; ++i) {
            final Neuron n = neurons[i];
            for(Dendrite d : n.outputs)
                //create a new input from the node it goes to pointing back to this one
                neurons[d.connection].inputs.add(new Dendrite(i, d.weight));
        }
    }


    /**
     * Sets the activation function for a specific neuron, used in construction.
     *
     * @param id The neuron who's activation function is to be set.
     * @param fn The new activation function.
     */
    private void setFunction(int id, ActivationFunction fn) {
        try {
            neurons[id].function = fn;
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new InvalidParameterException("Invalid function specified.");
        }
    }


    /**
     * Adds a new connection, used in construction.
     *
     * @param from   Node who's value is sent down the connection.
     * @param to     Node who receives the value sent along the connection.
     * @param weight Weight of the connection.
     */
    private void connect(int from, int to, float weight) {
        //if any of the indices are invalid, do nothing
        if(from >= neurons.length || to >= neurons.length || from < 0 || to < 0)
            throw new InvalidParameterException("Invalid connection specified.");

        if(!neurons[from].outputs.add(new Dendrite(to, weight)))
            throw new InvalidParameterException("Duplicate connection specified.");
    }


    /**
     * Run through the neural network until the difference each of the inputs is within the acceptable range or the
     * maximum number of cycles has been run.
     *
     * @param inputs  Array of values to set the input neurons to.
     * @return Output of the network.
     */
    public float[] calculate(float... inputs) {
        return calculate(DEFAULT_MAX_RECURRENT_CYCLES, DEFAULT_MAX_DIFFERENCE_BETWEEN_OUTPUTS, inputs);
    }


    /**
     * Run through the neural network until the difference each of the inputs is within the acceptable range or the
     * maximum number of cycles has been run.
     *
     * @param inputs  Array of values to set the input neurons to.
     * @param maxDiff Maximum difference between runs before accepting the result of the network.
     * @return Output of the network.
     */
    public float[] calculate(int maxCycles, float maxDiff, float... inputs) {
        float[] output = run(inputs, false), last = null;

        //run until we hit max cycles or the value has stabilized
        for(int cycle = 0; cycle < maxCycles; ++cycle) {
            last = output;
            output = run(inputs, false);

            //check if any exceed max difference and end the loop if so
            boolean within = true;
            for(int i = 0; i < output.length; ++i) {
                if(Math.abs(last[i] - output[i]) > maxDiff) {
                    within = false;
                    break;
                }
            }
            if(within) break;
        }
        return output;
    }


    /**
     * Steps values through the neural network by processing from the final nodes to the initial nodes. This could be
     * used with real-time applications where direct input-output pairing are not so important as temporal
     * comprehension.
     *
     * @param inputs Array of values to set the input neurons to.
     * @return Array of values from the output neurons.
     */
    public float[] step(float... inputs) {
        return run(inputs, true);
    }


    /**
     * Runs the neural network on a set of inputs and provides the resulting outputs. This will do a full run through
     * the network taking the inputs values all the way to the output neurons.
     *
     * @param inputs Array of values to set the input neurons to.
     * @param step   Set this to true if you want values to more slowly propagate through the network. Normal behavior
     *               would be when stepping is disabled.
     * @return Array of values from the output neurons.
     */
    private float[] run(float[] inputs, boolean step) {
        if(inputs.length != endInput)
            throw new InvalidParameterException("Invalid number of inputs.");

        //Set the input values
        for(int i = 0; i < endInput; ++i)
            neurons[i].inputValue(inputs[i]);

        //create work queue and visited information
        Queue<Integer> queue = new LinkedList<>();
        BitSet visited = new BitSet(neurons.length);

        if(step) for(int i = endInput; i < endOutput; ++i)
            queue.add(i);
        else for(int i = 0; i < endInput; ++i)
            queue.add(i);

        while(!queue.isEmpty()) {
            //check if we have visited this node before
            final int current = queue.poll();
            if(visited.get(current)) continue;
            visited.set(current);

            //calculate output of current neuron
            final Neuron neuron = neurons[current];
            final float value = neuron.calculate();

            //input the value to all connected neurons and add them to the work queue
            for(Dendrite d : neuron.outputs)
                neurons[d.connection].inputValue(value * d.weight);

            //if we are stepping, then go through inputs, otherwise go though outputs and add to queue
            for(Dendrite d : (step ? neuron.inputs : neuron.outputs))
                if(!visited.get(d.connection))
                    queue.add(d.connection);
        }

        //read values at output neurons
        float[] outputs = new float[endOutput - endInput];
        for(int i = endInput, j = 0; i < endOutput; ++i, ++j)
            outputs[j] = neurons[i].getOutput();

        return outputs;
    }


    /**
     * Empty the network of all stored values. This should be called between tests.
     */
    public void flush() {
        for(Neuron n : neurons)
            n.flush();
    }


    /**
     * Used to create a Neural Network.
     */
    public static class Builder {
        private int in;
        private int out;
        private int hidden;
        private Map<Pair<Integer, Integer>, Float> connections;
        private Map<Integer, ActivationFunction> activation;


        /**
         * Construct a new NeuralNetwork Builder.
         */
        public Builder() {
            in = -1;
            out = -1;
            hidden = -1;
            connections = new HashMap<>();
            activation = new HashMap<>();
        }


        /**
         * Set the number of inputs the network should accept.
         *
         * @param in Number of inputs.
         */
        public Builder inputs(int in) {
            this.in = in;
            return this;
        }


        /**
         * Set the number of outputs the network should generate.
         *
         * @param out Number of outputs.
         */
        public Builder outputs(int out) {
            this.out = out;
            return this;
        }


        /**
         * Set the number of hidden nodes which should be generated.
         *
         * @param hidden Number of hidden nodes.
         */
        public Builder hidden(int hidden) {
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
        public Builder connect(int from, int to, float weight) {
            connections.put(new Pair<>(from, to), weight);
            return this;
        }


        /**
         * Set an activation function.
         *
         * @param node ID of the node for which the activation function should be changed.
         * @param fn   Function which should be used by the node.
         */
        public Builder setFunction(int node, ActivationFunction fn) {
            activation.put(node, fn);
            return this;
        }


        /**
         * Construct the network with the specified configuration.
         *
         * @return The new neural network.
         */
        public NeuralNetwork create() {
            return new NeuralNetwork(this);
        }
    }
}