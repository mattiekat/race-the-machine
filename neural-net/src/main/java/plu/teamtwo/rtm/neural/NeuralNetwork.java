package plu.teamtwo.rtm.neural;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * This is an ANN which can be run on inputs and then will provide outputs based on that.
 * ANN's are not to be modified once made, if a new one is desired with different structure,
 * it will need to be re-built. This is designed to work in tandem with Genome.
 *
 * To use a NeuralNetwork, first construct it with the correct information about the number of different node types.
 * Next call setNeuron for all neurons, keep in mind that [0, inputNeurons) will be the inputs,
 * [inputNeurons, outputNeurons) will be the output nodes, and [outputNeurons, neurons.length) will be the hidden nodes.
 * Finally call validate() which will finalize the structure, enabling it to be calculated.
 *
 * TODO: Make sure output nodes are calculated last or change how we calculate recurrence
 */
public class NeuralNetwork {
    private static final ActivationFunction DEFAULT_ACTIVATION_FUNCTION = ActivationFunction.SIGMOID;
    private static final int DEFAULT_MAX_RECURRENT_CYCLES = 20;
    private static final float DEFAULT_MAX_DIFFERENCE_BETWEEN_OUTPUTS = 1e-4f;

    /// The neurons in the ANN. The neurons are stored in this order: input, output, hidden.
    private final Neuron[] neurons;
    /// The end of the input nodes in the ANN (i.e. input nodes are [0, inputNeurons) ).
    private final int inputNeurons;
    /// The end of the output nodes in the ANN (i.e. output nodes are [inputNeurons, outputNeurons) ).
    private final int outputNeurons;
    /// Variable used to finalize the state of the ANN
    private boolean validated;

    /**
     * Construct a new neural network.
     * @param numinputs Number of input nodes in the network.
     * @param numoutputs Number of output nodes in the network.
     * @param numhidden Number of hidden nodes in the network.
     */
    public NeuralNetwork(int numinputs, int numoutputs, int numhidden) {
        if(numinputs < 1 || numoutputs < 1 || numhidden < 0)
            throw new InvalidParameterException("Invalid number of nodes to form an ANN.");

        validated = false;

        inputNeurons = numinputs;
        outputNeurons = numinputs + numoutputs;

        neurons = new Neuron[numinputs + numoutputs + numhidden];
        for(int i = 0; i < neurons.length; ++i)
            neurons[i] = new Neuron(DEFAULT_ACTIVATION_FUNCTION);
    }


    /**
     * Sets the activation function for a specific neuron.
     * @param id The neuron who's activation function is to be set.
     * @param fn The new activation function.
     * @return True if the activation function was changed.
     */
    public boolean setFunction(int id, ActivationFunction fn) {
        if(validated)
            throw new IllegalStateException("Cannot modify Neurons once the ANN has been validated.");

        try {
            boolean changed = neurons[id].function != fn;
            neurons[id].function = fn;
            return changed;
        } catch(ArrayIndexOutOfBoundsException e) { return false; }
    }


    /**
     * Adds a new connection, if the connection already exists, no change will be made.
     * @param from Node who's value is sent down the connection.
     * @param to Node who receives the value sent along the connection.
     * @param weight Weight of the connection.
     * @return True if a connection was added.
     */
    public boolean connect(int from, int to, float weight) {
        if(validated)
            throw new IllegalStateException("Cannot modify Neurons once the ANN has been validated.");

        //if any of the indices are invalid, do nothing
        if(from >= neurons.length || to >= neurons.length || from < 0 || to < 0)
            return false;

        return neurons[from].outputs.add(new Dendrite(to, weight));
    }


    /**
     * Finalize the ANN state. This will enable running calculations if it succeeds. This will basically calculate other
     * index values or cache information which can then be used connection speed up the execution of the ANN.
     * @return True if it was successfully validated, false otherwise.
     */
    public boolean validate() {
        if(validated) return true;

        //construct the backreferences
        for(int i = 0; i < neurons.length; ++i) {
            final Neuron n = neurons[i];
            for(Dendrite d : n.outputs)
                //create a new input from the node it goes to pointing back to this one
                neurons[d.connection].inputs.add(new Dendrite(i, d.weight));
        }

        //we are done
        validated = true;
        return true;
    }


    public float[] calculate(float... inputs) {
        return calculate(DEFAULT_MAX_RECURRENT_CYCLES, DEFAULT_MAX_DIFFERENCE_BETWEEN_OUTPUTS, inputs);
    }

    /**
     * Run through the neural network until the difference each of the inputs is within the acceptable range or the
     * maximum number of cycles has been run.
     *
     * @param inputs Array of values to set the input neurons to.
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
            } if(within) break;
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
     * @param inputs Array of values to set the input neurons to.
     * @param step Set this to true if you want values to more slowly propagate through the network. Normal behavior
     *             would be when stepping is disabled.
     * @return Array of values from the output neurons.
     */
    private float[] run(float[] inputs, boolean step) {
        if(!validated)
            throw new IllegalStateException("Cannot run the ANN without being validated");
        if(inputs.length != inputNeurons)
            throw new InvalidParameterException("Invalid number of inputs.");

        //Set the input values
        for(int i = 0; i < inputNeurons; ++i)
            neurons[i].inputValue(inputs[i]);

        //create work queue and visited information
        Queue<Integer> queue = new LinkedList<>();
        BitSet visited = new BitSet(neurons.length);

        if(step) for(int i = inputNeurons; i < outputNeurons; ++i)
            queue.add(i);
        else for(int i = 0; i < inputNeurons; ++i)
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
        float[] outputs = new float[outputNeurons - inputNeurons];
        for(int i = inputNeurons, j = 0; i < outputNeurons; ++i, ++j)
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
}