package neat;

/**
 * Created by hannah on 4/20/17.
 */
public class Edge extends DirectEncoding {

    public boolean enabled;
    public int toNode;
    public int fromNode;
    public int innovationNum;
    public float weight;


    public Edge(){}

    public Edge(int toNode, int fromNode, int innovationNum, float weight){
        enabled = true;
        this.toNode = toNode;
        this.fromNode = fromNode;
        this.innovationNum = innovationNum;
        this.weight = weight;
    }
}
