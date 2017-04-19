package plu.teamtwo.rtm.core.util;

import java.util.Arrays;
import java.util.List;

/**
 * Polygon Utility Class. Used to represent a 2-dimensional shape comprised of multiple <code>Points</code>. Also
 * contains helper methods for data checking, such as determining if a Point lies inside a Polygon.
 */
public class Polygon {

    public final Point[] points;
    public final Point max;
    public final Point min;

    /**
     * Array Constructor. Copies the contents of the given <code>points</code> array to this <code>Polygon's</code>
     * internal data.
     *
     * @param points Array of <code>Points</code> to copy
     */
    public Polygon(Point[] points) {
        if(points.length < 3) throw new IllegalArgumentException("Polygon must be instantiated with at least 3 Points");
        this.points = Arrays.copyOf(points, points.length);
        double xMin = Double.POSITIVE_INFINITY, yMin = Double.POSITIVE_INFINITY,
                xMax = Double.NEGATIVE_INFINITY, yMax = Double.NEGATIVE_INFINITY;
        for(Point p : points) {
            double x = p.x.doubleValue(), y = p.y.doubleValue();
            if(x < xMin) xMin = x; if(x > xMax) xMax = x;
            if(y < yMin) yMin = y; if(y > yMax) yMax = y;
        }
        max = new Point(xMax, yMax);
        min = new Point(xMin, yMin);
    }

    /**
     * List Constructor. Turns the contents of the given <code>points List</code> into an array, and copies it to this
     * <code>Polygon's</code> internal data.
     *
     * @param points <code>List</code> of <code>Points</code> to copy
     */
    public Polygon(List<Point> points) {
        if(points.size() < 3) throw new IllegalArgumentException("Polygon must be instantiated with at least 3 Points");
        this.points = points.toArray(new Point[]{});
        double xMin = Double.POSITIVE_INFINITY, yMin = Double.POSITIVE_INFINITY,
                xMax = Double.NEGATIVE_INFINITY, yMax = Double.NEGATIVE_INFINITY;
        for(Point p : points) {
            double x = p.x.doubleValue(), y = p.y.doubleValue();
            if(x < xMin) xMin = x; if(x > xMax) xMax = x;
            if(y < yMin) yMin = y; if(y > yMax) yMax = y;
        }
        max = new Point(xMax, yMax);
        min = new Point(xMin, yMin);
    }

    /**
     * Determines if this <code>Polygon</code> contains a <code>Point</code>.
     *
     * @param point <code>Point</code> to check
     * @return <code>true</code> if the <code>Point</code> is inside this <code>Polygon</code>, <code>false</code> otherwise
     */
    public boolean contains(Point point) {
        if( Point.inBounds(min, max, point)) {

            Point extreme = new Point(Double.POSITIVE_INFINITY, point.y);

            int count = 0;
            for(int i = 0; i < points.length; i++) {

                int next = (i+1) % points.length;
                if(Point.lineIntersect(points[i], points[next], point, extreme)) {
                    if(Point.orientation(points[i], point, points[next]) == Point.Orientation.COLINEAR)
                        return Point.inBounds(points[i], points[next], point);
                    else count++;
                }
            }

            return count % 2 == 1;
        } else return false;
    }
}
