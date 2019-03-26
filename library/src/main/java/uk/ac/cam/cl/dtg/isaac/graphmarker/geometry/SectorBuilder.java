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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates and manages settings for Sector, which is more focused just on the geometry.
 */
public class SectorBuilder {
    private final Settings settings;

    /**
     * Constructor which stores settings.
     * @param settings The settings for this builder.
     */
    public SectorBuilder(Settings settings) {
        this.settings = settings;
    }

    private static final Map<Settings, SectorBuilder> SECTOR_BUILDER_CACHE = new HashMap<>();

    /**
     * The type of settings for SectorBuilder.
     */
    @SuppressWarnings("magicNumber")
    public interface Settings extends SettingsInterface {
        /**
         * @return The width either side of the axis that is considered as in the axis sector.
         */
        default double getAxisSlop() {
            return 0.02;
        }

        /**
         * @return The half-width of the square centred on the origin that is considered as the origin sector.
         */
        default double getOriginSlop() {
            return 0.05;
        }

        /**
         * The relaxed origin sector is used for checking odd/antisymmetric functions pass through the origin/their
         * centre respectively.
         *
         * @return The half-width of the square centred on the origin that is considered as the relaxed origin sector.
         */
        default double getRelaxedOriginSlop() {
            return 0.1;
        }

        /**
         * Factory method to get a SectorBuilder with these settings.
         *
         * SectorBuilder objects are cached by this method for performance.
         *
         * @return A SectorBuilder with these settings.
         */
        default SectorBuilder getSectorBuilder() {
            return SECTOR_BUILDER_CACHE.computeIfAbsent(this, SectorBuilder::new);
        }
    }

    /**
     * @return The default ordered list of sectors.
     */
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
                    String methodName = "get" + s.substring(0, 1).toUpperCase() + s.substring(1);
                    return (Sector) SectorBuilder.class.getMethod(methodName).invoke(this);
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException(s + " is not a valid sector");
        }
    }

    /**
     * Create a list of sectors from a stream of sector name strings.
     * @param sectors The stream of sector names.
     * @return The list of sectors.
     */
    public List<Sector> fromList(Stream<String> sectors) {
        return sectors
            .map(this::byName)
            .collect(Collectors.toList());
    }

    /**
     * Create a list of sectors from a comma-separated list of sector names with optional spaces.
     * @param csv The comma-separated sectors.
     * @return The list of sectors.
     */
    public List<Sector> fromList(String csv) {
        String[] sectorNames = csv.split(",");
        return fromList(Arrays.stream(sectorNames)
            .map(String::trim));
    }


    private final Map<String, Sector> sectorCache = new HashMap<>();

    /**
     * Helper method to create or cache a sector.
     *
     * @param name The name of the sector.
     * @param supplier The function to create the sector if it is not yet cached.
     * @return The sector.
     */
    private Sector cacheIfAbsent(String name, Supplier<List<Segment>> supplier) {
        return sectorCache.computeIfAbsent(name, name1 -> new Sector(name, supplier.get()));
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
        return cacheIfAbsent(name, () -> Arrays.asList(
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
        final Point leftScaled = left.times(settings.getAxisSlop());
        final Point rightScaled = right.times(settings.getAxisSlop());
        return cacheIfAbsent(name, () -> Arrays.asList(
                Segment.closed(leftScaled, rightScaled),
                Segment.openOneEnd(leftScaled, axis, Side.RIGHT),
                Segment.openOneEnd(rightScaled, axis, Side.LEFT)
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

        return cacheIfAbsent(name, () ->
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
        return cacheIfAbsent(name, () -> Collections.singletonList(
            Segment.openBothEnds(ORIGIN_POINT, axis, Side.LEFT)
        ));
    }

    @SuppressWarnings("javadocMethod")
    public Sector getOrigin() {
        return square("origin", settings.getOriginSlop());
    }

    @SuppressWarnings("javadocMethod")
    public Sector getOnAxisWithPositiveX() {
        return sloppyAxis("+Xaxis", UP, DOWN, RIGHT);
    }

    @SuppressWarnings("javadocMethod")
    public Sector getOnAxisWithNegativeX() {
        return sloppyAxis("-Xaxis", DOWN, UP, LEFT);
    }

    @SuppressWarnings("javadocMethod")
    public Sector getOnAxisWithPositiveY() {
        return sloppyAxis("+Yaxis", LEFT, RIGHT, UP);
    }

    @SuppressWarnings("javadocMethod")
    public Sector getOnAxisWithNegativeY() {
        return sloppyAxis("-Yaxis", RIGHT, LEFT, DOWN);
    }

    @SuppressWarnings("javadocMethod")
    public Sector getTopLeft() {
        return centeredQuadrant("topLeft", LEFT, UP);
    }

    @SuppressWarnings("javadocMethod")
    public Sector getTopRight() {
        return centeredQuadrant("topRight", RIGHT, UP);
    }

    @SuppressWarnings("javadocMethod")
    public Sector getBottomLeft() {
        return centeredQuadrant("bottomLeft", LEFT, DOWN);
    }

    @SuppressWarnings("javadocMethod")
    public Sector getBottomRight() {
        return centeredQuadrant("bottomRight", RIGHT, DOWN);
    }

    /**
     * @return The Sector of origin with a more relaxed boundary.
     */
    public Sector getRelaxedOrigin() {
        return square("relaxedOrigin", settings.getRelaxedOriginSlop());
    }

    @SuppressWarnings("javadocMethod")
    public Sector getLeft() {
        return centeredHalf("left", UP);
    }

    @SuppressWarnings("javadocMethod")
    public Sector getRight() {
        return centeredHalf("right", DOWN);
    }
}
