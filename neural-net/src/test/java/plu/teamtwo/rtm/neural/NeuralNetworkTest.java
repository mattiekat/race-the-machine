package plu.teamtwo.rtm.neural;

import org.junit.Test;

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

         CPPN.Builder builder = new CPPN.Builder()
                 .inputs(2)
                 .outputs(2)
                 .hidden(0)
                 .connect(0, 2, -2)
                 .connect(1, 3, 1);

        for(int i = 0; i < 4; ++i)
            builder.setFunction(i, ActivationFunction.LINEAR);

        NeuralNetwork net = builder.create();

        float[] outputs;

        outputs = net.calculate(2.0f, 3.0f);
        assertEquals(-4.0f, outputs[0], 1e-3);
        assertEquals(3.0f, outputs[1], 1e-3);

        outputs = net.calculate(-1.2f, -2.9f);
        assertEquals(2.4f, outputs[0], 1e-3);
        assertEquals(-2.9f, outputs[1], 1e-3);
    }


    @Test
    public void feedForwardNetworkTest() {
        //Inputs:   0, 1, 2
        //Outputs:  3
        //Hidden:   4
        //1 -> 4
        //2 -> 4
        //4 -> 3

        NeuralNetwork net = new CPPN.Builder()
                .inputs(3)
                .outputs(1)
                .hidden(1)
                .connect(0, 3,  0.21f)
                .connect(2, 3, -0.35f)
                .connect(2, 4, -1.19f)
                .connect(1, 3,  1.89f)
                .connect(1, 4,  1.94f)
                .connect(4, 3, -1.86f)
                .create();

        float[] outputs;
        outputs = net.calculate(1.0f, 0.0f, 0.0f);
        outputs = net.calculate(1.0f, 0.0f, 1.0f);
        outputs = net.calculate(1.0f, 1.0f, 0.0f);
        outputs = net.calculate(1.0f, 1.0f, 1.0f);
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

         CPPN.Builder builder = new CPPN.Builder()
                 .inputs(2)
                 .outputs(1)
                 .hidden(1)
                 .connect(0, 3, 1)
                 .connect(1, 3, 1)
                 .connect(2, 3, 1)
                 .connect(3, 2, 1)
                 .connect(3, 3, 1);

        for(int i = 0; i < 2; ++i)
            builder.setFunction(i, ActivationFunction.LINEAR);

        NeuralNetwork net = builder.create();

        float[] inputs = new float[]{0.18f, 0.2f};
        float[] outputs;

        outputs = net.calculate(inputs);
        outputs = net.calculate(inputs);
        outputs = net.calculate(inputs);

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

        CPPN.Builder builder = new CPPN.Builder()
                .inputs(1)
                .outputs(1)
                .hidden(2)
                .connect(0, 2, 1)
                .connect(2, 3, 1)
                .connect(3, 1, 1)
                .connect(1, 0, 1);

        for(int i = 0; i < 4; ++i)
            builder.setFunction(i, ActivationFunction.LINEAR);

        NeuralNetwork net = builder.create();

        float[] inputs = new float[]{1.0f};
        float[] outputs;

        //feed a 1 into the system
        outputs = net.step(inputs);
        assertEquals(0, outputs[0], 1e-3);
        inputs = new float[]{0};

        for(int s = 0; s < 50; ++s)
            //check if it alternates between 0 and 1 every third time
            assertEquals( ((s + 1) % 3 == 0 ? 1 : 0), net.step(inputs)[0], 1e-3);
    }
}