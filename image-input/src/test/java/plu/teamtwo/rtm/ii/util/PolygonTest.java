package plu.teamtwo.rtm.ii.util;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class PolygonTest {

    Point t0 = new Point(-8, -8);
    Point t1 = new Point(0, 10);
    Point t2 = new Point(8, -8);

    @Test
    public void construct() {
        Polygon poly;

        poly = new Polygon(new Point[]{ t0, t1, t2 });
        assertEquals(t0, poly.points[0]);
        assertEquals(t1, poly.points[1]);
        assertEquals(t2, poly.points[2]);
        assertEquals(new Point(-8, -8), poly.min);
        assertEquals(new Point(8, 10), poly.max);

        ArrayList<Point> points = new ArrayList<>();
        points.add(t0);
        points.add(t1);
        points.add(t2);

        poly = new Polygon(points);
        assertEquals(t0, poly.points[0]);
        assertEquals(t1, poly.points[1]);
        assertEquals(t2, poly.points[2]);
        assertEquals(new Point(-8, -8), poly.min);
        assertEquals(new Point(8, 10), poly.max);

        try {
            poly = new Polygon(new Point[]{ t0, t2 });
            fail("Expected IllegalArgumentException when constructing Polygon with less than 3 points");
        } catch(IllegalArgumentException ex) {}
    }

    @Test
    public void contains() {
        Polygon triangle = new Polygon(new Point[]{ t0, t1, t2 });

        assertTrue(triangle.contains(new Point()));
        assertTrue(triangle.contains(t0));
        assertTrue(triangle.contains(t1));
        assertTrue(triangle.contains(t2));

        assertTrue(triangle.contains(new Point(-4, 1)));
        assertTrue(triangle.contains(new Point(4, 1)));
        assertTrue(triangle.contains(new Point(0, -8)));

        assertFalse(triangle.contains(new Point(5, 5)));
        assertFalse(triangle.contains(new Point(-5, 5)));
        assertFalse(triangle.contains(new Point(-20, -4)));
        assertFalse(triangle.contains(new Point(10, 10)));
    }

}