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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.data.PointType;
import org.isaacphysics.graphchecker.geometry.Sector;
import org.isaacphysics.graphchecker.geometry.SectorClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnorderedPointsFeature extends PointsFeature{

    /**
     * Create a points feature with specified settings.
     * @param settings The settings.
     */
    UnorderedPointsFeature(SectorClassifier.Settings settings) {
        super(settings);
    }

    @Override
    public String tag() {
        return "has-points";
    }

    protected class Instance extends PointsFeature.Instance {

        /**
         * Create an instance which expects these to appear somewhere in the line, regardless of order.
         *
         * @param featureData    The specification for this feature.
         * @param expectedPoints The points of interest.
         */
        Instance(String featureData, Set<ImmutablePair<PointType, Sector>> expectedPoints) {
            super(featureData, new ArrayList<>(expectedPoints));
        }

        @Override
        public boolean test(Line line){
            for (ImmutablePair<PointType, Sector> expected : expectedPoints) {
                if (line.getPointsOfInterest().stream().noneMatch(actual -> pointsMatch(expected, actual))){
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public Instance deserializeInternal(String featureData) {
        String[] items = featureData.split("\\s*,\\s*");
        return new Instance(featureData, Arrays.stream(items).map(this::deserializeItem).collect(Collectors.toSet()));
    }

    @Override
    public List<String> generate(Line expectedLine) {
        return Collections.singletonList(
                expectedLine.getPointsOfInterest().stream()
                        .map(point -> ImmutablePair.of(point.getPointType(), settings().getSectorClassifier().classify(point)))
                        .distinct()
                        .map(this::generatePointSpec)
                        .collect(Collectors.joining(", "))
        );
    }
}
