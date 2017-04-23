package plu.teamtwo.rtm.neat;

/**
 * Created by hannah on 4/20/17.
 */
public class Node extends DirectEncoding {

    public int id;
    public NodeType nodeType;
   // public NodeType functionType;

    public Node(){}

    public Node(int id, NodeType nodeType){
        this.id = id;
        this.nodeType = nodeType;
    }
}
