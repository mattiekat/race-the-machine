package plu.teamtwo.rtm.genome.graph;

import plu.teamtwo.rtm.neural.ActivationFunction;

class CPPNNode extends Node {
    final ActivationFunction fn;

    CPPNNode(Node other) {
        super(other);

        if(other instanceof CPPNNode)
            fn = ((CPPNNode) other).fn;
        else
            fn = ActivationFunction.SIGMOID;
    }

    CPPNNode(int id, NodeType nodeType, ActivationFunction fn) {
        super(id, nodeType);
        this.fn = fn;
    }
}
