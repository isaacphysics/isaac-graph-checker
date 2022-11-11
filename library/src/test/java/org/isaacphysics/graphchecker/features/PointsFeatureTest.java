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

import org.isaacphysics.graphchecker.TestHelpers;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;
import org.junit.Test;
import org.isaacphysics.graphchecker.data.Line;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PointsFeatureTest {

    private final PointsFeature pointsFeature = new PointsFeature(SettingsWrapper.DEFAULT);

    @Test
    public void simplePointsTest() {
        List<String> data = pointsFeature.generate(TestHelpers.lineOf(x -> x * x, -5, 5));

        Line passLine = TestHelpers.lineOf(Math::abs, -5, 5);
        Line failLine = TestHelpers.lineOf(x -> x, -5, 5);

        assertTrue(pointsFeature.deserializeInternal(data.get(0)).test(passLine));
        assertFalse(pointsFeature.deserializeInternal(data.get(0)).test(failLine));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustProvideTwoArguments() {
        pointsFeature.deserializeInternal("one,two,three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectNames() {
        pointsFeature.deserializeInternal("middle, flat");
    }

    @Test
    public void pointsFeature_curveWithPOIsMatchingSpec_matches(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "minima in bottomLeft, maxima in topRight";

        // Act
        boolean matches = pointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertTrue(matches);
    }

    @Test
    public void pointsFeature_curveWithPOIsMatchingAnySectorSpec_matches(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "minima in any, maxima in any";

        // Act
        boolean matches = pointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertTrue(matches);
    }

    @Test
    public void pointsFeature_curveWithPOIsMatchingPartialAnySectorSpec_matches(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "minima in any, maxima in topRight";

        // Act
        boolean matches = pointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertTrue(matches);
    }

    @Test
    public void pointsFeature_curveWithOutOfOrderPOIs_fails(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "maxima in topRight, minima in bottomLeft";

        // Act
        boolean matches = pointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertFalse(matches);
    }

    @Test
    public void pointsFeature_curveWithOutOfOrderPOIsAnySectorSpec_fails(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "maxima in any, minima in any";

        // Act
        boolean matches = pointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertFalse(matches);
    }

    @Test
    public void pointsFeature_curveWithPOIsNotMatchingSpec_fails(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "maxima in topLeft, minima in topRight";

        // Act
        boolean matches = pointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertFalse(matches);
    }
}
