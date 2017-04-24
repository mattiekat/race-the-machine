package plu.teamtwo.rtm.neural;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * This is an ANN which can be run on inputs and then will provide outputs based on that.
 * ANN's are not to be modified once made, if a new one is desired with different structure,
 * it will need to be re-built. This is designed to work in tandem with Genome.
 *
 * To use a NeuralNetwork, first construct it with the correct information about the number of different node types.
 * Next call setNeuron for all neurons, keep in mind that [0 to inputNeurons) will be the inputs,
 * [inputNeurons, outputNeurons) will be the output nodes, and [outputNeurons, neurons.length) will be the hidden nodes.
 * Finally call validate() which will finalize the structure, enabling it to be calculated.
 */
public class NeuralNetwork {
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
        neurons = new Neuron[numinputs + numoutputs + numhidden];
        inputNeurons = numinputs;
        outputNeurons = numinputs + numoutputs;
    }


    /**
     * Set the value and outputs for a neuron. This will need to happen for every neuron before the ANN can be used.
     * @param id ID of the neuron to be set. Inputs are the lowest values, outputs follow, and then hidden are at the end.
     * @param connections IDs of neurons that this one connects to.
     * @param activationFunction The activation function that should be used for this neuron.
     * @return True if the neuron was set successfully, otherwise false.
     */
    public boolean setNeuron(int id, Collection<Dendrite> connections, ActivationFunction activationFunction) {
        if(validated)
            throw new IllegalStateException("Cannot set Neurons once the ANN has been validated.");

        //check validity
        if(id < 0 || id > neurons.length || neurons[id] != null)
            return false;
        for(Dendrite d : connections)
            if(d.to < 0 || d.to >= neurons.length)
                return false;

        //add the neuron
        neurons[id] = new Neuron(activationFunction);
        neurons[id].outputs.addAll(connections);
        return true;
    }


    /**
     * Finalize the ANN state. This will enable running calculations if it succeeds. This will basically calculate other
     * index values or cache information which can then be used to speed up the execution of the ANN.
     * @return True if it was successfully validated, false otherwise.
     */
    public boolean validate() {
        if(validated) return true;

        //validate the values
        for(Neuron n : neurons) {
            //make sure it is set
            if(n == null) return false;
            //make sure the connections are valid
            for(Dendrite d : n.outputs)
                if(d.to < 0 || d.to > neurons.length)
                    return false;
        }

        //construct the backreferences
        for(int i = 0; i < neurons.length; ++i) {
            final Neuron n = neurons[i];
            for(Dendrite d : n.outputs)
                //create a new input from the node it goes to pointing back to this one
                neurons[d.to].inputs.add(new Dendrite(i, d.weight));
        }

        //Sort the connections in the lists
        final Comparator<Dendrite> comparator =
                (Dendrite a, Dendrite b) -> (a.to - b.to);
        for(Neuron n : neurons) {
            n.inputs.sort(comparator);
            n.outputs.sort(comparator);
        }

        //we are done
        validated = true;
        return true;
    }


    /**
     * Runs the neural network on a set of inputs and provides the resulting outputs. This will do a full run through
     * the network taking the inputs values all the way to the output neurons.
     * @param inputs Array of values to set the input neurons to.
     * @param step Set this to true if you want values to more slowly propagate through the network. Normal behavior
     *             would be when stepping is disabled.
     * @return Array of values from the output neurons.
     */
    public float[] calculate(float[] inputs, boolean step) {
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
                neurons[d.to].inputValue(value * d.weight);

            //if we are stepping, then go through inputs, otherwise go though outputs and add to queue
            for(Dendrite d : (step ? neuron.inputs : neuron.outputs))
                if(!visited.get(d.to))
                    queue.add(d.to);
        }

        //read values at output neurons
        float[] outputs = new float[outputNeurons - inputNeurons];
        for(int i = inputNeurons, j = 0; i < outputNeurons; ++i, ++j)
            outputs[j] = neurons[i].getOutput();

        return outputs;
    }
}