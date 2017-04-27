package plu.teamtwo.rtm.neat;

public class Edge extends DirectEncoding {
    /// The historically-based ID of this edge (innovation number)
    public final int id;
    public boolean enabled;
    public int toNode;
    public int fromNode;
    public float weight;

    private static int nextEdgeID = 0;


    public Edge(Edge other){
        this(other.id, other.toNode, other.fromNode, other.weight);
        enabled = other.enabled;
    }


    public Edge(int toNode, int fromNode, float weight) {
        this(nextEdgeID, toNode, fromNode, weight);
    }


    Edge(int id, int toNode, int fromNode, float weight) {
        nextEdgeID = Math.max(id + 1, nextEdgeID);

        enabled = true;
        this.toNode = toNode;
        this.fromNode = fromNode;
        this.id = id;
        this.weight = weight;
    }
}
