package plu.teamtwo.rtm.neat;

public class Edge extends DirectEncoding {
    public final int innovationNum;
    public boolean enabled;
    public int toNode;
    public int fromNode;
    public float weight;

    private static int nextInnovationNumber = 0;


    public Edge(Edge other){
        this(other.innovationNum, other.toNode, other.fromNode, other.weight);
        enabled = other.enabled;
    }


    public Edge(int toNode, int fromNode, float weight) {
        this(nextInnovationNumber, toNode, fromNode, weight);
    }


    private Edge(int innovationNum, int toNode, int fromNode, float weight) {
        nextInnovationNumber = Math.max(innovationNum + 1, nextInnovationNumber);

        enabled = true;
        this.toNode = toNode;
        this.fromNode = fromNode;
        this.innovationNum = innovationNum;
        this.weight = weight;
    }
}
