package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class PointsFeatureTest {

    private PointsFeature pointsFeature = new PointsFeature();

    @Test
    public void simpleSlopeTestWorks() {
        String data = pointsFeature.generate(lineOf(x -> x * x, -10, 10));

        assertTrue(pointsFeature.matcher(pointsFeature.deserialize(data))
            .test(lineOf(x -> Math.abs(x), -10, 10)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustProvideTwoArguments() {
        pointsFeature.deserialize("one,two,three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectNames() {
        pointsFeature.deserialize("middle, flat");
    }



}