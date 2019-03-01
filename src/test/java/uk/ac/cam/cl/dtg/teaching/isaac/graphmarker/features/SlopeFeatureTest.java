package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class SlopeFeatureTest {

    private SlopeFeature slopeFeature = new SlopeFeature();

    @Test
    public void slopeCalculatorIsCorrect() {
        Map<Line, SlopeFeature.Slope> expectations = ImmutableMap.of(
            new Line(Arrays.asList(new Point(0, 0), new Point(10, 100))), SlopeFeature.Slope.UP,
            new Line(Arrays.asList(new Point(10, 0), new Point(15, -50))), SlopeFeature.Slope.DOWN,
            new Line(Arrays.asList(new Point(0, 0), new Point(100, -5))), SlopeFeature.Slope.FLAT,
            new Line(Arrays.asList(new Point(0, 0), new Point(-100, -5))), SlopeFeature.Slope.FLAT,
            new Line(Arrays.asList(new Point(0, 0), new Point(100, 100))), SlopeFeature.Slope.OTHER
        );

        expectations.forEach((line, slope) -> assertEquals(slope, slopeFeature.lineToSlope(line)));
    }

    @Test
    public void simpleSlopeTestWorks() {
        String data = slopeFeature.generate(lineOf(x -> 1 / x, 0.001, 10));

        assertTrue(slopeFeature.matcher(slopeFeature.deserialize(data))
            .test(lineOf(x -> 0.5 / x, 0.001, 10)));
    }


    @Test
    public void inverseOfXworks() {
        Predicate<Line> startMatcher = slopeFeature.matcher(slopeFeature.deserialize("start=down"));
        Predicate<Line> endMatcher = slopeFeature.matcher(slopeFeature.deserialize("end = flat"));

        assertTrue(startMatcher.test(lineOf(x -> 1 / x, 0.001, 15)));
        assertTrue(endMatcher.test(lineOf(x -> 1 / x, 0.001, 15)));

        assertFalse(startMatcher.test(lineOf(x -> 16 - x, 0.001, 15)));
        assertFalse(endMatcher.test(lineOf(x -> 16 - x, 0.001, 15)));
    }

    @Test
    public void inverseOfXworksAsOneMatcher() {
        Predicate<Line> matcher = slopeFeature.matcher(slopeFeature.deserialize("start=down, end = flat"));

        assertTrue(matcher.test(lineOf(x -> 1 / x, 0.001, 15)));

        assertFalse(matcher.test(lineOf(x -> 16 - x, 0.001, 15)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustProvideTwoArguments() {
        slopeFeature.deserialize("one,two,three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectNames() {
        slopeFeature.deserialize("middle, flat");
    }

    @Test
    public void slopeUpOrDownIsntOverSensitiveToX() {
        Predicate<Line> matcher = slopeFeature.matcher(slopeFeature.deserialize("all=down"));

        assertTrue(matcher.test(lineOf(new Point(0, 0), new Point(-1, -100))));
    }

    @Test
    public void inverseOfXgeneratesTwoSlopes() {
        String featureData = slopeFeature.generate(lineOf(x -> 1 / x, 0.001, 15));

        assertEquals(2, StringUtils.countMatches(featureData, '='));
    }


}