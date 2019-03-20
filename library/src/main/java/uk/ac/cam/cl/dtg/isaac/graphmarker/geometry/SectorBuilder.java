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

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsInterface;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SectorBuilder {
    private final Settings settings;

    public SectorBuilder(Settings settings) {
        this.settings = settings;
    }

    public List<Sector> getDefaultOrderedSectors() {
        return Arrays.asList(
            getOrigin(),
            getOnAxisWithPositiveX(),
            getOnAxisWithPositiveY(),
            getOnAxisWithNegativeX(),
            getOnAxisWithNegativeY(),
            getTopRight(),
            getTopLeft(),
            getBottomLeft(),
            getBottomRight());
    }

    public interface Settings extends SettingsInterface {
        default double getAxisSlop() {
            return 0.02;
        }

        default double getOriginSlop() {
            return 0.05;
        }

        default double getRelaxedOriginSlop() {
            return 0.1;
        }

        default SectorBuilder getSectorBuilder() {
            return new SectorBuilder(this);
        }
    }

    private static final Point ORIGIN_POINT = new Point(0, 0);
    private static final Point LEFT = new Point(-1, 0);
    private static final Point RIGHT = new Point(1, 0);
    private static final Point UP = new Point(0, 1);
    private static final Point DOWN = new Point(0, -1);

    /**
     * Get a sector by name.
     *
     * Some sectors have a shorter alias which should be specified here.
     *
     * @param s The name of the sector.
     * @return The sector.
     */
    public Sector byName(String s) {
        try {
            switch (s) {
                case "+Xaxis":
                    return getOnAxisWithPositiveX();
                case "-Xaxis":
                    return getOnAxisWithNegativeX();
                case "+Yaxis":
                    return getOnAxisWithPositiveY();
                case "-Yaxis":
                    return getOnAxisWithNegativeY();
                default:
                    return (Sector) SectorBuilder.class.getMethod("get" + s.substring(0, 1).toUpperCase() + s.substring(1)).invoke(this);
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException(s + " is not a valid sector");
        }
    }

    /**
     * Helper to generate a sector of the half-plane with x co-ordinate less than or equal to X.
     * @param x The x co-ordinate to split the plane.
     * @return The half plane.
     */
    public static Sector leftOfX(double x) {
        return new Sector(SettingsWrapper.DEFAULT, "leftOfX=" + x, Collections.singletonList(
            Segment.openBothEnds(new Point(x, 0), UP, Side.LEFT)
        ));
    }

    /**
     * Helper to generate a sector of the half-plane with x co-ordinate greater than or equal to X.
     * @param x The x co-ordinate to split the plane.
     * @return The half plane.
     */
    public static Sector rightOfX(double x) {
        return new Sector(SettingsWrapper.DEFAULT, "rightOfX=" + x, Collections.singletonList(
            Segment.openBothEnds(new Point(x, 0), UP, Side.RIGHT)
        ));
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
    private Sector quadrant(String name, Point origin, Point axis1, Point axis2) {
        return new Sector(this.settings, name, Arrays.asList(
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
    private Sector centeredQuadrant(String name, Point axis1, Point axis2) {
        return quadrant(name, ORIGIN_POINT, axis1, axis2);
    }

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
    private Sector sloppyAxis(String name, Point left, Point right, Point axis) {
        left = left.times(settings.getAxisSlop());
        right = right.times(settings.getAxisSlop());
        return new Sector(this.settings, name, Arrays.asList(
                Segment.closed(left, right),
                Segment.openOneEnd(left, axis, Side.RIGHT),
                Segment.openOneEnd(right, axis, Side.LEFT)
        ));
    }

    /**
     * Helper to create a Sector which represents a square of a certain size, centred on the origin.
     *
     * @param name The name of this sector.
     * @param size The radius (half-width) of the square.
     * @return The square sector.
     */
    private Sector square(String name, double size) {
        Point[] originPoints = new Point[] {
            new Point(size, size), new Point(-size, size),
            new Point(-size, -size), new Point(size, -size)};

        Iterable<Point> points = Iterables.cycle(originPoints);

        return new Sector(this.settings, name,
            Streams.zip(
                Streams.stream(points),
                Streams.stream(points).skip(1),
                Segment::closed)
            .limit(originPoints.length)
            .collect(Collectors.toList())
        );
    }

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
    private Sector centeredHalf(String name, Point axis) {
        return new Sector(this.settings, name, Collections.singletonList(
            Segment.openBothEnds(ORIGIN_POINT, axis, Side.LEFT)
        ));
    }

    public Sector getLeft() {
        return centeredHalf("left", UP);
    }

    public Sector getRight() {
        return centeredHalf("right", DOWN);
    }

    public Sector getTop() {
        return centeredHalf("top", RIGHT);
    }

    public Sector getOnAxisWithPositiveX() {
        return sloppyAxis("+Xaxis", UP, DOWN, RIGHT);
    }

    public Sector getBottomRight() {
        return centeredQuadrant("bottomRight", RIGHT, DOWN);
    }

    public Sector getTopRight() {
        return centeredQuadrant("topRight", RIGHT, UP);
    }

    public Sector getBottom() {
        return centeredHalf("right", LEFT);
    }

    public Sector getOnAxisWithNegativeX() {
        return sloppyAxis("-Xaxis", DOWN, UP, LEFT);
    }

    public Sector getOnAxisWithNegativeY() {
        return sloppyAxis("-Yaxis", RIGHT, LEFT, DOWN);
    }

    public Sector getOnAxisWithPositiveY() {
        return sloppyAxis("+Yaxis", LEFT, RIGHT, UP);
    }

    public Sector getBottomLeft() {
        return centeredQuadrant("bottomLeft", LEFT, DOWN);
    }

    public Sector getTopLeft() {
        return centeredQuadrant("topLeft", LEFT, UP);
    }

    public Sector getOrigin() {
        return square("origin", settings.getOriginSlop());
    }

    public Sector getRelaxedOrigin() {
        return square("relaxedOrigin", settings.getRelaxedOriginSlop());
    }
}
