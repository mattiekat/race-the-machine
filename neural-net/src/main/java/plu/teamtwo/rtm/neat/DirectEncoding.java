package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;

import static plu.teamtwo.rtm.core.util.Rand.getRandomNum;
import static plu.teamtwo.rtm.core.util.Rand.iWill;

class DirectEncoding extends Genome {
    /// Chance to mutate the edge weights.
    private static final float MUTATE_EDGE_WEIGHTS = 0.8f;
    /// Chance that an edge weight which is being mutated will be reinitialized.
    private static final float MUTATE_RESET_WEIGHT = 0.10f;
    /// Chance for a new edge to be added to the system.
    private static final float MUTATE_NEW_EDGE = 0.05f;
    /// Chance for a node to be added to the system.
    private static final float MUTATE_NEW_NODE = 0.03f;
    /// Chance for an edge's enabled status to be flipped.
    private static final float MUTATE_EDGE_TOGGLE = 0.03f;
    /// Chance to disable an edge if either parent had it disabled.
    private static final float CROSS_DISABLE_EDGE = 0.75f;
    /// Absolute value of the initial range for an edge weight.
    private static final float EDGE_WEIGHT_INIT_RANGE = 2.0f;
    /// Amount up or down an edge weight can be stepped.
    private static final float EDGE_WEIGHT_STEP_MAX = 2.0f;
    /// Cost of having excess nodes on distance function (c1).
    private static final float DISTANCE_EXCESS_COST = 2.0f;
    /// Cost of having disjoint nodes in distance function (c2).
    private static final float DISTANCE_DISJOINT_COST = 2.0f;
    /// Cost of average weight difference on matching edges (including disabled) in distance function (c3).
    private static final float DISTANCE_WEIGHT_DIFFERENCE_COST = 0.4f;

    private List<Node> nodeGenes = new LinkedList<>();
    private List<Edge> edgeGenes = new LinkedList<>();


    /**
     * Create a new DirectEncoding with the correct input and output nodes.
     *
     * @param gCache  Cached information about the nodes and edges.
     * @param inputs  Number of inputs the system should accept.
     * @param outputs Number of outputs the system should generate.
     */
    DirectEncoding(GenomeCache gCache, int inputs, int outputs) {
        if(inputs <= 0 || outputs <= 0)
            throw new InvalidParameterException("Inputs and outputs must be greater than 0.");
        DirectEncodingCache cache = (DirectEncodingCache) gCache;

        for(int i = 0; i < inputs; ++i)
            nodeGenes.add(new Node(cache.nextNodeID(), NodeType.INPUT));
        for(int i = 0; i < outputs; ++i)
            nodeGenes.add(new Node(cache.nextNodeID(), NodeType.OUTPUT));
    }


    /**
     * Make a deep copy of another DirectEncoding.
     *
     * @param other DirectEncoding to copy.
     */
    DirectEncoding(DirectEncoding other) {
        for(Node n : other.nodeGenes)
            nodeGenes.add(new Node(n));
        for(Edge e : other.edgeGenes)
            this.edgeGenes.add(new Edge(e));
    }


    /**
     * Used to create a new, empty DirectEncoding. If this is used, make sure to initialize the list
     * of nodes to include at minimum the input and output nodes.
     */
    private DirectEncoding() {
    }


    /**
     * Copies over the input and output nodes only.
     *
     * @param nodes The nodes to copy from.
     */
    private DirectEncoding(Collection<Node> nodes) {
        for(Node n : nodes)
            if(n.nodeType == NodeType.INPUT || n.nodeType == NodeType.OUTPUT)
                nodeGenes.add(new Node(n));
    }


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param cache Cached information about the nodes and edges.
     * @param p1    First parent.
     * @param p2    Second parent.
     * @return A child which is the result of crossing the genomes
     */
    public static DirectEncoding cross(DirectEncodingCache cache, DirectEncoding p1, DirectEncoding p2) {
        //make p1 the most fit parent
        if(p1.getFitness() < p2.getFitness()) {
            DirectEncoding t = p1;
            p1 = p2;
            p2 = t;
        }

        final boolean equal = p1.getFitness() == p2.getFitness();

        //sort based on innovation number
        Comparator<Edge> sortByInnovation = (Edge a, Edge b) -> a.id - b.id;
        p1.edgeGenes.sort(sortByInnovation);
        p2.edgeGenes.sort(sortByInnovation);

        //go through both parents and line up innovation numbers
        DirectEncoding child = new DirectEncoding();
        Iterator<Edge> i1 = p1.edgeGenes.iterator();
        Iterator<Edge> i2 = p2.edgeGenes.iterator();

        Edge e1 = i1.hasNext() ? i1.next() : null;
        Edge e2 = i2.hasNext() ? i2.next() : null;
        while(e1 != null || e2 != null) { //run until we hit the end of both lists
            boolean step1 = false, step2 = false;
            //get the next node, or null if we have reached the end.
            if(e1 != null && e2 == null) { //e1 is an excess node
                child.edgeGenes.add(new Edge(e1));
                step1 = true;
            } else if(e1 == null && e2 != null) { //e2 is an excess node
                if(equal) child.edgeGenes.add(new Edge(e2));
                step2 = true;
            }
            else if(e1.id < e2.id) { //e1 is a disjoint node
                child.edgeGenes.add(new Edge(e1));
                step1 = true;
            } else if(e1.id == e2.id) {
                //select edge at random
                Edge edge = new Edge(getRandomNum(0.0f, 1.0f) < 0.5f ? e1 : e2);

                //chance to disable child if either parent is disabled
                if(!e1.enabled || !e2.enabled)
                    if(iWill(CROSS_DISABLE_EDGE))
                        edge.enabled = false;

                //add the new edge to the list
                child.edgeGenes.add(edge);
                step1 = step2 = true;
            } else { // e1.id > e2.id //e2 is a disjoint node
                if(equal) child.edgeGenes.add(new Edge(e2));
                step2 = true;
            }

            if(step1) e1 = i1.hasNext() ? i1.next() : null;
            if(step2) e2 = i2.hasNext() ? i2.next() : null;
        }

        //Find what nodes are used
        BitSet discovered = new BitSet();
        for(Edge e : child.edgeGenes) {
            discovered.set(e.toNode);
            discovered.set(e.fromNode);
        }

        //add nodes which are used by the child from either parent (must go through both lists)
        // we make the assumption that both have the same input and output nodes, so just copy
        // from the first parent.
        for(Node n : p1.nodeGenes) {
            if(n.nodeType == NodeType.INPUT || n.nodeType == NodeType.OUTPUT || discovered.get(n.id)) {
                child.nodeGenes.add(new Node(n));
                discovered.set(n.id, false);
            }
        }
        for(Node n : p2.nodeGenes) {
            if(discovered.get(n.id)) {
                child.nodeGenes.add(new Node(n));
                discovered.set(n.id, false);
            }
        }

        return child;
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different the two Genomes are by counting
     * the disjoint and excess edges, and the the average difference in the weights.
     *
     * @param d1 First genome.
     * @param d2 Second genome.
     * @return The compatibility distance.
     */
    public static float compatibilityDistance(DirectEncoding d1, DirectEncoding d2) {
        //sort based on innovation number
        Comparator<Edge> sortByInnovation = (Edge a, Edge b) -> a.id - b.id;
        d1.edgeGenes.sort(sortByInnovation);
        d2.edgeGenes.sort(sortByInnovation);

        //go through both parents and line up innovation numbers
        Iterator<Edge> i1 = d1.edgeGenes.iterator();
        Iterator<Edge> i2 = d2.edgeGenes.iterator();

        Edge e1 = i1.hasNext() ? i1.next() : null;
        Edge e2 = i2.hasNext() ? i2.next() : null;

        int disjoint = 0, excess = 0, matching = 0;
        float matchingDiff = 0;

        while(e1 != null || e2 != null) { //run until we hit the end of both lists
            boolean step1 = false, step2 = false;
            //get the next node, or null if we have reached the end.
            if(e1 != null && e2 == null) { //e1 is an excess node
                excess++;
                step1 = true;
            } else if(e1 == null && e2 != null) { //e2 is an excess node
                excess++;
                step2 = true;
            } else if(e1.id < e2.id) { //e1 is a disjoint node
                disjoint++;
                step1 = true;
            } else if(e1.id == e2.id) {
                matchingDiff += Math.abs(e1.weight - e2.weight);
                matching++;
                step1 = step2 = true;
            } else { // e1.id > e2.id //e2 is a disjoint node
                disjoint++;
                step2 = true;
            }

            if(step1) e1 = i1.hasNext() ? i1.next() : null;
            if(step2) e2 = i2.hasNext() ? i2.next() : null;
        }
        matchingDiff /= (float) matching;

        final float n = disjoint + excess + matching;
        float distance = matchingDiff * DISTANCE_WEIGHT_DIFFERENCE_COST;
        distance += ((float) excess / n) * DISTANCE_EXCESS_COST;
        distance += ((float) disjoint / n) * DISTANCE_DISJOINT_COST;
        return distance;
    }


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param cache Cached information about the nodes and edges.
     * @param other The other parent.
     * @return A child which is the result of crossing the genomes
     */
    @Override
    public DirectEncoding cross(GenomeCache cache, Genome other) {
        return cross((DirectEncodingCache) cache, this, (DirectEncoding) other);
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different this genome is from the other
     * one by counting the disjoint and excess edges, and the the average difference in the weights.
     *
     * @param gOther The genome to compare this one against.
     * @return The compatibility distance.
     */
    @Override
    public float compatibilityDistance(Genome gOther) {
        return compatibilityDistance(this, (DirectEncoding) gOther);
    }


    /**
     * Used for initial members of the first generation to create connections between the inputs and outputs. This
     * should not be needed after the first generation. It is reccomended that mutate be called after this function to
     * give the initial species some variation.
     *
     * @param gCache Cached information about the nodes and edges.
     */
    @Override
    public void initialize(GenomeCache gCache) {
        DirectEncodingCache cache = (DirectEncodingCache) gCache;
        //create an edge from every input to every output
        for(Node from : nodeGenes) {
            for(Node to : nodeGenes) {
                if(from == to || from.nodeType != NodeType.INPUT || to.nodeType != NodeType.OUTPUT) continue;
                addEdge(cache, from.id, to.id);
            }
        }
    }


    /**
     * Create a deep copy of the genome. This will enable the copy to be modified without altering the original.
     *
     * @return A duplicate of the current instance.
     */
    @Override
    public DirectEncoding duplicate() {
        return new DirectEncoding(this);
    }


    /**
     * Used to create a new DirectEncoding Cache.
     *
     * @return A new DirectEncoding Cache.
     */
    @Override
    public GenomeCache createCache() {
        return new DirectEncodingCache();
    }


    /**
     * Make random alterations to the genome (i.e. mutations). The primary changes are, 1. altering an edge weight,
     * 2. adding an edge, 3. toggling an edge, and 4. adding a new node.
     *
     * @param gCache Cached information about the nodes and edges.
     */
    @Override
    public void mutate(GenomeCache gCache) {
        DirectEncodingCache cache = (DirectEncodingCache) gCache;

        //TODO: allow for activation function mutations?

        if(iWill(MUTATE_EDGE_WEIGHTS))
            for(Edge e : edgeGenes)
                mutateWeight(e);

        //TODO: allow for multiple new edges or nodes in a single mutation round
        // add an edge
        if(iWill(MUTATE_NEW_EDGE)) {
            int from = getRandomNum(0, nodeGenes.size() - 1);
            int to = getRandomNum(0, nodeGenes.size() - 1);

            //add the edge and make sure it is enabled if it was already there.
            addEdge(cache, from, to).enabled = true;
        }

        //add a node
        if(iWill(MUTATE_NEW_NODE))
            addNode(cache, getRandomNum(0, edgeGenes.size() - 1));

        for(Edge e : edgeGenes)
            if(iWill(MUTATE_EDGE_TOGGLE))
                e.enabled = !e.enabled;
    }


    /**
     * Alter the weight on edge e. Either step it or reset it depending on chance.
     *
     * @param e Edge who's weight is to be mutated.
     */
    private void mutateWeight(Edge e) {
        if(iWill(MUTATE_RESET_WEIGHT))
            e.weight = getRandomNum(-EDGE_WEIGHT_INIT_RANGE, EDGE_WEIGHT_INIT_RANGE);
        else
            e.weight += getRandomNum(-EDGE_WEIGHT_STEP_MAX, EDGE_WEIGHT_STEP_MAX);
    }


    /**
     * Add an edge between two nodes. If the node already exists, it will simply be returned. If the node is listed in
     * the cache, then it will use the same ID.
     *
     * @param cache    Cached information about the nodes and edges.
     * @param nodeFrom Origin node for the edge.
     * @param nodeTo   Termination node for the edge.
     * @return The edge which was added (or found).
     */
    private Edge addEdge(DirectEncodingCache cache, int nodeFrom, int nodeTo) {
        //check if the node already exists
        for(Edge e : edgeGenes)
            if(e.fromNode == nodeFrom && e.toNode == nodeTo)
                return e;

        //it does not already exist, check if it has been mutated before, if so, use same ID
        int id = cache.getMutatedEdge(nodeFrom, nodeTo);
        Edge e;
        if(id >= 0)
            e = new Edge(id, nodeFrom, nodeTo, 1.0f);
        else {
            e = new Edge(cache.nextEdgeID(), nodeFrom, nodeTo, 1.0f);
            cache.addMutatedEdge(e.id, nodeFrom, nodeTo);
        }

        edgeGenes.add(e);
        return e;
    }


    /**
     * Create a new node along the specified edge. This will create a new node and connect it to the input and output
     * nodes of the edge, and then disable the edge. Thus making the change as minimal as possible.
     *
     * @param cache Cached information about the nodes and edges.
     * @param edge The edge along which to add a node.
     * @return The node which was added.
     */
    private Node addNode(DirectEncodingCache cache, int edge) {
        Edge oldEdge = edgeGenes.get(edge);
        oldEdge.enabled = false;

        int ids[] = cache.getMutatedNode(oldEdge.id);
        Node newNode;
        Edge edgeTo, edgeFrom;
        if(ids == null) {
            newNode = new Node(cache.nextNodeID(), NodeType.HIDDEN);
            edgeTo = new Edge(cache.nextEdgeID(), oldEdge.fromNode, newNode.id, oldEdge.weight);
            edgeFrom = new Edge(cache.nextEdgeID(), newNode.id, oldEdge.toNode, 1);
            cache.addMutatedNode(newNode.id, edgeTo.id, edgeFrom.id, oldEdge.id);
        } else {
            newNode = new Node(ids[0], NodeType.HIDDEN);
            edgeTo = new Edge(ids[1], oldEdge.fromNode, newNode.id, oldEdge.weight);
            edgeFrom = new Edge(ids[2], newNode.id, oldEdge.toNode, 1);
        }

        nodeGenes.add(newNode);
        edgeGenes.add(edgeTo);
        edgeGenes.add(edgeFrom);
        return newNode;
    }


    /**
     * Create a runnable ANN which is represented by the genome.
     *
     * @return The ANN represented by the genome.
     */
    @Override
    public NeuralNetwork getANN() {
        //TODO: use modified sigmoid function described in paper?
        //create lists of each type of node
        List<Node> inputs = new ArrayList<>(),
                outputs = new ArrayList<>(),
                hidden = new ArrayList<>();

        //create a list of enabled edges
        List<Edge> edges = new LinkedList<Edge>();

        //create a
        for(Node n : nodeGenes) {
            switch(n.nodeType) {
                case INPUT:
                    inputs.add(n);
                    break;
                case OUTPUT:
                    outputs.add(n);
                    break;
                case HIDDEN:
                    hidden.add(n);
                    break;
            }
        }

        //add all the connections to a list
        for(Edge e : edgeGenes)
            if(e.enabled)
                edges.add(e);

        //construct a neural network now that we know the sizes
        NeuralNetwork net = new NeuralNetwork(inputs.size(), outputs.size(), hidden.size());

        //set the activation functions (not needed presently)
        /*{
            int i = 0;
            for(Node n : inputs)
                net.setFunction(i++, n.function)
            for(Node n : outputs)
                net.setFunction(i++, n.function)
            for(Node n : hidden)
                net.setFunction(i++, n.function)
        }*/

        //TODO: make sure there are not duplicate edges making their way into the system
        //TODO: improve efficiency of conversion
        //create the connections
        Function<Integer, Integer> findIndex = (Integer id) -> {
            int index = 0;
            for(Node n : inputs) {
                if(n.id == id) return index;
                index++;
            }
            for(Node n : outputs) {
                if(n.id == id) return index;
                index++;
            }
            for(Node n : hidden) {
                if(n.id == id) return index;
                index++;
            }
            return -1;
        };

        for(Edge e : edges) {
            net.connect(findIndex.apply(e.fromNode), findIndex.apply(e.toNode), e.weight);
        }

        net.validate();
        return net;
    }
}
