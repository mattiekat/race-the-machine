package plu.teamtwo.rtm.neat;

class Edge {
    /// The historically-based ID of this edge (innovation number)
    final int id;
    boolean enabled;
    int toNode;
    int fromNode;
    float weight;


    Edge(Edge other) {
        this(other.id, other.fromNode, other.toNode, other.weight);
        enabled = other.enabled;
    }


    Edge(int id, int fromNode, int toNode, float weight) {
        enabled = true;
        this.toNode = toNode;
        this.fromNode = fromNode;
        this.id = id;
        this.weight = weight;
    }
}
