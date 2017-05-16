package plu.teamtwo.rtm.genome;

import plu.teamtwo.rtm.core.util.Pair;

class CacheComposition extends Pair<GenomeCache, GenomeCache> implements GenomeCache {
    CacheComposition() {
        super();
    }


    CacheComposition(GenomeCache a, GenomeCache b) {
        super(a, b);
    }


    /**
     * Called when a new generation is created, will setup cache for continued use.
     */
    @Override
    public void newGeneration() {
        a.newGeneration();
        b.newGeneration();
    }
}
