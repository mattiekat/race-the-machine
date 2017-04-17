package plu.teamtwo.rtm.core.util;

/**
 * Point utility class. Used to represent a 2-dimensional point as a data structure that can be passed around. Contains
 * helper methods for point manipulation, such as arithmetic operations.
 */
public class Point implements Comparable<Point> {

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

}
