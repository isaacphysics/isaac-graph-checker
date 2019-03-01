package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.junit.Test;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.inputOf;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class CurvesCountFeatureTest {

    private CurvesCountFeature curvesCountFeature = new CurvesCountFeature();

    @Test
    public void simpleCurveCountWorks() {
        String data = curvesCountFeature.generate(inputOf(
            lineOf(x -> x, -10, 0),
            lineOf(x -> x, 0, 10)
        ));

        assertTrue(curvesCountFeature.matcher(curvesCountFeature.deserialize(data))
            .test(inputOf(lineOf(x -> 1.0, -10, 10), lineOf(x -> 0.0, -10, 10))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAnumberThrows() {
        curvesCountFeature.deserialize("foo");
    }
}