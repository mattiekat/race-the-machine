package plu.teamtwo.rtm.client.gui;

import plu.teamtwo.rtm.client.InputController;
import plu.teamtwo.rtm.ii.RTSProcessor;

import plu.teamtwo.rtm.core.util.Point;
import plu.teamtwo.rtm.core.util.Polygon;
import plu.teamtwo.rtm.ii.ScreenCap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel implements MouseListener, ActionListener {

    protected final RTSProcessor rtsp;
    protected BufferedImage img = null;
    protected List<Polygon> polygons = null;
    protected int score = 0;
    public static final Font FPS_FONT = new Font("Arial", Font.BOLD, 36);
    protected final Dimension fullSize;

    private Point firstClick = null;

    private final JPopupMenu contextMenu;
    private Point contextPosition = null;
    private final JMenuItem setStartClickMenuItem;
    private final JMenuItem setScoreBoxMenuItem;
    private final JMenuItem startMenuItem;

    private Point startButtonPosition = new Point();
    private boolean scoreBoxGrabbing = false;

    public ImagePanel(RTSProcessor rtsp) {
        this.rtsp = rtsp;
        DisplayMode dm = this.rtsp.getScreen().getDisplayMode();
        fullSize = new Dimension(dm.getWidth(), dm.getHeight());
        this.addMouseListener(this);

        // Create the context menu
        contextMenu = new JPopupMenu();
        setStartClickMenuItem = new JMenuItem("Set Start Button Position");
        setStartClickMenuItem.addActionListener(this);
        contextMenu.add(setStartClickMenuItem);
        setScoreBoxMenuItem = new JMenuItem("Select Score Box Position");
        setScoreBoxMenuItem.addActionListener(this);
        contextMenu.add(setScoreBoxMenuItem);
        contextMenu.addSeparator();
        startMenuItem = new JMenuItem("Start a Run");
        startMenuItem.addActionListener(this);
        contextMenu.add(startMenuItem);
    }

    public void setContents(BufferedImage img, List<Polygon> polygons, int score) {
        this.img = img;
        this.polygons = polygons;
        this.score = score;
    }

    @Override
    public Dimension getMinimumSize() {
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

        // Draw the Image
        if(img != null) g.drawImage(img, x, y, width, height, null);

        // Draw the Lines
        if(polygons != null) {
            float hue = 0.0f;
            ((Graphics2D)g).setStroke(new BasicStroke(2));
            for(Polygon poly : polygons) {
                hue += 0.01f;
                if(hue > 0.8f) hue = 0.0f;
                g.setColor(new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f)));
                int[] xvals = new int[poly.points.length];
                int[] yvals = new int[poly.points.length];
                for(int i = 0; i < poly.points.length; i++) {
                    xvals[i] = x + (int)(poly.points[i].x.doubleValue() * xMod);
                    yvals[i] = y + (int)(poly.points[i].y.doubleValue() * yMod);

                    /*
                    int i2 = (i+1) % poly.points.length;
                    g.drawLine(
                            x + (int)(poly.points[i2].x.doubleValue() * xMod),
                            y + (int)(poly.points[i2].y.doubleValue() * yMod),
                            x + (int)(poly.points[i] .x.doubleValue() * xMod),
                            y + (int)(poly.points[i] .y.doubleValue() * yMod));
                            */
                }
                g.fillPolygon(xvals, yvals, poly.points.length);
            }
        }

        g.setColor(Color.CYAN);
        g.fillOval(startButtonPosition.x.intValue()-10, startButtonPosition.y.intValue()-10, 20, 20);

        g.setFont(FPS_FONT);
        String fpsStr = ""+rtsp.getFPS() + ":" + (polygons == null ? -1 : polygons.size() + ":" + score + ":" + InputController.getInstance().isGameRunning());
        g.setColor(Color.BLACK);
        g.fillRect(6, 6, 50, 40);
        g.setColor(Color.YELLOW);
        g.drawString(fpsStr, 10, 40);

        if(firstClick != null) {
            java.awt.Point p = getMousePosition();
            if(p != null) {
                g.setColor(Color.MAGENTA);
                int xMin = Math.min(firstClick.x.intValue(), p.x);
                int yMin = Math.min(firstClick.y.intValue(), p.y);
                int xMax = Math.max(firstClick.x.intValue(), p.x);
                int yMax = Math.max(firstClick.y.intValue(), p.y);
                g.drawRect(xMin, yMin, xMax-xMin, yMax-yMin);
            }
        }
    }



    @Override
    public void mousePressed(MouseEvent e) {

        if( SwingUtilities.isRightMouseButton(e) ) {
            contextPosition = new Point(e.getX(), e.getY());
            contextMenu.show( e.getComponent(), e.getX(), e.getY() );
        }

        else if(e.getClickCount() == 2) {
            firstClick = new Point(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(firstClick != null) {
            Point secondClick = new Point(e.getX(), e.getY());
            double xMod = fullSize.getWidth() / this.getWidth();
            double yMod = fullSize.getHeight() / this.getHeight();
            int xMin = (int)(Math.min(firstClick.x.doubleValue(), secondClick.x.doubleValue()) * xMod);
            int yMin = (int)(Math.min(firstClick.y.doubleValue(), secondClick.y.doubleValue()) * yMod);
            int xMax = (int)(Math.max(firstClick.x.doubleValue(), secondClick.x.doubleValue()) * xMod);
            int yMax = (int)(Math.max(firstClick.y.doubleValue(), secondClick.y.doubleValue()) * yMod);
            if(scoreBoxGrabbing) {
                rtsp.setNumBounds(new Point(xMin, yMin), new Point(xMax, yMax));
            } else {
                ScreenCap capper = new ScreenCap(xMin, yMin, xMax-xMin, yMax-yMin);
                rtsp.setCapper(capper);
            }
            firstClick = null;
        }
    }

    @Override public void mouseClicked(MouseEvent e) { /* NOOP */ }
    @Override public void mouseEntered(MouseEvent e) { /* NOOP */ }
    @Override public void mouseExited(MouseEvent e) { /* NOOP */ }


    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == setStartClickMenuItem && contextPosition != null) {
            startButtonPosition = new Point(contextPosition);
            double xMod = fullSize.getWidth() / this.getWidth();
            double yMod = fullSize.getHeight() / this.getHeight();
            InputController.getInstance().setStartButtonPosition(new Point((int)(startButtonPosition.x.doubleValue()*xMod), (int)(startButtonPosition.y.doubleValue()*yMod)));
        }

        if(e.getSource() == setScoreBoxMenuItem) {
            scoreBoxGrabbing = true;
        }

        if(e.getSource() == startMenuItem) {
            InputController.getInstance().startGame();
            InputController.getInstance().setPressed(InputController.Key.LEFT, true);
            InputController.getInstance().updateInputs();
        }
    }
}
