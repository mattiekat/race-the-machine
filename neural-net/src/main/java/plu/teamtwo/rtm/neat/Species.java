package plu.teamtwo.rtm.neat;

import java.util.ArrayList;

class Species extends ArrayList<Genome> {
    Genome getRep() {
        return this.get(0);
    }

    float compatibilityDistance(Genome genome) {
        return getRep().compatibilityDistance(genome);
    }
}
