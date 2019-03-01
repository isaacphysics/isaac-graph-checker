package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class SymmetryFeatureTest {

    @Test
    public void noSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.NONE, SymmetryFeature.manager.getSymmetryOfLine(lineOf(x -> x*x + 2*x + 3, -10, 10)));
    }


    @Test
    public void evenSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.EVEN, SymmetryFeature.manager.getSymmetryOfLine(lineOf(x -> x*x, -10, 10)));
    }

    @Test
    public void oddSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.ODD, SymmetryFeature.manager.getSymmetryOfLine(lineOf(x -> 2*x, -10, 10)));
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

        functions.forEach((item) -> assertEquals(item.left + " should be " + item.right, item.right, SymmetryFeature.manager.getSymmetryOfLine(item.middle)));
    }

    @Test
    public void curveOnlyOnRightOfYaxisHasNoSymmetry() {
        assertEquals(SymmetryFeature.SymmetryType.NONE, SymmetryFeature.manager.getSymmetryOfLine(lineOf(x -> x*x + 2 * x + 3, 0, 10)));
        assertEquals(SymmetryFeature.SymmetryType.NONE, SymmetryFeature.manager.getSymmetryOfLine(lineOf(x -> x, 0, 10)));
    }

    @Test
    public void simpleSymmetryTestWorks() {
        String data = SymmetryFeature.manager.generate(lineOf(x -> x, -10, 10));

        assertTrue(((Predicate<Line>) line -> SymmetryFeature.manager.deserialize(data).match(line))
            .test(lineOf(x -> x * 1.5, -10, 10)));
    }
}