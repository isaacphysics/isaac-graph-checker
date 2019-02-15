package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geom;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import static org.junit.Assert.*;

public class SegmentTest {

    private static final Point origin = new Point(0, 0);

    @Test
    public void pointOnLeftOfVerticalLineIsInside() {
        Point p = new Point(-1, 0.5);
        Segment s = Segment.closed(origin, new Point(0, 1));

        assertTrue(s.inside(p));
    }

    @Test
    public void pointOnRightOfVerticalLineIsOutside() {
        Point p = new Point(1, 0.5);
        Segment s = Segment.closed(origin, new Point(0, 1));

        assertFalse(s.inside(p));
    }

    @Test
    public void pointOnVerticalLineIsOutside() {
        Point p = new Point(0, 0.5);
        Segment s = Segment.closed(origin, new Point(0, 1));

        assertFalse(s.inside(p));
    }

    @Test
    public void pointAboveVerticalLineSegmentIsOutside() {
        Point p = new Point(-1, 2);
        Segment s = Segment.closed(origin, new Point(0, 1));

        assertFalse(s.inside(p));
    }

    @Test
    public void pointAboveVerticalHalfLineIsInside() {
        Point p = new Point(-1, 2);
        Segment s = Segment.openOneEnd(origin, new Point(0, 1), Side.LEFT);

        assertTrue(s.inside(p));
    }

    @Test
    public void pointBelowVerticalLineIsInside() {
        Point p = new Point(-1, -2);
        Segment s = Segment.openBothEnds(origin, new Point(0, 1), Side.LEFT);

        assertTrue(s.inside(p));
    }

    @Test
    public void simpleCrossIntersects() {
        Segment s = Segment.closed(origin, new Point(1, 1));
        Segment t = Segment.closed(new Point(1, 0), new Point(0, 1));

        assertTrue(s.intersects(t));
    }

    @Test
    public void parallelLinesDontIntersect() {
        Segment s = Segment.closed(origin, new Point(1, 1));
        Segment t = Segment.closed(new Point(0, 1), new Point(1, 2));

        assertFalse(s.intersects(t));
    }

    @Test
    public void segmentsThatWouldIntersectAsLinesDont() {
        Segment s = Segment.closed(new Point(1, 1), new Point(2, 2));
        Segment t = Segment.closed(new Point(-1, 1), new Point(-2, 2));

        assertFalse(s.intersects(t));
    }

    @Test
    public void linesIntersect() {
        Segment s = Segment.openBothEnds(new Point(1, 1), new Point(1, 1), Side.LEFT);
        Segment t = Segment.openBothEnds(new Point(-1, 1), new Point(-1, 1), Side.LEFT);

        assertFalse(s.intersects(t));
    }
}