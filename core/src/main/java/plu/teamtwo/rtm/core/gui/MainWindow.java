package plu.teamtwo.rtm.core.gui;

import plu.teamtwo.rtm.ii.RTSProcessor;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private JPanel img_panel;

    public MainWindow(RTSProcessor rtsp) {

        this.setMinimumSize(new Dimension(800, 600));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        img_panel = new ImagePanel(rtsp);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Image Output", img_panel);

        this.add(tabs);

        this.pack();
        this.setVisible(true);
    }
}
