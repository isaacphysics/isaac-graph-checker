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

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.PointOfInterest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    @SuppressWarnings({"checkstyle:needBraces", "checkstyle:avoidInlineConditionals"})
    public static Point getSize(Line line) {
        if (line.getPoints().isEmpty()) return new Point(0, 0);

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

        double centreX = (maxX + minX) / 2;
        double centreY = (maxY + minY) / 2;

        double diffX = maxX - minX;
        double diffY = maxY - minY;

        double startX = line.getPoints().get(0).getX();
        double startY = line.getPoints().get(0).getY();

        double x = startX < centreX ? diffX : -diffX;
        double y = startY < centreY ? diffY : -diffY;

        return new Point(x, y);
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
}
