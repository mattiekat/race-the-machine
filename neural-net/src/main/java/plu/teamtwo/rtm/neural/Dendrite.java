package plu.teamtwo.rtm.neural;

/**
 * A simple structure representing a connection between Neurons.
 */
class Dendrite {
    final int to;
    final float weight;

    Dendrite(int to, float weight) {
        this.to = to;
        this.weight = weight;
    }
}
