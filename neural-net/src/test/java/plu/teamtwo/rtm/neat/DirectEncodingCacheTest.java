package plu.teamtwo.rtm.neat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DirectEncodingCacheTest {
    @Test
    public void testGetNextID() {
        DirectEncodingCache cache = new DirectEncodingCache();
        for(int x = 0; x < 100; ++x) {
            assertEquals(x, cache.nextNodeID());
            assertEquals(x, cache.nextEdgeID());
        }
    }


    @Test
    public void testMutatedNodes() {
        DirectEncodingCache cache = new DirectEncodingCache();
        assertEquals(null, cache.getMutatedNode(0));
        cache.addMutatedNode(4, 1, 2, 0);
        int[] info = cache.getMutatedNode(0);
        assertEquals(3, info.length);
        assertEquals(4, info[0]);
        assertEquals(1, info[1]);
        assertEquals(2, info[2]);
        for(int x = 1; x < 10; ++x)
            assertEquals(null, cache.getMutatedNode(x));
        cache.newGeneration();
        assertEquals(null, cache.getMutatedNode(0));
    }


    @Test
    public void testMutatedEdges() {
        DirectEncodingCache cache = new DirectEncodingCache();
        for(int x = 0; x < 10; ++x)
            for(int y = 0; y < 10; ++y)
                assertEquals(-1, cache.getMutatedEdge(x, y));

        cache.addMutatedEdge(4, 2, 7);
        cache.addMutatedEdge(6, 6, 6);
        assertEquals(4, cache.getMutatedEdge(2, 7));
        cache.addMutatedEdge(7, 8, 4);
        assertEquals(7, cache.getMutatedEdge(8, 4));
        assertEquals(6, cache.getMutatedEdge(6, 6));

        cache.newGeneration();
        for(int x = 0; x < 10; ++x)
            for(int y = 0; y < 10; ++y)
                assertEquals(-1, cache.getMutatedEdge(x, y));
    }
}
