package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.neural.ActivationFunction;

class Node implements Comparable<Node> {
    final int id;
    final NodeType nodeType;
    ActivationFunction fn;


    Node(Node other) {
        this(other.id, other.nodeType, other.fn);
    }


    Node(int id, NodeType nodeType, ActivationFunction fn) {
        this.id = id;
        this.nodeType = nodeType;
        this.fn = fn;
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