package plu.teamtwo.rtm.genome;

public interface GenomeBuilder {
    /**
     * Create and return the appropriate type of cache to be used for the given genome.
     *
     * @return The appropriate type of cache for the given genome.
     */
    GenomeCache createCache();

    /**
     * Creates a new, blank genome ready for mutations. This allows the the GAController to distance itself from the
     * specifics of the problem itself.
     *
     * @param cache The cache used in creation.
     * @return A new genome ready for use.
     */
    Genome create(GenomeCache cache);
}
