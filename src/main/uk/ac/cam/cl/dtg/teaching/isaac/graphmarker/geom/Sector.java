package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geom;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Intersection;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a region of the graph, e.g. above the x axis, or on the y axis with y < 0
 */
public class Sector {

    private static final double FAR_AWAY = 1000;
    private static final double SLOP = 0.01;

    private final List<Segment> segments;

    private Sector(List<Segment> segments) {
        this.segments = segments;
    }

    public boolean contains(Point p) {
        return this.segments.stream().allMatch(segment -> segment.inside(p));
    }

    private boolean intersects(Segment s) {
        return this.segments.stream().anyMatch(segment -> segment.intersects(s));
    }

    public Intersection intersects(Line line) {
        boolean allInside = true;
        boolean someInside = false;
        boolean anyIntersections = false;
        Point lastPoint = null;
        for (Point point : line) {
            if (contains(point)) {
                someInside = true;
            } else {
                allInside = false;
            }
            if (lastPoint != null) {
                anyIntersections |= intersects(Segment.closed(lastPoint, point));
            }
            lastPoint = point;
        }
        return  allInside && !anyIntersections ? Intersection.INSIDE
                : someInside || anyIntersections ? Intersection.INTERSECTS
                : Intersection.OUTSIDE;
    }

    /*public boolean whollyContains(Line line) {
        return false;
    }*/

    private static Sector quadrant(Point origin, Point axis1, Point axis2) {
        return new Sector(Arrays.asList(
            Segment.openOneEnd(origin, axis1, axis2),
            Segment.openOneEnd(origin, axis2, axis1)
        ));
    }

    private static Sector centeredQuadrant(Point axis1, Point axis2) {
        Point origin = new Point(0, 0);
        return quadrant(origin, axis1, axis2);
    }

    private static Sector axialQuadrant(boolean xPositive, boolean yPositive) {
        return centeredQuadrant(new Point(xPositive ? 1 : -1, 0), new Point(0, yPositive ? 1 : -1));
    }

    static Sector topRight() {
        return centeredQuadrant(new Point(0, 1), new Point(1, 0));
    }

    static Sector topLeft() {
        return axialQuadrant(false, true);
    }

    static Sector bottomLeft() {
        return axialQuadrant(false, false);
    }

    static Sector bottomRight() {
        return axialQuadrant(true, true);
    }

    private static Sector sloppyAxis(Point left, Point right, Point axis) {
        return new Sector(Arrays.asList(
                Segment.closed(left, right),
                Segment.openOneEnd(left, axis, Side.RIGHT),
                Segment.openOneEnd(right, axis, Side.LEFT)
        ));
    }

    static Sector onAxisWithPositiveY() {
        return sloppyAxis(new Point(-SLOP, 0), new Point(SLOP, 0), new Point(0, 1));
    }

    static Sector onAxisWithNegativeY() {
        return sloppyAxis(new Point(SLOP, 0), new Point(-SLOP, 0), new Point(0, -1));
    }

    static Sector onAxisWithPositiveX() {
        return sloppyAxis(new Point(0, SLOP), new Point(0, -SLOP), new Point(1, 0));
    }

    static Sector onAxisWithNegativeX() {
        return sloppyAxis(new Point(0, -SLOP), new Point(0, SLOP), new Point(-1, 0));
    }
}
