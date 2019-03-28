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

import org.isaacphysics.graphchecker.data.Input;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;
import org.junit.Test;
import org.isaacphysics.graphchecker.TestHelpers;
import org.isaacphysics.graphchecker.data.Line;

import java.util.List;

import static org.junit.Assert.*;

public class IntersectionPointsFeatureTest {

    private IntersectionPointsFeature intersectionPointsFeature = new IntersectionPointsFeature(SettingsWrapper.DEFAULT);

    @Test
    public void testMatchIntersection() {
        IntersectionPointsFeature.Instance instance = intersectionPointsFeature.deserializeInternal("a to b at origin");

        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2 = TestHelpers.lineOf(x -> -x, -10, 10);
        Line line3 = TestHelpers.lineOf(x -> 0.0, -10, 10);
        Line line4 = TestHelpers.lineOf(x -> 3.0, -10, 10);
        Input input = TestHelpers.inputOf(line1, line2, line3, line4);

        Context context = new Context(input);

        Context match = instance.test(input, context);

        assertNotNull(match);

        assertEquals(6, match.getAssignmentsCopy().size());
    }

    @Test
    public void testMatchNonIntersection() {
        IntersectionPointsFeature.Instance instance = intersectionPointsFeature.deserializeInternal("a to b nowhere");

        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2 = TestHelpers.lineOf(x -> -x, -10, 10);
        Line line3 = TestHelpers.lineOf(x -> 0.0, -10, 10);
        Line line4 = TestHelpers.lineOf(x -> 3.0, -10, 10);
        Input input = TestHelpers.inputOf(line1, line2, line3, line4);

        Context context = new Context(input);

        Context match = instance.test(input, context);

        assertNotNull(match);

        assertEquals(2, match.getAssignmentsCopy().size());
    }
    @Test
    public void testGenerate() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2 = TestHelpers.lineOf(x -> -x, -10, 10);
        List<String> lines = intersectionPointsFeature.generate(TestHelpers.inputOf(line1, line2));

        assertEquals(1, lines.size());
        assertEquals("A to B at origin", lines.get(0));
    }

    @Test
    public void testGenerateNonIntersection() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2 = TestHelpers.lineOf(x -> x + 1, -10, 10);
        List<String> lines = intersectionPointsFeature.generate(TestHelpers.inputOf(line1, line2));

        assertEquals(1, lines.size());
        assertEquals("A to B nowhere", lines.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithNumberedLine() {
        intersectionPointsFeature.deserializeInternal("1 to 2 at origin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithUnknownSector() {
        intersectionPointsFeature.deserializeInternal("a to b at foo");
    }
}