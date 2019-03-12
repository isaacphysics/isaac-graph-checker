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
package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Segment;

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
 * An line feature which requires the line to pass exactly through a list of sectors.
 */
public class ExpectedSectorsFeature implements LineFeature<ExpectedSectorsFeature.Instance> {

    public static final ExpectedSectorsFeature manager = new ExpectedSectorsFeature();

    private static final Logger log = LoggerFactory.getLogger(ExpectedSectorsFeature.class);

    @Override
    public String tag() {
        return "through";
    }

    private final List<Sector> orderedSectors;

    /**
     * An instance of the ExpectedSectors feature.
     */
    protected class Instance implements LineFeature.Instance {
        private final List<Sector> expectedSectors;

        /**
         * Create an instance which passes through these sectors.
         * @param expectedSectors The list of sectors an input must pass through.
         */
        Instance(List<Sector> expectedSectors) {
            this.expectedSectors = expectedSectors;
        }

        @Override
        public boolean match(Line line) {
            List<Set<Sector>> actualSectors = convertLineToSectorSetList(line);
            log.debug("User line passed through sectors: " + actualSectors);
            return match(expectedSectors, 0, actualSectors, 0);
        }

        /**
         * Recursive function to match an expected sector list against a list of sector sets.
         *
         * @param expected The list of expected sectors.
         * @param i The matched position so far in the expected sector list.
         * @param actual The list of sets of sectors to be matched.
         * @param j The matched position so far in the actual set of sectors list.
         * @return If there is a match.
         */
        @SuppressWarnings("RedundantIfStatement")
        private boolean match(List<Sector> expected, int i, List<Set<Sector>> actual, int j) {
            boolean expectedFinished = i >= expected.size();
            boolean actualFinished = j >= actual.size();
            if (expectedFinished) {
                return actualFinished;
            }
            if (actualFinished) {
                return false;
            }

            if (actual.get(j).isEmpty()) {
                return match(expected, i, actual, j + 1);
            }

            // TODO: There is probably a dynamic programming algorithm for this with much better worst-case performance.
            if (actual.get(j).contains(expected.get(i))) {
                if (match(expected, i, actual, j + 1)) {
                    return true;
                }

                if (match(expected, i + 1, actual, j)) {
                    return true;
                }
                if (match(expected, i + 1, actual, j + 1)) {
                    return true;
                }
            }
            return false;

        }
    }

    /**
     * Create a list of Sector objects from a list of sector names.
     * @param sectors A comma-separated list of sector names.
     * @return A list of Sectors.
     */
    private List<Sector> deserializeSectors(String sectors) {
        return Arrays.stream(sectors.split(","))
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .map(Sector::byName)
                .collect(Collectors.toList());
    }

    @Override
    public Instance deserialize(String featureData) {
        List<Sector> expectedSectors = deserializeSectors(featureData);
        return new Instance(expectedSectors);
    }

    /**
     * The manager singleton uses a default list of sectors.
     */
    private ExpectedSectorsFeature() {
        this.orderedSectors = Sector.defaultOrderedSectors;
    }

    /**
     * For testing, allow checking against a non-standard list of sectors.
     * @param orderedSectors The priority-ordered list of sectors to check against.
     */
    ExpectedSectorsFeature(List<Sector> orderedSectors) {
        this.orderedSectors = orderedSectors;
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
            .map(set -> orderedSectors.stream().filter(set::contains).findFirst().orElse(null))
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

    private final List<Set<Sector>> invalidSectorSets = ImmutableList.of(
        ImmutableSet.of(Sector.topRight, Sector.bottomRight),
        ImmutableSet.of(Sector.topLeft, Sector.bottomLeft),
        ImmutableSet.of(Sector.topRight, Sector.topLeft),
        ImmutableSet.of(Sector.bottomRight, Sector.bottomLeft),
        ImmutableSet.of(Sector.onAxisWithPositiveX, Sector.onAxisWithNegativeX),
        ImmutableSet.of(Sector.onAxisWithPositiveY, Sector.onAxisWithNegativeY)
    );

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

        if (output.size() == 0 || !output.get(output.size() - 1).equals(sectors)) {
            output.add(sectors);
        }
    }

    /**
     * Identify which sector this point is in.
     * @param point The point to be classified.
     * @return The highest-priority Sector that point is in.
     */
    private Set<Sector> classifyPoint(Point point) {
        return Sector.classify(point, orderedSectors);
    }

    /**
     * Add any sector sets this Segment passes through onto a list of sector sets.
     * @param output The current list of sets of sectors.
     * @param lineSegment The segment to be added.
     */
    private void classifyLineSegment(List<Set<Sector>> output, Segment lineSegment) {
        // Calculate when we enter and leave the line segment
        IntersectionParams[] intersectionParams = orderedSectors.stream()
            .map(sector -> sector.intersectionParams(lineSegment))
            .toArray(IntersectionParams[]::new);

        Boolean[] inside = orderedSectors.stream()
            .map(sector -> sector.contains(lineSegment.getStart()))
            .toArray(Boolean[]::new);

        int index = lowestIndex(intersectionParams);
        while (index != -1) {
            IntersectionParams.IntersectionParam intersection = intersectionParams[index].remove(0);

            inside[index] = intersection.isInside();

            // Record all of the sectors we are currently in
            @SuppressWarnings("checkstyle:avoidInlineConditionals")
            Set<Sector> internalSectors = Streams.zip(
                orderedSectors.stream(),
                Arrays.stream(inside),
                (sector, in) -> in ? sector : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            addSector(output, internalSectors);

            index = lowestIndex(intersectionParams);
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
