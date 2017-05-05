package plu.teamtwo.rtm.neat;

class Node implements Comparable<Node> {
    /// The historically-based ID of this Node
    public final int id;
    public NodeType nodeType;
   // public NodeType functionType;

    private static int nextNodeID = 0;


    public Node(Node other) {
        this(other.id, other.nodeType);
    }


    public Node(int id, NodeType nodeType) {
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
