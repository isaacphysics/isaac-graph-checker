package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class SlopeFeatureTest {

    private SlopeFeature slopeFeature = new SlopeFeature();

    @Test
    public void slopeCalculatorIsCorrect() {
        Line l = new Line(Arrays.asList(new Point(0, 0), new Point(100, 100)));

        assertThat(slopeFeature.lineToSlope(l), closeTo(1, 0.001));
    }

    @Test
    public void slopeCalculatorIsCorrectForNegative() {
        Line l = new Line(Arrays.asList(new Point(0, 0), new Point(100, -100)));

        assertThat(slopeFeature.lineToSlope(l), closeTo(-1, 0.001));
    }

    @Test
    public void slopeCalculatorIsCorrectWithThreePoints() {
        Line l = new Line(Arrays.asList(new Point(0, 0), new Point(100, -100), new Point(100.001, -200)));

        assertThat(slopeFeature.lineToSlope(l), closeTo(-1.5, 0.001));
    }

    @Test
    public void simpleSlopeTestWorks() {
        String data = slopeFeature.generate(lineOf(x -> x, -10, 10));

        assertTrue(slopeFeature.matcher(slopeFeature.deserialize(data))
            .test(lineOf(x -> x * 1.5, -5, 15)));
    }
}