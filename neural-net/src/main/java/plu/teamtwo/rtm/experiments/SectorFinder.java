package plu.teamtwo.rtm.experiments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import plu.teamtwo.rtm.genome.graph.MultilayerSubstrateEncodingBuilder;
import plu.teamtwo.rtm.neat.GAController;
import plu.teamtwo.rtm.neat.Individual;
import plu.teamtwo.rtm.neat.ScoringFunction;
import plu.teamtwo.rtm.neural.ActivationFunction;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static plu.teamtwo.rtm.core.util.Rand.getRandomNum;

public class SectorFinder implements Runnable {

    public SectorFinder() {}

    @Override
    public void run() {
        PrintStream output = new PrintStream(new FileOutputStream(FileDescriptor.out));
        GAController controller = new GAController(
              new MultilayerSubstrateEncodingBuilder()
                      .inputs(new int[]{9, 9})
                      .addLayer(new int[]{9, 9})
                      .addLayer(new int[]{9, 9})
                      .outputs(new int[]{3, 3})
                      .hiddenFunction(ActivationFunction.TANH)
                      .outputFunction(ActivationFunction.TANH)
        );

        controller.createFirstGeneration();

        for(int g = 0; g < 1000; ++g) {
            boolean foundWinner = controller.assesGeneration(new SectorFinderScore());
            final Individual best = controller.getBestIndividual();
            System.out.println(String.format("Gen %d: %.2f, %.1f", controller.getGenerationNum(), controller.getFitness(), best.getFitness()));
            if(foundWinner) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                System.out.println(gson.toJson(best));
                return;
            }
            controller.nextGeneration();
        }
    }

    public static void main(String[] args) {
        new SectorFinder().run();
    }


    /**
     * Imagine a Sudoku board, each little tile contains a value in the range (-1, 1). The task of the machine, is
     * to find the sector (3x3 area) which contains the highest value on the entire board.
     *   ╔═══╤═══╤═══╦═══╤═══╤═══╦═══╤═══╤═══╗
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╟───┼───┼───╫───┼───┼───╫───┼───┼───╢
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╟───┼───┼───╫───┼───┼───╫───┼───┼───╢
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╠═══╪═══╪═══╬═══╪═══╪═══╬═══╪═══╪═══╣
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╟───┼───┼───╫───┼───┼───╫───┼───┼───╢
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╟───┼───┼───╫───┼───┼───╫───┼───┼───╢
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╠═══╪═══╪═══╬═══╪═══╪═══╬═══╪═══╪═══╣
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╟───┼───┼───╫───┼───┼───╫───┼───┼───╢
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╟───┼───┼───╫───┼───┼───╫───┼───┼───╢
     *   ║   │   │   ║   │   │   ║   │   │   ║
     *   ╚═══╧═══╧═══╩═══╧═══╧═══╩═══╧═══╧═══╝
     */
    private class SectorFinderScore implements ScoringFunction {
        private int count = 0;
        private int expectedSector = 0;
        private int correct = 0;

        /**
         * This function will be called for once for every individual which is being evaluated. Each scoring function
         * can thus use its own data and know that it will be called with information about only one individual even in a
         * multithreaded context. This should act as a constructor since the caller will not know what subtype it is.
         *
         * @return A new scoring function in the initial state.
         */
        @Override
        public ScoringFunction createNew() {
            return new SectorFinderScore();
        }


        /**
         * This will be called to determine how many simultaneous instances of the function can exist.
         *
         * @return The maximum number of threads or 0 if there is no reasonable limit.
         */
        @Override
        public int getMaxThreads() {
            return 1;
        }


        /**
         * This will be called to determine if the neural network should be flushed between inputs.
         * It will only be called once.
         *
         * @return True if the network should be flushed between inputs.
         */
        @Override
        public boolean flushBetween() {
            return true;
        }


        /**
         * This will be called to determine if the neural network should use the step function instead of calculate. Step
         * may perform better on real-time tasks because it allows some carry over between steps, but will fail on discrete
         * input output pairs such as XOR. If this is enabled, flush should be disabled or values may not make it to
         * outputs.
         *
         * @return True if the network should use real time processing.
         */
        @Override
        public boolean realTimeProcessing() {
            return false;
        }


        /**
         * This function will be called to retrieve the inputs which should be used by the network. This will be called
         * until it returns null, signaling the end of inputs.
         *
         * @return An array of output values.
         */
        @Override
        public float[] generateInput() {
            if(count++ >= 100) return null;

            float[] vals = new float[81];
            float max = Float.NEGATIVE_INFINITY;
            for(int i = 0; i < 81; ++i) {
                final float val = getRandomNum(-1.0f, 1.0f);
                if(val > max) {
                    max = val;
                    expectedSector = calculateSector(i);
                }
                vals[i] = val;
            }
            return vals;
        }


        /**
         * This function will be called with the outputs generated by the neural network after being feed the input from
         * generateInput().
         *
         * @param output Array of output values generated by the network.
         */
        @Override
        public void acceptOutput(float[] output) {
            float max = Float.NEGATIVE_INFINITY;
            int sector = 0;
            for(int i = 0; i < 9; ++i) {
                final float val = output[i];
                if(val > max) {
                    max = val;
                    sector = calculateSector(i);
                }
            }

            if(expectedSector == sector) {
                //System.out.println(Arrays.toString(output));
                correct++;
            }
        }


        /**
         * This function will be called to asses the performance of the neural network. This function will not be called
         * until generateInput() returns null.
         *
         * @return A score which can be used to asses the fitness of the specified individual.
         */
        @Override
        public double getScore() {
            return correct;
        }


        /**
         * Check if the task was successfully completed. Some tasks may never qualify as completed.
         *
         * @return True if the assessment was passed.
         */
        @Override
        public boolean isWinner() {
            return correct == 100;
        }


        /**
         * Calculate the sector of a point in the 1D matrix representing a Sudoku-esqe board.
         * @param index Index in the 81 long array.
         * @return The sector the index is in.
         */
        private int calculateSector(int index) {
            final int col = (index % 9) / 3;
            final int row = ((index / 9) % 9) / 3; //remember: this uses integer division as a floor
            return row * 3 + col;
        }
    }
}
