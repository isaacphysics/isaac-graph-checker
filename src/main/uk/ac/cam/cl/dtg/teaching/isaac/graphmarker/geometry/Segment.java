package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Side.LEFT;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Side.RIGHT;

/**
 * Represents a line segment.
 *
 * http://www.cs.swan.ac.uk/~cssimon/line_intersection.html was a good reference.
 */
public class Segment {
    private final Point start;
    private final Point end;
    private Side side;
    private final boolean openBothEnds;

    /**
     * This is a segment, starting and ending at start and end respectively.
     */
    private Segment(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.side = null;
        this.openBothEnds = false;
    }

    /**
     * This is a half-line, from start, in the direction of end, with side considered the inside.
     */
    private Segment(Point start, Point end, Side side) {
        this.start = start;
        this.end = end;
        this.side = side;
        this.openBothEnds = false;
    }

    /**
     * This is a half-line, from start, in the direction of end, with inside considered the inside.
     */
    private Segment(Point start, Point end, Point inside) {
        this.start = start;
        this.end = end;
        this.openBothEnds = false;
        this.side = LEFT;
        if (!inside(inside)) this.side = RIGHT;
    }

    /**
     * This is a line, through start and end, with side considered the inside.
     */
    private Segment(Point start, Point end, Side side, boolean openBothEnds) {
        assert openBothEnds;
        this.start = start;
        this.end = end;
        this.side = side;
        this.openBothEnds = true;
    }

    /**
     * Inside is defined as on the anti-clockwise side of the line segment and within the region defined by the tangents to the line at start and end.
     */
     boolean inside(Point p) {
        Point endPrime = end.minus(start);
        Point pPrime = p.minus(start);
         if (!isOnInside(endPrime, pPrime)) return false;

         // Project p onto line and check inside this segment
        double dotEndPrime = endPrime.getX() * endPrime.getX() + endPrime.getY() * endPrime.getY();
        double pDotEndPrime = pPrime.getX() * endPrime.getX() + pPrime.getY() * endPrime.getY();
        double coefficientOfSegment = pDotEndPrime / dotEndPrime;
        return (this.openBothEnds || coefficientOfSegment >= 0) && (this.side != null || coefficientOfSegment <= 1);
    }

    private boolean isOnInside(Point endPrime, Point pPrime) {
        double crossProduct = endPrime.getX() * pPrime.getY() - endPrime.getY() * pPrime.getX();
        if (this.side == null || this.side == LEFT) {
            return crossProduct >= 0;
        } else {
            return crossProduct <= 0;
        }
    }

    boolean intersects(Segment s) {
        IntersectionParams.IntersectionParam u = intersectionParam(s);
        return u != null;
    }

    public IntersectionParams.IntersectionParam intersectionParam(Segment s) {
        double x1 = this.start.getX();
        double x2 = this.end.getX();
        double x3 = s.start.getX();
        double x4 = s.end.getX();

        double y1 = this.start.getY();
        double y2 = this.end.getY();
        double y3 = s.start.getY();
        double y4 = s.end.getY();

        double det = (x4-x3) * (y1-y2) - (x1-x2) * (y4-y3);

        if (det == 0) {
            // Lines are parallel, so don't intersect
            return null;
        }

        double t = ((y3-y4) * (x1-x3) + (x4-x3) * (y1-y3)) / det;

        if (!openBothEnds && t < 0) return null;
        if (side == null && t > 1) return null;

        double u = ((y1-y2) * (x1-x3) + (x2-x1) * (y1-y3)) / det;

        if (!s.openBothEnds && u < 0) return null;
        if (s.side == null && u > 1) return null;

        Point endPrime = end.minus(start);
        Point pPrime = s.end.minus(start);
        boolean inside = isOnInside(endPrime, pPrime);

        return new IntersectionParams.IntersectionParam(u, inside);
    }

    public static Segment closed(Point start, Point end) {
        return new Segment(start, end);
    }

    static Segment openOneEnd(Point origin, Point direction, Side side) {
        return new Segment(origin, origin.add(direction), side);
    }

    static Segment openOneEnd(Point origin, Point direction, Point inside) {
        return new Segment(origin, origin.add(direction), origin.add(inside));
    }

    static Segment openBothEnds(Point origin, Point direction, Side side) {
        return new Segment(origin, origin.add(direction), side, true);
    }

    public Point getStart() {
        return start;
    }
}
