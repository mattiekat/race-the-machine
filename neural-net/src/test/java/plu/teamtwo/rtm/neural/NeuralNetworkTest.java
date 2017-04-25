package plu.teamtwo.rtm.neural;

import org.junit.Test;

import java.awt.*;
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

        for(int i = 0; i < 4; ++i)
            if(i != 1) assertTrue( net.setFunction(i, ActivationFunction.LINEAR) );

        assertTrue( net.addConnection(0, 2, -2) );
        assertTrue( net.addConnection(1, 3, 1) );
        assertFalse( net.addConnection(-1, 4, 1));
        assertFalse( net.addConnection(1, 3, 1));

        assertTrue( net.validate() );

        try {
            net.addConnection(0, 1, 1);
            assertTrue(false);
        } catch(IllegalStateException e) {}

        try {
            net.setFunction(1, ActivationFunction.LINEAR);
            assertTrue(false);
        } catch(IllegalStateException e) {}


        float[] inputs = new float[]{2.0f, 3.0f};
        float[] outputs;

        outputs = net.calculate(inputs, false);
        assertEquals(-4.0f, outputs[0], 1e-3);
        assertEquals(0.9526f, outputs[1], 1e-3);

        inputs[0] = -1.2f; inputs[1] = -2.9f;
        outputs = net.calculate(inputs, false);
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

        for(int i = 0; i < 2; ++i)
            assertTrue( net.setFunction(i, ActivationFunction.LINEAR) );

        assertTrue( net.addConnection(0, 3, 1) );
        assertTrue( net.addConnection(1, 3, 1) );
        assertTrue( net.addConnection(2, 3, 1) );
        assertTrue( net.addConnection(3, 2, 1) );
        assertTrue( net.addConnection(3, 3, 1) );

        assertTrue( net.validate() );

        float[] inputs = new float[]{0.18f, 0.2f};
        float[] outputs;

        outputs = net.calculate(inputs, false);
        outputs = net.calculate(inputs, false);
        outputs = net.calculate(inputs, false);

        //Rather than trying connection figure out what the outputs are supposed connection be, just make sure it does not loop forever
    }

    @Test
    public void steppingNetworkTest() {
        //Inputs:   0
        //Outputs:  1
        //Hidden: 2, 3
        //0 -> 2
        //2 -> 3
        //3 -> 1

        NeuralNetwork net = new NeuralNetwork(1, 1, 2);

        for(int i = 0; i < 4; ++i)
            assertTrue( net.setFunction(i, ActivationFunction.LINEAR) );

        assertTrue( net.addConnection(0, 2, 1) );
        assertTrue( net.addConnection(2, 3, 1) );
        assertTrue( net.addConnection(3, 1, 1) );
        assertTrue( net.addConnection(1, 0, 1) );

        assertTrue( net.validate() );

        float[] inputs = new float[]{1.0f};
        float[] outputs;

        //feed a 1 into the system
        outputs = net.calculate(inputs, true);
        assertEquals(0, outputs[0], 1e-3);
        inputs = new float[]{0};

        for(int s = 0; s < 50; ++s)
            //check if it alternates between 0 and 1 every third time
            assertEquals( ((s + 1) % 3 == 0 ? 1 : 0), net.calculate(inputs, true)[0], 1e-3);
    }
}