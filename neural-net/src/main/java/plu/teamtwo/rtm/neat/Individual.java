package plu.teamtwo.rtm.neat;

import plu.teamtwo.rtm.genome.Genome;
import plu.teamtwo.rtm.genome.GenomeCache;

import java.security.InvalidParameterException;

//TODO: register json conversion functions for this class to allow it to correctly initialize from any genome
public class Individual {
    /// Genome of this individual
    public final Genome genome;
    /// Fitness score determined by the evaluation function.
    private float fitness = 0;
    /// This genome should be marked a winner iff it fulfills the requirements of the evaluation function.
    private boolean winner = false;


    /**
     * Construct a new individual with given Genome. Aka, take DNA and make a living thing of the blueprint.
     *
     * @param genome DNA of the new individual.
     */
    public Individual(Genome genome) {
        this.genome = genome;
    }


    /**
     * Makes a copy of an individual.
     *
     * @param individual The individual to copy.
     */
    public Individual(Individual individual) {
        genome = individual.genome.duplicate();
        fitness = individual.fitness;
        winner = individual.winner;
    }


    /**
     * Private constructor which should only be used by serialization.
     */
    private Individual() {
        genome = null;
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different the two individuals are.
     * This will throw an exception if they two genomes use different internal representations.
     *
     * @param a First individual.
     * @param b Second individual.
     * @return The compatibility distance.
     */
    public static float compatibilityDistance(Individual a, Individual b) {
        return a.compatibilityDistance(b);
    }


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param cache Cached information about the nodes and edges.
     * @param a     The first parent
     * @param a     The second parent
     * @return A child which is the result of crossing the individuals.
     */
    public static Individual crossMultipoint(GenomeCache cache, Individual a, Individual b) {
        return a.crossMultipoint(cache, b);
    }


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and average the values of the matching ones.
     *
     * @param cache Cached information about the genome.
     * @param a     The first parent
     * @param a     The second parent
     * @return A child which is the result of crossing the individuals.
     */
    public static Individual crossMultipointAvg(GenomeCache cache, Individual a, Individual b) {
        return a.crossMultipointAvg(cache, b);
    }


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and randomly choose between the matching ones.
     *
     * @param cache Cached information about the nodes and edges.
     * @param other The other parent.
     * @return A child which is the result of crossing the individuals.
     */
    public Individual crossMultipoint(GenomeCache cache, Individual other) {
        assertCompatible(other);
        return new Individual(genome.crossMultipoint(cache, fitness, other.genome, other.fitness, false));
    }


    /**
     * Cross the genomes of two parents to create a child. This will take the disjoint and excess genes from the most
     * fit parent and average the values of the matching ones.
     *
     * @param cache Cached information about the genome.
     * @param other The other parent.
     * @return A child which is the result of crossing the individuals.
     */
    public Individual crossMultipointAvg(GenomeCache cache, Individual other) {
        assertCompatible(other);
        return new Individual(genome.crossMultipoint(cache, fitness, other.genome, other.fitness, true));
    }


    /**
     * Get the fitness value for this individual.
     *
     * @return This individual's fitness value or 0 if unset.
     */
    public float getFitness() {
        return fitness;
    }


    /**
     * Set the fitness value for this individual.
     *
     * @param fitness A measure of how well this individual performed.
     */
    void setFitness(float fitness) {
        this.fitness = fitness;
    }


    /**
     * Set a flag representing that this genome has completely fulfilled the task defined by the evaluation function.
     */
    public void setWinner() {
        winner = true;
    }


    /**
     * Find out whether this genome has completely fulfilled the task defined by the evaluation function.
     *
     * @return True if this genome is an accepted solution to the evaluation function.
     */
    public boolean isWinner() {
        return winner;
    }


    /**
     * Compute the compatibility distance function δ. The value represents how different the two individuals are.
     * This will throw an exception if they two genomes use different internal representations.
     *
     * @param other The individual to compare this one against.
     * @return The compatibility distance.
     */
    public float compatibilityDistance(Individual other) {

        return Genome.compatibilityDistance(this.genome, other.genome);
    }


    /**
     * Internal function to verify that the other individual is compatible with this one before performing any
     * operations which would require them to be.
     *
     * @param other The individual to check for compatibility with this one.
     */
    private void assertCompatible(Individual other) {
        if(!this.genome.getClass().isInstance(other.genome))
            throw new InvalidParameterException("The two individuals are not compatible.");
    }
}
