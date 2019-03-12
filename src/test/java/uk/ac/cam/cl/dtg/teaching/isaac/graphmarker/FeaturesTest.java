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
package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features.Features;

import java.util.function.Predicate;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.inputOf;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class FeaturesTest {

    @Test
    public void testMatcherDesrializesAndWorks() {
        Predicate<Input> testFeature = Features.matcher("through:  onAxisWithNegativeX, topLeft, onAxisWithNegativeX, bottomLeft, origin, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX");

        assertTrue(testFeature.test(inputOf(x -> Math.sin(x), -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.test(inputOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void testCombinedFeaturesWorks() {
        Predicate<Input> testFeature = Features.matcher("through:  topLeft, +Yaxis, topRight\r\nsymmetry:even");

        assertTrue(testFeature.test(inputOf(x -> x * x + 3, -10, 10)));
        assertFalse(testFeature.test(inputOf(x -> x > 0 ? x + 3 : x * x + 3, -10, 10)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingFeatureThrows() {
        Features.matcher("foo#!!!1!!: bar?");
    }

    @Test
    public void testLineRecognitionWorks() {
        Predicate<Input> testFeature = Features.matcher("line: 1; through:  bottomLeft\r\nline: 2; through: topRight");

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
        Predicate<Input> testFeature = Features.matcher("through:  bottomRight, topLeft");

        assertFalse(testFeature.test(inputOf(
            lineOf(x -> x, -10, -0.01),
            lineOf(x -> x, 0.01, 10)
        )));
    }

    @Test
    public void testLineRecognitionExpectsLinesToBeInCorrectLeftToRightOrder() {
        Predicate<Input> testFeature = Features.matcher("line: 1; through:  bottomLeft\r\nline: 2; through: topRight");

        assertFalse(testFeature.test(inputOf(
            lineOf(x -> 1 / x, 0.01, 10),
            lineOf(x -> 1 / x, -10, -0.01)
        )));
    }

    @Test
    public void testFeaturesInputFeaturesDeserializationWorks() {
        Predicate<Input> testFeature = Features.matcher("curves: 2");

        assertFalse(testFeature.test(inputOf(x -> x, -1, 1)));
        assertTrue(testFeature.test(inputOf(lineOf(x -> x, -1, 1), lineOf(x -> -x, -1, 1))));
    }

    @Test
    public void testInverseX() {
        Predicate<Input> testFeature = Features.matcher(String.join("\r\n",
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
    public void testGenerate() {
        String features = Features.generate(inputOf(
            lineOf(x -> 1 / x, -10, -0.01),
            lineOf(x -> 1 / x, 0.01, 10)
        ));
        assertTrue(features.contains("line: 1;"));
        assertTrue(features.contains("line: 2;"));
        assertTrue(features.contains("start=flat"));
        assertTrue(features.contains("bottomLeft"));
        assertTrue(features.contains("topRight"));
    }
}