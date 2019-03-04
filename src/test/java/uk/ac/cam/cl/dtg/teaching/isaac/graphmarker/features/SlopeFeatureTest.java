package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class SlopeFeatureTest {

    @Test
    public void slopeCalculatorIsCorrect() {
        Map<Line, SlopeFeature.Slope> expectations = ImmutableMap.of(
            lineOf(new Point(0, 0), new Point(10, 100)), SlopeFeature.Slope.UP,
            lineOf(new Point(10, 0), new Point(15, -50)), SlopeFeature.Slope.DOWN,
            lineOf(new Point(0, 0), new Point(100, -5)), SlopeFeature.Slope.FLAT,
            lineOf(new Point(0, 0), new Point(-100, -5)), SlopeFeature.Slope.FLAT,
            lineOf(new Point(0, 0), new Point(100, 100)), SlopeFeature.Slope.OTHER
        );

        expectations.forEach((line, slope) -> assertEquals(slope, SlopeFeature.manager.lineToSlope(line)));
    }

    @Test
    public void simpleSlopeTestWorks() {
        List<String> data = SlopeFeature.manager.generate(lineOf(x -> 1 / x, 0.01, 10));

        assertTrue(((Predicate<Line>) line -> SlopeFeature.manager.deserialize(data.get(0)).match(line))
            .test(lineOf(x -> 0.5 / x, 0.001, 10)));
    }


    @Test
    public void inverseOfXworks() {
        Predicate<Line> startMatcher = line -> SlopeFeature.manager.deserialize("start=down").match(line);
        Predicate<Line> endMatcher = line -> SlopeFeature.manager.deserialize("end = flat").match(line);

        assertTrue(startMatcher.test(lineOf(x -> 1 / x, 0.001, 15)));
        assertTrue(endMatcher.test(lineOf(x -> 1 / x, 0.001, 15)));

        assertFalse(startMatcher.test(lineOf(x -> 16 - x, 0.001, 15)));
        assertFalse(endMatcher.test(lineOf(x -> 16 - x, 0.001, 15)));
    }

    @Test
    public void inverseOfXworksAsOneMatcher() {
        Predicate<Line> matcher = line -> SlopeFeature.manager.deserialize("start=down, end = flat").match(line);

        assertTrue(matcher.test(lineOf(x -> 1 / x, 0.001, 15)));

        assertFalse(matcher.test(lineOf(x -> 16 - x, 0.001, 15)));
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

        assertTrue(matcher.test(lineOf(new Point(0, 0), new Point(-1, -100))));
    }

    @Test
    public void inverseOfXgeneratesTwoSlopes() {
        List<String> featureData = SlopeFeature.manager.generate(lineOf(x -> 1 / x, 0.01, 10));

        assertEquals(2, StringUtils.countMatches(featureData.get(0), '='));
    }

    @Test
    public void almostStraightDownSlopeGeneratesCorrectly() {
        String featureData = SlopeFeature.manager.generate(lineOf(
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
        String featureData = SlopeFeature.manager.generate(lineOf(
            new Point(1, 0),
            new Point(1, -1),
            new Point(1, -2),
            new Point(1, -3),
            new Point(1, -4)
        )).get(0);

        assertTrue(featureData.contains("down"));
    }
}