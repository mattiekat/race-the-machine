package plu.teamtwo.rtm.core.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static plu.teamtwo.rtm.core.util.Rand.*;

public class RandTest {

    @Before
    public void seed() {
        //use this to prevent the tests from sometimes failing by shear chance
        seedRandom(790214376);
    }


    @Test
    public void testGetRandomNumIntInt() {
        int[] dist = new int[10];
        Arrays.fill(dist, 0);

        for(int x = 0; x < 1000; ++x) {
            final int i = getRandomNum(0, 9);
            assertTrue(i >= 0 && i < 10);
            dist[i]++;
        }

        for(int x = 0; x < 10; ++x)
            assertEquals(0.10f, (float) dist[x] / 1000.0f, 0.1f);
    }


    @Test
    public void testGetRandomNumFloatFloat() {
        int[] dist = new int[10];
        Arrays.fill(dist, 0);

        for(int x = 0; x < 1000; ++x) {
            final float i = getRandomNum(0.0f, 10.0f);
            assertTrue(i >= 0.0f && i < 10.0f);
            dist[(int) i]++;
        }

        for(int x = 0; x < 10; ++x)
            assertEquals(0.10f, (float) dist[x] / 1000.0f, 0.1f);
    }


    @Test
    public void testIWill() {
        int a = 0, b = 0;
        for(int x = 0; x < 1000; ++x) {
            if(iWill(0.2f)) ++a;
            else ++b;
        }
        assertEquals(0.2f, (float) a / 1000.0f, 0.1f);
    }


    @Test
    public void testRandomBackWeightedIndex() {
        int[] dist = new int[10];
        Arrays.fill(dist, 0);

        for(int x = 0; x < 10000; ++x) {
            final int i = randomBackWeightedIndex(10, 1.0f);
            assertTrue(i >= 0 && i < 10);
            dist[i]++;
        }

        int last = -1;
        for(int x = 0; x < 10; ++x) {
            assertTrue(dist[x] >= last);
            last = dist[x];
        }
    }


    @Test
    public void testRandomFrontWeightedIndex() {
        int[] dist = new int[10];
        Arrays.fill(dist, 0);

        for(int x = 0; x < 10000; ++x) {
            final int i = randomFrontWeightedIndex(10, 1.0f);
            assertTrue(i >= 0 && i < 10);
            dist[i]++;
        }

        int last = 10000;
        for(int x = 0; x < 10; ++x) {
            assertTrue(dist[x] < last);
            last = dist[x];
        }
    }
}