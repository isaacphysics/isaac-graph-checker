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
package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;

import java.util.function.Predicate;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers.inputOf;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers.lineOf;

public class FeaturesTest {

    private final ExpectedSectorsFeature expectedSectorsFeature = new ExpectedSectorsFeature(SettingsWrapper.DEFAULT);

    @Test
    public void testMatcherDesrializesAndWorks() {
        Predicate<Input> testFeature = new Features().matcher("through:  -Xaxis, topLeft, -Xaxis, bottomLeft, origin, topRight, +Xaxis, bottomRight, +Xaxis");

        assertTrue(testFeature.test(inputOf(Math::sin, -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.test(inputOf(Math::cos, -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void testCombinedFeaturesWorks() {
        Predicate<Input> testFeature = new Features().matcher("through:  topLeft, +Yaxis, topRight\r\nsymmetry: even ");

        assertTrue(testFeature.test(inputOf(x -> x * x + 3, -10, 10)));
        assertFalse(testFeature.test(inputOf(x -> x > 0 ? x + 3 : x * x + 3, -10, 10)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingFeatureThrows() {
        new Features().matcher("foo#!!!1!!: bar?");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMismatchedDeserializeThrows() {
        expectedSectorsFeature.deserialize("foo#!!!");
    }

    @Test
    public void testLineFeatureWrapperFunctionWorks() {
        Predicate<Input> testFeature = new Features().matcher("through:  topLeft, +Yaxis, topRight\r\nsymmetry: even ");

        assertTrue(testFeature.test(inputOf(x -> x * x + 3, -10, 10)));
        assertFalse(testFeature.test(inputOf(x -> x > 0 ? x + 3 : x * x + 3, -10, 10)));
    }

    @Test
    public void testLineRecognitionWorks() {
        Predicate<Input> testFeature = new Features().matcher("line: 1; through:  bottomLeft\r\nline: 2; through: topRight");

        assertTrue(testFeature.test(inputOf(
            lineOf(x -> 1 / x, -10, -0.01),
            lineOf(x -> 1 / x, 0.01, 10)
        )));

        assertFalse(testFeature.test(inputOf(
            lineOf(x -> 1 / x, -10, -0.01)
        )));

    }

    @Test
    public void testMultipleLinesFailsIfOnlyOneExpected() {
        Predicate<Input> testFeature = new Features().matcher("through:  bottomRight, topLeft");

        assertFalse(testFeature.test(inputOf(
            lineOf(x -> x, -10, -0.01),
            lineOf(x -> x, 0.01, 10)
        )));
    }

    @Test
    public void testLineRecognitionExpectsLinesToBeInCorrectLeftToRightOrder() {
        Predicate<Input> testFeature = new Features().matcher("line: 1; through:  bottomLeft\r\nline: 2; through: topRight");

        assertFalse(testFeature.test(inputOf(
            lineOf(x -> 1 / x, 0.01, 10),
            lineOf(x -> 1 / x, -10, -0.01)
        )));
    }

    @Test
    public void testFeaturesInputFeaturesDeserializationWorks() {
        Predicate<Input> testFeature = new Features().matcher("curves: 2");

        assertFalse(testFeature.test(inputOf(x -> x, -1, 1)));
        assertTrue(testFeature.test(inputOf(lineOf(x -> x, -1, 1), lineOf(x -> -x, -1, 1))));
    }

    @Test
    public void testInverseX() {
        Predicate<Input> testFeature = new Features().matcher(String.join("\r\n",
            "curves:2",
            "line: 1; through:  bottomLeft",
            "line: 1; slope: start=flat",
            "line: 1; slope: end=down",
            "line: 2; through: topRight",
            "line: 2; slope: start = down",
            "line: 2; slope: end= flat"));

        assertTrue(testFeature.test(inputOf(
            lineOf(x -> 1 / x, -10, -0.01),
            lineOf(x -> 1 / x, 0.01, 10)
        )));
    }

    @Test
    public void testGenerateOnSingleLine() {
        String features = new Features().generate(inputOf(
            lineOf(x -> x * x + 2, -10, 10)
        ));
        assertTrue(features.contains("start=down, end=up"));
        assertTrue(features.contains("topRight"));
    }

    @Test
    public void testGenerateNonOverlappingLines() {
        String features = new Features().generate(inputOf(
            lineOf(x -> 1 / x, -10, -0.01),
            lineOf(x -> 1 / x, 0.01, 10)
        ));
        assertTrue(features.contains("line: 1;"));
        assertTrue(features.contains("line: 2;"));
        assertTrue(features.contains("start=flat"));
        assertTrue(features.contains("bottomLeft"));
        assertTrue(features.contains("topRight"));
    }

    @Test
    public void testGenerateOverlappingLines() {
        String features = new Features().generate(inputOf(
            lineOf(x -> x, -10, 10),
            lineOf(x -> -x, -10, 10)
        ));
        assertTrue(features.contains("match: A;"));
        assertTrue(features.contains("match: B;"));
        assertTrue(features.contains("intersects: A to B at origin"));
    }

    @Test
    public void testThreeOverlappingLines() {
        Predicate<Input> testFeature = new Features().matcher(String.join("\r\n",
            "curves:3",
            "match: a; through:  topLeft, +Yaxis, topRight",
            "match: a; slope: start=flat, end=flat",
            "match: b; through:  bottomLeft, origin, topRight",
            "match: c; through:  topLeft, origin, bottomRight",
            "curves: 3",
            "intersects: a to b at topRight",
            "intersects: a to c at topLeft",
            "intersects: b to c at origin"));

        assertTrue(testFeature.test(inputOf(
            lineOf(x -> x, -10, 10),
            lineOf(x -> -x, -10, 10),
            lineOf(x -> 3.0, -10, 10)
        )));

        assertFalse(testFeature.test(inputOf(
            lineOf(x -> x, -10, 10),
            lineOf(x -> -x, -10, 10),
            lineOf(x -> 0.0, -10, 10)
        )));
    }
}