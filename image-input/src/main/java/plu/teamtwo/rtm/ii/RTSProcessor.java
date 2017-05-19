package plu.teamtwo.rtm.ii;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import plu.teamtwo.rtm.core.util.Point;
import plu.teamtwo.rtm.core.util.Polygon;

import javax.imageio.ImageIO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RTSProcessor {

    private ProcessRunner processRunner = null;
    private Thread processThread = null;
    private ListenRunner listenRunner = null;
    private Thread listenThread = null;

    // Atomic, so synchronization isn't needed
    private int fps = 0;

    private ScreenCap capper;
    private final Object capSwitchLock = new Object();

    private ConcurrentLinkedQueue<ProcessedData> cap_queue = new ConcurrentLinkedQueue<>();

    private final Mat[] numtemps = new Mat[10];
    private Mat[] numtempsScaled = new Mat[10];

    private Point numBoundsMin = null;
    private Point numBoundsMax = null;
    private final Object numBoundsSwitchLock = new Object();

    public RTSProcessor() {this(new ScreenCap()); }
    public RTSProcessor(ScreenCap capper) {
        this.capper = capper;

        try {
            numtemps[0] = loadFromJar("/numtemp_0.png", 1);
            numtemps[1] = loadFromJar("/numtemp_1.png", 1);
            numtemps[2] = loadFromJar("/numtemp_2.png", 1);
            numtemps[3] = loadFromJar("/numtemp_3.png", 1);
            numtemps[4] = loadFromJar("/numtemp_4.png", 1);
            numtemps[5] = loadFromJar("/numtemp_5.png", 1);
            numtemps[6] = loadFromJar("/numtemp_6.png", 1);
            numtemps[7] = loadFromJar("/numtemp_7.png", 1);
            numtemps[8] = loadFromJar("/numtemp_8.png", 1);
            numtemps[9] = loadFromJar("/numtemp_9.png", 1);
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("FileNotFoundException: " + ex.getMessage());
        }

        scaleTemps();
    }

    private void scaleTemps() {

        // Original Resolution for the number templates was 1920x1080
        double scaleX = (double)capper.getArea().width / 1920.0;
        double scaleY = (double)capper.getArea().height / 1080.0;
        System.out.println("Scale x:" + scaleX + ", y:" + scaleY);

        for(int i = 0; i < 10; i++) {
            if(numtemps[i].empty())
                System.err.println("Empty Img "+i);
            numtempsScaled[i] = new Mat();
            System.out.println(i + " - cols:" + numtemps[i].cols() + ", rows:" + numtemps[i].rows());
            Size size = new Size(numtemps[i].cols()*scaleX, numtemps[i].rows()*scaleY);
            System.out.println(i + " - width:" + size.width + ", height:" + size.height);
            Imgproc.resize(numtemps[i], numtempsScaled[i], size);
        }
    }

    private Mat loadFromJar(String name, int flags) throws FileNotFoundException {
        URL url = getClass().getResource(name);
        if(url == null)
            throw new FileNotFoundException(name);

        String path = url.getPath();
        if(path.startsWith("/"))
            path = path.substring(1);

        Mat image = Imgcodecs.imread(path, flags);

        if (image.empty()) {
            BufferedImage buf;

            try {
                buf = ImageIO.read(url);
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                return image;
            }

            int height = buf.getHeight();
            int width = buf.getWidth();
            int rgb, type, channels;

            switch (flags) {
                case Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE:
                    type = CvType.CV_8UC1;
                    channels = 1;
                    break;
                case Imgcodecs.CV_LOAD_IMAGE_COLOR:
                default:
                    type = CvType.CV_8UC3;
                    channels = 3;
                    break;
            }

            byte[] px = new byte[channels];
            image = new Mat(height, width, type);

            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    rgb = buf.getRGB(x, y);
                    px[0] = (byte)(rgb & 0xFF);
                    if (channels==3) {
                        px[1] = (byte)((rgb >> 8) & 0xFF);
                        px[2] = (byte)((rgb >> 16) & 0xFF);
                    }
                    image.put(y, x, px);
                }
            }
        }

        return image;
    }



    public int getFPS() { return fps; }

    //public ProcessedData getNext() { return cap_queue.poll(); }

    public synchronized void start() {

        // Start the process runner
        if(processRunner == null) {
            processRunner = new ProcessRunner();
            processThread = new Thread(processRunner);
            processThread.start();
        }

        // Start the listen runner
        if(listenRunner == null) {
            listenRunner = new ListenRunner();
            listenThread = new Thread(listenRunner);
            listenThread.start();
        }
    }

    public synchronized void stop() {

        // Stop the process runner
        if(processRunner != null) processRunner.running = false;
        processRunner = null;
        if(processThread != null) processThread.interrupt();
        processThread = null;

        // Stop the listen runner
        if(listenRunner != null) listenRunner.running = false;
        listenRunner = null;
        if(listenThread != null) listenThread.interrupt();
        listenThread = null;
    }

    public void setCapper(ScreenCap capper) {
        synchronized(capSwitchLock) {
            this.capper = capper;
        }
        scaleTemps();
    }

    public void setNumBounds(Point min, Point max) {
        synchronized(numBoundsSwitchLock) {
            this.numBoundsMin = min;
            this.numBoundsMax = max;
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
        void frameProcessed(ProcessedData data);
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

    private class ListenRunner implements Runnable {

        boolean running = true;

        @Override
        public void run() {
            while(running) {

                // Wait until there is data that has been processed
                while(cap_queue.isEmpty()) {
                    try {
                        synchronized(this) {
                            this.wait();
                        }
                    } catch(InterruptedException ex) {
                        running = false;
                        break;
                    }
                }

                // Thread was interrupted / stopped
                if(!running || cap_queue.isEmpty()) break;

                // Send processed data to listeners
                ProcessedData data = cap_queue.poll();
                synchronized(listeners) {
                    for(ProcessingListener listener : listeners)
                        listener.frameProcessed(data);
                }
            }
        }

        public synchronized void wake() {
            this.notifyAll();
        }
    }

    private class ProcessRunner implements Runnable {

        boolean running = true;

        @Override
        public void run() {
            long total_time = 0;
            int count = 0;
            long current_time = System.currentTimeMillis();
            while(running) {

                // Get Screencap
                BufferedImage cap;
                synchronized(capSwitchLock) {
                    cap = capper.capture();
                }

                // Process the Screencap
                cap_queue.offer(processImg(cap));
                if(cap_queue.size() > 300) cap_queue.poll();
                //ProcessedData data = processImg(cap);

                // Calculate current FPS
                count++;
                long new_time = System.currentTimeMillis();
                total_time += (new_time - current_time);
                if(total_time > 1000) {
                    total_time -= 1000;
                    fps = count;
                    count = 0;
                }
                current_time = new_time;

                // Notify the other thread to make sure its awake
                if(listenRunner != null) listenRunner.wake();
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

            int score = parseScore(frame);

            return new ProcessedData(img, Util.matToBufferedImage(dest), lp, score);
        }

        private int parseScore(Mat input) {

            Rect rectCrop;
            synchronized(numBoundsSwitchLock) {
                if(numBoundsMin == null || numBoundsMax == null) return -1;
                rectCrop = new Rect(
                        numBoundsMin.x.intValue() - capper.getArea().x,
                        numBoundsMin.y.intValue() - capper.getArea().y,
                        (numBoundsMax.x.intValue() - numBoundsMin.x.intValue() + 1),
                        (numBoundsMax.y.intValue() - numBoundsMin.y.intValue() + 1));
            }
            //System.out.println("rectCrop - X:"+rectCrop.x + ", Y:"+rectCrop.y + ", width:"+rectCrop.width + ", height:"+rectCrop.height);
            //System.out.println("input - X:"+input.rows() + ", Y:"+input.cols());
            Mat cropped = input.submat(rectCrop);

            TreeMap<Double, Integer> numbers = new TreeMap<>(Collections.reverseOrder());

            for(int i = 0; i < 10; i++) {

                Mat result = new Mat(cropped.rows() - numtempsScaled[i].rows() + 1, cropped.cols() - numtempsScaled[i].cols() + 1, CvType.CV_32FC1);

                Imgproc.matchTemplate(cropped, numtempsScaled[i], result, Imgproc.TM_CCOEFF_NORMED);
                Imgproc.threshold(result, result, 0.8, 1.0, Imgproc.THRESH_TOZERO);

                final double tolerance = 0.9;

                while(true) {

                    Core.MinMaxLocResult mm = Core.minMaxLoc(result);
                    if(mm.maxVal >= tolerance) {

                        numbers.put(mm.maxLoc.x, i);

                        Mat mask = Mat.zeros(result.rows() + 2, result.cols() + 2, CvType.CV_8U);
                        Imgproc.floodFill(result, mask, mm.maxLoc, new Scalar(0), null, new Scalar(0.1), new Scalar(1.0), 4);
                    } else break;
                }
            }

            int num = 0;
            int pow = 0;
            if(numbers.isEmpty()) num = -1;
            else {
                for (Map.Entry<Double, Integer> entry : numbers.entrySet())
                    num += (int) (entry.getValue().doubleValue() * Math.pow(10, pow++));
            }
            return num;
        }
    }

}
