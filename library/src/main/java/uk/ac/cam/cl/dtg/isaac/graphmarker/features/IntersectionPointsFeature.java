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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.internals.InputFeature;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Lines;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Sector;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.SectorClassifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An input feature which matches an intersection between two lines.
 */
public class IntersectionPointsFeature extends InputFeature<IntersectionPointsFeature.Instance,
    SectorClassifier.Settings> {

    /**
     * Create a curve count feature with specified settings.
     * @param settings The settings.
     */
    IntersectionPointsFeature(SectorClassifier.Settings settings) {
        super(settings);
    }

    @Override
    public String tag() {
        return "intersects";
    }

    /**
     * An instance of the CurvesCount feature.
     */
    public class Instance extends InputFeature<?, ?>.Instance {

        private final String lineA;
        private final String lineB;
        private final List<Sector> sectors;

        /**
         * Create a curve count feature.
         */
        private Instance(String lineA, String lineB, List<Sector> sectors) {
            super(serialize(lineA, lineB, sectors), true);

            this.lineA = lineA;
            this.lineB = lineB;
            this.sectors = sectors;
        }

        @Override
        public boolean test(Input input) {
            AssignmentContext.Current().putIfAbsent(lineA, input.getLines());
            AssignmentContext.Current().putIfAbsent(lineB, input.getLines());

            Set<ImmutableMap<String, Line>> assignments = AssignmentContext.Current().getAssignments();

            Iterator<ImmutableMap<String, Line>> iterator = assignments.iterator();
            while (iterator.hasNext()) {
                ImmutableMap<String, Line> mapping = iterator.next();

                Line theLineA = mapping.get(lineA);
                Line theLineB = mapping.get(lineB);
                if (theLineA == theLineB) {
                    // This shouldn't be possible.
                    assert false;
                    continue;
                }
                List<Sector> matches = getIntersectionSectors(theLineA, theLineB);

                if (!matches.equals(sectors)) {
                    iterator.remove();
                }
            }

            return !assignments.isEmpty();
        }
    }

    private final Pattern syntaxPattern = Pattern.compile("([a-zA-Z]+)\\s+to\\s([a-zA-Z]+)\\s+at(.*)");

    private static String serialize(String lineA, String lineB, List<Sector> sectors) {
        return lineA + " to " + lineB + " at " + Joiner.on(", ").join(sectors);
    }

    @Override
    protected Instance deserializeInternal(String featureData) {
        Matcher m = syntaxPattern.matcher(featureData);
        if (m.find()) {
            return new Instance(m.group(1).trim(), m.group(2).trim(),
                settings().getSectorBuilder().fromList(m.group(3)));
        } else {
            throw new IllegalArgumentException("Not a intersection points feature: " + featureData);
        }
    }

    @Override
    public List<String> generate(Input expectedInput) {
        List<Line> lines = expectedInput.getLines();

        List<String> output = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            Line lineA = lines.get(i);
            for (int j = i + 1; j < lines.size(); j++) {
                Line lineB = lines.get(j);

                List<Sector> intersections = getIntersectionSectors(lineA, lineB);
                if (!intersections.isEmpty()) {
                    output.add(serialize(AssignmentContext.standardLineName(i),
                        AssignmentContext.standardLineName(j),
                        intersections));
                }
            }
        }

        return output;
    }

    private List<Sector> getIntersectionSectors(Line lineA, Line lineB) {
        return Lines.findIntersections(lineA, lineB).stream()
            .map(p -> settings().getSectorClassifier().classify(p))
            .collect(Collectors.toList());
    }
}
