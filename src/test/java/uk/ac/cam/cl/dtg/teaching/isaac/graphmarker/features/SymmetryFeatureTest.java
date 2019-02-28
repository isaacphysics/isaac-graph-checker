package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.TestHelpers.lineOf;

public class SymmetryFeatureTest {

    private SymmetryFeature symmetryFeature = new SymmetryFeature();

    @Test
    public void noSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.NONE, symmetryFeature.getSymmetryOfLine(lineOf(x -> x*x + 2*x + 3, -10, 10)));
    }


    @Test
    public void evenSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.EVEN, symmetryFeature.getSymmetryOfLine(lineOf(x -> x*x, -10, 10)));
    }

    @Test
    public void oddSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.ODD, symmetryFeature.getSymmetryOfLine(lineOf(x -> 2*x, -10, 10)));
    }

    @Test
    public void checkFunctionSymmetry() {
        List<ImmutableTriple<String, Line, SymmetryFeature.SymmetryType>> functions = ImmutableList.<ImmutableTriple<String, Line, SymmetryFeature.SymmetryType>>builder()
            .add(ImmutableTriple.of("10", lineOf(x -> 10.0, -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("x", lineOf(x -> x, -10, 10), SymmetryFeature.SymmetryType.ODD))
            .add(ImmutableTriple.of("|x|", lineOf(x -> Math.abs(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("-|x|", lineOf(x -> -Math.abs(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("x+1", lineOf(x -> x + 1, -10, 10), SymmetryFeature.SymmetryType.NONE))
            .add(ImmutableTriple.of("1+|x|", lineOf(x -> 1 + Math.abs(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("sin(x)", lineOf(x -> Math.sin(x), -10, 10), SymmetryFeature.SymmetryType.ODD))
            .add(ImmutableTriple.of("cos(x)", lineOf(x -> Math.cos(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("1+sin(x)", lineOf(x -> 1 + Math.sin(x), -10, 10), SymmetryFeature.SymmetryType.NONE))
            .add(ImmutableTriple.of("1+cos(x)", lineOf(x -> 1 + Math.cos(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .build();

        functions.forEach((item) -> assertEquals(item.left + " should be " + item.right, item.right, symmetryFeature.getSymmetryOfLine(item.middle)));
    }

    @Test
    public void curveOnlyOnRightOfYaxisHasNoSymmetry() {
        assertEquals(SymmetryFeature.SymmetryType.NONE, symmetryFeature.getSymmetryOfLine(lineOf(x -> x*x + 2 * x + 3, 0, 10)));
        assertEquals(SymmetryFeature.SymmetryType.NONE, symmetryFeature.getSymmetryOfLine(lineOf(x -> x, 0, 10)));
    }

    @Test
    public void simpleSymmetryTestWorks() {
        String data = symmetryFeature.generate(lineOf(x -> x, -10, 10));

        assertTrue(symmetryFeature.matcher(symmetryFeature.deserialize(data))
            .test(lineOf(x -> x * 1.5, -10, 10)));
    }
}