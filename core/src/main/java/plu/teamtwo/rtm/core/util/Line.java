package plu.teamtwo.rtm.core.util;

/**
 * Line Utility Class. Used to represent a 2-dimensional lines comprised of two <code>Points</code>.
 */
public class Line {

    public final Point a;
    public final Point b;

    /**
     * Basic Constructor. Creates a new <code>Line</code> object out of two <code>Points</code>.
     *
     * @param a First <code>Point</code>
     * @param b Second <code>Point</code>
     */
    public Line(Point a, Point b) {
        this.a = a;
        this.b = b;
    }
}
