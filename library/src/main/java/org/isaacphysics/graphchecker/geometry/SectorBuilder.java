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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.settings.SettingsInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
         * @return The half-diagonal of the diamond centred on the origin that is considered as the origin sector.
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
            byName(ORIGIN),
            byName(POSITIVE_X_AXIS),
            byName(POSITIVE_Y_AXIS),
            byName(NEGATIVE_X_AXIS),
            byName(NEGATIVE_Y_AXIS),
            byName(TOP_RIGHT),
            byName(TOP_LEFT),
            byName(BOTTOM_LEFT),
            byName(BOTTOM_RIGHT),
            byName(TOP_RIGHT_SLOP),
            byName(TOP_LEFT_SLOP),
            byName(BOTTOM_LEFT_SLOP),
            byName(BOTTOM_RIGHT_SLOP));
    }

    public static final String ORIGIN = "origin";
    public static final String RELAXED_ORIGIN = "relaxedOrigin";
    public static final String POSITIVE_X_AXIS = "+Xaxis";
    public static final String NEGATIVE_X_AXIS = "-Xaxis";
    public static final String POSITIVE_Y_AXIS = "+Yaxis";
    public static final String NEGATIVE_Y_AXIS = "-Yaxis";
    public static final String TOP_LEFT = "topLeft";
    public static final String TOP_RIGHT = "topRight";
    public static final String BOTTOM_LEFT = "bottomLeft";
    public static final String BOTTOM_RIGHT = "bottomRight";
    public static final String TOP_LEFT_SLOP = "topLeftSlop";
    public static final String TOP_RIGHT_SLOP = "topRightSlop";
    public static final String BOTTOM_LEFT_SLOP = "bottomLeftSlop";
    public static final String BOTTOM_RIGHT_SLOP = "bottomRightSlop";
    public static final String LEFT_HALF = "left";
    public static final String RIGHT_HALF = "right";
    public static final String TOP_HALF = "top";
    public static final String BOTTOM_HALF = "bottom";
    public static final String ANY = "any";

    private static final Point ORIGIN_POINT = new Point(0, 0);
    private static final Point LEFT = new Point(-1, 0);
    private static final Point RIGHT = new Point(1, 0);
    private static final Point UP = new Point(0, 1);
    private static final Point DOWN = new Point(0, -1);

    private List<Sector> addSlop(List<Sector> sectorList) {
        int sectorListSize = sectorList.size();

        if (sectorListSize < 2) {
            // Can only add slop between elements in the list
            return sectorList;
        }

        List<Sector> newSectors = new ArrayList<>();
        // Loop through all the sectors except for the last
        for (int i = 0; i < sectorListSize - 1; i++) {
            Sector currentSector = sectorList.get(i);
            Sector nextSector = sectorList.get(i + 1);
            newSectors.add(currentSector);

            /*
            This should add slop twice `[topLeft, topRight] -> [topLeft, topLeftSlop, topRightSlop, topRight]`
            But this case is not sensible as the above should be reduced to just [topLeft]
             */
            if (Objects.equals(currentSector.toString(), TOP_LEFT) ||
                    Objects.equals(nextSector.toString(), TOP_LEFT)) {
                newSectors.add(byName(TOP_LEFT_SLOP));
            }
            if (Objects.equals(currentSector.toString(), TOP_RIGHT) ||
                    Objects.equals(nextSector.toString(), TOP_RIGHT)) {
                newSectors.add(byName(TOP_RIGHT_SLOP));
            }
            if (Objects.equals(currentSector.toString(), BOTTOM_LEFT) ||
                    Objects.equals(nextSector.toString(), BOTTOM_LEFT)) {
                newSectors.add(byName(BOTTOM_LEFT_SLOP));
            }
            if (Objects.equals(currentSector.toString(), BOTTOM_RIGHT) ||
                    Objects.equals(nextSector.toString(), BOTTOM_RIGHT)) {
                newSectors.add(byName(BOTTOM_RIGHT_SLOP));
            }
        }

        newSectors.add(sectorList.get(sectorListSize - 1));
        return newSectors;
    }

    /**
     * Get a sector by name.
     *
     * Some sectors have a shorter alias which should be specified here.
     *
     * @param s The name of the sector.
     * @return The sector.
     */
    public Sector byName(String s) {
        if (SECTOR_SHAPES.containsKey(s)) {
            return sectorCache.computeIfAbsent(s, name -> new Sector(name, SECTOR_SHAPES.get(name).apply(this)));
        }
        throw new IllegalArgumentException(s + " is not a valid sector");
    }

    /**
     * Create a list of sectors from a stream of sector name strings.
     * @param sectors The stream of sector names.
     * @return The list of sectors.
     */
    public List<Sector> fromList(Stream<String> sectors) {
        return addSlop(
                sectors.map(this::byName)
                .collect(Collectors.toList())
        );
    }

    /**
     * Create a list of sectors from a comma-separated list of sector names with optional spaces.
     * @param csv The comma-separated sectors.
     * @return The list of sectors.
     */
    public List<Sector> fromList(String csv) {
        String[] sectorNames = csv.split(",");
        return fromList(Arrays.stream(sectorNames).map(String::trim));
    }


    private final Map<String, Sector> sectorCache = new HashMap<>();

    /**
     * Helper method to create a quadrant sector.
     *
     * Technically, this could make shapes other than a quadrant. It is really an infinite sector from origin between
     * axis1 and axis2.
     *
     * @param origin The origin of the quadrant
     * @param axis1 The direction of one side of the quadrant.
     * @param axis2 The direction of the other side of the quadrant.
     * @return The quadrant segments.
     */
    private List<Segment> quadrant(Point origin, Point axis1, Point axis2) {
        return Arrays.asList(
            Segment.openOneEnd(origin, axis1, axis2),
            Segment.openOneEnd(origin, axis2, axis1)
        );
    }

    /**
     * Helper method to create a quadrant sector centred on the origin.
     * @param axis1 The direction of one side of the quadrant.
     * @param axis2 The direction of the other side of the quadrant.
     * @return The segments.
     */
    private List<Segment> centeredQuadrant(Point axis1, Point axis2) {
        return quadrant(ORIGIN_POINT, axis1, axis2);
    }

    /**
     * Helper method to create a quadrant sector centred on the origin with axis slop
     *
     * @param axis1 The direction of one side of the quadrant
     * @param axis2 The direction of the other side of the quadrant
     * @return The segments of the quadrant
     */
    private List<Segment> sloppyQuadrant(Point axis1, Point axis2) {
        final Point axis1Scaled = axis1.times(settings.getAxisSlop());
        final Point axis2Scaled = axis2.times(settings.getAxisSlop());
        final Point shiftedOrigin = axis1Scaled.add(axis2Scaled);
        return quadrant(shiftedOrigin, axis1Scaled, axis2Scaled);
    }

    /**
     * Helper to create a Sector which represents an area near an axis.
     *
     * The area is defined as between the points left and right, scaled by AXIS_SLOP, extending out to infinity in the
     * direction of axis.
     *
     * @param left The left-hand side of the strip.
     * @param right The right-hand side of the strip.
     * @param axis The direction the strip extends in.
     * @return The sloppy axis sector.
     */
    private List<Segment> sloppyAxis(Point left, Point right, Point axis) {
        final Point leftScaled = left.times(settings.getAxisSlop());
        final Point rightScaled = right.times(settings.getAxisSlop());
        return Arrays.asList(
            Segment.closed(leftScaled, rightScaled),
            Segment.openOneEnd(leftScaled, axis, Side.RIGHT),
            Segment.openOneEnd(rightScaled, axis, Side.LEFT)
        );
    }

    /**
     * Helper to create a Sector which represents a diamond of a certain size, centred on the origin.
     *
     * @param size The radius (half of the diagonal) of the diamond. Note it is smaller than the square.
     * @return The diamond sector.
     */
    private List<Segment> diamond(double size) {
        Point[] originPoints = new Point[] {
            new Point(size, 0), new Point(0, size),
            new Point(-size, 0), new Point(0, -size)};

        Iterable<Point> points = Iterables.cycle(originPoints);

        return Streams.zip(
            Streams.stream(points),
            Streams.stream(points).skip(1),
            Segment::closed)
            .limit(originPoints.length)
            .collect(Collectors.toList());
    }

    /**
     * Helper to create a Sector containing points to the left of line through the origin in the direction of axis.
     *
     * Note this is the trickiest one in terms of side. It is always the left, so if you want points to the right of the
     * origin, you need the axis to point down, because the left of the a line pointing downwards is positive X.
     *
     * @param axis The direction of the line defining this sector.
     * @return The half-plane segments.
     */
    private List<Segment> centeredHalf(Point axis) {
        return Collections.singletonList(
            Segment.openBothEnds(ORIGIN_POINT, axis, Side.LEFT)
        );
    }

    /**
     * Helper to create an empty list of segments, representing anywhere.
     *
     * @return Segments representing any location.
     */
    private List<Segment> anywhere() {
        return Collections.emptyList();
    }

    private static final ImmutableMap<String, Function<SectorBuilder, List<Segment>>> SECTOR_SHAPES =
        ImmutableMap.<String, Function<SectorBuilder, List<Segment>>>builder()
            .put(ORIGIN, builder -> builder.diamond(builder.settings.getOriginSlop()))
            .put(RELAXED_ORIGIN, builder -> builder.diamond(builder.settings.getRelaxedOriginSlop()))

            .put(POSITIVE_X_AXIS, builder -> builder.sloppyAxis(UP, DOWN, RIGHT))
            .put(NEGATIVE_X_AXIS, builder -> builder.sloppyAxis(DOWN, UP, LEFT))
            .put(POSITIVE_Y_AXIS, builder -> builder.sloppyAxis(LEFT, RIGHT, UP))
            .put(NEGATIVE_Y_AXIS, builder -> builder.sloppyAxis(RIGHT, LEFT, DOWN))

            .put(TOP_LEFT, builder -> builder.sloppyQuadrant(LEFT, UP))
            .put(TOP_RIGHT, builder -> builder.sloppyQuadrant(RIGHT, UP))
            .put(BOTTOM_LEFT, builder -> builder.sloppyQuadrant(LEFT, DOWN))
            .put(BOTTOM_RIGHT, builder -> builder.sloppyQuadrant(RIGHT, DOWN))

            // Add back the ignored slop to allow for lines to travel through the slop region
            .put(TOP_LEFT_SLOP, builder -> builder.centeredQuadrant(LEFT, UP))
            .put(TOP_RIGHT_SLOP, builder -> builder.centeredQuadrant(RIGHT, UP))
            .put(BOTTOM_LEFT_SLOP, builder -> builder.centeredQuadrant(LEFT, DOWN))
            .put(BOTTOM_RIGHT_SLOP, builder -> builder.centeredQuadrant(RIGHT, DOWN))

            .put(LEFT_HALF, builder -> builder.centeredHalf(UP))
            .put(RIGHT_HALF, builder -> builder.centeredHalf(DOWN))
            .put(TOP_HALF, builder -> builder.centeredHalf(RIGHT))
            .put(BOTTOM_HALF, builder -> builder.centeredHalf(LEFT))

            .put(ANY, SectorBuilder::anywhere)

            .build();

}
