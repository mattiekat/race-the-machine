package plu.teamtwo.rtm.neat;

public class Node extends DirectEncoding {
    public final int id;
    public NodeType nodeType;
   // public NodeType functionType;

    private static int nextNodeID = 0;


    public Node(Node other) {
        this(other.id, other.nodeType);
    }


    public Node(NodeType nodeType) {
        this(nextNodeID, nodeType);
    }


    private Node(int id, NodeType nodeType){
        nextNodeID = Math.max(id + 1, nextNodeID);

        this.id = id;
        this.nodeType = nodeType;
    }
}
