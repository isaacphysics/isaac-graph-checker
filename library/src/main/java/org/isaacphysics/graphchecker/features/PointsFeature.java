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

import com.google.common.collect.Streams;
import org.isaacphysics.graphchecker.data.PointOfInterest;
import org.isaacphysics.graphchecker.features.internals.LineFeature;
import org.isaacphysics.graphchecker.geometry.Sector;
import org.isaacphysics.graphchecker.geometry.SectorBuilder;
import org.isaacphysics.graphchecker.geometry.SectorClassifier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.data.PointType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A line feature which requires the line to contain points of interest of certain types in an order, and each of those
 * points to be in a particular sector.
 */
public class PointsFeature extends LineFeature<PointsFeature.Instance, SectorClassifier.Settings> {

    /**
     * Create a points feature with specified settings.
     * @param settings The settings.
     */
    PointsFeature(SectorClassifier.Settings settings) {
        super(settings);
    }

    @Override
    public String tag() {
        return "points";
    }

    /**
     * An instance of the PointsOfInterest feature.
     */
    protected class Instance extends LineFeature<?, ?>.Instance {

        protected final List<ImmutablePair<PointType, Sector>> expectedPoints;

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

            return Streams.zip(expectedPoints.stream(), line.getPointsOfInterest().stream(), this::pointsMatch)
                    .allMatch(Boolean::booleanValue);
        }
        
        protected boolean pointsMatch(ImmutablePair<PointType, Sector> expected, PointOfInterest actual) {
            return expected.getLeft() == actual.getPointType()
                    && (expected.getRight() == settings().getSectorBuilder().byName(SectorBuilder.ANY)
                    || settings().getSectorClassifier().classifyAll(actual).contains(expected.getRight()));
        }
    }

    @Override
    public Instance deserializeInternal(String featureData) {
        String[] items = featureData.split("\\s*,\\s*");
        return new Instance(featureData, Arrays.stream(items).map(this::deserializeItem).collect(Collectors.toList()));
    }

    protected ImmutablePair<PointType, Sector> deserializeItem(String item) {
        String[] parts = item.split(" (in|on|at) ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Incorrect number of point parts in: " + item);
        }
        PointType expectedType = PointType.valueOf(parts[0].trim().toUpperCase());
        Sector expectedSector = settings().getSectorBuilder().byName(parts[1].trim());
        return ImmutablePair.of(expectedType, expectedSector);
    }

    @Override
    public List<String> generate(Line expectedLine) {
        return Collections.singletonList(
            expectedLine.getPointsOfInterest().stream()
            .map(point -> ImmutablePair.of(point.getPointType(), settings().getSectorClassifier().classify(point)))
            .map(this::generatePointSpec)
            .collect(Collectors.joining(", "))
        );
    }

    public String generatePointSpec(ImmutablePair<PointType, Sector> entry) {
        Sector sector = entry.getRight();
        String sectorName = sector.toString();
        @SuppressWarnings("checkstyle:avoidInlineConditionals")
        String preposition = sector == settings().getSectorBuilder().byName(SectorBuilder.ORIGIN) ? "at"
                : sectorName.matches("[-+].*") ? "on"
                : "in";
        return entry.getLeft().humanName() + " " + preposition + " " + sectorName;
    }
}
