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
package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import com.google.common.collect.Streams;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Sector;
import org.apache.commons.lang3.tuple.ImmutablePair;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.PointType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A line feature which requires the line to contain points of interest of certain types in certain sectors.
 */
public class PointsFeature extends LineFeature<PointsFeature.Instance> {

    public static final PointsFeature manager = new PointsFeature();

    @Override
    public String tag() {
        return "points";
    }

    /**
     * An instance of the PointsOfInterest feature.
     */
    protected class Instance extends LineFeature<?>.Instance {

        private final List<ImmutablePair<PointType, Sector>> expectedPoints;

        /**
         * Create an instance which expects these points in order.
         * @param featureData The specification for this feature.
         * @param expectedPoints The points of interest.
         */
        Instance(String featureData, List<ImmutablePair<PointType, Sector>> expectedPoints) {
            super(featureData);
            this.expectedPoints = expectedPoints;
        }

        @Override
        public boolean test(Line line) {
            if (expectedPoints.size() != line.getPointsOfInterest().size()) {
                return false;
            }

            return Streams.zip(expectedPoints.stream(), line.getPointsOfInterest().stream(),
                (expected, actual) -> expected.getLeft() == actual.getPointType()
                    && Sector.classify(actual, Sector.defaultOrderedSectors).contains(expected.getRight()))
                .allMatch(Boolean::booleanValue);
        }
    }

    @Override
    public Instance deserializeInternal(String featureData) {
        String[] items = featureData.split("\\s*,\\s*");
        return new Instance(featureData, Arrays.stream(items)
            .map(item -> {
                String[] parts = item.split(" (in|on|at) ");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Incorrect number of point parts in: " + item);
                }
                PointType expectedType = PointType.valueOf(parts[0].trim().toUpperCase());
                Sector expectedSector = Sector.byName(parts[1].trim());
                return ImmutablePair.of(expectedType, expectedSector);
            })
            .collect(Collectors.toList()));
    }

    @Override
    public List<String> generate(Line expectedLine) {
        return Collections.singletonList(
            expectedLine.getPointsOfInterest().stream()
            .map(point -> ImmutablePair.of(point.getPointType(), Sector.classify(point)))
            .map(entry -> {
                Sector sector = entry.getRight();
                String sectorName = sector.toString();
                @SuppressWarnings("checkstyle:avoidInlineConditionals")
                String preposition = sector == Sector.origin ? "at" : sectorName.matches("[-+].*") ? "on" : "in";
                return entry.getLeft().humanName() + " " + preposition + " " + sectorName;
            })
            .collect(Collectors.joining(", "))
        );
    }

    /**
     * Use the manager singleton.
     */
    private PointsFeature() {
    }
}
