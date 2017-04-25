package plu.teamtwo.rtm.neural;

/**
 * A simple structure representing a connection between Neurons.
 */
class Dendrite implements Comparable<Dendrite> {
    //Node it connects to
    final int connection;
    //Weight associated with the connection
    final float weight;

    Dendrite(int connection, float weight) {
        this.connection = connection;
        this.weight = weight;
    }

    /**
     * Compares this dendrite connection another. Does not consider the weights.
     * @param dendrite The object connection compare with.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal connection, or greater than the specified object.
     */
    @Override
    public int compareTo(Dendrite dendrite) {
        return this.connection - dendrite.connection;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Dendrite) && (compareTo((Dendrite)o) == 0);
    }
}
