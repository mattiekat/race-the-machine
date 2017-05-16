package plu.teamtwo.rtm.genome.graph;

class Edge implements Comparable<plu.teamtwo.rtm.genome.graph.Edge> {
    /// The historically-based ID of this edge (innovation number)
    final int id;
    boolean enabled;
    int toNode;
    int fromNode;
    float weight;


    Edge(plu.teamtwo.rtm.genome.graph.Edge other) {
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


    @Override
    public boolean equals(Object other) {
        return other instanceof plu.teamtwo.rtm.genome.graph.Edge && compareTo((plu.teamtwo.rtm.genome.graph.Edge) other) == 0;
    }


    @Override
    public int hashCode() {
        return id;
    }


    @Override
    public int compareTo(plu.teamtwo.rtm.genome.graph.Edge other) {
        return this.id - other.id;
    }
}