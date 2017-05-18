package plu.teamtwo.rtm.client.ahni;

import com.anji.integration.Activator;
import com.ojcoleman.ahni.evaluation.HyperNEATFitnessFunction;
import com.ojcoleman.ahni.hyperneat.Properties;
import org.apache.log4j.Logger;
import org.jgapcustomised.Chromosome;
import plu.teamtwo.rtm.client.InputController;
import plu.teamtwo.rtm.core.util.Point;
import plu.teamtwo.rtm.ii.ProcessedData;
import plu.teamtwo.rtm.ii.RTSProcessor;

import java.util.Collection;
import java.util.List;


public class RTMFitnessFunction extends HyperNEATFitnessFunction implements RTSProcessor.ProcessingListener {

    //inputs
    private Point[][] positions;
    private boolean[] polygons;
    private int [][] targetCoords;
    //outputs
    private int leftKey, rightKey, spaceKey;

    // Activator
    Activator substrate;

    private static Logger logger = Logger.getLogger(RTMFitnessFunction.class);

    // Boolean determining whether or not the evaluation is currently running
    protected boolean running = false;

    public void init(Properties props){
        super.init(props);

        createPointArray(inputWidth, inputHeight);

    }

    @Override
    public double evaluate(Chromosome genotype, Activator activator, int evalThreadIndex) {
        running = true;

        substrate = activator;

        // Wait until done evaluating
        while(running) {
            try {
                synchronized(this) {
                    this.wait();
                }
            } catch(InterruptedException ex) {
                running = false;
                break;
            }
        }

        return  InputController.getInstance().getScore();
    }

    private void createPointArray(int screenWidth, int screenHeight){
        //inputWidth = width;
        //inputHeight = height;
        positions = new Point[inputWidth][inputHeight];

        double xSeg = screenWidth /  inputWidth;
        double ySeg = screenHeight / inputHeight;
        for(int i = 0; i < positions.length; i++){
            for(int j = 0; j < positions[i].length; j++){
                positions[i][j] = new Point((int)(xSeg*i + xSeg/2), (int)(ySeg*j + ySeg/2));
            }
        }

    }

    @Override
    public void frameProcessed(ProcessedData data) {
        //called everytime there's shapes
        //check when game is over, wake thread up
        double [][] input = new double[inputWidth][inputHeight];
      
        for(int i = 0; i < input.length; i++){
            for(int j = 0; j < input[i].length; j++) {
                if (data.checkPoint(positions[i][j])) {
                    input[i][j] = 1.0;
                } else {
                    input[i][j] = 0.0;
                }
            }
        }

        double[][] output = substrate.next(input);

        // Left
        InputController.getInstance().setPressed(InputController.Key.LEFT, output[0][0] > 0.5);

        // Space
        InputController.getInstance().setPressed(InputController.Key.SPACE, output[0][1] > 0.5);

        // Right
        InputController.getInstance().setPressed(InputController.Key.RIGHT, output[0][2] > 0.5);

        InputController.getInstance().updateInputs();
        // Do stuff

        // Notify when done
        if( !InputController.getInstance().isGameRunning() ) {
            synchronized(this) {
                running = false;
                this.notifyAll();
            }
        }
    }
}
