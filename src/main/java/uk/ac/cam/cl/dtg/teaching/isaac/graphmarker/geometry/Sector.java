package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Intersection;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointOfInterest;

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

    public static final double AXIS_SLOP = 0.01;
    private static final double ORIGIN_SLOP = AXIS_SLOP * 2;

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
                .filter(t -> t != null)
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

    public static Sector topRight = centeredQuadrant("topRight", RIGHT, UP);

    public static Sector topLeft = centeredQuadrant("topLeft", LEFT, UP);

    public static Sector bottomLeft = centeredQuadrant("bottomLeft", LEFT, DOWN);

    public static Sector bottomRight = centeredQuadrant("bottomRight", RIGHT, DOWN);

    private static Sector sloppyAxis(String name, Point left, Point right, Point axis) {
        left = left.times(AXIS_SLOP);
        right = right.times(AXIS_SLOP);
        return new Sector(name, Arrays.asList(
                Segment.closed(left, right),
                Segment.openOneEnd(left, axis, Side.RIGHT),
                Segment.openOneEnd(right, axis, Side.LEFT)
        ));
    }

    public static Sector onAxisWithPositiveY = sloppyAxis("onAxisWithPositiveY", LEFT, RIGHT, UP);

    public static Sector onAxisWithNegativeY = sloppyAxis("onAxisWithNegativeY", RIGHT, LEFT, DOWN);

    public static Sector onAxisWithPositiveX = sloppyAxis("onAxisWithPositiveX", UP, DOWN, RIGHT);

    public static Sector onAxisWithNegativeX = sloppyAxis("onAxisWithNegativeX", DOWN, UP, LEFT);

    private static final Point[] originPoints = new Point[] {
            new Point(ORIGIN_SLOP, ORIGIN_SLOP), new Point(-ORIGIN_SLOP, ORIGIN_SLOP),
            new Point(-ORIGIN_SLOP, -ORIGIN_SLOP), new Point(ORIGIN_SLOP, -ORIGIN_SLOP)};

    public static Sector origin = new Sector("origin", Arrays.asList(
            Segment.closed(originPoints[0], originPoints[1]),
            Segment.closed(originPoints[1], originPoints[2]),
            Segment.closed(originPoints[2], originPoints[3]),
            Segment.closed(originPoints[3], originPoints[0])
    ));

    private static Sector centeredHalf(String name, Point axis) {
        return new Sector(name, Collections.singletonList(
            Segment.openBothEnds(originPoint, axis, Side.LEFT)
        ));
    }

    public static Sector left = centeredHalf("left", UP);
    public static Sector right = centeredHalf("right", DOWN);
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
}
