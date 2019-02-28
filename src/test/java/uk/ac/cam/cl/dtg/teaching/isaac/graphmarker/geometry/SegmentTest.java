package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.TestHelpers.lineOf;

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
    public void pointOnVerticalLineIsInside() {
        Point p = new Point(0, 0.5);
        Segment s = Segment.closed(origin, new Point(0, 1));

        assertTrue(s.inside(p));
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

        assertTrue(s.intersects(t));
    }

    @Test
    public void overlapIsHandledRight() {
        Segment above = Segment.closed(new Point(-1, 0), new Point(1, 0));
        Segment below = Segment.closed(new Point(1, 0), new Point(-1, 0));

        Point a = new Point(0, 0);
        Point b = new Point(0, 1);
        Segment up = Segment.closed(a, b);

        assertTrue(below.inside(a));
        assertFalse(below.inside(b));
        assertTrue(below.intersects(up));

        assertTrue(above.inside(a));
        assertTrue(above.inside(b));
        assertTrue(above.intersects(up));
    }

    @Test
    public void atParameter() {
        Segment segment = Segment.closed(new Point(1, 0), new Point(0, 1));

        Point p = segment.atParameter(0.25);

        assertThat(p.getX(), closeTo(0.75, 0.001));
        assertThat(p.getY(), closeTo(0.25, 0.001));
    }

    @Test
    public void clip() {
        Segment segment = Segment.closed(new Point(-1, -1), new Point(1, 1));

        Segment clipSegment = Segment.openBothEnds(new Point(0, 0), new Point(0, 1), Side.LEFT);

        Segment clippedSegment = clipSegment.clip(segment);

        assertThat(clippedSegment.getStart().getX(), closeTo(-1, 0.001));
        assertThat(clippedSegment.getStart().getY(), closeTo(-1, 0.001));

        assertThat(clippedSegment.getEnd().getX(), closeTo(0, 0.001));
        assertThat(clippedSegment.getEnd().getY(), closeTo(0, 0.001));
    }

    @Test
    public void clipLine() {
        Line expectedLine = lineOf(new Point(-1, -1), new Point(0, 0), new Point(2, 0), new Point(3, -1));
        Line line = lineOf(new Point(-1, -1), new Point(1, 1), new Point(3, -1));

        Segment clipSegment = Segment.openBothEnds(new Point(0, 0), new Point(-1, 0), Side.LEFT);

        Line clippedLine = clipSegment.clip(line);

        assertEquals(4, clippedLine.getPoints().size());
        assertEquals(expectedLine, clippedLine);
    }

    @Test
    public void clipOutsideReturnsEmptyLine() {
        Line line = lineOf(new Point(-1, -1), new Point(1, 1), new Point(3, -1));

        Segment clipSegment = Segment.openBothEnds(new Point(0, 2), new Point(1, 0), Side.LEFT);

        Line clippedLine = clipSegment.clip(line);

        assertEquals(0, clippedLine.getPoints().size());
    }

    @Test
    public void clipOnEdgeReturnsSinglePoint() {
        Line line = lineOf(new Point(-1, -1), new Point(1, 1), new Point(3, -1));

        Segment clipSegment = Segment.openBothEnds(new Point(0, 1), new Point(1, 0), Side.LEFT);

        Line clippedLine = clipSegment.clip(line);

        assertEquals(1, clippedLine.getPoints().size());
    }
}