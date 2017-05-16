package plu.teamtwo.rtm.genome.graph;

class Node implements Comparable<Node> {
    private static int nextNodeID = 0;
    /// The historically-based ID of this Node
    final int id;
    // public NodeType functionType;
    NodeType nodeType;


    Node(Node other) {
        this(other.id, other.nodeType);
    }


    Node(int id, NodeType nodeType) {
        nextNodeID = Math.max(id + 1, nextNodeID);

        this.id = id;
        this.nodeType = nodeType;
    }


    @Override
    public boolean equals(Object other) {
        return other instanceof Node && compareTo((Node) other) == 0;
    }


    @Override
    public int hashCode() {
        return id;
    }


    @Override
    public int compareTo(Node node) {
        return this.id - node.id;
    }
}