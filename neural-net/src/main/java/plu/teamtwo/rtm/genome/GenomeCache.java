package plu.teamtwo.rtm.genome;

/**
 * Used to track mutations within a generation to properly give the same identification.
 */
public interface GenomeCache {
    /**
     * Called when a new generation is created, will setup cache for continued use.
     */
    void newGeneration();
}



