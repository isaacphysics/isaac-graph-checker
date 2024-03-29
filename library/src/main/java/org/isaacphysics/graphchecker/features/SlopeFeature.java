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
package org.isaacphysics.graphchecker.features;

import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.settings.SettingsInterface;
import org.isaacphysics.graphchecker.data.HumanNamedEnum;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.features.internals.LineFeature;
import org.isaacphysics.graphchecker.geometry.Lines;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A line feature which requires the line to have a specific slope at the start and/or end.
 *
 * The slope is calculated by taking a certain number of points at the start/end of the line and then computing the
 * bounding box of those lines. If the aspect ratio of the box is greater than getSlopeThreshold, the line has an
 * interesting slope.
 */
public class SlopeFeature extends LineFeature<SlopeFeature.Instance, SlopeFeature.Settings> {

    /**
     * Create a slope feature with specified settings.
     * @param settings The settings.
     */
    SlopeFeature(Settings settings) {
        super(settings);
    }

    /**
     * The settings for a SlopeFeature.
     */
    @SuppressWarnings("magicNumber")
    public interface Settings extends SettingsInterface {
        /**
         * @return The minimum ratio of change in order co-ordinate to the other to be considered steep.
         */
        default double getSlopeThreshold() {
            return 4;
        }

        /**
         * @return The number of points to take from either end of the line to check its slope.
         */
        default int getNumberOfPointsAtEnds() {
            return 5;
        }
    }

    /**
     * A section of the line.
     */
    enum Position implements HumanNamedEnum {
        START {
            @Override
            List<Point> selectPoints(List<Point> points, int size, int desired) {
                return points.subList(0, desired);
            }
        },
        END {
            @Override
            List<Point> selectPoints(List<Point> points, int size, int desired) {
                return points.subList(size - desired, size);
            }
        };

        /**
         * Select the points from this line that cover this position.
         *
         * @param points The points from the line.
         * @param size The length of points.
         * @param desired The number of points to be taken.
         * @return A subList of points.
         */
        abstract List<Point> selectPoints(List<Point> points, int size, int desired);
    }

    /**
     * The shape of the slope.
     */
    enum Slope implements HumanNamedEnum {
        UP, // Nearly vertical going upwards
        POSITIVE, // Between UP and FLAT
        FLAT, // Nearly horizontal
        NEGATIVE, // Between FLAT and DOWN
        DOWN, // Nearly vertical going downwards
    }

    @Override
    public String tag() {
        return "slope";
    }

    /**
     * An instance of the Slope feature.
     */
    protected class Instance extends LineFeature<?, ?>.Instance {

        private final Map<Position, Slope> expectedSlopes;

        /**
         * Create an instance which expects these positions to have specified slopes.
         * @param featureData The specification for this feature.
         * @param expectedSlopes A map of positions to expected slopes.
         */
        Instance(String featureData, Map<Position, Slope> expectedSlopes) {
            super(featureData);
            this.expectedSlopes = expectedSlopes;
        }

        @Override
        public boolean test(Line line) {
            return expectedSlopes.entrySet().stream()
                .allMatch(entry -> {
                    Position position = entry.getKey();
                    Line lineToMeasure = lineAtPosition(line, position);
                    Slope actualSlope = lineToSlope(lineToMeasure);
                    return entry.getValue() == actualSlope;
                });
        }
    }

    @Override
    public Instance deserializeInternal(String featureData) {
        String[] items = featureData.split("\\s*,\\s*");
        return new Instance(featureData, Arrays.stream(items)
            .map(item -> {
                String[] parts = item.split("=");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Incorrect number of slope parts in: " + item);
                }
                return parts;
            }).collect(Collectors.toMap(
                parts -> Position.valueOf(parts[0].trim().toUpperCase()),
                parts -> Slope.valueOf(parts[1].trim().toUpperCase())
            )));
    }

    @Override
    public List<String> generate(Line expectedLine) {
        return Collections.singletonList(Arrays.stream(Position.values())
        .map(position -> ImmutablePair.of(position, lineToSlope(lineAtPosition(expectedLine, position))))
        .map(pair -> pair.getLeft().humanName() + "=" + pair.getRight().humanName())
        .collect(Collectors.joining(", ")));
    }

    /**
     * Convert a line into a slope description.
     *
     * @param line The line to measure.
     * @return The slope of the line.
     */
    Slope lineToSlope(Line line) {
        Point size = Lines.getSize(line);

        // Negative X is incorrect for our purposes, so force it to be positive.
        size = new Point(Math.abs(size.getX()), size.getY());

        double highIfFlat = size.getX() / size.getY();
        if (Math.abs(highIfFlat) > settings().getSlopeThreshold()) {
            return Slope.FLAT;
        }

        double highIfSteep = size.getY() / size.getX();
        if (highIfSteep > 0) {
            if (Math.abs(highIfSteep) > settings().getSlopeThreshold()) {
                return Slope.UP;
            } else {
                return Slope.POSITIVE;
            }
        } else {
            if (Math.abs(highIfSteep) > settings().getSlopeThreshold()) {
                return Slope.DOWN;
            } else {
                return Slope.NEGATIVE;
            }
        }
    }

    /**
     * Take a section of a line at the start or end.
     * @param line The line.
     * @param position The section of the line to return.
     * @return A new line that just covers line at position.
     */
    private Line lineAtPosition(Line line, Position position) {
        List<Point> points = line.getPoints();
        int size = points.size();
        int desired = Math.min(settings().getNumberOfPointsAtEnds(), size);
        return new Line(position.selectPoints(points, size, desired), Collections.emptyList());
    }
}
