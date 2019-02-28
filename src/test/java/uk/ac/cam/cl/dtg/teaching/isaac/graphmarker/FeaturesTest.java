package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features.Features;

import java.util.function.Predicate;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.TestHelpers.lineOf;

public class FeaturesTest {

    @Test
    public void testMatcherDesrializesAndWorks() {
        Predicate<Line> testFeature = Features.matcher("through:  onAxisWithNegativeX, topLeft, onAxisWithNegativeX, bottomLeft, origin, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX");

        assertTrue(testFeature.test(lineOf(x -> Math.sin(x), -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.test(lineOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void testCombinedFeaturesWorks() {
        Predicate<Line> testFeature = Features.matcher("through:  topLeft, +Yaxis, topRight\r\nsymmetry:even");

        assertTrue(testFeature.test(lineOf(x -> x * x + 3, -10, 10)));
        assertFalse(testFeature.test(lineOf(x -> x > 0 ? x + 3 : x * x + 3, -10, 10)));
    }

}