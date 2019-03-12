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

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Intersection;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a region of the graph, e.g. above the x axis, or on the y axis with y < 0
 */
@SuppressWarnings("checkstyle:constantName")
public class Sector {

    public static final double AXIS_SLOP = 0.02;
    private static final double ORIGIN_SLOP = 0.05;

    private static final Point ORIGIN_POINT = new Point(0, 0);

    private static final Point UP = new Point(0, 1);
    private static final Point DOWN = new Point(0, -1);
    private static final Point RIGHT = new Point(1, 0);
    private static final Point LEFT = new Point(-1, 0);

    private final String name;
    private final List<Segment> segments;

    /**
     * Create a sector.
     * @param name The name of the sector.
     * @param segments The segments defining the boundaries of this sector.
     */
    private Sector(String name, List<Segment> segments) {
        this.name = name;
        this.segments = segments;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Does this sector contain the point?
     * @param p The point to test.
     * @return True if the point is inside this sector.
     */
    public boolean contains(Point p) {
        return this.segments.stream().allMatch(segment -> segment.inside(p));
    }

    /**
     * Does this sector intersect the segment?
     * @param s The segment to test.
     * @return True if the segment intersects with the boundary of this sector.
     */
    private boolean intersects(Segment s) {
        return this.segments.stream().anyMatch(segment -> segment.intersects(s));
    }

    /**
     * Measure whether a line is inside, outside, or intersecting this sector.
     * @param line The line to test.
     * @return Whether a line is inside, outside, or intersecting this sector.
     */
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
        if (allInside && !anyIntersections) {
            return Intersection.INSIDE;
        } else if (someInside || anyIntersections) {
            return Intersection.INTERSECTS;
        } else {
            return Intersection.OUTSIDE;
        }
    }

    /**
     * Find the parameters of all intersections between a segment and this sector.
     * @param lineSegment The segment to analyse.
     * @return The parameters of each intersection.
     */
    public IntersectionParams intersectionParams(Segment lineSegment) {
        return new IntersectionParams(this.segments.stream()
                .map(segment -> segment.intersectionParam(lineSegment))
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
    }

    /**
     * Create a line which is clipped to be only inside this sector.
     *
     * Any discontinuities are joined by straight lines. For example, clipping a sine wave to the top sector would give
     * a half-rectified wave.
     *
     * @param line The line to clip.
     * @return A new clip that is wholly inside this sector.
     */
    public Line clip(Line line) {
        Line result = line;
        for (Segment segment : this.segments) {
            result = segment.clip(result);
        }
        return result;
    }

    /**
     * Helper method to create a quadrant sector.
     *
     * Technically, this could make shapes other than a quadrant. It is really an infinite sector from origin between
     * axis1 and axis2.
     *
     * @param name The name of the sector.
     * @param origin The origin of the quadrant
     * @param axis1 The direction of one side of the quadrant.
     * @param axis2 The direction of the other side of the quadrant.
     * @return The quadrant sector.
     */
    private static Sector quadrant(String name, Point origin, Point axis1, Point axis2) {
        return new Sector(name, Arrays.asList(
            Segment.openOneEnd(origin, axis1, axis2),
            Segment.openOneEnd(origin, axis2, axis1)
        ));
    }

    /**
     * Helper method to create a quadrant sector centred on the origin.
     * @param name The name of the sector.
     * @param axis1 The direction of one side of the quadrant.
     * @param axis2 The direction of the other side of the quadrant.
     * @return The sector.
     */
    private static Sector centeredQuadrant(String name, Point axis1, Point axis2) {
        return quadrant(name, ORIGIN_POINT, axis1, axis2);
    }

    public static final Sector topRight = centeredQuadrant("topRight", RIGHT, UP);

    public static final Sector topLeft = centeredQuadrant("topLeft", LEFT, UP);

    public static final Sector bottomLeft = centeredQuadrant("bottomLeft", LEFT, DOWN);

    public static final Sector bottomRight = centeredQuadrant("bottomRight", RIGHT, DOWN);

    /**
     * Helper to create a Sector which represents an area near an axis.
     *
     * The area is defined as between the points left and right, scaled by AXIS_SLOP, extending out to infinity in the
     * direction of axis.
     *
     * @param name The name of this sector.
     * @param left The left-hand side of the strip.
     * @param right The right-hand side of the strip.
     * @param axis The direction the strip extends in.
     * @return The sloppy axis sector.
     */
    private static Sector sloppyAxis(String name, Point left, Point right, Point axis) {
        left = left.times(AXIS_SLOP);
        right = right.times(AXIS_SLOP);
        return new Sector(name, Arrays.asList(
                Segment.closed(left, right),
                Segment.openOneEnd(left, axis, Side.RIGHT),
                Segment.openOneEnd(right, axis, Side.LEFT)
        ));
    }

    public static final Sector onAxisWithPositiveY = sloppyAxis("+Yaxis", LEFT, RIGHT, UP);

    public static final Sector onAxisWithNegativeY = sloppyAxis("-Yaxis", RIGHT, LEFT, DOWN);

    public static final Sector onAxisWithPositiveX = sloppyAxis("+Xaxis", UP, DOWN, RIGHT);

    public static final Sector onAxisWithNegativeX = sloppyAxis("-Xaxis", DOWN, UP, LEFT);

    /**
     * Helper to create a Sector which represents a square of a certain size, centred on the origin.
     *
     * @param name The name of this sector.
     * @param size The radius (half-width) of the square.
     * @return The square sector.
     */
    private static Sector square(String name, double size) {
        Point[] originPoints = new Point[] {
            new Point(size, size), new Point(-size, size),
            new Point(-size, -size), new Point(size, -size)};

        Iterable<Point> points = Iterables.cycle(originPoints);

        return new Sector(name,
            Streams.zip(
                Streams.stream(points),
                Streams.stream(points).skip(1),
                Segment::closed)
            .limit(originPoints.length)
            .collect(Collectors.toList())
        );
    }

    public static final Sector origin = square("origin", ORIGIN_SLOP);

    public static final Sector relaxedOrigin = square("relaxedOrigin", 0.1);

    /**
     * Helper to create a Sector containing points to the left of line through the origin in the direction of axis.
     *
     * Note this is the trickiest one in terms of side. It is always the left, so if you want points to the right of the
     * origin, you need the axis to point down, because the left of the a line pointing downwards is positive X.
     *
     * @param name The name of this sector.
     * @param axis The direction of the line defining this sector.
     * @return The half-plane sector.
     */
    private static Sector centeredHalf(String name, Point axis) {
        return new Sector(name, Collections.singletonList(
            Segment.openBothEnds(ORIGIN_POINT, axis, Side.LEFT)
        ));
    }

    public static final Sector left = centeredHalf("left", UP);
    public static final Sector right = centeredHalf("right", DOWN);

    /**
     * Get a sector by name.
     *
     * Some sectors have a shorter alias which should be specified here.
     *
     * @param s The name of the sector.
     * @return The sector.
     */
    public static Sector byName(String s) {
        try {
            switch (s) {
                case "+Xaxis":
                    return onAxisWithPositiveX;
                case "-Xaxis":
                    return onAxisWithNegativeX;
                case "+Yaxis":
                    return onAxisWithPositiveY;
                case "-Yaxis":
                    return onAxisWithNegativeY;
                default:
                    return (Sector) Sector.class.getDeclaredField(s).get(null);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException(s + " is not a valid sector");
        }
    }

    public static final List<Sector> defaultOrderedSectors = Arrays.asList(
        origin,
        onAxisWithPositiveX,
        onAxisWithPositiveY,
        onAxisWithNegativeX,
        onAxisWithNegativeY,
        topRight,
        topLeft,
        bottomLeft,
        bottomRight
    );

    /**
     * Identify which sectors this point could be in.
     * @param point The point to be classified.
     * @param orderedSectors The list of sectors to classify this point against.
     * @return The set of sectors this point could be in.
     */
    public static Set<Sector> classify(Point point, List<Sector> orderedSectors) {
        return orderedSectors.stream()
            .filter(sector -> sector.contains(point))
            .collect(Collectors.toSet());
    }

    /**
     * Identify which sector this point is in against the default priority-ordered list of sectors.
     * @param point The point.
     * @return The highest-priority sector that contains this point.
     */
    public static Sector classify(Point point) {
        Set<Sector> possibleSectors = classify(point, defaultOrderedSectors);
        return defaultOrderedSectors.stream()
            .filter(possibleSectors::contains)
            .findFirst()
            .get();
    }

    /**
     * Helper to generate a sector of the half-plane with x co-ordinate less than or equal to X.
     * @param x The x co-ordinate to split the plane.
     * @return The half plane.
     */
    public static Sector leftOfX(double x) {
        return new Sector("leftOfX=" + x, Collections.singletonList(
            Segment.openBothEnds(new Point(x, 0), UP, Side.LEFT)
        ));
    }

    /**
     * Helper to generate a sector of the half-plane with x co-ordinate greater than or equal to X.
     * @param x The x co-ordinate to split the plane.
     * @return The half plane.
     */
    public static Sector rightOfX(double x) {
        return new Sector("rightOfX=" + x, Collections.singletonList(
            Segment.openBothEnds(new Point(x, 0), UP, Side.RIGHT)
        ));
    }
}
