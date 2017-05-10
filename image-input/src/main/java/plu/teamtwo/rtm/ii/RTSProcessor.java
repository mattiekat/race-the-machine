package plu.teamtwo.rtm.ii;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import plu.teamtwo.rtm.core.util.Point;
import plu.teamtwo.rtm.core.util.Polygon;

import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RTSProcessor {

    private Runner runner = null;

    // Atomic, so synchronization isn't needed
    private int fps = 0;

    private ScreenCap capper;
    private final Object capSwitchLock = new Object();

    //private Scalar lowerBounds = new Scalar(0,0,0);
    //private Scalar upperBounds = new Scalar(255, 255, 255);
    //private final Object boundsLock = new Object();

    private ConcurrentLinkedQueue<ProcessedData> cap_queue = new ConcurrentLinkedQueue<>();

    public RTSProcessor(ScreenCap capper) {
        this.capper = capper;
    }

    public RTSProcessor() {
        this.capper = new ScreenCap();
    }

    public int getFPS() { return fps; }

    public ProcessedData getNext() { return cap_queue.poll(); }

    public synchronized void start() {
        if(runner == null) {
            runner = new Runner();
            Thread thread = new Thread(runner);
            thread.start();
        }
    }

    public synchronized void stop() {
        if(runner != null) runner.running = false;
        runner = null;
    }

    public void setCapper(ScreenCap capper) {
        synchronized(capSwitchLock) {
            this.capper = capper;
        }
    }

    /**
     * Screen Getter. Retrieves the <code>GraphicsDevice</code> representing the screen this <code>RTSProcessor</code>
     * object is capturing from.
     *
     * @return <code>GraphicsDevice screen</code>
     */
    public GraphicsDevice getScreen() { return capper.getScreen(); }

    /**
     * Area Getter. Retrieves a <code>Rectangle</code> representing the area of the screen this <code>RTSProcessor</code>
     * object is capturing from.
     *
     * @return <code>Rectangle area</code>
     */
    public Rectangle getArea() { return capper.getArea(); }

    public interface ProcessingListener {
        void frameProcessed();
    }

    private final HashSet<ProcessingListener> listeners = new HashSet<>();

    public void addListener(ProcessingListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(ProcessingListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    private class Runner implements Runnable {

        public boolean running = true;

        @Override
        public void run() {
            long total_time = 0;
            int count = 0;
            long current_time = System.currentTimeMillis();
            while(running) {
                BufferedImage cap;
                synchronized(capSwitchLock) {
                    cap = capper.capture();
                }
                //cap_queue.offer(cap);
                cap_queue.offer(processImg(cap));
                if(cap_queue.size() > 300) cap_queue.poll();
                count++;
                long new_time = System.currentTimeMillis();
                total_time += (new_time - current_time);
                if(total_time > 1000) {
                    total_time -= 1000;
                    fps = count;
                    count = 0;
                }
                current_time = new_time;

                // TODO: Parallelize RTSProcessor Listener updates
                synchronized(listeners) {
                    for(ProcessingListener listener : listeners)
                        listener.frameProcessed();
                }
            }
        }

        private ProcessedData processImg(BufferedImage img) {
            Mat frame = Util.bufferedImageToMat(img);
            Mat gray = new Mat();
            Mat edges = new Mat();
            Mat morphOutput = new Mat();

            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.blur(gray, edges, new Size(3, 3));
            Imgproc.Canny(edges, edges, 10, 20*3);

            Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8));
            Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

            //Imgproc.erode(edges, morphOutput, erodeElement);
            //Imgproc.erode(edges, morphOutput, erodeElement);

            Imgproc.dilate(edges, morphOutput, dilateElement);

            List<MatOfPoint> contours = new LinkedList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(morphOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            LinkedList<Polygon> lp = new LinkedList<>();
            for(MatOfPoint entry : contours) {
                if(Imgproc.contourArea(entry) < 5) continue;
                //if(Imgproc.contourArea(entry) > 75000) continue;
                LinkedList<Point> myPoints = new LinkedList<>();
                for(org.opencv.core.Point p : entry.toList())
                    myPoints.add(new Point(p.x, p.y));
                if(myPoints.size() > 2) {
                    Polygon poly = new Polygon(myPoints);
                    if( (poly.max.x.doubleValue() - poly.min.x.doubleValue()) / (poly.max.y.doubleValue() - poly.min.y.doubleValue()) < 7.0 )
                        lp.add(poly);
                }
            }

            Mat dest = new Mat();
            morphOutput.copyTo(dest);

            return new ProcessedData(img, Util.matToBufferedImage(dest), lp);
        }
    }

}
