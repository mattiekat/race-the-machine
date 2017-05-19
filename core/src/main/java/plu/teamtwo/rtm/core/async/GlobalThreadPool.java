package plu.teamtwo.rtm.core.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A singleton thread pool which can be used across the code. Uses a Cached Thread pool to prevent unused threads from
 * idling around a long time. Use the returned futures to track when things are completed.
 */
public class GlobalThreadPool {
    private static ExecutorService threadPool = null;

    public static ExecutorService instance() {
        if(threadPool == null)
            threadPool = Executors.newCachedThreadPool();
        return threadPool;
    }
}