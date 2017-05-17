package plu.teamtwo.rtm.neural;

import plu.teamtwo.rtm.core.util.Pair;

import java.security.InvalidParameterException;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;

public class CPPN implements NeuralNetwork {
    private static final ActivationFunction DEFAULT_ACTIVATION_FUNCTION = ActivationFunction.TANH;

    /// The neurons in the ANN. The neurons are stored in this order: input, output, hidden.
    private final Neuron[] neurons;
    /// The end of the input nodes in the ANN (i.e. input nodes are [0, endInput) ).
    private final int endInput;
    /// The end of the output nodes in the ANN (i.e. output nodes are [endInput, endOutput) ).
    private final int endOutput;
    /// Is this specific neural network recurrent
    private final boolean recurrent;
    /// Maximum number of cycles to run for during calculations to compute recurrent neural networks.
    private final int MAX_RECURRENT_CYCLES;
    /// Maximum difference between outputs before considering it to have converged.
    private final float MAX_DIFFERENCE_BETWEEN_OUTPUTS;


    CPPN(CPPNBuilder builder) {
        if(builder.in < 1 || builder.out < 1 || builder.hidden < 0)
            throw new InvalidParameterException("Invalid number of nodes to form an ANN.");

        endInput = builder.in;
        endOutput = builder.in + builder.out;
        MAX_RECURRENT_CYCLES = builder.maxRecurrentCycles;
        MAX_DIFFERENCE_BETWEEN_OUTPUTS = builder.maxDifferenceBetweenOutputs;

        neurons = new Neuron[builder.in + builder.out + builder.hidden];
        for(int i = 0; i < neurons.length; ++i)
            neurons[i] = new Neuron(DEFAULT_ACTIVATION_FUNCTION);

        for(Pair<Integer, Integer> k : builder.connections.keySet())
            connect(k.a, k.b, builder.connections.get(k));

        for(int f : builder.activation.keySet())
            setFunction(f, builder.activation.get(f));

        //check for recurrence and unused nodes
        int[] discovered = new int[neurons.length];
        int[] completed = new int[neurons.length];
        BitSet black = new BitSet();

        //start from all input nodes
        boolean recurrent = false;
        for(int x = 0, count = 1; x < endInput; ++x) {
            Pair<Integer, Boolean> result = DFS(count, x, discovered, completed);
            count = result.a;
            recurrent = recurrent | result.b;
        }
        this.recurrent = recurrent;

        //now have discovered and completed values, check if anything was not reached and remove
        // its outputs if so to cut back on the network calculations.
        for(int x = 0; x < neurons.length; ++x)
            if(discovered[x] <= 0)
                neurons[x].outputs.clear();

        //construct the backreferences
        for(int i = 0; i < neurons.length; ++i) {
            final Neuron n = neurons[i];
            for(Dendrite d : n.outputs)
                //create a new input from the node it goes to pointing back to this one
                neurons[d.connection].inputs.add(new Dendrite(i, d.weight));
        }
    }


    /**
     * Run through the neural network until the difference each of the inputs is within the acceptable range or the
     * maximum number of cycles has been run.
     *
     * @param inputs Array of values to set the input neurons to.
     * @return Output of the network.
     */
    @Override
    public float[] calculate(float... inputs) {
        float[] output = run(inputs, false), last = null;

        Outer: //run until we hit max cycles or the value has stabilized
        for(int cycle = 0; cycle < MAX_RECURRENT_CYCLES; ++cycle) {
            last = output;
            output = run(inputs, false);

            //check if any exceed max difference and end the loop if so
            for(int i = 0; i < output.length; ++i)
                if(Math.abs(last[i] - output[i]) > MAX_DIFFERENCE_BETWEEN_OUTPUTS)
                    continue Outer;
            break; //none of them were different
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
    @Override
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
    @Override
    public void flush() {
        for(Neuron n : neurons)
            n.flush();
    }


    /**
     * Determines if the neural network has recurrent cycles in it.
     *
     * @return True if it is a recurrent neural network.
     */
    @Override
    public boolean isRecurrent() {
        return recurrent;
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
     * Checks if the CPPN is recurrent by performing DFS and also set the discovered and completed arrays such that they
     * can be used for topological sort. The first run of DFS should have a count of 1.
     *
     * @return Count to use in next DFS call and True/False over whether the network is recurrent.
     */
    private Pair<Integer, Boolean> DFS(int count, int neuron, int[] discovered, int[] completed) {
        //visit the node
        discovered[neuron] = count++;
        boolean recurrent = false;

        //go through outputs
        for(Dendrite d : neurons[neuron].outputs) {
            //while not technically recurrent if it connects to an input, short of sorting it needs to be treated as one.
            if(d.connection < endInput)
                recurrent = true;

            //recurrent if it is a grey node
            if(discovered[d.connection] > 0 && completed[d.connection] <= 0)
                recurrent = true;
                //if it is not already processed, perform DFS on it
            else if(completed[neuron] <= 0) {
                Pair<Integer, Boolean> result = DFS(count, d.connection, discovered, completed);
                count = result.a;
                recurrent = result.b;
            }
        }

        //leave this node
        completed[neuron] = count++;
        return new Pair<>(count, recurrent);
    }
}
