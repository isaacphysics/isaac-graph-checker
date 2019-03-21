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
package uk.ac.cam.cl.dtg.isaac.graphmarker.geometry;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.PointOfInterest;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class of functions on Line objects.
 */
public class Lines {
    /**
     * Split a line into a list of lines on the x-coordinates of the points of interest.
     *
     * There will be an overlapping point between each line.
     *
     * For example, 1--2--A--3--B--4--C---5 (where numbers are points and letters are points of interest) will split to:
     *
     * - 1--2--A
     * - A--3--B
     * - B--4--C
     * - C---5
     *
     * @param line The line to be split.
     * @return A list of lines.
     */
    public static List<Line> splitOnPointsOfInterest(Line line) {
        List<Line> lines = new ArrayList<>(line.getPointsOfInterest().size() + 1);
        Line remainder = line;
        for (PointOfInterest point : line.getPointsOfInterest()) {
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
        if (line.getPoints().isEmpty()) return new Point(0, 0);

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

    private static boolean boundingIntersects(Line subA, Line subB) {
        Rect boundingA = boundingRect(subA);
        Rect boundingB = boundingRect(subB);

        return boundingA.getLeft() <= boundingB.getRight()
            && boundingA.getRight() >= boundingB.getLeft()
            && boundingA.getTop() >= boundingB.getBottom()
            && boundingA.getBottom() <= boundingB.getTop();
    }

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

    private static Segment lineToSegment(Line line) {
        assert line.getPoints().size() == 2;
        return Segment.closed(line.getPoints().get(0), line.getPoints().get(1));
    }
}
