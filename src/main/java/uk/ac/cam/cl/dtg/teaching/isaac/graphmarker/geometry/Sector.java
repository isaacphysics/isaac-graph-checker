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
public class Sector {

    public static final double AXIS_SLOP = 0.02;
    private static final double ORIGIN_SLOP = 0.05;

    private static final Point originPoint = new Point(0, 0);

    private static final Point UP = new Point(0, 1);
    private static final Point DOWN = new Point(0, -1);
    private static final Point RIGHT = new Point(1, 0);
    private static final Point LEFT = new Point(-1, 0);

    private final String name;
    private final List<Segment> segments;

    private Sector(String name, List<Segment> segments) {
        this.name = name;
        this.segments = segments;
    }

    @Override
    public String toString() {
        return name;
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

    public IntersectionParams intersectionParams(Segment lineSegment) {
        return new IntersectionParams(this.segments.stream()
                .map(segment -> segment.intersectionParam(lineSegment))
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
    }

    public Line clip(Line line) {
        Line result = line;
        for (Segment segment : this.segments) {
            result = segment.clip(result);
        }
        return result;
    }

    private static Sector quadrant(String name, Point origin, Point axis1, Point axis2) {
        return new Sector(name, Arrays.asList(
            Segment.openOneEnd(origin, axis1, axis2),
            Segment.openOneEnd(origin, axis2, axis1)
        ));
    }

    private static Sector centeredQuadrant(String name, Point axis1, Point axis2) {
        return quadrant(name, originPoint, axis1, axis2);
    }

    public static final Sector topRight = centeredQuadrant("topRight", RIGHT, UP);

    public static final Sector topLeft = centeredQuadrant("topLeft", LEFT, UP);

    public static final Sector bottomLeft = centeredQuadrant("bottomLeft", LEFT, DOWN);

    public static final Sector bottomRight = centeredQuadrant("bottomRight", RIGHT, DOWN);

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

    private static Sector square(String name, double size) {
        Point[] originPoints = new Point[] {
            new Point(size, size), new Point(-size, size),
            new Point(-size, -size), new Point(size, -size)};

        return new Sector(name, Arrays.asList(
            Segment.closed(originPoints[0], originPoints[1]),
            Segment.closed(originPoints[1], originPoints[2]),
            Segment.closed(originPoints[2], originPoints[3]),
            Segment.closed(originPoints[3], originPoints[0])
        ));
    }

    public static final Sector origin = square("origin", ORIGIN_SLOP);

    public static final Sector relaxedOrigin = square("relaxedOrigin", 0.1);

    private static Sector centeredHalf(String name, Point axis) {
        return new Sector(name, Collections.singletonList(
            Segment.openBothEnds(originPoint, axis, Side.LEFT)
        ));
    }

    public static final Sector left = centeredHalf("left", UP);
    public static final Sector right = centeredHalf("right", DOWN);
    public static Sector top = centeredHalf("top", RIGHT);
    public static Sector bottom = centeredHalf("bottom", LEFT);

    public static Sector byName(String s) {
        try {
            switch(s) {
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
        } catch (IllegalAccessException|NoSuchFieldException e) {
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

    public static Set<Sector> classify(Point point, List<Sector> orderedSectors) {
        return orderedSectors.stream()
            .filter(sector -> sector.contains(point))
            .collect(Collectors.toSet());
    }

    public static Sector classify(Point point) {
        Set<Sector> possibleSectors = classify(point, defaultOrderedSectors);
        return defaultOrderedSectors.stream()
            .filter(possibleSectors::contains)
            .findFirst()
            .get();
    }

    public static Sector leftOfX(double x) {
        return new Sector("leftOfX=" + x, Collections.singletonList(
            Segment.openBothEnds(new Point(x, 0), UP, Side.LEFT)
        ));
    }

    public static Sector rightOfX(double x) {
        return new Sector("rightOfX=" + x, Collections.singletonList(
            Segment.openBothEnds(new Point(x, 0), UP, Side.RIGHT)
        ));
    }
}
