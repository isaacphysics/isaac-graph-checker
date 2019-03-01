package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;

import java.util.function.Predicate;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.inputOf;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class CurvesCountFeatureTest {

    @Test
    public void simpleCurveCountWorks() {
        String data = CurvesCountFeature.manager.generate(inputOf(
            lineOf(x -> x, -10, 0),
            lineOf(x -> x, 0, 10)
        ));

        Input input = inputOf(lineOf(x -> 1.0, -10, 10), lineOf(x -> 0.0, -10, 10));

        assertTrue(CurvesCountFeature.manager.deserialize(data).match(input));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAnumberThrows() {
        CurvesCountFeature.manager.deserialize("foo");
    }
}