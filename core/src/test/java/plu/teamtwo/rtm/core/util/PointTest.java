package plu.teamtwo.rtm.core.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PointTest {

    Point int1 = new Point(1, 2);
    Point int2 = new Point(-13, 6);
    Point int3 = new Point(101, 42);

    Point float1 = new Point(0.5f, 3.2f);
    Point float2 = new Point(14.0f, -5.8f);
    Point float3 = new Point(999.999f, -333.333f);
    
    Point double1 = new Point(-13.0, 6.0);
    Point double2 = new Point(999.999, -333.333);
    Point double3 = new Point(42.42, -4.0);

    Point rect1 = new Point(-10, -10);
    Point rect2 = new Point(-8, 10);
    Point rect3 = new Point(10, 10);
    Point rect4 = new Point(8, -10);


    @Test
    public void testEqualsInt() {
        assertEquals(new Point(), new Point(0, 0));
        assertEquals(new Point(1, 2), int1);
        assertEquals(new Point(-13, 6), int2);
        assertEquals(new Point(101, 42), int3);
        assertNotEquals(new Point(), int1);
        assertNotEquals(new Point(), int2);
        assertNotEquals(new Point(), int3);
    }

    @Test
    public void testEqualsFloat() {
        assertEquals(new Point(), new Point(0.0f, 0.0f));
        assertEquals(new Point(0.5f, 3.2f), float1);
        assertEquals(new Point(14.0f, -5.8f), float2);
        assertEquals(new Point(999.999f, -333.333f), float3);
        assertNotEquals(new Point(), float1);
        assertNotEquals(new Point(), float2);
        assertNotEquals(new Point(), float3);
    }

    @Test
    public void testEqualsDouble() {
        assertEquals(new Point(), new Point(0.0, 0.0));
        assertEquals(new Point(-13.0, 6.0), double1);
        assertEquals(new Point(999.999, -333.333), double2);
        assertEquals(new Point(42.42, -4.0), double3);
        assertNotEquals(new Point(), double1);
        assertNotEquals(new Point(), double2);
        assertNotEquals(new Point(), double3);
    }

    @Test
    public void testEqualsMixed() {
        assertNotEquals(int1, float1);
        assertNotEquals(int1, double3);
        assertEquals(int2, double1);
        assertNotEquals(int2, float1);
        assertNotEquals(int3, float2);
        assertNotEquals(int3, float3);
        assertNotEquals(float1, int3);
        assertNotEquals(float1, double1);
        assertNotEquals(float1, double2);
        assertNotEquals(float2, double2);
        assertNotEquals(float2, double3);
        assertNotEquals(float3, double3);
        assertEquals(float3, double2);
        assertNotEquals(double1, int1);
        assertNotEquals(double2, int2);
        assertNotEquals(double3, int3);
    }

    @Test
    public void addInt() {
        Point a = new Point(2, 3);
        Point b = new Point(3, 2);
        assertEquals(new Point(5, 5), a.add(b));
        Point c = new Point(-10, -10);
        assertEquals(new Point(-8, -7), a.add(c));
    }

    @Test
    public void subInt() {
        Point a = new Point(2, 3);
        Point b = new Point(3, 2);
        assertEquals(new Point(-1, 1), a.subtract(b));
        Point c = new Point(-10, -10);
        assertEquals(new Point(12, 13), a.subtract(c));
    }

    @Test
    public void multInt() {
        Point a = new Point(2, 3);
        Point b = new Point(3, 2);
        assertEquals(new Point(6, 6), a.multiply(b));
        Point c = new Point(-10, -10);
        assertEquals(new Point(-20, -30), a.multiply(c));
    }

    @Test
    public void divideInt() {
        Point a = new Point(100, 1000);
        Point b = new Point(4, 4);
        assertEquals(new Point(25, 250), a.divide(b));
        Point c = new Point(-10, -10);
        assertEquals(new Point(-10, -100), a.divide(c));
    }

    @Test
    public void bounds() {
        assertFalse(Point.inBounds(rect1, rect2, rect3));
        assertFalse(Point.inBounds(rect1, rect2, rect4));
        assertTrue(Point.inBounds(rect1, rect3, rect2));
        assertTrue(Point.inBounds(rect1, rect3, rect4));
        assertFalse(Point.inBounds(rect1, rect2, new Point()));
        assertTrue(Point.inBounds(rect1, rect3, new Point()));
    }

    @Test
    public void orientation() {
        assertEquals(Point.Orientation.CLOCKWISE, Point.orientation(rect1, rect2, rect3));
        assertEquals(Point.Orientation.CLOCKWISE, Point.orientation(rect3, rect4, rect1));
        assertEquals(Point.Orientation.COUNTER_CLOCKWISE, Point.orientation(rect2, rect1, rect4));
        assertEquals(Point.Orientation.COUNTER_CLOCKWISE, Point.orientation(rect4, rect3, rect2));
        assertEquals(Point.Orientation.COLINEAR, Point.orientation(rect1, new Point(), rect3));
        assertEquals(Point.Orientation.COLINEAR, Point.orientation(rect2, new Point(), rect4));
    }

    @Test
    public void intersect() {
        assertFalse(Point.lineIntersect(rect1, rect2, rect3, rect4));
        assertFalse(Point.lineIntersect(rect3, rect2, rect1, rect4));
        assertTrue(Point.lineIntersect(rect1, rect3, rect2, rect4));
        assertTrue(Point.lineIntersect(rect1, new Point(), rect2, new Point()));
    }
}