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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.features.internals.LineFeature;
import org.isaacphysics.graphchecker.geometry.SectorBuilder;
import org.isaacphysics.graphchecker.geometry.SectorClassifier;
import org.isaacphysics.graphchecker.data.IntersectionParams;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.geometry.Sector;
import org.isaacphysics.graphchecker.geometry.Segment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A line feature which requires the line to pass exactly through a list of sectors.
 *
 * This feature works out which sectors the user's line might have passed through (for example, getting near an axis
 * might mean they have touched the axis, or it might not), and then matches that with the expected path of the line.
 *
 * Crossing any axis is considered inviolate though; if you cross the axis, even a little bit, then you definitely have
 * at least touched that axis (this is what invalidSectorSets ensures.)
 */
public class ExpectedSectorsFeature extends LineFeature<ExpectedSectorsFeature.Instance, SectorClassifier.Settings> {
    private static final Logger log = LoggerFactory.getLogger(ExpectedSectorsFeature.class);

    /**
     * Create a expected sectors feature with specified settings.
     * @param settings The settings.
     */
    ExpectedSectorsFeature(SectorClassifier.Settings settings) {
        super(settings);
        SectorBuilder sectorBuilder = settings.getSectorBuilder();
        invalidSectorSets = ImmutableList.of(
            ImmutableSet.of(sectorBuilder.byName(SectorBuilder.TOP_RIGHT), sectorBuilder.byName(SectorBuilder.BOTTOM_RIGHT)),
            ImmutableSet.of(sectorBuilder.byName(SectorBuilder.TOP_LEFT), sectorBuilder.byName(SectorBuilder.BOTTOM_LEFT)),
            ImmutableSet.of(sectorBuilder.byName(SectorBuilder.TOP_RIGHT), sectorBuilder.byName(SectorBuilder.TOP_LEFT)),
            ImmutableSet.of(sectorBuilder.byName(SectorBuilder.BOTTOM_RIGHT), sectorBuilder.byName(SectorBuilder.BOTTOM_LEFT)),
            ImmutableSet.of(sectorBuilder.byName(SectorBuilder.POSITIVE_X_AXIS), sectorBuilder.byName(SectorBuilder.NEGATIVE_X_AXIS)),
            ImmutableSet.of(sectorBuilder.byName(SectorBuilder.POSITIVE_Y_AXIS), sectorBuilder.byName(SectorBuilder.NEGATIVE_Y_AXIS))
        );
    }

    @Override
    public String tag() {
        return "through";
    }

    /**
     * An instance of the ExpectedSectors feature.
     */
    protected class Instance extends LineFeature<?, ?>.Instance {
        private final List<Sector> expectedSectors;

        /**
         * Create an instance which passes through these sectors.
         * @param featureData The specification for this feature.
         * @param expectedSectors The list of sectors an input must pass through.
         */
        Instance(String featureData, List<Sector> expectedSectors) {
            super(featureData);
            this.expectedSectors = expectedSectors;
        }

        @Override
        public boolean test(Line line) {
            List<Set<Sector>> actualSectors = convertLineToSectorSetList(line);
            log.debug("User line passed through sectors: " + actualSectors);
            return match(actualSectors);
        }

        /**
         * Check if a list of actual possible sectors matches a list of expected sectors.
         *
         * This method uses dynamic programming to match the sectors. Imagine first a grid of matches between actual
         * sector sets and expected sectors:
         *
         * e   actual
         * x   0 1 2 3 4
         * p 0 x     x
         * e 1 x   x
         * c 2   x x x
         * t 3 x   x x x
         *
         * We need to find a path that connects the top left to the bottom right, either straight or diagonally, without
         * doubling back to the left. That is, a path that only moves down, right, or diagonally down and right.
         * In this case, actual sector set 0 matches expected sectors 0 and 1, then actual 1 and 2 could match expected
         * 2, and finally actual sets 3 and 4 could match expected 3.
         *
         * In order to find the path, we could imagine building the grid above, and then replacing each true with a true
         * if and only if there is a true above or left of it (working downwards).
         *
         * And finally, we can make the standard dynamic programming optimisation and keep just the last row and the row
         * we're building up from the top.
         *
         * @param actual The sectors we possibly pass through, in order.
         * @return True if there is a match.
         */
        private boolean match(List<Set<Sector>> actual) {

            // This has a phantom left-half column to avoid a test in the loop below
            // The phantom column will always be false except above the first row to anchor the beginning.
            int matchArraySize = actual.size() + 1;

            boolean[] matches = new boolean[matchArraySize];
            matches[0] = true; // This is the fake match to anchor things to the beginning.

            for (Sector expectedSector : expectedSectors) {
                boolean[] nextMatches = new boolean[matchArraySize];
                for (int j = 0; j < actual.size(); j++) {
                    if (actual.get(j).contains(expectedSector)) {
                        nextMatches[j + 1] = matches[j] || matches[j + 1] || nextMatches[j];
                    }
                }
                matches = nextMatches;
            }

            return matches[matchArraySize - 1];
        }
    }

    /**
     * Create a list of Sector objects from a list of sector names.
     * @param sectors A comma-separated list of sector names.
     * @return A list of Sectors.
     */
    private List<Sector> deserializeSectors(String sectors) {
        return settings().getSectorBuilder().fromList(sectors);
    }

    @Override
    public Instance deserializeInternal(String featureData) {
        List<Sector> expectedSectors = deserializeSectors(featureData);
        return new Instance(featureData, expectedSectors);
    }

    @Override
    public List<String> generate(Line expectedLine) {
        return Collections.singletonList(Joiner.on(", ").join(convertLineToSectorList(expectedLine)));
    }

    /**
     * Convert a line into a list of highest-priority sectors that it passes through.
     * @param line The line.
     * @return The list of sectors the line passes through.
     */
    List<Sector> convertLineToSectorList(Line line) {
        List<Set<Sector>> sectors = convertLineToSectorSetList(line);

        List<Sector> output = new ArrayList<>();
        sectors.stream()
            .map(set -> settings().getOrderedSectors().stream().filter(set::contains).findFirst().orElse(null))
            .forEach(sector -> {
                if (output.isEmpty() || !output.get(output.size() - 1).equals(sector)) {
                    output.add(sector);
                }
            });

        return output;
    }

    /**
     * Convert a line into a list of sets of sectors that it passes through.
     *
     * For example, a line passing near an axis might return a list like: [topRight], [topRight, +Xaxis], [topRight]
     *
     * @param line The line.
     * @return The list of sets of sectors that the line passes through.
     */
    private List<Set<Sector>> convertLineToSectorSetList(Line line) {
        List<Set<Sector>> output = new ArrayList<>();

        Point lastPoint = null;
        for (Point point : line) {
            if (lastPoint != null) {
                classifyLineSegment(output, Segment.closed(lastPoint, point));
            }

            Set<Sector> pointSector = classifyPoint(point);

            addSector(output, pointSector);

            lastPoint = point;
        }

        return output;
    }

    private final List<Set<Sector>> invalidSectorSets;

    /**
     * Add a set of sectors to a list of sets of sectors, removing illegal combinations and de-duplicating.
     * @param output The list to add this sector set to.
     * @param sectors The set of sectors.
     */
    private void addSector(List<Set<Sector>> output, Set<Sector> sectors) {
        Objects.requireNonNull(sectors);

        // If you are in an area that contains both sides of an axis say, remove both sides.
        List<Set<Sector>> sectorsToRemove = invalidSectorSets.stream()
            .filter(sectors::containsAll)
            .collect(Collectors.toList());
        sectorsToRemove.forEach(sectors::removeAll);

        if (output.size() == 0 || !output.get(output.size() - 1).equals(sectors) && !sectors.isEmpty()) {
            output.add(sectors);
        }
    }

    /**
     * Identify which sector this point is in.
     * @param point The point to be classified.
     * @return The highest-priority Sector that point is in.
     */
    private Set<Sector> classifyPoint(Point point) {
        return settings().getSectorClassifier().classifyAll(point);
    }

    /**
     * Add any sector sets this Segment passes through onto a list of sector sets.
     * @param output The current list of sets of sectors.
     * @param lineSegment The segment to be added.
     */
    private void classifyLineSegment(List<Set<Sector>> output, Segment lineSegment) {
        // Calculate when we enter and leave the line segment
        IntersectionParams[] intersectionParams = settings().getOrderedSectors().stream()
            .map(sector -> sector.intersectionParams(lineSegment))
            .toArray(IntersectionParams[]::new);

        Boolean[] inside = settings().getOrderedSectors().stream()
            .map(sector -> sector.contains(lineSegment.getStart()))
            .toArray(Boolean[]::new);

        int index = lowestIndex(intersectionParams);
        while (index != -1) {
            IntersectionParams.IntersectionParam intersection = intersectionParams[index].remove(0);
            inside[index] = intersection.isInside();

            index = lowestIndex(intersectionParams);
            while (index != -1 && intersection.getT() == intersectionParams[index].get(0).getT()) {
                intersection = intersectionParams[index].remove(0);
                inside[index] = intersection.isInside();

                index = lowestIndex(intersectionParams);
            }

            // Record all of the sectors we are currently in
            @SuppressWarnings("checkstyle:avoidInlineConditionals")
            Set<Sector> internalSectors = Streams.zip(
                settings().getOrderedSectors().stream(),
                Arrays.stream(inside),
                (sector, in) -> in ? sector : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            addSector(output, internalSectors);
        }
    }

    /**
     * From an array of IntersectionParams, return the index of the one with the earliest T value, or -1 if empty.
     * @param intersectionParams The array of IntersectionParams to consider.
     * @return The index of the param with the lowest T value, or -1 if the array is empty.
     */
    private int lowestIndex(IntersectionParams[] intersectionParams) {
        int index = -1;
        double minParam = Double.MAX_VALUE;
        for (int i = 0; i < intersectionParams.length; i++) {
            if (intersectionParams[i].size() > 0) {
                double param = intersectionParams[i].get(0).getT();
                if (param < minParam) {
                    index = i;
                    minParam = param;
                }
            }
        }
        return index;
    }
}
