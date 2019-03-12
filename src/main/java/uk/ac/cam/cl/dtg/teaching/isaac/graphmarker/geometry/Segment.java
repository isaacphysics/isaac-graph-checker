/**
 * Copyright 2019 University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointOfInterest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

         // Project originPoints onto line and check inside this segment
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

    public Line clip(Line line) {
        List<Point> points = new ArrayList<>();
        Point lastPoint = null;
        for (Point point : line) {
            if (lastPoint != null) {
                Segment lineSegment = Segment.closed(lastPoint, point);
                Segment clippedSegment = this.clip(lineSegment);
                if (clippedSegment != null) {
                    if (points.isEmpty() || !points.get(points.size() - 1).equals(clippedSegment.start)) {
                        points.add(clippedSegment.start);
                    }
                    if (points.isEmpty() || !points.get(points.size() - 1).equals(clippedSegment.end)) {
                        points.add(clippedSegment.end);
                    }
                }
            }
            lastPoint = point;
        }

        // CHECKME: Once clipped, these might not be maxima/minima any more
        List<PointOfInterest> pointsOfInterest = line.getPointsOfInterest().stream()
            .filter(this::inside)
            .collect(Collectors.toList());

        return new Line(points, pointsOfInterest);
    }

    Segment clip(Segment segment) {
         IntersectionParams.IntersectionParam intersectionParam = intersectionParam(segment);
         if (intersectionParam == null) {
             if (inside(segment.start)) {
                 return segment;
             } else {
                 return null;
             }
         }

         Point p = segment.atParameter(intersectionParam.getT());

         if (intersectionParam.isInside()) {
             if (inside(segment.start)) {
                 // Start is inside too, so don't clip
                 return segment;
             } else {
                 // End is inside, so clip start point
                 return Segment.closed(p, segment.end);
             }
         } else {
             return Segment.closed(segment.start, p);
         }
    }

    Point atParameter(double t) {
         return new Point(
             start.getX() * (1 - t) + end.getX() * t,
             start.getY() * (1 - t) + end.getY() * t
         );
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

    public Point getEnd() {
        return end;
    }
}
