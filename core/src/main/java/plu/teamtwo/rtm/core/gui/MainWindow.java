package plu.teamtwo.rtm.core.gui;

import plu.teamtwo.rtm.ii.ProcessedData;
import plu.teamtwo.rtm.ii.RTSProcessor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame implements RTSProcessor.ProcessingListener, ChangeListener {

    private ImagePanel img_panel_capture;
    private ImagePanel img_panel_processed;

    private JSlider slid_H_min;
    private JSlider slid_S_min;
    private JSlider slid_V_min;

    private JSlider slid_H_max;
    private JSlider slid_S_max;
    private JSlider slid_V_max;

    protected final RTSProcessor rtsp;

    public MainWindow(RTSProcessor rtsp) {
        this.rtsp = rtsp;
        this.rtsp.addListener(this);

        this.setMinimumSize(new Dimension(800, 600));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setLayout(new BorderLayout());

        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(2, 3));

        slid_H_min = new JSlider(0, 255, 0);
        slid_S_min = new JSlider(0, 255, 0);
        slid_V_min = new JSlider(0, 255, 100);

        slid_H_max = new JSlider(0, 255, 255);
        slid_S_max = new JSlider(0, 255, 255);
        slid_V_max = new JSlider(0, 255, 255);

        slid_H_min.addChangeListener(this);
        slid_S_min.addChangeListener(this);
        slid_V_min.addChangeListener(this);
        slid_H_max.addChangeListener(this);
        slid_S_max.addChangeListener(this);
        slid_V_max.addChangeListener(this);

        pane.add(slid_H_min);
        pane.add(slid_S_min);
        pane.add(slid_V_min);

        pane.add(slid_H_max);
        pane.add(slid_S_max);
        pane.add(slid_V_max);

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

    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e a ChangeEvent object
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        rtsp.setHSVBounds(
                slid_H_min.getValue(),
                slid_S_min.getValue(),
                slid_V_min.getValue(),
                slid_H_max.getValue(),
                slid_S_max.getValue(),
                slid_V_max.getValue()
        );
    }

    @Override
    public void frameProcessed() {
        SwingUtilities.invokeLater(new Runnable(){
            @Override public void run() {
                ProcessedData data = rtsp.getNext();
                img_panel_capture.setContents(data.capturedImage, data.polygons);
                img_panel_processed.setContents(data.processedImage, data.polygons);
                // TODO: Send Data to Neural Network
                repaint();
            }
        });
    }
}
