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

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import org.isaacphysics.graphchecker.data.IntersectionParams;
import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.data.Rect;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.data.PointOfInterest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class of functions on Line objects.
 */
public class Lines {
    /**
     * Split a line into a list of lines on the x-coordinates of the supplied points.
     *
     * There will be an overlapping point between each line.
     *
     * For example, 1--2--A--3--B--4--C---5 (where numbers are points and letters are the splitPoints) will split to:
     *
     * - 1--2--A
     * - A--3--B
     * - B--4--C
     * - C---5
     *
     * @param line The line to be split.
     * @param splitPoints The points to split on.
     * @return A list of lines.
     */
    public static List<Line> splitOnPoints(Line line, List<PointOfInterest> splitPoints) {
        List<Line> lines = new ArrayList<>(splitPoints.size() + 1);
        Line remainder = line;
        for (PointOfInterest point : splitPoints) {
            double x = point.getX();
            Line left = leftOfX(x).clip(remainder);
            remainder = rightOfX(x).clip(remainder);
            lines.add(left);
        }
        lines.add(remainder);
        return lines;
    }

    /**
     * Get the "size" of a Line.
     *
     * The size is the bounding box of the line, with the sign given by whether the line goes in the direction of the
     * axis, or against it.
     *
     * @param line The line to be analysed.
     * @return The width and height of the bounding box.
     */
    @SuppressWarnings({"checkstyle:avoidInlineConditionals"})
    public static Point getSize(Line line) {
        if (line.getPoints().isEmpty()) {
            return new Point(0, 0);
        }

        Rect bounds = boundingRect(line);

        double centreX = (bounds.getRight() + bounds.getLeft()) / 2;
        double centreY = (bounds.getTop() + bounds.getBottom()) / 2;

        double diffX = bounds.getRight() - bounds.getLeft();
        double diffY = bounds.getTop() - bounds.getBottom();

        double startX = line.getPoints().get(0).getX();
        double startY = line.getPoints().get(0).getY();

        double x = startX < centreX ? diffX : -diffX;
        double y = startY < centreY ? diffY : -diffY;

        return new Point(x, y);
    }

    /**
     * Check if there is no horizontal overlap between a collection of lines.
     *
     * @param lines The collection of lines.
     * @return True if there is no horizontal overlap between the lines; i.e. the set of lines is single-valued.
     */
    public static boolean noHorizonalOverlap(Collection<Line> lines) {
        List<Pair<Double, Double>> runs = new ArrayList<>();
        for (Line line : lines) {
            Pair<Double, Double> span = horizontalSpan(line);
            for (Pair<Double, Double> existing : runs) {
                if (span.getLeft() <= existing.getRight() && span.getRight() >= existing.getLeft()) {
                    return false;
                }
            }
            runs.add(span);
        }
        return true;
    }

    /**
     * This is just a utility class.
     */
    private Lines() {
    }

    private static final Point UP = new Point(0, 1);

    /**
     * Helper to generate a sector of the half-plane with x co-ordinate less than or equal to X.
     * @param x The x co-ordinate to split the plane.
     * @return The half plane.
     */
    private static Sector leftOfX(double x) {
        return new Sector("leftOfX=" + x, Collections.singletonList(
            Segment.openBothEnds(new Point(x, 0), UP, Side.LEFT)
        ));
    }

    /**
     * Helper to generate a sector of the half-plane with x co-ordinate greater than or equal to X.
     * @param x The x co-ordinate to split the plane.
     * @return The half plane.
     */
    private static Sector rightOfX(double x) {
        return new Sector("rightOfX=" + x, Collections.singletonList(
            Segment.openBothEnds(new Point(x, 0), UP, Side.RIGHT)
        ));
    }

    /**
     * Get the minimum and maximum X co-ordinates of a line.
     *
     * @param line The line.
     * @return A pair of (minimum, maximum) X co-ordinates.
     */
    private static Pair<Double, Double> horizontalSpan(Line line) {
        return Pair.of(line.getPoints().stream().mapToDouble(Point::getX).min().getAsDouble(),
            line.getPoints().stream().mapToDouble(Point::getX).max().getAsDouble());
    }

    /**
     * Find all the intersecting points between two lines.
     * @param lineA The first line.
     * @param lineB The second line.
     * @return The list of intersections between the lines.
     */
    public static List<Point> findIntersections(Line lineA, Line lineB) {
        if (lineA.getPoints().size() == 2 && lineB.getPoints().size() == 2) {
            Segment a = lineToSegment(lineA);
            Segment b = lineToSegment(lineB);
            IntersectionParams.IntersectionParam intersectionParam = a.intersectionParam(b);
            if (intersectionParam != null) {
                return Collections.singletonList(b.atParameter(intersectionParam.getT()));
            } else {
                return Collections.emptyList();
            }
        } else {
            List<Line> splitA = splitInHalf(lineA);
            List<Line> splitB = splitInHalf(lineB);

            return splitA.stream()
                .flatMap(subA ->
                    splitB.stream()
                        .flatMap(
                            subB -> {
                                if (boundingIntersects(subA, subB)) {
                                    return findIntersections(subA, subB).stream();
                                } else {
                                    return Stream.empty();
                                }
                            }
                        ))
                .distinct()
                .collect(Collectors.toList());
        }
    }

    /**
     * Check if the bounding boxes of two lines intersects.
     * @param subA The first line.
     * @param subB The second line.
     * @return True if their bounding boxes intersect.
     */
    private static boolean boundingIntersects(Line subA, Line subB) {
        Rect boundingA = boundingRect(subA);
        Rect boundingB = boundingRect(subB);

        return boundingA.getLeft() <= boundingB.getRight()
            && boundingA.getRight() >= boundingB.getLeft()
            && boundingA.getTop() >= boundingB.getBottom()
            && boundingA.getBottom() <= boundingB.getTop();
    }

    /**
     * Get the bounding rectangle of a line.
     * @param line The line.
     * @return The bounding rectangle.
     */
    @SuppressWarnings({"checkstyle:needBraces"})
    private static Rect boundingRect(Line line) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point p : line.getPoints()) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        return new Rect(minX, maxX, maxY, minY);
    }

    /**
     * Split a line in half, discarding any points of interest. If the line only has two points, it is not split.
     * @param line The line to split.
     * @return A list containing two new lines that share a point, or one line of just two points.
     */
    private static List<Line> splitInHalf(Line line) {
        List<Point> points = line.getPoints();
        if (points.size() == 2) {
            return Collections.singletonList(line);
        }
        int half = points.size() / 2;
        return ImmutableList.of(
            new Line(points.subList(0, half + 1), Collections.emptyList()),
            new Line(points.subList(half, points.size()), Collections.emptyList())
        );
    }

    /**
     * Convert a line of two points to a closed segment.
     * @param line The line.
     * @return A segment representing the first part of that line.
     */
    private static Segment lineToSegment(Line line) {
        assert line.getPoints().size() == 2;
        return Segment.closed(line.getPoints().get(0), line.getPoints().get(1));
    }

    /**
     * Find the centre point of a line (averaging the middle two points if the line has an even number of points).
     * @param points The list of points.
     * @return The median point.
     */
    @SuppressWarnings("magicNumber")
    public static Point getCentreOfPoints(List<Point> points) {
        if ((points.size() % 2) == 0) {
            Point center1 = points.get(points.size() / 2 - 1);
            Point center2 = points.get(points.size() / 2);
            return center1.add(center2).times(0.5);
        } else {
            return points.get(points.size() / 2);
        }
    }
}
