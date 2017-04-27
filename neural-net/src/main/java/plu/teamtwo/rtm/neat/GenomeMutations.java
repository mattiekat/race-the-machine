package plu.teamtwo.rtm.neat;

/**
 * Used to track mutations within a generation to properly give the same identification.
 */
public class GenomeMutations {}



/**
 * Used to track mutations within a generation to properly give the same node ids and edge ids.
 */
class DirectEncodingMutations extends GenomeMutations {

    /**
     * Get the ID of a node mutated along an edge.
     * @param edge ID of the edge to check if a node was mutated on.
     * @return Array of [nodeID, edgeToID, EdgeFromID], or null if no node was mutated on that edge.
     */
    int[] getMutatedNodeID(int edge) {
        return null;
    }


    /**
     * Get the ID (Innovation Number) of an edge which was mutated between the nodes `from` and `to`.
     * @param from Starting node of the edge to check for.
     * @param to Ending node of the edge to check for.
     * @return ID of the added edge, or -1 if no edge was mutated between those nodes.
     */
    int getMutatedEdgeID(int from, int to) {
        return -1;
    }


    void addMutatedNode(int nodeID, int edgeToID, int edgeFromID, int edge) {

    }


    void addMutatedEdge(int id, int from, int to) {

    }
}