package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.neural.NeuralNetwork;

import java.util.*;
import java.util.function.Function;

public class DirectEncoding implements Genome {
    private List<Node> nodeGenes = new LinkedList<>();
    private List<Edge> edgeGenes = new LinkedList<>();

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
    private static final float DISTANCE_EXCESS_COST = 1.0f;
    /// Cost of having disjoint nodes in distance function (c2).
    private static final float DISTANCE_DISJOINT_COST = 1.0f;
    /// Cost of average weight difference on matching edges (including disabled) in distance function (c3).
    private static final float DISTANCE_WEIGHT_DIFFERENCE_COST = 0.4f;

    private static final Random random = new Random();


    public DirectEncoding() {}


    public DirectEncoding(DirectEncoding other) {
        for(Node n : other.nodeGenes)
            this.nodeGenes.add(new Node(n));
        for(Edge e : other.edgeGenes)
            this.edgeGenes.add(new Edge(e));
    }


    @Override
    public DirectEncoding cross(Genome other) {
        return cross(this, (DirectEncoding)other);
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different this genome is from the other
     * one by counting the disjoint and excess edges, and the the average difference in the weights.
     * @param gOther The genome to compare this one against.
     * @return The compatibility distance.
     */
    @Override
    public float compatibilityDistance(Genome gOther) {
        return compatibilityDistance(this, (DirectEncoding)gOther);
    }


    @Override
    public DirectEncoding duplicate() {
        return new DirectEncoding(this);
    }


    @Override
    public void mutate(GenomeMutations gMutations) {
        DirectEncodingMutations mutations = (DirectEncodingMutations)gMutations;

        //TODO: allow for activation function mutations?

        if(iWill(MUTATE_EDGE_WEIGHTS))
            for(Edge e: edgeGenes)
                mutateWeight(e);

        //TODO: allow for multiple new edges or nodes in a single mutation round
        // add an edge
        if(iWill(MUTATE_NEW_EDGE))
            mutateNewEdge(mutations);

        //add a node
        if(iWill(MUTATE_NEW_NODE))
            mutateNewNode(mutations);

        for(Edge e: edgeGenes)
            if(iWill(MUTATE_EDGE_TOGGLE))
                e.enabled = !e.enabled;
    }


    private void mutateWeight(Edge e) {
        if(iWill(MUTATE_RESET_WEIGHT))
            e.weight = getRandomNum(-EDGE_WEIGHT_INIT_RANGE, EDGE_WEIGHT_INIT_RANGE);
        else
            e.weight += getRandomNum(-EDGE_WEIGHT_STEP_MAX, EDGE_WEIGHT_STEP_MAX);
    }


    private void mutateNewEdge(DirectEncodingMutations mutations) {
        int from = getRandomNum(0, nodeGenes.size() - 1);
        int to = getRandomNum(0, nodeGenes.size() - 1);

        //check if the node already exists, if so, then enable it and return from the function
        for(Edge e: edgeGenes) {
            if(e.fromNode == from && e.toNode == to) {
                e.enabled = true;
                return;
            }
        }

        //it does not already exist, check if it has been mutated before, if so, use same ID
        int id = mutations.getMutatedEdgeID(from, to);
        Edge e;
        if(id >= 0)
            e = new Edge(id, from, to, 1.0f);
        else {
            e = new Edge(to, from, 1.0f);
            mutations.addMutatedEdge(e.id, from, to);
        }

        edgeGenes.add(new Edge(id, to, from, 1.0f));
    }


    private void mutateNewNode(DirectEncodingMutations mutations) {
        Edge oldEdge = edgeGenes.get(getRandomNum(0, edgeGenes.size() - 1));
        oldEdge.enabled = false;

        int ids[] = mutations.getMutatedNodeID(oldEdge.id);
        Node newNode; Edge edgeTo, edgeFrom;
        if(ids == null) {
            newNode = new Node(NodeType.HIDDEN);
            edgeTo = new Edge(newNode.id, oldEdge.fromNode, oldEdge.weight);
            edgeFrom = new Edge(oldEdge.toNode, newNode.id, 1);
            mutations.addMutatedNode(newNode.id, edgeTo.id, edgeFrom.id, oldEdge.id);
        } else {
            newNode = new Node(ids[0], NodeType.HIDDEN);
            edgeTo = new Edge(ids[1], oldEdge.fromNode, oldEdge.weight);
            edgeFrom = new Edge(ids[2], oldEdge.toNode, newNode.id, 1);
        }

        nodeGenes.add(newNode);
        edgeGenes.add(edgeTo);
        edgeGenes.add(edgeFrom);
    }


    @Override
    public NeuralNetwork getANN() {
        //TODO: use modified sigmoid function described in paper?
        //create lists of each type of node
        List<Node> inputs  = new ArrayList<>(),
                   outputs = new ArrayList<>(),
                   hidden  = new ArrayList<>();

        //create a list of enabled edges
        List<Edge> edges = new LinkedList<Edge>();

        //create a
        for(Node n : nodeGenes) {
            switch(n.nodeType) {
                case INPUT:  inputs.add(n);  break;
                case OUTPUT: outputs.add(n); break;
                case HIDDEN: hidden.add(n);  break;
            }
        }

        //add all the connections to a list
        for(Edge e : edgeGenes)
            if(e.enabled)
                edges.add(e);

        //TODO: remove any hidden nodes that do not have a connection to the outputs
        // Invert directions and then perform DFS/BFS from exits and see what nodes are discovered

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

        return net;
    }


    public static DirectEncoding cross(DirectEncoding p1, DirectEncoding p2) {
        //TODO: only take disjoint or excess from the most fit parent
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
            //TODO: only take disjoint and excess from most fit
            //get the next node, or null if we have reached the end.
            if(e1 != null && e2 == null) { //e1 is an excess node
                child.edgeGenes.add(new Edge(e1));
                step1 = true;
            }
            else if(e1 == null && e2 != null) { //e2 is an excess node
                child.edgeGenes.add(new Edge(e2));
                step2 = true;
            }
            else if(e1.id < e2.id) { //e1 is a disjoint node
                child.edgeGenes.add(new Edge(e1));
                step1 = true;
            }
            else if(e1.id == e2.id) {
                //select edge at random
                Edge edge = new Edge(getRandomNum(0, 1) == 0 ? e1 : e2);

                //chance to disable child if either parent is disabled
                if(!e1.enabled || !e2.enabled)
                    if(iWill(CROSS_DISABLE_EDGE))
                        edge.enabled = false;

                //add the new edge to the list
                child.edgeGenes.add(edge);
                step1 = step2 = true;
            }
            else { // e1.id > e2.id //e2 is a disjoint node
                child.edgeGenes.add(new Edge(e2));
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
        for(Node n : p1.nodeGenes) {
            if(discovered.get(n.id)) {
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
            }
            else if(e1 == null && e2 != null) { //e2 is an excess node
                excess++;
                step2 = true;
            }
            else if(e1.id < e2.id) { //e1 is a disjoint node
                disjoint++;
                step1 = true;
            }
            else if(e1.id == e2.id) {
                matchingDiff += Math.abs(e1.weight - e2.weight);
                matching++;
                step1 = step2 = true;
            }
            else { // e1.id > e2.id //e2 is a disjoint node
                disjoint++;
                step2 = true;
            }

            if(step1) e1 = i1.hasNext() ? i1.next() : null;
            if(step2) e2 = i2.hasNext() ? i2.next() : null;
        }
        matchingDiff /= (float)matching;

        final float n = disjoint + excess + matching;
        float distance = matchingDiff * DISTANCE_WEIGHT_DIFFERENCE_COST;
        distance += ((float)excess / n) * DISTANCE_EXCESS_COST;
        distance += ((float)disjoint / n) * DISTANCE_DISJOINT_COST;
        return distance;
    }


    /**
     * Used to decide if the code will do something based on a probability.
     * @param p Chance this will return true.
     * @return A value of true or false with a Bernoulli distribution whose mean is p.
     */
    private static boolean iWill(float p) {
        if(p < 0.0f || p > 1.0f)
            throw new IllegalArgumentException("The probability must be between 0 and 1");
        return getRandomNum(0.0f, 1.0f) < p;
    }


    private static float getRandomNum(float min, float max) {
        if(min >= max)
            throw new IllegalArgumentException("Max must be greater than min");

        return random.nextFloat() * (max - min) + min;
    }


    private static int getRandomNum(int min, int max) {
        if(min >= max)
            throw new IllegalArgumentException("Max must be greater than min");

        return random.nextInt(max - min + 1) + min;
    }
}
