package plu.teamtwo.rtm.neat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to track mutations within a generation to properly give the same identification.
 */
abstract class GenomeCache {
    /**
     * Called when a new generation is created, will setup cache for continued use.
     */
    abstract void newGeneration();
}



/**
 * Used to track mutations within a generation to properly give the same node ids and edge ids.
 */
class DirectEncodingCache extends GenomeCache {
    private Map<Integer, int[]> mutatedNodes = new HashMap<>();
    private Map<Long, Integer> mutatedEdges = new HashMap<>();

    private int nextNodeID = 0;
    private int nextEdgeID = 0;


    /**
     * Get the ID of a node mutated along an edge.
     *
     * @param edge ID of the edge to check if a node was mutated on.
     * @return Array of [nodeID, edgeToID, EdgeFromID], or null if no node was mutated on that edge.
     */
    int[] getMutatedNodeID(int edge) {
        int[] vals = mutatedNodes.get(edge);
        return vals == null ? null : Arrays.copyOf(vals, 3);
    }


    /**
     * Get the ID (Innovation Number) of an edge which was mutated between the nodes `from` and `to`.
     *
     * @param from Starting node of the edge to check for.
     * @param to   Ending node of the edge to check for.
     * @return ID of the added edge, or -1 if no edge was mutated between those nodes.
     */
    int getMutatedEdgeID(int from, int to) {
        Integer edge = mutatedEdges.get(hashTwoInts(from, to));
        return edge == null ? -1 : edge;
    }


    int getNextNodeID() {
        return nextNodeID++;
    }


    int getNextEdgeID() {
        return nextEdgeID++;
    }


    /**
     * Add information about a newly mutated node.
     * @param nodeID The ID of the new node.
     * @param edgeToID ID of the edge going to the new node.
     * @param edgeFromID ID of the edge coming from the new node.
     * @param edge ID of the edge along which the node was added.
     */
    void addMutatedNode(int nodeID, int edgeToID, int edgeFromID, int edge) {
        mutatedNodes.put(edge, new int[]{nodeID, edgeToID, edgeFromID});
    }


    /**
     * Add information about a newly mutated edge.
     * @param id ID of the new edge.
     * @param from ID of the node which the edge comes from.
     * @param to ID of the node which the edge goes to.
     */
    void addMutatedEdge(int id, int from, int to) {
        mutatedEdges.put(hashTwoInts(from, to), id);
    }


    /**
     * Convert the (a, b) data to a unique id.
     * @param a First value to be hashed.
     * @param b Second value to be hashed.
     * @return Unique, deterministic value defined by the two values.
     */
    private static long hashTwoInts(int a, int b) {
        long result = a;
        result <<= 32;
        result |= b;
        return result;
    }


    /**
     * Called when a new generation is created, will setup cache for continued use. In this case, it will clear
     * the list of mutated edges and node, but maintain information about the new ID Values.
     */
    @Override
    void newGeneration() {
        mutatedEdges.clear();
        mutatedNodes.clear();
    }
}