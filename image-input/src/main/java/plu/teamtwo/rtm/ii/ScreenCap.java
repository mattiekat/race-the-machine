package plu.teamtwo.rtm.ii;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Screen Capture Class. Utility class designed to simplify the process of getting an image capture of the screen. Uses
 * <code>java.awt</code> methods and objects to produce a BufferedImage of the given section of screen on a screen
 * device.
 */
public class ScreenCap {

    private final Robot robot;
    private final Rectangle area;

    /**
     * Full Constructor. Creates a <code>ScreenCap</code> object around a given area on a specified <code>screen</code>.
     *
     * @param screen <code>GraphicsDevice screen</code> to capture from
     * @param x x-coordinate of capture area
     * @param y y-coordinate of capture area
     * @param width width of capture area
     * @param height height of capture area
     */
    public ScreenCap(GraphicsDevice screen, int x, int y, int width, int height) {
        try { robot = new Robot(screen); }
        catch(AWTException ex) { throw new RuntimeException(ex.getMessage()); }
        area = new Rectangle(x, y, width, height);
    }

    /**
     * Area Constructor. Creates a <code>ScreenCap</code> object around a given area on the primary screen.
     *
     * @param x x-coordinate of capture area
     * @param y y-coordinate of capture area
     * @param width width of capture area
     * @param height height of capture area
     */
    public ScreenCap(int x, int y, int width, int height) {
        try { robot = new Robot(); }
        catch(AWTException ex) { throw new RuntimeException(ex.getMessage()); }
        area = new Rectangle(x, y, width, height);
    }

    /**
     * Screen Constructor. Creates a fullscreen <code>ScreenCap</code> object on a specified <code>screen</code>.
     *
     * @param screen <code>GraphicsDevice screen</code> to capture from
     */
    public ScreenCap(GraphicsDevice screen) {
        try { robot = new Robot(screen); }
        catch(AWTException ex) { throw new RuntimeException(ex.getMessage()); }
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        area = new Rectangle(0, 0, dim.width, dim.height);
    }

    /**
     * Default Constructor. Creates a fullscreen <code>ScreenCap</code> object on the primary screen.
     */
    public ScreenCap() {
        try { robot = new Robot(); }
        catch(AWTException ex) { throw new RuntimeException(ex.getMessage()); }
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        area = new Rectangle(0, 0, dim.width, dim.height);
    }

    /**
     * Capture Method. Produces a <code>BufferedImage</code> representing the region of screen-space that this
     * <code>ScreenCap</code> object is setup to capture.
     *
     * @return <code>BufferedImage</code> screencap
     */
    public BufferedImage capture() {
        return robot.createScreenCapture(area);
    }
}
