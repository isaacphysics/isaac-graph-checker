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
package org.isaacphysics.graphchecker.features;

import com.google.common.collect.ImmutableList;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;
import org.isaacphysics.graphchecker.TestHelpers;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.Test;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.data.Point;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SymmetryFeatureTest {

    private SymmetryFeature symmetryFeature = new SymmetryFeature(SettingsWrapper.DEFAULT);

    @Test
    public void noSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.NONE, symmetryFeature.getSymmetryOfLine(TestHelpers.lineOf(x -> x*x + 2*x + 3, 0, 10)));
    }

    @Test
    public void evenSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.EVEN, symmetryFeature.getSymmetryOfLine(TestHelpers.lineOf(x -> x*x, -10, 10)));
    }

    @Test
    public void oddSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.ODD, symmetryFeature.getSymmetryOfLine(TestHelpers.lineOf(x -> 2*x, -10, 10)));
    }

    @Test
    public void checkFunctionSymmetry() {
        List<ImmutableTriple<String, Line, SymmetryFeature.SymmetryType>> functions = ImmutableList.<ImmutableTriple<String, Line, SymmetryFeature.SymmetryType>>builder()
            .add(ImmutableTriple.of("10", TestHelpers.lineOf(x -> 10.0, -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("|x|", TestHelpers.lineOf(Math::abs, -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("-|x|", TestHelpers.lineOf(x -> -Math.abs(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("1+sin(x)", TestHelpers.lineOf(x -> 1 + Math.sin(x), -Math.PI * 0.75, Math.PI * 0.75), SymmetryFeature.SymmetryType.ANTISYMMETRIC))
            .add(ImmutableTriple.of("1+x^3-x", TestHelpers.lineOf(x -> 1 + x*x*x - x, -5, 5), SymmetryFeature.SymmetryType.ANTISYMMETRIC))
            .add(ImmutableTriple.of("x", TestHelpers.lineOf(x -> x, -10, 10), SymmetryFeature.SymmetryType.ODD))
            .add(ImmutableTriple.of("x+1", TestHelpers.lineOf(x -> x + 1, -10, 10), SymmetryFeature.SymmetryType.ANTISYMMETRIC))
            .add(ImmutableTriple.of("1+|x|", TestHelpers.lineOf(x -> 1 + Math.abs(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("sin(x)", TestHelpers.lineOf(Math::sin, - Math.PI / 2, Math.PI / 2), SymmetryFeature.SymmetryType.ODD))
            .add(ImmutableTriple.of("cos(x)", TestHelpers.lineOf(Math::cos, -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("1+cos(x)", TestHelpers.lineOf(x -> 1 + Math.cos(x), -10, 10), SymmetryFeature.SymmetryType.EVEN))
            .add(ImmutableTriple.of("(x-2)(x-2)", TestHelpers.lineOf(x -> (x-2)*(x-2), -8, 12), SymmetryFeature.SymmetryType.SYMMETRIC))
            .build();

        functions.forEach((item) -> assertEquals(item.left + " should be " + item.right, item.right, symmetryFeature.getSymmetryOfLine(item.middle)));
    }

    @Test
    public void curveOnlyOnRightOfYaxisCanStillHaveSymmetry() {
        assertEquals(SymmetryFeature.SymmetryType.SYMMETRIC, symmetryFeature.getSymmetryOfLine(TestHelpers.lineOf(x -> x*x - 2 * x + 3, 0, 2)));
        assertEquals(SymmetryFeature.SymmetryType.ANTISYMMETRIC, symmetryFeature.getSymmetryOfLine(TestHelpers.lineOf(x -> x, 0, 10)));
    }

    @Test
    public void simpleSymmetryTestWorks() {
        List<String> data = symmetryFeature.generate(TestHelpers.lineOf(x -> x, -10, 10));

        Line line = TestHelpers.lineOf(x -> x * 1.5, -10, 10);
        symmetryFeature.deserializeInternal(data.get(0)).test(line);
    }

    @Test
    public void symmetricSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.SYMMETRIC, symmetryFeature.getSymmetryOfLine(TestHelpers.lineOf(x -> x*x + 2 * x + 3, -11, 9)));
    }

    @Test
    public void antiSymmetryIsDetectedCorrectly() {
        assertEquals(SymmetryFeature.SymmetryType.ANTISYMMETRIC, symmetryFeature.getSymmetryOfLine(TestHelpers.lineOf(x -> (x - 0.1) * (x - 0.3) * (x - 0.4), -0.24, 0.76)));
    }

    @Test
    public void noneSymmetryGeneratesNoFeature() {
        List<String> data = symmetryFeature.generate(TestHelpers.lineOf(x -> x < 0 ? 0 : x, -10, 10));

        assertEquals(0, data.size());
    }

    @Test
    public void nonSymmetryWithSameBoundingIsDetectedCorrectly() {
        Line line = TestHelpers.lineOf(-10,0, -9,1, 0,0, 1,-1, 10, 0);
        assertEquals(SymmetryFeature.SymmetryType.NONE, symmetryFeature.getSymmetryOfLine(line));
    }

    @Test
    public void emptyLineHasNoSymmetry() {
        Line line = TestHelpers.lineOf(new Point[]{});
        assertEquals(SymmetryFeature.SymmetryType.NONE, symmetryFeature.getSymmetryOfLine(line));
    }

}