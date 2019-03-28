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

import com.google.common.collect.ImmutableMap;
import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;
import org.isaacphysics.graphchecker.TestHelpers;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.isaacphysics.graphchecker.data.Line;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class SlopeFeatureTest {

    private SlopeFeature slopeFeature = new SlopeFeature(SettingsWrapper.DEFAULT);

    @Test
    public void slopeCalculatorIsCorrect() {
        Map<Line, SlopeFeature.Slope> expectations = ImmutableMap.of(
            TestHelpers.lineOf(new Point(0, 0), new Point(10, 100)), SlopeFeature.Slope.UP,
            TestHelpers.lineOf(new Point(10, 0), new Point(15, -50)), SlopeFeature.Slope.DOWN,
            TestHelpers.lineOf(new Point(0, 0), new Point(100, -5)), SlopeFeature.Slope.FLAT,
            TestHelpers.lineOf(new Point(0, 0), new Point(-100, -5)), SlopeFeature.Slope.FLAT,
            TestHelpers.lineOf(new Point(0, 0), new Point(100, 100)), SlopeFeature.Slope.OTHER
        );

        expectations.forEach((line, slope) -> assertEquals(slope, slopeFeature.lineToSlope(line)));
    }

    @Test
    public void simpleSlopeTestWorks() {
        List<String> data = slopeFeature.generate(TestHelpers.lineOf(x -> 1 / x, 0.01, 10));

        Line line = TestHelpers.lineOf(x -> 0.5 / x, 0.001, 10);
        assertTrue(slopeFeature.deserializeInternal(data.get(0)).test(line));
    }


    @Test
    public void inverseOfXworks() {
        Predicate<Line> startMatcher = line -> slopeFeature.deserializeInternal("start=down").test(line);
        Predicate<Line> endMatcher = line -> slopeFeature.deserializeInternal("end = flat").test(line);

        assertTrue(startMatcher.test(TestHelpers.lineOf(x -> 1 / x, 0.001, 15)));
        assertTrue(endMatcher.test(TestHelpers.lineOf(x -> 1 / x, 0.001, 15)));

        assertFalse(startMatcher.test(TestHelpers.lineOf(x -> 16 - x, 0.001, 15)));
        assertFalse(endMatcher.test(TestHelpers.lineOf(x -> 16 - x, 0.001, 15)));
    }

    @Test
    public void inverseOfXworksAsOneMatcher() {
        Predicate<Line> matcher = line -> slopeFeature.deserializeInternal("start=down, end = flat").test(line);

        assertTrue(matcher.test(TestHelpers.lineOf(x -> 1 / x, 0.001, 15)));

        assertFalse(matcher.test(TestHelpers.lineOf(x -> 16 - x, 0.001, 15)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustProvideTwoArguments() {
        slopeFeature.deserializeInternal("one=two=three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectRegionNames() {
        slopeFeature.deserializeInternal("middle=flat");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectSlopeNames() {
        slopeFeature.deserializeInternal("all=wibbly");
    }

    @Test
    public void slopeUpOrDownIsntOverSensitiveToX() {
        Predicate<Line> matcher = line -> slopeFeature.deserializeInternal("start=down, end=down").test(line);

        assertTrue(matcher.test(TestHelpers.lineOf(new Point(0, 0), new Point(-1, -100))));
    }

    @Test
    public void inverseOfXgeneratesTwoSlopes() {
        List<String> featureData = slopeFeature.generate(TestHelpers.lineOf(x -> 1 / x, 0.01, 10));

        assertEquals(2, StringUtils.countMatches(featureData.get(0), '='));
    }

    @Test
    public void almostStraightDownSlopeGeneratesCorrectly() {
        String featureData = slopeFeature.generate(TestHelpers.lineOf(
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
        String featureData = slopeFeature.generate(TestHelpers.lineOf(
            new Point(1, 0),
            new Point(1, -1),
            new Point(1, -2),
            new Point(1, -3),
            new Point(1, -4)
        )).get(0);

        assertTrue(featureData.contains("down"));
    }
}