package plu.teamtwo.rtm.neat;

/**
 * This is used by the NEAT Controller to run and asses the capability of individuals in the population.
 * The function generateInput() will be called until it returns null. For each time that generateInput() is called and
 * does not return null, it will be run through the network and then the output will be passed to acceptOutput(). Once
 * generateInput() returns null, getScore() will be called to determine the fitness of the current individual.
 *
 * The functions will be called from a separate thread, and if maxThreads returned a value greater than 1, multiple
 * threads may call the function concurrently. If the scoring function needs to keep track of what individual from the
 * population is calling it, then it should make use of the ID value passed as a parameter. This ID value will be in the
 * range [0, POPULATION_SIZE) and should allow for an array of relevant information to be kept and accessed
 * concurrently.
 */
public interface ScoringFunction {
    /**
     * This will be called to determine how many simultaneous instances of the function can exist.
     *
     * @return The maximum number of threads or 0 if there is no reasonable limit.
     */
    int maxThreads();

    /**
     * This function will be called to retrieve the inputs which should be used by the network. This will be called
     * until it returns null, signaling the end of inputs.
     *
     * @param id An ID used to identify the calling individual from the population.
     * @return An array of output values.
     */
    float[] generateInput(int id);

    /**
     * This function will be called with the outputs generated by the neural network after being feed the input from
     * generateInput().
     *
     * @param id     An ID used to identify the calling individual from the population.
     * @param output Array of output values generated by the network.
     */
    void acceptOutput(int id, float[] output);

    /**
     * This function will be called to asses the performance of the neural network. This function will not be called
     * until generateInput() returns null.
     *
     * @param id An ID used to identify the calling individual from the population.
     * @return A score which can be used to asses the fitness of the specified individual.
     */
    float getScore(int id);
}
