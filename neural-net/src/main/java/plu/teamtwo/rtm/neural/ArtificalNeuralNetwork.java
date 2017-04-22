package plu.teamtwo.rtm.neural;

import java.security.InvalidParameterException;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This is an ANN which can be run on inputs and then will provide outputs based on that.
 * ANN's are not to be modified once made, if a new one is desired with different structure,
 * it will need to be re-built. This is designed to work in tandem with Genome.
 */
public class ArtificalNeuralNetwork {
    /// The neurons in the ANN. The neurons are stored in this order: input, output, hidden.
    private final Neuron[] neurons;
    /// The end of the input nodes in the ANN (i.e. input nodes are [0, inputNodes) ).
    private final int inputNodes;
    /// The end of the output nodes in the ANN (i.e. output nodes are [inputNodes, outputNodes) ).
    private final int outputNodes;

    public ArtificalNeuralNetwork(int numinputs, int numoutputs, int numhidden) {
        if(numinputs < 1 || numoutputs < 1 || numhidden < 0)
            throw new InvalidParameterException("Invalid number of nodes to form an ANN.");

        neurons = new Neuron[numinputs + numoutputs + numhidden];
        inputNodes = numinputs;
        outputNodes = numinputs + numoutputs;
    }


    public boolean setNeuron(int id, Collection<Dendrite> connections, FunctionType functionType) {
        if(id < 0 || id > neurons.length || neurons[id] != null)
            return false;

        neurons[id] = new Neuron(id, functionType);
        neurons[id].connections.addAll(connections);
        return true;
    }


    public float[] calculate(float[] inputs) {
        if(inputs.length != inputNodes)
            throw new InvalidParameterException("Invalid number of inputs.");

        //Set the input values
        for(int i = 0; i < inputNodes; ++i)
            neurons[i].inputValue(inputs[i]);

        //create work queue and visited information
        Queue<Integer> queue = new LinkedList<>();
        BitSet visited = new BitSet(neurons.length);
        for(int i = 0; i < inputNodes; ++i)
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
            for(Dendrite d : neuron.connections) {
                neurons[d.to].inputValue(value * d.weight);

                if(!visited.get(d.to))
                    queue.add(d.to);
            }
        }

        //read values at output neurons
        float[] outputs = new float[outputNodes - inputNodes];
        for(int i = inputNodes, j = 0; i < outputNodes; ++i, ++j)
            outputs[j] = neurons[i].getOutput();

        return outputs;
    }
}