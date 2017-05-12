package plu.teamtwo.rtm.client.gui;

import plu.teamtwo.rtm.ii.ProcessedData;
import plu.teamtwo.rtm.ii.RTSProcessor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame implements RTSProcessor.ProcessingListener {

    private ImagePanel img_panel_capture;
    private ImagePanel img_panel_processed;

    protected final RTSProcessor rtsp;

    public MainWindow(RTSProcessor rtsp) {
        this.rtsp = rtsp;
        this.rtsp.addListener(this);

        this.setMinimumSize(new Dimension(800, 600));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setLayout(new BorderLayout());

        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(2, 3));

        this.add(pane, BorderLayout.NORTH);

        img_panel_capture = new ImagePanel(rtsp);
        img_panel_processed = new ImagePanel(rtsp);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Captured Image", img_panel_capture);
        tabs.addTab("Processed Image", img_panel_processed);

        this.add(tabs, BorderLayout.CENTER);

        this.pack();
        this.setVisible(true);
    }

    @Override
    public void frameProcessed(ProcessedData data) {
        SwingUtilities.invokeLater(() -> {
                img_panel_capture.setContents(data.capturedImage, data.polygons);
                img_panel_processed.setContents(data.processedImage, data.polygons);
                repaint();
            });
    }
}
