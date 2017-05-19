package plu.teamtwo.rtm.client.scratch;


import plu.teamtwo.rtm.client.InputController;
import plu.teamtwo.rtm.genome.graph.MultilayerSubstrateEncodingBuilder;
import plu.teamtwo.rtm.neat.GAController;
import plu.teamtwo.rtm.neat.Individual;

public class RTSHyperNeat implements Runnable {
    private GAController controller;

    public static int INPUT_WIDTH = 32;
    public static int INPUT_HEIGHT = 24;

    public static final int[] inputDimensions = {INPUT_WIDTH, INPUT_HEIGHT};
    public static final int[] hiddenDimensions = {INPUT_WIDTH, INPUT_HEIGHT};
    public static final int[] outputDimensions = {3};

    public RTSHyperNeat() {
        controller = new GAController(new MultilayerSubstrateEncodingBuilder()
                                              .inputs(inputDimensions)
                                              .outputs(outputDimensions)
                                              .addLayer(hiddenDimensions)
        );

        controller.createFirstGeneration();
    }

    @Override
    public void run() {
        for(int g = 0;/*EVER (Or Until Something Breaks)*/;++g) {
            controller.assesGeneration(new RTSScoringFunction(INPUT_WIDTH, INPUT_HEIGHT));
            Individual best = controller.getBestIndividual();
            System.out.println(String.format("Gen %d: %f, %f", g, controller.getFitness(), best.getFitness()));
            controller.nextGeneration();
        }
    }
}
