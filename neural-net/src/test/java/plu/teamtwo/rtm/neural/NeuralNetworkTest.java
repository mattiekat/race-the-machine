package plu.teamtwo.rtm.neural;

import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class NeuralNetworkTest {
    @Test
    public void basicNeuralNetworkTest() {
        //Inputs:   0, 1
        //Outputs:  2, 3
        //Hidden:
        //0 -> 2
        //1 -> 3

        NeuralNetwork net = new NeuralNetwork(2, 2, 0);
        LinkedList<Dendrite> dendrites = new LinkedList<>();

        dendrites.add(new Dendrite(2, -2));
        assertTrue( net.setNeuron(0, dendrites, ActivationFunction.LINEAR) );

        dendrites.clear(); dendrites.add(new Dendrite(3, 1));
        assertTrue( net.setNeuron(1, dendrites, ActivationFunction.SIGMOID) );

        assertFalse( net.validate() );

        dendrites.clear();
        assertTrue( net.setNeuron(2, dendrites, ActivationFunction.LINEAR) );
        assertTrue( net.setNeuron(3, dendrites, ActivationFunction.LINEAR) );

        assertTrue( net.validate() );

        try {
            net.setNeuron(0, dendrites, ActivationFunction.LINEAR);
            assertTrue(false);
        } catch(IllegalStateException e) {}

        float[] inputs = new float[]{2.0f, 3.0f};
        float[] outputs;

        outputs = net.calculate(inputs);
        assertEquals(-4.0f, outputs[0], 1e-3);
        assertEquals(0.9526f, outputs[1], 1e-3);

        inputs[0] = -1.2f; inputs[1] = -2.9f;
        outputs = net.calculate(inputs);
        assertEquals(2.4f, outputs[0], 1e-3);
        assertEquals(0.0522f, outputs[1], 1e-3);
    }

    @Test
    public void recurrentNeuralNetworkTest() {
        //Inputs:   0, 1
        //Outputs:  2
        //Hidden:   3
        //0 -> 3
        //1 -> 3
        //2 -> 3
        //3 -> 2, 3

        NeuralNetwork net = new NeuralNetwork(2, 1, 1);
        LinkedList<Dendrite> dendrites = new LinkedList<>();

        dendrites.add(new Dendrite(3, 1));
        assertTrue( net.setNeuron(0, dendrites, ActivationFunction.LINEAR) );

        dendrites.clear(); dendrites.add(new Dendrite(3, 1));
        assertTrue( net.setNeuron(1, dendrites, ActivationFunction.LINEAR) );

        dendrites.clear(); dendrites.add(new Dendrite(3, 1));
        assertTrue( net.setNeuron(2, dendrites, ActivationFunction.SIGMOID) );

        dendrites.clear();
        dendrites.add(new Dendrite(2, 1));
        dendrites.add(new Dendrite(3, 1));
        assertTrue( net.setNeuron(3, dendrites, ActivationFunction.SIGMOID) );

        assertTrue( net.validate() );

        float[] inputs = new float[]{0.18f, 0.2f};
        float[] outputs;

        outputs = net.calculate(inputs);
        outputs = net.calculate(inputs);
        outputs = net.calculate(inputs);

        //Rather than trying to figure out what the outputs are supposed to be, just make sure it does not loop forever
    }
}