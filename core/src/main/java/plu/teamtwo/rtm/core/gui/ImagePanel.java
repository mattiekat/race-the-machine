package plu.teamtwo.rtm.core.gui;

import plu.teamtwo.rtm.ii.RTSProcessor;

import plu.teamtwo.rtm.core.util.Point;
import plu.teamtwo.rtm.ii.ScreenCap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel implements RTSProcessor.ProcessingListener, MouseListener {

    protected final RTSProcessor rtsp;
    protected BufferedImage img = null;
    public static final Font FPS_FONT = new Font("Arial", Font.BOLD, 36);
    protected final Dimension fullSize;

    private long lastClickedTime = 0;
    private Point firstClick = null;

    public ImagePanel(RTSProcessor rtsp) {
        this.rtsp = rtsp;
        this.rtsp.addListener(this);
        DisplayMode dm = this.rtsp.getScreen().getDisplayMode();
        fullSize = new Dimension(dm.getWidth(), dm.getHeight());
        this.addMouseListener(this);
    }

    @Override
    public void frameProcessed() {
        SwingUtilities.invokeLater(new Runnable(){
            @Override public void run() {
                img = rtsp.getNext();
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        double xMod = this.getWidth() / fullSize.getWidth();
        double yMod = this.getHeight() / fullSize.getHeight();
        Rectangle area = rtsp.getArea();
        int x = (int)(area.getX() * xMod);
        int y = (int)(area.getY() * yMod);
        int width = (int)(area.getWidth() * xMod);
        int height = (int)(area.getHeight() * yMod);
        g.setColor(Color.YELLOW);
        ((Graphics2D)g).setStroke(new BasicStroke(8));
        g.drawRect(x, y, width, height);
        if(img != null) g.drawImage(img, x, y, width, height, null);
        g.setFont(FPS_FONT);
        String fpsStr = ""+rtsp.getFPS();
        g.setColor(Color.BLACK);
        g.fillRect(6, 6, 50, 40);
        g.setColor(Color.YELLOW);
        g.drawString(fpsStr, 10, 40);

        if(firstClick != null) {
            java.awt.Point p = getMousePosition();
            if(p != null) {
                g.setColor(Color.MAGENTA);
                g.drawRect(
                        Math.min(firstClick.x.intValue(), p.x),
                        Math.min(firstClick.y.intValue(), p.y),
                        Math.max(firstClick.x.intValue(), p.x),
                        Math.max(firstClick.y.intValue(), p.y)
                );
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastClickedTime;
        if(deltaTime < 1000) {
            firstClick = new Point(e.getX(), e.getY());
        }
        lastClickedTime = currentTime;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(firstClick != null) {
            Point secondClick = new Point(e.getX(), e.getY());
            double xMod = fullSize.getWidth() / this.getWidth();
            double yMod = fullSize.getHeight() / this.getHeight();
            ScreenCap capper = new ScreenCap(
                    (int)(Math.min(firstClick.x.doubleValue(), secondClick.x.doubleValue()) * xMod),
                    (int)(Math.min(firstClick.y.doubleValue(), secondClick.y.doubleValue()) * yMod),
                    (int)(Math.max(firstClick.x.doubleValue(), secondClick.x.doubleValue()) * xMod),
                    (int)(Math.max(firstClick.y.doubleValue(), secondClick.y.doubleValue()) * yMod)
            );
            rtsp.setCapper(capper);
            firstClick = null;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
