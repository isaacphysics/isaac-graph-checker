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

import com.google.common.collect.ImmutableMap;
import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers.lineOf;

public class SlopeFeatureTest {

    @Test
    public void slopeCalculatorIsCorrect() {
        Map<Line, SlopeFeature.Slope> expectations = ImmutableMap.of(
            TestHelpers.lineOf(new Point(0, 0), new Point(10, 100)), SlopeFeature.Slope.UP,
            TestHelpers.lineOf(new Point(10, 0), new Point(15, -50)), SlopeFeature.Slope.DOWN,
            TestHelpers.lineOf(new Point(0, 0), new Point(100, -5)), SlopeFeature.Slope.FLAT,
            TestHelpers.lineOf(new Point(0, 0), new Point(-100, -5)), SlopeFeature.Slope.FLAT,
            TestHelpers.lineOf(new Point(0, 0), new Point(100, 100)), SlopeFeature.Slope.OTHER
        );

        expectations.forEach((line, slope) -> assertEquals(slope, SlopeFeature.manager.lineToSlope(line)));
    }

    @Test
    public void simpleSlopeTestWorks() {
        List<String> data = SlopeFeature.manager.generate(TestHelpers.lineOf(x -> 1 / x, 0.01, 10));

        Line line = TestHelpers.lineOf(x -> 0.5 / x, 0.001, 10);
        assertTrue(SlopeFeature.manager.deserialize(data.get(0)).match(line));
    }


    @Test
    public void inverseOfXworks() {
        Predicate<Line> startMatcher = line -> SlopeFeature.manager.deserialize("start=down").match(line);
        Predicate<Line> endMatcher = line -> SlopeFeature.manager.deserialize("end = flat").match(line);

        assertTrue(startMatcher.test(TestHelpers.lineOf(x -> 1 / x, 0.001, 15)));
        assertTrue(endMatcher.test(TestHelpers.lineOf(x -> 1 / x, 0.001, 15)));

        assertFalse(startMatcher.test(TestHelpers.lineOf(x -> 16 - x, 0.001, 15)));
        assertFalse(endMatcher.test(TestHelpers.lineOf(x -> 16 - x, 0.001, 15)));
    }

    @Test
    public void inverseOfXworksAsOneMatcher() {
        Predicate<Line> matcher = line -> SlopeFeature.manager.deserialize("start=down, end = flat").match(line);

        assertTrue(matcher.test(TestHelpers.lineOf(x -> 1 / x, 0.001, 15)));

        assertFalse(matcher.test(TestHelpers.lineOf(x -> 16 - x, 0.001, 15)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustProvideTwoArguments() {
        SlopeFeature.manager.deserialize("one=two=three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectRegionNames() {
        SlopeFeature.manager.deserialize("middle=flat");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectSlopeNames() {
        SlopeFeature.manager.deserialize("all=wibbly");
    }

    @Test
    public void slopeUpOrDownIsntOverSensitiveToX() {
        Predicate<Line> matcher = line -> SlopeFeature.manager.deserialize("start=down, end=down").match(line);

        assertTrue(matcher.test(TestHelpers.lineOf(new Point(0, 0), new Point(-1, -100))));
    }

    @Test
    public void inverseOfXgeneratesTwoSlopes() {
        List<String> featureData = SlopeFeature.manager.generate(TestHelpers.lineOf(x -> 1 / x, 0.01, 10));

        assertEquals(2, StringUtils.countMatches(featureData.get(0), '='));
    }

    @Test
    public void almostStraightDownSlopeGeneratesCorrectly() {
        String featureData = SlopeFeature.manager.generate(TestHelpers.lineOf(
            new Point(1, 0),
            new Point(1, -1),
            new Point(1.001, -2),
            new Point(1, -3),
            new Point(1.001, -4)
        )).get(0);

        assertTrue(featureData.contains("down"));
    }

    @Test
    public void straightDownSlopeGeneratesCorrectly() {
        String featureData = SlopeFeature.manager.generate(TestHelpers.lineOf(
            new Point(1, 0),
            new Point(1, -1),
            new Point(1, -2),
            new Point(1, -3),
            new Point(1, -4)
        )).get(0);

        assertTrue(featureData.contains("down"));
    }
}