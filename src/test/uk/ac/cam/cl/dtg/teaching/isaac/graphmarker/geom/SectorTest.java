package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geom;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Intersection;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SectorTest {

    private void assertInside(Intersection c) {
        assertEquals(Intersection.INSIDE, c);
    }

    private void assertIntersects(Intersection c) {
        assertEquals(Intersection.INTERSECTS, c);
    }

    private void assertOutside(Intersection c) {
        assertEquals(Intersection.OUTSIDE, c);
    }

    private Line lineOf(Point... points) {
        return new Line(Arrays.asList(points));
    }

    private static final Point x1y1 = new Point(1, 1);
    private static final Point x_1y_1 = new Point(-1, -1);
    private static final Point x1y_1 = new Point(1, -1);
    private static final Point x_1y1 = new Point(-1, 1);
    private static final Point x0y0 = new Point(0, 0);
    private static final Point x0y1 = new Point(0, 1);
    private static final Point x0y_1 = new Point(0, -1);
    private static final Point x1y0 = new Point(1, 0);
    private static final Point x_1y0 = new Point(-1, 0);


    @Test
    public void topRightWorksOnPoint() {
        Sector topRight = Sector.topRight();

        assertTrue(topRight.contains(x1y1));
        assertFalse(topRight.contains(x_1y1));
        assertFalse(topRight.contains(x_1y_1));
        assertFalse(topRight.contains(x1y_1));
    }

    @Test
    public void topRightWorksOnLineFullyInside() {
        Sector topRight = Sector.topRight();

        assertInside(topRight.intersects(lineOf(x1y1, new Point(2, 1))));
    }

    @Test
    public void topRightWorksOnLineFromOrigin() {
        Sector topRight = Sector.topRight();

        assertIntersects(topRight.intersects(lineOf(x1y1, x0y0)));
    }

    @Test
    public void topRightWorksOnLineFromBottomLeft() {
        Sector topRight = Sector.topRight();

        assertIntersects(topRight.intersects(lineOf(x1y1, x_1y_1)));
    }

    @Test
    public void topRightWorksOnLineFromTopLeftToBottomRight() {
        Sector topRight = Sector.topRight();

        assertIntersects(topRight.intersects(lineOf(x_1y1, new Point(2, -1))));
    }

    @Test
    public void topLeftWorks() {
        Sector topLeft = Sector.topLeft();

        assertIntersects(topLeft.intersects(lineOf(x_1y1, x_1y_1)));
    }

    @Test
    public void bottomLeftWorks() {
        Sector bottomLeft = Sector.bottomLeft();

        assertInside(bottomLeft.intersects(lineOf(x_1y_1, new Point(-2, -1))));
    }

    @Test
    public void bottomRightWorks() {
        Sector bottomRight = Sector.bottomRight();

        assertIntersects(bottomRight.intersects(lineOf(x_1y_1, new Point(2, 1))));
    }


    @Test
    public void positiveXaxisWorks() {
        Sector axis = Sector.positiveXaxis();
        assertInside(axis.intersects(lineOf(new Point(0, 0.0001), x0y1)));

        assertIntersects(axis.intersects(lineOf(x_1y1, x1y1)));

        assertOutside(axis.intersects(lineOf(x_1y_1, x1y_1)));

        assertOutside(axis.intersects(lineOf(x_1y_1, new Point(0, -0.0001))));
    }

    @Test
    public void positiveXaxisDoesntContainPointsOutside() {
        Sector axis = Sector.positiveXaxis();

        assertFalse(axis.contains(x_1y_1));
        assertFalse(axis.contains(x0y_1));
        assertFalse(axis.contains(new Point(0, -0.0001)));
        assertFalse(axis.contains(x0y0));
    }

    @Test
    public void negativeXaxis() {
    }

    @Test
    public void positiveYaxis() {
    }

    @Test
    public void negativeYaxis() {
    }
}