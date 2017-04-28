package plu.teamtwo.rtm.neat;

public class Edge extends DirectEncoding {
    /// The historically-based ID of this edge (innovation number)
    public final int id;
    public boolean enabled;
    public int toNode;
    public int fromNode;
    public float weight;


    public Edge(Edge other) {
        this(other.id, other.fromNode, other.toNode, other.weight);
        enabled = other.enabled;
    }


    public Edge(int id, int fromNode, int toNode, float weight) {
        enabled = true;
        this.toNode = toNode;
        this.fromNode = fromNode;
        this.id = id;
        this.weight = weight;
    }
}
