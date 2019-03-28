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
package org.isaacphysics.graphchecker.geometry;

import org.isaacphysics.graphchecker.data.IntersectionParams;
import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.data.PointOfInterest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     *
     * @param start The start point.
     * @param end The end point.
     */
    private Segment(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.side = null;
        this.openBothEnds = false;
    }

    /**
     * This is a half-line, from start, in the direction of end, with side considered the inside.
     *
     * @param start The start point.
     * @param end The end point (not an end, just a point to indicate direction).
     * @param side Which side is considered the inside of this segment.
     */
    private Segment(Point start, Point end, Side side) {
        this.start = start;
        this.end = end;
        this.side = side;
        this.openBothEnds = false;
    }

    /**
     * This is a half-line, from start, in the direction of end, with inside considered to be on the inside.
     *
     * @param start The start point.
     * @param end The end point (not an end, just a point to indicate direction).
     * @param inside A point which is to be considered as inside this segment.
     */
    private Segment(Point start, Point end, Point inside) {
        this.start = start;
        this.end = end;
        this.openBothEnds = false;
        this.side = Side.LEFT;
        if (!inside(inside)) {
            this.side = Side.RIGHT;
        }
    }

    /**
     * This is a line, through start and end.
     *
     * @param start The start point (not an end, just a point on the line defining this segment).
     * @param end The end point (not an end, just a point to indicate direction).
     * @param side Which side is considered the inside of this segment.
     * @param openBothEnds This must be true.
     */
    private Segment(Point start, Point end, Side side, boolean openBothEnds) {
        assert openBothEnds;
        this.start = start;
        this.end = end;
        this.side = side;
        this.openBothEnds = true;
    }

    /**
     * Get the start point of this segment.
     *
     * @return The start point.
     */
    public Point getStart() {
        return start;
    }

    /**
     * Get the end point of this segment.
     *
     * @return The end point.
     */
    public Point getEnd() {
        return end;
    }

    /**
     * Is this point on the inside of this line segment?
     *
     * Inside is defined as on the anti-clockwise side of the line segment and within the region defined by the tangents
     * to the line at start and end.
     *
     * @param p The point to classifyAll.
     * @return True if this point is on the inside of this line segment.
     */
    boolean inside(Point p) {
        Point endPrime = end.minus(start);
        Point pPrime = p.minus(start);
        if (!isOnInside(endPrime, pPrime)) {
            return false;
        }

         // Project originPoints onto line and check inside this segment
        double dotEndPrime = endPrime.getX() * endPrime.getX() + endPrime.getY() * endPrime.getY();
        double pDotEndPrime = pPrime.getX() * endPrime.getX() + pPrime.getY() * endPrime.getY();
        double coefficientOfSegment = pDotEndPrime / dotEndPrime;
        return (this.openBothEnds || coefficientOfSegment >= 0) && (this.side != null || coefficientOfSegment <= 1);
    }

    /**
     * Helper to check if a point is on the correct side of the line.
     *
     * prime suffix means the original point has had this.start subtracted from it.
     *
     * @param endPrime The end point minus this.start.
     * @param pPrime The point to be tested minus this.start.
     * @return True if the point is on the inside.
     */
    private boolean isOnInside(Point endPrime, Point pPrime) {
        double crossProduct = endPrime.getX() * pPrime.getY() - endPrime.getY() * pPrime.getX();
        if (this.side == null || this.side == Side.LEFT) {
            return crossProduct >= 0;
        } else {
            return crossProduct <= 0;
        }
    }

    /**
     * Test if there is an intersection between these two segments.
     *
     * @param s The segment to test against.
     * @return True if there is an intersection.
     */
    boolean intersects(Segment s) {
        IntersectionParams.IntersectionParam u = intersectionParam(s);
        return u != null;
    }

    /**
     * Get the parameters of any intersection between another segment and this one.
     * @param s The segment to be measured to see where it intersects this one.
     * @return The parameters of the intersection in terms of s, or null if no intersection occurs.
     */
    @Nullable
    public IntersectionParams.IntersectionParam intersectionParam(Segment s) {
        double x1 = this.start.getX();
        double x2 = this.end.getX();
        double x3 = s.start.getX();
        double x4 = s.end.getX();

        double y1 = this.start.getY();
        double y2 = this.end.getY();
        double y3 = s.start.getY();
        double y4 = s.end.getY();

        double det = (x4 - x3) * (y1 - y2) - (x1 - x2) * (y4 - y3);

        if (det == 0) {
            // Lines are parallel, so don't intersect
            return null;
        }

        double t = ((y3 - y4) * (x1 - x3) + (x4 - x3) * (y1 - y3)) / det;

        if ((!openBothEnds && t < 0) || (side == null && t > 1)) {
            return null;
        }

        double u = ((y1 - y2) * (x1 - x3) + (x2 - x1) * (y1 - y3)) / det;

        if ((!s.openBothEnds && u < 0) || (s.side == null && u > 1)) {
            return null;
        }

        Point endPrime = end.minus(start);
        Point pPrime = s.end.minus(start);
        boolean inside = isOnInside(endPrime, pPrime);

        return new IntersectionParams.IntersectionParam(u, inside);
    }

    /**
     * Clip a line against this segment.
     *
     * Any discontinuities are joined by straight lines. For example, clipping a sine wave against a segment
     * representing the x-axis would give a half-rectified wave.
     *
     * @param line The line to clip.
     * @return The new, clipped line.
     */
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

    /**
     * Clip a segment against this segment.
     * @param segment The segment to be clipped.
     * @return The clipped version of the segment, or null if it is fully outside this segment.
     */
    @Nullable
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

    /**
     * Get the point at parameter t on this segment.
     * @param t The parameter.
     * @return The point at that parameter.
     */
    Point atParameter(double t) {
        return new Point(
            start.getX() * (1 - t) + end.getX() * t,
            start.getY() * (1 - t) + end.getY() * t
        );
    }

    /**
     * Helper to create a closed segment between two points.
     *
     * @param start The start point.
     * @param end The end point.
     * @return A closed segment between those points.
     */
    public static Segment closed(Point start, Point end) {
        return new Segment(start, end);
    }

    /**
     * Helper to create a half-line from origin in a direction.
     *
     * @param origin The origin of the half-line.
     * @param direction The vector of the direction of the half-line.
     * @param side Which side of the half-line is considered inside.
     * @return The half-line.
     */
    static Segment openOneEnd(Point origin, Point direction, Side side) {
        return new Segment(origin, origin.add(direction), side);
    }

    /**
     * Helper to create a half-line from origin in a direction.
     *
     * @param origin The origin of the half-line.
     * @param direction The vector of the direction of the half-line.
     * @param inside A point to be considered as on the inside of the half-line.
     * @return The half-line.
     */
    static Segment openOneEnd(Point origin, Point direction, Point inside) {
        return new Segment(origin, origin.add(direction), origin.add(inside));
    }

    /**
     * Helper to create a line through origin in a direction.
     *
     * @param origin The origin of the line.
     * @param direction The vector of the direction of the line.
     * @param side Which side of the line is considered inside.
     * @return The line.
     */
    static Segment openBothEnds(Point origin, Point direction, Side side) {
        return new Segment(origin, origin.add(direction), side, true);
    }
}
