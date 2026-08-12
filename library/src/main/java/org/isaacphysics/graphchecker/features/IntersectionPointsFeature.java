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
import org.isaacphysics.graphchecker.data.Input;
import org.isaacphysics.graphchecker.features.internals.InputFeature;
import org.isaacphysics.graphchecker.geometry.SectorClassifier;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.geometry.Lines;
import org.isaacphysics.graphchecker.geometry.Sector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An input feature which matches an intersection between two lines.
 *
 * We match by putting the two named lines into the Context, and then checking if any of the pairs of lines have the
 * required intersections.
 */
public class IntersectionPointsFeature extends InputFeature<IntersectionPointsFeature.Instance,
    SectorClassifier.Settings> {

    /**
     * Create an intersection points feature with specified settings.
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
     * An instance of the intersection points feature.
     */
    public class Instance extends InputFeature<?, ?>.Instance {

        private final String lineA;
        private final String lineB;
        private final List<Sector> sectors;

        /**
         * Create an intersection points feature.
         * @param lineA The name of the first line.
         * @param lineB The name of the second line.
         * @param sectors The sectors these lines must intersect, left to right.
         */
        private Instance(String lineA, String lineB, List<Sector> sectors) {
            super(serialize(lineA, lineB, sectors), true);

            this.lineA = lineA;
            this.lineB = lineB;
            this.sectors = sectors;
        }

        @Override
        public Context test(Input input, Context context) {
            return context.makeNewContext(mapping -> {
                Line theLineA = mapping.get(lineA);
                Line theLineB = mapping.get(lineB);
                List<Sector> matches = getIntersectionSectors(theLineA, theLineB);

                return matches.equals(sectors);
            }, lineA, lineB);
        }
    }

    private final Pattern syntaxPattern = Pattern.compile(
        "([a-zA-Z]+)\\s+to\\s([a-zA-Z]+)\\s+((?:at|in|on)(.*)|nowhere)");

    /**
     * Convert a intersection points features to a string.
     * @param lineA The name of the first line.
     * @param lineB The name of the second line.
     * @param sectors The sectors where they intersect.
     * @return The feature specification.
     */
    private static String serialize(String lineA, String lineB, List<Sector> sectors) {
        String lines = lineA + " to " + lineB;
        if (sectors.isEmpty()) {
            return lines + " nowhere";
        } else {
            return lines + " at " + Joiner.on(", ").join(sectors);
        }
    }

    @Override
    @SuppressWarnings("magicNumber")
    protected Instance deserializeInternal(String featureData) {
        Matcher m = syntaxPattern.matcher(featureData);
        if (m.find()) {
            List<Sector> sectors;
            if (m.group(4) == null) {
                sectors = Collections.emptyList();
            } else {
                sectors = settings().getSectorBuilder().fromList(m.group(4), false);
            }
            return new Instance(m.group(1).trim(), m.group(2).trim(), sectors);
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
                output.add(serialize(Context.standardLineName(i),
                    Context.standardLineName(j),
                    intersections));
            }
        }

        return output;
    }

    /**
     * Get a list of the sectors of intersections between two lines.
     *
     * @param lineA The first line.
     * @param lineB The second line.
     * @return The list of sectors where an intersection occurs.
     */
    private List<Sector> getIntersectionSectors(Line lineA, Line lineB) {
        return Lines.findIntersections(lineA, lineB).stream()
            .map(p -> settings().getSectorClassifier().classify(p))
            .collect(Collectors.toList());
    }
}
