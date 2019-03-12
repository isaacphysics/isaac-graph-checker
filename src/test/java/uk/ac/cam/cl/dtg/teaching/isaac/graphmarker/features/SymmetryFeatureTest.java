/**
 * Copyright 2019 University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        assertEquals(SymmetryFeature.SymmetryType.NONE, SymmetryFeature.manager.getSymmetryOfLine(lineOf(x -> x*x + 2*x + 3, 0, 10)));
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
            .add(ImmutableTriple.of("|x|", lineOf(x -> Math.abs(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("-|x|", lineOf(x -> -Math.abs(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("1+sin(x)", lineOf(x -> 1 + Math.sin(x), -Math.PI * 0.75, Math.PI * 0.75), SymmetryFeature.SymmetryType.ANTISYMMETRIC))
            .add(ImmutableTriple.of("1+x^3-x", lineOf(x -> 1 + x*x*x - x, -5, 5), SymmetryFeature.SymmetryType.ANTISYMMETRIC))
            .add(ImmutableTriple.of("x", lineOf(x -> x, -10, 10), SymmetryFeature.SymmetryType.ODD))
            .add(ImmutableTriple.of("x+1", lineOf(x -> x + 1, -10, 10), SymmetryFeature.SymmetryType.NONE))
            .add(ImmutableTriple.of("1+|x|", lineOf(x -> 1 + Math.abs(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("sin(x)", lineOf(x -> Math.sin(x), - Math.PI / 2, Math.PI / 2), SymmetryFeature.SymmetryType.ODD))
            .add(ImmutableTriple.of("cos(x)", lineOf(x -> Math.cos(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("1+cos(x)", lineOf(x -> 1 + Math.cos(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("(x-2)(x-2)", lineOf(x -> (x-2)*(x-2), -8, 12), SymmetryFeature.SymmetryType.SYMMETRIC))
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
        List<String> data = SymmetryFeature.manager.generate(lineOf(x -> x, -10, 10));

        Line line = lineOf(x -> x * 1.5, -10, 10);
        assertTrue(SymmetryFeature.manager.deserialize(data.get(0)).match(line));
    }

    @Test
    public void symmetricSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.SYMMETRIC, SymmetryFeature.manager.getSymmetryOfLine(lineOf(x -> x*x + 2 * x + 3, -11, 9)));
    }

    @Test
    public void antiSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.ANTISYMMETRIC, SymmetryFeature.manager.getSymmetryOfLine(lineOf(x -> (x - 0.1) * (x - 0.3) * (x - 0.4), -0.24, 0.76)));
    }

    @Test
    public void noneSymmetryGeneratesNoFeature() {
        List<String> data = SymmetryFeature.manager.generate(lineOf(x -> x < 0 ? 0 : x, -10, 10));

        assertEquals(0, data.size());
    }

    @Test
    public void nonSymmetryWithSameBoundingIsDetectedCorrectly() {
        Line line = lineOf(-10,0, -9,1, 0,0, 1,-1, 10, 0);
        assertEquals(SymmetryFeature.SymmetryType.NONE, SymmetryFeature.manager.getSymmetryOfLine(line));
    }
}