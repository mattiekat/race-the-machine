package plu.teamtwo.rtm.ii;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Main Test Class for OpenCV.
 *
 * Note about library loading: The library binaries must be in the location the Java binaries are ran from. When testing,
 * this is usually the project root. Currently, only Win64 binaries are in the project root; to test in a Unix
 * environment, please build the Unix binaries and include them.
 */
public class Main {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        System.out.println("Welcome to OpenCV " + Core.VERSION);
        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
        System.out.println("OpenCV Mat: " + m);
        Mat mr1 = m.row(1);
        mr1.setTo(new Scalar(1));
        Mat mc5 = m.col(5);
        mc5.setTo(new Scalar(5));
        System.out.println("OpenCV Mat data:\n" + m.dump());

        ScreenCap sc = new ScreenCap(0, 0, 1280, 720);

        int[] counts = new int[10];
        long total = 0;
        for(int i = 0; i < 10; i++) {
            long time = System.currentTimeMillis();
            int count = 0;
            while(total < 1000) {
                sc.capture();
                count++;
                long nt = System.currentTimeMillis();
                total += (nt - time);
                time = nt;
            }
            counts[i] = count;
            total -= 1000;
        }

        System.out.println("ScreenCap Counts (per second):");
        int tc = 0;
        for(int i = 0; i < 10; i++) {
            System.out.print("\t"+counts[i]+",");
            tc += counts[i];
        }
        System.out.println("\nAverage Count: "+(tc/10));
    }
}
