package neat;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by hannah on 4/20/17.
 */
public class DirectEncoding implements Genome {

    private LinkedList<Node> nodeGenes;
    private LinkedList<Edge> edgeGenes;
    private final float MUTATE_WEIGHT = 0.05f;
    private final float MUTATE_WEIGHT_STEP_MAX = 1.0f;
    private final float MUTATE_EDGE = 0.02f;
    private final float MUTATE_NODE = 0.01f;
    private final float MUTATE_ENABLER = 0.03f;
    private float weight;
    private boolean nodeEnabled;
    private int innovationNum = 0;
    private int nodeIDs = 0;

    public DirectEncoding(){
        nodeGenes = new LinkedList<Node>();
        edgeGenes = new LinkedList<Edge>();
    }

    @Override
    public void getANN() {
        //can't do yet
    }

    @Override
    public void cross() {

    }

    @Override
    public void mutation() {

        for(Edge e: edgeGenes){
            if(Math.random() < MUTATE_WEIGHT){
                mutateWeight(e);
            }
        }

        if(Math.random() < MUTATE_EDGE){
            mutateEdge();
        }

        //add node
        if(Math.random() < MUTATE_NODE){
            mutateNode();
        }

        for(Edge e: edgeGenes){
            if(Math.random() < MUTATE_ENABLER){
                e.enabled = !e.enabled;
            }
        }
    }

    private void mutateWeight(Edge e){

        float newStep = getRandomNum(-MUTATE_WEIGHT_STEP_MAX, MUTATE_WEIGHT_STEP_MAX);
        e.weight = e.weight + newStep;

    }

    private void mutateEdge(){

        int randFrom = getRandomNum(0, nodeGenes.size() - 1);
        int randTo = getRandomNum(0, nodeGenes.size() - 1);

        for(Edge e: edgeGenes){
            if(e.fromNode == randFrom && e.toNode == randTo ){
                return;
            }
        }
        edgeGenes.add(new Edge(randTo, randFrom, innovationNum++, 1.0f));
    }

    private void mutateNode(){

        int randIndex = getRandomNum(0, edgeGenes.size() - 1);
        Edge randEdge = edgeGenes.get(randIndex);
        randEdge.enabled = false;

        Node added = new Node(nodeIDs++, NodeType.HIDDEN);
        Edge extra1 = new Edge(added.id, randEdge.fromNode, innovationNum++, randEdge.weight);
        Edge extra2 = new Edge(randEdge.toNode, added.id, innovationNum++, 1);

        nodeGenes.add(added);
        edgeGenes.add(extra1);
        edgeGenes.add(extra2);

    }

    private float getRandomNum(float min, float max){

        if(min >= max){
            throw new IllegalArgumentException("Max must be greater than min");
        }

        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    private int getRandomNum(int min, int max) {

        if(min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }

        Random rand = new Random();
        return rand.nextInt() * (max -min) + min;
    }

    @Override
    public void serialization() {
        //don't worry yet

    }

    /**
     * initialize it to empty--serialization stuff, new linked list
     * node and edge types to 0.
     */

    //cross -- takes a genome, cast it to one and see if it's null
    //serialization -- LOOK IT UP, and do it


}
