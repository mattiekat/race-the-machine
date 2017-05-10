package plu.teamtwo.rtm.core.util;

/**
 * Point utility class. Used to represent a 2-dimensional point as a data structure that can be passed around. Contains
 * helper methods for point manipulation, such as arithmetic operations.
 */
public class Point implements Comparable<Point> {

    /**
     * Epsilon value used for comparison calculations.
     */
    public static final double EPSILON = 0.0001;

    /**
     * <code>x</code> coordinate of the Point.
     */
    public final Number x;

    /**
     * <code>y</code> coordinate of the Point.
     */
    public final Number y;

    /**
     * Blank Constructor. Creates a new <code>Point</code> that represents the origin, or <code>(0, 0)</code>.
     */
    public Point() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Default Constructor. Creates a new <code>Point</code> with the given <code>x</code> and <code>y</code> coordinates.
     *
     * @param x <code>x</code> coordinate
     * @param y <code>y</code> coordinate
     */
    public Point(Number x, Number y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy Constructor. Creates a new <code>Point</code> with the same coordinates as the given <code>Point</code>.
     *
     * @param point <code>Point</code> object to copy
     */
    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    /**
     * Static Arithmetic Addition. Adds two <code>Points</code> and returns the result.
     *
     * @param lhs left-hand <code>Point</code> to add
     * @param rhs right-hand <code>Point</code> to add
     * @return sum of both <code>Points</code>
     */
    public static Point add(Point lhs, Point rhs) {
        return new Point(lhs.x.doubleValue() + rhs.x.doubleValue(), lhs.y.doubleValue() + rhs.y.doubleValue());
    }

    /**
     * Static Arithmetic Subtraction. Subtracts two <code>Points</code> and returns the result.
     *
     * @param lhs left-hand <code>Point</code> to subtract
     * @param rhs right-hand <code>Point</code> to subtract
     * @return difference of both <code>Points</code>
     */
    public static Point subtract(Point lhs, Point rhs) {
        return new Point(lhs.x.doubleValue() - rhs.x.doubleValue(), lhs.y.doubleValue() - rhs.y.doubleValue());
    }

    /**
     * Static Arithmetic Multiplication. Multiplies two <code>Points</code> and returns the result.
     *
     * @param lhs left-hand <code>Point</code> to multiply
     * @param rhs right-hand <code>Point</code> to multiply
     * @return product of both <code>Points</code>
     */
    public static Point multiply(Point lhs, Point rhs) {
        return new Point(lhs.x.doubleValue() * rhs.x.doubleValue(), lhs.y.doubleValue() * rhs.y.doubleValue());
    }

    /**
     * Static Arithmetic Division. Divides two <code>Points</code> and returns the result.
     *
     * @param lhs left-hand <code>Point</code> to divide
     * @param rhs right-hand <code>Point</code> to divide
     * @return quotient of both <code>Points</code>
     */
    public static Point divide(Point lhs, Point rhs) {
        return new Point(lhs.x.doubleValue() / rhs.x.doubleValue(), lhs.y.doubleValue() / rhs.y.doubleValue());
    }

    /**
     * Static Arithmetic Modulus. Divides two <code>Points</code> and returns the remainder.
     *
     * @param lhs left-hand <code>Point</code> to divide
     * @param rhs right-hand <code>Point</code> to divide
     * @return modulus of both <code>Points</code>
     */
    public static Point modulus(Point lhs, Point rhs) {
        return new Point(lhs.x.doubleValue() % rhs.x.doubleValue(), lhs.y.doubleValue() % rhs.y.doubleValue());
    }

    /**
     * Arithmetic Addition. Adds another <code>Point</code> with this <code>Point</code>, and returns the result.
     *
     * @param other <code>Point</code> to add
     * @return sum of both <code>Points</code>
     */
    public Point add(Point other) {
        return Point.add(this, other);
    }

    /**
     * Arithmetic Subtraction. Subtracts another <code>Point</code> from this <code>Point</code>, and returns the result.
     *
     * @param other <code>Point</code> to subtract
     * @return difference of both <code>Points</code>
     */
    public Point subtract(Point other) {
        return Point.subtract(this, other);
    }

    /**
     * Arithmetic Multiplication. Multiplies another <code>Point</code> with this <code>Point</code>, and returns the result.
     *
     * @param other <code>Point</code> to multiply
     * @return product of both <code>Points</code>
     */
    public Point multiply(Point other) {
        return Point.multiply(this, other);
    }

    /**
     * Arithmetic Division. Divides this <code>Point</code> by another <code>Point</code>, and returns the result.
     *
     * @param other <code>Point</code> to divide with
     * @return quotient of both <code>Points</code>
     */
    public Point divide(Point other) {
        return Point.divide(this, other);
    }

    /**
     * Arithmetic Modulus. Divides this <code>Point</code> by another <code>Point</code>, and returns the remainder.
     *
     * @param other <code>Point</code> to divide with
     * @return modulus of both <code>Points</code>
     */
    public Point modulus(Point other) {
        return Point.modulus(this, other);
    }

    @Override
    public boolean equals(Object other) {
        final double epsilon = 0.0001;
        if(other instanceof Point) {
            double diffX = Math.abs(x.doubleValue() - ((Point) other).x.doubleValue());
            double diffY = Math.abs(y.doubleValue() - ((Point) other).y.doubleValue());
            return diffX < epsilon && diffY < epsilon;
        } else return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(Point other) {
        if(x instanceof Comparable && y instanceof Comparable && other.x instanceof Comparable && other.y instanceof Comparable) {
            int c = ((Comparable)x).compareTo(other.x);
            if(c == 0) return ((Comparable)y).compareTo(other.y);
            else return c;
        } else throw new RuntimeException("Point not comparable to other Point because an x and/or y value did not implement comparable");
    }

    @Override
    public String toString() {
        return "(" + x.toString() + ", " + y.toString() + ")";
    }


    /**
     * Determines if a <code>Point</code> is between two others. Creates a bounding box with <code>Points</code>
     * <code>boundsA</code> and <code>boundsB</code> as the two opposite corners, and checks to see if <code>point</code> lies
     * within said bounding box.
     *
     * @param boundsA First <code>Point</code> of the bounding box
     * @param boundsB Second <code>Point</code> of the bounding box
     * @param point <code>Point</code> to check
     * @return <code>true</code> if <code>point</code> is within the bounding box, <code>false</code> otherwise
     */
    public static boolean inBounds(Point boundsA, Point boundsB, Point point) {
        return
                   ( point.x.doubleValue() >= Math.min(boundsA.x.doubleValue(), boundsB.x.doubleValue()) - EPSILON )
                && ( point.x.doubleValue() <= Math.max(boundsA.x.doubleValue(), boundsB.x.doubleValue()) + EPSILON )
                && ( point.y.doubleValue() >= Math.min(boundsA.y.doubleValue(), boundsB.y.doubleValue()) - EPSILON)
                && ( point.y.doubleValue() <= Math.max(boundsA.y.doubleValue(), boundsB.y.doubleValue()) + EPSILON );
    }

    /**
     * An enum representing the orientation of a triplet of <code>Points</code>. Colinear means the points fall in a
     * line.
     */
    enum Orientation {
        CLOCKWISE,
        COUNTER_CLOCKWISE,
        COLINEAR
    }

    /**
     * Determines the <code>Orientation</code> of a triplet of <code>Points</code>.
     *
     * @param p First <code>Point</code>
     * @param q Second <code>Point</code>
     * @param r Third <code>Point</code>
     * @return <code>Orientation</code> enum based on the given <code>Points'</code> positions.
     */
    public static Orientation orientation(Point p, Point q, Point r) {

        // Get Our Parts
        double t0 = q.y.doubleValue() - p.y.doubleValue();
        double t1 = r.x.doubleValue() - q.x.doubleValue();
        double t2 = q.x.doubleValue() - p.x.doubleValue();
        double t3 = r.y.doubleValue() - q.y.doubleValue();

        // Avoid multiplying Infinity with 0
        double val1, val2;
        if(t0 == 0.0 || t1 == 0.0) val1 = 0.0;
        else val1 = t0 * t1;
        if(t2 == 0.0 || t3 == 0.0) val2 = 0.0;
        else val2 = t2 * t3;

        // Subtracting an infinity from itself results in NaN
        if(val1 == Double.POSITIVE_INFINITY && val2 == Double.POSITIVE_INFINITY) return Orientation.COLINEAR;
        if(val1 == Double.NEGATIVE_INFINITY && val2 == Double.NEGATIVE_INFINITY) return Orientation.COLINEAR;

        // Final Check
        double val = val1 - val2;
        if(Math.abs(val) < EPSILON) return Orientation.COLINEAR;
        else return val > 0.0 ? Orientation.CLOCKWISE : Orientation.COUNTER_CLOCKWISE;
    }

    /**
     * Determines if two lines intersect each other. Calculates if two lines, given by <code>p1q1</code> and
     * <code>p2q2</code> intersect each other.
     *
     * @param p1 First <code>Point</code> of line 1
     * @param q1 Second <code>Point</code> of line 1
     * @param p2 First <code>Point</code> of line 2
     * @param q2 Second <code>Point</code> of line 2
     * @return <code>true</code> if the two lines intersect, <code>false</code> otherwise
     */
    public static boolean lineIntersect(Point p1, Point q1, Point p2, Point q2) {

        // Find the four Orientations needed for calculations
        Orientation o1 = orientation(p1, q1, p2);
        Orientation o2 = orientation(p1, q1, q2);
        Orientation o3 = orientation(p2, q2, p1);
        Orientation o4 = orientation(p2, q2, q1);

        // General Case
        if( o1 != o2 && o3 != o4 )
            return true;

        // p1, q1, and p2 are colinear and p2 is between points p1 and q1
        if( o1 == Orientation.COLINEAR && inBounds(p1, q1, p2))
            return true;

        // p1, q1, and q2 are colinear and q2 is between points p1 and q1
        if( o2 == Orientation.COLINEAR && inBounds(p1, q1, q2))
            return true;

        // p2, q2, and p1 are colinear and p1 is between points p2 and q2
        if( o3 == Orientation.COLINEAR && inBounds(p2, q2, p1))
            return true;

        // p2, q2, and q1 are colinear and q1 is between points p2 and q2
        if( o4 == Orientation.COLINEAR && inBounds(p2, q2, q1))
            return true;

        return false;
    }

}
