package async;

import org.junit.Test;
import plu.teamtwo.rtm.core.async.GlobalThreadPool;
import plu.teamtwo.rtm.core.util.Rand;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ThreadPoolTest {

    @Test
    public void testThreadGlobalPool() throws ExecutionException, InterruptedException {
        for(int x = 0; x < 10; ++ x) {
            ExecutorService threadPool = GlobalThreadPool.instance();

            LinkedList<Future<?>> futures = new LinkedList<>();

            for(int i = 0; i < 1000; ++i) {
                futures.add(threadPool.submit(new ShitSorter()));
            }

            while(!futures.isEmpty())
                futures.poll().get();
        }
    }

    @Test
    public void testNonThreadPool() throws ExecutionException, InterruptedException {
        for(int x = 0; x < 10; ++ x) {
            for(int i = 0; i < 1000; ++i) {
                new ShitSorter().run();
            }
        }
    }


    private class ShitSorter implements Runnable {
        private int stuff[];

        @Override
        public void run() {
            stuff = new int[100];
            for(int i = 0; i < stuff.length; ++i)
                stuff[i] = Rand.getRandomNum(0, Integer.MAX_VALUE - 1);

            for(int i = 0; i < stuff.length; ++i) {
                for(int j = 0; j < stuff.length - 1; ++j) {
                    if(stuff[j] > stuff[j+1]) {
                        int t = stuff[j+1];
                        stuff[j+1] = stuff[j];
                        stuff[j] = t;
                    }
                }
            }
        }
    }
}
