package plu.teamtwo.rtm.core;

import org.opencv.core.Core;
import plu.teamtwo.rtm.core.gui.MainWindow;
import plu.teamtwo.rtm.ii.RTSProcessor;
import plu.teamtwo.rtm.ii.ScreenCap;

public class Main {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String args[]) {
        ScreenCap sc = new ScreenCap(400, 100, 1280, 720);
        RTSProcessor rtsp = new RTSProcessor(sc);

        MainWindow window = new MainWindow(rtsp);
        rtsp.start();
    }
}
