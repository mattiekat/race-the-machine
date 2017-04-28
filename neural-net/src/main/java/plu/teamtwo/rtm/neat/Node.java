package plu.teamtwo.rtm.neat;

public class Node extends DirectEncoding {
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
}
