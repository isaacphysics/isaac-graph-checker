package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class PointsFeatureTest {

    @Test
    public void simpleSlopeTestWorks() {
        String data = PointsFeature.manager.generate(lineOf(x -> x * x, -10, 10));

        Line line = lineOf(x -> Math.abs(x), -10, 10);
        
        assertTrue(PointsFeature.manager.deserialize(data).match(line));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustProvideTwoArguments() {
        PointsFeature.manager.deserialize("one,two,three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectNames() {
        PointsFeature.manager.deserialize("middle, flat");
    }



}