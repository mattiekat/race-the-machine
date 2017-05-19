package plu.teamtwo.rtm.client;

import org.opencv.core.Core;
import plu.teamtwo.rtm.client.gui.MainWindow;
import plu.teamtwo.rtm.ii.RTSProcessor;
import plu.teamtwo.rtm.ii.ScreenCap;


public class Main {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static RTSProcessor rtsp;

    public static void main(String args[]) {
        ScreenCap sc = new ScreenCap(400, 100, 1280, 720);
        rtsp = new RTSProcessor(sc);
        InputController.init(sc.getScreen());

        MainWindow window = new MainWindow(rtsp);
        rtsp.start();
    }
}
