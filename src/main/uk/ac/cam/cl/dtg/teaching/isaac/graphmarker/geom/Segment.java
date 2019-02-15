package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geom;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

/**
 * Represents a line segment.
 */
class Segment {
    private final Point start;
    private final Point end;

    public Segment(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Inside is defined as on the anti-clockwise side of the line segment and within the region defined by the tangents to the line at start and end.
     */
    public boolean inside(Point p) {
        Point endPrime = end.minus(start);
        Point pPrime = p.minus(start);
        double crossProduct = endPrime.getX() * pPrime.getY() - endPrime.getY() * pPrime.getX();
        if (crossProduct > 0) {
            // Project p onto line and check inside this segment
            double dotEndPrime = endPrime.getX() * endPrime.getX() + endPrime.getY() * endPrime.getY();
            double pDotEndPrime = pPrime.getX() * endPrime.getX() + pPrime.getY() * endPrime.getY();
            double coefficientOfSegment = pDotEndPrime / dotEndPrime;
            return coefficientOfSegment >= 0 && coefficientOfSegment <= 1;
        } else {
            return false;
        }
    }

    public boolean intersects(Segment s) {
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
            return false;
        }

        double t = ((y3-y4) * (x1-x3) + (x4-x3) * (y1-y3)) / det;
        double u = ((y1-y2) * (x1-x3) + (x2-x1) * (y1-y3)) / det;

        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }
}
